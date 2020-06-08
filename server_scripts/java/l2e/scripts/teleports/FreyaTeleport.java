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

import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

/**
 * Based on L2J Eternity-World
 */
public class FreyaTeleport extends Quest
{
	private final static int NPC = 32734;

	public FreyaTeleport(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(NPC);
		addTalkId(NPC);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (player.getLevel() >= 80)
			player.teleToLocation(-180218, 185923, -10576);
		else
			htmltext = "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "FreyaTeleport.CAN_NOT_TELE") + "</body></html>";

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new FreyaTeleport(-1, "FreyaTeleport", "teleports");
	}
}