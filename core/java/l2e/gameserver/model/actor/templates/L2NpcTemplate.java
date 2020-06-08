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
package l2e.gameserver.model.actor.templates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.gameserver.data.xml.HerbDropParser;
import l2e.gameserver.model.L2DropCategory;
import l2e.gameserver.model.L2DropData;
import l2e.gameserver.model.L2MinionData;
import l2e.gameserver.model.L2NpcAIData;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.Quest.QuestEventType;
import l2e.gameserver.model.skills.L2Skill;

public final class L2NpcTemplate extends L2CharTemplate implements IIdentifiable
{
	private static final Logger _log = Logger.getLogger(L2NpcTemplate.class.getName());
	
	public int _npcId;
	private int _idTemplate;
	private String _type;
	public String _name;
	public boolean _serverSideName;
	private String _title;
	public boolean _serverSideTitle;
	private String _sex;
	public byte _level;
	private int _rewardExp;
	private int _rewardSp;
	private int _rHand;
	private int _lHand;
	private int _enchantEffect;
	
	private Race _race;
	public String _clientClass;
	
	private int _dropHerbGroup;
	private boolean _isCustom;
	
	private boolean _isQuestMonster;
	private float _baseVitalityDivider;
	
	private final List<L2Skill> _buffSkills = new ArrayList<>();
	private final List<L2Skill> _negativeSkills = new ArrayList<>();
	private final List<L2Skill> _debuffSkills = new ArrayList<>();
	private final List<L2Skill> _atkSkills = new ArrayList<>();
	private final List<L2Skill> _rootSkills = new ArrayList<>();
	private final List<L2Skill> _stunskills = new ArrayList<>();
	private final List<L2Skill> _sleepSkills = new ArrayList<>();
	private final List<L2Skill> _paralyzeSkills = new ArrayList<>();
	private final List<L2Skill> _fossilSkills = new ArrayList<>();
	private final List<L2Skill> _floatSkills = new ArrayList<>();
	private final List<L2Skill> _immobilizeSkills = new ArrayList<>();
	private final List<L2Skill> _healSkills = new ArrayList<>();
	private final List<L2Skill> _resSkills = new ArrayList<>();
	private final List<L2Skill> _dotSkills = new ArrayList<>();
	private final List<L2Skill> _cotSkills = new ArrayList<>();
	private final List<L2Skill> _universalSkills = new ArrayList<>();
	private final List<L2Skill> _manaSkills = new ArrayList<>();
	private final List<L2Skill> _longRangeSkills = new ArrayList<>();
	private final List<L2Skill> _shortRangeSkills = new ArrayList<>();
	private final List<L2Skill> _generalSkills = new ArrayList<>();
	private final List<L2Skill> _suicideSkills = new ArrayList<>();
	
	private L2NpcAIData _AIdataStatic = new L2NpcAIData();
	
	private final List<L2DropCategory> _categories = new ArrayList<>();
	
	private final List<L2MinionData> _minions = new ArrayList<>();
	
	private final List<ClassId> _teachInfo = new ArrayList<>();
	
	private final Map<Integer, L2Skill> _skills = new FastMap<Integer, L2Skill>().shared();
	
	private final Map<QuestEventType, List<Quest>> _questEvents = new FastMap<QuestEventType, List<Quest>>().shared();
	
	private StatsSet _parameters;
	
	public static enum AIType
	{
		FIGHTER,
		ARCHER,
		BALANCED,
		MAGE,
		HEALER,
		CORPSE
	}
	
	public static enum Race
	{
		UNDEAD,
		MAGICCREATURE,
		BEAST,
		ANIMAL,
		PLANT,
		HUMANOID,
		SPIRIT,
		ANGEL,
		DEMON,
		DRAGON,
		GIANT,
		BUG,
		FAIRIE,
		HUMAN,
		ELVE,
		DARKELVE,
		ORC,
		DWARVE,
		OTHER,
		NONLIVING,
		SIEGEWEAPON,
		DEFENDINGARMY,
		MERCENARIE,
		UNKNOWN,
		KAMAEL,
		NONE
	}
	
	public static boolean isAssignableTo(Class<?> sub, Class<?> clazz)
	{
		if (clazz.isInterface())
		{
			Class<?>[] interfaces = sub.getInterfaces();
			for (Class<?> interface1 : interfaces)
			{
				if (clazz.getName().equals(interface1.getName()))
				{
					return true;
				}
			}
		}
		else
		{
			do
			{
				if (sub.getName().equals(clazz.getName()))
				{
					return true;
				}
				
				sub = sub.getSuperclass();
			}
			while (sub != null);
		}
		return false;
	}
	
	public static boolean isAssignableTo(Object obj, Class<?> clazz)
	{
		return L2NpcTemplate.isAssignableTo(obj.getClass(), clazz);
	}
	
	public L2NpcTemplate(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void set(StatsSet set)
	{
		super.set(set);
		_npcId = set.getInteger("npcId");
		_idTemplate = set.getInteger("idTemplate");
		_type = set.getString("type");
		_name = set.getString("name");
		_serverSideName = set.getBool("serverSideName");
		_title = set.getString("title");
		_isQuestMonster = getTitle().equalsIgnoreCase("Quest Monster");
		_serverSideTitle = set.getBool("serverSideTitle");
		_sex = set.getString("sex");
		_level = set.getByte("level");
		_rewardExp = set.getInteger("rewardExp");
		_rewardSp = set.getInteger("rewardSp");
		_rHand = set.getInteger("rhand");
		_lHand = set.getInteger("lhand");
		_enchantEffect = set.getInteger("enchant");
		final int herbGroup = set.getInteger("dropHerbGroup");
		if ((herbGroup > 0) && (HerbDropParser.getInstance().getHerbDroplist(herbGroup) == null))
		{
			_log.warning("Missing Herb Drop Group for npcId: " + getId());
			_dropHerbGroup = 0;
		}
		else
		{
			_dropHerbGroup = herbGroup;
		}
		
		_clientClass = set.getString("client_class");
		
		_baseVitalityDivider = (getLevel() > 0) && (getRewardExp() > 0) ? (getBaseHpMax() * 9 * getLevel() * getLevel()) / (100 * getRewardExp()) : 0;
		
		_isCustom = _npcId != _idTemplate;
	}
	
	public void addAtkSkill(L2Skill skill)
	{
		_atkSkills.add(skill);
	}
	
	public void addBuffSkill(L2Skill skill)
	{
		_buffSkills.add(skill);
	}
	
	public void addCOTSkill(L2Skill skill)
	{
		_cotSkills.add(skill);
	}
	
	public void addDebuffSkill(L2Skill skill)
	{
		_debuffSkills.add(skill);
	}
	
	public void addDOTSkill(L2Skill skill)
	{
		_dotSkills.add(skill);
	}
	
	public void addDropData(L2DropData drop, int categoryType)
	{
		if (!drop.isQuestDrop())
		{
			synchronized (_categories)
			{
				boolean catExists = false;
				for (L2DropCategory cat : _categories)
				{
					if (cat.getCategoryType() == categoryType)
					{
						cat.addDropData(drop, isType("L2RaidBoss") || isType("L2GrandBoss"));
						catExists = true;
						break;
					}
				}
				
				if (!catExists)
				{
					final L2DropCategory cat = new L2DropCategory(categoryType);
					cat.addDropData(drop, isType("L2RaidBoss") || isType("L2GrandBoss"));
					_categories.add(cat);
				}
			}
		}
	}
	
	public void addFloatSkill(L2Skill skill)
	{
		_floatSkills.add(skill);
	}
	
	public void addFossilSkill(L2Skill skill)
	{
		_fossilSkills.add(skill);
	}
	
	public void addGeneralSkill(L2Skill skill)
	{
		getGeneralskills().add(skill);
	}
	
	public void addHealSkill(L2Skill skill)
	{
		_healSkills.add(skill);
	}
	
	public void addImmobiliseSkill(L2Skill skill)
	{
		_immobilizeSkills.add(skill);
	}
	
	public void addManaHealSkill(L2Skill skill)
	{
		_manaSkills.add(skill);
	}
	
	public void addNegativeSkill(L2Skill skill)
	{
		_negativeSkills.add(skill);
	}
	
	public void addParalyzeSkill(L2Skill skill)
	{
		_paralyzeSkills.add(skill);
	}
	
	public void addQuestEvent(QuestEventType EventType, Quest q)
	{
		if (!_questEvents.containsKey(EventType))
		{
			List<Quest> quests = new ArrayList<>();
			quests.add(q);
			_questEvents.put(EventType, quests);
		}
		else
		{
			List<Quest> quests = _questEvents.get(EventType);
			
			if (!EventType.isMultipleRegistrationAllowed() && !quests.isEmpty())
			{
				_log.warning("Quest event not allowed in multiple quests.  Skipped addition of Event Type \"" + EventType + "\" for NPC \"" + _name + "\" and quest \"" + q.getName() + "\".");
			}
			else
			{
				quests.add(q);
			}
		}
	}
	
	public void removeQuest(Quest q)
	{
		for (Entry<QuestEventType, List<Quest>> entry : _questEvents.entrySet())
		{
			if (entry.getValue().contains(q))
			{
				Iterator<Quest> it = entry.getValue().iterator();
				while (it.hasNext())
				{
					Quest q1 = it.next();
					if (q1 == q)
					{
						it.remove();
					}
				}
				
				if (entry.getValue().isEmpty())
				{
					_questEvents.remove(entry.getKey());
				}
			}
		}
	}
	
	public void addRaidData(L2MinionData minion)
	{
		_minions.add(minion);
	}
	
	public void addRangeSkill(L2Skill skill)
	{
		if ((skill.getCastRange() <= 150) && (skill.getCastRange() > 0))
		{
			_shortRangeSkills.add(skill);
		}
		else if (skill.getCastRange() > 150)
		{
			_longRangeSkills.add(skill);
		}
	}
	
	public void addResSkill(L2Skill skill)
	{
		_resSkills.add(skill);
	}
	
	public void addRootSkill(L2Skill skill)
	{
		_rootSkills.add(skill);
	}
	
	public void addSkill(L2Skill skill)
	{
		if (!skill.isPassive())
		{
			if (skill.isSuicideAttack())
			{
				addSuicideSkill(skill);
			}
			else
			{
				addGeneralSkill(skill);
				switch (skill.getSkillType())
				{
					case BUFF:
						addBuffSkill(skill);
						break;
					case RESURRECT:
						addResSkill(skill);
						break;
					case DEBUFF:
						addDebuffSkill(skill);
						addCOTSkill(skill);
						addRangeSkill(skill);
						break;
					case ROOT:
						addRootSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case SLEEP:
						addSleepSkill(skill);
						addImmobiliseSkill(skill);
						break;
					case STUN:
						addRootSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case PARALYZE:
						addParalyzeSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case PDAM:
					case MDAM:
					case BLOW:
					case DRAIN:
					case CHARGEDAM:
					case FATAL:
					case DEATHLINK:
					case MANADAM:
					case CPDAMPERCENT:
						addAtkSkill(skill);
						addUniversalSkill(skill);
						addRangeSkill(skill);
						break;
					case POISON:
					case DOT:
					case MDOT:
					case BLEED:
						addDOTSkill(skill);
						addRangeSkill(skill);
						break;
					case MUTE:
					case FEAR:
						addCOTSkill(skill);
						addRangeSkill(skill);
						break;
					default:
						if (skill.hasEffectType(L2EffectType.CANCEL, L2EffectType.CANCEL_ALL, L2EffectType.NEGATE))
						{
							addNegativeSkill(skill);
							addRangeSkill(skill);
						}
						else if (skill.hasEffectType(L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
						{
							addHealSkill(skill);
						}
						else
						{
							addUniversalSkill(skill);
						}
						break;
				}
			}
		}
		_skills.put(skill.getId(), skill);
	}
	
	public void addSleepSkill(L2Skill skill)
	{
		_sleepSkills.add(skill);
	}
	
	public void addStunSkill(L2Skill skill)
	{
		_stunskills.add(skill);
	}
	
	public void addSuicideSkill(L2Skill skill)
	{
		_suicideSkills.add(skill);
	}
	
	public void addTeachInfo(List<ClassId> teachInfo)
	{
		_teachInfo.addAll(teachInfo);
	}
	
	public void addUniversalSkill(L2Skill skill)
	{
		_universalSkills.add(skill);
	}
	
	public boolean canTeach(ClassId classId)
	{
		if (classId.level() == 3)
		{
			return _teachInfo.contains(classId.getParent());
		}
		return _teachInfo.contains(classId);
	}
	
	public synchronized void clearAllDropData()
	{
		for (L2DropCategory cat : _categories)
		{
			cat.clearAllDrops();
		}
		_categories.clear();
	}
	
	public L2NpcAIData getAIDataStatic()
	{
		return _AIdataStatic;
	}
	
	public List<L2DropData> getAllDropData()
	{
		final List<L2DropData> list = new ArrayList<>();
		for (L2DropCategory tmp : _categories)
		{
			list.addAll(tmp.getAllDrops());
		}
		return list;
	}
	
	public List<L2Skill> getAtkSkills()
	{
		return _atkSkills;
	}
	
	public float getBaseVitalityDivider()
	{
		return _baseVitalityDivider;
	}
	
	public List<L2Skill> getBuffSkills()
	{
		return _buffSkills;
	}
	
	public String getClientClass()
	{
		return _clientClass;
	}
	
	public List<L2Skill> getCostOverTimeSkills()
	{
		return _cotSkills;
	}
	
	public List<L2Skill> getDebuffSkills()
	{
		return _debuffSkills;
	}
	
	public List<L2DropCategory> getDropData()
	{
		return _categories;
	}
	
	public int getDropHerbGroup()
	{
		return _dropHerbGroup;
	}
	
	public int getEnchantEffect()
	{
		return _enchantEffect;
	}
	
	public Map<QuestEventType, List<Quest>> getEventQuests()
	{
		return _questEvents;
	}
	
	public List<Quest> getEventQuests(QuestEventType EventType)
	{
		return _questEvents.get(EventType);
	}
	
	public List<L2Skill> getGeneralskills()
	{
		return _generalSkills;
	}
	
	public List<L2Skill> getHealSkills()
	{
		return _healSkills;
	}
	
	public int getIdTemplate()
	{
		return _idTemplate;
	}
	
	public List<L2Skill> getImmobiliseSkills()
	{
		return _immobilizeSkills;
	}
	
	public int getLeftHand()
	{
		return _lHand;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public List<L2Skill> getLongRangeSkills()
	{
		return _longRangeSkills;
	}
	
	public List<L2MinionData> getMinionData()
	{
		return _minions;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public List<L2Skill> getNegativeSkills()
	{
		return _negativeSkills;
	}
	
	@Override
	public int getId()
	{
		return _npcId;
	}
	
	public Race getRace()
	{
		if (_race == null)
		{
			_race = Race.NONE;
		}
		return _race;
	}
	
	public List<L2Skill> getResSkills()
	{
		return _resSkills;
	}
	
	public int getRewardExp()
	{
		return _rewardExp;
	}
	
	public int getRewardSp()
	{
		return _rewardSp;
	}
	
	public int getRightHand()
	{
		return _rHand;
	}
	
	public String getSex()
	{
		return _sex;
	}
	
	public List<L2Skill> getShortRangeSkills()
	{
		return _shortRangeSkills;
	}
	
	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	public List<L2Skill> getSuicideSkills()
	{
		return _suicideSkills;
	}
	
	public List<ClassId> getTeachInfo()
	{
		return _teachInfo;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public String getType()
	{
		return _type;
	}
	
	public List<L2Skill> getUniversalSkills()
	{
		return _universalSkills;
	}
	
	public boolean isCustom()
	{
		return _isCustom;
	}
	
	public boolean isQuestMonster()
	{
		return _isQuestMonster;
	}
	
	public boolean isServerSideName()
	{
		return _serverSideName;
	}
	
	public boolean isServerSideTitle()
	{
		return _serverSideTitle;
	}
	
	public boolean isType(String t)
	{
		return _type.equalsIgnoreCase(t);
	}
	
	public boolean isUndead()
	{
		return _race == Race.UNDEAD;
	}
	
	public void setAIData(L2NpcAIData AIData)
	{
		_AIdataStatic = AIData;
	}
	
	public void setRace(int raceId)
	{
		switch (raceId)
		{
			case 1:
				_race = Race.UNDEAD;
				break;
			case 2:
				_race = Race.MAGICCREATURE;
				break;
			case 3:
				_race = Race.BEAST;
				break;
			case 4:
				_race = Race.ANIMAL;
				break;
			case 5:
				_race = Race.PLANT;
				break;
			case 6:
				_race = Race.HUMANOID;
				break;
			case 7:
				_race = Race.SPIRIT;
				break;
			case 8:
				_race = Race.ANGEL;
				break;
			case 9:
				_race = Race.DEMON;
				break;
			case 10:
				_race = Race.DRAGON;
				break;
			case 11:
				_race = Race.GIANT;
				break;
			case 12:
				_race = Race.BUG;
				break;
			case 13:
				_race = Race.FAIRIE;
				break;
			case 14:
				_race = Race.HUMAN;
				break;
			case 15:
				_race = Race.ELVE;
				break;
			case 16:
				_race = Race.DARKELVE;
				break;
			case 17:
				_race = Race.ORC;
				break;
			case 18:
				_race = Race.DWARVE;
				break;
			case 19:
				_race = Race.OTHER;
				break;
			case 20:
				_race = Race.NONLIVING;
				break;
			case 21:
				_race = Race.SIEGEWEAPON;
				break;
			case 22:
				_race = Race.DEFENDINGARMY;
				break;
			case 23:
				_race = Race.MERCENARIE;
				break;
			case 24:
				_race = Race.UNKNOWN;
				break;
			case 25:
				_race = Race.KAMAEL;
				break;
			default:
				_race = Race.NONE;
				break;
		}
	}
	
	public boolean hasParameters()
	{
		return _parameters != null;
	}
	
	public StatsSet getParameters()
	{
		return _parameters;
	}
	
	public void setParameters(StatsSet set)
	{
		_parameters = set;
	}
}