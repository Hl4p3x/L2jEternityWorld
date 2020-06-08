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
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.GMHennaInfo;
import l2e.gameserver.network.serverpackets.GMViewCharacterInfo;
import l2e.gameserver.network.serverpackets.GMViewItemList;
import l2e.gameserver.network.serverpackets.GMViewPledgeInfo;
import l2e.gameserver.network.serverpackets.GMViewSkillInfo;
import l2e.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;
import l2e.gameserver.network.serverpackets.GmViewQuestInfo;

public final class RequestGMCommand extends L2GameClientPacket
{
	private String _targetName;
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!getClient().getActiveChar().isGM() || !getClient().getActiveChar().getAccessLevel().allowAltG())
		{
			return;
		}
		
		L2PcInstance player = L2World.getInstance().getPlayer(_targetName);
		
		L2Clan clan = ClanHolder.getInstance().getClanByName(_targetName);
		
		if ((player == null) && ((clan == null) || (_command != 6)))
		{
			return;
		}
		
		switch (_command)
		{
			case 1:
			{
				sendPacket(new GMViewCharacterInfo(player));
				sendPacket(new GMHennaInfo(player));
				break;
			}
			case 2:
			{
				if ((player != null) && (player.getClan() != null))
				{
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				break;
			}
			case 3:
			{
				sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4:
			{
				sendPacket(new GmViewQuestInfo(player));
				break;
			}
			case 5:
			{
				sendPacket(new GMViewItemList(player));
				sendPacket(new GMHennaInfo(player));
				break;
			}
			case 6:
			{
				if (player != null)
				{
					sendPacket(new GMViewWarehouseWithdrawList(player));
				}
				else
				{
					sendPacket(new GMViewWarehouseWithdrawList(clan));
				}
				break;
			}
		}
	}
}