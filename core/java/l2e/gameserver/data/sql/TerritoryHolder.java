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
package l2e.gameserver.data.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.L2Territory;

public class TerritoryHolder
{
	private static final Logger _log = Logger.getLogger(TerritoryHolder.class.getName());
	
	private static final Map<Integer, L2Territory> _territory = new HashMap<>();
	
	protected TerritoryHolder()
	{
		load();
	}
	
	public int[] getRandomPoint(int terr)
	{
		if (!_territory.containsKey(terr))
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": ahrung, territory=" + terr + " not found, check table locations by loc_id in spawnlist !!!");
			return new int[0];
		}
		L2Territory tr = _territory.get(terr);
		int[] tr1;
		for (int i = 0; i < 100; i++)
		{
			if (i == 80)
			{
				_log.warning(getClass().getSimpleName() + ": Heavy territory=" + terr + ", need manual correction");
			}
			
			tr1 = tr.getRandomPoint();
			
			if (Config.GEODATA)
			{
				int tempz = GeoClient.getInstance().getSpawnHeight(tr1[0], tr1[1], tr1[2]);
				if (tr.getZmin() != tr.getZmax())
				{
					if ((tempz < (tr.getZmin() - 32)) || (tempz > tr.getZmax()) || (tr.getZmin() > tr.getZmax()))
					{
						continue;
					}
				}
				else if ((tempz < (tr.getZmin() - 200)) || (tempz > (tr.getZmax() + 200)))
				{
					continue;
				}
				
				tr1[2] = tempz;
				
				if (GeoClient.getInstance().getNSWE(tr1[0], tr1[1], tr1[2]) != 15)
				{
					continue;
				}
				
				return tr1;
			}
			return tr1;
		}
		_log.warning(getClass().getSimpleName() + ": Heavy territory=" + terr + ", need manual correction, not found true random point");
		
		return tr.getRandomPoint();
	}
	
	public int getProcMax(int terr)
	{
		return _territory.get(terr).getProcMax();
	}
	
	public void load()
	{
		_territory.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement stmt = con.createStatement();
			ResultSet rset = stmt.executeQuery("SELECT * FROM locations WHERE loc_id>0"))
		{
			while (rset.next())
			{
				int terrId = rset.getInt("loc_id");
				L2Territory terr = _territory.get(terrId);
				if (terr == null)
				{
					terr = new L2Territory(terrId);
					_territory.put(terrId, terr);
				}
				terr.add(rset.getInt("loc_x"), rset.getInt("loc_y"), rset.getInt("loc_zmin"), rset.getInt("loc_zmax"), rset.getInt("proc"));
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + _territory.size() + " territories from database.");
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Failed to load territories from database!", e);
		}
	}
	
	public static TerritoryHolder getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TerritoryHolder _instance = new TerritoryHolder();
	}
}