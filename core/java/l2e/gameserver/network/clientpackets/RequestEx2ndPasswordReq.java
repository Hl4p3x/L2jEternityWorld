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
package l2e.gameserver.network.clientpackets;

import l2e.Config;
import l2e.gameserver.network.serverpackets.Ex2ndPasswordAck;
import l2e.gameserver.security.SecondaryPasswordAuth;

public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
	private int _changePass;
	private String _password, _newPassword;
	
	@Override
	protected void readImpl()
	{
		_changePass = readC();
		_password = readS();
		if (_changePass == 2)
		{
			_newPassword = readS();
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (!Config.SECOND_AUTH_ENABLED)
		{
			return;
		}
		
		SecondaryPasswordAuth spa = getClient().getSecondaryAuth();
		boolean exVal = false;
		
		if ((_changePass == 0) && !spa.passwordExist())
		{
			exVal = spa.savePassword(_password);
		}
		else if ((_changePass == 2) && spa.passwordExist())
		{
			exVal = spa.changePassword(_password, _newPassword);
		}
		
		if (exVal)
		{
			getClient().sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.SUCCESS));
		}
	}
}