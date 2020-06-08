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

/**
 * Created by LordWinter 28.06.2012 Based on L2J Eternity-World
 */
public class _162_CurseOfFortress extends Quest
{
	private static final String qn = "_162_CurseOfFortress";
	
	private static final int BONE_FRAGMENT = 1158;
	private static final int ELF_SKULL = 1159;
	private static final int BONE_SHIELD = 625;
	
	public _162_CurseOfFortress(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(30147);
		addTalkId(30147);
		
		addKillId(new int[]
		{
			20033,
			20345,
			20371,
			20463,
			20464,
			20504
		});
		
		questItemIds = new int[]
		{
			BONE_FRAGMENT,
			ELF_SKULL
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30147-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getRace().ordinal() == 2)
				{
					htmltext = "30147-00.htm";
					st.exitQuest(true);
				}
				else if ((player.getLevel() >= 12) && (player.getLevel() <= 21))
				{
					htmltext = "30147-02.htm";
				}
				else
				{
					htmltext = "30147-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if ((st.getQuestItemsCount(ELF_SKULL) < 3) && (st.getQuestItemsCount(BONE_FRAGMENT) < 10))
				{
					htmltext = "30147-05.htm";
				}
				else
				{
					htmltext = "30147-06.htm";
					st.takeItems(ELF_SKULL, -1);
					st.takeItems(BONE_FRAGMENT, -1);
					st.rewardItems(57, 24000);
					st.addExpAndSp(22652, 1004);
					st.giveItems(BONE_SHIELD, 1);
					st.unset("cond");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if ((st.getInt("cond") == 1) && (st.getRandom(4) == 1))
		{
			switch (npc.getId())
			{
				case 20463:
				case 20464:
				case 20504:
					if (st.getQuestItemsCount(BONE_FRAGMENT) < 10)
					{
						st.giveItems(BONE_FRAGMENT, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					break;
				case 20033:
				case 20345:
				case 20371:
					if (st.getQuestItemsCount(ELF_SKULL) < 3)
					{
						st.giveItems(ELF_SKULL, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					break;
			}
			
			if ((st.getQuestItemsCount(BONE_FRAGMENT) >= 10) && (st.getQuestItemsCount(ELF_SKULL) >= 3))
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "2");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _162_CurseOfFortress(162, qn, "");
	}
}