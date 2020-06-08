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
package l2e.gameserver.model.entity;

import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2BlockInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ExBasicActionList;
import l2e.gameserver.network.serverpackets.ExCubeGameChangePoints;
import l2e.gameserver.network.serverpackets.ExCubeGameCloseUI;
import l2e.gameserver.network.serverpackets.ExCubeGameEnd;
import l2e.gameserver.network.serverpackets.ExCubeGameExtendedChangePoints;
import l2e.gameserver.network.serverpackets.RelationChanged;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Rnd;

public final class BlockCheckerEngine
{
	protected static final Logger _log = Logger.getLogger(BlockCheckerEngine.class.getName());
	
	protected ArenaParticipantsHolder _holder;
	protected FastMap<L2PcInstance, Integer> _redTeamPoints = new FastMap<>();
	protected FastMap<L2PcInstance, Integer> _blueTeamPoints = new FastMap<>();
	protected int _redPoints = 15;
	protected int _bluePoints = 15;
	protected int _arena = -1;
	protected FastList<L2Spawn> _spawns = new FastList<>();
	protected boolean _isRedWinner;
	protected long _startedTime;
	
	protected static final int[][] _arenaCoordinates =
	{
		{
			-58368,
			-62745,
			-57751,
			-62131,
			-58053,
			-62417
		},
		{
			-58350,
			-63853,
			-57756,
			-63266,
			-58053,
			-63551
		},
		{
			-57194,
			-63861,
			-56580,
			-63249,
			-56886,
			-63551
		},
		{
			-57200,
			-62727,
			-56584,
			-62115,
			-56850,
			-62391
		}
	};
	
	private static final int _zCoord = -2405;
	protected FastList<L2ItemInstance> _drops = new FastList<>();
	private static final byte DEFAULT_ARENA = -1;
	protected boolean _isStarted = false;
	protected ScheduledFuture<?> _task;
	protected boolean _abnormalEnd = false;
	
	public BlockCheckerEngine(ArenaParticipantsHolder holder, int arena)
	{
		_holder = holder;
		if ((arena > -1) && (arena < 4))
		{
			_arena = arena;
		}
		
		for (L2PcInstance player : holder.getRedPlayers())
		{
			_redTeamPoints.put(player, 0);
		}
		for (L2PcInstance player : holder.getBluePlayers())
		{
			_blueTeamPoints.put(player, 0);
		}
	}
	
	public void updatePlayersOnStart(ArenaParticipantsHolder holder)
	{
		_holder = holder;
	}
	
	public ArenaParticipantsHolder getHolder()
	{
		return _holder;
	}
	
	public int getArena()
	{
		return _arena;
	}
	
	public long getStarterTime()
	{
		return _startedTime;
	}
	
	public int getRedPoints()
	{
		synchronized (this)
		{
			return _redPoints;
		}
	}
	
	public int getBluePoints()
	{
		synchronized (this)
		{
			return _bluePoints;
		}
	}
	
	public int getPlayerPoints(L2PcInstance player, boolean isRed)
	{
		if (!_redTeamPoints.containsKey(player) && !_blueTeamPoints.containsKey(player))
		{
			return 0;
		}
		
		if (isRed)
		{
			return _redTeamPoints.get(player);
		}
		return _blueTeamPoints.get(player);
	}
	
	public synchronized void increasePlayerPoints(L2PcInstance player, int team)
	{
		if (player == null)
		{
			return;
		}
		
		if (team == 0)
		{
			int points = _redTeamPoints.get(player) + 1;
			_redTeamPoints.put(player, points);
			_redPoints++;
			_bluePoints--;
		}
		else
		{
			int points = _blueTeamPoints.get(player) + 1;
			_blueTeamPoints.put(player, points);
			_bluePoints++;
			_redPoints--;
		}
	}
	
	public void addNewDrop(L2ItemInstance item)
	{
		if (item != null)
		{
			_drops.add(item);
		}
	}
	
	public boolean isStarted()
	{
		return _isStarted;
	}
	
	protected void broadcastRelationChanged(L2PcInstance plr)
	{
		for (L2PcInstance p : _holder.getAllPlayers())
		{
			p.sendPacket(new RelationChanged(plr, plr.getRelation(p), plr.isAutoAttackable(p)));
		}
	}
	
	public void endEventAbnormally()
	{
		try
		{
			synchronized (this)
			{
				_isStarted = false;
				
				if (_task != null)
				{
					_task.cancel(true);
				}
				
				_abnormalEnd = true;
				
				ThreadPoolManager.getInstance().executeTask(new EndEvent());
				
				if (Config.DEBUG)
				{
					_log.config("Handys Block Checker Event at arena " + _arena + " ended due lack of players!");
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Couldnt end Block Checker event at " + _arena, e);
		}
	}
	
	public class StartEvent implements Runnable
	{
		private final L2Skill _freeze, _transformationRed, _transformationBlue;
		private final ExCubeGameCloseUI _closeUserInterface = new ExCubeGameCloseUI();
		
		public StartEvent()
		{
			_freeze = SkillHolder.getInstance().getInfo(6034, 1);
			_transformationRed = SkillHolder.getInstance().getInfo(6035, 1);
			_transformationBlue = SkillHolder.getInstance().getInfo(6036, 1);
		}
		
		private void setUpPlayers()
		{
			HandysBlockCheckerManager.getInstance().setArenaBeingUsed(_arena);
			
			_redPoints = _spawns.size() / 2;
			_bluePoints = _spawns.size() / 2;
			final ExCubeGameChangePoints initialPoints = new ExCubeGameChangePoints(300, _bluePoints, _redPoints);
			ExCubeGameExtendedChangePoints clientSetUp;
			
			for (L2PcInstance player : _holder.getAllPlayers())
			{
				if (player == null)
				{
					continue;
				}
				
				boolean isRed = _holder.getRedPlayers().contains(player);
				
				clientSetUp = new ExCubeGameExtendedChangePoints(300, _bluePoints, _redPoints, isRed, player, 0);
				player.sendPacket(clientSetUp);
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				int tc = _holder.getPlayerTeam(player) * 2;
				int x = _arenaCoordinates[_arena][tc];
				int y = _arenaCoordinates[_arena][tc + 1];
				player.teleToLocation(x, y, _zCoord);
				
				if (isRed)
				{
					_redTeamPoints.put(player, 0);
					player.setTeam(2);
				}
				else
				{
					_blueTeamPoints.put(player, 0);
					player.setTeam(1);
				}
				player.stopAllEffects();
				
				if (player.hasSummon())
				{
					player.getSummon().unSummon(player);
				}
				
				_freeze.getEffects(player, player);
				
				if (_holder.getPlayerTeam(player) == 0)
				{
					_transformationRed.getEffects(player, player);
				}
				else
				{
					_transformationBlue.getEffects(player, player);
				}
				
				player.setBlockCheckerArena((byte) _arena);
				player.setInsideZone(ZoneId.PVP, true);
				player.sendPacket(initialPoints);
				player.sendPacket(_closeUserInterface);
				player.sendPacket(ExBasicActionList.STATIC_PACKET);
				broadcastRelationChanged(player);
			}
		}
		
		@Override
		public void run()
		{
			if (_arena == -1)
			{
				_log.severe("Couldnt set up the arena Id for the Block Checker event, cancelling event...");
				return;
			}
			_isStarted = true;
			ThreadPoolManager.getInstance().executeTask(new SpawnRound(16, 1));
			setUpPlayers();
			_startedTime = System.currentTimeMillis() + 300000;
		}
	}
	
	private class SpawnRound implements Runnable
	{
		int _numOfBoxes;
		int _round;
		
		SpawnRound(int numberOfBoxes, int round)
		{
			_numOfBoxes = numberOfBoxes;
			_round = round;
		}
		
		@Override
		public void run()
		{
			if (!_isStarted)
			{
				return;
			}
			
			switch (_round)
			{
				case 1:
					_task = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRound(20, 2), 60000);
					break;
				case 2:
					_task = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRound(14, 3), 60000);
					break;
				case 3:
					_task = ThreadPoolManager.getInstance().scheduleGeneral(new EndEvent(), 180000);
					break;
			}
			
			byte random = 2;
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(18672);
			try
			{
				for (int i = 0; i < _numOfBoxes; i++)
				{
					L2Spawn spawn = new L2Spawn(template);
					spawn.setX(_arenaCoordinates[_arena][4] + Rnd.get(-400, 400));
					spawn.setY(_arenaCoordinates[_arena][5] + Rnd.get(-400, 400));
					spawn.setZ(_zCoord);
					spawn.setAmount(1);
					spawn.setHeading(1);
					spawn.setRespawnDelay(1);
					SpawnTable.getInstance().addNewSpawn(spawn, false);
					spawn.init();
					L2BlockInstance block = (L2BlockInstance) spawn.getLastSpawn();
					
					if ((random % 2) == 0)
					{
						block.setRed(true);
					}
					else
					{
						block.setRed(false);
					}
					
					block.disableCoreAI(true);
					_spawns.add(spawn);
					random++;
				}
			}
			catch (Exception e)
			{
				_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
			}
			
			if ((_round == 1) || (_round == 2))
			{
				L2NpcTemplate girl = NpcTable.getInstance().getTemplate(18676);
				try
				{
					final L2Spawn girlSpawn = new L2Spawn(girl);
					girlSpawn.setX(_arenaCoordinates[_arena][4] + Rnd.get(-400, 400));
					girlSpawn.setY(_arenaCoordinates[_arena][5] + Rnd.get(-400, 400));
					girlSpawn.setZ(_zCoord);
					girlSpawn.setAmount(1);
					girlSpawn.setHeading(1);
					girlSpawn.setRespawnDelay(1);
					SpawnTable.getInstance().addNewSpawn(girlSpawn, false);
					girlSpawn.init();
					ThreadPoolManager.getInstance().scheduleGeneral(new CarryingGirlUnspawn(girlSpawn), 9000);
				}
				catch (Exception e)
				{
					_log.warning("Couldnt Spawn Block Checker NPCs! Wrong instance type at npc table?");
					_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
			_redPoints += _numOfBoxes / 2;
			_bluePoints += _numOfBoxes / 2;
			
			int timeLeft = (int) ((getStarterTime() - System.currentTimeMillis()) / 1000);
			ExCubeGameChangePoints changePoints = new ExCubeGameChangePoints(timeLeft, getBluePoints(), getRedPoints());
			getHolder().broadCastPacketToTeam(changePoints);
		}
	}
	
	private class CarryingGirlUnspawn implements Runnable
	{
		private final L2Spawn _spawn;
		
		protected CarryingGirlUnspawn(L2Spawn spawn)
		{
			_spawn = spawn;
		}
		
		@Override
		public void run()
		{
			if (_spawn == null)
			{
				_log.warning("HBCE: Block Carrying Girl is null");
				return;
			}
			SpawnTable.getInstance().deleteSpawn(_spawn, false);
			_spawn.stopRespawn();
			_spawn.getLastSpawn().deleteMe();
		}
	}
	
	protected class EndEvent implements Runnable
	{
		private void clearMe()
		{
			HandysBlockCheckerManager.getInstance().clearPaticipantQueueByArenaId(_arena);
			_holder.clearPlayers();
			_blueTeamPoints.clear();
			_redTeamPoints.clear();
			HandysBlockCheckerManager.getInstance().setArenaFree(_arena);
			
			for (L2Spawn spawn : _spawns)
			{
				spawn.stopRespawn();
				spawn.getLastSpawn().deleteMe();
				SpawnTable.getInstance().deleteSpawn(spawn, false);
				spawn = null;
			}
			_spawns.clear();
			
			for (L2ItemInstance item : _drops)
			{
				if (item == null)
				{
					continue;
				}
				
				if (!item.isVisible() || (item.getOwnerId() != 0))
				{
					continue;
				}
				
				item.decayMe();
				L2World.getInstance().removeObject(item);
			}
			_drops.clear();
		}
		
		private void rewardPlayers()
		{
			if (_redPoints == _bluePoints)
			{
				return;
			}
			
			_isRedWinner = _redPoints > _bluePoints ? true : false;
			
			if (_isRedWinner)
			{
				rewardAsWinner(true);
				rewardAsLooser(false);
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TEAM_C1_WON);
				msg.addString("Red Team");
				_holder.broadCastPacketToTeam(msg);
			}
			else if (_bluePoints > _redPoints)
			{
				rewardAsWinner(false);
				rewardAsLooser(true);
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TEAM_C1_WON);
				msg.addString("Blue Team");
				_holder.broadCastPacketToTeam(msg);
			}
			else
			{
				rewardAsLooser(true);
				rewardAsLooser(false);
			}
		}
		
		private void rewardAsWinner(boolean isRed)
		{
			FastMap<L2PcInstance, Integer> tempPoints = isRed ? _redTeamPoints : _blueTeamPoints;
			
			for (Entry<L2PcInstance, Integer> points : tempPoints.entrySet())
			{
				if (points.getKey() == null)
				{
					continue;
				}
				
				if (points.getValue() >= 10)
				{
					points.getKey().addItem("Block Checker", 13067, 2, points.getKey(), true);
				}
				else
				{
					tempPoints.remove(points.getKey());
				}
			}
			
			int first = 0, second = 0;
			L2PcInstance winner1 = null, winner2 = null;
			for (Entry<L2PcInstance, Integer> entry : tempPoints.entrySet())
			{
				L2PcInstance pc = entry.getKey();
				int pcPoints = entry.getValue();
				if (pcPoints > first)
				{
					second = first;
					winner2 = winner1;
					first = pcPoints;
					winner1 = pc;
				}
				else if (pcPoints > second)
				{
					second = pcPoints;
					winner2 = pc;
				}
			}
			if (winner1 != null)
			{
				winner1.addItem("Block Checker", 13067, 8, winner1, true);
			}
			if (winner2 != null)
			{
				winner2.addItem("Block Checker", 13067, 5, winner2, true);
			}
		}
		
		private void rewardAsLooser(boolean isRed)
		{
			FastMap<L2PcInstance, Integer> tempPoints = isRed ? _redTeamPoints : _blueTeamPoints;
			
			for (Entry<L2PcInstance, Integer> entry : tempPoints.entrySet())
			{
				L2PcInstance player = entry.getKey();
				if ((player != null) && (entry.getValue() >= 10))
				{
					player.addItem("Block Checker", 13067, 2, player, true);
				}
			}
		}
		
		private void setPlayersBack()
		{
			final ExCubeGameEnd end = new ExCubeGameEnd(_isRedWinner);
			
			for (L2PcInstance player : _holder.getAllPlayers())
			{
				if (player == null)
				{
					continue;
				}
				
				player.stopAllEffects();
				player.setTeam(0);
				player.setBlockCheckerArena(DEFAULT_ARENA);
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
				broadcastRelationChanged(player);
				player.teleToLocation(-57478, -60367, -2370);
				player.setInsideZone(ZoneId.PVP, false);
				player.sendPacket(end);
				player.broadcastUserInfo();
			}
		}
		
		@Override
		public void run()
		{
			if (!_abnormalEnd)
			{
				rewardPlayers();
			}
			setPlayersBack();
			clearMe();
			_isStarted = false;
			_abnormalEnd = false;
		}
	}
}