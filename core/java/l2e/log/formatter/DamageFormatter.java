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
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.util.StringUtil;

public class DamageFormatter extends Formatter
{
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("yy.MM.dd H:mm:ss");
	
	@Override
	public String format(LogRecord record)
	{
		final Object[] params = record.getParameters();
		final StringBuilder output = StringUtil.startAppend(30 + record.getMessage().length() + (params == null ? 0 : params.length * 10), "[", dateFmt.format(new Date(record.getMillis())), "] '---': ", record.getMessage());
		
		if (params != null)
		{
			for (Object p : params)
			{
				if (p == null)
				{
					continue;
				}
				
				if (p instanceof L2Character)
				{
					if ((p instanceof L2Attackable) && ((L2Attackable) p).isRaid())
					{
						StringUtil.append(output, "RaidBoss ");
					}
					
					StringUtil.append(output, ((L2Character) p).getName(), "(", String.valueOf(((L2Character) p).getObjectId()), ") ");
					StringUtil.append(output, String.valueOf(((L2Character) p).getLevel()), " lvl");
					
					if (p instanceof L2Summon)
					{
						L2PcInstance owner = ((L2Summon) p).getOwner();
						if (owner != null)
						{
							StringUtil.append(output, " Owner:", owner.getName(), "(", String.valueOf(owner.getObjectId()), ")");
						}
					}
				}
				else if (p instanceof L2Skill)
				{
					StringUtil.append(output, " with skill ", ((L2Skill) p).getName(), "(", String.valueOf(((L2Skill) p).getId()), ")");
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