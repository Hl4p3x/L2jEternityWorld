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
package l2e.gameserver.model.items.instance;

import static l2e.gameserver.model.itemcontainer.PcInventory.ADENA_ID;
import static l2e.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.xml.EnchantItemOptionsParser;
import l2e.gameserver.data.xml.OptionsParser;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.DropProtection;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.L2Augmentation;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.knownlist.NullKnownList;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.L2Armor;
import l2e.gameserver.model.items.L2EtcItem;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.type.L2EtcItemType;
import l2e.gameserver.model.items.type.L2ItemType;
import l2e.gameserver.model.options.EnchantOptions;
import l2e.gameserver.model.options.Options;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.DropItem;
import l2e.gameserver.network.serverpackets.GetItem;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SpawnItem;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.scripting.scriptengine.events.AugmentEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemDropEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemPickupEvent;
import l2e.gameserver.scripting.scriptengine.listeners.player.AugmentListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.DropListener;
import l2e.gameserver.util.GMAudit;

public final class L2ItemInstance extends L2Object
{
	protected static final Logger _log = Logger.getLogger(L2ItemInstance.class.getName());
	private static final Logger _logItems = Logger.getLogger("item");
	
	private static FastList<AugmentListener> augmentListeners = new FastList<AugmentListener>().shared();
	private static FastList<DropListener> dropListeners = new FastList<DropListener>().shared();
	
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
		REFUND,
		MAIL,
		FREIGHT
	}
	
	private int _ownerId;
	
	private int _dropperObjectId = 0;
	
	private long _count;
	private long _initCount;
	private long _time;
	private boolean _decrease = false;
	
	private final int _itemId;
	
	private final L2Item _item;
	
	private ItemLocation _loc;
	
	private int _locData;
	
	private int _enchantLevel;
	
	private boolean _wear;
	
	private L2Augmentation _augmentation = null;
	
	private int _mana = -1;
	private boolean _consumingMana = false;
	private static final int MANA_CONSUMPTION_RATE = 60000;
	
	private int _type1;
	private int _type2;
	
	private long _dropTime;
	
	private boolean _published = false;
	
	private boolean _protected;
	
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	
	public static final int[] DEFAULT_ENCHANT_OPTIONS = new int[]
	{
		0,
		0,
		0
	};
	
	private int _lastChange = 2;
	private boolean _existsInDb;
	private boolean _storedInDb;
	
	private final ReentrantLock _dbLock = new ReentrantLock();
	
	private Elementals[] _elementals = null;
	
	private ScheduledFuture<?> itemLootShedule = null;
	public ScheduledFuture<?> _lifeTimeTask;
	
	private final DropProtection _dropProtection = new DropProtection();
	
	private int _shotsMask = 0;
	
	private final List<Options> _enchantOptions = new ArrayList<>();
	
	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		_itemId = itemId;
		_item = ItemHolder.getInstance().getTemplate(itemId);
		if ((_itemId == 0) || (_item == null))
		{
			throw new IllegalArgumentException();
		}
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : System.currentTimeMillis() + ((long) _item.getTime() * 60 * 1000);
		scheduleLifeTimeTask();
	}
	
	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		_itemId = item.getId();
		_item = item;
		if (_itemId == 0)
		{
			throw new IllegalArgumentException();
		}
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : System.currentTimeMillis() + ((long) _item.getTime() * 60 * 1000);
		scheduleLifeTimeTask();
	}
	
	public L2ItemInstance(int itemId)
	{
		this(IdFactory.getInstance().getNextId(), itemId);
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new NullKnownList(this));
	}
	
	public final void pickupMe(L2Character player)
	{
		if (!firePickupListeners(player.getActingPlayer()))
		{
			return;
		}
		
		assert getPosition().getWorldRegion() != null;
		
		L2WorldRegion oldregion = getPosition().getWorldRegion();
		
		GetItem gi = new GetItem(this, player.getObjectId());
		player.broadcastPacket(gi);
		
		synchronized (this)
		{
			setIsVisible(false);
			getPosition().setWorldRegion(null);
		}
		
		int itemId = getId();
		
		if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
		{
			MercTicketManager.getInstance().removeTicket(this);
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		
		if (!Config.DISABLE_TUTORIAL && ((itemId == PcInventory.ADENA_ID) || (itemId == 6353)))
		{
			L2PcInstance actor = player.getActingPlayer();
			if (actor != null)
			{
				QuestState qs = actor.getQuestState("_255_Tutorial");
				if ((qs != null) && (qs.getQuest() != null))
				{
					qs.getQuest().notifyEvent("CE" + itemId, null, actor);
				}
			}
		}
		L2World.getInstance().removeVisibleObject(this, oldregion);
	}
	
	public void setOwnerId(String process, int owner_id, L2PcInstance creator, Object reference)
	{
		setOwnerId(owner_id);
		
		if (Config.LOG_ITEMS)
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (getItem().isEquipable() || (getItem().getId() == ADENA_ID))))
			{
				LogRecord record = new LogRecord(Level.INFO, "SETOWNER:" + process);
				record.setLoggerName("item");
				record.setParameters(new Object[]
				{
					this,
					creator,
					reference
				});
				_logItems.log(record);
			}
		}
		
		if (creator != null)
		{
			if (creator.isGM())
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
				String targetName = (creator.getTarget() != null ? creator.getTarget().getName() : "no-target");
				if (Config.GMAUDIT)
				{
					GMAudit.auditGMAction(creator.getName() + " [" + creator.getObjectId() + "]", process + "(id: " + getId() + " name: " + getName() + ")", targetName, "L2Object referencing this action is: " + referenceName);
				}
			}
		}
	}
	
	public void setOwnerId(int owner_id)
	{
		if (owner_id == _ownerId)
		{
			return;
		}
		
		removeSkillsFromOwner();
		
		_ownerId = owner_id;
		_storedInDb = false;
		
		giveSkillsToOwner();
	}
	
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	public void setItemLocation(ItemLocation loc)
	{
		setItemLocation(loc, 0);
	}
	
	public void setItemLocation(ItemLocation loc, int loc_data)
	{
		if ((loc == _loc) && (loc_data == _locData))
		{
			return;
		}
		
		removeSkillsFromOwner();
		
		_loc = loc;
		_locData = loc_data;
		_storedInDb = false;
		
		giveSkillsToOwner();
	}
	
	public ItemLocation getItemLocation()
	{
		return _loc;
	}
	
	public void setCount(long count)
	{
		if (getCount() == count)
		{
			return;
		}
		
		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}
	
	public long getCount()
	{
		return _count;
	}
	
	public void changeCount(String process, long count, L2PcInstance creator, Object reference)
	{
		if (count == 0)
		{
			return;
		}
		long old = getCount();
		long max = getId() == ADENA_ID ? MAX_ADENA : Integer.MAX_VALUE;
		
		if ((count > 0) && (getCount() > (max - count)))
		{
			setCount(max);
		}
		else
		{
			setCount(getCount() + count);
		}
		
		if (getCount() < 0)
		{
			setCount(0);
		}
		
		_storedInDb = false;
		
		if (Config.LOG_ITEMS && (process != null))
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (_item.isEquipable() || (_item.getId() == ADENA_ID))))
			{
				LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
				record.setLoggerName("item");
				record.setParameters(new Object[]
				{
					this,
					"PrevCount(" + old + ")",
					creator,
					reference
				});
				_logItems.log(record);
			}
		}
		
		if (creator != null)
		{
			if (creator.isGM())
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
				String targetName = (creator.getTarget() != null ? creator.getTarget().getName() : "no-target");
				if (Config.GMAUDIT)
				{
					GMAudit.auditGMAction(creator.getName() + " [" + creator.getObjectId() + "]", process + "(id: " + getId() + " objId: " + getObjectId() + " name: " + getName() + " count: " + count + ")", targetName, "L2Object referencing this action is: " + referenceName);
				}
			}
		}
	}
	
	public void changeCountWithoutTrace(int count, L2PcInstance creator, Object reference)
	{
		this.changeCount(null, count, creator, reference);
	}
	
	public int isEnchantable()
	{
		if ((getItemLocation() == ItemLocation.INVENTORY) || (getItemLocation() == ItemLocation.PAPERDOLL))
		{
			return getItem().isEnchantable();
		}
		return 0;
	}
	
	public boolean isEquipable()
	{
		return !((_item.getBodyPart() == 0) || (_item.getItemType() == L2EtcItemType.ARROW) || (_item.getItemType() == L2EtcItemType.BOLT) || (_item.getItemType() == L2EtcItemType.LURE));
	}
	
	public boolean isEquipped()
	{
		return (_loc == ItemLocation.PAPERDOLL) || (_loc == ItemLocation.PET_EQUIP);
	}
	
	public int getLocationSlot()
	{
		assert (_loc == ItemLocation.PAPERDOLL) || (_loc == ItemLocation.PET_EQUIP) || (_loc == ItemLocation.INVENTORY) || (_loc == ItemLocation.MAIL) || (_loc == ItemLocation.FREIGHT);
		return _locData;
	}
	
	public L2Item getItem()
	{
		return _item;
	}
	
	public int getCustomType1()
	{
		return _type1;
	}
	
	public int getCustomType2()
	{
		return _type2;
	}
	
	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}
	
	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}
	
	public void setDropTime(long time)
	{
		_dropTime = time;
	}
	
	public long getDropTime()
	{
		return _dropTime;
	}
	
	public L2ItemType getItemType()
	{
		return _item.getItemType();
	}
	
	public boolean isWear()
	{
		return _wear;
	}
	
	@Override
	public int getId()
	{
		return _itemId;
	}
	
	public int getDisplayId()
	{
		return getItem().getDisplayId();
	}
	
	public boolean isEtcItem()
	{
		return (_item instanceof L2EtcItem);
	}
	
	public boolean isWeapon()
	{
		return (_item instanceof L2Weapon);
	}
	
	public boolean isArmor()
	{
		return (_item instanceof L2Armor);
	}
	
	public L2EtcItem getEtcItem()
	{
		if (_item instanceof L2EtcItem)
		{
			return (L2EtcItem) _item;
		}
		return null;
	}
	
	public L2Weapon getWeaponItem()
	{
		if (_item instanceof L2Weapon)
		{
			return (L2Weapon) _item;
		}
		return null;
	}
	
	public L2Armor getArmorItem()
	{
		if (_item instanceof L2Armor)
		{
			return (L2Armor) _item;
		}
		return null;
	}
	
	public final int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}
	
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}
	
	public String getItemName()
	{
		return _item.getName();
	}
	
	public int getReuseDelay()
	{
		return _item.getReuseDelay();
	}
	
	public int getSharedReuseGroup()
	{
		return _item.getSharedReuseGroup();
	}
	
	public int getLastChange()
	{
		return _lastChange;
	}
	
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}
	
	public boolean isStackable()
	{
		return _item.isStackable();
	}
	
	public boolean isDropable()
	{
		return isAugmented() ? false : _item.isDropable();
	}
	
	public boolean isDestroyable()
	{
		return _item.isDestroyable();
	}
	
	public boolean isTradeable()
	{
		return isAugmented() ? false : _item.isTradeable();
	}
	
	public boolean isSellable()
	{
		return isAugmented() ? false : _item.isSellable();
	}
	
	public boolean isDepositable(boolean isPrivateWareHouse)
	{
		if (isEquipped() || !_item.isDepositable())
		{
			return false;
		}
		if (!isPrivateWareHouse)
		{
			if (!isTradeable() || isShadowItem())
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isConsumable()
	{
		return _item.isConsumable();
	}
	
	public boolean isPotion()
	{
		return _item.isPotion();
	}
	
	public boolean isElixir()
	{
		return _item.isElixir();
	}
	
	public boolean isHeroItem()
	{
		return _item.isHeroItem();
	}
	
	public boolean isCommonItem()
	{
		return _item.isCommon();
	}
	
	public boolean isPvp()
	{
		return _item.isPvpItem();
	}
	
	public boolean isOlyRestrictedItem()
	{
		return getItem().isOlyRestrictedItem();
	}
	
	public boolean isAvailable(L2PcInstance player, boolean allowAdena, boolean allowNonTradeable)
	{
		return ((!isEquipped()) && (getItem().getType2() != L2Item.TYPE2_QUEST) && ((getItem().getType2() != L2Item.TYPE2_MONEY) || (getItem().getType1() != L2Item.TYPE1_SHIELD_ARMOR)) && (!player.hasSummon() || (getObjectId() != player.getSummon().getControlObjectId())) && (player.getActiveEnchantItemId() != getObjectId()) && (player.getActiveEnchantSupportItemId() != getObjectId()) && (player.getActiveEnchantAttrItemId() != getObjectId()) && (allowAdena || (getId() != PcInventory.ADENA_ID)) && ((player.getCurrentSkill() == null) || (player.getCurrentSkill().getSkill().getItemConsumeId() != getId())) && (!player.isCastingSimultaneouslyNow() || (player.getLastSimultaneousSkillCast() == null) || (player.getLastSimultaneousSkillCast().getItemConsumeId() != getId())) && (allowNonTradeable || (isTradeable() && (!((getItem().getItemType() == L2EtcItemType.PET_COLLAR) && player.havePetInvItems())))));
	}
	
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
		{
			return;
		}
		clearEnchantStats();
		_enchantLevel = enchantLevel;
		applyEnchantStats();
		_storedInDb = false;
	}
	
	public boolean isAugmented()
	{
		return _augmentation != null;
	}
	
	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}
	
	public boolean setAugmentation(L2Augmentation augmentation)
	{
		if (_augmentation != null)
		{
			_log.info("Warning: Augment set for (" + getObjectId() + ") " + getName() + " owner: " + getOwnerId());
			return false;
		}
		if (!fireAugmentListeners(true, augmentation))
		{
			return false;
		}
		_augmentation = augmentation;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			updateItemAttributes(con);
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "Could not update atributes for item: " + this + " from DB:", e);
		}
		return true;
	}
	
	public void removeAugmentation()
	{
		if (_augmentation == null)
		{
			return;
		}
		if (!fireAugmentListeners(true, _augmentation))
		{
			return;
		}
		_augmentation = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?"))
		{
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not remove augmentation for item: " + this + " from DB:", e);
		}
	}
	
	public void restoreAttributes()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps1 = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?");
			PreparedStatement ps2 = con.prepareStatement("SELECT elemType,elemValue FROM item_elementals WHERE itemId=?"))
		{
			ps1.setInt(1, getObjectId());
			try (ResultSet rs = ps1.executeQuery())
			{
				if (rs.next())
				{
					int aug_attributes = rs.getInt(1);
					if (aug_attributes != -1)
					{
						_augmentation = new L2Augmentation(rs.getInt("augAttributes"));
					}
				}
			}
			
			ps2.setInt(1, getObjectId());
			try (ResultSet rs = ps2.executeQuery())
			{
				while (rs.next())
				{
					byte elem_type = rs.getByte(1);
					int elem_value = rs.getInt(2);
					if ((elem_type != -1) && (elem_value != -1))
					{
						applyAttribute(elem_type, elem_value);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore augmentation and elemental data for item " + this + " from DB: " + e.getMessage(), e);
		}
	}
	
	private void updateItemAttributes(Connection con)
	{
		try (PreparedStatement ps = con.prepareStatement("REPLACE INTO item_attributes VALUES(?,?)"))
		{
			ps.setInt(1, getObjectId());
			ps.setInt(2, _augmentation != null ? _augmentation.getAttributes() : -1);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "Could not update atributes for item: " + this + " from DB:", e);
		}
	}
	
	private void updateItemElements(Connection con)
	{
		try
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
			
			if (_elementals == null)
			{
				return;
			}
			
			statement = con.prepareStatement("INSERT INTO item_elementals VALUES(?,?,?)");
			
			for (Elementals elm : _elementals)
			{
				statement.setInt(1, getObjectId());
				statement.setByte(2, elm.getElement());
				statement.setInt(3, elm.getValue());
				statement.executeUpdate();
				statement.clearParameters();
			}
			
			statement.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "Could not update elementals for item: " + this + " from DB:", e);
		}
	}
	
	public Elementals[] getElementals()
	{
		return _elementals;
	}
	
	public Elementals getElemental(byte attribute)
	{
		if (_elementals == null)
		{
			return null;
		}
		for (Elementals elm : _elementals)
		{
			if (elm.getElement() == attribute)
			{
				return elm;
			}
		}
		return null;
	}
	
	public byte getAttackElementType()
	{
		if (!isWeapon())
		{
			return -2;
		}
		else if (getItem().getElementals() != null)
		{
			return getItem().getElementals()[0].getElement();
		}
		else if (_elementals != null)
		{
			return _elementals[0].getElement();
		}
		return -2;
	}
	
	public int getAttackElementPower()
	{
		if (!isWeapon())
		{
			return 0;
		}
		else if (getItem().getElementals() != null)
		{
			return getItem().getElementals()[0].getValue();
		}
		else if (_elementals != null)
		{
			return _elementals[0].getValue();
		}
		return 0;
	}
	
	public int getElementDefAttr(byte element)
	{
		if (!isArmor())
		{
			return 0;
		}
		else if (getItem().getElementals() != null)
		{
			Elementals elm = getItem().getElemental(element);
			if (elm != null)
			{
				return elm.getValue();
			}
		}
		else if (_elementals != null)
		{
			Elementals elm = getElemental(element);
			if (elm != null)
			{
				return elm.getValue();
			}
		}
		return 0;
	}
	
	private void applyAttribute(byte element, int value)
	{
		if (_elementals == null)
		{
			_elementals = new Elementals[1];
			_elementals[0] = new Elementals(element, value);
		}
		else
		{
			Elementals elm = getElemental(element);
			if (elm != null)
			{
				elm.setValue(value);
			}
			else
			{
				elm = new Elementals(element, value);
				Elementals[] array = new Elementals[_elementals.length + 1];
				System.arraycopy(_elementals, 0, array, 0, _elementals.length);
				array[_elementals.length] = elm;
				_elementals = array;
			}
		}
	}
	
	public void setElementAttr(byte element, int value)
	{
		applyAttribute(element, value);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			updateItemElements(con);
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "Could not update elementals for item: " + this + " from DB:", e);
		}
	}
	
	public void clearElementAttr(byte element)
	{
		if ((getElemental(element) == null) && (element != -1))
		{
			return;
		}
		
		Elementals[] array = null;
		if ((element != -1) && (_elementals != null) && (_elementals.length > 1))
		{
			array = new Elementals[_elementals.length - 1];
			int i = 0;
			for (Elementals elm : _elementals)
			{
				if (elm.getElement() != element)
				{
					array[i++] = elm;
				}
			}
		}
		_elementals = array;
		
		String query = (element != -1) ? "DELETE FROM item_elementals WHERE itemId = ? AND elemType = ?" : "DELETE FROM item_elementals WHERE itemId = ?";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			if (element != -1)
			{
				statement.setInt(2, element);
			}
			
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not remove elemental enchant for item: " + this + " from DB:", e);
		}
	}
	
	public static class ScheduleConsumeManaTask implements Runnable
	{
		private final L2ItemInstance _shadowItem;
		
		public ScheduleConsumeManaTask(L2ItemInstance item)
		{
			_shadowItem = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_shadowItem != null)
				{
					_shadowItem.decreaseMana(true);
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	public boolean isShadowItem()
	{
		return (_mana >= 0);
	}
	
	public int getMana()
	{
		return _mana;
	}
	
	public void decreaseMana(boolean resetConsumingMana)
	{
		decreaseMana(resetConsumingMana, 1);
	}
	
	public void decreaseMana(boolean resetConsumingMana, int count)
	{
		if (!isShadowItem())
		{
			return;
		}
		
		if ((_mana - count) >= 0)
		{
			_mana -= count;
		}
		else
		{
			_mana = 0;
		}
		
		if (_storedInDb)
		{
			_storedInDb = false;
		}
		if (resetConsumingMana)
		{
			_consumingMana = false;
		}
		
		final L2PcInstance player = getActingPlayer();
		if (player != null)
		{
			SystemMessage sm;
			switch (_mana)
			{
				case 10:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10);
					sm.addItemName(_item);
					player.sendPacket(sm);
					break;
				case 5:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5);
					sm.addItemName(_item);
					player.sendPacket(sm);
					break;
				case 1:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1);
					sm.addItemName(_item);
					player.sendPacket(sm);
					break;
			}
			
			if (_mana == 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0);
				sm.addItemName(_item);
				player.sendPacket(sm);
				
				if (isEquipped())
				{
					L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
					InventoryUpdate iu = new InventoryUpdate();
					for (L2ItemInstance item : unequiped)
					{
						item.unChargeAllShots();
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					player.broadcastUserInfo();
				}
				
				if (getItemLocation() != ItemLocation.WAREHOUSE)
				{
					player.getInventory().destroyItem("L2ItemInstance", this, player, null);
					
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);
					
					StatusUpdate su = new StatusUpdate(player);
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
					
				}
				else
				{
					player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
				}
				L2World.getInstance().removeObject(this);
			}
			else
			{
				if (!_consumingMana && isEquipped())
				{
					scheduleConsumeManaTask();
				}
				if (getItemLocation() != ItemLocation.WAREHOUSE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
				}
			}
		}
	}
	
	public void scheduleConsumeManaTask()
	{
		if (_consumingMana)
		{
			return;
		}
		_consumingMana = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	public Func[] getStatFuncs(L2Character player)
	{
		return getItem().getStatFuncs(this, player);
	}
	
	public void updateDatabase()
	{
		this.updateDatabase(false);
	}
	
	public void updateDatabase(boolean force)
	{
		_dbLock.lock();
		
		try
		{
			if (_existsInDb)
			{
				if ((_ownerId == 0) || (_loc == ItemLocation.VOID) || (_loc == ItemLocation.REFUND) || ((getCount() == 0) && (_loc != ItemLocation.LEASE)))
				{
					removeFromDb();
				}
				else if (!Config.LAZY_ITEMS_UPDATE || force)
				{
					updateInDb();
				}
			}
			else
			{
				if ((_ownerId == 0) || (_loc == ItemLocation.VOID) || (_loc == ItemLocation.REFUND) || ((getCount() == 0) && (_loc != ItemLocation.LEASE)))
				{
					return;
				}
				insertIntoDb();
			}
		}
		finally
		{
			_dbLock.unlock();
		}
	}
	
	public static L2ItemInstance restoreFromDb(int ownerId, ResultSet rs)
	{
		L2ItemInstance inst = null;
		int objectId, item_id, loc_data, enchant_level, custom_type1, custom_type2, manaLeft;
		long time, count;
		ItemLocation loc;
		try
		{
			objectId = rs.getInt(1);
			item_id = rs.getInt("item_id");
			count = rs.getLong("count");
			loc = ItemLocation.valueOf(rs.getString("loc"));
			loc_data = rs.getInt("loc_data");
			enchant_level = rs.getInt("enchant_level");
			custom_type1 = rs.getInt("custom_type1");
			custom_type2 = rs.getInt("custom_type2");
			manaLeft = rs.getInt("mana_left");
			time = rs.getLong("time");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore an item owned by " + ownerId + " from DB:", e);
			return null;
		}
		L2Item item = ItemHolder.getInstance().getTemplate(item_id);
		if (item == null)
		{
			_log.severe("Item item_id=" + item_id + " not known, object_id=" + objectId);
			return null;
		}
		inst = new L2ItemInstance(objectId, item);
		inst._ownerId = ownerId;
		inst.setCount(count);
		inst._enchantLevel = enchant_level;
		inst._type1 = custom_type1;
		inst._type2 = custom_type2;
		inst._loc = loc;
		inst._locData = loc_data;
		inst._existsInDb = true;
		inst._storedInDb = true;
		inst._mana = manaLeft;
		inst._time = time;
		
		if (inst.isEquipable())
		{
			inst.restoreAttributes();
		}
		
		return inst;
	}
	
	public class ItemDropTask implements Runnable
	{
		private int _x, _y, _z;
		private final L2Character _dropper;
		private final L2ItemInstance _itm;
		
		public ItemDropTask(L2ItemInstance item, L2Character dropper, int x, int y, int z)
		{
			_x = x;
			_y = y;
			_z = z;
			_dropper = dropper;
			_itm = item;
		}
		
		@Override
		public final void run()
		{
			assert _itm.getPosition().getWorldRegion() == null;
			
			if (Config.GEODATA)
			{
				_z = GeoClient.getInstance().getSpawnHeight(_x, _y, _z);
			}
			
			if ((Config.GEODATA) && (_dropper != null))
			{
				Location dropDest = GeoClient.getInstance().moveCheck(_dropper, new Location(_x, _y, _z), true);
				if (dropDest != null)
				{
					_x = dropDest.getX();
					_y = dropDest.getY();
					_z = dropDest.getZ();
				}
				else
				{
					_x = _dropper.getX();
					_y = _dropper.getY();
					_z = _dropper.getZ();
				}
			}
			
			if ((_dropper != null) && InstanceManager.getInstance().instanceExist(_dropper.getInstanceId()))
			{
				setInstanceId(_dropper.getInstanceId());
			}
			else
			{
				setInstanceId(0);
			}
			
			synchronized (_itm)
			{
				_itm.setIsVisible(true);
				_itm.getPosition().setWorldPosition(_x, _y, _z);
				_itm.getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
			}
			
			_itm.getPosition().getWorldRegion().addVisibleObject(_itm);
			_itm.setDropTime(System.currentTimeMillis());
			_itm.setDropperObjectId(_dropper != null ? _dropper.getObjectId() : 0);
			
			L2World.getInstance().addVisibleObject(_itm, _itm.getPosition().getWorldRegion());
			if (Config.SAVE_DROPPED_ITEM)
			{
				ItemsOnGroundManager.getInstance().save(_itm);
			}
			_itm.setDropperObjectId(0);
		}
	}
	
	public final void dropMe(L2Character dropper, int x, int y, int z)
	{
		if (!fireDropListeners(dropper, new Location(x, y, z)))
		{
			return;
		}
		ThreadPoolManager.getInstance().executeTask(new ItemDropTask(this, dropper, x, y, z));
	}
	
	private void updateInDb()
	{
		assert _existsInDb;
		
		if (_wear)
		{
			return;
		}
		
		if (_storedInDb)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=? " + "WHERE object_id = ?"))
		{
			statement.setInt(1, _ownerId);
			statement.setLong(2, getCount());
			statement.setString(3, _loc.name());
			statement.setInt(4, _locData);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, getCustomType1());
			statement.setInt(7, getCustomType2());
			statement.setInt(8, getMana());
			statement.setLong(9, getTime());
			statement.setInt(10, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not update item " + this + " in DB: Reason: " + e.getMessage(), e);
		}
	}
	
	private void insertIntoDb()
	{
		assert !_existsInDb && (getObjectId() != 0);
		
		if (_wear)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?)"))
		{
			statement.setInt(1, _ownerId);
			statement.setInt(2, _itemId);
			statement.setLong(3, getCount());
			statement.setString(4, _loc.name());
			statement.setInt(5, _locData);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, getObjectId());
			statement.setInt(8, _type1);
			statement.setInt(9, _type2);
			statement.setInt(10, getMana());
			statement.setLong(11, getTime());
			
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			
			if (_augmentation != null)
			{
				updateItemAttributes(con);
			}
			if (_elementals != null)
			{
				updateItemElements(con);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not insert item " + this + " into DB: Reason: " + e.getMessage(), e);
		}
	}
	
	private void removeFromDb()
	{
		assert _existsInDb;
		
		if (_wear)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE object_id = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not delete item " + this + " in DB: " + e.getMessage(), e);
		}
	}
	
	@Override
	public String toString()
	{
		return _item + "[" + getObjectId() + "]";
	}
	
	public void resetOwnerTimer()
	{
		if (itemLootShedule != null)
		{
			itemLootShedule.cancel(true);
		}
		itemLootShedule = null;
	}
	
	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		itemLootShedule = sf;
	}
	
	public ScheduledFuture<?> getItemLootShedule()
	{
		return itemLootShedule;
	}
	
	public void setProtected(boolean isProtected)
	{
		_protected = isProtected;
	}
	
	public boolean isProtected()
	{
		return _protected;
	}
	
	public boolean isNightLure()
	{
		return (((_itemId >= 8505) && (_itemId <= 8513)) || (_itemId == 8485));
	}
	
	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}
	
	public boolean getCountDecrease()
	{
		return _decrease;
	}
	
	public void setInitCount(int InitCount)
	{
		_initCount = InitCount;
	}
	
	public long getInitCount()
	{
		return _initCount;
	}
	
	public void restoreInitCount()
	{
		if (_decrease)
		{
			setCount(_initCount);
		}
	}
	
	public boolean isTimeLimitedItem()
	{
		return (_time > 0);
	}
	
	public long getTime()
	{
		return _time;
	}
	
	public long getRemainingTime()
	{
		return _time - System.currentTimeMillis();
	}
	
	public void endOfLife()
	{
		L2PcInstance player = getActingPlayer();
		if (player != null)
		{
			if (isEquipped())
			{
				L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
				InventoryUpdate iu = new InventoryUpdate();
				for (L2ItemInstance item : unequiped)
				{
					item.unChargeAllShots();
					iu.addModifiedItem(item);
				}
				player.sendPacket(iu);
				player.broadcastUserInfo();
			}
			
			if (getItemLocation() != ItemLocation.WAREHOUSE)
			{
				player.getInventory().destroyItem("L2ItemInstance", this, player, null);
				
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(this);
				player.sendPacket(iu);
				
				StatusUpdate su = new StatusUpdate(player);
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
				
			}
			else
			{
				player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
			}
			player.sendPacket(SystemMessageId.TIME_LIMITED_ITEM_DELETED);
			
			L2World.getInstance().removeObject(this);
		}
	}
	
	public void scheduleLifeTimeTask()
	{
		if (!isTimeLimitedItem())
		{
			return;
		}
		if (getRemainingTime() <= 0)
		{
			endOfLife();
		}
		else
		{
			if (_lifeTimeTask != null)
			{
				_lifeTimeTask.cancel(false);
			}
			_lifeTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleLifeTimeTask(this), getRemainingTime());
		}
	}
	
	public static class ScheduleLifeTimeTask implements Runnable
	{
		private final L2ItemInstance _limitedItem;
		
		public ScheduleLifeTimeTask(L2ItemInstance item)
		{
			_limitedItem = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_limitedItem != null)
				{
					_limitedItem.endOfLife();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	public void updateElementAttrBonus(L2PcInstance player)
	{
		if (_elementals == null)
		{
			return;
		}
		for (Elementals elm : _elementals)
		{
			elm.updateBonus(player, isArmor());
		}
	}
	
	public void removeElementAttrBonus(L2PcInstance player)
	{
		if (_elementals == null)
		{
			return;
		}
		for (Elementals elm : _elementals)
		{
			elm.removeBonus(player);
		}
	}
	
	public void setDropperObjectId(int id)
	{
		_dropperObjectId = id;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (_dropperObjectId != 0)
		{
			activeChar.sendPacket(new DropItem(this, _dropperObjectId));
		}
		else
		{
			activeChar.sendPacket(new SpawnItem(this));
		}
	}
	
	public final DropProtection getDropProtection()
	{
		return _dropProtection;
	}
	
	public boolean isPublished()
	{
		return _published;
	}
	
	public void publish()
	{
		_published = true;
	}
	
	@Override
	public void decayMe()
	{
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		super.decayMe();
	}
	
	public boolean isQuestItem()
	{
		return getItem().isQuestItem();
	}
	
	public boolean isElementable()
	{
		if ((getItemLocation() == ItemLocation.INVENTORY) || (getItemLocation() == ItemLocation.PAPERDOLL))
		{
			return getItem().isElementable();
		}
		return false;
	}
	
	public boolean isFreightable()
	{
		return getItem().isFreightable();
	}
	
	public int useSkillDisTime()
	{
		return getItem().useSkillDisTime();
	}
	
	public int getOlyEnchantLevel()
	{
		L2PcInstance player = getActingPlayer();
		int enchant = getEnchantLevel();
		
		if (player == null)
		{
			return enchant;
		}
		
		if (player.isInOlympiadMode() && (Config.ALT_OLY_ENCHANT_LIMIT >= 0) && (enchant > Config.ALT_OLY_ENCHANT_LIMIT))
		{
			enchant = Config.ALT_OLY_ENCHANT_LIMIT;
		}
		
		return enchant;
	}
	
	public int getDefaultEnchantLevel()
	{
		return _item.getDefaultEnchantLevel();
	}
	
	public boolean hasPassiveSkills()
	{
		return (getItemType() == L2EtcItemType.RUNE) && (getItemLocation() == ItemLocation.INVENTORY) && (getOwnerId() > 0) && getItem().hasSkills();
	}
	
	public void giveSkillsToOwner()
	{
		if (!hasPassiveSkills())
		{
			return;
		}
		
		L2PcInstance player = getActingPlayer();
		
		if (player != null)
		{
			for (SkillsHolder sh : getItem().getSkills())
			{
				if (sh.getSkill().isPassive())
				{
					player.addSkill(sh.getSkill(), false);
				}
			}
		}
	}
	
	public void removeSkillsFromOwner()
	{
		if (!hasPassiveSkills())
		{
			return;
		}
		
		L2PcInstance player = getActingPlayer();
		
		if (player != null)
		{
			for (SkillsHolder sh : getItem().getSkills())
			{
				if (sh.getSkill().isPassive())
				{
					player.removeSkill(sh.getSkill(), false, true);
				}
			}
		}
	}
	
	@Override
	public boolean isItem()
	{
		return true;
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return L2World.getInstance().getPlayer(getOwnerId());
	}
	
	public int getEquipReuseDelay()
	{
		return _item.getEquipReuseDelay();
	}
	
	public void onBypassFeedback(L2PcInstance activeChar, String command)
	{
		if (command.startsWith("Quest"))
		{
			String questName = command.substring(6);
			String content = null;
			
			String event = null;
			int idx = questName.indexOf(' ');
			if (idx > 0)
			{
				event = questName.substring(idx).trim();
				questName = questName.substring(0, idx);
			}
			
			Quest q = QuestManager.getInstance().getQuest(questName);
			QuestState qs = activeChar.getQuestState(questName);
			
			if (q != null)
			{
				if (((q.getId() >= 1) && (q.getId() < 20000)) && ((activeChar.getWeightPenalty() >= 3) || !activeChar.isInventoryUnder90(true)))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
					return;
				}
				
				if (qs == null)
				{
					if ((q.getId() >= 1) && (q.getId() < 20000))
					{
						if (activeChar.getAllActiveQuests().length > 40)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TOO_MANY_QUESTS));
							return;
						}
					}
					qs = q.newQuestState(activeChar);
				}
			}
			else
			{
				content = Quest.getNoQuestMsg(activeChar);
			}
			
			if (qs != null)
			{
				if ((event != null) && !qs.getQuest().notifyItemEvent(this, activeChar, event))
				{
					return;
				}
				else if (!qs.getQuest().notifyItemTalk(this, activeChar))
				{
					return;
				}
				questName = qs.getQuest().getName();
				String stateId = State.getStateName(qs.getState());
				String path = "data/scripts/quests/" + questName + "/" + stateId + ".htm";
				content = HtmCache.getInstance().getHtm(activeChar.getLang(), path);
			}
			
			if (content != null)
			{
				showChatWindow(activeChar, content);
			}
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public void showChatWindow(L2PcInstance activeChar, String content)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0, getId());
		html.setHtml(content);
		html.replace("%itemId%", String.valueOf(getObjectId()));
		activeChar.sendPacket(html);
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
		{
			_shotsMask |= type.getMask();
		}
		else
		{
			_shotsMask &= ~type.getMask();
		}
	}
	
	public void unChargeAllShots()
	{
		_shotsMask = 0;
	}
	
	public int[] getEnchantOptions()
	{
		EnchantOptions op = EnchantItemOptionsParser.getInstance().getOptions(this);
		if (op != null)
		{
			return op.getOptions();
		}
		return DEFAULT_ENCHANT_OPTIONS;
	}
	
	public void clearEnchantStats()
	{
		final L2PcInstance player = getActingPlayer();
		if (player == null)
		{
			_enchantOptions.clear();
			return;
		}
		
		for (Options op : _enchantOptions)
		{
			op.remove(player);
		}
		_enchantOptions.clear();
	}
	
	public void applyEnchantStats()
	{
		final L2PcInstance player = getActingPlayer();
		if (!isEquipped() || (player == null) || (getEnchantOptions() == DEFAULT_ENCHANT_OPTIONS))
		{
			return;
		}
		
		for (int id : getEnchantOptions())
		{
			final Options options = OptionsParser.getInstance().getOptions(id);
			if (options != null)
			{
				options.apply(player);
				_enchantOptions.add(options);
			}
			else if (id != 0)
			{
				_log.log(Level.INFO, "applyEnchantStats: Couldn't find option: " + id);
			}
		}
	}
	
	private boolean firePickupListeners(L2PcInstance actor)
	{
		if (!dropListeners.isEmpty() && (actor != null))
		{
			ItemPickupEvent event = new ItemPickupEvent();
			event.setItem(this);
			event.setPicker(actor);
			event.setLocation(new Location(getPosition().getX(), getPosition().getY(), getPosition().getZ()));
			for (DropListener listener : dropListeners)
			{
				if (!listener.onPickup(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean fireAugmentListeners(boolean isAugment, L2Augmentation augmentation)
	{
		if (!augmentListeners.isEmpty() && (augmentation != null))
		{
			AugmentEvent event = new AugmentEvent();
			event.setAugmentation(augmentation);
			event.setIsAugment(isAugment);
			event.setItem(this);
			for (AugmentListener listener : augmentListeners)
			{
				if (isAugment)
				{
					if (!listener.onAugment(event))
					{
						return false;
					}
				}
				else
				{
					if (!listener.onRemoveAugment(event))
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean fireDropListeners(L2Character dropper, Location loc)
	{
		if (!dropListeners.isEmpty() && (dropper != null))
		{
			ItemDropEvent event = new ItemDropEvent();
			event.setDropper(dropper);
			event.setItem(this);
			event.setLocation(loc);
			for (DropListener listener : dropListeners)
			{
				if (!listener.onDrop(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public static void addAugmentListener(AugmentListener listener)
	{
		if (!augmentListeners.contains(listener))
		{
			augmentListeners.add(listener);
		}
	}
	
	public static void removeAugmentListener(AugmentListener listener)
	{
		augmentListeners.remove(listener);
	}
	
	public static void addDropListener(DropListener listener)
	{
		if (!dropListeners.contains(listener))
		{
			dropListeners.add(listener);
		}
	}
	
	public static void removeDropListener(DropListener listener)
	{
		dropListeners.remove(listener);
	}
	
	public void deleteMe()
	{
		if ((_lifeTimeTask != null) && !_lifeTimeTask.isDone())
		{
			_lifeTimeTask.cancel(false);
			_lifeTimeTask = null;
		}
	}
}