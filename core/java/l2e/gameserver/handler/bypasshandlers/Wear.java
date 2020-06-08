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
package l2e.gameserver.handler.bypasshandlers;

import java.util.StringTokenizer;
import java.util.logging.Level;

import l2e.Config;
import l2e.gameserver.data.xml.BuyListParser;
import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.buylist.L2BuyList;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ShopPreviewList;

public class Wear implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Wear"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!target.isNpc())
		{
			return false;
		}
		
		if (!Config.ALLOW_WEAR)
		{
			return false;
		}
		
		try
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			if (st.countTokens() < 1)
			{
				return false;
			}
			
			showWearWindow(activeChar, Integer.parseInt(st.nextToken()));
			return true;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
	
	private static final void showWearWindow(L2PcInstance player, int val)
	{
		final L2BuyList buyList = BuyListParser.getInstance().getBuyList(val);
		if (buyList == null)
		{
			_log.warning("BuyList not found! BuyListId:" + val);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		player.setInventoryBlockingStatus(true);
		player.sendPacket(new ShopPreviewList(buyList, player.getAdena(), player.getExpertiseLevel()));
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}