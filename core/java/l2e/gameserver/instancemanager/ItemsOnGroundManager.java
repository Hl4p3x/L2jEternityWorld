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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ItemsAutoDestroy;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2EtcItemType;
import l2e.util.L2FastList;

public class ItemsOnGroundManager implements Runnable
{
	private static final Logger _log = Logger.getLogger(ItemsOnGroundManager.class.getName());
	
	protected List<L2ItemInstance> _items = new L2FastList<>(true);
	
	protected ItemsOnGroundManager()
	{
		if (Config.SAVE_DROPPED_ITEM_INTERVAL > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, Config.SAVE_DROPPED_ITEM_INTERVAL, Config.SAVE_DROPPED_ITEM_INTERVAL);
		}
		load();
	}
	
	private void load()
	{
		if (!Config.SAVE_DROPPED_ITEM && Config.CLEAR_DROPPED_ITEM_TABLE)
		{
			emptyTable();
		}
		
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				String str = null;
				if (!Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
				{
					str = "update itemsonground set drop_time=? where drop_time=-1 and equipable=0";
				}
				else if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
				{
					str = "update itemsonground set drop_time=? where drop_time=-1";
				}
				PreparedStatement statement = con.prepareStatement(str);
				statement.setLong(1, System.currentTimeMillis());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error while updating table ItemsOnGround " + e.getMessage(), e);
			}
		}
		
		L2ItemInstance item;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable FROM itemsonground");
			ResultSet rset;
			int count = 0;
			rset = statement.executeQuery();
			while (rset.next())
			{
				item = new L2ItemInstance(rset.getInt(1), rset.getInt(2));
				L2World.getInstance().storeObject(item);
				
				if (item.isStackable() && (rset.getInt(3) > 1))
				{
					item.setCount(rset.getInt(3));
				}
				
				if (rset.getInt(4) > 0)
				{
					item.setEnchantLevel(rset.getInt(4));
				}
				item.getPosition().setWorldPosition(rset.getInt(5), rset.getInt(6), rset.getInt(7));
				item.getPosition().setWorldRegion(L2World.getInstance().getRegion(item.getPosition().getWorldPosition()));
				item.getPosition().getWorldRegion().addVisibleObject(item);
				final long dropTime = rset.getLong(8);
				item.setDropTime(dropTime);
				item.setProtected(dropTime == -1);
				item.setIsVisible(true);
				L2World.getInstance().addVisibleObject(item, item.getPosition().getWorldRegion());
				_items.add(item);
				count++;
				
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getId()))
				{
					if (dropTime > -1)
					{
						if (((Config.AUTODESTROY_ITEM_AFTER > 0) && (item.getItemType() != L2EtcItemType.HERB)) || ((Config.HERB_AUTO_DESTROY_TIME > 0) && (item.getItemType() == L2EtcItemType.HERB)))
						{
							ItemsAutoDestroy.getInstance().addItem(item);
						}
					}
				}
			}
			rset.close();
			statement.close();
			
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " items.");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error while loading ItemsOnGround " + e.getMessage(), e);
		}
		
		if (Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD)
		{
			emptyTable();
		}
	}
	
	public void save(L2ItemInstance item)
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		_items.add(item);
	}
	
	public void removeObject(L2ItemInstance item)
	{
		if (Config.SAVE_DROPPED_ITEM)
		{
			_items.remove(item);
		}
	}
	
	public void saveInDb()
	{
		run();
	}
	
	public void cleanUp()
	{
		_items.clear();
	}
	
	public void emptyTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM itemsonground");
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error while cleaning table ItemsOnGround " + e1.getMessage(), e1);
		}
	}
	
	@Override
	public synchronized void run()
	{
		if (!Config.SAVE_DROPPED_ITEM)
		{
			return;
		}
		
		emptyTable();
		
		if (_items.isEmpty())
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) VALUES(?,?,?,?,?,?,?,?,?)");
			
			for (L2ItemInstance item : _items)
			{
				if (item == null)
				{
					continue;
				}
				
				if (CursedWeaponsManager.getInstance().isCursed(item.getId()))
				{
					continue;
				}
				
				try
				{
					statement.setInt(1, item.getObjectId());
					statement.setInt(2, item.getId());
					statement.setLong(3, item.getCount());
					statement.setInt(4, item.getEnchantLevel());
					statement.setInt(5, item.getX());
					statement.setInt(6, item.getY());
					statement.setInt(7, item.getZ());
					statement.setLong(8, (item.isProtected() ? -1 : item.getDropTime()));
					statement.setLong(9, (item.isEquipable() ? 1 : 0));
					statement.execute();
					statement.clearParameters();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error while inserting into table ItemsOnGround: " + e.getMessage(), e);
				}
			}
			statement.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": SQL error while storing items on ground: " + e.getMessage(), e);
		}
	}
	
	public static final ItemsOnGroundManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsOnGroundManager _instance = new ItemsOnGroundManager();
	}
}