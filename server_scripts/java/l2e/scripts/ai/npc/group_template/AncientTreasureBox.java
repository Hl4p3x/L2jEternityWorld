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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;

/**
 * Created by LordWinter 19.08.2012
 * Based on L2J Eternity-World
 */
public class AncientTreasureBox extends Quest
{
  	private static final int BOX = 18693;
  	private static final int GUARD1 = 18694;
  	private static final int GUARD2 = 18695;

  	private static final int[] mobs = 
	{ 
  	  	20965, 20966, 20967, 20968, 20969, 20970, 20971, 20972, 20973, 18693 
	};

  	public AncientTreasureBox(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		for (int id : mobs)
      			addKillId(id);
  	}

	@Override
  	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
  	{
    		if (killer == null)
      			return null;

    		if (npc.getId() == BOX) 
		{
      			((L2MonsterInstance)npc).dropItem(killer, 13799, 1);
    		}
		else if (getRandom(1000) < 2)
    		{
      			L2Character attacker = isSummon ? killer.getSummon().getOwner() : killer;
      			L2Attackable box = (L2Attackable)addSpawn(BOX, npc.getX(), npc.getY(), npc.getZ(), 0, false, 300000L);
      			int x = box.getX();
      			int y = box.getY();
      			box.setIsImmobilized(true);

      			L2Attackable guard1 = (L2Attackable)addSpawn(GUARD1, x + 50, y + 50, npc.getZ(), 0, false, 300000L);
      			guard1.setIsImmobilized(true);
      			guard1.addDamageHate(attacker, 0, 999);
      			guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);

      			L2Attackable guard2 = (L2Attackable)addSpawn(GUARD2, x + 50, y - 50, npc.getZ(), 0, false, 300000L);
      			guard2.setIsImmobilized(true);
      			guard2.addDamageHate(attacker, 0, 999);
      			guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);

      			L2Attackable guard3 = (L2Attackable)addSpawn(GUARD2, x - 50, y + 50, npc.getZ(), 0, false, 300000L);
      			guard3.setIsImmobilized(true);
      			guard3.addDamageHate(attacker, 0, 999);
      			guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);

      			L2Attackable guard4 = (L2Attackable)addSpawn(GUARD1, x - 50, y - 50, npc.getZ(), 0, false, 300000L);
      			guard4.setIsImmobilized(true);
      			guard4.addDamageHate(attacker, 0, 999);
      			guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
    		}
    		return super.onKill(npc, killer, isSummon);
  	}

  	public static void main(String[] args)
  	{
    		new AncientTreasureBox(-1, "AncientTreasureBox", "ai");
  	}
}