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
package l2e.util;

import java.util.Map;

import javolution.util.FastMap;

import l2e.gameserver.model.interfaces.IL2EntryProcedure;
import l2e.gameserver.model.interfaces.IL2Procedure;

public class L2FastMap<K, V> extends FastMap<K, V>
{
	private static final long serialVersionUID = 8503855490858805336L;
	
	public L2FastMap()
	{
		this(false);
	}
	
	public L2FastMap(Map<? extends K, ? extends V> map)
	{
		this(map, false);
	}
	
	public L2FastMap(int initialCapacity)
	{
		this(initialCapacity, false);
	}
	
	public L2FastMap(boolean shared)
	{
		super();
		if (shared)
		{
			shared();
		}
	}
	
	public L2FastMap(Map<? extends K, ? extends V> map, boolean shared)
	{
		super(map);
		if (shared)
		{
			shared();
		}
	}
	
	public L2FastMap(int initialCapacity, boolean shared)
	{
		super(initialCapacity);
		if (shared)
		{
			shared();
		}
	}
	
	public boolean executeForEachEntry(IL2EntryProcedure<K, V> proc)
	{
		for (Map.Entry<K, V> e : entrySet())
		{
			if (!proc.execute(e.getKey(), e.getValue()))
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean executeForEachKey(IL2Procedure<K> proc)
	{
		for (K k : keySet())
		{
			if (!proc.execute(k))
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean executeForEachValue(IL2Procedure<V> proc)
	{
		for (V v : values())
		{
			if (!proc.execute(v))
			{
				return false;
			}
		}
		return true;
	}
}