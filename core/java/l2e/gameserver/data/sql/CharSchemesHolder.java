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
package l2e.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.model.skills.L2Skill;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Created by LordWinter 03.09.2012 Fixed by L2J Eternity-World
 */
public class CharSchemesHolder
{
	private final static Logger _log = Logger.getLogger(CharSchemesHolder.class.getName());
	
	private static TIntObjectHashMap<HashMap<String, ArrayList<L2Skill>>> _schemesTable;
	private static CharSchemesHolder _instance = null;
	
	private static final String SQL_LOAD_SCHEME = "SELECT * FROM character_schemes WHERE ownerId=?";
	private static final String SQL_DELETE_SCHEME = "DELETE FROM character_schemes WHERE ownerId=?";
	
	public CharSchemesHolder()
	{
		_schemesTable = new TIntObjectHashMap<HashMap<String, ArrayList<L2Skill>>>()
		{
		};
	}
	
	public void loadScheme(int objectId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SQL_LOAD_SCHEME);
			statement.setInt(1, objectId);
			
			ResultSet rs = statement.executeQuery();
			
			HashMap<String, ArrayList<L2Skill>> map = new HashMap<>();
			
			while (rs.next())
			{
				int skillId = rs.getInt("id");
				int skillLevel = rs.getInt("level");
				String scheme = rs.getString("scheme");
				
				if (!map.containsKey(scheme) && (map.size() <= Config.BUFF_MAX_SCHEMES))
				{
					map.put(scheme, new ArrayList<L2Skill>());
				}
				
				if ((map.get(scheme) != null) && (map.get(scheme).size() < Config.BUFF_MAX_SKILLS))
				{
					map.get(scheme).add(SkillHolder.getInstance().getInfo(skillId, skillLevel));
				}
			}
			if (!map.isEmpty())
			{
				_schemesTable.put(objectId, map);
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(getClass().getSimpleName() + ": Error trying to load buff scheme from object id: " + objectId);
		}
	}
	
	public void onPlayerLogin(int playerId)
	{
		if (_schemesTable.get(playerId) == null)
		{
			loadScheme(playerId);
		}
	}
	
	public void onServerShutdown()
	{
		if (Config.BUFF_STORE_SCHEMES)
		{
			clearDB();
			saveDataToDB();
		}
	}
	
	public void clearDB()
	{
		if (_schemesTable.isEmpty())
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (int i : _schemesTable.keys())
			{
				PreparedStatement statement = con.prepareStatement(SQL_DELETE_SCHEME);
				statement.setInt(1, i);
				statement.execute();
			}
		}
		catch (Exception e)
		{
			_log.warning(getClass().getSimpleName() + ": Error while trying to delete schemes");
		}
	}
	
	@SuppressWarnings("unused")
	public void saveDataToDB()
	{
		if (_schemesTable.isEmpty())
		{
			return;
		}
		
		int count = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			String query = "INSERT INTO character_schemes (ownerId, id, level, scheme) VALUES ";
			
			for (int ownerId : _schemesTable.keys())
			{
				HashMap<String, ArrayList<L2Skill>> map = _schemesTable.get(ownerId);
				for (String name : map.keySet())
				{
					for (L2Skill sk : map.get(name))
					{
						query += "(" + ownerId + ", " + sk.getId() + ", " + sk.getLevel() + ", '" + name + "'),";
					}
				}
				count++;
			}
			query += "(1,NULL,NULL,NULL);";
			con.prepareStatement(query).execute();
			con.prepareStatement("DELETE FROM character_schemes WHERE ownerId=1;").execute();
			
		}
		catch (Exception e)
		{
			_log.warning(getClass().getSimpleName() + ": Error while trying to delete schemes");
		}
	}
	
	public ArrayList<L2Skill> getScheme(int playerid, String scheme_key)
	{
		if (_schemesTable.get(playerid) == null)
		{
			return null;
		}
		return _schemesTable.get(playerid).get(scheme_key);
	}
	
	public boolean getSchemeContainsSkill(int playerId, String scheme_key, int skillId)
	{
		for (L2Skill sk : getScheme(playerId, scheme_key))
		{
			if (sk.getId() == skillId)
			{
				return true;
			}
		}
		return false;
	}
	
	public void setScheme(int playerId, String schemeKey, ArrayList<L2Skill> list)
	{
		_schemesTable.get(playerId).put(schemeKey, list);
	}
	
	public HashMap<String, ArrayList<L2Skill>> getAllSchemes(int playerId)
	{
		return _schemesTable.get(playerId);
	}
	
	public TIntObjectHashMap<HashMap<String, ArrayList<L2Skill>>> getSchemesTable()
	{
		return _schemesTable;
	}
	
	public static CharSchemesHolder getInstance()
	{
		if (_instance == null)
		{
			_instance = new CharSchemesHolder();
		}
		return _instance;
	}
}