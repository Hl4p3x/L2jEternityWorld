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

import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.gameserver.model.entity.events.phoenix.AbstractEvent;
import l2e.gameserver.model.entity.events.phoenix.Configuration;
import l2e.gameserver.model.entity.events.phoenix.container.NpcContainer;
import l2e.gameserver.model.entity.events.phoenix.io.Out;
import l2e.gameserver.model.entity.events.phoenix.model.EventNpc;
import l2e.gameserver.model.entity.events.phoenix.model.EventPlayer;
import l2e.gameserver.model.entity.events.phoenix.model.SingleEventStatus;

public class Russian extends AbstractEvent
{
	public static boolean enabled = true;
	
	protected int c;
	
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
						spawnRussians();
						setStatus(EventState.CHOOSE);
						schedule(20000);
						break;
					case CHOOSE:
						if (round == 0)
						{
							forceStandAll();
						}
						round++;
						announce("Choose a russian!");
						setStatus(EventState.CHECK);
						schedule(Configuration.getInstance().getInt(getId(), "roundTime") * 1000);
						break;
					case CHECK:
						removeAfkers();
						killRandomRussian();
						
						if (countOfPositiveStatus() != 0)
						{
							if (russians.size() != 1)
							{
								for (EventPlayer player : getPlayersWithStatus(1))
								{
									player.setStatus(0);
									player.increaseScore();
									player.setNameColor(255, 255, 255);
									player.broadcastUserInfo();
								}
								
								for (FastList<EventPlayer> chose : choses.values())
								{
									chose.reset();
								}
								setStatus(EventState.CHOOSE);
								schedule(Configuration.getInstance().getInt(getId(), "roundTime") * 1000);
							}
							else
							{
								for (EventPlayer player : getPlayersWithStatus(1))
								{
									giveReward(player);
								}
								unspawnRussians();
								announce("Congratulation! " + countOfPositiveStatus() + " player survived the event!");
								eventEnded();
							}
						}
						else
						{
							unspawnRussians();
							announce("Unfortunatly noone survived the event!");
							eventEnded();
						}
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
		CHOOSE,
		CHECK,
		INACTIVE
	}
	
	public EventState eventState;
	
	private final Core task;
	
	public int round;
	
	public FastMap<Integer, EventNpc> russians;
	
	public FastMap<Integer, FastList<EventPlayer>> choses;
	
	public Russian(Integer containerId)
	{
		super(containerId);
		
		eventId = 7;
		createNewTeam(1, "All", Configuration.getInstance().getColor(getId(), "All"), Configuration.getInstance().getPosition(getId(), "All", 1));
		task = new Core();
		round = 0;
		russians = new FastMap<>();
		choses = new FastMap<>();
	}
	
	@Override
	public boolean canAttack(EventPlayer player, EventPlayer target)
	{
		return false;
	}
	
	@Override
	public void endEvent()
	{
		EventPlayer winner = players.head().getNext().getValue();
		giveReward(winner);
		unspawnRussians();
		announce("Congratulation! 1 player survived the event!");
		eventEnded();
	}
	
	@Override
	public String getScorebar()
	{
		return "";
	}
	
	public void killRandomRussian()
	{
		FastList<Integer> ids = new FastList<>();
		for (int id : russians.keySet())
		{
			ids.add(id);
		}
		int russnum = ids.get(rnd.nextInt(ids.size()));
		EventNpc russian = russians.get(russnum);
		russian.unspawn();
		announce(getPlayerList(), "The #" + russnum + " russian died.");
		
		for (EventPlayer victim : choses.get(russnum))
		{
			victim.setStatus(-1);
			victim.doDieNpc(russian.getId());
			victim.sendMessage("Your russian died!");
			victim.setNameColor(255, 255, 255);
		}
		russians.remove(russnum);
	}
	
	@Override
	public void onClockZero()
	{
	}
	
	@Override
	public void onLogout(EventPlayer player)
	{
		super.onLogout(player);
		for (FastList<EventPlayer> list : choses.values())
		{
			if (list.contains(player))
			{
				list.remove(player);
			}
		}
	}
	
	@Override
	public boolean onTalkNpc(Integer npc, EventPlayer player)
	{
		EventNpc npci = NpcContainer.getInstance().getNpc(npc);
		
		if (npci == null)
		{
			return false;
		}
		
		if (!russians.containsValue(npci))
		{
			return false;
		}
		
		if (player.getStatus() != 0)
		{
			return true;
		}
		
		for (Map.Entry<Integer, EventNpc> russian : russians.entrySet())
		{
			if (russian.getValue().equals(npci))
			{
				choses.get(russian.getKey()).add(player);
				player.setNameColor(0, 255, 0);
				player.broadcastUserInfo();
				player.setStatus(1);
			}
		}
		return true;
	}
	
	public void removeAfkers()
	{
		c = 0;
		for (EventPlayer player : getPlayerList())
		{
			if (player.getStatus() == 0)
			{
				player.sendMessage("Timeout!");
				player.doDie();
				player.setStatus(-1);
				c++;
			}
		}
	}
	
	@Override
	protected void reset()
	{
		super.reset();
		round = 0;
		russians.clear();
		choses.clear();
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
	
	public void spawnRussians()
	{
		for (int i = 1; i <= Configuration.getInstance().getInt(getId(), "numberOfRussians"); i++)
		{
			int[] pos = Configuration.getInstance().getPosition(getId(), "Russian", i);
			russians.put(i, NpcContainer.getInstance().createNpc(pos[0], pos[1], pos[2], Configuration.getInstance().getInt(getId(), "russianNpcId"), instanceId));
			choses.put(i, new FastList<EventPlayer>());
			russians.get(i).setTitle("--" + i + "--");
		}
	}
	
	@Override
	public void start()
	{
		setStatus(EventState.START);
		schedule(1);
	}
	
	public void unspawnRussians()
	{
		for (EventNpc russian : russians.values())
		{
			russian.unspawn();
		}
	}
	
	@Override
	public void createStatus()
	{
		status = new SingleEventStatus(containerId);
		
	}
}