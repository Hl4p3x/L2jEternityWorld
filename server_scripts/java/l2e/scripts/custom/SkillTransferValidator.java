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
package l2e.scripts.custom;

import l2e.Config;
import l2e.gameserver.data.xml.ClassListParser;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.scripting.scriptengine.events.ProfessionChangeEvent;
import l2e.gameserver.scripting.scriptengine.impl.L2Script;
import l2e.gameserver.util.Util;

public final class SkillTransferValidator extends L2Script
{
	private static final String qn = "SkillTransfer";

	private static final ItemsHolder[] PORMANDERS =
	{
	                new ItemsHolder(15307, 1),
	                new ItemsHolder(15308, 1),
	                new ItemsHolder(15309, 4)
	};

	public SkillTransferValidator(int id, String name, String descr)
	{
		super(id, name, descr);
		setOnEnterWorld(true);
	}

	@Override
	public String onEnterWorld(L2PcInstance player)
	{
		if (getTransferClassIndex(player) >= 0)
		{
			addProfessionChangeNotify(player);
			startQuestTimer("givePormanders", 2000, null, player);
		}
		return null;
	}

	@Override
	public void onProfessionChange(ProfessionChangeEvent event)
	{
		startQuestTimer("givePormanders", 2000, null, event.getPlayer());
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("givePormanders"))
		{
			final int index = getTransferClassIndex(player);
			if (index >= 0)
			{
				QuestState st = player.getQuestState(qn);
				if (st == null)
				{
					st = newQuestState(player);
				}

				final String name = qn + String.valueOf(player.getClassId().getId());
				if (st.getInt(name) == 0)
				{
					st.setInternal(name, "1");
					if (st.getGlobalQuestVar(name).isEmpty())
					{
						st.saveGlobalQuestVar(name, "1");
						player.addItem(qn, PORMANDERS[index].getId(), PORMANDERS[index].getCount(), null, true);
					}
				}

				if (Config.SKILL_CHECK_ENABLE && (!player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) || Config.SKILL_CHECK_GM))
				{
					long count = PORMANDERS[index].getCount() - player.getInventory().getInventoryItemCount(PORMANDERS[index].getId(), -1, false);
					for (L2Skill sk : player.getAllSkills())
					{
						for (L2SkillLearn s : SkillTreesParser.getInstance().getTransferSkillTree(player.getClassId()).values())
						{
							if (s.getSkillId() == sk.getId())
							{
								if ((sk.getId() == 1043) && (index == 2) && player.isInStance())
								{
									continue;
								}

								count--;
								if (count < 0)
								{
									final String className = ClassListParser.getInstance().getClass(player.getClassId()).getClassName();
									Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has too many transfered skills or items, skill:" + s.getName() + " (" + sk.getId() + "/" + sk.getLevel() + "), class:" + className, 1);
									if (Config.SKILL_CHECK_REMOVE)
									{
										player.removeSkill(sk);
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private int getTransferClassIndex(L2PcInstance player)
	{
		switch (player.getClassId().getId())
		{
			case 97:
				return 0;
			case 105:
				return 1;
			case 112:
				return 2;
			default:
				return -1;
		}
	}

	public static void main(String[] args)
	{
		new SkillTransferValidator(-1, qn, "custom");
	}
}
