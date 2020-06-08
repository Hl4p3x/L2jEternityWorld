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
import l2e.gameserver.util.Util;
import l2e.scripts.events.LastHero;
import l2e.scripts.events.LastHero.EventState;

public class LastHeroCmd implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"LH_SwitchEnabled",
		"LH_IsEnabled",
		"LH_TimeLeft",
		"LH_StartNow"
	};

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equalsIgnoreCase("LH_SwitchEnabled"))
		{
			if (activeChar.isGM())
			{
				Config.LH_ENABLED = !Config.LH_ENABLED;
				activeChar.sendMessage(""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.EVENT") +" "+((Config.LH_ENABLED)? ""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.ENABLE") +"" : ""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.DISABLE") +"")+".");
			}
			else
				activeChar.sendMessage(""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.GM_EDIT") +"");
		}
		else if (command.equalsIgnoreCase("LH_IsEnabled"))
		{
			activeChar.sendMessage(""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.EVENT") +" "+((Config.LH_ENABLED)? ""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.ENABLE") +"" : ""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.DISABLE") +"")+".");
		}
		else if (command.equalsIgnoreCase("LH_TimeLeft"))
		{
			activeChar.sendMessage(Util.getTimeFromMilliseconds(LastHero.getMillisecondsUntilNextEvent()) +""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.BEFORE_STARTING") +"");
		}
		else if (command.equalsIgnoreCase("LH_StartNow"))
		{
			if (Config.LH_ENABLED)
			{
				if (activeChar.isGM())
				{
					if (LastHero.CurrentState == EventState.Inactive)
						LastHero.getInstance().StartNow();
					else
						activeChar.sendMessage(""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.EVENT_RUNING") +"");
				}
				else
					activeChar.sendMessage(""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.GM_EDIT") +"");
			}
			else
				activeChar.sendMessage(""+ LocalizationStorage.getInstance().getString(activeChar.getLang(), "LastHero.EVENT_DISABLED") +"");
		}
		return false;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}	
}