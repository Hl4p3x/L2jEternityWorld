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
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class BrekaOrcOverlord extends L2AttackableAIScript
{
	private static final int BREKA = 20270;
	
	private static boolean _FirstAttacked;
	
	public BrekaOrcOverlord(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs =
		{
			BREKA
		};
		registerMobs(mobs, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL);
		_FirstAttacked = false;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == BREKA)
		{
			if (_FirstAttacked)
			{
				if (getRandom(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.S1_SHOW_YOUR_STRENGTH));
				}
				if (getRandom(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.IM_THE_STRONGEST_I_LOST_EVERYTHING_TO_WIN));
				}
				if (getRandom(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH));
				}
			}
			_FirstAttacked = true;
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();
		if (npcId == BREKA)
		{
			_FirstAttacked = false;
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new BrekaOrcOverlord(-1, "BrekaOrcOverlord", "ai");
	}
}