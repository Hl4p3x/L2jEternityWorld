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
package l2e.gameserver.handler.voicedcommandhandlers;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.tasks.player.VoteRewardTask;

/**
 * Created by LordWinter 01.16.2013 Based on L2J Eternity-World
 */
public class Vote implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"vote",
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("vote"))
		{
			boolean disabled = false;
			if (disabled)
			{
				activeChar.sendMessage("Temporally disabled. Vote reward will be available soon again.");
				return true;
			}
			
			if (!activeChar.isVoting())
			{
				activeChar.sendMessage("Checking votes... please wait. Make sure you voted on all links!");
				ThreadPoolManager.getInstance().scheduleGeneral(new VoteRewardTask(activeChar), 180000);
				activeChar.setVoting(true);
			}
			else
			{
				activeChar.sendMessage("Already checking votes... please wait.");
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}