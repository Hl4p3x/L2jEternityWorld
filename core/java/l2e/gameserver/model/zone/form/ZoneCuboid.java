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
package l2e.gameserver.model.zone.form;

import java.awt.Rectangle;

import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.zone.L2ZoneForm;
import l2e.util.Rnd;

public class ZoneCuboid extends L2ZoneForm
{
	private final int _z1, _z2;
	Rectangle _r;
	
	public ZoneCuboid(int x1, int x2, int y1, int y2, int z1, int z2)
	{
		int _x1 = Math.min(x1, x2);
		int _x2 = Math.max(x1, x2);
		int _y1 = Math.min(y1, y2);
		int _y2 = Math.max(y1, y2);
		
		_r = new Rectangle(_x1, _y1, _x2 - _x1, _y2 - _y1);
		
		_z1 = Math.min(z1, z2);
		_z2 = Math.max(z1, z2);
	}
	
	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return (_r.contains(x, y) && (z >= _z1) && (z <= _z2));
	}
	
	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		return (_r.intersects(Math.min(ax1, ax2), Math.min(ay1, ay2), Math.abs(ax2 - ax1), Math.abs(ay2 - ay1)));
	}
	
	@Override
	public double getDistanceToZone(int x, int y)
	{
		int _x1 = _r.x;
		int _x2 = _r.x + _r.width;
		int _y1 = _r.y;
		int _y2 = _r.y + _r.height;
		
		double test, shortestDist = Math.pow(_x1 - x, 2) + Math.pow(_y1 - y, 2);
		
		test = Math.pow(_x1 - x, 2) + Math.pow(_y2 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}
		
		test = Math.pow(_x2 - x, 2) + Math.pow(_y1 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}
		
		test = Math.pow(_x2 - x, 2) + Math.pow(_y2 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}
		
		return Math.sqrt(shortestDist);
	}
	
	@Override
	public int getLowZ()
	{
		return _z1;
	}
	
	@Override
	public int getHighZ()
	{
		return _z2;
	}
	
	@Override
	public void visualizeZone(int z)
	{
		int _x1 = _r.x;
		int _x2 = _r.x + _r.width;
		int _y1 = _r.y;
		int _y2 = _r.y + _r.height;
		
		for (int x = _x1; x < _x2; x = x + STEP)
		{
			dropDebugItem(PcInventory.ADENA_ID, 1, x, _y1, z);
			dropDebugItem(PcInventory.ADENA_ID, 1, x, _y2, z);
		}
		
		for (int y = _y1; y < _y2; y = y + STEP)
		{
			dropDebugItem(PcInventory.ADENA_ID, 1, _x1, y, z);
			dropDebugItem(PcInventory.ADENA_ID, 1, _x2, y, z);
		}
	}
	
	@Override
	public int[] getRandomPoint()
	{
		int x = Rnd.get(_r.x, _r.x + _r.width);
		int y = Rnd.get(_r.y, _r.y + _r.height);
		
		return new int[]
		{
			x,
			y,
			GeoClient.getInstance().getHeight(x, y, _z1)
		};
	}
}