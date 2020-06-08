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
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ElcadiaTent extends Quest
{
	private static final String qn = "ElcadiaTent";
	
	// Values
	private static final int INSTANCE_ID = 158;
	
	// NPC's
	private static final int Gruff_looking_Man = 32862;
	private static final int Elcadia = 32784;
	
	// Teleports
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	
	private static final int[][] TELEPORTS =
	{
		{
			89706,
			-238074,
			-9632
		},
		{
			43316,
			-87986,
			-2832
		}
	};
	
	private class ElcadiaTentWorld extends InstanceWorld
	{
		public ElcadiaTentWorld()
		{
		}
	}
	
	public ElcadiaTent(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Gruff_looking_Man);
		addTalkId(Gruff_looking_Man);
		addTalkId(Elcadia);
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
	}

	protected void exitInstance(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(0);
		player.teleToLocation(coords[0], coords[1], coords[2], false);

		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
		inst.setDuration(5 * 60000);
		inst.setEmptyDestroyTime(0);
	}
	
	protected void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof ElcadiaTentWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
			{
				teleportPlayer(player, TELEPORTS[ENTER], world.instanceId);
			}
			return;
		}
		
		final int instanceId = InstanceManager.getInstance().createDynamicInstance("ElcadiaTent.xml");
		
		world = new ElcadiaTentWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCE_ID;
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);
		
		world.allowed.add(player.getObjectId());
		teleportPlayer(player, TELEPORTS[ENTER], instanceId);
		
		return;
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
		
		if (npc.getId() == Gruff_looking_Man)
		{
			if ((player.getQuestState("_10292_SevenSignsGirlofDoubt") != null) && (player.getQuestState("_10292_SevenSignsGirlofDoubt").getState() == State.STARTED))
			{
				enterInstance(player);
				return null;
			}
			else if ((player.getQuestState("_10292_SevenSignsGirlofDoubt") != null) && (player.getQuestState("_10292_SevenSignsGirlofDoubt").getState() == State.COMPLETED) && (player.getQuestState("_10293_SevenSignsForbiddenBook") == null))
			{
				enterInstance(player);
				return null;
			}
			else if ((player.getQuestState("_10293_SevenSignsForbiddenBook") != null) && (player.getQuestState("_10293_SevenSignsForbiddenBook").getState() != State.COMPLETED))
			{
				enterInstance(player);
				return null;
			}
			else if ((player.getQuestState("_10293_SevenSignsForbiddenBook") != null) && (player.getQuestState("_10293_SevenSignsForbiddenBook").getState() == State.COMPLETED) && (player.getQuestState("_10294_SevenSignsToTheMonasteryOfSilence") == null))
			{
				enterInstance(player);
				return null;
			}
			else if ((player.getQuestState("_10296_SevenSignsOneWhoSeeksThePowerOfTheSeal") != null) && (player.getQuestState("_10296_SevenSignsOneWhoSeeksThePowerOfTheSeal").getInt("cond") == 3))
			{
				enterInstance(player);
				return null;
			}
			else
			{
				htmltext = "32862.htm";
			}
		}
		if (npc.getId() == Elcadia)
		{
			exitInstance(player, TELEPORTS[EXIT], 0);
			return null;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new ElcadiaTent(-1, qn, "instances");
	}
}