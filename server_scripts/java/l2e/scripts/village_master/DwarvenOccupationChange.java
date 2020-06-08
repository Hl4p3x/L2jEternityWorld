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
package l2e.scripts.village_master;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

/**
 * Create by LordWinter 28.14.2013 Based on L2J Eternity-World
 */
public class DwarvenOccupationChange extends Quest
{
	private static final String qn = "DwarvenOccupationChange";
	
	private static final Map<String, int[]> CLASSES1 = new HashMap<>();
	private static final Map<String, int[]> CLASSES2 = new HashMap<>();
	
	private static final int[] BH_NPCS =
	{
		30511,
		30676,
		30685,
		30845,
		30894,
		31269,
		31314,
		31958
	};

	private static final int[] WS_NPCS =
	{
		30512,
		30677,
		30687,
		30847,
		30897,
		31272,
		31317,
		31961
	};

	private static final int[] SCAV_NPCS =
	{
		30503,
		30594,
		30498,
		32092,
		32093,
		32158,
		32171
	};

	private static final int[] ARTI_NPCS =
	{
		30504,
		30595,
		30499,
		32157
	};
	
	private static final int[] BH_MARKS =
	{
		2809,
		3119,
		3238
	};

	private static final int[] WS_MARKS =
	{
		2867,
		3119,
		3238
	};

	private static final int SCAV_MARK = 1642;
	private static final int ARTI_MARK = 1635;
	private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
	
	private static final int[] UNIQUE_DIALOGS =
	{
		30594,
		30595,
		30498,
		30499
	};
	
	static
	{
		CLASSES1.put("BH", new int[]
		{
			30511,
			4,
			0x36,
			0x35,
			0x00,
			0x37,
			0x39,
			0x75,
			0x76,
			BH_MARKS[0],
			BH_MARKS[1],
			BH_MARKS[2],
			40,
			0x37,
			0
		});

		CLASSES1.put("WS", new int[]
		{
			30512,
			4,
			0x38,
			0x35,
			0x00,
			0x37,
			0x39,
			0x75,
			0x76,
			WS_MARKS[0],
			WS_MARKS[1],
			WS_MARKS[2],
			40,
			0x39,
			SHADOW_WEAPON_COUPON_CGRADE
		});

		CLASSES2.put("SC", new int[]
		{
			30503,
			4,
			0x35,
			0x36,
			0x38,
			0x37,
			0x39,
			0x75,
			0x76,
			SCAV_MARK,
			0,
			0,
			20,
			0x36,
			SHADOW_WEAPON_COUPON_DGRADE
		});

		CLASSES2.put("AR", new int[]
		{
			30504,
			4,
			0x35,
			0x36,
			0x38,
			0x37,
			0x39,
			0x75,
			0x76,
			ARTI_MARK,
			0,
			0,
			20,
			0x38,
			SHADOW_WEAPON_COUPON_DGRADE
		});
	}
	
	public DwarvenOccupationChange(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int npcId : BH_NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		
		for (int npcId : WS_NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		
		for (int npcId : SCAV_NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		
		for (int npcId : ARTI_NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		final int npcId = npc.getId();
		final int race = player.getRace().ordinal();
		final int classid = player.getClassId().getId();
		final int level = player.getLevel();
		if (CLASSES1.keySet().contains(event) || CLASSES2.keySet().contains(event))
		{
			Map<String, int[]> CLASSES;
			if (event.equalsIgnoreCase("BH") || event.equalsIgnoreCase("WS"))
			{
				CLASSES = CLASSES1;
			}
			else
			{
				CLASSES = CLASSES2;
			}
			String prefix = CLASSES.get(event)[0] + "-";
			final int intended_race = CLASSES.get(event)[1];
			final int required_class = CLASSES.get(event)[2];
			final int[] required_marks = new int[]
			{
				CLASSES.get(event)[9],
				CLASSES.get(event)[10],
				CLASSES.get(event)[11]
			};
			final int required_level = CLASSES.get(event)[12];
			final int new_class = CLASSES.get(event)[13];
			final int reward = CLASSES.get(event)[14];
			if (ArrayUtils.contains(UNIQUE_DIALOGS, npcId))
			{
				prefix = npcId + "-";
			}
			if ((classid == required_class) && (race == intended_race))
			{
				int marks = 0;
				for (int item : required_marks)
				{
					if (item == 0)
					{
						continue;
					}
					if (st.hasQuestItems(item))
					{
						marks++;
					}
				}
				
				final int lenght = event.equalsIgnoreCase("AR") || event.equalsIgnoreCase("SC") ? 1 : required_marks.length;
				if (level < required_level)
				{
					if (marks < lenght)
					{
						htmltext = prefix + "05.htm";
					}
					else
					{
						htmltext = prefix + "06.htm";
					}
				}
				else
				{
					if (marks < lenght)
					{
						htmltext = prefix + "07.htm";
					}
					else
					{
						for (int item : required_marks)
						{
							if (item == 0)
							{
								continue;
							}
							st.takeItems(item, 1);
						}
						
						if (reward > 0)
						{
							st.giveItems(reward, 15);
						}
						player.setClassId(new_class);
						player.setBaseClass(new_class);
						player.broadcastUserInfo();
						st.playSound("ItemSound.quest_fanfare_2");
						htmltext = prefix + "08.htm";
					}
				}
			}
			else
			{
				htmltext = getNoQuestMsg(player);
			}
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
			st = newQuestState(player);
		}
		
		String key = "";
		final int npcId = npc.getId();
		final int race = player.getRace().ordinal();
		final int classid = player.getClassId().getId();
		if (player.isSubClassActive())
		{
			st.exitQuest(true);
			return htmltext;
		}
		
		if (ArrayUtils.contains(BH_NPCS, npcId))
		{
			key = "BH";
		}
		else if (ArrayUtils.contains(WS_NPCS, npcId))
		{
			key = "WS";
		}
		else if (ArrayUtils.contains(SCAV_NPCS, npcId))
		{
			key = "SC";
		}
		else if (ArrayUtils.contains(ARTI_NPCS, npcId))
		{
			key = "AR";
		}
		
		final Map<String, int[]> CLASSES;
		if (key.equalsIgnoreCase("BH") || key.equalsIgnoreCase("WS"))
		{
			CLASSES = CLASSES1;
		}
		else
		{
			CLASSES = CLASSES2;
		}
		
		if (!key.equalsIgnoreCase(""))
		{
			String prefix = CLASSES.get(key)[0] + "-";
			final int intended_race = CLASSES.get(key)[1];
			final int required_class = CLASSES.get(key)[2];
			final int[] denial1 = new int[]
			{
				CLASSES.get(key)[3],
				CLASSES.get(key)[4]
			};
			final int[] denial2 = new int[]
			{
				CLASSES.get(key)[5],
				CLASSES.get(key)[6],
				CLASSES.get(key)[7],
				CLASSES.get(key)[8]
			};
			if (ArrayUtils.contains(UNIQUE_DIALOGS, npcId))
			{
				prefix = npcId + "-";
			}
			htmltext = prefix + "11.htm";
			if (race == intended_race)
			{
				if (classid == required_class)
				{
					htmltext = prefix + "01.htm";
				}
				else if (ArrayUtils.contains(denial1, classid))
				{
					htmltext = prefix + "09.htm";
					st.exitQuest(true);
				}
				else if (ArrayUtils.contains(denial2, classid))
				{
					htmltext = prefix + "10.htm";
					st.exitQuest(true);
				}
				else
				{
					st.exitQuest(true);
				}
			}
		}
		else
		{
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new DwarvenOccupationChange(-1, qn, "village_master");
	}
}