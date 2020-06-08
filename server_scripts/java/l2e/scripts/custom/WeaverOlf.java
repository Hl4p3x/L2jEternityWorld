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
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.npc.AbstractNpcAI;

/**
 * Based on L2J Eternity-World
 */
public class WeaverOlf extends AbstractNpcAI
{
	private static final int[] NPCs =
	{
		32610,
		32612,
	};
	
	private static final int[] UNSEAL_PRICE =
	{
		3200,
		11800,
		26500,
		136600
	};
	
	private static final int[] CHANCES =
	{
		1,
		10,
		40,
		100,
	};
	
	private static final int[][] PINS =
	{
		{
			13898,
			13905,
			13904,
			13903,
			13902,
		},
		{
			13899,
			13909,
			13908,
			13907,
			13906,
		},
		{
			13900,
			13913,
			13912,
			13911,
			13910,
		},
		{
			13901,
			13917,
			13916,
			13915,
			13914,
		}
	};
	
	private static final int[][] POUCHS =
	{
		{
			13918,
			13925,
			13924,
			13923,
			13922,
		},
		{
			13919,
			13929,
			13928,
			13927,
			13926,
		},
		{
			13920,
			13933,
			13932,
			13931,
			13930,
		},
		{
			13921,
			13937,
			13936,
			13935,
			13934,
		}
	};
	
	private static final int[][] CLIPS_ORNAMENTS =
	{
		{
			14902,
			14909,
			14908,
			14907,
			14906,
		},
		{
			14903,
			14913,
			14912,
			14911,
			14910,
		},
		{
			14904,
			14917,
			14916,
			14915,
			14914,
		},
		{
			14905,
			14921,
			14920,
			14919,
			14918,
		}
	};
	
	private WeaverOlf(String name, String descr)
	{
		super(name, descr);

		addStartNpc(NPCs);
		addTalkId(NPCs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.contains("_grade_"))
		{
			int grade = Integer.parseInt(event.substring(0, 1));
			int price;
			int[] itemIds;
			if (event.endsWith("_pin"))
			{
				price = UNSEAL_PRICE[grade];
				itemIds = PINS[grade];
			}
			else if (event.endsWith("_pouch"))
			{
				price = UNSEAL_PRICE[grade];
				itemIds = POUCHS[grade];
			}
			else if (event.endsWith("_clip"))
			{
				price = UNSEAL_PRICE[grade];
				itemIds = CLIPS_ORNAMENTS[grade - 2];
			}
			else if (event.endsWith("_ornament"))
			{
				price = UNSEAL_PRICE[grade];
				itemIds = CLIPS_ORNAMENTS[grade];
			}
			else
			{
				return super.onAdvEvent(event, npc, player);
			}
			
			if (hasQuestItems(player, itemIds[0]))
			{
				if (player.getAdena() > price)
				{
					takeItems(player, PcInventory.ADENA_ID, price);
					takeItems(player, itemIds[0], 1);
					int rand = getRandom(200);
					if (rand <= CHANCES[0])
					{
						giveItems(player, itemIds[1], 1);
					}
					else if (rand <= CHANCES[1])
					{
						giveItems(player, itemIds[2], 1);
					}
					else if (rand <= CHANCES[2])
					{
						giveItems(player, itemIds[3], 1);
					}
					else if (rand <= CHANCES[3])
					{
						giveItems(player, itemIds[4], 1);
					}
					else
					{
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.WHAT_A_PREDICAMENT_MY_ATTEMPTS_WERE_UNSUCCESSFUL));
					}
				}
				else
				{
					return npc.getId() + "-low.htm";
				}
			}
			else
			{
				return npc.getId() + "-no.htm";
			}
			return super.onAdvEvent(event, npc, player);
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + "-1.htm";
	}
	
	public static void main(String[] args)
	{
		new WeaverOlf(WeaverOlf.class.getSimpleName(), "custom");
	}
}
