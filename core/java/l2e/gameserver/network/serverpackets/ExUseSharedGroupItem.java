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

public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private final int _itemId;
	private final int _grpId;
	private final int _remainingTime;
	private final int _totalTime;
	
	public ExUseSharedGroupItem(int itemId, int grpId, long remainingTime, int totalTime)
	{
		_itemId = itemId;
		_grpId = grpId;
		_remainingTime = (int) (remainingTime / 1000);
		_totalTime = totalTime / 1000;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x4A);
		
		writeD(_itemId);
		writeD(_grpId);
		writeD(_remainingTime);
		writeD(_totalTime);
	}
}