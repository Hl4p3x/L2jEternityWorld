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
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.SocialAction;

/**
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public final class _065_CertifiedSoulBreaker extends Quest
{
    	private static final String qn = "_065_CertifiedSoulBreaker";

    	// NPC
    	private static final int LUCAS = 30071;
    	private static final int JACOB = 30073;
    	private static final int HARLAN = 30074;
    	private static final int XABER = 30075;
    	private static final int LIAM = 30076;
    	private static final int VESA = 30123;
    	private static final int ZEROME = 30124;
    	private static final int FELTON = 30879;
    	private static final int KEKROPUS = 32138;
    	private static final int CASCA = 32139;
    	private static final int HOLST = 32199;
    	private static final int VITUS = 32213;
    	private static final int MELDINA = 32214;
    	private static final int KATENAR = 32242;
    	private static final int CARGOBOX = 32243;

    	private final int[] _talkNpc =
    	{
        	LUCAS, JACOB, HARLAN, XABER, LIAM, VESA, ZEROME, FELTON, KEKROPUS, CASCA, HOLST, MELDINA, CARGOBOX
    	};

    	// MOBS
    	private static final int WYRM = 20176;
    	private static final int ANGEL = 27332;

    	// ITEMS
    	private static final int DOCUMENT = 9803;
    	private static final int HEART = 9804;
    	private static final int RECOMMEND = 9805;
    	private static final int CERTIFICATE = 9806;
    	private static boolean _isSpawnedAngel = false;
    	private static boolean _isSpawnedKatenar = false;
    	public L2Npc _angel;
    	public L2Npc _katenar;

    	private static final int[] QUESTITEMS =
    	{
        	DOCUMENT, HEART, RECOMMEND
    	};

    	public _065_CertifiedSoulBreaker(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(VITUS);
        	for (int npcId : _talkNpc)
        	{
            		addTalkId(npcId);
        	}
        	addFirstTalkId(KATENAR);

        	addKillId(WYRM);
        	addKillId(ANGEL);

        	questItemIds = QUESTITEMS;
    	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

        	if (event.equalsIgnoreCase("32213-03.htm"))
        	{
            		st.playSound("ItemSound.quest_accept");
            		st.set("cond", "1");
            		st.setState(State.STARTED);
        	}
        	else if (event.equalsIgnoreCase("32138-03.htm"))
        	{
            		st.set("cond", "2");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32139-02.htm"))
        	{
            		st.set("cond", "3");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32139-04.htm"))
        	{
            		st.set("cond", "4");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32199-02.htm"))
        	{
            		st.set("cond", "5");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30071-02.htm"))
        	{
            		st.set("cond", "8");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32214-02.htm"))
        	{
            		st.set("cond", "11");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30879-03.htm"))
        	{
            		st.set("cond", "12");
            		st.set("angel", "0");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("angel_cleanup"))
        	{
            		_isSpawnedAngel = false;
        	}
        	else if (event.equalsIgnoreCase("katenar_cleanup"))
        	{
            		_isSpawnedKatenar = false;
        	}
        	else if (event.equalsIgnoreCase("32139-08.htm"))
        	{
            		st.set("cond", "14");
            		st.takeItems(DOCUMENT, 1);
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32138-06.htm"))
        	{
            		st.set("cond", "15");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32138-11.htm"))
        	{
            		st.set("cond", "17");
            		st.takeItems(HEART, -1);
            		st.giveItems(RECOMMEND, 1);
            		st.playSound("ItemSound.quest_middle");
        	}
        	return htmltext;
    	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
            		return null;

        	if (npc.getId() == KATENAR && st.getInt("cond") == 12)
        	{
            		st.unset("angel");
            		st.playSound("ItemSound.quest_itemget");
            		st.set("cond", "13");
            		_isSpawnedAngel = false;
            		_isSpawnedKatenar = false;
            		st.giveItems(DOCUMENT, 1);
            		return "32242-01.htm";
        	}
        	return null;
    	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

        	final int npcId = npc.getId();
        	final int cond = st.getInt("cond");

        	if (st.getState() == State.COMPLETED)
        	{
            		htmltext = getAlreadyCompletedMsg(player);
        	}
        	else if (npcId == VITUS)
        	{
        		if (player.getClassId().getId() != 0x7D || player.getClassId().getId() != 0x7E || player.getLevel() < 39)
            		{
                		htmltext = "32213-00.htm";
                		st.exitQuest(true);
            		}
            		else if (st.getState() != State.CREATED)
            		{
                		htmltext = "32213-01.htm";
            		}
            		else if (cond >= 1 && cond <= 3)
            		{
                		htmltext = "32213-04.htm";
            		}
            		else if (cond >= 4 && cond < 17)
            		{
                		htmltext = "32213-05.htm";
            		}
            		else if (cond == 17 && st.getQuestItemsCount(RECOMMEND) == 1)
            		{
                		htmltext = "32213-06.htm";
                		st.takeItems(RECOMMEND, -1);
                		st.addExpAndSp(196875, 13510);
                		st.giveItems(57, 35597);
                		st.giveItems(CERTIFICATE, 1);
                		player.sendPacket(new SocialAction(player.getObjectId(), 3));
                		st.exitQuest(false);
                		st.playSound("ItemSound.quest_finish");
            		}
        	}
        	else if (npcId == KEKROPUS)
        	{
            		if (cond == 1)
            		{
                		htmltext = "32138-00.htm";
            		}
            		else if (cond == 2)
            		{
                		htmltext = "32138-04.htm";
            		}
            		else if (cond == 14)
            		{
                		htmltext = "32138-05.htm";
            		}
            		else if (cond == 15)
            		{
                		htmltext = "32138-07.htm";
            		}
            		else if (cond == 16)
            		{
                		htmltext = "32138-08.htm";
            		}
            		else if (cond == 17)
            		{
                		htmltext = "32138-12.htm";
            		}
        	}
        	else if (npcId == CASCA)
        	{
            		if (cond == 2)
            		{
               		 	htmltext = "32139-01.htm";
            		}
            		else if (cond == 3)
            		{
                		htmltext = "32139-03.htm";
            		}
            		else if (cond == 4)
            		{
                		htmltext = "32139-05.htm";
            		}
            		else if (cond == 13)
            		{
                		htmltext = "32139-06.htm";
            		}
            		else if (cond == 14)
            		{
                		htmltext = "32139-09.htm";
            		}
        	}
        	else if (npcId == HOLST)
        	{
            		if (cond == 4)
            		{
                		htmltext = "32199-01.htm";
            		}
            		else if (cond == 5)
            		{
                		htmltext = "32199-03.htm";
                		st.set("cond", "6");
                		st.playSound("ItemSound.quest_middle");
            		}
            		else if (cond == 6)
            		{
                		htmltext = "32199-04.htm";
            		}
        	}
        	else if (npcId == HARLAN)
        	{
            		if (cond == 6)
            		{
                		htmltext = "30074-01.htm";
            		}
            		else if (cond == 7)
            		{
                		htmltext = "30074-02.htm";
            		}
        	}
        	else if (npcId == JACOB)
        	{
            		if (cond == 6)
            		{
                		htmltext = "30073-01.htm";
                		st.set("cond", "7");
                		st.playSound("ItemSound.quest_middle");
            		}
            		else if (cond == 7)
            		{
                		htmltext = "30073-02.htm";
            		}
        	}
        	else if (npcId == LUCAS)
        	{
            		if (cond == 7)
            		{
                		htmltext = "30071-01.htm";
            		}
            		else if (cond == 8)
            		{
                		htmltext = "30071-03.htm";
            		}
        	}
        	else if (npcId == XABER)
        	{
            		if (cond == 8)
            		{
                		htmltext = "30075-01.htm";
            		}
            		else if (cond == 9)
            		{
                		htmltext = "30075-02.htm";
            		}
        	}
        	else if (npcId == LIAM)
        	{
            		if (cond == 8)
            		{
                		htmltext = "30076-01.htm";
                		st.set("cond", "9");
                		st.playSound("ItemSound.quest_middle");
            		}
            		else if (cond == 9)
			{
            		    htmltext = "30076-02.htm";
            		}
        	}
        	else if (npcId == ZEROME)
        	{
            		if (cond == 9)
            		{
                		htmltext = "30124-01.htm";
            		}
            		else if (cond == 10)
            		{
                		htmltext = "30124-02.htm";
            		}
        	}
        	else if (npcId == VESA)
        	{
            		if (cond == 9)
            		{
                		htmltext = "30123-01.htm";
                		st.set("cond", "10");
                		st.playSound("ItemSound.quest_middle");
            		}
            		else if (cond == 10)
            		{
                		htmltext = "30123-02.htm";
            		}
        	}
        	else if (npcId == MELDINA)
        	{
            		if (cond == 10)
            		{
                		htmltext = "32214-01.htm";
            		}
            		else if (cond == 11)
            		{
                		htmltext = "32214-03.htm";
            		}
        	}
        	else if (npcId == FELTON)
        	{
            		if (cond == 11)
            		{
                		htmltext = "30879-01.htm";
            		}
            		else if (cond == 12)
            		{
                		htmltext = "30879-04.htm";
            		}
        	}
        	else if (npcId == CARGOBOX)
        	{
            		if (cond == 12)
            		{
                		htmltext = "32243-01.htm";
                		if (st.getInt("angel") == 0 && _isSpawnedAngel == false)
                		{
                    			_angel = st.addSpawn(27332, 36198, 191949, -3728, 180000);
                    			_angel.broadcastNpcSay(player.getName() + "! Step back from the confounded box! I will take it myself!");
                    			_angel.setRunning();
                    			((L2Attackable) _angel).addDamageHate(player, 0, 99999);
                    			_angel.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player, true);
                    			_isSpawnedAngel = true;
                    			startQuestTimer("angel_cleanup", 180000, _angel, player);
                		}
                		else if (st.getInt("angel") == 1 && _isSpawnedKatenar == false)
                		{
                    			_katenar = st.addSpawn(32242, 36110, 191921, -3712, 60000);
                    			_katenar.broadcastNpcSay("I am late!");
                    			_isSpawnedKatenar = true;
                    			startQuestTimer("katenar_cleanup", 60000, _katenar, player);
                    			htmltext = "32243-02.htm";
                		}
            		}
            		else if (cond == 13)
            		{
                		htmltext = "32243-03.htm";
            		}
        	}
        	return htmltext;
    	}

    	@Override
    	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
    	{
        	QuestState st = player.getQuestState(getName());
        	if (st == null)
        	{
            		return null;
        	}

        	int npcId = npc.getId();
        	int cond = st.getInt("cond");

        	if (npcId == ANGEL && cond == 12)
        	{
            		st.set("angel", "1");
            		_isSpawnedAngel = false;
            		npc.broadcastNpcSay("Grr. I've been hit...");
            		if (_isSpawnedKatenar == false)
            		{
                		_katenar = st.addSpawn(32242, 36110, 191921, -3712, 60000);
                		_katenar.broadcastNpcSay("I am late!");
                		_isSpawnedKatenar = true;
                		startQuestTimer("katenar_cleanup", 60000, _katenar, player);
            		}
        	}
        	if (npcId == WYRM && st.getQuestItemsCount(HEART) < 10 && cond == 15 && st.getRandom(100) <= 25)
        	{
            		if (st.getQuestItemsCount(HEART) == 9)
            		{
                		st.giveItems(HEART, 1);
                		st.set("cond", "16");
                		st.playSound("ItemSound.quest_middle");
            		}
            		else
            		{
                		st.giveItems(HEART, 1);
                		st.playSound("ItemSound.quest_itemget");
            		}
        	}
        	return null;
    	}

    	public static void main(String[] args)
    	{
        	new _065_CertifiedSoulBreaker(65, qn, "");
    	}
}