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
package l2e.gameserver.model.zone.type;

import l2e.Config;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.ZoneId;

public class L2PeaceZone extends L2ZoneType
{
	public L2PeaceZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		L2PcInstance player = character.getActingPlayer();
		
		if (character.isPlayer())
		{
			if (player.isCombatFlagEquipped() && TerritoryWarManager.getInstance().isTWInProgress())
			{
				TerritoryWarManager.getInstance().dropCombatFlag(player, false, true);
			}
			
			if ((player.getSiegeState() != 0) && (Config.PEACE_ZONE_MODE == 1))
			{
				return;
			}
		}
		
		if (Config.PEACE_ZONE_MODE != 2)
		{
			character.setInsideZone(ZoneId.PEACE, true);
			
			if (character.isPlayer())
			{
				if (player != null)
				{
					player.pauseAdventTask();
				}
				
				if (Config.SPEED_UP_RUN)
				{
					if (player != null)
					{
						player.broadcastUserInfo();
					}
				}
			}
		}
		
		if (!getAllowStore())
		{
			character.setInsideZone(ZoneId.NO_STORE, true);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (Config.PEACE_ZONE_MODE != 2)
		{
			character.setInsideZone(ZoneId.PEACE, false);
		}
		
		if (Config.SPEED_UP_RUN)
		{
			if (character.isPlayer())
			{
				character.getActingPlayer().broadcastUserInfo();
			}
		}
		
		if (!getAllowStore())
		{
			character.setInsideZone(ZoneId.NO_STORE, false);
		}
	}
}