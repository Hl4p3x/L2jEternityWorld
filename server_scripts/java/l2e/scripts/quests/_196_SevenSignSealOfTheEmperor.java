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
package l2e.scripts.quests;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 04.07.2011 Based on L2J Eternity-World
 */
public class _196_SevenSignSealOfTheEmperor extends Quest
{
	public static final String qn = "_196_SevenSignSealOfTheEmperor";

	protected static class SIGNSNpc
	{
		public L2Npc npc;

		public boolean isDead = false;
	}

	protected static class SIGNSRoom
	{
		public FastList<SIGNSNpc> npcList = new FastList<>();
	}

	private class SIGNSWorld extends InstanceWorld
	{
		public FastMap<String, SIGNSRoom> rooms = new FastMap<>();

		public L2Attackable _lilith = null;
		public L2Attackable _lilith_guard0 = null;
		public L2Attackable _lilith_guard1 = null;
		public L2Attackable _anakim = null;
		public L2Attackable _anakim_guard0 = null;
		public L2Attackable _anakim_guard1 = null;
		public L2Attackable _anakim_guard2 = null;

		public long[] storeTime =
		{
		                0,
		                0
		};

		public SIGNSWorld()
		{
		}
	}

	private static boolean noRndWalk = true;

	private static final int INSTANCE_ID = 112;

	// NPCs
	private static final int HEINE = 30969;
	private static final int MAMMON = 32584;
	private static final int SHUNAIMAN = 32586;
	private static final int MAGICAN = 32598;
	private static final int WOOD = 32593;
	private static final int LEON = 32587;
	private static final int PROMICE_OF_MAMMON = 32585;
	private static final int DISCIPLES_GK = 32657;

	// FIGHTING NPCS
	private static final int LILITH = 32715;
	private static final int LILITH_GUARD0 = 32716;
	private static final int LILITH_GUARD1 = 32717;
	private static final int ANAKIM = 32718;
	private static final int ANAKIM_GUARD0 = 32719;
	private static final int ANAKIM_GUARD1 = 32720;
	private static final int ANAKIM_GUARD2 = 32721;

	// DOOR
	private static final int DOOR2 = 17240102;
	private static final int DOOR4 = 17240104;
	private static final int DOOR6 = 17240106;
	private static final int DOOR8 = 17240108;
	private static final int DOOR10 = 17240110;
	private static final int DOOR = 17240111;

	// INSTANCE TP
	private static final int[] TELEPORT =
	{
	                -89559,
	                216030,
	                -7488
	};

	private static final int[] NPCS =
	{
	                HEINE,
	                WOOD,
	                MAMMON,
	                MAGICAN,
	                SHUNAIMAN,
	                LEON,
	                PROMICE_OF_MAMMON,
	                DISCIPLES_GK
	};

	// MOBs
	private static final int SEALDEVICE = 27384;

	private static int[] NOTMOVE =
	{
	                ANAKIM,
	                LILITH,
	                SEALDEVICE
	};

	private static final int[] TOKILL =
	{
	                27371,
	                27372,
	                27373,
	                27374,
	                27375,
	                27377,
	                27378,
	                27379,
	                27384
	};

	private static final int[] TOCHAT =
	{
	                27371,
	                27372,
	                27373,
	                27377,
	                27378,
	                27379
	};

	// QUEST ITEMS
	private static final int WATER = 13808;
	private static final int SWORD = 15310;
	private static final int SEAL = 13846;
	private static final int STAFF = 13809;
	private static final int EINHASAD_STRIKE = 8357;

	private int mammonst = 0;

	private static final NpcStringId[] ANAKIM_TEXT =
	{
	                NpcStringId.FOR_THE_ETERNITY_OF_EINHASAD,
	                NpcStringId.DEAR_SHILLIENS_OFFSPRINGS_YOU_ARE_NOT_CAPABLE_OF_CONFRONTING_US,
	                NpcStringId.ILL_SHOW_YOU_THE_REAL_POWER_OF_EINHASAD,
	                NpcStringId.DEAR_MILITARY_FORCE_OF_LIGHT_GO_DESTROY_THE_OFFSPRINGS_OF_SHILLIEN
	};

	private static final NpcStringId[] LILITH_TEXT =
	{
	                NpcStringId.YOU_SUCH_A_FOOL_THE_VICTORY_OVER_THIS_WAR_BELONGS_TO_SHILIEN,
	                NpcStringId.HOW_DARE_YOU_TRY_TO_CONTEND_AGAINST_ME_IN_STRENGTH_RIDICULOUS,
	                NpcStringId.ANAKIM_IN_THE_NAME_OF_GREAT_SHILIEN_I_WILL_CUT_YOUR_THROAT,
	                NpcStringId.YOU_CANNOT_BE_THE_MATCH_OF_LILITH_ILL_TEACH_YOU_A_LESSON
	};

	private static void removeBuffs(L2Character ch)
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
			e.exit();
		}
	}

	public _196_SevenSignSealOfTheEmperor(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(HEINE);
		addSkillSeeId(SEALDEVICE);
		addAttackId(SEALDEVICE);

		for (int i : NPCS)
		{
			addTalkId(i);
		}

		for (int mob : TOKILL)
		{
			addKillId(mob);
		}

		for (int mob1 : TOCHAT)
		{
			addAggroRangeEnterId(mob1);
		}

		addAttackId(LILITH);
		addAttackId(LILITH_GUARD0);
		addAttackId(LILITH_GUARD1);
		addAttackId(ANAKIM);
		addAttackId(ANAKIM_GUARD0);
		addAttackId(ANAKIM_GUARD1);
		addAttackId(ANAKIM_GUARD2);

		for (int id : NOTMOVE)
		{
			addSpawnId(id);
		}

		questItemIds = new int[]
		{
		                SWORD,
		                WATER,
		                SEAL,
		                STAFF
		};
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		if (tmpworld instanceof SIGNSWorld)
		{
			if (npc.getId() == 27371)
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			if (npc.getId() == 27372)
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			if ((npc.getId() == 27373) || (npc.getId() == 27379))
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			if (npc.getId() == 27377)
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			if (npc.getId() == 27378)
			{
				((L2Attackable) npc).abortAttack();
				npc.setTarget(player);
				npc.setIsRunning(true);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}
		return null;
	}

	protected void runStartRoom(SIGNSWorld world)
	{
		world.status = 0;
		SIGNSRoom StartRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SHUNAIMAN, -89456, 216184, -7504, 40960, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(LEON, -89400, 216125, -7504, 40960, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(DISCIPLES_GK, -84385, 216117, -7497, 0, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(MAGICAN, -84945, 220643, -7495, 0, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(MAGICAN, -89563, 220647, -7491, 0, false, 0, false, world.instanceId);
		StartRoom.npcList.add(thisnpc);

		world.rooms.put("StartRoom", StartRoom);
	}

	protected void runFirstRoom(SIGNSWorld world)
	{
		SIGNSRoom FirstRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371, -89049, 217979, -7495, 0, false, 0, false, world.instanceId);
		FirstRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -89049, 217979, -7495, 0, false, 0, false, world.instanceId);
		FirstRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -89049, 217979, -7495, 0, false, 0, false, world.instanceId);
		FirstRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -89049, 217979, -7495, 0, false, 0, false, world.instanceId);
		FirstRoom.npcList.add(thisnpc);

		world.rooms.put("FirstRoom", FirstRoom);
		world.status = 1;
	}

	protected void runSecondRoom(SIGNSWorld world)
	{
		SIGNSRoom SecondRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27371, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -88599, 220071, -7495, 0, false, 0, false, world.instanceId);
		SecondRoom.npcList.add(thisnpc);

		world.rooms.put("SecondRoom", SecondRoom);
		world.status = 2;
	}

	protected void runThirdRoom(SIGNSWorld world)
	{
		SIGNSRoom ThirdRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27371, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -86846, 220639, -7495, 0, false, 0, false, world.instanceId);
		ThirdRoom.npcList.add(thisnpc);

		world.rooms.put("ThirdRoom", ThirdRoom);
		world.status = 3;
	}

	protected void runForthRoom(SIGNSWorld world)
	{
		SIGNSRoom ForthRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27375, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27377, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27378, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27379, -85463, 219227, -7495, 0, false, 0, false, world.instanceId);
		ForthRoom.npcList.add(thisnpc);

		world.rooms.put("ForthRoom", ForthRoom);
		world.status = 4;
	}

	protected void runFifthRoom(SIGNSWorld world)
	{
		SIGNSRoom FifthRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;
		thisnpc = new SIGNSNpc();
		thisnpc.isDead = false;
		thisnpc.npc = addSpawn(27371, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27372, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27373, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27374, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27375, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27375, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27377, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27377, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27378, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27378, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27379, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(27379, -87441, 217623, -7495, 0, false, 0, false, world.instanceId);
		FifthRoom.npcList.add(thisnpc);

		world.rooms.put("FifthRoom", FifthRoom);
		world.status = 5;
	}

	protected void runBossRoom(SIGNSWorld world)
	{
		world._lilith = (L2Attackable) addSpawn(LILITH, -83175, 217021, -7504, 49151, false, 0, false, world.instanceId);
		world._lilith_guard0 = (L2Attackable) addSpawn(LILITH_GUARD0, -83222, 217055, -7504, 49151, false, 0, false, world.instanceId);
		world._lilith_guard1 = (L2Attackable) addSpawn(LILITH_GUARD1, -83127, 217056, -7504, 49151, false, 0, false, world.instanceId);
		world._anakim = (L2Attackable) addSpawn(ANAKIM, -83179, 216479, -7504, 16384, false, 0, false, world.instanceId);
		world._anakim_guard0 = (L2Attackable) addSpawn(ANAKIM_GUARD0, -83227, 216443, -7504, 16384, false, 0, false, world.instanceId);
		world._anakim_guard1 = (L2Attackable) addSpawn(ANAKIM_GUARD1, -83179, 216432, -7504, 16384, false, 0, false, world.instanceId);
		world._anakim_guard2 = (L2Attackable) addSpawn(ANAKIM_GUARD2, -83134, 216443, -7504, 16384, false, 0, false, world.instanceId);

		world._lilith_guard0.setIsImmobilized(true);
		world._lilith_guard1.setIsImmobilized(true);
		world._anakim_guard0.setIsImmobilized(true);
		world._anakim_guard1.setIsImmobilized(true);
		world._anakim_guard2.setIsImmobilized(true);

		SIGNSRoom BossRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -83177, 217353, -7520, 32768, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -83177, 216137, -7520, 32768, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -82588, 216754, -7520, 32768, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -83804, 216754, -7520, 32768, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(32592, -83176, 216753, -7497, 0, false, 0, false, world.instanceId);
		BossRoom.npcList.add(thisnpc);

		world.rooms.put("BossRoom", BossRoom);
		world.status = 6;
	}

	protected void runSDRoom(SIGNSWorld world)
	{
		SIGNSRoom SDRoom = new SIGNSRoom();
		SIGNSNpc thisnpc;

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -83177, 217353, -7520, 32768, false, 0, false, world.instanceId);
		SDRoom.npcList.add(thisnpc);
		if (noRndWalk)
		{
			thisnpc.npc.setIsNoRndWalk(true);
		}
		thisnpc.npc.setRHandId(15281);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -83177, 216137, -7520, 32768, false, 0, false, world.instanceId);
		SDRoom.npcList.add(thisnpc);
		if (noRndWalk)
		{
			thisnpc.npc.setIsNoRndWalk(true);
		}
		thisnpc.npc.setRHandId(15281);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -82588, 216754, -7520, 32768, false, 0, false, world.instanceId);
		SDRoom.npcList.add(thisnpc);
		if (noRndWalk)
		{
			thisnpc.npc.setIsNoRndWalk(true);
		}
		thisnpc.npc.setRHandId(15281);

		thisnpc = new SIGNSNpc();
		thisnpc.npc = addSpawn(SEALDEVICE, -83804, 216754, -7520, 32768, false, 0, false, world.instanceId);
		SDRoom.npcList.add(thisnpc);
		if (noRndWalk)
		{
			thisnpc.npc.setIsNoRndWalk(true);
		}
		thisnpc.npc.setRHandId(15281);

		world.rooms.put("SDRoom", SDRoom);
	}

	protected boolean checkKillProgress(L2Npc npc, SIGNSRoom room)
	{
		boolean cont = true;
		for (SIGNSNpc npcobj : room.npcList)
		{
			if (npcobj.npc == npc)
			{
				npcobj.isDead = true;
			}
			if (npcobj.isDead == false)
			{
				cont = false;
			}
		}

		return cont;
	}

	private static void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		removeBuffs(player);
		if (player.hasSummon())
		{
			removeBuffs(player.getSummon());
		}
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	private synchronized void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world.templateId != INSTANCE_ID)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
			{
				teleportPlayer(player, TELEPORT, world.instanceId);
			}
			return;
		}

		final int instanceId = InstanceManager.getInstance().createDynamicInstance("SanctumSealOfTheEmperor.xml");

		world = new SIGNSWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCE_ID;
		InstanceManager.getInstance().addWorld(world);
		((SIGNSWorld) world).storeTime[0] = System.currentTimeMillis();
		runStartRoom((SIGNSWorld) world);
		runFirstRoom((SIGNSWorld) world);
		world.allowed.add(player.getObjectId());
		teleportPlayer(player, TELEPORT, instanceId);
	}

	protected void exitInstance(L2PcInstance player)
	{
		player.setInstanceId(0);
		player.teleToLocation(171782, -17612, -4901);

		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
		inst.setDuration(5 * 60000);
		inst.setEmptyDestroyTime(0);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		int npcId = npc.getId();
		if ((npcId == LILITH) || (npcId == LILITH_GUARD0) || (npcId == LILITH_GUARD1))
		{
			npc.setCurrentHp(npc.getCurrentHp() + damage);
			((L2Attackable) npc).stopHating(attacker);
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SIGNSWorld)
		{
			SIGNSWorld world = (SIGNSWorld) tmpworld;

			if ((world.status == 6) && (npc.getId() == SEALDEVICE))
			{
				npc.doCast(SkillHolder.getInstance().getInfo(5980, 3));
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof SIGNSWorld)
		{
			SIGNSWorld world = (SIGNSWorld) tmpworld;

			if ((skill.getId() == EINHASAD_STRIKE) && (world.status == 6) && (npc.getId() == SEALDEVICE))
			{
				npc.doCast(SkillHolder.getInstance().getInfo(5980, 3));
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);

		if (event.equalsIgnoreCase("30969-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32598-02.htm"))
		{
			st.giveItems(STAFF, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30969-11.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");

		}
		else if (event.equalsIgnoreCase("32584-05.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
			npc.deleteMe();
		}
		else if (event.equalsIgnoreCase("32586-06.htm"))
		{
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "4");
			st.giveItems(SWORD, 1);
			st.giveItems(WATER, 1);
		}
		else if (event.equalsIgnoreCase("32586-12.htm"))
		{
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "5");
			st.takeItems(SEAL, 4);
			st.takeItems(SWORD, 1);
			st.takeItems(WATER, 1);
			st.takeItems(STAFF, 1);
		}
		else if (event.equalsIgnoreCase("32593-02.htm"))
		{
			st.addExpAndSp(52518015, 5817676);
			st.unset("cond");
			st.setState(State.COMPLETED);
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		else if (event.equalsIgnoreCase("30969-06.htm"))
		{
			if (mammonst == 0)
			{
				mammonst = 1;
				L2Npc mammon = addSpawn(MAMMON, 109742, 219978, -3520, 0, false, 120000, true);
				mammon.broadcastPacket(new NpcSay(mammon.getObjectId(), 0, mammon.getId(), NpcStringId.WHO_DARES_SUMMON_THE_MERCHANT_OF_MAMMON));
				st.startQuestTimer("despawn", 120000, mammon);
			}
			else
			{
				return "30969-06a.htm";
			}
		}
		else if (event.equalsIgnoreCase("despawn"))
		{
			mammonst = 0;
			return null;
		}
		else if (event.equalsIgnoreCase("DOORS"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof SIGNSWorld)
			{
				SIGNSWorld world = (SIGNSWorld) tmpworld;

				openDoor(DOOR, world.instanceId);
				for (int objId : world.allowed)
				{
					L2PcInstance pl = L2World.getInstance().getPlayer(objId);
					if (pl != null)
					{
						pl.showQuestMovie(12);
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new SpawnLilithRoom(world), 22000);
					st.startQuestTimer("lilith_text", 26000, npc);
					st.startQuestTimer("anakim_text", 26000, npc);
					st.startQuestTimer("go_fight", 25000, npc);
				}
				return null;
			}
		}
		else if (event.equalsIgnoreCase("Tele"))
		{
			player.teleToLocation(-89528, 216056, -7516);
			return null;
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof SIGNSWorld)
		{
			SIGNSWorld world = (SIGNSWorld) tmpworld;

			if (event.equalsIgnoreCase("anakim_text"))
			{
				cancelQuestTimer("anakim_text", npc, player);
				NpcSay ns = new NpcSay(world._anakim.getObjectId(), 0, world._anakim.getId(), ANAKIM_TEXT[getRandom(ANAKIM_TEXT.length)]);
				player.sendPacket(ns);
				startQuestTimer("anakim_text", 20000, npc, player);
				return null;
			}
			else if (event.equalsIgnoreCase("lilith_text"))
			{
				cancelQuestTimer("lilith_text", npc, player);
				NpcSay ns = new NpcSay(world._lilith.getObjectId(), 0, world._lilith.getId(), LILITH_TEXT[getRandom(LILITH_TEXT.length)]);
				player.sendPacket(ns);
				startQuestTimer("lilith_text", 22000, npc, player);
				return null;
			}
			else if (event.equalsIgnoreCase("go_fight"))
			{
				world._lilith_guard0.setIsImmobilized(false);
				world._lilith_guard1.setIsImmobilized(false);
				world._anakim_guard0.setIsImmobilized(false);
				world._anakim_guard1.setIsImmobilized(false);
				world._anakim_guard2.setIsImmobilized(false);

				world._lilith.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getLilithTarget(world));
				world._anakim.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAnakimTarget(world));
				world._lilith_guard0.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getLilithTargetGuard(world));
				world._lilith_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getLilithTargetGuard(world));
				world._anakim_guard0.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAnakimTargetGuard(world));
				world._anakim_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAnakimTargetGuard(world));
				world._anakim_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAnakimTargetGuard(world));
			}
			else if (event.equalsIgnoreCase("Delete"))
			{
				world._lilith.deleteMe();
				world._lilith = null;
				world._anakim.deleteMe();
				world._anakim = null;
				world._lilith_guard0.deleteMe();
				world._lilith_guard0 = null;
				world._lilith_guard1.deleteMe();
				world._lilith_guard1 = null;
				world._anakim_guard0.deleteMe();
				world._anakim_guard0 = null;
				world._anakim_guard1.deleteMe();
				world._anakim_guard1 = null;
				world._anakim_guard2.deleteMe();
				world._anakim_guard2 = null;
				return null;
			}
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);

		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}

		final int cond = st.getInt("cond");
		switch (npc.getId())
		{
			case HEINE:
				if (player.getLevel() < 79)
				{
					st.exitQuest(true);
					htmltext = "30969-00.htm";
				}
				QuestState qs = player.getQuestState("_195_SevenSignSecretRitualOfThePriests");
				if (qs == null)
				{
					return htmltext;
				}
				if (qs.isCompleted() && (st.getState() == State.CREATED))
				{
					htmltext = "30969-01.htm";
				}
				else
				{
					switch (cond)
					{
						case 0:
							st.exitQuest(true);
							htmltext = "30969-00.htm";
							break;
						case 1:
							htmltext = "30969-05.htm";
							break;
						case 2:
							st.set("cond", "3");
							htmltext = "30969-08.htm";
							break;
						case 5:
							htmltext = "30969-09.htm";
							break;
						case 6:
							htmltext = "30969-11.htm";
							break;
					}
				}
				break;
			case WOOD:
				if (cond == 6)
				{
					htmltext = "32593-01.htm";
				}
				else if (st.getState() == State.COMPLETED)
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			case MAMMON:
				switch (cond)
				{
					case 1:
						htmltext = "32584-01.htm";
						break;
				}
				break;
			case PROMICE_OF_MAMMON:
				switch (cond)
				{
					case 0:
						return null;
					case 1:
						return null;
					case 2:
						return null;
					case 3:
						enterInstance(player);
					case 4:
						enterInstance(player);
					case 5:
						return null;
					case 6:
						return null;
				}
				break;
			case MAGICAN:
				switch (cond)
				{
					case 4:
						if (st.getQuestItemsCount(STAFF) == 0)
						{
							htmltext = "32598-01.htm";
						}
						if (st.getQuestItemsCount(STAFF) >= 1)
						{
							htmltext = "32598-03.htm";
						}
						break;
				}
				break;
			case SHUNAIMAN:
				switch (cond)
				{
					case 3:
						htmltext = "32586-01.htm";
						break;
					case 4:
						if (st.getQuestItemsCount(SWORD) == 0)
						{
							st.giveItems(SWORD, 1);
							htmltext = "32586-14.htm";
						}
						if (st.getQuestItemsCount(WATER) == 0)
						{
							st.giveItems(WATER, 1);
							htmltext = "32586-14.htm";
						}
						if (st.getQuestItemsCount(SEAL) <= 3)
						{
							htmltext = "32586-07.htm";
						}
						if (st.getQuestItemsCount(SEAL) == 4)
						{
							htmltext = "32586-08.htm";
						}
						break;
					case 5:
						htmltext = "32586-13.htm";
						break;
				}
				break;
			case DISCIPLES_GK:
				switch (cond)
				{
					case 4:
						htmltext = "32657-01.htm";
						break;
				}
				break;
			case LEON:
				switch (cond)
				{
					case 3:
						exitInstance(player);
						htmltext = "32587-02.htm";
						break;
					case 4:
						exitInstance(player);
						htmltext = "32587-02.htm";
						break;
					case 5:
						exitInstance(player);
						htmltext = "32587-02.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if ((npc != null) && Util.contains(NOTMOVE, npc.getId()))
		{
			npc.setIsNoRndWalk(true);
			npc.setIsImmobilized(true);
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		SIGNSWorld world;

		if (st == null)
		{
			return null;
		}

		if (tmpworld instanceof SIGNSWorld)
		{
			world = (SIGNSWorld) tmpworld;

			if (world.status == 1)
			{
				if (checkKillProgress(npc, world.rooms.get("FirstRoom")))
				{
					runSecondRoom(world);
					openDoor(DOOR2, world.instanceId);
				}
			}
			else if (world.status == 2)
			{
				if (checkKillProgress(npc, world.rooms.get("SecondRoom")))
				{
					runThirdRoom(world);
					openDoor(DOOR4, world.instanceId);
				}
			}
			else if (world.status == 3)
			{
				if (checkKillProgress(npc, world.rooms.get("ThirdRoom")))
				{
					runForthRoom(world);
					openDoor(DOOR6, world.instanceId);
				}
			}
			else if (world.status == 4)
			{
				if (checkKillProgress(npc, world.rooms.get("ForthRoom")))
				{
					runFifthRoom(world);
					openDoor(DOOR8, world.instanceId);
				}
			}
			else if (world.status == 5)
			{
				if (checkKillProgress(npc, world.rooms.get("FifthRoom")))
				{
					openDoor(DOOR10, world.instanceId);
				}
			}
			else if (world.status == 6)
			{
				if (npc.getId() == SEALDEVICE)
				{
					if (st.getQuestItemsCount(SEAL) < 3)
					{
						npc.setRHandId(15281);
						st.playSound("ItemSound.quest_itemget");
						st.giveItems(SEAL, 1);
					}
					else
					{
						npc.setRHandId(15281);
						st.giveItems(SEAL, 1);
						st.playSound("ItemSound.quest_middle");
						runSDRoom(world);
						player.showQuestMovie(13);
						startQuestTimer("Tele", 26000, null, player);
						startQuestTimer("Delete", 26000, null, player);
					}
				}
			}
		}
		return "";
	}

	private class SpawnLilithRoom implements Runnable
	{
		private final SIGNSWorld _world;

		public SpawnLilithRoom(SIGNSWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			if (_world != null)
			{
				runBossRoom(_world);
			}
		}
	}

	private L2Npc getLilithTarget(SIGNSWorld world)
	{
		FastList<L2Npc> npcList = new FastList<>();
		L2Npc victim = null;
		victim = world._anakim;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		if (npcList.size() > 0)
		{
			return npcList.get(getRandom(npcList.size() - 1));
		}
		return null;
	}

	private L2Npc getLilithTargetGuard(SIGNSWorld world)
	{
		FastList<L2Npc> npcList = new FastList<>();
		L2Npc victim = null;
		victim = world._anakim_guard0;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._anakim_guard1;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._anakim_guard2;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		if (npcList.size() > 0)
		{
			return npcList.get(getRandom(npcList.size() - 1));
		}
		return null;
	}

	private L2Npc getAnakimTarget(SIGNSWorld world)
	{
		FastList<L2Npc> npcList = new FastList<>();
		L2Npc victim = null;
		victim = world._lilith;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		if (npcList.size() > 0)
		{
			return npcList.get(getRandom(npcList.size() - 1));
		}
		return null;
	}

	private L2Npc getAnakimTargetGuard(SIGNSWorld world)
	{
		FastList<L2Npc> npcList = new FastList<>();
		L2Npc victim = null;
		victim = world._lilith_guard0;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._lilith_guard1;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		if (npcList.size() > 0)
		{
			return npcList.get(getRandom(npcList.size() - 1));
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _196_SevenSignSealOfTheEmperor(196, qn, "");
	}
}
