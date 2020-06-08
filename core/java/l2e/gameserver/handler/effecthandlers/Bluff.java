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

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.StartRotation;
import l2e.gameserver.network.serverpackets.StopRotation;

public class Bluff extends L2Effect
{
	public Bluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2NpcInstance)
		{
			return false;
		}
		
		if ((getEffected() instanceof L2Npc) && (((L2Npc) getEffected()).getId() == 35062))
		{
			return false;
		}
		
		if (getEffected() instanceof L2SiegeSummonInstance)
		{
			return false;
		}
		
		getEffected().broadcastPacket(new StartRotation(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new StopRotation(getEffected().getObjectId(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		return true;
	}
}