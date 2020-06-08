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
package l2e.gameserver.handler.skillhandlers;

import l2e.Config;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.L2FishingZone;
import l2e.gameserver.model.zone.type.L2HotSpringZone;
import l2e.gameserver.model.zone.type.L2WaterZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

public class Fishing implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.FISHING
	};
	
	@SuppressWarnings("null")
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!activeChar.isPlayer())
		{
			return;
		}
		
		final L2PcInstance player = activeChar.getActingPlayer();
		
		if (!Config.ALLOWFISHING && !player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			player.sendMessage("Fishing server is currently offline");
			return;
		}
		if (player.isFishing())
		{
			if (player.getFishCombat() != null)
			{
				player.getFishCombat().doDie(false);
			}
			else
			{
				player.endFishing(false);
			}
			
			player.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED);
			return;
		}
		L2Weapon weaponItem = player.getActiveWeaponItem();
		if (((weaponItem == null) || (weaponItem.getItemType() != L2WeaponType.FISHINGROD)))
		{
			player.sendPacket(SystemMessageId.FISHING_POLE_NOT_EQUIPPED);
			return;
		}
		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
			player.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING);
			return;
		}
		player.setLure(lure);
		L2ItemInstance lure2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		
		if ((lure2 == null) || (lure2.getCount() < 1))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
			return;
		}
		
		if (!player.isGM())
		{
			if (player.isInBoat())
			{
				player.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT);
				return;
			}
			
			if (player.isInCraftMode() || player.isInStoreMode())
			{
				player.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
				return;
			}
			
			if (player.isInsideZone(ZoneId.WATER))
			{
				player.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER);
				return;
			}
			
			if (player.isInsideZone(ZoneId.PEACE))
			{
				player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
				return;
			}
		}
		boolean isHotSpringZone = false;
		int rnd = Rnd.get(150) + 50;
		double angle = Util.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle);
		double sin = Math.sin(radian);
		double cos = Math.cos(radian);
		int x = player.getX() + (int) (cos * rnd);
		int y = player.getY() + (int) (sin * rnd);
		int z = player.getZ() + 50;
		
		L2FishingZone aimingTo = null;
		L2WaterZone water = null;
		L2HotSpringZone hszone = null;
		boolean canFish = false;
		for (L2ZoneType zone : ZoneManager.getInstance().getZones(x, y))
		{
			if (zone instanceof L2FishingZone)
			{
				aimingTo = (L2FishingZone) zone;
				continue;
			}
			if (zone instanceof L2WaterZone)
			{
				water = (L2WaterZone) zone;
			}
			if (zone instanceof L2HotSpringZone)
			{
				hszone = (L2HotSpringZone) zone;
				continue;
			}
		}
		if (aimingTo != null)
		{
			if (Config.GEODATA)
			{
				if (!GeoClient.getInstance().canSeeTarget(player, x, y, z + 50, activeChar.isFlying()))
				{
					if (water != null)
					{
						if (GeoClient.getInstance().getHeight(x, y, z) < water.getWaterZ())
						{
							if (hszone != null)
							{
								isHotSpringZone = true;
								
								z = water.getWaterZ() + 10;
								canFish = true;
							}
							else
							{
								z = water.getWaterZ() + 10;
								canFish = true;
							}
						}
					}
					else
					{
						if (GeoClient.getInstance().getHeight(x, y, z) < aimingTo.getWaterZ())
						{
							if (hszone != null)
							{
								isHotSpringZone = true;
								
								z = aimingTo.getWaterZ() + 10;
								z = hszone.getWaterZ() + 10;
								canFish = true;
							}
							else
							{
								z = aimingTo.getWaterZ() + 10;
								z = hszone.getWaterZ() + 10;
								canFish = true;
							}
						}
					}
				}
			}
			else
			{
				if (hszone != null)
				{
					isHotSpringZone = true;
					
					if (water != null)
					{
						z = water.getWaterZ() + 10;
					}
					else
					{
						z = hszone.getWaterZ() + 10;
						z = aimingTo.getWaterZ() + 10;
					}
				}
				else
				{
					if (water != null)
					{
						z = water.getWaterZ() + 10;
					}
					else
					{
						z = hszone.getWaterZ() + 10;
						z = aimingTo.getWaterZ() + 10;
					}
				}
				canFish = true;
			}
		}
		if (!canFish)
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
			if (!player.isGM())
			{
				return;
			}
		}
		lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(lure2);
		player.sendPacket(iu);
		player.startFishing(x, y, z, isHotSpringZone);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}