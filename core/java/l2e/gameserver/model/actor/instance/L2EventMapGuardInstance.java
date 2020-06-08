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

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.network.serverpackets.ActionFailed;

public final class L2EventMapGuardInstance extends L2GuardInstance
{
	private static Logger _log = Logger.getLogger(L2GuardInstance.class.getName());

	public L2EventMapGuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (getObjectId() != player.getTargetId())
		{
			if (Config.DEBUG)
				_log.fine(player.getObjectId()+": Targetted guard "+getObjectId());

			player.setTarget(this);
		}
		else
		{
			if (containsTarget(player))
			{
				if (Config.DEBUG) _log.fine(player.getObjectId()+": Attacked guard "+getObjectId());

				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				if (!canInteract(player))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					player.sendMessage("Did you know that you are on the event right now?");
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}