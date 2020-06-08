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
package l2e.gameserver.model.entity.events.phoenix;

import l2e.gameserver.model.entity.events.phoenix.function.Buffer;
import l2e.gameserver.model.entity.events.phoenix.function.Scheduler;
import l2e.gameserver.model.entity.events.phoenix.function.Vote;
import l2e.gameserver.model.entity.events.phoenix.io.Out;

public class PhoenixEngine
{
	public static void phoenixStart()
	{
		Configuration.getInstance();
		
		if (Configuration.getInstance().getBoolean(0, "voteEnabled"))
		{
			Vote.getInstance();
		}
		if (Configuration.getInstance().getBoolean(0, "schedulerEnabled"))
		{
			Scheduler.getInstance();
		}
		if (Configuration.getInstance().getBoolean(0, "eventBufferEnabled"))
		{
			Buffer.getInstance();
		}
		Out.registerHandlers();
	}
}