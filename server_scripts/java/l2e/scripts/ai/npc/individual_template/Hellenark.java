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
package l2e.scripts.ai.npc.individual_template;

import java.util.ArrayList;

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.scripts.ai.L2AttackableAIScript;

public class Hellenark extends L2AttackableAIScript
{
	private static final int Hellenark = 22326;
	private static final int naia = 18484;
	private int status = 0;
	public ArrayList<L2Npc> spawnnaia = new ArrayList<>();

	private static final int[][] naialoc =
	{
	                {
	                                -24542,
	                                245792,
	                                -3133,
	                                19078
	                },
	                {
	                                -23839,
	                                246056,
	                                -3133,
	                                17772
	                },
	                {
	                                -23713,
	                                244358,
	                                -3133,
	                                53369
	                },
	                {
	                                -23224,
	                                244524,
	                                -3133,
	                                57472
	                },
	                {
	                                -24709,
	                                245186,
	                                -3133,
	                                63974
	                },
	                {
	                                -24394,
	                                244379,
	                                -3133,
	                                5923
	                }
	};

	public Hellenark(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addAttackId(Hellenark);
		addTalkId(naia);
		addFirstTalkId(naia);
		addStartNpc(naia);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon, L2Skill skill)
	{
		if (npc.getId() == Hellenark)
		{
			if (status == 0)
			{
				startQuestTimer("spawn", 20000, npc, null, false);
			}
			status = 1;
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		if (event.equalsIgnoreCase("spawn"))
		{
			if (status == 1)
			{
				status = 3;
			}
			startQuestTimer("check", 30000, npc, null, false);
			for (int i = 0; i < 6; i++)
			{
				L2Npc mob = addSpawn(naia, naialoc[i][0], naialoc[i][1], naialoc[i][2], naialoc[i][3], false, 0);
				spawnnaia.add(mob);
				mob.setIsInvul(true);
				mob.setIsImmobilized(true);
				mob.setIsOverloaded(true);
			}
			startQuestTimer("cast", 5000, npc, null, false);
		}
		if (event.equalsIgnoreCase("check"))
		{
			if (status == 1)
			{
				startQuestTimer("check", 180000, npc, null, false);
			}
			if (status == 3)
			{
				startQuestTimer("desp", 180000, npc, null, false);
			}
			status = 3;
		}
		if (event.equalsIgnoreCase("desp"))
		{
			cancelQuestTimers("cast");
			for (L2Npc npc1 : spawnnaia)
			{
				npc1.deleteMe();
			}
			status = 0;
		}
		if (event.equalsIgnoreCase("cast"))
		{
			for (L2Npc npc1 : spawnnaia)
			{
				npc1.setTarget(player);
				npc1.doCast(SkillHolder.getInstance().getInfo(5765, 1));
			}
			startQuestTimer("cast", 5000, npc, null, false);
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Hellenark(-1, "Hellenark", "ai");
	}
}
