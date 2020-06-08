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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.xml.SpawnParser;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.handler.ItemHandler;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;

/**
 * Based on L2J Eternity-World
 */
public final class PrimevalIsle extends AbstractNpcAI
{
	private static final int EGG = 18344;
	private static final int SAILREN = 29065;
	private static final int ORNIT = 22742;
	private static final int DEINO = 22743;

	private static final int[] SPRIGNANT =
	{
	                18345,
	                18346
	};
	private static final int[] MONSTERS =
	{
	                22196,
	                22198,
	                22200,
	                22202,
	                22203,
	                22205,
	                22208,
	                22210,
	                22211,
	                22213,
	                22223,
	                22224,
	                22225,
	                22226,
	                22227,
	                22742,
	                22743
	};

	private static final int[] TREX =
	{
	                22215,
	                22216,
	                22217
	};

	private static final int[] VEGETABLE =
	{
	                22200,
	                22201,
	                22202,
	                22203,
	                22204,
	                22205,
	                22224,
	                22225
	};

	private static final int DEINONYCHUS = 14828;

	private static final SkillsHolder ANESTHESIA = new SkillsHolder(5085, 1);
	private static final SkillsHolder DEADLY_POISON = new SkillsHolder(5086, 1);
	private static final SkillsHolder SELFBUFF1 = new SkillsHolder(5087, 1);
	private static final SkillsHolder SELFBUFF2 = new SkillsHolder(5087, 2);
	private static final SkillsHolder LONGRANGEDMAGIC1 = new SkillsHolder(5120, 1);
	private static final SkillsHolder PHYSICALSPECIAL1 = new SkillsHolder(5083, 4);
	private static final SkillsHolder PHYSICALSPECIAL2 = new SkillsHolder(5081, 4);
	private static final SkillsHolder PHYSICALSPECIAL3 = new SkillsHolder(5082, 4);
	private static final SkillsHolder CREW_SKILL = new SkillsHolder(6172, 1);
	private static final SkillsHolder INVIN_BUFF_ON = new SkillsHolder(5225, 1);

	private PrimevalIsle()
	{
		super(PrimevalIsle.class.getSimpleName(), "ai");

		addSpawnId(TREX);
		addSpawnId(SPRIGNANT);
		addSpawnId(MONSTERS);
		addAggroRangeEnterId(TREX);
		addSpellFinishedId(TREX);
		addAttackId(EGG);
		addAttackId(TREX);
		addAttackId(MONSTERS);
		addKillId(EGG, SAILREN, DEINO, ORNIT);
		addSeeCreatureId(TREX);
		addSeeCreatureId(MONSTERS);

		for (int npcId : SPRIGNANT)
		{
			for (L2Spawn spawn : SpawnParser.getInstance().getSpawnData())
			{
				if (spawn != null)
				{
					if (spawn.getId() == npcId)
					{
						onSpawn(spawn.getLastSpawn());
					}
				}
			}
		}

		for (int npcId : TREX)
		{
			for (L2Spawn spawn : SpawnParser.getInstance().getSpawnData())
			{
				if (spawn != null)
				{
					if (spawn.getId() == npcId)
					{
						onSpawn(spawn.getLastSpawn());
					}
				}
			}
		}
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (skill.getId() == CREW_SKILL.getSkillId())
		{
			startQuestTimer("START_INVUL", 4000, npc, null);
			final L2Npc target = (L2Npc) npc.getTarget();
			if (target != null)
			{
				target.doDie(npc);
			}
		}
		if (npc.isInCombat())
		{
			final L2Attackable mob = (L2Attackable) npc;
			final L2Character target = mob.getMostHated();
			if (((npc.getCurrentHp() / npc.getMaxHp()) * 100) < 60)
			{
				if (skill.getId() == SELFBUFF1.getSkillId())
				{
					npc.setScriptValue(3);
					if ((target != null))
					{
						npc.setTarget(target);
						mob.setIsRunning(true);
						mob.addDamageHate(target, 0, 555);
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
				}
			}
			else if (((npc.getCurrentHp() / npc.getMaxHp()) * 100) < 30)
			{
				if (skill.getId() == SELFBUFF1.getSkillId())
				{
					npc.setScriptValue(1);
					if ((target != null))
					{
						npc.setTarget(target);
						mob.setIsRunning(true);
						mob.addDamageHate(target, 0, 555);
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
				}
				else if (skill.getId() == SELFBUFF2.getSkillId())
				{
					npc.setScriptValue(5);
					if ((target != null))
					{
						npc.setTarget(target);
						mob.setIsRunning(true);
						mob.addDamageHate(target, 0, 555);
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "USE_SKILL":
			{
				if ((npc != null) && !npc.isDead())
				{
					npc.doCast((npc.getId() == SPRIGNANT[0] ? ANESTHESIA.getSkill() : DEADLY_POISON.getSkill()));
					startQuestTimer("USE_SKILL", 15000, npc, null);
				}
				break;
			}
			case "GHOST_DESPAWN":
			{
				if ((npc != null) && !npc.isDead())
				{
					if (!npc.isInCombat())
					{
						npc.deleteMe();
					}
					else
					{
						startQuestTimer("GHOST_DESPAWN", 1800000, npc, null);
					}
				}
				break;
			}
			case "TREX_ATTACK":
			{
				if ((npc != null) && (player != null))
				{
					npc.setScriptValue(0);
					if (player.isInsideRadius(npc, 800, true, false))
					{
						npc.setTarget(player);
						npc.doCast(LONGRANGEDMAGIC1.getSkill());
						attackPlayer((L2Attackable) npc, player);
					}
				}
				break;
			}
			case "START_INVUL":
			{
				if ((npc != null) && !npc.isDead())
				{
					npc.doCast(INVIN_BUFF_ON.getSkill());
					startQuestTimer("START_INVUL_2", 30000, npc, null);
				}
				break;
			}
			case "START_INVUL_2":
			{
				if ((npc != null) && !npc.isDead())
				{
					INVIN_BUFF_ON.getSkill().getEffects(npc, npc);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (Util.contains(MONSTERS, npc.getId()))
		{
			if (creature.isPlayer())
			{
				final L2Attackable mob = (L2Attackable) npc;
				final int ag_type = npc.getTemplate().getParameters().getInteger("ag_type", 0);
				final int probPhysicalSpecial1 = npc.getTemplate().getParameters().getInteger("ProbPhysicalSpecial1", 0);
				final int probPhysicalSpecial2 = npc.getTemplate().getParameters().getInteger("ProbPhysicalSpecial2", 0);
				final SkillsHolder physicalSpecial1 = npc.getTemplate().getParameters().getObject("PhysicalSpecial1", SkillsHolder.class);
				final SkillsHolder physicalSpecial2 = npc.getTemplate().getParameters().getObject("PhysicalSpecial2", SkillsHolder.class);

				if (((getRandom(100) < 30) && (npc.getId() == DEINO)) || ((npc.getId() == ORNIT) && npc.isScriptValue(0)))
				{
					mob.clearAggroList();
					npc.setScriptValue(1);
					npc.setRunning();

					final int distance = 3000;
					final int heading = Util.calculateHeadingFrom(creature, npc);
					final double angle = Util.convertHeadingToDegree(heading);
					final double radian = Math.toRadians(angle);
					final double sin = Math.sin(radian);
					final double cos = Math.cos(radian);
					final int newX = (int) (npc.getX() + (cos * distance));
					final int newY = (int) (npc.getY() + (sin * distance));
					final Location loc = GeoClient.getInstance().moveCheck(npc.getX(), npc.getY(), npc.getZ(), newX, newY, npc.getZ(), true);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc, 0);
				}
				else if (ag_type == 1)
				{
					if (getRandom(100) <= (probPhysicalSpecial1 * npc.getVariables().getInteger("SKILL_MULTIPLER")))
					{
						if (!npc.isSkillDisabled(physicalSpecial1.getSkillId()))
						{
							npc.setTarget(creature);
							npc.doCast(physicalSpecial1.getSkill());
						}
					}
					else if (getRandom(100) <= (probPhysicalSpecial2 * npc.getVariables().getInteger("SKILL_MULTIPLER")))
					{
						if (!npc.isSkillDisabled(physicalSpecial2.getSkill()))
						{
							npc.setTarget(creature);
							npc.doCast(physicalSpecial2.getSkill());
						}
					}
				}
			}
		}
		else if (Util.contains(VEGETABLE, creature.getId()))
		{
			npc.setTarget(creature);
			npc.doCast(CREW_SKILL.getSkill());
			npc.setIsRunning(true);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, creature);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			broadcastNpcSay(npc, Say2.NPC_ALL, "?");
			((L2Attackable) npc).clearAggroList();
			startQuestTimer("TREX_ATTACK", 6000, npc, player);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == EGG)
		{
			if ((getRandom(100) <= 80) && npc.isScriptValue(0))
			{
				npc.setScriptValue(1);
				final L2Playable playable = isSummon ? attacker.getSummon() : attacker;
				for (L2Character characters : npc.getKnownList().getKnownCharactersInRadius(500))
				{
					if ((characters != null) && (characters.isL2Attackable()) && (getRandomBoolean()))
					{
						L2Attackable monster = (L2Attackable) characters;
						attackPlayer(monster, playable);
					}
				}
			}
		}
		else if (Util.contains(TREX, npc.getId()))
		{
			final L2Attackable mob = (L2Attackable) npc;
			final L2PcInstance target = (L2PcInstance) mob.getMostHated();

			if (((npc.getCurrentHp() / npc.getMaxHp()) * 100) <= 30)
			{
				if (npc.isScriptValue(3))
				{
					if (!npc.isSkillDisabled(SELFBUFF1.getSkill()))
					{
						npc.doCast(SELFBUFF1.getSkill());
					}
				}
				else if (npc.isScriptValue(1))
				{
					if (!npc.isSkillDisabled(SELFBUFF2.getSkill()))
					{
						npc.doCast(SELFBUFF2.getSkill());
					}
				}
			}
			else if ((((npc.getCurrentHp() / npc.getMaxHp()) * 100) <= 60) && (npc.isScriptValue(3)))
			{
				if (!npc.isSkillDisabled(SELFBUFF1.getSkill()))
				{
					npc.doCast(SELFBUFF1.getSkill());
				}
			}

			if (Util.calculateDistance(npc, attacker, true) > 100)
			{
				if (!npc.isSkillDisabled(LONGRANGEDMAGIC1.getSkill()) && (getRandom(100) <= (10 * npc.getScriptValue())))
				{
					npc.setTarget(attacker);
					npc.doCast(LONGRANGEDMAGIC1.getSkill());
				}
			}
			else
			{
				if (!npc.isSkillDisabled(LONGRANGEDMAGIC1.getSkill()) && (getRandom(100) <= (10 * npc.getScriptValue())))
				{
					npc.setTarget(target);
					npc.doCast(LONGRANGEDMAGIC1.getSkill());
				}
				if (!npc.isSkillDisabled(PHYSICALSPECIAL1.getSkill()) && (getRandom(100) <= (5 * npc.getScriptValue())))
				{
					npc.setTarget(target);
					npc.doCast(PHYSICALSPECIAL1.getSkill());
				}
				if (!npc.isSkillDisabled(PHYSICALSPECIAL2.getSkill()) && (getRandom(100) <= (3 * npc.getScriptValue())))
				{
					npc.setTarget(target);
					npc.doCast(PHYSICALSPECIAL2.getSkill());
				}
				if (!npc.isSkillDisabled(PHYSICALSPECIAL3.getSkill()) && (getRandom(100) <= (5 * npc.getScriptValue())))
				{
					npc.setTarget(target);
					npc.doCast(PHYSICALSPECIAL3.getSkill());
				}
			}
		}
		else
		{
			L2PcInstance target = null;
			final int probPhysicalSpecial1 = npc.getTemplate().getParameters().getInteger("ProbPhysicalSpecial1", 0);
			final int probPhysicalSpecial2 = npc.getTemplate().getParameters().getInteger("ProbPhysicalSpecial2", 0);
			final SkillsHolder selfRangeBuff1 = npc.getTemplate().getParameters().getObject("SelfRangeBuff1", SkillsHolder.class);
			final SkillsHolder physicalSpecial1 = npc.getTemplate().getParameters().getObject("PhysicalSpecial1", SkillsHolder.class);
			final SkillsHolder physicalSpecial2 = npc.getTemplate().getParameters().getObject("PhysicalSpecial2", SkillsHolder.class);

			if (((npc.getCurrentHp() / npc.getMaxHp()) * 100) <= 50)
			{
				npc.getVariables().set("SKILL_MULTIPLER", 2);
			}
			else
			{
				npc.getVariables().set("SKILL_MULTIPLER", 1);
			}

			if ((((npc.getCurrentHp() / npc.getMaxHp()) * 100) <= 30) && (npc.getVariables().getInteger("SELFBUFF_USED") == 0))
			{
				final L2Attackable mob = (L2Attackable) npc;
				target = (L2PcInstance) mob.getMostHated();
				mob.clearAggroList();
				if (!npc.isSkillDisabled(selfRangeBuff1.getSkillId()))
				{
					npc.getVariables().set("SELFBUFF_USED", 1);
					npc.doCast(selfRangeBuff1.getSkill());
					attackPlayer(mob, target);
				}
			}

			if (target != null)
			{
				if (getRandom(100) <= (probPhysicalSpecial1 * npc.getVariables().getInteger("SKILL_MULTIPLER")))
				{
					if (!npc.isSkillDisabled(physicalSpecial1.getSkill()))
					{
						npc.setTarget(target);
						npc.doCast(physicalSpecial1.getSkill());
					}
				}
				if (getRandom(100) <= (probPhysicalSpecial2 * npc.getVariables().getInteger("SKILL_MULTIPLER")))
				{
					if (!npc.isSkillDisabled(physicalSpecial2.getSkill()))
					{
						npc.setTarget(target);
						npc.doCast(physicalSpecial2.getSkill());
					}
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if ((npc.getId() == DEINO) || ((npc.getId() == ORNIT) && !npc.isScriptValue(1)))
		{
			return super.onKill(npc, killer, isSummon);
		}
		if ((npc.getId() == SAILREN) || (getRandom(100) < 3))
		{
			final L2PcInstance player = npc.getId() == SAILREN ? getRandomPartyMember(killer) : killer;
			if (player.getInventory().getSize(false) <= (player.getInventoryLimit() * 0.8))
			{
				giveItems(player, DEINONYCHUS, 1);
				final L2ItemInstance summonItem = player.getInventory().getItemByItemId(DEINONYCHUS);
				final IItemHandler handler = ItemHandler.getInstance().getHandler(summonItem.getEtcItem());
				if ((handler != null) && !player.hasPet())
				{
					handler.useItem(player, summonItem, true);
				}
				showOnScreenMsg(player, NpcStringId.LIFE_STONE_FROM_THE_BEGINNING_ACQUIRED, 2, 6000);
			}
			else
			{
				showOnScreenMsg(player, NpcStringId.WHEN_INVENTORY_WEIGHT_NUMBER_ARE_MORE_THAN_80_THE_LIFE_STONE_FROM_THE_BEGINNING_CANNOT_BE_ACQUIRED, 2, 6000);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (Util.contains(SPRIGNANT, npc.getId()))
		{
			startQuestTimer("USE_SKILL", 15000, npc, null);
		}
		else if (Util.contains(TREX, npc.getId()))
		{
			final int collectGhost = npc.getTemplate().getParameters().getInteger("CollectGhost", 0);
			final int collectDespawn = npc.getTemplate().getParameters().getInteger("CollectGhostDespawnTime", 30);

			if (collectGhost == 1)
			{
				startQuestTimer("GHOST_DESPAWN", collectDespawn * 60000, npc, null);
			}
		}
		else
		{
			npc.getVariables().set("SELFBUFF_USED", 0);
			npc.getVariables().set("SKILL_MULTIPLER", 1);
		}
		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new PrimevalIsle();
	}
}
