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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.L2DatabaseFactory;
import l2e.gameserver.model.PcColorContainer;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class CharColorHolder
{
	private final static Logger _log = Logger.getLogger(CharColorHolder.class.getName());
	
	private final FastMap<String, PcColorContainer> _pcColors = new FastMap<>();
	
	protected CharColorHolder()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Vector<String> deleteNames = new Vector<>();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM `character_colors`");
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				long regTime = rs.getLong("reg_time"), time = rs.getLong("time");
				String charName = rs.getString("char_name");
				int color = rs.getInt("color");
				if ((time == 0) || ((regTime + time) > System.currentTimeMillis()))
				{
					_pcColors.put(charName, new PcColorContainer(color, regTime, time));
				}
				else
				{
					deleteNames.add(charName);
				}
			}
			ps.close();
			rs.close();
			for (String deleteName : deleteNames)
			{
				PreparedStatement psDel = con.prepareStatement("DELETE FROM `character_colors` WHERE `char_name`=?");
				psDel.setString(1, deleteName);
				psDel.executeUpdate();
				psDel.close();
			}
			_log.info(getClass().getSimpleName() + ": " + _pcColors.size() + " Expired/Deleted: " + deleteNames.size());
			deleteNames.clear();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while loading data from DB!");
		}
	}
	
	public static CharColorHolder getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public synchronized void process(L2PcInstance activeChar)
	{
		PcColorContainer colorContainer = _pcColors.get(activeChar.getName());
		if (colorContainer == null)
		{
			return;
		}
		long time = colorContainer.getTime();
		if ((time == 0) || ((colorContainer.getRegTime() + time) > System.currentTimeMillis()))
		{
			activeChar.getAppearance().setNameColor(colorContainer.getColor());
			activeChar.broadcastUserInfo();
		}
		else
		{
			delete(activeChar.getName());
		}
	}
	
	public synchronized void add(L2PcInstance activeChar, int color, long regTime, long time)
	{
		String charName = activeChar.getName();
		PcColorContainer colorContainer = _pcColors.get(charName);
		if (colorContainer != null)
		{
			if (!delete(charName))
			{
				return;
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement psIns = con.prepareStatement("INSERT INTO `character_colors` VALUES (?,?,?,?)");
			psIns.setString(1, charName);
			psIns.setInt(2, color);
			psIns.setLong(3, regTime);
			psIns.setLong(4, time);
			psIns.executeUpdate();
			psIns.close();
			_pcColors.put(activeChar.getName(), new PcColorContainer(color, regTime, time));
			activeChar.getAppearance().setNameColor(color);
			activeChar.broadcastUserInfo();
			activeChar.sendMessage("Your name color has been changed!");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error while add " + charName + "'s color to DB!");
		}
	}
	
	public synchronized boolean delete(String charName)
	{
		PcColorContainer colorContainer = _pcColors.get(charName);
		if (colorContainer == null)
		{
			return false;
		}
		
		colorContainer = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement psDel = con.prepareStatement("DELETE FROM `character_colors` WHERE `char_name`=?");
			psDel.setString(1, charName);
			psDel.executeUpdate();
			psDel.close();
			_pcColors.remove(charName);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error while delete " + charName + "'s color from DB!");
			return false;
		}
		return true;
	}
	
	private static class SingletonHolder
	{
		protected static final CharColorHolder _instance = new CharColorHolder();
	}
}