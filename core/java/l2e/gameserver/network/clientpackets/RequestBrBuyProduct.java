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
package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.xml.ProductItemParser;
import l2e.gameserver.model.actor.instance.L2PcInstance;

/**
 * Created by LordWinter 06.10.2011 Fixed by L2J Eternity-World
 */
public class RequestBrBuyProduct extends L2GameClientPacket
{
	private int _productId;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_productId = readD();
		_count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		ProductItemParser.getInstance().requestBuyItem(player, _productId, _count);
	}
}