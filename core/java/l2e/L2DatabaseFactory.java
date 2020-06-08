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
package l2e;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.ThreadPoolManager;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class L2DatabaseFactory
{
	protected static final Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());
	
	private static L2DatabaseFactory _instance;
	private static volatile ScheduledExecutorService _executor;
	private ComboPooledDataSource _source;
	
	public L2DatabaseFactory() throws SQLException
	{
		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 2)
			{
				Config.DATABASE_MAX_CONNECTIONS = 2;
				_log.warning("A minimum of " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}
			_source = new ComboPooledDataSource();
			_source.setAutoCommitOnClose(true);
			
			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(Math.max(10, Config.DATABASE_MAX_CONNECTIONS));
			
			_source.setAcquireRetryAttempts(0);
			_source.setAcquireRetryDelay(500);
			_source.setCheckoutTimeout(0);
			_source.setAcquireIncrement(5);
			
			_source.setAutomaticTestTable("connection_test_table");
			_source.setTestConnectionOnCheckin(false);
			
			_source.setIdleConnectionTestPeriod(3600);
			_source.setMaxIdleTime(Config.DATABASE_MAX_IDLE_TIME);
			
			_source.setMaxStatementsPerConnection(100);
			
			_source.setBreakAfterAcquireFailure(false);
			_source.setDriverClass(Config.DATABASE_DRIVER);
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUser(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD);
			
			_source.getConnection().close();
			
			if (Config.DEBUG)
			{
				_log.fine("Database Connection Working");
			}
		}
		catch (SQLException x)
		{
			if (Config.DEBUG)
			{
				_log.fine("Database Connection FAILED");
			}
			throw x;
		}
		catch (Exception e)
		{
			if (Config.DEBUG)
			{
				_log.fine("Database Connection FAILED");
			}
			throw new SQLException("Could not init DB connection:" + e.getMessage());
		}
	}
	
	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			_log.log(Level.INFO, "", e);
		}
		try
		{
			_source = null;
		}
		catch (Exception e)
		{
			_log.log(Level.INFO, "", e);
		}
	}
	
	public static L2DatabaseFactory getInstance() throws SQLException
	{
		synchronized (L2DatabaseFactory.class)
		{
			if (_instance == null)
			{
				_instance = new L2DatabaseFactory();
			}
		}
		return _instance;
	}
	
	public Connection getConnection()
	{
		Connection con = null;
		while (con == null)
		{
			try
			{
				con = _source.getConnection();
				if (Server.serverMode == Server.MODE_GAMESERVER)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ConnectionCloser(con, new RuntimeException()), Config.CONNECTION_CLOSE_TIME);
				}
				else
				{
					getExecutor().schedule(new ConnectionCloser(con, new RuntimeException()), Config.CONNECTION_CLOSE_TIME, TimeUnit.MILLISECONDS);
				}
			}
			catch (SQLException e)
			{
				_log.log(Level.WARNING, "L2DatabaseFactory: getConnection() failed, trying again " + e.getMessage(), e);
			}
		}
		return con;
	}
	
	private static class ConnectionCloser implements Runnable
	{
		private final Connection c;
		private final RuntimeException exp;
		
		public ConnectionCloser(Connection con, RuntimeException e)
		{
			c = con;
			exp = e;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (!c.isClosed())
				{
					_log.log(Level.WARNING, "Unclosed connection! Trace: " + exp.getStackTrace()[1], exp);
				}
			}
			catch (SQLException e)
			{
				_log.log(Level.WARNING, "", e);
			}
		}
	}
	
	private static ScheduledExecutorService getExecutor()
	{
		if (_executor == null)
		{
			synchronized (L2DatabaseFactory.class)
			{
				if (_executor == null)
				{
					_executor = Executors.newSingleThreadScheduledExecutor();
				}
			}
		}
		return _executor;
	}
	
	public int getBusyConnectionCount() throws SQLException
	{
		return _source.getNumBusyConnectionsDefaultUser();
	}
	
	public int getIdleConnectionCount() throws SQLException
	{
		return _source.getNumIdleConnectionsDefaultUser();
	}
}