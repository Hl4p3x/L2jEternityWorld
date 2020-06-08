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

import l2e.gameserver.model.actor.L2Character;

public class ExFishingHpRegen extends L2GameServerPacket
{
	private final L2Character _activeChar;
	private final int _time, _fishHP, _hpMode, _anim, _goodUse, _penalty, _hpBarColor;
	
	public ExFishingHpRegen(L2Character character, int time, int fishHP, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor)
	{
		_activeChar = character;
		_time = time;
		_fishHP = fishHP;
		_hpMode = HPmode;
		_goodUse = GoodUse;
		_anim = anim;
		_penalty = penalty;
		_hpBarColor = hpBarColor;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x28);
		
		writeD(_activeChar.getObjectId());
		writeD(_time);
		writeD(_fishHP);
		writeC(_hpMode);
		writeC(_goodUse);
		writeC(_anim);
		writeD(_penalty);
		writeC(_hpBarColor);	
	}
}