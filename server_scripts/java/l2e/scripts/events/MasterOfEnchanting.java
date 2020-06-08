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
import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Rework by LordWinter 10.05.2012 Based on L2J Eternity-World
 */
public class MasterOfEnchanting extends Event
{
	private static final String event = "MasterOfEnchanting";
	
	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";
	
	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;
	
	private static final int _master_yogi = 32599;
	private static final int _master_yogi_staff = 13539;
	private static final int _master_yogi_scroll = 13540;
	
	private static final int _staff_price = 1000000;
	private static final int _scroll_24_price = 5000000;
	private static final int _scroll_24_time = 6;
	
	private static final int _scroll_1_price = 500000;
	private static final int _scroll_10_price = 5000000;
	
	private static final int[] _hat_shadow_reward =
	{
		13074,
		13075,
		13076
	};
	
	private static final int[] _hat_event_reward =
	{
		13518,
		13519,
		13522
	};
	
	private static final int[] _crystal_reward =
	{
		9570,
		9571,
		9572
	};
	
	private static void addDrop()
	{
		int item[] =
		{
			13540
		};
		int cnt[] =
		{
			Config.SCROLL_COUNT
		};
		int chance[] =
		{
			Config.SCROLL_CHANCE
		};
		EventsDropManager.getInstance().addMasterOfEnchantingRule(event, ruleType.ALL_NPC, item, cnt, chance);
	}
	
	private static void removeDrop()
	{
		EventsDropManager.getInstance().removeMasterOfEnchantingRules(event);
	}
	
	private static final Location[] _coords =
	{
		new Location(16111, 142850, -2707, 16000),
		new Location(17275, 145000, -3037, 25000),
		new Location(83037, 149324, -3470, 44000),
		new Location(82145, 148609, -3468, 0),
		new Location(81755, 146487, -3534, 32768),
		new Location(-81031, 150038, -3045, 0),
		new Location(-83156, 150994, -3130, 0),
		new Location(-13727, 122117, -2990, 16384),
		new Location(-14129, 123869, -3118, 40959),
		new Location(-84411, 244813, -3730, 57343),
		new Location(-84023, 243051, -3730, 4096),
		new Location(46908, 50856, -2997, 8192),
		new Location(45538, 48357, -3061, 18000),
		new Location(9929, 16324, -4576, 62999),
		new Location(11546, 17599, -4586, 46900),
		new Location(81987, 53723, -1497, 0),
		new Location(81083, 56118, -1562, 32768),
		new Location(147200, 25614, -2014, 16384),
		new Location(148557, 26806, -2206, 32768),
		new Location(117356, 76708, -2695, 49151),
		new Location(115887, 76382, -2714, 0),
		new Location(-117239, 46842, 367, 49151),
		new Location(-119494, 44882, 367, 24576),
		new Location(111004, 218928, -3544, 16384),
		new Location(108426, 221876, -3600, 49151),
		new Location(-45278, -112766, -241, 0),
		new Location(-45372, -114104, -241, 16384),
		new Location(115096, -178370, -891, 0),
		new Location(116199, -182694, -1506, 0),
		new Location(86865, -142915, -1341, 26000),
		new Location(85584, -142490, -1343, 0),
		new Location(147421, -55435, -2736, 49151),
		new Location(148206, -55786, -2782, 61439),
		new Location(43165, -48461, -797, 17000),
		new Location(43966, -47709, -798, 49999)
	};
	
	public MasterOfEnchanting(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_master_yogi);
		addFirstTalkId(_master_yogi);
		addTalkId(_master_yogi);
		
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
			recordSpawn(_master_yogi, loc, false, 0);
		}
		addDrop();

		CustomMessage msg = new CustomMessage("EventMasterOfEnchanting.START", true);
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

		CustomMessage msg = new CustomMessage("EventMasterOfEnchanting.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		if (event.equalsIgnoreCase("buy_staff"))
		{
			if (!st.hasQuestItems(_master_yogi_staff) && (st.getQuestItemsCount(PcInventory.ADENA_ID) > _staff_price))
			{
				st.takeItems(PcInventory.ADENA_ID, _staff_price);
				st.giveItems(_master_yogi_staff, 1);
				htmltext = "32599-staffbuyed.htm";
			}
			else
			{
				htmltext = "32599-staffcant.htm";
			}
		}
		else if (event.equalsIgnoreCase("buy_scroll_24"))
		{
			long _curr_time = System.currentTimeMillis();
			String value = loadGlobalQuestVar(player.getAccountName());
			long _reuse_time = value == "" ? 0 : Long.parseLong(value);
			
			if (_curr_time > _reuse_time)
			{
				if (st.getQuestItemsCount(PcInventory.ADENA_ID) > _scroll_24_price)
				{
					st.takeItems(PcInventory.ADENA_ID, _scroll_24_price);
					st.giveItems(_master_yogi_scroll, 24);
					saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + (_scroll_24_time * 3600000)));
					htmltext = "32599-scroll24.htm";
				}
				else
				{
					htmltext = "32599-s24-no.htm";
				}
			}
			else
			{
				long _remaining_time = (_reuse_time - _curr_time) / 1000;
				int hours = (int) _remaining_time / 3600;
				int minutes = ((int) _remaining_time % 3600) / 60;
				if (hours > 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ITEM_PURCHASABLE_IN_S1_HOURS_S2_MINUTES);
					sm.addNumber(hours);
					sm.addNumber(minutes);
					player.sendPacket(sm);
					htmltext = "32599-scroll24.htm";
				}
				else if (minutes > 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ITEM_PURCHASABLE_IN_S1_MINUTES);
					sm.addNumber(minutes);
					player.sendPacket(sm);
					htmltext = "32599-scroll24.htm";
				}
				else
				{
					if (st.getQuestItemsCount(PcInventory.ADENA_ID) > _scroll_24_price)
					{
						st.takeItems(PcInventory.ADENA_ID, _scroll_24_price);
						st.giveItems(_master_yogi_scroll, 24);
						saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + (_scroll_24_time * 3600000)));
						htmltext = "32599-scroll24.htm";
					}
					else
					{
						htmltext = "32599-s24-no.htm";
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("buy_scroll_1"))
		{
			if (st.getQuestItemsCount(PcInventory.ADENA_ID) > _scroll_1_price)
			{
				st.takeItems(PcInventory.ADENA_ID, _scroll_1_price);
				st.giveItems(_master_yogi_scroll, 1);
				htmltext = "32599-scroll-ok.htm";
			}
			else
			{
				htmltext = "32599-s1-no.htm";
			}
		}
		else if (event.equalsIgnoreCase("buy_scroll_10"))
		{
			if (st.getQuestItemsCount(PcInventory.ADENA_ID) > _scroll_10_price)
			{
				st.takeItems(PcInventory.ADENA_ID, _scroll_10_price);
				st.giveItems(_master_yogi_scroll, 10);
				htmltext = "32599-scroll-ok.htm";
			}
			else
			{
				htmltext = "32599-s10-no.htm";
			}
		}
		else if (event.equalsIgnoreCase("receive_reward"))
		{
			if ((st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == _master_yogi_staff) && (st.getEnchantLevel(_master_yogi_staff) > 3))
			{
				switch (st.getEnchantLevel(_master_yogi_staff))
				{
					case 4:
						st.giveItems(6406, 1);
						break;
					case 5:
						st.giveItems(6406, 2);
						st.giveItems(6407, 1);
						break;
					case 6:
						st.giveItems(6406, 3);
						st.giveItems(6407, 2);
						break;
					case 7:
						st.giveItems(_hat_shadow_reward[getRandom(3)], 1);
						break;
					case 8:
						st.giveItems(955, 1);
						break;
					case 9:
						st.giveItems(955, 1);
						st.giveItems(956, 1);
						break;
					case 10:
						st.giveItems(951, 1);
						break;
					case 11:
						st.giveItems(951, 1);
						st.giveItems(952, 1);
						break;
					case 12:
						st.giveItems(948, 1);
						break;
					case 13:
						st.giveItems(729, 1);
						break;
					case 14:
						st.giveItems(_hat_event_reward[getRandom(3)], 1);
						break;
					case 15:
						st.giveItems(13992, 1);
						break;
					case 16:
						st.giveItems(8762, 1);
						break;
					case 17:
						st.giveItems(959, 1);
						break;
					case 18:
						st.giveItems(13991, 1);
						break;
					case 19:
						st.giveItems(13990, 1);
						break;
					case 20:
						st.giveItems(_crystal_reward[getRandom(3)], 1);
						break;
					case 21:
						st.giveItems(8762, 1);
						st.giveItems(8752, 1);
						st.giveItems(_crystal_reward[getRandom(3)], 1);
						break;
					case 22:
						st.giveItems(13989, 1);
						break;
					case 23:
						st.giveItems(13988, 1);
					default:
						if (st.getEnchantLevel(_master_yogi_staff) > 23)
						{
							st.giveItems(13988, 1);
						}
						break;
				}
				st.takeItems(_master_yogi_staff, 1);
				htmltext = "32599-rewardok.htm";
			}
			else
			{
				htmltext = "32599-rewardnostaff.htm";
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
		new MasterOfEnchanting(-1, "MasterOfEnchanting", "events");
	}
}