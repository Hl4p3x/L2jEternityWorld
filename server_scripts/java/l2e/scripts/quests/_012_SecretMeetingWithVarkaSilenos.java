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

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 21.03.2011
 * Based on L2J Eternity-World
 */
public class _012_SecretMeetingWithVarkaSilenos extends Quest
{
	private static final String qn = "_012_SecretMeetingWithVarkaSilenos";

	private static final int CADMON = 31296;
	private static final int HELMUT = 31258;
	private static final int NARAN_ASHANUK = 31378;

	private static final int MUNITIONS_BOX = 7232;

	public _012_SecretMeetingWithVarkaSilenos(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(CADMON);
		addTalkId(CADMON);
		addTalkId(HELMUT);
		addTalkId(NARAN_ASHANUK);
		questItemIds = new int[] {MUNITIONS_BOX};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31296-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31258-02.htm"))
		{
			st.giveItems(MUNITIONS_BOX, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31378-02.htm"))
		{
			st.takeItems(MUNITIONS_BOX, 1);
			st.addExpAndSp(233125, 18142);
			st.unset("cond");
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(qn);
		if (qs == null)
			qs = newQuestState(player);
		
		String htmltext = getNoQuestMsg(player);		
		final int cond = qs.getInt("cond");
		final int npcId = npc.getId();
		
		switch (qs.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == CADMON)
				{
					if (qs.getPlayer().getLevel() >= 74)
						htmltext = "31296-01.htm";
					else
					{
						htmltext = "31296-02.htm";
						qs.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				switch (npcId)
				{
					case CADMON:
						switch (cond)
						{
							case 1:
								htmltext = "31296-04.htm";
								break;
						}
						break;
					case HELMUT:
						switch (cond)
						{
							case 1:
								htmltext = "31258-01.htm";
								break;
							case 2:
								htmltext = "31258-03.htm";
								break;							
						}
						break;
					case NARAN_ASHANUK:
						switch (cond)
						{
							case 2:
								if (qs.getQuestItemsCount(MUNITIONS_BOX) > 0)
									htmltext = "31378-01.htm";
								break;
						}
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _012_SecretMeetingWithVarkaSilenos(12, qn, "");    	
	}
}
