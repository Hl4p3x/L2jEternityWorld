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
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class SecretAreaKeucereus extends Quest
{
	private static final String qn = "SecretAreaKeucereus";
	
	private class KSAWorld extends InstanceWorld
	{
		public KSAWorld()
		{
		}
	}
	
	public SecretAreaKeucereus(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addTalkId(32566);
		addTalkId(32567);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, "SecretAreaKeucereus.xml");
		}
		else if (event.equalsIgnoreCase("enter_118"))
		{
			enterInstance118(player, "SecretAreaKeucereus.xml");
		}
		else if (event.equalsIgnoreCase("exit"))
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			inst.setDuration(5 * 60000);
			inst.setEmptyDestroyTime(0);

			player.teleToLocation(-184997, 242818, 1578);
			player.setInstanceId(0);
		}
		return "";
	}
	
	private void enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (!(world instanceof KSAWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player, (KSAWorld) world);
			return;
		}
		
		if (!checkCond(player))
		{
			return;
		}
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new KSAWorld();
		
		world.instanceId = instanceId;
		world.templateId = 117;
		world.status = 0;
		
		InstanceManager.getInstance().addWorld(world);
		teleportPlayer(player, (KSAWorld) world);
	}
	
	private void enterInstance118(L2PcInstance player, String template)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (!(world instanceof KSAWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player, (KSAWorld) world);
			return;
		}
		
		if (!checkCond118(player))
		{
			return;
		}
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new KSAWorld();
		
		world.instanceId = instanceId;
		world.templateId = 118;
		world.status = 0;
		
		InstanceManager.getInstance().addWorld(world);
		_log.info("SecretAreaKeucereus started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleportPlayer(player, (KSAWorld) world);
	}
	
	private void teleportPlayer(L2PcInstance player, KSAWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.teleToLocation(-23530, -8963, -5413);
		player.setInstanceId(world.instanceId);
		if (player.hasSummon())
		{
			player.getSummon().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getSummon().setInstanceId(world.instanceId);
			player.getSummon().teleToLocation(-23530, -8963, -5413);
		}
	}
	
	private boolean checkCond(L2PcInstance player)
	{
		if (QuestManager.getInstance().getQuest(10270) != null)
		{
			if ((player.getQuestState(QuestManager.getInstance().getQuest(10270).getName()).getState() == State.STARTED) && (player.getQuestState(QuestManager.getInstance().getQuest(10270).getName()).getInt("cond") == 4))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean checkCond118(L2PcInstance player)
	{
		if (QuestManager.getInstance().getQuest(10272) != null)
		{
			if ((player.getQuestState(QuestManager.getInstance().getQuest(10272).getName()).getState() == State.STARTED) && (player.getQuestState(QuestManager.getInstance().getQuest(10272).getName()).getInt("cond") == 3))
			{
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		new SecretAreaKeucereus(-1, qn, "instances");
	}
}