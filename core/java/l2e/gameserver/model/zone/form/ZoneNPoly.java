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

import java.awt.Polygon;

import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.zone.L2ZoneForm;
import l2e.util.Rnd;

public class ZoneNPoly extends L2ZoneForm
{
	private final Polygon _p;
	private final int _z1;
	private final int _z2;
	
	public ZoneNPoly(int[] x, int[] y, int z1, int z2)
	{
		_p = new Polygon(x, y, x.length);
		
		_z1 = Math.min(z1, z2);
		_z2 = Math.max(z1, z2);
	}
	
	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return (_p.contains(x, y) && (z >= _z1) && (z <= _z2));
	}
	
	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		return (_p.intersects(Math.min(ax1, ax2), Math.min(ay1, ay2), Math.abs(ax2 - ax1), Math.abs(ay2 - ay1)));
	}
	
	@Override
	public double getDistanceToZone(int x, int y)
	{
		int[] _x = _p.xpoints;
		int[] _y = _p.ypoints;
		
		double test, shortestDist = Math.pow(_x[0] - x, 2) + Math.pow(_y[0] - y, 2);
		
		for (int i = 1; i < _p.npoints; i++)
		{
			test = Math.pow(_x[i] - x, 2) + Math.pow(_y[i] - y, 2);
			if (test < shortestDist)
			{
				shortestDist = test;
			}
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
		int[] _x = _p.xpoints;
		int[] _y = _p.ypoints;
		
		for (int i = 0; i < _p.npoints; i++)
		{
			int nextIndex = i + 1;
			
			if (nextIndex == _x.length)
			{
				nextIndex = 0;
			}
			int vx = _x[nextIndex] - _x[i];
			int vy = _y[nextIndex] - _y[i];
			float lenght = (float) Math.sqrt((vx * vx) + (vy * vy));
			lenght /= STEP;
			for (int o = 1; o <= lenght; o++)
			{
				float k = o / lenght;
				dropDebugItem(PcInventory.ADENA_ID, 1, (int) (_x[i] + (k * vx)), (int) (_y[i] + (k * vy)), z);
			}
		}
	}
	
	@Override
	public int[] getRandomPoint()
	{
		int x, y;
		
		int _minX = _p.getBounds().x;
		int _maxX = _p.getBounds().x + _p.getBounds().width;
		int _minY = _p.getBounds().y;
		int _maxY = _p.getBounds().y + _p.getBounds().height;
		
		x = Rnd.get(_minX, _maxX);
		y = Rnd.get(_minY, _maxY);
		
		int antiBlocker = 0;
		while (!_p.contains(x, y) && (antiBlocker < 1000))
		{
			x = Rnd.get(_minX, _maxX);
			y = Rnd.get(_minY, _maxY);
			antiBlocker++;
		}
		
		return new int[]
		{
			x,
			y,
			GeoClient.getInstance().getHeight(x, y, _z1)
		};
	}
}