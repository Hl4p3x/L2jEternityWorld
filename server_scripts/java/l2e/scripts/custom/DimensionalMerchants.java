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
package l2e.scripts.custom;

import l2e.Config;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

/**
 * Based on L2J Eternity-World
 */
public class DimensionalMerchants extends Quest
{
	public DimensionalMerchants(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(32478);
		addTalkId(32478);
		addFirstTalkId(32478);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		if (event.equalsIgnoreCase("13017") || event.equalsIgnoreCase("13018") || event.equalsIgnoreCase("13019") || event.equalsIgnoreCase("13020"))
		{
			long normalItem = st.getQuestItemsCount(13273);
			long eventItem = st.getQuestItemsCount(13383);
			if (normalItem >= 1)
			{
				st.takeItems(13273, 1);
				st.giveItems(Integer.valueOf(event), 1);
				st.exitQuest(true);
				return htmltext;
			}
			else if (eventItem >= 1)
			{
				event = (event) + 286;
				st.takeItems(13383, 1);
				st.giveItems(Integer.valueOf(event), 1);
				st.exitQuest(true);
				return htmltext;
			}
			else
			{
				htmltext = "32478-11.htm";
			}
		}
		else if (event.equalsIgnoreCase("13548") || event.equalsIgnoreCase("13549") || event.equalsIgnoreCase("13550") || event.equalsIgnoreCase("13551"))
		{
			if (st.getQuestItemsCount(14065) >= 1)
			{
				st.takeItems(14065, 1);
				st.giveItems(Integer.valueOf(event), 1);
				st.exitQuest(true);
			}
			else
			{
				htmltext = "32478-11.htm";
				st.exitQuest(true);
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		
		if (Config.VITAMIN_MANAGER)
		{
			htmltext = "32478.htm";
		}
		else
		{
			htmltext = "32478-na.htm";
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new DimensionalMerchants(-1, "DimensionalMerchants", "custom");
	}
}