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

import java.util.logging.Level;

import l2e.Config;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public final class NpcHtmlMessage extends L2GameServerPacket
{
	private final int _npcObjId;
	private String _html;
	private int _itemId = 0;
	private boolean _validate = true;
	
	public NpcHtmlMessage(int npcObjId, int itemId)
	{
		_npcObjId = npcObjId;
		_itemId = itemId;
	}

	public NpcHtmlMessage(int npcObjId, int itemId, String text)
	{
		_npcObjId = npcObjId;
		_itemId = itemId;
		_html = text;
	}
	
	public NpcHtmlMessage(int npcObjId, String text)
	{
		_npcObjId = npcObjId;
		setHtml(text);
	}
	
	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}
	
	public void disableValidation()
	{
		_validate = false;
	}
	
	@Override
	public void runImpl()
	{
		if (Config.BYPASS_VALIDATION && _validate)
			buildBypassCache(getClient().getActiveChar());		
	}
	
	public void setHtml(String text)
	{
		if (text.length() > 17200)
		{
			_log.log(Level.WARNING, "Html is too long! this will crash the client!", new Throwable());
			_html = text.substring(0, 17200);
		}
		if (!text.contains("<html>"))
			text = "<html><body>" + text + "</body></html>";
		
		_html = text;
	}

	public boolean setFile(String prefix, String path)
	{
		String oriPath = path;
		if (prefix != null && !prefix.equalsIgnoreCase("en"))
		{
			if (path.contains("html/"))
				path = path.replace("html/", "html-" + prefix +"/");
		}
		String content = HtmCache.getInstance().getHtm(path);
		if (content == null && !oriPath.equals(path))
			content = HtmCache.getInstance().getHtm(oriPath);
		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>"+path+"</body></html>");
			_log.warning("missing html page "+path);
			return false;
		}
		setHtml(content);
		return true;
	}

	public boolean setFile(String path)
	{
		String content = HtmCache.getInstance().getHtm(path);
		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>"+path+"</body></html>");
			_log.warning("Missing html page: " + path);
			return false;
		}
		setHtml(content);
		return true;
	}

	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
	}

	public void replace(String pattern, long value)
	{
		replace(pattern, String.valueOf(value));
	}
	
	public void replace2(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}
	
	public boolean setEventHtml(String path)
	{
      		String content = HtmCache.getInstance().getHtm(path);

		if (content == null)
			return false;

       		setHtml(content);
        	return true;
	}

	private final void buildBypassCache(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;
		
		activeChar.clearBypass();
		int len = _html.length();
		for (int i = 0; i < len; i++)
		{
			int start = _html.indexOf("\"bypass ", i);
			int finish = _html.indexOf("\"", start + 1);
			if (start < 0 || finish < 0)
				break;
			
			if (_html.substring(start+8, start+10).equals("-h"))
				start += 11;
			else
				start += 8;
			
			i = finish;
			int finish2 = _html.indexOf("$", start);
			if (finish2 < finish && finish2 > 0)
				activeChar.addBypass2(_html.substring(start, finish2).trim());
			else
				activeChar.addBypass(_html.substring(start, finish).trim());
		}
	}

	public String getText()
	{
		return _html;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x19);
		
		writeD(_npcObjId);
		writeS(_html);
		if (_npcObjId != 0)
			writeD(_itemId);
	}

    	public String getHtm()
    	{
        	return _html;
    	}
}