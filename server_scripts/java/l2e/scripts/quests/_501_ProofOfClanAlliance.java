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

import gnu.trove.map.hash.TIntIntHashMap;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.QuestTimer;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.util.Rnd;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class _501_ProofOfClanAlliance extends Quest
{
	private static final String qn = "_501_ProofOfClanAlliance";

	private static final int SIR_KRISTOF_RODEMAI = 30756;
	private static final int STATUE_OF_OFFERING = 30757;
	private static final int WITCH_ATHREA = 30758;
	private static final int WITCH_KALIS = 30759;

	private static final int POISON_OF_DEATH = 4082;
	private static final int DYING = 4083;

	private static final int HERB_OF_HARIT = 3832;
	private static final int HERB_OF_VANOR = 3833;
	private static final int HERB_OF_OEL_MAHUM = 3834;
	private static final int BLOOD_OF_EVA = 3835;
	private static final int SYMBOL_OF_LOYALTY = 3837;
	private static final int PROOF_OF_ALLIANCE = 3874;
	private static final int VOUCHER_OF_FAITH = 3873;
	private static final int ANTIDOTE_RECIPE = 3872;
	private static final int POTION_OF_RECOVERY = 3889;

	private static final int CHESTS[] =
	{
	                27173,
	                27178
	};

	private static final int CHEST_LOCS[][] =
	{
	                {
	                                102273,
	                                103433,
	                                -3512
	                },
	                {
	                                102190,
	                                103379,
	                                -3524
	                },
	                {
	                                102107,
	                                103325,
	                                -3533
	                },
	                {
	                                102024,
	                                103271,
	                                -3500
	                },
	                {
	                                102327,
	                                103350,
	                                -3511
	                },
	                {
	                                102244,
	                                103296,
	                                -3518
	                },
	                {
	                                102161,
	                                103242,
	                                -3529
	                },
	                {
	                                102078,
	                                103188,
	                                -3500
	                },
	                {
	                                102381,
	                                103267,
	                                -3538
	                },
	                {
	                                102298,
	                                103213,
	                                -3532
	                },
	                {
	                                102215,
	                                103159,
	                                -3520
	                },
	                {
	                                102132,
	                                103105,
	                                -3513
	                },
	                {
	                                102435,
	                                103184,
	                                -3515
	                },
	                {
	                                102352,
	                                103130,
	                                -3522
	                },
	                {
	                                102269,
	                                103076,
	                                -3533
	                },
	                {
	                                102186,
	                                103022,
	                                -3541
	                }
	};

	private static TIntIntHashMap MOBS = new TIntIntHashMap();

	private static boolean isArthea = false;

	public _501_ProofOfClanAlliance(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(SIR_KRISTOF_RODEMAI);
		addStartNpc(STATUE_OF_OFFERING);
		addTalkId(SIR_KRISTOF_RODEMAI);
		addTalkId(STATUE_OF_OFFERING);
		addTalkId(WITCH_ATHREA);
		addTalkId(WITCH_KALIS);

		for (int i : CHESTS)
		{
			addKillId(i);
		}

		addKillId(20685);
		addKillId(20644);
		addKillId(20576);

		MOBS.putIfAbsent(20685, HERB_OF_VANOR);
		MOBS.putIfAbsent(20644, HERB_OF_HARIT);
		MOBS.putIfAbsent(20576, HERB_OF_OEL_MAHUM);

		isArthea = false;

		questItemIds = new int[]
		{
		                HERB_OF_VANOR,
		                HERB_OF_HARIT,
		                HERB_OF_OEL_MAHUM,
		                BLOOD_OF_EVA,
		                SYMBOL_OF_LOYALTY,
		                ANTIDOTE_RECIPE,
		                VOUCHER_OF_FAITH,
		                POTION_OF_RECOVERY,
		                ANTIDOTE_RECIPE
		};
	}

	private QuestState getLeaderQuestState(L2PcInstance player)
	{
		if (player.isClanLeader())
		{
			return player.getQuestState(qn);
		}

		L2Clan clan = player.getClan();
		if (clan == null)
		{
			return null;
		}

		L2PcInstance leader = clan.getLeader().getPlayerInstance();
		if (leader == null)
		{
			return null;
		}

		QuestState leaderst = leader.getQuestState(qn);
		return leaderst;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState leaderst = null;
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}

		if (event.equalsIgnoreCase("chest_timer"))
		{
			isArthea = false;
			return "";
		}

		if (player.isClanLeader())
		{
			leaderst = st;
		}
		else
		{
			leaderst = getLeaderQuestState(player);
		}

		if (leaderst == null)
		{
			return null;
		}

		if (player.isClanLeader())
		{
			if (event.equalsIgnoreCase("30756-07.htm"))
			{
				st.playSound("ItemSound.quest_accept");
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.set("part", "1");
			}
			else if (event.equalsIgnoreCase("30759-03.htm"))
			{
				st.set("part", "2");
				st.set("cond", "2");
				st.set("dead_list", " ");
			}
			else if (event.equalsIgnoreCase("30759-07.htm"))
			{
				st.takeItems(SYMBOL_OF_LOYALTY, 1);
				st.takeItems(SYMBOL_OF_LOYALTY, 1);
				st.takeItems(SYMBOL_OF_LOYALTY, 1);
				st.giveItems(ANTIDOTE_RECIPE, 1);
				st.set("part", "3");
				st.set("cond", "3");
				st.startQuestTimer("poison_timer", 3600000);
				st.addNotifyOfDeath(player);
				SkillHolder.getInstance().getInfo(POISON_OF_DEATH, 1).getEffects(npc, player);
			}
			else if (event.equalsIgnoreCase("poison_timer"))
			{
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("30757-05.htm"))
		{
			if (player.isClanLeader())
			{
				return "������ ����� ����� ����� ������������ �����!";
			}

			if (getRandom(10) > 5)
			{
				st.giveItems(SYMBOL_OF_LOYALTY, 1);
				String[] deadlist = leaderst.get("dead_list").split(" ");
				leaderst.set("dead_list", joinStringArray(setNewValToArray(deadlist, player.getName().toLowerCase()), " "));
				return "30757-06.htm";
			}
			L2Skill skill = SkillHolder.getInstance().getInfo(DYING, 1);
			npc.setTarget(player);
			npc.doCast(skill);
			startQuestTimer(player.getName(), 4000, npc, player, false);
		}
		else if (event.equalsIgnoreCase(player.getName()))
		{
			if (player.isDead())
			{
				st.giveItems(SYMBOL_OF_LOYALTY, 1);
				String[] deadlist = leaderst.get("dead_list").split(" ");
				leaderst.set("dead_list", joinStringArray(setNewValToArray(deadlist, player.getName().toLowerCase()), " "));
			}
		}
		else if (event.equalsIgnoreCase("30758-03.htm"))
		{
			if (isArthea)
			{
				return "30758-04.htm";
			}

			isArthea = true;
			leaderst.set("part", "4");
			for (int[] element : CHEST_LOCS)
			{
				int rand = getRandom(5);
				addSpawn(CHESTS[0] + rand, element[0], element[1], element[2], 0, false, 300000);
				startQuestTimer("chest_timer", 60000, npc, player, false);
			}
		}
		else if (event.equalsIgnoreCase("30758-07.htm"))
		{
			if ((st.getQuestItemsCount(57) >= 10000) && !isArthea)
			{
				st.takeItems(57, 10000);
				return "30758-08.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);

		QuestState st = talker.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}

		int npcId = npc.getId();
		byte id = st.getState();
		L2Clan clan = talker.getClan();
		int part = st.getInt("part");

		switch (npcId)
		{
			case SIR_KRISTOF_RODEMAI:
				if (id == State.CREATED)
				{
					if (!talker.isClanLeader() || (clan == null))
					{
						return returningString("05", npcId);
					}

					int level = clan.getLevel();
					if (level <= 2)
					{
						return returningString("01", npcId);
					}
					else if (level >= 4)
					{
						return returningString("02", npcId);
					}
					else if (level == 3)
					{
						if (st.hasQuestItems(PROOF_OF_ALLIANCE))
						{
							return returningString("03", npcId);
						}

						return returningString("04", npcId);
					}
				}
				else if (id == State.STARTED)
				{
					if (!st.hasQuestItems(VOUCHER_OF_FAITH) || (part != 6))
					{
						return returningString("10", npcId);
					}

					st.playSound("ItemSound.quest_finish");
					st.takeItems(VOUCHER_OF_FAITH, 1);
					st.giveItems(PROOF_OF_ALLIANCE, 1);
					st.addExpAndSp(0, 120000);
					st.exitQuest(false);
					return returningString("09", npcId);
				}
				break;

			case WITCH_KALIS:
				if (id == State.CREATED)
				{
					QuestState leaderst = getLeaderQuestState(talker);
					if (leaderst == null)
					{
						return "";
					}

					if (talker.isClanLeader() || (leaderst == st))
					{
						return "�� ������ ��������� ���� �������� �������, ����� ������ �����! � �� ���� ������ ���!";
					}
					else if (leaderst.getState() == State.STARTED)
					{
						return returningString("12", npcId);
					}
				}
				else if (id == State.STARTED)
				{
					long symbol = st.getQuestItemsCount(SYMBOL_OF_LOYALTY);
					if (part == 1)
					{
						return returningString("01", npcId);
					}
					else if ((part == 2) && (symbol < 3))
					{
						return returningString("05", npcId);
					}
					else if (symbol == 3)
					{
						return returningString("06", npcId);
					}
					else if ((part == 5) && st.hasQuestItems(HERB_OF_HARIT) && st.hasQuestItems(HERB_OF_VANOR) && st.hasQuestItems(HERB_OF_OEL_MAHUM) && st.hasQuestItems(BLOOD_OF_EVA) && isAffected(talker, 4082))
					{
						st.giveItems(VOUCHER_OF_FAITH, 1);
						st.giveItems(POTION_OF_RECOVERY, 1);

						st.takeItems(HERB_OF_HARIT, -1);
						st.takeItems(HERB_OF_VANOR, -1);
						st.takeItems(HERB_OF_OEL_MAHUM, -1);
						st.takeItems(BLOOD_OF_EVA, -1);

						st.set("part", "6");
						st.set("cond", "4");
						QuestTimer timer = st.getQuestTimer("poison_timer");
						if (timer != null)
						{
							timer.cancel();
						}
						return returningString("08", npcId);
					}
					else if ((part == 3) || (part == 4) || (part == 5))
					{
						if (!isAffected(talker, 4082))
						{
							st.set("part", "1");
							st.takeItems(ANTIDOTE_RECIPE, -1);
							return returningString("09", npcId);
						}
						return returningString("10", npcId);
					}
					else if (part == 6)
					{
						return returningString("11", npcId);
					}
				}
				break;

			case STATUE_OF_OFFERING:
				QuestState leaderst = getLeaderQuestState(talker);
				if (leaderst == null)
				{
					return "";
				}

				byte sId = leaderst.getState();
				switch (sId)
				{
					case State.STARTED:
						if (leaderst.getInt("part") != 2)
						{
							return "";
						}

						if (talker.isClanLeader() || (leaderst == st))
						{
							return returningString("02", npcId);
						}

						if (talker.getLevel() >= 40)
						{
							String[] dlist = leaderst.get("dead_list").split(" ");
							if (dlist.length < 3)
							{
								for (String str : dlist)
								{
									if (talker.getName().equalsIgnoreCase(str))
									{
										return returningString("03", npcId);
									}
								}
								return returningString("01", npcId);
							}
							return returningString("03", npcId);
						}
						return returningString("04", npcId);
					default:
						return returningString("08", npcId);
				}

			case WITCH_ATHREA:
				QuestState leader_st = getLeaderQuestState(talker);
				if (leader_st == null)
				{
					return "";
				}

				byte s_Id = leader_st.getState();
				switch (s_Id)
				{
					case State.STARTED:
						int partA = leader_st.getInt("part");
						if ((partA == 3) && leader_st.hasQuestItems(ANTIDOTE_RECIPE) && !leader_st.hasQuestItems(BLOOD_OF_EVA))
						{
							return returningString("01", npcId);
						}
						else if (partA == 5)
						{
							return returningString("10", npcId);
						}
						else if (partA == 4)
						{
							if (leader_st.getInt("chest_wins") >= 4)
							{
								st.giveItems(BLOOD_OF_EVA, 1);
								leader_st.set("part", "5");
								return returningString("09", npcId);
							}
							return returningString("06", npcId);
						}
						break;
					default:
						break;
				}
				break;

			default:
				break;
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState leaderst = getLeaderQuestState(killer);
		if (leaderst == null)
		{
			return null;
		}

		if (leaderst.getState() != State.STARTED)
		{
			return null;
		}

		int part = leaderst.getInt("part");
		int npcId = npc.getId();

		if (MOBS.containsKey(npcId))
		{
			QuestState st = killer.getQuestState(qn);
			if (st == null)
			{
				st = newQuestState(killer);
			}

			if (st == leaderst)
			{
				return null;
			}

			if ((part >= 3) && (part < 6))
			{
				if (getRandom(10) == 0)
				{
					st.giveItems(MOBS.get(npcId), 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}

		for (int i : CHESTS)
		{
			QuestState st = killer.getQuestState(qn);
			if (st == null)
			{
				st = newQuestState(killer);
			}

			if (npcId == i)
			{
				if (Rnd.chance(25))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.BINGO));
					int wins = leaderst.getInt("chest_wins");
					if (wins < 4)
					{
						wins += 1;
						leaderst.set("chest_wins", String.valueOf(wins));
					}
					if (wins >= 4)
					{
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
				return null;
			}
		}
		return null;
	}

	@Override
	public String onDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		if (qs.getPlayer().equals(victim))
		{
			QuestTimer tm = qs.getQuestTimer("poison_timer");
			if (tm != null)
			{
				tm.cancel();
			}

			qs.exitQuest(true);

		}
		return null;
	}

	private boolean isAffected(L2PcInstance player, int skillId)
	{
		return player.getFirstEffect(skillId) != null;
	}

	private static String joinStringArray(String[] s, String sep)
	{
		String ts = "";
		for (int i = 0; i < s.length; i++)
		{
			if (i == (s.length - 1))
			{
				ts += s[i];
			}
			else
			{
				ts += s[i] + sep;
			}
		}
		return ts;
	}

	public static String[] setNewValToArray(String[] s, String s1)
	{
		String[] ts = new String[s.length + 1];
		for (int i = 0; i < s.length; i++)
		{
			ts[i] = s[i];
		}
		ts[s.length] = s1;
		return ts;
	}

	private static String returningString(String s, int npcId)
	{
		return String.valueOf(npcId) + "-" + s + ".htm";
	}

	public static void main(String[] args)
	{
		new _501_ProofOfClanAlliance(501, qn, "");
	}
}
