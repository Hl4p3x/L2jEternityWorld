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

import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class TerritoryStatus implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"TerritoryStatus"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!target.isNpc())
		{
			return false;
		}
		
		final L2Npc npc = (L2Npc) target;
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		{
			if (npc.getCastle().getOwnerId() > 0)
			{
				html.setFile(activeChar.getLang(), "data/html/territorystatus.htm");
				L2Clan clan = ClanHolder.getInstance().getClan(npc.getCastle().getOwnerId());
				html.replace("%clanname%", clan.getName());
				html.replace("%clanleadername%", clan.getLeaderName());
			}
			else
			{
				html.setFile(activeChar.getLang(), "data/html/territorynoclan.htm");
			}
		}
		html.replace("%castlename%", npc.getCastle().getName());
		html.replace("%taxpercent%", "" + npc.getCastle().getTaxPercent());
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		{
			if (npc.getCastle().getId() > 6)
			{
				html.replace("%territory%", "The Kingdom of Elmore");
			}
			else
			{
				html.replace("%territory%", "The Kingdom of Aden");
			}
		}
		activeChar.sendPacket(html);
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}