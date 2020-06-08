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
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.SocialAction;

/**
 * Created by LordWinter 06.08.2011
 * Based on L2J Eternity-World
 */
public class _062_PathoftheTrooper extends Quest
{
	private static final String qn = "_062_PathoftheTrooper";

	// NPC's
	private static final int Shubain 		  	= 32194;
	private static final int Gwain 				= 32197;

	// MONSTERS
	private static final int FelimLizardmanWarrior 		= 20014;
	private static final int VenomousSpider 		= 20038;
	private static final int TumranBugbear 			= 20062;

	// QUEST ITEMS
	private static final int FelimHead 			= 9749;
	private static final int VenomousSpiderLeg 		= 9750;
	private static final int TumranBugbearHeart 		= 9751;
	private static final int ShubainsRecommendation 	= 9752;
	private static final int GwainsRecommendation 		= 9753;

	public _062_PathoftheTrooper(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Gwain);
		addTalkId(Gwain);
		addTalkId(Shubain);

		addKillId(FelimLizardmanWarrior);
		addKillId(VenomousSpider);
		addKillId(TumranBugbear);

		questItemIds = new int[] { FelimHead, VenomousSpiderLeg, ShubainsRecommendation, TumranBugbearHeart };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("32197-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("32194-02.htm"))
			st.set("cond", "2");
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
		byte id = st.getState();
		int cond = st.getInt("cond");

		if (id == State.COMPLETED)
			htmltext = "32197-07.htm";

		if(npcId == Gwain)
		{
			if (id == State.CREATED)
			{
				if(player.getClassId() != ClassId.maleSoldier)
				{
					htmltext = "32197-00b.htm";
					st.exitQuest(false);
				}
				else if(player.getLevel() < 18)
				{
					htmltext = "32197-00a.htm";
					st.exitQuest(false);
				}
				else
					htmltext = "32197-01.htm";
			}
			else if(cond < 4)
			{
				htmltext = "32197-03.htm";
			}
			else if(cond == 4)
			{
				st.takeItems(ShubainsRecommendation, -1);
				st.set("cond", "5");
				htmltext = "32197-04.htm";
			}
			else if(cond == 5)
			{
				if (st.getQuestItemsCount(TumranBugbearHeart) < 1)
					htmltext = "32197-05.htm";
				else
				{
					st.takeItems(TumranBugbearHeart, -1);
					st.giveItems(GwainsRecommendation, 1);
					String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
					if (isFinished.equals(""))
					{
						st.giveItems(57, 163800);
						st.addExpAndSp(8064,2368);
						st.saveGlobalQuestVar("1ClassQuestFinished","1");
					}
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
					player.sendPacket(new SocialAction(player.getObjectId(),3));
					htmltext = "32197-06.htm";
				}
			}
		}
		else if(npcId == Shubain)
		{
			if(cond == 1)
				htmltext = "32194-01.htm";
			else if(cond == 2)
			{
				if (st.getQuestItemsCount(FelimHead) < 5)
					htmltext = "32194-03.htm";
				else
				{
					st.takeItems(FelimHead, -1);
					st.set("cond", "3");
					htmltext = "32194-04.htm";
				}
			}
			else if(cond == 3)
			{
				if (st.getQuestItemsCount(VenomousSpiderLeg) < 10)
					htmltext = "32194-05.htm";
				else
				{
					st.takeItems(VenomousSpiderLeg, -1);
					st.giveItems(ShubainsRecommendation, 1);
					st.set("cond", "4");
					htmltext = "32194-06.htm";
				}
			}
			else if (cond > 3)
				htmltext = "32194-07.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
        	if (st == null)
			return null;

		int id = npc.getId();
		int cond = st.getInt("cond");

		if(id == FelimLizardmanWarrior && cond == 2)
		{
			long count = st.getQuestItemsCount(FelimHead);
			if(count < 5)
			{
				st.giveItems(FelimHead, 1);
				if(count == 4)
					st.playSound("ItemSound.quest_middle");
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}

		if(id == VenomousSpider && cond == 3)
		{
			long count = st.getQuestItemsCount(VenomousSpiderLeg);
			if(count < 10)
			{
				st.giveItems(VenomousSpiderLeg, 1);
				if(count == 9)
					st.playSound("ItemSound.quest_middle");
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}

		if(id == TumranBugbear && cond == 5)
		{
			if(st.getQuestItemsCount(TumranBugbearHeart) == 0)
			{
				st.giveItems(TumranBugbearHeart, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _062_PathoftheTrooper(62, qn, "");
	}
}