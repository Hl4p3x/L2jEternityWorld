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
package l2e.gameserver.model.actor.knownlist;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.util.Util;
import l2e.util.L2FastMap;

public class ObjectKnownList
{
	private final L2Object _activeObject;
	private Map<Integer, L2Object> _knownObjects;
	
	public ObjectKnownList(L2Object activeObject)
	{
		_activeObject = activeObject;
	}
	
	public boolean addKnownObject(L2Object object)
	{
		if (object == null)
		{
			return false;
		}
		
		if ((getActiveObject().getInstanceId() != -1) && (object.getInstanceId() != getActiveObject().getInstanceId()))
		{
			return false;
		}
		
		if (object.isPlayer() && object.getActingPlayer().getAppearance().isGhost())
		{
			return false;
		}
		
		if (knowsObject(object))
		{
			return false;
		}
		
		if (!Util.checkIfInShortRadius(getDistanceToWatchObject(object), getActiveObject(), object, true))
		{
			return false;
		}
		return (getKnownObjects().put(object.getObjectId(), object) == null);
	}
	
	public final boolean knowsObject(L2Object object)
	{
		if (object == null)
		{
			return false;
		}
		return (getActiveObject() == object) || getKnownObjects().containsKey(object.getObjectId());
	}
	
	public void removeAllKnownObjects()
	{
		getKnownObjects().clear();
	}
	
	public final boolean removeKnownObject(L2Object object)
	{
		return removeKnownObject(object, false);
	}
	
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (object == null)
		{
			return false;
		}
		
		if (forget)
		{
			return true;
		}
		return getKnownObjects().remove(object.getObjectId()) != null;
	}
	
	public final void findObjects()
	{
		final L2WorldRegion region = getActiveObject().getWorldRegion();
		if (region == null)
		{
			return;
		}
		
		if (getActiveObject().isPlayable())
		{
			for (L2WorldRegion regi : region.getSurroundingRegions())
			{
				Collection<L2Object> vObj = regi.getVisibleObjects().values();
				for (L2Object object : vObj)
				{
					if (object != getActiveObject())
					{
						addKnownObject(object);
						if (object instanceof L2Character)
						{
							object.getKnownList().addKnownObject(getActiveObject());
						}
					}
				}
			}
		}
		else if (getActiveObject() instanceof L2Character)
		{
			for (L2WorldRegion regi : region.getSurroundingRegions())
			{
				if (regi.isActive())
				{
					Collection<L2Playable> vPls = regi.getVisiblePlayable().values();
					for (L2Object object : vPls)
					{
						if (object != getActiveObject())
						{
							addKnownObject(object);
						}
					}
				}
			}
		}
	}
	
	public void forgetObjects(boolean fullCheck)
	{
		final Collection<L2Object> objs = getKnownObjects().values();
		final Iterator<L2Object> oIter = objs.iterator();
		L2Object object;
		while (oIter.hasNext())
		{
			object = oIter.next();
			if (object == null)
			{
				oIter.remove();
				continue;
			}
			
			if (!fullCheck && !object.isPlayable())
			{
				continue;
			}
			
			if (!object.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true))
			{
				oIter.remove();
				removeKnownObject(object, true);
			}
		}
	}
	
	public L2Object getActiveObject()
	{
		return _activeObject;
	}
	
	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}
	
	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}
	
	public final Map<Integer, L2Object> getKnownObjects()
	{
		if (_knownObjects == null)
		{
			_knownObjects = new L2FastMap<>(true);
		}
		return _knownObjects;
	}
}