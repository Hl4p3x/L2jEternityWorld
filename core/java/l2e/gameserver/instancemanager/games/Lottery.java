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
package l2e.gameserver.instancemanager.games;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Rnd;

public class Lottery
{
	public static final long SECOND = 1000;
	public static final long MINUTE = 60000;
	
	protected static final Logger _log = Logger.getLogger(Lottery.class.getName());
	
	private static final String INSERT_LOTTERY = "INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)";
	private static final String UPDATE_PRICE = "UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?";
	private static final String UPDATE_LOTTERY = "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?";
	private static final String SELECT_LAST_LOTTERY = "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1";
	private static final String SELECT_LOTTERY_ITEM = "SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?";
	private static final String SELECT_LOTTERY_TICKET = "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 and idnr = ?";
	
	protected int _number;
	protected long _prize;
	protected boolean _isSellingTickets;
	protected boolean _isStarted;
	protected long _enddate;
	
	protected Lottery()
	{
		_number = 1;
		_prize = Config.ALT_LOTTERY_PRIZE;
		_isSellingTickets = false;
		_isStarted = false;
		_enddate = System.currentTimeMillis();
		
		if (Config.ALLOW_LOTTERY)
			(new startLottery()).run();
	}
	
	public static Lottery getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public int getId()
	{
		return _number;
	}
	
	public long getPrize()
	{
		return _prize;
	}
	
	public long getEndDate()
	{
		return _enddate;
	}
	
	public void increasePrize(long count)
	{
		_prize += count;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(UPDATE_PRICE);
			statement.setLong(1, getPrize());
			statement.setLong(2, getPrize());
			statement.setInt(3, getId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Lottery: Could not increase current lottery prize: " + e.getMessage(), e);
		}
	}
	
	public boolean isSellableTickets()
	{
		return _isSellingTickets;
	}
	
	public boolean isStarted()
	{
		return _isStarted;
	}
	
	private class startLottery implements Runnable
	{
		protected startLottery()
		{
		}
		
		@Override
		public void run()
		{
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				Statement statement = con.createStatement();
				ResultSet rset = statement.executeQuery(SELECT_LAST_LOTTERY))
			{
				if (rset.next())
				{
					_number = rset.getInt("idnr");
					
					if (rset.getInt("finished") == 1)
					{
						_number++;
						_prize = rset.getLong("newprize");
					}
					else
					{
						_prize = rset.getLong("prize");
						_enddate = rset.getLong("enddate");
						
						if (_enddate <= System.currentTimeMillis() + 2 * MINUTE)
						{
							(new finishLottery()).run();
							return;
						}
						
						if (_enddate > System.currentTimeMillis())
						{
							_isStarted = true;
							ThreadPoolManager.getInstance().scheduleGeneral(new finishLottery(), _enddate - System.currentTimeMillis());
							
							if (_enddate > System.currentTimeMillis() + 12 * MINUTE)
							{
								_isSellingTickets = true;
								ThreadPoolManager.getInstance().scheduleGeneral(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 10 * MINUTE);
							}
							return;
						}
					}
				}
			}
			catch (SQLException e)
			{
				_log.log(Level.WARNING, "Lottery: Could not restore lottery data: " + e.getMessage(), e);
			}
			
			if (Config.DEBUG)
				_log.info("Lottery: Starting ticket sell for lottery #" + getId() + ".");
			_isSellingTickets = true;
			_isStarted = true;
			
			Announcements.getInstance().announceToAll("Lottery tickets are now available for Lucky Lottery #" + getId() + ".");
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(_enddate);
			finishtime.set(Calendar.MINUTE, 0);
			finishtime.set(Calendar.SECOND, 0);
			
			if (finishtime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			{
				finishtime.set(Calendar.HOUR_OF_DAY, 19);
				_enddate = finishtime.getTimeInMillis();
				_enddate += 604800000;
			}
			else
			{
				finishtime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				finishtime.set(Calendar.HOUR_OF_DAY, 19);
				_enddate = finishtime.getTimeInMillis();
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 10 * MINUTE);
			ThreadPoolManager.getInstance().scheduleGeneral(new finishLottery(), _enddate - System.currentTimeMillis());
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INSERT_LOTTERY))
			{
				statement.setInt(1, 1);
				statement.setInt(2, getId());
				statement.setLong(3, getEndDate());
				statement.setLong(4, getPrize());
				statement.setLong(5, getPrize());
				statement.execute();
			}
			catch (SQLException e)
			{
				_log.log(Level.WARNING, "Lottery: Could not store new lottery data: " + e.getMessage(), e);
			}
		}
	}
	
	private class stopSellingTickets implements Runnable
	{
		protected stopSellingTickets()
		{
		}
		
		@Override
		public void run()
		{
			if (Config.DEBUG)
				_log.info("Lottery: Stopping ticket sell for lottery #" + getId() + ".");
			_isSellingTickets = false;
			
			Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.LOTTERY_TICKET_SALES_TEMP_SUSPENDED));
		}
	}
	
	private class finishLottery implements Runnable
	{
		protected finishLottery()
		{
		}
		
		@Override
		public void run()
		{
			if (Config.DEBUG)
				_log.info("Lottery: Ending lottery #" + getId() + ".");
			
			int[] luckynums = new int[5];
			int luckynum = 0;
			
			for (int i = 0; i < 5; i++)
			{
				boolean found = true;
				
				while (found)
				{
					luckynum = Rnd.get(20) + 1;
					found = false;
					
					for (int j = 0; j < i; j++)
						if (luckynums[j] == luckynum)
							found = true;
				}
				
				luckynums[i] = luckynum;
			}
			
			if (Config.DEBUG)
				_log.info("Lottery: The lucky numbers are " + luckynums[0] + ", " + luckynums[1] + ", " + luckynums[2] + ", " + luckynums[3] + ", " + luckynums[4] + ".");
			
			int enchant = 0;
			int type2 = 0;
			
			for (int i = 0; i < 5; i++)
			{
				if (luckynums[i] < 17)
					enchant += Math.pow(2, luckynums[i] - 1);
				else
					type2 += Math.pow(2, luckynums[i] - 17);
			}
			
			if (Config.DEBUG)
				_log.info("Lottery: Encoded lucky numbers are " + enchant + ", " + type2);
			
			int count1 = 0;
			int count2 = 0;
			int count3 = 0;
			int count4 = 0;
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(SELECT_LOTTERY_ITEM))
			{
				statement.setInt(1, getId());
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						int curenchant = rset.getInt("enchant_level") & enchant;
						int curtype2 = rset.getInt("custom_type2") & type2;
						
						if (curenchant == 0 && curtype2 == 0)
							continue;
						
						int count = 0;
						
						for (int i = 1; i <= 16; i++)
						{
							int val = curenchant / 2;
							
							if (val != Math.round((double) curenchant / 2))
								count++;
							
							int val2 = curtype2 / 2;
							
							if (val2 != (double) curtype2 / 2)
								count++;
							
							curenchant = val;
							curtype2 = val2;
						}
						
						if (count == 5)
							count1++;
						else if (count == 4)
							count2++;
						else if (count == 3)
							count3++;
						else if (count > 0)
							count4++;
					}
				}
			}
			catch (SQLException e)
			{
				_log.log(Level.WARNING, "Lottery: Could restore lottery data: " + e.getMessage(), e);
			}
			
			long prize4 = count4 * Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
			long prize1 = 0;
			long prize2 = 0;
			long prize3 = 0;
			
			if (count1 > 0)
				prize1 = (long) ((getPrize() - prize4) * Config.ALT_LOTTERY_5_NUMBER_RATE / count1);
			
			if (count2 > 0)
				prize2 = (long) ((getPrize() - prize4) * Config.ALT_LOTTERY_4_NUMBER_RATE / count2);
			
			if (count3 > 0)
				prize3 = (long) ((getPrize() - prize4) * Config.ALT_LOTTERY_3_NUMBER_RATE / count3);
			
			if (Config.DEBUG)
			{
				_log.info("Lottery: " + count1 + " players with all FIVE numbers each win " + prize1 + ".");
				_log.info("Lottery: " + count2 + " players with FOUR numbers each win " + prize2 + ".");
				_log.info("Lottery: " + count3 + " players with THREE numbers each win " + prize3 + ".");
				_log.info("Lottery: " + count4 + " players with ONE or TWO numbers each win " + prize4 + ".");
			}
			
			long newprize = getPrize() - (prize1 + prize2 + prize3 + prize4);
			if (Config.DEBUG)
				_log.info("Lottery: Jackpot for next lottery is " + newprize + ".");
			
			SystemMessage sm;
			if (count1 > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER);
				sm.addNumber(getId());
				sm.addItemNumber(getPrize());
				sm.addItemNumber(count1);
				Announcements.getInstance().announceToAll(sm);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER);
				sm.addNumber(getId());
				sm.addItemNumber(getPrize());
				Announcements.getInstance().announceToAll(sm);
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_LOTTERY))
			{
				statement.setLong(1, getPrize());
				statement.setLong(2, newprize);
				statement.setInt(3, enchant);
				statement.setInt(4, type2);
				statement.setLong(5, prize1);
				statement.setLong(6, prize2);
				statement.setLong(7, prize3);
				statement.setInt(8, getId());
				statement.execute();
			}
			catch (SQLException e)
			{
				_log.log(Level.WARNING, "Lottery: Could not store finished lottery data: " + e.getMessage(), e);
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(new startLottery(), MINUTE);
			_number++;
			
			_isStarted = false;
		}
	}
	
	public int[] decodeNumbers(int enchant, int type2)
	{
		int res[] = new int[5];
		int id = 0;
		int nr = 1;
		
		while (enchant > 0)
		{
			int val = enchant / 2;
			if (val != Math.round((double) enchant / 2))
			{
				res[id++] = nr;
			}
			enchant /= 2;
			nr++;
		}
		
		nr = 17;
		
		while (type2 > 0)
		{
			int val = type2 / 2;
			if (val != (double) type2 / 2)
			{
				res[id++] = nr;
			}
			type2 /= 2;
			nr++;
		}
		
		return res;
	}
	
	public long[] checkTicket(L2ItemInstance item)
	{
		return checkTicket(item.getCustomType1(), item.getEnchantLevel(), item.getCustomType2());
	}
	
	public long[] checkTicket(int id, int enchant, int type2)
	{
		long res[] = { 0, 0 };
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_LOTTERY_TICKET))
		{
			statement.setInt(1, id);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					int curenchant = rset.getInt("number1") & enchant;
					int curtype2 = rset.getInt("number2") & type2;
					
					if (curenchant == 0 && curtype2 == 0)
					{
						return res;
					}
					
					int count = 0;
					
					for (int i = 1; i <= 16; i++)
					{
						int val = curenchant / 2;
						if (val != Math.round((double) curenchant / 2))
							count++;
						int val2 = curtype2 / 2;
						if (val2 != (double) curtype2 / 2)
							count++;
						curenchant = val;
						curtype2 = val2;
					}
					
					switch (count)
					{
						case 0:
							break;
						case 5:
							res[0] = 1;
							res[1] = rset.getLong("prize1");
							break;
						case 4:
							res[0] = 2;
							res[1] = rset.getLong("prize2");
							break;
						case 3:
							res[0] = 3;
							res[1] = rset.getLong("prize3");
							break;
						default:
							res[0] = 4;
							res[1] = 200;
					}
					
					if (Config.DEBUG)
						_log.warning("count: " + count + ", id: " + id + ", enchant: " + enchant + ", type2: " + type2);
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Lottery: Could not check lottery ticket #" + id + ": " + e.getMessage(), e);
		}
		return res;
	}
	
	private static class SingletonHolder
	{
		protected static final Lottery _instance = new Lottery();
	}
}