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

import java.util.ArrayList;
import java.util.List;

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.type.L2BossZone;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class Valakas extends AbstractNpcAI
{
	private static final int VALAKAS = 29028;

	private static final SkillsHolder VALAKAS_LAVA_SKIN = new SkillsHolder(4680, 1);
	private static final int VALAKAS_REGENERATION = 4691;

	private static final SkillsHolder[] VALAKAS_REGULAR_SKILLS =
	{
	                new SkillsHolder(4681, 1),
	                new SkillsHolder(4682, 1),
	                new SkillsHolder(4683, 1),
	                new SkillsHolder(4689, 1)
	};

	private static final SkillsHolder[] VALAKAS_LOWHP_SKILLS =
	{
	                new SkillsHolder(4681, 1),
	                new SkillsHolder(4682, 1),
	                new SkillsHolder(4683, 1),
	                new SkillsHolder(4689, 1),
	                new SkillsHolder(4690, 1)
	};

	private static final SkillsHolder[] VALAKAS_AOE_SKILLS =
	{
	                new SkillsHolder(4683, 1),
	                new SkillsHolder(4684, 1),
	                new SkillsHolder(4685, 1),
	                new SkillsHolder(4686, 1),
	                new SkillsHolder(4688, 1),
	                new SkillsHolder(4689, 1),
	                new SkillsHolder(4690, 1)
	};

	private static final Location TELEPORT_CUBE_LOCATIONS[] =
	{
	                new Location(214880, -116144, -1644),
	                new Location(213696, -116592, -1644),
	                new Location(212112, -116688, -1644),
	                new Location(211184, -115472, -1664),
	                new Location(210336, -114592, -1644),
	                new Location(211360, -113904, -1644),
	                new Location(213152, -112352, -1644),
	                new Location(214032, -113232, -1644),
	                new Location(214752, -114592, -1644),
	                new Location(209824, -115568, -1421),
	                new Location(210528, -112192, -1403),
	                new Location(213120, -111136, -1408),
	                new Location(215184, -111504, -1392),
	                new Location(215456, -117328, -1392),
	                new Location(213200, -118160, -1424)
	};

	private static final byte DORMANT = 0;
	private static final byte WAITING = 1;
	private static final byte FIGHTING = 2;
	private static final byte DEAD = 3;

	private long _timeTracker = 0;
	private L2Playable _actualVictim;
	private static L2BossZone ZONE;

	private Valakas(String name, String descr)
	{
		super(name, descr);
		registerMobs(VALAKAS);

		ZONE = GrandBossManager.getInstance().getZone(212852, -114842, -1632);

		final StatsSet info = GrandBossManager.getInstance().getStatsSet(VALAKAS);
		final int status = GrandBossManager.getInstance().getBossStatus(VALAKAS);

		if (status == DEAD)
		{
			long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				startQuestTimer("valakas_unlock", temp, null, null);
			}
			else
			{
				final L2Npc valakas = addSpawn(VALAKAS, -105200, -253104, -15264, 0, false, 0);
				GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT);
				GrandBossManager.getInstance().addBoss((L2GrandBossInstance) valakas);

				valakas.setIsInvul(true);
				valakas.setRunning();

				valakas.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		else
		{
			final int loc_x = info.getInteger("loc_x");
			final int loc_y = info.getInteger("loc_y");
			final int loc_z = info.getInteger("loc_z");
			final int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");

			final L2Npc valakas = addSpawn(VALAKAS, loc_x, loc_y, loc_z, heading, false, 0);
			GrandBossManager.getInstance().addBoss((L2GrandBossInstance) valakas);

			valakas.setCurrentHpMp(hp, mp);
			valakas.setRunning();

			if (status == FIGHTING)
			{
				_timeTracker = System.currentTimeMillis();

				startQuestTimer("regen_task", 60000, valakas, null, true);
				startQuestTimer("skill_task", 2000, valakas, null, true);
			}
			else
			{
				valakas.setIsInvul(true);
				valakas.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

				if (status == WAITING)
				{
					startQuestTimer("beginning", (Config.VALAKAS_WAIT_TIME * 60000), valakas, null);
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc != null)
		{
			if (event.equalsIgnoreCase("beginning"))
			{
				_timeTracker = System.currentTimeMillis();

				npc.teleToLocation(212852, -114842, -1632);

				startQuestTimer("spawn_1", 1700, npc, null);
				startQuestTimer("spawn_2", 3200, npc, null);
				startQuestTimer("spawn_3", 6500, npc, null);
				startQuestTimer("spawn_4", 9400, npc, null);
				startQuestTimer("spawn_5", 12100, npc, null);
				startQuestTimer("spawn_6", 12430, npc, null);
				startQuestTimer("spawn_7", 15430, npc, null);
				startQuestTimer("spawn_8", 16830, npc, null);
				startQuestTimer("spawn_9", 23530, npc, null);
				startQuestTimer("spawn_10", 26000, npc, null);
			}
			else if (event.equalsIgnoreCase("regen_task"))
			{
				if (GrandBossManager.getInstance().getBossStatus(VALAKAS) == FIGHTING)
				{
					if ((_timeTracker + 900000) < System.currentTimeMillis())
					{
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						npc.teleToLocation(-105200, -253104, -15264);

						GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT);
						npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());

						ZONE.oustAllPlayers();

						cancelQuestTimer("regen_task", npc, null);
						cancelQuestTimer("skill_task", npc, null);
						return null;
					}
				}

				final L2Effect e = npc.getFirstEffect(VALAKAS_REGENERATION);
				final int lvl = e != null ? e.getSkill().getLevel() : 0;

				if ((npc.getCurrentHp() < (npc.getMaxHp() / 4)) && (lvl != 4))
				{
					npc.setTarget(npc);
					npc.doCast(SkillHolder.getInstance().getInfo(VALAKAS_REGENERATION, 4));
				}
				else if ((npc.getCurrentHp() < ((npc.getMaxHp() * 2) / 4.0)) && (lvl != 3))
				{
					npc.setTarget(npc);
					npc.doCast(SkillHolder.getInstance().getInfo(VALAKAS_REGENERATION, 3));
				}
				else if ((npc.getCurrentHp() < ((npc.getMaxHp() * 3) / 4.0)) && (lvl != 2))
				{
					npc.setTarget(npc);
					npc.doCast(SkillHolder.getInstance().getInfo(VALAKAS_REGENERATION, 2));
				}
				else if (lvl != 1)
				{
					npc.setTarget(npc);
					npc.doCast(SkillHolder.getInstance().getInfo(VALAKAS_REGENERATION, 1));
				}
			}
			else if (event.equalsIgnoreCase("spawn_1"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1800, 180, -1, 1500, 15000, 10000, 0, 0, 1, 0, 0));
				for (L2PcInstance plyr : ZONE.getPlayersInside())
				{
					plyr.sendPacket(new PlaySound(1, "B03_A", 0, 0, 0, 0, 0));
					plyr.sendPacket(new SocialAction(npc.getObjectId(), 3));
				}
			}
			else if (event.equalsIgnoreCase("spawn_2"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1300, 180, -5, 3000, 15000, 10000, 0, -5, 1, 0, 0));
			}
			else if (event.equalsIgnoreCase("spawn_3"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 500, 180, -8, 600, 15000, 10000, 0, 60, 1, 0, 0));
			}
			else if (event.equalsIgnoreCase("spawn_4"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 800, 180, -8, 2700, 15000, 10000, 0, 30, 1, 0, 0));
			}
			else if (event.equalsIgnoreCase("spawn_5"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 200, 250, 70, 0, 15000, 10000, 30, 80, 1, 0, 0));
			}
			else if (event.equalsIgnoreCase("spawn_6"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1100, 250, 70, 2500, 15000, 10000, 30, 80, 1, 0, 0));
			}
			else if (event.equalsIgnoreCase("spawn_7"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 700, 150, 30, 0, 15000, 10000, -10, 60, 1, 0, 0));
			}
			else if (event.equalsIgnoreCase("spawn_8"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1200, 150, 20, 2900, 15000, 10000, -10, 30, 1, 0, 0));
			}
			else if (event.equalsIgnoreCase("spawn_9"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 750, 170, -10, 3400, 15000, 4000, 10, -15, 1, 0, 0));
			}
			else if (event.equalsIgnoreCase("spawn_10"))
			{
				GrandBossManager.getInstance().setBossStatus(VALAKAS, FIGHTING);
				npc.setIsInvul(false);

				startQuestTimer("regen_task", 60000, npc, null, true);
				startQuestTimer("skill_task", 2000, npc, null, true);
			}
			else if (event.equalsIgnoreCase("die_1"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 2000, 130, -1, 0, 15000, 10000, 0, 0, 1, 1, 0));
			}
			else if (event.equalsIgnoreCase("die_2"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1100, 210, -5, 3000, 15000, 10000, -13, 0, 1, 1, 0));
			}
			else if (event.equalsIgnoreCase("die_3"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1300, 200, -8, 3000, 15000, 10000, 0, 15, 1, 1, 0));
			}
			else if (event.equalsIgnoreCase("die_4"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1000, 190, 0, 500, 15000, 10000, 0, 10, 1, 1, 0));
			}
			else if (event.equalsIgnoreCase("die_5"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1700, 120, 0, 2500, 15000, 10000, 12, 40, 1, 1, 0));
			}
			else if (event.equalsIgnoreCase("die_6"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1700, 20, 0, 700, 15000, 10000, 10, 10, 1, 1, 0));
			}
			else if (event.equalsIgnoreCase("die_7"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1700, 10, 0, 1000, 15000, 10000, 20, 70, 1, 1, 0));
			}
			else if (event.equalsIgnoreCase("die_8"))
			{
				ZONE.broadcastPacket(new SpecialCamera(npc, 1700, 10, 0, 300, 15000, 250, 20, -20, 1, 1, 0));

				for (Location loc : TELEPORT_CUBE_LOCATIONS)
				{
					addSpawn(31759, loc, false, 900000);
				}

				startQuestTimer("remove_players", 900000, null, null);
			}
			else if (event.equalsIgnoreCase("skill_task"))
			{
				callSkillAI(npc);
			}
		}
		else
		{
			if (event.equalsIgnoreCase("valakas_unlock"))
			{
				final L2Npc valakas = addSpawn(VALAKAS, -105200, -253104, -15264, 32768, false, 0);
				GrandBossManager.getInstance().addBoss((L2GrandBossInstance) valakas);
				GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT);
			}
			else if (event.equalsIgnoreCase("remove_players"))
			{
				ZONE.oustAllPlayers();
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		return super.onSpawn(npc);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (!ZONE.isInsideZone(attacker))
		{
			attacker.doDie(attacker);
			return null;
		}

		if (npc.isInvul())
		{
			return null;
		}

		if (GrandBossManager.getInstance().getBossStatus(VALAKAS) != FIGHTING)
		{
			attacker.teleToLocation(150037, -57255, -2976);
			return null;
		}

		if (attacker.getMountType() == MountType.STRIDER)
		{
			final L2Skill skill = SkillHolder.getInstance().getInfo(4258, 1);
			if (attacker.getFirstEffect(skill) == null)
			{
				npc.setTarget(attacker);
				npc.doCast(skill);
			}
		}
		_timeTracker = System.currentTimeMillis();

		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		cancelQuestTimer("regen_task", npc, null);
		cancelQuestTimer("skill_task", npc, null);

		ZONE.broadcastPacket(new PlaySound(1, "B03_D", 0, 0, 0, 0, 0));
		ZONE.broadcastPacket(new SpecialCamera(npc, 1200, 20, -10, 0, 10000, 13000, 0, 0, 0, 0, 0));

		startQuestTimer("die_1", 300, npc, null);
		startQuestTimer("die_2", 600, npc, null);
		startQuestTimer("die_3", 3800, npc, null);
		startQuestTimer("die_4", 8200, npc, null);
		startQuestTimer("die_5", 8700, npc, null);
		startQuestTimer("die_6", 13300, npc, null);
		startQuestTimer("die_7", 14000, npc, null);
		startQuestTimer("die_8", 16500, npc, null);

		GrandBossManager.getInstance().setBossStatus(VALAKAS, DEAD);

		long respawnTime = Config.VALAKAS_SPAWN_INTERVAL + getRandom(-Config.VALAKAS_SPAWN_RANDOM, Config.VALAKAS_SPAWN_RANDOM);
		respawnTime *= 3600000;
		startQuestTimer("valakas_unlock", respawnTime, null, null);

		StatsSet info = GrandBossManager.getInstance().getStatsSet(VALAKAS);
		info.set("respawn_time", (System.currentTimeMillis() + respawnTime));
		GrandBossManager.getInstance().setStatsSet(VALAKAS, info);

		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		return null;
	}

	private void callSkillAI(L2Npc npc)
	{
		if (npc.isInvul() || npc.isCastingNow())
		{
			return;
		}

		if ((_actualVictim == null) || _actualVictim.isDead() || !(npc.getKnownList().knowsObject(_actualVictim)) || (getRandom(10) == 0))
		{
			_actualVictim = getRandomTarget(npc);
		}

		if (_actualVictim == null)
		{
			if (getRandom(10) == 0)
			{
				int x = npc.getX();
				int y = npc.getY();
				int z = npc.getZ();

				int posX = x + getRandom(-1400, 1400);
				int posY = y + getRandom(-1400, 1400);

				if (GeoClient.getInstance().canMoveToCoord(x, y, z, posX, posY, z, false))
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(posX, posY, z, 0));
				}
			}
			return;
		}

		final L2Skill skill = getRandomSkill(npc).getSkill();

		if (Util.checkIfInRange((skill.getCastRange() < 600) ? 600 : skill.getCastRange(), npc, _actualVictim, true))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			npc.setIsCastingNow(true);
			npc.setTarget(_actualVictim);
			npc.doCast(skill);
		}
		else
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _actualVictim, null);
			npc.setIsCastingNow(false);
		}
	}

	private SkillsHolder getRandomSkill(L2Npc npc)
	{
		final int hpRatio = (int) ((npc.getCurrentHp() / npc.getMaxHp()) * 100);

		if ((hpRatio < 75) && (getRandom(150) == 0) && (npc.getFirstEffect(VALAKAS_LAVA_SKIN.getSkillId()) == null))
		{
			return VALAKAS_LAVA_SKIN;
		}

		if (Util.getPlayersCountInRadius(1200, npc, false, false) >= 20)
		{
			return VALAKAS_AOE_SKILLS[getRandom(VALAKAS_AOE_SKILLS.length)];
		}

		if (hpRatio > 50)
		{
			return VALAKAS_REGULAR_SKILLS[getRandom(VALAKAS_REGULAR_SKILLS.length)];
		}
		return VALAKAS_LOWHP_SKILLS[getRandom(VALAKAS_LOWHP_SKILLS.length)];
	}

	private L2Playable getRandomTarget(L2Npc npc)
	{
		List<L2Playable> result = new ArrayList<>();

		for (L2Character obj : npc.getKnownList().getKnownCharacters())
		{
			if ((obj == null) || obj.isSummon())
			{
				continue;
			}
			else if (!obj.isDead() && obj.isPlayable())
			{
				result.add((L2Playable) obj);
			}
		}

		return (result.isEmpty()) ? null : result.get(getRandom(result.size()));
	}

	public static void main(String[] args)
	{
		new Valakas(Valakas.class.getSimpleName(), "ai");
	}
}
