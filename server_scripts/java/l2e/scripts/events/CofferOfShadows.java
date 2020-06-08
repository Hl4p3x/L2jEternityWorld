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
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;

/**
 * Created by LordWinter 23.06.2013 Fixed by L2J Eternity-World
 */
public class CofferOfShadows extends Event
{
	private static final String event = "CofferOfShadows";
	
	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";
	
	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;
	
	private static int _manager = 32091;
	
	private static final Location[] _coords =
	{
		new Location(-14823, 123567, -3143, 8192),
		new Location(-83159, 150914, -3155, 49152),
		new Location(18600, 145971, -3095, 40960),
		new Location(82158, 148609, -3493, 60),
		new Location(110992, 218753, -3568, 0),
		new Location(116339, 75424, -2738, 0),
		new Location(81140, 55218, -1551, 32768),
		new Location(147148, 27401, -2231, 2300),
		new Location(43532, -46807, -823, 31471),
		new Location(87765, -141947, -1367, 6500),
		new Location(147154, -55527, -2807, 61300)
	};
	
	public CofferOfShadows(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_manager);
		addFirstTalkId(_manager);
		addTalkId(_manager);
		
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
			recordSpawn(_manager, loc, false, 0);
		}

		CustomMessage msg = new CustomMessage("EventCofferOfShadows.START", true);
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
		

		CustomMessage msg = new CustomMessage("EventCofferOfShadows.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		if (event.equalsIgnoreCase("COFFER1"))
		{
			if ((st.getQuestItemsCount(Config.COFFER_PRICE_ID) >= Config.COFFER_PRICE_AMOUNT))
			{
				st.takeItems(Config.COFFER_PRICE_ID, Config.COFFER_PRICE_AMOUNT);
				st.giveItems(8659, 1);
				return null;
			}
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
		}
		else if (event.equalsIgnoreCase("COFFER5"))
		{
			if ((st.getQuestItemsCount(Config.COFFER_PRICE_ID) >= (Config.COFFER_PRICE_AMOUNT * 5)))
			{
				st.takeItems(Config.COFFER_PRICE_ID, Config.COFFER_PRICE_AMOUNT * 5);
				st.giveItems(8659, 5);
				return null;
			}
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
		}
		else if (event.equalsIgnoreCase("COFFER10"))
		{
			if ((st.getQuestItemsCount(Config.COFFER_PRICE_ID) >= (Config.COFFER_PRICE_AMOUNT * 10)))
			{
				st.takeItems(Config.COFFER_PRICE_ID, Config.COFFER_PRICE_AMOUNT * 10);
				st.giveItems(8659, 10);
				return null;
			}
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
		}
		else if (event.equalsIgnoreCase("COFFER50"))
		{
			if ((st.getQuestItemsCount(Config.COFFER_PRICE_ID) >= (Config.COFFER_PRICE_AMOUNT * 50)))
			{
				st.takeItems(Config.COFFER_PRICE_ID, Config.COFFER_PRICE_AMOUNT * 50);
				st.giveItems(8659, 50);
				return null;
			}
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
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
		new CofferOfShadows(-1, "CofferOfShadows", "events");
	}
}