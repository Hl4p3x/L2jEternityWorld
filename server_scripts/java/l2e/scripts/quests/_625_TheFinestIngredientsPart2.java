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

import java.util.logging.Level;

import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2RaidBossInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 13.01.2013 Based on L2J Eternity-World
 */
public class _625_TheFinestIngredientsPart2 extends Quest
{
	private static final String qn = "_625_TheFinestIngredientsPart2";
	
	private static final int ICICLE_EMPEROR_BUMBALUMP = 25296;
	
	private static final int JEREMY = 31521;
	private static final int YETIS_TABLE = 31542;
	
	private static final int SOY_SAUCE_JAR = 7205;
	private static final int FOOD_FOR_BUMBALUMP = 7209;
	private static final int SPECIAL_YETI_MEAT = 7210;
	private static final int REWARD_DYE[] =
	{
		4589,
		4590,
		4591,
		4592,
		4593,
		4594
	};
	
	private static final int CHECK_INTERVAL = 600000;
	private static final int IDLE_INTERVAL = 3;
	private static L2Npc _npc = null;
	private static int _status = -1;
	
	public _625_TheFinestIngredientsPart2(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]
		{
			FOOD_FOR_BUMBALUMP,
			SPECIAL_YETI_MEAT
		};
		
		addStartNpc(JEREMY);
		addTalkId(JEREMY, YETIS_TABLE);
		
		addAttackId(ICICLE_EMPEROR_BUMBALUMP);
		addKillId(ICICLE_EMPEROR_BUMBALUMP);
		
		switch (RaidBossSpawnManager.getInstance().getRaidBossStatusId(ICICLE_EMPEROR_BUMBALUMP))
		{
			case UNDEFINED:
				_log.log(Level.WARNING, qn + ": can not find spawned L2RaidBoss id=" + ICICLE_EMPEROR_BUMBALUMP);
				break;
			
			case ALIVE:
				spawnNpc();
			case DEAD:
				startQuestTimer("check", CHECK_INTERVAL, null, null, true);
				break;
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("check"))
		{
			L2RaidBossInstance raid = RaidBossSpawnManager.getInstance().getBosses().get(ICICLE_EMPEROR_BUMBALUMP);
			if ((raid != null) && (raid.getRaidStatus() == StatusEnum.ALIVE))
			{
				if ((_status >= 0) && (_status-- == 0))
				{
					despawnRaid(raid);
				}
				
				spawnNpc();
			}
			
			return null;
		}
		
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("31521-03.htm"))
		{
			if (st.hasQuestItems(SOY_SAUCE_JAR))
			{
				st.set("cond", "1");
				st.takeItems(SOY_SAUCE_JAR, 1);
				st.giveItems(FOOD_FOR_BUMBALUMP, 1);
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "31521-04.htm";
			}
		}
		else if (event.equalsIgnoreCase("31521-08.htm"))
		{
			if (st.hasQuestItems(SPECIAL_YETI_MEAT))
			{
				st.takeItems(SPECIAL_YETI_MEAT, 1);
				st.rewardItems(REWARD_DYE[getRandom(REWARD_DYE.length)], 5);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31521-09.htm";
			}
		}
		else if (event.equalsIgnoreCase("31542-02.htm"))
		{
			if (st.hasQuestItems(FOOD_FOR_BUMBALUMP))
			{
				if (_status < 0)
				{
					if (spawnRaid())
					{
						st.takeItems(FOOD_FOR_BUMBALUMP, 1);
						st.set("cond", "2");
						st.playSound("ItemSound.quest_middle");
					}
				}
				else
				{
					htmltext = "31542-04.htm";
				}
			}
			else
			{
				htmltext = "31542-03.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() < 73)
				{
					htmltext = "31521-02.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "31521-01.htm";
				}
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getId())
				{
					case JEREMY:
						if (cond == 1)
						{
							htmltext = "31521-05.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31521-06.htm";
						}
						else
						{
							htmltext = "31521-07.htm";
						}
						break;
					
					case YETIS_TABLE:
						if (cond == 1)
						{
							htmltext = "31542-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31542-05.htm";
						}
						break;
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		_status = IDLE_INTERVAL;
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		for (L2PcInstance partyMember : getPartyMembers(player, npc, "cond", "2"))
		{
			QuestState st = partyMember.getQuestState(qn);
			st.giveItems(SPECIAL_YETI_MEAT, 1);
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		
		despawnRaid(npc);
		
		if (_npc != null)
		{
			_npc.deleteMe();
			_npc = null;
		}
		return null;
	}
	
	private void spawnNpc()
	{
		if (_npc == null)
		{
			_npc = addSpawn(YETIS_TABLE, 157136, -121456, -2363, 40000, false, 0, false);
		}
	}
	
	private static boolean spawnRaid()
	{
		L2RaidBossInstance raid = RaidBossSpawnManager.getInstance().getBosses().get(ICICLE_EMPEROR_BUMBALUMP);
		if (raid.getRaidStatus() == StatusEnum.ALIVE)
		{
			raid.getSpawn().setX(157117);
			raid.getSpawn().setY(-121939);
			raid.getSpawn().setZ(-2397);
			raid.teleToLocation(157117, -121939, -2397, 100);
			raid.broadcastNpcSay("Hmmm, what do I smell over here?");
			_status = IDLE_INTERVAL;
			
			return true;
		}
		return false;
	}
	
	private static void despawnRaid(L2Npc raid)
	{
		raid.getSpawn().setX(-104700);
		raid.getSpawn().setY(-252700);
		raid.getSpawn().setZ(-15542);
		
		if (!raid.isDead())
		{
			raid.teleToLocation(-104700, -252700, -15542, 0);
		}
		
		_status = -1;
	}
	
	public static void main(String[] args)
	{
		new _625_TheFinestIngredientsPart2(625, qn, "");
	}
}