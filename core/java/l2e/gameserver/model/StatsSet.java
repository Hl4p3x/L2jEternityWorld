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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

public class StatsSet
{
	private static final Logger _log = Logger.getLogger(StatsSet.class.getName());
	private final Map<String, Object> _set;
	
	public StatsSet()
	{
		this(new FastMap<String, Object>());
	}
	
	public StatsSet(Map<String, Object> map)
	{
		_set = map;
	}
	
	public final Map<String, Object> getSet()
	{
		return _set;
	}
	
	public void add(StatsSet newSet)
	{
		Map<String, Object> newMap = newSet.getSet();
		for (Entry<String, Object> entry : newMap.entrySet())
		{
			_set.put(entry.getKey(), entry.getValue());
		}
	}
	
	public boolean getBool(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Boolean value required, but not specified");
		}
		if (val instanceof Boolean)
		{
			return ((Boolean) val).booleanValue();
		}
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
		{
			return deflt;
		}
		if (val instanceof Boolean)
		{
			return ((Boolean) val).booleanValue();
		}
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
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).byteValue();
		}
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
		{
			throw new IllegalArgumentException("Byte value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).byteValue();
		}
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}
	
	public byte[] getByteArray(String name, String splitOn)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Byte value required, but not specified");
		}
		if (val instanceof Number)
		{
			byte[] result =
			{
				((Number) val).byteValue()
			};
			return result;
		}
		int c = 0;
		String[] vals = ((String) val).split(splitOn);
		byte[] result = new byte[vals.length];
		for (String v : vals)
		{
			try
			{
				result[c++] = Byte.parseByte(v);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Byte value required, but found: " + val);
			}
		}
		return result;
	}
	
	public List<Byte> getByteList(String name, String splitOn)
	{
		List<Byte> result = new ArrayList<>();
		for (Byte i : getByteArray(name, splitOn))
		{
			result.add(i);
		}
		return result;
	}
	
	public short getShort(String name, short deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).shortValue();
		}
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	public short getShort(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Short value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).shortValue();
		}
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	public int getInteger(String name)
	{
		final Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified: " + name + "!");
		}
		
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val + "!");
		}
	}
	
	public int getInteger(String name, int deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	public int[] getIntegerArray(String name, String splitOn)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified");
		}
		if (val instanceof Number)
		{
			int[] result =
			{
				((Number) val).intValue()
			};
			return result;
		}
		int c = 0;
		String[] vals = ((String) val).split(splitOn);
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
	
	public List<Integer> getIntegerList(String name, String splitOn)
	{
		List<Integer> result = new ArrayList<>();
		for (int i : getIntegerArray(name, splitOn))
		{
			result.add(i);
		}
		return result;
	}
	
	public long getLong(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
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
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	public float getFloat(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Float value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
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
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
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
		{
			throw new IllegalArgumentException("Float value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
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
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	public String getString(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("String value required, but not specified");
		}
		return String.valueOf(val);
	}
	
	public String getString(String name, String deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		return String.valueOf(val);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but not specified");
		}
		if (enumClass.isInstance(val))
		{
			return (T) val;
		}
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but found: " + val);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass, T deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (enumClass.isInstance(val))
		{
			return (T) val;
		}
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
		}
	}
	
	public void set(String name, String value)
	{
		_set.put(name, value);
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
		assert !(((min <= max) && ((value < min) || (value >= max))));
		if ((min <= max) && ((value < min) || (value >= max)))
		{
			_log.log(Level.SEVERE, "Incorrect value: " + value + "for: " + name + "Ref: " + reference);
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
	
	public void set(String name, Enum<?> value)
	{
		_set.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public final <A> A getObject(String name, Class<A> type)
	{
		Object obj = _set.get(name);
		if ((obj == null) || !type.isAssignableFrom(obj.getClass()))
		{
			return null;
		}
		return (A) obj;
	}
}