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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import l2e.gameserver.model.Hit;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;

public class Attack extends L2GameServerPacket
{
	private final int _attackerObjId;
	private final boolean _soulshot;
	private final int _ssGrade;
	private final Location _attackerLoc;
	private final Location _targetLoc;
	private final List<Hit> _hits = new ArrayList<>();
	
	public Attack(L2Character attacker, L2Character target, boolean useShots, int ssGrade)
	{
		_attackerObjId = attacker.getObjectId();
		_soulshot = useShots;
		_ssGrade = ssGrade;
		_attackerLoc = new Location(attacker);
		_targetLoc = new Location(target);
	}
	
	public void addHit(L2Character target, int damage, boolean miss, boolean crit, byte shld)
	{
		_hits.add(new Hit(target, damage, miss, crit, shld, _soulshot, _ssGrade));
	}
	
	public boolean hasHits()
	{
		return !_hits.isEmpty();
	}
	
	public boolean hasSoulshot()
	{
		return _soulshot;
	}
	
	private void writeHit(Hit hit)
	{
		writeD(hit.getTargetId());
		writeD(hit.getDamage());
		writeC(hit.getFlags());
	}
	
	@Override
	protected final void writeImpl()
	{
		final Iterator<Hit> it = _hits.iterator();
		writeC(0x33);
		
		writeD(_attackerObjId);
		writeHit(it.next());
		writeLoc(_attackerLoc);
		
		writeH(_hits.size() - 1);
		while (it.hasNext())
		{
			writeHit(it.next());
		}
		writeLoc(_targetLoc);
	}
}