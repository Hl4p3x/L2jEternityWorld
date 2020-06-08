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

import java.util.HashMap;
import java.util.Map;

import javolution.util.FastList;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.L2Skill;
import l2e.util.Rnd;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class PhantomSkillsParser extends DocumentParser
{
	private final Map<ClassId, FastList<L2Skill>> _magicAttackSkills = new HashMap<>();
	private final Map<ClassId, FastList<L2Skill>> _figherAttackSkills = new HashMap<>();
	private final Map<ClassId, FastList<L2Skill>> _buffSkills = new HashMap<>();
	private final Map<ClassId, FastList<L2Skill>> _debuffSkills = new HashMap<>();
	private final Map<ClassId, FastList<L2Skill>> _activePvpSkills = new HashMap<>();
	private final Map<ClassId, FastList<L2Skill>> _healSkills = new HashMap<>();
	
	protected PhantomSkillsParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_magicAttackSkills.clear();
		_figherAttackSkills.clear();
		_buffSkills.clear();
		_debuffSkills.clear();
		_activePvpSkills.clear();
		_healSkills.clear();
		parseDatapackFile("data/stats/chars/phantoms/phantomSkills.xml");
		_log.info("[PhantomListener]: Loaded " + _magicAttackSkills.size() + " phantom classes with skills.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("phantomSkills".equalsIgnoreCase(d.getNodeName()))
					{
						parseEquipment(d);
					}
				}
			}
		}
	}
	
	private void parseEquipment(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		int id, level;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final FastList<L2Skill> mAttackSkillList = new FastList<>();
		final FastList<L2Skill> fAttackSkillList = new FastList<>();
		final FastList<L2Skill> buffSkillList = new FastList<>();
		final FastList<L2Skill> debuffSkillList = new FastList<>();
		final FastList<L2Skill> pvpSkillList = new FastList<>();
		final FastList<L2Skill> healSkillList = new FastList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("mAttackSkill".equalsIgnoreCase(c.getNodeName()))
			{
				attrs = c.getAttributes();
				
				id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
				
				L2Skill skill = SkillHolder.getInstance().getInfo(id, level);
				if (skill == null)
				{
					_log.warning(getClass().getSimpleName() + ": Can't find phantom attack skill id: " + id + " level: " + level + " in phantomSkills.xml");
					continue;
				}
				mAttackSkillList.add(skill);
			}
			else if ("fAttackSkill".equalsIgnoreCase(c.getNodeName()))
			{
				attrs = c.getAttributes();
				
				id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
				
				L2Skill skill = SkillHolder.getInstance().getInfo(id, level);
				if (skill == null)
				{
					_log.warning(getClass().getSimpleName() + ": Can't find phantom buff skill id: " + id + " level: " + level + " in phantomSkills.xml");
					continue;
				}
				fAttackSkillList.add(skill);
			}
			else if ("buffSkill".equalsIgnoreCase(c.getNodeName()))
			{
				attrs = c.getAttributes();
				
				id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
				
				L2Skill skill = SkillHolder.getInstance().getInfo(id, level);
				if (skill == null)
				{
					_log.warning(getClass().getSimpleName() + ": Can't find phantom buff skill id: " + id + " level: " + level + " in phantomSkills.xml");
					continue;
				}
				buffSkillList.add(skill);
			}
			else if ("debuffSkill".equalsIgnoreCase(c.getNodeName()))
			{
				attrs = c.getAttributes();
				
				id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
				
				L2Skill skill = SkillHolder.getInstance().getInfo(id, level);
				if (skill == null)
				{
					_log.warning(getClass().getSimpleName() + ": Can't find phantom buff skill id: " + id + " level: " + level + " in phantomSkills.xml");
					continue;
				}
				debuffSkillList.add(skill);
			}
			else if ("activePvpSkill".equalsIgnoreCase(c.getNodeName()))
			{
				attrs = c.getAttributes();
				
				id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
				
				L2Skill skill = SkillHolder.getInstance().getInfo(id, level);
				if (skill == null)
				{
					_log.warning(getClass().getSimpleName() + ": Can't find phantom buff skill id: " + id + " level: " + level + " in phantomSkills.xml");
					continue;
				}
				pvpSkillList.add(skill);
			}
			else if ("healSkill".equalsIgnoreCase(c.getNodeName()))
			{
				attrs = c.getAttributes();
				
				id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
				
				L2Skill skill = SkillHolder.getInstance().getInfo(id, level);
				if (skill == null)
				{
					_log.warning(getClass().getSimpleName() + ": Can't find phantom heal skill id: " + id + " level: " + level + " in phantomSkills.xml");
					continue;
				}
				healSkillList.add(skill);
			}
		}
		_magicAttackSkills.put(classId, mAttackSkillList);
		_figherAttackSkills.put(classId, fAttackSkillList);
		_buffSkills.put(classId, buffSkillList);
		_debuffSkills.put(classId, debuffSkillList);
		_activePvpSkills.put(classId, pvpSkillList);
		_healSkills.put(classId, healSkillList);
	}
	
	public FastList<L2Skill> getMagicAttackSkill(ClassId cId)
	{
		return _magicAttackSkills.get(cId);
	}
	
	public FastList<L2Skill> getFigherAttackSkill(ClassId cId)
	{
		return _figherAttackSkills.get(cId);
	}
	
	public FastList<L2Skill> getBuffSkillList(ClassId cId)
	{
		return _buffSkills.get(cId);
	}
	
	public FastList<L2Skill> getDebuffSkillList(ClassId cId)
	{
		return _debuffSkills.get(cId);
	}
	
	public FastList<L2Skill> getActivePvpSkillList(ClassId cId)
	{
		return _activePvpSkills.get(cId);
	}
	
	public FastList<L2Skill> getHealSkillList(ClassId cId)
	{
		return _healSkills.get(cId);
	}
	
	public L2Skill getRndMagicAttackSkill(ClassId cId)
	{
		L2Skill skill = null;
		FastList<L2Skill> rndSkill = getMagicAttackSkill(cId);
		if ((skill == null) && ((rndSkill != null) && !rndSkill.isEmpty() && (rndSkill.size() > 0)))
		{
			skill = rndSkill.get(Rnd.get(0, rndSkill.size() - 1));
		}
		return skill;
	}
	
	public L2Skill getRndFighterAttackSkill(ClassId cId)
	{
		L2Skill skill = null;
		FastList<L2Skill> rndSkill = getFigherAttackSkill(cId);
		if ((skill == null) && ((rndSkill != null) && !rndSkill.isEmpty() && (rndSkill.size() > 0)))
		{
			skill = rndSkill.get(Rnd.get(0, rndSkill.size() - 1));
		}
		return skill;
	}
	
	public L2Skill getRndBuffSkill(ClassId cId)
	{
		L2Skill skill = null;
		FastList<L2Skill> rndSkill = getBuffSkillList(cId);
		if ((skill == null) && ((rndSkill != null) && !rndSkill.isEmpty() && (rndSkill.size() > 0)))
		{
			skill = rndSkill.get(Rnd.get(0, rndSkill.size() - 1));
		}
		return skill;
	}
	
	public L2Skill getRndDebuffSkill(ClassId cId)
	{
		L2Skill skill = null;
		FastList<L2Skill> rndSkill = getDebuffSkillList(cId);
		if ((skill == null) && ((rndSkill != null) && !rndSkill.isEmpty() && (rndSkill.size() > 0)))
		{
			skill = rndSkill.get(Rnd.get(0, rndSkill.size() - 1));
		}
		return skill;
	}
	
	public L2Skill getRndAcvitePvpSkill(ClassId cId)
	{
		L2Skill skill = null;
		FastList<L2Skill> rndSkill = getActivePvpSkillList(cId);
		if ((skill == null) && ((rndSkill != null) && !rndSkill.isEmpty() && (rndSkill.size() > 0)))
		{
			skill = rndSkill.get(Rnd.get(0, rndSkill.size() - 1));
		}
		return skill;
	}
	
	public L2Skill getRndHealSkill(ClassId cId)
	{
		L2Skill skill = null;
		FastList<L2Skill> rndSkill = getHealSkillList(cId);
		if ((skill == null) && ((rndSkill != null) && !rndSkill.isEmpty() && (rndSkill.size() > 0)))
		{
			skill = rndSkill.get(Rnd.get(0, rndSkill.size() - 1));
		}
		return skill;
	}
	
	public static PhantomSkillsParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PhantomSkillsParser _instance = new PhantomSkillsParser();
	}
}