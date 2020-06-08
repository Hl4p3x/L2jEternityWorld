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
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;

public class QuestItems implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceuse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		L2PcInstance player = playable.getActingPlayer();
		
		if (!player.destroyItem("Item Handler - QuestItems", item, player, true))
			return false;
		
		final L2Item itm = item.getItem();
		if (itm.getQuestEvents() == null)
		{
			_log.warning(QuestItems.class.getSimpleName() + ": Null list for item handler QuestItems!");
			return false;
		}

		for (Quest quest : itm.getQuestEvents())
		{
			QuestState state = player.getQuestState(quest.getName());
			if (state == null || !state.isStarted())
				continue;
			
			quest.notifyItemUse(itm, player);
		}
		return true;
	}
}