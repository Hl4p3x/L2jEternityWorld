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
 * Created by LordWinter 02.03.2011
 * Based on L2J Eternity-World
 */
public class _645_GhostsOfBatur extends Quest
{
	private static String qn = "_645_GhostsOfBatur";

	// NPC
	private static final int KARUDA = 32017;

	// Item
	private static final int CURSED_BURIAL = 14861;

	// MOBS
	private static final int[] MOBS = { 22703, 22704, 22705, 22706 };

	// Rewards
        private static final int[] REWARDS = { 9967, 9968, 9969, 9970, 9971, 9972, 9973, 9974, 9975, 10544, 10545 };
    
    // Chance (100% = 1000)
	private static final int DROP_CHANCE = 400;


	public _645_GhostsOfBatur(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		
		for (final int i : MOBS)
			addKillId(i);	
		
		questItemIds = new int[] { CURSED_BURIAL };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32017-03.htm"))
		{
			if (player.getLevel() < 80)
			{
				htmltext = "32017-02.htm";
				st.exitQuest(true);
			}
			else
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		
		else if (event.equalsIgnoreCase("32017-06.htm"))
		{
			if (player.getLevel() < 80)
			{
				htmltext = "32017-02.htm";
				st.exitQuest(true);
			}
			else
			{
				htmltext = "32017-06.htm";
			}
		}
		
		else if (event.equalsIgnoreCase("REWARDS"))
		{
			if (st.getQuestItemsCount(CURSED_BURIAL) >= 500)
			{
				st.takeItems(CURSED_BURIAL, 500);
				st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)], 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "32017-05c.htm";
			}
			else
			{
				htmltext = "32017-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("LEO"))
		{
			if (st.getQuestItemsCount(CURSED_BURIAL) >= 8)
			{
				st.takeItems(CURSED_BURIAL, 8);
				st.rewardItems(9628, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "32017-05c.htm";
			}
			else
			{
				htmltext = "32017-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("ADA"))
		{
			if (st.getQuestItemsCount(CURSED_BURIAL) >= 15)
			{
				st.takeItems(CURSED_BURIAL, 15);
				st.rewardItems(9629, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "32017-05c.htm";
			}
			else
			{
				htmltext = "32017-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("ORI"))
		{
			if (st.getQuestItemsCount(CURSED_BURIAL) >= 12)
			{
				st.takeItems(CURSED_BURIAL, 12);
				st.rewardItems(9630, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "32017-05c.htm";
			}
			else
			{
				htmltext = "32017-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("32017-08.htm"))
		{
			st.takeItems(CURSED_BURIAL,-1);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED :
				htmltext = "32017-01.htm";
				break;
			case State.STARTED :
				switch (st.getInt("cond"))
				{
					case 0:
						htmltext = "32017-04.htm";
						break;
					case 1:
						if (st.getQuestItemsCount(CURSED_BURIAL) > 0)
							htmltext = "32017-05b.htm";
						else
							htmltext = "32017-05a.htm";
						break;
					default:
						htmltext = "32017-02.htm";
						st.exitQuest(true);
						break;
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
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
			long count = st.getQuestItemsCount(CURSED_BURIAL);
			if(cond == 1)
			{
				int chance = (int) (DROP_CHANCE * Config.RATE_QUEST_DROP);
				int numItems = (chance / 1000);
				chance = chance % 1000;
				if (getRandom(1000) < chance)
					numItems++;
				if (numItems > 0)
				{
					if ((count + numItems) / 500 > count / 500)
						st.playSound("ItemSound.quest_middle");
					else
						st.playSound("ItemSound.quest_itemget");
					st.giveItems(CURSED_BURIAL, numItems);
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _645_GhostsOfBatur(645, qn, "");
	}
}
