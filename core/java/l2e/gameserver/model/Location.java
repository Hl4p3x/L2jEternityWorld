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

import l2e.Config;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.interfaces.IPositionable;
import l2e.geoserver.model.MoveTrick;
import l2e.util.Rnd;

public class Location implements IPositionable
{
	public int _x;
	public int _y;
	public int _z;
	public int _heading;
	private int _instanceId;
	private MoveTrick[] _tricks;
	
	public Location()
	{
		super();
	}
	
	public Location(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	public Location(L2Object obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		_heading = obj.getHeading();
		_instanceId = obj.getInstanceId();
	}
	
	public Location(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
		_instanceId = -1;
	}
	
	public Location(int x, int y, int z, int heading, int instanceId)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
		_instanceId = instanceId;
	}
	
	@Override
	public int getX()
	{
		return _x;
	}
	
	public void setX(int x)
	{
		_x = x;
	}
	
	@Override
	public int getY()
	{
		return _y;
	}
	
	public void setY(int y)
	{
		_y = y;
	}
	
	@Override
	public int getZ()
	{
		return _z;
	}
	
	public void setZ(int z)
	{
		_z = z;
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}
	
	@Override
	public Location getLocation()
	{
		return this;
	}
	
	public void setLocation(Location loc)
	{
		_x = loc.getX();
		_y = loc.getY();
		_z = loc.getZ();
		_heading = loc.getHeading();
		_instanceId = loc.getInstanceId();
	}
	
	public synchronized void setTricks(MoveTrick[] mt)
	{
		_tricks = mt;
	}
	
	public MoveTrick[] getTricks()
	{
		return _tricks;
	}
	
	public void setAll(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	public boolean equals(Location loc)
	{
		return (loc.getX() == _x) && (loc.getY() == _y) && (loc.getZ() == _z);
	}
	
	public boolean equals(int x, int y, int z)
	{
		return (_x == x) && (_y == y) && (_z == z);
	}
	
	public Location geo2world()
	{
		_x = (_x << 4) + L2World.MAP_MIN_X + 8;
		_y = (_y << 4) + L2World.MAP_MIN_Y + 8;
		return this;
	}
	
	public Location world2geo()
	{
		_x = (_x - L2World.MAP_MIN_X) >> 4;
		_y = (_y - L2World.MAP_MIN_Y) >> 4;
		return this;
	}
	
	public Location(String s) throws IllegalArgumentException
	{
		_heading = 0;
		String xyzh[] = s.replaceAll(",", " ").replaceAll(";", " ").replaceAll("  ", " ").trim().split(" ");
		if (xyzh.length < 3)
		{
			throw new IllegalArgumentException((new StringBuilder()).append("Can't parse location from string: ").append(s).toString());
		}
		_x = Integer.parseInt(xyzh[0]);
		_y = Integer.parseInt(xyzh[1]);
		_z = Integer.parseInt(xyzh[2]);
		_heading = xyzh.length >= 4 ? Integer.parseInt(xyzh[3]) : 0;
		return;
	}
	
	public static Location coordsRandomize(Location loc, int radiusmin, int radiusmax)
	{
		return coordsRandomize(loc._x, loc._y, loc._z, loc._heading, radiusmin, radiusmax);
	}
	
	public static Location coordsRandomize(int x, int y, int z, int heading, int radiusmin, int radiusmax)
	{
		if ((radiusmax == 0) || (radiusmax < radiusmin))
		{
			return new Location(x, y, z, heading);
		}
		int radius = Rnd.get(radiusmin, radiusmax);
		double angle = Rnd.nextDouble() * 2 * Math.PI;
		return new Location((int) (x + (radius * Math.cos(angle))), (int) (y + (radius * Math.sin(angle))), z, heading);
	}
	
	public Location rnd(int min, int max, boolean change)
	{
		Location loc = coordsRandomize(this, min, max);
		if (Config.GEODATA)
		{
			loc = GeoClient.getInstance().moveCheck(_x, _y, _z, loc._x, loc._y, loc._z, true);
		}
		
		if (change)
		{
			_x = loc._x;
			_y = loc._y;
			_z = loc._z;
			return this;
		}
		return loc;
	}
	
	@Override
	public String toString()
	{
		return "[" + getClass().getSimpleName() + "] X: " + _x + " Y: " + _y + " Z: " + _z + " Heading: " + _heading + " InstanceId: " + _instanceId;
	}
	
	public Location correctGeoZ()
	{
		_z = GeoClient.getInstance().getHeight(_x, _y, _z);
		return this;
	}
}