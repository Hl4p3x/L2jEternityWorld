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

import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2ClanMember;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;
import gnu.trove.procedure.TObjectProcedure;

public final class RequestStopPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;
	
	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		L2Clan playerClan = player.getClan();
		if (playerClan == null)
		{
			return;
		}
		
		L2Clan clan = ClanHolder.getInstance().getClanByName(_pledgeName);
		
		if (clan == null)
		{
			player.sendMessage("No such clan.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!playerClan.isAtWarWith(clan.getId()))
		{
			player.sendMessage("You aren't at war with this clan.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((player.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) != L2Clan.CP_CL_PLEDGE_WAR)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		for (L2ClanMember member : playerClan.getMembers())
		{
			if ((member == null) || (member.getPlayerInstance() == null))
			{
				continue;
			}
			
			if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(member.getPlayerInstance()))
			{
				player.sendPacket(SystemMessageId.CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT);
				return;
			}
		}
		
		ClanHolder.getInstance().deleteclanswars(playerClan.getId(), clan.getId());
		L2World.getInstance().forEachPlayer(new ForEachPlayerBroadcastUserInfo(clan, player));
	}
	
	private final class ForEachPlayerBroadcastUserInfo implements TObjectProcedure<L2PcInstance>
	{
		L2PcInstance _player;
		L2Clan _cln;
		
		protected ForEachPlayerBroadcastUserInfo(L2Clan clan, L2PcInstance player)
		{
			_cln = clan;
			_player = player;
		}
		
		@Override
		public final boolean execute(final L2PcInstance cha)
		{
			if ((cha.getClan() == _player.getClan()) || (cha.getClan() == _cln))
			{
				cha.broadcastUserInfo();
			}
			return true;
		}
	}
}