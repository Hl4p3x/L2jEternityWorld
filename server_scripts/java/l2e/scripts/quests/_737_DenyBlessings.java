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
public class _737_DenyBlessings extends TerritoryWarSuperClass
{
	public static String qn1 = "_737_DenyBlessings";
	public static int qnu = 737;
	public static String qna = "";
	
	public _737_DenyBlessings()
	{
		super(qnu,qn1,qna);
		CLASS_IDS = new int[] { 43, 112, 30, 105, 16, 97, 17, 98, 52, 116 };
		qn = qn1;
		RANDOM_MIN = 3;
		RANDOM_MAX = 8;
		npcString = new NpcStringId[]{NpcStringId.YOU_HAVE_DEFEATED_S2_OF_S1_HEALERS_AND_BUFFERS,NpcStringId.YOU_WEAKENED_THE_ENEMYS_ATTACK};
	}
}
