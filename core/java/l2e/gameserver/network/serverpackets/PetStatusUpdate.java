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

import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.actor.instance.L2ServitorInstance;

public class PetStatusUpdate extends L2GameServerPacket
{
	private final L2Summon _summon;
	private final int _maxHp, _maxMp;
	private int _maxFed, _curFed;
	
	public PetStatusUpdate(L2Summon summon)
	{
		_summon = summon;
		_maxHp = _summon.getMaxHp();
		_maxMp = _summon.getMaxMp();
		if (_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance)_summon;
			_curFed = pet.getCurrentFed();
			_maxFed = pet.getMaxFed();
		}
		else if (_summon instanceof L2ServitorInstance)
		{
			L2ServitorInstance sum = (L2ServitorInstance)_summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB6);
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getX());
		writeD(_summon.getY());
		writeD(_summon.getZ());
		writeS("");
		writeD(_curFed);
		writeD(_maxFed);
		writeD((int) _summon.getCurrentHp());
		writeD(_maxHp);
		writeD((int) _summon.getCurrentMp());
		writeD(_maxMp);
		writeD(_summon.getLevel());
		writeQ(_summon.getStat().getExp());
		writeQ(_summon.getExpForThisLevel());
		writeQ(_summon.getExpForNextLevel());
	}
}