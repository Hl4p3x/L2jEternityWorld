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

import java.util.logging.Logger;
import java.util.regex.Pattern;

import l2e.geoserver.geodata.GeoEngine;

/**
 * Created by LordWinter 10.09.2013 Based on L2J Eternity-World
 */
public class L2JGeoLoader extends AbstractGeoLoader
{
	private static final Logger _log = Logger.getLogger(L2JGeoLoader.class.getName());
	
	private static final Pattern PATTERN = Pattern.compile("[\\d]{2}_[\\d]{2}.l2j");
	
	@Override
	protected byte[][] parse(byte[] data)
	{
		if (data.length <= 196608)
		{
			return null;
		}
		
		byte[][] blocks = new byte[65536][];
		
		int index = 0;
		
		for (int block = 0, n = blocks.length; block < n; block++)
		{
			byte type = data[index];
			index++;
			
			byte[] geoBlock;
			switch (type)
			{
				case GeoEngine.BLOCKTYPE_FLAT:
					geoBlock = new byte[2 + 1];
					
					geoBlock[0] = type;
					geoBlock[1] = data[index];
					geoBlock[2] = data[index + 1];
					
					blocks[block] = geoBlock;
					index += 2;
					break;
				case GeoEngine.BLOCKTYPE_COMPLEX:
					if (index < data.length)
					{
						geoBlock = new byte[128 + 1];
						
						geoBlock[0] = type;
						System.arraycopy(data, index, geoBlock, 1, 128);
						
						index += 128;
						
						blocks[block] = geoBlock;
					}
					else
					{
						_log.warning("L2JGeoLoader.parse BLOCKTYPE_COMPLEX format corrupt, skipped. index=" + index + " data.length=" + data.length);
					}
					break;
				case GeoEngine.BLOCKTYPE_MULTILEVEL:
					int orgIndex = index;
					
					for (int b = 0; b < 64; b++)
					{
						byte layers = data[index];
						index += (layers << 1) + 1;
					}
					
					int diff = index - orgIndex;
					
					geoBlock = new byte[diff + 1];
					
					geoBlock[0] = type;
					System.arraycopy(data, orgIndex, geoBlock, 1, diff);
					
					blocks[block] = geoBlock;
					break;
				default:
					_log.warning("GeoEngine: invalid block type: " + type);
			}
		}
		return blocks;
	}
	
	@Override
	public Pattern getPattern()
	{
		return PATTERN;
	}
	
	@Override
	public byte[] convert(byte[] data)
	{
		return data;
	}
}