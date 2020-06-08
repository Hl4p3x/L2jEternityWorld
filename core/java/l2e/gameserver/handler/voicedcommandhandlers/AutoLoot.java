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
package l2e.gameserver.handler.voicedcommandhandlers;

import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class AutoLoot implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"autoloot",
		"autolootherbs"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("autoloot"))
		{
			if (activeChar.getUseAutoLoot())
			{
				activeChar.setVar("useAutoLoot@", "0");
				activeChar.sendMessage((new CustomMessage("AutoLoot.AUTO_LOOT_DISABLED", activeChar.getLang())).toString());
			}
			else
			{
				activeChar.setVar("useAutoLoot@", "1");
				activeChar.sendMessage((new CustomMessage("AutoLoot.AUTO_LOOT_ENABLED", activeChar.getLang())).toString());
			}
		}
		else if (command.equalsIgnoreCase("autolootherbs"))
		{
			if (activeChar.getUseAutoLootHerbs())
			{
				activeChar.setVar("useAutoLootHerbs@", "0");
				activeChar.sendMessage((new CustomMessage("AutoLoot.HERB_AUTO_LOOT_DISABLED", activeChar.getLang())).toString());
			}
			else
			{
				activeChar.setVar("useAutoLootHerbs@", "1");
				activeChar.sendMessage((new CustomMessage("AutoLoot.HERB_AUTO_LOOT_ENABLED", activeChar.getLang())).toString());
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}