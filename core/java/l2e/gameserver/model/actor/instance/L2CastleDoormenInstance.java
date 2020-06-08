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
package l2e.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.clanhall.SiegableHall;

public class L2CastleDoormenInstance extends L2DoormenInstance
{
	public L2CastleDoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
		setInstanceType(InstanceType.L2CastleDoormenInstance);
	}
	
	@Override
	protected final void openDoors(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens())
		{
			if(getConquerableHall() != null)
				getConquerableHall().openCloseDoor(Integer.parseInt(st.nextToken()), true);
			else
				getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
		}
	}
	
	@Override
	protected final void closeDoors(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens())
		{
			if(getConquerableHall() != null)
				getConquerableHall().openCloseDoor(Integer.parseInt(st.nextToken()), false);
			else
				getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
		}
	}
	
	@Override
	protected final boolean isOwnerClan(L2PcInstance player)
	{
		if (player.getClan() != null)
		{
			if(getConquerableHall() != null)
			{
				if (player.getClanId() == getConquerableHall().getOwnerId()
						&& (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
					return true;
			}
			else if(getCastle() != null)
			{
				if (player.getClanId() == getCastle().getOwnerId()
						&& (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
					return true;
			}
		}
		return false;
	}
	
	@Override
	protected final boolean isUnderSiege()
	{
		SiegableHall hall = getConquerableHall();
		if(hall != null)
			return hall.isInSiege();
		return getCastle().getZone().isActive();
	}
}