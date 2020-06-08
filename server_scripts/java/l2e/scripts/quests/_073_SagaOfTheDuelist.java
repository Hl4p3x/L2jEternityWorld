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

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.QuestState;

/**
 * Based on L2J Eternity-World
 */
public class _073_SagaOfTheDuelist extends SagasSuperClass
{
	public static String qn1 = "_073_SagaOfTheDuelist";
	public static int qnu = 73;
	public static String qna = "";
	
	private final int TUNATUN = 31537;
	private final int TOPQUALITYMEAT = 7546;
	
	public _073_SagaOfTheDuelist()
	{
		super(qnu, qn1, qna);
		NPC = new int[]
		{
			30849,
			31624,
			31226,
			31331,
			31639,
			31646,
			31647,
			31653,
			31654,
			31655,
			31656,
			31277
		};
		Items = new int[]
		{
			7080,
			7537,
			7081,
			7488,
			7271,
			7302,
			7333,
			7364,
			7395,
			7426,
			7096,
			7546
		};
		Mob = new int[]
		{
			27289,
			27222,
			27281
		};
		qn = qn1;
		classid = new int[]
		{
			88
		};
		prevclass = new int[]
		{
			0x02
		};
		X = new int[]
		{
			164650,
			47429,
			47391
		};
		Y = new int[]
		{
			-74121,
			-56923,
			-56929
		};
		Z = new int[]
		{
			-2871,
			-2383,
			-2370
		};
		Text = new String[]
		{
			"PLAYERNAME! Pursued to here! However, I jumped out of the Banshouren boundaries! You look at the giant as the sign of power!",
			"... Oh ... good! So it was ... let's begin!",
			"I do not have the patience ..! I have been a giant force ...! Cough chatter ah ah ah!",
			"Paying homage to those who disrupt the orderly will be PLAYERNAME's death!",
			"Now, my soul freed from the shackles of the millennium, Halixia, to the back side I come ...",
			"Why do you interfere others' battles?",
			"This is a waste of time.. Say goodbye...!",
			"...That is the enemy",
			"...Goodness! PLAYERNAME you are still looking?",
			"PLAYERNAME ... Not just to whom the victory. Only personnel involved in the fighting are eligible to share in the victory.",
			"Your sword is not an ornament. Don't you think, PLAYERNAME?",
			"Goodness! I no longer sense a battle there now.",
			"let...",
			"Only engaged in the battle to bar their choice. Perhaps you should regret.",
			"The human nation was foolish to try and fight a giant's strength.",
			"Must...Retreat... Too...Strong.",
			"PLAYERNAME. Defeat...by...retaining...and...Mo...Hacker",
			"....! Fight...Defeat...It...Fight...Defeat...It..."
		};
		registerNPCs();
		
		addTalkId(TUNATUN);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == TUNATUN)
		{
			String htmltext = getNoQuestMsg(player);
			QuestState st = player.getQuestState(qn);
			if (st != null)
			{
				int cond = st.getInt("cond");
				if (cond == 3)
				{
					if (st.getQuestItemsCount(TOPQUALITYMEAT) == 0)
					{
						st.giveItems(TOPQUALITYMEAT, 1);
						return "tunatun_01.htm";
					}
					return "tunatun_02.htm";
				}
			}
			return htmltext;
		}
		return super.onTalk(npc, player);
	}
}
