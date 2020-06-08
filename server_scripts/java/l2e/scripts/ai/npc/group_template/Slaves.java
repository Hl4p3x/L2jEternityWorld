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

import java.util.List;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.taskmanager.DecayTaskManager;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class Slaves extends AbstractNpcAI
{
	private static final int[] MASTERS =
	{
		22320,
		22321
	};
	
	private static final Location MOVE_TO = new Location(-25451, 252291, -3252, 3500);
	
	private static final int TRUST_REWARD = 10;
	
	private Slaves(String name, String descr)
	{
		super(name, descr);
		
		addSpawnId(MASTERS);
		addKillId(MASTERS);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		((L2MonsterInstance) npc).enableMinions(HellboundManager.getInstance().getLevel() < 5);
		((L2MonsterInstance) npc).setOnKillDelay(1000);
		
		return super.onSpawn(npc);
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (((L2MonsterInstance) npc).getMinionList() != null)
		{
			final List<L2MonsterInstance> slaves = ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions();
			if ((slaves != null) && !slaves.isEmpty())
			{
				for (L2MonsterInstance slave : slaves)
				{
					if ((slave == null) || slave.isDead())
					{
						continue;
					}
					
					slave.clearAggroList();
					slave.abortAttack();
					slave.abortCast();
					slave.broadcastPacket(new NpcSay(slave.getObjectId(), Say2.NPC_ALL, slave.getId(), NpcStringId.THANK_YOU_FOR_SAVING_ME_FROM_THE_CLUTCHES_OF_EVIL));
					
					if ((HellboundManager.getInstance().getLevel() >= 1) && (HellboundManager.getInstance().getLevel() <= 2))
					{
						HellboundManager.getInstance().updateTrust(TRUST_REWARD, false);
					}
					
					slave.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO);
					DecayTaskManager.getInstance().addDecayTask(slave);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Slaves(Slaves.class.getSimpleName(), "ai");
	}
}