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
package l2e.gameserver.model.entity.events.phoenix;

import java.util.List;
import java.util.Map;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import l2e.gameserver.model.entity.events.phoenix.AbstractEvent.AbstractPhase;
import l2e.gameserver.model.entity.events.phoenix.container.EventContainer;
import l2e.gameserver.model.entity.events.phoenix.container.PlayerContainer;
import l2e.gameserver.model.entity.events.phoenix.function.Vote;
import l2e.gameserver.model.entity.events.phoenix.io.Out;
import l2e.gameserver.model.entity.events.phoenix.model.EventPlayer;
import l2e.gameserver.model.entity.events.phoenix.model.ManagerNpcHtml;

public class ManagerNpc
{
	protected int i;
	
	private static class SingletonHolder
	{
		public static final ManagerNpc _instance = new ManagerNpc();
	}
	
	public static ManagerNpc getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void showMain(Integer player)
	{
		TextBuilder builder = new TextBuilder();
		
		if (EventContainer.getInstance().getEventMap().size() > 1)
		{
			int count = 0;
			builder.append("<center><table width=270 bgcolor=4f4f4f><tr><td width=70><font color=ac9775>Registering</font></td><td width=130><center><font color=9f9f9f>Time left:</font> <font color=ac9775>" + "</font></td><td width=70><font color=9f9f9f>Votes:</font> <font color=ac9775>" + "</font></td></tr></table><br>");
			
			for (Map.Entry<Integer, AbstractEvent> event : EventContainer.getInstance().getEventMap().entrySet())
			{
				count++;
				builder.append("<center><table width=270 " + ((count % 2) == 1 ? "" : "bgcolor=4f4f4f") + "><tr><td width=180><font color=ac9775>" + Configuration.getInstance().getString(event.getValue().getId(), "eventName") + "</font></td><td width=30><font color=9f9f9f>Info</font></td><td width=30>");
				
				builder.append("<a action=\"bypass -h phoenix showreg " + event.getKey() + " 0\">Show</a>");
				
				builder.append("</td><td width=30><center><font color=9f9f9f>" + "1" + "</font></td></tr></table>");
			}
			
			Out.html(player, new ManagerNpcHtml(builder.toString()).string());
		}
		else if (EventContainer.getInstance().getEventMap().size() == 1)
		{
			showRegisterPage(player, EventContainer.getInstance().getEventMap().head().getNext().getKey(), 0);
		}
		else
		{
			if (Configuration.getInstance().getBoolean(0, "voteEnabled"))
			{
				showVoteList(player);
			}
			else
			{
				Out.html(player, new ManagerNpcHtml("Theres no active event.").string());
			}
		}
	}
	
	public void showRegisterPage(Integer player, Integer event, Integer beginIndex)
	{
		TextBuilder builder = new TextBuilder();
		
		builder.append("<center><table width=270 bgcolor=4f4f4f><tr><td width=70>");
		if ((PlayerContainer.getInstance().getPlayer(player) != null) && EventContainer.getInstance().getEvent(event).getPlayerList().contains(PlayerContainer.getInstance().getPlayer(player)))
		{
			builder.append("<a action=\"bypass -h phoenix unregister " + event + "\"><font color=9f9f9f>Unregister</font></a>");
		}
		else
		{
			builder.append("<a action=\"bypass -h phoenix register " + event + "\"><font color=9f9f9f>Register</font></a></a>");
		}
		builder.append("</td><td width=130><center><font color=ac9775>" + Configuration.getInstance().getString(EventContainer.getInstance().getEvent(event).getId(), "eventName") + "</font></td><td width=70><font color=9f9f9f>Time: " + EventContainer.getInstance().getEvent(event).getRegisterTimeLeft() + "</font></td></tr></table><br>");
		
		int count = 0;
		i = 0;
		List<EventPlayer> sublist;
		
		FastList<EventPlayer> list = EventContainer.getInstance().getEvent(event).getPlayerList();
		
		if ((beginIndex * 20) > list.size())
		{
			beginIndex -= 1;
		}
		
		if (list.size() >= ((beginIndex * 20) + 20))
		{
			sublist = list.subList(beginIndex * 20, (beginIndex * 20) + 20);
		}
		else
		{
			sublist = list.subList(beginIndex * 20, list.size());
		}
		
		for (EventPlayer p : sublist)
		{
			count++;
			i++;
			builder.append("<center><table width=270 " + ((count % 2) == 1 ? "" : "bgcolor=4f4f4f") + "><tr>" + "<td width=120><font color=ac9775>" + p.getName() + "</font></td>" + "<td width=40><font color=9f9f9f>lvl " + p.getLevel() + "</font></td>" + "<td width=110><font color=9f9f9f>" + p.getClassName() + "</font></td>" + "</tr></table>");
		}
		
		if (beginIndex > 0)
		{
			builder.append("<a action=\"bypass -h phoenix showreg " + event + " " + (beginIndex - 1) + "\">Previous</a>");
		}
		
		if (list.size() > ((beginIndex * 20) + 20))
		{
			builder.append(" <a action=\"bypass -h phoenix showreg " + event + " " + (beginIndex + 1) + "\">Next</a>");
		}
		Out.html(player, new ManagerNpcHtml(builder.toString()).string());
	}
	
	public void showVoteList(Integer player)
	{
		TextBuilder builder = new TextBuilder();
		int count = 0;
		
		builder.append("<center><table width=270 bgcolor=4f4f4f><tr><td width=70><font color=ac9775>Voting</font></td><td width=130><center><font color=9f9f9f>Time left:</font> <font color=ac9775>" + Vote.getInstance().getVoteTimeLeft() + "</font></td><td width=70><font color=9f9f9f>Votes:</font> <font color=ac9775>" + Vote.getInstance().getVoteCount() + "</font></td></tr></table><br>");
		
		for (Integer event : EventContainer.getInstance().eventIds)
		{
			count++;
			builder.append("<center><table width=270 " + ((count % 2) == 1 ? "" : "bgcolor=4f4f4f") + "><tr><td width=150><font color=ac9775>" + Configuration.getInstance().getString(event, "eventName") + "</font></td><td width=30><font color=9f9f9f>Info</font></td><td width=60>");
			
			if (Vote.getInstance().getBannedEvents().contains(event))
			{
				builder.append("<font color=ff0000>Vote</font>");
			}
			else if (!Vote.getInstance().getVotes().containsKey(player))
			{
				builder.append("<a action=\"bypass -h phoenix vote " + event + "\"><font color=ac9775>Vote</font></a>");
			}
			else
			{
				builder.append("<font color=ac9775>Vote</font>");
			}
			
			builder.append("</td><td width=30><center><font color=9f9f9f>" + Vote.getInstance().getVoteCount(event) + "</font></td></tr></table>");
		}
		builder.append("</body></html>");
		Out.html(player, new ManagerNpcHtml(builder.toString()).string());
	}
	
	public void showRunningList(Integer player)
	{
		TextBuilder builder = new TextBuilder();
		
		builder.append("<center><table width=270 bgcolor=4f4f4f><tr><td><font color=ac9775>Running events</font></td></tr></table><br>");
		
		builder.append("<table width=270>");
		
		for (Map.Entry<Integer, AbstractEvent> event : EventContainer.getInstance().getEventMap().entrySet())
		{
			if (event.getValue().getAbstractPhase() == AbstractPhase.RUNNING)
			{
				builder.append("<tr><td>" + Configuration.getInstance().getString(event.getValue().getId(), "eventName") + "</td><td>" + event.getValue().getStarted() + "</td><td><a action=\"bypass -h phoenix status " + event.getKey() + "\">Status</a></td></tr>");
			}
		}
		builder.append("</table>");
		Out.html(player, new ManagerNpcHtml(builder.toString()).string());
	}
	
	public void showStatusPage(Integer player, Integer event)
	{
		Out.html(player, new ManagerNpcHtml(EventContainer.getInstance().getEvent(event).getStatus().generateList()).string());
	}
}