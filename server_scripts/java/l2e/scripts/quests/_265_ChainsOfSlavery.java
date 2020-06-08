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
import l2e.util.Rnd;

public class _265_ChainsOfSlavery extends Quest
{
	private static final String qn = "_265_ChainsOfSlavery";
	
	// NPC
	private static final int KRISTIN = 30357;
	
	// MOBS
	private static final int IMP = 20004;
	private static final int IMP_ELDER = 20005;
	
	// ITEMS
	private static final int IMP_SHACKLES = 1368;
	
	// Newbie section
	private static final int NEWBIE_REWARD = 4;
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public _265_ChainsOfSlavery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(KRISTIN);
		addTalkId(KRISTIN);
		
		addKillId(IMP);
		addKillId(IMP_ELDER);
		
		questItemIds = new int[]
		{
			IMP_SHACKLES
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
		
		if (event.equalsIgnoreCase("30357-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30357-06.htm"))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}
		
		int cond = st.getInt("cond");
		int id = st.getState();
		
		if (id == State.CREATED)
		{
			st.set("cond", "0");
		}
		
		if (cond == 0)
		{
			if (player.getRace().ordinal() != 2)
			{
				htmltext = "30357-00.htm";
				st.exitQuest(true);
			}
			else
			{
				if (player.getLevel() < 6)
				{
					htmltext = "30357-01.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "30357-02.htm";
				}
			}
		}
		else
		{
			long count = st.getQuestItemsCount(IMP_SHACKLES);
			
			if (count > 0)
			{
				if (count >= 10)
				{
					st.giveItems(57, (12 * count) + 500);
				}
				else
				{
					st.giveItems(57, 12 * count);
				}
				
				st.takeItems(IMP_SHACKLES, -1);
				
				int newbie = player.getNewbie();
				
				if ((newbie | NEWBIE_REWARD) != newbie)
				{
					player.setNewbie(newbie | NEWBIE_REWARD);
					st.showQuestionMark(26);
					
					if (player.getClassId().isMage())
					{
						st.playTutorialVoice("tutorial_voice_027");
						st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000);
						player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2", player.getLang())).toString()), 3000));
					}
					else
					{
						st.playTutorialVoice("tutorial_voice_026");
						st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
						player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2a", player.getLang())).toString()), 3000));
					}
				}
				htmltext = "30357-05.htm";
			}
			else
			{
				htmltext = "30357-04.htm";
			}
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
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		if ((cond == 1) && Rnd.getChance((5 + npcId) - 20004))
		{
			st.giveItems(IMP_SHACKLES, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _265_ChainsOfSlavery(265, qn, "");
	}
}