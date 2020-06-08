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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class ForgeOfTheGods extends AbstractNpcAI
{
	private static final int[] FOG_MOBS =
	{
		22634,
		22635,
		22636,
		22637,
		22638,
		22639,
		22640,
		22641,
		22642,
		22643,
		22644,
		22645,
		22646,
		22647,
		22648,
		22649
	};
	
	private static final int[] LAVASAURUSES =
	{
		18799,
		18800,
		18801,
		18802,
		18803
	};
	
	private static final int REFRESH = 15;
	
	private static final int MOBCOUNT_BONUS_MIN = 3;
	
	private static final int BONUS_UPPER_LV01 = 5;
	private static final int BONUS_UPPER_LV02 = 10;
	private static final int BONUS_UPPER_LV03 = 15;
	private static final int BONUS_UPPER_LV04 = 20;
	private static final int BONUS_UPPER_LV05 = 35;
	
	private static final int BONUS_LOWER_LV01 = 5;
	private static final int BONUS_LOWER_LV02 = 10;
	private static final int BONUS_LOWER_LV03 = 15;
	
	private static final int FORGE_BONUS01 = 20;
	private static final int FORGE_BONUS02 = 40;
	
	private static int _npcCount = 0;
	
	public ForgeOfTheGods(String name, String descr)
	{
		super(name, descr);
		
		addKillId(FOG_MOBS);
		
		addSpawnId(LAVASAURUSES);
		
		startQuestTimer("refresh", REFRESH * 1000, null, null, true);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "suicide":
				if (npc != null)
				{
					npc.doDie(null);
				}
				break;
			case "refresh":
				_npcCount = 0;
				break;
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int rand = getRandom(100);
		L2Npc mob = null;
		_npcCount++;
		
		if (npc.getSpawn().getZ() < -5000)
		{
			if ((_npcCount > BONUS_LOWER_LV03) && (rand <= FORGE_BONUS02))
			{
				mob = addSpawn(LAVASAURUSES[4], npc, true);
			}
			else if (_npcCount > BONUS_LOWER_LV02)
			{
				mob = spawnLavasaurus(npc, rand, LAVASAURUSES[4], LAVASAURUSES[3]);
			}
			else if (_npcCount > BONUS_LOWER_LV01)
			{
				mob = spawnLavasaurus(npc, rand, LAVASAURUSES[3], LAVASAURUSES[2]);
			}
			else if (_npcCount >= MOBCOUNT_BONUS_MIN)
			{
				mob = spawnLavasaurus(npc, rand, LAVASAURUSES[2], LAVASAURUSES[1]);
			}
		}
		else
		{
			if ((_npcCount > BONUS_UPPER_LV05) && (rand <= FORGE_BONUS02))
			{
				mob = addSpawn(LAVASAURUSES[1], npc, true);
			}
			else if (_npcCount > BONUS_UPPER_LV04)
			{
				mob = spawnLavasaurus(npc, rand, LAVASAURUSES[4], LAVASAURUSES[3]);
			}
			else if (_npcCount > BONUS_UPPER_LV03)
			{
				mob = spawnLavasaurus(npc, rand, LAVASAURUSES[3], LAVASAURUSES[2]);
			}
			else if (_npcCount > BONUS_UPPER_LV02)
			{
				mob = spawnLavasaurus(npc, rand, LAVASAURUSES[2], LAVASAURUSES[1]);
			}
			else if (_npcCount > BONUS_UPPER_LV01)
			{
				mob = spawnLavasaurus(npc, rand, LAVASAURUSES[1], LAVASAURUSES[0]);
			}
			else if ((_npcCount >= MOBCOUNT_BONUS_MIN) && (rand <= FORGE_BONUS01))
			{
				mob = addSpawn(LAVASAURUSES[0], npc, true);
			}
		}
		if (mob != null)
		{
			((L2Attackable) mob).addDamageHate(killer, 0, 9999);
			mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		startQuestTimer("suicide", 60000, npc, null);
		return super.onSpawn(npc);
	}
	
	private L2Npc spawnLavasaurus(L2Npc npc, int rand, int... mobs)
	{
		if (mobs.length < 2)
		{
			return null;
		}
		
		L2Npc mob = null;
		if (rand <= FORGE_BONUS01)
		{
			mob = addSpawn(mobs[0], npc, true);
		}
		else if (rand <= FORGE_BONUS02)
		{
			mob = addSpawn(mobs[1], npc, true);
		}
		return mob;
	}
	
	public static void main(String[] args)
	{
		new ForgeOfTheGods(ForgeOfTheGods.class.getSimpleName(), "ai");
	}
}