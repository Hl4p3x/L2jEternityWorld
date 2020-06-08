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
package l2e.gameserver.model.skills.l2skills;

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.ActionFailed;

public class L2SkillDefault extends L2Skill
{
	
	public L2SkillDefault(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		caster.sendPacket(ActionFailed.STATIC_PACKET);
		caster.sendMessage("Skill not implemented. Skill ID: " + getId() + " " + getSkillType());
	}	
}