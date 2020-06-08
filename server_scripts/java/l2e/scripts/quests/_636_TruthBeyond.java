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

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.zone.L2ZoneType;

/**
 * Based on L2J Eternity-World
 */
public final class _636_TruthBeyond extends Quest
{
	private static final String qn = "_636_TruthBeyond";
	
	private static final int ELIAH = 31329;
	private static final int FLAURON = 32010;
	private static final int ZONE = 30100;
	private static final int VISITOR_MARK = 8064;
	private static final int FADED_MARK = 8065;
	private static final int MARK = 8067;
	
	public _636_TruthBeyond(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ELIAH);
		addTalkId(ELIAH);
		addTalkId(FLAURON);

		addEnterZoneId(ZONE);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if ("31329-04.htm".equalsIgnoreCase(event))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if ("32010-02.htm".equalsIgnoreCase(event))
		{
			st.giveItems(VISITOR_MARK, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();

		switch (st.getState())
		{
			case State.CREATED:
				if (npcId == ELIAH)
				{
					if ((st.getQuestItemsCount(VISITOR_MARK) == 0) && (st.getQuestItemsCount(MARK) == 0))
					{
						if (player.getLevel() >= 73)
						{
							htmltext = "31329-02.htm";
						}	
						else
						{
							st.exitQuest(true);
							htmltext = "31329-01.htm";
						}
					}
					else
					{
						htmltext = "31329-mark.htm";
					}
				}
				else if(npcId == FLAURON)
				{
					if (st.getQuestItemsCount(VISITOR_MARK) == 1)
					{
						htmltext = "32010-03.htm";
					}
				}
				break;
			case State.STARTED:
				if (npcId == ELIAH)
				{
					htmltext = "31329-05.htm";
				}
				else if (npcId == FLAURON)
				{
					if (st.getInt("cond") == 1)
					{
						htmltext = "32010-01.htm";
					}
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.isPlayer())
		{
			if (((L2PcInstance) character).destroyItemByItemId("Mark", VISITOR_MARK, 1, character, false))
			{
				((L2PcInstance) character).addItem("Mark", FADED_MARK, 1, character, true);
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _636_TruthBeyond(636, qn, "");
	}
}