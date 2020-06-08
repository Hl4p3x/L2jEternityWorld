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

import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;

public class CastleTeleporter extends AbstractNpcAI
{
	private static final int[] NPCS =
	{
		35095,
		35137,
		35179,
		35221,
		35266,
		35311,
		35355,
		35502,
		35547
	};
	
	private CastleTeleporter(String name, String descr)
	{
		super(name, descr);

		addStartNpc(NPCS);
		addTalkId(NPCS);
		addFirstTalkId(NPCS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("teleporter-03.htm"))
		{
			if (npc.isScriptValue(0))
			{
				final Siege siege = npc.getCastle().getSiege();
				final int time = (siege.getIsInProgress() && (siege.getControlTowerCount() == 0)) ? 480000 : 30000;
				startQuestTimer("teleport", time, npc, null);
				npc.setScriptValue(1);
			}
			return event;
		}
		else if (event.equalsIgnoreCase("teleport"))
		{
			final int region = MapRegionManager.getInstance().getMapRegionLocId(npc.getX(), npc.getY());
			final NpcSay msg = new NpcSay(npc, Say2.NPC_SHOUT, NpcStringId.THE_DEFENDERS_OF_S1_CASTLE_WILL_BE_TELEPORTED_TO_THE_INNER_CASTLE);
			msg.addStringParameter(npc.getCastle().getName());
			npc.getCastle().oustAllPlayers();
			npc.setScriptValue(0);
			
			final L2PcInstance[] players = L2World.getInstance().getAllPlayersArray();
			for (L2PcInstance pl : players)
			{
				if (region == MapRegionManager.getInstance().getMapRegionLocId(pl))
				{
					pl.sendPacket(msg);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final Siege siege = npc.getCastle().getSiege();
		return (npc.isScriptValue(0)) ? (siege.getIsInProgress() && (siege.getControlTowerCount() == 0)) ? "teleporter-02.htm" : "teleporter-01.htm" : "teleporter-03.htm";
	}
	
	public static void main(String[] args)
	{
		new CastleTeleporter(CastleTeleporter.class.getSimpleName(), "custom");
	}
}