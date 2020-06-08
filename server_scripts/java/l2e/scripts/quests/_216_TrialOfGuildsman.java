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
package l2e.scripts.quests;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _216_TrialOfGuildsman extends Quest
{
	private static final String qn = "_216_TrialOfGuildsman";

	// Npc
	private static final int VALKON = 30103;
	private static final int NORMAN = 30210;
	private static final int ALTRAN = 30283;
	private static final int PINTER = 30298;
	private static final int DUNING = 30688;

	private static final int[] TALKERS = { VALKON, NORMAN, ALTRAN, PINTER, DUNING };

	// Mobs
	private static final int MANDRAGORA_SPROUT = 20154;
	private static final int MANDRAGORA_SAPLING = 20155;
	private static final int MANDRAGORA_BLOSSOM = 20156;
	private static final int SILENOS = 20168;
	private static final int STRAIN = 20200;
	private static final int GHOUL = 20201;
	private static final int DEAD_SEEKER = 20202;
	private static final int MANDRAGORA_SPROUT2 = 20223;
	private static final int BREKA_ORC = 20267;
	private static final int BREKA_ORC_ARCHER = 20268;
	private static final int BREKA_ORC_SHAMAN = 20269;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int BREKA_ORC_WARRIOR = 20271;
	private static final int ANT = 20079;
	private static final int ANT_CAPTAIN = 20080;
	private static final int ANT_OVERSEER = 20081;
	private static final int GRANITE_GOLEM = 20083;

	private static final int[] MOBS =
	{
		MANDRAGORA_SPROUT, MANDRAGORA_SAPLING, MANDRAGORA_BLOSSOM, SILENOS, STRAIN, GHOUL, DEAD_SEEKER,
		MANDRAGORA_SPROUT2, BREKA_ORC, BREKA_ORC_ARCHER, BREKA_ORC_SHAMAN, BREKA_ORC_OVERLORD, BREKA_ORC_WARRIOR, ANT, ANT_CAPTAIN, ANT_OVERSEER, GRANITE_GOLEM
	};

	// Quest items
	private static final int VALKONS_RECOMMEND = 3120;
	private static final int MANDRAGORA_BERRY = 3121;
	private static final int ALLTRANS_INSTRUCTIONS = 3122;
	private static final int ALLTRANS_RECOMMEND1 = 3123;
	private static final int ALLTRANS_RECOMMEND2 = 3124;
	private static final int NORMANS_INSTRUCTIONS = 3125;
	private static final int NORMANS_RECEIPT = 3126;
	private static final int DUNINGS_INSTRUCTIONS = 3127;
	private static final int DUNINGS_KEY = 3128;
	private static final int NORMANS_LIST = 3129;
	private static final int GRAY_BONE_POWDER = 3130;
	private static final int GRANITE_WHETSTONE = 3131;
	private static final int RED_PIGMENT = 3132;
	private static final int BRAIDED_YARN = 3133;
	private static final int JOURNEYMAN_GEM = 3134;
	private static final int PINTERS_INSTRUCTIONS = 3135;
	private static final int AMBER_BEAD = 3136;
	private static final int AMBER_LUMP = 3137;
	private static final int JOURNEYMAN_DECO_BEADS = 3138;
	private static final int JOURNEYMAN_RING = 3139;
	private static final int RP_JOURNEYMAN_RING = 3024;
	private static final int RP_AMBER_BEAD = 3025;

	private static final int[] QUESTITEMS =
	{
		RP_JOURNEYMAN_RING, ALLTRANS_INSTRUCTIONS, RP_JOURNEYMAN_RING, VALKONS_RECOMMEND, MANDRAGORA_BERRY,
		ALLTRANS_RECOMMEND1, DUNINGS_KEY, NORMANS_INSTRUCTIONS, NORMANS_LIST, NORMANS_RECEIPT, ALLTRANS_RECOMMEND2, PINTERS_INSTRUCTIONS,
		RP_AMBER_BEAD, AMBER_BEAD, DUNINGS_INSTRUCTIONS
	};

	// Reward
	private static final int MARK_OF_GUILDSMAN = 3119;

	// Allowed classes
	private static final int[] CLASSES = { 0x38, 0x36 };

	public _216_TrialOfGuildsman(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(VALKON);

		for (int talkId : TALKERS)
			addTalkId(talkId);

		for (int mobId : MOBS)
			addKillId(mobId);

		questItemIds = QUESTITEMS;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("1"))
		{
			htmltext = "30103-06.htm";
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(VALKONS_RECOMMEND, 1);
			st.takeItems(57, 2000);
		}
		else if (event.equalsIgnoreCase("30103_1"))
		{
			htmltext = "30103-04.htm";
		}
		else if (event.equalsIgnoreCase("30103_2"))
		{
			htmltext = st.getQuestItemsCount(57) >= 2000 ? "30103-05.htm" : "30103-05a.htm";
		}
		else if (event.equalsIgnoreCase("30103_3"))
		{
			htmltext = "30103-09a.htm";
			st.set("cond", "0");
			st.set("onlyone", "1");
			st.takeItems(JOURNEYMAN_RING, -1);
			st.takeItems(ALLTRANS_INSTRUCTIONS, 1);
			st.takeItems(RP_JOURNEYMAN_RING, 1);
			st.addExpAndSp(514739, 33384);
			st.giveItems(57, 93803);
			st.giveItems(MARK_OF_GUILDSMAN, 1);
			player.sendPacket(new SocialAction(player.getObjectId(), 3));
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		else if (event.equalsIgnoreCase("30103_4"))
		{
			st.addExpAndSp(514739, 33384);
			st.giveItems(57, 93803);
			st.giveItems(7562, 85);
			htmltext = "30103-09b.htm";
			st.set("cond", "0");
			st.set("onlyone", "1");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
			st.takeItems(JOURNEYMAN_RING, -1);
			st.takeItems(ALLTRANS_INSTRUCTIONS, 1);
			st.takeItems(RP_JOURNEYMAN_RING, 1);
			st.giveItems(MARK_OF_GUILDSMAN, 1);
			player.sendPacket(new SocialAction(player.getObjectId(), 3));
		}
		else if (event.equalsIgnoreCase("30283_1"))
		{
			htmltext = "30283-03.htm";
			st.giveItems(ALLTRANS_INSTRUCTIONS, 1);
			st.takeItems(VALKONS_RECOMMEND, 1);
			st.giveItems(RP_JOURNEYMAN_RING, 1);
			st.takeItems(MANDRAGORA_BERRY, 1);
			st.giveItems(ALLTRANS_RECOMMEND1, 1);
			st.giveItems(ALLTRANS_RECOMMEND2, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "5");
		}
		else if (event.equalsIgnoreCase("30210_1"))
		{
			htmltext = "30210-02.htm";
		}
		else if (event.equalsIgnoreCase("30210_2"))
		{
			htmltext = "30210-03.htm";
		}
		else if (event.equalsIgnoreCase("30210_3"))
		{
			htmltext = "30210-04.htm";
			st.giveItems(NORMANS_INSTRUCTIONS, 1);
			st.takeItems(ALLTRANS_RECOMMEND1, 1);
			st.giveItems(NORMANS_RECEIPT, 1);
		}
		else if (event.equalsIgnoreCase("30210_4"))
		{
			htmltext = "30210-08.htm";
		}
		else if (event.equalsIgnoreCase("30210_5"))
		{
			htmltext = "30210-09.htm";
		}
		else if (event.equalsIgnoreCase("30210_6"))
		{
			htmltext = "30210-10.htm";
			st.takeItems(DUNINGS_KEY, st.getQuestItemsCount(DUNINGS_KEY));
			st.giveItems(NORMANS_LIST, 1);
			st.takeItems(NORMANS_INSTRUCTIONS, 1);
		}
		else if (event.equalsIgnoreCase("30688_1"))
		{
			htmltext = "30688-02.htm";
			st.giveItems(DUNINGS_INSTRUCTIONS, 1);
			st.takeItems(NORMANS_RECEIPT, 1);
		}
		else if (event.equalsIgnoreCase("30298_1"))
		{
			htmltext = "30298-03.htm";
		}
		else if (event.equalsIgnoreCase("30298_2"))
		{
			if (player.getClassId().getId() == 0x36)
			{
				htmltext = "30298-04.htm";
				st.giveItems(PINTERS_INSTRUCTIONS, 1);
				st.takeItems(ALLTRANS_RECOMMEND2, 1);
			}
			else
			{
				htmltext = "30298-05.htm";
				st.giveItems(RP_AMBER_BEAD, 1);
				st.takeItems(ALLTRANS_RECOMMEND2, 1);
				st.giveItems(PINTERS_INSTRUCTIONS, 1);
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = Quest.getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(qn);
		if (st == null)
			return htmltext;

		int npcId = npc.getId();
		int id = st.getState();

		if (npcId != VALKON && id != State.STARTED)
			return htmltext;

		if (npcId == VALKON && st.getInt("cond") == 0 && st.getInt("onlyone") == 0)
		{
			if (Util.contains(CLASSES, talker.getClassId().getId()))
			{
				if (talker.getLevel() < 35)
				{
					htmltext = "30103-02.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "30103-03.htm";
			}
			else
			{
				htmltext = "30103-01.htm";
				st.exitQuest(true);
			}
		}
		else if (npcId == VALKON && st.getInt("cond") == 0 && st.getInt("onlyone") == 1)
		{
			htmltext = Quest.getAlreadyCompletedMsg(talker);
		}
		else if (npcId == VALKON && st.getInt("cond") >= 1 && st.getQuestItemsCount(VALKONS_RECOMMEND) == 1)
		{
			htmltext = "30103-07.htm";
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == VALKON && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1)
		{
			htmltext = st.getQuestItemsCount(JOURNEYMAN_RING) < 7 ? "30103-08.htm" : "30103-09.htm";
		}
		else if (npcId == ALTRAN && st.getInt("cond") >= 1 && st.getQuestItemsCount(VALKONS_RECOMMEND) == 1
				&& st.getQuestItemsCount(MANDRAGORA_BERRY) == 0)
		{
			htmltext = "30283-01.htm";
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == ALTRAN && st.getInt("cond") >= 1 && st.getQuestItemsCount(VALKONS_RECOMMEND) == 1
				&& st.getQuestItemsCount(MANDRAGORA_BERRY) == 1)
		{
			htmltext = "30283-02.htm";
		}
		else if (npcId == ALTRAN && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1)
		{
			htmltext = st.getQuestItemsCount(JOURNEYMAN_RING) < 7 ? "30283-04.htm" : "30283-05.htm";
		}
		else if (npcId == NORMAN && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1
				&& st.getQuestItemsCount(ALLTRANS_RECOMMEND1) == 1)
		{
			htmltext = "30210-01.htm";
		}
		else if (npcId == NORMAN && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) > 0
				&& st.getQuestItemsCount(NORMANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(NORMANS_RECEIPT) > 0)
		{
			htmltext = "30210-05.htm";
		}
		else if (npcId == NORMAN && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) > 0
				&& st.getQuestItemsCount(NORMANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(DUNINGS_INSTRUCTIONS) > 0)
		{
			htmltext = "30210-06.htm";
		}
		else if (npcId == NORMAN && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) > 0
				&& st.getQuestItemsCount(NORMANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(DUNINGS_KEY) >= 30)
		{
			htmltext = "30210-07.htm";
		}
		else if (npcId == NORMAN && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(NORMANS_LIST) > 0)
		{
			if (st.getQuestItemsCount(GRAY_BONE_POWDER) >= 70 && st.getQuestItemsCount(GRANITE_WHETSTONE) >= 70 && st.getQuestItemsCount(RED_PIGMENT) >= 70 && st.getQuestItemsCount(BRAIDED_YARN) >= 70)
			{
				htmltext = "30210-12.htm";
				st.takeItems(NORMANS_LIST, 1);
				st.takeItems(GRAY_BONE_POWDER, st.getQuestItemsCount(GRAY_BONE_POWDER));
				st.takeItems(GRANITE_WHETSTONE, st.getQuestItemsCount(GRANITE_WHETSTONE));
				st.takeItems(RED_PIGMENT, st.getQuestItemsCount(RED_PIGMENT));
				st.takeItems(BRAIDED_YARN, st.getQuestItemsCount(BRAIDED_YARN));
				st.giveItems(JOURNEYMAN_GEM, 7);
				if (st.getQuestItemsCount(JOURNEYMAN_DECO_BEADS) >= 7)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "6");
				}
			}
			else
				htmltext = "30210-11.htm";
		}
		else if (npcId == NORMAN && st.getInt("cond") >= 1 && st.getQuestItemsCount(NORMANS_INSTRUCTIONS) == 0 && st.getQuestItemsCount(NORMANS_LIST) == 0 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1 && (st.getQuestItemsCount(JOURNEYMAN_GEM) > 0 || st.getQuestItemsCount(JOURNEYMAN_RING) > 0))
		{
			htmltext = "30210-13.htm";
		}
		else if (npcId == DUNING && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(NORMANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(NORMANS_RECEIPT) > 0)
		{
			htmltext = "30688-01.htm";
		}
		else if (npcId == DUNING && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(NORMANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(DUNINGS_INSTRUCTIONS) > 0)
		{
			htmltext = "30688-03.htm";
		}
		else if (npcId == DUNING && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(NORMANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(DUNINGS_KEY) >= 30)
		{
			htmltext = "30688-04.htm";
		}
		else if (npcId == DUNING && st.getInt("cond") >= 1 && st.getQuestItemsCount(NORMANS_RECEIPT) == 0 && st.getQuestItemsCount(DUNINGS_INSTRUCTIONS) == 0 && st.getQuestItemsCount(DUNINGS_KEY) == 0 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1)
		{
			htmltext = "30688-01.htm";
		}
		else if (npcId == PINTER && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(ALLTRANS_RECOMMEND2) > 0)
		{
			htmltext = talker.getLevel() < 35 ? "30298-01.htm" : "30298-02.htm";
		}
		else if (npcId == PINTER && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) > 0 && st.getQuestItemsCount(PINTERS_INSTRUCTIONS) > 0)
		{
			if (st.getQuestItemsCount(AMBER_BEAD) < 70)
				htmltext = "30298-06.htm";
			else
			{
				htmltext = "30298-07.htm";
				st.takeItems(PINTERS_INSTRUCTIONS, 1);
				st.takeItems(AMBER_BEAD, st.getQuestItemsCount(AMBER_BEAD));
				st.takeItems(RP_AMBER_BEAD, st.getQuestItemsCount(RP_AMBER_BEAD));
				st.takeItems(AMBER_LUMP, st.getQuestItemsCount(AMBER_LUMP));
				st.giveItems(JOURNEYMAN_DECO_BEADS, 7);
				if (st.getQuestItemsCount(JOURNEYMAN_GEM) >= 7)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "6");
				}
			}
		}
		else if (npcId == PINTER && st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1 && st.getQuestItemsCount(PINTERS_INSTRUCTIONS) == 0 && (st.getQuestItemsCount(JOURNEYMAN_DECO_BEADS) > 0 || st.getQuestItemsCount(JOURNEYMAN_RING) > 0))
		{
			htmltext = "30298-08.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(qn);
		if (st == null)
			return null;

		int npcId = npc.getId();

		if (npcId == MANDRAGORA_SPROUT2)
		{
			if (st.getInt("cond") >= 1 && st.getQuestItemsCount(VALKONS_RECOMMEND) == 1 && st.getQuestItemsCount(MANDRAGORA_BERRY) == 0)
			{
				st.giveItems(MANDRAGORA_BERRY, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "4");
			}
		}
		else if (npcId == MANDRAGORA_SPROUT || npcId == MANDRAGORA_SAPLING || npcId == MANDRAGORA_BLOSSOM)
		{
			if (st.getInt("cond") >= 1 && st.getQuestItemsCount(VALKONS_RECOMMEND) == 1 && st.getQuestItemsCount(MANDRAGORA_BERRY) == 0)
			{
				st.giveItems(MANDRAGORA_BERRY, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "4");
			}
		}
		else if (npcId == BREKA_ORC || npcId == BREKA_ORC_ARCHER || npcId == BREKA_ORC_SHAMAN || npcId == BREKA_ORC_OVERLORD || npcId == BREKA_ORC_WARRIOR)
		{
			if (st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1 && st.getQuestItemsCount(NORMANS_INSTRUCTIONS) == 1 && st.getQuestItemsCount(DUNINGS_INSTRUCTIONS) == 1)
			{
				if (st.getQuestItemsCount(DUNINGS_KEY) <= 29)
				{
					if (st.getQuestItemsCount(DUNINGS_KEY) == 29)
					{
						st.giveItems(DUNINGS_KEY, 1);
						st.takeItems(DUNINGS_INSTRUCTIONS, 1);
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.giveItems(DUNINGS_KEY, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
		}
		else if (npcId == GHOUL || npcId == STRAIN)
		{
			if (st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1 && st.getQuestItemsCount(NORMANS_LIST) == 1 && st.getQuestItemsCount(GRAY_BONE_POWDER) < 70)
			{
				st.giveItems(GRAY_BONE_POWDER, 5);
				st.playSound(st.getQuestItemsCount(GRAY_BONE_POWDER) >= 70 ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
			}
		}
		else if (npcId == GRANITE_GOLEM)
		{
			if (st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1 && st.getQuestItemsCount(NORMANS_LIST) == 1 && st.getQuestItemsCount(GRANITE_WHETSTONE) < 70)
			{
				st.giveItems(GRANITE_WHETSTONE, 7);
				st.playSound(st.getQuestItemsCount(GRANITE_WHETSTONE) >= 70 ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
			}
		}
		else if (npcId == DEAD_SEEKER)
		{
			if (st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1 && st.getQuestItemsCount(NORMANS_LIST) == 1 && st.getQuestItemsCount(RED_PIGMENT) < 70)
			{
				st.giveItems(RED_PIGMENT, 7);
				st.playSound(st.getQuestItemsCount(RED_PIGMENT) >= 70 ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
			}
		}
		else if (npcId == SILENOS)
		{
			if (st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1 && st.getQuestItemsCount(NORMANS_LIST) == 1 && st.getQuestItemsCount(BRAIDED_YARN) < 70)
			{
				st.giveItems(BRAIDED_YARN, 10);
				st.playSound(st.getQuestItemsCount(BRAIDED_YARN) >= 70 ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
			}
		}
		else if (npcId == ANT || npcId == ANT_CAPTAIN || npcId == ANT_OVERSEER)
		{
			if (st.getInt("cond") >= 1 && st.getQuestItemsCount(ALLTRANS_INSTRUCTIONS) == 1 && st.getQuestItemsCount(PINTERS_INSTRUCTIONS) == 1)
			{
				if (st.getQuestItemsCount(AMBER_BEAD) < 70)
				{
					st.giveItems(AMBER_BEAD, 5);
					st.playSound("ItemSound.quest_itemget");
					st.playSound(st.getQuestItemsCount(AMBER_BEAD) >= 70 ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	public static void main(String[] args)
	{
		new _216_TrialOfGuildsman(216, qn, "");
	}
}