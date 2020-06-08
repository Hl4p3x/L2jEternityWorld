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

public class _174_SupplyCheck extends Quest
{
	private static final String qn = "_174_SupplyCheck";

	// NPC
	private static final int Marcela = 32173;
	private static final int Benis = 32170;
	private static final int Nika = 32167;

	// QuestItem
	private static final int WarehouseManifest = 9792;
	private static final int GroceryStoreManifest = 9793;

	private static final int WoodenBreastplate = 23;
	private static final int WoodenGaiters = 2386;
	private static final int LeatherTunic = 429;
	private static final int LeatherStockings = 464;
	private static final int WoodenHelmet = 43;
	private static final int LeatherShoes = 37;
	private static final int Gloves = 49;

	public _174_SupplyCheck(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Marcela);

		addTalkId(Marcela);
		addTalkId(Benis);
		addTalkId(Nika);

		questItemIds = new int [] { WarehouseManifest, GroceryStoreManifest };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("32173-03.htm"))
		{
			st.set("cond","1");
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

		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int id = st.getState();

		if(id == State.COMPLETED)
			htmltext = getAlreadyCompletedMsg(player);

		else if(id == State.CREATED && npcId == Marcela)
		{
			if(player.getLevel() >= 2)
				htmltext = "32173-01.htm";
			else
			{
				htmltext = "32173-02.htm";
				st.exitQuest(true);
			}
		}
		else if(id == State.STARTED)
		{
			if(npcId == Marcela)
			{
				if(cond == 1)
					htmltext = "32173-04.htm";
				else if(cond == 2)
				{
					st.set("cond","3");
					st.takeItems(WarehouseManifest, -1);
					st.playSound("ItemSound.quest_middle");
					htmltext = "32173-05.htm";
				}
				else if(cond == 3)
					htmltext = "32173-06.htm";
				else if(cond == 4)
				{
					if(player.getClassId().isMage())
					{
						st.giveItems(LeatherTunic, 1);
						st.giveItems(LeatherStockings, 1);
					}
					else
					{
						st.giveItems(WoodenBreastplate, 1);
						st.giveItems(WoodenGaiters, 1);
					}
					st.giveItems(WoodenHelmet, 1);
					st.giveItems(LeatherShoes, 1);
					st.giveItems(Gloves, 1);
					st.giveItems(57,2466);
					st.addExpAndSp(5672,446);
					player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message1", player.getLang())).toString()), 3000));
					st.exitQuest(false);
					htmltext = "32173-07.htm";
				}
			}
			else if(npcId == Benis)
			{
				if(cond == 1)
				{
					st.set("cond","2");
					st.giveItems(WarehouseManifest, 1);
					st.playSound("ItemSound.quest_middle");
					htmltext = "32170-01.htm";
				}
				else if(cond == 2)
					htmltext = "32170-02.htm";
			}
			else if(npcId == Nika)
			{
				if(cond == 3)
				{
					st.set("cond","4");
					st.giveItems(GroceryStoreManifest, 1);
					st.playSound("ItemSound.quest_middle");
					htmltext = "32167-01.htm";
				}
				else if(cond == 4)
					htmltext = "32167-02.htm";
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _174_SupplyCheck(174, qn, "");
	}
}