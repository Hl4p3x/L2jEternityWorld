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
import l2e.gameserver.model.L2ClanMember;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private final L2Clan _clan;
	private final L2PcInstance _activeChar;
	private final L2ClanMember[] _members;
	private int _pledgeType;
	
	public PledgeShowMemberListAll(L2Clan clan, L2PcInstance activeChar)
	{
		_clan = clan;
		_activeChar = activeChar;
		_members = _clan.getMembers();
	}
	
	@Override
	protected final void writeImpl()
	{
		_pledgeType = 0;
		writePledge(0);
		
		for (SubPledge subPledge : _clan.getAllSubPledges())
		{
			_activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge, _clan));
		}
		
		for (L2ClanMember m : _members)
		{
			if (m.getPledgeType() == 0)
			{
				continue;
			}
			_activeChar.sendPacket(new PledgeShowMemberListAdd(m));
		}
		_activeChar.sendPacket(new UserInfo(_activeChar));
		_activeChar.sendPacket(new ExBrExtraUserInfo(_activeChar));
		
	}
	
	void writePledge(int mainOrSubpledge)
	{
		writeC(0x5a);
		
		writeD(mainOrSubpledge);
		writeD(_clan.getId());
		writeD(_pledgeType);
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());
		
		writeD(_clan.getCrestId());
		writeD(_clan.getLevel());
		writeD(_clan.getCastleId());
		writeD(_clan.getHideoutId());
		writeD(_clan.getFortId());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0x00);
		writeD(0x00);
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar() ? 1 : 0);
		writeD(0x00);
		writeD(_clan.getSubPledgeMembersCount(_pledgeType));
		
		for (L2ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());
			L2PcInstance player;
			if ((player = m.getPlayerInstance()) != null)
			{
				writeD(player.getAppearance().getSex() ? 1 : 0);
				writeD(player.getRace().ordinal());
			}
			else
			{
				writeD(0x01);
				writeD(0x01);
			}
			writeD(m.isOnline() ? m.getObjectId() : 0);
			writeD(m.getSponsor() != 0 ? 1 : 0);
		}
	}
}