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

import java.util.Collection;

import l2e.gameserver.data.xml.ProductItemParser;
import l2e.gameserver.model.L2ProductItem;

/**
 * Created by LordWinter 06.10.2011 Fixed by L2J Eternity-World
 */
public class ExBrProductList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD6);
		Collection<L2ProductItem> items = ProductItemParser.getInstance().getAllItems();
		writeD(items.size());
		
		for (L2ProductItem template : items)
		{
			if (System.currentTimeMillis() < template.getStartTimeSale())
			{
				continue;
			}
			
			if (System.currentTimeMillis() > template.getEndTimeSale())
			{
				continue;
			}
			
			writeD(template.getProductId());
			writeH(template.getCategory());
			writeD(template.getPoints());
			writeD(template.getTabId());
			writeD((int) (template.getStartTimeSale() / 1000));
			writeD((int) (template.getEndTimeSale() / 1000));
			writeC(127);
			writeC(template.getStartHour());
			writeC(template.getStartMin());
			writeC(template.getEndHour());
			writeC(template.getEndMin());
			writeD(0);
			writeD(-1);
		}
	}
}