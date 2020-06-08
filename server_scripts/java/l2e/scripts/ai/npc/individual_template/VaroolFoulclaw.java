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
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Created by LordWinter 24.06.2011 Based on L2J Eternity-World
 */
public class VaroolFoulclaw extends L2AttackableAIScript
{
	private static final int VAROOL_FOULCLAW = 27020;
	
	public VaroolFoulclaw(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addAttackId(VAROOL_FOULCLAW);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		if (npc.getId() == VAROOL_FOULCLAW)
		{
			if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (getRandom(100) < 10)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.WHY_DO_YOU_OPPRESS_US_SO));
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	public static void main(String[] args)
	{
		new VaroolFoulclaw(-1, "VaroolFoulclaw", "ai");
	}
}