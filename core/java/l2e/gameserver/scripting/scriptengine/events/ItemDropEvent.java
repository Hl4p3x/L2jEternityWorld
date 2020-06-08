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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2e.gameserver.scripting.scriptengine.events;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.scripting.scriptengine.events.impl.L2Event;

public class ItemDropEvent implements L2Event
{
	private L2ItemInstance item;
	private L2Character dropper;
	private Location loc;
	
	public L2ItemInstance getItem()
	{
		return item;
	}
	
	public void setItem(L2ItemInstance i)
	{
		item = i;
	}
	
	public L2Character getDropper()
	{
		return dropper;
	}
	
	public void setDropper(L2Character d)
	{
		dropper = d;
	}
	
	public Location getLocation()
	{
		return loc;
	}
	
	public void setLocation(Location l)
	{
		loc = l;
	}
}