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

import java.util.Map;

public class ExGetBossRecord extends L2GameServerPacket
{
	private final Map<Integer, Integer> _bossRecordInfo;
	private final int _ranking;
	private final int _totalPoints;
	
	public ExGetBossRecord(int ranking, int totalScore, Map<Integer, Integer> list)
	{
		_ranking = ranking;
		_totalPoints = totalScore;
		_bossRecordInfo = list;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x34);
		writeD(_ranking);
		writeD(_totalPoints);
		if (_bossRecordInfo == null)
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		else
		{
			writeD(_bossRecordInfo.size());
			for (int bossId : _bossRecordInfo.keySet())
			{
				writeD(bossId);
				writeD(_bossRecordInfo.get(bossId));
				writeD(0x00);
			}
		}
	}
}