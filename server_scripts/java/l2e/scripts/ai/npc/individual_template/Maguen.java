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
package l2e.scripts.ai.npc.individual_template;

import org.apache.commons.lang.ArrayUtils;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.scripts.ai.L2AttackableAIScript;
import l2e.util.Rnd;

/**
 * Created by LordWinter 20.06.2012 Based on L2J Eternity-World
 */
public class Maguen extends L2AttackableAIScript
{
	private static final int MAGUEN = 18839;

	private static final int[] MOBS =
	{
	                22746,
	                22747,
	                22748,
	                22749,
	                22754,
	                22755,
	                22756,
	                22760,
	                22761,
	                22762
	};

	private static final int[] maguenStatsSkills =
	{
	                6343,
	                6365,
	                6366
	};

	private static final int[] maguenRaceSkills =
	{
	                6367,
	                6368,
	                6369
	};

	public Maguen(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addSpawnId(MAGUEN);
		addSkillSeeId(MAGUEN);

		for (int i : MOBS)
		{
			addKillId(i);
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Plasma(npc), 2000L);

		return super.onSpawn(npc);
	}

	@Override
	@Deprecated
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (npc.getId() == MAGUEN)
		{
			if (skill.getId() != 9060)
			{
				return null;
			}

			if (Rnd.chance(4))
			{
				caster.addItem("Maguen", 15490, 1, null, true);
			}
			if (Rnd.chance(2))
			{
				caster.addItem("Maguen", 15491, 1, null, true);
			}

			L2ZoneType zone = getZone(npc, "Seed of Annihilation", true);

			if (zone != null)
			{
				for (L2Character ch : zone.getCharactersInside())
				{
					if ((ch != null) && !ch.isDead())
					{
						npc.setTarget(caster);

						switch (npc.getDisplayEffect())
						{
							case 1:
								if (Rnd.chance(80))
								{
									npc.doCast(SkillHolder.getInstance().getInfo(maguenRaceSkills[0], getRandom(2, 3)));
								}
								else
								{
									npc.doCast(SkillHolder.getInstance().getInfo(maguenStatsSkills[0], getRandom(1, 2)));
								}
								break;
							case 2:
								if (Rnd.chance(80))
								{
									npc.doCast(SkillHolder.getInstance().getInfo(maguenRaceSkills[1], getRandom(2, 3)));
								}
								else
								{
									npc.doCast(SkillHolder.getInstance().getInfo(maguenStatsSkills[1], getRandom(1, 2)));
								}
								break;
							case 3:
								if (Rnd.chance(80))
								{
									npc.doCast(SkillHolder.getInstance().getInfo(maguenRaceSkills[2], getRandom(2, 3)));
								}
								else
								{
									npc.doCast(SkillHolder.getInstance().getInfo(maguenStatsSkills[2], getRandom(1, 2)));
								}
								break;
							default:
								break;
						}
					}
					else
					{
						switch (npc.getDisplayEffect())
						{
							case 1:
								npc.doCast(SkillHolder.getInstance().getInfo(maguenRaceSkills[0], 1));
								break;
							case 2:
								npc.doCast(SkillHolder.getInstance().getInfo(maguenRaceSkills[1], 1));
								break;
							case 3:
								npc.doCast(SkillHolder.getInstance().getInfo(maguenRaceSkills[2], 1));
								break;
							default:
								break;
						}
					}
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (ArrayUtils.contains(MOBS, npc.getId()))
		{
			if (Rnd.chance(5))
			{
				final L2Npc maguen = addSpawn(MAGUEN, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 10000, true);
				maguen.setRunning();
				((L2Attackable) maguen).addDamageHate(killer, 1, 99999);
				maguen.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);

				killer.sendPacket(new ExShowScreenMessage(NpcStringId.MAGUEN_APPEARANCE, 2, 5000));
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	private class Plasma implements Runnable
	{
		private final L2Npc _npc;

		public Plasma(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			_npc.setDisplayEffect(getRandom(1, 3));
		}
	}

	private L2ZoneType getZone(L2Npc npc, String nameTemplate, boolean currentLoc)
	{
		try
		{
			int x;
			int y;
			int z;

			if (currentLoc)
			{
				x = npc.getX();
				y = npc.getY();
				z = npc.getZ();
			}
			else
			{
				x = npc.getSpawn().getX();
				y = npc.getSpawn().getY();
				z = npc.getSpawn().getZ();
			}

			for (L2ZoneType zone : ZoneManager.getInstance().getZones(x, y, z))
			{
				if (zone.getName().startsWith(nameTemplate))
				{
					return zone;
				}
			}
		}

		catch (NullPointerException e)
		{
		}
		catch (IndexOutOfBoundsException e)
		{
		}
		return null;
	}

	public static void main(String[] args)
	{
		new Maguen(-1, "Maguen", "ai");
	}
}
