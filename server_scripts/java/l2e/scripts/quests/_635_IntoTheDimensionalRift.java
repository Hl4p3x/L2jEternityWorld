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
package l2e.scripts.quests;

import l2e.gameserver.model.quest.Quest;

public class _635_IntoTheDimensionalRift extends Quest
{
	private _635_IntoTheDimensionalRift(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
	
	public static void main(String[] args)
	{
		new _635_IntoTheDimensionalRift(635, _635_IntoTheDimensionalRift.class.getSimpleName(), "");
	}
}