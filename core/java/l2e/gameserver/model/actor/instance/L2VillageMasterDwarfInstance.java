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
package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.Race;

public final class L2VillageMasterDwarfInstance extends L2VillageMasterInstance
{
	public L2VillageMasterDwarfInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected final boolean checkVillageMasterRace(PlayerClass pclass)
	{
		if (pclass == null)
			return false;
		
		return pclass.isOfRace(Race.Dwarf);
	}
}