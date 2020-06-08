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

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class PowderKeg extends L2AttackableAIScript
{
	private static final int[] NPC_IDS =
	{
		        18622
	};

	public PowderKeg(int questId, String name, String descr)
	{
		super(questId, name, descr);

		this.registerMobs(NPC_IDS);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();

		if (Util.contains(NPC_IDS, npcId))
		{
			L2Skill _boom = SkillHolder.getInstance().getInfo(5714, 1);
			npc.disableCoreAI(true);
			npc.doCast(_boom);
			npc.broadcastStatusUpdate();
			return "";
		}
		return super.onKill(npc, killer, isSummon);
	}

	public static void main(String[] args)
	{
		new PowderKeg(-1, "powderkeg", "ai");
	}
}
