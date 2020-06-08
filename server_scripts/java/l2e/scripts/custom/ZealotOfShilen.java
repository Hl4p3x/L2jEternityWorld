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
package l2e.scripts.custom;

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.xml.SpawnParser;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class ZealotOfShilen extends AbstractNpcAI
{
	private static final int ZEALOT = 18782;
	private static final int GUARD1 = 32628;
	private static final int GUARD2 = 32629;

	private final List<L2Npc> _zealot = new ArrayList<>();
	private final List<L2Npc> _guard1 = new ArrayList<>();
	private final List<L2Npc> _guard2 = new ArrayList<>();

	private ZealotOfShilen(String name, String descr)
	{
		super(name, descr);

		addSpawnId(ZEALOT);

		addFirstTalkId(GUARD1, GUARD2);

		findNpcs();
	}

	private void findNpcs()
	{
		for (L2Spawn spawn : SpawnParser.getInstance().getSpawnData())
		{
			if (spawn != null)
			{
				if (spawn.getId() == ZEALOT)
				{
					_zealot.add(spawn.getLastSpawn());
					for (L2Npc zealot : _zealot)
					{
						zealot.setIsNoRndWalk(true);
					}
				}
				else if (spawn.getId() == GUARD1)
				{
					_guard1.add(spawn.getLastSpawn());
					for (L2Npc guard : _guard1)
					{
						guard.setIsInvul(true);
						((L2Attackable) guard).setCanReturnToSpawnPoint(false);
						startQuestTimer("WATCHING", 10000, guard, null, true);
					}
				}
				else if (spawn.getId() == GUARD2)
				{
					_guard2.add(spawn.getLastSpawn());
					for (L2Npc guards : _guard2)
					{
						guards.setIsInvul(true);
						((L2Attackable) guards).setCanReturnToSpawnPoint(false);
						startQuestTimer("WATCHING", 10000, guards, null, true);
					}
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("WATCHING") && !npc.isAttackingNow())
		{
			for (L2Character character : npc.getKnownList().getKnownCharacters())
			{
				if (character.isMonster() && !character.isDead() && !((L2Attackable) character).isDecayed())
				{
					npc.setRunning();
					((L2Attackable) npc).addDamageHate(character, 0, 999);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null);
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return (npc.isAttackingNow()) ? "32628-01.htm" : npc.getId() + ".htm";
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new ZealotOfShilen(ZealotOfShilen.class.getSimpleName(), "custom");
	}
}
