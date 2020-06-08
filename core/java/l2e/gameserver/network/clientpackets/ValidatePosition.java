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

import l2e.Config;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.geoeditorcon.GeoEditorListener;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.serverpackets.GetOnVehicle;
import l2e.gameserver.network.serverpackets.ValidateLocation;

public class ValidatePosition extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _data;
	
	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
		_data = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar == null) || activeChar.isTeleporting() || activeChar.inObserverMode())
		{
			return;
		}
		
		final int realX = activeChar.getX();
		final int realY = activeChar.getY();
		int realZ = activeChar.getZ();
		
		if (Config.DEVELOPER)
		{
			_log.fine("client pos: " + _x + " " + _y + " " + _z + " head " + _heading);
			_log.fine("server pos: " + realX + " " + realY + " " + realZ + " head " + activeChar.getHeading());
		}
		
		if ((_x == 0) && (_y == 0))
		{
			if (realX != 0)
			{
				return;
			}
		}
		
		int dx, dy, dz;
		double diffSq;
		
		if (activeChar.isInBoat())
		{
			if (Config.GEODATA)
			{
				dx = _x - activeChar.getInVehiclePosition().getX();
				dy = _y - activeChar.getInVehiclePosition().getY();
				diffSq = ((dx * dx) + (dy * dy));
				if (diffSq > 250000)
				{
					sendPacket(new GetOnVehicle(activeChar.getObjectId(), _data, activeChar.getInVehiclePosition()));
				}
			}
			return;
		}
		if (activeChar.isInAirShip())
		{
			return;
		}
		
		if (activeChar.isFalling(_z))
		{
			return;
		}
		
		dx = _x - realX;
		dy = _y - realY;
		dz = _z - realZ;
		diffSq = ((dx * dx) + (dy * dy));
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			if ((GeoEditorListener.getInstance().getThread() != null) && GeoEditorListener.getInstance().getThread().isWorking() && GeoEditorListener.getInstance().getThread().isSend(activeChar))
			{
				GeoEditorListener.getInstance().getThread().sendGmPosition(_x, _y, (short) _z);
			}
		}
		
		if (activeChar.isFlyingMounted() && (_x > L2World.GRACIA_MAX_X))
		{
			activeChar.untransform();
		}
		
		if (activeChar.isFlying() || activeChar.isInsideZone(ZoneId.WATER))
		{
			activeChar.setXYZ(realX, realY, _z);
			if (diffSq > 90000)
			{
				activeChar.sendPacket(new ValidateLocation(activeChar));
			}
		}
		else if (diffSq < 360000)
		{
			if (!activeChar.isMoving() || !activeChar.validateMovementHeading(_heading))
			{
				if (diffSq < 2500)
				{
					activeChar.setXYZ(realX, realY, _z);
				}
				else
				{
					activeChar.setXYZ(_x, _y, _z);
				}
			}
			else
			{
				activeChar.setXYZ(realX, realY, _z);
			}
			activeChar.setHeading(_heading);
			
			if (Config.GEODATA && ((diffSq > 250000) || (Math.abs(dz) > 200)))
			{
				if ((Math.abs(dz) > 200) && (Math.abs(dz) < 1500) && (Math.abs(_z - activeChar.getClientZ()) < 800))
				{
					activeChar.setXYZ(realX, realY, _z);
					realZ = GeoClient.getInstance().getSpawnHeight(_x, _y, activeChar.getZ());
				}
				else
				{
					if (Config.DEVELOPER)
					{
						_log.info(activeChar.getName() + ": Synchronizing position Server --> Client");
					}
					activeChar.sendPacket(new ValidateLocation(activeChar));
				}
			}
		}
		activeChar.setClientX(_x);
		activeChar.setClientY(_y);
		activeChar.setClientZ(_z);
		activeChar.setClientHeading(_heading);
		activeChar.setLastServerPosition(realX, realY, realZ);
	}
}