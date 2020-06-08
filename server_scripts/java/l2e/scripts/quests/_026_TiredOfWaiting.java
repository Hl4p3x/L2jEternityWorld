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

public class _026_TiredOfWaiting extends Quest
{
	private static final String qn = "_026_TiredOfWaiting";
	
	// NPCs
	private static final int ISAEL_SILVERSHADOW = 30655;
	private static final int KITZKA = 31045;
	
	// Quest Item - Rewards
	private static final int DELIVERY_BOX = 17281;
	private static final int WILL_OF_ANTHARAS = 17266;
	private static final int LARGE_DRAGON_BONE = 17248;
	private static final int SEALED_BLOOD_CRYSTAL = 17267;
	
	public _026_TiredOfWaiting(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ISAEL_SILVERSHADOW);
		addTalkId(ISAEL_SILVERSHADOW, KITZKA);
		
		questItemIds = new int[] { DELIVERY_BOX };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		final int npcId = npc.getId();
		switch (npcId)
		{
			case ISAEL_SILVERSHADOW:
				if (event.equalsIgnoreCase("30655-04.html"))
				{
					st.setState(State.STARTED);
					st.set("cond", "1");
					st.giveItems(DELIVERY_BOX, 1);
					st.playSound("ItemSound.quest_accept");
				}
				break;
			case KITZKA:
				if (event.equalsIgnoreCase("31045-04.html"))
				{
					st.takeItems(DELIVERY_BOX, 1);
				}
				else if (event.equalsIgnoreCase("31045-10.html"))
				{
					if (st.getInt("cond") == 1)
					{
						st.giveItems(LARGE_DRAGON_BONE, 1);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
				}
				else if (event.equalsIgnoreCase("31045-11.html"))
				{
					if (st.getInt("cond") == 1)
					{
						st.giveItems(WILL_OF_ANTHARAS, 1);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
				}
				else if (event.equalsIgnoreCase("31045-12.html"))
				{
					if (st.getInt("cond") == 1)
					{
						st.giveItems(SEALED_BLOOD_CRYSTAL, 1);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		final int npcId = npc.getId();
		switch (st.getState())
		{
			case State.CREATED:
				if (npcId == ISAEL_SILVERSHADOW)
				{
					htmltext = (player.getLevel() >= 80) ? "30655-01.htm" : "30655-00.html";
				}
				break;
			case State.STARTED:
				if (st.getInt("cond") == 1)
				{
					switch (npcId)
					{
						case ISAEL_SILVERSHADOW:
							htmltext = "30655-07.html";
							break;
						case KITZKA:
							htmltext = (st.hasQuestItems(DELIVERY_BOX)) ? "31045-01.html" : "31045-09.html";
							break;
					}
				}
				break;
			case State.COMPLETED:
				if (npcId == ISAEL_SILVERSHADOW)
				{
					htmltext = "30655-08.html";
				}
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _026_TiredOfWaiting(26, qn, "");
	}
}