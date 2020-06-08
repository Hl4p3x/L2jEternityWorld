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
package l2e.gameserver.model.entity.events;

import l2e.Config;
import l2e.gameserver.Announcements;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.L2TownZone;

public class TW extends FunEvent
{
	private static final int ALL_TOWNS_INT = 17;
	
	@Override
	public void loadConfig()
	{
		EVENT_NAME = "TW";
		EVENT_AUTO_MODE = Config.TW_AUTO_MODE;
		EVENT_INTERVAL = Config.TW_EVENT_INTERVAL;
	}
	
	@Override
	public void abortEvent()
	{
		if (_state == State.INACTIVE)
		{
			return;
		}
		
		if (_state == State.FIGHTING)
		{
			endFight();
		}
		_state = State.INACTIVE;
		clearData();
		autoStart();
	}
	
	private void clearData()
	{
		if (_sheduleNext != null)
		{
			_sheduleNext.cancel(false);
			_sheduleNext = null;
		}
	}
	
	private void startFight()
	{
		if (Config.TW_ALL_TOWNS)
		{
			for (int i = 1; i <= ALL_TOWNS_INT; i++)
			{
				TownManager.getTown(i).setIsTWZone(true);
				TownManager.getTown(i).updateForCharactersInside();
			}
			TownManager.getTown(20).setIsTWZone(true);
			TownManager.getTown(20).updateForCharactersInside();
			
			CustomMessage msg = new CustomMessage("TW.HAOS_STARTED_ALL", true);
			Announcements.getInstance().announceToAll(msg);
		}
		if (!Config.TW_ALL_TOWNS && (Config.TW_TOWN_ID != 18) && (Config.TW_TOWN_ID != 21) && (Config.TW_TOWN_ID != 22))
		{
			TownManager.getTown(Config.TW_TOWN_ID).setIsTWZone(true);
			TownManager.getTown(Config.TW_TOWN_ID).updateForCharactersInside();
			CustomMessage msg = new CustomMessage("TW.HAOS_STARTED", true);
			msg.add(TownManager.getTown(Config.TW_TOWN_ID).getName());
			Announcements.getInstance().announceToAll(msg);
		}
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
		{
			if (player.isOnline())
			{
				L2TownZone Town = TownManager.getTown(player.getX(), player.getY(), player.getZ());
				if (Town != null)
				{
					if (((Town.getTownId() == Config.TW_TOWN_ID) && !Config.TW_ALL_TOWNS) || Config.TW_ALL_TOWNS)
					{
						player.setInsideZone(ZoneId.PEACE, false);
						player.revalidateZone(true);
					}
				}
			}
		}
	}
	
	private void endFight()
	{
		if (Config.TW_ALL_TOWNS)
		{
			for (int i = 1; i <= ALL_TOWNS_INT; i++)
			{
				TownManager.getTown(i).setIsTWZone(false);
				TownManager.getTown(i).updateForCharactersInside();
			}
			TownManager.getTown(20).setIsTWZone(false);
			TownManager.getTown(20).updateForCharactersInside();
			
			CustomMessage msg = new CustomMessage("TW.HAOS_STOPED_ALL", true);
			Announcements.getInstance().announceToAll(msg);
		}
		if (!Config.TW_ALL_TOWNS && (Config.TW_TOWN_ID != 18) && (Config.TW_TOWN_ID != 21) && (Config.TW_TOWN_ID != 22))
		{
			TownManager.getTown(Config.TW_TOWN_ID).setIsTWZone(false);
			TownManager.getTown(Config.TW_TOWN_ID).updateForCharactersInside();
			CustomMessage msg = new CustomMessage("TW.HAOS_STOPED", true);
			msg.add(TownManager.getTown(Config.TW_TOWN_ID).getName());
			Announcements.getInstance().announceToAll(msg);
		}
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
		{
			if (player.isOnline())
			{
				L2TownZone Town = TownManager.getTown(player.getX(), player.getY(), player.getZ());
				if (Town != null)
				{
					if (((Town.getTownId() == Config.TW_TOWN_ID) && !Config.TW_ALL_TOWNS) || Config.TW_ALL_TOWNS)
					{
						player.setInsideZone(ZoneId.PEACE, true);
						player.revalidateZone(true);
					}
				}
			}
		}
	}
	
	@Override
	protected void StartNext()
	{
		long delay = 0;
		
		if (_state == State.WAITING)
		{
			delay = Config.TW_FIGHT_TIME * 60000;
			_state = State.FIGHTING;
			startFight();
		}
		else if (_state == State.FIGHTING)
		{
			endFight();
			clearData();
			
			_state = State.INACTIVE;
			autoStart();
			return;
		}
		
		sheduleNext(delay);
	}
	
	@Override
	public boolean onPlayerDie(L2PcInstance player, L2PcInstance killer)
	{
		for (String reward : Config.TW_REWARD)
		{
			String[] rew = reward.split(":");
			killer.addItem("TownWar", Integer.parseInt(rew[0]), Integer.parseInt(rew[1]), null, true);
		}
		CustomMessage msg = new CustomMessage("TW.REWARD", killer.getLang());
		msg.add(player.getName());
		killer.sendMessage(msg.toString());
		
		return true;
	}
}