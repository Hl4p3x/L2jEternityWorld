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
package l2e.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;

import l2e.L2DatabaseFactory;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;

/**
 * Created by LordWinter 28.07.2013 Based on L2J Eternity-World
 */
public class VoteReward implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"reward"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("reward"))
		{
			try
			{
				long votecount = 0;
				votecount = getVote(activeChar);
				if (votecount > 0)
				{
					activeChar.addItem("Buy", 6673, votecount, activeChar, false);
					CustomMessage msg = new CustomMessage("VoteReward.GIVE_REWARD", activeChar.getLang());
					msg.add(votecount);
					activeChar.sendMessage(msg.toString());
				}
				else
				{
					activeChar.sendMessage((new CustomMessage("VoteReward.VISITE_SITE", activeChar.getLang())).toString());
				}
				resetVote(activeChar);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not give vote reward to: " + activeChar.getName() + ": " + e.getMessage(), e);
			}
		}
		return true;
	}
	
	private int getVote(L2PcInstance activeChar)
	{
		int votecount = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT votecount FROM votesystem WHERE char_name='" + activeChar.getName() + "';");
			rs.last();
			votecount = rs.getInt(1);
			rs.close();
			stmt.close();
			
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not give vote reward to: " + activeChar.getName() + ": " + e.getMessage(), e);
		}
		return votecount;
	}
	
	private void resetVote(L2PcInstance activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement stmt = con.createStatement();
			stmt.executeUpdate("update votesystem set votecount = 0 WHERE char_name='" + activeChar.getName() + "';");
			stmt.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not reset vote reward to: " + activeChar.getName() + ": " + e.getMessage(), e);
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}