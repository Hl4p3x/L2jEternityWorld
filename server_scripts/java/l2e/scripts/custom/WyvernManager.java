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

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.util.Util;

public final class WyvernManager extends AbstractNpcAI
{
	private enum ManagerType
	{
		CASTLE,
		CLAN_HALL,
		FORT,
	}
	
	private static final int CRYSTAL_B_GRADE = 1460;
	private static final int WYVERN = 12621;
	private static final int WYVERN_FEE = 25;
	private static final int STRIDER_LVL = 55;

	private static final int[] STRIDERS =
	{
		12526,
		12527,
		12528,
		16038,
		16039,
		16040,
		16068,
		13197
	};

	private static final Map<Integer, ManagerType> MANAGERS = new HashMap<>();
	static
	{
		MANAGERS.put(35101, ManagerType.CASTLE);
		MANAGERS.put(35143, ManagerType.CASTLE);
		MANAGERS.put(35185, ManagerType.CASTLE);
		MANAGERS.put(35227, ManagerType.CASTLE);
		MANAGERS.put(35275, ManagerType.CASTLE);
		MANAGERS.put(35317, ManagerType.CASTLE);
		MANAGERS.put(35364, ManagerType.CASTLE);
		MANAGERS.put(35510, ManagerType.CASTLE);
		MANAGERS.put(35536, ManagerType.CASTLE);
		MANAGERS.put(35419, ManagerType.CLAN_HALL);
		MANAGERS.put(35638, ManagerType.CLAN_HALL);
		MANAGERS.put(36457, ManagerType.FORT);
		MANAGERS.put(36458, ManagerType.FORT);
		MANAGERS.put(36459, ManagerType.FORT);
		MANAGERS.put(36460, ManagerType.FORT);
		MANAGERS.put(36461, ManagerType.FORT);
		MANAGERS.put(36462, ManagerType.FORT);
		MANAGERS.put(36463, ManagerType.FORT);
		MANAGERS.put(36464, ManagerType.FORT);
		MANAGERS.put(36465, ManagerType.FORT);
		MANAGERS.put(36466, ManagerType.FORT);
		MANAGERS.put(36467, ManagerType.FORT);
		MANAGERS.put(36468, ManagerType.FORT);
		MANAGERS.put(36469, ManagerType.FORT);
		MANAGERS.put(36470, ManagerType.FORT);
		MANAGERS.put(36471, ManagerType.FORT);
		MANAGERS.put(36472, ManagerType.FORT);
		MANAGERS.put(36473, ManagerType.FORT);
		MANAGERS.put(36474, ManagerType.FORT);
		MANAGERS.put(36475, ManagerType.FORT);
		MANAGERS.put(36476, ManagerType.FORT);
		MANAGERS.put(36477, ManagerType.FORT);
	}
	
	private WyvernManager(String name, String descr)
	{
		super(name, descr);

		for (int npcId : MANAGERS.keySet())
		{
			addStartNpc(npcId);
			addTalkId(npcId);
			addFirstTalkId(npcId);
		}
	}
	
	private String mountWyvern(L2Npc npc, L2PcInstance player)
	{
		if (player.isMounted() && (player.getMountLevel() >= STRIDER_LVL) && Util.contains(STRIDERS, player.getMountNpcId()))
		{
			if (isOwnerClan(npc, player) && (getQuestItemsCount(player, CRYSTAL_B_GRADE) >= WYVERN_FEE))
			{
				takeItems(player, CRYSTAL_B_GRADE, WYVERN_FEE);
				player.dismount();
				player.mount(WYVERN, 0, true);
				return "wyvernmanager-04.htm";
			}
			return replacePart(player, player.getLang(), "wyvernmanager-06.htm");
		}
		return replacePart(player, player.getLang(), "wyvernmanager-05.htm");
	}
	
	private boolean isOwnerClan(L2Npc npc, L2PcInstance player)
	{
		switch (MANAGERS.get(npc.getId()))
		{
			case CASTLE:
			{
				if ((player.getClan() != null) && (npc.getCastle() != null))
				{
					return (player.isClanLeader() && (player.getId() == npc.getCastle().getOwnerId()));
				}
				return false;
			}
			case CLAN_HALL:
			{
				if ((player.getClan() != null) && (npc.getConquerableHall() != null))
				{
					return (player.isClanLeader() && (player.getId() == npc.getConquerableHall().getOwnerId()));
				}
				return false;
			}
			case FORT:
			{
				final Fort fort = npc.getFort();
				if ((player.getClan() != null) && (fort != null) && (fort.getOwnerClan() != null))
				{
					return (player.isClanLeader() && (player.getId() == npc.getFort().getOwnerClan().getId()));
				}
				return false;
			}
			default:
			{
				return false;
			}
		}
	}
	
	private boolean isInSiege(L2Npc npc)
	{
		switch (MANAGERS.get(npc.getId()))
		{
			case CASTLE:
			{
				return npc.getCastle().getZone().isActive();
			}
			case CLAN_HALL:
			{
				SiegableHall hall = npc.getConquerableHall();
				return (hall != null) ? hall.isInSiege() : npc.getCastle().getSiege().getIsInProgress();
			}
			case FORT:
			{
				return npc.getFort().getZone().isActive();
			}
			default:
			{
				return false;
			}
		}
	}
	
	private String getResidenceName(L2Npc npc)
	{
		switch (MANAGERS.get(npc.getId()))
		{
			case CASTLE:
			{
				return npc.getCastle().getName();
			}
			case CLAN_HALL:
			{
				return npc.getConquerableHall().getName();
			}
			case FORT:
			{
				return npc.getFort().getName();
			}
			default:
			{
				return null;
			}
		}
	}
	
	private String replaceAll(L2PcInstance player, L2Npc npc, String lang)
	{
		return replacePart(player, lang, "wyvernmanager-01.htm").replace("%residence_name%", getResidenceName(npc));
	}
	
	private String replacePart(L2PcInstance player, String lang, String htmlFile)
	{
		return getHtm(player, lang, htmlFile).replace("%wyvern_fee%", String.valueOf(WYVERN_FEE)).replace("%strider_level%", String.valueOf(STRIDER_LVL));
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "Return":
			{
				if (!isOwnerClan(npc, player))
				{
					htmltext = "wyvernmanager-02.htm";
				}
				else if (Config.ALLOW_WYVERN_ALWAYS)
				{
					htmltext = replaceAll(player, npc, player.getLang());
				}
				else if ((MANAGERS.get(npc.getId()) == ManagerType.CASTLE) && SevenSigns.getInstance().isSealValidationPeriod() && (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK))
				{
					htmltext = "wyvernmanager-dusk.htm";
				}
				else
				{
					htmltext = replaceAll(player, npc, player.getLang());
				}
				break;
			}
			case "Help":
			{
				htmltext = MANAGERS.get(npc.getId()) == ManagerType.CASTLE ? replacePart(player, player.getLang(), "wyvernmanager-03.htm") : replacePart(player, player.getLang(), "wyvernmanager-03b.htm");
				break;
			}
			case "RideWyvern":
			{
				if (!Config.ALLOW_WYVERN_ALWAYS)
				{
					if (!Config.ALLOW_WYVERN_DURING_SIEGE && (isInSiege(npc) || player.isInSiege()))
					{
						player.sendMessage("You cannot summon wyvern while in siege.");
						return null;
					}
					if ((MANAGERS.get(npc.getId()) == ManagerType.CASTLE) && SevenSigns.getInstance().isSealValidationPeriod() && ((SevenSigns.getInstance()).getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK))
					{
						htmltext = "wyvernmanager-dusk.htm";
					}
					else
					{
						htmltext = mountWyvern(npc, player);
					}
				}
				else
				{
					htmltext = mountWyvern(npc, player);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		if (!isOwnerClan(npc, player))
		{
			htmltext = "wyvernmanager-02.htm";
		}
		else
		{
			if (Config.ALLOW_WYVERN_ALWAYS)
			{
				htmltext = replaceAll(player, npc, player.getLang());
			}
			else
			{
				if ((MANAGERS.get(npc.getId()) == ManagerType.CASTLE) && SevenSigns.getInstance().isSealValidationPeriod() && (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK))
				{
					htmltext = "wyvernmanager-dusk.htm";
				}
				else
				{
					htmltext = replaceAll(player, npc, player.getLang());
				}
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new WyvernManager(WyvernManager.class.getSimpleName(), "custom");
	}
}