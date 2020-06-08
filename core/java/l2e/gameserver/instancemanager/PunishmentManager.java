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
package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.model.holders.PunishmentHolder;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTask;
import l2e.gameserver.model.punishment.PunishmentType;

public class PunishmentManager
{
	private static final Logger _log = Logger.getLogger(PunishmentManager.class.getName());
	
	private final Map<PunishmentAffect, PunishmentHolder> _tasks = new ConcurrentHashMap<>();
	
	protected PunishmentManager()
	{
		load();
	}
	
	private void load()
	{
		for (PunishmentAffect affect : PunishmentAffect.values())
		{
			_tasks.put(affect, new PunishmentHolder());
		}
		
		int initiated = 0;
		int expired = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement st = con.createStatement();
			ResultSet rset = st.executeQuery("SELECT * FROM punishments"))
		{
			while (rset.next())
			{
				final int id = rset.getInt("id");
				final String key = rset.getString("key");
				final PunishmentAffect affect = PunishmentAffect.getByName(rset.getString("affect"));
				final PunishmentType type = PunishmentType.getByName(rset.getString("type"));
				final long expirationTime = rset.getLong("expiration");
				final String reason = rset.getString("reason");
				final String punishedBy = rset.getString("punishedBy");
				if ((type != null) && (affect != null))
				{
					if ((expirationTime > 0) && (System.currentTimeMillis() > expirationTime)) // expired task.
					{
						expired++;
					}
					else
					{
						initiated++;
						_tasks.get(affect).addPunishment(new PunishmentTask(id, key, affect, type, expirationTime, reason, punishedBy, true));
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error while loading punishments: ", e);
		}
		
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + initiated + " active and " + expired + " expired punishments.");
	}
	
	public void startPunishment(PunishmentTask task)
	{
		_tasks.get(task.getAffect()).addPunishment(task);
	}
	
	public void stopPunishment(Object key, PunishmentAffect affect, PunishmentType type)
	{
		final PunishmentTask task = getPunishment(key, affect, type);
		if (task != null)
		{
			_tasks.get(affect).stopPunishment(task);
		}
	}
	
	public boolean hasPunishment(Object key, PunishmentAffect affect, PunishmentType type)
	{
		final PunishmentHolder holder = _tasks.get(affect);
		return holder.hasPunishment(String.valueOf(key), type);
	}
	
	public long getPunishmentExpiration(Object key, PunishmentAffect affect, PunishmentType type)
	{
		final PunishmentTask p = getPunishment(key, affect, type);
		return p != null ? p.getExpirationTime() : 0;
	}
	
	private PunishmentTask getPunishment(Object key, PunishmentAffect affect, PunishmentType type)
	{
		return _tasks.get(affect).getPunishment(String.valueOf(key), type);
	}
	
	public static final PunishmentManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PunishmentManager _instance = new PunishmentManager();
	}
}