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
 * Created by LordWinter 19.02.2011
 * Based on L2J Eternity-World
 */
public class _278_HomeSecurity extends Quest
{
	private static final String qn = "_278_HomeSecurity";

	// Npc
	private static final int TUNATUN = 31537;

	// Chance (100% = 1000)
	private static final int DROP_CHANCE = 510;

	// Items
	private static final int TORCH = 15531;

	// Mobs
	private static final int[] MOBS = { 18906, 18907 };

	// REWARDS
	private static final int REWARDS = 959;
	private static final int REWARDS2 = 960;
	private static final int REWARDS3 = 9553;

	public _278_HomeSecurity(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(TUNATUN);

		addTalkId(TUNATUN);

		for (int i : MOBS)
			addKillId(i);
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
			if (count >= 300)
			{
				if (random == 0)
				{
					st.takeItems(TORCH,300);
          				st.rewardItems(REWARDS,1);
				}
				if (random == 1)
				{
				        st.takeItems(TORCH,300);
          				st.rewardItems(REWARDS2,getRandom(9)+1);
          				st.playSound("ItemSound.quest_middle");
				}
				if (random == 2)
				{
				        st.takeItems(TORCH,300);
          				st.rewardItems(REWARDS3,getRandom(1)+1);
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
				if (count < 300)
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
					if ((count + numItems) / 300 > count / 300)
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
		new _278_HomeSecurity(278, qn, "");
	}
}