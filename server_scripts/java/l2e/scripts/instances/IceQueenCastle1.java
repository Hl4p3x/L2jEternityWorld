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

import javolution.util.FastList;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class IceQueenCastle1 extends Quest
{
	private static final String qn = "IceQueenCastle1";
	private static final int INSTANCEID = 137;

	private static final int _jinia_2 = 32781;
	private static final int _freya = 18847;
	private static final int _jinia_guard1 = 18848;
	private static final int _jinia_guard2 = 18849;
	private static final int _jinia_guard3 = 18926;
	private static final int _ice_knight = 22767;
	private static final int _freya_controller = 18930;

	private static final int[] ENTRY_POINT =
	{
	                114000,
	                -112357,
	                -11200
	};

	protected class FDWorld extends InstanceWorld
	{
		public L2Attackable _freya = null;
		public L2Attackable _jinia_guard1 = null;
		public L2Attackable _jinia_guard2 = null;
		public L2Attackable _jinia_guard3 = null;
		public L2Attackable _jinia_guard4 = null;
		public L2Attackable _jinia_guard5 = null;
		public L2Attackable _jinia_guard6 = null;
		public L2Attackable _freya_guard1 = null;
		public L2Attackable _freya_guard2 = null;
		public L2Attackable _freya_guard3 = null;
		public L2Attackable _freya_guard4 = null;
		public L2Attackable _freya_guard5 = null;
		public L2Attackable _freya_controller = null;

		public FDWorld()
		{
		}
	}

	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}

	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}

	public IceQueenCastle1(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_jinia_2);
		addTalkId(_jinia_2);

		addAggroRangeEnterId(_freya_controller);

		addAttackId(_jinia_guard1);
		addAttackId(_jinia_guard2);
		addAttackId(_jinia_guard3);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (npcId == _jinia_2)
		{
			teleCoord tele = new teleCoord();
			tele.x = ENTRY_POINT[0];
			tele.y = ENTRY_POINT[1];
			tele.z = ENTRY_POINT[2];

			QuestState hostQuest = player.getQuestState("_10285_MeetingSirra");

			if ((hostQuest != null) && (hostQuest.getState() == State.STARTED) && (hostQuest.getInt("progress") == 2))
			{
				hostQuest.set("cond", "9");
				hostQuest.playSound("ItemSound.quest_middle");
			}

			if (enterInstance(player, "IceQueenCastle1.xml", tele) <= 0)
			{
				return "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "Freya.CAN_NOT_ENTER") + "</body></html>";
			}
		}
		return "";
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof FDWorld)
		{
			FDWorld world = (FDWorld) tmpworld;

			if (event.equalsIgnoreCase("check_guards"))
			{
				if (((world._freya_guard1 == null) || world._freya_guard1.isDead()) && (getQuestTimer("spawn_ice_guard1", null, player) == null))
				{
					startQuestTimer("spawn_ice_guard1", 30000, null, player);
				}
				if (((world._freya_guard2 == null) || world._freya_guard2.isDead()) && (getQuestTimer("spawn_ice_guard2", null, player) == null))
				{
					startQuestTimer("spawn_ice_guard2", 30000, null, player);
				}
				if (((world._freya_guard3 == null) || world._freya_guard3.isDead()) && (getQuestTimer("spawn_ice_guard3", null, player) == null))
				{
					startQuestTimer("spawn_ice_guard3", 30000, null, player);
				}
				if (((world._freya_guard4 == null) || world._freya_guard4.isDead()) && (getQuestTimer("spawn_ice_guard4", null, player) == null))
				{
					startQuestTimer("spawn_ice_guard4", 30000, null, player);
				}
				if (((world._freya_guard5 == null) || world._freya_guard5.isDead()) && (getQuestTimer("spawn_ice_guard5", null, player) == null))
				{
					startQuestTimer("spawn_ice_guard5", 30000, null, player);
				}

				if (((world._jinia_guard1 == null) || world._jinia_guard1.isDead()) && (getQuestTimer("spawn_guard1", null, player) == null))
				{
					startQuestTimer("spawn_guard1", 60000, null, player);
				}
				else
				{
					world._jinia_guard1.stopHating(player);
				}
				if (((world._jinia_guard2 == null) || world._jinia_guard2.isDead()) && (getQuestTimer("spawn_guard2", null, player) == null))
				{
					startQuestTimer("spawn_guard2", 45000, null, player);
				}
				else
				{
					world._jinia_guard2.stopHating(player);
				}
				if (((world._jinia_guard3 == null) || world._jinia_guard3.isDead()) && (getQuestTimer("spawn_guard3", null, player) == null))
				{
					startQuestTimer("spawn_guard3", 45000, null, player);
				}
				else
				{
					world._jinia_guard3.stopHating(player);
				}
				if (((world._jinia_guard4 == null) || world._jinia_guard4.isDead()) && (getQuestTimer("spawn_guard4", null, player) == null))
				{
					startQuestTimer("spawn_guard4", 60000, null, player);
				}
				else
				{
					world._jinia_guard4.stopHating(player);
				}
				if (((world._jinia_guard5 == null) || world._jinia_guard5.isDead()) && (getQuestTimer("spawn_guard5", null, player) == null))
				{
					startQuestTimer("spawn_guard5", 45000, null, player);
				}
				else
				{
					world._jinia_guard5.stopHating(player);
				}
				if (((world._jinia_guard6 == null) || world._jinia_guard6.isDead()) && (getQuestTimer("spawn_guard6", null, player) == null))
				{
					startQuestTimer("spawn_guard6", 45000, null, player);
				}
				else
				{
					world._jinia_guard6.stopHating(player);
				}
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard1"))
			{
				world._freya_guard1 = (L2Attackable) addSpawn(_ice_knight, 114713, -115109, -11198, 16456, false, 0, false, world.instanceId);
				L2Character target = getRandomTargetFreya(world);
				(world._freya_guard1).addDamageHate(target, 9999, 9999);
				world._freya_guard1.setRunning();
				world._freya_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard2"))
			{
				world._freya_guard2 = (L2Attackable) addSpawn(_ice_knight, 114008, -115080, -11198, 3568, false, 0, false, world.instanceId);
				L2Character target = getRandomTargetFreya(world);
				(world._freya_guard2).addDamageHate(target, 9999, 9999);
				world._freya_guard2.setRunning();
				world._freya_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard3"))
			{
				world._freya_guard3 = (L2Attackable) addSpawn(_ice_knight, 114422, -115508, -11198, 12400, false, 0, false, world.instanceId);
				L2Character target = getRandomTargetFreya(world);
				(world._freya_guard3).addDamageHate(target, 9999, 9999);
				world._freya_guard3.setRunning();
				world._freya_guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard4"))
			{
				world._freya_guard4 = (L2Attackable) addSpawn(_ice_knight, 115023, -115508, -11198, 20016, false, 0, false, world.instanceId);
				L2Character target = getRandomTargetFreya(world);
				(world._freya_guard4).addDamageHate(target, 9999, 9999);
				world._freya_guard4.setRunning();
				world._freya_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard5"))
			{
				world._freya_guard5 = (L2Attackable) addSpawn(_ice_knight, 115459, -115079, -11198, 27936, false, 0, false, world.instanceId);
				L2Character target = getRandomTargetFreya(world);
				(world._freya_guard5).addDamageHate(target, 9999, 9999);
				world._freya_guard5.setRunning();
				world._freya_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_guard1"))
			{
				world._jinia_guard1 = (L2Attackable) addSpawn(_jinia_guard1, 114861, -113615, -11198, -21832, false, 0, false, world.instanceId);
				world._jinia_guard1.setRunning();
				L2Character target = getRandomTargetGuard(world);
				(world._jinia_guard1).addDamageHate(target, 9999, 9999);
				world._jinia_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_guard2"))
			{
				world._jinia_guard2 = (L2Attackable) addSpawn(_jinia_guard2, 114950, -113647, -11198, -20880, false, 0, false, world.instanceId);
				world._jinia_guard2.setRunning();
				L2Character target = getRandomTargetGuard(world);
				(world._jinia_guard2).addDamageHate(target, 9999, 9999);
				world._jinia_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_guard3"))
			{
				world._jinia_guard3 = (L2Attackable) addSpawn(_jinia_guard3, 115041, -113694, -11198, -22440, false, 0, false, world.instanceId);
				world._jinia_guard3.setRunning();
				L2Character target = getRandomTargetGuard(world);
				(world._jinia_guard3).addDamageHate(target, 9999, 9999);
				world._jinia_guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_guard4"))
			{
				world._jinia_guard4 = (L2Attackable) addSpawn(_jinia_guard1, 114633, -113619, -11198, -12224, false, 0, false, world.instanceId);
				world._jinia_guard4.setRunning();
				L2Character target = getRandomTargetGuard(world);
				(world._jinia_guard4).addDamageHate(target, 9999, 9999);
				world._jinia_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_guard5"))
			{
				world._jinia_guard5 = (L2Attackable) addSpawn(_jinia_guard2, 114540, -113654, -11198, -12880, false, 0, false, world.instanceId);
				world._jinia_guard5.setRunning();
				L2Character target = getRandomTargetGuard(world);
				(world._jinia_guard5).addDamageHate(target, 9999, 9999);
				world._jinia_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("spawn_guard6"))
			{
				world._jinia_guard6 = (L2Attackable) addSpawn(_jinia_guard3, 114446, -113698, -11198, -11264, false, 0, false, world.instanceId);
				world._jinia_guard6.setRunning();
				L2Character target = getRandomTargetGuard(world);
				(world._jinia_guard6).addDamageHate(target, 9999, 9999);
				world._jinia_guard6.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("call_freya_skill"))
			{
				L2Object target = world._freya.getTarget();
				if ((target != null) && (player != null) && (target.getObjectId() == player.getObjectId()) && !world._freya.isCastingNow())
				{
					if (getRandom(100) < 40)
					{
						world._freya.doCast(SkillHolder.getInstance().getInfo(6278, 1));
					}
				}
			}
			else if (event.equalsIgnoreCase("go_guards"))
			{
				NpcSay ns = new NpcSay(world._jinia_guard1.getObjectId(), 0, world._jinia_guard1.getId(), NpcStringId.S1_MAY_THE_PROTECTION_OF_THE_GODS_BE_UPON_YOU);
				ns.addStringParameter(player.getAppearance().getVisibleName());
				player.sendPacket(ns);

				world._jinia_guard1.setRunning();
				world._jinia_guard2.setRunning();
				world._jinia_guard3.setRunning();
				world._jinia_guard4.setRunning();
				world._jinia_guard5.setRunning();
				world._jinia_guard6.setRunning();
				world._jinia_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114673, -113374, -11200, 0));
				world._jinia_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114745, -113383, -11200, 0));
				world._jinia_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114711, -113382, -11200, 0));
				world._jinia_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114662, -113382, -11200, 0));

				startQuestTimer("go_fight", 3000, null, player);
			}
			else if (event.equalsIgnoreCase("go_fight"))
			{
				world._jinia_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114673, -114324, -11200, 0));
				world._jinia_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114745, -114324, -11200, 0));
				world._jinia_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114711, -114324, -11200, 0));
				world._jinia_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114662, -114324, -11200, 0));
				world._jinia_guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(115041, -114324, -11200, 0));
				world._jinia_guard6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114446, -114324, -11200, 0));

				world._freya_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114713, -114920, -11200, 0));
				world._freya_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114008, -114920, -11200, 0));
				world._freya_guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114422, -114920, -11200, 0));
				world._freya_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(115023, -114920, -11200, 0));
				world._freya_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(115459, -114920, -11200, 0));
				world._freya.setRunning();
				world._freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114722, -114798, -11205, 15956));
				startQuestTimer("freya", 17000, null, player);
				startQuestTimer("go_fight2", 7000, null, player);
			}
			else if (event.equalsIgnoreCase("go_fight2"))
			{
				world._jinia_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getRandomTargetGuard(world));
				world._jinia_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getRandomTargetGuard(world));
				world._jinia_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getRandomTargetGuard(world));
				world._jinia_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getRandomTargetGuard(world));
				world._jinia_guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getRandomTargetGuard(world));
				world._jinia_guard6.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getRandomTargetGuard(world));
			}
			else if (event.equalsIgnoreCase("freya"))
			{
				L2Character target = getRandomTargetFreya(world);
				(world._freya).addDamageHate(target, 9999, 9999);
				world._freya.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (event.equalsIgnoreCase("end_inst"))
			{
				cancelQuestTimer("spawn_guard1", null, player);
				cancelQuestTimer("spawn_guard2", null, player);
				cancelQuestTimer("spawn_guard3", null, player);
				cancelQuestTimer("spawn_guard4", null, player);
				cancelQuestTimer("spawn_guard5", null, player);
				cancelQuestTimer("spawn_guard6", null, player);
				cancelQuestTimer("check_guards", null, player);
				cancelQuestTimer("spawn_ice_guard1", null, player);
				cancelQuestTimer("spawn_ice_guard2", null, player);
				cancelQuestTimer("spawn_ice_guard3", null, player);
				cancelQuestTimer("spawn_ice_guard4", null, player);
				cancelQuestTimer("spawn_ice_guard5", null, player);
				cancelQuestTimer("call_freya_skill", null, player);

				world._freya.abortAttack();
				world._freya.abortCast();
				world._freya.setTarget(player);
				world._freya.doCast(SkillHolder.getInstance().getInfo(6275, 1));
				startQuestTimer("movie", 7000, null, player);
			}
			else if (event.equalsIgnoreCase("movie"))
			{
				player.sendPacket(new ExShowScreenMessage2(1801111, 3000, ExShowScreenMessage2.ScreenMessageAlign.MIDDLE_CENTER, true, false, -1, true));
				startQuestTimer("movie2", 3000, null, player);

				QuestState st = player.getQuestState("_10285_MeetingSirra");
				if ((st != null) && (st.getState() == State.STARTED) && (st.getInt("progress") == 2))
				{
					st.set("cond", "10");
					st.playSound("ItemSound.quest_middle");
					st.set("progress", "3");
				}
			}
			else if (event.equalsIgnoreCase("movie2"))
			{
				player.showQuestMovie(21);

				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				inst.setDuration(5 * 60000);
				inst.setEmptyDestroyTime(0);

				player.setInstanceId(0);
				player.teleToLocation(113851, -108987, -837);

				InstanceManager.getInstance().destroyInstance(world.instanceId);
			}
		}
		return null;
	}

	protected int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if (world != null)
		{
			if (!(world instanceof FDWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}

		if (!checkCond(player))
		{
			return 0;
		}
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new FDWorld();

		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;

		world.allowed.add(player.getObjectId());

		InstanceManager.getInstance().addWorld(world);
		teleto.instanceId = instanceId;
		teleportplayer(player, teleto);
		world.allowed.add(player.getObjectId());
		spawnFirst((FDWorld) world);

		return instanceId;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof FDWorld)
		{
			FDWorld world = (FDWorld) tmpworld;
			if (npc.getId() == _freya_controller)
			{
				world._jinia_guard1.setIsImmobilized(false);
				world._jinia_guard2.setIsImmobilized(false);
				world._jinia_guard3.setIsImmobilized(false);
				world._jinia_guard4.setIsImmobilized(false);
				world._jinia_guard5.setIsImmobilized(false);
				world._jinia_guard6.setIsImmobilized(false);
				world._freya.setIsImmobilized(false);
				world._freya_guard1.setIsImmobilized(false);
				world._freya_guard2.setIsImmobilized(false);
				world._freya_guard3.setIsImmobilized(false);
				world._freya_guard4.setIsImmobilized(false);
				world._freya_guard5.setIsImmobilized(false);

				startQuestTimer("go_guards", 300, npc, player);
				startQuestTimer("end_inst", 120000, npc, player);
				startQuestTimer("check_guards", 1000, null, player, true);
				startQuestTimer("call_freya_skill", 7000, null, player, true);
				world._freya_controller.deleteMe();
				world._freya_controller = null;
			}
		}
		return null;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		int npcId = npc.getId();
		if ((npcId == _jinia_guard1) || (npcId == _jinia_guard2) || (npcId == _jinia_guard3))
		{
			npc.setCurrentHp(npc.getCurrentHp() + damage);
			((L2Attackable) npc).stopHating(attacker);
		}
		return onAttack(npc, attacker, damage, isSummon);
	}

	private void spawnFirst(FDWorld world)
	{
		world._freya = (L2Attackable) addSpawn(_freya, 114722, -114798, -11205, 15956, false, 0, false, world.instanceId);
		world._freya.teleToLocation(114720, -117085, -11088, 15956, false);
		world._jinia_guard1 = (L2Attackable) addSpawn(_jinia_guard1, 114861, -113615, -11198, -21832, false, 0, false, world.instanceId);
		world._jinia_guard2 = (L2Attackable) addSpawn(_jinia_guard2, 114950, -113647, -11198, -20880, false, 0, false, world.instanceId);
		world._jinia_guard3 = (L2Attackable) addSpawn(_jinia_guard3, 115041, -113694, -11198, -22440, false, 0, false, world.instanceId);
		world._jinia_guard4 = (L2Attackable) addSpawn(_jinia_guard1, 114633, -113619, -11198, -12224, false, 0, false, world.instanceId);
		world._jinia_guard5 = (L2Attackable) addSpawn(_jinia_guard2, 114540, -113654, -11198, -12880, false, 0, false, world.instanceId);
		world._jinia_guard6 = (L2Attackable) addSpawn(_jinia_guard3, 114446, -113698, -11198, -11264, false, 0, false, world.instanceId);
		world._freya_guard1 = (L2Attackable) addSpawn(_ice_knight, 114713, -115109, -11198, 16456, false, 0, false, world.instanceId);
		world._freya_guard2 = (L2Attackable) addSpawn(_ice_knight, 114008, -115080, -11198, 3568, false, 0, false, world.instanceId);
		world._freya_guard3 = (L2Attackable) addSpawn(_ice_knight, 114422, -115508, -11198, 12400, false, 0, false, world.instanceId);
		world._freya_guard4 = (L2Attackable) addSpawn(_ice_knight, 115023, -115508, -11198, 20016, false, 0, false, world.instanceId);
		world._freya_guard5 = (L2Attackable) addSpawn(_ice_knight, 115459, -115079, -11198, 27936, false, 0, false, world.instanceId);
		world._freya_controller = (L2Attackable) addSpawn(_freya_controller, 114713, -113578, -11200, 27936, false, 0, false, world.instanceId);

		world._freya_controller.setIsImmobilized(true);
		world._jinia_guard1.setIsImmobilized(true);
		world._jinia_guard2.setIsImmobilized(true);
		world._jinia_guard3.setIsImmobilized(true);
		world._jinia_guard4.setIsImmobilized(true);
		world._jinia_guard5.setIsImmobilized(true);
		world._jinia_guard6.setIsImmobilized(true);
		world._freya.setIsImmobilized(true);
		world._freya_guard1.setIsImmobilized(true);
		world._freya_guard2.setIsImmobilized(true);
		world._freya_guard3.setIsImmobilized(true);
		world._freya_guard4.setIsImmobilized(true);
		world._freya_guard5.setIsImmobilized(true);
		world._freya_guard1.setRunning();
		world._freya_guard2.setRunning();
		world._freya_guard3.setRunning();
		world._freya_guard4.setRunning();
		world._freya_guard5.setRunning();

		InstanceManager.getInstance().getInstance(world.instanceId).getDoor(23140101).openMe();
	}

	private L2Npc getRandomTargetFreya(FDWorld world)
	{
		FastList<L2Npc> npcList = new FastList<>();
		L2Npc victim = null;
		victim = world._jinia_guard1;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._jinia_guard2;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._jinia_guard3;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._jinia_guard4;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._jinia_guard5;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._jinia_guard6;
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

	private L2Npc getRandomTargetGuard(FDWorld world)
	{
		FastList<L2Npc> npcList = new FastList<>();
		L2Npc victim = null;
		victim = world._freya_guard1;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._freya_guard2;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._freya_guard3;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._freya_guard4;
		if ((victim != null) && !victim.isDead())
		{
			npcList.add(victim);
		}
		victim = world._freya_guard5;
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

	private boolean checkCond(L2PcInstance player)
	{
		if (player.getLevel() < 82)
		{
			return false;
		}

		return true;
	}

	public static void main(String[] args)
	{
		new IceQueenCastle1(-1, qn, "instances");
	}
}
