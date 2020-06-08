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
package l2e.scripts.instances;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Created by LordWinter 12.05.2011 Based on L2J Eternity-World
 */
public class JiniasHideout extends Quest
{
	private class JiniasWorld extends InstanceWorld
	{
		public long[] storeTime =
		{
			0,
			0
		};
		public int questId;
		
		public JiniasWorld()
		{
		}
	}
	
	private static final String qn = "JiniasHideout";
	
	private static final int RAFFORTY = 32020;
	private static final int JINIA = 32760;
	private static final int SIRRA = 32762;
	
	private static final int[] ENTRY_POINT =
	{
		-23530,
		-8963,
		-5413
	};
	
	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
	
	public JiniasHideout(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY);
		addTalkId(JINIA);
		
		addSpawnId(SIRRA);
	}
	
	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		if ((player.getLevel() < 82) || (player.getLevel() > 85))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		return true;
	}
	
	protected void exitInstance(L2PcInstance player, Location tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele, false);
	}
	
	protected int enterInstance(L2PcInstance player, String template, teleCoord teleto, int questId)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (!(world instanceof JiniasWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}
		
		if (!checkConditions(player))
		{
			return 0;
		}
		
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
		inst.setSpawnsLoc(new int[]
		{
			player.getX(),
			player.getY(),
			player.getZ()
		});
		world = new JiniasWorld();
		world.instanceId = instanceId;
		((JiniasWorld) world).questId = questId;
		
		switch (questId)
		{
			case 10284:
				world.templateId = 140;
				break;
			case 10285:
				world.templateId = 141;
				break;
			case 10286:
				world.templateId = 145;
				break;
			case 10287:
				world.templateId = 146;
				break;
		}
		world.status = 0;
		((JiniasWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		teleto.instanceId = instanceId;
		teleportplayer(player, teleto);
		world.allowed.add(player.getObjectId());
		return instanceId;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		
		if (event.startsWith("enterInstance_") && (npc.getId() == RAFFORTY))
		{
			int questId = -1;
			String tmpl = null;
			QuestState hostQuest = null;
			try
			{
				System.out.println(event.substring(14));
				questId = Integer.parseInt(event.substring(14));
				
				teleCoord tele = new teleCoord();
				tele.x = ENTRY_POINT[0];
				tele.y = ENTRY_POINT[1];
				tele.z = ENTRY_POINT[2];
				
				switch (questId)
				{
					case 10284:
						hostQuest = player.getQuestState("_10284_AcquisitionOfDivineSword");
						tmpl = "JiniasHideout1.xml";
						htmltext = "10284_failed.htm";
						break;
					case 10285:
						hostQuest = player.getQuestState("_10285_MeetingSirra");
						tmpl = "JiniasHideout2.xml";
						htmltext = "10285_failed.htm";
						break;
					case 10286:
						hostQuest = player.getQuestState("_10286_ReunionWithSirra");
						tmpl = "JiniasHideout2.xml";
						htmltext = "10286_failed.htm";
						break;
					case 10287:
						hostQuest = player.getQuestState("_10287_StoryOfThoseLeft");
						tmpl = "JiniasHideout2.xml";
						htmltext = "10287_failed.htm";
						break;
				}
				
				if ((hostQuest != null) && (hostQuest.getState() == State.STARTED) && (hostQuest.getInt("progress") == 1))
				{
					hostQuest.playSound("ItemSound.quest_middle");
					hostQuest.set("cond", "2");
				}
				
				if (tmpl != null)
				{
					if (enterInstance(player, tmpl, tele, questId) > 0)
					{
						htmltext = null;
					}
				}
			}
			catch (Exception e)
			{
			}
			return null;
		}
		else if (event.equalsIgnoreCase("leaveInstance") && (npc.getId() == JINIA))
		{
			QuestState hostQuest = null;
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
			Location tele = inst.getSpawnLoc();
			exitInstance(player, tele);
			
			switch (((JiniasWorld) world).questId)
			{
				case 10285:
					hostQuest = player.getQuestState("_10285_MeetingSirra");
					break;
				case 10286:
					hostQuest = player.getQuestState("_10286_ReunionWithSirra");
					break;
				case 10287:
					hostQuest = player.getQuestState("_10287_StoryOfThoseLeft");
					break;
			}
			
			if ((hostQuest != null) && (hostQuest.getState() == State.STARTED) && (hostQuest.getInt("progress") == 2))
			{
				switch (((JiniasWorld) world).questId)
				{
					case 10285:
						htmltext = "10285_goodbye.htm";
						break;
					case 10286:
						htmltext = "10286_goodbye.htm";
						hostQuest.playSound("ItemSound.quest_middle");
						hostQuest.set("cond", "5");
						break;
					case 10287:
						htmltext = "10287_goodbye.htm";
						hostQuest.playSound("ItemSound.quest_middle");
						hostQuest.set("cond", "5");
						break;
				}
			}
			return null;
		}
		return htmltext;
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (npc.getId() == SIRRA)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if ((tmpworld != null) && (tmpworld instanceof JiniasWorld))
			{
				NpcStringId npcString = null;
				switch (tmpworld.templateId)
				{
                                                case 141:
                                                        npcString = NpcStringId.THERES_NOTHING_YOU_CANT_SAY_I_CANT_LISTEN_TO_YOU_ANYMORE;
                                                        break;
                                                case 145:
                                                        npcString = NpcStringId.YOU_ADVANCED_BRAVELY_BUT_GOT_SUCH_A_TINY_RESULT_HOHOHO;
                                                        break;
				}

				if (npcString != null)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), npcString));
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new JiniasHideout(-1, qn, "instances");
	}
}