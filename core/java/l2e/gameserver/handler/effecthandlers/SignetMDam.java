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
package l2e.gameserver.handler.effecthandlers;

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2EffectPointInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillLaunched;

public class SignetMDam extends L2Effect
{
	private L2EffectPointInstance _actor;
	
	public SignetMDam(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (_actor == null)
		{
			return false;
		}
		
		final int mpConsume = getSkill().getMpConsume();
		final L2PcInstance activeChar = getEffector().getActingPlayer();
		activeChar.rechargeShots(getSkill().useSoulShot(), getSkill().useSpiritShot());
		boolean sps = getSkill().useSpiritShot() && getEffector().isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = getSkill().useSpiritShot() && getEffector().isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final List<L2Character> targets = new ArrayList<>();
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getAffectRange()))
		{
			if ((cha == null) || (cha == activeChar))
			{
				continue;
			}
			
			if (cha.isL2Attackable() || cha.isPlayable())
			{
				if (cha.isAlikeDead())
				{
					continue;
				}
				
				if (mpConsume > activeChar.getCurrentMp())
				{
					activeChar.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
					return false;
				}
				
				activeChar.reduceCurrentMp(mpConsume);
				if (cha.isPlayable())
				{
					if (activeChar.canAttackCharacter(cha))
					{
						targets.add(cha);
						activeChar.updatePvPStatus(cha);
					}
				}
				else
				{
					targets.add(cha);
				}
			}
		}
		
		if (!targets.isEmpty())
		{
			activeChar.broadcastPacket(new MagicSkillLaunched(activeChar, getSkill().getId(), getSkill().getLevel(), targets.toArray(new L2Character[targets.size()])));
			for (L2Character target : targets)
			{
				final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, getSkill()));
				final byte shld = Formulas.calcShldUse(activeChar, target, getSkill());
				final int mdam = (int) Formulas.calcMagicDam(activeChar, target, getSkill(), shld, sps, bss, mcrit);
				
				if (target.isSummon())
				{
					target.broadcastStatusUpdate();
				}
				
				if (mdam > 0)
				{
					if (!target.isRaid() && Formulas.calcAtkBreak(target, mdam))
					{
						target.breakAttack();
						target.breakCast();
					}
					activeChar.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, activeChar, getSkill());
					target.notifyDamageReceived(mdam, activeChar, getSkill(), mcrit, false);
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
			}
		}
		activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
		return false;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}
	
	@Override
	public boolean onStart()
	{
		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(getSkill().getNpcId());
		_actor = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, getEffector());
		_actor.setCurrentHp(_actor.getMaxHp());
		_actor.setCurrentMp(_actor.getMaxMp());
		int x = getEffector().getX();
		int y = getEffector().getY();
		int z = getEffector().getZ();
		if (getEffector().isPlayer() && (getSkill().getTargetType() == L2TargetType.GROUND))
		{
			final Location wordPosition = getEffector().getActingPlayer().getCurrentSkillWorldPosition();
			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		_actor.setIsInvul(true);
		_actor.spawnMe(x, y, z);
		return true;
	}
}