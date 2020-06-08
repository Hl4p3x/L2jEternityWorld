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
package l2e.gameserver.model;

import l2e.gameserver.model.interfaces.IIdentifiable;

public final class FortSiegeSpawn extends Location implements IIdentifiable
{
	Location _location;
	private final int _npcId;
	private final int _heading;
	private final int _fortId;
	private final int _id;
	
	public FortSiegeSpawn(int fort_id, int x, int y, int z, int heading, int npc_id, int id)
	{
		super(x, y, z, heading);
		
		_fortId = fort_id;
		_location = new Location(x, y, z, heading);
		_heading = heading;
		_npcId = npc_id;
		_id = id;
	}
	
	public int getFortId()
	{
		return _fortId;
	}
	
	@Override
	public int getId()
	{
		return _npcId;
	}
	
	@Override
	public int getHeading()
	{
		return _heading;
	}
	
	@Override
	public Location getLocation()
	{
		return _location;
	}
	
	public int getMessageId()
	{
		return _id;
	}
}