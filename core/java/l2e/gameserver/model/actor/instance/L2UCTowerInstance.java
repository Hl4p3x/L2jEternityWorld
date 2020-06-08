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
package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.underground_coliseum.UCTeam;
import l2e.gameserver.model.skills.L2Skill;

public class L2UCTowerInstance extends L2Npc
{
	private UCTeam _team;
	
	public L2UCTowerInstance(UCTeam team, int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		_team = team;
	}
	
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character creature)
	{
		return true;
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (damage < getStatus().getCurrentHp())
		{
			getStatus().setCurrentHp(getStatus().getCurrentHp() - damage);
		}
		else
		{
			doDie(attacker);
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (_team != null)
		{
			_team.deleteTower();
			_team = null;
		}
		return true;
	}
	
	@Override
	public int getTeam()
	{
		return _team.getIndex() + 1;
	}
}