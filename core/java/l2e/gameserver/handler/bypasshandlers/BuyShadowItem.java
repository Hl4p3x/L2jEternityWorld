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

import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MerchantInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class BuyShadowItem implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"BuyShadowItem"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2MerchantInstance))
		{
			return false;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(((L2Npc) target).getObjectId());
		if (activeChar.getLevel() < 40)
		{
			html.setFile(activeChar.getLang(), "data/html/common/shadow_item-lowlevel.htm");
		}
		else if ((activeChar.getLevel() >= 40) && (activeChar.getLevel() < 46))
		{
			html.setFile(activeChar.getLang(), "data/html/common/shadow_item_d.htm");
		}
		else if ((activeChar.getLevel() >= 46) && (activeChar.getLevel() < 52))
		{
			html.setFile(activeChar.getLang(), "data/html/common/shadow_item_c.htm");
		}
		else if (activeChar.getLevel() >= 52)
		{
			html.setFile(activeChar.getLang(), "data/html/common/shadow_item_b.htm");
		}
		html.replace("%objectId%", String.valueOf(((L2Npc) target).getObjectId()));
		activeChar.sendPacket(html);
		
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}