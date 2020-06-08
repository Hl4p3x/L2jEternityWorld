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
package l2e.gameserver.network.serverpackets;

import l2e.gameserver.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
	public static final ClientSetTime STATIC_PACKET = new ClientSetTime();
	
	private ClientSetTime()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf2);
		writeD(GameTimeController.getInstance().getGameTime());
		writeD(6);
	}
}