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

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.model.L2CommandChannel;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2Territory;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2TrapInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

public class Stage1 extends Quest
{
	private class SOD1World extends InstanceWorld
	{
		public Map<L2Npc, Boolean> npcList = new FastMap<>();
		public int deviceSpawnedMobCount = 0;
		public Lock lock = new ReentrantLock();
		
		public SOD1World()
		{
		}
	}
	
	protected static class SODSpawn
	{
		public boolean isZone = false;
		public boolean isNeededNextFlag = false;
		public int npcId;
		public int x = 0;
		public int y = 0;
		public int z = 0;
		public int h = 0;
		public int zone = 0;
		public int count = 0;
	}
	
	private static final String qn = "SoDStage1";
	private static final int INSTANCEID = 110;
	private static final int MAX_DEVICESPAWNEDMOBCOUNT = 100;
	
	private final Map<Integer, L2Territory> _spawnZoneList = new HashMap<>();
	private final Map<Integer, List<SODSpawn>> _spawnList = new HashMap<>();
	private final List<Integer> _mustKillMobsId = new FastList<>();
	
	private static final Location ENTER_TELEPORT_1 = new Location(-242759, 219981, -9986);
	private static final Location ENTER_TELEPORT_2 = new Location(-245800, 220488, -12112);
	private static final Location CENTER_TELEPORT = new Location(-245802, 220528, -12104);
	
	private static final SkillsHolder TRAP_HOLD = new SkillsHolder(4186, 9);
	private static final SkillsHolder TRAP_STUN = new SkillsHolder(4072, 10);
	private static final SkillsHolder TRAP_DAMAGE = new SkillsHolder(5340, 4);
	private static final SkillsHolder TRAP_SPAWN = new SkillsHolder(10002, 1);
	
	private static final int[] TRAP_18771_NPCS =
	{
		22541,
		22544,
		22541,
		22544
	};
	
	private static final int[] TRAP_OTHER_NPCS =
	{
		22546,
		22546,
		22538,
		22537
	};
	
	private static final int ALENOS = 32526;
	private static final int TELEPORT = 32601;
	
	private static final int OBELISK = 18776;
	private static final int POWERFUL_DEVICE = 18777;
	private static final int THRONE_POWERFUL_DEVICE = 18778;
	private static final int SPAWN_DEVICE = 18696;
	private static final int TIAT = 29163;
	private static final int TIAT_GUARD = 29162;
	private static final int TIAT_GUARD_NUMBER = 5;
	private static final int TIAT_VIDEO_NPC = 29169;
	private static final Location MOVE_TO_TIAT = new Location(-250403, 207273, -11952, 16384);
	private static final Location MOVE_TO_DOOR = new Location(-251432, 214905, -12088, 16384);
	
	private static final int[] SPAWN_MOB_IDS =
	{
		22536,
		22537,
		22538,
		22539,
		22540,
		22541,
		22542,
		22543,
		22544,
		22547,
		22550,
		22551,
		22552,
		22596
	};
	
	private static final int[] ATTACKABLE_DOORS =
	{
		12240005,
		12240006,
		12240007,
		12240008,
		12240009,
		12240010,
		12240013,
		12240014,
		12240015,
		12240016,
		12240017,
		12240018,
		12240021,
		12240022,
		12240023,
		12240024,
		12240025,
		12240026,
		12240028,
		12240029,
		12240030
	};
	
	private static final int[] ENTRANCE_ROOM_DOORS =
	{
		12240001,
		12240002
	};
	
	private static final int[] SQUARE_DOORS =
	{
		12240003,
		12240004,
		12240011,
		12240012,
		12240019,
		12240020
	};
	
	private static final int SCOUTPASS_DOOR = 12240027;
	private static final int FORTRESS_DOOR = 12240030;
	private static final int THRONE_DOOR = 12240031;
	
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	private static final int RESET_DAY_1 = 4;
	private static final int RESET_DAY_2 = 7;
	
	public Stage1(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		load();
		addStartNpc(ALENOS);
		addTalkId(ALENOS);
		addStartNpc(TELEPORT);
		addTalkId(TELEPORT);
		addAttackId(OBELISK);
		addSpawnId(OBELISK);
		addKillId(OBELISK);
		addSpawnId(POWERFUL_DEVICE);
		addKillId(POWERFUL_DEVICE);
		addSpawnId(THRONE_POWERFUL_DEVICE);
		addKillId(THRONE_POWERFUL_DEVICE);
		addAttackId(TIAT);
		addKillId(TIAT);
		addKillId(SPAWN_DEVICE);
		addSpawnId(TIAT_GUARD);
		addKillId(TIAT_GUARD);
		addAggroRangeEnterId(TIAT_VIDEO_NPC);
		
		for (int i = 18771; i <= 18774; i++)
		{
			addTrapActionId(i);
		}
		
		for (int mobId : _mustKillMobsId)
		{
			addKillId(mobId);
		}
	}
	
	private void load()
	{
		int spawnCount = 0;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT + "/data/spawnZones/seed_of_destruction.xml");
			if (!file.exists())
			{
				_log.severe("[Seed of Destruction] Missing seed_of_destruction.xml. The quest wont work without it!");
				return;
			}
			
			Document doc = factory.newDocumentBuilder().parse(file);
			Node first = doc.getFirstChild();
			if ((first != null) && "list".equalsIgnoreCase(first.getNodeName()))
			{
				for (Node n = first.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("npc".equalsIgnoreCase(n.getNodeName()))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("spawn".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								Node att = attrs.getNamedItem("npcId");
								if (att == null)
								{
									_log.severe("[Seed of Destruction] Missing npcId in npc List, skipping");
									continue;
								}
								int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());
								
								att = attrs.getNamedItem("flag");
								if (att == null)
								{
									_log.severe("[Seed of Destruction] Missing flag in npc List npcId: " + npcId + ", skipping");
									continue;
								}
								int flag = Integer.parseInt(attrs.getNamedItem("flag").getNodeValue());
								if (!_spawnList.containsKey(flag))
								{
									_spawnList.put(flag, new FastList<SODSpawn>());
								}
								
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if ("loc".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										SODSpawn spw = new SODSpawn();
										spw.npcId = npcId;
										
										att = attrs.getNamedItem("x");
										if (att != null)
										{
											spw.x = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("y");
										if (att != null)
										{
											spw.y = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("z");
										if (att != null)
										{
											spw.z = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("heading");
										if (att != null)
										{
											spw.h = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("mustKill");
										if (att != null)
										{
											spw.isNeededNextFlag = Boolean.parseBoolean(att.getNodeValue());
										}
										if (spw.isNeededNextFlag)
										{
											_mustKillMobsId.add(npcId);
										}
										_spawnList.get(flag).add(spw);
										spawnCount++;
									}
									else if ("zone".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										SODSpawn spw = new SODSpawn();
										spw.npcId = npcId;
										spw.isZone = true;
										
										att = attrs.getNamedItem("id");
										if (att != null)
										{
											spw.zone = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("count");
										if (att != null)
										{
											spw.count = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("mustKill");
										if (att != null)
										{
											spw.isNeededNextFlag = Boolean.parseBoolean(att.getNodeValue());
										}
										if (spw.isNeededNextFlag)
										{
											_mustKillMobsId.add(npcId);
										}
										_spawnList.get(flag).add(spw);
										spawnCount++;
									}
								}
							}
						}
					}
					else if ("spawnZones".equalsIgnoreCase(n.getNodeName()))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("zone".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								Node att = attrs.getNamedItem("id");
								if (att == null)
								{
									_log.severe("[Seed of Destruction] Missing id in spawnZones List, skipping");
									continue;
								}
								int id = Integer.parseInt(att.getNodeValue());
								att = attrs.getNamedItem("minZ");
								if (att == null)
								{
									_log.severe("[Seed of Destruction] Missing minZ in spawnZones List id: " + id + ", skipping");
									continue;
								}
								int minz = Integer.parseInt(att.getNodeValue());
								att = attrs.getNamedItem("maxZ");
								if (att == null)
								{
									_log.severe("[Seed of Destruction] Missing maxZ in spawnZones List id: " + id + ", skipping");
									continue;
								}
								int maxz = Integer.parseInt(att.getNodeValue());
								L2Territory ter = new L2Territory(id);
								
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if ("point".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										int x, y;
										att = attrs.getNamedItem("x");
										if (att != null)
										{
											x = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("y");
										if (att != null)
										{
											y = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										
										ter.add(x, y, minz, maxz, 0);
									}
								}
								
								_spawnZoneList.put(id, ter);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "[Seed of Destruction] Could not parse data.xml file: " + e.getMessage(), e);
		}
		if (Config.DEBUG)
		{
			_log.info("[Seed of Destruction] Loaded " + spawnCount + " spawns data.");
			_log.info("[Seed of Destruction] Loaded " + _spawnZoneList.size() + " spawn zones data.");
		}
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		final L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		final L2CommandChannel channel = player.getParty().getCommandChannel();
		if (channel == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		else if (channel.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		else if ((channel.getMemberCount() < Config.MIN_TIAT_PLAYERS) || (channel.getMemberCount() > Config.MAX_TIAT_PLAYERS))
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		for (L2PcInstance partyMember : channel.getMembers())
		{
			if (partyMember.getLevel() < 75)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	protected int enterInstance(L2PcInstance player, String template, Location loc)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof SOD1World))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}
			teleportPlayer(player, loc, world.instanceId);
			return world.instanceId;
		}
		
		if (!checkConditions(player))
		{
			return 0;
		}
		
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new SOD1World();
		world.templateId = INSTANCEID;
		world.instanceId = instanceId;
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);
		spawnState((SOD1World) world);
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
		{
			if (Util.contains(ATTACKABLE_DOORS, door.getDoorId()))
			{
				door.setIsAttackableDoor(true);
			}
		}
		
		if ((player.getParty() == null) || (player.getParty().getCommandChannel() == null))
		{
			teleportPlayer(player, loc, instanceId, false);
			world.allowed.add(player.getObjectId());
		}
		else
		{
			for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
			{
				teleportPlayer(channelMember, loc, instanceId, false);
				world.allowed.add(channelMember.getObjectId());
			}
		}
		return instanceId;
	}
	
	protected boolean checkKillProgress(L2Npc mob, SOD1World world)
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
	
	private void spawnFlaggedNPCs(SOD1World world, int flag)
	{
		if (world.lock.tryLock())
		{
			try
			{
				for (SODSpawn spw : _spawnList.get(flag))
				{
					if (spw.isZone)
					{
						for (int i = 0; i < spw.count; i++)
						{
							if (_spawnZoneList.containsKey(spw.zone))
							{
								int[] point = _spawnZoneList.get(spw.zone).getRandomPoint();
								spawn(world, spw.npcId, point[0], point[1], GeoClient.getInstance().getSpawnHeight(point[0], point[1], point[2]), getRandom(65535), spw.isNeededNextFlag);
							}
							else
							{
								_log.info("[Seed of Destruction] Missing zone: " + spw.zone);
							}
						}
					}
					else
					{
						spawn(world, spw.npcId, spw.x, spw.y, spw.z, spw.h, spw.isNeededNextFlag);
					}
				}
			}
			finally
			{
				world.lock.unlock();
			}
		}
	}
	
	protected boolean spawnState(SOD1World world)
	{
		if (world.lock.tryLock())
		{
			try
			{
				world.npcList.clear();
				switch (world.status)
				{
					case 0:
						spawnFlaggedNPCs(world, 0);
						break;
					case 1:
						ExShowScreenMessage message1 = new ExShowScreenMessage(NpcStringId.THE_ENEMIES_HAVE_ATTACKED_EVERYONE_COME_OUT_AND_FIGHT_URGH, 5, 1);
						sendScreenMessage(world, message1);
						for (int i : ENTRANCE_ROOM_DOORS)
						{
							openDoor(i, world.instanceId);
						}
						spawnFlaggedNPCs(world, 1);
						break;
					case 2:
					case 3:
						return true;
					case 4:
						ExShowScreenMessage message2 = new ExShowScreenMessage(NpcStringId.OBELISK_HAS_COLLAPSED_DONT_LET_THE_ENEMIES_JUMP_AROUND_WILDLY_ANYMORE, 5, 1);
						sendScreenMessage(world, message2);
						for (int i : SQUARE_DOORS)
						{
							openDoor(i, world.instanceId);
						}
						spawnFlaggedNPCs(world, 4);
						break;
					case 5:
						openDoor(SCOUTPASS_DOOR, world.instanceId);
						spawnFlaggedNPCs(world, 3);
						spawnFlaggedNPCs(world, 5);
						break;
					case 6:
						openDoor(THRONE_DOOR, world.instanceId);
						break;
					case 7:
						spawnFlaggedNPCs(world, 7);
						break;
					case 8:
						ExShowScreenMessage message4 = new ExShowScreenMessage(NpcStringId.COME_OUT_WARRIORS_PROTECT_SEED_OF_DESTRUCTION, 5, 1);
						sendScreenMessage(world, message4);
						world.deviceSpawnedMobCount = 0;
						spawnFlaggedNPCs(world, 8);
						break;
					case 9:
						break;
				}
				world.status++;
				return true;
			}
			finally
			{
				world.lock.unlock();
			}
		}
		return false;
	}
	
	protected void spawn(SOD1World world, int npcId, int x, int y, int z, int h, boolean addToKillTable)
	{
		if ((npcId >= 18720) && (npcId <= 18774))
		{
			L2Skill skill = null;
			if (npcId <= 18728)
			{
				skill = TRAP_HOLD.getSkill();
			}
			else if (npcId <= 18736)
			{
				skill = TRAP_STUN.getSkill();
			}
			else if (npcId <= 18770)
			{
				skill = TRAP_DAMAGE.getSkill();
			}
			else
			{
				skill = TRAP_SPAWN.getSkill();
			}
			addTrap(npcId, x, y, z, h, skill, world.instanceId);
			return;
		}
		L2Npc npc = addSpawn(npcId, x, y, z, h, false, 0, false, world.instanceId);
		if (addToKillTable)
		{
			world.npcList.put(npc, false);
		}
		npc.setIsNoRndWalk(true);
		if (npc.isInstanceType(InstanceType.L2Attackable))
		{
			((L2Attackable) npc).setSeeThroughSilentMove(true);
		}
		if (npcId == TIAT_VIDEO_NPC)
		{
			startQuestTimer("DoorCheck", 10000, npc, null);
		}
		else if (npcId == SPAWN_DEVICE)
		{
			npc.disableCoreAI(true);
			startQuestTimer("Spawn", 10000, npc, null, true);
		}
		else if (npcId == TIAT)
		{
			for (int i = 0; i < TIAT_GUARD_NUMBER; i++)
			{
				addMinion((L2MonsterInstance) npc, TIAT_GUARD);
			}
		}
	}
	
	protected void setInstanceTimeRestrictions(SOD1World world)
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, RESET_MIN);
		reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
		
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
		{
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		}
		if (reenter.get(Calendar.DAY_OF_WEEK) <= RESET_DAY_1)
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_1)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		else
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_2)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
		sm.addInstanceName(INSTANCEID);
		
		for (int objectId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			InstanceManager.getInstance().setInstanceTime(objectId, INSTANCEID, reenter.getTimeInMillis());
			if ((player != null) && player.isOnline())
			{
				player.sendPacket(sm);
			}
		}
	}
	
	private void sendScreenMessage(SOD1World world, ExShowScreenMessage message)
	{
		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if (player != null)
			{
				player.sendPacket(message);
			}
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == TIAT_GUARD)
		{
			startQuestTimer("GuardThink", 2500 + getRandom(-200, 200), npc, null, true);
		}
		else
		{
			npc.disableCoreAI(true);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((isSummon == false) && (player != null))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if (tmpworld instanceof SOD1World)
			{
				SOD1World world = (SOD1World) tmpworld;
				if (world.status == 7)
				{
					if (spawnState(world))
					{
						for (int objId : world.allowed)
						{
							L2PcInstance pl = L2World.getInstance().getPlayer(objId);
							if (pl != null)
							{
								pl.showQuestMovie(5);
							}
						}
						npc.deleteMe();
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			if ((world.status == 2) && (npc.getId() == OBELISK))
			{
				world.status = 4;
				spawnFlaggedNPCs(world, 3);
			}
			else if ((world.status == 3) && (npc.getId() == OBELISK))
			{
				world.status = 4;
				spawnFlaggedNPCs(world, 2);
			}
			else if ((world.status <= 8) && (npc.getId() == TIAT))
			{
				if (npc.getCurrentHp() < (npc.getMaxHp() / 2))
				{
					if (spawnState(world))
					{
						startQuestTimer("TiatFullHp", 3000, npc, null);
						setInstanceTimeRestrictions(world);
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			if (event.equalsIgnoreCase("Spawn"))
			{
				L2PcInstance target = L2World.getInstance().getPlayer(world.allowed.get(getRandom(world.allowed.size())));
				if ((world.deviceSpawnedMobCount < MAX_DEVICESPAWNEDMOBCOUNT) && (target != null) && (target.getInstanceId() == npc.getInstanceId()) && !target.isDead())
				{
					L2Attackable mob = (L2Attackable) addSpawn(SPAWN_MOB_IDS[getRandom(SPAWN_MOB_IDS.length)], npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), npc.getSpawn().getHeading(), false, 0, false, world.instanceId);
					world.deviceSpawnedMobCount++;
					mob.setSeeThroughSilentMove(true);
					mob.setRunning();
					if (world.status >= 7)
					{
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_TIAT);
					}
					else
					{
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_DOOR);
					}
				}
			}
			else if (event.equalsIgnoreCase("DoorCheck"))
			{
				L2DoorInstance tmp = getDoor(FORTRESS_DOOR, npc.getInstanceId());
				if (tmp.getCurrentHp() < tmp.getMaxHp())
				{
					world.deviceSpawnedMobCount = 0;
					spawnFlaggedNPCs(world, 6);
					ExShowScreenMessage message3 = new ExShowScreenMessage(NpcStringId.ENEMIES_ARE_TRYING_TO_DESTROY_THE_FORTRESS_EVERYONE_DEFEND_THE_FORTRESS, 5, 1);
					sendScreenMessage(world, message3);
				}
				else
				{
					startQuestTimer("DoorCheck", 10000, npc, null);
				}
			}
			else if (event.equalsIgnoreCase("TiatFullHp"))
			{
				if (!npc.isStunned() && !npc.isInvul())
				{
					npc.setCurrentHp(npc.getMaxHp());
				}
			}
			else if (event.equalsIgnoreCase("BodyGuardThink"))
			{
				L2Character mostHate = ((L2Attackable) npc).getMostHated();
				if (mostHate != null)
				{
					double dist = Util.calculateDistance(mostHate.getXdestination(), mostHate.getYdestination(), npc.getSpawn().getX(), npc.getSpawn().getY());
					if (dist > 900)
					{
						((L2Attackable) npc).reduceHate(mostHate, ((L2Attackable) npc).getHating(mostHate));
					}
					mostHate = ((L2Attackable) npc).getMostHated();
					if ((mostHate != null) || (((L2Attackable) npc).getHating(mostHate) < 5))
					{
						((L2Attackable) npc).returnHome();
					}
				}
			}
		}
		return "";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (npc.getId() == SPAWN_DEVICE)
		{
			this.cancelQuestTimer("Spawn", npc, null);
			return "";
		}
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			if (world.status == 1)
			{
				if (checkKillProgress(npc, world))
				{
					spawnState(world);
				}
			}
			else if (world.status == 2)
			{
				if (checkKillProgress(npc, world))
				{
					world.status++;
				}
			}
			else if ((world.status == 4) && (npc.getId() == OBELISK))
			{
				spawnState(world);
			}
			else if ((world.status == 5) && (npc.getId() == POWERFUL_DEVICE))
			{
				if (checkKillProgress(npc, world))
				{
					spawnState(world);
				}
			}
			else if ((world.status == 6) && (npc.getId() == THRONE_POWERFUL_DEVICE))
			{
				if (checkKillProgress(npc, world))
				{
					spawnState(world);
				}
			}
			else if (world.status >= 7)
			{
				if (npc.getId() == TIAT)
				{
					world.status++;
					for (int objId : world.allowed)
					{
						L2PcInstance pl = L2World.getInstance().getPlayer(objId);
						if (pl != null)
						{
							pl.showQuestMovie(6);
						}
					}
					for (L2Npc mob : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
					{
						mob.deleteMe();
					}
					
					SoDManager.getInstance().increaseSoDTiatKilled();
				}
				else if (npc.getId() == TIAT_GUARD)
				{
					addMinion(((L2MonsterInstance) npc).getLeader(), TIAT_GUARD);
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
		if (npcId == ALENOS)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if ((SoDManager.getInstance().getSoDState() == 1) || ((world != null) && (world instanceof SOD1World)))
			{
				enterInstance(player, "SeedOfDestructionStage1.xml", ENTER_TELEPORT_1);
			}
			else if (SoDManager.getInstance().getSoDState() == 2)
			{
				teleportPlayer(player, ENTER_TELEPORT_2, 0, false);
			}
		}
		else if (npcId == TELEPORT)
		{
			teleportPlayer(player, CENTER_TELEPORT, player.getInstanceId(), false);
		}
		return "";
	}
	
	@Override
	public String onTrapAction(L2TrapInstance trap, L2Character trigger, TrapAction action)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(trap.getInstanceId());
		if (tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			switch (action)
			{
				case TRAP_TRIGGERED:
					if (trap.getId() == 18771)
					{
						for (int npcId : TRAP_18771_NPCS)
						{
							addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0, true, world.instanceId);
						}
					}
					else
					{
						for (int npcId : TRAP_OTHER_NPCS)
						{
							addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0, true, world.instanceId);
						}
					}
					break;
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Stage1(-1, qn, "instances");
	}
}