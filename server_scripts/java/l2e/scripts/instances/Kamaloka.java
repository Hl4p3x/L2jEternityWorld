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
package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Kamaloka extends Quest
{
	private static String qn = "Kamaloka";

	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;

	private static final int EMPTY_DESTROY_TIME = 5;

	private static final int EXIT_TIME = 5;

	private static final int MAX_LEVEL_DIFFERENCE = 5;

	private static final boolean STEALTH_SHAMAN = true;

	private static final int[] INSTANCE_IDS =
	{
	                57,
	                58,
	                73,
	                60,
	                61,
	                74,
	                63,
	                64,
	                75,
	                66,
	                67,
	                76,
	                69,
	                70,
	                77,
	                72,
	                78,
	                79,
	                134
	};

	private static final int[] LEVEL =
	{
	                23,
	                26,
	                29,
	                33,
	                36,
	                39,
	                43,
	                46,
	                49,
	                53,
	                56,
	                59,
	                63,
	                66,
	                69,
	                73,
	                78,
	                81,
	                83
	};

	private static final int[] DURATION =
	{
	                30,
	                30,
	                45,
	                30,
	                30,
	                45,
	                30,
	                30,
	                45,
	                30,
	                30,
	                45,
	                30,
	                30,
	                45,
	                30,
	                45,
	                45,
	                45
	};

	private static final int[] MAX_PARTY_SIZE =
	{
	                6,
	                6,
	                9,
	                6,
	                6,
	                9,
	                6,
	                6,
	                9,
	                6,
	                6,
	                9,
	                6,
	                6,
	                9,
	                6,
	                9,
	                9,
	                9
	};

	private static final int[] BUFFS_WHITELIST =
	{
	                4322,
	                4323,
	                4324,
	                4325,
	                4326,
	                4327,
	                4328,
	                4329,
	                4330,
	                4331,
	                5632,
	                5637,
	                5950
	};

	private static final Location[] TELEPORTS =
	{
	                new Location(-88429, -220629, -7903),
	                new Location(-82464, -219532, -7899),
	                new Location(-10700, -174882, -10936),
	                new Location(-89683, -213573, -8106),
	                new Location(-81413, -213568, -8104),
	                new Location(-10700, -174882, -10936),
	                new Location(-89759, -206143, -8120),
	                new Location(-81415, -206078, -8107),
	                new Location(-10700, -174882, -10936),
	                new Location(-56999, -219856, -8117),
	                new Location(-48794, -220261, -8075),
	                new Location(-10700, -174882, -10936),
	                new Location(-56940, -212939, -8072),
	                new Location(-55566, -206139, -8120),
	                new Location(-10700, -174882, -10936),
	                new Location(-49805, -206139, -8117),
	                new Location(-10700, -174882, -10936),
	                new Location(-10700, -174882, -10936),
	                new Location(22003, -174886, -10900),
	};

	private static final int FIRST_ROOM_RESPAWN_DELAY = 25;

	private static final int[][] FIRST_ROOM =
	{
	                null,
	                null,
	                {
	                                22485,
	                                22486,
	                                5699,
	                                1
	                },
	                null,
	                null,
	                {
	                                22488,
	                                22489,
	                                5699,
	                                2
	                },
	                null,
	                null,
	                {
	                                22491,
	                                22492,
	                                5699,
	                                3
	                },
	                null,
	                null,
	                {
	                                22494,
	                                22495,
	                                5699,
	                                4
	                },
	                null,
	                null,
	                {
	                                22497,
	                                22498,
	                                5699,
	                                5
	                },
	                null,
	                {
	                                22500,
	                                22501,
	                                5699,
	                                6
	                },
	                {
	                                22503,
	                                22504,
	                                5699,
	                                7
	                },
	                {
	                                25706,
	                                25707,
	                                5699,
	                                7
	                }
	};

	private static final int[][][] FIRST_ROOM_SPAWNS =
	{
	                null,
	                null,
	                {
	                                {
	                                                -12381,
	                                                -174973,
	                                                -10955
	                                },
	                                {
	                                                -12413,
	                                                -174905,
	                                                -10955
	                                },
	                                {
	                                                -12377,
	                                                -174838,
	                                                -10953
	                                },
	                                {
	                                                -12316,
	                                                -174903,
	                                                -10953
	                                },
	                                {
	                                                -12326,
	                                                -174786,
	                                                -10953
	                                },
	                                {
	                                                -12330,
	                                                -175024,
	                                                -10953
	                                },
	                                {
	                                                -12211,
	                                                -174900,
	                                                -10955
	                                },
	                                {
	                                                -12238,
	                                                -174849,
	                                                -10953
	                                },
	                                {
	                                                -12233,
	                                                -174954,
	                                                -10953
	                                }
	                },
	                null,
	                null,
	                {
	                                {
	                                                -12381,
	                                                -174973,
	                                                -10955
	                                },
	                                {
	                                                -12413,
	                                                -174905,
	                                                -10955
	                                },
	                                {
	                                                -12377,
	                                                -174838,
	                                                -10953
	                                },
	                                {
	                                                -12316,
	                                                -174903,
	                                                -10953
	                                },
	                                {
	                                                -12326,
	                                                -174786,
	                                                -10953
	                                },
	                                {
	                                                -12330,
	                                                -175024,
	                                                -10953
	                                },
	                                {
	                                                -12211,
	                                                -174900,
	                                                -10955
	                                },
	                                {
	                                                -12238,
	                                                -174849,
	                                                -10953
	                                },
	                                {
	                                                -12233,
	                                                -174954,
	                                                -10953
	                                }
	                },
	                null,
	                null,
	                {
	                                {
	                                                -12381,
	                                                -174973,
	                                                -10955
	                                },
	                                {
	                                                -12413,
	                                                -174905,
	                                                -10955
	                                },
	                                {
	                                                -12377,
	                                                -174838,
	                                                -10953
	                                },
	                                {
	                                                -12316,
	                                                -174903,
	                                                -10953
	                                },
	                                {
	                                                -12326,
	                                                -174786,
	                                                -10953
	                                },
	                                {
	                                                -12330,
	                                                -175024,
	                                                -10953
	                                },
	                                {
	                                                -12211,
	                                                -174900,
	                                                -10955
	                                },
	                                {
	                                                -12238,
	                                                -174849,
	                                                -10953
	                                },
	                                {
	                                                -12233,
	                                                -174954,
	                                                -10953
	                                }
	                },
	                null,
	                null,
	                {
	                                {
	                                                -12381,
	                                                -174973,
	                                                -10955
	                                },
	                                {
	                                                -12413,
	                                                -174905,
	                                                -10955
	                                },
	                                {
	                                                -12377,
	                                                -174838,
	                                                -10953
	                                },
	                                {
	                                                -12316,
	                                                -174903,
	                                                -10953
	                                },
	                                {
	                                                -12326,
	                                                -174786,
	                                                -10953
	                                },
	                                {
	                                                -12330,
	                                                -175024,
	                                                -10953
	                                },
	                                {
	                                                -12211,
	                                                -174900,
	                                                -10955
	                                },
	                                {
	                                                -12238,
	                                                -174849,
	                                                -10953
	                                },
	                                {
	                                                -12233,
	                                                -174954,
	                                                -10953
	                                }
	                },
	                null,
	                null,
	                {
	                                {
	                                                -12381,
	                                                -174973,
	                                                -10955
	                                },
	                                {
	                                                -12413,
	                                                -174905,
	                                                -10955
	                                },
	                                {
	                                                -12377,
	                                                -174838,
	                                                -10953
	                                },
	                                {
	                                                -12316,
	                                                -174903,
	                                                -10953
	                                },
	                                {
	                                                -12326,
	                                                -174786,
	                                                -10953
	                                },
	                                {
	                                                -12330,
	                                                -175024,
	                                                -10953
	                                },
	                                {
	                                                -12211,
	                                                -174900,
	                                                -10955
	                                },
	                                {
	                                                -12238,
	                                                -174849,
	                                                -10953
	                                },
	                                {
	                                                -12233,
	                                                -174954,
	                                                -10953
	                                }
	                },
	                null,
	                {
	                                {
	                                                -12381,
	                                                -174973,
	                                                -10955
	                                },
	                                {
	                                                -12413,
	                                                -174905,
	                                                -10955
	                                },
	                                {
	                                                -12377,
	                                                -174838,
	                                                -10953
	                                },
	                                {
	                                                -12316,
	                                                -174903,
	                                                -10953
	                                },
	                                {
	                                                -12326,
	                                                -174786,
	                                                -10953
	                                },
	                                {
	                                                -12330,
	                                                -175024,
	                                                -10953
	                                },
	                                {
	                                                -12211,
	                                                -174900,
	                                                -10955
	                                },
	                                {
	                                                -12238,
	                                                -174849,
	                                                -10953
	                                },
	                                {
	                                                -12233,
	                                                -174954,
	                                                -10953
	                                }
	                },
	                {
	                                {
	                                                -12381,
	                                                -174973,
	                                                -10955
	                                },
	                                {
	                                                -12413,
	                                                -174905,
	                                                -10955
	                                },
	                                {
	                                                -12377,
	                                                -174838,
	                                                -10953
	                                },
	                                {
	                                                -12316,
	                                                -174903,
	                                                -10953
	                                },
	                                {
	                                                -12326,
	                                                -174786,
	                                                -10953
	                                },
	                                {
	                                                -12330,
	                                                -175024,
	                                                -10953
	                                },
	                                {
	                                                -12211,
	                                                -174900,
	                                                -10955
	                                },
	                                {
	                                                -12238,
	                                                -174849,
	                                                -10953
	                                },
	                                {
	                                                -12233,
	                                                -174954,
	                                                -10953
	                                }
	                },
	                {
	                                {
	                                                20409,
	                                                -174827,
	                                                -10912
	                                },
	                                {
	                                                20409,
	                                                -174947,
	                                                -10912
	                                },
	                                {
	                                                20494,
	                                                -174887,
	                                                -10912
	                                },
	                                {
	                                                20494,
	                                                -174767,
	                                                -10912
	                                },
	                                {
	                                                20614,
	                                                -174887,
	                                                -10912
	                                },
	                                {
	                                                20579,
	                                                -174827,
	                                                -10912
	                                },
	                                {
	                                                20579,
	                                                -174947,
	                                                -10912
	                                },
	                                {
	                                                20494,
	                                                -175007,
	                                                -10912
	                                },
	                                {
	                                                20374,
	                                                -174887,
	                                                -10912
	                                }
	                }
	};

	private static final int[][] SECOND_ROOM =
	{
	                null,
	                null,
	                {
	                                22487,
	                                5700,
	                                1
	                },
	                null,
	                null,
	                {
	                                22490,
	                                5700,
	                                2
	                },
	                null,
	                null,
	                {
	                                22493,
	                                5700,
	                                3
	                },
	                null,
	                null,
	                {
	                                22496,
	                                5700,
	                                4
	                },
	                null,
	                null,
	                {
	                                22499,
	                                5700,
	                                5
	                },
	                null,
	                {
	                                22502,
	                                5700,
	                                6
	                },
	                {
	                                22505,
	                                5700,
	                                7
	                },
	                {
	                                25708,
	                                5700,
	                                7
	                }

	};

	private static final int[][][] SECOND_ROOM_SPAWNS =
	{
	                null,
	                null,
	                {
	                                {
	                                                -14547,
	                                                -174901,
	                                                -10690
	                                },
	                                {
	                                                -14543,
	                                                -175030,
	                                                -10690
	                                },
	                                {
	                                                -14668,
	                                                -174900,
	                                                -10690
	                                },
	                                {
	                                                -14538,
	                                                -174774,
	                                                -10690
	                                },
	                                {
	                                                -14410,
	                                                -174904,
	                                                -10690
	                                }
	                },
	                null,
	                null,
	                {
	                                {
	                                                -14547,
	                                                -174901,
	                                                -10690
	                                },
	                                {
	                                                -14543,
	                                                -175030,
	                                                -10690
	                                },
	                                {
	                                                -14668,
	                                                -174900,
	                                                -10690
	                                },
	                                {
	                                                -14538,
	                                                -174774,
	                                                -10690
	                                },
	                                {
	                                                -14410,
	                                                -174904,
	                                                -10690
	                                }
	                },
	                null,
	                null,
	                {
	                                {
	                                                -14547,
	                                                -174901,
	                                                -10690
	                                },
	                                {
	                                                -14543,
	                                                -175030,
	                                                -10690
	                                },
	                                {
	                                                -14668,
	                                                -174900,
	                                                -10690
	                                },
	                                {
	                                                -14538,
	                                                -174774,
	                                                -10690
	                                },
	                                {
	                                                -14410,
	                                                -174904,
	                                                -10690
	                                }
	                },
	                null,
	                null,
	                {
	                                {
	                                                -14547,
	                                                -174901,
	                                                -10690
	                                },
	                                {
	                                                -14543,
	                                                -175030,
	                                                -10690
	                                },
	                                {
	                                                -14668,
	                                                -174900,
	                                                -10690
	                                },
	                                {
	                                                -14538,
	                                                -174774,
	                                                -10690
	                                },
	                                {
	                                                -14410,
	                                                -174904,
	                                                -10690
	                                }
	                },
	                null,
	                null,
	                {
	                                {
	                                                -14547,
	                                                -174901,
	                                                -10690
	                                },
	                                {
	                                                -14543,
	                                                -175030,
	                                                -10690
	                                },
	                                {
	                                                -14668,
	                                                -174900,
	                                                -10690
	                                },
	                                {
	                                                -14538,
	                                                -174774,
	                                                -10690
	                                },
	                                {
	                                                -14410,
	                                                -174904,
	                                                -10690
	                                }
	                },
	                null,
	                {
	                                {
	                                                -14547,
	                                                -174901,
	                                                -10690
	                                },
	                                {
	                                                -14543,
	                                                -175030,
	                                                -10690
	                                },
	                                {
	                                                -14668,
	                                                -174900,
	                                                -10690
	                                },
	                                {
	                                                -14538,
	                                                -174774,
	                                                -10690
	                                },
	                                {
	                                                -14410,
	                                                -174904,
	                                                -10690
	                                }
	                },
	                {
	                                {
	                                                -14547,
	                                                -174901,
	                                                -10690
	                                },
	                                {
	                                                -14543,
	                                                -175030,
	                                                -10690
	                                },
	                                {
	                                                -14668,
	                                                -174900,
	                                                -10690
	                                },
	                                {
	                                                -14538,
	                                                -174774,
	                                                -10690
	                                },
	                                {
	                                                -14410,
	                                                -174904,
	                                                -10690
	                                }
	                },
	                {
	                                {
	                                                18175,
	                                                -174991,
	                                                -10653
	                                },
	                                {
	                                                18070,
	                                                -174890,
	                                                -10655
	                                },
	                                {
	                                                18157,
	                                                -174886,
	                                                -10655
	                                },
	                                {
	                                                18249,
	                                                -174885,
	                                                -10653
	                                },
	                                {
	                                                18144,
	                                                -174821,
	                                                -10648
	                                }
	                }
	};

	private static final int[][] MINIBOSS =
	{
	                null,
	                null,
	                {
	                                25616,
	                                -16874,
	                                -174900,
	                                -10427,
	                                5701,
	                                1
	                },
	                null,
	                null,
	                {
	                                25617,
	                                -16874,
	                                -174900,
	                                -10427,
	                                5701,
	                                2
	                },
	                null,
	                null,
	                {
	                                25618,
	                                -16874,
	                                -174900,
	                                -10427,
	                                5701,
	                                3
	                },
	                null,
	                null,
	                {
	                                25619,
	                                -16874,
	                                -174900,
	                                -10427,
	                                5701,
	                                4
	                },
	                null,
	                null,
	                {
	                                25620,
	                                -16874,
	                                -174900,
	                                -10427,
	                                5701,
	                                5
	                },
	                null,
	                {
	                                25621,
	                                -16874,
	                                -174900,
	                                -10427,
	                                5701,
	                                6
	                },
	                {
	                                25622,
	                                -16874,
	                                -174900,
	                                -10427,
	                                5701,
	                                7
	                },
	                {
	                                25709,
	                                15828,
	                                -174885,
	                                -10384,
	                                5701,
	                                7
	                }
	};

	private static final int[][] BOSS =
	{
	                {
	                                18554,
	                                -88998,
	                                -220077,
	                                -7892
	                },
	                {
	                                18555,
	                                -81891,
	                                -220078,
	                                -7893
	                },
	                {
	                                29129,
	                                -20659,
	                                -174903,
	                                -9983
	                },
	                {
	                                18558,
	                                -89183,
	                                -213564,
	                                -8100
	                },
	                {
	                                18559,
	                                -81937,
	                                -213566,
	                                -8100
	                },
	                {
	                                29132,
	                                -20659,
	                                -174903,
	                                -9983
	                },
	                {
	                                18562,
	                                -89054,
	                                -206144,
	                                -8115
	                },
	                {
	                                18564,
	                                -81937,
	                                -206077,
	                                -8100
	                },
	                {
	                                29135,
	                                -20659,
	                                -174903,
	                                -9983
	                },
	                {
	                                18566,
	                                -56281,
	                                -219859,
	                                -8115
	                },
	                {
	                                18568,
	                                -49336,
	                                -220260,
	                                -8068
	                },
	                {
	                                29138,
	                                -20659,
	                                -174903,
	                                -9983
	                },
	                {
	                                18571,
	                                -56415,
	                                -212939,
	                                -8068
	                },
	                {
	                                18573,
	                                -56281,
	                                -206140,
	                                -8115
	                },
	                {
	                                29141,
	                                -20659,
	                                -174903,
	                                -9983
	                },
	                {
	                                18577,
	                                -49084,
	                                -206140,
	                                -8115
	                },
	                {
	                                29144,
	                                -20659,
	                                -174903,
	                                -9983
	                },
	                {
	                                29147,
	                                -20659,
	                                -174903,
	                                -9983
	                },
	                {
	                                25710,
	                                12047,
	                                -174887,
	                                -9944
	                }
	};

	private static final int[][] TELEPORTERS =
	{
	                null,
	                null,
	                {
	                                -10865,
	                                -174905,
	                                -10944
	                },
	                null,
	                null,
	                {
	                                -10865,
	                                -174905,
	                                -10944
	                },
	                null,
	                null,
	                {
	                                -10865,
	                                -174905,
	                                -10944
	                },
	                null,
	                null,
	                {
	                                -10865,
	                                -174905,
	                                -10944
	                },
	                null,
	                null,
	                {
	                                -10865,
	                                -174905,
	                                -10944
	                },
	                null,
	                {
	                                -10865,
	                                -174905,
	                                -10944
	                },
	                {
	                                -10865,
	                                -174905,
	                                -10944
	                },
	                {
	                                21837,
	                                -174885,
	                                -10904
	                }
	};

	private static final int TELEPORTER = 32496;

	private static final int[] CAPTAINS =
	{
	                30332,
	                30071,
	                30916,
	                30196,
	                31981,
	                31340
	};

	private class KamaWorld extends InstanceWorld
	{
		public int index;
		public int shaman = 0;
		public List<L2Spawn> firstRoom;
		public List<Integer> secondRoom;
		public int miniBoss = 0;
		public L2Npc boss = null;

		public KamaWorld()
		{
		}
	}

	public Kamaloka(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(TELEPORTER);
		addTalkId(TELEPORTER);

		for (int cap : CAPTAINS)
		{
			addStartNpc(cap);
			addTalkId(cap);
		}

		for (int[] mob : FIRST_ROOM)
		{
			if (mob != null)
			{
				if (STEALTH_SHAMAN)
				{
					addKillId(mob[1]);
				}
				else
				{
					addKillId(mob[0]);
				}
			}
		}

		for (int[] mob : SECOND_ROOM)
		{
			if (mob != null)
			{
				addKillId(mob[0]);
			}
		}

		for (int[] mob : MINIBOSS)
		{
			if (mob != null)
			{
				addKillId(mob[0]);
			}
		}

		for (int[] mob : BOSS)
		{
			addKillId(mob[0]);
		}
	}

	private static final boolean checkConditions(L2PcInstance player, int index)
	{
		final L2Party party = player.getParty();

		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		if (party.getMemberCount() > MAX_PARTY_SIZE[index])
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		final int level = LEVEL[index];
		final String instanceName = InstanceManager.getInstance().getInstanceIdName(INSTANCE_IDS[index]);

		Map<Integer, Long> instanceTimes;
		for (L2PcInstance partyMember : party.getMembers())
		{
			if (Math.abs(partyMember.getLevel() - level) > MAX_LEVEL_DIFFERENCE)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return false;
			}
			if (!partyMember.isInsideRadius(player, 1000, true, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return false;
			}
			instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(partyMember.getObjectId());
			if (instanceTimes != null)
			{
				for (int id : instanceTimes.keySet())
				{
					if (!instanceName.equals(InstanceManager.getInstance().getInstanceIdName(id)))
					{
						continue;
					}
					if (System.currentTimeMillis() < instanceTimes.get(id))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
						sm.addPcName(partyMember);
						player.sendPacket(sm);
						return false;
					}
				}
			}
		}
		return true;
	}

	private static final void removeBuffs(L2Character ch)
	{
		for (L2Effect e : ch.getAllEffects())
		{
			if (e == null)
			{
				continue;
			}
			L2Skill skill = e.getSkill();
			if (skill.isDebuff() || skill.isStayAfterDeath())
			{
				continue;
			}
			if (Arrays.binarySearch(BUFFS_WHITELIST, skill.getId()) >= 0)
			{
				continue;
			}
			e.exit();
		}
		if (ch.getSummon() != null)
		{
			for (L2Effect e : ch.getSummon().getAllEffects())
			{
				if (e == null)
				{
					continue;
				}
				L2Skill skill = e.getSkill();
				if (skill.isDebuff() || skill.isStayAfterDeath())
				{
					continue;
				}
				if (Arrays.binarySearch(BUFFS_WHITELIST, skill.getId()) >= 0)
				{
					continue;
				}
				e.exit();
			}
		}
	}

	private final synchronized void enterInstance(L2PcInstance player, int index)
	{
		int templateId;
		try
		{
			templateId = INSTANCE_IDS[index];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw e;
		}
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if (world != null)
		{
			if (!(world instanceof KamaWorld) || (world.templateId != templateId))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			if (Math.abs(player.getLevel() - LEVEL[((KamaWorld) world).index]) > MAX_LEVEL_DIFFERENCE)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(player);
				player.sendPacket(sm);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
			{
				removeBuffs(player);
				teleportPlayer(player, TELEPORTS[index], world.instanceId);
			}
			return;
		}
		if (!checkConditions(player, index))
		{
			return;
		}

		final int instanceId = InstanceManager.getInstance().createDynamicInstance(null);
		final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
		inst.setName(InstanceManager.getInstance().getInstanceIdName(templateId));
		inst.setSpawnLoc(new Location(player));
		inst.setAllowSummon(false);
		inst.setDuration(DURATION[index] * 60000);
		inst.setEmptyDestroyTime(EMPTY_DESTROY_TIME * 60000);

		world = new KamaWorld();
		world.instanceId = instanceId;
		world.templateId = templateId;
		((KamaWorld) world).index = index;
		InstanceManager.getInstance().addWorld(world);
		world.status = 0;
		spawnKama((KamaWorld) world);

		final L2Party party = player.getParty();
		for (L2PcInstance partyMember : party.getMembers())
		{
			if (partyMember.getQuestState(qn) == null)
			{
				newQuestState(partyMember);
			}
			world.allowed.add(partyMember.getObjectId());

			removeBuffs(partyMember);
			teleportPlayer(partyMember, TELEPORTS[index], instanceId);
		}
		return;
	}

	private static final void finishInstance(InstanceWorld world)
	{
		if (world instanceof KamaWorld)
		{
			Calendar reenter = Calendar.getInstance();
			reenter.set(Calendar.MINUTE, RESET_MIN);

			if (reenter.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
			{
				reenter.add(Calendar.DATE, 1);
			}
			reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
			sm.addInstanceName(world.templateId);

			for (int objectId : world.allowed)
			{
				L2PcInstance obj = L2World.getInstance().getPlayer(objectId);
				if ((obj != null) && obj.isOnline())
				{
					InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, reenter.getTimeInMillis());
					obj.sendPacket(sm);
				}
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			inst.setDuration(EXIT_TIME * 60000);
			inst.setEmptyDestroyTime(0);
		}
	}

	private final void spawnKama(KamaWorld world)
	{
		int[] npcs;
		int[][] spawns;
		L2Npc npc;
		final int index = world.index;

		npcs = FIRST_ROOM[index];
		spawns = FIRST_ROOM_SPAWNS[index];
		if (npcs != null)
		{
			world.firstRoom = new ArrayList<>(spawns.length - 1);
			int shaman = getRandom(spawns.length);

			for (int i = 0; i < spawns.length; i++)
			{
				if (i == shaman)
				{
					npc = addSpawn(STEALTH_SHAMAN ? npcs[1] : npcs[0], spawns[i][0], spawns[i][1], spawns[i][2], 0, false, 0, false, world.instanceId);
					world.shaman = npc.getObjectId();
				}
				else
				{
					npc = addSpawn(npcs[1], spawns[i][0], spawns[i][1], spawns[i][2], 0, false, 0, false, world.instanceId);
					L2Spawn spawn = npc.getSpawn();
					spawn.setRespawnDelay(FIRST_ROOM_RESPAWN_DELAY);
					spawn.setAmount(1);
					spawn.startRespawn();
					world.firstRoom.add(spawn);
				}
				npc.setIsNoRndWalk(true);
			}
		}
		npcs = SECOND_ROOM[index];
		spawns = SECOND_ROOM_SPAWNS[index];
		if (npcs != null)
		{
			world.secondRoom = new ArrayList<>(spawns.length);

			for (int[] spawn : spawns)
			{
				npc = addSpawn(npcs[0], spawn[0], spawn[1], spawn[2], 0, false, 0, false, world.instanceId);
				npc.setIsNoRndWalk(true);
				world.secondRoom.add(npc.getObjectId());
			}
		}

		if (MINIBOSS[index] != null)
		{
			npc = addSpawn(MINIBOSS[index][0], MINIBOSS[index][1], MINIBOSS[index][2], MINIBOSS[index][3], 0, false, 0, false, world.instanceId);
			npc.setIsNoRndWalk(true);
			world.miniBoss = npc.getObjectId();
		}

		if (TELEPORTERS[index] != null)
		{
			addSpawn(TELEPORTER, TELEPORTERS[index][0], TELEPORTERS[index][1], TELEPORTERS[index][2], 0, false, 0, false, world.instanceId);
		}

		npc = addSpawn(BOSS[index][0], BOSS[index][1], BOSS[index][2], BOSS[index][3], 0, false, 0, false, world.instanceId);
		((L2MonsterInstance) npc).setOnKillDelay(100);
		world.boss = npc;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc == null)
		{
			return "";
		}

		try
		{
			enterInstance(player, Integer.parseInt(event));
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
		return "";
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			newQuestState(player);
		}
		final int npcId = npc.getId();

		if (npcId == TELEPORTER)
		{
			final L2Party party = player.getParty();

			if ((party != null) && party.isLeader(player))
			{
				final InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if (world instanceof KamaWorld)
				{
					if (world.allowed.contains(player.getObjectId()))
					{
						Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);

						for (L2PcInstance partyMember : party.getMembers())
						{
							if ((partyMember != null) && (partyMember.getInstanceId() == world.instanceId))
							{
								teleportPlayer(partyMember, inst.getSpawnLoc(), 0);
							}
						}
					}
				}
			}
		}
		else
		{
			return npcId + ".htm";
		}

		return "";
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == TELEPORTER)
		{
			if (player.isInParty() && player.getParty().isLeader(player))
			{
				return "32496.htm";
			}
			return "32496-no.htm";
		}
		return "";
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof KamaWorld)
		{
			final KamaWorld world = (KamaWorld) tmpWorld;
			final int objectId = npc.getObjectId();

			if (world.firstRoom != null)
			{
				if ((world.shaman != 0) && (world.shaman == objectId))
				{
					world.shaman = 0;
					for (L2Spawn spawn : world.firstRoom)
					{
						if (spawn != null)
						{
							spawn.stopRespawn();
						}
					}
					world.firstRoom.clear();
					world.firstRoom = null;

					if (world.boss != null)
					{
						final int skillId = FIRST_ROOM[world.index][2];
						final int skillLvl = FIRST_ROOM[world.index][3];
						if ((skillId != 0) && (skillLvl != 0))
						{
							final L2Skill skill = SkillHolder.getInstance().getInfo(skillId, skillLvl);
							if (skill != null)
							{
								skill.getEffects(world.boss, world.boss);
							}
						}
					}
					return super.onKill(npc, player, isSummon);
				}
			}

			if (world.secondRoom != null)
			{
				boolean all = true;
				for (int i = 0; i < world.secondRoom.size(); i++)
				{
					if (world.secondRoom.get(i) == objectId)
					{
						world.secondRoom.set(i, 0);
					}
					else if (world.secondRoom.get(i) != 0)
					{
						all = false;
					}
				}

				if (all)
				{
					world.secondRoom.clear();
					world.secondRoom = null;

					if (world.boss != null)
					{
						final int skillId = SECOND_ROOM[world.index][1];
						final int skillLvl = SECOND_ROOM[world.index][2];
						if ((skillId != 0) && (skillLvl != 0))
						{
							final L2Skill skill = SkillHolder.getInstance().getInfo(skillId, skillLvl);
							if (skill != null)
							{
								skill.getEffects(world.boss, world.boss);
							}
						}
					}
					return super.onKill(npc, player, isSummon);
				}
			}

			if ((world.miniBoss != 0) && (world.miniBoss == objectId))
			{
				world.miniBoss = 0;

				if (world.boss != null)
				{
					final int skillId = MINIBOSS[world.index][4];
					final int skillLvl = MINIBOSS[world.index][5];
					if ((skillId != 0) && (skillLvl != 0))
					{
						final L2Skill skill = SkillHolder.getInstance().getInfo(skillId, skillLvl);
						if (skill != null)
						{
							skill.getEffects(world.boss, world.boss);
						}
					}
				}
				return super.onKill(npc, player, isSummon);
			}

			if ((world.boss != null) && (world.boss == npc))
			{
				world.boss = null;
				finishInstance(world);
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new Kamaloka(-1, qn, "instances");
	}
}
