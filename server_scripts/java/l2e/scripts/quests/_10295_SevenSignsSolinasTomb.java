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

import org.apache.commons.lang.ArrayUtils;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 23.05.2012 Based on L2J Eternity-World
 */
public class _10295_SevenSignsSolinasTomb extends Quest
{
	private static final String qn = "_10295_SevenSignsSolinasTomb";
	
	// NPCs
	private static final int EVIL = 32792;
	private static final int ELCARDIA = 32787;
	private static final int SOLINA = 32793;
	private static final int TELEPORT_DEVICE = 32820;
	
	private static final int ALTAR_OF_HALLOWS_1 = 32857;
	private static final int ALTAR_OF_HALLOWS_2 = 32858;
	private static final int ALTAR_OF_HALLOWS_3 = 32859;
	private static final int ALTAR_OF_HALLOWS_4 = 32860;
	private static final int TELEPORT_DEVICE_2 = 32837;
	private static final int TELEPORT_DEVICE_3 = 32842;
	
	private static final int[] NPCs =
	{
		EVIL,
		ELCARDIA,
		TELEPORT_DEVICE,
		ALTAR_OF_HALLOWS_1,
		ALTAR_OF_HALLOWS_2,
		ALTAR_OF_HALLOWS_3,
		ALTAR_OF_HALLOWS_4,
		TELEPORT_DEVICE_2,
		TELEPORT_DEVICE_3,
		SOLINA
	};
	
	// ITEMs
	private static int SCROLL_OF_ABSTINENCE = 17228;
	private static int SHIELD_OF_SACRIFICE = 17229;
	private static int SWORD_OF_HOLYSPIRIT = 17230;
	private static int STAFF_OF_BLESSING = 17231;
	
	// MOBs
	private static final int[] SolinaGuardians =
	{
		18952,
		18953,
		18954,
		18955
	};
	
	public _10295_SevenSignsSolinasTomb(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(EVIL);
		
		for (int id : NPCs)
		{
			addTalkId(id);
		}
		
		for (int i : SolinaGuardians)
		{
			addKillId(i);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32792-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32857-02.htm"))
		{
			if (st.getQuestItemsCount(STAFF_OF_BLESSING) == 0)
			{
				st.giveItems(STAFF_OF_BLESSING, 1);
			}
			else
			{
				htmltext = "empty-atlar.htm";
			}
		}
		else if (event.equalsIgnoreCase("32859-02.htm"))
		{
			if (st.getQuestItemsCount(SCROLL_OF_ABSTINENCE) == 0)
			{
				st.giveItems(SCROLL_OF_ABSTINENCE, 1);
			}
			else
			{
				htmltext = "empty-atlar.htm";
			}
		}
		else if (event.equalsIgnoreCase("32858-02.htm"))
		{
			if (st.getQuestItemsCount(SWORD_OF_HOLYSPIRIT) == 0)
			{
				st.giveItems(SWORD_OF_HOLYSPIRIT, 1);
			}
			else
			{
				htmltext = "empty-atlar.htm";
			}
		}
		else if (event.equalsIgnoreCase("32860-02.htm"))
		{
			if (st.getQuestItemsCount(SHIELD_OF_SACRIFICE) == 0)
			{
				st.giveItems(SHIELD_OF_SACRIFICE, 1);
			}
			else
			{
				htmltext = "empty-atlar.htm";
			}
		}
		else if (event.equalsIgnoreCase("32793-04.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32793-08.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int ac = st.getInt("active");
		
		if (player.isSubClassActive())
		{
			return "no_subclass-allowed.htm";
		}
		
		if (st.getState() == State.CREATED)
		{
			if (npcId == EVIL)
			{
				QuestState qs = player.getQuestState("_10294_SevenSignToTheMonastery");
				if (cond == 0)
				{
					if ((player.getLevel() >= 81) && (qs != null) && qs.isCompleted())
					{
						htmltext = "32792-01.htm";
					}
					else
					{
						htmltext = "32792-00a.htm";
						st.exitQuest(true);
					}
				}
			}
		}
		else if (st.getState() == State.STARTED)
		{
			if (npcId == EVIL)
			{
				if (cond == 1)
				{
					htmltext = "32792-06.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32792-07.htm";
				}
				else if (cond == 3)
				{
					if (player.getLevel() >= 81)
					{
						htmltext = "32792-08.htm";
						st.addExpAndSp(125000000, 12500000);
						st.setState(State.COMPLETED);
						st.unset("cond");
						st.unset("active");
						st.unset("first");
						st.unset("second");
						st.unset("third");
						st.unset("fourth");
						st.unset("firstgroup");
						st.unset("secondgroup");
						st.unset("thirdgroup");
						st.unset("fourthgroup");
						st.unset("activity");
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
					else
					{
						htmltext = "32792-00.htm";
					}
				}
			}
			else if (npcId == ELCARDIA)
			{
				htmltext = "32787-01.htm";
			}
			else if (npcId == TELEPORT_DEVICE)
			{
				if (ac == 1)
				{
					htmltext = "32820-02.htm";
				}
				else
				{
					htmltext = "32820-01.htm";
				}
				
			}
			else if (npcId == TELEPORT_DEVICE_2)
			{
				htmltext = "32837-01.htm";
			}
			else if (npcId == TELEPORT_DEVICE_3)
			{
				htmltext = "32842-01.htm";
			}
			else if (npcId == ALTAR_OF_HALLOWS_1)
			{
				htmltext = "32857-01.htm";
			}
			else if (npcId == ALTAR_OF_HALLOWS_2)
			{
				htmltext = "32858-01.htm";
			}
			else if (npcId == ALTAR_OF_HALLOWS_3)
			{
				htmltext = "32859-01.htm";
			}
			else if (npcId == ALTAR_OF_HALLOWS_4)
			{
				htmltext = "32860-01.htm";
			}
			else if (npcId == SOLINA)
			{
				if (cond == 1)
				{
					htmltext = "32793-01.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32793-04.htm";
				}
				else if (cond == 3)
				{
					htmltext = "32793-08.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		int npcId = npc.getId();
		int first = st.getInt("first");
		int second = st.getInt("second");
		int third = st.getInt("third");
		int fourth = st.getInt("fourth");
		
		if (ArrayUtils.contains(SolinaGuardians, npcId))
		{
			switch (npcId)
			{
				case 18952:
					st.set("first", "1");
					break;
				case 18953:
					st.set("second", "1");
					break;
				case 18954:
					st.set("third", "1");
					break;
				case 18955:
					st.set("fourth", "1");
					break;
			}
			
			if ((first == 1) && (second == 1) && (third == 1) && (fourth == 1))
			{
				player.showQuestMovie(27);
				st.set("active", "1");
			}
		}
		return null;
	}
	
	public static void main(String args[])
	{
		new _10295_SevenSignsSolinasTomb(10295, qn, "");
	}
}