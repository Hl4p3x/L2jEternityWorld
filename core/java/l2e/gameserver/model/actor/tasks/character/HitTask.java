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
package l2e.gameserver.model.actor.tasks.character;

import l2e.gameserver.model.actor.L2Character;

public final class HitTask implements Runnable
{
	private final L2Character _character;
	private final L2Character _hitTarget;
	private final int _damage;
	private final boolean _crit;
	private final boolean _miss;
	private final byte _shld;
	private final boolean _soulshot;
	
	public HitTask(L2Character character, L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		_character = character;
		_hitTarget = target;
		_damage = damage;
		_crit = crit;
		_shld = shld;
		_miss = miss;
		_soulshot = soulshot;
	}
	
	@Override
	public void run()
	{
		if (_character != null)
		{
			_character.onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
		}
	}
}