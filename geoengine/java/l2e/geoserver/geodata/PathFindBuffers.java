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

import java.util.Iterator;

import javolution.util.FastMap;
import javolution.util.FastTable;
import l2e.gameserver.model.Location;

/**
 * Created by LordWinter 10.09.2013 Based on L2J Eternity-World
 */
public class PathFindBuffers
{
	public static class BufferInfo
	{
		
		final int MapSize;
		final int sqMapSize;
		final int maxIterations;
		final int index;
		protected int overBuffers;
		protected int totalUses;
		protected double useTimeMillis;
		protected FastTable<PathFindBuffer> buffers;
		
		public BufferInfo(int mapSize, int idx, int buffersCount)
		{
			overBuffers = 0;
			totalUses = 0;
			useTimeMillis = 0.0D;
			buffers = new FastTable<>(buffersCount);
			MapSize = mapSize;
			sqMapSize = mapSize * mapSize;
			index = idx;
			if (sqMapSize <= 10000)
			{
				maxIterations = sqMapSize / 2;
			}
			else if (sqMapSize < 30000)
			{
				maxIterations = sqMapSize / 3;
			}
			else
			{
				maxIterations = sqMapSize / 4;
			}
		}
	}
	
	public static class GeoNode
	{
		
		public void free()
		{
			score = -1D;
			link = null;
			parent = null;
			_z = 0;
		}
		
		public static GeoNode initNode(PathFindBuffer buff, int bx, int by, int x, int y, short z, double mCost, GeoNode parentNode)
		{
			GeoNode result;
			if (buff == null)
			{
				result = new GeoNode();
				result._x = x;
				result._y = y;
				result._z = z;
				result.moveCost = mCost;
				result.parent = parentNode;
				result.score = 0.0D;
				result.closed = false;
				return result;
			}
			if (buff.nodes[bx][by] == null)
			{
				buff.nodes[bx][by] = new GeoNode();
			}
			result = buff.nodes[bx][by];
			if ((result._x != x) || (result._y != y) || (result._z == 0) || (Math.abs(z - result._z) > 64))
			{
				GeoEngine.NgetHeightAndNSWE(x, y, z, buff.hNSWE);
				result._x = x;
				result._y = y;
				result._z = buff.hNSWE[0];
				result._nswe = buff.hNSWE[1];
			}
			result.moveCost = mCost;
			result.parent = parentNode;
			result.score = 0.0D;
			result.closed = false;
			return result;
		}
		
		public static GeoNode initNode(PathFindBuffer buff, int bx, int by, Location loc)
		{
			return initNode(buff, bx, by, loc.getX(), loc.getY(), (short) loc.getZ(), 0.0D, null);
		}
		
		public static boolean isNull(GeoNode node)
		{
			return (node == null) || (node.score == -1D);
		}
		
		public static GeoNode initNodeGeo(PathFindBuffer buff, int bx, int by, int x, int y, short z)
		{
			if (buff.nodes[bx][by] == null)
			{
				buff.nodes[bx][by] = new GeoNode();
			}
			GeoNode result = buff.nodes[bx][by];
			GeoEngine.NgetHeightAndNSWE(x, y, z, buff.hNSWE);
			result._x = x;
			result._y = y;
			result._z = buff.hNSWE[0];
			result._nswe = buff.hNSWE[1];
			result.score = -1D;
			return result;
		}
		
		public GeoNode reuse(GeoNode old, GeoNode parentNode)
		{
			_x = old._x;
			_y = old._y;
			_z = old._z;
			_nswe = old._nswe;
			moveCost = 0.0D;
			closed = old.closed;
			parent = parentNode;
			return this;
		}
		
		public void copy(GeoNode old)
		{
			_x = old._x;
			_y = old._y;
			_z = old._z;
			_nswe = old._nswe;
			moveCost = old.moveCost;
			score = old.score;
			closed = old.closed;
		}
		
		public Location getLoc()
		{
			return new Location(_x, _y, _z);
		}
		
		@Override
		public String toString()
		{
			return (new StringBuilder()).append("GeoNode: ").append(_x).append("\t").append(_y).append("\t").append(_z).toString();
		}
		
		public int _x;
		public int _y;
		public short _z;
		public short _nswe;
		public double score;
		public double moveCost;
		public boolean closed;
		public GeoNode link;
		public GeoNode parent;
		
		public GeoNode()
		{
			score = 0.0D;
			moveCost = 0.0D;
			closed = false;
			link = null;
			parent = null;
		}
	}
	
	public static class PathFindBuffer
	{
		
		public synchronized void free()
		{
			if (!inUse)
			{
				return;
			}
			
			for (GeoNode[] node : nodes)
			{
				for (GeoNode element : node)
				{
					if (element != null)
					{
						element.free();
					}
				}
				
			}
			
			firstNode = null;
			currentNode = null;
			endpoint = null;
			currentNode = null;
			info.totalUses++;
			info.useTimeMillis += (System.nanoTime() - useStartedNanos) / 1000000D;
			inUse = false;
		}
		
		final short hNSWE[] = new short[2];
		final GeoNode nodes[][];
		final BufferInfo info;
		boolean inUse;
		Location startpoint;
		Location endpoint;
		int offsetX;
		int offsetY;
		protected long useStartedNanos;
		GeoNode firstNode;
		GeoNode currentNode;
		GeoNode tempNode;
		
		public PathFindBuffer(BufferInfo inf)
		{
			nodes = new GeoNode[inf.MapSize][inf.MapSize];
			tempNode = new GeoNode();
			info = inf;
		}
	}
	
	public PathFindBuffers()
	{
	}
	
	public synchronized static void initBuffers(String s)
	{
		FastMap<Integer, Integer> conf_data = new FastMap<>();
		String arr$[] = s.split(";");
		int len$ = arr$.length;
		for (int i$ = 0; i$ < len$; i$++)
		{
			String e = arr$[i$];
			String k[];
			if (!e.isEmpty() && ((k = e.split("x")).length == 2))
			{
				conf_data.put(Integer.valueOf(k[1]), Integer.valueOf(k[0]));
			}
		}
		
		BufferInfo _allbuffers[] = new BufferInfo[conf_data.size()];
		for (int idx = 0; !conf_data.isEmpty(); idx++)
		{
			Integer lowestKey = null;
			Iterator<Integer> i$ = conf_data.keySet().iterator();
			do
			{
				if (!i$.hasNext())
				{
					break;
				}
				Integer ke = i$.next();
				if ((lowestKey == null) || (lowestKey.intValue() > ke.intValue()))
				{
					lowestKey = ke;
				}
			}
			while (true);
			_allbuffers[idx] = new BufferInfo(lowestKey.intValue(), idx, conf_data.remove(lowestKey).intValue());
		}
		
		all_buffers = _allbuffers;
	}
	
	public static boolean resizeBuffers(int MapSize, int newCapacity)
	{
		if (newCapacity < 1)
		{
			return false;
		}
		for (int i = 0; i < all_buffers.length; i++)
		{
			if (MapSize == all_buffers[i].MapSize)
			{
				if (newCapacity == all_buffers[i].buffers.size())
				{
					return true;
				}
				FastTable<PathFindBuffer> new_buffers = new FastTable<>(newCapacity);
				synchronized (all_buffers[i])
				{
					for (; all_buffers[i].buffers.size() > newCapacity; all_buffers[i].buffers.removeLast())
					{
					}
					new_buffers.addAll(all_buffers[i].buffers);
					all_buffers[i].buffers = new_buffers;
				}
				return true;
			}
		}
		
		return false;
	}
	
	private static PathFindBuffer alloc(BufferInfo fine_buffer)
	{
		synchronized (fine_buffer)
		{
			for (PathFindBuffer bufferinfo : fine_buffer.buffers)
			{
				PathFindBuffer b = bufferinfo;
				if (!b.inUse)
				{
					b.inUse = true;
					return b;
				}
			}
			
			PathFindBuffer result;
			result = new PathFindBuffer(fine_buffer);
			if (fine_buffer.buffers.size() < fine_buffer.buffers.size())
			{
				result.inUse = true;
				fine_buffer.buffers.add(result);
			}
			else
			{
				fine_buffer.overBuffers++;
			}
			return result;
		}
	}
	
	public static PathFindBuffer alloc(int mapSize, Location startpoint, Location endpoint)
	{
		if ((mapSize % 2) > 0)
		{
			mapSize--;
		}
		BufferInfo fine_buffer = null;
		int i = 0;
		do
		{
			if (i >= all_buffers.length)
			{
				break;
			}
			if (mapSize <= all_buffers[i].MapSize)
			{
				fine_buffer = all_buffers[i];
				mapSize = all_buffers[i].MapSize;
				break;
			}
			i++;
		}
		while (true);
		if (fine_buffer == null)
		{
			return null;
		}
		PathFindBuffer result = alloc(fine_buffer);
		result.useStartedNanos = System.nanoTime();
		result.startpoint = startpoint;
		result.endpoint = endpoint;
		result.offsetX = startpoint.getX() - (mapSize / 2);
		result.offsetY = startpoint.getY() - (mapSize / 2);
		return result;
	}
	
	private static BufferInfo all_buffers[];
}