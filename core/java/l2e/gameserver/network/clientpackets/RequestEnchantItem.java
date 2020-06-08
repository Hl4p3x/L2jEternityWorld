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
package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.EnchantItemParser;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.enchant.EnchantItem;
import l2e.gameserver.model.enchant.EnchantResultType;
import l2e.gameserver.model.enchant.EnchantScroll;
import l2e.gameserver.model.items.L2Armor;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.EnchantResult;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

public final class RequestEnchantItem extends L2GameClientPacket
{
	protected static final Logger _logEnchant = Logger.getLogger("enchant");
	
	private int _objectId = 0;
	private int _supportId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_supportId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if ((activeChar == null) || (_objectId == 0))
		{
			return;
		}
		
		if (!activeChar.isOnline() || getClient().isDetached())
		{
			activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
			return;
		}
		
		if (activeChar.isProcessingTransaction() || activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
			activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance scroll = activeChar.getInventory().getItemByObjectId(activeChar.getActiveEnchantItemId());
		L2ItemInstance support = activeChar.getInventory().getItemByObjectId(activeChar.getActiveEnchantSupportItemId());
		
		if ((item == null) || (scroll == null))
		{
			activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
			return;
		}
		
		EnchantScroll scrollTemplate = EnchantItemParser.getInstance().getEnchantScroll(scroll);
		
		if (scrollTemplate == null)
		{
			return;
		}
		
		EnchantItem supportTemplate = null;
		if (support != null)
		{
			if (support.getObjectId() != _supportId)
			{
				activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
				return;
			}
			supportTemplate = EnchantItemParser.getInstance().getSupportItem(support);
		}
		
		if (!scrollTemplate.isValid(item, supportTemplate))
		{
			activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
			activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
			activeChar.sendPacket(new EnchantResult(2, 0, 0));
			return;
		}
		
		if ((activeChar.getActiveEnchantTimestamp() == 0) || ((System.currentTimeMillis() - activeChar.getActiveEnchantTimestamp()) < 2000))
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " use autoenchant program ", Config.DEFAULT_PUNISH);
			activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
			activeChar.sendPacket(new EnchantResult(2, 0, 0));
			return;
		}
		
		scroll = activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item);
		if (scroll == null)
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a scroll he doesn't have", Config.DEFAULT_PUNISH);
			activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
			activeChar.sendPacket(new EnchantResult(2, 0, 0));
			return;
		}
		
		if (support != null)
		{
			support = activeChar.getInventory().destroyItem("Enchant", support.getObjectId(), 1, activeChar, item);
			if (support == null)
			{
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a support item he doesn't have", Config.DEFAULT_PUNISH);
				activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
				activeChar.sendPacket(new EnchantResult(2, 0, 0));
				return;
			}
		}
		
		synchronized (item)
		{
			if ((item.getOwnerId() != activeChar.getObjectId()) || (item.isEnchantable() == 0))
			{
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
				activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
				activeChar.sendPacket(new EnchantResult(2, 0, 0));
				return;
			}
			
			final EnchantResultType resultType = scrollTemplate.calculateSuccess(activeChar, item, supportTemplate);
			switch (resultType)
			{
				case ERROR:
				{
					activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
					activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
					activeChar.sendPacket(new EnchantResult(2, 0, 0));
					break;
				}
				case SUCCESS:
				{
					L2Skill enchant4Skill = null;
					L2Item it = item.getItem();
					
					item.setEnchantLevel(item.getEnchantLevel() + 1);
					item.updateDatabase();
					activeChar.sendPacket(new EnchantResult(0, 0, 0));
					
					if (Config.LOG_ITEM_ENCHANTS)
					{
						LogRecord record = new LogRecord(Level.INFO, "Success");
						record.setParameters(new Object[]
						{
							activeChar,
							item,
							scroll,
							support,
						});
						record.setLoggerName("item");
						_logEnchant.log(record);
					}
					
					int minEnchantAnnounce = item.isArmor() ? 6 : 7;
					int maxEnchantAnnounce = item.isArmor() ? 0 : 15;
					if ((item.getEnchantLevel() == minEnchantAnnounce) || (item.getEnchantLevel() == maxEnchantAnnounce))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_SUCCESSFULY_ENCHANTED_A_S2_S3);
						sm.addCharName(activeChar);
						sm.addNumber(item.getEnchantLevel());
						sm.addItemName(item);
						activeChar.broadcastPacket(sm);
						
						L2Skill skill = SkillHolder.FrequentSkill.FIREWORK.getSkill();
						if (skill != null)
						{
							activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
						}
					}
					
					if ((it instanceof L2Armor) && (item.getEnchantLevel() == 4) && activeChar.getInventory().getItemByObjectId(item.getObjectId()).isEquipped())
					{
						enchant4Skill = ((L2Armor) it).getEnchant4Skill();
						if (enchant4Skill != null)
						{
							activeChar.addSkill(enchant4Skill, false);
							activeChar.sendSkillList();
						}
					}
					break;
				}
				case FAILURE:
				{
					if (scrollTemplate.isSafe())
					{
						activeChar.sendPacket(SystemMessageId.SAFE_ENCHANT_FAILED);
						activeChar.sendPacket(new EnchantResult(5, 0, 0));
						
						if (Config.LOG_ITEM_ENCHANTS)
						{
							LogRecord record = new LogRecord(Level.INFO, "Safe Fail");
							record.setParameters(new Object[]
							{
								activeChar,
								item,
								scroll,
								support,
							});
							record.setLoggerName("item");
							_logEnchant.log(record);
						}
					}
					else
					{
						if (item.isEquipped())
						{
							if (item.getEnchantLevel() > 0)
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
								sm.addNumber(item.getEnchantLevel());
								sm.addItemName(item);
								activeChar.sendPacket(sm);
							}
							else
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
								sm.addItemName(item);
								activeChar.sendPacket(sm);
							}
							
							L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
							InventoryUpdate iu = new InventoryUpdate();
							for (L2ItemInstance itm : unequiped)
							{
								iu.addModifiedItem(itm);
							}
							
							activeChar.sendPacket(iu);
							activeChar.broadcastUserInfo();
						}
						
						if (scrollTemplate.isBlessed())
						{
							activeChar.sendPacket(SystemMessageId.BLESSED_ENCHANT_FAILED);
							
							if (Config.SYSTEM_BLESSED_ENCHANT)
							{
								item.setEnchantLevel(Config.BLESSED_ENCHANT_SAVE);
							}
							else
							{
								item.setEnchantLevel(0);
								item.updateDatabase();
								activeChar.sendPacket(new EnchantResult(3, 0, 0));
							}
							
							if (Config.LOG_ITEM_ENCHANTS)
							{
								LogRecord record = new LogRecord(Level.INFO, "Blessed Fail");
								record.setParameters(new Object[]
								{
									activeChar,
									item,
									scroll,
									support,
								});
								record.setLoggerName("item");
								_logEnchant.log(record);
							}
						}
						else
						{
							int crystalId = item.getItem().getCrystalItemId();
							int count = item.getCrystalCount() - ((item.getItem().getCrystalCount() + 1) / 2);
							if (count < 1)
							{
								count = 1;
							}
							
							L2ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
							if (destroyItem == null)
							{
								Util.handleIllegalPlayerAction(activeChar, "Unable to delete item on enchant failure from player " + activeChar.getName() + ", possible cheater !", Config.DEFAULT_PUNISH);
								activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
								activeChar.sendPacket(new EnchantResult(2, 0, 0));
								
								if (Config.LOG_ITEM_ENCHANTS)
								{
									LogRecord record = new LogRecord(Level.INFO, "Unable to destroy");
									record.setParameters(new Object[]
									{
										activeChar,
										item,
										scroll,
										support,
									});
									record.setLoggerName("item");
									_logEnchant.log(record);
								}
								return;
							}
							
							L2ItemInstance crystals = null;
							if (crystalId != 0)
							{
								crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, destroyItem);
								
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
								sm.addItemName(crystals);
								sm.addItemNumber(count);
								activeChar.sendPacket(sm);
							}
							
							if (!Config.FORCE_INVENTORY_UPDATE)
							{
								InventoryUpdate iu = new InventoryUpdate();
								if (destroyItem.getCount() == 0)
								{
									iu.addRemovedItem(destroyItem);
								}
								else
								{
									iu.addModifiedItem(destroyItem);
								}
								
								if (crystals != null)
								{
									iu.addItem(crystals);
								}
								
								if (scroll.getCount() == 0)
								{
									iu.addRemovedItem(scroll);
								}
								else
								{
									iu.addModifiedItem(scroll);
								}
								
								activeChar.sendPacket(iu);
							}
							else
							{
								activeChar.sendPacket(new ItemList(activeChar, true));
							}
							
							L2World world = L2World.getInstance();
							world.removeObject(destroyItem);
							if (crystalId == 0)
							{
								activeChar.sendPacket(new EnchantResult(4, 0, 0));
							}
							else
							{
								activeChar.sendPacket(new EnchantResult(1, crystalId, count));
							}
							
							if (Config.LOG_ITEM_ENCHANTS)
							{
								LogRecord record = new LogRecord(Level.INFO, "Fail");
								record.setParameters(new Object[]
								{
									activeChar,
									item,
									scroll,
									support,
								});
								record.setLoggerName("item");
								_logEnchant.log(record);
							}
						}
					}
					break;
				}
			}
			
			final StatusUpdate su = new StatusUpdate(activeChar);
			su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
			activeChar.sendPacket(su);
			
			activeChar.sendPacket(new ItemList(activeChar, false));
			activeChar.broadcastUserInfo();
			activeChar.setActiveEnchantItemId(L2PcInstance.ID_NONE);
		}
	}
}