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

public class ChangeMoveType extends L2GameServerPacket
{
	public static final int WALK = 0;
	public static final int RUN = 1;
	
	private final int _charObjId;
	private final boolean _running;
	
	public ChangeMoveType(L2Character character)
	{
		_charObjId = character.getObjectId();
		_running = character.isRunning();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x28);
		writeD(_charObjId);
		writeD(_running ? RUN : WALK);
		writeD(0);
	}
}