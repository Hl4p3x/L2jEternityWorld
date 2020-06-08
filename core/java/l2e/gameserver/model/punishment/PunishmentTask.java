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
package l2e.gameserver.model.punishment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.handler.IPunishmentHandler;
import l2e.gameserver.handler.PunishmentHandler;
import l2e.gameserver.instancemanager.PunishmentManager;

public class PunishmentTask implements Runnable
{
	protected static final Logger _log = Logger.getLogger(PunishmentTask.class.getName());
	
	private static final String INSERT_QUERY = "INSERT INTO punishments (`key`, `affect`, `type`, `expiration`, `reason`, `punishedBy`) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_QUERY = "UPDATE punishments SET expiration = ? WHERE id = ?";
	
	private int _id;
	private final String _key;
	private final PunishmentAffect _affect;
	private final PunishmentType _type;
	private final long _expirationTime;
	private final String _reason;
	private final String _punishedBy;
	private boolean _isStored;
	private ScheduledFuture<?> _task = null;
	
	public PunishmentTask(Object key, PunishmentAffect affect, PunishmentType type, long expirationTime, String reason, String punishedBy)
	{
		this(0, key, affect, type, expirationTime, reason, punishedBy, false);
	}
	
	public PunishmentTask(int id, Object key, PunishmentAffect affect, PunishmentType type, long expirationTime, String reason, String punishedBy, boolean isStored)
	{
		_id = id;
		_key = String.valueOf(key);
		_affect = affect;
		_type = type;
		_expirationTime = expirationTime;
		_reason = reason;
		_punishedBy = punishedBy;
		_isStored = isStored;
		
		startPunishment();
	}

	public Object getKey()
	{
		return _key;
	}

	public PunishmentAffect getAffect()
	{
		return _affect;
	}
	
	public PunishmentType getType()
	{
		return _type;
	}
	
	public final long getExpirationTime()
	{
		return _expirationTime;
	}
	
	public String getReason()
	{
		return _reason;
	}
	
	public String getPunishedBy()
	{
		return _punishedBy;
	}
	
	public boolean isStored()
	{
		return _isStored;
	}
	
	public final boolean isExpired()
	{
		return (_expirationTime > 0) && (System.currentTimeMillis() > _expirationTime);
	}
	
	private void startPunishment()
	{
		if (isExpired())
		{
			return;
		}
		
		onStart();
		if (_expirationTime > 0)
		{
			_task = ThreadPoolManager.getInstance().scheduleGeneral(this, (_expirationTime - System.currentTimeMillis()));
		}
	}
	
	public final void stopPunishment()
	{
		abortTask();
		onEnd();
	}
	
	private void abortTask()
	{
		if (_task != null)
		{
			if (!_task.isCancelled() && !_task.isDone())
			{
				_task.cancel(false);
			}
			_task = null;
		}
	}

	private void onStart()
	{
		if (!_isStored)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement st = con.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS))
			{
				st.setString(1, _key);
				st.setString(2, _affect.name());
				st.setString(3, _type.name());
				st.setLong(4, _expirationTime);
				st.setString(5, _reason);
				st.setString(6, _punishedBy);
				st.execute();
				try (ResultSet rset = st.getGeneratedKeys())
				{
					if (rset.next())
					{
						_id = rset.getInt(1);
					}
				}
				_isStored = true;
			}
			catch (SQLException e)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't store punishment task for: " + _affect + " " + _key, e);
			}
		}
		
		final IPunishmentHandler handler = PunishmentHandler.getInstance().getHandler(_type);
		if (handler != null)
		{
			handler.onStart(this);
		}
	}
	
	private void onEnd()
	{
		if (_isStored)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement st = con.prepareStatement(UPDATE_QUERY))
			{
				st.setLong(1, System.currentTimeMillis());
				st.setLong(2, _id);
				st.execute();
			}
			catch (SQLException e)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't update punishment task for: " + _affect + " " + _key + " id: " + _id, e);
			}
		}
		
		final IPunishmentHandler handler = PunishmentHandler.getInstance().getHandler(_type);
		if (handler != null)
		{
			handler.onEnd(this);
		}
	}
	
	@Override
	public final void run()
	{
		PunishmentManager.getInstance().stopPunishment(_key, _affect, _type);
	}
}