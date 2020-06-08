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

import java.util.Collection;

import javolution.util.FastList;

import l2e.gameserver.model.interfaces.IL2Procedure;

public class L2FastList<T> extends FastList<T>
{
	private static final long serialVersionUID = 8354641653178203420L;
	
	public L2FastList()
	{
		this(false);
	}
	
	public L2FastList(int initialCapacity)
	{
		this(initialCapacity, false);
	}
	
	public L2FastList(Collection<? extends T> c)
	{
		this(c, false);
	}
	
	public L2FastList(boolean shared)
	{
		super();
		if (shared)
		{
			shared();
		}
	}
	
	public L2FastList(int initialCapacity, boolean shared)
	{
		super(initialCapacity);
		if (shared)
		{
			shared();
		}
	}
	
	public L2FastList(Collection<? extends T> c, boolean shared)
	{
		super(c);
		if (shared)
		{
			shared();
		}
	}
	
	public boolean executeForEach(IL2Procedure<T> proc)
	{
		for (T e : this)
		{
			if (!proc.execute(e))
			{
				return false;
			}
		}
		return true;
	}
}