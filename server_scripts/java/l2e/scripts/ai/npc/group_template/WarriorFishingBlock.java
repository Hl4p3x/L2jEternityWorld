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

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;

public class WarriorFishingBlock extends AbstractNpcAI
{
	private static final int[] MONSTERS =
	{
		18319,
		18320,
		18321,
		18322,
		18323,
		18324,
		18325,
		18326
	};

	private static final NpcStringId[] NPC_STRINGS_ON_SPAWN =
	{
		NpcStringId.CROAK_CROAK_FOOD_LIKE_S1_IN_THIS_PLACE,
		NpcStringId.S1_HOW_LUCKY_I_AM,
		NpcStringId.PRAY_THAT_YOU_CAUGHT_A_WRONG_FISH_S1
	};
	private static final NpcStringId[] NPC_STRINGS_ON_ATTACK =
	{
		NpcStringId.DO_YOU_KNOW_WHAT_A_FROG_TASTES_LIKE,
		NpcStringId.I_WILL_SHOW_YOU_THE_POWER_OF_A_FROG,
		NpcStringId.I_WILL_SWALLOW_AT_A_MOUTHFUL
	};
	private static final NpcStringId[] NPC_STRINGS_ON_KILL =
	{
		NpcStringId.UGH_NO_CHANCE_HOW_COULD_THIS_ELDER_PASS_AWAY_LIKE_THIS,
		NpcStringId.CROAK_CROAK_A_FROG_IS_DYING,
		NpcStringId.A_FROG_TASTES_BAD_YUCK
	};

	private static final int CHANCE_TO_SHOUT_ON_ATTACK = 33;
	private static final int DESPAWN_TIME = 50000;
	
	public WarriorFishingBlock(String name, String descr)
	{
		super(name, descr);

		addAttackId(MONSTERS);
		addKillId(MONSTERS);
		addSpawnId(MONSTERS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ((npc != null) && event.equals("DESPAWN"))
		{
			npc.deleteMe();
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (getRandom(100) < CHANCE_TO_SHOUT_ON_ATTACK)
		{
			npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, NPC_STRINGS_ON_ATTACK[getRandom(NPC_STRINGS_ON_ATTACK.length)]));
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, NPC_STRINGS_ON_KILL[getRandom(NPC_STRINGS_ON_KILL.length)]));
		cancelQuestTimer("DESPAWN", npc, killer);
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if ((npc == null) || !npc.isL2Attackable())
		{
			return null;
		}
		
		final L2Object target = npc.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			npc.deleteMe();
			return null;
		}
		
		final L2PcInstance player = target.getActingPlayer();
		final NpcSay say = new NpcSay(npc, Say2.NPC_ALL, NPC_STRINGS_ON_SPAWN[getRandom(NPC_STRINGS_ON_SPAWN.length)]);
		say.addStringParameter(player.getName());
		npc.broadcastPacket(say);
		
		((L2Attackable) npc).addDamageHate(player, 0, 2000);
		npc.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
		npc.addAttackerToAttackByList(player);
		
		startQuestTimer("DESPAWN", DESPAWN_TIME, npc, player);
		return null;
	}
	
	public static void main(String[] args)
	{
		new WarriorFishingBlock(WarriorFishingBlock.class.getSimpleName(), "ai");
	}
}