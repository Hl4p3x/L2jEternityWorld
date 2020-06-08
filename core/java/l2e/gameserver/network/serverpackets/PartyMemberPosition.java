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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.position.PcPosition;

public class PartyMemberPosition extends L2GameServerPacket
{
	private final Map<Integer, PcPosition> locations = new HashMap<>();
	
	public PartyMemberPosition(L2Party party)
	{
		reuse(party);
	}
	
	public void reuse(L2Party party)
	{
		locations.clear();
		for (L2PcInstance member : party.getMembers())
		{
			if (member == null)
				continue;
			locations.put(member.getObjectId(), member.getPosition());
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xba);
		writeD(locations.size());
		for (Map.Entry<Integer, PcPosition> entry : locations.entrySet())
		{
			PcPosition loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
}