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

import l2e.Config;
import l2e.gameserver.model.L2ExtractableProductItem;
import l2e.gameserver.model.L2ExtractableSkill;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.util.Rnd;

public class RestorationRandom extends L2Effect
{
	public RestorationRandom(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null) || !getEffector().isPlayer() || !getEffected().isPlayer())
		{
			return false;
		}
		
		final L2ExtractableSkill exSkill = getSkill().getExtractableSkill();
		if (exSkill == null)
		{
			return false;
		}
		
		if (exSkill.getProductItems().isEmpty())
		{
			_log.warning("Extractable Skill with no data, probably wrong/empty table in Skill Id: " + getSkill().getId());
			return false;
		}
		
		final double rndNum = 100 * Rnd.nextDouble();
		double chance = 0;
		double chanceFrom = 0;
		final List<ItemsHolder> creationList = new ArrayList<>();
		
		for (L2ExtractableProductItem expi : exSkill.getProductItems())
		{
			chance = expi.getChance();
			if ((rndNum >= chanceFrom) && (rndNum <= (chance + chanceFrom)))
			{
				creationList.addAll(expi.getItems());
				break;
			}
			chanceFrom += chance;
		}
		
		final L2PcInstance player = getEffected().getActingPlayer();
		if (creationList.isEmpty())
		{
			player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			return false;
		}
		
		for (ItemsHolder item : creationList)
		{
			if ((item.getId() <= 0) || (item.getCount() <= 0))
			{
				continue;
			}
			player.addItem("Extract", item.getId(), (long) (item.getCount() * Config.RATE_EXTRACTABLE), getEffector(), true);
		}
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RESTORATION_RANDOM;
	}
}