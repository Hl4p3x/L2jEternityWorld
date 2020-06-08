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
package l2e.gameserver.model;

public class L2PhantomPlayer
{
	protected String _name;
	protected String _title;
	protected int _x;
	protected int _y;
	protected int _z;
	protected int _clanId;
	
	public L2PhantomPlayer(String name, String title, int x, int y, int z, int clanId)
	{
		_name = name;
		_title = title;
		_x = x;
		_y = y;
		_z = z;
		_clanId = clanId;
	}
}