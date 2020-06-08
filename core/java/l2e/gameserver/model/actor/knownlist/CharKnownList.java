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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.util.Util;
import l2e.util.L2FastMap;

public class CharKnownList extends ObjectKnownList
{
	private Map<Integer, L2PcInstance> _knownPlayers;
	private Map<Integer, L2Summon> _knownSummons;
	private Map<Integer, Integer> _knownRelations;
	
	public CharKnownList(L2Character activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}
		else if (object.isPlayer())
		{
			getKnownPlayers().put(object.getObjectId(), object.getActingPlayer());
			getKnownRelations().put(object.getObjectId(), -1);
		}
		else if (object.isSummon())
		{
			getKnownSummons().put(object.getObjectId(), (L2Summon) object);
		}
		return true;
	}
	
	public final boolean knowsThePlayer(L2PcInstance player)
	{
		return (getActiveChar() == player) || getKnownPlayers().containsKey(player.getObjectId());
	}
	
	@Override
	public final void removeAllKnownObjects()
	{
		super.removeAllKnownObjects();
		getKnownPlayers().clear();
		getKnownRelations().clear();
		getKnownSummons().clear();
		getActiveChar().setTarget(null);
		
		if (getActiveChar().hasAI())
		{
			getActiveChar().setAI(null);
		}
	}
	
	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (!super.removeKnownObject(object, forget))
		{
			return false;
		}
		
		if (!forget)
		{
			if (object.isPlayer())
			{
				getKnownPlayers().remove(object.getObjectId());
				getKnownRelations().remove(object.getObjectId());
			}
			else if (object.isSummon())
			{
				getKnownSummons().remove(object.getObjectId());
			}
		}
		
		if (object == getActiveChar().getTarget())
		{
			getActiveChar().setTarget(null);
		}
		
		return true;
	}
	
	@Override
	public void forgetObjects(boolean fullCheck)
	{
		if (!fullCheck)
		{
			final Collection<L2PcInstance> plrs = getKnownPlayers().values();
			final Iterator<L2PcInstance> pIter = plrs.iterator();
			L2PcInstance player;
			while (pIter.hasNext())
			{
				player = pIter.next();
				if (player == null)
				{
					pIter.remove();
				}
				else if (!player.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(player), getActiveObject(), player, true))
				{
					pIter.remove();
					removeKnownObject(player, true);
					getKnownRelations().remove(player.getObjectId());
					getKnownObjects().remove(player.getObjectId());
				}
			}
			
			final Collection<L2Summon> sums = getKnownSummons().values();
			final Iterator<L2Summon> sIter = sums.iterator();
			L2Summon summon;
			
			while (sIter.hasNext())
			{
				summon = sIter.next();
				if (summon == null)
				{
					sIter.remove();
				}
				else if (!summon.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(summon), getActiveObject(), summon, true))
				{
					sIter.remove();
					removeKnownObject(summon, true);
					getKnownObjects().remove(summon.getObjectId());
				}
			}
			
			return;
		}
		
		final Collection<L2Object> objs = getKnownObjects().values();
		final Iterator<L2Object> oIter = objs.iterator();
		L2Object object;
		while (oIter.hasNext())
		{
			object = oIter.next();
			if (object == null)
			{
				oIter.remove();
			}
			else if (!object.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true))
			{
				oIter.remove();
				removeKnownObject(object, true);
				
				if (object.isPlayer())
				{
					getKnownPlayers().remove(object.getObjectId());
					getKnownRelations().remove(object.getObjectId());
				}
				else if (object.isSummon())
				{
					getKnownSummons().remove(object.getObjectId());
				}
			}
		}
	}
	
	public L2Character getActiveChar()
	{
		return (L2Character) super.getActiveObject();
	}
	
	public Collection<L2Character> getKnownCharacters()
	{
		FastList<L2Character> result = new FastList<>();
		
		final Collection<L2Object> objs = getKnownObjects().values();
		for (L2Object obj : objs)
		{
			if (obj instanceof L2Character)
			{
				result.add((L2Character) obj);
			}
		}
		return result;
	}
	
	public Collection<L2Character> getKnownCharactersInRadius(long radius)
	{
		List<L2Character> result = new ArrayList<>();
		
		final Collection<L2Object> objs = getKnownObjects().values();
		
		for (L2Object obj : objs)
		{
			if (obj instanceof L2Character)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((L2Character) obj);
				}
			}
		}
		return result;
	}
	
	public Collection<L2MonsterInstance> getKnownMonstersInRadius(long radius)
	{
		FastList<L2MonsterInstance> result = new FastList<>();
		
		final Collection<L2Object> objs = getKnownObjects().values();
		
		for (L2Object obj : objs)
		{
			if (obj instanceof L2MonsterInstance)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((L2MonsterInstance) obj);
				}
			}
		}
		return result;
	}
	
	public final Map<Integer, L2PcInstance> getKnownPlayers()
	{
		if (_knownPlayers == null)
		{
			_knownPlayers = new L2FastMap<>(true);
		}
		return _knownPlayers;
	}
	
	public final Map<Integer, Integer> getKnownRelations()
	{
		if (_knownRelations == null)
		{
			_knownRelations = new L2FastMap<>(true);
		}
		return _knownRelations;
	}
	
	public final Map<Integer, L2Summon> getKnownSummons()
	{
		if (_knownSummons == null)
		{
			_knownSummons = new L2FastMap<>(true);
		}
		return _knownSummons;
	}
	
	public final Collection<L2PcInstance> getKnownPlayersInRadius(long radius)
	{
		List<L2PcInstance> result = new ArrayList<>();
		
		final Collection<L2PcInstance> plrs = getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (Util.checkIfInRange((int) radius, getActiveChar(), player, true))
			{
				result.add(player);
			}
		}
		return result;
	}
	
	public Collection<L2Npc> getKnownNpcInRadius(long radius)
	{
		List<L2Npc> result = new ArrayList<>();
		
		final Collection<L2Object> objs = getKnownObjects().values();
		
		for (L2Object obj : objs)
		{
			if (obj instanceof L2NpcInstance)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((L2NpcInstance) obj);
				}
			}
		}
		return result;
	}
	
	public Collection<L2ItemInstance> getKnownObjectInRadius(long radius)
	{
		List<L2ItemInstance> result = new ArrayList<>();
		
		final Collection<L2Object> objs = getKnownObjects().values();
		
		for (L2Object obj : objs)
		{
			if (obj instanceof L2ItemInstance)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((L2ItemInstance) obj);
				}
			}
		}
		return result;
	}
}