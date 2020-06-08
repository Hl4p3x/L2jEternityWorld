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

public class ExQuestNpcLogList extends L2GameServerPacket
{
	private final int _questId;
	private final List<NpcHolder> _npcs = new ArrayList<>();
	
	public ExQuestNpcLogList(int questId)
	{
		_questId = questId;
	}
	
	public void addNpc(int npcId, int count)
	{
		_npcs.add(new NpcHolder(npcId, 0, count));
	}
	
	public void addNpc(int npcId, int unknown, int count)
	{
		_npcs.add(new NpcHolder(npcId, unknown, count));
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xC5);
		writeD(_questId);
		writeC(_npcs.size());
		for (NpcHolder holder : _npcs)
		{
			writeD((holder.getNpcId() + 1000000));
			writeC(holder.getUnknown());
			writeD(holder.getCount());
		}
	}
	
	private class NpcHolder
	{
		private final int _npcId;
		private final int _unknown;
		private final int _count;
		
		public NpcHolder(int npcId, int unknown, int count)
		{
			_npcId = npcId;
			_unknown = unknown;
			_count = count;
		}
		
		public int getNpcId()
		{
			return _npcId;
		}
		
		public int getUnknown()
		{
			return _unknown;
		}
		
		public int getCount()
		{
			return _count;
		}
	}
}