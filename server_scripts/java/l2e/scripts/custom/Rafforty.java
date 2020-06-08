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

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class Rafforty extends AbstractNpcAI
{
	private static final int RAFFORTY = 32020;
	
	private static final int NECKLACE = 16025;
	private static final int BLESSED_NECKLACE = 16026;
	private static final int BOTTLE = 16027;

	public Rafforty(String name, String descr)
	{
		super(name, descr);

		addStartNpc(RAFFORTY);
		addFirstTalkId(RAFFORTY);
		addTalkId(RAFFORTY);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		switch (event)
		{
			case "32020-01.htm":
				if (!hasQuestItems(player, NECKLACE))
				{
					htmltext = "32020-02.htm";
				}
				break;
			case "32020-04.htm":
				if (!hasQuestItems(player, BOTTLE))
				{
					htmltext = "32020-05.htm";
				}
				break;
			case "32020-07.htm":
				if (!hasQuestItems(player, BOTTLE, NECKLACE))
				{
					return "32020-08.htm";
				}
				takeItems(player, NECKLACE, 1);
				takeItems(player, BOTTLE, 1);
				giveItems(player, BLESSED_NECKLACE, 1);
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Rafforty(Rafforty.class.getSimpleName(), "custom");
	}
}