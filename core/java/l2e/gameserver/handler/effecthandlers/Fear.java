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

import l2e.Config;
import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.L2DefenderInstance;
import l2e.gameserver.model.actor.instance.L2FortCommanderInstance;
import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2e.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2e.gameserver.model.effects.EffectFlag;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;

public class Fear extends L2Effect
{
	public static final int FEAR_RANGE = 500;
	
	private int _dX = -1;
	private int _dY = -1;
	
	public Fear(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.FEAR.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FEAR;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() instanceof L2NpcInstance) || (getEffected() instanceof L2DefenderInstance) || (getEffected() instanceof L2FortCommanderInstance) || (getEffected() instanceof L2SiegeFlagInstance) || (getEffected() instanceof L2SiegeSummonInstance))
		{
			return false;
		}
		
		if (getEffected().isAfraid())
		{
			return false;
		}
		
		if (getEffected().isCastingNow() && getEffected().canAbortCast())
		{
			getEffected().abortCast();
		}
		
		if (getEffected().getX() > getEffector().getX())
		{
			_dX = 1;
		}
		if (getEffected().getY() > getEffector().getY())
		{
			_dY = 1;
		}
		
		getEffected().getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		onActionTime();
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		int posX = getEffected().getX();
		int posY = getEffected().getY();
		int posZ = getEffected().getZ();
		
		if (getEffected().getX() > getEffector().getX())
		{
			_dX = 1;
		}
		
		if (getEffected().getY() > getEffector().getY())
		{
			_dY = 1;
		}
		
		posX += _dX * FEAR_RANGE;
		posY += _dY * FEAR_RANGE;
		
		if (Config.GEODATA)
		{
			Location destiny = GeoClient.getInstance().moveCheck(getEffected(), new Location(posX, posY, posZ), true);
			posX = destiny.getX();
			posY = destiny.getY();
		}
		
		if (!getEffected().isPet())
		{
			getEffected().setRunning();
		}
		
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(posX, posY, posZ));
		return false;
	}
}