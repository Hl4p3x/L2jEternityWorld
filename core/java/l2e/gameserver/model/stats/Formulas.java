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
package l2e.gameserver.model.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.data.xml.HitConditionBonusParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.L2SiegeClan;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2CubicInstance;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2FortCommanderInstance;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.items.L2Armor;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.type.L2ArmorType;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.model.skills.L2TraitType;
import l2e.gameserver.model.skills.funcs.formulas.FuncArmorSet;
import l2e.gameserver.model.skills.funcs.formulas.FuncAtkAccuracy;
import l2e.gameserver.model.skills.funcs.formulas.FuncAtkCritical;
import l2e.gameserver.model.skills.funcs.formulas.FuncAtkEvasion;
import l2e.gameserver.model.skills.funcs.formulas.FuncBowAtkRange;
import l2e.gameserver.model.skills.funcs.formulas.FuncCrossBowAtkRange;
import l2e.gameserver.model.skills.funcs.formulas.FuncGatesMDefMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncGatesPDefMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncHenna;
import l2e.gameserver.model.skills.funcs.formulas.FuncMAtkCritical;
import l2e.gameserver.model.skills.funcs.formulas.FuncMAtkMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncMAtkSpeed;
import l2e.gameserver.model.skills.funcs.formulas.FuncMDefMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncMaxCpMul;
import l2e.gameserver.model.skills.funcs.formulas.FuncMaxHpMul;
import l2e.gameserver.model.skills.funcs.formulas.FuncMaxMpMul;
import l2e.gameserver.model.skills.funcs.formulas.FuncMoveSpeed;
import l2e.gameserver.model.skills.funcs.formulas.FuncPAtkMod;
import l2e.gameserver.model.skills.funcs.formulas.FuncPAtkSpeed;
import l2e.gameserver.model.skills.funcs.formulas.FuncPDefMod;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.L2CastleZone;
import l2e.gameserver.model.zone.type.L2ClanHallZone;
import l2e.gameserver.model.zone.type.L2FortZone;
import l2e.gameserver.model.zone.type.L2MotherTreeZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;
import l2e.util.StringUtil;

public final class Formulas
{
	private static final Logger _log = Logger.getLogger(Formulas.class.getName());
	
	private static final int HP_REGENERATE_PERIOD = 3000;
	
	public static final byte SHIELD_DEFENSE_FAILED = 0;
	public static final byte SHIELD_DEFENSE_SUCCEED = 1;
	public static final byte SHIELD_DEFENSE_PERFECT_BLOCK = 2;
	
	public static final byte SKILL_REFLECT_FAILED = 0;
	public static final byte SKILL_REFLECT_SUCCEED = 1;
	public static final byte SKILL_REFLECT_VENGEANCE = 2;
	
	private static final byte MELEE_ATTACK_RANGE = 40;
	
	public static int getRegeneratePeriod(L2Character cha)
	{
		return cha.isDoor() ? HP_REGENERATE_PERIOD * 100 : HP_REGENERATE_PERIOD;
	}
	
	public static Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];
		
		std[Stats.MAX_HP.ordinal()] = new Calculator();
		std[Stats.MAX_HP.ordinal()].addFunc(FuncMaxHpMul.getInstance());
		
		std[Stats.MAX_MP.ordinal()] = new Calculator();
		std[Stats.MAX_MP.ordinal()].addFunc(FuncMaxMpMul.getInstance());
		
		std[Stats.POWER_ATTACK.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK.ordinal()].addFunc(FuncPAtkMod.getInstance());
		
		std[Stats.MAGIC_ATTACK.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK.ordinal()].addFunc(FuncMAtkMod.getInstance());
		
		std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
		std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncPDefMod.getInstance());
		
		std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
		std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncMDefMod.getInstance());
		
		std[Stats.CRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.CRITICAL_RATE.ordinal()].addFunc(FuncAtkCritical.getInstance());
		
		std[Stats.MCRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.MCRITICAL_RATE.ordinal()].addFunc(FuncMAtkCritical.getInstance());
		
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
		
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
		
		std[Stats.POWER_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK_SPEED.ordinal()].addFunc(FuncPAtkSpeed.getInstance());
		
		std[Stats.MAGIC_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK_SPEED.ordinal()].addFunc(FuncMAtkSpeed.getInstance());
		
		std[Stats.MOVE_SPEED.ordinal()] = new Calculator();
		std[Stats.MOVE_SPEED.ordinal()].addFunc(FuncMoveSpeed.getInstance());
		
		return std;
	}
	
	public static Calculator[] getStdDoorCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];
		
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
		
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
		
		std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
		std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncGatesPDefMod.getInstance());
		
		std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
		std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncGatesMDefMod.getInstance());
		
		return std;
	}
	
	public static void addFuncsToNewCharacter(L2Character cha)
	{
		if (cha.isPlayer())
		{
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			cha.addStatFunc(FuncCrossBowAtkRange.getInstance());
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());
			
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_STR));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_DEX));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_INT));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_MEN));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_CON));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_WIT));
			
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_STR));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_DEX));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_INT));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_MEN));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_CON));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_WIT));
		}
		else if (cha.isSummon())
		{
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
		}
	}
	
	public static final double calcHpRegen(L2Character cha)
	{
		double init = cha.isPlayer() ? cha.getActingPlayer().getTemplate().getBaseHpRegen(cha.getLevel()) : cha.getTemplate().getBaseHpReg();
		double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;
		
		if (Config.CHAMPION_ENABLE && cha.isChampion())
		{
			hpRegenMultiplier *= Config.CHAMPION_HP_REGEN;
		}
		
		if (cha.isPlayer())
		{
			L2PcInstance player = cha.getActingPlayer();
			
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				hpRegenMultiplier *= calcFestivalRegenModifier(player);
			}
			else
			{
				double siegeModifier = calcSiegeRegenModifier(player);
				if (siegeModifier > 0)
				{
					hpRegenMultiplier *= siegeModifier;
				}
			}
			
			if (player.isInsideZone(ZoneId.CLAN_HALL) && (player.getClan() != null) && (player.getClan().getHideoutId() > 0))
			{
				L2ClanHallZone zone = ZoneManager.getInstance().getZone(player, L2ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getClanHallId();
				int clanHallIndex = player.getClan().getHideoutId();
				if ((clanHallIndex > 0) && (clanHallIndex == posChIndex))
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + ((double) clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneId.CASTLE) && (player.getClan() != null) && (player.getClan().getCastleId() > 0))
			{
				L2CastleZone zone = ZoneManager.getInstance().getZone(player, L2CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getCastleId();
				int castleIndex = player.getClan().getCastleId();
				if ((castleIndex > 0) && (castleIndex == posCastleIndex))
				{
					Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
					if (castle != null)
					{
						if (castle.getFunction(Castle.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + ((double) castle.getFunction(Castle.FUNC_RESTORE_HP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneId.FORT) && (player.getClan() != null) && (player.getClan().getFortId() > 0))
			{
				L2FortZone zone = ZoneManager.getInstance().getZone(player, L2FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getFortId();
				int fortIndex = player.getClan().getFortId();
				if ((fortIndex > 0) && (fortIndex == posFortIndex))
				{
					Fort fort = FortManager.getInstance().getFortById(fortIndex);
					if (fort != null)
					{
						if (fort.getFunction(Fort.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + ((double) fort.getFunction(Fort.FUNC_RESTORE_HP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneId.MOTHER_TREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int hpBonus = zone == null ? 0 : zone.getHpRegenBonus();
				hpRegenBonus += hpBonus;
			}
			
			if (player.isSitting())
			{
				hpRegenMultiplier *= 1.5;
			}
			else if (!player.isMoving())
			{
				hpRegenMultiplier *= 1.1;
			}
			else if (player.isRunning())
			{
				hpRegenMultiplier *= 0.7;
			}
			init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		}
		else if (cha.isPet())
		{
			init = ((L2PetInstance) cha).getPetLevelData().getPetRegenHP() * Config.PET_HP_REGEN_MULTIPLIER;
		}
		return (cha.calcStat(Stats.REGENERATE_HP_RATE, Math.max(1, init), null, null) * hpRegenMultiplier) + hpRegenBonus;
	}
	
	public static final double calcMpRegen(L2Character cha)
	{
		double init = cha.isPlayer() ? cha.getActingPlayer().getTemplate().getBaseMpRegen(cha.getLevel()) : cha.getTemplate().getBaseMpReg();
		double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;
		
		if (cha.isPlayer())
		{
			L2PcInstance player = cha.getActingPlayer();
			
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				mpRegenMultiplier *= calcFestivalRegenModifier(player);
			}
			
			if (player.isInsideZone(ZoneId.MOTHER_TREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int mpBonus = zone == null ? 0 : zone.getMpRegenBonus();
				mpRegenBonus += mpBonus;
			}
			
			if (player.isInsideZone(ZoneId.CLAN_HALL) && (player.getClan() != null) && (player.getClan().getHideoutId() > 0))
			{
				L2ClanHallZone zone = ZoneManager.getInstance().getZone(player, L2ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getClanHallId();
				int clanHallIndex = player.getClan().getHideoutId();
				if ((clanHallIndex > 0) && (clanHallIndex == posChIndex))
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + ((double) clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneId.CASTLE) && (player.getClan() != null) && (player.getClan().getCastleId() > 0))
			{
				L2CastleZone zone = ZoneManager.getInstance().getZone(player, L2CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getCastleId();
				int castleIndex = player.getClan().getCastleId();
				if ((castleIndex > 0) && (castleIndex == posCastleIndex))
				{
					Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
					if (castle != null)
					{
						if (castle.getFunction(Castle.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + ((double) castle.getFunction(Castle.FUNC_RESTORE_MP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneId.FORT) && (player.getClan() != null) && (player.getClan().getFortId() > 0))
			{
				L2FortZone zone = ZoneManager.getInstance().getZone(player, L2FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getFortId();
				int fortIndex = player.getClan().getFortId();
				if ((fortIndex > 0) && (fortIndex == posFortIndex))
				{
					Fort fort = FortManager.getInstance().getFortById(fortIndex);
					if (fort != null)
					{
						if (fort.getFunction(Fort.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + ((double) fort.getFunction(Fort.FUNC_RESTORE_MP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isSitting())
			{
				mpRegenMultiplier *= 1.5;
			}
			else if (!player.isMoving())
			{
				mpRegenMultiplier *= 1.1;
			}
			else if (player.isRunning())
			{
				mpRegenMultiplier *= 0.7;
			}
			init *= cha.getLevelMod() * BaseStats.MEN.calcBonus(cha);
		}
		else if (cha.isPet())
		{
			init = ((L2PetInstance) cha).getPetLevelData().getPetRegenMP() * Config.PET_MP_REGEN_MULTIPLIER;
		}
		return (cha.calcStat(Stats.REGENERATE_MP_RATE, Math.max(1, init), null, null) * mpRegenMultiplier) + mpRegenBonus;
	}
	
	public static final double calcCpRegen(L2Character cha)
	{
		double init = cha.isPlayer() ? cha.getActingPlayer().getTemplate().getBaseCpRegen(cha.getLevel()) : cha.getTemplate().getBaseHpReg();
		double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
		double cpRegenBonus = 0;
		
		if (cha.isPlayer())
		{
			L2PcInstance player = cha.getActingPlayer();
			
			if (player.isSitting())
			{
				cpRegenMultiplier *= 1.5;
			}
			else if (!player.isMoving())
			{
				cpRegenMultiplier *= 1.1;
			}
			else if (player.isRunning())
			{
				cpRegenMultiplier *= 0.7;
			}
		}
		else
		{
			if (!cha.isMoving())
			{
				cpRegenMultiplier *= 1.1;
			}
			else if (cha.isRunning())
			{
				cpRegenMultiplier *= 0.7;
			}
		}
		
		init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		return (cha.calcStat(Stats.REGENERATE_CP_RATE, Math.max(1, init), null, null) * cpRegenMultiplier) + cpRegenBonus;
	}
	
	@SuppressWarnings("deprecation")
	public static final double calcFestivalRegenModifier(L2PcInstance activeChar)
	{
		final int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
		final int oracle = festivalInfo[0];
		final int festivalId = festivalInfo[1];
		int[] festivalCenter;
		
		if (festivalId < 0)
		{
			return 0;
		}
		
		if (oracle == SevenSigns.CABAL_DAWN)
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
		}
		else
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];
		}
		
		double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);
		
		if (Config.DEBUG)
		{
			_log.info("Distance: " + distToCenter + ", RegenMulti: " + ((distToCenter * 2.5) / 50));
		}
		return 1.0 - (distToCenter * 0.0005);
	}
	
	public static final double calcSiegeRegenModifier(L2PcInstance activeChar)
	{
		if ((activeChar == null) || (activeChar.getClan() == null))
		{
			return 0;
		}
		
		Siege siege = SiegeManager.getInstance().getSiege(activeChar.getPosition().getX(), activeChar.getPosition().getY(), activeChar.getPosition().getZ());
		if ((siege == null) || !siege.getIsInProgress())
		{
			return 0;
		}
		
		L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getId());
		if ((siegeClan == null) || siegeClan.getFlag().isEmpty() || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true))
		{
			return 0;
		}
		return 1.5;
	}
	
	public static double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss)
	{
		double defence = target.getPDef(attacker);
		
		switch (shld)
		{
			case Formulas.SHIELD_DEFENSE_SUCCEED:
				defence += target.getShldDef();
				break;
			case Formulas.SHIELD_DEFENSE_PERFECT_BLOCK:
				return 1;
		}
		
		boolean isPvP = attacker.isPlayable() && target.isPlayer();
		boolean isPvE = attacker.isPlayable() && target.isL2Attackable();
		double power = skill.getPower(isPvP, isPvE);
		double damage = 0;
		double proximityBonus = 1;
		double graciaPhysSkillBonus = skill.isMagic() ? 1 : 1.10113;
		double ssboost = ss ? (skill.getSSBoost() > 0 ? skill.getSSBoost() : 2.04) : 1;
		double pvpBonus = 1;
		
		if (attacker.isPlayable() && target.isPlayable())
		{
			pvpBonus *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			defence *= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
		}
		
		proximityBonus = attacker.isBehindTarget() ? 1.2 : attacker.isInFrontOfTarget() ? 1.1 : 1;
		
		damage *= calcValakasTrait(attacker, target, skill);
		
		double element = calcElemental(attacker, target, skill);
		
		if (skill.getSSBoost() > 0)
		{
			damage += (((70. * graciaPhysSkillBonus * (attacker.getPAtk(target) + power)) / defence) * (attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill)) * (target.calcStat(Stats.CRIT_VULN, 1, target, skill)) * ssboost * proximityBonus * element * pvpBonus) + (((attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.1 * 70) / defence) * graciaPhysSkillBonus);
		}
		else
		{
			damage += (((70. * graciaPhysSkillBonus * (power + (attacker.getPAtk(target) * ssboost))) / defence) * (attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill)) * (target.calcStat(Stats.CRIT_VULN, 1, target, skill)) * proximityBonus * element * pvpBonus) + (((attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.1 * 70) / defence) * graciaPhysSkillBonus);
		}
		
		damage += target.calcStat(Stats.CRIT_ADD_VULN, 0, target, skill) * 6.1;
		
		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		damage *= attacker.getRandomDamageMultiplier();
		
		if (target.isL2Attackable() && !target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
		{
			int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
			if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
			{
				damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
			}
			else
			{
				damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
			}
			
		}
		return damage < 1 ? 1. : damage;
	}
	
	public static double calcBackstabDamage(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss)
	{
		if ((attacker.isPlayer() && !attacker.getAccessLevel().canGiveDamage()) || (((skill.getCondition() & L2Skill.COND_BEHIND) != 0) && !attacker.isBehind(target)))
		{
			return 0;
		}
		
		double defence = target.getPDef(attacker);
		
		switch (shld)
		{
			case Formulas.SHIELD_DEFENSE_SUCCEED:
				defence += target.getShldDef();
				break;
			case Formulas.SHIELD_DEFENSE_PERFECT_BLOCK:
				return 1;
		}
		
		boolean isPvP = attacker.isPlayable() && target.isPlayer();
		boolean isPvE = attacker.isPlayable() && target.isL2Attackable();
		double damage = attacker.getPAtk(target);
		double proximityBonus = 1;
		double graciaPhysSkillBonus = skill.isMagic() ? 1 : 1.10113;
		double ssboost = ss ? 1.5 : 1;
		double pvpBonus = 1;
		
		if (attacker.isPlayable() && target.isPlayable())
		{
			pvpBonus *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			defence *= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
		}
		
		proximityBonus = attacker.isBehindTarget() ? 1.2 : attacker.isInFrontOfTarget() ? 1 : 1.1;
		
		damage *= calcValakasTrait(attacker, target, skill);
		double element = calcElemental(attacker, target, skill);
		
		damage += (((70. * graciaPhysSkillBonus * (skill.getPower(isPvP, isPvE) + damage)) / defence) * ssboost * (attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill)) * (target.calcStat(Stats.CRIT_VULN, 1, target, skill)) * proximityBonus * element * pvpBonus) + (((attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.1 * 70) / defence) * graciaPhysSkillBonus);
		damage += target.calcStat(Stats.CRIT_ADD_VULN, 0, target, skill) * 6.1;
		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		damage *= attacker.getRandomDamageMultiplier();
		
		if (target.isL2Attackable() && !target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
		{
			int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
			if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
			{
				damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
			}
			else
			{
				damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
			}
			
		}
		return damage < 1 ? 1. : damage;
	}
	
	public static final double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean crit, boolean ss)
	{
		final boolean isPvP = attacker.isPlayable() && target.isPlayable();
		final boolean isPvE = attacker.isPlayable() && target.isL2Attackable();
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		damage *= calcValakasTrait(attacker, target, skill);
		
		if (isPvP)
		{
			if (skill == null)
			{
				defence *= target.calcStat(Stats.PVP_PHYSICAL_DEF, 1, null, null);
			}
			else
			{
				defence *= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
			}
		}
		
		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				if (!Config.ALT_GAME_SHIELD_BLOCKS)
				{
					defence += target.getShldDef();
				}
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK:
				return 1.;
		}
		
		if (ss)
		{
			damage *= 2;
		}
		
		if (skill != null)
		{
			double skillpower = skill.getPower(attacker, target, isPvP, isPvE);
			float ssboost = skill.getSSBoost();
			if (ssboost <= 0)
			{
				damage += skillpower;
			}
			else if (ssboost > 0)
			{
				if (ss)
				{
					skillpower *= ssboost;
					damage += skillpower;
				}
				else
				{
					damage += skillpower;
				}
			}
		}
		
		L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		boolean isBow = false;
		if ((weapon != null) && !attacker.isTransformed())
		{
			switch (weapon.getItemType())
			{
				case BOW:
					isBow = true;
					stat = Stats.BOW_WPN_VULN;
					break;
				case CROSSBOW:
					isBow = true;
					stat = Stats.CROSSBOW_WPN_VULN;
					break;
				case BLUNT:
					stat = Stats.BLUNT_WPN_VULN;
					break;
				case DAGGER:
					stat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					stat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					stat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					stat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					stat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					stat = Stats.SWORD_WPN_VULN;
					break;
				case BIGSWORD:
					stat = Stats.BIGSWORD_WPN_VULN;
					break;
				case BIGBLUNT:
					stat = Stats.BIGBLUNT_WPN_VULN;
					break;
				case DUALDAGGER:
					stat = Stats.DUALDAGGER_WPN_VULN;
					break;
				case RAPIER:
					stat = Stats.RAPIER_WPN_VULN;
					break;
				case ANCIENTSWORD:
					stat = Stats.ANCIENT_WPN_VULN;
					break;
			}
		}
		
		if (attacker.isServitor())
		{
			stat = Stats.PET_WPN_VULN;
		}
		
		if (crit)
		{
			damage = 2 * attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill) * target.calcStat(Stats.CRIT_VULN, target.getTemplate().getBaseCritVuln(), target, null) * ((70 * damage) / defence);
			damage += ((attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 70) / defence);
		}
		else
		{
			damage = (70 * damage) / defence;
		}
		
		if (stat != null)
		{
			damage = target.calcStat(stat, damage, target, null);
		}
		
		damage *= attacker.getRandomDamageMultiplier();
		if ((shld > 0) && Config.ALT_GAME_SHIELD_BLOCKS)
		{
			damage -= target.getShldDef();
			if (damage < 0)
			{
				damage = 0;
			}
		}
		
		if (target.isNpc())
		{
			switch (((L2Npc) target).getTemplate().getRace())
			{
				case BEAST:
					damage *= attacker.getPAtkMonsters(target);
					break;
				case ANIMAL:
					damage *= attacker.getPAtkAnimals(target);
					break;
				case PLANT:
					damage *= attacker.getPAtkPlants(target);
					break;
				case DRAGON:
					damage *= attacker.getPAtkDragons(target);
					break;
				case BUG:
					damage *= attacker.getPAtkInsects(target);
					break;
				case GIANT:
					damage *= attacker.getPAtkGiants(target);
					break;
				case MAGICCREATURE:
					damage *= attacker.getPAtkMagicCreatures(target);
					break;
				default:
					break;
			}
		}
		
		if ((damage > 0) && (damage < 1))
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}
		
		if (isPvP)
		{
			if (skill == null)
			{
				damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}
		
		if (skill != null)
		{
			damage = attacker.calcStat(Stats.PHYSICAL_SKILL_POWER, damage, null, null);
		}
		
		damage *= calcElemental(attacker, target, skill);
		if (target.isL2Attackable())
		{
			if (isBow)
			{
				if (skill != null)
				{
					damage *= attacker.calcStat(Stats.PVE_BOW_SKILL_DMG, 1, null, null);
				}
				else
				{
					damage *= attacker.calcStat(Stats.PVE_BOW_DMG, 1, null, null);
				}
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVE_PHYSICAL_DMG, 1, null, null);
			}
			if (!target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (skill != null)
				{
					if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
					{
						damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
					}
					else
					{
						damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
					}
				}
				else if (crit)
				{
					if (lvlDiff >= Config.NPC_CRIT_DMG_PENALTY.size())
					{
						damage *= Config.NPC_CRIT_DMG_PENALTY.get(Config.NPC_CRIT_DMG_PENALTY.size() - 1);
					}
					else
					{
						damage *= Config.NPC_CRIT_DMG_PENALTY.get(lvlDiff);
					}
				}
				else
				{
					if (lvlDiff >= Config.NPC_DMG_PENALTY.size())
					{
						damage *= Config.NPC_DMG_PENALTY.get(Config.NPC_DMG_PENALTY.size() - 1);
					}
					else
					{
						damage *= Config.NPC_DMG_PENALTY.get(lvlDiff);
					}
				}
			}
		}
		
		if (target.isPlayer() && (weapon != null) && (weapon.getItemType() == L2WeaponType.DAGGER) && (skill != null))
		{
			L2Armor armor = ((L2PcInstance) target).getActiveChestArmorItem();
			if (armor != null)
			{
				if (((L2PcInstance) target).isWearingHeavyArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_HEAVY;
				}
				
				if (((L2PcInstance) target).isWearingLightArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_LIGHT;
				}
				
				if (((L2PcInstance) target).isWearingMagicArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_ROBE;
				}
			}
		}
		
		if (target.isPlayer() && (weapon != null) && (weapon.getItemType() == L2WeaponType.BOW) && (skill != null))
		{
			L2Armor armor = ((L2PcInstance) target).getActiveChestArmorItem();
			if (armor != null)
			{
				if (((L2PcInstance) target).isWearingHeavyArmor())
				{
					damage /= Config.ALT_BOW_DMG_VS_HEAVY;
				}
				
				if (((L2PcInstance) target).isWearingLightArmor())
				{
					damage /= Config.ALT_BOW_DMG_VS_LIGHT;
				}
				
				if (((L2PcInstance) target).isWearingMagicArmor())
				{
					damage /= Config.ALT_BOW_DMG_VS_ROBE;
				}
			}
		}
		
		if (attacker.isPlayer())
		{
			if (((L2PcInstance) attacker).getClassId().isMage())
			{
				damage = damage * Config.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * Config.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
			}
		}
		else if (attacker.isSummon())
		{
			damage = damage * Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
		}
		else if (attacker.isNpc())
		{
			damage = damage * Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
		}
		return damage;
	}
	
	public static final double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean sps, boolean bss, boolean mcrit)
	{
		int mAtk = attacker.getMAtk(target, skill);
		int mDef = target.getMDef(attacker, skill);
		final boolean isPvP = attacker.isPlayable() && target.isPlayable();
		final boolean isPvE = attacker.isPlayable() && target.isL2Attackable();
		
		if (isPvP)
		{
			if (skill.isMagic())
			{
				mDef *= target.calcStat(Stats.PVP_MAGICAL_DEF, 1, null, null);
			}
			else
			{
				mDef *= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
			}
		}
		
		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				mDef += target.getShldDef();
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK:
				return 1;
		}
		
		mAtk *= bss ? 4 : sps ? 2 : 1;
		
		double damage = ((91 * Math.sqrt(mAtk)) / mDef) * skill.getPower(attacker, target, isPvP, isPvE);
		
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker.isPlayer())
			{
				if (calcMagicSuccess(attacker, target, skill) && ((target.getLevel() - attacker.getLevel()) <= 9))
				{
					if (skill.getSkillType() == L2SkillType.DRAIN)
					{
						attacker.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
					}
					else
					{
						attacker.sendPacket(SystemMessageId.ATTACK_FAILED);
					}
					
					damage /= 2;
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
					sm.addCharName(target);
					sm.addSkillName(skill);
					attacker.sendPacket(sm);
					
					damage = 1;
				}
			}
			
			if (target.isPlayer())
			{
				final SystemMessage sm = (skill.getSkillType() == L2SkillType.DRAIN) ? SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN) : SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC);
				sm.addCharName(attacker);
				target.sendPacket(sm);
			}
		}
		else if (mcrit)
		{
			damage *= attacker.isPlayer() && target.isPlayer() ? 2.5 : 3;
			damage *= attacker.calcStat(Stats.MAGIC_CRIT_DMG, 1, null, null);
		}
		
		damage *= attacker.getRandomDamageMultiplier();
		
		if (isPvP)
		{
			Stats stat = skill.isMagic() ? Stats.PVP_MAGICAL_DMG : Stats.PVP_PHYS_SKILL_DMG;
			damage *= attacker.calcStat(stat, 1, null, null);
		}
		damage *= target.calcStat(Stats.MAGIC_DAMAGE_VULN, 1, null, null);
		
		damage *= calcElemental(attacker, target, skill);
		
		if (target.isL2Attackable())
		{
			damage *= attacker.calcStat(Stats.PVE_MAGICAL_DMG, 1, null, null);
			if (!target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
				}
				else
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}
		
		if (attacker.isPlayer())
		{
			if (((L2PcInstance) attacker).getClassId().isMage())
			{
				damage = damage * Config.ALT_MAGES_MAGICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * Config.ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
			}
		}
		else if (attacker.isSummon())
		{
			damage = damage * Config.ALT_PETS_MAGICAL_DAMAGE_MULTI;
		}
		else if (attacker.isNpc())
		{
			damage = damage * Config.ALT_NPC_MAGICAL_DAMAGE_MULTI;
		}
		return damage;
	}
	
	public static final double calcMagicDam(L2CubicInstance attacker, L2Character target, L2Skill skill, boolean mcrit, byte shld)
	{
		int mAtk = attacker.getCubicPower();
		int mDef = target.getMDef(attacker.getOwner(), skill);
		final boolean isPvP = target.isPlayable();
		final boolean isPvE = target.isL2Attackable();
		
		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				mDef += target.getShldDef();
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK:
				return 1;
		}
		
		double damage = 91 * ((mAtk + skill.getPower(isPvP, isPvE)) / mDef);
		L2PcInstance owner = attacker.getOwner();
		
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(owner, target, skill))
		{
			if (calcMagicSuccess(owner, target, skill) && ((target.getLevel() - skill.getMagicLevel()) <= 9))
			{
				if (skill.getSkillType() == L2SkillType.DRAIN)
				{
					owner.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
				}
				else
				{
					owner.sendPacket(SystemMessageId.ATTACK_FAILED);
				}
				
				damage /= 2;
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(target);
				sm.addSkillName(skill);
				owner.sendPacket(sm);
				
				damage = 1;
			}
			
			if (target.isPlayer())
			{
				if (skill.getSkillType() == L2SkillType.DRAIN)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN);
					sm.addCharName(owner);
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC);
					sm.addCharName(owner);
					target.sendPacket(sm);
				}
			}
		}
		else if (mcrit)
		{
			damage *= 3;
		}
		
		damage *= target.calcStat(Stats.MAGIC_DAMAGE_VULN, 1, null, null);
		
		damage *= calcElemental(owner, target, skill);
		
		if (target.isL2Attackable())
		{
			damage *= attacker.getOwner().calcStat(Stats.PVE_MAGICAL_DMG, 1, null, null);
			if (!target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getOwner() != null) && ((target.getLevel() - attacker.getOwner().getLevel()) >= 2))
			{
				int lvlDiff = target.getLevel() - attacker.getOwner().getLevel() - 1;
				if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
				}
				else
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}
		
		return damage;
	}
	
	public static final boolean calcCrit(double rate, boolean skill, L2Character target)
	{
		final boolean success = rate > Rnd.get(1000);
		
		if (success)
		{
			if (target == null)
			{
				return true;
			}
			
			if (skill)
			{
				return success;
			}
			return Rnd.get((int) target.getStat().calcStat(Stats.CRIT_DAMAGE_EVASION, 100, null, null)) < 100;
		}
		return success;
	}
	
	public static final boolean calcLethalHit(L2Character activeChar, L2Character target, L2Skill skill)
	{
		double lethal2chance = 0;
		double lethal1chance = 0;
		
		if ((activeChar.isPlayer() && !activeChar.getAccessLevel().canGiveDamage()) || (((skill.getCondition() & L2Skill.COND_BEHIND) != 0) && !activeChar.isBehind(target)))
		{
			return false;
		}
		
		if (target.isRaid())
		{
			return false;
		}
		
		if (!target.isInvul())
		{
			double lethalStrikeRate = skill.getLethalStrikeRate() * calcLvlBonusMod(activeChar, target, skill);
			double halfKillRate = skill.getHalfKillRate() * calcLvlBonusMod(activeChar, target, skill);
			
			if (Rnd.get(100) < activeChar.calcStat(Stats.LETHAL_RATE, lethalStrikeRate, target, null))
			{
				lethal2chance = activeChar.calcStat(Stats.LETHAL_RATE, lethalStrikeRate, target, null);
				if ((activeChar instanceof L2PcInstance) && Config.SKILL_CHANCE_SHOW)
				{
					L2PcInstance attacker = (L2PcInstance) activeChar;
					attacker.sendMessage((new CustomMessage("Formulas.Lethal_Shot", attacker.getLang())).toString() + ": " + String.format("%1.2f", (lethal2chance / 10)) + "%");
				}
				
				if (target.isPlayer())
				{
					target.setCurrentCp(1);
					target.setCurrentHp(1);
					target.sendPacket(SystemMessageId.LETHAL_STRIKE);
				}
				else if (target.isMonster() || target.isSummon())
				{
					target.setCurrentHp(1);
				}
				activeChar.sendPacket(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL);
			}
			else if (Rnd.get(100) < activeChar.calcStat(Stats.LETHAL_RATE, halfKillRate, target, null))
			{
				lethal1chance = activeChar.calcStat(Stats.LETHAL_RATE, halfKillRate, target, null);
				if ((activeChar instanceof L2PcInstance) && Config.SKILL_CHANCE_SHOW)
				{
					L2PcInstance attacker = (L2PcInstance) activeChar;
					attacker.sendMessage((new CustomMessage("Formulas.Lethal_Shot", attacker.getLang())).toString() + ": " + String.format("%1.2f", (lethal1chance / 10)) + "%");
				}
				
				if (target.isPlayer())
				{
					target.setCurrentCp(1);
					target.sendPacket(SystemMessageId.HALF_KILL);
					target.sendPacket(SystemMessageId.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
				}
				else if (target.isMonster() || target.isSummon())
				{
					target.setCurrentHp(target.getCurrentHp() * 0.5);
				}
				activeChar.sendPacket(SystemMessageId.HALF_KILL);
			}
		}
		else
		{
			return false;
		}
		return true;
	}
	
	public static final boolean calcMCrit(double mRate)
	{
		return mRate > Rnd.get(1000);
	}
	
	public static final boolean calcAtkBreak(L2Character target, double dmg)
	{
		if (target.getFusionSkill() != null)
		{
			return true;
		}
		
		double init = 0;
		
		if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow())
		{
			init = 15;
		}
		if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
		{
			L2Weapon wpn = target.getActiveWeaponItem();
			if ((wpn != null) && (wpn.getItemType() == L2WeaponType.BOW))
			{
				init = 15;
			}
		}
		
		if (target.isRaid() || target.isInvul() || (init <= 0))
		{
			return false;
		}
		
		init += Math.sqrt(13 * dmg);
		
		init -= ((BaseStats.MEN.calcBonus(target) * 100) - 100);
		
		double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);
		
		rate = Math.max(Math.min(rate, 99), 1);
		
		return Rnd.get(100) < rate;
	}
	
	public static final int calcPAtkSpd(L2Character attacker, L2Character target, double rate)
	{
		if (rate < 2)
		{
			return 2700;
		}
		return (int) (470000 / rate);
	}
	
	public static final int calcAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
	{
		if (skill.isMagic())
		{
			return (int) ((skillTime / attacker.getMAtkSpd()) * 333);
		}
		return (int) ((skillTime / attacker.getPAtkSpd()) * 300);
	}
	
	public static boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		int chance = (80 + (2 * (attacker.getAccuracy() - target.getEvasionRate(attacker)))) * 10;
		
		chance *= HitConditionBonusParser.getInstance().getConditionBonus(attacker, target);
		
		chance = Math.max(chance, 200);
		chance = Math.min(chance, 980);
		
		return chance < Rnd.get(1000);
	}
	
	public static byte calcShldUse(L2Character attacker, L2Character target, L2Skill skill, boolean sendSysMsg)
	{
		if ((skill != null) && skill.ignoreShield())
		{
			return 0;
		}
		
		L2Item item = target.getSecondaryWeaponItem();
		if ((item == null) || !(item instanceof L2Armor) || (((L2Armor) item).getItemType() == L2ArmorType.SIGIL))
		{
			return 0;
		}
		
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * BaseStats.DEX.calcBonus(target);
		if (shldRate <= 1e-6)
		{
			return 0;
		}
		
		int degreeside = (int) target.calcStat(Stats.SHIELD_DEFENCE_ANGLE, 0, null, null) + 120;
		if ((degreeside < 360) && (!target.isFacing(attacker, degreeside)))
		{
			return 0;
		}
		
		byte shldSuccess = SHIELD_DEFENSE_FAILED;
		
		L2Weapon at_weapon = attacker.getActiveWeaponItem();
		if ((at_weapon != null) && (at_weapon.getItemType() == L2WeaponType.BOW))
		{
			shldRate *= 1.3;
		}
		
		if ((shldRate > 0) && ((100 - Config.ALT_PERFECT_SHLD_BLOCK) < Rnd.get(100)))
		{
			shldSuccess = SHIELD_DEFENSE_PERFECT_BLOCK;
		}
		else if (shldRate > Rnd.get(100))
		{
			shldSuccess = SHIELD_DEFENSE_SUCCEED;
		}
		
		if (sendSysMsg && target.isPlayer())
		{
			L2PcInstance enemy = target.getActingPlayer();
			
			switch (shldSuccess)
			{
				case SHIELD_DEFENSE_SUCCEED:
					enemy.sendPacket(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL);
					break;
				case SHIELD_DEFENSE_PERFECT_BLOCK:
					enemy.sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
					break;
			}
		}
		return shldSuccess;
	}
	
	public static byte calcShldUse(L2Character attacker, L2Character target, L2Skill skill)
	{
		return calcShldUse(attacker, target, skill, true);
	}
	
	public static byte calcShldUse(L2Character attacker, L2Character target)
	{
		return calcShldUse(attacker, target, null, true);
	}
	
	public static boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
	{
		double defence = 0;
		if (skill.isActive() && skill.isOffensive() && !skill.isNeutral())
		{
			defence = target.getMDef(actor, skill);
		}
		
		double attack = 2 * actor.getMAtk(target, skill) * (1 + (calcSkillVulnerability(actor, target, skill) / 100));
		double d = (attack - defence) / (attack + defence);
		
		if (skill.isDebuff())
		{
			if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0)
			{
				return false;
			}
		}
		
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}
	
	public static double calcSkillVulnerability(L2Character attacker, L2Character target, L2Skill skill)
	{
		double multiplier = 0;
		
		if (skill != null)
		{
			multiplier = calcSkillTraitVulnerability(multiplier, target, skill);
		}
		return multiplier;
	}
	
	public static double calcSkillTraitVulnerability(double multiplier, L2Character target, L2Skill skill)
	{
		if (skill == null)
		{
			return multiplier;
		}
		
		final L2TraitType trait = skill.getTraitType();
		
		if ((trait != null) && (trait != L2TraitType.NONE))
		{
			switch (trait)
			{
				case BLEED:
					multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
					break;
				case BOSS:
					multiplier = target.calcStat(Stats.BOSS_VULN, multiplier, target, null);
					break;
				case DERANGEMENT:
					multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
					break;
				case GUST:
					multiplier = target.calcStat(Stats.GUST_VULN, multiplier, target, null);
					break;
				case HOLD:
					multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
					break;
				case PARALYZE:
					multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
					break;
				case PHYSICAL_BLOCKADE:
					multiplier = target.calcStat(Stats.PHYSICALBLOCKADE_VULN, multiplier, target, null);
					break;
				case POISON:
					multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
					break;
				case SHOCK:
					multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
					break;
				case SLEEP:
					multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
					break;
				case VALAKAS:
					multiplier = target.calcStat(Stats.VALAKAS_VULN, multiplier, target, null);
					break;
			}
		}
		else
		{
			final L2SkillType type = skill.getSkillType();
			if (type == L2SkillType.BUFF)
			{
				multiplier = target.calcStat(Stats.BUFF_VULN, multiplier, target, null);
			}
			else if ((type == L2SkillType.DEBUFF) || (skill.isDebuff()))
			{
				multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
			}
		}
		return multiplier;
	}
	
	public static double calcSkillProficiency(L2Skill skill, L2Character attacker, L2Character target)
	{
		double multiplier = 0;
		
		if (skill != null)
		{
			multiplier = calcSkillTraitProficiency(multiplier, attacker, target, skill);
		}
		
		return multiplier;
	}
	
	public static double calcSkillTraitProficiency(double multiplier, L2Character attacker, L2Character target, L2Skill skill)
	{
		if (skill == null)
		{
			return multiplier;
		}
		
		final L2TraitType trait = skill.getTraitType();
		
		if ((trait != null) && (trait != L2TraitType.NONE))
		{
			switch (trait)
			{
				case BLEED:
					multiplier = attacker.calcStat(Stats.BLEED_PROF, multiplier, target, null);
					break;
				case DERANGEMENT:
					multiplier = attacker.calcStat(Stats.DERANGEMENT_PROF, multiplier, target, null);
					break;
				case HOLD:
					multiplier = attacker.calcStat(Stats.ROOT_PROF, multiplier, target, null);
					break;
				case PARALYZE:
					multiplier = attacker.calcStat(Stats.PARALYZE_PROF, multiplier, target, null);
					break;
				case POISON:
					multiplier = attacker.calcStat(Stats.POISON_PROF, multiplier, target, null);
					break;
				case SHOCK:
					multiplier = attacker.calcStat(Stats.STUN_PROF, multiplier, target, null);
					break;
				case SLEEP:
					multiplier = attacker.calcStat(Stats.SLEEP_PROF, multiplier, target, null);
					break;
				case VALAKAS:
					multiplier = attacker.calcStat(Stats.VALAKAS_PROF, multiplier, target, null);
					break;
			}
		}
		else
		{
			final L2SkillType type = skill.getSkillType();
			if ((type == L2SkillType.DEBUFF) || (skill.isDebuff()))
			{
				multiplier = target.calcStat(Stats.DEBUFF_PROF, multiplier, target, null);
			}
		}
		return multiplier;
	}
	
	public static double calcSkillStatMod(L2Skill skill, L2Character target)
	{
		return skill.getSaveVs() != null ? skill.getSaveVs().calcBonus(target) : 1;
	}
	
	public static double calcResMod(L2Character attacker, L2Character target, L2Skill skill)
	{
		double vuln = calcSkillVulnerability(attacker, target, skill);
		double prof = calcSkillProficiency(skill, attacker, target);
		double resMod = 1 + ((vuln + prof) / 100);
		return Math.min(Math.max(resMod, 0.1), 1.9);
	}
	
	public static double calcLvlBonusMod(L2Character attacker, L2Character target, L2Skill skill)
	{
		int attackerLvl = skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel();
		double skillLvlBonusRateMod = 1 + (skill.getLvlBonusRate() / 100.);
		double lvlMod = 1 + ((attackerLvl - target.getLevel()) / 100.);
		return skillLvlBonusRateMod * lvlMod;
	}
	
	public static double calcElementMod(L2Character attacker, L2Character target, L2Skill skill)
	{
		final byte skillElement = skill.getElement();
		if (skillElement == Elementals.NONE)
		{
			return 1;
		}
		
		int attackerElement = attacker.getAttackElement() == skillElement ? attacker.getAttackElementValue(skillElement) + skill.getElementPower() : attacker.getAttackElementValue(skillElement);
		int targetElement = target.getDefenseElementValue(skillElement);
		double elementMod = 1 + ((attackerElement - targetElement) / 1000.);
		return elementMod;
	}
	
	public static boolean calcEffectSuccess(L2Character attacker, L2Character target, EffectTemplate effect, L2Skill skill, byte shld, boolean ss, boolean sps, boolean bss)
	{
		final double baseRate = effect.getEffectPower();
		if ((baseRate < 0) || skill.hasEffectType(L2EffectType.CANCEL_DEBUFF, L2EffectType.CANCEL))
		{
			return true;
		}
		
		if (skill.isDebuff())
		{
			if ((target instanceof L2Npc) && !(target instanceof L2Attackable))
			{
				attacker.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			if (target.isEkimusFood() || ((target.isRaid() || (target instanceof L2FortCommanderInstance)) && !Config.ALLOW_RAIDBOSS_CHANCE_DEBUFF) || ((target instanceof L2GrandBossInstance) && !Config.ALLOW_GRANDBOSS_CHANCE_DEBUFF) || (target instanceof L2DoorInstance))
			{
				attacker.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			if (skill.getPower() == -1)
			{
				if (attacker.isDebug())
				{
					attacker.sendDebugMessage(skill.getName() + " effect ignoring resists");
				}
				return true;
			}
			else if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0)
			{
				return false;
			}
		}
		
		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
		{
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(skill.getName() + " effect blocked by shield");
			}
			
			return false;
		}
		
		double statMod = calcSkillStatMod(skill, target);
		double rate = (baseRate / statMod);
		
		double vuln = calcSkillTraitVulnerability(0, target, skill);
		double prof = calcSkillTraitProficiency(0, attacker, target, skill);
		double resMod = 1 + ((vuln + prof) / 100);
		
		rate *= Math.min(Math.max(resMod, 0.1), 1.9);
		
		double lvlBonusMod = calcLvlBonusMod(attacker, target, skill);
		rate *= lvlBonusMod;
		
		double elementMod = calcElementMod(attacker, target, skill);
		rate *= elementMod;
		rate = Math.min(Math.max(rate, skill.getMinChance()), skill.getMaxChance());
		
		if (attacker.isDebug() || Config.DEVELOPER)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, skill.getName(), " power:", String.valueOf(baseRate), " stat:", String.format("%1.2f", statMod), " res:", String.format("%1.2f", resMod), "(", String.format("%1.2f", prof), "/", String.format("%1.2f", vuln), ") elem:", String.format("%1.2f", elementMod), " lvl:", String.format("%1.2f", lvlBonusMod), " total:", String.valueOf(rate));
			final String result = stat.toString();
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(result);
			}
			if (Config.DEVELOPER)
			{
				_log.info(result);
			}
		}
		
		if (target.isRaid())
		{
			if (target instanceof L2GrandBossInstance)
			{
				if (Arrays.binarySearch(Config.GRANDBOSS_DEBUFF_SPECIAL, ((L2Npc) target).getId()) > 0)
				{
					rate *= Config.GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
				}
				else
				{
					rate *= Config.GRANDBOSS_CHANCE_DEBUFF;
				}
			}
			else
			{
				if (Arrays.binarySearch(Config.RAIDBOSS_DEBUFF_SPECIAL, ((L2Npc) target).getId()) > 0)
				{
					rate *= Config.RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
				}
				else
				{
					rate *= Config.RAIDBOSS_CHANCE_DEBUFF;
				}
			}
		}
		
		if (Config.SKILL_CHANCE_SHOW)
		{
			if (attacker instanceof L2PcInstance)
			{
				attacker.sendMessage(skill.getName() + ": " + String.format("%1.2f", rate) + "%");
			}
			if (target instanceof L2PcInstance)
			{
				target.sendMessage(attacker.getName() + " - " + skill.getName() + ": " + String.format("%1.2f", rate) + "%");
			}
		}
		return (Rnd.get(100) < rate);
	}
	
	public static boolean calcEffectSuccess(Env env)
	{
		final double baseRate = env.getEffect().getEffectTemplate().getEffectPower();
		if (baseRate < 0)
		{
			return true;
		}
		
		if (env.getSkill().isDebuff())
		{
			if ((env.getTarget() instanceof L2Npc) && !(env.getTarget() instanceof L2Attackable))
			{
				env.getCharacter().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			if (env.getTarget().isEkimusFood() || ((env.getTarget().isRaid() || (env.getTarget() instanceof L2FortCommanderInstance)) && !Config.ALLOW_RAIDBOSS_CHANCE_DEBUFF) || ((env.getTarget() instanceof L2GrandBossInstance) && !Config.ALLOW_GRANDBOSS_CHANCE_DEBUFF) || (env.getTarget() instanceof L2DoorInstance))
			{
				env.getCharacter().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			if (env.getSkill().getPower() == -1)
			{
				if (env.getCharacter().isDebug())
				{
					env.getCharacter().sendDebugMessage(env.getSkill().getName() + " effect ignoring resists");
				}
				return true;
			}
			else if (env.getTarget().calcStat(Stats.DEBUFF_IMMUNITY, 0, null, env.getSkill()) > 0)
			{
				return false;
			}
		}
		
		if (env.getShield() == SHIELD_DEFENSE_PERFECT_BLOCK)
		{
			if (env.getCharacter().isDebug())
			{
				env.getCharacter().sendDebugMessage(env.getSkill().getName() + " effect blocked by shield");
			}
			return false;
		}
		
		double statMod = calcSkillStatMod(env.getSkill(), env.getTarget());
		double rate = (baseRate / statMod);
		
		double resMod = calcResMod(env.getCharacter(), env.getTarget(), env.getSkill());
		rate *= resMod;
		
		double lvlBonusMod = calcLvlBonusMod(env.getCharacter(), env.getTarget(), env.getSkill());
		rate *= lvlBonusMod;
		
		double elementMod = calcElementMod(env.getCharacter(), env.getTarget(), env.getSkill());
		rate *= elementMod;
		
		rate = Math.min(Math.max(rate, env.getSkill().getMinChance()), env.getSkill().getMaxChance());
		
		if (env.getCharacter().isDebug() || Config.DEVELOPER)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, "Effect Name: ", String.valueOf(env.getEffect().getEffectTemplate().getName()), " Base Rate: ", String.valueOf(baseRate), " Stat Type: ", String.valueOf(env.getSkill().getSaveVs()), " Stat Mod: ", String.format("%1.2f", statMod), " Res Mod: ", String.format("%1.2f", resMod), " Elem Mod: ", String.format("%1.2f", elementMod), " Lvl Mod: ", String.format("%1.2f", lvlBonusMod), " Final Rate: ", String.valueOf(rate));
			final String result = stat.toString();
			if (env.getCharacter().isDebug())
			{
				env.getCharacter().sendDebugMessage(result);
			}
			if (Config.DEVELOPER)
			{
				_log.info(result);
			}
		}
		
		if (env.getTarget().isRaid())
		{
			if (env.getTarget() instanceof L2GrandBossInstance)
			{
				if (Arrays.binarySearch(Config.GRANDBOSS_DEBUFF_SPECIAL, ((L2Npc) env.getTarget()).getId()) > 0)
				{
					rate *= Config.GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
				}
				else
				{
					rate *= Config.GRANDBOSS_CHANCE_DEBUFF;
				}
			}
			else
			{
				if (Arrays.binarySearch(Config.RAIDBOSS_DEBUFF_SPECIAL, ((L2Npc) env.getTarget()).getId()) > 0)
				{
					rate *= Config.RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
				}
				else
				{
					rate *= Config.RAIDBOSS_CHANCE_DEBUFF;
				}
			}
		}
		
		if (Config.SKILL_CHANCE_SHOW)
		{
			if (env.getCharacter() instanceof L2PcInstance)
			{
				env.getCharacter().sendMessage(env.getSkill().getName() + ": " + String.format("%1.2f", rate) + "%");
			}
			if (env.getTarget() instanceof L2PcInstance)
			{
				env.getTarget().sendMessage(env.getCharacter().getName() + " - " + env.getSkill().getName() + ": " + String.format("%1.2f", rate) + "%");
			}
		}
		
		if ((Rnd.get(100) >= rate))
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
			sm.addCharName(env.getTarget());
			sm.addSkillName(env.getSkill());
			env.getCharacter().sendPacket(sm);
			return false;
		}
		return true;
	}
	
	public static boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss, boolean sps, boolean bss)
	{
		if (skill.isDebuff())
		{
			if ((target instanceof L2Npc) && !(target instanceof L2Attackable))
			{
				attacker.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			if (target.isEkimusFood() || ((target.isRaid() || (target instanceof L2FortCommanderInstance)) && !Config.ALLOW_RAIDBOSS_CHANCE_DEBUFF) || ((target instanceof L2GrandBossInstance) && !Config.ALLOW_GRANDBOSS_CHANCE_DEBUFF) || (target instanceof L2DoorInstance))
			{
				attacker.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			if (skill.getPower() == -1)
			{
				if (attacker.isDebug())
				{
					attacker.sendDebugMessage(skill.getName() + " ignoring resists");
				}
				return true;
			}
			else if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0)
			{
				return false;
			}
		}
		
		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
		{
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(skill.getName() + " blocked by shield");
			}
			
			return false;
		}
		
		double baseRate = skill.getPower();
		double statMod = calcSkillStatMod(skill, target);
		double rate = (baseRate / statMod);
		
		double resMod = calcResMod(attacker, target, skill);
		rate *= resMod;
		
		double lvlBonusMod = calcLvlBonusMod(attacker, target, skill);
		rate *= lvlBonusMod;
		
		double elementMod = calcElementMod(attacker, target, skill);
		rate *= elementMod;
		rate = Math.min(Math.max(rate, skill.getMinChance()), skill.getMaxChance());
		
		if (attacker.isDebug() || Config.DEVELOPER)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, skill.getName(), " type:", skill.getSkillType().toString(), " power:", String.valueOf(baseRate), " stat:", String.format("%1.2f", statMod), " res:", String.format("%1.2f", resMod), " elem:", String.format("%1.2f", elementMod), " lvl:", String.format("%1.2f", lvlBonusMod), " total:", String.valueOf(rate));
			final String result = stat.toString();
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(result);
			}
			if (Config.DEVELOPER)
			{
				_log.info(result);
			}
		}
		
		if (target.isRaid())
		{
			if (target instanceof L2GrandBossInstance)
			{
				if (Arrays.binarySearch(Config.GRANDBOSS_DEBUFF_SPECIAL, ((L2Npc) target).getId()) > 0)
				{
					rate *= Config.GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
				}
				else
				{
					rate *= Config.GRANDBOSS_CHANCE_DEBUFF;
				}
			}
			else
			{
				if (Arrays.binarySearch(Config.RAIDBOSS_DEBUFF_SPECIAL, ((L2Npc) target).getId()) > 0)
				{
					rate *= Config.RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
				}
				else
				{
					rate *= Config.RAIDBOSS_CHANCE_DEBUFF;
				}
			}
		}
		
		if (Config.SKILL_CHANCE_SHOW)
		{
			if (attacker instanceof L2PcInstance)
			{
				attacker.sendMessage(skill.getName() + ": " + String.format("%1.2f", rate) + "%");
			}
			if (target instanceof L2PcInstance)
			{
				target.sendMessage(attacker.getName() + " - " + skill.getName() + ": " + String.format("%1.2f", rate) + "%");
			}
		}
		return (Rnd.get(100) < rate);
	}
	
	public static boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss, boolean sps, boolean bss, int activateRate)
	{
		Env env = new Env();
		env._character = attacker;
		env._target = target;
		env._skill = skill;
		env._shield = shld;
		env._soulShot = ss;
		env._spiritShot = sps;
		env._blessedSpiritShot = bss;
		env._value = activateRate;
		return calcSkillSuccess(attacker, target, skill, shld, ss, sps, bss);
	}
	
	public static boolean calcCubicSkillSuccess(L2CubicInstance attacker, L2Character target, L2Skill skill, byte shld)
	{
		if (skill.isDebuff())
		{
			if ((target instanceof L2Npc) && !(target instanceof L2Attackable))
			{
				attacker.getOwner().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			if (target.isEkimusFood() || ((target.isRaid() || (target instanceof L2FortCommanderInstance)) && !Config.ALLOW_RAIDBOSS_CHANCE_DEBUFF) || ((target instanceof L2GrandBossInstance) && !Config.ALLOW_GRANDBOSS_CHANCE_DEBUFF) || (target instanceof L2DoorInstance))
			{
				attacker.getOwner().sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			if (skill.getPower() == -1)
			{
				return true;
			}
			else if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0)
			{
				return false;
			}
		}
		
		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
		{
			return false;
		}
		
		if (!calcBuffDebuffReflection(target, skill))
		{
			return false;
		}
		
		double baseRate = skill.getPower();
		double statMod = calcSkillStatMod(skill, target);
		double rate = (baseRate / statMod);
		
		double resMod = calcResMod(attacker.getOwner(), target, skill);
		rate *= resMod;
		
		double lvlBonusMod = calcLvlBonusMod(attacker.getOwner(), target, skill);
		rate *= lvlBonusMod;
		
		double elementMod = calcElementMod(attacker.getOwner(), target, skill);
		rate *= elementMod;
		rate = Math.min(Math.max(rate, skill.getMinChance()), skill.getMaxChance());
		
		if (attacker.getOwner().isDebug() || Config.DEVELOPER)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, skill.getName(), " type:", skill.getSkillType().toString(), " power:", String.valueOf(baseRate), " stat:", String.format("%1.2f", statMod), " res:", String.format("%1.2f", resMod), " elem:", String.format("%1.2f", elementMod), " lvl:", String.format("%1.2f", lvlBonusMod), " total:", String.valueOf(rate));
			final String result = stat.toString();
			if (attacker.getOwner().isDebug())
			{
				attacker.getOwner().sendDebugMessage(result);
			}
			if (Config.DEVELOPER)
			{
				_log.info(result);
			}
		}
		
		if (target.isRaid())
		{
			if (target instanceof L2GrandBossInstance)
			{
				if (Arrays.binarySearch(Config.GRANDBOSS_DEBUFF_SPECIAL, ((L2Npc) target).getId()) > 0)
				{
					rate *= Config.GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
				}
				else
				{
					rate *= Config.GRANDBOSS_CHANCE_DEBUFF;
				}
			}
			else
			{
				if (Arrays.binarySearch(Config.RAIDBOSS_DEBUFF_SPECIAL, ((L2Npc) target).getId()) > 0)
				{
					rate *= Config.RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
				}
				else
				{
					rate *= Config.RAIDBOSS_CHANCE_DEBUFF;
				}
			}
		}
		return (Rnd.get(100) < rate);
	}
	
	public static boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
	{
		if (skill.getPower() == -1)
		{
			return true;
		}
		
		int lvlDifference = (target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()));
		double lvlModifier = Math.pow(1.3, lvlDifference);
		float targetModifier = 1;
		if (target.isL2Attackable() && !target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_MAGIC_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 3))
		{
			int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 2;
			if (lvlDiff >= Config.NPC_SKILL_CHANCE_PENALTY.size())
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(Config.NPC_SKILL_CHANCE_PENALTY.size() - 1);
			}
			else
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(lvlDiff);
			}
		}
		final double resModifier = target.calcStat(Stats.MAGIC_SUCCESS_RES, 1, null, skill);
		final double failureModifier = attacker.calcStat(Stats.MAGIC_FAILURE_RATE, 1, target, skill);
		double rate = 100 - Math.round((float) (lvlModifier * targetModifier * resModifier * failureModifier));
		
		if (attacker instanceof L2PcInstance)
		{
			if ((target.getLevel() - attacker.getActingPlayer().getLevel()) > 6)
			{
				if (rate > skill.getMaxChance())
				{
					rate = skill.getMaxChance();
				}
				else if (rate < skill.getMinChance())
				{
					rate = skill.getMinChance();
				}
			}
			else
			{
				rate = skill.getMaxChance();
			}
		}
		
		if (attacker.isDebug() || Config.DEVELOPER)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, skill.getName(), " lvlDiff:", String.valueOf(lvlDifference), " lvlMod:", String.format("%1.2f", lvlModifier), " res:", String.format("%1.2f", resModifier), " fail:", String.format("%1.2f", failureModifier), " tgt:", String.valueOf(targetModifier), " total:", String.valueOf(rate));
			final String result = stat.toString();
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(result);
			}
			if (Config.DEVELOPER)
			{
				_log.info(result);
			}
		}
		
		if (Config.SKILL_CHANCE_SHOW)
		{
			if (attacker instanceof L2PcInstance)
			{
				attacker.sendMessage(skill.getName() + ": " + String.format("%1.2f", rate) + "%");
			}
			if (target instanceof L2PcInstance)
			{
				target.sendMessage(attacker.getName() + " - " + skill.getName() + ": " + String.format("%1.2f", rate) + "%");
			}
		}
		return (Rnd.get(100) < rate);
	}
	
	public static double calcManaDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss)
	{
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		final boolean isPvP = attacker.isPlayable() && target.isPlayable();
		final boolean isPvE = attacker.isPlayable() && target.isL2Attackable();
		double mp = target.getMaxMp();
		if (bss)
		{
			mAtk *= 4;
		}
		else if (ss)
		{
			mAtk *= 2;
		}
		
		double damage = (Math.sqrt(mAtk) * skill.getPower(attacker, target, isPvP, isPvE) * (mp / 97)) / mDef;
		damage *= (1 + (calcSkillVulnerability(attacker, target, skill) / 100));
		if (target.isL2Attackable())
		{
			damage *= attacker.calcStat(Stats.PVE_MAGICAL_DMG, 1, null, null);
			if (!target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
				}
				else
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}
		return damage;
	}
	
	public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, L2Character caster)
	{
		if ((baseRestorePercent == 0) || (baseRestorePercent == 100))
		{
			return baseRestorePercent;
		}
		
		double restorePercent = baseRestorePercent * BaseStats.WIT.calcBonus(caster);
		if ((restorePercent - baseRestorePercent) > 20.0)
		{
			restorePercent += 20.0;
		}
		
		restorePercent = Math.max(restorePercent, baseRestorePercent);
		restorePercent = Math.min(restorePercent, 90.0);
		
		return restorePercent;
	}
	
	public static boolean calcPhysicalSkillEvasion(L2Character target, L2Skill skill)
	{
		if ((skill.isMagic() && (skill.getSkillType() != L2SkillType.BLOW)) || skill.isDebuff())
		{
			return false;
		}
		return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill);
	}
	
	public static boolean calcSkillMastery(L2Character actor, L2Skill sk)
	{
		if (sk.isStatic())
		{
			return false;
		}
		
		double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 1, null, null);
		
		if (actor.isPlayer())
		{
			if (actor.getActingPlayer().isMageClass())
			{
				val *= BaseStats.INT.calcBonus(actor);
			}
			else
			{
				val *= BaseStats.STR.calcBonus(actor);
			}
		}
		return Rnd.get(100) < val;
	}
	
	public static double calcValakasTrait(L2Character attacker, L2Character target, L2Skill skill)
	{
		double calcPower = 0;
		double calcDefen = 0;
		
		if ((skill != null) && (skill.getTraitType() == L2TraitType.VALAKAS))
		{
			calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
			calcDefen = target.calcStat(Stats.VALAKAS_VULN, calcDefen, target, skill);
		}
		else
		{
			calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
			if (calcPower > 0)
			{
				calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
				calcDefen = target.calcStat(Stats.VALAKAS_VULN, calcDefen, target, skill);
			}
		}
		return 1 + ((calcDefen + calcPower) / 100);
	}
	
	public static double calcElemental(L2Character attacker, L2Character target, L2Skill skill)
	{
		int calcPower = 0;
		int calcDefen = 0;
		int calcTotal = 0;
		double result = 1.0;
		byte element;
		
		if (skill != null)
		{
			element = skill.getElement();
			if (element >= 0)
			{
				calcPower = skill.getElementPower();
				calcDefen = target.getDefenseElementValue(element);
				
				if (attacker.getAttackElement() == element)
				{
					calcPower += attacker.getAttackElementValue(element);
				}
				
				calcTotal = calcPower - calcDefen;
				if (calcTotal > 0)
				{
					if (calcTotal < 50)
					{
						result += calcTotal * 0.003948;
					}
					else if (calcTotal < 150)
					{
						result = 1.1974;
					}
					else if (calcTotal < 300)
					{
						result = 1.3973;
					}
					else
					{
						result = 1.6963;
					}
				}
				
				if (Config.DEVELOPER)
				{
					_log.info(skill.getName() + ": " + calcPower + ", " + calcDefen + ", " + result);
				}
			}
		}
		else
		{
			element = attacker.getAttackElement();
			if (element >= 0)
			{
				calcTotal = Math.max(attacker.getAttackElementValue(element) - target.getDefenseElementValue(element), 0);
				
				if (calcTotal < 50)
				{
					result += calcTotal * 0.003948;
				}
				else if (calcTotal < 150)
				{
					result = 1.1974;
				}
				else if (calcTotal < 300)
				{
					result = 1.3973;
				}
				else
				{
					result = 1.6963;
				}
				
				if (Config.DEVELOPER)
				{
					_log.info("Hit: " + calcPower + ", " + calcDefen + ", " + result);
				}
			}
		}
		return result;
	}
	
	public static void calcDamageReflected(L2Character activeChar, L2Character target, L2Skill skill, boolean crit)
	{
		boolean reflect = true;
		if ((skill.getCastRange() == -1) || (skill.getCastRange() > MELEE_ATTACK_RANGE))
		{
			reflect = false;
		}
		
		if (reflect)
		{
			final double vengeanceChance = target.getStat().calcStat(Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE, 0, target, skill);
			if (vengeanceChance > Rnd.get(100))
			{
				if (target.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK);
					sm.addCharName(activeChar);
					target.sendPacket(sm);
				}
				if (activeChar.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK);
					sm.addCharName(target);
					activeChar.sendPacket(sm);
				}
				
				double vegdamage = ((1189 * target.getPAtk(activeChar)) / activeChar.getPDef(target));
				activeChar.reduceCurrentHp(vegdamage, target, skill);
				if (crit)
				{
					activeChar.reduceCurrentHp(vegdamage, target, skill);
				}
			}
		}
	}
	
	public static boolean calcBuffDebuffReflection(L2Character target, L2Skill skill)
	{
		boolean reflect = false;
		if ((skill.getPower() == -1) || ((skill.isHeroSkill() && skill.isDebuff()) || (!skill.isDebuff() && skill.isOffensive())))
		{
			return reflect;
		}
		
		final double reflectChance = target.calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, null, skill);
		if (Rnd.get(100) < reflectChance)
		{
			reflect = true;
		}
		return reflect;
	}
	
	public static byte calcSkillReflect(L2Character target, L2Skill skill)
	{
		if (!skill.canBeReflected() || (skill.getPower() == -1))
		{
			return SKILL_REFLECT_FAILED;
		}
		
		if (!skill.isMagic() && ((skill.getCastRange() == -1) || (skill.getCastRange() > MELEE_ATTACK_RANGE)))
		{
			return SKILL_REFLECT_FAILED;
		}
		
		byte reflect = SKILL_REFLECT_FAILED;
		switch (skill.getSkillType())
		{
			case PDAM:
			case MDAM:
			case BLOW:
			case DRAIN:
			case CHARGEDAM:
			case FATAL:
			case DEATHLINK:
			case MANADAM:
			case CPDAMPERCENT:
				final Stats stat = skill.isMagic() ? Stats.VENGEANCE_SKILL_MAGIC_DAMAGE : Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE;
				final double venganceChance = target.getStat().calcStat(stat, 0, target, skill);
				if (venganceChance > Rnd.get(100))
				{
					reflect |= SKILL_REFLECT_VENGEANCE;
				}
				break;
		}
		
		final double reflectChance = target.calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, null, skill);
		if (Rnd.get(100) < reflectChance)
		{
			reflect |= SKILL_REFLECT_SUCCEED;
		}
		return reflect;
	}
	
	public static double calcFallDam(L2Character cha, int fallHeight)
	{
		if (!Config.ENABLE_FALLING_DAMAGE || (fallHeight < 0))
		{
			return 0;
		}
		final double damage = cha.calcStat(Stats.FALL, (fallHeight * cha.getMaxHp()) / 1000.0, null, null);
		return damage;
	}
	
	public static boolean calcBlowSuccess(L2Character activeChar, L2Character target, L2Skill skill)
	{
		if (((skill.getCondition() & L2Skill.COND_BEHIND) != 0) && !activeChar.isBehindTarget())
		{
			return false;
		}
		
		double dexMod = BaseStats.DEX.calcBonus(activeChar);
		double blowChance = skill.getBlowChance();
		double sideMod = (activeChar.isInFrontOfTarget()) ? 1 : (activeChar.isBehindTarget()) ? 2 : 1.5;
		double baseRate = blowChance * dexMod * sideMod;
		double rate = activeChar.calcStat(Stats.BLOW_RATE, baseRate, target, null);
		
		if (Config.SKILL_CHANCE_SHOW)
		{
			if (activeChar instanceof L2PcInstance)
			{
				activeChar.sendMessage(skill.getName() + ": " + String.format("%1.2f", blowChance) + "%");
			}
		}
		return Rnd.get(100) < rate;
	}
	
	public static List<L2Effect> calcCancelStealEffects(L2Character activeChar, L2Character target, L2Skill skill, String slot, int rate, int max)
	{
		final L2Effect[] effects = target.getAllEffects();
		List<L2Effect> canceled = new ArrayList<>(max);
		
		switch (slot)
		{
			case "buff":
			{
				int cancelMagicLvl = skill.getMagicLevel();
				final double vuln = target.calcStat(Stats.CANCEL_VULN, 0, target, null);
				final double prof = activeChar.calcStat(Stats.CANCEL_PROF, 0, target, null);
				double resMod = 1 + (((vuln + prof) * -1) / 100);
				double finalRate = rate / resMod;
				
				if (activeChar.isDebug() || Config.DEVELOPER)
				{
					final StringBuilder stat = new StringBuilder(100);
					StringUtil.append(stat, skill.getName(), " Base Rate:", String.valueOf(rate), " Magiclvl:", String.valueOf(cancelMagicLvl), " resMod:", String.format("%1.2f", resMod), " Rate:", String.format("%1.2f", finalRate));
					final String result = stat.toString();
					if (activeChar.isDebug())
					{
						activeChar.sendDebugMessage(result);
					}
					if (Config.DEVELOPER)
					{
						_log.info(result);
					}
				}
				
				if (skill.getNegateAbnormals() != null)
				{
					for (L2Effect eff : effects)
					{
						if (eff == null)
						{
							continue;
						}
						
						for (String negateAbnormalType : skill.getNegateAbnormals().keySet())
						{
							if (negateAbnormalType.equalsIgnoreCase(eff.getAbnormalType()) && (skill.getNegateAbnormals().get(negateAbnormalType) >= eff.getAbnormalLvl()))
							{
								if (calcCancelSuccess(eff, cancelMagicLvl, (int) finalRate, skill))
								{
									eff.exit();
								}
							}
						}
					}
				}
				else
				{
					int lastCanceledSkillId = 0;
					L2Effect effect;
					for (int i = effects.length; --i >= 0;)
					{
						effect = effects[i];
						if (effect == null)
						{
							continue;
						}
						
						if (!effect.canBeStolen())
						{
							effects[i] = null;
							continue;
						}
						
						if ((effect.getAbnormalTime() - effect.getTime()) < 5)
						{
							effects[i] = null;
							continue;
						}
						
						if (!effect.getSkill().isDance())
						{
							continue;
						}
						
						if (!calcCancelSuccess(effect, cancelMagicLvl, (int) finalRate, skill))
						{
							continue;
						}
						
						if (effect.getSkill().getId() != lastCanceledSkillId)
						{
							lastCanceledSkillId = effect.getSkill().getId();
							max--;
						}
						
						canceled.add(effect);
						if (max == 0)
						{
							break;
						}
					}
					
					if (max > 0)
					{
						lastCanceledSkillId = 0;
						for (int i = effects.length; --i >= 0;)
						{
							effect = effects[i];
							if (effect == null)
							{
								continue;
							}
							
							if (effect.getSkill().isDance())
							{
								continue;
							}
							
							if (!calcCancelSuccess(effect, cancelMagicLvl, (int) finalRate, skill))
							{
								continue;
							}
							
							if (effect.getSkill().getId() != lastCanceledSkillId)
							{
								lastCanceledSkillId = effect.getSkill().getId();
								max--;
							}
							
							canceled.add(effect);
							if (max == 0)
							{
								break;
							}
						}
					}
				}
				break;
			}
			case "debuff":
			{
				L2Effect effect;
				for (int i = effects.length; --i >= 0;)
				{
					effect = effects[i];
					if (effect.getSkill().isDebuff() && effect.getSkill().canBeDispeled() && (Rnd.get(100) <= rate))
					{
						canceled.add(effect);
						if (canceled.size() >= max)
						{
							break;
						}
					}
				}
				break;
			}
		}
		return canceled;
	}
	
	public static boolean calcCancelSuccess(L2Effect eff, int cancelMagicLvl, int rate, L2Skill skill)
	{
		rate *= eff.getSkill().getMagicLevel() > 0 ? 1 + ((cancelMagicLvl - eff.getSkill().getMagicLevel()) / 100.) : 1;
		return Rnd.get(100) < Math.min(Math.max(rate, skill.getMinChance()), skill.getMaxChance());
	}
	
	public static int calcEffectAbnormalTime(Env env, EffectTemplate template)
	{
		final L2Character caster = env.getCharacter();
		final L2Character target = env.getTarget();
		final L2Skill skill = env.getSkill();
		int time = (template.getAbnormalTime() != 0) || (skill == null) ? template.getAbnormalTime() : skill.isPassive() || skill.isToggle() ? -1 : skill.getAbnormalTime();
		
		if ((target != null) && target.isServitor() && (skill != null) && skill.isAbnormalInstant())
		{
			time /= 2;
		}
		
		if (env.isSkillMastery())
		{
			time *= 2;
		}
		
		if ((caster != null) && (target != null) && (skill != null) && skill.isDebuff())
		{
			double statMod = calcSkillStatMod(skill, target);
			double resMod = calcResMod(caster, target, skill);
			double lvlBonusMod = calcLvlBonusMod(caster, target, skill);
			double elementMod = calcElementMod(caster, target, skill);
			time = (int) Math.ceil(Math.min(Math.max((time * resMod * lvlBonusMod * elementMod) / statMod, time * 0.5), time));
		}
		return time;
	}
	
	public static boolean calcProbability(double baseChance, L2Character attacker, L2Character target, L2Skill skill)
	{
		return Rnd.get(100) < (((((skill.getMagicLevel() + baseChance) - target.getLevel()) + 30) - target.getINT()) * Formulas.calcElemental(attacker, target, skill));
	}
}