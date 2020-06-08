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

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;

/**
 * Based on L2J Eternity-World
 */
public class Darnel extends L2AttackableAIScript
{
	private static final int DARNEL = 25531;

	public Darnel(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addKillId(DARNEL);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcId = npc.getId();

		if (npcId == DARNEL)
			addSpawn(32279, 152761, 145950, -12588, 0, false, 0, false, player.getInstanceId());

		return "";
	}

	public static void main(String[] args)
	{
		new Darnel(-1, "Darnel", "ai");
	}
}