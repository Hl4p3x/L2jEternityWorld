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

import java.util.StringTokenizer;
import java.util.logging.Level;

import l2e.gameserver.LoginServerThread;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class ChangePassword implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"changepassword"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (target != null && !(target.isEmpty()))
		{
			final StringTokenizer st = new StringTokenizer(target);
			try
			{
				String curpass = null, newpass = null, repeatnewpass = null;
				if (st.hasMoreTokens())
				{
					curpass = st.nextToken();
				}
				if (st.hasMoreTokens())
				{
					newpass = st.nextToken();
				}
				if (st.hasMoreTokens())
				{
					repeatnewpass = st.nextToken();
				}
				
				if (!((curpass == null) || (newpass == null) || (repeatnewpass == null)))
				{
					if (!newpass.equals(repeatnewpass))
					{
						activeChar.sendMessage("The new password doesn't match with the repeated one!");
						return false;
					}
					if (newpass.length() < 3)
					{
						activeChar.sendMessage("The new password is shorter than 3 chars! Please try with a longer one.");
						return false;
					}
					if (newpass.length() > 30)
					{
						activeChar.sendMessage("The new password is longer than 30 chars! Please try with a shorter one.");
						return false;
					}
					
					LoginServerThread.getInstance().sendChangePassword(activeChar.getAccountName(), activeChar.getName(), curpass, newpass);
				}
				else
				{
					activeChar.sendMessage("Invalid password data! You have to fill all boxes.");
					return false;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("A problem occured while changing password!");
				_log.log(Level.WARNING, "", e);
			}
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
			html.setFile(activeChar.getLang(), "data/html/mods/ChangePassword.htm");
			activeChar.sendPacket(html);
			return true;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}