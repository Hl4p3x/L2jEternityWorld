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

import java.util.Collection;

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo;
import l2e.gameserver.network.serverpackets.ServerObjectInfo;

public class L2WaterZone extends L2ZoneType
{
	public L2WaterZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(ZoneId.WATER, true);
		
		if (character.isPlayer())
		{
			L2PcInstance player = character.getActingPlayer();
			if (player.isTransformed() && !player.getTransformation().canSwim())
			{
				character.stopTransformation(true);
			}
			else
			{
				player.broadcastUserInfo();
			}
		}
		else if (character.isNpc())
		{
			Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
			
			for (L2PcInstance player : plrs)
			{
				if (character.getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
				}
				else
				{
					player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) character, player));
				}
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(ZoneId.WATER, false);
		
		if (character.isPlayer())
		{
			character.getActingPlayer().broadcastUserInfo();
		}
		else if (character.isNpc())
		{
			Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
			for (L2PcInstance player : plrs)
			{
				if (character.getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
				}
				else
				{
					player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) character, player));
				}
			}
		}
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}