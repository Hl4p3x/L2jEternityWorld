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
package l2e.protection.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import l2e.gameserver.model.actor.instance.L2PcInstance;

public class Log
{	
	private static final Logger _log = Logger.getLogger(Log.class.getName());
	
	public static void add(String text, String cat)
	{
		if (cat.equals("items") || cat.equals("chat") || cat.equals("CommunityBoard"))
		{
			cat = cat + (new SimpleDateFormat("yyyy.MM.dd")).format(new Date());
		}
		add(text, cat, "yy.MM.dd HH:mm:ss", null);
	}
	
	public static void add(String text, String cat, L2PcInstance player)
	{
		add(text, cat, "yy.MM.dd HH:mm:ss", player);
	}
	
	public static void add(String text, String cat, String DateFormat)
	{
		add(text, cat, DateFormat, null);
	}
	
	public static synchronized void add(String text, String cat, String DateFormat, L2PcInstance player)
	{
		new File("log/protection").mkdirs();
		File file = new File("log/protection/" + (cat != null ? cat : "_all") + ".txt");
		
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
				_log.warning("saving " + (cat != null ? cat : "all") + " log failed, can't create file: " + e);
				return;
			}
		}
		
		FileWriter save = null;
		StringBuffer msgb = new StringBuffer();
		
		try
		{
			save = new FileWriter(file, true);
			if (!DateFormat.equals(""))
			{
				String date = (new SimpleDateFormat(DateFormat)).format(new Date());
				msgb.append("[" + date + "]: ");
			}
			
			if (player != null)
			{
				msgb.append(player.getName() + " ");
			}
			
			msgb.append(text + "\n");
			save.write(msgb.toString());
		}
		catch (IOException e)
		{
			_log.warning("saving " + (cat != null ? cat : "all") + " log failed: " + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (save != null)
				{
					save.close();
				}
			}
			catch (Exception e1)
			{
			}
		}
	}
}