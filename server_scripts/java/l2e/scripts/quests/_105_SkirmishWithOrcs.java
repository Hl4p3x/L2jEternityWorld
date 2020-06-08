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
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Created by LordWinter 09.08.2011
 * Based on L2J Eternity-World
 */
public class _105_SkirmishWithOrcs extends Quest
{
	private static final String qn = "_105_SkirmishWithOrcs";

	// NPC
	private static final int KENDELL 		= 30218;

	// QuestItem
	private static final int KENDNELLS_ORDER1 	= 1836;
	private static final int KENDNELLS_ORDER2 	= 1837;
	private static final int KENDNELLS_ORDER3 	= 1838;
	private static final int KENDNELLS_ORDER4 	= 1839;
	private static final int KENDNELLS_ORDER5 	= 1840;
	private static final int KENDNELLS_ORDER6 	= 1841;
	private static final int KENDNELLS_ORDER7 	= 1842;
	private static final int KENDNELLS_ORDER8 	= 1843;
	private static final int KABOO_CHIEF_TORC1 	= 1844;
	private static final int KABOO_CHIEF_TORC2 	= 1845;
	private static final int RED_SUNSET_SWORD 	= 981;
	private static final int RED_SUNSET_STAFF 	= 754;

	// MOBS
	private static final int KabooChiefUoph 	= 27059;
	private static final int KabooChiefKracha 	= 27060;
	private static final int KabooChiefBatoh 	= 27061;
	private static final int KabooChiefTanukia 	= 27062;
	private static final int KabooChiefTurel 	= 27064;
	private static final int KabooChiefRoko 	= 27065;
	private static final int KabooChiefKamut 	= 27067;
	private static final int KabooChiefMurtika 	= 27068;

	// REWARDS
	private final static int SPIRITSHOT_NO_GRADE_FOR_BEGINNERS = 5790;
	private final static int SOULSHOT_NO_GRADE_FOR_BEGINNERS   = 5789;
	private final static int LESSER_HEALING_POT 		   = 1060;

	private final static int[] MOBS = new int[] { KabooChiefUoph, KabooChiefKracha, KabooChiefBatoh, KabooChiefTanukia, KabooChiefTurel, KabooChiefRoko, KabooChiefKamut, KabooChiefMurtika };

	public _105_SkirmishWithOrcs(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(KENDELL);
		addTalkId(KENDELL);

                for (int npcId : MOBS)
                    addKillId(npcId);

		questItemIds = new int[] { KENDNELLS_ORDER1, KENDNELLS_ORDER2, KENDNELLS_ORDER3, KENDNELLS_ORDER4, KENDNELLS_ORDER5, KENDNELLS_ORDER6, KENDNELLS_ORDER7, KENDNELLS_ORDER8, KABOO_CHIEF_TORC1, KABOO_CHIEF_TORC2 };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{		
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("30218-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			if(st.getQuestItemsCount(KENDNELLS_ORDER1) + st.getQuestItemsCount(KENDNELLS_ORDER2) + st.getQuestItemsCount(KENDNELLS_ORDER3) + st.getQuestItemsCount(KENDNELLS_ORDER4) == 0)
			{
				int n = getRandom(100);
				if(n < 25)
					st.giveItems(KENDNELLS_ORDER1, 1);
				else if(n < 50)
					st.giveItems(KENDNELLS_ORDER2, 1);
				else if(n < 75)
					st.giveItems(KENDNELLS_ORDER3, 1);
				else
					st.giveItems(KENDNELLS_ORDER4, 1);
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

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		int cond = st.getInt("cond");

		if(cond == 0)
		{
			if(player.getRace().ordinal() != 1)
			{
				htmltext = "30218-00.htm";
				st.exitQuest(true);
			}
			else if(player.getLevel() < 10)
			{
				htmltext = "30218-10.htm";
				st.exitQuest(true);
			}
			else
				htmltext = "30218-02.htm";
		}
		else if(cond == 1 && st.getQuestItemsCount(KENDNELLS_ORDER1) + st.getQuestItemsCount(KENDNELLS_ORDER2) + st.getQuestItemsCount(KENDNELLS_ORDER3) + st.getQuestItemsCount(KENDNELLS_ORDER4) != 0)
			htmltext = "30218-05.htm";
		else if(cond == 2 && st.getQuestItemsCount(KABOO_CHIEF_TORC1) != 0)
		{
			htmltext = "30218-06.htm";
			if(st.getQuestItemsCount(KENDNELLS_ORDER1) > 0)
				st.takeItems(KENDNELLS_ORDER1, -1);
			if(st.getQuestItemsCount(KENDNELLS_ORDER2) > 0)
				st.takeItems(KENDNELLS_ORDER2, -1);
			if(st.getQuestItemsCount(KENDNELLS_ORDER3) > 0)
				st.takeItems(KENDNELLS_ORDER3, -1);
			if(st.getQuestItemsCount(KENDNELLS_ORDER4) > 0)
				st.takeItems(KENDNELLS_ORDER4, -1);
			st.takeItems(KABOO_CHIEF_TORC1, 1);
			int n = getRandom(100);
			if(n < 25)
				st.giveItems(KENDNELLS_ORDER5, 1);
			else if(n < 50)
				st.giveItems(KENDNELLS_ORDER6, 1);
			else if(n < 75)
				st.giveItems(KENDNELLS_ORDER7, 1);
			else
				st.giveItems(KENDNELLS_ORDER8, 1);
			st.set("cond", "3");
			st.setState(State.STARTED);
		}
		else if(cond == 3 && st.getQuestItemsCount(KENDNELLS_ORDER5) + st.getQuestItemsCount(KENDNELLS_ORDER6) + st.getQuestItemsCount(KENDNELLS_ORDER7) + st.getQuestItemsCount(KENDNELLS_ORDER8) == 1)
			htmltext = "30218-07.htm";
		else if(cond == 4 && st.getQuestItemsCount(KABOO_CHIEF_TORC2) > 0)
		{
			htmltext = "30218-08.htm";
			if(st.getQuestItemsCount(KENDNELLS_ORDER5) > 0)
				st.takeItems(KENDNELLS_ORDER5, -1);
			if(st.getQuestItemsCount(KENDNELLS_ORDER6) > 0)
				st.takeItems(KENDNELLS_ORDER6, -1);
			if(st.getQuestItemsCount(KENDNELLS_ORDER7) > 0)
				st.takeItems(KENDNELLS_ORDER7, -1);
			if(st.getQuestItemsCount(KENDNELLS_ORDER8) > 0)
				st.takeItems(KENDNELLS_ORDER8, -1);
			for(int ECHO_CHRYTSAL = 4412; ECHO_CHRYTSAL <= 4417; ECHO_CHRYTSAL++)
				st.giveItems(ECHO_CHRYTSAL, 10);
			st.takeItems(KABOO_CHIEF_TORC2, -1);
			st.giveItems(LESSER_HEALING_POT, 100);

			if(player.getClassId().isMage())
			{
				st.giveItems(RED_SUNSET_STAFF, 1);
				st.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 3000);
				st.playTutorialVoice("tutorial_voice_027");
			}
			else
			{
				st.giveItems(RED_SUNSET_SWORD, 1);
				st.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 7000);
				st.playTutorialVoice("tutorial_voice_026");
			}
			st.giveItems(57, 17599);
			st.addExpAndSp(41478,3555);
			player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message3", player.getLang())).toString()), 3000));
			st.unset("cond");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
			player.sendPacket(new SocialAction(player.getObjectId(), 3));
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

		if(cond == 1 && st.getQuestItemsCount(KABOO_CHIEF_TORC1) == 0)
		{
			if(npcId == KabooChiefUoph && st.getQuestItemsCount(KENDNELLS_ORDER1) > 0)
				st.giveItems(KABOO_CHIEF_TORC1, 1);
			else if(npcId == KabooChiefKracha && st.getQuestItemsCount(KENDNELLS_ORDER2) > 0)
				st.giveItems(KABOO_CHIEF_TORC1, 1);
			else if(npcId == KabooChiefBatoh && st.getQuestItemsCount(KENDNELLS_ORDER3) > 0)
				st.giveItems(KABOO_CHIEF_TORC1, 1);
			else if(npcId == KabooChiefTanukia && st.getQuestItemsCount(KENDNELLS_ORDER4) > 0)
				st.giveItems(KABOO_CHIEF_TORC1, 1);
			if(st.getQuestItemsCount(KABOO_CHIEF_TORC1) > 0)
			{
				st.set("cond", "2");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if(cond == 3 && st.getQuestItemsCount(KABOO_CHIEF_TORC2) == 0)
		{
			if(npcId == KabooChiefTurel && st.getQuestItemsCount(KENDNELLS_ORDER5) > 0)
				st.giveItems(KABOO_CHIEF_TORC2, 1);
			else if(npcId == KabooChiefRoko && st.getQuestItemsCount(KENDNELLS_ORDER6) > 0)
				st.giveItems(KABOO_CHIEF_TORC2, 1);
			else if(npcId == KabooChiefKamut && st.getQuestItemsCount(KENDNELLS_ORDER7) > 0)
				st.giveItems(KABOO_CHIEF_TORC2, 1);
			else if(npcId == KabooChiefMurtika && st.getQuestItemsCount(KENDNELLS_ORDER8) > 0)
				st.giveItems(KABOO_CHIEF_TORC2, 1);
			if(st.getQuestItemsCount(KABOO_CHIEF_TORC2) > 0)
			{
				st.set("cond", "4");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}

	public static void main(String[] args)
    	{
    		new _105_SkirmishWithOrcs(105, qn, "");
    	}
}