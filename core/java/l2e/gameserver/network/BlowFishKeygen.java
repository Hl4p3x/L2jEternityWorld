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
package l2e.gameserver.network;

import l2e.util.Rnd;

public class BlowFishKeygen
{
	private static final int CRYPT_KEYS_SIZE = 20;
	private static final byte[][] CRYPT_KEYS = new byte[CRYPT_KEYS_SIZE][16];
	
	static
	{
		for (int i = 0; i < CRYPT_KEYS_SIZE; i++)
		{
			for (int j = 0; j < CRYPT_KEYS[i].length; j++)
			{
				CRYPT_KEYS[i][j] = (byte) Rnd.get(255);
			}
			CRYPT_KEYS[i][8] = (byte) 0xc8;
			CRYPT_KEYS[i][9] = (byte) 0x27;
			CRYPT_KEYS[i][10] = (byte) 0x93;
			CRYPT_KEYS[i][11] = (byte) 0x01;
			CRYPT_KEYS[i][12] = (byte) 0xa1;
			CRYPT_KEYS[i][13] = (byte) 0x6c;
			CRYPT_KEYS[i][14] = (byte) 0x31;
			CRYPT_KEYS[i][15] = (byte) 0x97;
		}
	}
	
	private BlowFishKeygen()
	{	
	}
	
	public static byte[] getRandomKey()
	{
		return CRYPT_KEYS[Rnd.get(CRYPT_KEYS_SIZE)];
	}
}