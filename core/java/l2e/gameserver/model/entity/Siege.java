/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://eternity-world.ru/>.
 */
package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.instancemanager.SiegeGuardManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.SiegeManager.SiegeSpawn;
import l2e.gameserver.instancemanager.SiegeRewardManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2ClanMember;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2SiegeClan;
import l2e.gameserver.model.L2SiegeClan.SiegeClanType;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2ControlTowerInstance;
import l2e.gameserver.model.actor.instance.L2FlameTowerInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.RelationChanged;
import l2e.gameserver.network.serverpackets.SiegeInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.scripting.scriptengine.events.SiegeEvent;
import l2e.gameserver.scripting.scriptengine.impl.L2Script.EventStage;
import l2e.gameserver.scripting.scriptengine.listeners.events.SiegeListener;

public class Siege implements Siegable
{
	protected static final Logger _log = Logger.getLogger(Siege.class.getName());
	
	private static final List<SiegeListener> siegeListeners = new FastList<SiegeListener>().shared();
	
	public static final byte OWNER = -1;
	public static final byte DEFENDER = 0;
	public static final byte ATTACKER = 1;
	public static final byte DEFENDER_NOT_APPROWED = 2;
	
	public static enum TeleportWhoType
	{
		All,
		Attacker,
		DefenderNotOwner,
		Owner,
		Spectator
	}
	
	private int _controlTowerCount;
	private int _controlTowerMaxCount;
	private int _flameTowerCount;
	private int _flameTowerMaxCount;
	
	public class ScheduleEndSiegeTask implements Runnable
	{
		private final Castle _castleInst;
		
		public ScheduleEndSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}
		
		@Override
		public void run()
		{
			if (!getIsInProgress())
			{
				return;
			}
			
			try
			{
				long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 3600000)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HOURS_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber(2);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 3600000);
				}
				else if ((timeRemaining <= 3600000) && (timeRemaining > 600000))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber((int) timeRemaining / 60000);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 600000);
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber((int) timeRemaining / 60000);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 300000);
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber((int) timeRemaining / 60000);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 10000);
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CASTLE_SIEGE_S1_SECONDS_LEFT);
					sm.addNumber((int) timeRemaining / 1000);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining);
				}
				else
				{
					_castleInst.getSiege().endSiege();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Castle _castleInst;
		
		public ScheduleStartSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}
		
		@Override
		public void run()
		{
			_scheduledStartSiegeTask.cancel(false);
			if (getIsInProgress())
			{
				return;
			}
			
			try
			{
				if (!getIsTimeRegistrationOver())
				{
					long regTimeRemaining = getTimeRegistrationOverDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
					if (regTimeRemaining > 0)
					{
						_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), regTimeRemaining);
						return;
					}
					endTimeRegistration(true);
				}
				
				long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 86400000)
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 86400000);
				}
				else if ((timeRemaining <= 86400000) && (timeRemaining > 13600000))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
					sm.addCastleId(getCastle().getId());
					Announcements.getInstance().announceToAll(sm);
					_isRegistrationOver = true;
					clearSiegeWaitingClan();
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 13600000);
				}
				else if ((timeRemaining <= 13600000) && (timeRemaining > 600000))
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 600000);
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 300000);
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 10000);
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining);
				}
				else
				{
					_castleInst.getSiege().startSiege();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	private final List<L2SiegeClan> _attackerClans = new FastList<>();
	private final List<L2SiegeClan> _defenderClans = new FastList<>();
	private final List<L2SiegeClan> _defenderWaitingClans = new FastList<>();
	
	private List<L2ControlTowerInstance> _controlTowers = new ArrayList<>();
	private List<L2FlameTowerInstance> _flameTowers = new ArrayList<>();
	private final Castle[] _castle;
	private boolean _isInProgress = false;
	private boolean _isNormalSide = true;
	protected boolean _isRegistrationOver = false;
	protected Calendar _siegeEndDate;
	private SiegeGuardManager _siegeGuardManager;
	protected ScheduledFuture<?> _scheduledStartSiegeTask = null;
	protected int _firstOwnerClanId = -1;
	
	public Siege(Castle[] castle)
	{
		_castle = castle;
		_siegeGuardManager = new SiegeGuardManager(getCastle());
		
		startAutoTask();
	}
	
	@Override
	public void endSiege()
	{
		if (getIsInProgress())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
			sm.addCastleId(getCastle().getId());
			Announcements.getInstance().announceToAll(sm);
			
			if (getCastle().getOwnerId() > 0)
			{
				L2Clan clan = ClanHolder.getInstance().getClan(getCastle().getOwnerId());
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
				sm.addString(clan.getName());
				sm.addCastleId(getCastle().getId());
				Announcements.getInstance().announceToAll(sm);
				
				if (clan.getId() == _firstOwnerClanId)
				{
					clan.increaseBloodAllianceCount();
				}
				else
				{
					getCastle().setTicketBuyCount(0);
					for (L2ClanMember member : clan.getMembers())
					{
						if (member != null)
						{
							L2PcInstance player = member.getPlayerInstance();
							if ((player != null) && player.isNoble())
							{
								Hero.getInstance().setCastleTaken(player.getObjectId(), getCastle().getId());
							}
						}
					}
				}
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
				sm.addCastleId(getCastle().getId());
				Announcements.getInstance().announceToAll(sm);
			}
			
			for (L2SiegeClan attackerClan : getAttackerClans())
			{
				final L2Clan clan = ClanHolder.getInstance().getClan(attackerClan.getClanId());
				if (clan == null)
				{
					continue;
				}
				
				clan.clearSiegeKills();
				clan.clearSiegeDeaths();
			}
			
			for (L2SiegeClan defenderClan : getDefenderClans())
			{
				final L2Clan clan = ClanHolder.getInstance().getClan(defenderClan.getClanId());
				if (clan == null)
				{
					continue;
				}
				
				clan.clearSiegeKills();
				clan.clearSiegeDeaths();
			}
			
			getCastle().updateClansReputation();
			removeFlags();
			teleportPlayer(Siege.TeleportWhoType.Attacker, TeleportWhereType.TOWN);
			teleportPlayer(Siege.TeleportWhoType.DefenderNotOwner, TeleportWhereType.TOWN);
			teleportPlayer(Siege.TeleportWhoType.Spectator, TeleportWhereType.TOWN);
			_isInProgress = false;
			updatePlayerSiegeStateFlags(true);
			saveCastleSiege();
			clearSiegeClan();
			removeControlTower();
			removeFlameTower();
			_siegeGuardManager.unspawnSiegeGuard();
			if (getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs();
			}
			getCastle().spawnDoor();
			getCastle().getZone().setIsActive(false);
			getCastle().getZone().updateZoneStatusForCharactersInside();
			getCastle().getZone().setSiegeInstance(null);
			if (getCastle().getOwnerId() > 0)
			{
				SiegeRewardManager.getInstance().notifySiegeEnded(ClanHolder.getInstance().getClan(getCastle().getOwnerId()), getCastle().getName());
			}
			fireSiegeListeners(EventStage.END);
		}
	}
	
	private void removeDefender(L2SiegeClan sc)
	{
		if (sc != null)
		{
			getDefenderClans().remove(sc);
		}
	}
	
	private void removeAttacker(L2SiegeClan sc)
	{
		if (sc != null)
		{
			getAttackerClans().remove(sc);
		}
	}
	
	private void addDefender(L2SiegeClan sc, SiegeClanType type)
	{
		if (sc == null)
		{
			return;
		}
		sc.setType(type);
		getDefenderClans().add(sc);
	}
	
	private void addAttacker(L2SiegeClan sc)
	{
		if (sc == null)
		{
			return;
		}
		sc.setType(SiegeClanType.ATTACKER);
		getAttackerClans().add(sc);
	}
	
	public synchronized void midVictory(L2Clan oldOwner)
	{
		if (getIsInProgress())
		{
			if (getCastle().getOwnerId() > 0)
			{
				_siegeGuardManager.removeMercs();
			}
			
			if (getDefenderClans().isEmpty() && (getAttackerClans().size() == 1))
			{
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				endSiege();
				return;
			}
			
			if (getCastle().getOwnerId() > 0)
			{
				int allyId = ClanHolder.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
				if (getDefenderClans().isEmpty())
				{
					if (allyId != 0)
					{
						boolean allinsamealliance = true;
						for (L2SiegeClan sc : getAttackerClans())
						{
							if (sc != null)
							{
								if (ClanHolder.getInstance().getClan(sc.getClanId()).getAllyId() != allyId)
								{
									allinsamealliance = false;
								}
							}
						}
						if (allinsamealliance)
						{
							L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
							removeAttacker(sc_newowner);
							addDefender(sc_newowner, SiegeClanType.OWNER);
							endSiege();
							return;
						}
					}
				}
				
				for (L2SiegeClan sc : getDefenderClans())
				{
					if (sc != null)
					{
						removeDefender(sc);
						addAttacker(sc);
					}
				}
				
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				
				if (oldOwner != null)
				{
					addAttacker(oldOwner.getId());
				}
				
				removeDefenderFlags();
				getCastle().removeUpgrade();
				getCastle().spawnDoor(true);
				removeControlTower();
				removeFlameTower();
				_controlTowerCount = 0;
				_controlTowerMaxCount = 0;
				_flameTowerCount = 0;
				_flameTowerMaxCount = 0;
				spawnControlTower(getCastle().getId());
				spawnFlameTower(getCastle().getId());
				updatePlayerSiegeStateFlags(false);
				teleportPlayer(TeleportWhoType.Attacker, TeleportWhereType.SIEGEFLAG);
				teleportPlayer(TeleportWhoType.Spectator, TeleportWhereType.TOWN);
				fireSiegeListeners(EventStage.CONTROL_CHANGE);
			}
		}
	}
	
	@Override
	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (!fireSiegeListeners(EventStage.START))
			{
				return;
			}
			
			_firstOwnerClanId = getCastle().getOwnerId();
			
			if (getAttackerClans().isEmpty())
			{
				SystemMessage sm;
				if (_firstOwnerClanId <= 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
					final L2Clan ownerClan = ClanHolder.getInstance().getClan(_firstOwnerClanId);
					ownerClan.increaseBloodAllianceCount();
				}
				sm.addCastleId(getCastle().getId());
				Announcements.getInstance().announceToAll(sm);
				saveCastleSiege();
				return;
			}
			
			_isNormalSide = true;
			_isInProgress = true;
			
			loadSiegeClan();
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(Siege.TeleportWhoType.Attacker, TeleportWhereType.TOWN);
			_controlTowerCount = 0;
			_controlTowerMaxCount = 0;
			spawnControlTower(getCastle().getId());
			spawnFlameTower(getCastle().getId());
			getCastle().spawnDoor();
			spawnSiegeGuard();
			MercTicketManager.getInstance().deleteTickets(getCastle().getId());
			getCastle().getZone().setSiegeInstance(this);
			getCastle().getZone().setIsActive(true);
			getCastle().getZone().updateZoneStatusForCharactersInside();
			
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, SiegeManager.getInstance().getSiegeLength());
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getCastle()), 1000);
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_STARTED);
			sm.addCastleId(getCastle().getId());
			Announcements.getInstance().announceToAll(sm);
		}
	}
	
	public void announceToPlayer(SystemMessage message, boolean bothSides)
	{
		for (L2SiegeClan siegeClans : getDefenderClans())
		{
			L2Clan clan = ClanHolder.getInstance().getClan(siegeClans.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member != null)
				{
					member.sendPacket(message);
				}
			}
		}
		
		if (bothSides)
		{
			for (L2SiegeClan siegeClans : getAttackerClans())
			{
				L2Clan clan = ClanHolder.getInstance().getClan(siegeClans.getClanId());
				for (L2PcInstance member : clan.getOnlineMembers(0))
				{
					if (member != null)
					{
						member.sendPacket(message);
					}
				}
			}
		}
	}
	
	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			if (siegeclan == null)
			{
				continue;
			}
			
			clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member == null)
				{
					continue;
				}
				
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setSiegeSide(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				}
				else
				{
					member.setSiegeState((byte) 1);
					member.setSiegeSide(getCastle().getId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.sendPacket(new UserInfo(member));
				member.sendPacket(new ExBrExtraUserInfo(member));
				for (L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					if (player == null)
					{
						continue;
					}
					
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
					if (member.hasSummon())
					{
						player.sendPacket(new RelationChanged(member.getSummon(), member.getRelation(player), member.isAutoAttackable(player)));
					}
				}
			}
		}
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			if (siegeclan == null)
			{
				continue;
			}
			
			clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member == null)
				{
					continue;
				}
				
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setSiegeSide(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				}
				else
				{
					member.setSiegeState((byte) 2);
					member.setSiegeSide(getCastle().getId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.sendPacket(new UserInfo(member));
				member.sendPacket(new ExBrExtraUserInfo(member));
				
				for (L2PcInstance player : member.getKnownList().getKnownPlayers().values())
				{
					if (player == null)
					{
						continue;
					}
					player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
					if (member.hasSummon())
					{
						player.sendPacket(new RelationChanged(member.getSummon(), member.getRelation(player), member.isAutoAttackable(player)));
					}
				}
			}
		}
	}
	
	public void approveSiegeDefenderClan(int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		saveSiegeClan(ClanHolder.getInstance().getClan(clanId), DEFENDER, true);
		loadSiegeClan();
	}
	
	public boolean checkIfInZone(L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	public boolean checkIfInZone(int x, int y, int z)
	{
		return (getIsInProgress() && (getCastle().checkIfInZone(x, y, z)));
	}
	
	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		return (getAttackerClan(clan) != null);
	}
	
	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		return (getDefenderClan(clan) != null);
	}
	
	public boolean checkIsDefenderWaiting(L2Clan clan)
	{
		return (getDefenderWaitingClan(clan) != null);
	}
	
	public void clearSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?"))
		{
			statement.setInt(1, getCastle().getId());
			statement.execute();
			
			if (getCastle().getOwnerId() > 0)
			{
				try (PreparedStatement delete = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?"))
				{
					delete.setInt(1, getCastle().getOwnerId());
					delete.execute();
				}
			}
			
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: clearSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	public void clearSiegeWaitingClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and type = 2"))
		{
			statement.setInt(1, getCastle().getId());
			statement.execute();
			
			getDefenderWaitingClans().clear();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: clearSiegeWaitingClan(): " + e.getMessage(), e);
		}
	}
	
	@Override
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				if (player == null)
				{
					continue;
				}
				
				if (player.isInSiege())
				{
					players.add(player);
				}
			}
		}
		return players;
	}
	
	public List<L2PcInstance> getDefendersButNotOwnersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
			if (clan.getId() == getCastle().getOwnerId())
			{
				continue;
			}
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				if (player == null)
				{
					continue;
				}
				
				if (player.isInSiege())
				{
					players.add(player);
				}
			}
		}
		return players;
	}
	
	public List<L2PcInstance> getPlayersInZone()
	{
		return getCastle().getZone().getPlayersInside();
	}
	
	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
			if (clan.getId() != getCastle().getOwnerId())
			{
				continue;
			}
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				if (player == null)
				{
					continue;
				}
				
				if (player.isInSiege())
				{
					players.add(player);
				}
			}
		}
		return players;
	}
	
	public List<L2PcInstance> getSpectatorsInZone()
	{
		List<L2PcInstance> players = new ArrayList<>();
		
		for (L2PcInstance player : getCastle().getZone().getPlayersInside())
		{
			if (player == null)
			{
				continue;
			}
			
			if (!player.isInSiege())
			{
				players.add(player);
			}
		}
		return players;
	}
	
	public void killedCT(L2Npc ct)
	{
		_controlTowerCount--;
		if (_controlTowerCount < 0)
		{
			_controlTowerCount = 0;
		}
	}
	
	public void killedFlag(L2Npc flag)
	{
		if (flag == null)
		{
			return;
		}
		for (L2SiegeClan clan : getAttackerClans())
		{
			if (clan.removeFlag(flag))
			{
				return;
			}
		}
	}
	
	public void listRegisterClan(L2PcInstance player)
	{
		player.sendPacket(new SiegeInfo(getCastle()));
	}
	
	public void registerAttacker(L2PcInstance player)
	{
		registerAttacker(player, false);
	}
	
	public void registerAttacker(L2PcInstance player, boolean force)
	{
		if (player.getClan() == null)
		{
			return;
		}
		int allyId = 0;
		if (getCastle().getOwnerId() != 0)
		{
			allyId = ClanHolder.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
		}
		if (allyId != 0)
		{
			if ((player.getClan().getAllyId() == allyId) && !force)
			{
				player.sendPacket(SystemMessageId.CANNOT_ATTACK_ALLIANCE_CASTLE);
				return;
			}
		}
		if (force)
		{
			if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getId()))
			{
				player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
			}
			else
			{
				saveSiegeClan(player.getClan(), ATTACKER, false);
			}
			return;
		}
		
		if (checkIfCanRegister(player, ATTACKER))
		{
			saveSiegeClan(player.getClan(), ATTACKER, false);
		}
	}
	
	public void registerDefender(L2PcInstance player)
	{
		registerDefender(player, false);
	}
	
	public void registerDefender(L2PcInstance player, boolean force)
	{
		if (getCastle().getOwnerId() <= 0)
		{
			player.sendMessage("You cannot register as a defender because " + getCastle().getName() + " is owned by NPC.");
			return;
		}
		
		if (force)
		{
			if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getId()))
			{
				player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
			}
			else
			{
				saveSiegeClan(player.getClan(), DEFENDER_NOT_APPROWED, false);
			}
			return;
		}
		
		if (checkIfCanRegister(player, DEFENDER_NOT_APPROWED))
		{
			saveSiegeClan(player.getClan(), DEFENDER_NOT_APPROWED, false);
		}
	}
	
	public void removeSiegeClan(int clanId)
	{
		if (clanId <= 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and clan_id=?"))
		{
			statement.setInt(1, getCastle().getId());
			statement.setInt(2, clanId);
			statement.execute();
			
			loadSiegeClan();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: removeSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	public void removeSiegeClan(L2Clan clan)
	{
		if ((clan == null) || (clan.getCastleId() == getCastle().getId()) || !SiegeManager.getInstance().checkIsRegistered(clan, getCastle().getId()))
		{
			return;
		}
		removeSiegeClan(clan.getId());
	}
	
	public void removeSiegeClan(L2PcInstance player)
	{
		removeSiegeClan(player.getClan());
	}
	
	public void startAutoTask()
	{
		correctSiegeDateTime();
		
		_log.info("Siege of " + getCastle().getName() + ": " + getCastle().getSiegeDate().getTime());
		
		loadSiegeClan();
		
		if (_scheduledStartSiegeTask != null)
		{
			_scheduledStartSiegeTask.cancel(false);
		}
		_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new Siege.ScheduleStartSiegeTask(getCastle()), 1000);
	}
	
	public void teleportPlayer(TeleportWhoType teleportWho, TeleportWhereType teleportWhere)
	{
		List<L2PcInstance> players;
		switch (teleportWho)
		{
			case Owner:
				players = getOwnersInZone();
				break;
			case Attacker:
				players = getAttackersInZone();
				break;
			case DefenderNotOwner:
				players = getDefendersButNotOwnersInZone();
				break;
			case Spectator:
				players = getSpectatorsInZone();
				break;
			default:
				players = getPlayersInZone();
		}
		
		for (L2PcInstance player : players)
		{
			if (player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS) || player.isJailed())
			{
				continue;
			}
			player.teleToLocation(teleportWhere);
		}
	}
	
	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER));
	}
	
	private void addDefender(int clanId)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER));
	}
	
	private void addDefender(int clanId, SiegeClanType type)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, type));
	}
	
	private void addDefenderWaiting(int clanId)
	{
		getDefenderWaitingClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING));
	}
	
	private boolean checkIfCanRegister(L2PcInstance player, byte typeId)
	{
		if (getIsRegistrationOver())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED);
			sm.addCastleId(getCastle().getId());
			player.sendPacket(sm);
		}
		else if (getIsInProgress())
		{
			player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
		}
		else if ((player.getClan() == null) || (player.getClan().getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel()))
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEVEL_5_ABOVE_MAY_SIEGE);
		}
		else if (player.getClan().getId() == getCastle().getOwnerId())
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		}
		else if (player.getClan().getCastleId() > 0)
		{
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
		}
		else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getId()))
		{
			player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
		}
		else if (checkIfAlreadyRegisteredForSameDay(player.getClan()))
		{
			player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
		}
		else if ((typeId == ATTACKER) && (getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans()))
		{
			player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
		}
		else if (((typeId == DEFENDER) || (typeId == DEFENDER_NOT_APPROWED) || (typeId == OWNER)) && ((getDefenderClans().size() + getDefenderWaitingClans().size()) >= SiegeManager.getInstance().getDefenderMaxClans()))
		{
			player.sendPacket(SystemMessageId.DEFENDER_SIDE_FULL);
		}
		else
		{
			return true;
		}
		
		return false;
	}
	
	public boolean checkIfAlreadyRegisteredForSameDay(L2Clan clan)
	{
		for (Siege siege : SiegeManager.getInstance().getSieges())
		{
			if (siege == this)
			{
				continue;
			}
			if (siege.getSiegeDate().get(Calendar.DAY_OF_WEEK) == this.getSiegeDate().get(Calendar.DAY_OF_WEEK))
			{
				if (siege.checkIsAttacker(clan))
				{
					return true;
				}
				if (siege.checkIsDefender(clan))
				{
					return true;
				}
				if (siege.checkIsDefenderWaiting(clan))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public void correctSiegeDateTime()
	{
		boolean corrected = false;
		
		if (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			corrected = true;
			setNextSiegeDate();
		}
		
		if (!SevenSigns.getInstance().isDateInSealValidPeriod(getCastle().getSiegeDate()))
		{
			corrected = true;
			setNextSiegeDate();
		}
		
		if (corrected)
		{
			saveSiegeDate();
		}
	}
	
	private void loadSiegeClan()
	{
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans where castle_id=?"))
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
			
			if (getCastle().getOwnerId() > 0)
			{
				addDefender(getCastle().getOwnerId(), SiegeClanType.OWNER);
			}
			
			statement.setInt(1, getCastle().getId());
			try (ResultSet rs = statement.executeQuery())
			{
				int typeId;
				while (rs.next())
				{
					typeId = rs.getInt("type");
					if (typeId == DEFENDER)
					{
						addDefender(rs.getInt("clan_id"));
					}
					else if (typeId == ATTACKER)
					{
						addAttacker(rs.getInt("clan_id"));
					}
					else if (typeId == DEFENDER_NOT_APPROWED)
					{
						addDefenderWaiting(rs.getInt("clan_id"));
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: loadSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	private void removeControlTower()
	{
		if ((_controlTowers != null) && !_controlTowers.isEmpty())
		{
			for (L2ControlTowerInstance ct : _controlTowers)
			{
				if (ct != null)
				{
					try
					{
						ct.deleteMe();
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING, "Exception: removeControlTower(): " + e.getMessage(), e);
					}
				}
			}
			_controlTowers.clear();
			_controlTowers = null;
		}
	}
	
	private void removeFlameTower()
	{
		if ((_flameTowers != null) && !_flameTowers.isEmpty())
		{
			for (L2FlameTowerInstance ct : _flameTowers)
			{
				if (ct != null)
				{
					try
					{
						ct.deleteMe();
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING, "Exception: removeFlamelTower(): " + e.getMessage(), e);
					}
				}
			}
			_flameTowers.clear();
			_flameTowers = null;
		}
	}
	
	private void removeFlags()
	{
		for (L2SiegeClan sc : getAttackerClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
		for (L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}
	
	private void removeDefenderFlags()
	{
		for (L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
			{
				sc.removeFlags();
			}
		}
	}
	
	private void saveCastleSiege()
	{
		setNextSiegeDate();
		getTimeRegistrationOverDate().setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		getTimeRegistrationOverDate().add(Calendar.DAY_OF_MONTH, 1);
		getCastle().setIsTimeRegistrationOver(false);
		
		saveSiegeDate();
		startAutoTask();
	}
	
	public void saveSiegeDate()
	{
		if (_scheduledStartSiegeTask != null)
		{
			_scheduledStartSiegeTask.cancel(true);
			_scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new Siege.ScheduleStartSiegeTask(getCastle()), 1000);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE castle SET siegeDate = ?, regTimeEnd = ?, regTimeOver = ?  WHERE id = ?"))
		{
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setLong(2, getTimeRegistrationOverDate().getTimeInMillis());
			statement.setString(3, String.valueOf(getIsTimeRegistrationOver()));
			statement.setInt(4, getCastle().getId());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveSiegeDate(): " + e.getMessage(), e);
		}
	}
	
	private void saveSiegeClan(L2Clan clan, byte typeId, boolean isUpdateRegistration)
	{
		if (clan.getCastleId() > 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if ((typeId == DEFENDER) || (typeId == DEFENDER_NOT_APPROWED) || (typeId == OWNER))
			{
				if ((getDefenderClans().size() + getDefenderWaitingClans().size()) >= SiegeManager.getInstance().getDefenderMaxClans())
				{
					return;
				}
			}
			else
			{
				if (getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans())
				{
					return;
				}
			}
			
			if (!isUpdateRegistration)
			{
				try (PreparedStatement statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) values (?,?,?,0)"))
				{
					statement.setInt(1, clan.getId());
					statement.setInt(2, getCastle().getId());
					statement.setInt(3, typeId);
					statement.execute();
				}
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement("UPDATE siege_clans SET type = ? WHERE castle_id = ? AND clan_id = ?"))
				{
					statement.setInt(1, typeId);
					statement.setInt(2, getCastle().getId());
					statement.setInt(3, clan.getId());
					statement.execute();
				}
			}
			
			if ((typeId == DEFENDER) || (typeId == OWNER))
			{
				addDefender(clan.getId());
			}
			else if (typeId == ATTACKER)
			{
				addAttacker(clan.getId());
			}
			else if (typeId == DEFENDER_NOT_APPROWED)
			{
				addDefenderWaiting(clan.getId());
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage(), e);
		}
	}
	
	private void setNextSiegeDate()
	{
		while (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			if ((getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) && (getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY))
			{
				getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			}
			
			if ((getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY))
			{
				getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			}
			getCastle().getSiegeDate().add(Calendar.DAY_OF_MONTH, 7);
		}
		
		if (!SevenSigns.getInstance().isDateInSealValidPeriod(getCastle().getSiegeDate()))
		{
			getCastle().getSiegeDate().add(Calendar.DAY_OF_MONTH, 7);
		}
		_isRegistrationOver = false;
	}
	
	private void spawnControlTower(int Id)
	{
		if (_controlTowers == null)
		{
			_controlTowers = new ArrayList<>();
		}
		
		for (SiegeSpawn _sp : SiegeManager.getInstance().getControlTowerSpawnList(Id))
		{
			L2ControlTowerInstance ct;
			
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(_sp.getNpcId());
			
			ct = new L2ControlTowerInstance(IdFactory.getInstance().getNextId(), template);
			
			ct.setCurrentHpMp(_sp.getHp(), ct.getMaxMp());
			ct.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 20);
			_controlTowerCount++;
			_controlTowerMaxCount++;
			_controlTowers.add(ct);
		}
	}
	
	private void spawnFlameTower(int Id)
	{
		if (_flameTowers == null)
		{
			_flameTowers = new ArrayList<>();
		}
		
		for (SiegeSpawn _sp : SiegeManager.getInstance().getFlameTowerSpawnList(Id))
		{
			L2FlameTowerInstance ct;
			
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(_sp.getNpcId());
			
			ct = new L2FlameTowerInstance(IdFactory.getInstance().getNextId(), template);
			
			ct.setCurrentHpMp(_sp.getHp(), ct.getMaxMp());
			ct.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 20);
			_flameTowerCount++;
			_flameTowerMaxCount++;
			_flameTowers.add(ct);
		}
		if (_flameTowerCount == 0)
		{
			_flameTowerCount = 1;
		}
	}
	
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
		
		if (!getSiegeGuardManager().getSiegeGuardSpawn().isEmpty() && !_controlTowers.isEmpty())
		{
			L2ControlTowerInstance closestCt;
			int x, y, z;
			double distance;
			double distanceClosest = 0;
			for (L2Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn())
			{
				if (spawn == null)
				{
					continue;
				}
				
				closestCt = null;
				distanceClosest = Integer.MAX_VALUE;
				
				x = spawn.getX();
				y = spawn.getY();
				z = spawn.getZ();
				
				for (L2ControlTowerInstance ct : _controlTowers)
				{
					if (ct == null)
					{
						continue;
					}
					
					distance = ct.getDistanceSq(x, y, z);
					
					if (distance < distanceClosest)
					{
						closestCt = ct;
						distanceClosest = distance;
					}
				}
				if (closestCt != null)
				{
					closestCt.registerGuard(spawn);
				}
			}
		}
	}
	
	@Override
	public final L2SiegeClan getAttackerClan(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getAttackerClan(clan.getId());
	}
	
	@Override
	public final L2SiegeClan getAttackerClan(int clanId)
	{
		for (L2SiegeClan sc : getAttackerClans())
		{
			if ((sc != null) && (sc.getClanId() == clanId))
			{
				return sc;
			}
		}
		return null;
	}
	
	@Override
	public final List<L2SiegeClan> getAttackerClans()
	{
		if (_isNormalSide)
		{
			return _attackerClans;
		}
		return _defenderClans;
	}
	
	public final int getAttackerRespawnDelay()
	{
		return (SiegeManager.getInstance().getAttackerRespawnDelay());
	}
	
	public final Castle getCastle()
	{
		if ((_castle == null) || (_castle.length <= 0))
		{
			return null;
		}
		return _castle[0];
	}
	
	@Override
	public final L2SiegeClan getDefenderClan(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getDefenderClan(clan.getId());
	}
	
	@Override
	public final L2SiegeClan getDefenderClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderClans())
		{
			if ((sc != null) && (sc.getClanId() == clanId))
			{
				return sc;
			}
		}
		return null;
	}
	
	@Override
	public final List<L2SiegeClan> getDefenderClans()
	{
		if (_isNormalSide)
		{
			return _defenderClans;
		}
		return _attackerClans;
	}
	
	public final L2SiegeClan getDefenderWaitingClan(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		return getDefenderWaitingClan(clan.getId());
	}
	
	public final L2SiegeClan getDefenderWaitingClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderWaitingClans())
		{
			if ((sc != null) && (sc.getClanId() == clanId))
			{
				return sc;
			}
		}
		return null;
	}
	
	public final List<L2SiegeClan> getDefenderWaitingClans()
	{
		return _defenderWaitingClans;
	}
	
	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}
	
	public final boolean getIsRegistrationOver()
	{
		return _isRegistrationOver;
	}
	
	public final boolean getIsTimeRegistrationOver()
	{
		return getCastle().getIsTimeRegistrationOver();
	}
	
	@Override
	public final Calendar getSiegeDate()
	{
		return getCastle().getSiegeDate();
	}
	
	public final Calendar getTimeRegistrationOverDate()
	{
		return getCastle().getTimeRegistrationOverDate();
	}
	
	public void endTimeRegistration(boolean automatic)
	{
		getCastle().setIsTimeRegistrationOver(true);
		if (!automatic)
		{
			saveSiegeDate();
		}
	}
	
	@Override
	public List<L2Npc> getFlag(L2Clan clan)
	{
		if (clan != null)
		{
			L2SiegeClan sc = getAttackerClan(clan);
			if (sc != null)
			{
				return sc.getFlag();
			}
		}
		return null;
	}
	
	public final SiegeGuardManager getSiegeGuardManager()
	{
		if (_siegeGuardManager == null)
		{
			_siegeGuardManager = new SiegeGuardManager(getCastle());
		}
		return _siegeGuardManager;
	}
	
	public int getControlTowerCount()
	{
		return _controlTowerCount;
	}
	
	public int getControlTowerMaxCount()
	{
		return _controlTowerMaxCount;
	}
	
	public int getFlameTowerMaxCount()
	{
		return _flameTowerMaxCount;
	}
	
	public void disableTraps()
	{
		_flameTowerCount--;
	}
	
	public boolean isTrapsActive()
	{
		return _flameTowerCount > 0;
	}
	
	@Override
	public boolean giveFame()
	{
		return true;
	}
	
	@Override
	public int getFameFrequency()
	{
		return Config.CASTLE_ZONE_FAME_TASK_FREQUENCY;
	}
	
	@Override
	public int getFameAmount()
	{
		return Config.CASTLE_ZONE_FAME_AQUIRE_POINTS;
	}
	
	@Override
	public void updateSiege()
	{
	}
	
	private boolean fireSiegeListeners(EventStage stage)
	{
		if (!siegeListeners.isEmpty())
		{
			SiegeEvent event = new SiegeEvent();
			event.setSiege(this);
			event.setStage(stage);
			switch (stage)
			{
				case START:
				{
					for (SiegeListener listener : siegeListeners)
					{
						if (!listener.onStart(event))
						{
							return false;
						}
					}
					break;
				}
				case END:
				{
					for (SiegeListener listener : siegeListeners)
					{
						listener.onEnd(event);
					}
					break;
				}
				case CONTROL_CHANGE:
				{
					for (SiegeListener listener : siegeListeners)
					{
						listener.onControlChange(event);
					}
					break;
				}
			}
		}
		return true;
	}
	
	protected int getArtifactCount(int casleId)
	{
		if ((casleId == 7) || (casleId == 9))
		{
			return 2;
		}
		else
		{
			return 1;
		}
	}
	
	public static void addSiegeListener(SiegeListener listener)
	{
		if (!siegeListeners.contains(listener))
		{
			siegeListeners.add(listener);
		}
	}
	
	public static void removeSiegeListener(SiegeListener listener)
	{
		siegeListeners.remove(listener);
	}
}