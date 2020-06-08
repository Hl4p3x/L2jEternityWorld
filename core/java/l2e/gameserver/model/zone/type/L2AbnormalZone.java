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
package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.model.zone.L2ZoneType;

public class L2AbnormalZone extends L2ZoneType
{
	private int abnormal = AbnormalEffect.BIG_HEAD.getMask();
	private int special = 0;
	
	public L2AbnormalZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("AbnormalMask"))
		{
			abnormal = Integer.parseInt(value);
		}
		else if (name.equals("SpecialMask"))
		{
			special = Integer.parseInt(value);
		}
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.startAbnormalEffect(abnormal);
		character.startSpecialEffect(special);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.stopAbnormalEffect(abnormal);
		character.stopSpecialEffect(special);
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		onExit(character);
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		onEnter(character);
	}	
}