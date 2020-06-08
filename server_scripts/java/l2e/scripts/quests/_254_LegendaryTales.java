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
import l2e.gameserver.util.Util;

public class _254_LegendaryTales extends Quest
{
	private static final String qn = "_254_LegendaryTales";

	private static final int GILMORE = 30754;

	private static final int LARGE_DRAGON_SKULL = 17249;

	public enum Bosses
	{
		EMERALD_HORN(25718),
		DUST_RIDER(25719),
		BLEEDING_FLY(25720),
		BLACK_DAGGER(25721),
		SHADOW_SUMMONER(25722),
		SPIKE_SLASHER(25723),
		MUSCLE_BOMBER(25724);
		
		private final int _bossId;
		private final int _mask;
		
		private Bosses(int bossId)
		{
			_bossId = bossId;
			_mask = 1 << ordinal();
		}
		
		public int getId()
		{
			return _bossId;
		}
		
		public int getMask()
		{
			return _mask;
		}
		
		public static Bosses valueOf(int npcId)
		{
			for (Bosses val : values())
			{
				if (val.getId() == npcId)
				{
					return val;
				}
			}
			return null;
		}
	}

	private static final int[] BOSS = 
	{
		Bosses.EMERALD_HORN.getId(), Bosses.DUST_RIDER.getId(), Bosses.BLEEDING_FLY.getId(), 
		Bosses.BLACK_DAGGER.getId(), Bosses.SHADOW_SUMMONER.getId(), Bosses.SPIKE_SLASHER.getId(), 
		Bosses.MUSCLE_BOMBER.getId()
	};

	private static final int[] REWARDS = 
	{
		0,
		13457,
		13458,
		13459,
		13460,
		13461,
		13462,
		13463,
		13464,
		13465,
		13466,
		13467
	};

	public _254_LegendaryTales(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(GILMORE);
		addTalkId(GILMORE);

		addKillId(BOSS);

		questItemIds = new int[]
		{
			LARGE_DRAGON_SKULL
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

		if (npc.getId() == GILMORE)
		{
			if (event.equalsIgnoreCase("accept"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				htmltext = "30754-07.htm";
			}
			else if (event.equalsIgnoreCase("emerald"))
			{
				htmltext = (checkMask(st, Bosses.EMERALD_HORN) ? "30754-22.htm" : "30754-16.htm");
			}
			else if (event.equalsIgnoreCase("dust"))
			{
				htmltext = (checkMask(st, Bosses.DUST_RIDER) ? "30754-23.htm" : "30754-17.htm");
			}
			else if (event.equalsIgnoreCase("bleeding"))
			{
				htmltext = (checkMask(st, Bosses.BLEEDING_FLY) ? "30754-24.htm" : "30754-18.htm");
			}
			else if (event.equalsIgnoreCase("daggerwyrm"))
			{
				htmltext = (checkMask(st, Bosses.BLACK_DAGGER) ? "30754-25.htm" : "30754-19.htm");
			}
			else if (event.equalsIgnoreCase("shadowsummoner"))
			{
				htmltext = (checkMask(st, Bosses.SHADOW_SUMMONER) ? "30754-26.htm" : "30754-16.htm");
			}
			else if (event.equalsIgnoreCase("spikeslasher"))
			{
				htmltext = (checkMask(st, Bosses.SPIKE_SLASHER) ? "30754-27.htm" : "30754-17.htm");
			}
			else if (event.equalsIgnoreCase("muclebomber"))
			{
				htmltext = (checkMask(st, Bosses.MUSCLE_BOMBER) ? "30754-28.htm" : "30754-18.htm");
			}
			else if (Util.isDigit(event))
			{
				final int reward_id = Integer.parseInt(event);
				if (reward_id > 0)
				{
					if (st.getQuestItemsCount(LARGE_DRAGON_SKULL) == 7)
					{
						int REWARD = REWARDS[reward_id];
						
						st.takeItems(LARGE_DRAGON_SKULL, 7);
						st.giveItems(REWARD, 1);
						htmltext = "30754-13.htm";
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
					else
					{
						htmltext = "30754-12.htm";
					}
				}
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
			return htmltext;
		}

		if (npc.getId() == GILMORE)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() < 80)
					{
						htmltext = "30754-03.htm";
					}
					else
					{
						htmltext = "30754-01.htm";
					}
					break;
				case State.STARTED:
					if (st.isCond(1))
					{
						htmltext = "30754-09.htm";
					}
					else if (st.isCond(2))
					{
						htmltext = "30754-10.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = "30754-02.htm";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
        	if (st == null)
		{
			return null;
		}
		
		if (player.isInParty())
		{
			for(L2PcInstance memb : player.getParty().getMembers())
			{
				rewardPlayer(npc, memb);
			}
		}
		else
		{
			rewardPlayer(npc, player);
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);

		if (st != null && st.isCond(1))
		{
			int raids = st.getInt("raids");
			Bosses boss = Bosses.valueOf(npc.getId());
			
			if (!checkMask(st, boss))
			{
				st.set("raids", raids | boss.getMask());
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				
				if (st.getQuestItemsCount(LARGE_DRAGON_SKULL) < 7)
				{
					st.playSound("Itemsound.quest_itemget");
				}
				else
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
	}

	private static boolean checkMask(QuestState st, Bosses boss)
	{
		int pos = boss.getMask();
		return ((st.getInt("raids") & pos) == pos);
	}

	public static void main(String[] args)
	{
		new _254_LegendaryTales(254, qn, "");
	}
}