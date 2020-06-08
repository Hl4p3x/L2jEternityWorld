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
package l2e.scripts.ai.npc.group_template;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Create by LordWinter 13.12.2012 Based on L2J Eternity-World
 */
public class SelMahumTrainingGrounds extends L2AttackableAIScript
{
	private static final TIntObjectHashMap<Camp> camps = new TIntObjectHashMap<>();
	private static final TObjectIntHashMap<L2Npc> campIdByNpc = new TObjectIntHashMap<>();
	
	private static final int[] SELMAHUM_RECRUIT =
	{
		22780,
		22782,
		22784
	};
	
	private static final int[] SELMAHUM_SOLDIER =
	{
		22781,
		22783,
		22785
	};
	
	private static final int[] SELMAHUM_DRILL_SERGEANT =
	{
		22775,
		22777,
		22778
	};
	
	private static final int SELMAHUM_TRAINING_OFFICER = 22776;
	
	private static final int[] CHIEF_SOCIAL_ACTIONS =
	{
		1,
		4,
		5,
		7
	};
	
	private static final int[] SOLDIER_SOCIAL_ACTIONS =
	{
		1,
		5,
		6,
		7
	};
	
	private class Camp
	{
		public final int id;
		public final List<SpawnData> spawns;
		public L2Npc officer;
		public ArrayList<L2Npc> recruits;
		
		public Camp(final int id, final List<SpawnData> spawns)
		{
			this.id = id;
			this.spawns = spawns;
			recruits = new ArrayList<>();
		}
	}
	
	private class SpawnData
	{
		public final int npcId;
		public final Location spawnLoc;
		
		public SpawnData(final int npcId, final Location spawnLoc)
		{
			this.npcId = npcId;
			this.spawnLoc = spawnLoc;
		}
	}
	
	public SelMahumTrainingGrounds(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int i : SELMAHUM_DRILL_SERGEANT)
		{
			addAttackId(i);
			addKillId(i);
			addSpawnId(i);
		}
		
		for (int i : SELMAHUM_RECRUIT)
		{
			addAttackId(i);
			addKillId(i);
			addSpawnId(i);
		}
		
		for (int i : SELMAHUM_SOLDIER)
		{
			addAttackId(i);
			addKillId(i);
			addSpawnId(i);
		}
		
		addAttackId(SELMAHUM_TRAINING_OFFICER);
		addKillId(SELMAHUM_TRAINING_OFFICER);
		addSpawnId(SELMAHUM_TRAINING_OFFICER);
		
		init();
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("LineUpRank"))
		{
			if (!npc.isDead())
			{
				npc.setHeading(npc.getSpawn().getHeading());
				npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ());
				npc.getAttackByList().clear();
				npc.setWalking();
			}
		}
		else if (event.equalsIgnoreCase("Animate"))
		{
			if ((npc == null) || npc.isDead() || npc.isInCombat())
			{
				return null;
			}
			
			int campId = getCampId(npc);
			Camp camp = camps.get(campId);
			
			int idx = getRandom(6);
			if (idx <= (CHIEF_SOCIAL_ACTIONS.length - 1))
			{
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), CHIEF_SOCIAL_ACTIONS[idx]));
				for (L2Npc recruit : camp.recruits)
				{
					if ((recruit == null) || recruit.isDead())
					{
						continue;
					}
					recruit.broadcastPacket(new SocialAction(recruit.getObjectId(), SOLDIER_SOCIAL_ACTIONS[idx]));
				}
			}
			startQuestTimer("Animate", 15000, npc, null);
		}
		return null;
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (isOfficer(npc.getId()))
		{
			startQuestTimer("Animate", 15000, npc, null, true);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		Camp camp = null;
		if (isRecruit(npc.getId()) || isOfficer(npc.getId()))
		{
			camp = camps.get(getCampId(npc));
			if ((camp.officer != null) && !camp.officer.isDead())
			{
				int chance = getRandom(100);
				if (chance < 10)
				{
					camp.officer.broadcastPacket(new CreatureSay(camp.officer.getObjectId(), Say2.SHOUT, camp.officer.getName(), NpcStringId.HOW_DARE_YOU_ATTACK_MY_RECRUITS));
				}
				else if (chance < 20)
				{
					camp.officer.broadcastPacket(new CreatureSay(camp.officer.getObjectId(), Say2.SHOUT, camp.officer.getName(), NpcStringId.WHO_IS_DISRUPTING_THE_ORDER));
				}
				camp.officer.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 1);
			}
			for (L2Npc recruit : camp.recruits)
			{
				if ((recruit == null) || recruit.isDead() || recruit.equals(npc))
				{
					continue;
				}
				recruit.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 1);
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final int npcId = npc.getId();
		
		if (isOfficer(npcId))
		{
			Camp camp = camps.get(getCampId(npc));
			
			L2Npc mob = camp.recruits.get(getRandom(camp.recruits.size()));
			if (!mob.isDead())
			{
				mob.broadcastPacket(new CreatureSay(mob.getObjectId(), Say2.SHOUT, mob.getName(), NpcStringId.THE_DRILLMASTER_IS_DEAD));
			}
			
			mob = camp.recruits.get(getRandom(camp.recruits.size()));
			if (!mob.isDead())
			{
				mob.broadcastPacket(new CreatureSay(mob.getObjectId(), Say2.SHOUT, mob.getName(), NpcStringId.LINE_UP_THE_RANKS));
			}
			
			for (Iterator<L2Npc> i = camp.recruits.iterator(); i.hasNext();)
			{
				mob = i.next();
				if (mob.getId() == npcId)
				{
					continue;
				}
				final int fearLocX = mob.getX() + (getRandom(800, 1200) - getRandom(1200));
				final int fearLocY = mob.getY() + (getRandom(800, 1200) - getRandom(1200));
				final int fearHeading = Util.calculateHeadingFrom(mob.getX(), mob.getY(), fearLocX, fearLocY);
				mob.getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(fearLocX, fearLocY, mob.getZ(), fearHeading));
				startQuestTimer("LineUpRank", 30000, mob, null);
			}
			cancelQuestTimer("Animate", npc, null);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private int getCampId(L2Npc npc)
	{
		if (!campIdByNpc.containsKey(npc))
		{
			return -1;
		}
		return campIdByNpc.get(npc);
	}
	
	private boolean isOfficer(int npcId)
	{
		return Util.contains(SELMAHUM_DRILL_SERGEANT, npcId) || (SELMAHUM_TRAINING_OFFICER == npcId);
	}
	
	private boolean isRecruit(int npcId)
	{
		return Util.contains(SELMAHUM_RECRUIT, npcId) || Util.contains(SELMAHUM_SOLDIER, npcId);
	}
	
	private void initSpawns()
	{
		for (int id : camps.keys())
		{
			Camp camp = camps.get(id);
			
			for (SpawnData spawnData : camp.spawns)
			{
				Location loc = spawnData.spawnLoc;
				L2Npc npc = addSpawn(spawnData.npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 0, false);
				npc.getSpawn().setAmount(1);
				npc.getSpawn().setRespawnDelay(60);
				npc.getSpawn().startRespawn();
				npc.setIsNoRndWalk(true);
				npc.setIsNoAnimation(true);
				if (isOfficer(spawnData.npcId))
				{
					camp.officer = npc;
				}
				else
				{
					camp.recruits.add(npc);
				}
				campIdByNpc.put(npc, camp.id);
			}
			startQuestTimer("Animate", 15000, camp.officer, null, true);
		}
	}
	
	private void init()
	{
		File f = new File(Config.DATAPACK_ROOT, "data/spawnZones/training_grounds.xml");
		if (!f.exists())
		{
			_log.severe("[Sel Mahum Training Grounds] Missing training_grounds.xml!");
			return;
		}
		
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setValidating(true);
			Document doc = factory.newDocumentBuilder().parse(f);
			
			for (Node n = doc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equals("camp"))
				{
					NamedNodeMap attrs = n.getAttributes();
					final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					
					ArrayList<SpawnData> spawns = new ArrayList<>();
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equals("spawn"))
						{
							attrs = d.getAttributes();
							int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());
							int xPos = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
							int yPos = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
							int zPos = Integer.parseInt(attrs.getNamedItem("z").getNodeValue());
							int heading = d.getAttributes().getNamedItem("heading").getNodeValue() != null ? Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue()) : 0;
							
							SpawnData spawnData = new SpawnData(npcId, new Location(xPos, yPos, zPos, heading));
							spawns.add(spawnData);
						}
					}
					Camp camp = new Camp(id, spawns);
					camps.put(id, camp);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "[Sel Mahum Training Grounds] Could not parse training_grounds.xml file: " + e.getMessage(), e);
		}
		initSpawns();
	}
	
	public static void main(String[] args)
	{
		new SelMahumTrainingGrounds(-1, SelMahumTrainingGrounds.class.getSimpleName(), "ai");
	}
}