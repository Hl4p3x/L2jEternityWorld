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

import java.util.ArrayList;
import java.util.Collection;

import l2e.gameserver.model.interfaces.IL2Procedure;

public class L2ArrayList<T> extends ArrayList<T>
{
	private static final long serialVersionUID = 8354641653178203420L;
	
	public L2ArrayList()
	{
		super();
	}
	
	public L2ArrayList(Collection<? extends T> c)
	{
		super(c);
	}
	
	public L2ArrayList(int initialCapacity)
	{
		super(initialCapacity);
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