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
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Menu implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"menu"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("menu"))
		{
			showConfigMenu(activeChar);
		}
		else if (command.startsWith("menu"))
		{
			String[] params = command.split(" ");
			if ((params.length == 4) && params[1].equalsIgnoreCase("set_var"))
			{
				activeChar.setVar(params[2], params[3]);
				showConfigMenu(activeChar);
			}
			else
			{
				showConfigMenu(activeChar);
			}
		}
		return true;
	}
	
	private void showConfigMenu(L2PcInstance activeChar)
	{
		String lang = activeChar.getLang();
		String info = "";
		if (Config.MULTILANG_ENABLE && (Config.MULTILANG_ALLOWED.size() > 1))
		{
			info += "<tr>";
			info += "<td width=5></td>";
			info += "<td width=130>" + LocalizationStorage.getInstance().getString(lang, "Menu.STRING_LANGUAGE") + ":</td>";
			info += "<td width=65><font color=\"LEVEL\">" + lang.toUpperCase() + "</font></td>";
			if (Config.MULTILANG_VOICED_ALLOW)
			{
				int count1 = Config.MULTILANG_ALLOWED.size() / 2;
				int i = 0;
				info += "<td width=30>";
				for (String lng : Config.MULTILANG_ALLOWED)
				{
					i++;
					info += "<button width=30 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_menu set_var lang@ " + lng + "\" value=\"" + lng.toUpperCase() + "\">";
					if (i == count1)
					{
						info += "</td><td width=30>";
					}
				}
				info += "</td>";
			}
			else
			{
				info += "<td width=30>" + LocalizationStorage.getInstance().getString(lang, "Menu.STRING_NOT_AVAILABLE") + "</td>";
				info += "<td width=30></td>";
			}
			info += "</tr>";
		}
		if (Config.AUTO_LOOT)
		{
			info += getBooleanFrame(activeChar, LocalizationStorage.getInstance().getString(lang, "Menu.STRING_AUTOLOOT"), "useAutoLoot@", Config.ALLOW_AUTOLOOT_COMMAND);
		}
		else
		{
			info += getManualBooleanFrame(activeChar, LocalizationStorage.getInstance().getString(lang, "Menu.STRING_AUTOLOOT"), false);
		}
		if (Config.AUTO_LOOT_HERBS)
		{
			info += getBooleanFrame(activeChar, LocalizationStorage.getInstance().getString(lang, "Menu.STRING_AUTOLOOT_HERBS"), "useAutoLootHerbs@", Config.ALLOW_AUTOLOOT_COMMAND);
		}
		else
		{
			info += getManualBooleanFrame(activeChar, LocalizationStorage.getInstance().getString(lang, "Menu.STRING_AUTOLOOT_HERBS"), false);
		}
		info += getBooleanFrame(activeChar, LocalizationStorage.getInstance().getString(lang, "Menu.STRING_BLOCK_XP"), "blockedEXP@", Config.ALLOW_EXP_GAIN_COMMAND);
		String text = "<html><title>" + LocalizationStorage.getInstance().getString(lang, "Menu.STRING_CONFIGS") + "</title><body><br>";
		if (info.equals(""))
		{
			text += LocalizationStorage.getInstance().getString(lang, "Menu.STRING_NO_CONFIGS");
		}
		else
		{
			text += "<table>";
			text += info;
			text += "</table>";
		}
		text += "</body></html>";
		activeChar.sendPacket(new NpcHtmlMessage(6, text));
	}
	
	private final static String _ONText = "<font color=\"00FF00\">ON</font>";
	private final static String _OFFText = "<font color=\"FF0000\">OFF</font>";
	
	private String getBooleanFrame(L2PcInstance activeChar, String configTitle, String configName, boolean allowtomod)
	{
		String info = "<tr>";
		info += "<td width=5></td>";
		info += "<td width=130>" + configTitle + ":</td>";
		info += "<td width=65>" + (activeChar.getVarB(configName) ? _ONText : _OFFText) + "</td>";
		if (allowtomod)
		{
			info += "<td width=30><button width=30 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_menu set_var " + configName + " 1\" value=\"On\"></td>";
			info += "<td width=30><button width=30 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h voiced_menu set_var " + configName + " 0\" value=\"Off\"></td>";
		}
		else
		{
			info += "<td width=30>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Menu.STRING_NOT_AVAILABLE") + "</td>";
			info += "<td width=30></td>";
		}
		info += "</tr>";
		return info;
	}
	
	private String getManualBooleanFrame(L2PcInstance activeChar, String configTitle, boolean isON)
	{
		String info = "<tr>";
		info += "<td width=5></td>";
		info += "<td width=130>" + configTitle + ":</td>";
		info += "<td width=65>" + (isON ? _ONText : _OFFText) + "</td>";
		info += "<td width=30>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Menu.STRING_NOT_AVAILABLE") + "</td>";
		info += "<td width=30></td>";
		info += "</tr>";
		return info;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}