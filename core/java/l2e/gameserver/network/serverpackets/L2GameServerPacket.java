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
package l2e.gameserver.network.serverpackets;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mmocore.network.SendablePacket;

import l2e.EternityWorld;
import l2e.gameserver.model.Location;
import l2e.gameserver.network.L2GameClient;

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{	
	protected static final Logger _log = Logger.getLogger(L2GameServerPacket.class.getName());
	
	protected boolean _invisible = false;
	
	public boolean isInvisible()
	{
		return _invisible;
	}
	
	public void setInvisible(boolean b)
	{
		_invisible = b;
	}

	protected void writeLoc(Location loc)
	{
		writeD(loc.getX());
		writeD(loc.getY());
		writeD(loc.getZ());
	}
	
	@Override
	protected void write()
	{
		try
		{
			writeImpl();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed writing: " + getClass().getSimpleName() + " - L2J Eternity-World Server Version: " + EternityWorld._revision + " ; " + e.getMessage(), e);
		}
	}
	
	public void runImpl()
	{	
	}
	
	protected abstract void writeImpl();
}