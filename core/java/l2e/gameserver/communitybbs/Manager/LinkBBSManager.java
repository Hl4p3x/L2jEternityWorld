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

import java.util.logging.Logger;

import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.ShowBoard;

/**
 * Created by LordWinter 05.07.2013 Fixed by L2J Eternity-World
 */
public class LinkBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(LinkBBSManager.class.getName());
	
	protected LinkBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equalsIgnoreCase("_bbslink"))
		{
			sendHtm(activeChar, "data/html/CommunityBoard/44.htm");
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.LINK") + "");
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private boolean sendHtm(L2PcInstance player, String path)
	{
		String oriPath = path;
		if ((player.getLang() != null) && !player.getLang().equalsIgnoreCase("en"))
		{
			if (path.contains("html/"))
			{
				path = path.replace("html/", "html-" + player.getLang() + "/");
			}
		}
		String content = HtmCache.getInstance().getHtm(path);
		if ((content == null) && !oriPath.equals(path))
		{
			content = HtmCache.getInstance().getHtm(oriPath);
		}
		if (content == null)
		{
			return false;
		}
		
		separateAndSend(content, player);
		return true;
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	public static LinkBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final LinkBBSManager _instance = new LinkBBSManager();
	}
}