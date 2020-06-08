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

import javolution.util.FastMap;
import l2e.gameserver.data.xml.ManorParser;
import l2e.gameserver.model.CropProcure;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public class ExShowSellCropList extends L2GameServerPacket
{
	private int _manorId = 1;
	private final FastMap<Integer, L2ItemInstance> _cropsItems;
	private final FastMap<Integer, CropProcure> _castleCrops;
	
	public ExShowSellCropList(L2PcInstance player, int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_castleCrops = new FastMap<>();
		_cropsItems = new FastMap<>();
		
		List<Integer> allCrops = ManorParser.getInstance().getAllCrops();
		for (int cropId : allCrops)
		{
			L2ItemInstance item = player.getInventory().getItemByItemId(cropId);
			if (item != null)
			{
				_cropsItems.put(cropId, item);
			}
		}
		
		for (CropProcure crop : crops)
		{
			if (_cropsItems.containsKey(crop.getId()) && (crop.getAmount() > 0))
			{
				_castleCrops.put(crop.getId(), crop);
			}
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x2c);
		
		writeD(_manorId);
		writeD(_cropsItems.size());
		
		for (L2ItemInstance item : _cropsItems.values())
		{
			writeD(item.getObjectId());
			writeD(item.getDisplayId());
			writeD(ManorParser.getInstance().getSeedLevelByCrop(item.getId()));
			writeC(0x01);
			writeD(ManorParser.getInstance().getRewardItem(item.getId(), 1));
			writeC(0x01);
			writeD(ManorParser.getInstance().getRewardItem(item.getId(), 2));
			
			if (_castleCrops.containsKey(item.getId()))
			{
				CropProcure crop = _castleCrops.get(item.getId());
				writeD(_manorId);
				writeQ(crop.getAmount());
				writeQ(crop.getPrice());
				writeC(crop.getReward());
			}
			else
			{
				writeD(0xFFFFFFFF);
				writeQ(0x00);
				writeQ(0x00);
				writeC(0x00);
			}
			writeQ(item.getCount());
		}
	}
}