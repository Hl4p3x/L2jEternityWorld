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
package l2e.gameserver.model.actor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.CategoryParser;
import l2e.gameserver.data.xml.PhantomLocationParser;
import l2e.gameserver.data.xml.PhantomMessagesParser;
import l2e.gameserver.data.xml.PhantomSkillsParser;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2PhantomLoc;
import l2e.gameserver.model.L2PhantomPlayer;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.L2MerchantInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2TrainerInstance;
import l2e.gameserver.model.actor.instance.L2VillageMasterFighterInstance;
import l2e.gameserver.model.actor.instance.L2VillageMasterPriestInstance;
import l2e.gameserver.model.actor.instance.L2WarehouseInstance;
import l2e.gameserver.model.actor.templates.L2PcTemplate;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.PcItemTemplate;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.util.Rnd;

public class L2Phantom
{
	private static final Logger _log = Logger.getLogger(L2Phantom.class.getName());
	
	private static String _phantomAcc = Config.PHANTOM_PLAYERS_AKK;
	private volatile int _phantomsTotal = Config.MAX_PHANTOM_COUNT;
	
	private static FastList<Integer> _nameColors = new FastList<>();
	private static FastList<Integer> _titleColors = new FastList<>();
	private static FastList<Integer> _phantomsList = new FastList<>();
	private static FastMap<Integer, L2PhantomPlayer> _phantoms = new FastMap<>();
	
	private static Map<Integer, ConcurrentLinkedQueue<L2PcInstance>> _phantomPlayersList = new ConcurrentHashMap<>();
	private static Map<Integer, ConcurrentLinkedQueue<Integer>> _phantomsClanList = new ConcurrentHashMap<>();
	
	private static final int[] BASE_PROFF = Config.PHANTOM_BASE_PROFF_CLASSID;
	private static final int[] FIRST_PROFF = Config.PHANTOM_FIRST_PROFF_CLASSID;
	private static final int[] SECOND_PROFF = Config.PHANTOM_SECOND_PROFF_CLASSID;
	private static final int[] THIRD_PROFF = Config.PHANTOM_THIRD_PROFF_CLASSID;
	
	private static final SkillsHolder CUBIC = new SkillsHolder(4338, 1);
	
	private static final SkillsHolder[] FIGHTER_BUFFS =
	{
		new SkillsHolder(4322, 1),
		new SkillsHolder(4323, 1),
		new SkillsHolder(4324, 1),
		new SkillsHolder(4325, 1),
		new SkillsHolder(4326, 1),
		new SkillsHolder(5632, 1),
	};
	private static final SkillsHolder[] MAGE_BUFFS =
	{
		new SkillsHolder(4322, 1),
		new SkillsHolder(4323, 1),
		new SkillsHolder(4328, 1),
		new SkillsHolder(4329, 1),
		new SkillsHolder(4330, 1),
		new SkillsHolder(4331, 1),
	};
	
	private static final int[] NPC_BUFFERS =
	{
		30598,
		30599,
		30600,
		30601,
		30602,
		32135
	};
	
	public L2Phantom()
	{
		load();
	}
	
	private void load()
	{
		parceColors();
		parceClans();
		cacheFantoms();
		
		_phantomPlayersList.put((1), new ConcurrentLinkedQueue<L2PcInstance>());
		_phantomPlayersList.put((2), new ConcurrentLinkedQueue<L2PcInstance>());
		_phantomsList.clear();
	}
	
	private void cacheFantoms()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				String name = "";
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					con.setTransactionIsolation(1);
					PreparedStatement st = con.prepareStatement("SELECT charId,char_name,title,x,y,z,clanid FROM characters WHERE account_name = ?");
					st.setString(1, _phantomAcc);
					ResultSet rs = st.executeQuery();
					rs.setFetchSize(250);
					while (rs.next())
					{
						name = rs.getString("char_name");
						_phantoms.put(Integer.valueOf(rs.getInt("charId")), new L2PhantomPlayer(name, rs.getString("title"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("clanid")));
					}
					st.close();
					rs.close();
					con.close();
					_log.info("[PhantomListener]: Loaded " + _phantoms.size() + " phantom players...");
				}
				catch (Exception e)
				{
					_log.warning("[PhantomListener]: could not load chars from DB: " + e);
				}
				if (!_phantoms.isEmpty())
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new FantomTask(1), Config.PHANTOM_PLAYERS_DELAY_FIRST * 1000L);
				}
			}
		}).start();
	}
	
	private void parceClans()
	{
		LineNumberReader lnr = null;
		BufferedReader br = null;
		FileReader fr = null;
		try
		{
			File Data = new File("./config/mods/phantoms/town_clans.ini");
			if (!Data.exists())
			{
				return;
			}
			fr = new FileReader(Data);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);
			int clanId = 0;
			String line;
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() != 0) && (!line.startsWith("#")))
				{
					String[] items = line.split(":");
					clanId = Integer.parseInt(items[0]);
					String[] pls = items[1].split(",");
					ConcurrentLinkedQueue<Integer> players = new ConcurrentLinkedQueue<>();
					for (String plid : pls)
					{
						players.add(Integer.valueOf(plid));
					}
					_phantomsClanList.put(clanId, players);
				}
			}
			_log.info("[PhantomListener]: Loaded " + _phantomsClanList.size() + " phantom player clans...");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (fr != null)
				{
					fr.close();
				}
				if (br != null)
				{
					br.close();
				}
				if (lnr != null)
				{
					lnr.close();
				}
			}
			catch (Exception e1)
			{
			}
		}
	}
	
	protected void startWalk(L2PcInstance phantom)
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new PhantomTask(phantom), 4559, 1177);
	}
	
	private void parceColors()
	{
		_nameColors = Config.PHANTOM_PLAYERS_NAME_CLOLORS;
		_titleColors = Config.PHANTOM_PLAYERS_TITLE_CLOLORS;
	}
	
	private int getRandomPhantomNext()
	{
		int obj = Rnd.get(600000001, 600000011);
		if (!_phantomsList.contains(obj))
		{
			return obj;
		}
		return getRandomPhantomNext();
	}
	
	// private int getRandomClan()
	// {
	// return Rnd.get(600000000, 600006081);
	// }
	
	private int getRandomLvl(int grade)
	{
		int level = 1;
		switch (grade)
		{
			case 0:
				return level = Rnd.get(1, 9);
			case 1:
				return level = Rnd.get(10, 19);
			case 2:
				return level = Rnd.get(20, 39);
			case 3:
				return level = Rnd.get(40, 51);
			case 4:
				return level = Rnd.get(52, 60);
			case 5:
				return level = Rnd.get(61, 75);
			case 6:
				return level = Rnd.get(76, 80);
			case 7:
				return level = Rnd.get(80, 85);
		}
		return level;
	}
	
	private int getRandomLvlTown(String town)
	{
		int level = 1;
		switch (town)
		{
			case "Talking Island":
			case "Dark Elven Village":
			case "Elven Village":
			case "Orc Village":
			case "Kamael Village":
			case "Dwarven Village":
				return level = Rnd.get(6, 39);
			case "Gludin":
				return level = Rnd.get(15, 39);
			case "Gludio":
				return level = Rnd.get(18, 39);
			case "Dion":
				return level = Rnd.get(20, 48);
			case "Giran":
				return level = Rnd.get(39, 85);
			case "Oren":
				return level = Rnd.get(52, 85);
			case "Heine":
				return level = Rnd.get(40, 75);
			case "Hunters Village":
				return level = Rnd.get(45, 75);
			case "Aden":
				return level = Rnd.get(48, 85);
			case "Goddard":
				return level = Rnd.get(58, 85);
			case "Rune":
				return level = Rnd.get(62, 85);
			case "Schuttgart":
				return level = Rnd.get(51, 75);
		}
		return level;
	}
	
	private int getRandomClass(int grade)
	{
		int classId = 0;
		switch (grade)
		{
			case 0:
			case 1:
				return classId = BASE_PROFF[Rnd.get(BASE_PROFF.length)];
			case 2:
				return classId = FIRST_PROFF[Rnd.get(FIRST_PROFF.length)];
			case 3:
			case 4:
			case 5:
				return classId = SECOND_PROFF[Rnd.get(SECOND_PROFF.length)];
			case 6:
			case 7:
				return classId = THIRD_PROFF[Rnd.get(THIRD_PROFF.length)];
		}
		return classId;
	}
	
	private int getRandomClassTown(int level)
	{
		int classId = 0;
		if (level < 20)
		{
			return classId = BASE_PROFF[Rnd.get(BASE_PROFF.length)];
		}
		else if ((level > 19) && (level < 40))
		{
			return classId = FIRST_PROFF[Rnd.get(FIRST_PROFF.length)];
		}
		else if ((level > 39) && (level < 76))
		{
			return classId = SECOND_PROFF[Rnd.get(SECOND_PROFF.length)];
		}
		else if (level > 75)
		{
			return classId = THIRD_PROFF[Rnd.get(THIRD_PROFF.length)];
		}
		return classId;
	}
	
	protected L2PcInstance loadPhantom(int objId)
	{
		int nbPlayerIG = L2World.getInstance().getAllPlayers().size();
		if (nbPlayerIG < Config.MAXIMUM_ONLINE_USERS)
		{
			L2PhantomPlayer fantom = _phantoms.get(objId);
			if (fantom == null)
			{
				return null;
			}
			
			L2PhantomLoc loc = PhantomLocationParser.getInstance().getRandomLoc();
			int lvl;
			if (loc.getTown() != null)
			{
				lvl = getRandomLvlTown(loc.getTown());
			}
			else
			{
				lvl = getRandomLvl(loc.getGrade());
			}
			
			int classId;
			if (loc.getTown() != null)
			{
				classId = getRandomClassTown(lvl);
			}
			else
			{
				classId = getRandomClass(loc.getGrade());
			}
			
			L2PcInstance phantom = L2PcInstance.restorePhantoms(objId, lvl, classId);
			phantom.setOnlineStatus(true, false);
			phantom.setPhantomLoc(loc.getLocX(), loc.getLocY(), loc.getLocZ());
			phantom.setXYZ(loc.getLocX() + Rnd.get(60), loc.getLocY() + Rnd.get(60), loc.getLocZ());
			Location loc1 = new Location(loc.getLocX() + Rnd.get(350), loc.getLocY() + Rnd.get(350), loc.getLocZ());
			phantom.spawnMe(loc1.getX(), loc1.getY(), loc1.getZ());
			phantom.setOnlineStatus(true, true);
			phantom.restoreEffects();
			phantom.setCurrentHpMp(phantom.getMaxHp(), phantom.getMaxMp());
			phantom.setCurrentCp(phantom.getMaxCp());
			if (Config.ALLOW_PHANTOM_SETS)
			{
				if ((phantom.getLevel() >= 1) && (phantom.getLevel() < 10))
				{
					L2PcTemplate template = phantom.getTemplate();
					for (PcItemTemplate ie : template.getInitialEquipment())
					{
						L2ItemInstance item = phantom.getInventory().addItem("FtNewbieArmor", ie.getId(), ie.getCount(), phantom, null);
						phantom.getInventory().addItem(item);
						
						if (item.isEquipable() && ie.isEquipped())
						{
							phantom.getInventory().equipItem(item);
							phantom.addAutoSoulShot(3947);
						}
					}
				}
				else if ((phantom.getLevel() >= 10) && (phantom.getLevel() < 20))
				{
					L2PcTemplate template = phantom.getTemplate();
					for (PcItemTemplate ie : template.getPhantomNgradeItems())
					{
						L2ItemInstance item = phantom.getInventory().addItem("FtNgArmor", ie.getId(), ie.getCount(), phantom, null);
						phantom.getInventory().addItem(item);
						
						if (item.isEquipable() && ie.isEquipped())
						{
							phantom.getInventory().equipItem(item);
							phantom.addAutoSoulShot(3947);
						}
					}
				}
				else if ((phantom.getLevel() >= 20) && (phantom.getLevel() < 40))
				{
					L2PcTemplate template = phantom.getTemplate();
					for (PcItemTemplate ie : template.getPhantomDgradeItems())
					{
						L2ItemInstance item = phantom.getInventory().addItem("FtDArmor", ie.getId(), ie.getCount(), phantom, null);
						phantom.getInventory().addItem(item);
						
						if (item.isEquipable() && ie.isEquipped())
						{
							phantom.getInventory().equipItem(item);
							phantom.addAutoSoulShot(3948);
						}
					}
				}
				else if ((phantom.getLevel() >= 40) && (phantom.getLevel() < 52))
				{
					L2PcTemplate template = phantom.getTemplate();
					for (PcItemTemplate ie : template.getPhantomCgradeItems())
					{
						L2ItemInstance item = phantom.getInventory().addItem("FtCArmor", ie.getId(), ie.getCount(), phantom, null);
						phantom.getInventory().addItem(item);
						
						if (item.isEquipable() && ie.isEquipped())
						{
							phantom.getInventory().equipItem(item);
							phantom.addAutoSoulShot(3949);
						}
					}
				}
				else if ((phantom.getLevel() >= 52) && (phantom.getLevel() < 61))
				{
					L2PcTemplate template = phantom.getTemplate();
					for (PcItemTemplate ie : template.getPhantomBgradeItems())
					{
						L2ItemInstance item = phantom.getInventory().addItem("FtBArmor", ie.getId(), ie.getCount(), phantom, null);
						phantom.getInventory().addItem(item);
						
						if (item.isEquipable() && ie.isEquipped())
						{
							phantom.getInventory().equipItem(item);
							phantom.addAutoSoulShot(3950);
						}
					}
				}
				else if ((phantom.getLevel() >= 61) && (phantom.getLevel() < 76))
				{
					L2PcTemplate template = phantom.getTemplate();
					for (PcItemTemplate ie : template.getPhantomAgradeItems())
					{
						L2ItemInstance item = phantom.getInventory().addItem("FtAArmor", ie.getId(), ie.getCount(), phantom, null);
						phantom.getInventory().addItem(item);
						
						if (item.isEquipable() && ie.isEquipped())
						{
							phantom.getInventory().equipItem(item);
							phantom.addAutoSoulShot(3951);
						}
					}
				}
				else if ((phantom.getLevel() >= 76) && (phantom.getLevel() < 80))
				{
					L2PcTemplate template = phantom.getTemplate();
					for (PcItemTemplate ie : template.getPhantomSgradeItems())
					{
						L2ItemInstance item = phantom.getInventory().addItem("FtSArmor", ie.getId(), ie.getCount(), phantom, null);
						phantom.getInventory().addItem(item);
						
						if (item.isEquipable() && ie.isEquipped())
						{
							phantom.getInventory().equipItem(item);
							phantom.addAutoSoulShot(3952);
						}
					}
				}
				else if ((phantom.getLevel() >= 80) && (phantom.getLevel() < 84))
				{
					L2PcTemplate template = phantom.getTemplate();
					for (PcItemTemplate ie : template.getPhantomS80gradeItems())
					{
						L2ItemInstance item = phantom.getInventory().addItem("FtS80Armor", ie.getId(), ie.getCount(), phantom, null);
						phantom.getInventory().addItem(item);
						
						if (item.isEquipable() && ie.isEquipped())
						{
							phantom.getInventory().equipItem(item);
							phantom.addAutoSoulShot(3952);
						}
					}
				}
				else if (phantom.getLevel() >= 84)
				{
					L2PcTemplate template = phantom.getTemplate();
					for (PcItemTemplate ie : template.getPhantomS84gradeItems())
					{
						L2ItemInstance item = phantom.getInventory().addItem("FtS84Armor", ie.getId(), ie.getCount(), phantom, null);
						phantom.getInventory().addItem(item);
						
						if (item.isEquipable() && ie.isEquipped())
						{
							phantom.getInventory().equipItem(item);
							phantom.addAutoSoulShot(3952);
						}
					}
				}
			}
			if (!Config.ALLOW_PHANTOM_SETS)
			{
				L2PcTemplate template = phantom.getTemplate();
				for (PcItemTemplate ie : template.getInitialEquipment())
				{
					L2ItemInstance item = phantom.getInventory().addItem("FtNewbieArmor", ie.getId(), ie.getCount(), phantom, null);
					phantom.getInventory().addItem(item);
					
					if (item.isEquipable() && ie.isEquipped())
					{
						phantom.getInventory().equipItem(item);
					}
				}
			}
			phantom.broadcastUserInfo();
			phantom.rewardSkills();
			startWalk(phantom);
			return phantom;
		}
		return null;
	}
	
	private String getRandomShoutMessage()
	{
		return PhantomMessagesParser.getInstance().getRndShoutMessage();
	}
	
	protected int getNameColor()
	{
		return _nameColors.get(Rnd.get(_nameColors.size() - 1)).intValue();
	}
	
	protected int getTitleColor()
	{
		return _titleColors.get(Rnd.get(_titleColors.size() - 1)).intValue();
	}
	
	protected class Social implements Runnable
	{
		public Social()
		{
		}
		
		@Override
		public void run()
		{
			TextBuilder tb = new TextBuilder();
			for (Map.Entry<Integer, ConcurrentLinkedQueue<L2PcInstance>> entry : _phantomPlayersList.entrySet())
			{
				Integer wave = entry.getKey();
				ConcurrentLinkedQueue<L2PcInstance> players = entry.getValue();
				if ((wave != null) && (players != null) && (!players.isEmpty()))
				{
					int count = 0;
					for (L2PcInstance player : players)
					{
						if (player.isInsideZone(ZoneId.PEACE))
						{
							if (Rnd.get(100) < 65)
							{
								switch (Rnd.get(2))
								{
									case 0:
									case 1:
										L2ItemInstance wpn = player.getActiveWeaponInstance();
										if ((wpn != null) && (wpn.getItem().getCrystalType() != L2Item.CRYSTAL_NONE))
										{
											int enhchant = wpn.getEnchantLevel();
											int nextench = enhchant + 1;
											if ((Rnd.get(100) < 45) && (enhchant <= Config.PHANTOM_PLAYERS_ENCHANT_MAX))
											{
												wpn.setEnchantLevel(nextench);
											}
											else if (Rnd.get(100) < 70)
											{
												wpn.setEnchantLevel(3);
												if ((nextench > 13) && (Rnd.get(100) < 2))
												{
													tb.append("!");
													for (int i = Rnd.get(2, 13); i > 0; i--)
													{
														tb.append("!");
													}
													tb.clear();
												}
											}
											player.broadcastUserInfo();
										}
										break;
									case 2:
										if (Rnd.get(100) < 5)
										{
											player.moveToLocation(player.getX() + Rnd.get(30), player.getY() + Rnd.get(30), player.getZ(), 40);
											player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, null);
										}
										break;
								}
								try
								{
									Thread.sleep(Rnd.get(500, 1500));
								}
								catch (InterruptedException e)
								{
								}
								count++;
							}
						}
						if (count > 55)
						{
							break;
						}
					}
				}
			}
			tb.clear();
			tb = null;
			ThreadPoolManager.getInstance().scheduleGeneral(new Social(), 12000L);
		}
	}
	
	protected class CheckCount implements Runnable
	{
		public CheckCount()
		{
		}
		
		@Override
		public void run()
		{
			for (Map.Entry<Integer, ConcurrentLinkedQueue<L2PcInstance>> entry : _phantomPlayersList.entrySet())
			{
				Integer wave = entry.getKey();
				ConcurrentLinkedQueue<L2PcInstance> players = entry.getValue();
				if ((wave != null) && (players != null) && (!players.isEmpty()))
				{
					int limit = wave.intValue() == 1 ? Config.PHANTOM_PLAYERS_COUNT_FIRST : Config.PHANTOM_PLAYERS_COUNT_NEXT;
					int overflow = players.size() - limit;
					if (overflow >= 1)
					{
						for (L2PcInstance fantom : players)
						{
							fantom.setOnlineStatus(false, false);
							_phantomPlayersList.get(wave).remove(fantom);
							_phantomsList.remove(fantom.getObjectId());
							overflow--;
							if (overflow == 0)
							{
								break;
							}
						}
					}
				}
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new CheckCount(), 300000L);
		}
	}
	
	protected class FantomTaskDespawn implements Runnable
	{
		public int _task;
		
		public FantomTaskDespawn(int task)
		{
			_task = task;
		}
		
		@SuppressWarnings("unused")
		@Override
		public void run()
		{
			Location loc = null;
			L2PcInstance next = null;
			ConcurrentLinkedQueue<L2PcInstance> players = _phantomPlayersList.get(_task);
			for (L2PcInstance fantom : players)
			{
				if (fantom != null)
				{
					loc = fantom.getPhantomLoc();
					
					fantom.setOnlineStatus(false, false);
					_phantomPlayersList.get(_task).remove(fantom);
					_phantomsList.remove(fantom.getObjectId());
					try
					{
						Thread.sleep(_task == 1 ? Config.PHANTOM_PLAYERS_DELAY_DESPAWN_FIRST : Config.PHANTOM_PLAYERS_DELAY_DESPAWN_NEXT);
					}
					catch (InterruptedException e)
					{
					}
					if (_phantomsTotal <= (Config.PHANTOM_PLAYERS_COUNT_FIRST + Config.PHANTOM_PLAYERS_COUNT_NEXT))
					{
						int nextObjId = getRandomPhantomNext();
						if (!_phantomsList.contains(nextObjId))
						{
							next = loadPhantom(nextObjId);
							if (next != null)
							{
								_phantomPlayersList.get(_task).add(next);
								_phantomsList.add(next.getObjectId());
								if (Rnd.get(100) < 3)
								{
									next.sitDown();
								}
								try
								{
									Thread.sleep(100L);
								}
								catch (InterruptedException e)
								{
								}
							}
						}
					}
				}
			}
			loc = null;
			next = null;
			ThreadPoolManager.getInstance().scheduleGeneral(new FantomTaskDespawn(1), _task == 1 ? Config.PHANTOM_PLAYERS_DESPAWN_FIRST : Config.PHANTOM_PLAYERS_DESPAWN_NEXT);
		}
	}
	
	protected class FantomTask implements Runnable
	{
		public int _task;
		
		public FantomTask(int task)
		{
			_task = task;
		}
		
		@Override
		public void run()
		{
			int count = 0;
			int count2 = 0;
			int PhantomObjId = 0;
			int PhantomObjId2 = 0;
			switch (_task)
			{
				case 1:
					_log.info("[PhantomListener]: Started 1st stage spawn!");
					while (count < Config.PHANTOM_PLAYERS_COUNT_FIRST)
					{
						L2PcInstance fantom = null;
						PhantomObjId = getRandomPhantomNext();
						if (!_phantomsList.contains(PhantomObjId))
						{
							fantom = loadPhantom(PhantomObjId);
							if (fantom != null)
							{
								_phantomPlayersList.get(1).add(fantom);
								_phantomsList.add(fantom.getObjectId());
								if (Rnd.get(100) < 5)
								{
									fantom.sitDown();
								}
								if (Rnd.get(100) < 30)
								{
									fantom.startAbnormalEffect(AbnormalEffect.AVE_ADVENT_BLESSING);
								}
								try
								{
									Thread.sleep(Config.PHANTOM_PLAYERS_DELAY_SPAWN_FIRST);
								}
								catch (InterruptedException e)
								{
								}
								count++;
							}
						}
					}
					_log.info("[PhantomListener]: Loaded " + count + " phantom players in game, from 1st stage spawn!");
					
					ThreadPoolManager.getInstance().scheduleGeneral(new FantomTaskDespawn(1), Config.PHANTOM_PLAYERS_DESPAWN_FIRST);
					ThreadPoolManager.getInstance().scheduleGeneral(new FantomTask(2), Config.PHANTOM_PLAYERS_DELAY_NEXT);
					ThreadPoolManager.getInstance().scheduleGeneral(new Social(), 12000L);
					ThreadPoolManager.getInstance().scheduleGeneral(new CheckCount(), 300000L);
					break;
				case 2:
					_log.info("[PhantomListener]: Started 2st stage spawn!");
					while (count2 < Config.PHANTOM_PLAYERS_COUNT_NEXT)
					{
						L2PcInstance fantom2 = null;
						PhantomObjId2 = getRandomPhantomNext();
						if (!_phantomsList.contains(PhantomObjId2))
						{
							fantom2 = loadPhantom(PhantomObjId2);
							if (fantom2 != null)
							{
								_phantomPlayersList.get(2).add(fantom2);
								_phantomsList.add(fantom2.getObjectId());
								if (Rnd.get(100) < 3)
								{
									fantom2.sitDown();
								}
								try
								{
									Thread.sleep(Config.PHANTOM_PLAYERS_DELAY_SPAWN_NEXT);
								}
								catch (InterruptedException e)
								{
								}
								count2++;
							}
						}
					}
					_log.info("[PhantomListener]: Loaded " + count2 + " phantom players in game, from 2st stage spawn!");
					
					ThreadPoolManager.getInstance().scheduleGeneral(new FantomTaskDespawn(2), Config.PHANTOM_PLAYERS_DESPAWN_NEXT);
			}
		}
	}
	
	protected class PhantomTask implements Runnable
	{
		L2PcInstance _phantom;
		
		public PhantomTask(L2PcInstance phantom)
		{
			_phantom = phantom;
		}
		
		@Override
		public void run()
		{
			getRndMessage(_phantom);
			
			if (_phantom.isInsideZone(ZoneId.PEACE))
			{
				if (_phantom.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					if (_phantom.getTarget() == null)
					{
						if (Rnd.get(1000) < 1)
						{
							_phantom.sitDown();
						}
						
						if (Rnd.get(100) < 40)
						{
							searchTownNpc(_phantom);
						}
						else
						{
							_phantom.rndWalk();
						}
					}
					else
					{
						for (L2Npc npc : _phantom.getKnownList().getKnownNpcInRadius(100))
						{
							if ((npc != null) && (_phantom.getTarget() == npc))
							{
								if ((npc instanceof L2TrainerInstance) || (npc instanceof L2VillageMasterFighterInstance) || (npc instanceof L2VillageMasterPriestInstance))
								{
									try
									{
										Thread.sleep(Rnd.get(2000, 4000));
									}
									catch (InterruptedException e)
									{
									}
								}
								else if (npc instanceof L2MerchantInstance)
								{
									try
									{
										Thread.sleep(Rnd.get(4000, 6000));
									}
									catch (InterruptedException e)
									{
									}
								}
								else if (npc instanceof L2WarehouseInstance)
								{
									try
									{
										Thread.sleep(Rnd.get(4000, 6000));
									}
									catch (InterruptedException e)
									{
									}
								}
								else
								{
									try
									{
										Thread.sleep(Rnd.get(1000, 2000));
									}
									catch (InterruptedException e)
									{
									}
								}
								
								_phantom.setTarget(null);
								if ((Rnd.get(100) < 40) && (_phantom.getTarget() == null))
								{
									searchTownNpc(_phantom);
								}
								else
								{
									_phantom.rndWalk();
								}
							}
							else
							{
								Location npcLoc = new Location(_phantom.getTarget().getX() + Rnd.get(10, 30), _phantom.getTarget().getY() + Rnd.get(10, 30), _phantom.getTarget().getZ());
								_phantom.setRunning();
								_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, npcLoc);
							}
						}
					}
				}
				else if (_phantom.getAI().getIntention() == CtrlIntention.AI_INTENTION_REST)
				{
					if ((Rnd.get(100) < 10) && (Rnd.get(100) < 10) && (_phantom.isSitting()))
					{
						_phantom.standUp();
					}
				}
			}
			else
			{
				searchToAttackPlayer(_phantom);
				
				if (_phantom.isInsideZone(ZoneId.WATER))
				{
					_phantom.teleToClosestTown();
				}
				
				if (_phantom.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
				{
					return;
				}
				else if (_phantom.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					if (_phantom.getCurrentHp() < (_phantom.getMaxHp() * 0.2))
					{
						L2Skill heal = PhantomSkillsParser.getInstance().getRndHealSkill(_phantom.getClassId());
						if (heal != null)
						{
							_phantom.setTarget(_phantom);
							_phantom.doCast(heal);
							_phantom.setTarget(null);
						}
						else
						{
							_phantom.sitDown();
						}
					}
					else if (_phantom.getCurrentMp() < (_phantom.getMaxMp() * 0.03))
					{
						if (_phantom.getClassId().isMage())
						{
							_phantom.setCurrentMp(100 + _phantom.getCurrentMp());
							StatusUpdate su = new StatusUpdate(_phantom);
							su.addAttribute(StatusUpdate.CUR_MP, (int) _phantom.getCurrentMp());
							_phantom.sendPacket(su);
							_phantom.sitDown();
						}
					}
					getRandomBuff(_phantom);
				}
				else if (_phantom.getAI().getIntention() == CtrlIntention.AI_INTENTION_REST)
				{
					if (_phantom.isSitting())
					{
						if ((_phantom.getCurrentHp() == (_phantom.getMaxHp())) && (_phantom.getCurrentMp() == (_phantom.getMaxMp())))
						{
							_phantom.standUp();
							getRandomBuff(_phantom);
						}
					}
				}
			}
		}
	}
	
	protected void phantomGetAttack(L2PcInstance _phantom)
	{
		if (Rnd.get(100) < 80)
		{
			if (_phantom.getClassId().isMage())
			{
				phantomMagicAttack(_phantom);
			}
			else
			{
				phantomFighterAttack(_phantom);
			}
		}
		else
		{
			_phantom.rndWalk();
		}
	}
	
	protected void phantomMagicAttack(L2PcInstance _phantom)
	{
		checkSpiritShotCount(_phantom);
		if (!_phantom.isCastingNow() && (_phantom.getTarget() != null) && !((L2Character) _phantom.getTarget()).isAlikeDead())
		{
			checkMagicAttackSkill(_phantom, _phantom.getTarget());
		}
		else
		{
			for (L2MonsterInstance monster : _phantom.getKnownList().getKnownMonstersInRadius(1100))
			{
				if (monster != null)
				{
					if ((GeoClient.getInstance().canSeeTarget(_phantom, monster)) && (!monster.isDead()))
					{
						_phantom.setTarget(monster);
						checkMagicAttackSkill(_phantom, monster);
					}
					else
					{
						_phantom.setTarget(null);
						_phantom.rndWalk();
					}
				}
				else
				{
					_phantom.rndWalk();
				}
			}
		}
	}
	
	protected void phantomFighterAttack(L2PcInstance _phantom)
	{
		if (!_phantom.isCastingNow() && (_phantom.getTarget() != null) && !((L2Character) _phantom.getTarget()).isAlikeDead())
		{
			checkPhysicalAttackSkill(_phantom, _phantom.getTarget());
		}
		else
		{
			for (L2MonsterInstance monster : _phantom.getKnownList().getKnownMonstersInRadius(1000))
			{
				if (monster != null)
				{
					if ((GeoClient.getInstance().canSeeTarget(_phantom, monster)) && (!monster.isDead()))
					{
						_phantom.setTarget(monster);
						checkPhysicalAttackSkill(_phantom, monster);
					}
					else
					{
						_phantom.setTarget(null);
						_phantom.rndWalk();
					}
				}
				else
				{
					_phantom.rndWalk();
				}
			}
		}
	}
	
	protected void getRandomBuff(L2PcInstance phantom)
	{
		L2Skill rndbuff = PhantomSkillsParser.getInstance().getRndBuffSkill(phantom.getClassId());
		if (rndbuff != null)
		{
			Collection<L2Skill> skills = SkillTreesParser.getInstance().getAllAvailableSkills(phantom, phantom.getClassId(), true, true);
			for (L2Skill skill : skills)
			{
				if (phantom.getKnownSkill(rndbuff.getId()) == skill)
				{
					L2Skill buffSkill = SkillHolder.getInstance().getInfo(rndbuff.getId(), skill.getLevel());
					
					L2Effect[] effects = phantom.getAllEffects();
					if (effects.length < 1)
					{
						rndbuff.getEffects(phantom, phantom);
					}
					
					for (L2Effect e : effects)
					{
						if (e == null)
						{
							phantom.setTarget(phantom);
							phantom.doCast(buffSkill);
							phantom.setTarget(null);
						}
						else
						{
							if ((phantom.getFirstEffect(rndbuff) != null) || (e.getSkill() == rndbuff))
							{
								phantomGetAttack(phantom);
							}
							else
							{
								if (e.getAbnormalType().equals(rndbuff.getEffectTemplates()[0].abnormalType) && (e.getAbnormalLvl() >= rndbuff.getEffectTemplates()[0].abnormalLvl))
								{
									phantomGetAttack(phantom);
								}
								else
								{
									phantom.setTarget(phantom);
									phantom.doCast(buffSkill);
									phantom.setTarget(null);
								}
							}
						}
					}
				}
			}
		}
		else
		{
			phantomGetAttack(phantom);
		}
	}
	
	protected void getRndMessage(L2PcInstance _phantom)
	{
		if (Rnd.get(100) < 5)
		{
			CreatureSay cs, cs2, cs3;
			switch (Rnd.get(1, 3))
			{
				case 1:
					cs = new CreatureSay(_phantom.getObjectId(), Say2.SHOUT, _phantom.getName(), getRandomShoutMessage());
					for (L2PcInstance player : _phantom.getKnownList().getKnownPlayersInRadius(3000))
					{
						if (player != null)
						{
							player.sendPacket(cs);
						}
					}
					break;
				case 2:
					cs2 = new CreatureSay(_phantom.getObjectId(), Say2.TRADE, _phantom.getName(), getRandomShoutMessage());
					for (L2PcInstance player : _phantom.getKnownList().getKnownPlayersInRadius(2000))
					{
						if (player != null)
						{
							player.sendPacket(cs2);
						}
					}
					break;
				case 3:
					cs3 = new CreatureSay(_phantom.getObjectId(), Say2.ALL, _phantom.getName(), getRandomShoutMessage());
					for (L2PcInstance player : _phantom.getKnownList().getKnownPlayersInRadius(1000))
					{
						if (player != null)
						{
							player.sendPacket(cs3);
						}
					}
			}
		}
	}
	
	protected void searchToAttackPlayer(L2PcInstance _phantom)
	{
		for (L2PcInstance player : _phantom.getKnownList().getKnownPlayersInRadius(800))
		{
			if (player.isDead() || player.isAlikeDead())
			{
				continue;
			}
			
			if ((GeoClient.getInstance().canSeeTarget(_phantom, player)) && (!player.isInsideZone(ZoneId.PEACE)) && (!_phantom.isInsideZone(ZoneId.PEACE)) && ((player.getPvpFlag() > 0) || (player.getKarma() != 0)))
			{
				if (((player.getPvpFlag() > 0) && ((player.getLevel()) > _phantom.getLevel()) && (player.getKarma() == 0)) || (((player.getLevel() - 10) > _phantom.getLevel()) && (player.getKarma() == 0)))
				{
					if (_phantom.getClassId().isMage())
					{
						L2Skill rndMagicSkill = PhantomSkillsParser.getInstance().getRndMagicAttackSkill(_phantom.getActingPlayer().getClassId());
						if (rndMagicSkill != null)
						{
							Collection<L2Skill> skills = SkillTreesParser.getInstance().getAllAvailableSkills(_phantom, _phantom.getClassId(), true, true);
							for (L2Skill skill : skills)
							{
								if (_phantom.getKnownSkill(rndMagicSkill.getId()) == skill)
								{
									L2Skill attackSkill = SkillHolder.getInstance().getInfo(rndMagicSkill.getId(), skill.getLevel());
									if (!_phantom.isCastingNow() && (_phantom.getTarget() != null) && !((L2Character) _phantom.getTarget()).isAlikeDead())
									{
										if (_phantom.getCurrentMp() >= _phantom.getStat().getMpConsume(attackSkill))
										{
											_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, attackSkill, _phantom.getTarget());
										}
										else
										{
											_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
										}
									}
									else
									{
										if (_phantom.getCurrentMp() >= _phantom.getStat().getMpConsume(attackSkill))
										{
											_phantom.setTarget(player);
											_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, attackSkill, player);
										}
										else
										{
											_phantom.setTarget(player);
											_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
										}
									}
								}
							}
						}
						else
						{
							_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						}
					}
					else
					{
						L2Skill rndFighterSkill = PhantomSkillsParser.getInstance().getRndFighterAttackSkill(_phantom.getActingPlayer().getClassId());
						if (rndFighterSkill != null)
						{
							Collection<L2Skill> skills = SkillTreesParser.getInstance().getAllAvailableSkills(_phantom, _phantom.getClassId(), true, true);
							for (L2Skill skill : skills)
							{
								if (_phantom.getKnownSkill(rndFighterSkill.getId()) == skill)
								{
									L2Skill attackSkill = SkillHolder.getInstance().getInfo(rndFighterSkill.getId(), skill.getLevel());
									if (!_phantom.isCastingNow() && (_phantom.getTarget() != null) && !((L2Character) _phantom.getTarget()).isAlikeDead())
									{
										if (Rnd.get(100) < 40)
										{
											if (_phantom.getCurrentMp() >= _phantom.getStat().getMpConsume(attackSkill))
											{
												_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, attackSkill, player);
											}
											else
											{
												_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
											}
										}
										else
										{
											_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
										}
									}
									else
									{
										if (_phantom.getCurrentMp() >= _phantom.getStat().getMpConsume(attackSkill))
										{
											_phantom.setTarget(player);
											_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, attackSkill, player);
										}
										else
										{
											_phantom.setTarget(player);
											_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
										}
									}
								}
							}
						}
						else
						{
							_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						}
					}
				}
			}
		}
	}
	
	protected void checkMagicAttackSkill(L2PcInstance phantom, L2Object target)
	{
		L2Skill rndMagicSkill = PhantomSkillsParser.getInstance().getRndMagicAttackSkill(phantom.getActingPlayer().getClassId());
		if (rndMagicSkill != null)
		{
			Collection<L2Skill> skills = SkillTreesParser.getInstance().getAllAvailableSkills(phantom, phantom.getClassId(), true, true);
			for (L2Skill skill : skills)
			{
				if (phantom.getKnownSkill(rndMagicSkill.getId()) == skill)
				{
					L2Skill attackSkill = SkillHolder.getInstance().getInfo(rndMagicSkill.getId(), skill.getLevel());
					if (phantom.getCurrentMp() >= phantom.getStat().getMpConsume(attackSkill))
					{
						phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, attackSkill, target);
					}
					else
					{
						phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
				}
			}
		}
		else
		{
			phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
	}
	
	protected void checkPhysicalAttackSkill(L2PcInstance phantom, L2Object target)
	{
		if (Rnd.get(100) < 50)
		{
			L2Skill rndFighterSkill = PhantomSkillsParser.getInstance().getRndFighterAttackSkill(phantom.getActingPlayer().getClassId());
			if (rndFighterSkill != null)
			{
				Collection<L2Skill> skills = SkillTreesParser.getInstance().getAllAvailableSkills(phantom, phantom.getClassId(), true, true);
				for (L2Skill skill : skills)
				{
					if (phantom.getKnownSkill(rndFighterSkill.getId()) == skill)
					{
						L2Skill attackSkill = SkillHolder.getInstance().getInfo(rndFighterSkill.getId(), skill.getLevel());
						if (phantom.getCurrentMp() >= phantom.getStat().getMpConsume(attackSkill))
						{
							phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, attackSkill, target);
						}
						else
						{
							phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						}
					}
				}
			}
			else
			{
				phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		else
		{
			phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
	}
	
	protected void checkSpiritShotCount(L2PcInstance _phantom)
	{
		if ((_phantom.getClassId().level() == 0) && (_phantom.getInventory().getItemByItemId(3947) != null) && (_phantom.getInventory().getItemByItemId(3947).getCount() < 10))
		{
			L2ItemInstance item = _phantom.getInventory().addItem("BSSNG", 3947, 2500, _phantom, null);
			_phantom.getInventory().addItem(item);
		}
		else if ((_phantom.getClassId().level() == 1) && (_phantom.getInventory().getItemByItemId(3948) != null) && (_phantom.getInventory().getItemByItemId(3948).getCount() < 10))
		{
			L2ItemInstance item = _phantom.getInventory().addItem("BSSD", 3948, 2500, _phantom, null);
			_phantom.getInventory().addItem(item);
		}
		else if ((_phantom.getClassId().level() == 2) && (_phantom.getInventory().getItemByItemId(3949) != null) && (_phantom.getInventory().getItemByItemId(3949).getCount() < 10))
		{
			L2ItemInstance item = _phantom.getInventory().addItem("BSSC", 3949, 2500, _phantom, null);
			_phantom.getInventory().addItem(item);
		}
		else if ((_phantom.getClassId().level() == 2) && (_phantom.getInventory().getItemByItemId(3950) != null) && (_phantom.getInventory().getItemByItemId(3950).getCount() < 10))
		{
			L2ItemInstance item = _phantom.getInventory().addItem("BSSB", 3950, 2500, _phantom, null);
			_phantom.getInventory().addItem(item);
		}
		else if ((_phantom.getClassId().level() == 2) && (_phantom.getInventory().getItemByItemId(3951) != null) && (_phantom.getInventory().getItemByItemId(3951).getCount() < 10))
		{
			L2ItemInstance item = _phantom.getInventory().addItem("BSSA", 3951, 2500, _phantom, null);
			_phantom.getInventory().addItem(item);
		}
		else if ((_phantom.getClassId().level() == 3) && (_phantom.getInventory().getItemByItemId(3952) != null) && (_phantom.getInventory().getItemByItemId(3952).getCount() < 10))
		{
			L2ItemInstance item = _phantom.getInventory().addItem("BSSS", 3952, 2500, _phantom, null);
			_phantom.getInventory().addItem(item);
		}
	}
	
	protected void searchTownNpc(L2PcInstance _phantom)
	{
		searchNpcBuffer(_phantom);
		
		if (Rnd.get(100) < 40)
		{
			for (L2Npc npc : _phantom.getKnownList().getKnownNpcInRadius(1500))
			{
				if ((npc != null) && (npc instanceof L2MerchantInstance))
				{
					_phantom.setTarget(npc);
					Location npcLoc = new Location(npc.getX() + Rnd.get(10, 30), npc.getY() + Rnd.get(10, 30), npc.getZ());
					_phantom.setRunning();
					_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, npcLoc);
				}
			}
		}
		else if (Rnd.get(100) < 30)
		{
			for (L2Npc npc : _phantom.getKnownList().getKnownNpcInRadius(1500))
			{
				if ((npc != null) && (npc instanceof L2TrainerInstance))
				{
					_phantom.setTarget(npc);
					Location npcLoc = new Location(npc.getX() + Rnd.get(10, 30), npc.getY() + Rnd.get(10, 30), npc.getZ());
					_phantom.setRunning();
					_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, npcLoc);
				}
			}
		}
		else if (Rnd.get(100) < 20)
		{
			for (L2Npc npc : _phantom.getKnownList().getKnownNpcInRadius(1500))
			{
				if (npc != null)
				{
					if (_phantom.getClassId().isMage())
					{
						if (npc instanceof L2VillageMasterPriestInstance)
						{
							_phantom.setTarget(npc);
							Location npcLoc = new Location(npc.getX() + Rnd.get(10, 30), npc.getY() + Rnd.get(10, 30), npc.getZ());
							_phantom.setRunning();
							_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, npcLoc);
						}
						else
						{
							_phantom.rndWalk();
						}
					}
					else
					{
						if (npc instanceof L2VillageMasterFighterInstance)
						{
							_phantom.setTarget(npc);
							Location npcLoc = new Location(npc.getX() + Rnd.get(10, 30), npc.getY() + Rnd.get(10, 30), npc.getZ());
							_phantom.setRunning();
							_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, npcLoc);
						}
						else
						{
							_phantom.rndWalk();
						}
					}
				}
			}
		}
		else if (Rnd.get(100) < 10)
		{
			for (L2Npc npc : _phantom.getKnownList().getKnownNpcInRadius(1500))
			{
				if ((npc != null) && (npc instanceof L2WarehouseInstance))
				{
					_phantom.setTarget(npc);
					Location npcLoc = new Location(npc.getX() + Rnd.get(10, 30), npc.getY() + Rnd.get(10, 30), npc.getZ());
					_phantom.setRunning();
					_phantom.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, npcLoc);
				}
			}
		}
		
		if (Rnd.get(1000) < 1)
		{
			_phantom.sitDown();
		}
	}
	
	protected void searchNpcBuffer(L2PcInstance _phantom)
	{
		for (L2Npc npc : _phantom.getKnownList().getKnownNpcInRadius(100))
		{
			for (int npcId : NPC_BUFFERS)
			{
				if ((npc.getId() == npcId) && (!_phantom.isSitting()) && (_phantom.getLevel() < 76) && (_phantom.getFirstEffect(SkillHolder.getInstance().getInfo(4323, 1)) == null))
				{
					npc.setTarget(_phantom);
					_phantom.stopMove(null);
					try
					{
						Thread.sleep(Rnd.get(1500, 2500));
					}
					catch (InterruptedException e)
					{
					}
					
					if (CategoryParser.getInstance().isInCategory(CategoryType.BEGINNER_MAGE, _phantom.getClassId().getId()))
					{
						for (SkillsHolder skill : MAGE_BUFFS)
						{
							npc.doCast(skill.getSkill());
						}
					}
					else
					{
						for (SkillsHolder skill : FIGHTER_BUFFS)
						{
							npc.doCast(skill.getSkill());
						}
					}
					
					if ((_phantom.getLevel() >= 16) && (_phantom.getLevel() <= 34))
					{
						_phantom.doSimultaneousCast(CUBIC.getSkill());
					}
				}
			}
		}
	}
	
	public static L2Phantom getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final L2Phantom _instance = new L2Phantom();
	}
}