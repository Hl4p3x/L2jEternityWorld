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
package l2e.scripts.teleports;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.zone.L2ZoneType;

public class Warpgate extends Quest
{
	private static final String THATS_BLOODY_HOT = "_133_ThatsBloodyHot";
	private static final String PATH_TO_HELLBOUND = "_130_PathToHellbound";
	
	private static final int MAP = 9994;
	private static final int ZONE = 40101;
	
	private static final int[] WARPGATES =
	{
		32314, 32315, 32316, 32317, 32318, 32319
	};
	
	private static final boolean canEnter(L2PcInstance player)
	{
		if (player.isFlying())
		{
			return false;
		}

	    	if (Config.ENTER_HELLBOUND_WITHOUT_QUEST)
			return true;	
		
		QuestState st;
		if (!HellboundManager.getInstance().isLocked())
		{
			st = player.getQuestState(PATH_TO_HELLBOUND);
			if ((st != null) && st.isCompleted())
			{
				return true;
			}
		}
		
		st = player.getQuestState(THATS_BLOODY_HOT);
		return ((st != null) && st.isCompleted());
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (!canEnter(player))
		{
			if (HellboundManager.getInstance().isLocked())
			{
				return "warpgate-locked.htm";
			}
		}
		
		return npc.getId() + ".htm";
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (!canEnter(player))
		{
			return "warpgate-no.htm";
		}
		
		player.teleToLocation(-11272, 236464, -3248, true);
		HellboundManager.getInstance().unlock();
		return null;
	}
	
	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.isPlayer())
		{
			if (!canEnter(character.getActingPlayer()) && !character.canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(character), 1000);
			}
			else if (!((L2PcInstance) character).isMinimapAllowed())
			{
				if (character.getInventory().getItemByItemId(MAP) != null)
				{
					((L2PcInstance) character).setMinimapAllowed(true);
				}
			}
		}
		return null;
	}
	
	private static final class Teleport implements Runnable
	{
		private final L2Character _char;
		
		public Teleport(L2Character c)
		{
			_char = c;
		}
		
		@Override
		public void run()
		{
			try
			{
				_char.teleToLocation(-16555, 209375, -3670, true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public Warpgate(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : WARPGATES)
		{
			addStartNpc(id);
			addFirstTalkId(id);
			addTalkId(id);
		}
		addEnterZoneId(ZONE);
	}
	
	public static void main(String[] args)
	{
		new Warpgate(-1, "Warpgate", "teleports");
	}
}