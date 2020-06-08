
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
 * Created by LordWinter 02.03.2011
 * Based on L2J Eternity-World
 */
public class _003_WillTheSealBeBroken extends Quest

{

	private static final String qn = "_003_WillTheSealBeBroken";
	
	// NPC
	private final static int TALLOTH = 30141;


	// MOBS
	private final static int[] MONSTERS = { 20031, 20041, 20046, 20048, 20052, 20057 };



	// Quest Items
	private final static int ONYX_BEAST_EYE = 1081;

	private final static int TAINT_STONE = 1082;

	private final static int SUCCUBUS_BLOOD = 1083;

	private final static int SCROLL_ENCHANT_ARMOR_D = 956;



	public _003_WillTheSealBeBroken(int questId, String name, String descr)

	{
		
		super(questId, name, descr);


		addStartNpc(TALLOTH);

		addTalkId(TALLOTH);

		for (int mobId : MONSTERS)

			addKillId(mobId);


		questItemIds = new int[]{ ONYX_BEAST_EYE, TAINT_STONE, SUCCUBUS_BLOOD };
	
	}


	
	@Override

	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)

	{

		String htmltext = event;

		final QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;



		if(event.equalsIgnoreCase("30141-03.htm"))

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

		if (npc.getId() == TALLOTH)

			return "";

		QuestState st = player.getQuestState(qn);


		if (st == null)

			st = newQuestState(player);

			String htmltext = getNoQuestMsg(player);
			final int cond = st.getInt("cond");


			switch (st.getState())

			{

				case State.COMPLETED:

					htmltext = getAlreadyCompletedMsg(player);
				break;

				case State.CREATED:

					if (player.getRace().ordinal() != 2)

					{

						htmltext = "30141-00.htm";

						st.exitQuest(true);

					}

					else if (player.getLevel() >= 16)

					{

						htmltext = "30141-02.htm";

					}

					else

					{

						htmltext = "30141-01.htm";

						st.exitQuest(true);

					}

					break;

				case State.STARTED:

					switch (cond)

					{

						case 2:

							if (st.getQuestItemsCount(ONYX_BEAST_EYE) > 0 && st.getQuestItemsCount(TAINT_STONE) > 0 && st.getQuestItemsCount(SUCCUBUS_BLOOD) > 0)

							{

								htmltext = "30141-06.htm";

								st.takeItems(ONYX_BEAST_EYE, 1);

								st.takeItems(TAINT_STONE, 1);

								st.takeItems(SUCCUBUS_BLOOD, 1);

								st.giveItems(SCROLL_ENCHANT_ARMOR_D, 1);

								st.playSound("ItemSound.quest_finish");

								st.unset("cond");
								st.setState(State.COMPLETED);

								st.exitQuest(false);

							}

							else

								htmltext = "30141-04.htm";

								break;

					}

					break;

			}

			return htmltext;

	}



	@Override

	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)

	{

		final QuestState st = player.getQuestState(qn);


		if (st == null)
			return null;


		final int npcId = npc.getId();


		if (st.getState() == State.STARTED && st.getInt("cond") == 1)

		{

			if (npcId == MONSTERS[0])

			{

				if (st.getQuestItemsCount(ONYX_BEAST_EYE) == 0)

				{

					st.giveItems(ONYX_BEAST_EYE, 1);

					st.playSound("Itemsound.quest_itemget");

				}

			}

			else if (npcId == MONSTERS[1] || npcId == MONSTERS[2])

			{

				if (st.getQuestItemsCount(TAINT_STONE) == 0)

				{

					st.giveItems(TAINT_STONE, 1);

					st.playSound("Itemsound.quest_itemget");

				}

			}

			else if (npcId == MONSTERS[3] || npcId == MONSTERS[4] || npcId == MONSTERS[5])

			{

				if (st.getQuestItemsCount(SUCCUBUS_BLOOD) == 0)
	
				{

					st.giveItems(SUCCUBUS_BLOOD, 1);

					st.playSound("Itemsound.quest_itemget");

				}

			}

			if(st.getQuestItemsCount(ONYX_BEAST_EYE) == 1 && st.getQuestItemsCount(TAINT_STONE) == 1 && st.getQuestItemsCount(SUCCUBUS_BLOOD) == 1)

			{

				st.set("cond", "2");

				st.playSound("ItemSound.quest_middle");

			}

		}

		return null;

	}


	public static void main(String[] args)

	{
		
		new _003_WillTheSealBeBroken(3, qn, "");

	}

}