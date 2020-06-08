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
package l2e.gameserver.model.variables;

import java.util.concurrent.atomic.AtomicBoolean;

import javolution.util.FastMap;

import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.interfaces.IRestorable;
import l2e.gameserver.model.interfaces.IStorable;

public abstract class AbstractVariables extends StatsSet implements IRestorable, IStorable
{
	private final AtomicBoolean _hasChanges = new AtomicBoolean(false);

	public AbstractVariables()
	{
		super(new FastMap<String, Object>().shared());
	}
	
	@Override
	public final void set(String name, boolean value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, double value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, Enum<?> value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, int value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, long value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	@Override
	public final void set(String name, String value)
	{
		_hasChanges.compareAndSet(false, true);
		super.set(name, value);
	}
	
	public boolean hasVariable(String name)
	{
		return getSet().keySet().contains(name);
	}
	
	public final boolean hasChanges()
	{
		return _hasChanges.get();
	}
	
	public final boolean compareAndSetChanges(boolean expect, boolean update)
	{
		return _hasChanges.compareAndSet(expect, update);
	}
	
	public final void remove(String name)
	{
		_hasChanges.compareAndSet(false, true);
		getSet().remove(name);
	}
}