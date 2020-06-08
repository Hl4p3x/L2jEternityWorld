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

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Created by LordWinter 21.08.2012 Based on L2J Eternity-World
 */
public class Anais extends L2AttackableAIScript
{
	private static final int ANAIS = 25701;
	private static final int GUARD = 25702;

	private static boolean FIGHTHING = false;
	private final FastList<L2Npc> burners = new FastList<>();
	private final FastList<L2Npc> guards = new FastList<>();
	private final FastMap<L2Npc, L2PcInstance> targets = new FastMap<>();

	private static int BURNERS_ENABLED = 0;

	private static final int[][] BURNERS =
	{
	                {
	                                113632,
	                                -75616,
	                                50
	                },
	                {
	                                111904,
	                                -75616,
	                                58
	                },
	                {
	                                111904,
	                                -77424,
	                                51
	                },
	                {
	                                113696,
	                                -77393,
	                                48
	                }
	};

	L2Skill guard_skill = SkillHolder.getInstance().getInfo(6326, 1);

	public Anais(int questId, String name, String descr)
	{
		super(questId, name, descr);

		registerMobs(new int[]
		{
		                ANAIS,
		                GUARD
		});

		spawnBurners();
	}

	private void spawnBurners()
	{
		for (int[] SPAWN : BURNERS)
		{
			L2Npc npc = addSpawn(18915, SPAWN[0], SPAWN[1], SPAWN[2], 0, false, 0L);
			if (npc == null)
			{
				continue;
			}
			burners.add(npc);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("check_status"))
		{
			if (FIGHTHING)
			{
				if ((npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) || (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
				{
					stopFight();
				}
				else
				{
					startQuestTimer("check_status", 50000L, npc, null);
				}
			}
		}
		else if (event.equalsIgnoreCase("burner_action"))
		{
			if ((FIGHTHING) && (npc != null))
			{
				L2Npc guard = addSpawn(GUARD, npc);
				if (guard != null)
				{
					guards.add(guard);
					startQuestTimer("guard_action", 500L, guard, null);
				}
				startQuestTimer("burner_action", 20000L, npc, null);
			}
		}
		else if (event.equalsIgnoreCase("guard_action"))
		{
			if ((FIGHTHING) && (npc != null) && (!npc.isDead()))
			{
				if (targets.containsKey(npc))
				{
					L2PcInstance target = targets.get(npc);
					if ((target != null) && (target.isOnline()) && (target.isInsideRadius(npc, 5000, false, false)))
					{
						npc.setIsRunning(true);
						npc.setTarget(target);

						if (target.isInsideRadius(npc, 200, false, false))
						{
							npc.doCast(guard_skill);
						}
						else
						{
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
						}
					}
					else
					{
						npc.deleteMe();
						if (targets.containsKey(npc))
						{
							targets.remove(npc);
						}
					}
				}
				else
				{
					FastList<L2PcInstance> result = FastList.newInstance();
					L2PcInstance target = null;
					for (L2PcInstance pl : npc.getKnownList().getKnownPlayersInRadius(3000L))
					{
						if ((pl == null) || (pl.isAlikeDead()))
						{
							continue;
						}

						if ((pl.isInsideRadius(npc, 3000, true, false)) && (GeoClient.getInstance().canSeeTarget(npc, pl)))
						{
							result.add(pl);
						}
					}
					if (!result.isEmpty())
					{
						target = result.get(getRandom(result.size() - 1));
					}
					if (target != null)
					{
						npc.setTitle(target.getName());
						npc.broadcastPacket(new AbstractNpcInfo.NpcInfo(npc, target));
						npc.setIsRunning(true);
						targets.put(npc, target);
					}
					FastList.recycle(result);
				}
				startQuestTimer("guard_action", 1000L, npc, null);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == ANAIS)
		{
			if (!FIGHTHING)
			{
				FIGHTHING = true;
				startQuestTimer("check_status", 50000L, npc, null);
			}
			else if ((getRandom(10) == 0) && (BURNERS_ENABLED < 4))
			{
				checkBurnerStatus(npc);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (npc.getId() == GUARD)
		{
			if (guards.contains(npc))
			{
				guards.remove(npc);
			}
			npc.doDie(npc);
			npc.deleteMe();
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == ANAIS)
		{
			stopFight();
		}

		return super.onKill(npc, killer, isSummon);
	}

	private synchronized void checkBurnerStatus(L2Npc anais)
	{
		switch (BURNERS_ENABLED)
		{
			case 0:
				enableBurner(1);
				BURNERS_ENABLED = 1;
				break;
			case 1:
				if (anais.getCurrentHp() > (anais.getMaxHp() * 0.75D))
				{
					break;
				}
				enableBurner(2);
				BURNERS_ENABLED = 2;
				break;
			case 2:
				if (anais.getCurrentHp() > (anais.getMaxHp() * 0.5D))
				{
					break;
				}
				enableBurner(3);
				BURNERS_ENABLED = 3;
				break;
			case 3:
				if (anais.getCurrentHp() > (anais.getMaxHp() * 0.25D))
				{
					break;
				}
				enableBurner(4);
				BURNERS_ENABLED = 4;
				break;
		}
	}

	private void enableBurner(int index)
	{
		if (!burners.isEmpty())
		{
			L2Npc burner = burners.get(index - 1);
			if (burner != null)
			{
				burner.setDisplayEffect(1);
				startQuestTimer("burner_action", 1000L, burner, null);
			}
		}
	}

	private void stopFight()
	{
		if (!targets.isEmpty())
		{
			targets.clear();
		}

		if (!burners.isEmpty())
		{
			for (L2Npc burner : burners)
			{
				if (burner != null)
				{
					burner.setDisplayEffect(2);
				}
			}
		}

		if (!guards.isEmpty())
		{
			for (L2Npc guard : guards)
			{
				if (guard != null)
				{
					guard.deleteMe();
				}
			}
		}

		cancelQuestTimers("guard_action");
		cancelQuestTimers("burner_action");

		BURNERS_ENABLED = 0;
		FIGHTHING = false;
	}

	public static void main(String[] args)
	{
		new Anais(-1, "Anais", "ai");
	}
}
