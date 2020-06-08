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
package l2e.gameserver.model.entity.events.phoenix.container;

import java.util.Collection;

import javolution.util.FastMap;
import l2e.gameserver.model.entity.events.phoenix.io.Out;
import l2e.gameserver.model.entity.events.phoenix.model.EventPlayer;

public class PlayerContainer
{
	private static class SingletonHolder
	{
		protected static final PlayerContainer _instance = new PlayerContainer();
	}
	
	public static final PlayerContainer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final FastMap<Integer, EventPlayer> players;
	
	public PlayerContainer()
	{
		players = new FastMap<Integer, EventPlayer>().shared();
	}
	
	public void clearPlayers()
	{
		players.clear();
	}
	
	public EventPlayer createInfo(Integer player)
	{
		EventPlayer pi = new EventPlayer(Out.getPlayerById(player));
		players.put(player, pi);
		return pi;
	}
	
	public void deleteInfo(EventPlayer player)
	{
		players.remove(player.getPlayersId());
	}
	
	public void deleteInfo(int player)
	{
		players.remove(player);
	}
	
	public void deleteInfo(Integer player)
	{
		players.remove(player.intValue());
	}
	
	public EventPlayer getPlayer(int id)
	{
		return players.get(id);
	}
	
	public EventPlayer getPlayer(Integer id)
	{
		return players.get(id.intValue());
	}
	
	public EventPlayer getPlayerByName(String name)
	{
		for (EventPlayer player : players.values())
		{
			if (name.equals(player.getName()))
			{
				return player;
			}
		}
		return null;
	}
	
	public Collection<EventPlayer> getPlayers()
	{
		return players.values();
	}
}