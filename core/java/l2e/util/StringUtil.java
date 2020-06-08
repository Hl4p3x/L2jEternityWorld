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

import javolution.text.TextBuilder;

import l2e.Config;

public final class StringUtil
{
	private StringUtil()
	{
	}
	
	public static String concat(final String... strings)
	{
		final TextBuilder sbString = TextBuilder.newInstance();
		
		for (final String string : strings)
		{
			sbString.append(string);
		}
		
		String result = sbString.toString();
		TextBuilder.recycle(sbString);
		return result;
	}
	
	public static StringBuilder startAppend(final int sizeHint, final String... strings)
	{
		final int length = getLength(strings);
		final StringBuilder sbString = new StringBuilder(sizeHint > length ? sizeHint : length);
		
		for (final String string : strings)
		{
			sbString.append(string);
		}
		
		return sbString;
	}
	
	public static void append(final StringBuilder sbString, final String... strings)
	{
		sbString.ensureCapacity(sbString.length() + getLength(strings));
		
		for (final String string : strings)
		{
			sbString.append(string);
		}
	}
	
	private static int getLength(final String[] strings)
	{
		int length = 0;
		
		for (final String string : strings)
		{
			if (string == null)
			{
				length += 4;
			}
			else
			{
				length += string.length();
			}
		}
		
		return length;
	}
	
	public static String getTraceString(StackTraceElement[] trace)
	{
		final TextBuilder sbString = TextBuilder.newInstance();
		for (final StackTraceElement element : trace)
		{
			sbString.append(element.toString()).append(Config.EOL);
		}
		
		String result = sbString.toString();
		TextBuilder.recycle(sbString);
		return result;
	}
}