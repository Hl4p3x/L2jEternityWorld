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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.data.sql.NpcBufferHolder;
import l2e.gameserver.data.sql.NpcBufferHolder.NpcBufferData;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public class L2NpcBufferInstance extends L2Npc
{
	private static final Logger _log = Logger.getLogger(L2NpcBufferInstance.class.getName());
	
	private static final Map<Integer, Integer> pageVal = new HashMap<>();
	
	public L2NpcBufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2NpcBufferInstance);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (player == null)
		{
			return;
		}
		
		String htmContent = HtmCache.getInstance().getHtm(player.getLang(), "data/html/mods/NpcBuffer.htm");
		if (val > 0)
		{
			htmContent = HtmCache.getInstance().getHtm(player.getLang(), "data/html/mods/NpcBuffer-" + val + ".htm");
		}
		
		if (htmContent != null)
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
			npcHtmlMessage.setHtml(htmContent);
			npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(npcHtmlMessage);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if ((player == null) || (player.getLastFolkNPC() == null) || (player.getLastFolkNPC().getObjectId() != getObjectId()))
		{
			return;
		}
		
		L2Character target = player;
		if (command.startsWith("Pet"))
		{
			if (!player.hasSummon())
			{
				player.sendPacket(SystemMessageId.DONT_HAVE_PET);
				showChatWindow(player, 0);
				return;
			}
			target = player.getSummon();
		}
		
		int npcId = getId();
		if (command.startsWith("Chat"))
		{
			int val = Integer.parseInt(command.substring(5));
			
			pageVal.put(player.getObjectId(), val);
			
			showChatWindow(player, val);
		}
		else if (command.startsWith("Buff") || command.startsWith("PetBuff"))
		{
			String[] buffGroupArray = command.substring(command.indexOf("Buff") + 5).split(" ");
			
			for (String buffGroupList : buffGroupArray)
			{
				if (buffGroupList == null)
				{
					_log.warning("NPC Buffer Warning: npcId = " + npcId + " has no buffGroup set in the bypass for the buff selected.");
					return;
				}
				
				int buffGroup = Integer.parseInt(buffGroupList);
				
				final NpcBufferData npcBuffGroupInfo = NpcBufferHolder.getInstance().getSkillInfo(npcId, buffGroup);
				if (npcBuffGroupInfo == null)
				{
					_log.warning("NPC Buffer Warning: npcId = " + npcId + " Location: " + getX() + ", " + getY() + ", " + getZ() + " Player: " + player.getName() + " has tried to use skill group (" + buffGroup + ") not assigned to the NPC Buffer!");
					return;
				}
				
				if (npcBuffGroupInfo.getFee().getId() != 0)
				{
					L2ItemInstance itemInstance = player.getInventory().getItemByItemId(npcBuffGroupInfo.getFee().getId());
					if ((itemInstance == null) || (!itemInstance.isStackable() && (player.getInventory().getInventoryItemCount(npcBuffGroupInfo.getFee().getId(), -1) < npcBuffGroupInfo.getFee().getCount())))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
						player.sendPacket(sm);
						continue;
					}
					
					if (itemInstance.isStackable())
					{
						if (!player.destroyItemByItemId("Npc Buffer", npcBuffGroupInfo.getFee().getId(), npcBuffGroupInfo.getFee().getCount(), player.getTarget(), true))
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
							player.sendPacket(sm);
							continue;
						}
					}
					else
					{
						for (int i = 0; i < npcBuffGroupInfo.getFee().getCount(); ++i)
						{
							player.destroyItemByItemId("Npc Buffer", npcBuffGroupInfo.getFee().getId(), 1, player.getTarget(), true);
						}
					}
				}
				
				final L2Skill skill = SkillHolder.getInstance().getInfo(npcBuffGroupInfo.getSkill().getSkillId(), npcBuffGroupInfo.getSkill().getSkillLvl());
				if (skill != null)
				{
					skill.getEffects(player, target);
				}
			}
			
			showChatWindow(player, pageVal.get(player.getObjectId()));
		}
		else if (command.startsWith("Heal") || command.startsWith("PetHeal"))
		{
			if (!target.isInCombat() && !AttackStanceTaskManager.getInstance().hasAttackStanceTask(target))
			{
				String[] healArray = command.substring(command.indexOf("Heal") + 5).split(" ");
				
				for (String healType : healArray)
				{
					if (healType.equalsIgnoreCase("HP"))
					{
						target.setCurrentHp(target.getMaxHp());
					}
					else if (healType.equalsIgnoreCase("MP"))
					{
						target.setCurrentMp(target.getMaxMp());
					}
					else if (healType.equalsIgnoreCase("CP"))
					{
						target.setCurrentCp(target.getMaxCp());
					}
				}
			}
			showChatWindow(player, pageVal.get(player.getObjectId()));
		}
		else if (command.startsWith("RemoveBuffs") || command.startsWith("PetRemoveBuffs"))
		{
			target.stopAllEffectsExceptThoseThatLastThroughDeath();
			showChatWindow(player, pageVal.get(player.getObjectId()));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}