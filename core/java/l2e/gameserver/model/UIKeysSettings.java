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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.data.xml.UIParser;

public class UIKeysSettings
{
	private static final Logger _log = Logger.getLogger(UIKeysSettings.class.getName());
	
	private final int _playerObjId;
	private Map<Integer, List<ActionKey>> _storedKeys;
	private Map<Integer, List<Integer>> _storedCategories;
	private boolean _saved = true;
	
	public UIKeysSettings(int playerObjId)
	{
		_playerObjId = playerObjId;
		loadFromDB();
	}
	
	public void storeAll(Map<Integer, List<Integer>> catMap, Map<Integer, List<ActionKey>> keyMap)
	{
		_saved = false;
		_storedCategories = catMap;
		_storedKeys = keyMap;
	}
	
	public void storeCategories(Map<Integer, List<Integer>> catMap)
	{
		_saved = false;
		_storedCategories = catMap;
	}
	
	public Map<Integer, List<Integer>> getCategories()
	{
		return _storedCategories;
	}
	
	public void storeKeys(Map<Integer, List<ActionKey>> keyMap)
	{
		_saved = false;
		_storedKeys = keyMap;
	}
	
	public Map<Integer, List<ActionKey>> getKeys()
	{
		return _storedKeys;
	}
	
	public void loadFromDB()
	{
		getCatsFromDB();
		getKeysFromDB();
	}
	
	public void saveInDB()
	{
		String query;
		if (_saved)
		{
			return;
		}
		
		query = "REPLACE INTO character_ui_categories (`charId`, `catId`, `order`, `cmdId`) VALUES ";
		for (int category : _storedCategories.keySet())
		{
			int order = 0;
			for (int key : _storedCategories.get(category))
			{
				query += "(" + _playerObjId + ", " + category + ", " + (order++) + ", " + key + "),";
			}
		}
		query = query.substring(0, query.length() - 1) + "; ";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveInDB(): " + e.getMessage(), e);
		}
		
		query = "REPLACE INTO character_ui_actions (`charId`, `cat`, `order`, `cmd`, `key`, `tgKey1`, `tgKey2`, `show`) VALUES";
		for (List<ActionKey> keyLst : _storedKeys.values())
		{
			int order = 0;
			for (ActionKey key : keyLst)
			{
				query += key.getSqlSaveString(_playerObjId, order++) + ",";
			}
		}
		query = query.substring(0, query.length() - 1) + ";";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: saveInDB(): " + e.getMessage(), e);
		}
		_saved = true;
	}
	
	public void getCatsFromDB()
	{
		if (_storedCategories != null)
		{
			return;
		}
		
		_storedCategories = new HashMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM character_ui_categories WHERE `charId` = ? ORDER BY `catId`, `order`"))
		{
			stmt.setInt(1, _playerObjId);
			try (ResultSet rs = stmt.executeQuery())
			{
				while (rs.next())
				{
					UIParser.addCategory(_storedCategories, rs.getInt("catId"), rs.getInt("cmdId"));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: getCatsFromDB(): " + e.getMessage(), e);
		}
		
		if (_storedCategories.isEmpty())
		{
			_storedCategories = UIParser.getInstance().getCategories();
		}
	}
	
	public void getKeysFromDB()
	{
		if (_storedKeys != null)
		{
			return;
		}
		
		_storedKeys = new HashMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM character_ui_actions WHERE `charId` = ? ORDER BY `cat`, `order`"))
		{
			stmt.setInt(1, _playerObjId);
			try (ResultSet rs = stmt.executeQuery())
			{
				while (rs.next())
				{
					int cat = rs.getInt("cat");
					int cmd = rs.getInt("cmd");
					int key = rs.getInt("key");
					int tgKey1 = rs.getInt("tgKey1");
					int tgKey2 = rs.getInt("tgKey2");
					int show = rs.getInt("show");
					UIParser.addKey(_storedKeys, cat, new ActionKey(cat, cmd, key, tgKey1, tgKey2, show));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: getKeysFromDB(): " + e.getMessage(), e);
		}
		
		if (_storedKeys.isEmpty())
		{
			_storedKeys = UIParser.getInstance().getKeys();
		}
	}
	
	public boolean isSaved()
	{
		return _saved;
	}
}