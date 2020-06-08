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

public final class NewCrypt
{
	private final BlowfishEngine _cipher;
	
	public NewCrypt(byte[] blowfishKey)
	{
		_cipher = new BlowfishEngine();
		_cipher.init(blowfishKey);
	}
	
	public NewCrypt(String key)
	{
		this(key.getBytes());
	}
	
	public static boolean verifyChecksum(final byte[] raw)
	{
		return NewCrypt.verifyChecksum(raw, 0, raw.length);
	}
	
	public static boolean verifyChecksum(final byte[] raw, final int offset, final int size)
	{
		if ((size & 3) != 0 || size <= 4)
		{
			return false;
		}
		
		long chksum = 0;
		int count = size-4;
		long check = -1;
		int i;
		
		for (i = offset; i<count; i+=4)
		{
			check = raw[i] &0xff;
			check |= raw[i+1] << 8 &0xff00;
			check |= raw[i+2] << 0x10 &0xff0000;
			check |= raw[i+3] << 0x18 &0xff000000;
			
			chksum ^= check;
		}
		
		check = raw[i] &0xff;
		check |= raw[i+1] << 8 &0xff00;
		check |= raw[i+2] << 0x10 &0xff0000;
		check |= raw[i+3] << 0x18 &0xff000000;
		
		return check == chksum;
	}
	
	public static void appendChecksum(final byte[] raw)
	{
		NewCrypt.appendChecksum(raw, 0, raw.length);
	}
	
	public static void appendChecksum(final byte[] raw, final int offset, final int size)
	{
		long chksum = 0;
		int count = size-4;
		long ecx;
		int i;
		
		for (i = offset; i<count; i+=4)
		{
			ecx = raw[i] &0xff;
			ecx |= raw[i+1] << 8 &0xff00;
			ecx |= raw[i+2] << 0x10 &0xff0000;
			ecx |= raw[i+3] << 0x18 &0xff000000;
			
			chksum ^= ecx;
		}
		
		ecx = raw[i] &0xff;
		ecx |= raw[i+1] << 8 &0xff00;
		ecx |= raw[i+2] << 0x10 &0xff0000;
		ecx |= raw[i+3] << 0x18 &0xff000000;
		
		raw[i] = (byte) (chksum &0xff);
		raw[i+1] = (byte) (chksum >>0x08 &0xff);
		raw[i+2] = (byte) (chksum >>0x10 &0xff);
		raw[i+3] = (byte) (chksum >>0x18 &0xff);
	}
	
	public static void encXORPass(byte[] raw, int key)
	{
		NewCrypt.encXORPass(raw, 0, raw.length, key);
	}
	
	static void encXORPass(byte[] raw, final int offset, final int size, int key)
	{
		int stop = size-8;
		int pos = 4 + offset;
		int edx;
		int ecx = key;
		
		while (pos < stop)
		{
			edx = (raw[pos] & 0xFF);
			edx |= (raw[pos+1] & 0xFF) << 8;
			edx |= (raw[pos+2] & 0xFF) << 16;
			edx |=  (raw[pos+3] & 0xFF) << 24;
			
			ecx += edx;
			
			edx ^= ecx;
			
			raw[pos++] = (byte) (edx & 0xFF);
			raw[pos++] = (byte) (edx >> 8 & 0xFF);
			raw[pos++] = (byte) (edx >> 16 & 0xFF);
			raw[pos++] = (byte) (edx >> 24 & 0xFF);
		}
		
		raw[pos++] = (byte) (ecx & 0xFF);
		raw[pos++] = (byte) (ecx >> 8 & 0xFF);
		raw[pos++] = (byte) (ecx >> 16 & 0xFF);
		raw[pos++] = (byte) (ecx >> 24 & 0xFF);
	}
	
	
	public void decrypt(byte[] raw, final int offset, final int size)
	{
		for (int i = offset; i < (offset + size); i += 8)
		{
			_cipher.decryptBlock(raw, i);
		}
	}
	
	public void crypt(byte[] raw, final int offset, final int size)
	{
		for (int i = offset; i < (offset + size); i += 8)
		{
			_cipher.encryptBlock(raw, i);
		}
	}
}