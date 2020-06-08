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

import l2e.Config;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.serverpackets.KeyPacket;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.protection.ConfigProtection;
import l2e.protection.Protection;

public final class ProtocolVersion extends L2GameClientPacket
{
	protected static final Logger _log = Logger.getLogger(ProtocolVersion.class.getName());
	private static final Logger _logAccounting = Logger.getLogger("accounting");
	
	private int _version;
	private byte[] _data;
	private String _hwidHdd = "", _hwidMac = "", _hwidCPU = "";
	
	@Override
	protected void readImpl()
	{
		L2GameClient client = getClient();
		_version = readD();
		if (_buf.remaining() > 260)
		{
			_data = new byte[260];
			readB(_data);
			if (Protection.isProtectionOn())
			{
				_hwidHdd = readS();
				_hwidMac = readS();
				_hwidCPU = readS();
			}
		}
		else if (Protection.isProtectionOn())
		{
			client.close(new KeyPacket(getClient().enableCrypt(), 0));
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_version == -2)
		{
			if (Config.DEBUG)
			{
				_log.info("Ping received");
			}
			getClient().close((L2GameServerPacket) null);
		}
		else if (!Config.PROTOCOL_LIST.contains(_version))
		{
			LogRecord record = new LogRecord(Level.WARNING, "Wrong protocol");
			record.setParameters(new Object[]
			{
				_version,
				getClient()
			});
			_logAccounting.log(record);
			KeyPacket pk = new KeyPacket(getClient().enableCrypt(), 0);
			getClient().setProtocolOk(false);
			getClient().close(pk);
		}
		getClient().setRevision(_version);
		if (Protection.isProtectionOn())
		{
			switch (ConfigProtection.GET_CLIENT_HWID)
			{
				case 1:
					if (_hwidHdd == "")
					{
						_log.info("Status HWID HDD : NoPatch!!!");
						getClient().close(new KeyPacket(getClient().enableCrypt(), 0));
					}
					else
					{
						getClient().setHWID(_hwidHdd);
					}
					break;
				case 2:
					if (_hwidMac == "")
					{
						_log.info("Status HWID MAC : NoPatch!!!");
						getClient().close(new KeyPacket(getClient().enableCrypt(), 0));
					}
					else
					{
						getClient().setHWID(_hwidMac);
					}
					break;
				case 3:
					if (_hwidCPU == "")
					{
						_log.info("Status HWID : NoPatch!!!");
						getClient().close(new KeyPacket(getClient().enableCrypt(), 0));
					}
					else
					{
						getClient().setHWID(_hwidCPU);
					}
					break;
			}
		}
		else
		{
			getClient().setHWID("NoGuard");
		}
		
		if (Config.DEBUG)
		{
			_log.fine("Client Protocol Revision is ok: " + _version);
		}
		
		KeyPacket pk = new KeyPacket(getClient().enableCrypt(), 1);
		getClient().sendPacket(pk);
		getClient().setProtocolOk(true);
	}
}