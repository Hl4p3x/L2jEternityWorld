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
package l2e.scripts.custom;

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public final class FortressArcherCaptain extends AbstractNpcAI
{
	private static final int[] ARCHER_CAPTAIN =
	{
		35661,
		35692,
		35730,
		35761,
		35799,
		35830,
		35861,
		35899,
		35930,
		35968,
		36006,
		36037,
		36075,
		36113,
		36144,
		36175,
		36213,
		36251,
		36289,
		36320,
		36358
	};
	
	private FortressArcherCaptain()
	{
		super(FortressArcherCaptain.class.getSimpleName(), "custom");
		addStartNpc(ARCHER_CAPTAIN);
		addFirstTalkId(ARCHER_CAPTAIN);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final int fortOwner = npc.getFort().getOwnerClan() == null ? 0 : npc.getFort().getOwnerClan().getId();
		return ((player.getClan() != null) && (player.getClanId() == fortOwner)) ? "FortressArcherCaptain.htm" : "FortressArcherCaptain-01.htm";
	}
	
	public static void main(String[] args)
	{
		new FortressArcherCaptain();
	}
}