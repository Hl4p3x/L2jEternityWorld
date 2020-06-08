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
package l2e.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.StringTokenizer;

import l2e.Config;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.data.xml.TeleLocationParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.model.L2TeleportLocation;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class L2TeleporterInstance extends L2Npc
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;
	
	public L2TeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TeleporterInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		int condition = validateCondition(player);
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		
		if ((player.getFirstEffect(6201) != null) || (player.getFirstEffect(6202) != null) || (player.getFirstEffect(6203) != null))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			
			String filename = "data/html/teleporter/epictransformed.htm";
			
			html.setFile(player.getLang(), filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
		}
		else if (actualCommand.equalsIgnoreCase("goto"))
		{
			int npcId = getId();
			
			switch (npcId)
			{
				case 32534:
				case 32539:
					if (player.isFlyingMounted())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_ENTER_SEED_IN_FLYING_TRANSFORM);
						return;
					}
					break;
			}
			
			if (st.countTokens() <= 0)
			{
				return;
			}
			
			int whereTo = Integer.parseInt(st.nextToken());
			if (condition == COND_REGULAR)
			{
				doTeleport(player, whereTo);
				return;
			}
			else if (condition == COND_OWNER)
			{
				int minPrivilegeLevel = 0;
				if (st.countTokens() >= 1)
				{
					minPrivilegeLevel = Integer.parseInt(st.nextToken());
				}
				
				if (10 >= minPrivilegeLevel)
				{
					doTeleport(player, whereTo);
				}
				else
				{
					player.sendMessage("You don't have the sufficient access level to teleport there.");
				}
				return;
			}
		}
		else if (command.startsWith("Chat"))
		{
			Calendar cal = Calendar.getInstance();
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			
			if ((val == 1) && (player.getLevel() < 41))
			{
				showNewbieHtml(player);
				return;
			}
			else if ((val == 1) && (cal.get(Calendar.HOUR_OF_DAY) >= 20) && (cal.get(Calendar.HOUR_OF_DAY) <= 23) && ((cal.get(Calendar.DAY_OF_WEEK) == 1) || (cal.get(Calendar.DAY_OF_WEEK) == 7)))
			{
				showHalfPriceHtml(player);
				return;
			}
			showChatWindow(player, val);
		}
		
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/teleporter/" + pom + ".htm";
	}
	
	private void showNewbieHtml(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		String filename = "data/html/teleporter/free/" + getTemplate().getId() + ".htm";
		if (!HtmCache.getInstance().isLoadable(filename))
		{
			filename = "data/html/teleporter/" + getTemplate().getId() + "-1.htm";
		}
		
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private void showHalfPriceHtml(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		String filename = "data/html/teleporter/half/" + getId() + ".htm";
		if (!HtmCache.getInstance().isLoadable(filename))
		{
			filename = "data/html/teleporter/" + getId() + "-1.htm";
		}
		
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";
		
		int condition = validateCondition(player);
		if (condition == COND_REGULAR)
		{
			super.showChatWindow(player);
			return;
		}
		else if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/teleporter/castleteleporter-busy.htm";
			}
			else if (condition == COND_OWNER)
			{
				filename = getHtmlPath(getId(), 0);
			}
		}
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleLocationParser.getInstance().getTemplate(val);
		if (list != null)
		{
			if (Config.TW_DISABLE_GK && isInTownWarEvent())
			{
				player.sendMessage((new CustomMessage("TW.DISABLE_GK", player.getLang())).toString());
				return;
			}
			else if (SiegeManager.getInstance().getSiege(list.getLocX(), list.getLocY(), list.getLocZ()) != null)
			{
				player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
				return;
			}
			else if (TownManager.townHasCastleInSiege(list.getLocX(), list.getLocY()) && isInsideZone(ZoneId.TOWN))
			{
				player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
				return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (player.getKarma() > 0))
			{
				player.sendMessage("Go away, you're not welcome here.");
				return;
			}
			else if (player.isCombatFlagEquipped())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
				return;
			}
			else if (list.getIsForNoble() && !player.isNoble())
			{
				String filename = "data/html/teleporter/nobleteleporter-no.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getLang(), filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			else if (player.isAlikeDead())
			{
				return;
			}
			
			Calendar cal = Calendar.getInstance();
			int price = list.getPrice();
			
			if (player.getLevel() < 41)
			{
				price = 0;
			}
			else if (!list.getIsForNoble())
			{
				if ((cal.get(Calendar.HOUR_OF_DAY) >= 20) && (cal.get(Calendar.HOUR_OF_DAY) <= 23) && ((cal.get(Calendar.DAY_OF_WEEK) == 1) || (cal.get(Calendar.DAY_OF_WEEK) == 7)))
				{
					price /= 2;
				}
			}
			
			if (Config.ALT_GAME_FREE_TELEPORT || player.destroyItemByItemId("Teleport " + (list.getIsForNoble() ? " nobless" : ""), list.getItemId(), price, this, true))
			{
				if (Config.DEBUG)
				{
					_log.info("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
				}
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
			}
		}
		else
		{
			_log.warning("No teleport destination with id:" + val);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private int validateCondition(L2PcInstance player)
	{
		if (CastleManager.getInstance().getCastleIndex(this) < 0)
		{
			return COND_REGULAR;
		}
		else if (getCastle().getSiege().getIsInProgress())
		{
			return COND_BUSY_BECAUSE_OF_SIEGE;
		}
		else if (player.getClan() != null)
		{
			if (getCastle().getOwnerId() == player.getClanId())
			{
				return COND_OWNER;
			}
		}
		return COND_ALL_FALSE;
	}
}