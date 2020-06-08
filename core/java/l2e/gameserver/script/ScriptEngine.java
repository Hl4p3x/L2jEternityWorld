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
package l2e.gameserver.script;

import java.util.Hashtable;

public class ScriptEngine
{
	public static final Hashtable<String, ParserFactory> parserFactories = new Hashtable<>();
	
	protected static Parser createParser(String name) throws ParserNotCreatedException
	{
		ParserFactory s = parserFactories.get(name);
		if (s == null)
		{
			try
			{
				Class.forName("l2e.gameserver.script." + name);
				
				s = parserFactories.get(name);
				if (s == null)
					throw (new ParserNotCreatedException());
			}
			catch (ClassNotFoundException e)
			{
				throw (new ParserNotCreatedException());
			}
		}
		return (s.create());
	}
}