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

import l2e.gameserver.network.L2GameClient;

public class GameGuardReply extends L2GameClientPacket
{
	private int _dx;
	
	@Override
	protected void readImpl()
	{
		_dx = readC();
	}
	
	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		
		// _log.warning("Player connection " + _dx + "");
		if (_dx == 0)
		{
			client.setGameGuardOk(true);
		}
		else
		{
			client.setGameGuardOk(false);
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}