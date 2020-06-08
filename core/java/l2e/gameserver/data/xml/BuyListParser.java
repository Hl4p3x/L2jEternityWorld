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
package l2e.gameserver.data.xml;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.buylist.L2BuyList;
import l2e.gameserver.model.buylist.Product;
import l2e.gameserver.model.items.L2Item;
import l2e.util.file.filter.NumericNameFilter;

public final class BuyListParser extends DocumentParser
{
	private final Map<Integer, L2BuyList> _buyLists = new HashMap<>();
	
	protected BuyListParser()
	{
		setCurrentFileFilter(new NumericNameFilter());
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_buyLists.clear();
		parseDirectory("data/buylists");
		if (Config.CUSTOM_BUYLIST_LOAD)
		{
			parseDirectory("data/buylists/custom");
		}
		
		_log.info(getClass().getSimpleName() + ": Loaded " + _buyLists.size() + " BuyLists.");
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM `buylists`"))
		{
			while (rs.next())
			{
				int buyListId = rs.getInt("buylist_id");
				int itemId = rs.getInt("item_id");
				long count = rs.getLong("count");
				long nextRestockTime = rs.getLong("next_restock_time");
				final L2BuyList buyList = getBuyList(buyListId);
				if (buyList == null)
				{
					_log.warning("BuyList found in database but not loaded from xml! BuyListId: " + buyListId);
					continue;
				}
				final Product product = buyList.getProductByItemId(itemId);
				if (product == null)
				{
					_log.warning("ItemId found in database but not loaded from xml! BuyListId: " + buyListId + " ItemId: " + itemId);
					continue;
				}
				if (count < product.getMaxCount())
				{
					product.setCount(count);
					product.restartRestockTask(nextRestockTime);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Failed to load buyList data from database.", e);
		}
	}
	
	@Override
	protected void parseDocument()
	{
		try
		{
			final int buyListId = Integer.parseInt(getCurrentFile().getName().replaceAll(".xml", ""));
			
			for (Node node = getCurrentDocument().getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("list".equalsIgnoreCase(node.getNodeName()))
				{
					final L2BuyList buyList = new L2BuyList(buyListId);
					for (Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling())
					{
						if ("item".equalsIgnoreCase(list_node.getNodeName()))
						{
							int itemId = -1;
							long price = -1;
							long restockDelay = -1;
							long count = -1;
							NamedNodeMap attrs = list_node.getAttributes();
							Node attr = attrs.getNamedItem("id");
							itemId = Integer.parseInt(attr.getNodeValue());
							attr = attrs.getNamedItem("price");
							if (attr != null)
							{
								price = Long.parseLong(attr.getNodeValue());
							}
							attr = attrs.getNamedItem("restock_delay");
							if (attr != null)
							{
								restockDelay = Long.parseLong(attr.getNodeValue());
							}
							attr = attrs.getNamedItem("count");
							if (attr != null)
							{
								count = Long.parseLong(attr.getNodeValue());
							}
							final L2Item item = ItemHolder.getInstance().getTemplate(itemId);
							if (item != null)
							{
								buyList.addProduct(new Product(buyList.getListId(), item, price, restockDelay, count));
							}
							else
							{
								_log.warning("Item not found. BuyList:" + buyList.getListId() + " ItemID:" + itemId + " File:" + getCurrentFile().getName());
							}
						}
						else if ("npcs".equalsIgnoreCase(list_node.getNodeName()))
						{
							for (Node npcs_node = list_node.getFirstChild(); npcs_node != null; npcs_node = npcs_node.getNextSibling())
							{
								if ("npc".equalsIgnoreCase(npcs_node.getNodeName()))
								{
									int npcId = Integer.parseInt(npcs_node.getTextContent());
									buyList.addAllowedNpc(npcId);
								}
							}
						}
					}
					_buyLists.put(buyList.getListId(), buyList);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Failed to load buyList data from xml File:" + getCurrentFile().getName(), e);
		}
	}
	
	public L2BuyList getBuyList(int listId)
	{
		return _buyLists.get(listId);
	}
	
	public static BuyListParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected final static BuyListParser _instance = new BuyListParser();
	}
}