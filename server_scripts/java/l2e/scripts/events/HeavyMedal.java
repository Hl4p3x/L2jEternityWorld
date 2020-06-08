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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.instancemanager.EventsDropManager;
import l2e.gameserver.instancemanager.EventsDropManager.ruleType;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.QuestState;

/**
 * Rework by LordWinter 01.05.2012 Based on L2J Eternity-World
 */
public class HeavyMedal extends Event
{
	private static final String event = "HeavyMedal";
	
	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";
	
	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;
	
	private final static int CAT_ROY = 31228;
	private final static int CAT_WINNIE = 31229;
	private final static int GLITTERING_MEDAL = 6393;
	
	private final static int WIN_CHANCE = 50;
	
	private final static int[] MEDALS =
	{
		5,
		10,
		20,
		40
	};
	
	private final static int[] BADGES =
	{
		6399,
		6400,
		6401,
		6402
	};
	
	private static void addDrop()
	{
		int item[] =
		{
			6392,
			6393
		};
		int cnt[] =
		{
			Config.MEDAL_COUNT1,
			Config.MEDAL_COUNT2
		};
		int chance[] =
		{
			Config.MEDAL_CHANCE1,
			Config.MEDAL_CHANCE2
		};
		EventsDropManager.getInstance().addMedalsRule(event, ruleType.ALL_NPC, item, cnt, chance);
	}
	
	private static void removeDrop()
	{
		EventsDropManager.getInstance().removeMedalsRules(event);
	}
	
	private static final Location[] _spawns_winnie =
	{
		new Location(-44342, -113726, -240, 0),
		new Location(-44671, -115437, -240, 22500),
		new Location(-13073, 122841, -3117, 0),
		new Location(-13972, 121893, -2988, 32768),
		new Location(-14843, 123710, -3117, 8192),
		new Location(11327, 15682, -4584, 25000),
		new Location(11243, 17712, -4574, 57344),
		new Location(18154, 145192, -3054, 7400),
		new Location(19214, 144327, -3097, 32768),
		new Location(19459, 145775, -3086, 48000),
		new Location(17418, 170217, -3507, 36000),
		new Location(47146, 49382, -3059, 32000),
		new Location(44157, 50827, -3059, 57344),
		new Location(79798, 55629, -1560, 0),
		new Location(83328, 55769, -1525, 32768),
		new Location(80986, 54452, -1525, 32768),
		new Location(83329, 149095, -3405, 49152),
		new Location(82277, 148564, -3467, 0),
		new Location(81620, 148689, -3464, 32768),
		new Location(81691, 145610, -3467, 32768),
		new Location(114719, -178742, -821, 0),
		new Location(115708, -182422, -1449, 0),
		new Location(-80731, 151152, -3043, 28672),
		new Location(-84097, 150171, -3129, 4096),
		new Location(-82678, 151666, -3129, 49152),
		new Location(117459, 76664, -2695, 38000),
		new Location(115936, 76488, -2711, 59000),
		new Location(119576, 76940, -2275, 40960),
		new Location(-84516, 243015, -3730, 34000),
		new Location(-86031, 243153, -3730, 60000),
		new Location(147124, 27401, -2192, 40960),
		new Location(147985, 25664, -2000, 16384),
		new Location(111724, 221111, -3543, 16384),
		new Location(107899, 218149, -3675, 0),
		new Location(114920, 220080, -3632, 32768),
		new Location(147924, -58052, -2979, 49000),
		new Location(147285, -56461, -2776, 33000),
		new Location(44176, -48688, -800, 33000),
		new Location(44294, -47642, -792, 50000)
	};
	
	private static final Location[] _spawns_roy =
	{
		new Location(-44337, -113669, -224, 0),
		new Location(-44628, -115409, -240, 22500),
		new Location(-13073, 122801, -3117, 0),
		new Location(-13949, 121934, -2988, 32768),
		new Location(-14786, 123686, -3117, 8192),
		new Location(11281, 15652, -4584, 25000),
		new Location(11303, 17732, -4574, 57344),
		new Location(18178, 145149, -3054, 7400),
		new Location(19208, 144380, -3097, 32768),
		new Location(19508, 145775, -3086, 48000),
		new Location(17396, 170259, -3507, 36000),
		new Location(47151, 49436, -3059, 32000),
		new Location(44122, 50784, -3059, 57344),
		new Location(79806, 55570, -1560, 0),
		new Location(83328, 55824, -1525, 32768),
		new Location(80986, 54504, -1525, 32768),
		new Location(83332, 149160, -3405, 49152),
		new Location(82277, 148598, -3467, 0),
		new Location(81621, 148725, -3467, 32768),
		new Location(81680, 145656, -3467, 32768),
		new Location(114733, -178691, -821, 0),
		new Location(115708, -182362, -1449, 0),
		new Location(-80789, 151073, -3043, 28672),
		new Location(-84049, 150176, -3129, 4096),
		new Location(-82623, 151666, -3129, 49152),
		new Location(117498, 76630, -2695, 38000),
		new Location(115914, 76449, -2711, 59000),
		new Location(119536, 76988, -2275, 40960),
		new Location(-84516, 242971, -3730, 34000),
		new Location(-86003, 243205, -3730, 60000),
		new Location(147184, 27405, -2192, 17000),
		new Location(147920, 25664, -2000, 16384),
		new Location(111776, 221104, -3543, 16384),
		new Location(107904, 218096, -3675, 0),
		new Location(114920, 220020, -3632, 32768),
		new Location(147888, -58048, -2979, 49000),
		new Location(147262, -56450, -2776, 33000),
		new Location(44176, -48732, -800, 33000),
		new Location(44319, -47640, -792, 50000)
	};
	
	public HeavyMedal(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CAT_ROY);
		addStartNpc(CAT_WINNIE);
		addTalkId(CAT_ROY);
		addTalkId(CAT_WINNIE);
		addFirstTalkId(CAT_ROY);
		addFirstTalkId(CAT_WINNIE);
		
		restoreStatus();
	}
	
	@Override
	public boolean eventStart()
	{
		if (_isactive)
		{
			return false;
		}
		_isactive = true;
		_npclist = new FastList<>();
		
		for (Location loc : _spawns_roy)
		{
			recordSpawn(CAT_ROY, loc, false, 0);
		}
		for (Location loc : _spawns_winnie)
		{
			recordSpawn(CAT_WINNIE, loc, false, 0);
		}
		addDrop();

		CustomMessage msg = new CustomMessage("EventHeavyMedal.START", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(true);
		
		return true;
	}
	
	@Override
	public boolean eventStop()
	{
		if (!_isactive)
		{
			return false;
		}
		_isactive = false;
		
		if (!_npclist.isEmpty())
		{
			for (L2Npc _npc : _npclist)
			{
				if (_npc != null)
				{
					_npc.deleteMe();
				}
			}
		}
		_npclist.clear();
		removeDrop();

		CustomMessage msg = new CustomMessage("EventHeavyMedal.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		int level = checkLevel(st);
		
		if (event.equalsIgnoreCase("game"))
		{
			htmltext = st.getQuestItemsCount(GLITTERING_MEDAL) < MEDALS[level] ? "31229-no.htm" : "31229-game.htm";
		}
		else if (event.equalsIgnoreCase("heads") || event.equalsIgnoreCase("tails"))
		{
			if (st.getQuestItemsCount(GLITTERING_MEDAL) < MEDALS[level])
			{
				htmltext = "31229-" + event.toLowerCase() + "-10.htm";
			}
			else
			{
				st.takeItems(GLITTERING_MEDAL, MEDALS[level]);
				
				if (getRandom(100) > WIN_CHANCE)
				{
					level = 0;
				}
				else
				{
					if (level > 0)
					{
						st.takeItems(BADGES[level - 1], -1);
					}
					st.giveItems(BADGES[level], 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					level++;
				}
				htmltext = "31229-" + event.toLowerCase() + "-" + String.valueOf(level) + ".htm";
			}
		}
		else if (event.equalsIgnoreCase("talk"))
		{
			htmltext = String.valueOf(npc.getId()) + "-lvl-" + String.valueOf(level) + ".htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		return npc.getId() + ".htm";
	}
	
	public int checkLevel(QuestState st)
	{
		int _lev = 0;
		if (st == null)
		{
			return 0;
		}
		if (st.hasQuestItems(6402))
		{
			_lev = 4;
		}
		else if (st.hasQuestItems(6401))
		{
			_lev = 3;
		}
		else if (st.hasQuestItems(6400))
		{
			_lev = 2;
		}
		else if (st.hasQuestItems(6399))
		{
			_lev = 1;
		}
		return _lev;
	}
	
	private L2Npc recordSpawn(int npcId, Location loc, boolean randomOffSet, long despawnDelay)
	{
		L2Npc _tmp = addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffSet, despawnDelay);
		if (_tmp != null)
		{
			_npclist.add(_tmp);
		}
		return _tmp;
	}
	
	private void restoreStatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int status = 0;
			String event_name = event;
			PreparedStatement statement = con.prepareStatement(UPDATE_STATUS);
			statement.setString(1, event_name);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				status = rset.getInt("status");
			}
			rset.close();
			statement.close();
			if (status > 0)
			{
				eventStart();
			}
			else
			{
				eventStop();
			}
		}
		catch (Exception e)
		{
			_log.warning("Error: Could not restore custom event data info: " + e);
		}
	}
	
	private void updateStatus(boolean newEvent)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			String event_name = event;
			boolean insert = newEvent;
			
			PreparedStatement stmt = con.prepareStatement(insert ? EVENT_INSERT : EVENT_DELETE);
			
			if (newEvent)
			{
				stmt.setString(1, event_name);
				stmt.setInt(2, 1);
				stmt.execute();
				stmt.close();
			}
			else
			{
				stmt.setInt(1, newEvent ? 0 : 0);
				stmt.setString(2, event_name);
				stmt.execute();
				stmt.close();
			}
		}
		catch (Exception e)
		{
			_log.warning("Error: could not update custom event database!");
		}
	}
	
	public static void main(String[] args)
	{
		new HeavyMedal(-1, "HeavyMedal", "events");
	}
}