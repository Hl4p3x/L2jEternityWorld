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

import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

public class _169_NightmareChildren extends Quest
{
	private final static String qn = "_169_NightmareChildren";
	
	// Items
	private static final int CRACKED_SKULL = 1030;
	private static final int PERFECT_SKULL = 1031;
	private static final int BONE_GAITERS = 31;
	
	// NPC
	private static final int VLASTY = 30145;
	
	public _169_NightmareChildren(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(VLASTY);
		addTalkId(VLASTY);
		
		addKillId(20105, 20025);

		questItemIds = new int[] { CRACKED_SKULL, PERFECT_SKULL };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30145-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30145-08.htm"))
		{
			long reward = 17000 + (st.getQuestItemsCount(CRACKED_SKULL) * 20);
			st.takeItems(PERFECT_SKULL, -1);
			st.takeItems(CRACKED_SKULL, -1);
			st.giveItems(BONE_GAITERS, 1);
			st.rewardItems(57, reward);
			st.addExpAndSp(17475,818);
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
			player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message4", player.getLang())).toString()), 3000));
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getRace().ordinal() == 2)
					htmltext = "30145-00.htm";
				if (player.getLevel() >= 15 && player.getLevel() <= 20)
					htmltext = "30145-03.htm";
				else
				{
					htmltext = "30145-02.htm";
					st.exitQuest(true);
				}
				break;
				
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.getQuestItemsCount(CRACKED_SKULL) >= 1)
						htmltext = "30145-06.htm";
					else
						htmltext = "30145-05.htm";
				}
				else if (cond == 2)
					htmltext = "30145-07.htm";
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
			return null;
		
		if (st.isStarted())
		{
			int chance = getRandom(10);
			if (st.getInt("cond") == 1 && chance == 0)
			{
				st.set("cond", "2");
				st.giveItems(PERFECT_SKULL, 1);
				st.playSound("ItemSound.quest_middle");
			}
			else if (chance > 6)
			{
				st.giveItems(CRACKED_SKULL, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _169_NightmareChildren(169, qn, "");
	}
}