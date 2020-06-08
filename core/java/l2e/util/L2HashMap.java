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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.model.interfaces.IL2EntryProcedure;
import l2e.gameserver.model.interfaces.IL2Procedure;

public class L2HashMap<K, V> extends HashMap<K, V>
{
	private static final long serialVersionUID = 8503855490858805336L;
	
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	
	public L2HashMap()
	{
		super();
	}
	
	public L2HashMap(Map<? extends K, ? extends V> map)
	{
		super(map);
	}
	
	public L2HashMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}
	
	public L2HashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
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