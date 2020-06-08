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
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.ZoneId;

public class L2TownZone extends L2ZoneType
{
	private int _townId;
	private int _taxById;
	private boolean _isTWZone = false;
	
	public L2TownZone(int id)
	{
		super(id);
		
		_taxById = 0;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("townId"))
		{
			_townId = Integer.parseInt(value);
		}
		else if (name.equals("taxById"))
		{
			_taxById = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (_isTWZone)
		{
			character.setInTownWarEvent(true);
		}
		
		character.setInsideZone(ZoneId.TOWN, true);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (_isTWZone)
		{
			character.setInTownWarEvent(false);
		}
		
		character.setInsideZone(ZoneId.TOWN, false);
	}
	
	public void onUpdate(L2Character character)
	{
		if (_isTWZone)
		{
			character.setInTownWarEvent(true);
		}
		else
		{
			character.setInTownWarEvent(false);
		}
	}
	
	public void updateForCharactersInside()
	{
		for (L2Character character : _characterList.values())
		{
			if (character != null)
			{
				onEnter(character);
			}
			onUpdate(character);
		}
	}
	
	public int getTownId()
	{
		return _townId;
	}
	
	public final int getTaxById()
	{
		return _taxById;
	}
	
	public final void setIsTWZone(boolean value)
	{
		_isTWZone = value;
	}
}