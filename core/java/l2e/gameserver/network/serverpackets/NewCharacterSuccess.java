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

import l2e.gameserver.model.actor.templates.L2PcTemplate;

public final class NewCharacterSuccess extends L2GameServerPacket
{
	private final List<L2PcTemplate> _chars = new ArrayList<>();
	
	public void addChar(L2PcTemplate template)
	{
		_chars.add(template);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0D);
		writeD(_chars.size());
		
		for (L2PcTemplate chr : _chars)
		{
			if (chr == null)
			{
				continue;
			}
			
			writeD(chr.getRace().ordinal());
			writeD(chr.getClassId().getId());
			writeD(0x46);
			writeD(chr.getBaseSTR());
			writeD(0x0A);
			writeD(0x46);
			writeD(chr.getBaseDEX());
			writeD(0x0A);
			writeD(0x46);
			writeD(chr.getBaseCON());
			writeD(0x0A);
			writeD(0x46);
			writeD(chr.getBaseINT());
			writeD(0x0A);
			writeD(0x46);
			writeD(chr.getBaseWIT());
			writeD(0x0A);
			writeD(0x46);
			writeD(chr.getBaseMEN());
			writeD(0x0A);
		}
	}
}