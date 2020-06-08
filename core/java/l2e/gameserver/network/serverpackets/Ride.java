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

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public final class Ride extends L2GameServerPacket
{
	private final int _objectId;
	private final int _mounted;
	private final int _rideType;
	private final int _rideNpcId;
	private final Location _loc;
	
	public Ride(L2PcInstance player)
	{
		_objectId = player.getObjectId();
		_mounted = player.isMounted() ? 1 : 0;
		_rideType = player.getMountType().ordinal();
		_rideNpcId = player.getMountNpcId() + 1000000;
		_loc = player.getLocation();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x8C);
		writeD(_objectId);
		writeD(_mounted);
		writeD(_rideType);
		writeD(_rideNpcId);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
	}
}