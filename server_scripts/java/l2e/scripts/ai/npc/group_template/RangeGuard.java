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

import java.util.List;

import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;

/**
 * Created by LordWinter 17.11.2013 Based on L2J Eternity-World
 */
public final class RangeGuard extends AbstractNpcAI
{
	private static SkillsHolder ULTIMATE_DEFENSE = new SkillsHolder(5044, 3);
	private static final int MIN_DISTANCE = 150;

	private static final int[] NOT_ALLOWED_SKILLS =
	{
	                15, 28,
	                51, 65,
	                106, 115,
	                122, 127,
	                254, 352,
	                353, 358,
	                402, 403,
	                412, 485,
	                501, 511,
	                522, 531,
	                680, 695,
	                696, 716,
	                775, 792,
	                1042, 1049,
	                1069, 1071,
	                1072, 1074,
	                1083, 1097,
	                1092, 1064,
	                1160, 1164,
	                1169, 1170,
	                1201, 1206,
	                1222, 1223,
	                1224, 1263,
	                1269, 1336,
	                1337, 1338,
	                1358, 1359,
	                1386, 1394,
	                1396, 1445,
	                1446, 1447,
	                1481, 1482,
	                1483, 1484,
	                1485, 1486,
	                1511, 1524,
	                1529,
	};

	private RangeGuard()
	{
		super(RangeGuard.class.getSimpleName(), "ai");

		final List<L2NpcTemplate> monsters = NpcTable.getInstance().getAllNpcOfClassType("L2Monster");
		for (L2NpcTemplate template : monsters)
		{
			if (template.hasParameters() && (template.getParameters().getInteger("LongRangeGuardRate", -1) > 0))
			{
				addAttackId(template.getId());
			}
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		final L2Playable playable = (isSummon) ? attacker.getSummon() : attacker;
		final int longRangeGuardRate = npc.getTemplate().getParameters().getInteger("LongRangeGuardRate");
		final double distance = Util.calculateDistance(npc, playable, true);

		if ((npc.getFirstEffect(ULTIMATE_DEFENSE.getSkillId()) != null) && (distance <= MIN_DISTANCE))
		{
			npc.stopSkillEffects(ULTIMATE_DEFENSE.getSkillId());
		}
		else if ((distance > MIN_DISTANCE) && !npc.isSkillDisabled(ULTIMATE_DEFENSE.getSkillId()) && !((skill != null) && Util.contains(NOT_ALLOWED_SKILLS, skill.getId())) && (getRandom(100) < longRangeGuardRate))
		{
			npc.setTarget(npc);
			npc.doCast(ULTIMATE_DEFENSE.getSkill());
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}

	public static void main(String[] args)
	{
		new RangeGuard();
	}
}
