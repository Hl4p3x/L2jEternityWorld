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
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2EventChestInstance;
import l2e.gameserver.model.actor.instance.L2EventMonsterInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.util.Util;

public class Rabbits extends Event
{
	private List<L2Npc> _npclist;
	ScheduledFuture<?> _eventTask = null;
	public static final int _event_time = Config.EVENT_TIME_RABBITS;
	private static boolean _isactive = false;
	private static int _chest_count = 0;
	private static final int _option_howmuch = Config.EVENT_NUMBER_OF_SPAWNED_CHESTS;

	public static final int _npc_snow = 900101;
	public static final int _npc_chest = 900102;
	public static final int _skill_tornado = 630;
	public static final int _skill_magic_eye = 629;

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

	public Rabbits(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_npc_snow);
		addFirstTalkId(_npc_snow);
		addTalkId(_npc_snow);

		addFirstTalkId(_npc_chest);
		addSkillSeeId(_npc_chest);
		addSpawnId(_npc_chest);
		addAttackId(_npc_chest);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		((L2EventMonsterInstance) npc).eventSetDropOnGround(true);
		((L2EventMonsterInstance) npc).eventSetBlockOffensiveSkills(true);

		npc.setIsImmobilized(true);
		npc.disableCoreAI(true);

		return super.onSpawn(npc);
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

		recordSpawn(_npc_snow, -59227, -56939, -2039, 64106, false, 0);

		for (int i = 0; i < _option_howmuch; i++)
		{
			int x = getRandom(-60653, -58772);
			int y = getRandom(-55830, -57718);
			recordSpawn(_npc_chest, x, y, -2030, 0, true, _event_time * 60 * 1000);
			_chest_count++;
		}

		CustomMessage msg1 = new CustomMessage("EventRabbits.START_MSG_1", true);
		Announcements.getInstance().announceToAll(msg1);

		CustomMessage msg2 = new CustomMessage("EventRabbits.START_MSG_2", true);
		Announcements.getInstance().announceToAll(msg2);

		CustomMessage msg3 = new CustomMessage("EventRabbits.START_MSG_3", true);
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
		CustomMessage msg = new CustomMessage("EventRabbits.TIME_UP", true);
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

		CustomMessage msg = new CustomMessage("EventRabbits.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		return true;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		if (event.equalsIgnoreCase("transform"))
		{
			if (player.isTransformed() || player.isInStance())
			{
				player.untransform();
			}

			SkillHolder.getInstance().getInfo(2428, 1).getEffects(npc, player);

			return null;
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
		return npc.getId() + ".htm";
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (Util.contains(targets, npc))
		{
			if (skill.getId() == _skill_tornado)
			{
				dropItem(npc, caster, DROPLIST);
				npc.deleteMe();
				_chest_count--;

				if (_chest_count <= 0)
				{
					CustomMessage msg = new CustomMessage("EventRabbits.NO_MORE", true);
					Announcements.getInstance().announceToAll(msg);
					eventStop();
				}
			}
			else if (skill.getId() == _skill_magic_eye)
			{
				if (npc instanceof L2EventChestInstance)
				{
					((L2EventChestInstance) npc).trigger();
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		if (_isactive && (npc.getId() == _npc_chest))
		{
			SkillHolder.getInstance().getInfo(4515, 1).getEffects(npc, attacker);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
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
		new Rabbits(-1, "Rabbits", "events");
	}
}
