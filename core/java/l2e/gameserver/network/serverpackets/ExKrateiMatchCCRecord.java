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

import l2e.gameserver.instancemanager.KrateisCubeManager;
import l2e.gameserver.instancemanager.KrateisCubeManager.CCPlayer;

public class ExKrateiMatchCCRecord extends L2GameServerPacket
{
	private final int	   _state;
	private final CCPlayer[]   _players;
	private String		   playername;
	private Integer 	   kills;
	private static String[]	   SCBnames;
	private static Integer[][] SCBkills;

	public ExKrateiMatchCCRecord(int state, CCPlayer[] players)
	{
		_state = state;
		_players = players;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x89);

		writeD(_state);
		writeD(_players.length);

		SCBnames = KrateisCubeManager.scoreboardnames;
		SCBkills = KrateisCubeManager.scoreboardkills;
		for (int i = 0; i <= 23; i++)
		{
			if (SCBkills[i][1] < SCBkills[i+1][1])
			{
				playername = SCBnames[i];
				SCBnames[i] = SCBnames[i+1];
				SCBnames[i+1] = playername;
				kills = SCBkills[i][1];
				SCBkills[i][1] = SCBkills[i+1][1];
				SCBkills[i+1][1] = kills;
				kills = SCBkills[i][0];
				SCBkills[i][0] = SCBkills[i+1][0];
				SCBkills[i+1][0] = kills;
				i=0;
			}
		}
		
		for (int i = 0; i <= 24; i++)
		{
			if (SCBkills[i][0] > 0)
			{
				playername = SCBnames[i];
				kills = SCBkills[i][1];

				writeS(playername);
				writeD(kills);
			}
		}
	}
}