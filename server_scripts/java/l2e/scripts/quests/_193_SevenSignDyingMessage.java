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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class _193_SevenSignDyingMessage extends Quest
{
	private static final String qn = "_193_SevenSignDyingMessage";

	// NPC
	private static final int HOLLINT = 30191;
	private static final int CAIN = 32569;
	private static final int ERIC = 32570;
	private static final int ATHEBALDT = 30760;

	// MOB
	private static final int SHILENSEVIL = 27343;

	// ITEMS
	private static final int JACOB_NECK = 13814;
	private static final int DEADMANS_HERB = 13816;
	private static final int SCULPTURE = 14353;

	private boolean ShilensevilOnSpawn;

	public _193_SevenSignDyingMessage(int questId, String name, String descr)
	{
		super(questId, name, descr);

		ShilensevilOnSpawn = false;

		addStartNpc(HOLLINT);
		addTalkId(HOLLINT);
		addTalkId(CAIN);
		addTalkId(ERIC);
		addTalkId(ATHEBALDT);

		addKillId(SHILENSEVIL);
		questItemIds = new int[]
		{
		                JACOB_NECK,
		                DEADMANS_HERB,
		                SCULPTURE
		};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		if (npc.getId() == HOLLINT)
		{
			if (event.equalsIgnoreCase("30191-02.htm"))
			{
				st.setState((byte) 1);
				st.set("cond", "1");
				st.giveItems(JACOB_NECK, 1L);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getId() == CAIN)
		{
			if (event.equalsIgnoreCase("32569-05.htm"))
			{
				st.set("cond", "2");
				st.takeItems(JACOB_NECK, 1L);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				if (event.equalsIgnoreCase("9"))
				{
					st.takeItems(DEADMANS_HERB, 1L);
					st.set("cond", "4");
					st.playSound("ItemSound.quest_middle");
					player.showQuestMovie(9);
					return "";
				}
				if (event.equalsIgnoreCase("32569-09.htm"))
				{
					if (ShilensevilOnSpawn)
					{
						htmltext = getNoQuestMsg(player);
					}
					else
					{
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP));
						L2MonsterInstance monster = (L2MonsterInstance) addSpawn(SHILENSEVIL, 0x142c0, 47422, -3220, 0, false, 0x493e0L, true);
						monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getId(), NpcStringId.YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM));
						monster.setRunning();
						monster.addDamageHate(player, 0, 999);
						monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						ShilensevilOnSpawn = true;
						startQuestTimer("spawnS", 0x497c8L, npc, player);
						startQuestTimer("aiplayer", 30000L, npc, player);
						startQuestTimer("stopaiplayer", 0x497c8L, npc, player);
					}
				}
				else
				{
					if (event.equalsIgnoreCase("spawnS"))
					{
						ShilensevilOnSpawn = false;
						return "";
					}
					if (event.equalsIgnoreCase("aiplayer"))
					{
						npc.setTarget(player);
						npc.doCast(SkillHolder.getInstance().getInfo(1011, 18));
						startQuestTimer("aiplayer", 30000L, npc, player);
						return "";
					}
					if (event.equalsIgnoreCase("stopaiplayer"))
					{
						cancelQuestTimer("aiplayer", npc, player);
						return "";
					}
					if (event.equalsIgnoreCase("32569-13.htm"))
					{
						st.set("cond", "6");
						st.takeItems(SCULPTURE, 1L);
						st.playSound("ItemSound.quest_middle");
					}
				}
			}
		}
		else if (npc.getId() == ERIC)
		{
			if (event.equalsIgnoreCase("32570-02.htm"))
			{
				st.set("cond", "3");
				st.giveItems(DEADMANS_HERB, 1L);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if ((npc.getId() == ATHEBALDT) && event.equalsIgnoreCase("30760-02.htm"))
		{
			st.addExpAndSp(0x17d7840, 0x2625a0);
			st.unset("cond");
			st.setState((byte) 2);
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		QuestState first = player.getQuestState("_192_SevenSignSeriesOfDoubt");
		if (st == null)
		{
			return htmltext;
		}
		if (npc.getId() == HOLLINT)
		{
			switch (st.getState())
			{
				case 0:
					if ((first != null) && (first.getState() == 2) && (player.getLevel() >= 79))
					{
						htmltext = "30191-01.htm";
					}
					else
					{
						htmltext = "30191-00.htm";
						st.exitQuest(true);
					}
					break;

				case 1:
					if (st.getInt("cond") == 1)
					{
						htmltext = "30191-03.htm";
					}
					break;

				case 2:
					htmltext = getAlreadyCompletedMsg(player);
					break;
			}
		}
		else if (npc.getId() == CAIN)
		{
			if (st.getState() == 1)
			{
				switch (st.getInt("cond"))
				{
					case 1:
						htmltext = "32569-01.htm";
						break;

					case 2:
						htmltext = "32569-06.htm";
						break;

					case 3:
						htmltext = "32569-07.htm";
						break;

					case 4:
						htmltext = "32569-08.htm";
						break;

					case 5:
						htmltext = "32569-10.htm";
						break;
				}
			}
		}
		else if (npc.getId() == ERIC)
		{
			if (st.getState() == 1)
			{
				switch (st.getInt("cond"))
				{
					case 2:
						htmltext = "32570-01.htm";
						break;

					case 3:
						htmltext = "32570-03.htm";
						break;
				}
			}
		}
		else if ((npc.getId() == ATHEBALDT) && (st.getState() == 1) && (st.getInt("cond") == 6))
		{
			htmltext = "30760-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		if ((npc.getId() == SHILENSEVIL) && (st.getInt("cond") == 4))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU));
			st.giveItems(SCULPTURE, 1L);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
			ShilensevilOnSpawn = false;
			cancelQuestTimer("aiplayer", npc, player);
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String args[])
	{
		new _193_SevenSignDyingMessage(193, qn, "");
	}
}
