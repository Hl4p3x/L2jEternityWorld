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
package l2e.gameserver.model.actor.instance;

import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.handler.VoicedCommandHandler;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;

public class L2AioNpcInstance extends L2NpcInstance
{
	public L2AioNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		setInstanceType(InstanceType.L2AioNpcInstance);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		IVoicedCommandHandler aio_command = VoicedCommandHandler.getInstance().getHandler("aioitem");
		if (aio_command != null)
		{
			aio_command.useVoicedCommand("aioitem", player, "");
			return;
		}
	}
}