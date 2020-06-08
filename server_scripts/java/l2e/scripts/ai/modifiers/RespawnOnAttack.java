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
package l2e.scripts.ai.modifiers;

import javolution.util.FastMap;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;
import l2e.util.Rnd;

/**
 * Based on L2J Eternity-World
 */
public class RespawnOnAttack extends L2AttackableAIScript
{
	private final int[] Mobs =
	{
		20832,
		20836,
		20833,
		20835,
		20840,
		20218,
		20841,
		20842,
		20843,
		20844,
		20845,
		20846,
		20847,
		21612
	};
	private final FastMap<Integer, Integer> MobEvolves = new FastMap<>();
	
	public RespawnOnAttack(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		this.registerMobs(new int[]
		{
			20832,
			20836,
			20833,
			20835,
			20840,
			20218,
			20841,
			20842,
			20843,
			20845,
			20846,
			20847,
			21612,
			21604,
			21605,
			21607,
			21610,
			21613,
			21616,
			21619,
			21622,
			21628,
			21631,
			21634,
			21637
		});
		MobEvolves.put(21607, 20833);
		MobEvolves.put(21604, 20832);
		MobEvolves.put(21605, 20843);
		MobEvolves.put(21610, 20835);
		MobEvolves.put(21616, 20840);
		MobEvolves.put(21619, 20841);
		MobEvolves.put(21622, 20842);
		MobEvolves.put(21628, 20844);
		MobEvolves.put(21631, 20845);
		MobEvolves.put(21634, 20846);
		MobEvolves.put(21637, 20847);
		MobEvolves.put(21613, 20839);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon, L2Skill skill)
	{
		int maxHp = npc.getMaxHp();
		int nowHp = (int) npc.getStatus().getCurrentHp();
		if (MobEvolves.containsKey(npc.getId()))
		{
			if ((nowHp < (maxHp * 0.78)) && (nowHp > (maxHp * 0.72)) && Rnd.getChance(10))
			{
				int evolve = MobEvolves.get(npc.getId());
				npc.decayMe();
				L2Npc newNpc = addSpawn(evolve, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false);
				newNpc.setRunning();
				((L2Attackable) newNpc).addDamageHate(player, 0, 500);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		else if (Util.contains(Mobs, npc.getId()))
		{
			if ((nowHp < (maxHp * 0.80)) && (nowHp > (maxHp * 0.70)) && Rnd.getChance(25))
			{
				npc.decayMe();
				L2Npc newNpc = addSpawn(npc.getId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false);
				newNpc.setRunning();
				((L2Attackable) newNpc).addDamageHate(player, 0, 500);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return super.onAttack(npc, player, damage, isSummon, skill);
	}
	
	public static void main(String[] args)
	{
		new RespawnOnAttack(-1, "RespawnOnAttack", "modifiers");
	}
}