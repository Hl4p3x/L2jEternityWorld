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
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public final class _184_NikolasCooperationContract extends Quest
{
	private static final String qn = "_184_NikolasCooperationContract";

	private static int Nikola = 30621;
	private static int Lorain = 30673;
	private static int Device = 32366;
	private static int Alarm = 32367;

	private static int Certificate = 10362;
	private static int Metal = 10359;
	private static int BrokenMetal = 10360;
	private static int NicolasMap = 10361;
		
	public _184_NikolasCooperationContract(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addTalkId(Nikola);
		addTalkId(Lorain);
		addTalkId(Device);
		addTalkId(Alarm);

		questItemIds = new int[] { NicolasMap, BrokenMetal, Metal };
	}	

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;	
	
        	if (event.equalsIgnoreCase("30621-01.htm"))
		{
            		if (player.getLevel() < 40)
                		htmltext = "30621-00.htm";
		}		
		else if(event.equalsIgnoreCase("30621-04.htm"))
        	{
			st.set("cond","1");			
            		st.giveItems(NicolasMap,1);
			st.playSound("ItemSound.quest_accept");
        	}
        	else if(event.equalsIgnoreCase("30673-03.htm"))
        	{
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond","2");
            		st.takeItems(NicolasMap,-1);
        	}
        	else if(event.equalsIgnoreCase("30673-05.htm"))
        	{
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond","3");
        	} 
        	else if(event.equalsIgnoreCase("30673-09.htm"))
        	{
            		if (st.getQuestItemsCount(BrokenMetal) > 0)
                		htmltext = "30673-10.htm";
            		else if (st.getQuestItemsCount(Metal) > 0)
                		st.giveItems(Certificate,1);
            		if (player.getLevel() < 50)
               			st.addExpAndSp(203717,14032);
            		st.giveItems(57,72527);
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
        	} 
        	else if(event.equalsIgnoreCase("32366-02.htm"))
        	{
            		L2Npc alarm = st.addSpawn(32367,16491,113563,-9064);
            		st.set("step","1");
            		st.playSound("ItemSound3.sys_siren");
            		startQuestTimer("1",60000, alarm, player);
            		npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.INTRUDER_ALERT_THE_ALARM_WILL_SELF_DESTRUCT_IN_2_MINUTES));
        	}
        	else if(event.equalsIgnoreCase("32366-05.htm"))
        	{
            		st.unset("step");
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond","5");
            		st.giveItems(BrokenMetal,1);
        	}
        	else if(event.equalsIgnoreCase("32366-06.htm"))
        	{
            		st.unset("step");
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond","4");
            		st.giveItems(Metal,1);
        	}
        	else if(event.equalsIgnoreCase("32367-02.htm"))
        	{
            		st.set("pass","0");
        	}
        	else if(event.startsWith("correct"))
        	{
        		st.set("pass",Integer.toString(st.getInt("pass")+1));
            		htmltext = event.substring(8);
            		if (htmltext == "32367-07.htm")
            		{
                		if (st.getInt("pass") == 4)
                		{
                    			st.set("step","3");
                    			cancelQuestTimer("1",npc,player);
                    			cancelQuestTimer("2",npc,player);
                    			cancelQuestTimer("3",npc,player);
                    			cancelQuestTimer("4",npc,player);
                    			st.unset("pass");
                    			npc.deleteMe();
                		}
                		else
                    			htmltext = "32367-06.htm";
            		}
        	}
        	else if(event.equalsIgnoreCase("1"))
        	{
        		npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.THE_ALARM_WILL_SELF_DESTRUCT_IN_60_SECONDS_ENTER_PASSCODE_TO_OVERRIDE));
            		startQuestTimer("2",30000, npc, player);
            		return null;
        	} 
        	else if(event.equalsIgnoreCase("2"))
        	{
        		npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.THE_ALARM_WILL_SELF_DESTRUCT_IN_30_SECONDS_ENTER_PASSCODE_TO_OVERRIDE));
            		startQuestTimer("3",20000, npc, player);
            		return null;
        	}
        	else if(event.equalsIgnoreCase("3"))
        	{
        		npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.THE_ALARM_WILL_SELF_DESTRUCT_IN_10_SECONDS_ENTER_PASSCODE_TO_OVERRIDE));
            		startQuestTimer("4",10000, npc, player);
            		return null;
        	}
        	else if(event.equalsIgnoreCase("4"))
        	{
        		npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.RECORDER_CRUSHED));
            		npc.deleteMe();
            		st.set("step","2");
            		return null;
        	}
        	return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;

        	int npcId = npc.getId();
        	int cond = st.getInt("cond");
		int step = st.getInt("step");	
	
		switch (st.getState())
		{				
			case State.STARTED:
				if (npcId == Nikola)
				{	
					if(cond == 0)
					{
						if (player.getLevel() < 40)
							htmltext = "30621-00.htm";	
						else
							htmltext = "30621-01.htm";							
					}						
					else if(cond == 1)
						htmltext = "30621-05.htm";
				}		
				else if(npcId == Lorain)
				{
					if (cond == 1)
						htmltext = "30673-01.htm";
					else if(cond == 2)
						htmltext = "30673-04.htm";
					else if(cond == 3)
						htmltext = "30673-06.htm";
					else if(cond == 4 || cond == 5)
						htmltext = "30673-07.htm";
				}		
				else if(npcId == Device)
				{
					if (cond == 3)
					{
						if (step == 0)
							htmltext = "32366-01.htm";
						else if(step == 1)
							htmltext = "32366-02.htm";
						else if(step == 2)
							htmltext = "32366-04.htm";
						else if(step == 3)
							htmltext = "32366-03.htm";
					}		
				}			
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}		
	
	public static void main(String[] args)
	{
		new _184_NikolasCooperationContract(184, qn, "");
	}
}