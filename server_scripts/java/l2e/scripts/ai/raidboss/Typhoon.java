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

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2RaidBossInstance;
import l2e.gameserver.model.holders.SkillsHolder;

public class Typhoon extends AbstractNpcAI
{
	private static final int TYPHOON = 25539;
	
	private static SkillsHolder STORM = new SkillsHolder(5434, 1);
	
	private Typhoon(String name, String descr)
	{
		super(name, descr);
		
		addAggroRangeEnterId(TYPHOON);
		addSpawnId(TYPHOON);
		
		final L2RaidBossInstance boss = RaidBossSpawnManager.getInstance().getBosses().get(TYPHOON);

		if (HellboundManager.getInstance().getLevel() > 3 && boss != null)
			onSpawn(boss);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cast") && (npc != null) && !npc.isDead())
		{
			npc.doSimultaneousCast(STORM.getSkill());
			startQuestTimer("cast", 5000, npc, null);
		}
		return null;
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		npc.doSimultaneousCast(STORM.getSkill());
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
		{
			startQuestTimer("cast", 5000, npc, null);
		}
		
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Typhoon(Typhoon.class.getSimpleName(), "ai");
	}
}