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
import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.scripts.ai.npc.AbstractNpcAI;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public final class CatsEyeBandit extends AbstractNpcAI
{
	// NPC ID
	private static final int MOB_ID = 27038;
	
	// Weapons
	private static final int BOW = 1181;
	private static final int DAGGER = 1182;
	
	public CatsEyeBandit(String name, String descr)
	{
		super(name, descr);

		addAttackId(MOB_ID);
		addKillId(MOB_ID);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		final QuestState qs = attacker.getQuestState("_403_PathToRogue");
		if (npc.isScriptValue(0) && qs != null && (qs.getItemEquipped(Inventory.PAPERDOLL_RHAND) == BOW || qs.getItemEquipped(Inventory.PAPERDOLL_RHAND) == DAGGER))
		{
			broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.YOU_CHILDISH_FOOL_DO_YOU_THINK_YOU_CAN_CATCH_ME);
			npc.setScriptValue(1);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = killer.getQuestState("_403_PathToRogue");
		if (qs != null)
		{
			broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.I_MUST_DO_SOMETHING_ABOUT_THIS_SHAMEFUL_INCIDENT);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new CatsEyeBandit(CatsEyeBandit.class.getSimpleName(), "ai");
	}
}