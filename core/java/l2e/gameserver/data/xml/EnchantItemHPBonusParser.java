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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.LambdaConst;
import l2e.gameserver.model.stats.Stats;

public class EnchantItemHPBonusParser extends DocumentParser
{
	private final Map<Integer, List<Integer>> _armorHPBonuses = new HashMap<>();
	
	private static final float fullArmorModifier = 1.5f;
	
	protected EnchantItemHPBonusParser()
	{
		load();
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("enchantHP".equals(d.getNodeName()))
					{
						List<Integer> bonuses = new ArrayList<>();
						for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
						{
							if ("bonus".equals(e.getNodeName()))
							{
								bonuses.add(Integer.valueOf(e.getTextContent()));
							}
						}
						_armorHPBonuses.put(parseInteger(d.getAttributes(), "grade"), bonuses);
					}
				}
			}
		}
		
		if (!_armorHPBonuses.isEmpty())
		{
			final ItemHolder it = ItemHolder.getInstance();
			L2Item item;
			
			final Collection<Integer> armorIds = it.getAllArmorsId();
			for (Integer itemId : armorIds)
			{
				item = it.getTemplate(itemId);
				if ((item != null) && (item.getCrystalType() != L2Item.CRYSTAL_NONE))
				{
					switch (item.getBodyPart())
					{
						case L2Item.SLOT_CHEST:
						case L2Item.SLOT_FEET:
						case L2Item.SLOT_GLOVES:
						case L2Item.SLOT_HEAD:
						case L2Item.SLOT_LEGS:
						case L2Item.SLOT_BACK:
						case L2Item.SLOT_FULL_ARMOR:
						case L2Item.SLOT_UNDERWEAR:
						case L2Item.SLOT_L_HAND:
							item.attach(new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0)));
							break;
						default:
							break;
					}
				}
			}
			
			final Collection<Integer> shieldIds = it.getAllWeaponsId();
			for (Integer itemId : shieldIds)
			{
				item = it.getTemplate(itemId);
				if ((item != null) && (item.getCrystalType() != L2Item.CRYSTAL_NONE))
				{
					switch (item.getBodyPart())
					{
						case L2Item.SLOT_L_HAND:
							item.attach(new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0)));
							break;
						default:
							break;
					}
				}
			}
		}
	}
	
	@Override
	public void load()
	{
		_armorHPBonuses.clear();
		parseDatapackFile("data/stats/enchantHPBonus.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _armorHPBonuses.size() + " Enchant HP Bonuses!");
	}
	
	public final int getHPBonus(L2ItemInstance item)
	{
		final List<Integer> values = _armorHPBonuses.get(item.getItem().getItemGradeSPlus());
		if ((values == null) || values.isEmpty() || (item.getOlyEnchantLevel() <= 0))
		{
			return 0;
		}
		
		final int bonus = values.get(Math.min(item.getOlyEnchantLevel(), values.size()) - 1);
		if (item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
		{
			return (int) (bonus * fullArmorModifier);
		}
		return bonus;
	}
	
	public static final EnchantItemHPBonusParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemHPBonusParser _instance = new EnchantItemHPBonusParser();
	}
}