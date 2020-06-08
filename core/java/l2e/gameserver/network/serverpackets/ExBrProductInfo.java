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

import l2e.gameserver.data.xml.ProductItemParser;
import l2e.gameserver.model.L2ProductItem;
import l2e.gameserver.model.L2ProductItemComponent;

/**
 * Created by LordWinter 06.10.2011 Fixed by L2J Eternity-World
 */
public class ExBrProductInfo extends L2GameServerPacket
{
	private final L2ProductItem _productId;
	
	public ExBrProductInfo(int id)
	{
		_productId = ProductItemParser.getInstance().getProduct(id);
	}
	
	@Override
	protected void writeImpl()
	{
		if (_productId == null)
		{
			return;
		}
		
		writeC(0xFE);
		writeH(0xD7);
		
		writeD(_productId.getProductId());
		writeD(_productId.getPoints());
		writeD(_productId.getComponents().size());
		
		for (L2ProductItemComponent com : _productId.getComponents())
		{
			writeD(com.getItemId());
			writeD(com.getCount());
			writeD(com.getWeight());
			writeD(com.isDropable() ? 1 : 0);
		}
	}
}