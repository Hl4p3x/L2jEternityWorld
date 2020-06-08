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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Created by LordWinter 23.05.2012 Based on L2J Eternity-World
 */
public class EtisEtina extends L2AttackableAIScript
{
	private static final int ETIS = 18949;
	private static final int GUARD1 = 18950;
	private static final int GUARD2 = 18951;
	
	private boolean _FirstAttacked = false;
	
	public EtisEtina(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addAttackId(ETIS);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == ETIS)
		{
			final int maxHp = npc.getMaxHp();
			final double nowHp = npc.getStatus().getCurrentHp();
			
			if (nowHp < (maxHp * 0.7))
			{
				if (_FirstAttacked)
				{
					return null;
				}
				final L2Npc warrior = addSpawn(GUARD1, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				warrior.setRunning();
				((L2Attackable) warrior).addDamageHate(attacker, 1, 99999);
				warrior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
				
				final L2Npc warrior1 = addSpawn(GUARD2, npc.getX() + getRandom(10, 80), npc.getY() + getRandom(10, 80), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				warrior1.setRunning();
				((L2Attackable) warrior1).addDamageHate(attacker, 1, 99999);
				warrior1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
				_FirstAttacked = true;
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new EtisEtina(-1, "EtisEtina", "ai");
	}
}