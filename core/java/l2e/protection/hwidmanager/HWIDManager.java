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
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.network.L2GameClient;

public class HWIDManager
{
	protected static Logger _log = Logger.getLogger(HWIDManager.class.getName());
	
	private static HWIDManager _instance;
	public static Map<Integer, HWIDInfoList> _listHWID;
	
	public static HWIDManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new HWIDManager();
		}
		return _instance;
	}
	
	public HWIDManager()
	{
		_listHWID = new HashMap<>();
		load();
		_log.info("HWIDManager: Loaded " + _listHWID.size() + " HWIDs");
	}
	
	public static void reload()
	{
		_instance = new HWIDManager();
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM hwid_info");
			ResultSet rset = statement.executeQuery();
			int counterHWIDInfo = 0;
			while (rset.next())
			{
				final HWIDInfoList hInfo = new HWIDInfoList(counterHWIDInfo);
				hInfo.setHwids(rset.getString("HWID"));
				hInfo.setCount(rset.getInt("WindowsCount"));
				hInfo.setLogin(rset.getString("Account"));
				hInfo.setPlayerID(rset.getInt("PlayerID"));
				hInfo.setLockType(HWIDInfoList.LockType.valueOf(rset.getString("LockType")));
				_listHWID.put(counterHWIDInfo, hInfo);
				counterHWIDInfo++;
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void updateHWIDInfo(final L2GameClient client, final HWIDInfoList.LockType lockType)
	{
		updateHWIDInfo(client, 1, lockType);
	}
	
	public static void updateHWIDInfo(final L2GameClient client, final int windowscount)
	{
		updateHWIDInfo(client, windowscount, HWIDInfoList.LockType.NONE);
	}
	
	public static void updateHWIDInfo(L2GameClient client, final int windowsCount, final HWIDInfoList.LockType lockType)
	{
		int counterHwidInfo = _listHWID.size();
		boolean isFound = false;
		for (int i = 0; i < _listHWID.size(); i++)
		{
			if (_listHWID.get(i).getHWID().equals(client.getHWID()))
			{
				isFound = true;
				counterHwidInfo = i;
				break;
			}
		}
		
		final HWIDInfoList hInfo = new HWIDInfoList(counterHwidInfo);
		hInfo.setHwids(client.getHWID());
		hInfo.setCount(windowsCount);
		hInfo.setLogin(client.getAccountName());
		hInfo.setPlayerID(client.getPlayerId());
		hInfo.setLockType(lockType);
		_listHWID.put(counterHwidInfo, hInfo);
		if (isFound)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE hwid_info SET WindowsCount=?,Account=?,PlayerID=?,LockType=? WHERE HWID=?");
				statement.setInt(1, windowsCount);
				statement.setString(2, client.getAccountName());
				statement.setInt(3, client.getPlayerId());
				statement.setString(4, lockType.toString());
				statement.setString(5, client.getHWID());
				statement.execute();
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("INSERT INTO hwid_info (HWID, WindowsCount, Account, PlayerID, LockType) values (?,?,?,?,?)");
				statement.setString(1, client.getHWID());
				statement.setInt(2, windowsCount);
				statement.setString(3, client.getAccountName());
				statement.setInt(4, client.getPlayerId());
				statement.setString(5, lockType.toString());
				statement.execute();
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static int getAllowedWindowsCount(final L2GameClient client)
	{
		if (_listHWID.size() == 0)
		{
			return -1;
		}
		for (int i = 0; i < _listHWID.size(); i++)
		{
			if (_listHWID.get(i).getHWID().equals(client.getHWID()))
			{
				if (_listHWID.get(i).getHWID().equals(""))
				{
					return -1;
				}
				return _listHWID.get(i).getCount();
			}
		}
		return -1;
	}

	public static boolean checkLockedHWID(final L2GameClient client)
	{
		if (_listHWID.size() == 0)
		{
			return false;
		}

		boolean result = false;

		for (int i = 0; i < _listHWID.size(); i++)
		{
			switch (_listHWID.get(i).getLockType())
			{
				case NONE:
					break;
				case PLAYER_LOCK:
					if ((client.getPlayerId() != 0) && (_listHWID.get(i).getPlayerID() == client.getPlayerId()))
					{
						if (_listHWID.get(i).getHWID().equals(client.getHWID()))
						{
							return false;
						}
						result = true;
					}
					break;
				case ACCOUNT_LOCK:
					if (_listHWID.get(i).getLogin().equals(client.getLoginName()))
					{
						if (_listHWID.get(i).getHWID().equals(client.getHWID()))
						{
							return false;
						}
						result = true;
					}
					break;
				default:
					break;
			}
		}
		return result;
	}

	public static int getCountHwidInfo()
	{
		return _listHWID.size();
	}
}