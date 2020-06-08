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

import l2e.Config;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.L2CommandChannel;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.zone.type.L2BossZone;

public class SteelCitadelTeleport extends Quest
{
	private static final int BELETH = 29118;
	private static final int NAIA_CUBE = 32376;
	private static final boolean debug = false;
	
	private static final byte WAITING = 1;
	private static final byte DEAD = 3;
	
	public SteelCitadelTeleport(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(NAIA_CUBE);
		addTalkId(NAIA_CUBE);
		addSpawnId(NAIA_CUBE);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		startQuestTimer("despawn", 600000, npc, null);
		return null;
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc == null)
		{
			return null;
		}
		
		if (event.equalsIgnoreCase("despawn") && npc.getId() == NAIA_CUBE)
		{
			npc.deleteMe();
		}
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		switch (npc.getId())
		{
			case NAIA_CUBE:
				if (GrandBossManager.getInstance().getBossStatus(BELETH) == DEAD)
				{
					return "32376-02.htm";
				}
				
				final L2CommandChannel channel = player.getParty() == null ? null : player.getParty().getCommandChannel();

				if (!debug)
				{
					if ((channel == null) || (channel.getLeader().getObjectId() != player.getObjectId()) || (channel.getMemberCount() < Config.BELETH_MIN_PLAYERS))
					{
						return "32376-02a.htm";
					}
				}
				
				if (GrandBossManager.getInstance().getBossStatus(BELETH) > 0)
				{
					return "32376-03.htm";
				}
				
				final L2BossZone zone = (L2BossZone) ZoneManager.getInstance().getZoneById(12018);
				if (zone != null)
				{
					GrandBossManager.getInstance().setBossStatus(BELETH, WAITING);
					
					if (!debug)
					{
						for (L2Party party : channel.getPartys())
						{
							if (party == null)
							{
								continue;
							}
						
							for (L2PcInstance pl : party.getMembers())
							{
								if (pl.isInsideRadius(npc.getX(), npc.getY(), npc.getZ(), 3000, true, false))
								{
									zone.allowPlayerEntry(pl, 30);
									pl.teleToLocation(16342, 209557, -9352, true);
								}
							}
						}
					}
					else
					{
						if (player.getParty() != null)
						{
							for (L2PcInstance pl : player.getParty().getMembers())
							{
								if (pl.isInsideRadius(npc.getX(), npc.getY(), npc.getZ(), 3000, true, false))
								{
									zone.allowPlayerEntry(pl, 30);
									pl.teleToLocation(16342, 209557, -9352, true);
								}
							}
						}
					}
				}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new SteelCitadelTeleport(-1, SteelCitadelTeleport.class.getSimpleName(), "teleports");
	}
}