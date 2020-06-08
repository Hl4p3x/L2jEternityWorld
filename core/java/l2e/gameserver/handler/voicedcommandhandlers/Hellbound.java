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

import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class Hellbound implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"hellbound"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (HellboundManager.getInstance().isLocked())
		{
			activeChar.sendMessage("Hellbound is currently locked.");
			return true;
		}
		
		final int maxTrust = HellboundManager.getInstance().getMaxTrust();
		activeChar.sendMessage("Hellbound level: " + HellboundManager.getInstance().getLevel() + " trust: " + HellboundManager.getInstance().getTrust() + (maxTrust > 0 ? "/" + maxTrust : ""));
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}