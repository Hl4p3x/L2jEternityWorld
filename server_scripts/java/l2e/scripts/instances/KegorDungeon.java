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

import java.util.List;

import javolution.util.FastList;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class KegorDungeon extends Quest
{
	private class KegorWorld extends InstanceWorld
	{
		public long[] storeTime =
		{
		                0,
		                0
		};
		public boolean underAttack = false;
		public L2Npc KEGOR = null;
		public List<L2Attackable> liveMobs;

		public KegorWorld()
		{
		}
	}

	private static final String qn = "KegorDungeon";
	private static final int INSTANCEID = 138;

	private static final int KROON = 32653;
	private static final int TAROON = 32654;
	private static final int KEGOR_IN_CAVE = 18846;
	private static final int MONSTER = 22766;

	private static final int ANTIDOTE = 15514;

	private static final int BUFF = 6286;

	private static final int[][] MOB_SPAWNS =
	{
	                {
	                                185216,
	                                -184112,
	                                -3308,
	                                -15396
	                },
	                {
	                                185456,
	                                -184240,
	                                -3308,
	                                -19668
	                },
	                {
	                                185712,
	                                -184384,
	                                -3308,
	                                -26696
	                },
	                {
	                                185920,
	                                -184544,
	                                -3308,
	                                -32544
	                },
	                {
	                                185664,
	                                -184720,
	                                -3308,
	                                27892
	                },
	};

	private static final int[] ENTRY_POINT =
	{
	                186852,
	                -173492,
	                -3763
	};

	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}

	public KegorDungeon(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(KEGOR_IN_CAVE);
		addStartNpc(KROON);
		addStartNpc(TAROON);
		addTalkId(KROON);
		addTalkId(TAROON);
		addTalkId(KEGOR_IN_CAVE);

		addKillId(KEGOR_IN_CAVE);
		addKillId(MONSTER);

		addSpawnId(KEGOR_IN_CAVE);
		addSpawnId(MONSTER);

	}

	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}

	private boolean checkConditions(L2PcInstance player)
	{
		if ((player.getLevel() < 82) || (player.getLevel() > 85))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}

		return true;
	}

	protected void exitInstance(L2PcInstance player, teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
	}

	protected int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof KegorWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}

		if (!checkConditions(player))
		{
			return 0;
		}

		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
		inst.setSpawnsLoc(new int[]
		{
		                player.getX(),
		                player.getY(),
		                player.getZ()
		});
		world = new KegorWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;

		((KegorWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		teleto.instanceId = instanceId;
		teleportplayer(player, teleto);
		world.allowed.add(player.getObjectId());
		return instanceId;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == KEGOR_IN_CAVE)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if ((tmpworld != null) && (tmpworld instanceof KegorWorld))
			{
				KegorWorld world = (KegorWorld) tmpworld;

				if (event.equalsIgnoreCase("spawn"))
				{
					world.liveMobs = new FastList<>();
					for (int[] spawn : MOB_SPAWNS)
					{
						L2Attackable spawnedMob = (L2Attackable) addSpawn(MONSTER, spawn[0], spawn[1], spawn[2], spawn[3], false, 0, false, world.instanceId);
						world.liveMobs.add(spawnedMob);
					}
				}

				else if (event.equalsIgnoreCase("buff"))
				{
					if ((world.liveMobs != null) && !world.liveMobs.isEmpty())
					{
						for (L2Attackable monster : world.liveMobs)
						{
							if (monster.getKnownList().knowsObject(npc))
							{
								monster.addDamageHate(npc, 0, 999);
								monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc, null);
							}
							else
							{
								monster.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX(), npc.getY(), npc.getZ(), 0));
							}
						}

						if (npc.getKnownList().getKnownPlayers().size() == 1)
						{
							L2Skill buff = SkillHolder.getInstance().getInfo(BUFF, 1);
							if (buff != null)
							{
								for (L2PcInstance pl : npc.getKnownList().getKnownPlayers().values())
								{
									if (Util.checkIfInRange(buff.getCastRange(), npc, pl, false))
									{
										npc.setTarget(pl);
										npc.doCast(buff);
									}
								}
							}
						}
						startQuestTimer("buff", 30000, npc, player);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		String htmltext = getNoQuestMsg(player);

		QuestState hostQuest = player.getQuestState("_10284_AcquisitionOfDivineSword");

		if (hostQuest == null)
		{
			System.out.println("null host quest");
			return htmltext;
		}

		if ((npcId == KROON) || (npcId == TAROON))
		{
			teleCoord tele = new teleCoord();
			tele.x = ENTRY_POINT[0];
			tele.y = ENTRY_POINT[1];
			tele.z = ENTRY_POINT[2];

			htmltext = npcId == KROON ? "32653-07.htm" : "32654-07.htm";
			if (enterInstance(player, "Kegor.xml", tele) > 0)
			{
				htmltext = "";
				if ((hostQuest.getInt("progress") == 2) && (hostQuest.getQuestItemsCount(ANTIDOTE) == 0))
				{
					hostQuest.giveItems(ANTIDOTE, 1);
					hostQuest.playSound("ItemSound.quest_middle");
					hostQuest.set("cond", "4");
				}
			}
		}

		else if (npc.getId() == KEGOR_IN_CAVE)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if ((tmpworld != null) && (tmpworld instanceof KegorWorld))
			{
				KegorWorld world = (KegorWorld) tmpworld;
				if ((hostQuest.getInt("progress") == 2) && (hostQuest.getQuestItemsCount(ANTIDOTE) > 0) && !world.underAttack)
				{
					hostQuest.takeItems(ANTIDOTE, hostQuest.getQuestItemsCount(ANTIDOTE));
					hostQuest.playSound("ItemSound.quest_middle");
					hostQuest.set("cond", "5");
					htmltext = "18846-01.htm";
					world.underAttack = true;
					npc.setIsInvul(false);
					npc.setIsMortal(true);
					startQuestTimer("spawn", 3000, npc, player);
					startQuestTimer("buff", 3500, npc, player);
				}

				else if (hostQuest.getState() == State.COMPLETED)
				{
					world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
					final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
					teleCoord tele = new teleCoord();
					tele.instanceId = 0;
					tele.x = inst.getSpawnsLoc()[0];
					tele.y = inst.getSpawnsLoc()[1];
					tele.z = inst.getSpawnsLoc()[2];
					exitInstance(player, tele);
					htmltext = "";
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState hostQuest = player.getQuestState("_10284_AcquisitionOfDivineSword");
		if (hostQuest == null)
		{
			return null;
		}

		if (npc.getId() == KEGOR_IN_CAVE)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if ((tmpworld != null) && (tmpworld instanceof KegorWorld))
			{
				KegorWorld world = (KegorWorld) tmpworld;

				if (world.KEGOR == null)
				{
					world.KEGOR = npc;
				}

				if (hostQuest.getState() != State.STARTED)
				{
					return "18846-04.htm";
				}

				if (!world.underAttack && (hostQuest.getInt("progress") == 2))
				{
					return "18846-00.htm";
				}
				else if (hostQuest.getInt("progress") == 3)
				{
					hostQuest.giveItems(57, 296425);
					hostQuest.addExpAndSp(921805, 82230);
					hostQuest.playSound("ItemSound.quest_finish");
					hostQuest.exitQuest(false);
					return "18846-03.htm";
				}
				else
				{
					return "18846-02.htm";
				}
			}
		}
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState hostQuest = player.getQuestState("_10284_AcquisitionOfDivineSword");
		if ((hostQuest == null) || (hostQuest.getState() != State.STARTED))
		{
			return null;
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if ((tmpworld != null) && (tmpworld instanceof KegorWorld))
		{
			KegorWorld world = (KegorWorld) tmpworld;

			if (npc.getId() == MONSTER)
			{
				if (world.liveMobs != null)
				{
					world.liveMobs.remove(npc);
					if (world.liveMobs.isEmpty() && (world.KEGOR != null) && !world.KEGOR.isDead() && (hostQuest.getInt("progress") == 2))
					{
						world.underAttack = false;
						world.liveMobs = null;
						cancelQuestTimer("buff", world.KEGOR, null);
						world.KEGOR.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player, null);
						NpcSay cs = new NpcSay(world.KEGOR.getObjectId(), Say2.ALL, world.KEGOR.getId(), NpcStringId.I_CAN_FINALLY_TAKE_A_BREATHER_BY_THE_WAY_WHO_ARE_YOU_HMM_I_THINK_I_KNOW_WHO_SENT_YOU);
						world.KEGOR.broadcastPacket(cs);
						hostQuest.set("progress", "3");
						hostQuest.set("cond", "6");
						hostQuest.playSound("ItemSound.quest_middle");

						Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
						inst.setDuration(3 * 60000);
						inst.setEmptyDestroyTime(0);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onKillByMob(L2Npc npc, L2Npc killer)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if ((tmpworld != null) && (tmpworld instanceof KegorWorld))
		{
			KegorWorld world = (KegorWorld) tmpworld;

			if (npc.getId() == KEGOR_IN_CAVE)
			{
				world.KEGOR = null;
				NpcSay cs = new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.HOW_COULD_I_FALL_IN_A_PLACE_LIKE_THIS);
				npc.broadcastPacket(cs);

				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				inst.setDuration(60000);
				inst.setEmptyDestroyTime(0);
			}
		}
		return null;
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new KegorDungeon(-1, qn, "instances");
	}
}
