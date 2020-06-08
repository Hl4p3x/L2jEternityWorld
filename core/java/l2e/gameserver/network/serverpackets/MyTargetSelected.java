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
import l2e.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class MyTargetSelected extends L2GameServerPacket
{
	private final int _objectId;
	private final int _color;
	
	public MyTargetSelected(L2PcInstance player, L2Character target)
	{
		_objectId = (target instanceof L2ControllableAirShipInstance) ? ((L2ControllableAirShipInstance) target).getHelmObjectId() : target.getObjectId();
		_color = target.isAutoAttackable(player) ? (player.getLevel() - target.getLevel()) : 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB9);
		writeD(_objectId);
		writeH(_color);
		writeD(0x00);
	}
}