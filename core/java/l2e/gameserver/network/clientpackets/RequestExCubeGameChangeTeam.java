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
package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public final class RequestExCubeGameChangeTeam extends L2GameClientPacket
{
	int _arena;
	int _team;
	
	@Override
	protected void readImpl()
	{
		_arena = readD() + 1;
		_team = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(_arena))
		{
			return;
		}
		L2PcInstance player = getClient().getActiveChar();
		
		switch (_team)
		{
			case 0:
			case 1:
				HandysBlockCheckerManager.getInstance().changePlayerToTeam(player, _arena, _team);
				break;
			case -1:
			{
				int team = HandysBlockCheckerManager.getInstance().getHolder(_arena).getPlayerTeam(player);
				
				if (team > -1)
				{
					HandysBlockCheckerManager.getInstance().removePlayer(player, _arena, team);
				}
				break;
			}
			default:
				_log.warning("Wrong Cube Game Team ID: " + _team);
				break;
		}
	}
}