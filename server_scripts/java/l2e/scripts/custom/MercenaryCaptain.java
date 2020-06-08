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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import l2e.gameserver.data.xml.MultiSellParser;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.TerritoryWarManager.Territory;
import l2e.gameserver.instancemanager.TerritoryWarManager.TerritoryNPCSpawn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.ExShowDominionRegistry;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.ai.npc.AbstractNpcAI;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class MercenaryCaptain extends AbstractNpcAI
{
	private static final Map<Integer, Integer> NPCS = new HashMap<>();
	static
	{
		NPCS.put(36481, 13757);
		NPCS.put(36482, 13758);
		NPCS.put(36483, 13759);
		NPCS.put(36484, 13760);
		NPCS.put(36485, 13761);
		NPCS.put(36486, 13762);
		NPCS.put(36487, 13763);
		NPCS.put(36488, 13764);
		NPCS.put(36489, 13765);
	}

	private static final int STRIDER_WIND = 4422;
	private static final int STRIDER_STAR = 4423;
	private static final int STRIDER_TWILIGHT = 4424;
	private static final int GUARDIAN_STRIDER = 14819;
	private static final int ELITE_MERCENARY_CERTIFICATE = 13767;
	private static final int TOP_ELITE_MERCENARY_CERTIFICATE = 13768;

	private static final int DELAY = 3600000;
	private static final int MIN_LEVEL = 40;
	private static final int CLASS_LEVEL = 2;

	private MercenaryCaptain(String name, String descr)
	{
		super(name, descr);

		for (int id : NPCS.keySet())
		{
			addStartNpc(id);
			addFirstTalkId(id);
			addTalkId(id);
		}

		for (Territory terr : TerritoryWarManager.getInstance().getAllTerritories())
		{
			for (TerritoryNPCSpawn spawn : terr.getSpawnList())
			{
				if (NPCS.keySet().contains(spawn.getId()))
				{
					startQuestTimer("say", DELAY, spawn.getNpc(), null, true);
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		if (player != null)
		{
			final StringTokenizer st = new StringTokenizer(event, " ");
			switch (st.nextToken())
			{
				case "36481-02.htm":
				{
					htmltext = event;
					break;
				}
				case "36481-03.htm":
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
					html.setFile(player.getLang(), "data/scripts/custom/MercenaryCaptain/" + player.getLang() + "/36481-03.htm");
					html.replace("%strider%", String.valueOf(TerritoryWarManager.MINTWBADGEFORSTRIDERS));
					html.replace("%gstrider%", String.valueOf(TerritoryWarManager.MINTWBADGEFORBIGSTRIDER));
					player.sendPacket(html);
					break;
				}
				case "territory":
				{
					player.sendPacket(new ExShowDominionRegistry(npc.getCastle().getId(), player));
					break;
				}
				case "strider":
				{
					final String type = st.nextToken();
					final int price = (type.equals("3")) ? TerritoryWarManager.MINTWBADGEFORBIGSTRIDER : TerritoryWarManager.MINTWBADGEFORSTRIDERS;
					final int badgeId = NPCS.get(npc.getId());
					if (getQuestItemsCount(player, badgeId) < price)
					{
						return "36481-07.htm";
					}

					final int striderId;
					switch (type)
					{
						case "0":
						{
							striderId = STRIDER_WIND;
							break;
						}
						case "1":
						{
							striderId = STRIDER_STAR;
							break;
						}
						case "2":
						{
							striderId = STRIDER_TWILIGHT;
							break;
						}
						case "3":
						{
							striderId = GUARDIAN_STRIDER;
							break;
						}
						default:
						{
							_log.warning(MercenaryCaptain.class.getSimpleName() + ": Unknown strider type: " + type);
							return null;
						}
					}
					takeItems(player, badgeId, price);
					giveItems(player, striderId, 1);
					htmltext = "36481-09.htm";
					break;
				}
				case "elite":
				{
					if (!hasQuestItems(player, ELITE_MERCENARY_CERTIFICATE))
					{
						htmltext = "36481-10.htm";
					}
					else
					{
						final int listId = 676 + npc.getCastle().getId();
						MultiSellParser.getInstance().separateAndSend(listId, player, npc, false);
					}
					break;
				}
				case "top-elite":
				{
					if (!hasQuestItems(player, TOP_ELITE_MERCENARY_CERTIFICATE))
					{
						htmltext = "36481-10.htm";
					}
					else
					{
						final int listId = 685 + npc.getCastle().getId();
						MultiSellParser.getInstance().separateAndSend(listId, player, npc, false);
					}
					break;
				}
			}
		}
		else if (event.equalsIgnoreCase("say") && !npc.isDecayed())
		{
			if (TerritoryWarManager.getInstance().isTWInProgress())
			{
				broadcastNpcSay(npc, Say2.NPC_SHOUT, NpcStringId.CHARGE_CHARGE_CHARGE);
			}
			else if (getRandom(2) == 0)
			{
				broadcastNpcSay(npc, Say2.NPC_SHOUT, NpcStringId.COURAGE_AMBITION_PASSION_MERCENARIES_WHO_WANT_TO_REALIZE_THEIR_DREAM_OF_FIGHTING_IN_THE_TERRITORY_WAR_COME_TO_ME_FORTUNE_AND_GLORY_ARE_WAITING_FOR_YOU);
			}
			else
			{
				broadcastNpcSay(npc, Say2.NPC_SHOUT, NpcStringId.DO_YOU_WISH_TO_FIGHT_ARE_YOU_AFRAID_NO_MATTER_HOW_HARD_YOU_TRY_YOU_HAVE_NOWHERE_TO_RUN_BUT_IF_YOU_FACE_IT_HEAD_ON_OUR_MERCENARY_TROOP_WILL_HELP_YOU_OUT);
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final String htmltext;
		if ((player.getLevel() < MIN_LEVEL) || (player.getClassId().level() < CLASS_LEVEL))
		{
			htmltext = "36481-08.htm";
		}
		else if (npc.isMyLord(player))
		{
			htmltext = (npc.getCastle().getSiege().getIsInProgress() || TerritoryWarManager.getInstance().isTWInProgress()) ? "36481-05.htm" : "36481-04.htm";
		}
		else
		{
			htmltext = (npc.getCastle().getSiege().getIsInProgress() || TerritoryWarManager.getInstance().isTWInProgress()) ? "36481-06.htm" : npc.getId() + "-01.htm";
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new MercenaryCaptain(MercenaryCaptain.class.getSimpleName(), "custom");
	}
}
