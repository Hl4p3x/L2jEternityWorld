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

import l2e.Config;
import l2e.gameserver.model.actor.instance.L2PcInstance;

/**
 * Created by LordWinter 06.10.2011
 * Fixed by L2J Eternity-World
 */
public class ExBrGamePoint extends L2GameServerPacket
{
	private int _objId;
	private long _points;

	public ExBrGamePoint(L2PcInstance player)
	{
		_objId = player.getObjectId();

		if(Config.GAME_POINT_ITEM_ID == -1)
			_points = player.getGamePoints();
		else
			_points = player.getInventory().getInventoryItemCount(Config.GAME_POINT_ITEM_ID, -100);
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
	 	writeH(0xD5);
		writeD(_objId);
		writeQ(_points);
		writeD(0x00);
	}
}