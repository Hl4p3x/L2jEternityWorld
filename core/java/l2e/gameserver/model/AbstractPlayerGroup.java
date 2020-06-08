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

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.interfaces.IL2Procedure;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Rnd;

public abstract class AbstractPlayerGroup
{
	public abstract List<L2PcInstance> getMembers();
	
	public List<Integer> getMembersObjectId()
	{
		final List<Integer> ids = new ArrayList<>();
		forEachMember(new IL2Procedure<L2PcInstance>()
		{
			
			@Override
			public boolean execute(L2PcInstance member)
			{
				ids.add(member.getObjectId());
				return true;
			}
		});
		return ids;
	}
	
	public abstract L2PcInstance getLeader();

	public abstract void setLeader(L2PcInstance leader);
	
	public int getLeaderObjectId()
	{
		return getLeader().getObjectId();
	}

	public boolean isLeader(L2PcInstance player)
	{
		return (getLeaderObjectId() == player.getObjectId());
	}
	
	public int getMemberCount()
	{
		return getMembers().size();
	}
	
	public abstract int getLevel();
	
	public void broadcastPacket(final L2GameServerPacket packet)
	{
		forEachMember(new IL2Procedure<L2PcInstance>()
		{
			
			@Override
			public boolean execute(L2PcInstance member)
			{
				if (member != null)
				{
					member.sendPacket(packet);
				}
				return true;
			}
		});
	}
	
	public void broadcastMessage(SystemMessageId message)
	{
		broadcastPacket(SystemMessage.getSystemMessage(message));
	}
	
	public void broadcastString(String text)
	{
		broadcastPacket(SystemMessage.sendString(text));
	}
	
	public void broadcastCreatureSay(final CreatureSay msg, final L2PcInstance broadcaster)
	{
		forEachMember(new IL2Procedure<L2PcInstance>()
		{
			
			@Override
			public boolean execute(L2PcInstance member)
			{
				if ((member != null) && !BlockList.isBlocked(member, broadcaster))
				{
					member.sendPacket(msg);
				}
				return true;
			}
		});
	}
	
	public boolean containsPlayer(L2PcInstance player)
	{
		return getMembers().contains(player);
	}
	
	public L2PcInstance getRandomPlayer()
	{
		return getMembers().get(Rnd.get(getMemberCount()));
	}
	
	public boolean forEachMember(IL2Procedure<L2PcInstance> procedure)
	{
		for (L2PcInstance player : getMembers())
		{
			if (!procedure.execute(player))
			{
				return false;
			}
		}
		return true;
	}
}