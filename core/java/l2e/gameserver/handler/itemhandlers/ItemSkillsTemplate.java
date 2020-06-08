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
package l2e.gameserver.handler.itemhandlers;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2EtcItemType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ItemSkillsTemplate implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer() && !playable.isPet())
		{
			return false;
		}
		
		if (!TvTEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (!TvTRoundEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (playable.isPet() && !item.isTradeable())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		if (!checkReuse(playable, null, item))
		{
			return false;
		}
		
		final SkillsHolder[] skills = item.getEtcItem().getSkills();
		if (skills == null)
		{
			_log.info("Item " + item + " does not have registered any skill for handler.");
			return false;
		}
		
		int skillId;
		int skillLvl;
		final L2PcInstance activeChar = playable.getActingPlayer();
		for (SkillsHolder skillInfo : skills)
		{
			if (skillInfo == null)
			{
				continue;
			}
			
			L2Skill itemSkill = skillInfo.getSkill();			
			if (itemSkill != null)
			{
				if (!itemSkill.checkCondition(playable, playable.getTarget(), false))
				{
					return false;
				}
				
				if (playable.isSkillDisabled(itemSkill))
				{
					return false;
				}

				if (!checkReuse(playable, itemSkill, item))
				{
					return false;
				}
				
				if (!item.isPotion() && !item.isElixir() && playable.isCastingNow())
				{
					return false;
				}
				
				if ((itemSkill.getItemConsumeId() == 0) && (itemSkill.getItemConsume() > 0) && (item.isPotion() || item.isElixir() || itemSkill.isSimultaneousCast()))
				{
					if (!playable.destroyItem("Consume", item.getObjectId(), itemSkill.getItemConsume(), playable, false))
					{
						playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
						return false;
					}
				}
				
				if (playable.isPet())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1);
					sm.addSkillName(itemSkill);
					playable.sendPacket(sm);
				}
				else
				{
					skillId = skillInfo.getSkillId();
					skillLvl = skillInfo.getSkillLvl();
					switch (skillId)
					{
						case 2031:
						case 2032:
						case 2037:
						case 26025:
						case 26026:
							final int buffId = activeChar.getShortBuffTaskSkillId();
							if ((skillId == 2037) || (skillId == 26025))
							{
								activeChar.shortBuffStatusUpdate(skillId, skillLvl, itemSkill.getEffectTemplates()[0].getTotalTickCount());
							}
							else if (((skillId == 2032) || (skillId == 26026)) && (buffId != 2037) && (buffId != 26025))
							{
								activeChar.shortBuffStatusUpdate(skillId, skillLvl, itemSkill.getEffectTemplates()[0].getTotalTickCount());
							}
							else
							{
								if ((buffId != 2037) && (buffId != 26025) && (buffId != 2032) && (buffId != 26026))
								{
									activeChar.shortBuffStatusUpdate(skillId, skillLvl, itemSkill.getEffectTemplates()[0].getTotalTickCount());
								}
							}
							break;
					}
				}
				
				if (item.isPotion() || item.isElixir() || (item.getItemType() == L2EtcItemType.HERB) || itemSkill.isSimultaneousCast())
				{
					playable.doSimultaneousCast(itemSkill);

					if (!playable.isSummon() && activeChar.hasSummon())
					{
						activeChar.getSummon().doSimultaneousCast(itemSkill);
					}
				}
				else
				{
					playable.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					
					if (!playable.useMagic(itemSkill, forceUse, false))
					{
						return false;
					}
					
					if ((itemSkill.getItemConsumeId() == 0) && (itemSkill.getItemConsume() > 0))
					{
						if (!playable.destroyItem("Consume", item.getObjectId(), itemSkill.getItemConsume(), null, false))
						{
							playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
							return false;
						}
					}
				}
				
				if (itemSkill.getReuseDelay() > 0)
				{
					playable.addTimeStamp(itemSkill, itemSkill.getReuseDelay());
				}
			}
		}
		return true;
	}
	
	private boolean checkReuse(L2Playable playable, L2Skill skill, L2ItemInstance item)
	{
		final long remainingTime = (skill != null) ? playable.getSkillRemainingReuseTime(skill.getReuseHashCode()) : playable.getItemRemainingReuseTime(item.getObjectId());
		final boolean isAvailable = remainingTime <= 0;
		if (playable.isPlayer())
		{
			if (!isAvailable)
			{
				final int hours = (int) (remainingTime / 3600000L);
				final int minutes = (int) (remainingTime % 3600000L) / 60000;
				final int seconds = (int) ((remainingTime / 1000) % 60);
				SystemMessage sm = null;
				if (hours > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
					if ((skill == null) || skill.isStatic())
					{
						sm.addItemName(item);
					}
					else
					{
						sm.addSkillName(skill);
					}
					sm.addNumber(hours);
					sm.addNumber(minutes);
				}
				else if (minutes > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
					if ((skill == null) || skill.isStatic())
					{
						sm.addItemName(item);
					}
					else
					{
						sm.addSkillName(skill);
					}
					sm.addNumber(minutes);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
					if ((skill == null) || skill.isStatic())
					{
						sm.addItemName(item);
					}
					else
					{
						sm.addSkillName(skill);
					}
				}
				sm.addNumber(seconds);
				playable.sendPacket(sm);
			}
		}
		return isAvailable;
	}
}