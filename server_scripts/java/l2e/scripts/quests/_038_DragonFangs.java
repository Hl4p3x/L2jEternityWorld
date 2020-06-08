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

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 05.08.2011
 * Based on L2J Eternity-World
 */
public class _038_DragonFangs extends Quest
{
	private static final String qn = "_038_DragonFangs";

	// NPC`s
	public final int ROHMER 		= 30344;
	public final int LUIS 			= 30386;
	public final int IRIS 			= 30034;

	// QUEST ITEM
	public final int FEATHER_ORNAMENT 	= 7173;
	public final int TOOTH_OF_TOTEM 	= 7174;
	public final int LETTER_OF_IRIS 	= 7176;
	public final int LETTER_OF_ROHMER 	= 7177;
	public final int TOOTH_OF_DRAGON 	= 7175;

	// MOBS
	public final int LANGK_LIZARDMAN_LIEUTENANT = 20357;
	public final int LANGK_LIZARDMAN_SENTINEL   = 21100;
	public final int LANGK_LIZARDMAN_LEADER     = 20356;
	public final int LANGK_LIZARDMAN_SHAMAN     = 21101;

	// CHANCE FOR DROP
	public final int CHANCE_FOR_QUEST_ITEMS     = 100;

	// REWARD
	public final int ADENA 			= 57;
	public final int BONE_HELMET 		= 45;
	public final int ASSAULT_BOOTS 		= 1125;
	public final int BLUE_BUCKSKIN_BOOTS 	= 1123;

	public _038_DragonFangs(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(LUIS);

		addTalkId(IRIS);
		addTalkId(ROHMER);

		addKillId(LANGK_LIZARDMAN_LEADER);
		addKillId(LANGK_LIZARDMAN_SHAMAN);
		addKillId(LANGK_LIZARDMAN_SENTINEL);
		addKillId(LANGK_LIZARDMAN_LIEUTENANT);

		questItemIds = new int[] { TOOTH_OF_TOTEM, LETTER_OF_IRIS, LETTER_OF_ROHMER, TOOTH_OF_DRAGON, FEATHER_ORNAMENT };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		int cond = st.getInt("cond");

		if(event.equalsIgnoreCase("30386-02.htm"))
		{
			if(cond == 0)
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		if(event.equalsIgnoreCase("30386-04.htm"))
		{
			if(cond == 2)
			{
				st.set("cond", "3");
				st.takeItems(FEATHER_ORNAMENT, 100);
				st.giveItems(TOOTH_OF_TOTEM, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		if(event.equalsIgnoreCase("30034-02a.htm"))
		{
			if(cond == 3)
			{
				st.set("cond", "4");
				st.takeItems(TOOTH_OF_TOTEM, 1);
				st.giveItems(LETTER_OF_IRIS, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		if(event.equalsIgnoreCase("30344-02a.htm"))
		{
			if(cond == 4)
			{
				st.set("cond", "5");
				st.takeItems(LETTER_OF_IRIS, 1);
				st.giveItems(LETTER_OF_ROHMER, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		if(event.equalsIgnoreCase("30034-04a.htm"))
		{
			if(cond == 5)
			{
				st.set("cond", "6");
				st.takeItems(LETTER_OF_ROHMER, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		if(event.equalsIgnoreCase("30034-06a.htm"))
		{
			if((cond == 7) & st.getQuestItemsCount(TOOTH_OF_DRAGON) == 50)
			{
				htmltext = "30034-06.htm";
				st.takeItems(TOOTH_OF_DRAGON, 50);
				int luck = getRandom(3);
				if(luck == 0)
				{
					st.giveItems(BLUE_BUCKSKIN_BOOTS, 1);
					st.giveItems(ADENA, 1500);
				}
				if(luck == 1)
				{
					st.giveItems(BONE_HELMET, 1);
					st.giveItems(ADENA, 5200);
				}
				if(luck == 2)
				{
					st.giveItems(ASSAULT_BOOTS, 1);
					st.giveItems(ADENA, 1500);
				}
				st.addExpAndSp(435117, 23977);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
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
			return htmltext;

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		if(npcId == LUIS && cond == 0)
		{
			if(player.getLevel() < 19)
			{
				htmltext = "30386-01a.htm";
				st.exitQuest(true);
			}
			else if(player.getLevel() >= 19)
				htmltext = "30386-01.htm";
		}

		if(npcId == LUIS && cond == 1)
			htmltext = "30386-02a.htm";

		if(npcId == LUIS && cond == 2 && st.getQuestItemsCount(FEATHER_ORNAMENT) == 100)
			htmltext = "30386-03.htm";

		if(npcId == LUIS && cond == 3)
			htmltext = "30386-03a.htm";

		if(npcId == IRIS && cond == 3 && st.getQuestItemsCount(TOOTH_OF_TOTEM) == 1)
			htmltext = "30034-01.htm";

		if(npcId == IRIS && cond == 4)
			htmltext = "30034-02b.htm";

		if(npcId == IRIS && cond == 5 && st.getQuestItemsCount(LETTER_OF_ROHMER) == 1)
			htmltext = "30034-03.htm";

		if(npcId == IRIS && cond == 6)
			htmltext = "30034-05a.htm";

		if(npcId == IRIS && cond == 7 && st.getQuestItemsCount(TOOTH_OF_DRAGON) == 50)
			htmltext = "30034-05.htm";

		if(npcId == ROHMER && cond == 4 && st.getQuestItemsCount(LETTER_OF_IRIS) == 1)
			htmltext = "30344-01.htm";

		if(npcId == ROHMER && cond == 5)
			htmltext = "30344-03.htm";

		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
        	if (st == null)
			return null;

		int npcId = npc.getId();
		boolean chance = getRandom(100) < CHANCE_FOR_QUEST_ITEMS;
		int cond = st.getInt("cond");

		if(npcId == 20357 || npcId == 21100)
		{
			if(cond == 1 && chance && st.getQuestItemsCount(FEATHER_ORNAMENT) < 100)
			{
				st.giveItems(FEATHER_ORNAMENT, 1);
				if(st.getQuestItemsCount(FEATHER_ORNAMENT) == 100)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "2");
				}
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}

		if(npcId == 20356 || npcId == 21101)
		{
			if(cond == 6 && chance && st.getQuestItemsCount(TOOTH_OF_DRAGON) < 50)
			{
				st.giveItems(TOOTH_OF_DRAGON, 1);
				if(st.getQuestItemsCount(TOOTH_OF_DRAGON) == 50)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "7");
				}
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	public static void main(String[] args)
	{
		new _038_DragonFangs(38, qn, "");
	}
}