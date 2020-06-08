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
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class Race extends Event
{
	private List<L2Npc> _npclist;
	private L2Npc _npc;
	private List<L2PcInstance> _players;

	ScheduledFuture<?> _eventTask = null;

	private static boolean _isactive = false;
	private static boolean _isRaceStarted = false;
	private static final int _time_register = Config.EVENT_REG_TIME_RACE;
	private static final int _time_race = Config.EVENT_RUNNING_TIME_RACE;

	private static final int _start_npc = 900103;
	private static final int _stop_npc = 900104;
	private static int _skill = 6201;
	private static int[] _randspawn = null;

	private static final String[] _locations =
	{
	                "Heretic catacomb enterance",
	                "Dion castle bridge",
	                "Floran village enterance",
	                "Floran fort gate"
	};

	private static final int[][] _coords =
	{
	                {
	                                39177,
	                                144345,
	                                -3650,
	                                0
	                },
	                {
	                                22294,
	                                155892,
	                                -2950,
	                                0
	                },
	                {
	                                16537,
	                                169937,
	                                -3500,
	                                0
	                },
	                {
	                                7644,
	                                150898,
	                                -2890,
	                                0
	                }
	};

	private static final int[][] _rewards =
	{
	                {
	                                6622,
	                                2
	                },
	                {
	                                9625,
	                                2
	                },
	                {
	                                9626,
	                                2
	                },
	                {
	                                9627,
	                                2
	                },
	                {
	                                9546,
	                                5
	                },
	                {
	                                9547,
	                                5
	                },
	                {
	                                9548,
	                                5
	                },
	                {
	                                9549,
	                                5
	                },
	                {
	                                9550,
	                                5
	                },
	                {
	                                9551,
	                                5
	                },
	                {
	                                9574,
	                                3
	                },
	                {
	                                9575,
	                                2
	                },
	                {
	                                9576,
	                                1
	                },
	                {
	                                20034,
	                                1
	                }
	};

	public Race(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_start_npc);
		addFirstTalkId(_start_npc);
		addTalkId(_start_npc);

		addStartNpc(_stop_npc);
		addFirstTalkId(_stop_npc);
		addTalkId(_stop_npc);
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
		_players = new FastList<>();
		_isactive = true;
		_npc = recordSpawn(_start_npc, 18429, 145861, -3090, 0, false, 0);

		CustomMessage msg1 = new CustomMessage("EventRace.START_MSG_1", true);
		Announcements.getInstance().announceToAll(msg1);

		CustomMessage msg2 = new CustomMessage("EventRace.START_MSG_2", true);
		msg2.add(_time_register);
		Announcements.getInstance().announceToAll(msg2);

		_eventTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				StartRace();
			}
		}, _time_register * 60 * 1000);

		return true;

	}

	protected void StartRace()
	{
		if (_players.isEmpty())
		{
			CustomMessage msg = new CustomMessage("EventRace.ABORTED", true);
			Announcements.getInstance().announceToAll(msg);
			eventStop();
			return;
		}
		_isRaceStarted = true;

		CustomMessage msg = new CustomMessage("EventRace.RACE_START", true);
		Announcements.getInstance().announceToAll(msg);

		int location = getRandom(0, _locations.length - 1);
		_randspawn = _coords[location];

		recordSpawn(_stop_npc, _randspawn[0], _randspawn[1], _randspawn[2], _randspawn[3], false, 0);
		for (L2PcInstance player : _players)
		{
			if ((player != null) && player.isOnline())
			{
				if (player.isInsideRadius(_npc, 500, false, false))
				{
					sendMessage(player, "Race started! Go find Finish NPC as fast as you can... He is located near " + _locations[location]);
					transformPlayer(player);
					player.getRadar().addMarker(_randspawn[0], _randspawn[1], _randspawn[2]);
				}
				else
				{
					sendMessage(player, "I told you stay near me right? Distance was too high, you are excluded from race");
					_players.remove(player);
				}
			}
		}
		_eventTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				timeUp();
			}
		}, _time_race * 60 * 1000);
	}

	@Override
	public boolean eventStop()
	{
		if (!_isactive)
		{
			return false;
		}
		_isactive = false;
		_isRaceStarted = false;

		if (_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}

		if (!_players.isEmpty())
		{
			for (L2PcInstance player : _players)
			{
				if ((player != null) && player.isOnline())
				{
					player.untransform();
					player.teleToLocation(_npc.getX(), _npc.getY(), _npc.getZ(), true);
				}
			}
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
		_players.clear();

		CustomMessage msg = new CustomMessage("EventRace.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		return true;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}

		if (event.equalsIgnoreCase("transform"))
		{
			transformPlayer(player);
			return null;
		}
		else if (event.equalsIgnoreCase("untransform"))
		{
			player.untransform();
			return null;
		}
		else if (event.equalsIgnoreCase("showfinish"))
		{
			player.getRadar().addMarker(_randspawn[0], _randspawn[1], _randspawn[2]);
			return null;
		}
		else if (event.equalsIgnoreCase("signup"))
		{
			if (_players.contains(player))
			{
				return "900103-onlist.htm";
			}
			_players.add(player);
			return "900103-signup.htm";
		}
		else if (event.equalsIgnoreCase("quit"))
		{
			player.untransform();
			if (_players.contains(player))
			{
				_players.remove(player);
			}
			return "900103-quit.htm";
		}
		else if (event.equalsIgnoreCase("finish"))
		{
			if (player.getFirstEffect(_skill) != null)
			{
				winRace(player);
				return "900104-winner.htm";
			}
			return "900104-notrans.htm";
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (npc.getId() == _start_npc)
		{
			if (_isRaceStarted)
			{
				return _start_npc + "-started-" + isRacing(player) + ".htm";
			}
			return _start_npc + "-" + isRacing(player) + ".htm";
		}
		else if ((npc.getId() == _stop_npc) && _isRaceStarted)
		{
			return _stop_npc + "-" + isRacing(player) + ".htm";
		}
		return npc.getId() + ".htm";
	}

	private int isRacing(L2PcInstance player)
	{
		if (_players.isEmpty())
		{
			return 0;
		}
		if (_players.contains(player))
		{
			return 1;
		}
		return 0;
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

	private void transformPlayer(L2PcInstance player)
	{
		if (player.isTransformed() || player.isInStance())
		{
			player.untransform();
		}
		if (player.isSitting())
		{
			player.standUp();
		}

		for (L2Effect e : player.getAllEffects())
		{
			if (e.getAbnormalType().equalsIgnoreCase("SPEED_UP"))
			{
				e.exit();
			}
			if ((e.getSkill() != null) && ((e.getSkill().getId() == 268) || (e.getSkill().getId() == 298)))
			{
				e.exit();
			}
		}
		SkillHolder.getInstance().getInfo(_skill, 1).getEffects(player, player);
	}

	private void sendMessage(L2PcInstance player, String text)
	{
		player.sendPacket(new CreatureSay(_npc.getObjectId(), 20, _npc.getName(), text));
	}

	protected void timeUp()
	{
		CustomMessage msg = new CustomMessage("EventRace.TIME_UP", true);
		Announcements.getInstance().announceToAll(msg);
		eventStop();
	}

	private void winRace(L2PcInstance player)
	{
		int[] _reward = _rewards[getRandom(_rewards.length - 1)];
		player.addItem("Race", _reward[0], _reward[1], _npc, true);
		CustomMessage msg = new CustomMessage("EventRace.WINNER", true);
		msg.add(player.getName());
		Announcements.getInstance().announceToAll(msg);
		eventStop();
	}

	public static void main(String[] args)
	{
		new Race(-1, "Race", "events");
	}
}
