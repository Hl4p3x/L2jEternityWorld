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

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
	private final int _skillId;
	private final int _skillLvl;
	private final int _duration;
	
	public ShortBuffStatusUpdate(int skillId, int skillLvl, int duration)
	{
		_skillId = skillId;
		_skillLvl = skillLvl;
		_duration = duration;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfa);
		writeD(_skillId);
		writeD(_skillLvl);
		writeD(_duration);
	}
}