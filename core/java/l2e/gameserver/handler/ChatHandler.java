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
package l2e.gameserver.handler;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.handler.chathandlers.*;

public class ChatHandler
{
	private static Logger _log = Logger.getLogger(ChatHandler.class.getName());
	
	private final TIntObjectHashMap<IChatHandler> _datatable;
	
	public static ChatHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ChatHandler()
	{
		_datatable = new TIntObjectHashMap<>();

		registerHandler(new ChatAll());
		registerHandler(new ChatAlliance());
		registerHandler(new ChatBattlefield());
		registerHandler(new ChatClan());
		registerHandler(new ChatHeroVoice());
		registerHandler(new ChatParty());
		registerHandler(new ChatPartyMatchRoom());
		registerHandler(new ChatPartyRoomAll());
		registerHandler(new ChatPartyRoomCommander());
		registerHandler(new ChatPetition());
		registerHandler(new ChatShout());
		registerHandler(new ChatTell());
		registerHandler(new ChatTrade());

		_log.info("Loaded " + _datatable.size() + "  ChatHandlers.");
	}
	
	public void registerHandler(IChatHandler handler)
	{
		int[] ids = handler.getChatTypeList();
		for (int i = 0; i < ids.length; i++)
		{
			if (Config.DEBUG)
				_log.fine("Adding handler for chat type " + ids[i]);
			_datatable.put(ids[i], handler);
		}
	}
	
	public IChatHandler getHandler(int chatType)
	{
		return _datatable.get(chatType);
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final ChatHandler _instance = new ChatHandler();
	}
}