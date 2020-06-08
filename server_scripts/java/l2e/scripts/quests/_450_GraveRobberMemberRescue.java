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

import java.util.Calendar;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class _450_GraveRobberMemberRescue extends Quest
{
	private static final String qn = "_450_GraveRobberMemberRescue";
	
	private static final int KANEMIKA = 32650;
	private static final int WARRIOR_NPC = 32651;
	
	private static final int WARRIOR_MON = 22741;
	
	private static final int EVIDENCE_OF_MIGRATION = 14876;
	
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	
	public _450_GraveRobberMemberRescue(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(KANEMIKA);
		addTalkId(KANEMIKA);
		addTalkId(WARRIOR_NPC);
		
		questItemIds = new int[]
		{
			EVIDENCE_OF_MIGRATION
		};
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
		
		if (event.equalsIgnoreCase("32650-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
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
		
		int cond = st.getInt("cond");
		
		if (npc.getId() == KANEMIKA)
		{
			if (cond == 0)
			{
				String reset = st.get("reset");
				long remain = 0;
				if ((reset != null) && isDigit(reset))
				{
					remain = Long.parseLong(reset) - System.currentTimeMillis();
				}
				if (remain <= 0)
				{
					if (player.getLevel() >= 80)
					{
						htmltext = "32650-01.htm";
					}
					else
					{
						htmltext = "32650-00.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "32650-09.htm";
				}
			}
			else if (cond == 1)
			{
				if (st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) >= 1)
				{
					htmltext = "32650-07.htm";
				}
				else
				{
					htmltext = "32650-06.htm";
				}
			}
			else if ((cond == 2) && (st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) == 10))
			{
				htmltext = "32650-08.htm";
				st.giveItems(57, 65000);
				st.takeItems(EVIDENCE_OF_MIGRATION, 10);
				st.setState(State.COMPLETED);
				st.unset("cond");
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
				Calendar reset = Calendar.getInstance();
				reset.set(Calendar.MINUTE, RESET_MIN);
				// if time is >= RESET_HOUR - roll to the next day
				if (reset.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
				{
					reset.add(Calendar.DATE, 1);
				}
				reset.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
				st.set("reset", String.valueOf(reset.getTimeInMillis()));
			}
		}
		else if ((cond == 1) && (npc.getId() == WARRIOR_NPC))
		{
			if (getRandom(100) < 50)
			{
				htmltext = "32651-01.htm";
				st.giveItems(EVIDENCE_OF_MIGRATION, 1);
				st.playSound("ItemSound.quest_itemget");
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0));
				npc.getSpawn().decreaseCount(npc);
				npc.deleteMe();
				if (st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) == 10)
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
			else
			{
				htmltext = "";
				L2Npc warrior = st.addSpawn(WARRIOR_MON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 600000);
				warrior.setRunning();
				((L2Attackable) warrior).addDamageHate(player, 0, 999);
				warrior.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				showOnScreenMsg(player, NpcStringId.THE_GRAVE_ROBBER_WARRIOR_HAS_BEEN_FILLED_WITH_DARK_ENERGY_AND_IS_ATTACKING_YOU, 5, 5000);
				if (getRandom(100) < 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.GRUNT_OH));
				}
				else
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.GRUNT_WHATS_WRONG_WITH_ME));
				}
				npc.getSpawn().decreaseCount(npc);
				npc.deleteMe();
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _450_GraveRobberMemberRescue(450, qn, "");
	}
}