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
package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class Couple
{
	private static final Logger _log = Logger.getLogger(Couple.class.getName());
	
	private int _Id = 0;
	private int _player1Id = 0;
	private int _player2Id = 0;
	private boolean _maried = false;
	private Calendar _affiancedDate;
	private Calendar _weddingDate;
	
	public Couple(int coupleId)
	{
		_Id = coupleId;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM mods_wedding WHERE id = ?");
			statement.setInt(1, _Id);
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				_player1Id = rs.getInt("player1Id");
				_player2Id = rs.getInt("player2Id");
				_maried = rs.getBoolean("married");
				
				_affiancedDate = Calendar.getInstance();
				_affiancedDate.setTimeInMillis(rs.getLong("affianceDate"));
				
				_weddingDate = Calendar.getInstance();
				_weddingDate.setTimeInMillis(rs.getLong("weddingDate"));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Couple.load(): " + e.getMessage(), e);
		}
	}
	
	public Couple(L2PcInstance player1, L2PcInstance player2)
	{
		int _tempPlayer1Id = player1.getObjectId();
		int _tempPlayer2Id = player2.getObjectId();
		
		_player1Id = _tempPlayer1Id;
		_player2Id = _tempPlayer2Id;
		
		_affiancedDate = Calendar.getInstance();
		_affiancedDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		
		_weddingDate = Calendar.getInstance();
		_weddingDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			_Id = IdFactory.getInstance().getNextId();
			PreparedStatement statement = con.prepareStatement("INSERT INTO mods_wedding (id, player1Id, player2Id, married, affianceDate, weddingDate) VALUES (?, ?, ?, ?, ?, ?)");
			statement.setInt(1, _Id);
			statement.setInt(2, _player1Id);
			statement.setInt(3, _player2Id);
			statement.setBoolean(4, false);
			statement.setLong(5, _affiancedDate.getTimeInMillis());
			statement.setLong(6, _weddingDate.getTimeInMillis());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not create couple: " + e.getMessage(), e);
		}
	}
	
	public void marry()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE mods_wedding set married = ?, weddingDate = ? where id = ?");
			statement.setBoolean(1, true);
			_weddingDate = Calendar.getInstance();
			statement.setLong(2, _weddingDate.getTimeInMillis());
			statement.setInt(3, _Id);
			statement.execute();
			statement.close();
			_maried = true;
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not marry: " + e.getMessage(), e);
		}
	}
	
	public void divorce()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM mods_wedding WHERE id=?");
			statement.setInt(1, _Id);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Couple.divorce(): " + e.getMessage(), e);
		}
	}
	
	public final int getId()
	{
		return _Id;
	}
	
	public final int getPlayer1Id()
	{
		return _player1Id;
	}
	
	public final int getPlayer2Id()
	{
		return _player2Id;
	}
	
	public final boolean getMaried()
	{
		return _maried;
	}
	
	public final Calendar getAffiancedDate()
	{
		return _affiancedDate;
	}
	
	public final Calendar getWeddingDate()
	{
		return _weddingDate;
	}
}