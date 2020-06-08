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
package l2e.gameserver.model.entity.events;

import java.util.logging.Logger;

import javolution.util.FastSet;
import l2e.Config;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance.ConfirmDialogScripts;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class MonsterRush
{
	protected static final Logger _log = Logger.getLogger(MonsterRush.class.getName());
	
	enum Status
	{
		INACTIVE,
		STARTED,
		REGISTER,
		TELEPORT,
		REWARDING
	}
	
	public static FastSet<L2PcInstance> _participants = new FastSet<>();
	public static FastSet<L2Npc> _monsters = new FastSet<>();
	public static Status _status = Status.INACTIVE;
	public static int[] _miniIntervals =
	{
		80000,
		160000,
		240000,
		360000
	};
	
	public static int[] _CoordsX =
	{
		17918,
		19769,
		19157,
		17900
	};
	public static int[] _CoordsY =
	{
		146271,
		145728,
		143732,
		145674
	};
	public static int[] _CoordsZ =
	{
		-3110,
		-3120,
		-3066,
		-3103
	};
	
	public static int[][] _wave1Mons =
	{
		{
			50000,
			50001
		},
		{
			1,
			10
		}
	};
	public static int[][] _wave2Mons =
	{
		{
			50002,
			50003,
			50004
		},
		{
			10,
			10,
			10
		}
	};
	public static int[][] _wave3Mons =
	{
		{
			50005,
			50006,
			50007,
			50008
		},
		{
			5,
			5,
			5,
			1
		}
	};
	
	protected static L2Npc _lord = null;
	public static int _wave = 1;
	public static int X = 18864;
	public static int Y = 145216;
	public static int Z = -3132;
	
	public static int getParticipatingPlayers()
	{
		return _participants.size();
	}
	
	protected static void monWave(final int _waveNum)
	{
		if (_status == Status.INACTIVE)
		{
			return;
		}
		
		if (_waveNum == 2)
		{
			CustomMessage msg = new CustomMessage("MonsterRush.FIRST_WAVE", true);
			AnnounceToPlayers(true, msg);
		}
		else if (_waveNum == 3)
		{
			CustomMessage msg = new CustomMessage("MonsterRush.SECOND_WAVE", true);
			AnnounceToPlayers(true, msg);
		}
		
		L2Npc mobas = null;
		int[][] wave = _wave1Mons;
		if (_waveNum == 2)
		{
			wave = _wave2Mons;
		}
		else if (_waveNum == 3)
		{
			wave = _wave3Mons;
		}
		
		for (int i = 0; i <= (wave[0].length - 1); i++)
		{
			for (int a = 1; a <= wave[1][i]; a++)
			{
				for (int r = 0; r <= (_CoordsX.length - 1); r++)
				{
					mobas = addSpawn(wave[0][i], _CoordsX[r], _CoordsY[r], _CoordsZ[r]);
					mobas.getKnownList().addKnownObject(_lord);
					_monsters.add(mobas);
				}
			}
		}
		for (L2Npc monster : _monsters)
		{
			((L2Attackable) monster).addDamageHate(_lord, 9000, 9000);
			monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _lord, null);
		}
		
		for (int i = 0; i <= (_miniIntervals.length - 1); i++)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					miniWave(_waveNum);
				}
			}, _miniIntervals[i]);
		}
		
	}
	
	public static void abortEvent()
	{
		_participants.clear();
		_status = Status.INACTIVE;
		CustomMessage msg = new CustomMessage("MonsterRush.EVENT_ABORTED", true);
		AnnounceToPlayers(true, msg);
	}
	
	protected static boolean checkPlayersCount()
	{
		if ((_participants == null) || _participants.isEmpty() || (_participants.size() < Config.MRUSH_MIN_PLAYERS))
		{
			CustomMessage msg = new CustomMessage("MonsterRush.NOT_ENOUGH_PLAYERS", true);
			msg.add(Config.MRUSH_MIN_PLAYERS);
			msg.add(_participants.size());
			AnnounceToPlayers(true, msg);
			return false;
		}
		return true;
	}
	
	public static void endByLordDeath()
	{
		endAndReward();
	}
	
	protected static void endAndReward()
	{
		if (_status == Status.INACTIVE)
		{
			return;
		}
		
		_status = Status.REWARDING;
		for (L2Npc monster : _monsters)
		{
			monster.onDecay();
		}
		_monsters.clear();
		if (L2World.getInstance().findObject(_lord.getObjectId()) == null)
		{
			CustomMessage msg = new CustomMessage("MonsterRush.NOT_PROTECTED", true);
			AnnounceToPlayers(true, msg);
			CustomMessage msg2 = new CustomMessage("MonsterRush.TELE_BACK", true);
			AnnounceToPlayers(true, msg2);
			CustomMessage msg3 = new CustomMessage("MonsterRush.EVENT_ENDED", true);
			AnnounceToPlayers(true, msg3);
			synchronized (_participants)
			{
				for (L2PcInstance player : _participants)
				{
					player.teleToLocation(X, Y, Z, true);
					player.setTeam(0);
					player.setIsInMonsterRush(false);
				}
			}
		}
		else
		{
			CustomMessage msg = new CustomMessage("MonsterRush.WAS_PROTECTED", true);
			AnnounceToPlayers(true, msg);
			CustomMessage msg2 = new CustomMessage("MonsterRush.TELE_BACK", true);
			AnnounceToPlayers(true, msg2);
			CustomMessage msg3 = new CustomMessage("MonsterRush.EVENT_ENDED", true);
			AnnounceToPlayers(true, msg3);
			_lord.deleteMe();
			synchronized (_participants)
			{
				for (L2PcInstance player : _participants)
				{
					player.sendMessage((new CustomMessage("MonsterRush.THANKS", player.getLang())).toString());
					player.sendMessage((new CustomMessage("MonsterRush.COURAGE", player.getLang())).toString());
					
					player.getInventory().addItem("MonsterRush Event", Config.MRUSH_REWARD_ITEM, Config.MRUSH_REWARD_AMOUNT, player, null);
					player.getInventory().updateDatabase();
					player.teleToLocation(X, Y, Z, true);
					player.setTeam(0);
					player.setIsInMonsterRush(false);
				}
			}
		}
		_participants.clear();
		_status = Status.INACTIVE;
	}
	
	protected static void miniWave(int _waveNum)
	{
		if (_status == Status.INACTIVE)
		{
			return;
		}
		
		int[][] wave = _wave1Mons;
		if (_waveNum == 2)
		{
			wave = _wave2Mons;
		}
		else if (_waveNum == 3)
		{
			wave = _wave3Mons;
		}
		
		L2Npc mobas = null;
		for (int i = 0; i <= (wave[0].length - 1); i++)
		{
			for (int a = 1; a <= Math.round(wave[1][i] * 0.65); a++)
			{
				for (int r = 0; r <= (_CoordsX.length - 1); r++)
				{
					mobas = addSpawn(wave[0][i], _CoordsX[r], _CoordsY[r], _CoordsZ[r]);
					_monsters.add(mobas);
				}
			}
		}
		
		for (L2Npc monster : _monsters)
		{
			((L2Attackable) monster).addDamageHate(_lord, 7000, 7000);
			monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _lord, null);
		}
	}
	
	public static L2Npc addSpawn(int npcId, int x, int y, int z)
	{
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(0);
				spawn.setHeading(1);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z);
				if (Config.GEODATA)
				{
					spawn.setZ(GeoClient.getInstance().getSpawnHeight(x, y, z));
				}
				else
				{
					spawn.setZ(z);
				}
				spawn.stopRespawn();
				result = spawn.spawnOne(true);
				
				return result;
			}
		}
		catch (Exception e1)
		{
		}
		return null;
	}
	
	public static void doUnReg(L2PcInstance player)
	{
		if (_status == Status.REGISTER)
		{
			if (_participants.contains(player))
			{
				_participants.remove(player);
				player.sendMessage((new CustomMessage("MonsterRush.UNREG", player.getLang())).toString());
			}
			else
			{
				player.sendMessage((new CustomMessage("MonsterRush.REGISTER", player.getLang())).toString());
			}
		}
		else
		{
			player.sendMessage((new CustomMessage("MonsterRush.INACTIVE", player.getLang())).toString());
		}
	}
	
	public static void doReg(L2PcInstance player)
	{
		if (_status == Status.REGISTER)
		{
			if (!_participants.contains(player))
			{
				_participants.add(player);
				player.sendMessage((new CustomMessage("MonsterRush.REG", player.getLang())).toString());
			}
			else
			{
				player.sendMessage((new CustomMessage("MonsterRush.ALREADY_REG", player.getLang())).toString());
			}
		}
		else
		{
			player.sendMessage((new CustomMessage("MonsterRush.CANNOT_REG", player.getLang())).toString());
		}
	}
	
	public static void OnConfirmDlgAnswer(L2PcInstance player, int answer)
	{
		if ((player != null) && player.isOnline())
		{
			if (answer == 1)
			{
				if (_status == Status.REGISTER)
				{
					if (!player.isInOlympiadMode())
					{
						if (!_participants.contains(player))
						{
							_participants.add(player);
							player.sendMessage((new CustomMessage("MonsterRush.REG", player.getLang())).toString());
						}
						else
						{
							player.sendMessage((new CustomMessage("MonsterRush.ALREADY_REG", player.getLang())).toString());
						}
					}
					else
					{
						player.sendMessage((new CustomMessage("MonsterRush.OLIMPIAD", player.getLang())).toString());
					}
				}
				else
				{
					player.sendMessage((new CustomMessage("MonsterRush.CANNOT_REG", player.getLang())).toString());
				}
			}
		}
	}
	
	public static void startRegister()
	{
		_status = Status.REGISTER;
		_participants.clear();
		
		CustomMessage msg = new CustomMessage("MonsterRush.REGISTRATION", true);
		AnnounceToPlayers(true, msg);
		CustomMessage msg2 = new CustomMessage("MonsterRush.REG_COMMAND", true);
		AnnounceToPlayers(true, msg2);
		
		for (L2PcInstance pl : L2World.getInstance().getAllPlayersArray())
		{
			if ((pl != null) && pl.isOnline())
			{
				CustomMessage msg3 = new CustomMessage("MonsterRush.WANT_TO_REG", pl.getLang());
				pl.CurrentConfirmDialog = ConfirmDialogScripts.MonsterRush;
				ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1.getId());
				confirm.addString(msg3.toString());
				confirm.addTime(30000);
				pl.sendPacket(confirm);
			}
		}
	}
	
	public static void startEvent()
	{
		_status = Status.STARTED;
		CustomMessage msg = new CustomMessage("MonsterRush.REG_CLOSE", true);
		AnnounceToPlayers(true, msg);
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				beginTeleport();
			}
		}, 20000);
	}
	
	public static boolean isInactive()
	{
		boolean isInactive;
		
		synchronized (_status)
		{
			isInactive = _status == Status.INACTIVE;
		}
		
		return isInactive;
	}
	
	public static boolean isParticipating()
	{
		boolean isParticipating;
		
		synchronized (_status)
		{
			isParticipating = _status == Status.REGISTER;
		}
		return isParticipating;
	}
	
	public static boolean isStarted()
	{
		boolean isStarted;
		
		synchronized (_status)
		{
			isStarted = _status == Status.STARTED;
		}
		return isStarted;
	}
	
	public static void sysMsgToAllParticipants(CustomMessage message)
	{
		for (final L2PcInstance player : _participants)
		{
			if (player != null)
			{
				player.sendMessage(message.toString(player.getLang()));
			}
		}
	}
	
	protected static void beginTeleport()
	{
		_status = Status.TELEPORT;
		_lord = addSpawn(40030, X, Y, Z);
		_lord.setIsParalyzed(true);
		synchronized (_participants)
		{
			for (L2PcInstance player : _participants)
			{
				if (player.isInOlympiadMode() || TvTEvent.isPlayerParticipant(player.getObjectId()))
				{
					_participants.remove(player);
					return;
				}
				player.teleToLocation(X, Y, Z, true);
				player.setTeam(2);
				player.setIsInMonsterRush(true);
			}
			CustomMessage msg = new CustomMessage("MonsterRush.TELE_DONE", true);
			AnnounceToPlayers(true, msg);
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					monWave(1);
				}
			}, 180000);
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					monWave(2);
				}
			}, 360000);
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					monWave(3);
				}
			}, 720000);
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					endAndReward();
				}
			}, 1220000);
		}
	}
	
	public static void AnnounceToPlayers(Boolean toall, CustomMessage msg)
	{
		if (toall)
		{
			Announcements.getInstance().announceToAll(msg);
		}
		else
		{
			if ((_participants != null) && !_participants.isEmpty())
			{
				for (L2PcInstance player : _participants)
				{
					CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", msg.toString(player.getLang()));
					if (player.isOnline())
					{
						player.sendPacket(cs);
					}
				}
			}
		}
	}
}