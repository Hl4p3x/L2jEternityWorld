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
import l2e.util.Rnd;

public class _293_HiddenVein extends Quest
{
	private static final String qn = "_293_HiddenVein";

	// NPCs
	private static int Filaur = 30535;
	private static int Chichirin = 30539;

	// Mobs
	private static int Utuku_Orc = 20446;
	private static int Utuku_Orc_Archer = 20447;
	private static int Utuku_Orc_Grunt = 20448;

	// Quest Items
	private static int Chrysolite_Ore = 1488;
	private static int Torn_Map_Fragment = 1489;
	private static int Hidden_Ore_Map = 1490;

	// Newbie section
	private static final int NEWBIE_REWARD = 4;
	private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;

	public _293_HiddenVein(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Filaur);
		addTalkId(Filaur);
		addTalkId(Chichirin);

		addKillId(Utuku_Orc);
		addKillId(Utuku_Orc_Archer);
		addKillId(Utuku_Orc_Grunt);

		questItemIds = new int[] { Chrysolite_Ore, Torn_Map_Fragment, Hidden_Ore_Map };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("30535-03.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30535-06.htm"))
		{
			st.takeItems(Torn_Map_Fragment,-1);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		else if(event.equalsIgnoreCase("30539-02.htm"))
		{
			if(st.getQuestItemsCount(Torn_Map_Fragment) >= 4)
			{
				htmltext = "30539-03.htm";
				st.takeItems(Torn_Map_Fragment, 4);
				st.giveItems(Hidden_Ore_Map, 1);
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;

		int id = st.getState();
		int npcId = npc.getId();

		if(npcId != Filaur && id != State.STARTED)
			return htmltext;

		if(id == State.CREATED)
			st.set("cond","0");

		if(npcId == Filaur)
		{
			if(st.getInt("cond") == 0)
			{
				if(player.getRace().ordinal() != 4)
				{
					st.exitQuest(true);
					htmltext = "30535-00.htm";
				}
				else if(player.getLevel() >= 6)
					htmltext = "30535-02.htm";
				else
				{
					st.exitQuest(true);
					htmltext = "30535-01.htm";
				}
			}
			else
			{
				long Chrysolite_Ore_count = st.getQuestItemsCount(Chrysolite_Ore);
				long Hidden_Ore_Map_count = st.getQuestItemsCount(Hidden_Ore_Map);

				long reward = st.getQuestItemsCount(Chrysolite_Ore) * 10 + st.getQuestItemsCount(Hidden_Ore_Map) * 1000L;
				if(reward == 0)
					htmltext = "30535-04.htm";

				if(Chrysolite_Ore_count > 0)
					st.takeItems(Chrysolite_Ore, -1);
				if(Hidden_Ore_Map_count > 0)
					st.takeItems(Hidden_Ore_Map, -1);
				st.giveItems(57, reward);
	
				int newbie = player.getNewbie();

				if ((newbie | NEWBIE_REWARD) != newbie)
				{
					player.setNewbie(newbie|NEWBIE_REWARD);

					if(player.getClassId().isMage())
					{
             					st.playTutorialVoice("tutorial_voice_027");
             					st.giveItems(SPIRITSHOT_FOR_BEGINNERS,3000);
	     					player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2", player.getLang())).toString()), 3000));
					}
					else
					{
             					st.playTutorialVoice("tutorial_voice_026");
             					st.giveItems(SOULSHOT_FOR_BEGINNERS,6000);
	     					player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message2a", player.getLang())).toString()), 3000));
					}
				}
				return Chrysolite_Ore_count > 0 && Hidden_Ore_Map_count > 0 ? "30535-09.htm" : Hidden_Ore_Map_count > 0 ? "30535-08.htm" : "30535-05.htm";
			}
		}

		if(npcId == Chichirin)
			return "30539-01.htm";

		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		if(Rnd.getChance(5))
		{
			st.giveItems(Torn_Map_Fragment, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if(Rnd.getChance(45))
		{
			st.giveItems(Chrysolite_Ore, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _293_HiddenVein(293, qn, "");
	}
}