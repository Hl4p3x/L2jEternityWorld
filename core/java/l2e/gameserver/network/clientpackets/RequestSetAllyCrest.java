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

import l2e.gameserver.cache.CrestCache;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public final class RequestSetAllyCrest extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length > 192)
		{
			return;
		}
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		if (_length > 192)
		{
			activeChar.sendMessage("The ally crest file size was too big (max 192 bytes).");
			return;
		}
		
		if (activeChar.getAllyId() != 0)
		{
			L2Clan leaderclan = ClanHolder.getInstance().getClan(activeChar.getAllyId());
			
			if ((activeChar.getClanId() != leaderclan.getId()) || !activeChar.isClanLeader())
			{
				return;
			}
			
			boolean remove = false;
			if ((_length == 0) || (_data.length == 0))
			{
				remove = true;
			}
			
			int newId = 0;
			if (!remove)
			{
				newId = IdFactory.getInstance().getNextId();
			}
			
			if (!remove && !CrestCache.getInstance().saveAllyCrest(newId, _data))
			{
				_log.log(Level.INFO, "Error saving crest for ally " + leaderclan.getAllyName() + " [" + leaderclan.getAllyId() + "]");
				return;
			}
			
			leaderclan.changeAllyCrest(newId, false);
		}
	}
}