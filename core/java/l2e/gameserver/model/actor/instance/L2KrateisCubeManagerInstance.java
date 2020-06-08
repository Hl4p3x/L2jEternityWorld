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

import l2e.gameserver.instancemanager.KrateisCubeManager;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.network.SystemMessageId;

/**
 * Created by LordWinter 06.03.2011 Fixed by L2J Eternity-World
 */
public class L2KrateisCubeManagerInstance extends L2NpcInstance
{
	public L2KrateisCubeManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Register"))
		{
			if ((player.getInventoryLimit() * 0.8) <= player.getInventory().getSize())
			{
				player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				showChatWindow(player, "data/html/krateisCube/32503-9.htm");
				return;
			}
			
			int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
			switch (cmdChoice)
			{
				case 1:
					if ((player.getLevel() < 70) || (player.getLevel() > 75))
					{
						showChatWindow(player, "data/html/krateisCube/32503-7.htm");
						return;
					}
					break;
				case 2:
					if ((player.getLevel() < 76) || (player.getLevel() > 79))
					{
						showChatWindow(player, "data/html/krateisCube/32503-7.htm");
						return;
					}
					break;
				case 3:
					if (player.getLevel() < 80)
					{
						showChatWindow(player, "data/html/krateisCube/32503-7.htm");
						return;
					}
				case 4:
					if (player.getLevel() < 70)
					{
						showChatWindow(player, "data/html/krateisCube/32503-10.htm");
						return;
					}
					break;
			}
			
			if (KrateisCubeManager.getInstance().isTimeToRegister())
			{
				if (KrateisCubeManager.getInstance().registerPlayer(player))
				{
					showChatWindow(player, "data/html/krateisCube/32503-4.htm");
				}
				else
				{
					showChatWindow(player, "data/html/krateisCube/32503-5.htm");
				}
			}
			else
			{
				showChatWindow(player, "data/html/krateisCube/32503-8.htm");
				return;
			}
		}
		else if (command.startsWith("Cancel"))
		{
			KrateisCubeManager.getInstance().removePlayer(player);
			showChatWindow(player, "data/html/krateisCube/32503-6.htm");
			return;
		}
		else if (command.startsWith("TeleportToFI"))
		{
			player.teleToLocation(-59193, -56893, -2034);
			L2Summon pet = player.getSummon();
			if (pet != null)
			{
				pet.teleToLocation(-59193, -56893, -2034);
			}
			
			return;
		}
		else if (command.startsWith("TeleportIn"))
		{
			KrateisCubeManager.getInstance().teleportPlayerIn(player);
			return;
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/krateisCube/" + pom + ".htm";
	}
}