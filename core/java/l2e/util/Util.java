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
package l2e.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import java.util.logging.Logger;
import java.util.Map;

public class Util
{
	private static final Logger _log = Logger.getLogger(Util.class.getName());

	private static final char[] ILLEGAL_CHARACTERS =
	{
		'/',
		'\n',
		'\r',
		'\t',
		'\0',
		'\f',
		'`',
		'?',
		'*',
		'\\',
		'<',
		'>',
		'|',
		'\"',
		':'
	};
	
	public static boolean isInternalHostname(String host)
	{
		try
		{
			InetAddress addr = InetAddress.getByName(host);
			return addr.isSiteLocalAddress() || addr.isLoopbackAddress();
		}
		catch (UnknownHostException e)
		{
			_log.warning("Util: " + e.getMessage());
		}
		return false;
	}

	public static String printData(byte[] data, int len)
	{
		return new String(HexUtils.bArr2HexEdChars(data, len));
	}
	
	public static String printData(byte[] data)
	{
		return printData(data, data.length);
	}

	public static String printData(ByteBuffer buf)
	{
		byte[] data = new byte[buf.remaining()];
		buf.get(data);
		String hex = Util.printData(data, data.length);
		buf.position(buf.position() - data.length);
		return hex;
	}

	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}

	public static String getStackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static Map<Integer, Integer> sortMap(Map<Integer, Integer> map, boolean asc)
	{
		ValueSortMap vsm = new ValueSortMap();
		return vsm.sortThis(map, asc);
	}

  	public static boolean ArrayContains(int[] paramArrayOfInt, int paramInt)
  	{
    		for (int k : paramArrayOfInt)
      		if (k == paramInt)
        		return true;
    		return false;
  	}

	public static String replaceIllegalCharacters(String str)
	{
		String valid = str;
		for (char c : ILLEGAL_CHARACTERS)
		{
			valid = valid.replace(c, '_');
		}
		return valid;
	}

	public static boolean isValidFileName(String name)
	{
		final File f = new File(name);
		try
		{
			f.getCanonicalPath();
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}
}