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
package l2e.gameserver.model.olympiad;

import l2e.gameserver.model.L2World;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author DS, Zoey76
 */
public final class Participant
{
	private final int objectId;
	private L2PcInstance player;
	private final String name;
	private final int side;
	private final int baseClass;
	private boolean disconnected = false;
	private boolean defaulted = false;
	private final StatsSet stats;
	public String clanName;
	public int clanId;
	
	public Participant(L2PcInstance plr, int olympiadSide)
	{
		objectId = plr.getObjectId();
		player = plr;
		name = plr.getName();
		side = olympiadSide;
		baseClass = plr.getBaseClass();
		stats = Olympiad.getNobleStats(getObjectId());
		clanName = plr.getClan() != null ? plr.getClan().getName() : "";
		clanId = plr.getClanId();
	}
	
	public Participant(int objId, int olympiadSide)
	{
		objectId = objId;
		player = null;
		name = "-";
		side = olympiadSide;
		baseClass = 0;
		stats = null;
		clanName = "";
		clanId = 0;
	}
	
	public final boolean updatePlayer()
	{
		if ((player == null) || !player.isOnline())
		{
			player = L2World.getInstance().getPlayer(getObjectId());
		}
		return (player != null);
	}
	
	public final void updateStat(String statName, int increment)
	{
		stats.set(statName, Math.max(stats.getInteger(statName) + increment, 0));
	}

	public String getName()
	{
		return name;
	}
	
	public L2PcInstance getPlayer()
	{
		return player;
	}

	public int getObjectId()
	{
		return objectId;
	}
	public StatsSet getStats()
	{
		return stats;
	}

	public void setPlayer(L2PcInstance noble)
	{
		player = noble;
	}

	public int getSide()
	{
		return side;
	}
	
	public int getBaseClass()
	{
		return baseClass;
	}
	
	public boolean isDisconnected()
	{
		return disconnected;
	}
	
	public void setDisconnected(boolean val)
	{
		disconnected = val;
	}
	
	public boolean isDefaulted()
	{
		return defaulted;
	}
	
	public void setDefaulted(boolean val)
	{
		defaulted = val;
	}

	public String getClanName()
	{
		return clanName;
	}
	
	public int getClanId()
	{
		return clanId;
	}
}