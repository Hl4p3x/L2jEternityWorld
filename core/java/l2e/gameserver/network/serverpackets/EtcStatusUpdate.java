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

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.EffectFlag;
import l2e.gameserver.model.zone.ZoneId;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	
	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xf9);
		writeD(_activeChar.getCharges());
		writeD(_activeChar.getWeightPenalty());
		writeD((_activeChar.getMessageRefusal() || _activeChar.isChatBanned() || _activeChar.isSilenceMode()) ? 1 : 0);
		writeD(_activeChar.isInsideZone(ZoneId.DANGER_AREA) ? 1 : 0);
		writeD(_activeChar.getExpertiseWeaponPenalty());
		writeD(_activeChar.getExpertiseArmorPenalty());
		writeD(_activeChar.isAffected(EffectFlag.CHARM_OF_COURAGE) ? 1 : 0);
		writeD(_activeChar.getDeathPenaltyBuffLevel());
		writeD(_activeChar.getChargedSouls());
	}
}