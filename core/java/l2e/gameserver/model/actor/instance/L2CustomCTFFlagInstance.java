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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.events.CTF;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.ActionFailed;

public class L2CustomCTFFlagInstance extends L2Npc
{
	public String _mode;
	public int _teamId;
	public CTF _event;

	public L2CustomCTFFlagInstance(int objectId, L2NpcTemplate template)
    	{
		super(objectId, template);
		setServerSideName(true);
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
			return;

		if (_mode != null && _mode.equals("THRONE"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (this != player.getTarget())
		{
			player.setTarget(this);
		}
		else if (interact)
		{
			if (!canInteract(player))
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			else
			{
				if (player._CTFHaveFlagOfTeam > 0 && player._eventTeamId == _teamId)
					_event.onPlayerBringFlag(player);
				else if (player._CTFHaveFlagOfTeam == 0 && player._eventTeamId != _teamId)
					_event.onPlayerTakeFlag(player, _teamId);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		return;
	}
}