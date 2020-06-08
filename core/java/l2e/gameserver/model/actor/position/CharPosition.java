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
package l2e.gameserver.model.actor.position;

import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.actor.L2Character;

public class CharPosition extends ObjectPosition
{
	public CharPosition(L2Character activeObject)
	{
		super(activeObject);
	}
	
	@Override
	protected void badCoords()
	{
		getActiveObject().decayMe();
	}
	
	@Override
	public final void setWorldRegion(L2WorldRegion value)
	{
		if(getWorldRegion() != null && getActiveObject() instanceof L2Character)
		{
			if (value != null)
				getWorldRegion().revalidateZones((L2Character)getActiveObject());
			else
				getWorldRegion().removeFromZones((L2Character)getActiveObject());
		}
		super.setWorldRegion(value);
	}
}