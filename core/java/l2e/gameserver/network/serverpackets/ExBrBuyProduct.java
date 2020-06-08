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

/**
 * Created by LordWinter 06.10.2011
 * Fixed by L2J Eternity-World
 */
public class ExBrBuyProduct extends L2GameServerPacket
{
	public static final int RESULT_OK 			= 1;
	public static final int RESULT_NOT_ENOUGH_POINTS 	= -1;
	public static final int RESULT_WRONG_PRODUCT 		= -2;
	public static final int RESULT_INVENTORY_FULL 		= -4;
	public static final int RESULT_SALE_PERIOD_ENDED 	= -7;
	public static final int RESULT_WRONG_USER_STATE 	= -9;
	public static final int RESULT_WRONG_PRODUCT_ITEM 	= -10;

	private final int _result;

	public ExBrBuyProduct(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD8);
		writeD(_result);
	}
}