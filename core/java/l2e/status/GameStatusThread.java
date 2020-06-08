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
package l2e.status;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.handler.ITelnetHandler;
import l2e.gameserver.handler.TelnetHandler;

public class GameStatusThread extends Thread
{
	private static final Logger _log = Logger.getLogger(GameStatusThread.class.getName());
	
	private final Socket _cSocket;
	
	private final PrintWriter _print;
	private final BufferedReader _read;
	
	private final int _uptime;
	
	private void telnetOutput(int type, String text)
	{
		if (Config.DEVELOPER)
		{
			if (type == 1)
				System.out.println("TELNET | " + text);
			else if (type == 2)
				System.out.print("TELNET | " + text);
			else if (type == 3)
				System.out.print(text);
			else if (type == 4)
				System.out.println(text);
			else
				System.out.println("TELNET | " + text);
		}
		else
		{
			// only print output if the message is rejected
			if (type == 5)
				System.out.println("TELNET | " + text);
		}
	}
	
	private boolean isValidIP(Socket client)
	{
		boolean result = false;
		InetAddress ClientIP = client.getInetAddress();
		
		// convert IP to String, and compare with list
		String clientStringIP = ClientIP.getHostAddress();
		
		telnetOutput(1, "Connection from: " + clientStringIP);
		
		// read and loop thru list of IPs, compare with newIP
		if (Config.DEVELOPER)
		{
			telnetOutput(2, "");
		}
		
		final File file = new File(Config.TELNET_FILE);
		try (InputStream telnetIS = new FileInputStream(file))
		{
			Properties telnetSettings = new Properties();
			telnetSettings.load(telnetIS);
			
			String HostList = telnetSettings.getProperty("ListOfHosts", "127.0.0.1,localhost,::1");
			
			if (Config.DEVELOPER)
			{
				telnetOutput(3, "Comparing ip to list...");
			}
			
			// compare
			String ipToCompare = null;
			for (String ip : HostList.split(","))
			{
				if (!result)
				{
					ipToCompare = InetAddress.getByName(ip).getHostAddress();
					if (clientStringIP.equals(ipToCompare))
						result = true;
					if (Config.DEVELOPER)
						telnetOutput(3, clientStringIP + " = " + ipToCompare + "(" + ip + ") = " + result);
				}
			}
		}
		catch (IOException e)
		{
			if (Config.DEVELOPER)
				telnetOutput(4, "");
			telnetOutput(1, "Error: " + e);
		}

		if (Config.DEVELOPER)
			telnetOutput(4, "Allow IP: " + result);
		return result;
	}
	
	public GameStatusThread(Socket client, int uptime, String StatusPW) throws IOException
	{
		setPriority(Thread.MAX_PRIORITY);
		_cSocket = client;
		_uptime = uptime;
		
		_print = new PrintWriter(_cSocket.getOutputStream());
		_read = new BufferedReader(new InputStreamReader(_cSocket.getInputStream()));
		
		if (isValidIP(client))
		{
			telnetOutput(1, client.getInetAddress().getHostAddress() + " accepted.");
			_print.println("Welcome To The L2J Telnet Session.");
			_print.println("Please Insert Your Password!");
			_print.print("Password: ");
			_print.flush();
			String tmpLine = _read.readLine();
			if (tmpLine == null)
			{
				_print.println("Error.");
				_print.println("Disconnected...");
				_print.flush();
				_cSocket.close();
			}
			else
			{
				if (!tmpLine.equals(StatusPW))
				{
					_print.println("Incorrect Password!");
					_print.println("Disconnected...");
					_print.flush();
					_cSocket.close();
				}
				else
				{
					_print.println("Password Correct!");
					_print.println("[L2J Game Server]");
					_print.print("");
					_print.flush();
					start();
				}
			}
		}
		else
		{
			telnetOutput(5, "Connection attempt from " + client.getInetAddress().getHostAddress() + " rejected.");
			_cSocket.close();
		}
	}
	
	@Override
	public void run()
	{
		String _usrCommand = "";
		try
		{
			while (_usrCommand.compareTo("quit") != 0 && _usrCommand.compareTo("exit") != 0)
			{
				_usrCommand = _read.readLine();
				if (_usrCommand == null)
				{
					_cSocket.close();
					break;
				}
				
				final ITelnetHandler handler = TelnetHandler.getInstance().getHandler(_usrCommand);
				if (handler != null)
					handler.useCommand(_usrCommand, _print, _cSocket, _uptime);
				
				else if (_usrCommand.equalsIgnoreCase("quit") || _usrCommand.equalsIgnoreCase("exit") || _usrCommand.isEmpty())
				{
				}
				else
					_print.print("Command: " + _usrCommand + " was not found!");
				
				_print.print("");
				_print.flush();
			}
			if (!_cSocket.isClosed())
			{
				_print.println("Bye Bye!");
				_print.flush();
				_cSocket.close();
			}
			telnetOutput(1, "Connection from " + _cSocket.getInetAddress().getHostAddress() + " was closed by client.");
		}
		catch (IOException e)
		{
			_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}
