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

import java.util.List;

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.util.StringUtil;

public class ShowBoard extends L2GameServerPacket
{
	private final StringBuilder _htmlCode;
	private String _addFav = "";
	
	public ShowBoard(String htmlCode, String id, L2PcInstance player)
	{
		if(player.getSessionVar("add_fav") != null)
		{
			_addFav = "bypass _bbsaddfav";
		}
		_htmlCode = StringUtil.startAppend(500, id, "\u0008", htmlCode);
	}
	
	public ShowBoard(List<String> arg)
	{
		_htmlCode = StringUtil.startAppend(500, "1002\u0008");
		for (String str : arg)
		{
			StringUtil.append(_htmlCode, str, " \u0008");
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7B);
		writeC(0x01);
		writeS("bypass _bbshome");
		writeS("bypass _bbsgetfav");
		writeS("bypass _bbsloc");
		writeS("bypass _bbsclan");
		writeS("bypass _bbsmemo");
		writeS("bypass _bbsmail");
		writeS("bypass _bbsfriends");
		writeS(_addFav);
		writeS(_htmlCode.toString());
	}
}