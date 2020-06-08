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
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class Monastery extends AbstractNpcAI
{
	private static final int CAPTAIN = 18910;
	private static final int KNIGHT = 18909;
	private static final int SCARECROW = 18912;

	private static final int[] SOLINA_CLAN =
	{
	                22789,
	                22790,
	                22791,
	                22793
	};

	private static final int[] DIVINITY_CLAN =
	{
	                22794,
	                22795
	};

	private static final NpcStringId[] SOLINA_KNIGHTS_MSG =
	{
	                NpcStringId.PUNISH_ALL_THOSE_WHO_TREAD_FOOTSTEPS_IN_THIS_PLACE,
	                NpcStringId.WE_ARE_THE_SWORD_OF_TRUTH_THE_SWORD_OF_SOLINA,
	                NpcStringId.WE_RAISE_OUR_BLADES_FOR_THE_GLORY_OF_SOLINA
	};

	private static final NpcStringId[] DIVINITY_MSG =
	{
	                NpcStringId.S1_WHY_WOULD_YOU_CHOOSE_THE_PATH_OF_DARKNESS,
	                NpcStringId.S1_HOW_DARE_YOU_DEFY_THE_WILL_OF_EINHASAD
	};

	private static final SkillsHolder DECREASE_SPEED = new SkillsHolder(4589, 8);

	private Monastery(String name, String descr)
	{
		super(name, descr);

		addSeeCreatureId(SOLINA_CLAN);
		addSeeCreatureId(CAPTAIN, KNIGHT);

		addSkillSeeId(DIVINITY_CLAN);

		addAttackId(KNIGHT, CAPTAIN);

		addSpawnId(KNIGHT);

		for (L2Spawn spawn : SpawnParser.getInstance().getSpawnData())
		{
			switch (spawn.getId())
			{
				case KNIGHT:
					startQuestTimer("training", 5000, spawn.getLastSpawn(), null, true);
					break;
				case SCARECROW:
					spawn.getLastSpawn().setIsInvul(true);
					spawn.getLastSpawn().disableCoreAI(true);
					break;
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "training":
				if (!npc.isInCombat() && (getRandom(100) < 25))
				{
					for (L2Character character : npc.getKnownList().getKnownCharactersInRadius(300))
					{
						if (!character.isPlayable() && (((L2Npc) character).getId() == SCARECROW))
						{
							for (L2Skill skill : npc.getAllSkills())
							{
								if (skill.isActive())
								{
									npc.disableSkill(skill, 0);
								}
							}
							npc.setRunning();
							((L2Attackable) npc).addDamageHate(character, 0, 100);
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null);
							break;
						}
					}
				}
				break;
			case "checking":
				if (!npc.getKnownList().getKnownCharacters().contains(player))
				{
					cancelQuestTimer("checking", npc, player);
					return super.onAdvEvent(event, npc, player);
				}

				if ((player.isInvisible()) || (player.isSilentMoving()) || (player.getActiveWeaponInstance() == null))
				{
					npc.setTarget(null);
					npc.getAI().stopFollow();
					((L2Attackable) npc).getAggroList().remove(player);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, player, null);

					for (L2Character character : player.getKnownList().getKnownCharactersInRadius(500))
					{
						if ((character instanceof L2MonsterInstance) && (character.getTarget() == player))
						{
							character.setTarget(null);
							character.getAI().stopFollow();
							((L2Attackable) character).getAggroList().remove(player);
							character.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, player, null);
						}
					}
					return super.onAdvEvent(event, npc, player);
				}

				final double distance = Math.sqrt(npc.getPlanDistanceSq(player.getX(), player.getY()));
				if (((distance < 500) && !player.isDead() && GeoClient.getInstance().canSeeTarget(npc, player)))
				{
					switch (npc.getId())
					{
						case CAPTAIN:
						case KNIGHT:
						{
							npc.setRunning();
							npc.setTarget(player);
							((L2Attackable) npc).addDamageHate(player, 0, 999);
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player, null);
							break;
						}
						default:
						{
							if (player.getActiveWeaponInstance() != null)
							{
								npc.setRunning();
								npc.setTarget(player);
								if (getRandom(10) < 2)
								{
									broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.YOU_CANNOT_CARRY_A_WEAPON_WITHOUT_AUTHORIZATION);
								}
								npc.doCast(DECREASE_SPEED.getSkill());
								((L2Attackable) npc).addDamageHate(player, 0, 999);
								npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player, null);
							}
						}
					}
				}
				break;
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		if (getRandom(10) < 1)
		{
			broadcastNpcSay(npc, Say2.NPC_ALL, SOLINA_KNIGHTS_MSG[getRandom(2)]);
		}
		return super.onAttack(npc, player, damage, isSummon);
	}

	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (creature.isPlayer())
		{
			startQuestTimer("checking", 2000, npc, (L2PcInstance) creature, true);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if ((skill.getSkillType() == L2SkillType.AGGDAMAGE) && (targets.length != 0))
		{
			for (L2Object obj : targets)
			{
				if (obj.equals(npc))
				{
					NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), DIVINITY_MSG[getRandom(1)]);
					packet.addStringParameter(caster.getName());
					npc.broadcastPacket(packet);
					((L2Attackable) npc).addDamageHate(caster, 0, 999);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
					break;
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.FOR_THE_GLORY_OF_SOLINA);
		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new Monastery(Monastery.class.getSimpleName(), "ai");
	}
}
