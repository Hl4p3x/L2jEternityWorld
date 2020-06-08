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
package l2e.gameserver.model.skills;

public enum L2SkillOpType
{
	A1,
	A2,
	A3,
	A4,
	CA1,
	CA5,
	DA1,
	DA2,
	P,
	T;
	
	public boolean isActive()
	{
		switch (this)
		{
			case A1:
			case A2:
			case A3:
			case A4:
			case CA1:
			case CA5:
			case DA1:
			case DA2:
				return true;
			default:
				return false;
		}
	}
	
	public boolean isPassive()
	{
		return (this == P);
	}
	
	public boolean isToggle()
	{
		return (this == T);
	}
}