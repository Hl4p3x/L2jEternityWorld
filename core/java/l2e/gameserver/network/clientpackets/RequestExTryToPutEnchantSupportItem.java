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

import l2e.gameserver.data.xml.EnchantItemParser;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.enchant.EnchantItem;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPutEnchantSupportItemResult;

public class RequestExTryToPutEnchantSupportItem extends L2GameClientPacket
{
	private int _supportObjectId;
	private int _enchantObjectId;
	
	@Override
	protected void readImpl()
	{
		_supportObjectId = readD();
		_enchantObjectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = this.getClient().getActiveChar();
		if (activeChar != null)
		{
			if (activeChar.isEnchanting())
			{
				L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_enchantObjectId);
				L2ItemInstance support = activeChar.getInventory().getItemByObjectId(_supportObjectId);
				
				if ((item == null) || (support == null))
				{
					return;
				}
				
				EnchantItem supportTemplate = EnchantItemParser.getInstance().getSupportItem(support);
				
				if ((supportTemplate == null) || !supportTemplate.isValid(item))
				{
					activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
					activeChar.setActiveEnchantSupportItemId(L2PcInstance.ID_NONE);
					activeChar.sendPacket(new ExPutEnchantSupportItemResult(0));
					return;
				}
				activeChar.setActiveEnchantSupportItemId(support.getObjectId());
				activeChar.sendPacket(new ExPutEnchantSupportItemResult(_supportObjectId));
			}
		}
	}
}