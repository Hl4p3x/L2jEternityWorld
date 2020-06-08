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
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class LoveYourGatekeeper extends Event
{
	private static final String event = "LoveYourGatekeeper";

	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";

	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;

	private static final int GATEKEEPER = 32477;
	private static final int GATEKEEPER_TRANSFORMATION_STICK = 12814;

	private static final int HOURS = 24;
	private static final int PRICE = 10000;

	private static SkillsHolder TELEPORTER_TRANSFORM = new SkillsHolder(5655, 1);

	private static final Location[] _coords =
	{
		new Location(-80762, 151118, -3043, 28672),
		new Location(-84046, 150193, -3129, 4096),
		new Location(-82675, 151652, -3129, 46943),
		new Location(-12992, 122818, -3117, 0),
		new Location(-13964, 121947, -2988, 32768),
		new Location(-14823, 123752, -3117, 8192),
		new Location(18178, 145149, -3054, 7400),
		new Location(19185, 144377, -3097, 32768),
		new Location(19508, 145753, -3086, 47999),
		new Location(17396, 170259, -3507, 30000),
		new Location(44150, -48708, -800, 32999),
		new Location(44280, -47664, -792, 49167),
		new Location(79806, 55570, -1560, 0),
		new Location(83328, 55824, -1525, 32768),
		new Location(80986, 54504, -1525, 32768),
		new Location(83358, 149223, -3400, 32768),
		new Location(82277, 148598, -3467, 0),
		new Location(81621, 148725, -3467, 32768),
		new Location(81680, 145656, -3533, 32768),
		new Location(117498, 76630, -2695, 38000),
		new Location(119536, 76988, -2275, 40960),
		new Location(111585, 221011, -3544, 16384),
		new Location(107922, 218094, -3675, 0),
		new Location(114920, 220020, -3632, 32768),
		new Location(147888, -58048, -2979, 49000),
		new Location(147285, -56461, -2776, 11500),
		new Location(147120, 27312, -2192, 40960),
		new Location(147959, 25695, -2000, 16384),
		new Location(87792, -142240, -1343, 44000),
		new Location(87557, -140657, -1542, 20476),
		new Location(115933, 76482, -2711, 58999)
	};
	
	public LoveYourGatekeeper(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(GATEKEEPER);
		addFirstTalkId(GATEKEEPER);
		addTalkId(GATEKEEPER);

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
			recordSpawn(GATEKEEPER, loc, false, 0);
		}

		CustomMessage msg = new CustomMessage("EventLoveYourGatekeeper.START", true);
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
		
		CustomMessage msg = new CustomMessage("EventLoveYourGatekeeper.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "transform_stick":
			{
				if (player.getAdena() >= PRICE)
				{
					long _reuse = 0;
					String _streuse = st.get("reuse");
					if (_streuse != null)
					{
						_reuse = Long.parseLong(_streuse);
					}
					if (_reuse > System.currentTimeMillis())
					{
						final long remainingTime = (_reuse - System.currentTimeMillis()) / 1000;
						final int hours = (int) (remainingTime / 3600);
						final int minutes = (int) ((remainingTime % 3600) / 60);
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
						sm.addItemName(GATEKEEPER_TRANSFORMATION_STICK);
						sm.addNumber(hours);
						sm.addNumber(minutes);
						player.sendPacket(sm);
					}
					else
					{
						st.takeItems(PcInventory.ADENA_ID, PRICE);
						st.giveItems(GATEKEEPER_TRANSFORMATION_STICK, 1);
						st.setState(State.STARTED);
						st.set("reuse", String.valueOf(System.currentTimeMillis() + (HOURS * 3600000)));
					}
					
				}
				else
				{
					return "32477-3.htm";
				}
				return null;
			}
			case "transform":
			{
				if (player.isTransformed())
				{
					return null;
				}
				player.doCast(TELEPORTER_TRANSFORM.getSkill());
				return null;
			}
		}
		return event;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		return "32477.htm";
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
		new LoveYourGatekeeper(-1, "LoveYourGatekeeper", "events");
	}
}