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
package l2e.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.L2SkillLearn.SubClassData;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.base.SocialClass;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.holders.PlayerSkillHolder;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.interfaces.ISkillsHolder;
import l2e.gameserver.model.skills.L2Skill;
import gnu.trove.map.hash.TIntObjectHashMap;

public final class SkillTreesParser extends DocumentParser
{
	private static final Map<ClassId, Map<Integer, L2SkillLearn>> _classSkillTrees = new HashMap<>();
	private static final Map<ClassId, Map<Integer, L2SkillLearn>> _transferSkillTrees = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _collectSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _fishingSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _pledgeSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _subClassSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _subPledgeSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _transformSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _commonSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _nobleSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _heroSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _gameMasterSkillTree = new HashMap<>();
	private static final Map<Integer, L2SkillLearn> _gameMasterAuraSkillTree = new HashMap<>();
	
	private TIntObjectHashMap<int[]> _skillsByClassIdHashCodes;
	private TIntObjectHashMap<int[]> _skillsByRaceHashCodes;
	TIntObjectHashMap<ArrayList<Integer>> _restrictedSkills = new TIntObjectHashMap<>();
	
	private int[] _allSkillsHashCodes;
	
	private boolean _loading = true;
	
	private static final Map<ClassId, ClassId> _parentClassMap = new HashMap<>();
	
	protected SkillTreesParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_loading = true;
		_classSkillTrees.clear();
		_collectSkillTree.clear();
		_fishingSkillTree.clear();
		_pledgeSkillTree.clear();
		_subClassSkillTree.clear();
		_subPledgeSkillTree.clear();
		_transferSkillTrees.clear();
		_transformSkillTree.clear();
		_nobleSkillTree.clear();
		_heroSkillTree.clear();
		_gameMasterSkillTree.clear();
		_gameMasterAuraSkillTree.clear();
		
		parseDirectory(new File(Config.DATAPACK_ROOT, "data/skillTrees/"));
		
		generateCheckArrays();
		
		_loading = false;
		
		report();
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node attr;
		String type = null;
		int cId = -1;
		int parentClassId = -1;
		ClassId classId = null;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("skillTree".equalsIgnoreCase(d.getNodeName()))
					{
						final Map<Integer, L2SkillLearn> classSkillTree = new HashMap<>();
						final Map<Integer, L2SkillLearn> trasferSkillTree = new HashMap<>();
						
						type = d.getAttributes().getNamedItem("type").getNodeValue();
						attr = d.getAttributes().getNamedItem("classId");
						if (attr != null)
						{
							cId = Integer.parseInt(attr.getNodeValue());
							classId = ClassId.values()[cId];
						}
						else
						{
							cId = -1;
						}
						
						attr = d.getAttributes().getNamedItem("parentClassId");
						if (attr != null)
						{
							parentClassId = Integer.parseInt(attr.getNodeValue());
							if ((cId > -1) && (cId != parentClassId) && (parentClassId > -1) && !_parentClassMap.containsKey(classId))
							{
								_parentClassMap.put(classId, ClassId.values()[parentClassId]);
							}
						}
						
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("skill".equalsIgnoreCase(c.getNodeName()))
							{
								final StatsSet learnSkillSet = new StatsSet();
								attrs = c.getAttributes();
								for (int i = 0; i < attrs.getLength(); i++)
								{
									attr = attrs.item(i);
									learnSkillSet.set(attr.getNodeName(), attr.getNodeValue());
								}
								
								final L2SkillLearn skillLearn = new L2SkillLearn(learnSkillSet);
								for (Node b = c.getFirstChild(); b != null; b = b.getNextSibling())
								{
									attrs = b.getAttributes();
									switch (b.getNodeName())
									{
										case "item":
											skillLearn.addRequiredItem(new ItemsHolder(parseInt(attrs, "id"), parseInt(attrs, "count")));
											break;
										case "preRequisiteSkill":
											skillLearn.addPreReqSkill(new SkillsHolder(parseInt(attrs, "id"), parseInt(attrs, "lvl")));
											break;
										case "race":
											skillLearn.addRace(Race.valueOf(b.getTextContent()));
											break;
										case "residenceId":
											skillLearn.addResidenceId(Integer.valueOf(b.getTextContent()));
											break;
										case "socialClass":
											skillLearn.setSocialClass(Enum.valueOf(SocialClass.class, b.getTextContent()));
											break;
										case "subClassConditions":
											skillLearn.addSubclassConditions(parseInt(attrs, "slot"), parseInt(attrs, "lvl"));
											break;
									}
								}
								
								final int skillHashCode = SkillHolder.getSkillHashCode(skillLearn.getSkillId(), skillLearn.getSkillLevel());
								switch (type)
								{
									case "classSkillTree":
									{
										if (cId != -1)
										{
											classSkillTree.put(skillHashCode, skillLearn);
										}
										else
										{
											_commonSkillTree.put(skillHashCode, skillLearn);
										}
										break;
									}
									case "transferSkillTree":
									{
										trasferSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "collectSkillTree":
									{
										_collectSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "fishingSkillTree":
									{
										_fishingSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "pledgeSkillTree":
									{
										_pledgeSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "subClassSkillTree":
									{
										_subClassSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "subPledgeSkillTree":
									{
										_subPledgeSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "transformSkillTree":
									{
										_transformSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "nobleSkillTree":
									{
										_nobleSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "heroSkillTree":
									{
										_heroSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "gameMasterSkillTree":
									{
										_gameMasterSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									case "gameMasterAuraSkillTree":
									{
										_gameMasterAuraSkillTree.put(skillHashCode, skillLearn);
										break;
									}
									default:
									{
										_log.warning(getClass().getSimpleName() + ": Unknown Skill Tree type: " + type + "!");
									}
								}
							}
						}
						
						if (type.equals("transferSkillTree"))
						{
							_transferSkillTrees.put(classId, trasferSkillTree);
						}
						else if (type.equals("classSkillTree") && (cId > -1))
						{
							if (!_classSkillTrees.containsKey(classId))
							{
								_classSkillTrees.put(classId, classSkillTree);
							}
							else
							{
								_classSkillTrees.get(classId).putAll(classSkillTree);
							}
						}
					}
				}
			}
		}
	}
	
	public Map<Integer, L2SkillLearn> getCompleteClassSkillTree(ClassId classId)
	{
		final Map<Integer, L2SkillLearn> skillTree = new HashMap<>();
		skillTree.putAll(_commonSkillTree);
		while ((classId != null) && (_classSkillTrees.get(classId) != null))
		{
			skillTree.putAll(_classSkillTrees.get(classId));
			classId = _parentClassMap.get(classId);
		}
		return skillTree;
	}
	
	public Map<Integer, L2SkillLearn> getTransferSkillTree(ClassId classId)
	{
		if (classId.level() >= 3)
		{
			return getTransferSkillTree(classId.getParent());
		}
		return _transferSkillTrees.get(classId);
	}
	
	public Map<Integer, L2SkillLearn> getCommonSkillTree()
	{
		return _commonSkillTree;
	}
	
	public Map<Integer, L2SkillLearn> getCollectSkillTree()
	{
		return _collectSkillTree;
	}
	
	public Map<Integer, L2SkillLearn> getFishingSkillTree()
	{
		return _fishingSkillTree;
	}
	
	public Map<Integer, L2SkillLearn> getPledgeSkillTree()
	{
		return _pledgeSkillTree;
	}
	
	public Map<Integer, L2SkillLearn> getSubClassSkillTree()
	{
		return _subClassSkillTree;
	}
	
	public Map<Integer, L2SkillLearn> getSubPledgeSkillTree()
	{
		return _subPledgeSkillTree;
	}
	
	public Map<Integer, L2SkillLearn> getTransformSkillTree()
	{
		return _transformSkillTree;
	}
	
	public Map<Integer, L2Skill> getNobleSkillTree()
	{
		final Map<Integer, L2Skill> tree = new HashMap<>();
		final SkillHolder st = SkillHolder.getInstance();
		for (Entry<Integer, L2SkillLearn> e : _nobleSkillTree.entrySet())
		{
			tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
		}
		return tree;
	}
	
	public Map<Integer, L2Skill> getHeroSkillTree()
	{
		final Map<Integer, L2Skill> tree = new HashMap<>();
		final SkillHolder st = SkillHolder.getInstance();
		for (Entry<Integer, L2SkillLearn> e : _heroSkillTree.entrySet())
		{
			tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
		}
		return tree;
	}
	
	public Map<Integer, L2Skill> getGMSkillTree()
	{
		final Map<Integer, L2Skill> tree = new HashMap<>();
		final SkillHolder st = SkillHolder.getInstance();
		for (Entry<Integer, L2SkillLearn> e : _gameMasterSkillTree.entrySet())
		{
			tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
		}
		return tree;
	}
	
	public Map<Integer, L2Skill> getGMAuraSkillTree()
	{
		final Map<Integer, L2Skill> tree = new HashMap<>();
		final SkillHolder st = SkillHolder.getInstance();
		for (Entry<Integer, L2SkillLearn> e : _gameMasterAuraSkillTree.entrySet())
		{
			tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
		}
		return tree;
	}
	
	public List<L2SkillLearn> getAvailableSkills(L2PcInstance player, ClassId classId, boolean includeByFs, boolean includeAutoGet)
	{
		return getAvailableSkills(player, classId, includeByFs, includeAutoGet, player);
	}
	
	private List<L2SkillLearn> getAvailableSkills(L2PcInstance player, ClassId classId, boolean includeByFs, boolean includeAutoGet, ISkillsHolder holder)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		final Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(classId);
		if (skills.isEmpty())
		{
			_log.warning(getClass().getSimpleName() + ": Skilltree for class " + classId + " is not defined!");
			return result;
		}
		
		for (L2SkillLearn skill : skills.values())
		{
			if (((includeAutoGet && skill.isAutoGet()) || skill.isLearnedByNpc() || (includeByFs && skill.isLearnedByFS())) && (player.getLevel() >= skill.getGetLevel()))
			{
				final L2Skill oldSkill = holder.getKnownSkill(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	public Collection<L2Skill> getAllAvailableSkills(L2PcInstance player, ClassId classId, boolean includeByFs, boolean includeAutoGet)
	{
		int unLearnable = 0;
		PlayerSkillHolder holder = new PlayerSkillHolder(player);
		List<L2SkillLearn> learnable = getAvailableSkills(player, classId, includeByFs, includeAutoGet, holder);
		while (learnable.size() > unLearnable)
		{
			for (L2SkillLearn s : learnable)
			{
				L2Skill sk = SkillHolder.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());
				if ((sk == null) || ((sk.getId() == L2Skill.SKILL_DIVINE_INSPIRATION) && !Config.AUTO_LEARN_DIVINE_INSPIRATION && !player.isGM()))
				{
					unLearnable++;
					continue;
				}
				
				holder.addSkill(sk);
			}
			learnable = getAvailableSkills(player, classId, includeByFs, includeAutoGet, holder);
		}
		return holder.getSkills().values();
	}
	
	public List<L2SkillLearn> getAvailableAutoGetSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		final Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(player.getClassId());
		if (skills.isEmpty())
		{
			_log.warning(getClass().getSimpleName() + ": Skill Tree for this class Id(" + player.getClassId() + ") is not defined!");
			return result;
		}
		
		final Race race = player.getRace();
		for (L2SkillLearn skill : skills.values())
		{
			if (!skill.getRaces().isEmpty() && !skill.getRaces().contains(race))
			{
				continue;
			}
			
			if (skill.isAutoGet() && (player.getLevel() >= skill.getGetLevel()))
			{
				final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() < skill.getSkillLevel())
					{
						result.add(skill);
					}
				}
				else
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	public List<L2SkillLearn> getAvailableFishingSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		final Race playerRace = player.getRace();
		for (L2SkillLearn skill : _fishingSkillTree.values())
		{
			if (!skill.getRaces().isEmpty() && !skill.getRaces().contains(playerRace))
			{
				continue;
			}
			
			if (skill.isLearnedByNpc() && (player.getLevel() >= skill.getGetLevel()))
			{
				final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	public List<L2SkillLearn> getAvailableCollectSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _collectSkillTree.values())
		{
			final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
			if (oldSkill != null)
			{
				if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
				{
					result.add(skill);
				}
			}
			else if (skill.getSkillLevel() == 1)
			{
				result.add(skill);
			}
		}
		return result;
	}
	
	public List<L2SkillLearn> getAvailableTransferSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		ClassId classId = player.getClassId();
		
		if (classId.level() == 3)
		{
			classId = classId.getParent();
		}
		
		if (!_transferSkillTrees.containsKey(classId))
		{
			return result;
		}
		
		for (L2SkillLearn skill : _transferSkillTrees.get(classId).values())
		{
			if (player.getKnownSkill(skill.getSkillId()) == null)
			{
				result.add(skill);
			}
		}
		return result;
	}
	
	public List<L2SkillLearn> getAvailableTransformSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		final Race race = player.getRace();
		for (L2SkillLearn skill : _transformSkillTree.values())
		{
			if ((player.getLevel() >= skill.getGetLevel()) && (skill.getRaces().isEmpty() || skill.getRaces().contains(race)))
			{
				final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	public List<L2SkillLearn> getAvailablePledgeSkills(L2Clan clan)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _pledgeSkillTree.values())
		{
			if (!skill.isResidencialSkill() && (clan.getLevel() >= skill.getGetLevel()))
			{
				final L2Skill oldSkill = clan.getSkills().get(skill.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
					{
						result.add(skill);
					}
				}
				else if (skill.getSkillLevel() == 1)
				{
					result.add(skill);
				}
			}
		}
		return result;
	}
	
	public List<L2SkillLearn> getAvailableSubPledgeSkills(L2Clan clan)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _subPledgeSkillTree.values())
		{
			if ((clan.getLevel() >= skill.getGetLevel()) && clan.isLearnableSubSkill(skill.getSkillId(), skill.getSkillLevel()))
			{
				result.add(skill);
			}
		}
		return result;
	}
	
	public List<L2SkillLearn> getAvailableSubClassSkills(L2PcInstance player)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _subClassSkillTree.values())
		{
			if (player.getLevel() >= skill.getGetLevel())
			{
				List<SubClassData> subClassConds = null;
				for (SubClass subClass : player.getSubClasses().values())
				{
					subClassConds = skill.getSubClassConditions();
					if (!subClassConds.isEmpty() && (subClass.getClassIndex() <= subClassConds.size()) && (subClass.getClassIndex() == subClassConds.get(subClass.getClassIndex() - 1).getSlot()) && (subClassConds.get(subClass.getClassIndex() - 1).getLvl() <= subClass.getLevel()))
					{
						final L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
						if (oldSkill != null)
						{
							if (oldSkill.getLevel() == (skill.getSkillLevel() - 1))
							{
								result.add(skill);
							}
						}
						else if (skill.getSkillLevel() == 1)
						{
							result.add(skill);
						}
					}
				}
			}
		}
		return result;
	}
	
	public List<L2SkillLearn> getAvailableResidentialSkills(int residenceId)
	{
		final List<L2SkillLearn> result = new ArrayList<>();
		for (L2SkillLearn skill : _pledgeSkillTree.values())
		{
			if (skill.isResidencialSkill() && skill.getResidenceIds().contains(residenceId))
			{
				result.add(skill);
			}
		}
		return result;
	}
	
	public L2SkillLearn getSkillLearn(AcquireSkillType skillType, int id, int lvl, L2PcInstance player)
	{
		L2SkillLearn sl = null;
		switch (skillType)
		{
			case CLASS:
				sl = getClassSkill(id, lvl, player.getLearningClass());
				break;
			case TRANSFORM:
				sl = getTransformSkill(id, lvl);
				break;
			case FISHING:
				sl = getFishingSkill(id, lvl);
				break;
			case PLEDGE:
				sl = getPledgeSkill(id, lvl);
				break;
			case SUBPLEDGE:
				sl = getSubPledgeSkill(id, lvl);
				break;
			case TRANSFER:
				sl = getTransferSkill(id, lvl, player.getClassId());
				break;
			case SUBCLASS:
				sl = getSubClassSkill(id, lvl);
				break;
			case COLLECT:
				sl = getCollectSkill(id, lvl);
				break;
		}
		return sl;
	}
	
	public L2SkillLearn getTransformSkill(int id, int lvl)
	{
		return _transformSkillTree.get(SkillHolder.getSkillHashCode(id, lvl));
	}
	
	public L2SkillLearn getClassSkill(int id, int lvl, ClassId classId)
	{
		return getCompleteClassSkillTree(classId).get(SkillHolder.getSkillHashCode(id, lvl));
	}
	
	public L2SkillLearn getFishingSkill(int id, int lvl)
	{
		return _fishingSkillTree.get(SkillHolder.getSkillHashCode(id, lvl));
	}
	
	public L2SkillLearn getPledgeSkill(int id, int lvl)
	{
		return _pledgeSkillTree.get(SkillHolder.getSkillHashCode(id, lvl));
	}
	
	public L2SkillLearn getSubPledgeSkill(int id, int lvl)
	{
		return _subPledgeSkillTree.get(SkillHolder.getSkillHashCode(id, lvl));
	}
	
	public L2SkillLearn getTransferSkill(int id, int lvl, ClassId classId)
	{
		if (classId.getParent() != null)
		{
			final ClassId parentId = classId.getParent();
			if (_transferSkillTrees.get(parentId) != null)
			{
				return _transferSkillTrees.get(parentId).get(SkillHolder.getSkillHashCode(id, lvl));
			}
		}
		return null;
	}
	
	public L2SkillLearn getSubClassSkill(int id, int lvl)
	{
		return _subClassSkillTree.get(SkillHolder.getSkillHashCode(id, lvl));
	}
	
	public L2SkillLearn getCommonSkill(int id, int lvl)
	{
		return _commonSkillTree.get(SkillHolder.getSkillHashCode(id, lvl));
	}
	
	public L2SkillLearn getCollectSkill(int id, int lvl)
	{
		return _collectSkillTree.get(SkillHolder.getSkillHashCode(id, lvl));
	}
	
	public int getMinLevelForNewSkill(L2PcInstance player, Map<Integer, L2SkillLearn> skillTree)
	{
		int minLevel = 0;
		if (skillTree.isEmpty())
		{
			_log.warning(getClass().getSimpleName() + ": SkillTree is not defined for getMinLevelForNewSkill!");
		}
		else
		{
			for (L2SkillLearn s : skillTree.values())
			{
				if (s.isLearnedByNpc() && (player.getLevel() < s.getGetLevel()))
				{
					if ((minLevel == 0) || (minLevel > s.getGetLevel()))
					{
						minLevel = s.getGetLevel();
					}
				}
			}
		}
		return minLevel;
	}
	
	public boolean isHeroSkill(int skillId, int skillLevel)
	{
		if (_heroSkillTree.containsKey(SkillHolder.getSkillHashCode(skillId, skillLevel)))
		{
			return true;
		}
		
		for (L2SkillLearn skill : _heroSkillTree.values())
		{
			if ((skill.getSkillId() == skillId) && (skillLevel == -1))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isGMSkill(int skillId, int skillLevel)
	{
		final Map<Integer, L2SkillLearn> gmSkills = new HashMap<>();
		gmSkills.putAll(_gameMasterSkillTree);
		gmSkills.putAll(_gameMasterAuraSkillTree);
		if (gmSkills.containsKey(SkillHolder.getSkillHashCode(skillId, skillLevel)))
		{
			return true;
		}
		
		for (L2SkillLearn skill : gmSkills.values())
		{
			if ((skill.getSkillId() == skillId) && (skillLevel == -1))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isClanSkill(int skillId, int skillLevel)
	{
		final int hashCode = SkillHolder.getSkillHashCode(skillId, skillId);
		return _pledgeSkillTree.containsKey(hashCode) || _subPledgeSkillTree.containsKey(hashCode);
	}
	
	public void addSkills(L2PcInstance gmchar, boolean auraSkills)
	{
		final Collection<L2SkillLearn> skills = auraSkills ? _gameMasterAuraSkillTree.values() : _gameMasterSkillTree.values();
		final SkillHolder st = SkillHolder.getInstance();
		for (L2SkillLearn sl : skills)
		{
			gmchar.addSkill(st.getInfo(sl.getSkillId(), sl.getSkillLevel()), false);
		}
	}
	
	private void generateCheckArrays()
	{
		int i;
		int[] array;
		
		Map<Integer, L2SkillLearn> tempMap;
		final Set<ClassId> keySet = _classSkillTrees.keySet();
		_skillsByClassIdHashCodes = new TIntObjectHashMap<>(keySet.size());
		for (ClassId cls : keySet)
		{
			i = 0;
			tempMap = getCompleteClassSkillTree(cls);
			array = new int[tempMap.size()];
			for (int h : tempMap.keySet())
			{
				array[i++] = h;
			}
			tempMap.clear();
			Arrays.sort(array);
			_skillsByClassIdHashCodes.put(cls.ordinal(), array);
		}
		
		final List<Integer> list = new ArrayList<>();
		_skillsByRaceHashCodes = new TIntObjectHashMap<>(Race.values().length);
		for (Race r : Race.values())
		{
			for (L2SkillLearn s : _fishingSkillTree.values())
			{
				if (s.getRaces().contains(r))
				{
					list.add(SkillHolder.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
				}
			}
			
			for (L2SkillLearn s : _transformSkillTree.values())
			{
				if (s.getRaces().contains(r))
				{
					list.add(SkillHolder.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
				}
			}
			
			i = 0;
			array = new int[list.size()];
			for (int s : list)
			{
				array[i++] = s;
			}
			Arrays.sort(array);
			_skillsByRaceHashCodes.put(r.ordinal(), array);
			list.clear();
		}
		
		for (L2SkillLearn s : _commonSkillTree.values())
		{
			if (s.getRaces().isEmpty())
			{
				list.add(SkillHolder.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
			}
		}
		
		for (L2SkillLearn s : _fishingSkillTree.values())
		{
			if (s.getRaces().isEmpty())
			{
				list.add(SkillHolder.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
			}
		}
		
		for (L2SkillLearn s : _transformSkillTree.values())
		{
			if (s.getRaces().isEmpty())
			{
				list.add(SkillHolder.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
			}
		}
		
		for (L2SkillLearn s : _collectSkillTree.values())
		{
			list.add(SkillHolder.getSkillHashCode(s.getSkillId(), s.getSkillLevel()));
		}
		
		_allSkillsHashCodes = new int[list.size()];
		int j = 0;
		for (int hashcode : list)
		{
			_allSkillsHashCodes[j++] = hashcode;
		}
		Arrays.sort(_allSkillsHashCodes);
	}
	
	public boolean isSkillAllowed(L2PcInstance player, L2Skill skill)
	{
		if (skill.isExcludedFromCheck())
		{
			return true;
		}
		
		if (player.isGM() && skill.isGMSkill())
		{
			return true;
		}
		
		if (_loading)
		{
			return true;
		}
		
		final int maxLvl = SkillHolder.getInstance().getMaxLevel(skill.getId());
		final int hashCode = SkillHolder.getSkillHashCode(skill.getId(), Math.min(skill.getLevel(), maxLvl));
		
		if (Arrays.binarySearch(_skillsByClassIdHashCodes.get(player.getClassId().ordinal()), hashCode) >= 0)
		{
			return true;
		}
		
		if (Arrays.binarySearch(_skillsByRaceHashCodes.get(player.getRace().ordinal()), hashCode) >= 0)
		{
			return true;
		}
		
		if (Arrays.binarySearch(_allSkillsHashCodes, hashCode) >= 0)
		{
			return true;
		}
		
		if (getTransferSkill(skill.getId(), Math.min(skill.getLevel(), maxLvl), player.getClassId()) != null)
		{
			return true;
		}
		return false;
	}
	
	private void report()
	{
		int classSkillTreeCount = 0;
		for (Map<Integer, L2SkillLearn> classSkillTree : _classSkillTrees.values())
		{
			classSkillTreeCount += classSkillTree.size();
		}
		
		int trasferSkillTreeCount = 0;
		for (Map<Integer, L2SkillLearn> trasferSkillTree : _transferSkillTrees.values())
		{
			trasferSkillTreeCount += trasferSkillTree.size();
		}
		
		int dwarvenOnlyFishingSkillCount = 0;
		for (L2SkillLearn fishSkill : _fishingSkillTree.values())
		{
			if (fishSkill.getRaces().contains(Race.Dwarf))
			{
				dwarvenOnlyFishingSkillCount++;
			}
		}
		
		int resSkillCount = 0;
		for (L2SkillLearn pledgeSkill : _pledgeSkillTree.values())
		{
			if (pledgeSkill.isResidencialSkill())
			{
				resSkillCount++;
			}
		}
		
		final String className = getClass().getSimpleName();
		_log.info(className + ": Loaded " + classSkillTreeCount + " Class Skills for " + _classSkillTrees.size() + " Class Skill Trees.");
		_log.info(className + ": Loaded " + _subClassSkillTree.size() + " Sub-Class Skills.");
		_log.info(className + ": Loaded " + trasferSkillTreeCount + " Transfer Skills for " + _transferSkillTrees.size() + " Transfer Skill Trees.");
		_log.info(className + ": Loaded " + _fishingSkillTree.size() + " Fishing Skills, " + dwarvenOnlyFishingSkillCount + " Dwarven only Fishing Skills.");
		_log.info(className + ": Loaded " + _collectSkillTree.size() + " Collect Skills.");
		_log.info(className + ": Loaded " + _pledgeSkillTree.size() + " Pledge Skills, " + (_pledgeSkillTree.size() - resSkillCount) + " for Pledge and " + resSkillCount + " Residential.");
		_log.info(className + ": Loaded " + _subPledgeSkillTree.size() + " Sub-Pledge Skills.");
		_log.info(className + ": Loaded " + _transformSkillTree.size() + " Transform Skills.");
		_log.info(className + ": Loaded " + _nobleSkillTree.size() + " Noble Skills.");
		_log.info(className + ": Loaded " + _heroSkillTree.size() + " Hero Skills.");
		_log.info(className + ": Loaded " + _gameMasterSkillTree.size() + " Game Master Skills.");
		_log.info(className + ": Loaded " + _gameMasterAuraSkillTree.size() + " Game Master Aura Skills.");
		final int commonSkills = _commonSkillTree.size();
		if (commonSkills > 0)
		{
			_log.info(className + ": Loaded " + commonSkills + " Common Skills to all classes.");
		}
		loadRestrictedSkills();
	}
	
	private void loadRestrictedSkills()
	{
		TIntObjectHashMap<ArrayList<Integer>> allowedSkillIds = new TIntObjectHashMap<>();
		
		for (ClassId classid : ClassId.values())
		{
			if (classid.getRace() == null)
			{
				continue;
			}
			
			final Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(classid);
			ArrayList<Integer> skillIds = new ArrayList<>();
			
			for (L2SkillLearn sk : skills.values())
			{
				if (!skillIds.contains(sk.getSkillId()))
				{
					skillIds.add(sk.getSkillId());
				}
			}
			allowedSkillIds.put(classid.getId(), skillIds);
		}
		for (ClassId classId : ClassId.values())
		{
			if (classId.getRace() == null)
			{
				continue;
			}
			ArrayList<Integer> skillIds = new ArrayList<>();
			for (ClassId classid : ClassId.values())
			{
				if ((classid == classId) || (classid.getRace() == null) || classId.childOf(classid))
				{
					continue;
				}
				for (Integer skillId : allowedSkillIds.get(classid.getId()))
				{
					if (!skillIds.contains(skillId))
					{
						skillIds.add(skillId);
					}
				}
			}
			for (Integer skillId : allowedSkillIds.get(classId.getId()))
			{
				if (skillIds.contains(skillId))
				{
					skillIds.remove(skillId);
				}
			}
			_restrictedSkills.put(classId.getId(), skillIds);
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + _restrictedSkills.size() + " classes with restricted skill(s).");
	}
	
	public ArrayList<Integer> getRestrictedSkills(ClassId classId)
	{
		return _restrictedSkills.get(classId.getId());
	}
	
	public static SkillTreesParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillTreesParser _instance = new SkillTreesParser();
	}
}