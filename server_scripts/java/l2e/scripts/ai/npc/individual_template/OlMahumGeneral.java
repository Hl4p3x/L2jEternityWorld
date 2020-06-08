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
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class OlMahumGeneral extends L2AttackableAIScript
{
	private static final int Ol_Mahum_General = 20438;
	
	private static boolean _FirstAttacked;
	
	public OlMahumGeneral(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		int[] mobs =
		{
			Ol_Mahum_General
		};
		registerMobs(mobs, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL);
		_FirstAttacked = false;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == Ol_Mahum_General)
		{
			if (_FirstAttacked)
			{
				if (getRandom(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.I_WILL_DEFINITELY_REPAY_THIS_HUMILIATION));
				}
				if (getRandom(100) == 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.WE_SHALL_SEE_ABOUT_THAT));
				}
			}
			_FirstAttacked = true;
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();
		if (npcId == Ol_Mahum_General)
		{
			_FirstAttacked = false;
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new OlMahumGeneral(-1, "OlMahumGeneral", "ai");
	}
}