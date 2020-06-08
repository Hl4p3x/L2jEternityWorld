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
package l2e.gameserver.model.entity.underground_coliseum;

import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class UCPoint
{
	private final int[] _doors = new int[2];
	private final int _x;
	private final int _y;
	private final int _z;
	
	public UCPoint(int door1, int door2, int x, int y, int z)
	{
		_doors[0] = door1;
		_doors[1] = door2;
		_x = x;
		_y = y;
		_z = z;
	}
	
	public void teleportPeoples(L2PcInstance[] players)
	{
		if (players == null)
		{
			return;
		}
		
		for (L2PcInstance player : players)
		{
			if (player == null)
			{
				continue;
			}
			
			if (player.isDead())
			{
				UCTeam.resPlayer(player);
			}
			
			player.teleToLocation(_x, _y, _z);
		}
	}
	
	public L2Party getParty()
	{
		return null;
	}
	
	public void actionDoors(boolean open)
	{
		for (int id : _doors)
		{
			L2DoorInstance door = DoorParser.getInstance().getDoor(id);
			door.setOpen(open);
			door.broadcastStatusUpdate();
		}
	}
	
	public int getX()
	{
		return _x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public int getZ()
	{
		return _z;
	}
}