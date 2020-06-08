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
package l2e.gameserver.data.xml;

import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2PhantomLoc;
import l2e.util.Rnd;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class PhantomLocationParser extends DocumentParser
{
	private static Logger _log = Logger.getLogger(PhantomLocationParser.class.getName());
	
	private static FastList<L2PhantomLoc> _locations = new FastList<>();
	
	protected PhantomLocationParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_locations.clear();
		parseDatapackFile("data/stats/chars/phantoms/phantomLocations.xml");
		_log.log(Level.INFO, "[PhantomListener]: Loaded " + _locations.size() + " phantom locations.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node list = getCurrentDocument().getFirstChild().getFirstChild(); list != null; list = list.getNextSibling())
		{
			if (list.getNodeName().equalsIgnoreCase("location"))
			{
				NamedNodeMap node = list.getAttributes();
				
				L2PhantomLoc loc = new L2PhantomLoc();
				loc.setLocX(Integer.valueOf(node.getNamedItem("locX").getNodeValue()));
				loc.setLocY(Integer.valueOf(node.getNamedItem("locY").getNodeValue()));
				loc.setLocZ(Integer.valueOf(node.getNamedItem("locZ").getNodeValue()));
				Node attr = node.getNamedItem("grade");
				loc.setGrade((attr != null) ? Integer.valueOf(attr.getNodeValue()) : 0);
				Node attrs = node.getNamedItem("town");
				loc.setTown((attrs != null) ? attrs.getNodeValue() : null);
				_locations.add(loc);
			}
		}
	}
	
	public L2PhantomLoc getRandomLoc()
	{
		L2PhantomLoc loc = null;
		if (loc == null)
		{
			loc = _locations.get(Rnd.get(0, _locations.size() - 1));
		}
		return loc;
	}
	
	public static PhantomLocationParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PhantomLocationParser _instance = new PhantomLocationParser();
	}
}