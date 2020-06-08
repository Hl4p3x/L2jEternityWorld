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

import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Created by LordWinter 03.08.2011
 * Based on L2J Eternity-World
 */
public class _281_HeadForTheHills extends Quest
{
	private static final String qn 		= "_281_HeadForTheHills";

	// NPC'S
	private final int MARCELA 		= 32173;

	// MOBS
	public final int GreenGoblin 		= 22234;
	public final int MountainWerewolf 	= 22235;
	public final int MuertosArcher 		= 22236;
	public final int MountainFungus 	= 22237;
	public final int MountainWerewolfChief 	= 22238;
	public final int MuertosGuard 		= 22239;

	// ITEM'S
	private final int HILLS = 9796;
	private final int SOULSHOT_FOR_BEGINNERS    = 5789;
	private final int SPIRITSHOT_FOR_BEGINNERS  = 5790;
	private final int[] REWARDS 		    = {736,876,115};

	//DROP COND
	public final int[][] DROPLIST = 
	{
		{ GreenGoblin, 70 },
		{ MountainWerewolf, 75 },
		{ MuertosArcher, 80 },
		{ MountainFungus, 70 },
		{ MountainWerewolfChief, 90 },
		{ MuertosGuard, 90 } 
	};

	public _281_HeadForTheHills(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(MARCELA);
		addTalkId(MARCELA);
		for (int[] mobIds : DROPLIST)
			addKillId(mobIds[0]);

		questItemIds = new int[] {HILLS};
    	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		long hills = st.getQuestItemsCount(HILLS);
		int onlyone = st.getInt("onlyone");

		if(event.equalsIgnoreCase("32173-03.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("32173-06.htm"))
		{
			if (onlyone == 0)
			{
				if (player.isMageClass())
				{
					st.giveItems(SPIRITSHOT_FOR_BEGINNERS,3000);
					player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2", player.getLang())).toString()), 3000));
				}
				else
				{
					st.giveItems(SOULSHOT_FOR_BEGINNERS,6000);
					player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2a", player.getLang())).toString()), 3000));
				}
				st.playTutorialVoice("tutorial_voice_026");
				st.set("onlyone","1");
			}
			if (hills > 20)
				st.giveItems(57,hills*23+400);
			else
				st.giveItems(57,hills*23);
			st.takeItems(HILLS,-1);
		}
		else if(event.equalsIgnoreCase("32173-07.htm"))
		{
			if (hills < 50)
				htmltext = "32173-07a.htm";
			else
			{
				int rnd = getRandom(REWARDS.length);
				int REWARD = REWARDS[rnd];
				if (onlyone == 0)
				{
					if (player.isMageClass())
					{
						st.giveItems(SPIRITSHOT_FOR_BEGINNERS,3000);
						player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2", player.getLang())).toString()), 3000));
					}
					else
					{
						st.giveItems(SOULSHOT_FOR_BEGINNERS,6000);
						player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2a", player.getLang())).toString()), 3000));
					}
					st.playTutorialVoice("tutorial_voice_026");
					st.set("onlyone","1");
				}
				st.giveItems(REWARD,1);
				st.takeItems(HILLS,50);
			}
		}
		else if(event.equalsIgnoreCase("32173-09.htm"))
		{
			st.takeItems(HILLS,-1);
			st.exitQuest(true);
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

		int npcId = npc.getId();
		byte state = st.getState();

		if (npcId == MARCELA)
		{
			if (state == State.CREATED)
			{
				if (player.getLevel() < 6)
				{
					htmltext = "32173-02.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "32173-01.htm";
			}
			else if (state == State.STARTED)
			{
				if (st.getQuestItemsCount(HILLS) > 0)
					htmltext = "32173-05.htm";
				else
					htmltext = "32173-04.htm";
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
		int cond = st.getInt("cond");

		if(cond != 1)
			return null;
		for(int[] element : DROPLIST)
		{
			if(npcId == element[0] && getRandom(100) < element[1])
			{
				st.giveItems(HILLS, 1);
				return null;
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _281_HeadForTheHills(281, qn, "");
	}
}