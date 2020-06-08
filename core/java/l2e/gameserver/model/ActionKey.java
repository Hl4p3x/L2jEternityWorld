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
package l2e.gameserver.model;

public class ActionKey
{
	private final int _cat;
	private int _cmd = 0;
	private int _key = 0;
	private int _tgKey1 = 0;
	private int _tgKey2 = 0;
	private int _show = 1;
	
	public ActionKey(int cat)
	{
		_cat = cat;
	}
	
	public ActionKey(int cat, int cmd, int key, int tgKey1, int tgKey2, int show)
	{
		_cat = cat;
		_cmd = cmd;
		_key = key;
		_tgKey1 = tgKey1;
		_tgKey2 = tgKey2;
		_show = show;
	}
	
	public int getCategory()
	{
		return _cat;
	}
	
	public int getCommandId()
	{
		return _cmd;
	}
	
	public void setCommandId(int cmd)
	{
		_cmd = cmd;
	}
	
	public int getKeyId()
	{
		return _key;
	}
	
	public void setKeyId(int key)
	{
		_key = key;
	}
	
	public int getToogleKey1()
	{
		return _tgKey1;
	}
	
	public void setToogleKey1(int tKey1)
	{
		_tgKey1 = tKey1;
	}
	
	public int getToogleKey2()
	{
		return _tgKey2;
	}
	
	public void setToogleKey2(int tKey2)
	{
		_tgKey2 = tKey2;
	}
	
	public int getShowStatus()
	{
		return _show;
	}
	
	public void setShowStatus(int show)
	{
		_show = show;
	}
	
	public String getSqlSaveString(int playerId, int order)
	{
		return "(" + playerId + ", " + _cat + ", " + order + ", " + _cmd + "," + _key + ", " + _tgKey1 + ", " + _tgKey2 + ", " + _show + ")";
	}
}