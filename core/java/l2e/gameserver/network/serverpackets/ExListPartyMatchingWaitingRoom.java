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

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.model.PartyMatchWaitingList;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _minlvl;
	private final int _maxlvl;
	private final int _mode;
	private final List<L2PcInstance> _members;
	
	public ExListPartyMatchingWaitingRoom(L2PcInstance player, int page, int minlvl, int maxlvl, int mode)
	{
		_activeChar = player;
		_minlvl = minlvl;
		_maxlvl = maxlvl;
		_mode = mode;
		_members = new ArrayList<>();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x36);
		if (_mode == 0)
		{
			writeD(0);
			writeD(0);
			return;
		}
		
		for (L2PcInstance cha : PartyMatchWaitingList.getInstance().getPlayers())
		{
			if ((cha == null) || (cha == _activeChar))
			{
				continue;
			}
			
			if (!cha.isPartyWaiting())
			{
				PartyMatchWaitingList.getInstance().removePlayer(cha);
				continue;
			}
			
			else if ((cha.getLevel() < _minlvl) || (cha.getLevel() > _maxlvl))
			{
				continue;
			}
			
			_members.add(cha);
		}
		
		writeD(0x01);
		writeD(_members.size());
		for (L2PcInstance member : _members)
		{
			writeS(member.getName());
			writeD(member.getActiveClass());
			writeD(member.getLevel());
		}
	}
}