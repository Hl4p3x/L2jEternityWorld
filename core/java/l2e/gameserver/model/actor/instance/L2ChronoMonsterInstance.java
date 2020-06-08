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

import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import l2e.gameserver.ai.L2AttackableAI;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;

public class L2ChronoMonsterInstance extends L2MonsterInstance
{
	private L2PcInstance _owner;
	
	public L2ChronoMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		setInstanceType(InstanceType.L2ChronoMonsterInstance);
		setAI(new L2ChronoAI(new AIAccessor()));
	}
	
	public final L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public void setOwner(L2PcInstance newOwner)
	{
		_owner = newOwner;
	}
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		@Override
		public void detachAI()
		{
		}
	}
	
	@Override
	public L2Character getMostHated()
	{
		return null;
	}
	
	class L2ChronoAI extends L2AttackableAI
	{
		public L2ChronoAI(L2Character.AIAccessor accessor)
		{
			super(accessor);
		}
		
		@Override
		protected void onEvtThink()
		{
			if (_actor.isAllSkillsDisabled())
			{
				return;
			}
			
			if (getIntention() == AI_INTENTION_ATTACK)
			{
				setIntention(AI_INTENTION_ACTIVE);
			}
		}
	}

	@Override
	public boolean isMonster()
	{
		return false;
	}
}