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
package l2e.gameserver.model.entity.events.phoenix.container;

import javolution.util.FastMap;
import l2e.gameserver.model.entity.events.phoenix.model.EventNpc;

public class NpcContainer
{
	private static class SingletonHolder
	{
		protected static final NpcContainer _instance = new NpcContainer();
	}
	
	public static final NpcContainer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final FastMap<Integer, EventNpc> npcs;
	
	public NpcContainer()
	{
		npcs = new FastMap<>();
	}
	
	public EventNpc createNpc(int x, int y, int z, int npcId, int instance)
	{
		EventNpc npci = new EventNpc(x, y, z, npcId, instance);
		npcs.put(npci.getId(), npci);
		return npci;
	}
	
	public void deleteNpc(EventNpc npc)
	{
		npcs.remove(npc.getId());
	}
	
	public EventNpc getNpc(Integer id)
	{
		return npcs.get(id);
	}
}