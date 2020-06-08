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

public class _276_TotemOfTheHestui extends Quest
{
	private final static String qn = "_276_TotemOfTheHestui";
	
	// NPC
	private static final int TANAPI = 30571;
	
	// Items
	private static final int KASHA_PARASITE = 1480;
	private static final int KASHA_CRYSTAL = 1481;
	private static final int HESTUIS_TOTEM = 1500;
	private static final int LEATHER_PANTS = 29;
	
	public _276_TotemOfTheHestui(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(TANAPI);
		addTalkId(TANAPI);
		
		addKillId(20479, 27044);

		questItemIds = new int[] { KASHA_PARASITE, KASHA_CRYSTAL };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30571-03.htm"))
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
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getRace().ordinal() == 3)
				{
					if (player.getLevel() >= 15 && player.getLevel() <= 21)
						htmltext = "30571-02.htm";
					else
					{
						htmltext = "30571-01.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30571-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				if (st.getQuestItemsCount(KASHA_CRYSTAL) == 0)
					htmltext = "30571-04.htm";
				else
				{
					htmltext = "30571-05.htm";
					st.takeItems(KASHA_CRYSTAL, -1);
					st.takeItems(KASHA_PARASITE, -1);
					st.giveItems(HESTUIS_TOTEM, 1);
					st.giveItems(LEATHER_PANTS, 1);
					st.exitQuest(true);
					st.playSound("ItemSound.quest_finish");
					player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message4", player.getLang())).toString()), 3000));
				}
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
			switch (npc.getId())
			{
				case 20479:
					if (st.getInt("cond") == 1 && st.getQuestItemsCount(KASHA_CRYSTAL) == 0)
					{
						long count = st.getQuestItemsCount(KASHA_PARASITE);
						int random = getRandom(100);
						
						if ((count >= 70 && random < 90) 
								|| (count >= 65 && random < 75)
								|| (count >= 60 && random < 60)
								|| (count >= 52 && random < 45)
								|| (count >= 50 && random < 30))
						{
							st.addSpawn(27044, npc);
							st.takeItems(KASHA_PARASITE, count);
						}
						else
						{
							st.playSound("ItemSound.quest_itemget");
							st.giveItems(KASHA_PARASITE, 1);
						}
					}
					break;
					
				case 27044:
					if (st.getInt("cond") == 1 && st.getQuestItemsCount(KASHA_CRYSTAL) == 0)
					{
						st.playSound("ItemSound.quest_middle");
						st.giveItems(KASHA_CRYSTAL, 1);
						st.set("cond", "2");
					}
					break;
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _276_TotemOfTheHestui(276, qn, "");
	}
}