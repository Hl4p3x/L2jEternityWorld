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

import l2e.gameserver.model.actor.instance.L2PcInstance;

public class ObservationReturn extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	
	public ObservationReturn(L2PcInstance observer)
	{
		_activeChar = observer;
	}
	
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xEC);
		writeD(_activeChar.getLastX());
		writeD(_activeChar.getLastY());
		writeD(_activeChar.getLastZ());
	}
}