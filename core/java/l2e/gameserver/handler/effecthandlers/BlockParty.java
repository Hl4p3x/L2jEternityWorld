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

import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTask;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.model.stats.Env;

public final class BlockParty extends L2Effect
{
	public BlockParty(Env env, EffectTemplate template)
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
		if ((getEffected() == null) || !getEffected().isPlayer())
		{
			return false;
		}
		PunishmentManager.getInstance().startPunishment(new PunishmentTask(0, getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN, 0, "Party banned by bot report", "system", true));
		return true;
	}
	
	@Override
	public void onExit()
	{
		PunishmentManager.getInstance().stopPunishment(getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN);
	}
}