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
package l2e.gameserver.ai;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.L2BoatInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.VehicleDeparture;
import l2e.gameserver.network.serverpackets.VehicleInfo;
import l2e.gameserver.network.serverpackets.VehicleStarted;

public class L2BoatAI extends L2VehicleAI
{
	public L2BoatAI(L2BoatInstance.AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void moveTo(int x, int y, int z)
	{
		if (!_actor.isMovementDisabled())
		{
			if (!_clientMoving)
			{
				_actor.broadcastPacket(new VehicleStarted(getActor(), 1));
			}
			
			_clientMoving = true;
			_accessor.moveTo(x, y, z);
			_actor.broadcastPacket(new VehicleDeparture(getActor()));
		}
	}
	
	@Override
	protected void clientStopMoving(Location loc)
	{
		if (_actor.isMoving())
		{
			_accessor.stopMove(loc);
		}
		
		if (_clientMoving || (loc != null))
		{
			_clientMoving = false;
			_actor.broadcastPacket(new VehicleStarted(getActor(), 0));
			_actor.broadcastPacket(new VehicleInfo(getActor()));
		}
	}
	
	@Override
	public void describeStateToPlayer(L2PcInstance player)
	{
		if (_clientMoving)
		{
			player.sendPacket(new VehicleDeparture(getActor()));
		}
	}
	
	@Override
	public L2BoatInstance getActor()
	{
		return (L2BoatInstance) _actor;
	}
}