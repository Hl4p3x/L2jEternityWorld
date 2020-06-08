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

import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PetItemList;

public class Restoration extends L2Effect
{
	private final int _itemId;
	private final int _itemCount;
	
	public Restoration(Env env, EffectTemplate template)
	{
		super(env, template);
		_itemId = template.getParameters().getInteger("itemId", 0);
		_itemCount = template.getParameters().getInteger("itemCount", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayable())
		{
			return false;
		}
		
		if ((_itemId <= 0) || (_itemCount <= 0))
		{
			getEffected().sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			_log.warning(Restoration.class.getSimpleName() + " effect with wrong item Id/count: " + _itemId + "/" + _itemCount + "!");
			return false;
		}
		
		if (getEffected().isPlayer())
		{
			getEffected().getActingPlayer().addItem("Skill", _itemId, _itemCount, getEffector(), true);
		}
		else if (getEffected().isPet())
		{
			getEffected().getInventory().addItem("Skill", _itemId, _itemCount, getEffected().getActingPlayer(), getEffector());
			getEffected().getActingPlayer().sendPacket(new PetItemList(getEffected().getInventory().getItems()));
		}
		return true;
	}
}