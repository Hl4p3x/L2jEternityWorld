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

import l2e.Config;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2AirShipInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.DeleteObject;
import l2e.gameserver.network.serverpackets.SpawnItem;

public class PcKnownList extends PlayableKnownList
{
	public PcKnownList(L2PcInstance activeChar)
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
		
		if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
		{
			getActiveChar().sendPacket(new SpawnItem(object));
		}
		else
		{
			if (object.isVisibleFor(getActiveChar()))
			{
				object.sendInfo(getActiveChar());
				if (object instanceof L2Character)
				{
					final L2Character obj = (L2Character) object;
					if (obj.hasAI())
					{
						obj.getAI().describeStateToPlayer(getActiveChar());
					}
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
		
		if (object instanceof L2AirShipInstance)
		{
			if ((((L2AirShipInstance) object).getCaptainId() != 0) && (((L2AirShipInstance) object).getCaptainId() != getActiveChar().getObjectId()))
			{
				getActiveChar().sendPacket(new DeleteObject(((L2AirShipInstance) object).getCaptainId()));
			}
			if (((L2AirShipInstance) object).getHelmObjectId() != 0)
			{
				getActiveChar().sendPacket(new DeleteObject(((L2AirShipInstance) object).getHelmObjectId()));
			}
		}
		
		getActiveChar().sendPacket(new DeleteObject(object));
		
		if (Config.CHECK_KNOWN && (object instanceof L2Npc) && getActiveChar().isGM())
		{
			getActiveChar().sendMessage("Removed NPC: " + ((L2Npc) object).getName());
		}
		
		return true;
	}
	
	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if (object.isWalker())
		{
			return 10000;
		}
		
		if (object.isPhantome())
		{
			return 5000;
		}
		
		if (object.isRunner() || object.isSpecialCamera() || object.isEkimusFood())
		{
			return 15000;
		}
		
		final int knownlistSize = getKnownObjects().size();
		if (knownlistSize <= 25)
		{
			return 4000;
		}
		if (knownlistSize <= 35)
		{
			return 3500;
		}
		if (knownlistSize <= 70)
		{
			return 2910;
		}
		return 2310;
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object.isWalker())
		{
			return 9000;
		}

		if (object.isPhantome())
		{
			return 4000;
		}
		
		if (object.isRunner() || object.isSpecialCamera() || object.isEkimusFood())
		{
			return 14000;
		}
		
		final int knownlistSize = getKnownObjects().size();
		if (knownlistSize <= 25)
		{
			return 3400;
		}
		if (knownlistSize <= 35)
		{
			return 2900;
		}
		if (knownlistSize <= 70)
		{
			return 2300;
		}
		return 1700;
	}
}