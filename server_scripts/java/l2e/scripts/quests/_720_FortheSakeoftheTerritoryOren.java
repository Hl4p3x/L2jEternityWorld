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
package l2e.scripts.quests;

import l2e.gameserver.network.NpcStringId;

/**
 * Updated by LordWinter 03.10.2011
 * Based on L2J Eternity-World
 */
public class _720_FortheSakeoftheTerritoryOren extends TerritoryWarSuperClass
{
	public static String qn1 = "_720_FortheSakeoftheTerritoryOren";
	public static int qnu = 720;
	public static String qna = "";
	
	public _720_FortheSakeoftheTerritoryOren()
	{
		super(qnu,qn1,qna);
		CATAPULT_ID = 36502;
		TERRITORY_ID = 84;
		LEADER_IDS = new int[]{36526, 36528, 36531, 36594};
		GUARD_IDS = new int[]{36527, 36529, 36530};
		qn = qn1;
		npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_OREN_HAS_BEEN_DESTROYED};
		registerKillIds();
	}
}
