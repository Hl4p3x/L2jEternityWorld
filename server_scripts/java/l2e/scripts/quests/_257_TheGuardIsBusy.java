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

public class _257_TheGuardIsBusy extends Quest
{
	private static final String qn = "_257_TheGuardIsBusy";

	// Items
	private static final int GLUDIO_LORDS_MARK = 1084;
	private static final int ORC_AMULET = 752;
	private static final int ORC_NECKLACE = 1085;
	private static final int WEREWOLF_FANG = 1086;
	private static final int ADENA = 57;

	// Newbie section
	private static final int NEWBIE_REWARD = 4;
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;

	public _257_TheGuardIsBusy(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(30039);
		addTalkId(30039);

		addKillId(20130);
		addKillId(20131);
		addKillId(20132);
		addKillId(20342);
		addKillId(20343);
		addKillId(20006);
		addKillId(20093);
		addKillId(20096);
		addKillId(20098);

		questItemIds = new int [] { GLUDIO_LORDS_MARK, ORC_AMULET, ORC_NECKLACE, WEREWOLF_FANG };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

	    	if (event.equalsIgnoreCase("30039-03.htm"))
	    	{
	    		st.set("cond","1");
	    		st.setState(State.STARTED);
	    		st.playSound("ItemSound.quest_accept");
	    		st.giveItems(GLUDIO_LORDS_MARK,1);
	    	}
	    	else if (event.equalsIgnoreCase("30039-05.htm"))
	    	{
	    		st.takeItems(GLUDIO_LORDS_MARK,1);
	    		st.exitQuest(true);
	    		st.playSound("ItemSound.quest_finish");
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

		int id = st.getState();

		if (id == State.CREATED)
			st.set("cond","0");

		if (st.getInt("cond") == 0)
		{
			if (player.getLevel() >= 6)
				htmltext = "30039-02.htm";
			else
			{
				htmltext = "30039-01.htm";
				st.exitQuest(true);
			}
		}
		else
		{
			long orc_a = st.getQuestItemsCount(ORC_AMULET);
			long orc_n = st.getQuestItemsCount(ORC_NECKLACE);
			long wer_f = st.getQuestItemsCount(WEREWOLF_FANG);
			if (orc_a == 0 && orc_n == 0 && wer_f == 0)
				htmltext = "30039-04.htm";
			else
			{
			    	int newbie = player.getNewbie();

			    	if ((newbie | NEWBIE_REWARD) != newbie)
			    	{
			    		player.setNewbie(newbie | NEWBIE_REWARD);
			    		st.showQuestionMark(26);
			    		if (player.getClassId().isMage())
			    		{
			    			st.playTutorialVoice("tutorial_voice_027");
			    			st.giveItems(SPIRITSHOT_FOR_BEGINNERS,3000);
						player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2", player.getLang())).toString()), 3000));
			    		}
			    		else
			    		{
			    			st.playTutorialVoice("tutorial_voice_026");
			    			st.giveItems(SOULSHOT_FOR_BEGINNERS,6000);
						player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2a", player.getLang())).toString()), 3000));
			    		}
			    	}
			    	st.giveItems(ADENA, 5*orc_a+15*orc_n+10*wer_f);
			    	st.takeItems(ORC_AMULET,-1);
			    	st.takeItems(ORC_NECKLACE,-1);
			    	st.takeItems(WEREWOLF_FANG,-1);
			    	htmltext = "30039-07.htm";
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
		int chance = 5, item;

		if (npcId  == 20130 || npcId == 20131 || npcId == 20006)
		     	item = ORC_AMULET;
		else if (npcId == 20093 || npcId == 20096 || npcId == 20098)
		     	item = ORC_NECKLACE;
		else
		{
			item = WEREWOLF_FANG;
		     	if (npcId == 20343)
		    		chance = 4;
		     	else if (npcId == 20342)
		    		chance = 2;
		}
		if (st.getQuestItemsCount(GLUDIO_LORDS_MARK) == 1 && getRandom(10) < chance)
		{
	       		st.giveItems(item,1);
	       		st.playSound("ItemSound.quest_itemget");
		}
	   	return "";
	}
	
	public static void main(String[] args)
	{
		new _257_TheGuardIsBusy(257, qn, "");
	}
}