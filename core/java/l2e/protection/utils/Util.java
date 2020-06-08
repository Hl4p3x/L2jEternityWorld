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

public class Util
{
	public static byte[] asByteArray(String hex)
	{
		byte[] buf = new byte[hex.length() / 2];
		
		for (int i = 0; i < hex.length(); i += 2)
		{
			int j = Integer.parseInt(hex.substring(i, i + 2), 16);
			buf[(i / 2)] = (byte) (j & 0xFF);
		}
		return buf;
	}
	
	public static String asHwidString(String hex)
	{
		byte[] buf = asByteArray(hex);
		return asHex(buf);
	}
	
	public static final String asHex(byte[] raw, int offset, int size)
	{
		StringBuffer strbuf = new StringBuffer(raw.length * 2);
		
		for (int i = 0; i < size; i++)
		{
			if ((raw[(offset + i)] & 0xFF) < 16)
			{
				strbuf.append("0");
			}
			strbuf.append(Long.toString(raw[(offset + i)] & 0xFF, 16));
		}
		
		return strbuf.toString();
	}
	
	public static final String asHex(byte[] raw)
	{
		return asHex(raw, 0, raw.length);
	}

	public static boolean verifyChecksum(byte[] raw, final int offset, final int size)
	{
		if (((size & 3) != 0) || (size <= 4))
		{
			return false;
		}
		long chksum = 0;
		int count = size - 4;
		int i = 0;
		for (i = offset; i < count; i += 4)
		{
			chksum ^= bytesToInt(raw, i);
		}
		long check = bytesToInt(raw, count);
		return check == chksum;
	}

	public static int bytesToInt(byte[] array, int offset)
	{
		return ((array[offset++] & 0xff) | ((array[offset++] & 0xff) << 8) | ((array[offset++] & 0xff) << 16) | ((array[offset++] & 0xff) << 24));
	}

	public static String LastErrorConvertion(Integer LastError)
	{
		return LastError.toString();
	}

	public static void intToBytes(int value, byte[] array, int offset)
	{
		array[offset++] = (byte) (value & 0xff);
		array[offset++] = (byte) ((value >> 0x08) & 0xff);
		array[offset++] = (byte) ((value >> 0x10) & 0xff);
		array[offset++] = (byte) ((value >> 0x18) & 0xff);
	}
}