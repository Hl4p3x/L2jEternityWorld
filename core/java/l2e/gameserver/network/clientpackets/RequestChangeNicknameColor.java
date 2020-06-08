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

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public class RequestChangeNicknameColor extends L2GameClientPacket
{
	private static final int COLORS[] =
	{
		0x9393FF,
		0x7C49FC,
		0x97F8FC,
		0xFA9AEE,
		0xFF5D93,
		0x00FCA0,
		0xA0A601,
		0x7898AF,
		0x486295,
		0x999999
	};
	
	private int _colorNum, _itemObjectId;
	private String _title;
	
	@Override
	protected void readImpl()
	{
		_colorNum = readD();
		_title = readS();
		_itemObjectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_colorNum < 0) || (_colorNum >= COLORS.length))
		{
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjectId);
		if ((item == null) || (item.getEtcItem() == null) || (item.getEtcItem().getHandlerName() == null) || !item.getEtcItem().getHandlerName().equalsIgnoreCase("NicknameColor"))
		{
			return;
		}
		
		if (activeChar.destroyItem("Consume", item, 1, null, true))
		{
			activeChar.setTitle(_title);
			activeChar.getAppearance().setTitleColor(COLORS[_colorNum]);
			activeChar.broadcastUserInfo();
		}
	}
}