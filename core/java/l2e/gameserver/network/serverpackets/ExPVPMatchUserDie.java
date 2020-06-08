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

import l2e.gameserver.model.entity.underground_coliseum.UCArena;

public class ExPVPMatchUserDie extends L2GameServerPacket
{
	private final int _t1;
	private final int _t2;
	
	public ExPVPMatchUserDie(UCArena a)
	{
		_t1 = a.getTeams()[0].getKillCount();
		_t2 = a.getTeams()[1].getKillCount();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x7e);
		
		writeD(_t1);
		writeD(_t2);
	}
}