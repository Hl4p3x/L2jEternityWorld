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

import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 14.06.2013 Based on L2J Eternity-World
 */
public final class _504_CompetitionfortheBanditStronghold extends Quest
{	
	private static final int MESSENGER = 35437;

	private static final int TARLK_AMULET = 4332;
	private static final int TROPHY_OF_ALLIANCE = 5009;

	private static final int[] MOBS =
	{
		20570,
		20571,
		20572,
		20573,
		20574
	};
	
	private static final SiegableHall BANDIT_STRONGHOLD = CHSiegeManager.getInstance().getSiegableHall(35);
	
	public _504_CompetitionfortheBanditStronghold(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);

		for (int mob : MOBS)
		{
			addKillId(mob);
		}
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		if (npc.getId() == MESSENGER)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (BANDIT_STRONGHOLD.getSiege().getAttackers().size() >= 5)
					{
						htmltext = "35437-00.htm";
					}
					else
					{
						htmltext = "35437-01.htm";
						st.setState(State.STARTED);
						st.set("cond", "1");
						st.playSound("ItemSound.quest_accept");
					}
					break;
				case State.STARTED:
					if (st.getQuestItemsCount(TARLK_AMULET) < 30)
					{
						htmltext = "35437-02.htm";
					}
					else
					{
						st.takeItems(TARLK_AMULET, 30);
						st.rewardItems(TROPHY_OF_ALLIANCE, 1);
						st.exitQuest(true);
						htmltext = "35437-03.htm";
					}
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{	
		QuestState st = killer.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if (!Util.contains(MOBS, npc.getId()))
		{
			return null;
		}
		
		if (st.isStarted() && st.isCond(1))
		{
			st.giveItems(TARLK_AMULET, 1);
			if (st.getQuestItemsCount(TARLK_AMULET) < 30)
			{
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.setCond(2, true);
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _504_CompetitionfortheBanditStronghold(504, _504_CompetitionfortheBanditStronghold.class.getSimpleName(), "");
	}
}