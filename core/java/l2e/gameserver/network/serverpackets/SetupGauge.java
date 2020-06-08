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

public final class SetupGauge extends L2GameServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;
	
	private final int _dat1;
	private final int _time;
	private final int _time2;
	private int _charObjId;
	
	public SetupGauge(int dat1, int time)
	{
		_dat1 = dat1;
		_time = time;
		_time2 = time;
	}
	
	public SetupGauge (int color, int currentTime, int maxTime)
	{
		_dat1 = color;
		_time = currentTime;
		_time2 = maxTime;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x6b);
		writeD(_charObjId);
		writeD(_dat1);
		writeD(_time);
		writeD(_time2);
	}

	@Override
	public void runImpl()
	{
		_charObjId = getClient().getActiveChar().getObjectId();
	}
}