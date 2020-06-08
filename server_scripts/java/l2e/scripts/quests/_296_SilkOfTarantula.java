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

public class _296_SilkOfTarantula extends Quest
{
	private static final String qn = "_296_SilkOfTarantula";
	
	// QuestItem
	private static final int TARANTULA_SPIDER_SILK = 1493;
	private static final int TARANTULA_SPINNERETTE = 1494;
	private static final int RING_OF_RACCOON = 1508;
	private static final int RING_OF_FIREFLY = 1509;
	
	public _296_SilkOfTarantula(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(30519);
		addTalkId(30519);
		addTalkId(30548);
		
		addKillId(20394);
		addKillId(20403);
		addKillId(20508);
		
		questItemIds = new int[]
		{
			TARANTULA_SPIDER_SILK,
			TARANTULA_SPINNERETTE
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
		
		if (event.equalsIgnoreCase("30519-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30519-06.htm"))
		{
			st.takeItems(TARANTULA_SPINNERETTE, -1);
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		else if (event.equalsIgnoreCase("30548-02.htm"))
		{
			if (st.getQuestItemsCount(TARANTULA_SPINNERETTE) >= 1)
			{
				htmltext = "30548-03.htm";
				st.giveItems(TARANTULA_SPIDER_SILK, 17);
				st.takeItems(TARANTULA_SPINNERETTE, -1);
			}
		}
		else if (event.equalsIgnoreCase("30519-09.htm"))
		{
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
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int id = st.getState();
		int onlyone = st.getInt("onlyone");
		
		if ((npcId != 30519) && (id != State.STARTED))
		{
			return htmltext;
		}
		
		if (id == State.CREATED)
		{
			st.set("cond", "0");
		}
		
		if (npcId == 30519)
		{
			if (cond == 0)
			{
				if (player.getLevel() >= 15)
				{
					if ((st.getQuestItemsCount(RING_OF_RACCOON) > 0) || (st.getQuestItemsCount(RING_OF_FIREFLY) > 0))
					{
						htmltext = "30519-02.htm";
					}
					else
					{
						htmltext = "30519-08.htm";
						return htmltext;
					}
				}
				else
				{
					htmltext = "30519-01.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				if (st.getQuestItemsCount(TARANTULA_SPIDER_SILK) < 1)
				{
					htmltext = "30519-04.htm";
				}
				else if (st.getQuestItemsCount(TARANTULA_SPIDER_SILK) >= 1)
				{
					htmltext = "30519-05.htm";
					st.giveItems(57, st.getQuestItemsCount(TARANTULA_SPIDER_SILK) * 23);
					st.takeItems(TARANTULA_SPIDER_SILK, -1);
					
					if (onlyone == 0)
					{
						st.set("onlyone", "1");
						st.playTutorialVoice("tutorial_voice_026");
						player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message4", player.getLang())).toString()), 3000));
					}
				}
			}
		}
		else if ((npcId == 30548) && (cond == 1))
		{
			htmltext = "30548-01.htm";
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
		
		int n = getRandom(100);
		int cond = st.getInt("cond");
		
		if (cond == 1)
		{
			if (n > 95)
			{
				st.giveItems(TARANTULA_SPINNERETTE, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if (n > 45)
			{
				st.giveItems(TARANTULA_SPIDER_SILK, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _296_SilkOfTarantula(296, qn, "");
	}
}