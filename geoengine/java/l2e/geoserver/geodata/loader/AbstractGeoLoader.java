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
package l2e.geoserver.geodata.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import l2e.Config;

/**
 * Created by LordWinter 10.09.2013 Based on L2J Eternity-World
 */
public abstract class AbstractGeoLoader implements GeoLoader
{
	private static final Logger _log = Logger.getLogger(AbstractGeoLoader.class.getName());
	
	private static final Pattern SCANNER_DELIMITER = Pattern.compile("([_|\\.]){1}");
	
	@Override
	public boolean isAcceptable(File file)
	{
		if (!file.exists())
		{
			_log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file doesn't exists.");
			return false;
		}
		
		if (file.isDirectory())
		{
			_log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is directory.");
			return false;
		}
		
		if (file.isHidden())
		{
			_log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is hidden.");
			return false;
		}
		
		if (file.length() > Integer.MAX_VALUE)
		{
			_log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is to big.");
			return false;
		}
		
		if (!getPattern().matcher(file.getName()).matches())
		{
			if (Config.DEBUG)
			{
				_log.info(getClass().getSimpleName() + ": can't load file: " + file.getName() + "!!! Reason: pattern missmatch");
			}
			return false;
		}
		return true;
	}
	
	@Override
	public GeoFileInfo readFile(File file)
	{
		
		_log.info(getClass().getSimpleName() + ": loading geodata file: " + file.getName());
		
		FileInputStream fis = null;
		byte[] data = null;
		try
		{
			fis = new FileInputStream(file);
			data = new byte[fis.available()];
			int readed = fis.read(data);
			if (readed != data.length)
			{
				_log.warning("Not fully readed file?");
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				if (fis != null)
				{
					fis.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		GeoFileInfo geoFileInfo = createGeoFileInfo(file);
		geoFileInfo.setData(parse(convert(data)));
		return geoFileInfo;
	}
	
	protected GeoFileInfo createGeoFileInfo(File file)
	{
		Scanner scanner = new Scanner(file.getName());
		scanner.useDelimiter(SCANNER_DELIMITER);
		int ix = scanner.nextInt();
		int iy = scanner.nextInt();
		scanner.close();
		
		GeoFileInfo geoFileInfo = new GeoFileInfo();
		geoFileInfo.setX(ix);
		geoFileInfo.setY(iy);
		return geoFileInfo;
	}
	
	protected abstract byte[][] parse(byte[] data);
	
	public abstract Pattern getPattern();
	
	public abstract byte[] convert(byte[] data);
}