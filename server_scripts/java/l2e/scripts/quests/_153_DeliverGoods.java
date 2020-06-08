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
 * Created by LordWinter 29.03.2011
 * Based on L2J Eternity-World
 */
public class _153_DeliverGoods extends Quest
{
	private static final String qn = "_153_DeliverGoods";
	
	// NPCs
	private static final int JacksonId	 = 30002;
	private static final int SilviaId	 = 30003;
	private static final int ArnoldId	 = 30041;
	private static final int RantId		 = 30054;

	//ITEMs
	private static final int DeliveryListId	 = 1012;
	private static final int HeavyWoodBoxId	 = 1013;
	private static final int ClothBundleId	 = 1014;
	private static final int ClayPotId	 = 1015;
	private static final int JacksonsReceipt = 1016;
	private static final int SilviasReceipt	 = 1017;
	private static final int RantsReceipt	 = 1018;
	
	//REWARDs
	private static final int SoulshotNoGradeId = 1835; //You get 3 Soulshots no grade.
	private static final int RingofKnowledgeId = 875;
	private static final int XpRewardAmount	   = 600;
	
	public _153_DeliverGoods(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[] { DeliveryListId, HeavyWoodBoxId, ClothBundleId, ClayPotId, JacksonsReceipt, SilviasReceipt, RantsReceipt };
		addStartNpc(ArnoldId);
		addTalkId(JacksonId);
		addTalkId(SilviaId);
		addTalkId(ArnoldId);
		addTalkId(RantId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if ((st != null) && (npc.getId() == ArnoldId))
		{
			if (event.equalsIgnoreCase("30041-02.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				st.giveItems(DeliveryListId, 1);
				st.giveItems(HeavyWoodBoxId, 1);
				st.giveItems(ClothBundleId, 1);
				st.giveItems(ClayPotId, 1);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st != null)
		{
			if (npc.getId() == ArnoldId)
			{
				switch (st.getState())
				{
					case State.CREATED:
						if (player.getLevel() >= 2)
						{
							htmltext = "30041-01.htm";
						}
						else
						{
							htmltext = "30041-00.htm";
						}
						break;
					case State.STARTED:
						if (st.getInt("cond") == 1)
						{
							htmltext = "30041-03.htm";
						}
						else if (st.getInt("cond") == 2)
						{
							htmltext = "30041-04.htm";
							st.takeItems(DeliveryListId, -1);
							st.takeItems(JacksonsReceipt, -1);
							st.takeItems(SilviasReceipt, -1);
							st.takeItems(RantsReceipt, -1);
							//On retail it gives 2 rings but one at the time.
							st.giveItems(RingofKnowledgeId, 1);
							st.giveItems(RingofKnowledgeId, 1);
							st.addExpAndSp(XpRewardAmount, 0);
							st.exitQuest(false);
						}
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
			}
			else
			{
				if (npc.getId() == JacksonId)
				{
					if (st.getQuestItemsCount(HeavyWoodBoxId) > 0)
					{
						htmltext = "30002-01.htm";
						st.takeItems(HeavyWoodBoxId, -1);
						st.giveItems(JacksonsReceipt, 1);
					}
					else
					{
						htmltext = "30002-02.htm";
					}
				}
				else if (npc.getId() == SilviaId)
				{
					if (st.getQuestItemsCount(ClothBundleId) > 0)
					{
						htmltext = "30003-01.htm";
						st.takeItems(ClothBundleId, -1);
						st.giveItems(SilviasReceipt, 1);
						st.giveItems(SoulshotNoGradeId, 3);
					}
					else
					{
						htmltext = "30003-02.htm";
					}
				}
				else if (npc.getId() == RantId)
				{
					if (st.getQuestItemsCount(ClayPotId) > 0)
					{
						htmltext = "30054-01.htm";
						st.takeItems(ClayPotId, -1);
						st.giveItems(RantsReceipt, 1);
					}
					else
					{
						htmltext = "30054-02.htm";
					}
				}
				
				if ((st.getInt("cond") == 1) && (st.getQuestItemsCount(JacksonsReceipt) > 0) && (st.getQuestItemsCount(SilviasReceipt) > 0) && (st.getQuestItemsCount(RantsReceipt) > 0))
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _153_DeliverGoods(153, qn, "");
	}
}