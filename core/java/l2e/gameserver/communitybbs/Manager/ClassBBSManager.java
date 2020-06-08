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
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.xml.ClassListParser;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 26.02.2011 Fixed by L2J Eternity-World
 */
public class ClassBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(ClassBBSManager.class.getName());
	
	protected ClassBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		String lang = activeChar.getLang();
		ClassId classId = activeChar.getClassId();
		int jobLevel = classId.level();
		int level = activeChar.getLevel();
		TextBuilder html = new TextBuilder("");
		html.append("<br>");
		html.append("<center>");
		if (Config.ALLOW_CLASS_MASTERS_LISTCB.isEmpty() || !Config.ALLOW_CLASS_MASTERS_LISTCB.contains(jobLevel))
		{
			jobLevel = 3;
		}
		if ((((level >= 20) && (jobLevel == 0)) || ((level >= 40) && (jobLevel == 1)) || ((level >= 76) && (jobLevel == 2))) && Config.ALLOW_CLASS_MASTERS_LISTCB.contains(jobLevel))
		{
			L2Item item = ItemHolder.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEMCB);
			html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.MUST_PAY") + " <font color=\"LEVEL\">");
			html.append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LISTCB[jobLevel])).append("</font> <font color=\"LEVEL\">").append(item.getName()).append("</font> " + LocalizationStorage.getInstance().getString(lang, "ClassBBS.FOR_CHANGE") + "<br>");
			for (ClassId cid : ClassId.values())
			{
				if (cid == ClassId.inspector)
				{
					continue;
				}
				if (cid.childOf(classId) && (cid.level() == (classId.level() + 1)))
				{
					html.append("<br><center><button value=\"").append(cid.name()).append("\" action=\"bypass -h _bbsclass;change_class;").append(cid.getId()).append(";").append(Config.CLASS_MASTERS_PRICE_LISTCB[jobLevel]).append("\" width=250 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				}
			}
			html.append("</center>");
		}
		else
		{
			switch (jobLevel)
			{
				case 0:
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.WELCOME") + " " + activeChar.getName() + "! " + LocalizationStorage.getInstance().getString(lang, "ClassBBS.CURRENT_PROF") + " <font color=F2C202>" + ClassListParser.getInstance().getClass(activeChar.getClassId()).getClientCode() + "</font>.<br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.FOR_CHANGE_PROF") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.20_LVL") + "</font><br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.SUBCLASS_CHANGE") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.76_LVL") + ".</font><br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.NOOBLESS_CHANGE") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.76_LVL") + ".</font><br>");
					break;
				case 1:
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.WELCOME") + " " + activeChar.getName() + "! " + LocalizationStorage.getInstance().getString(lang, "ClassBBS.CURRENT_PROF") + " <font color=F2C202>" + ClassListParser.getInstance().getClass(activeChar.getClassId()).getClientCode() + "</font>.<br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.FOR_CHANGE_PROF") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.40_LVL") + "</font><br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.SUBCLASS_CHANGE") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.76_LVL") + ".</font><br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.NOOBLESS_CHANGE") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.76_LVL") + ".</font><br>");
					break;
				case 2:
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.WELCOME") + " " + activeChar.getName() + "! " + LocalizationStorage.getInstance().getString(lang, "ClassBBS.CURRENT_PROF") + " <font color=F2C202>" + ClassListParser.getInstance().getClass(activeChar.getClassId()).getClientCode() + "</font>.<br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.FOR_CHANGE_PROF") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.76_LVL") + ".</font><br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.SUBCLASS_CHANGE") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.76_LVL") + ".</font><br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.NOOBLESS_CHANGE") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.76_LVL") + ".</font><br>");
					break;
				case 3:
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.WELCOME") + " " + activeChar.getName() + "! " + LocalizationStorage.getInstance().getString(lang, "ClassBBS.CURRENT_PROF") + " <font color=F2C202>" + ClassListParser.getInstance().getClass(activeChar.getClassId()).getClientCode() + "</font>.<br>");
					html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.NO_CHANGE_PROF") + "<br>");
					if (level >= 76)
					{
						html.append("" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.YOU_REACH") + " <font color=F2C202>" + LocalizationStorage.getInstance().getString(lang, "ClassBBS.76_LVL") + "</font>! " + LocalizationStorage.getInstance().getString(lang, "ClassBBS.ACTIVE_SUBCLASS") + "<br>");
					}
					break;
			}
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/5.htm");
		adminReply.replace("%classmaster%", html.toString());
		separateAndSend(adminReply.getHtm(), activeChar);
		activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.CLASSMASTER") + "");
		
		if (command.startsWith("_bbsclass;change_class;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			short val = Short.parseShort(st.nextToken());
			int price = Integer.parseInt(st.nextToken());
			L2Item item = ItemHolder.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEMCB);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getId());
			if ((pay != null) && (pay.getCount() >= price))
			{
				activeChar.destroyItem("ClassMaster", pay, price, activeChar, true);
				changeClass(activeChar, val);
				parsecmd("_bbsclass;", activeChar);
			}
			else if (Config.CLASS_MASTERS_PRICE_ITEMCB == 57)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			}
			else
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			}
		}
	}
	
	private void changeClass(L2PcInstance activeChar, short val)
	{
		if (activeChar.getClassId().level() == ClassId.values()[val].level())
		{
			return;
		}
		
		if (activeChar.getClassId().level() == 3)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIRD_CLASS_TRANSFER));
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLASS_TRANSFER));
		}
		activeChar.setClassId(val);
		
		if (activeChar.isSubClassActive())
		{
			activeChar.getSubClasses().get(Integer.valueOf(activeChar.getClassIndex())).setClassId(activeChar.getActiveClass());
		}
		else
		{
			if (activeChar.getClassId().level() == 1)
			{
				activeChar.getInventory().addItem("ClassBBSManager", Config.COMMUNITY_1_PROF_REWARD, Config.COMMUNITY_1_PROF_REWARD_COUNT, activeChar, null);
			}
			else if (activeChar.getClassId().level() == 2)
			{
				activeChar.getInventory().addItem("ClassBBSManager", Config.COMMUNITY_2_PROF_REWARD, Config.COMMUNITY_2_PROF_REWARD_COUNT, activeChar, null);
			}
			else if (activeChar.getClassId().level() == 3)
			{
				activeChar.getInventory().addItem("ClassBBSManager", Config.COMMUNITY_3_PROF_REWARD, Config.COMMUNITY_3_PROF_REWARD_COUNT, activeChar, null);
			}
			activeChar.setBaseClass(activeChar.getActiveClass());
		}
		activeChar.broadcastUserInfo();
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	public static ClassBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClassBBSManager _instance = new ClassBBSManager();
	}
}