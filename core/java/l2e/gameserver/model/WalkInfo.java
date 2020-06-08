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

import java.util.concurrent.ScheduledFuture;

import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.quest.Quest;
import l2e.util.Rnd;

public class WalkInfo
{
	private final String _routeName;
	
	private ScheduledFuture<?> _walkCheckTask;
	private boolean _blocked = false;
	private boolean _suspended = false;
	private boolean _stoppedByAttack = false;
	private int _currentNode = 0;
	private boolean _forward = true;
	private long _lastActionTime;
	
	public WalkInfo(String routeName)
	{
		_routeName = routeName;
	}
	
	public L2WalkRoute getRoute()
	{
		return WalkingManager.getInstance().getRoute(_routeName);
	}
	
	public L2NpcWalkerNode getCurrentNode()
	{
		return getRoute().getNodeList().get(_currentNode);
	}
	
	public void calculateNextNode(L2Npc npc)
	{
		if (getRoute().getRepeatType() == WalkingManager.REPEAT_RANDOM)
		{
			int newNode = _currentNode;
			
			while (newNode == _currentNode)
			{
				newNode = Rnd.get(getRoute().getNodesCount());
			}
			
			_currentNode = newNode;
			npc.sendDebugMessage("Route: " + getRoute().getName() + ", next random node is " + _currentNode);
		}
		else
		{
			if (_forward)
			{
				_currentNode++;
			}
			else
			{
				_currentNode--;
			}
			
			if (_currentNode == getRoute().getNodesCount())
			{
				if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ROUTE_FINISHED) != null)
				{
					for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ROUTE_FINISHED))
					{
						quest.notifyRouteFinished(npc);
					}
				}
				npc.sendDebugMessage("Route: " + getRoute().getName() + ", last node arrived");
				
				if (!getRoute().repeatWalk())
				{
					WalkingManager.getInstance().cancelMoving(npc);
					return;
				}
				
				switch (getRoute().getRepeatType())
				{
					case WalkingManager.REPEAT_GO_BACK:
					{
						_forward = false;
						_currentNode -= 2;
						break;
					}
					case WalkingManager.REPEAT_GO_FIRST:
					{
						_currentNode = 0;
						break;
					}
					case WalkingManager.REPEAT_TELE_FIRST:
					{
						npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ());
						_currentNode = 0;
						break;
					}
				}
			}
			
			else if (_currentNode == -1)
			{
				_currentNode = 1;
				_forward = true;
			}
		}
	}
	
	public boolean isBlocked()
	{
		return _blocked;
	}
	
	public void setBlocked(boolean val)
	{
		_blocked = val;
	}
	
	public boolean isSuspended()
	{
		return _suspended;
	}
	
	public void setSuspended(boolean val)
	{
		_suspended = val;
	}
	
	public boolean isStoppedByAttack()
	{
		return _stoppedByAttack;
	}
	
	public void setStoppedByAttack(boolean val)
	{
		_stoppedByAttack = val;
	}
	
	public int getCurrentNodeId()
	{
		return _currentNode;
	}
	
	public long getLastAction()
	{
		return _lastActionTime;
	}
	
	public void setLastAction(long val)
	{
		_lastActionTime = val;
	}
	
	public ScheduledFuture<?> getWalkCheckTask()
	{
		return _walkCheckTask;
	}
	
	public void setWalkCheckTask(ScheduledFuture<?> val)
	{
		_walkCheckTask = val;
	}
}