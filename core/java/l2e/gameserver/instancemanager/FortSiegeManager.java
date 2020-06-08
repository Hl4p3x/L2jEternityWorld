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
import javolution.util.FastMap;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.CombatFlag;
import l2e.gameserver.model.FortSiegeSpawn;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.FortSiege;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class FortSiegeManager
{
	private static final Logger _log = Logger.getLogger(FortSiegeManager.class.getName());
	
	private int _attackerMaxClans = 500;
	
	private FastMap<Integer, FastList<FortSiegeSpawn>> _commanderSpawnList;
	private FastMap<Integer, FastList<CombatFlag>> _flagList;
	private int _flagMaxCount = 1;
	private int _siegeClanMinLevel = 4;
	private int _siegeLength = 60;
	private int _countDownLength = 10;
	private int _suspiciousMerchantRespawnDelay = 180;
	private List<FortSiege> _sieges;
	
	protected FortSiegeManager()
	{
		load();
	}
	
	public final void addSiegeSkills(L2PcInstance character)
	{
		character.addSkill(SkillHolder.FrequentSkill.SEAL_OF_RULER.getSkill(), false);
		character.addSkill(SkillHolder.FrequentSkill.BUILD_HEADQUARTERS.getSkill(), false);
	}
	
	public final boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if (!(activeChar.isPlayer()))
		{
			return false;
		}
		
		String text = "";
		final L2PcInstance player = (L2PcInstance) activeChar;
		final Fort fort = FortManager.getInstance().getFort(player);
		
		if ((fort == null) || (fort.getId() <= 0))
		{
			text = "You must be on fort ground to summon this";
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			text = "You can only summon this during a siege.";
		}
		else if ((player.getClanId() != 0) && (fort.getSiege().getAttackerClan(player.getClanId()) == null))
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
	
	public final boolean checkIsRegistered(L2Clan clan, int fortid)
	{
		if (clan == null)
		{
			return false;
		}
		
		boolean register = false;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans where clan_id=? and fort_id=?");
			statement.setInt(1, clan.getId());
			statement.setInt(2, fortid);
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
		character.removeSkill(SkillHolder.FrequentSkill.SEAL_OF_RULER.getSkill());
		character.removeSkill(SkillHolder.FrequentSkill.BUILD_HEADQUARTERS.getSkill());
	}
	
	private final void load()
	{
		final Properties siegeSettings = new Properties();
		final File file = new File(Config.FORTSIEGE_CONFIGURATION_FILE);
		try (InputStream is = new FileInputStream(file))
		{
			siegeSettings.load(is);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while loading Fort Siege Manager settings!", e);
		}
		
		_attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
		_flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
		_siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
		_siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "60"));
		_countDownLength = Integer.decode(siegeSettings.getProperty("CountDownLength", "10"));
		_suspiciousMerchantRespawnDelay = Integer.decode(siegeSettings.getProperty("SuspiciousMerchantRespawnDelay", "180"));
		
		_commanderSpawnList = new FastMap<>();
		_flagList = new FastMap<>();
		
		for (Fort fort : FortManager.getInstance().getForts())
		{
			FastList<FortSiegeSpawn> _commanderSpawns = new FastList<>();
			FastList<CombatFlag> _flagSpawns = new FastList<>();
			for (int i = 1; i < 5; i++)
			{
				final String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Commander" + i, "");
				if (_spawnParams.isEmpty())
				{
					break;
				}
				final StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
				
				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int heading = Integer.parseInt(st.nextToken());
					int npc_id = Integer.parseInt(st.nextToken());
					
					_commanderSpawns.add(new FortSiegeSpawn(fort.getId(), x, y, z, heading, npc_id, i));
				}
				catch (Exception e)
				{
					_log.warning("Error while loading commander(s) for " + fort.getName() + " fort.");
				}
			}
			
			_commanderSpawnList.put(fort.getId(), _commanderSpawns);
			
			for (int i = 1; i < 4; i++)
			{
				final String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Flag" + i, "");
				if (_spawnParams.isEmpty())
				{
					break;
				}
				final StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
				
				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int flag_id = Integer.parseInt(st.nextToken());
					
					_flagSpawns.add(new CombatFlag(fort.getId(), x, y, z, 0, flag_id));
				}
				catch (Exception e)
				{
					_log.warning("Error while loading flag(s) for " + fort.getName() + " fort.");
				}
			}
			_flagList.put(fort.getId(), _flagSpawns);
		}
	}
	
	public final FastList<FortSiegeSpawn> getCommanderSpawnList(int _fortId)
	{
		if (_commanderSpawnList.containsKey(_fortId))
		{
			return _commanderSpawnList.get(_fortId);
		}
		return null;
	}
	
	public final FastList<CombatFlag> getFlagList(int _fortId)
	{
		if (_flagList.containsKey(_fortId))
		{
			return _flagList.get(_fortId);
		}
		return null;
	}
	
	public final int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}
	
	public final int getFlagMaxCount()
	{
		return _flagMaxCount;
	}
	
	public final int getSuspiciousMerchantRespawnDelay()
	{
		return _suspiciousMerchantRespawnDelay;
	}
	
	public final FortSiege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final FortSiege getSiege(int x, int y, int z)
	{
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (fort.getSiege().checkIfInZone(x, y, z))
			{
				return fort.getSiege();
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
	
	public final int getCountDownLength()
	{
		return _countDownLength;
	}
	
	public final List<FortSiege> getSieges()
	{
		if (_sieges == null)
		{
			_sieges = new FastList<>();
		}
		return _sieges;
	}
	
	public final void addSiege(FortSiege fortSiege)
	{
		if (_sieges == null)
		{
			_sieges = new FastList<>();
		}
		_sieges.add(fortSiege);
	}
	
	public boolean isCombat(int itemId)
	{
		return (itemId == 9819);
	}
	
	public boolean activateCombatFlag(L2PcInstance player, L2ItemInstance item)
	{
		if (!checkIfCanPickup(player))
		{
			return false;
		}
		
		final Fort fort = FortManager.getInstance().getFort(player);
		
		final FastList<CombatFlag> fcf = _flagList.get(fort.getId());
		for (CombatFlag cf : fcf)
		{
			if (cf.getCombatFlagInstance() == item)
			{
				cf.activate(player, item);
			}
		}
		return true;
	}
	
	public boolean checkIfCanPickup(L2PcInstance player)
	{
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED);
		sm.addItemName(9819);
		
		if (player.isCombatFlagEquipped())
		{
			player.sendPacket(sm);
			return false;
		}
		
		final Fort fort = FortManager.getInstance().getFort(player);
		
		if ((fort == null) || (fort.getId() <= 0))
		{
			player.sendPacket(sm);
			return false;
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			player.sendPacket(sm);
			return false;
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			player.sendPacket(sm);
			return false;
		}
		return true;
	}
	
	public void dropCombatFlag(L2PcInstance player, int fortId)
	{
		final Fort fort = FortManager.getInstance().getFortById(fortId);
		
		final FastList<CombatFlag> fcf = _flagList.get(fort.getId());
		
		for (CombatFlag cf : fcf)
		{
			if (cf.getPlayerObjectId() == player.getObjectId())
			{
				cf.dropIt();
				if (fort.getSiege().getIsInProgress())
				{
					cf.spawnMe();
				}
			}
		}
	}
	
	public static final FortSiegeManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FortSiegeManager _instance = new FortSiegeManager();
	}
}