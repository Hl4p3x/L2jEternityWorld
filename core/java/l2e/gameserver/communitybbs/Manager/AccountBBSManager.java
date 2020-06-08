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

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.handler.VoicedCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Created by LordWinter 11.08.2013 Fixed by L2J Eternity-World
 */
public class AccountBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(AccountBBSManager.class.getName());
	
	protected AccountBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance player)
	{
		if (command.equals("_bbsaccount;"))
		{
			showMain(player);
			player.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FavoriteBBS.CABINET") + "");
		}
		else if (command.startsWith("_bbsaccount;menu"))
		{
			IVoicedCommandHandler menu_voice = VoicedCommandHandler.getInstance().getHandler("menu");
			if (menu_voice != null)
			{
				menu_voice.useVoicedCommand("menu", player, "");
				return;
			}
		}
		else if (command.startsWith("_bbsaccount;lock"))
		{
			IVoicedCommandHandler lock_voice = VoicedCommandHandler.getInstance().getHandler("lock");
			if (lock_voice != null)
			{
				lock_voice.useVoicedCommand("lock", player, "");
				return;
			}
		}
		else if (command.startsWith("_bbsaccount;repair"))
		{
			IVoicedCommandHandler repair_voice = VoicedCommandHandler.getInstance().getHandler("repair");
			if (repair_voice != null)
			{
				repair_voice.useVoicedCommand("repair", player, "");
				return;
			}
		}
		else if (command.startsWith("_bbsaccount;changepassword"))
		{
			IVoicedCommandHandler changepassword_voice = VoicedCommandHandler.getInstance().getHandler("changepassword");
			if (changepassword_voice != null)
			{
				changepassword_voice.useVoicedCommand("changepassword", player, "");
				return;
			}
		}
		else if (command.startsWith("_bbsaccount;hellbound"))
		{
			IVoicedCommandHandler hellbound_voice = VoicedCommandHandler.getInstance().getHandler("hellbound");
			if (hellbound_voice != null)
			{
				hellbound_voice.useVoicedCommand("hellbound", player, "");
				return;
			}
		}
		else if (command.startsWith("_bbsaccount;teletocl"))
		{
			IVoicedCommandHandler teletocl_voice = VoicedCommandHandler.getInstance().getHandler("teletocl");
			if (teletocl_voice != null)
			{
				teletocl_voice.useVoicedCommand("teletocl", player, "");
				return;
			}
		}
	}
	
	private String getStatus(L2PcInstance player)
	{
		return player.getPremiumService() == 1 ? "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(player.getLang(), "AccountBBSManager.PREMIUM") + "</font>" : "" + LocalizationStorage.getInstance().getString(player.getLang(), "AccountBBSManager.SIMPLE") + "";
	}
	
	private void showMain(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat format_time = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat format_days = new SimpleDateFormat("dd");
		
		NpcHtmlMessage htm = new NpcHtmlMessage(5);
		htm.setFile(player.getLang(), "data/html/CommunityBoard/46.htm");
		htm.replace("%content%", sb.toString());
		htm.replace("%Name_Server%", Config.SERVER_NAME);
		htm.replace("%STATUS_ACCOUNT%", getStatus(player));
		htm.replace("%NAME_ACCOUNT%", "<font color=\"LEVEL\">" + player.getName() + "</font>");
		htm.replace("%ACCOUNT_IP%", player.getClient().getConnection().getInetAddress().getHostAddress());
		htm.replace("%ACCOUNT_CLAN%", player.getClan() == null ? "" + LocalizationStorage.getInstance().getString(player.getLang(), "AccountBBSManager.NO_CLAN") + "" : player.getClan().getName() + ", " + LocalizationStorage.getInstance().getString(player.getLang(), "AccountBBSManager.LEVEL") + " " + player.getClan().getLevel());
		htm.replace("%ACCOUNT_MAIN%", player.getAccountName());
		htm.replace("%CHARACTER_ONLINE%", format_time.format(player.getOnlineTime()) + ", " + LocalizationStorage.getInstance().getString(player.getLang(), "AccountBBSManager.DAYS") + " " + format_days.format(player.getOnlineTime()));
		htm.replace("%RATE_XP%", String.valueOf(player.getPremiumService() == 1 ? " " + Config.PREMIUM_RATE_XP + " " : " " + Config.RATE_XP + " "));
		htm.replace("%RATE_SP%", String.valueOf(player.getPremiumService() == 1 ? " " + Config.PREMIUM_RATE_SP + " " : " " + Config.RATE_SP + " "));
		htm.replace("%RATE_ITEMS%", String.valueOf(player.getPremiumService() == 1 ? " " + Config.PREMIUM_RATE_DROP_ITEMS + " " : " " + Config.RATE_DROP_ITEMS + " "));
		htm.replace("%RATE_SPOIL%", String.valueOf(player.getPremiumService() == 1 ? " " + Config.PREMIUM_RATE_DROP_SPOIL + " " : " " + Config.RATE_DROP_SPOIL + " "));
		separateAndSend(htm.getHtm(), player);
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	public static AccountBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AccountBBSManager _instance = new AccountBBSManager();
	}
}