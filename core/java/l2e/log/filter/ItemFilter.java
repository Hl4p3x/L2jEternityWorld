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
package l2e.log.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import l2e.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author Advi
 */
public class ItemFilter implements Filter
{
	// private String _excludeProcess;
	// private String _excludeItemType;
	
	// This is an example how to exclude consuming of shots and arrows from logging
	private final String _excludeProcess = "Consume";
	private final String _excludeItemType = "Arrow, Shot, Herb";
	
	@Override
	public boolean isLoggable(LogRecord record)
	{
		if (!"item".equals(record.getLoggerName()))
		{
			return false;
		}
		if (_excludeProcess != null)
		{
			// if (record.getMessage() == null) return true;
			String[] messageList = record.getMessage().split(":");
			if ((messageList.length < 2) || !_excludeProcess.contains(messageList[1]))
			{
				return true;
			}
		}
		if (_excludeItemType != null)
		{
			// if (record.getParameters() == null || record.getParameters().length == 0 || !(record.getParameters()[0] instanceof L2ItemInstance)) return true;
			L2ItemInstance item = ((L2ItemInstance) record.getParameters()[0]);
			if (!_excludeItemType.contains(item.getItemType().toString()))
			{
				return true;
			}
		}
		return ((_excludeProcess == null) && (_excludeItemType == null));
	}
}
