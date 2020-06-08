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
package l2e.scripts.village_master;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 28.12.2012
 * Based on L2J Eternity-World
 */
public class ElvenHumanBuffers2 extends Quest
{
	private static final String qn = "ElvenHumanBuffers2";
	
	// NPCs
	private static int[] NPCS =
	{
		30120, 30191, 30857, 30905, 31276, 31321, 31279, 31755, 31968, 32095, 31336
	};
	
	// Items
	private static int MARK_OF_PILGRIM     = 2721;
	private static int MARK_OF_TRUST       = 2734;
	private static int MARK_OF_HEALER      = 2820;
	private static int MARK_OF_REFORMER    = 2821;
	private static int MARK_OF_LIFE        = 3140;
	
	private static int[][] CLASSES = 
	{
		{ 30, 29, 12, 13, 14, 15, MARK_OF_PILGRIM, MARK_OF_LIFE, MARK_OF_HEALER },
		{ 16, 15, 16, 17, 18, 19, MARK_OF_PILGRIM, MARK_OF_TRUST, MARK_OF_HEALER },
		{ 17, 15, 20, 21, 22, 23, MARK_OF_PILGRIM, MARK_OF_TRUST, MARK_OF_REFORMER }
	};
	
	public ElvenHumanBuffers2(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPCS);
		addTalkId(NPCS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return getNoQuestMsg(player);
		
		if (Util.isDigit(event))
		{
			int i = Integer.valueOf(event);
			final ClassId cid = player.getClassId();
			if ((cid.getRace() == Race.Elf || cid.getRace() == Race.Human) && (cid.getId() == CLASSES[i][1]))
			{
				int suffix;
				final boolean item1 = st.hasQuestItems(CLASSES[i][6]);
				final boolean item2 = st.hasQuestItems(CLASSES[i][7]);
				final boolean item3 = st.hasQuestItems(CLASSES[i][8]);
				if (player.getLevel() < 40)
				{
					suffix = (!item1 || !item2 || !item3) ? CLASSES[i][2] : CLASSES[i][3];
				}
				else
				{
					if (!item1 || !item2 || !item3)
					{
						suffix = CLASSES[i][4];
					}
					else
					{
						suffix = CLASSES[i][5];
						st.takeItems(CLASSES[i][6], -1);
						st.takeItems(CLASSES[i][7], -1);
						st.takeItems(CLASSES[i][8], -1);
						st.playSound("ItemSound.quest_fanfare_2");
						player.setClassId(CLASSES[i][0]);
						player.setBaseClass(CLASSES[i][0]);
						player.broadcastUserInfo();
						st.exitQuest(false);
					}
				}
				event = "30120-" + suffix + ".htm";
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);

		if (player.isSubClassActive())
			return htmltext;
		
		final ClassId cid = player.getClassId();
		if (cid.getRace() == Race.Elf || cid.getRace() == Race.Human)
		{
			switch (cid)
			{
				case oracle:
				{
					htmltext = "30120-01.htm";
					break;
				}
				case cleric:
				{
					htmltext = "30120-05.htm";
					break;
				}
				default:
				{
					if (cid.level() == 0)
					{
						htmltext = "30120-24.htm";
					}
					else if (cid.level() >= 2)
					{
						htmltext = "30120-25.htm";
					}
					else
					{
						htmltext = "30120-26.htm";
					}	
				}
			}
		}
		else
		{
			htmltext = "30120-26.htm";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new ElvenHumanBuffers2(-1, qn, "village_master");
	}
}