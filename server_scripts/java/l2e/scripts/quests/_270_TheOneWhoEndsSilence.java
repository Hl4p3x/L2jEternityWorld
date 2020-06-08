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
 * Created by LordWinter 25.02.2011
 * Based on L2J Eternity-World
 */
public class _270_TheOneWhoEndsSilence extends Quest
{
	private static final String qn = "_270_TheOneWhoEndsSilence";

	// Npc
	private static final int GREMORY = 32757;

	// Chance (100% = 1000)
	private static final int DROP_CHANCE = 520;

	// Items
	private static final int TORCH = 15526;

	// Mobs
	private static final int[] MOBS = { 22790, 22791, 22793, 22789, 22797, 22795, 22794, 22796, 22800, 22798, 22799 };

	// REWARDS
	private static final int[] REWARDS  = { 10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381 };
	private static final int[] REWARDS2 = { 10398, 10399, 10400, 10401, 10402, 10403, 10404, 10405 };
	private static final int[] REWARDS3 = { 5595, 5594, 5593 };

	public _270_TheOneWhoEndsSilence(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(GREMORY);

		addTalkId(GREMORY);

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

		long random = getRandom(2);
		
		if (event.equalsIgnoreCase("32757-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32757-05.htm"))
		{
			if (count >= 100)
			{
				if (random == 0)
				{
					st.takeItems(TORCH,100);
          				st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)],1);
					st.playSound("ItemSound.quest_middle");
				}
				if (random == 1)
				{
				        st.takeItems(TORCH,100);
          				st.rewardItems(REWARDS3[getRandom(REWARDS3.length - 1)],1);
          				st.playSound("ItemSound.quest_middle");
				}
				htmltext = "32757-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("32757-09.htm"))
		{
			if (count >= 200)
			{
				st.takeItems(TORCH,200);
				st.giveItems(REWARDS[getRandom(REWARDS.length - 1)],1);
       				st.giveItems(REWARDS3[getRandom(REWARDS3.length - 1)],1);
       				st.playSound("ItemSound.quest_middle");
				htmltext = "32757-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("32757-10.htm"))
		{
			if (count >= 300)
			{
				st.takeItems(TORCH,300);
       				st.giveItems(REWARDS[getRandom(REWARDS.length - 1)],1);
       				st.giveItems(REWARDS2[getRandom(REWARDS2.length - 1)],1);
       				st.giveItems(REWARDS3[getRandom(REWARDS3.length - 1)],1);
       				st.playSound("ItemSound.quest_middle");
				htmltext = "32757-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("32757-11.htm"))
		{
			if (count >= 400)
			{
				if (random == 0)
				{
					st.takeItems(TORCH,400);
          				st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)],2);
    	  				st.rewardItems(REWARDS3[getRandom(REWARDS3.length - 1)],1);
          				st.playSound("ItemSound.quest_middle");
				}
				if (random == 1)
				{
         				st.takeItems(TORCH,400);
          				st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)],1);
    	  				st.rewardItems(REWARDS2[getRandom(REWARDS2.length - 1)],1);
    	  				st.rewardItems(REWARDS3[getRandom(REWARDS3.length - 1)],2);
          				st.playSound("ItemSound.quest_middle");
				}			
				htmltext = "32757-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("32757-12.htm"))
		{
			if (count >= 500)
			{		
       				st.takeItems(TORCH,500);
       				st.giveItems(REWARDS[getRandom(REWARDS.length - 1)],2);
       				st.giveItems(REWARDS2[getRandom(REWARDS2.length - 1)],1);
       				st.giveItems(REWARDS3[getRandom(REWARDS3.length - 1)],2);
       				st.playSound("ItemSound.quest_middle");
				htmltext = "32757-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("32757-08.htm"))
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

		if(npcId == GREMORY)
		{
			if(id == State.CREATED && cond == 0)
			{
				QuestState _prev = player.getQuestState("_10288_SecretMission");
				if (player.getLevel() >= 82)
				{
					if (_prev != null && _prev.getState() == State.COMPLETED)
						htmltext = "32757-01.htm";
					else
						htmltext = "32757-02a.htm";
				}
				else
					htmltext = "32757-02.htm";
			}
			else if(id == State.STARTED && cond == 1)
			{
				if (count >= 100)
					htmltext = "32757-04.htm";
				else 
					htmltext = "32757-05.htm";
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
		new _270_TheOneWhoEndsSilence(270, qn, "");
	}
}