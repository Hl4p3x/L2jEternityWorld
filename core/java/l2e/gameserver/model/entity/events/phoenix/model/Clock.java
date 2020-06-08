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
package l2e.gameserver.model.entity.events.phoenix.model;

import l2e.gameserver.model.entity.events.phoenix.io.Out;

public abstract class Clock implements Runnable
{
	protected int counter;
	
	public Clock(int time)
	{
		counter = time;
	}
	
	public abstract void clockBody();
	
	public int getTimeInInt()
	{
		return counter;
	}
	
	public String getTimeInString()
	{
		String mins = "" + (counter / 60);
		String secs = ((counter % 60) < 10 ? "0" + (counter % 60) : "" + (counter % 60));
		return "" + mins + ":" + secs + "";
	}
	
	protected abstract void onZero();
	
	@Override
	public void run()
	{
		clockBody();
		
		if (counter == 0)
		{
			onZero();
		}
		else
		{
			counter--;
			Out.tpmScheduleGeneral(this, 1000);
		}
	}
	
	public void start()
	{
		Out.tpmScheduleGeneral(this, 1);
	}
	
	public void stop()
	{
		counter = 0;
	}
}