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

import javolution.util.FastList;
import l2e.gameserver.engines.DocumentParser;
import l2e.util.Rnd;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class PhantomMessagesParser extends DocumentParser
{
	private final FastList<String> _shoutMessages = new FastList<>();
	private final FastList<String> _deadMessages = new FastList<>();
	
	protected PhantomMessagesParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_shoutMessages.clear();
		_deadMessages.clear();
		parseDatapackFile("data/stats/chars/phantoms/phantomMessages.xml");
		_log.log(Level.INFO, "[PhantomListener]: Loaded " + (_shoutMessages.size() + _deadMessages.size()) + " phantom messages...");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("phantomMessages".equalsIgnoreCase(d.getNodeName()))
					{
						parseEquipment(d);
					}
				}
			}
		}
	}
	
	private void parseEquipment(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		String message;
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("shout".equalsIgnoreCase(c.getNodeName()))
			{
				attrs = c.getAttributes();
				message = attrs.getNamedItem("message").getNodeValue();
				_shoutMessages.add(message);
			}
			else if ("dead".equalsIgnoreCase(c.getNodeName()))
			{
				attrs = c.getAttributes();
				message = attrs.getNamedItem("message").getNodeValue();
				_deadMessages.add(message);
			}
		}
	}
	
	public String getRndShoutMessage()
	{
		return _shoutMessages.get(Rnd.get(0, _shoutMessages.size() - 1));
	}
	
	public String getRndDeadMessage()
	{
		return _deadMessages.get(Rnd.get(0, _deadMessages.size() - 1));
	}
	
	public static PhantomMessagesParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PhantomMessagesParser _instance = new PhantomMessagesParser();
	}
}