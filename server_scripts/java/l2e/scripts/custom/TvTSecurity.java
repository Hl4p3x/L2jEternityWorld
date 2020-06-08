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
package l2e.scripts.custom;

import java.util.ArrayList;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTEventTeam;

/**
 * Based on L2J Eternity-World
 */
public class TvTSecurity
{
	private final int CheckDelay = 30000;
	
	protected static ArrayList<String> TvTPlayerList = new ArrayList<>();
	private static String[] Splitter;
	private static int xx, yy, zz, SameLoc;
	protected static L2PcInstance _player;
	
	protected TvTSecurity()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AntiAfk(), 60000, CheckDelay);
	}
	
	protected class AntiAfk implements Runnable
	{
		@Override
		public void run()
		{
			if (TvTEvent.isStarted())
			{
				for (TvTEventTeam team : TvTEvent._teams)
				{
					for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
					{
						if ((playerInstance != null) && playerInstance.isOnline())
						{
							_player = playerInstance;
							AddTvTSpawnInfo(playerInstance.getName(), playerInstance.getX(), playerInstance.getY(), playerInstance.getZ());
						}
					}
				}
			}
			else
			{
				TvTPlayerList.clear();
			}
		}
	}
	
	protected static void AddTvTSpawnInfo(String name, int _x, int _y, int _z)
	{
		if (!CheckTvTSpawnInfo(name))
		{
			String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":1";
			TvTPlayerList.add(temp);
		}
		else
		{
			Object[] elements = TvTPlayerList.toArray();
			for (int i = 0; i < elements.length; i++)
			{
				Splitter = ((String) elements[i]).split(":");
				String nameVal = Splitter[0];
				if (name.equals(nameVal))
				{
					GetTvTSpawnInfo(name);
					if ((_x == xx) && (_y == yy) && (_z == zz) && (_player.isAttackingNow() == false) && (_player.isCastingNow() == false) && (_player.isOnline() == true))
					{
						++SameLoc;
						if (SameLoc >= 4)
						{
							TvTPlayerList.remove(i);
							_player.logout();
						}
						else
						{
							TvTPlayerList.remove(i);
							String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":" + SameLoc;
							TvTPlayerList.add(temp);
						}
						return;
					}
					TvTPlayerList.remove(i);
					String temp = name + ":" + Integer.toString(_x) + ":" + Integer.toString(_y) + ":" + Integer.toString(_z) + ":1";
					TvTPlayerList.add(temp);
				}
			}
		}
	}
	
	private static boolean CheckTvTSpawnInfo(String name)
	{
		
		Object[] elements = TvTPlayerList.toArray();
		for (Object element : elements)
		{
			Splitter = ((String) element).split(":");
			String nameVal = Splitter[0];
			if (name.equals(nameVal))
			{
				return true;
			}
		}
		return false;
	}
	
	private static void GetTvTSpawnInfo(String name)
	{
		
		Object[] elements = TvTPlayerList.toArray();
		for (Object element : elements)
		{
			Splitter = ((String) element).split(":");
			String nameVal = Splitter[0];
			if (name.equals(nameVal))
			{
				xx = Integer.parseInt(Splitter[1]);
				yy = Integer.parseInt(Splitter[2]);
				zz = Integer.parseInt(Splitter[3]);
				SameLoc = Integer.parseInt(Splitter[4]);
			}
		}
	}
	
	public static TvTSecurity getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TvTSecurity _instance = new TvTSecurity();
	}
	
	public static void main(String[] args)
	{
		TvTSecurity.getInstance();
	}
}