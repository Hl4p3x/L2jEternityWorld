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
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.EtcStatusUpdate;

public class BlackJudge extends AbstractNpcAI
{
	private static final int BLACK_JUDGE = 30981;
	
	private static final int[] COSTS =
	{
		3600, 8640, 25200, 50400, 86400, 144000
	};
	
	private BlackJudge()
	{
		super(BlackJudge.class.getSimpleName(), "custom");

		addStartNpc(BLACK_JUDGE);
		addTalkId(BLACK_JUDGE);
		addFirstTalkId(BLACK_JUDGE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;

		final int level = ((player.getExpertiseLevel() < 5) ? player.getExpertiseLevel() : 5);
		switch (event)
		{
			case "remove_info":
			{
				htmltext = "30981-0" + (level + 1) + ".html";
				break;
			}
			case "remove_dp":
			{
				if (player.getDeathPenaltyBuffLevel() > 0)
				{
					int cost = COSTS[level];
					
					if (player.getAdena() >= cost)
					{
						takeItems(player, PcInventory.ADENA_ID, cost);
						player.setDeathPenaltyBuffLevel(player.getDeathPenaltyBuffLevel() - 1);
						player.sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
						player.sendPacket(new EtcStatusUpdate(player));
					}
					else
					{
						htmltext = "30981-07.htm";
					}
				}
				else
				{
					htmltext = "30981-08.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new BlackJudge();
	}
}