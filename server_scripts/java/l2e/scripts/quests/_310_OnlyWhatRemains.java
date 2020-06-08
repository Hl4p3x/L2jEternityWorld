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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

public class _310_OnlyWhatRemains extends Quest
{
	// NPC
	private static final int KINTAIJIN = 32640;
	
	// Items
	private static final int GROW_ACCELERATOR = 14832;
	private static final int MULTI_COLORED_JEWEL = 14835;
	private static final int DIRTY_BEAD = 14880;
	
	// Monsters
	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	
	static
	{
		MOBS.put(22617, 646);
		MOBS.put(22618, 646);
		MOBS.put(22619, 646);
		MOBS.put(22620, 666);
		MOBS.put(22621, 630);
		MOBS.put(22622, 940);
		MOBS.put(22623, 622);
		MOBS.put(22624, 630);
		MOBS.put(22625, 678);
		MOBS.put(22626, 940);
		MOBS.put(22627, 646);
		MOBS.put(22628, 646);
		MOBS.put(22629, 646);
		MOBS.put(22630, 638);
		MOBS.put(22631, 880);
		MOBS.put(22632, 722);
		MOBS.put(22633, 638);
	}

	public _310_OnlyWhatRemains(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(KINTAIJIN);
		addTalkId(KINTAIJIN);

		addKillId(MOBS.keySet());
		
		registerQuestItems(DIRTY_BEAD);
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
			case "32640-04.htm":
				st.startQuest();
				break;
			case "32640-quit.htm":
				st.exitQuest(true, true);
				break;
			case "32640-02.htm":
			case "32640-03.htm":
			case "32640-05.htm":
			case "32640-06.htm":
			case "32640-07.htm":
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
				final QuestState prev = player.getQuestState("_240_ImTheOnlyOneYouCanTrust");
				htmltext = ((player.getLevel() >= 81) && (prev != null) && prev.isCompleted()) ? "32640-01.htm" : "32640-00.htm";
				break;
			case State.STARTED:
				if (!st.hasQuestItems(DIRTY_BEAD))
				{
					htmltext = "32640-08.htm";
				}
				else if (st.getQuestItemsCount(DIRTY_BEAD) < 500)
				{
					htmltext = "32640-09.htm";
				}
				else
				{
					st.takeItems(DIRTY_BEAD, 500);
					st.giveItems(GROW_ACCELERATOR, 1);
					st.giveItems(MULTI_COLORED_JEWEL, 1);
					htmltext = "32640-10.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
			return super.onKill(npc, player, isSummon);
		
		final QuestState st = partyMember.getQuestState(getName());
		
		if (getRandom(1000) < MOBS.get(npc.getId()))
		{
			st.giveItems(DIRTY_BEAD, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _310_OnlyWhatRemains(310, _310_OnlyWhatRemains.class.getSimpleName(), "");
	}
}