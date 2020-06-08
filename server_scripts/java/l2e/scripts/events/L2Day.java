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
 * Rework by LordWinter 11.05.2012 Based on L2J Eternity-World
 */
public class L2Day extends Event
{
	private static final String event = "L2Day";
	
	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";
	
	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;
	
	private final static int letterA = 3875;
	private final static int letterC = 3876;
	private final static int letterE = 3877;
	private final static int letterF = 3878;
	private final static int letterG = 3879;
	private final static int letterI = 3881;
	private final static int letterL = 3882;
	private final static int letterN = 3883;
	private final static int letterO = 3884;
	private final static int letterS = 3886;
	private final static int letterT = 3887;
	private final static int letterII = 3888;
	
	private final static int EventNPC = 4313;
	
	private static final Location[] _coords =
	{
		new Location(-119492, 44877, 363, 23321),
		new Location(-117242, 46843, 363, 48137),
		new Location(-84415, 244813, -3737, 57343),
		new Location(-84021, 243049, -3734, 2902),
		new Location(9924, 16328, -4578, 63027),
		new Location(11548, 17597, -4589, 46936),
		new Location(115087, -178360, -890, 0),
		new Location(116198, -182696, -1513, 63635),
		new Location(46907, 50860, -3000, 7497),
		new Location(45542, 48348, -3064, 49816),
		new Location(-45273, -112764, -244, 0),
		new Location(-45368, -114106, -244, 15612),
		new Location(-81028, 150037, -3048, 62180),
		new Location(-83158, 150994, -3133, 65142),
		new Location(-13726, 122116, -2993, 16383),
		new Location(-14133, 123862, -3121, 39765),
		new Location(16110, 142851, -2714, 18106),
		new Location(17273, 144997, -3039, 22165),
		new Location(82143, 148614, -3475, 612),
		new Location(83037, 149328, -3473, 32199),
		new Location(81757, 146487, -3541, 32767),
		new Location(111003, 218924, -3547, 14661),
		new Location(108423, 221873, -3602, 48655),
		new Location(81986, 53725, -1500, 65051),
		new Location(81081, 56116, -1564, 32767),
		new Location(115883, 76384, -2717, 64055),
		new Location(117350, 76703, -2699, 45557),
		new Location(147198, 25615, -2017, 14613),
		new Location(148558, 26803, -2209, 32024),
		new Location(148210, -55784, -2785, 60699),
		new Location(147415, -55432, -2741, 47429),
		new Location(43962, -47707, -801, 48698),
		new Location(43165, -48465, -801, 17559),
		new Location(86863, -142917, -1345, 27497),
		new Location(87791, -142236, -1348, 43140)
	};
	
	private static void addDrop()
	{
		int item[] =
		{
			3875,
			3876,
			3877,
			3878,
			3879,
			3881,
			3882,
			3883,
			3884,
			3886,
			3887,
			3888
		};
		int cnt[] =
		{
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT,
			Config.LETTER_COUNT
		};
		int chance[] =
		{
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE,
			Config.LETTER_CHANCE
		};
		EventsDropManager.getInstance().addL2DayRule(event, ruleType.ALL_NPC, item, cnt, chance);
	}
	
	private static void removeDrop()
	{
		EventsDropManager.getInstance().removeL2DayRules(event);
	}
	
	public L2Day(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(EventNPC);
		addFirstTalkId(EventNPC);
		addTalkId(EventNPC);
		
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
		
		for (Location loc : _coords)
		{
			recordSpawn(EventNPC, loc, false, 0);
		}
		addDrop();

		CustomMessage msg = new CustomMessage("EventL2Day.START", true);
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

		CustomMessage msg = new CustomMessage("EventL2Day.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		int prize;
		
		if (event.equalsIgnoreCase("LINEAGEII"))
		{
			if ((st.getQuestItemsCount(letterL) >= 1) && (st.getQuestItemsCount(letterI) >= 1) && (st.getQuestItemsCount(letterN) >= 1) && (st.getQuestItemsCount(letterE) >= 2) && (st.getQuestItemsCount(letterA) >= 1) && (st.getQuestItemsCount(letterG) >= 1) && (st.getQuestItemsCount(letterII) >= 1))
			{
				st.takeItems(letterL, 1);
				st.takeItems(letterI, 1);
				st.takeItems(letterN, 1);
				st.takeItems(letterE, 2);
				st.takeItems(letterA, 1);
				st.takeItems(letterG, 1);
				st.takeItems(letterII, 1);
				
				prize = getRandom(1000);
				
				if (prize <= 5)
				{
					st.giveItems(6658, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(52, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13457, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13458, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13459, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13460, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13461, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13462, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13463, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13464, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13465, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13466, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13467, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13468, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13884, 1);
				}
				else if (prize <= 50)
				{
					st.giveItems(13071, 1);
				}
				else if (prize <= 50)
				{
					st.giveItems(13072, 1);
				}
				else if (prize <= 50)
				{
					st.giveItems(13073, 1);
				}
				else if (prize <= 100)
				{
					st.giveItems(10480, 1);
				}
				else if (prize <= 100)
				{
					st.giveItems(10481, 1);
				}
				else if (prize <= 100)
				{
					st.giveItems(10482, 1);
				}
				else if (prize <= 200)
				{
					st.giveItems(9570, 1);
				}
				else if (prize <= 200)
				{
					st.giveItems(9571, 1);
				}
				else if (prize <= 200)
				{
					st.giveItems(9572, 1);
				}
				else if (prize <= 350)
				{
					st.giveItems(3959, 1);
				}
				else if (prize <= 400)
				{
					st.giveItems(3958, 1);
				}
				else if (prize <= 500)
				{
					st.giveItems(21730, 1);
				}
				else
				{
					st.giveItems(14701, 5);
				}
				return null;
			}
			htmltext = "4313-02.htm";
		}
		else if (event.equalsIgnoreCase("NCSOFT"))
		{
			if ((st.getQuestItemsCount(letterN) >= 1) && (st.getQuestItemsCount(letterC) >= 1) && (st.getQuestItemsCount(letterS) >= 1) && (st.getQuestItemsCount(letterO) >= 1) && (st.getQuestItemsCount(letterF) >= 1) && (st.getQuestItemsCount(letterT) >= 1))
			{
				st.takeItems(letterN, 1);
				st.takeItems(letterC, 1);
				st.takeItems(letterS, 1);
				st.takeItems(letterO, 1);
				st.takeItems(letterF, 1);
				st.takeItems(letterT, 1);
				
				prize = getRandom(1000);
				
				if (prize <= 5)
				{
					st.giveItems(6660, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13143, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13144, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13145, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14105, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14106, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14107, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14108, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14109, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14110, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14112, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14113, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14114, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14115, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14116, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14117, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(13887, 1);
				}
				else if (prize <= 10)
				{
					st.giveItems(14111, 1);
				}
				else if (prize <= 125)
				{
					st.giveItems(9552, 1);
				}
				else if (prize <= 125)
				{
					st.giveItems(9553, 1);
				}
				else if (prize <= 125)
				{
					st.giveItems(9554, 1);
				}
				else if (prize <= 125)
				{
					st.giveItems(9555, 1);
				}
				else if (prize <= 125)
				{
					st.giveItems(9556, 1);
				}
				else if (prize <= 125)
				{
					st.giveItems(9557, 1);
				}
				else if (prize <= 350)
				{
					st.giveItems(3959, 2);
				}
				else if (prize <= 400)
				{
					st.giveItems(3958, 2);
				}
				else if (prize <= 500)
				{
					st.giveItems(21730, 1);
				}
				else
				{
					st.giveItems(14701, 5);
				}
				return null;
			}
			htmltext = "4313-02.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		return npc.getId() + ".htm";
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
		new L2Day(-1, "L2Day", "events");
	}
}