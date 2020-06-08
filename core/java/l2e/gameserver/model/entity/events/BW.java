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
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.L2Party.messageType;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2CustomBWBaseInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import gnu.trove.map.hash.TIntObjectHashMap;

public class BW extends FunEvent
{
	protected static final Logger _log = Logger.getLogger(BW.class.getName());
	
	private final int _BWBaseId = 97007;
	private final TIntObjectHashMap<L2CustomBWBaseInstance> _baseSpawns = new TIntObjectHashMap<>();
	
	@Override
	public void loadConfig()
	{
		EVENT_ID = 3;
		EVENT_NAME = "BW";
		EVENT_AUTO_MODE = Config.BW_AUTO_MODE;
		EVENT_INTERVAL = Config.BW_EVENT_INTERVAL;
		EVENT_NPC_LOC = (new int[]
		{
			Config.BW_NPC_X,
			Config.BW_NPC_Y,
			Config.BW_NPC_Z
		});
		EVENT_NPC_LOC_NAME = Config.BW_NPC_LOC_NAME;
		EVENT_TEAMS_TYPE = Config.BW_EVEN_TEAMS;
		EVENT_PLAYER_LEVEL_MIN = Config.BW_PLAYER_LEVEL_MIN;
		EVENT_PLAYER_LEVEL_MAX = Config.BW_PLAYER_LEVEL_MAX;
		EVENT_COUNTDOWN_TIME = Config.BW_COUNTDOWN_TIME;
		EVENT_MIN_PLAYERS = Config.BW_MIN_PLAYERS;
		EVENT_DOORS_TO_CLOSE = Config.BW_DOORS_TO_CLOSE;
		EVENT_DOORS_TO_OPEN = Config.BW_DOORS_TO_OPEN;
	}
	
	@Override
	protected BWTeam getTeam(int team)
	{
		return (BWTeam) _teams.get(team);
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
			unspawnBases();
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
			PreparedStatement statement = con.prepareStatement("SELECT * FROM z_bw_teams");
			ResultSet rset = statement.executeQuery();
			BWTeam team = null;
			int index = 0;
			
			while (rset.next())
			{
				index++;
				
				if (index > Config.BW_TEAMS_NUM)
				{
					break;
				}
				
				team = new BWTeam();
				
				team._teamId = index;
				team._teamName = rset.getString("teamName");
				team._teamX = rset.getInt("teamX");
				team._teamY = rset.getInt("teamY");
				team._teamZ = rset.getInt("teamZ");
				team._baseX = rset.getInt("baseX");
				team._baseY = rset.getInt("baseY");
				team._baseZ = rset.getInt("baseZ");
				team._teamColor = rset.getString("teamColor");
				
				_teams.put(index, team);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "BWEventEngine[BW.loadInfo()]: Error while loading BW Teams data: " + e);
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
		_baseSpawns.clear();
		_players.clear();
		_teams.clear();
	}
	
	private void startFight()
	{
		if (Config.EVENT_INSTANCE)
		{
			_instanceId = InstanceManager.getInstance().createInstance();
		}
		
		for (L2PcInstance player : _players.values(new L2PcInstance[_players.size()]))
		{
			BWTeam team = getTeam(player._eventTeamId);
			
			if (Config.BW_ON_START_UNSUMMON_PET)
			{
				if (player.hasSummon() && (player.getSummon() instanceof L2PetInstance))
				{
					player.getSummon().unSummon(player);
				}
			}
			
			if (Config.BW_ON_START_REMOVE_ALL_EFFECTS)
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
			
			if (Config.BW_AURA && (_teams.size() == 2))
			{
				player.setTeam(player._eventTeamId);
			}
			
			if (player.isMounted())
			{
				player.dismount();
			}
			
			player.getAppearance().setVisibleTitle(LocalizationStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "FunEvent.STRING_KILLS") + ": " + player._eventCountKills);
			player.broadcastTitleInfo();
			player.broadcastUserInfo();
			player.setInstanceId(_instanceId);
			player.teleToLocation(team._teamX, team._teamY, team._teamZ);
			player._eventTeleported = true;
		}
	}
	
	private void spawnBases()
	{
		try
		{
			for (Team teams : _teams.values(new Team[_teams.size()]))
			{
				BWTeam team = (BWTeam) teams;
				L2CustomBWBaseInstance BWBase = new L2CustomBWBaseInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(_BWBaseId));
				BWBase._teamId = team._teamId;
				BWBase._event = this;
				BWBase.setName(team._teamName + "'s Base");
				BWBase.setInstanceId(_instanceId);
				BWBase.setCurrentHpMp(BWBase.getMaxHp(), BWBase.getMaxMp());
				BWBase.setIsInvul(false);
				BWBase.spawnMe(team._baseX, team._baseY, team._baseZ);
				_baseSpawns.put(team._teamId, BWBase);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void unspawnBases()
	{
		try
		{
			if ((_baseSpawns != null) && !_baseSpawns.isEmpty())
			{
				for (L2CustomBWBaseInstance BWBase : _baseSpawns.values(new L2CustomBWBaseInstance[_baseSpawns.size()]))
				{
					BWBase.deleteMe();
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private void endFight()
	{
		int topteamId = 0;
		int topteambases = 0;
		int topteams = 0;
		
		for (Team teams : _teams.values(new Team[_teams.size()]))
		{
			BWTeam team = (BWTeam) teams;
			if (team._teamBases > topteambases)
			{
				topteambases = team._teamBases;
			}
		}
		
		for (Team teams : _teams.values(new Team[_teams.size()]))
		{
			BWTeam team = (BWTeam) teams;
			if (team._teamBases == topteambases)
			{
				topteamId = team._teamId;
				topteams++;
			}
		}
		
		if (topteambases == 0)
		{
			AnnounceToPlayers(true, new CustomMessage("BW.NOBODY_WINS", true));
		}
		else if (topteams > 1)
		{
			AnnounceToPlayers(true, new CustomMessage("BW.NOBODY_WINS", true));
		}
		else
		{
			for (L2PcInstance player : _players.values(new L2PcInstance[_players.size()]))
			{
				if (player._eventTeamId == topteamId)
				{
					player.sendMessage(LocalizationStorage.getInstance().getString(player.getLang(), "BW.YOU_WIN"));
					if (Config.BW_PRICE_NO_KILLS || (player._eventCountKills > 0))
					{
						for (String reward : Config.BW_REWARD)
						{
							String[] rew = reward.split(":");
							player.addItem("BW Event", Integer.parseInt(rew[0]), Integer.parseInt(rew[1]), null, true);
						}
					}
				}
			}
			CustomMessage msg = new CustomMessage("BW.TEAM_WIN", true);
			msg.add(_teams.get(topteamId)._teamName);
			msg.add(topteambases);
			AnnounceToPlayers(true, msg);
		}
	}
	
	private void teleportPlayersBack()
	{
		for (L2PcInstance player : _players.values(new L2PcInstance[_players.size()]))
		{
			if (player.isOnline())
			{
				player.getAppearance().setNameColor(player._eventOriginalNameColor);
				player.getAppearance().setVisibleTitle(player._eventOriginalTitle);
				player.setKarma(player._eventOriginalKarma);
				player.setTeam(0);
				player._eventName = "";
				player._eventTeamId = 0;
				player._eventCountKills = 0;
				player.setInstanceId(0);
				player.teleToLocation(Config.BW_NPC_X, Config.BW_NPC_Y, Config.BW_NPC_Z, false);
				player.broadcastTitleInfo();
				player.broadcastUserInfo();
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, karma=? WHERE char_name=?");
					statement.setInt(1, Config.BW_NPC_X);
					statement.setInt(2, Config.BW_NPC_Y);
					statement.setInt(3, Config.BW_NPC_Z);
					statement.setInt(4, player._eventOriginalKarma);
					statement.setString(5, player.getName());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "BWEventEngine[BW.endFight()]: Error while updating player's " + player.getName() + " data: " + e);
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
			delay = Config.BW_COUNTDOWN_TIME * 60000;
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
			
			delay = Config.BW_FIGHT_TIME * 60000;
			_state = State.FIGHTING;
			startFight();
			spawnBases();
			makeDoors(set);
			startActivityCheck();
		}
		else if (_state == State.FIGHTING)
		{
			stopActivityCheck();
			unspawnBases();
			endFight();
			teleportPlayersBack();
			removeDoors();
			clearData();
			
			_state = State.INACTIVE;
			autoStart();
			return;
		}
		
		sheduleNext(delay);
	}
	
	@Override
	public boolean onPlayerDie(final L2PcInstance player, L2PcInstance killer)
	{
		killer._eventCountKills++;
		killer.getAppearance().setVisibleTitle(LocalizationStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "FunEvent.STRING_KILLS") + ": " + killer._eventCountKills);
		killer.broadcastTitleInfo();
		killer.broadcastUserInfo();
		
		player.getStatus().setCurrentHp(player.getMaxHp());
		player.getStatus().setCurrentMp(player.getMaxMp());
		player.getStatus().setCurrentCp(player.getMaxCp());
		player.broadcastStatusUpdate();
		player.teleToLocation(Config.BW_DEAD_X, Config.BW_DEAD_Y, Config.BW_DEAD_Z, false);
		CustomMessage msg = new CustomMessage("BW.REVIVE", player.getLang());
		msg.add(Config.BW_RES_TIME);
		player.sendMessage(msg.toString());
		
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				BWTeam team = getTeam(player._eventTeamId);
				player.teleToLocation(team._teamX, team._teamY, team._teamZ, false);
			}
		}, Config.BW_RES_TIME * 1000);
		
		return false;
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
				player._eventCountKills = member._eventCountKills;
				player.setKarma(0);
				if (Config.BW_AURA && (_teams.size() == 2))
				{
					player.setTeam(player._eventTeamId);
				}
				player.getAppearance().setNameColor(member.getAppearance().getNameColor());
				player.getAppearance().setVisibleTitle(LocalizationStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "FunEvent.STRING_KILLS") + ": " + player._eventCountKills);
				player.getAppearance().setNameColor(member.getAppearance().getNameColor());
				player.broadcastTitleInfo();
				player.broadcastUserInfo();
				BWTeam team = getTeam(player._eventTeamId);
				player.setInstanceId(_instanceId);
				if (!member._eventTeleported)
				{
					if (Config.BW_ON_START_UNSUMMON_PET)
					{
						if (player.hasSummon() && (player.getSummon() instanceof L2PetInstance))
						{
							player.getSummon().unSummon(player);
						}
					}
					if (Config.BW_ON_START_REMOVE_ALL_EFFECTS)
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
	
	public void onPlayerKillBase(L2PcInstance killer, int teamId)
	{
		BWTeam team = getTeam(killer._eventTeamId);
		team._teamBases++;
		killer.sendMessage(LocalizationStorage.getInstance().getString(killer.getLang(), "BW.YOU_DESTROYED_BASE"));
		
		for (L2PcInstance player : _players.values(new L2PcInstance[_players.size()]))
		{
			if (player._eventTeamId != teamId)
			{
				continue;
			}
			
			_players.remove(player.getObjectId());
			if (player.isOnline())
			{
				player.getAppearance().setNameColor(player._eventOriginalNameColor);
				player.getAppearance().setVisibleTitle(player._eventOriginalTitle);
				player.setKarma(player._eventOriginalKarma);
				player.setTeam(0);
				player._eventName = "";
				player._eventTeamId = 0;
				player._eventCountKills = 0;
				player.setInstanceId(0);
				player.teleToLocation(Config.BW_NPC_X, Config.BW_NPC_Y, Config.BW_NPC_Z, false);
				player.broadcastTitleInfo();
				player.broadcastUserInfo();
				player.sendMessage(LocalizationStorage.getInstance().getString(player.getLang(), "BW.YOUR_BASE_DESTROYED"));
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, karma=? WHERE char_name=?");
					statement.setInt(1, Config.BW_NPC_X);
					statement.setInt(2, Config.BW_NPC_Y);
					statement.setInt(3, Config.BW_NPC_Z);
					statement.setInt(4, player._eventOriginalKarma);
					statement.setString(5, player.getName());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "BWEventEngine[BW.onPlayerKillBase()]: Error while updating player's " + player.getName() + " data: " + e);
				}
			}
		}
		
		_baseSpawns.remove(teamId);
		if (_baseSpawns.size() < 2)
		{
			abortEvent();
		}
	}
	
	public class BWTeam extends Team
	{
		public int _baseX;
		public int _baseY;
		public int _baseZ;
		public int _teamBases;
	}
}