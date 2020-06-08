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
import l2e.util.Rnd;

/**
 * Created by LordWinter 09.08.2011
 * Based on L2J Eternity-World
 */
public class _125_TheNameOfEvil1 extends Quest
{
    	private static final String qn = "_125_TheNameOfEvil1";

	// NPC's
	private final int Mushika 	= 32114;
	private final int Karakawei 	= 32117;
	private final int UluKaimu 	= 32119;
	private final int BaluKaimu 	= 32120;
	private final int ChutaKaimu 	= 32121;

	// QUEST ITEMS
	private final int GAZKHFRAG 	= 8782;
	private final int EPITAPH 	= 8781;
	private final int OrClaw 	= 8779;
	private final int DienBone 	= 8780;

	public _125_TheNameOfEvil1(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(Mushika);
		addTalkId(Karakawei);
		addTalkId(UluKaimu);
		addTalkId(BaluKaimu);
		addTalkId(ChutaKaimu);

		addKillId(22200);
		addKillId(22201);
		addKillId(22202);
		addKillId(22203);
		addKillId(22204);
		addKillId(22205);
		addKillId(22219);
		addKillId(22220);
		addKillId(22224);
		addKillId(22224);

		questItemIds = new int[] { OrClaw, DienBone };
	}

	private String getWordText32119(QuestState st)
	{
		String htmltext = "32119-04.htm";
		if(st.getInt("T32119") > 0 && st.getInt("E32119") > 0 && st.getInt("P32119") > 0 && st.getInt("U32119") > 0)
			htmltext = "32119-09.htm";
		return htmltext;
	}

	private String getWordText32120(QuestState st)
	{
		String htmltext = "32120-04.htm";
		if(st.getInt("T32120") > 0 && st.getInt("O32120") > 0 && st.getInt("O32120_2") > 0 && st.getInt("N32120") > 0)
			htmltext = "32120-09.htm";
		return htmltext;
	}

	private String getWordText32121(QuestState st)
	{
		String htmltext = "32121-04.htm";
		if(st.getInt("W32121") > 0 && st.getInt("A32121") > 0 && st.getInt("G32121") > 0 && st.getInt("U32121") > 0)
			htmltext = "32121-09.htm";
		return htmltext;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32114-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("32114-12.htm"))
		{
			st.giveItems(GAZKHFRAG, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32114-13.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32117-08.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32117-16.htm"))
		{
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32119-20.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32120-19.htm"))
		{
			st.set("cond", "7");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32121-23.htm"))
		{
			st.giveItems(EPITAPH, 1);
			st.set("cond", "8");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("T32119"))
		{
			htmltext = "32119-05.htm";
			if(st.getInt("T32119") < 1)
				st.set("T32119", "1");
		}
		else if(event.equalsIgnoreCase("E32119"))
		{
			htmltext = "32119-06.htm";
			if(st.getInt("E32119") < 1)
				st.set("E32119", "1");
		}
		else if(event.equalsIgnoreCase("P32119"))
		{
			htmltext = "32119-07.htm";
			if(st.getInt("P32119") < 1)
				st.set("P32119", "1");
		}
		else if(event.equalsIgnoreCase("U32119"))
		{
			if(st.getInt("U32119") < 1)
				st.set("U32119", "1");
			htmltext = getWordText32119(st);
		}
		else if(event.equalsIgnoreCase("T32120"))
		{
			htmltext = "32120-05.htm";
			if(st.getInt("T32120") < 1)
				st.set("T32120", "1");
		}
		else if(event.equalsIgnoreCase("O32120"))
		{
			htmltext = "32120-06.htm";
			if(st.getInt("O32120") < 1)
				st.set("O32120", "1");
		}
		else if(event.equalsIgnoreCase("O32120_2"))
		{
			htmltext = "32120-07.htm";
			if(st.getInt("O32120_2") < 1)
				st.set("O32120_2", "1");
		}
		else if(event.equalsIgnoreCase("N32120"))
		{
			if(st.getInt("N32120") < 1)
				st.set("N32120", "1");
			htmltext = getWordText32120(st);
		}
		else if(event.equalsIgnoreCase("W32121"))
		{
			htmltext = "32121-05.htm";
			if(st.getInt("W32121") < 1)
				st.set("W32121", "1");
		}
		else if(event.equalsIgnoreCase("A32121"))
		{
			htmltext = "32121-06.htm";
			if(st.getInt("A32121") < 1)
				st.set("A32121", "1");
		}
		else if(event.equalsIgnoreCase("G32121"))
		{
			htmltext = "32121-07.htm";
			if(st.getInt("G32121") < 1)
				st.set("G32121", "1");
		}
		else if(event.equalsIgnoreCase("U32121"))
		{
			if(st.getInt("U32121") < 1)
				st.set("U32121", "1");
			htmltext = getWordText32121(st);
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

		if(npcId == Mushika)
		{
			QuestState qs124 = player.getQuestState("_124_MeetingTheElroki");
			if(cond == 0)
			{
				if(qs124 != null && qs124.isCompleted())
				{
					htmltext = "32114-01.htm";
					st.exitQuest(true);
				}
				else if(player.getLevel() < 76)
				{
					htmltext = "32114-02.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "32114-04.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
				htmltext = "32114-10.htm";
			else if(cond > 1 && cond < 8)
				htmltext = "32114-14.htm";
			else if(cond == 8)
			{
				st.unset("T32119");
				st.unset("E32119");
				st.unset("P32119");
				st.unset("U32119");
				st.unset("T32120");
				st.unset("O32120");
				st.unset("O32120_2");
				st.unset("N32120");
				st.unset("W32121");
				st.unset("A32121");
				st.unset("G32121");
				st.unset("U32121");
				st.unset("cond");

				htmltext = "32114-15.htm";
				st.addExpAndSp(859195, 86603);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
		}
		else if(npcId == Karakawei)
		{
			if(cond == 1)
				htmltext = "32117-02.htm";
			else if(cond == 2)
				htmltext = "32117-01.htm";
			else if(cond == 3 && (st.getQuestItemsCount(OrClaw) < 2 || st.getQuestItemsCount(DienBone) < 2))
				htmltext = "32117-12.htm";
			else if(cond == 3 && (st.getQuestItemsCount(OrClaw) == 2 && st.getQuestItemsCount(DienBone) == 2))
			{
				htmltext = "32117-11.htm";
				st.takeItems(OrClaw, 2);
				st.takeItems(DienBone, 2);
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
			}
			else if(cond > 4 && cond < 8)
				htmltext = "32117-19.htm";
			else if(cond == 8)
				htmltext = "32117-20.htm";
		}
		else if(npcId == UluKaimu)
		{
			if(cond == 5)
				htmltext = "32119-01.htm";
			else if(cond < 5)
				htmltext = "32119-02.htm";
			else if(cond > 5)
				htmltext = "32119-03.htm";
		}
		else if(npcId == BaluKaimu)
		{
			if(cond == 6)
				htmltext = "32120-01.htm";
			else if(cond < 6)
				htmltext = "32120-02.htm";
			else if(cond > 6)
				htmltext = "32120-03.htm";
		}
		else if(npcId == ChutaKaimu)
		{
			if(cond == 7)
				htmltext = "32121-01.htm";
			else if(cond < 7)
				htmltext = "32121-02.htm";
			else if(cond > 7)
				htmltext = "32121-03.htm";
			else if(cond == 8)
				htmltext = "32121-24.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		int npcId = npc.getId();

		if(((npcId >= 22200 && npcId <= 22202) || npcId == 22219 || npcId == 22224) && st.getQuestItemsCount(OrClaw) < 2 && Rnd.calcChance(10 * Config.RATE_QUEST_DROP))
		{
			st.giveItems(OrClaw, 1);
			st.playSound("ItemSound.quest_middle");
		}
		if(((npcId >= 22203 && npcId <= 22205) || npcId == 22220 || npcId == 22225) && st.getQuestItemsCount(DienBone) < 2 && Rnd.calcChance(10 * Config.RATE_QUEST_DROP))
		{
			st.giveItems(DienBone, 1);
			st.playSound("ItemSound.quest_middle");
		}
		return null;
	}

    	public static void main(String[] args)
    	{
        	new  _125_TheNameOfEvil1(125, qn, "");
    	}
}