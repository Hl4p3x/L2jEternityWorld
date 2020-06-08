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
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.model.SeedProduction;
import l2e.gameserver.model.entity.Castle;

public class ExShowSeedSetting extends L2GameServerPacket
{
	private final int _manorId;
	private final int _count;
	private final long[] _seedData;
	
	public ExShowSeedSetting(int manorId)
	{
		_manorId = manorId;
		Castle c = CastleManager.getInstance().getCastleById(_manorId);
		List<Integer> seeds = ManorParser.getInstance().getSeedsForCastle(_manorId);
		_count = seeds.size();
		_seedData = new long[_count * 12];
		int i = 0;
		for (int s : seeds)
		{
			_seedData[(i * 12) + 0] = s;
			_seedData[(i * 12) + 1] = ManorParser.getInstance().getSeedLevel(s);
			_seedData[(i * 12) + 2] = ManorParser.getInstance().getRewardItemBySeed(s, 1);
			_seedData[(i * 12) + 3] = ManorParser.getInstance().getRewardItemBySeed(s, 2);
			_seedData[(i * 12) + 4] = ManorParser.getInstance().getSeedSaleLimit(s);
			_seedData[(i * 12) + 5] = ManorParser.getInstance().getSeedBuyPrice(s);
			_seedData[(i * 12) + 6] = (ManorParser.getInstance().getSeedBasicPrice(s) * 60) / 100;
			_seedData[(i * 12) + 7] = ManorParser.getInstance().getSeedBasicPrice(s) * 10;
			SeedProduction seedPr = c.getSeed(s, CastleManorManager.PERIOD_CURRENT);
			if (seedPr != null)
			{
				_seedData[(i * 12) + 8] = seedPr.getStartProduce();
				_seedData[(i * 12) + 9] = seedPr.getPrice();
			}
			else
			{
				_seedData[(i * 12) + 8] = 0;
				_seedData[(i * 12) + 9] = 0;
			}
			seedPr = c.getSeed(s, CastleManorManager.PERIOD_NEXT);
			if (seedPr != null)
			{
				_seedData[(i * 12) + 10] = seedPr.getStartProduce();
				_seedData[(i * 12) + 11] = seedPr.getPrice();
			}
			else
			{
				_seedData[(i * 12) + 10] = 0;
				_seedData[(i * 12) + 11] = 0;
			}
			i++;
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x26);
		
		writeD(_manorId);
		writeD(_count);
		
		for (int i = 0; i < _count; i++)
		{
			writeD((int) _seedData[(i * 12) + 0]);
			writeD((int) _seedData[(i * 12) + 1]);
			writeC(1);
			writeD((int) _seedData[(i * 12) + 2]);
			writeC(1);
			writeD((int) _seedData[(i * 12) + 3]);
			
			writeD((int) _seedData[(i * 12) + 4]);
			writeD((int) _seedData[(i * 12) + 5]);
			writeD((int) _seedData[(i * 12) + 6]);
			writeD((int) _seedData[(i * 12) + 7]);
			
			writeQ(_seedData[(i * 12) + 8]);
			writeQ(_seedData[(i * 12) + 9]);
			writeQ(_seedData[(i * 12) + 10]);
			writeQ(_seedData[(i * 12) + 11]);
		}
	}
}