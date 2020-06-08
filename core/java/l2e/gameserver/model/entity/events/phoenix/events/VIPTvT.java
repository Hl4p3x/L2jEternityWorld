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

import javolution.util.FastMap;
import l2e.gameserver.model.entity.events.phoenix.AbstractEvent;
import l2e.gameserver.model.entity.events.phoenix.Configuration;
import l2e.gameserver.model.entity.events.phoenix.io.Out;
import l2e.gameserver.model.entity.events.phoenix.model.EventPlayer;
import l2e.gameserver.model.entity.events.phoenix.model.TeamEventStatus;

public class VIPTvT extends AbstractEvent
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
						selectNewVipOfTeam(1);
						selectNewVipOfTeam(2);
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
						setStatus(EventState.INACTIVE);
						announce("Congratulation! The " + teams.get(winnerTeam).getName() + " team won the event with " + teams.get(winnerTeam).getScore() + " kills!");
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
		TELEPORT,
		INACTIVE
	}
	
	public EventState eventState;
	
	private final Core task;
	
	private final FastMap<Integer, EventPlayer> vips;
	
	public VIPTvT(Integer containerId)
	{
		super(containerId);
		
		eventId = 5;
		createNewTeam(1, "Blue", Configuration.getInstance().getColor(getId(), "Blue"), Configuration.getInstance().getPosition(getId(), "Blue", 1));
		createNewTeam(2, "Red", Configuration.getInstance().getColor(getId(), "Red"), Configuration.getInstance().getPosition(getId(), "Red", 1));
		task = new Core();
		vips = new FastMap<>();
		winnerTeam = 0;
		clock = new EventClock(Configuration.getInstance().getInt(getId(), "matchTime"));
	}
	
	@Override
	public void endEvent()
	{
		winnerTeam = players.head().getNext().getValue().getMainTeam();
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
		if (vips.get(1).equals(victim))
		{
			teams.get(2).increaseScore();
			killer.increaseScore();
			selectNewVipOfTeam(1);
		}
		if (vips.get(2).equals(victim))
		{
			teams.get(1).increaseScore();
			killer.increaseScore();
			selectNewVipOfTeam(2);
		}
		addToResurrector(victim);
	}
	
	@Override
	public void onLogout(EventPlayer player)
	{
		super.onLogout(player);
		if (vips.get(1).equals(player))
		{
			selectNewVipOfTeam(1);
		}
		if (vips.get(2).equals(player))
		{
			selectNewVipOfTeam(2);
		}
	}
	
	@Override
	public void schedule(int time)
	{
		Out.tpmScheduleGeneral(task, time);
	}
	
	public void selectNewVipOfTeam(int team)
	{
		if (vips.get(team) != null)
		{
			int[] nameColor = teams.get(vips.get(team).getMainTeam()).getTeamColor();
			vips.get(team).setNameColor(nameColor[0], nameColor[1], nameColor[2]);
		}
		
		EventPlayer newvip = getRandomPlayerFromTeam(team);
		vips.getEntry(team).setValue(newvip);
		if (team == 1)
		{
			int[] c = Configuration.getInstance().getColor(getId(), "BlueVIP");
			newvip.setNameColor(c[0], c[1], c[2]);
		}
		
		if (team == 2)
		{
			int[] c = Configuration.getInstance().getColor(getId(), "RedVIP");
			newvip.setNameColor(c[0], c[1], c[2]);
		}
		
		if (!newvip.isDead())
		{
			newvip.healToMax();
		}
		newvip.broadcastUserInfo();
	}
	
	public void setStatus(EventState s)
	{
		eventState = s;
	}
	
	@Override
	public void start()
	{
		vips.put(1, null);
		vips.put(2, null);
		setStatus(EventState.START);
		schedule(1);
	}
	
	@Override
	public void createStatus()
	{
		status = new TeamEventStatus(containerId);
	}
}