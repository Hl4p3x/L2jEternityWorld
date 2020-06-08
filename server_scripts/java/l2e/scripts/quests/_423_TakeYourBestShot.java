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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 20.05.2011 Based on L2J Eternity-World
 */
public class _423_TakeYourBestShot extends Quest
{
	private static final String qn = "_423_TakeYourBestShot";
	
	// NPC
	private static final int _batracos = 32740;
	private static final int _johnny = 32744;
	
	// Item
	private static final int _seer_ugoros_pass = 15496;
	
	// Spawn chance x/1000
	private static final int _spawn_chance = 2;
	
	// Guard
	private static final int _tanta_guard = 18862;
	
	// Mobs
	private static final int[] _mobs =
	{
		22768,
		22769,
		22770,
		22771,
		22772,
		22773,
		22774
	};
	
	public _423_TakeYourBestShot(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_johnny);
		addTalkId(_johnny);
		addStartNpc(_batracos);
		addTalkId(_batracos);
		addFirstTalkId(_batracos);
		
		addKillId(_tanta_guard);
		for (int _mob : _mobs)
		{
			addKillId(_mob);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == _johnny)
		{
			if (event.equalsIgnoreCase("32744-04.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32744-quit.htm"))
			{
				st.exitQuest(true);
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
		
		if (npc.getId() == _johnny)
		{
			switch (st.getState())
			{
				case State.CREATED:
					QuestState _prev = player.getQuestState("_249_PoisonedPlainsOfTheLizardmen");
					if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 82))
					{
						if (st.hasQuestItems(_seer_ugoros_pass))
						{
							htmltext = "32744-07.htm";
						}
						else
						{
							htmltext = "32744-01.htm";
						}
					}
					else
					{
						htmltext = "32744-00.htm";
					}
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
					{
						htmltext = "32744-05.htm";
					}
					else if (st.getInt("cond") == 2)
					{
						htmltext = "32744-06.htm";
					}
					break;
			}
		}
		else if (npc.getId() == _batracos)
		{
			if (st.getState() == State.CREATED)
			{
				if (st.hasQuestItems(_seer_ugoros_pass))
				{
					htmltext = "32740-05.htm";
				}
				else
				{
					htmltext = "32740-00.htm";
				}
			}
			else if ((st.getState() == State.STARTED) && (st.getInt("cond") == 1))
			{
				htmltext = "32740-02.htm";
			}
			else if ((st.getState() == State.STARTED) && (st.getInt("cond") == 2))
			{
				st.giveItems(_seer_ugoros_pass, 1);
				st.playSound("ItemSound.quest_finish");
				st.unset("cond");
				st.exitQuest(true);
				htmltext = "32740-04.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		
		if (npc.isInsideRadius(96782, 85918, 100, true))
		{
			return "32740-ugoros.htm";
		}
		
		return "32740.htm";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if (Util.contains(_mobs, npc.getId()) && (getRandom(1000) <= _spawn_chance))
		{
			L2Npc guard = addSpawn(_tanta_guard, npc, false);
			attackPlayer((L2Attackable) guard, player);
		}
		else if ((npc.getId() == _tanta_guard) && (st.getInt("cond") == 1))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		return null;
	}
	
	private void attackPlayer(L2Attackable npc, L2PcInstance player)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(player, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
	}
	
	public static void main(String[] args)
	{
		new _423_TakeYourBestShot(423, qn, "");
	}
}
