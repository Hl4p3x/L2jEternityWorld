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

import l2e.gameserver.util.Point3D;

public class GetOnVehicle extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _boatObjId;
	private final Point3D _pos;
	
	public GetOnVehicle(int charObjId, int boatObjId, Point3D pos)
	{
		_charObjId = charObjId;
		_boatObjId = boatObjId;
		_pos = pos;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x6e);
		writeD(_charObjId);
		writeD(_boatObjId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());	
	}
}