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

import l2e.gameserver.model.L2CommandChannel;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

public class _10270_BirthOfTheSeed extends Quest
{
	private static final String qn = "_10270_BirthOfTheSeed";
	
	private static final int _flenos 	= 32563;
	private static final int _klodekus 	= 25665;
	private static final int _klanikus 	= 25666;
	private static final int _cohemenes 	= 25634; 
	private static final int _jinbi 	= 32566;
	private static final int _relrikia 	= 32567;
	private static final int _artius 	= 32559;
	
	public _10270_BirthOfTheSeed(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_flenos);
		addTalkId(_flenos);
		addTalkId(_relrikia);
		addTalkId(_jinbi);
		addTalkId(_artius);

		addKillId(_klanikus);
		addKillId(_klodekus);
		addKillId(_cohemenes);

		questItemIds = new int[] {13868,13869,13870};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState qs = player.getQuestState(qn);
        	if (qs == null)
			return htmltext;

		if (event.equalsIgnoreCase("32563-05.htm"))
		{
			qs.setState(State.STARTED);
			qs.set("cond", "1");
			qs.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32559-03.htm"))
		{
			qs.set("cond", "2");
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32559-09.htm"))
		{
			qs.set("cond", "4");
			qs.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32559-13.htm"))
		{
			qs.setState(State.COMPLETED);
			qs.playSound("ItemSound.quest_finish");
			qs.exitQuest(false);
			qs.addExpAndSp(251602, 25244);
			qs.giveAdena(41677, true);
		}
		else if (event.equalsIgnoreCase("32566-05.htm"))
		{
			if (qs.getQuestItemsCount(57) < 10000)
			{
				htmltext = "32566-04a.htm";
			}
			else
			{
				qs.takeItems(57, 10000);
				qs.set("pay", "1");
			}
		}
		else if (event.equalsIgnoreCase("32567-05.htm"))
		{
			qs.set("cond", "5");
			qs.playSound("ItemSound.quest_middle");
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
        	String htmltext = getNoQuestMsg(player);
        	QuestState qs = player.getQuestState(qn);
        	if (qs == null) 
			return htmltext;

		int npcId = npc.getId();
		int cond = qs.getInt("cond");
		
		switch(npcId)
		{
			case _flenos:
				switch (qs.getState())
				{
					case State.CREATED:
						if (player.getLevel() < 75)
							htmltext = "32563-02.htm";
						else
							htmltext = "32563-01.htm";
						break;
						
					case State.STARTED:
						if (cond == 1)
							htmltext = "32563-06.htm";
						break;
						
					case State.COMPLETED:
						htmltext = "32563-03.htm";
						break;
				}
				break;
			case _artius:
				if (cond == 1)
					htmltext = "32559-01.htm";
				else if (cond == 2)
				{
					if (qs.getQuestItemsCount(13868) < 1
							&& qs.getQuestItemsCount(13869) < 1
							&& qs.getQuestItemsCount(13870) < 1)
						htmltext = "32559-04.htm";
					
					else if (qs.getQuestItemsCount(13868) +
							qs.getQuestItemsCount(13869) +
							qs.getQuestItemsCount(13870) < 3)
						htmltext = "32559-05.htm";
					
					else if (qs.getQuestItemsCount(13868) == 1
							&& qs.getQuestItemsCount(13869) == 1
							&& qs.getQuestItemsCount(13870) == 1)
					{
						htmltext = "32559-06.htm";
						qs.takeItems(13868, 1);
						qs.takeItems(13869, 1);
						qs.takeItems(13870, 1);
						qs.set("cond","3");
						qs.playSound("ItemSound.quest_middle");
					}
				}
				else if (cond == 3 || cond == 4)
					htmltext = "32559-07.htm";
				else if (cond == 5)
					htmltext = "32559-12.htm";
				if (qs.getState() == State.COMPLETED)
					htmltext = "32559-02.htm";
				break;
			case _jinbi:
				if (cond < 4)
					htmltext = "32566-02.htm";
				else if (cond == 4)
				{
					if (qs.getInt("pay") == 1)
					{
						htmltext = "32566-10.htm";
					}
					else
					{
						htmltext = "32566-04.htm";
					}
				}
				else if (cond > 4)
					htmltext = "32566-12.htm";
				
				break;
			case _relrikia:
				if (cond == 4)
				{
					htmltext = "32567-01.htm";
				}
				else if (cond == 5)
				{
					htmltext = "32567-07.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon) 
	{
		if (npc.getId() == _klanikus)
		{
			if (player.getParty() != null)
			{
				L2Party party = player.getParty();
				if (party.getCommandChannel() != null)
				{
					L2CommandChannel cc =party.getCommandChannel();
					for (L2PcInstance partyMember : cc.getMembers())
					{
						QuestState qs = partyMember.getQuestState(qn);
						if (qs != null && qs.getInt("cond") == 2)
							qs.giveItems(13869, 1);
					}
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						QuestState qs = partyMember.getQuestState(qn);
						if (qs != null && qs.getInt("cond") == 2)
							qs.giveItems(13869, 1);
					}
				}
			}
			else
			{
				QuestState qs = player.getQuestState(qn);
				if (qs != null && qs.getInt("cond") == 2)
					qs.giveItems(13869, 1);
			}
		}
		else if (npc.getId() == _klodekus)
		{
			if (player.getParty() != null)
			{
				L2Party party = player.getParty();
				if (party.getCommandChannel() != null)
				{
					L2CommandChannel cc =party.getCommandChannel();
					for (L2PcInstance partyMember : cc.getMembers())
					{
						QuestState qs = partyMember.getQuestState(qn);
						if (qs != null && qs.getInt("cond") == 2)
							qs.giveItems(13868, 1);
					}
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						QuestState qs = partyMember.getQuestState(qn);
						if (qs != null && qs.getInt("cond") == 2)
							qs.giveItems(13868, 1);
					}
				}
			}
			else
			{
				QuestState qs = player.getQuestState(qn);
				if (qs != null && qs.getInt("cond") == 2)
					qs.giveItems(13868, 1);
			}
		}
		else if (npc.getId() == _cohemenes)
		{
			if (player.getParty() != null)
			{
				L2Party party = player.getParty();
				if (party.getCommandChannel() != null)
				{
					L2CommandChannel cc =party.getCommandChannel();
					for (L2PcInstance partyMember : cc.getMembers())
					{
						QuestState qs = partyMember.getQuestState(qn);
						if (qs != null && qs.getInt("cond") == 2)
							qs.giveItems(13870, 1);
					}
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						QuestState qs = partyMember.getQuestState(qn);
						if (qs != null && qs.getInt("cond") == 2)
							qs.giveItems(13870, 1);
					}
				}
			}
			else
			{
				QuestState qs = player.getQuestState(qn);
				if (qs != null && qs.getInt("cond") == 2)
					qs.giveItems(13870, 1);
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _10270_BirthOfTheSeed(10270, qn, "");
	}
}