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

import l2e.gameserver.model.interfaces.IIdentifiable;

public class ItemsHolder implements IIdentifiable
{
	private final int _id;
	private final int _objectId;
	private final long _count;
	
	public ItemsHolder(int id, long count)
	{
		_id = id;
		_objectId = -1;
		_count = count;
	}
	
	public ItemsHolder(int id, int objectId, long count)
	{
		_id = id;
		_objectId = objectId;
		_count = count;
	}
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public long getCount()
	{
		return _count;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": Id: " + _id + " Count: " + _count;
	}
}