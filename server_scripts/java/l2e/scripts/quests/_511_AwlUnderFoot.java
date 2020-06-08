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
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.util.Util;

public final class _511_AwlUnderFoot extends Quest
{
	private static final String qn = "_511_AwlUnderFoot";
	
	protected class FAUWorld extends InstanceWorld
	{
	}
	
	public static class FortDungeon
	{
		private final int INSTANCEID;
		private long _reEnterTime = 0;
		
		public FortDungeon(int iId)
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
	
	private final Map<Integer, FortDungeon> _fortDungeons = new HashMap<>(21);
	
	// QUEST ITEMS
	private static final int DL_MARK = 9797;
	
	// REWARDS
	private static final int KNIGHT_EPALUETTE = 9912;
	
	// MONSTER TO KILL -- Only last 3 Raids (lvl ordered) give DL_MARK
	protected static final int[] RAIDS1 =
	{
		25572,
		25575,
		25578
	};
	protected static final int[] RAIDS2 =
	{
		25579,
		25582,
		25585,
		25588
	};
	protected static final int[] RAIDS3 =
	{
		25589,
		25592,
		25593
	};
	
	private static final SkillsHolder RAID_CURSE = new SkillsHolder(5456, 1);
	
	public _511_AwlUnderFoot(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		_fortDungeons.put(35666, new FortDungeon(22));
		_fortDungeons.put(35698, new FortDungeon(23));
		_fortDungeons.put(35735, new FortDungeon(24));
		_fortDungeons.put(35767, new FortDungeon(25));
		_fortDungeons.put(35804, new FortDungeon(26));
		_fortDungeons.put(35835, new FortDungeon(27));
		_fortDungeons.put(35867, new FortDungeon(28));
		_fortDungeons.put(35904, new FortDungeon(29));
		_fortDungeons.put(35936, new FortDungeon(30));
		_fortDungeons.put(35974, new FortDungeon(31));
		_fortDungeons.put(36011, new FortDungeon(32));
		_fortDungeons.put(36043, new FortDungeon(33));
		_fortDungeons.put(36081, new FortDungeon(34));
		_fortDungeons.put(36118, new FortDungeon(35));
		_fortDungeons.put(36149, new FortDungeon(36));
		_fortDungeons.put(36181, new FortDungeon(37));
		_fortDungeons.put(36219, new FortDungeon(38));
		_fortDungeons.put(36257, new FortDungeon(39));
		_fortDungeons.put(36294, new FortDungeon(40));
		_fortDungeons.put(36326, new FortDungeon(41));
		_fortDungeons.put(36364, new FortDungeon(42));
		
		for (int i : _fortDungeons.keySet())
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
		
		for (int i = 25572; i <= 25595; i++)
		{
			addAttackId(i);
		}
	}
	
	private String checkConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if (party == null)
		{
			return "FortressWarden-03.htm";
		}
		if (party.getLeader() != player)
		{
			return showHtmlFile(player, "FortressWarden-04.htm").replace("%leader%", party.getLeader().getName());
		}
		for (L2PcInstance partyMember : party.getMembers())
		{
			QuestState st = partyMember.getQuestState(qn);
			if ((st == null) || (st.getInt("cond") < 1))
			{
				return showHtmlFile(player, "FortressWarden-05.htm").replace("%player%", partyMember.getName());
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				return showHtmlFile(player, "FortressWarden-06.htm").replace("%player%", partyMember.getName());
			}
		}
		return null;
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	protected String enterInstance(L2PcInstance player, String template, int[] coords, FortDungeon dungeon, String ret)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof FAUWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return "";
			}
			teleportPlayer(player, coords, world.instanceId);
			return "";
		}
		// New instance
		if (ret != null)
		{
			return ret;
		}
		ret = checkConditions(player);
		if (ret != null)
		{
			return ret;
		}
		L2Party party = player.getParty();
		int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		Instance ins = InstanceManager.getInstance().getInstance(instanceId);
		ins.setSpawnLoc(new Location(player));
		world = new FAUWorld();
		world.instanceId = instanceId;
		world.templateId = dungeon.getInstanceId();
		world.status = 0;
		dungeon.setReEnterTime(System.currentTimeMillis() + REENTERTIME);
		InstanceManager.getInstance().addWorld(world);
		ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid((FAUWorld) world), RAID_SPAWN_DELAY);
		
		// teleport players
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
		return showHtmlFile(player, "FortressWarden-08.htm").replace("%clan%", player.getClan().getName());
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
				if (_world.status == 0)
				{
					spawnId = RAIDS1[getRandom(RAIDS1.length)];
				}
				else if (_world.status == 1)
				{
					spawnId = RAIDS2[getRandom(RAIDS2.length)];
				}
				else
				{
					spawnId = RAIDS3[getRandom(RAIDS3.length)];
				}
				L2Npc raid = addSpawn(spawnId, 53319, 245814, -6576, 0, false, 0, false, _world.instanceId);
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
	
	private String checkFortCondition(L2PcInstance player, L2Npc npc, boolean isEnter)
	{
		Fort fortress = npc.getFort();
		FortDungeon dungeon = _fortDungeons.get(npc.getId());
		if ((player == null) || (fortress == null) || (dungeon == null))
		{
			return "FortressWarden-01.htm";
		}
		if ((player.getClan() == null) || (player.getClan().getFortId() != fortress.getId()))
		{
			return "FortressWarden-01.htm";
		}
		else if (fortress.getFortState() == 0)
		{
			return "FortressWarden-02a.htm";
		}
		else if (fortress.getFortState() == 2)
		{
			return "FortressWarden-02b.htm";
		}
		else if (isEnter && (dungeon.getReEnterTime() > System.currentTimeMillis()))
		{
			return "FortressWarden-07.htm";
		}
		
		L2Party party = player.getParty();
		if (party == null)
		{
			return "FortressWarden-03.htm";
		}
		for (L2PcInstance partyMember : party.getMembers())
		{
			if ((partyMember.getClan() == null) || (partyMember.getClan().getFortId() == 0) || (partyMember.getClan().getFortId() != fortress.getId()))
			{
				return showHtmlFile(player, "FortressWarden-05.htm").replace("%player%", partyMember.getName());
			}
		}
		return null;
	}
	
	private void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st.getInt("cond") == 1)
		{
			st.giveItems(DL_MARK, 140);
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
			tele[0] = 53322;
			tele[1] = 246380;
			tele[2] = -6580;
			return enterInstance(player, "fortdungeon.xml", tele, _fortDungeons.get(npc.getId()), checkFortCondition(player, npc, true));
		}
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("FortressWarden-10.htm"))
		{
			if (cond == 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("FortressWarden-15.htm"))
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
		String ret = checkFortCondition(player, npc, false);
		if (ret != null)
		{
			return ret;
		}
		else if (st != null)
		{
			int npcId = npc.getId();
			int cond = 0;
			if (st.getState() == State.CREATED)
			{
				st.set("cond", "0");
			}
			else
			{
				cond = st.getInt("cond");
			}
			if (_fortDungeons.containsKey(npcId) && (cond == 0))
			{
				if (player.getLevel() >= 60)
				{
					htmltext = "FortressWarden-09.htm";
				}
				else
				{
					htmltext = "FortressWarden-00.htm";
					st.exitQuest(true);
				}
			}
			else if (_fortDungeons.containsKey(npcId) && (cond > 0) && (st.getState() == State.STARTED))
			{
				long count = st.getQuestItemsCount(DL_MARK);
				if ((cond == 1) && (count > 0))
				{
					htmltext = "FortressWarden-14.htm";
					st.takeItems(DL_MARK, -1);
					st.rewardItems(KNIGHT_EPALUETTE, count);
				}
				else if ((cond == 1) && (count == 0))
				{
					htmltext = "FortressWarden-10.htm";
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
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FAUWorld)
		{
			FAUWorld world = (FAUWorld) tmpworld;
			if (Util.contains(RAIDS3, npc.getId()))
			{
				if (player.getParty() != null)
				{
					for (L2PcInstance pl : player.getParty().getMembers())
					{
						rewardPlayer(pl);
					}
				}
				else
				{
					rewardPlayer(player);
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
		new _511_AwlUnderFoot(511, qn, "");
	}
}