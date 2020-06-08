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
package l2e.gameserver.model.entity.events.phoenix.events;

import javolution.util.FastList;
import l2e.gameserver.model.entity.events.phoenix.AbstractEvent;
import l2e.gameserver.model.entity.events.phoenix.Configuration;
import l2e.gameserver.model.entity.events.phoenix.container.NpcContainer;
import l2e.gameserver.model.entity.events.phoenix.io.Out;
import l2e.gameserver.model.entity.events.phoenix.model.EventNpc;
import l2e.gameserver.model.entity.events.phoenix.model.EventPlayer;
import l2e.gameserver.model.entity.events.phoenix.model.TeamEventStatus;

public class Domination extends AbstractEvent
{
	public static boolean enabled = true;
	
	protected class Core implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				switch (eventState)
				{
					case START:
						divideIntoTeams(2);
						teleportToTeamPos();
						preparePlayers();
						createPartyOfTeam(1);
						createPartyOfTeam(2);
						forceSitAll();
						setStatus(EventState.FIGHT);
						schedule(10000);
						break;
					case FIGHT:
						forceStandAll();
						setStatus(EventState.END);
						clock.start();
						break;
					case END:
						clock.stop();
						if (winnerTeam == 0)
						{
							winnerTeam = getWinnerTeam();
						}
						giveReward(getPlayersOfTeam(winnerTeam));
						unSpawnZones();
						setStatus(EventState.INACTIVE);
						announce("Congratulation! The " + teams.get(winnerTeam).getName() + " team won the event with " + teams.get(winnerTeam).getScore() + " points!");
						eventEnded();
						break;
					default:
						break;
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				announce("Error! Event ended.");
				eventEnded();
			}
		}
	}
	
	private enum EventState
	{
		START,
		FIGHT,
		END,
		INACTIVE
	}
	
	public EventState eventState;
	
	private final Core task;
	
	private final FastList<EventNpc> zones;
	
	public Domination(Integer containerId)
	{
		super(containerId);
		
		eventId = 1;
		createNewTeam(1, "Blue", Configuration.getInstance().getColor(getId(), "Blue"), Configuration.getInstance().getPosition(getId(), "Blue", 1));
		createNewTeam(2, "Red", Configuration.getInstance().getColor(getId(), "Red"), Configuration.getInstance().getPosition(getId(), "Red", 1));
		task = new Core();
		zones = new FastList<>();
		winnerTeam = 0;
		clock = new EventClock(Configuration.getInstance().getInt(getId(), "matchTime"));
	}
	
	@Override
	public void clockTick()
	{
		int team1 = 0;
		int team2 = 0;
		
		for (EventPlayer player : getPlayerList())
		{
			switch (player.getMainTeam())
			{
				case 1:
					if (Math.sqrt(player.getPlanDistanceSq(zones.getFirst().getNpc())) <= Configuration.getInstance().getInt(getId(), "zoneRadius"))
					{
						team1++;
					}
					break;
				case 2:
					if (Math.sqrt(player.getPlanDistanceSq(zones.getFirst().getNpc())) <= Configuration.getInstance().getInt(getId(), "zoneRadius"))
					{
						team2++;
					}
					break;
			}
		}
		
		if (team1 > team2)
		{
			for (EventPlayer player : getPlayersOfTeam(1))
			{
				player.increaseScore();
			}
			teams.get(1).increaseScore();
		}
		
		if (team2 > team1)
		{
			for (EventPlayer player : getPlayersOfTeam(2))
			{
				player.increaseScore();
			}
			teams.get(2).increaseScore();
		}
		
	}
	
	@Override
	public void endEvent()
	{
		setStatus(EventState.END);
		clock.stop();
	}
	
	@Override
	public String getScorebar()
	{
		return "" + teams.get(1).getName() + ": " + teams.get(1).getScore() + "  " + teams.get(2).getName() + ": " + teams.get(2).getScore() + "  Time: " + clock.getTimeInString();
	}
	
	@Override
	public void onClockZero()
	{
		setStatus(EventState.END);
		schedule(1);
	}
	
	@Override
	public void onDie(EventPlayer victim, EventPlayer killer)
	{
		super.onDie(victim, killer);
		addToResurrector(victim);
	}
	
	@Override
	public void schedule(int time)
	{
		Out.tpmScheduleGeneral(task, time);
	}
	
	public void setStatus(EventState s)
	{
		eventState = s;
	}
	
	@Override
	public void start()
	{
		int[] npcpos = Configuration.getInstance().getPosition(getId(), "Zone", 1);
		zones.add(NpcContainer.getInstance().createNpc(npcpos[0], npcpos[1], npcpos[2], Configuration.getInstance().getInt(getId(), "zoneNpcId"), instanceId));
		setStatus(EventState.START);
		schedule(1);
	}
	
	public void unSpawnZones()
	{
		for (EventNpc s : zones)
		{
			s.unspawn();
			zones.remove(s);
		}
	}
	
	@Override
	public void createStatus()
	{
		status = new TeamEventStatus(containerId);
	}
}