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

import java.util.Collection;

import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.model.ClanInfo;
import l2e.gameserver.model.L2Clan;

public class AllianceInfo extends L2GameServerPacket
{
	private final String _name;
	private final int _total;
	private final int _online;
	private final String _leaderC;
	private final String _leaderP;
	private final ClanInfo[] _allies;
	
	public AllianceInfo(int allianceId)
	{
		final L2Clan leader = ClanHolder.getInstance().getClan(allianceId);
		_name = leader.getAllyName();
		_leaderC = leader.getName();
		_leaderP = leader.getLeaderName();
		
		final Collection<L2Clan> allies = ClanHolder.getInstance().getClanAllies(allianceId);
		_allies = new ClanInfo[allies.size()];
		int idx = 0, total = 0, online = 0;
		for (final L2Clan clan : allies)
		{
			final ClanInfo ci = new ClanInfo(clan);
			_allies[idx++] = ci;
			total += ci.getTotal();
			online += ci.getOnline();
		}
		
		_total = total;
		_online = online;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xB5);
		
		writeS(_name);
		writeD(_total);
		writeD(_online);
		writeS(_leaderC);
		writeS(_leaderP);
		
		writeD(_allies.length);
		for (final ClanInfo aci : _allies)
		{
			writeS(aci.getClan().getName());
			writeD(0x00);
			writeD(aci.getClan().getLevel());
			writeS(aci.getClan().getLeaderName());
			writeD(aci.getTotal());
			writeD(aci.getOnline());
		}
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getTotal()
	{
		return _total;
	}
	
	public int getOnline()
	{
		return _online;
	}
	
	public String getLeaderC()
	{
		return _leaderC;
	}
	
	public String getLeaderP()
	{
		return _leaderP;
	}
	
	public ClanInfo[] getAllies()
	{
		return _allies;
	}
}