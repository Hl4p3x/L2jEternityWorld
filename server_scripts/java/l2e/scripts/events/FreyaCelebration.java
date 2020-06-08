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
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

/**
 * Rework by LordWinter 01.05.2012 Based on L2J Eternity-World
 */
public class FreyaCelebration extends Event
{
	private static final String event = "FreyaCelebration";
	
	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";
	
	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;
	private static final int _freya = 13296;
	
	private static final int _freya_potion = 15440;
	private static final int _freya_gift = 17138;
	private static final int _hours = 20;
	
	private static final int[] _skills =
	{
		9150,
		9151,
		9152,
		9153,
		9154,
		9155,
		9156
	};
	
	private static final NpcStringId[] FREYA_TEXT =
	{
		NpcStringId.EVEN_THOUGH_YOU_BRING_SOMETHING_CALLED_A_GIFT_AMONG_YOUR_HUMANS_IT_WOULD_JUST_BE_PROBLEMATIC_FOR_ME,
		NpcStringId.I_JUST_DONT_KNOW_WHAT_EXPRESSION_I_SHOULD_HAVE_IT_APPEARED_ON_ME_ARE_HUMANS_EMOTIONS_LIKE_THIS_FEELING,
		NpcStringId.THE_FEELING_OF_THANKS_IS_JUST_TOO_MUCH_DISTANT_MEMORY_FOR_ME,
		NpcStringId.BUT_I_KIND_OF_MISS_IT_LIKE_I_HAD_FELT_THIS_FEELING_BEFORE,
		NpcStringId.I_AM_ICE_QUEEN_FREYA_THIS_FEELING_AND_EMOTION_ARE_NOTHING_BUT_A_PART_OF_MELISSAA_MEMORIES
	};
	
	private static final Location[] _coords =
	{
		new Location(-119494, 44882, 360, 24576),
		new Location(-84023, 243051, -3728, 4096),
		new Location(45538, 48357, -3056, 18000),
		new Location(-45372, -114104, -240, 16384),
		new Location(11546, 17599, -4584, 46900),
		new Location(115096, -178370, -880, 0),
		new Location(-14129, 123869, -3112, 40959),
		new Location(-83156, 150994, -3120, 0),
		new Location(17275, 145000, -3032, 25000),
		new Location(111004, 218928, -3536, 16384),
		new Location(81755, 146487, -3528, 32768),
		new Location(83037, 149324, -3464, 44000),
		new Location(81987, 53723, -1488, 0),
		new Location(147200, 25614, -2008, 16384),
		new Location(147421, -55435, -2728, 49151),
		new Location(86865, -142915, -1336, 26000),
		new Location(43966, -47709, -792, 49999),
		new Location(117293, 76740, -2694, 0),
		new Location(17614, 170147, -3508, 0)
	};
	
	public FreyaCelebration(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_freya);
		addFirstTalkId(_freya);
		addTalkId(_freya);
		addSkillSeeId(_freya);
		
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
			recordSpawn(_freya, loc, false, 0);
		}

		CustomMessage msg = new CustomMessage("EventFreyaCelebration.START", true);
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
		
		CustomMessage msg = new CustomMessage("EventFreyaCelebration.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (event.equalsIgnoreCase("give_potion"))
		{
			if (st.getQuestItemsCount(PcInventory.ADENA_ID) > 1)
			{
				long _curr_time = System.currentTimeMillis();
				String value = loadGlobalQuestVar(player.getAccountName());
				long _reuse_time = value == "" ? 0 : Long.parseLong(value);
				
				if (_curr_time > _reuse_time)
				{
					st.setState(State.STARTED);
					player.destroyItemByItemId("Adena", PcInventory.ADENA_ID, 1, player, true);
					player.addItem("FreyaPotion", _freya_potion, 1, player, true);
					saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + (_hours * 3600000)));
				}
				else
				{
					long remainingTime = (_reuse_time - System.currentTimeMillis()) / 1000;
					int hours = (int) (remainingTime / 3600);
					int minutes = (int) ((remainingTime % 3600) / 60);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
					sm.addItemName(_freya_potion);
					sm.addNumber(hours);
					sm.addNumber(minutes);
					player.sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
				sm.addItemName(PcInventory.ADENA_ID);
				sm.addNumber(1);
				player.sendPacket(sm);
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		return "13296.htm";
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if ((caster == null) || (npc == null))
		{
			return null;
		}
		
		if ((npc.getId() == _freya) && Util.contains(targets, npc) && Util.contains(_skills, skill.getId()))
		{
			if (getRandom(100) < 5)
			{
				CreatureSay cs = new CreatureSay(npc.getObjectId(), Say2.NPC_ALL, npc.getName(), NpcStringId.DEAR_S1_THINK_OF_THIS_AS_MY_APPRECIATION_FOR_THE_GIFT_TAKE_THIS_WITH_YOU_THERES_NOTHING_STRANGE_ABOUT_IT_ITS_JUST_A_BIT_OF_MY_CAPRICIOUSNESS);
				cs.addStringParameter(caster.getName());
				
				npc.broadcastPacket(cs);
				
				caster.addItem("FreyaCelebration", _freya_gift, 1, npc, true);
			}
			else
			{
				if (getRandom(10) < 2)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.NPC_ALL, npc.getName(), FREYA_TEXT[getRandom(FREYA_TEXT.length - 1)]));
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
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
		new FreyaCelebration(-1, "FreyaCelebration", "events");
	}
}