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

public class _432_BirthdayPartySong extends Quest
{
	private static final int OCTAVIA = 31043;
	
	private static final int GOLEM = 21103;

	private static final int RED_CRYSTAL = 7541;

	private static final int ECHO_CRYSTAL = 7061;

	public _432_BirthdayPartySong(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(OCTAVIA);
		addTalkId(OCTAVIA);

		addKillId(GOLEM);
		
		registerQuestItems(RED_CRYSTAL);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;
		
		String htmltext = event;
		switch (event)
		{
			case "31043-02.htm":
				st.startQuest();
				break;
			case "31043-05.htm":
				if (st.getQuestItemsCount(RED_CRYSTAL) < 50)
				{
					return "31043-06.htm";
				}
				st.giveItems(ECHO_CRYSTAL, 25);
				st.exitQuest(true, true);
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() >= 31) ? "31043-01.htm" : "31043-00.htm";
				break;
			case State.STARTED:
				htmltext = (st.isCond(1)) ? "31043-03.htm" : "31043-04.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(getName());
		
		if ((st != null) && st.isCond(1) && getRandomBoolean())
		{
			st.giveItems(RED_CRYSTAL, 1);
			if (st.getQuestItemsCount(RED_CRYSTAL) == 50)
				st.setCond(2, true);
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _432_BirthdayPartySong(432, _432_BirthdayPartySong.class.getSimpleName(), "");
	}
}