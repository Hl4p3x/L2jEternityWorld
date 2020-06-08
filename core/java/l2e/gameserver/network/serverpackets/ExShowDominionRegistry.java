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

import java.util.Calendar;

import javolution.util.FastList;

import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.TerritoryWarManager.Territory;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class ExShowDominionRegistry extends L2GameServerPacket
{
	private static final int MINID = 80;
	private final int _castleId;
	private int _clanReq = 0x00;
	private int _mercReq = 0x00;
	private int _isMercRegistered = 0x00;
	private int _isClanRegistered = 0x00;
	private int _warTime = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
	private final int _currentTime = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
	
	public ExShowDominionRegistry(int castleId, L2PcInstance player)
	{
		_castleId = castleId;
		if (TerritoryWarManager.getInstance().getRegisteredClans(castleId) != null)
		{
			_clanReq = TerritoryWarManager.getInstance().getRegisteredClans(castleId).size();
			if (player.getClan() != null)
				_isClanRegistered = (TerritoryWarManager.getInstance().getRegisteredClans(castleId).contains(player.getClan()) ? 0x01 : 0x00);
		}
		if (TerritoryWarManager.getInstance().getRegisteredMercenaries(castleId) != null)
		{
			_mercReq = TerritoryWarManager.getInstance().getRegisteredMercenaries(castleId).size();
			_isMercRegistered = (TerritoryWarManager.getInstance().getRegisteredMercenaries(castleId).contains(player.getObjectId()) ? 0x01 : 0x00);
		}
		_warTime = (int) (TerritoryWarManager.getInstance().getTWStartTimeInMillis() / 1000);
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x90);
		writeD(MINID + _castleId);
		if (TerritoryWarManager.getInstance().getTerritory(_castleId) == null)
		{
			writeS("No Owner");
			writeS("No Owner");
			writeS("No Ally");
		}
		else
		{
			L2Clan clan = TerritoryWarManager.getInstance().getTerritory(_castleId).getOwnerClan();
			if (clan == null)
			{
				writeS("No Owner");
				writeS("No Owner");
				writeS("No Ally");
			}
			else
			{
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeS(clan.getAllyName());
			}
		}
		writeD(_clanReq);
		writeD(_mercReq);
		writeD(_warTime);
		writeD(_currentTime);
		writeD(_isClanRegistered);
		writeD(_isMercRegistered);
		writeD(0x01);
		FastList<Territory> territoryList = TerritoryWarManager.getInstance().getAllTerritories();
		writeD(territoryList.size());
		for (Territory t : territoryList)
		{
			writeD(t.getTerritoryId());
			writeD(t.getOwnedWardIds().size());
			for (int i : t.getOwnedWardIds())
				writeD(i);
		}
	}
}