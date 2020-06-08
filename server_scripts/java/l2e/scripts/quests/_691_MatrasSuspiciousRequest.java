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

import java.util.Map;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Created by LordWinter 02.03.2011 Based on L2J Eternity-World
 */
public class _691_MatrasSuspiciousRequest extends Quest
{
	private static final String qn = "_691_MatrasSuspiciousRequest";
	
	// NPCs
	private static final int MATRAS = 32245;
	long item_cou = 0;
	
	// ITEMs
	private static final int DYNASTIC_ESSENCE_II = 10413;
	private static final int RED_STONE = 10372;
	
	private static final Map<Integer, Integer> REWARD_CHANCES = new FastMap<>();
	
	static
	{
		REWARD_CHANCES.put(22363, 890);
		REWARD_CHANCES.put(22364, 261);
		REWARD_CHANCES.put(22365, 560);
		REWARD_CHANCES.put(22366, 560);
		REWARD_CHANCES.put(22367, 190);
		REWARD_CHANCES.put(22368, 129);
		REWARD_CHANCES.put(22369, 210);
		REWARD_CHANCES.put(22370, 787);
		REWARD_CHANCES.put(22371, 257);
		REWARD_CHANCES.put(22372, 656);
	}
	
	public _691_MatrasSuspiciousRequest(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MATRAS);
		addTalkId(MATRAS);
		
		for (int npcId : REWARD_CHANCES.keySet())
		{
			addKillId(npcId);
		}
		
		questItemIds = new int[]
		{
			RED_STONE
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		item_cou = st.getQuestItemsCount(RED_STONE);
		
		if (event.equalsIgnoreCase("32245-04.htm"))
		{
			if (player.getLevel() >= 76)
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("take_reward"))
		{
			if (item_cou >= 744)
			{
				st.takeItems(RED_STONE, 744);
				st.giveItems(DYNASTIC_ESSENCE_II, 1);
				htmltext = "32245-09.htm";
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				
				html.setFile(player.getLang(), "32245-06.htm");
				html.replace("%itemcount%", Long.toString(item_cou));
			}
		}
		else if (event.equalsIgnoreCase("32245-08.htm"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			
			html.setFile(player.getLang(), "32245-08.htm");
			html.replace("%itemcount%", Long.toString(item_cou));
		}
		else if (event.equalsIgnoreCase("32245-12.htm"))
		{
			st.giveItems(57, (item_cou * 10000));
			st.takeItems(RED_STONE, item_cou);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (st.getState() == State.CREATED)
		{
			if (player.getLevel() >= 76)
			{
				htmltext = "32245-01.htm";
			}
			else
			{
				htmltext = "32245-03.htm";
			}
		}
		else if (st.getState() == State.STARTED)
		{
			item_cou = st.getQuestItemsCount(RED_STONE);
			if (item_cou > 0)
			{
				htmltext = "32245-05.htm";
			}
			else if (item_cou == 0)
			{
				htmltext = "32245-06.htm";
			}
			else if (item_cou > 0)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				
				html.setFile(player.getLang(), "32245-06.htm");
				html.replace("%itemcount%", Long.toString(item_cou));
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance pl = getRandomPartyMember(player, 1);
		if (pl == null)
		{
			return null;
		}
		
		final QuestState st = pl.getQuestState(qn);
		
		int chance = (int) (Config.RATE_QUEST_DROP * REWARD_CHANCES.get(npc.getId()));
		int numItems = Math.max((chance / 1000), 1);
		chance = chance % 1000;
		
		if (getRandom(1000) <= chance)
		{
			st.giveItems(RED_STONE, numItems);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _691_MatrasSuspiciousRequest(691, qn, "");
	}
}