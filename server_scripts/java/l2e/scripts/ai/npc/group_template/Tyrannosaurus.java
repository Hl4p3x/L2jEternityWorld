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

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.scripts.ai.L2AttackableAIScript;
import gnu.trove.map.hash.TIntObjectHashMap;

public class Tyrannosaurus extends L2AttackableAIScript
{
	private final int[] TREX =
	{
		22215,
		22216,
		22217
	};
	
	private final TIntObjectHashMap<int[]> SKILLS_HP = new TIntObjectHashMap<>();
	
	public Tyrannosaurus(int id, String name, String descr)
	{
		super(id, name, descr);
		
		SKILLS_HP.put(3626, new int[]
		{
			65,
			100
		});
		SKILLS_HP.put(3627, new int[]
		{
			25,
			65
		});
		SKILLS_HP.put(3628, new int[]
		{
			0,
			25
		});
		for (final int npcId : TREX)
		{
			addSkillSeeId(npcId);
		}
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		boolean b = false;
		for (final L2Object trg : targets)
		{
			if (trg == npc)
			{
				b = true;
			}
		}
		if (!b)
		{
			return super.onSkillSee(npc, player, skill, targets, isSummon);
		}
		final int skillId = skill.getId();
		final int trexHp = (int) npc.getCurrentHp();
		final int trexMaxHp = npc.getMaxHp();
		if ((skillId >= 3626) && (skillId <= 3628))
		{
			final int minHp = ((SKILLS_HP.get(skillId))[0] * trexMaxHp) / 100;
			final int maxHp = ((SKILLS_HP.get(skillId))[1] * trexMaxHp) / 100;
			if ((trexHp < minHp) || (trexHp > maxHp))
			{
				npc.stopSkillEffects(skillId);
				player.sendMessage("The conditions are not right to use this skill now.");
			}
		}
		return super.onSkillSee(npc, player, skill, targets, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Tyrannosaurus(-1, "Tyrannosaurus", "ai");
	}
}