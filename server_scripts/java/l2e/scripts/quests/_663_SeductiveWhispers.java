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

import l2e.Config;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 22.01.2013 Based on L2J Eternity-World
 */
public class _663_SeductiveWhispers extends Quest
{
	private static final String qn = "_663_SeductiveWhispers";
	
	private final static int WILBERT = 30846;
	
	private final static int[] MOBS =
	{
		20674,
		20678,
		20954,
		20955,
		20956,
		20957,
		20958,
		20959,
		20960,
		20961,
		20962,
		20974,
		20975,
		20976,
		20996,
		20997,
		20998,
		20999,
		21001,
		21002,
		21006,
		21007,
		21008,
		21009,
		21010
	};
	
	private final static int BEAD = 8766;
	
	private final static int EWD = 955;
	private final static int EWC = 951;
	private final static int EWB = 947;
	private final static int EAB = 948;
	private final static int EWA = 729;
	private final static int EAA = 730;
	
	private final static int[] RECIPES =
	{
		4963,
		4966,
		4967,
		4968,
		5001,
		5003,
		5004,
		5005,
		5006,
		5007
	};
	
	private final static int[] INGREDIENTS =
	{
		4101,
		4107,
		4108,
		4109,
		4115,
		4117,
		4118,
		4119,
		4120,
		4121
	};
	
	public _663_SeductiveWhispers(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(WILBERT);
		addTalkId(WILBERT);
		
		addKillId(MOBS);
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
		
		long BEAD_COUNT = st.getQuestItemsCount(BEAD);
		
		if (event.equalsIgnoreCase("30846-04.htm"))
		{
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.set("cond", "1");
			st.set("round", "0");
		}
		else if (event.equalsIgnoreCase("30846-09.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30846-08.htm"))
		{
			if (BEAD_COUNT < 1)
			{
				htmltext = "30846-11.htm";
			}
			else
			{
				st.takeItems(BEAD, 1);
				
				int random = getRandom(100);
				if (random < 68)
				{
					htmltext = "30846-08.htm";
				}
				else
				{
					htmltext = "30846-08a.htm";
				}
			}
		}
		else if (event.equalsIgnoreCase("30846-10.htm"))
		{
			st.set("round", "0");
			if (BEAD_COUNT < 50)
			{
				htmltext = "30846-11.htm";
			}
		}
		else if (event.equalsIgnoreCase("30846-12.htm"))
		{
			int round = st.getInt("round");
			if (round == 0)
			{
				if (BEAD_COUNT < 50)
				{
					htmltext = "30846-11.htm";
				}
				st.takeItems(BEAD, 50);
			}
			if (getRandom(100) > 68)
			{
				st.set("round", "0");
				htmltext = "30846-12.htm";
			}
			else
			{
				int next_round = round + 1;
				htmltext = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.TEXT1") + " <font color=\"LEVEL\">NROUND</font> " + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.TEXT5") + "<br><font color=\"LEVEL\">MYPRIZE</font><br><br><a action=\"bypass -h Quest _663_SeductiveWhispers 30846-12.htm\">" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.TEXT2") + "</a><br><a action=\"bypass -h Quest _663_SeductiveWhispers 30846-13.htm\">" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.TEXT3") + "</a><br><a action=\"bypass -h Quest _663_SeductiveWhispers 30846-09.htm\">" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.TEXT4") + "</a></body></html>";
				htmltext = htmltext.replace("NROUND", String.valueOf(next_round));
				if (next_round == 1)
				{
					htmltext = htmltext.replace("MYPRIZE", "" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.1_ROUND") + "");
				}
				if (next_round == 2)
				{
					htmltext = htmltext.replace("MYPRIZE", "" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.2_ROUND") + "");
				}
				if (next_round == 3)
				{
					htmltext = htmltext.replace("MYPRIZE", "" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.3_ROUND") + "");
				}
				if (next_round == 4)
				{
					htmltext = htmltext.replace("MYPRIZE", "" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.4_ROUND") + "");
				}
				if (next_round == 5)
				{
					htmltext = htmltext.replace("MYPRIZE", "" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.5_ROUND") + "");
				}
				if (next_round == 6)
				{
					htmltext = htmltext.replace("MYPRIZE", "" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.6_ROUND") + "");
				}
				if (next_round == 7)
				{
					htmltext = htmltext.replace("MYPRIZE", "" + LocalizationStorage.getInstance().getString(player.getLang(), "663quest.7_ROUND") + "");
				}
				if (next_round == 8)
				{
					next_round = 0;
					st.giveItems(57, 2384000);
					st.giveItems(EWA, 1);
					st.giveItems(EAA, 2);
					htmltext = "30846-12a.htm";
				}
				st.set("round", String.valueOf(next_round));
			}
		}
		else if (event.equalsIgnoreCase("30846-13.htm"))
		{
			int round = st.getInt("round");
			
			if (round == 0)
			{
				htmltext = "30846-13a.htm";
			}
			
			st.set("round", "0");
			htmltext = "30846-13.htm";
			
			if (round == 1)
			{
				st.giveItems(57, 40000);
			}
			else if (round == 2)
			{
				st.giveItems(57, 80000);
			}
			else if (round == 3)
			{
				st.giveItems(57, 110000);
				st.giveItems(EWD, 1);
			}
			else if (round == 4)
			{
				st.giveItems(57, 199000);
				st.giveItems(EWC, 1);
			}
			else if (round == 5)
			{
				st.giveItems(57, 388000);
				st.giveItems(RECIPES[getRandom(RECIPES.length - 1)], 1);
			}
			else if (round == 6)
			{
				st.giveItems(57, 675000);
				st.giveItems(INGREDIENTS[getRandom(INGREDIENTS.length - 1)], 1);
			}
			else if (round == 7)
			{
				st.giveItems(57, 1284000);
				st.giveItems(EWB, 2);
				st.giveItems(EAB, 2);
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() < 50)
				{
					st.exitQuest(true);
					htmltext = "30846-00.htm";
				}
				else
				{
					htmltext = "30846-01.htm";
				}
				break;
			case State.STARTED:
				htmltext = "30846-03.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if ((st == null) || !st.isStarted())
		{
			return null;
		}
		
		int chance = (int) (800 * Config.RATE_QUEST_DROP);
		int numItems = (chance / 1000);
		chance = chance % 1000;
		if (getRandom(1000) < chance)
		{
			numItems += 1;
		}
		
		if (numItems > 0)
		{
			st.giveItems(BEAD, (numItems));
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _663_SeductiveWhispers(663, qn, "");
	}
}