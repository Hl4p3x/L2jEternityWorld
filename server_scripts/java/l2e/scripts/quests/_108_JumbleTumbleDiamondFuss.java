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
import l2e.util.Rnd;

public class _108_JumbleTumbleDiamondFuss extends Quest
{
	private static final String qn = "_108_JumbleTumbleDiamondFuss";

	// QuestItem
	private static final int GOUPHS_CONTRACT = 1559;
	private static final int REEPS_CONTRACT = 1560;
	private static final int ELVEN_WINE = 1561;
	private static final int BRONPS_DICE = 1562;
	private static final int BRONPS_CONTRACT = 1563;
	private static final int AQUAMARINE = 1564;
	private static final int CHRYSOBERYL = 1565;
	private static final int GEM_BOX1 = 1566;
	private static final int COAL_PIECE = 1567;
	private static final int BRONPS_LETTER = 1568;
	private static final int BERRY_TART = 1569;
	private static final int BAT_DIAGRAM = 1570;
	private static final int STAR_DIAMOND = 1571;
	private static final int SILVERSMITH_HAMMER = 1511;

	// Newbie section
	private static final int NEWBIE_REWARD = 2;
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;

	public _108_JumbleTumbleDiamondFuss(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(30523);

		addTalkId(30523);
		addTalkId(30516);
		addTalkId(30521);
		addTalkId(30522);
		addTalkId(30526);
		addTalkId(30529);
		addTalkId(30555);

		addKillId(20323);
		addKillId(20324);
		addKillId(20480);

		questItemIds = new int[] { GEM_BOX1, STAR_DIAMOND, GOUPHS_CONTRACT, REEPS_CONTRACT, ELVEN_WINE, BRONPS_CONTRACT, AQUAMARINE, CHRYSOBERYL, COAL_PIECE, BRONPS_DICE, BRONPS_LETTER, BERRY_TART, BAT_DIAGRAM };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("30523-03.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.giveItems(GOUPHS_CONTRACT, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30555-02.htm"))
		{
			st.takeItems(REEPS_CONTRACT, 1);
			st.giveItems(ELVEN_WINE, 1);
			st.set("cond","3");
		}
		else if(event.equalsIgnoreCase("30526-02.htm"))
		{
			st.takeItems(BRONPS_DICE, 1);
			st.giveItems(BRONPS_CONTRACT, 1);
			st.set("cond","5");
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;

		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int id = st.getState();

		if (id == State.CREATED)
			st.set("cond","0");

		if(npcId == 30523 && id == State.COMPLETED)
			htmltext = getAlreadyCompletedMsg(player);

		else if(npcId == 30523)
		{
			if(cond == 0)
			{
				if (player.getRace().ordinal() == 4)
				{
					if(player.getLevel() >= 10)
						htmltext = "30523-02.htm";
					else
					{
						htmltext = "30523-01.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30523-00.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 0 && st.getQuestItemsCount(GOUPHS_CONTRACT) > 0)
				htmltext = "30523-04.htm";
			else if(cond > 1 && cond < 7 && (st.getQuestItemsCount(REEPS_CONTRACT) > 0 || st.getQuestItemsCount(ELVEN_WINE) > 0 || st.getQuestItemsCount(BRONPS_DICE) > 0 || st.getQuestItemsCount(BRONPS_CONTRACT) > 0))
				htmltext = "30523-05.htm";
			else if(cond == 7 && st.getQuestItemsCount(GEM_BOX1) > 0)
			{
				htmltext = "30523-06.htm";
				st.takeItems(GEM_BOX1, 1);
				st.giveItems(COAL_PIECE, 1);
				st.set("cond","8");
			}
			else if(cond > 7 && cond < 12 && (st.getQuestItemsCount(BRONPS_LETTER) > 0 || st.getQuestItemsCount(COAL_PIECE) > 0 || st.getQuestItemsCount(BERRY_TART) > 0 || st.getQuestItemsCount(BAT_DIAGRAM) > 0))
				htmltext = "30523-07.htm";
			else if(cond == 12 && st.getQuestItemsCount(STAR_DIAMOND) > 0)
			{
				int newbie = player.getNewbie();

				if ((newbie | NEWBIE_REWARD) != newbie)
				{
					player.setNewbie(newbie|NEWBIE_REWARD);

					if(player.getClassId().isMage())
					{
             					st.playTutorialVoice("tutorial_voice_027");
             					st.giveItems(SPIRITSHOT_FOR_BEGINNERS,3000);
					}
					else
					{
             					st.playTutorialVoice("tutorial_voice_026");
             					st.giveItems(SOULSHOT_FOR_BEGINNERS,6000);
					}
	            			player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message3", player.getLang())).toString()), 3000));
				}
				htmltext = "30523-08.htm";
				st.takeItems(STAR_DIAMOND, 1);
				st.giveItems(SILVERSMITH_HAMMER, 1);
				st.addExpAndSp(34565, 2962);
				st.giveItems(57, 14666);
				for(int ECHO_CHRYSTAL = 4412; ECHO_CHRYSTAL <= 4416; ECHO_CHRYSTAL++)
					st.giveItems(ECHO_CHRYSTAL, 10);
				st.giveItems(1060, 100);
				st.set("cond","0");
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
		}
 		else if(id == State.STARTED)
		{
			if(npcId == 30516)
			{
				if(cond == 1 && st.getQuestItemsCount(GOUPHS_CONTRACT) > 0)
				{
					htmltext = "30516-01.htm";
					st.giveItems(REEPS_CONTRACT, 1);
					st.takeItems(GOUPHS_CONTRACT, 1);
					st.set("cond","2");
				}
				else if(cond >= 2)
					htmltext = "30516-02.htm";
			}
			else if(npcId == 30555)
			{
				if(cond == 2 && st.getQuestItemsCount(REEPS_CONTRACT) == 1)
					htmltext = "30555-01.htm";
				else if(cond == 3 && st.getQuestItemsCount(ELVEN_WINE) > 0)
					htmltext = "30555-03.htm";
				else if(cond == 7 && st.getQuestItemsCount(GEM_BOX1) == 1)
					htmltext = "30555-04.htm";
				else
					htmltext = "30555-05.htm";
			}
			else if(npcId == 30529)
			{
				if(cond == 3 && st.getQuestItemsCount(ELVEN_WINE) > 0)
				{
					st.takeItems(ELVEN_WINE, 1);
					st.giveItems(BRONPS_DICE, 1);
					htmltext = "30529-01.htm";
					st.set("cond","4");
				}
				else if(cond == 4)
					htmltext = "30529-02.htm";
				else
					htmltext = "30529-03.htm";
			}
			else if(npcId == 30526)
			{
				if(cond == 4 && st.getQuestItemsCount(BRONPS_DICE) > 0)
					htmltext = "30526-01.htm";
				else if(cond == 5 && st.getQuestItemsCount(BRONPS_CONTRACT) > 0 && (st.getQuestItemsCount(AQUAMARINE) < 10 || st.getQuestItemsCount(CHRYSOBERYL) < 10))
					htmltext = "30526-03.htm";
				else if(cond == 6 && st.getQuestItemsCount(BRONPS_CONTRACT) > 0 && st.getQuestItemsCount(AQUAMARINE) == 10 && st.getQuestItemsCount(CHRYSOBERYL) == 10)
				{
					htmltext = "30526-04.htm";
					st.takeItems(BRONPS_CONTRACT, -1);
					st.takeItems(AQUAMARINE, -1);
					st.takeItems(CHRYSOBERYL, -1);
					st.giveItems(GEM_BOX1, 1);
					st.set("cond","7");
				}
				else if(cond == 7 && st.getQuestItemsCount(GEM_BOX1) > 0)
					htmltext = "30526-05.htm";
				else if(cond == 8 && st.getQuestItemsCount(COAL_PIECE) > 0)
				{
					htmltext = "30526-06.htm";
					st.takeItems(COAL_PIECE, 1);
					st.giveItems(BRONPS_LETTER, 1);
					st.set("cond","9");
				}
				else if(cond == 9 && st.getQuestItemsCount(BRONPS_LETTER) > 0)
					htmltext = "30526-07.htm";
				else
					htmltext = "30526-08.htm";
			}
			else if(npcId == 30521)
			{
				if(cond == 9 && st.getQuestItemsCount(BRONPS_LETTER) > 0)
				{
					htmltext = "30521-01.htm";
					st.takeItems(BRONPS_LETTER, 1);
					st.giveItems(BERRY_TART, 1);
					st.set("cond","10");
				}
				else if(cond == 10 && st.getQuestItemsCount(BERRY_TART) > 0)
					htmltext = "30521-02.htm";
				else
					htmltext = "30521-03.htm";
			}
			else if(npcId == 30522)
			{
				if(cond == 10 && st.getQuestItemsCount(BERRY_TART) > 0)
				{
					htmltext = "30522-01.htm";
					st.takeItems(BERRY_TART, 1);
					st.giveItems(BAT_DIAGRAM, 1);
					st.set("cond","11");
				}
				else if(cond == 11 && st.getQuestItemsCount(BAT_DIAGRAM) > 0)
					htmltext = "30522-02.htm";
				else if(cond == 12 && st.getQuestItemsCount(STAR_DIAMOND) > 0)
					htmltext = "30522-03.htm";
				else
					htmltext = "30522-04.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if(npcId == 20323 || npcId == 20324)
		{
			if(cond == 5 && st.getQuestItemsCount(BRONPS_CONTRACT) > 0)
			{
				if(st.getQuestItemsCount(AQUAMARINE) < 10 && Rnd.getChance(80))
				{
					st.giveItems(AQUAMARINE, 1);
					if(st.getQuestItemsCount(AQUAMARINE) < 10)
						st.playSound("ItemSound.quest_itemget");
					else
					{
						st.playSound("ItemSound.quest_middle");
						if(st.getQuestItemsCount(AQUAMARINE) == 10 && st.getQuestItemsCount(CHRYSOBERYL) == 10)
							st.set("cond","6");
					}
				}
				if(st.getQuestItemsCount(CHRYSOBERYL) < 10 && Rnd.getChance(80))
				{
					st.giveItems(CHRYSOBERYL, 1);
					if(st.getQuestItemsCount(CHRYSOBERYL) < 10)
						st.playSound("ItemSound.quest_itemget");
					else
					{
						st.playSound("ItemSound.quest_middle");
						if(st.getQuestItemsCount(AQUAMARINE) == 10 && st.getQuestItemsCount(CHRYSOBERYL) == 10)
							st.set("cond","6");
					}
				}
			}
		}
		else if(npcId == 20480)
		{
			if(cond == 11 && st.getQuestItemsCount(BAT_DIAGRAM) > 0 && st.getQuestItemsCount(STAR_DIAMOND) == 0)
				if(Rnd.getChance(50))
				{
					st.takeItems(BAT_DIAGRAM, 1);
					st.giveItems(STAR_DIAMOND, 1);
					st.set("cond","12");
					st.playSound("ItemSound.quest_middle");
				}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _108_JumbleTumbleDiamondFuss(108, qn, "");
	}	
}