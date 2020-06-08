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
package l2e.scripts.hellbound;

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class Kief extends Quest
{
	private static final int KIEF = 32354;
	
	private static final int BOTTLE = 9672;
	private static final int DARION_BADGE = 9674;
	private static final int DIM_LIFE_FORCE = 9680;
	private static final int LIFE_FORCE = 9681;
	private static final int CONTAINED_LIFE_FORCE = 9682;
	private static final int STINGER = 10012;
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			qs = newQuestState(player);
		}
		
		if ("Badges".equalsIgnoreCase(event))
		{
			switch (HellboundManager.getInstance().getLevel())
			{
				case 2:
				case 3:
					if (qs.hasQuestItems(DARION_BADGE))
					{
						HellboundManager.getInstance().updateTrust((int) qs.getQuestItemsCount(DARION_BADGE) * 10, true);
						qs.takeItems(DARION_BADGE, -1);
						return "32354-10.htm";
					}
			}
			return "32354-10a.htm";
		}
		else if ("Bottle".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() >= 7)
			{
				if (qs.getQuestItemsCount(STINGER) >= 20)
				{
					qs.takeItems(STINGER, 20);
					qs.giveItems(BOTTLE, 1);
					return "32354-11h.htm";
				}
				return "32354-11i.htm";
			}
		}
		else if ("dlf".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 7)
			{
				if (qs.hasQuestItems(DIM_LIFE_FORCE))
				{
					HellboundManager.getInstance().updateTrust((int) qs.getQuestItemsCount(DIM_LIFE_FORCE) * 20, true);
					qs.takeItems(DIM_LIFE_FORCE, -1);
					return "32354-11a.htm";
				}
				return "32354-11b.htm";
			}
		}
		else if ("lf".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 7)
			{
				if (qs.hasQuestItems(LIFE_FORCE))
				{
					HellboundManager.getInstance().updateTrust((int) qs.getQuestItemsCount(LIFE_FORCE) * 80, true);
					qs.takeItems(LIFE_FORCE, -1);
					return "32354-11c.htm";
				}
				return "32354-11d.htm";
			}
		}
		else if ("clf".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 7)
			{
				if (qs.hasQuestItems(CONTAINED_LIFE_FORCE))
				{
					HellboundManager.getInstance().updateTrust((int) qs.getQuestItemsCount(CONTAINED_LIFE_FORCE) * 200, true);
					qs.takeItems(CONTAINED_LIFE_FORCE, -1);
					return "32354-11e.htm";
				}
				return "32354-11f.htm";
			}
		}
		return event;
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		
		switch (HellboundManager.getInstance().getLevel())
		{
			case 1:
				return "32354-01.htm";
			case 2:
			case 3:
				return "32354-01a.htm";
			case 4:
				return "32354-01e.htm";
			case 5:
				return "32354-01d.htm";
			case 6:
				return "32354-01b.htm";
			case 7:
				return "32354-01c.htm";
			default:
				return "32354-01f.htm";
		}
	}
	
	public Kief(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(KIEF);
		addStartNpc(KIEF);
		addTalkId(KIEF);
	}
	
	public static void main(String[] args)
	{
		new Kief(-1, "Kief", "hellbound");
	}
}