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

public class Battlefield extends AbstractEvent
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
						giveSkill();
						spawnBases();
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
						unspawnBases();
						removeSkill();
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
		TELEPORT,
		INACTIVE
	}
	
	public EventState eventState;
	
	private final Core task;
	
	public int winnerTeam;
	
	private final FastList<EventNpc> bases;
	
	public Battlefield(int containerId)
	{
		super(containerId);
		
		eventId = 10;
		createNewTeam(1, "Blue", Configuration.getInstance().getColor(getId(), "Blue"), Configuration.getInstance().getPosition(getId(), "Blue", 1));
		createNewTeam(2, "Red", Configuration.getInstance().getColor(getId(), "Red"), Configuration.getInstance().getPosition(getId(), "Red", 1));
		bases = new FastList<>();
		task = new Core();
		winnerTeam = 0;
		clock = new EventClock(Configuration.getInstance().getInt(getId(), "matchTime"));
	}
	
	@Override
	public void clockTick()
	{
		for (EventNpc base : bases)
		{
			if (base.getTeam() != 0)
			{
				teams.get(base.getTeam()).increaseScore(1);
			}
		}
	}
	
	@Override
	public void endEvent()
	{
		winnerTeam = players.head().getNext().getValue().getMainTeam();
		
		setStatus(EventState.END);
		schedule(1);
		
	}
	
	@Override
	public String getScorebar()
	{
		return "" + teams.get(1).getName() + ": " + teams.get(1).getScore() + "  " + teams.get(2).getName() + ": " + teams.get(2).getScore() + "  Time: " + clock.getTimeInString();
	}
	
	@Override
	public int getWinnerTeam()
	{
		if (teams.get(1).getScore() > teams.get(2).getScore())
		{
			return 1;
		}
		if (teams.get(2).getScore() > teams.get(1).getScore())
		{
			return 2;
		}
		if (teams.get(1).getScore() == teams.get(2).getScore())
		{
			if (rnd.nextInt(1) == 1)
			{
				return 1;
			}
			return 2;
		}
		return 1;
	}
	
	public void giveSkill()
	{
		for (EventPlayer player : getPlayerList())
		{
			player.addSkill(Configuration.getInstance().getInt(getId(), "captureSkillId"), 1);
		}
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
	
	public void removeSkill()
	{
		for (EventPlayer player : getPlayerList())
		{
			player.removeSkill(Configuration.getInstance().getInt(getId(), "captureSkillId"), 1);
		}
	}
	
	@Override
	protected void reset()
	{
		super.reset();
		bases.clear();
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
	
	public void spawnBases()
	{
		for (int i = 1; i <= Configuration.getInstance().getInt(getId(), "numOfBases"); i++)
		{
			int[] pos = Configuration.getInstance().getPosition(getId(), "Base", i);
			EventNpc npci = NpcContainer.getInstance().createNpc(pos[0], pos[1], pos[2], Configuration.getInstance().getInt(getId(), "baseNpcId"), instanceId);
			bases.add(npci);
			npci.setTitle("- Neutral -");
		}
	}
	
	@Override
	public void start()
	{
		setStatus(EventState.START);
		schedule(1);
	}
	
	public void unspawnBases()
	{
		for (EventNpc base : bases)
		{
			base.unspawn();
		}
	}
	
	@Override
	public void useCapture(EventPlayer player, Integer base)
	{
		EventNpc baseinfo = NpcContainer.getInstance().getNpc(base);
		if (baseinfo == null)
		{
			return;
		}
		
		if (!bases.contains(baseinfo))
		{
			return;
		}
		
		for (EventNpc b : bases)
		{
			if (b.equals(baseinfo))
			{
				if (b.getTeam() == player.getMainTeam())
				{
					return;
				}
				
				b.setTeam(player.getMainTeam());
				b.setTitle("- " + teams.get(player.getMainTeam()).getName() + " -");
				
				for (EventPlayer p : getPlayerList())
				{
					p.sendAbstractNpcInfo(b);
				}
				
				announce(getPlayerList(), "The " + teams.get(player.getMainTeam()).getName() + " team captured a base!");
				player.increaseScore();
				break;
			}
		}
	}
	
	@Override
	public void createStatus()
	{
		status = new TeamEventStatus(containerId);
		
	}
}