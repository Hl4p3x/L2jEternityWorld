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

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 06.08.2011
 * Based on L2J Eternity-World
 */
public class _131_BirdInACage extends Quest
{
	private static final String qn = "_131_BirdInACage";

	// NPC's
	private static final int KANIS 			= 32264;
	private static final int PARME 			= 32271;

	// MOBS
	private static final int GIFTBOX 		= 32342;

	// ITEMS
	private static final int[][] GIFTBOXITEMS 	= {{9692, 100, 2} , {9693, 50, 1}};
	private static final int KANIS_ECHO_CRY 	= 9783;
	private static final int PARMES_LETTER 		= 9784;

	// OTHER
	private static final int KISSOFEVA 		= 1073;

	public _131_BirdInACage(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(KANIS);
		addTalkId(KANIS);
		addTalkId(PARME);
		addKillId(GIFTBOX);
		addSpawnId(GIFTBOX);

		questItemIds = new int[] { KANIS_ECHO_CRY, PARMES_LETTER };
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		int cond = st.getInt("cond");

		if(event.equalsIgnoreCase("32264-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("32264-08.htm") && cond == 1)
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(KANIS_ECHO_CRY, 1);
		}
		else if(event.equalsIgnoreCase("32271-03.htm") && cond == 2)
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(PARMES_LETTER, 1);
			player.teleToLocation(143472 + getRandom(-100, 100), 191040 + getRandom(-100, 100), -3696);
		}
		else if(event.equalsIgnoreCase("32264-12.htm") && cond == 3)
		{
			st.playSound("ItemSound.quest_middle");
			st.takeItems(PARMES_LETTER, -1);
		}
		else if(event.equalsIgnoreCase("32264-13.htm") && cond == 3)
		{
			HellboundManager.getInstance().unlock();
			st.playSound("ItemSound.quest_finish");
			st.takeItems(KANIS_ECHO_CRY, -1);
			st.addExpAndSp(1304752, 25019);
			st.exitQuest(false);
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

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		else if(npcId == KANIS)
		{
			if (cond == 0)
			{
				if(player.getLevel() >= 78)
					htmltext = "32264-01.htm";
				else
				{
					htmltext = "32264-00.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
				htmltext = "32264-03.htm";
			else if (cond == 2)
				htmltext = "32264-08a.htm";
			else if (cond == 3)
			{
				if (st.getQuestItemsCount(PARMES_LETTER) > 0)
					htmltext = "32264-11.htm";
				else
					htmltext = "32264-12.htm";
			}
		}
		else if(npcId == PARME && cond == 2)
			htmltext = "32271-01.htm";

		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(qn);
        	if (st == null)
			return null;

		if (npc.getId() == GIFTBOX)
		{
			if (killer.getFirstEffect(KISSOFEVA) != null)
			{
				for (int[] GIFTBOXITEM : GIFTBOXITEMS)
				{
					if (getRandom(100) < GIFTBOXITEM[1])
						st.giveItems(GIFTBOXITEM[0], 1);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	public static void main(String[] args)
	{
		new _131_BirdInACage(131, qn, "");
	}
}