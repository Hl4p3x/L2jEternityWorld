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
package l2e.gameserver.model.entity.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.L2Party.messageType;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2CustomCTFFlagInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExCubeGameAddPlayer;
import l2e.gameserver.network.serverpackets.ExCubeGameChangePoints;
import l2e.gameserver.network.serverpackets.ExCubeGameEnd;
import l2e.gameserver.network.serverpackets.ExCubeGameExtendedChangePoints;
import l2e.gameserver.network.serverpackets.ExCubeGameTeamList;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.SocialAction;
import gnu.trove.map.hash.TIntObjectHashMap;

public class CTF extends FunEvent
{
	protected static final Logger _log = Logger.getLogger(CTF.class.getName());
	
	private ArrayList<L2PcInstance> _bluePlayers = null;
	private ArrayList<L2PcInstance> _redPlayers = null;
	
	private final int _CTFFlagId = 97005;
	private final TIntObjectHashMap<L2CustomCTFFlagInstance> _flagSpawns = new TIntObjectHashMap<>();
	private final int _CTFThroneId = 97006;
	private final TIntObjectHashMap<L2CustomCTFFlagInstance> _throneSpawns = new TIntObjectHashMap<>();
	private final int _CTFFlagInHandId = 6718;
	
	@Override
	public void loadConfig()
	{
		EVENT_ID = 2;
		EVENT_NAME = "CTF";
		EVENT_AUTO_MODE = Config.CTF_AUTO_MODE;
		EVENT_INTERVAL = Config.CTF_EVENT_INTERVAL;
		EVENT_NPC_LOC = (new int[]
		{
			Config.CTF_NPC_X,
			Config.CTF_NPC_Y,
			Config.CTF_NPC_Z
		});
		EVENT_NPC_LOC_NAME = Config.CTF_NPC_LOC_NAME;
		EVENT_TEAMS_TYPE = Config.CTF_EVEN_TEAMS;
		EVENT_PLAYER_LEVEL_MIN = Config.CTF_PLAYER_LEVEL_MIN;
		EVENT_PLAYER_LEVEL_MAX = Config.CTF_PLAYER_LEVEL_MAX;
		EVENT_COUNTDOWN_TIME = Config.CTF_COUNTDOWN_TIME;
		EVENT_MIN_PLAYERS = Config.CTF_MIN_PLAYERS;
		EVENT_DOORS_TO_CLOSE = Config.CTF_DOORS_TO_CLOSE;
		EVENT_DOORS_TO_OPEN = Config.CTF_DOORS_TO_OPEN;
	}
	
	@Override
	protected CTFTeam getTeam(int team)
	{
		return (CTFTeam) _teams.get(team);
	}
	
	@Override
	public void abortEvent()
	{
		if (_state == State.INACTIVE)
		{
			return;
		}
		
		if (_state == State.PARTICIPATING)
		{
			unspawnManager();
		}
		else if (_state == State.STARTING)
		{
			teleportPlayersBack();
		}
		else if (_state == State.FIGHTING)
		{
			unspawnFlags();
			endFight();
			removeDoors();
			teleportPlayersBack();
		}
		_state = State.INACTIVE;
		clearData();
		autoStart();
	}
	
	private void loadData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM z_ctf_teams");
			ResultSet rset = statement.executeQuery();
			CTFTeam team = null;
			int index = 0;
			
			while (rset.next())
			{
				index++;
				
				if (index > Config.CTF_TEAMS_NUM)
				{
					break;
				}
				
				team = new CTFTeam();
				
				team._teamId = index;
				team._teamName = rset.getString("teamName");
				team._teamX = rset.getInt("teamX");
				team._teamY = rset.getInt("teamY");
				team._teamZ = rset.getInt("teamZ");
				team._flagX = rset.getInt("flagX");
				team._flagY = rset.getInt("flagY");
				team._flagZ = rset.getInt("flagZ");
				team._teamColor = rset.getString("teamColor");
				
				_teams.put(index, team);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "CTFEventEngine[CTF.loadInfo()]: Error while loading CTF Teams data: " + e);
		}
	}
	
	private void clearData()
	{
		if (_sheduleNext != null)
		{
			_sheduleNext.cancel(true);
			_sheduleNext = null;
		}
		_Manager = null;
		_flagSpawns.clear();
		_throneSpawns.clear();
		_players.clear();
		_teams.clear();
	}
	
	private void startFight()
	{
		if (Config.EVENT_INSTANCE)
		{
			_instanceId = InstanceManager.getInstance().createInstance();
		}
		
		if (Config.EVENT_SHOW_SCOREBOARD && (_teams.size() == 2))
		{
			_bluePlayers = new ArrayList<>();
			_redPlayers = new ArrayList<>();
		}
		for (L2PcInstance player : getAllPlayers())
		{
			CTFTeam team = getTeam(player._eventTeamId);
			
			if (Config.CTF_ON_START_UNSUMMON_PET)
			{
				if (player.hasSummon() && (player.getSummon() instanceof L2PetInstance))
				{
					player.getSummon().unSummon(player);
				}
			}
			
			if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
			{
				player.stopAllEffects();
				if (player.hasSummon())
				{
					player.getSummon().stopAllEffects();
				}
			}
			
			if (player.getParty() != null)
			{
				player.getParty().removePartyMember(player, messageType.Expelled);
			}
			
			if (Config.CTF_AURA && (_teams.size() == 2))
			{
				player.setTeam(player._eventTeamId);
			}
			
			if (Config.EVENT_SHOW_SCOREBOARD && (_teams.size() == 2))
			{
				player.sendPacket(new ExCubeGameChangePoints(Config.CTF_FIGHT_TIME * 60, 0, 0));
				if (player._eventTeamId == 1)
				{
					_bluePlayers.add(player);
				}
				else if (player._eventTeamId == 2)
				{
					_redPlayers.add(player);
				}
			}
			
			if (player.isMounted())
			{
				player.dismount();
			}
			
			player.getAppearance().setVisibleTitle(LocalizationStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "CTF.STRING_FLAGS") + ": " + player._CTFCountFlags);
			player.broadcastTitleInfo();
			player.broadcastUserInfo();
			player.setInstanceId(_instanceId);
			player.teleToLocation(team._teamX, team._teamY, team._teamZ);
			player._eventTeleported = true;
			player.updateLastActivityAction();
		}
		if (Config.EVENT_SHOW_SCOREBOARD && (_teams.size() == 2))
		{
			for (L2PcInstance player : getAllPlayers())
			{
				for (L2PcInstance plr : getAllPlayers())
				{
					player.sendPacket(new ExCubeGameAddPlayer(plr, plr._eventTeamId == 2));
				}
			}
			
			for (L2PcInstance player : getAllPlayers())
			{
				player.sendPacket(new ExCubeGameTeamList(_redPlayers, _bluePlayers, 1));
			}
		}
	}
	
	private void spawnFlags()
	{
		try
		{
			for (Team teams : getAllTeams())
			{
				CTFTeam team = (CTFTeam) teams;
				L2CustomCTFFlagInstance CTFFlag = new L2CustomCTFFlagInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(_CTFFlagId));
				CTFFlag._teamId = team._teamId;
				CTFFlag._event = this;
				CTFFlag._mode = "FLAG";
				CTFFlag.setName(team._teamName + "'s Flag");
				CTFFlag.setInstanceId(_instanceId);
				CTFFlag.spawnMe(team._flagX, team._flagY, team._flagZ);
				_flagSpawns.put(team._teamId, CTFFlag);
				
				L2CustomCTFFlagInstance CTFThrone = new L2CustomCTFFlagInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(_CTFThroneId));
				CTFThrone._event = this;
				CTFThrone._mode = "THRONE";
				CTFThrone.setName(" ");
				CTFThrone.setInstanceId(_instanceId);
				CTFThrone.spawnMe(team._flagX, team._flagY, team._flagZ + 30);
				_throneSpawns.put(team._teamId, CTFThrone);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void unspawnFlags()
	{
		try
		{
			if ((_flagSpawns != null) && !_flagSpawns.isEmpty())
			{
				for (L2CustomCTFFlagInstance CTFFlag : _flagSpawns.values(new L2CustomCTFFlagInstance[_flagSpawns.size()]))
				{
					CTFFlag.deleteMe();
				}
			}
			
			if ((_throneSpawns != null) && !_throneSpawns.isEmpty())
			{
				for (L2CustomCTFFlagInstance CTFThrone : _throneSpawns.values(new L2CustomCTFFlagInstance[_throneSpawns.size()]))
				{
					CTFThrone.deleteMe();
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	protected void checkFlagsLoop()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (_state == State.FIGHTING)
				{
					checkFlags();
					checkFlagsLoop();
				}
			}
		}, 60000);
	}
	
	protected void checkFlags()
	{
		for (L2PcInstance player : getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			if (!player.isOnline() && (player._CTFHaveFlagOfTeam != 0))
			{
				CustomMessage msg = new CustomMessage("CTF.LOGOUT_WITH_FLAG", true);
				msg.add(player.getName());
				AnnounceToPlayers(false, msg);
				spawnFlag(player._CTFHaveFlagOfTeam);
				takeFlag(player);
			}
		}
	}
	
	private void spawnFlag(int teamId)
	{
		if (_flagSpawns.containsKey(teamId))
		{
			return;
		}
		
		CTFTeam team = getTeam(teamId);
		
		L2CustomCTFFlagInstance CTFFlag = new L2CustomCTFFlagInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(_CTFFlagId));
		CTFFlag._teamId = team._teamId;
		CTFFlag._event = this;
		CTFFlag._mode = "FLAG";
		CTFFlag.setName(team._teamName + "'s Flag");
		CTFFlag.setInstanceId(_instanceId);
		CTFFlag.spawnMe(team._flagX, team._flagY, team._flagZ);
		_flagSpawns.put(team._teamId, CTFFlag);
	}
	
	private void unspawnFlag(int teamId)
	{
		_flagSpawns.get(teamId).deleteMe();
		_flagSpawns.remove(teamId);
	}
	
	private void endFight()
	{
		int topteamId = 0;
		int topteamflags = 0;
		int topteams = 0;
		
		for (Team teams : getAllTeams())
		{
			CTFTeam team = (CTFTeam) teams;
			if (team._teamFlags > topteamflags)
			{
				topteamflags = team._teamFlags;
			}
		}
		for (Team teams : getAllTeams())
		{
			CTFTeam team = (CTFTeam) teams;
			if (team._teamFlags == topteamflags)
			{
				topteamId = team._teamId;
				topteams++;
			}
		}
		
		int topplayerId = 0;
		int topplayerPoints = 0;
		int topplayers = 0;
		
		for (L2PcInstance player : getAllPlayers())
		{
			if (player._eventTeamId != topteamId)
			{
				continue;
			}
			if (player._CTFCountFlags > topplayerPoints)
			{
				topplayerPoints = player._CTFCountFlags;
			}
		}
		for (L2PcInstance player : getAllPlayers())
		{
			if (player._eventTeamId != topteamId)
			{
				continue;
			}
			if (player._CTFCountFlags == topplayerPoints)
			{
				topplayerId = player.getObjectId();
				topplayers++;
			}
		}
		
		if (topteamflags == 0)
		{
			AnnounceToPlayers(true, new CustomMessage("CTF.NOBODY_WINS", true));
			if (Config.EVENT_SHOW_SCOREBOARD && (_teams.size() == 2))
			{
				for (L2PcInstance player : getAllPlayers())
				{
					player.sendPacket(new ExCubeGameEnd(topteamId == 2));
				}
			}
		}
		else if (topteams > 1)
		{
			AnnounceToPlayers(true, new CustomMessage("CTF.DRAW", true));
			if (Config.EVENT_SHOW_SCOREBOARD && (_teams.size() == 2))
			{
				for (L2PcInstance player : getAllPlayers())
				{
					player.sendPacket(new ExCubeGameEnd(topteamId == 2));
				}
			}
		}
		else
		{
			CustomMessage msg = new CustomMessage("CTF.TEAM_WIN", true);
			msg.add(_teams.get(topteamId)._teamName);
			msg.add(topteamflags);
			AnnounceToPlayers(true, msg);
			for (L2PcInstance player : getAllPlayers())
			{
				if (player._eventTeamId == topteamId)
				{
					player.sendMessage(LocalizationStorage.getInstance().getString(player.getLang(), "CTF.YOU_WIN"));
					if ((player.getObjectId() == topplayerId) && (topplayers == 1))
					{
						for (String reward : Config.CTF_REWARD_TOP)
						{
							String[] rew = reward.split(":");
							player.addItem("CTF Event", Integer.parseInt(rew[0]), Integer.parseInt(rew[1]), null, true);
						}
						CustomMessage topmsg = new CustomMessage("CTF.TEAM_WIN_TOP_PLAYER", true);
						topmsg.add(player.getName());
						topmsg.add(player._CTFCountFlags);
						AnnounceToPlayers(true, topmsg);
					}
					else
					{
						if (Config.CTF_PRICE_NO_KILLS || (player._eventCountKills > 0))
						{
							for (String reward : Config.CTF_REWARD)
							{
								String[] rew = reward.split(":");
								player.addItem("CTF Event", Integer.parseInt(rew[0]), Integer.parseInt(rew[1]), null, true);
							}
						}
					}
				}
				if (Config.EVENT_SHOW_SCOREBOARD && (_teams.size() == 2))
				{
					player.sendPacket(new ExCubeGameEnd(topteamId == 2));
				}
			}
		}
	}
	
	private void teleportPlayersBack()
	{
		for (L2PcInstance player : getAllPlayers())
		{
			if (player.isOnline())
			{
				if (player._CTFHaveFlagOfTeam != 0)
				{
					takeFlag(player);
				}
				player.getAppearance().setNameColor(player._eventOriginalNameColor);
				player.getAppearance().setVisibleTitle(player._eventOriginalTitle);
				player.setKarma(player._eventOriginalKarma);
				player.setTeam(0);
				player._eventName = "";
				player._eventTeamId = 0;
				player._CTFHaveFlagOfTeam = 0;
				player._CTFCountFlags = 0;
				player.setInstanceId(0);
				player.teleToLocation(Config.CTF_NPC_X, Config.CTF_NPC_Y, Config.CTF_NPC_Z, false);
				player.broadcastTitleInfo();
				player.broadcastUserInfo();
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, karma=? WHERE char_name=?");
					statement.setInt(1, Config.CTF_NPC_X);
					statement.setInt(2, Config.CTF_NPC_Y);
					statement.setInt(3, Config.CTF_NPC_Z);
					statement.setInt(4, player._eventOriginalKarma);
					statement.setString(5, player.getName());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "CTFEventEngine[CTF.endFight()]: Error while updating player's " + player.getName() + " data: " + e);
				}
			}
		}
		InstanceManager.getInstance().destroyInstance(_instanceId);
	}
	
	@Override
	protected void StartNext()
	{
		long delay = 0;
		StatsSet set;
		
		if (_state == State.WAITING)
		{
			delay = Config.CTF_COUNTDOWN_TIME * 60000;
			_state = State.PARTICIPATING;
			loadData();
			spawnManager();
			countdown();
			sendConfirmDialog();
		}
		else if (_state == State.PARTICIPATING)
		{
			delay = 20000;
			unspawnManager();
			if (checkPlayersCount())
			{
				teleportPlayers();
			}
			else
			{
				abortEvent();
				return;
			}
			_state = State.STARTING;
		}
		else if (_state == State.STARTING)
		{
			set = new StatsSet();
			
			delay = Config.CTF_FIGHT_TIME * 60000;
			_state = State.FIGHTING;
			startFight();
			makeDoors(set);
			spawnFlags();
			checkFlagsLoop();
			startActivityCheck();
		}
		else if (_state == State.FIGHTING)
		{
			stopActivityCheck();
			unspawnFlags();
			endFight();
			removeDoors();
			teleportPlayersBack();
			clearData();
			_state = State.INACTIVE;
			autoStart();
			return;
		}
		sheduleNext(delay);
	}
	
	@Override
	public void onPlayerLogin(final L2PcInstance player)
	{
		if (_players.containsKey(player.getObjectId()))
		{
			L2PcInstance member = _players.get(player.getObjectId());
			player._eventName = member._eventName;
			player._eventTeamId = member._eventTeamId;
			if (_state == State.STARTING)
			{
				player._eventOriginalTitle = member._eventOriginalTitle;
				player._eventOriginalNameColor = member._eventOriginalNameColor;
				player._eventOriginalKarma = member._eventOriginalKarma;
				player.getAppearance().setNameColor(member.getAppearance().getNameColor());
				player.setKarma(0);
				player.broadcastUserInfo();
			}
			else if (_state == State.FIGHTING)
			{
				player._eventOriginalTitle = member._eventOriginalTitle;
				player._eventOriginalNameColor = member._eventOriginalNameColor;
				player._eventOriginalKarma = member._eventOriginalKarma;
				player._CTFCountFlags = member._CTFCountFlags;
				player.setKarma(0);
				if (Config.CTF_AURA && (_teams.size() == 2))
				{
					player.setTeam(player._eventTeamId);
				}
				if (Config.EVENT_SHOW_SCOREBOARD && (_teams.size() == 2))
				{
					player.sendPacket(new ExCubeGameChangePoints(Config.CTF_FIGHT_TIME * 60, 0, 0));
					for (L2PcInstance plr : getAllPlayers())
					{
						player.sendPacket(new ExCubeGameAddPlayer(plr, plr._eventTeamId == 2));
					}
					player.sendPacket(new ExCubeGameTeamList(_redPlayers, _bluePlayers, 1));
					for (L2PcInstance plr : getAllPlayers())
					{
						player.sendPacket(new ExCubeGameExtendedChangePoints(getStartNextTime(), getTeam(1)._teamFlags, getTeam(2)._teamFlags, plr._eventTeamId == 2, plr, plr._CTFCountFlags));
					}
				}
				player.getAppearance().setVisibleTitle(LocalizationStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "CTF.STRING_FLAGS") + ": " + player._CTFCountFlags);
				player.getAppearance().setNameColor(member.getAppearance().getNameColor());
				player.broadcastTitleInfo();
				player.broadcastUserInfo();
				CTFTeam team = getTeam(player._eventTeamId);
				player.setInstanceId(_instanceId);
				if (!member._eventTeleported)
				{
					if (Config.CTF_ON_START_UNSUMMON_PET)
					{
						if (player.hasSummon() && (player.getSummon() instanceof L2PetInstance))
						{
							player.getSummon().unSummon(player);
						}
					}
					if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
					{
						player.stopAllEffects();
						if (player.hasSummon())
						{
							player.getSummon().stopAllEffects();
						}
					}
					if (player.isMounted())
					{
						player.dismount();
					}
					player.teleToLocation(team._teamX, team._teamY, team._teamZ);
					player._eventTeleported = true;
				}
			}
			_players.put(player.getObjectId(), player);
		}
	}
	
	@Override
	public void onPlayerLogout(final L2PcInstance player)
	{
		if (player._CTFHaveFlagOfTeam != 0)
		{
			spawnFlag(player._CTFHaveFlagOfTeam);
			CustomMessage msg = new CustomMessage("CTF.FLAG_RETURNED", true);
			msg.add(getTeam(player._CTFHaveFlagOfTeam)._teamName);
			AnnounceToPlayers(false, msg);
			
			for (L2PcInstance plr : getAllPlayers())
			{
				if (plr._eventTeamId == player._CTFHaveFlagOfTeam)
				{
					CreatureSay cs = new CreatureSay(plr.getObjectId(), Say2.PARTYROOM_COMMANDER, ":", LocalizationStorage.getInstance().getString(plr.getLang(), "CTF.YOUR_FLAG_RETURNED"));
					plr.sendPacket(cs);
				}
			}
			
			takeFlag(player);
		}
	}
	
	public void onPlayerTakeFlag(L2PcInstance player, int teamId)
	{
		unspawnFlag(teamId);
		giveFlag(player, teamId);
		if (player.isInvisible())
		{
			L2Effect eInvisible = player.getFirstEffect(L2EffectType.INVINCIBLE);
			if (eInvisible != null)
			{
				player.sendMessage(LocalizationStorage.getInstance().getString(player.getLang(), "CTF.CANT_HIDE"));
				eInvisible.exit();
			}
		}
		
		CustomMessage msg = new CustomMessage("CTF.FLAG_CAPTURED", true);
		msg.add(getTeam(teamId)._teamName);
		AnnounceToPlayers(false, msg);
		
		for (L2PcInstance plr : getAllPlayers())
		{
			if (plr._eventTeamId == teamId)
			{
				CreatureSay cs = new CreatureSay(plr.getObjectId(), Say2.PARTYROOM_COMMANDER, ":", LocalizationStorage.getInstance().getString(plr.getLang(), "CTF.YOUR_FLAG_CAPTURED"));
				plr.sendPacket(cs);
			}
		}
	}
	
	public void onPlayerBringFlag(L2PcInstance player)
	{
		CustomMessage msg = new CustomMessage("CTF.TEAM_RECIEVE_POINT", true);
		msg.add(getTeam(player._eventTeamId)._teamName);
		msg.add(getTeam(player._CTFHaveFlagOfTeam)._teamName);
		AnnounceToPlayers(false, msg);
		
		getTeam(player._eventTeamId)._teamFlags++;
		player._CTFCountFlags++;
		
		if (Config.EVENT_SHOW_SCOREBOARD && (_teams.size() == 2))
		{
			for (L2PcInstance plr : getAllPlayers())
			{
				plr.sendPacket(new ExCubeGameExtendedChangePoints(getStartNextTime(), getTeam(1)._teamFlags, getTeam(2)._teamFlags, player._eventTeamId == 2, player, player._CTFCountFlags));
			}
		}
		
		player.getAppearance().setVisibleTitle(LocalizationStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "CTF.STRING_FLAGS") + ": " + player._CTFCountFlags);
		player.broadcastTitleInfo();
		player.broadcastUserInfo();
		
		spawnFlag(player._CTFHaveFlagOfTeam);
		
		for (L2PcInstance plr : getAllPlayers())
		{
			if (plr._eventTeamId == player._CTFHaveFlagOfTeam)
			{
				CreatureSay cs = new CreatureSay(plr.getObjectId(), Say2.PARTYROOM_COMMANDER, ":", LocalizationStorage.getInstance().getString(plr.getLang(), "CTF.YOUR_FLAG_RETURNED"));
				plr.sendPacket(cs);
			}
		}
		
		takeFlag(player);
	}
	
	@Override
	public boolean onPlayerDie(final L2PcInstance player, L2PcInstance killer)
	{
		if (player._CTFHaveFlagOfTeam != 0)
		{
			spawnFlag(player._CTFHaveFlagOfTeam);
			CustomMessage msg = new CustomMessage("CTF.FLAG_RETURNED", true);
			msg.add(getTeam(player._CTFHaveFlagOfTeam)._teamName);
			AnnounceToPlayers(false, msg);
			
			for (L2PcInstance plr : getAllPlayers())
			{
				if (plr._eventTeamId == player._CTFHaveFlagOfTeam)
				{
					CreatureSay cs = new CreatureSay(plr.getObjectId(), Say2.PARTYROOM_COMMANDER, ":", LocalizationStorage.getInstance().getString(plr.getLang(), "CTF.YOUR_FLAG_RETURNED"));
					plr.sendPacket(cs);
				}
			}
			
			takeFlag(player);
		}
		
		player.getStatus().setCurrentHp(player.getMaxHp());
		player.getStatus().setCurrentMp(player.getMaxMp());
		player.getStatus().setCurrentCp(player.getMaxCp());
		player.broadcastStatusUpdate();
		player.teleToLocation(Config.CTF_DEAD_X, Config.CTF_DEAD_Y, Config.CTF_DEAD_Z, false);
		CustomMessage msg = new CustomMessage("CTF.REVIVE", player.getLang());
		msg.add(Config.CTF_RES_TIME);
		player.sendMessage(msg.toString());
		
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				CTFTeam team = getTeam(player._eventTeamId);
				player.teleToLocation(team._teamX, team._teamY, team._teamZ, false);
			}
		}, Config.CTF_RES_TIME * 1000);
		
		return false;
	}
	
	public void giveFlag(L2PcInstance player, int teamId)
	{
		L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (wpn != null)
			{
				player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_RHAND);
			}
		}
		else
		{
			player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			if (wpn != null)
			{
				player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_L_HAND);
			}
		}
		player.getInventory().equipItem(ItemHolder.getInstance().createItem("", _CTFFlagInHandId, 1, player, null));
		player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
		player._CTFHaveFlagOfTeam = teamId;
		player.broadcastUserInfo();
		CreatureSay cs = new CreatureSay(player.getObjectId(), Say2.PARTYROOM_COMMANDER, ":", LocalizationStorage.getInstance().getString(player.getLang(), "CTF.YOU_CAPTURED_FLAG"));
		player.sendPacket(cs);
	}
	
	public void takeFlag(L2PcInstance player)
	{
		L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		player._CTFHaveFlagOfTeam = 0;
		if (wpn != null)
		{
			L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			player.getInventory().destroyItemByItemId("", _CTFFlagInHandId, 1, player, null);
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			player.sendPacket(iu);
			player.sendPacket(new ItemList(player, true));
			player.abortAttack();
			player.broadcastUserInfo();
		}
		else
		{
			player.getInventory().destroyItemByItemId("", _CTFFlagInHandId, 1, player, null);
			player.sendPacket(new ItemList(player, true));
			player.abortAttack();
			player.broadcastUserInfo();
		}
	}
	
	public class CTFTeam extends Team
	{
		public int _flagX;
		public int _flagY;
		public int _flagZ;
		public int _teamFlags;
	}
}