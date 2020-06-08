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
package l2e.gameserver.model.entity.underground_coliseum;

import l2e.gameserver.model.L2Party;

public class UCWaiting
{
	private L2Party _party;
	private long _registerMillis;
	private final UCArena _baseArena;

	public UCWaiting(L2Party party, UCArena baseArena)
	{
		_party = party;
		_baseArena = baseArena;
	}

	public void clean()
	{
		_registerMillis = 0L;
		setParty(null);
	}

	public UCArena getBaseArena()
	{
		return _baseArena;
	}

	public L2Party getParty()
	{
		if(_party != null && _party.getLeader() == null)
			setParty(null);

		return _party;
	}

	public void setParty(L2Party party)
	{
		L2Party oldParty = _party;
		_party = party;

		if(_party != null)
			_party.setUCState(this);

		if(oldParty != null)
			oldParty.setUCState(null);
	}

	public void hasRegisterdNow()
	{
		_registerMillis = System.currentTimeMillis();
	}

	public long getRegisterMillis()
	{
		return _registerMillis;
	}
}