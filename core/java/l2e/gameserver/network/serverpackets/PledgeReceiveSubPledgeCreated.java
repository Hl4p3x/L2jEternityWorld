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

import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Clan.SubPledge;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
	private final SubPledge _subPledge;
	private final L2Clan _clan;
	
	public PledgeReceiveSubPledgeCreated(SubPledge subPledge, L2Clan clan)
	{
		_subPledge = subPledge;
		_clan = clan;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x40);
		
		writeD(0x01);
		writeD(_subPledge.getId());
		writeS(_subPledge.getName());
		writeS(getLeaderName());
	}
	
	private String getLeaderName()
	{
		int LeaderId = _subPledge.getLeaderId();
		if ((_subPledge.getId() == L2Clan.SUBUNIT_ACADEMY) || (LeaderId == 0))
		{
			return "";
		}
		else if (_clan.getClanMember(LeaderId) == null)
		{
			_log.warning("SubPledgeLeader: " + LeaderId + " is missing from clan: " + _clan.getName() + "[" + _clan.getId() + "]");
			return "";
		}
		else
		{
			return _clan.getClanMember(LeaderId).getName();
		}
	}
}