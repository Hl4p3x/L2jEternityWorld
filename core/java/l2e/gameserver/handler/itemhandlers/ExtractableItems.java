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

import java.util.List;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.model.L2ExtractableProduct;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2EtcItem;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.util.Rnd;

public class ExtractableItems implements IItemHandler
{
	private static Logger _log = Logger.getLogger(ItemHolder.class.getName());
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		final L2EtcItem etcitem = (L2EtcItem) item.getItem();
		final List<L2ExtractableProduct> exitem = etcitem.getExtractableItems();
		if (exitem == null)
		{
			_log.info("No extractable data defined for " + etcitem);
			return false;
		}
		
		if (!activeChar.destroyItem("Extract", item.getObjectId(), 1, activeChar, true))
		{
			return false;
		}
		
		boolean created = false;
		int min;
		int max;
		int createitemAmount;
		for (L2ExtractableProduct expi : exitem)
		{
			if (Rnd.get(100000) <= expi.getChance())
			{
				min = (int) (expi.getMin() * Config.RATE_EXTRACTABLE);
				max = (int) (expi.getMax() * Config.RATE_EXTRACTABLE);
				
				createitemAmount = (max == min) ? min : (Rnd.get((max - min) + 1) + min);
				activeChar.addItem("Extract", expi.getId(), createitemAmount, activeChar, true);
				created = true;
			}
		}
		
		if (!created)
		{
			activeChar.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
		}
		return true;
	}
}