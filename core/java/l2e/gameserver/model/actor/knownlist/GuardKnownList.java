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
package l2e.gameserver.model.actor.knownlist;

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.instance.L2GuardInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;

public class GuardKnownList extends AttackableKnownList
{
	private static final Logger _log = Logger.getLogger(GuardKnownList.class.getName());
	
	public GuardKnownList(L2GuardInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}
		
		if (object.isPlayer())
		{
			if (object.getActingPlayer().getKarma() > 0)
			{
				if (Config.DEBUG)
				{
					_log.fine(getActiveChar().getObjectId() + ": PK " + object.getObjectId() + " entered scan range");
				}
				
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		else if ((Config.GUARD_ATTACK_AGGRO_MOB && getActiveChar().isInActiveRegion()) && object.isMonster())
		{
			if (((L2MonsterInstance) object).isAggressive())
			{
				if (Config.DEBUG)
				{
					_log.fine(getActiveChar().getObjectId() + ": Aggressive mob " + object.getObjectId() + " entered scan range");
				}
				
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		
		return true;
	}
	
	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (!super.removeKnownObject(object, forget))
		{
			return false;
		}
		
		if (getActiveChar().noTarget())
		{
			if (getActiveChar().hasAI() && !getActiveChar().isWalker() && !getActiveChar().isRunner() && !getActiveChar().isSpecialCamera() && !getActiveChar().isEkimusFood())
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			}
		}
		return true;
	}
	
	@Override
	public final L2GuardInstance getActiveChar()
	{
		return (L2GuardInstance) super.getActiveChar();
	}
}