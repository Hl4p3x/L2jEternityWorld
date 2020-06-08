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
import l2e.gameserver.model.entity.events.TvTEventTeam;
import l2e.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class TvtKillEvent implements L2Event
{
	private L2PcInstance killer;
	private L2PcInstance victim;
	private TvTEventTeam killerTeam;
	
	/**
	 * @return the killer
	 */
	public L2PcInstance getKiller()
	{
		return killer;
	}
	
	/**
	 * @param killer the killer to set
	 */
	public void setKiller(L2PcInstance killer)
	{
		this.killer = killer;
	}
	
	/**
	 * @return the victim
	 */
	public L2PcInstance getVictim()
	{
		return victim;
	}
	
	/**
	 * @param victim the victim to set
	 */
	public void setVictim(L2PcInstance victim)
	{
		this.victim = victim;
	}
	
	/**
	 * @return the killerTeam
	 */
	public TvTEventTeam getKillerTeam()
	{
		return killerTeam;
	}
	
	/**
	 * @param killerTeam the killerTeam to set
	 */
	public void setKillerTeam(TvTEventTeam killerTeam)
	{
		this.killerTeam = killerTeam;
	}
}
