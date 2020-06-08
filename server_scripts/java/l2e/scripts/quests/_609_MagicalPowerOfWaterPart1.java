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

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter 12.01.2013 Based on L2J Eternity-World
 */
public class _609_MagicalPowerOfWaterPart1 extends Quest
{
  	private static final String qn = "_609_MagicalPowerOfWaterPart1";

  	private static final int WAHKAN = 31371;
  	private static final int ASEFA = 31372;
  	private static final int UDAN_BOX = 31561;

  	private static final int EYE = 31685;

  	private static final int KEY = 1661;
  	private static final int STOLEN_GREEN_TOTEM = 7237;
  	private static final int GREEN_TOTEM = 7238;
  	private static final int DIVINE_STONE = 7081;

	private static final int[] KETRA_MARKS =
	{
		7211,
		7212,
		7213,
		7214,
		7215
	};

  	public _609_MagicalPowerOfWaterPart1(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(WAHKAN);
    		addTalkId(WAHKAN, ASEFA, UDAN_BOX);

    		addAggroRangeEnterId(21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362, 21369, 21370, 21364, 21365, 21366, 21368, 21371, 21372, 21373, 21374, 21375);

		questItemIds = new int[] { STOLEN_GREEN_TOTEM };
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

    		if (event.equalsIgnoreCase("31371-03.htm"))
    		{
      			st.set("cond", "1");
      			st.set("spawned", "0");
      			st.setState(State.STARTED);
      			st.playSound("ItemSound.quest_accept");
    		}
    		else if (event.equalsIgnoreCase("31561-03.htm"))
    		{
      			if (st.getInt("spawned") == 1)
      			{
        			htmltext = "31561-04.htm";
      			}
      			else if (st.getQuestItemsCount(KEY) == 0)
      			{
        			htmltext = "31561-02.htm";
      			}
      			else
      			{
        			st.set("cond", "3");
        			st.takeItems(KEY, 1);
        			st.giveItems(STOLEN_GREEN_TOTEM, 1);
        			st.playSound("ItemSound.quest_itemget");
      			}
    		}
    		else if (event.equalsIgnoreCase("AsefaEyeDespawn"))
    		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.ASEFA_HAS_ALREADY_SEEN_YOUR_FACE));
      			return null;
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

    		switch (st.getState())
    		{
    			case State.CREATED:
      				if ((player.getLevel() >= 74) && (hasAtLeastOneQuestItem(player, KETRA_MARKS)))
      				{
        				htmltext = "31371-01.htm";
      				}
      				else
      				{
        				htmltext = "31371-02.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				int cond = st.getInt("cond");
      				switch (npc.getId())
      				{
      					case WAHKAN:
        					htmltext = "31371-04.htm";
        					break;
      					case ASEFA:
        					if (cond == 1)
        					{
          						htmltext = "31372-01.htm";
          						st.set("cond", "2");
          						st.playSound("ItemSound.quest_middle");
        					}
        					else if (cond == 2)
        					{
          						if (st.getInt("spawned") == 0)
          						{
            							htmltext = "31372-02.htm";
          						}
          						else
          						{
            							htmltext = "31372-03.htm";
            							st.set("spawned", "0");
            							st.playSound("ItemSound.quest_middle");
          						}
        					}
        					else if ((cond == 3) && (st.getQuestItemsCount(STOLEN_GREEN_TOTEM) >= 1))
        					{
          						htmltext = "31372-04.htm";
          						st.takeItems(STOLEN_GREEN_TOTEM, 1);
          						st.giveItems(GREEN_TOTEM, 1);
          						st.giveItems(DIVINE_STONE, 1);
          						st.unset("spawned");
          						st.playSound("ItemSound.quest_finish");
          						st.exitQuest(true);
						}
						break;
      					case UDAN_BOX:
        					if (cond == 2)
        					{
          						htmltext = "31561-01.htm";
        					}
        					else if (cond == 3)
        					{
          						htmltext = "31561-05.htm";
        					}
        					break;
      				}
      				break;
    		}
    		return htmltext;
  	}

	@Override
  	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
  	{
    		QuestState st = player.getQuestState(qn);
    		if (st == null)
    		{
      			return null;
    		}

    		if ((st.getInt("spawned") == 0) && (st.getInt("cond") == 2))
    		{
      			st.set("spawned", "1");
                        int xx = player.getX();
                        int yy = player.getY();
                        int zz = player.getZ();

      			L2Npc asefaEye = st.addSpawn(EYE, xx, yy, zz, 10000);
      			if (asefaEye != null)
      			{
        			startQuestTimer("AsefaEyeDespawn", 9000L, asefaEye, player, false);
				asefaEye.broadcastPacket(new NpcSay(asefaEye.getObjectId(), 0, asefaEye.getId(), NpcStringId.YOU_CANT_AVOID_THE_EYES_OF_ASEFA));
        			st.playSound("ItemSound.quest_giveup");
      			}
    		}
    		return null;
  	}

  	public static void main(String[] args)
  	{
    		new _609_MagicalPowerOfWaterPart1(609, qn, "");
  	}
}