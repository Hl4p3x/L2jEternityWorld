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
package l2e.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import l2e.Config;
import l2e.util.StringUtil;

public class FileLogFormatter extends Formatter
{
	private static final String _ = "\t";
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss,SSS");
	
	@Override
	public String format(LogRecord record)
	{
		return StringUtil.concat(dateFmt.format(new Date(record.getMillis())), _, record.getLevel().getName(), _, String.valueOf(record.getThreadID()), _, record.getLoggerName(), _, record.getMessage(), Config.EOL);
	}
}