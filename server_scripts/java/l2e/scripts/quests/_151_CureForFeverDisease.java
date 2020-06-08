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

public class _151_CureForFeverDisease extends Quest
{
	private final static String qn = "_151_CureForFeverDisease";
	
	// Items
	private static final int POISON_SAC = 703;
	private static final int FEVER_MEDICINE = 704;
	
	// NPCs
	private static final int ELIAS = 30050;
	private static final int YOHANES = 30032;
	
	public _151_CureForFeverDisease(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ELIAS);
		addTalkId(ELIAS, YOHANES);
		
		addKillId(20103, 20106, 20108);

		questItemIds = new int[] { FEVER_MEDICINE, POISON_SAC };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30050-03.htm"))
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
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 15 && player.getLevel() <= 21)
					htmltext = "30050-02.htm";
				else
				{
					htmltext = "30050-01.htm";
					st.exitQuest(true);
				}
				break;
				
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getId())
				{
					case ELIAS:
						if (cond == 1)
							htmltext = "30050-04.htm";
						else if (cond == 2)
							htmltext = "30050-05.htm";
						else if (cond == 3)
						{
							htmltext = "30050-06.htm";
							st.takeItems(FEVER_MEDICINE, 1);
							st.giveItems(102, 1);
							st.addExpAndSp(13106,613);
					        	st.exitQuest(false);
					        	st.playSound("ItemSound.quest_finish");
							player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message4", player.getLang())).toString()), 3000));
						}
						break;
						
					case YOHANES:
						if (cond == 2)
						{
							htmltext = "30032-01.htm";
							st.set("cond", "3");
					        	st.takeItems(POISON_SAC, 1);
					        	st.giveItems(FEVER_MEDICINE, 1);
					        	st.playSound("ItemSound.quest_middle");
						}
						else if (cond == 3)
							htmltext = "30032-02.htm";
						break;
				}
				break;
				
			case State.COMPLETED:
                		htmltext = Quest.getAlreadyCompletedMsg(player);
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
		
		if (st.getInt("cond") == 1 && getRandom(5) == 0)
		{
			st.set("cond", "2");
			st.giveItems(POISON_SAC, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _151_CureForFeverDisease(151, qn, "");
	}
}