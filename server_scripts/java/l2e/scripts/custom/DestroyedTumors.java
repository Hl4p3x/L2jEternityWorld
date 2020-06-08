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
package l2e.scripts.custom;

import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

/**
 * Create by LordWinter 12.11.2012 Based on L2J Eternity-World
 */
public class DestroyedTumors extends Quest
{
	private long warpTimer = 0;
	
	public DestroyedTumors(int id, String name, String desc)
	{
		super(id, name, desc);
		
		addStartNpc(32535);
		addFirstTalkId(32535);
		addTalkId(32535);
		
		addStartNpc(32536);
		addFirstTalkId(32536);
		addTalkId(32536);
		
		warpTimer = System.currentTimeMillis();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if ((world != null) && (world.templateId == 119))
		{
			if (event.equalsIgnoreCase("examine_tumor"))
			{
				if ((player.getParty() == null) || (player.getParty().getLeader() != player))
				{
					htmltext = "32535-2.htm";
				}
				else
				{
					htmltext = "32535-1.htm";
				}
			}
			else if(event.equalsIgnoreCase("showcheckpage"))
			{
				if (player.getInventory().getItemByItemId(13797) == null)
				{
					htmltext = "32535-6.htm";
				}
				else if ((warpTimer + 60000) > System.currentTimeMillis())
				{
					htmltext = "32535-4.htm";
				}
				else if (world.tag <= 0)
				{
					htmltext = "32535-3.htm";
				}
				else
				{
					htmltext = "32535-5a.htm";
				}
			}
		}
		else if ((world != null) && (world.templateId == 120))
		{
			if (event.equalsIgnoreCase("examine_tumor"))
			{
				if ((player.getParty() == null) || (player.getParty().getLeader() != player))
				{
					htmltext = "32535-2.htm";
				}
				else
				{
					htmltext = "32535-1.htm";
				}
			}
			else if(event.equalsIgnoreCase("showcheckpage"))
			{
				if (player.getInventory().getItemByItemId(13797) == null)
				{
					htmltext = "32535-6.htm";
				}
				else if ((warpTimer + 60000) > System.currentTimeMillis())
				{
					htmltext = "32535-4.htm";
				}
				else if (world.tag <= 0)
				{
					htmltext = "32535-3.htm";
				}
				else
				{
					htmltext = "32535-5b.htm";
				}
			}
		}
		else if ((world != null) && (world.templateId == 121))
		{
			if (event.equalsIgnoreCase("examine_tumor"))
			{
				if (npc.getId() == 32536)
				{
					if ((player.getParty() == null) || (player.getParty().getLeader() != player))
					{
						htmltext = "32536-2.htm";
					}
					else
					{
						htmltext = "32536-1.htm";
					}
				}
				if (npc.getId() == 32535)
				{
					if ((player.getParty() == null) || (player.getParty().getLeader() != player))
					{
						htmltext = "32535-2.htm";
					}
					else
					{
						htmltext = "32535-7.htm";
					}
				}
			}
			else if (event.equalsIgnoreCase("showcheckpage"))
			{
				if (player.getInventory().getItemByItemId(13797) == null)
				{
					htmltext = "32535-6.htm";
				}
				else if ((warpTimer + 60000) > System.currentTimeMillis())
				{
					htmltext = "32535-4.htm";
				}
				else if (world.tag <= 0)
				{
					htmltext = "32535-3.htm";
				}
				else
				{
					htmltext = "32535-5.htm";
				}
			}
			else if (event.equalsIgnoreCase("reenter"))
			{
				if ((player.getInventory().getItemByItemId(13797) == null) || (player.getInventory().getItemByItemId(13797).getCount() < 3))
				{
					htmltext = "32535-6.htm";
				}
				else
				{
					htmltext = "32535-8.htm";
				}
			}
		}
		else if ((world != null) && (world.templateId == 122))
		{
			if (event.equalsIgnoreCase("examine_tumor"))
			{
				if (npc.getId() == 32535)
				{
					htmltext = "32535-4.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npc.getId() == 32535)
		{
			return "32535.htm";
		}
		
		if (npc.getId() == 32536)
		{
			return "32536.htm";
		}
		
		return "";
	}
	
	public static void main(String[] args)
	{
		new DestroyedTumors(-1, DestroyedTumors.class.getSimpleName(), "custom");
	}
}