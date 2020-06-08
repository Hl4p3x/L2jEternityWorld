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
package l2e.gameserver.model.restriction;

import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public interface GlobalRestriction
{
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction);

	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target);

	public boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage, L2PcInstance attacker_, L2PcInstance target_);

	public boolean canRequestRevive(L2PcInstance activeChar);

	public boolean canTeleport(L2PcInstance activeChar);

	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2PcInstance player);

	public boolean canUseItem(int itemId, L2PcInstance activeChar, L2ItemInstance item);

	public boolean canStandUp(L2PcInstance activeChar);

	public void levelChanged(L2PcInstance activeChar);

	public void playerLoggedIn(L2PcInstance activeChar);

	public void playerDisconnected(L2PcInstance activeChar);

	public boolean playerKilled(L2Character activeChar, L2PcInstance target, L2PcInstance killer);

	public void playerRevived(L2PcInstance player);

	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command);

	public boolean onAction(L2Npc npc, L2PcInstance activeChar);

	public boolean fakePvPZone(L2PcInstance activeChar, L2PcInstance target);
}