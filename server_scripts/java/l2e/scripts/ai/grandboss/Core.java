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
package l2e.scripts.ai.grandboss;

import java.util.List;

import javolution.util.FastList;
import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.Config;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.PlaySound;

public class Core extends AbstractNpcAI
{
	private static final int CORE = 29006;
	private static final int DEATH_KNIGHT = 29007;
	private static final int DOOM_WRAITH = 29008;
	private static final int SUSCEPTOR = 29011;
	
	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;
	
	private static boolean _FirstAttacked;
	
	private final List<L2Attackable> Minions = new FastList<>();
	
	private Core(String name, String descr)
	{
		super(name, descr);
		registerMobs(CORE, DEATH_KNIGHT, DOOM_WRAITH, SUSCEPTOR);
		
		_FirstAttacked = false;
		StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
		int status = GrandBossManager.getInstance().getBossStatus(CORE);
		if (status == DEAD)
		{
			long temp = (info.getLong("respawn_time") - System.currentTimeMillis());

			if (temp > 0)
			{
				startQuestTimer("core_unlock", temp, null, null);
			}
			else
			{
				L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
				GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
				spawnBoss(core);
			}
		}
		else
		{
			String test = loadGlobalQuestVar("Core_Attacked");
			if (test.equalsIgnoreCase("true"))
			{
				_FirstAttacked = true;
			}
			int loc_x = info.getInteger("loc_x");
			int loc_y = info.getInteger("loc_y");
			int loc_z = info.getInteger("loc_z");
			int heading = info.getInteger("heading");
			int hp = info.getInteger("currentHP");
			int mp = info.getInteger("currentMP");
			L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, loc_x, loc_y, loc_z, heading, false, 0);
			core.setCurrentHpMp(hp, mp);
			spawnBoss(core);
		}
	}
	
	@Override
	public void saveGlobalData()
	{
		saveGlobalQuestVar("Core_Attacked", Boolean.toString(_FirstAttacked));
	}
	
	public void spawnBoss(L2GrandBossInstance npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		L2Attackable mob;
		for (int i = 0; i < 5; i++)
		{
			int x = 16800 + (i * 360);
			mob = (L2Attackable) addSpawn(DEATH_KNIGHT, x, 110000, npc.getZ(), 280 + getRandom(40), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
			mob = (L2Attackable) addSpawn(DEATH_KNIGHT, x, 109000, npc.getZ(), 280 + getRandom(40), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
			int x2 = 16800 + (i * 600);
			mob = (L2Attackable) addSpawn(DOOM_WRAITH, x2, 109300, npc.getZ(), 280 + getRandom(40), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
		}
		for (int i = 0; i < 4; i++)
		{
			int x = 16800 + (i * 450);
			mob = (L2Attackable) addSpawn(SUSCEPTOR, x, 110300, npc.getZ(), 280 + getRandom(40), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("core_unlock"))
		{
			L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
			GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
			spawnBoss(core);
		}
		else if (event.equalsIgnoreCase("spawn_minion"))
		{
			L2Attackable mob = (L2Attackable) addSpawn(npc.getId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (int i = 0; i < Minions.size(); i++)
			{
				L2Attackable mob = Minions.get(i);
				if (mob != null)
				{
					mob.decayMe();
				}
			}
			Minions.clear();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == CORE)
		{
			if (_FirstAttacked)
			{
				if (getRandom(100) == 0)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.REMOVING_INTRUDERS));
				}
			}
			else
			{
				_FirstAttacked = true;
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.A_NON_PERMITTED_TARGET_HAS_BEEN_DISCOVERED));
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), NpcStringId.INTRUDER_REMOVAL_SYSTEM_INITIATED));
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();
		if (npcId == CORE)
		{
			int objId = npc.getObjectId();
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, objId, npc.getX(), npc.getY(), npc.getZ()));
			npc.broadcastPacket(new NpcSay(objId, Say2.NPC_ALL, npcId, NpcStringId.A_FATAL_ERROR_HAS_OCCURRED));
			npc.broadcastPacket(new NpcSay(objId, Say2.NPC_ALL, npcId, NpcStringId.SYSTEM_IS_BEING_SHUT_DOWN));
			npc.broadcastPacket(new NpcSay(objId, Say2.NPC_ALL, npcId, NpcStringId.DOT_DOT_DOT_DOT_DOT_DOT));
			_FirstAttacked = false;
			GrandBossManager.getInstance().setBossStatus(CORE, DEAD);
			long respawnTime = Config.CORE_SPAWN_INTERVAL + getRandom(-Config.CORE_SPAWN_RANDOM, Config.CORE_SPAWN_RANDOM);
			respawnTime *= 3600000;
			startQuestTimer("core_unlock", respawnTime, null, null);
			StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
			info.set("respawn_time", (System.currentTimeMillis() + respawnTime));
			GrandBossManager.getInstance().setStatsSet(CORE, info);
			startQuestTimer("despawn_minions", 20000, null, null);
			cancelQuestTimers("spawn_minion");
		}
		else if ((GrandBossManager.getInstance().getBossStatus(CORE) == ALIVE) && (Minions != null) && Minions.contains(npc))
		{
			Minions.remove(npc);
			startQuestTimer("spawn_minion", 60000, npc, null);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Core(Core.class.getSimpleName(), "ai");
	}
}