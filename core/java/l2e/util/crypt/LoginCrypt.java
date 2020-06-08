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
package l2e.util.crypt;

import java.io.IOException;

import l2e.util.Rnd;

public class LoginCrypt
{
	private static final byte[] STATIC_BLOWFISH_KEY =
	{
		(byte) 0x6b,
		(byte) 0x60,
		(byte) 0xcb,
		(byte) 0x5b,
		(byte) 0x82,
		(byte) 0xce,
		(byte) 0x90,
		(byte) 0xb1,
		(byte) 0xcc,
		(byte) 0x2b,
		(byte) 0x6c,
		(byte) 0x55,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c
	};
	
	private static final NewCrypt _STATIC_CRYPT = new NewCrypt(STATIC_BLOWFISH_KEY);
	private NewCrypt _crypt = null;
	private boolean _static = true;
	
	public void setKey(byte[] key)
	{
		_crypt = new NewCrypt(key);
	}
	
	public boolean decrypt(byte[] raw, final int offset, final int size) throws IOException
	{
		if ((size % 8) != 0)
		{
			throw new IOException("size have to be multiple of 8");
		}
		if ((offset + size) > raw.length)
		{
			throw new IOException("raw array too short for size starting from offset");
		}
		
		_crypt.decrypt(raw, offset, size);
		return NewCrypt.verifyChecksum(raw, offset, size);
	}
	
	public int encrypt(byte[] raw, final int offset, int size) throws IOException
	{
		size += 4;
		
		if (_static)
		{
			size += 4;
			
			size += 8 - (size % 8);
			if ((offset + size) > raw.length)
			{
				throw new IOException("packet too long");
			}
			NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt());
			_STATIC_CRYPT.crypt(raw, offset, size);
			_static = false;
		}
		else
		{
			size += 8 - (size % 8);
			if ((offset + size) > raw.length)
			{
				throw new IOException("packet too long");
			}
			NewCrypt.appendChecksum(raw, offset, size);
			_crypt.crypt(raw, offset, size);
		}
		return size;
	}
}