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
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class CastleWarehouse extends AbstractNpcAI
{
	private static final int[] NPCS =
	{
		35099,
		35141,
		35183,
		35225,
		35273,
		35315,
		35362,
		35508,
		35554
	};

	private static final int BLOOD_OATH = 9910;
	private static final int BLOOD_ALLIANCE = 9911;
	
	private CastleWarehouse(String name, String descr)
	{
		super(name, descr);

		addStartNpc(NPCS);
		addTalkId(NPCS);
		addFirstTalkId(NPCS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		switch (event)
		{
			case "warehouse-01.htm":
			case "warehouse-02.htm":
			case "warehouse-03.htm":
				break;
			case "warehouse-04.htm":
				if (npc.isMyLord(player))
				{
					html.setFile(player.getLang(), "data/scripts/custom/CastleWarehouse/" + player.getLang() + "/warehouse-04.htm");
					html.replace("%blood%", Integer.toString(player.getClan().getBloodAllianceCount()));
				}
				else
				{
					html.setFile(player.getLang(), "data/scripts/custom/CastleWarehouse/" + player.getLang() + "/warehouse-no.htm");
				}
				player.sendPacket(html);
				break;
			case "Receive":
				if (!npc.isMyLord(player))
				{
					htmltext = "warehouse-no.htm";
				}
				else if (player.getClan().getBloodAllianceCount() == 0)
				{
					htmltext = "warehouse-05.htm";
				}
				else
				{
					giveItems(player, BLOOD_ALLIANCE, player.getClan().getBloodAllianceCount());
					player.getClan().resetBloodAllianceCount();
					htmltext = "warehouse-06.htm";
				}
				break;
			case "Exchange":
				if (!npc.isMyLord(player))
				{
					htmltext = "warehouse-no.htm";
				}
				else if (!hasQuestItems(player, BLOOD_ALLIANCE))
				{
					htmltext = "warehouse-08.htm";
				}
				else
				{
					takeItems(player, BLOOD_ALLIANCE, 1);
					giveItems(player, BLOOD_OATH, 30);
					htmltext = "warehouse-07.htm";
				}
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "warehouse-01.htm";
	}
	
	public static void main(String[] args)
	{
		new CastleWarehouse(CastleWarehouse.class.getSimpleName(), "custom");
	}
}