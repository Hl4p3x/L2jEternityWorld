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

import l2e.gameserver.model.entity.events.phoenix.AbstractEvent;
import l2e.gameserver.model.entity.events.phoenix.Configuration;
import l2e.gameserver.model.entity.events.phoenix.container.NpcContainer;
import l2e.gameserver.model.entity.events.phoenix.io.Out;
import l2e.gameserver.model.entity.events.phoenix.model.EventNpc;
import l2e.gameserver.model.entity.events.phoenix.model.EventPlayer;
import l2e.gameserver.model.entity.events.phoenix.model.SingleEventStatus;

public class Simon extends AbstractEvent
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
						setStatus(EventState.SAY);
						schedule(20000);
						break;
					case SAY:
						round++;
						say = createNewRandomString(Configuration.getInstance().getInt(getId(), "lengthOfFirstWord") + (Configuration.getInstance().getInt(getId(), "increasePerRound") * round));
						sendToPlayers(say.toUpperCase());
						setStatus(EventState.CHECK);
						schedule(Configuration.getInstance().getInt(getId(), "roundTime") * 1000);
						break;
					case CHECK:
						if (removeAfkers())
						{
							setAllToFalse();
							setStatus(EventState.SAY);
							schedule(Configuration.getInstance().getInt(getId(), "roundTime") * 1000);
						}
						break;
					case END:
						setStatus(EventState.INACTIVE);
						forceStandAll();
						
						if (winner != null)
						{
							giveReward(winner);
							announce("Congratulation! " + winner.getName() + " won the event!");
							eventEnded();
						}
						else
						{
							announce("The mathc ended as a tie!");
						}
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
		SAY,
		CHECK,
		END,
		INACTIVE
	}
	
	public EventState eventState;
	
	private final Core task;
	
	public int round;
	
	public String say;
	
	private EventNpc spawn;
	
	public EventPlayer winner;
	
	protected int restriction = 1;
	
	public Simon(Integer containerId)
	{
		super(containerId);
		
		eventId = 4;
		createNewTeam(1, "All", Configuration.getInstance().getColor(getId(), "All"), Configuration.getInstance().getPosition(getId(), "All", 1));
		task = new Core();
		round = 0;
		spawn = null;
		winner = null;
	}
	
	public String createNewRandomString(int size)
	{
		String str = "";
		
		for (int i = 0; i < size; i++)
		{
			str = str + (char) (rnd.nextInt(26) + 97);
		}
		return str;
	}
	
	@Override
	public void endEvent()
	{
		winner = players.head().getNext().getValue();
		setStatus(EventState.END);
		schedule(1);
	}
	
	@Override
	public String getScorebar()
	{
		return "";
	}
	
	@Override
	public void onClockZero()
	{
	}
	
	@Override
	public void onSay(int type, EventPlayer player, String text)
	{
		if ((eventState == EventState.CHECK) && (player.getStatus() != -1))
		{
			if (text.equalsIgnoreCase(say))
			{
				player.setStatus(1);
				player.sendMessage("Correct!");
				player.increaseScore();
				player.setNameColor(0, 255, 0);
				player.broadcastUserInfo();
			}
			else
			{
				player.setStatus(-1);
				player.sendMessage("Wrong!");
				player.setNameColor(255, 0, 0);
				player.broadcastUserInfo();
			}
			
			int falses = 0;
			EventPlayer falsed = null;
			for (EventPlayer p : getPlayerList())
			{
				if (p.getStatus() == 0)
				{
					falses++;
					falsed = p;
				}
			}
			
			if (falses == 1)
			{
				int count = 0;
				for (EventPlayer pla : getPlayerList())
				{
					if (pla.getStatus() == 1)
					{
						count++;
					}
				}
				
				if (count >= 1)
				{
					falsed.sendMessage("Last one!");
					falsed.setNameColor(255, 0, 0);
					falsed.broadcastUserInfo();
					falsed.setStatus(-1);
				}
				
				if (count == 0)
				{
					winner = getPlayersWithStatus(0).head().getNext().getValue();
					setStatus(EventState.END);
					schedule(1);
				}
			}
			
			if (countOfPositiveStatus() == 1)
			{
				winner = getPlayersWithStatus(1).head().getNext().getValue();
				setStatus(EventState.END);
				schedule(1);
			}
		}
	}
	
	public boolean removeAfkers()
	{
		for (EventPlayer player : getPlayerList())
		{
			if (player.getStatus() == 0)
			{
				
				player.sendMessage("Timeout!");
				player.setNameColor(255, 0, 0);
				player.broadcastUserInfo();
				player.setStatus(-1);
			}
			
			if (countOfPositiveStatus() == 1)
			{
				if (getPlayersWithStatus(1).size() == 1)
				{
					winner = getPlayersWithStatus(1).head().getNext().getValue();
				}
				else
				{
					winner = null;
				}
				setStatus(EventState.END);
				schedule(1);
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void reset()
	{
		super.reset();
		round = 0;
		say = "";
		
		if (spawn != null)
		{
			spawn.unspawn();
		}
		
		spawn = null;
	}
	
	@Override
	public void schedule(int time)
	{
		Out.tpmScheduleGeneral(task, time);
	}
	
	public void sendToPlayers(String text)
	{
		for (EventPlayer player : getPlayerList())
		{
			player.simon(spawn.getId(), text);
		}
	}
	
	public void setAllToFalse()
	{
		for (EventPlayer player : getPlayerList())
		{
			if (player.getStatus() != -1)
			{
				player.setStatus(0);
				player.setNameColor(255, 255, 255);
				player.broadcastUserInfo();
			}
		}
	}
	
	public void setStatus(EventState s)
	{
		eventState = s;
	}
	
	@Override
	public void start()
	{
		int[] npcpos = Configuration.getInstance().getPosition(getId(), "Simon", 1);
		spawn = NpcContainer.getInstance().createNpc(npcpos[0], npcpos[1], npcpos[2], Configuration.getInstance().getInt(getId(), "simonNpcId"), instanceId);
		setStatus(EventState.START);
		schedule(1);
	}
	
	@Override
	public void createStatus()
	{
		status = new SingleEventStatus(containerId);
	}
}