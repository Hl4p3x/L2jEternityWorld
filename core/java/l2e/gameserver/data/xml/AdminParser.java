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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javolution.util.FastMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2AccessLevel;
import l2e.gameserver.model.L2AdminCommandAccessRight;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class AdminParser extends DocumentParser
{
	private static final Map<Integer, L2AccessLevel> _accessLevels = new HashMap<>();
	private static final Map<String, L2AdminCommandAccessRight> _adminCommandAccessRights = new HashMap<>();
	private static final Map<L2PcInstance, Boolean> _gmList = new FastMap<L2PcInstance, Boolean>().shared();
	private int _highestLevel = 0;
	
	protected AdminParser()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_accessLevels.clear();
		_adminCommandAccessRights.clear();
		parseDatapackFile("data/stats/accessLevels.xml");
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _accessLevels.size() + " Access Levels");
		parseDatapackFile("data/stats/adminCommands.xml");
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _adminCommandAccessRights.size() + " Access Commands");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node attr;
		StatsSet set;
		L2AccessLevel level;
		L2AdminCommandAccessRight command;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("access".equalsIgnoreCase(d.getNodeName()))
					{
						set = new StatsSet();
						attrs = d.getAttributes();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							attr = attrs.item(i);
							set.set(attr.getNodeName(), attr.getNodeValue());
						}
						level = new L2AccessLevel(set);
						if (level.getLevel() > _highestLevel)
						{
							_highestLevel = level.getLevel();
						}
						_accessLevels.put(level.getLevel(), level);
					}
					else if ("admin".equalsIgnoreCase(d.getNodeName()))
					{
						set = new StatsSet();
						attrs = d.getAttributes();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							attr = attrs.item(i);
							set.set(attr.getNodeName(), attr.getNodeValue());
						}
						command = new L2AdminCommandAccessRight(set);
						_adminCommandAccessRights.put(command.getAdminCommand(), command);
					}
				}
			}
		}
	}
	
	public L2AccessLevel getAccessLevel(int accessLevelNum)
	{
		if (accessLevelNum < 0)
		{
			return _accessLevels.get(-1);
		}
		else if (!_accessLevels.containsKey(accessLevelNum))
		{
			_accessLevels.put(accessLevelNum, new L2AccessLevel());
		}
		return _accessLevels.get(accessLevelNum);
	}
	
	public L2AccessLevel getMasterAccessLevel()
	{
		return _accessLevels.get(_highestLevel);
	}
	
	public boolean hasAccessLevel(int id)
	{
		return _accessLevels.containsKey(id);
	}
	
	public boolean hasAccess(String adminCommand, L2AccessLevel accessLevel)
	{
		L2AdminCommandAccessRight acar = _adminCommandAccessRights.get(adminCommand);
		
		if (acar == null)
		{
			if ((accessLevel.getLevel() > 0) && (accessLevel.getLevel() == _highestLevel))
			{
				acar = new L2AdminCommandAccessRight(adminCommand, true, accessLevel.getLevel());
				_adminCommandAccessRights.put(adminCommand, acar);
				_log.info(getClass().getSimpleName() + ": No rights defined for admin command " + adminCommand + " auto setting accesslevel: " + accessLevel.getLevel() + " !");
			}
			else
			{
				_log.info(getClass().getSimpleName() + ": No rights defined for admin command " + adminCommand + " !");
				return false;
			}
		}
		
		return acar.hasAccess(accessLevel);
	}
	
	public boolean requireConfirm(String command)
	{
		final L2AdminCommandAccessRight acar = _adminCommandAccessRights.get(command);
		if (acar == null)
		{
			_log.info(getClass().getSimpleName() + ": No rights defined for admin command " + command + ".");
			return false;
		}
		return acar.getRequireConfirm();
	}
	
	public List<L2PcInstance> getAllGms(boolean includeHidden)
	{
		final List<L2PcInstance> tmpGmList = new ArrayList<>();
		
		for (Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
		{
			if (includeHidden || !entry.getValue())
			{
				tmpGmList.add(entry.getKey());
			}
		}
		
		return tmpGmList;
	}
	
	public List<String> getAllGmNames(boolean includeHidden)
	{
		final List<String> tmpGmList = new ArrayList<>();
		
		for (Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
		{
			if (!entry.getValue())
			{
				tmpGmList.add(entry.getKey().getName());
			}
			else if (includeHidden)
			{
				tmpGmList.add(entry.getKey().getName() + " (invis)");
			}
		}
		
		return tmpGmList;
	}
	
	public void addGm(L2PcInstance player, boolean hidden)
	{
		_gmList.put(player, hidden);
	}
	
	public void deleteGm(L2PcInstance player)
	{
		_gmList.remove(player);
	}
	
	public void showGm(L2PcInstance player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, false);
		}
	}
	
	public void hideGm(L2PcInstance player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, true);
		}
	}
	
	public boolean isGmOnline(boolean includeHidden)
	{
		for (Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
		{
			if (includeHidden || !entry.getValue())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void sendListToPlayer(L2PcInstance player)
	{
		if (isGmOnline(player.isGM()))
		{
			player.sendPacket(SystemMessageId.GM_LIST);
			
			for (String name : getAllGmNames(player.isGM()))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.GM_C1);
				sm.addString(name);
				player.sendPacket(sm);
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
		}
	}
	
	public void broadcastToGMs(L2GameServerPacket packet)
	{
		for (L2PcInstance gm : getAllGms(true))
		{
			gm.sendPacket(packet);
		}
	}
	
	public void broadcastMessageToGMs(String message)
	{
		for (L2PcInstance gm : getAllGms(true))
		{
			gm.sendMessage(message);
		}
	}
	
	public static AdminParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminParser _instance = new AdminParser();
	}
}