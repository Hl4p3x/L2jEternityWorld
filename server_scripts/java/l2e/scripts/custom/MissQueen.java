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

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class MissQueen extends Quest
{
	private static final String qn = "MissQueen";

	private static final int COUPNE_ONE = 7832;
	private static final int COUPNE_TWO = 7833;

	private static final int[] NPCs =
	{
			31760, 31761, 31762, 31763, 31764, 31765, 31766
	};

	// enable/disable coupon give
	private static boolean QUEEN_ENABLED = false;
	private static final int NEWBIE_REWARD = 16;
	private static final int TRAVELER_REWARD = 32;

	public MissQueen(int id, String name, String descr)
	{
		super(id, name, descr);

		for (int i : NPCs)
		{
			addStartNpc(i);
			addFirstTalkId(i);
			addTalkId(i);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		if (!QUEEN_ENABLED)
		{
			return htmltext;
		}
		
		QuestState st = player.getQuestState(qn);
		int newbie = player.getNewbie();
		int level = player.getLevel();
		int occupation_level = player.getClassId().level();
		int pkkills = player.getPkKills();
		if (event.equals("newbie_give_coupon"))
		{
			/*
			 * TODO: check if this is the very first character for this account would need a bit of SQL, or a core method to determine it. This condition should be stored by the core in the account_data table upon character creation.
			 */
			if (level >= 6 && level <= 25 && pkkills == 0 && occupation_level == 0)
			{
				if ((newbie | NEWBIE_REWARD) != newbie)
				{
					player.setNewbie(newbie | NEWBIE_REWARD);
					st.giveItems(COUPNE_ONE, 1);
					htmltext = "31760-2.htm"; // here's the coupon you requested
				}
				else
					htmltext = "31760-1.htm"; // you got a coupon already!
			}
			else
				htmltext = "31760-3.htm"; // you're not eligible to get a coupon (level caps, pkkills or already changed class)
		}
		else if (event.equals("traveller_give_coupon"))
		{
			if (level >= 6 && level <= 25 && pkkills == 0 && occupation_level == 1)
			{ // check the player state against this quest newbie rewarding mark.
				if ((newbie | TRAVELER_REWARD) != newbie)
				{
					player.setNewbie(newbie | TRAVELER_REWARD);
					st.giveItems(COUPNE_TWO, 1);
					htmltext = "31760-5.htm"; // here's the coupon you requested
				}
				else
					htmltext = "31760-4.htm"; // you got a coupon already!
			}
			else
				htmltext = "31760-6.htm"; // you're not eligible to get a coupon (level caps, pkkills or already changed class)
		}
		
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		return "31760.htm";
	}

	public static void main(String args[])
	{
		new MissQueen(-1, qn, "custom");
	}
}