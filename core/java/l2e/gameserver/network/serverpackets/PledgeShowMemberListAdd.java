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

import l2e.gameserver.model.L2ClanMember;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public final class PledgeShowMemberListAdd extends L2GameServerPacket
{
	private final String _name;
	private final int _lvl;
	private final int _classId;
	private final int _isOnline;
	private final int _pledgeType;
	
	public PledgeShowMemberListAdd(L2PcInstance player)
	{
		_name = player.getName();
		_lvl = player.getLevel();
		_classId = player.getClassId().getId();
		_isOnline = (player.isOnline() ? player.getObjectId() : 0);
		_pledgeType = player.getPledgeType();
	}
	
	public PledgeShowMemberListAdd(L2ClanMember cm)
	{
		_name = cm.getName();
		_lvl = cm.getLevel();
		_classId = cm.getClassId();
		_isOnline = (cm.isOnline() ? cm.getObjectId() : 0);
		_pledgeType = cm.getPledgeType();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x5c);
		writeS(_name);
		writeD(_lvl);
		writeD(_classId);
		writeD(0x00);
		writeD(0x01);
		writeD(_isOnline);
		writeD(_pledgeType);
	}	
}