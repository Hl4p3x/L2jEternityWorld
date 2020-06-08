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

/**
 * Created by LordWinter 10.09.2013 Based on L2J Eternity-World
 */
public class GeoLoaderFactory
{
	private static GeoLoaderFactory instance;
	
	private final GeoLoader[] geoLoaders;
	
	public static GeoLoaderFactory getInstance()
	{
		if (instance == null)
		{
			instance = new GeoLoaderFactory();
		}
		
		return instance;
	}
	
	private GeoLoaderFactory()
	{
		geoLoaders = new GeoLoader[]
		{
			new L2JGeoLoader(),
			new OffGeoLoader()
		};
	}
	
	public GeoLoader getGeoLoader(File file)
	{
		if (file == null)
		{
			return null;
		}
		
		for (GeoLoader geoLoader : geoLoaders)
		{
			if (geoLoader.isAcceptable(file))
			{
				return geoLoader;
			}
		}
		return null;
	}
}