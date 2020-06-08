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
package l2e.gameserver.model.entity.events;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Duel;
import l2e.util.Rnd;

public class TvTEventTeleporter implements Runnable
{
	private L2PcInstance _playerInstance = null;
	private int[] _coordinates = new int[3];
	private boolean _adminRemove = false;
	
	public TvTEventTeleporter(L2PcInstance playerInstance, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;
		_adminRemove = adminRemove;
		
		long delay = (TvTEvent.isStarted() ? Config.TVT_EVENT_RESPAWN_TELEPORT_DELAY : Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;
		
		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}
	
	@Override
	public void run()
	{
		if (_playerInstance == null)
			return;
		
		L2Summon summon = _playerInstance.getSummon();
		
		if (summon != null)
			summon.unSummon(_playerInstance);
		
		if (Config.TVT_EVENT_EFFECTS_REMOVAL == 0
				|| (Config.TVT_EVENT_EFFECTS_REMOVAL == 1 && (_playerInstance.getTeam() == 0 || (_playerInstance.isInDuel() && _playerInstance.getDuelState() != Duel.DUELSTATE_INTERRUPTED))))
			_playerInstance.stopAllEffectsExceptThoseThatLastThroughDeath();
		
		if (_playerInstance.isInDuel())
			_playerInstance.setDuelState(Duel.DUELSTATE_INTERRUPTED);
		
		int TvTInstance = TvTEvent.getTvTEventInstance();
		if (TvTInstance != 0)
		{
			if (TvTEvent.isStarted() && !_adminRemove)
			{
				_playerInstance.setInstanceId(TvTInstance);
			}
			else
			{
				_playerInstance.setInstanceId(0);
			}
		}
		else
		{
			_playerInstance.setInstanceId(0);
		}
		
		_playerInstance.doRevive();
		
		_playerInstance.teleToLocation( _coordinates[ 0 ] + Rnd.get(101)-50, _coordinates[ 1 ] + Rnd.get(101)-50, _coordinates[ 2 ], false );
		
		if (TvTEvent.isStarted() && !_adminRemove)
			_playerInstance.setTeam(TvTEvent.getParticipantTeamId(_playerInstance.getObjectId()) + 1);
		else
			_playerInstance.setTeam(0);
		
		_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
		_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
		_playerInstance.setCurrentMp(_playerInstance.getMaxMp());
		
		_playerInstance.broadcastStatusUpdate();
		_playerInstance.broadcastUserInfo();
	}
}