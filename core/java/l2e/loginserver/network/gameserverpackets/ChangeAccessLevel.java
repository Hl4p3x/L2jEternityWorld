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

import l2e.loginserver.GameServerThread;
import l2e.loginserver.LoginController;
import l2e.util.network.BaseRecievePacket;

public class ChangeAccessLevel extends BaseRecievePacket
{
	protected static Logger _log = Logger.getLogger(ChangeAccessLevel.class.getName());
	
	public ChangeAccessLevel(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		int level = readD();
		String account = readS();
		
		LoginController.getInstance().setAccountAccessLevel(account, level);
		_log.info("Changed "+account+" access level to "+level);
	}	
}