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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.util.PlayerEventStatus;

public class L2Event
{
	protected static final Logger _log = Logger.getLogger(L2Event.class.getName());
	
	public static EventState eventState = EventState.OFF;
	public static String _eventName = "";
	public static String _eventCreator = "";
	public static String _eventInfo = "";
	public static int _teamsNumber = 0;
	public static final Map<Integer, String> _teamNames = new FastMap<>();
	public static final List<L2PcInstance> _registeredPlayers = new FastList<>();
	public static final Map<Integer, List<L2PcInstance>> _teams = new FastMap<>();
	public static int _npcId = 0;
	private static final Map<L2PcInstance, PlayerEventStatus> _connectionLossData = new FastMap<>();
	
	public enum EventState
	{
		OFF,
		STANDBY,
		ON
	}
	
	public static int getPlayerTeamId(L2PcInstance player)
	{
		if (player == null)
		{
			return -1;
		}
		
		for (Entry<Integer, List<L2PcInstance>> team : _teams.entrySet())
		{
			if (team.getValue().contains(player))
			{
				return team.getKey();
			}
		}
		return -1;
	}
	
	public static List<L2PcInstance> getTopNKillers(int n)
	{
		final Map<L2PcInstance, Integer> tmp = new HashMap<>();
		for (List<L2PcInstance> teamList : _teams.values())
		{
			for (L2PcInstance player : teamList)
			{
				if (player.getEventStatus() == null)
				{
					continue;
				}
				tmp.put(player, player.getEventStatus().kills.size());
			}
		}
		
		sortByValue(tmp);
		
		if (tmp.size() <= n)
		{
			return new ArrayList<>(tmp.keySet());
		}
		
		final List<L2PcInstance> toReturn = new ArrayList<>(tmp.keySet());
		return toReturn.subList(1, n);
	}
	
	public static void showEventHtml(L2PcInstance player, String objectid)
	{
		if (eventState == EventState.STANDBY)
		{
			try
			{
				final String htmContent;
				NpcHtmlMessage html = new NpcHtmlMessage(5);
				
				if (_registeredPlayers.contains(player))
				{
					htmContent = HtmCache.getInstance().getHtm(player.getLang(), "data/html/mods/EventEngine/Participating.htm");
				}
				else
				{
					htmContent = HtmCache.getInstance().getHtm(player.getLang(), "data/html/mods/EventEngine/Participation.htm");
				}
				
				if (htmContent != null)
				{
					html.setHtml(htmContent);
				}
				
				html.replace("%objectId%", objectid);
				html.replace("%eventName%", _eventName);
				html.replace("%eventCreator%", _eventCreator);
				html.replace("%eventInfo%", _eventInfo);
				player.sendPacket(html);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception on showEventHtml(): " + e.getMessage(), e);
			}
		}
	}
	
	public static void spawnEventNpc(L2PcInstance target)
	{
		
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(_npcId);
		
		try
		{
			L2Spawn spawn = new L2Spawn(template);
			
			spawn.setX(target.getX() + 50);
			spawn.setY(target.getY() + 50);
			spawn.setZ(target.getZ());
			spawn.setAmount(1);
			spawn.setHeading(target.getHeading());
			spawn.stopRespawn();
			SpawnTable.getInstance().addNewSpawn(spawn, false);
			
			spawn.init();
			spawn.getLastSpawn().setCurrentHp(999999999);
			spawn.getLastSpawn().setTitle(_eventName);
			spawn.getLastSpawn().setEventMob(true);
			
			spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on spawn(): " + e.getMessage(), e);
		}
	}
	
	public static void unspawnEventNpcs()
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if ((spawn.getLastSpawn() != null) && spawn.getLastSpawn().isEventMob())
			{
				spawn.getLastSpawn().deleteMe();
				spawn.stopRespawn();
				SpawnTable.getInstance().deleteSpawn(spawn, false);
			}
		}
	}
	
	public static boolean isParticipant(L2PcInstance player)
	{
		if ((player == null) || (player.getEventStatus() == null))
		{
			return false;
		}
		
		switch (eventState)
		{
			case OFF:
				return false;
			case STANDBY:
				return _registeredPlayers.contains(player);
			case ON:
				for (List<L2PcInstance> teamList : _teams.values())
				{
					if (teamList.contains(player))
					{
						return true;
					}
				}
		}
		return false;
	}
	
	public static void registerPlayer(L2PcInstance player)
	{
		if (eventState != EventState.STANDBY)
		{
			player.sendMessage("The registration period for this event is over.");
			return;
		}
		
		if ((Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP == 0) || AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.L2EVENT_ID, player, Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP))
		{
			_registeredPlayers.add(player);
		}
		else
		{
			player.sendMessage("You have reached the maximum allowed participants per IP.");
			return;
		}
	}
	
	public static void removeAndResetPlayer(L2PcInstance player)
	{
		try
		{
			if (isParticipant(player))
			{
				if (player.isDead())
				{
					player.restoreExp(100.0);
					player.doRevive();
					player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
					player.setCurrentCp(player.getMaxCp());
				}
				
				player.getPoly().setPolyInfo(null, "1");
				player.decayMe();
				player.spawnMe(player.getX(), player.getY(), player.getZ());
				CharInfo info1 = new CharInfo(player);
				player.broadcastPacket(info1);
				UserInfo info2 = new UserInfo(player);
				player.sendPacket(info2);
				player.broadcastPacket(new ExBrExtraUserInfo(player));
				
				player.stopTransformation(true);
			}
			
			if (player.getEventStatus() != null)
			{
				player.getEventStatus().restoreInits();
			}
			
			player.setEventStatus(null);
			
			_registeredPlayers.remove(player);
			int teamId = getPlayerTeamId(player);
			if (_teams.containsKey(teamId))
			{
				_teams.get(teamId).remove(player);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error at unregisterAndResetPlayer in the event:" + e.getMessage(), e);
		}
	}
	
	public static void savePlayerEventStatus(L2PcInstance player)
	{
		_connectionLossData.put(player, player.getEventStatus());
	}
	
	public static void restorePlayerEventStatus(L2PcInstance player)
	{
		if (_connectionLossData.containsKey(player))
		{
			player.setEventStatus(_connectionLossData.get(player));
			_connectionLossData.remove(player);
		}
	}
	
	public static String startEventParticipation()
	{
		try
		{
			switch (eventState)
			{
				case ON:
					return "Cannot start event, it is already on.";
				case STANDBY:
					return "Cannot start event, it is on standby mode.";
				case OFF:
					eventState = EventState.STANDBY;
					break;
			}
			
			AntiFeedManager.getInstance().registerEvent(AntiFeedManager.L2EVENT_ID);
			AntiFeedManager.getInstance().clear(AntiFeedManager.L2EVENT_ID);
			
			unspawnEventNpcs();
			_registeredPlayers.clear();
			
			if (NpcTable.getInstance().getTemplate(_npcId) == null)
			{
				return "Cannot start event, invalid npc id.";
			}
			
			try (FileReader fr = new FileReader(Config.DATAPACK_ROOT + "/data/events/" + _eventName);
				BufferedReader br = new BufferedReader(fr))
			{
				_eventCreator = br.readLine();
				_eventInfo = br.readLine();
			}
			
			List<L2PcInstance> temp = new FastList<>();
			for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
			{
				if (!player.isOnline())
				{
					continue;
				}
				
				if (!temp.contains(player))
				{
					spawnEventNpc(player);
					temp.add(player);
				}
				for (L2PcInstance playertemp : player.getKnownList().getKnownPlayers().values())
				{
					if ((Math.abs(playertemp.getX() - player.getX()) < 1000) && (Math.abs(playertemp.getY() - player.getY()) < 1000) && (Math.abs(playertemp.getZ() - player.getZ()) < 1000))
					{
						temp.add(playertemp);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("L2Event: " + e.getMessage());
			return "Cannot start event participation, an error has occured.";
		}
		return "The event participation has been successfully started.";
	}
	
	public static String startEvent()
	{
		try
		{
			switch (eventState)
			{
				case ON:
					return "Cannot start event, it is already on.";
				case STANDBY:
					eventState = EventState.ON;
					break;
				case OFF:
					return "Cannot start event, it is off. Participation start is required.";
			}
			
			unspawnEventNpcs();
			_teams.clear();
			_connectionLossData.clear();
			
			for (int i = 0; i < _teamsNumber; i++)
			{
				_teams.put(i + 1, new FastList<L2PcInstance>());
			}
			
			int i = 0;
			while (!_registeredPlayers.isEmpty())
			{
				int max = 0;
				L2PcInstance biggestLvlPlayer = null;
				for (L2PcInstance player : _registeredPlayers)
				{
					if (player == null)
					{
						continue;
					}
					
					if (max < player.getLevel())
					{
						max = player.getLevel();
						biggestLvlPlayer = player;
					}
				}
				
				if (biggestLvlPlayer == null)
				{
					continue;
				}
				
				_registeredPlayers.remove(biggestLvlPlayer);
				_teams.get(i + 1).add(biggestLvlPlayer);
				biggestLvlPlayer.setEventStatus();
				i = (i + 1) % _teamsNumber;
			}
		}
		catch (Exception e)
		{
			_log.warning("L2Event: " + e.getMessage());
			return "Cannot start event, an error has occured.";
		}
		return "The event has been successfully started.";
	}
	
	public static String finishEvent()
	{
		switch (eventState)
		{
			case OFF:
				return "Cannot finish event, it is already off.";
			case STANDBY:
				for (L2PcInstance player : _registeredPlayers)
				{
					removeAndResetPlayer(player);
				}
				
				unspawnEventNpcs();
				_registeredPlayers.clear();
				_teams.clear();
				_connectionLossData.clear();
				_teamsNumber = 0;
				_eventName = "";
				eventState = EventState.OFF;
				return "The event has been stopped at STANDBY mode, all players unregistered and all event npcs unspawned.";
			case ON:
				for (List<L2PcInstance> teamList : _teams.values())
				{
					for (L2PcInstance player : teamList)
					{
						removeAndResetPlayer(player);
					}
				}
				
				eventState = EventState.OFF;
				AntiFeedManager.getInstance().clear(AntiFeedManager.TVT_ID);
				unspawnEventNpcs();
				_registeredPlayers.clear();
				_teams.clear();
				_connectionLossData.clear();
				_teamsNumber = 0;
				_eventName = "";
				_npcId = 0;
				_eventCreator = "";
				_eventInfo = "";
				return "The event has been stopped, all players unregistered and all event npcs unspawned.";
		}
		return "The event has been successfully finished.";
	}
	
	private static final Map<L2PcInstance, Integer> sortByValue(Map<L2PcInstance, Integer> unsortMap)
	{
		final List<Entry<L2PcInstance, Integer>> list = new LinkedList<>(unsortMap.entrySet());
		Collections.sort(list, new Comparator<Entry<L2PcInstance, Integer>>()
		{
			@Override
			public int compare(Entry<L2PcInstance, Integer> e1, Entry<L2PcInstance, Integer> e2)
			{
				return e1.getValue().compareTo(e2.getValue());
			}
		});
		
		final Map<L2PcInstance, Integer> sortedMap = new LinkedHashMap<>();
		for (Entry<L2PcInstance, Integer> entry : list)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}