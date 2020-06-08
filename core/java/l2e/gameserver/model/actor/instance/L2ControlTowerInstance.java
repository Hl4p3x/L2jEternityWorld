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
package l2e.gameserver.model.actor.instance;

import java.util.List;
import java.util.logging.Level;

import javolution.util.FastList;

import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Tower;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;

public class L2ControlTowerInstance extends L2Tower
{
	private List<L2Spawn> _guards;
	
	public L2ControlTowerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ControlTowerInstance);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (getCastle().getSiege().getIsInProgress())
		{
			getCastle().getSiege().killedCT(this);
			
			if ((_guards != null) && !_guards.isEmpty())
			{
				for (L2Spawn spawn : _guards)
				{
					if (spawn == null)
						continue;
					try
					{
						spawn.stopRespawn();
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING, "Error at L2ControlTowerInstance", e);
					}
				}
				_guards.clear();
			}
		}
		return super.doDie(killer);
	}
	
	public void registerGuard(L2Spawn guard)
	{
		getGuards().add(guard);
	}
	
	private final List<L2Spawn> getGuards()
	{
		if (_guards == null)
		{
			synchronized (this)
			{
				if (_guards == null)
					_guards = new FastList<>();
			}
		}
		return _guards;
	}
}