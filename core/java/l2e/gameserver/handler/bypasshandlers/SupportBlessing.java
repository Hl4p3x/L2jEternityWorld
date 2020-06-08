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

import l2e.gameserver.data.sql.SkillHolder.FrequentSkill;
import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class SupportBlessing implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"GiveBlessing"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!target.isNpc())
		{
			return false;
		}
		
		final L2Npc npc = (L2Npc) target;
		
		if ((activeChar.getLevel() > 39) || (activeChar.getClassId().level() >= 2))
		{
			npc.showChatWindow(activeChar, "data/html/default/SupportBlessingHighLevel.htm");
			return true;
		}
		npc.setTarget(activeChar);
		npc.doCast(FrequentSkill.BLESSING_OF_PROTECTION.getSkill());
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}