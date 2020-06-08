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

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.xml.ManorParser;
import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.quest.Quest.QuestSound;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Rnd;

public class Sow implements ISkillHandler
{
	private static Logger _log = Logger.getLogger(Sow.class.getName());
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SOW
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!activeChar.isPlayer())
		{
			return;
		}
		
		final L2Object[] targetList = skill.getTargetList(activeChar);
		if ((targetList == null) || (targetList.length == 0))
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.info("Casting sow");
		}
		
		L2MonsterInstance target;
		
		for (L2Object tgt : targetList)
		{
			if (!tgt.isMonster())
			{
				continue;
			}
			
			target = (L2MonsterInstance) tgt;
			if (target.isDead() || target.isSeeded() || (target.getSeederId() != activeChar.getObjectId()))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			final int seedId = target.getSeedType();
			if (seedId == 0)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			if (!activeChar.destroyItemByItemId("Consume", seedId, 1, target, false))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			SystemMessage sm;
			if (calcSuccess(activeChar, target, seedId))
			{
				activeChar.sendPacket(QuestSound.ITEMSOUND_QUEST_ITEMGET.getPacket());
				target.setSeeded(activeChar.getActingPlayer());
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
			}
			
			if (activeChar.getParty() == null)
			{
				activeChar.sendPacket(sm);
			}
			else
			{
				activeChar.getParty().broadcastPacket(sm);
			}
			
			target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	private boolean calcSuccess(L2Character activeChar, L2Character target, int seedId)
	{
		int basicSuccess = (ManorParser.getInstance().isAlternative(seedId) ? 20 : 90);
		final int minlevelSeed = ManorParser.getInstance().getSeedMinLevel(seedId);
		final int maxlevelSeed = ManorParser.getInstance().getSeedMaxLevel(seedId);
		final int levelPlayer = activeChar.getLevel();
		final int levelTarget = target.getLevel();
		
		if (levelTarget < minlevelSeed)
		{
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		}
		if (levelTarget > maxlevelSeed)
		{
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);
		}
		
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
		{
			diff = -diff;
		}
		if (diff > 5)
		{
			basicSuccess -= 5 * (diff - 5);
		}
		
		if (basicSuccess < 1)
		{
			basicSuccess = 1;
		}
		
		return Rnd.nextInt(99) < basicSuccess;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}