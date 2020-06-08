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
package l2e.gameserver.model.entity.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Hitman
{
	protected static final Logger _log = Logger.getLogger(Hitman.class.getName());
	
	private static Hitman _instance;
	protected FastMap<Integer, PlayerToAssasinate> _targets;
	private final FastMap<String, Integer> _currency;
	private final DecimalFormat f = new DecimalFormat(",##0,000");
	
	private static String SQL_SELECT = "SELECT targetId, clientId, target_name, itemId, bounty, pending_delete FROM hitman_list";
	private static String SQL_DELETE = "DELETE FROM hitman_list WHERE targetId = ?";
	private static String SQL_SAVEING = "REPLACE INTO hitman_list (targetId,clientId,target_name,itemId,bounty,pending_delete) VALUES (?,?,?,?,?,?)";
	private static String[] SQL_OFFLINE =
	{
		"SELECT charId, char_name FROM characters WHERE char_name = ?",
		"SELECT charId, char_name FROM characters WHERE charId = ?"
	};
	
	private final int MIN_MAX_CLEAN_RATE = Config.HITMAN_SAVE_TARGET * 60000;
	
	public static boolean start()
	{
		if (Config.HITMAN_ENABLE_EVENT)
		{
			getInstance();
		}
		return _instance != null;
	}
	
	public static Hitman getInstance()
	{
		if (_instance == null)
		{
			_instance = new Hitman();
		}
		return _instance;
	}
	
	public Hitman()
	{
		_targets = load();
		_currency = loadCurrency();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new AISystem(), MIN_MAX_CLEAN_RATE, MIN_MAX_CLEAN_RATE);
	}
	
	private FastMap<String, Integer> loadCurrency()
	{
		FastMap<String, Integer> currency = new FastMap<>();
		try
		{
			for (Integer itemId : Config.HITMAN_CURRENCY)
			{
				currency.put(getCurrencyName(itemId).trim().replaceAll(" ", "_"), itemId);
			}
		}
		catch (Exception e)
		{
			return new FastMap<>();
		}
		return currency;
	}
	
	public FastMap<String, Integer> getCurrencys()
	{
		return _currency;
	}
	
	public Integer getCurrencyId(String name)
	{
		return _currency.get(name);
	}
	
	public String getCurrencyName(Integer itemId)
	{
		return ItemHolder.getInstance().getTemplate(itemId).getName();
	}
	
	private FastMap<Integer, PlayerToAssasinate> load()
	{
		FastMap<Integer, PlayerToAssasinate> map = new FastMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement(SQL_SELECT);
			ResultSet rs = st.executeQuery();
			while (rs.next())
			{
				int targetId = rs.getInt("targetId");
				int clientId = rs.getInt("clientId");
				String target_name = rs.getString("target_name");
				int itemId = rs.getInt("itemId");
				Long bounty = rs.getLong("bounty");
				boolean pending = rs.getInt("pending_delete") == 1;
				
				if (pending)
				{
					removeTarget(targetId, false);
				}
				else
				{
					map.put(targetId, new PlayerToAssasinate(targetId, clientId, itemId, bounty, target_name));
				}
			}
			_log.info("HitmanEngine[Hitman.load()]: Started - " + map.size() + " Assassination Target(s)");
		}
		catch (Exception e)
		{
			_log.warning("Hitman: " + e.getCause());
			return new FastMap<>();
		}
		return map;
	}
	
	public void onDeath(L2PcInstance assassin, L2PcInstance target)
	{
		if (_targets.containsKey(target.getObjectId()))
		{
			int assassinClan = assassin.getClanId();
			int assassinAlly = assassin.getAllyId();
			
			if (!Config.HITMAN_SAME_TEAM)
			{
				if (((assassinClan != 0) && (assassinClan == target.getClanId())) || ((assassinAlly != 0) && (assassinAlly == target.getAllyId())))
				{
					assassin.sendMessage((new CustomMessage("Hitman.CONSIDERED", assassin.getLang())).toString());
					assassin.sendMessage((new CustomMessage("Hitman.SAME_CLAN", assassin.getLang())).toString());
					return;
				}
			}
			
			PlayerToAssasinate pta = _targets.get(target.getObjectId());
			String name = getOfflineData(null, pta.getClientId())[1];
			L2PcInstance client = L2World.getInstance().getPlayer(name);
			
			target.sendMessage((new CustomMessage("Hitman.RECEIVE_REWARD", target.getLang())).toString());
			
			if (client != null)
			{
				client.sendMessage("" + LocalizationStorage.getInstance().getString(assassin.getLang(), "Hitman.ASSASIN_REQUEST") + " " + target.getName() + " " + LocalizationStorage.getInstance().getString(assassin.getLang(), "Hitman.FULFILL") + "");
				client.setHitmanTarget(0);
			}
			
			assassin.sendMessage("" + LocalizationStorage.getInstance().getString(assassin.getLang(), "Hitman.MURDER") + " " + target.getName() + " " + LocalizationStorage.getInstance().getString(assassin.getLang(), "Hitman.RECEIVE_REWARD") + "");
			rewardAssassin(assassin, target, pta.getItemId(), pta.getBounty());
			removeTarget(pta.getObjectId(), true);
		}
	}
	
	private void rewardAssassin(L2PcInstance activeChar, L2PcInstance target, int itemId, Long bounty)
	{
		PcInventory inv = activeChar.getInventory();
		SystemMessage systemMessage;
		
		if (ItemHolder.getInstance().createDummyItem(itemId).isStackable())
		{
			inv.addItem("Hitman", itemId, bounty, activeChar, target);
			if (bounty > 1)
			{
				systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				systemMessage.addItemName(itemId);
				systemMessage.addItemNumber(bounty);
			}
			else
			{
				systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				systemMessage.addItemName(itemId);
			}
			activeChar.sendPacket(systemMessage);
		}
		else
		{
			for (int i = 0; i < bounty; ++i)
			{
				inv.addItem("Hitman", itemId, 1, activeChar, target);
				systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				systemMessage.addItemName(itemId);
				activeChar.sendPacket(systemMessage);
			}
		}
	}
	
	public void onEnterWorld(L2PcInstance activeChar)
	{
		if (_targets.containsKey(activeChar.getObjectId()))
		{
			activeChar.sendMessage((new CustomMessage("Hitman.ASK_MURDER", activeChar.getLang())).toString());
		}
		
		if (activeChar.getHitmanTarget() > 0)
		{
			if (!_targets.containsKey(activeChar.getHitmanTarget()))
			{
				activeChar.sendMessage((new CustomMessage("Hitman.TARGET_ELIMINATE", activeChar.getLang())).toString());
				activeChar.setHitmanTarget(0);
			}
			else
			{
				activeChar.sendMessage((new CustomMessage("Hitman.TARGET_STILL", activeChar.getLang())).toString());
			}
		}
	}
	
	public void save()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (PlayerToAssasinate pta : _targets.values())
			{
				PreparedStatement st = con.prepareStatement(SQL_SAVEING);
				st.setInt(1, pta.getObjectId());
				st.setInt(2, pta.getClientId());
				st.setString(3, pta.getName());
				st.setInt(4, pta.getItemId());
				st.setLong(5, pta.getBounty());
				st.setInt(6, pta.isPendingDelete() ? 1 : 0);
				st.execute();
				st.close();
			}
		}
		catch (Exception e)
		{
			_log.warning("Hitman: " + e);
		}
		System.out.println("Hitman System: Data saved!!");
	}
	
	public void putHitOn(L2PcInstance client, String playerName, Long bounty, Integer itemId)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(playerName);
		
		if (client.getHitmanTarget() > 0)
		{
			client.sendMessage("" + LocalizationStorage.getInstance().getString(client.getLang(), "Hitman.OUR_CLIENT") + " - " + CharNameHolder.getInstance().getNameById(client.getHitmanTarget()));
			return;
		}
		else if (client.getInventory().getInventoryItemCount(itemId, -1) < bounty)
		{
			client.sendMessage("" + LocalizationStorage.getInstance().getString(client.getLang(), "Hitman.REWARD_FOR_KILL") + " " + ItemHolder.getInstance().getTemplate(itemId).getName() + ".");
			return;
		}
		else if ((player == null) && CharNameHolder.getInstance().doesCharNameExist(playerName))
		{
			Integer targetId = Integer.parseInt(getOfflineData(playerName, 0)[0]);
			
			if (_targets.containsKey(targetId))
			{
				client.sendMessage((new CustomMessage("Hitman.ALREADY_HIT", client.getLang())).toString());
				return;
			}
			_targets.put(targetId, new PlayerToAssasinate(targetId, client.getObjectId(), itemId, bounty, playerName));
			client.destroyItemByItemId("Hitman", itemId, bounty, client, true);
			client.setHitmanTarget(targetId);
			if (Config.HITMAN_ANNOUNCE)
			{
				Announcements.getInstance().announceToAll(client.getName() + " " + LocalizationStorage.getInstance().getString(client.getLang(), "Hitman.PAID") + " " + (bounty > 999 ? f.format(bounty) : bounty) + " " + getCurrencyName(itemId) + " " + LocalizationStorage.getInstance().getString(client.getLang(), "Hitman.FOR_KILL") + " " + playerName + ".");
			}
		}
		else if ((player != null) && CharNameHolder.getInstance().doesCharNameExist(playerName))
		{
			if (_targets.containsKey(player.getObjectId()))
			{
				client.sendMessage((new CustomMessage("Hitman.ALREADY_HIT", client.getLang())).toString());
				return;
			}
			player.sendMessage((new CustomMessage("Hitman.HIT_YOU", client.getLang())).toString());
			_targets.put(player.getObjectId(), new PlayerToAssasinate(player, client.getObjectId(), itemId, bounty));
			client.destroyItemByItemId("Hitman", itemId, bounty, client, true);
			client.setHitmanTarget(player.getObjectId());
			if (Config.HITMAN_ANNOUNCE)
			{
				Announcements.getInstance().announceToAll(client.getName() + " " + LocalizationStorage.getInstance().getString(client.getLang(), "Hitman.PAID") + " " + (bounty > 999 ? f.format(bounty) : bounty) + " " + getCurrencyName(itemId) + " " + LocalizationStorage.getInstance().getString(client.getLang(), "Hitman.FOR_KILL") + " " + playerName + ".");
			}
		}
		else
		{
			client.sendMessage((new CustomMessage("Hitman.NAME_INVALID", client.getLang())).toString());
		}
	}
	
	public class AISystem implements Runnable
	{
		@Override
		public void run()
		{
			if (Config.DEBUG)
			{
				_log.info("Cleaning sequance initiated.");
			}
			for (PlayerToAssasinate target : _targets.values())
			{
				if (target.isPendingDelete())
				{
					removeTarget(target.getObjectId(), true);
				}
			}
			save();
		}
	}
	
	public void removeTarget(int obId, boolean live)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement(SQL_DELETE);
			st.setInt(1, obId);
			st.execute();
			
			if (live)
			{
				_targets.remove(obId);
			}
		}
		catch (Exception e)
		{
			_log.warning("Hitman: " + e);
		}
	}
	
	public void cancelAssasination(String name, L2PcInstance client)
	{
		L2PcInstance target = L2World.getInstance().getPlayer(name);
		
		if (client.getHitmanTarget() <= 0)
		{
			client.sendMessage((new CustomMessage("Hitman.DONT_OWN", client.getLang())).toString());
			return;
		}
		else if ((target == null) && CharNameHolder.getInstance().doesCharNameExist(name))
		{
			PlayerToAssasinate pta = _targets.get(client.getHitmanTarget());
			
			if (!_targets.containsKey(pta.getObjectId()))
			{
				client.sendMessage((new CustomMessage("Hitman.NO_HIT", client.getLang())).toString());
			}
			else if (pta.getClientId() == client.getObjectId())
			{
				removeTarget(pta.getObjectId(), true);
				client.sendMessage((new CustomMessage("Hitman.CANCEL_HIT", client.getLang())).toString());
				client.setHitmanTarget(0);
			}
			else
			{
				client.sendMessage((new CustomMessage("Hitman.NO_ACTUAL_TARGET", client.getLang())).toString());
			}
		}
		else if ((target != null) && CharNameHolder.getInstance().doesCharNameExist(name))
		{
			PlayerToAssasinate pta = _targets.get(target.getObjectId());
			
			if (!_targets.containsKey(pta.getObjectId()))
			{
				client.sendMessage((new CustomMessage("Hitman.NO_HIT", client.getLang())).toString());
			}
			else if (pta.getClientId() == client.getObjectId())
			{
				removeTarget(pta.getObjectId(), true);
				client.sendMessage((new CustomMessage("Hitman.CANCEL_HIT", client.getLang())).toString());
				target.sendMessage((new CustomMessage("Hitman.YOUR_HIT_CANCEL", target.getLang())).toString());
				client.setHitmanTarget(0);
			}
			else
			{
				client.sendMessage((new CustomMessage("Hitman.NO_ACTUAL_TARGET", client.getLang())).toString());
			}
		}
		else
		{
			client.sendMessage((new CustomMessage("Hitman.NAME_INVALID", client.getLang())).toString());
		}
	}
	
	public String[] getOfflineData(String name, int objId)
	{
		String[] set = new String[2];
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement(objId > 0 ? SQL_OFFLINE[1] : SQL_OFFLINE[0]);
			
			if (objId > 0)
			{
				st.setInt(1, objId);
			}
			else
			{
				st.setString(1, name);
			}
			
			ResultSet rs = st.executeQuery();
			
			while (rs.next())
			{
				set[0] = String.valueOf(rs.getInt("charId"));
				set[1] = rs.getString("char_name");
			}
			st.close();
			rs.close();
		}
		catch (Exception e)
		{
			_log.warning("Hitman: " + e);
		}
		return set;
	}
	
	public boolean exists(int objId)
	{
		return _targets.containsKey(objId);
	}
	
	public PlayerToAssasinate getTarget(int objId)
	{
		return _targets.get(objId);
	}
	
	public FastMap<Integer, PlayerToAssasinate> getTargets()
	{
		return _targets;
	}
	
	public FastMap<Integer, PlayerToAssasinate> getTargetsOnline()
	{
		FastMap<Integer, PlayerToAssasinate> online = new FastMap<>();
		
		for (Integer objId : _targets.keySet())
		{
			PlayerToAssasinate pta = _targets.get(objId);
			if (pta.isOnline() && !pta.isPendingDelete())
			{
				online.put(objId, pta);
			}
		}
		return online;
	}
	
	public void set_targets(FastMap<Integer, PlayerToAssasinate> targets)
	{
		_targets = targets;
	}
}