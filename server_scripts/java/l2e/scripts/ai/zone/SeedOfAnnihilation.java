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
package l2e.scripts.ai.zone;

import java.util.Calendar;
import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.type.L2EffectZone;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class SeedOfAnnihilation extends AbstractNpcAI
{
	private static final Map<Integer, Location> _teleportZones = new FastMap<>();
	private static final int ANNIHILATION_FURNACE = 18928;

	private static final int[] ZONE_BUFFS =
	{
	                0,
	                6443,
	                6444,
	                6442
	};

	private static final int[][] ZONE_BUFFS_LIST =
	{
	                {
	                                1, 2, 3
	                },
	                {
	                                1, 3, 2
	                },
	                {
	                                2, 1, 3
	                },
	                {
	                                2, 3, 1
	                },
	                {
	                                3, 2, 1
	                },
	                {
	                                3, 1, 2
	                }
	};

	private final SeedRegion[] _regionsData = new SeedRegion[3];
	private Long _seedsNextStatusChange;

	static
	{
		_teleportZones.put(60002, new Location(-213175, 182648, -10992));
		_teleportZones.put(60003, new Location(-181217, 186711, -10528));
		_teleportZones.put(60004, new Location(-180211, 182984, -15152));
		_teleportZones.put(60005, new Location(-179275, 186802, -10720));
	}

	private SeedOfAnnihilation(String name, String descr)
	{
		super(name, descr);

		loadSeedRegionData();
		for (int i : _teleportZones.keySet())
		{
			addEnterZoneId(i);
		}
		for (SeedRegion element : _regionsData)
		{
			for (int elite_mob_id : element.elite_mob_ids)
			{
				addSpawnId(elite_mob_id);
			}
		}
		addStartNpc(32739);
		addTalkId(32739);
		initialMinionsSpawn();
		startEffectZonesControl();
	}

	public void loadSeedRegionData()
	{
		_regionsData[0] = new SeedRegion(new int[]
		{
		                22750,
		                22751,
		                22752,
		                22753
		}, new int[][]
		{
		                {
		                                22746,
		                                22746,
		                                22746
		                },
		                {
		                                22747,
		                                22747,
		                                22747
		                },
		                {
		                                22748,
		                                22748,
		                                22748
		                },
		                {
		                                22749,
		                                22749,
		                                22749
		                }
		}, 60006, new int[][]
		{
		                {
		                                -180450,
		                                185507,
		                                -10544,
		                                11632
		                },
		                {
		                                -180005,
		                                185489,
		                                -10544,
		                                11632
		                }
		});

		_regionsData[1] = new SeedRegion(new int[]
		{
		                22757,
		                22758,
		                22759
		}, new int[][]
		{
			        {
			                        22754,
			                        22755,
			                        22756
			}
		}, 60007, new int[][]
		{
		                {
		                                -179600,
		                                186998,
		                                -10704,
		                                11632
		                },
		                {
		                                -179295,
		                                186444,
		                                -10704,
		                                11632
		                }
		});

		_regionsData[2] = new SeedRegion(new int[]
		{
		                22763,
		                22764,
		                22765
		}, new int[][]
		{
		                {
		                                22760,
		                                22760,
		                                22761
		                },
		                {
		                                22760,
		                                22760,
		                                22762
		                },
		                {
		                                22761,
		                                22761,
		                                22760
		                },
		                {
		                                22761,
		                                22761,
		                                22762
		                },
		                {
		                                22762,
		                                22762,
		                                22760
		                },
		                {
		                                22762,
		                                22762,
		                                22761
		                }
		}, 60008, new int[][]
		{
		                {
		                                -180971,
		                                186361,
		                                -10528,
		                                11632
		                },
		                {
		                                -180758,
		                                186739,
		                                -10528,
		                                11632
		                }
		});

		int buffsNow = 0;
		String var = loadGlobalQuestVar("SeedNextStatusChange");
		if (var.equalsIgnoreCase("") || (Long.parseLong(var) < System.currentTimeMillis()))
		{
			buffsNow = getRandom(ZONE_BUFFS_LIST.length);
			saveGlobalQuestVar("SeedBuffsList", String.valueOf(buffsNow));
			_seedsNextStatusChange = getNextSeedsStatusChangeTime();
			saveGlobalQuestVar("SeedNextStatusChange", String.valueOf(_seedsNextStatusChange));
		}
		else
		{
			_seedsNextStatusChange = Long.parseLong(var);
			buffsNow = Integer.parseInt(loadGlobalQuestVar("SeedBuffsList"));
		}
		for (int i = 0; i < _regionsData.length; i++)
		{
			_regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];
		}
	}

	private Long getNextSeedsStatusChangeTime()
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.SECOND, 0);
		reenter.set(Calendar.MINUTE, 0);
		reenter.set(Calendar.HOUR_OF_DAY, 13);
		reenter.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
		{
			reenter.add(Calendar.DAY_OF_MONTH, 7);
		}
		return reenter.getTimeInMillis();
	}

	private void startEffectZonesControl()
	{
		for (int i = 0; i < _regionsData.length; i++)
		{
			for (int j = 0; j < _regionsData[i].af_spawns.length; j++)
			{
				_regionsData[i].af_npcs[j] = addSpawn(ANNIHILATION_FURNACE, _regionsData[i].af_spawns[j][0], _regionsData[i].af_spawns[j][1], _regionsData[i].af_spawns[j][2], _regionsData[i].af_spawns[j][3], false, 0);
				_regionsData[i].af_npcs[j].setDisplayEffect(_regionsData[i].activeBuff);
			}
			ZoneManager.getInstance().getZoneById(_regionsData[i].buff_zone, L2EffectZone.class).addSkill(ZONE_BUFFS[_regionsData[i].activeBuff], 1);
		}
		startQuestTimer("ChangeSeedsStatus", _seedsNextStatusChange - System.currentTimeMillis(), null, null);
	}

	private void initialMinionsSpawn()
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn == null)
			{
				continue;
			}
			for (SeedRegion element : _regionsData)
			{
				if (Util.contains(element.elite_mob_ids, spawn.getId()))
				{
					L2MonsterInstance mob = (L2MonsterInstance) spawn.getLastSpawn();
					if (mob != null)
					{
						spawnGroupOfMinion(mob, element.minion_lists[getRandom(element.minion_lists.length)]);
					}
				}
			}
		}
	}

	private void spawnGroupOfMinion(L2MonsterInstance npc, int[] mobIds)
	{
		for (int mobId : mobIds)
		{
			addMinion(npc, mobId);
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		for (SeedRegion element : _regionsData)
		{
			if (Util.contains(element.elite_mob_ids, npc.getId()))
			{
				spawnGroupOfMinion((L2MonsterInstance) npc, element.minion_lists[getRandom(element.minion_lists.length)]);
			}
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("ChangeSeedsStatus"))
		{
			int buffsNow = getRandom(ZONE_BUFFS_LIST.length);
			saveGlobalQuestVar("SeedBuffsList", String.valueOf(buffsNow));
			_seedsNextStatusChange = getNextSeedsStatusChangeTime();
			saveGlobalQuestVar("SeedNextStatusChange", String.valueOf(_seedsNextStatusChange));
			for (int i = 0; i < _regionsData.length; i++)
			{
				_regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];

				for (L2Npc af : _regionsData[i].af_npcs)
				{
					af.setDisplayEffect(_regionsData[i].activeBuff);
				}

				L2EffectZone zone = ZoneManager.getInstance().getZoneById(_regionsData[i].buff_zone, L2EffectZone.class);
				zone.clearSkills();
				zone.addSkill(ZONE_BUFFS[_regionsData[i].activeBuff], 1);
			}
			startQuestTimer("ChangeSeedsStatus", _seedsNextStatusChange - System.currentTimeMillis(), null, null);
		}
		else if (event.equalsIgnoreCase("transform"))
		{
			if (player.getFirstEffect(6408) != null)
			{
				npc.showChatWindow(player, 2);
			}
			else
			{
				npc.setTarget(player);
				npc.doCast(SkillHolder.getInstance().getInfo(6408, 1));
				npc.doCast(SkillHolder.getInstance().getInfo(6649, 1));
				npc.showChatWindow(player, 1);
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (_teleportZones.containsKey(zone.getId()))
		{
			Location teleLoc = _teleportZones.get(zone.getId());
			character.teleToLocation(teleLoc, false);
		}
		return super.onEnterZone(character, zone);
	}

	private static class SeedRegion
	{
		public int[] elite_mob_ids;
		public int[][] minion_lists;
		public int buff_zone;
		public int[][] af_spawns;
		public L2Npc[] af_npcs = new L2Npc[2];
		public int activeBuff = 0;

		public SeedRegion(int[] emi, int[][] ml, int bz, int[][] as)
		{
			elite_mob_ids = emi;
			minion_lists = ml;
			buff_zone = bz;
			af_spawns = as;
		}
	}

	public static void main(String[] args)
	{
		new SeedOfAnnihilation(SeedOfAnnihilation.class.getSimpleName(), "ai");
	}
}
