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
package l2e.scripts.ai.npc.group_template;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.SpawnParser;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Create by LordWinter 17.12.2012 Based on L2J Eternity-World
 */
public class SelMahumChefs extends L2AttackableAIScript
{
	private static final int SELMAHUM_CHEF = 18908;
	private static final int SELMAHUM_ESCORT_GUARD = 22779;

	protected static final int[] BLOCK_NPC =
	{
		        22779
	};

	private static final int[] SELMAHUM_SQUAD_LEADERS =
	{
	                22786,
	                22787,
	                22788
	};

	protected static final NpcStringId[] CHEF_FSTRINGS =
	{
	                NpcStringId.I_BROUGHT_THE_FOOD,
	                NpcStringId.COME_AND_EAT
	};

	private static final int CAMP_FIRE = 18927;
	private static final int FIRE_FEED = 18933;

	private static final int SKILL_TIRED = 6331;
	private static final int SKILL_FULL = 6332;

	protected static final FastMap<Integer, ChefGroup> chefGroups = new FastMap<>();
	protected static final TIntObjectHashMap<Location[]> escortSpawns = new TIntObjectHashMap<>();
	protected static final ConcurrentHashMap<L2Npc, Integer> fireplaces = new ConcurrentHashMap<>();
	protected static final ConcurrentHashMap<L2Npc, L2Npc> fireplacesFeed = new ConcurrentHashMap<>();

	private class ChefGroup
	{
		public final int id;
		public L2Npc chef;
		public L2Npc[] escorts;
		public int currentPoint = 0;
		public boolean atFirePlace = false;
		public int lastFirePlaceId = 0;
		public AtomicLong lastInvincible = new AtomicLong();
		public boolean reverseDirection = false;
		public TreeMap<Integer, Location> pathPoints;

		public ChefGroup(int id)
		{
			this.id = id;
			lastInvincible.set(0);
		}
	}

	public SelMahumChefs(int questId, String name, String descr)
	{
		super(questId, name, descr);

		int[] mobs = new int[]
		{
		                SELMAHUM_CHEF,
		                SELMAHUM_ESCORT_GUARD
		};
		registerMobs(mobs, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL);
		addSpawnId(SELMAHUM_CHEF);
		addFirstTalkId(CAMP_FIRE);
		addFirstTalkId(FIRE_FEED);

		for (int i : SELMAHUM_SQUAD_LEADERS)
		{
			addAttackId(i);
		}

		load();
	}

	protected class WalkTask implements Runnable
	{
		@Override
		public void run()
		{
			for (int groupId : chefGroups.keySet())
			{
				ChefGroup group = chefGroups.get(groupId);
				if (group.chef.isInCombat() || group.chef.isDead() || group.chef.isMoving() || group.atFirePlace)
				{
					if (group.chef.isMoving())
					{
						MoveToLocation mov = new MoveToLocation(group.chef);
						group.chef.broadcastPacket(mov);
					}
					continue;
				}

				if (doFireplace(group))
				{
					continue;
				}
				group.currentPoint = getNextPoint(group, group.currentPoint);
				Location loc = group.pathPoints.get(group.currentPoint);
				int nextPathPoint = getNextPoint(group, group.currentPoint);
				loc.setHeading(calculateHeading(loc, group.pathPoints.get(nextPathPoint)));
				group.chef.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading()));
			}
		}
	}

	protected class RangeCheckTask implements Runnable
	{
		@Override
		public void run()
		{
			for (int groupId : chefGroups.keySet())
			{
				ChefGroup group = chefGroups.get(groupId);
				if (group.chef.isInCombat() || group.chef.isDead())
				{
					continue;
				}
				for (L2Npc escort : group.escorts)
				{
					if ((escort == null) || escort.isDead())
					{
						continue;
					}
					if (Util.checkIfInRange(150, escort, group.chef, false))
					{
						escort.setWalking();
					}
					else
					{
						escort.setRunning();
					}
					if (!escort.getAI().getIntention().equals(CtrlIntention.AI_INTENTION_FOLLOW))
					{
						escort.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, group.chef);
					}
					MoveToLocation mov1 = new MoveToLocation(escort);
					escort.broadcastPacket(mov1);
				}
			}
		}
	}

	private class FireplaceTask implements Runnable
	{
		private final ChefGroup group;
		private L2Npc fireplace;

		protected FireplaceTask(ChefGroup group, L2Npc fireplace)
		{
			this.group = group;
			this.fireplace = fireplace;
		}

		@Override
		public void run()
		{
			if ((fireplace.getDisplayEffect() == 0) && fireplacesFeed.containsKey(fireplace))
			{
				fireplacesFeed.get(fireplace).deleteMe();
				fireplacesFeed.remove(fireplace);
			}
			else if (fireplace.getDisplayEffect() == 0)
			{
				broadcastAnimation(fireplace);
				fireplace.setDisplayEffect(1);

				for (L2MonsterInstance leader : fireplace.getKnownList().getKnownMonstersInRadius(1000))
				{
					if (leader != null)
					{
						if (!Util.contains(BLOCK_NPC, leader.getId()))
						{
							if (!leader.isAttackingNow() && !leader.isDead() && (leader.getFirstEffect(SKILL_TIRED) == null))
							{
								int rndX = getRandom(100) < 50 ? -getRandom(50, 100) : getRandom(50, 100);
								int rndY = getRandom(100) < 50 ? -getRandom(50, 100) : getRandom(50, 100);
								Location fireplaceLoc = new Location(fireplace.getX(), fireplace.getY(), fireplace.getZ());
								Location leaderLoc = new Location(fireplace.getX() + rndX, fireplace.getY() + rndY, fireplace.getZ());
								Location position = new Location(fireplace.getX() + rndX, fireplace.getY() + rndY, fireplace.getZ(), calculateHeading(leaderLoc, fireplaceLoc));
								leader.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, position);
								ThreadPoolManager.getInstance().scheduleAi(new MoveToFireplace(leader, 0), 1000);
							}
						}
					}
				}
			}
			else if ((fireplace.getDisplayEffect() == 1) && !fireplacesFeed.containsKey(fireplace))
			{
				L2Npc feed = addSpawn(FIRE_FEED, fireplace.getX(), fireplace.getY(), fireplace.getZ(), 0, false, 0, false);
				feed.isShowName();
				fireplacesFeed.put(fireplace, feed);
				group.chef.broadcastPacket(new CreatureSay(group.chef.getObjectId(), Say2.ALL, group.chef.getName(), CHEF_FSTRINGS[getRandom(2)]));
				for (L2MonsterInstance leader : fireplace.getKnownList().getKnownMonstersInRadius(1500))
				{
					if (leader != null)
					{
						if (!Util.contains(BLOCK_NPC, leader.getId()))
						{
							if (!leader.isAttackingNow() && !leader.isDead() && (leader.getFirstEffect(SKILL_FULL) == null))
							{
								int rndX = getRandom(100) < 50 ? -getRandom(50, 100) : getRandom(50, 100);
								int rndY = getRandom(100) < 50 ? -getRandom(50, 100) : getRandom(50, 100);
								Location fireplaceLoc = new Location(fireplace.getX(), fireplace.getY(), fireplace.getZ());
								Location leaderLoc = new Location(fireplace.getX() + rndX, fireplace.getY() + rndY, fireplace.getZ());
								Location position = new Location(fireplace.getX() + rndX, fireplace.getY() + rndY, fireplace.getZ(), calculateHeading(leaderLoc, fireplaceLoc));
								leader.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, position);
								ThreadPoolManager.getInstance().scheduleAi(new MoveToFireplace(leader, 1), 100);
							}
						}
					}
				}
			}
			else if ((fireplace.getDisplayEffect() == 1) && fireplacesFeed.containsKey(fireplace))
			{
				L2Npc feed = fireplacesFeed.get(fireplace);
				fireplacesFeed.remove(fireplace);
				fireplaces.remove(fireplace);
				L2Npc fire = addSpawn(CAMP_FIRE, fireplace);
				fire.isShowName();
				broadcastAnimation(fire);
				fire.setDisplayEffect(0);
				fireplace.deleteMe();
				fireplaces.put(fire, 1);
				fireplacesFeed.put(fire, feed);
				fireplace = fire;
				broadcastAnimation(fireplace);
				fireplace.setDisplayEffect(0);
			}
			group.lastFirePlaceId = fireplace.getObjectId();
			ThreadPoolManager.getInstance().scheduleAi(new MoveChefFromFireplace(group, fireplace), 10000);
		}
	}

	private class MoveChefFromFireplace implements Runnable
	{
		private final ChefGroup group;
		private final L2Npc fire;

		protected MoveChefFromFireplace(ChefGroup group, L2Npc fire)
		{
			this.group = group;
			this.fire = fire;
		}

		@Override
		public void run()
		{
			group.atFirePlace = false;
			fireplaces.replace(fire, 0);
		}
	}

	private class MoveToFireplace implements Runnable
	{
		private final L2MonsterInstance mob;
		private final int type;

		protected MoveToFireplace(L2MonsterInstance mob, int type)
		{
			this.mob = mob;
			this.type = type;
		}

		@Override
		public void run()
		{
			if (mob.isMoving())
			{
				ThreadPoolManager.getInstance().scheduleAi(new MoveToFireplace(mob, type), 1000);
			}
			else if (!mob.isInCombat() && !mob.isDead())
			{
				if (type == 0)
				{
					broadcastAnimation(mob);
					SkillHolder.getInstance().getInfo(SKILL_TIRED, 1).getEffects(mob, mob);
					mob.setDisplayEffect(2);
				}
				else if (type == 1)
				{
					broadcastAnimation(mob);
					SkillHolder.getInstance().getInfo(SKILL_FULL, 1).getEffects(mob, mob);
					mob.setDisplayEffect(1);
				}
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
				mob.setIsNoRndWalk(true);
				ThreadPoolManager.getInstance().scheduleAi(new ReturnFromFireplace(mob), 300000);
			}
		}
	}

	private class ReturnFromFireplace implements Runnable
	{
		private final L2MonsterInstance mob;

		protected ReturnFromFireplace(L2MonsterInstance mob)
		{
			this.mob = mob;
		}

		@Override
		public void run()
		{
			if ((mob != null) && !mob.isInCombat() && !mob.isDead())
			{
				if ((mob.getFirstEffect(SKILL_FULL) == null) && (mob.getFirstEffect(SKILL_TIRED) == null))
				{
					broadcastAnimation(mob);
					mob.setIsNoRndWalk(false);
					mob.setDisplayEffect(3);
					mob.returnToSpawn();
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleAi(new ReturnFromFireplace(mob), 30000);
				}
			}
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return super.onFirstTalk(npc, player);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (npc.getId() == SELMAHUM_CHEF)
		{
			ChefGroup group = getChefGroup(npc);
			if (group == null)
			{
				return null;
			}
			Location[] spawns = escortSpawns.get(group.id);
			for (int i = 0; i < 2; i++)
			{
				group.escorts[i] = addSpawn(SELMAHUM_ESCORT_GUARD, spawns[i].getX(), spawns[i].getY(), spawns[i].getZ(), spawns[i].getHeading(), false, 0);
				group.escorts[i].getSpawn().stopRespawn();
				group.escorts[i].setIsNoRndWalk(true);
				group.escorts[i].setWalking();
				group.escorts[i].getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, group.chef);
			}
		}
		return super.onSpawn(npc);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return "";
	}

	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == SELMAHUM_CHEF)
		{
			ChefGroup group = getChefGroup(npc);
			if ((group.lastInvincible.get() < System.currentTimeMillis()) && (((npc.getCurrentHp() / npc.getMaxHp()) * 100) < 50))
			{
				group.lastInvincible.set(System.currentTimeMillis() + 600000);
				SkillHolder.getInstance().getInfo(5989, 1).getEffects(npc, npc);
			}
			else if (npc.getFirstEffect(5989) != null)
			{
				if ((group.chef.getTarget() != null) && group.chef.getTarget().equals(attacker) && (((attacker.getCurrentHp() / attacker.getMaxHp()) * 100) < 90))
				{
					if (!npc.isCastingNow())
					{
						npc.doCast(SkillHolder.getInstance().getInfo(6330, 1));
					}
				}
			}

			for (L2Npc escort : group.escorts)
			{
				if (!escort.isInCombat())
				{
					escort.setRunning();
					((L2Attackable) escort).addDamageHate(attacker, 0, 500);
					escort.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
				}
			}
		}
		else if (npc.getId() == SELMAHUM_ESCORT_GUARD)
		{
			ChefGroup group = getChefGroup(npc);
			if ((group != null) && !group.chef.isDead() && !group.chef.isInCombat())
			{
				group.chef.setRunning();
				((L2Attackable) group.chef).addDamageHate(attacker, 0, 500);
				group.chef.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}

			if ((group != null) && (group.escorts != null))
			{
				for (L2Npc escort : group.escorts)
				{
					if (!escort.isInCombat())
					{
						escort.setRunning();
						((L2Attackable) escort).addDamageHate(attacker, 0, 500);
						escort.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
					}
				}
			}
		}
		else if (!Util.contains(SELMAHUM_SQUAD_LEADERS, npc.getId()))
		{
			broadcastAnimation(npc);
			npc.setDisplayEffect(0);
			npc.setIsNoRndWalk(false);
		}
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == SELMAHUM_CHEF)
		{
			ChefGroup group = getChefGroup(npc);
			for (L2Npc escort : group.escorts)
			{
				if ((escort != null) && !npc.isDead())
				{
					escort.deleteMe();
				}
			}
		}
		return null;
	}

	protected boolean doFireplace(ChefGroup group)
	{
		if (!group.atFirePlace)
		{
			for (L2Npc fire : fireplaces.keySet())
			{
				if ((Util.calculateDistance(group.chef, fire, true) < 400) && (fire.getObjectId() != group.lastFirePlaceId) && (fireplaces.get(fire) == 0))
				{
					group.atFirePlace = true;
					int xDiff = (group.chef.getX() - fire.getX()) > 0 ? -getRandom(30, 40) : getRandom(30, 40);
					int yDiff = (group.chef.getY() - fire.getY()) > 0 ? -getRandom(30, 40) : getRandom(30, 40);
					group.chef.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(fire.getX() - xDiff, fire.getY() - yDiff, fire.getZ(), calculateHeading(group.chef, fire)));
					fireplaces.replace(fire, 1);
					ThreadPoolManager.getInstance().scheduleAi(new FireplaceTask(group, fire), 1000);
					break;
				}
			}
			if (group.atFirePlace)
			{
				return true;
			}
		}
		return false;
	}

	private ChefGroup getChefGroup(L2Npc npc)
	{
		if ((npc == null) || ((npc.getId() != SELMAHUM_CHEF) && (npc.getId() != SELMAHUM_ESCORT_GUARD)))
		{
			return null;
		}
		for (ChefGroup group : chefGroups.values())
		{
			if ((npc.getId() == SELMAHUM_CHEF) && npc.equals(group.chef))
			{
				return group;
			}
			if (npc.getId() == SELMAHUM_ESCORT_GUARD)
			{
				for (L2Npc escort : group.escorts)
				{
					if (npc.equals(escort))
					{
						return group;
					}
				}
			}
		}
		return null;
	}

	protected int getNextPoint(ChefGroup group, int currentPoint)
	{
		if (group.pathPoints.lastKey().intValue() == currentPoint)
		{
			group.reverseDirection = true;
		}
		else if (group.pathPoints.firstKey().intValue() == currentPoint)
		{
			group.reverseDirection = false;
		}

		if (group.reverseDirection)
		{
			return group.pathPoints.lowerKey(currentPoint);
		}
		return group.pathPoints.higherKey(currentPoint);
	}

	protected int calculateHeading(Location fromLoc, Location toLoc)
	{
		return Util.calculateHeadingFrom(fromLoc.getX(), fromLoc.getY(), toLoc.getX(), toLoc.getY());
	}

	private int calculateHeading(L2Character fromLoc, L2Character toLoc)
	{
		return Util.calculateHeadingFrom(fromLoc.getX(), fromLoc.getY(), toLoc.getX(), toLoc.getY());
	}

	private void loadFireplaces()
	{
		for (L2Spawn spawn : SpawnParser.getInstance().getSpawnData())
		{
			if (spawn != null)
			{
				if (spawn.getId() == CAMP_FIRE)
				{
					broadcastAnimation(spawn.getLastSpawn());
					spawn.getLastSpawn().setDisplayEffect(0);
					spawn.getLastSpawn().setIsRunner(true);
					fireplaces.put(spawn.getLastSpawn(), 0);
					spawn.getLastSpawn().isShowName();
				}
			}
		}
	}

	private void loadSpawns()
	{
		for (Integer integer : chefGroups.keySet())
		{
			final int groupId = integer;
			ChefGroup group = chefGroups.get(groupId);
			Location spawn = group.pathPoints.firstEntry().getValue();
			group.chef = addSpawn(SELMAHUM_CHEF, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0);
			group.chef.getSpawn().setAmount(1);
			group.chef.getSpawn().startRespawn();
			group.chef.getSpawn().setRespawnDelay(60);
			group.chef.setIsRunner(true);
			group.chef.setWalking();
			group.escorts = new L2Npc[2];
			Location[] spawns = escortSpawns.get(groupId);
			for (int i = 0; i < 2; i++)
			{
				group.escorts[i] = addSpawn(SELMAHUM_ESCORT_GUARD, spawns[i].getX(), spawns[i].getY(), spawns[i].getZ(), spawns[i].getHeading(), false, 0);
				group.escorts[i].getSpawn().stopRespawn();
				group.escorts[i].setIsNoRndWalk(true);
				group.escorts[i].setIsRunner(true);
				group.escorts[i].setWalking();
				group.escorts[i].getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, group.chef);
			}
		}
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new WalkTask(), 1000, 2500);
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RangeCheckTask(), 1000, 1000);
	}

	private void calculateEscortSpawns()
	{
		for (Integer integer : chefGroups.keySet())
		{
			final int groupId = integer;
			ChefGroup group = chefGroups.get(groupId);
			Location loc = group.pathPoints.firstEntry().getValue();
			double chefAngle = Util.convertHeadingToDegree(loc.getHeading());
			chefAngle = chefAngle + 180;
			if (chefAngle > 359)
			{
				chefAngle -= 360;
			}
			final int xDirection = ((chefAngle <= 90) || (chefAngle >= 270)) ? 1 : -1;
			final int yDirection = ((chefAngle >= 180) && (chefAngle < 360)) ? -1 : 1;
			Location[] spawnLocs = new Location[2];
			spawnLocs[0] = new Location();
			spawnLocs[0].setX(loc.getX() + (xDirection * ((int) Math.sin(45) * 100)));
			spawnLocs[0].setY(loc.getY() + (yDirection * ((int) Math.cos(45) * 100)));
			spawnLocs[0].setZ(loc.getZ());
			spawnLocs[0].setHeading(loc.getHeading());
			spawnLocs[1] = new Location();
			spawnLocs[1].setX(loc.getX() - (xDirection * ((int) Math.sin(45) * 100)));
			spawnLocs[1].setY(loc.getY() + (yDirection * ((int) Math.cos(45) * 100)));
			spawnLocs[1].setZ(loc.getZ());
			spawnLocs[1].setHeading(loc.getHeading());
			escortSpawns.put(groupId, spawnLocs);
		}
	}

	private void load()
	{
		File f = new File(Config.DATAPACK_ROOT, "data/spawnZones/selmahum_chefs.xml");
		if (!f.exists())
		{
			_log.severe("[Sel Mahum Chefs]: Error! selmahum_chefs.xml file is missing!");
			return;
		}

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setValidating(true);
			Document doc = factory.newDocumentBuilder().parse(f);

			for (Node n = doc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("chef".equalsIgnoreCase(n.getNodeName()))
				{
					final int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
					ChefGroup group = new ChefGroup(id);
					group.pathPoints = new TreeMap<>();
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("pathPoint".equalsIgnoreCase(d.getNodeName()))
						{
							final int order = Integer.parseInt(d.getAttributes().getNamedItem("order").getNodeValue());
							final int x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
							final int y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
							final int z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
							Location loc = new Location(x, y, z, 0);
							group.pathPoints.put(order, loc);
						}
					}
					chefGroups.put(id, group);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "[Sel Mahum Chefs]: Error while loading selmahum_chefs.xml file: " + e.getMessage(), e);
		}
		calculateEscortSpawns();
		loadFireplaces();
		loadSpawns();
	}

	private void broadcastAnimation(L2Npc npc)
	{
		for (L2PcInstance player : npc.getKnownList().getKnownPlayersInRadius(10000))
		{
			player.sendPacket(new NpcInfo(npc, player));
		}
	}

	public static void main(String[] args)
	{
		new SelMahumChefs(-1, SelMahumChefs.class.getSimpleName(), "ai");
	}
}
