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

import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public final class _114_ResurrectionOfAnOldManager extends Quest
{
	private static final String qn = "_114_ResurrectionOfAnOldManager";

	// NPC
	private static final int NEWYEAR = 31961;
	private static final int YUMI = 32041;
	private static final int STONES = 32046;
	private static final int WENDY = 32047;
	private static final int BOX = 32050;

	// MOBS
	private static final int GUARDIAN = 27318;

	// QUEST ITEMS
	private static final int DETECTOR = 8090;
	private static final int DETECTOR2 = 8091;
	private static final int STARSTONE = 8287;
	private static final int LETTER = 8288;
	private static final int STARSTONE2 = 8289;	
	private L2PcInstance GUARDIAN_SPAWN = null;
	
	public _114_ResurrectionOfAnOldManager(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(YUMI);
		addTalkId(YUMI);
		addTalkId(WENDY);
		addTalkId(BOX);
		addTalkId(STONES);
		addTalkId(NEWYEAR);
		addFirstTalkId(STONES);

		addKillId(GUARDIAN);

		questItemIds = new int[] { DETECTOR, DETECTOR2, STARSTONE, LETTER, STARSTONE2 };
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;				
	
		if(event.equalsIgnoreCase("31961-02.htm"))
		{
			st.set("cond", "22");
			st.takeItems(LETTER, 1);
			st.giveItems(STARSTONE2, 1);
			st.playSound("ItemSound.quest_middle");
		}
		if(event.equalsIgnoreCase("32041-02.htm"))
		{
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.set("cond", "1");
			st.set("talk", "0");
		}
		else if(event.equalsIgnoreCase("32041-06.htm"))
		{
			st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("32041-07.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
		}
		else if(event.equalsIgnoreCase("32041-10.htm"))
		{
			int choice = st.getInt("choice");
			if(choice == 1)
				htmltext = "32041-10.htm";
			else if(choice == 2)
				htmltext = "32041-10a.htm";
			else if(choice == 3)
				htmltext = "32041-10b.htm";
		}
		else if(event.equalsIgnoreCase("32041-11.htm"))
		{
			st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("32041-18.htm"))
		{
			st.set("talk", "2");
		}
		else if(event.equalsIgnoreCase("32041-20.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
		}
		else if(event.equalsIgnoreCase("32041-25.htm"))
		{
			st.set("cond", "17");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(DETECTOR, 1);
		}
		else if(event.equalsIgnoreCase("32041-28.htm"))
		{
			st.takeItems(DETECTOR2, 1);
			st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("32041-31.htm"))
		{
			int choice = st.getInt("choice");
			if(choice > 1)
				htmltext = "32041-37.htm";
		}
		else if(event.equalsIgnoreCase("32041-32.htm"))
		{
			st.set("cond", "21");
			st.giveItems(LETTER, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32041-36.htm"))
		{
			st.set("cond", "20");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32046-02.htm"))
		{
			st.set("cond", "19");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32046-06.htm"))
		{
			st.addExpAndSp(1846611, 144270);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		else if(event.equalsIgnoreCase("32047-01.htm"))
		{
			if(st.getInt("talk") + st.getInt("talk1") == 2)
				htmltext = "32047-04.htm";
			else if(st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 6)
				htmltext = "32047-08.htm";
		}
		else if(event.equalsIgnoreCase("32047-02.htm"))
		{
			if(st.getInt("talk") == 0)
				st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("32047-03.htm"))
		{
			if(st.getInt("talk1") == 0)
				st.set("talk1", "1");
		}
		else if(event.equalsIgnoreCase("32047-05.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
			st.set("choice", "1");
			st.unset("talk1");
		}
		else if(event.equalsIgnoreCase("32047-06.htm"))
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
			st.set("choice", "2");
			st.unset("talk1");
		}
		else if(event.equalsIgnoreCase("32047-07.htm"))
		{
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
			st.set("choice", "3");
			st.unset("talk1");
		}
		else if(event.equalsIgnoreCase("32047-13.htm"))
		{
			st.set("cond", "7");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32047-13a.htm"))
		{
			st.set("cond", "10");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32047-15.htm"))
		{
			if(st.getInt("talk") == 0)
				st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("32047-15a.htm"))
		{
			if(GUARDIAN_SPAWN == null || !GUARDIAN_SPAWN.isVisible())
			{
				L2Npc GUARDIAN_SPAWN = st.addSpawn(GUARDIAN,96977,-110625,-3280,0,false,900000);
          			final NpcSay ns = new NpcSay(GUARDIAN_SPAWN.getObjectId(), 0, GUARDIAN_SPAWN.getId(), NpcStringId.YOU_S1_YOU_ATTACKED_WENDY_PREPARE_TO_DIE);
          			ns.addStringParameter(player.getName());
          			GUARDIAN_SPAWN.broadcastPacket(ns);
				GUARDIAN_SPAWN.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 999);
			}		
			else
				htmltext = "32047-19a.htm";
		}
		else if(event.equalsIgnoreCase("32047-17a.htm"))
		{
			st.set("cond", "12");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32047-20.htm"))
		{
			st.set("talk", "2");
		}
		else if(event.equalsIgnoreCase("32047-23.htm"))
		{
			st.set("cond", "13");
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
		}
		else if(event.equalsIgnoreCase("32047-25.htm"))
		{
			st.set("cond", "15");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(STARSTONE, 1);
		}
		else if(event.equalsIgnoreCase("32047-30.htm"))
		{
			st.set("talk", "2");
		}
		else if(event.equalsIgnoreCase("32047-33.htm"))
		{
			if(st.getInt("cond") == 7)
			{
				st.set("cond", "8");
				st.set("talk", "0");
				st.playSound("ItemSound.quest_middle");
			}
			else if(st.getInt("cond") == 8)
			{
				st.set("cond", "9");
				st.playSound("ItemSound.quest_middle");
				htmltext = "32047-34.htm";
			}
		}
		else if(event.equalsIgnoreCase("32047-34.htm"))
		{
			st.set("cond", "9");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32047-38.htm"))
		{
			st.giveItems(STARSTONE2, 1);
			st.takeItems(57, 3000);
			st.set("cond", "26");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32050-02.htm"))
		{
			st.playSound("ItemSound.armor_wood_3");
			st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("32050-04.htm"))
		{
			st.set("cond", "14");
			st.giveItems(STARSTONE, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("talk", "0");
		}	
		return htmltext;
	}	
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;

		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int talk = st.getInt("talk");
		int talk1 = st.getInt("talk1");

		switch (st.getState())
		{		
			case State.CREATED:
				QuestState qs = player.getQuestState("_121_PavelTheGiant");
				if(player.getLevel() >= 70 && qs != null && qs.isCompleted())
					htmltext = "32041-01.htm";
				else
				{
					htmltext = "32041-00.htm";
					st.exitQuest(true);
				}			
				break;
			case State.STARTED:
				if(npcId == YUMI)
				{
					if(cond == 1)
					{
						if(talk == 0)
							htmltext = "32041-02.htm";
						else
							htmltext = "32041-06.htm";
					}
					else if(cond == 2)
						htmltext = "32041-08.htm";
					else if(cond == 3 || cond == 4 || cond == 5)
					{
						if(talk == 0)
							htmltext = "32041-09.htm";
						else if(talk == 1)
							htmltext = "32041-11.htm";
						else
							htmltext = "32041-18.htm";
					}
					else if(cond == 6)
						htmltext = "32041-21.htm";
					else if(cond == 9 || cond == 12 || cond == 16)
						htmltext = "32041-22.htm";
					else if(cond == 17)
						htmltext = "32041-26.htm";
					else if(cond == 19)
					{
						if(talk == 0)
							htmltext = "32041-27.htm";
						else
							htmltext = "32041-28.htm";
					}
					else if(cond == 20)
						htmltext = "32041-36.htm";
					else if(cond == 21)
						htmltext = "32041-33.htm";
					else if(cond == 22 || cond == 26)
					{
						htmltext = "32041-34.htm";
						st.set("cond", "27");
						st.playSound("ItemSound.quest_middle");
					}
					else if(cond == 27)
						htmltext = "32041-35.htm";
				}
				else if(npcId == WENDY)
				{
					if(cond == 2)
					{
						if(talk + talk1 < 2)
							htmltext = "32047-01.htm";
						else if(talk + talk1 == 2)
							htmltext = "32047-04.htm";
					}
					else if(cond == 3)
						htmltext = "32047-09.htm";
					else if(cond == 4 || cond == 5)
						htmltext = "32047-09a.htm";
					else if(cond == 6)
					{
						int choice = st.getInt("choice");
						if(choice == 1)
						{
							if(talk == 0)
								htmltext = "32047-10.htm";
							else if(talk == 1)
								htmltext = "32047-20.htm";
							else
								htmltext = "32047-30.htm";
						}
						else if(choice == 2)
							htmltext = "32047-10a.htm";
						else if(choice == 3)
							if(talk == 0)
								htmltext = "32047-14.htm";
							else if(talk == 1)
								htmltext = "32047-15.htm";
							else
								htmltext = "32047-20.htm";
					}
					else if(cond == 7)
					{
						if(talk == 0)
							htmltext = "32047-14.htm";
						else if(talk == 1)
							htmltext = "32047-15.htm";
						else
							htmltext = "32047-20.htm";
					}
					else if(cond == 8)
						htmltext = "32047-30.htm";
					else if(cond == 9)
						htmltext = "32047-27.htm";
					else if(cond == 10)
						htmltext = "32047-14a.htm";
					else if(cond == 11)
						htmltext = "32047-16a.htm";
					else if(cond == 12)
						htmltext = "32047-18a.htm";
					else if(cond == 13)
						htmltext = "32047-23.htm";
					else if(cond == 14)
						htmltext = "32047-24.htm";
					else if(cond == 15)
					{
						htmltext = "32047-26.htm";
						st.set("cond", "16");
						st.playSound("ItemSound.quest_middle");
					}
					else if(cond == 16)
						htmltext = "32047-27.htm";
					else if(cond == 20)
						htmltext = "32047-35.htm";
					else if(cond == 26)
						htmltext = "32047-40.htm";
				}
				else if(npcId == BOX)
				{
					if(cond == 13)
					{
						if(talk == 0)
							htmltext = "32050-01.htm";
						else
							htmltext = "32050-03.htm";
					}
					else if(cond == 14)
						htmltext = "32050-05.htm";
				}
				else if(npcId == STONES)
				{
					if(cond == 18)
						htmltext = "32046-01.htm";
					else if(cond == 19)
						htmltext = "32046-02.htm";
					else if(cond == 27)
						htmltext = "32046-03.htm";
				}
				else if(npcId == NEWYEAR)
					if(cond == 21)
						htmltext = "31961-01.htm";
					else if(cond == 22)
						htmltext = "31961-03.htm";				
				break;
		}
		return htmltext;
	}	


	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);

		if(npc.getId() == STONES && st.getInt("cond") == 17)
		{
			st.playSound("ItemSound.quest_middle");
			st.takeItems(DETECTOR, 1);
			st.giveItems(DETECTOR2, 1);
			st.set("cond", "18");
	        	player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_RADIO_SIGNAL_DETECTOR_IS_RESPONDING_A_SUSPICIOUS_PILE_OF_STONES_CATCHES_YOUR_EYE, 2, 4500));
		}
		npc.showChatWindow(player);

		return "";
	}		
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		int npcId = npc.getId();

		if(st.getInt("cond") == 10)
		{
			if(npcId == GUARDIAN)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npcId, NpcStringId.THIS_ENEMY_IS_FAR_TOO_POWERFUL_FOR_ME_TO_FIGHT_I_MUST_WITHDRAW));	
				st.set("cond", "11");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}	

	public static void main(String[] args)
	{
		new _114_ResurrectionOfAnOldManager(114, qn, "");
	}
}