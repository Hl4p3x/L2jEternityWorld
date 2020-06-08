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
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class FollowerOfAllosce extends L2AttackableAIScript
{
	private static final int FOFALLOSCE = 18568;

	public FollowerOfAllosce(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addAggroRangeEnterId(FOFALLOSCE);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_skill"))
		{
			npc.setTarget(player);
			npc.doCast(SkillHolder.getInstance().getInfo(5624, 1));
			startQuestTimer("time_to_skill", 30000, npc, player);
		}
		return "";
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcId = npc.getId();

		if (npcId == FOFALLOSCE)
		{
			npc.setIsInvul(true);
			startQuestTimer("time_to_skill", 30000, npc, player);
			npc.setTarget(player);
			npc.doCast(SkillHolder.getInstance().getInfo(5624, 1));
		}
		return "";
	}

	public static void main(String[] args)
	{
		new FollowerOfAllosce(-1, "FollowerOfAllosce", "ai");
	}
}
