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
public class _001_LettersOfLove extends Quest
{
	private static final String qn = "_001_LettersOfLove";

	/* Darin; Roxxy; Baulro */
	private final static int QUEST_NPC[] = { 30048, 30006, 30033 };

	/* Darin's Letter; Roxxy's Kerchief; Darin's Receipt; Baulro's Potion */
	private final static int QUEST_ITEM[] = { 687, 688, 1079, 1080 };

	/* Adena; Necklace of Knowledge */
	private final static int QUEST_REWARD[] = { 57, 906 };

	public _001_LettersOfLove(int questId, String name, String descr)
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
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30048-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(QUEST_ITEM[0], 1);
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
					if (player.getLevel() >= 2 && cond == 0)
						htmltext = "30048-02.htm";
					else
					{
						htmltext = "30048-01.htm";
						st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (npcId == QUEST_NPC[0])
				{
					switch (cond)
					{
						case 2:
							if (st.getQuestItemsCount(QUEST_ITEM[1]) > 0)
							{
								htmltext = "30048-07.htm";
								st.takeItems(QUEST_ITEM[1], -1);
								st.giveItems(QUEST_ITEM[2], 1);
								st.set("cond", "3");
								st.playSound("ItemSound.quest_middle");
							}
							break;
						case 3:
							if (st.getQuestItemsCount(QUEST_ITEM[2]) > 0)
								htmltext = "30048-08.htm";
							break;
						case 4:
							if (st.getQuestItemsCount(QUEST_ITEM[3]) > 0)
							{
								htmltext = "30048-09.htm";
								st.takeItems(QUEST_ITEM[3], -1);
								st.giveItems(QUEST_REWARD[0], 2466 * Math.round(Config.RATE_QUEST_REWARD_ADENA));
								st.giveItems(QUEST_REWARD[1], (long) Config.RATE_QUEST_REWARD);
								st.addExpAndSp(5672, 446);
								st.exitQuest(false);
								st.playSound("ItemSound.quest_finish");
								player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message1", player.getLang())).toString()), 3000));
							}
							break;
						default:
							htmltext = "30048-06.htm";
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
								htmltext = "30006-01.htm";
								st.takeItems(QUEST_ITEM[0], -1);
								st.giveItems(QUEST_ITEM[1], 1);
								st.set("cond", "2");
								st.playSound("ItemSound.quest_middle");
				        	 }
					         break;
						default:
							if (cond > 1)
								htmltext = "30006-02.htm";
							break;
					}
				}
				else if (npcId == QUEST_NPC[2])
				{
					switch (cond)
					{
						case 3:
					         if (st.getQuestItemsCount(QUEST_ITEM[2]) > 0)
				        	 {
					        	 htmltext = "30033-01.htm";
					        	 st.takeItems(QUEST_ITEM[2], -1);
					        	 st.giveItems(QUEST_ITEM[3], 1);
					        	 st.set("cond", "4");
					        	 st.playSound("ItemSound.quest_middle");
				        	 }
					         break;
						default:
							if (cond > 3)
								htmltext = "30033-02.htm";
							break;
					}
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _001_LettersOfLove(1, qn, "");
	}
}
