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

import l2e.Config;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;

public final class AllyLeave extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		if (player.getClan() == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			return;
		}
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_WITHDRAW_ALLY);
			return;
		}
		L2Clan clan = player.getClan();
		if (clan.getAllyId() == 0)
		{
			player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		if (clan.getId() == clan.getAllyId())
		{
			player.sendPacket(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW);
			return;
		}
		
		long currentTime = System.currentTimeMillis();
		clan.setAllyId(0);
		clan.setAllyName(null);
		clan.changeAllyCrest(0, true);
		clan.setAllyPenaltyExpiryTime(currentTime + (Config.ALT_ALLY_JOIN_DAYS_WHEN_LEAVED * 86400000L), L2Clan.PENALTY_TYPE_CLAN_LEAVED); // 24*60*60*1000 = 86400000
		clan.updateClanInDB();
		
		player.sendPacket(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_ALLIANCE);
	}
}