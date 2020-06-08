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
package l2e.gameserver.data.sql;

import static l2e.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.xml.EnchantItemHPBonusParser;
import l2e.gameserver.engines.DocumentEngine;
import l2e.gameserver.engines.items.Item;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.instance.L2EventMonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Armor;
import l2e.gameserver.model.items.L2EtcItem;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance.ItemLocation;
import l2e.gameserver.model.items.type.L2ArmorType;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.scripting.scriptengine.events.ItemCreateEvent;
import l2e.gameserver.scripting.scriptengine.listeners.player.NewItemListener;
import l2e.gameserver.util.GMAudit;

public class ItemHolder
{
	private static Logger _log = Logger.getLogger(ItemHolder.class.getName());
	private static Logger _logItems = Logger.getLogger("item");
	
	private static FastList<NewItemListener> newItemListeners = new FastList<NewItemListener>().shared();
	
	public static final Map<String, Integer> _materials = new FastMap<>();
	public static final Map<String, Integer> _crystalTypes = new FastMap<>();
	public static final Map<String, Integer> _slots = new FastMap<>();
	public static final Map<String, L2WeaponType> _weaponTypes = new FastMap<>();
	public static final Map<String, L2ArmorType> _armorTypes = new FastMap<>();
	
	private L2Item[] _allTemplates;
	private final Map<Integer, L2EtcItem> _etcItems;
	private final Map<Integer, L2Armor> _armors;
	private final Map<Integer, L2Weapon> _weapons;
	
	static
	{
		_materials.put("adamantaite", L2Item.MATERIAL_ADAMANTAITE);
		_materials.put("blood_steel", L2Item.MATERIAL_BLOOD_STEEL);
		_materials.put("bone", L2Item.MATERIAL_BONE);
		_materials.put("bronze", L2Item.MATERIAL_BRONZE);
		_materials.put("cloth", L2Item.MATERIAL_CLOTH);
		_materials.put("chrysolite", L2Item.MATERIAL_CHRYSOLITE);
		_materials.put("cobweb", L2Item.MATERIAL_COBWEB);
		_materials.put("cotton", L2Item.MATERIAL_FINE_STEEL);
		_materials.put("crystal", L2Item.MATERIAL_CRYSTAL);
		_materials.put("damascus", L2Item.MATERIAL_DAMASCUS);
		_materials.put("dyestuff", L2Item.MATERIAL_DYESTUFF);
		_materials.put("fine_steel", L2Item.MATERIAL_FINE_STEEL);
		_materials.put("fish", L2Item.MATERIAL_FISH);
		_materials.put("gold", L2Item.MATERIAL_GOLD);
		_materials.put("horn", L2Item.MATERIAL_HORN);
		_materials.put("leather", L2Item.MATERIAL_LEATHER);
		_materials.put("liquid", L2Item.MATERIAL_LIQUID);
		_materials.put("mithril", L2Item.MATERIAL_MITHRIL);
		_materials.put("oriharukon", L2Item.MATERIAL_ORIHARUKON);
		_materials.put("paper", L2Item.MATERIAL_PAPER);
		_materials.put("rune_xp", L2Item.MATERIAL_RUNE_XP);
		_materials.put("rune_sp", L2Item.MATERIAL_RUNE_SP);
		_materials.put("rune_remove_penalty", L2Item.MATERIAL_RUNE_PENALTY);
		_materials.put("scale_of_dragon", L2Item.MATERIAL_SCALE_OF_DRAGON);
		_materials.put("seed", L2Item.MATERIAL_SEED);
		_materials.put("silver", L2Item.MATERIAL_SILVER);
		_materials.put("steel", L2Item.MATERIAL_STEEL);
		_materials.put("wood", L2Item.MATERIAL_WOOD);
		
		_crystalTypes.put("s84", L2Item.CRYSTAL_S84);
		_crystalTypes.put("s80", L2Item.CRYSTAL_S80);
		_crystalTypes.put("s", L2Item.CRYSTAL_S);
		_crystalTypes.put("a", L2Item.CRYSTAL_A);
		_crystalTypes.put("b", L2Item.CRYSTAL_B);
		_crystalTypes.put("c", L2Item.CRYSTAL_C);
		_crystalTypes.put("d", L2Item.CRYSTAL_D);
		_crystalTypes.put("none", L2Item.CRYSTAL_NONE);
		
		for (L2WeaponType type : L2WeaponType.values())
		{
			_weaponTypes.put(type.toString(), type);
		}
		
		for (L2ArmorType type : L2ArmorType.values())
		{
			_armorTypes.put(type.toString(), type);
		}
		
		_slots.put("shirt", L2Item.SLOT_UNDERWEAR);
		_slots.put("lbracelet", L2Item.SLOT_L_BRACELET);
		_slots.put("rbracelet", L2Item.SLOT_R_BRACELET);
		_slots.put("talisman", L2Item.SLOT_DECO);
		_slots.put("chest", L2Item.SLOT_CHEST);
		_slots.put("fullarmor", L2Item.SLOT_FULL_ARMOR);
		_slots.put("head", L2Item.SLOT_HEAD);
		_slots.put("hair", L2Item.SLOT_HAIR);
		_slots.put("hairall", L2Item.SLOT_HAIRALL);
		_slots.put("underwear", L2Item.SLOT_UNDERWEAR);
		_slots.put("back", L2Item.SLOT_BACK);
		_slots.put("neck", L2Item.SLOT_NECK);
		_slots.put("legs", L2Item.SLOT_LEGS);
		_slots.put("feet", L2Item.SLOT_FEET);
		_slots.put("gloves", L2Item.SLOT_GLOVES);
		_slots.put("chest,legs", L2Item.SLOT_CHEST | L2Item.SLOT_LEGS);
		_slots.put("belt", L2Item.SLOT_BELT);
		_slots.put("rhand", L2Item.SLOT_R_HAND);
		_slots.put("lhand", L2Item.SLOT_L_HAND);
		_slots.put("lrhand", L2Item.SLOT_LR_HAND);
		_slots.put("rear;lear", L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR);
		_slots.put("rfinger;lfinger", L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER);
		_slots.put("wolf", L2Item.SLOT_WOLF);
		_slots.put("greatwolf", L2Item.SLOT_GREATWOLF);
		_slots.put("hatchling", L2Item.SLOT_HATCHLING);
		_slots.put("strider", L2Item.SLOT_STRIDER);
		_slots.put("babypet", L2Item.SLOT_BABYPET);
		_slots.put("none", L2Item.SLOT_NONE);
		
		_slots.put("onepiece", L2Item.SLOT_FULL_ARMOR);
		_slots.put("hair2", L2Item.SLOT_HAIR2);
		_slots.put("dhair", L2Item.SLOT_HAIRALL);
		_slots.put("alldress", L2Item.SLOT_ALLDRESS);
		_slots.put("deco1", L2Item.SLOT_DECO);
		_slots.put("waist", L2Item.SLOT_BELT);
		
	}
	
	public static ItemHolder getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public Item newItem()
	{
		return new Item();
	}
	
	protected ItemHolder()
	{
		_etcItems = new FastMap<>();
		_armors = new FastMap<>();
		_weapons = new FastMap<>();
		load();
	}
	
	private void load()
	{
		int highest = 0;
		_armors.clear();
		_etcItems.clear();
		_weapons.clear();
		for (L2Item item : DocumentEngine.getInstance().loadItems())
		{
			if (highest < item.getId())
			{
				highest = item.getId();
			}
			if (item instanceof L2EtcItem)
			{
				_etcItems.put(item.getId(), (L2EtcItem) item);
			}
			else if (item instanceof L2Armor)
			{
				_armors.put(item.getId(), (L2Armor) item);
			}
			else
			{
				_weapons.put(item.getId(), (L2Weapon) item);
			}
		}
		buildFastLookupTable(highest);
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _etcItems.size() + " Etc Items");
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _armors.size() + " Armor Items");
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _weapons.size() + " Weapon Items");
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + (_etcItems.size() + _armors.size() + _weapons.size()) + " Items in total.");
	}
	
	private void buildFastLookupTable(int size)
	{
		_log.info(getClass().getSimpleName() + ": Highest item id used:" + size);
		_allTemplates = new L2Item[size + 1];
		
		for (L2Armor item : _armors.values())
		{
			_allTemplates[item.getId()] = item;
		}
		
		for (L2Weapon item : _weapons.values())
		{
			_allTemplates[item.getId()] = item;
		}
		
		for (L2EtcItem item : _etcItems.values())
		{
			_allTemplates[item.getId()] = item;
		}
	}
	
	public L2Item getTemplate(int id)
	{
		if ((id >= _allTemplates.length) || (id < 0))
		{
			return null;
		}
		
		return _allTemplates[id];
	}
	
	public L2ItemInstance createItem(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		if (!fireNewItemListeners(process, itemId, count, actor, reference))
		{
			return null;
		}
		
		L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		
		if (process.equalsIgnoreCase("loot"))
		{
			ScheduledFuture<?> itemLootShedule;
			if ((reference instanceof L2Attackable) && ((L2Attackable) reference).isRaid())
			{
				L2Attackable raid = (L2Attackable) reference;
				
				if ((raid.getFirstCommandChannelAttacked() != null) && !Config.AUTO_LOOT_RAIDS)
				{
					item.setOwnerId(raid.getFirstCommandChannelAttacked().getLeaderObjectId());
					itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), Config.LOOT_RAIDS_PRIVILEGE_INTERVAL);
					item.setItemLootShedule(itemLootShedule);
				}
			}
			else if ((!Config.AUTO_LOOT && actor.getUseAutoLoot()) || ((reference instanceof L2EventMonsterInstance) && ((L2EventMonsterInstance) reference).eventDropOnGround()))
			{
				item.setOwnerId(actor.getObjectId());
				itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), 15000);
				item.setItemLootShedule(itemLootShedule);
			}
		}
		
		if (Config.DEBUG)
		{
			_log.fine(getClass().getSimpleName() + ": Item created  oid:" + item.getObjectId() + " itemid:" + itemId);
		}
		
		L2World.getInstance().storeObject(item);
		
		if (item.isStackable() && (count > 1))
		{
			item.setCount(count);
		}
		
		if (Config.LOG_ITEMS && !process.equals("Reset"))
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (item.isEquipable() || (item.getId() == ADENA_ID))))
			{
				LogRecord record = new LogRecord(Level.INFO, "CREATE:" + process);
				record.setLoggerName("item");
				record.setParameters(new Object[]
				{
					item,
					actor,
					reference
				});
				_logItems.log(record);
			}
		}
		
		if (actor != null)
		{
			if (actor.isGM())
			{
				String referenceName = "no-reference";
				if (reference instanceof L2Object)
				{
					referenceName = (((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name");
				}
				else if (reference instanceof String)
				{
					referenceName = (String) reference;
				}
				String targetName = (actor.getTarget() != null ? actor.getTarget().getName() : "no-target");
				if (Config.GMAUDIT)
				{
					GMAudit.auditGMAction(actor.getName() + " [" + actor.getObjectId() + "]", process + "(id: " + itemId + " count: " + count + " name: " + item.getItemName() + " objId: " + item.getObjectId() + ")", targetName, "L2Object referencing this action is: " + referenceName);
				}
			}
		}
		
		return item;
	}
	
	public L2ItemInstance createItem(String process, int itemId, int count, L2PcInstance actor)
	{
		return createItem(process, itemId, count, actor, null);
	}
	
	public L2ItemInstance createDummyItem(int itemId)
	{
		L2Item item = getTemplate(itemId);
		if (item == null)
		{
			return null;
		}
		L2ItemInstance temp = new L2ItemInstance(0, item);
		return temp;
	}
	
	public void destroyItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		synchronized (item)
		{
			long old = item.getCount();
			item.setCount(0);
			item.setOwnerId(0);
			item.setItemLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);
			
			L2World.getInstance().removeObject(item);
			IdFactory.getInstance().releaseId(item.getObjectId());
			
			if (Config.LOG_ITEMS)
			{
				if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (item.isEquipable() || (item.getId() == ADENA_ID))))
				{
					LogRecord record = new LogRecord(Level.INFO, "DELETE:" + process);
					record.setLoggerName("item");
					record.setParameters(new Object[]
					{
						item,
						"PrevCount(" + old + ")",
						actor,
						reference
					});
					_logItems.log(record);
				}
			}
			
			if (actor != null)
			{
				if (actor.isGM())
				{
					String referenceName = "no-reference";
					if (reference instanceof L2Object)
					{
						referenceName = (((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name");
					}
					else if (reference instanceof String)
					{
						referenceName = (String) reference;
					}
					String targetName = (actor.getTarget() != null ? actor.getTarget().getName() : "no-target");
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(actor.getName() + " [" + actor.getObjectId() + "]", process + "(id: " + item.getId() + " count: " + item.getCount() + " itemObjId: " + item.getObjectId() + ")", targetName, "L2Object referencing this action is: " + referenceName);
					}
				}
			}
			
			if (item.getItem().isPetItem())
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?"))
				{
					statement.setInt(1, item.getObjectId());
					statement.execute();
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "could not delete pet objectid:", e);
				}
			}
		}
	}
	
	public void reload()
	{
		load();
		EnchantItemHPBonusParser.getInstance().load();
	}
	
	protected static class ResetOwner implements Runnable
	{
		L2ItemInstance _item;
		
		public ResetOwner(L2ItemInstance item)
		{
			_item = item;
		}
		
		@Override
		public void run()
		{
			_item.setOwnerId(0);
			_item.setItemLootShedule(null);
		}
	}
	
	public Set<Integer> getAllArmorsId()
	{
		return _armors.keySet();
	}
	
	public Set<Integer> getAllWeaponsId()
	{
		return _weapons.keySet();
	}
	
	public int getArraySize()
	{
		return _allTemplates.length;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemHolder _instance = new ItemHolder();
	}
	
	private boolean fireNewItemListeners(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		if (!newItemListeners.isEmpty() && (actor != null))
		{
			ItemCreateEvent event = new ItemCreateEvent();
			event.setItemId(itemId);
			event.setPlayer(actor);
			event.setCount(count);
			event.setProcess(process);
			event.setReference(reference);
			for (NewItemListener listener : newItemListeners)
			{
				if (listener.containsItemId(itemId))
				{
					if (!listener.onCreate(event))
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static void addNewItemListener(NewItemListener listener)
	{
		if (!newItemListeners.contains(listener))
		{
			newItemListeners.add(listener);
		}
	}
	
	public static void removeNewItemListener(NewItemListener listener)
	{
		newItemListeners.remove(listener);
	}
}