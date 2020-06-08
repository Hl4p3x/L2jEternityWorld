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
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class _198_SevenSignEmbryo extends Quest
{
	private static final String qn = "_198_SevenSignEmbryo";

	// NPCs
	private static final int WOOD = 32593;
	private static final int FRANZ = 32597;

	// MOBS
	private static final int SHILENSEVIL1 = 27346;
	private static final int SHILENSEVIL2 = 27399;
	private static final int SHILENSEVIL3 = 27402;

	// ITEMS
	private static final int SCULPTURE = 14360;
	private static final int BRACELET = 15312;
	private static final int AA = 5575;

	// AA reward rate
	private static final int AARATE = 1;

	private boolean ShilensevilOnSpawn = false;

	public _198_SevenSignEmbryo(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(WOOD);
		addTalkId(WOOD);
		addTalkId(FRANZ);

		addKillId(SHILENSEVIL1);
		addKillId(SHILENSEVIL2);
		addKillId(SHILENSEVIL3);

		questItemIds = new int[]
		{
			        SCULPTURE
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

		if (npc.getId() == WOOD)
		{
			if (event.equalsIgnoreCase("32593-02.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getId() == FRANZ)
		{
			if (event.equalsIgnoreCase("32597-05.htm"))
			{
				if (ShilensevilOnSpawn)
				{
				}
				else
				{
					NpcSay ns = new NpcSay(FRANZ, 0, FRANZ, NpcStringId.S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP);
					ns.addStringParameter(player.getAppearance().getVisibleName());
					player.sendPacket(ns);

					L2MonsterInstance monster = (L2MonsterInstance) addSpawn(SHILENSEVIL1, -23656, -9236, -5392, 0, false, 600000, true, npc.getInstanceId());
					monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getId(), NpcStringId.YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM));
					monster.setRunning();
					monster.addDamageHate(player, 0, 999);
					monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
					L2MonsterInstance monster1 = (L2MonsterInstance) addSpawn(SHILENSEVIL2, -23656, -9236, -5392, 0, false, 600000, true, npc.getInstanceId());
					monster1.setRunning();
					monster1.addDamageHate(player, 0, 999);
					monster1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
					L2MonsterInstance monster2 = (L2MonsterInstance) addSpawn(SHILENSEVIL3, -23656, -9236, -5392, 0, false, 600000, true, npc.getInstanceId());
					monster2.setRunning();
					monster2.addDamageHate(player, 0, 999);
					monster2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
					ShilensevilOnSpawn = true;
					startQuestTimer("aiplayer", 30000, npc, player);
				}
			}
			else if (event.equalsIgnoreCase("aiplayer"))
			{
				if (ShilensevilOnSpawn == true)
				{
					npc.setTarget(player);
					npc.doCast(SkillHolder.getInstance().getInfo(1011, 18));
					startQuestTimer("aiplayer", 30000, npc, player);
				}
				else
				{
					cancelQuestTimer("aiplayer", npc, player);
				}
				return "";
			}
			else if (event.equalsIgnoreCase("32597-10.htm"))
			{
				st.set("cond", "3");
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.WE_WILL_BE_WITH_YOU_ALWAYS));
				st.takeItems(SCULPTURE, -1);
				st.playSound("ItemSound.quest_middle");
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

		QuestState fifth = player.getQuestState("_197_SevenSignTheSacredBookOfSeal");

		if (npc.getId() == WOOD)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if ((fifth != null) && (fifth.getState() == State.COMPLETED) && (player.getLevel() >= 79))
					{
						htmltext = "32593-01.htm";
					}
					else
					{
						htmltext = "32593-00.htm";
						st.exitQuest(true);
					}
					break;

				case State.STARTED:
					if ((st.getInt("cond") == 1) || (st.getInt("cond") == 2))
					{
						htmltext = "32593-02.htm";
					}
					else if (st.getInt("cond") == 3)
					{
						htmltext = "32593-04.htm";
						st.giveItems(BRACELET, 1);
						st.giveItems(AA, 1500000 * AARATE);
						st.addExpAndSp(150000000, 15000000);
						st.unset("cond");
						st.setState(State.COMPLETED);
						st.exitQuest(false);
						st.playSound("ItemSound.quest_finish");
					}
					break;
			}
		}
		else if (npc.getId() == FRANZ)
		{
			if (st.getState() == State.STARTED)
			{
				if (st.getInt("cond") == 1)
				{
					htmltext = "32597-01.htm";
				}
				else if (st.getInt("cond") == 2)
				{
					htmltext = "32597-06.htm";
				}
				else if (st.getInt("cond") == 3)
				{
					htmltext = "32597-11.htm";
				}
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
			return super.onKill(npc, player, isSummon);
		}

		if ((npc.getId() == SHILENSEVIL1) && (st.getInt("cond") == 1))
		{
			NpcSay ns = new NpcSay(SHILENSEVIL1, 0, SHILENSEVIL1, NpcStringId.S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU);
			ns.addStringParameter(player.getAppearance().getVisibleName());
			player.sendPacket(ns);

			NpcSay nss = new NpcSay(FRANZ, 0, FRANZ, NpcStringId.WELL_DONE_S1_YOUR_HELP_IS_MUCH_APPRECIATED);
			nss.addStringParameter(player.getAppearance().getVisibleName());
			player.sendPacket(nss);

			st.giveItems(SCULPTURE, 1);
			st.set("cond", "2");
			player.showQuestMovie(14);
			ShilensevilOnSpawn = false;
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _198_SevenSignEmbryo(198, qn, "");
	}
}
