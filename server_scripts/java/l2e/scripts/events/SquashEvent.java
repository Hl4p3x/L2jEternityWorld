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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.EventsDropManager;
import l2e.gameserver.instancemanager.EventsDropManager.ruleType;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2ChronoMonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.PlaySound;

/**
 * Rework by LordWinter 01.05.2012 Based on L2J Eternity-World
 */
public class SquashEvent extends Event
{
	private static final String event = "SquashEvent";

	private static final String UPDATE_STATUS = "SELECT status FROM events_custom_data WHERE event_name = ?";
	private static final String EVENT_INSERT = "REPLACE INTO events_custom_data (event_name, status) VALUES (?,?)";
	private static final String EVENT_DELETE = "UPDATE events_custom_data SET status = ? WHERE event_name = ?";

	private static boolean _isactive = false;
	private List<L2Npc> _npclist;
	protected L2Npc _npc;

	private static final int MANAGER = 31255;
	private static final int NECTAR_SKILL = 2005;

	private static final long DESPAWN_FIRST = 180000;
	private static final long DESPAWN_NEXT = 90000;

	private static final int DAMAGE_MAX = 12;
	private static final int DAMAGE_DEFAULT = 5;

	private static final int[] CHRONO_LIST =
	{
	                4202,
	                5133,
	                5817,
	                7058,
	                8350
	};

	private static final int[] SQUASH_LIST =
	{
	                12774,
	                12775,
	                12776,
	                12777,
	                12778,
	                12779,
	                13016,
	                13017
	};

	private static final String[] SPAWN_TEXT =
	{
	                "...Где это я?",
	                "Что Вы хотите сделать со мной?",
	                "Что происходит...",
	                "Для чего я здесь?",
	                "Вы очень поозрительный тип!"
	};

	private static final String[] GROWUP_TEXT =
	{
	                "Очень хорошо, дайте мне еще нектара.",
	                "Ммм... Весьма не плохо...",
	                "... Это еще не конец...",
	                "Вы отлично справляетесь с поставленной задачей.",
	                "Думаю, Вы спрособны на большее..."
	};

	private static final String[] KILL_TEXT =
	{
	                "Вы добились своего...",
	                ".....",
	                "Получите призи, которые заслужили.",
	                "Моя жизнь была так коротка...",
	                "Вы все-таки осилили меня."
	};

	private static final String[] NOCHRONO_TEXT =
	{
	                "Вы не можете убить меня без Сувенира",
	                "Ха-ха...продолжайте пробовать...",
	                "Хорошая попытка...",
	                "Устали?",
	                "Вперед вперед! ха-ха..."
	};

	private static final String[] CHRONO_TEXT =
	{
	                "Аааа... Сувенирное Оружие...",
	                "Мой конец близится...",
	                "Пожалуйста, оставьте меня!",
	                "Помогите...",
	                "Кто-нибудь помогите мне, пожалуйста..."
	};

	private static final String[] NECTAR_TEXT =
	{
	                "Вкусный... Нектар...",
	                "Пожалуйста дайте мне еще...",
	                "Хмм.. Больше.. Я хочу больше...",
	                "Вы мне больше будете нравится, если дадите мне больше...",
	                "Хммммм...",
	                "Мой любимый..."
	};

	private static final int[][] DROPLIST =
	{
	                {
	                                12774,
	                                100,
	                                6391,
	                                2
	                },
	                {
	                                12776,
	                                100,
	                                6391,
	                                10
	                },
	                {
	                                12775,
	                                100,
	                                6391,
	                                30
	                },
	                {
	                                13016,
	                                100,
	                                6391,
	                                50
	                },
	                {
	                                12777,
	                                100,
	                                14701,
	                                2,
	                                14700,
	                                2
	                },
	                {
	                                12779,
	                                50,
	                                729,
	                                4,
	                                730,
	                                4,
	                                6569,
	                                2,
	                                6570,
	                                2
	                },
	                {
	                                12779,
	                                30,
	                                6622,
	                                1
	                },
	                {
	                                12779,
	                                10,
	                                8750,
	                                1
	                },
	                {
	                                12779,
	                                10,
	                                8751,
	                                1
	                },
	                {
	                                12779,
	                                99,
	                                14701,
	                                4,
	                                14700,
	                                4
	                },
	                {
	                                12779,
	                                50,
	                                1461,
	                                4
	                },
	                {
	                                12779,
	                                30,
	                                1462,
	                                3
	                },
	                {
	                                12779,
	                                50,
	                                2133,
	                                4
	                },
	                {
	                                12779,
	                                30,
	                                2134,
	                                3
	                },
	                {
	                                12778,
	                                7,
	                                9570,
	                                1,
	                                9571,
	                                1,
	                                9572,
	                                1,
	                                10480,
	                                1,
	                                10481,
	                                1,
	                                10482,
	                                1,
	                                13071,
	                                1,
	                                13072,
	                                1,
	                                13073,
	                                1
	                },
	                {
	                                12778,
	                                35,
	                                729,
	                                4,
	                                730,
	                                4,
	                                959,
	                                3,
	                                960,
	                                3,
	                                6569,
	                                2,
	                                6570,
	                                2,
	                                6577,
	                                1,
	                                6578,
	                                1
	                },
	                {
	                                12778,
	                                28,
	                                6622,
	                                3,
	                                9625,
	                                2,
	                                9626,
	                                2,
	                                9627,
	                                2
	                },
	                {
	                                12778,
	                                14,
	                                8750,
	                                10
	                },
	                {
	                                12778,
	                                14,
	                                8751,
	                                8
	                },
	                {
	                                12778,
	                                14,
	                                8752,
	                                6
	                },
	                {
	                                12778,
	                                14,
	                                9575,
	                                4
	                },
	                {
	                                12778,
	                                14,
	                                10485,
	                                2
	                },
	                {
	                                12778,
	                                14,
	                                14168,
	                                1
	                },
	                {
	                                12778,
	                                21,
	                                8760,
	                                1,
	                                8761,
	                                1,
	                                8762,
	                                1,
	                                9576,
	                                1,
	                                10486,
	                                1,
	                                14169,
	                                1
	                },
	                {
	                                12778,
	                                21,
	                                14683,
	                                1,
	                                14684,
	                                1,
	                                14685,
	                                1,
	                                14686,
	                                1,
	                                14687,
	                                1,
	                                14689,
	                                1,
	                                14690,
	                                1,
	                                14691,
	                                1,
	                                14692,
	                                1,
	                                14693,
	                                1,
	                                14695,
	                                1,
	                                14696,
	                                1,
	                                14697,
	                                1,
	                                14698,
	                                1,
	                                14699,
	                                1
	                },
	                {
	                                12778,
	                                99,
	                                14701,
	                                9,
	                                14700,
	                                9
	                },
	                {
	                                12778,
	                                63,
	                                1461,
	                                8
	                },
	                {
	                                12778,
	                                49,
	                                1462,
	                                5
	                },
	                {
	                                12778,
	                                63,
	                                2133,
	                                6
	                },
	                {
	                                12778,
	                                49,
	                                2134,
	                                4
	                },
	                {
	                                13017,
	                                10,
	                                9570,
	                                1,
	                                9571,
	                                1,
	                                9572,
	                                1,
	                                10480,
	                                1,
	                                10481,
	                                1,
	                                10482,
	                                1,
	                                13071,
	                                1,
	                                13072,
	                                1,
	                                13073,
	                                1
	                },
	                {
	                                13017,
	                                50,
	                                729,
	                                4,
	                                730,
	                                4,
	                                959,
	                                3,
	                                960,
	                                3,
	                                6569,
	                                2,
	                                6570,
	                                2,
	                                6577,
	                                1,
	                                6578,
	                                1
	                },
	                {
	                                13017,
	                                40,
	                                6622,
	                                3,
	                                9625,
	                                2,
	                                9626,
	                                2,
	                                9627,
	                                2
	                },
	                {
	                                13017,
	                                20,
	                                8750,
	                                10
	                },
	                {
	                                13017,
	                                20,
	                                8751,
	                                8
	                },
	                {
	                                13017,
	                                20,
	                                8752,
	                                6
	                },
	                {
	                                13017,
	                                20,
	                                9575,
	                                4
	                },
	                {
	                                13017,
	                                20,
	                                10485,
	                                2
	                },
	                {
	                                13017,
	                                20,
	                                14168,
	                                1
	                },
	                {
	                                13017,
	                                30,
	                                8760,
	                                1,
	                                8761,
	                                1,
	                                8762,
	                                1,
	                                9576,
	                                1,
	                                10486,
	                                1,
	                                14169,
	                                1
	                },
	                {
	                                13017,
	                                30,
	                                14683,
	                                1,
	                                14684,
	                                1,
	                                14685,
	                                1,
	                                14686,
	                                1,
	                                14687,
	                                1,
	                                14689,
	                                1,
	                                14690,
	                                1,
	                                14691,
	                                1,
	                                14692,
	                                1,
	                                14693,
	                                1,
	                                14695,
	                                1,
	                                14696,
	                                1,
	                                14697,
	                                1,
	                                14698,
	                                1,
	                                14699,
	                                1
	                },
	                {
	                                13017,
	                                99,
	                                14701,
	                                12,
	                                14700,
	                                12
	                },
	                {
	                                13017,
	                                90,
	                                1461,
	                                8
	                },
	                {
	                                13017,
	                                70,
	                                1462,
	                                5
	                },
	                {
	                                13017,
	                                90,
	                                2133,
	                                6
	                },
	                {
	                                13017,
	                                70,
	                                2134,
	                                4
	                },
	};

	private static final Location[] _coords =
	{
	                new Location(-84267, 243246, -3729, 0),
	                new Location(-44813, -113364, -202, 0),
	                new Location(45235, 49396, 3068, 0),
	                new Location(114702, -178369, -820, 0),
	                new Location(10925, 15968, -4574, 0),
	                new Location(-117156, 46897, 367, 0),
	                new Location(83075, 148394, -3472, 0),
	                new Location(148059, -56554, -2781, 0),
	                new Location(19000, 145783, -3081, 0),
	                new Location(-14052, 123194, -3117, 0),
	                new Location(86979, -142163, -1343, 0),
	                new Location(116468, 75321, -2712, 0),
	                new Location(147146, 26536, -2205, 0),
	                new Location(43488, -48421, -797, 0),
	                new Location(81963, 53937, -1496, 0),
	                new Location(-81634, 150195, -3132, 0),
	                new Location(111624, 219228, -3543, 0),
	                new Location(17559, 170318, -3506, 0)
	};

	private static void addDrop()
	{
		int item[] =
		{
			        6391
		};
		int cnt[] =
		{
			        Config.NECTAR_COUNT
		};
		int chance[] =
		{
			        Config.NECTAR_CHANCE
		};
		EventsDropManager.getInstance().addSquashRule(event, ruleType.ALL_NPC, item, cnt, chance);
	}

	private static void removeDrop()
	{
		EventsDropManager.getInstance().removeSquashRules(event);
	}

	private int _numAtk = 0;
	private int w_nectar = 0;

	class TheInstance
	{
		int nectar;
		long despawnTime;
	}

	FastMap<L2ChronoMonsterInstance, TheInstance> _monsterInstances = new FastMap<L2ChronoMonsterInstance, TheInstance>().shared();

	private TheInstance create(L2ChronoMonsterInstance mob)
	{
		TheInstance mons = new TheInstance();
		_monsterInstances.put(mob, mons);
		return mons;
	}

	private TheInstance get(L2ChronoMonsterInstance mob)
	{
		return _monsterInstances.get(mob);
	}

	private void remove(L2ChronoMonsterInstance mob)
	{
		cancelQuestTimer("countdown", mob, null);
		cancelQuestTimer("despawn", mob, null);
		_monsterInstances.remove(mob);
	}

	public SquashEvent(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int mob : SQUASH_LIST)
		{
			addAttackId(mob);
			addKillId(mob);
			addSpawnId(mob);
			addSkillSeeId(mob);
		}

		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
		addTalkId(MANAGER);

		restoreStatus();
	}

	@Override
	public boolean eventStart()
	{
		if (_isactive)
		{
			return false;
		}
		_isactive = true;
		_npclist = new FastList<>();

		for (Location loc : _coords)
		{
			recordSpawn(MANAGER, loc, false, 0);
		}
		addDrop();

		CustomMessage msg = new CustomMessage("EventSquashes.START", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(true);

		return true;
	}

	@Override
	public boolean eventStop()
	{
		if (!_isactive)
		{
			return false;
		}
		_isactive = false;

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
		removeDrop();

		CustomMessage msg = new CustomMessage("EventSquashes.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		updateStatus(false);

		return true;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event == "countdown")
		{
			final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance) npc;
			final TheInstance self = get(mob);
			int timeLeft = (int) ((self.despawnTime - System.currentTimeMillis()) / 1000);
			if (timeLeft == 30)
			{
				autoChat(mob, "У Вас остальсь 30сек. на убийство.");
			}
			else if (timeLeft == 20)
			{
				autoChat(mob, "У Вас остальсь 20сек. на убийство.");
			}
			else if (timeLeft == 10)
			{
				autoChat(mob, "Время на исходе... 9 ... 8 ... 7 ...");
			}
			else if (timeLeft == 0)
			{
				if (self.nectar == 0)
				{
					autoChat(mob, "Мне нужно больше нектара, чтоьы выжить.");
				}
				else
				{
					autoChat(mob, "Нектар...");
				}
			}
			else if ((timeLeft % 60) == 0)
			{
				if (self.nectar == 0)
				{
					autoChat(mob, "Я исчезну через" + (timeLeft / 60) + "время на исходе.");
				}
			}
		}
		else if (event == "despawn")
		{
			remove((L2ChronoMonsterInstance) npc);
			npc.deleteMe();
		}
		else if (event == "sound")
		{
			final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance) npc;
			mob.broadcastPacket(new PlaySound(0, "ItemSound3.sys_sow_success", 0, 0, 0, 0, 0));
		}
		else
		{
			return super.onAdvEvent(event, npc, player);
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		switch (npc.getId())
		{
			case MANAGER:
				return "31255.htm";
		}
		throw new RuntimeException();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance) npc;
		L2Weapon weapon;
		final boolean isChronoAttack = !isSummon && ((weapon = attacker.getActiveWeaponItem()) != null) && contains(CHRONO_LIST, weapon.getId());
		switch (mob.getId())
		{
			case 12774:
			case 12775:
			case 12776:
			case 13016:
				if (isChronoAttack)
				{
					chronoText(mob);
				}
				else
				{
					noChronoText(mob);
				}
				break;
			case 12777:
			case 12778:
			case 12779:
			case 13017:
				if (isChronoAttack)
				{
					mob.setIsInvul(false);
					if (damage == 0)
					{
						mob.getStatus().reduceHp(DAMAGE_DEFAULT, attacker);
					}
					else if (damage > DAMAGE_MAX)
					{
						mob.getStatus().setCurrentHp((mob.getStatus().getCurrentHp() + damage) - DAMAGE_MAX);
					}
					chronoText(mob);
				}
				else
				{
					mob.setIsInvul(true);
					mob.setCurrentHp(mob.getMaxHp());
					noChronoText(mob);
				}
				break;
			default:
				throw new RuntimeException();
		}
		mob.getStatus().stopHpMpRegeneration();
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if ((skill.getId() == NECTAR_SKILL) && (targets[0] == npc))
		{
			final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance) npc;
			switch (mob.getId())
			{
				case 12774:
					if ((w_nectar == 0) || (w_nectar == 1) || (w_nectar == 2) || (w_nectar == 3) || (w_nectar == 4))
					{
						if (getRandom(100) < 50)
						{
							nectarText(mob);
							npc.doCast(SkillHolder.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							npc.doCast(SkillHolder.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(12775, 12775, 13016, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else
						{
							randomSpawn(12776, 12776, 12776, mob);
							_numAtk = 0;
						}
					}
					break;
				case 12777:
					if ((w_nectar == 0) || (w_nectar == 1) || (w_nectar == 2) || (w_nectar == 3) || (w_nectar == 4))
					{
						if (getRandom(100) < 50)
						{
							nectarText(mob);
							npc.doCast(SkillHolder.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							npc.doCast(SkillHolder.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(12778, 12778, 13017, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else
						{
							randomSpawn(12779, 12779, 12779, mob);
							_numAtk = 0;
						}
					}
					break;
				case 12775:
					npc.doCast(SkillHolder.getInstance().getInfo(4513, 1));
					randomSpawn(13016, mob);
					break;
				case 12778:
					npc.doCast(SkillHolder.getInstance().getInfo(4513, 1));
					randomSpawn(13017, mob);
					break;
				case 12776:
				case 12779:
					autoChat(mob, "Я хочу Нектара!");
					break;
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance) npc;
		remove(mob);
		autoChat(mob, KILL_TEXT[getRandom(KILL_TEXT.length)]);
		dropItem(mob, killer);
		w_nectar = 0;
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		assert npc instanceof L2ChronoMonsterInstance;

		final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance) npc;
		mob.setOnKillDelay(1500);
		final TheInstance self = create(mob);
		switch (mob.getId())
		{
			case 12774:
			case 12777:
				startQuestTimer("countdown", 10000, mob, null, true);
				startQuestTimer("despawn", DESPAWN_FIRST, mob, null);
				self.nectar = 0;
				self.despawnTime = System.currentTimeMillis() + DESPAWN_FIRST;
				autoChat(mob, SPAWN_TEXT[getRandom(SPAWN_TEXT.length)]);
				break;
			case 12775:
			case 12776:
			case 12778:
			case 12779:
			case 13016:
			case 13017:
				startQuestTimer("countdown", 10000, mob, null, true);
				startQuestTimer("despawn", DESPAWN_NEXT, mob, null);
				startQuestTimer("sound", 100, mob, null);
				self.nectar = 5;
				self.despawnTime = System.currentTimeMillis() + DESPAWN_NEXT;
				autoChat(mob, GROWUP_TEXT[getRandom(GROWUP_TEXT.length)]);
				break;
			default:
				throw new RuntimeException();
		}
		return null;
	}

	static
	{
		Arrays.sort(DROPLIST, new Comparator<int[]>()
		{
			@Override
			public int compare(int[] a, int[] b)
			{
				return a[0] - b[0];
			}
		});
	}

	private static final void dropItem(L2ChronoMonsterInstance mob, L2PcInstance player)
	{
		final int npcId = mob.getId();
		for (int[] drop : DROPLIST)
		{
			if (npcId == drop[0])
			{
				final int chance = getRandom(100);
				if (chance < drop[1])
				{
					int i = 2 + (2 * getRandom((drop.length - 2) / 2));
					int itemId = drop[i + 0];
					int itemQty = drop[i + 1];
					if (itemQty > 1)
					{
						itemQty = getRandom(1, itemQty);
					}
					mob.dropItem(mob.getOwner(), itemId, itemQty);
					continue;
				}
			}

			if (npcId < drop[0])
			{
				return;
			}
		}
	}

	private void randomSpawn(int bad, int good, int king, L2ChronoMonsterInstance mob)
	{
		if (w_nectar >= 5)
		{
			w_nectar = 0;
			int _random = getRandom(100);
			if ((_random -= 10) < 0)
			{
				spawnNext(king, mob);
			}
			else if ((_random -= 40) < 0)
			{
				spawnNext(good, mob);
			}
			else
			{
				spawnNext(bad, mob);
			}
		}
		else
		{
			nectarText(mob);
		}
	}

	private void randomSpawn(int king, L2ChronoMonsterInstance mob)
	{
		final TheInstance self = get(mob);
		if ((++self.nectar > 5) && (self.nectar <= 15) && (getRandom(100) < 10))
		{
			spawnNext(king, mob);
		}
		else
		{
			nectarText(mob);
		}
	}

	private void autoChat(L2ChronoMonsterInstance mob, String text)
	{
		mob.broadcastPacket(new CreatureSay(mob.getObjectId(), Say2.ALL, mob.getName(), text));
	}

	private void chronoText(L2ChronoMonsterInstance mob)
	{
		if (getRandom(100) < 20)
		{
			autoChat(mob, CHRONO_TEXT[getRandom(CHRONO_TEXT.length)]);
		}
	}

	private void noChronoText(L2ChronoMonsterInstance mob)
	{
		if (getRandom(100) < 20)
		{
			autoChat(mob, NOCHRONO_TEXT[getRandom(NOCHRONO_TEXT.length)]);
		}
	}

	private void nectarText(L2ChronoMonsterInstance mob)
	{
		autoChat(mob, NECTAR_TEXT[getRandom(NECTAR_TEXT.length)]);
	}

	private void spawnNext(int npcId, L2ChronoMonsterInstance oldMob)
	{
		remove(oldMob);
		L2ChronoMonsterInstance newMob = (L2ChronoMonsterInstance) addSpawn(npcId, oldMob.getX(), oldMob.getY(), oldMob.getZ(), oldMob.getHeading(), false, 0);
		newMob.setOwner(oldMob.getOwner());
		newMob.setTitle(oldMob.getTitle());
		oldMob.deleteMe();
	}

	public static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		return event;
	}

	private L2Npc recordSpawn(int npcId, Location loc, boolean randomOffSet, long despawnDelay)
	{
		L2Npc _tmp = addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffSet, despawnDelay);
		if (_tmp != null)
		{
			_npclist.add(_tmp);
		}
		return _tmp;
	}

	private void restoreStatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int status = 0;
			String event_name = event;
			PreparedStatement statement = con.prepareStatement(UPDATE_STATUS);
			statement.setString(1, event_name);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				status = rset.getInt("status");
			}
			rset.close();
			statement.close();
			if (status > 0)
			{
				eventStart();
			}
			else
			{
				eventStop();
			}
		}
		catch (Exception e)
		{
			_log.warning("Error: Could not restore custom event data info: " + e);
		}
	}

	private void updateStatus(boolean newEvent)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			String event_name = event;
			boolean insert = newEvent;

			PreparedStatement stmt = con.prepareStatement(insert ? EVENT_INSERT : EVENT_DELETE);

			if (newEvent)
			{
				stmt.setString(1, event_name);
				stmt.setInt(2, 1);
				stmt.execute();
				stmt.close();
			}
			else
			{
				stmt.setInt(1, newEvent ? 0 : 0);
				stmt.setString(2, event_name);
				stmt.execute();
				stmt.close();
			}
		}
		catch (Exception e)
		{
			_log.warning("Error: could not update custom event database!");
		}
	}

	public static void main(String[] args)
	{
		new SquashEvent(-1, "SquashEvent", "events");
	}
}
