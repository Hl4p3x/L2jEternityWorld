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

import l2e.Config;
import l2e.gameserver.model.actor.L2Character;

public class MoveToPawn extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _targetId;
	private final int _distance;
	private final int _x, _y, _z, _tx, _ty, _tz;
	
	public MoveToPawn(L2Character cha, L2Character target, int distance)
	{
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x72);
		
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_distance);
		
		writeD(_x);
		writeD(_y);
		writeD(_z + Config.CLIENT_SHIFTZ);
		writeD(_tx);
		writeD(_ty);
		writeD(_tz + Config.CLIENT_SHIFTZ);
	}
}