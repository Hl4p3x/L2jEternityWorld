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
package l2e.gameserver.model.entity.mods;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.taskmanager.tasks.MonthlyResetTask;
import l2e.gameserver.taskmanager.tasks.TriesResetTask;

public class VoteManager
{
	private static boolean _hasVotedHop;
	private static boolean _hasVotedTop;
	
	public VoteManager()
	{
	}
	
	public static void load()
	{
		System.out.println("Vote Individual Reward System Started Successfully.");
		TriesResetTask.getInstance();
		MonthlyResetTask.getInstance();
	}
	
	protected static int getHopZoneVotes()
	{
		int votes = -1;
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(Config.VOTE_LINK_HOPZONE);
			con = url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("rank anonymous tooltip"))
				{
					votes = Integer.valueOf(inputLine.split(">")[2].replace("</span", ""));
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return votes;
	}
	
	protected static int getTopZoneVotes()
	{
		int votes = -1;
		URL url = null;
		URLConnection con = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader in = null;
		try
		{
			url = new URL(Config.VOTE_LINK_TOPZONE);
			con = url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			is = con.getInputStream();
			isr = new InputStreamReader(is);
			in = new BufferedReader(isr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				if (inputLine.contains("Votes"))
				{
					String votesLine = inputLine;
					votes = Integer.valueOf(votesLine.split(">")[3].replace("</div", ""));
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return votes;
	}
	
	public static String hopCd(L2PcInstance player)
	{
		long hopCdMs = 0;
		long voteDelay = 43200000L;
		PreparedStatement statement = null;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			statement = con.prepareStatement("SELECT lastVoteHopzone FROM characters WHERE charId=?");
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				hopCdMs = rset.getLong("lastVoteHopzone");
			}
		}
		catch (Exception e)
		{
		}
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
		
		Date resultdate = new Date(hopCdMs + voteDelay);
		return sdf.format(resultdate);
	}
	
	public static String topCd(L2PcInstance player)
	{
		long topCdMs = 0;
		long voteDelay = 43200000L;
		PreparedStatement statement = null;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			statement = con.prepareStatement("SELECT lastVoteTopzone FROM characters WHERE charId=?");
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				topCdMs = rset.getLong("lastVoteTopzone");
			}
		}
		catch (Exception e)
		{
		}
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
		
		Date resultdate = new Date(topCdMs + voteDelay);
		return sdf.format(resultdate);
	}
	
	public static String whosVoting()
	{
		for (L2PcInstance voter : L2World.getInstance().getAllPlayersArray())
		{
			if (voter.isVoting())
			{
				return voter.getName();
			}
		}
		return "Yes";
	}
	
	public static void hopvote(final L2PcInstance player)
	{
		long lastVoteHopzone = 0L;
		long voteDelay = 43200000L;
		final int firstvoteshop;
		
		firstvoteshop = getHopZoneVotes();
		
		class hopvotetask implements Runnable
		{
			private final L2PcInstance p;
			
			public hopvotetask(L2PcInstance player)
			{
				p = player;
			}
			
			@Override
			public void run()
			{
				if (firstvoteshop < getHopZoneVotes())
				{
					p.setVoting(false);
					setHasVotedHop(player);
					p.sendMessage("Thank you for voting for us!");
					updateLastVoteHopzone(p);
					updateVotes(p);
				}
				else
				{
					p.setVoting(false);
					p.sendMessage("You did not vote.Please try again.");
					setTries(player, getTries(p) - 1);
				}
			}
			
		}
		
		PreparedStatement statement = null;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			statement = con.prepareStatement("SELECT lastVoteHopzone FROM characters WHERE charId=?");
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				lastVoteHopzone = rset.getLong("lastVoteHopzone");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (getTries(player) <= 0)
		{
			player.sendMessage("Due to your multiple failures in voting you lost your chance to vote today");
		}
		else if (((lastVoteHopzone + voteDelay) < System.currentTimeMillis()) && (getTries(player) > 0))
		{
			for (L2PcInstance j : L2World.getInstance().getAllPlayersArray())
			{
				if (j.isVoting())
				{
					player.sendMessage("Someone is already voting.Wait for your turn please!");
					return;
				}
			}
			
			player.setVoting(true);
			player.sendMessage("Go fast on the site and vote on the hopzone banner!");
			player.sendMessage("You have " + Config.SECS_TO_VOTE + " seconds.Hurry!");
			ThreadPoolManager.getInstance().scheduleGeneral(new hopvotetask(player), Config.SECS_TO_VOTE * 1000);
		}
		else if ((getTries(player) <= 0) && ((lastVoteHopzone + voteDelay) < System.currentTimeMillis()))
		{
			for (L2PcInstance j : L2World.getInstance().getAllPlayersArray())
			{
				if (j.isVoting())
				{
					player.sendMessage("Someone is already voting.Wait for your turn please!");
					return;
				}
			}
			
			player.setVoting(true);
			player.sendMessage("Go fast on the site and vote on the hopzone banner!");
			player.sendMessage("You have " + Config.SECS_TO_VOTE + " seconds.Hurry!");
			ThreadPoolManager.getInstance().scheduleGeneral(new hopvotetask(player), Config.SECS_TO_VOTE * 1000);
			
		}
		else
		{
			player.sendMessage("12 hours have to pass till you are able to vote again.");
		}
		
	}
	
	public static void topvote(final L2PcInstance player)
	{
		long lastVoteTopzone = 0L;
		long voteDelay = 43200000L;
		final int firstvotestop;
		
		firstvotestop = getTopZoneVotes();
		
		class topvotetask implements Runnable
		{
			private final L2PcInstance p;
			
			public topvotetask(L2PcInstance player)
			{
				p = player;
			}
			
			@Override
			public void run()
			{
				if (firstvotestop < getTopZoneVotes())
				{
					p.setVoting(false);
					setHasVotedTop(p);
					p.sendMessage("Thank you for voting for us!");
					updateLastVoteTopzone(p);
					updateVotes(p);
				}
				else
				{
					p.setVoting(false);
					p.sendMessage("You did not vote.Please try again.");
					setTries(p, getTries(p) - 1);
				}
			}
			
		}
		
		PreparedStatement statement = null;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			statement = con.prepareStatement("SELECT lastVoteTopzone FROM characters WHERE charId=?");
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				lastVoteTopzone = rset.getLong("lastVoteTopzone");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (getTries(player) <= 0)
		{
			player.sendMessage("Due to your multiple failures in voting you lost your chance to vote today");
		}
		else if ((getTries(player) <= 0) && ((lastVoteTopzone + voteDelay) < System.currentTimeMillis()))
		{
			for (L2PcInstance j : L2World.getInstance().getAllPlayersArray())
			{
				if (j.isVoting())
				{
					player.sendMessage("Someone is already voting.Wait for your turn please!");
					return;
				}
			}
			player.setVoting(true);
			player.sendMessage("Go fast on the site and vote on the topzone banner!");
			player.sendMessage((new StringBuilder()).append("You have ").append(Config.SECS_TO_VOTE).append(" seconds.Hurry!").toString());
			ThreadPoolManager.getInstance().scheduleGeneral(new topvotetask(player), Config.SECS_TO_VOTE * 1000);
		}
		else if (((lastVoteTopzone + voteDelay) < System.currentTimeMillis()) && (getTries(player) > 0))
		{
			for (L2PcInstance j : L2World.getInstance().getAllPlayersArray())
			{
				if (j.isVoting())
				{
					player.sendMessage("Someone is already voting.Wait for your turn please!");
					return;
				}
			}
			player.setVoting(true);
			player.sendMessage("Go fast on the site and vote on the topzone banner!");
			player.sendMessage((new StringBuilder()).append("You have ").append(Config.SECS_TO_VOTE).append(" seconds.Hurry!").toString());
			ThreadPoolManager.getInstance().scheduleGeneral(new topvotetask(player), Config.SECS_TO_VOTE * 1000);
		}
		else
		{
			player.sendMessage("12 hours have to pass till you are able to vote again.");
		}
		
	}
	
	public static void hasVotedHop(L2PcInstance player)
	{
		int _hasVotedHop = -1;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT hasVotedHop FROM characters WHERE charId=?");
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				_hasVotedHop = rset.getInt("hasVotedHop");
			}
			
			if (_hasVotedHop == 1)
			{
				setHasVotedHop(true);
			}
			else if (_hasVotedHop == 0)
			{
				setHasVotedHop(false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void hasVotedTop(L2PcInstance player)
	{
		int _hasVotedTop = -1;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT hasVotedTop FROM characters WHERE charId=?");
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				_hasVotedTop = rset.getInt("hasVotedTop");
			}
			
			if (_hasVotedTop == 1)
			{
				setHasVotedTop(true);
			}
			else if (_hasVotedTop == 0)
			{
				setHasVotedTop(false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void updateVotes(L2PcInstance activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET monthVotes=?, totalVotes=? WHERE charId=?");
			
			statement.setInt(1, getMonthVotes(activeChar) + 1);
			statement.setInt(2, getTotalVotes(activeChar) + 1);
			statement.setInt(3, activeChar.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void setHasVotedHop(L2PcInstance activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET hasVotedHop=? WHERE charId=?");
			
			statement.setInt(1, 1);
			statement.setInt(2, activeChar.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void setHasVotedTop(L2PcInstance activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET hasVotedTop=? WHERE charId=?");
			
			statement.setInt(1, 1);
			statement.setInt(2, activeChar.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void setHasNotVotedHop(L2PcInstance activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET hasVotedHop=? WHERE charId=?");
			
			statement.setInt(1, 0);
			statement.setInt(2, activeChar.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void setHasNotVotedTop(L2PcInstance activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET hasVotedTop=? WHERE charId=?");
			
			statement.setInt(1, 0);
			statement.setInt(2, activeChar.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static int getTries(L2PcInstance player)
	{
		int tries = -1;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT tries FROM characters WHERE charId=?");
			statement.setInt(1, player.getObjectId());
			for (ResultSet rset = statement.executeQuery(); rset.next();)
			{
				tries = rset.getInt("tries");
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return tries;
	}
	
	public static void setTries(L2PcInstance player, int tries)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET tries=? WHERE charId=?");
			
			statement.setInt(1, tries);
			statement.setInt(2, player.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static int getMonthVotes(L2PcInstance player)
	{
		int monthVotes = -1;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT monthVotes FROM characters WHERE charId=?");
			
			statement.setInt(1, player.getObjectId());
			for (ResultSet rset = statement.executeQuery(); rset.next();)
			{
				monthVotes = rset.getInt("monthVotes");
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return monthVotes;
	}
	
	public static int getTotalVotes(L2PcInstance player)
	{
		int totalVotes = -1;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT totalVotes FROM characters WHERE charId=?");
			
			statement.setInt(1, player.getObjectId());
			for (ResultSet rset = statement.executeQuery(); rset.next();)
			{
				totalVotes = rset.getInt("totalVotes");
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return totalVotes;
	}
	
	public static int getBigTotalVotes(L2PcInstance player)
	{
		int bigTotalVotes = -1;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT SUM(totalVotes) FROM characters");
			
			for (ResultSet rset = statement.executeQuery(); rset.next();)
			{
				bigTotalVotes = rset.getInt("SUM(totalVotes)");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return bigTotalVotes;
	}
	
	public static int getBigMonthVotes(L2PcInstance player)
	{
		int bigMonthVotes = -1;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT SUM(monthVotes) FROM characters");
			
			for (ResultSet rset = statement.executeQuery(); rset.next();)
			{
				bigMonthVotes = rset.getInt("SUM(monthVotes)");
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return bigMonthVotes;
	}
	
	public static void updateLastVoteHopzone(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET lastVoteHopzone=? WHERE charId=?");
			statement.setLong(1, System.currentTimeMillis());
			statement.setInt(2, player.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void updateLastVoteTopzone(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET lastVoteTopzone=? WHERE charId=?");
			statement.setLong(1, System.currentTimeMillis());
			statement.setInt(2, player.getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean hasVotedHop()
	{
		return _hasVotedHop;
	}
	
	public static void setHasVotedHop(boolean hasVotedHop)
	{
		_hasVotedHop = hasVotedHop;
	}
	
	public static boolean hasVotedTop()
	{
		return _hasVotedTop;
	}
	
	public static void setHasVotedTop(boolean hasVotedTop)
	{
		_hasVotedTop = hasVotedTop;
	}
}