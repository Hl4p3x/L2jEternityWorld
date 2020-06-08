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
package l2e.gameserver.handler;

import java.util.logging.Logger;

import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public interface IItemHandler
{
	public static final Logger _log = Logger.getLogger(IItemHandler.class.getName());
	
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse);
}