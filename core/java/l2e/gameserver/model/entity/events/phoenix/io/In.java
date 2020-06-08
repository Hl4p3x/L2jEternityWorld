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
package l2e.gameserver.model.entity.events.phoenix.io;

import l2e.gameserver.model.entity.events.phoenix.AbstractEvent.AbstractPhase;
import l2e.gameserver.model.entity.events.phoenix.Configuration;
import l2e.gameserver.model.entity.events.phoenix.Interface;
import l2e.gameserver.model.entity.events.phoenix.ManagerNpc;
import l2e.gameserver.model.entity.events.phoenix.container.EventContainer;
import l2e.gameserver.model.entity.events.phoenix.container.PlayerContainer;
import l2e.gameserver.model.entity.events.phoenix.function.Buffer;
import l2e.gameserver.model.entity.events.phoenix.function.Scheduler;
import l2e.gameserver.model.entity.events.phoenix.function.Vote;
import l2e.gameserver.model.entity.events.phoenix.model.EventPlayer;

public class In
{
	private static class SingletonHolder
	{
		protected static final In _instance = new In();
	}
	
	public static final In getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public boolean areTeammates(EventPlayer player, EventPlayer target)
	{
		if (player.getEvent() == null)
		{
			return false;
		}
		
		if (player.getEvent().numberOfTeams() == 1)
		{
			return false;
		}
		
		if (player.getEvent().numberOfTeams() > 1)
		{
			if (player.getMainTeam() == target.getMainTeam())
			{
				return true;
			}
			return false;
		}
		return false;
	}
	
	public boolean areTeammates(Integer player, Integer target)
	{
		if ((PlayerContainer.getInstance().getPlayer(player) != null) && (PlayerContainer.getInstance().getPlayer(target) != null))
		{
			return areTeammates(PlayerContainer.getInstance().getPlayer(player), PlayerContainer.getInstance().getPlayer(target));
		}
		return false;
	}
	
	public boolean canAttack(EventPlayer player, EventPlayer target)
	{
		return player.getEvent().canAttack(player, target);
	}
	
	public boolean canAttack(Integer player, Integer target)
	{
		if ((PlayerContainer.getInstance().getPlayer(player) != null) && (PlayerContainer.getInstance().getPlayer(target) != null))
		{
			return canAttack(PlayerContainer.getInstance().getPlayer(player), PlayerContainer.getInstance().getPlayer(target));
		}
		return true;
	}
	
	public boolean canTargetPlayer(EventPlayer target, EventPlayer self)
	{
		if (self.getEvent().getAbstractPhase() == AbstractPhase.RUNNING)
		{
			if ((isParticipating(target) && isParticipating(self)) || (!isParticipating(target) && !isParticipating(self)))
			{
				return true;
			}
			return false;
		}
		return true;
	}
	
	public boolean canTargetPlayer(Integer target, Integer self)
	{
		if ((PlayerContainer.getInstance().getPlayer(target) != null) && (PlayerContainer.getInstance().getPlayer(self) != null))
		{
			return canTargetPlayer(PlayerContainer.getInstance().getPlayer(target), PlayerContainer.getInstance().getPlayer(self));
		}
		return true;
	}
	
	public boolean canUseSkill(Integer player, Integer skill)
	{
		return Configuration.getInstance().getBoolean(PlayerContainer.getInstance().getPlayer(player).getEvent().getId(), "allowUseMagic");
	}
	
	public void eventOnLogout(Integer player)
	{
		EventPlayer pi = PlayerContainer.getInstance().getPlayer(player);
		if (pi != null)
		{
			PlayerContainer.getInstance().getPlayer(player).getEvent().onLogout(pi);
		}
	}
	
	public boolean isParticipating(EventPlayer player)
	{
		if (player.getEvent() != null)
		{
			return player.getEvent().getAbstractPhase() == AbstractPhase.RUNNING;
		}
		return false;
	}
	
	public boolean isParticipating(Integer player)
	{
		if (isRegistered(player))
		{
			return isParticipating(PlayerContainer.getInstance().getPlayer(player));
		}
		return false;
	}
	
	public boolean isRegistered(Integer player)
	{
		if (PlayerContainer.getInstance().getPlayer(player) != null)
		{
			return true;
		}
		return false;
	}
	
	public boolean isRunning(Integer player)
	{
		if ((PlayerContainer.getInstance().getPlayer(player) != null) && (PlayerContainer.getInstance().getPlayer(player).getEvent() != null))
		{
			if (PlayerContainer.getInstance().getPlayer(player).getEvent().getAbstractPhase() == AbstractPhase.RUNNING)
			{
				return true;
			}
		}
		return false;
	}
	
	public void onDie(EventPlayer victim, EventPlayer killer)
	{
		victim.getEvent().onDie(victim, killer);
	}
	
	public void onDie(Integer player, Integer target)
	{
		if ((PlayerContainer.getInstance().getPlayer(player) != null) && (PlayerContainer.getInstance().getPlayer(target) != null))
		{
			onDie(PlayerContainer.getInstance().getPlayer(player), PlayerContainer.getInstance().getPlayer(target));
		}
		else
		{
			return;
		}
	}
	
	public void onHit(EventPlayer actor, EventPlayer target)
	{
		actor.getEvent().onHit(actor, target);
	}
	
	public void onHit(Integer actor, Integer target)
	{
		if ((PlayerContainer.getInstance().getPlayer(actor) != null) && (PlayerContainer.getInstance().getPlayer(target) != null))
		{
			onHit(PlayerContainer.getInstance().getPlayer(actor), PlayerContainer.getInstance().getPlayer(target));
		}
		else
		{
			return;
		}
	}
	
	public void onKill(EventPlayer victim, EventPlayer killer)
	{
		killer.getEvent().onKill(victim, killer);
	}
	
	public void onKill(Integer player, Integer target)
	{
		if ((PlayerContainer.getInstance().getPlayer(player) != null) && (PlayerContainer.getInstance().getPlayer(target) != null))
		{
			onKill(PlayerContainer.getInstance().getPlayer(player), PlayerContainer.getInstance().getPlayer(target));
		}
		else
		{
			return;
		}
	}
	
	public void onLogout(Integer player)
	{
		if (Configuration.getInstance().getBoolean(0, "voteEnabled") && Vote.getInstance().votes.containsKey(player))
		{
			Vote.getInstance().votes.remove(player);
		}
		if (PlayerContainer.getInstance().getPlayer(player) != null)
		{
			PlayerContainer.getInstance().deleteInfo(player);
		}
	}
	
	public void onSay(int type, EventPlayer player, String text)
	{
		player.getEvent().onSay(type, player, text);
	}
	
	public void onSay(int type, Integer player, String text)
	{
		EventPlayer pi = PlayerContainer.getInstance().getPlayer(player);
		if (pi != null)
		{
			onSay(type, pi, text);
		}
	}
	
	public boolean onTalkNpc(Integer npc, Integer player)
	{
		EventPlayer pi = PlayerContainer.getInstance().getPlayer(player);
		if (pi != null)
		{
			return pi.getEvent().onTalkNpc(npc, pi);
		}
		return false;
	}
	
	public boolean onUseItem(Integer player, Integer npc)
	{
		EventPlayer pi = PlayerContainer.getInstance().getPlayer(player);
		if (pi != null)
		{
			return pi.getEvent().onUseItem(pi, npc);
		}
		return false;
	}
	
	public boolean onUseMagic(Integer player, Integer npc)
	{
		EventPlayer pi = PlayerContainer.getInstance().getPlayer(player);
		if (pi != null)
		{
			return pi.getEvent().onUseMagic(pi, npc);
		}
		return false;
	}
	
	public boolean getBoolean(String propName, Integer player)
	{
		if (player == 0)
		{
			return Configuration.getInstance().getBoolean(0, propName);
		}
		return Configuration.getInstance().getBoolean(PlayerContainer.getInstance().getPlayer(player).getEvent().getId(), propName);
	}
	
	public int getInt(String propName, Integer player)
	{
		if (player == 0)
		{
			return Configuration.getInstance().getInt(0, propName);
		}
		return Configuration.getInstance().getInt(PlayerContainer.getInstance().getPlayer(player).getEvent().getId(), propName);
	}
	
	public void registerPlayer(Integer player, Integer eventId)
	{
		EventContainer.getInstance().getEvent(eventId).registerPlayer(player);
	}
	
	public void showVoteList(Integer player)
	{
		ManagerNpc.getInstance().showVoteList(player);
	}
	
	public void unregisterPlayer(Integer player, Integer eventId)
	{
		EventContainer.getInstance().getEvent(eventId).unregisterPlayer(player);
	}
	
	public void showFirstHtml(Integer player, int obj)
	{
		ManagerNpc.getInstance().showMain(player);
	}
	
	public void showRegisterPage(Integer player, Integer event)
	{
		ManagerNpc.getInstance().showRegisterPage(player, event, 0);
	}
	
	public void addVote(Integer player, int event)
	{
		Vote.getInstance().addVote(player, event);
	}
	
	public void bypass(Integer player, String command)
	{
		if (command.startsWith("vote "))
		{
			Vote.getInstance().addVote(player, Integer.parseInt(command.substring(5)));
		}
		else if (command.equals("buffershow"))
		{
			Buffer.getInstance().showHtml(player);
		}
		else if (command.startsWith("buffer "))
		{
			Buffer.getInstance().changeList(player, Integer.parseInt(command.substring(7, command.length() - 2)), (Integer.parseInt(command.substring(command.length() - 1)) == 0 ? false : true));
			Buffer.getInstance().showHtml(player);
		}
		else if (command.startsWith("register "))
		{
			EventContainer.getInstance().getEvent(Integer.parseInt(command.substring(9))).registerPlayer(player);
		}
		else if (command.startsWith("showreg"))
		{
			ManagerNpc.getInstance().showRegisterPage(player, Integer.parseInt(command.substring(8), command.length() - 2), Integer.parseInt(command.substring(command.length() - 1)));
		}
		else if (command.startsWith("unregister"))
		{
			EventContainer.getInstance().getEvent(Integer.parseInt(command.substring(11))).unregisterPlayer(player);
		}
		else if (command.equals("showvotelist"))
		{
			ManagerNpc.getInstance().showVoteList(player);
		}
		else if (command.startsWith("mainmenu"))
		{
			Interface.showFirstHtml(player, 0);
		}
		else if (command.equals("scheduler"))
		{
			Scheduler.getInstance().scheduleList(player);
		}
		else if (command.startsWith("status"))
		{
			ManagerNpc.getInstance().showStatusPage(player, Integer.parseInt(command.substring(7)));
		}
		else if (command.equals("running"))
		{
			ManagerNpc.getInstance().showRunningList(player);
		}
	}
	
	public boolean logout(Integer player)
	{
		if (isParticipating(player) && !getBoolean("restartAllowed", 0))
		{
			PlayerContainer.getInstance().getPlayer(player).sendMessage("You cannot logout while you are a participant in an event.");
			return true;
		}
		return false;
	}
	
	public void shutdown()
	{
		Buffer.getInstance().updateSQL();
	}
	
	public boolean talkNpc(Integer player, Integer npc)
	{
		if (npc == Configuration.getInstance().getInt(0, "managerNpcId"))
		{
			ManagerNpc.getInstance().showMain(player);
			return true;
		}
		
		if (isParticipating(player))
		{
			if (onTalkNpc(npc, player))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean doAttack(Integer self, Integer target)
	{
		if (isParticipating(self) && isParticipating(target))
		{
			if (areTeammates(self, target) && Configuration.getInstance().getBoolean(0, "friendlyFireEnabled"))
			{
				return true;
			}
		}
		
		if (!canAttack(self, target))
		{
			return true;
		}
		return false;
	}
}