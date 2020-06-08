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
package l2e.scripts.quests;

import javolution.util.FastMap;
import l2e.gameserver.model.L2Bingo;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 06.08.2011 Based on L2J Eternity-World
 */
public class _386_StolenDignity extends Quest
{
	private static final String qn = "_386_StolenDignity";
	
	// NPCs
	private final static int Romp = 30843;
	
	// Items
	private final static short Stolen_Infernium_Ore = 6363;
	
	private final static short Required_Stolen_Infernium_Ore = 100;
	private final static FastMap<Integer, Integer> dropchances = new FastMap<>();
	protected final static FastMap<Integer, Bingo> bingos = new FastMap<>();
	
	protected final static int[][] Rewards_Win =
	{
		{
			5529,
			10
		}, // dragon_slayer_edge
		{
			5532,
			10
		}, // meteor_shower_head
		{
			5533,
			10
		}, // elysian_head
		{
			5534,
			10
		}, // soul_bow_shaft
		{
			5535,
			10
		}, // carnium_bow_shaft
		{
			5536,
			10
		}, // bloody_orchid_head
		{
			5537,
			10
		}, // soul_separator_head
		{
			5538,
			10
		}, // dragon_grinder_edge
		{
			5539,
			10
		}, // blood_tornado_edge
		{
			5541,
			10
		}, // tallum_glaive_edge
		{
			5542,
			10
		}, // halbard_edge
		{
			5543,
			10
		}, // dasparion_s_staff_head
		{
			5544,
			10
		}, // worldtree_s_branch_head
		{
			5545,
			10
		}, // dark_legion_s_edge_edge
		{
			5546,
			10
		}, // sword_of_miracle_edge
		{
			5547,
			10
		}, // elemental_sword_edge
		{
			5548,
			10
		}, // tallum_blade_edge
		{
			8331,
			10
		}, // Infernal Master Blade
		{
			8341,
			10
		}, // Spiritual Eye Piece
		{
			8342,
			10
		}, // Flaming Dragon Skull Piece
		{
			8346,
			10
		}, // Hammer Piece of Destroyer
		{
			8349,
			10
		}, // Doom Crusher Head
		{
			8712,
			10
		}, // Sirra's Blade Edge
		{
			8713,
			10
		}, // Sword of Ipos Blade
		{
			8714,
			10
		}, // Barakiel's Axe Piece
		{
			8715,
			10
		}, // Behemoth's Tuning Fork Piece
		{
			8716,
			10
		}, // Naga Storm Piece
		{
			8717,
			10
		}, // Tiphon's Spear Edge
		{
			8718,
			10
		}, // Shyeed's Bow Shaft
		{
			8719,
			10
		}, // Sobekk's Hurricane Edge
		{
			8720,
			10
		}, // Themis' Tongue Piece
		{
			8721,
			10
		}, // Cabrio's Hand Head
		{
			8722,
			10
		}, // Daimon Crystal Fragment
	};
	
	protected final static int[][] Rewards_Lose =
	{
		{
			5529,
			4
		}, // dragon_slayer_edge
		{
			5532,
			4
		}, // meteor_shower_head
		{
			5533,
			4
		}, // elysian_head
		{
			5534,
			4
		}, // soul_bow_shaft
		{
			5535,
			4
		}, // carnium_bow_shaft
		{
			5536,
			4
		}, // bloody_orchid_head
		{
			5537,
			4
		}, // soul_separator_head
		{
			5538,
			4
		}, // dragon_grinder_edge
		{
			5539,
			4
		}, // blood_tornado_edge
		{
			5541,
			4
		}, // tallum_glaive_edge
		{
			5542,
			4
		}, // halbard_edge
		{
			5543,
			4
		}, // dasparion_s_staff_head
		{
			5544,
			4
		}, // worldtree_s_branch_head
		{
			5545,
			4
		}, // dark_legion_s_edge_edge
		{
			5546,
			4
		}, // sword_of_miracle_edge
		{
			5547,
			4
		}, // elemental_sword_edge
		{
			5548,
			4
		}, // tallum_blade_edge
		{
			8331,
			4
		}, // Infernal Master Blade
		{
			8341,
			4
		}, // Spiritual Eye Piece
		{
			8342,
			4
		}, // Flaming Dragon Skull Piece
		{
			8346,
			4
		}, // Hammer Piece of Destroyer
		{
			8349,
			4
		}, // Doom Crusher Head
		{
			8712,
			4
		}, // Sirra's Blade Edge
		{
			8713,
			4
		}, // Sword of Ipos Blade
		{
			8714,
			4
		}, // Barakiel's Axe Piece
		{
			8715,
			4
		}, // Behemoth's Tuning Fork Piece
		{
			8716,
			4
		}, // Naga Storm Piece
		{
			8717,
			4
		}, // Tiphon's Spear Edge
		{
			8718,
			4
		}, // Shyeed's Bow Shaft
		{
			8719,
			4
		}, // Sobekk's Hurricane Edge
		{
			8720,
			4
		}, // Themis' Tongue Piece
		{
			8721,
			4
		}, // Cabrio's Hand Head
		{
			8722,
			4
		}, // Daimon Crystal Fragment
	};
	
	public _386_StolenDignity(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(Romp);
		addTalkId(Romp);
		
		dropchances.put(20670, 140000);
		dropchances.put(20671, 140000);
		dropchances.put(20954, 110000);
		dropchances.put(20956, 130000);
		dropchances.put(20958, 130000);
		dropchances.put(20959, 130000);
		dropchances.put(20960, 110000);
		dropchances.put(20964, 130000);
		dropchances.put(20969, 190000);
		dropchances.put(20967, 180000);
		dropchances.put(20970, 180000);
		dropchances.put(20971, 180000);
		dropchances.put(20974, 280000);
		dropchances.put(20975, 280000);
		dropchances.put(21001, 140000);
		dropchances.put(21003, 180000);
		dropchances.put(21005, 140000);
		dropchances.put(21020, 160000);
		dropchances.put(21021, 150000);
		dropchances.put(21259, 150000);
		dropchances.put(21089, 130000);
		dropchances.put(21108, 190000);
		dropchances.put(21110, 180000);
		dropchances.put(21113, 250000);
		dropchances.put(21114, 230000);
		dropchances.put(21116, 250000);
		
		for (int kill_id : dropchances.keySet())
		{
			addKillId(kill_id);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30843-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30843-06.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("game"))
		{
			if (st.getQuestItemsCount(Stolen_Infernium_Ore) < Required_Stolen_Infernium_Ore)
			{
				return "30843-08.htm";
			}
			st.takeItems(Stolen_Infernium_Ore, Required_Stolen_Infernium_Ore);
			int char_obj_id = player.getObjectId();
			if (bingos.containsKey(char_obj_id))
			{
				bingos.remove(char_obj_id);
			}
			Bingo bingo = new Bingo(st);
			bingos.put(char_obj_id, bingo);
			return bingo.getDialog("");
		}
		else if (event.contains("choice-"))
		{
			int char_obj_id = player.getObjectId();
			if (!bingos.containsKey(char_obj_id))
			{
				return null;
			}
			Bingo bingo = bingos.get(char_obj_id);
			return bingo.Select(event.replaceFirst("choice-", ""));
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (st.getState() == State.CREATED)
		{
			if (player.getLevel() < 58)
			{
				st.exitQuest(true);
				return "30843-00.htm";
			}
			return "30843-01.htm";
		}
		return st.getQuestItemsCount(Stolen_Infernium_Ore) < Required_Stolen_Infernium_Ore ? "30843-04.htm" : "30843-05.htm";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		Integer _chance = dropchances.get(npc.getId());
		if (_chance != null)
		{
			st.dropQuestItems(Stolen_Infernium_Ore, 1, 10000000, _chance, true);
		}
		
		return null;
	}
	
	public static class Bingo extends L2Bingo
	{
		protected final static String msg_begin = "I've arranged the numbers 1 through 9 on the grid. Don't peek!<br>Let me have the 100 Infernium Ores. Too many players try to run away without paying when it becomes obvious that they're losing...<br>OK, select six numbers between 1 and 9. Choose the %choicenum% number.";
		protected final static String msg_again = "You've already chosen that number. Make your %choicenum% choice again.";
		protected final static String msg_0lines = "Wow! How unlucky can you get? Your choices are highlighted in red below. As you can see, your choices didn't make a single line! Losing this badly is actually quite rare!<br>You look so sad, I feel bad for you... Wait here... <br>.<br>.<br>.<br>Take this... I hope it will bring you better luck in the future.";
		protected final static String msg_3lines = "Excellent! As you can see, you've formed three lines! Congratulations! As promised, I'll give you some unclaimed merchandise from the warehouse. Wait here...<br>.<br>.<br>.<br>Whew, it's dusty! OK, here you go. Do you like it?";
		protected final static String msg_lose = "Oh, too bad. Your choices didn't form three lines. You should try again... Your choices are highlighted in red.";
		private final static String template_choice = "<a action=\"bypass -h Quest _386_StolenDignity choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ";
		private final QuestState _qs;
		
		public Bingo(QuestState qs)
		{
			super(template_choice);
			_qs = qs;
		}
		
		@Override
		protected String getFinal()
		{
			String result = super.getFinal();
			if (lines == 3)
			{
				reward(Rewards_Win);
			}
			else if (lines == 0)
			{
				reward(Rewards_Lose);
			}
			
			bingos.remove(_qs.getPlayer().getObjectId());
			return result;
		}
		
		private void reward(int[][] rew)
		{
			int[] r = rew[getRandom(rew.length)];
			_qs.giveItems(r[0], r[1], false);
		}
	}
	
	public static void main(String[] args)
	{
		new _386_StolenDignity(386, qn, "");
	}
}