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
package l2e.scripts.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import l2e.Config;
import l2e.gameserver.Announcements;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance.ConfirmDialogScripts;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Rework by LordWinter 08.09.2012 Based on L2J Eternity-World
 */
public class LastHero extends Quest
{
	protected TIntObjectHashMap<L2PcInstance> _players = new TIntObjectHashMap<>();
	public static EventState CurrentState = EventState.Inactive;
	protected static final String htmlPath = "data/html/mods/";
	protected static LastHero _instance;
	
	public enum EventState
	{
		Disabled,
		Inactive,
		Registration,
		WaitBattle,
		Battle,
		EndBattle
	}
	
	private static final int TeleportCoordinat[][] =
	{
		{
			149438,
			46785,
			-3413
		},
		{
			150052,
			46349,
			-3412
		},
		{
			150247,
			46957,
			-3412
		},
		{
			149346,
			47417,
			-3412
		},
		{
			148595,
			47166,
			-3412
		},
		{
			148744,
			46199,
			-3412
		},
		{
			149432,
			46087,
			-3412
		}
	};
	
	private static final int Doors[] =
	{
		24190002,
		24190003
	};
	private static int AnnounceCount = 1;
	public static ArrayList<String> Players = new ArrayList<>();
	private static ArrayList<String> DeadPlayers = new ArrayList<>();
	private static HashMap<String, Integer[]> PreviousPosition = new HashMap<>(50);
	
	private LastHero(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Config.LH_REGNPC_ID);
		addTalkId(Config.LH_REGNPC_ID);
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.LAST_HERO_ID);
	}
	
	@Override
	protected void init_LoadGlobalData()
	{
		if (Config.LH_ENABLED)
		{
			this.startQuestTimer("OpenRegistration", getMillisecondsUntilNextEvent(), null, null);
		}
	}
	
	public void StartNow()
	{
		if (CurrentState == EventState.Inactive)
		{
			LastHero.getInstance().getQuestTimer("OpenRegistration", null, null).cancel();
			LastHero.getInstance().startQuestTimer("OpenRegistration", 100, null, null);
		}
	}
	
	public static long getMillisecondsUntilNextEvent()
	{
		Calendar currentTime = Calendar.getInstance();
		Calendar nextStartTime = null;
		Calendar testStartTime = null;
		
		try
		{
			for (String timeOfTheDay : Config.LH_EVENT_INTERVAL)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfTheDay = timeOfTheDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfTheDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfTheDay[1]));
				testStartTime.set(Calendar.SECOND, 0);
				
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				if ((nextStartTime == null) || (testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis()))
				{
					nextStartTime = testStartTime;
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("LastHero: Error in getSecondsUntilNextEvent() : " + e.getClass() + ":" + e.getMessage());
		}
		return nextStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
	}
	
	public static void OnConfirmDlgAnswer(L2PcInstance player, int answer)
	{
		if ((player != null) && player.isOnline())
		{
			if (answer == 1)
			{
				if (CurrentState == EventState.Registration)
				{
					if (!player.isInOlympiadMode())
					{
						if (player.getLevel() >= Config.LH_MIN_LEVEL)
						{
							if (!Players.contains(player.getName()))
							{
								if (Players.size() < Config.LH_MAX_PATRICIPATE_COUNT)
								{
									Players.add(player.getName());
									player.sendMessage((new CustomMessage("LastHero.REG_SUCCESFULL", player.getLang())).toString());
								}
								else
								{
									player.sendMessage((new CustomMessage("LastHero.REG_MAX_PLAYERS", player.getLang())).toString());
								}
							}
							else
							{
								player.sendMessage((new CustomMessage("LastHero.REG_ALREADY", player.getLang())).toString());
							}
						}
						else
						{
							player.sendMessage((new CustomMessage("LastHero.REG_FAILED", player.getLang())).toString());
						}
					}
					else
					{
						player.sendMessage((new CustomMessage("LastHero.OLIMPIAD", player.getLang())).toString());
					}
				}
				else
				{
					player.sendMessage((new CustomMessage("LastHero.REGISTER", player.getLang())).toString());
				}
				return;
			}
		}
	}
	
	public static void SendRegistrationRequest(L2PcInstance player)
	{
		if ((player != null) && player.isOnline())
		{
			CustomMessage msg = new CustomMessage("LastHero.WANT_TO_REG", player.getLang());
			player.CurrentConfirmDialog = ConfirmDialogScripts.LastHero;
			ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1.getId());
			confirm.addString(msg.toString());
			confirm.addTime(30000);
			player.sendPacket(confirm);
		}
	}
	
	public static void OnLogout(L2PcInstance player)
	{
		if (Players.contains(player.getName()))
		{
			Players.remove(player.getName());
		}
		if (DeadPlayers.contains(player.getName()))
		{
			DeadPlayers.remove(player.getName());
		}
		if (CurrentState == EventState.Battle)
		{
			if (Players.size() == 1)
			{
				getInstance().getQuestTimer("EndBattle", null, null).cancel();
				getInstance().startQuestTimer("EndBattle", 5000, null, L2World.getInstance().getPlayer(Players.get(0)));
			}
			if (Players.size() == 0)
			{
				getInstance().getQuestTimer("EndBattle", null, null).cancel();
				getInstance().startQuestTimer("EndBattle", 3000, null, null);
			}
		}
		
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (Config.LH_ENABLED == false)
		{
			return "noreg.htm";
		}
		if (CurrentState == EventState.Registration)
		{
			
			if ((Config.LH_MAX_PARTICIPANTS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.LAST_HERO_ID, player, Config.LH_MAX_PARTICIPANTS_PER_IP))
			{
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
				final String htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "IPRestriction.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(AntiFeedManager.getInstance().getLimit(player, Config.LH_MAX_PARTICIPANTS_PER_IP)));
				}
			}
			if (!player.isInOlympiadMode())
			{
				if (player.getLevel() >= Config.LH_MIN_LEVEL)
				{
					if (!Players.contains(player.getName()))
					{
						if (Players.size() < Config.LH_MAX_PATRICIPATE_COUNT)
						{
							Players.add(player.getName());
							return "reg.htm";
						}
						return "max.htm";
					}
					return "yje.htm";
				}
				return "lvl.htm";
			}
			return "Sorry, you are in olimpiad mode";
		}
		return "noreg.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("OpenRegistration"))
		{
			CurrentState = EventState.Registration;
			npc = addSpawn(Config.LH_REGNPC_ID, Config.LH_REGNPC_COORDINATE[0], Config.LH_REGNPC_COORDINATE[1], Config.LH_REGNPC_COORDINATE[2], 30000, false, 0);
			ClearEventVars();
			CustomMessage msg = new CustomMessage("LastHero.OPEN_REGISTER", true);
			AnnounceToPlayers(true, msg);
			AnnounceCount = 1;
			this.startQuestTimer("WaitBattle", Config.LH_TIME_FOR_REGISTRATION_IN_MINUTES * 60000, npc, null);
			this.startQuestTimer("AnnounceRegistration", Config.LH_ANNOUNCE_REGISTRATION_INTERVAL_IN_SECONDS * 1000, null, null);
			for (L2PcInstance pl : L2World.getInstance().getAllPlayersArray())
			{
				if (canParticipate(pl))
				{
					SendRegistrationRequest(pl);
				}
			}
			return null;
		}
		else if (event.equalsIgnoreCase("AnnounceRegistration"))
		{
			if (CurrentState == EventState.Registration)
			{
				int timeleftInt = (Config.LH_TIME_FOR_REGISTRATION_IN_MINUTES * 60) - (Config.LH_ANNOUNCE_REGISTRATION_INTERVAL_IN_SECONDS * AnnounceCount);
				if (timeleftInt <= 0)
				{
					return null;
				}
				AnnounceCount++;
				CustomMessage msg = new CustomMessage("LastHero.BEFORE_START", true);
				msg.add(Util.getTimeFromMilliseconds(timeleftInt * 1000));
				AnnounceToPlayers(true, msg);
				startQuestTimer("AnnounceRegistration", Config.LH_ANNOUNCE_REGISTRATION_INTERVAL_IN_SECONDS * 1000, null, null);
			}
			return null;
		}
		else if (event.equalsIgnoreCase("WaitBattle"))
		{
			npc.deleteMe();
			if (!CheckPlayers())
			{
				CustomMessage msg = new CustomMessage("LastHero.WAS_STOPED", true);
				AnnounceToPlayers(true, msg);
				AntiFeedManager.getInstance().clear(AntiFeedManager.LAST_HERO_ID);
				WaitForNextEvent();
				return null;
			}
			CurrentState = EventState.WaitBattle;
			
			HealnTeleportPlayers(true);
			paralyzePlayers();
			CloseDoors();
			startQuestTimer("StartBattle", Config.LH_TIME_TO_WAIT_BATTLE_IN_SECONDS * 1000, null, null);
			CustomMessage msg = new CustomMessage("LastHero.REGISTER", true);
			msg.add(Config.LH_TIME_TO_WAIT_BATTLE_IN_SECONDS);
			AnnounceToPlayers(true, msg);
			return null;
		}
		else if (event.equalsIgnoreCase("StartBattle"))
		{
			if (!CheckPlayers())
			{
				CustomMessage msg = new CustomMessage("LastHero.WAS_STOPED", true);
				AnnounceToPlayers(true, msg);
				AntiFeedManager.getInstance().clear(AntiFeedManager.LAST_HERO_ID);
				WaitForNextEvent();
				return null;
			}
			CurrentState = EventState.Battle;
			unParalyzePlayers();
			for (String name : Players)
			{
				L2PcInstance pl = L2World.getInstance().getPlayer(name);
				if (pl != null)
				{
					if (!pl.isOnline())
					{
						Players.remove(pl.getName());
					}
				}
			}
			startQuestTimer("EndBattle", Config.LH_BATTLE_DURATION_IN_MINUTES * 60000, null, null);
			return null;
			
		}
		else if (event.equalsIgnoreCase("EndBattle"))
		{
			L2PcInstance winner = player;
			if ((winner == null) || winner.isDead())
			{
				CustomMessage msg = new CustomMessage("LastHero.EVENT_END", true);
				AnnounceToPlayers(true, msg);
			}
			else
			{
				CustomMessage msg = new CustomMessage("LastHero.EVENT_WIN", true);
				msg.add(winner.getName());
				AnnounceToPlayers(true, msg);
				winner.setHero(true);
			}
			AntiFeedManager.getInstance().clear(AntiFeedManager.LAST_HERO_ID);
			HealnTeleportPlayers(false);
			WaitForNextEvent();
			OpenDoors();
			return null;
			
		}
		else if (event.equalsIgnoreCase("RefuseParticipation"))
		{
			if (Players.contains(player.getName()))
			{
				Players.remove(player.getName());
				return "exit.htm";
			}
			return "default.htm";
		}
		return null;
	}
	
	public static String onKill(L2Character killer, L2PcInstance victim)
	{
		if (killer instanceof L2Summon)
		{
			killer = ((L2Summon) killer).getOwner();
		}
		if ((CurrentState == EventState.Battle) && Players.contains(victim.getName()) && Players.contains(killer.getName()))
		{
			victim.setTeam(0);
			Players.remove(victim.getName());
			DeadPlayers.add(victim.getName());
			if (Players.size() == 1)
			{
				getInstance().getQuestTimer("EndBattle", null, null).cancel();
				getInstance().startQuestTimer("EndBattle", 5000, null, L2World.getInstance().getPlayer(Players.get(0)));
			}
			if (Players.size() == 0)
			{
				getInstance().getQuestTimer("EndBattle", null, null).cancel();
				getInstance().startQuestTimer("EndBattle", 3000, null, null);
			}
		}
		return null;
	}
	
	private static void ClearEventVars()
	{
		if (Players == null)
		{
			Players = new ArrayList<>();
		}
		if (DeadPlayers == null)
		{
			DeadPlayers = new ArrayList<>();
		}
		if (PreviousPosition == null)
		{
			PreviousPosition = new HashMap<>();
		}
		Players.clear();
		DeadPlayers.clear();
		PreviousPosition.clear();
		AnnounceCount = 1;
	}
	
	private static Boolean CheckPlayers()
	{
		for (String name : Players)
		{
			L2PcInstance pl = L2World.getInstance().getPlayer(name);
			if (!canParticipate(pl))
			{
				Players.remove(name);
			}
		}
		return Players.size() >= Config.LH_MIN_PARTICIPATE_COUNT;
	}
	
	public static Boolean canParticipate(L2PcInstance player)
	{
		return ((player != null) && (player.getLevel() >= Config.LH_MIN_LEVEL) && player.isOnline() && !player.isInOlympiadMode() && !player.isJailed());
		
	}
	
	private static void HealnTeleportPlayers(Boolean TeleportToEvent)
	{
		Random rand = new Random();
		for (String name : Players)
		{
			L2PcInstance pl = L2World.getInstance().getPlayer(name);
			if (pl != null)
			{
				if (pl.isDead())
				{
					pl.doRevive();
				}
				pl.setCurrentCp(pl.getMaxCp());
				pl.setCurrentMp(pl.getMaxMp());
				pl.setCurrentHp(pl.getMaxHp());
				pl.broadcastStatusUpdate();
				pl.broadcastUserInfo();
				if (TeleportToEvent)
				{
					PreviousPosition.put(name, new Integer[]
					{
						pl.getX(),
						pl.getY(),
						pl.getZ()
					});
					pl.setTeam(2);
					int num = rand.nextInt(TeleportCoordinat.length);
					pl.teleToLocation(TeleportCoordinat[num][0], TeleportCoordinat[num][1], TeleportCoordinat[num][2], true);
					pl.stopAllEffects();
					if (pl.hasSummon())
					{
						pl.getSummon().stopAllEffects();
					}
				}
				else
				{
					pl.setTeam(0);
					Integer[] pos = PreviousPosition.get(name);
					if (pos != null)
					{
						pl.teleToLocation(pos[0], pos[1], pos[2]);
					}
				}
			}
		}
		for (String name : DeadPlayers)
		{
			L2PcInstance pl = L2World.getInstance().getPlayer(name);
			if (pl != null)
			{
				if (pl.isDead())
				{
					pl.doRevive();
				}
				pl.setCurrentCp(pl.getMaxCp());
				pl.setCurrentMp(pl.getMaxMp());
				pl.setCurrentHp(pl.getMaxHp());
				pl.broadcastStatusUpdate();
				pl.broadcastUserInfo();
				pl.setTeam(0);
				
				Integer[] pos = PreviousPosition.get(name);
				if (pos != null)
				{
					pl.teleToLocation(pos[0], pos[1], pos[2]);
				}
			}
		}
	}
	
	private static void CloseDoors()
	{
		for (Integer doorId : Doors)
		{
			DoorParser.getInstance().getDoor(doorId).closeMe();
		}
	}
	
	private static void OpenDoors()
	{
		for (Integer doorId : Doors)
		{
			DoorParser.getInstance().getDoor(doorId).openMe();
		}
	}
	
	private static void WaitForNextEvent()
	{
		CurrentState = EventState.Inactive;
		ClearEventVars();
		getInstance().startQuestTimer("OpenRegistration", getMillisecondsUntilNextEvent(), null, null);
	}
	
	public static Boolean canBeRessurected(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}
		if (CurrentState != EventState.Battle)
		{
			if (Players != null)
			{
				if (Players.contains(player.getName()))
				{
					return false;
				}
			}
			if (DeadPlayers != null)
			{
				if (DeadPlayers.contains(player.getName()))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean canUseEscape(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}
		if (Players == null)
		{
			return true;
		}
		return !(Players.contains(player.getName()));
	}
	
	private static void paralyzePlayers()
	{
		L2Skill revengeSkill = SkillHolder.getInstance().getInfo(4515, 1);
		for (String name : Players)
		{
			L2PcInstance pl = L2World.getInstance().getPlayer(name);
			revengeSkill.getEffects(pl, pl);
			if (pl.hasSummon())
			{
				revengeSkill.getEffects(pl, pl.getSummon());
			}
		}
	}
	
	private static void unParalyzePlayers()
	{
		for (String name : Players)
		{
			L2PcInstance pl = L2World.getInstance().getPlayer(name);
			for (L2Effect eff : pl.getAllEffects())
			{
				if (eff.getSkill().getId() == 4515)
				{
					eff.exit();
					break;
				}
			}
			if (pl.hasSummon())
			{
				for (L2Effect eff : pl.getSummon().getAllEffects())
				{
					if (eff.getSkill().getId() == 4515)
					{
						eff.exit();
						break;
					}
				}
			}
		}
	}
	
	public static boolean onScrollUse(int playerObjectId)
	{
		return true;
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
	
	protected L2PcInstance[] getAllPlayers()
	{
		return _players.values(new L2PcInstance[_players.size()]);
	}
	
	public static LastHero getInstance()
	{
		if (_instance == null)
		{
			_instance = new LastHero(-1, "LastHero", "events");
		}
		return _instance;
	}
	
	public static void main(String[] args)
	{
		LastHero.getInstance();
	}
}