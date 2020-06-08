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
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.util.Rnd;

public class OpenAtod implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"openatod"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equalsIgnoreCase("openatod"))
		{
			if (params == null)
			{
				activeChar.sendMessage("Usage: .openatod [num]");
			}
			else
			{
				int num = 0;
				try
				{
					num = Integer.parseInt(params);
				}
				catch (NumberFormatException nfe)
				{
					activeChar.sendMessage("You must enter a number. Usage: .openatod [num]");
					return false;
				}
				
				if (num == 0)
				{
					return false;
				}
				else if (activeChar.getInventory().getInventoryItemCount(9599, 0) >= num)
				{
					int a = 0, b = 0, c = 0, rnd;
					for (int i = 0; i < num; i++)
					{
						rnd = Rnd.get(100);
						if ((rnd <= 100) && (rnd > 44))
						{
							a++;
						}
						else if ((rnd <= 44) && (rnd > 14))
						{
							b++;
						}
						else if (rnd <= 14)
						{
							c++;
						}
					}
					if (activeChar.destroyItemByItemId("ATOD", 9599, a + b + c, null, true))
					{
						if (a > 0)
						{
							activeChar.addItem("ATOD", 9600, a, null, true);
						}
						if (b > 0)
						{
							activeChar.addItem("ATOD", 9601, b, null, true);
						}
						if (c > 0)
						{
							activeChar.addItem("ATOD", 9602, c, null, true);
						}
					}
					else
					{
						activeChar.sendMessage("You do not have enough tomes.");
					}
				}
				else
				{
					activeChar.sendMessage("You do not have enough tomes.");
				}
			}
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}