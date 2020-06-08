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

import java.util.ArrayList;

public class ExShowScreenMessage2 extends L2GameServerPacket
{
	private final boolean _hide;
	private final int _sysMessageId;
	private final int _time;
	private final ScreenMessageAlign _text_align;
	private final boolean _big_font;
	private final boolean _effect;
	private int _clientMessageId;
	private final ArrayList<String> _text = new ArrayList<>();
	
	public static enum ScreenMessageAlign
	{
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT;
	}
	
	public ExShowScreenMessage2(String text, int time, ScreenMessageAlign text_align, boolean big_font, boolean type, int messageId, boolean showEffect)
	{
		_hide = type;
		_sysMessageId = messageId;
		_text.add(text);
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
	}
	
	public ExShowScreenMessage2(int clientMsgId, int time, ScreenMessageAlign text_align, boolean big_font, boolean type, int messageId, boolean showEffect)
	{
		_hide = type;
		_sysMessageId = messageId;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
		_clientMessageId = clientMsgId;
	}
	
	public ExShowScreenMessage2 add(String text)
	{
		_text.add(text);
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(57);
		writeD(_hide ? 0 : 1);
		if (_hide)
		{
			return;
		}
		writeD(_sysMessageId);
		writeD(_text_align.ordinal() + 1);
		writeD(0);
		writeD(_big_font ? 0 : 1);
		writeD(0);
		writeD(0);
		writeD(_effect ? 1 : 0);
		writeD(_time);
		writeD(0);
		writeD(_clientMessageId);
		for (String text : _text)
		{
			writeS(text);
		}
	}
}