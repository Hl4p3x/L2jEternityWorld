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

import l2e.gameserver.model.entity.FortSiege;
import l2e.gameserver.scripting.scriptengine.events.impl.L2Event;
import l2e.gameserver.scripting.scriptengine.impl.L2Script.EventStage;

/**
 * @author TheOne
 */
public class FortSiegeEvent implements L2Event
{
	private FortSiege siege;
	private EventStage stage;
	
	/**
	 * @return the siege
	 */
	public FortSiege getSiege()
	{
		return siege;
	}
	
	/**
	 * @param siege the siege to set
	 */
	public void setSiege(FortSiege siege)
	{
		this.siege = siege;
	}
	
	/**
	 * @return the stage
	 */
	public EventStage getStage()
	{
		return stage;
	}
	
	/**
	 * @param stage the stage to set
	 */
	public void setStage(EventStage stage)
	{
		this.stage = stage;
	}
}
