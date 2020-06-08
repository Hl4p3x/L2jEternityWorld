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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2TeleportLocation;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TeleLocationParser extends DocumentParser
{
	private static Logger _log = Logger.getLogger(TeleLocationParser.class.getName());
	
	private final Map<Integer, L2TeleportLocation> _teleports = new HashMap<>();
	
	protected TeleLocationParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_teleports.clear();
		parseDatapackFile("data/teleports.xml");
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _teleports.size() + " teleport templates.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node list = getCurrentDocument().getFirstChild().getFirstChild(); list != null; list = list.getNextSibling())
		{
			if (list.getNodeName().equalsIgnoreCase("teleport"))
			{
				NamedNodeMap node = list.getAttributes();
				
				L2TeleportLocation teleport = new L2TeleportLocation();
				teleport.setTeleId(Integer.valueOf(node.getNamedItem("id").getNodeValue()));
				teleport.setLocX(Integer.valueOf(node.getNamedItem("locX").getNodeValue()));
				teleport.setLocY(Integer.valueOf(node.getNamedItem("locY").getNodeValue()));
				teleport.setLocZ(Integer.valueOf(node.getNamedItem("locZ").getNodeValue()));
				teleport.setPrice(Integer.valueOf(node.getNamedItem("price").getNodeValue()));
				teleport.setIsForNoble(Integer.valueOf(node.getNamedItem("noobless").getNodeValue()) == 1);
				teleport.setItemId(Integer.valueOf(node.getNamedItem("itemId").getNodeValue()));
				_teleports.put(teleport.getTeleId(), teleport);
			}
		}
	}
	
	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
	
	public static TeleLocationParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TeleLocationParser _instance = new TeleLocationParser();
	}
}