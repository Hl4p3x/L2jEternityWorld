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
import java.util.concurrent.ScheduledFuture;
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
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.L2CommandChannel;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2Territory;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import l2e.gameserver.network.serverpackets.Earthquake;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.MagicSkillCanceld;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

public class FinalEmperialTomb extends Quest
{
	private class FETWorld extends InstanceWorld
	{
		public Lock lock = new ReentrantLock();
		public FastList<L2Npc> npcList = new FastList<>();
		public int darkChoirPlayerCount = 0;
		public FrintezzaSong OnSong = null;
		public ScheduledFuture<?> songTask = null;
		public ScheduledFuture<?> songEffectTask = null;
		public boolean isVideo = false;
		public L2Npc frintezzaDummy = null;
		public L2Npc overheadDummy = null;
		public L2Npc portraitDummy1 = null;
		public L2Npc portraitDummy3 = null;
		public L2Npc scarletDummy = null;
		public L2GrandBossInstance frintezza = null;
		public L2GrandBossInstance activeScarlet = null;
		public List<L2MonsterInstance> demons = new FastList<>();
		public Map<L2MonsterInstance, Integer> portraits = new FastMap<>();
		public int scarlet_x = 0;
		public int scarlet_y = 0;
		public int scarlet_z = 0;
		public int scarlet_h = 0;
		public int scarlet_a = 0;
		
		public FETWorld()
		{
			npcList.shared();
		}
	}
	
	protected static class FETSpawn
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
	
	private static class FrintezzaSong
	{
		public SkillsHolder skill;
		public SkillsHolder effectSkill;
		public NpcStringId songName;
		public int chance;
		
		public FrintezzaSong(SkillsHolder sk, SkillsHolder esk, NpcStringId sn, int ch)
		{
			skill = sk;
			effectSkill = esk;
			songName = sn;
			chance = ch;
		}
	}
	
	private static final int INSTANCEID = 136;
	private static final int MIN_PLAYERS = Config.MIN_FRINTEZZA_PLAYERS;
	private static final int MAX_PLAYERS = Config.MAX_FRINTEZZA_PLAYERS;
	private static final boolean debug = false;
	
	private final Map<Integer, L2Territory> _spawnZoneList = new HashMap<>();
	private final Map<Integer, List<FETSpawn>> _spawnList = new HashMap<>();
	private final List<Integer> _mustKillMobsId = new FastList<>();
	
	private static final Location ENTER_TELEPORT = new Location(-88015, -141153, -9168);
	
	private static final int GUIDE = 32011;
	private static final int CUBE = 29061;
	
	private static final int DEWDROP_OF_DESTRUCTION_ITEM_ID = 8556;
	
	private static final int SCARLET1 = 29046;
	private static final int SCARLET2 = 29047;
	private static final int FRINTEZZA = 29045;
	private static final int[] PORTRAITS =
	{
		29048,
		29049
	};
	private static final int[] DEMONS =
	{
		29050,
		29051
	};
	private static final int HALL_ALARM = 18328;
	private static final int HALL_KEEPER_CAPTAIN = 18329;
	private static final int HALL_KEEPER_SUICIDAL_SOLDIER = 18333;
	private static final int DARK_CHOIR_PLAYER = 18339;
	private static final int[] AI_DISABLED_MOBS =
	{
		18328
	};
	
	private static final int FIRST_SCARLET_WEAPON = 8204;
	private static final int SECOND_SCARLET_WEAPON = 7903;
	protected static final SkillsHolder INTRO_SKILL = new SkillsHolder(5004, 1);
	private static final SkillsHolder FIRST_MORPH_SKILL = new SkillsHolder(5017, 1);
	
	protected static final FrintezzaSong[] FRINTEZZASONGLIST =
	{
		new FrintezzaSong(new SkillsHolder(5007, 1), new SkillsHolder(5008, 1), NpcStringId.REQUIEM_OF_HATRED, 5),
		new FrintezzaSong(new SkillsHolder(5007, 2), new SkillsHolder(5008, 2), NpcStringId.RONDO_OF_SOLITUDE, 50),
		new FrintezzaSong(new SkillsHolder(5007, 3), new SkillsHolder(5008, 3), NpcStringId.FRENETIC_TOCCATA, 70),
		new FrintezzaSong(new SkillsHolder(5007, 4), new SkillsHolder(5008, 4), NpcStringId.FUGUE_OF_JUBILATION, 90),
		new FrintezzaSong(new SkillsHolder(5007, 5), new SkillsHolder(5008, 5), NpcStringId.HYPNOTIC_MAZURKA, 100),
	};
	
	private static final int DEWDROP_OF_DESTRUCTION_SKILL_ID = 2276;
	private static final int SOUL_BREAKING_ARROW_SKILL_ID = 2234;
	
	protected static final int[] FIRST_ROOM_DOORS =
	{
		17130051,
		17130052,
		17130053,
		17130054,
		17130055,
		17130056,
		17130057,
		17130058
	};
	
	protected static final int[] SECOND_ROOM_DOORS =
	{
		17130061,
		17130062,
		17130063,
		17130064,
		17130065,
		17130066,
		17130067,
		17130068,
		17130069,
		17130070
	};
	
	protected static final int[] FIRST_ROUTE_DOORS =
	{
		17130042,
		17130043
	};
	
	protected static final int[] SECOND_ROUTE_DOORS =
	{
		17130045,
		17130046
	};
	
	protected static final Location MOVE_TO_CENTER = new Location(-87904, -141296, -9168, 0);
	
	private static final int TIME_BETWEEN_DEMON_SPAWNS = 20000;
	private static final int MAX_DEMONS = 24;
	protected static final int[][] PORTRAIT_SPAWNS =
	{
		{
			29048,
			-89381,
			-153981,
			-9168,
			3368,
			-89378,
			-153968,
			-9168,
			3368
		},
		{
			29048,
			-86234,
			-152467,
			-9168,
			37656,
			-86261,
			-152492,
			-9168,
			37656
		},
		{
			29049,
			-89342,
			-152479,
			-9168,
			-5152,
			-89311,
			-152491,
			-9168,
			-5152
		},
		{
			29049,
			-86189,
			-153968,
			-9168,
			29456,
			-86217,
			-153956,
			-9168,
			29456
		}
	};
	
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	private static final int RESET_DAY_1 = 4;
	private static final int RESET_DAY_2 = 7;
	protected int spawnCount;
	
	public FinalEmperialTomb(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		load();
		addAttackId(SCARLET1, FRINTEZZA);
		addAttackId(PORTRAITS);
		addStartNpc(GUIDE, CUBE);
		addTalkId(GUIDE, CUBE);
		addKillId(HALL_ALARM, HALL_KEEPER_CAPTAIN, DARK_CHOIR_PLAYER, SCARLET2);
		addKillId(PORTRAITS);
		addKillId(DEMONS);
		addKillId(_mustKillMobsId);
		addSpellFinishedId(HALL_KEEPER_SUICIDAL_SOLDIER);
	}
	
	protected void load()
	{
		spawnCount = 0;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT + "/data/spawnZones/final_emperial_tomb.xml");
			if (!file.exists())
			{
				_log.severe("[Final Emperial Tomb] Missing final_emperial_tomb.xml. The quest wont work without it!");
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
									_log.severe("[Final Emperial Tomb] Missing npcId in npc List, skipping");
									continue;
								}
								int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());
								
								att = attrs.getNamedItem("flag");
								if (att == null)
								{
									_log.severe("[Final Emperial Tomb] Missing flag in npc List npcId: " + npcId + ", skipping");
									continue;
								}
								int flag = Integer.parseInt(attrs.getNamedItem("flag").getNodeValue());
								if (!_spawnList.containsKey(flag))
								{
									_spawnList.put(flag, new FastList<FETSpawn>());
								}
								
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if ("loc".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										FETSpawn spw = new FETSpawn();
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
										FETSpawn spw = new FETSpawn();
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
									_log.severe("[Final Emperial Tomb] Missing id in spawnZones List, skipping");
									continue;
								}
								int id = Integer.parseInt(att.getNodeValue());
								att = attrs.getNamedItem("minZ");
								if (att == null)
								{
									_log.severe("[Final Emperial Tomb] Missing minZ in spawnZones List id: " + id + ", skipping");
									continue;
								}
								int minz = Integer.parseInt(att.getNodeValue());
								att = attrs.getNamedItem("maxZ");
								if (att == null)
								{
									_log.severe("[Final Emperial Tomb] Missing maxZ in spawnZones List id: " + id + ", skipping");
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
			_log.log(Level.WARNING, "[Final Emperial Tomb] Could not parse final_emperial_tomb.xml file: " + e.getMessage(), e);
		}
		if (debug)
		{
			_log.info("[Final Emperial Tomb] Loaded " + _spawnZoneList.size() + " spawn zones data.");
			_log.info("[Final Emperial Tomb] Loaded " + spawnCount + " spawns data.");
		}
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		if (debug || player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
		{
			return true;
		}
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		L2CommandChannel channel = player.getParty().getCommandChannel();
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
		else if (player.getInventory().getItemByItemId(8073) == null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		else if ((channel.getMemberCount() < MIN_PLAYERS) || (channel.getMemberCount() > MAX_PLAYERS))
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		for (L2PcInstance channelMember : channel.getMembers())
		{
			if (channelMember.getLevel() < 80)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(channelMember);
				party.broadcastPacket(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, channelMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(channelMember);
				party.broadcastPacket(sm);
				return false;
			}
			Long reentertime = InstanceManager.getInstance().getInstanceTime(channelMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(channelMember);
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
			if (!(world instanceof FETWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}
			teleportPlayer(player, loc, world.instanceId, false);
			return world.instanceId;
		}
		
		if (!checkConditions(player))
		{
			return 0;
		}
		if (!player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS) && !player.destroyItemByItemId("QUEST", 8073, 1, player, true))
		{
			return 0;
		}
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new FETWorld();
		world.templateId = INSTANCEID;
		world.instanceId = instanceId;
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);
		controlStatus((FETWorld) world);
		
		if ((player.getParty() == null) || (player.getParty().getCommandChannel() == null))
		{
			player.destroyItemByItemId(getName(), DEWDROP_OF_DESTRUCTION_ITEM_ID, player.getInventory().getInventoryItemCount(DEWDROP_OF_DESTRUCTION_ITEM_ID, -1), null, true);
			world.allowed.add(player.getObjectId());
			teleportPlayer(player, loc, instanceId, false);
		}
		else
		{
			for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
			{
				channelMember.destroyItemByItemId(getName(), DEWDROP_OF_DESTRUCTION_ITEM_ID, channelMember.getInventory().getInventoryItemCount(DEWDROP_OF_DESTRUCTION_ITEM_ID, -1), null, true);
				world.allowed.add(channelMember.getObjectId());
				teleportPlayer(channelMember, loc, instanceId, false);
			}
		}
		return instanceId;
	}
	
	protected boolean checkKillProgress(L2Npc mob, FETWorld world)
	{
		if (world.npcList.contains(mob))
		{
			world.npcList.remove(mob);
		}
		return world.npcList.isEmpty();
	}
	
	private void spawnFlaggedNPCs(FETWorld world, int flag)
	{
		if (world.lock.tryLock())
		{
			try
			{
				for (FETSpawn spw : _spawnList.get(flag))
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
								_log.info("[Final Emperial Tomb] Missing zone: " + spw.zone);
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
	
	protected boolean controlStatus(FETWorld world)
	{
		if (world.lock.tryLock())
		{
			try
			{
				if (debug)
				{
					_log.info("[Final Emperial Tomb] Starting " + world.status + ". status.");
				}
				world.npcList.clear();
				switch (world.status)
				{
					case 0:
						spawnFlaggedNPCs(world, 0);
						break;
					case 1:
						for (int doorId : FIRST_ROUTE_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						spawnFlaggedNPCs(world, world.status);
						break;
					case 2:
						for (int doorId : SECOND_ROUTE_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 0), 600000);
						break;
					case 3:
						if (world.songEffectTask != null)
						{
							world.songEffectTask.cancel(false);
						}
						world.songEffectTask = null;
						world.activeScarlet.setIsInvul(true);
						if (world.activeScarlet.isCastingNow())
						{
							world.activeScarlet.abortCast();
						}
						setInstanceTimeRestrictions(world);
						world.activeScarlet.doCast(FIRST_MORPH_SKILL.getSkill());
						ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(world, 2), 1500);
						break;
					case 4:
						world.isVideo = true;
						broadCastPacket(world, new MagicSkillCanceld(world.frintezza.getObjectId()));
						if (world.songEffectTask != null)
						{
							world.songEffectTask.cancel(false);
						}
						world.songEffectTask = null;
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 23), 2000);
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 24), 2100);
						break;
					case 5:
						world.isVideo = true;
						broadCastPacket(world, new MagicSkillCanceld(world.frintezza.getObjectId()));
						if (world.songTask != null)
						{
							world.songTask.cancel(true);
						}
						if (world.songEffectTask != null)
						{
							world.songEffectTask.cancel(false);
						}
						world.songTask = null;
						world.songEffectTask = null;
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 33), 500);
						break;
					case 6:
						InstanceManager.getInstance().getInstance(world.instanceId).setDuration(300000);
						for (int doorId : FIRST_ROOM_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						for (int doorId : FIRST_ROUTE_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						for (int doorId : SECOND_ROUTE_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						for (int doorId : SECOND_ROOM_DOORS)
						{
							closeDoor(doorId, world.instanceId);
						}
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
	
	protected void spawn(FETWorld world, int npcId, int x, int y, int z, int h, boolean addToKillTable)
	{
		L2Npc npc = addSpawn(npcId, x, y, z, h, false, 0, false, world.instanceId);
		if (addToKillTable)
		{
			world.npcList.add(npc);
		}
		npc.setIsNoRndWalk(true);
		if (npc.isInstanceType(InstanceType.L2Attackable))
		{
			((L2Attackable) npc).setSeeThroughSilentMove(true);
		}
		if (Util.contains(AI_DISABLED_MOBS, npcId))
		{
			npc.disableCoreAI(true);
		}
		if (npcId == DARK_CHOIR_PLAYER)
		{
			world.darkChoirPlayerCount++;
		}
	}
	
	private class DemonSpawnTask implements Runnable
	{
		private final FETWorld _world;
		
		DemonSpawnTask(FETWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			if ((InstanceManager.getInstance().getWorld(_world.instanceId) != _world) || _world.portraits.isEmpty())
			{
				if (debug)
				{
					_log.info("[Final Emperial Tomb] Instance is deleted or all Portraits is killed.");
				}
				return;
			}
			for (int i : _world.portraits.values())
			{
				if (_world.demons.size() > MAX_DEMONS)
				{
					break;
				}
				L2MonsterInstance demon = (L2MonsterInstance) addSpawn(PORTRAIT_SPAWNS[i][0] + 2, PORTRAIT_SPAWNS[i][5], PORTRAIT_SPAWNS[i][6], PORTRAIT_SPAWNS[i][7], PORTRAIT_SPAWNS[i][8], false, 0, false, _world.instanceId);
				updateKnownList(_world, demon);
				_world.demons.add(demon);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new DemonSpawnTask(_world), TIME_BETWEEN_DEMON_SPAWNS);
		}
	}
	
	private class SoulBreakingArrow implements Runnable
	{
		private final L2Npc _npc;
		
		protected SoulBreakingArrow(L2Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			_npc.setScriptValue(0);
		}
	}
	
	private class SongTask implements Runnable
	{
		private final FETWorld _world;
		private final int _status;
		
		SongTask(FETWorld world, int status)
		{
			_world = world;
			_status = status;
		}
		
		@Override
		public void run()
		{
			if (InstanceManager.getInstance().getWorld(_world.instanceId) != _world)
			{
				return;
			}
			switch (_status)
			{
				case 0:
					if (_world.isVideo)
					{
						_world.songTask = ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(_world, 0), 1000);
					}
					else if ((_world.frintezza != null) && !_world.frintezza.isDead())
					{
						if (_world.frintezza.getScriptValue() != 1)
						{
							int rnd = getRandom(100);
							for (FrintezzaSong element : FRINTEZZASONGLIST)
							{
								if (rnd < element.chance)
								{
									_world.OnSong = element;
									broadCastPacket(_world, new ExShowScreenMessage(2, -1, 2, 0, 0, 0, 0, true, 4000, false, null, element.songName, null));
									broadCastPacket(_world, new MagicSkillUse(_world.frintezza, _world.frintezza, element.skill.getSkillId(), element.skill.getSkillLvl(), element.skill.getSkill().getHitTime(), 0));
									_world.songEffectTask = ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(_world, 1), element.skill.getSkill().getHitTime() - 10000);
									_world.songTask = ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(_world, 0), element.skill.getSkill().getHitTime());
									break;
								}
							}
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new SoulBreakingArrow(_world.frintezza), 35000);
						}
					}
					break;
				case 1:
					_world.songEffectTask = null;
					L2Skill skill = _world.OnSong.effectSkill.getSkill();
					if (skill == null)
					{
						return;
					}
					
					if ((_world.frintezza != null) && !_world.frintezza.isDead() && (_world.activeScarlet != null) && !_world.activeScarlet.isDead())
					{
						List<L2Character> targetList = new FastList<>();
						if (skill.hasEffectType(L2EffectType.STUN) || (skill.getSkillType() == L2SkillType.DEBUFF))
						{
							for (int objId : _world.allowed)
							{
								L2PcInstance player = L2World.getInstance().getPlayer(objId);
								if ((player != null) && player.isOnline() && (player.getInstanceId() == _world.instanceId))
								{
									if (!player.isDead())
									{
										targetList.add(player);
									}
									if (player.hasSummon() && !player.getSummon().isDead())
									{
										targetList.add(player.getSummon());
									}
								}
							}
						}
						else
						{
							targetList.add(_world.activeScarlet);
						}
						if (targetList.size() > 0)
						{
							_world.frintezza.doCast(skill, targetList.get(0), targetList.toArray(new L2Character[targetList.size()]));
						}
					}
					break;
				case 2:
					_world.activeScarlet.setRHandId(SECOND_SCARLET_WEAPON);
					_world.activeScarlet.setIsInvul(false);
					break;
			}
		}
	}
	
	private class IntroTask implements Runnable
	{
		private final FETWorld _world;
		private final int _status;
		
		IntroTask(FETWorld world, int status)
		{
			_world = world;
			_status = status;
		}
		
		@Override
		public void run()
		{
			switch (_status)
			{
				case 0:
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 1), 27000);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 2), 30000);
					broadCastPacket(_world, new Earthquake(-87784, -155083, -9087, 45, 27));
					break;
				case 1:
					for (int doorId : FIRST_ROOM_DOORS)
					{
						closeDoor(doorId, _world.instanceId);
					}
					for (int doorId : FIRST_ROUTE_DOORS)
					{
						closeDoor(doorId, _world.instanceId);
					}
					for (int doorId : SECOND_ROOM_DOORS)
					{
						closeDoor(doorId, _world.instanceId);
					}
					for (int doorId : SECOND_ROUTE_DOORS)
					{
						closeDoor(doorId, _world.instanceId);
					}
					addSpawn(29061, -87904, -141296, -9168, 0, false, 0, false, _world.instanceId);
					break;
				case 2:
					_world.frintezzaDummy = addSpawn(29052, -87784, -155083, -9087, 16048, false, 0, false, _world.instanceId);
					_world.frintezzaDummy.setIsInvul(true);
					_world.frintezzaDummy.setIsImmobilized(true);
					
					_world.overheadDummy = addSpawn(29052, -87784, -153298, -9175, 16384, false, 0, false, _world.instanceId);
					_world.overheadDummy.setIsInvul(true);
					_world.overheadDummy.setIsImmobilized(true);
					_world.overheadDummy.setCollisionHeight(600);
					broadCastPacket(_world, new NpcInfo(_world.overheadDummy, null));
					
					_world.portraitDummy1 = addSpawn(29052, -89566, -153168, -9165, 16048, false, 0, false, _world.instanceId);
					_world.portraitDummy1.setIsImmobilized(true);
					_world.portraitDummy1.setIsInvul(true);
					
					_world.portraitDummy3 = addSpawn(29052, -86004, -153168, -9165, 16048, false, 0, false, _world.instanceId);
					_world.portraitDummy3.setIsImmobilized(true);
					_world.portraitDummy3.setIsInvul(true);
					
					_world.scarletDummy = addSpawn(29053, -87784, -153298, -9175, 16384, false, 0, false, _world.instanceId);
					_world.scarletDummy.setIsInvul(true);
					_world.scarletDummy.setIsImmobilized(true);
					
					stopPc();
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 3), 1000);
					break;
				case 3:
					broadCastPacket(_world, new SpecialCamera(_world.overheadDummy, 0, 75, -89, 0, 100, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SpecialCamera(_world.overheadDummy, 0, 75, -89, 0, 100, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SpecialCamera(_world.overheadDummy, 300, 90, -10, 6500, 7000, 0, 0, 1, 0, 0));
					
					_world.frintezza = (L2GrandBossInstance) addSpawn(FRINTEZZA, -87780, -155086, -9080, 16384, false, 0, false, _world.instanceId);
					_world.frintezza.setIsImmobilized(true);
					_world.frintezza.setIsInvul(true);
					_world.frintezza.disableAllSkills();
					updateKnownList(_world, _world.frintezza);
					
					for (int[] element : PORTRAIT_SPAWNS)
					{
						L2MonsterInstance demon = (L2MonsterInstance) addSpawn(element[0] + 2, element[5], element[6], element[7], element[8], false, 0, false, _world.instanceId);
						demon.setIsImmobilized(true);
						demon.disableAllSkills();
						updateKnownList(_world, demon);
						_world.demons.add(demon);
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 4), 6500);
					break;
				case 4:
					broadCastPacket(_world, new SpecialCamera(_world.frintezzaDummy, 1800, 90, 8, 6500, 7000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 5), 900);
					break;
				case 5:
					broadCastPacket(_world, new SpecialCamera(_world.frintezzaDummy, 140, 90, 10, 2500, 4500, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 6), 4000);
					break;
				case 6:
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 40, 75, -10, 0, 1000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 40, 75, -10, 0, 12000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 7), 1350);
					break;
				case 7:
					broadCastPacket(_world, new SocialAction(_world.frintezza.getObjectId(), 2));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 8), 7000);
					break;
				case 8:
					_world.frintezzaDummy.deleteMe();
					_world.frintezzaDummy = null;
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 9), 1000);
					break;
				case 9:
					broadCastPacket(_world, new SocialAction(_world.demons.get(1).getObjectId(), 1));
					broadCastPacket(_world, new SocialAction(_world.demons.get(2).getObjectId(), 1));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 10), 400);
					break;
				case 10:
					broadCastPacket(_world, new SocialAction(_world.demons.get(0).getObjectId(), 1));
					broadCastPacket(_world, new SocialAction(_world.demons.get(3).getObjectId(), 1));
					sendPacketX(new SpecialCamera(_world.portraitDummy1, 1000, 118, 0, 0, 1000, 0, 0, 1, 0, 0), new SpecialCamera(_world.portraitDummy3, 1000, 62, 0, 0, 1000, 0, 0, 1, 0, 0), -87784);
					sendPacketX(new SpecialCamera(_world.portraitDummy1, 1000, 118, 0, 0, 10000, 0, 0, 1, 0, 0), new SpecialCamera(_world.portraitDummy3, 1000, 62, 0, 0, 10000, 0, 0, 1, 0, 0), -87784);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 11), 2000);
					break;
				case 11:
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 240, 90, 0, 0, 1000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 240, 90, 25, 5500, 10000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SocialAction(_world.frintezza.getObjectId(), 3));
					_world.portraitDummy1.deleteMe();
					_world.portraitDummy3.deleteMe();
					_world.portraitDummy1 = null;
					_world.portraitDummy3 = null;
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 12), 4500);
					break;
				case 12:
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 100, 195, 35, 0, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 13), 700);
					break;
				case 13:
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 100, 195, 35, 0, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 14), 1300);
					break;
				case 14:
					broadCastPacket(_world, new ExShowScreenMessage(NpcStringId.MOURNFUL_CHORALE_PRELUDE, 2, 5000));
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 120, 180, 45, 1500, 10000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new MagicSkillUse(_world.frintezza, _world.frintezza, 5006, 1, 34000, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 15), 1500);
					break;
				case 15:
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 520, 135, 45, 8000, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 16), 7500);
					break;
				case 16:
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 1500, 110, 25, 10000, 13000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 17), 9500);
					break;
				case 17:
					broadCastPacket(_world, new SpecialCamera(_world.overheadDummy, 930, 160, -20, 0, 1000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SpecialCamera(_world.overheadDummy, 600, 180, -25, 0, 10000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new MagicSkillUse(_world.scarletDummy, _world.overheadDummy, 5004, 1, 5800, 0));
					
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 18), 5000);
					break;
				case 18:
					_world.activeScarlet = (L2GrandBossInstance) addSpawn(29046, -87789, -153295, -9176, 16384, false, 0, false, _world.instanceId);
					_world.activeScarlet.setRHandId(FIRST_SCARLET_WEAPON);
					_world.activeScarlet.setIsInvul(true);
					_world.activeScarlet.setIsImmobilized(true);
					_world.activeScarlet.disableAllSkills();
					updateKnownList(_world, _world.activeScarlet);
					broadCastPacket(_world, new SocialAction(_world.activeScarlet.getObjectId(), 3));
					broadCastPacket(_world, new SpecialCamera(_world.scarletDummy, 800, 180, 10, 1000, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 19), 2100);
					break;
				case 19:
					broadCastPacket(_world, new SpecialCamera(_world.activeScarlet, 300, 60, 8, 0, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 20), 2000);
					break;
				case 20:
					broadCastPacket(_world, new SpecialCamera(_world.activeScarlet, 500, 90, 10, 3000, 5000, 0, 0, 1, 0, 0));
					_world.songTask = ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(_world, 0), 100);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 21), 3000);
					break;
				case 21:
					for (int i = 0; i < PORTRAIT_SPAWNS.length; i++)
					{
						L2MonsterInstance portrait = (L2MonsterInstance) addSpawn(PORTRAIT_SPAWNS[i][0], PORTRAIT_SPAWNS[i][1], PORTRAIT_SPAWNS[i][2], PORTRAIT_SPAWNS[i][3], PORTRAIT_SPAWNS[i][4], false, 0, false, _world.instanceId);
						updateKnownList(_world, portrait);
						_world.portraits.put(portrait, i);
					}
					
					_world.overheadDummy.deleteMe();
					_world.scarletDummy.deleteMe();
					_world.overheadDummy = null;
					_world.scarletDummy = null;
					
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 22), 2000);
					break;
				case 22:
					for (L2MonsterInstance demon : _world.demons)
					{
						demon.setIsImmobilized(false);
						demon.enableAllSkills();
					}
					_world.activeScarlet.setIsInvul(false);
					_world.activeScarlet.setIsImmobilized(false);
					_world.activeScarlet.enableAllSkills();
					_world.activeScarlet.setRunning();
					_world.activeScarlet.doCast(INTRO_SKILL.getSkill());
					_world.frintezza.enableAllSkills();
					_world.frintezza.disableCoreAI(true);
					_world.frintezza.setIsMortal(false);
					startPc();
					
					ThreadPoolManager.getInstance().scheduleGeneral(new DemonSpawnTask(_world), TIME_BETWEEN_DEMON_SPAWNS);
					break;
				case 23:
					broadCastPacket(_world, new SocialAction(_world.frintezza.getObjectId(), 4));
					break;
				case 24:
					stopPc();
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 250, 120, 15, 0, 1000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 250, 120, 15, 0, 10000, 0, 0, 1, 0, 0));
					_world.activeScarlet.abortAttack();
					_world.activeScarlet.abortCast();
					_world.activeScarlet.setIsInvul(true);
					_world.activeScarlet.setIsImmobilized(true);
					_world.activeScarlet.disableAllSkills();
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 25), 7000);
					break;
				case 25:
					broadCastPacket(_world, new MagicSkillUse(_world.frintezza, _world.frintezza, 5006, 1, 34000, 0));
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 500, 70, 15, 3000, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 26), 3000);
					break;
				case 26:
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 2500, 90, 12, 6000, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 27), 3000);
					break;
				case 27:
					_world.scarlet_x = _world.activeScarlet.getX();
					_world.scarlet_y = _world.activeScarlet.getY();
					_world.scarlet_z = _world.activeScarlet.getZ();
					_world.scarlet_h = _world.activeScarlet.getHeading();
					if (_world.scarlet_h < 32768)
					{
						_world.scarlet_a = Math.abs(180 - (int) (_world.scarlet_h / 182.044444444));
					}
					else
					{
						_world.scarlet_a = Math.abs(540 - (int) (_world.scarlet_h / 182.044444444));
					}
					broadCastPacket(_world, new SpecialCamera(_world.activeScarlet, 250, _world.scarlet_a, 12, 0, 1000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SpecialCamera(_world.activeScarlet, 250, _world.scarlet_a, 12, 0, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 28), 500);
					break;
				case 28:
					_world.activeScarlet.doDie(_world.activeScarlet);
					broadCastPacket(_world, new SpecialCamera(_world.activeScarlet, 450, _world.scarlet_a, 14, 8000, 8000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 29), 6250);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 30), 7200);
					break;
				case 29:
					_world.activeScarlet.deleteMe();
					_world.activeScarlet = null;
					break;
				case 30:
					_world.activeScarlet = (L2GrandBossInstance) addSpawn(SCARLET2, _world.scarlet_x, _world.scarlet_y, _world.scarlet_z, _world.scarlet_h, false, 0, false, _world.instanceId);
					_world.activeScarlet.setIsInvul(true);
					_world.activeScarlet.setIsImmobilized(true);
					_world.activeScarlet.disableAllSkills();
					updateKnownList(_world, _world.activeScarlet);
					
					broadCastPacket(_world, new SpecialCamera(_world.activeScarlet, 450, _world.scarlet_a, 12, 500, 14000, 0, 0, 1, 0, 0));
					
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 31), 8100);
					break;
				case 31:
					broadCastPacket(_world, new SocialAction(_world.activeScarlet.getObjectId(), 2));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 32), 9000);
					break;
				case 32:
					startPc();
					_world.activeScarlet.setIsInvul(false);
					_world.activeScarlet.setIsImmobilized(false);
					_world.activeScarlet.enableAllSkills();
					_world.isVideo = false;
					break;
				case 33:
					broadCastPacket(_world, new SpecialCamera(_world.activeScarlet, 300, _world.scarlet_a - 180, 5, 0, 7000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SpecialCamera(_world.activeScarlet, 200, _world.scarlet_a, 85, 4000, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 34), 7400);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 35), 7500);
					break;
				case 34:
					_world.frintezza.doDie(_world.frintezza);
					break;
				case 35:
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 100, 120, 5, 0, 7000, 0, 0, 1, 0, 0));
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 100, 90, 5, 5000, 15000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 36), 7000);
					break;
				case 36:
					broadCastPacket(_world, new SpecialCamera(_world.frintezza, 900, 90, 25, 7000, 10000, 0, 0, 1, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(_world, 37), 9000);
					break;
				case 37:
					controlStatus(_world);
					_world.isVideo = false;
					startPc();
					break;
			}
		}
		
		private void stopPc()
		{
			for (int objId : _world.allowed)
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objId);
				if ((player != null) && player.isOnline() && (player.getInstanceId() == _world.instanceId))
				{
					player.abortAttack();
					player.abortCast();
					player.disableAllSkills();
					player.setTarget(null);
					player.stopMove(null);
					player.setIsImmobilized(true);
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
			}
		}
		
		private void startPc()
		{
			for (int objId : _world.allowed)
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objId);
				if ((player != null) && player.isOnline() && (player.getInstanceId() == _world.instanceId))
				{
					player.enableAllSkills();
					player.setIsImmobilized(false);
				}
			}
		}
		
		private void sendPacketX(L2GameServerPacket packet1, L2GameServerPacket packet2, int x)
		{
			for (int objId : _world.allowed)
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objId);
				if ((player != null) && player.isOnline() && (player.getInstanceId() == _world.instanceId))
				{
					if (player.getX() < x)
					{
						player.sendPacket(packet1);
					}
					else
					{
						player.sendPacket(packet2);
					}
				}
			}
		}
	}
	
	private class StatusTask implements Runnable
	{
		private final FETWorld _world;
		private final int _status;
		
		StatusTask(FETWorld world, int status)
		{
			_world = world;
			_status = status;
		}
		
		@Override
		public void run()
		{
			if (InstanceManager.getInstance().getWorld(_world.instanceId) != _world)
			{
				return;
			}
			switch (_status)
			{
				case 0:
					ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(_world, 1), 2000);
					for (int doorId : FIRST_ROOM_DOORS)
					{
						openDoor(doorId, _world.instanceId);
					}
					break;
				case 1:
					addAggroToMobs();
					break;
				case 2:
					ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(_world, 3), 100);
					for (int doorId : SECOND_ROOM_DOORS)
					{
						openDoor(doorId, _world.instanceId);
					}
					break;
				case 3:
					addAggroToMobs();
					break;
				case 4:
					controlStatus(_world);
					break;
			}
		}
		
		private void addAggroToMobs()
		{
			L2PcInstance target = L2World.getInstance().getPlayer(_world.allowed.get(getRandom(_world.allowed.size())));
			if ((target == null) || (target.getInstanceId() != _world.instanceId) || target.isDead() || target.isFakeDeath())
			{
				for (int objId : _world.allowed)
				{
					target = L2World.getInstance().getPlayer(objId);
					if ((target != null) && (target.getInstanceId() == _world.instanceId) && !target.isDead() && !target.isFakeDeath())
					{
						break;
					}
					target = null;
				}
			}
			for (L2Npc mob : _world.npcList)
			{
				mob.setRunning();
				if (target != null)
				{
					((L2MonsterInstance) mob).addDamageHate(target, 0, 500);
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
				else
				{
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_CENTER);
				}
			}
		}
	}
	
	protected void setInstanceTimeRestrictions(FETWorld world)
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
	
	protected void broadCastPacket(FETWorld world, L2GameServerPacket packet)
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
	
	protected void updateKnownList(FETWorld world, L2Npc npc)
	{
		Map<Integer, L2PcInstance> npcKnownPlayers = npc.getKnownList().getKnownPlayers();
		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if ((player != null) && player.isOnline() && (player.getInstanceId() == world.instanceId))
			{
				npcKnownPlayers.put(player.getObjectId(), player);
			}
		}
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FETWorld)
		{
			final FETWorld world = (FETWorld) tmpworld;
			if ((npc.getId() == SCARLET1) && (world.status == 3) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.80)))
			{
				controlStatus(world);
			}
			else if ((npc.getId() == SCARLET1) && (world.status == 4) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.20)))
			{
				controlStatus(world);
			}
			if (skill != null)
			{
				if (Util.contains(PORTRAITS, npc.getId()) && (skill.getId() == DEWDROP_OF_DESTRUCTION_SKILL_ID))
				{
					npc.doDie(attacker);
				}
				else if ((npc.getId() == FRINTEZZA) && (skill.getId() == SOUL_BREAKING_ARROW_SKILL_ID))
				{
					npc.setScriptValue(1);
					npc.setTarget(null);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (skill.isSuicideAttack())
		{
			return onKill(npc, null, false);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FETWorld)
		{
			FETWorld world = (FETWorld) tmpworld;
			if (npc.getId() == HALL_ALARM)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(world, 0), 2000);
				if (debug)
				{
					_log.info("[Final Emperial Tomb] Hall alarm is disabled, doors will open!");
				}
			}
			else if (npc.getId() == DARK_CHOIR_PLAYER)
			{
				world.darkChoirPlayerCount--;
				if (world.darkChoirPlayerCount < 1)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(world, 2), 2000);
					if (debug)
					{
						_log.info("[Final Emperial Tomb] All Dark Choir Players are killed, doors will open!");
					}
				}
			}
			else if (npc.getId() == SCARLET2)
			{
				controlStatus(world);
			}
			else if (world.status <= 2)
			{
				if (npc.getId() == HALL_KEEPER_CAPTAIN)
				{
					if (getRandom(100) < 5)
					{
						((L2MonsterInstance) npc).dropItem(player, DEWDROP_OF_DESTRUCTION_ITEM_ID, 1);
					}
				}
				
				if (checkKillProgress(npc, world))
				{
					controlStatus(world);
				}
			}
			else if (world.demons.contains(npc))
			{
				world.demons.remove(npc);
			}
			else if (world.portraits.containsKey(npc))
			{
				world.portraits.remove(npc);
			}
		}
		return "";
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
		if (npcId == GUIDE)
		{
			enterInstance(player, "FinalEmperialTomb.xml", ENTER_TELEPORT);
		}
		else if (npc.getId() == CUBE)
		{
			int x = -87534 + getRandom(500);
			int y = -153048 + getRandom(500);
			player.teleToLocation(x, y, -9165);
			return null;
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new FinalEmperialTomb(-1, FinalEmperialTomb.class.getSimpleName(), "instances");
	}
}