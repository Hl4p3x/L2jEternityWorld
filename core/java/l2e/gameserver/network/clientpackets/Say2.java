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
package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.handler.ChatHandler;
import l2e.gameserver.handler.IChatHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.scripting.scriptengine.events.ChatEvent;
import l2e.gameserver.scripting.scriptengine.listeners.talk.ChatFilterListener;
import l2e.gameserver.scripting.scriptengine.listeners.talk.ChatListener;
import l2e.gameserver.util.Util;

public final class Say2 extends L2GameClientPacket
{
	private static Logger _logChat = Logger.getLogger("chat");
	
	private static FastList<ChatListener> chatListeners = new FastList<ChatListener>().shared();
	private static FastList<ChatFilterListener> chatFilterListeners = new FastList<ChatFilterListener>().shared();
	
	public static final int ALL = 0;
	public static final int SHOUT = 1;
	public static final int TELL = 2;
	public static final int PARTY = 3;
	public static final int CLAN = 4;
	public static final int GM = 5;
	public static final int PETITION_PLAYER = 6;
	public static final int PETITION_GM = 7;
	public static final int TRADE = 8;
	public static final int ALLIANCE = 9;
	public static final int ANNOUNCEMENT = 10;
	public static final int BOAT = 11;
	public static final int L2FRIEND = 12;
	public static final int MSNCHAT = 13;
	public static final int PARTYMATCH_ROOM = 14;
	public static final int PARTYROOM_COMMANDER = 15;
	public static final int PARTYROOM_ALL = 16;
	public static final int HERO_VOICE = 17;
	public static final int CRITICAL_ANNOUNCE = 18;
	public static final int SCREEN_ANNOUNCE = 19;
	public static final int BATTLEFIELD = 20;
	public static final int MPCC_ROOM = 21;
	public static final int NPC_ALL = 22;
	public static final int NPC_SHOUT = 23;
	
	private static final String[] CHAT_NAMES =
	{
		"ALL",
		"SHOUT",
		"TELL",
		"PARTY",
		"CLAN",
		"GM",
		"PETITION_PLAYER",
		"PETITION_GM",
		"TRADE",
		"ALLIANCE",
		"ANNOUNCEMENT",
		"BOAT",
		"L2FRIEND",
		"MSNCHAT",
		"PARTYMATCH_ROOM",
		"PARTYROOM_COMMANDER",
		"PARTYROOM_ALL",
		"HERO_VOICE",
		"CRITICAL_ANNOUNCE",
		"SCREEN_ANNOUNCE",
		"BATTLEFIELD",
		"MPCC_ROOM"
	};
	
	private static final String[] WALKER_COMMAND_LIST =
	{
		"USESKILL",
		"USEITEM",
		"BUYITEM",
		"SELLITEM",
		"SAVEITEM",
		"LOADITEM",
		"MSG",
		"DELAY",
		"LABEL",
		"JMP",
		"CALL",
		"RETURN",
		"MOVETO",
		"NPCSEL",
		"NPCDLG",
		"DLGSEL",
		"CHARSTATUS",
		"POSOUTRANGE",
		"POSINRANGE",
		"GOHOME",
		"SAY",
		"EXIT",
		"PAUSE",
		"STRINDLG",
		"STRNOTINDLG",
		"CHANGEWAITTYPE",
		"FORCEATTACK",
		"ISMEMBER",
		"REQUESTJOINPARTY",
		"REQUESTOUTPARTY",
		"QUITPARTY",
		"MEMBERSTATUS",
		"CHARBUFFS",
		"ITEMCOUNT",
		"FOLLOWTELEPORT"
	};
	
	private String _text;
	private int _type;
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_text = readS();
		_type = readD();
		_target = (_type == TELL) ? readS() : null;
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.info("Say2: Msg Type = '" + _type + "' Text = '" + _text + "'.");
		}
		
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_type < 0) || (_type >= CHAT_NAMES.length))
		{
			_log.warning("Say2: Invalid type: " + _type + " Player : " + activeChar.getName() + " text: " + String.valueOf(_text));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.logout();
			return;
		}
		
		if (_text.isEmpty())
		{
			_log.warning(activeChar.getName() + ": sending empty text. Possible packet hack!");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.logout();
			return;
		}
		
		if (!activeChar.isGM() && (((_text.indexOf(8) >= 0) && (_text.length() > 500)) || ((_text.indexOf(8) < 0) && (_text.length() > 105))))
		{
			activeChar.sendPacket(SystemMessageId.DONT_SPAM);
			return;
		}
		
		if (Config.L2WALKER_PROTECTION && (_type == TELL) && checkBot(_text))
		{
			Util.handleIllegalPlayerAction(activeChar, "Client Emulator Detect: Player " + activeChar.getName() + " using l2walker.", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (activeChar.isCursedWeaponEquipped() && ((_type == TRADE) || (_type == SHOUT)))
		{
			activeChar.sendPacket(SystemMessageId.SHOUT_AND_TRADE_CHAT_CANNOT_BE_USED_WHILE_POSSESSING_CURSED_WEAPON);
			return;
		}
		
		if (activeChar.isChatBanned() && (_text.charAt(0) != '.'))
		{
			if (activeChar.getFirstEffect(L2EffectType.CHAT_BLOCK) != null)
			{
				activeChar.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_CHATTING_NOT_ALLOWED);
			}
			else
			{
				for (int chatId : Config.BAN_CHAT_CHANNELS)
				{
					if (_type == chatId)
					{
						activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
					}
				}
			}
			return;
		}
		
		if (activeChar.isJailed() && Config.JAIL_DISABLE_CHAT)
		{
			if ((_type == TELL) || (_type == SHOUT) || (_type == TRADE) || (_type == HERO_VOICE))
			{
				activeChar.sendMessage("You can not chat with players outside of the jail.");
				return;
			}
		}
		
		if ((_type == PETITION_PLAYER) && activeChar.isGM())
		{
			_type = PETITION_GM;
		}
		
		if (Config.LOG_CHAT)
		{
			LogRecord record = new LogRecord(Level.INFO, _text);
			record.setLoggerName("chat");
			
			if (_type == TELL)
			{
				record.setParameters(new Object[]
				{
					CHAT_NAMES[_type],
					"[" + activeChar.getName() + " to " + _target + "]"
				});
			}
			else
			{
				record.setParameters(new Object[]
				{
					CHAT_NAMES[_type],
					"[" + activeChar.getName() + "]"
				});
			}
			
			_logChat.log(record);
		}
		
		if (_text.indexOf(8) >= 0)
		{
			if (!parseAndPublishItem(activeChar))
			{
				return;
			}
		}
		
		fireChatListeners(activeChar);
		
		if (Config.USE_SAY_FILTER)
		{
			checkText();
		}
		
		fireChatFilters(activeChar);
		
		IChatHandler handler = ChatHandler.getInstance().getHandler(_type);
		if (handler != null)
		{
			handler.handleChat(_type, activeChar, _target, _text);
		}
		else
		{
			_log.info("No handler registered for ChatType: " + _type + " Player: " + getClient());
		}
	}
	
	private boolean checkBot(String text)
	{
		for (String botCommand : WALKER_COMMAND_LIST)
		{
			if (text.startsWith(botCommand))
			{
				return true;
			}
		}
		return false;
	}
	
	private void checkText()
	{
		String filteredText = _text;
		for (String pattern : Config.FILTER_LIST)
		{
			filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
		}
		_text = filteredText;
	}
	
	private boolean parseAndPublishItem(L2PcInstance owner)
	{
		int pos1 = -1;
		while ((pos1 = _text.indexOf(8, pos1)) > -1)
		{
			int pos = _text.indexOf("ID=", pos1);
			if (pos == -1)
			{
				return false;
			}
			StringBuilder result = new StringBuilder(9);
			pos += 3;
			while (Character.isDigit(_text.charAt(pos)))
			{
				result.append(_text.charAt(pos++));
			}
			int id = Integer.parseInt(result.toString());
			L2Object item = L2World.getInstance().findObject(id);
			if (item instanceof L2ItemInstance)
			{
				if (owner.getInventory().getItemByObjectId(id) == null)
				{
					_log.info(getClient() + " trying publish item which doesnt own! ID:" + id);
					return false;
				}
				((L2ItemInstance) item).publish();
			}
			else
			{
				_log.info(getClient() + " trying publish object which is not item! Object:" + item);
				return false;
			}
			pos1 = _text.indexOf(8, pos) + 1;
			if (pos1 == 0)
			{
				_log.info(getClient() + " sent invalid publish item msg! ID:" + id);
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
	
	private void fireChatListeners(L2PcInstance activeChar)
	{
		if (!chatListeners.isEmpty())
		{
			ChatEvent event = null;
			event = new ChatEvent();
			event.setOrigin(activeChar);
			event.setTarget(_target);
			event.setTargetType(ChatListener.getTargetType(CHAT_NAMES[_type]));
			event.setText(_text);
			for (ChatListener listener : chatListeners)
			{
				listener.onTalk(event);
			}
		}
	}
	
	private void fireChatFilters(L2PcInstance activeChar)
	{
		if (!chatFilterListeners.isEmpty())
		{
			ChatEvent event = null;
			event = new ChatEvent();
			event.setOrigin(activeChar);
			event.setTarget(_target);
			event.setTargetType(ChatListener.getTargetType(CHAT_NAMES[_type]));
			event.setText(_text);
			for (ChatFilterListener listener : chatFilterListeners)
			{
				_text = listener.onTalk(event);
			}
		}
		
	}
	
	public static void addChatListener(ChatListener listener)
	{
		if (!chatListeners.contains(listener))
		{
			chatListeners.add(listener);
		}
	}
	
	public static void removeChatListener(ChatListener listener)
	{
		chatListeners.remove(listener);
	}
	
	public static void addChatFilterListener(ChatFilterListener listener)
	{
		if (!chatFilterListeners.contains(listener))
		{
			chatFilterListeners.add(listener);
		}
	}
	
	public static void removeChatFilterListener(ChatFilterListener listener)
	{
		chatFilterListeners.remove(listener);
	}
}