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

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestLinkHtml extends L2GameClientPacket
{
	private String _link;
	
	@Override
	protected void readImpl()
	{
		_link = readS();
	}
	
	@Override
	public void runImpl()
	{
		L2PcInstance actor = getClient().getActiveChar();
		if (actor == null)
		{
			return;
		}
		
		if (_link.contains("..") || !_link.contains(".htm"))
		{
			_log.warning("[RequestLinkHtml] hack? link contains prohibited characters: '" + _link + "', skipped");
			return;
		}
		try
		{
			String filename = "data/html/" + _link;
			NpcHtmlMessage msg = new NpcHtmlMessage(0);
			msg.disableValidation();
			msg.setFile(actor.getLang(), filename);
			sendPacket(msg);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Bad RequestLinkHtml: ", e);
		}
	}
}