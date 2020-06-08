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

import l2e.geoserver.model.GeoShape;
import l2e.util.vctutil.AdvUtil;

public class L2Range implements GeoShape
{
	final Location[] _points = new Location[4];
	final Polygon _polygon = new Polygon();
	int _idx = 0;
	int _zmin = 30000;
	int _zmax = -30000;
	int _xMin, _yMin;
	int _xMax, _yMax;
	
	public L2Range()
	{
	}
	
	public synchronized void add(int x, int y, int z)
	{
		if (_idx == 0)
		{
			_xMin = _xMax = x;
			_yMin = _yMax = y;
		}
		else
		{
			if (x > _xMax)
			{
				_xMax = x;
			}
			if (x < _xMin)
			{
				_xMin = x;
			}
			if (y < _yMin)
			{
				_yMin = y;
			}
			if (y > _yMax)
			{
				_yMax = y;
			}
		}
		_points[_idx] = new Location(x, y, z);
		_idx++;
		if (z < _zmin)
		{
			_zmin = z;
		}
		if (z > _zmax)
		{
			_zmax = z;
		}
		_polygon.addPoint(x, y);
	}
	
	public int findIntersect(Location fromLoc, Location toLoc)
	{
		if (((fromLoc.getZ() < _zmin) && (toLoc.getZ() < _zmin)) || ((fromLoc.getZ() > _zmax) && (toLoc.getZ() > _zmax)))
		{
			return -1;
		}
		
		if (AdvUtil.findIntersection(_points[0], _points[1], fromLoc, toLoc))
		{
			return 0;
		}
		else if (AdvUtil.findIntersection(_points[1], _points[2], fromLoc, toLoc))
		{
			return 1;
		}
		else if (AdvUtil.findIntersection(_points[2], _points[3], fromLoc, toLoc))
		{
			return 2;
		}
		else if (AdvUtil.findIntersection(_points[3], _points[0], fromLoc, toLoc))
		{
			return 3;
		}
		return -1;
	}
	
	public Location[] getPoints()
	{
		return _points;
	}
	
	public int getMinZ()
	{
		return _zmin;
	}
	
	@Override
	public boolean isInside(int x, int y)
	{
		return _polygon.contains(x, y);
	}
	
	@Override
	public int getXmax()
	{
		return _xMax;
	}
	
	@Override
	public int getXmin()
	{
		return _xMin;
	}
	
	@Override
	public int getYmax()
	{
		return _yMax;
	}
	
	@Override
	public int getYmin()
	{
		return _yMin;
	}
	
	@Override
	public int getZmax()
	{
		return _zmax;
	}
	
	@Override
	public int getZmin()
	{
		return _zmin;
	}
}
