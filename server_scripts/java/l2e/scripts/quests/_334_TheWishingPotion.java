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
import l2e.util.Rnd;

/**
 * Created by LordWinter 06.04.2013 Based on L2J Eternity-World
 */
public class _334_TheWishingPotion extends Quest
{
	private static final String qn = "_334_TheWishingPotion";
	
	private static final int GRIMA = 27135;
	private static final int SUCCUBUS_OF_SEDUCTION = 27136;
	private static final int GREAT_DEMON_KING = 27138;
	private static final int SECRET_KEEPER_TREE = 27139;
	private static final int SANCHES = 27153;
	private static final int BONAPARTERIUS = 27154;
	private static final int RAMSEBALIUS = 27155;
	private static final int TORAI = 30557;
	private static final int ALCHEMIST_MATILD = 30738;
	private static final int RUPINA = 30742;
	private static final int WISDOM_CHEST = 30743;
	
	private static final int WHISPERING_WIND = 20078;
	private static final int ANT_SOLDIER = 20087;
	private static final int ANT_WARRIOR_CAPTAIN = 20088;
	private static final int SILENOS = 20168;
	private static final int TYRANT = 20192;
	private static final int TYRANT_KINGPIN = 20193;
	private static final int AMBER_BASILISK = 20199;
	private static final int HORROR_MIST_RIPPER = 20227;
	private static final int TURAK_BUGBEAR = 20248;
	private static final int TURAK_BUGBEAR_WARRIOR = 20249;
	private static final int GLASS_JAGUAR = 20250;
	
	private static final int DEMONS_TUNIC_ID = 441;
	private static final int DEMONS_STOCKINGS_ID = 472;
	private static final int SCROLL_OF_ESCAPE_ID = 736;
	private static final int NECKLACE_OF_GRACE_ID = 931;
	private static final int SPELLBOOK_ICEBOLT_ID = 1049;
	private static final int SPELLBOOK_BATTLEHEAL_ID = 1050;
	private static final int DEMONS_BOOTS_ID = 2435;
	private static final int DEMONS_GLOVES_ID = 2459;
	private static final int WISH_POTION_ID = 3467;
	private static final int ANCIENT_CROWN_ID = 3468;
	private static final int CERTIFICATE_OF_ROYALTY_ID = 3469;
	private static final int GOLD_BAR_ID = 3470;
	private static final int ALCHEMY_TEXT_ID = 3678;
	private static final int SECRET_BOOK_ID = 3679;
	private static final int POTION_RECIPE_1_ID = 3680;
	private static final int POTION_RECIPE_2_ID = 3681;
	private static final int MATILDS_ORB_ID = 3682;
	private static final int FORBIDDEN_LOVE_SCROLL_ID = 3683;
	private static final int HEART_OF_PAAGRIO_ID = 3943;
	
	private static final int AMBER_SCALE_ID = 3684;
	private static final int WIND_SOULSTONE_ID = 3685;
	private static final int GLASS_EYE_ID = 3686;
	private static final int HORROR_ECTOPLASM_ID = 3687;
	private static final int SILENOS_HORN_ID = 3688;
	private static final int ANT_SOLDIER_APHID_ID = 3689;
	private static final int TYRANTS_CHITIN_ID = 3690;
	private static final int BUGBEAR_BLOOD_ID = 3691;
	
	private static final int DROP_CHANCE_FORBIDDEN_LOVE_SCROLL_ID = 3;
	private static final int DROP_CHANCE_NECKLACE_OF_GRACE_ID = 4;
	private static final int DROP_CHANCE_GOLD_BAR_ID = 10;
	
	private static final int[][] DROPLIST_COND =
	{
		{
			1,
			2,
			SECRET_KEEPER_TREE,
			0,
			SECRET_BOOK_ID,
			1,
			1000000,
			1
		},
		{
			3,
			0,
			AMBER_BASILISK,
			0,
			AMBER_SCALE_ID,
			1,
			150000,
			1
		},
		{
			3,
			0,
			WHISPERING_WIND,
			0,
			WIND_SOULSTONE_ID,
			1,
			200000,
			1
		},
		{
			3,
			0,
			GLASS_JAGUAR,
			0,
			GLASS_EYE_ID,
			1,
			350000,
			1
		},
		{
			3,
			0,
			HORROR_MIST_RIPPER,
			0,
			HORROR_ECTOPLASM_ID,
			1,
			150000,
			1
		},
		{
			3,
			0,
			SILENOS,
			0,
			SILENOS_HORN_ID,
			1,
			300000,
			1
		},
		{
			3,
			0,
			ANT_SOLDIER,
			0,
			ANT_SOLDIER_APHID_ID,
			1,
			400000,
			1
		},
		{
			3,
			0,
			ANT_WARRIOR_CAPTAIN,
			0,
			ANT_SOLDIER_APHID_ID,
			1,
			400000,
			1
		},
		{
			3,
			0,
			TYRANT,
			0,
			TYRANTS_CHITIN_ID,
			1,
			500000,
			1
		},
		{
			3,
			0,
			TYRANT_KINGPIN,
			0,
			TYRANTS_CHITIN_ID,
			1,
			500000,
			1
		},
		{
			3,
			0,
			TURAK_BUGBEAR,
			0,
			BUGBEAR_BLOOD_ID,
			1,
			150000,
			1
		},
		{
			3,
			0,
			TURAK_BUGBEAR_WARRIOR,
			0,
			BUGBEAR_BLOOD_ID,
			1,
			250000,
			1
		}
	};
	
	public _334_TheWishingPotion(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ALCHEMIST_MATILD);
		addTalkId(ALCHEMIST_MATILD);
		addTalkId(TORAI);
		addTalkId(WISDOM_CHEST);
		addTalkId(RUPINA);
		
		for (int[] array : DROPLIST_COND)
		{
			addKillId(array[2]);
		}
		
		questItemIds = new int[]
		{
			ALCHEMY_TEXT_ID,
			SECRET_BOOK_ID,
			AMBER_SCALE_ID,
			WIND_SOULSTONE_ID,
			GLASS_EYE_ID,
			HORROR_ECTOPLASM_ID,
			SILENOS_HORN_ID,
			ANT_SOLDIER_APHID_ID,
			TYRANTS_CHITIN_ID,
			BUGBEAR_BLOOD_ID
		};
	}
	
	public boolean checkIngr(QuestState st)
	{
		if ((st.getQuestItemsCount(AMBER_SCALE_ID) == 1) && (st.getQuestItemsCount(WIND_SOULSTONE_ID) == 1) && (st.getQuestItemsCount(GLASS_EYE_ID) == 1) && (st.getQuestItemsCount(HORROR_ECTOPLASM_ID) == 1) && (st.getQuestItemsCount(SILENOS_HORN_ID) == 1) && (st.getQuestItemsCount(ANT_SOLDIER_APHID_ID) == 1) && (st.getQuestItemsCount(TYRANTS_CHITIN_ID) == 1) && (st.getQuestItemsCount(BUGBEAR_BLOOD_ID) == 1))
		{
			st.set("cond", "4");
			return true;
		}
		st.set("cond", "3");
		return false;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		String htmltext = event;
		if (event.equalsIgnoreCase("30738-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.giveItems(ALCHEMY_TEXT_ID, 1);
		}
		else if (event.equalsIgnoreCase("30738-06.htm"))
		{
			if (st.getQuestItemsCount(WISH_POTION_ID) == 0)
			{
				st.takeItems(ALCHEMY_TEXT_ID, -1);
				st.takeItems(SECRET_BOOK_ID, -1);
				if (st.getQuestItemsCount(POTION_RECIPE_1_ID) == 0)
				{
					st.giveItems(POTION_RECIPE_1_ID, 1);
				}
				if (st.getQuestItemsCount(POTION_RECIPE_2_ID) == 0)
				{
					st.giveItems(POTION_RECIPE_2_ID, 1);
				}
				if (st.getQuestItemsCount(MATILDS_ORB_ID) == 0)
				{
					htmltext = "30738-06.htm";
				}
				else
				{
					htmltext = "30738-12.htm";
				}
				st.set("cond", "3");
			}
			else if ((st.getQuestItemsCount(MATILDS_ORB_ID) >= 1) && (st.getQuestItemsCount(WISH_POTION_ID) >= 1))
			{
				htmltext = "30738-13.htm";
			}
		}
		else if (event.equalsIgnoreCase("30738-10.htm"))
		{
			if (checkIngr(st))
			{
				st.playSound("ItemSound.quest_finish");
				st.takeItems(ALCHEMY_TEXT_ID, -1);
				st.takeItems(SECRET_BOOK_ID, -1);
				st.takeItems(POTION_RECIPE_1_ID, -1);
				st.takeItems(POTION_RECIPE_2_ID, -1);
				st.takeItems(AMBER_SCALE_ID, -1);
				st.takeItems(WIND_SOULSTONE_ID, -1);
				st.takeItems(GLASS_EYE_ID, -1);
				st.takeItems(HORROR_ECTOPLASM_ID, -1);
				st.takeItems(SILENOS_HORN_ID, -1);
				st.takeItems(ANT_SOLDIER_APHID_ID, -1);
				st.takeItems(TYRANTS_CHITIN_ID, -1);
				st.takeItems(BUGBEAR_BLOOD_ID, -1);
				if (st.getQuestItemsCount(MATILDS_ORB_ID) == 0)
				{
					st.giveItems(MATILDS_ORB_ID, 1);
				}
				st.giveItems(WISH_POTION_ID, 1);
				st.set("cond", "0");
			}
			else
			{
				htmltext = "<html><head><body>You don't have required items</body></html>";
			}
		}
		else if (event.equalsIgnoreCase("30738-14.htm"))
		{
			if (st.getQuestItemsCount(WISH_POTION_ID) >= 1)
			{
				htmltext = "30738-15.htm";
			}
		}
		else if (event.equalsIgnoreCase("30738-16.htm"))
		{
			if (st.getQuestItemsCount(WISH_POTION_ID) >= 1)
			{
				st.takeItems(WISH_POTION_ID, 1);
				if (Rnd.chance(50))
				{
					st.addSpawn(SUCCUBUS_OF_SEDUCTION);
					st.addSpawn(SUCCUBUS_OF_SEDUCTION);
					st.addSpawn(SUCCUBUS_OF_SEDUCTION);
				}
				else
				{
					st.addSpawn(RUPINA);
				}
			}
			else
			{
				htmltext = "30738-14.htm";
			}
		}
		else if (event.equalsIgnoreCase("30738-17.htm"))
		{
			if (st.getQuestItemsCount(WISH_POTION_ID) >= 1)
			{
				st.takeItems(WISH_POTION_ID, 1);
				int WISH_CHANCE = getRandom(100) + 1;
				if (WISH_CHANCE <= 33)
				{
					st.addSpawn(GRIMA);
					st.addSpawn(GRIMA);
					st.addSpawn(GRIMA);
				}
				else if (WISH_CHANCE >= 66)
				{
					st.giveItems(57, 10000);
				}
				else if (Rnd.chance(2))
				{
					st.giveItems(57, ((getRandom(10) + 1) * 1000000L));
				}
				else
				{
					st.addSpawn(GRIMA);
					st.addSpawn(GRIMA);
					st.addSpawn(GRIMA);
				}
			}
			else
			{
				htmltext = "30738-14.htm";
			}
			
		}
		else if (event.equalsIgnoreCase("30738-18.htm"))
		{
			if (st.getQuestItemsCount(WISH_POTION_ID) >= 1)
			{
				st.takeItems(WISH_POTION_ID, 1);
				int WISH_CHANCE = getRandom(100) + 1;
				if (WISH_CHANCE <= 33)
				{
					st.giveItems(CERTIFICATE_OF_ROYALTY_ID, 1);
				}
				else if (WISH_CHANCE >= 66)
				{
					st.giveItems(ANCIENT_CROWN_ID, 1);
				}
				else
				{
					st.addSpawn(SANCHES);
				}
			}
			else
			{
				htmltext = "30738-14.htm";
			}
			
		}
		else if (event.equalsIgnoreCase("30738-19.htm"))
		{
			if (st.getQuestItemsCount(WISH_POTION_ID) >= 1)
			{
				st.takeItems(WISH_POTION_ID, 1);
				int WISH_CHANCE = getRandom(100) + 1;
				if (WISH_CHANCE <= 33)
				{
					st.giveItems(SPELLBOOK_ICEBOLT_ID, 1);
				}
				else if (WISH_CHANCE <= 66)
				{
					st.giveItems(SPELLBOOK_BATTLEHEAL_ID, 1);
				}
				else
				{
					st.addSpawn(WISDOM_CHEST);
				}
			}
			else
			{
				htmltext = "30738-14.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		int npcId = npc.getId();
		String htmltext = getNoQuestMsg(player);
		int id = st.getState();
		int cond = 0;
		if (id != State.CREATED)
		{
			cond = st.getInt("cond");
		}
		if (npcId == ALCHEMIST_MATILD)
		{
			if (cond == 0)
			{
				if (player.getLevel() <= 29)
				{
					htmltext = "30738-21.htm";
					st.exitQuest(true);
				}
				else if (st.getQuestItemsCount(MATILDS_ORB_ID) == 0)
				{
					htmltext = "30738-01.htm";
				}
				else if (st.getQuestItemsCount(WISH_POTION_ID) == 0)
				{
					st.set("cond", "3");
					if (st.getQuestItemsCount(POTION_RECIPE_1_ID) == 0)
					{
						st.giveItems(POTION_RECIPE_1_ID, 1);
					}
					if (st.getQuestItemsCount(POTION_RECIPE_2_ID) == 0)
					{
						st.giveItems(POTION_RECIPE_2_ID, 1);
					}
					htmltext = "30738-12.htm";
				}
				else
				{
					htmltext = "30738-11.htm";
				}
			}
			else if ((cond == 1) && (st.getQuestItemsCount(ALCHEMY_TEXT_ID) == 1))
			{
				htmltext = "30738-04.htm";
			}
			else if (cond == 2)
			{
				if ((st.getQuestItemsCount(SECRET_BOOK_ID) == 1) && (st.getQuestItemsCount(ALCHEMY_TEXT_ID) == 1))
				{
					htmltext = "30738-05.htm";
				}
			}
			else if (cond == 4)
			{
				if (checkIngr(st))
				{
					htmltext = "30738-08.htm";
				}
				else
				{
					htmltext = "30738-07.htm";
				}
			}
		}
		else if (npcId == TORAI)
		{
			if (st.getQuestItemsCount(FORBIDDEN_LOVE_SCROLL_ID) >= 1)
			{
				st.takeItems(FORBIDDEN_LOVE_SCROLL_ID, 1);
				st.giveItems(57, 500000);
				htmltext = "30557-01.htm";
			}
			else
			{
				htmltext = getNoQuestMsg(player);
			}
		}
		else if (npcId == WISDOM_CHEST)
		{
			int DROP_CHANCE = getRandom(100) + 1;
			if (DROP_CHANCE <= 20)
			{
				st.giveItems(SPELLBOOK_ICEBOLT_ID, 1);
				st.giveItems(SPELLBOOK_BATTLEHEAL_ID, 1);
				player.getTarget().decayMe();
				htmltext = "30743-06.htm";
			}
			else if (DROP_CHANCE <= 30)
			{
				st.giveItems(HEART_OF_PAAGRIO_ID, 1);
				player.getTarget().decayMe();
				htmltext = "30743-06.htm";
			}
			else
			{
				player.getTarget().decayMe();
				htmltext = "30743-0" + String.valueOf(getRandom(5) + 1) + ".htm";
			}
		}
		else if (npcId == RUPINA)
		{
			int DROP_CHANCE = getRandom(100) + 1;
			
			if (DROP_CHANCE <= DROP_CHANCE_NECKLACE_OF_GRACE_ID)
			{
				st.giveItems(NECKLACE_OF_GRACE_ID, 1);
			}
			else
			{
				st.giveItems(SCROLL_OF_ESCAPE_ID, 1);
			}
			player.getTarget().decayMe();
			htmltext = "30742-01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		if (st.getState() != State.STARTED)
		{
			return null;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		for (int[] element : DROPLIST_COND)
		{
			if ((cond == element[0]) && (npcId == element[2]))
			{
				if ((element[3] == 0) || (st.getQuestItemsCount(element[3]) > 0))
				{
					if (element[5] == 0)
					{
						st.dropItems(element[4], element[7], element[6]);
					}
					else if (st.dropQuestItems(element[4], element[7], element[7], element[5], element[6], true))
					{
						if (cond == 3)
						{
							checkIngr(st);
						}
						if ((element[1] != cond) && (element[1] != 0))
						{
							st.set("cond", String.valueOf(element[1]));
							st.setState(State.STARTED);
						}
					}
				}
			}
		}
		int DROP_CHANCE = getRandom(100) + 1;
		if ((npcId == SUCCUBUS_OF_SEDUCTION) && (DROP_CHANCE <= DROP_CHANCE_FORBIDDEN_LOVE_SCROLL_ID))
		{
			st.playSound("ItemSound.quest_itemget");
			st.giveItems(FORBIDDEN_LOVE_SCROLL_ID, 1);
		}
		else if ((npcId == GRIMA) && (DROP_CHANCE <= DROP_CHANCE_GOLD_BAR_ID))
		{
			st.playSound("ItemSound.quest_itemget");
			st.giveItems(GOLD_BAR_ID, getRandom(5) + 1);
		}
		else if ((npcId == SANCHES) && Rnd.chance(51))
		{
			st.addSpawn(BONAPARTERIUS);
		}
		else if ((npcId == BONAPARTERIUS) && Rnd.chance(51))
		{
			st.addSpawn(RAMSEBALIUS);
		}
		else if ((npcId == RAMSEBALIUS) && Rnd.chance(51))
		{
			st.addSpawn(GREAT_DEMON_KING);
		}
		else if ((npcId == GREAT_DEMON_KING) && Rnd.chance(51))
		{
			if (DROP_CHANCE <= 25)
			{
				st.giveItems(DEMONS_BOOTS_ID, 1);
			}
			else if (DROP_CHANCE <= 50)
			{
				st.giveItems(DEMONS_GLOVES_ID, 1);
			}
			else if (DROP_CHANCE <= 75)
			{
				st.giveItems(DEMONS_STOCKINGS_ID, 1);
			}
			else
			{
				st.giveItems(DEMONS_TUNIC_ID, 1);
			}
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _334_TheWishingPotion(334, qn, "");
	}
}