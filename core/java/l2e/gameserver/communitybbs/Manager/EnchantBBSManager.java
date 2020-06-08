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
package l2e.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import l2e.Config;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.xml.ItemIconsParser;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2EtcItem;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Created by LordWinter 25.02.2011 Fixed by L2J Eternity-World
 */
public class EnchantBBSManager extends BaseBBSManager
{
	private static Logger _log = Logger.getLogger(EnchantBBSManager.class.getName());
	
	protected EnchantBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		String lang = activeChar.getLang();
		
		if (command.equals("_bbsechant"))
		{
			String name = "None Name";
			name = ItemHolder.getInstance().getTemplate(Config.ENCHANT_ITEM).getName();
			TextBuilder sb = new TextBuilder();
			sb.append("<table width=400>");
			L2ItemInstance arr[] = activeChar.getInventory().getItems();
			int len = arr.length;
			for (int i = 0; i < len; i++)
			{
				
				L2ItemInstance _item = arr[i];
				
				if ((_item == null) || (_item.getItem() instanceof L2EtcItem) || !_item.isEquipped() || _item.isHeroItem() || (_item.getItem().getCrystalType() == L2Item.CRYSTAL_NONE) || ((_item.getId() >= 7816) && (_item.getId() <= 7831)) || _item.isShadowItem() || _item.isCommonItem() || _item.isWear() || (_item.getEnchantLevel() >= 26))
				{
					continue;
				}
				sb.append((new StringBuilder()).append("<tr><td><img src=icon.").append(ItemIconsParser.getIcon(_item.getItem().getId())).append(" width=32 height=32></td><td>").toString());
				sb.append((new StringBuilder()).append("<font color=\"LEVEL\">").append(_item.getItem().getName()).append(" ").append(_item.getEnchantLevel() <= 0 ? "" : (new StringBuilder()).append("</font><font color=3293F3>" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT_ON") + " +").append(_item.getEnchantLevel()).toString()).append("</font><br1>").toString());
				
				sb.append((new StringBuilder()).append("" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT_FOR") + " <font color=\"LEVEL\">").append(name).append("</font>").toString());
				sb.append("<img src=\"l2ui.squaregray\" width=\"170\" height=\"1\">");
				sb.append("</td><td>");
				sb.append((new StringBuilder()).append("<button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT") + " action=\"bypass -h _bbsechant;enchlistpage;").append(_item.getObjectId()).append("\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString());
				sb.append("</td></tr>");
				sb.append("<td>");
				sb.append((new StringBuilder()).append("<button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE") + " action=\"bypass -h _bbsechant;enchlistpageAtrChus;").append(_item.getObjectId()).append("\" width=75 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString());
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/36.htm");
			adminReply.replace("%enchanter%", sb.toString());
			separateAndSend(adminReply.getHtm(), activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.ENCHANTMAIN") + "");
		}
		if (command.startsWith("_bbsechant;enchlistpage;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int ItemForEchantObjID = Integer.parseInt(st.nextToken());
			int price = 0;
			double mod = 0;
			String name = "None Name";
			name = ItemHolder.getInstance().getTemplate(Config.ENCHANT_ITEM).getName();
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_D)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = Config.COMMUNITY_ENCH_D_WEAPON_PRICE;
					mod = Config.COMMUNITY_ENCH_D_PRICE_MOD;
				}
				else
				{
					price = Config.COMMUNITY_ENCH_D_ARMOR_PRICE;
					mod = Config.COMMUNITY_ENCH_D_PRICE_MOD;
				}
			}
			else if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_C)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = Config.COMMUNITY_ENCH_C_WEAPON_PRICE;
					mod = Config.COMMUNITY_ENCH_C_PRICE_MOD;
				}
				else
				{
					price = Config.COMMUNITY_ENCH_C_ARMOR_PRICE;
					mod = Config.COMMUNITY_ENCH_C_PRICE_MOD;
				}
			}
			else if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_B)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = Config.COMMUNITY_ENCH_B_WEAPON_PRICE;
					mod = Config.COMMUNITY_ENCH_B_PRICE_MOD;
				}
				else
				{
					price = Config.COMMUNITY_ENCH_B_ARMOR_PRICE;
					mod = Config.COMMUNITY_ENCH_B_PRICE_MOD;
				}
			}
			else if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_A)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = Config.COMMUNITY_ENCH_A_WEAPON_PRICE;
					mod = Config.COMMUNITY_ENCH_A_PRICE_MOD;
				}
				else
				{
					price = Config.COMMUNITY_ENCH_A_ARMOR_PRICE;
					mod = Config.COMMUNITY_ENCH_A_PRICE_MOD;
				}
			}
			else if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_S)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = Config.COMMUNITY_ENCH_S_WEAPON_PRICE;
					mod = Config.COMMUNITY_ENCH_S_PRICE_MOD;
				}
				else
				{
					price = Config.COMMUNITY_ENCH_S_ARMOR_PRICE;
					mod = Config.COMMUNITY_ENCH_S_PRICE_MOD;
				}
			}
			else if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_S80)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = Config.COMMUNITY_ENCH_S80_WEAPON_PRICE;
					mod = Config.COMMUNITY_ENCH_S80_PRICE_MOD;
				}
				else
				{
					price = Config.COMMUNITY_ENCH_S80_ARMOR_PRICE;
					mod = Config.COMMUNITY_ENCH_S80_PRICE_MOD;
				}
			}
			else if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_S84)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = Config.COMMUNITY_ENCH_S84_WEAPON_PRICE;
					mod = Config.COMMUNITY_ENCH_S84_PRICE_MOD;
				}
				else
				{
					price = Config.COMMUNITY_ENCH_S84_ARMOR_PRICE;
					mod = Config.COMMUNITY_ENCH_S84_PRICE_MOD;
				}
			}
			TextBuilder sb = new TextBuilder();
			sb.append("" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ITEM_SELECT") + "<br1><table width=300>");
			sb.append((new StringBuilder()).append("<tr><td width=32><img src=icon.").append(ItemIconsParser.getIcon(EhchantItem.getItem().getId())).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>").toString());
			sb.append((new StringBuilder()).append("<font color=\"LEVEL\">").append(EhchantItem.getItem().getName()).append(" ").append(EhchantItem.getEnchantLevel() <= 0 ? "" : (new StringBuilder()).append("</font><font color=3293F3>" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT_ON") + " +").append(EhchantItem.getEnchantLevel()).toString()).append("</font><br1>").toString());
			sb.append((new StringBuilder()).append("" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT_MADE") + " <font color=\"LEVEL\">").append(name).append("</font>").toString());
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append((new StringBuilder()).append("<td width=32><img src=icon.").append(ItemIconsParser.getIcon(EhchantItem.getItem().getId())).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>").toString());
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br1>");
			sb.append("<br1>");
			sb.append("<table border=0 width=400>");
			for (int $i = Config.COMMUNITY_ENCHANT_MIN - 2; $i <= Config.COMMUNITY_ENCHANT_MAX; $i++)
			{
				if (($i % 2) == 0)
				{
					sb.append("<td width=200>");
					sb.append("<center><button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +" + $i + " (" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + "" + (int) (price * (mod + ($i * price))) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;" + $i + ";" + (int) (price * (mod + ($i * price))) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></center>");
					sb.append("</td><tr>");
				}
				else
				{
					sb.append("</tr><td width=200>");
					sb.append("<center><button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +" + $i + " (" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + "" + (int) (price * (mod + ($i * price))) + " " + name + ")\" action=\"bypass -h _bbsechant;enchantgo;" + $i + ";" + (int) (price * (mod + ($i * price))) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></center>");
					sb.append("</td>");
				}
			}
			sb.append("</table><br1><button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.BACK") + " action=\"bypass -h _bbsechant\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/36.htm");
			adminReply.replace("%enchanter%", sb.toString());
			separateAndSend(adminReply.getHtm(), activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.ENCHANTITEM") + "");
		}
		if (command.startsWith("_bbsechant;enchlistpageAtrChus;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int ItemForEchantObjID = Integer.parseInt(st.nextToken());
			String name = "None Name";
			name = ItemHolder.getInstance().getTemplate(Config.ENCHANT_ITEM).getName();
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			
			TextBuilder sb = new TextBuilder();
			sb.append("" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_ITEM_SELECT") + "<br1><table width=300>");
			sb.append((new StringBuilder()).append("<tr><td width=32><img src=icon.").append(ItemIconsParser.getIcon(EhchantItem.getItem().getId())).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>").toString());
			sb.append((new StringBuilder()).append("<font color=\"LEVEL\">").append(EhchantItem.getItem().getName()).append(" ").append(EhchantItem.getEnchantLevel() <= 0 ? "" : (new StringBuilder()).append("</font><br1><font color=3293F3>" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT_ON") + " +").append(EhchantItem.getEnchantLevel()).toString()).append("</font><br1>").toString());
			
			sb.append((new StringBuilder()).append("" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT_MADE") + " <font color=\"LEVEL\">").append(name).append("</font>").toString());
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append((new StringBuilder()).append("<td width=32><img src=icon.").append(ItemIconsParser.getIcon(EhchantItem.getItem().getId())).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>").toString());
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br1>");
			sb.append("<br1>");
			sb.append("<table border=0 width=400><tr><td width=200>");
			sb.append("<center><img src=icon.etc_wind_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_WIND") + " action=\"bypass -h _bbsechant;enchlistpageAtr;2;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_earth_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_EARTH") + " action=\"bypass -h _bbsechant;enchlistpageAtr;3;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_fire_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_FIRE") + " action=\"bypass -h _bbsechant;enchlistpageAtr;0;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td><td width=200>");
			sb.append("<center><img src=icon.etc_water_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_WATER") + " action=\"bypass -h _bbsechant;enchlistpageAtr;1;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_holy_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_DIVINE") + " action=\"bypass -h _bbsechant;enchlistpageAtr;4;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("<br1><center><img src=icon.etc_unholy_stone_i00 width=32 height=32></center><br1>");
			sb.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_DARK") + " action=\"bypass -h _bbsechant;enchlistpageAtr;5;" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
			sb.append("</td></tr></table><br1><button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.BACK") + " action=\"bypass -h _bbsechant\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/36.htm");
			adminReply.replace("%enchanter%", sb.toString());
			separateAndSend(adminReply.getHtm(), activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.ATRIBUTTEMAIN") + "");
		}
		if (command.startsWith("_bbsechant;enchlistpageAtr;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int AtributType = Integer.parseInt(st.nextToken());
			int ItemForEchantObjID = Integer.parseInt(st.nextToken());
			int price = 0;
			String ElementName = "";
			
			if (AtributType == 0)
			{
				ElementName = "" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_FIRE") + "";
			}
			else if (AtributType == 1)
			{
				ElementName = "" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_WATER") + "";
			}
			else if (AtributType == 2)
			{
				ElementName = "" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_WIND") + "";
			}
			else if (AtributType == 3)
			{
				ElementName = "" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_EARTH") + "";
			}
			else if (AtributType == 4)
			{
				ElementName = "" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_DIVINE") + "";
			}
			else if (AtributType == 5)
			{
				ElementName = "" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_DARK") + "";
			}
			
			String name = "None Name";
			name = ItemHolder.getInstance().getTemplate(Config.ENCHANT_ITEM).getName();
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(ItemForEchantObjID);
			if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_S)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 2;
				}
				else
				{
					price = 1;
				}
			}
			else if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_S80)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 4;
				}
				else
				{
					price = 2;
				}
			}
			else if (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_S84)
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					price = 6;
				}
				else
				{
					price = 4;
				}
			}
			TextBuilder sb = new TextBuilder();
			sb.append("" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ELEMENT_SELECT") + " <font color=\"LEVEL\">" + ElementName + "</font><br1> " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ITEM_SELECT") + "<br1><table width=300>");
			sb.append((new StringBuilder()).append("<tr><td width=32><img src=icon.").append(ItemIconsParser.getIcon(EhchantItem.getItem().getId())).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td><td width=236><center>").toString());
			sb.append((new StringBuilder()).append("<font color=\"LEVEL\">").append(EhchantItem.getItem().getName()).append(" ").append(EhchantItem.getEnchantLevel() <= 0 ? "" : (new StringBuilder()).append("</font><br1><font color=3293F3>" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT_ON") + " +").append(EhchantItem.getEnchantLevel()).toString()).append("</font><br1>").toString());
			
			sb.append((new StringBuilder()).append("" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT_MADE") + " <font color=\"LEVEL\">").append(name).append("</font>").toString());
			sb.append("<img src=\"l2ui.squaregray\" width=\"236\" height=\"1\"><center></td>");
			sb.append((new StringBuilder()).append("<td width=32><img src=icon.").append(ItemIconsParser.getIcon(EhchantItem.getItem().getId())).append(" width=32 height=32> <img src=\"l2ui.squaregray\" width=\"32\" height=\"1\"></td>").toString());
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("<br1>");
			sb.append("<br1>");
			if ((EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_S) || (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_S80) || (EhchantItem.getItem().getCrystalType() == L2Item.CRYSTAL_S84))
			{
				if (EhchantItem.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					sb.append("<table border=0 width=400><tr><td width=200>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +25 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 1)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;25;" + AtributType + ";" + (price * (price + 1)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +50 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 2)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;50;" + AtributType + ";" + (price * (price + 2)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +75 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 3)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;75;" + AtributType + ";" + (price * (price + 3)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +100 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 4)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;100;" + AtributType + ";" + (price * (price + 4)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("</td><td width=200>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +125 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 5)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;125;" + AtributType + ";" + (price * (price + 5)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +150 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 6)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;150;" + AtributType + ";" + (price * (price + 6)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +300 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 7)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;300;" + AtributType + ";" + (price * (price + 7)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +450 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 8)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;450;" + AtributType + ";" + (price * (price + 8)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("</td></tr></table><br1>");
				}
				else
				{
					sb.append("<table border=0 width=400><tr><td width=200>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +25 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 1)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;25;" + AtributType + ";" + (price * (price + 1)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +50 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 2)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;50;" + AtributType + ";" + (price * (price + 2)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +75 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 3)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;75;" + AtributType + ";" + (price * (price + 3)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("</td><td width=200>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +100 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 4)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;100;" + AtributType + ";" + (price * (price + 4)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +125 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 5)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;125;" + AtributType + ";" + (price * (price + 5)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("<br1>");
					sb.append("<button value=\"" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.FOR") + " +150 [ " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.PRICE") + " " + (price * (price + 6)) + " " + name + " ]\" action=\"bypass -h _bbsechant;enchantgoAtr;150;" + AtributType + ";" + (price * (price + 6)) + ";" + ItemForEchantObjID + "\" width=200 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					sb.append("</td></tr></table><br1>");
				}
			}
			else
			{
				sb.append("<table border=0 width=400><tr><td width=200>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ENCHANT_POSSIBLE") + "</font></center>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("<br1>");
				sb.append("</td></tr></table><br1>");
			}
			sb.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.BACK") + " action=\"bypass -h _bbsechant\" width=70 height=18 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/36.htm");
			adminReply.replace("%enchanter%", sb.toString());
			separateAndSend(adminReply.getHtm(), activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.ATRIBUTTEITEM") + "");
		}
		if (command.startsWith("_bbsechant;enchantgo;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int EchantVal = Integer.parseInt(st.nextToken());
			int EchantPrice = Integer.parseInt(st.nextToken());
			int EchantObjID = Integer.parseInt(st.nextToken());
			L2Item item = ItemHolder.getInstance().getTemplate(Config.ENCHANT_ITEM);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getId());
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(EchantObjID);
			
			if ((activeChar.isProcessingTransaction()) || (activeChar.getPrivateStoreType() != 0) || (activeChar.getActiveTradeList() != null))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
				return;
			}
			else if ((pay != null) && (pay.getCount() >= EchantPrice))
			{
				activeChar.destroyItem("Enchanting", pay, EchantPrice, activeChar, true);
				EhchantItem.setEnchantLevel(EchantVal);
				activeChar.getInventory().equipItem(EhchantItem);
				activeChar.broadcastUserInfo();
				activeChar.sendMessage("" + EhchantItem.getItem().getName() + " " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.WAS_ENCHANTED") + " " + EchantVal + ".");
				parsecmd("_bbsechant", activeChar);
				
				_log.info("WMZSELLER: Item: " + EhchantItem + " Val: " + EchantVal + " Price: " + EchantPrice + " Player: " + activeChar.getName() + "");
			}
			else
			{
				activeChar.sendMessage((new CustomMessage("EnchantBBS.ERROR_MSG", activeChar.getLang())).toString());
				return;
			}
			
		}
		if (command.startsWith("_bbsechant;enchantgoAtr;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int EchantVal = Integer.parseInt(st.nextToken());
			int AtrType = Integer.parseInt(st.nextToken());
			int EchantPrice = Integer.parseInt(st.nextToken());
			int EchantObjID = Integer.parseInt(st.nextToken());
			L2Item item = ItemHolder.getInstance().getTemplate(Config.ENCHANT_ITEM);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getId());
			L2ItemInstance EhchantItem = activeChar.getInventory().getItemByObjectId(EchantObjID);
			
			if ((activeChar.isProcessingTransaction()) || (activeChar.getPrivateStoreType() != 0) || (activeChar.getActiveTradeList() != null))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
				return;
			}
			else if ((pay != null) && (pay.getCount() >= EchantPrice))
			{
				activeChar.destroyItem("Enchanting", pay, EchantPrice, activeChar, true);
				EhchantItem.setElementAttr((byte) AtrType, EchantVal);
				activeChar.broadcastUserInfo();
				activeChar.sendMessage("" + EhchantItem.getItem().getName() + ": " + LocalizationStorage.getInstance().getString(lang, "EnchantBBS.ATRIBUTTE_POWER") + " " + EchantVal + ".");
				parsecmd("_bbsechant", activeChar);
				
				_log.info("WMZSELLER: Item: " + EhchantItem + " Val: " + EchantVal + " Price: " + EchantPrice + " Player: " + activeChar.getName() + "");
			}
			else
			{
				activeChar.sendMessage((new CustomMessage("EnchantBBS.ERROR_MSG", activeChar.getLang())).toString());
				return;
			}
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	public static EnchantBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantBBSManager _instance = new EnchantBBSManager();
	}
}