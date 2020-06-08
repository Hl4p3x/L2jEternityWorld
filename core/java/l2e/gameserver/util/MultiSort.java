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
package l2e.gameserver.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

public class MultiSort
{
	public static final int SORT_ASCENDING = 0;
	public static final int SORT_DESCENDING = 1;
	
	private List<?> _keyList;
	private List<Integer> _valueList;
	
	private boolean _isSortDescending;
	private boolean _isSorted;
	
	public MultiSort(int[] valueList)
	{
		_valueList = getIntList(valueList);
	}
	
	public MultiSort(Collection<Integer> valueList)
	{
		_valueList = getIntList(valueList);
	}
	
	public MultiSort(Object[] keyList, int[] valueList)
	{
		_keyList = getList(keyList);
		_valueList = getIntList(valueList);
	}
	
	public MultiSort(Map<?, Integer> valueMap)
	{
		_keyList = getList(valueMap.keySet());
		_valueList = getIntList(valueMap.values());
	}
	
	private final List<Integer> getIntList(Collection<Integer> valueList)
	{
		return Arrays.asList(valueList.toArray(new Integer[valueList.size()]));
	}
	
	private final List<Integer> getIntList(int[] valueList)
	{
		Integer[] tempIntList = new Integer[valueList.length];
		
		for (int i = 0; i < valueList.length; i++)
			tempIntList[i] = Integer.valueOf(valueList[i]);
		
		return Arrays.asList(tempIntList);
	}
	
	private final List<?> getList(Collection<?> valueList)
	{
		return getList(valueList.toArray(new Object[valueList.size()]));
	}
	
	private final List<Object> getList(Object[] valueList)
	{
		return Arrays.asList(valueList);
	}
	
	public final int getCount()
	{
		return getValues().size();
	}
	
	public final int getHarmonicMean()
	{
		if (getValues().isEmpty())
			return -1;
		
		int totalValue = 0;
		
		for (int currValue : getValues())
			totalValue += (1 / currValue);
		
		return (getCount() / totalValue);
	}
	
	public final List<?> getKeys()
	{
		if (_keyList == null)
			return new FastList<>();
		
		return _keyList;
	}
	
	public final int getFrequency(int checkValue)
	{
		return Collections.frequency(getValues(), checkValue);
	}
	
	public final int getMaxValue()
	{
		return Collections.max(getValues());
	}
	
	public final int getMinValue()
	{
		return Collections.min(getValues());
	}
	
	public final int getMean()
	{
		if (getValues().isEmpty())
			return -1;
		
		return (getTotalValue() / getCount());
	}
	
	public final double getStandardDeviation()
	{
		if (getValues().isEmpty())
			return -1;
		
		List<Double> tempValList = new FastList<>();
		
		int meanValue = getMean();
		int numValues = getCount();
		
		for (int value : getValues())
		{
			double adjValue = Math.pow(value - meanValue, 2);
			tempValList.add(adjValue);
		}
		
		double totalValue = 0;
		
		for (double storedVal : tempValList)
			totalValue += storedVal;
		
		return Math.sqrt(totalValue / (numValues - 1));
	}
	
	public final int getTotalValue()
	{
		if (getValues().isEmpty())
			return 0;
		
		int totalValue = 0;
		
		for (int currValue : getValues())
			totalValue += currValue;
		
		return totalValue;
	}
	
	public final List<Integer> getValues()
	{
		if (_valueList == null)
			return new FastList<>();
		
		return _valueList;
	}
	
	public final boolean isSortDescending()
	{
		return _isSortDescending;
	}
	
	public final boolean isSorted()
	{
		return _isSorted;
	}
	
	public final void setSortDescending(boolean isDescending)
	{
		_isSortDescending = isDescending;
	}
	
	public boolean sort()
	{
		try
		{
			List<Object> newKeyList = new FastList<>();
			List<Integer> newValueList = new FastList<>();
			
			Collections.sort(getValues());
			
			int lastValue = 0;
			
			if (!isSortDescending())
			{
				if (getKeys().isEmpty())
					return true;
				
				for (int i = getValues().size() - 1; i > -1; i--)
				{
					int currValue = getValues().get(i);

					if (currValue == lastValue)
						continue;
					
					lastValue = currValue;
					
					for (int j = 0; j < getKeys().size(); j++)
					{
						Object currKey = getKeys().get(j);
						
						if (getValues().get(j) == currValue)
						{
							newKeyList.add(currKey);
							newValueList.add(currValue);
						}
					}
				}
			}
			else
			{
				if (getKeys().isEmpty())
				{
					Collections.reverse(getValues());
					return true;
				}
				
				for (int i = 0; i < getValues().size(); i++)
				{
					int currValue = getValues().get(i);
					
					if (currValue == lastValue)
						continue;
					
					lastValue = currValue;
					
					for (int j = 0; j < getKeys().size(); j++)
					{
						Object currKey = getKeys().get(j);
						
						if (getValues().get(j) == currValue)
						{
							newKeyList.add(currKey);
							newValueList.add(currValue);
						}
					}
				}
			}
			
			_keyList = newKeyList;
			_valueList = newValueList;
			_isSorted = true;
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}