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
package l2e.scripts.ai.raidboss;

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.type.L2EffectZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;

public class QueenShyeed extends AbstractNpcAI
{
	private static final int SHYEED = 25671;
	private static final Location SHYEED_LOC = new Location(79634, -55428, -6104, 0);
	
	private static final int RESPAWN = 86400000;
	private static final int RANDOM_RESPAWN = 43200000;
	
	private static final L2EffectZone MOB_BUFF_ZONE = ZoneManager.getInstance().getZoneById(200103, L2EffectZone.class);
	private static final L2EffectZone MOB_BUFF_DISPLAY_ZONE = ZoneManager.getInstance().getZoneById(200104, L2EffectZone.class);
	private static final L2EffectZone PC_BUFF_ZONE = ZoneManager.getInstance().getZoneById(200105, L2EffectZone.class);
	
	public QueenShyeed(String name, String descr)
	{
		super(name, descr);

		addKillId(SHYEED);
		spawnShyeed();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "respawn":
				spawnShyeed();
				break;
			case "despawn":
				if (!npc.isDead())
				{
					npc.deleteMe();
					startRespawn();
				}
				break;
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.SHYEEDS_CRY_IS_STEADILY_DYING_DOWN);
		startRespawn();
		PC_BUFF_ZONE.setZoneEnabled(true);
		return super.onKill(npc, killer, isSummon);
	}
	
	private void spawnShyeed()
	{
		String respawn = loadGlobalQuestVar("Respawn");
		long remain = (!respawn.isEmpty()) ? Long.parseLong(respawn) - System.currentTimeMillis() : 0;
		if (remain > 0)
		{
			startQuestTimer("respawn", remain, null, null);
			return;
		}
		final L2Npc npc = addSpawn(SHYEED, SHYEED_LOC, false, 0);
		startQuestTimer("despawn", 10800000, npc, null);
		PC_BUFF_ZONE.setZoneEnabled(false);
		MOB_BUFF_ZONE.setZoneEnabled(true);
		MOB_BUFF_DISPLAY_ZONE.setZoneEnabled(true);
	}
	
	private void startRespawn()
	{
		int respawnTime = RESPAWN - getRandom(RANDOM_RESPAWN);
		saveGlobalQuestVar("Respawn", Long.toString(System.currentTimeMillis() + respawnTime));
		startQuestTimer("respawn", respawnTime, null, null);
		MOB_BUFF_ZONE.setZoneEnabled(false);
		MOB_BUFF_DISPLAY_ZONE.setZoneEnabled(false);
	}
	
	public static void main(String[] args)
	{
		new QueenShyeed(QueenShyeed.class.getSimpleName(), "ai");
	}
}