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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance.ConfirmDialogScripts;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.actor.instance.L2ServitorInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Rnd;
import l2e.util.StringUtil;

public class TvTRoundEvent
{
	enum EventState
	{
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		FIRSTROUND,
		FROUNDFINISHED,
		SECONDROUND,
		SROUNDFINISHED,
		THIRDROUND,
		TROUNDFINISHED,
		NOWINNERS,
		REWARDING
	}
	
	protected static final Logger _log = Logger.getLogger(TvTRoundEvent.class.getName());
	
	private static final String htmlPath = "data/html/mods/TvTRoundEvent/";
	public static TvTRoundEventTeam[] _teams = new TvTRoundEventTeam[2];
	private static EventState _state = EventState.INACTIVE;
	private static L2Spawn _npcSpawn = null;
	private static L2Npc _lastNpcSpawn = null;
	private static int _TvTRoundEventInstance = 0;
	private static short _roundTie;
	
	private TvTRoundEvent()
	{
	}
	
	public static void init()
	{
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.TVT_ID);
		_teams[0] = new TvTRoundEventTeam(Config.TVT_ROUND_EVENT_TEAM_1_NAME, Config.TVT_ROUND_EVENT_TEAM_1_COORDINATES);
		_teams[1] = new TvTRoundEventTeam(Config.TVT_ROUND_EVENT_TEAM_2_NAME, Config.TVT_ROUND_EVENT_TEAM_2_COORDINATES);
	}
	
	public static boolean startParticipation()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_ID);
		
		if (tmpl == null)
		{
			_log.warning("TvTRoundEventEngine[TvTRoundEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
			return false;
		}
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setX(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setY(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setZ(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			_npcSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("TvT Round Event Participation");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
			
			for (L2PcInstance pl : L2World.getInstance().getAllPlayersArray())
			{
				if ((pl != null) && pl.isOnline())
				{
					ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1.getId());
					confirm.addString("" + LocalizationStorage.getInstance().getString(pl.getLang(), "TvTRound.WANT_TO_REG") + "");
					confirm.addTime(30000);
					pl.CurrentConfirmDialog = ConfirmDialogScripts.TvTRound;
					pl.sendPacket(confirm);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "TvTRoundEventEngine[TvTRoundEvent.startParticipation()]: exception: " + e.getMessage(), e);
			return false;
		}
		
		setState(EventState.PARTICIPATING);
		return true;
	}
	
	private static int highestLevelPcInstanceOf(Map<Integer, L2PcInstance> players)
	{
		int maxLevel = Integer.MIN_VALUE, maxLevelId = -1;
		for (L2PcInstance player : players.values())
		{
			if (player.getLevel() >= maxLevel)
			{
				maxLevel = player.getLevel();
				maxLevelId = player.getObjectId();
			}
		}
		return maxLevelId;
	}
	
	public static boolean startEvent()
	{
		setState(EventState.STARTING);
		
		Map<Integer, L2PcInstance> allParticipants = new FastMap<>();
		allParticipants.putAll(_teams[0].getParticipatedPlayers());
		allParticipants.putAll(_teams[1].getParticipatedPlayers());
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		cleanRoundTie();
		
		L2PcInstance player;
		Iterator<L2PcInstance> iter;
		if (needParticipationFee())
		{
			iter = allParticipants.values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!hasParticipationFee(player))
				{
					iter.remove();
				}
			}
		}
		
		int balance[] =
		{
			0,
			0
		}, priority = 0, highestLevelPlayerId;
		L2PcInstance highestLevelPlayer;
		while (!allParticipants.isEmpty())
		{
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			if (allParticipants.isEmpty())
			{
				break;
			}
			priority = 1 - priority;
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			priority = balance[0] > balance[1] ? 1 : 0;
		}
		
		if ((_teams[0].getParticipatedPlayerCount() < Config.TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS) || (_teams[1].getParticipatedPlayerCount() < Config.TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS))
		{
			setState(EventState.INACTIVE);
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			unSpawnNpc();
			AntiFeedManager.getInstance().clear(AntiFeedManager.TVT_ID);
			return false;
		}
		
		if (needParticipationFee())
		{
			iter = _teams[0].getParticipatedPlayers().values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!payParticipationFee(player))
				{
					iter.remove();
				}
			}
			iter = _teams[1].getParticipatedPlayers().values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!payParticipationFee(player))
				{
					iter.remove();
				}
			}
		}
		
		if (Config.TVT_ROUND_EVENT_IN_INSTANCE)
		{
			try
			{
				_TvTRoundEventInstance = InstanceManager.getInstance().createDynamicInstance(Config.TVT_ROUND_EVENT_INSTANCE_FILE);
				InstanceManager.getInstance().getInstance(_TvTRoundEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_TvTRoundEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_TvTRoundEventInstance).setEmptyDestroyTime((Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY * 1000) + 60000L);
			}
			catch (Exception e)
			{
				_TvTRoundEventInstance = 0;
				_log.log(Level.WARNING, "TvTRoundEventEngine[TvTRoundEvent.createDynamicInstance]: exception: " + e.getMessage(), e);
			}
		}
		openDoors(Config.TVT_ROUND_DOORS_IDS_TO_OPEN);
		closeDoors(Config.TVT_ROUND_DOORS_IDS_TO_CLOSE);
		openAnteroomDoors();
		
		setState(EventState.FIRSTROUND);
		
		for (TvTRoundEventTeam team : _teams)
		{
			for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					new TvTRoundEventTeleporter(playerInstance, team.getCoordinates(), false, false);
				}
			}
		}
		return true;
	}
	
	public static boolean startFights()
	{
		if ((_teams[0].getParticipatedPlayerCount() < Config.TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS) || (_teams[1].getParticipatedPlayerCount() < Config.TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS))
		{
			return false;
		}
		
		openAnteroomDoors();
		
		for (TvTRoundEventTeam team : _teams)
		{
			for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					new TvTRoundEventTeleporter(playerInstance, team.getCoordinates(), false, false);
				}
			}
		}
		return true;
	}
	
	public static void openAnteroomDoors()
	{
		int TvTRoundWaitOpenAnteroomDoors = (Config.TVT_ROUND_EVENT_WAIT_OPEN_ANTEROOM_DOORS + Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY);
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				openDoors(Config.TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE);
				closeAnteroomDoors();
			}
		}, TvTRoundWaitOpenAnteroomDoors * 1000);
	}
	
	public static void closeAnteroomDoors()
	{
		int TvTRoundWaitCloseAnteroomDoors = (Config.TVT_ROUND_EVENT_WAIT_CLOSE_ANTEROOM_DOORS + Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY);
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				closeDoors(Config.TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE);
			}
		}, TvTRoundWaitCloseAnteroomDoors * 1000);
	}
	
	public static String calculatePoints()
	{
		if (_teams[0].getPoints() == _teams[1].getPoints())
		{
			if ((_teams[0].getParticipatedPlayerCount() == 0) || (_teams[1].getParticipatedPlayerCount() == 0))
			{
				setState(EventState.REWARDING);
				CustomMessage msg = new CustomMessage("TvTRoundEvent.INACTIVITY", true);
				return msg.toString();
			}
			
			CustomMessage msg1 = new CustomMessage("TvTRoundEvent.TIED", true);
			sysMsgToAllParticipants(msg1.toString());
			
			if (Config.TVT_ROUND_GIVE_POINT_TEAM_TIE)
			{
				_teams[0].increaseRoundPoints();
				_teams[1].increaseRoundPoints();
				if (Config.TVT_ROUND_EVENT_STOP_ON_TIE)
				{
					addRoundTie();
				}
				CustomMessage msg = new CustomMessage("TvTRoundEvent.TYING", true);
				return msg.toString();
			}
			
			if (Config.TVT_ROUND_EVENT_STOP_ON_TIE)
			{
				addRoundTie();
			}
			CustomMessage msg2 = new CustomMessage("TvTRoundEvent.TYING", true);
			return msg2.toString();
		}
		setState(EventState.REWARDING);
		
		TvTRoundEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
		team.increaseRoundPoints();
		
		CustomMessage msg3 = new CustomMessage("TvTRoundEvent.ROUND_FINISH", true);
		msg3.add(team.getName());
		msg3.add(team.getPoints());
		
		return msg3.toString();
	}
	
	public static String calculateRewards()
	{
		if (_teams[0].getRoundPoints() == _teams[1].getRoundPoints())
		{
			if ((_teams[0].getParticipatedPlayerCount() == 0) || (_teams[1].getParticipatedPlayerCount() == 0))
			{
				setState(EventState.REWARDING);
				CustomMessage msg = new CustomMessage("TvTRoundEvent.INACTIVITY", true);
				return msg.toString();
			}
			
			CustomMessage msg1 = new CustomMessage("TvTRoundEvent.TIED", true);
			sysMsgToAllParticipants(msg1.toString());
			
			if (Config.TVT_ROUND_REWARD_TEAM_TIE)
			{
				rewardTeam(_teams[0]);
				rewardTeam(_teams[1]);
				CustomMessage msg = new CustomMessage("TvTRoundEvent.TYING", true);
				return msg.toString();
			}
			CustomMessage msg2 = new CustomMessage("TvTRoundEvent.TYING", true);
			return msg2.toString();
		}
		setState(EventState.REWARDING);
		
		TvTRoundEventTeam team = _teams[_teams[0].getRoundPoints() > _teams[1].getRoundPoints() ? 0 : 1];
		rewardTeam(team);
		
		CustomMessage msg3 = new CustomMessage("TvTRoundEvent.NORMAL_FINISH", true);
		msg3.add(team.getName());
		msg3.add(team.getRoundPoints());
		
		return msg3.toString();
	}
	
	private static void rewardTeam(TvTRoundEventTeam team)
	{
		for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
		{
			if (playerInstance == null)
			{
				continue;
			}
			
			SystemMessage systemMessage = null;
			
			for (int[] reward : Config.TVT_ROUND_EVENT_REWARDS)
			{
				PcInventory inv = playerInstance.getInventory();
				
				if (ItemHolder.getInstance().createDummyItem(reward[0]).isStackable())
				{
					inv.addItem("TvT Round Event", reward[0], reward[1], playerInstance, playerInstance);
					
					if (reward[1] > 1)
					{
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
						systemMessage.addItemName(reward[0]);
						systemMessage.addItemNumber(reward[1]);
					}
					else
					{
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						systemMessage.addItemName(reward[0]);
					}
					
					playerInstance.sendPacket(systemMessage);
				}
				else
				{
					for (int i = 0; i < reward[1]; ++i)
					{
						inv.addItem("TvT Round Event", reward[0], 1, playerInstance, playerInstance);
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						systemMessage.addItemName(reward[0]);
						playerInstance.sendPacket(systemMessage);
					}
				}
			}
			StatusUpdate statusUpdate = new StatusUpdate(playerInstance);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			
			statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad());
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Reward.htm"));
			playerInstance.sendPacket(statusUpdate);
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}
	
	public static void stopEvent()
	{
		setState(EventState.INACTIVATING);
		unSpawnNpc();
		openDoors(Config.TVT_ROUND_DOORS_IDS_TO_CLOSE);
		closeDoors(Config.TVT_ROUND_DOORS_IDS_TO_OPEN);
		
		for (TvTRoundEventTeam team : _teams)
		{
			for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					new TvTRoundEventTeleporter(playerInstance, Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES, false, false);
				}
			}
		}
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		setState(EventState.INACTIVE);
		AntiFeedManager.getInstance().clear(AntiFeedManager.TVT_ID);
	}
	
	public static synchronized boolean addParticipant(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		
		byte teamId = 0;
		
		if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount())
		{
			teamId = (byte) (Rnd.get(2));
		}
		else
		{
			teamId = (byte) (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
		}
		playerInstance.addEventListener(new TvTRoundEventListener(playerInstance));
		return _teams[teamId].addPlayer(playerInstance);
	}
	
	public static boolean removeParticipant(int playerObjectId)
	{
		byte teamId = getParticipantTeamId(playerObjectId);
		
		if (teamId != -1)
		{
			_teams[teamId].removePlayer(playerObjectId);
			final L2PcInstance player = L2World.getInstance().getPlayer(playerObjectId);
			if (player != null)
			{
				player.removeEventListener(TvTRoundEventListener.class);
			}
			return true;
		}
		return false;
	}
	
	public static boolean needParticipationFee()
	{
		return (Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[0] != 0) && (Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[1] != 0);
	}
	
	public static boolean hasParticipationFee(L2PcInstance playerInstance)
	{
		return playerInstance.getInventory().getInventoryItemCount(Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[0], -1) >= Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[1];
	}
	
	public static boolean payParticipationFee(L2PcInstance playerInstance)
	{
		return playerInstance.destroyItemByItemId("TvT Participation Fee", Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[0], Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[1], _lastNpcSpawn, true);
	}
	
	public static String getParticipationFee()
	{
		int itemId = Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[0];
		int itemNum = Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[1];
		
		if ((itemId == 0) || (itemNum == 0))
		{
			return "-";
		}
		
		return StringUtil.concat(String.valueOf(itemNum), " ", ItemHolder.getInstance().getTemplate(itemId).getName());
	}
	
	public static void sysMsgToAllParticipants(String message)
	{
		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendMessage(message);
			}
		}
		
		for (L2PcInstance playerInstance : _teams[1].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendMessage(message);
			}
		}
	}
	
	protected static void closeDoors(List<Integer> doors)
	{
		for (int doorId : doors)
		{
			L2DoorInstance doorInstance = DoorParser.getInstance().getDoor(doorId);
			
			if (doorInstance != null)
			{
				doorInstance.closeMe();
			}
		}
	}
	
	protected static void openDoors(List<Integer> doors)
	{
		for (int doorId : doors)
		{
			L2DoorInstance doorInstance = DoorParser.getInstance().getDoor(doorId);
			
			if (doorInstance != null)
			{
				doorInstance.openMe();
			}
		}
	}
	
	private static void unSpawnNpc()
	{
		_lastNpcSpawn.deleteMe();
		SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn(), false);
		_npcSpawn.stopRespawn();
		_npcSpawn = null;
		_lastNpcSpawn = null;
	}
	
	public static void onLogin(L2PcInstance playerInstance)
	{
		if ((playerInstance == null) || (!isStarting() && !isStarted()))
		{
			return;
		}
		
		byte teamId = getParticipantTeamId(playerInstance.getObjectId());
		
		if (teamId == -1)
		{
			return;
		}
		_teams[teamId].addPlayer(playerInstance);
		new TvTRoundEventTeleporter(playerInstance, _teams[teamId].getCoordinates(), true, false);
	}
	
	public static void onLogout(L2PcInstance playerInstance)
	{
		if ((playerInstance != null) && (isStarting() || isStarted() || isParticipating()))
		{
			if (removeParticipant(playerInstance.getObjectId()))
			{
				playerInstance.setXYZInvisible((Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101)) - 50, (Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101)) - 50, Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			}
		}
	}
	
	public static synchronized void onBypass(String command, L2PcInstance playerInstance)
	{
		if ((playerInstance == null) || !isParticipating())
		{
			return;
		}
		
		final String htmContent;
		
		if (command.equals("tvt_round_event_participation"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			int playerLevel = playerInstance.getLevel();
			
			if (playerInstance.isCursedWeaponEquipped())
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "CursedWeaponEquipped.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (OlympiadManager.getInstance().isRegistered(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Olympiad.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (playerInstance.getKarma() > 0)
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Karma.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if ((playerLevel < Config.TVT_ROUND_EVENT_MIN_LVL) || (playerLevel > Config.TVT_ROUND_EVENT_MAX_LVL))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Level.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%roundmin%", String.valueOf(Config.TVT_ROUND_EVENT_MIN_LVL));
					npcHtmlMessage.replace("%roundmax%", String.valueOf(Config.TVT_ROUND_EVENT_MAX_LVL));
				}
			}
			else if ((_teams[0].getParticipatedPlayerCount() == Config.TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS) && (_teams[1].getParticipatedPlayerCount() == Config.TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "TeamsFull.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%roundmax%", String.valueOf(Config.TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS));
				}
			}
			else if ((Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.TVT_ID, playerInstance, Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "IPRestriction.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%roundmax%", String.valueOf(AntiFeedManager.getInstance().getLimit(playerInstance, Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP)));
				}
			}
			else if (needParticipationFee() && !hasParticipationFee(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "ParticipationFee.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%roundfee%", getParticipationFee());
				}
			}
			else if (addParticipant(playerInstance))
			{
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Registered.htm"));
			}
			else
			{
				return;
			}
			
			playerInstance.sendPacket(npcHtmlMessage);
		}
		else if (command.equals("tvt_round_event_remove_participation"))
		{
			removeParticipant(playerInstance.getObjectId());
			if (Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP > 0)
			{
				AntiFeedManager.getInstance().removePlayer(AntiFeedManager.TVT_ID, playerInstance);
			}
			
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Unregistered.htm"));
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}
	
	public static boolean onAction(L2PcInstance playerInstance, int targetedPlayerObjectId)
	{
		if ((playerInstance == null) || !isStarted())
		{
			return true;
		}
		
		if (playerInstance.isGM())
		{
			return true;
		}
		
		byte playerTeamId = getParticipantTeamId(playerInstance.getObjectId());
		byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);
		
		if (((playerTeamId != -1) && (targetedPlayerTeamId == -1)) || ((playerTeamId == -1) && (targetedPlayerTeamId != -1)))
		{
			return false;
		}
		
		if ((playerTeamId != -1) && (targetedPlayerTeamId != -1) && (playerTeamId == targetedPlayerTeamId) && (playerInstance.getObjectId() != targetedPlayerObjectId) && !Config.TVT_ROUND_EVENT_TARGET_TEAM_MEMBERS_ALLOWED)
		{
			return false;
		}
		return true;
	}
	
	public static boolean onScrollUse(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_ROUND_EVENT_SCROLL_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean onPotionUse(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_ROUND_EVENT_POTIONS_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean onEscapeUse(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId))
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean onItemSummon(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_ROUND_EVENT_SUMMON_BY_ITEM_ALLOWED)
		{
			return false;
		}
		return true;
	}
	
	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if ((killedPlayerInstance == null) || !isStarted())
		{
			return;
		}
		
		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());
		
		if (killedTeamId == -1)
		{
			return;
		}
		
		if (Config.TVT_ROUND_EVENT_ON_DIE)
		{
			new TvTRoundEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false, false);
		}
		else
		{
			killedPlayerInstance.sendMessage("You're dead. Now you must wait until new round or event end.");
		}
		
		if (killerCharacter == null)
		{
			return;
		}
		
		L2PcInstance killerPlayerInstance = null;
		
		if ((killerCharacter instanceof L2PetInstance) || (killerCharacter instanceof L2ServitorInstance))
		{
			killerPlayerInstance = ((L2Summon) killerCharacter).getOwner();
			
			if (killerPlayerInstance == null)
			{
				return;
			}
		}
		else if (killerCharacter.isPlayer())
		{
			killerPlayerInstance = (L2PcInstance) killerCharacter;
		}
		else
		{
			return;
		}
		
		byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());
		
		if ((killerTeamId != -1) && (killedTeamId != -1) && (killerTeamId != killedTeamId))
		{
			TvTRoundEventTeam killerTeam = _teams[killerTeamId];
			
			killerTeam.increasePoints();
			
			CreatureSay cs = new CreatureSay(killerPlayerInstance.getObjectId(), Say2.TELL, killerPlayerInstance.getName(), "I have killed " + killedPlayerInstance.getName() + "!");
			
			for (L2PcInstance playerInstance : _teams[killerTeamId].getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					playerInstance.sendPacket(cs);
				}
			}
		}
	}
	
	public static void onTeleported(L2PcInstance playerInstance)
	{
		if (!isStarted() || (playerInstance == null) || !isPlayerParticipant(playerInstance.getObjectId()))
		{
			return;
		}
		
		if (playerInstance.isMageClass())
		{
			if ((Config.TVT_ROUND_EVENT_MAGE_BUFFS != null) && !Config.TVT_ROUND_EVENT_MAGE_BUFFS.isEmpty())
			{
				for (Entry<Integer, Integer> e : Config.TVT_ROUND_EVENT_MAGE_BUFFS.entrySet())
				{
					L2Skill skill = SkillHolder.getInstance().getInfo(e.getKey(), e.getValue());
					if (skill != null)
					{
						skill.getEffects(playerInstance, playerInstance);
					}
				}
			}
		}
		else
		{
			if ((Config.TVT_ROUND_EVENT_FIGHTER_BUFFS != null) && !Config.TVT_ROUND_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for (Entry<Integer, Integer> e : Config.TVT_ROUND_EVENT_FIGHTER_BUFFS.entrySet())
				{
					L2Skill skill = SkillHolder.getInstance().getInfo(e.getKey(), e.getValue());
					if (skill != null)
					{
						skill.getEffects(playerInstance, playerInstance);
					}
				}
			}
		}
	}
	
	public static final boolean checkForTvTRoundSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
	{
		if (!isStarted())
		{
			return true;
		}
		
		final int sourcePlayerId = source.getObjectId();
		final int targetPlayerId = target.getObjectId();
		final boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		final boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);
		
		if (!isSourceParticipant && !isTargetParticipant)
		{
			return true;
		}
		
		if (!(isSourceParticipant && isTargetParticipant))
		{
			return false;
		}
		
		if (getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
		{
			if (!skill.isOffensive())
			{
				return false;
			}
		}
		return true;
	}
	
	public static void addRoundTie()
	{
		++_roundTie;
	}
	
	public static short getRoundTie()
	{
		return _roundTie;
	}
	
	public static void cleanRoundTie()
	{
		_roundTie = 0;
	}
	
	private static void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}
	
	public static boolean isInactive()
	{
		boolean isInactive;
		
		synchronized (_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}
		
		return isInactive;
	}
	
	public static boolean isInactivating()
	{
		boolean isInactivating;
		
		synchronized (_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}
		
		return isInactivating;
	}
	
	public static boolean isParticipating()
	{
		boolean isParticipating;
		
		synchronized (_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}
		
		return isParticipating;
	}
	
	public static boolean isStarting()
	{
		boolean isStarting;
		
		synchronized (_state)
		{
			isStarting = _state == EventState.STARTING;
		}
		
		return isStarting;
	}
	
	public static boolean isInFirstRound()
	{
		boolean isInFirstRound;
		
		synchronized (_state)
		{
			isInFirstRound = _state == EventState.FIRSTROUND;
		}
		
		return isInFirstRound;
	}
	
	public static boolean isFRoundFinished()
	{
		boolean isFRoundFinished;
		
		synchronized (_state)
		{
			isFRoundFinished = _state == EventState.FROUNDFINISHED;
		}
		
		return isFRoundFinished;
	}
	
	public static boolean isInSecondRound()
	{
		boolean isInSecondRound;
		
		synchronized (_state)
		{
			isInSecondRound = _state == EventState.SECONDROUND;
		}
		
		return isInSecondRound;
	}
	
	public static boolean isSRoundFinished()
	{
		boolean isSRoundFinished;
		
		synchronized (_state)
		{
			isSRoundFinished = _state == EventState.SROUNDFINISHED;
		}
		
		return isSRoundFinished;
	}
	
	public static boolean isInThirdRound()
	{
		boolean isInThirdRound;
		
		synchronized (_state)
		{
			isInThirdRound = _state == EventState.THIRDROUND;
		}
		
		return isInThirdRound;
	}
	
	public static boolean isTRoundFinished()
	{
		boolean isTRoundFinished;
		
		synchronized (_state)
		{
			isTRoundFinished = _state == EventState.TROUNDFINISHED;
		}
		
		return isTRoundFinished;
	}
	
	public static boolean isStarted()
	{
		if (isInFirstRound() || isInSecondRound() || isInThirdRound())
		{
			return true;
		}
		return false;
	}
	
	public static boolean isRewarding()
	{
		boolean isRewarding;
		
		synchronized (_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}
		
		return isRewarding;
	}
	
	public static boolean isWithoutWinners()
	{
		boolean isWithoutWinners;
		
		synchronized (_state)
		{
			isWithoutWinners = _state == EventState.NOWINNERS;
		}
		
		return isWithoutWinners;
	}
	
	public static void setFirstRoundFinished()
	{
		setState(EventState.FROUNDFINISHED);
	}
	
	public static void setSecondRoundFinished()
	{
		setState(EventState.SROUNDFINISHED);
	}
	
	public static void setThirdRoundFinished()
	{
		setState(EventState.TROUNDFINISHED);
	}
	
	public static void setInSecondRound()
	{
		setState(EventState.SECONDROUND);
	}
	
	public static void setInThirdRound()
	{
		setState(EventState.THIRDROUND);
	}
	
	public static void setIsWithoutWinners()
	{
		setState(EventState.NOWINNERS);
	}
	
	public static byte getParticipantTeamId(int playerObjectId)
	{
		return (byte) (_teams[0].containsPlayer(playerObjectId) ? 0 : (_teams[1].containsPlayer(playerObjectId) ? 1 : -1));
	}
	
	public static TvTRoundEventTeam getParticipantTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[0] : (_teams[1].containsPlayer(playerObjectId) ? _teams[1] : null));
	}
	
	public static TvTRoundEventTeam getParticipantEnemyTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[1] : (_teams[1].containsPlayer(playerObjectId) ? _teams[0] : null));
	}
	
	public static int[] getParticipantTeamCoordinates(int playerObjectId)
	{
		return _teams[0].containsPlayer(playerObjectId) ? _teams[0].getCoordinates() : (_teams[1].containsPlayer(playerObjectId) ? _teams[1].getCoordinates() : null);
	}
	
	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if (!isParticipating() && !isStarting() && !isStarted())
		{
			return false;
		}
		return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
	}
	
	public static int getParticipatedPlayersCount()
	{
		if (!isParticipating() && !isStarting() && !isStarted())
		{
			return 0;
		}
		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}
	
	public static String[] getTeamNames()
	{
		return new String[]
		{
			_teams[0].getName(),
			_teams[1].getName()
		};
	}
	
	public static int[] getTeamsPlayerCounts()
	{
		return new int[]
		{
			_teams[0].getParticipatedPlayerCount(),
			_teams[1].getParticipatedPlayerCount()
		};
	}
	
	public static int[] getTeamsPoints()
	{
		return new int[]
		{
			_teams[0].getPoints(),
			_teams[1].getPoints()
		};
	}
	
	public static void cleanTeamsPoints()
	{
		_teams[0].cleanPoints();
		_teams[1].cleanPoints();
	}
	
	public static boolean checkForPossibleWinner()
	{
		TvTRoundEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
		if (team.getRoundPoints() == 2)
		{
			return true;
		}
		return false;
	}
	
	public static int getTvTRoundEventInstance()
	{
		return _TvTRoundEventInstance;
	}
	
	public static final boolean checkForTvTSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
	{
		if (!isStarted())
		{
			return true;
		}
		
		final int sourcePlayerId = source.getObjectId();
		final int targetPlayerId = target.getObjectId();
		final boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		final boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);
		
		if (!isSourceParticipant && !isTargetParticipant)
		{
			return true;
		}
		
		if (!(isSourceParticipant && isTargetParticipant))
		{
			return false;
		}
		
		if (getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
		{
			if (!skill.isOffensive())
			{
				return false;
			}
		}
		return true;
	}
}