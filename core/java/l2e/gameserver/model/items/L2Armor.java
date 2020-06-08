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
package l2e.gameserver.model.items;

import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.items.type.L2ArmorType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.util.StringUtil;

public final class L2Armor extends L2Item
{
	private SkillsHolder _enchant4Skill = null;
	private L2ArmorType _type;
	
	public L2Armor(StatsSet set)
	{
		super(set);
		_type = L2ArmorType.valueOf(set.getString("armor_type", "none").toUpperCase());
		
		int _bodyPart = getBodyPart();
		if ((_bodyPart == L2Item.SLOT_NECK) || ((_bodyPart & L2Item.SLOT_L_EAR) != 0) || ((_bodyPart & L2Item.SLOT_L_FINGER) != 0) || ((_bodyPart & L2Item.SLOT_R_BRACELET) != 0) || ((_bodyPart & L2Item.SLOT_L_BRACELET) != 0))
		{
			_type1 = L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
			_type2 = L2Item.TYPE2_ACCESSORY;
		}
		else
		{
			if ((_type == L2ArmorType.NONE) && (getBodyPart() == L2Item.SLOT_L_HAND))
			{
				_type = L2ArmorType.SHIELD;
			}
			_type1 = L2Item.TYPE1_SHIELD_ARMOR;
			_type2 = L2Item.TYPE2_SHIELD_ARMOR;
		}
		
		String skill = set.getString("enchant4_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch (Exception nfe)
				{
					_log.info(StringUtil.concat("> Couldnt parse ", skill, " in armor enchant skills! item ", toString()));
				}
				if ((id > 0) && (level > 0))
				{
					_enchant4Skill = new SkillsHolder(id, level);
				}
			}
		}
	}
	
	@Override
	public L2ArmorType getItemType()
	{
		return _type;
	}
	
	@Override
	public final int getItemMask()
	{
		return getItemType().mask();
	}
	
	@Override
	public L2Skill getEnchant4Skill()
	{
		if (_enchant4Skill == null)
		{
			return null;
		}
		return _enchant4Skill.getSkill();
	}
}