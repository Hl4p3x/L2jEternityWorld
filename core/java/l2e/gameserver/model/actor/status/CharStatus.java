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

import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastSet;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.stat.CharStat;
import l2e.gameserver.model.stats.Formulas;
import l2e.util.Rnd;

public class CharStatus
{
	protected static final Logger _log = Logger.getLogger(CharStatus.class.getName());
	
	private final L2Character _activeChar;
	
	private double _currentHp = 0;
	private double _currentMp = 0;
	
	private Set<L2Character> _StatusListener;
	
	private Future<?> _regTask;
	
	protected byte _flagsRegenActive = 0;
	
	protected static final byte REGEN_FLAG_CP = 4;
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;
	
	public CharStatus(L2Character activeChar)
	{
		_activeChar = activeChar;
	}
	
	public final void addStatusListener(L2Character object)
	{
		if (object == getActiveChar())
		{
			return;
		}
		
		getStatusListener().add(object);
	}
	
	public final void removeStatusListener(L2Character object)
	{
		getStatusListener().remove(object);
	}
	
	public final Set<L2Character> getStatusListener()
	{
		if (_StatusListener == null)
		{
			_StatusListener = new FastSet<L2Character>().shared();
		}
		return _StatusListener;
	}
	
	public void reduceCp(int value)
	{
	}
	
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	public void reduceHp(double value, L2Character attacker, boolean isHpConsumption)
	{
		reduceHp(value, attacker, true, false, isHpConsumption);
	}
	
	public void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		if (getActiveChar().isInvul() && !(isDOT || isHPConsumption))
		{
			return;
		}
		
		if (attacker != null)
		{
			final L2PcInstance attackerPlayer = attacker.getActingPlayer();
			if ((attackerPlayer != null) && attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
			{
				return;
			}
		}
		
		if (!isDOT && !isHPConsumption)
		{
			getActiveChar().stopEffectsOnDamage(awake);
			if (getActiveChar().isStunned() && (Rnd.get(10) == 0))
			{
				getActiveChar().stopStunning(true);
			}
		}
		
		if (value > 0)
		{
			setCurrentHp(Math.max(getCurrentHp() - value, 0));
		}
		
		if ((getActiveChar().getCurrentHp() < 0.5) && getActiveChar().isMortal())
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();
			
			if (Config.DEBUG)
			{
				_log.fine("char is dead.");
			}
			
			getActiveChar().doDie(attacker);
		}
	}
	
	public void reduceMp(double value)
	{
		setCurrentMp(Math.max(getCurrentMp() - value, 0));
	}
	
	public final synchronized void startHpMpRegeneration()
	{
		if ((_regTask == null) && !getActiveChar().isDead())
		{
			if (Config.DEBUG)
			{
				_log.fine("HP/MP regen started");
			}
			
			int period = Formulas.getRegeneratePeriod(getActiveChar());
			_regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RegenTask(), period, period);
		}
	}
	
	public final synchronized void stopHpMpRegeneration()
	{
		if (_regTask != null)
		{
			if (Config.DEBUG)
			{
				_log.fine("HP/MP regen stop");
			}
			
			_regTask.cancel(false);
			_regTask = null;
			_flagsRegenActive = 0;
		}
	}
	
	public double getCurrentCp()
	{
		return 0;
	}
	
	public void setCurrentCp(double newCp)
	{
	}
	
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	public final void setCurrentHp(double newHp)
	{
		setCurrentHp(newHp, true);
	}
	
	public boolean setCurrentHp(double newHp, boolean broadcastPacket)
	{
		int currentHp = (int) getCurrentHp();
		final double maxHp = getActiveChar().getStat().getMaxHp();
		
		synchronized (this)
		{
			if (getActiveChar().isDead())
			{
				return false;
			}
			
			if (newHp >= maxHp)
			{
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;
				
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;
				startHpMpRegeneration();
			}
		}
		
		boolean hpWasChanged = currentHp != _currentHp;
		
		if (hpWasChanged && broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate();
		}
		return hpWasChanged;
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		boolean hpOrMpWasChanged = setCurrentHp(newHp, false);
		hpOrMpWasChanged |= setCurrentMp(newMp, false);
		if (hpOrMpWasChanged)
		{
			getActiveChar().broadcastStatusUpdate();
		}
	}
	
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}
	
	public final boolean setCurrentMp(double newMp, boolean broadcastPacket)
	{
		int currentMp = (int) getCurrentMp();
		final int maxMp = getActiveChar().getStat().getMaxMp();
		
		synchronized (this)
		{
			if (getActiveChar().isDead())
			{
				return false;
			}
			
			if (newMp >= maxMp)
			{
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;
				
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;
				startHpMpRegeneration();
			}
		}
		
		boolean mpWasChanged = currentMp != _currentMp;
		
		if (mpWasChanged && broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate();
		}
		
		return mpWasChanged;
	}
	
	protected void doRegeneration()
	{
		final CharStat charstat = getActiveChar().getStat();
		
		if (getCurrentHp() < charstat.getMaxRecoverableHp())
		{
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		}
		
		if (getCurrentMp() < charstat.getMaxRecoverableMp())
		{
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		}
		
		if (!getActiveChar().isInActiveRegion())
		{
			if ((getCurrentHp() == charstat.getMaxRecoverableHp()) && (getCurrentMp() == charstat.getMaxMp()))
			{
				stopHpMpRegeneration();
			}
		}
		else
		{
			getActiveChar().broadcastStatusUpdate();
		}
	}
	
	class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				doRegeneration();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	public L2Character getActiveChar()
	{
		return _activeChar;
	}
}