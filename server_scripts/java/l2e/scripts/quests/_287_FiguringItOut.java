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
public class _287_FiguringItOut extends Quest
{
	private static final String qn = "_287_FiguringItOut";

	// Npc
	private static final int RAKI  = 32742;

	// Chance (100% = 1000)
	private static final int DROP_CHANCE = 310;

	// Items
	private static final int TORCH = 15499;

	// Mobs
	private static final int[] MOBS = { 22774, 22772, 22770, 22771, 22769, 22773, 22768 };

	// REWARDS
	private static final int[] REWARDS = { 15779, 15782, 15776, 15785, 15788, 15812, 15813, 15814, 15649, 15652, 15646, 15655, 15658, 15772, 15773, 5774 };
	private static final int REWARDS2 = 10381;
	private static final int REWARDS3 = 10405;

	public _287_FiguringItOut(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(RAKI);

		addTalkId(RAKI);

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

		long random = getRandom(1000);
		
		if (event.equalsIgnoreCase("32742-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32742-05.htm"))
		{
			if (count >= 100)
			{
				st.takeItems(TORCH, 100);
				st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)], 1);
				htmltext = "32742-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("32742-09.htm"))
		{
			if (count >= 500)
			{
				st.takeItems(TORCH, 500);
				htmltext = "32742-07.htm";
				if (random < 100)
         				st.giveItems(REWARDS2,1);
       				else if (random < 400)
         				st.giveItems(REWARDS3,1);
       				else if (random < 600)
         				st.giveItems(REWARDS3,2);
       				else if (random < 710)
         				st.giveItems(REWARDS3,3);
       				else if (random < 830)
         				st.giveItems(REWARDS3,4);
       				else if (random < 950)
         				st.giveItems(REWARDS3,5);
       				else if (random < 1000)
         				st.giveItems(REWARDS3,6);
			}
			else
				htmltext = "32742-09.htm";
		}
		else if (event.equalsIgnoreCase("32742-08.htm"))
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

		if(npcId == RAKI)
		{
			if(id == State.CREATED && cond == 0)
			{
				if (player.getLevel() < 82)
				{
					htmltext = "32742-02.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "32742-01.htm";
				}
	
			}
			else if(id == State.STARTED && cond == 1)
			{
				if (count < 100)
					htmltext = "32742-05.htm";
				else 
					htmltext = "32742-04.htm";
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
					if ((count + numItems) / 100 > count / 100)
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
		new _287_FiguringItOut(287, qn, "");
	}
}