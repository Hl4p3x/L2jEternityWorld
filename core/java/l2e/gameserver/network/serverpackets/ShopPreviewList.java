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

import java.util.Collection;

import l2e.Config;
import l2e.gameserver.model.buylist.L2BuyList;
import l2e.gameserver.model.buylist.Product;
import l2e.gameserver.model.items.L2Item;

public class ShopPreviewList extends L2GameServerPacket
{
	private final int _listId;
	private final Collection<Product> _list;
	private final long _money;
	private int _expertise;
	
	public ShopPreviewList(L2BuyList list, long currentMoney, int expertiseIndex)
	{
		_listId = list.getListId();
		_list = list.getProducts();
		_money = currentMoney;
		_expertise = expertiseIndex;
	}
	
	public ShopPreviewList(Collection<Product> lst, int listId, long currentMoney)
	{
		_listId = listId;
		_list = lst;
		_money = currentMoney;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xF5);
		writeC(0xC0);
		writeC(0x13);
		writeC(0x00);
		writeC(0x00);
		writeQ(_money);
		writeD(_listId);
		
		int newlength = 0;
		for (Product product : _list)
		{
			if ((product.getItem().getCrystalType() <= _expertise) && product.getItem().isEquipable())
			{
				newlength++;
			}
		}
		writeH(newlength);
		
		for (Product product : _list)
		{
			if ((product.getItem().getCrystalType() <= _expertise) && product.getItem().isEquipable())
			{
				writeD(product.getItemId());
				writeH(product.getItem().getType2());
				
				if (product.getItem().getType1() != L2Item.TYPE1_ITEM_QUESTITEM_ADENA)
				{
					writeH(product.getItem().getBodyPart());
				}
				else
				{
					writeH(0x00);
				}
				
				writeQ(Config.WEAR_PRICE);
			}
		}
	}
}