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
package l2e.gameserver.model.base;

import java.util.regex.Matcher;

public final class ClassInfo
{
	private final ClassId _classId;
	private final String _className;
	private final String _classServName;
	private final ClassId _parentClassId;
	
	public ClassInfo(ClassId classId, String className, String classServName, ClassId parentClassId)
	{
		_classId = classId;
		_className = className;
		_classServName = classServName;
		_parentClassId = parentClassId;
	}
	
	public ClassId getClassId()
	{
		return _classId;
	}
	
	public String getClassName()
	{
		return _className;
	}
	
	private int getClassClientId()
	{
		int classClientId = _classId.getId();
		if ((classClientId >= 0) && (classClientId <= 57))
		{
			classClientId += 247;
		}
		else if ((classClientId >= 88) && (classClientId <= 118))
		{
			classClientId += 1071;
		}
		else if ((classClientId >= 123) && (classClientId <= 136))
		{
			classClientId += 1438;
		}
		return classClientId;
	}
	
	public String getClientCode()
	{
		return "&$" + getClassClientId() + ";";
	}
	
	public String getEscapedClientCode()
	{
		return Matcher.quoteReplacement(getClientCode());
	}
	
	public String getClassServName()
	{
		return _classServName;
	}
	
	public ClassId getParentClassId()
	{
		return _parentClassId;
	}
}