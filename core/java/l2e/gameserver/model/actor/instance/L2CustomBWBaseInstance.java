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
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.events.BW;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.ActionFailed;

public class L2CustomBWBaseInstance extends L2Npc
{
	public int _teamId;
	public BW _event;

	public L2CustomBWBaseInstance(int objectId, L2NpcTemplate template)
    	{
		super(objectId, template);
		setServerSideName(true);
	}

	public boolean canAttack(L2PcInstance player)
	{
		if (player._eventTeamId != 0 && player._eventTeamId != _teamId)
			return true;
		return false;
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);
		}
		else if (interact)
		{
			if (!isAlikeDead() && Math.abs(player.getZ() - getZ()) < 400 && canAttack(player))
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
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
		L2PcInstance player = null;
		if (attacker.isPlayer())
			player = (L2PcInstance) attacker;
		else if (attacker instanceof L2Summon)
			player = ((L2Summon) attacker).getOwner();
		else
			return;

		if (!canAttack(player))
			return;

		player.updatePvPStatus();

		if (damage < getStatus().getCurrentHp())
			getStatus().setCurrentHp(getStatus().getCurrentHp() - damage);
		else
			doDie(attacker);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		L2PcInstance player = null;
		if (killer.isPlayer())
			player = (L2PcInstance) killer;
		else if (killer instanceof L2Summon)
			player = ((L2Summon) killer).getOwner();
		else
			return false;

		if (!super.doDie(killer))
			return false;

		_event.onPlayerKillBase(player, _teamId);
		return true;
	}
}