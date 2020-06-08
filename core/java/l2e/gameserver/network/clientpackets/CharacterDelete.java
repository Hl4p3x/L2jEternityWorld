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

import java.util.List;
import java.util.logging.Level;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.network.serverpackets.CharDeleteFail;
import l2e.gameserver.network.serverpackets.CharDeleteSuccess;
import l2e.gameserver.network.serverpackets.CharSelectionInfo;
import l2e.gameserver.scripting.scriptengine.events.PlayerEvent;
import l2e.gameserver.scripting.scriptengine.listeners.player.PlayerListener;

public final class CharacterDelete extends L2GameClientPacket
{
	private static final List<PlayerListener> _listeners = new FastList<PlayerListener>().shared();
	
	private int _charSlot;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterDelete"))
		{
			sendPacket(new CharDeleteFail(CharDeleteFail.REASON_DELETION_FAILED));
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.fine("deleting slot:" + _charSlot);
		}
		
		try
		{
			byte answer = getClient().markToDeleteChar(_charSlot);
			
			switch (answer)
			{
				default:
				case -1:
					break;
				case 0:
					sendPacket(new CharDeleteSuccess());
					PlayerEvent event = new PlayerEvent();
					event.setClient(getClient());
					event.setObjectId(getClient().getCharSelection(_charSlot).getObjectId());
					event.setName(getClient().getCharSelection(_charSlot).getName());
					firePlayerListener(event);
					break;
				case 1:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
					break;
				case 2:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
					break;
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error:", e);
		}
		CharSelectionInfo cl = new CharSelectionInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
	
	private void firePlayerListener(PlayerEvent event)
	{
		for (PlayerListener listener : _listeners)
		{
			listener.onCharDelete(event);
		}
	}
	
	public static void addPlayerListener(PlayerListener listener)
	{
		if (!_listeners.contains(listener))
		{
			_listeners.add(listener);
		}
	}
	
	public static void removePlayerListener(PlayerListener listener)
	{
		_listeners.remove(listener);
	}
}