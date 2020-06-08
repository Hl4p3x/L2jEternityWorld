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
package l2e.scripts.ai.zone.LairOfAntharas;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Created by LordWinter 08.05.2012 Based on L2J Eternity-World
 */
public class DragonKnights extends L2AttackableAIScript
{
	private static final int DRAGON_KNIGHT_1 = 22844;
	private static final int DRAGON_KNIGHT_2 = 22845;
	private static final int ELITE_DRAGON_KNIGHT = 22846;
	private static final int DRAGON_KNIGHT_WARRIOR = 22847;
	
	public DragonKnights(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addKillId(DRAGON_KNIGHT_1);
		addKillId(DRAGON_KNIGHT_2);
		addKillId(ELITE_DRAGON_KNIGHT);
		addKillId(DRAGON_KNIGHT_WARRIOR);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == DRAGON_KNIGHT_1)
		{
			if (getRandom(1000) < 400)
			{
				final L2Npc warrior = addSpawn(DRAGON_KNIGHT_2, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 240000, true);
				warrior.broadcastPacket(new NpcSay(warrior.getObjectId(), 0, warrior.getId(), NpcStringId.THOSE_WHO_SET_FOOT_IN_THIS_PLACE_SHALL_NOT_LEAVE_ALIVE));
				warrior.setRunning();
				((L2Attackable) warrior).addDamageHate(killer, 1, 99999);
				warrior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
				
				final L2Npc warrior1 = addSpawn(DRAGON_KNIGHT_2, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 240000, true);
				warrior1.setRunning();
				((L2Attackable) warrior1).addDamageHate(killer, 1, 99999);
				warrior1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
		}
		
		if (npc.getId() == DRAGON_KNIGHT_2)
		{
			if (getRandom(1000) < 350)
			{
				final L2Npc knight = addSpawn(ELITE_DRAGON_KNIGHT, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 240000, true);
				knight.broadcastPacket(new NpcSay(knight.getObjectId(), 0, knight.getId(), NpcStringId.IF_YOU_WISH_TO_SEE_HELL_I_WILL_GRANT_YOU_YOUR_WISH));
				knight.setRunning();
				((L2Attackable) knight).addDamageHate(killer, 1, 99999);
				knight.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
			
			if (getRandom(1000) < 350)
			{
				final L2Npc warior = addSpawn(DRAGON_KNIGHT_WARRIOR, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 240000, true);
				warior.broadcastPacket(new NpcSay(warior.getObjectId(), 0, warior.getId(), NpcStringId.IF_YOU_WISH_TO_SEE_HELL_I_WILL_GRANT_YOU_YOUR_WISH));
				warior.setRunning();
				((L2Attackable) warior).addDamageHate(killer, 1, 99999);
				warior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new DragonKnights(-1, DragonKnights.class.getSimpleName(), "ai");
	}
}