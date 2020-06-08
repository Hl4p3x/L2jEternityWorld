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

import l2e.gameserver.model.TradeItem;

public final class TradeOtherAdd extends L2GameServerPacket
{
	private final TradeItem _item;
	
	public TradeOtherAdd(TradeItem item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1b);
		
		writeH(1);
		writeH(0);
		writeD(_item.getObjectId());
		writeD(_item.getItem().getDisplayId());
		writeQ(_item.getCount());
		writeH(_item.getItem().getType2());
		writeH(_item.getCustomType1());
		
		writeD(_item.getItem().getBodyPart());
		writeH(_item.getEnchant());
		writeH(0x00);
		writeH(_item.getCustomType2());
		
		writeH(_item.getAttackElementType());
		writeH(_item.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
			writeH(_item.getElementDefAttr(i));
		
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
	}
}