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

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.util.Util;

/**
 * Fixed by LordWinter 15.11.2012 Based on L2J Eternity-World
 */
public class StarStones extends AbstractNpcAI
{
	private static final int[] MOBS =
	{
		18684,
		18685,
		18686,
		18687,
		18688,
		18689,
		18690,
		18691,
		18692
	};
	
	private static final int RATE = 1;
	
	private StarStones(String name, String descr)
	{
		super(name, descr);

		registerMobs(MOBS, QuestEventType.ON_SKILL_SEE);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (Util.contains(targets, npc))
		{
			if (npc.isInsideRadius(caster, 120, false, false))
			{
				if (skill.getId() == 932)
				{
					int itemId = 0;
			
					switch (npc.getId())
					{
						case 18684:
						case 18685:
						case 18686:
							itemId = 14009;
							break;
						case 18687:
						case 18688:
						case 18689:
							itemId = 14010;
							break;
						case 18690:
						case 18691:
						case 18692:
							itemId = 14011;
							break;
						default:
							return super.onSkillSee(npc, caster, skill, targets, isSummon);
					}
					if (getRandom(100) < 33)
					{
						caster.sendPacket(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
						caster.addItem("StarStone", itemId, getRandom(RATE + 1, 2 * RATE), null, true);
					}
					else if (((skill.getLevel() == 1) && (getRandom(100) < 15)) || ((skill.getLevel() == 2) && (getRandom(100) < 50)) || ((skill.getLevel() == 3) && (getRandom(100) < 75)))
					{
						caster.sendPacket(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
						caster.addItem("StarStone", itemId, getRandom(1, RATE), null, true);
					}
					else
					{
						caster.sendPacket(SystemMessageId.THE_COLLECTION_HAS_FAILED);
					}
					npc.deleteMe();
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	public static void main(String[] args)
	{
		new StarStones(StarStones.class.getSimpleName(), "ai");
	}
}