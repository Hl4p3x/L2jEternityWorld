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

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

/**
 * Based on L2J Eternity-World
 */
public class DelusionTeleport extends Quest
{
	private final static int REWARDER_ONE = 32658;
	private final static int REWARDER_TWO = 32659;
	private final static int REWARDER_THREE = 32660;
	private final static int REWARDER_FOUR = 32661;
	private final static int REWARDER_FIVE = 32663;
	private final static int REWARDER_SIX = 32662;
	private final static int START_NPC = 32484;
	private int x;
	private int y;
	private int z;

	public DelusionTeleport(int questId, String name, String descr)
	{
		super(questId, name, descr);
			addStartNpc(START_NPC);
			addTalkId(START_NPC);
			addTalkId(REWARDER_ONE);
			addTalkId(REWARDER_TWO);
			addTalkId(REWARDER_THREE);
			addTalkId(REWARDER_FOUR);
			addTalkId(REWARDER_FIVE);
			addTalkId(REWARDER_SIX);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());

		int npcId = npc.getId();

		if (npcId == START_NPC)
		{    	x = player.getX();  
			y = player.getY();
			z = player.getZ();
			player.teleToLocation(-114592,-152509,-6723);
			if (player.hasSummon())
			{
				player.getSummon().teleToLocation(-114592,-152509,-6723);
			}	
		}
		else if (npcId == REWARDER_ONE || npcId == REWARDER_TWO || npcId == REWARDER_THREE || npcId == REWARDER_FOUR || npcId == REWARDER_FIVE || npcId == REWARDER_SIX)
		{	
			player.teleToLocation(x,y,z);
			if (player.hasSummon())
			{
				player.getSummon().teleToLocation(x,y,z);
			}	
			st.exitQuest(true);
		} 
		return "";
	}
	public static void main(String[] args)
	{
		new DelusionTeleport(-1, "DelusionTeleport", "teleports");
	}
}