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
import l2e.util.Rnd;

/**
 * Created by LordWinter 20.03.2011
 * Based on L2J Eternity-World
 */
public class _050_LanoscosSpecialBait extends Quest
{
	private static final String qn = "_050_LanoscosSpecialBait";

	// NPC
	int Lanosco = 31570;
	int SingingWind = 21026;

	// Items
	int EssenceofWind = 7621;
	int WindFishingLure = 7610;

	// Skill
	Integer FishSkill = 1315;

	public _050_LanoscosSpecialBait(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Lanosco);
		addTalkId(Lanosco);
		addKillId(SingingWind);
		questItemIds = new int[] {EssenceofWind};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("31570-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("31570-06.htm"))
			if(st.getQuestItemsCount(EssenceofWind) < 100)
				htmltext = "31570-07.htm";
			else
			{
				st.unset("cond");
				st.takeItems(EssenceofWind, -1);
				st.giveItems(WindFishingLure, 4);
				st.playSound("ItemSound.quest_finish");
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
		int id = st.getState();
		if(npcId == Lanosco)
			if(id == State.CREATED)
			{
				if(player.getLevel() < 27)
				{
					htmltext = "31570-02a.htm";
					st.exitQuest(true);
				}
				else if(player.getSkillLevel(FishSkill) >= 8)
					htmltext = "31570-01.htm";
				else
				{
					htmltext = "31570-02.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1 || cond == 2)
				if(st.getQuestItemsCount(EssenceofWind) < 100)
				{
					htmltext = "31570-05.htm";
					st.set("cond", "1");
				}
				else
					htmltext = "31570-04.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null) 
			return null;

		int npcId = npc.getId();

		if(npcId == SingingWind && st.getInt("cond") == 1)
		{
			if(st.getQuestItemsCount(EssenceofWind) < 100 && Rnd.getChance(30))
			{
				st.giveItems(EssenceofWind, 1);
				if(st.getQuestItemsCount(EssenceofWind) == 100)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "2");
				}
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	public static void main(String[] args)
	{
		new _050_LanoscosSpecialBait(50, qn, "");
	}
}