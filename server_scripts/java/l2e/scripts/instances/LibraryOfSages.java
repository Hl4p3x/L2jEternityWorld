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

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class LibraryOfSages extends Quest
{
	private static final String qn = "LibraryOfSages";
	
	// Values
	private static final int INSTANCE_ID = 156;
	
	// NPC's
	private static final int Sophia = 32596;
	private static final int Sophia2 = 32861;
	private static final int Sophia3 = 32863;
	private static final int Elcadia_Support = 32785;
	
	// Teleports
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	private static final int HidenRoom = 2;
	
	private static final int[][] TELEPORTS =
	{
		{
			37063,
			-49813,
			-1128
		},
		{
			37063,
			-49813,
			-1128
		},
		{
			37355,
			-50065,
			-1127
		}
	};
	
	private static final NpcStringId[] spam =
	{
		NpcStringId.I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK,
		NpcStringId.THIS_LIBRARY_ITS_HUGE_BUT_THERE_ARENT_MANY_USEFUL_BOOKS_RIGHT,
		NpcStringId.AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES,
		NpcStringId.THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH
	};
	
	private final FastMap<Integer, InstanceHolder> instanceWorlds = new FastMap<>();
	
	protected static class InstanceHolder
	{
		FastList<L2Npc> mobs = new FastList<>();
	}
	
	private class LibraryOfSagesWorld extends InstanceWorld
	{
		public LibraryOfSagesWorld()
		{
		}
	}
	
	public LibraryOfSages(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Sophia);
		addStartNpc(Sophia2);
		addTalkId(Sophia);
		addTalkId(Sophia2);
		addTalkId(Sophia3);
		addTalkId(Elcadia_Support);
	}
	
	private void teleportPlayer(L2Npc npc, L2PcInstance player, int[] coords, int instanceId)
	{
		InstanceHolder holder = instanceWorlds.get(instanceId);
		if (holder == null)
		{
			holder = new InstanceHolder();
			instanceWorlds.put(instanceId, holder);
		}
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
		cancelQuestTimer("check_follow", npc, player);
		
		for (L2Npc h : holder.mobs)
		{
			h.deleteMe();
		}
		holder.mobs.clear();
		
		if (instanceId > 0)
		{
			L2Npc support = addSpawn(Elcadia_Support, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, player.getInstanceId());
			holder.mobs.add(support);
			startQuestTimer("check_follow", 3000, support, player);
		}
	}
	
	protected void enterInstance(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof LibraryOfSagesWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], world.instanceId);
			}
		}
		else
		{
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("LibraryOfSages.xml");
			
			world = new LibraryOfSagesWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);
			
			world.allowed.add(player.getObjectId());
			
			teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);
		}
		return;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (event.equalsIgnoreCase("check_follow"))
		{
			cancelQuestTimer("check_follow", npc, player);
			npc.getAI().stopFollow();
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), spam[getRandom(0, spam.length - 1)]));
			startQuestTimer("check_follow", 20000, npc, player);
			return "";
		}
		else if (npc.getId() == Sophia)
		{
			if (event.equalsIgnoreCase("tele1"))
			{
				enterInstance(npc, player);
				return null;
			}
		}
		else if (npc.getId() == Sophia2)
		{
			if (event.equalsIgnoreCase("tele2"))
			{
				teleportPlayer(npc, player, TELEPORTS[HidenRoom], player.getInstanceId());
				return null;
			}
			else if (event.equalsIgnoreCase("tele3"))
			{
				InstanceHolder holder = instanceWorlds.get(player.getInstanceId());
				if (holder != null)
				{
					for (L2Npc h : holder.mobs)
					{
						h.deleteMe();
					}
					holder.mobs.clear();
				}

				InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				inst.setDuration(5 * 60000);
				inst.setEmptyDestroyTime(0);
				player.setInstanceId(0);

				teleportPlayer(npc, player, TELEPORTS[EXIT], 0);
				return null;
			}
		}
		else if (npc.getId() == Sophia3)
		{
			if (event.equalsIgnoreCase("tele4"))
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], player.getInstanceId());
				return null;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new LibraryOfSages(-1, qn, "instances");
	}
}