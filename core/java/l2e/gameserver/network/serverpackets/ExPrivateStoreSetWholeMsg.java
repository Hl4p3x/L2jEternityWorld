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

public class ExPrivateStoreSetWholeMsg extends L2GameServerPacket
{
	private final int _objectId;
	private final String _msg;
	
	public ExPrivateStoreSetWholeMsg(L2PcInstance player, String msg)
	{
		_objectId = player.getObjectId();
		_msg = msg;
	}
	
	public ExPrivateStoreSetWholeMsg(L2PcInstance player)
	{
		this(player, player.getSellList().getTitle());
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x80);
		writeD(_objectId);
		writeS(_msg);
	}	
}