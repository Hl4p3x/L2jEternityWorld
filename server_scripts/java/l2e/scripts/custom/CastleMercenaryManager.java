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

import java.util.StringTokenizer;

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MerchantInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class CastleMercenaryManager extends AbstractNpcAI
{
	private static final int[] NPCS =
	{
		35102,
		35144,
		35186,
		35228,
		35276,
		35318,
		35365,
		35511,
		35557,
	};
	
	private CastleMercenaryManager(String name, String descr)
	{
		super(name, descr);

		addStartNpc(NPCS);
		addTalkId(NPCS);
		addFirstTalkId(NPCS);
	}
	
	private boolean hasRights(L2PcInstance player, L2Npc npc)
	{
		return player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS) || ((player.getId() == npc.getCastle().getOwnerId()) && ((player.getClanPrivileges() & L2Clan.CP_CS_MERCENARIES) == L2Clan.CP_CS_MERCENARIES));
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		final StringTokenizer st = new StringTokenizer(event, " ");
		switch (st.nextToken())
		{
			case "limit":
			{
				final Castle castle = npc.getCastle();
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				if (castle.getName().equals("aden"))
				{
					html.setFile(player.getLang(), "data/scripts/custom/CastleMercenaryManager/" + player.getLang() + "/mercmanager-aden-limit.htm");
				}
				else if (castle.getName().equals("rune"))
				{
					html.setFile(player.getLang(), "data/scripts/custom/CastleMercenaryManager/" + player.getLang() + "/mercmanager-rune-limit.htm");
				}
				else
				{
					html.setFile(player.getLang(), "data/scripts/custom/CastleMercenaryManager/" + player.getLang() + "/mercmanager-limit.htm");
				}
				html.replace("%feud_name%", String.valueOf(1001000 + castle.getId()));
				player.sendPacket(html);
				break;
			}
			case "buy":
			{
				if (SevenSigns.getInstance().isSealValidationPeriod())
				{
					htmltext = "mercmanager-ssq.htm";
				}
				else
				{
					final int listId = Integer.parseInt(npc.getId() + st.nextToken());
					((L2MerchantInstance) npc).showBuyWindow(player, listId, false);
				}
				break;
			}
			case "main":
			{
				htmltext = onFirstTalk(npc, player);
				break;
			}
			case "mercmanager-01.htm":
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final String htmltext;
		if (hasRights(player, npc))
		{
			if (npc.getCastle().getSiege().getIsInProgress())
			{
				htmltext = "mercmanager-siege.htm";
			}
			else if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK)
			{
				htmltext = "mercmanager-dusk.htm";
			}
			else if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
			{
				htmltext = "mercmanager-dawn.htm";
			}
			else
			{
				htmltext = "mercmanager.htm";
			}
		}
		else
		{
			htmltext = "mercmanager-no.htm";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new CastleMercenaryManager(CastleMercenaryManager.class.getSimpleName(), "custom");
	}
}