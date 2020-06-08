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
package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.xml.AdminParser;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.L2AccessLevel;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2SiegeClan;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.entity.events.phoenix.Interface;
import l2e.gameserver.model.restriction.GlobalRestrictions;

public class Die extends L2GameServerPacket
{
	private int _charObjId;
	private boolean _canTeleport;
	private boolean _sweepable;
	private L2AccessLevel _access = AdminParser.getInstance().getAccessLevel(0);
	private boolean _haveItem = false;
	private L2Clan _clan;
	private final L2Character _activeChar;
	private boolean _isJailed;
	
	public Die(L2Character cha)
	{
		_charObjId = cha.getObjectId();
		_activeChar = cha;
		if (cha.isPlayer())
		{
			final L2PcInstance player = cha.getActingPlayer();
			_access = player.getAccessLevel();
			_clan = player.getClan();
			_haveItem = player.getInventory().getItemByItemId(13300) != null;
			_isJailed = player.isJailed();
			
			if (!GlobalRestrictions.canRequestRevive(player))
			{
				return;
			}
			
			if (Interface.isParticipating(cha.getObjectId()))
			{
				_canTeleport = false;
			}
		}
		_canTeleport = cha.canRevive() && !cha.isPendingRevive();
		_sweepable = cha.isL2Attackable() && cha.isSweepActive();
	}
	
	@Override
	protected final void writeImpl()
	{
		
		writeC(0x00);
		
		writeD(_charObjId);
		writeD(_canTeleport ? 0x01 : 0);
		if (_canTeleport && (_clan != null) && !_isJailed)
		{
			boolean isInCastleDefense = false;
			boolean isInFortDefense = false;
			
			L2SiegeClan siegeClan = null;
			Castle castle = CastleManager.getInstance().getCastle(_activeChar);
			Fort fort = FortManager.getInstance().getFort(_activeChar);
			SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(_activeChar);
			if ((castle != null) && castle.getSiege().getIsInProgress())
			{
				siegeClan = castle.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && castle.getSiege().checkIsDefender(_clan))
				{
					isInCastleDefense = true;
				}
			}
			else if ((fort != null) && fort.getSiege().getIsInProgress())
			{
				siegeClan = fort.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && fort.getSiege().checkIsDefender(_clan))
				{
					isInFortDefense = true;
				}
			}
			
			writeD(_clan.getHideoutId() > 0 ? 0x01 : 0x00);
			writeD((_clan.getCastleId() > 0) || isInCastleDefense ? 0x01 : 0x00);
			writeD((TerritoryWarManager.getInstance().getFlagForClan(_clan) != null) || ((siegeClan != null) && !isInCastleDefense && !isInFortDefense && !siegeClan.getFlag().isEmpty()) || ((hall != null) && hall.getSiege().checkIsAttacker(_clan)) ? 0x01 : 0x00);
			writeD(_sweepable ? 0x01 : 0x00);
			writeD(_access.allowFixedRes() || _haveItem ? 0x01 : 0x00);
			writeD((_clan.getFortId() > 0) || isInFortDefense ? 0x01 : 0x00);
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(_sweepable ? 0x01 : 0x00);
			writeD(_access.allowFixedRes() || _haveItem ? 0x01 : 0x00);
			writeD(0x00);
		}
	}
}