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

import java.util.List;

import javolution.util.FastList;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class FriendList extends L2GameServerPacket
{
	private final List<FriendInfo> _info;
	
	private static class FriendInfo
	{
		int _objId;
		String _name;
		boolean _online;
		
		public FriendInfo(int objId, String name, boolean online)
		{
			_objId = objId;
			_name = name;
			_online = online;
		}
	}
	
	public FriendList(L2PcInstance player)
	{
		_info = new FastList<>(player.getFriendList().size());
		for (int objId : player.getFriendList())
		{
			String name = CharNameHolder.getInstance().getNameById(objId);
			L2PcInstance player1 = L2World.getInstance().getPlayer(objId);
			boolean online = false;
			if ((player1 != null) && player1.isOnline())
			{
				online = true;
			}
			_info.add(new FriendInfo(objId, name, online));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x75);
		writeD(_info.size());
		for (FriendInfo info : _info)
		{
			writeD(info._objId);
			writeS(info._name);
			writeD(info._online ? 0x01 : 0x00);
			writeD(info._online ? info._objId : 0x00);
		}
	}
}