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

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class BodyDestroyer extends L2AttackableAIScript
{
	private static final int BDESTROYER = 22363;

	boolean _isLocked = false;

	public BodyDestroyer(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addAttackId(BDESTROYER);
		addKillId(BDESTROYER);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_destroy"))
		{
			player.setCurrentHp(1);
		}

		return "";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon, L2Skill skill)
	{
		int npcId = npc.getId();

		if (npcId == BDESTROYER)
		{
			if (_isLocked == false)
			{
				((L2Attackable) npc).addDamageHate(player, 0, 9999);
				_isLocked = true;
				npc.setTarget(player);
				npc.doCast(SkillHolder.getInstance().getInfo(5256, 1));
				player.setCurrentHp(1);
				startQuestTimer("time_to_destroy", 30000, npc, player);
			}
		}
		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcId = npc.getId();

		if (npcId == BDESTROYER)
		{
			cancelQuestTimer("time_to_destroy", npc, player);
			player.stopSkillEffects(5256);
			_isLocked = false;
		}
		return "";
	}

	public static void main(String[] args)
	{
		new BodyDestroyer(-1, "BodyDestroyer", "ai");
	}
}
