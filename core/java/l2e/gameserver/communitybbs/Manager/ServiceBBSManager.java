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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.CustomSQLs;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.xml.ExperienceParser;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.stat.PcStat;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.UserInfo;

public class ServiceBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(ServiceBBSManager.class.getName());
	
	protected ServiceBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	int PremiumService;
	private final int ConsumableItemId = Config.PREMIUM_ID;
	private final int Count = Config.PREMIUM_COUNT;
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsservice"))
		{
			sendHtm(activeChar, "data/html/CommunityBoard/42.htm");
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.SERVICES") + "");
		}
		else if (command.startsWith("_bbsservice;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String param;
			
			param = st.nextToken();
			
			if (param.equalsIgnoreCase("Noobles"))
			{
				if (activeChar.getInventory().getItemByItemId(Config.NoblItemId) == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getInventory().getItemByItemId(Config.NoblItemId).getCount() < Config.NoblItemCount)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getClassId().level() < 3)
				{
					activeChar.sendMessage((new CustomMessage("ServiceBBS.NOOBLES_MSG", activeChar.getLang())).toString());
					return;
				}
				if (activeChar.isNoble())
				{
					activeChar.sendMessage((new CustomMessage("ServiceBBS.NOOBLES_MSG_1", activeChar.getLang())).toString());
					return;
				}
				activeChar.destroyItemByItemId("ShopBBS", Config.NoblItemId, Config.NoblItemCount, activeChar, true);
				activeChar.setNoble(true);
				activeChar.broadcastUserInfo();
			}
			else if (param.equalsIgnoreCase("Gender"))
			{
				if (activeChar.getRace().ordinal() == 5)
				{
					activeChar.sendMessage((new CustomMessage("ServiceBBS.GENDER_MSG", activeChar.getLang())).toString());
					return;
				}
				if (activeChar.getInventory().getItemByItemId(Config.GenderItemId) == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getInventory().getItemByItemId(Config.GenderItemId).getCount() < Config.GenderItemCount)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				activeChar.destroyItemByItemId("ShopBBS", Config.GenderItemId, Config.GenderItemCount, activeChar, true);
				activeChar.getAppearance().setSex(activeChar.getAppearance().getSex() ? false : true);
				activeChar.broadcastUserInfo();
				activeChar.decayMe();
				activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			else if (param.equalsIgnoreCase("Delevel"))
			{
				if (activeChar.getInventory().getItemByItemId(Config.DelevelItemId) == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getInventory().getItemByItemId(Config.DelevelItemId).getCount() < Config.DelevelItemCount)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				activeChar.destroyItemByItemId("ShopBBS", Config.DelevelItemId, Config.DelevelItemCount, activeChar, true);
				activeChar.getStat().removeExpAndSp((activeChar.getExp() - ExperienceParser.getInstance().getExpForLevel(activeChar.getStat().getLevel() - 1)), 0);
			}
			else if (param.equalsIgnoreCase("SetHero"))
			{
				if (activeChar.getInventory().getItemByItemId(Config.HeroItemId) == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getInventory().getItemByItemId(Config.HeroItemId).getCount() < Config.HeroItemCount)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				
				if (activeChar.isHero())
				{
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				}
				else
				{
					activeChar.destroyItemByItemId("ShopBBS", Config.HeroItemId, Config.HeroItemCount, activeChar, true);
					activeChar.setHero(true);
					CustomSQLs.updateDatabase(activeChar, true);
					activeChar.broadcastUserInfo();
				}
			}
			else if (param.equalsIgnoreCase("RecoveryPK"))
			{
				if (activeChar.getInventory().getItemByItemId(Config.RecoveryPKItemId) == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getInventory().getItemByItemId(Config.RecoveryPKItemId).getCount() < Config.RecoveryPKItemCount)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				activeChar.destroyItemByItemId("ShopBBS", Config.RecoveryPKItemId, Config.RecoveryPKItemCount, activeChar, true);
				activeChar.setKarma(0);
				activeChar.setPkKills(0);
				activeChar.broadcastUserInfo();
				activeChar.sendPacket(new UserInfo(activeChar));
				activeChar.sendPacket(new ExBrExtraUserInfo(activeChar));
			}
			else if (param.equalsIgnoreCase("RecoveryVitality"))
			{
				if (activeChar.getInventory().getItemByItemId(Config.RecoveryVitalityItemId) == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getInventory().getItemByItemId(Config.RecoveryVitalityItemId).getCount() < Config.RecoveryVitalityItemCount)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				activeChar.destroyItemByItemId("ShopBBS", Config.RecoveryVitalityItemId, Config.RecoveryVitalityItemCount, activeChar, true);
				activeChar.setVitalityPoints(Math.min(Config.STARTING_VITALITY_POINTS, PcStat.MAX_VITALITY_POINTS), true);
			}
			else if (param.equalsIgnoreCase("AddSP"))
			{
				if (activeChar.getInventory().getItemByItemId(Config.SPItemId) == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getInventory().getItemByItemId(Config.SPItemId).getCount() < Config.SPItemCount)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				activeChar.destroyItemByItemId("ShopBBS", Config.SPItemId, Config.SPItemCount, activeChar, true);
				activeChar.setSp(activeChar.getSp() + 10000000);
				activeChar.sendMessage((new CustomMessage("ServiceBBS.ADDSP_MSG", activeChar.getLang())).toString());
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
				activeChar.broadcastUserInfo();
			}
			else if (param.equalsIgnoreCase("AddPremium"))
			{
				if (activeChar.getInventory().getItemByItemId(Config.PREMIUM_ID) == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getInventory().getItemByItemId(Config.PREMIUM_ID).getCount() < Config.PREMIUM_COUNT)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				if (activeChar.getPremiumService() == 1)
				{
					activeChar.sendMessage((new CustomMessage("ServiceBBS.PREMIUM_MSG", activeChar.getLang())).toString());
					return;
				}
				activeChar.destroyItemByItemId("ShopBBS", ConsumableItemId, (long) Count * 30, activeChar, true);
				getPS(activeChar);
				addPremiumServices(30, activeChar);
				activeChar.sendMessage((new CustomMessage("ServiceBBS.PREMIUM_MSG_1", activeChar.getLang())).toString());
			}
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
	
	private void getPS(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT premium_service FROM character_premium WHERE account_name=?");
			statement.setString(1, player.getAccountName());
			final ResultSet chars = statement.executeQuery();
			PremiumService = chars.getInt("premium_service");
			chars.close();
			statement.close();
		}
		catch (final Exception e)
		{
		}
	}
	
	private void addPremiumServices(int Days, L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.set(Calendar.SECOND, 0);
			finishtime.add(Calendar.DAY_OF_YEAR, Days);
			
			final PreparedStatement statement = con.prepareStatement("UPDATE character_premium SET premium_service=?,enddate=? WHERE account_name=?");
			statement.setInt(1, 1);
			statement.setLong(2, finishtime.getTimeInMillis());
			statement.setString(3, player.getAccountName());
			statement.execute();
			statement.close();
		}
		catch (final SQLException e)
		{
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	public static ServiceBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ServiceBBSManager _instance = new ServiceBBSManager();
	}
}