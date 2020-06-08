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
package l2e.gameserver.model.entity.mods.aio.main;

public class PlayersTopData
{
	private String _charName;
	private String _clanName;
	private int _pvp;
	private int _pk;
	private int _clanLevel;
	private int _onlineTime;
	
	public void setCharName(String charName)
	{
		_charName = charName;
	}
	
	public void setClanName(String clanName)
	{
		_clanName = clanName;
	}
	
	public void setPvp(int pvp)
	{
		_pvp = pvp;
	}
	
	public void setPk(int pk)
	{
		_pk = pk;
	}
	
	public void setClanLevel(int clanLevel)
	{
		_clanLevel = clanLevel;
	}
	
	public void setOnlineTime(int onlineTime)
	{
		_onlineTime = onlineTime;
	}
	
	public String getCharName()
	{
		return _charName;
	}
	
	public String getClanName()
	{
		return _clanName;
	}
	
	public int getPvp()
	{
		return _pvp;
	}
	
	public int getPk()
	{
		return _pk;
	}
	
	public int getClanLevel()
	{
		return _clanLevel;
	}
	
	public int getOnlineTime()
	{
		return _onlineTime;
	}
}
