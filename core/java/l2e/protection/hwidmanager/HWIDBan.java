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
package l2e.protection.hwidmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import l2e.L2DatabaseFactory;
import l2e.gameserver.network.L2GameClient;

public class HWIDBan
{
	private static HWIDBan _instance;
	
	private static Map<Integer, HWIDBanList> _lists;
	
	public static HWIDBan getInstance()
	{
		if (_instance == null)
		{
			_instance = new HWIDBan();
		}
		return _instance;
	}
	
	public static void reload()
	{
		_instance = new HWIDBan();
	}
	
	public HWIDBan()
	{
		_lists = new HashMap<>();
		load();
	}
	
	private void load()
	{
		String HWID = "";
		int counterHWIDBan = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM hwid_bans");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				HWID = rset.getString("HWID");
				HWIDBanList hb = new HWIDBanList(counterHWIDBan);
				hb.setHWIDBan(HWID);
				_lists.put(counterHWIDBan, hb);
				counterHWIDBan++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean checkFullHWIDBanned(L2GameClient client)
	{
		if (_lists.size() == 0)
		{
			return false;
		}
		for (int i = 0; i < _lists.size(); i++)
		{
			if (_lists.get(i).getHWID().equals(client.getHWID()))
			{
				return true;
			}
		}
		return false;
	}
	
	public static int getCountHWIDBan()
	{
		return _lists.size();
	}
	
	public static void addHWIDBan(L2GameClient client)
	{
		String HWID = client.getHWID();
		int counterHwidBan = _lists.size();
		HWIDBanList hb = new HWIDBanList(counterHwidBan);
		hb.setHWIDBan(HWID);
		_lists.put(counterHwidBan, hb);

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO hwid_bans SET HWID=?");
			statement.setString(1, HWID);
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}