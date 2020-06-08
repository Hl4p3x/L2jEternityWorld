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

import l2e.Config;
import l2e.gameserver.Announcements;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.CoupleManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.Couple;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2WeddingManagerInstance extends L2Npc
{
	public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2WeddingManagerInstance);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/mods/Wedding_start.htm";
		String replace = String.valueOf(Config.WEDDING_PRICE);
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		String filename = "data/html/mods/Wedding_start.htm";
		String replace = "";
		
		if (player.getPartnerId() == 0)
		{
			filename = "data/html/mods/Wedding_nopartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		
		final L2PcInstance ptarget = L2World.getInstance().getPlayer(player.getPartnerId());
		
		if ((ptarget == null) || !ptarget.isOnline())
		{
			filename = "data/html/mods/Wedding_notfound.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		
		if (player.isMarried())
		{
			filename = "data/html/mods/Wedding_already.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (player.isMarryAccepted())
		{
			filename = "data/html/mods/Wedding_waitforpartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AcceptWedding"))
		{
			if (!wearsFormalWear(player, ptarget))
			{
				filename = "data/html/mods/Wedding_noformal.htm";
				sendHtmlMessage(ptarget, filename, replace);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			
			if ((player.getAdena() < Config.WEDDING_PRICE) || (ptarget.getAdena() < Config.WEDDING_PRICE))
			{
				filename = "data/html/mods/Wedding_adena.htm";
				replace = String.valueOf(Config.WEDDING_PRICE);
				sendHtmlMessage(ptarget, filename, replace);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			player.reduceAdena("Wedding", Config.WEDDING_PRICE, player.getLastFolkNPC(), true);
			ptarget.reduceAdena("Wedding", Config.WEDDING_PRICE, player.getLastFolkNPC(), true);
			player.setMarryAccepted(true);
			Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
			couple.marry();
			player.sendMessage("Congratulations you are married!");
			player.setMarried(true);
			player.setMarryRequest(false);
			ptarget.sendMessage("Congratulations you are married!");
			ptarget.setMarried(true);
			ptarget.setMarryRequest(false);
			
			MagicSkillUse MSU = new MagicSkillUse(player, player, 2230, 1, 1, 0);
			player.broadcastPacket(MSU);
			MSU = new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0);
			ptarget.broadcastPacket(MSU);
			
			L2Skill skill = SkillHolder.FrequentSkill.LARGE_FIREWORK.getSkill();
			if (skill != null)
			{
				MSU = new MagicSkillUse(player, player, 2025, 1, 1, 0);
				player.sendPacket(MSU);
				player.broadcastPacket(MSU);
				player.useMagic(skill, false, false);
				
				MSU = new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0);
				ptarget.sendPacket(MSU);
				ptarget.broadcastPacket(MSU);
				ptarget.useMagic(skill, false, false);
				
			}
			Announcements.getInstance().announceToAll("Congratulations to " + player.getName() + " and " + ptarget.getName() + "! They have been married.");
			
			filename = "data/html/mods/Wedding_accepted.htm";
			sendHtmlMessage(ptarget, filename, replace);
			sendHtmlMessage(player, filename, replace);
			
			return;
		}
		else if (command.startsWith("DeclineWedding"))
		{
			player.setMarryRequest(false);
			ptarget.setMarryRequest(false);
			player.setMarryAccepted(false);
			ptarget.setMarryAccepted(false);
			
			player.sendMessage("You declined your partner's marriage request.");
			ptarget.sendMessage("Your partner declined your marriage request.");
			
			filename = "data/html/mods/Wedding_declined.htm";
			sendHtmlMessage(ptarget, filename, replace);
			sendHtmlMessage(player, filename, replace);
			
			return;
		}
		else if (player.isMarryRequest())
		{
			if (!wearsFormalWear(player, ptarget))
			{
				filename = "data/html/mods/Wedding_noformal.htm";
				sendHtmlMessage(ptarget, filename, replace);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			filename = "data/html/mods/Wedding_ask.htm";
			player.setMarryRequest(false);
			ptarget.setMarryRequest(false);
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AskWedding"))
		{
			if (!wearsFormalWear(player, ptarget))
			{
				filename = "data/html/mods/Wedding_noformal.htm";
				sendHtmlMessage(ptarget, filename, replace);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			
			player.setMarryAccepted(true);
			ptarget.setMarryRequest(true);
			
			filename = "data/html/mods/Wedding_ask.htm";
			replace = player.getName();
			sendHtmlMessage(ptarget, filename, replace);
			
			filename = "data/html/mods/Wedding_requested.htm";
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
			
			return;
		}
		sendHtmlMessage(player, filename, replace);
	}
	
	private boolean wearsFormalWear(L2PcInstance p1, L2PcInstance p2)
	{
		if (Config.WEDDING_FORMALWEAR)
		{
			L2ItemInstance fw1 = p1.getChestArmorInstance();
			L2ItemInstance fw2 = p2.getChestArmorInstance();
			
			if ((fw1 == null) || (fw2 == null) || (fw1.getId() != 6408) || (fw2.getId() != 6408))
			{
				return false;
			}
		}
		return true;
	}
	
	private void sendHtmlMessage(L2PcInstance player, String filename, String replace)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}