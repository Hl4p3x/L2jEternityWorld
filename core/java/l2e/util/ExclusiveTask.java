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
package l2e.util;

import java.util.concurrent.Future;

import l2e.gameserver.ThreadPoolManager;

public abstract class ExclusiveTask
{
	private final boolean _returnIfAlreadyRunning;
	
	private Future<?> _future;
	private boolean _isRunning;
	private Thread _currentThread;
	
	protected ExclusiveTask(boolean returnIfAlreadyRunning)
	{
		_returnIfAlreadyRunning = returnIfAlreadyRunning;
	}
	
	protected ExclusiveTask()
	{
		this(false);
	}
	
	public synchronized boolean isScheduled()
	{
		return _future != null;
	}
	
	public synchronized final void cancel()
	{
		if (_future != null)
		{
			_future.cancel(false);
			_future = null;
		}
	}
	
	public synchronized final void schedule(long delay)
	{
		cancel();
		
		_future = ThreadPoolManager.getInstance().scheduleGeneral(_runnable, delay);
	}
	
	public synchronized final void scheduleAtFixedRate(long delay, long period)
	{
		cancel();
		
		_future = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(_runnable, delay, period);
	}
	
	private final Runnable _runnable = new Runnable()
	{
		@Override
		public void run()
		{
			if (tryLock())
			{
				try
				{
					onElapsed();
				}
				finally
				{
					unlock();
				}
			}
		}
	};
	
	protected abstract void onElapsed();
	
	protected synchronized boolean tryLock()
	{
		if (_returnIfAlreadyRunning)
		{
			return !_isRunning;
		}
		
		_currentThread = Thread.currentThread();
		
		for (;;)
		{
			try
			{
				notifyAll();
				
				if (_currentThread != Thread.currentThread())
				{
					return false;
				}
				
				if (!_isRunning)
				{
					return true;
				}
				
				wait();
			}
			catch (InterruptedException e)
			{
			}
		}
	}
	
	protected synchronized void unlock()
	{
		_isRunning = false;
	}
}