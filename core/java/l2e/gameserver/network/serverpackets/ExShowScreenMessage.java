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
import java.util.List;

import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;

public class ExShowScreenMessage extends L2GameServerPacket
{
	private final int _type;
	private final int _sysMessageId;
	private final int _unk1;
	private final int _unk2;
	private final int _unk3;
	private final boolean _fade;
	private final int _size;
	private final int _position;
	private boolean _effect;
	private final String _text;
	private final int _time;
	private final int _npcString;
	private List<String> _parameters = null;

	public static final byte TOP_LEFT = 0x01;
	public static final byte TOP_CENTER = 0x02;
	public static final byte TOP_RIGHT = 0x03;
	public static final byte MIDDLE_LEFT = 0x04;
	public static final byte MIDDLE_CENTER = 0x05;
	public static final byte MIDDLE_RIGHT = 0x06;
	public static final byte BOTTOM_CENTER = 0x07;
	public static final byte BOTTOM_RIGHT = 0x08;
	
	public ExShowScreenMessage(String text, int time)
	{
		_type = 2;
		_sysMessageId = -1;
		_unk1 = 0;
		_unk2 = 0;
		_unk3 = 0;
		_fade = false;
		_position = TOP_CENTER;
		_text = text;
		_time = time;
		_size = 0;
		_effect = false;
		_npcString = -1;
	}
	
	public ExShowScreenMessage(NpcStringId npcString, int position, int time, String... params)
	{
		_type = 2;
		_sysMessageId = -1;
		_unk1 = 0x00;
		_unk2 = 0x00;
		_unk3 = 0x00;
		_fade = false;
		_position = position;
		_text = null;
		_time = time;
		_size = 0x00;
		_effect = false;
		_npcString = npcString.getId();
		if (params != null)
		{
			addStringParameter(params);
		}
	}
	
	public ExShowScreenMessage(SystemMessageId systemMsg, int position, int time, String... params)
	{
		_type = 2;
		_sysMessageId = systemMsg.getId();
		_unk1 = 0x00;
		_unk2 = 0x00;
		_unk3 = 0x00;
		_fade = false;
		_position = position;
		_text = null;
		_time = time;
		_size = 0x00;
		_effect = false;
		_npcString = -1;
		if (params != null)
		{
			addStringParameter(params);
		}
	}

	public ExShowScreenMessage(int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, boolean fade, String text, NpcStringId npcString)
	{
		_type = type;
		_sysMessageId = messageId;
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_fade = fade;
		_position = position;
		_text = text;
		_time = time;
		_size = size;
		_effect = showEffect;
		_npcString = npcString.getId();
	}
	
	public ExShowScreenMessage(int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, boolean fade, String text, NpcStringId npcString, String params)
	{
		_type = type;
		_sysMessageId = messageId;
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_fade = fade;
		_position = position;
		_text = text;
		_time = time;
		_size = size;
		_effect = showEffect;
		_npcString = npcString.getId();
	}

	public ExShowScreenMessage(int type, int messageId, int position, int unk1, int size, int unk2, int unk3,boolean showEffect, int time,boolean fade, String text)
	{
		_type = type;
		_sysMessageId = messageId;
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_fade = fade;
		_position = position;
		_text = text;
		_time = time;
		_size = size;
		_effect = showEffect;
		_npcString = -1;
	}
	
	public void addStringParameter(String... params)
	{
		if (_parameters == null)
		{
			_parameters = new ArrayList<>();
		}
		for (String param : params)
		{
			_parameters.add(param);
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x39);
		writeD(_type);
		writeD(_sysMessageId);
		writeD(_position);
		writeD(_unk1);
		writeD(_size);
		writeD(_unk2);
		writeD(_unk3);
		writeD(_effect ? 0x01 : 0x00);
		writeD(_time);
		writeD(_fade ? 0x01 : 0x00);
		writeD(_npcString);
		if (_npcString == -1)
		{
			writeS(_text);
		}
		else
		{
			if (_parameters != null)
			{
				for (String s : _parameters)
					writeS(s);
			}
		}
	}

	public ExShowScreenMessage setUpperEffect(boolean value)
	{
		_effect = value;
		return this;
	}
}	