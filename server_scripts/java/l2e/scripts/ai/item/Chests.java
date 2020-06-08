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
package l2e.scripts.ai.item;

import org.apache.commons.lang.ArrayUtils;

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2ChestInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Updated by LordWinter 12.06.2011 Based on L2J Eternity-World
 */
public class Chests extends L2AttackableAIScript
{
	private static final int[] NPC_IDS =
	{
	                18265,
	                18266,
	                18267,
	                18268,
	                18269,
	                18270,
	                18271,
	                18272,
	                18273,
	                18274,
	                18275,
	                18276,
	                18277,
	                18278,
	                18279,
	                18280,
	                18281,
	                18282,
	                18283,
	                18284,
	                18285,
	                18286
	};

	public Chests(int questId, String name, String descr)
	{
		super(questId, name, descr);

		registerMobs(NPC_IDS, QuestEventType.ON_ATTACK);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		L2ChestInstance chest = ((L2ChestInstance) npc);
		int npcId = chest.getId();

		if (ArrayUtils.contains(NPC_IDS, npcId))
		{
			if (!chest.isInteracted())
			{
				L2Skill bomb = SkillHolder.getInstance().getInfo(4143, getBombLvl(npc));
				if (bomb != null)
				{
					npc.setTarget(attacker);
					npc.doCast(bomb);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	private int getBombLvl(L2Npc npc)
	{
		int npcLvl = npc.getLevel();
		int lvl = 1;
		if (npcLvl >= 78)
		{
			lvl = 10;
		}
		else if (npcLvl >= 72)
		{
			lvl = 9;
		}
		else if (npcLvl >= 66)
		{
			lvl = 8;
		}
		else if (npcLvl >= 60)
		{
			lvl = 7;
		}
		else if (npcLvl >= 54)
		{
			lvl = 6;
		}
		else if (npcLvl >= 48)
		{
			lvl = 5;
		}
		else if (npcLvl >= 42)
		{
			lvl = 4;
		}
		else if (npcLvl >= 36)
		{
			lvl = 3;
		}
		else if (npcLvl >= 30)
		{
			lvl = 2;
		}
		return lvl;
	}

	public static void main(String[] args)
	{
		new Chests(-1, "chests", "ai");
	}
}
