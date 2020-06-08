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
package l2e.scripts.ai.zone;

import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.scripts.ai.npc.AbstractNpcAI;

/**
 * Based on L2J Eternity-World
 */
public final class GiantsCave extends AbstractNpcAI
{
	private static final int[] SCOUTS =
	{
		22668,
		22669
	};
	
	private GiantsCave()
	{
		super(GiantsCave.class.getSimpleName(), "ai");
		
		addAttackId(SCOUTS);
		addAggroRangeEnterId(SCOUTS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("ATTACK") && (player != null) && (npc != null) && !npc.isDead())
		{
			if (npc.getId() == SCOUTS[0])
			{
				broadcastNpcSay(npc, Say2.NPC_SHOUT, NpcStringId._INTRUDER_DETECTED);
			}
			else
			{
				broadcastNpcSay(npc, Say2.NPC_SHOUT, NpcStringId.OH_GIANTS_AN_INTRUDER_HAS_BEEN_DISCOVERED);
			}
			
			for (L2Character characters : npc.getKnownList().getKnownCharactersInRadius(450))
			{
				if ((characters != null) && (characters.isL2Attackable()) && (getRandomBoolean()))
				{
					L2Attackable monster = (L2Attackable) characters;
					attackPlayer(monster, player);
				}
			}
		}
		else if (event.equals("CLEAR") && (npc != null) && !npc.isDead())
		{
			npc.setScriptValue(0);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			startQuestTimer("ATTACK", 6000, npc, attacker);
			startQuestTimer("CLEAR", 120000, npc, null);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			if (getRandomBoolean())
			{
				broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.YOU_GUYS_ARE_DETECTED);
			}
			else
			{
				broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.WHAT_KIND_OF_CREATURES_ARE_YOU);
			}
			startQuestTimer("ATTACK", 6000, npc, player);
			startQuestTimer("CLEAR", 120000, npc, null);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new GiantsCave();
	}
}