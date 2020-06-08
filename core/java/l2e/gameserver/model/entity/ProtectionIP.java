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
package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class ProtectionIP
{
	public static void onEnterWorld(L2PcInstance player)
	{
		String lang = player.getLang();
		String last = "";
		String curr = "";
		try
		{
			last = LastIP(player);
			curr = player.getClient().getConnection().getInetAddress().getHostAddress();
		}
		catch (Exception e)
		{
		}
		
		if (Config.PROTECTION_IP_ENABLED)
		{
			player.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "ProtectionIP.LAST_IP") + " " + last + " " + LocalizationStorage.getInstance().getString(lang, "ProtectionIP.CURRENT_IP") + " " + curr);
		}
		
		UpdateLastIP(player, player.getAccountName());
	}
	
	public static String LastIP(L2PcInstance player)
	{
		String lastIp = "";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			ResultSet rset;
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `accounts` WHERE login = ?");
			statement.setString(1, player.getAccountName());
			rset = statement.executeQuery();
			while (rset.next())
			{
				lastIp = rset.getString("lastIP");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return lastIp;
	}
	
	public static void UpdateLastIP(L2PcInstance player, String user)
	{
		String address = player.getClient().getConnection().getInetAddress().getHostAddress();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE accounts SET lastIP=? WHERE login=?");
			statement.setString(1, address);
			statement.setString(2, user);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}