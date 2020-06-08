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

import l2e.gameserver.data.xml.AdminParser;

public class L2AdminCommandAccessRight
{
	private final String _adminCommand;
	private final int _accessLevel;
	private final boolean _requireConfirm;
	
	public L2AdminCommandAccessRight(StatsSet set)
	{
		_adminCommand = set.getString("command");
		_requireConfirm = set.getBool("confirmDlg", false);
		_accessLevel = set.getInteger("accessLevel", 7);
	}
	
	public L2AdminCommandAccessRight(String command, boolean confirm, int level)
	{
		_adminCommand = command;
		_requireConfirm = confirm;
		_accessLevel = level;
	}
	
	public String getAdminCommand()
	{
		return _adminCommand;
	}
	
	public boolean hasAccess(L2AccessLevel characterAccessLevel)
	{
		L2AccessLevel accessLevel = AdminParser.getInstance().getAccessLevel(_accessLevel);
		return ((accessLevel.getLevel() == characterAccessLevel.getLevel()) || characterAccessLevel.hasChildAccess(accessLevel));
	}
	
	public boolean getRequireConfirm()
	{
		return _requireConfirm;
	}
}