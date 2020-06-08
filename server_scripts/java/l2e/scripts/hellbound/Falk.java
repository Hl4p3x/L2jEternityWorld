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
package l2e.scripts.hellbound;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class Falk extends Quest
{
	private static final int FALK = 32297;
	private static final int BASIC_CERT = 9850;
	private static final int STANDART_CERT = 9851;
	private static final int PREMIUM_CERT = 9852;
	private static final int DARION_BADGE = 9674;
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			qs = newQuestState(player);
		}
		
		if (qs.hasQuestItems(BASIC_CERT) || qs.hasQuestItems(STANDART_CERT) || qs.hasQuestItems(PREMIUM_CERT))
		{
			return "32297-01a.htm";
		}
		return "32297-01.htm";
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			qs = newQuestState(player);
		}
		
		if (qs.hasQuestItems(BASIC_CERT) || qs.hasQuestItems(STANDART_CERT) || qs.hasQuestItems(PREMIUM_CERT))
		{
			return "32297-01a.htm";
		}
		return "32297-02.htm";
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			qs = newQuestState(player);
		}
		
		if (event.equalsIgnoreCase("badges"))
		{
			if (!qs.hasQuestItems(BASIC_CERT) && !qs.hasQuestItems(STANDART_CERT) && !qs.hasQuestItems(PREMIUM_CERT))
			{
				if (qs.getQuestItemsCount(DARION_BADGE) >= 20)
				{
					qs.takeItems(DARION_BADGE, 20);
					qs.giveItems(BASIC_CERT, 1);
					return "32297-02a.htm";
				}
				return "32297-02b.htm";
			}
		}
		return event;
	}
	
	public Falk(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(FALK);
		addStartNpc(FALK);
		addTalkId(FALK);
	}
	
	public static void main(String[] args)
	{
		new Falk(-1, "Falk", "hellbound");
	}
}