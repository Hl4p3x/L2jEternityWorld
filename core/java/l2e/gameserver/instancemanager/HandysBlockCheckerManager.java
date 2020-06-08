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
package l2e.gameserver.instancemanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.tasks.PenaltyRemoveTask;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExCubeGameAddPlayer;
import l2e.gameserver.network.serverpackets.ExCubeGameChangeTeam;
import l2e.gameserver.network.serverpackets.ExCubeGameRemovePlayer;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class HandysBlockCheckerManager
{
	private static final ArenaParticipantsHolder[] _arenaPlayers = new ArenaParticipantsHolder[4];
	
	private static final Map<Integer, Integer> _arenaVotes = new HashMap<>();
	
	private static final Map<Integer, Boolean> _arenaStatus = new HashMap<>();
	
	protected static Set<Integer> _registrationPenalty = Collections.synchronizedSet(new HashSet<Integer>());
	
	public synchronized int getArenaVotes(int arenaId)
	{
		return _arenaVotes.get(arenaId);
	}
	
	public synchronized void increaseArenaVotes(int arena)
	{
		int newVotes = _arenaVotes.get(arena) + 1;
		ArenaParticipantsHolder holder = _arenaPlayers[arena];
		
		if ((newVotes > (holder.getAllPlayers().size() / 2)) && !holder.getEvent().isStarted())
		{
			clearArenaVotes(arena);
			if ((holder.getBlueTeamSize() == 0) || (holder.getRedTeamSize() == 0))
			{
				return;
			}
			if (Config.HBCE_FAIR_PLAY)
			{
				holder.checkAndShuffle();
			}
			ThreadPoolManager.getInstance().executeTask(holder.getEvent().new StartEvent());
		}
		else
		{
			_arenaVotes.put(arena, newVotes);
		}
	}
	
	public synchronized void clearArenaVotes(int arena)
	{
		_arenaVotes.put(arena, 0);
	}
	
	protected HandysBlockCheckerManager()
	{
		_arenaStatus.put(0, false);
		_arenaStatus.put(1, false);
		_arenaStatus.put(2, false);
		_arenaStatus.put(3, false);
		
		_arenaVotes.put(0, 0);
		_arenaVotes.put(1, 0);
		_arenaVotes.put(2, 0);
		_arenaVotes.put(3, 0);
	}
	
	public ArenaParticipantsHolder getHolder(int arena)
	{
		return _arenaPlayers[arena];
	}
	
	public void startUpParticipantsQueue()
	{
		for (int i = 0; i < 4; ++i)
		{
			_arenaPlayers[i] = new ArenaParticipantsHolder(i);
		}
	}
	
	public boolean addPlayerToArena(L2PcInstance player, int arenaId)
	{
		ArenaParticipantsHolder holder = _arenaPlayers[arenaId];
		
		synchronized (holder)
		{
			boolean isRed;
			
			for (int i = 0; i < 4; i++)
			{
				if (_arenaPlayers[i].getAllPlayers().contains(player))
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST);
					msg.addCharName(player);
					player.sendPacket(msg);
					return false;
				}
			}
			
			if (player.isCursedWeaponEquipped())
			{
				player.sendPacket(SystemMessageId.CANNOT_REGISTER_PROCESSING_CURSED_WEAPON);
				return false;
			}
			
			if (player.isOnEvent() || player.isInOlympiadMode())
			{
				player.sendMessage("Couldnt register you due other event participation");
				return false;
			}
			
			if (OlympiadManager.getInstance().isRegistered(player))
			{
				OlympiadManager.getInstance().unRegisterNoble(player);
				player.sendPacket(SystemMessageId.COLISEUM_OLYMPIAD_KRATEIS_APPLICANTS_CANNOT_PARTICIPATE);
			}
			
			if (_registrationPenalty.contains(player.getObjectId()))
			{
				player.sendPacket(SystemMessageId.CANNOT_REQUEST_REGISTRATION_10_SECS_AFTER);
				return false;
			}
			
			if (holder.getBlueTeamSize() < holder.getRedTeamSize())
			{
				holder.addPlayer(player, 1);
				isRed = false;
			}
			else
			{
				holder.addPlayer(player, 0);
				isRed = true;
			}
			holder.broadCastPacketToTeam(new ExCubeGameAddPlayer(player, isRed));
			return true;
		}
	}
	
	public void removePlayer(L2PcInstance player, int arenaId, int team)
	{
		ArenaParticipantsHolder holder = _arenaPlayers[arenaId];
		synchronized (holder)
		{
			boolean isRed = team == 0 ? true : false;
			
			holder.removePlayer(player, team);
			holder.broadCastPacketToTeam(new ExCubeGameRemovePlayer(player, isRed));
			
			int teamSize = isRed ? holder.getRedTeamSize() : holder.getBlueTeamSize();
			if (teamSize == 0)
			{
				holder.getEvent().endEventAbnormally();
			}
			_registrationPenalty.add(player.getObjectId());
			schedulePenaltyRemoval(player.getObjectId());
		}
	}
	
	public void changePlayerToTeam(L2PcInstance player, int arena, int team)
	{
		ArenaParticipantsHolder holder = _arenaPlayers[arena];
		
		synchronized (holder)
		{
			boolean isFromRed = holder.getRedPlayers().contains(player);
			
			if (isFromRed && (holder.getBlueTeamSize() == 6))
			{
				player.sendMessage("The team is full");
				return;
			}
			else if (!isFromRed && (holder.getRedTeamSize() == 6))
			{
				player.sendMessage("The team is full");
				return;
			}
			
			int futureTeam = isFromRed ? 1 : 0;
			holder.addPlayer(player, futureTeam);
			
			if (isFromRed)
			{
				holder.removePlayer(player, 0);
			}
			else
			{
				holder.removePlayer(player, 1);
			}
			holder.broadCastPacketToTeam(new ExCubeGameChangeTeam(player, isFromRed));
		}
	}
	
	public synchronized void clearPaticipantQueueByArenaId(int arenaId)
	{
		_arenaPlayers[arenaId].clearPlayers();
	}
	
	public boolean arenaIsBeingUsed(int arenaId)
	{
		if ((arenaId < 0) || (arenaId > 3))
		{
			return false;
		}
		return _arenaStatus.get(arenaId);
	}
	
	public void setArenaBeingUsed(int arenaId)
	{
		_arenaStatus.put(arenaId, true);
	}
	
	public void setArenaFree(int arenaId)
	{
		_arenaStatus.put(arenaId, false);
	}
	
	public void onDisconnect(L2PcInstance player)
	{
		int arena = player.getBlockCheckerArena();
		int team = getHolder(arena).getPlayerTeam(player);
		HandysBlockCheckerManager.getInstance().removePlayer(player, arena, team);
		if (player.getTeam() > 0)
		{
			player.stopAllEffects();
			player.setTeam(0);
			
			PcInventory inv = player.getInventory();
			
			if (inv.getItemByItemId(13787) != null)
			{
				long count = inv.getInventoryItemCount(13787, 0);
				inv.destroyItemByItemId("Handys Block Checker", 13787, count, player, player);
			}
			if (inv.getItemByItemId(13788) != null)
			{
				long count = inv.getInventoryItemCount(13788, 0);
				inv.destroyItemByItemId("Handys Block Checker", 13788, count, player, player);
			}
			player.setInsideZone(ZoneId.PVP, false);
			player.teleToLocation(-57478, -60367, -2370);
		}
	}
	
	public static HandysBlockCheckerManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static HandysBlockCheckerManager _instance = new HandysBlockCheckerManager();
	}
	
	public void removePenalty(int objectId)
	{
		_registrationPenalty.remove(objectId);
	}
	
	private void schedulePenaltyRemoval(int objId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new PenaltyRemoveTask(objId), 10000);
	}
}