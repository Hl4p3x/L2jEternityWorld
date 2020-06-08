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

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class WhiteAllosce extends L2AttackableAIScript
{
	private static final int ALLOSCE = 18577;
	private static final int GUARD = 18578;
	
	boolean _isLock = false;
	
	public WhiteAllosce(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(ALLOSCE);
		addKillId(ALLOSCE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		int x = player.getX();
		int y = player.getY();
		
		if (event.equalsIgnoreCase("time_to_spawn"))
		{
			addSpawn(GUARD, x + 100, y + 50, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			_isLock = false;
		}
		
		return "";
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon, L2Skill skill)
	{
		int npcId = npc.getId();
		
		if (npcId == ALLOSCE)
		{
			if (_isLock == false)
			{
				startQuestTimer("time_to_spawn", 40000, npc, player);
				_isLock = true;
			}
			else
			{
				return "";
			}
		}
		
		return "";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcId = npc.getId();
		
		if (npcId == ALLOSCE)
		{
			cancelQuestTimer("time_to_spawn", npc, player);
		}
		
		return "";
	}
	
	public static void main(String[] args)
	{
		new WhiteAllosce(-1, "WhiteAllosce", "ai");
	}
}