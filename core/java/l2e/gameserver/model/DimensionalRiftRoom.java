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
package l2e.gameserver.model;

import java.awt.Polygon;
import java.awt.Shape;

import javolution.util.FastList;

import l2e.gameserver.model.actor.L2Npc;
import l2e.util.Rnd;

public final class DimensionalRiftRoom
{
	private final byte _type;
	private final byte _room;
	private final int _xMin;
	private final int _xMax;
	private final int _yMin;
	private final int _yMax;
	private final int _zMin;
	private final int _zMax;
	private final int[] _teleportCoords;
	private final Shape _s;
	private final boolean _isBossRoom;
	private final FastList<L2Spawn> _roomSpawns;
	protected final FastList<L2Npc> _roomMobs;
	private boolean _partyInside = false;
	
	public DimensionalRiftRoom(byte type, byte room, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, int xT, int yT, int zT, boolean isBossRoom)
	{
		_type = type;
		_room = room;
		_xMin = (xMin + 128);
		_xMax = (xMax - 128);
		_yMin = (yMin + 128);
		_yMax = (yMax - 128);
		_zMin = zMin;
		_zMax = zMax;
		_teleportCoords = new int[]
		{
			xT,
			yT,
			zT
		};
		_isBossRoom = isBossRoom;
		_roomSpawns = new FastList<>();
		_roomMobs = new FastList<>();
		_s = new Polygon(new int[]
		{
			xMin,
			xMax,
			xMax,
			xMin
		}, new int[]
		{
			yMin,
			yMin,
			yMax,
			yMax
		}, 4);
	}
	
	public byte getType()
	{
		return _type;
	}
	
	public byte getRoom()
	{
		return _room;
	}
	
	public int getRandomX()
	{
		return Rnd.get(_xMin, _xMax);
	}
	
	public int getRandomY()
	{
		return Rnd.get(_yMin, _yMax);
	}
	
	public int[] getTeleportCoorinates()
	{
		return _teleportCoords;
	}
	
	public boolean checkIfInZone(int x, int y, int z)
	{
		return _s.contains(x, y) && (z >= _zMin) && (z <= _zMax);
	}
	
	public boolean isBossRoom()
	{
		return _isBossRoom;
	}
	
	public FastList<L2Spawn> getSpawns()
	{
		return _roomSpawns;
	}
	
	public void spawn()
	{
		for (L2Spawn spawn : _roomSpawns)
		{
			spawn.doSpawn();
			spawn.startRespawn();
		}
	}
	
	public DimensionalRiftRoom unspawn()
	{
		for (L2Spawn spawn : _roomSpawns)
		{
			spawn.stopRespawn();
			if (spawn.getLastSpawn() != null)
			{
				spawn.getLastSpawn().deleteMe();
			}
		}
		return this;
	}
	
	public boolean isPartyInside()
	{
		return _partyInside;
	}
	
	public void setPartyInside(boolean partyInside)
	{
		_partyInside = partyInside;
	}
}