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
package l2e.gameserver.model.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.conditions.Condition;
import l2e.gameserver.model.conditions.ConditionLogicOr;
import l2e.gameserver.model.conditions.ConditionPetType;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2ActionType;
import l2e.gameserver.model.items.type.L2EtcItemType;
import l2e.gameserver.model.items.type.L2ItemType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.StringUtil;

public abstract class L2Item implements IIdentifiable
{
	protected static final Logger _log = Logger.getLogger(L2Item.class.getName());
	
	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
	
	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;
	
	public static final int STRIDER = 0x1;
	public static final int GROWN_UP_WOLF_GROUP = 0x2;
	public static final int HATCHLING_GROUP = 0x4;
	public static final int ALL_WOLF_GROUP = 0x8;
	public static final int BABY_PET_GROUP = 0x16;
	public static final int UPGRADE_BABY_PET_GROUP = 0x32;
	public static final int ITEM_EQUIP_PET_GROUP = 0x64;
	
	public static final int SLOT_NONE = 0x0000;
	public static final int SLOT_UNDERWEAR = 0x0001;
	public static final int SLOT_R_EAR = 0x0002;
	public static final int SLOT_L_EAR = 0x0004;
	public static final int SLOT_LR_EAR = 0x00006;
	public static final int SLOT_NECK = 0x0008;
	public static final int SLOT_R_FINGER = 0x0010;
	public static final int SLOT_L_FINGER = 0x0020;
	public static final int SLOT_LR_FINGER = 0x0030;
	public static final int SLOT_HEAD = 0x0040;
	public static final int SLOT_R_HAND = 0x0080;
	public static final int SLOT_L_HAND = 0x0100;
	public static final int SLOT_GLOVES = 0x0200;
	public static final int SLOT_CHEST = 0x0400;
	public static final int SLOT_LEGS = 0x0800;
	public static final int SLOT_FEET = 0x1000;
	public static final int SLOT_BACK = 0x2000;
	public static final int SLOT_LR_HAND = 0x4000;
	public static final int SLOT_FULL_ARMOR = 0x8000;
	public static final int SLOT_HAIR = 0x010000;
	public static final int SLOT_ALLDRESS = 0x020000;
	public static final int SLOT_HAIR2 = 0x040000;
	public static final int SLOT_HAIRALL = 0x080000;
	public static final int SLOT_R_BRACELET = 0x100000;
	public static final int SLOT_L_BRACELET = 0x200000;
	public static final int SLOT_DECO = 0x400000;
	public static final int SLOT_BELT = 0x10000000;
	public static final int SLOT_WOLF = -100;
	public static final int SLOT_HATCHLING = -101;
	public static final int SLOT_STRIDER = -102;
	public static final int SLOT_BABYPET = -103;
	public static final int SLOT_GREATWOLF = -104;
	
	public static final int SLOT_MULTI_ALLWEAPON = SLOT_LR_HAND | SLOT_R_HAND;
	
	public static final int MATERIAL_STEEL = 0x00;
	public static final int MATERIAL_FINE_STEEL = 0x01;
	public static final int MATERIAL_BLOOD_STEEL = 0x02;
	public static final int MATERIAL_BRONZE = 0x03;
	public static final int MATERIAL_SILVER = 0x04;
	public static final int MATERIAL_GOLD = 0x05;
	public static final int MATERIAL_MITHRIL = 0x06;
	public static final int MATERIAL_ORIHARUKON = 0x07;
	public static final int MATERIAL_PAPER = 0x08;
	public static final int MATERIAL_WOOD = 0x09;
	public static final int MATERIAL_CLOTH = 0x0a;
	public static final int MATERIAL_LEATHER = 0x0b;
	public static final int MATERIAL_BONE = 0x0c;
	public static final int MATERIAL_HORN = 0x0d;
	public static final int MATERIAL_DAMASCUS = 0x0e;
	public static final int MATERIAL_ADAMANTAITE = 0x0f;
	public static final int MATERIAL_CHRYSOLITE = 0x10;
	public static final int MATERIAL_CRYSTAL = 0x11;
	public static final int MATERIAL_LIQUID = 0x12;
	public static final int MATERIAL_SCALE_OF_DRAGON = 0x13;
	public static final int MATERIAL_DYESTUFF = 0x14;
	public static final int MATERIAL_COBWEB = 0x15;
	public static final int MATERIAL_SEED = 0x16;
	public static final int MATERIAL_FISH = 0x17;
	public static final int MATERIAL_RUNE_XP = 0x18;
	public static final int MATERIAL_RUNE_SP = 0x19;
	public static final int MATERIAL_RUNE_PENALTY = 0x20;
	
	public static final int CRYSTAL_NONE = 0x00;
	public static final int CRYSTAL_D = 0x01;
	public static final int CRYSTAL_C = 0x02;
	public static final int CRYSTAL_B = 0x03;
	public static final int CRYSTAL_A = 0x04;
	public static final int CRYSTAL_S = 0x05;
	public static final int CRYSTAL_S80 = 0x06;
	public static final int CRYSTAL_S84 = 0x07;
	
	private static final int[] CRYSTAL_ITEM_ID =
	{
		0,
		1458,
		1459,
		1460,
		1461,
		1462,
		1462,
		1462
	};
	
	private static final int[] CRYSTAL_ENCHANT_BONUS_ARMOR =
	{
		0,
		11,
		6,
		11,
		19,
		25,
		25,
		25
	};
	
	private static final int[] CRYSTAL_ENCHANT_BONUS_WEAPON =
	{
		0,
		90,
		45,
		67,
		144,
		250,
		250,
		250
	};
	
	private final int _itemId;
	private final int _displayId;
	private final String _name;
	private final String _icon;
	private final int _weight;
	private final boolean _stackable;
	private final int _materialType;
	private final int _crystalType;
	private final int _equipReuseDelay;
	private final int _duration;
	private final int _time;
	private final int _autoDestroyTime;
	private final int _bodyPart;
	private final int _referencePrice;
	private final int _crystalCount;
	private final boolean _sellable;
	private final boolean _dropable;
	private final boolean _destroyable;
	private final boolean _tradeable;
	private final boolean _depositable;
	private final int _enchantable;
	private final boolean _elementable;
	private final boolean _questItem;
	private final boolean _freightable;
	private final boolean _is_oly_restricted;
	private final boolean _for_npc;
	private final boolean _common;
	private final boolean _heroItem;
	private final boolean _pvpItem;
	private final boolean _ex_immediate_effect;
	private final int _defaultEnchantLevel;
	private final L2ActionType _defaultAction;
	
	protected int _type1;
	protected int _type2;
	protected Elementals[] _elementals = null;
	protected FuncTemplate[] _funcTemplates;
	protected EffectTemplate[] _effectTemplates;
	protected List<Condition> _preConditions;
	private SkillsHolder[] _SkillsHolder;
	private SkillsHolder _unequipSkill = null;
	
	protected static final Func[] _emptyFunctionSet = new Func[0];
	protected static final L2Effect[] _emptyEffectSet = new L2Effect[0];
	
	private List<Quest> _questEvents;
	
	private final int _useSkillDisTime;
	private final int _reuseDelay;
	private final int _sharedReuseGroup;
	
	protected L2Item(StatsSet set)
	{
		_itemId = set.getInteger("item_id");
		_displayId = set.getInteger("displayId", _itemId);
		_name = set.getString("name");
		_icon = set.getString("icon", null);
		_weight = set.getInteger("weight", 0);
		_materialType = ItemHolder._materials.get(set.getString("material", "steel"));
		
		_duration = set.getInteger("duration", -1);
		_time = set.getInteger("time", -1);
		_autoDestroyTime = set.getInteger("auto_destroy_time", -1) * 1000;
		_bodyPart = ItemHolder._slots.get(set.getString("bodypart", "none"));
		_referencePrice = set.getInteger("price", 0);
		_crystalType = ItemHolder._crystalTypes.get(set.getString("crystal_type", "none"));
		_crystalCount = set.getInteger("crystal_count", 0);
		_equipReuseDelay = set.getInteger("equip_reuse_delay", 0) * 1000;
		_stackable = set.getBool("is_stackable", false);
		_sellable = set.getBool("is_sellable", true);
		_dropable = set.getBool("is_dropable", true);
		_destroyable = set.getBool("is_destroyable", true);
		_tradeable = set.getBool("is_tradable", true);
		_depositable = set.getBool("is_depositable", true);
		_elementable = set.getBool("element_enabled", false);
		_enchantable = set.getInteger("enchant_enabled", 0);
		_questItem = set.getBool("is_questitem", false);
		_freightable = set.getBool("is_freightable", false);
		_is_oly_restricted = set.getBool("is_oly_restricted", false);
		_for_npc = set.getBool("for_npc", false);
		
		_ex_immediate_effect = set.getBool("ex_immediate_effect", false);
		
		_defaultAction = set.getEnum("default_action", L2ActionType.class, L2ActionType.none);
		_useSkillDisTime = set.getInteger("useSkillDisTime", 0);
		_defaultEnchantLevel = set.getInteger("enchanted", 0);
		_reuseDelay = set.getInteger("reuse_delay", 0);
		_sharedReuseGroup = set.getInteger("shared_reuse_group", 0);
		
		String equip_condition = set.getString("equip_condition", null);
		if (equip_condition != null)
		{
			ConditionLogicOr cond = new ConditionLogicOr();
			if (equip_condition.contains("strider"))
			{
				cond.add(new ConditionPetType(STRIDER));
			}
			if (equip_condition.contains("grown_up_wolf_group"))
			{
				cond.add(new ConditionPetType(GROWN_UP_WOLF_GROUP));
			}
			if (equip_condition.contains("hatchling_group"))
			{
				cond.add(new ConditionPetType(HATCHLING_GROUP));
			}
			if (equip_condition.contains("all_wolf_group"))
			{
				cond.add(new ConditionPetType(ALL_WOLF_GROUP));
			}
			if (equip_condition.contains("baby_pet_group"))
			{
				cond.add(new ConditionPetType(BABY_PET_GROUP));
			}
			if (equip_condition.contains("upgrade_baby_pet_group"))
			{
				cond.add(new ConditionPetType(UPGRADE_BABY_PET_GROUP));
			}
			if (equip_condition.contains("item_equip_pet_group"))
			{
				cond.add(new ConditionPetType(ITEM_EQUIP_PET_GROUP));
			}
			
			if (cond.conditions.length > 0)
			{
				attach(cond);
			}
		}
		
		String skills = set.getString("item_skill", null);
		if (skills != null)
		{
			String[] skillsSplit = skills.split(";");
			_SkillsHolder = new SkillsHolder[skillsSplit.length];
			int used = 0;
			
			for (String element : skillsSplit)
			{
				try
				{
					String[] skillSplit = element.split("-");
					int id = Integer.parseInt(skillSplit[0]);
					int level = Integer.parseInt(skillSplit[1]);
					
					if (id == 0)
					{
						_log.info(StringUtil.concat("Ignoring item_skill(", element, ") for item ", toString(), ". Skill id is 0!"));
						continue;
					}
					
					if (level == 0)
					{
						_log.info(StringUtil.concat("Ignoring item_skill(", element, ") for item ", toString(), ". Skill level is 0!"));
						continue;
					}
					
					_SkillsHolder[used] = new SkillsHolder(id, level);
					++used;
				}
				catch (Exception e)
				{
					_log.warning(StringUtil.concat("Failed to parse item_skill(", element, ") for item ", toString(), "! Format: SkillId0-SkillLevel0[;SkillIdN-SkillLevelN]"));
				}
			}
			
			if (used != _SkillsHolder.length)
			{
				SkillsHolder[] SkillsHolder = new SkillsHolder[used];
				System.arraycopy(_SkillsHolder, 0, SkillsHolder, 0, used);
				_SkillsHolder = SkillsHolder;
			}
		}
		
		skills = set.getString("unequip_skill", null);
		if (skills != null)
		{
			String[] info = skills.split("-");
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch (Exception nfe)
				{
					_log.info(StringUtil.concat("Couldnt parse ", skills, " in weapon unequip skills! item ", toString()));
				}
				if ((id > 0) && (level > 0))
				{
					_unequipSkill = new SkillsHolder(id, level);
				}
			}
		}
		_common = ((_itemId >= 11605) && (_itemId <= 12361));
		_heroItem = ((_itemId >= 6611) && (_itemId <= 6621)) || ((_itemId >= 9388) && (_itemId <= 9390)) || (_itemId == 6842);
		_pvpItem = ((_itemId >= 10667) && (_itemId <= 10835)) || ((_itemId >= 12852) && (_itemId <= 12977)) || ((_itemId >= 14363) && (_itemId <= 14525)) || (_itemId == 14528) || (_itemId == 14529) || (_itemId == 14558) || ((_itemId >= 15913) && (_itemId <= 16024)) || ((_itemId >= 16134) && (_itemId <= 16147)) || (_itemId == 16149) || (_itemId == 16151) || (_itemId == 16153) || (_itemId == 16155) || (_itemId == 16157) || (_itemId == 16159) || ((_itemId >= 16168) && (_itemId <= 16176)) || ((_itemId >= 16179) && (_itemId <= 16220));
	}
	
	public abstract L2ItemType getItemType();
	
	public boolean isMagicWeapon()
	{
		return false;
	}
	
	public int getEquipReuseDelay()
	{
		return _equipReuseDelay;
	}
	
	public final int getDuration()
	{
		return _duration;
	}
	
	public final int getTime()
	{
		return _time;
	}
	
	public final int getAutoDestroyTime()
	{
		return _autoDestroyTime;
	}
	
	@Override
	public final int getId()
	{
		return _itemId;
	}
	
	public final int getDisplayId()
	{
		return _displayId;
	}
	
	public abstract int getItemMask();
	
	public final int getMaterialType()
	{
		return _materialType;
	}
	
	public final int getType2()
	{
		return _type2;
	}
	
	public final int getWeight()
	{
		return _weight;
	}
	
	public final boolean isCrystallizable()
	{
		return (_crystalType != L2Item.CRYSTAL_NONE) && (_crystalCount > 0);
	}
	
	public final int getCrystalType()
	{
		return _crystalType;
	}
	
	public final int getCrystalItemId()
	{
		return CRYSTAL_ITEM_ID[_crystalType];
	}
	
	public final int getItemGrade()
	{
		return getCrystalType();
	}
	
	public final int getItemGradeSPlus()
	{
		switch (getItemGrade())
		{
			case CRYSTAL_S80:
			case CRYSTAL_S84:
				return CRYSTAL_S;
			default:
				return getItemGrade();
		}
	}
	
	public final int getCrystalCount()
	{
		return _crystalCount;
	}
	
	public final int getCrystalCount(int enchantLevel)
	{
		if (enchantLevel > 3)
		{
			switch (_type2)
			{
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
					return _crystalCount + (CRYSTAL_ENCHANT_BONUS_ARMOR[getCrystalType()] * ((3 * enchantLevel) - 6));
				case TYPE2_WEAPON:
					return _crystalCount + (CRYSTAL_ENCHANT_BONUS_WEAPON[getCrystalType()] * ((2 * enchantLevel) - 3));
				default:
					return _crystalCount;
			}
		}
		else if (enchantLevel > 0)
		{
			switch (_type2)
			{
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
					return _crystalCount + (CRYSTAL_ENCHANT_BONUS_ARMOR[getCrystalType()] * enchantLevel);
				case TYPE2_WEAPON:
					return _crystalCount + (CRYSTAL_ENCHANT_BONUS_WEAPON[getCrystalType()] * enchantLevel);
				default:
					return _crystalCount;
			}
		}
		else
		{
			return _crystalCount;
		}
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final Elementals[] getElementals()
	{
		return _elementals;
	}
	
	public Elementals getElemental(byte attribute)
	{
		for (Elementals elm : _elementals)
		{
			if (elm.getElement() == attribute)
			{
				return elm;
			}
		}
		return null;
	}
	
	public void setElementals(Elementals element)
	{
		if (_elementals == null)
		{
			_elementals = new Elementals[1];
			_elementals[0] = element;
		}
		else
		{
			Elementals elm = getElemental(element.getElement());
			if (elm != null)
			{
				elm.setValue(element.getValue());
			}
			else
			{
				elm = element;
				Elementals[] array = new Elementals[_elementals.length + 1];
				System.arraycopy(_elementals, 0, array, 0, _elementals.length);
				array[_elementals.length] = elm;
				_elementals = array;
			}
		}
	}
	
	public final int getBodyPart()
	{
		return _bodyPart;
	}
	
	public final int getType1()
	{
		return _type1;
	}
	
	public final boolean isStackable()
	{
		return _stackable;
	}
	
	public boolean isConsumable()
	{
		return false;
	}
	
	public boolean isEquipable()
	{
		return (getBodyPart() != 0) && !(getItemType() instanceof L2EtcItemType);
	}
	
	public final int getReferencePrice()
	{
		return (isConsumable() ? (int) (_referencePrice * Config.RATE_CONSUMABLE_COST) : _referencePrice);
	}
	
	public final boolean isSellable()
	{
		return _sellable;
	}
	
	public final boolean isDropable()
	{
		return _dropable;
	}
	
	public final boolean isDestroyable()
	{
		return _destroyable;
	}
	
	public final boolean isTradeable()
	{
		return _tradeable;
	}
	
	public final boolean isDepositable()
	{
		return _depositable;
	}
	
	public final int isEnchantable()
	{
		return Arrays.binarySearch(Config.ENCHANT_BLACKLIST, getId()) < 0 ? _enchantable : 0;
	}
	
	public final boolean isElementable()
	{
		return _elementable;
	}
	
	public final boolean isCommon()
	{
		return _common;
	}
	
	public final boolean isHeroItem()
	{
		return _heroItem;
	}
	
	public final boolean isPvpItem()
	{
		return _pvpItem;
	}
	
	public boolean isPotion()
	{
		return (getItemType() == L2EtcItemType.POTION);
	}
	
	public boolean isElixir()
	{
		return (getItemType() == L2EtcItemType.ELIXIR);
	}
	
	public final Func[] getStatFuncs(L2ItemInstance item, L2Character player)
	{
		if ((_funcTemplates == null) || (_funcTemplates.length == 0))
		{
			return _emptyFunctionSet;
		}
		
		ArrayList<Func> funcs = new ArrayList<>(_funcTemplates.length);
		
		Env env = new Env();
		env.setCharacter(player);
		env.setTarget(player);
		env.setItem(item);
		
		Func f;
		for (FuncTemplate t : _funcTemplates)
		{
			f = t.getFunc(env, item);
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
	
	public L2Effect[] getEffects(L2ItemInstance item, L2Character player)
	{
		if ((_effectTemplates == null) || (_effectTemplates.length == 0))
		{
			return _emptyEffectSet;
		}
		
		FastList<L2Effect> effects = FastList.newInstance();
		
		Env env = new Env();
		env.setCharacter(player);
		env.setTarget(player);
		env.setItem(item);
		
		L2Effect e;
		for (EffectTemplate et : _effectTemplates)
		{
			
			e = et.getEffect(env);
			if (e != null)
			{
				e.scheduleEffect();
				effects.add(e);
			}
		}
		
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		
		L2Effect[] result = effects.toArray(new L2Effect[effects.size()]);
		FastList.recycle(effects);
		return result;
	}
	
	public void attach(FuncTemplate f)
	{
		switch (f.stat)
		{
			case FIRE_RES:
			case FIRE_POWER:
				setElementals(new Elementals(Elementals.FIRE, (int) f.lambda.calc(null)));
				break;
			case WATER_RES:
			case WATER_POWER:
				setElementals(new Elementals(Elementals.WATER, (int) f.lambda.calc(null)));
				break;
			case WIND_RES:
			case WIND_POWER:
				setElementals(new Elementals(Elementals.WIND, (int) f.lambda.calc(null)));
				break;
			case EARTH_RES:
			case EARTH_POWER:
				setElementals(new Elementals(Elementals.EARTH, (int) f.lambda.calc(null)));
				break;
			case HOLY_RES:
			case HOLY_POWER:
				setElementals(new Elementals(Elementals.HOLY, (int) f.lambda.calc(null)));
				break;
			case DARK_RES:
			case DARK_POWER:
				setElementals(new Elementals(Elementals.DARK, (int) f.lambda.calc(null)));
				break;
		}
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
	
	public void attach(EffectTemplate effect)
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
	
	public final void attach(Condition c)
	{
		if (_preConditions == null)
		{
			_preConditions = new ArrayList<>(1);
		}
		if (!_preConditions.contains(c))
		{
			_preConditions.add(c);
		}
	}
	
	public boolean hasSkills()
	{
		return _SkillsHolder != null;
	}
	
	public final SkillsHolder[] getSkills()
	{
		return _SkillsHolder;
	}
	
	public final L2Skill getUnequipSkill()
	{
		return _unequipSkill == null ? null : _unequipSkill.getSkill();
	}
	
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean sendMessage)
	{
		if (activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !Config.GM_ITEM_RESTRICTION)
		{
			return true;
		}
		
		if ((isOlyRestrictedItem() || isHeroItem()) && ((activeChar.isPlayer()) && activeChar.getActingPlayer().isInOlympiadMode()))
		{
			if (isEquipable())
			{
				activeChar.sendPacket(SystemMessageId.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			}
			return false;
		}
		
		if (!isConditionAttached())
		{
			return true;
		}
		
		Env env = new Env();
		env.setCharacter(activeChar);
		if (target instanceof L2Character)
		{
			env.setTarget((L2Character) target);
		}
		
		for (Condition preCondition : _preConditions)
		{
			if (preCondition == null)
			{
				continue;
			}
			
			if (!preCondition.test(env))
			{
				if (activeChar instanceof L2Summon)
				{
					activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
					return false;
				}
				
				if (sendMessage)
				{
					String msg = preCondition.getMessage();
					int msgId = preCondition.getMessageId();
					if (msg != null)
					{
						activeChar.sendMessage(msg);
					}
					else if (msgId != 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(msgId);
						if (preCondition.isAddName())
						{
							sm.addItemName(_itemId);
						}
						activeChar.sendPacket(sm);
					}
				}
				return false;
			}
		}
		return true;
	}
	
	public boolean isConditionAttached()
	{
		return (_preConditions != null) && !_preConditions.isEmpty();
	}
	
	public boolean isQuestItem()
	{
		return _questItem;
	}
	
	public boolean isFreightable()
	{
		return _freightable;
	}
	
	public boolean isOlyRestrictedItem()
	{
		return _is_oly_restricted || Config.LIST_OLY_RESTRICTED_ITEMS.contains(_itemId);
	}
	
	public boolean isForNpc()
	{
		return _for_npc;
	}
	
	@Override
	public String toString()
	{
		return _name + "(" + _itemId + ")";
	}
	
	public boolean is_ex_immediate_effect()
	{
		return _ex_immediate_effect;
	}
	
	public L2ActionType getDefaultAction()
	{
		return _defaultAction;
	}
	
	public int useSkillDisTime()
	{
		return _useSkillDisTime;
	}
	
	public int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public int getSharedReuseGroup()
	{
		return _sharedReuseGroup;
	}
	
	public String getIcon()
	{
		return _icon;
	}
	
	public void addQuestEvent(Quest q)
	{
		if (_questEvents == null)
		{
			_questEvents = new ArrayList<>();
		}
		_questEvents.add(q);
	}
	
	public List<Quest> getQuestEvents()
	{
		return _questEvents;
	}
	
	public int getDefaultEnchantLevel()
	{
		return _defaultEnchantLevel;
	}
	
	public boolean isPetItem()
	{
		return getItemType() == L2EtcItemType.PET_COLLAR;
	}
	
	public L2Skill getEnchant4Skill()
	{
		return null;
	}
}