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
package l2e.gameserver.instancemanager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2MapRegion;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.zone.type.L2ClanHallZone;
import l2e.gameserver.model.zone.type.L2RespawnZone;

public final class MapRegionManager extends DocumentParser
{
	private static final Map<String, L2MapRegion> _regions = new HashMap<>();
	private static final String defaultRespawn = "talking_island_town";
	
	protected MapRegionManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_regions.clear();
		parseDirectory(new File(Config.DATAPACK_ROOT, "data/mapregion/"));
		_log.info(getClass().getSimpleName() + ": Loaded " + _regions.size() + " map regions.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		String name;
		String town;
		int locId;
		int castle;
		int bbs;
		
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("region".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						name = attrs.getNamedItem("name").getNodeValue();
						town = attrs.getNamedItem("town").getNodeValue();
						locId = parseInt(attrs, "locId");
						castle = parseInt(attrs, "castle");
						bbs = parseInt(attrs, "bbs");
						
						L2MapRegion region = new L2MapRegion(name, town, locId, castle, bbs);
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							attrs = c.getAttributes();
							if ("respawnPoint".equalsIgnoreCase(c.getNodeName()))
							{
								int spawnX = parseInt(attrs, "X");
								int spawnY = parseInt(attrs, "Y");
								int spawnZ = parseInt(attrs, "Z");
								
								boolean other = parseBoolean(attrs, "isOther");
								boolean chaotic = parseBoolean(attrs, "isChaotic");
								boolean banish = parseBoolean(attrs, "isBanish");
								
								if (other)
								{
									region.addOtherSpawn(spawnX, spawnY, spawnZ);
								}
								else if (chaotic)
								{
									region.addChaoticSpawn(spawnX, spawnY, spawnZ);
								}
								else if (banish)
								{
									region.addBanishSpawn(spawnX, spawnY, spawnZ);
								}
								else
								{
									region.addSpawn(spawnX, spawnY, spawnZ);
								}
							}
							else if ("map".equalsIgnoreCase(c.getNodeName()))
							{
								region.addMap(parseInt(attrs, "X"), parseInt(attrs, "Y"));
							}
							else if ("banned".equalsIgnoreCase(c.getNodeName()))
							{
								region.addBannedRace(attrs.getNamedItem("race").getNodeValue(), attrs.getNamedItem("point").getNodeValue());
							}
						}
						_regions.put(name, region);
					}
				}
			}
		}
	}
	
	public final L2MapRegion getMapRegion(int locX, int locY)
	{
		for (L2MapRegion region : _regions.values())
		{
			if (region.isZoneInRegion(getMapRegionX(locX), getMapRegionY(locY)))
			{
				return region;
			}
		}
		return null;
	}
	
	public final int getMapRegionLocId(int locX, int locY)
	{
		L2MapRegion region = getMapRegion(locX, locY);
		if (region != null)
		{
			return region.getLocId();
		}
		return 0;
	}
	
	public final L2MapRegion getMapRegion(L2Object obj)
	{
		return getMapRegion(obj.getX(), obj.getY());
	}
	
	public final int getMapRegionLocId(L2Object obj)
	{
		return getMapRegionLocId(obj.getX(), obj.getY());
	}
	
	public final int getMapRegionX(int posX)
	{
		return (posX >> 15) + 9 + 11;
	}
	
	public final int getMapRegionY(int posY)
	{
		return (posY >> 15) + 10 + 8;
	}
	
	public String getClosestTownName(L2Character activeChar)
	{
		L2MapRegion region = getMapRegion(activeChar);
		
		if (region == null)
		{
			return "Aden Castle Town";
		}
		
		return region.getTown();
	}
	
	public int getAreaCastle(L2Character activeChar)
	{
		L2MapRegion region = getMapRegion(activeChar);
		
		if (region == null)
		{
			return 0;
		}
		
		return region.getCastle();
	}
	
	public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
	{
		Location loc;
		
		if (activeChar.isPlayer())
		{
			L2PcInstance player = ((L2PcInstance) activeChar);
			
			Castle castle = null;
			Fort fort = null;
			ClanHall clanhall = null;
			
			if ((player.getClan() != null) && !player.isFlyingMounted() && !player.isFlying())
			{
				if (teleportWhere == TeleportWhereType.CLANHALL)
				{
					clanhall = ClanHallManager.getInstance().getAbstractHallByOwner(player.getClan());
					if (clanhall != null)
					{
						L2ClanHallZone zone = clanhall.getZone();
						if ((zone != null) && !player.isFlyingMounted())
						{
							if (player.getKarma() > 0)
							{
								return zone.getChaoticSpawnLoc();
							}
							return zone.getSpawnLoc();
						}
					}
				}
				
				if (teleportWhere == TeleportWhereType.CASTLE)
				{
					castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
					
					if (castle == null)
					{
						castle = CastleManager.getInstance().getCastle(player);
						if (!((castle != null) && castle.getSiege().getIsInProgress() && (castle.getSiege().getDefenderClan(player.getClan()) != null)))
						{
							castle = null;
						}
					}
					
					if ((castle != null) && (castle.getId() > 0))
					{
						if (player.getKarma() > 0)
						{
							return castle.getCastleZone().getChaoticSpawnLoc();
						}
						return castle.getCastleZone().getSpawnLoc();
					}
				}
				
				if (teleportWhere == TeleportWhereType.FORTRESS)
				{
					fort = FortManager.getInstance().getFortByOwner(player.getClan());
					
					if (fort == null)
					{
						fort = FortManager.getInstance().getFort(player);
						if (!((fort != null) && fort.getSiege().getIsInProgress() && (fort.getOwnerClan() == player.getClan())))
						{
							fort = null;
						}
					}
					
					if ((fort != null) && (fort.getId() > 0))
					{
						if (player.getKarma() > 0)
						{
							return fort.getFortZone().getChaoticSpawnLoc();
						}
						return fort.getFortZone().getSpawnLoc();
					}
				}
				
				if (teleportWhere == TeleportWhereType.SIEGEFLAG)
				{
					castle = CastleManager.getInstance().getCastle(player);
					fort = FortManager.getInstance().getFort(player);
					clanhall = ClanHallManager.getInstance().getNearbyAbstractHall(activeChar.getX(), activeChar.getY(), 10000);
					L2SiegeFlagInstance tw_flag = TerritoryWarManager.getInstance().getFlagForClan(player.getClan());
					if (tw_flag != null)
					{
						return new Location(tw_flag.getX(), tw_flag.getY(), tw_flag.getZ());
					}
					else if (castle != null)
					{
						if (castle.getSiege().getIsInProgress())
						{
							List<L2Npc> flags = castle.getSiege().getFlag(player.getClan());
							if ((flags != null) && !flags.isEmpty())
							{
								L2Npc flag = flags.get(0);
								return new Location(flag.getX(), flag.getY(), flag.getZ());
							}
						}
						
					}
					else if (fort != null)
					{
						if (fort.getSiege().getIsInProgress())
						{
							List<L2Npc> flags = fort.getSiege().getFlag(player.getClan());
							if ((flags != null) && !flags.isEmpty())
							{
								L2Npc flag = flags.get(0);
								return new Location(flag.getX(), flag.getY(), flag.getZ());
							}
						}
					}
					else if ((clanhall != null) && clanhall.isSiegableHall())
					{
						SiegableHall sHall = (SiegableHall) clanhall;
						List<L2Npc> flags = sHall.getSiege().getFlag(player.getClan());
						if ((flags != null) && !flags.isEmpty())
						{
							L2Npc flag = flags.get(0);
							return new Location(flag.getX(), flag.getY(), flag.getZ());
						}
					}
				}
			}
			
			if (teleportWhere == TeleportWhereType.CASTLE_BANISH)
			{
				castle = CastleManager.getInstance().getCastle(player);
				if (castle != null)
				{
					return castle.getCastleZone().getBanishSpawnLoc();
				}
			}
			else if (teleportWhere == TeleportWhereType.FORTRESS_BANISH)
			{
				fort = FortManager.getInstance().getFort(activeChar);
				if (fort != null)
				{
					return fort.getFortZone().getBanishSpawnLoc();
				}
			}
			else if (teleportWhere == TeleportWhereType.CLANHALL_BANISH)
			{
				clanhall = ClanHallManager.getInstance().getClanHall(activeChar);
				if (clanhall != null)
				{
					return clanhall.getZone().getBanishSpawnLoc();
				}
			}
			
			if (player.getKarma() > 0)
			{
				try
				{
					L2RespawnZone zone = ZoneManager.getInstance().getZone(player, L2RespawnZone.class);
					if (zone != null)
					{
						return getRestartRegion(activeChar, zone.getRespawnPoint((L2PcInstance) activeChar)).getChaoticSpawnLoc();
					}
					return getMapRegion(activeChar).getChaoticSpawnLoc();
				}
				catch (Exception e)
				{
					if (player.isFlyingMounted())
					{
						return _regions.get("union_base_of_kserth").getChaoticSpawnLoc();
					}
					return _regions.get(defaultRespawn).getChaoticSpawnLoc();
				}
			}
			
			castle = CastleManager.getInstance().getCastle(player);
			if (castle != null)
			{
				if (castle.getSiege().getIsInProgress())
				{
					if ((castle.getSiege().checkIsDefender(player.getClan()) || castle.getSiege().checkIsAttacker(player.getClan())) && (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN))
					{
						return castle.getCastleZone().getOtherSpawnLoc();
					}
				}
			}
			
			if (player.getInstanceId() > 0)
			{
				Instance inst = InstanceManager.getInstance().getInstance(player.getInstanceId());
				if (inst != null)
				{
					loc = inst.getSpawnLoc();
					if (loc != null)
					{
						return loc;
					}
				}
			}
		}
		
		try
		{
			L2RespawnZone zone = ZoneManager.getInstance().getZone(activeChar, L2RespawnZone.class);
			if (zone != null)
			{
				return getRestartRegion(activeChar, zone.getRespawnPoint((L2PcInstance) activeChar)).getSpawnLoc();
			}
			return getMapRegion(activeChar).getSpawnLoc();
		}
		catch (Exception e)
		{
			return _regions.get(defaultRespawn).getSpawnLoc();
		}
	}
	
	public L2MapRegion getRestartRegion(L2Character activeChar, String point)
	{
		try
		{
			L2PcInstance player = ((L2PcInstance) activeChar);
			L2MapRegion region = _regions.get(point);
			
			if (region.getBannedRace().containsKey(player.getRace()))
			{
				getRestartRegion(player, region.getBannedRace().get(player.getRace()));
			}
			return region;
		}
		catch (Exception e)
		{
			return _regions.get(defaultRespawn);
		}
	}
	
	public L2MapRegion getMapRegionByName(String regionName)
	{
		return _regions.get(regionName);
	}
	
	public static MapRegionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MapRegionManager _instance = new MapRegionManager();
	}
}