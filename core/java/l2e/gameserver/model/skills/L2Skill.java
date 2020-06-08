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
package l2e.gameserver.model.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.handler.ITargetTypeHandler;
import l2e.gameserver.handler.TargetHandler;
import l2e.gameserver.model.ChanceCondition;
import l2e.gameserver.model.L2ExtractableProductItem;
import l2e.gameserver.model.L2ExtractableSkill;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2CubicInstance;
import l2e.gameserver.model.actor.instance.L2CustomBWBaseInstance;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2e.gameserver.model.conditions.Condition;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.interfaces.IChanceSkillTrigger;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

public abstract class L2Skill implements IChanceSkillTrigger, IIdentifiable
{
	protected static final Logger _log = Logger.getLogger(L2Skill.class.getName());
	
	private static final L2Object[] EMPTY_TARGET_LIST = new L2Object[0];
	private static final L2Effect[] _emptyEffectSet = new L2Effect[0];
	private static final Func[] _emptyFunctionSet = new Func[0];
	
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_EXPERTISE = 239;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_CLAN_LUCK = 390;
	public static final int SKILL_ONYX_BEAST_TRANSFORMATION = 617;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;
	public static final int SKILL_NPC_RACE = 4416;
	
	public static final boolean geoEnabled = Config.GEODATA;
	
	public static final int COND_BEHIND = 0x0008;
	public static final int COND_CRIT = 0x0010;
	
	private final int _id;
	private final int _level;
	
	private final int _displayId;
	private final int _displayLevel;
	
	private final String _name;
	private final L2SkillOpType _operateType;
	private final int _magic;
	private final L2TraitType _traitType;
	private final boolean _staticReuse;
	private final boolean _staticDamage;
	private final int _mpConsume;
	private final int _mpInitialConsume;
	private final int _hpConsume;
	
	private final int _activateRate;
	private final int _levelModifier;
	
	private final int _itemConsumeCount;
	private final int _itemConsumeId;
	
	private final int _castRange;
	private final int _effectRange;
	
	private final int _abnormalLvl;
	private final boolean _isAbnormalInstant;
	private final int _abnormalTime;
	private final boolean _stayAfterDeath;
	private final boolean _stayOnSubclassChange;
	private final int[] _negateCasterId;
	
	private final int _refId;
	private final int _hitTime;
	private final int[] _hitTimings;
	private final int _coolTime;
	private final int _reuseHashCode;
	private final int _reuseDelay;
	
	private final L2TargetType _targetType;
	private final int _feed;
	private final double _power;
	private final double _pvpPower;
	private final double _pvePower;
	private final int _magicLevel;
	private final int _lvlBonusRate;
	private final int _minChance;
	private final int _maxChance;
	private final int _blowChance;
	
	private final boolean _isNeutral;
	private final int _affectRange;
	private final int[] _affectLimit = new int[2];
	
	private final L2SkillType _skillType;
	private final int _effectId;
	private final int _effectLvl;
	
	private final boolean _nextActionIsAttack;
	
	private final boolean _removedOnAnyActionExceptMove;
	private final boolean _removedOnDamage;
	
	private final byte _element;
	private final int _elementPower;
	
	private final BaseStats _saveVs;
	
	private final int _condition;
	private final int _conditionValue;
	private final boolean _overhit;
	
	private final int _minPledgeClass;
	private final boolean _isOffensive;
	private final boolean _isPVP;
	private final int _chargeConsume;
	private final int _triggeredId;
	private final int _triggeredLevel;
	private final String _chanceType;
	private final int _soulMaxConsume;
	private final boolean _dependOnTargetBuff;
	
	private final int _afterEffectId;
	private final int _afterEffectLvl;
	private final boolean _isHeroSkill;
	private final boolean _isGMSkill;
	private final boolean _isSevenSigns;
	
	private final int _baseCritRate;
	private final int _halfKillRate;
	private final int _lethalStrikeRate;
	private final boolean _directHpDmg;
	private final boolean _isTriggeredSkill;
	private final float _sSBoost;
	private final int _aggroPoints;
	
	private List<Condition> _preCondition;
	private List<Condition> _itemPreCondition;
	private FuncTemplate[] _funcTemplates;
	public EffectTemplate[] _effectTemplates;
	private EffectTemplate[] _effectTemplatesSelf;
	private EffectTemplate[] _effectTemplatesPassive;
	
	protected ChanceCondition _chanceCondition = null;
	
	private final String _flyType;
	private final int _flyRadius;
	private final boolean _flyToBack;
	private final float _flyCourse;
	
	private final boolean _isDebuff;
	
	private final String _attribute;
	
	private final boolean _ignoreShield;
	
	private final boolean _isSuicideAttack;
	private final boolean _canBeReflected;
	private final boolean _canBeDispeled;
	
	private final boolean _isClanSkill;
	private final boolean _excludedFromCheck;
	private final boolean _simultaneousCast;
	
	private L2ExtractableSkill _extractableItems = null;
	
	private int _npcId = 0;
	
	private final String _icon;
	
	private byte[] _effectTypes;
	
	private final Map<String, Byte> _negateAbnormals;
	
	protected L2Skill(StatsSet set)
	{
		_isAbnormalInstant = set.getBool("abnormalInstant", false);
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		_refId = set.getInteger("referenceId", 0);
		_displayId = set.getInteger("displayId", _id);
		_displayLevel = set.getInteger("displayLevel", _level);
		_name = set.getString("name", "");
		_operateType = set.getEnum("operateType", L2SkillOpType.class);
		_magic = set.getInteger("isMagic", 0);
		_traitType = set.getEnum("trait", L2TraitType.class, L2TraitType.NONE);
		_staticReuse = set.getBool("staticReuse", false);
		_staticDamage = set.getBool("staticDamage", false);
		_mpConsume = set.getInteger("mpConsume", 0);
		_mpInitialConsume = set.getInteger("mpInitialConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_itemConsumeCount = set.getInteger("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_afterEffectId = set.getInteger("afterEffectId", 0);
		_afterEffectLvl = set.getInteger("afterEffectLvl", 1);
		
		_castRange = set.getInteger("castRange", -1);
		_effectRange = set.getInteger("effectRange", -1);
		
		_abnormalLvl = set.getInteger("abnormalLvl", -1);
		String negateCasterId = set.getString("negateCasterId", null);
		if (negateCasterId != null)
		{
			String[] valuesSplit = negateCasterId.split(",");
			_negateCasterId = new int[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length; i++)
			{
				_negateCasterId[i] = Integer.parseInt(valuesSplit[i]);
			}
		}
		else
		{
			_negateCasterId = new int[0];
		}
		
		int abnormalTime = set.getInteger("abnormalTime", 1);
		if (Config.ENABLE_MODIFY_SKILL_DURATION && Config.SKILL_DURATION_LIST.containsKey(getId()))
		{
			if ((getLevel() < 100) || (getLevel() > 140))
			{
				abnormalTime = Config.SKILL_DURATION_LIST.get(getId());
			}
			else if ((getLevel() >= 100) && (getLevel() < 140))
			{
				abnormalTime += Config.SKILL_DURATION_LIST.get(getId());
			}
		}
		_abnormalTime = abnormalTime;
		
		_attribute = set.getString("attribute", "");
		
		String negateAbnormals = set.getString("negateAbnormals", null);
		if ((negateAbnormals != null) && !negateAbnormals.isEmpty())
		{
			_negateAbnormals = new FastMap<>();
			for (String ngtStack : negateAbnormals.split(";"))
			{
				String[] ngt = ngtStack.split(",");
				if (ngt.length == 1)
				{
					_negateAbnormals.put(ngt[0], Byte.MAX_VALUE);
				}
				else if (ngt.length == 2)
				{
					try
					{
						_negateAbnormals.put(ngt[0], Byte.parseByte(ngt[1]));
					}
					catch (Exception e)
					{
						throw new IllegalArgumentException("SkillId: " + _id + " Byte value required, but found: " + ngt[1]);
					}
				}
				else
				{
					throw new IllegalArgumentException("SkillId: " + _id + ": Incorrect negate Abnormals for " + ngtStack + ". Lvl: abnormalType1,abnormalLvl1;abnormalType2,abnormalLvl2;abnormalType3,abnormalLvl3... or abnormalType1;abnormalType2;abnormalType3...");
				}
			}
		}
		else
		{
			_negateAbnormals = null;
		}
		
		_stayAfterDeath = set.getBool("stayAfterDeath", false);
		_stayOnSubclassChange = set.getBool("stayOnSubclassChange", true);
		
		_isNeutral = set.getBool("neutral", false);
		_hitTime = set.getInteger("hitTime", 0);
		String hitTimings = set.getString("hitTimings", null);
		if (hitTimings != null)
		{
			try
			{
				String[] valuesSplit = hitTimings.split(",");
				_hitTimings = new int[valuesSplit.length];
				for (int i = 0; i < valuesSplit.length; i++)
				{
					_hitTimings[i] = Integer.parseInt(valuesSplit[i]);
				}
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("SkillId: " + _id + " invalid hitTimings value: " + hitTimings + ", \"percent,percent,...percent\" required");
			}
		}
		else
		{
			_hitTimings = new int[0];
		}
		
		_coolTime = set.getInteger("coolTime", 0);
		_isDebuff = set.getBool("isDebuff", false);
		_feed = set.getInteger("feed", 0);
		_reuseHashCode = SkillHolder.getSkillHashCode(_id, _level);
		
		if (Config.ENABLE_MODIFY_SKILL_REUSE && Config.SKILL_REUSE_LIST.containsKey(_id))
		{
			if (Config.DEBUG)
			{
				_log.info("*** Skill " + _name + " (" + _level + ") changed reuse from " + set.getInteger("reuseDelay", 0) + " to " + Config.SKILL_REUSE_LIST.get(_id) + " seconds.");
			}
			_reuseDelay = Config.SKILL_REUSE_LIST.get(_id);
		}
		else
		{
			_reuseDelay = set.getInteger("reuseDelay", 0);
		}
		
		_affectRange = set.getInteger("affectRange", 0);
		
		final String affectLimit = set.getString("affectLimit", null);
		if (affectLimit != null)
		{
			try
			{
				String[] valuesSplit = affectLimit.split("-");
				_affectLimit[0] = Integer.parseInt(valuesSplit[0]);
				_affectLimit[1] = Integer.parseInt(valuesSplit[1]);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("SkillId: " + _id + " invalid affectLimit value: " + affectLimit + ", \"percent-percent\" required");
			}
		}
		
		_targetType = set.getEnum("targetType", L2TargetType.class);
		_power = set.getFloat("power", 0.f);
		_pvpPower = set.getFloat("pvpPower", (float) getPower());
		_pvePower = set.getFloat("pvePower", (float) getPower());
		_magicLevel = set.getInteger("magicLvl", 0);
		_lvlBonusRate = set.getInteger("lvlBonusRate", 0);
		_minChance = set.getInteger("minChance", Config.MIN_ABNORMAL_STATE_SUCCESS_RATE);
		_maxChance = set.getInteger("maxChance", Config.MAX_ABNORMAL_STATE_SUCCESS_RATE);
		_ignoreShield = set.getBool("ignoreShld", false);
		_skillType = set.getEnum("skillType", L2SkillType.class, L2SkillType.DUMMY);
		_effectId = set.getInteger("effectId", 0);
		_effectLvl = set.getInteger("effectLevel", 0);
		
		_nextActionIsAttack = set.getBool("nextActionAttack", false);
		
		_removedOnAnyActionExceptMove = set.getBool("removedOnAnyActionExceptMove", false);
		_removedOnDamage = set.getBool("removedOnDamage", false);
		
		_element = set.getByte("element", (byte) -1);
		_elementPower = set.getInteger("elementPower", 0);
		
		_activateRate = set.getInteger("activateRate", -1);
		_levelModifier = set.getInteger("levelModifier", 1);
		
		_saveVs = set.getEnum("saveVs", BaseStats.class, BaseStats.NULL);
		
		_condition = set.getInteger("condition", 0);
		_conditionValue = set.getInteger("conditionValue", 0);
		_overhit = set.getBool("overHit", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		_isOffensive = set.getBool("offensive", false);
		_isPVP = set.getBool("pvp", false);
		_chargeConsume = set.getInteger("chargeConsume", 0);
		_triggeredId = set.getInteger("triggeredId", 0);
		_triggeredLevel = set.getInteger("triggeredLevel", 1);
		_chanceType = set.getString("chanceType", "");
		if (!_chanceType.isEmpty())
		{
			_chanceCondition = ChanceCondition.parse(set);
		}
		
		_soulMaxConsume = set.getInteger("soulMaxConsumeCount", 0);
		_blowChance = set.getInteger("blowChance", 0);
		
		_isHeroSkill = SkillTreesParser.getInstance().isHeroSkill(_id, _level);
		_isGMSkill = SkillTreesParser.getInstance().isGMSkill(_id, _level);
		_isSevenSigns = (_id > 4360) && (_id < 4367);
		_isClanSkill = SkillTreesParser.getInstance().isClanSkill(_id, _level);
		
		_baseCritRate = set.getInteger("baseCritRate", 0);
		_halfKillRate = set.getInteger("halfKillRate", 0);
		_lethalStrikeRate = set.getInteger("lethalStrikeRate", 0);
		
		_directHpDmg = set.getBool("dmgDirectlyToHp", false);
		_isTriggeredSkill = set.getBool("isTriggeredSkill", false);
		_sSBoost = set.getFloat("SSBoost", 0.f);
		_aggroPoints = set.getInteger("aggroPoints", 0);
		
		_flyType = set.getString("flyType", null);
		_flyRadius = set.getInteger("flyRadius", 0);
		_flyToBack = set.getBool("flyToBack", false);
		_flyCourse = set.getFloat("flyCourse", 0);
		_canBeReflected = set.getBool("canBeReflected", true);
		
		_canBeDispeled = set.getBool("canBeDispeled", true);
		
		_excludedFromCheck = set.getBool("excludedFromCheck", false);
		_dependOnTargetBuff = set.getBool("dependOnTargetBuff", false);
		_simultaneousCast = set.getBool("simultaneousCast", false);
		
		String capsuled_items = set.getString("capsuled_items_skill", null);
		if (capsuled_items != null)
		{
			if (capsuled_items.isEmpty())
			{
				_log.warning("Empty Extractable Item Skill data in Skill Id: " + _id);
			}
			
			_extractableItems = parseExtractableSkill(_id, _level, capsuled_items);
		}
		_npcId = set.getInteger("npcId", 0);
		_icon = set.getString("icon", "icon.skill0000");
	}
	
	public abstract void useSkill(L2Character caster, L2Object[] targets);
	
	public final int getConditionValue()
	{
		return _conditionValue;
	}
	
	public final int getCondition()
	{
		return _condition;
	}
	
	public final L2SkillType getSkillType()
	{
		return _skillType;
	}
	
	public final L2TraitType getTraitType()
	{
		return _traitType;
	}
	
	public final byte getElement()
	{
		return _element;
	}
	
	public final int getElementPower()
	{
		return _elementPower;
	}
	
	public final L2TargetType getTargetType()
	{
		return _targetType;
	}
	
	public final boolean isOverhit()
	{
		return _overhit;
	}
	
	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}
	
	public final boolean allowOnTransform()
	{
		return isPassive();
	}
	
	public final double getPower(L2Character activeChar, L2Character target, boolean isPvP, boolean isPvE)
	{
		if (activeChar == null)
		{
			return getPower(isPvP, isPvE);
		}
		
		switch (_skillType)
		{
			case DEATHLINK:
			{
				return getPower(isPvP, isPvE) * (-((activeChar.getCurrentHp() * 2) / activeChar.getMaxHp()) + 2);
			}
			case FATAL:
			{
				return getPower(isPvP, isPvE) * (-((target.getCurrentHp() * 2) / target.getMaxHp()) + 2);
			}
			default:
			{
				return getPower(isPvP, isPvE);
			}
		}
	}
	
	public final double getPower()
	{
		return _power;
	}
	
	public final Map<String, Byte> getNegateAbnormals()
	{
		return _negateAbnormals;
	}
	
	public final int getAbnormalLvl()
	{
		return _abnormalLvl;
	}
	
	public final double getPower(boolean isPvP, boolean isPvE)
	{
		return isPvE ? _pvePower : isPvP ? _pvpPower : _power;
	}
	
	public final boolean isAbnormalInstant()
	{
		return _isAbnormalInstant;
	}
	
	public final int getAbnormalTime()
	{
		return _abnormalTime;
	}
	
	public final int[] getNegateCasterId()
	{
		return _negateCasterId;
	}
	
	public final int getMagicLevel()
	{
		return _magicLevel;
	}
	
	public final int getLvlBonusRate()
	{
		return _lvlBonusRate;
	}
	
	public final int getMinChance()
	{
		return _minChance;
	}
	
	public final int getMaxChance()
	{
		return _maxChance;
	}
	
	public final boolean isRemovedOnAnyActionExceptMove()
	{
		return _removedOnAnyActionExceptMove;
	}
	
	public final boolean isRemovedOnDamage()
	{
		return _removedOnDamage;
	}
	
	public final int getEffectId()
	{
		return _effectId;
	}
	
	public final int getEffectLvl()
	{
		return _effectLvl;
	}
	
	public final boolean nextActionIsAttack()
	{
		return _nextActionIsAttack;
	}
	
	public final int getCastRange()
	{
		return _castRange;
	}
	
	public final int getEffectRange()
	{
		return _effectRange;
	}
	
	public final int getHpConsume()
	{
		return _hpConsume;
	}
	
	@Override
	public final int getId()
	{
		return _id;
	}
	
	public final boolean isDebuff()
	{
		return _isDebuff;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public int getDisplayLevel()
	{
		return _displayLevel;
	}
	
	public int getTriggeredId()
	{
		return _triggeredId;
	}
	
	public int getTriggeredLevel()
	{
		return _triggeredLevel;
	}
	
	public boolean triggerAnotherSkill()
	{
		return _triggeredId > 1;
	}
	
	public final BaseStats getSaveVs()
	{
		return _saveVs;
	}
	
	public final int getItemConsume()
	{
		return _itemConsumeCount;
	}
	
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	public final int getLevel()
	{
		return _level;
	}
	
	public final boolean isPhysical()
	{
		return _magic == 0;
	}
	
	public final boolean isMagic()
	{
		return _magic == 1;
	}
	
	public final boolean isDance()
	{
		return _magic == 3;
	}
	
	public final boolean isStatic()
	{
		return _magic == 2;
	}
	
	public final boolean isStaticReuse()
	{
		return _staticReuse;
	}
	
	public final boolean isStaticDamage()
	{
		return _staticDamage;
	}
	
	public final int getMpConsume()
	{
		return _mpConsume;
	}
	
	public final int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public final int getReuseHashCode()
	{
		return _reuseHashCode;
	}
	
	public final int getHitTime()
	{
		return _hitTime;
	}
	
	public final int getHitCounts()
	{
		return _hitTimings.length;
	}
	
	public final int[] getHitTimings()
	{
		return _hitTimings;
	}
	
	public final int getCoolTime()
	{
		return _coolTime;
	}
	
	public final int getAffectRange()
	{
		return _affectRange;
	}
	
	public final int getAffectLimit()
	{
		return (_affectLimit[0] + Rnd.get(_affectLimit[1]));
	}
	
	public final boolean isActive()
	{
		return (_operateType != null) && _operateType.isActive();
	}
	
	public final boolean isPassive()
	{
		return (_operateType != null) && _operateType.isPassive();
	}
	
	public final boolean isToggle()
	{
		return (_operateType != null) && _operateType.isToggle();
	}
	
	public final boolean isChance()
	{
		return (_chanceCondition != null) && isPassive();
	}
	
	public final boolean isTriggeredSkill()
	{
		return _isTriggeredSkill;
	}
	
	public final float getSSBoost()
	{
		return _sSBoost;
	}
	
	public final int getAggroPoints()
	{
		return _aggroPoints;
	}
	
	public final boolean useSoulShot()
	{
		switch (getSkillType())
		{
			case PDAM:
			case CHARGEDAM:
			case BLOW:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean useSpiritShot()
	{
		return _magic == 1;
	}
	
	public final boolean useFishShot()
	{
		return ((getSkillType() == L2SkillType.PUMPING) || (getSkillType() == L2SkillType.REELING));
	}
	
	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}
	
	public final boolean isOffensive()
	{
		return _isOffensive || isPVP();
	}
	
	public final boolean isNeutral()
	{
		return _isNeutral;
	}
	
	public final boolean isPVP()
	{
		return _isPVP;
	}
	
	public final boolean isHeroSkill()
	{
		return _isHeroSkill;
	}
	
	public final boolean isGMSkill()
	{
		return _isGMSkill;
	}
	
	public final boolean is7Signs()
	{
		return _isSevenSigns;
	}
	
	public final int getChargeConsume()
	{
		return _chargeConsume;
	}
	
	public final int getMaxSoulConsumeCount()
	{
		return _soulMaxConsume;
	}
	
	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public final int getHalfKillRate()
	{
		return _halfKillRate;
	}
	
	public final int getLethalStrikeRate()
	{
		return _lethalStrikeRate;
	}
	
	public final boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}
	
	public final String getFlyType()
	{
		return _flyType;
	}
	
	public boolean isFlyToBack()
	{
		return _flyToBack;
	}
	
	public final int getFlyRadius()
	{
		return _flyRadius;
	}
	
	public final float getFlyCourse()
	{
		return _flyCourse;
	}
	
	public final boolean isEffectTypeBattle()
	{
		switch (getSkillType())
		{
			case MDAM:
			case PDAM:
			case CHARGEDAM:
			case BLOW:
			case DEATHLINK:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean isStayAfterDeath()
	{
		return _stayAfterDeath;
	}
	
	public final boolean isStayOnSubclassChange()
	{
		return _stayOnSubclassChange;
	}
	
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
	{
		if (activeChar.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && !Config.GM_SKILL_RESTRICTION)
		{
			return true;
		}
		
		final List<Condition> preCondition = itemOrWeapon ? _itemPreCondition : _preCondition;
		if ((preCondition == null) || preCondition.isEmpty())
		{
			return true;
		}
		
		final Env env = new Env();
		env.setCharacter(activeChar);
		if (target instanceof L2Character)
		{
			env.setTarget((L2Character) target);
		}
		env.setSkill(this);
		
		for (Condition cond : preCondition)
		{
			if (!cond.test(env))
			{
				final String msg = cond.getMessage();
				final int msgId = cond.getMessageId();
				if (msgId != 0)
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(msgId);
					if (cond.isAddName())
					{
						sm.addSkillName(_id);
					}
					activeChar.sendPacket(sm);
				}
				else if (msg != null)
				{
					activeChar.sendMessage(msg);
				}
				return false;
			}
		}
		return true;
	}
	
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst)
	{
		L2Character target = null;
		
		L2Object objTarget = activeChar.getTarget();
		if (objTarget instanceof L2Character)
		{
			target = (L2Character) objTarget;
		}
		
		return getTargetList(activeChar, onlyFirst, target);
	}
	
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final ITargetTypeHandler handler = TargetHandler.getInstance().getHandler(getTargetType());
		if (handler != null)
		{
			try
			{
				return handler.getTargetList(this, activeChar, onlyFirst, target);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception in L2Skill.getTargetList(): " + e.getMessage(), e);
			}
		}
		activeChar.sendMessage("Target type of skill is not currently handled.");
		return EMPTY_TARGET_LIST;
	}
	
	public final L2Object[] getTargetList(L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}
	
	public final L2Object getFirstOfTargetList(L2Character activeChar)
	{
		L2Object[] targets = getTargetList(activeChar, true);
		if (targets.length == 0)
		{
			return null;
		}
		return targets[0];
	}
	
	public static final boolean checkForAreaOffensiveSkills(L2Character caster, L2Character target, L2Skill skill, boolean sourceInArena)
	{
		if ((target == null) || target.isDead() || (target == caster))
		{
			return false;
		}
		
		final L2PcInstance player = caster.getActingPlayer();
		final L2PcInstance targetPlayer = target.getActingPlayer();
		if (player != null)
		{
			if (targetPlayer != null)
			{
				if ((targetPlayer == caster) || (targetPlayer == player))
				{
					return false;
				}
				
				if (targetPlayer.inObserverMode())
				{
					return false;
				}
				
				if (skill.isOffensive() && (player.getSiegeState() > 0) && player.isInsideZone(ZoneId.SIEGE) && (player.getSiegeState() == targetPlayer.getSiegeState()) && (player.getSiegeSide() == targetPlayer.getSiegeSide()))
				{
					return false;
				}
				
				if (skill.isOffensive() && target.isInsideZone(ZoneId.PEACE))
				{
					return false;
				}
				
				if (player.isInParty() && targetPlayer.isInParty())
				{
					if (player.getParty().getLeaderObjectId() == targetPlayer.getParty().getLeaderObjectId())
					{
						return false;
					}
					
					if (player.getParty().isInCommandChannel() && (player.getParty().getCommandChannel() == targetPlayer.getParty().getCommandChannel()))
					{
						return false;
					}
				}
				
				if ((player.isFightingInEvent() || targetPlayer.isFightingInEvent()) && !player.isInSameEvent(targetPlayer))
				{
					if (((player.getEventName().equals("CTF") || targetPlayer.getEventName().equals("CTF")) && !Config.CTF_ALLOW_INTERFERENCE) || ((player.getEventName().equals("BW") || targetPlayer.getEventName().equals("BW")) && !Config.BW_ALLOW_INTERFERENCE))
					{
						return false;
					}
				}
				if (player.isInSameEvent(targetPlayer) && !player.isInSameTeam(targetPlayer))
				{
					return true;
				}
				if (player.isInSameTeam(targetPlayer))
				{
					if ((player.getEventName().equals("CTF") && !Config.CTF_ALLOW_TEAM_ATTACKING) || (player.getEventName().equals("BW") && !Config.BW_ALLOW_TEAM_ATTACKING))
					{
						return false;
					}
				}
				
				if (!TvTEvent.checkForTvTSkill(player, targetPlayer, skill))
				{
					return false;
				}
				
				if (!TvTRoundEvent.checkForTvTRoundSkill(player, targetPlayer, skill))
				{
					return false;
				}
				
				if (!sourceInArena && !(targetPlayer.isInsideZone(ZoneId.PVP) && !targetPlayer.isInsideZone(ZoneId.SIEGE)))
				{
					if ((player.getAllyId() != 0) && (player.getAllyId() == targetPlayer.getAllyId()))
					{
						return false;
					}
					
					if ((player.getClanId() != 0) && (player.getClanId() == targetPlayer.getClanId()))
					{
						return false;
					}
					
					if (!player.checkPvpSkill(targetPlayer, skill, (caster instanceof L2Summon)))
					{
						return false;
					}
				}
			}
		}
		else
		{
			if ((targetPlayer == null) && (target instanceof L2Attackable) && (caster instanceof L2Attackable))
			{
				String casterEnemyClan = ((L2Attackable) caster).getEnemyClan();
				if ((casterEnemyClan == null) || casterEnemyClan.isEmpty())
				{
					return false;
				}
				
				String targetClan = ((L2Attackable) target).getClan();
				if ((targetClan == null) || targetClan.isEmpty())
				{
					return false;
				}
				
				if (!casterEnemyClan.equals(targetClan))
				{
					return false;
				}
			}
		}
		
		if (geoEnabled && !GeoClient.getInstance().canSeeTarget(caster, target))
		{
			return false;
		}
		
		return true;
	}
	
	public static final boolean addSummon(L2Character caster, L2PcInstance owner, int radius, boolean isDead)
	{
		if (!owner.hasSummon())
		{
			return false;
		}
		return addCharacter(caster, owner.getSummon(), radius, isDead);
	}
	
	public static final boolean addCharacter(L2Character caster, L2Character target, int radius, boolean isDead)
	{
		if (isDead != target.isDead())
		{
			return false;
		}
		
		if ((radius > 0) && !Util.checkIfInRange(radius, caster, target, true))
		{
			return false;
		}
		
		return true;
		
	}
	
	public final Func[] getStatFuncs(L2Effect effect, L2Character player)
	{
		if (_funcTemplates == null)
		{
			return _emptyFunctionSet;
		}
		
		if (!(player instanceof L2Playable) && !(player instanceof L2Attackable))
		{
			return _emptyFunctionSet;
		}
		
		List<Func> funcs = new ArrayList<>(_funcTemplates.length);
		
		Env env = new Env();
		env.setCharacter(player);
		env.setSkill(this);
		
		Func f;
		
		for (FuncTemplate t : _funcTemplates)
		{
			
			f = t.getFunc(env, this);
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.isEmpty())
		{
			return _emptyFunctionSet;
		}
		
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public boolean hasEffects()
	{
		return ((_effectTemplates != null) && (_effectTemplates.length > 0));
	}
	
	public EffectTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}
	
	public EffectTemplate[] getEffectTemplatesPassive()
	{
		return _effectTemplatesPassive;
	}
	
	public boolean hasSelfEffects()
	{
		return ((_effectTemplatesSelf != null) && (_effectTemplatesSelf.length > 0));
	}
	
	public boolean hasPassiveEffects()
	{
		return ((_effectTemplatesPassive != null) && (_effectTemplatesPassive.length > 0));
	}
	
	public final L2Effect[] getEffects(L2Character effector, L2Character effected, Env env)
	{
		if (!hasEffects() || isPassive())
		{
			return _emptyEffectSet;
		}
		
		if ((effected instanceof L2DoorInstance) || (effected instanceof L2SiegeFlagInstance) || (effected instanceof L2CustomBWBaseInstance))
		{
			return _emptyEffectSet;
		}
		
		if (effector != effected)
		{
			if (isOffensive() || isDebuff())
			{
				if (effected.isInvul())
				{
					return _emptyEffectSet;
				}
				
				if ((effector instanceof L2PcInstance) && ((L2PcInstance) effector).isGM())
				{
					if (!((L2PcInstance) effector).getAccessLevel().canGiveDamage())
					{
						return _emptyEffectSet;
					}
				}
			}
		}
		
		List<L2Effect> effects = new ArrayList<>(_effectTemplates.length);
		
		if (env == null)
		{
			env = new Env();
		}
		
		env.setSkillMastery(Formulas.calcSkillMastery(effector, this));
		env.setCharacter(effector);
		env.setTarget(effected);
		env.setSkill(this);
		
		for (EffectTemplate et : _effectTemplates)
		{
			if (Formulas.calcEffectSuccess(effector, effected, et, this, env.getShield(), env.isSoulShot(), env.isSpiritShot(), env.isBlessedSpiritShot()))
			{
				L2Effect e = et.getEffect(env);
				if (e != null)
				{
					e.scheduleEffect();
					effects.add(e);
				}
			}
			else if (effector instanceof L2PcInstance)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(effected);
				sm.addSkillName(this);
				((L2PcInstance) effector).sendPacket(sm);
			}
		}
		
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final L2Effect[] getEffects(L2Character effector, L2Character effected)
	{
		return getEffects(effector, effected, null);
	}
	
	public final L2Effect[] getEffects(L2CubicInstance effector, L2Character effected, Env env)
	{
		if (!hasEffects() || isPassive())
		{
			return _emptyEffectSet;
		}
		
		if (effector.getOwner() != effected)
		{
			if (isDebuff() || isOffensive())
			{
				if (effected.isInvul())
				{
					return _emptyEffectSet;
				}
				
				if (effector.getOwner().isGM() && !effector.getOwner().getAccessLevel().canGiveDamage())
				{
					return _emptyEffectSet;
				}
			}
		}
		
		List<L2Effect> effects = new ArrayList<>(_effectTemplates.length);
		
		if (env == null)
		{
			env = new Env();
		}
		
		env.setCharacter(effector.getOwner());
		env.setCubic(effector);
		env.setTarget(effected);
		env.setSkill(this);
		
		for (EffectTemplate et : _effectTemplates)
		{
			if (Formulas.calcEffectSuccess(effector.getOwner(), effected, et, this, env.getShield(), env.isSoulShot(), env.isSpiritShot(), env.isBlessedSpiritShot()))
			{
				L2Effect e = et.getEffect(env);
				if (e != null)
				{
					e.scheduleEffect();
					effects.add(e);
				}
			}
		}
		
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final L2Effect[] getEffectsSelf(L2Character effector)
	{
		if (!hasSelfEffects() || isPassive())
		{
			return _emptyEffectSet;
		}
		
		List<L2Effect> effects = new ArrayList<>(_effectTemplatesSelf.length);
		
		for (EffectTemplate et : _effectTemplatesSelf)
		{
			Env env = new Env();
			env.setCharacter(effector);
			env.setTarget(effector);
			env.setSkill(this);
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				e.setSelfEffect();
				e.scheduleEffect();
				effects.add(e);
			}
		}
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final L2Effect[] getEffectsPassive(L2Character effector)
	{
		if (!hasPassiveEffects())
		{
			return _emptyEffectSet;
		}
		
		List<L2Effect> effects = new ArrayList<>(_effectTemplatesPassive.length);
		
		for (EffectTemplate et : _effectTemplatesPassive)
		{
			Env env = new Env();
			env.setCharacter(effector);
			env.setTarget(effector);
			env.setSkill(this);
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				e.setPassiveEffect();
				e.scheduleEffect();
				effects.add(e);
			}
		}
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}
	
	public final void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
		
	}
	
	public final void attachSelf(EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplatesSelf.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}
	
	public final void attachPassive(EffectTemplate effect)
	{
		if (_effectTemplatesPassive == null)
		{
			_effectTemplatesPassive = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplatesPassive.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesPassive, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesPassive = tmp;
		}
	}
	
	public final void attach(Condition c, boolean itemOrWeapon)
	{
		if (itemOrWeapon)
		{
			if (_itemPreCondition == null)
			{
				_itemPreCondition = new ArrayList<>();
			}
			_itemPreCondition.add(c);
		}
		else
		{
			if (_preCondition == null)
			{
				_preCondition = new ArrayList<>();
			}
			_preCondition.add(c);
		}
	}
	
	@Override
	public String toString()
	{
		return "Skill " + _name + "(" + _id + "," + _level + ")";
	}
	
	public int getFeed()
	{
		return _feed;
	}
	
	public int getReferenceItemId()
	{
		return _refId;
	}
	
	public int getAfterEffectId()
	{
		return _afterEffectId;
	}
	
	public int getAfterEffectLvl()
	{
		return _afterEffectLvl;
	}
	
	@Override
	public boolean triggersChanceSkill()
	{
		return (_triggeredId > 0) && isChance();
	}
	
	@Override
	public int getTriggeredChanceId()
	{
		return _triggeredId;
	}
	
	@Override
	public int getTriggeredChanceLevel()
	{
		return _triggeredLevel;
	}
	
	@Override
	public ChanceCondition getTriggeredChanceCondition()
	{
		return _chanceCondition;
	}
	
	public String getAttributeName()
	{
		return _attribute;
	}
	
	public int getBlowChance()
	{
		return _blowChance;
	}
	
	public boolean ignoreShield()
	{
		return _ignoreShield;
	}
	
	public boolean canBeReflected()
	{
		return _canBeReflected;
	}
	
	public boolean canBeDispeled()
	{
		return _canBeDispeled;
	}
	
	public boolean isClanSkill()
	{
		return _isClanSkill;
	}
	
	public boolean isExcludedFromCheck()
	{
		return _excludedFromCheck;
	}
	
	public boolean getDependOnTargetBuff()
	{
		return _dependOnTargetBuff;
	}
	
	public boolean isSimultaneousCast()
	{
		return _simultaneousCast;
	}
	
	private L2ExtractableSkill parseExtractableSkill(int skillId, int skillLvl, String values)
	{
		final String[] prodLists = values.split(";");
		final List<L2ExtractableProductItem> products = new ArrayList<>();
		String[] prodData;
		for (String prodList : prodLists)
		{
			prodData = prodList.split(",");
			if (prodData.length < 3)
			{
				_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> wrong seperator!");
			}
			List<ItemsHolder> items = null;
			double chance = 0;
			int prodId = 0;
			int quantity = 0;
			final int lenght = prodData.length - 1;
			try
			{
				items = new ArrayList<>(lenght / 2);
				for (int j = 0; j < lenght; j++)
				{
					prodId = Integer.parseInt(prodData[j]);
					quantity = Integer.parseInt(prodData[j += 1]);
					if ((prodId <= 0) || (quantity <= 0))
					{
						_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " wrong production Id: " + prodId + " or wrond quantity: " + quantity + "!");
					}
					items.add(new ItemsHolder(prodId, quantity));
				}
				chance = Double.parseDouble(prodData[lenght]);
			}
			catch (Exception e)
			{
				_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> incomplete/invalid production data or wrong seperator!");
			}
			products.add(new L2ExtractableProductItem(items, chance));
		}
		
		if (products.isEmpty())
		{
			_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> There are no production items!");
		}
		return new L2ExtractableSkill(SkillHolder.getSkillHashCode(skillId, skillLvl), products);
	}
	
	public L2ExtractableSkill getExtractableSkill()
	{
		return _extractableItems;
	}
	
	public final int getActivateRate()
	{
		return _activateRate;
	}
	
	public final int getLevelModifier()
	{
		return _levelModifier;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public boolean hasEffectType(L2EffectType... types)
	{
		if (hasEffects() && (types != null) && (types.length > 0))
		{
			if (_effectTypes == null)
			{
				_effectTypes = new byte[_effectTemplates.length];
				
				final Env env = new Env();
				env.setSkill(this);
				
				int i = 0;
				for (EffectTemplate et : _effectTemplates)
				{
					final L2Effect e = et.getEffect(env, true);
					if (e == null)
					{
						continue;
					}
					_effectTypes[i++] = (byte) e.getEffectType().ordinal();
				}
				Arrays.sort(_effectTypes);
			}
			
			for (L2EffectType type : types)
			{
				if (Arrays.binarySearch(_effectTypes, (byte) type.ordinal()) >= 0)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public String getIcon()
	{
		return _icon;
	}
}