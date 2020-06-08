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
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.skills.L2Skill;
import gnu.trove.map.hash.THashMap;

/**
 * Rework by LordWinter 26.06.2013 Fixed by L2J Eternity-World
 */
public class SchemesParser
{
	private final static Logger _log = Logger.getLogger(SchemesParser.class.getName());
	
	private final THashMap<String, ArrayList<SkillInfo>> _buffs;
	
	protected SchemesParser()
	{
		_buffs = new THashMap<>();
		load();
	}
	
	protected void load()
	{
		_buffs.clear();
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT, "data/communityBuffer.xml");
			if (!file.exists())
			{
				_log.warning(getClass().getSimpleName() + ": Couldn't find data/" + file.getName());
				return;
			}
			
			Document doc = factory.newDocumentBuilder().parse(file);
			NamedNodeMap attrs;
			String group;
			int id, level, adena, minLvl;
			
			for (Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			{
				if ("list".equalsIgnoreCase(list.getNodeName()))
				{
					for (Node groups = list.getFirstChild(); groups != null; groups = groups.getNextSibling())
					{
						if ("group".equalsIgnoreCase(groups.getNodeName()))
						{
							attrs = groups.getAttributes();
							group = attrs.getNamedItem("buff").getNodeValue();
							
							for (Node skills = groups.getFirstChild(); skills != null; skills = skills.getNextSibling())
							{
								if ("skill".equalsIgnoreCase(skills.getNodeName()))
								{
									attrs = skills.getAttributes();
									id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
									level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
									adena = Integer.parseInt(attrs.getNamedItem("adena").getNodeValue());
									minLvl = Integer.parseInt(attrs.getNamedItem("minLvl").getNodeValue());
									
									SkillInfo info = new SkillInfo();
									info._id = id;
									info._lvl = level;
									info._group = group;
									info._cost = adena;
									info._minLvl = minLvl;
									
									L2Skill skill = SkillHolder.getInstance().getInfo(info._id, info._lvl);
									if (skill == null)
									{
										_log.warning(getClass().getSimpleName() + ": Can't find skill id: " + info._id + " level: " + info._lvl + " in communityBuffer.xml");
										continue;
									}
									info._name = skill.getName();
									
									_buffs.putIfAbsent(info._group, new ArrayList<SkillInfo>());
									_buffs.get(info._group).add(info);
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warning(getClass().getSimpleName() + ": Error while loading buffs: " + e);
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + _buffs.size() + " Buff Templates.");
	}
	
	public Set<String> getBuffGroups()
	{
		return _buffs.keySet();
	}
	
	public ArrayList<SkillInfo> getBuffInfoByGroup(String group)
	{
		return _buffs.get(group);
	}
	
	public boolean buffsContainsSkill(int skillId, int skillLvl)
	{
		for (ArrayList<SkillInfo> infos : _buffs.values())
		{
			for (SkillInfo info : infos)
			{
				if ((skillId == info._id) && (skillLvl == info._lvl))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean buffsIdContainsSkill(int skillId)
	{
		for (ArrayList<SkillInfo> infos : _buffs.values())
		{
			for (SkillInfo info : infos)
			{
				if (skillId == info._id)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public int getSkillFee(int skillId)
	{
		for (ArrayList<SkillInfo> infos : _buffs.values())
		{
			for (SkillInfo info : infos)
			{
				if (skillId == info._id)
				{
					return (int) info._cost;
				}
			}
		}
		return 0;
	}
	
	public int getSkillMinLvl(int skillId)
	{
		for (ArrayList<SkillInfo> infos : _buffs.values())
		{
			for (SkillInfo info : infos)
			{
				if (skillId == info._id)
				{
					return info._minLvl;
				}
			}
		}
		return 0;
	}
	
	public class SkillInfo
	{
		public int _id;
		public int _lvl;
		public String _group;
		public long _cost;
		public String _name;
		public int _minLvl;
	}
	
	public static SchemesParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SchemesParser _instance = new SchemesParser();
	}
}