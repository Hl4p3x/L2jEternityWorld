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
public class _615_MagicalPowerOfFirePart1 extends Quest
{
  	private static final String qn = "_615_MagicalPowerOfFirePart1";

  	private static final int NARAN = 31378;
  	private static final int UDAN = 31379;
  	private static final int ASEFA_BOX = 31559;

  	private static final int EYE = 31684;

  	private static final int KEY = 1661;
  	private static final int STOLEN_RED_TOTEM = 7242;
  	private static final int RED_TOTEM = 7243;
  	private static final int DIVINE_STONE = 7081;

	private static final int[] VARKA_MARKS =
	{
		7221,
		7222,
		7223,
		7224,
		7225
	};

  	public _615_MagicalPowerOfFirePart1(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(NARAN);
    		addTalkId(NARAN, UDAN, ASEFA_BOX);

    		addAggroRangeEnterId(21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362, 21369, 21370, 21364, 21365, 21366, 21368, 21371, 21372, 21373, 21374, 21375);

		questItemIds = new int[] { STOLEN_RED_TOTEM };
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

    		if (event.equalsIgnoreCase("31378-03.htm"))
    		{
      			st.set("cond", "1");
      			st.set("spawned", "0");
      			st.setState(State.STARTED);
      			st.playSound("ItemSound.quest_accept");
    		}
    		else if (event.equalsIgnoreCase("31559-03.htm"))
    		{
      			if (st.getInt("spawned") == 1)
      			{
        			htmltext = "31559-04.htm";
      			}
      			else if (st.getQuestItemsCount(KEY) == 0)
      			{
        			htmltext = "31559-02.htm";
      			}
      			else
      			{
        			st.set("cond", "3");
        			st.takeItems(KEY, 1);
        			st.giveItems(STOLEN_RED_TOTEM, 1);
        			st.playSound("ItemSound.quest_itemget");
      			}
    		}
    		else if (event.equalsIgnoreCase("UdanEyeDespawn"))
    		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.UDAN_HAS_ALREADY_SEEN_YOUR_FACE));
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
      				if ((player.getLevel() >= 74) && (hasAtLeastOneQuestItem(player, VARKA_MARKS)))
      				{
        				htmltext = "31378-01.htm";
      				}
      				else
      				{
        				htmltext = "31378-02.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				int cond = st.getInt("cond");
      				switch (npc.getId())
      				{
      					case NARAN:
        					htmltext = "31378-04.htm";
        					break;
      					case UDAN:
        					if (cond == 1)
        					{
          						htmltext = "31379-01.htm";
          						st.set("cond", "2");
          						st.playSound("ItemSound.quest_middle");
        					}
        					else if (cond == 2)
        					{
          						if (st.getInt("spawned") == 0)
          						{
            							htmltext = "31379-02.htm";
          						}
          						else
          						{
            							htmltext = "31379-03.htm";
            							st.set("spawned", "0");
            							st.playSound("ItemSound.quest_middle");
          						}
        					}
        					else if ((cond == 3) && (st.getQuestItemsCount(STOLEN_RED_TOTEM) >= 1))
        					{
          						htmltext = "31379-04.htm";
          						st.takeItems(STOLEN_RED_TOTEM, 1);
          						st.giveItems(RED_TOTEM, 1);
          						st.giveItems(DIVINE_STONE, 1);
          						st.unset("spawned");
          						st.playSound("ItemSound.quest_finish");
          						st.exitQuest(true);
						}
						break;
      					case ASEFA_BOX:
        					if (cond == 2)
        					{
          						htmltext = "31559-01.htm";
        					}
        					else if (cond == 3)
        					{
          						htmltext = "31559-05.htm";
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

      			L2Npc udanEye = st.addSpawn(EYE, xx, yy, zz, 10000);
      			if (udanEye != null)
      			{
        			startQuestTimer("UdanEyeDespawn", 9000L, udanEye, player, false);
				udanEye.broadcastPacket(new NpcSay(udanEye.getObjectId(), 0, udanEye.getId(), NpcStringId.YOU_CANT_AVOID_THE_EYES_OF_UDAN));
        			st.playSound("ItemSound.quest_giveup");
      			}
    		}
    		return null;
  	}

  	public static void main(String[] args)
  	{
    		new _615_MagicalPowerOfFirePart1(615, qn, "");
  	}
}