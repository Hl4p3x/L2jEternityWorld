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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Rnd;

/**
 * Created by LordWinter 01.10.2011 Fixed by L2J Eternity-World
 */
public class FishingChampionship
{
	protected static final Logger _log = Logger.getLogger(FishingChampionship.class.getName());
	
	private static FishingChampionship _instance;
	private long _enddate = 0;
	
	protected ArrayList<String> _playersName = new ArrayList<>();
	protected ArrayList<String> _fishLength = new ArrayList<>();
	protected ArrayList<String> _winPlayersName = new ArrayList<>();
	protected ArrayList<String> _winFishLength = new ArrayList<>();
	protected ArrayList<Fisher> _tmpPlayer = new ArrayList<>();
	protected ArrayList<Fisher> _winPlayer = new ArrayList<>();
	
	private float _minFishLength = 0;
	private int x;
	
	public static FishingChampionship getInstance()
	{
		if (_instance == null)
		{
			_instance = new FishingChampionship();
		}
		return _instance;
	}
	
	protected FishingChampionship()
	{
		_log.info("Fishing Championship Manager : started");
		
		restoreData();
		refreshWinResult();
		setNewMin();
		if (_enddate <= System.currentTimeMillis())
		{
			_enddate = System.currentTimeMillis();
			new finishChamp().run();
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new finishChamp(), _enddate - System.currentTimeMillis());
		}
	}
	
	protected class Fisher
	{
		float _length = 0;
		String _name;
		int _rewarded = 0;
	}
	
	protected class finishChamp implements Runnable
	{
		@Override
		public void run()
		{
			_winPlayer.clear();
			for (Fisher fisher : _tmpPlayer)
			{
				fisher._rewarded = 1;
				_winPlayer.add(fisher);
			}
			_tmpPlayer.clear();
			refreshWinResult();
			setEndOfChamp();
			shutdown();
			
			_log.info("Fishing Championship Manager : start new event period.");
		}
	}
	
	protected void setEndOfChamp()
	{
		Calendar finishtime = Calendar.getInstance();
		finishtime.setTimeInMillis(_enddate);
		finishtime.set(Calendar.MINUTE, 0);
		finishtime.set(Calendar.SECOND, 0);
		finishtime.add(Calendar.DAY_OF_MONTH, 6);
		finishtime.set(Calendar.DAY_OF_WEEK, 3);
		finishtime.set(Calendar.HOUR_OF_DAY, 19);
		_enddate = finishtime.getTimeInMillis();
	}
	
	private void restoreData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT finish_date FROM fishing_championship_date");
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				_enddate = rs.getLong("finish_date");
			}
			
			rs.close();
			statement.close();
			statement = con.prepareStatement("SELECT PlayerName,fishLength,rewarded FROM fishing_championship");
			rs = statement.executeQuery();
			while (rs.next())
			{
				int rewarded = rs.getInt("rewarded");
				Fisher fisher;
				if (rewarded == 0)
				{
					fisher = new Fisher();
					fisher._name = rs.getString("PlayerName");
					fisher._length = rs.getFloat("fishLength");
					_tmpPlayer.add(fisher);
				}
				if (rewarded > 0)
				{
					fisher = new Fisher();
					fisher._name = rs.getString("PlayerName");
					fisher._length = rs.getFloat("fishLength");
					fisher._rewarded = rewarded;
					_winPlayer.add(fisher);
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: can't get fishing championship info: " + e.getMessage(), e);
		}
	}
	
	public synchronized void newFish(L2PcInstance player)
	{
		float p1 = Rnd.get(60, 90);
		float len = (Rnd.get(0, 99) / 100) + p1;
		if (_tmpPlayer.size() < 5)
		{
			for (x = 0; x < _tmpPlayer.size(); x++)
			{
				if (_tmpPlayer.get(x)._name.equalsIgnoreCase(player.getName()))
				{
					if (_tmpPlayer.get(x)._length < len)
					{
						_tmpPlayer.get(x)._length = len;
						player.sendMessage((new CustomMessage("FishingChampionship.IMPROVED_RESULT", player.getLang())).toString());
						setNewMin();
					}
					return;
				}
			}
			Fisher newFisher = new Fisher();
			newFisher._name = player.getName();
			newFisher._length = len;
			_tmpPlayer.add(newFisher);
			player.sendMessage((new CustomMessage("FishingChampionship.GOT_TO_LIST", player.getLang())).toString());
			setNewMin();
		}
		else
		{
			if (_minFishLength >= len)
			{
				return;
			}
			for (x = 0; x < _tmpPlayer.size(); x++)
			{
				if (_tmpPlayer.get(x)._name.equalsIgnoreCase(player.getName()))
				{
					if (_tmpPlayer.get(x)._length < len)
					{
						_tmpPlayer.get(x)._length = len;
						player.sendMessage((new CustomMessage("FishingChampionship.IMPROVED_RESULT", player.getLang())).toString());
						setNewMin();
					}
					return;
				}
			}
			Fisher minFisher = null;
			float minLen = 99999;
			for (Fisher a_tmpPlayer : _tmpPlayer)
			{
				if (a_tmpPlayer._length < minLen)
				{
					minFisher = a_tmpPlayer;
					minLen = minFisher._length;
				}
			}
			_tmpPlayer.remove(minFisher);
			Fisher newFisher = new Fisher();
			newFisher._name = player.getName();
			newFisher._length = len;
			_tmpPlayer.add(newFisher);
			player.sendMessage((new CustomMessage("FishingChampionship.GOT_TO_LIST", player.getLang())).toString());
			setNewMin();
		}
	}
	
	private void setNewMin()
	{
		float minLen = 99999;
		for (Fisher a_tmpPlayer : _tmpPlayer)
		{
			if (a_tmpPlayer._length < minLen)
			{
				minLen = a_tmpPlayer._length;
			}
		}
		_minFishLength = minLen;
	}
	
	public long getTimeRemaining()
	{
		return _enddate - (System.currentTimeMillis() / 60000);
	}
	
	public String getWinnerName(L2PcInstance player, int par)
	{
		if (_winPlayersName.size() >= par)
		{
			return _winPlayersName.get(par - 1);
		}
		return "" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.NO") + "";
	}
	
	public String getCurrentName(L2PcInstance player, int par)
	{
		if (_playersName.size() >= par)
		{
			return _playersName.get(par - 1);
		}
		return "" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.NO") + "";
	}
	
	public String getFishLength(int par)
	{
		if (_winFishLength.size() >= par)
		{
			return _winFishLength.get(par - 1);
		}
		return "0";
	}
	
	public String getCurrentFishLength(int par)
	{
		if (_fishLength.size() >= par)
		{
			return _fishLength.get(par - 1);
		}
		return "0";
	}
	
	public void getReward(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player.getObjectId());
		
        	String str = "<html><head><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ROYAL_TOURNAMENT") + "</title></head>";
        	str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ACCEPT_CONGRATULATIONS") + "<br>";
        	str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.HERE_YOUR_PRIZE") + "<br>";
        	str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.GOOD_LUCK") + "";
        	str = str + "</body></html>";
		html.setHtml(str);
		player.sendPacket(html);
		for (Fisher fisher : _winPlayer)
		{
			if (fisher._name.equalsIgnoreCase(player.getName()) && (fisher._rewarded != 2))
			{
				int rewardCnt = 0;
				for (int x = 0; x < _winPlayersName.size(); x++)
				{
					if (_winPlayersName.get(x).equalsIgnoreCase(player.getName()))
					{
						switch (x)
						{
							case 0:
								rewardCnt = 800000;
								break;
							case 1:
								rewardCnt = 500000;
								break;
							case 2:
								rewardCnt = 300000;
								break;
							case 3:
								rewardCnt = 200000;
								break;
							case 4:
								rewardCnt = 100000;
								break;
						}
					}
				}
				fisher._rewarded = 2;
				if (rewardCnt > 0)
				{
					L2ItemInstance item = player.getInventory().addItem("reward", 57, rewardCnt, player, player);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item).addNumber(rewardCnt));
					player.sendPacket(new ItemList(player, false));
				}
			}
		}
	}
	
	public void showMidResult(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player.getObjectId());
		
        	String str = "<html><head><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ROYAL_TOURNAMENT") + "</title></head>";
        	str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.NOW_PASS_COMPETITIONS") + "<br><br>";
        	str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.UPON_CPMPETITIONS") + "<br>";
        	str = str + "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + "</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.FISHERMAN") + "</td><td width=80 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.LENGTH") + "</td></tr></table><table width=280>";
        	for (int x = 1; x <= 5; x++)
        	{
           	 	str = str + "<tr><td width=70 align=center>" + x + " " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td>";
            		str = str + "<td width=110 align=center>" + getCurrentName(player, x) + "</td>";
            		str = str + "<td width=80 align=center>" + getCurrentFishLength(x) + "</td></tr>";
        	}
        	str = str + "<td width=80 align=center>0</td></tr></table><br>";
        	str = str + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZES_LIST") + "<br><table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + "</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZE") + "</td><td width=80 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.AMOUNT") + "</td></tr></table><table width=280>";
        	str = str + "<tr><td width=70 align=center>1 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>800000</td></tr><tr><td width=70 align=center>2 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>500000</td></tr><tr><td width=70 align=center>3 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>300000</td></tr>";
        	str = str + "<tr><td width=70 align=center>4 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>200000</td></tr><tr><td width=70 align=center>5 " + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + LocalizationStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA") + "</td><td width=80 align=center>100000</td></tr></table></body></html>";
        	html.setHtml(str);
		player.sendPacket(html);
	}
	
	public void shutdown()
	{
		PreparedStatement statement;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			statement = con.prepareStatement("DELETE FROM fishing_championship_date");
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("INSERT INTO fishing_championship_date (finish_date) VALUES (?)");
			statement.setLong(1, _enddate);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM fishing_championship");
			statement.execute();
			statement.close();
			
			for (Fisher fisher : _winPlayer)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher._name);
				statement.setFloat(2, fisher._length);
				statement.setInt(3, fisher._rewarded);
				statement.execute();
				statement.close();
			}
			for (Fisher fisher : _tmpPlayer)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher._name);
				statement.setFloat(2, fisher._length);
				statement.setInt(3, 0);
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Exception: can't update player vitality: " + e.getMessage(), e);
		}
	}
	
	protected synchronized void refreshResult()
	{
		_playersName.clear();
		_fishLength.clear();
		Fisher fisher1, fisher2;
		for (int x = 0; x <= (_tmpPlayer.size() - 1); x++)
		{
			for (int y = 0; y <= (_tmpPlayer.size() - 2); y++)
			{
				fisher1 = _tmpPlayer.get(y);
				fisher2 = _tmpPlayer.get(y + 1);
				if (fisher1._length < fisher2._length)
				{
					_tmpPlayer.set(y, fisher2);
					_tmpPlayer.set(y + 1, fisher1);
				}
			}
		}
		
		for (x = 0; x <= (_tmpPlayer.size() - 1); x++)
		{
			_playersName.add(_tmpPlayer.get(x)._name);
			_fishLength.add("" + _tmpPlayer.get(x)._length);
		}
	}
	
	protected void refreshWinResult()
	{
		_winPlayersName.clear();
		_winFishLength.clear();
		Fisher fisher1, fisher2;
		for (int x = 0; x <= (_winPlayer.size() - 1); x++)
		{
			for (int y = 0; y <= (_winPlayer.size() - 2); y++)
			{
				fisher1 = _winPlayer.get(y);
				fisher2 = _winPlayer.get(y + 1);
				if (fisher1._length < fisher2._length)
				{
					_winPlayer.set(y, fisher2);
					_winPlayer.set(y + 1, fisher1);
				}
			}
		}
		
		for (x = 0; x <= (_winPlayer.size() - 1); x++)
		{
			_winPlayersName.add(_winPlayer.get(x)._name);
			_winFishLength.add("" + _winPlayer.get(x)._length);
		}
	}
}