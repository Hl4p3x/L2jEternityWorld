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
package l2e.gameserver.communitybbs;

import java.util.StringTokenizer;

import l2e.Config;
import l2e.gameserver.communitybbs.Manager.AccountBBSManager;
import l2e.gameserver.communitybbs.Manager.BuffBBSManager;
import l2e.gameserver.communitybbs.Manager.ClanBBSManager;
import l2e.gameserver.communitybbs.Manager.ClassBBSManager;
import l2e.gameserver.communitybbs.Manager.CustomServiceBBSManager;
import l2e.gameserver.communitybbs.Manager.EnchantBBSManager;
import l2e.gameserver.communitybbs.Manager.EventBBSManager;
import l2e.gameserver.communitybbs.Manager.FavoritesBBSManager;
import l2e.gameserver.communitybbs.Manager.FriendsBBSManager;
import l2e.gameserver.communitybbs.Manager.LinkBBSManager;
import l2e.gameserver.communitybbs.Manager.MailBBSManager;
import l2e.gameserver.communitybbs.Manager.PostBBSManager;
import l2e.gameserver.communitybbs.Manager.RegionBBSManager;
import l2e.gameserver.communitybbs.Manager.ServiceBBSManager;
import l2e.gameserver.communitybbs.Manager.ShopBBSManager;
import l2e.gameserver.communitybbs.Manager.StateBBSManager;
import l2e.gameserver.communitybbs.Manager.TeleportBBSManager;
import l2e.gameserver.communitybbs.Manager.TopBBSManager;
import l2e.gameserver.communitybbs.Manager.TopicBBSManager;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.xml.MultiSellParser;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2e.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.scripts.events.LastHero;

public class CommunityBoard
{
	protected CommunityBoard()
	{
	}
	
	public static CommunityBoard getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		String lang = activeChar.getLang();
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isAlikeDead())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isInSiege())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isFightingInEvent() || activeChar.isFightingInTW() || (TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(activeChar.getObjectId())) || (TvTRoundEvent.isStarted() && TvTRoundEvent.isPlayerParticipant(activeChar.getObjectId())) || ((LastHero.CurrentState == LastHero.EventState.Battle) && LastHero.Players.contains(activeChar.getName())) || activeChar.getIsInMonsterRush())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isInsideZone(ZoneId.PVP))
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isInCombat())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isDead())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isCastingNow())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isAttackingNow())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isJailed())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isFlying())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (activeChar.isInDuel())
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
			return;
		}
		if (Config.ALLOW_COMMUNITY_PEACE_ZONE)
		{
			if (!activeChar.isInsideZone(ZoneId.PEACE))
			{
				activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ALL_DISABLE") + "");
				return;
			}
		}
		
		switch (Config.COMMUNITY_TYPE)
		{
			default:
			case 0:
				activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
				break;
			case 1:
				if (Config.ALLOW_COMMUNITY_REGION_MANAGER)
				{
					RegionBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else
				{
					activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
					return;
				}
				break;
			case 2:
				if (command.startsWith("_bbsclan"))
				{
					if (Config.ALLOW_COMMUNITY_CLAN_MANAGER)
					{
						ClanBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsmemo") || command.startsWith("_bbstopics"))
				{
					if (Config.ALLOW_COMMUNITY_TOPIC_MANAGER)
					{
						TopicBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsposts"))
				{
					if (Config.ALLOW_COMMUNITY_POST_MANAGER)
					{
						PostBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbstop") || command.startsWith("_bbshome"))
				{
					if (Config.ALLOW_COMMUNITY_TOP_MANAGER)
					{
						TopBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsloc"))
				{
					if (Config.ALLOW_COMMUNITY_REGION_MANAGER)
					{
						RegionBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsstat;"))
				{
					if (Config.ALLOW_COMMUNITY_STATS)
					{
						StateBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.STATS_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsteleport;"))
				{
					if (Config.ALLOW_COMMUNITY_TELEPORT)
					{
						TeleportBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.TELEPOT_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbs_buff"))
				{
					if (Config.ALLOW_COMMUNITY_BUFF)
					{
						if (activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isInSiege() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isJailed() || activeChar.isFlying() || (activeChar.getKarma() > 0) || activeChar.isInDuel())
						{
							activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.BUFFER_DISABLE") + "");
							return;
						}
						BuffBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.BUFFER_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsservice"))
				{
					if (Config.ALLOW_COMMUNITY_SERVICES)
					{
						ServiceBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.SERVICES_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbs_service"))
				{
					if (Config.ALLOW_COMMUNITY_SERVICES)
					{
						CustomServiceBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.SERVICES_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsechant"))
				{
					if (Config.ALLOW_COMMUNITY_ENCHANT)
					{
						EnchantBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ENCHANT_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsclass"))
				{
					if (Config.ALLOW_COMMUNITY_CLASS)
					{
						ClassBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.CLASSMASTER_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsmultisell;"))
				{
					if (Config.ALLOW_COMMUNITY_MULTISELL)
					{
						if (activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isInSiege() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isJailed() || activeChar.isFlying() || (activeChar.getKarma() > 0) || activeChar.isInDuel())
						{
							activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.MULTISELL_DISABLE") + "");
							return;
						}
						StringTokenizer st = new StringTokenizer(command, ";");
						st.nextToken();
						TopBBSManager.getInstance().parsecmd("_bbstop;" + st.nextToken(), activeChar);
						int multisell = Integer.parseInt(st.nextToken());
						MultiSellParser.getInstance().separateAndSend(multisell, activeChar, null, false);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.SHOP_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsshop") || command.startsWith("_bbsshop;adena"))
				{
					if (Config.ALLOW_COMMUNITY_MULTISELL)
					{
						ShopBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.SHOP_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsAugment;add"))
				{
					if (Config.ALLOW_COMMUNITY_MULTISELL)
					{
						TopBBSManager.getInstance().parsecmd(command, activeChar);
						activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED);
						activeChar.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET);
						activeChar.cancelActiveTrade();
						TopBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ENCHANT_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsAugment;remove"))
				{
					if (Config.ALLOW_COMMUNITY_MULTISELL)
					{
						TopBBSManager.getInstance().parsecmd(command, activeChar);
						activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
						activeChar.sendPacket(ExShowVariationCancelWindow.STATIC_PACKET);
						activeChar.cancelActiveTrade();
						TopBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.ENCHANT_DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbsevent"))
				{
					if (Config.ALLOW_COMMUNITY_EVENTS)
					{
						EventBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.EVENTS_DISABLE") + "");
					}
				}
				else if (command.startsWith("_bbsaccount;"))
				{
					if (Config.ALLOW_COMMUNITY_ACCOUNT)
					{
						AccountBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.EVENTS_DISABLE") + "");
					}
				}
				else if (command.startsWith("_bbsgetfav") || command.startsWith("_bbsaddfav") || command.startsWith("_bbsdelfav"))
				{
					if (Config.ALLOW_COMMUNITY_FAVORITE_MANAGER)
					{
						FavoritesBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_friendlist_0_"))
				{
					if (Config.ALLOW_COMMUNITY_FRIENDS_MANAGER)
					{
						FriendsBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_maillist_0_1_0_"))
				{
					if (Config.ALLOW_COMMUNITY_MAIL_MANAGER)
					{
						MailBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
						return;
					}
				}
				else if (command.startsWith("_bbslink"))
				{
					if (Config.ALLOW_COMMUNITY_LINK_MANAGER)
					{
						LinkBBSManager.getInstance().parsecmd(command, activeChar);
					}
					else
					{
						activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(lang, "Community.DISABLE") + "");
						return;
					}
				}
				else
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
					activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
				}
				break;
		}
	}
	
	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		switch (Config.COMMUNITY_TYPE)
		{
			case 2:
				if (url.equals("Topic"))
				{
					TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Post"))
				{
					PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Region"))
				{
					RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Friends"))
				{
					FriendsBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Notice"))
				{
					ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else if (url.equals("Mail"))
				{
					MailBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				}
				else
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + url + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
					activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
				}
				break;
			case 1:
				RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				break;
			default:
			case 0:
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>The Community board is currently disabled</center><br><br></body></html>", "101", activeChar);
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
				activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
				break;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityBoard _instance = new CommunityBoard();
	}
}