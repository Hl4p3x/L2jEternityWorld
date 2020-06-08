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

import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;

public class Escape extends L2Effect
{
	private final TeleportWhereType _escapeType;
	
	public Escape(Env env, EffectTemplate template)
	{
		super(env, template);
		_escapeType = template.getParameters().getEnum("escapeType", TeleportWhereType.class, null);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TELEPORT;
	}

	@Override
	public boolean calcSuccess()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (_escapeType == null)
		{
			return false;
		}
		getEffected().teleToLocation(MapRegionManager.getInstance().getTeleToLocation(getEffected(), _escapeType), true);
		return true;
	}
}