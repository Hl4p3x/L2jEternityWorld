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
import l2e.geoserver.geodata.PathFindBuffers.GeoNode;
import l2e.gameserver.model.Location;

/**
 * Created by LordWinter 10.09.2013 Based on L2J Eternity-World
 */
public class PathFind
{
	private static final byte NSWE_NONE = 0, EAST = 1, WEST = 2, SOUTH = 4, NORTH = 8, NSWE_ALL = 15;
	private Vector<Location> path;
	private PathFindBuffers.PathFindBuffer buff;
	
	public PathFind(int x, int y, int z, int destX, int destY, int destZ, GeoEngine engine)
	{
		Location startpoint = engine.moveCheckWithCollision(x, y, z, destX, destY, true, true);
		Location endpoint = new Location(destX, destY, destZ);
		startpoint.world2geo();
		endpoint.world2geo();
		startpoint.setZ(engine.getHeightGeo(startpoint.getX(), startpoint.getY(), startpoint.getZ()));
		endpoint.setZ(engine.getHeightGeo(endpoint.getX(), endpoint.getY(), endpoint.getZ()));
		int xdiff = Math.abs(endpoint.getX() - startpoint.getX());
		int ydiff = Math.abs(endpoint.getY() - startpoint.getY());
		if ((xdiff == 0) && (ydiff == 0))
		{
			if (Math.abs(endpoint.getZ() - startpoint.getZ()) < 32)
			{
				path = new Vector<>();
			}
			return;
		}
		if ((buff = PathFindBuffers.alloc(64 + (2 * Math.max(xdiff, ydiff)), startpoint, endpoint)) != null)
		{
			path = findPath();
			buff.free();
		}
	}
	
	public Vector<Location> findPath()
	{
		buff.firstNode = PathFindBuffers.GeoNode.initNode(buff, buff.startpoint.getX() - buff.offsetX, buff.startpoint.getY() - buff.offsetY, buff.startpoint);
		buff.firstNode.closed = true;
		PathFindBuffers.GeoNode nextNode = buff.firstNode;
		PathFindBuffers.GeoNode finish = null;
		int i = buff.info.maxIterations;
		while ((nextNode != null) && (i-- > 0))
		{
			if ((finish = handleNode(nextNode)) != null)
			{
				return tracePath(finish);
			}
			nextNode = getBestOpenNode();
		}
		return null;
	}
	
	private PathFindBuffers.GeoNode getBestOpenNode()
	{
		PathFindBuffers.GeoNode bestNodeLink = null;
		PathFindBuffers.GeoNode oldNode = buff.firstNode;
		for (PathFindBuffers.GeoNode nextNode = buff.firstNode.link; nextNode != null; nextNode = oldNode.link)
		{
			if ((bestNodeLink == null) || (nextNode.score < bestNodeLink.link.score))
			{
				bestNodeLink = oldNode;
			}
			oldNode = nextNode;
		}
		
		if (bestNodeLink != null)
		{
			bestNodeLink.link.closed = true;
			PathFindBuffers.GeoNode bestNode = bestNodeLink.link;
			bestNodeLink.link = bestNode.link;
			if (bestNode == buff.currentNode)
			{
				buff.currentNode = bestNodeLink;
			}
			return bestNode;
		}
		return null;
	}
	
	private Vector<Location> tracePath(GeoNode f)
	{
		Vector<Location> locations = new Vector<>();
		do
		{
			locations.add(0, f.getLoc());
			f = f.parent;
		}
		while (f.parent != null);
		return locations;
	}
	
	public PathFindBuffers.GeoNode handleNode(GeoNode node)
	{
		PathFindBuffers.GeoNode result = null;
		int clX = node._x;
		int clY = node._y;
		short clZ = node._z;
		if (!getHeightAndNSWE(clX, clY, clZ))
		{
			return result;
		}
		short NSWE = buff.hNSWE[1];
		
		if (Config.PATHFIND_DIAGONAL)
		{
			if (((NSWE & SOUTH) == SOUTH) && ((NSWE & EAST) == EAST))
			{
				if (getHeightAndNSWE(clX + 1, clY, clZ))
				{
					if ((buff.hNSWE[1] & SOUTH) == SOUTH)
					{
						if (getHeightAndNSWE(clX, clY + 1, clZ))
						{
							if ((buff.hNSWE[1] & EAST) == EAST)
							{
								result = getNeighbour(clX + 1, clY + 1, node, true);
								if (result != null)
								{
									return result;
								}
							}
						}
					}
				}
			}
			
			if (((NSWE & SOUTH) == SOUTH) && ((NSWE & WEST) == WEST))
			{
				if (getHeightAndNSWE(clX - 1, clY, clZ))
				{
					if ((buff.hNSWE[1] & SOUTH) == SOUTH)
					{
						if (getHeightAndNSWE(clX, clY + 1, clZ))
						{
							if ((buff.hNSWE[1] & WEST) == WEST)
							{
								result = getNeighbour(clX - 1, clY + 1, node, true);
								if (result != null)
								{
									return result;
								}
							}
						}
					}
				}
			}
			
			if (((NSWE & NORTH) == NORTH) && ((NSWE & EAST) == EAST))
			{
				if (getHeightAndNSWE(clX + 1, clY, clZ))
				{
					if ((buff.hNSWE[1] & NORTH) == NORTH)
					{
						if (getHeightAndNSWE(clX, clY - 1, clZ))
						{
							if ((buff.hNSWE[1] & EAST) == EAST)
							{
								result = getNeighbour(clX + 1, clY - 1, node, true);
								if (result != null)
								{
									return result;
								}
							}
						}
					}
				}
			}
			
			if (((NSWE & NORTH) == NORTH) && ((NSWE & WEST) == WEST))
			{
				if (getHeightAndNSWE(clX - 1, clY, clZ))
				{
					if ((buff.hNSWE[1] & NORTH) == NORTH)
					{
						if (getHeightAndNSWE(clX, clY - 1, clZ))
						{
							if ((buff.hNSWE[1] & WEST) == WEST)
							{
								result = getNeighbour(clX - 1, clY - 1, node, true);
								if (result != null)
								{
									return result;
								}
							}
						}
					}
				}
			}
		}
		
		if ((NSWE & EAST) == EAST)
		{
			result = getNeighbour(clX + 1, clY, node, false);
			if (result != null)
			{
				return result;
			}
		}
		
		if ((NSWE & WEST) == WEST)
		{
			result = getNeighbour(clX - 1, clY, node, false);
			if (result != null)
			{
				return result;
			}
		}
		
		if ((NSWE & SOUTH) == SOUTH)
		{
			result = getNeighbour(clX, clY + 1, node, false);
			if (result != null)
			{
				return result;
			}
		}
		
		if ((NSWE & NORTH) == NORTH)
		{
			result = getNeighbour(clX, clY - 1, node, false);
		}
		return result;
	}
	
	public PathFindBuffers.GeoNode getNeighbour(int x, int y, PathFindBuffers.GeoNode from, boolean d)
	{
		int nX = x - buff.offsetX, nY = y - buff.offsetY;
		
		if ((nX >= buff.info.MapSize) || (nX < 0) || (nY >= buff.info.MapSize) || (nY < 0))
		{
			return null;
		}
		
		boolean isOldNull = PathFindBuffers.GeoNode.isNull(buff.nodes[nX][nY]);
		
		if (!isOldNull && buff.nodes[nX][nY].closed)
		{
			return null;
		}
		PathFindBuffers.GeoNode n = ((isOldNull) ? PathFindBuffers.GeoNode.initNode(buff, nX, nY, x, y, from._z, 0, from) : buff.tempNode.reuse(buff.nodes[nX][nY], from));
		
		int height = Math.abs(n._z - from._z);
		
		if ((height > Config.PATHFIND_MAX_Z_DIFF) || (n._nswe == NSWE_NONE))
		{
			return null;
		}
		
		double weight = d ? 1.414213562373095 * 0.5 : 0.5;
		
		if ((n._nswe != NSWE_ALL) || (height > 16))
		{
			weight += 2.0;
		}
		if (getHeightAndNSWE(x + 1, y, n._z))
		{
			if ((buff.hNSWE[1] != NSWE_ALL) || (Math.abs(n._z - buff.hNSWE[0]) > 16))
			{
				weight += 1.0;
			}
		}
		if (getHeightAndNSWE(x - 1, y, n._z))
		{
			if ((buff.hNSWE[1] != NSWE_ALL) || (Math.abs(n._z - buff.hNSWE[0]) > 16))
			{
				weight += 1.0;
			}
		}
		if (getHeightAndNSWE(x, y + 1, n._z))
		{
			if ((buff.hNSWE[1] != NSWE_ALL) || (Math.abs(n._z - buff.hNSWE[0]) > 16))
			{
				weight += 1.0;
			}
		}
		if (getHeightAndNSWE(x, y - 1, n._z))
		{
			if ((buff.hNSWE[1] != NSWE_ALL) || (Math.abs(n._z - buff.hNSWE[0]) > 16))
			{
				weight += 1.0;
			}
		}
		
		int diffx = buff.endpoint.getX() - x;
		int diffy = buff.endpoint.getY() - y;
		int dz = Math.abs(buff.endpoint.getZ() - n._z);
		
		n.moveCost += from.moveCost + weight;
		n.score = n.moveCost + (Config.PATHFIND_DIAGONAL ? Math.sqrt((diffx * diffx) + (diffy * diffy) + ((dz * dz) / 256)) : Math.abs(diffx) + Math.abs(diffy) + (dz / 16));
		if ((x == buff.endpoint.getX()) && (y == buff.endpoint.getY()) && (dz < Config.PATHFIND_MAX_Z_DIFF))
		{
			return n;
		}
		if (isOldNull)
		{
			if (buff.currentNode == null)
			{
				buff.firstNode.link = n;
			}
			else
			{
				buff.currentNode.link = n;
			}
			buff.currentNode = n;
		}
		else if (n.moveCost < buff.nodes[nX][nY].moveCost)
		{
			buff.nodes[nX][nY].copy(n);
		}
		return null;
	}
	
	private boolean getHeightAndNSWE(int x, int y, short z)
	{
		int nX = x - buff.offsetX;
		int nY = y - buff.offsetY;
		if ((nX >= buff.info.MapSize) || (nX < 0) || (nY >= buff.info.MapSize) || (nY < 0))
		{
			buff.hNSWE[1] = 0;
			return true;
		}
		PathFindBuffers.GeoNode n = buff.nodes[nX][nY];
		if (n == null)
		{
			n = PathFindBuffers.GeoNode.initNodeGeo(buff, nX, nY, x, y, z);
		}
		
		if ((n != null) && ((n._z - z) <= 16))
		{
			buff.hNSWE[0] = n._z;
			buff.hNSWE[1] = n._nswe;
			return true;
		}
		return false;
	}
	
	public Vector<Location> getPath()
	{
		return path;
	}
}