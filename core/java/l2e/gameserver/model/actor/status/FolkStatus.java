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
package l2e.gameserver.model.actor.status;

import l2e.Config;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import l2e.gameserver.model.actor.instance.L2NpcInstance;

public class FolkStatus extends NpcStatus
{
	public FolkStatus(L2Npc activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	@Override
	public final void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
	}
	
	@Override
	public final void reduceMp(double value)
	{
		if (Config.CH_BUFF_FREE && (getActiveChar() instanceof L2ClanHallManagerInstance))
		{
			return;
		}
		super.reduceMp(value);
	}
	
	@Override
	public L2NpcInstance getActiveChar()
	{
		return (L2NpcInstance) super.getActiveChar();
	}
}