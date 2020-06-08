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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2RaidBossInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

/**
 * Based on L2J Eternity-World
 */
public final class _512_BladeUnderFoot extends Quest
{
	private static final String qn = "_512_BladeUnderFoot";

	protected class FAUWorld extends InstanceWorld
	{
	}
	
	private static class CastleDungeon
	{
		private final int INSTANCEID;
		private long _reEnterTime = 0;
		
		public CastleDungeon(int iId)
		{
			INSTANCEID = iId;
		}
		
		public int getInstanceId()
		{
			return INSTANCEID;
		}
		
		public long getReEnterTime()
		{
			return _reEnterTime;
		}
		
		public void setReEnterTime(long time)
		{
			_reEnterTime = time;
		}
	}
	
	private static final long REENTERTIME = 14400000;
	private static final long RAID_SPAWN_DELAY = 120000;
	
	private final Map<Integer, CastleDungeon> _castleDungeons = new HashMap<>(9);
	
	// QUEST ITEMS
	private static final int DL_MARK = 9798;
	
	// REWARDS
	private static final int KNIGHT_EPALUETTE = 9912;
	
	// MONSTER TO KILL -- Only last 3 Raids (lvl ordered) give DL_MARK
	protected static final int[] RAIDS1 =
	{
		25546,
		25549,
		25552
	};
	protected static final int[] RAIDS2 =
	{
		25553,
		25554,
		25557,
		25560
	};
	protected static final int[] RAIDS3 =
	{
		25563,
		25566,
		25569
	};
	
	private static final SkillsHolder RAID_CURSE = new SkillsHolder(5456, 1);
	
	public _512_BladeUnderFoot(int questId, String name, String descr)
	{
		super(questId, name, descr);

		_castleDungeons.put(36403, new CastleDungeon(13));
		_castleDungeons.put(36404, new CastleDungeon(14));
		_castleDungeons.put(36405, new CastleDungeon(15));
		_castleDungeons.put(36406, new CastleDungeon(16));
		_castleDungeons.put(36407, new CastleDungeon(17));
		_castleDungeons.put(36408, new CastleDungeon(18));
		_castleDungeons.put(36409, new CastleDungeon(19));
		_castleDungeons.put(36410, new CastleDungeon(20));
		_castleDungeons.put(36411, new CastleDungeon(21));
		
		for (int i : _castleDungeons.keySet())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		
		for (int i : RAIDS1)
		{
			addKillId(i);
		}
		for (int i : RAIDS2)
		{
			addKillId(i);
		}
		for (int i : RAIDS3)
		{
			addKillId(i);
		}
		
		for (int i = 25546; i <= 25571; i++)
		{
			addAttackId(i);
		}
	}
	
	private String checkConditions(L2PcInstance player)
	{		
		final L2Party party = player.getParty();
		if (party == null)
		{
			return "CastleWarden-03.htm";
		}
		
		if (party.getLeader() != player)
		{
			return showHtmlFile(player, "CastleWarden-04.htm").replace("%leader%", party.getLeader().getName());
		}
		
		for (L2PcInstance partyMember : party.getMembers())
		{
			final QuestState st = partyMember.getQuestState(qn);
			if ((st == null) || (st.getInt("cond") < 1))
			{
				return showHtmlFile(player, "CastleWarden-05.htm").replace("%player%", partyMember.getName());
			}
			
			if (partyMember.getLevel() < 70)
			{
				player.sendMessage(partyMember.getName() + "s level requirement is not sufficient and cannot be entered.");
				return null;
			}
			
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				return showHtmlFile(player, "CastleWarden-06.htm").replace("%player%", partyMember.getName());
			}
		}
		return null;
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	protected String enterInstance(L2PcInstance player, String template, int[] coords, CastleDungeon dungeon, String ret)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof FAUWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return "";
			}
			teleportPlayer(player, coords, world.instanceId);
			return "";
		}
		
		if (ret != null)
		{
			return ret;
		}
		ret = checkConditions(player);
		if (ret != null)
		{
			return ret;
		}
		
		final L2Party party = player.getParty();
		final int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		Instance ins = InstanceManager.getInstance().getInstance(instanceId);
		ins.setSpawnLoc(new Location(player));
		world = new FAUWorld();
		world.instanceId = instanceId;
		world.templateId = dungeon.getInstanceId();
		world.status = 0;
		dungeon.setReEnterTime(System.currentTimeMillis() + REENTERTIME);
		InstanceManager.getInstance().addWorld(world);
		ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid((FAUWorld) world), RAID_SPAWN_DELAY);
		
		if (player.getParty() == null)
		{
			teleportPlayer(player, coords, instanceId);
			world.allowed.add(player.getObjectId());
		}
		else
		{
			for (L2PcInstance partyMember : party.getMembers())
			{
				teleportPlayer(partyMember, coords, instanceId);
				world.allowed.add(partyMember.getObjectId());
				if (partyMember.getQuestState(qn) == null)
				{
					newQuestState(partyMember);
				}
			}
		}
		return showHtmlFile(player, "CastleWarden-08.htm").replace("%clan%", player.getClan().getName());
	}
	
	private class spawnRaid implements Runnable
	{
		private final FAUWorld _world;
		
		public spawnRaid(FAUWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			try
			{
				int spawnId;
				switch (_world.status)
				{
					case 0:
						spawnId = RAIDS1[getRandom(RAIDS1.length)];
						break;
					case 1:
						spawnId = RAIDS2[getRandom(RAIDS2.length)];
						break;
					default:
						spawnId = RAIDS3[getRandom(RAIDS3.length)];
						break;
				}
				
				final L2Npc raid = addSpawn(spawnId, 12161, -49144, -3000, 0, false, 0, false, _world.instanceId);
				if (raid instanceof L2RaidBossInstance)
				{
					((L2RaidBossInstance) raid).setUseRaidCurse(false);
				}
			}
			catch (Exception e)
			{
			}
		}
	}
	
	private String checkCastleCondition(L2PcInstance player, L2Npc npc, boolean isEnter)
	{
		final Castle castle = npc.getCastle();
		final CastleDungeon dungeon = _castleDungeons.get(npc.getId());
		if ((player == null) || (castle == null) || (dungeon == null))
		{
			return "CastleWarden-01.htm";
		}
		if ((player.getClan() == null) || (player.getClan().getCastleId() != castle.getId()))
		{
			return "CastleWarden-01.htm";
		}
		if (isEnter && (dungeon.getReEnterTime() > System.currentTimeMillis()))
		{
			return "CastleWarden-07.htm";
		}
		
		return null;
	}
	
	private void rewardPlayer(L2PcInstance player, int partySize)
	{
		final QuestState st = player.getQuestState(qn);
		if ((st != null) && (st.getInt("cond") == 1))
		{
			int itemsN = 140;
			switch (partySize)
			{
				case 1:
					itemsN = 420;
					break;
				case 2:
					itemsN = 420;
					break;
				case 3:
					itemsN = 420;
					break;
				case 4:
					itemsN = 336;
					break;
				case 5:
					itemsN = 252;
					break;
				case 6:
					itemsN = 224;
					break;
				case 7:
					itemsN = 196;
					break;
				case 8:
					itemsN = 168;
					break;
				case 9:
					itemsN = 140;
					break;
			}
			
			st.giveItems(DL_MARK, itemsN);
			st.playSound("ItemSound.quest_itemget");
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("enter"))
		{
			int[] tele = new int[3];
			tele[0] = 11740;
			tele[1] = -49148;
			tele[2] = -3000;
			return enterInstance(player, "RimPailakaCastle.xml", tele, _castleDungeons.get(npc.getId()), checkCastleCondition(player, npc, true));
		}
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (event.equalsIgnoreCase("CastleWarden-10.htm"))
		{
			if (st.getInt("cond") == 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("CastleWarden-15.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		final String ret = checkCastleCondition(player, npc, false);
		if (ret != null)
		{
			return ret;
		}
		else if (st != null)
		{
			final int npcId = npc.getId();
			int cond = 0;
			if (st.getState() == State.CREATED)
			{
				st.set("cond", "0");
			}
			else
			{
				cond = st.getInt("cond");
			}
		
			if (_castleDungeons.containsKey(npcId) && (cond == 0))
			{
				if (player.getLevel() >= 70)
				{
					htmltext = "CastleWarden-09.htm";
				}
				else
				{
					htmltext = "CastleWarden-00.htm";
					st.exitQuest(true);
				}
			}
			else if (_castleDungeons.containsKey(npcId) && (cond > 0) && (st.getState() == State.STARTED))
			{
				final long count = st.getQuestItemsCount(DL_MARK);
				if (cond == 1)
				{
					if (count > 0)
					{
						htmltext = "CastleWarden-14.htm";
						st.takeItems(DL_MARK, count);
						st.rewardItems(KNIGHT_EPALUETTE, count);
					}
					else if (count == 0)
					{
						htmltext = "CastleWarden-10.htm";
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		L2Playable attacker = (isSummon ? player.getSummon() : player);
		if ((attacker.getLevel() - npc.getLevel()) >= 9)
		{
			if ((attacker.getBuffCount() > 0) || (attacker.getDanceCount() > 0))
			{
				npc.setTarget(attacker);
				npc.doSimultaneousCast(RAID_CURSE.getSkill());
			}
			else if (player.getParty() != null)
			{
				for (L2PcInstance pmember : player.getParty().getMembers())
				{
					if ((pmember.getBuffCount() > 0) || (pmember.getDanceCount() > 0))
					{
						npc.setTarget(pmember);
						npc.doSimultaneousCast(RAID_CURSE.getSkill());
					}
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FAUWorld)
		{
			FAUWorld world = (FAUWorld) tmpworld;
			if (Util.contains(RAIDS3, npc.getId()))
			{
				final int allowedN = world.allowed.size();
				
				if (player.getParty() != null)
				{
					for (L2PcInstance pl : player.getParty().getMembers())
					{
						rewardPlayer(pl, allowedN);
					}
				}
				else
				{
					rewardPlayer(player, allowedN);
				}
				
				Instance instanceObj = InstanceManager.getInstance().getInstance(world.instanceId);
				instanceObj.setDuration(360000);
				instanceObj.removeNpcs();
			}
			else
			{
				world.status++;
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid(world), RAID_SPAWN_DELAY);
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _512_BladeUnderFoot(512, qn, "");
	}
}