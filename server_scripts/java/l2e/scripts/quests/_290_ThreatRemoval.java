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
 * Created by Sigrlinne
 * Based on L2J Eternity-World
 */
public class _290_ThreatRemoval extends Quest
{
	private static final String qn = "_290_ThreatRemoval";

	//Npc
	private static final int PINAPS = 30201;

	//Mobs
	private static final int[] MOBS = { 22775, 22776, 22777, 22780, 22781, 22782, 22783, 22784  };

	//Items
	private static final int tag = 15714;

	// REWARDS
	private static final int REWARDS = 959;
	private static final int REWARDS2 = 960;
	private static final int REWARDS3 = 9552;

	// Chance (100% = 1000)
	private static final int DROP_CHANCE = 510;
	
	public _290_ThreatRemoval(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(PINAPS);
		addTalkId(PINAPS);

		for (int i : MOBS)
			addKillId(i);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
			
		long count = st.getQuestItemsCount(tag);

		long random = getRandom(3);
			
		if (event.equalsIgnoreCase("30201-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		
		if (event.equalsIgnoreCase("30201-06.htm"))
		{
			if (count >= 400)
			{
				if (random == 0)
				{
					st.takeItems(tag,400);
          				st.rewardItems(REWARDS,1);
					st.playSound("ItemSound.quest_middle");
				}
				if (random == 1)
				{
				        st.takeItems(tag,400);
          				st.rewardItems(REWARDS2,getRandom(2)+1);
          				st.playSound("ItemSound.quest_middle");
				}
				if (random == 2)
				{
				        st.takeItems(tag,400);
          				st.rewardItems(REWARDS3,getRandom(1)+1);
          				st.playSound("ItemSound.quest_middle");
				}
			}
		}
		
		if (event.equalsIgnoreCase("30201-07.htm"))
		{
			st.set("cond", "1");
			st.playSound("ItemSound.quest_middle");
		}
		
		if (event.equalsIgnoreCase("30201-09.htm"))
		{
			st.unset("cond");
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		int id = st.getState();
		int npcId = npc.getId();
		final int cond = st.getInt("cond");
		long count = st.getQuestItemsCount(tag);

		if(npcId == PINAPS)
		{
			if(id == State.CREATED && cond == 0)
			{
				QuestState _prev = player.getQuestState("_251_NoSecrets");
				if (_prev != null && _prev.getState() == State.COMPLETED && player.getLevel() >= 82)
					htmltext = "30201-02.htm";
				else
				{
						htmltext = "30201-01.htm";
						st.exitQuest(true);
				}
			}
			else if(id == State.STARTED && cond == 1 &&  count < 400)
			{
				htmltext = "30201-04.htm";
			}
			else if(id == State.STARTED && cond == 1 && count >= 400)
			{
				htmltext = "30201-05.htm";
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
			long count = st.getQuestItemsCount(tag);
			if(cond == 1)
			{
				int chance = (int) (DROP_CHANCE * Config.RATE_QUEST_DROP);
				int numItems = (chance / 1000);
				chance = chance % 1000;
				if (getRandom(1000) < chance)
					numItems++;
				if (numItems > 0)
				{
					if ((count + numItems) / 400 > count / 400)
					{
						st.giveItems(tag, numItems);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
						st.giveItems(tag, numItems);
					}
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _290_ThreatRemoval(290, qn, "");
	}
}	