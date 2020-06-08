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
package l2e.gameserver.model.effects;

public enum EffectFlag
{
	NONE,
	CHARM_OF_COURAGE,
	CHARM_OF_LUCK,
	PHOENIX_BLESSING,
	NOBLESS_BLESSING,
	SILENT_MOVE,
	PROTECTION_BLESSING,
	RELAXING,
	FEAR,
	CONFUSED,
	MUTED,
	PSYCHICAL_MUTED,
	PSYCHICAL_ATTACK_MUTED,
	DISARMED,
	ROOTED,
	SLEEP,
	STUNNED,
	BETRAYED,
	INVUL,
	PARALYZED,
	BLOCK_RESURRECTION,
	SERVITOR_SHARE;
	
	public int getMask()
	{
		return 1 << ordinal();
	}
}