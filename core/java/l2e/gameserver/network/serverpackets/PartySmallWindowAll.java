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
import l2e.gameserver.model.actor.instance.L2PcInstance;

public final class PartySmallWindowAll extends L2GameServerPacket
{
	private final L2Party _party;
	private final L2PcInstance _exclude;
	private final int _dist, _LeaderOID;
	
	public PartySmallWindowAll(L2PcInstance exclude, L2Party party)
	{
		_exclude = exclude;
		_party = party;
		_LeaderOID = _party.getLeaderObjectId();
		_dist = _party.getLootDistribution();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4e);
		writeD(_LeaderOID);
		writeD(_dist);
		writeD(_party.getMemberCount() - 1);
		
		for (L2PcInstance member : _party.getMembers())
		{
			if ((member != null) && (member != _exclude))
			{
				writeD(member.getObjectId());
				writeS(member.getName());
				
				writeD((int) member.getCurrentCp());
				writeD(member.getMaxCp());
				
				writeD((int) member.getCurrentHp());
				writeD(member.getMaxHp());
				writeD((int) member.getCurrentMp());
				writeD(member.getMaxMp());
				writeD(member.getLevel());
				writeD(member.getClassId().getId());
				writeD(0x00);
				writeD(member.getRace().ordinal());
				writeD(0x00);
				writeD(0x00);
				if (member.hasSummon())
				{
					writeD(member.getSummon().getObjectId());
					writeD(member.getSummon().getId() + 1000000);
					writeD(member.getSummon().getSummonType());
					writeS(member.getSummon().getName());
					writeD((int) member.getSummon().getCurrentHp());
					writeD(member.getSummon().getMaxHp());
					writeD((int) member.getSummon().getCurrentMp());
					writeD(member.getSummon().getMaxMp());
					writeD(member.getSummon().getLevel());
				}
				else
				{
					writeD(0x00);
				}
			}
		}
	}
}