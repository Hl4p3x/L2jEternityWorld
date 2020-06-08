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
package l2e.gameserver.handler.itemhandlers;

import l2e.Config;
import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class AIOItem implements IItemHandler
{
	private static final int ITEM_IDS = Config.AIO_ITEM_ID;
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (playable == null)
		{
			return false;
		}
		L2PcInstance player = null;
		if (!(playable instanceof L2PcInstance))
		{
			return false;
		}
		
		player = (L2PcInstance) playable;
		
		int itemId = item.getId();
		if (itemId == Config.AIO_ITEM_ID)
		{
			String htmFile = "data/html/AioItemNpcs/main.htm";
			NpcHtmlMessage msg = new NpcHtmlMessage(1);
			msg.setFile(player.getLang(), htmFile);
			player.sendPacket(msg);
		}
		return true;
	}
	
	public int getItemIds()
	{
		return ITEM_IDS;
	}
}