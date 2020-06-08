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

import java.util.Random;

public class Rnd
{
	private static final Random random = new Random();
	
	public static int nextInt(int n)
	{
		if (n < 0)
		{
			return random.nextInt(-n) * -1;
		}
		if (n == 0)
		{
			return 0;
		}
		return random.nextInt(n);
	}
	
	public static byte[] nextBytes(byte[] array)
	{
		random.nextBytes(array);
		return array;
	}
}