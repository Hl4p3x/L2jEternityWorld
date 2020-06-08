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
package l2e.scripts.custom;

import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

/**
 * Rework by LordWinter 04.05.2014 Based on L2J Eternity-World
 */
public class DragonVortex extends Quest
{
	protected static final int VORTEX_1 = 32871;
	protected static final int VORTEX_2 = 32892;
	protected static final int VORTEX_3 = 32893;
	protected static final int VORTEX_4 = 32894;

	protected final FastList<L2Npc> bosses1 = new FastList<>();
	protected final FastList<L2Npc> bosses2 = new FastList<>();
	protected final FastList<L2Npc> bosses3 = new FastList<>();
	protected final FastList<L2Npc> bosses4 = new FastList<>();

	protected boolean progress1 = false;
	protected boolean progress2 = false;
	protected boolean progress3 = false;
	protected boolean progress4 = false;

	protected ScheduledFuture<?> _despawnTask1;
	protected ScheduledFuture<?> _despawnTask2;
	protected ScheduledFuture<?> _despawnTask3;
	protected ScheduledFuture<?> _despawnTask4;

	private static final int LARGE_DRAGON_BONE = 17248;

	private static final int[] RAIDS =
	{
	                25724,
	                25723,
	                25722,
	                25721,
	                25720,
	                25719,
	                25718,
	};

	protected L2Npc boss1;
	protected L2Npc boss2;
	protected L2Npc boss3;
	protected L2Npc boss4;

	protected int boss1ObjId = 0;
	protected int boss2ObjId = 0;
	protected int boss3ObjId = 0;
	protected int boss4ObjId = 0;

	private static Location[] BOSS_SPAWN_1 =
	{
	                new Location(91948, 113665, -3059),
	                new Location(92486, 113568, -3072),
	                new Location(92519, 114071, -3072),
	                new Location(91926, 114162, -3072)
	};

	private static Location[] BOSS_SPAWN_2 =
	{
	                new Location(108953, 112366, -3047),
	                new Location(108500, 112039, -3047),
	                new Location(108977, 111575, -3047),
	                new Location(109316, 112004, -3033)
	};

	private static Location[] BOSS_SPAWN_3 =
	{
	                new Location(109840, 125178, -3687),
	                new Location(110461, 125227, -3687),
	                new Location(110405, 125814, -3687),
	                new Location(109879, 125828, -3686)
	};

	private static Location[] BOSS_SPAWN_4 =
	{
	                new Location(121543, 113580, -3793),
	                new Location(120877, 113714, -3793),
	                new Location(120848, 113058, -3793),
	                new Location(121490, 113084, -3793)
	};

	private static final int DESPAWN_DELAY = 3600000;

	public DragonVortex(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(VORTEX_1, VORTEX_2, VORTEX_3, VORTEX_4);
		addStartNpc(VORTEX_1, VORTEX_2, VORTEX_3, VORTEX_4);
		addTalkId(VORTEX_1, VORTEX_2, VORTEX_3, VORTEX_4);

		for (int i : RAIDS)
		{
			addKillId(i);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Spawn"))
		{
			if (npc.getId() == VORTEX_1)
			{
				if (progress1)
				{
					return "32871-03.htm";
				}

				if (hasQuestItems(player, LARGE_DRAGON_BONE))
				{
					takeItems(player, LARGE_DRAGON_BONE, 1);
					Location bossSpawn = BOSS_SPAWN_1[getRandom(0, BOSS_SPAWN_1.length - 1)];
					if (Config.DRAGON_VORTEX_UNLIMITED_SPAWN)
					{
						addSpawn(RAIDS[getRandom(RAIDS.length)], new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true), false, 0);
					}
					else
					{
						boss1 = addSpawn(RAIDS[getRandom(RAIDS.length)], new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true), false, 0);
						progress1 = true;

						if (boss1 != null)
						{
							bosses1.add(boss1);
							boss1ObjId = boss1.getObjectId();
						}
						_despawnTask1 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnFirstVortrexBoss(), DESPAWN_DELAY);
					}
					return "32871-01.htm";
				}
				return "32871-02.htm";
			}

			if (npc.getId() == VORTEX_2)
			{
				if (progress2)
				{
					return "32871-03.htm";
				}

				if (hasQuestItems(player, LARGE_DRAGON_BONE))
				{
					takeItems(player, LARGE_DRAGON_BONE, 1);
					Location bossSpawn = BOSS_SPAWN_2[getRandom(0, BOSS_SPAWN_2.length - 1)];
					if (Config.DRAGON_VORTEX_UNLIMITED_SPAWN)
					{
						addSpawn(RAIDS[getRandom(RAIDS.length)], new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true), false, 0);
					}
					else
					{
						boss2 = addSpawn(RAIDS[getRandom(RAIDS.length)], new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true), false, 0);
						progress2 = true;

						if (boss2 != null)
						{
							bosses2.add(boss2);
							boss2ObjId = boss2.getObjectId();
						}
						_despawnTask2 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnSecondVortrexBoss(), DESPAWN_DELAY);
					}
					return "32871-01.htm";
				}
				return "32871-02.htm";
			}

			if (npc.getId() == VORTEX_3)
			{
				if (progress3)
				{
					return "32871-03.htm";
				}

				if (hasQuestItems(player, LARGE_DRAGON_BONE))
				{
					takeItems(player, LARGE_DRAGON_BONE, 1);
					Location bossSpawn = BOSS_SPAWN_3[getRandom(0, BOSS_SPAWN_3.length - 1)];
					if (Config.DRAGON_VORTEX_UNLIMITED_SPAWN)
					{
						addSpawn(RAIDS[getRandom(RAIDS.length)], new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true), false, 0);
					}
					else
					{
						boss3 = addSpawn(RAIDS[getRandom(RAIDS.length)], new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true), false, 0);
						progress3 = true;

						if (boss3 != null)
						{
							bosses3.add(boss3);
							boss3ObjId = boss3.getObjectId();
						}
						_despawnTask3 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnThirdVortrexBoss(), DESPAWN_DELAY);
					}
					return "32871-01.htm";
				}
				return "32871-02.htm";
			}

			if (npc.getId() == VORTEX_4)
			{
				if (progress4)
				{
					return "32871-03.htm";
				}

				if (hasQuestItems(player, LARGE_DRAGON_BONE))
				{
					takeItems(player, LARGE_DRAGON_BONE, 1);
					Location bossSpawn = BOSS_SPAWN_4[getRandom(0, BOSS_SPAWN_4.length - 1)];
					if (Config.DRAGON_VORTEX_UNLIMITED_SPAWN)
					{
						addSpawn(RAIDS[getRandom(RAIDS.length)], new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true), false, 0);
					}
					else
					{
						boss4 = addSpawn(RAIDS[getRandom(RAIDS.length)], new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true), false, 0);
						progress4 = true;

						if (boss4 != null)
						{
							bosses4.add(boss4);
							boss4ObjId = boss4.getObjectId();
						}
						_despawnTask4 = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnFourthVortrexBoss(), DESPAWN_DELAY);
					}
					return "32871-01.htm";
				}
				return "32871-02.htm";
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		return "32871.htm";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcObjId = npc.getObjectId();

		if ((boss1ObjId != 0) && (npcObjId == boss1ObjId) && progress1)
		{
			progress1 = false;
			boss1ObjId = 0;
			bosses1.clear();
			if (_despawnTask1 != null)
			{
				_despawnTask1.cancel(true);
			}
		}

		if ((boss2ObjId != 0) && (npcObjId == boss2ObjId) && progress2)
		{
			progress2 = false;
			boss2ObjId = 0;
			bosses2.clear();
			if (_despawnTask2 != null)
			{
				_despawnTask2.cancel(true);
			}
		}

		if ((boss3ObjId != 0) && (npcObjId == boss3ObjId) && progress3)
		{
			progress3 = false;
			boss3ObjId = 0;
			bosses3.clear();
			if (_despawnTask3 != null)
			{
				_despawnTask3.cancel(true);
			}
		}

		if ((boss4ObjId != 0) && (npcObjId == boss4ObjId) && progress4)
		{
			progress4 = false;
			boss4ObjId = 0;
			bosses4.clear();
			if (_despawnTask4 != null)
			{
				_despawnTask4.cancel(true);
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	protected class SpawnFirstVortrexBoss implements Runnable
	{
		@Override
		public void run()
		{
			if (!bosses1.isEmpty())
			{
				for (L2Npc boss : bosses1)
				{
					if (boss != null)
					{
						boss.deleteMe();
						progress1 = false;
					}
				}
				boss1ObjId = 0;
				bosses1.clear();
			}
		}
	}

	protected class SpawnSecondVortrexBoss implements Runnable
	{
		@Override
		public void run()
		{
			if (!bosses2.isEmpty())
			{
				for (L2Npc boss : bosses2)
				{
					if (boss != null)
					{
						boss.deleteMe();
						progress2 = false;
					}
				}
				boss2ObjId = 0;
				bosses2.clear();
			}
		}
	}

	protected class SpawnThirdVortrexBoss implements Runnable
	{
		@Override
		public void run()
		{
			if (!bosses3.isEmpty())
			{
				for (L2Npc boss : bosses3)
				{
					if (boss != null)
					{
						boss.deleteMe();
						progress3 = false;
					}
				}
				boss3ObjId = 0;
				bosses3.clear();
			}
		}
	}

	protected class SpawnFourthVortrexBoss implements Runnable
	{
		@Override
		public void run()
		{
			if (!bosses4.isEmpty())
			{
				for (L2Npc boss : bosses4)
				{
					if (boss != null)
					{
						boss.deleteMe();
						progress4 = false;
					}
				}
				boss4ObjId = 0;
				bosses4.clear();
			}
		}
	}

	public static void main(String[] args)
	{
		new DragonVortex(-1, "DragonVortex", "custom");
	}
}
