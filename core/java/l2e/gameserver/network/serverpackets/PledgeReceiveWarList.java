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

import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.model.L2Clan;

public class PledgeReceiveWarList extends L2GameServerPacket
{
	private final L2Clan _clan;
	private final int _tab;
	
	public PledgeReceiveWarList(L2Clan clan, int tab)
	{
		_clan = clan;
		_tab = tab;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3f);
		
		writeD(_tab);
		writeD(0x00);
		writeD(_tab == 0 ? _clan.getWarList().size() : _clan.getAttackerList().size());
		for (Integer i : _tab == 0 ? _clan.getWarList() : _clan.getAttackerList())
		{
			L2Clan clan = ClanHolder.getInstance().getClan(i);
			if (clan == null)
			{
				continue;
			}
			
			writeS(clan.getName());
			writeD(_tab);
			writeD(_tab);
		}
	}
}