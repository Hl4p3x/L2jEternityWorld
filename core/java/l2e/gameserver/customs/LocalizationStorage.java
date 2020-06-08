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
package l2e.gameserver.customs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import l2e.Config;

public class LocalizationStorage
{
	private static final Logger _log = Logger.getLogger(LocalizationStorage.class.getName());
	
	private final HashMap<String, HashMap<String, String>> _sysmessages = new HashMap<>();
	
	protected LocalizationStorage()
	{
		reload();
	}
	
	public String getString(String lang, String name)
	{
		if (lang == null)
		{
			lang = "en";
		}
		if (_sysmessages.get(lang) == null)
		{
			return "";
		}
		if (_sysmessages.get(lang).get(name) == null)
		{
			return "";
		}
		return _sysmessages.get(lang).get(name);
	}
	
	private void reload()
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/localization/");
		for (File file : dir.listFiles())
		{
			if (file.isDirectory() && !file.isHidden())
			{
				String lang = file.getName();
				HashMap<String, String> map = new HashMap<>();
				readFromDisk(map, lang);
				_sysmessages.put(lang, map);
			}
		}
	}
	
	private void readFromDisk(HashMap<String, String> map, String lang)
	{
		LineNumberReader lnr = null;
		try
		{
			String line;
			File file = new File(Config.DATAPACK_ROOT, "data/localization/" + lang + "/messages.txt");
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			while ((line = lnr.readLine()) != null)
			{
				String[] args = line.split("=");
				if (args.length != 2)
				{
					continue;
				}
				map.put(args[0], args[1]);
			}
		}
		catch (IOException e1)
		{
			_log.log(Level.SEVERE, "Error loading \"" + lang + "\" language pack: ", e1);
		}
		finally
		{
			IOUtils.closeQuietly(lnr);
		}
	}
	
	public static LocalizationStorage getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final LocalizationStorage _instance = new LocalizationStorage();
	}
}