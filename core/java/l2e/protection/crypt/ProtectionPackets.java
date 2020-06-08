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
package l2e.protection.crypt;

import l2e.protection.utils.Rnd;

public class ProtectionPackets
{
	public static int readB(byte[] raw, int offset, byte[] data, int size)
	{
		for (int i = 0; i < size; i++)
		{
			data[i] = (byte) (raw[offset] ^ raw[0]);
			offset += (raw[(offset + 1)] & 0xFF);
		}
		
		return offset;
	}
	
	public static int readS(byte[] raw, int offset, byte[] data, int size)
	{
		for (int i = 0; i < size; i++)
		{
			data[i] = (byte) (raw[offset] ^ raw[0]);
			offset += (raw[(offset + 1)] & 0xFF);
			
			if (data[i] == 0)
			{
				break;
			}
		}
		return offset;
	}
	
	public static int writeB(byte[] raw, int offset, byte[] data, int size)
	{
		for (int i = 0; i < size; i++)
		{
			raw[offset] = (byte) (data[i] ^ raw[0]);
			raw[(offset + 1)] = (byte) (2 + Rnd.nextInt(10));
			offset += (raw[(offset + 1)] & 0xFF);
		}
		
		return offset;
	}
	
	public static byte ck(byte[] raw, int offset, int size)
	{
		byte c = -1;
		
		for (int i = 0; i < size; i++)
		{
			c = (byte) (c ^ raw[(offset + i)]);
		}
		return c;
	}
}