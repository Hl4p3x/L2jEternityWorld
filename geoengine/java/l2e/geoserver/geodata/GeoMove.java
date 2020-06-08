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
package l2e.geoserver.geodata;

import java.util.Vector;

import l2e.Config;
import l2e.gameserver.model.Location;

/**
 * Created by LordWinter 10.09.2013 Based on L2J Eternity-World
 */
public class GeoMove
{
	private final GeoEngine engine;
	
	public GeoMove(GeoEngine engine)
	{
		this.engine = engine;
	}
	
	public Vector<Location> pathFind(int x, int y, int z, int tx, int ty, int tz)
	{
		Location target = new Location(tx, ty, tz);
		z = engine.getHeight(x, y, z);
		if (Math.abs(z - target.getZ()) > 256)
		{
			return null;
		}
		target.setZ(engine.getHeight(target));
		PathFind n = new PathFind(x, y, z, target.getX(), target.getY(), target.getZ(), engine);
		if ((n.getPath() == null) || n.getPath().isEmpty())
		{
			return null;
		}
		for (Location p : n.getPath())
		{
			p.geo2world();
		}
		
		n.getPath().add(0, new Location(x, y, z));
		n.getPath().addElement(target);
		
		if (Config.PATH_CLEAN)
		{
			pathClean(n.getPath());
		}
		
		if (n.getPath().size() > 0)
		{
			n.getPath().removeElementAt(0);
		}
		return n.getPath();
	}
	
	@SuppressWarnings("unused")
	private void pathClean(Vector<Location> path)
	{
		int size = path.size();
		if (size > 2)
		{
			for (int i = 2; i < size; i++)
			{
				Location p3 = path.elementAt(i);
				Location p2 = path.elementAt(i - 1);
				Location p1 = path.elementAt(i - 2);
				if (p1.equals(p2) || p3.equals(p2) || IsPointInLine(p1.getX(), p1.getY(), p3.getX(), p3.getY(), p2))
				{
					path.remove(i - 1);
					size--;
					i = Math.max(2, i - 2);
				}
			}
			
		}
		
		for (int current = 0; current < (path.size() - 2); current++)
		{
			for (int sub = current + 2; sub < path.size(); sub++)
			{
				Location one = path.elementAt(current);
				Location two = path.elementAt(sub);
				if (!one.equals(two) && !engine.canMoveToCoord(one.getX(), one.getY(), one.getZ(), two.getX(), two.getY(), two.getZ(), false))
				{
					continue;
				}
				
				for (; (current + 1) < sub; sub--)
				{
					path.remove(current + 1);
				}
			}
		}
		
		for (int current = 0; current < (path.size() - 2); current++)
		{
			Location one = path.elementAt(current);
			Location two = path.elementAt(current + 1);
			two.setTricks(engine.canMoveToTargetWithCollision(one.getX(), one.getY(), one.getZ(), two.getX(), two.getY(), two.getZ(), false, true));
			
			if (two.getTricks() == null)
			{
				if (current == 0)
				{
					path.clear();
				}
				else
				{
					for (int sub = current + 1; (current + 1) < path.size(); sub++)
					{
						path.remove(current + 1);
					}
				}
				return;
			}
		}
	}
	
	private static boolean IsPointInLine(int x1, int y1, int x2, int y2, Location P)
	{
		if (((x1 == x2) && (x2 == P.getX())) || ((y1 == y2) && (y2 == P.getY())))
		{
			return true;
		}
		return ((x1 - P.getX()) * (y1 - P.getY())) == ((P.getX() - x2) * (P.getY() - y2));
	}
}