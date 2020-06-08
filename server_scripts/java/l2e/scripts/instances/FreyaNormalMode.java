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

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.customs.LocalizationStorage;
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
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExChangeNpcState;
import l2e.gameserver.network.serverpackets.ExSendUIEvent;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage2;
import l2e.gameserver.network.serverpackets.OnEventTrigger;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Broadcast;
import l2e.gameserver.util.Util;

public class FreyaNormalMode extends Quest
{
	private static final int INSTANCE_ID = 139;

	protected static int _sirra = 32762;
	protected static int Jinia = 32781;
	protected static int glacier = 18853;
	protected static int archery_breathe = 18854;
	protected static int archery_knight = 18855;

	protected static int Glakias = 25699;
	protected static int freyaOnThrone = 29177;
	protected static int freyaSpelling = 29178;
	protected static int freyaStand = 29179;

	private static int door = 23140101;

	private static int[] emmiters =
	{
	                23140202,
	                23140204,
	                23140206,
	                23140208,
	                23140212,
	                23140214,
	                23140216
	};

	private static int decoration = 0;

	protected static final int[] archery_blocked_status =
	{
	                11,
	                19,
	                22,
	                29,
	                39
	};

	protected static final int[] glacier_blocked_status =
	{
	                11,
	                19,
	                29,
	                39
	};

	protected static final int[][] frozeKnightsSpawn =
	{
	                {
	                                113845,
	                                -116091,
	                                -11168,
	                                8264
	                },
	                {
	                                113381,
	                                -115622,
	                                -11168,
	                                8264
	                },
	                {
	                                113380,
	                                -113978,
	                                -11168,
	                                -8224
	                },
	                {
	                                113845,
	                                -113518,
	                                -11168,
	                                -8224
	                },
	                {
	                                115591,
	                                -113516,
	                                -11168,
	                                -24504
	                },
	                {
	                                116053,
	                                -113981,
	                                -11168,
	                                -24504
	                },
	                {
	                                116061,
	                                -115611,
	                                -11168,
	                                24804
	                },
	                {
	                                115597,
	                                -116080,
	                                -11168,
	                                24804
	                },
	                {
	                                112942,
	                                -115480,
	                                -10960,
	                                52
	                },
	                {
	                                112940,
	                                -115146,
	                                -10960,
	                                52
	                },
	                {
	                                112945,
	                                -114453,
	                                -10960,
	                                52
	                },
	                {
	                                112945,
	                                -114123,
	                                -10960,
	                                52
	                },
	                {
	                                116497,
	                                -114117,
	                                -10960,
	                                32724
	                },
	                {
	                                116499,
	                                -114454,
	                                -10960,
	                                32724
	                },
	                {
	                                116501,
	                                -115145,
	                                -10960,
	                                32724
	                },
	                {
	                                116502,
	                                -115473,
	                                -10960,
	                                32724
	                }
	};

	protected static final int[][] _archeryKnightsSpawn =
	{
	                {
	                                114713,
	                                -115109,
	                                -11202,
	                                16456
	                },
	                {
	                                114008,
	                                -115080,
	                                -11202,
	                                3568
	                },
	                {
	                                114422,
	                                -115508,
	                                -11202,
	                                12400
	                },
	                {
	                                115023,
	                                -115508,
	                                -11202,
	                                20016
	                },
	                {
	                                115459,
	                                -115079,
	                                -11202,
	                                27936
	                }
	};

	private class FreyaWorld extends InstanceWorld
	{
		public L2Attackable _freyaThrone = null;
		public L2Npc _freyaSpelling = null;
		public L2Attackable _freyaStand = null;
		public L2Attackable _glakias = null;
		public L2Attackable _jinia = null;
		public L2Attackable _kegor = null;
		public boolean isMovieNow = false;
		public FastMap<Integer, L2Npc> _archery_knights = new FastMap<>();
		public FastMap<Integer, L2Npc> _simple_knights = new FastMap<>();
		public FastMap<Integer, L2Npc> _glaciers = new FastMap<>();

		public FreyaWorld()
		{
			InstanceManager.getInstance();
		}
	}

	public FreyaNormalMode(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Jinia);
		addTalkId(Jinia);

		addAttackId(archery_knight);
		addAttackId(freyaStand);
		addAttackId(freyaOnThrone);

		addAggroRangeEnterId(archery_breathe);
		addAggroRangeEnterId(archery_knight);

		addSpellFinishedId(freyaStand);
		addSpellFinishedId(freyaOnThrone);

		addKillId(freyaOnThrone);
		addKillId(freyaStand);
		addKillId(freyaSpelling);
		addKillId(archery_knight);
		addKillId(glacier);
		addKillId(Glakias);

		addSpawnId(archery_knight);
		addSpawnId(glacier);
	}

	private class spawnWave implements Runnable
	{
		private final int _waveId;
		private final FreyaWorld _world;

		public spawnWave(int waveId, int instanceId)
		{
			_waveId = waveId;
			_world = getWorld(instanceId);
		}

		@Override
		public void run()
		{
			switch (_waveId)
			{
				case 1:
					spawnNpc(_sirra, 114766, -113141, -11200, 15956, _world.instanceId);
					handleWorldState(1, _world.instanceId);
					break;
				case 3:
					if (_world == null)
					{
						break;
					}
					if (Util.contains(archery_blocked_status, _world.status))
					{
						break;
					}
					if ((_world._archery_knights.size() < 5) && (_world.status < 44))
					{
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						L2Attackable mob = (L2Attackable) spawnNpc(archery_knight, spawnXY[0], spawnXY[1], -11200, 20016, _world.instanceId);
						mob.setOnKillDelay(0);
						L2PcInstance victim = getRandomPlayer(_world);
						mob.setTarget(victim);
						mob.setRunning();
						mob.addDamageHate(victim, 0, 9999);
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
						_world._archery_knights.put(mob.getObjectId(), mob);
						if ((_world.status == 1) || (_world.status == 11) || (_world.status == 24) || (_world.status == 30) || (_world.status == 40))
						{
							mob.setIsImmobilized(true);
						}
					}
					break;
				case 4:
					break;
				case 5:
					if ((_world != null) && (_world._glaciers.size() < 5) && (_world.status < 44) && !Util.contains(glacier_blocked_status, _world.status))
					{
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						L2Attackable mob = (L2Attackable) spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 20016, _world.instanceId);
						_world._glaciers.put(mob.getObjectId(), mob);
					}
					if (_world.status < 44)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, _world.instanceId), (getRandom(10, 40) * 1000) + 40000);
					}
					break;
				case 6:
					for (int[] iter : _archeryKnightsSpawn)
					{
						L2Attackable mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], _world.instanceId);
						mob.setOnKillDelay(0);
						mob.setRunning();
						L2PcInstance victim = getRandomPlayer(_world);
						mob.setTarget(victim);
						mob.addDamageHate(victim, 0, 9999);
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
						_world._archery_knights.put(mob.getObjectId(), mob);
					}
					handleWorldState(_world.status + 1, _world);
					break;
				case 7:
					handleWorldState(2, _world.instanceId);
					break;
				case 8:
					handleWorldState(3, _world.instanceId);
					break;
				case 9:
					handleWorldState(11, _world.instanceId);
					break;
				case 10:
					handleWorldState(19, _world.instanceId);
					break;
				case 11:
					handleWorldState(20, _world.instanceId);
					break;
				case 12:
					handleWorldState(25, _world.instanceId);
					break;
				case 13:
					handleWorldState(30, _world.instanceId);
					break;
				case 14:
					handleWorldState(31, _world.instanceId);
					break;
				case 15:
					handleWorldState(41, _world.instanceId);
					break;
				case 16:
					handleWorldState(43, _world.instanceId);
					break;
				case 17:
					handleWorldState(45, _world.instanceId);
					break;
				case 18:
					handleWorldState(46, _world.instanceId);
					break;
				case 19:
					setInstanceRestriction(_world);
					InstanceManager.getInstance().getInstance(_world.instanceId).setDuration(300000);
					InstanceManager.getInstance().getInstance(_world.instanceId).setEmptyDestroyTime(0);
					break;
				case 20:
					stopAll(_world);
					break;
				case 21:
					_world.isMovieNow = false;
					startAll(_world);
					break;
			}
		}
	}

	private void broadcastMovie(int movieId, FreyaWorld world)
	{
		world.isMovieNow = true;

		stopAll(world);

		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			player.showQuestMovie(movieId);
		}

		int pause = 0;

		switch (movieId)
		{
			case 15:
				pause = 53500;
				break;
			case 16:
				pause = 21100;
				break;
			case 17:
				pause = 21500;
				break;
			case 18:
				pause = 27000;
				break;
			case 19:
				pause = 16000;
				break;
			case 23:
				pause = 7000;
				break;
			case 20:
				pause = 55500;
				break;
			default:
				pause = 0;
		}

		if (movieId != 15)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(21, world.instanceId), pause);
		}

		if (movieId == 19)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 100);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 200);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 500);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 1000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 2000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 3000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 4000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 5000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 6000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 7000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 8000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.instanceId), 9000);
		}
	}

	private void broadcastString(int strId, int instanceId)
	{
		ExShowScreenMessage2 sm = new ExShowScreenMessage2(strId, 3000, ExShowScreenMessage2.ScreenMessageAlign.TOP_CENTER, true, false, -1, true);
		Broadcast.toPlayersInInstance(sm, instanceId);
	}

	private void broadcastTimer(FreyaWorld world)
	{
		for (int objId : world.allowed)
		{
			L2PcInstance plr = L2World.getInstance().getPlayer(objId);
			String lang = plr.getLang();
			ExSendUIEvent time_packet = new ExSendUIEvent(plr, false, false, 60, 0, "" + LocalizationStorage.getInstance().getString(lang, "Freya.TIME_TO_NEXT_STAGE") + "");
			plr.sendPacket(time_packet);
		}
	}

	protected void handleWorldState(int statusId, int instanceId)
	{
		FreyaWorld world = getWorld(instanceId);
		if (world != null)
		{
			handleWorldState(statusId, world);
		}
		else
		{
			System.out.println("Warning!!! Not Found world at handleWorldState(int, int).");
		}
	}

	protected void handleWorldState(int statusId, FreyaWorld world)
	{
		int instanceId = world.instanceId;

		switch (statusId)
		{
			case 0:
				break;
			case 1:
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(door).openMe();
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(7, world.instanceId), 180000);
				break;
			case 2:
				broadcastMovie(15, world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(8, world.instanceId), 52500);
				break;
			case 3:
				world._freyaThrone = (L2Attackable) spawnNpc(freyaOnThrone, 114720, -117085, -11088, 15956, instanceId);
				world._freyaThrone.setIsNoRndWalk(true);
				world._freyaThrone.setisReturningToSpawnPoint(false);
				world._freyaThrone.setOnKillDelay(0);
				world._freyaThrone.setIsInvul(true);
				world._freyaThrone.setIsImmobilized(true);
				for (int objId : world.allowed)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					if ((player != null) && player.isOnline())
					{
						player.getKnownList().addKnownObject(world._freyaThrone);
					}
				}

				for (int[] iter : frozeKnightsSpawn)
				{
					L2Attackable mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
					archerySpawn(mob);
					world._simple_knights.put(mob.getObjectId(), mob);
				}

				for (int[] iter : _archeryKnightsSpawn)
				{
					L2Attackable mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
					archerySpawn(mob);
					mob.setDisplayEffect(1);
					world._archery_knights.put(mob.getObjectId(), mob);
				}

				for (int objId : world.allowed)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					player.setIsImmobilized(false);
					player.setIsInvul(false);
				}
				world.isMovieNow = false;
				break;
			case 10:
				broadcastString(1801086, world.instanceId);
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(door).closeMe();
				world._freyaThrone.setIsInvul(false);
				world._freyaThrone.setIsImmobilized(false);
				world._freyaThrone.getAI();
				world._freyaThrone.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114722, -114798, -11205, 15956));

				for (int i = 0; i < 5; i++)
				{
					int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
					L2Attackable mob = (L2Attackable) spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
					world._glaciers.put(mob.getObjectId(), mob);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.instanceId), 7000);

				for (L2Npc mob : world._archery_knights.values())
				{
					archeryAttack(mob, world);
				}
				break;
			case 11:
				broadcastMovie(16, world);
				for (L2Npc mob : world._archery_knights.values())
				{
					mob.deleteMe();
				}
				world._archery_knights.clear();
				world._freyaThrone.deleteMe();
				world._freyaThrone = null;
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(10, world.instanceId), 22000);
				break;
			case 12:
				break;
			case 19:
				world._freyaSpelling = spawnNpc(freyaSpelling, 114723, -117502, -10672, 15956, world.instanceId);
				world._freyaSpelling.setIsImmobilized(true);
				broadcastTimer(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(11, world.instanceId), 60000);
				break;
			case 20:
				for (int[] iter : _archeryKnightsSpawn)
				{
					L2Attackable mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
					archerySpawn(mob);
					mob.setDisplayEffect(1);
					world._archery_knights.put(mob.getObjectId(), mob);
				}
				break;
			case 21:
				broadcastString(1801087, instanceId);
				for (L2Npc mob : world._archery_knights.values())
				{
					archeryAttack(mob, world);
				}

				for (int i = 0; i < 5; i++)
				{
					int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
					L2Attackable mob = (L2Attackable) spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
					world._glaciers.put(mob.getObjectId(), mob);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.instanceId), 7000);
				break;
			case 22:
			case 23:
				break;
			case 24:
				broadcastMovie(23, world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(12, world.instanceId), 7000);
				break;
			case 25:
				world._glakias = (L2Attackable) spawnNpc(Glakias, 114707, -114799, -11199, 15956, instanceId);
				world._glakias.setOnKillDelay(0);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.instanceId), 7000);
				break;
			case 29:
				broadcastTimer(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(13, world.instanceId), 60000);
				break;
			case 30:
				for (int[] iter : _archeryKnightsSpawn)
				{
					L2Attackable mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
					mob.setOnKillDelay(0);
					world._archery_knights.put(mob.getObjectId(), mob);
				}
				world._freyaSpelling.deleteMe();
				world._freyaSpelling = null;
				broadcastMovie(17, world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(14, world.instanceId), 21500);
				break;
			case 31:
				ExChangeNpcState as = new ExChangeNpcState(decoration, 2);
				Broadcast.toPlayersInInstance(as, world.instanceId);
				for (int emitter : emmiters)
				{
					OnEventTrigger et = new OnEventTrigger(emitter, false);
					Broadcast.toPlayersInInstance(et, world.instanceId);
				}
				broadcastString(1801088, instanceId);
				world._freyaStand = (L2Attackable) spawnNpc(freyaStand, 114720, -117085, -11088, 15956, world.instanceId);
				world._freyaStand.setOnKillDelay(0);
				world._freyaStand.getAI();
				world._freyaStand.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114722, -114798, -11205, 15956));
				for (int objId : world.allowed)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					if ((player != null) && player.isOnline())
					{
						player.getKnownList().addKnownObject(world._freyaStand);
					}
				}
				break;
			case 40:
				broadcastMovie(18, world);
				stopAll(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(15, world.instanceId), 27000);
				break;
			case 41:
				for (L2Npc mob : world._archery_knights.values())
				{
					archeryAttack(mob, world);
				}
				world._jinia = (L2Attackable) spawnNpc(18850, 114727, -114700, -11200, -16260, instanceId);
				world._jinia.setAutoAttackable(false);
				world._jinia.setIsMortal(false);
				world._kegor = (L2Attackable) spawnNpc(18851, 114690, -114700, -11200, -16260, instanceId);
				world._kegor.setAutoAttackable(false);
				world._kegor.setIsMortal(false);
				handleWorldState(42, instanceId);
				break;
			case 42:
				broadcastString(1801089, instanceId);
				if ((world._freyaStand != null) && !world._freyaStand.isDead())
				{
					L2Character target = getFreyaTarget(world);

					world._jinia.setRunning();
					(world._jinia).addDamageHate(target, 9999, 9999);
					world._jinia.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

					world._kegor.setRunning();
					(world._kegor).addDamageHate(target, 9999, 9999);
					world._kegor.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
				else
				{
					world._jinia.setIsImmobilized(true);
					world._kegor.setIsImmobilized(true);
				}
				L2Skill skill1 = SkillHolder.getInstance().getInfo(6288, 1);
				L2Skill skill2 = SkillHolder.getInstance().getInfo(6289, 1);
				for (int objId : world.allowed)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					if (player != null)
					{
						skill1.getEffects(world._jinia, player);
						skill2.getEffects(world._kegor, player);
					}
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(16, instanceId), 6000);
				break;
			case 43:
				break;
			case 44:
				broadcastMovie(19, world);
				stopAll(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(17, instanceId), 20000);
				handleWorldState(45, instanceId);
			case 45:
				broadcastMovie(20, world);
				handleWorldState(46, instanceId);
			case 46:
				for (L2Npc mob : InstanceManager.getInstance().getInstance(instanceId).getNpcs())
				{
					if (mob.getId() != freyaStand)
					{
						mob.deleteMe();
						InstanceManager.getInstance().getInstance(instanceId).getNpcs().remove(mob);
					}
				}
				for (int objId : world.allowed)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					QuestState st = player.getQuestState("_10286_ReunionWithSirra");
					if ((st != null) && (st.getState() == State.STARTED) && (st.getInt("progress") == 2))
					{
						st.set("cond", "7");
						st.playSound("ItemSound.quest_middle");
						st.set("progress", "3");
					}
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, instanceId), 20000);
				break;
			default:
				System.out.println("Warning!!! Not handled world status - " + statusId);
				break;
		}
		world.status = statusId;
	}

	protected L2PcInstance getRandomPlayer(FreyaWorld world)
	{
		boolean exists = false;
		while (!exists)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(world.allowed.get(getRandom(0, world.allowed.size() - 1)));
			if (player != null)
			{
				exists = true;
				return player;
			}
		}
		return null;
	}

	protected FreyaWorld getWorld(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		FreyaWorld world = null;
		if (tmpworld instanceof FreyaWorld)
		{
			world = (FreyaWorld) tmpworld;
		}

		if (world == null)
		{
			System.out.println("Warning!!! World not found in getWorld(int instanceId)");
		}
		return world;
	}

	protected int getWorldStatus(L2PcInstance player)
	{
		return getWorld(player).status;
	}

	protected FreyaWorld getWorld(L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		FreyaWorld world = null;
		if (tmpworld instanceof FreyaWorld)
		{
			world = (FreyaWorld) tmpworld;
		}

		if (world == null)
		{
			System.out.println("Warning!!! World not found in getWorld(int instanceId)");
		}
		return world;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		int npcId = npc.getId();

		if (npcId == archery_knight)
		{
			if (npc.getDisplayEffect() == 1)
			{
				npc.setDisplayEffect(2);
			}

			if (getWorldStatus(attacker) == 3)
			{
				handleWorldState(10, attacker.getInstanceId());
			}
			else if (getWorldStatus(attacker) == 20)
			{
				handleWorldState(21, attacker.getInstanceId());
			}
		}
		else if (npcId == freyaStand)
		{
			if (!npc.isCastingNow())
			{
				callSkillAI(npc);
			}
			double cur_hp = npc.getCurrentHp();
			double max_hp = npc.getMaxHp();
			int percent = (int) Math.round((cur_hp / max_hp) * 100);
			if ((percent <= 20) && (getWorldStatus(attacker) < 40))
			{
				handleWorldState(40, attacker.getInstanceId());
			}
		}
		else if (npcId == freyaOnThrone)
		{
			if (!npc.isCastingNow())
			{
				callSkillAI(npc);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();

		FreyaWorld world = getWorld(killer);
		if (npcId == glacier)
		{
			if (world != null)
			{
				world._glaciers.remove(npc.getObjectId());
			}
			npc.setDisplayEffect(3);
			L2Npc mob = spawnNpc(archery_breathe, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), npc.getInstanceId());
			mob.setRunning();
			mob.setTarget(killer);
			((L2Attackable) mob).addDamageHate(killer, 0, 99999);
			mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		else if ((npcId == archery_knight) && (world != null))
		{
			if (world._archery_knights.containsKey(npc.getObjectId()))
			{
				world._archery_knights.remove(npc.getObjectId());

				if ((world.status > 20) && (world.status < 24))
				{
					if (world._archery_knights.size() == 0)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(6, killer.getInstanceId()), 30000);
					}
				}
				else if (world.status < 44)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(3, killer.getInstanceId()), (getRandom(10, 40) * 1000) + 30000);
				}
			}
			else if (world._simple_knights.containsKey(npc.getObjectId()))
			{
				world._simple_knights.remove(npc.getObjectId());
				startQuestTimer("spawndeco_" + npc.getSpawn().getX() + "_" + npc.getSpawn().getY() + "_" + npc.getSpawn().getZ() + "_" + npc.getSpawn().getHeading() + "_" + npc.getInstanceId(), 30000, null, null);
			}
		}
		else if (npcId == freyaOnThrone)
		{
			handleWorldState(11, killer.getInstanceId());
		}
		else if (npcId == Glakias)
		{
			handleWorldState(29, killer.getInstanceId());
		}
		else if (npcId == freyaStand)
		{
			handleWorldState(44, killer.getInstanceId());
		}
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		FreyaWorld world = getWorld(npc.getInstanceId());
		if ((world != null) && (world.status >= 44))
		{
			npc.deleteMe();
		}

		if ((world != null) && world.isMovieNow && (npc instanceof L2Attackable))
		{
			npc.abortAttack();
			npc.abortCast();
			npc.setIsImmobilized(true);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}

		if (npc.getId() == glacier)
		{
			npc.setDisplayEffect(1);
			npc.setIsImmobilized(true);
			((L2Attackable) npc).setOnKillDelay(0);
			startQuestTimer("setDisplayEffect2", 1000, npc, null);
			startQuestTimer("cast", 20000, npc, null);
		}
		return super.onSpawn(npc);
	}

	private void enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if (world != null)
		{
			if (!(world instanceof FreyaWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			teleportPlayer(player, (FreyaWorld) world);
			return;
		}

		if (!checkConditions(player))
		{
			return;
		}

		L2Party party = player.getParty();
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
		inst.setSpawnsLoc(new int[]
		{
		                player.getX(),
		                player.getY(),
		                player.getZ()
		});

		world = new FreyaWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCE_ID;
		world.status = 0;

		InstanceManager.getInstance().addWorld(world);

		if ((party != null) && party.isInCommandChannel())
		{
			for (L2PcInstance plr : party.getCommandChannel().getMembers())
			{
				QuestState qs = plr.getQuestState("_10286_ReunionWithSirra");
				if (qs != null)
				{
					if (qs.getInt("cond") == 5)
					{
						qs.set("cond", "6");
						qs.playSound("ItemSound.quest_middle");
					}
				}
				world.allowed.add(plr.getObjectId());
				teleportPlayer(plr, (FreyaWorld) world);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(1, world.instanceId), 100);
			return;
		}
	}

	private boolean checkConditions(L2PcInstance player)
	{
		if (player.getParty() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}

		if (player.getParty().getCommandChannel() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}

		if (player.getObjectId() != player.getParty().getCommandChannel().getLeader().getObjectId())
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}

		if (player.getParty().getCommandChannel().getMemberCount() < Config.MIN_FREYA_PLAYERS)
		{
			player.getParty().getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(2793).addNumber(10));
			return false;
		}

		if (player.getParty().getCommandChannel().getMemberCount() > Config.MAX_FREYA_PLAYERS)
		{
			player.getParty().getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(2102));
			return false;
		}

		for (L2PcInstance partyMember : player.getParty().getCommandChannel().getMembers())
		{
			if (partyMember.getLevel() < Config.MIN_LEVEL_PLAYERS)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2097);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}

			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2096);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}

			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCE_ID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2100);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}

			QuestState st = partyMember.getQuestState("_10286_ReunionWithSirra");
			if ((st == null) || st.isCompleted())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}

	private void teleportPlayer(L2PcInstance player, FreyaWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(world.instanceId);
		player.teleToLocation(113991, -112297, -11200);
		if (player.hasSummon())
		{
			player.getSummon().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getSummon().setInstanceId(world.instanceId);
			player.getSummon().teleToLocation(113991, -112297, -11200);
		}
		return;
	}

	protected void setInstanceRestriction(FreyaWorld world)
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, 30);
		reenter.set(Calendar.HOUR_OF_DAY, 6);

		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
		{
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		}

		if (reenter.get(Calendar.DAY_OF_WEEK) <= Calendar.WEDNESDAY)
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		else
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
		sm.addString(InstanceManager.getInstance().getInstanceIdName(INSTANCE_ID));

		for (int objectId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			InstanceManager.getInstance().setInstanceTime(objectId, INSTANCE_ID, reenter.getTimeInMillis());
			if ((player != null) && player.isOnline())
			{
				player.sendPacket(sm);
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("setDisplayEffect2"))
		{
			if (!npc.isDead())
			{
				npc.setDisplayEffect(2);
			}
		}
		else if (event.equalsIgnoreCase("cast"))
		{
			if ((npc != null) && !npc.isDead())
			{
				L2Skill skill = SkillHolder.getInstance().getInfo(6437, getRandom(1, 3));
				for (L2PcInstance plr : npc.getKnownList().getKnownPlayersInRadius(skill.getAffectRange()))
				{
					if (!hasBuff(6437, plr) && !plr.isDead() && !plr.isAlikeDead())
					{
						skill.getEffects(npc, plr);
					}
				}
				startQuestTimer("cast", 20000, npc, null);
			}
		}
		else if (event.startsWith("spawndeco"))
		{
			String[] params = event.split("_");
			FreyaWorld world = getWorld(Integer.parseInt(params[5]));

			if ((world != null) && (world.status < 44))
			{
				L2Attackable mob = (L2Attackable) spawnNpc(archery_knight, Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Integer.parseInt(params[5]));
				mob.setIsImmobilized(true);
				mob.setDisplayEffect(1);
				world._simple_knights.put(mob.getObjectId(), mob);
			}
		}
		else if (event.equalsIgnoreCase("show_string"))
		{
			if ((npc != null) && !npc.isDead())
			{
				broadcastString(1801111, npc.getInstanceId());
			}
		}
		else if (event.equalsIgnoreCase("summon_breathe"))
		{
			L2Npc mob = spawnNpc(archery_breathe, npc.getX() + getRandom(-90, 90), npc.getY() + getRandom(-90, 90), npc.getZ(), npc.getHeading(), npc.getInstanceId());
			mob.setRunning();
			if (npc.getTarget() != null)
			{
				mob.setTarget(npc.getTarget());
				((L2Attackable) mob).addDamageHate((L2Character) npc.getTarget(), 0, 99999);
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc.getTarget());
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (npcId == Jinia)
		{
			enterInstance(player, "IceQueenCastle2.xml");
		}
		return "";
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((npc.getId() == archery_breathe) || (npc.getId() == archery_knight))
		{
			if (npc.isImmobilized())
			{
				npc.abortAttack();
				npc.abortCast();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}

	protected int[] getRandomPoint(int min_x, int max_x, int min_y, int max_y)
	{
		int[] ret =
		{
		                0,
		                0
		};
		ret[0] = getRandom(min_x, max_x);
		ret[1] = getRandom(min_y, max_y);
		return ret;
	}

	private void archerySpawn(L2Npc mob)
	{
		((L2Attackable) mob).setOnKillDelay(0);
		mob.setDisplayEffect(1);
		mob.setIsImmobilized(true);
	}

	private void archeryAttack(L2Npc mob, FreyaWorld world)
	{
		mob.setDisplayEffect(2);
		mob.setIsImmobilized(false);
		mob.setRunning();
		L2PcInstance victim = getRandomPlayer(world);
		mob.setTarget(victim);
		((L2Attackable) mob).addDamageHate(victim, 0, 9999);
		mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
	}

	protected void stopAll(FreyaWorld world)
	{
		if (world == null)
		{
			return;
		}

		if ((world._freyaStand != null) && !world._freyaStand.isDead())
		{
			if (world._freyaStand.getTarget() != null)
			{
				world._freyaStand.abortAttack();
				world._freyaStand.abortCast();
				world._freyaStand.setTarget(null);
				world._freyaStand.clearAggroList();
				world._freyaStand.setIsImmobilized(true);
				world._freyaStand.teleToLocation(world._freyaStand.getX() - 100, world._freyaStand.getY() + 100, world._freyaStand.getZ(), world._freyaStand.getHeading(), false);
			}
		}

		for (L2Npc mob : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
		{
			if ((mob != null) && !mob.isDead())
			{
				mob.abortAttack();
				mob.abortCast();
				if (mob instanceof L2Attackable)
				{
					((L2Attackable) mob).clearAggroList();
				}
				mob.setIsImmobilized(true);
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}

		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);

			if (player == null)
			{
				continue;
			}

			player.abortAttack();
			player.abortCast();
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.setIsImmobilized(true);
			player.setIsInvul(true);
		}
	}

	protected void startAll(FreyaWorld world)
	{
		if (world == null)
		{
			return;
		}

		for (L2Npc mob : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
		{
			L2Object target = null;

			if (mob.getTarget() != null)
			{
				target = mob.getTarget();
			}
			else
			{
				target = getRandomPlayer(world);
			}

			if ((mob.getId() != glacier) && !world._simple_knights.containsKey(mob.getObjectId()) && (mob instanceof L2Attackable))
			{
				((L2Attackable) mob).addDamageHate((L2Character) target, 0, 9999);
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				mob.setIsImmobilized(false);
			}
		}

		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			player.setIsImmobilized(false);
			if (player.getFirstEffect(L2EffectType.INVINCIBLE) == null)
			{
				player.setIsInvul(false);
			}
		}
	}

	private L2Npc getFreyaTarget(FreyaWorld world)
	{
		FastList<L2Npc> npcList = new FastList<>();
		L2Npc victim = null;
		victim = world._freyaStand;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		if (npcList.size() > 0)
		{
			return npcList.get(getRandom(npcList.size() - 1));
		}
		return null;
	}

	private void callSkillAI(L2Npc mob)
	{
		int[][] freya_skills =
		{
		                {
		                                6274,
		                                1,
		                                4000,
		                                10
		                },
		                {
		                                6276,
		                                1,
		                                -1,
		                                100
		                },
		                {
		                                6277,
		                                1,
		                                -1,
		                                100
		                },
		                {
		                                6278,
		                                1,
		                                -1,
		                                100
		                },
		                {
		                                6279,
		                                1,
		                                2000,
		                                100
		                },
		                {
		                                6282,
		                                1,
		                                -1,
		                                100
		                }
		};

		int iter = getRandom(0, 2);

		if ((freya_skills[iter][3] < 100) && (getRandom(100) > freya_skills[iter][3]))
		{
			iter = 3;
		}

		mob.doCast(SkillHolder.getInstance().getInfo(freya_skills[iter][0], freya_skills[iter][1]));
		if (freya_skills[iter][2] > 0)
		{
			startQuestTimer("show_string", freya_skills[iter][2], mob, null);
		}

		if (freya_skills[iter][0] == 6277)
		{
			startQuestTimer("summon_breathe", 10000, mob, null);
		}
	}

	private boolean hasBuff(int id, L2PcInstance player)
	{
		for (L2Effect e : player.getAllEffects())
		{
			if (e.getSkill().getId() == id)
			{
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args)
	{
		new FreyaNormalMode(-1, FreyaNormalMode.class.getSimpleName(), "instances");
	}
}
