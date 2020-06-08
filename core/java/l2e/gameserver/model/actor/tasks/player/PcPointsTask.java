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
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Rnd;

public class PcPointsTask implements Runnable
{
	private final L2PcInstance _player;
	
	public PcPointsTask(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if ((_player.getLevel() > Config.PC_BANG_MIN_LEVEL) && _player.isOnline())
		{
			
			if (_player.getPcBangPoints() >= Config.MAX_PC_BANG_POINTS)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED);
				_player.sendPacket(sm);
				return;
			}
			
			int _points = Rnd.get(Config.PC_BANG_POINTS_MIN, Config.PC_BANG_POINTS_MAX);
			
			boolean doublepoint = false;
			SystemMessage sm = null;
			if (_points > 0)
			{
				if (Config.ENABLE_DOUBLE_PC_BANG_POINTS && (Rnd.get(100) < Config.DOUBLE_PC_BANG_POINTS_CHANCE))
				{
					_points *= 2;
					sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE);
					_player.broadcastPacket(new MagicSkillUse(_player, _player, 2023, 1, 100, 0));
					doublepoint = true;
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
				}
				
				if ((_player.getPcBangPoints() + _points) > Config.MAX_PC_BANG_POINTS)
				{
					_points = Config.MAX_PC_BANG_POINTS - _player.getPcBangPoints();
				}
				sm.addNumber(_points);
				_player.sendPacket(sm);
				
				if (Config.PC_POINT_ID < 0)
				{
					_player.setPcBangPoints(_player.getPcBangPoints() + _points);
				}
				else
				{
					_player.setPcBangPoints(_player.getPcBangPoints() + _points);
					_player.addItem("PcPoints", Config.PC_POINT_ID, _points, _player, true);
				}
				_player.sendPacket(new ExPCCafePointInfo(_player.getPcBangPoints(), _points, true, doublepoint, 1));
			}
		}
	}
}