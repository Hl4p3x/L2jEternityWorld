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
package l2e.gameserver.model.fishing;

import java.util.concurrent.Future;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.xml.FishMonstersParser;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExFishingHpRegen;
import l2e.gameserver.network.serverpackets.ExFishingStartCombat;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Rnd;

public class L2Fishing implements Runnable
{
	private L2PcInstance _fisher;
	private int _time;
	private int _stop = 0;
	private int _goodUse = 0;
	private int _anim = 0;
	private int _mode = 0;
	private int _deceptiveMode = 0;
	private Future<?> _fishAiTask;
	private boolean _thinking;
	private final int _fishId;
	private final int _fishMaxHp;
	private int _fishCurHp;
	private final double _regenHp;
	private final boolean _isUpperGrade;
	private int _lureType;
	
	@Override
	public void run()
	{
		if (_fisher == null)
		{
			return;
		}
		
		if (_fishCurHp >= (_fishMaxHp * 2))
		{
			_fisher.sendPacket(SystemMessageId.BAIT_STOLEN_BY_FISH);
			doDie(false);
		}
		else if (_time <= 0)
		{
			_fisher.sendPacket(SystemMessageId.FISH_SPIT_THE_HOOK);
			doDie(false);
		}
		else
		{
			aiTask();
		}
	}
	
	public L2Fishing(L2PcInstance Fisher, L2Fish fish, boolean isNoob, boolean isUpperGrade)
	{
		_fisher = Fisher;
		_fishMaxHp = fish.getFishHp();
		_fishCurHp = _fishMaxHp;
		_regenHp = fish.getHpRegen();
		_fishId = fish.getItemId();
		_time = fish.getCombatDuration();
		_isUpperGrade = isUpperGrade;
		if (isUpperGrade)
		{
			_deceptiveMode = Rnd.get(100) >= 90 ? 1 : 0;
			_lureType = 2;
		}
		else
		{
			_deceptiveMode = 0;
			_lureType = isNoob ? 0 : 1;
		}
		_mode = Rnd.get(100) >= 80 ? 1 : 0;
		
		_fisher.broadcastPacket(new ExFishingStartCombat(_fisher, _time, _fishMaxHp, _mode, _lureType, _deceptiveMode));
		_fisher.sendPacket(new PlaySound(1, "SF_S_01", 0, 0, 0, 0, 0));
		_fisher.sendPacket(SystemMessageId.GOT_A_BITE);
		
		if (_fishAiTask == null)
		{
			_fishAiTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 1000, 1000);
		}
	}
	
	public void changeHp(int hp, int pen)
	{
		_fishCurHp -= hp;
		if (_fishCurHp < 0)
		{
			_fishCurHp = 0;
		}
		
		ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, _goodUse, _anim, pen, _deceptiveMode);
		_fisher.broadcastPacket(efhr);
		_anim = 0;
		if (_fishCurHp > (_fishMaxHp * 2))
		{
			_fishCurHp = _fishMaxHp * 2;
			doDie(false);
			return;
		}
		else if (_fishCurHp == 0)
		{
			doDie(true);
			return;
		}
	}
	
	public synchronized void doDie(boolean win)
	{
		if (_fishAiTask != null)
		{
			_fishAiTask.cancel(false);
			_fishAiTask = null;
		}
		
		if (_fisher == null)
		{
			return;
		}
		
		if (win)
		{
			final L2FishingMonster fishingMonster = FishMonstersParser.getInstance().getFishingMonster(_fisher.getLevel());
			if (fishingMonster != null)
			{
				if (Rnd.get(100) <= fishingMonster.getProbability())
				{
					_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK);
					spawnMonster(fishingMonster.getFishingMonsterId());
				}
				else
				{
					_fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING);
					_fisher.addItem("Fishing", _fishId, 1, null, true);
				}
			}
		}
		_fisher.endFishing(win);
		_fisher = null;
	}
	
	protected void aiTask()
	{
		if (_thinking)
		{
			return;
		}
		_thinking = true;
		_time--;
		
		try
		{
			if (_mode == 1)
			{
				if (_deceptiveMode == 0)
				{
					_fishCurHp += (int) _regenHp;
				}
			}
			else
			{
				if (_deceptiveMode == 1)
				{
					_fishCurHp += (int) _regenHp;
				}
			}
			if (_stop == 0)
			{
				_stop = 1;
				int check = Rnd.get(100);
				if (check >= 70)
				{
					_mode = _mode == 0 ? 1 : 0;
				}
				if (_isUpperGrade)
				{
					check = Rnd.get(100);
					if (check >= 90)
					{
						_deceptiveMode = _deceptiveMode == 0 ? 1 : 0;
					}
				}
			}
			else
			{
				_stop--;
			}
		}
		finally
		{
			_thinking = false;
			ExFishingHpRegen efhr = new ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, 0, _anim, 0, _deceptiveMode);
			if (_anim != 0)
			{
				_fisher.broadcastPacket(efhr);
			}
			else
			{
				_fisher.sendPacket(efhr);
			}
		}
	}
	
	public void useReeling(int dmg, int pen)
	{
		_anim = 2;
		if (Rnd.get(100) > 90)
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, pen);
			return;
		}
		if (_fisher == null)
		{
			return;
		}
		if (_mode == 1)
		{
			if (_deceptiveMode == 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				if (pen > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_fisher.sendPacket(sm);
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else
		{
			if (_deceptiveMode == 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				if (pen > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_fisher.sendPacket(sm);
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
		}
	}
	
	public void usePumping(int dmg, int pen)
	{
		_anim = 1;
		if (Rnd.get(100) > 90)
		{
			_fisher.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN);
			_goodUse = 0;
			changeHp(0, pen);
			return;
		}
		if (_fisher == null)
		{
			return;
		}
		if (_mode == 0)
		{
			if (_deceptiveMode == 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				if (pen > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_fisher.sendPacket(sm);
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else
		{
			if (_deceptiveMode == 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				_goodUse = 2;
				changeHp(-dmg, pen);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_fisher.sendPacket(sm);
				if (pen > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_fisher.sendPacket(sm);
				}
				_goodUse = 1;
				changeHp(dmg, pen);
			}
		}
	}
	
	private void spawnMonster(int npcId)
	{
		final L2NpcTemplate monster = NpcTable.getInstance().getTemplate(npcId);
		if (monster != null)
		{
			try
			{
				L2Spawn spawn = new L2Spawn(monster);
				spawn.setX(_fisher.getX());
				spawn.setY(_fisher.getY());
				spawn.setZ(_fisher.getZ());
				spawn.setAmount(1);
				spawn.setHeading(_fisher.getHeading());
				spawn.stopRespawn();
				spawn.doSpawn();
				spawn.getLastSpawn().setTarget(_fisher);
			}
			catch (Exception e)
			{
			}
		}
	}
}