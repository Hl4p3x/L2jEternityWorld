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

import java.util.logging.Logger;

import l2e.Config;
import l2e.loginserver.GameServerThread;
import l2e.loginserver.LoginController;
import l2e.loginserver.SessionKey;
import l2e.loginserver.network.loginserverpackets.PlayerAuthResponse;
import l2e.util.network.BaseRecievePacket;

public class PlayerAuthRequest extends BaseRecievePacket
{
	private static Logger _log = Logger.getLogger(PlayerAuthRequest.class.getName());
	
	public PlayerAuthRequest(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		String account = readS();
		int playKey1 = readD();
		int playKey2 = readD();
		int loginKey1 = readD();
		int loginKey2 = readD();
		
		SessionKey sessionKey = new SessionKey(loginKey1, loginKey2, playKey1, playKey2);
		
		PlayerAuthResponse authResponse;
		if (Config.DEBUG)
		{
			_log.info("auth request received for Player " + account);
		}
		SessionKey key = LoginController.getInstance().getKeyForAccount(account);
		if ((key != null) && key.equals(sessionKey))
		{
			if (Config.DEBUG)
			{
				_log.info("auth request: OK");
			}
			LoginController.getInstance().removeAuthedLoginClient(account);
			authResponse = new PlayerAuthResponse(account, true);
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.info("auth request: NO");
				_log.info("session key from self: " + key);
				_log.info("session key sent: " + sessionKey);
			}
			authResponse = new PlayerAuthResponse(account, false);
		}
		server.sendPacket(authResponse);
	}
}