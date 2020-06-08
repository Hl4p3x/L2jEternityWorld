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

import java.util.Calendar;

import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.Util;

/**
 * Fixed by L2J Etermity-World
 */
public class _458_PerfectForm extends Quest
{
	private static final String qn = "_458_PerfectForm";

	// NPCs
	private static final int _kelia = 32768;

	// Mobs 
	private static final int[] _mobs1 = {18878, 18879};
	private static final int[] _mobs2 = {18885, 18886};
	private static final int[] _mobs3 = {18892, 18893};
	private static final int[] _mobs4 = {18899, 18900};

	/*
	 * Reset time for Quest
	 */
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;

	public int mobs1Count = 0;
	public int mobs2Count = 0;
	public int mobs3Count = 0;
	public int mobs4Count = 0;
	public int mobsoverhitCount = 0;

	private static final int SPICE1 = 15482;
	private static final int SPICE2 = 15483;

	private static final int[][] _rewards1 =
	{{10397, 2}, {10398, 2}, {10399, 2}, {10400, 2}, {10401, 2}, {10402, 2}, {10403, 2}, {10404, 2}, {10405, 2}};

	private static final int[][] _rewards2 =
	{{10397, 5}, {10398, 5}, {10399, 5}, {10400, 5}, {10401, 5}, {10402, 5}, {10403, 5}, {10404, 5}, {10405, 5}};

	private static final int[][] _rewards3 =
	{{10373, 1}, {10374, 1}, {10375, 1}, {10376, 1}, {10377, 1}, {10378, 1}, {10379, 1}, {10380, 1}, {10381, 1}};
	
	public _458_PerfectForm(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_kelia);
		addTalkId(_kelia);
		for (int i : _mobs1)
			addKillId(i);
		for (int i : _mobs2)
			addKillId(i);
		for (int i : _mobs3)
			addKillId(i);
		for (int i : _mobs4)
			addKillId(i);
	}

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getId() == _kelia)
		{
			if (event.equalsIgnoreCase("32768-12.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32768-16.htm"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());

				if ((mobsoverhitCount >= 0) && (mobsoverhitCount <= 6))
				{
					html.setFile(player.getLang(), "32768-16c.htm");
					html.replace("%overhits%", "" + mobsoverhitCount);
				}
				else if ((mobsoverhitCount >= 7) && (mobsoverhitCount <= 19))
				{
					html.setFile(player.getLang(), "32768-16b.htm");
					html.replace("%overhits%", "" + mobsoverhitCount);
				}
				else if (mobsoverhitCount >= 20)
				{
					html.setFile(player.getLang(), "32768-16a.htm");
					html.replace("%overhits%", "" + mobsoverhitCount);
				}
			}
			else if (event.equalsIgnoreCase("32768-17.htm"))
			{
				if ((mobsoverhitCount >= 0) && (mobsoverhitCount <= 6))
				{
					st.unset("cond");
					st.giveItems(_rewards1[getRandom(_rewards1.length)][0],_rewards1[getRandom(_rewards1.length)][1]*(int) Config.RATE_QUEST_REWARD);
					st.giveItems(SPICE1,(int) Config.RATE_QUEST_REWARD);
					st.giveItems(SPICE2,(int) Config.RATE_QUEST_REWARD);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
					mobs1Count = 0;
					mobs2Count = 0;
					mobs3Count = 0;
					mobs4Count = 0;
					mobsoverhitCount = 0;

					Calendar reDo = Calendar.getInstance();
					reDo.set(Calendar.MINUTE, RESET_MIN);
					if (reDo.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
						reDo.add(Calendar.DATE, 1);
					reDo.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
					st.set("reDoTime", String.valueOf(reDo.getTimeInMillis()));
				}
				else if ((mobsoverhitCount >= 7) && (mobsoverhitCount <= 19))
				{
					st.unset("cond");
					st.giveItems(_rewards2[getRandom(_rewards2.length)][0],_rewards2[getRandom(_rewards2.length)][1]*(int) Config.RATE_QUEST_REWARD);
					st.giveItems(SPICE1,(int) Config.RATE_QUEST_REWARD);
					st.giveItems(SPICE2,(int) Config.RATE_QUEST_REWARD);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
					mobs1Count = 0;
					mobs2Count = 0;
					mobs3Count = 0;
					mobs4Count = 0;
					mobsoverhitCount = 0;

					Calendar reDo = Calendar.getInstance();
					reDo.set(Calendar.MINUTE, RESET_MIN);
					if (reDo.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
						reDo.add(Calendar.DATE, 1);
					reDo.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
					st.set("reDoTime", String.valueOf(reDo.getTimeInMillis()));
				}
				else if (mobsoverhitCount >= 20)
				{
					st.unset("cond");
					st.giveItems(_rewards3[getRandom(_rewards3.length)][0],_rewards3[getRandom(_rewards3.length)][1]*(int) Config.RATE_QUEST_REWARD);
					st.giveItems(SPICE1,(int) Config.RATE_QUEST_REWARD);
					st.giveItems(SPICE2,(int) Config.RATE_QUEST_REWARD);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
					mobs1Count = 0;
					mobs2Count = 0;
					mobs3Count = 0;
					mobs4Count = 0;
					mobsoverhitCount = 0;

					Calendar reDo = Calendar.getInstance();
					reDo.set(Calendar.MINUTE, RESET_MIN);
					if (reDo.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
						reDo.add(Calendar.DATE, 1);
					reDo.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
					st.set("reDoTime", String.valueOf(reDo.getTimeInMillis()));
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (npc.getId() == _kelia)
		{
			switch(st.getState())
			{
				case State.CREATED :
					if (player.getLevel() >= 82)
						htmltext = "32768-01.htm";
					else
						htmltext = "32768-03.htm";
					break;
				case State.STARTED :
					if (st.getInt("cond") == 1)
					{
						if ((mobs1Count == 0) && (mobs2Count == 0) && (mobs3Count == 0) && (mobs4Count == 0))
						{
							htmltext = "32768-13.htm";
						}
						else
							htmltext = "32768-14.htm";
					}
					else if (st.getInt("cond") == 2)
					{
						htmltext = "32768-15.htm";
					}
					break;
				case State.COMPLETED :
					Long reDoTime = Long.parseLong(st.get("reDoTime"));
					if (reDoTime > System.currentTimeMillis())
						htmltext = "32768-02.htm";
					else
					{
						st.setState(State.CREATED);
						if (player.getLevel() >= 82)
							htmltext = "32768-01.htm";
						else
							htmltext = "32768-03.htm";
					}
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		int npcId = npc.getId();

		if (st.getInt("cond") == 1)
		{
			if ((mobs1Count >= 10) && (mobs2Count >= 10) && (mobs3Count >= 10) && (mobs4Count >= 10))
			{
				st.set("cond", "2");
			}
			else
			{
				if (Util.contains(_mobs1, npcId))
				{
					mobs1Count++;
					if (((L2Attackable) npc).isOverhit())
					{
						mobsoverhitCount++;
					}
				}
				else if (Util.contains(_mobs2, npcId))
				{
					mobs2Count++;
					if (((L2Attackable) npc).isOverhit())
					{
						mobsoverhitCount++;
					}
				}
				else if (Util.contains(_mobs3, npcId))
				{
					mobs3Count++;
					if (((L2Attackable) npc).isOverhit())
					{
						mobsoverhitCount++;
					}
				}
				else if (Util.contains(_mobs4, npcId))
				{
					mobs4Count++;
					if (((L2Attackable) npc).isOverhit())
					{
						mobsoverhitCount++;
					}
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _458_PerfectForm(458, qn, "");
	}
}