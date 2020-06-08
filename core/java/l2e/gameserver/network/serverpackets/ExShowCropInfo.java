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
import l2e.gameserver.model.CropProcure;

public class ExShowCropInfo extends L2GameServerPacket
{
	private final List<CropProcure> _crops;
	private final int _manorId;
	
	public ExShowCropInfo(int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_crops = crops;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x24);
		writeC(0x00);
		writeD(_manorId);
		writeD(0x00);
		if (_crops == null)
		{
			writeD(0);
			return;
		}
		writeD(_crops.size());
		for (CropProcure crop : _crops)
		{
			writeD(crop.getId());
			writeQ(crop.getAmount());
			writeQ(crop.getStartAmount());
			writeQ(crop.getPrice());
			writeC(crop.getReward());
			writeD(ManorParser.getInstance().getSeedLevelByCrop(crop.getId()));
			writeC(0x01);
			writeD(ManorParser.getInstance().getRewardItem(crop.getId(), 1));
			writeC(0x01);
			writeD(ManorParser.getInstance().getRewardItem(crop.getId(), 2));
		}
	}
}