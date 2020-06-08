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

import l2e.gameserver.model.Macro;
import l2e.gameserver.model.MacroCmd;

public class SendMacroList extends L2GameServerPacket
{	
	private final int _rev;
	private final int _count;
	private final Macro _macro;
	
	public SendMacroList(int rev, int count, Macro macro)
	{
		_rev = rev;
		_count = count;
		_macro = macro;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE8);
		
		writeD(_rev);
		writeC(0x00);
		writeC(_count);
		writeC(_macro != null ? 1 : 0);
		
		if (_macro != null)
		{
			writeD(_macro.getId());
			writeS(_macro.getName());
			writeS(_macro.getDescr());
			writeS(_macro.getAcronym());
			writeC(_macro.getIcon());
			
			writeC(_macro.getCommands().size());
			
			int i = 1;
			for (MacroCmd cmd : _macro.getCommands())
			{
				writeC(i++);
				writeC(cmd.getType());
				writeD(cmd.getD1());
				writeC(cmd.getD2());
				writeS(cmd.getCmd());
			}
		}	
	}
}