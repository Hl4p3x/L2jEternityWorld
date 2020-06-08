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
public class _006_StepIntoTheFuture extends Quest
{
	private static final String qn = "_006_StepIntoTheFuture";

	// Roxxy; Baulro; Sir Collin Windawood
	private final static int QUEST_NPC[] = { 30006, 30033, 30311 };

	// Baulro's Letter
	private final static int QUEST_ITEM[] = { 7571 };

	// SoE: Giran; Mark of Traveler
	private final static int QUEST_REWARD[] = { 7559, 7570 };

	public _006_StepIntoTheFuture(int questId, String name, String descr)
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
		
		if (event.equalsIgnoreCase("30006-03.htm"))
		{
			qs.set("cond", "1");
			qs.setState(State.STARTED);
			qs.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30033-02.htm"))
		{
			qs.set("cond", "2");
			qs.giveItems(QUEST_ITEM[0], 1);
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30311-03.htm"))
		{
			qs.set("cond", "3");
			qs.takeItems(QUEST_ITEM[0], 1);
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30006-06.htm"))
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
		            		if (player.getRace().ordinal() == 0 && player.getLevel() >= 3)
		                		htmltext = "30006-02.htm";
		            		else
		            		{
		               	 		htmltext = "30006-01.htm";
		                		qs.exitQuest(true);
		            		}
				}
				break;
			case State.STARTED:
				switch (cond)
				{
					case 1:
						if (npcId == QUEST_NPC[0])
							htmltext = "30006-04.htm";
						else if (npcId == QUEST_NPC[1])
							htmltext = "30033-01.htm";
						break;
					case 2:
						if (npcId == QUEST_NPC[1])
						{
							if (qs.getQuestItemsCount(QUEST_ITEM[0]) > 0)
								htmltext = "30033-03.htm"; 
						}
						else if (npcId == QUEST_NPC[2])
						{
							if (qs.getQuestItemsCount(QUEST_ITEM[0]) > 0)
								htmltext = "30311-02.htm";
						}
						break;
					case 3:
						if (npcId == QUEST_NPC[0])
							htmltext = "30006-05.htm";
						break;
				}
				break;
		}
		return htmltext;	
	}

	public static void main(String[] args)
	{
		new _006_StepIntoTheFuture(6, qn, "");
	}
}