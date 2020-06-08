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

import java.util.List;

import javolution.util.FastList;
import l2e.gameserver.model.actor.instance.L2MerchantInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public class SellList extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2MerchantInstance _lease;
	private final long _money;
	private final List<L2ItemInstance> _selllist = new FastList<>();
	
	public SellList(L2PcInstance player)
	{
		_activeChar = player;
		_lease = null;
		_money = _activeChar.getAdena();
		doLease();
	}
	
	public SellList(L2PcInstance player, L2MerchantInstance lease)
	{
		_activeChar = player;
		_lease = lease;
		_money = _activeChar.getAdena();
		doLease();
	}
	
	private void doLease()
	{
		if (_lease == null)
		{
			for (L2ItemInstance item : _activeChar.getInventory().getItems())
			{
				if (!item.isEquipped() && item.isSellable() && (!_activeChar.hasSummon() || (item.getObjectId() != _activeChar.getSummon().getControlObjectId()))) // Pet is summoned and not the item that summoned the pet
				{
					_selllist.add(item);
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x06);
		writeQ(_money);
		writeD(_lease == null ? 0x00 : 1000000 + _lease.getTemplate().getId());
		writeH(_selllist.size());
		
		for (L2ItemInstance item : _selllist)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getDisplayId());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.isEquipped() ? 0x01 : 0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0x00);
			writeH(item.getCustomType2());
			writeQ(item.getItem().getReferencePrice() / 2);
			
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(item.getElementDefAttr(i));
			}
			
			for (int op : item.getEnchantOptions())
			{
				writeH(op);
			}
		}
	}
}