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
package l2e.gameserver.model.entity.events.phoenix;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.gameserver.model.entity.events.phoenix.container.EventContainer;
import l2e.gameserver.model.entity.events.phoenix.container.PlayerContainer;
import l2e.gameserver.model.entity.events.phoenix.function.Buffer;
import l2e.gameserver.model.entity.events.phoenix.function.Vote;
import l2e.gameserver.model.entity.events.phoenix.io.Out;
import l2e.gameserver.model.entity.events.phoenix.model.Clock;
import l2e.gameserver.model.entity.events.phoenix.model.EventPlayer;
import l2e.gameserver.model.entity.events.phoenix.model.EventStatus;
import l2e.gameserver.model.entity.events.phoenix.model.PLoc;

public abstract class AbstractEvent
{
	public class AbstractCore implements Runnable
	{
		@Override
		public void run()
		{
			switch (phase)
			{
				case REGISTER:
					announce("The next event will be: " + Configuration.getInstance().getString(c.getId(), "eventName"));
					registerCountdown = new RegisterCountdown(Configuration.getInstance().getInt(0, "registerTime"));
					registerCountdown.start();
					break;
				case CHECK:
					if (players.size() < Configuration.getInstance().getInt(getId(), "minPlayers"))
					{
						announce("Theres not enough participant!");
						
						reset();
						
						if (Configuration.getInstance().getBoolean(0, "voteEnabled"))
						{
							Vote.getInstance().checkIfCurrent(c);
						}
						
						EventContainer.getInstance().removeEvent(containerId);
						
					}
					else
					{
						announce("Event started!");
						msgToAll("You'll be teleported to the event in 10 secs");
						if (Configuration.getInstance().getBoolean(0, "showEscapeEffect"))
						{
							showEscapeEffectOnAll();
						}
						setAbstractPhase(AbstractPhase.START);
						abstractSchedule(10000);
					}
					break;
				case START:
					setAbstractPhase(AbstractPhase.RUNNING);
					start();
					break;
				case RESET:
					teleBackEveryone();
					if (Configuration.getInstance().getBoolean(0, "voteEnabled"))
					{
						Vote.getInstance().checkIfCurrent(c);
					}
					reset();
					EventContainer.getInstance().removeEvent(containerId);
					break;
				default:
					break;
			}
		}
	}
	
	public enum AbstractPhase
	{
		REGISTER,
		CHECK,
		START,
		RUNNING,
		RESET
	}
	
	public class EventClock extends Clock
	{
		public EventClock(int time)
		{
			super(time);
		}
		
		@Override
		public void clockBody()
		{
			clockTick();
			scorebartext = getScorebar();
			for (EventPlayer player : getPlayerList())
			{
				player.scorebarPacket(scorebartext);
			}
		}
		
		@Override
		public void onZero()
		{
			onClockZero();
			
		}
	}
	
	public class RegisterCountdown extends Clock
	{
		public RegisterCountdown(int time)
		{
			super(time);
		}
		
		@Override
		public void clockBody()
		{
			switch (counter)
			{
				case 1800:
				case 1200:
				case 600:
				case 300:
				case 60:
					announce("" + (counter / 60) + " minutes left to register.");
					break;
				case 30:
				case 10:
				case 5:
					announce("" + counter + " seconds left to register.");
					break;
			}
		}
		
		@Override
		public void onZero()
		{
			setAbstractPhase(AbstractPhase.CHECK);
			abstractSchedule(1);
		}
	}
	
	public class ResurrectorTask implements Runnable
	{
		public EventPlayer player;
		
		public ResurrectorTask(EventPlayer p)
		{
			player = p;
			Out.tpmScheduleGeneral(this, 7000);
		}
		
		@Override
		public void run()
		{
			if ((player != null) && (player.getEvent() != null))
			{
				player.doRevive();
				
				if (Configuration.getInstance().getBoolean(0, "eventBufferEnabled"))
				{
					Buffer.getInstance().buffPlayer(player);
				}
				
				player.healToMax();
				teleportToTeamPos(player);
			}
		}
	}
	
	public AbstractEvent c = this;
	
	public int containerId;
	
	public int instanceId;
	
	public AbstractPhase phase;
	
	public AbstractCore abstractCore;
	
	public RegisterCountdown registerCountdown;
	
	public int eventId;
	
	public FastMap<Integer, Team> teams;
	
	public FastList<EventPlayer> players;
	
	public String scorebartext;
	
	public int winnerTeam;
	
	public Random rnd = new Random();
	
	public EventClock clock;
	
	public Calendar started;
	
	public EventStatus status;
	
	public abstract void createStatus();
	
	public AbstractEvent(int cId)
	{
		containerId = cId;
		instanceId = cId + 9100;
		Out.createInstance(instanceId);
		Out.setPvPInstance(instanceId);
		teams = new FastMap<>();
		players = new FastList<>();
		abstractCore = new AbstractCore();
		started = Calendar.getInstance();
		setAbstractPhase(AbstractPhase.REGISTER);
		abstractSchedule(1);
	}
	
	public void abstractSchedule(int time)
	{
		Out.tpmScheduleGeneral(abstractCore, time);
	}
	
	public void addToResurrector(EventPlayer player)
	{
		new ResurrectorTask(player);
	}
	
	public void announce(FastList<EventPlayer> list, String text)
	{
		for (EventPlayer player : list)
		{
			player.sendCreatureMessage("[Event] " + text);
		}
	}
	
	public void announce(String text)
	{
		Out.broadcastCreatureSay("[" + Configuration.getInstance().getString(getId(), "shortName") + "] " + text);
	}
	
	public boolean canAttack(EventPlayer player, EventPlayer target)
	{
		return true;
	}
	
	public boolean canRegister(EventPlayer player)
	{
		if (player.isJailed())
		{
			player.sendMessage("You cant register from the jail.");
			return false;
		}
		
		if (player.isInSiege())
		{
			player.sendMessage("You cant register while in siege.");
			return false;
		}
		
		if (player.isInDuel())
		{
			player.sendMessage("You cant register while you're dueling.");
			return false;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You cant register while youre in the olympiad.");
			return false;
		}
		
		if (player.getKarma() > 0)
		{
			player.sendMessage("You cant register if you have karma.");
			return false;
		}
		
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage("You cant register with a cursed weapon.");
			return false;
		}
		if (player.getLevel() > Configuration.getInstance().getInt(getId(), "maxLvl"))
		{
			player.sendMessage("You're higher than the max allowed lvl.");
			return false;
		}
		if (player.getLevel() < Configuration.getInstance().getInt(getId(), "minLvl"))
		{
			player.sendMessage("You're lower than the min allowed lvl.");
			return false;
		}
		return true;
	}
	
	public void clockTick()
	{
	}
	
	public int countOfPositiveStatus()
	{
		int count = 0;
		for (EventPlayer player : getPlayerList())
		{
			if (player.getStatus() >= 0)
			{
				count++;
			}
		}
		return count;
	}
	
	public void createNewTeam(int id, String name, int[] color, int[] startPos)
	{
		teams.put(id, new Team(id, name, color, startPos));
	}
	
	public void createPartyOfTeam(int teamId)
	{
		int count = 0;
		
		FastList<EventPlayer> list = new FastList<>();
		
		for (EventPlayer p : players)
		{
			if (p.getMainTeam() == teamId)
			{
				list.add(p);
			}
		}
		
		FastList<EventPlayer> sublist = new FastList<>();
		for (EventPlayer player : list)
		{
			if (((count % 9) == 0) && ((list.size() - count) != 1))
			{
				if (sublist.size() == 0)
				{
					sublist.add(player);
				}
				else
				{
					Out.createParty2(sublist);
					sublist.reset();
					sublist.add(player);
				}
			}
			if ((count % 9) < 9)
			{
				sublist.add(player);
			}
			count++;
		}
	}
	
	public void divideIntoTeams(int number)
	{
		FastList<EventPlayer> temp = new FastList<>(players);
		
		int i = 0;
		while (temp.size() != 0)
		{
			i++;
			EventPlayer player = temp.get(rnd.nextInt(temp.size()));
			player.setMainTeam(i);
			temp.remove(player);
			if (i == number)
			{
				i = 0;
			}
		}
	}
	
	public void dropBomb(EventPlayer player)
	{
	}
	
	public abstract void endEvent();
	
	public void eventEnded()
	{
		msgToAll("You will be teleported back in 10secs");
		
		for (EventPlayer player : players)
		{
			player.restoreTitle();
		}
		setAbstractPhase(AbstractPhase.RESET);
		abstractSchedule(10000);
	}
	
	public void forceSitAll()
	{
		for (EventPlayer player : players)
		{
			if (player.isCastingNow())
			{
				player.abortCast();
			}
			player.sitDown();
			player.setSitForced(true);
		}
	}
	
	public void forceStandAll()
	{
		for (EventPlayer player : players)
		{
			player.setSitForced(false);
			player.standUp();
			player.setTitle("<- 0 ->");
		}
	}
	
	public AbstractPhase getAbstractPhase()
	{
		return phase;
	}
	
	public int getId()
	{
		return eventId;
	}
	
	public FastList<EventPlayer> getPlayerList()
	{
		return players;
	}
	
	public FastList<EventPlayer> getPlayersOfTeam(int team)
	{
		FastList<EventPlayer> list = new FastList<>();
		
		for (EventPlayer player : getPlayerList())
		{
			if (player.getMainTeam() == team)
			{
				list.add(player);
			}
		}
		return list;
	}
	
	public Team getPlayersTeam(EventPlayer player)
	{
		return teams.get(player.getMainTeam());
	}
	
	public FastList<EventPlayer> getPlayersWithStatus(int status)
	{
		FastList<EventPlayer> list = new FastList<>();
		
		for (EventPlayer player : getPlayerList())
		{
			if (player.getStatus() == status)
			{
				list.add(player);
			}
		}
		return list;
	}
	
	public EventPlayer getPlayerWithMaxScore()
	{
		EventPlayer max;
		max = players.head().getNext().getValue();
		for (EventPlayer player : players)
		{
			if (player.getScore() > max.getScore())
			{
				max = player;
			}
		}
		return max;
	}
	
	public EventPlayer getRandomPlayer()
	{
		FastList<EventPlayer> temp = new FastList<>();
		for (EventPlayer player : players)
		{
			temp.add(player);
		}
		return temp.get(rnd.nextInt(temp.size()));
	}
	
	public EventPlayer getRandomPlayerFromTeam(int team)
	{
		FastList<EventPlayer> temp = new FastList<>();
		for (EventPlayer player : players)
		{
			if (player.getMainTeam() == team)
			{
				temp.add(player);
			}
		}
		return temp.get(rnd.nextInt(temp.size()));
	}
	
	public String getRegisterTimeLeft()
	{
		return registerCountdown.getTimeInString();
	}
	
	public Team getTeam(int id)
	{
		return teams.get(id);
	}
	
	public EventClock getClock()
	{
		return clock;
	}
	
	public abstract String getScorebar();
	
	public int getWinnerTeam()
	{
		FastList<Team> t = new FastList<>();
		
		for (Team team : teams.values())
		{
			if (t.size() == 0)
			{
				t.add(team);
				continue;
			}
			
			if (team.getScore() > t.getFirst().getScore())
			{
				t.clear();
				t.add(team);
				continue;
			}
			if (team.getScore() == t.getFirst().getScore())
			{
				t.add(team);
			}
		}
		
		if (t.size() > 1)
		{
			return t.get(rnd.nextInt(t.size())).getId();
		}
		return t.getFirst().getId();
	}
	
	public void giveReward(EventPlayer player)
	{
		FastList<InetAddress> ip = new FastList<>();
		
		FastMap<Integer, Integer> rewardmap = Configuration.getInstance().getRewards(getId(), "winner");
		
		for (Map.Entry<Integer, Integer> entry : rewardmap.entrySet())
		{
			player.addItem(entry.getKey(), entry.getValue(), true);
		}
		
		List<EventPlayer> losers = new LinkedList<>();
		losers.addAll(getPlayerList());
		losers.remove(player);
		
		FastMap<Integer, Integer> loserRewards = Configuration.getInstance().getRewards(getId(), "loser");
		
		for (EventPlayer loser : losers)
		{
			InetAddress ipc = player.getInetAddress();
			
			if (ipc == null)
			{
				continue;
			}
			
			if (ip.contains(ipc))
			{
				continue;
			}
			
			ip.add(ipc);
			
			for (Map.Entry<Integer, Integer> entry : loserRewards.entrySet())
			{
				loser.addItem(entry.getKey(), entry.getValue(), true);
			}
		}
	}
	
	public void giveReward(FastList<EventPlayer> players)
	{
		FastList<InetAddress> ip = new FastList<>();
		
		FastMap<Integer, Integer> rewardmap = Configuration.getInstance().getRewards(getId(), "winner");
		
		for (EventPlayer player : players)
		{
			InetAddress ipc = player.getInetAddress();
			
			if (ipc == null)
			{
				continue;
			}
			
			if (ip.contains(ipc))
			{
				continue;
			}
			
			ip.add(ipc);
			
			for (Map.Entry<Integer, Integer> entry : rewardmap.entrySet())
			{
				player.addItem(entry.getKey(), entry.getValue(), true);
			}
		}
		
		List<EventPlayer> losers = new LinkedList<>();
		losers.addAll(getPlayerList());
		losers.remove(players);
		
		FastMap<Integer, Integer> loserRewards = Configuration.getInstance().getRewards(getId(), "loser");
		
		for (EventPlayer loser : losers)
		{
			InetAddress ipc = loser.getInetAddress();
			
			if (ip.contains(ipc))
			{
				continue;
			}
			
			ip.add(ipc);
			
			for (Map.Entry<Integer, Integer> entry : loserRewards.entrySet())
			{
				loser.addItem(entry.getKey(), entry.getValue(), true);
			}
		}
	}
	
	public boolean isRunning()
	{
		if (phase == AbstractPhase.RUNNING)
		{
			return true;
		}
		return false;
	}
	
	public void msgToAll(String text)
	{
		for (EventPlayer player : players)
		{
			player.sendMessage(text);
		}
	}
	
	public int numberOfTeams()
	{
		return teams.size();
	}
	
	public abstract void onClockZero();
	
	public void onDie(EventPlayer victim, EventPlayer killer)
	{
		return;
	}
	
	public void onHit(EventPlayer actor, EventPlayer target)
	{
	}
	
	public void onKill(EventPlayer victim, EventPlayer killer)
	{
		return;
	}
	
	public void onLogout(EventPlayer player)
	{
		if (players.contains(player))
		{
			removePlayer(player);
		}
		
		player.teleport(player.getOrigLoc(), 0, true, 0);
		player.restoreData();
		PlayerContainer.getInstance().deleteInfo(player);
		
		if (teams.size() == 1)
		{
			if (getPlayerList().size() == 1)
			{
				endEvent();
				return;
			}
		}
		
		if (teams.size() > 1)
		{
			int t = players.head().getNext().getValue().getMainTeam();
			for (EventPlayer p : getPlayerList())
			{
				if (p.getMainTeam() != t)
				{
					return;
				}
			}
			
			endEvent();
			return;
		}
	}
	
	public void onSay(int type, EventPlayer player, String text)
	{
		return;
	}
	
	public boolean onTalkNpc(Integer npc, EventPlayer player)
	{
		return false;
	}
	
	public boolean onUseItem(EventPlayer player, Integer item)
	{
		if (Configuration.getInstance().getRestriction(0, "item").contains(item) || Configuration.getInstance().getRestriction(getId(), "item").contains(item))
		{
			return false;
		}
		
		if (Out.isPotion(item) && !Configuration.getInstance().getBoolean(getId(), "allowPotions"))
		{
			return false;
		}
		
		if (Out.isScroll(item))
		{
			return false;
		}
		return true;
	}
	
	public boolean onUseMagic(EventPlayer player, Integer skill)
	{
		if (Configuration.getInstance().getRestriction(0, "skill").contains(skill) || Configuration.getInstance().getRestriction(getId(), "skill").contains(skill))
		{
			return false;
		}
		
		if (Out.isRestrictedSkill(skill))
		{
			return false;
		}
		return true;
	}
	
	public void prepare(EventPlayer player)
	{
		if (player.isCastingNow())
		{
			player.abortCast();
		}
		
		player.setVisible();
		
		player.unsummonPet();
		
		if (Configuration.getInstance().getBoolean(getId(), "removeBuffs"))
		{
			player.stopAllEffects();
		}
		
		player.removeFromParty();
		
		int[] nameColor = getPlayersTeam(player).getTeamColor();
		player.setNameColor(nameColor[0], nameColor[1], nameColor[2]);
		
		if (Configuration.getInstance().getBoolean(0, "eventBufferEnabled"))
		{
			Buffer.getInstance().buffPlayer(player);
		}
		
		if (player.isDead())
		{
			player.doRevive();
		}
		
		player.healToMax();
		player.broadcastUserInfo();
	}
	
	public void preparePlayers()
	{
		for (EventPlayer player : players)
		{
			prepare(player);
		}
	}
	
	public boolean registerPlayer(Integer player)
	{
		EventPlayer pi = PlayerContainer.getInstance().getPlayer(player);
		if (pi != null)
		{
			pi.sendMessage("You already registered to the event!");
			PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
			return false;
		}
		
		pi = PlayerContainer.getInstance().createInfo(player);
		
		if (Configuration.getInstance().getBoolean(0, "ipCheckOnRegister"))
		{
			for (EventPlayer p : PlayerContainer.getInstance().getPlayers())
			{
				if (p.getInetAddress().equals(pi.getInetAddress()) && (p.getOwner().getObjectId() == pi.getOwner().getObjectId()))
				{
					pi.sendMessage("This IP address is already registered to an event");
					return false;
				}
			}
		}
		
		if (getAbstractPhase() != AbstractPhase.REGISTER)
		{
			pi.sendMessage("You can't register now!");
			PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
			return false;
		}
		
		if (Configuration.getInstance().getBoolean(0, "eventBufferEnabled"))
		{
			if (!Buffer.getInstance().playerHaveTemplate(player))
			{
				pi.sendMessage("You have to set a buff template first!");
				Buffer.getInstance().showHtml(player);
				PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
				return false;
			}
		}
		if (canRegister(pi))
		{
			pi.sendMessage("You succesfully registered to the event!");
			pi.setEvent(this);
			pi.initOrigInfo();
			players.add(pi);
			return true;
		}
		pi.sendMessage("You failed on registering to the event!");
		PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
		return false;
	}
	
	public void removePlayer(EventPlayer player)
	{
		players.remove(player);
	}
	
	protected void reset()
	{
		for (EventPlayer p : players)
		{
			PlayerContainer.getInstance().deleteInfo(p);
		}
		players.clear();
		Out.tpmPurge();
		winnerTeam = 0;
		
		for (Team team : teams.values())
		{
			team.setScore(0);
		}
	}
	
	public abstract void schedule(int time);
	
	public void setAbstractPhase(AbstractPhase p)
	{
		phase = p;
	}
	
	public void showEscapeEffectOnAll()
	{
		for (EventPlayer player : PlayerContainer.getInstance().getPlayers())
		{
			player.showEscapeEffect();
		}
	}
	
	public abstract void start();
	
	public void teleBackEveryone()
	{
		for (EventPlayer player : getPlayerList())
		{
			player.teleport(player.getOrigLoc(), 0, true, 0);
			player.restoreData();
			player.removeFromParty();
			
			player.broadcastUserInfo();
			if (player.isDead())
			{
				player.doRevive();
			}
		}
	}
	
	public void teleportPlayer(EventPlayer player, int[] coordinates, int instance)
	{
		player.teleport(new PLoc(coordinates[0], coordinates[1], coordinates[2]), 0, true, instance);
	}
	
	public void teleportToTeamPos()
	{
		for (EventPlayer player : players)
		{
			teleportToTeamPos(player);
		}
	}
	
	public void teleportToTeamPos(EventPlayer player)
	{
		
		int[] pos = Configuration.getInstance().getPosition(getId(), teams.get(player.getMainTeam()).getName(), 0);
		teleportPlayer(player, pos, instanceId);
	}
	
	public boolean unregisterPlayer(EventPlayer pi)
	{
		if (getAbstractPhase() != AbstractPhase.REGISTER)
		{
			pi.sendMessage("You can't unregister now!");
			return false;
		}
		pi.sendMessage("You succesfully unregistered from the event!");
		PlayerContainer.getInstance().deleteInfo(pi.getPlayersId());
		players.remove(pi);
		return true;
	}
	
	public boolean unregisterPlayer(Integer player)
	{
		EventPlayer pi = PlayerContainer.getInstance().getPlayer(player);
		if (pi == null)
		{
			Out.sendMessage(player, "You're not registered to the event!");
			return false;
		}
		return unregisterPlayer(pi);
	}
	
	public void useCapture(EventPlayer player, Integer base)
	{
	}
	
	public String getStarted()
	{
		return "" + (started.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + started.get(Calendar.HOUR_OF_DAY) : started.get(Calendar.HOUR_OF_DAY)) + ":" + (started.get(Calendar.MINUTE) < 10 ? "0" + started.get(Calendar.MINUTE) : started.get(Calendar.MINUTE));
	}
	
	public EventStatus getStatus()
	{
		return status;
	}
}