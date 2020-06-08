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
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Rework by LordWinter 10.05.2012 Based on L2J Eternity-World
 */
public class GiftOfVitality extends Event
{
	private static final String event = "GiftOfVitality";
	
	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";
	
	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;

	private static final int STEVE_SHYAGEL = 4306;

	private static final SkillsHolder GIFT_OF_VITALITY = new SkillsHolder(23179, 1);
	private static final SkillsHolder JOY_OF_VITALITY = new SkillsHolder(23180, 1);
	
	private static SkillsHolder[] FIGHTER_SKILLS =
	{
		new SkillsHolder(5627, 1),
		new SkillsHolder(5628, 1),
		new SkillsHolder(5637, 1),
		new SkillsHolder(5629, 1),
		new SkillsHolder(5630, 1),
		new SkillsHolder(5631, 1),
		new SkillsHolder(5632, 1),
	};
	
	private static SkillsHolder[] MAGE_SKILLS =
	{
		new SkillsHolder(5627, 1),
		new SkillsHolder(5628, 1),
		new SkillsHolder(5637, 1),
		new SkillsHolder(5633, 1),
		new SkillsHolder(5634, 1),
		new SkillsHolder(5635, 1),
		new SkillsHolder(5636, 1),
	};
	
	private static SkillsHolder[] SERVITOR_SKILLS =
	{
		new SkillsHolder(5627, 1),
		new SkillsHolder(5628, 1),
		new SkillsHolder(5637, 1),
		new SkillsHolder(5629, 1),
		new SkillsHolder(5633, 1),
		new SkillsHolder(5630, 1),
		new SkillsHolder(5634, 1),
		new SkillsHolder(5631, 1),
		new SkillsHolder(5635, 1),
		new SkillsHolder(5632, 1),
		new SkillsHolder(5636, 1),
	};

	private static final int HOURS = 5;
	private static final int MIN_LEVEL = 75;
	
	private static final Location[] _coords =
	{
		new Location(82766, 149438, -3464, 33865),
		new Location(82286, 53291, -1488, 15250),
		new Location(147060, 25943, -2008, 18774),
		new Location(148096, -55466, -2728, 40541),
		new Location(87116, -141332, -1336, 52193),
		new Location(43521, -47542, -792, 31655),
		new Location(17203, 144949, -3024, 18166),
		new Location(111164, 221062, -3544, 2714),
		new Location(-13869, 122063, -2984, 18270),
		new Location(-83161, 150915, -3120, 17311),
		new Location(45402, 48355, -3056, 49153),
		new Location(115616, -177941, -896, 30708),
		new Location(-44928, -113608, -192, 30212),
		new Location(-84037, 243194, -3728, 8992),
		new Location(-119690, 44583, 360, 29289),
		new Location(12084, 16576, -4584, 57345)
	};
	
	public GiftOfVitality(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(STEVE_SHYAGEL);
		addFirstTalkId(STEVE_SHYAGEL);
		addTalkId(STEVE_SHYAGEL);
		
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
			recordSpawn(STEVE_SHYAGEL, loc, false, 0);
		}

		CustomMessage msg = new CustomMessage("EventGiftOfVitality.START", true);
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
		
		CustomMessage msg = new CustomMessage("EventGiftOfVitality.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		switch (event)
		{
			case "vitality":
			{
				long _reuse = 0;
				String _streuse = st.get("reuse");
				if (_streuse != null)
				{
					_reuse = Long.parseLong(_streuse);
				}
				if (_reuse > System.currentTimeMillis())
				{
					long remainingTime = (_reuse - System.currentTimeMillis()) / 1000;
					int hours = (int) (remainingTime / 3600);
					int minutes = (int) ((remainingTime % 3600) / 60);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
					sm.addSkillName(23179);
					sm.addNumber(hours);
					sm.addNumber(minutes);
					player.sendPacket(sm);
					htmltext = "4306-notime.htm";
				}
				else
				{
					player.doCast(GIFT_OF_VITALITY.getSkill());
					player.doCast(JOY_OF_VITALITY.getSkill());
					st.setState(State.STARTED);
					st.set("reuse", String.valueOf(System.currentTimeMillis() + (HOURS * 3600000)));
					htmltext = "4306-okvitality.htm";
				}
				break;
			}
			case "memories_player":
			{
				if (player.getLevel() <= MIN_LEVEL)
				{
					htmltext = "4306-nolevel.htm";
				}
				else
				{
					final SkillsHolder[] skills = (player.isMageClass()) ? MAGE_SKILLS : FIGHTER_SKILLS;
					npc.setTarget(player);
					for (SkillsHolder sk : skills)
					{
						npc.doCast(sk.getSkill());
					}
					htmltext = "4306-okbuff.htm";
				}
				break;
			}
			case "memories_summon":
			{
				if (player.getLevel() <= MIN_LEVEL)
				{
					htmltext = "4306-nolevel.htm";
				}
				else if (!player.hasServitor())
				{
					htmltext = "4306-nosummon.htm";
				}
				else
				{
					npc.setTarget(player.getSummon());
					for (SkillsHolder sk : SERVITOR_SKILLS)
					{
						npc.doCast(sk.getSkill());
					}
					htmltext = "4306-okbuff.htm";
				}
				break;
			}
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
		return "4306.htm";
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
		new GiftOfVitality(-1, "GiftOfVitality", "events");
	}
}