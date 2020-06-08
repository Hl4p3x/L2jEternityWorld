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

import l2e.gameserver.data.xml.ManorParser;
import l2e.gameserver.model.SeedProduction;

public class ExShowSeedInfo extends L2GameServerPacket
{
	private final List<SeedProduction> _seeds;
	private final int _manorId;
	
	public ExShowSeedInfo(int manorId, List<SeedProduction> seeds)
	{
		_manorId = manorId;
		_seeds = seeds;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x23);
		writeC(0x00);
		writeD(_manorId);
		writeD(0x00);
		if (_seeds == null)
		{
			writeD(0);
			return;
		}
		writeD(_seeds.size());
		for (SeedProduction seed : _seeds)
		{
			writeD(seed.getId());
			writeQ(seed.getCanProduce());
			writeQ(seed.getStartProduce());
			writeQ(seed.getPrice());
			writeD(ManorParser.getInstance().getSeedLevel(seed.getId()));
			writeC(0x01);
			writeD(ManorParser.getInstance().getRewardItemBySeed(seed.getId(), 1));
			writeC(0x01);
			writeD(ManorParser.getInstance().getRewardItemBySeed(seed.getId(), 2));
		}
	}
}