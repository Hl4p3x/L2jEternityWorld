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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.items.PcItemTemplate;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class PhantomArmorParser extends DocumentParser
{
	protected final Map<ClassId, List<PcItemTemplate>> _phantomNgradeItemsList = new HashMap<>();
	protected final Map<ClassId, List<PcItemTemplate>> _phantomDgradeItemsList = new HashMap<>();
	protected final Map<ClassId, List<PcItemTemplate>> _phantomCgradeItemsList = new HashMap<>();
	protected final Map<ClassId, List<PcItemTemplate>> _phantomBgradeItemsList = new HashMap<>();
	protected final Map<ClassId, List<PcItemTemplate>> _phantomAgradeItemsList = new HashMap<>();
	protected final Map<ClassId, List<PcItemTemplate>> _phantomSgradeItemsList = new HashMap<>();
	protected final Map<ClassId, List<PcItemTemplate>> _phantomS80gradeItemsList = new HashMap<>();
	protected final Map<ClassId, List<PcItemTemplate>> _phantomS84gradeItemsList = new HashMap<>();
	
	protected PhantomArmorParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_phantomNgradeItemsList.clear();
		_phantomDgradeItemsList.clear();
		_phantomCgradeItemsList.clear();
		_phantomBgradeItemsList.clear();
		_phantomAgradeItemsList.clear();
		_phantomSgradeItemsList.clear();
		_phantomS80gradeItemsList.clear();
		_phantomS84gradeItemsList.clear();
		
		parseDatapackFile("data/stats/chars/phantoms/phantomArmorSets.xml");
		
		_log.info(getClass().getSimpleName() + ": Loaded " + (_phantomNgradeItemsList.size() + _phantomDgradeItemsList.size() + _phantomCgradeItemsList.size() + _phantomBgradeItemsList.size() + _phantomAgradeItemsList.size() + _phantomSgradeItemsList.size() + _phantomS80gradeItemsList.size() + _phantomS84gradeItemsList.size()) + " Phamtom Sets.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node list = n.getFirstChild(); list != null; list = list.getNextSibling())
				{
					if ("NgradeItems".equalsIgnoreCase(list.getNodeName()))
					{
						parseNgGrade(list);
					}
					else if ("DgradeItems".equalsIgnoreCase(list.getNodeName()))
					{
						parseDGrade(list);
					}
					else if ("CgradeItems".equalsIgnoreCase(list.getNodeName()))
					{
						parseCGrade(list);
					}
					else if ("BgradeItems".equalsIgnoreCase(list.getNodeName()))
					{
						parseBGrade(list);
					}
					else if ("AgradeItems".equalsIgnoreCase(list.getNodeName()))
					{
						parseAGrade(list);
					}
					else if ("SgradeItems".equalsIgnoreCase(list.getNodeName()))
					{
						parseSGrade(list);
					}
					else if ("S80gradeItems".equalsIgnoreCase(list.getNodeName()))
					{
						parseS80Grade(list);
					}
					else if ("S84gradeItems".equalsIgnoreCase(list.getNodeName()))
					{
						parseS84Grade(list);
					}
				}
			}
		}
	}
	
	private void parseNgGrade(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_phantomNgradeItemsList.put(classId, equipList);
	}
	
	private void parseDGrade(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_phantomDgradeItemsList.put(classId, equipList);
	}
	
	private void parseCGrade(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_phantomCgradeItemsList.put(classId, equipList);
	}
	
	private void parseBGrade(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_phantomBgradeItemsList.put(classId, equipList);
	}
	
	private void parseAGrade(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_phantomAgradeItemsList.put(classId, equipList);
	}
	
	private void parseSGrade(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_phantomSgradeItemsList.put(classId, equipList);
	}
	
	private void parseS80Grade(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_phantomS80gradeItemsList.put(classId, equipList);
	}
	
	private void parseS84Grade(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_phantomS84gradeItemsList.put(classId, equipList);
	}
	
	public List<PcItemTemplate> getNgGradeList(ClassId cId)
	{
		return _phantomNgradeItemsList.get(cId);
	}
	
	public List<PcItemTemplate> getDGradeList(ClassId cId)
	{
		return _phantomDgradeItemsList.get(cId);
	}
	
	public List<PcItemTemplate> getCGradeList(ClassId cId)
	{
		return _phantomCgradeItemsList.get(cId);
	}
	
	public List<PcItemTemplate> getBGradeList(ClassId cId)
	{
		return _phantomBgradeItemsList.get(cId);
	}
	
	public List<PcItemTemplate> getAGradeList(ClassId cId)
	{
		return _phantomAgradeItemsList.get(cId);
	}
	
	public List<PcItemTemplate> getSGradeList(ClassId cId)
	{
		return _phantomSgradeItemsList.get(cId);
	}
	
	public List<PcItemTemplate> getS80GradeList(ClassId cId)
	{
		return _phantomS80gradeItemsList.get(cId);
	}
	
	public List<PcItemTemplate> getS84GradeList(ClassId cId)
	{
		return _phantomS84gradeItemsList.get(cId);
	}
	
	public static PhantomArmorParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PhantomArmorParser _instance = new PhantomArmorParser();
	}
}