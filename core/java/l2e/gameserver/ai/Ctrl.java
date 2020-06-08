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

import l2e.gameserver.model.actor.L2Character;

public interface Ctrl
{
	L2Character getActor();
	
	CtrlIntention getIntention();
	
	L2Character getAttackTarget();
	
	void setIntention(CtrlIntention intention);
	
	void setIntention(CtrlIntention intention, Object arg0);
	
	void setIntention(CtrlIntention intention, Object arg0, Object arg1);
	
	void notifyEvent(CtrlEvent evt);
	
	void notifyEvent(CtrlEvent evt, Object arg0);
	
	void notifyEvent(CtrlEvent evt, Object arg0, Object arg1);
}