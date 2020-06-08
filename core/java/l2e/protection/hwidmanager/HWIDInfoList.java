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

public class HWIDInfoList
{
	private final int _id;
	private String HWID;
	private int count;
	private int playerID;
	private String login;
	private LockType lockType;

	public static enum LockType
	{
		PLAYER_LOCK,
		ACCOUNT_LOCK,
		NONE
	}
	
	public HWIDInfoList(int id)
	{
		_id = id;
	}
	
	public int get_id()
	{
		return _id;
	}
	
	public void setHwids(String hwid)
	{
		HWID = hwid;
		count = 1;
	}
	
	public String getHWID()
	{
		return HWID;
	}
	
	public void setHWID(String HWID)
	{
		this.HWID = HWID;
	}
	
	public int getPlayerID()
	{
		return playerID;
	}
	
	public void setPlayerID(int playerID)
	{
		this.playerID = playerID;
	}
	
	public String getLogin()
	{
		return login;
	}
	
	public void setLogin(String login)
	{
		this.login = login;
	}

	public int getCount()
	{
		return count;
	}
	
	public void setCount(int count)
	{
		this.count = count;
	}

	public LockType getLockType()
	{
		return lockType;
	}
	
	public void setLockType(LockType lockType)
	{
		this.lockType = lockType;
	}
}