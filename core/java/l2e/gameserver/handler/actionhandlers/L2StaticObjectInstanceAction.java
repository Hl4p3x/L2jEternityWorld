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
package l2e.gameserver.handler.actionhandlers;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.handler.IActionHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2StaticObjectInstanceAction implements IActionHandler
{
	@Override
	public boolean action(final L2PcInstance activeChar, final L2Object target, final boolean interact)
	{
		final L2StaticObjectInstance staticObject = (L2StaticObjectInstance) target;
		if (staticObject.getType() < 0)
		{
			_log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " + staticObject.getId());
		}
		
		if (activeChar.getTarget() != staticObject)
		{
			activeChar.setTarget(staticObject);
		}
		else if (interact)
		{
			if (!activeChar.isInsideRadius(staticObject, L2Npc.INTERACTION_DISTANCE, false, false))
			{
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, staticObject);
			}
			else
			{
				if (staticObject.getType() == 2)
				{
					final String filename = (staticObject.getId() == 24230101) ? "data/html/signboards/tomb_of_crystalgolem.htm" : "data/html/signboards/pvp_signboard.htm";
					final String content = HtmCache.getInstance().getHtm(activeChar.getLang(), filename);
					final NpcHtmlMessage html = new NpcHtmlMessage(staticObject.getObjectId());
					
					if (content == null)
					{
						html.setHtml("<html><body>Signboard is missing:<br>" + filename + "</body></html>");
					}
					else
					{
						html.setHtml(content);
					}
					
					activeChar.sendPacket(html);
				}
				else if (staticObject.getType() == 0)
				{
					activeChar.sendPacket(staticObject.getMap());
				}
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2StaticObjectInstance;
	}
}