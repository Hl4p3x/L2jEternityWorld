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

import java.util.Collection;

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;

public final class L2EventChestInstance extends L2EventMonsterInstance
{
	private boolean _isTriggered = false;
	
	public L2EventChestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		setIsNoRndWalk(true);
		disableCoreAI(true);
		
		eventSetDropOnGround(true);
		eventSetBlockOffensiveSkills(true);
	}
	
	public boolean canSee(L2Character cha)
	{
		return (cha != null) && (cha.isGM() || !isInvisible());
	}
	
	public void trigger()
	{
		_isTriggered = true;
		broadcastPacket(new AbstractNpcInfo.NpcInfo(this, null));
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (_isTriggered || canSee(activeChar))
		{
			activeChar.sendPacket(new AbstractNpcInfo.NpcInfo(this, activeChar));
		}
	}
	
	@Override
	public void broadcastPacket(L2GameServerPacket mov)
	{
		mov.setInvisible(isInvisible());
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if ((player != null) && (_isTriggered || canSee(player)))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		mov.setInvisible(isInvisible());
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player == null)
			{
				continue;
			}
			if (isInsideRadius(player, radiusInKnownlist, false, false))
			{
				if (_isTriggered || canSee(player))
				{
					player.sendPacket(mov);
				}
			}
		}
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !canSee(attacker);
	}
	
	@Override
	public void reduceHate(L2Character target, int amount)
	{
	}
	
	@Override
	public boolean isAttackable()
	{
		return false;
	}
}