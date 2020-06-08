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
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.CharSelectInfoPackage;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.L2GameClient.GameClientState;
import l2e.gameserver.network.serverpackets.CharSelected;
import l2e.gameserver.network.serverpackets.SSQInfo;
import l2e.gameserver.network.serverpackets.ServerClose;
import l2e.gameserver.scripting.scriptengine.events.PlayerEvent;
import l2e.gameserver.scripting.scriptengine.listeners.player.PlayerListener;
import l2e.protection.Protection;

public class CharacterSelect extends L2GameClientPacket
{
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	private static final List<PlayerListener> _listeners = new FastList<PlayerListener>().shared();
	
	private int _charSlot;
	
	@SuppressWarnings("unused")
	private int _unk1;
	@SuppressWarnings("unused")
	private int _unk2;
	@SuppressWarnings("unused")
	private int _unk3;
	@SuppressWarnings("unused")
	private int _unk4;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		if (!client.getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect"))
		{
			return;
		}
		
		if (Config.SECOND_AUTH_ENABLED && !client.getSecondaryAuth().isAuthed())
		{
			client.getSecondaryAuth().openDialog();
			return;
		}
		
		if (client.getActiveCharLock().tryLock())
		{
			try
			{
				if (client.getActiveChar() == null)
				{
					final CharSelectInfoPackage info = client.getCharSelection(_charSlot);
					if (info == null)
					{
						return;
					}
					
					if (PunishmentManager.getInstance().hasPunishment(info.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getConnectionAddress().getHostAddress(), PunishmentAffect.IP, PunishmentType.BAN))
					{
						client.close(ServerClose.STATIC_PACKET);
						return;
					}
					
					if (info.getAccessLevel() < 0)
					{
						client.close(ServerClose.STATIC_PACKET);
						return;
					}
					if ((Config.DUALBOX_CHECK_MAX_PLAYERS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddClient(AntiFeedManager.GAME_ID, client, Config.DUALBOX_CHECK_MAX_PLAYERS_PER_IP))
					{
						return;
					}
					
					if (Config.DEBUG)
					{
						_log.fine("selected slot:" + _charSlot);
					}
					
					PlayerEvent event = new PlayerEvent();
					event.setClient(client);
					event.setObjectId(client.getCharSelection(_charSlot).getObjectId());
					event.setName(client.getCharSelection(_charSlot).getName());
					firePlayerListener(event);
					
					final L2PcInstance cha = client.loadCharFromDisk(_charSlot);
					if (cha == null)
					{
						return;
					}
					
					CharNameHolder.getInstance().addName(cha);
					
					cha.setClient(client);
					client.setActiveChar(cha);
					cha.setOnlineStatus(true, true);
					
					sendPacket(new SSQInfo());
					
					if (Protection.isProtectionOn())
					{
						if (!Protection.checkPlayerWithHWID(client, cha.getObjectId(), cha.getName()))
						{
							return;
						}
					}
					
					client.setState(GameClientState.IN_GAME);
					CharSelected cs = new CharSelected(cha, client.getSessionId().playOkID1);
					sendPacket(cs);
				}
			}
			finally
			{
				client.getActiveCharLock().unlock();
			}
			LogRecord record = new LogRecord(Level.INFO, "Logged in");
			record.setParameters(new Object[]
			{
				client
			});
			_logAccounting.log(record);
		}
	}
	
	private void firePlayerListener(PlayerEvent event)
	{
		for (PlayerListener listener : _listeners)
		{
			listener.onCharSelect(event);
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