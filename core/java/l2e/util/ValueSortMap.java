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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ValueSortMap
{
	public Map<Integer, Integer> sortThis(Map<Integer, Integer> map, boolean asc)
	{
		return sortMapByValue(map, asc);
	}
	
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap sortMapByValue(Map inMap, Comparator comparator)
	{
		return sortMapByValue(inMap, comparator, null);
	}
	
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap sortMapByValue(Map inMap, boolean ascendingOrder)
	{
		return sortMapByValue(inMap, null, ascendingOrder);
	}
	
	@SuppressWarnings("rawtypes")
	public static LinkedHashMap sortMapByValue(Map inMap)
	{
		return sortMapByValue(inMap, null, null);
	}
	
	@SuppressWarnings(
	{
		"rawtypes"
	})
	private static LinkedHashMap sortMapByValue(Map inMap, Comparator comparator, Boolean ascendingOrder)
	{
		int iSize = inMap.size();
		
		LinkedHashMap sortedMap = new LinkedHashMap(iSize);
		
		Collection values = inMap.values();
		ArrayList valueList = new ArrayList(values);
		HashSet distinctValues = new HashSet(values);
		
		int iNullValueCount = 0;
		if (distinctValues.contains(null))
		{
			distinctValues.remove(null);
			for (int i = 0; i < valueList.size(); i++)
			{
				if (valueList.get(i) == null)
				{
					valueList.remove(i);
					iNullValueCount++;
					i--;
					continue;
				}
			}
		}
		
		if (ascendingOrder == null)
		{
			Collections.sort(valueList, comparator);
		}
		else if (ascendingOrder)
		{
			Collections.sort(valueList);
		}
		else
		{
			Collections.sort(valueList);
			Collections.reverse(valueList);
		}
		
		boolean bAllDistinct = true;
		if (iSize != (distinctValues.size() + iNullValueCount))
		{
			bAllDistinct = false;
		}
		
		Object key = null, value = null, sortedValue;
		Set keySet = null;
		Iterator itKeyList = null;
		HashMap hmTmpMap = new HashMap(iSize);
		HashMap hmNullValueMap = new HashMap();
		
		if (bAllDistinct)
		{
			keySet = inMap.keySet();
			itKeyList = keySet.iterator();
			while (itKeyList.hasNext())
			{
				key = itKeyList.next();
				value = inMap.get(key);
				
				if (value != null)
				{
					hmTmpMap.put(value, key);
				}
				else
				{
					hmNullValueMap.put(key, value);
				}
			}
			
			if ((ascendingOrder != null) && !ascendingOrder)
			{
				sortedMap.putAll(hmNullValueMap);
			}
			
			for (int i = 0; i < valueList.size(); i++)
			{
				value = valueList.get(i);
				key = hmTmpMap.get(value);
				
				sortedMap.put(key, value);
			}
			
			if ((ascendingOrder == null) || ascendingOrder)
			{
				sortedMap.putAll(hmNullValueMap);
			}
		}
		else
		{
			keySet = inMap.keySet();
			itKeyList = keySet.iterator();
			while (itKeyList.hasNext())
			{
				key = itKeyList.next();
				value = inMap.get(key);
				
				if (value != null)
				{
					hmTmpMap.put(key, value);
				}
				else
				{
					hmNullValueMap.put(key, value);
				}
			}
			
			if ((ascendingOrder != null) && !ascendingOrder)
			{
				sortedMap.putAll(hmNullValueMap);
			}
			
			for (int i = 0; i < valueList.size(); i++)
			{
				sortedValue = valueList.get(i);
				
				keySet = hmTmpMap.keySet();
				itKeyList = keySet.iterator();
				while (itKeyList.hasNext())
				{
					key = itKeyList.next();
					value = hmTmpMap.get(key);
					if (value.equals(sortedValue))
					{
						sortedMap.put(key, value);
						hmTmpMap.remove(key);
						break;
					}
				}
			}
			
			if ((ascendingOrder == null) || ascendingOrder)
			{
				sortedMap.putAll(hmNullValueMap);
			}
		}
		return sortedMap;
	}
}