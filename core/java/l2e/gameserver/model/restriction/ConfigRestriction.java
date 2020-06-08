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
package l2e.gameserver.model.restriction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.data.sql.CharColorHolder;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class ConfigRestriction extends AbstractRestriction
{
	private static final Logger _log = Logger.getLogger(ConfigRestriction.class.getName());
	
	private static final String STATUS_DATA_GET = "SELECT hero FROM characters_custom_data WHERE charId = ?";
	
	private static final class SingletonHolder
	{
		protected static final ConfigRestriction _instance = new ConfigRestriction();
	}
	
	public static ConfigRestriction getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		restoreCustomStatus(activeChar);
		CharColorHolder.getInstance().process(activeChar);
	}
	
	private static void restoreCustomStatus(L2PcInstance activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int hero = 0;
			PreparedStatement statement = con.prepareStatement(STATUS_DATA_GET);
			statement.setInt(1, activeChar.getObjectId());
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				hero = rset.getInt("hero");
			}
			rset.close();
			statement.close();
			if (hero > 0)
			{
				activeChar.setHero(true);
			}
		}
		catch (Exception e)
		{
			_log.warning("Error: Could not restore char custom data info: " + e);
		}
	}
}