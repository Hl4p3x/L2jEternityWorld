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
package l2e.gameserver.instancemanager;

import java.util.Map;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.interfaces.IL2Procedure;
import l2e.gameserver.network.L2GameClient;
import l2e.util.L2FastMap;
import l2e.util.L2HashMap;

public final class AntiFeedManager
{
	public static final int GAME_ID = 0;
	public static final int OLYMPIAD_ID = 1;
	public static final int TVT_ID = 2;
	public static final int L2EVENT_ID = 3;
	public static final int LAST_HERO_ID = 4;
	public static final int TVT_ROUND_ID = 5;
	
	private final Map<Integer, Long> _lastDeathTimes = new L2FastMap<>(true);
	private final L2HashMap<Integer, Map<Integer, Connections>> _eventIPs = new L2HashMap<>();
	
	protected AntiFeedManager()
	{
	}
	
	public final void setLastDeathTime(int objectId)
	{
		_lastDeathTimes.put(objectId, System.currentTimeMillis());
	}
	
	public final boolean check(L2Character attacker, L2Character target)
	{
		if (!Config.ANTIFEED_ENABLE)
		{
			return true;
		}
		
		if (target == null)
		{
			return false;
		}
		
		final L2PcInstance targetPlayer = target.getActingPlayer();
		if (targetPlayer == null)
		{
			return false;
		}
		
		if ((Config.ANTIFEED_INTERVAL > 0) && _lastDeathTimes.containsKey(targetPlayer.getObjectId()))
		{
			if ((System.currentTimeMillis() - _lastDeathTimes.get(targetPlayer.getObjectId())) < Config.ANTIFEED_INTERVAL)
			{
				return false;
			}
		}
		
		if (Config.ANTIFEED_DUALBOX && (attacker != null))
		{
			final L2PcInstance attackerPlayer = attacker.getActingPlayer();
			if (attackerPlayer == null)
			{
				return false;
			}
			
			final L2GameClient targetClient = targetPlayer.getClient();
			final L2GameClient attackerClient = attackerPlayer.getClient();
			if ((targetClient == null) || (attackerClient == null) || targetClient.isDetached() || attackerClient.isDetached())
			{
				return !Config.ANTIFEED_DISCONNECTED_AS_DUALBOX;
			}
			
			return !targetClient.getConnectionAddress().equals(attackerClient.getConnectionAddress());
		}
		return true;
	}
	
	public final void clear()
	{
		_lastDeathTimes.clear();
	}
	
	public final void registerEvent(int eventId)
	{
		if (!_eventIPs.containsKey(eventId))
		{
			_eventIPs.put(eventId, new FastMap<Integer, Connections>());
		}
	}
	
	public final boolean tryAddPlayer(int eventId, L2PcInstance player, int max)
	{
		return tryAddClient(eventId, player.getClient(), max);
	}
	
	public final boolean tryAddClient(int eventId, L2GameClient client, int max)
	{
		if (client == null)
		{
			return false;
		}
		
		final Map<Integer, Connections> event = _eventIPs.get(eventId);
		if (event == null)
		{
			return false;
		}
		
		final Integer addrHash = Integer.valueOf(client.getConnectionAddress().hashCode());
		int limit = max;
		if (Config.DUALBOX_CHECK_WHITELIST.containsKey(addrHash))
		{
			limit += Config.DUALBOX_CHECK_WHITELIST.get(addrHash);
		}
		
		Connections conns;
		synchronized (event)
		{
			conns = event.get(addrHash);
			if (conns == null)
			{
				conns = new Connections();
				event.put(addrHash, conns);
			}
		}
		
		return conns.testAndIncrement(limit);
	}
	
	public final boolean removePlayer(int eventId, L2PcInstance player)
	{
		final L2GameClient client = player.getClient();
		if (client == null)
		{
			return false;
		}
		
		final Map<Integer, Connections> event = _eventIPs.get(eventId);
		if (event == null)
		{
			return false;
		}
		
		final Integer addrHash = Integer.valueOf(client.getConnectionAddress().hashCode());
		Connections conns = event.get(addrHash);
		if (conns == null)
		{
			return false;
		}
		
		synchronized (event)
		{
			if (conns.testAndDecrement())
			{
				event.remove(addrHash);
			}
		}
		
		return true;
	}
	
	public final void onDisconnect(L2GameClient client)
	{
		if (client == null)
		{
			return;
		}
		
		final Integer addrHash = Integer.valueOf(client.getConnectionAddress().hashCode());
		_eventIPs.executeForEachValue(new DisconnectProcedure(addrHash));
	}
	
	public final void clear(int eventId)
	{
		final Map<Integer, Connections> event = _eventIPs.get(eventId);
		if (event != null)
		{
			event.clear();
		}
	}
	
	public final int getLimit(L2PcInstance player, int max)
	{
		return getLimit(player.getClient(), max);
	}
	
	public final int getLimit(L2GameClient client, int max)
	{
		if (client == null)
		{
			return max;
		}
		
		final Integer addrHash = Integer.valueOf(client.getConnectionAddress().hashCode());
		int limit = max;
		if (Config.DUALBOX_CHECK_WHITELIST.containsKey(addrHash))
		{
			limit += Config.DUALBOX_CHECK_WHITELIST.get(addrHash);
		}
		return limit;
	}
	
	protected static final class Connections
	{
		private int _num = 0;
		
		public final synchronized boolean testAndIncrement(int max)
		{
			if (_num < max)
			{
				_num++;
				return true;
			}
			return false;
		}
		
		public final synchronized boolean testAndDecrement()
		{
			if (_num > 0)
			{
				_num--;
			}
			
			return _num == 0;
		}
	}
	
	private static final class DisconnectProcedure implements IL2Procedure<Map<Integer, Connections>>
	{
		private final Integer _addrHash;
		
		public DisconnectProcedure(Integer addrHash)
		{
			_addrHash = addrHash;
		}
		
		@Override
		public final boolean execute(Map<Integer, Connections> event)
		{
			final Connections conns = event.get(_addrHash);
			if (conns != null)
			{
				synchronized (event)
				{
					if (conns.testAndDecrement())
					{
						event.remove(_addrHash);
					}
				}
			}
			return true;
		}
	}
	
	public static final AntiFeedManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AntiFeedManager _instance = new AntiFeedManager();
	}
}