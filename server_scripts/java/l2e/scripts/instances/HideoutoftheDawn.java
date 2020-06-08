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
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Created by LordWinter 28.05.2012 Based on L2J Eternity-World
 */
public class HideoutoftheDawn extends Quest
{
	private static final String qn = "HideoutoftheDawn";

	private class HoDWorld extends InstanceWorld
	{
		public long[] storeTime =
		{
		                0,
		                0
		};

		public HoDWorld()
		{
		}
	}

	private static final int INSTANCEID = 113;

	private static final int WOOD = 32593;
	private static final int JAINA = 32617;

	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}

	public HideoutoftheDawn(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(WOOD);
		addTalkId(WOOD);
		addTalkId(JAINA);
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

		if (npc.getId() == WOOD)
		{
			teleCoord tele = new teleCoord();
			tele.x = -23769;
			tele.y = -8961;
			tele.z = -5392;
			enterInstance(player, "HideoutoftheDawn.xml", tele);
			return null;
		}
		else if (npc.getId() == JAINA)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
			Location tele = inst.getSpawnLoc();
			exitInstance(player, tele);
			return null;
		}
		return htmltext;
	}

	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		removeBuffs(player);
		if (player.hasSummon())
		{
			removeBuffs(player.getSummon());
		}
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}

	protected int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof HoDWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}

		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new HoDWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		((HoDWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		teleto.instanceId = instanceId;
		teleportplayer(player, teleto);
		world.allowed.add(player.getObjectId());

		return instanceId;
	}

	protected void exitInstance(L2PcInstance player, Location tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele, false);
	}

	private static final void removeBuffs(L2Character ch)
	{
		for (L2Effect e : ch.getAllEffects())
		{
			if (e == null)
			{
				continue;
			}
			L2Skill skill = e.getSkill();
			if (skill.isDebuff() || skill.isStayAfterDeath())
			{
				continue;
			}
			e.exit();
		}
	}

	public static void main(String[] args)
	{
		new HideoutoftheDawn(-1, qn, "instances");
	}
}
