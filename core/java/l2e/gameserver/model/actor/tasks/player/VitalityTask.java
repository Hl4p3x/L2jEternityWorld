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
package l2e.gameserver.model.actor.tasks.player;

import l2e.Config;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.stat.PcStat;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.serverpackets.ExVitalityPointInfo;

public class VitalityTask implements Runnable
{
	private final L2PcInstance _player;
	
	public VitalityTask(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if (!_player.isInsideZone(ZoneId.PEACE))
		{
			return;
		}
		
		if (_player.getVitalityPoints() >= PcStat.MAX_VITALITY_POINTS)
		{
			return;
		}
		
		_player.updateVitalityPoints(Config.RATE_RECOVERY_VITALITY_PEACE_ZONE, false, false);
		_player.sendPacket(new ExVitalityPointInfo(_player.getVitalityPoints()));
	}
}