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
package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import l2e.L2DatabaseFactory;
import l2e.gameserver.model.actor.templates.StatsSet;

/**
 * Created by LordWinter 27.10.2012 Based on L2J Eternity-World
 */
public class ServerVariables
{
	private static StatsSet server_vars = null;
	
	private static StatsSet getVars()
	{
		if (server_vars == null)
		{
			server_vars = new StatsSet();
			LoadFromDB();
		}
		return server_vars;
	}
	
	private static void LoadFromDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM server_variables"))
		{
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				server_vars.set(rs.getString("name"), rs.getString("value"));
			}
		}
		catch (SQLException e)
		{
			System.out.println("ServerVariables: Could not load table");
			e.printStackTrace();
		}
	}
	
	private static void SaveToDB(String name)
	{
		PreparedStatement statement;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			String e = getVars().getString(name, "");
			if (e.isEmpty())
			{
				statement = con.prepareStatement("DELETE FROM server_variables WHERE name = ?");
				statement.setString(1, name);
				statement.execute();
			}
			else
			{
				statement = con.prepareStatement("REPLACE INTO server_variables (name, value) VALUES (?,?)");
				statement.setString(1, name);
				statement.setString(2, e);
				statement.execute();
			}
		}
		catch (SQLException e)
		{
			System.out.println("ServerVariables: Could not save table");
			e.printStackTrace();
		}
	}
	
	public static boolean getBool(String name)
	{
		return getVars().getBool(name);
	}
	
	public static boolean getBool(String name, boolean defult)
	{
		return getVars().getBool(name, defult);
	}
	
	public static int getInt(String name)
	{
		return getVars().getInteger(name);
	}
	
	public static int getInt(String name, int defult)
	{
		return getVars().getInteger(name, defult);
	}
	
	public static long getLong(String name)
	{
		return getVars().getLong(name);
	}
	
	public static long getLong(String name, long defult)
	{
		return getVars().getLong(name, defult);
	}
	
	public static double getFloat(String name)
	{
		return getVars().getDouble(name);
	}
	
	public static double getFloat(String name, double defult)
	{
		return getVars().getDouble(name, defult);
	}
	
	public static String getString(String name)
	{
		return getVars().getString(name);
	}
	
	public static String getString(String name, String defult)
	{
		return getVars().getString(name, defult);
	}
	
	public static void set(String name, boolean value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}
	
	public static void set(String name, int value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}
	
	public static void set(String name, long value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}
	
	public static void set(String name, double value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}
	
	public static void set(String name, String value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}
	
	public static void unset(String name)
	{
		getVars().unset(name);
		SaveToDB(name);
	}
}