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

import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.model.L2World;

public class FriendPacket extends L2GameServerPacket
{
	private final boolean _action, _online;
	private final int _objid;
	private final String _name;
	
	public FriendPacket(boolean action, int objId)
	{
		_action = action;
		_objid = objId;
		_name = CharNameHolder.getInstance().getNameById(objId);
		_online = L2World.getInstance().getPlayer(objId) != null;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x76);
		writeD(_action ? 1 : 3);
		writeD(_objid);
		writeS(_name);
		writeD(_online ? 1 : 0);
		writeD(_online ? _objid : 0);
	}
}