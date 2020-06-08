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
package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;

public class ValidateLocation extends L2GameServerPacket
{
	private final int _charObjId;
	private final Location _loc;
	
	public ValidateLocation(L2Object obj)
	{
		_charObjId = obj.getObjectId();
		_loc = obj.getLocation();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x79);
		writeD(_charObjId);
		writeLoc(_loc);
		writeD(_loc.getHeading());
	}
}