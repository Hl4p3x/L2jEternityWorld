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
package l2e.gameserver.handler.skillhandlers;

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2ChestInstance;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.util.Rnd;

public class Unlock implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.UNLOCK,
		L2SkillType.UNLOCK_SPECIAL
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for (L2Object target : targets)
		{
			if (target.isDoor())
			{
				L2DoorInstance door = (L2DoorInstance) target;
				
				if (activeChar.getInstanceId() != door.getInstanceId())
				{
					final Instance inst = InstanceManager.getInstance().getInstance(activeChar.getInstanceId());
					if (inst == null)
					{
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					final L2DoorInstance instanceDoor = inst.getDoor(door.getDoorId());
					if (instanceDoor != null)
					{
						door = instanceDoor;
					}
					if (activeChar.getInstanceId() != door.getInstanceId())
					{
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				
				if ((!door.isOpenableBySkill() && (skill.getSkillType() != L2SkillType.UNLOCK_SPECIAL)) || (door.getFort() != null))
				{
					activeChar.sendPacket(SystemMessageId.UNABLE_TO_UNLOCK_DOOR);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (doorUnlock(skill) && !door.getOpen())
				{
					door.openMe();
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.FAILED_TO_UNLOCK_DOOR);
				}
			}
			else if (target instanceof L2ChestInstance)
			{
				L2ChestInstance chest = (L2ChestInstance) target;
				if ((chest.getCurrentHp() <= 0) || (activeChar.getInstanceId() != chest.getInstanceId()))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				final L2Npc npc = (L2Npc) target;
				double chance = calcChance(npc, activeChar, skill);
				
				chest.setInteracted();
				if (Rnd.chance(chance))
				{
					activeChar.broadcastSocialAction(3);
					chest.setSpecialDrop();
					chest.setMustRewardExpSp(false);
					chest.reduceCurrentHp(99999999, activeChar, skill);
				}
				else
				{
					L2Skill bomb = SkillHolder.getInstance().getInfo(4143, getBombLvl(npc));
					if (bomb != null)
					{
						npc.setTarget(activeChar);
						npc.doCast(bomb);
					}
				}
			}
		}
	}
	
	private static final boolean doorUnlock(L2Skill skill)
	{
		if (skill.getSkillType() == L2SkillType.UNLOCK_SPECIAL)
		{
			return Rnd.get(100) < skill.getPower();
		}
		
		switch (skill.getLevel())
		{
			case 0:
				return false;
			case 1:
				return Rnd.get(120) < 30;
			case 2:
				return Rnd.get(120) < 50;
			case 3:
				return Rnd.get(120) < 75;
			default:
				return Rnd.get(120) < 100;
		}
	}
	
	private double calcChance(L2Npc npc, L2Character activeChar, L2Skill skill)
	{
		double chance = skill.getActivateRate();
		int npcLvl = npc.getLevel();
		
		if (!isCommonTreasureChest(npc))
		{
			double levelmod = (double) skill.getMagicLevel() - npcLvl;
			chance += levelmod * skill.getLevelModifier();
		}
		else
		{
			int openerLvl = activeChar.getLevel();
			int lvlDiff = Math.max(openerLvl - npcLvl, 0);
			if (((openerLvl <= 77) && (lvlDiff >= 6)) || ((openerLvl >= 78) && (lvlDiff >= 5)))
			{
				chance = 0;
			}
		}
		
		if (chance < 0)
		{
			chance = 1;
		}
		
		return chance;
	}
	
	private int getBombLvl(L2Npc npc)
	{
		int npcLvl = npc.getLevel();
		int lvl = 1;
		if (npcLvl >= 78)
		{
			lvl = 10;
		}
		else if (npcLvl >= 72)
		{
			lvl = 9;
		}
		else if (npcLvl >= 66)
		{
			lvl = 8;
		}
		else if (npcLvl >= 60)
		{
			lvl = 7;
		}
		else if (npcLvl >= 54)
		{
			lvl = 6;
		}
		else if (npcLvl >= 48)
		{
			lvl = 5;
		}
		else if (npcLvl >= 42)
		{
			lvl = 4;
		}
		else if (npcLvl >= 36)
		{
			lvl = 3;
		}
		else if (npcLvl >= 30)
		{
			lvl = 2;
		}
		return lvl;
	}
	
	private boolean isCommonTreasureChest(L2Npc npc)
	{
		int npcId = npc.getId();
		
		if ((npcId >= 18265) && (npcId <= 18286))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}