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
package l2e.gameserver.model.actor.tasks.npc.trap;

import l2e.gameserver.model.actor.instance.L2TrapInstance;

public class TrapUnsummonTask implements Runnable
{
	private final L2TrapInstance _trap;
	
	public TrapUnsummonTask(L2TrapInstance trap)
	{
		_trap = trap;
	}
	
	@Override
	public void run()
	{
		_trap.unSummon();
	}
}