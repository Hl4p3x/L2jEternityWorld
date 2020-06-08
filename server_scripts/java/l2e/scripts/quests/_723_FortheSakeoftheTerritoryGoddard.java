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
public class _723_FortheSakeoftheTerritoryGoddard extends TerritoryWarSuperClass
{
	public static String qn1 = "_723_FortheSakeoftheTerritoryGoddard";
	public static int qnu = 723;
	public static String qna = "";
	
	public _723_FortheSakeoftheTerritoryGoddard()
	{
		super(qnu,qn1,qna);
		CATAPULT_ID = 36505;
		TERRITORY_ID = 87;
		LEADER_IDS = new int[]{36544, 36546, 36549, 36597};
		GUARD_IDS = new int[]{36545, 36547, 36548};
		qn = qn1;
		npcString = new NpcStringId[]{NpcStringId.THE_CATAPULT_OF_GODDARD_HAS_BEEN_DESTROYED};
		registerKillIds();
	}
}
