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

import l2e.gameserver.data.xml.CategoryParser;
import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;

public class SupportMagic implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"supportmagicservitor",
		"supportmagic"
	};
	
	private static final SkillsHolder HASTE_1 = new SkillsHolder(4327, 1);
	private static final SkillsHolder HASTE_2 = new SkillsHolder(5632, 1);
	private static final SkillsHolder CUBIC = new SkillsHolder(4338, 1);
	
	private static final SkillsHolder[] FIGHTER_BUFFS =
	{
		new SkillsHolder(4322, 1),
		new SkillsHolder(4323, 1),
		new SkillsHolder(4324, 1),
		new SkillsHolder(4325, 1),
		new SkillsHolder(4326, 1),
	};
	private static final SkillsHolder[] MAGE_BUFFS =
	{
		new SkillsHolder(4322, 1),
		new SkillsHolder(4323, 1),
		new SkillsHolder(4328, 1),
		new SkillsHolder(4329, 1),
		new SkillsHolder(4330, 1),
		new SkillsHolder(4331, 1),
	};
	private static final SkillsHolder[] SUMMON_BUFFS =
	{
		new SkillsHolder(4322, 1),
		new SkillsHolder(4323, 1),
		new SkillsHolder(4324, 1),
		new SkillsHolder(4325, 1),
		new SkillsHolder(4326, 1),
		new SkillsHolder(4328, 1),
		new SkillsHolder(4329, 1),
		new SkillsHolder(4330, 1),
		new SkillsHolder(4331, 1),
	};
	
	private static final int LOWEST_LEVEL = 6;
	private static final int HIGHEST_LEVEL = 75;
	private static final int CUBIC_LOWEST = 16;
	private static final int CUBIC_HIGHEST = 34;
	private static final int HASTE_LEVEL_2 = 40;
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!target.isNpc() || activeChar.isCursedWeaponEquipped())
		{
			return false;
		}
		
		if (command.equalsIgnoreCase(COMMANDS[0]))
		{
			makeSupportMagic(activeChar, (L2Npc) target, true);
		}
		else if (command.equalsIgnoreCase(COMMANDS[1]))
		{
			makeSupportMagic(activeChar, (L2Npc) target, false);
		}
		return true;
	}
	
	private static void makeSupportMagic(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		final int level = player.getLevel();
		if (isSummon && !player.hasServitor())
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicNoSummon.htm");
			return;
		}
		else if (level > HIGHEST_LEVEL)
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicHighLevel.htm");
			return;
		}
		else if (level < LOWEST_LEVEL)
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicLowLevel.htm");
			return;
		}
		else if (player.getClassId().level() == 3)
		{
			player.sendMessage("Only adventurers who have not completed their 3rd class transfer may receive these buffs.");
			return;
		}
		
		if (isSummon)
		{
			npc.setTarget(player.getSummon());
			for (SkillsHolder skill : SUMMON_BUFFS)
			{
				npc.doCast(skill.getSkill());
			}
			
			if (level >= HASTE_LEVEL_2)
			{
				npc.doCast(HASTE_2.getSkill());
			}
			else
			{
				npc.doCast(HASTE_1.getSkill());
			}
		}
		else
		{
			npc.setTarget(player);
			if (CategoryParser.getInstance().isInCategory(CategoryType.BEGINNER_MAGE, player.getClassId().getId()))
			{
				for (SkillsHolder skill : MAGE_BUFFS)
				{
					npc.doCast(skill.getSkill());
				}
			}
			else
			{
				for (SkillsHolder skill : FIGHTER_BUFFS)
				{
					npc.doCast(skill.getSkill());
				}
				
				if (level >= HASTE_LEVEL_2)
				{
					npc.doCast(HASTE_2.getSkill());
				}
				else
				{
					npc.doCast(HASTE_1.getSkill());
				}
			}
			
			if ((level >= CUBIC_LOWEST) && (level <= CUBIC_HIGHEST))
			{
				player.doSimultaneousCast(CUBIC.getSkill());
			}
		}
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}