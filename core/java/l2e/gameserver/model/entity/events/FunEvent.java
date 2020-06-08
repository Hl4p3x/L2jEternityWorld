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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2CustomEventManagerInstance;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2DoorTemplate;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;

public abstract class FunEvent
{
	protected static final Logger _log = Logger.getLogger(FunEvent.class.getName());
	
	protected TIntObjectHashMap<L2PcInstance> _players = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<Team> _teams = new TIntObjectHashMap<>();
	protected State _state = State.INACTIVE;
	protected Future<?> _sheduleNext;
	protected long _startNextTime = 0;
	protected countdownTask _countdownTask;
	protected final int _ManagerId = 97001;
	protected L2CustomEventManagerInstance _Manager;
	protected int _instanceId = 0;
	protected ArrayList<L2DoorInstance> _doors = new ArrayList<>();
	protected Future<?> _checkActivityTask;
	
	public int EVENT_ID;
	public String EVENT_NAME;
	protected boolean EVENT_AUTO_MODE;
	protected String[] EVENT_INTERVAL;
	protected int[] EVENT_NPC_LOC;
	protected String EVENT_NPC_LOC_NAME;
	public boolean EVENT_JOIN_CURSED = true;
	protected String EVENT_TEAMS_TYPE;
	public int EVENT_PLAYER_LEVEL_MIN;
	public int EVENT_PLAYER_LEVEL_MAX;
	protected int EVENT_COUNTDOWN_TIME;
	protected int EVENT_MIN_PLAYERS;
	protected ArrayList<Integer> EVENT_DOORS_TO_CLOSE;
	protected ArrayList<Integer> EVENT_DOORS_TO_OPEN;
	
	public enum State
	{
		INACTIVE,
		WAITING,
		PARTICIPATING,
		STARTING,
		FIGHTING
	}
	
	public FunEvent()
	{
		loadConfig();
	}
	
	public State getState()
	{
		return _state;
	}
	
	protected Team getTeam(int team)
	{
		return _teams.get(team);
	}
	
	protected Team[] getAllTeams()
	{
		return _teams.values(new Team[_teams.size()]);
	}
	
	protected L2PcInstance[] getAllPlayers()
	{
		return _players.values(new L2PcInstance[_players.size()]);
	}
	
	public int getStartNextTime()
	{
		long currentTime = Calendar.getInstance().getTimeInMillis();
		return (_startNextTime > currentTime) ? (int) ((_startNextTime - currentTime) / 1000) : 0;
	}
	
	protected void spawnManager()
	{
		try
		{
			_Manager = new L2CustomEventManagerInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(_ManagerId));
			_Manager._event = this;
			_Manager.setName(EVENT_NAME + " Manager");
			_Manager.spawnMe(EVENT_NPC_LOC[0], EVENT_NPC_LOC[1], EVENT_NPC_LOC[2]);
			CustomMessage msg = new CustomMessage(EVENT_NAME + ".REG_STARTED", true);
			msg.add(_Manager.getName());
			msg.add(EVENT_NPC_LOC_NAME);
			AnnounceToPlayers(true, msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected void unspawnManager()
	{
		try
		{
			if (_Manager == null)
			{
				return;
			}
			
			_Manager.deleteMe();
			_Manager = null;
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public void startEvent()
	{
		loadConfig();
		if ((_state != State.INACTIVE) || EVENT_AUTO_MODE)
		{
			return;
		}
		
		_state = State.WAITING;
		sheduleNext(0);
	}
	
	public void autoStart()
	{
		loadConfig();
		if ((_state != State.INACTIVE) || !EVENT_AUTO_MODE)
		{
			return;
		}
		
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime;
			for (String timeOfDay : EVENT_INTERVAL)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				if ((nextStartTime == null) || (testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis()))
				{
					nextStartTime = testStartTime;
				}
			}
			if (nextStartTime != null)
			{
				_log.info(EVENT_NAME + "EventEngine[" + EVENT_NAME + ".autoStart()]: " + EVENT_NAME + " AUTOSTART in " + (nextStartTime.getTimeInMillis() - currentTime.getTimeInMillis()) + " ms.");
				_state = State.WAITING;
				sheduleNext(nextStartTime.getTimeInMillis() - currentTime.getTimeInMillis());
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, EVENT_NAME + "EventEngine[" + EVENT_NAME + ".autoStart()]: Error figuring out a start time. Check " + EVENT_NAME + "EventInterval in config file.");
		}
	}
	
	public void AnnounceToPlayers(Boolean toall, String announce)
	{
		if (toall)
		{
			Announcements.getInstance().announceToAll(announce);
		}
		else
		{
			CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", announce);
			if ((_players != null) && !_players.isEmpty())
			{
				for (L2PcInstance player : getAllPlayers())
				{
					if ((player != null) && player.isOnline())
					{
						player.sendPacket(cs);
					}
				}
			}
		}
	}
	
	public void AnnounceToPlayers(Boolean toall, CustomMessage msg)
	{
		if (toall)
		{
			Announcements.getInstance().announceToAll(msg);
		}
		else
		{
			if ((_players != null) && !_players.isEmpty())
			{
				for (L2PcInstance player : _players.values(new L2PcInstance[_players.size()]))
				{
					CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", msg.toString(player.getLang()));
					if (player.isOnline())
					{
						player.sendPacket(cs);
					}
				}
			}
		}
	}
	
	public void addPlayer(L2PcInstance player, int joinTeamId)
	{
		String lang = player.getLang();
		if ((player.getLevel() < EVENT_PLAYER_LEVEL_MIN) || (player.getLevel() > EVENT_PLAYER_LEVEL_MAX))
		{
			player.sendMessage(LocalizationStorage.getInstance().getString(lang, "FunEvent.CANT_JOIN_LVL"));
			return;
		}
		if (!EVENT_JOIN_CURSED && player.isCursedWeaponEquipped())
		{
			player.sendMessage(LocalizationStorage.getInstance().getString(lang, "FunEvent.CANT_JOIN_CURSED"));
			return;
		}
		if (EVENT_TEAMS_TYPE.equals("SHUFFLE"))
		{
			player._eventName = EVENT_NAME;
			_players.put(player.getObjectId(), player);
		}
		else if (EVENT_TEAMS_TYPE.equals("BALANCE"))
		{
			int topPlayersCount = 0;
			int topPlayersCountTeam = 0;
			int minPlayersCount = 0;
			int minPlayersCountTeam = 0;
			
			for (Team team : _teams.values(new Team[_teams.size()]))
			{
				if (team._playersCount <= minPlayersCount)
				{
					minPlayersCount = team._playersCount;
					minPlayersCountTeam = team._teamId;
				}
				if (team._playersCount >= topPlayersCount)
				{
					topPlayersCount = team._playersCount;
					topPlayersCountTeam = team._teamId;
				}
			}
			
			if ((topPlayersCountTeam == minPlayersCountTeam) || (joinTeamId != topPlayersCountTeam))
			{
				player._eventName = EVENT_NAME;
				player._eventTeamId = joinTeamId;
				_teams.get(joinTeamId)._playersCount++;
				_players.put(player.getObjectId(), player);
			}
			else
			{
				player.sendMessage(LocalizationStorage.getInstance().getString(lang, "FunEvent.CANT_JOIN_TEAM"));
			}
		}
	}
	
	public void removePlayer(L2PcInstance player)
	{
		if (EVENT_TEAMS_TYPE.equals("SHUFFLE"))
		{
			if (_players.containsKey(player.getObjectId()))
			{
				player._eventTeamId = 0;
				_players.remove(player.getObjectId());
			}
		}
		else if (EVENT_TEAMS_TYPE.equals("BALANCE"))
		{
			if (_players.containsKey(player.getObjectId()))
			{
				_teams.get(player._eventTeamId)._playersCount--;
				player._eventTeamId = 0;
				_players.remove(player.getObjectId());
			}
		}
	}
	
	protected void sheduleNext(long delay)
	{
		_startNextTime = Calendar.getInstance().getTimeInMillis() + delay;
		_sheduleNext = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				StartNext();
			}
		}, delay);
	}
	
	protected abstract void loadConfig();
	
	public abstract void abortEvent();
	
	protected abstract void StartNext();
	
	public boolean onPlayerDie(final L2PcInstance player, final L2PcInstance killer)
	{
		return true;
	}
	
	public void onPlayerLogin(final L2PcInstance player)
	{
	}
	
	public void onPlayerLogout(final L2PcInstance player)
	{
	}
	
	public NpcHtmlMessage getChatWindow(L2PcInstance player)
	{
		if (_state != State.PARTICIPATING)
		{
			return null;
		}
		
		String joinType = "";
		String teamsInfo = "";
		String countDownTimer;
		int timeLeft = getStartNextTime();
		
		String lang = player.getLang();
		
		if (EVENT_TEAMS_TYPE.equals("SHUFFLE"))
		{
			joinType = LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_TEAMS_TYPE_SHUFFLE");
		}
		else if (EVENT_TEAMS_TYPE.equals("BALANCE"))
		{
			joinType = LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_TEAMS_TYPE_BALANCE");
		}
		
		if (timeLeft > 60)
		{
			countDownTimer = (timeLeft / 60) + " " + LocalizationStorage.getInstance().getString(lang, "Time.MIN");
		}
		else
		{
			countDownTimer = timeLeft + " " + LocalizationStorage.getInstance().getString(lang, "Time.SEC");
		}
		
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
		
		if (!_players.containsKey(player.getObjectId()))
		{
			String joiningButtons = "";
			
			if (EVENT_TEAMS_TYPE.equals("SHUFFLE"))
			{
				joiningButtons = "<center><button value=\"" + LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_REGISTER") + "\" action=\"bypass -h npc_%objectId%_join 0\" width=200 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>";
			}
			else if (EVENT_TEAMS_TYPE.equals("BALANCE"))
			{
				teamsInfo += "<br>" + LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_PLAYERS") + ":<br>";
				for (Team team : _teams.values(new Team[_teams.size()]))
				{
					teamsInfo += "<font color=" + team._teamColor + ">" + team._teamName + "</font>: " + team._playersCount + "<br>";
				}
				
				joiningButtons += "<br>";
				for (Team team : _teams.values(new Team[_teams.size()]))
				{
					joiningButtons += "<center><button value=\"" + LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_JOIN_TEAM") + " " + team._teamName + "\" action=\"bypass -h npc_%objectId%_join " + team._teamId + "\" width=200 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>";
				}
			}
			
			npcHtmlMessage.setFile(lang, "data/html/addon/EventManager/joining.htm");
			npcHtmlMessage.replace("%eventName%", EVENT_NAME);
			npcHtmlMessage.replace("%joinType%", joinType);
			npcHtmlMessage.replace("%playerLevels%", EVENT_PLAYER_LEVEL_MIN + "-" + EVENT_PLAYER_LEVEL_MAX);
			npcHtmlMessage.replace("%teamsCount%", _teams.size());
			npcHtmlMessage.replace("%playersCount%", _players.size());
			npcHtmlMessage.replace("%playersCountMin%", EVENT_MIN_PLAYERS);
			npcHtmlMessage.replace("%teamsInfo%", teamsInfo);
			npcHtmlMessage.replace("%countdownTime%", countDownTimer);
			npcHtmlMessage.replace("%joiningButtons%", joiningButtons);
		}
		else
		{
			if (EVENT_TEAMS_TYPE.equals("BALANCE"))
			{
				teamsInfo += "<br>" + LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_PLAYERS") + ":<br>";
				for (Team team : _teams.values(new Team[_teams.size()]))
				{
					teamsInfo += "<font color=" + team._teamColor + ">" + team._teamName + "</font>: " + team._playersCount + "<br>";
				}
			}
			Team team = _teams.get(player._eventTeamId);
			String playerTeamName = (team == null) ? LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_NO_TEAM") : team._teamName;
			String playerTeamColor = (team == null) ? "LEVEL" : team._teamColor;
			
			npcHtmlMessage.setFile(lang, "data/html/addon/EventManager/joined.htm");
			npcHtmlMessage.replace("%eventName%", EVENT_NAME);
			npcHtmlMessage.replace("%joinType%", joinType);
			npcHtmlMessage.replace("%playerTeamName%", playerTeamName);
			npcHtmlMessage.replace("%playerTeamColor%", playerTeamColor);
			npcHtmlMessage.replace("%teamsCount%", _teams.size());
			npcHtmlMessage.replace("%playersCount%", _players.size());
			npcHtmlMessage.replace("%playersCountMin%", EVENT_MIN_PLAYERS);
			npcHtmlMessage.replace("%teamsInfo%", teamsInfo);
			npcHtmlMessage.replace("%countdownTime%", countDownTimer);
		}
		return npcHtmlMessage;
	}
	
	public String getInfo(String lang)
	{
		String name = EVENT_NAME + " (" + LocalizationStorage.getInstance().getString(lang, EVENT_NAME + ".DESC") + ")";
		String info = "";
		String state = null;
		int timer = 0;
		int timeLeft = getStartNextTime();
		
		switch (_state)
		{
			case INACTIVE:
				state = LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_EVENT_OFF");
				break;
			case WAITING:
				state = LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_EVENT_WAIT_START");
				timer = timeLeft;
				break;
			case PARTICIPATING:
				state = LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_EVENT_REGISTRATION");
				timer = timeLeft;
				break;
			case STARTING:
				state = LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_EVENT_TELEPORTATION");
				break;
			case FIGHTING:
				state = LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_EVENT_FIGHT");
				timer = timeLeft;
				break;
		}
		
		info += "<br>" + LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_TYPE") + ":&nbsp;<font color=\"LEVEL\">" + name + "</font><br1>";
		info += LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_STATE") + "Status:&nbsp;<font color=\"LEVEL\">" + state + "</font><br1>";
		
		String left = LocalizationStorage.getInstance().getString(lang, "FunEvent.STRING_LEFT");
		if (timer > 0)
		{
			if (timer > 3600)
			{
				info += left + ":&nbsp;<font color=\"LEVEL\">> " + (timer / 3600) + "</font> " + LocalizationStorage.getInstance().getString(lang, "FunEvent.Time.HOUR") + "<br1>";
			}
			else if (timer > 60)
			{
				info += left + ":&nbsp;<font color=\"LEVEL\">" + (timer / 60) + "</font> " + LocalizationStorage.getInstance().getString(lang, "FunEvent.Time.MIN") + "<br1>";
			}
			else
			{
				info += left + ":&nbsp;<font color=\"LEVEL\">" + timer + "</font> " + LocalizationStorage.getInstance().getString(lang, "FunEvent.Time.SEC") + "<br1>";
			}
		}
		
		return info;
	}
	
	protected boolean checkPlayersCount()
	{
		if ((_players == null) || _players.isEmpty() || (_players.size() < EVENT_MIN_PLAYERS))
		{
			CustomMessage msg = new CustomMessage("FunEvent.NOT_ENOUGH_PLAYERS", true);
			msg.add(EVENT_MIN_PLAYERS);
			msg.add(_players.size());
			AnnounceToPlayers(true, msg);
			return false;
		}
		return true;
	}
	
	protected void teleportPlayers()
	{
		L2PcInstance[] players = getAllPlayers();
		if (EVENT_TEAMS_TYPE.equals("SHUFFLE"))
		{
			int index = 1;
			
			for (L2PcInstance player : players)
			{
				player._eventName = EVENT_NAME;
				player._eventTeamId = index;
				if (index < _teams.size())
				{
					index++;
				}
				else
				{
					index = 1;
				}
			}
		}
		
		for (L2PcInstance player : players)
		{
			updatePlayerInfo(player);
		}
		
		CustomMessage msg = new CustomMessage("FunEvent.PREPARE_ARENA", true);
		msg.add(EVENT_NAME);
		AnnounceToPlayers(false, msg);
	}
	
	protected void updatePlayerInfo(L2PcInstance player)
	{
		Team team = _teams.get(player._eventTeamId);
		
		player._eventOriginalTitle = player.getTitle();
		player._eventOriginalKarma = player.getKarma();
		player._eventOriginalNameColor = player.getAppearance().getNameColor();
		if (!team._teamColor.equals("None"))
		{
			player.getAppearance().setNameColor(Util.decodeColor(team._teamColor));
		}
		player.setKarma(0);
		player.broadcastUserInfo();
	}
	
	protected void kickPlayerFromEvent(L2PcInstance player)
	{
		_players.remove(player.getObjectId());
		int x = EVENT_NPC_LOC[0];
		int y = EVENT_NPC_LOC[1];
		int z = EVENT_NPC_LOC[2];
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
			player.teleToLocation(x, y, z, true);
			player.broadcastTitleInfo();
			player.broadcastUserInfo();
			player._eventTeleported = false;
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, karma=? WHERE char_name=?");
				statement.setInt(1, x);
				statement.setInt(2, y);
				statement.setInt(3, z);
				statement.setInt(4, player._eventOriginalKarma);
				statement.setString(5, player.getName());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, EVENT_NAME + "EventEngine[" + EVENT_NAME + ".kickPlayerFromEvent()]: Error while updating player's " + player.getName() + " data: " + e);
			}
		}
	}
	
	protected void kickPlayersFromEvent()
	{
		for (L2PcInstance player : _players.values(new L2PcInstance[_players.size()]))
		{
			kickPlayerFromEvent(player);
		}
	}
	
	protected void countdown()
	{
		_countdownTask = new countdownTask(EVENT_COUNTDOWN_TIME * 60);
		ThreadPoolManager.getInstance().scheduleGeneral(_countdownTask, 1000);
	}
	
	protected class countdownTask implements Runnable
	{
		private boolean _firstmessage = true;
		public long _countdownTime;
		
		public countdownTask(long time)
		{
			_countdownTime = time;
		}
		
		@Override
		public void run()
		{
			if (_state != State.PARTICIPATING)
			{
				return;
			}
			
			if ((_countdownTime == 3600) || (_countdownTime == 1800) || (_countdownTime == 600) || (_countdownTime == 60) || _firstmessage)
			{
				CustomMessage msg = new CustomMessage("FunEvent.TIME_REG_END", true);
				msg.add(EVENT_NAME);
				msg.add(_countdownTime / 60);
				AnnounceToPlayers(true, msg);
				_firstmessage = false;
			}
			
			_countdownTime--;
			
			if (_countdownTime > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
			}
		}
	}
	
	protected void startActivityCheck()
	{
		if (Config.EVENT_CHECK_ACTIVITY_TIME > 0)
		{
			_checkActivityTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new checkActivityTask(), 5000, 5000);
		}
	}
	
	protected void stopActivityCheck()
	{
		if (_checkActivityTask != null)
		{
			_checkActivityTask.cancel(true);
			_checkActivityTask = null;
		}
	}
	
	protected class checkActivityTask implements Runnable
	{
		@Override
		public void run()
		{
			for (L2PcInstance player : getAllPlayers())
			{
				long checkTime = Config.EVENT_CHECK_ACTIVITY_TIME * 1000;
				if (!player.checkLastActivityAction(checkTime) && !player.checkLastActivityMove(checkTime))
				{
					kickPlayerFromEvent(player);
				}
			}
		}
	}
	
	protected void makeDoors(StatsSet set)
	{
		if (_instanceId > 0)
		{
			for (int doorId : EVENT_DOORS_TO_OPEN)
			{
				if (doorId == 0)
				{
					break;
				}
				L2DoorTemplate temp = new L2DoorTemplate(set);
				L2DoorInstance newdoor = new L2DoorInstance(IdFactory.getInstance().getNextId(), temp, set);
				newdoor.setInstanceId(_instanceId);
				newdoor.getStatus().setCurrentHpMp(newdoor.getMaxHp(), newdoor.getMaxMp());
				newdoor.setOpen(true);
				newdoor.getPosition().setXYZInvisible(temp.posX, temp.posY, temp.posZ);
				newdoor.spawnMe(newdoor.getX(), newdoor.getY(), newdoor.getZ());
				_doors.add(newdoor);
			}
			for (int doorId : EVENT_DOORS_TO_CLOSE)
			{
				if (doorId == 0)
				{
					break;
				}
				L2DoorTemplate temp = new L2DoorTemplate(set);
				L2DoorInstance newdoor = new L2DoorInstance(IdFactory.getInstance().getNextId(), temp, set);
				newdoor.setInstanceId(_instanceId);
				newdoor.getStatus().setCurrentHpMp(newdoor.getMaxHp(), newdoor.getMaxMp());
				newdoor.setOpen(false);
				newdoor.getPosition().setXYZInvisible(temp.posX, temp.posY, temp.posZ);
				newdoor.spawnMe(newdoor.getX(), newdoor.getY(), newdoor.getZ());
				_doors.add(newdoor);
			}
		}
		else
		{
			for (int doorId : EVENT_DOORS_TO_OPEN)
			{
				if (doorId == 0)
				{
					break;
				}
				L2DoorInstance doorInstance = DoorParser.getInstance().getDoor(doorId);
				if (doorInstance != null)
				{
					doorInstance.openMe();
				}
			}
			for (int doorId : EVENT_DOORS_TO_CLOSE)
			{
				if (doorId == 0)
				{
					break;
				}
				L2DoorInstance doorInstance = DoorParser.getInstance().getDoor(doorId);
				if (doorInstance != null)
				{
					doorInstance.closeMe();
				}
			}
		}
	}
	
	protected void removeDoors()
	{
		if (_instanceId > 0)
		{
			for (L2DoorInstance door : _doors)
			{
				L2WorldRegion region = door.getWorldRegion();
				door.decayMe();
				if (region != null)
				{
					region.removeVisibleObject(door);
				}
				door.getKnownList().removeAllKnownObjects();
				L2World.getInstance().removeObject(door);
			}
			_doors.clear();
		}
		else
		{
			for (int doorId : EVENT_DOORS_TO_OPEN)
			{
				if (doorId == 0)
				{
					break;
				}
				L2DoorInstance doorInstance = DoorParser.getInstance().getDoor(doorId);
				if (doorInstance != null)
				{
					doorInstance.closeMe();
				}
			}
			for (int doorId : EVENT_DOORS_TO_CLOSE)
			{
				if (doorId == 0)
				{
					break;
				}
				L2DoorInstance doorInstance = DoorParser.getInstance().getDoor(doorId);
				if (doorInstance != null)
				{
					doorInstance.openMe();
				}
			}
		}
	}
	
	public void sendConfirmDialog()
	{
		if (Config.EVENT_SHOW_JOIN_DIALOG && EVENT_TEAMS_TYPE.equals("SHUFFLE"))
		{
			for (L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayersArray())
			{
				if (onlinePlayer.isOnline() && onlinePlayer.getEventName().equals(""))
				{
					CustomMessage msg = new CustomMessage("FunEvent.JOIN_DIALOG", onlinePlayer.getLang());
					msg.add(EVENT_NAME + " (" + LocalizationStorage.getInstance().getString(onlinePlayer.getLang(), EVENT_NAME + ".DESC") + ")");
					ConfirmDlg dlg = new ConfirmDlg(1987);
					dlg.addString(msg.toString());
					dlg.addTime(30000);
					dlg.addRequesterId(EVENT_ID);
					onlinePlayer.sendPacket(dlg);
				}
			}
		}
	}
	
	public void recieveConfirmDialog(L2PcInstance player, int answer)
	{
		if (EVENT_TEAMS_TYPE.equals("SHUFFLE") && (answer == 1))
		{
			addPlayer(player, 0);
		}
	}
	
	protected class Team
	{
		public int _teamId;
		public String _teamName;
		public int _teamX;
		public int _teamY;
		public int _teamZ;
		public String _teamColor = "None";
		public int _teamKills;
		public int _playersCount;
	}
}