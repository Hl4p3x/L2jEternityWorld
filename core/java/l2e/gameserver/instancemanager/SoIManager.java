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

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

/**
 * Created by LordWinter 27.10.2012 Based on L2J Eternity-World
 */
public class SoIManager
{
	protected static final Logger _log = Logger.getLogger(SoIManager.class.getName());
	
	private static SoIManager _instance = null;
	private static final long SOI_OPEN_TIME = 24 * 60 * 60 * 1000L;
	
	private static final Location[] openSeedTeleportLocs =
	{
		new Location(-179537, 209551, -15504),
		new Location(-179779, 212540, -15520),
		new Location(-177028, 211135, -15520),
		new Location(-176355, 208043, -15520),
		new Location(-179284, 205990, -15520),
		new Location(-182268, 208218, -15520),
		new Location(-182069, 211140, -15520),
		new Location(-176036, 210002, -11948),
		new Location(-176039, 208203, -11949),
		new Location(-183288, 208205, -11939),
		new Location(-183290, 210004, -11939),
		new Location(-187776, 205696, -9536),
		new Location(-186327, 208286, -9536),
		new Location(-184429, 211155, -9536),
		new Location(-182811, 213871, -9504),
		new Location(-180921, 216789, -9536),
		new Location(-177264, 217760, -9536),
		new Location(-173727, 218169, -9536)
	};
	
	public static SoIManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new SoIManager();
		}
		return _instance;
	}
	
	public SoIManager()
	{
		_log.info("Seed of Infinity Manager: Loaded. Current stage is: " + getCurrentStage());
		checkStageAndSpawn();
		if (isSeedOpen())
		{
			openSeed(getOpenedTime());
		}
	}
	
	public static int getCurrentStage()
	{
		return ServerVariables.getInt("SoI_stage", 1);
	}
	
	public static long getOpenedTime()
	{
		if (getCurrentStage() != 3)
		{
			return 0;
		}
		return (ServerVariables.getLong("SoI_opened", 0) * 1000L) - System.currentTimeMillis();
	}
	
	public static void setCurrentStage(int stage)
	{
		if (getCurrentStage() == stage)
		{
			return;
		}
		if (stage == 3)
		{
			openSeed(SOI_OPEN_TIME);
		}
		else if (isSeedOpen())
		{
			closeSeed();
		}
		ServerVariables.set("SoI_stage", stage);
		setCohemenesCount(0);
		setEkimusCount(0);
		setHoEDefCount(0);
		checkStageAndSpawn();
		_log.info("Seed of Infinity Manager: Set to stage " + stage);
	}
	
	public static boolean isSeedOpen()
	{
		return getOpenedTime() > 0;
	}
	
	public static void openSeed(long time)
	{
		if (time <= 0)
		{
			return;
		}
		ServerVariables.set("SoI_opened", (System.currentTimeMillis() + time) / 1000L);
		_log.info("Seed of Infinity Manager: Opening the seed for " + Util.formatTime((int) time / 1000));
		spawnOpenedSeed();
		DoorParser.getInstance().getDoor(14240102).openMe();
		
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				closeSeed();
				setCurrentStage(4);
			}
		}, time);
	}
	
	public static void closeSeed()
	{
		_log.info("Seed of Infinity Manager: Closing the seed.");
		ServerVariables.unset("SoI_opened");
		l2e.scripts.ai.zone.EnergySeeds.SoiSeedStop();
		DoorParser.getInstance().getDoor(14240102).closeMe();
		for (L2PcInstance ch : ZoneManager.getInstance().getZoneById(200033).getPlayersInside())
		{
			if (ch != null)
			{
				ch.teleToLocation(-183285, 205996, -12896);
			}
		}
	}
	
	public static void checkStageAndSpawn()
	{
		l2e.scripts.ai.zone.EnergySeeds.SoiCloseMouthStop();
		l2e.scripts.ai.zone.EnergySeeds.SoiMouthStop();
		l2e.scripts.ai.zone.EnergySeeds.SoiAbyssGaze2Stop();
		l2e.scripts.ai.zone.EnergySeeds.SoiAbyssGaze1Stop();
		switch (getCurrentStage())
		{
			case 1:
			case 4:
				l2e.scripts.ai.zone.EnergySeeds.SoiMouthSpawn();
				l2e.scripts.ai.zone.EnergySeeds.SoiAbyssGaze2Spawn();
				break;
			case 5:
				l2e.scripts.ai.zone.EnergySeeds.SoiCloseMouthSpawn();
				l2e.scripts.ai.zone.EnergySeeds.SoiAbyssGaze2Spawn();
				break;
			default:
				l2e.scripts.ai.zone.EnergySeeds.SoiCloseMouthSpawn();
				l2e.scripts.ai.zone.EnergySeeds.SoiAbyssGaze1Spawn();
				break;
		}
	}
	
	public static void notifyCohemenesKill()
	{
		if (getCurrentStage() == 1)
		{
			if (getCohemenesCount() < 9)
			{
				setCohemenesCount(getCohemenesCount() + 1);
			}
			else
			{
				setCurrentStage(2);
			}
		}
	}
	
	public static void notifyEkimusKill()
	{
		if (getCurrentStage() == 2)
		{
			if (getEkimusCount() < Config.SOI_EKIMUS_KILL_COUNT)
			{
				setEkimusCount(getEkimusCount() + 1);
			}
			else
			{
				setCurrentStage(3);
			}
		}
	}
	
	public static void notifyHoEDefSuccess()
	{
		if (getCurrentStage() == 4)
		{
			if (getHoEDefCount() < 9)
			{
				setHoEDefCount(getHoEDefCount() + 1);
			}
			else
			{
				setCurrentStage(5);
			}
		}
	}
	
	public static void setCohemenesCount(int i)
	{
		ServerVariables.set("SoI_CohemenesCount", i);
	}
	
	public static void setEkimusCount(int i)
	{
		ServerVariables.set("SoI_EkimusCount", i);
	}
	
	public static void setHoEDefCount(int i)
	{
		ServerVariables.set("SoI_hoedefkillcount", i);
	}
	
	public static int getCohemenesCount()
	{
		return ServerVariables.getInt("SoI_CohemenesCount", 0);
	}
	
	public static int getEkimusCount()
	{
		return ServerVariables.getInt("SoI_EkimusCount", 0);
	}
	
	public static int getHoEDefCount()
	{
		return ServerVariables.getInt("SoI_hoedefkillcount", 0);
	}
	
	private static void spawnOpenedSeed()
	{
		l2e.scripts.ai.zone.EnergySeeds.SoiSeedSpawn();
	}
	
	public static void teleportInSeed(L2PcInstance player)
	{
		player.teleToLocation(openSeedTeleportLocs[Rnd.get(openSeedTeleportLocs.length)], false);
	}
}