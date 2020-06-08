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

import java.util.Arrays;
import java.util.Map;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Armor;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public abstract class AbstractRefinePacket extends L2GameClientPacket
{
	public static final int GRADE_NONE = 0;
	public static final int GRADE_MID = 1;
	public static final int GRADE_HIGH = 2;
	public static final int GRADE_TOP = 3;
	public static final int GRADE_ACC = 4;
	
	protected static final int GEMSTONE_D = 2130;
	protected static final int GEMSTONE_C = 2131;
	protected static final int GEMSTONE_B = 2132;
	
	private static final Map<Integer, LifeStone> _lifeStones = new FastMap<>();
	
	protected static final class LifeStone
	{
		private static final int[] LEVELS =
		{
			46,
			49,
			52,
			55,
			58,
			61,
			64,
			67,
			70,
			76,
			80,
			82,
			84,
			85
		};
		private final int _grade;
		private final int _level;
		
		public LifeStone(int grade, int level)
		{
			_grade = grade;
			_level = level;
		}
		
		public final int getLevel()
		{
			return _level;
		}
		
		public final int getGrade()
		{
			return _grade;
		}
		
		public final int getPlayerLevel()
		{
			return LEVELS[_level];
		}
	}
	
	static
	{
		_lifeStones.put(8723, new LifeStone(GRADE_NONE, 0));
		_lifeStones.put(8724, new LifeStone(GRADE_NONE, 1));
		_lifeStones.put(8725, new LifeStone(GRADE_NONE, 2));
		_lifeStones.put(8726, new LifeStone(GRADE_NONE, 3));
		_lifeStones.put(8727, new LifeStone(GRADE_NONE, 4));
		_lifeStones.put(8728, new LifeStone(GRADE_NONE, 5));
		_lifeStones.put(8729, new LifeStone(GRADE_NONE, 6));
		_lifeStones.put(8730, new LifeStone(GRADE_NONE, 7));
		_lifeStones.put(8731, new LifeStone(GRADE_NONE, 8));
		_lifeStones.put(8732, new LifeStone(GRADE_NONE, 9));
		
		_lifeStones.put(8733, new LifeStone(GRADE_MID, 0));
		_lifeStones.put(8734, new LifeStone(GRADE_MID, 1));
		_lifeStones.put(8735, new LifeStone(GRADE_MID, 2));
		_lifeStones.put(8736, new LifeStone(GRADE_MID, 3));
		_lifeStones.put(8737, new LifeStone(GRADE_MID, 4));
		_lifeStones.put(8738, new LifeStone(GRADE_MID, 5));
		_lifeStones.put(8739, new LifeStone(GRADE_MID, 6));
		_lifeStones.put(8740, new LifeStone(GRADE_MID, 7));
		_lifeStones.put(8741, new LifeStone(GRADE_MID, 8));
		_lifeStones.put(8742, new LifeStone(GRADE_MID, 9));
		
		_lifeStones.put(8743, new LifeStone(GRADE_HIGH, 0));
		_lifeStones.put(8744, new LifeStone(GRADE_HIGH, 1));
		_lifeStones.put(8745, new LifeStone(GRADE_HIGH, 2));
		_lifeStones.put(8746, new LifeStone(GRADE_HIGH, 3));
		_lifeStones.put(8747, new LifeStone(GRADE_HIGH, 4));
		_lifeStones.put(8748, new LifeStone(GRADE_HIGH, 5));
		_lifeStones.put(8749, new LifeStone(GRADE_HIGH, 6));
		_lifeStones.put(8750, new LifeStone(GRADE_HIGH, 7));
		_lifeStones.put(8751, new LifeStone(GRADE_HIGH, 8));
		_lifeStones.put(8752, new LifeStone(GRADE_HIGH, 9));
		
		_lifeStones.put(8753, new LifeStone(GRADE_TOP, 0));
		_lifeStones.put(8754, new LifeStone(GRADE_TOP, 1));
		_lifeStones.put(8755, new LifeStone(GRADE_TOP, 2));
		_lifeStones.put(8756, new LifeStone(GRADE_TOP, 3));
		_lifeStones.put(8757, new LifeStone(GRADE_TOP, 4));
		_lifeStones.put(8758, new LifeStone(GRADE_TOP, 5));
		_lifeStones.put(8759, new LifeStone(GRADE_TOP, 6));
		_lifeStones.put(8760, new LifeStone(GRADE_TOP, 7));
		_lifeStones.put(8761, new LifeStone(GRADE_TOP, 8));
		_lifeStones.put(8762, new LifeStone(GRADE_TOP, 9));
		
		_lifeStones.put(9573, new LifeStone(GRADE_NONE, 10));
		_lifeStones.put(9574, new LifeStone(GRADE_MID, 10));
		_lifeStones.put(9575, new LifeStone(GRADE_HIGH, 10));
		_lifeStones.put(9576, new LifeStone(GRADE_TOP, 10));
		
		_lifeStones.put(10483, new LifeStone(GRADE_NONE, 11));
		_lifeStones.put(10484, new LifeStone(GRADE_MID, 11));
		_lifeStones.put(10485, new LifeStone(GRADE_HIGH, 11));
		_lifeStones.put(10486, new LifeStone(GRADE_TOP, 11));
		
		_lifeStones.put(12754, new LifeStone(GRADE_ACC, 0));
		_lifeStones.put(12755, new LifeStone(GRADE_ACC, 1));
		_lifeStones.put(12756, new LifeStone(GRADE_ACC, 2));
		_lifeStones.put(12757, new LifeStone(GRADE_ACC, 3));
		_lifeStones.put(12758, new LifeStone(GRADE_ACC, 4));
		_lifeStones.put(12759, new LifeStone(GRADE_ACC, 5));
		_lifeStones.put(12760, new LifeStone(GRADE_ACC, 6));
		_lifeStones.put(12761, new LifeStone(GRADE_ACC, 7));
		_lifeStones.put(12762, new LifeStone(GRADE_ACC, 8));
		_lifeStones.put(12763, new LifeStone(GRADE_ACC, 9));
		
		_lifeStones.put(12821, new LifeStone(GRADE_ACC, 10));
		_lifeStones.put(12822, new LifeStone(GRADE_ACC, 11));
		
		_lifeStones.put(12840, new LifeStone(GRADE_ACC, 0));
		_lifeStones.put(12841, new LifeStone(GRADE_ACC, 1));
		_lifeStones.put(12842, new LifeStone(GRADE_ACC, 2));
		_lifeStones.put(12843, new LifeStone(GRADE_ACC, 3));
		_lifeStones.put(12844, new LifeStone(GRADE_ACC, 4));
		_lifeStones.put(12845, new LifeStone(GRADE_ACC, 5));
		_lifeStones.put(12846, new LifeStone(GRADE_ACC, 6));
		_lifeStones.put(12847, new LifeStone(GRADE_ACC, 7));
		_lifeStones.put(12848, new LifeStone(GRADE_ACC, 8));
		_lifeStones.put(12849, new LifeStone(GRADE_ACC, 9));
		_lifeStones.put(12850, new LifeStone(GRADE_ACC, 10));
		_lifeStones.put(12851, new LifeStone(GRADE_ACC, 11));
		
		_lifeStones.put(14008, new LifeStone(GRADE_ACC, 12));
		
		_lifeStones.put(14166, new LifeStone(GRADE_NONE, 12));
		_lifeStones.put(14167, new LifeStone(GRADE_MID, 12));
		_lifeStones.put(14168, new LifeStone(GRADE_HIGH, 12));
		_lifeStones.put(14169, new LifeStone(GRADE_TOP, 12));
		
		_lifeStones.put(16160, new LifeStone(GRADE_NONE, 13));
		_lifeStones.put(16161, new LifeStone(GRADE_MID, 13));
		_lifeStones.put(16162, new LifeStone(GRADE_HIGH, 13));
		_lifeStones.put(16163, new LifeStone(GRADE_TOP, 13));
		_lifeStones.put(16177, new LifeStone(GRADE_ACC, 13));
		
		_lifeStones.put(16164, new LifeStone(GRADE_NONE, 13));
		_lifeStones.put(16165, new LifeStone(GRADE_MID, 13));
		_lifeStones.put(16166, new LifeStone(GRADE_HIGH, 13));
		_lifeStones.put(16167, new LifeStone(GRADE_TOP, 13));
		_lifeStones.put(16178, new LifeStone(GRADE_ACC, 13));
	}
	
	protected static final LifeStone getLifeStone(int itemId)
	{
		return _lifeStones.get(itemId);
	}
	
	protected static final boolean isValid(L2PcInstance player, L2ItemInstance item, L2ItemInstance refinerItem, L2ItemInstance gemStones)
	{
		if (!isValid(player, item, refinerItem))
		{
			return false;
		}
		
		if (gemStones.getOwnerId() != player.getObjectId())
		{
			return false;
		}
		
		if (gemStones.getItemLocation() != L2ItemInstance.ItemLocation.INVENTORY)
		{
			return false;
		}
		
		final int grade = item.getItem().getItemGrade();
		final LifeStone ls = getLifeStone(refinerItem.getId());
		
		if (getGemStoneId(grade) != gemStones.getId())
		{
			return false;
		}
		
		if (getGemStoneCount(grade, ls.getGrade()) > gemStones.getCount())
		{
			return false;
		}
		
		return true;
	}
	
	protected static final boolean isValid(L2PcInstance player, L2ItemInstance item, L2ItemInstance refinerItem)
	{
		if (!isValid(player, item))
		{
			return false;
		}
		
		if (refinerItem.getOwnerId() != player.getObjectId())
		{
			return false;
		}
		
		if (refinerItem.getItemLocation() != L2ItemInstance.ItemLocation.INVENTORY)
		{
			return false;
		}
		
		final LifeStone ls = getLifeStone(refinerItem.getId());
		if (ls == null)
		{
			return false;
		}
		
		if ((item.getItem() instanceof L2Weapon) && (ls.getGrade() == GRADE_ACC))
		{
			return false;
		}
		
		if ((item.getItem() instanceof L2Armor) && (ls.getGrade() != GRADE_ACC))
		{
			return false;
		}
		
		if (player.getLevel() < ls.getPlayerLevel())
		{
			return false;
		}
		
		return true;
	}
	
	protected static final boolean isValid(L2PcInstance player, L2ItemInstance item)
	{
		if (!isValid(player))
		{
			return false;
		}
		
		if (item.getOwnerId() != player.getObjectId())
		{
			return false;
		}
		if (item.isAugmented())
		{
			return false;
		}
		if (item.isHeroItem())
		{
			return false;
		}
		if (item.isShadowItem())
		{
			return false;
		}
		if (item.isCommonItem())
		{
			return false;
		}
		if (item.isEtcItem())
		{
			return false;
		}
		if (item.isTimeLimitedItem())
		{
			return false;
		}
		if (item.isPvp() && !Config.ALT_ALLOW_AUGMENT_PVP_ITEMS)
		{
			return false;
		}
		if (item.getItem().getCrystalType() < L2Item.CRYSTAL_C)
		{
			return false;
		}
		
		switch (item.getItemLocation())
		{
			case INVENTORY:
			case PAPERDOLL:
				break;
			default:
				return false;
		}
		
		if (item.getItem() instanceof L2Weapon)
		{
			switch (((L2Weapon) item.getItem()).getItemType())
			{
				case NONE:
				case FISHINGROD:
					return false;
				default:
					break;
			}
		}
		else if (item.getItem() instanceof L2Armor)
		{
			switch (item.getItem().getBodyPart())
			{
				case L2Item.SLOT_LR_FINGER:
				case L2Item.SLOT_LR_EAR:
				case L2Item.SLOT_NECK:
					break;
				default:
					return false;
			}
		}
		else
		{
			return false;
		}
		
		if (Arrays.binarySearch(Config.AUGMENTATION_BLACKLIST, item.getId()) >= 0)
		{
			return false;
		}
		
		return true;
	}
	
	protected static final boolean isValid(L2PcInstance player)
	{
		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return false;
		}
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING);
			return false;
		}
		if (player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return false;
		}
		if (player.isParalyzed())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return false;
		}
		if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return false;
		}
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
			return false;
		}
		if (player.isCursedWeaponEquipped())
		{
			return false;
		}
		if (player.isEnchanting() || player.isProcessingTransaction())
		{
			return false;
		}
		
		return true;
	}
	
	protected static final int getGemStoneId(int itemGrade)
	{
		switch (itemGrade)
		{
			case L2Item.CRYSTAL_C:
			case L2Item.CRYSTAL_B:
				return GEMSTONE_D;
			case L2Item.CRYSTAL_A:
			case L2Item.CRYSTAL_S:
				return GEMSTONE_C;
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				return GEMSTONE_B;
			default:
				return 0;
		}
	}
	
	protected static final int getGemStoneCount(int itemGrade, int lifeStoneGrade)
	{
		switch (lifeStoneGrade)
		{
			case GRADE_ACC:
				switch (itemGrade)
				{
					case L2Item.CRYSTAL_C:
						return 200;
					case L2Item.CRYSTAL_B:
						return 300;
					case L2Item.CRYSTAL_A:
						return 200;
					case L2Item.CRYSTAL_S:
						return 250;
					case L2Item.CRYSTAL_S80:
						return 360;
					case L2Item.CRYSTAL_S84:
						return 480;
					default:
						return 0;
				}
			default:
				switch (itemGrade)
				{
					case L2Item.CRYSTAL_C:
						return 20;
					case L2Item.CRYSTAL_B:
						return 30;
					case L2Item.CRYSTAL_A:
						return 20;
					case L2Item.CRYSTAL_S:
						return 25;
					case L2Item.CRYSTAL_S80:
					case L2Item.CRYSTAL_S84:
						return 36;
					default:
						return 0;
				}
		}
	}
}