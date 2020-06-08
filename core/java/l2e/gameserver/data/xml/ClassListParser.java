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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.ClassInfo;

public final class ClassListParser extends DocumentParser
{
	private static final Map<ClassId, ClassInfo> _classData = new HashMap<>();
	
	protected ClassListParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_classData.clear();
		parseDatapackFile("data/stats/chars/classList.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _classData.size() + " Class data.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node attr;
		ClassId classId;
		String className;
		String classServName;
		ClassId parentClassId;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					attrs = d.getAttributes();
					if ("class".equals(d.getNodeName()))
					{
						attr = attrs.getNamedItem("classId");
						classId = ClassId.getClassId(parseInt(attr));
						attr = attrs.getNamedItem("name");
						className = attr.getNodeValue();
						attr = attrs.getNamedItem("serverName");
						classServName = attr.getNodeValue();
						attr = attrs.getNamedItem("parentClassId");
						parentClassId = (attr != null) ? ClassId.getClassId(parseInt(attr)) : null;
						_classData.put(classId, new ClassInfo(classId, className, classServName, parentClassId));
					}
				}
			}
		}
	}
	
	public Map<ClassId, ClassInfo> getClassList()
	{
		return _classData;
	}
	
	public ClassInfo getClass(final ClassId classId)
	{
		return _classData.get(classId);
	}
	
	public ClassInfo getClass(final int classId)
	{
		final ClassId id = ClassId.getClassId(classId);
		return (id != null) ? _classData.get(id) : null;
	}
	
	public ClassInfo getClass(final String classServName)
	{
		for (final ClassInfo classInfo : _classData.values())
		{
			if (classInfo.getClassServName().equals(classServName))
			{
				return classInfo;
			}
		}
		return null;
	}
	
	public static ClassListParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClassListParser _instance = new ClassListParser();
	}
}