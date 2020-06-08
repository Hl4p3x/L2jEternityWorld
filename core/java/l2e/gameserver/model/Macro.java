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
package l2e.gameserver.model;

import java.util.List;

import l2e.gameserver.model.interfaces.IIdentifiable;

public class Macro implements IIdentifiable
{
	public static final int CMD_TYPE_SKILL = 1;
	public static final int CMD_TYPE_ACTION = 3;
	public static final int CMD_TYPE_SHORTCUT = 4;
	
	private int _id;
	private final int _icon;
	private final String _name;
	private final String _descr;
	private final String _acronym;
	private final List<MacroCmd> _commands;
	
	public Macro(int id, int icon, String name, String descr, String acronym, List<MacroCmd> list)
	{
		setId(id);
		_icon = icon;
		_name = name;
		_descr = descr;
		_acronym = acronym;
		_commands = list;
	}
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	public void setId(int _id)
	{
		this._id = _id;
	}
	
	public int getIcon()
	{
		return _icon;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getDescr()
	{
		return _descr;
	}
	
	public String getAcronym()
	{
		return _acronym;
	}
	
	public List<MacroCmd> getCommands()
	{
		return _commands;
	}
}