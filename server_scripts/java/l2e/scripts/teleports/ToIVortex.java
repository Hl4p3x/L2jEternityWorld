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
package l2e.scripts.teleports;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.itemcontainer.PcInventory;

public class ToIVortex extends Quest
{
	// NPCs
	private static final int KEPLON = 30949;
	private static final int EUCLIE = 30950;
	private static final int PITHGON = 30951;
	private static final int DIMENSION_VORTEX_1 = 30952;
	private static final int DIMENSION_VORTEX_2 = 30953;
	private static final int DIMENSION_VORTEX_3 = 30954;
	
	// ITEMS
	private static final int GREEN_DIMENSION_STONE = 4401;
	private static final int BLUE_DIMENSION_STONE = 4402;
	private static final int RED_DIMENSION_STONE = 4403;

	public ToIVortex(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(KEPLON);
		addTalkId(KEPLON);
		addStartNpc(EUCLIE);
		addTalkId(EUCLIE);
		addStartNpc(PITHGON);
		addTalkId(PITHGON);
		addStartNpc(DIMENSION_VORTEX_1);
		addTalkId(DIMENSION_VORTEX_1);
		addStartNpc(DIMENSION_VORTEX_2);
		addTalkId(DIMENSION_VORTEX_2);
		addStartNpc(DIMENSION_VORTEX_3);
		addTalkId(DIMENSION_VORTEX_3);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		
		switch (event)
		{
			case "1":
			{
				if (hasQuestItems(player, GREEN_DIMENSION_STONE))
				{
					takeItems(player, GREEN_DIMENSION_STONE, 1);
					player.teleToLocation(114356, 13423, -5096, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "2":
			{
				if (hasQuestItems(player, GREEN_DIMENSION_STONE))
				{
					takeItems(player, GREEN_DIMENSION_STONE, 1);
					player.teleToLocation(114666, 13380, -3608, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "3":
			{
				if (hasQuestItems(player, GREEN_DIMENSION_STONE))
				{
					takeItems(player, GREEN_DIMENSION_STONE, 1);
					player.teleToLocation(111982, 16028, -2120, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "4":
			{
				if (hasQuestItems(player, BLUE_DIMENSION_STONE))
				{
					takeItems(player, BLUE_DIMENSION_STONE, 1);
					player.teleToLocation(114636, 13413, -640, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "5":
			{
				if (hasQuestItems(player, BLUE_DIMENSION_STONE))
				{
					takeItems(player, BLUE_DIMENSION_STONE, 1);
					player.teleToLocation(114152, 19902, 928, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "6":
			{
				if (hasQuestItems(player, BLUE_DIMENSION_STONE))
				{
					takeItems(player, BLUE_DIMENSION_STONE, 1);
					player.teleToLocation(117131, 16044, 1944, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "7":
			{
				if (hasQuestItems(player, RED_DIMENSION_STONE))
				{
					takeItems(player, RED_DIMENSION_STONE, 1);
					player.teleToLocation(113026, 17687, 2952, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "8":
			{
				if (hasQuestItems(player, RED_DIMENSION_STONE))
				{
					takeItems(player, RED_DIMENSION_STONE, 1);
					player.teleToLocation(115571, 13723, 3960, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "9":
			{
				if (hasQuestItems(player, RED_DIMENSION_STONE))
				{
					takeItems(player, RED_DIMENSION_STONE, 1);
					player.teleToLocation(114649, 14144, 4976, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "10":
			{
				if (hasQuestItems(player, RED_DIMENSION_STONE))
				{
					takeItems(player, RED_DIMENSION_STONE, 1);
					player.teleToLocation(118507, 16605, 5984, true);
				}
				else
				{
					return "no-stones.htm";
				}
				break;
			}
			case "GREEN":
			{
				if (player.getAdena() >= 10000)
				{
					takeItems(player, PcInventory.ADENA_ID, 10000);
					giveItems(player, GREEN_DIMENSION_STONE, 1);
				}
				else
				{
					return npcId + "no-adena.htm";
				}
				break;
			}
			case "BLUE":
			{
				if (player.getAdena() >= 10000)
				{
					takeItems(player, PcInventory.ADENA_ID, 10000);
					giveItems(player, BLUE_DIMENSION_STONE, 1);
				}
				else
				{
					return npcId + "no-adena.htm";
				}
				break;
			}
			case "RED":
			{
				if (player.getAdena() >= 10000)
				{
					takeItems(player, PcInventory.ADENA_ID, 10000);
					giveItems(player, RED_DIMENSION_STONE, 1);
				}
				else
				{
					return npcId + "no-adena.htm";
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new ToIVortex(-1, ToIVortex.class.getSimpleName(), "teleports");
	}
}