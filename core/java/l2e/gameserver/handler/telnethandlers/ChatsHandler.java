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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2e.gameserver.handler.telnethandlers;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import l2e.gameserver.Announcements;
import l2e.gameserver.data.xml.AdminParser;
import l2e.gameserver.handler.ITelnetHandler;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class ChatsHandler implements ITelnetHandler
{
	private final String[] _commands =
	{
		"announce",
		"msg",
		"gmchat"
	};
	
	@Override
	public boolean useCommand(String command, PrintWriter _print, Socket _cSocket, int _uptime)
	{
		if (command.startsWith("announce"))
		{
			try
			{
				command = command.substring(9);
				Announcements.getInstance().announceToAll(command);
				_print.println("Announcement Sent!");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				_print.println("Please Enter Some Text To Announce!");
			}
		}
		else if (command.startsWith("msg"))
		{
			try
			{
				String val = command.substring(4);
				StringTokenizer st = new StringTokenizer(val);
				String name = st.nextToken();
				String message = val.substring(name.length() + 1);
				L2PcInstance reciever = L2World.getInstance().getPlayer(name);
				CreatureSay cs = new CreatureSay(0, Say2.TELL, "Telnet Priv", message);
				if (reciever != null)
				{
					reciever.sendPacket(cs);
					_print.println("Telnet Priv->" + name + ": " + message);
					_print.println("Message Sent!");
				}
				else
				{
					_print.println("Unable To Find Username: " + name);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				_print.println("Please Enter Some Text!");
			}
		}
		else if (command.startsWith("gmchat"))
		{
			try
			{
				command = command.substring(7);
				CreatureSay cs = new CreatureSay(0, Say2.ALLIANCE, "Telnet GM Broadcast from " + _cSocket.getInetAddress().getHostAddress(), command);
				AdminParser.getInstance().broadcastToGMs(cs);
				_print.println("Your Message Has Been Sent To " + getOnlineGMS() + " GM(s).");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				_print.println("Please Enter Some Text To Announce!");
			}
		}
		return false;
	}
	
	private int getOnlineGMS()
	{
		return AdminParser.getInstance().getAllGms(true).size();
	}
	
	@Override
	public String[] getCommandList()
	{
		return _commands;
	}
}