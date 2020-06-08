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
package l2e.gameserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.xml.RecipeParser;
import l2e.gameserver.model.L2ManufactureItem;
import l2e.gameserver.model.L2RecipeInstance;
import l2e.gameserver.model.L2RecipeList;
import l2e.gameserver.model.L2RecipeStatInstance;
import l2e.gameserver.model.TempItem;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.RecipeBookItemList;
import l2e.gameserver.network.serverpackets.RecipeItemMakeInfo;
import l2e.gameserver.network.serverpackets.RecipeShopItemInfo;
import l2e.gameserver.network.serverpackets.SetupGauge;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

public class RecipeController
{
	protected static final FastMap<Integer, RecipeItemMaker> _activeMakers = new FastMap<>();
	
	protected RecipeController()
	{
		_activeMakers.shared();
	}
	
	public void requestBookOpen(L2PcInstance player, boolean isDwarvenCraft)
	{
		if (!_activeMakers.containsKey(player.getObjectId()))
		{
			RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, player.getMaxMp());
			response.addRecipes(isDwarvenCraft ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook());
			player.sendPacket(response);
			return;
		}
		player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
	}
	
	public void requestMakeItemAbort(L2PcInstance player)
	{
		_activeMakers.remove(player.getObjectId());
	}
	
	public void requestManufactureItem(L2PcInstance manufacturer, int recipeListId, L2PcInstance player)
	{
		final L2RecipeList recipeList = RecipeParser.getInstance().getValidRecipeList(player, recipeListId);
		if (recipeList == null)
		{
			return;
		}
		
		List<L2RecipeList> dwarfRecipes = Arrays.asList(manufacturer.getDwarvenRecipeBook());
		List<L2RecipeList> commonRecipes = Arrays.asList(manufacturer.getCommonRecipeBook());
		
		if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList))
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (Config.ALT_GAME_CREATION && _activeMakers.containsKey(manufacturer.getObjectId()))
		{
			player.sendPacket(SystemMessageId.CLOSE_STORE_WINDOW_AND_TRY_AGAIN);
			return;
		}
		
		final RecipeItemMaker maker = new RecipeItemMaker(manufacturer, recipeList, player);
		if (maker._isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				_activeMakers.put(manufacturer.getObjectId(), maker);
				ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
			}
			else
			{
				maker.run();
			}
		}
	}
	
	public void requestMakeItem(L2PcInstance player, int recipeListId)
	{
		if (player.isInCombat() || player.isInDuel())
		{
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			return;
		}
		
		final L2RecipeList recipeList = RecipeParser.getInstance().getValidRecipeList(player, recipeListId);
		if (recipeList == null)
		{
			return;
		}
		
		List<L2RecipeList> dwarfRecipes = Arrays.asList(player.getDwarvenRecipeBook());
		List<L2RecipeList> commonRecipes = Arrays.asList(player.getCommonRecipeBook());
		
		if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList))
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (Config.ALT_GAME_CREATION && _activeMakers.containsKey(player.getObjectId()))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1);
			sm.addItemName(recipeList.getItemId());
			sm.addString("You are busy creating.");
			player.sendPacket(sm);
			return;
		}
		
		final RecipeItemMaker maker = new RecipeItemMaker(player, recipeList, player);
		if (maker._isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				_activeMakers.put(player.getObjectId(), maker);
				ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
			}
			else
			{
				maker.run();
			}
		}
	}
	
	private static class RecipeItemMaker implements Runnable
	{
		private static final Logger _log = Logger.getLogger(RecipeItemMaker.class.getName());
		protected boolean _isValid;
		protected List<TempItem> _items = null;
		protected final L2RecipeList _recipeList;
		protected final L2PcInstance _player;
		protected final L2PcInstance _target;
		protected final L2Skill _skill;
		protected final int _skillId;
		protected final int _skillLevel;
		protected int _creationPasses = 1;
		protected int _itemGrab;
		protected int _exp = -1;
		protected int _sp = -1;
		protected long _price;
		protected int _totalItems;
		protected int _delay;
		
		public RecipeItemMaker(L2PcInstance pPlayer, L2RecipeList pRecipeList, L2PcInstance pTarget)
		{
			_player = pPlayer;
			_target = pTarget;
			_recipeList = pRecipeList;
			
			_isValid = false;
			_skillId = _recipeList.isDwarvenRecipe() ? L2Skill.SKILL_CREATE_DWARVEN : L2Skill.SKILL_CREATE_COMMON;
			_skillLevel = _player.getSkillLevel(_skillId);
			_skill = _player.getKnownSkill(_skillId);
			
			_player.isInCraftMode(true);
			
			if (_player.isAlikeDead())
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_target.isAlikeDead())
			{
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_target.isProcessingTransaction())
			{
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_player.isProcessingTransaction())
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_recipeList.getRecipes().length == 0)
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_recipeList.getLevel() > _skillLevel)
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_player != _target)
			{
				final L2ManufactureItem item = _player.getManufactureItems().get(_recipeList.getId());
				if (item != null)
				{
					_price = item.getCost();
					if (_target.getAdena() < _price)
					{
						_target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
						abort();
						return;
					}
				}
			}
			
			if ((_items = listItems(false)) == null)
			{
				abort();
				return;
			}
			
			for (TempItem i : _items)
			{
				_totalItems += i.getQuantity();
			}
			
			if (!calculateStatUse(false, false))
			{
				abort();
				return;
			}
			
			if (Config.ALT_GAME_CREATION)
			{
				calculateAltStatChange();
			}
			updateMakeInfo(true);
			updateCurMp();
			updateCurLoad();
			
			_player.isInCraftMode(false);
			_isValid = true;
		}
		
		@Override
		public void run()
		{
			if (!Config.IS_CRAFTING_ENABLED)
			{
				_target.sendMessage("Item creation is currently disabled.");
				abort();
				return;
			}
			
			if ((_player == null) || (_target == null))
			{
				_log.warning("player or target == null (disconnected?), aborting" + _target + _player);
				abort();
				return;
			}
			
			if (!_player.isOnline() || !_target.isOnline())
			{
				_log.warning("player or target is not online, aborting " + _target + _player);
				abort();
				return;
			}
			
			if (Config.ALT_GAME_CREATION && !_activeMakers.containsKey(_player.getObjectId()))
			{
				if (_target != _player)
				{
					_target.sendMessage("Manufacture aborted");
					_player.sendMessage("Manufacture aborted");
				}
				else
				{
					_player.sendMessage("Item creation aborted");
				}
				
				abort();
				return;
			}
			
			if (Config.ALT_GAME_CREATION && !_items.isEmpty())
			{
				
				if (!calculateStatUse(true, true))
				{
					return;
				}
				updateCurMp();
				
				grabSomeItems();
				
				if (!_items.isEmpty())
				{
					_delay = (int) ((Config.ALT_GAME_CREATION_SPEED * _player.getMReuseRate(_skill) * GameTimeController.TICKS_PER_SECOND) / Config.RATE_CONSUMABLE_COST) * GameTimeController.MILLIS_IN_TICK;
					
					MagicSkillUse msk = new MagicSkillUse(_player, _skillId, _skillLevel, _delay, 0);
					_player.broadcastPacket(msk);
					
					_player.sendPacket(new SetupGauge(0, _delay));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
				}
				else
				{
					_player.sendPacket(new SetupGauge(0, _delay));
					
					try
					{
						Thread.sleep(_delay);
					}
					catch (InterruptedException e)
					{
					}
					finally
					{
						finishCrafting();
					}
				}
			}
			else
			{
				finishCrafting();
			}
		}
		
		private void finishCrafting()
		{
			if (!Config.ALT_GAME_CREATION)
			{
				calculateStatUse(false, true);
			}
			
			if ((_target != _player) && (_price > 0))
			{
				L2ItemInstance adenatransfer = _target.transferItem("PayManufacture", _target.getInventory().getAdenaInstance().getObjectId(), _price, _player.getInventory(), _player);
				
				if (adenatransfer == null)
				{
					_target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
					abort();
					return;
				}
			}
			
			if ((_items = listItems(true)) == null)
			{
			}
			else if (Rnd.get(100) < _recipeList.getSuccessRate())
			{
				rewardPlayer();
				updateMakeInfo(true);
			}
			else
			{
				if (_target != _player)
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CREATION_OF_S2_FOR_C1_AT_S3_ADENA_FAILED);
					msg.addString(_target.getName());
					msg.addItemName(_recipeList.getItemId());
					msg.addItemNumber(_price);
					_player.sendPacket(msg);
					
					msg = SystemMessage.getSystemMessage(SystemMessageId.C1_FAILED_TO_CREATE_S2_FOR_S3_ADENA);
					msg.addString(_player.getName());
					msg.addItemName(_recipeList.getItemId());
					msg.addItemNumber(_price);
					_target.sendPacket(msg);
				}
				else
				{
					_target.sendPacket(SystemMessageId.ITEM_MIXING_FAILED);
				}
				updateMakeInfo(false);
			}
			updateCurMp();
			updateCurLoad();
			_activeMakers.remove(_player.getObjectId());
			_player.isInCraftMode(false);
			_target.sendPacket(new ItemList(_target, false));
		}
		
		private void updateMakeInfo(boolean success)
		{
			if (_target == _player)
			{
				_target.sendPacket(new RecipeItemMakeInfo(_recipeList.getId(), _target, success));
			}
			else
			{
				_target.sendPacket(new RecipeShopItemInfo(_player, _recipeList.getId()));
			}
		}
		
		private void updateCurLoad()
		{
			StatusUpdate su = new StatusUpdate(_target);
			su.addAttribute(StatusUpdate.CUR_LOAD, _target.getCurrentLoad());
			_target.sendPacket(su);
		}
		
		private void updateCurMp()
		{
			StatusUpdate su = new StatusUpdate(_target);
			su.addAttribute(StatusUpdate.CUR_MP, (int) _target.getCurrentMp());
			_target.sendPacket(su);
		}
		
		private void grabSomeItems()
		{
			int grabItems = _itemGrab;
			while ((grabItems > 0) && !_items.isEmpty())
			{
				TempItem item = _items.get(0);
				
				int count = item.getQuantity();
				if (count >= grabItems)
				{
					count = grabItems;
				}
				
				item.setQuantity(item.getQuantity() - count);
				if (item.getQuantity() <= 0)
				{
					_items.remove(0);
				}
				else
				{
					_items.set(0, item);
				}
				
				grabItems -= count;
				
				if (_target == _player)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
					sm.addItemNumber(count);
					sm.addItemName(item.getItemId());
					_player.sendPacket(sm);
				}
				else
				{
					_target.sendMessage("Manufacturer " + _player.getName() + " used " + count + " " + item.getItemName());
				}
			}
		}
		
		private void calculateAltStatChange()
		{
			_itemGrab = _skillLevel;
			
			for (L2RecipeStatInstance altStatChange : _recipeList.getAltStatChange())
			{
				if (altStatChange.getType() == L2RecipeStatInstance.StatType.XP)
				{
					_exp = altStatChange.getValue();
				}
				else if (altStatChange.getType() == L2RecipeStatInstance.StatType.SP)
				{
					_sp = altStatChange.getValue();
				}
				else if (altStatChange.getType() == L2RecipeStatInstance.StatType.GIM)
				{
					_itemGrab *= altStatChange.getValue();
				}
			}
			_creationPasses = (_totalItems / _itemGrab) + ((_totalItems % _itemGrab) != 0 ? 1 : 0);
			if (_creationPasses < 1)
			{
				_creationPasses = 1;
			}
		}
		
		private boolean calculateStatUse(boolean isWait, boolean isReduce)
		{
			boolean ret = true;
			for (L2RecipeStatInstance statUse : _recipeList.getStatUse())
			{
				double modifiedValue = statUse.getValue() / _creationPasses;
				if (statUse.getType() == L2RecipeStatInstance.StatType.HP)
				{
					if (_player.getCurrentHp() <= modifiedValue)
					{
						if (Config.ALT_GAME_CREATION && isWait)
						{
							_player.sendPacket(new SetupGauge(0, _delay));
							ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
						}
						else
						{
							_target.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
							abort();
						}
						ret = false;
					}
					else if (isReduce)
					{
						_player.reduceCurrentHp(modifiedValue, _player, _skill);
					}
				}
				else if (statUse.getType() == L2RecipeStatInstance.StatType.MP)
				{
					if (_player.getCurrentMp() < modifiedValue)
					{
						if (Config.ALT_GAME_CREATION && isWait)
						{
							_player.sendPacket(new SetupGauge(0, _delay));
							ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
						}
						else
						{
							_target.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							abort();
						}
						ret = false;
					}
					else if (isReduce)
					{
						_player.reduceCurrentMp(modifiedValue);
					}
				}
				else
				{
					_target.sendMessage("Recipe error!!!, please tell this to your GM.");
					ret = false;
					abort();
				}
			}
			return ret;
		}
		
		private List<TempItem> listItems(boolean remove)
		{
			L2RecipeInstance[] recipes = _recipeList.getRecipes();
			Inventory inv = _target.getInventory();
			List<TempItem> materials = new ArrayList<>();
			SystemMessage sm;
			
			for (L2RecipeInstance recipe : recipes)
			{
				if (recipe.getQuantity() > 0)
				{
					L2ItemInstance item = inv.getItemByItemId(recipe.getItemId());
					long itemQuantityAmount = item == null ? 0 : item.getCount();
					
					if (itemQuantityAmount < recipe.getQuantity())
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.MISSING_S2_S1_TO_CREATE);
						sm.addItemName(recipe.getItemId());
						sm.addItemNumber(recipe.getQuantity() - itemQuantityAmount);
						_target.sendPacket(sm);
						
						abort();
						return null;
					}
					TempItem temp = new TempItem(item, recipe.getQuantity());
					materials.add(temp);
				}
			}
			
			if (remove)
			{
				for (TempItem tmp : materials)
				{
					inv.destroyItemByItemId("Manufacture", tmp.getItemId(), tmp.getQuantity(), _target, _player);
					
					if (tmp.getQuantity() > 1)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(tmp.getItemId());
						sm.addItemNumber(tmp.getQuantity());
						_target.sendPacket(sm);
					}
					else
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(tmp.getItemId());
						_target.sendPacket(sm);
					}
				}
			}
			return materials;
		}
		
		private void abort()
		{
			updateMakeInfo(false);
			_player.isInCraftMode(false);
			_activeMakers.remove(_player.getObjectId());
		}
		
		private void rewardPlayer()
		{
			int rareProdId = _recipeList.getRareItemId();
			int itemId = _recipeList.getItemId();
			int itemCount = _recipeList.getCount();
			L2Item template = ItemHolder.getInstance().getTemplate(itemId);
			
			if ((rareProdId != -1) && ((rareProdId == itemId) || Config.CRAFT_MASTERWORK))
			{
				if (Rnd.get(100) < _recipeList.getRarity())
				{
					itemId = rareProdId;
					itemCount = _recipeList.getRareCount();
				}
			}
			
			_target.getInventory().addItem("Manufacture", itemId, itemCount, _target, _player);
			
			SystemMessage sm = null;
			if (_target != _player)
			{
				if (itemCount == 1)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CREATED_FOR_C1_FOR_S3_ADENA);
					sm.addString(_target.getName());
					sm.addItemName(itemId);
					sm.addItemNumber(_price);
					_player.sendPacket(sm);
					
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_FOR_S3_ADENA);
					sm.addString(_player.getName());
					sm.addItemName(itemId);
					sm.addItemNumber(_price);
					_target.sendPacket(sm);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S3_S_CREATED_FOR_C1_FOR_S4_ADENA);
					sm.addString(_target.getName());
					sm.addNumber(itemCount);
					sm.addItemName(itemId);
					sm.addItemNumber(_price);
					_player.sendPacket(sm);
					
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_S3_S_FOR_S4_ADENA);
					sm.addString(_player.getName());
					sm.addNumber(itemCount);
					sm.addItemName(itemId);
					sm.addItemNumber(_price);
					_target.sendPacket(sm);
				}
			}
			
			if (itemCount > 1)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(itemId);
				sm.addItemNumber(itemCount);
				_target.sendPacket(sm);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				sm.addItemName(itemId);
				_target.sendPacket(sm);
			}
			
			if (Config.ALT_GAME_CREATION)
			{
				int recipeLevel = _recipeList.getLevel();
				if (_exp < 0)
				{
					_exp = template.getReferencePrice() * itemCount;
					_exp /= recipeLevel;
				}
				if (_sp < 0)
				{
					_sp = _exp / 10;
				}
				if (itemId == rareProdId)
				{
					_exp *= Config.ALT_GAME_CREATION_RARE_XPSP_RATE;
					_sp *= Config.ALT_GAME_CREATION_RARE_XPSP_RATE;
				}
				
				if (_exp < 0)
				{
					_exp = 0;
				}
				if (_sp < 0)
				{
					_sp = 0;
				}
				
				for (int i = _skillLevel; i > recipeLevel; i--)
				{
					_exp /= 4;
					_sp /= 4;
				}
				_player.addExpAndSp((int) _player.calcStat(Stats.EXPSP_RATE, _exp * Config.ALT_GAME_CREATION_XP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null), (int) _player.calcStat(Stats.EXPSP_RATE, _sp * Config.ALT_GAME_CREATION_SP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null));
			}
			updateMakeInfo(true);
		}
	}
	
	public static RecipeController getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final RecipeController _instance = new RecipeController();
	}
}