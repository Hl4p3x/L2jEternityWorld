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
package l2e.protection.network.serverpackets;

import l2e.gameserver.network.serverpackets.L2GameServerPacket;

public final class ProtectionPacket extends L2GameServerPacket
{
	private final int _strId, _fontSize, _x, _y, _color;
	private final boolean _isDraw;
	private final String _text;
	
	public ProtectionPacket(int strId, boolean isDraw, int fontSize, int x, int y, int color, String text)
	{
		_strId = strId;
		_isDraw = isDraw;
		_fontSize = fontSize;
		_x = x;
		_y = y;
		_color = color;
		_text = text;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB0);
		writeC(_strId);
		writeC(_isDraw ? 1 : 0);
		writeC(_fontSize);
		writeD(_x);
		writeD(_y);
		writeD(_color);
		writeS(_text);
	}
}