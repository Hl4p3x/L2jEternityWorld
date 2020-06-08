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
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class L2TerritoryWardInstance extends L2Attackable
{
	public L2TerritoryWardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		disableCoreAI(true);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (isInvul())
		{
			return false;
		}
		if ((getCastle() == null) || !getCastle().getZone().isActive())
		{
			return false;
		}
		
		final L2PcInstance actingPlayer = attacker.getActingPlayer();
		if (actingPlayer == null)
		{
			return false;
		}
		if (actingPlayer.getSiegeSide() == 0)
		{
			return false;
		}
		if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, getCastle().getId()))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		if (getCastle() == null)
		{
			_log.warning("L2TerritoryWardInstance(" + getName() + ") spawned outside Castle Zone!");
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if ((skill != null) || !TerritoryWarManager.getInstance().isTWInProgress())
		{
			return;
		}
		
		final L2PcInstance actingPlayer = attacker.getActingPlayer();
		if (actingPlayer == null)
		{
			return;
		}
		if (actingPlayer.isCombatFlagEquipped())
		{
			return;
		}
		if (actingPlayer.getSiegeSide() == 0)
		{
			return;
		}
		if (getCastle() == null)
		{
			return;
		}
		if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, getCastle().getId()))
		{
			return;
		}
		
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	@Override
	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer) || (getCastle() == null) || !TerritoryWarManager.getInstance().isTWInProgress())
		{
			return false;
		}
		
		if (killer.isPlayer())
		{
			if ((((L2PcInstance) killer).getSiegeSide() > 0) && !((L2PcInstance) killer).isCombatFlagEquipped())
			{
				((L2PcInstance) killer).addItem("Pickup", getId() - 23012, 1, null, false);
			}
			else
			{
				TerritoryWarManager.getInstance().getTerritoryWard(getId() - 36491).spawnMe();
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S1_WARD_HAS_BEEN_DESTROYED_C2_HAS_THE_WARD);
			sm.addString(this.getName().replaceAll(" Ward", ""));
			sm.addPcName((L2PcInstance) killer);
			TerritoryWarManager.getInstance().announceToParticipants(sm, 0, 0);
		}
		else
		{
			TerritoryWarManager.getInstance().getTerritoryWard(getId() - 36491).spawnMe();
		}
		decayMe();
		return true;
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if ((player == null) || !canTarget(player))
		{
			return;
		}
		
		if (this != player.getTarget())
		{
			player.setTarget(this);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && (Math.abs(player.getZ() - getZ()) < 100))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}