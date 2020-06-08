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
package l2e.scripts.instances;

import java.util.Calendar;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

/**
 * Based on L2J Eternity-World
 */
public class SufferingHallDefence extends Quest
{
	private class DHSWorld extends InstanceWorld
	{
		public Map<L2Npc, Boolean> npcList = new FastMap<>();
		public FastList<L2Npc> tumor = new FastList<>();
		public int tumorIndex = 300;
		public int tumorcount = 0;
		public L2Npc klodekus = null;
		public L2Npc klanikus = null;
		public boolean isBossesAttacked = false;
		public long[] storeTime =
		{
		                0,
		                0
		};

		public synchronized void TumorIndex(int value)
		{
			tumorIndex += value;
		}

		public synchronized void TumorCount(int value)
		{
			tumorcount += value;
		}

		protected void calcRewardItemId()
		{
			Long finishDiff = storeTime[1] - storeTime[0];
			if (finishDiff < 1260000)
			{
				tag = 13777;
			}
			else if (finishDiff < 1380000)
			{
				tag = 13778;
			}
			else if (finishDiff < 1500000)
			{
				tag = 13779;
			}
			else if (finishDiff < 1620000)
			{
				tag = 13780;
			}
			else if (finishDiff < 1740000)
			{
				tag = 13781;
			}
			else if (finishDiff < 1860000)
			{
				tag = 13782;
			}
			else if (finishDiff < 1980000)
			{
				tag = 13783;
			}
			else if (finishDiff < 2100000)
			{
				tag = 13784;
			}
			else if (finishDiff < 2220000)
			{
				tag = 13785;
			}
			else
			{
				tag = 13786;
			}
		}

		public DHSWorld()
		{
			tag = -1;
		}
	}

	private static final String qn = "SufferingHallDefence";
	private static final int INSTANCEID = 116;

	private static final int MOUTHOFEKIMUS = 32537;
	private static final int TEPIOS = 32530;

	private static final int KLODEKUS = 25665;
	private static final int KLANIKUS = 25666;
	private static final int TUMOR_ALIVE = 18704;
	private static final int TUMOR_DEAD = 18705;

	protected int tumorIndex = 300;
	private boolean doCountCoffinNotifications = false;

	private static final int[] TUMOR_MOBIDS =
	{
	                22509,
	                22510,
	                22511,
	                22512,
	                22513,
	                22514,
	                22515
	};
	private static final int[] TWIN_MOBIDS =
	{
	                22509,
	                22510,
	                22511,
	                22512,
	                22513
	};

	private static final int[][] ROOM_1_MOBS =
	{
	                {
	                                22509,
	                                -173712,
	                                217838,
	                                -9559
	                },
	                {
	                                22509,
	                                -173489,
	                                218281,
	                                -9557
	                },
	                {
	                                22509,
	                                -173824,
	                                218389,
	                                -9558
	                },
	                {
	                                22510,
	                                -174018,
	                                217970,
	                                -9559
	                },
	                {
	                                22510,
	                                -173382,
	                                218198,
	                                -9547
	                }
	};

	private static final int[][] ROOM_2_MOBS =
	{
	                {
	                                22511,
	                                -173456,
	                                217976,
	                                -9556
	                },
	                {
	                                22511,
	                                -173673,
	                                217951,
	                                -9547
	                },
	                {
	                                22509,
	                                -173622,
	                                218233,
	                                -9547
	                },
	                {
	                                22510,
	                                -173775,
	                                218218,
	                                -9545
	                },
	                {
	                                22510,
	                                -173660,
	                                217980,
	                                -9542
	                },
	                {
	                                22510,
	                                -173712,
	                                217838,
	                                -9559
	                }
	};

	private static final int[][] ROOM_3_MOBS =
	{
	                {
	                                22512,
	                                -173489,
	                                218281,
	                                -9557
	                },
	                {
	                                22512,
	                                -173824,
	                                218389,
	                                -9558
	                },
	                {
	                                22512,
	                                -174018,
	                                217970,
	                                -9559
	                },
	                {
	                                22509,
	                                -173382,
	                                218198,
	                                -9547
	                },
	                {
	                                22511,
	                                -173456,
	                                217976,
	                                -9556
	                },
	                {
	                                22511,
	                                -173673,
	                                217951,
	                                -9547
	                },
	                {
	                                22510,
	                                -173622,
	                                218233,
	                                -9547
	                },
	                {
	                                22510,
	                                -173775,
	                                218218,
	                                -9545
	                }
	};

	private static final int[][] ROOM_4_MOBS =
	{
	                {
	                                22514,
	                                -173660,
	                                217980,
	                                -9542
	                },
	                {
	                                22514,
	                                -173712,
	                                217838,
	                                -9559
	                },
	                {
	                                22514,
	                                -173489,
	                                218281,
	                                -9557
	                },
	                {
	                                22513,
	                                -173824,
	                                218389,
	                                -9558
	                },
	                {
	                                22513,
	                                -174018,
	                                217970,
	                                -9559
	                },
	                {
	                                22511,
	                                -173382,
	                                218198,
	                                -9547
	                },
	                {
	                                22511,
	                                -173456,
	                                217976,
	                                -9556
	                },
	                {
	                                22512,
	                                -173673,
	                                217951,
	                                -9547
	                },
	                {
	                                22512,
	                                -173622,
	                                218233,
	                                -9547
	                }
	};

	private static final int[][] ROOM_5_MOBS =
	{
	                {
	                                22512,
	                                -173775,
	                                218218,
	                                -9545
	                },
	                {
	                                22512,
	                                -173660,
	                                217980,
	                                -9542
	                },
	                {
	                                22512,
	                                -173712,
	                                217838,
	                                -9559
	                },
	                {
	                                22513,
	                                -173489,
	                                218281,
	                                -9557
	                },
	                {
	                                22513,
	                                -173824,
	                                218389,
	                                -9558
	                },
	                {
	                                22514,
	                                -174018,
	                                217970,
	                                -9559
	                },
	                {
	                                22514,
	                                -173382,
	                                218198,
	                                -9547
	                },
	                {
	                                22514,
	                                -173456,
	                                217976,
	                                -9556
	                },
	                {
	                                22515,
	                                -173673,
	                                217951,
	                                -9547
	                },
	                {
	                                22515,
	                                -173622,
	                                218233,
	                                -9547
	                }
	};

	private static final int[][] TUMOR_SPAWNS =
	{
	                {
	                                -173727,
	                                218109,
	                                -9536
	                },
	                {
	                                -173727,
	                                218109,
	                                -9536
	                },
	                {
	                                -173727,
	                                218109,
	                                -9536
	                },
	                {
	                                -173727,
	                                218109,
	                                -9536
	                },
	                {
	                                -173727,
	                                218109,
	                                -9536
	                }
	};

	private static final int[][] TWIN_SPAWNS =
	{
	                {
	                                25665,
	                                -173727,
	                                218169,
	                                -9536
	                },
	                {
	                                25666,
	                                -173727,
	                                218049,
	                                -9536
	                }
	};

	private static final int[] TEPIOS_SPAWN =
	{
	                -173695,
	                218052,
	                -9538
	};

	private static final int BOSS_INVUL_TIME = 30000;
	private static final int BOSS_MINION_SPAWN_TIME = 60000;
	private static final int BOSS_RESSURECT_TIME = 20000;

	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;

	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}

	public SufferingHallDefence(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(MOUTHOFEKIMUS);
		addTalkId(MOUTHOFEKIMUS);
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);

		addKillId(TUMOR_ALIVE);
		addKillId(KLODEKUS);
		addKillId(KLANIKUS);

		addAttackId(KLODEKUS);
		addAttackId(KLANIKUS);

		for (int mobId : TUMOR_MOBIDS)
		{
			addSkillSeeId(mobId);
			addKillId(mobId);
		}
	}

	private boolean checkConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(2101));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessage.getSystemMessage(2185));
			return false;
		}
		for (L2PcInstance partyMember : party.getMembers())
		{
			if (partyMember.getLevel() < 75)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2097);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2096);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2100);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}

			QuestState st = partyMember.getQuestState("_695_DefendTheHallofSuffering");
			if ((st == null) || (st.getInt("cond") != 1))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}

	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}

	protected int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if (world != null)
		{
			if (!(world instanceof DHSWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}

		if (!checkConditions(player))
		{
			return 0;
		}

		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new DHSWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		((DHSWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		runTumors((DHSWorld) world);
		teleto.instanceId = instanceId;

		if (player.getParty() == null)
		{
			teleportplayer(player, teleto);
			world.allowed.add(player.getObjectId());
		}
		else
		{
			for (L2PcInstance partyMember : player.getParty().getMembers())
			{
				teleportplayer(partyMember, teleto);
				world.allowed.add(partyMember.getObjectId());
			}
		}
		return instanceId;
	}

	protected void exitInstance(L2PcInstance player, teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
		L2Summon pet = player.getSummon();
		if (pet != null)
		{
			pet.setInstanceId(0);
			pet.teleToLocation(tele.x, tele.y, tele.z);
		}
	}

	protected boolean checkKillProgress(L2Npc mob, DHSWorld world)
	{
		if (world.npcList.containsKey(mob))
		{
			world.npcList.put(mob, true);
		}
		for (boolean isDead : world.npcList.values())
		{
			if (!isDead)
			{
				return false;
			}
		}
		return true;
	}

	protected int[][] getRoomSpawns(int room)
	{
		switch (room)
		{
			case 0:
				return ROOM_1_MOBS;
			case 1:
				return ROOM_2_MOBS;
			case 2:
				return ROOM_3_MOBS;
			case 3:
				return ROOM_4_MOBS;
			case 4:
				return ROOM_5_MOBS;
		}
		return new int[][] {};
	}

	protected void runTumors(DHSWorld world)
	{
		for (int[] mob : getRoomSpawns(world.status))
		{
			L2Npc npc = addSpawn(mob[0], mob[1], mob[2], mob[3], 0, false, 0, false, world.instanceId);
			world.npcList.put(npc, false);
		}
		L2Npc mob = addSpawn(TUMOR_ALIVE, TUMOR_SPAWNS[world.status][0], TUMOR_SPAWNS[world.status][1], TUMOR_SPAWNS[world.status][2], 0, false, 0, false, world.instanceId);
		mob.disableCoreAI(true);
		mob.setIsImmobilized(true);
		mob.setCurrentHp(mob.getMaxHp() * 0.5);
		world.npcList.put(mob, false);
		world.status++;
	}

	protected void runTwins(DHSWorld world)
	{
		world.status++;
		world.klodekus = addSpawn(TWIN_SPAWNS[0][0], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, world.instanceId);
		world.klanikus = addSpawn(TWIN_SPAWNS[1][0], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, world.instanceId);
		world.klanikus.setIsMortal(false);
		world.klodekus.setIsMortal(false);
	}

	protected void bossSimpleDie(L2Npc boss)
	{
		synchronized (this)
		{
			if (boss.isDead())
			{
				return;
			}
			boss.setCurrentHp(0);
			boss.setIsDead(true);
		}
		boss.setTarget(null);
		boss.stopMove(null);
		boss.getStatus().stopHpMpRegeneration();
		boss.stopAllEffectsExceptThoseThatLastThroughDeath();
		boss.broadcastStatusUpdate();
		boss.getAI().notifyEvent(CtrlEvent.EVT_DEAD);

		if (boss.getWorldRegion() != null)
		{
			boss.getWorldRegion().onDeath(boss);
		}
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (skill.hasEffectType(L2EffectType.REBALANCE_HP, L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
		{
			int hate = 2 * skill.getAggroPoints();
			if (hate < 2)
			{
				hate = 1000;
			}
			((L2Attackable) npc).addDamageHate(caster, 0, hate);
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			DHSWorld world = (DHSWorld) tmpworld;
			if (event.equalsIgnoreCase("spawnBossGuards"))
			{
				L2Npc mob = addSpawn(TWIN_MOBIDS[getRandom(TWIN_MOBIDS.length)], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, npc.getInstanceId());
				((L2Attackable) mob).addDamageHate(((L2Attackable) npc).getMostHated(), 0, 1);
				if (getRandom(100) < 33)
				{
					mob = addSpawn(TWIN_MOBIDS[getRandom(TWIN_MOBIDS.length)], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, npc.getInstanceId());
					((L2Attackable) mob).addDamageHate(((L2Attackable) npc).getMostHated(), 0, 1);
				}
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
			}
			else if (event.equalsIgnoreCase("isTwinSeparated"))
			{
				if (Util.checkIfInRange(500, world.klanikus, world.klodekus, false))
				{
					world.klanikus.setIsInvul(false);
					world.klodekus.setIsInvul(false);
				}
				else
				{
					world.klanikus.setIsInvul(true);
					world.klodekus.setIsInvul(true);
				}
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (event.equalsIgnoreCase("ressurectTwin"))
			{
				L2Skill skill = SkillHolder.getInstance().getInfo(5824, 1);
				L2Npc aliveTwin = (world.klanikus == npc ? world.klodekus : world.klanikus);
				npc.doRevive();
				npc.doCast(skill);
				npc.setCurrentHp(aliveTwin.getCurrentHp());

				L2Character hated = ((L2MonsterInstance) aliveTwin).getMostHated();
				if (hated != null)
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, 1000);
				}

				aliveTwin.setIsInvul(true);
				startQuestTimer("uninvul", BOSS_INVUL_TIME, aliveTwin, null);
			}
			else if (event.equals("uninvul"))
			{
				npc.setIsInvul(false);
			}
		}
		return "";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			if (!((DHSWorld) tmpworld).isBossesAttacked)
			{
				((DHSWorld) tmpworld).isBossesAttacked = true;
				Calendar reenter = Calendar.getInstance();
				reenter.set(Calendar.MINUTE, RESET_MIN);

				if (reenter.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
				{
					reenter.add(Calendar.DATE, 1);
				}
				reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);

				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
				sm.addString(InstanceManager.getInstance().getInstanceIdName(tmpworld.templateId));

				for (int objectId : tmpworld.allowed)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objectId);
					if ((player != null) && player.isOnline())
					{
						InstanceManager.getInstance().setInstanceTime(objectId, tmpworld.templateId, reenter.getTimeInMillis());
						player.sendPacket(sm);
					}
				}
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (damage >= npc.getCurrentHp())
			{
				if (((DHSWorld) tmpworld).klanikus.isDead())
				{
					((DHSWorld) tmpworld).klanikus.setIsDead(false);
					((DHSWorld) tmpworld).klanikus.doDie(attacker);
					((DHSWorld) tmpworld).klodekus.doDie(attacker);
				}
				else if (((DHSWorld) tmpworld).klodekus.isDead())
				{
					((DHSWorld) tmpworld).klodekus.setIsDead(false);
					((DHSWorld) tmpworld).klodekus.doDie(attacker);
					((DHSWorld) tmpworld).klanikus.doDie(attacker);
				}
				else
				{
					bossSimpleDie(npc);
					startQuestTimer("ressurectTwin", BOSS_RESSURECT_TIME, npc, null);
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			Location loc = npc.getLocation();
			DHSWorld world = (DHSWorld) tmpworld;
			if (world.status < 5)
			{
				if (checkKillProgress(npc, world))
				{
					runTumors(world);
				}
			}
			else if (world.status == 5)
			{
				if (checkKillProgress(npc, world))
				{
					runTwins(world);
				}
			}
			else if ((world.status == 6) && ((npc.getId() == KLODEKUS) || (npc.getId() == KLANIKUS)))
			{
				if (world.klanikus.isDead() && world.klodekus.isDead())
				{
					world.status++;
					world.storeTime[1] = System.currentTimeMillis();
					world.calcRewardItemId();
					world.klanikus = null;
					world.klodekus = null;
					cancelQuestTimers("ressurectTwin");
					cancelQuestTimers("spawnBossGuards");
					cancelQuestTimers("isTwinSeparated");
					addSpawn(TEPIOS, TEPIOS_SPAWN[0], TEPIOS_SPAWN[1], TEPIOS_SPAWN[2], 0, false, 0, false, world.instanceId);
					doCountCoffinNotifications = false;

					Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
					inst.setDuration(5 * 60000);
					inst.setEmptyDestroyTime(0);
				}
			}

			if (npc.getId() == TUMOR_ALIVE)
			{
				world.TumorIndex(-50);
				world.TumorCount(1);
				doCountCoffinNotifications = true;
				notifyCoffinActivity(npc, world);
				if (world.tumorcount == 5)
				{
					npc.deleteMe();
					L2Npc deadTumor = spawnNpc(TUMOR_DEAD, loc, 0, world.instanceId);
					world.tumor.add(deadTumor);
				}
			}
		}
		return "";
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (npcId == MOUTHOFEKIMUS)
		{
			teleCoord tele = new teleCoord();
			tele.x = -174701;
			tele.y = 218109;
			tele.z = -9592;
			enterInstance(player, "SufferingHallDefence.xml", tele);
			return null;
		}
		return "";
	}

	public void notifyCoffinActivity(L2Npc npc, DHSWorld world)
	{
		if (!doCountCoffinNotifications)
		{
			return;
		}

		if (world.tumorIndex == 200)
		{
			broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_AREA_NEAR_THE_TUMOR_IS_FULL_OF_OMINOUS_ENERGY, 2, 8000));
		}
		else if (world.tumorIndex == 100)
		{
			broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_CAN_FEEL_THE_SURGING_ENERGY_OF_DEATH_FROM_THE_TUMOR, 2, 8000));
		}
		if (world.tumorIndex <= 0)
		{
			Location loc = npc.getLocation();

			for (L2Npc npcs : world.tumor)
			{
				if (npcs != null)
				{
					npcs.deleteMe();
				}
			}
			L2Npc aliveTumor = spawnNpc(TUMOR_ALIVE, loc, 0, world.instanceId);
			world.npcList.put(aliveTumor, false);
			doCountCoffinNotifications = false;
		}
	}

	protected void broadCastPacket(DHSWorld world, L2GameServerPacket packet)
	{
		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if ((player != null) && player.isOnline() && (player.getInstanceId() == world.instanceId))
			{
				player.sendPacket(packet);
			}
		}
	}

	public static void main(String[] args)
	{
		new SufferingHallDefence(-1, SufferingHallDefence.class.getSimpleName(), "instances");
	}
}
