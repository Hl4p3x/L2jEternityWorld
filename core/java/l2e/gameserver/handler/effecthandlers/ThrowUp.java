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

import l2e.gameserver.model.Location;
import l2e.gameserver.model.effects.EffectFlag;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.FlyToLocation;
import l2e.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2e.gameserver.network.serverpackets.ValidateLocation;

public class ThrowUp extends L2Effect
{
	protected Location flyLoc;
	
	public ThrowUp(Env env, EffectTemplate template)
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
		flyLoc = getEffector().getFlyLocation(null, getSkill());
		if (flyLoc != null)
		{
			getEffected().setLoc(flyLoc);
			getEffected().broadcastPacket(new FlyToLocation(getEffected(), flyLoc.getX(), flyLoc.getY(), flyLoc.getZ(), FlyType.THROW_UP));
		}
		getEffected().startStunning();
		
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopStunning(false);
		getEffected().setXYZ(flyLoc.getX(), flyLoc.getY(), flyLoc.getZ());
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.STUNNED.getMask();
	}
}