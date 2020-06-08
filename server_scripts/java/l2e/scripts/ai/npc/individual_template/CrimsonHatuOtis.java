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
package l2e.scripts.ai.npc.individual_template;

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;

public final class CrimsonHatuOtis extends AbstractNpcAI
{
	private static final int CRIMSON_HATU_OTIS = 18558;

	private static SkillsHolder BOSS_SPINING_SLASH = new SkillsHolder(4737, 1);
	private static SkillsHolder BOSS_HASTE = new SkillsHolder(4175, 1);
	
	private CrimsonHatuOtis(String name, String descr)
	{
		super(name, descr);

		addAttackId(CRIMSON_HATU_OTIS);
		addKillId(CRIMSON_HATU_OTIS);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "skill":
				if (npc.isDead())
				{
					cancelQuestTimer("skill", npc, null);
					return null;
				}
				npc.setTarget(player);
				npc.doCast(BOSS_SPINING_SLASH.getSkill());
				startQuestTimer("skill", 60000, npc, null);
				break;
			case "buff":
				if (npc.isScriptValue(2))
				{
					npc.setTarget(npc);
					npc.doCast(BOSS_HASTE.getSkill());
				}
				break;
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			startQuestTimer("skill", 5000, npc, null);
		}
		else if (npc.isScriptValue(1) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.3)))
		{
			broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.IVE_HAD_IT_UP_TO_HERE_WITH_YOU_ILL_TAKE_CARE_OF_YOU);
			npc.setScriptValue(2);
			startQuestTimer("buff", 1000, npc, null);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		cancelQuestTimer("skill", npc, null);
		cancelQuestTimer("buff", npc, null);
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new CrimsonHatuOtis(CrimsonHatuOtis.class.getSimpleName(), "ai");
	}
}