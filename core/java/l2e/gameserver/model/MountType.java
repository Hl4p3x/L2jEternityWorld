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

import l2e.gameserver.data.xml.CategoryParser;

public enum MountType
{
	NONE,
	STRIDER,
	WYVERN,
	WOLF;
	
	public static MountType findByNpcId(int npcId)
	{
		if (CategoryParser.getInstance().isInCategory(CategoryType.STRIDER, npcId))
		{
			return STRIDER;
		}
		else if (CategoryParser.getInstance().isInCategory(CategoryType.WYVERN_GROUP, npcId))
		{
			return WYVERN;
		}
		else if (CategoryParser.getInstance().isInCategory(CategoryType.WOLF_GROUP, npcId))
		{
			return WOLF;
		}
		return NONE;
	}
}