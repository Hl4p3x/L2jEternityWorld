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
 * Create by LordWinter 15.11.2012 Based on L2J Eternity-World
 */
public class EnergySeed extends AbstractNpcAI
{
	private static final int[] MOBS =
	{
		18678,
		18679,
		18680,
		18681,
		18682,
		18683
	};
	
	private static final int RATE = 1;
	
	private EnergySeed(String name, String descr)
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
				if (skill.getId() == 5780)
				{
					int itemId = 0;
			
					switch (npc.getId())
					{
						case 18678:
							itemId = 14016;
							break;
						case 18679:
							itemId = 14015;
							break;
						case 18680:
							itemId = 14017;
							break;
						case 18681:
							itemId = 14018;
							break;
						case 18682:
							itemId = 14020;
							break;
						case 18683:
							itemId = 14019;
							break;
						default:
							return super.onSkillSee(npc, caster, skill, targets, isSummon);
					}

					if (getRandom(100) < 33)
					{
						caster.sendPacket(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
						caster.addItem("EnergySeed", itemId, getRandom(RATE + 1, 2 * RATE), null, true);
					}
					else if (((skill.getLevel() == 1) && (getRandom(100) < 15)) || ((skill.getLevel() == 2) && (getRandom(100) < 50)) || ((skill.getLevel() == 3) && (getRandom(100) < 75)))
					{
						caster.sendPacket(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
						caster.addItem("EnergySeed", itemId, getRandom(1, RATE), null, true);
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
		new EnergySeed(EnergySeed.class.getSimpleName(), "ai");
	}
}