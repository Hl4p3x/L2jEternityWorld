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

import java.util.Collection;
import java.util.List;

import l2e.Config;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class HealerTrainer extends AbstractNpcAI
{
	private static final int[] HEALER_TRAINERS =
	{
	                30022, 30030, 30032, 30036, 30067, 30068, 30116, 30117, 30118, 30119, 30144, 30145, 30188, 30194, 30293, 30330, 30375, 30377, 30464, 30473, 30476, 30680, 30701, 30720, 30721, 30858, 30859, 30860, 30861, 30864, 30906, 30908, 30912, 31280, 31281, 31287, 31329, 31330, 31335, 31969, 31970, 31976, 32155, 32162
	};

	private static final int MIN_LEVEL = 76;
	private static final int MIN_CLASS_LEVEL = 3;

	public HealerTrainer()
	{
		super(HealerTrainer.class.getSimpleName(), "custom");

		addStartNpc(HEALER_TRAINERS);
		addTalkId(HEALER_TRAINERS);
		addFirstTalkId(HEALER_TRAINERS);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "30864.htm":
			case "30864-1.htm":
			{
				htmltext = event;
				break;
			}
			case "SkillTransfer":
			{
				htmltext = "main.htm";
				break;
			}
			case "SkillTransferLearn":
			{
				if (!npc.getTemplate().canTeach(player.getClassId()))
				{
					htmltext = npc.getId() + "-noteach.htm";
					break;
				}
				if ((player.getLevel() < MIN_LEVEL) || (player.getClassId().level() < MIN_CLASS_LEVEL))
				{
					htmltext = "learn-lowlevel.htm";
					break;
				}
				displayTransferSkillList(player);
				break;
			}
			case "SkillTransferCleanse":
			{
				if (!npc.getTemplate().canTeach(player.getClassId()))
				{
					htmltext = "cleanse-no.htm";
					break;
				}

				if ((player.getLevel() < MIN_LEVEL) || (player.getClassId().level() < MIN_CLASS_LEVEL))
				{
					htmltext = "cleanse-no.htm";
					break;
				}

				if (player.getAdena() < Config.FEE_DELETE_TRANSFER_SKILLS)
				{
					player.sendPacket(SystemMessageId.CANNOT_RESET_SKILL_LINK_BECAUSE_NOT_ENOUGH_ADENA);
					break;
				}

				boolean hasSkills = false;
				if (!hasTransferSkillItems(player))
				{
					final Collection<L2SkillLearn> skills = SkillTreesParser.getInstance().getTransferSkillTree(player.getClassId()).values();
					for (L2SkillLearn s : skills)
					{
						final L2Skill sk = player.getKnownSkill(s.getSkillId());
						if (sk != null)
						{
							player.removeSkill(sk);
							for (ItemsHolder item : s.getRequiredItems())
							{
								player.addItem("Cleanse", item.getId(), item.getCount(), npc, true);
							}
							hasSkills = true;
						}
					}

					if (hasSkills)
					{
						player.reduceAdena("Cleanse", Config.FEE_DELETE_TRANSFER_SKILLS, npc, true);
					}
				}
				else
				{
					htmltext = "cleanse-no_skills.htm";
				}
				break;
			}
		}
		return htmltext;
	}

	private static void displayTransferSkillList(L2PcInstance player)
	{
		final List<L2SkillLearn> skills = SkillTreesParser.getInstance().getAvailableTransferSkills(player);
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.TRANSFER);
		int count = 0;

		for (L2SkillLearn s : skills)
		{
			if (SkillHolder.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()) != null)
			{
				count++;
				asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 0);
			}
		}

		if (count > 0)
		{
			player.sendPacket(asl);
		}
		else
		{
			player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
		}
	}

	private static boolean hasTransferSkillItems(L2PcInstance player)
	{
		int itemId;
		switch (player.getClassId())
		{
			case cardinal:
				itemId = 15307;
				break;
			case evaSaint:
				itemId = 15308;
				break;
			case shillienSaint:
				itemId = 15309;
				break;
			default:
				itemId = -1;
		}
		return (player.getInventory().getInventoryItemCount(itemId, -1) > 0);
	}

	public static void main(String[] args)
	{
		new HealerTrainer();
	}
}
