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
package l2e.loginserver.network.gameserverpackets;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.loginserver.GameServerTable;
import l2e.loginserver.GameServerTable.GameServerInfo;
import l2e.loginserver.GameServerThread;
import l2e.util.Base64;
import l2e.util.network.BaseRecievePacket;

public class ChangePassword extends BaseRecievePacket
{
	protected static Logger _log = Logger.getLogger(ChangePassword.class.getName());
	private static GameServerThread gst = null;
	
	public ChangePassword(byte[] decrypt)
	{
		super(decrypt);
		
		String accountName = readS();
		String characterName = readS();
		String curpass = readS();
		String newpass = readS();
		
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			if ((gsi.getGameServerThread() != null) && gsi.getGameServerThread().hasAccountOnGameServer(accountName))
			{
				gst = gsi.getGameServerThread();
			}
		}
		
		if (gst == null)
		{
			return;
		}
		
		if ((curpass == null) || (newpass == null))
		{
			gst.ChangePasswordResponse((byte) 0, characterName, "Invalid password data! Try again.");
		}
		else
		{
			try
			{
				MessageDigest md = MessageDigest.getInstance("SHA");
				
				byte[] raw = curpass.getBytes("UTF-8");
				raw = md.digest(raw);
				String curpassEnc = Base64.encodeBytes(raw);
				String pass = null;
				int passUpdated = 0;
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement("SELECT password FROM accounts WHERE login=?"))
				{
					ps.setString(1, accountName);
					try (ResultSet rs = ps.executeQuery())
					{
						if (rs.next())
						{
							pass = rs.getString("password");
						}
					}
				}
				
				if (curpassEnc.equals(pass))
				{
					byte[] password = newpass.getBytes("UTF-8");
					password = md.digest(password);
					
					try (Connection con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement ps = con.prepareStatement("UPDATE accounts SET password=? WHERE login=?"))
					{
						ps.setString(1, Base64.encodeBytes(password));
						ps.setString(2, accountName);
						passUpdated = ps.executeUpdate();
					}
					
					_log.log(Level.INFO, "The password for account " + accountName + " has been changed from " + curpassEnc + " to " + Base64.encodeBytes(password));
					if (passUpdated > 0)
					{
						gst.ChangePasswordResponse((byte) 1, characterName, "You have successfully changed your password!");
					}
					else
					{
						gst.ChangePasswordResponse((byte) 0, characterName, "The password change was unsuccessful!");
					}
				}
				else
				{
					gst.ChangePasswordResponse((byte) 0, characterName, "The typed current password doesn't match with your current one.");
				}
			}
			catch (Exception e)
			{
				_log.warning("Error while changing password for account " + accountName + " requested by player " + characterName + "! " + e);
			}
		}
	}
}