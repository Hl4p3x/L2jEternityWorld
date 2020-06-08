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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2PetData;
import l2e.gameserver.model.L2PetLevelData;
import l2e.gameserver.model.StatsSet;

public final class PetsParser extends DocumentParser
{
	private static final Map<Integer, L2PetData> _pets = new HashMap<>();
	
	protected PetsParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_pets.clear();
		parseDirectory("data/stats/pets/");
		_log.info(getClass().getSimpleName() + ": Loaded " + _pets.size() + " Pets.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node n = getCurrentDocument().getFirstChild();
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if (d.getNodeName().equals("pet"))
			{
				int npcId = parseInt(d.getAttributes(), "id");
				int itemId = parseInt(d.getAttributes(), "itemId");
				L2PetData data = new L2PetData(npcId, itemId);
				for (Node p = d.getFirstChild(); p != null; p = p.getNextSibling())
				{
					if (p.getNodeName().equals("set"))
					{
						attrs = p.getAttributes();
						String type = attrs.getNamedItem("name").getNodeValue();
						if ("food".equals(type))
						{
							for (String foodId : attrs.getNamedItem("val").getNodeValue().split(";"))
							{
								data.addFood(Integer.valueOf(foodId));
							}
						}
						else if ("load".equals(type))
						{
							data.setLoad(parseInt(attrs, "val"));
						}
						else if ("hungry_limit".equals(type))
						{
							data.setHungryLimit(parseInt(attrs, "val"));
						}
						else if ("sync_level".equals(type))
						{
							data.setSyncLevel(parseInt(attrs, "val") == 1);
						}
					}
					else if (p.getNodeName().equals("skills"))
					{
						for (Node s = p.getFirstChild(); s != null; s = s.getNextSibling())
						{
							if (s.getNodeName().equals("skill"))
							{
								attrs = s.getAttributes();
								data.addNewSkill(parseInt(attrs, "skillId"), parseInt(attrs, "skillLvl"), parseInt(attrs, "minLvl"));
							}
						}
					}
					else if (p.getNodeName().equals("stats"))
					{
						for (Node s = p.getFirstChild(); s != null; s = s.getNextSibling())
						{
							if (s.getNodeName().equals("stat"))
							{
								final int level = Integer.parseInt(s.getAttributes().getNamedItem("level").getNodeValue());
								final StatsSet set = new StatsSet();
								for (Node bean = s.getFirstChild(); bean != null; bean = bean.getNextSibling())
								{
									if (bean.getNodeName().equals("set"))
									{
										attrs = bean.getAttributes();
										if (attrs.getNamedItem("name").getNodeValue().equals("speed_on_ride"))
										{
											set.set("walkSpeedOnRide", attrs.getNamedItem("walk").getNodeValue());
											set.set("runSpeedOnRide", attrs.getNamedItem("run").getNodeValue());
											set.set("slowSwimSpeedOnRide", attrs.getNamedItem("slowSwim").getNodeValue());
											set.set("fastSwimSpeedOnRide", attrs.getNamedItem("fastSwim").getNodeValue());
											if (attrs.getNamedItem("slowFly") != null)
											{
												set.set("slowFlySpeedOnRide", attrs.getNamedItem("slowFly").getNodeValue());
											}
											if (attrs.getNamedItem("fastFly") != null)
											{
												set.set("fastFlySpeedOnRide", attrs.getNamedItem("fastFly").getNodeValue());
											}
										}
										else
										{
											set.set(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("val").getNodeValue());
										}
									}
								}
								data.addNewStat(level, new L2PetLevelData(set));
							}
						}
					}
				}
				_pets.put(npcId, data);
			}
		}
	}
	
	public L2PetData getPetDataByItemId(int itemId)
	{
		for (L2PetData data : _pets.values())
		{
			if (data.getItemId() == itemId)
			{
				return data;
			}
		}
		return null;
	}
	
	public L2PetLevelData getPetLevelData(int petId, int petLevel)
	{
		final L2PetData pd = getPetData(petId);
		if (pd != null)
		{
			return pd.getPetLevelData(petLevel);
		}
		return null;
	}
	
	public L2PetData getPetData(int petId)
	{
		if (!_pets.containsKey(petId))
		{
			_log.info(getClass().getSimpleName() + ": Missing pet data for npcid: " + petId);
		}
		return _pets.get(petId);
	}
	
	public int getPetMinLevel(int petId)
	{
		return _pets.get(petId).getMinLevel();
	}
	
	public static boolean isStrider(int npcId)
	{
		return ((npcId >= 12526) && (npcId <= 12528)) || ((npcId >= 16038) && (npcId <= 16040)) || (npcId == 16068);
	}
	
	public static boolean isGrowUpWolfGroup(int npcId)
	{
		return (npcId == 16025) || (npcId == 16030) || (npcId == 16037) || (npcId == 16041) || (npcId == 16042);
	}
	
	public static boolean isHatchlingGroup(int npcId)
	{
		return (npcId >= 12311) && (npcId <= 12313);
	}
	
	public static boolean isAllWolfGroup(int npcId)
	{
		return (npcId == 12077) || (npcId == 16025) || (npcId == 16030) || (npcId == 16037) || (npcId == 16041) || (npcId == 16042);
	}
	
	public static boolean isBabyPetGroup(int npcId)
	{
		return (npcId >= 12780) && (npcId <= 12782);
	}
	
	public static boolean isUpgradeBabyPetGroup(int npcId)
	{
		return (npcId >= 16034) && (npcId <= 16036);
	}
	
	public static boolean isItemEquipPetGroup(int npcId)
	{
		return (npcId == 12077) || ((npcId >= 12311) && (npcId <= 12313)) || ((npcId >= 12526) && (npcId <= 12528)) || ((npcId >= 12780) && (npcId <= 12782)) || (npcId == 16025) || (npcId == 16030) || ((npcId >= 16034) && (npcId <= 16036)) || (npcId == 16037) || ((npcId >= 16038) && (npcId <= 16042)) || (npcId == 16068) || (npcId == 16067) || (npcId == 16071) || (npcId == 16072) || (npcId == 1561);
	}
	
	public static int[] getPetItemsByNpc(int npcId)
	{
		switch (npcId)
		{
			case 12077:
				return new int[]
				{
					2375
				};
			case 16025:
				return new int[]
				{
					9882
				};
			case 16030:
				return new int[]
				{
					10163
				};
			case 16037:
				return new int[]
				{
					10307
				};
			case 16041:
				return new int[]
				{
					10426
				};
			case 16042:
				return new int[]
				{
					10611
				};
			case 12564:
				return new int[]
				{
					4425
				};
			case 12311:
			case 12312:
			case 12313:
				return new int[]
				{
					3500,
					3501,
					3502
				};
			case 12526:
			case 12527:
			case 12528:
			case 16038:
			case 16039:
			case 16040:
			case 16068:
				return new int[]
				{
					4422,
					4423,
					4424,
					10308,
					10309,
					10310,
					14819
				};
			case 12621:
				return new int[]
				{
					8663
				};
			case 12780:
			case 12782:
			case 12781:
				return new int[]
				{
					6648,
					6649,
					6650
				};
			case 16034:
			case 16035:
			case 16036:
			case 16043:
			case 16044:
			case 16045:
			case 16046:
			case 16050:
			case 16051:
			case 16052:
			case 16053:
				return new int[]
				{
					10311,
					10312,
					10313,
					13020,
					13019,
					13017,
					13018,
					13550,
					14062,
					14061,
					13551
				};
			default:
				return new int[]
				{
					0
				};
		}
	}
	
	public static boolean isMountable(int npcId)
	{
		return (npcId == 12526) || (npcId == 12527) || (npcId == 12528) || (npcId == 12621) || (npcId == 16037) || (npcId == 16041) || (npcId == 16042) || (npcId == 16038) || (npcId == 16039) || (npcId == 16040) || (npcId == 16068);
	}
	
	public static PetsParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PetsParser _instance = new PetsParser();
	}
}