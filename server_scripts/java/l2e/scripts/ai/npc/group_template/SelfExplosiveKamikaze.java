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

import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class SelfExplosiveKamikaze extends L2AttackableAIScript
{
	private static final Map<Integer, SkillsHolder> MONSTERS = new FastMap<>();
	
	static
	{
		MONSTERS.put(18817, new SkillsHolder(5376, 4));
		MONSTERS.put(18818, new SkillsHolder(5376, 4));
		MONSTERS.put(18821, new SkillsHolder(5376, 5));
		MONSTERS.put(21666, new SkillsHolder(4614, 3));
		MONSTERS.put(21689, new SkillsHolder(4614, 4));
		MONSTERS.put(21712, new SkillsHolder(4614, 5));
		MONSTERS.put(21735, new SkillsHolder(4614, 6));
		MONSTERS.put(21758, new SkillsHolder(4614, 7));
		MONSTERS.put(21781, new SkillsHolder(4614, 9));
	}
	
	public SelfExplosiveKamikaze(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int npcId : MONSTERS.keySet())
		{
			addAttackId(npcId);
			addSpellFinishedId(npcId);
		}
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon, L2Skill skil)
	{
		if (player != null)
		{
			if (MONSTERS.containsKey(npc.getId()) && !npc.isDead() && Util.checkIfInRange(MONSTERS.get(npc.getId()).getSkill().getAffectRange(), player, npc, true))
			{
				npc.doCast(MONSTERS.get(npc.getId()).getSkill());
			}
		}
		return super.onAttack(npc, player, damage, isSummon, skil);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (MONSTERS.containsKey(npc.getId()) && !npc.isDead() && ((skill.getId() == 4614) || (skill.getId() == 5376)))
		{
			npc.doDie(null);
		}
		
		return super.onSpellFinished(npc, player, skill);
	}
	
	public static void main(String[] args)
	{
		new SelfExplosiveKamikaze(-1, "SelfExplosiveKamikaze", "ai");
	}
}