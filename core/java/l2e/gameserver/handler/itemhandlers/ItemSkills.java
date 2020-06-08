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

import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class ItemSkills extends ItemSkillsTemplate
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		final L2PcInstance activeChar = playable.getActingPlayer();
		if ((activeChar != null) && activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}
		
		if ((activeChar != null) && (activeChar.getPvpFlag() != 0) && ((item.getId() == 1538) || (item.getId() == 3958) || (item.getId() == 13750) || (item.getId() == 5858) || (item.getId() == 5859) || (item.getId() == 9156) || (item.getId() == 10130) || (item.getId() == 13258) || (item.getId() == 13731) || (item.getId() == 13732) || (item.getId() == 13733) || (item.getId() == 13734) || (item.getId() == 13735) || (item.getId() == 13736) || (item.getId() == 13737) || (item.getId() == 13738) || (item.getId() == 13739) || (item.getId() == 20583) || (item.getId() == 21195)))
		{
			activeChar.sendMessage("You can't use while you are flagged.");
			return false;
		}
		return super.useItem(playable, item, forceUse);
	}
}