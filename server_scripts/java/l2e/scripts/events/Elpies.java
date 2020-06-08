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
package l2e.scripts.events;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2EventMonsterInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Event;

/**
 * Updated by LordWinter 01.05.2012 Based on L2J Eternity-World
 */
public class Elpies extends Event
{
	private List<L2Npc> _npclist;
	ScheduledFuture<?> _eventTask = null;
	public static final int _event_time = Config.EVENT_TIME_ELPIES;
	private static boolean _isactive = false;
	
	private static final int _elpy = 900100;
	private static final int _option_howmuch = Config.EVENT_NUMBER_OF_SPAWNED_ELPIES;
	private static int _elpies_count = 0;
	
	private static final String[] _locations =
	{
		"Aden",
		"Gludin",
		"Hunters Village",
		"Dion",
		"Oren"
	};
	
	private static final int[][] _spawns =
	{
		{
			146558,
			148341,
			26622,
			28560,
			-2200
		},
		{
			-84040,
			-81420,
			150257,
			151175,
			-3125
		},
		{
			116094,
			117141,
			75776,
			77072,
			-2700
		},
		{
			18564,
			19200,
			144377,
			145782,
			-3081
		},
		{
			82048,
			82940,
			53240,
			54126,
			-1490
		}
	};
	
	private static final int[][] DROPLIST =
	{
		{
			1540,
			80,
			10,
			15
		},
		{
			1538,
			60,
			5,
			10
		},
		{
			3936,
			40,
			5,
			10
		},
		{
			6387,
			25,
			5,
			10
		},
		{
			22025,
			15,
			5,
			10
		},
		{
			6622,
			10,
			1,
			1
		},
		{
			20034,
			5,
			1,
			1
		},
		{
			20004,
			1,
			1,
			1
		},
		{
			20004,
			0,
			1,
			1
		}
	};
	
	private static final int[][] DROPLIST_CRYSTALS =
	{
		{
			1458,
			80,
			50,
			100
		},
		{
			1459,
			60,
			40,
			80
		},
		{
			1460,
			40,
			30,
			60
		},
		{
			1461,
			20,
			20,
			30
		},
		{
			1462,
			0,
			10,
			20
		},
	};
	
	public Elpies(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addSpawnId(_elpy);
		addKillId(_elpy);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		((L2EventMonsterInstance) npc).eventSetDropOnGround(true);
		((L2EventMonsterInstance) npc).eventSetBlockOffensiveSkills(true);
		
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (_isactive)
		{
			dropItem(npc, killer, DROPLIST);
			dropItem(npc, killer, DROPLIST_CRYSTALS);
			_elpies_count--;
			
			if (_elpies_count <= 0)
			{
				CustomMessage msg = new CustomMessage("EventElpies.NO_MORE", true);
				Announcements.getInstance().announceToAll(msg);
				eventStop();
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public boolean eventStart()
	{
		if (_isactive)
		{
			return false;
		}
		
		if (!Config.CUSTOM_NPC_TABLE)
		{
			_log.info(getName() + ": Event can't be started, because custom npc table is disabled!");
			return false;
		}
		
		_npclist = new FastList<>();
		_isactive = true;
		
		int location = getRandom(0, _locations.length - 1);
		int[] _spawndata = _spawns[location];
		
		_elpies_count = 0;
		
		for (int i = 0; i < _option_howmuch; i++)
		{
			int x = getRandom(_spawndata[0], _spawndata[1]);
			int y = getRandom(_spawndata[2], _spawndata[3]);
			recordSpawn(_elpy, x, y, _spawndata[4], 0, true, _event_time * 60 * 1000);
			_elpies_count++;
		}

		CustomMessage msg1 = new CustomMessage("EventElpies.START_MSG_1", true);
		msg1.add(_locations[location]);
		Announcements.getInstance().announceToAll(msg1);

		CustomMessage msg2 = new CustomMessage("EventElpies.START_MSG_2", true);
		Announcements.getInstance().announceToAll(msg2);

		CustomMessage msg3 = new CustomMessage("EventElpies.START_MSG_3", true);
		msg3.add(_event_time);
		Announcements.getInstance().announceToAll(msg3);
		
		_eventTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				timeUp();
			}
		}, _event_time * 60 * 1000);
		
		return true;
	}
	
	protected void timeUp()
	{
		CustomMessage msg = new CustomMessage("EventElpies.TIME_UP", true);
		Announcements.getInstance().announceToAll(msg);
		eventStop();
	}
	
	@Override
	public boolean eventStop()
	{
		if (!_isactive)
		{
			return false;
		}
		
		_isactive = false;
		
		if (_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}
		
		if (!_npclist.isEmpty())
		{
			for (L2Npc _npc : _npclist)
			{
				if (_npc != null)
				{
					_npc.deleteMe();
				}
			}
		}
		_npclist.clear();
		
		CustomMessage msg = new CustomMessage("EventElpies.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		return true;
	}
	
	private static final void dropItem(L2Npc mob, L2PcInstance player, int[][] droplist)
	{
		final int chance = getRandom(100);
		
		for (int[] drop : droplist)
		{
			if (chance > drop[1])
			{
				((L2MonsterInstance) mob).dropItem(player, drop[0], getRandom(drop[2], drop[3]));
				return;
			}
		}
	}
	
	private L2Npc recordSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffSet, long despawnDelay)
	{
		L2Npc _tmp = addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay);
		if (_tmp != null)
		{
			_npclist.add(_tmp);
		}
		return _tmp;
	}
	
	public static void main(String[] args)
	{
		new Elpies(-1, "Elpies", "events");
	}
}