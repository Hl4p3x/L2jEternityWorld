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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter $ Sigrlinne 14.03.2011 Based on L2J Eternity-World
 */
public class _021_HiddenTruth extends Quest
{
	private static final String qn = "_021_HiddenTruth";
	
	// npcId list:
	public static final int MYSTERIOUS_WIZARD = 31522;
	public static final int TOMBSTONE = 31523;
	public static final int GHOST_OF_VON_HELLMAN_ID = 31524;
	public static final int GHOST_OF_VON_HELLMAN_PAGE_ID = 31525;
	public static final int BROKEN_BOOK_SHELF = 31526;
	public static final int AGRIPEL = 31348;
	public static final int DOMINIC = 31350;
	public static final int BENEDICT = 31349;
	public static final int INNOCENTIN = 31328;
	public static final int[] TALK_NPC =
	{
		MYSTERIOUS_WIZARD,
		TOMBSTONE,
		GHOST_OF_VON_HELLMAN_ID,
		GHOST_OF_VON_HELLMAN_PAGE_ID,
		BROKEN_BOOK_SHELF,
		AGRIPEL,
		DOMINIC,
		BENEDICT,
		INNOCENTIN
	};
	
	// ItemId list:
	public static final int CROSS_OF_EINHASAD = 7140;
	public static final int CROSS_OF_EINHASAD_NEXT_QUEST = 7141;
	
	public L2Npc GHOST_OF_VON_HELLMANS_PAGE;
	public L2Npc GHOST_OF_VON_HELLMAN;
	
	private void spawnGHOST_OF_VON_HELLMANS_PAGE(QuestState st)
	{
		GHOST_OF_VON_HELLMANS_PAGE = st.addSpawn(GHOST_OF_VON_HELLMAN_PAGE_ID, 51462, -54539, -3176, getRandom(0, 20), true, 0);
		NpcSay packet = new NpcSay(GHOST_OF_VON_HELLMANS_PAGE.getObjectId(), Say2.NPC_ALL, GHOST_OF_VON_HELLMANS_PAGE.getId(), NpcStringId.MY_MASTER_HAS_INSTRUCTED_ME_TO_BE_YOUR_GUIDE_S1);
		packet.addStringParameter(st.getPlayer().getName().toString());
		GHOST_OF_VON_HELLMANS_PAGE.broadcastPacket(packet);
	}
	
	private void despawnGHOST_OF_VON_HELLMANS_PAGE(QuestState st)
	{
		if (GHOST_OF_VON_HELLMANS_PAGE != null)
		{
			GHOST_OF_VON_HELLMANS_PAGE.deleteMe();
		}
		GHOST_OF_VON_HELLMANS_PAGE = null;
	}
	
	private void spawnGHOST_OF_VON_HELLMAN(QuestState st)
	{
		GHOST_OF_VON_HELLMAN = st.addSpawn(GHOST_OF_VON_HELLMAN_ID, 51432, -54570, -3136, getRandom(0, 20), false, 0);
		GHOST_OF_VON_HELLMAN.broadcastPacket(new NpcSay(GHOST_OF_VON_HELLMAN.getObjectId(), 0, GHOST_OF_VON_HELLMAN.getId(), NpcStringId.WHO_AWOKE_ME));
	}
	
	private void despawnGHOST_OF_VON_HELLMAN(QuestState st)
	{
		if (GHOST_OF_VON_HELLMAN != null)
		{
			GHOST_OF_VON_HELLMAN.deleteMe();
		}
		GHOST_OF_VON_HELLMAN = null;
	}
	
	public _021_HiddenTruth(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(MYSTERIOUS_WIZARD);
		for (int npcId : TALK_NPC)
		{
			addTalkId(npcId);
		}
		
		questItemIds = new int[]
		{
			CROSS_OF_EINHASAD
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
		
		if (event.equalsIgnoreCase("31522-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("html"))
		{
			htmltext = "31328-05.htm";
		}
		else if (event.equalsIgnoreCase("31328-05.htm"))
		{
			st.unset("cond");
			st.takeItems(CROSS_OF_EINHASAD, 1);
			if (st.getQuestItemsCount(CROSS_OF_EINHASAD_NEXT_QUEST) == 0)
			{
				st.giveItems(CROSS_OF_EINHASAD_NEXT_QUEST, 1);
			}
			st.playSound("ItemSound.quest_finish");
			st.addExpAndSp(131228, 11978);
			st.startQuestTimer("html", 1);
			htmltext = "31328-05a.htm";
			st.setState(State.COMPLETED);
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("31523-03.htm"))
		{
			st.playSound("SkillSound5.horror_02");
			st.set("cond", "2");
			despawnGHOST_OF_VON_HELLMAN(st);
			spawnGHOST_OF_VON_HELLMAN(st);
		}
		else if (event.equalsIgnoreCase("31524-06.htm"))
		{
			st.set("cond", "3");
			despawnGHOST_OF_VON_HELLMANS_PAGE(st);
			spawnGHOST_OF_VON_HELLMANS_PAGE(st);
			startQuestTimer("1", 4000, GHOST_OF_VON_HELLMANS_PAGE, player);
		}
		else if (event.equalsIgnoreCase("31526-03.htm"))
		{
			st.playSound("ItemSound.item_drop_equip_armor_cloth");
		}
		else if (event.equalsIgnoreCase("31526-08.htm"))
		{
			st.playSound("AmdSound.ed_chimes_05");
			st.set("cond", "5");
		}
		else if (event.equalsIgnoreCase("31526-14.htm"))
		{
			st.giveItems(CROSS_OF_EINHASAD, 1);
			st.set("cond", "6");
		}
		else if (event.equalsIgnoreCase("1"))
		{
			GHOST_OF_VON_HELLMANS_PAGE.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(52373, -54296, -3136, 0));
			st.startQuestTimer("2", 5000, GHOST_OF_VON_HELLMANS_PAGE);
		}
		else if (event.equalsIgnoreCase("2"))
		{
			GHOST_OF_VON_HELLMANS_PAGE.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(52451, -52921, -3152, 0));
			st.startQuestTimer("3", 12000, GHOST_OF_VON_HELLMANS_PAGE);
		}
		else if (event.equalsIgnoreCase("3"))
		{
			GHOST_OF_VON_HELLMANS_PAGE.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(51909, -51725, -3125, 0));
			st.startQuestTimer("4", 15000, GHOST_OF_VON_HELLMANS_PAGE);
		}
		else if (event.equalsIgnoreCase("4"))
		{
			GHOST_OF_VON_HELLMANS_PAGE.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(52438, -51240, -3097, 0));
			st.startQuestTimer("5", 5000, GHOST_OF_VON_HELLMANS_PAGE);
		}
		else if (event.equalsIgnoreCase("5"))
		{
			GHOST_OF_VON_HELLMANS_PAGE.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(52143, -51418, -3085, 0));
			return null;
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (player.getLevel() > 54)
				{
					htmltext = "31522-01.htm";
				}
				else
				{
					htmltext = "31522-03.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case MYSTERIOUS_WIZARD:
						if (cond == 1)
						{
							htmltext = "31522-05.htm";
						}
						break;
					case TOMBSTONE:
						switch (cond)
						{
							case 1:
								htmltext = "31523-01.htm";
								break;
							case 2:
							case 3:
								htmltext = "31523-04.htm";
								st.playSound("SkillSound5.horror_02");
								despawnGHOST_OF_VON_HELLMAN(st);
								spawnGHOST_OF_VON_HELLMAN(st);
								break;
						}
						break;
					case GHOST_OF_VON_HELLMAN_ID:
						switch (cond)
						{
							case 2:
								htmltext = "31524-01.htm";
								break;
							case 3:
								htmltext = "31524-07b.htm";
								break;
							case 4:
								htmltext = "31524-07c.htm";
								break;
						}
						break;
					case GHOST_OF_VON_HELLMAN_PAGE_ID:
						switch (cond)
						{
							case 3:
							case 4:
								htmltext = "31525-01.htm";
								
								if (!GHOST_OF_VON_HELLMANS_PAGE.isMoving())
								{
									htmltext = "31525-02.htm";
									if (cond == 3)
									{
										st.set("cond", "4");
									}
								}
								else
								{
									return "31525-01.htm";
								}
								break;
						}
						break;
					case BROKEN_BOOK_SHELF:
						switch (cond)
						{
							case 3:
							case 4:
								htmltext = "31525-01.htm";
								
								if (!GHOST_OF_VON_HELLMANS_PAGE.isMoving())
								{
									despawnGHOST_OF_VON_HELLMANS_PAGE(st);
									despawnGHOST_OF_VON_HELLMAN(st);
									st.set("cond", "5");
									htmltext = "31526-01.htm";
								}
								
								break;
							case 5:
								htmltext = "31526-10.htm";
								st.playSound("AmdSound.ed_chimes_05");
								break;
							case 6:
								htmltext = "31526-15.htm";
								break;
						}
						break;
					case AGRIPEL:
						if (st.getQuestItemsCount(CROSS_OF_EINHASAD) >= 1)
						{
							switch (cond)
							{
								case 6:
									if ((st.getInt("DOMINIC") == 1) && (st.getInt("BENEDICT") == 1))
									{
										htmltext = "31348-02.htm";
										st.set("cond", "7");
									}
									else
									{
										st.set("AGRIPEL", "1");
										htmltext = "31348-0" + getRandom(3) + ".htm";
									}
									break;
								case 7:
									htmltext = "31348-03.htm";
									break;
							}
						}
						break;
					case DOMINIC:
						if (st.getQuestItemsCount(CROSS_OF_EINHASAD) >= 1)
						{
							switch (cond)
							{
								case 6:
									if ((st.getInt("AGRIPEL") == 1) && (st.getInt("BENEDICT") == 1))
									{
										htmltext = "31350-02.htm";
										st.set("cond", "7");
									}
									else
									{
										st.set("DOMINIC", "1");
										htmltext = "31350-0" + getRandom(3) + ".htm";
									}
									break;
								case 7:
									htmltext = "31350-03.htm";
									break;
							}
						}
						break;
					case BENEDICT:
						if (st.getQuestItemsCount(CROSS_OF_EINHASAD) >= 1)
						{
							switch (cond)
							{
								case 6:
									if ((st.getInt("AGRIPEL") == 1) && (st.getInt("DOMINIC") == 1))
									{
										htmltext = "31349-02.htm";
										st.set("cond", "7");
									}
									else
									{
										st.set("BENEDICT", "1");
										htmltext = "31349-0" + getRandom(3) + ".htm";
									}
									break;
								case 7:
									htmltext = "31349-03.htm";
									break;
							}
						}
						break;
					case INNOCENTIN:
						switch (cond)
						{
							case 0:
								htmltext = "31328-06.htm";
								break;
							case 7:
								if (st.getQuestItemsCount(CROSS_OF_EINHASAD) != 0)
								{
									htmltext = "31328-01.htm";
								}
								break;
						}
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _021_HiddenTruth(21, qn, "");
	}
}