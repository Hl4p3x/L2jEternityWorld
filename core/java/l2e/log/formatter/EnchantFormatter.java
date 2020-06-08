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
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.util.StringUtil;

public class EnchantFormatter extends Formatter
{
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");
	
	@Override
	public String format(LogRecord record)
	{
		final Object[] params = record.getParameters();
		final StringBuilder output = StringUtil.startAppend(30 + record.getMessage().length() + (params == null ? 0 : params.length * 10), "[", dateFmt.format(new Date(record.getMillis())), "] ", record.getMessage());
		
		if (params != null)
		{
			for (Object p : params)
			{
				if (p == null)
				{
					continue;
				}
				
				StringUtil.append(output, ", ");
				
				if (p instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) p;
					StringUtil.append(output, "Character:", player.getName(), " [" + String.valueOf(player.getObjectId()) + "] Account:", player.getAccountName());
					if ((player.getClient() != null) && !player.getClient().isDetached())
					{
						StringUtil.append(output, " IP:", player.getClient().getConnection().getInetAddress().getHostAddress());
					}
				}
				else if (p instanceof L2ItemInstance)
				{
					L2ItemInstance item = (L2ItemInstance) p;
					if (item.getEnchantLevel() > 0)
					{
						StringUtil.append(output, "+", String.valueOf(item.getEnchantLevel()), " ");
					}
					StringUtil.append(output, item.getItem().getName(), "(", String.valueOf(item.getCount()), ")");
					StringUtil.append(output, " [", String.valueOf(item.getObjectId()), "]");
				}
				else if (p instanceof L2Skill)
				{
					L2Skill skill = (L2Skill) p;
					if (skill.getLevel() > 100)
					{
						StringUtil.append(output, "+", String.valueOf(skill.getLevel() % 100), " ");
					}
					StringUtil.append(output, skill.getName(), "(", String.valueOf(skill.getId()), " ", String.valueOf(skill.getLevel()), ")");
				}
				else
				{
					StringUtil.append(output, p.toString());
				}
			}
		}
		output.append(Config.EOL);
		return output.toString();
	}
}