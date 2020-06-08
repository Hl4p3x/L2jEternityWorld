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
package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExAskJoinMPCC;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestExAskJoinMPCC extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance player = L2World.getInstance().getPlayer(_name);
		if (player == null)
		{
			return;
		}
		
		if (activeChar.isInParty() && player.isInParty() && activeChar.getParty().equals(player.getParty()))
		{
			return;
		}
		
		SystemMessage sm;
		
		if (activeChar.isInParty())
		{
			L2Party activeParty = activeChar.getParty();
			
			if (activeParty.getLeader().equals(activeChar))
			{
				if (activeParty.isInCommandChannel() && activeParty.getCommandChannel().getLeader().equals(activeChar))
				{
					if (player.isInParty())
					{
						if (player.getParty().isInCommandChannel())
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL);
							sm.addString(player.getName());
							activeChar.sendPacket(sm);
						}
						else
						{
							askJoinMPCC(activeChar, player);
						}
					}
					else
					{
						activeChar.sendMessage(player.getName() + " doesn't have party and cannot be invited to Command Channel.");
					}
					
				}
				else if (activeParty.isInCommandChannel() && !activeParty.getCommandChannel().getLeader().equals(activeChar))
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
					activeChar.sendPacket(sm);
				}
				else
				{
					if (player.isInParty())
					{
						if (player.getParty().isInCommandChannel())
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL);
							sm.addString(player.getName());
							activeChar.sendPacket(sm);
						}
						else
						{
							askJoinMPCC(activeChar, player);
						}
					}
					else
					{
						activeChar.sendMessage(player.getName() + " doesn't have party and cannot be invited to Command Channel.");
					}
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
			}
		}
	}
	
	private void askJoinMPCC(L2PcInstance requestor, L2PcInstance target)
	{
		boolean hasRight = false;
		if (requestor.isClanLeader() && (requestor.getClan().getLevel() >= 5))
		{
			hasRight = true;
		}
		else if (requestor.getInventory().getItemByItemId(8871) != null)
		{
			hasRight = true;
		}
		else if ((requestor.getPledgeClass() >= 5) && (requestor.getKnownSkill(391) != null))
		{
			hasRight = true;
		}
		
		if (!hasRight)
		{
			requestor.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER);
			return;
		}
		
		final L2PcInstance targetLeader = target.getParty().getLeader();
		SystemMessage sm;
		if (!targetLeader.isProcessingRequest())
		{
			requestor.onTransactionRequest(targetLeader);
			sm = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_CONFIRM_FROM_C1);
			sm.addString(requestor.getName());
			targetLeader.sendPacket(sm);
			targetLeader.sendPacket(new ExAskJoinMPCC(requestor.getName()));
			
			requestor.sendMessage("You invited " + targetLeader.getName() + " to your Command Channel.");
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(targetLeader.getName());
			requestor.sendPacket(sm);
		}
	}
}