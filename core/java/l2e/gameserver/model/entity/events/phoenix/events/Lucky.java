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
import l2e.gameserver.model.entity.events.phoenix.model.SingleEventStatus;

public class Lucky extends AbstractEvent
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
						divideIntoTeams(1);
						teleportToTeamPos();
						preparePlayers();
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
						unSpawnChests();
						EventPlayer winner = getPlayerWithMaxScore();
						giveReward(winner);
						setStatus(EventState.INACTIVE);
						announce("Congratulation! " + winner.getName() + " won the event with " + winner.getScore() + " opened chests!");
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
	
	private final FastList<EventNpc> chests;
	
	protected int restriction = 1;
	
	public Lucky(Integer containerId)
	{
		super(containerId);
		
		eventId = 3;
		createNewTeam(1, "All", Configuration.getInstance().getColor(getId(), "All"), Configuration.getInstance().getPosition(getId(), "All", 1));
		task = new Core();
		chests = new FastList<>();
		clock = new EventClock(Configuration.getInstance().getInt(getId(), "matchTime"));
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
		return "Max: " + getPlayerWithMaxScore().getScore() + "  Time: " + clock.getTimeInString() + "";
	}
	
	@Override
	public void onClockZero()
	{
		setStatus(EventState.END);
		schedule(1);
	}
	
	@Override
	public boolean onTalkNpc(Integer npc, EventPlayer player)
	{
		EventNpc npci = NpcContainer.getInstance().getNpc(npc);
		
		if (npci == null)
		{
			return false;
		}
		
		if (!chests.contains(npci))
		{
			return false;
		}
		
		if (rnd.nextInt(3) == 0)
		{
			Out.startFlameEffect(npci.getId());
			player.doDieNpc(npci.getId());
			addToResurrector(player);
		}
		else
		{
			npci.doDie();
			player.increaseScore();
		}
		
		npci.unspawn();
		chests.remove(npc);
		
		if (chests.size() == 0)
		{
			clock.stop();
		}
		return true;
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
		int[] coor = Configuration.getInstance().getPosition(getId(), "Chests", 1);
		for (int i = 0; i < Configuration.getInstance().getInt(getId(), "numberOfChests"); i++)
		{
			chests.add(NpcContainer.getInstance().createNpc(coor[0] + (rnd.nextInt(coor[3] * 2) - coor[3]), coor[1] + (rnd.nextInt(coor[3] * 2) - coor[3]), coor[2], Configuration.getInstance().getInt(getId(), "chestNpcId"), instanceId));
		}
		setStatus(EventState.START);
		schedule(1);
	}
	
	public void unSpawnChests()
	{
		for (EventNpc s : chests)
		{
			s.unspawn();
			chests.remove(s);
		}
	}
	
	@Override
	public void createStatus()
	{
		status = new SingleEventStatus(containerId);
		
	}
}