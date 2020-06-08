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

import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class Disguise implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		L2PcInstance activeChar = playable.getActingPlayer();
		
		int regId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar);
		if (regId > 0 && regId == (item.getId() - 13596))
		{
			if (activeChar.getClan() != null && activeChar.getClan().getCastleId() > 0)
			{
				activeChar.sendPacket(SystemMessageId.TERRITORY_OWNING_CLAN_CANNOT_USE_DISGUISE_SCROLL);
				return false;
			}
			TerritoryWarManager.getInstance().addDisguisedPlayer(activeChar.getObjectId());
			activeChar.broadcastUserInfo();
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
			return true;
		}
		else if (regId > 0)
		{
			activeChar.sendPacket(SystemMessageId.THE_DISGUISE_SCROLL_MEANT_FOR_DIFFERENT_TERRITORY);
			return false;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TERRITORY_WAR_SCROLL_CAN_NOT_USED_NOW);
			return false;
		}
	}
}