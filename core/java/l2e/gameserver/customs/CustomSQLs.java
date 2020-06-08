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
package l2e.gameserver.customs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class CustomSQLs
{
	private static final Logger _log = Logger.getLogger(CustomSQLs.class.getName());
	
	static String DATA_INSERT = "REPLACE INTO characters_custom_data (charId, char_name, hero) VALUES (?,?,?)";
	static String DATA_DELETE = "UPDATE characters_custom_data SET hero = ? WHERE charId = ?";
	
	public static void updateDatabase(L2PcInstance player, boolean newHero)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (player == null)
			{
				return;
			}
			
			String charName = player.getName();
			int charId = player.getObjectId();
			boolean insert = newHero;
			
			PreparedStatement stmt = con.prepareStatement(insert ? DATA_INSERT : DATA_DELETE);
			
			if (newHero)
			{
				stmt.setInt(1, charId);
				stmt.setString(2, charName);
				stmt.setInt(3, 1);
				stmt.execute();
				stmt.close();
			}
			else
			{
				stmt.setInt(1, newHero ? 0 : 0);
				stmt.setInt(2, charId);
				stmt.execute();
				stmt.close();
			}
		}
		catch (Exception e)
		{
			_log.warning("Error: could not update database");
		}
	}
}