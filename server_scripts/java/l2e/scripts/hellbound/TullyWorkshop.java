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
package l2e.scripts.hellbound;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.type.L2DamageZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.util.MinionList;
import l2e.gameserver.util.Util;

public class TullyWorkshop extends Quest
{
	private static final int AGENT = 32372;
	private static final int CUBE_68 = 32467;
	private static final int DORIAN = 32373;
	private static final int DARION = 25603;
	private static final int TULLY = 25544;
	private static final int DWARVEN_GHOST = 32370;
	private static final int TOMBSTONE = 32344;
	private static final int INGENIOUS_CONTRAPTION = 32371;
	private static final int PILLAR = 18506;
	private static final int TIMETWISTER_GOLEM = 22392;

	private static final int[] SIN_WARDENS =
	{
	                22423,
	                22431
	};

	private static final int SERVANT_FIRST = 22405;
	private static final int SERVANT_LAST = 22410;
	private static final int TEMENIR = 25600;
	private static final int DRAXIUS = 25601;
	private static final int KIRETCENAH = 25602;

	private static final int[] REWARDS =
	{
	                10427,
	                10428,
	                10429,
	                10430,
	                10431
	};

	private static final int[] DEATH_COUNTS =
	{
	                7,
	                10
	};

	private static final byte STATE_OPEN = 0;
	private static final byte STATE_CLOSE = 1;

	private static final int[] TELEPORTING_MONSTERS =
	{
	                22377,
	                22378,
	                22379,
	                22383
	};

	private static final Map<Integer, int[]> TULLY_DOORLIST = new FastMap<>();
	private static final Map<Integer, int[][]> TELE_COORDS = new FastMap<>();

	protected int countdownTime;
	private int nextServantIdx = 0;
	private int killedFollowersCount = 0;
	private boolean allowServantSpawn = true;
	private boolean allowAgentSpawn = true;
	private boolean allowAgentSpawn_7th = true;
	private boolean is7thFloorAttackBegan = false;

	protected ScheduledFuture<?> _countdown = null;

	protected static List<L2Npc> postMortemSpawn = new FastList<>();
	protected static Set<Integer> brokenContraptions = new HashSet<>();
	protected static Set<Integer> rewardedContraptions = new HashSet<>();
	protected static Set<Integer> talkedContraptions = new HashSet<>();

	private final List<L2MonsterInstance> spawnedFollowers = new FastList<>();
	private final List<L2MonsterInstance> spawnedFollowerMinions = new FastList<>();
	private L2Npc spawnedAgent = null;
	private L2Spawn pillarSpawn = null;

	private final int[][] deathCount = new int[2][4];

	private static final int[][] POST_MORTEM_SPAWNLIST =
	{
	                {
	                                32371,
	                                -12524,
	                                273932,
	                                -9014,
	                                49151,
	                                0
	                },
	                {
	                                32371,
	                                -10831,
	                                273890,
	                                -9040,
	                                81895,
	                                0
	                },
	                {
	                                32371,
	                                -10817,
	                                273986,
	                                -9040,
	                                -16452,
	                                0
	                },
	                {
	                                32371,
	                                -13773,
	                                275119,
	                                -9040,
	                                8428,
	                                49151,
	                                0
	                },
	                {
	                                32371,
	                                -11547,
	                                271772,
	                                -9040,
	                                -19124,
	                                0
	                },
	                {

	                                22392,
	                                -10832,
	                                273808,
	                                -9040,
	                                0,
	                                0
	                },
	                {
	                                22392,
	                                -10816,
	                                274096,
	                                -9040,
	                                14964,
	                                0
	                },
	                {
	                                22392,
	                                -13824,
	                                275072,
	                                -9040,
	                                -24644,
	                                0
	                },
	                {
	                                22392,
	                                -11504,
	                                271952,
	                                -9040,
	                                9328,
	                                0
	                },
	                {
	                                22392,
	                                -11680,
	                                275353,
	                                -9040,
	                                0,
	                                0
	                },
	                {
	                                22392,
	                                -12388,
	                                271668,
	                                -9040,
	                                0,
	                                0
	                },
	                {
	                                32370,
	                                -11984,
	                                272928,
	                                -9040,
	                                23644,
	                                900000
	                },
	                {
	                                32370,
	                                -14643,
	                                274588,
	                                -9040,
	                                49152,
	                                0
	                },
	                {
	                                32344,
	                                -14756,
	                                274788,
	                                -9040,
	                                -13868,
	                                0
	                }
	};

	private static final int[][] SPAWNLIST_7TH_FLOOR =
	{
	                {
	                                25602,
	                                -12528,
	                                279488,
	                                -11622,
	                                16384
	                },
	                {
	                                25600,
	                                -12736,
	                                279681,
	                                -11622,
	                                0
	                },
	                {
	                                25601,
	                                -12324,
	                                279681,
	                                -11622,
	                                32768
	                },
	                {
	                                25599,
	                                -12281,
	                                281497,
	                                -11935,
	                                49151
	                },
	                {
	                                25599,
	                                -11903,
	                                281488,
	                                -11934,
	                                49151
	                },
	                {
	                                25599,
	                                -11966,
	                                277935,
	                                -11936,
	                                16384
	                },
	                {
	                                25599,
	                                -12334,
	                                277935,
	                                -11936,
	                                16384
	                },
	                {
	                                25599,
	                                -12739,
	                                277935,
	                                -11936,
	                                16384
	                },
	                {
	                                25599,
	                                -13063,
	                                277934,
	                                -11936,
	                                16384
	                },
	                {
	                                25599,
	                                -13077,
	                                281506,
	                                -11935,
	                                49151
	                },
	                {
	                                25599,
	                                -12738,
	                                281503,
	                                -11935,
	                                49151
	                },
	                {
	                                25597,
	                                -11599,
	                                281323,
	                                -11933,
	                                -23808
	                },
	                {
	                                25597,
	                                -11381,
	                                281114,
	                                -11934,
	                                -23808
	                },
	                {
	                                25597,
	                                -11089,
	                                280819,
	                                -11934,
	                                -23808
	                },
	                {
	                                25597,
	                                -10818,
	                                280556,
	                                -11934,
	                                -23808
	                },
	                {
	                                25597,
	                                -10903,
	                                278798,
	                                -11934,
	                                25680
	                },
	                {
	                                25597,
	                                -11134,
	                                278558,
	                                -11934,
	                                25680
	                },
	                {
	                                25597,
	                                -11413,
	                                278265,
	                                -11934,
	                                25680
	                },
	                {
	                                25597,
	                                -11588,
	                                278072,
	                                -11935,
	                                25680
	                },
	                {
	                                25597,
	                                -13357,
	                                278058,
	                                -11935,
	                                9068
	                },
	                {
	                                25597,
	                                -13617,
	                                278289,
	                                -11935,
	                                9068
	                },
	                {
	                                25597,
	                                -13920,
	                                278567,
	                                -11935,
	                                9068
	                },
	                {
	                                25597,
	                                -14131,
	                                278778,
	                                -11936,
	                                9068
	                },
	                {
	                                25597,
	                                -14184,
	                                280545,
	                                -11936,
	                                -7548
	                },
	                {
	                                25597,
	                                -13946,
	                                280792,
	                                -11936,
	                                -7548
	                },
	                {
	                                25597,
	                                -13626,
	                                281105,
	                                -11936,
	                                -7548
	                },
	                {
	                                25597,
	                                -13386,
	                                281360,
	                                -11935,
	                                -7548
	                },
	                {
	                                25598,
	                                -10697,
	                                280244,
	                                -11936,
	                                32768
	                },
	                {
	                                25598,
	                                -10702,
	                                279926,
	                                -11936,
	                                32768
	                },
	                {
	                                25598,
	                                -10722,
	                                279470,
	                                -11936,
	                                32768
	                },
	                {
	                                25598,
	                                -10731,
	                                279126,
	                                -11936,
	                                32768
	                },
	                {
	                                25598,
	                                -14284,
	                                279140,
	                                -11936,
	                                0
	                },
	                {
	                                25598,
	                                -14286,
	                                279464,
	                                -11936,
	                                0
	                },
	                {
	                                25598,
	                                -14290,
	                                279909,
	                                -11935,
	                                0
	                },
	                {
	                                25598,
	                                -14281,
	                                280229,
	                                -11936,
	                                0
	                }
	};

	private static final int[][] SPAWN_ZONE_DEF =
	{
	                {
	                                200012,
	                                200013,
	                                200014,
	                                200015
	                },
	                {
	                                200016,
	                                200017,
	                                200018,
	                                200019
	                }
	};

	private static final int[][] AGENT_COORDINATES =
	{
	                {
	                                -13312,
	                                279172,
	                                -13599,
	                                -20300
	                },
	                {
	                                -11696,
	                                280208,
	                                -13599,
	                                13244
	                },
	                {
	                                -13008,
	                                280496,
	                                -13599,
	                                27480
	                },
	                {
	                                -11984,
	                                278880,
	                                -13599,
	                                -4472
	                },
	                {
	                                -13312,
	                                279172,
	                                -10492,
	                                -20300
	                },
	                {
	                                -11696,
	                                280208,
	                                -10492,
	                                13244
	                },
	                {
	                                -13008,
	                                280496,
	                                -10492,
	                                27480
	                },
	                {
	                                -11984,
	                                278880,
	                                -10492,
	                                -4472
	                }
	};

	private static final int[][] SERVANT_COORDINATES =
	{
	                {
	                                -13214,
	                                278493,
	                                -13601,
	                                0
	                },
	                {
	                                -11727,
	                                280711,
	                                -13601,
	                                0
	                },
	                {
	                                -13562,
	                                280175,
	                                -13601,
	                                0
	                },
	                {
	                                -11514,
	                                278592,
	                                -13601,
	                                0
	                },
	                {
	                                -13370,
	                                278459,
	                                -10497,
	                                0
	                },
	                {
	                                -11984,
	                                280894,
	                                -10497,
	                                0
	                },
	                {
	                                -14050,
	                                280312,
	                                -10497,
	                                0
	                },
	                {
	                                -11559,
	                                278725,
	                                -10495,
	                                0
	                }
	};

	private static final int[][] CUBE_68_TELEPORTS =
	{
	                {
	                                -12176,
	                                279696,
	                                -13596
	                },
	                {
	                                -12176,
	                                279696,
	                                -10492
	                },
	                {
	                                21935,
	                                243923,
	                                11088
	                }
	};

	static
	{
		TULLY_DOORLIST.put(18445, new int[]
		{
		                19260001,
		                19260002
		});
		TULLY_DOORLIST.put(18446, new int[]
		{
			        19260003
		});
		TULLY_DOORLIST.put(18447, new int[]
		{
		                19260003,
		                19260004,
		                19260005
		});
		TULLY_DOORLIST.put(18448, new int[]
		{
		                19260006,
		                19260007
		});
		TULLY_DOORLIST.put(18449, new int[]
		{
		                19260007,
		                19260008
		});
		TULLY_DOORLIST.put(18450, new int[]
		{
			        19260010
		});
		TULLY_DOORLIST.put(18451, new int[]
		{
		                19260011,
		                19260012
		});
		TULLY_DOORLIST.put(18452, new int[]
		{
		                19260009,
		                19260011
		});
		TULLY_DOORLIST.put(18453, new int[]
		{
		                19260014,
		                19260023,
		                19260013
		});
		TULLY_DOORLIST.put(18454, new int[]
		{
		                19260015,
		                19260023
		});
		TULLY_DOORLIST.put(18455, new int[]
		{
			        19260016
		});
		TULLY_DOORLIST.put(18456, new int[]
		{
		                19260017,
		                19260018
		});
		TULLY_DOORLIST.put(18457, new int[]
		{
		                19260021,
		                19260020
		});
		TULLY_DOORLIST.put(18458, new int[]
		{
			        19260022
		});
		TULLY_DOORLIST.put(18459, new int[]
		{
			        19260018
		});
		TULLY_DOORLIST.put(18460, new int[]
		{
			        19260051
		});
		TULLY_DOORLIST.put(18461, new int[]
		{
			        19260052
		});
		TULLY_DOORLIST.put(99999, new int[]
		{
			        19260019
		});

		TELE_COORDS.put(32753, new int[][]
		{
		                {
		                                -12700,
		                                273340,
		                                -13600
		                },
		                {
		                                0,
		                                0,
		                                0
		                }
		});
		TELE_COORDS.put(32754, new int[][]
		{
		                {
		                                -13246,
		                                275740,
		                                -11936
		                },
		                {
		                                -12894,
		                                273900,
		                                -15296
		                }
		});
		TELE_COORDS.put(32755, new int[][]
		{
		                {
		                                -12798,
		                                273458,
		                                -10496
		                },
		                {
		                                -12718,
		                                273490,
		                                -13600
		                }
		});
		TELE_COORDS.put(32756, new int[][]
		{
		                {
		                                -13500,
		                                275912,
		                                -9032
		                },
		                {
		                                -13246,
		                                275740,
		                                -11936
		                }
		});
	}

	public TullyWorkshop(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(DORIAN);
		addTalkId(DORIAN);

		for (int npcId : TULLY_DOORLIST.keySet())
		{
			if (npcId != 99999)
			{
				addFirstTalkId(npcId);
				addStartNpc(npcId);
				addTalkId(npcId);
			}
		}

		for (int npcId : TELE_COORDS.keySet())
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}

		for (int monsterId : TELEPORTING_MONSTERS)
		{
			addAttackId(monsterId);
		}

		for (int monsterId : SIN_WARDENS)
		{
			addKillId(monsterId);
		}

		addStartNpc(AGENT);
		addStartNpc(CUBE_68);
		addStartNpc(INGENIOUS_CONTRAPTION);
		addStartNpc(DWARVEN_GHOST);
		addStartNpc(TOMBSTONE);
		addTalkId(AGENT);
		addTalkId(CUBE_68);
		addTalkId(INGENIOUS_CONTRAPTION);
		addTalkId(DWARVEN_GHOST);
		addTalkId(DWARVEN_GHOST);
		addTalkId(TOMBSTONE);
		addFirstTalkId(AGENT);
		addFirstTalkId(CUBE_68);
		addFirstTalkId(INGENIOUS_CONTRAPTION);
		addFirstTalkId(DWARVEN_GHOST);
		addFirstTalkId(TOMBSTONE);
		addKillId(TULLY);
		addKillId(TIMETWISTER_GOLEM);
		addKillId(TEMENIR);
		addKillId(DRAXIUS);
		addKillId(KIRETCENAH);
		addKillId(DARION);
		addKillId(PILLAR);
		addFactionCallId(TEMENIR);
		addFactionCallId(DRAXIUS);
		addFactionCallId(KIRETCENAH);

		addSpawnId(CUBE_68);
		addSpawnId(DARION);
		addSpawnId(TULLY);
		addSpawnId(PILLAR);
		addSpellFinishedId(AGENT);
		addSpellFinishedId(TEMENIR);

		for (int i = SERVANT_FIRST; i <= SERVANT_LAST; i++)
		{
			addKillId(i);
		}

		for (int i = SERVANT_FIRST; i <= SERVANT_LAST; i++)
		{
			addSpellFinishedId(i);
		}

		initDeathCounter(0);
		initDeathCounter(1);
		do7thFloorSpawn();
		doOnLoadSpawn();
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		ClassId classId = player.getClassId();
		int npcId = npc.getId();

		if (TULLY_DOORLIST.containsKey(npcId))
		{
			if (classId.equalsOrChildOf(ClassId.maestro))
			{
				return "doorman-01c.htm";
			}
			return "doorman-01.htm";
		}
		else if (npcId == INGENIOUS_CONTRAPTION)
		{
			if (talkedContraptions.contains(npc.getObjectId()))
			{
				return "32371-02.htm";
			}
			else if (!brokenContraptions.contains(npc.getObjectId()))
			{
				if (classId.equalsOrChildOf(ClassId.maestro))
				{
					return "32371-01a.htm";
				}
				return "32371-01.htm";
			}
			return "32371-04.htm";
		}
		else if (npcId == DWARVEN_GHOST)
		{
			if (postMortemSpawn.indexOf(npc) == 11)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.HA_HA_YOU_WERE_SO_AFRAID_OF_DEATH_LET_ME_SEE_IF_YOU_FIND_ME_IN_TIME_MAYBE_YOU_CAN_FIND_A_WAY));
				npc.deleteMe();
				return null;
			}
			else if (postMortemSpawn.indexOf(npc) == 12)
			{
				return "32370-01.htm";
			}
			else if (npc.isInsideRadius(-45531, 245872, -14192, 100, true, false))
			{
				return "32370-03.htm";
			}
			else
			{
				return "32370-02.htm";
			}
		}
		else if (npcId == AGENT)
		{
			final L2Party party = player.getParty();
			if ((party == null) || (party.getLeaderObjectId() != player.getObjectId()))
			{
				return "32372-01a.htm";
			}

			final int[] roomData = getRoomData(npc);
			if ((roomData[0] < 0) || (roomData[1] < 0))
			{
				return "32372-02.htm";
			}
			return "32372-01.htm";
		}
		else if (npcId == CUBE_68)
		{
			if (npc.isInsideRadius(-12752, 279696, -13596, 100, true, false))
			{
				return "32467-01.htm";
			}
			else if (npc.isInsideRadius(-12752, 279696, -10492, 100, true, false))
			{
				return "32467-02.htm";
			}
			return "32467-03.htm";
		}
		else if (npcId == TOMBSTONE)
		{
			for (int itemId : REWARDS)
			{
				if (player.getInventory().getInventoryItemCount(itemId, -1, false) > 0)
				{
					return "32344-01.htm";
				}
			}
			return "32344-01a.htm";
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == TOMBSTONE)
		{
			final L2Party party = player.getParty();
			if (party == null)
			{
				return "32344-03.htm";
			}

			boolean[] haveItems =
			{
			                false,
			                false,
			                false,
			                false,
			                false
			};
			for (L2PcInstance pl : party.getMembers())
			{
				if (pl == null)
				{
					continue;
				}

				for (int i = 0; i < REWARDS.length; i++)
				{
					if ((pl.getInventory().getInventoryItemCount(REWARDS[i], -1, false) > 0) && Util.checkIfInRange(300, pl, npc, true))
					{
						haveItems[i] = true;
						break;
					}
				}
			}

			int medalsCount = 0;
			for (boolean haveItem : haveItems)
			{
				if (haveItem)
				{
					medalsCount++;
				}
			}

			if (medalsCount == 0)
			{
				return "32344-03.htm";
			}
			else if (medalsCount < 5)
			{
				return "32344-02.htm";
			}

			for (L2PcInstance pl : party.getMembers())
			{
				if ((pl != null) && Util.checkIfInRange(6000, pl, npc, false))
				{
					pl.teleToLocation(26612, 248567, -2856);
				}
			}
		}
		return null;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		if (event.equalsIgnoreCase("disable_zone"))
		{
			final L2DamageZone dmgZone = (L2DamageZone) ZoneManager.getInstance().getZoneById(200011);
			if (dmgZone != null)
			{
				dmgZone.setEnabled(false);
			}
		}
		else if (event.equalsIgnoreCase("cube_68_spawn"))
		{
			final L2Npc spawnedNpc = addSpawn(CUBE_68, 12527, 279714, -11622, 16384, false, 0, false);
			startQuestTimer("cube_68_despawn", 600000, spawnedNpc, null);
		}
		else if (event.equalsIgnoreCase("end_7th_floor_attack"))
		{
			do7thFloorDespawn();
		}
		else if (event.equalsIgnoreCase("start_7th_floor_spawn"))
		{
			do7thFloorSpawn();
		}

		if (npc == null)
		{
			return null;
		}

		int npcId = npc.getId();
		if (event.equalsIgnoreCase("close") && TULLY_DOORLIST.containsKey(npcId))
		{
			if ((npcId == 18455) && (npc.getX() == -14610))
			{
				npcId = 99999;
			}

			final int[] doors = TULLY_DOORLIST.get(npcId);
			for (int doorId : doors)
			{
				DoorParser.getInstance().getDoor(doorId).closeMe();
			}
		}

		if (event.equalsIgnoreCase("repair_device"))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.DE_ACTIVATE_THE_ALARM));
			brokenContraptions.remove(npc.getObjectId());
		}
		else if (event.equalsIgnoreCase("despawn_servant") && !npc.isDead())
		{
			if ((npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK) && (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST) && (npc.getCurrentHp() == npc.getMaxHp()))
			{
				npc.deleteMe();
				allowServantSpawn = true;
			}
			else
			{
				startQuestTimer("despawn_servant", 180000, npc, null);
			}
		}
		else if (event.equalsIgnoreCase("despawn_agent"))
		{
			npc.deleteMe();
			allowServantSpawn = true;
			allowAgentSpawn = true;
		}
		else if (event.equalsIgnoreCase("despawn_agent_7"))
		{
			for (L2PcInstance pl : npc.getKnownList().getKnownPlayersInRadius(300))
			{
				if (pl != null)
				{
					pl.teleToLocation(-12176, 279696, -10492, true);
				}
			}

			allowAgentSpawn_7th = true;
			spawnedAgent = null;
			npc.deleteMe();
		}
		else if (event.equalsIgnoreCase("cube_68_despawn"))
		{
			for (L2PcInstance pl : npc.getKnownList().getKnownPlayersInRadius(500))
			{
				if (pl != null)
				{
					pl.teleToLocation(-12176, 279696, -10492, true);
				}
			}

			npc.deleteMe();
			startQuestTimer("start_7th_floor_spawn", 120000, null, null);
		}

		if (player == null)
		{
			return null;
		}

		if (event.equalsIgnoreCase("enter") && (npcId == DORIAN))
		{
			L2Party party = player.getParty();

			if ((party != null) && (party.getLeaderObjectId() == player.getObjectId()))
			{
				for (L2PcInstance partyMember : party.getMembers())
				{
					if (!Util.checkIfInRange(300, partyMember, npc, true))
					{
						return "32373-02.htm";
					}
				}

				for (L2PcInstance partyMember : party.getMembers())
				{
					partyMember.teleToLocation(-13400, 272827, -15300, true);
				}
				htmltext = null;

			}
			else
			{
				htmltext = "32373-02a.htm";
			}
		}
		else if (event.equalsIgnoreCase("open") && TULLY_DOORLIST.containsKey(npcId))
		{
			if ((npcId == 18455) && (npc.getX() == -14610))
			{
				npcId = 99999;
			}

			final int[] doors = TULLY_DOORLIST.get(npcId);
			for (int doorId : doors)
			{
				DoorParser.getInstance().getDoor(doorId).openMe();
			}

			startQuestTimer("close", 120000, npc, null);
			htmltext = null;
		}
		else if ((event.equalsIgnoreCase("up") || event.equalsIgnoreCase("down")) && TELE_COORDS.containsKey(npcId))
		{
			final int direction = event.equalsIgnoreCase("up") ? 0 : 1;
			final L2Party party = player.getParty();
			if (party == null)
			{
				player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			}
			else if (party.getLeaderObjectId() != player.getObjectId())
			{
				player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			}
			else if (!Util.checkIfInRange(4000, player, npc, true))
			{
				player.sendPacket(SystemMessageId.TOO_FAR_FROM_NPC);
			}
			else
			{
				final int tele[] = TELE_COORDS.get(npcId)[direction];
				for (L2PcInstance partyMember : party.getMembers())
				{
					if (Util.checkIfInRange(4000, partyMember, npc, true))
					{
						partyMember.teleToLocation(tele[0], tele[1], tele[2], true);
					}
				}
			}
			htmltext = null;
		}
		else if (npcId == INGENIOUS_CONTRAPTION)
		{
			if (event.equalsIgnoreCase("touch_device"))
			{
				int i0 = talkedContraptions.contains(npc.getObjectId()) ? 0 : 1;
				int i1 = player.getClassId().equalsOrChildOf(ClassId.maestro) ? 6 : 3;

				if (getRandom(1000) < ((i1 - i0) * 100))
				{
					talkedContraptions.add(npc.getObjectId());
					htmltext = player.getClassId().equalsOrChildOf(ClassId.maestro) ? "32371-03a.htm" : "32371-03.htm";
				}
				else
				{
					brokenContraptions.add(npc.getObjectId());
					startQuestTimer("repair_device", 60000, npc, null);
					htmltext = "32371-04.htm";
				}
			}
			else if (event.equalsIgnoreCase("take_reward"))
			{
				boolean alreadyHaveItem = false;
				for (int itemId : REWARDS)
				{
					if (player.getInventory().getInventoryItemCount(itemId, -1, false) > 0)
					{
						alreadyHaveItem = true;
						break;
					}
				}

				if (!alreadyHaveItem && !rewardedContraptions.contains(npc.getObjectId()))
				{
					int idx = postMortemSpawn.indexOf(npc);
					if ((idx > -1) && (idx < 5))
					{
						player.addItem("Quest", REWARDS[idx], 1, npc, true);
						rewardedContraptions.add(npc.getObjectId());
						if (idx != 0)
						{
							npc.deleteMe();
						}
					}
					htmltext = null;
				}
				else
				{
					htmltext = "32371-05.htm";
				}
			}
		}
		else if (npcId == AGENT)
		{
			if (event.equalsIgnoreCase("tele_to_7th_floor") && (allowAgentSpawn == false))
			{
				htmltext = null;
				L2Party party = player.getParty();
				if (party == null)
				{
					player.teleToLocation(-12501, 281397, -11936);
					if (allowAgentSpawn_7th)
					{
						if (spawnedAgent != null)
						{
							spawnedAgent.deleteMe();
						}
						spawnedAgent = addSpawn(AGENT, -12527, 279714, -11622, 16384, false, 0, false);
						allowAgentSpawn_7th = false;
					}
				}
				else
				{
					if (party.getLeaderObjectId() != player.getObjectId())
					{
						player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
					}
					else
					{
						for (L2PcInstance partyMember : party.getMembers())
						{
							if (Util.checkIfInRange(6000, partyMember, npc, true))
							{
								partyMember.teleToLocation(-12501, 281397, -11936, true);
							}
						}

						if (allowAgentSpawn_7th)
						{

							if (spawnedAgent != null)
							{
								spawnedAgent.deleteMe();
							}
							spawnedAgent = addSpawn(AGENT, -12527, 279714, -11622, 16384, false, 0, false);
							allowAgentSpawn_7th = false;
						}
					}
				}
			}
			else if (event.equalsIgnoreCase("buff") && (allowAgentSpawn_7th == false))
			{
				htmltext = null;
				final L2Party party = player.getParty();
				if (party == null)
				{
					if (!Util.checkIfInRange(400, player, npc, true))
					{
						htmltext = "32372-01b.htm";
					}
					else
					{
						npc.setTarget(player);
					}
					npc.doCast(SkillHolder.getInstance().getInfo(5526, 1));
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						if (!Util.checkIfInRange(400, partyMember, npc, true))
						{
							return "32372-01b.htm";
						}
					}

					for (L2PcInstance partyMember : party.getMembers())
					{
						npc.setTarget(partyMember);
						npc.doCast(SkillHolder.getInstance().getInfo(5526, 1));
					}
					startQuestTimer("despawn_agent_7", 60000, npc, null);
				}
			}
			else if (event.equalsIgnoreCase("refuse") && (allowAgentSpawn_7th == false))
			{
				allowAgentSpawn_7th = true;
				npc.deleteMe();
				spawnedAgent = null;

				for (L2MonsterInstance monster : spawnedFollowers)
				{
					if ((monster != null) && !monster.isDead())
					{
						if (!monster.hasMinions())
						{
							MinionList.spawnMinion(monster, 25596);
							MinionList.spawnMinion(monster, 25596);
						}

						L2PcInstance target = player.getParty() == null ? player : player.getParty().getMembers().get(getRandom(player.getParty().getMembers().size()));

						if ((target != null) && !target.isDead())
						{
							monster.addDamageHate(target, 0, 999);
							monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
						}
					}
				}

				if (!is7thFloorAttackBegan)
				{
					is7thFloorAttackBegan = true;
					startQuestTimer("end_7th_floor_attack", 1200000, null, null);
				}
			}
		}
		else if (event.equalsIgnoreCase("teleport") && (npcId == DWARVEN_GHOST))
		{
			htmltext = null;
			final L2Party party = player.getParty();
			if (party == null)
			{
				player.teleToLocation(-12176, 279696, -13596);
			}
			else
			{
				if (party.getLeaderObjectId() != player.getObjectId())
				{
					player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
					return null;
				}

				for (L2PcInstance partyMember : party.getMembers())
				{
					if (!Util.checkIfInRange(3000, partyMember, npc, true))
					{
						return "32370-01f.htm";
					}
				}

				for (L2PcInstance partyMember : party.getMembers())
				{
					if (Util.checkIfInRange(6000, partyMember, npc, true))
					{
						partyMember.teleToLocation(-12176, 279696, -13596, true);
					}
				}
			}
		}
		else if ((npcId == CUBE_68) && event.startsWith("cube68_tp"))
		{
			htmltext = null;
			int tpId = Integer.parseInt(event.substring(10));
			L2Party party = player.getParty();

			if (party != null)
			{
				if (party.getLeaderObjectId() != player.getObjectId())
				{
					player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
				}
				else if (!Util.checkIfInRange(3000, player, npc, true))
				{
					htmltext = "32467-04.htm";
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						if (Util.checkIfInRange(6000, partyMember, npc, true))
						{
							partyMember.teleToLocation(CUBE_68_TELEPORTS[tpId][0], CUBE_68_TELEPORTS[tpId][1], CUBE_68_TELEPORTS[tpId][2], true);
						}
					}
				}
			}
			else
			{
				player.teleToLocation(CUBE_68_TELEPORTS[tpId][0], CUBE_68_TELEPORTS[tpId][1], CUBE_68_TELEPORTS[tpId][2]);
			}
		}
		return htmltext;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		final int npcId = npc.getId();
		if (Arrays.binarySearch(TELEPORTING_MONSTERS, npcId) >= 0)
		{
			if (Math.abs(npc.getZ() - attacker.getZ()) > 150)
			{
				((L2MonsterInstance) npc).clearAggroList();
				attacker.teleToLocation(npc.getX() + 50, npc.getY() - 50, npc.getZ());
			}
		}
		else if (((npcId == TEMENIR) || (npcId == KIRETCENAH)) && spawnedFollowers.contains(npc))
		{
			L2MonsterInstance victim1 = spawnedFollowers.get(1);
			L2MonsterInstance victim2 = spawnedFollowers.get(0);
			L2MonsterInstance actor = spawnedFollowers.get(2);

			if ((actor != null) && !actor.isDead())
			{
				double transferringHp = actor.getMaxHp() * 0.0001;
				if ((getRandom(10000) > 1500) && (victim1 != null) && !victim1.isDead())
				{
					if ((actor.getCurrentHp() - transferringHp) > 1)
					{
						actor.setCurrentHp(actor.getCurrentHp() - transferringHp);
						victim1.setCurrentHp(victim1.getCurrentHp() + transferringHp);
					}
				}

				if ((getRandom(10000) > 3000) && (victim2 != null) && !victim2.isDead())
				{
					if ((actor.getCurrentHp() - transferringHp) > 1)
					{
						actor.setCurrentHp(actor.getCurrentHp() - transferringHp);
						victim2.setCurrentHp(victim2.getCurrentHp() + transferringHp);
					}
				}
			}
		}

		if (((npcId == TEMENIR) || (npcId == DRAXIUS)) && spawnedFollowers.contains(npc))
		{
			L2MonsterInstance victim = npcId == TEMENIR ? spawnedFollowers.get(1) : spawnedFollowers.get(2);
			L2MonsterInstance actor = spawnedFollowers.get(0);

			if ((actor != null) && (victim != null) && !actor.isDead() && !victim.isDead() && (getRandom(1000) > 333))
			{
				actor.clearAggroList();
				actor.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actor.setTarget(victim);
				actor.doCast(SkillHolder.getInstance().getInfo(4065, 11));
				victim.setCurrentHp(victim.getCurrentHp() + (victim.getMaxHp() * 0.03));
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}

	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isSummon)
	{
		int npcId = npc.getId();
		if ((npcId == TEMENIR) || (npcId == DRAXIUS) || (npcId == KIRETCENAH))
		{
			if (!((L2MonsterInstance) npc).hasMinions())
			{
				MinionList.spawnMinion((L2MonsterInstance) npc, 25596);
				MinionList.spawnMinion((L2MonsterInstance) npc, 25596);
			}

			if (!is7thFloorAttackBegan)
			{
				is7thFloorAttackBegan = true;
				startQuestTimer("end_7th_floor_attack", 1200000, null, null);

				if (spawnedAgent != null)
				{
					spawnedAgent.deleteMe();
					spawnedAgent = null;
					allowAgentSpawn_7th = true;
				}
			}
		}
		return super.onFactionCall(npc, caller, attacker, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();

		if ((npcId == TULLY) && npc.isInsideRadius(-12557, 273901, -9000, 1000, false, false))
		{
			for (int i[] : POST_MORTEM_SPAWNLIST)
			{
				L2Npc spawnedNpc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, i[5], false);
				postMortemSpawn.add(spawnedNpc);
			}

			DoorParser.getInstance().getDoor(19260051).openMe();
			DoorParser.getInstance().getDoor(19260052).openMe();

			countdownTime = 600000;
			_countdown = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CountdownTask(), 60000, 10000);
			NpcSay ns = new NpcSay(postMortemSpawn.get(0).getObjectId(), Say2.NPC_SHOUT, postMortemSpawn.get(0).getId(), NpcStringId.DETONATOR_INITIALIZATION_TIME_S1_MINUTES_FROM_NOW);
			ns.addStringParameter(Integer.toString((countdownTime / 60000)));
			postMortemSpawn.get(0).broadcastPacket(ns);
		}
		else if ((npcId == TIMETWISTER_GOLEM) && (_countdown != null))
		{
			if (getRandom(1000) >= 700)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.A_FATAL_ERROR_HAS_OCCURRED));
				if (countdownTime > 180000)
				{
					countdownTime = Math.max(countdownTime - 180000, 60000);
					if ((postMortemSpawn != null) && (postMortemSpawn.size() > 0) && (postMortemSpawn.get(0) != null) && (postMortemSpawn.get(0).getId() == INGENIOUS_CONTRAPTION))
					{
						postMortemSpawn.get(0).broadcastPacket(new NpcSay(postMortemSpawn.get(0).getObjectId(), Say2.NPC_SHOUT, postMortemSpawn.get(0).getId(), NpcStringId.ZZZZ_CITY_INTERFERENCE_ERROR_FORWARD_EFFECT_CREATED));
					}
				}
			}
			else
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.TIME_RIFT_DEVICE_ACTIVATION_SUCCESSFUL));
				if ((countdownTime > 0) && (countdownTime <= 420000))
				{
					countdownTime += 180000;
					if ((postMortemSpawn != null) && (postMortemSpawn.size() > 0) && (postMortemSpawn.get(0) != null) && (postMortemSpawn.get(0).getId() == INGENIOUS_CONTRAPTION))
					{
						postMortemSpawn.get(0).broadcastPacket(new NpcSay(postMortemSpawn.get(0).getObjectId(), Say2.NPC_SHOUT, postMortemSpawn.get(0).getId(), NpcStringId.ZZZZ_CITY_INTERFERENCE_ERROR_RECURRENCE_EFFECT_CREATED));
					}
				}
			}
		}
		else if (Arrays.binarySearch(SIN_WARDENS, npcId) >= 0)
		{
			int[] roomData = getRoomData(npc);
			if ((roomData[0] >= 0) && (roomData[1] >= 0))
			{
				deathCount[roomData[0]][roomData[1]]++;

				if (allowServantSpawn)
				{
					int max = 0;
					int floor = roomData[0];
					int room = -1;
					for (int i = 0; i < 4; i++)
					{
						if (deathCount[floor][i] > max)
						{
							max = deathCount[floor][i];
							room = i;
						}
					}

					if ((room >= 0) && (max >= DEATH_COUNTS[floor]))
					{
						int cf = floor == 1 ? 3 : 0;
						int servantId = SERVANT_FIRST + nextServantIdx + cf;
						int[] coords = SERVANT_COORDINATES[(room + cf)];
						L2Npc spawnedNpc = addSpawn(servantId, coords[0], coords[1], coords[2], 0, false, 0, false);
						allowServantSpawn = false;
						startQuestTimer("despawn_servant", 180000, spawnedNpc, null);
					}
				}
			}
		}
		else if ((npcId >= SERVANT_FIRST) && (npcId <= SERVANT_LAST))
		{
			int[] roomData = getRoomData(npc);

			if ((roomData[0] >= 0) && (roomData[1] >= 0) && allowAgentSpawn)
			{
				allowServantSpawn = true;
				if (nextServantIdx == 2)
				{
					nextServantIdx = 0;
					initDeathCounter(roomData[0]);
					if (RaidBossSpawnManager.getInstance().getRaidBossStatusId(DARION) == StatusEnum.ALIVE)
					{
						allowAgentSpawn = false;
						allowServantSpawn = false;
						int cf = roomData[0] == 1 ? 3 : 0;
						int[] coords = AGENT_COORDINATES[(roomData[1] + cf)];
						L2Npc spawnedNpc = addSpawn(AGENT, coords[0], coords[1], coords[2], 0, false, 0, false);
						startQuestTimer("despawn_agent", 180000, spawnedNpc, null);
					}
				}
				else
				{
					for (int i = 0; i < 4; i++)
					{
						if (i == roomData[1])
						{
							deathCount[roomData[0]][i] = 0;
						}
						else
						{
							deathCount[roomData[0]][i] = (deathCount[roomData[0]][i] + 1) * getRandom(3);
						}
					}

					if (getRandom(1000) > 500)
					{
						nextServantIdx++;
					}
				}
			}

			if (((npc.getId() - 22404) == 3) || ((npc.getId() - 22404) == 6))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.I_FAILED_PLEASE_FORGIVE_ME_DARION));
			}
			else
			{
				NpcSay ns = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_ILL_BE_BACK_DONT_GET_COMFORTABLE);
				ns.addStringParameter(killer.getName());
				npc.broadcastPacket(ns);
			}
		}
		else if (((npcId == TEMENIR) || (npcId == DRAXIUS) || (npcId == KIRETCENAH)) && spawnedFollowers.contains(npc))
		{
			killedFollowersCount++;
			if (killedFollowersCount >= 3)
			{
				do7thFloorDespawn();
			}
		}
		else if (npcId == DARION)
		{
			if (pillarSpawn != null)
			{
				pillarSpawn.getLastSpawn().setIsInvul(false);
			}

			handleDoorsOnDeath();
		}
		else if (npcId == PILLAR)
		{
			addSpawn(DWARVEN_GHOST, npc.getX() + 30, npc.getY() - 30, npc.getZ(), 0, false, 900000, false);
		}
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if ((npc.getId() == TULLY) && npc.isInsideRadius(-12557, 273901, -9000, 1000, true, false))
		{
			for (L2Npc spawnedNpc : postMortemSpawn)
			{
				if (spawnedNpc != null)
				{
					spawnedNpc.deleteMe();
				}
			}
			postMortemSpawn.clear();
		}
		else if (npc.getId() == DARION)
		{
			if (pillarSpawn != null)
			{
				pillarSpawn.getLastSpawn().setIsInvul(true);
			}
			handleDoorsOnRespawn();
		}
		else if (npc.getId() == PILLAR)
		{
			npc.setIsInvul(RaidBossSpawnManager.getInstance().getRaidBossStatusId(DARION) == StatusEnum.ALIVE);
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		final int npcId = npc.getId();
		final int skillId = skill.getId();

		if ((npcId == AGENT) && (skillId == 5526))
		{
			player.teleToLocation(21935, 243923, 11088, true);
		}
		else if ((npcId == TEMENIR) && (skillId == 5331))
		{
			if (!npc.isDead())
			{
				npc.setCurrentHp(npc.getCurrentHp() + (npc.getMaxHp() * 0.005));
			}
		}
		else if ((npcId >= SERVANT_FIRST) && (npcId <= SERVANT_LAST) && (skillId == 5392))
		{
			final NpcSay ns = new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.S1_THANK_YOU_FOR_GIVING_ME_YOUR_LIFE);
			ns.addStringParameter(player.getName());
			npc.broadcastPacket(ns);

			final int dmg = (int) (player.getCurrentHp() / (npc.getId() - 22404));
			player.reduceCurrentHp(dmg, null, null);
			npc.setCurrentHp((npc.getCurrentHp() + 10) - (npc.getId() - 22404));
		}
		return null;
	}

	private int[] getRoomData(L2Npc npc)
	{
		int[] ret =
		{
		                -1,
		                -1
		};
		if (npc != null)
		{
			L2Spawn spawn = npc.getSpawn();
			int x = spawn.getX();
			int y = spawn.getY();
			int z = spawn.getZ();
			for (L2ZoneType zone : ZoneManager.getInstance().getZones(x, y, z))
			{
				for (int i = 0; i < 2; i++)
				{
					for (int j = 0; j < 4; j++)
					{
						if (SPAWN_ZONE_DEF[i][j] == zone.getId())
						{
							ret[0] = i;
							ret[1] = j;
							return ret;
						}
					}
				}
			}
		}
		return ret;
	}

	private void initDeathCounter(int floor)
	{
		for (int i = 0; i < 4; i++)
		{
			deathCount[floor][i] = getRandom(DEATH_COUNTS[floor]);
		}
	}

	private void do7thFloorSpawn()
	{
		killedFollowersCount = 0;
		is7thFloorAttackBegan = false;

		for (int[] data : SPAWNLIST_7TH_FLOOR)
		{
			L2MonsterInstance monster = (L2MonsterInstance) addSpawn(data[0], data[1], data[2], data[3], data[4], false, 0, false);
			if ((data[0] == TEMENIR) || (data[0] == DRAXIUS) || (data[0] == KIRETCENAH))
			{
				spawnedFollowers.add(monster);
			}
			else
			{
				spawnedFollowerMinions.add(monster);
			}
		}
	}

	private void do7thFloorDespawn()
	{
		cancelQuestTimers("end_7th_floor_attack");
		for (L2MonsterInstance monster : spawnedFollowers)
		{
			if ((monster != null) && !monster.isDead())
			{
				monster.deleteMe();
			}
		}

		for (L2MonsterInstance monster : spawnedFollowerMinions)
		{
			if ((monster != null) && !monster.isDead())
			{
				monster.deleteMe();
			}
		}

		spawnedFollowers.clear();
		spawnedFollowerMinions.clear();
		startQuestTimer("cube_68_spawn", 60000, null, null);
	}

	private void doOnLoadSpawn()
	{
		if (RaidBossSpawnManager.getInstance().getRaidBossStatusId(TULLY) != StatusEnum.ALIVE)
		{
			for (int i = 12; i <= 13; i++)
			{
				int[] data = POST_MORTEM_SPAWNLIST[i];
				L2Npc spawnedNpc = addSpawn(data[0], data[1], data[2], data[3], data[4], false, 0, false);
				postMortemSpawn.add(spawnedNpc);
			}
		}

		pillarSpawn = addSpawn(PILLAR, 21008, 244000, 11087, 0, false, 0, false).getSpawn();
		pillarSpawn.setAmount(1);
		pillarSpawn.setRespawnDelay(1200);
		pillarSpawn.startRespawn();

		if (RaidBossSpawnManager.getInstance().getRaidBossStatusId(DARION) != StatusEnum.ALIVE)
		{
			handleDoorsOnDeath();
		}
	}

	private void handleDoorsOnDeath()
	{
		DoorParser.getInstance().getDoor(20250005).openMe();
		DoorParser.getInstance().getDoor(20250004).openMe();
		ThreadPoolManager.getInstance().scheduleGeneral(new DoorTask(new int[]
		{
		                20250006,
		                20250007
		}, STATE_OPEN), 2000);
		ThreadPoolManager.getInstance().scheduleGeneral(new DoorTask(new int[]
		{
			        20250778
		}, STATE_CLOSE), 3000);
		ThreadPoolManager.getInstance().scheduleGeneral(new DoorTask(new int[]
		{
			        20250777
		}, STATE_CLOSE), 6000);
		ThreadPoolManager.getInstance().scheduleGeneral(new DoorTask(new int[]
		{
		                20250009,
		                20250008
		}, STATE_OPEN), 11000);
	}

	private void handleDoorsOnRespawn()
	{
		DoorParser.getInstance().getDoor(20250009).closeMe();
		DoorParser.getInstance().getDoor(20250008).closeMe();
		ThreadPoolManager.getInstance().scheduleGeneral(new DoorTask(new int[]
		{
		                20250777,
		                20250778
		}, STATE_OPEN), 1000);
		ThreadPoolManager.getInstance().scheduleGeneral(new DoorTask(new int[]
		{
		                20250005,
		                20250004,
		                20250006,
		                20250007
		}, STATE_CLOSE), 4000);
	}

	protected class CountdownTask implements Runnable
	{
		@Override
		public void run()
		{
			countdownTime -= 10000;
			L2Npc npc = null;
			if ((postMortemSpawn != null) && (postMortemSpawn.size() > 0))
			{
				npc = postMortemSpawn.get(0);
			}
			if (countdownTime > 60000)
			{
				if ((countdownTime % 60000) == 0)
				{
					if ((npc != null) && (npc.getId() == INGENIOUS_CONTRAPTION))
					{
						NpcSay ns = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_MINUTES_REMAINING);
						ns.addStringParameter(Integer.toString((countdownTime / 60000)));
						npc.broadcastPacket(ns);
					}
				}
			}
			else if (countdownTime <= 0)
			{
				if (_countdown != null)
				{
					_countdown.cancel(false);
					_countdown = null;
				}

				for (L2Npc spawnedNpc : postMortemSpawn)
				{
					if ((spawnedNpc != null) && ((spawnedNpc.getId() == INGENIOUS_CONTRAPTION) || (spawnedNpc.getId() == TIMETWISTER_GOLEM)))
					{
						spawnedNpc.deleteMe();
					}
				}

				brokenContraptions.clear();
				rewardedContraptions.clear();
				talkedContraptions.clear();
				final L2DamageZone dmgZone = (L2DamageZone) ZoneManager.getInstance().getZoneById(200011);
				if (dmgZone != null)
				{
					dmgZone.setEnabled(true);
				}
				startQuestTimer("disable_zone", 300000, null, null);
			}
			else
			{
				if ((npc != null) && (npc.getId() == INGENIOUS_CONTRAPTION))
				{
					final NpcSay ns = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_SECONDS_REMAINING);
					ns.addStringParameter(Integer.toString((countdownTime / 1000)));
					npc.broadcastPacket(ns);
				}
			}
		}
	}

	private static class DoorTask implements Runnable
	{
		private final int[] _doorIds;
		private final byte _state;

		public DoorTask(int[] doorIds, byte state)
		{
			_doorIds = doorIds;
			_state = state;
		}

		@Override
		public void run()
		{
			L2DoorInstance door;
			for (int doorId : _doorIds)
			{
				door = DoorParser.getInstance().getDoor(doorId);
				if (door != null)
				{
					switch (_state)
					{
						case STATE_OPEN:
							door.openMe();
							break;
						case STATE_CLOSE:
							door.closeMe();
							break;
					}
				}
			}
		}
	}

	public static void main(String[] args)
	{
		new TullyWorkshop(-1, TullyWorkshop.class.getSimpleName(), "hellbound");
	}
}
