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
package l2e.scripts.ai.zone.LairOfAntharas;

import java.io.File;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Created by LordWinter 24.09.2013 Based on L2J Eternity-World
 */
public class DrakeLeaders extends L2AttackableAIScript
{
	private static final int DRAKE_LEADER = 22848;
	
	protected static final FastMap<Integer, DrakeLeaderGroup> _leadersGroups = new FastMap<>();
	
	protected class DrakeLeaderGroup
	{
		protected final int _id;
		protected L2Npc _leader;
		protected int _currentRoute = 0;
		protected boolean _attackDirection = false;
		protected TreeMap<Integer, Location> _pathRoutes;
		
		protected DrakeLeaderGroup(int id)
		{
			_id = id;
		}
	}
	
	public DrakeLeaders(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addAggroRangeEnterId(DRAKE_LEADER);
		addAttackId(DRAKE_LEADER);
		load();
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc.getId() == DRAKE_LEADER)
		{
			DrakeLeaderGroup group = getGroup(npc);
			
			if ((!group._leader.isCastingNow()) && (!group._leader.isAttackingNow()) && (!group._leader.isInCombat()) && (!player.isDead()))
			{
				group._attackDirection = true;
				((L2Attackable) group._leader).addDamageHate(player, 0, 999);
				group._leader.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return null;
	}
	
	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == DRAKE_LEADER)
		{
			DrakeLeaderGroup group = getGroup(npc);
			group._attackDirection = true;
		}
		return null;
	}
	
	protected class RunTask implements Runnable
	{
		@Override
		public void run()
		{
			for (int groupId : _leadersGroups.keySet())
			{
				DrakeLeaderGroup group = _leadersGroups.get(groupId);
				if (group._leader.isInCombat() || group._leader.isCastingNow() || group._leader.isAttackingNow() || group._leader.isDead() || (group._leader.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO))
				{
					continue;
				}
				
				group._currentRoute = getNextRoute(group, group._currentRoute);
				Location loc = group._pathRoutes.get(group._currentRoute);
				int nextPathRoute;
				if (group._attackDirection)
				{
					nextPathRoute = getNextRoute(group, group._currentRoute - 1);
				}
				else
				{
					nextPathRoute = getNextRoute(group, group._currentRoute);
				}
				loc.setHeading(calculateHeading(loc, group._pathRoutes.get(nextPathRoute)));
				if (!group._leader.isRunning())
				{
					group._leader.setIsRunning(true);
				}
				MoveToLocation mov = new MoveToLocation(group._leader);
				group._leader.broadcastPacket(mov);
				group._leader.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading()));
			}
		}
	}
	
	protected int getNextRoute(DrakeLeaderGroup group, int currentRoute)
	{
		if (group._pathRoutes.lastKey().intValue() == currentRoute)
		{
			group._currentRoute = 0;
			return group._pathRoutes.firstKey();
		}
		return group._pathRoutes.higherKey(currentRoute);
	}
	
	protected DrakeLeaderGroup getGroup(L2Npc npc)
	{
		if ((npc == null) || (npc.getId() != DRAKE_LEADER))
		{
			return null;
		}
		
		for (DrakeLeaderGroup group : _leadersGroups.values())
		{
			if ((npc.getId() == DRAKE_LEADER) && npc.equals(group._leader))
			{
				return group;
			}
		}
		return null;
	}
	
	protected int calculateHeading(Location fromLoc, Location toLoc)
	{
		return Util.calculateHeadingFrom(fromLoc.getX(), fromLoc.getY(), toLoc.getX(), toLoc.getY());
	}
	
	protected void loadSpawns()
	{
		for (Iterator<Integer> id = _leadersGroups.keySet().iterator(); id.hasNext();)
		{
			final int groupId = id.next();
			DrakeLeaderGroup group = _leadersGroups.get(groupId);
			Location spawn = group._pathRoutes.firstEntry().getValue();
			group._leader = addSpawn(DRAKE_LEADER, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0);
			group._leader.getSpawn().setAmount(1);
			group._leader.getSpawn().startRespawn();
			group._leader.getSpawn().setRespawnDelay(300);
			group._leader.setIsRunner(true);
			group._leader.getKnownList().startTrackingTask();
			group._leader.setRunning();
		}
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RunTask(), 30000, 1000);
	}
	
	protected void load()
	{
		File f = new File(Config.DATAPACK_ROOT, "data/spawnZones/drake_leaders.xml");
		if (!f.exists())
		{
			_log.severe("[Drake Leaders AI]: Error! drake_leaders.xml file is missing!");
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
				if ("drakeLeader".equalsIgnoreCase(n.getNodeName()))
				{
					final int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
					DrakeLeaderGroup group = new DrakeLeaderGroup(id);
					group._pathRoutes = new TreeMap<>();
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("pathRoute".equalsIgnoreCase(d.getNodeName()))
						{
							final int order = Integer.parseInt(d.getAttributes().getNamedItem("position").getNodeValue());
							final int x = Integer.parseInt(d.getAttributes().getNamedItem("locX").getNodeValue());
							final int y = Integer.parseInt(d.getAttributes().getNamedItem("locY").getNodeValue());
							final int z = Integer.parseInt(d.getAttributes().getNamedItem("locZ").getNodeValue());
							Location loc = new Location(x, y, z, 0);
							group._pathRoutes.put(order, loc);
						}
					}
					_leadersGroups.put(id, group);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "[Drake Leaders AI]: Error while loading drake_leaders.xml file: " + e.getMessage(), e);
		}
		loadSpawns();
	}
	
	public static void main(String[] args)
	{
		new DrakeLeaders(-1, DrakeLeaders.class.getSimpleName(), "ai");
	}
}