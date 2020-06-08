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

public final class RestartResponse extends L2GameServerPacket
{
	private static final RestartResponse STATIC_PACKET_TRUE = new RestartResponse(true);
	private static final RestartResponse STATIC_PACKET_FALSE = new RestartResponse(false);
	
	public static final RestartResponse valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}
	
	private final boolean _result;
	
	public RestartResponse(boolean result)
	{
		_result = result;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x71);
		writeD(_result ? 1 : 0);
	}
}