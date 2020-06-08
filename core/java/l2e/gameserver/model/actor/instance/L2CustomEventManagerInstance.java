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

import java.util.StringTokenizer;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.events.FunEvent;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2CustomEventManagerInstance extends L2Npc
{
	public FunEvent _event = null;
	
	public L2CustomEventManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if ((player == null) || (_event == null))
		{
			return;
		}
		
		if (!player.getEventName().equals("") && !player.getEventName().equalsIgnoreCase(_event.EVENT_NAME))
		{
			player.sendMessage("You are already registered in other Fun Event.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You are already registered in olympiad match.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		NpcHtmlMessage npcHtmlMessage = _event.getChatWindow(player);
		
		if (npcHtmlMessage != null)
		{
			npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(npcHtmlMessage);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String currentCommand = st.nextToken();
		
		if (currentCommand.startsWith("join"))
		{
			int joinTeamId = Integer.parseInt(st.nextToken());
			_event.addPlayer(player, joinTeamId);
			showChatWindow(player, 0);
		}
		else if (currentCommand.startsWith("leave"))
		{
			_event.removePlayer(player);
			showChatWindow(player, 0);
		}
		else
		{
			showChatWindow(player, 0);
		}
	}
}