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
public class ElvenHumanMystics1 extends Quest
{
	private static final String qn = "ElvenHumanMystics1";
	
	// NPCs
	private static int[] NPCS =
	{
		30070, 30289, 30037, 32153, 32147
	};
	
	// Items
	private static int MARK_OF_FAITH = 1201;
	private static int ETERNITY_DIAMOND = 1230;
	private static int LEAF_OF_ORACLE = 1235;
	private static int BEAD_OF_SEASON = 1292;
	
	// Rewards
	private static int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	
	private static int[][] CLASSES = 
	{
		{ 26, 25, 15, 16, 17, 18, ETERNITY_DIAMOND },
		{ 29, 25, 19, 20, 21, 22, LEAF_OF_ORACLE },
		{ 11, 10, 23, 24, 25, 26, BEAD_OF_SEASON },
		{ 15, 10, 27, 28, 29, 30, MARK_OF_FAITH }
	};
	
	public ElvenHumanMystics1(int questId, String name, String descr)
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
				final boolean item = st.hasQuestItems(CLASSES[i][6]);
				if (player.getLevel() < 20)
				{
					suffix = (!item) ? CLASSES[i][2] : CLASSES[i][3];
				}
				else
				{
					if (!item)
					{
						suffix = CLASSES[i][4];
					}
					else
					{
						suffix = CLASSES[i][5];
						st.giveItems(SHADOW_WEAPON_COUPON_DGRADE, 15);
						st.takeItems(CLASSES[i][6], -1);
						player.setClassId(CLASSES[i][0]);
						player.setBaseClass(CLASSES[i][0]);
						st.playSound("ItemSound.quest_fanfare_2");
						player.broadcastUserInfo();
						st.exitQuest(false);
					}
				}
				event = npc.getId() + "-" + suffix + ".htm";
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
		{
			st = newQuestState(player);
		}
		if (player.isSubClassActive())
		{
			return htmltext;
		}
		
		final ClassId cid = player.getClassId();
		
		if (cid.getRace() == Race.Elf || cid.getRace() == Race.Human)
		{
			switch (cid)
			{
				case elvenMage:
				{
					htmltext = npc.getId() + "-01.htm";
					break;
				}
				case mage:
				{
					htmltext = npc.getId() + "-08.htm";
					break;
				}
				default:
				{
					if (cid.level() == 1)
					{
						return npc.getId() + "-31.htm";
					}
					else if (cid.level() >= 2)
					{
						return npc.getId() + "-32.htm";
					}
				}
			}
		}
		else
		{
			htmltext = npc.getId() + "-33.htm";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new ElvenHumanMystics1(-1, qn, "village_master");
	}
}