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

import gnu.trove.map.hash.TIntIntHashMap;

import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _385_YokeOfThePast extends Quest
{
	private static final String qn = "_385_YokeOfThePast";
	
	// NPCs
	private static final int GATEKEEPER_ZIGGURAT[] =
	{
		31095,
		31096,
		31097,
		31098,
		31099,
		31100,
		31101,
		31102,
		31103,
		31104,
		31105,
		31106,
		31107,
		31108,
		31109,
		31110,
		31114,
		31115,
		31116,
		31117,
		31118,
		31119,
		31120,
		31121,
		31122,
		31123,
		31124,
		31125,
		31126
	};
	
	// Item
	private static final int ANCIENT_SCROLL = 5902;
	
	// Reward
	private static final int BLANK_SCROLL = 5965;
	
	private final TIntIntHashMap Chance = new TIntIntHashMap();
	{
		Chance.put(21208, 7);
		Chance.put(21209, 8);
		Chance.put(21210, 11);
		Chance.put(21211, 11);
		Chance.put(21213, 14);
		Chance.put(21214, 19);
		Chance.put(21215, 19);
		Chance.put(21217, 24);
		Chance.put(21218, 30);
		Chance.put(21219, 30);
		Chance.put(21221, 37);
		Chance.put(21222, 46);
		Chance.put(21223, 45);
		Chance.put(21224, 50);
		Chance.put(21225, 54);
		Chance.put(21226, 66);
		Chance.put(21227, 64);
		Chance.put(21228, 70);
		Chance.put(21229, 75);
		Chance.put(21230, 91);
		Chance.put(21231, 86);
		Chance.put(21236, 12);
		Chance.put(21237, 14);
		Chance.put(21238, 19);
		Chance.put(21239, 19);
		Chance.put(21240, 22);
		Chance.put(21241, 24);
		Chance.put(21242, 30);
		Chance.put(21243, 30);
		Chance.put(21244, 34);
		Chance.put(21245, 37);
		Chance.put(21246, 46);
		Chance.put(21247, 45);
		Chance.put(21248, 50);
		Chance.put(21249, 54);
		Chance.put(21250, 99);
		Chance.put(21251, 64);
		Chance.put(21252, 70);
		Chance.put(21253, 75);
		Chance.put(21254, 91);
		Chance.put(21255, 86);
	}
	
	public _385_YokeOfThePast(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int ziggurat : GATEKEEPER_ZIGGURAT)
		{
			addStartNpc(ziggurat);
			addTalkId(ziggurat);
		}
		
		addKillId(21208, 21209, 21210, 21211, 21213, 21214, 21215, 21217, 21218, 21219, 21221, 21223, 21224, 21225, 21226, 21227, 21228, 21229, 21230, 21231, 21236, 21237, 21238, 21239, 21240, 21241, 21242, 21243, 21244, 21245, 21246, 21247, 21248, 21249, 21250, 21251, 21252, 21253, 21254, 21255);

		questItemIds = new int[]
		{
			ANCIENT_SCROLL
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("10.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 20 && player.getLevel() <= 75)
					htmltext = "01.htm";
				else
				{
					htmltext = "02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(ANCIENT_SCROLL) == 0)
					htmltext = "08.htm";
				else
				{
					htmltext = "09.htm";
					long count = st.getQuestItemsCount(ANCIENT_SCROLL);
					st.takeItems(ANCIENT_SCROLL, -1);
					st.rewardItems(BLANK_SCROLL, count);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		int p = Chance.get(npc.getId());
		int chance = (int) (p * Config.RATE_QUEST_DROP);
		int numItems = chance / 100;
		chance = chance % 100;
		
		if (st.getRandom(100) < chance)
			numItems++;
		
		if (numItems > 0)
		{
			st.giveItems(ANCIENT_SCROLL, numItems);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _385_YokeOfThePast(385, qn, "");		
	}
}