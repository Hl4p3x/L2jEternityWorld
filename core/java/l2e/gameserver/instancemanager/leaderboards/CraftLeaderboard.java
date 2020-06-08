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
package l2e.gameserver.instancemanager.leaderboards;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Util;

public class CraftLeaderboard
{
	public Logger _log = Logger.getLogger(CraftLeaderboard.class.getName());
	
	private static CraftLeaderboard _instance;
	public Map<Integer, CraftRank> _ranks = new FastMap<>();
	protected Future<?> _actionTask = null;
	protected int TASK_DELAY = Config.RANK_CRAFT_INTERVAL;
	protected Long nextTimeUpdateReward = 0L;
	
	public static CraftLeaderboard getInstance()
	{
		if (_instance == null)
		{
			_instance = new CraftLeaderboard();
		}
		
		return _instance;
	}
	
	public CraftLeaderboard()
	{
		engineInit();
	}
	
	public void onSucess(int owner, String name)
	{
		CraftRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new CraftRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}
		
		ar.sucess();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void onFail(int owner, String name)
	{
		CraftRank ar = null;
		if (_ranks.get(owner) == null)
		{
			ar = new CraftRank();
		}
		else
		{
			ar = _ranks.get(owner);
		}
		
		ar.fail();
		ar.name = name;
		_ranks.put(owner, ar);
	}
	
	public void stopTask()
	{
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
		}
		
		_actionTask = null;
	}
	
	public class CraftTask implements Runnable
	{
		@Override
		public void run()
		{
			_log.info("CraftManager: Autotask init.");
			formRank();
			nextTimeUpdateReward = System.currentTimeMillis() + (TASK_DELAY * 60000);
		}
	}
	
	public void startTask()
	{
		if (_actionTask == null)
		{
			_actionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CraftTask(), 1000, TASK_DELAY * 60000);
		}
	}
	
	public void formRank()
	{
		Map<Integer, Integer> scores = new FastMap<>();
		for (int obj : _ranks.keySet())
		{
			CraftRank ar = _ranks.get(obj);
			scores.put(obj, ar.sucess - ar.fail);
		}
		
		int Top = -1;
		int idTop = 0;
		for (int id : scores.keySet())
		{
			if (scores.get(id) > Top)
			{
				idTop = id;
				Top = scores.get(id);
			}
		}
		
		CraftRank arTop = _ranks.get(idTop);
		
		if (arTop == null)
		{
			Announcements.getInstance().announceToAll("CraftMaster: No winners at this time!");
			_ranks.clear();
			return;
		}
		
		L2PcInstance winner = (L2PcInstance) L2World.getInstance().findObject(idTop);
		
		Announcements.getInstance().announceToAll("Attention Players: " + arTop.name + " is the winner for this time with " + arTop.sucess + "/" + arTop.fail + ". Next calculation in " + Config.RANK_CRAFT_INTERVAL + " min(s).");
		if ((winner != null) && (Config.RANK_CRAFT_REWARD_ID > 0) && (Config.RANK_CRAFT_REWARD_COUNT > 0))
		{
			winner.getInventory().addItem("CraftManager", Config.RANK_CRAFT_REWARD_ID, Config.RANK_CRAFT_REWARD_COUNT, winner, null);
			if (Config.RANK_CRAFT_REWARD_COUNT > 1)
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(Config.RANK_CRAFT_REWARD_ID).addNumber(Config.RANK_CRAFT_REWARD_COUNT));
			}
			else
			{
				winner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(Config.RANK_CRAFT_REWARD_ID));
			}
			winner.sendPacket(new ItemList(winner, false));
		}
		_ranks.clear();
	}
	
	public String showHtm(int owner)
	{
		Map<Integer, Integer> scores = new FastMap<>();
		for (int obj : _ranks.keySet())
		{
			CraftRank ar = _ranks.get(obj);
			scores.put(obj, ar.sucess - ar.fail);
		}
		
		scores = Util.sortMap(scores, false);
		
		int counter = 0, max = 20;
		String pt = "<html><body><center>" + "<font color=\"cc00ad\">TOP " + max + " Crafters</font><br>";
		
		pt += "<table width=260 border=0 cellspacing=0 cellpadding=0 bgcolor=333333>";
		pt += "<tr> <td align=center>No.</td> <td align=center>Name</td> <td align=center>Success</td> <td align=center>Fail</td> </tr>";
		pt += "<tr> <td align=center>&nbsp;</td> <td align=center>&nbsp;</td> <td align=center></td> <td align=center></td> </tr>";
		boolean inTop = false;
		for (int id : scores.keySet())
		{
			if (counter < max)
			{
				CraftRank ar = _ranks.get(id);
				pt += tx(counter, ar.name, ar.sucess, ar.fail, id == owner);
				if (id == owner)
				{
					inTop = true;
				}
				
				counter++;
			}
			else
			{
				break;
			}
		}
		
		if (!inTop)
		{
			CraftRank arMe = _ranks.get(owner);
			if (arMe != null)
			{
				pt += "<tr> <td align=center>...</td> <td align=center>...</td> <td align=center>...</td> <td align=center>...</td> </tr>";
				int placeMe = 0;
				for (int idMe : scores.keySet())
				{
					placeMe++;
					if (idMe == owner)
					{
						break;
					}
				}
				pt += tx(placeMe, arMe.name, arMe.sucess, arMe.fail, true);
			}
		}
		
		pt += "</table>";
		pt += "<br><br>";
		if ((Config.RANK_CRAFT_REWARD_ID > 0) && (Config.RANK_CRAFT_REWARD_COUNT > 0))
		{
			pt += "Next Reward Time in <font color=\"LEVEL\">" + calcMinTo() + " min(s)</font><br1>";
			pt += "<font color=\"aadd77\">" + Config.RANK_CRAFT_REWARD_COUNT + " &#" + Config.RANK_CRAFT_REWARD_ID + ";</font>";
		}
		
		pt += "</center></body></html>";
		
		return pt;
	}
	
	private int calcMinTo()
	{
		return ((int) (nextTimeUpdateReward - System.currentTimeMillis())) / 60000;
	}
	
	private String tx(int counter, String name, int kills, int deaths, boolean mi)
	{
		String t = "";
		
		t += "	<tr>" + "<td align=center>" + (mi ? "<font color=\"LEVEL\">" : "") + (counter + 1) + ".</td>" + "<td align=center>" + name + "</td>" + "<td align=center>" + kills + "</td>" + "<td align=center>" + deaths + "" + (mi ? "</font>" : "") + " </td>" + "</tr>";
		
		return t;
	}
	
	public void engineInit()
	{
		_ranks = new FastMap<>();
		startTask();
		_log.info(getClass().getSimpleName() + ": Initialized");
	}
	
	public class CraftRank
	{
		public int sucess, fail;
		public String name;
		
		public CraftRank()
		{
			sucess = 0;
			fail = 0;
		}
		
		public void sucess()
		{
			sucess++;
		}
		
		public void fail()
		{
			fail++;
		}
	}
}