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

import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;

public class Spoil extends L2Effect
{
	public Spoil(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SPOIL;
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isMonster() || getEffected().isDead())
		{
			getEffector().sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}
		
		final L2MonsterInstance target = (L2MonsterInstance) getEffected();
		if (target.isSpoil())
		{
			getEffector().sendPacket(SystemMessageId.ALREADY_SPOILED);
			return false;
		}
		
		if (Formulas.calcMagicSuccess(getEffector(), target, getSkill()))
		{
			target.setSpoil(true);
			target.setIsSpoiledBy(getEffector().getObjectId());
			getEffector().sendPacket(SystemMessageId.SPOIL_SUCCESS);
		}
		target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
		return true;
	}
}