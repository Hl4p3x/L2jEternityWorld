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
import l2e.gameserver.model.quest.State;

/**
 * Rework by LordWinter 10.05.2012 Based on L2J Eternity-World
 */
public class TheValentineEvent extends Event
{
	private static final String event = "TheValentineEvent";
	
	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";
	
	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;
	
	private static final int _miss_queen = 4301;
	private static final int _recipe = 20191;
	
	private static void addDrop()
	{
		int item[] =
		{
			20192,
			20193,
			20194
		};
		int cnt[] =
		{
			Config.DARK_CHOCOLATE_COUNT,
			Config.WHITE_CHOCOLATE_COUNT,
			Config.FRESH_CREAM_COUNT
		};
		int chance[] =
		{
			Config.DARK_CHOCOLATE_CHANCE,
			Config.WHITE_CHOCOLATE_CHANCE,
			Config.FRESH_CREAM_CHANCE
		};
		EventsDropManager.getInstance().addValentineRule(event, ruleType.ALL_NPC, item, cnt, chance);
	}
	
	private static void removeDrop()
	{
		EventsDropManager.getInstance().removeValentineRules(event);
	}
	
	private static final Location[] _coords =
	{
		new Location(87792, -142240, -1343, 44000),
		new Location(87616, -140688, -1542, 16500),
		new Location(114733, -178691, -821, 0),
		new Location(115708, -182362, -1449, 0),
		new Location(-44337, -113669, -224, 0),
		new Location(-44628, -115409, -240, 22500),
		new Location(-13073, 122801, -3117, 0),
		new Location(-13949, 121934, -2988, 32768),
		new Location(-14822, 123708, -3117, 8192),
		new Location(-80762, 151118, -3043, 28672),
		new Location(-84049, 150176, -3129, 4096),
		new Location(-82623, 151666, -3129, 49152),
		new Location(-84516, 242971, -3730, 34000),
		new Location(-86003, 243205, -3730, 60000),
		new Location(11281, 15652, -4584, 25000),
		new Location(11303, 17732, -4574, 57344),
		new Location(47151, 49436, -3059, 32000),
		new Location(79806, 55570, -1560, 0),
		new Location(83328, 55824, -1525, 32768),
		new Location(80986, 54504, -1525, 32768),
		new Location(18178, 145149, -3054, 7400),
		new Location(19208, 144380, -3097, 32768),
		new Location(19508, 145775, -3086, 48000),
		new Location(17396, 170259, -3507, 30000),
		new Location(83332, 149160, -3405, 49152),
		new Location(82277, 148598, -3467, 0),
		new Location(81621, 148725, -3467, 32768),
		new Location(81680, 145656, -3533, 32768),
		new Location(117498, 76630, -2695, 38000),
		new Location(115914, 76449, -2711, 59000),
		new Location(119536, 76988, -2275, 40960),
		new Location(147120, 27312, -2192, 40960),
		new Location(147920, 25664, -2000, 16384),
		new Location(111776, 221104, -3543, 16384),
		new Location(107904, 218096, -3675, 0),
		new Location(114920, 220020, -3632, 32768),
		new Location(147888, -58048, -2979, 49000),
		new Location(147285, -56461, -2776, 11500),
		new Location(44176, -48732, -800, 33000),
		new Location(44294, -47642, -792, 50000),
		new Location(-116677, 46824, 360, 34828)
	};
	
	public TheValentineEvent(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_miss_queen);
		addFirstTalkId(_miss_queen);
		addTalkId(_miss_queen);
		
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
			recordSpawn(_miss_queen, loc, false, 0);
		}
		addDrop();

		CustomMessage msg = new CustomMessage("EventTheValentine.START", true);
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

		CustomMessage msg = new CustomMessage("EventTheValentine.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		if (event.equalsIgnoreCase("4301-3.htm"))
		{
			if (st.isCompleted())
			{
				htmltext = "4301-4.htm";
			}
			else
			{
				st.giveItems(_recipe, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				st.setState(State.COMPLETED);
			}
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
		new TheValentineEvent(-1, "TheValentineEvent", "events");
	}
}