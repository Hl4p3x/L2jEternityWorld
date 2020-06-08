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
package l2e.gameserver.handler;

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.handler.itemhandlers.AIOItem;
import l2e.gameserver.handler.itemhandlers.BeastSoulShot;
import l2e.gameserver.handler.itemhandlers.BeastSpice;
import l2e.gameserver.handler.itemhandlers.BeastSpiritShot;
import l2e.gameserver.handler.itemhandlers.BlessedSpiritShot;
import l2e.gameserver.handler.itemhandlers.Book;
import l2e.gameserver.handler.itemhandlers.Bypass;
import l2e.gameserver.handler.itemhandlers.Calculator;
import l2e.gameserver.handler.itemhandlers.ChristmasTree;
import l2e.gameserver.handler.itemhandlers.Disguise;
import l2e.gameserver.handler.itemhandlers.Elixir;
import l2e.gameserver.handler.itemhandlers.EnchantAttribute;
import l2e.gameserver.handler.itemhandlers.EnchantScrolls;
import l2e.gameserver.handler.itemhandlers.EventItem;
import l2e.gameserver.handler.itemhandlers.ExtractableItems;
import l2e.gameserver.handler.itemhandlers.FishShots;
import l2e.gameserver.handler.itemhandlers.Harvester;
import l2e.gameserver.handler.itemhandlers.ItemSkills;
import l2e.gameserver.handler.itemhandlers.ItemSkillsTemplate;
import l2e.gameserver.handler.itemhandlers.ManaPotion;
import l2e.gameserver.handler.itemhandlers.Maps;
import l2e.gameserver.handler.itemhandlers.MercTicket;
import l2e.gameserver.handler.itemhandlers.NevitHourglass;
import l2e.gameserver.handler.itemhandlers.NicknameColor;
import l2e.gameserver.handler.itemhandlers.PetFood;
import l2e.gameserver.handler.itemhandlers.QuestItems;
import l2e.gameserver.handler.itemhandlers.Recipes;
import l2e.gameserver.handler.itemhandlers.RollingDice;
import l2e.gameserver.handler.itemhandlers.ScrollOfResurrection;
import l2e.gameserver.handler.itemhandlers.Seed;
import l2e.gameserver.handler.itemhandlers.SevenSignsRecord;
import l2e.gameserver.handler.itemhandlers.SoulShots;
import l2e.gameserver.handler.itemhandlers.SpecialXMas;
import l2e.gameserver.handler.itemhandlers.SpiritShot;
import l2e.gameserver.handler.itemhandlers.SummonItems;
import l2e.gameserver.handler.itemhandlers.TeleportBookmark;
import l2e.gameserver.model.items.L2EtcItem;
import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemHandler
{
	private static Logger _log = Logger.getLogger(ItemHandler.class.getName());
	
	private final TIntObjectHashMap<IItemHandler> _datatable;
	
	public static ItemHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	protected ItemHandler()
	{
		_datatable = new TIntObjectHashMap<>();
		
		if (Config.ENABLE_AIO_NPCS)
		{
			registerHandler(new AIOItem());
		}
		registerHandler(new BeastSoulShot());
		registerHandler(new BeastSpice());
		registerHandler(new BeastSpiritShot());
		registerHandler(new BlessedSpiritShot());
		registerHandler(new Bypass());
		registerHandler(new Book());
		registerHandler(new Calculator());
		registerHandler(new ChristmasTree());
		registerHandler(new Disguise());
		registerHandler(new Elixir());
		registerHandler(new EnchantAttribute());
		registerHandler(new EnchantScrolls());
		registerHandler(new EventItem());
		registerHandler(new ExtractableItems());
		registerHandler(new FishShots());
		registerHandler(new Harvester());
		registerHandler(new ItemSkills());
		registerHandler(new ItemSkillsTemplate());
		registerHandler(new ManaPotion());
		registerHandler(new Maps());
		registerHandler(new MercTicket());
		registerHandler(new NevitHourglass());
		registerHandler(new NicknameColor());
		registerHandler(new PetFood());
		registerHandler(new QuestItems());
		registerHandler(new Recipes());
		registerHandler(new RollingDice());
		registerHandler(new ScrollOfResurrection());
		registerHandler(new Seed());
		registerHandler(new SevenSignsRecord());
		registerHandler(new SoulShots());
		registerHandler(new SpecialXMas());
		registerHandler(new SpiritShot());
		registerHandler(new SummonItems());
		registerHandler(new TeleportBookmark());
		
		_log.info("Loaded " + _datatable.size() + " ItemHandlers.");
	}
	
	public void registerHandler(IItemHandler handler)
	{
		_datatable.put(handler.getClass().getSimpleName().intern().hashCode(), handler);
	}
	
	public IItemHandler getHandler(L2EtcItem item)
	{
		if ((item == null) || (item.getHandlerName() == null))
		{
			return null;
		}
		return _datatable.get(item.getHandlerName().hashCode());
	}
	
	private static class SingletonHolder
	{
		protected static final ItemHandler _instance = new ItemHandler();
	}
}