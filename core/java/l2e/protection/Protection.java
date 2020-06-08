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
package l2e.protection;

import static l2e.protection.utils.Util.LastErrorConvertion;
import static l2e.protection.utils.Util.verifyChecksum;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.serverpackets.ServerClose;
import l2e.protection.crypt.BlowfishEngine;
import l2e.protection.hwidmanager.HWIDBan;
import l2e.protection.hwidmanager.HWIDManager;
import l2e.protection.network.ProtectionManager;
import l2e.protection.utils.Log;

public class Protection
{
	private static final Logger _log = Logger.getLogger(Protection.class.getName());
	
	private static byte[] _key = new byte[16];
	private static String _logFile = "protection_logs";
	
	public static void Init()
	{
		ConfigProtection.load();
		
		if (isProtectionOn())
		{
			HWIDBan.getInstance();
			HWIDManager.getInstance();
			ProtectionManager.getInstance();
		}
	}
	
	public static boolean isProtectionOn()
	{
		if (ConfigProtection.ALLOW_GUARD_SYSTEM)
		{
			return true;
		}
		return false;
	}
	
	public static byte[] getKey(byte[] key)
	{
		byte[] bfkey =
		{
			110,
			36,
			2,
			15,
			-5,
			17,
			24,
			23,
			18,
			45,
			1,
			21,
			122,
			16,
			-5,
			12
		};
		try
		{
			BlowfishEngine bf = new BlowfishEngine();
			bf.init(true, bfkey);
			bf.processBlock(key, 0, _key, 0);
			bf.processBlock(key, 8, _key, 8);
		}
		catch (IOException e)
		{
			_log.info("Bad key!!!");
		}
		return _key;
	}
	
	public static boolean checkPlayerWithHWID(L2GameClient client, int playerID, String playerName)
	{
		if (!isProtectionOn())
		{
			return true;
		}
		
		client.setPlayerName(playerName);
		client.setPlayerId(playerID);

		if (ConfigProtection.PROTECT_ENABLE_HWID_LOCK)
		{
			if (HWIDManager.checkLockedHWID(client))
			{
				_log.info("An attempt to log in to locked character, " + client.toString());
				client.close(ServerClose.STATIC_PACKET);
				return false;
			}
		}
		
		if (ConfigProtection.PROTECT_WINDOWS_COUNT != 0)
		{
			final int count = ProtectionManager.getInstance().getCountByHWID(client.getHWID());
			if (count > ConfigProtection.PROTECT_WINDOWS_COUNT)
			{
				final int count2 = HWIDManager.getAllowedWindowsCount(client);
				if (count2 > 0)
				{
					if (count > count2)
					{
						_log.info("Multi windows: " + client.toString());
						client.close(ServerClose.STATIC_PACKET);
						return false;
					}
				}
				else
				{
					client.close(ServerClose.STATIC_PACKET);
					return false;
				}
			}
		}
		addPlayer(client);
		return true;
	}
	
	public static boolean CheckHWIDs(L2GameClient client, int LastError1, int LastError2)
	{
		boolean resultHWID = false;
		boolean resultLastError = false;
		String HWID = client.getHWID();
		
		if (HWID.equalsIgnoreCase("fab800b1cc9de379c8046519fa841e6"))
		{
			Log.add("HWID:" + HWID + "|is empty, account:" + client.getLoginName() + "|IP: " + client.toString(), _logFile);
			if (ConfigProtection.PROTECT_KICK_WITH_EMPTY_HWID)
			{
				resultHWID = true;
			}
		}

		if (LastError1 != 0)
		{
			Log.add("LastError(HWID):" + LastError1 + "|" + LastErrorConvertion(LastError1) + "|isn't empty, " + client.toString(), _logFile);
			if (ConfigProtection.PROTECT_KICK_WITH_LASTERROR_HWID)
			{
				resultLastError = true;
			}
		}
		return resultHWID || resultLastError;
	}
	
	public static boolean doAuthLogin(L2GameClient client, byte[] data, String loginName)
	{
		if (!isProtectionOn())
		{
			return true;
		}
		
		client.setLoginName(loginName);
		
		String fullHWID = Protection.ExtractHWID(data);
		if (fullHWID == null)
		{
			_log.info("AuthLogin CRC Check Fail! May be BOT or unprotected client! Client IP: " + client.toString());
			client.close(ServerClose.STATIC_PACKET);
			return false;
		}
		
		Log.add(loginName + "|" + client.toString() + "|" + client.getHWID(), "hwid_log");
		
		int LastError1 = ByteBuffer.wrap(data, 16, 4).getInt();
		
		if (Protection.CheckHWIDs(client, LastError1, 0))
		{
			_log.info("HWID error, look protection_logs.txt file, from IP: " + client.toString());
			client.close(ServerClose.STATIC_PACKET);
			return false;
		}
		
		if (HWIDBan.getInstance().checkFullHWIDBanned(client))
		{
			_log.info("Client " + client + " is banned. Kicked! |HWID: " + client.getHWID() + " IP: " + client.toString());
			client.close(ServerClose.STATIC_PACKET);
		}
		
		int VerfiFlag = ByteBuffer.wrap(data, 40, 4).getInt();
		
		if (!checkVerfiFlag(client, VerfiFlag))
		{
			return false;
		}

		if (ConfigProtection.PROTECT_ENABLE_HWID_LOCK)
		{
			if (HWIDManager.checkLockedHWID(client))
			{
				Log.add("An attempt to log in to locked account|" + "|HWID: " + client.getHWID() + " IP: " + client.toString(), _logFile);
				client.close(ServerClose.STATIC_PACKET);
				return false;
			}
		}
		return true;
	}
	
	public static void addPlayer(L2GameClient client)
	{
		if (isProtectionOn() && (client != null))
		{
			ProtectionManager.getInstance().addPlayer(client);
		}
	}
	
	public static void removePlayer(L2GameClient client)
	{
		if (isProtectionOn() && (client != null))
		{
			ProtectionManager.getInstance().removePlayer(client.getPlayerName());
		}
	}
	
	public static void doDisconection(L2GameClient client)
	{
		removePlayer(client);
	}
	
	public static boolean checkVerfiFlag(L2GameClient client, int flag)
	{
		boolean result = true;
		int fl = Integer.reverseBytes(flag);
		
		if (fl == 0xFFFFFFFF)
		{
			Log.add("Error Verify Flag|" + client.toString(), _logFile);
			return false;
		}
		
		if (fl == 0x50000000)
		{
			Log.add("Error get net data client|" + client.toString() + "|DEBUG INFO:" + fl, _logFile);
			return false;
		}
		
		if ((fl & 0x01) != 0)
		{
			Log.add("Sniffer detect |" + client.toString() + "|DEBUG INFO:" + fl, _logFile);
			result = false;
		}
		
		if ((fl & 0x10) != 0)
		{
			Log.add("Sniffer detect2 |" + client.toString() + "|DEBUG INFO:" + fl, _logFile);
			result = false;
		}
		
		if ((fl & 0x10000000) != 0)
		{
			Log.add("L2ext detect |" + client.toString() + "|DEBUG INFO:" + fl, _logFile);
			result = false;
		}
		return result;
	}
	
	public static String ExtractHWID(byte[] _data)
	{
		if (verifyChecksum(_data, 0, _data.length))
		{
			StringBuilder resultHWID = new StringBuilder();
			for (int i = 0; i < (_data.length - 8); i++)
			{
				resultHWID.append(fillHex(_data[i] & 0xff, 2));
			}
			return resultHWID.toString();
		}
		return null;
	}
	
	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		
		for (int i = number.length(); i < digits; i++)
		{
			number = "0" + number;
		}
		return number;
	}
}