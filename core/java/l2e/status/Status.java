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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javolution.util.FastList;

import l2e.Config;
import l2e.Server;
import l2e.util.Rnd;

public class Status extends Thread
{
	protected static final Logger _log = Logger.getLogger(Status.class.getName());
	
	private final ServerSocket statusServerSocket;
	
	private final int _uptime;
	private final int _statusPort;
	private String _statusPw;
	private final int _mode;
	private final List<LoginStatusThread> _loginStatus;
	
	@Override
	public void run()
	{
		setPriority(Thread.MAX_PRIORITY);
		
		while (!isInterrupted())
		{
			try
			{
				Socket connection = statusServerSocket.accept();
				if (_mode == Server.MODE_GAMESERVER)
				{
					new GameStatusThread(connection, _uptime, _statusPw);
				}
				else if (_mode == Server.MODE_LOGINSERVER)
				{
					LoginStatusThread lst = new LoginStatusThread(connection, _uptime, _statusPw);
					if (lst.isAlive())
					{
						_loginStatus.add(lst);
					}
				}
				if (isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch (IOException io)
					{
						_log.warning(getClass().getSimpleName() + ": " + io.getMessage());
					}
					break;
				}
			}
			catch (IOException e)
			{
				if (isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch (IOException io)
					{
						_log.warning(getClass().getSimpleName() + ": " + io.getMessage());
					}
					break;
				}
			}
		}
	}
	
	public Status(int mode) throws IOException
	{
		super("Status");
		_mode = mode;
		Properties telnetSettings = new Properties();
		try (InputStream is = new FileInputStream(new File(Config.TELNET_FILE)))
		{
			telnetSettings.load(is);
		}
		
		_statusPort = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
		_statusPw = telnetSettings.getProperty("StatusPW");
		
		if (_mode == Server.MODE_GAMESERVER || _mode == Server.MODE_LOGINSERVER)
		{
			if (_statusPw == null)
			{
				_log.info("Server's Telnet Function Has No Password Defined!");
				_log.info("A Password Has Been Automaticly Created!");
				_statusPw = rndPW(10);
				_log.info("Password Has Been Set To: " + _statusPw);
			}
			_log.info("Telnet StatusServer started successfully, listening on Port: " + _statusPort);
		}
		statusServerSocket = new ServerSocket(_statusPort);
		_uptime = (int) System.currentTimeMillis();
		_loginStatus = new FastList<>();
	}
	
	private String rndPW(int length)
	{
		final String lowerChar = "qwertyuiopasdfghjklzxcvbnm";
		final String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
		final String digits = "1234567890";
		final StringBuilder password = new StringBuilder(length);
		
		for (int i = 0; i < length; i++)
		{
			int charSet = Rnd.nextInt(3);
			switch (charSet)
			{
				case 0:
					password.append(lowerChar.charAt(Rnd.nextInt(lowerChar.length() - 1)));
					break;
				case 1:
					password.append(upperChar.charAt(Rnd.nextInt(upperChar.length() - 1)));
					break;
				case 2:
					password.append(digits.charAt(Rnd.nextInt(digits.length() - 1)));
					break;
			}
		}
		return password.toString();
	}
	
	public void sendMessageToTelnets(String msg)
	{
		List<LoginStatusThread> lsToRemove = new FastList<>();
		for (LoginStatusThread ls : _loginStatus)
		{
			if (ls.isInterrupted())
			{
				lsToRemove.add(ls);
			}
			else
			{
				ls.printToTelnet(msg);
			}
		}
	}
}
