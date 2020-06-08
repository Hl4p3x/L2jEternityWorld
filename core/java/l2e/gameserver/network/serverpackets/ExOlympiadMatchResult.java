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

import l2e.gameserver.model.olympiad.OlympiadInfo;

public class ExOlympiadMatchResult extends L2GameServerPacket
{
	private final boolean _tie;
	private int _winTeam;
	private int _loseTeam = 2;
	private final List<OlympiadInfo> _winnerList;
	private final List<OlympiadInfo> _loserList;
	
	public ExOlympiadMatchResult(boolean tie, int winTeam, List<OlympiadInfo> winnerList, List<OlympiadInfo> loserList)
	{
		_tie = tie;
		_winTeam = winTeam;
		_winnerList = winnerList;
		_loserList = loserList;
		
		if (_winTeam == 2)
		{
			_loseTeam = 1;
		}
		else if (_winTeam == 0)
		{
			_winTeam = 1;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD4);
		writeD(0x01);
		
		writeD(_tie ? 1 : 0);
		writeS(_winnerList.get(0).getName());
		writeD(_winTeam);
		writeD(_winnerList.size());
		for (OlympiadInfo info : _winnerList)
		{
			writeS(info.getName());
			writeS(info.getClanName());
			writeD(info.getClanId());
			writeD(info.getClassId());
			writeD(info.getDamage());
			writeD(info.getCurrentPoints());
			writeD(info.getDiffPoints());
		}
		
		writeD(_loseTeam);
		writeD(_loserList.size());
		for (OlympiadInfo info : _loserList)
		{
			writeS(info.getName());
			writeS(info.getClanName());
			writeD(info.getClanId());
			writeD(info.getClassId());
			writeD(info.getDamage());
			writeD(info.getCurrentPoints());
			writeD(info.getDiffPoints());
		}
	}
}