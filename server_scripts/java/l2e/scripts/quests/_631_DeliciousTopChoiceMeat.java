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
 * Created by LordWinter 21.02.2011
 * Based on L2J Eternity-World
 */
public class _631_DeliciousTopChoiceMeat extends Quest
{
	private static final String qn = "_631_DeliciousTopChoiceMeat";

	// Npc
	private static final int TUNATUN = 31537;

	// Chance (100% = 1000)
	private static final int DROP_CHANCE = 530;

	// Items
	private static final int TORCH = 15534;

	// Mobs
	private static final int[] MOBS = { 18878, 1885, 18899, 18892, 18879, 18886, 18900, 18893 };

	// REWARDS
	private static final int[] REWARDS  = { 10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381 };
	private static final int[] REWARDS2 = { 10397, 10398, 10399, 10400, 10401, 10402, 10403, 10404, 10405 };
	private static final int[] REWARDS3 = { 15482, 15483 };

	public _631_DeliciousTopChoiceMeat(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(TUNATUN);

		addTalkId(TUNATUN);

		for (int i : MOBS)
			addKillId(i);
		
		questItemIds = new int[] { TORCH };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;

		long count = st.getQuestItemsCount(TORCH);

		long random = getRandom(3);
		
		if (event.equalsIgnoreCase("31537-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31537-05.htm"))
		{
			if (count >= 120)
			{
				if (random == 0)
				{
					st.takeItems(TORCH,120);
          				st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)], 1);
					st.playSound("ItemSound.quest_middle");
				}
				if (random == 1)
				{
				        st.takeItems(TORCH,120);
					st.rewardItems(REWARDS2[getRandom(REWARDS2.length - 1)],getRandom(8)+1);
          				st.playSound("ItemSound.quest_middle");
				}
				if (random == 2)
				{
				        st.takeItems(TORCH,120);
					st.rewardItems(REWARDS3[getRandom(REWARDS3.length - 1)],getRandom(1)+1);
          				st.playSound("ItemSound.quest_middle");
				}
				htmltext = "31537-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("31537-08.htm"))
		{
			st.takeItems(TORCH,-1);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		int id = st.getState();
		final int cond = st.getInt("cond");
		int npcId = npc.getId();
		long count = st.getQuestItemsCount(TORCH);

		if(npcId == TUNATUN)
		{
			if(id == State.CREATED && cond == 0)
			{
				if (player.getLevel() < 82)
				{
					htmltext = "31537-02.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "31537-01.htm";
				}
	
			}
			else if(id == State.STARTED && cond == 1)
			{
				if (count < 120)
					htmltext = "31537-05.htm";
				else 
					htmltext = "31537-04.htm";
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
			return null;

		QuestState st = partyMember.getQuestState(qn);
		if (st == null)
			return null;

		int id = st.getState();
		final int cond = st.getInt("cond");

		if(id == State.STARTED)
		{
			long count = st.getQuestItemsCount(TORCH);
			if(cond == 1)
			{
				int chance = (int) (DROP_CHANCE * Config.RATE_QUEST_DROP);
				int numItems = (chance / 1000);
				chance = chance % 1000;
				if (getRandom(1000) < chance)
					numItems++;
				if (numItems > 0)
				{
					if ((count + numItems) / 120 > count / 120)
						st.playSound("ItemSound.quest_middle");
					else
						st.playSound("ItemSound.quest_itemget");
					st.giveItems(TORCH, numItems);
				}
			}
		}
		return null;
	}
		
	public static void main(String[] args)
	{
		new _631_DeliciousTopChoiceMeat(631, qn, "");
	}
}