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
package l2e.gameserver.scripting.scriptengine.listeners.player;

import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.scripting.scriptengine.events.AugmentEvent;
import l2e.gameserver.scripting.scriptengine.impl.L2JListener;

public abstract class AugmentListener extends L2JListener
{
	public AugmentListener()
	{
		register();
	}
	
	public abstract boolean onAugment(AugmentEvent event);
	
	public abstract boolean onRemoveAugment(AugmentEvent event);
	
	@Override
	public void register()
	{
		L2ItemInstance.addAugmentListener(this);
	}
	
	@Override
	public void unregister()
	{
		L2ItemInstance.removeAugmentListener(this);
	}
	
}
