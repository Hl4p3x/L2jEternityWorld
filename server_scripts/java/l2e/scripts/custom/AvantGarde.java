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

import java.util.List;

import l2e.Config;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.MultiSellParser;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.RequestAcquireSkill;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class AvantGarde extends AbstractNpcAI
{
	private static final int AVANT_GARDE = 32323;

	private static final int[] ITEMS =
	{
	                10280,
	                10281,
	                10282,
	                10283,
	                10284,
	                10285,
	                10286,
	                10287,
	                10288,
	                10289,
	                10290,
	                10291,
	                10292,
	                10293,
	                10294,
	                10612
	};

	private static final String[] QUEST_VAR_NAMES =
	{
	                "EmergentAbility65-",
	                "EmergentAbility70-",
	                "ClassAbility75-",
	                "ClassAbility80-"
	};

	public AvantGarde()
	{
		super(AvantGarde.class.getSimpleName(), "custom");

		addStartNpc(AVANT_GARDE);
		addTalkId(AVANT_GARDE);
		addFirstTalkId(AVANT_GARDE);
		addAcquireSkillId(AVANT_GARDE);
	}

	@Override
	public String onAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill, AcquireSkillType type)
	{
		switch (type)
		{
			case TRANSFORM:
			{
				showTransformSkillList(player);
				break;
			}
			case SUBCLASS:
			{
				showSubClassSkillList(player);
				break;
			}
		}
		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "32323-02.htm":
			case "32323-02a.htm":
			case "32323-02b.htm":
			case "32323-02c.htm":
			case "32323-05.htm":
			case "32323-05a.htm":
			case "32323-05no.htm":
			case "32323-06.htm":
			case "32323-06no.htm":
			{
				htmltext = event;
				break;
			}
			case "LearnTransformationSkill":
			{
				if (RequestAcquireSkill.canTransform(player))
				{
					showTransformSkillList(player);
				}
				else
				{
					htmltext = "32323-03.htm";
				}
				break;
			}
			case "BuyTransformationItems":
			{
				if (RequestAcquireSkill.canTransform(player))
				{
					MultiSellParser.getInstance().separateAndSend(32323001, player, npc, false);
				}
				else
				{
					htmltext = "32323-04.htm";
				}
				break;
			}
			case "LearnSubClassSkill":
			{
				if (!RequestAcquireSkill.canTransform(player))
				{
					htmltext = "32323-04.htm";
				}
				if (player.isSubClassActive())
				{
					htmltext = "32323-08.htm";
				}
				else
				{
					boolean hasItems = false;
					for (int id : ITEMS)
					{
						if (player.getInventory().getItemByItemId(id) != null)
						{
							hasItems = true;
							break;
						}
					}
					if (hasItems)
					{
						showSubClassSkillList(player);
						break;
					}
					htmltext = "32323-08.htm";
				}
				break;
			}
			case "CancelCertification":
			{
				if (player.getSubClasses().size() == 0)
				{
					htmltext = "32323-07.htm";
				}
				else if (player.isSubClassActive())
				{
					htmltext = "32323-08.htm";
				}
				else if (player.getAdena() < Config.FEE_DELETE_SUBCLASS_SKILLS)
				{
					htmltext = "32323-08no.htm";
				}
				else
				{
					QuestState st = player.getQuestState("SubClassSkills");
					if (st == null)
					{
						st = QuestManager.getInstance().getQuest("SubClassSkills").newQuestState(player);
					}

					int activeCertifications = 0;
					for (String varName : QUEST_VAR_NAMES)
					{
						for (int i = 1; i <= Config.MAX_SUBCLASS; i++)
						{
							String qvar = st.getGlobalQuestVar(varName + i);
							if (!qvar.isEmpty() && (qvar.endsWith(";") || !qvar.equals("0")))
							{
								activeCertifications++;
							}
						}
					}
					if (activeCertifications == 0)
					{
						htmltext = "32323-10no.htm";
					}
					else
					{
						for (String varName : QUEST_VAR_NAMES)
						{
							for (int i = 1; i <= Config.MAX_SUBCLASS; i++)
							{
								final String qvarName = varName + i;
								final String qvar = st.getGlobalQuestVar(qvarName);
								if (qvar.endsWith(";"))
								{
									final String skillIdVar = qvar.replace(";", "");
									if (Util.isDigit(skillIdVar))
									{
										int skillId = Integer.parseInt(skillIdVar);
										final L2Skill sk = SkillHolder.getInstance().getInfo(skillId, 1);
										if (sk != null)
										{
											player.removeSkill(sk);
											st.saveGlobalQuestVar(qvarName, "0");
										}
									}
									else
									{
										_log.warning("Invalid Sub-Class Skill Id: " + skillIdVar + " for player " + player.getName() + "!");
									}
								}
								else if (!qvar.isEmpty() && !qvar.equals("0"))
								{
									if (Util.isDigit(qvar))
									{
										final int itemObjId = Integer.parseInt(qvar);
										L2ItemInstance itemInstance = player.getInventory().getItemByObjectId(itemObjId);
										if (itemInstance != null)
										{
											player.destroyItem("CancelCertification", itemObjId, 1, player, false);
										}
										else
										{
											itemInstance = player.getWarehouse().getItemByObjectId(itemObjId);
											if (itemInstance != null)
											{
												_log.warning("Somehow " + player.getName() + " put a certification book into warehouse!");
												player.getWarehouse().destroyItem("CancelCertification", itemInstance, 1, player, false);
											}
											else
											{
												_log.warning("Somehow " + player.getName() + " deleted a certification book!");
											}
										}
										st.saveGlobalQuestVar(qvarName, "0");
									}
									else
									{
										_log.warning("Invalid item object Id: " + qvar + " for player " + player.getName() + "!");
									}
								}
							}
						}

						player.reduceAdena("Cleanse", Config.FEE_DELETE_SUBCLASS_SKILLS, npc, true);
						htmltext = "32323-09no.htm";
						player.sendSkillList();
					}

					for (int itemId : ITEMS)
					{
						L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
						if (item != null)
						{
							_log.warning(getClass().getName() + ": player " + player + " had 'extra' certification skill books while cancelling sub-class certifications!");
							player.destroyItem("CancelCertificationExtraBooks", item, npc, false);
						}
					}
				}
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "32323-01.htm";
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		return "32323-01.htm";
	}

	public static void showSubClassSkillList(L2PcInstance player)
	{
		final List<L2SkillLearn> subClassSkills = SkillTreesParser.getInstance().getAvailableSubClassSkills(player);
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.SUBCLASS);
		int count = 0;

		for (L2SkillLearn s : subClassSkills)
		{
			if (SkillHolder.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()) != null)
			{
				count++;
				asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), 0, 0);
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

	public static void showTransformSkillList(L2PcInstance player)
	{
		final List<L2SkillLearn> skills = SkillTreesParser.getInstance().getAvailableTransformSkills(player);
		final AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.TRANSFORM);
		int counts = 0;

		for (L2SkillLearn s : skills)
		{
			if (SkillHolder.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()) != null)
			{
				counts++;
				asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 0);
			}
		}

		if (counts == 0)
		{
			final int minlevel = SkillTreesParser.getInstance().getMinLevelForNewSkill(player, SkillTreesParser.getInstance().getTransformSkillTree());
			if (minlevel > 0)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
			}
		}
		else
		{
			player.sendPacket(asl);
		}
	}

	public static void main(String[] args)
	{
		new AvantGarde();
	}
}
