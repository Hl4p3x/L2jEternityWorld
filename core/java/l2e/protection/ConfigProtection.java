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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.Properties;

public class ConfigProtection
{	
	public static boolean PROTECT_DEBUG;
	public static int PROTECT_WINDOWS_COUNT;
	public static boolean ALLOW_GUARD_SYSTEM;
	public static boolean SHOW_PROTECTION_INFO_IN_CLIENT;
	public static boolean SHOW_NAME_SERVER_IN_CLIENT;
	public static boolean SHOW_ONLINE_IN_CLIENT;
	public static boolean SHOW_SERVER_TIME_IN_CLIENT;
	public static boolean SHOW_REAL_TIME_IN_CLIENT;
	public static boolean SHOW_PING_IN_CLIENT;
	public static long TIME_REFRESH_SPECIAL_STRING;
	public static int PositionXProtectionInfoInClient;
	public static int PositionYProtectionInfoInClient;
	public static String NameServerInfoInClient;
	public static int PositionXNameServerInfoInClient;
	public static int PositionYNameServerInfoInClient;
	public static int PositionXOnlineInClient;
	public static int PositionYOnlineInClient;
	public static int PositionXServerTimeInClient;
	public static int PositionYServerTimeInClient;
	public static int PositionXRealTimeInClient;
	public static int PositionYRealTimeInClient;
	public static int PositionXPingInClient;
	public static int PositionYPingInClient;
	public static int ColorProtectionInfoInClient;
	public static int ColorNameServerInfoInClient;
	public static int ColorOnlineInClient;
	public static int ColorServerTimeInClient;
	public static int ColorRealTimeInClient;
	public static int ColorPingInClient;
	public static int GET_CLIENT_HWID;
	public static boolean PROTECT_KICK_WITH_EMPTY_HWID;
	public static boolean PROTECT_KICK_WITH_LASTERROR_HWID;
	public static boolean PROTECT_ENABLE_GG_SYSTEM;
	public static long PROTECT_GG_SEND_INTERVAL;
	public static long PROTECT_GG_RECV_INTERVAL;
	public static long PROTECT_TASK_GG_INVERVAL;
	public static boolean PROTECT_ENABLE_HWID_LOCK;
	
	public static final String PROTECT_FILE = "./config/main/protection.ini";
	
	public static final void load()
	{
		File fp = new File(PROTECT_FILE);
		ALLOW_GUARD_SYSTEM = fp.exists();
		if (ALLOW_GUARD_SYSTEM)
		{
			try
			{
				Properties guardSettings = new Properties();
				InputStream is = new FileInputStream(fp);
				guardSettings.load(is);
				is.close();
				
				ALLOW_GUARD_SYSTEM = getBooleanProperty(guardSettings, "AllowGuardSystem", true);
				PROTECT_DEBUG = getBooleanProperty(guardSettings, "ProtectDebug", false);
				PROTECT_WINDOWS_COUNT = getIntProperty(guardSettings, "AllowedWindowsCount", 99);
				SHOW_PROTECTION_INFO_IN_CLIENT = getBooleanProperty(guardSettings, "ShowProtectionInfoInClient", false);
				SHOW_NAME_SERVER_IN_CLIENT = getBooleanProperty(guardSettings, "ShowNameServerInfoInClient", false);
				SHOW_ONLINE_IN_CLIENT = getBooleanProperty(guardSettings, "ShowOnlineInClient", false);
				SHOW_SERVER_TIME_IN_CLIENT = getBooleanProperty(guardSettings, "ShowServerTimeInClient", false);
				SHOW_REAL_TIME_IN_CLIENT = getBooleanProperty(guardSettings, "ShowRealTimeInClient", false);
				SHOW_PING_IN_CLIENT = getBooleanProperty(guardSettings, "ShowPingInClient", false);
				TIME_REFRESH_SPECIAL_STRING = getLongProperty(guardSettings, "TimeRefreshStringToClient", 1000L);
				NameServerInfoInClient = getProperty(guardSettings, "NameServerInfoInClient", "Test");
				PositionXProtectionInfoInClient = getIntProperty(guardSettings, "PositionXProtectionInfoInClient", 320);
				PositionYProtectionInfoInClient = getIntProperty(guardSettings, "PositionYProtectionInfoInClient", 10);
				PositionXNameServerInfoInClient = getIntProperty(guardSettings, "PositionXNameServerInfoInClient", 320);
				PositionYNameServerInfoInClient = getIntProperty(guardSettings, "PositionYNameServerInfoInClient", 25);
				PositionXOnlineInClient = getIntProperty(guardSettings, "PositionXOnlineInClient", 320);
				PositionYOnlineInClient = getIntProperty(guardSettings, "PositionYOnlineInClient", 40);
				PositionXServerTimeInClient = getIntProperty(guardSettings, "PositionXServerTimeInClient", 320);
				PositionYServerTimeInClient = getIntProperty(guardSettings, "PositionYServerTimeInClient", 70);
				PositionXRealTimeInClient = getIntProperty(guardSettings, "PositionXRealTimeInClient", 320);
				PositionYRealTimeInClient = getIntProperty(guardSettings, "PositionYRealTimeInClient", 85);
				PositionXPingInClient = getIntProperty(guardSettings, "PositionXPingInClient", 320);
				PositionYPingInClient = getIntProperty(guardSettings, "PositionYPingInClient", 100);
				ColorProtectionInfoInClient = getIntHexProperty(guardSettings, "ColorProtectionInfoInClient", -16711936);
				ColorNameServerInfoInClient = getIntHexProperty(guardSettings, "ColorNameServerInfoInClient", -16711936);
				ColorOnlineInClient = getIntHexProperty(guardSettings, "ColorOnlineInClient", -16711936);
				ColorServerTimeInClient = getIntHexProperty(guardSettings, "ColorServerTimeInClient", -16711936);
				ColorRealTimeInClient = getIntHexProperty(guardSettings, "ColorRealTimeInClient", -16711936);
				ColorPingInClient = getIntHexProperty(guardSettings, "ColorPingInClient", -16711936);
				GET_CLIENT_HWID = getIntProperty(guardSettings, "UseClientHWID", 2);
				PROTECT_KICK_WITH_EMPTY_HWID = getBooleanProperty(guardSettings, "KickWithEmptyHWID", true);
				PROTECT_KICK_WITH_LASTERROR_HWID = getBooleanProperty(guardSettings, "KickWithLastErrorHWID", false);
				PROTECT_ENABLE_GG_SYSTEM = getBooleanProperty(guardSettings, "EnableGGSystem", true);
				PROTECT_GG_SEND_INTERVAL = getLongProperty(guardSettings, "GGSendInterval", 60000);
				PROTECT_GG_RECV_INTERVAL = getLongProperty(guardSettings, "GGRecvInterval", 8000);
				PROTECT_TASK_GG_INVERVAL = getLongProperty(guardSettings, "GGTaskInterval", 5000);
				PROTECT_ENABLE_HWID_LOCK = getBooleanProperty(guardSettings, "EnableHWIDLock", false);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	protected static Properties getSettings(String CONFIGURATION_FILE) throws Exception
	{
		Properties serverSettings = new Properties();
		InputStream is = new FileInputStream(new File(CONFIGURATION_FILE));
		serverSettings.load(is);
		is.close();
		return serverSettings;
	}
	
	protected static String getProperty(Properties prop, String name)
	{
		return prop.getProperty(name.trim(), null);
	}
	
	protected static String getProperty(Properties prop, String name, String _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : s;
	}
	
	protected static int getIntProperty(Properties prop, String name, int _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Integer.parseInt(s.trim());
	}
	
	protected static int getIntHexProperty(Properties prop, String name, int _default)
	{
		return (int) getLongHexProperty(prop, name, _default);
	}
	
	protected static long getLongProperty(Properties prop, String name, long _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Long.parseLong(s.trim());
	}
	
	protected static long getLongHexProperty(Properties prop, String name, long _default)
	{
		String s = getProperty(prop, name);
		if (s == null)
		{
			return _default;
		}
		s = s.trim();
		if (!s.startsWith("0x"))
		{
			s = "0x" + s;
		}
		return Long.decode(s).longValue();
	}
	
	protected static byte getByteProperty(Properties prop, String name, byte _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Byte.parseByte(s.trim());
	}
	
	protected static byte getByteProperty(Properties prop, String name, int _default)
	{
		return getByteProperty(prop, name, (byte) _default);
	}
	
	protected static boolean getBooleanProperty(Properties prop, String name, boolean _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Boolean.parseBoolean(s.trim());
	}
	
	protected static float getFloatProperty(Properties prop, String name, float _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Float.parseFloat(s.trim());
	}
	
	protected static float getFloatProperty(Properties prop, String name, double _default)
	{
		return getFloatProperty(prop, name, (float) _default);
	}
	
	protected static double getDoubleProperty(Properties prop, String name, double _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : Double.parseDouble(s.trim());
	}
	
	protected static int[] getIntArray(Properties prop, String name, int[] _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : parseCommaSeparatedIntegerArray(s.trim());
	}
	
	protected static float[] getFloatArray(Properties prop, String name, float[] _default)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : parseCommaSeparatedFloatArray(s.trim());
	}
	
	protected static String[] getStringArray(Properties prop, String name, String[] _default, String delimiter)
	{
		String s = getProperty(prop, name);
		return s == null ? _default : s.split(delimiter);
	}
	
	protected static String[] getStringArray(Properties prop, String name, String[] _default)
	{
		return getStringArray(prop, name, _default, ",");
	}
	
	protected static float[] parseCommaSeparatedFloatArray(String s)
	{
		if (s.isEmpty())
		{
			return new float[0];
		}
		String[] tmp = s.replaceAll(",", ";").split(";");
		float[] ret = new float[tmp.length];
		for (int i = 0; i < tmp.length; i++)
		{
			ret[i] = Float.parseFloat(tmp[i]);
		}
		return ret;
	}
	
	protected static int[] parseCommaSeparatedIntegerArray(String s)
	{
		if (s.isEmpty())
		{
			return new int[0];
		}
		String[] tmp = s.replaceAll(",", ";").split(";");
		int[] ret = new int[tmp.length];
		for (int i = 0; i < tmp.length; i++)
		{
			ret[i] = Integer.parseInt(tmp[i]);
		}
		return ret;
	}
}