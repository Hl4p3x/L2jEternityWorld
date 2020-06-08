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

import java.util.List;

import javolution.util.FastList;

import l2e.Config;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.interfaces.IL2Procedure;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExCloseMPCC;
import l2e.gameserver.network.serverpackets.ExMPCCPartyInfoUpdate;
import l2e.gameserver.network.serverpackets.ExOpenMPCC;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class L2CommandChannel extends AbstractPlayerGroup
{
	private final List<L2Party> _parties;
	private L2PcInstance _commandLeader = null;
	private int _channelLvl;
	
	public L2CommandChannel(L2PcInstance leader)
	{
		_commandLeader = leader;
		L2Party party = leader.getParty();
		_parties = new FastList<L2Party>().shared();
		_parties.add(party);
		_channelLvl = party.getLevel();
		party.setCommandChannel(this);
		party.broadcastMessage(SystemMessageId.COMMAND_CHANNEL_FORMED);
		party.broadcastPacket(ExOpenMPCC.STATIC_PACKET);
	}
	
	public void addParty(L2Party party)
	{
		if (party == null)
		{
			return;
		}
		broadcastPacket(new ExMPCCPartyInfoUpdate(party, 1));
		
		_parties.add(party);
		if (party.getLevel() > _channelLvl)
		{
			_channelLvl = party.getLevel();
		}
		party.setCommandChannel(this);
		party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
		party.broadcastPacket(ExOpenMPCC.STATIC_PACKET);
	}
	
	public void removeParty(L2Party party)
	{
		if (party == null)
		{
			return;
		}
		
		_parties.remove(party);
		_channelLvl = 0;
		for (L2Party pty : _parties)
		{
			if (pty.getLevel() > _channelLvl)
			{
				_channelLvl = pty.getLevel();
			}
		}
		party.setCommandChannel(null);
		party.broadcastPacket(new ExCloseMPCC());
		if (_parties.size() < 2)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));
			disbandChannel();
		}
		else
		{
			broadcastPacket(new ExMPCCPartyInfoUpdate(party, 0));
		}
	}
	
	public void disbandChannel()
	{
		if (_parties != null)
		{
			for (L2Party party : _parties)
			{
				if (party != null)
				{
					removeParty(party);
				}
			}
			_parties.clear();
		}
	}
	
	@Override
	public int getMemberCount()
	{
		int count = 0;
		for (L2Party party : _parties)
		{
			if (party != null)
			{
				count += party.getMemberCount();
			}
		}
		return count;
	}
	
	public List<L2Party> getPartys()
	{
		return _parties;
	}
	
	@Override
	public List<L2PcInstance> getMembers()
	{
		List<L2PcInstance> members = new FastList<L2PcInstance>().shared();
		for (L2Party party : getPartys())
		{
			members.addAll(party.getMembers());
		}
		return members;
	}
	
	@Override
	public int getLevel()
	{
		return _channelLvl;
	}
	
	@Override
	public void setLeader(L2PcInstance leader)
	{
		_commandLeader = leader;
		if (leader.getLevel() > _channelLvl)
		{
			_channelLvl = leader.getLevel();
		}
	}
	
	public boolean meetRaidWarCondition(L2Object obj)
	{
		if (!((obj instanceof L2Character) && ((L2Character) obj).isRaid()))
		{
			return false;
		}
		return (getMemberCount() >= Config.LOOT_RAIDS_PRIVILEGE_CC_SIZE);
	}
	
	@Override
	public L2PcInstance getLeader()
	{
		return _commandLeader;
	}
	
	@Override
	public boolean containsPlayer(L2PcInstance player)
	{
		if ((_parties != null) && !_parties.isEmpty())
		{
			for (L2Party party : _parties)
			{
				if (party.containsPlayer(player))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean forEachMember(IL2Procedure<L2PcInstance> procedure)
	{
		if ((_parties != null) && !_parties.isEmpty())
		{
			for (L2Party party : _parties)
			{
				if (!party.forEachMember(procedure))
				{
					return false;
				}
			}
		}
		return true;
	}

	public boolean equals(L2CommandChannel cc)
	{
		return (cc != null) && (getLeaderObjectId() == cc.getLeaderObjectId());
	}
}