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

import l2e.gameserver.model.Location;

public class SpawnsHolder
{
	protected final int npcId;
	protected final Location loc;
	
	public SpawnsHolder(int _npcId, Location _spawnLoc)
	{
		npcId = _npcId;
		loc = _spawnLoc;
	}
	
	public int getX()
	{
		return loc.getX();
	}
	
	public int getY()
	{
		return loc.getY();
	}
	
	public int getZ()
	{
		return loc.getZ();
	}
	
	public int getHeading()
	{
		return loc.getHeading();
	}
}