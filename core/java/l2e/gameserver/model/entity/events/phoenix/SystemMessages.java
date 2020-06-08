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
package l2e.gameserver.model.entity.events.phoenix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import javolution.util.FastMap;

public class SystemMessages
{
	private static class SingletonHolder
	{
		protected static final SystemMessages _instance = new SystemMessages();
	}
	
	public static SystemMessages getInstance()
	{
		return SingletonHolder._instance;
	}
	
	FastMap<Integer, String> messages;
	
	public SystemMessages()
	{
		messages = new FastMap<>();
		loadMessages();
	}
	
	public String getMsg(Integer msgId, Object[] params)
	{
		String message = messages.get(msgId);
		if (message != null)
		{
			if (params == null)
			{
				return message;
			}
			int i = 0;
			while (message.indexOf("#" + i) != -1)
			{
				message = message.replaceAll("#" + i, params[i].toString());
				i++;
			}
			return message;
		}
		return "MSG-ERROR";
	}
	
	private void loadMessages()
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader("config/EventMessages.txt"));
			String str;
			while ((str = in.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(str, "|");
				Integer id = Integer.valueOf(st.nextToken());
				String message = st.nextToken();
				messages.put(id, message);
			}
			in.close();
		}
		catch (IOException e)
		{
		}
	}
}