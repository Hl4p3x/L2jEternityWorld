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

import java.util.Calendar;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.QuestState.QuestType;
import l2e.gameserver.model.quest.State;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class BlackMarketeerOfMammon extends AbstractNpcAI
{
	private static final int BLACK_MARKETEER = 31092;

	private static final int MIN_LEVEL = 60;
	
	private BlackMarketeerOfMammon(String name, String descr)
	{
		super(name, descr);
		
		addStartNpc(BLACK_MARKETEER);
		addTalkId(BLACK_MARKETEER);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		return exchangeAvailable() ? "31092-01.htm" : "31092-02.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState qs = player.getQuestState(getName());
		if ("exchange".equals(event))
		{
			if (exchangeAvailable())
			{
				if (player.getLevel() >= MIN_LEVEL)
				{
					if (!qs.isNowAvailable())
					{
						htmltext = "31092-03.htm";
					}
					else
					{
						if (player.getAdena() >= 2000000)
						{
							qs.setState(State.STARTED);
							takeItems(player, PcInventory.ADENA_ID, 2000000);
							giveItems(player, PcInventory.ANCIENT_ADENA_ID, 500000);
							htmltext = "31092-04.htm";
							qs.exitQuest(QuestType.DAILY, false);
						}
						else
						{
							htmltext = "31092-05.htm";
						}
					}
				}
				else
				{
					htmltext = "31092-06.htm";
				}
			}
			else
			{
				htmltext = "31092-02.htm";
			}
		}
		return htmltext;
	}
	
	private boolean exchangeAvailable()
	{
		Calendar currentTime = Calendar.getInstance();
		Calendar minTime = Calendar.getInstance();
		minTime.set(Calendar.HOUR_OF_DAY, 20);
		minTime.set(Calendar.MINUTE, 0);
		minTime.set(Calendar.SECOND, 0);
		Calendar maxtTime = Calendar.getInstance();
		maxtTime.set(Calendar.HOUR_OF_DAY, 23);
		maxtTime.set(Calendar.MINUTE, 59);
		maxtTime.set(Calendar.SECOND, 59);
		
		return (currentTime.compareTo(minTime) >= 0) && (currentTime.compareTo(maxtTime) <= 0);
	}
	
	public static void main(String[] args)
	{
		new BlackMarketeerOfMammon(BlackMarketeerOfMammon.class.getSimpleName(), "custom");
	}
}