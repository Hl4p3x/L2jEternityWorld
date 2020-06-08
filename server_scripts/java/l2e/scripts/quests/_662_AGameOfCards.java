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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2e.Config;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

public class _662_AGameOfCards extends Quest
{
	private static final String qn = "_662_AGameOfCards";
	
	private final static int KLUMP = 30845;
	
	private final static int[] mobs =
	{
		20677,
		21109,
		21112,
		21116,
		21114,
		21004,
		21002,
		21006,
		21008,
		21010,
		18001,
		20672,
		20673,
		20674,
		20955,
		20962,
		20961,
		20959,
		20958,
		20966,
		20965,
		20968,
		20973,
		20972,
		21278,
		21279,
		21280,
		21286,
		21287,
		21288,
		21520,
		21526,
		21530,
		21535,
		21508,
		21510,
		21513,
		21515
	};
	
	private final static int RED_GEM = 8765;
	
	private final static int Enchant_Weapon_S = 959;
	private final static int Enchant_Weapon_A = 729;
	private final static int Enchant_Weapon_B = 947;
	private final static int Enchant_Weapon_C = 951;
	private final static int Enchant_Weapon_D = 955;
	private final static int Enchant_Armor_D = 956;
	private final static int ZIGGOS_GEMSTONE = 8868;
	
	protected final static Map<Integer, CardGame> Games = new ConcurrentHashMap<>();
	
	public _662_AGameOfCards(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(KLUMP);
		addTalkId(KLUMP);
		
		addKillId(mobs);
		
		questItemIds = new int[]
		{
			RED_GEM
		};
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
		
		int _state = st.getState();
		
		if (event.equalsIgnoreCase("30845-02.htm") && (_state == State.CREATED))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30845-07.htm") && (_state == State.STARTED))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30845-03.htm") && (_state == State.STARTED) && (st.getQuestItemsCount(RED_GEM) >= 50))
		{
			htmltext = "30845-04.htm";
		}
		else if (event.equalsIgnoreCase("30845-10.htm") && (_state == State.STARTED))
		{
			if (st.getQuestItemsCount(RED_GEM) < 50)
			{
				htmltext = "30845-10a.htm";
			}
			st.takeItems(RED_GEM, 50);
			int player_id = player.getObjectId();
			if (Games.containsKey(player_id))
			{
				Games.remove(player_id);
			}
			Games.put(player_id, new CardGame(player_id));
		}
		else if (event.equalsIgnoreCase("play") && (_state == State.STARTED))
		{
			int player_id = player.getObjectId();
			if (!Games.containsKey(player_id))
			{
				return null;
			}
			return Games.get(player_id).playField(player);
		}
		else if (event.startsWith("card") && (_state == State.STARTED))
		{
			int player_id = player.getObjectId();
			if (!Games.containsKey(player_id))
			{
				return null;
			}
			try
			{
				int cardn = Integer.valueOf(event.replaceAll("card", ""));
				return Games.get(player_id).next(cardn, st, player);
			}
			catch (Exception E)
			{
				return null;
			}
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
		
		int _state = st.getState();
		
		if (_state == State.CREATED)
		{
			if (player.getLevel() < 61)
			{
				st.exitQuest(true);
				htmltext = "30845-00.htm";
			}
			else
			{
				htmltext = "30845-01.htm";
			}
		}
		else if (_state == State.STARTED)
		{
			return st.getQuestItemsCount(RED_GEM) < 50 ? "30845-03.htm" : "30845-04.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if (st.isStarted() && (st.getRandom(100) < 45))
		{
			st.giveItems(RED_GEM, 1 * Config.RATE_QUEST_DROP);
		}
		
		return null;
	}
	
	private static class CardGame
	{
		private final String[] cards = new String[5];
		private final int player_id;
		private final static String[] card_chars = new String[]
		{
			"A",
			"1",
			"2",
			"3",
			"4",
			"5",
			"6",
			"7",
			"8",
			"9",
			"10",
			"J",
			"Q",
			"K"
		};
		
		private final static String html_header = "<html><body>";
		private final static String html_footer = "</body></html>";
		private final static String table_header = "<table border=\"1\" cellpadding=\"3\"><tr>";
		private final static String table_footer = "</tr></table><br><br>";
		private final static String td_begin = "<center><td width=\"50\" align=\"center\"><br><br><br> ";
		private final static String td_end = " <br><br><br><br></td></center>";
		
		public CardGame(int _player_id)
		{
			player_id = _player_id;
			for (int i = 0; i < cards.length; i++)
			{
				cards[i] = "<a action=\"bypass -h Quest _662_AGameOfCards card" + i + "\">?</a>";
			}
		}
		
		public String next(int cardn, QuestState st, L2PcInstance player)
		{
			if ((cardn >= cards.length) || !cards[cardn].startsWith("<a"))
			{
				return null;
			}
			cards[cardn] = card_chars[getRandom(card_chars.length)];
			for (String card : cards)
			{
				if (card.startsWith("<a"))
				{
					return playField(player);
				}
			}
			return finish(st, player);
		}
		
		private String finish(QuestState st, L2PcInstance player)
		{
			String result = html_header + table_header;
			Map<String, Integer> matches = new HashMap<>();
			for (String card : cards)
			{
				int count = matches.containsKey(card) ? matches.remove(card) : 0;
				count++;
				matches.put(card, count);
			}
			for (String card : cards)
			{
				if (matches.get(card) < 2)
				{
					matches.remove(card);
				}
			}
			String[] smatches = matches.keySet().toArray(new String[matches.size()]);
			Integer[] cmatches = matches.values().toArray(new Integer[matches.size()]);
			String txt = "" + LocalizationStorage.getInstance().getString(player.getLang(), "662quest.NO_PAIRS") + "";
			if (cmatches.length == 1)
			{
				if (cmatches[0] == 5)
				{
					txt = "" + LocalizationStorage.getInstance().getString(player.getLang(), "662quest.5_PAIRS") + "";
					st.giveItems(ZIGGOS_GEMSTONE, 43);
					st.giveItems(Enchant_Weapon_S, 3);
					st.giveItems(Enchant_Weapon_A, 1);
				}
				else if (cmatches[0] == 4)
				{
					txt = "" + LocalizationStorage.getInstance().getString(player.getLang(), "662quest.4_PAIRS") + "";
					st.giveItems(Enchant_Weapon_S, 2);
					st.giveItems(Enchant_Weapon_C, 2);
				}
				else if (cmatches[0] == 3)
				{
					txt = "" + LocalizationStorage.getInstance().getString(player.getLang(), "662quest.3_PAIRS") + "";
					st.giveItems(Enchant_Weapon_C, 2);
				}
				else if (cmatches[0] == 2)
				{
					txt = "" + LocalizationStorage.getInstance().getString(player.getLang(), "662quest.1_PAIRS") + "";
					st.giveItems(Enchant_Armor_D, 2);
				}
			}
			else if (cmatches.length == 2)
			{
				if ((cmatches[0] == 3) || (cmatches[1] == 3))
				{
					txt = "" + LocalizationStorage.getInstance().getString(player.getLang(), "662quest.FULL_HOUSE") + "";
					st.giveItems(Enchant_Weapon_A, 1);
					st.giveItems(Enchant_Weapon_B, 2);
					st.giveItems(Enchant_Weapon_D, 1);
				}
				else
				{
					txt = "" + LocalizationStorage.getInstance().getString(player.getLang(), "662quest.2_PAIRS") + "";
					st.giveItems(Enchant_Weapon_C, 1);
				}
			}
			
			for (String card : cards)
			{
				if ((smatches.length > 0) && smatches[0].equalsIgnoreCase(card))
				{
					result += td_begin + "<font color=\"55FD44\">" + card + "</font>" + td_end;
				}
				else if ((smatches.length == 2) && smatches[1].equalsIgnoreCase(card))
				{
					result += td_begin + "<font color=\"FE6666\">" + card + "</font>" + td_end;
				}
				else
				{
					result += td_begin + card + td_end;
				}
			}
			
			result += table_footer + txt;
			if (st.getQuestItemsCount(RED_GEM) >= 50)
			{
				result += "<br><br><a action=\"bypass -h Quest _662_AGameOfCards 30845-10.htm\">" + LocalizationStorage.getInstance().getString(player.getLang(), "662quest.PLAY_AGAIN") + "</a>";
			}
			result += html_footer;
			Games.remove(player_id);
			return result;
		}
		
		public String playField(L2PcInstance player)
		{
			String result = html_header + table_header;
			for (String card : cards)
			{
				result += td_begin + card + td_end;
			}
			result += table_footer + "" + LocalizationStorage.getInstance().getString(player.getLang(), "662quest.NEXT_CARD") + "" + html_footer;
			return result;
		}
	}
	
	public static void main(String[] args)
	{
		new _662_AGameOfCards(662, qn, "");
	}
}