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
package l2e.gameserver.scripting.scriptengine.events;

import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.scripting.scriptengine.events.impl.L2Event;

public class PlayerEvent implements L2Event
{
	private int _objectId;
	private String _name;
	private L2GameClient _client;
	
	public PlayerEvent()
	{	
	}
	
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setClient(L2GameClient client)
	{
		_client = client;
	}
	
	public L2GameClient getClient()
	{
		return _client;
	}
}