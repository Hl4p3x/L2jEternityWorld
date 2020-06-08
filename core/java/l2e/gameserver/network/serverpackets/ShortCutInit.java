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

import l2e.gameserver.model.L2ShortCut;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public final class ShortCutInit extends L2GameServerPacket
{
	private L2ShortCut[] _shortCuts;
	private L2PcInstance _activeChar;
	
	public ShortCutInit(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		
		if (_activeChar == null)
			return;
		
		_shortCuts = _activeChar.getAllShortCuts();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x45);
		writeD(_shortCuts.length);
		
		for (L2ShortCut sc: _shortCuts)
		{
			writeD(sc.getType());
			writeD(sc.getSlot() + sc.getPage() * 12);
			
			switch(sc.getType())
			{
				case L2ShortCut.TYPE_ITEM:
					writeD(sc.getId());
					writeD(0x01);
					writeD(sc.getSharedReuseGroup());
					writeD(0x00);
					writeD(0x00);
					writeH(0x00);
					writeH(0x00);
					break;
				case L2ShortCut.TYPE_SKILL:
					writeD(sc.getId());
					writeD(sc.getLevel());
					writeC(0x00);
					writeD(0x01);
					break;
				case L2ShortCut.TYPE_ACTION:
					writeD(sc.getId());
					writeD(0x01);
					break;
				case L2ShortCut.TYPE_MACRO:
					writeD(sc.getId());
					writeD(0x01);
					break;
				case L2ShortCut.TYPE_RECIPE:
					writeD(sc.getId());
					writeD(0x01);
					break;
				default:
					writeD(sc.getId());
					writeD(0x01);
			}
		}
	}
}