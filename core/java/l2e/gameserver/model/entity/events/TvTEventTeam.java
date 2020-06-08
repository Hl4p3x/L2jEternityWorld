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
package l2e.gameserver.model.entity.events;

import java.util.Map;

import javolution.util.FastMap;

import l2e.gameserver.model.actor.instance.L2PcInstance;

public class TvTEventTeam
{
	private final String _name;
	private int[] _coordinates = new int[3];
	private short _points;
	private Map<Integer, L2PcInstance> _participatedPlayers = new FastMap<>();
	
	public TvTEventTeam(String name, int[] coordinates)
	{
		_name = name;
		_coordinates = coordinates;
		_points = 0;
	}
	
	public boolean addPlayer(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		
		synchronized (_participatedPlayers)
		{
			_participatedPlayers.put(playerInstance.getObjectId(), playerInstance);
		}
		
		return true;
	}
	
	public void removePlayer(int playerObjectId)
	{
		synchronized (_participatedPlayers)
		{
			_participatedPlayers.remove(playerObjectId);
		}
	}
	
	public void increasePoints()
	{
		++_points;
	}
	
	public void cleanMe()
	{
		_participatedPlayers.clear();
		_participatedPlayers = new FastMap<>();
		_points = 0;
	}
	
	public boolean containsPlayer(int playerObjectId)
	{
		boolean containsPlayer;
		
		synchronized (_participatedPlayers)
		{
			containsPlayer = _participatedPlayers.containsKey(playerObjectId);
		}
		
		return containsPlayer;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int[] getCoordinates()
	{
		return _coordinates;
	}
	
	public short getPoints()
	{
		return _points;
	}
	
	public Map<Integer, L2PcInstance> getParticipatedPlayers()
	{
		Map<Integer, L2PcInstance> participatedPlayers = null;
		
		synchronized (_participatedPlayers)
		{
			participatedPlayers = _participatedPlayers;
		}
		
		return participatedPlayers;
	}
	
	public int getParticipatedPlayerCount()
	{
		int participatedPlayerCount;
		
		synchronized (_participatedPlayers)
		{
			participatedPlayerCount = _participatedPlayers.size();
		}
		
		return participatedPlayerCount;
	}
}