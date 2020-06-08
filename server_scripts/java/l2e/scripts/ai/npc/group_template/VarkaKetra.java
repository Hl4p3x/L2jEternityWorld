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
package l2e.scripts.ai.npc.group_template;

import l2e.scripts.quests._605_AllianceWithKetraOrcs;
import l2e.scripts.quests._606_WarWithVarkaSilenos;
import l2e.scripts.quests._607_ProveYourCourage;
import l2e.scripts.quests._608_SlayTheEnemyCommander;
import l2e.scripts.quests._609_MagicalPowerOfWaterPart1;
import l2e.scripts.quests._610_MagicalPowerOfWaterPart2;
import l2e.scripts.quests._611_AllianceWithVarkaSilenos;
import l2e.scripts.quests._612_WarWithKetraOrcs;
import l2e.scripts.quests._613_ProveYourCourage;
import l2e.scripts.quests._614_SlayTheEnemyCommander;
import l2e.scripts.quests._615_MagicalPowerOfFirePart1;
import l2e.scripts.quests._616_MagicalPowerOfFirePart2;

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.util.Util;

public class VarkaKetra extends AbstractNpcAI
{
	private static final int[] KETRA =
	{
		21324,
		21325,
		21327,
		21328,
		21329, 
		21331,
		21332,
		21334,
		21336,
		21338,
		21339,
		21340,
		21342,
		21343,
		21344,
		21345,
		21346,
		21347,
		21348,
		21349,
		25299,
		25302,
		25305,
		25306
	};
	private static final int[] VARKA =
	{
		21350,
		21351,
		21353,
		21354,
		21355,
		21357,
		21358,
		21360,
		21361,
		21362,
		21364,
		21365,
		21366,
		21368,
		21369,
		21370,
		21371,
		21372,
		21373,
		21374,
		21375,
		25309,
		25312,
		25315,
		25316
	};

	private static final int[] KETRA_MARKS =
	{
		7211,
		7212,
		7213,
		7214,
		7215
	};

	private static final int[] VARKA_MARKS =
	{
		7221,
		7222,
		7223,
		7224,
		7225
	};

	private static final String[] KETRA_QUESTS =
	{
		_605_AllianceWithKetraOrcs.class.getSimpleName(),
		_606_WarWithVarkaSilenos.class.getSimpleName(),
		_607_ProveYourCourage.class.getSimpleName(),
		_608_SlayTheEnemyCommander.class.getSimpleName(),
		_609_MagicalPowerOfWaterPart1.class.getSimpleName(),
		_610_MagicalPowerOfWaterPart2.class.getSimpleName()
	};

	private static final String[] VARKA_QUESTS =
	{
		_611_AllianceWithVarkaSilenos.class.getSimpleName(),
		_612_WarWithKetraOrcs.class.getSimpleName(),
		_613_ProveYourCourage.class.getSimpleName(),
		_614_SlayTheEnemyCommander.class.getSimpleName(),
		_615_MagicalPowerOfFirePart1.class.getSimpleName(),
		_616_MagicalPowerOfFirePart2.class.getSimpleName()
	};
	
	private VarkaKetra(String name, String descr)
	{
		super(name, descr);

		addAggroRangeEnterId(KETRA);
		addAggroRangeEnterId(VARKA);
		addKillId(KETRA);
		addKillId(VARKA);
	}
	
	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		if (Util.checkIfInRange(1500, player, npc, false))
		{
			if (Util.contains(KETRA, npc.getId()) && hasAtLeastOneQuestItem(player, KETRA_MARKS))
			{
				decreaseAlliance(player, KETRA_MARKS);
				exitQuests(player, KETRA_QUESTS);
			}
			else if (Util.contains(VARKA, npc.getId()) && hasAtLeastOneQuestItem(player, VARKA_MARKS))
			{
				decreaseAlliance(player, VARKA_MARKS);
				exitQuests(player, VARKA_QUESTS);
			}
		}
	}
	
	private void decreaseAlliance(L2PcInstance player, int[] marks)
	{
		for (int i = 0; i < marks.length; i++)
		{
			if (hasQuestItems(player, marks[i]))
			{
				takeItems(player, marks[i], -1);
				if (i > 0)
				{
					giveItems(player, marks[i - 1], 1);
				}
				return;
			}
		}
	}
	
	private void exitQuests(L2PcInstance player, String[] quests)
	{
		for (String quest : quests)
		{
			final QuestState qs = player.getQuestState(quest);
			if ((qs != null) && qs.isStarted())
			{
				qs.exitQuest(true);
			}
		}
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((Util.contains(KETRA, npc.getId()) && hasAtLeastOneQuestItem(player, KETRA_MARKS)) || (Util.contains(VARKA, npc.getId()) && hasAtLeastOneQuestItem(player, VARKA_MARKS)))
		{
			if (((L2Attackable) npc).containsTarget(player))
			{
				((L2Attackable) npc).getAggroList().get(player).stopHate();
			}
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new VarkaKetra(VarkaKetra.class.getSimpleName(), "ai");
	}
}