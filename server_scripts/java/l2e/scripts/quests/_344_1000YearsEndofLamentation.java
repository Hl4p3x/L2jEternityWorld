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
 * Created by LordWinter 20.01.2013 Based on L2J Eternity-World
 */
public class _344_1000YearsEndofLamentation extends Quest
{
	private static final String qn = "_344_1000YearsEndofLamentation";

	private static final int DEAD_HEROES = 4269;
	private static final int OLD_KEY = 4270;
	private static final int OLD_HILT = 4271;
	private static final int OLD_TOTEM = 4272;
	private static final int CRUCIFIX = 4273;

	private static final int GILMORE = 30754;
	private static final int RODEMAI = 30756;
	private static final int ORVEN = 30857;
	private static final int KAIEN = 30623;
	private static final int GARVARENTZ = 30704;

	public _344_1000YearsEndofLamentation(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(GILMORE);
		addTalkId(GILMORE);
		addTalkId(RODEMAI);
		addTalkId(ORVEN);
		addTalkId(GARVARENTZ);
		addTalkId(KAIEN);

		for(int mob = 20236; mob < 20241; mob++)
		{
			addKillId(mob);
		}

		questItemIds = new int[]
		{
			DEAD_HEROES,
			OLD_KEY,
			OLD_HILT,
			OLD_TOTEM,
			CRUCIFIX
		};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		long amount = st.getQuestItemsCount(DEAD_HEROES);
		int cond = st.getInt("cond");
		int level = player.getLevel();

		if(event.equalsIgnoreCase("30754-04.htm"))
		{
			if(level >= 48 && cond == 0)
			{
          			st.setState(State.STARTED);
          			st.set("cond","1");
          			st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = getNoQuestMsg(player);
				st.exitQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("30754-08.htm"))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		else if(event.equalsIgnoreCase("30754-06.htm") && cond == 1)
		{
			if(amount == 0)
			{
				htmltext = "30754-06a.htm";
			}
			else
			{
				st.giveItems(57, amount * 60);
				st.takeItems(DEAD_HEROES, -1);
				int random = getRandom(1000);

          			if (random < 10)
				{
             				htmltext = "30754-12.htm";
             				st.giveItems(OLD_KEY,1);
             				st.set("cond","2");
				}
          			else if (random < 20)
				{
             				htmltext = "30754-13.htm";
             				st.giveItems(OLD_HILT,1);
             				st.set("cond","2");
				}
          			else if (random < 30)
				{
             				htmltext = "30754-14.htm";
             				st.giveItems(OLD_TOTEM,1);
             				st.set("cond","2");
				}
          			else if (random < 40)
				{
             				htmltext = "30754-15.htm";
             				st.giveItems(CRUCIFIX,1);
             				st.set("cond","2");
				}
          			else
				{
             				htmltext = "30754-16.htm";
             				st.set("cond","1");
				}
			}
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
		long amount = st.getQuestItemsCount(DEAD_HEROES);

		switch (st.getState())
		{
			case State.CREATED:
				if(player.getLevel() >= 48)
					htmltext = "30754-02.htm";
				else
				{
					htmltext = "30754-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if(npcId == GILMORE && cond == 1)
				{
					if(amount > 0)
					{
						htmltext = "30754-05.htm";
					}
					else
					{
						htmltext = "30754-04.htm";
					}
				}
				else if(cond == 2)
				{
					if(npcId == GILMORE)
					{
						htmltext = "30754-15.htm";
					}
					else if(rewards(st, npcId))
					{
						htmltext = npcId + "-01.htm";
						st.set("cond","1");
						st.unset("mission");
						st.playSound("ItemSound.quest_middle");
					}
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
    		{
      			return null;
    		}

		if(st.getInt("cond") == 1)
		{
    		 	int chance = (36 + (npc.getId() - 20234) * 2);
     			if (getRandom(100) < chance)
			{
         			st.giveItems(DEAD_HEROES, 1);
         			st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}

	private boolean rewards(QuestState st, int npcId)
	{
		boolean state = false;
		int chance = getRandom(100);
		if(npcId == ORVEN && st.getQuestItemsCount(CRUCIFIX) > 0)
		{
			st.set("mission", "1");
			st.takeItems(CRUCIFIX, -1);
			state = true;
			if(chance < 50)
			{
				st.giveItems(1875, 19);
			}
			else if(chance < 70)
			{
				st.giveItems(952, 5);
			}
			else
			{
				st.giveItems(2437, 1);
			}
		}
		else if(npcId == GARVARENTZ && st.getQuestItemsCount(OLD_TOTEM) > 0)
		{
			st.set("mission", "2");
			st.takeItems(OLD_TOTEM, -1);
			state = true;
			if(chance < 45)
			{
				st.giveItems(1882, 70);
			}
			else if(chance < 95)
			{
				st.giveItems(1881, 50);
			}
			else
			{
				st.giveItems(191, 1);
			}
		}
		else if(npcId == KAIEN && st.getQuestItemsCount(OLD_HILT) > 0)
		{
			st.set("mission", "3");
			st.takeItems(OLD_HILT, -1);
			state = true;
			if(chance < 50)
			{
				st.giveItems(1874, 25);
			}
			else if(chance < 75)
			{
				st.giveItems(1887, 10);
			}
			else if(chance < 99)
			{
				st.giveItems(951, 1);
			}
			else
			{
				st.giveItems(133, 1);
			}
		}
		else if(npcId == RODEMAI && st.getQuestItemsCount(OLD_KEY) > 0)
		{
			st.set("mission", "4");
			st.takeItems(OLD_KEY, -1);
			state = true;
			if(chance < 40)
			{
				st.giveItems(1879, 55);
			}
			else if(chance < 90)
			{
				st.giveItems(951, 1);
			}
			else
			{
				st.giveItems(885, 1);
			}
		}
		return state;
	}
	
	public static void main(String[] args)
	{
		new _344_1000YearsEndofLamentation(344, qn, "");
	}
}