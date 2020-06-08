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
package l2e.gameserver.model.conditions;

import l2e.gameserver.data.xml.PetsParser;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.stats.Env;

public class ConditionPetType extends Condition
{
	private final int petType;
	
	public ConditionPetType(int petType)
	{
		this.petType = petType;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.getCharacter() instanceof L2PetInstance))
		{
			return false;
		}
		
		int npcid = ((L2Summon) env.getCharacter()).getId();
		
		if (PetsParser.isStrider(npcid) && (petType == L2Item.STRIDER))
		{
			return true;
		}
		else if (PetsParser.isGrowUpWolfGroup(npcid) && (petType == L2Item.GROWN_UP_WOLF_GROUP))
		{
			return true;
		}
		else if (PetsParser.isHatchlingGroup(npcid) && (petType == L2Item.HATCHLING_GROUP))
		{
			return true;
		}
		else if (PetsParser.isAllWolfGroup(npcid) && (petType == L2Item.ALL_WOLF_GROUP))
		{
			return true;
		}
		else if (PetsParser.isBabyPetGroup(npcid) && (petType == L2Item.BABY_PET_GROUP))
		{
			return true;
		}
		else if (PetsParser.isUpgradeBabyPetGroup(npcid) && (petType == L2Item.UPGRADE_BABY_PET_GROUP))
		{
			return true;
		}
		else if (PetsParser.isItemEquipPetGroup(npcid) && (petType == L2Item.ITEM_EQUIP_PET_GROUP))
		{
			return true;
		}
		return false;
	}
}