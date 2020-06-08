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
package l2e.gameserver.handler.actionhandlers;

import java.util.List;

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.handler.IActionHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.L2Event;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.Quest.QuestEventType;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.util.Rnd;

public class L2NpcAction implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (!((L2Npc)target).canTarget(activeChar))
			return false;
		
		activeChar.setLastFolkNPC((L2Npc)target);
		
		if (target != activeChar.getTarget())
		{
			activeChar.setTarget(target);
			
			if (target.isAutoAttackable(activeChar))
			{
				((L2Npc)target).getAI();
			}
		}
		else if (interact)
		{
			if (target.isAutoAttackable(activeChar) && !((L2Character)target).isAlikeDead())
			{
				if (Math.abs(activeChar.getZ() - target.getZ()) < 400)
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
				else
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else if (!target.isAutoAttackable(activeChar))
			{
				if (!((L2Npc)target).canInteract(activeChar))
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				}
				else
				{
					L2Npc npc = (L2Npc) target;
					if (npc.hasRandomAnimation())
						npc.onRandomAnimation(Rnd.get(8));
					
					if (npc.isEventMob())
					{
						L2Event.showEventHtml(activeChar, String.valueOf(target.getObjectId()));
					}
					else
					{
						List<Quest> qlsa = npc.getTemplate().getEventQuests(QuestEventType.QUEST_START);
						List<Quest> qlst = npc.getTemplate().getEventQuests(QuestEventType.ON_FIRST_TALK);
						
						if ((qlsa != null) && !qlsa.isEmpty())
						{
							activeChar.setLastQuestNpcObject(target.getObjectId());
						}
						
						if ((qlst != null) && qlst.size() == 1)
						{
							qlst.get(0).notifyFirstTalk(npc, activeChar);
						}
						else
						{
							npc.showChatWindow(activeChar);
						}
					}
					
					if (Config.PLAYER_MOVEMENT_BLOCK_TIME > 0 && !activeChar.isGM())
						activeChar.updateNotMoveUntil();
				}
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2Npc;
	}
}