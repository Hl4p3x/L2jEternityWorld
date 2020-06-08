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
package l2e.gameserver.model.olympiad;

public class OlympiadInfo
{
	private final String _name;
	private final String _clan;
	private final int _clanId;
	private final int _classId;
	private final int _dmg;
	private final int _curPoints;
	private final int _diffPoints;
	
	public OlympiadInfo(String name, String clan, int clanId, int classId, int dmg, int curPoints, int diffPoints)
	{
		_name = name;
		_clan = clan;
		_clanId = clanId;
		_classId = classId;
		_dmg = dmg;
		_curPoints = curPoints;
		_diffPoints = diffPoints;
	}

	public String getName()
	{
		return _name;
	}

	public String getClanName()
	{
		return _clan;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getDamage()
	{
		return _dmg;
	}

	public int getCurrentPoints()
	{
		return _curPoints;
	}

	public int getDiffPoints()
	{
		return _diffPoints;
	}
}