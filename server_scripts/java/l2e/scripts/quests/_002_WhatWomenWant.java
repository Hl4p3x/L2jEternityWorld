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
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Created by LordWinter 21.03.2011
 * Based on L2J Eternity-World
 */
public class _002_WhatWomenWant extends Quest
{
	private static final String qn = "_002_WhatWomenWant";

	/* Arujien; Mirabel; Herbiel; Greenis */
	private final static int QUEST_NPC[] = { 30223, 30146, 30150, 30157 };

	/* Arujien's Letter; Arujien's Letter; Arujien's Letter; Poetry Book; Greenis's Letter */
	private final static int QUEST_ITEM[] = { 1092, 1093, 1094, 689, 693 };

	/* Adena; Mystic's Earring */
	private final static int QUEST_REWARD[] = { 57, 113 };

	public _002_WhatWomenWant(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(QUEST_NPC[0]);
		for (int npcId : QUEST_NPC)
		{
			addTalkId(npcId);
		}
		questItemIds = QUEST_ITEM;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{		
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30223-04.htm"))
		{
			st.giveItems(QUEST_ITEM[0], 1);
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30223-08.htm"))
		{
			st.takeItems(QUEST_ITEM[2], -1);
			st.giveItems(QUEST_ITEM[3], 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30223-10.htm"))
		{
			st.takeItems(QUEST_ITEM[2], -1);
			st.giveItems(QUEST_REWARD[0], 2300 * Math.round(Config.RATE_QUEST_REWARD_ADENA));
			st.addExpAndSp(4254, 335);
			st.set("cond", "0");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
			player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message1", player.getLang())).toString()), 3000));
			return null;
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		String htmltext = getNoQuestMsg(player);
		final int npcId = npc.getId();
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == QUEST_NPC[0])
				{
					if ((player.getRace().ordinal() == 1 || player.getRace().ordinal() == 0) && player.getLevel() >= 2)
						htmltext = "30223-02.htm";
					else
					{
						htmltext = "30223-01.htm";
						st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (npcId == QUEST_NPC[0])
				{
					switch (cond)
					{
						case 1:
							if (st.getQuestItemsCount(QUEST_ITEM[0]) > 0)
								htmltext = "30223-05.htm";
							break;
						case 2:
							if (st.getQuestItemsCount(QUEST_ITEM[1]) > 0)
								htmltext = "30223-06.htm";
							break;
						case 3:
							if (st.getQuestItemsCount(QUEST_ITEM[2]) > 0)
								htmltext = "30223-07.htm";
							break;
						case 4:
							if (st.getQuestItemsCount(QUEST_ITEM[3]) > 0)
								htmltext = "30223-11.htm";
							break;
						case 5:
							if (st.getQuestItemsCount(QUEST_ITEM[4]) > 0)
							{
								htmltext = "30223-10.htm";
								st.takeItems(QUEST_ITEM[4], -1);
								st.giveItems(QUEST_REWARD[0], 1850 * Math.round(Config.RATE_QUEST_REWARD_ADENA));
								st.giveItems(QUEST_REWARD[1], (long) Config.RATE_QUEST_REWARD);
								st.addExpAndSp(4254 * Math.round(Config.RATE_QUEST_REWARD_XP), 335 * Math.round(Config.RATE_QUEST_REWARD_SP));
								st.set("cond", "0");
								st.exitQuest(false);
								st.playSound("ItemSound.quest_finish");
							}
							break;
					}
				}
				else if (npcId == QUEST_NPC[1])
				{
					switch (cond)
					{
						case 1:
					         if (st.getQuestItemsCount(QUEST_ITEM[0]) > 0)
				        	 {
									htmltext = "30146-01.htm";
									st.takeItems(QUEST_ITEM[0], -1);
									st.giveItems(QUEST_ITEM[1], 1);
									st.set("cond", "2");
									st.playSound("ItemSound.quest_middle");
				        	 }
					         break;
						case 2:
					         if (st.getQuestItemsCount(QUEST_ITEM[1]) > 0)
					         	htmltext = "30146-02.htm";
					         break;
					}
				}
				else if (npcId == QUEST_NPC[2])
				{
					switch (cond)
					{
						case 2:
					         if (st.getQuestItemsCount(QUEST_ITEM[1]) > 0)
				        	 {
					        	 htmltext = "30150-01.htm";
					        	 st.takeItems(QUEST_ITEM[1], -1);
					        	 st.giveItems(QUEST_ITEM[2], 1);
					        	 st.set("cond", "3");
					        	 st.playSound("ItemSound.quest_middle");
				        	 }
					         break;
						case 3:
					         if (st.getQuestItemsCount(QUEST_ITEM[2]) > 0)
					        	 htmltext = "30150-02.htm";
					         break;
					}
				}
				else if (npcId == QUEST_NPC[3])
				{
					switch (cond)
					{
						case 4:
					         if (st.getQuestItemsCount(QUEST_ITEM[3]) > 0)
				        	 {
					        	 htmltext = "30157-01.htm";
					        	 st.takeItems(QUEST_ITEM[3], -1);
					        	 st.giveItems(QUEST_ITEM[4], 1);
					        	 st.set("cond", "5");
					        	 st.playSound("ItemSound.quest_middle");
				        	 }
					         break;
						case 5:
					         if (st.getQuestItemsCount(QUEST_ITEM[4]) > 0)
					        	 htmltext = "30157-02.htm";
					         break;
					}
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _002_WhatWomenWant(2, qn, "");
	}
}