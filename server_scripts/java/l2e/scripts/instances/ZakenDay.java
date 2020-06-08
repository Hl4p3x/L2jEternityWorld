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

import java.util.Calendar;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 04.10.2012 Based on L2J Eternity-World
 */
public class ZakenDay extends Quest
{
	private class ZakenDayWorld extends InstanceWorld
	{
		protected long startTime = 0;
		protected int zakenRoom;
		protected int _candleCount = 0;
		protected final FastList<Candle> _listCandles = new FastList<>();

		public synchronized void addCandleCount(int value)
		{
			_candleCount += value;
		}

		public ZakenDayWorld()
		{
		}
	}

	private boolean _is60 = false;
	private boolean _is83 = false;

	private static final int INSTANCEID_60 = 133;
	private static final int INSTANCEID_83 = 135;

	private static final int ZAKEN_60 = 29176;
	private static final int ZAKEN_83 = 29181;

	private static final int TELEPORTER = 32713;
	private static final int ZAKENS_CANDLE = 32705;
	private static final int DOLL_BLADER_60 = 29023;
	private static final int VALE_MASTER_60 = 29024;
	private static final int PIRATES_ZOMBIE_CAPTAIN_60 = 29026;
	private static final int PIRATES_ZOMBIE_60 = 29027;

	private static final int DOLL_BLADER_83 = 29182;
	private static final int VALE_MASTER_83 = 29183;
	private static final int PIRATES_ZOMBIE_CAPTAIN_83 = 29184;
	private static final int PIRATES_ZOMBIE_83 = 29185;

	private static final int[][] ROOM_SPAWN =
	{
	                // Floor 1
	                {
	                                54240,
	                                220133,
	                                -3498,
	                                1,
	                                3,
	                                4,
	                                6
	                },
	                {
	                                54240,
	                                218073,
	                                -3498,
	                                2,
	                                5,
	                                4,
	                                7
	                },
	                {
	                                55265,
	                                219095,
	                                -3498,
	                                4,
	                                9,
	                                6,
	                                7
	                },
	                {
	                                56289,
	                                220133,
	                                -3498,
	                                8,
	                                11,
	                                6,
	                                9
	                },
	                {
	                                56289,
	                                218073,
	                                -3498,
	                                10,
	                                12,
	                                7,
	                                9
	                },

	                // Floor 2
	                {
	                                54240,
	                                220133,
	                                -3226,
	                                13,
	                                15,
	                                16,
	                                18
	                },
	                {
	                                54240,
	                                218073,
	                                -3226,
	                                14,
	                                17,
	                                16,
	                                19
	                },
	                {
	                                55265,
	                                219095,
	                                -3226,
	                                21,
	                                16,
	                                19,
	                                18
	                },
	                {
	                                56289,
	                                220133,
	                                -3226,
	                                20,
	                                23,
	                                21,
	                                18
	                },
	                {
	                                56289,
	                                218073,
	                                -3226,
	                                22,
	                                24,
	                                19,
	                                21
	                },

	                // Floor 3
	                {
	                                54240,
	                                220133,
	                                -2954,
	                                25,
	                                27,
	                                28,
	                                30
	                },
	                {
	                                54240,
	                                218073,
	                                -2954,
	                                26,
	                                29,
	                                28,
	                                31
	                },
	                {
	                                55265,
	                                219095,
	                                -2954,
	                                33,
	                                28,
	                                31,
	                                30
	                },
	                {
	                                56289,
	                                220133,
	                                -2954,
	                                32,
	                                35,
	                                30,
	                                33
	                },
	                {
	                                56289,
	                                218073,
	                                -2954,
	                                34,
	                                36,
	                                31,
	                                33
	                }
	};

	private static final int[][] CANDLE_SPAWN =
	{
	                // Floor 1
	                {
	                                53313,
	                                220133,
	                                -3498
	                },
	                {
	                                53313,
	                                218079,
	                                -3498
	                },
	                {
	                                54240,
	                                221045,
	                                -3498
	                },
	                {
	                                54325,
	                                219095,
	                                -3498
	                },
	                {
	                                54240,
	                                217155,
	                                -3498
	                },
	                {
	                                55257,
	                                220028,
	                                -3498
	                },
	                {
	                                55257,
	                                218172,
	                                -3498
	                },
	                {
	                                56280,
	                                221045,
	                                -3498
	                },
	                {
	                                56195,
	                                219095,
	                                -3498
	                },
	                {
	                                56280,
	                                217155,
	                                -3498
	                },
	                {
	                                57215,
	                                220133,
	                                -3498
	                },
	                {
	                                57215,
	                                218079,
	                                -3498
	                },

	                // Floor 2
	                {
	                                53313,
	                                220133,
	                                -3226
	                },
	                {
	                                53313,
	                                218079,
	                                -3226
	                },
	                {
	                                54240,
	                                221045,
	                                -3226
	                },
	                {
	                                54325,
	                                219095,
	                                -3226
	                },
	                {
	                                54240,
	                                217155,
	                                -3226
	                },
	                {
	                                55257,
	                                220028,
	                                -3226
	                },
	                {
	                                55257,
	                                218172,
	                                -3226
	                },
	                {
	                                56280,
	                                221045,
	                                -3226
	                },
	                {
	                                56195,
	                                219095,
	                                -3226
	                },
	                {
	                                56280,
	                                217155,
	                                -3226
	                },
	                {
	                                57215,
	                                220133,
	                                -3226
	                },
	                {
	                                57215,
	                                218079,
	                                -3226
	                },

	                // Floor 3
	                {
	                                53313,
	                                220133,
	                                -2954
	                },
	                {
	                                53313,
	                                218079,
	                                -2954
	                },
	                {
	                                54240,
	                                221045,
	                                -2954
	                },
	                {
	                                54325,
	                                219095,
	                                -2954
	                },
	                {
	                                54240,
	                                217155,
	                                -2954
	                },
	                {
	                                55257,
	                                220028,
	                                -2954
	                },
	                {
	                                55257,
	                                218172,
	                                -2954
	                },
	                {
	                                56280,
	                                221045,
	                                -2954
	                },
	                {
	                                56195,
	                                219095,
	                                -2954
	                },
	                {
	                                56280,
	                                217155,
	                                -2954
	                },
	                {
	                                57215,
	                                220133,
	                                -2954
	                },
	                {
	                                57215,
	                                218079,
	                                -2954
	                },
	};

	protected class Candle
	{
		int id;
		L2Npc npcCandle;
		boolean trueCandle;
		boolean fire;
	}

	public ZakenDay(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(TELEPORTER);
		addTalkId(TELEPORTER);
		addFirstTalkId(ZAKENS_CANDLE);
		addKillId(ZAKEN_60);
		addKillId(ZAKEN_83);
	}

	private void CheckCandle(L2PcInstance player, L2Npc can, ZakenDayWorld world)
	{
		for (Candle candlex : world._listCandles)
		{
			if (candlex.npcCandle == can)
			{
				if (candlex.fire)
				{
					return;
				}
				candlex.fire = true;
				if (candlex.trueCandle)
				{
					world.addCandleCount(1);
					startQuestTimer("burn_good_candle", 500, candlex.npcCandle, player);
					return;
				}
				startQuestTimer("burn_bad_candle", 500, candlex.npcCandle, player);
				return;
			}
		}
		return;
	}

	private void teleportPlayer(L2PcInstance player, ZakenDayWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(world.instanceId);
		player.teleToLocation(52680, 219088, -3232);
		if (player.hasSummon())
		{
			player.getSummon().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getSummon().setInstanceId(world.instanceId);
			player.getSummon().teleToLocation(52680, 219088, -3232);
		}
		return;
	}

	private final synchronized void enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if (world != null)
		{
			if (!(world instanceof ZakenDayWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player, (ZakenDayWorld) world);
			return;
		}

		if (!checkConditions(player))
		{
			return;
		}

		L2Party party = player.getParty();
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new ZakenDayWorld();
		if (_is60)
		{
			world.instanceId = instanceId;
			world.templateId = INSTANCEID_60;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);
			((ZakenDayWorld) world).startTime = System.currentTimeMillis();

			if (party.getCommandChannel() == null)
			{
				for (L2PcInstance partyMember : party.getMembers())
				{
					world.allowed.add(partyMember.getObjectId());
					teleportPlayer(partyMember, (ZakenDayWorld) world);
				}
			}
			else
			{
				for (L2PcInstance channelMember : party.getCommandChannel().getMembers())
				{
					world.allowed.add(channelMember.getObjectId());
					teleportPlayer(channelMember, (ZakenDayWorld) world);
				}
			}
		}
		else if (_is83)
		{
			world.instanceId = instanceId;
			world.templateId = INSTANCEID_83;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			if (party.getCommandChannel() == null)
			{
				for (L2PcInstance partyMember : party.getMembers())
				{
					world.allowed.add(partyMember.getObjectId());
					teleportPlayer(partyMember, (ZakenDayWorld) world);
				}
			}
			else
			{
				for (L2PcInstance channelMember : party.getCommandChannel().getMembers())
				{
					world.allowed.add(channelMember.getObjectId());
					teleportPlayer(channelMember, (ZakenDayWorld) world);
				}
			}
		}
		spawnCandles((ZakenDayWorld) world);
		return;
	}

	private boolean checkConditions(L2PcInstance player)
	{
		if (_is60)
		{
			if ((getTimeHour() < 4) || (getTimeHour() > 24))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME));
				return false;
			}
			if (player.getParty() == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
				return false;
			}
			if (player.getParty().getCommandChannel() == null)
			{
				if (player.getParty().getLeader() != player)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
					return false;
				}
				if (player.getParty().getMemberCount() < Config.MIN_ZAKEN_DAY_PLAYERS)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
					return false;
				}
				for (L2PcInstance partyMember : player.getParty().getMembers())
				{
					if ((partyMember.getLevel() > 68) || (partyMember.getLevel() < 52))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(partyMember);
						player.getParty().broadcastPacket(sm);
						return false;
					}
					if (!Util.checkIfInRange(1000, player, partyMember, true))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
						sm.addPcName(partyMember);
						player.getParty().broadcastPacket(sm);
						return false;
					}
					Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID_60);
					if (System.currentTimeMillis() < reentertime)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
						sm.addPcName(partyMember);
						player.getParty().broadcastPacket(sm);
						return false;
					}
				}
			}
			else
			{
				if (player.getParty().getCommandChannel().getLeader() != player)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
					return false;
				}
				else if ((player.getParty().getCommandChannel().getMemberCount() < 9) || (player.getParty().getCommandChannel().getMemberCount() > 27))
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
					return false;
				}

				for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
				{
					if (channelMember.getLevel() > 72)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(channelMember);
						player.getParty().getCommandChannel().broadcastPacket(sm);
						return false;
					}
					if (!Util.checkIfInRange(1000, player, channelMember, true))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
						sm.addPcName(channelMember);
						player.getParty().getCommandChannel().broadcastPacket(sm);
						return false;
					}
					Long reentertime = InstanceManager.getInstance().getInstanceTime(channelMember.getObjectId(), INSTANCEID_60);
					if (System.currentTimeMillis() < reentertime)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
						sm.addPcName(channelMember);
						player.getParty().getCommandChannel().broadcastPacket(sm);
						return false;
					}
				}
			}
		}
		else if (_is83)
		{
			if ((getTimeHour() < 4) || (getTimeHour() > 24))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER));
				return false;
			}
			if (player.getParty() == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
				return false;
			}
			if (player.getParty().getCommandChannel() == null)
			{
				if (player.getParty().getLeader() != player)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
					return false;
				}
				if (player.getParty().getMemberCount() < Config.MIN_ZAKEN_DAY_PLAYERS)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
					return false;
				}
				for (L2PcInstance partyMember : player.getParty().getMembers())
				{
					if (partyMember.getLevel() < 78)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(partyMember);
						player.getParty().broadcastPacket(sm);
						return false;
					}
					if (!Util.checkIfInRange(1000, player, partyMember, true))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
						sm.addPcName(partyMember);
						player.getParty().broadcastPacket(sm);
						return false;
					}
					Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID_83);
					if (System.currentTimeMillis() < reentertime)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
						sm.addPcName(partyMember);
						player.getParty().broadcastPacket(sm);
						return false;
					}
				}
			}
			else
			{
				if (player.getParty().getCommandChannel().getLeader() != player)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
					return false;
				}
				else if ((player.getParty().getCommandChannel().getMemberCount() < 9) || (player.getParty().getCommandChannel().getMemberCount() > 27))
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
					return false;
				}

				for (L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
				{
					if (channelMember.getLevel() < 78)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
						sm.addPcName(channelMember);
						player.getParty().getCommandChannel().broadcastPacket(sm);
						return false;
					}
					if (!Util.checkIfInRange(1000, player, channelMember, true))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
						sm.addPcName(channelMember);
						player.getParty().getCommandChannel().broadcastPacket(sm);
						return false;
					}
					Long reentertime = InstanceManager.getInstance().getInstanceTime(channelMember.getObjectId(), INSTANCEID_83);
					if (System.currentTimeMillis() < reentertime)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
						sm.addPcName(channelMember);
						player.getParty().getCommandChannel().broadcastPacket(sm);
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == TELEPORTER)
		{
			if (event.equalsIgnoreCase("60"))
			{
				_is60 = true;
				_is83 = false;
				enterInstance(player, "ZakenDay.xml");
				return "";
			}
			else if (event.equalsIgnoreCase("83"))
			{
				_is60 = false;
				_is83 = true;
				enterInstance(player, "ZakenDay83.xml");
				return "";
			}
		}

		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof ZakenDayWorld)
		{
			ZakenDayWorld world = (ZakenDayWorld) tmpWorld;
			if (event.startsWith("burn_good_candle"))
			{
				if (npc.getRightHandItem() == 0)
				{
					npc.setRHandId(15280);
					npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, player));
					startQuestTimer("burn_blue_candle", 10000, npc, player);
					if (world._candleCount == 4)
					{
						startQuestTimer("spawn_zaken", 20000, npc, player);
						world._candleCount = 0;
					}
				}
			}
			else if (event.startsWith("burn_bad_candle"))
			{
				if (npc.getRightHandItem() == 0)
				{
					npc.setRHandId(15280);
					npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, player));
					startQuestTimer("burn_red_candle", 10000, npc, player);
				}
			}
			else if (event.startsWith("burn_red_candle"))
			{
				if (npc.getRightHandItem() == 15280)
				{
					npc.setRHandId(15281);
					npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, player));
					int room = getRoomByCandle(npc, world);
					if (_is60)
					{
						spawnInRoom(DOLL_BLADER_60, room, player, world);
						spawnInRoom(VALE_MASTER_60, room, player, world);
						spawnInRoom(PIRATES_ZOMBIE_60, room, player, world);
						spawnInRoom(PIRATES_ZOMBIE_CAPTAIN_60, room, player, world);
					}
					else if (_is83)
					{
						spawnInRoom(DOLL_BLADER_83, room, player, world);
						spawnInRoom(VALE_MASTER_83, room, player, world);
						spawnInRoom(PIRATES_ZOMBIE_83, room, player, world);
						spawnInRoom(PIRATES_ZOMBIE_CAPTAIN_83, room, player, world);
					}
				}
			}
			else if (event.startsWith("burn_blue_candle"))
			{
				if (npc.getRightHandItem() == 15280)
				{
					npc.setRHandId(15302);
					npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, player));
				}
			}
			else if (event.equalsIgnoreCase("spawn_zaken"))
			{
				if (_is60)
				{
					spawnInRoom(ZAKEN_60, world.zakenRoom, player, world);
					spawnInRoom(DOLL_BLADER_60, world.zakenRoom, player, world);
					spawnInRoom(PIRATES_ZOMBIE_60, world.zakenRoom, player, world);
					spawnInRoom(PIRATES_ZOMBIE_CAPTAIN_60, world.zakenRoom, player, world);
					spawnInRoom(VALE_MASTER_60, world.zakenRoom, player, world);
				}
				else if (_is83)
				{
					spawnInRoom(ZAKEN_83, world.zakenRoom, player, world);
					spawnInRoom(DOLL_BLADER_83, world.zakenRoom, player, world);
					spawnInRoom(PIRATES_ZOMBIE_83, world.zakenRoom, player, world);
					spawnInRoom(PIRATES_ZOMBIE_CAPTAIN_83, world.zakenRoom, player, world);
					spawnInRoom(VALE_MASTER_83, world.zakenRoom, player, world);
				}
			}
		}
		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof ZakenDayWorld)
		{
			ZakenDayWorld world = (ZakenDayWorld) tmpWorld;
			long finishDiff = System.currentTimeMillis() - world.startTime;
			int npcId = npc.getId();
			if (npcId == ZAKEN_60)
			{
				finishInstance(world);
				despawnCandles(world);
			}
			else if (npcId == ZAKEN_83)
			{
				finishInstance(world);
				despawnCandles(world);
			}

			if (finishDiff <= 900000)
			{
				if ((npc.getId() == ZAKEN_83) && killer.isInParty() && (killer.getParty().getCommandChannel() != null))
				{
					for (L2PcInstance player : killer.getParty().getCommandChannel().getMembers())
					{
						timebonus(world, npc, player);
					}
				}
				else if ((npc.getId() == ZAKEN_83) && killer.isInParty())
				{
					for (L2PcInstance player : killer.getParty().getMembers())
					{
						timebonus(world, npc, player);
					}
				}
				else if ((npc.getId() == ZAKEN_83) && !killer.isInParty())
				{
					timebonus(world, npc, killer);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (npcId == TELEPORTER)
		{
			enterInstance(player, "ZakenDay.xml");
		}
		return "";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof ZakenDayWorld)
		{
			ZakenDayWorld world = (ZakenDayWorld) tmpWorld;
			if (npc.getId() == ZAKENS_CANDLE)
			{
				CheckCandle(player, npc, world);
			}
		}
		return "";
	}

	private void timebonus(ZakenDayWorld world, L2Npc npc, L2PcInstance player)
	{
		long finishDiff = System.currentTimeMillis() - world.startTime;
		if (player.isInsideRadius(npc, 2000, false, false))
		{
			int rand = getRandom(100);
			if (finishDiff <= 300000)
			{
				if (rand < 50)
				{
					player.addItem("Zaken", 15763, 1, npc, true);
				}
			}
			else if (finishDiff <= 600000)
			{
				if (rand < 30)
				{
					player.addItem("Zaken", 15764, 1, npc, true);
				}
			}
			else if (finishDiff <= 900000)
			{
				if (rand < 25)
				{
					player.addItem("Zaken", 15763, 1, npc, true);
				}
			}
		}
	}

	private void spawnCandles(ZakenDayWorld world)
	{
		for (int i = 0; i < 36; i++)
		{
			Candle candlex = new Candle();
			candlex.id = i + 1;
			candlex.npcCandle = addSpawn(ZAKENS_CANDLE, CANDLE_SPAWN[i][0], CANDLE_SPAWN[i][1], CANDLE_SPAWN[i][2], 0, false, 0, false, world.instanceId);
			candlex.trueCandle = false;
			world._listCandles.add(candlex);
		}
		world.zakenRoom = getRandom(1, 15);
		world._listCandles.get(ROOM_SPAWN[world.zakenRoom - 1][3] - 1).trueCandle = true;
		world._listCandles.get(ROOM_SPAWN[world.zakenRoom - 1][4] - 1).trueCandle = true;
		world._listCandles.get(ROOM_SPAWN[world.zakenRoom - 1][5] - 1).trueCandle = true;
		world._listCandles.get(ROOM_SPAWN[world.zakenRoom - 1][6] - 1).trueCandle = true;
	}

	private void despawnCandles(ZakenDayWorld world)
	{
		for (int i = 0; i < world._listCandles.size(); i++)
		{
			world._listCandles.get(i).npcCandle.decayMe();
		}
		world._listCandles.clear();
	}

	private int getRoomByCandleId(int candleId)
	{
		for (int i = 0; i < 15; i++)
		{
			if ((ROOM_SPAWN[i][3] == candleId) || (ROOM_SPAWN[i][4] == candleId))
			{
				return i + 1;
			}
		}
		if ((candleId == 6) || (candleId == 7))
		{
			return 3;
		}
		if ((candleId == 18) || (candleId == 19))
		{
			return 8;
		}
		if ((candleId == 30) || (candleId == 31))
		{
			return 13;
		}
		return 0;
	}

	private int getRoomByCandle(L2Npc candle, ZakenDayWorld world)
	{
		for (Candle candlex : world._listCandles)
		{
			if (candlex.npcCandle == candle)
			{
				return getRoomByCandleId(candlex.id);
			}
		}
		return 0;
	}

	private void spawnInRoom(int npcId, int roomId, L2PcInstance player, ZakenDayWorld world)
	{
		if ((player != null) && (npcId != ZAKEN_60) && (npcId != ZAKEN_83))
		{
			L2Npc mob = addSpawn(npcId, ROOM_SPAWN[roomId - 1][0] + getRandom(350), ROOM_SPAWN[roomId - 1][1] + getRandom(350), ROOM_SPAWN[roomId - 1][2], 0, false, 0, false, world.instanceId);
			mob.setRunning();
			mob.setTarget(player);
			((L2Attackable) mob).addDamageHate(player, 0, 999);
			mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		else
		{
			addSpawn(npcId, ROOM_SPAWN[roomId - 1][0], ROOM_SPAWN[roomId - 1][1], ROOM_SPAWN[roomId - 1][2], 0, false, 0, false, world.instanceId);
		}
	}

	private void finishInstance(InstanceWorld world)
	{
		if (world instanceof ZakenDayWorld)
		{
			Calendar reenter = Calendar.getInstance();
			reenter.set(Calendar.MINUTE, 30);
			reenter.set(Calendar.HOUR_OF_DAY, 6);

			if (reenter.getTimeInMillis() <= System.currentTimeMillis())
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
			if ((reenter.get(Calendar.DAY_OF_WEEK) <= 2) || (reenter.get(Calendar.DAY_OF_WEEK) > 6))
			{
				while (reenter.get(Calendar.DAY_OF_WEEK) != 2)
				{
					reenter.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
			else if (reenter.get(Calendar.DAY_OF_WEEK) <= 4)
			{
				while (reenter.get(Calendar.DAY_OF_WEEK) != 4)
				{
					reenter.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
			else
			{
				while (reenter.get(Calendar.DAY_OF_WEEK) != 6)
				{
					reenter.add(Calendar.DAY_OF_MONTH, 1);
				}
			}

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
			sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));

			for (int objectId : world.allowed)
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objectId);
				InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, reenter.getTimeInMillis());
				if ((player != null) && player.isOnline())
				{
					player.sendPacket(sm);
				}
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			inst.setDuration(5 * 60000);
			inst.setEmptyDestroyTime(0);
		}
	}

	private int getTimeHour()
	{
		return (GameTimeController.getInstance().getGameTime() / 60) % 24;
	}

	public static void main(String[] args)
	{
		new ZakenDay(-1, ZakenDay.class.getSimpleName(), "instances");
	}
}
