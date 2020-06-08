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
package l2e.gameserver.network.clientpackets;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.VehiclePathPoint;
import l2e.gameserver.model.actor.instance.L2AirShipInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;

public class MoveToLocationAirShip extends L2GameClientPacket
{
	public static final int MIN_Z = -895;
	public static final int MAX_Z = 6105;
	public static final int STEP = 300;
	
	private int _command;
	private int _param1;
	private int _param2 = 0;
	
	@Override
	protected void readImpl()
	{
		_command = readD();
		_param1 = readD();
		if (_buf.remaining() > 0)
		{
			_param2 = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!activeChar.isInAirShip())
		{
			return;
		}
		
		final L2AirShipInstance ship = activeChar.getAirShip();
		if (!ship.isCaptain(activeChar))
		{
			return;
		}
		
		int z = ship.getZ();
		
		switch (_command)
		{
			case 0:
				if (!ship.canBeControlled())
				{
					return;
				}
				if (_param1 < L2World.GRACIA_MAX_X)
				{
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_param1, _param2, z));
				}
				break;
			case 1:
				if (!ship.canBeControlled())
				{
					return;
				}
				ship.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				break;
			case 2:
				if (!ship.canBeControlled())
				{
					return;
				}
				if (z < L2World.GRACIA_MAX_Z)
				{
					z = Math.min(z + STEP, L2World.GRACIA_MAX_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(ship.getX(), ship.getY(), z));
				}
				break;
			case 3:
				if (!ship.canBeControlled())
				{
					return;
				}
				if (z > L2World.GRACIA_MIN_Z)
				{
					z = Math.max(z - STEP, L2World.GRACIA_MIN_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(ship.getX(), ship.getY(), z));
				}
				break;
			case 4:
				if (!ship.isInDock() || ship.isMoving())
				{
					return;
				}
				
				final VehiclePathPoint[] dst = AirShipManager.getInstance().getTeleportDestination(ship.getDockId(), _param1);
				if (dst == null)
				{
					return;
				}
				
				final int fuelConsumption = AirShipManager.getInstance().getFuelConsumption(ship.getDockId(), _param1);
				if (fuelConsumption > 0)
				{
					if (fuelConsumption > ship.getFuel())
					{
						activeChar.sendPacket(SystemMessageId.THE_AIRSHIP_CANNOT_TELEPORT);
						return;
					}
					ship.setFuel(ship.getFuel() - fuelConsumption);
				}
				ship.executePath(dst);
				break;
		}
	}
}