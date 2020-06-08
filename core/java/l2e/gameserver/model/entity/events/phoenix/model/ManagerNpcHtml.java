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
package l2e.gameserver.model.entity.events.phoenix.model;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import l2e.gameserver.model.entity.events.phoenix.Configuration;

public class ManagerNpcHtml
{
	TextBuilder sb = new TextBuilder();
	
	public ManagerNpcHtml(String content)
	{
		FastList<String> buttons = new FastList<>();
		
		if (Configuration.getInstance().getBoolean(0, "voteEnabled"))
		{
			buttons.add("<button value=\"Vote\" action=\"bypass -h phoenix showvotelist\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (Configuration.getInstance().getBoolean(0, "eventBufferEnabled"))
		{
			buttons.add("<button value=\"Buffer\" action=\"bypass -h phoenix buffershow\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		if (Configuration.getInstance().getBoolean(0, "schedulerEnabled"))
		{
			buttons.add("<button value=\"Scheduler\" action=\"bypass -h phoenix scheduler\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		buttons.add("<button value=\"Running\" action=\"bypass -h phoenix running\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		
		sb.append("<html><title>Phoenix Event Manager</title><body>");
		sb.append("<table width=270 border=0 bgcolor=666666><tr>");
		
		int c = 0;
		
		for (String button : buttons)
		{
			c++;
			if (c == 4)
			{
				sb.append("</tr><tr>");
			}
			
			sb.append("<td width=90>" + button + "</td>");
		}
		
		switch (c)
		{
			case 1:
				sb.append("<td width=90></td><td width=90></td>");
				break;
			case 2:
				sb.append("<td width=90></td>");
				break;
			case 4:
				sb.append("<td width=90></td><td width=90></td>");
				break;
			case 5:
				sb.append("<td width=90></td>");
				break;
		}
		sb.append("</tr></table>");
		sb.append("<br>");
		sb.append(content);
		sb.append("</body></html>");
	}
	
	public String string()
	{
		return sb.toString();
	}
}