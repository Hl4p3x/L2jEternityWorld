/* This program is free software: you can redistribute it and/or modify it under
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

/**
 * Created by LordWinter 30.01.2011
 */
public class Strings
{
	public static String stripSlashes(String s)
	{
		if (s == null)
			return "";
		s = s.replace("\\'", "'");
		s = s.replace("\\\\", "\\");
		return s;
	}

	public static String addSlashes(String s)
	{
		if (s == null)
			return "";
		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		s = s.replace("@", "\\@");
		s = s.replace("'", "\\'");
		return s;
	}
}