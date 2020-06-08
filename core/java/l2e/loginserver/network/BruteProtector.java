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
package l2e.loginserver.network;

import java.util.ArrayList;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.Config;

public class BruteProtector
{
	private static final Logger _log = Logger.getLogger(BruteProtector.class.getName());
	private static final FastMap<String, ArrayList<Integer>> _clients = new FastMap<>();
	
	public static boolean canLogin(String ip)
	{
		if (!_clients.containsKey(ip))
		{
			_clients.put(ip, new ArrayList<Integer>());
			_clients.get(ip).add((int) (System.currentTimeMillis() / 1000));
			return true;
		}
		
		_clients.get(ip).add((int) (System.currentTimeMillis() / 1000));
		
		if (_clients.get(ip).size() < Config.BRUT_LOGON_ATTEMPTS)
		{
			return true;
		}
		
		int lastTime = 0;
		int avg = 0;
		for (int i : _clients.get(ip))
		{
			if (lastTime == 0)
			{
				lastTime = i;
				continue;
			}
			avg += i - lastTime;
			lastTime = i;
		}
		avg = avg / (_clients.get(ip).size() - 1);
		
		if (avg < Config.BRUT_AVG_TIME)
		{
			_log.warning("IP " + ip + " has " + avg + " seconds between login attempts. Possible BruteForce.");
			synchronized (_clients.get(ip))
			{
				_clients.get(ip).remove(0);
				_clients.get(ip).remove(0);
			}
			return false;
		}
		
		synchronized (_clients.get(ip))
		{
			_clients.get(ip).remove(0);
		}
		return true;
	}
}