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
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeGuardManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.model.CombatFlag;
import l2e.gameserver.model.FortSiegeSpawn;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2SiegeClan;
import l2e.gameserver.model.L2SiegeClan.SiegeClanType;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2FortCommanderInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.scripting.scriptengine.events.FortSiegeEvent;
import l2e.gameserver.scripting.scriptengine.impl.L2Script.EventStage;
import l2e.gameserver.scripting.scriptengine.listeners.events.FortSiegeListener;

public class FortSiege implements Siegable
{
	protected static final Logger _log = Logger.getLogger(FortSiege.class.getName());
	
	private static FastList<FortSiegeListener> fortSiegeListeners = new FastList<FortSiegeListener>().shared();
	
	public static enum TeleportWhoType
	{
		All,
		Attacker,
		Owner,
	}
	
	private static final String DELETE_FORT_SIEGECLANS_BY_CLAN_ID = "DELETE FROM fortsiege_clans WHERE fort_id = ? AND clan_id = ?";
	private static final String DELETE_FORT_SIEGECLANS = "DELETE FROM fortsiege_clans WHERE fort_id = ?";
	
	public class ScheduleEndSiegeTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!getIsInProgress())
			{
				return;
			}
			
			try
			{
				_siegeEnd = null;
				endSiege();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleEndSiegeTask() for Fort: " + _fort.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Fort _fortInst;
		private final int _time;
		
		public ScheduleStartSiegeTask(int time)
		{
			_fortInst = _fort;
			_time = time;
		}
		
		@Override
		public void run()
		{
			if (getIsInProgress())
			{
				return;
			}
			
			try
			{
				final SystemMessage sm;
				if (_time == 3600)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(600), 3000000);
				}
				else if (_time == 600)
				{
					getFort().despawnSuspiciousMerchant();
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(10);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(300), 300000);
				}
				else if (_time == 300)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(5);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(60), 240000);
				}
				else if (_time == 60)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(1);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(30), 30000);
				}
				else if (_time == 30)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(30);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(10), 20000);
				}
				else if (_time == 10)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(10);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(5), 5000);
				}
				else if (_time == 5)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(5);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(1), 4000);
				}
				else if (_time == 1)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS);
					sm.addNumber(1);
					announceToPlayer(sm);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(0), 1000);
				}
				else if (_time == 0)
				{
					_fortInst.getSiege().startSiege();
				}
				else
				{
					_log.warning("Exception: ScheduleStartSiegeTask(): unknown siege time: " + String.valueOf(_time));
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleStartSiegeTask() for Fort: " + _fortInst.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	public class ScheduleSuspiciousMerchantSpawn implements Runnable
	{
		@Override
		public void run()
		{
			if (getIsInProgress())
			{
				return;
			}
			
			try
			{
				_fort.spawnSuspiciousMerchant();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleSuspicoiusMerchantSpawn() for Fort: " + FortSiege.this._fort.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	public class ScheduleSiegeRestore implements Runnable
	{
		@Override
		public void run()
		{
			if (!getIsInProgress())
			{
				return;
			}
			
			try
			{
				_siegeRestore = null;
				resetSiege();
				announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.BARRACKS_FUNCTION_RESTORED));
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception: ScheduleSiegeRestore() for Fort: " + _fort.getName() + " " + e.getMessage(), e);
			}
		}
	}
	
	private final List<L2SiegeClan> _attackerClans = new FastList<>();
	
	protected FastList<L2Spawn> _commanders = new FastList<>();
	protected final Fort _fort;
	private boolean _isInProgress = false;
	private FortSiegeGuardManager _siegeGuardManager;
	ScheduledFuture<?> _siegeEnd = null;
	ScheduledFuture<?> _siegeRestore = null;
	ScheduledFuture<?> _siegeStartTask = null;
	
	public FortSiege(Fort fort)
	{
		_fort = fort;
		
		checkAutoTask();
		FortSiegeManager.getInstance().addSiege(this);
	}
	
	@Override
	public void endSiege()
	{
		if (getIsInProgress())
		{
			_isInProgress = false;
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED);
			sm.addCastleId(getFort().getId());
			announceToPlayer(sm);
			
			removeFlags();
			unSpawnFlags();
			
			updatePlayerSiegeStateFlags(true);
			
			int ownerId = -1;
			if (getFort().getOwnerClan() != null)
			{
				ownerId = getFort().getOwnerClan().getId();
			}
			getFort().getZone().banishForeigners(ownerId);
			getFort().getZone().setIsActive(false);
			getFort().getZone().updateZoneStatusForCharactersInside();
			getFort().getZone().setSiegeInstance(null);
			
			saveFortSiege();
			clearSiegeClan();
			removeCommanders();
			
			getFort().spawnNpcCommanders();
			getSiegeGuardManager().unspawnSiegeGuard();
			getFort().resetDoors();
			
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSuspiciousMerchantSpawn(), FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay() * 60 * 1000L);
			setSiegeDateTime(true);
			
			if (_siegeEnd != null)
			{
				_siegeEnd.cancel(true);
				_siegeEnd = null;
			}
			if (_siegeRestore != null)
			{
				_siegeRestore.cancel(true);
				_siegeRestore = null;
			}
			
			if ((getFort().getOwnerClan() != null) && (getFort().getFlagPole().getMeshIndex() == 0))
			{
				getFort().setVisibleFlag(true);
			}
			
			_log.info("Siege of " + getFort().getName() + " fort finished.");
			fireFortSiegeEventListeners(EventStage.END);
		}
	}
	
	@Override
	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (!fireFortSiegeEventListeners(EventStage.START))
			{
				return;
			}
			if (_siegeStartTask != null)
			{
				_siegeStartTask.cancel(true);
				getFort().despawnSuspiciousMerchant();
			}
			_siegeStartTask = null;
			
			if (getAttackerClans().isEmpty())
			{
				return;
			}
			
			_isInProgress = true;
			
			loadSiegeClan();
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(FortSiege.TeleportWhoType.Attacker, TeleportWhereType.TOWN);
			
			getFort().despawnNpcCommanders();
			spawnCommanders();
			getFort().resetDoors();
			spawnSiegeGuard();
			getFort().setVisibleFlag(false);
			getFort().getZone().setSiegeInstance(this);
			getFort().getZone().setIsActive(true);
			getFort().getZone().updateZoneStatusForCharactersInside();
			
			_siegeEnd = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(), FortSiegeManager.getInstance().getSiegeLength() * 60 * 1000L);
			
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_S1_HAS_BEGUN);
			sm.addCastleId(getFort().getId());
			announceToPlayer(sm);
			saveFortSiege();
			
			_log.info("Siege of " + getFort().getName() + " fort started.");
		}
	}
	
	public void announceToPlayer(SystemMessage sm)
	{
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member != null)
				{
					member.sendPacket(sm);
				}
			}
		}
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanHolder.getInstance().getClan(getFort().getOwnerClan().getId());
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				if (member != null)
				{
					member.sendPacket(sm);
				}
			}
		}
	}
	
	public void announceToPlayer(SystemMessage sm, String s)
	{
		sm.addString(s);
		announceToPlayer(sm);
	}
	
	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
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
					member.setSiegeSide(getFort().getId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.broadcastUserInfo();
			}
		}
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanHolder.getInstance().getClan(getFort().getOwnerClan().getId());
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
					member.setSiegeSide(getFort().getId());
					if (checkIfInZone(member))
					{
						member.setIsInSiege(true);
						member.startFameTask(Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY * 1000, Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.broadcastUserInfo();
			}
		}
	}
	
	public boolean checkIfInZone(L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	public boolean checkIfInZone(int x, int y, int z)
	{
		return (getIsInProgress() && (getFort().checkIfInZone(x, y, z)));
	}
	
	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		return (getAttackerClan(clan) != null);
	}
	
	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		if ((clan != null) && (getFort().getOwnerClan() == clan))
		{
			return true;
		}
		
		return false;
	}
	
	public void clearSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM fortsiege_clans WHERE fort_id=?"))
		{
			ps.setInt(1, getFort().getId());
			ps.execute();
			
			if (getFort().getOwnerClan() != null)
			{
				try (PreparedStatement delete = con.prepareStatement("DELETE FROM fortsiege_clans WHERE clan_id=?"))
				{
					delete.setInt(1, getFort().getOwnerClan().getId());
					delete.execute();
				}
			}
			
			getAttackerClans().clear();
			
			if (getIsInProgress())
			{
				endSiege();
			}
			
			if (_siegeStartTask != null)
			{
				_siegeStartTask.cancel(true);
				_siegeStartTask = null;
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: clearSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	private void clearSiegeDate()
	{
		getFort().getSiegeDate().setTimeInMillis(0);
	}
	
	@Override
	public List<L2PcInstance> getAttackersInZone()
	{
		List<L2PcInstance> players = new FastList<>();
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
	
	public List<L2PcInstance> getPlayersInZone()
	{
		return getFort().getZone().getPlayersInside();
	}
	
	public List<L2PcInstance> getOwnersInZone()
	{
		List<L2PcInstance> players = new FastList<>();
		L2Clan clan;
		if (getFort().getOwnerClan() != null)
		{
			clan = ClanHolder.getInstance().getClan(getFort().getOwnerClan().getId());
			if (clan != getFort().getOwnerClan())
			{
				return null;
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
	
	public void killedCommander(L2FortCommanderInstance instance)
	{
		if ((_commanders != null) && (getFort() != null) && (_commanders.size() != 0))
		{
			L2Spawn spawn = instance.getSpawn();
			if (spawn != null)
			{
				FastList<FortSiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getId());
				for (FortSiegeSpawn spawn2 : commanders)
				{
					if (spawn2.getId() == spawn.getId())
					{
						NpcStringId npcString = null;
						switch (spawn2.getId())
						{
							case 1:
								npcString = NpcStringId.YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT;
								break;
							case 2:
								npcString = NpcStringId.AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY;
								break;
							case 3:
								npcString = NpcStringId.AT_LAST_THE_MAGIC_FIELD_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK;
								break;
							case 4:
								npcString = NpcStringId.I_FEEL_SO_MUCH_GRIEF_THAT_I_CANT_EVEN_TAKE_CARE_OF_MYSELF_THERE_ISNT_ANY_REASON_FOR_ME_TO_STAY_HERE_ANY_LONGER;
								break;
						}
						if (npcString != null)
						{
							instance.broadcastPacket(new NpcSay(instance.getObjectId(), Say2.NPC_SHOUT, instance.getId(), npcString));
						}
					}
				}
				_commanders.remove(spawn);
				if (_commanders.isEmpty())
				{
					spawnFlag(getFort().getId());
					
					if (_siegeRestore != null)
					{
						_siegeRestore.cancel(true);
					}
					
					for (L2DoorInstance door : getFort().getDoors())
					{
						if (door.getIsShowHp())
						{
							continue;
						}
						
						door.openMe();
					}
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.ALL_BARRACKS_OCCUPIED));
				}
				else if (_siegeRestore == null)
				{
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
					_siegeRestore = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSiegeRestore(), FortSiegeManager.getInstance().getCountDownLength() * 60 * 1000L);
				}
				else
				{
					getFort().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.SEIZED_BARRACKS));
				}
			}
			else
			{
				_log.warning("FortSiege.killedCommander(): killed commander, but commander not registered for fortress. NpcId: " + instance.getId() + " FortId: " + getFort().getId());
			}
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
	
	public boolean registerAttacker(L2PcInstance player, boolean force)
	{
		if (player.getClan() == null)
		{
			return false;
		}
		
		if (force || checkIfCanRegister(player))
		{
			saveSiegeClan(player.getClan());
			
			if (getAttackerClans().size() == 1)
			{
				if (!force)
				{
					player.reduceAdena("siege", 250000, null, true);
				}
				startAutoTask(true);
			}
			return true;
		}
		return false;
	}
	
	private void removeSiegeClan(int clanId)
	{
		final String query = (clanId != 0) ? DELETE_FORT_SIEGECLANS_BY_CLAN_ID : DELETE_FORT_SIEGECLANS;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setInt(1, getFort().getId());
			if (clanId != 0)
			{
				statement.setInt(2, clanId);
			}
			statement.execute();
			
			loadSiegeClan();
			if (getAttackerClans().isEmpty())
			{
				if (getIsInProgress())
				{
					endSiege();
				}
				else
				{
					saveFortSiege();
				}
				
				if (_siegeStartTask != null)
				{
					_siegeStartTask.cancel(true);
					_siegeStartTask = null;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on removeSiegeClan: " + e.getMessage(), e);
		}
	}
	
	public void removeSiegeClan(L2Clan clan)
	{
		if ((clan == null) || (clan.getFortId() == getFort().getId()) || !FortSiegeManager.getInstance().checkIsRegistered(clan, getFort().getId()))
		{
			return;
		}
		
		removeSiegeClan(clan.getId());
	}
	
	public void checkAutoTask()
	{
		if (_siegeStartTask != null)
		{
			return;
		}
		
		final long delay = getFort().getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		
		if (delay < 0)
		{
			saveFortSiege();
			clearSiegeClan();
			ThreadPoolManager.getInstance().executeTask(new ScheduleSuspiciousMerchantSpawn());
		}
		else
		{
			loadSiegeClan();
			if (getAttackerClans().isEmpty())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSuspiciousMerchantSpawn(), delay);
			}
			else
			{
				if (delay > 3600000)
				{
					ThreadPoolManager.getInstance().executeTask(new ScheduleSuspiciousMerchantSpawn());
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(3600), delay - 3600000);
				}
				if (delay > 600000)
				{
					ThreadPoolManager.getInstance().executeTask(new ScheduleSuspiciousMerchantSpawn());
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(600), delay - 600000);
				}
				else if (delay > 300000)
				{
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(300), delay - 300000);
				}
				else if (delay > 60000)
				{
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(60), delay - 60000);
				}
				else
				{
					_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(60), 0);
				}
				
				_log.info("Siege of " + getFort().getName() + " fort: " + getFort().getSiegeDate().getTime());
			}
		}
	}
	
	public void startAutoTask(boolean setTime)
	{
		if (_siegeStartTask != null)
		{
			return;
		}
		
		if (setTime)
		{
			setSiegeDateTime(false);
		}
		
		if (getFort().getOwnerClan() != null)
		{
			getFort().getOwnerClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.A_FORTRESS_IS_UNDER_ATTACK));
		}
		
		_siegeStartTask = ThreadPoolManager.getInstance().scheduleGeneral(new FortSiege.ScheduleStartSiegeTask(3600), 0);
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
			default:
				players = getPlayersInZone();
		}
		
		for (L2PcInstance player : players)
		{
			if (player.canOverrideCond(PcCondOverride.FORTRESS_CONDITIONS) || player.isJailed())
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
	
	public boolean checkIfCanRegister(L2PcInstance player)
	{
		boolean b = true;
		if ((player.getClan() == null) || (player.getClan().getLevel() < FortSiegeManager.getInstance().getSiegeClanMinLevel()))
		{
			b = false;
			player.sendMessage("Only clans with Level " + FortSiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a fortress siege.");
		}
		else if ((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) != L2Clan.CP_CS_MANAGE_SIEGE)
		{
			b = false;
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
		}
		else if (player.getClan() == getFort().getOwnerClan())
		{
			b = false;
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		}
		else if ((getFort().getOwnerClan() != null) && (player.getClan().getCastleId() > 0) && (player.getClan().getCastleId() == getFort().getContractedCastleId()))
		{
			b = false;
			player.sendPacket(SystemMessageId.CANT_REGISTER_TO_SIEGE_DUE_TO_CONTRACT);
		}
		else if ((getFort().getTimeTillRebelArmy() > 0) && (getFort().getTimeTillRebelArmy() <= 7200))
		{
			b = false;
			player.sendMessage("You cannot register for the fortress siege 2 hours prior to rebel army attack.");
		}
		else if (getFort().getSiege().getAttackerClans().isEmpty() && (player.getInventory().getAdena() < 250000))
		{
			b = false;
			player.sendMessage("You need 250,000 adena to register");
		}
		else
		{
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort.getSiege().getAttackerClan(player.getClanId()) != null)
				{
					b = false;
					player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
					break;
				}
				if ((fort.getOwnerClan() == player.getClan()) && (fort.getSiege().getIsInProgress() || (fort.getSiege()._siegeStartTask != null)))
				{
					b = false;
					player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
					break;
				}
			}
		}
		return b;
	}
	
	public boolean checkIfAlreadyRegisteredForSameDay(L2Clan clan)
	{
		for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
		{
			if (siege == this)
			{
				continue;
			}
			
			if (siege.getSiegeDate().get(Calendar.DAY_OF_WEEK) == getSiegeDate().get(Calendar.DAY_OF_WEEK))
			{
				if (siege.checkIsAttacker(clan))
				{
					return true;
				}
				if (siege.checkIsDefender(clan))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void setSiegeDateTime(boolean merchant)
	{
		Calendar newDate = Calendar.getInstance();
		if (merchant)
		{
			newDate.add(Calendar.MINUTE, FortSiegeManager.getInstance().getSuspiciousMerchantRespawnDelay());
		}
		else
		{
			newDate.add(Calendar.MINUTE, 60);
		}
		getFort().setSiegeDate(newDate);
		saveSiegeDate();
	}
	
	private void loadSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			getAttackerClans().clear();
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans WHERE fort_id=?");
			statement.setInt(1, getFort().getId());
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				addAttacker(rs.getInt("clan_id"));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: loadSiegeClan(): " + e.getMessage(), e);
		}
	}
	
	private void removeCommanders()
	{
		if ((_commanders != null) && !_commanders.isEmpty())
		{
			for (L2Spawn spawn : _commanders)
			{
				if (spawn != null)
				{
					spawn.stopRespawn();
					if (spawn.getLastSpawn() != null)
					{
						spawn.getLastSpawn().deleteMe();
					}
				}
			}
			_commanders.clear();
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
	}
	
	private void saveFortSiege()
	{
		clearSiegeDate();
		saveSiegeDate();
	}
	
	private void saveSiegeDate()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE fort SET siegeDate = ? WHERE id = ?"))
		{
			ps.setLong(1, getSiegeDate().getTimeInMillis());
			ps.setInt(2, getFort().getId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveSiegeDate(): " + e.getMessage(), e);
		}
	}
	
	private void saveSiegeClan(L2Clan clan)
	{
		if (getAttackerClans().size() >= FortSiegeManager.getInstance().getAttackerMaxClans())
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO fortsiege_clans (clan_id,fort_id) values (?,?)"))
		{
			statement.setInt(1, clan.getId());
			statement.setInt(2, getFort().getId());
			statement.execute();
			
			addAttacker(clan.getId());
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveSiegeClan(L2Clan clan): " + e.getMessage(), e);
		}
	}
	
	private void spawnCommanders()
	{
		try
		{
			_commanders.clear();
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			for (FortSiegeSpawn _sp : FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getId()))
			{
				template1 = NpcTable.getInstance().getTemplate(_sp.getId());
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(1);
					spawnDat.setX(_sp.getLocation().getX());
					spawnDat.setY(_sp.getLocation().getY());
					spawnDat.setZ(_sp.getLocation().getZ());
					spawnDat.setHeading(_sp.getLocation().getHeading());
					spawnDat.setRespawnDelay(60);
					spawnDat.doSpawn();
					spawnDat.stopRespawn();
					_commanders.add(spawnDat);
				}
				else
				{
					_log.warning("FortSiege.spawnCommander: Data missing in NPC table for ID: " + _sp.getId() + ".");
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "FortSiege.spawnCommander: Spawn could not be initialized: " + e.getMessage(), e);
		}
	}
	
	private void spawnFlag(int Id)
	{
		for (CombatFlag cf : FortSiegeManager.getInstance().getFlagList(Id))
		{
			cf.spawnMe();
		}
	}
	
	private void unSpawnFlags()
	{
		if (FortSiegeManager.getInstance().getFlagList(getFort().getId()) == null)
		{
			return;
		}
		
		for (CombatFlag cf : FortSiegeManager.getInstance().getFlagList(getFort().getId()))
		{
			cf.unSpawnMe();
		}
	}
	
	private void spawnSiegeGuard()
	{
		getSiegeGuardManager().spawnSiegeGuard();
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
		return _attackerClans;
	}
	
	public final Fort getFort()
	{
		return _fort;
	}
	
	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}
	
	@Override
	public final Calendar getSiegeDate()
	{
		return getFort().getSiegeDate();
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
	
	public final FortSiegeGuardManager getSiegeGuardManager()
	{
		if (_siegeGuardManager == null)
		{
			_siegeGuardManager = new FortSiegeGuardManager(getFort());
		}
		
		return _siegeGuardManager;
	}
	
	public void resetSiege()
	{
		removeCommanders();
		spawnCommanders();
		getFort().resetDoors();
	}
	
	public List<L2Spawn> getCommanders()
	{
		return _commanders;
	}
	
	@Override
	public L2SiegeClan getDefenderClan(int clanId)
	{
		return null;
	}
	
	@Override
	public L2SiegeClan getDefenderClan(L2Clan clan)
	{
		return null;
	}
	
	@Override
	public List<L2SiegeClan> getDefenderClans()
	{
		return null;
	}
	
	@Override
	public boolean giveFame()
	{
		return true;
	}
	
	@Override
	public int getFameFrequency()
	{
		return Config.FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	}
	
	@Override
	public int getFameAmount()
	{
		return Config.FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	}
	
	@Override
	public void updateSiege()
	{
	}
	
	private boolean fireFortSiegeEventListeners(EventStage stage)
	{
		if (!fortSiegeListeners.isEmpty())
		{
			FortSiegeEvent event = new FortSiegeEvent();
			event.setSiege(this);
			event.setStage(stage);
			switch (stage)
			{
				case START:
				{
					for (FortSiegeListener listener : fortSiegeListeners)
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
					for (FortSiegeListener listener : fortSiegeListeners)
					{
						listener.onEnd(event);
					}
					break;
				}
			}
		}
		return true;
	}
	
	public static void addFortSiegeListener(FortSiegeListener listener)
	{
		if (!fortSiegeListeners.contains(listener))
		{
			fortSiegeListeners.add(listener);
		}
	}
	
	public static void removeFortSiegeListener(FortSiegeListener listener)
	{
		fortSiegeListeners.remove(listener);
	}
}