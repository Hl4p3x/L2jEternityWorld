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
package l2e.scripts.instances.KamalokaSolo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;

import javolution.util.FastMap;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class KamalokaSolo extends Quest
{
	protected static int ENTRANCE = 32484;
	protected static int REWARDER = 32485;
	
	private static int[] HERBS =
	{
		8600,
		8601,
		8602,
		8603,
		8604,
		8605
	};
	private static int[] BATTLEHERBS =
	{
		8606,
		8607,
		8608,
		8609,
		8610
	};
	
	private static int INSTANCE_TIME = 1800000;
	private static int COMBAT_TIME = 1200000;
	private static int REWARD_TIME = 600000;
	
	private static int ClientId = 46;
	
	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
	
	public teleCoord newCoord(int x, int y, int z)
	{
		teleCoord tele = new teleCoord();
		tele.x = x;
		tele.y = y;
		tele.z = z;
		return tele;
	}
	
	protected class KamaPlayer
	{
		public int instance = 0;
		public long timeStamp = 0;
		public int points = 0;
		public int count = 0;
		public int reward = 0;
		public boolean rewarded = false;
	}
	
	public class KamaParam
	{
		public String qn = "KamalokaSolo";
		public teleCoord enterCoord = null;
		public teleCoord rewPosition = null;
		public int minLev = 0;
		public int maxLev = 0;
	}
	
	protected class KamaWorld extends InstanceWorld
	{
		public FastMap<String, KamaPlayer> KamalokaPlayers = new FastMap<>();
		public KamaParam param = new KamaParam();
		
		public KamaWorld()
		{
		}
	}
	
	public KamalokaSolo(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
	
	protected boolean checkConditions(L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
			return false;
		}
		
		if (System.currentTimeMillis() < InstanceManager.getInstance().getInstanceTime(player.getObjectId(), ClientId))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		return true;
	}
	
	private boolean isWithinLevel(L2PcInstance player, int minLev, int maxLev)
	{
		if (player.getLevel() > maxLev)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		if (player.getLevel() < minLev)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		return true;
	}
	
	private void teleportplayer(L2PcInstance player, L2Npc entryNpc, teleCoord teleto)
	{
		final int instanceId = teleto.instanceId;
		final Instance instanceObj = InstanceManager.getInstance().getInstance(instanceId);
		
		instanceObj.setReturnTeleport(player.getX(), player.getY(), player.getZ());
		player.setInstanceId(instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		
		final L2Summon pet = player.getSummon();
		if (pet != null)
		{
			pet.setInstanceId(instanceId);
			pet.teleToLocation(teleto.x, teleto.y, teleto.z);
		}
		return;
	}
	
	protected int enterInstance(L2PcInstance player, L2Npc npc, String template, KamaParam param)
	{
		if (!checkConditions(player))
		{
			return 0;
		}
		
		if (!isWithinLevel(player, param.minLev, param.maxLev))
		{
			return 0;
		}
		
		final int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		Instance instanceObj = InstanceManager.getInstance().getInstance(instanceId);
		
		KamaWorld world = new KamaWorld();
		world.instanceId = instanceId;
		world.templateId = ClientId;
		
		instanceObj.setDuration(INSTANCE_TIME);
		final long instanceOver = INSTANCE_TIME + System.currentTimeMillis();
		KamaPlayer kp = new KamaPlayer();
		kp.instance = instanceId;
		kp.timeStamp = instanceOver;
		world.param = param;
		world.KamalokaPlayers.put(player.getName(), kp);
		InstanceManager.getInstance().addWorld(world);
		
		teleCoord teleto = param.enterCoord;
		teleto.instanceId = instanceId;
		teleportplayer(player, npc, teleto);
		
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.HOUR_OF_DAY, 6);
		reenter.set(Calendar.MINUTE, 30);
		
		long reenterDelay = reenter.getTimeInMillis();
		if (reenterDelay < System.currentTimeMillis())
		{
			reenterDelay += 86400000;
		}
		
		InstanceManager.getInstance().setInstanceTime(player.getObjectId(), ClientId, reenterDelay);
		player.setKamalokaId(instanceId);
		startQuestTimer("time", COMBAT_TIME, null, player);
		
		return instanceId;
	}
	
	protected void exitInstance(L2PcInstance player, teleCoord tele)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
		inst.setDuration(5 * 60000);
		inst.setEmptyDestroyTime(0);

		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
		player.setKamalokaId(0);
		final L2Summon pet = player.getSummon();
		if (pet != null)
		{
			pet.setInstanceId(0);
			pet.teleToLocation(tele.x, tele.y, tele.z);
		}
	}
	
	public String onAdvEventTo(String event, L2Npc npc, L2PcInstance player, String qn, int[] REW1, int[] REW2)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int instanceId = player.getInstanceId();
		final String playerName = player.getName();
		
		if (instanceId == 0)
		{
			instanceId = player.getKamalokaId();
		}
		
		Instance instanceObj;
		KamaWorld world;
		teleCoord rewPosition = null;
		if (InstanceManager.getInstance().instanceExist(instanceId))
		{
			instanceObj = InstanceManager.getInstance().getInstance(instanceId);
			world = (KamaWorld) InstanceManager.getInstance().getWorld(instanceId);
			if (world == null)
			{
				player.setKamalokaId(0);
				return "";
			}
			rewPosition = world.param.rewPosition;
		}
		else
		{
			player.setKamalokaId(0);
			return "";
		}
		
		if (event.equalsIgnoreCase("time"))
		{
			if (!player.isOnline())
			{
				return null;
			}
			
			instanceObj.setDuration(REWARD_TIME);
			instanceObj.removeNpcs();
			addSpawn(REWARDER, rewPosition.x, rewPosition.y, rewPosition.z, 0, false, 0, false, instanceId);
			if (!world.KamalokaPlayers.containsKey(playerName))
			{
				return "";
			}
			
			KamaPlayer kp = world.KamalokaPlayers.get(playerName);
			if (kp == null)
			{
				return null;
			}
			
			if (kp.count < 10)
			{
				kp.reward = 1;
			}
			else
			{
				kp.reward = (kp.points / kp.count) + 1;
				final int reward = kp.reward;
				final int count = kp.count;
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					int KamaCode = (world.param.minLev * 100) + world.param.maxLev;
					PreparedStatement statement = con.prepareStatement("INSERT INTO kamaloka_results (char_name,Level,Grade,Count) VALUES (?,?,?,?)");
					statement.setString(1, playerName);
					statement.setInt(2, KamaCode);
					statement.setInt(3, reward);
					statement.setInt(4, count);
					statement.executeUpdate();
					statement.close();
				}
				catch (Exception e)
				{
					_log.warning("Error while inserting Kamaloka data: " + e);
				}
			}
		}
		else if (event.equalsIgnoreCase("Reward"))
		{
			KamaPlayer kp = world.KamalokaPlayers.get(playerName);
			if (kp != null)
			{
				if (!kp.rewarded)
				{
					kp.rewarded = true;
					final int r = kp.reward - 1;
					
					st.giveItems(REW1[r * 2], REW1[(r * 2) + 1]);
					st.giveItems(REW2[r * 2], REW2[(r * 2) + 1]);
					
					NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
					html.setFile("data/scripts/instances/KamalokaSolo/" + player.getLang() + "/" + "1.htm");
					html.replace("%kamaloka%", qn);
					player.sendPacket(html);
				}
			}
		}
		else if (event.equalsIgnoreCase("Exit"))
		{
			instanceObj.removePlayers();
			player.setKamalokaId(0);
			InstanceManager.getInstance().destroyInstance(instanceId);
		}
		
		return null;
	}
	
	public String onEnterTo(L2Npc npc, L2PcInstance player, KamaParam param)
	{
		final String playerName = player.getName();
		QuestState st = player.getQuestState(param.qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		int instanceId = player.getInstanceId();
		if (instanceId == 0)
		{
			instanceId = player.getKamalokaId();
		}
		
		final String template = param.qn + ".xml";
		if (instanceId == 0)
		{
			if (enterInstance(player, npc, template, param) == 0)
			{
			}
		}
		else
		{
			final KamaWorld world = (KamaWorld) InstanceManager.getInstance().getWorld(instanceId);
			
			if (!InstanceManager.getInstance().instanceExist(instanceId))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
				sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));
				return "";
			}
			
			if (!InstanceManager.getInstance().getInstance(instanceId).getName().equalsIgnoreCase(param.qn))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return "";
			}
			
			if (world == null)
			{
				throw new RuntimeException("Kamaloka world is null!!!");
			}
			else if (world.param == null)
			{
				throw new RuntimeException("Kamaloka world.param is null!!!");
			}
			else if (world.param.qn == null)
			{
				throw new RuntimeException("Kamaloka world.param.qn is null!!!");
			}
			
			if (!world.param.qn.isEmpty() && !world.param.qn.equalsIgnoreCase(param.qn))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return "";
			}
			
			KamaPlayer kp = world.KamalokaPlayers.get(playerName);
			if (kp != null)
			{
				long currentTime = System.currentTimeMillis();
				if (kp.timeStamp > currentTime)
				{
					final teleCoord tele = param.enterCoord;
					
					player.setInstanceId(instanceId);
					player.teleToLocation(tele.x, tele.y, tele.z);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
					sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));
				}
			}
		}
		return "";
	}
	
	public String onTalkTo(L2Npc npc, L2PcInstance player, String qn)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (!(tmpworld instanceof KamaWorld))
		{
			return "";
		}
		
		final String playerName = player.getName();
		final KamaWorld world = (KamaWorld) tmpworld;
		final KamaPlayer kp = world.KamalokaPlayers.get(playerName);
		if (kp == null)
		{
			return "";
		}
		
		if (npc.getId() == REWARDER)
		{
			if (!world.KamalokaPlayers.containsKey(playerName))
			{
				return "";
			}
			
			String msgReward = "0.htm";
			if (!kp.rewarded)
			{
				switch (kp.reward)
				{
					case 1:
						msgReward = "D.htm";
						break;
					case 2:
						msgReward = "C.htm";
						break;
					case 3:
						msgReward = "B.htm";
						break;
					case 4:
						msgReward = "A.htm";
						break;
					case 5:
						msgReward = "S.htm";
						break;
					default:
						msgReward = "1.htm";
						break;
				}
			}
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/scripts/instances/KamalokaSolo/" + player.getLang() + "/" + msgReward);
			html.replace("%kamaloka%", qn);
			player.sendPacket(html);
		}
		return null;
	}
	
	public String onKillTo(L2Npc npc, L2PcInstance player, boolean isPet, String qn, int KANABION, int[] APPEAR, int[] REW)
	{
		if (player == null)
		{
			return "";
		}
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (!(tmpworld instanceof KamaWorld))
		{
			return "";
		}
		
		final String playerName = player.getName();
		final KamaWorld world = (KamaWorld) tmpworld;
		
		if (!world.KamalokaPlayers.containsKey(playerName))
		{
			return "";
		}
		
		KamaPlayer kp = world.KamalokaPlayers.get(playerName);
		
		final int npcId = npc.getId();
		if (npcId == KANABION)
		{
			kp.count += 1;
			if (getRandom(100) <= REW[0])
			{
				final L2Attackable newMob = (L2Attackable) addSpawn(APPEAR[0], npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				if (newMob != null)
				{
					newMob.setRunning();
					newMob.addDamageHate(player, 0, 999);
					newMob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				}
			}
			
			if (getRandom(100) <= REW[1])
			{
				st.dropItem((L2MonsterInstance) npc, player, HERBS[getRandom(HERBS.length)], 1);
			}
			if (getRandom(10000) <= REW[2])
			{
				st.dropItem((L2MonsterInstance) npc, player, REW[3], 1);
			}
		}
		else if (npcId == APPEAR[0])
		{
			kp.points += 1;
			if (getRandom(100) <= REW[4])
			{
				final L2Attackable newMob = (L2Attackable) addSpawn(APPEAR[getRandom(APPEAR.length)], npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				if (newMob != null)
				{
					newMob.setRunning();
					newMob.addDamageHate(player, 0, 999);
					newMob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				}
			}
			
			if (getRandom(100) <= REW[5])
			{
				st.dropItem((L2MonsterInstance) npc, player, HERBS[getRandom(HERBS.length)], 1);
			}
			else if (getRandom(100) <= REW[6])
			{
				st.dropItem((L2MonsterInstance) npc, player, BATTLEHERBS[getRandom(BATTLEHERBS.length)], 1);
			}
			if (getRandom(10000) <= REW[7])
			{
				st.dropItem((L2MonsterInstance) npc, player, REW[8], 1);
			}
		}
		else if (npcId == APPEAR[1])
		{
			kp.points += 2;
			if (getRandom(100) <= REW[9])
			{
				st.dropItem((L2MonsterInstance) npc, player, HERBS[getRandom(HERBS.length)], 1);
			}
			else if (getRandom(100) <= REW[10])
			{
				st.dropItem((L2MonsterInstance) npc, player, BATTLEHERBS[getRandom(BATTLEHERBS.length)], 1);
			}
			
			if (getRandom(100) <= REW[11])
			{
				final L2Attackable newMob = (L2Attackable) addSpawn(APPEAR[1], npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				if (newMob != null)
				{
					newMob.setRunning();
					newMob.addDamageHate(player, 0, 999);
					newMob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				}
			}
			
			if (getRandom(10000) <= REW[12])
			{
				st.dropItem((L2MonsterInstance) npc, player, REW[13], 1);
			}
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new KamalokaSolo(-1, "qn", "instance");
	}
}