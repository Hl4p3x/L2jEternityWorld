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

public class Bomb extends AbstractEvent
{
	public static boolean enabled = true;
	
	protected class BombTask implements Runnable
	{
		private final EventPlayer owner;
		private final EventNpc bomb;
		private FastList<EventPlayer> victims;
		
		protected BombTask(EventPlayer p)
		{
			owner = p;
			bomb = NpcContainer.getInstance().createNpc(owner.getOwnerLoc().getX(), owner.getOwnerLoc().getY(), owner.getOwnerLoc().getZ(), Configuration.getInstance().getInt(getId(), "bombNpcId"), instanceId);
			bomb.setTitle(owner.getMainTeam() == 1 ? "Blue" : "Red");
			bomb.broadcastStatusUpdate();
			
			for (EventPlayer pl : getPlayerList())
			{
				pl.sendAbstractNpcInfo(bomb);
			}
			Out.tpmScheduleGeneral(this, 3000);
		}
		
		@Override
		public void run()
		{
			victims = new FastList<>();
			
			for (EventPlayer victim : getPlayerList())
			{
				if ((owner.getMainTeam() != victim.getMainTeam()) && (Math.sqrt(victim.getPlanDistanceSq(bomb.getNpc())) <= Configuration.getInstance().getInt(getId(), "bombRadius")))
				{
					victim.doDie();
					owner.increaseScore();
					
					victims.add(victim);
					
					if (victim.getMainTeam() == 1)
					{
						teams.get(2).increaseScore();
					}
					if (victim.getMainTeam() == 2)
					{
						teams.get(1).increaseScore();
					}
				}
			}
			if (victims.size() != 0)
			{
				bomb.showBombEffect(victims);
				victims.clear();
			}
			bomb.unspawn();
		}
	}
	
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
						removeSkill();
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
	
	protected enum EventState
	{
		START,
		FIGHT,
		END,
		TELEPORT,
		INACTIVE
	}
	
	protected EventState eventState;
	
	private final Core task;
	
	public Bomb(Integer containerId)
	{
		super(containerId);
		
		eventId = 8;
		createNewTeam(1, "Blue", Configuration.getInstance().getColor(getId(), "Blue"), Configuration.getInstance().getPosition(getId(), "Blue", 1));
		createNewTeam(2, "Red", Configuration.getInstance().getColor(getId(), "Red"), Configuration.getInstance().getPosition(getId(), "Red", 1));
		task = new Core();
		winnerTeam = 0;
		clock = new EventClock(Configuration.getInstance().getInt(getId(), "matchTime"));
	}
	
	@Override
	public boolean canAttack(EventPlayer player, EventPlayer target)
	{
		return false;
	}
	
	@Override
	public void dropBomb(EventPlayer player)
	{
		new BombTask(player);
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
	
	protected void giveSkill()
	{
		for (EventPlayer player : getPlayerList())
		{
			player.addSkill(Configuration.getInstance().getInt(getId(), "bombSkillId"), 1);
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
	
	@Override
	public void onLogout(EventPlayer player)
	{
		player.removeSkill(Configuration.getInstance().getInt(getId(), "bombSkillId"), 1);
	}
	
	@Override
	public boolean onUseMagic(EventPlayer player, Integer skill)
	{
		if (Integer.valueOf(skill) == Configuration.getInstance().getInt(getId(), "bombSkillId"))
		{
			return true;
		}
		return false;
	}
	
	protected void removeSkill()
	{
		for (EventPlayer player : getPlayerList())
		{
			player.removeSkill(Configuration.getInstance().getInt(getId(), "bombSkillId"), 1);
		}
	}
	
	@Override
	public void schedule(int time)
	{
		Out.tpmScheduleGeneral(task, time);
	}
	
	protected void setStatus(EventState s)
	{
		eventState = s;
	}
	
	@Override
	public void start()
	{
		setStatus(EventState.START);
		schedule(1);
	}
	
	@Override
	public void createStatus()
	{
		status = new TeamEventStatus(containerId);
		
	}
}