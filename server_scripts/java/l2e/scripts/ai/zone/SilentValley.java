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

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.scripts.ai.npc.AbstractNpcAI;

/**
 * Based on L2J Eternity-World
 */
public class SilentValley extends AbstractNpcAI
{
	private static final SkillsHolder BETRAYAL = new SkillsHolder(6033, 1);
	private static final SkillsHolder BLAZE = new SkillsHolder(4157, 10);

	private static final int SACK = 13799;

	private static final int SPAWN_CHANCE = 2;
	private static final int CHEST_DIE_CHANCE = 5;

	private static final int CHEST = 18693;
	private static final int GUARD1 = 18694;
	private static final int GUARD2 = 18695;
	private static final int[] MOBS =
	{
		20965,
		20966,
		20967,
		20968,
		20969,
		20970,
		20971,
		20972,
		20973
	};
	
	private SilentValley()
	{
		super(SilentValley.class.getSimpleName(), "ai");
		
		addAttackId(MOBS);
		addAttackId(CHEST, GUARD1, GUARD2);
		addEventReceivedId(GUARD1, GUARD2);
		addKillId(MOBS);
		addSeeCreatureId(GUARD1, GUARD2);
		addSpawnId(CHEST, GUARD2);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ((npc != null) && !npc.isDead())
		{
			switch (event)
			{
				case "CLEAR":
					npc.doDie(null);
					break;
				case "CLEAR_EVENT":
					npc.broadcastEvent("CLEAR_ALL_INSTANT", 2000, null);
					npc.doDie(null);
					break;
				case "SPAWN_CHEST":
					addSpawn(CHEST, npc.getX() - 100, npc.getY(), npc.getZ() - 100, 0, false, 0);
					break;
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		switch (npc.getId())
		{
			case CHEST:
			{
				if (!isSummon && npc.isScriptValue(0))
				{
					npc.setScriptValue(1);
					broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.YOU_WILL_BE_CURSED_FOR_SEEKING_THE_TREASURE);
					npc.setTarget(player);
					npc.doCast(BETRAYAL.getSkill());
				}
				else if (isSummon || (getRandom(100) < CHEST_DIE_CHANCE))
				{
					((L2Attackable) npc).dropItem(player, SACK, 1);
					npc.broadcastEvent("CLEAR_ALL", 2000, null);
					npc.doDie(null);
					cancelQuestTimer("CLEAR_EVENT", npc, null);
				}
				break;
			}
			case GUARD1:
			case GUARD2:
			{
				npc.setTarget(player);
				npc.doCast(BLAZE.getSkill());
				attackPlayer((L2Attackable) npc, player);
				break;
			}
			default:
			{
				if (isSummon)
				{
					attackPlayer((L2Attackable) npc, player);
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (getRandom(1000) < SPAWN_CHANCE)
		{
			final int newZ = npc.getZ() + 100;
			addSpawn(GUARD2, npc.getX() + 100, npc.getY(), newZ, 0, false, 0);
			addSpawn(GUARD1, npc.getX() - 100, npc.getY(), newZ, 0, false, 0);
			addSpawn(GUARD1, npc.getX(), npc.getY() + 100, newZ, 0, false, 0);
			addSpawn(GUARD1, npc.getX(), npc.getY() - 100, newZ, 0, false, 0);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (creature.isPlayable())
		{
			final L2PcInstance player = (isSummon) ? ((L2Summon) creature).getOwner() : creature.getActingPlayer();
			if ((npc.getId() == GUARD1) || (npc.getId() == GUARD2))
			{
				npc.setTarget(player);
				npc.doCast(BLAZE.getSkill());
				attackPlayer((L2Attackable) npc, player);
			}
			else if (creature.getFirstEffect(BETRAYAL.getSkillId()) == null)
			{
				attackPlayer((L2Attackable) npc, player);
			}
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == CHEST)
		{
			npc.setIsInvul(true);
			startQuestTimer("CLEAR_EVENT", 300000, npc, null);
		}
		else
		{
			startQuestTimer("SPAWN_CHEST", 10000, npc, null);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		if ((receiver != null) && !receiver.isDead())
		{
			switch (eventName)
			{
				case "CLEAR_ALL":
					startQuestTimer("CLEAR", 60000, receiver, null);
					break;
				case "CLEAR_ALL_INSTANT":
					receiver.doDie(null);
					break;
			}
		}
		return super.onEventReceived(eventName, sender, receiver, reference);
	}
	
	public static void main(String[] args)
	{
		new SilentValley();
	}
}