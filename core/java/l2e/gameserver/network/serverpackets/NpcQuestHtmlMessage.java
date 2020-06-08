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

import l2e.Config;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public final class NpcQuestHtmlMessage extends L2GameServerPacket
{
	private final int _npcObjId;
	private String _html;
	private int _questId = 0;
	
	public NpcQuestHtmlMessage(int npcObjId, int questId)
	{
		_npcObjId = npcObjId;
		_questId = questId;
	}
	
	@Override
	public void runImpl()
	{
		if (Config.BYPASS_VALIDATION)
			buildBypassCache(getClient().getActiveChar());
	}
	
	public void setHtml(String text)
	{
		if(!text.contains("<html>"))
			text = "<html><body>" + text + "</body></html>";

		_html = text;
	}
	
	public boolean setFile(String path)
	{
		String content = HtmCache.getInstance().getHtm(getClient().getActiveChar().getLang(), path);
		
		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>"+path+"</body></html>");
			_log.warning("missing html page "+path);
			return false;
		}
		
		setHtml(content);
		return true;
	}
	
	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}
	
	private final void buildBypassCache(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;
		
		activeChar.clearBypass();
		int len = _html.length();
		for(int i=0; i<len; i++)
		{
			int start = _html.indexOf("bypass -h", i);
			int finish = _html.indexOf("\"", start);
			
			if(start < 0 || finish < 0)
				break;
			
			start += 10;
			i = finish;
			int finish2 = _html.indexOf("$",start);
			if (finish2 < finish && finish2 > 0)
				activeChar.addBypass2(_html.substring(start, finish2).trim());
			else
				activeChar.addBypass(_html.substring(start, finish).trim());
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x8D);
		writeD(_npcObjId);
		writeS(_html);
		writeD(_questId);
	}
}