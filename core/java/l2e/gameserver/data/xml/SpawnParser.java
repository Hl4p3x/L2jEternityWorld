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
package l2e.gameserver.data.xml;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastSet;

import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.instancemanager.DayNightSpawnManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * Create by LordWinter 17.12.2012 Based on L2J Eternity-World
 */
public class SpawnParser extends DocumentParser
{
	private static Logger _log = Logger.getLogger(SpawnParser.class.getName());
	
	private final FastSet<L2Spawn> _spawntable = new FastSet<>();
	protected int _npcSpawnCount;
	
	protected SpawnParser()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			load();
		}
	}
	
	public FastSet<L2Spawn> getSpawnData()
	{
		return _spawntable;
	}
	
	@Override
	public final void load()
	{
		_spawntable.shared();
		parseDirectory("data/spawns");
		_log.info(getClass().getSimpleName() + ": Loaded " + _spawntable.size() + " npc spawns from .xml files.");
	}
	
	@Override
	protected void parseDocument()
	{
		@SuppressWarnings("unused")
		String name = null;
		
		for (Node c = getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("list".equalsIgnoreCase(c.getNodeName()))
			{
				for (Node n = c.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("spawnloc".equalsIgnoreCase(n.getNodeName()))
					{
						name = n.getAttributes().getNamedItem("name").getNodeValue();
						
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							int npcId = 0, x = 0, y = 0, z = 0, respawn = 0, heading = 0, delay = -1, period = 0;
							
							if ("spawn".equalsIgnoreCase(d.getNodeName()))
							{
								npcId = Integer.parseInt(d.getAttributes().getNamedItem("npcId").getNodeValue());
								x = Integer.parseInt(d.getAttributes().getNamedItem("locX").getNodeValue());
								y = Integer.parseInt(d.getAttributes().getNamedItem("locY").getNodeValue());
								z = Integer.parseInt(d.getAttributes().getNamedItem("locZ").getNodeValue());
								heading = Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue());
								respawn = Integer.parseInt(d.getAttributes().getNamedItem("respawnDelay").getNodeValue());
								if (d.getAttributes().getNamedItem("onKillDelay") != null)
								{
									delay = Integer.parseInt(d.getAttributes().getNamedItem("onKillDelay").getNodeValue());
								}
								period = Integer.parseInt(d.getAttributes().getNamedItem("periodOfDay").getNodeValue());
								
								L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
								if (npcTemplate != null)
								{
									if (npcTemplate.isType("L2SiegeGuard"))
									{
									}
									else if (npcTemplate.isType("L2RaidBoss"))
									{
									}
									else if (!Config.ALLOW_CLASS_MASTERS && npcTemplate.isType("L2ClassMaster"))
									{
									}
									else
									{
										try
										{
											L2Spawn spawnDat = new L2Spawn(npcTemplate);
											spawnDat.setX(x);
											spawnDat.setY(y);
											spawnDat.setZ(z);
											spawnDat.setAmount(1);
											spawnDat.setHeading(heading);
											spawnDat.setRespawnDelay(respawn);
											if (respawn == 0)
											{
												spawnDat.stopRespawn();
											}
											else
											{
												spawnDat.startRespawn();
											}
											L2Npc spawned = spawnDat.doSpawn();
											if ((delay >= 0) && (spawned instanceof L2Attackable))
											{
												((L2Attackable) spawned).setOnKillDelay(delay);
											}
											
											switch (period)
											{
												case 0:
													_npcSpawnCount += spawnDat.init();
													break;
												case 1:
													DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
													_npcSpawnCount++;
													break;
												case 2:
													DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
													_npcSpawnCount++;
													break;
											}
											_spawntable.add(spawnDat);
										}
										catch (Exception e)
										{
											_log.log(Level.WARNING, getClass().getSimpleName() + ": Spawn could not be initialized: " + e.getMessage(), e);
										}
									}
								}
								else
								{
									_log.warning(getClass().getSimpleName() + ": Data missing in NPC table for ID: " + npcId + " in spawnlist");
								}
							}
						}
					}
					else if ("spawnzone".equalsIgnoreCase(n.getNodeName()))
					{
						name = n.getAttributes().getNamedItem("name").getNodeValue();
						
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							int npcId = 0, x = 0, y = 0, z = 0, respawn = 0, heading = 0, delay = -1, period = 0;
							
							if ("spawn".equalsIgnoreCase(d.getNodeName()))
							{
								npcId = Integer.parseInt(d.getAttributes().getNamedItem("npcId").getNodeValue());
								x = Integer.parseInt(d.getAttributes().getNamedItem("locX").getNodeValue());
								y = Integer.parseInt(d.getAttributes().getNamedItem("locY").getNodeValue());
								z = Integer.parseInt(d.getAttributes().getNamedItem("locZ").getNodeValue());
								heading = Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue());
								respawn = Integer.parseInt(d.getAttributes().getNamedItem("respawnDelay").getNodeValue());
								if (d.getAttributes().getNamedItem("onKillDelay") != null)
								{
									delay = Integer.parseInt(d.getAttributes().getNamedItem("onKillDelay").getNodeValue());
								}
								period = Integer.parseInt(d.getAttributes().getNamedItem("periodOfDay").getNodeValue());
								
								L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
								if (npcTemplate != null)
								{
									if (npcTemplate.isType("L2SiegeGuard"))
									{
									}
									else if (npcTemplate.isType("L2RaidBoss"))
									{
									}
									else if (!Config.ALLOW_CLASS_MASTERS && npcTemplate.isType("L2ClassMaster"))
									{
									}
									else
									{
										try
										{
											L2Spawn spawnDat = new L2Spawn(npcTemplate);
											
											spawnDat.setX(x);
											spawnDat.setY(y);
											spawnDat.setZ(z);
											spawnDat.setAmount(1);
											spawnDat.setRandom(true);
											spawnDat.setHeading(heading);
											spawnDat.setRespawnDelay(respawn);
											if (respawn == 0)
											{
												spawnDat.stopRespawn();
											}
											else
											{
												spawnDat.startRespawn();
											}
											L2Npc spawned = spawnDat.doSpawn();
											if ((delay >= 0) && (spawned instanceof L2Attackable))
											{
												((L2Attackable) spawned).setOnKillDelay(delay);
											}
											
											switch (period)
											{
												case 0:
													_npcSpawnCount += spawnDat.init();
													break;
												case 1:
													DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
													_npcSpawnCount++;
													break;
												case 2:
													DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
													_npcSpawnCount++;
													break;
											}
											_spawntable.add(spawnDat);
										}
										catch (Exception e)
										{
											_log.log(Level.WARNING, getClass().getSimpleName() + ": Spawn could not be initialized: " + e.getMessage(), e);
										}
									}
								}
								else
								{
									_log.warning(getClass().getSimpleName() + ": Data missing in NPC table for ID: " + npcId + " in spawnlist");
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void reloadAll()
	{
		load();
	}
	
	public Collection<L2Spawn> getAllSpawns()
	{
		return _spawntable;
	}
	
	public static SpawnParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnParser _instance = new SpawnParser();
	}
}