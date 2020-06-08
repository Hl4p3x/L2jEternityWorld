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
package l2e.scripts.ai.raidboss;

import l2e.scripts.ai.L2AttackableAIScript;

import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;

/**
 * Based on L2J Eternity-World
 */
public class Barakiel extends L2AttackableAIScript
{
	// Barakiel NpcID
	private static final int BARAKIEL = 25325;

	// Barakiel Z coords
	private static final int x1 = 89800;
	private static final int x2 = 93200;
	private static final int y1 = -87038;

	public Barakiel (int questId, String name, String descr)
	{
		super(questId,name,descr);

		int[] mobs = {BARAKIEL};
		registerMobs(mobs);
	}

	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		int npcId = npc.getId();

		if (npcId == BARAKIEL)
		{
			int x = npc.getX();
			int y = npc.getY();
			if (x < x1 || x > x2 || y < y1)
			{
				npc.teleToLocation(91008,-85904,-2736);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	public static void main(String[] args)
	{
		new Barakiel(-1, "Barakiel", "ai");
	}
}