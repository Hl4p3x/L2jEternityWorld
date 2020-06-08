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
package l2e.gameserver.model.entity.events.phoenix.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import l2e.gameserver.model.entity.events.phoenix.Configuration;
import l2e.gameserver.model.entity.events.phoenix.container.EventContainer;

public class SingleEventStatus extends EventStatus
{
	private final List<EventPlayer> team;
	
	public SingleEventStatus(Integer eventContainerId)
	{
		event = EventContainer.getInstance().getEvent(eventContainerId);
		team = new LinkedList<>();
	}
	
	@Override
	public String generateList()
	{
		sb.clear();
		
		for (EventPlayer player : event.getPlayersOfTeam(1))
		{
			team.add(player);
		}
		
		Collections.sort(team);
		Collections.reverse(team);
		
		sb.append("<center><table width=270 bgcolor=5A5A5A><tr><td width=70>Running</td><td width=130><center>" + Configuration.getInstance().getString(event.getId(), "eventName") + "</td><td width=70>Time: " + event.getClock().getTimeInString() + "</td></tr></table>");
		sb.append("<br><table width=270>");
		
		for (EventPlayer p : team.subList(0, (team.size() < 10 ? team.size() : 10)))
		{
			sb.append("<tr><td>" + p.getName() + "</td><td>lvl " + p.getLevel() + "</td><td>" + p.getClassName() + "</td><td>" + p.getScore() + "</td></tr>");
		}
		sb.append("</table>");
		team.clear();
		
		return sb.toString();
	}
}