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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2e.gameserver.scripting.scriptengine.listeners.character;

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.events.AbstractCharEvents;
import l2e.gameserver.model.actor.events.listeners.IAttackEventListener;
import l2e.gameserver.scripting.scriptengine.impl.L2JListener;

public abstract class AttackListener extends L2JListener implements IAttackEventListener
{
	private L2Character _character = null;

	public AttackListener(L2Character character)
	{
		_character = character;
		register();
	}
	
	@Override
	public void register()
	{
		if (_character == null)
		{
			AbstractCharEvents.registerStaticListener(this);
		}
		else
		{
			_character.getEvents().registerListener(this);
		}
	}
	
	@Override
	public void unregister()
	{
		if (_character == null)
		{
			AbstractCharEvents.unregisterStaticListener(this);
		}
		else
		{
			_character.getEvents().unregisterListener(this);
		}
	}

	public L2Character getCharacter()
	{
		return _character;
	}
}