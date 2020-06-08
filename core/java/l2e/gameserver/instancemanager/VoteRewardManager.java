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
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.model.actor.instance.L2PcInstance;

/**
 * Created by LordWinter 01.16.2013 Based on L2J Eternity-World
 */
public class VoteRewardManager
{
	protected static final Logger _log = Logger.getLogger(VoteRewardManager.class.getName());
	
	public static final VoteRewardManager getInstance()
	{
		return SingletonHolder.Instance;
	}
	
	private static class SingletonHolder
	{
		protected static final VoteRewardManager Instance = new VoteRewardManager();
	}
	
	public long getLastTimeVoted(L2PcInstance activeChar)
	{
		long lastTime = 0;
		String Ip = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT time_voted FROM votes WHERE IP=?");
			statement.setString(1, Ip);
			
			ResultSet rset = statement.executeQuery();
			
			if (rset.next())
			{
				lastTime = rset.getLong("time_voted");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Fatal vote reward error in method getLastTimeVoted: " + e);
		}
		return lastTime;
	}
	
	public long getLastTimeRewarded(L2PcInstance activeChar)
	{
		long lastTime = 0;
		String Ip = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT time_rewarded FROM votes WHERE IP=?");
			statement.setString(1, Ip);
			
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				lastTime = rset.getLong("time_rewarded");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Fatal vote reward error in method getLastTimeRewarded: " + e);
		}
		return lastTime;
	}
	
	public void updateLastTimeRewarded(L2PcInstance activeChar)
	{
		long lastTime = System.currentTimeMillis();
		String Ip = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("REPLACE INTO votes(IP, time_voted, time_rewarded) VALUES (?,?,?)");
			statement.setString(1, Ip);
			statement.setLong(2, getLastTimeVoted(activeChar));
			statement.setLong(3, lastTime);
			
			statement.executeQuery();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Fatal vote reward error in method updateLastTimeRewarded: " + e);
		}
	}
}