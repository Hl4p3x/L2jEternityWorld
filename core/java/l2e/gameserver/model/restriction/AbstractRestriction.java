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
package l2e.gameserver.model.restriction;

import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public abstract class AbstractRestriction implements GlobalRestriction
{
	public void activate()
	{
		GlobalRestrictions.activate(this);
	}
	
	public void deactivate()
	{
		GlobalRestrictions.deactivate(this);
	}
	
	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return getClass().equals(obj.getClass());
	}
	
	@Override
	@DisabledRestriction
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage, L2PcInstance attacker_, L2PcInstance target_)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canRequestRevive(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canTeleport(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2PcInstance player)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canUseItem(int itemId, L2PcInstance activeChar, L2ItemInstance item)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canStandUp(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void levelChanged(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void playerDisconnected(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean playerKilled(L2Character activeChar, L2PcInstance target, L2PcInstance killer)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public void playerRevived(L2PcInstance player)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean onAction(L2Npc npc, L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean fakePvPZone(L2PcInstance activeChar, L2PcInstance target)
	{
		throw new AbstractMethodError();
	}
}