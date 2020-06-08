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

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Henna;
import l2e.gameserver.scripting.scriptengine.events.impl.L2Event;

public class HennaEvent implements L2Event
{
	private L2PcInstance player;
	private L2Henna henna;
	private boolean add;
	
	public L2PcInstance getPlayer()
	{
		return player;
	}
	
	public void setPlayer(L2PcInstance p)
	{
		player = p;
	}
	
	public L2Henna getHenna()
	{
		return henna;
	}
	
	public void setHenna(L2Henna h)
	{
		henna = h;
	}
	
	public boolean isAdd()
	{
		return add;
	}
	
	public void setAdd(boolean add)
	{
		this.add = add;
	}
}