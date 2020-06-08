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

public class L2WalkRoute
{
	private final String _name;
	private final List<L2NpcWalkerNode> _nodeList;
	private final boolean _repeatWalk;
	private boolean _stopAfterCycle;
	private final byte _repeatType;
	
	public L2WalkRoute(String name, List<L2NpcWalkerNode> route, boolean repeat, boolean once, byte repeatType)
	{	
		_name = name;
		_nodeList = route;
		_repeatType = repeatType;
		_repeatWalk = ((_repeatType >= 0) && (_repeatType <= 2)) ? repeat : false;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public List<L2NpcWalkerNode> getNodeList()
	{
		return _nodeList;
	}
	
	public L2NpcWalkerNode getLastNode()
	{
		return _nodeList.get(_nodeList.size() - 1);
	}
	
	public boolean repeatWalk()
	{
		return _repeatWalk;
	}
	
	public boolean doOnce()
	{
		return _stopAfterCycle;
	}
	
	public byte getRepeatType()
	{
		return _repeatType;
	}
	
	public int getNodesCount()
	{
		return _nodeList.size();
	}
}