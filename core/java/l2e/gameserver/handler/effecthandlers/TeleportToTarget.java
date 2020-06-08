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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.FlyToLocation;
import l2e.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2e.gameserver.network.serverpackets.ValidateLocation;

public class TeleportToTarget extends L2Effect
{
	protected Location _flyLoc;
	
	public TeleportToTarget(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TELEPORT_TO_TARGET;
	}
	
	@Override
	public boolean calcSuccess()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() == null)
		{
			return false;
		}
		
		Location flyLoc = getEffector().getFlyLocation(getEffected(), getSkill());
		if (flyLoc != null)
		{
			_flyLoc = flyLoc;
			getEffector().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			getEffector().broadcastPacket(new FlyToLocation(getEffector(), flyLoc.getX(), flyLoc.getY(), flyLoc.getZ(), FlyType.DUMMY));
			getEffector().abortAttack();
			getEffector().abortCast();
			getEffector().setXYZ(flyLoc.getX(), flyLoc.getY(), flyLoc.getZ());
			getEffector().broadcastPacket(new ValidateLocation(getEffector()));
			getEffector().setHeading(getEffected().getHeading());
			getEffector().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(getEffected().getX(), getEffected().getY(), getEffected().getZ(), getEffected().getHeading()));
		}
		return true;
	}
}