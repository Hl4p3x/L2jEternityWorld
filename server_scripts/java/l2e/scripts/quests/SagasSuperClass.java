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

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.util.L2FastList;
import l2e.util.L2FastMap;

public class SagasSuperClass extends Quest
{
	private static L2FastList<Quest> _scripts = new L2FastList<>();
	public String qn = SagasSuperClass.class.getSimpleName();
	public int qnu;
	public int[] NPC = {};
	public int[] Items = {};
	public int[] Mob = {};
	public int[] classid = {};
	public int[] prevclass = {};
	public int[] X = {};
	public int[] Y = {};
	public int[] Z = {};
	public String[] Text = {};
	private static final L2FastMap<L2Npc, Integer> _spawnList = new L2FastMap<>();
	
	private static int[] QuestClass[] =
	{
		{ 0x7f }, { 0x80, 0x81 }, { 0x82 }, { 0x05 }, { 0x14 }, { 0x15 },
		{ 0x02 }, { 0x03 }, { 0x2e }, { 0x30 }, { 0x33 }, { 0x34 }, { 0x08 },
		{ 0x17 }, { 0x24 }, { 0x09 }, { 0x18 }, { 0x25 }, { 0x10 }, { 0x11 },
		{ 0x1e }, { 0x0c }, { 0x1b }, { 0x28 }, { 0x0e }, { 0x1c }, { 0x29 },
		{ 0x0d }, { 0x06 }, { 0x22 }, { 0x21 }, { 0x2b }, { 0x37 }, { 0x39 }
	};

	public SagasSuperClass(int id, String name, String descr)
	{
		super(id, name, descr);

		qnu = id;
	}
	
	public void registerNPCs()
	{
		addStartNpc(NPC[0]);
		addAttackId(Mob[2]);
		addAttackId(Mob[1]);
		addSkillSeeId(Mob[1]);
		addFirstTalkId(NPC[4]);
		for (int npc : NPC)
			addTalkId(npc);
		for (int mobid : Mob)
			addKillId(mobid);
		questItemIds = Items.clone();
		questItemIds[0] = 0;
		questItemIds[2] = 0;
		for (int Archon_Minion = 21646; Archon_Minion < 21652; Archon_Minion++)
			addKillId(Archon_Minion);
		int[] Archon_Hellisha_Norm = { 18212, 18214, 18215, 18216, 18218 };
		for (int i = 0; i < Archon_Hellisha_Norm.length; i++)
			addKillId(Archon_Hellisha_Norm[i]);
		for (int Guardian_Angel = 27214; Guardian_Angel < 27217; Guardian_Angel++)
			addKillId(Guardian_Angel);
	}
	
	private static void cast(L2Npc npc, L2Character target, int skillId, int level)
	{
		target.broadcastPacket(new MagicSkillUse(target, target, skillId, level, 6000, 1));
		target.broadcastPacket(new MagicSkillUse(npc, npc, skillId, level, 6000, 1));
	}
	
	private static void autoChat(L2Npc npc, String text)
	{
		npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), text));
	}
	
	private static void addSpawn(QuestState st, L2Npc mob)
	{
		_spawnList.put(mob, st.getPlayer().getObjectId());
	}
	
	private static L2Npc FindSpawn(L2PcInstance player, L2Npc npc)
	{
		if (_spawnList.containsKey(npc) && (_spawnList.get(npc) == player.getObjectId()))
		{
			return npc;
		}
		return null;
	}
	
	private static void DeleteSpawn(QuestState st, L2Npc npc)
	{
		if (_spawnList.containsKey(npc))
		{
			_spawnList.remove(npc);
			npc.deleteMe();
		}
	}
	
	private QuestState findRightState(L2Npc npc)
	{
		L2PcInstance player = null;
		QuestState st = null;
		if (_spawnList.containsKey(npc))
		{
			player = L2World.getInstance().getPlayer(_spawnList.get(npc));
			if (player != null)
			{
				st = player.getQuestState(qn);
			}
		}
		return st;
	}
	
	private void giveHalishaMark(QuestState st2)
	{
		if (st2.getInt("spawned") == 0)
		{
			if (st2.getQuestItemsCount(Items[3]) >= 700)
			{
				st2.takeItems(Items[3], 20);
				int xx = st2.getPlayer().getX();
				int yy = st2.getPlayer().getY();
				int zz = st2.getPlayer().getZ();
				L2Npc Archon = st2.addSpawn(Mob[1], xx, yy, zz);
				addSpawn(st2, Archon);
				st2.set("spawned", "1");
				st2.startQuestTimer("Archon Hellisha has despawned", 600000, Archon);
				autoChat(Archon, Text[13].replace("PLAYERNAME", st2.getPlayer().getName()));
				((L2Attackable) Archon).addDamageHate(st2.getPlayer(), 0, 99999);
				Archon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st2.getPlayer(), null);
			}
			else
			{
				st2.giveItems(Items[3], getRandom(1, 4));
			}
		}
	}
	
	private QuestState findQuest(L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st != null)
		{
			if (qnu == 68)
			{
				for (int q = 0; q < 2; q++)
				{
					if (player.getClassId().getId() == QuestClass[1][q])
					{
						return st;
					}
				}
			}
			else if (player.getClassId().getId() == QuestClass[qnu - 67][0])
			{
				return st;
			}
		}
		return null;
	}
	
	public int getClassId(L2PcInstance player)
	{
		if (player.getClassId().getId() == 0x81)
		{
			return classid[1];
		}
		return classid[0];
	}
	
	public int getPrevClass(L2PcInstance player)
	{
		if (player.getClassId().getId() == 0x81)
		{
			if (prevclass.length == 1)
				return -1;
			return prevclass[1];
		}
		return prevclass[0];
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = null;
		if (st != null)
		{
			switch (event)
			{
				case "0-011.htm":
				case "0-012.htm":
				case "0-013.htm":
				case "0-014.htm":
				case "0-015.htm":
					htmltext = event;
					break;
				case "accept":
					st.startQuest();
					giveItems(player, Items[10], 1);
					htmltext = "0-03.htm";
					break;
				case "0-1":
					if (player.getLevel() < 76)
					{
						htmltext = "0-02.htm";
						if (st.isCreated())
						{
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "0-05.htm";
					}
					break;
				case "0-2":
					if (player.getLevel() < 76)
					{
						takeItems(player, Items[10], -1);
						st.setCond(20, true);
						htmltext = "0-08.htm";
					}
					else
					{
						takeItems(player, Items[10], -1);
						addExpAndSp(player, 2299404, 0);
						giveAdena(player, 5000000, true);
						giveItems(player, 6622, 1);
						int Class = getClassId(player);
						int prevClass = getPrevClass(player);
						player.setClassId(Class);
						if (!player.isSubClassActive() && (player.getBaseClass() == prevClass))
						{
							player.setBaseClass(Class);
						}
						player.broadcastUserInfo();
						cast(npc, player, 4339, 1);
						st.exitQuest(false);
						htmltext = "0-07.htm";
					}
					break;
				case "1-3":
					st.setCond(3);
					htmltext = "1-05.htm";
					break;
				case "1-4":
					st.setCond(4);
					takeItems(player, Items[0], 1);
					if (Items[11] != 0)
					{
						takeItems(player, Items[11], 1);
					}
					giveItems(player, Items[1], 1);
					htmltext = "1-06.htm";
					break;
				case "2-1":
					st.setCond(2);
					htmltext = "2-05.htm";
					break;
				case "2-2":
					st.setCond(5);
					takeItems(player, Items[1], 1);
					giveItems(player, Items[4], 1);
					htmltext = "2-06.htm";
					break;
				case "3-5":
					htmltext = "3-07.htm";
					break;
				case "3-6":
					st.setCond(11);
					htmltext = "3-02.htm";
					break;
				case "3-7":
					st.setCond(12);
					htmltext = "3-03.htm";
					break;
				case "3-8":
					st.setCond(13);
					takeItems(player, Items[2], 1);
					giveItems(player, Items[7], 1);
					htmltext = "3-08.htm";
					break;
				case "4-1":
					htmltext = "4-010.htm";
					break;
				case "4-2":
					giveItems(player, Items[9], 1);
					st.setCond(18, true);
					htmltext = "4-011.htm";
					break;
				case "4-3":
					giveItems(player, Items[9], 1);
					st.setCond(18, true);
					autoChat(npc, Text[13].replace("PLAYERNAME", player.getName()));
					st.set("Quest0", "0");
					cancelQuestTimer("Mob_2 has despawned", npc, player);
					DeleteSpawn(st, npc);
					return null;
				case "5-1":
					st.setCond(6, true);
					takeItems(player, Items[4], 1);
					cast(npc, player, 4546, 1);
					htmltext = "5-02.htm";
					break;
				case "6-1":
					st.setCond(8, true);
					takeItems(player, Items[5], 1);
					cast(npc, player, 4546, 1);
					htmltext = "6-03.htm";
					break;
				case "7-1":
					if (st.getInt("spawned") == 1)
					{
						htmltext = "7-03.htm";
					}
					else if (st.getInt("spawned") == 0)
					{
						L2Npc Mob_1 = st.addSpawn(Mob[0], X[0], Y[0], Z[0]);
						st.set("spawned", "1");
						st.startQuestTimer("Mob_1 Timer 1", 500, Mob_1);
						st.startQuestTimer("Mob_1 has despawned", 300000, Mob_1);
						addSpawn(st, Mob_1);
						htmltext = "7-02.htm";
					}
					else
					{
						htmltext = "7-04.htm";
					}
					break;
				case "7-2":
					st.setCond(10, true);
					takeItems(player, Items[6], 1);
					cast(npc, player, 4546, 1);
					htmltext = "7-06.htm";
					break;
				case "8-1":
					st.setCond(14, true);
					takeItems(player, Items[7], 1);
					cast(npc, player, 4546, 1);
					htmltext = "8-02.htm";
					break;
				case "9-1":
					st.setCond(17, true);
					takeItems(player, Items[8], 1);
					cast(npc, player, 4546, 1);
					htmltext = "9-03.htm";
					break;
				case "10-1":
					if (st.getInt("Quest0") == 0)
					{
						L2Npc Mob_3 = st.addSpawn(Mob[2], X[1], Y[1], Z[1]);
						L2Npc Mob_2 = st.addSpawn(NPC[4], X[2], Y[2], Z[2]);
						addSpawn(st, Mob_3);
						addSpawn(st, Mob_2);
						st.set("Mob_2", String.valueOf(Mob_2.getObjectId()));
						st.set("Quest0", "1");
						st.set("Quest1", "45");
						st.startRepeatingQuestTimer("Mob_3 Timer 1", 500, Mob_3);
						st.startQuestTimer("Mob_3 has despawned", 59000, Mob_3);
						st.startQuestTimer("Mob_2 Timer 1", 500, Mob_2);
						st.startQuestTimer("Mob_2 has despawned", 60000, Mob_2);
						htmltext = "10-02.htm";
					}
					else if (st.getInt("Quest1") == 45)
					{
						htmltext = "10-03.htm";
					}
					else
					{
						htmltext = "10-04.htm";
					}
					break;
				case "10-2":
					st.setCond(19, true);
					takeItems(player, Items[9], 1);
					cast(npc, player, 4546, 1);
					htmltext = "10-06.htm";
					break;
				case "11-9":
					st.setCond(15);
					htmltext = "11-03.htm";
					break;
				case "Mob_1 Timer 1":
					autoChat(npc, Text[0].replace("PLAYERNAME", player.getName()));
					return null;
				case "Mob_1 has despawned":
					autoChat(npc, Text[1].replace("PLAYERNAME", player.getName()));
					st.set("spawned", "0");
					DeleteSpawn(st, npc);
					return null;
				case "Archon Hellisha has despawned":
					autoChat(npc, Text[6].replace("PLAYERNAME", player.getName()));
					st.set("spawned", "0");
					DeleteSpawn(st, npc);
					return null;
				case "Mob_3 Timer 1":
					L2Npc Mob_2 = FindSpawn(player, (L2Npc) L2World.getInstance().findObject(st.getInt("Mob_2")));
					if (npc.getKnownList().knowsObject(Mob_2))
					{
						((L2Attackable) npc).addDamageHate(Mob_2, 0, 99999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, Mob_2, null);
						Mob_2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc, null);
						autoChat(npc, Text[14].replace("PLAYERNAME", player.getName()));
						cancelQuestTimer("Mob_3 Timer 1", npc, player);
					}
					return null;
				case "Mob_3 has despawned":
					autoChat(npc, Text[15].replace("PLAYERNAME", player.getName()));
					st.set("Quest0", "2");
					DeleteSpawn(st, npc);
					return null;
				case "Mob_2 Timer 1":
					autoChat(npc, Text[7].replace("PLAYERNAME", player.getName()));
					st.startQuestTimer("Mob_2 Timer 2", 1500, npc);
					if (st.getInt("Quest1") == 45)
					{
						st.set("Quest1", "0");
					}
					return null;
				case "Mob_2 Timer 2":
					autoChat(npc, Text[8].replace("PLAYERNAME", player.getName()));
					st.startQuestTimer("Mob_2 Timer 3", 10000, npc);
					return null;
				case "Mob_2 Timer 3":
					if (st.getInt("Quest0") == 0)
					{
						st.startQuestTimer("Mob_2 Timer 3", 13000, npc);
						if (getRandom(2) == 0)
						{
							autoChat(npc, Text[9].replace("PLAYERNAME", player.getName()));
						}
						else
						{
							autoChat(npc, Text[10].replace("PLAYERNAME", player.getName()));
						}
					}
					return null;
				case "Mob_2 has despawned":
					st.set("Quest1", String.valueOf(st.getInt("Quest1") + 1));
					if ((st.getInt("Quest0") == 1) || (st.getInt("Quest0") == 2) || (st.getInt("Quest1") > 3))
					{
						st.set("Quest0", "0");
						if (st.getInt("Quest0") == 1)
						{
							autoChat(npc, Text[11].replace("PLAYERNAME", player.getName()));
						}
						else
						{
							autoChat(npc, Text[12].replace("PLAYERNAME", player.getName()));
						}
						DeleteSpawn(st, npc);
					}
					else
					{
						st.startQuestTimer("Mob_2 has despawned", 1000, npc);
					}
					return null;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st != null)
		{
			int npcId = npc.getId();
			if ((npcId == NPC[0]) && st.isCompleted())
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
			else if (player.getClassId().getId() == getPrevClass(player))
			{
				switch (st.getCond())
				{
					case 0:
						if (npcId == NPC[0])
						{
							htmltext = "0-01.htm";
						}
						break;
					case 1:
						if (npcId == NPC[0])
						{
							htmltext = "0-04.htm";
						}
						else if (npcId == NPC[2])
						{
							htmltext = "2-01.htm";
						}
						break;
					case 2:
						if (npcId == NPC[2])
						{
							htmltext = "2-02.htm";
						}
						else if (npcId == NPC[1])
						{
							htmltext = "1-01.htm";
						}
						break;
					case 3:
						if ((npcId == NPC[1]) && hasQuestItems(player, Items[0]))
						{
							if ((Items[11] == 0) || hasQuestItems(player, Items[11]))
							{
								htmltext = "1-03.htm";
							}
							else
							{
								htmltext = "1-02.htm";
							}
						}
						break;
					case 4:
						if (npcId == NPC[1])
						{
							htmltext = "1-04.htm";
						}
						else if (npcId == NPC[2])
						{
							htmltext = "2-03.htm";
						}
						break;
					case 5:
						if (npcId == NPC[2])
						{
							htmltext = "2-04.htm";
						}
						else if (npcId == NPC[5])
						{
							htmltext = "5-01.htm";
						}
						break;
					case 6:
						if (npcId == NPC[5])
						{
							htmltext = "5-03.htm";
						}
						else if (npcId == NPC[6])
						{
							htmltext = "6-01.htm";
						}
						break;
					case 7:
						if (npcId == NPC[6])
						{
							htmltext = "6-02.htm";
						}
						break;
					case 8:
						if (npcId == NPC[6])
						{
							htmltext = "6-04.htm";
						}
						else if (npcId == NPC[7])
						{
							htmltext = "7-01.htm";
						}
						break;
					case 9:
						if (npcId == NPC[7])
						{
							htmltext = "7-05.htm";
						}
						break;
					case 10:
						if (npcId == NPC[7])
						{
							htmltext = "7-07.htm";
						}
						else if (npcId == NPC[3])
						{
							htmltext = "3-01.htm";
						}
						break;
					case 11:
					case 12:
						if (npcId == NPC[3])
						{
							if (hasQuestItems(player, Items[2]))
							{
								htmltext = "3-05.htm";
							}
							else
							{
								htmltext = "3-04.htm";
							}
						}
						break;
					case 13:
						if (npcId == NPC[3])
						{
							htmltext = "3-06.htm";
						}
						else if (npcId == NPC[8])
						{
							htmltext = "8-01.htm";
						}
						break;
					case 14:
						if (npcId == NPC[8])
						{
							htmltext = "8-03.htm";
						}
						else if (npcId == NPC[11])
						{
							htmltext = "11-01.htm";
						}
						break;
					case 15:
						if (npcId == NPC[11])
						{
							htmltext = "11-02.htm";
						}
						else if (npcId == NPC[9])
						{
							htmltext = "9-01.htm";
						}
						break;
					case 16:
						if (npcId == NPC[9])
						{
							htmltext = "9-02.htm";
						}
						break;
					case 17:
						if (npcId == NPC[9])
						{
							htmltext = "9-04.htm";
						}
						else if (npcId == NPC[10])
						{
							htmltext = "10-01.htm";
						}
						break;
					case 18:
						if (npcId == NPC[10])
						{
							htmltext = "10-05.htm";
						}
						break;
					case 19:
						if (npcId == NPC[10])
						{
							htmltext = "10-07.htm";
						}
						else if (npcId == NPC[0])
						{
							htmltext = "0-06.htm";
						}
						break;
					case 20:
						if (npcId == NPC[0])
						{
							if (player.getLevel() >= 76)
							{
								htmltext = "0-09.htm";
								if ((getClassId(player) < 131) || (getClassId(player) > 135))
								{
									st.exitQuest(false);
									addExpAndSp(player, 2299404, 0);
									giveAdena(player, 5000000, true);
									giveItems(player, 6622, 1);
									int classId = getClassId(player);
									int prevClass = getPrevClass(player);
									player.setClassId(classId);
									if (!player.isSubClassActive() && (player.getBaseClass() == prevClass))
									{
										player.setBaseClass(classId);
									}
									player.broadcastUserInfo();
									cast(npc, player, 4339, 1);
								}
							}
							else
							{
								htmltext = "0-010.htm";
							}
						}
						break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getId();
		if (st != null)
		{
			if (npcId == NPC[4])
			{
				int cond = st.getCond();
				if (cond == 17)
				{
					QuestState st2 = findRightState(npc);
					if (st2 != null)
					{
						player.setLastQuestNpcObject(npc.getObjectId());
						int tab = st.getInt("Tab");
						int quest0 = st.getInt("Quest0");
						
						if (st == st2)
						{
							if (tab == 1)
							{
								if (quest0 == 0)
								{
									htmltext = "4-04.htm";
								}
								else if (quest0 == 1)
								{
									htmltext = "4-06.htm";
								}
							}
							else if (quest0 == 0)
							{
								htmltext = "4-01.htm";
							}
							else if (quest0 == 1)
							{
								htmltext = "4-03.htm";
							}
						}
						else if (tab == 1)
						{
							if (quest0 == 0)
							{
								htmltext = "4-05.htm";
							}
							else if (quest0 == 1)
							{
								htmltext = "4-07.htm";
							}
						}
						else if (quest0 == 0)
						{
							htmltext = "4-02.htm";
						}
					}
				}
				else if (cond == 18)
				{
					htmltext = "4-08.htm";
				}
			}
		}
		if (htmltext == "")
		{
			npc.showChatWindow(player);
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		QuestState st2 = findRightState(npc);
		if (st2 != null)
		{
			int cond = st2.getCond();
			QuestState st = player.getQuestState(qn);
			int npcId = npc.getId();
			if ((npcId == Mob[2]) && (st == st2) && (cond == 17))
			{
				int quest0 = st.getInt("Quest0") + 1;
				if (quest0 == 1)
				{
					autoChat(npc, Text[16].replace("PLAYERNAME", player.getName()));
				}
				
				if (quest0 > 15)
				{
					quest0 = 1;
					autoChat(npc, Text[17].replace("PLAYERNAME", player.getName()));
					cancelQuestTimer("Mob_3 has despawned", npc, st2.getPlayer());
					st.set("Tab", "1");
					DeleteSpawn(st, npc);
				}
				
				st.set("Quest0", Integer.toString(quest0));
			}
			else if ((npcId == Mob[1]) && (cond == 15))
			{
				if ((st != st2) || ((st == st2) && player.isInParty()))
				{
					autoChat(npc, Text[5].replace("PLAYERNAME", player.getName()));
					cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
					st2.set("spawned", "0");
					DeleteSpawn(st2, npc);
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (_spawnList.containsKey(npc) && (_spawnList.get(npc) != player.getObjectId()))
		{
			L2PcInstance quest_player = (L2PcInstance) L2World.getInstance().findObject(_spawnList.get(npc));
			if (quest_player == null)
			{
				return null;
			}
			
			for (L2Object obj : targets)
			{
				if ((obj == quest_player) || (obj == npc))
				{
					QuestState st2 = findRightState(npc);
					if (st2 == null)
					{
						return null;
					}
					autoChat(npc, Text[5].replace("PLAYERNAME", player.getName()));
					cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
					st2.set("spawned", "0");
					DeleteSpawn(st2, npc);
				}
			}
		}
		return super.onSkillSee(npc, player, skill, targets, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcId = npc.getId();
		QuestState st = player.getQuestState(qn);
		for (int Archon_Minion = 21646; Archon_Minion < 21652; Archon_Minion++)
		{
			if (npcId == Archon_Minion)
			{
				L2Party party = player.getParty();
				if (party != null)
				{
					L2FastList<QuestState> PartyQuestMembers = new L2FastList<>();
					for (L2PcInstance player1 : party.getMembers())
					{
						QuestState st1 = findQuest(player1);
						if ((st1 != null) && player1.isInsideRadius(player, Config.ALT_PARTY_RANGE2, false, false))
						{
							if (st1.isCond(15))
							{
								PartyQuestMembers.add(st1);
							}
						}
					}
					if (PartyQuestMembers.size() > 0)
					{
						QuestState st2 = PartyQuestMembers.get(getRandom(PartyQuestMembers.size()));
						giveHalishaMark(st2);
					}
				}
				else
				{
					QuestState st1 = findQuest(player);
					if (st1 != null)
					{
						if (st1.isCond(15))
						{
							giveHalishaMark(st1);
						}
					}
				}
				return super.onKill(npc, player, isSummon);
			}
		}
		
		int[] Archon_Hellisha_Norm =
		{
			18212,
			18214,
			18215,
			18216,
			18218
		};
		for (int element : Archon_Hellisha_Norm)
		{
			if (npcId == element)
			{
				QuestState st1 = findQuest(player);
				if (st1 != null)
				{
					if (st1.isCond(15))
					{
						autoChat(npc, Text[4].replace("PLAYERNAME", st1.getPlayer().getName()));
						st1.giveItems(Items[8], 1);
						st1.takeItems(Items[3], -1);
						st1.setCond(16, true);
					}
				}
				return super.onKill(npc, player, isSummon);
			}
		}
		
		for (int Guardian_Angel = 27214; Guardian_Angel < 27217; Guardian_Angel++)
		{
			if (npcId == Guardian_Angel)
			{
				QuestState st1 = findQuest(player);
				if ((st1 != null) && st1.isCond(6))
				{
					int kills = st1.getInt("kills");
					if (kills < 9)
					{
						st1.set("kills", Integer.toString(kills + 1));
					}
					else
					{
						st1.giveItems(Items[5], 1);
						st.setCond(7, true);
					}
				}
				return super.onKill(npc, player, isSummon);
			}
		}
		if ((st != null) && (npcId != Mob[2]))
		{
			QuestState st2 = findRightState(npc);
			if (st2 != null)
			{
				int cond = st.getCond();
				if ((npcId == Mob[0]) && (cond == 8))
				{
					if (!player.isInParty())
					{
						if (st == st2)
						{
							autoChat(npc, Text[12].replace("PLAYERNAME", player.getName()));
							giveItems(player, Items[6], 1);
							st.setCond(9, true);
						}
					}
					cancelQuestTimer("Mob_1 has despawned", npc, st2.getPlayer());
					st2.set("spawned", "0");
					DeleteSpawn(st2, npc);
				}
				else if ((npcId == Mob[1]) && (cond == 15))
				{
					if (!player.isInParty())
					{
						if (st == st2)
						{
							autoChat(npc, Text[4].replace("PLAYERNAME", player.getName()));
							giveItems(player, Items[8], 1);
							takeItems(player, Items[3], -1);
							st.setCond(16, true);
						}
						else
						{
							autoChat(npc, Text[5].replace("PLAYERNAME", player.getName()));
						}
					}
					cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
					st2.set("spawned", "0");
					DeleteSpawn(st2, npc);
				}
			}
		}
		else if (npcId == Mob[0])
		{
			st = findRightState(npc);
			if (st != null)
			{
				cancelQuestTimer("Mob_1 has despawned", npc, st.getPlayer());
				st.set("spawned", "0");
				DeleteSpawn(st, npc);
			}
		}
		else if (npcId == Mob[1])
		{
			st = findRightState(npc);
			if (st != null)
			{
				cancelQuestTimer("Archon Hellisha has despawned", npc, st.getPlayer());
				st.set("spawned", "0");
				DeleteSpawn(st, npc);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public boolean unload()
	{
		if (_scripts.size() == 0)
			return super.unload();
		
		for (int index = 0; index < _scripts.size(); index++)
		{
			if (_scripts.get(index) == null)
				continue;
			QuestManager.getInstance().removeQuest(_scripts.get(index));
		}
		_scripts.clear();
		
		return super.unload();
	}
	
	public static void main(String[] args)
	{
		new SagasSuperClass(-1, SagasSuperClass.class.getSimpleName(), "Saga's SuperClass");
		
		_scripts.add(new _087_SagaOfEvasSaint());
		_scripts.add(new _071_SagaOfEvasTemplar());
		_scripts.add(new _079_SagaOfTheAdventurer());
		_scripts.add(new _091_SagaOfTheArcanaLord());
		_scripts.add(new _088_SagaOfTheArchmage());
		_scripts.add(new _085_SagaOfTheCardinal());
		_scripts.add(new _077_SagaOfTheDominator());
		_scripts.add(new _067_SagaOfTheDoombringer());
		_scripts.add(new _078_SagaOfTheDoomcryer());
		_scripts.add(new _074_SagaOfTheDreadnoughts());
		_scripts.add(new _073_SagaOfTheDuelist());
		_scripts.add(new _092_SagaOfTheElementalMaster());
		_scripts.add(new _099_SagaOfTheFortuneSeeker());
		_scripts.add(new _081_SagaOfTheGhostHunter());
		_scripts.add(new _084_SagaOfTheGhostSentinel());
		_scripts.add(new _076_SagaOfTheGrandKhavatari());
		_scripts.add(new _095_SagaOfTheHellKnight());
		_scripts.add(new _086_SagaOfTheHierophant());
		_scripts.add(new _100_SagaOfTheMaestro());
		_scripts.add(new _083_SagaOfTheMoonlightSentinel());
		_scripts.add(new _089_SagaOfTheMysticMuse());
		_scripts.add(new _070_SagaOfThePhoenixKnight());
		_scripts.add(new _082_SagaOfTheSagittarius());
		_scripts.add(new _098_SagaOfTheShillienSaint());
		_scripts.add(new _097_SagaOfTheShillienTemplar());
		_scripts.add(new _068_SagaOfTheSoulHound());
		_scripts.add(new _094_SagaOfTheSoultaker());
		_scripts.add(new _096_SagaOfTheSpectralDancer());
		_scripts.add(new _093_SagaOfTheSpectralMaster());
		_scripts.add(new _090_SagaOfTheStormScreamer());
		_scripts.add(new _072_SagaOfTheSwordMuse());
		_scripts.add(new _075_SagaOfTheTitan());
		_scripts.add(new _069_SagaOfTheTrickster());
		_scripts.add(new _080_SagaOfTheWindRider());
	}
}