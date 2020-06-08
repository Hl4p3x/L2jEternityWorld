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
package l2e.gameserver.ai;

public enum CtrlEvent
{
	EVT_THINK,
	EVT_ATTACKED,
	EVT_AGGRESSION,
	EVT_STUNNED,
	EVT_PARALYZED,
	EVT_SLEEPING,
	EVT_ROOTED,
	EVT_EVADED,
	EVT_READY_TO_ACT,
	EVT_USER_CMD,
	EVT_ARRIVED,
	EVT_ARRIVED_REVALIDATE,
	EVT_ARRIVED_BLOCKED,
	EVT_FORGET_OBJECT,
	EVT_CANCEL,
	EVT_DEAD,
	EVT_FAKE_DEATH,
	EVT_CONFUSED,
	EVT_MUTED,
	EVT_AFRAID,
	EVT_FINISH_CASTING,
	EVT_BETRAYED
}