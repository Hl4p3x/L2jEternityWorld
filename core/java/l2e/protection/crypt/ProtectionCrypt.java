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

public final class ProtectionCrypt
{
	int x;
	int y;
	byte[] state = new byte[256];
	boolean _inited = false;
	
	final int arcfour_byte()
	{
		int x;
		int y;
		int sx, sy;
		
		x = (this.x + 1) & 0xff;
		sx = state[x];
		y = (sx + this.y) & 0xff;
		sy = state[y];
		this.x = x;
		this.y = y;
		state[y] = (byte) (sx & 0xff);
		state[x] = (byte) (sy & 0xff);
		return state[((sx + sy) & 0xff)];
	}
	
	public synchronized void encrypt(byte[] src, int srcOff, byte[] dest, int destOff, int len)
	{
		if (!_inited)
		{
			return;
		}
		int end = srcOff + len;
		for (int si = srcOff, di = destOff; si < end; si++, di++)
		{
			dest[di] = (byte) ((src[si] ^ arcfour_byte()) & 0xff);
		}
	}
	
	public void decrypt(byte[] src, int srcOff, byte[] dest, int destOff, int len)
	{
		encrypt(src, srcOff, dest, destOff, len);
	}
	
	public void setKey(byte[] key)
	{
		int t, u;
		int counter;
		this.x = 0;
		this.y = 0;
		
		for (counter = 0; counter < 256; counter++)
		{
			state[counter] = (byte) counter;
		}
		
		int keyindex = 0;
		int stateindex = 0;
		for (counter = 0; counter < 256; counter++)
		{
			t = state[counter];
			stateindex = (stateindex + key[keyindex] + t) & 0xff;
			u = state[stateindex];
			state[stateindex] = (byte) (t & 0xff);
			state[counter] = (byte) (u & 0xff);
			if (++keyindex >= key.length)
			{
				keyindex = 0;
			}
		}
		this._inited = true;
	}
	
	public boolean isInited()
	{
		return this._inited;
	}
	
	public static int getValue(final int index)
	{
		return ProtectData.getValue(index);
	}
}