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
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class RequestPartyLootModification extends L2GameClientPacket
{
	private byte _mode;
	
	@Override
	protected void readImpl()
	{
		_mode = (byte) readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if ((_mode < 0) || (_mode > L2Party.ITEM_ORDER_SPOIL))
		{
			return;
		}
		L2Party party = activeChar.getParty();
		if ((party == null) || !party.isLeader(activeChar) || (_mode == party.getLootDistribution()))
		{
			return;
		}
		party.requestLootChange(_mode);
	}
}