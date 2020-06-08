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
package l2e.gameserver.model.entity.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.Earthquake;
import l2e.gameserver.network.serverpackets.ExRedSky;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.util.Rnd;

/**
 * Created by LordWinter 18.01.2013 Based on L2J Eternity-World
 */
public class Leprechaun
{
	protected static final Logger _log = Logger.getLogger(Leprechaun.class.getName());
	
	private static Leprechaun _instance;
	
	protected final Object _lock = new Object();
	protected int x, y, z, mobId, timer = -1, timerAnnounce = -1;
	protected L2Npc lep;
	protected boolean isSpawn = false;
	protected String nearestTown = "", mobName = "";
	
	public static Leprechaun getInstance()
	{
		if (_instance == null)
		{
			_instance = new Leprechaun();
		}
		return _instance;
	}
	
	public Leprechaun()
	{
		new LeprechaunQuest(-1, "Leprechaun", "events");
		ThreadPoolManager.getInstance().scheduleGeneral(new LeprechaunMain(), Config.LEPRECHAUN_FIRST_SPAWN_DELAY * 60000);
		_log.info("Leprechaun: Event Leprechaun is Starting!");
	}
	
	public class LeprechaunQuest extends Quest
	{
		public LeprechaunQuest(int id, String name, String descr)
		{
			super(id, name, descr);
			
			addStartNpc(Config.LEPRECHAUN_ID);
			addFirstTalkId(Config.LEPRECHAUN_ID);
		}
		
		@Override
		public String onFirstTalk(L2Npc npc, L2PcInstance player)
		{
			QuestState qst = player.getQuestState(getName());
			if (qst == null)
			{
				qst = newQuestState(player);
			}
			if (isSpawn && (npc == lep))
			{
				isSpawn = false;
				int chance, min, max;
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement st = con.prepareStatement("SELECT itemId,min,max,chance FROM droplist WHERE mobId=?"))
				{
					st.setInt(1, Config.LEPRECHAUN_ID);
					ResultSet rset = st.executeQuery();
					while (rset.next())
					{
						chance = rset.getInt("chance");
						if (Rnd.get(1000000) < chance)
						{
							min = rset.getInt("min");
							max = rset.getInt("max");
							qst.giveItems(rset.getInt("itemId"), max > min ? min + Rnd.get(max - min) : min);
						}
					}
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
				
				if (Config.SHOW_NICK)
				{
					CustomMessage msg = new CustomMessage("Leprechaun.WAS_FOUND", true);
					msg.add(player.getName());
					Announcements.getInstance().announceToAll(msg);
				}
				else
				{
					CustomMessage msg = new CustomMessage("Leprechaun.NOT_FOUND", true);
					Announcements.getInstance().announceToAll(msg);
				}
				this.startQuestTimer("effect", 100, npc, player);
				this.startQuestTimer("delete", 1000, npc, player);
			}
			return null;
		}
		
		@Override
		public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
		{
			if (event.equals("effect"))
			{
				player.broadcastPacket(new Earthquake(player.getX(), player.getY(), player.getZ(), 30, 3));
				player.broadcastPacket(new ExRedSky(5));
				player.broadcastPacket(new MagicSkillUse(lep, lep, 1469, 1, 0x300, 0));
			}
			else if (event.equals("delete"))
			{
				lep.deleteMe();
			}
			return null;
		}
	}
	
	public class LeprechaunMain implements Runnable
	{
		@Override
		public void run()
		{
			timer++;
			timerAnnounce++;
			
			if (isSpawn && (timer == Config.LEPRECHAUN_SPAWN_TIME))
			{
				lep.deleteMe();
				isSpawn = false;
				CustomMessage msg = new CustomMessage("Leprechaun.DISAPPEARED", true);
				Announcements.getInstance().announceToAll(msg);
			}
			
			if (timerAnnounce == Config.LEPRECHAUN_ANNOUNCE_INTERVAL)
			{
				if (isSpawn)
				{
					CustomMessage msg = new CustomMessage("Leprechaun.NEAR", true);
					msg.add(mobName);
					msg.add(nearestTown);
					msg.add((Config.LEPRECHAUN_SPAWN_TIME - timer));
					Announcements.getInstance().announceToAll(msg);
				}
				timerAnnounce = 0;
			}
			
			if ((timer == 0) || (timer == Config.LEPRECHAUN_RESPAWN_INTERVAL))
			{
				boolean repeat = true;
				while (repeat)
				{
					selectRandomNpc();
					if ((mobName != "") && !mobName.equals("Treasure Chest"))
					{
						repeat = false;
					}
				}
				spawnLep();
				timer = 0;
			}
			ThreadPoolManager.getInstance().scheduleGeneral(this, 60000);
		}
		
		private void selectRandomNpc()
		{
			PreparedStatement st = null;
			ResultSet rset = null;
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				synchronized (_lock)
				{
					st = con.prepareStatement("SELECT npc_templateid,locx,locy,locz FROM spawnlist ORDER BY RAND()");
					rset = st.executeQuery();
					if (rset.next())
					{
						mobId = rset.getInt("npc_templateid");
						x = rset.getInt("locx");
						y = rset.getInt("locy");
						z = rset.getInt("locz");
					}
					st = con.prepareStatement("SELECT name FROM npc WHERE id=?");
					st.setInt(1, mobId);
					rset = st.executeQuery();
					if (rset.next())
					{
						mobName = rset.getString("name");
					}
				}
			}
			catch (Exception e)
			{
				System.out.println(e);
			}
		}
		
		private void spawnLep()
		{
			if (isSpawn)
			{
				lep.deleteMe();
				lep = null;
				isSpawn = false;
			}
			
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(Config.LEPRECHAUN_ID);
			if (template != null)
			{
				L2Spawn spawnDat;
				try
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setX(x);
					spawnDat.setY(y);
					spawnDat.setZ(z);
					spawnDat.setHeading(0);
					spawnDat.stopRespawn();
					lep = spawnDat.spawnOne(false);
					if (Config.SHOW_REGION)
					{
						nearestTown = " (" + MapRegionManager.getInstance().getClosestTownName(lep) + ")";
					}
					CustomMessage msg = new CustomMessage("Leprechaun.NEAR", true);
					msg.add(mobName);
					msg.add(nearestTown);
					Announcements.getInstance().announceToAll(msg);
					System.out.println("Leprechaun spawned in " + mobName + ": " + x + "," + y + "," + z);
					isSpawn = true;
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Leprechaun: " + e.getMessage(), e);
					lep = null;
				}
			}
			else
			{
				_log.warning("Leprechaun: Data missing in NPC table for ID: " + Config.LEPRECHAUN_ID + ".");
				lep = null;
			}
		}
	}
}