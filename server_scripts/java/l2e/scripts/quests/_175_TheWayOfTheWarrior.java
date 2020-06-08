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

import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.util.Util;

public class _175_TheWayOfTheWarrior extends Quest
{
	private static final String qn = "_175_TheWayOfTheWarrior";
	
	// NPC
	private static final int Kekropus = 32138;
	private static final int Perwan = 32133;
	
	// Quest Items
	private static final int WolfTail = 9807;
	private static final int MuertosClaw = 9808;
	
	// Items
	private static final int WarriorsSword = 9720;
	
	// MOBs
	private static final int MountainWerewolf = 22235;
	private static final int[] MUERTOS =
	{
		22236,
		22239,
		22240,
		22242,
		22243,
		22245,
		22246
	};
	
	// Newbie section
	private static final int NEWBIE_REWARD = 16;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public _175_TheWayOfTheWarrior(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Kekropus);
		
		addTalkId(Kekropus);
		addTalkId(Perwan);
		
		addKillId(MountainWerewolf);
		for (int i : MUERTOS)
		{
			addKillId(i);
		}
		
		questItemIds = new int[]
		{
			WolfTail,
			MuertosClaw
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
		
		if (event.equalsIgnoreCase("32138-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32133-06.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32138-09.htm"))
		{
			st.set("cond", "7");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32138-12.htm"))
		{
			int newbie = player.getNewbie();
			
			if ((newbie | NEWBIE_REWARD) != newbie)
			{
				player.setNewbie(newbie | NEWBIE_REWARD);
				
				player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message3", player.getLang())).toString()), 3000));
				st.giveItems(1060, 100);
				for (int item = 4412; item <= 4417; item++)
				{
					st.giveItems(item, 10);
				}
				st.playTutorialVoice("tutorial_voice_026");
				st.giveItems(SOULSHOT_FOR_BEGINNERS, 7000);
			}
			st.takeItems(MuertosClaw, -1);
			st.giveItems(WarriorsSword, 1);
			st.giveItems(57, 8799);
			st.addExpAndSp(20739, 1777);
			player.sendPacket(new SocialAction(player.getObjectId(), 3));
			player.sendPacket(new SocialAction(player.getObjectId(), 15));
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int id = st.getState();
		
		if (id == State.COMPLETED)
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		else if ((id == State.CREATED) && (npcId == Kekropus))
		{
			if ((player.getLevel() >= 10) && (player.getRace().ordinal() == 5))
			{
				htmltext = "32138-01.htm";
			}
			else
			{
				htmltext = "32138-02.htm";
				st.exitQuest(true);
			}
		}
		else if (id == State.STARTED)
		{
			if (npcId == Kekropus)
			{
				if (cond == 1)
				{
					htmltext = "32138-05.htm";
				}
				else if (cond == 4)
				{
					st.set("cond", "5");
					st.playSound("ItemSound.quest_middle");
					htmltext = "32138-06.htm";
				}
				else if (cond == 5)
				{
					htmltext = "32138-07.htm";
				}
				else if (cond == 6)
				{
					htmltext = "32138-08.htm";
				}
				else if (cond == 7)
				{
					htmltext = "32138-10.htm";
				}
				else if (cond == 8)
				{
					htmltext = "32138-11.htm";
				}
			}
			else if (npcId == Perwan)
			{
				if (cond == 1)
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
					htmltext = "32133-01.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32133-02.htm";
				}
				else if (cond == 3)
				{
					st.takeItems(WolfTail, -1);
					st.set("cond", "4");
					st.playSound("ItemSound.quest_middle");
					htmltext = "32133-03.htm";
				}
				else if (cond == 4)
				{
					htmltext = "32133-04.htm";
				}
				else if (cond == 5)
				{
					htmltext = "32133-05.htm";
				}
				else if (cond == 6)
				{
					htmltext = "32133-07.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int chance = getRandom(100);
		
		long tails = st.getQuestItemsCount(WolfTail);
		long claws = st.getQuestItemsCount(MuertosClaw);
		
		if ((npcId == MountainWerewolf) && (chance < 50) && (cond == 2) && (tails < 5))
		{
			st.giveItems(WolfTail, 1);
			st.playSound("ItemSound.quest_itemget");
			
			if (st.getQuestItemsCount(WolfTail) == 5)
			{
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (Util.contains(MUERTOS, npc.getId()) && (claws < 10) && (cond == 7))
		{
			st.giveItems(MuertosClaw, 1);
			st.playSound("ItemSound.quest_itemget");
			
			if (st.getQuestItemsCount(MuertosClaw) == 10)
			{
				st.set("cond", "8");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _175_TheWayOfTheWarrior(175, qn, "");
	}
}