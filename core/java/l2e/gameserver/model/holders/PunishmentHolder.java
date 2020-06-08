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
package l2e.gameserver.model.holders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2e.gameserver.model.punishment.PunishmentTask;
import l2e.gameserver.model.punishment.PunishmentType;

public class PunishmentHolder
{
	private final Map<String, Map<PunishmentType, PunishmentTask>> _holder = new ConcurrentHashMap<>();
	
	public void addPunishment(PunishmentTask task)
	{
		if (!task.isExpired())
		{
			String key = String.valueOf(task.getKey());
			if (!_holder.containsKey(key))
			{
				_holder.put(key, new ConcurrentHashMap<PunishmentType, PunishmentTask>());
			}
			_holder.get(key).put(task.getType(), task);
		}
	}

	public void stopPunishment(PunishmentTask task)
	{
		String key = String.valueOf(task.getKey());
		if (_holder.containsKey(key))
		{
			task.stopPunishment();
			final Map<PunishmentType, PunishmentTask> punishments = _holder.get(key);
			punishments.remove(task.getType());
			if (punishments.isEmpty())
			{
				_holder.remove(key);
			}
		}
	}

	public boolean hasPunishment(String key, PunishmentType type)
	{
		return getPunishment(key, type) != null;
	}
	
	public PunishmentTask getPunishment(String key, PunishmentType type)
	{
		if (_holder.containsKey(key))
		{
			return _holder.get(key).get(type);
		}
		return null;
	}
}