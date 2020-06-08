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
import l2e.scripts.ai.L2AttackableAIScript;

public class ValakasMinions extends L2AttackableAIScript
{
	private static final int Valakas = 29028;
	private static final int PUSTBON = 29029;
	
	public int ValakasStatus;
	
	public ValakasMinions(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addKillId(Valakas);
		addAttackId(Valakas);
		addSpawnId(Valakas);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == Valakas)
		{
			ValakasStatus = 0;
		}
		
		return super.onSpawn(npc);
	}
	
	private void SpawnMobs(L2Npc npc)
	{
		addSpawn(PUSTBON, 211555, -113281, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 212558, -112708, -1639, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 214460, -113874, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 214498, -115229, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 214209, -116394, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 214256, -116424, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 213214, -116647, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 212590, -116376, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 211801, -116142, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 210882, -114370, -1636, 0, false, 0, false, npc.getInstanceId());
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == Valakas)
		{
			final int maxHp = npc.getMaxHp();
			final double nowHp = npc.getStatus().getCurrentHp();
			
			switch (ValakasStatus)
			{
				case 0:
					if (nowHp < (maxHp * 0.8))
					{
						ValakasStatus = 1;
						SpawnMobs(npc);
					}
					break;
				case 1:
					if (nowHp < (maxHp * 0.6))
					{
						ValakasStatus = 2;
						SpawnMobs(npc);
					}
					break;
				case 2:
					if (nowHp < (maxHp * 0.4))
					{
						ValakasStatus = 3;
						SpawnMobs(npc);
					}
					break;
				case 3:
					if (nowHp < (maxHp * 0.3))
					{
						ValakasStatus = 4;
						SpawnMobs(npc);
					}
					break;
				case 4:
					if (nowHp < (maxHp * 0.2))
					{
						ValakasStatus = 5;
						SpawnMobs(npc);
					}
					break;
				case 5:
					if (nowHp < (maxHp * 0.1))
					{
						ValakasStatus = 6;
						SpawnMobs(npc);
					}
					break;
				case 6:
					if (nowHp < (maxHp * 0.05))
					{
						ValakasStatus = 7;
						SpawnMobs(npc);
					}
					break;
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	public static void main(String[] args)
	{
		new ValakasMinions(-1, "ValakasMinions", "ai");
	}
}