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

import l2e.gameserver.model.L2Party;

public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{
	private final L2Party _party;
	private final int _mode, _LeaderOID, _memberCount;
	private final String _name;
	
	public ExMPCCPartyInfoUpdate(L2Party party, int mode)
	{
		_party = party;
		_name = _party.getLeader().getName();
		_LeaderOID = _party.getLeaderObjectId();
		_memberCount = _party.getMemberCount();
		_mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x5B);
		writeS(_name);
		writeD(_LeaderOID);
		writeD(_memberCount);
		writeD(_mode);
	}	
}