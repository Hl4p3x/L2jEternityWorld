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

import static l2e.gameserver.data.xml.MultiSellParser.PAGE_SIZE;
import l2e.gameserver.model.multisell.Entry;
import l2e.gameserver.model.multisell.Ingredient;
import l2e.gameserver.model.multisell.ListContainer;

public final class MultiSellList extends L2GameServerPacket
{
	private int _size, _index;
	private final ListContainer _list;
	private final boolean _finished;
	
	public MultiSellList(ListContainer list, int index)
	{
		_list = list;
		_index = index;
		_size = list.getEntries().size() - index;
		if (_size > PAGE_SIZE)
		{
			_finished = false;
			_size = PAGE_SIZE;
		}
		else
		{
			_finished = true;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xd0);
		writeD(_list.getListId());
		writeD(1 + (_index / PAGE_SIZE));
		writeD(_finished ? 1 : 0);
		writeD(PAGE_SIZE);
		writeD(_size);
		
		Entry ent;
		while (_size-- > 0)
		{
			ent = _list.getEntries().get(_index++);
			writeD(ent.getEntryId());
			writeC(ent.isStackable() ? 1 : 0);
			writeH(0x00);
			writeD(0x00);
			writeD(0x00);
			writeH(65534);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			
			writeH(ent.getProducts().size());
			writeH(ent.getIngredients().size());
			
			for (Ingredient ing : ent.getProducts())
			{
				writeD(ing.getItemId());
				if (ing.getTemplate() != null)
				{
					writeD(ing.getTemplate().getBodyPart());
					writeH(ing.getTemplate().getType2());
				}
				else
				{
					writeD(0);
					writeH(65535);
				}
				writeQ(ing.getItemCount());
				if (ing.getItemInfo() != null)
				{
					writeH(ing.getItemInfo().getEnchantLevel());
					writeD(ing.getItemInfo().getAugmentId());
					writeD(0x00);
					writeH(ing.getItemInfo().getElementId());
					writeH(ing.getItemInfo().getElementPower());
					writeH(ing.getItemInfo().getElementals()[0]);
					writeH(ing.getItemInfo().getElementals()[1]);
					writeH(ing.getItemInfo().getElementals()[2]);
					writeH(ing.getItemInfo().getElementals()[3]);
					writeH(ing.getItemInfo().getElementals()[4]);
					writeH(ing.getItemInfo().getElementals()[5]);
				}
				else
				{
					writeH(0x00);
					writeD(0x00);
					writeD(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
				}
			}
			
			for (Ingredient ing : ent.getIngredients())
			{
				writeD(ing.getItemId());
				writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
				writeQ(ing.getItemCount());
				if (ing.getItemInfo() != null)
				{
					writeH(ing.getItemInfo().getEnchantLevel());
					writeD(ing.getItemInfo().getAugmentId());
					writeD(0x00);
					writeH(ing.getItemInfo().getElementId());
					writeH(ing.getItemInfo().getElementPower());
					writeH(ing.getItemInfo().getElementals()[0]);
					writeH(ing.getItemInfo().getElementals()[1]);
					writeH(ing.getItemInfo().getElementals()[2]);
					writeH(ing.getItemInfo().getElementals()[3]);
					writeH(ing.getItemInfo().getElementals()[4]);
					writeH(ing.getItemInfo().getElementals()[5]);
				}
				else
				{
					writeH(0x00);
					writeD(0x00);
					writeD(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
					writeH(0x00);
				}
			}
		}
	}
}