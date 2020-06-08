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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2Seed;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.items.L2Item;

public class ManorParser extends DocumentParser
{
	private static Logger _log = Logger.getLogger(ManorParser.class.getName());
	
	private static Map<Integer, L2Seed> _seeds = new HashMap<>();
	
	protected ManorParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_seeds.clear();
		parseDatapackFile("data/seeds.xml");
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _seeds.size() + " Seeds");
	}
	
	@Override
	protected void parseDocument()
	{
		StatsSet set;
		NamedNodeMap attrs;
		Node att;
		int castleId;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("castle".equalsIgnoreCase(d.getNodeName()))
					{
						castleId = parseInt(d.getAttributes(), "id");
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("crop".equalsIgnoreCase(c.getNodeName()))
							{
								set = new StatsSet();
								set.set("castleId", castleId);
								
								attrs = c.getAttributes();
								for (int i = 0; i < attrs.getLength(); i++)
								{
									att = attrs.item(i);
									set.set(att.getNodeName(), att.getNodeValue());
								}
								
								L2Seed seed = new L2Seed(set);
								_seeds.put(seed.getSeedId(), seed);
							}
						}
					}
				}
			}
		}
	}
	
	public List<Integer> getAllCrops()
	{
		List<Integer> crops = new ArrayList<>();
		
		for (L2Seed seed : _seeds.values())
		{
			if (!crops.contains(seed.getCropId()) && (seed.getCropId() != 0) && !crops.contains(seed.getCropId()))
			{
				crops.add(seed.getCropId());
			}
		}
		
		return crops;
	}
	
	public int getSeedBasicPrice(int seedId)
	{
		final L2Item seedItem = ItemHolder.getInstance().getTemplate(seedId);
		if (seedItem != null)
		{
			return seedItem.getReferencePrice();
		}
		return 0;
	}
	
	public int getSeedBasicPriceByCrop(int cropId)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return getSeedBasicPrice(seed.getSeedId());
			}
		}
		return 0;
	}
	
	public int getCropBasicPrice(int cropId)
	{
		final L2Item cropItem = ItemHolder.getInstance().getTemplate(cropId);
		if (cropItem != null)
		{
			return cropItem.getReferencePrice();
		}
		return 0;
	}
	
	public int getMatureCrop(int cropId)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getMatureId();
			}
		}
		return 0;
	}
	
	public long getSeedBuyPrice(int seedId)
	{
		long buyPrice = getSeedBasicPrice(seedId);
		return (buyPrice > 0 ? buyPrice : 1);
	}
	
	public int getSeedMinLevel(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getLevel() - 5;
		}
		return -1;
	}
	
	public int getSeedMaxLevel(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getLevel() + 5;
		}
		return -1;
	}
	
	public int getSeedLevelByCrop(int cropId)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getLevel();
			}
		}
		return 0;
	}
	
	public int getSeedLevel(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getLevel();
		}
		return -1;
	}
	
	public boolean isAlternative(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.isAlternative();
		}
		return false;
	}
	
	public int getCropType(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getCropId();
		}
		return -1;
	}
	
	public int getRewardItem(int cropId, int type)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getReward(type);
			}
		}
		return -1;
	}
	
	public int getRewardItemBySeed(int seedId, int type)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getReward(type);
		}
		return 0;
	}
	
	public List<Integer> getCropsForCastle(int castleId)
	{
		List<Integer> crops = new ArrayList<>();
		
		for (L2Seed seed : _seeds.values())
		{
			if ((seed.getCastleId() == castleId) && !crops.contains(seed.getCropId()))
			{
				crops.add(seed.getCropId());
			}
		}
		
		return crops;
	}
	
	public List<Integer> getSeedsForCastle(int castleId)
	{
		List<Integer> seedsID = new ArrayList<>();
		
		for (L2Seed seed : _seeds.values())
		{
			if ((seed.getCastleId() == castleId) && !seedsID.contains(seed.getSeedId()))
			{
				seedsID.add(seed.getSeedId());
			}
		}
		
		return seedsID;
	}
	
	public int getCastleIdForSeed(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getCastleId();
		}
		return 0;
	}
	
	public int getSeedSaleLimit(int seedId)
	{
		L2Seed seed = _seeds.get(seedId);
		
		if (seed != null)
		{
			return seed.getSeedLimit();
		}
		return 0;
	}
	
	public int getCropPuchaseLimit(int cropId)
	{
		for (L2Seed seed : _seeds.values())
		{
			if (seed.getCropId() == cropId)
			{
				return seed.getCropLimit();
			}
		}
		return 0;
	}
	
	public static ManorParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ManorParser _instance = new ManorParser();
	}
}