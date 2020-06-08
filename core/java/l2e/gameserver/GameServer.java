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
package l2e.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import l2e.Config;
import l2e.EternityWorld;
import l2e.L2DatabaseFactory;
import l2e.Server;
import l2e.gameserver.cache.CrestCache;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.communitybbs.Manager.AccountBBSManager;
import l2e.gameserver.communitybbs.Manager.BuffBBSManager;
import l2e.gameserver.communitybbs.Manager.ClanBBSManager;
import l2e.gameserver.communitybbs.Manager.ClassBBSManager;
import l2e.gameserver.communitybbs.Manager.CustomServiceBBSManager;
import l2e.gameserver.communitybbs.Manager.EnchantBBSManager;
import l2e.gameserver.communitybbs.Manager.EventBBSManager;
import l2e.gameserver.communitybbs.Manager.FavoritesBBSManager;
import l2e.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2e.gameserver.communitybbs.Manager.FriendsBBSManager;
import l2e.gameserver.communitybbs.Manager.LinkBBSManager;
import l2e.gameserver.communitybbs.Manager.MailBBSManager;
import l2e.gameserver.communitybbs.Manager.PostBBSManager;
import l2e.gameserver.communitybbs.Manager.RegionBBSManager;
import l2e.gameserver.communitybbs.Manager.ServiceBBSManager;
import l2e.gameserver.communitybbs.Manager.ShopBBSManager;
import l2e.gameserver.communitybbs.Manager.StateBBSManager;
import l2e.gameserver.communitybbs.Manager.TeleportBBSManager;
import l2e.gameserver.communitybbs.Manager.TopBBSManager;
import l2e.gameserver.communitybbs.Manager.TopicBBSManager;
import l2e.gameserver.data.sql.CharColorHolder;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.sql.CharSummonHolder;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.NevitHolder;
import l2e.gameserver.data.sql.NpcBufferHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.OfflineTradersHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.data.sql.SummonSkillsHolder;
import l2e.gameserver.data.xml.AIOItemParser;
import l2e.gameserver.data.xml.AdminParser;
import l2e.gameserver.data.xml.ArmorSetsParser;
import l2e.gameserver.data.xml.AugmentationParser;
import l2e.gameserver.data.xml.BotReportParser;
import l2e.gameserver.data.xml.BuyListParser;
import l2e.gameserver.data.xml.CategoryParser;
import l2e.gameserver.data.xml.CharTemplateParser;
import l2e.gameserver.data.xml.ClassListParser;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.data.xml.EnchantItemGroupsParser;
import l2e.gameserver.data.xml.EnchantItemHPBonusParser;
import l2e.gameserver.data.xml.EnchantItemOptionsParser;
import l2e.gameserver.data.xml.EnchantItemParser;
import l2e.gameserver.data.xml.EnchantSkillGroupsParser;
import l2e.gameserver.data.xml.ExperienceParser;
import l2e.gameserver.data.xml.FishMonstersParser;
import l2e.gameserver.data.xml.FishParser;
import l2e.gameserver.data.xml.HennaParser;
import l2e.gameserver.data.xml.HerbDropParser;
import l2e.gameserver.data.xml.HitConditionBonusParser;
import l2e.gameserver.data.xml.InitialEquipmentParser;
import l2e.gameserver.data.xml.ItemIconsParser;
import l2e.gameserver.data.xml.ManorParser;
import l2e.gameserver.data.xml.MerchantPriceParser;
import l2e.gameserver.data.xml.MultiSellParser;
import l2e.gameserver.data.xml.OptionsParser;
import l2e.gameserver.data.xml.PetsParser;
import l2e.gameserver.data.xml.PhantomArmorParser;
import l2e.gameserver.data.xml.PhantomLocationParser;
import l2e.gameserver.data.xml.PhantomMessagesParser;
import l2e.gameserver.data.xml.PhantomSkillsParser;
import l2e.gameserver.data.xml.ProductItemParser;
import l2e.gameserver.data.xml.RecipeParser;
import l2e.gameserver.data.xml.SchemesParser;
import l2e.gameserver.data.xml.SkillLearnParser;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.data.xml.SpawnParser;
import l2e.gameserver.data.xml.StaticObjectsParser;
import l2e.gameserver.data.xml.TeleLocationParser;
import l2e.gameserver.data.xml.TransformParser;
import l2e.gameserver.data.xml.UIParser;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.geoeditorcon.GeoEditorListener;
import l2e.gameserver.handler.ActionHandler;
import l2e.gameserver.handler.ActionShiftHandler;
import l2e.gameserver.handler.AdminCommandHandler;
import l2e.gameserver.handler.BypassHandler;
import l2e.gameserver.handler.ChatHandler;
import l2e.gameserver.handler.EffectHandler;
import l2e.gameserver.handler.ItemHandler;
import l2e.gameserver.handler.PunishmentHandler;
import l2e.gameserver.handler.SkillHandler;
import l2e.gameserver.handler.TargetHandler;
import l2e.gameserver.handler.TelnetHandler;
import l2e.gameserver.handler.UserCommandHandler;
import l2e.gameserver.handler.VoicedCommandHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.instancemanager.AuctionManager;
import l2e.gameserver.instancemanager.BoatManager;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.CoupleManager;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.DayNightSpawnManager;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.instancemanager.EventsDropManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.FourSepulchersManager;
import l2e.gameserver.instancemanager.FunEventsManager;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.ItemAuctionManager;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.instancemanager.KrateisCubeManager;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.instancemanager.PetitionManager;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.RaidBossPointsManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.SiegeRewardManager;
import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.UndergroundColiseumManager;
import l2e.gameserver.instancemanager.VoteRewardManager;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.instancemanager.games.FishingChampionship;
import l2e.gameserver.instancemanager.leaderboards.ArenaLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.CraftLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.FishermanLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.TvTLeaderboard;
import l2e.gameserver.model.AutoSpawnHandler;
import l2e.gameserver.model.EventDroplist;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.PartyMatchRoomList;
import l2e.gameserver.model.PartyMatchWaitingList;
import l2e.gameserver.model.actor.L2Phantom;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.entity.VoteRewardHopzone;
import l2e.gameserver.model.entity.VoteRewardTopzone;
import l2e.gameserver.model.entity.events.Hitman;
import l2e.gameserver.model.entity.events.Leprechaun;
import l2e.gameserver.model.entity.events.MRManager;
import l2e.gameserver.model.entity.events.TvTManager;
import l2e.gameserver.model.entity.events.TvTRoundManager;
import l2e.gameserver.model.entity.events.phoenix.Interface;
import l2e.gameserver.model.entity.mods.AchievementsManager;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.L2GamePacketHandler;
import l2e.gameserver.scripting.L2ScriptEngineManager;
import l2e.gameserver.taskmanager.AutoAnnounceTaskManager;
import l2e.gameserver.taskmanager.KnownListUpdateTaskManager;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.protection.ConfigProtection;
import l2e.protection.Protection;
import l2e.status.Status;
import l2e.util.DeadLockDetector;
import l2e.util.IPv4Filter;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private final LoginServerThread _loginThread;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public L2GamePacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		_log.finest("used mem:" + getUsedMemoryMB() + "MB");
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.severe("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		ConfigProtection.load();
		
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		new File("log/game").mkdirs();
		
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("World");
		GameTimeController.init();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		EventsDropManager.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Data");
		CategoryParser.getInstance();
		
		printSection("Skills");
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsParser.getInstance();
		SkillTreesParser.getInstance();
		SkillHolder.getInstance();
		SummonSkillsHolder.getInstance();
		SchemesParser.getInstance();
		
		printSection("Items");
		ItemHolder.getInstance();
		ItemIconsParser.getInstance();
		ProductItemParser.getInstance();
		EnchantItemGroupsParser.getInstance();
		EnchantItemParser.getInstance();
		EnchantItemOptionsParser.getInstance();
		OptionsParser.getInstance();
		EnchantItemHPBonusParser.getInstance();
		MerchantPriceParser.getInstance().loadInstances();
		BuyListParser.getInstance();
		MultiSellParser.getInstance();
		RecipeParser.getInstance();
		ArmorSetsParser.getInstance();
		FishMonstersParser.getInstance();
		FishParser.getInstance();
		HennaParser.getInstance();
		
		printSection("Characters");
		ClassListParser.getInstance();
		InitialEquipmentParser.getInstance();
		ExperienceParser.getInstance();
		HitConditionBonusParser.getInstance();
		CharTemplateParser.getInstance();
		CharNameHolder.getInstance();
		NevitHolder.getInstance();
		AdminParser.getInstance();
		RaidBossPointsManager.getInstance();
		PetsParser.getInstance();
		CharSummonHolder.getInstance().init();
		
		printSection("Clans");
		ClanHolder.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("Geodata");
		GeoClient.getInstance();
		
		printSection("NPCs");
		HerbDropParser.getInstance();
		SkillLearnParser.getInstance();
		NpcTable.getInstance();
		WalkingManager.getInstance();
		StaticObjectsParser.getInstance();
		ZoneManager.getInstance();
		DoorParser.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferHolder.getInstance();
		SpawnTable.getInstance();
		SpawnParser.getInstance();
		HellboundManager.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		EventDroplist.getInstance();
		BotReportParser.getInstance();
		
		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		ManorParser.getInstance();
		
		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		
		printSection("Cache");
		HtmCache.getInstance();
		CrestCache.getInstance();
		TeleLocationParser.getInstance();
		UIParser.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		AugmentationParser.getInstance();
		CursedWeaponsManager.getInstance();
		TransformParser.getInstance();
		
		printSection("Handlers");
		AutoSpawnHandler.getInstance();
		_log.info("Loaded " + AutoSpawnHandler.getInstance().size() + " AutoSpawnHandlers");
		ActionHandler.getInstance();
		ActionShiftHandler.getInstance();
		AdminCommandHandler.getInstance();
		BypassHandler.getInstance();
		ChatHandler.getInstance();
		ItemHandler.getInstance();
		PunishmentHandler.getInstance();
		SkillHandler.getInstance();
		TargetHandler.getInstance();
		TelnetHandler.getInstance();
		UserCommandHandler.getInstance();
		VoicedCommandHandler.getInstance();
		
		printSection("Gracia Seeds");
		SoDManager.getInstance();
		SoIManager.getInstance();
		
		printSection("Community Board");
		if (Config.COMMUNITY_TYPE == 0)
		{
			_log.info("Community Board Disabled all functions.");
		}
		
		if (Config.COMMUNITY_TYPE == 1)
		{
			if (Config.ALLOW_COMMUNITY_REGION_MANAGER)
			{
				RegionBBSManager.getInstance();
			}
		}
		
		if (Config.COMMUNITY_TYPE == 2)
		{
			if (Config.ALLOW_COMMUNITY_ACCOUNT)
			{
				AccountBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_BUFF)
			{
				BuffBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_CLAN_MANAGER)
			{
				ClanBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_CLASS)
			{
				ClassBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_ENCHANT)
			{
				EnchantBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_EVENTS)
			{
				EventBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_FAVORITE_MANAGER)
			{
				FavoritesBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_FRIENDS_MANAGER)
			{
				ForumsBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_LINK_MANAGER)
			{
				FriendsBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_LINK_MANAGER)
			{
				LinkBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_MAIL_MANAGER)
			{
				MailBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_POST_MANAGER)
			{
				PostBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_REGION_MANAGER)
			{
				RegionBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_SERVICES)
			{
				ServiceBBSManager.getInstance();
				CustomServiceBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_MULTISELL)
			{
				ShopBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_STATS)
			{
				StateBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_TELEPORT)
			{
				TeleportBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_TOP_MANAGER)
			{
				TopBBSManager.getInstance();
			}
			
			if (Config.ALLOW_COMMUNITY_TOPIC_MANAGER)
			{
				TopicBBSManager.getInstance();
			}
		}
		
		printSection("Scripts");
		QuestManager.getInstance();
		BoatManager.getInstance();
		FishingChampionship.getInstance();
		SiegeRewardManager.getInstance();
		if (Config.ALLOW_VOTE_REWARD_SYSTEM)
		{
			VoteRewardManager.getInstance();
		}
		AirShipManager.getInstance();
		UndergroundColiseumManager.getInstance();
		
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		MerchantPriceParser.getInstance().updateReferences();
		
		_log.info("Loading Server Scripts");
		L2ScriptEngineManager.getInstance().executeScriptList();
		
		QuestManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		KrateisCubeManager.getInstance().init();
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		
		if (Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		
		if (Config.RANK_ARENA_ENABLED)
		{
			ArenaLeaderboard.getInstance();
		}
		
		if (Config.RANK_FISHERMAN_ENABLED)
		{
			FishermanLeaderboard.getInstance();
		}
		
		if (Config.RANK_CRAFT_ENABLED)
		{
			CraftLeaderboard.getInstance();
		}
		
		if (Config.RANK_TVT_ENABLED)
		{
			TvTLeaderboard.getInstance();
		}
		
		if (Config.ALLOW_HOPZONE_VOTE_REWARD)
		{
			VoteRewardHopzone.getInstance();
		}
		
		if (Config.ALLOW_TOPZONE_VOTE_REWARD)
		{
			VoteRewardTopzone.getInstance();
		}
		
		AchievementsManager.getInstance();
		
		TaskManager.getInstance();
		
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			GeoEditorListener.getInstance();
		}
		
		PunishmentManager.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		printSection("Protection System");
		if (Protection.isProtectionOn())
		{
			_log.info("[Protection]: System is loading.");
			Protection.Init();
		}
		else
		{
			_log.info("[Protection]: System is disabled.");
		}
		
		printSection("Eternity-World Mods");
		CharColorHolder.getInstance();
		AIOItemParser.getInstance();
		
		printSection("Events");
		FunEventsManager.getInstance().autoStartEvents();
		TvTManager.getInstance();
		TvTRoundManager.getInstance();
		Hitman.start();
		if (Config.ENABLED_LEPRECHAUN)
		{
			Leprechaun.getInstance();
		}
		if (Config.MR_ENABLED)
		{
			MRManager.getInstance();
		}
		
		if (Config.ENABLE_EVENT_ENGINE)
		{
			Interface.start();
		}
		
		printSection("Phantom Mode");
		if (Config.ALLOW_PHANTOM_PLAYERS)
		{
			_log.info("[PhantomListener]: Phantom players mode loading...");
			PhantomLocationParser.getInstance();
			PhantomArmorParser.getInstance();
			PhantomSkillsParser.getInstance();
			PhantomMessagesParser.getInstance();
			L2Phantom.getInstance();
		}
		else
		{
			_log.info("[PhantomListener]: Phantom players mode is disabled.");
		}
		
		printSection("Other");
		if (Config.AUTO_RESTART_ENABLE)
		{
			AutoRestart.getInstance();
		}
		else
		{
			_log.info("[Auto Restart]: System is disabled.");
		}
		
		KnownListUpdateTaskManager.getInstance();
		
		if (Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL > 0)
		{
			OnlinePlayers.getInstance();
		}
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersHolder.getInstance().restoreOfflineTraders();
		}
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();
		long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info("GameServer Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		Toolkit.getDefaultToolkit().beep();
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;
		
		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.log(Level.SEVERE, "WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		long serverLoadEnd = System.currentTimeMillis();
		_log.info("Server Loaded in " + ((serverLoadEnd - serverLoadStart) / 1000) + " seconds");
		
		AutoAnnounceTaskManager.getInstance();
		
		EternityWorld.info();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		final String LOG_FOLDER = "log";
		final String LOG_NAME = "./log.ini";
		
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		Config.load();
		printSection("Database");
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info("Telnet server is currently disabled.");
		}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
}