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

import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.stats.Env;

public class Sweeper extends L2Effect
{
	private static final int MAX_SWEEPER_TIME = 15000;
	
	public Sweeper(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null) || !getEffector().isPlayer() || !getEffected().isL2Attackable())
		{
			return false;
		}
		
		final L2PcInstance player = getEffector().getActingPlayer();
		final L2Attackable monster = (L2Attackable) getEffected();
		if (!monster.checkSpoilOwner(player, false))
		{
			return false;
		}
		
		if (monster.isOldCorpse(player, MAX_SWEEPER_TIME, false))
		{
			return false;
		}
		
		if (!player.getInventory().checkInventorySlotsAndWeight(monster.getSpoilLootItems(), false, false))
		{
			return false;
		}
		
		ItemsHolder[] items = monster.takeSweep();
		if ((items == null) || (items.length == 0))
		{
			return false;
		}
		
		for (ItemsHolder item : items)
		{
			if (player.isInParty())
			{
				player.getParty().distributeItem(player, item, true, monster);
			}
			else
			{
				player.addItem("Sweeper", item, getEffected(), true);
			}
		}
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
}