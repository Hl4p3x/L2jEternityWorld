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
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Created by LordWinter 13.12.2012 Fixed by L2J Eternity-World
 */
public class Christmas extends Event
{
	private static final String event = "Christmas";
	
	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";
	
	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;
	
	private final int _santaTrainee = 31863;
	private static int _cristmasTree = 13006;
	
	private static final Location[] _santacoords =
	{
		new Location(81921, 148921, -3467, 16384),
		new Location(146405, 28360, -2269, 49648),
		new Location(19319, 144919, -3103, 31135),
		new Location(-82805, 149890, -3129, 16384),
		new Location(-12347, 122549, -3104, 16384),
		new Location(110642, 220165, -3655, 61898),
		new Location(116619, 75463, -2721, 20881),
		new Location(85513, 16014, -3668, 23681),
		new Location(81999, 53793, -1496, 61621),
		new Location(148159, -55484, -2734, 44315),
		new Location(44185, -48502, -797, 27479),
		new Location(86899, -143229, -1293, 8192)
	};

	private static final Location[] _treecoords =
	{
		new Location(81961, 148921, -3467, 0),
		new Location(146445, 28360, -2269, 0),
		new Location(19319, 144959, -3103, 0),
		new Location(-82845, 149890, -3129, 0),
		new Location(-12387, 122549, -3104, 0),
		new Location(110602, 220165, -3655, 0),
		new Location(116659, 75463, -2721, 0),
		new Location(85553, 16014, -3668, 0),
		new Location(81999, 53743, -1496, 0),
		new Location(148199, -55484, -2734, 0),
		new Location(44185, -48542, -797, 0),
		new Location(86859, -143229, -1293, 0)
	};

	private static void addDrop()
	{
		int item[] =
		{
			5556,
			5557,
			5558,
			5559
		};
		int cnt[] =
		{
			Config.STAR_COUNT,
			Config.BEAD_COUNT,
			Config.FIR_COUNT,
			Config.FLOWER_COUNT
		};
		int chance[] =
		{
			Config.STAR_CHANCE,
			Config.BEAD_CHANCE,
			Config.FIR_CHANCE,
			Config.FLOWER_CHANCE
		};
		EventsDropManager.getInstance().addCristmasRule(event, ruleType.ALL_NPC, item, cnt, chance);
	}
	
	private static void removeDrop()
	{
		EventsDropManager.getInstance().removeCristmasRules(event);
	}
	
	public Christmas(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_santaTrainee);
		addFirstTalkId(_santaTrainee);
		addTalkId(_santaTrainee);
		
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
		
		for (Location loc : _santacoords)
		{
			recordSpawn(_santaTrainee, loc, false, 0);
		}

		for (Location locs : _treecoords)
		{
			recordSpawn(_cristmasTree, locs, false, 0);
		}
		addDrop();


		CustomMessage msg = new CustomMessage("EventChristmas.START", true);
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


		CustomMessage msg = new CustomMessage("EventChristmas.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		if (event.equalsIgnoreCase("0"))
		{
			if((st.getQuestItemsCount(5556) >= 4) && (st.getQuestItemsCount(5557) >= 4) && (st.getQuestItemsCount(5558) >= 10) && (st.getQuestItemsCount(5559) >= 1))
			{
				st.takeItems(5556, 4);
				st.takeItems(5557, 4);
				st.takeItems(5558, 10);
				st.takeItems(5559, 1);
				st.giveItems(5560, 1);
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_REQUIRED_ITEMS));
			return null;
		}
		else if (event.equalsIgnoreCase("1"))
		{
			if(st.getQuestItemsCount(5560) >= 10)
			{
				st.takeItems(5560, 10);
				st.giveItems(5561, 1);
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_REQUIRED_ITEMS));
			return null;
		}
		else if(event.equalsIgnoreCase("2"))
		{
			if(st.getQuestItemsCount(5560) >= 10)
			{
				st.takeItems(5560, 10);
				st.giveItems(7836, 1);
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_REQUIRED_ITEMS));
			return null;
		}
		else if(event.equalsIgnoreCase("3"))
		{
			if(st.getQuestItemsCount(5560) >= 10)
			{
				st.takeItems(5560, 10);
				st.giveItems(8936, 1);
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_REQUIRED_ITEMS));
			return null;
		}
		else if(event.equalsIgnoreCase("4"))
		{
			if(st.getQuestItemsCount(5560) >= 20)
			{
				st.takeItems(5560, 20);
				st.giveItems(10606, 1);
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_REQUIRED_ITEMS));
			return null;
		}
		return htmltext;
	}

	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
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
		new Christmas(-1, "Christmas", "events");
	}
}