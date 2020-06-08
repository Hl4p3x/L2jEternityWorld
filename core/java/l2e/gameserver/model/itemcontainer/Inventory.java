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
package l2e.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.xml.ArmorSetsParser;
import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.handler.SkillHandler;
import l2e.gameserver.model.L2ArmorSet;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance.ItemLocation;
import l2e.gameserver.model.items.type.L2EtcItemType;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.serverpackets.SkillCoolTime;
import l2e.util.StringUtil;

public abstract class Inventory extends ItemContainer
{
	protected static final Logger _log = Logger.getLogger(Inventory.class.getName());
	
	public interface PaperdollListener
	{
		public void notifyEquiped(int slot, L2ItemInstance inst, Inventory inventory);
		
		public void notifyUnequiped(int slot, L2ItemInstance inst, Inventory inventory);
	}
	
	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_HEAD = 1;
	public static final int PAPERDOLL_HAIR = 2;
	public static final int PAPERDOLL_HAIR2 = 3;
	public static final int PAPERDOLL_NECK = 4;
	public static final int PAPERDOLL_RHAND = 5;
	public static final int PAPERDOLL_CHEST = 6;
	public static final int PAPERDOLL_LHAND = 7;
	public static final int PAPERDOLL_REAR = 8;
	public static final int PAPERDOLL_LEAR = 9;
	public static final int PAPERDOLL_GLOVES = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_RFINGER = 13;
	public static final int PAPERDOLL_LFINGER = 14;
	public static final int PAPERDOLL_LBRACELET = 15;
	public static final int PAPERDOLL_RBRACELET = 16;
	public static final int PAPERDOLL_DECO1 = 17;
	public static final int PAPERDOLL_DECO2 = 18;
	public static final int PAPERDOLL_DECO3 = 19;
	public static final int PAPERDOLL_DECO4 = 20;
	public static final int PAPERDOLL_DECO5 = 21;
	public static final int PAPERDOLL_DECO6 = 22;
	public static final int PAPERDOLL_CLOAK = 23;
	public static final int PAPERDOLL_BELT = 24;
	public static final int PAPERDOLL_TOTALSLOTS = 25;
	
	public static final double MAX_ARMOR_WEIGHT = 12000;
	
	private final L2ItemInstance[] _paperdoll;
	private final List<PaperdollListener> _paperdollListeners;
	
	protected int _totalWeight;
	
	private int _wearedMask;
	
	private static final class ChangeRecorder implements PaperdollListener
	{
		private final Inventory _inventory;
		private final List<L2ItemInstance> _changed;
		
		ChangeRecorder(Inventory inventory)
		{
			_inventory = inventory;
			_changed = new FastList<>();
			_inventory.addPaperdollListener(this);
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}
		}
		
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}
		}
		
		public L2ItemInstance[] getChangedItems()
		{
			return _changed.toArray(new L2ItemInstance[_changed.size()]);
		}
	}
	
	private static final class BowCrossRodListener implements PaperdollListener
	{
		private static BowCrossRodListener instance = new BowCrossRodListener();
		
		public static BowCrossRodListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (slot != PAPERDOLL_RHAND)
			{
				return;
			}
			
			if (item.getItemType() == L2WeaponType.BOW)
			{
				L2ItemInstance arrow = inventory.getPaperdollItem(PAPERDOLL_LHAND);
				
				if (arrow != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
			else if (item.getItemType() == L2WeaponType.CROSSBOW)
			{
				L2ItemInstance bolts = inventory.getPaperdollItem(PAPERDOLL_LHAND);
				
				if (bolts != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
			else if (item.getItemType() == L2WeaponType.FISHINGROD)
			{
				L2ItemInstance lure = inventory.getPaperdollItem(PAPERDOLL_LHAND);
				
				if (lure != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (slot != PAPERDOLL_RHAND)
			{
				return;
			}
			
			if (item.getItemType() == L2WeaponType.BOW)
			{
				L2ItemInstance arrow = inventory.findArrowForBow(item.getItem());
				
				if (arrow != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, arrow);
				}
			}
			else if (item.getItemType() == L2WeaponType.CROSSBOW)
			{
				L2ItemInstance bolts = inventory.findBoltForCrossBow(item.getItem());
				
				if (bolts != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, bolts);
				}
			}
		}
	}
	
	private static final class StatsListener implements PaperdollListener
	{
		private static StatsListener instance = new StatsListener();
		
		public static StatsListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			inventory.getOwner().removeStatsOwner(item);
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			inventory.getOwner().addStatFuncs(item.getStatFuncs(inventory.getOwner()));
		}
	}
	
	private static final class ItemSkillsListener implements PaperdollListener
	{
		private static ItemSkillsListener instance = new ItemSkillsListener();
		
		public static ItemSkillsListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!(inventory.getOwner().isPlayer()))
			{
				return;
			}
			final L2PcInstance player = (L2PcInstance) inventory.getOwner();
			
			L2Skill enchant4Skill, itemSkill;
			L2Item it = item.getItem();
			boolean update = false;
			boolean updateTimeStamp = false;
			
			if (item.isAugmented())
			{
				item.getAugmentation().removeBonus(player);
			}
			
			item.removeElementAttrBonus(player);
			
			if (item.getEnchantLevel() >= 4)
			{
				enchant4Skill = it.getEnchant4Skill();
				
				if (enchant4Skill != null)
				{
					player.removeSkill(enchant4Skill, false, enchant4Skill.isPassive());
					update = true;
				}
			}
			
			item.clearEnchantStats();
			
			final SkillsHolder[] skills = it.getSkills();
			
			if (skills != null)
			{
				for (SkillsHolder skillInfo : skills)
				{
					if (skillInfo == null)
					{
						continue;
					}
					
					itemSkill = skillInfo.getSkill();
					
					if (itemSkill != null)
					{
						player.removeSkill(itemSkill, false, itemSkill.isPassive());
						update = true;
					}
					else
					{
						_log.warning("Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + ".");
					}
				}
			}
			
			if (item.isArmor())
			{
				for (L2ItemInstance itm : inventory.getItems())
				{
					if (!itm.isEquipped() || (itm.getItem().getSkills() == null))
					{
						continue;
					}
					for (SkillsHolder sk : itm.getItem().getSkills())
					{
						if (player.getSkillLevel(sk.getSkillId()) != -1)
						{
							continue;
						}
						
						itemSkill = sk.getSkill();
						
						if (itemSkill != null)
						{
							player.addSkill(itemSkill, false);
							
							if (itemSkill.isActive())
							{
								if (!player.hasSkillReuse(itemSkill.getReuseHashCode()))
								{
									int equipDelay = item.getEquipReuseDelay();
									if (equipDelay > 0)
									{
										player.addTimeStamp(itemSkill, equipDelay);
										player.disableSkill(itemSkill, equipDelay);
									}
								}
								updateTimeStamp = true;
							}
							update = true;
						}
					}
				}
			}
			
			L2Skill unequipSkill = it.getUnequipSkill();
			if (unequipSkill != null)
			{
				ISkillHandler handler = SkillHandler.getInstance().getHandler(unequipSkill.getSkillType());
				L2PcInstance[] targets =
				{
					player
				};
				
				if (handler != null)
				{
					handler.useSkill(player, unequipSkill, targets);
				}
				else
				{
					unequipSkill.useSkill(player, targets);
				}
			}
			
			if (update)
			{
				player.sendSkillList();
				
				if (updateTimeStamp)
				{
					player.sendPacket(new SkillCoolTime(player));
				}
			}
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!(inventory.getOwner().isPlayer()))
			{
				return;
			}
			
			final L2PcInstance player = (L2PcInstance) inventory.getOwner();
			
			L2Skill enchant4Skill, itemSkill;
			L2Item it = item.getItem();
			boolean update = false;
			boolean updateTimeStamp = false;
			
			if (item.isAugmented())
			{
				item.getAugmentation().applyBonus(player);
			}
			
			item.updateElementAttrBonus(player);
			
			if (item.getEnchantLevel() >= 4)
			{
				enchant4Skill = it.getEnchant4Skill();
				
				if (enchant4Skill != null)
				{
					player.addSkill(enchant4Skill, false);
					update = true;
				}
			}
			
			item.applyEnchantStats();
			
			final SkillsHolder[] skills = it.getSkills();
			
			if (skills != null)
			{
				for (SkillsHolder skillInfo : skills)
				{
					if (skillInfo == null)
					{
						continue;
					}
					
					itemSkill = skillInfo.getSkill();
					
					if (itemSkill != null)
					{
						player.addSkill(itemSkill, false);
						
						if (itemSkill.isActive())
						{
							if (!player.hasSkillReuse(itemSkill.getReuseHashCode()))
							{
								int equipDelay = item.getEquipReuseDelay();
								if (equipDelay > 0)
								{
									player.addTimeStamp(itemSkill, equipDelay);
									player.disableSkill(itemSkill, equipDelay);
								}
							}
							updateTimeStamp = true;
						}
						update = true;
					}
					else
					{
						_log.warning("Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + ".");
					}
				}
			}
			
			if (update)
			{
				player.sendSkillList();
				
				if (updateTimeStamp)
				{
					player.sendPacket(new SkillCoolTime(player));
				}
			}
		}
	}
	
	private static final class ArmorSetListener implements PaperdollListener
	{
		private static ArmorSetListener instance = new ArmorSetListener();
		
		public static ArmorSetListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!(inventory.getOwner().isPlayer()))
			{
				return;
			}
			
			final L2PcInstance player = (L2PcInstance) inventory.getOwner();
			
			final L2ItemInstance chestItem = inventory.getPaperdollItem(PAPERDOLL_CHEST);
			
			if (chestItem == null)
			{
				return;
			}
			
			if (!ArmorSetsParser.getInstance().isArmorSet(chestItem.getId()))
			{
				return;
			}
			final L2ArmorSet armorSet = ArmorSetsParser.getInstance().getSet(chestItem.getId());
			boolean update = false;
			boolean updateTimeStamp = false;
			
			if (armorSet.containItem(slot, item.getId()))
			{
				if (armorSet.containAll(player))
				{
					L2Skill itemSkill;
					final List<SkillsHolder> skills = armorSet.getSkills();
					
					if (skills != null)
					{
						for (SkillsHolder holder : skills)
						{
							
							itemSkill = holder.getSkill();
							if (itemSkill != null)
							{
								player.addSkill(itemSkill, false);
								
								if (itemSkill.isActive())
								{
									if (!player.hasSkillReuse(itemSkill.getReuseHashCode()))
									{
										int equipDelay = item.getEquipReuseDelay();
										if (equipDelay > 0)
										{
											player.addTimeStamp(itemSkill, equipDelay);
											player.disableSkill(itemSkill, equipDelay);
										}
									}
									updateTimeStamp = true;
								}
								update = true;
							}
							else
							{
								_log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
							}
						}
					}
					
					if (armorSet.containShield(player))
					{
						for (SkillsHolder holder : armorSet.getShieldSkillId())
						{
							if (holder.getSkill() != null)
							{
								player.addSkill(holder.getSkill(), false);
								update = true;
							}
							else
							{
								_log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
							}
						}
					}
					
					if (armorSet.isEnchanted6(player))
					{
						for (SkillsHolder holder : armorSet.getEnchant6skillId())
						{
							if (holder.getSkill() != null)
							{
								player.addSkill(holder.getSkill(), false);
								update = true;
							}
							else
							{
								_log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
							}
						}
					}
				}
			}
			else if (armorSet.containShield(item.getId()))
			{
				for (SkillsHolder holder : armorSet.getShieldSkillId())
				{
					if (holder.getSkill() != null)
					{
						player.addSkill(holder.getSkill(), false);
						update = true;
					}
					else
					{
						_log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
					}
				}
			}
			
			if (update)
			{
				player.sendSkillList();
				
				if (updateTimeStamp)
				{
					player.sendPacket(new SkillCoolTime(player));
				}
			}
		}
		
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!(inventory.getOwner().isPlayer()))
			{
				return;
			}
			
			final L2PcInstance player = (L2PcInstance) inventory.getOwner();
			
			boolean remove = false;
			L2Skill itemSkill;
			List<SkillsHolder> skills = null;
			List<SkillsHolder> shieldSkill = null;
			List<SkillsHolder> skillId6 = null;
			
			if (slot == PAPERDOLL_CHEST)
			{
				if (!ArmorSetsParser.getInstance().isArmorSet(item.getId()))
				{
					return;
				}
				final L2ArmorSet armorSet = ArmorSetsParser.getInstance().getSet(item.getId());
				remove = true;
				skills = armorSet.getSkills();
				shieldSkill = armorSet.getShieldSkillId();
				skillId6 = armorSet.getEnchant6skillId();
			}
			else
			{
				L2ItemInstance chestItem = inventory.getPaperdollItem(PAPERDOLL_CHEST);
				if (chestItem == null)
				{
					return;
				}
				
				L2ArmorSet armorSet = ArmorSetsParser.getInstance().getSet(chestItem.getId());
				if (armorSet == null)
				{
					return;
				}
				
				if (armorSet.containItem(slot, item.getId()))
				{
					remove = true;
					skills = armorSet.getSkills();
					shieldSkill = armorSet.getShieldSkillId();
					skillId6 = armorSet.getEnchant6skillId();
				}
				else if (armorSet.containShield(item.getId()))
				{
					remove = true;
					shieldSkill = armorSet.getShieldSkillId();
				}
			}
			
			if (remove)
			{
				if (skills != null)
				{
					for (SkillsHolder holder : skills)
					{
						itemSkill = holder.getSkill();
						if (itemSkill != null)
						{
							player.removeSkill(itemSkill, false, itemSkill.isPassive());
						}
						else
						{
							_log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
						}
					}
				}
				
				if (shieldSkill != null)
				{
					for (SkillsHolder holder : shieldSkill)
					{
						itemSkill = holder.getSkill();
						if (itemSkill != null)
						{
							player.removeSkill(itemSkill, false, itemSkill.isPassive());
						}
						else
						{
							_log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
						}
					}
				}
				
				if (skillId6 != null)
				{
					for (SkillsHolder holder : skillId6)
					{
						itemSkill = holder.getSkill();
						if (itemSkill != null)
						{
							player.removeSkill(itemSkill, false, itemSkill.isPassive());
						}
						else
						{
							_log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
						}
					}
				}
				
				player.checkItemRestriction();
				player.sendSkillList();
			}
		}
	}
	
	private static final class BraceletListener implements PaperdollListener
	{
		private static BraceletListener instance = new BraceletListener();
		
		public static BraceletListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (item.getItem().getBodyPart() == L2Item.SLOT_R_BRACELET)
			{
				inventory.unEquipItemInSlot(PAPERDOLL_DECO1);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO2);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO3);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO4);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO5);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO6);
			}
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
		}
	}
	
	protected Inventory()
	{
		_paperdoll = new L2ItemInstance[PAPERDOLL_TOTALSLOTS];
		_paperdollListeners = new ArrayList<>();
		
		if (this instanceof PcInventory)
		{
			addPaperdollListener(ArmorSetListener.getInstance());
			addPaperdollListener(BowCrossRodListener.getInstance());
			addPaperdollListener(ItemSkillsListener.getInstance());
			addPaperdollListener(BraceletListener.getInstance());
		}
		addPaperdollListener(StatsListener.getInstance());
		
	}
	
	protected abstract ItemLocation getEquipLocation();
	
	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}
	
	public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		if (item == null)
		{
			return null;
		}
		
		synchronized (item)
		{
			if (!_items.contains(item))
			{
				return null;
			}
			
			removeItem(item);
			item.setOwnerId(process, 0, actor, reference);
			item.setItemLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);
			
			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}
	
	public L2ItemInstance dropItem(String process, int objectId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		
		synchronized (item)
		{
			if (!_items.contains(item))
			{
				return null;
			}
			
			if (item.getCount() > count)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(L2ItemInstance.MODIFIED);
				item.updateDatabase();
				
				item = ItemHolder.getInstance().createItem(process, item.getId(), count, actor, reference);
				item.updateDatabase();
				refreshWeight();
				return item;
			}
		}
		return dropItem(process, item, actor, reference);
	}
	
	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		if (item.isEquipped())
		{
			equipItem(item);
		}
	}
	
	@Override
	protected boolean removeItem(L2ItemInstance item)
	{
		for (int i = 0; i < _paperdoll.length; i++)
		{
			if (_paperdoll[i] == item)
			{
				unEquipItemInSlot(i);
			}
		}
		return super.removeItem(item);
	}
	
	public L2ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}
	
	public boolean isPaperdollSlotEmpty(int slot)
	{
		return _paperdoll[slot] == null;
	}
	
	public static int getPaperdollIndex(int slot)
	{
		switch (slot)
		{
			case L2Item.SLOT_UNDERWEAR:
				return PAPERDOLL_UNDER;
			case L2Item.SLOT_R_EAR:
				return PAPERDOLL_REAR;
			case L2Item.SLOT_LR_EAR:
			case L2Item.SLOT_L_EAR:
				return PAPERDOLL_LEAR;
			case L2Item.SLOT_NECK:
				return PAPERDOLL_NECK;
			case L2Item.SLOT_R_FINGER:
			case L2Item.SLOT_LR_FINGER:
				return PAPERDOLL_RFINGER;
			case L2Item.SLOT_L_FINGER:
				return PAPERDOLL_LFINGER;
			case L2Item.SLOT_HEAD:
				return PAPERDOLL_HEAD;
			case L2Item.SLOT_R_HAND:
			case L2Item.SLOT_LR_HAND:
				return PAPERDOLL_RHAND;
			case L2Item.SLOT_L_HAND:
				return PAPERDOLL_LHAND;
			case L2Item.SLOT_GLOVES:
				return PAPERDOLL_GLOVES;
			case L2Item.SLOT_CHEST:
			case L2Item.SLOT_FULL_ARMOR:
			case L2Item.SLOT_ALLDRESS:
				return PAPERDOLL_CHEST;
			case L2Item.SLOT_LEGS:
				return PAPERDOLL_LEGS;
			case L2Item.SLOT_FEET:
				return PAPERDOLL_FEET;
			case L2Item.SLOT_BACK:
				return PAPERDOLL_CLOAK;
			case L2Item.SLOT_HAIR:
			case L2Item.SLOT_HAIRALL:
				return PAPERDOLL_HAIR;
			case L2Item.SLOT_HAIR2:
				return PAPERDOLL_HAIR2;
			case L2Item.SLOT_R_BRACELET:
				return PAPERDOLL_RBRACELET;
			case L2Item.SLOT_L_BRACELET:
				return PAPERDOLL_LBRACELET;
			case L2Item.SLOT_DECO:
				return PAPERDOLL_DECO1;
			case L2Item.SLOT_BELT:
				return PAPERDOLL_BELT;
		}
		return -1;
	}
	
	public L2ItemInstance getPaperdollItemByL2ItemId(int slot)
	{
		int index = getPaperdollIndex(slot);
		if (index == -1)
		{
			return null;
		}
		return _paperdoll[index];
	}
	
	public int getPaperdollItemId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			return item.getId();
		}
		return 0;
	}
	
	public int getPaperdollItemDisplayId(int slot)
	{
		final L2ItemInstance item = _paperdoll[slot];
		return (item != null) ? item.getDisplayId() : 0;
	}
	
	public int getPaperdollAugmentationId(int slot)
	{
		final L2ItemInstance item = _paperdoll[slot];
		return ((item != null) && (item.getAugmentation() != null)) ? item.getAugmentation().getAugmentationId() : 0;
	}
	
	public int getPaperdollObjectId(int slot)
	{
		final L2ItemInstance item = _paperdoll[slot];
		return (item != null) ? item.getObjectId() : 0;
	}
	
	public synchronized void addPaperdollListener(PaperdollListener listener)
	{
		assert !_paperdollListeners.contains(listener);
		_paperdollListeners.add(listener);
	}
	
	public synchronized void removePaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}
	
	public synchronized L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item)
	{
		L2ItemInstance old = _paperdoll[slot];
		if (old != item)
		{
			if (old != null)
			{
				_paperdoll[slot] = null;
				
				old.setItemLocation(getBaseLocation());
				old.setLastChange(L2ItemInstance.MODIFIED);
				
				int mask = 0;
				for (int i = 0; i < PAPERDOLL_TOTALSLOTS; i++)
				{
					L2ItemInstance pi = _paperdoll[i];
					if (pi != null)
					{
						mask |= pi.getItem().getItemMask();
					}
				}
				_wearedMask = mask;
				
				for (PaperdollListener listener : _paperdollListeners)
				{
					if (listener == null)
					{
						continue;
					}
					
					listener.notifyUnequiped(slot, old, this);
				}
				old.updateDatabase();
			}
			
			if (item != null)
			{
				_paperdoll[slot] = item;
				item.setItemLocation(getEquipLocation(), slot);
				item.setLastChange(L2ItemInstance.MODIFIED);
				_wearedMask |= item.getItem().getItemMask();
				for (PaperdollListener listener : _paperdollListeners)
				{
					if (listener == null)
					{
						continue;
					}
					
					listener.notifyEquiped(slot, item, this);
				}
				item.updateDatabase();
			}
		}
		return old;
	}
	
	public int getWearedMask()
	{
		return _wearedMask;
	}
	
	public int getSlotFromItem(L2ItemInstance item)
	{
		int slot = -1;
		final int location = item.getLocationSlot();
		switch (location)
		{
			case PAPERDOLL_UNDER:
				slot = L2Item.SLOT_UNDERWEAR;
				break;
			case PAPERDOLL_LEAR:
				slot = L2Item.SLOT_L_EAR;
				break;
			case PAPERDOLL_REAR:
				slot = L2Item.SLOT_R_EAR;
				break;
			case PAPERDOLL_NECK:
				slot = L2Item.SLOT_NECK;
				break;
			case PAPERDOLL_RFINGER:
				slot = L2Item.SLOT_R_FINGER;
				break;
			case PAPERDOLL_LFINGER:
				slot = L2Item.SLOT_L_FINGER;
				break;
			case PAPERDOLL_HAIR:
				slot = L2Item.SLOT_HAIR;
				break;
			case PAPERDOLL_HAIR2:
				slot = L2Item.SLOT_HAIR2;
				break;
			case PAPERDOLL_HEAD:
				slot = L2Item.SLOT_HEAD;
				break;
			case PAPERDOLL_RHAND:
				slot = L2Item.SLOT_R_HAND;
				break;
			case PAPERDOLL_LHAND:
				slot = L2Item.SLOT_L_HAND;
				break;
			case PAPERDOLL_GLOVES:
				slot = L2Item.SLOT_GLOVES;
				break;
			case PAPERDOLL_CHEST:
				slot = item.getItem().getBodyPart();
				break;
			case PAPERDOLL_LEGS:
				slot = L2Item.SLOT_LEGS;
				break;
			case PAPERDOLL_CLOAK:
				slot = L2Item.SLOT_BACK;
				break;
			case PAPERDOLL_FEET:
				slot = L2Item.SLOT_FEET;
				break;
			case PAPERDOLL_LBRACELET:
				slot = L2Item.SLOT_L_BRACELET;
				break;
			case PAPERDOLL_RBRACELET:
				slot = L2Item.SLOT_R_BRACELET;
				break;
			case PAPERDOLL_DECO1:
			case PAPERDOLL_DECO2:
			case PAPERDOLL_DECO3:
			case PAPERDOLL_DECO4:
			case PAPERDOLL_DECO5:
			case PAPERDOLL_DECO6:
				slot = L2Item.SLOT_DECO;
				break;
			case PAPERDOLL_BELT:
				slot = L2Item.SLOT_BELT;
				break;
		}
		return slot;
	}
	
	public L2ItemInstance[] unEquipItemInBodySlotAndRecord(int slot)
	{
		Inventory.ChangeRecorder recorder = newRecorder();
		
		try
		{
			unEquipItemInBodySlot(slot);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	public L2ItemInstance unEquipItemInSlot(int pdollSlot)
	{
		return setPaperdollItem(pdollSlot, null);
	}
	
	public L2ItemInstance[] unEquipItemInSlotAndRecord(int slot)
	{
		Inventory.ChangeRecorder recorder = newRecorder();
		
		try
		{
			unEquipItemInSlot(slot);
			if (getOwner().isPlayer())
			{
				((L2PcInstance) getOwner()).refreshExpertisePenalty();
			}
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	public L2ItemInstance unEquipItemInBodySlot(int slot)
	{
		if (Config.DEBUG)
		{
			_log.info(Inventory.class.getSimpleName() + ": Unequip body slot:" + slot);
		}
		
		int pdollSlot = -1;
		
		switch (slot)
		{
			case L2Item.SLOT_L_EAR:
				pdollSlot = PAPERDOLL_LEAR;
				break;
			case L2Item.SLOT_R_EAR:
				pdollSlot = PAPERDOLL_REAR;
				break;
			case L2Item.SLOT_NECK:
				pdollSlot = PAPERDOLL_NECK;
				break;
			case L2Item.SLOT_R_FINGER:
				pdollSlot = PAPERDOLL_RFINGER;
				break;
			case L2Item.SLOT_L_FINGER:
				pdollSlot = PAPERDOLL_LFINGER;
				break;
			case L2Item.SLOT_HAIR:
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case L2Item.SLOT_HAIR2:
				pdollSlot = PAPERDOLL_HAIR2;
				break;
			case L2Item.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_HAIR, null);
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case L2Item.SLOT_HEAD:
				pdollSlot = PAPERDOLL_HEAD;
				break;
			case L2Item.SLOT_R_HAND:
			case L2Item.SLOT_LR_HAND:
				pdollSlot = PAPERDOLL_RHAND;
				break;
			case L2Item.SLOT_L_HAND:
				pdollSlot = PAPERDOLL_LHAND;
				break;
			case L2Item.SLOT_GLOVES:
				pdollSlot = PAPERDOLL_GLOVES;
				break;
			case L2Item.SLOT_CHEST:
			case L2Item.SLOT_ALLDRESS:
			case L2Item.SLOT_FULL_ARMOR:
				pdollSlot = PAPERDOLL_CHEST;
				break;
			case L2Item.SLOT_LEGS:
				pdollSlot = PAPERDOLL_LEGS;
				break;
			case L2Item.SLOT_BACK:
				pdollSlot = PAPERDOLL_CLOAK;
				break;
			case L2Item.SLOT_FEET:
				pdollSlot = PAPERDOLL_FEET;
				break;
			case L2Item.SLOT_UNDERWEAR:
				pdollSlot = PAPERDOLL_UNDER;
				break;
			case L2Item.SLOT_L_BRACELET:
				pdollSlot = PAPERDOLL_LBRACELET;
				break;
			case L2Item.SLOT_R_BRACELET:
				pdollSlot = PAPERDOLL_RBRACELET;
				break;
			case L2Item.SLOT_DECO:
				pdollSlot = PAPERDOLL_DECO1;
				break;
			case L2Item.SLOT_BELT:
				pdollSlot = PAPERDOLL_BELT;
				break;
			default:
				_log.info("Unhandled slot type: " + slot);
				_log.info(StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
		}
		if (pdollSlot >= 0)
		{
			L2ItemInstance old = setPaperdollItem(pdollSlot, null);
			if (old != null)
			{
				if (getOwner().isPlayer())
				{
					((L2PcInstance) getOwner()).refreshExpertisePenalty();
				}
			}
			return old;
		}
		return null;
	}
	
	public L2ItemInstance[] equipItemAndRecord(L2ItemInstance item)
	{
		Inventory.ChangeRecorder recorder = newRecorder();
		
		try
		{
			equipItem(item);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	public void equipItem(L2ItemInstance item)
	{
		if ((getOwner().isPlayer()) && (((L2PcInstance) getOwner()).getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE))
		{
			return;
		}
		
		if (getOwner().isPlayer())
		{
			L2PcInstance player = (L2PcInstance) getOwner();
			
			if (!player.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !player.isHero() && item.isHeroItem())
			{
				return;
			}
		}
		
		int targetSlot = item.getItem().getBodyPart();
		
		L2ItemInstance formal = getPaperdollItem(PAPERDOLL_CHEST);
		if ((item.getId() != 21163) && (formal != null) && (formal.getItem().getBodyPart() == L2Item.SLOT_ALLDRESS))
		{
			switch (targetSlot)
			{
				case L2Item.SLOT_LR_HAND:
				case L2Item.SLOT_L_HAND:
				case L2Item.SLOT_R_HAND:
				case L2Item.SLOT_LEGS:
				case L2Item.SLOT_FEET:
				case L2Item.SLOT_GLOVES:
				case L2Item.SLOT_HEAD:
					return;
			}
		}
		
		switch (targetSlot)
		{
			case L2Item.SLOT_LR_HAND:
			{
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case L2Item.SLOT_L_HAND:
			{
				L2ItemInstance rh = getPaperdollItem(PAPERDOLL_RHAND);
				if ((rh != null) && (rh.getItem().getBodyPart() == L2Item.SLOT_LR_HAND) && !(((rh.getItemType() == L2WeaponType.BOW) && (item.getItemType() == L2EtcItemType.ARROW)) || ((rh.getItemType() == L2WeaponType.CROSSBOW) && (item.getItemType() == L2EtcItemType.BOLT)) || ((rh.getItemType() == L2WeaponType.FISHINGROD) && (item.getItemType() == L2EtcItemType.LURE))))
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				
				setPaperdollItem(PAPERDOLL_LHAND, item);
				break;
			}
			case L2Item.SLOT_R_HAND:
			{
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case L2Item.SLOT_L_EAR:
			case L2Item.SLOT_R_EAR:
			case L2Item.SLOT_LR_EAR:
			{
				if (_paperdoll[PAPERDOLL_LEAR] == null)
				{
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				else if (_paperdoll[PAPERDOLL_REAR] == null)
				{
					setPaperdollItem(PAPERDOLL_REAR, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				break;
			}
			case L2Item.SLOT_L_FINGER:
			case L2Item.SLOT_R_FINGER:
			case L2Item.SLOT_LR_FINGER:
			{
				if (_paperdoll[PAPERDOLL_LFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				else if (_paperdoll[PAPERDOLL_RFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_RFINGER, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				break;
			}
			case L2Item.SLOT_NECK:
				setPaperdollItem(PAPERDOLL_NECK, item);
				break;
			case L2Item.SLOT_FULL_ARMOR:
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case L2Item.SLOT_CHEST:
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case L2Item.SLOT_LEGS:
			{
				L2ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
				if ((chest != null) && (chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR))
				{
					setPaperdollItem(PAPERDOLL_CHEST, null);
				}
				
				setPaperdollItem(PAPERDOLL_LEGS, item);
				break;
			}
			case L2Item.SLOT_FEET:
				setPaperdollItem(PAPERDOLL_FEET, item);
				break;
			case L2Item.SLOT_GLOVES:
				setPaperdollItem(PAPERDOLL_GLOVES, item);
				break;
			case L2Item.SLOT_HEAD:
				setPaperdollItem(PAPERDOLL_HEAD, item);
				break;
			case L2Item.SLOT_HAIR:
				L2ItemInstance hair = getPaperdollItem(PAPERDOLL_HAIR);
				if ((hair != null) && (hair.getItem().getBodyPart() == L2Item.SLOT_HAIRALL))
				{
					setPaperdollItem(PAPERDOLL_HAIR2, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
				}
				
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;
			case L2Item.SLOT_HAIR2:
				L2ItemInstance hair2 = getPaperdollItem(PAPERDOLL_HAIR);
				if ((hair2 != null) && (hair2.getItem().getBodyPart() == L2Item.SLOT_HAIRALL))
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_HAIR2, null);
				}
				
				setPaperdollItem(PAPERDOLL_HAIR2, item);
				break;
			case L2Item.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_HAIR2, null);
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;
			case L2Item.SLOT_UNDERWEAR:
				setPaperdollItem(PAPERDOLL_UNDER, item);
				break;
			case L2Item.SLOT_BACK:
				setPaperdollItem(PAPERDOLL_CLOAK, item);
				break;
			case L2Item.SLOT_L_BRACELET:
				setPaperdollItem(PAPERDOLL_LBRACELET, item);
				break;
			case L2Item.SLOT_R_BRACELET:
				setPaperdollItem(PAPERDOLL_RBRACELET, item);
				break;
			case L2Item.SLOT_DECO:
				equipTalisman(item);
				break;
			case L2Item.SLOT_BELT:
				setPaperdollItem(PAPERDOLL_BELT, item);
				break;
			case L2Item.SLOT_ALLDRESS:
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, null);
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			default:
				_log.warning("Unknown body slot " + targetSlot + " for Item ID:" + item.getId());
		}
	}
	
	@Override
	protected void refreshWeight()
	{
		long weight = 0;
		
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && (item.getItem() != null))
			{
				weight += item.getItem().getWeight() * item.getCount();
			}
		}
		_totalWeight = (int) Math.min(weight, Integer.MAX_VALUE);
	}
	
	public int getTotalWeight()
	{
		return _totalWeight;
	}
	
	public L2ItemInstance findArrowForBow(L2Item bow)
	{
		if (bow == null)
		{
			return null;
		}
		
		L2ItemInstance arrow = null;
		
		for (L2ItemInstance item : getItems())
		{
			if (item.isEtcItem() && (item.getItem().getItemGradeSPlus() == bow.getItemGradeSPlus()) && (item.getEtcItem().getItemType() == L2EtcItemType.ARROW))
			{
				arrow = item;
				break;
			}
		}
		return arrow;
	}
	
	public L2ItemInstance findBoltForCrossBow(L2Item crossbow)
	{
		L2ItemInstance bolt = null;
		
		for (L2ItemInstance item : getItems())
		{
			if (item.isEtcItem() && (item.getItem().getItemGradeSPlus() == crossbow.getItemGradeSPlus()) && (item.getEtcItem().getItemType() == L2EtcItemType.BOLT))
			{
				bolt = item;
				break;
			}
		}
		return bolt;
	}
	
	@Override
	public void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			statement.setString(3, getEquipLocation().name());
			ResultSet inv = statement.executeQuery();
			
			L2ItemInstance item;
			while (inv.next())
			{
				item = L2ItemInstance.restoreFromDb(getOwnerId(), inv);
				if (item == null)
				{
					continue;
				}
				
				if (getOwner().isPlayer())
				{
					L2PcInstance player = (L2PcInstance) getOwner();
					
					if (!player.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !player.isHero() && item.isHeroItem())
					{
						item.setItemLocation(ItemLocation.INVENTORY);
					}
				}
				
				L2World.getInstance().storeObject(item);
				
				if (item.isStackable() && (getItemByItemId(item.getId()) != null))
				{
					addItem("Restore", item, getOwner().getActingPlayer(), null);
				}
				else
				{
					addItem(item);
				}
			}
			inv.close();
			statement.close();
			refreshWeight();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore inventory: " + e.getMessage(), e);
		}
	}
	
	public int getMaxTalismanCount()
	{
		return (int) getOwner().getStat().calcStat(Stats.TALISMAN_SLOTS, 0, null, null);
	}
	
	private void equipTalisman(L2ItemInstance item)
	{
		if (getMaxTalismanCount() == 0)
		{
			return;
		}
		
		for (int i = PAPERDOLL_DECO1; i < (PAPERDOLL_DECO1 + getMaxTalismanCount()); i++)
		{
			if (_paperdoll[i] != null)
			{
				if (getPaperdollItemId(i) == item.getId())
				{
					setPaperdollItem(i, item);
					return;
				}
			}
		}
		
		for (int i = PAPERDOLL_DECO1; i < (PAPERDOLL_DECO1 + getMaxTalismanCount()); i++)
		{
			if (_paperdoll[i] == null)
			{
				setPaperdollItem(i, item);
				return;
			}
		}
		setPaperdollItem(PAPERDOLL_DECO1, item);
	}
	
	public int getCloakStatus()
	{
		return (int) getOwner().getStat().calcStat(Stats.CLOAK_SLOT, 0, null, null);
	}
	
	public void reloadEquippedItems()
	{
		int slot;
		
		for (L2ItemInstance item : _paperdoll)
		{
			if (item == null)
			{
				continue;
			}
			
			slot = item.getLocationSlot();
			
			for (PaperdollListener listener : _paperdollListeners)
			{
				if (listener == null)
				{
					continue;
				}
				
				listener.notifyUnequiped(slot, item, this);
				listener.notifyEquiped(slot, item, this);
			}
		}
	}
}