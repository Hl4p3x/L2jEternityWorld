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

import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import l2e.Config;
import l2e.loginserver.GameServerThread;
import l2e.loginserver.network.L2JGameServerPacketHandler.GameServerState;
import l2e.util.crypt.NewCrypt;
import l2e.util.network.BaseRecievePacket;

public class BlowFishKey extends BaseRecievePacket
{
	protected static final Logger _log = Logger.getLogger(BlowFishKey.class.getName());
	
	public BlowFishKey(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		int size = readD();
		byte[] tempKey = readB(size);
		try
		{
			byte [] tempDecryptKey;
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, server.getPrivateKey());
			tempDecryptKey = rsaCipher.doFinal(tempKey);

			int i = 0;
			int len = tempDecryptKey.length;
			for(; i < len; i++)
			{
				if(tempDecryptKey[i] != 0)
					break;
			}
			byte[] key = new byte[len-i];
			System.arraycopy(tempDecryptKey,i,key,0,len-i);
			
			server.SetBlowFish(new NewCrypt(key));
			if (Config.DEBUG)
			{
				_log.info("New BlowFish key received, Blowfih Engine initialized:");
			}
			server.setLoginConnectionState(GameServerState.BF_CONNECTED);
		}
		catch(GeneralSecurityException e)
		{
			_log.log(Level.SEVERE, "Error While decrypting blowfish key (RSA): " + e.getMessage(), e);
		}
	}
}