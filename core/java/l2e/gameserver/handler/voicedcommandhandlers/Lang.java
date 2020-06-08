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

import l2e.Config;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class Lang implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"lang"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if ((target == null) || !Config.MULTILANG_ALLOWED.contains(target))
		{
			String answer = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Lang.WRONG_LANG") + "";
			for (String lang : Config.MULTILANG_ALLOWED)
			{
				answer += " " + lang;
			}
			activeChar.sendMessage(answer);
		}
		else
		{
			activeChar.setLang(target);
			if (target.equalsIgnoreCase("en"))
			{
				activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Lang.EN_LANG") + "");
			}
			else if (target.equalsIgnoreCase("ru"))
			{
				activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Lang.RU_LANG") + "");
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