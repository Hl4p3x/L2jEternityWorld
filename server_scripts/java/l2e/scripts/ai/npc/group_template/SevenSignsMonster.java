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
package l2e.scripts.ai.npc.group_template;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Created by LordWinter 31.08.2013 Based on L2J Eternity-World
 */
public class SevenSignsMonster extends L2AttackableAIScript
{	
	private static final int[] SEVENSIGNS_MONSTERS =
	{
		21228,
		21183,
		21204,
		21161,
                21179,
                21248,
                21156,
                21220,
                21241,
                21240,
                21173,
                21194,
                21242,
                21243,
                21205,
                21184,
                21162,
                21253,
                21187,
                21208,
                21209,
                21166,
                21225,
                21224,
                21200,
                21214,
                21213,
                21144,
                21215,
                21158,
                21226,
                21202,
                21181,
                21217,
                21148,
                21174,
                21195,
                21204,
                21183,
                21161,
                21198,
                21222,
                21221     
	};
	
	public SevenSignsMonster(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int mobId : SEVENSIGNS_MONSTERS)
		{
			addSpawnId(mobId);
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (Util.contains(SEVENSIGNS_MONSTERS, npc.getId()))
		{
			npc.setSevenSignsMonster(true);
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new SevenSignsMonster(-1, SevenSignsMonster.class.getSimpleName(), "ai");
	}
}