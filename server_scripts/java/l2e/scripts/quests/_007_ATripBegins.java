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
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 21.03.2011
 * Based on L2J Eternity-World
 */
public class _007_ATripBegins extends Quest
{
	private static final String qn = "_007_ATripBegins";

	// Mirabel; Ariel; Asterios
	private final static int QUEST_NPC[] = { 30146, 30148, 30154 };

	// Ariel's Recommendation
	private final static int QUEST_ITEM[] = { 7572 };

	// SoE: Giran; Mark of Traveler
	private final static int QUEST_REWARD[] = { 7559, 7570 };

	public _007_ATripBegins(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(QUEST_NPC[0]);
		for (int npcId : QUEST_NPC)
			addTalkId(npcId);
		questItemIds = QUEST_ITEM;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState qs = player.getQuestState(qn);
        	if (qs == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30146-03.htm"))
		{
			qs.set("cond", "1");
			qs.setState(State.STARTED);
			qs.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30148-02.htm"))
		{
			qs.set("cond", "2");
			qs.giveItems(QUEST_ITEM[0], 1);
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30154-02.htm"))
		{
			qs.set("cond", "3");
			qs.takeItems(QUEST_ITEM[0], 1);
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30146-06.htm"))
		{
			qs.giveItems(QUEST_REWARD[0], (long) Config.RATE_QUEST_REWARD);
			qs.giveItems(QUEST_REWARD[1], 1);
			qs.unset("cond");
			qs.exitQuest(false);
			qs.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(qn);
		if (qs == null)
			qs = newQuestState(player);
		String htmltext = getNoQuestMsg(player);		
		final int cond = qs.getInt("cond");
		final int npcId = npc.getId();
		
		switch (qs.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == QUEST_NPC[0])
				{
		            if (player.getRace().ordinal() == 1 && player.getLevel() >= 3)
		                htmltext = "30146-02.htm";
		            else
		            {
		                htmltext = "30146-01.htm";
		                qs.exitQuest(true);
		            }
				}
				break;
			case State.STARTED:
				switch (cond)
				{
					case 1:
						if (npcId == QUEST_NPC[0])
						{
							htmltext = "30146-04.htm";
						}
						else if (npcId == QUEST_NPC[1])
						{
							if (qs.getQuestItemsCount(QUEST_ITEM[0]) == 0)
								htmltext = "30148-01.htm";
						}
						break;
					case 2:
						if (npcId == QUEST_NPC[1])
						{
							htmltext = "30148-03.htm";
						}
						else if (npcId == QUEST_NPC[2])
						{
							if (qs.getQuestItemsCount(QUEST_ITEM[0]) == 0)
								htmltext = "30154-01.htm";
						}
						break;
					case 3:
						if (npcId == QUEST_NPC[0])
						{
							htmltext = "30146-05.htm";
						}
						else if (npcId == QUEST_NPC[2])
						{
							htmltext = "30154-03.htm";
						}
						break;
				}
				break;
		}
		return htmltext;	
	}

	public static void main(String[] args)
	{
		new _007_ATripBegins(7, qn, "");
	}
}
