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
package l2e.util.network;

import java.util.logging.Logger;

public abstract class BaseRecievePacket
{
	private static final Logger _log = Logger.getLogger(BaseRecievePacket.class.getName());
	
	private final byte[] _decrypt;
	private int _off;
	
	public BaseRecievePacket(byte[] decrypt)
	{
		_decrypt = decrypt;
		_off = 1;
	}
	
	public int readD()
	{
		int result = _decrypt[_off++] & 0xff;
		result |= _decrypt[_off++] << 8 & 0xff00;
		result |= _decrypt[_off++] << 0x10 & 0xff0000;
		result |= _decrypt[_off++] << 0x18 & 0xff000000;
		return result;
	}
	
	public int readC()
	{
		int result = _decrypt[_off++] &0xff;
		return result;
	}
	
	public int readH()
	{
		int result = _decrypt[_off++] &0xff;
		result |= _decrypt[_off++] << 8 &0xff00;
		return result;
	}
	
	public double readF()
	{
		long result = _decrypt[_off++] & 0xff;
		result |= (_decrypt[_off++] & 0xffL) << 8L;
		result |= (_decrypt[_off++] & 0xffL) << 16L;
		result |= (_decrypt[_off++] & 0xffL) << 24L;
		result |= (_decrypt[_off++] & 0xffL) << 32L;
		result |= (_decrypt[_off++] & 0xffL) << 40L;
		result |= (_decrypt[_off++] & 0xffL) << 48L;
		result |= (_decrypt[_off++] & 0xffL) << 56L;
		return Double.longBitsToDouble(result);
	}
	
	public String readS()
	{
		String result = null;
		try
		{
			result = new String(_decrypt,_off,_decrypt.length-_off, "UTF-16LE");
			result = result.substring(0, result.indexOf(0x00));
			_off += result.length()*2 + 2;
		}
		catch (Exception e)
		{
			_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		return result;
	}
	
	public final byte[] readB(int length)
	{
		byte[] result = new byte[length];
		System.arraycopy(_decrypt, _off, result, 0, length);
		_off += length;
		return result;
	}
	
	public long readQ()
	{
		long result = _decrypt[_off++] & 0xff;
		result |= (_decrypt[_off++] & 0xffL) << 8L;
		result |= (_decrypt[_off++] & 0xffL) << 16L;
		result |= (_decrypt[_off++] & 0xffL) << 24L;
		result |= (_decrypt[_off++] & 0xffL) << 32L;
		result |= (_decrypt[_off++] & 0xffL) << 40L;
		result |= (_decrypt[_off++] & 0xffL) << 48L;
		result |= (_decrypt[_off++] & 0xffL) << 56L;
		return result;
	}
}