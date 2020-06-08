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

import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Heal extends L2Effect
{
	public Heal(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL;
	}
	
	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		L2Character activeChar = getEffector();
		if (target == null || target.isDead() || target.isDoor())
			return false;
		
		double amount = calc();
		double staticShotBonus = 0;
		int mAtkMul = 1;
		boolean sps = getSkill().isMagic() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = getSkill().isMagic() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		
		if ((sps || bss) && (activeChar.isPlayer() && activeChar.getActingPlayer().isMageClass()) || activeChar.isSummon())
		{
			staticShotBonus = getSkill().getMpConsume();
			mAtkMul = bss ? 4 : 2;
			staticShotBonus *= bss ? 2.4 : 1.0;
		}
		else if ((sps || bss) && activeChar.isNpc())
		{
			staticShotBonus = 2.4 * getSkill().getMpConsume();
			mAtkMul = 4;
		}
		else
		{
			final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
			if (weaponInst != null)
			{
				mAtkMul = weaponInst.getItem().getItemGrade() == L2Item.CRYSTAL_S84 ? 4 : weaponInst.getItem().getItemGrade() == L2Item.CRYSTAL_S80 ? 2 : 1;
			}
			mAtkMul = bss ? mAtkMul * 4 : mAtkMul + 1;
		}
		
		if (!getSkill().isStatic())
		{
			amount += staticShotBonus + Math.sqrt(mAtkMul * activeChar.getMAtk(activeChar, null));
			amount = target.calcStat(Stats.HEAL_EFFECT, amount, null, null);
			if (getSkill().isMagic() && Formulas.calcMCrit(activeChar.getMCriticalHit(target, getSkill())))
			{
				amount *= 3;
			}
		}
		
		amount = Math.max(Math.min(amount, target.getMaxRecoverableHp() - target.getCurrentHp()), 0);
		
		if (amount != 0)
		{
			target.setCurrentHp(amount + target.getCurrentHp());
			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
		}
		
		if (target.isPlayer())
		{
			if (getSkill().getId() == 4051)
			{
				target.sendPacket(SystemMessageId.REJUVENATING_HP);
			}
			else
			{
				if (activeChar.isPlayer() && activeChar != target)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
					sm.addString(activeChar.getName());
					sm.addNumber((int) amount);
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
					sm.addNumber((int) amount);
					target.sendPacket(sm);
				}
			}
		}
		activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
		return true;
	}
}