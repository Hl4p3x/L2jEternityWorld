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
public class _734_Piercethroughashield extends TerritoryWarSuperClass
{
	public static String qn1 = "_734_Piercethroughashield";
	public static int qnu = 734;
	public static String qna = "";
	
	public _734_Piercethroughashield()
	{
		super(qnu,qn1,qna);
		CLASS_IDS = new int[] { 6, 91, 5, 90, 20, 99, 33, 106 };
		qn = qn1;
		RANDOM_MIN = 10;
		RANDOM_MAX = 15;
		npcString = new NpcStringId[]{NpcStringId.YOU_HAVE_DEFEATED_S2_OF_S1_KNIGHTS,NpcStringId.YOU_WEAKENED_THE_ENEMYS_DEFENSE};
	}
}
