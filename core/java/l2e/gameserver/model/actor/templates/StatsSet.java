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
package l2e.gameserver.model.actor.templates;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by LordWinter 27.10.2012 Based on L2J Eternity-World
 */
public final class StatsSet
{
	private static final Logger _log = Logger.getLogger(StatsSet.class.getName());
	private final HashMap<String, Object> _set = new HashMap<>();

	public final HashMap<String, Object> getSet()
	{
		return _set;
	}

	public boolean getBool(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Boolean value required, but not specified");
		if (val instanceof Boolean)
			return (Boolean) val;
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required, but found: " + val);
		}
	}

	public boolean getBool(String name, boolean deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Boolean)
			return (Boolean) val;
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required, but found: " + val);
		}
	}

	public byte getByte(String name, byte deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number) val).byteValue();
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}

	public byte getByte(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Byte value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).byteValue();
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}

	public int getInteger(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Integer value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).intValue();
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}

	public int getInteger(String name, int deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number) val).intValue();
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}

	public int[] getIntegerArray(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Integer value required, but not specified");
		if (val instanceof Number)
		{
            return new int[]{ ((Number) val).intValue() };
		}
		int c = 0;
		String[] vals = ((String) val).split(";");
		int[] result = new int[vals.length];
		for (String v : vals)
		{
			try
			{
				result[c++] = Integer.parseInt(v);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Integer value required, but found: " + val);
			}
		}
		return result;
	}

	public long getLong(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Integer value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).longValue();
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}

    	public long getLong(String name, long deflt)
     	{
	    	Object val = _set.get(name);
	    	if (val == null)
	    	{
		   	return deflt;
	    	}
	    	else if (val instanceof Number)
	    	{
		   	return ((Number) val).longValue();
	    	}
	    	else
	    	{
		   	try
		   	{
				return Long.parseLong((String) val);
		   	}
		   	catch (Exception e)
		   	{
			  	throw new IllegalArgumentException("Integer value required, but found: " + val);
		   	}
	    	}
     	}

	public float getFloat(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Float value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).floatValue();
		try
		{
			return (float) Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}

	public float getFloat(String name, float deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (val instanceof Number)
			return ((Number) val).floatValue();
		try
		{
			return (float) Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}

	public double getDouble(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Float value required, but not specified");
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}

    	public double getDouble(String name, double deflt)
     	{
	    	Object val = _set.get(name);
	    	if (val == null)
	    	{
		   	return deflt;
	    	}
	    	else if (val instanceof Number)
	    	{
		   	return ((Number) val).doubleValue();
	    	}
	    	else
	    	{
		   	try
		   	{
			  	return Double.parseDouble((String) val);
		   	}
		   	catch (Exception e)
		   	{
			  	throw new IllegalArgumentException("Double value required, but found: " + val);
		   	}
	    	}
     	}

	public String getString(String name)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("String value required, but not specified");
		return String.valueOf(val);
	}

	public String getString(String name, String deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		return String.valueOf(val);
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass)
	{
		Object val = _set.get(name);
		if (val == null)
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but not specified");
		if (enumClass.isInstance(val))
			return (T) val;
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass, T deflt)
	{
		Object val = _set.get(name);
		if (val == null)
			return deflt;
		if (enumClass.isInstance(val))
			return (T) val;
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
		}
	}

	public Object getObject(String name)
	{
		return _set.get(name);
	}

	public void set(String name, String value)
	{
		_set.put(name, value == null ? null : value.intern());
	}

	public void set(String name, boolean value)
	{
		_set.put(name, value);
	}

	public void set(String name, int value)
	{
		_set.put(name, value);
	}

	public void safeSet(String name, int value, int min, int max, String reference)
	{
		if (min <= max && (value < min || value >= max))
		{
			_log.warning("Incorrect value: " + value + "for: " + name + "Ref: " + reference);
		}
		set(name, value);
	}

	public void set(String name, double value)
	{
		_set.put(name, value);
	}

	public void set(String name, long value)
	{
		_set.put(name, value);
	}

    	public void unset(String name)
    	{
		_set.remove(name);
    	}

	public void setObject(String name, Object value)
	{
		_set.put(name, value);
	}
}