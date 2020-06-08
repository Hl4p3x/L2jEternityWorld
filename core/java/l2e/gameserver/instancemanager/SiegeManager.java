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
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.skills.L2Skill;
import gnu.trove.map.hash.TIntObjectHashMap;

public class SiegeManager
{
	private static final Logger _log = Logger.getLogger(SiegeManager.class.getName());
	
	public static final SiegeManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private int _attackerMaxClans = 500;
	private int _attackerRespawnDelay = 0;
	private int _defenderMaxClans = 500;
	
	private TIntObjectHashMap<FastList<SiegeSpawn>> _artefactSpawnList;
	private TIntObjectHashMap<FastList<SiegeSpawn>> _controlTowerSpawnList;
	private TIntObjectHashMap<FastList<SiegeSpawn>> _flameTowerSpawnList;
	
	private int _flagMaxCount = 1;
	private int _siegeClanMinLevel = 5;
	private int _siegeLength = 120;
	private int _bloodAllianceReward = 0;
	
	protected SiegeManager()
	{
		load();
	}
	
	public final void addSiegeSkills(L2PcInstance character)
	{
		for (L2Skill sk : SkillHolder.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0))
		{
			character.addSkill(sk, false);
		}
	}
	
	public final boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if (!(activeChar.isPlayer()))
		{
			return false;
		}
		
		String text = "";
		L2PcInstance player = (L2PcInstance) activeChar;
		Castle castle = CastleManager.getInstance().getCastle(player);
		
		if ((castle == null) || (castle.getId() <= 0))
		{
			text = "You must be on castle ground to summon this";
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			text = "You can only summon this during a siege.";
		}
		else if ((player.getClanId() != 0) && (castle.getSiege().getAttackerClan(player.getClanId()) == null))
		{
			text = "You can only summon this as a registered attacker.";
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}
	
	public final boolean checkIsRegistered(L2Clan clan, int castleid)
	{
		if (clan == null)
		{
			return false;
		}
		
		if (clan.getCastleId() > 0)
		{
			return true;
		}
		
		boolean register = false;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?");
			statement.setInt(1, clan.getId());
			statement.setInt(2, castleid);
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				register = true;
				break;
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: checkIsRegistered(): " + e.getMessage(), e);
		}
		return register;
	}
	
	public final void removeSiegeSkills(L2PcInstance character)
	{
		for (L2Skill sk : SkillHolder.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0))
		{
			character.removeSkill(sk);
		}
	}
	
	private final void load()
	{
		Properties siegeSettings = new Properties();
		final File file = new File(Config.SIEGE_CONFIGURATION_FILE);
		try (InputStream is = new FileInputStream(file))
		{
			siegeSettings.load(is);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while loading Territory War Manager settings!", e);
		}
		
		_attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
		_attackerRespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "0"));
		_defenderMaxClans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
		_flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
		_siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "5"));
		_siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
		_bloodAllianceReward = Integer.decode(siegeSettings.getProperty("BloodAllianceReward", "0"));
		
		_controlTowerSpawnList = new TIntObjectHashMap<>();
		_artefactSpawnList = new TIntObjectHashMap<>();
		_flameTowerSpawnList = new TIntObjectHashMap<>();
		
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			FastList<SiegeSpawn> _controlTowersSpawns = new FastList<>();
			
			for (int i = 1; i < 0xFF; i++)
			{
				String _spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + i, "");
				
				if (_spawnParams.isEmpty())
				{
					break;
				}
				
				StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
				
				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int npc_id = Integer.parseInt(st.nextToken());
					int hp = Integer.parseInt(st.nextToken());
					
					_controlTowersSpawns.add(new SiegeSpawn(castle.getId(), x, y, z, 0, npc_id, hp));
				}
				catch (Exception e)
				{
					_log.warning("Error while loading control tower(s) for " + castle.getName() + " castle.");
				}
			}
			
			FastList<SiegeSpawn> _flameTowersSpawns = new FastList<>();
			
			for (int i = 1; i < 0xFF; i++)
			{
				String _spawnParams = siegeSettings.getProperty(castle.getName() + "FlameTower" + i, "");
				
				if (_spawnParams.isEmpty())
				{
					break;
				}
				
				StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
				
				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int npc_id = Integer.parseInt(st.nextToken());
					int hp = Integer.parseInt(st.nextToken());
					
					_flameTowersSpawns.add(new SiegeSpawn(castle.getId(), x, y, z, 0, npc_id, hp));
				}
				catch (Exception e)
				{
					_log.warning("Error while loading artefact(s) for " + castle.getName() + " castle.");
				}
			}
			
			FastList<SiegeSpawn> _artefactSpawns = new FastList<>();
			
			for (int i = 1; i < 0xFF; i++)
			{
				String _spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + i, "");
				
				if (_spawnParams.isEmpty())
				{
					break;
				}
				
				StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
				
				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int heading = Integer.parseInt(st.nextToken());
					int npc_id = Integer.parseInt(st.nextToken());
					
					_artefactSpawns.add(new SiegeSpawn(castle.getId(), x, y, z, heading, npc_id));
				}
				catch (Exception e)
				{
					_log.warning("Error while loading artefact(s) for " + castle.getName() + " castle.");
				}
			}
			
			MercTicketManager.MERCS_MAX_PER_CASTLE[castle.getId() - 1] = Integer.parseInt(siegeSettings.getProperty(castle.getName() + "MaxMercenaries", Integer.toString(MercTicketManager.MERCS_MAX_PER_CASTLE[castle.getId() - 1])).trim());
			
			_controlTowerSpawnList.put(castle.getId(), _controlTowersSpawns);
			_artefactSpawnList.put(castle.getId(), _artefactSpawns);
			_flameTowerSpawnList.put(castle.getId(), _flameTowersSpawns);
		}
	}
	
	public final FastList<SiegeSpawn> getArtefactSpawnList(int _castleId)
	{
		return _artefactSpawnList.get(_castleId);
	}
	
	public final FastList<SiegeSpawn> getControlTowerSpawnList(int _castleId)
	{
		return _controlTowerSpawnList.get(_castleId);
	}
	
	public final FastList<SiegeSpawn> getFlameTowerSpawnList(int _castleId)
	{
		return _flameTowerSpawnList.get(_castleId);
	}
	
	public final int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}
	
	public final int getAttackerRespawnDelay()
	{
		return _attackerRespawnDelay;
	}
	
	public final int getDefenderMaxClans()
	{
		return _defenderMaxClans;
	}
	
	public final int getFlagMaxCount()
	{
		return _flagMaxCount;
	}
	
	public final Siege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final Siege getSiege(int x, int y, int z)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getSiege().checkIfInZone(x, y, z))
			{
				return castle.getSiege();
			}
		}
		return null;
	}
	
	public final int getSiegeClanMinLevel()
	{
		return _siegeClanMinLevel;
	}
	
	public final int getSiegeLength()
	{
		return _siegeLength;
	}
	
	public final int getBloodAllianceReward()
	{
		return _bloodAllianceReward;
	}
	
	public final List<Siege> getSieges()
	{
		FastList<Siege> sieges = new FastList<>();
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			sieges.add(castle.getSiege());
		}
		return sieges;
	}
	
	public static class SiegeSpawn
	{
		Location _location;
		private final int _npcId;
		private final int _heading;
		private final int _castleId;
		private int _hp;
		
		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
		}
		
		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
			_hp = hp;
		}
		
		public int getCastleId()
		{
			return _castleId;
		}
		
		public int getNpcId()
		{
			return _npcId;
		}
		
		public int getHeading()
		{
			return _heading;
		}
		
		public int getHp()
		{
			return _hp;
		}
		
		public Location getLocation()
		{
			return _location;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final SiegeManager _instance = new SiegeManager();
	}
}