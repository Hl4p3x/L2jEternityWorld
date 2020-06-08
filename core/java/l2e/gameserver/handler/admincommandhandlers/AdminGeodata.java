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
package l2e.gameserver.handler.admincommandhandlers;

import l2e.Config;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class AdminGeodata implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_geo_z",
		"admin_geo_type",
		"admin_geo_nswe",
		"admin_geo_los",
		"admin_geo_position"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.GEODATA)
		{
			activeChar.sendMessage("Geo Engine is Turned Off!");
			return true;
		}
		
		if (command.equals("admin_geo_z"))
		{
			activeChar.sendMessage("GeoEngine: Geo_Z = " + GeoClient.getInstance().getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ()) + " Loc_Z = " + activeChar.getZ());
		}
		else if (command.equals("admin_geo_type"))
		{
			short type = GeoClient.getInstance().getType(activeChar.getX(), activeChar.getY());
			activeChar.sendMessage("GeoEngine: Geo_Type = " + type);
			int height = GeoClient.getInstance().getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			activeChar.sendMessage("GeoEngine: height = " + height);
		}
		else if (command.equals("admin_geo_nswe"))
		{
			String result = "";
			short nswe = GeoClient.getInstance().getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			if ((nswe & 8) == 0)
			{
				result += " N";
			}
			if ((nswe & 4) == 0)
			{
				result += " S";
			}
			if ((nswe & 2) == 0)
			{
				result += " W";
			}
			if ((nswe & 1) == 0)
			{
				result += " E";
			}
			activeChar.sendMessage("GeoEngine: Geo_NSWE -> " + nswe + "->" + result);
		}
		else if (command.equals("admin_geo_los"))
		{
			if (activeChar.getTarget() != null)
			{
				if (GeoClient.getInstance().canSeeTarget(activeChar, activeChar.getTarget()))
				{
					activeChar.sendMessage("GeoEngine: Can See Target");
				}
				else
				{
					activeChar.sendMessage("GeoEngine: Can't See Target");
				}
				
			}
			else
			{
				activeChar.sendMessage("None Target!");
			}
		}
		else if (command.equals("admin_geo_position"))
		{
			activeChar.sendMessage("GeoEngine: Your current position: ");
			activeChar.sendMessage(".... world coords: x: " + activeChar.getX() + " y: " + activeChar.getY() + " z: " + activeChar.getZ());
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}