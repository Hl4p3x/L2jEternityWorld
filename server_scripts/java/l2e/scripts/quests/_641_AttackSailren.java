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
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Created by LordWinter 06.09.2011
 * Based on L2J Eternity-World
 */
public class _641_AttackSailren extends Quest
{
	public static String qn = "_641_AttackSailren"; 
	
	public static int _statue = 32109;

	public static int[] _mobs =
	{
		22196, 22197, 22198, 22218, 22223, 22199
	};
	
	public static int GAZKH_FRAGMENT = 8782;
	public static int GAZKH 	 = 8784;

	public static int DROP_CHANCE 	 = 400;
	
	public _641_AttackSailren(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_statue);
		addTalkId(_statue);

		for (int npcId : _mobs)
		{
			addKillId(npcId);
		}

		questItemIds = new int[] { GAZKH_FRAGMENT };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{	
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32109-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond","1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32109-05.htm"))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
			sm.addString("Shilen's Protection");
			player.sendPacket(sm);
			st.takeItems(GAZKH_FRAGMENT,-1);
			st.giveItems(GAZKH,1);
			st.set("cond","3");
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
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

		int cond = st.getInt("cond");

		switch (st.getState())
		{
			case State.CREATED:
				QuestState qs = player.getQuestState("_126_TheNameOfEvil2");
				if(qs != null && qs.isCompleted())
				{
					if (player.getLevel() >= 77)
					{
						htmltext = "32109-01.htm";
					}
					else
					{
						htmltext = "32109-01a.htm";
					}
				}
				htmltext = "32109-00.htm";
				break;
			case State.STARTED:
				if (cond == 1)
				{
					htmltext = "32109-03.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32109-04.htm";
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
			long count = st.getQuestItemsCount(GAZKH_FRAGMENT);
			if(cond == 1)
			{
				int chance = (int) (DROP_CHANCE * Config.RATE_QUEST_DROP);
				int numItems = (chance / 1000);
				chance = chance % 1000;
				if (getRandom(1000) < chance)
					numItems++;
				if (numItems > 0)
				{
					if ((count + numItems) / 30 > count / 30)
					{
						st.playSound("ItemSound.quest_middle");
						st.set("cond","2");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
						st.giveItems(GAZKH_FRAGMENT, numItems);
					}
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _641_AttackSailren(641, qn, "");
	}
}