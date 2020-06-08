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
package l2e.gameserver.handler.admincommandhandlers;

import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptException;

import javolution.text.TextBuilder;
import l2e.Config;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomSQLs;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.AdminParser;
import l2e.gameserver.data.xml.BuyListParser;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.data.xml.EnchantItemGroupsParser;
import l2e.gameserver.data.xml.EnchantItemParser;
import l2e.gameserver.data.xml.MultiSellParser;
import l2e.gameserver.data.xml.TeleLocationParser;
import l2e.gameserver.data.xml.TransformParser;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.scripting.L2ScriptEngineManager;

public class AdminAdmin implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminAdmin.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_admin",
		"admin_admin1",
		"admin_admin2",
		"admin_admin3",
		"admin_admin4",
		"admin_admin5",
		"admin_admin6",
		"admin_admin7",
		"admin_gmliston",
		"admin_gmlistoff",
		"admin_silence",
		"admin_diet",
		"admin_tradeoff",
		"admin_reload",
		"admin_set",
		"admin_set_mod",
		"admin_saveolymp",
		"admin_manualhero",
		"admin_sethero",
		"admin_endolympiad",
		"admin_setconfig",
		"admin_config_server",
		"admin_gmon"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
		
		if (command.startsWith("admin_admin"))
		{
			showMainPage(activeChar, command);
		}
		else if (command.equals("admin_config_server"))
		{
			showConfigPage(activeChar);
		}
		else if (command.startsWith("admin_gmliston"))
		{
			AdminParser.getInstance().showGm(activeChar);
			activeChar.sendMessage("Registered into gm list");
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/gm_menu.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.startsWith("admin_gmlistoff"))
		{
			AdminParser.getInstance().hideGm(activeChar);
			activeChar.sendMessage("Removed from gm list");
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/gm_menu.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.startsWith("admin_silence"))
		{
			if (activeChar.isSilenceMode())
			{
				activeChar.setSilenceMode(false);
				activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
			}
			else
			{
				activeChar.setSilenceMode(true);
				activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
			}
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/gm_menu.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.startsWith("admin_saveolymp"))
		{
			Olympiad.getInstance().saveOlympiadStatus();
			activeChar.sendMessage("olympiad system saved.");
		}
		else if (command.startsWith("admin_endolympiad"))
		{
			try
			{
				Olympiad.getInstance().manualSelectHeroes();
			}
			catch (Exception e)
			{
				_log.warning("An error occured while ending olympiad: " + e);
			}
			activeChar.sendMessage("Heroes formed");
		}
		else if (command.startsWith("admin_manualhero") || command.startsWith("admin_sethero"))
		{
			L2Object target = activeChar.getTarget();
			if (target instanceof L2PcInstance)
			{
				L2PcInstance targetPlayer = (L2PcInstance) target;
				boolean isHero = targetPlayer.isHero();
				if (isHero)
				{
					targetPlayer.setHero(false);
					CustomSQLs.updateDatabase(targetPlayer, false);
					targetPlayer.sendMessage("You are not hero now!");
				}
				else
				{
					targetPlayer.setHero(true);
					targetPlayer.broadcastPacket(new SocialAction(targetPlayer.getObjectId(), 16));
					CustomSQLs.updateDatabase(targetPlayer, true);
					targetPlayer.sendMessage("You are hero now!");
				}
				targetPlayer.broadcastUserInfo();
			}
			else
			{
				_log.info("GM: " + activeChar.getName() + " is trying to set a non Player Target as Hero.");
				return false;
			}
		}
		else if (command.startsWith("admin_diet"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				if (st.nextToken().equalsIgnoreCase("on"))
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("Diet mode on");
				}
				else if (st.nextToken().equalsIgnoreCase("off"))
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("Diet mode off");
				}
			}
			catch (Exception ex)
			{
				if (activeChar.getDietMode())
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("Diet mode off");
				}
				else
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("Diet mode on");
				}
			}
			finally
			{
				activeChar.refreshOverloaded();
			}
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/gm_menu.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.startsWith("admin_tradeoff"))
		{
			try
			{
				String mode = command.substring(15);
				if (mode.equalsIgnoreCase("on"))
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("Trade refusal enabled");
				}
				else if (mode.equalsIgnoreCase("off"))
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("Trade refusal disabled");
				}
			}
			catch (Exception ex)
			{
				if (activeChar.getTradeRefusal())
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("Trade refusal disabled");
				}
				else
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("Trade refusal enabled");
				}
			}
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/gm_menu.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.startsWith("admin_reload"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("You need to specify a type to reload!");
				activeChar.sendMessage("Usage: //reload <multisell|buylist|teleport|skill|npc|htm|item|config|access|quests|door|walker|handler>");
				return false;
			}
			
			final String type = st.nextToken();
			try
			{
				if (type.equals("multisell"))
				{
					MultiSellParser.getInstance().load();
					activeChar.sendMessage("All Multisells have been reloaded");
				}
				else if (type.startsWith("buylist"))
				{
					BuyListParser.getInstance().load();
					activeChar.sendMessage("All BuyLists have been reloaded");
				}
				else if (type.startsWith("teleport"))
				{
					TeleLocationParser.getInstance().load();
					activeChar.sendMessage("Teleport Locations have been reloaded");
				}
				else if (type.startsWith("skill"))
				{
					SkillHolder.getInstance().reload();
					activeChar.sendMessage("All Skills have been reloaded");
				}
				else if (type.startsWith("npcId"))
				{
					Integer npcId = Integer.parseInt(st.nextToken());
					if (npcId != null)
					{
						NpcTable.getInstance().reloadNpc(npcId, true, true, true, true, true, true);
						activeChar.sendMessage("NPC " + npcId + " have been reloaded");
					}
				}
				else if (type.equals("npc"))
				{
					NpcTable.getInstance().reloadAllNpc();
					QuestManager.getInstance().reloadAllQuests();
					activeChar.sendMessage("All NPCs have been reloaded");
				}
				else if (type.startsWith("htm"))
				{
					HtmCache.getInstance().reload();
					activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
				}
				else if (type.startsWith("item"))
				{
					ItemHolder.getInstance().reload();
					activeChar.sendMessage("Item Templates have been reloaded");
				}
				else if (type.startsWith("config"))
				{
					Config.load();
					activeChar.sendMessage("All Config Settings have been reloaded");
				}
				else if (type.startsWith("access"))
				{
					AdminParser.getInstance().load();
					activeChar.sendMessage("Access Rights have been reloaded");
				}
				else if (type.startsWith("quests"))
				{
					QuestManager.getInstance().reloadAllQuests();
					activeChar.sendMessage("All Quests have been reloaded");
				}
				else if (type.startsWith("door"))
				{
					DoorParser.getInstance().load();
					activeChar.sendMessage("All Doors have been reloaded.");
				}
				else if (type.startsWith("walker"))
				{
					WalkingManager.getInstance().load();
					activeChar.sendMessage("All Walkers have been reloaded");
				}
				else if (type.startsWith("handler"))
				{
					File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, "handlers/MasterHandler.java");
					try
					{
						L2ScriptEngineManager.getInstance().executeScript(file);
						activeChar.sendMessage("All handlers have been reloaded");
					}
					catch (ScriptException e)
					{
						L2ScriptEngineManager.getInstance().reportScriptFileError(file, e);
						activeChar.sendMessage("There was an error while loading handlers.");
					}
				}
				else if (type.startsWith("enchant"))
				{
					EnchantItemGroupsParser.getInstance().load();
					EnchantItemParser.getInstance().load();
					activeChar.sendMessage(activeChar.getName() + ": Reloaded item enchanting data.");
				}
				else if (type.startsWith("transform"))
				{
					TransformParser.getInstance().load();
					activeChar.sendMessage(activeChar.getName() + ": Reloaded transform data.");
				}
				activeChar.sendMessage("WARNING: There are several known issues regarding this feature. Reloading server data during runtime is STRONGLY NOT RECOMMENDED for live servers, just for developing environments.");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("An error occured while reloading " + type + " !");
				activeChar.sendMessage("Usage: //reload <multisell|buylist|teleport|skill|npc|htm|item|config|access|quests|door|walker|handler>");
				_log.log(Level.WARNING, "An error occured while reloading " + type + ": " + e, e);
			}
		}
		else if (command.startsWith("admin_setconfig"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String pName = st.nextToken();
				String pValue = st.nextToken();
				if (Config.setParameterValue(pName, pValue))
				{
					activeChar.sendMessage("Config parameter " + pName + " set to " + pValue);
				}
				else
				{
					activeChar.sendMessage("Invalid parameter!");
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setconfig <parameter> <value>");
			}
			finally
			{
				showConfigPage(activeChar);
			}
		}
		else if (command.startsWith("admin_set"))
		{
			StringTokenizer st = new StringTokenizer(command);
			String[] cmd = st.nextToken().split("_");
			try
			{
				String[] parameter = st.nextToken().split("=");
				String pName = parameter[0].trim();
				String pValue = parameter[1].trim();
				if (Config.setParameterValue(pName, pValue))
				{
					activeChar.sendMessage("parameter " + pName + " succesfully set to " + pValue);
				}
				else
				{
					activeChar.sendMessage("Invalid parameter!");
				}
			}
			catch (Exception e)
			{
				if (cmd.length == 2)
				{
					activeChar.sendMessage("Usage: //set parameter=value");
				}
			}
			finally
			{
				if (cmd.length == 3)
				{
					if (cmd[2].equalsIgnoreCase("mod"))
					{
						adminhtm.setFile(activeChar.getLang(), "data/html/admin/mods_menu.htm");
						activeChar.sendPacket(adminhtm);
					}
				}
			}
		}
		else if (command.startsWith("admin_gmon"))
		{
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(L2PcInstance activeChar, String command)
	{
		NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
		
		int mode = 0;
		try
		{
			mode = Integer.parseInt(command.substring(11));
		}
		catch (Exception e)
		{
		}
		switch (mode)
		{
			case 1:
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/main_menu.htm");
				activeChar.sendPacket(adminhtm);
				break;
			case 2:
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/game_menu.htm");
				activeChar.sendPacket(adminhtm);
				break;
			case 3:
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/effects_menu.htm");
				activeChar.sendPacket(adminhtm);
				break;
			case 4:
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/server_menu.htm");
				activeChar.sendPacket(adminhtm);
				break;
			case 5:
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/mods_menu.htm");
				activeChar.sendPacket(adminhtm);
				break;
			case 6:
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/char_menu.htm");
				activeChar.sendPacket(adminhtm);
				break;
			case 7:
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/gm_menu.htm");
				activeChar.sendPacket(adminhtm);
				break;
			default:
				adminhtm.setFile(activeChar.getLang(), "data/html/admin/main_menu.htm");
				activeChar.sendPacket(adminhtm);
				break;
		}
	}
	
	public void showConfigPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		TextBuilder replyMSG = new TextBuilder("<html><title>L2J :: Config</title><body>");
		replyMSG.append("<center><table width=270><tr><td width=60><button value=\"Main\" action=\"bypass -h admin_admin\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=150>Config Server Panel</td><td width=60><button value=\"Back\" action=\"bypass -h admin_admin4\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=260><tr><td width=140></td><td width=40></td><td width=40></td></tr>");
		replyMSG.append("<tr><td><font color=\"00AA00\">Drop:</font></td><td></td><td></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate EXP</font> = " + Config.RATE_XP + "</td><td><edit var=\"param1\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig RateXp $param1\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate SP</font> = " + Config.RATE_SP + "</td><td><edit var=\"param2\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig RateSp $param2\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Drop Spoil</font> = " + Config.RATE_DROP_SPOIL + "</td><td><edit var=\"param4\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig RateDropSpoil $param4\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td width=140></td><td width=40></td><td width=40></td></tr>");
		replyMSG.append("<tr><td><font color=\"00AA00\">Enchant:</font></td><td></td><td></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Enchant Element Stone</font> = " + Config.ENCHANT_CHANCE_ELEMENT_STONE + "</td><td><edit var=\"param8\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceElementStone $param8\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Enchant Element Crystal</font> = " + Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL + "</td><td><edit var=\"param9\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceElementCrystal $param9\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Enchant Element Jewel</font> = " + Config.ENCHANT_CHANCE_ELEMENT_JEWEL + "</td><td><edit var=\"param10\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceElementJewel $param10\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Enchant Element Energy</font> = " + Config.ENCHANT_CHANCE_ELEMENT_ENERGY + "</td><td><edit var=\"param11\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceElementEnergy $param11\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		
		replyMSG.append("</table></body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
}