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
package l2e.scripts.ai.npc.group_template;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Created by LordWinter 19.08.2012 Based on L2J Eternity-World
 */
public class TheSpiritOfPigs extends L2AttackableAIScript
{
	private static final int[][] LUCKPY_TRIGGER_MOB_IDS =
	{
	                {
	                                20589,
	                                20590,
	                                20591,
	                                20592,
	                                20593,
	                                20594,
	                                20595,
	                                20596,
	                                20597,
	                                20598,
	                                20599
	                },
	                {
	                                21520,
	                                21521,
	                                21522,
	                                21523,
	                                21524,
	                                21525,
	                                21526,
	                                21527,
	                                21528,
	                                21529,
	                                21530,
	                                21531,
	                                21532,
	                                21533,
	                                21534,
	                                21535,
	                                21536,
	                                21537,
	                                21538,
	                                21539,
	                                21540,
	                                21541,
	                                21542,
	                                21543,
	                                21544,
	                                21545,
	                                21546,
	                                21547,
	                                21548,
	                                21549,
	                                21550,
	                                21551,
	                                21552,
	                                21553,
	                                21554,
	                                21555,
	                                21556,
	                                21557,
	                                21558,
	                                21559,
	                                21560,
	                                21561,
	                                21562,
	                                21563,
	                                21564,
	                                21565,
	                                21566,
	                                21567,
	                                21568,
	                                21569,
	                                21570,
	                                21571,
	                                21572,
	                                21573,
	                                21574,
	                                21575,
	                                21576,
	                                21577,
	                                21578,
	                                21579,
	                                21580,
	                                21581,
	                                21582,
	                                21583,
	                                21584,
	                                21585,
	                                21586,
	                                21587,
	                                21588,
	                                21589,
	                                21590,
	                                21591,
	                                21592,
	                                21593,
	                                21594,
	                                21595,
	                                21596,
	                                21597,
	                                21598,
	                                21599,
	                                21600,
	                                21601
	                },
	                {
	                                18873,
	                                18874,
	                                18875,
	                                18876,
	                                18877,
	                                18878,
	                                18879,
	                                18880,
	                                18881,
	                                18882,
	                                18883,
	                                18884,
	                                18885,
	                                18886,
	                                18887,
	                                18888,
	                                18889,
	                                18890,
	                                18891,
	                                18892,
	                                18893,
	                                18894,
	                                18895,
	                                18896,
	                                18897,
	                                18898,
	                                18899,
	                                18900,
	                                18901,
	                                18902,
	                                18903,
	                                18904,
	                                18905,
	                                18906,
	                                18907,
	                                22196,
	                                22197,
	                                22198,
	                                22199,
	                                22200,
	                                22201,
	                                22202,
	                                22203,
	                                22204,
	                                22205,
	                                22206,
	                                22207,
	                                22208,
	                                22209,
	                                22210,
	                                22211,
	                                22212,
	                                22213,
	                                22214,
	                                22215,
	                                22216,
	                                22217,
	                                22218,
	                                22219,
	                                22220,
	                                22221,
	                                22222,
	                                22223,
	                                22224,
	                                22225,
	                                22226,
	                                22227,
	                                22650,
	                                22651,
	                                22652,
	                                22653,
	                                22654,
	                                22655,
	                                22656,
	                                22657,
	                                22658,
	                                22659,
	                                22691,
	                                22692,
	                                22693,
	                                22694,
	                                22695,
	                                22696,
	                                22697,
	                                22698,
	                                22699,
	                                22700,
	                                22701,
	                                22702,
	                                22703,
	                                22704,
	                                22705,
	                                22706,
	                                22707,
	                                22742,
	                                22743,
	                                22744,
	                                22745,
	                                22768,
	                                22769,
	                                22770,
	                                22771,
	                                22772,
	                                22773,
	                                22774,
	                                22775,
	                                22776,
	                                22777,
	                                22778,
	                                22779,
	                                22780,
	                                22781,
	                                22782,
	                                22783,
	                                22784,
	                                22785,
	                                22786,
	                                22787,
	                                22788,
	                                22815,
	                                22818,
	                                22819,
	                                22820,
	                                22821,
	                                22858
	                }
	};

	protected static final int[][] LUCKPY_ENLARGE_ADENA =
	{
	                {
	                                500,
	                                5000
	                },
	                {
	                                1000,
	                                10000
	                },
	                {
	                                2000,
	                                20000
	                }
	};

	private static final int LUCKPY_EATER = 18664;
	private static final int LUCKPY_NORMAL = 2502;
	private static final int LUCKPY_GOLD = 2503;
	protected static final SkillsHolder LUCKPY_ENLARGE = new SkillsHolder(23325, 1);
	protected static final SkillsHolder LUCKPY_REDUCE = new SkillsHolder(23326, 1);
	protected static final int[] GOLD_CHANCE =
	{
	                20,
	                35,
	                50,
	                50,
	                50,
	                50,
	                50,
	                50,
	                50,
	                50,
	                50
	};

	private static final int[][] DROPLIST_0 =
	{
	                {
	                                LUCKPY_NORMAL,
	                                25,
	                                8755,
	                                1,
	                                8755,
	                                2
	                },
	                {
	                                LUCKPY_GOLD,
	                                25,
	                                14678,
	                                1
	                }
	};

	private static final int[][] DROPLIST_1 =
	{
	                {
	                                LUCKPY_NORMAL,
	                                80,
	                                5577,
	                                1,
	                                5578,
	                                1,
	                                5579,
	                                1
	                },
	                {
	                                LUCKPY_NORMAL,
	                                30,
	                                5577,
	                                2,
	                                5578,
	                                2,
	                                5579,
	                                2
	                },
	                {
	                                LUCKPY_GOLD,
	                                25,
	                                14679,
	                                1
	                }
	};

	private static final int[][] DROPLIST_2 =
	{
	                {
	                                LUCKPY_NORMAL,
	                                80,
	                                9552,
	                                1,
	                                9553,
	                                1,
	                                9554,
	                                1,
	                                9555,
	                                1,
	                                9556,
	                                1,
	                                9557,
	                                1
	                },
	                {
	                                LUCKPY_NORMAL,
	                                30,
	                                9552,
	                                2,
	                                9553,
	                                2,
	                                9554,
	                                2,
	                                9555,
	                                2,
	                                9556,
	                                2,
	                                9557,
	                                2
	                },
	                {
	                                LUCKPY_GOLD,
	                                25,
	                                14680,
	                                1
	                }
	};

	protected static NpcStringId[] TEXT_RANDOM =
	{
	                NpcStringId.LUCKY_IM_LUCKY_THE_SPIRIT_THAT_LOVES_ADENA,
	                NpcStringId.LUCKY_I_WANT_TO_EAT_ADENA_GIVE_IT_TO_ME,
	                NpcStringId.LUCKY_IF_I_EAT_TOO_MUCH_ADENA_MY_WINGS_DISAPPEAR
	};

	protected static NpcStringId[] TEXT_EATING =
	{
	                NpcStringId.GRRRR_YUCK,
	                NpcStringId.LUCKY_IT_WASNT_ENOUGH_ADENA_ITS_GOTTA_BE_AT_LEAST_S,
	                NpcStringId.YUMMY_THANKS_LUCKY,
	                NpcStringId.LUCKY_THE_ADENA_IS_SO_YUMMY_IM_GETTING_BIGGER,
	                NpcStringId.LUCKY_NO_MORE_ADENA_OH_IM_SO_HEAVY,
	                NpcStringId.LUCKY_IM_FULL_THANKS_FOR_THE_YUMMY_ADENA_OH_IM_SO_HEAVY
	};

	protected static NpcStringId[] TEXT_NOWING =
	{
	                NpcStringId.OH_MY_WINGS_DISAPPEARED_ARE_YOU_GONNA_HIT_ME_IF_YOU_HIT_ME_ILL_THROW_UP_EVERYTHING_THAT_I_ATE,
	                NpcStringId.OH_MY_WINGS_ACK_ARE_YOU_GONNA_HIT_ME_SCARY_SCARY_IF_YOU_HIT_ME_SOMETHING_BAD_WILL_HAPPEN
	};

	protected Map<L2Npc, LivingLuckpy> _luckpyList = new FastMap<>();

	public TheSpiritOfPigs(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(LUCKPY_EATER);
		addTalkId(LUCKPY_EATER);
		addFirstTalkId(LUCKPY_EATER);

		if (Config.LUCKPY_ENABLED)
		{
			for (int[] temp : LUCKPY_TRIGGER_MOB_IDS)
			{
				addKillId(temp);
			}
		}
		addKillId(LUCKPY_NORMAL);
		addKillId(LUCKPY_GOLD);
	}

	static
	{
		Arrays.sort(DROPLIST_0, new Comparator<int[]>()
		{
			@Override
			public int compare(int[] a, int[] b)
			{
				return a[0] - b[0];
			}
		});
		Arrays.sort(DROPLIST_1, new Comparator<int[]>()
		{
			@Override
			public int compare(int[] a, int[] b)
			{
				return a[0] - b[0];
			}
		});
		Arrays.sort(DROPLIST_2, new Comparator<int[]>()
		{
			@Override
			public int compare(int[] a, int[] b)
			{
				return a[0] - b[0];
			}
		});
	}

	protected void dropItem(L2Npc mob, L2PcInstance player)
	{
		if (_luckpyList.containsKey(mob))
		{
			int npcId = mob.getId();
			LivingLuckpy pig = _luckpyList.remove(mob);

			if (pig.type == 0)
			{
				for (int[] drop : DROPLIST_0)
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
							((L2MonsterInstance) mob).dropItem(player, itemId, itemQty);
							continue;
						}
					}
					if (npcId < drop[0])
					{
						return;
					}
				}
			}
			else if (pig.type == 1)
			{
				for (int[] drop : DROPLIST_1)
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
							((L2MonsterInstance) mob).dropItem(player, itemId, itemQty);
							continue;
						}
					}
					if (npcId < drop[0])
					{
						return;
					}
				}
			}
			else if (pig.type == 2)
			{
				for (int[] drop : DROPLIST_2)
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
							((L2MonsterInstance) mob).dropItem(player, itemId, itemQty);
							continue;
						}
					}
					if (npcId < drop[0])
					{
						return;
					}
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("transform"))
		{
			if (_luckpyList.containsKey(npc))
			{
				(_luckpyList.get(npc)).eatCount = 10;
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());

		if (_luckpyList.containsKey(npc))
		{
			if ((_luckpyList.get(npc)).eatCount < 3)
			{
				html.setFile(player.getLang(), "data/html/default/18664.htm");
				player.sendPacket(html);
			}
			else
			{
				html.setFile(player.getLang(), "data/html/default/18664-1.htm");
				player.sendPacket(html);
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (_luckpyList.containsKey(npc))
		{
			dropItem(npc, killer);
		}
		else if (getRandom(1000) < 20)
		{
			for (byte i = 0; i < LUCKPY_TRIGGER_MOB_IDS.length; i = (byte) (i + 1))
			{
				if (!Util.contains(LUCKPY_TRIGGER_MOB_IDS[i], npc.getId()))
				{
					continue;
				}
				L2PcInstance player = killer.getActingPlayer();
				if (player == null)
				{
					return null;
				}

				LivingLuckpy newPig = new LivingLuckpy();
				newPig.type = i;
				newPig.targetAdena = (int) (getRandom(LUCKPY_ENLARGE_ADENA[i][0], LUCKPY_ENLARGE_ADENA[i][1]) * Config.RATE_DROP_ITEMS_ID.get(57));
				newPig.spawnTime = System.currentTimeMillis();
				L2Npc newPigNpc = addSpawn(LUCKPY_EATER, npc, true);
				_luckpyList.put(newPigNpc, newPig);
				ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(newPigNpc, 0), 2000L);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	protected static class LivingLuckpy
	{
		public byte type = -1;
		public int eatCount = 0;
		public byte full = 0;
		public int targetAdena = 0;
		public long spawnTime = 0L;
		public Location oldPos = null;
		public L2ItemInstance food = null;
	}

	protected class LuckpyEaterTask implements Runnable
	{
		protected L2Npc _pig;
		private final int _status;

		public LuckpyEaterTask(L2Npc pig, int status)
		{
			_pig = pig;
			_status = status;
		}

		@Override
		public void run()
		{
			if (!_luckpyList.containsKey(_pig))
			{
				return;
			}
			if ((System.currentTimeMillis() - (_luckpyList.get(_pig)).spawnTime) > 600000L)
			{
				_luckpyList.remove(_pig);
				_pig.deleteMe();
				return;
			}
			LivingLuckpy luckpy = _luckpyList.get(_pig);
			if ((_status != 4) && (luckpy.eatCount == 10))
			{
				int npcId = getRandom(100) < GOLD_CHANCE[luckpy.full] ? LUCKPY_GOLD : LUCKPY_NORMAL;
				L2Npc newPigNpc = spawnNpc(npcId, _pig.getLocation(), 0);
				_pig.deleteMe();
				_luckpyList.put(newPigNpc, _luckpyList.remove(_pig));
				ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(newPigNpc, 4), 2000L);
				return;
			}
			switch (_status)
			{
				case 0:
					if (luckpy.food != null)
					{
						return;
					}
					List<L2Object> foodTargets = new FastList<>();
					for (L2Object object : L2World.getInstance().getVisibleObjects(_pig, 300))
					{
						if (((object instanceof L2ItemInstance)) && (((L2ItemInstance) object).getId() == 57))
						{
							foodTargets.add(object);
						}
					}

					int minDist = 300000;
					for (L2Object adena : foodTargets)
					{
						int dx = _pig.getX() - adena.getX();
						int dy = _pig.getY() - adena.getY();
						int d = (dx * dx) + (dy * dy);
						if (d >= minDist)
						{
							continue;
						}
						minDist = d;
						luckpy.food = ((L2ItemInstance) adena);
					}

					if (minDist != 300000)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 1), 2000L);
					}
					else
					{
						if (getRandom(100) < 20)
						{
							_pig.broadcastPacket(new NpcSay(_pig.getObjectId(), Say2.ALL, _pig.getId(), TEXT_RANDOM[getRandom(TEXT_RANDOM.length)]));
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 0), 2000L);
					}
					break;
				case 1:
					Location newpos = new Location(luckpy.food.getX(), luckpy.food.getY(), luckpy.food.getZ(), 0);
					luckpy.oldPos = new Location(_pig.getX(), _pig.getY(), _pig.getZ(), _pig.getHeading());
					_pig.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, newpos);
					ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 2), 2000L);
					break;
				case 2:
					if ((luckpy.food == null) || (!luckpy.food.isVisible()))
					{
						_pig.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, luckpy.oldPos);
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 3), 2000L);
					}
					else if (_pig.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
					{
						Long count = Long.valueOf(luckpy.food.getCount());
						L2World.getInstance().removeVisibleObject(luckpy.food, luckpy.food.getWorldRegion());
						L2World.getInstance().removeObject(luckpy.food);
						_pig.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
						luckpy.food = null;
						if (Math.abs(luckpy.targetAdena - count.longValue()) <= ((LUCKPY_ENLARGE_ADENA[luckpy.type][1] * Config.RATE_DROP_ITEMS_ID.get(57)) / 800.0F))
						{
							_pig.broadcastPacket(new NpcSay(_pig.getObjectId(), Say2.ALL, _pig.getId(), TEXT_EATING[getRandom(3, 5)]));
							_pig.doCast(LUCKPY_ENLARGE.getSkill());
							LivingLuckpy tmp900_899 = luckpy;
							tmp900_899.full = (byte) (tmp900_899.full + 1);
						}
						else if (Math.abs(luckpy.targetAdena - count.longValue()) <= ((LUCKPY_ENLARGE_ADENA[luckpy.type][1] * Config.RATE_DROP_ITEMS_ID.get(57)) / 80.0F))
						{
							_pig.broadcastPacket(new NpcSay(_pig.getObjectId(), Say2.ALL, _pig.getId(), TEXT_EATING[getRandom(2, 5)]));
							if (luckpy.full == 0)
							{
								_pig.doCast(LUCKPY_ENLARGE.getSkill());
								LivingLuckpy tmp1015_1014 = luckpy;
								tmp1015_1014.full = (byte) (tmp1015_1014.full + 1);
							}
						}
						else
						{
							switch (luckpy.full)
							{
								case 1:
								case 2:
									LivingLuckpy tmp1061_1060 = luckpy;
									tmp1061_1060.full = (byte) (tmp1061_1060.full - 1);
								case 0:
									_pig.doCast(LUCKPY_REDUCE.getSkill());
									break;
								default:
									LivingLuckpy tmp1088_1087 = luckpy;
									tmp1088_1087.full = (byte) (tmp1088_1087.full - 1);
							}
							_pig.broadcastPacket(new NpcSay(_pig.getObjectId(), Say2.ALL, _pig.getId(), TEXT_EATING[getRandom(0, 1)]));
						}
						luckpy.eatCount += 1;
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 0), 2000L);
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 2), 2000L);
					}
					break;
				case 3:
					if (_pig.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
					{
						_pig.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
						luckpy.food = null;
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 0), 2000L);
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 3), 2000L);
					}
					break;
				case 4:
					if (_pig.isDead())
					{
						break;
					}
					if (getRandom(100) < 20)
					{
						_pig.broadcastPacket(new NpcSay(_pig.getObjectId(), Say2.ALL, _pig.getId(), TEXT_NOWING[getRandom(TEXT_NOWING.length)]));
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new LuckpyEaterTask(_pig, 4), 2000L);
			}
		}
	}

	public static void main(String[] args)
	{
		new TheSpiritOfPigs(-1, "TheSpiritOfPigs", "ai");
	}
}
