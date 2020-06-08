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
package l2e;

import info.tak11.subnet.Subnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.util.FloodProtectorConfig;
import l2e.gameserver.util.Util;
import l2e.geoserver.geodata.PathFindBuffers;
import l2e.util.L2Properties;
import l2e.util.StringUtil;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class Config
{
	private static final Logger _log = Logger.getLogger(Config.class.getName());
	
	public static final String EOL = System.getProperty("line.separator");
	
	// --------------------------------------------------
	// L2J Eternity-World Property File Definitions
	// --------------------------------------------------
	// Game Server
	public static final String BW_FILE = "./config/events/bw_event.ini";
	public static final String CTF_FILE = "./config/events/ctf_event.ini";
	public static final String DM_FILE = "./config/events/dm_event.ini";
	public static final String FUN_EVENTS_FILE = "./config/events/funevents_settings.ini";
	public static final String TVT_CONFIG_FILE = "./config/events/tvt_event.ini";
	public static final String TVT_ROUND_CONFIGURATION_FILE = "./config/events/tvtround_event.ini";
	public static final String TW_CONFIG_FILE = "./config/events/tw_event.ini";
	public static final String HITMAN_CONFIG = "./config/events/hitman_event.ini";
	public static final String UNDERGROUND_CONFIG_FILE = "./config/events/undergroundColiseum.ini";
	public static final String CUSTOM_EVENTS_FILE = "./config/events/customEvents_settings.ini";
	public static final String LASTHERO_FILE = "./config/events/lasthero_event.ini";
	public static final String LEPRECHAUN_FILE = "./config/events/leprechaun_event.ini";
	public static final String MR_FILES = "./config/events/monsterRush.ini";
	
	public static final String CHARACTER_CONFIG_FILE = "./config/main/character.ini";
	public static final String FEATURE_CONFIG_FILE = "./config/main/feature.ini";
	public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/main/fortsiege.ini";
	public static final String GENERAL_CONFIG_FILE = "./config/main/general.ini";
	public static final String ID_CONFIG_FILE = "./config/main/idfactory.ini";
	public static final String NPC_CONFIG_FILE = "./config/main/npc.ini";
	public static final String PVP_CONFIG_FILE = "./config/main/pvp.ini";
	public static final String RATES_CONFIG_FILE = "./config/main/rates.ini";
	public static final String SIEGE_CONFIGURATION_FILE = "./config/main/siege.ini";
	public static final String TW_CONFIGURATION_FILE = "./config/main/territorywar.ini";
	public static final String FLOOD_PROTECTOR_FILE = "./config/main/floodprotector.ini";
	public static final String MMO_CONFIG_FILE = "./config/main/mmo.ini";
	public static final String OLYMPIAD_CONFIG_FILE = "./config/main/olympiad.ini";
	public static final String GRANDBOSS_CONFIG_FILE = "./config/main/grandboss.ini";
	public static final String GRACIASEEDS_CONFIG_FILE = "./config/main/graciaSeeds.ini";
	public static final String CHAT_FILTER_FILE = "./config/main/chatfilter.txt";
	public static final String SECURITY_CONFIG_FILE = "./config/main/security.ini";
	public static final String CH_SIEGE_FILE = "./config/main/clanhallSiege.ini";
	public static final String LANGUAGE_FILE = "./config/main/language.ini";
	public static final String VOICE_CONFIG_FILE = "./config/main/voicecommands.ini";
	public static final String CUSTOM_FILE = "./config/main/custom.ini";
	public static final String PREMIUM_CONFIG_FILE = "./config/main/premiumAccount.ini";
	public static final String COMMUNITY_BOARD_CONFIG_FILE = "./config/main/communityBoard.ini";
	public static final String ENCHANT_CONFIG_FILE = "./config/main/enchant.ini";
	public static final String ITEM_MALL_CONFIG_FILE = "./config/main/itemMall.ini";
	public static final String GEO_CONFIG_FILE = "./config/main/geodata.ini";
	
	public static final String LEADERBOARDS_CONFIG_FILE = "./config/mods/leaderboards.ini";
	public static final String PCBANG_CONFIG_FILE = "./config/mods/pcPoints.ini";
	public static final String CHAMPION_CONFIG_FILE = "./config/mods/champion.ini";
	public static final String WEDDING_CONFIG_FILE = "./config/mods/wedding.ini";
	public static final String OFFLINE_TRADE_CONFIG_FILE = "./config/mods/offline_trade.ini";
	public static final String ANTIFEED_CONFIG_FILE = "./config/mods/antiFeed.ini";
	public static final String DUALBOX_CONFIG_FILE = "./config/mods/dualbox.ini";
	public static final String VOTE_CONFIG_FILE = "./config/mods/vote.ini";
	public static final String AIO_CONFIG_FILE = "./config/mods/aioconfig.ini";
	public static final String OLY_ANTI_FEED_FILE = "./config/mods/olympiadAntiFeed.ini";
	public static final String NPC_BUFFER = "./config/mods/buffer.ini";
	public static final String ANTIBOT_CONFIG = "./config/mods/antiBot.ini";
	public static final String CUSTOM_RATES_CONFIG = "./config/mods/customRates.ini";
	public static final String PHANTOMS_CONFIG_FILE = "./config/mods/phantoms/phantoms.ini";
	
	public static final String CONFIGURATION_FILE = "./config/network/server.ini";
	public static final String TELNET_FILE = "./config/network/telnet.ini";
	
	public static final String DELEVEL_CONFIG_FILE = "./config/scripts/delevel_npc.ini";
	public static final String RENAME_CONFIG_FILE = "./config/scripts/rename_npc.ini";
	public static final String COLORNAME_NPC_CONFIG = "./config/scripts/colorname_npc.ini";
	public static final String SERVERINFO_NPC_CONFIG = "./config/scripts/serverinfo_npc.ini";
	
	public static final String IP_CONFIG_FILE = "./config/ipconfig.xml";
	public static final String HEXID_FILE = "./config/hexid.txt";
	
	// Login Server
	public static final String EMAIL_CONFIG_FILE = "./config/main/email.ini";
	
	public static final String LOGIN_CONFIGURATION_FILE = "./config/network/loginserver.ini";
	
	// --------------------------------------------------
	// L2J Variable Definitions
	// --------------------------------------------------
	public static boolean ALT_GAME_DELEVEL;
	public static boolean DECREASE_SKILL_LEVEL;
	public static double ALT_WEIGHT_LIMIT;
	public static int RUN_SPD_BOOST;
	public static int DEATH_PENALTY_CHANCE;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean ALT_GAME_TIREDNESS;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	public static boolean ENABLE_MODIFY_SKILL_REUSE;
	public static Map<Integer, Integer> SKILL_REUSE_LIST;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_FS_SKILLS;
	public static boolean AUTO_LOOT_HERBS;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte TRIGGERED_BUFFS_MAX_AMOUNT;
	public static byte DANCES_MAX_AMOUNT;
	public static boolean DANCE_CANCEL_BUFF;
	public static boolean DANCE_CONSUME_ADDITIONAL_MP;
	public static boolean ALT_STORE_DANCES;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean EFFECT_CANCELING;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean SUBCLASS_STORE_SKILL_COOLTIME;
	public static boolean SUBCLASS_STORE_SKILL;
	public static boolean SUMMON_STORE_SKILL_COOLTIME;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean ALLOW_CLASS_MASTERS;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_ENTIRE_TREE;
	public static boolean ALTERNATE_CLASS_MASTER;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;
	public static boolean ALT_GAME_SUBCLASS_ALL_CLASSES;
	public static boolean ALLOW_TRANSFORM_WITHOUT_QUEST;
	public static int FEE_DELETE_TRANSFER_SKILLS;
	public static int FEE_DELETE_SUBCLASS_SKILLS;
	public static boolean RESTORE_SERVITOR_ON_RECONNECT;
	public static boolean RESTORE_PET_ON_RECONNECT;
	public static double MAX_BONUS_EXP;
	public static double MAX_BONUS_SP;
	public static int MAX_RUN_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_EVASION;
	public static int MIN_ABNORMAL_STATE_SUCCESS_RATE;
	public static int MAX_ABNORMAL_STATE_SUCCESS_RATE;
	public static byte MAX_SUBCLASS;
	public static byte BASE_SUBCLASS_LEVEL;
	public static byte MAX_SUBCLASS_LEVEL;
	public static int MAX_PVTSTORESELL_SLOTS_DWARF;
	public static int MAX_PVTSTORESELL_SLOTS_OTHER;
	public static int MAX_PVTSTOREBUY_SLOTS_DWARF;
	public static int MAX_PVTSTOREBUY_SLOTS_OTHER;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int INVENTORY_MAXIMUM_QUEST_ITEMS;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int ALT_FREIGHT_SLOTS;
	public static int ALT_FREIGHT_PRICE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static int MAX_PERSONAL_FAME_POINTS;
	public static int FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	public static int FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	public static int CASTLE_ZONE_FAME_TASK_FREQUENCY;
	public static int CASTLE_ZONE_FAME_AQUIRE_POINTS;
	public static boolean FAME_FOR_DEAD_PLAYERS;
	public static boolean IS_CRAFTING_ENABLED;
	public static boolean CRAFT_MASTERWORK;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_RARE_XPSP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static int ALT_CLAN_LEADER_DATE_CHANGE;
	public static String ALT_CLAN_LEADER_HOUR_CHANGE;
	public static boolean ALT_CLAN_LEADER_INSTANT_ACTIVATION;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static boolean ALT_LEAVE_PARTY_LEADER;
	public static boolean INITIAL_EQUIPMENT_EVENT;
	public static long STARTING_ADENA;
	public static byte STARTING_LEVEL;
	public static int STARTING_SP;
	public static long MAX_ADENA;
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAIDS;
	public static int LOOT_RAIDS_PRIVILEGE_INTERVAL;
	public static int LOOT_RAIDS_PRIVILEGE_CC_SIZE;
	public static int UNSTUCK_INTERVAL;
	public static int TELEPORT_WATCHDOG_TIMEOUT;
	public static int PLAYER_SPAWN_PROTECTION;
	public static List<Integer> SPAWN_PROTECTION_ALLOWED_ITEMS;
	public static int PLAYER_TELEPORT_PROTECTION;
	public static boolean RANDOM_RESPAWN_IN_TOWN_ENABLED;
	public static boolean OFFSET_ON_TELEPORT_ENABLED;
	public static int MAX_OFFSET_ON_TELEPORT;
	public static boolean RESTORE_PLAYER_INSTANCE;
	public static boolean ALLOW_SUMMON_TO_INSTANCE;
	public static int EJECT_DEAD_PLAYER_TIME;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static int DELETE_DAYS;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static int[][] PARTY_XP_CUTOFF_GAPS;
	public static int[] PARTY_XP_CUTOFF_GAP_PERCENTS;
	public static boolean DISABLE_TUTORIAL;
	public static boolean EXPERTISE_PENALTY;
	public static boolean STORE_RECIPE_SHOPLIST;
	public static boolean STORE_UI_SETTINGS;
	public static String[] FORBIDDEN_NAMES;
	public static boolean SILENCE_MODE_EXCLUDE;
	public static boolean ALT_VALIDATE_TRIGGER_SKILLS;
	public static float ALT_DAGGER_DMG_VS_HEAVY;
	public static float ALT_DAGGER_DMG_VS_ROBE;
	public static float ALT_DAGGER_DMG_VS_LIGHT;
	public static float ALT_BOW_DMG_VS_HEAVY;
	public static float ALT_BOW_DMG_VS_ROBE;
	public static float ALT_BOW_DMG_VS_LIGHT;
	public static float ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_MAGES_MAGICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_PETS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_PETS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_NPC_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_NPC_MAGICAL_DAMAGE_MULTI;
	public static float PATK_SPEED_MULTI;
	public static float MATK_SPEED_MULTI;
	public static boolean RESTORE_DISPEL_SKILLS;
	public static int RESTORE_DISPEL_SKILLS_TIME;
	public static boolean ALT_GAME_VIEWPLAYER;
	
	// --------------------------------------------------
	// ClanHall Settings
	// --------------------------------------------------
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	public static boolean CH_BUFF_FREE;
	
	// --------------------------------------------------
	// Castle Settings
	// --------------------------------------------------
	public static long CS_TELE_FEE_RATIO;
	public static int CS_TELE1_FEE;
	public static int CS_TELE2_FEE;
	public static long CS_MPREG_FEE_RATIO;
	public static int CS_MPREG1_FEE;
	public static int CS_MPREG2_FEE;
	public static int CS_MPREG3_FEE;
	public static int CS_MPREG4_FEE;
	public static long CS_HPREG_FEE_RATIO;
	public static int CS_HPREG1_FEE;
	public static int CS_HPREG2_FEE;
	public static int CS_HPREG3_FEE;
	public static int CS_HPREG4_FEE;
	public static int CS_HPREG5_FEE;
	public static long CS_EXPREG_FEE_RATIO;
	public static int CS_EXPREG1_FEE;
	public static int CS_EXPREG2_FEE;
	public static int CS_EXPREG3_FEE;
	public static int CS_EXPREG4_FEE;
	public static long CS_SUPPORT_FEE_RATIO;
	public static int CS_SUPPORT1_FEE;
	public static int CS_SUPPORT2_FEE;
	public static int CS_SUPPORT3_FEE;
	public static int CS_SUPPORT4_FEE;
	public static List<Integer> SIEGE_HOUR_LIST;
	
	// --------------------------------------------------
	// Fortress Settings
	// --------------------------------------------------
	public static long FS_TELE_FEE_RATIO;
	public static int FS_TELE1_FEE;
	public static int FS_TELE2_FEE;
	public static long FS_MPREG_FEE_RATIO;
	public static int FS_MPREG1_FEE;
	public static int FS_MPREG2_FEE;
	public static long FS_HPREG_FEE_RATIO;
	public static int FS_HPREG1_FEE;
	public static int FS_HPREG2_FEE;
	public static long FS_EXPREG_FEE_RATIO;
	public static int FS_EXPREG1_FEE;
	public static int FS_EXPREG2_FEE;
	public static long FS_SUPPORT_FEE_RATIO;
	public static int FS_SUPPORT1_FEE;
	public static int FS_SUPPORT2_FEE;
	public static int FS_BLOOD_OATH_COUNT;
	public static int FS_UPDATE_FRQ;
	public static int FS_MAX_SUPPLY_LEVEL;
	public static int FS_FEE_FOR_CASTLE;
	public static int FS_MAX_OWN_TIME;
	
	// --------------------------------------------------
	// Feature Settings
	// --------------------------------------------------
	public static int TAKE_FORT_POINTS;
	public static int LOOSE_FORT_POINTS;
	public static int TAKE_CASTLE_POINTS;
	public static int LOOSE_CASTLE_POINTS;
	public static int CASTLE_DEFENDED_POINTS;
	public static int FESTIVAL_WIN_POINTS;
	public static int HERO_POINTS;
	public static int ROYAL_GUARD_COST;
	public static int KNIGHT_UNIT_COST;
	public static int KNIGHT_REINFORCE_COST;
	public static int BALLISTA_POINTS;
	public static int BLOODALLIANCE_POINTS;
	public static int BLOODOATH_POINTS;
	public static int KNIGHTSEPAULETTE_POINTS;
	public static int REPUTATION_SCORE_PER_KILL;
	public static int JOIN_ACADEMY_MIN_REP_SCORE;
	public static int JOIN_ACADEMY_MAX_REP_SCORE;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static int CLAN_LEVEL_6_COST;
	public static int CLAN_LEVEL_7_COST;
	public static int CLAN_LEVEL_8_COST;
	public static int CLAN_LEVEL_9_COST;
	public static int CLAN_LEVEL_10_COST;
	public static int CLAN_LEVEL_11_COST;
	public static int CLAN_LEVEL_6_REQUIREMENT;
	public static int CLAN_LEVEL_7_REQUIREMENT;
	public static int CLAN_LEVEL_8_REQUIREMENT;
	public static int CLAN_LEVEL_9_REQUIREMENT;
	public static int CLAN_LEVEL_10_REQUIREMENT;
	public static int CLAN_LEVEL_11_REQUIREMENT;
	public static boolean ALLOW_WYVERN_ALWAYS;
	public static boolean ALLOW_WYVERN_DURING_SIEGE;
	
	// --------------------------------------------------
	// General Settings
	// --------------------------------------------------
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean DISPLAY_SERVER_VERSION;
	public static boolean SERVER_LIST_BRACKET;
	public static int SERVER_LIST_TYPE;
	public static int SERVER_LIST_AGE;
	public static boolean SERVER_GMONLY;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean GM_STARTUP_DIET_MODE;
	public static boolean GM_ITEM_RESTRICTION;
	public static boolean GM_SKILL_RESTRICTION;
	public static boolean GM_TRADE_RESTRICTED_ITEMS;
	public static boolean GM_RESTART_FIGHTING;
	public static boolean GM_ANNOUNCER_NAME;
	public static boolean GM_CRITANNOUNCER_NAME;
	public static boolean GM_GIVE_SPECIAL_SKILLS;
	public static boolean GM_GIVE_SPECIAL_AURA_SKILLS;
	public static boolean BYPASS_VALIDATION;
	public static boolean GAMEGUARD_ENFORCE;
	public static boolean LOG_CHAT;
	public static boolean LOG_AUTO_ANNOUNCEMENTS;
	public static boolean LOG_ITEMS;
	public static boolean LOG_ITEMS_SMALL_LOG;
	public static boolean LOG_ITEM_ENCHANTS;
	public static boolean LOG_SKILL_ENCHANTS;
	public static boolean GMAUDIT;
	public static boolean LOG_GAME_DAMAGE;
	public static int LOG_GAME_DAMAGE_THRESHOLD;
	public static boolean SKILL_CHECK_ENABLE;
	public static boolean SKILL_CHECK_REMOVE;
	public static boolean SKILL_CHECK_GM;
	public static boolean DEBUG;
	public static boolean PACKET_HANDLER_DEBUG;
	public static boolean DEVELOPER;
	// public static boolean ACCEPT_GEOEDITOR_CONN;
	public static boolean ALT_DEV_NO_HANDLERS;
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	public static int AI_MAX_THREAD;
	public static int CLIENT_PACKET_QUEUE_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE;
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL;
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND;
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN;
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN;
	public static boolean DEADLOCK_DETECTOR;
	public static int DEADLOCK_CHECK_INTERVAL;
	public static boolean RESTART_ON_DEADLOCK;
	public static boolean ALLOW_DISCARDITEM;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static List<Integer> LIST_PROTECTED_ITEMS;
	public static boolean DATABASE_CLEAN_UP;
	public static long CONNECTION_CLOSE_TIME;
	public static int CHAR_STORE_INTERVAL;
	public static boolean LAZY_ITEMS_UPDATE;
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean LAZY_CACHE;
	public static boolean CACHE_CHAR_NAMES;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static boolean ENABLE_FALLING_DAMAGE;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static boolean MOVE_BASED_KNOWNLIST;
	public static long KNOWNLIST_UPDATE_INTERVAL;
	public static int PEACE_ZONE_MODE;
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_REFUND;
	public static boolean ALLOW_MAIL;
	public static boolean ALLOW_ATTACHMENTS;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_BOAT;
	public static int BOAT_BROADCAST_RADIUS;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_MANOR;
	public static boolean ALLOW_PET_WALKERS;
	public static boolean SERVER_NEWS;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static int[] BAN_CHAT_CHANNELS;
	public static int ALT_OLY_START_TIME;
	public static String OLYMPIAD_PERIOD;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_START_POINTS;
	public static int ALT_OLY_WEEKLY_POINTS;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_TEAMS;
	public static int ALT_OLY_REG_DISPLAY;
	public static int[][] ALT_OLY_CLASSED_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_REWARD;
	public static int[][] ALT_OLY_TEAM_REWARD;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_MIN_MATCHES;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int ALT_OLY_MAX_POINTS;
	public static int ALT_OLY_DIVIDER_CLASSED;
	public static int ALT_OLY_DIVIDER_NON_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_TEAM;
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int ALT_OLY_WAIT_TIME;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static long ALT_LOTTERY_PRIZE;
	public static long ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static long ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static int ALT_ITEM_AUCTION_EXPIRED_AFTER;
	public static long ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static int DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	public static boolean JAIL_DISABLE_TRANSACTION;
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE;
	public static boolean CUSTOM_NPC_SKILLS_TABLE;
	public static boolean CUSTOM_TELEPORT_TABLE;
	public static boolean CUSTOM_DROPLIST_TABLE;
	public static boolean CUSTOM_NPCBUFFER_TABLES;
	public static boolean CUSTOM_SKILLS_LOAD;
	public static boolean CUSTOM_ITEMS_LOAD;
	public static boolean CUSTOM_MULTISELL_LOAD;
	public static boolean CUSTOM_BUYLIST_LOAD;
	public static int ALT_BIRTHDAY_GIFT;
	public static String ALT_BIRTHDAY_MAIL_SUBJECT;
	public static String ALT_BIRTHDAY_MAIL_TEXT;
	public static boolean ENABLE_BLOCK_CHECKER_EVENT;
	public static int MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static boolean HBCE_FAIR_PLAY;
	public static int PLAYER_MOVEMENT_BLOCK_TIME;
	public static boolean CLEAR_CREST_CACHE;
	public static boolean SKILL_CHANCE_SHOW;
	public static int NORMAL_ENCHANT_COST_MULTIPLIER;
	public static int SAFE_ENCHANT_COST_MULTIPLIER;
	
	// --------------------------------------------------
	// FloodProtector Settings
	// --------------------------------------------------
	public static FloodProtectorConfig FLOOD_PROTECTOR_USE_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ROLL_DICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_FIREWORK;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_PET_SUMMON;
	public static FloodProtectorConfig FLOOD_PROTECTOR_HERO_VOICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GLOBAL_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SUBCLASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_DROP_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SERVER_BYPASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MULTISELL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRANSACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANUFACTURE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANOR;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SENDMAIL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CHARACTER_SELECT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_AUCTION;
	
	// --------------------------------------------------
	// Mods Settings
	// --------------------------------------------------
	public static boolean CHAMPION_ENABLE;
	public static boolean CHAMPION_PASSIVE;
	public static int CHAMPION_FREQUENCY;
	public static String CHAMP_TITLE;
	public static int CHAMP_MIN_LVL;
	public static int CHAMP_MAX_LVL;
	public static int CHAMPION_HP;
	public static int CHAMPION_REWARDS;
	public static float CHAMPION_ADENAS_REWARDS;
	public static float CHAMPION_HP_REGEN;
	public static float CHAMPION_ATK;
	public static float CHAMPION_SPD_ATK;
	public static int CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE;
	public static int CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE;
	public static int CHAMPION_REWARD_ID;
	public static int CHAMPION_REWARD_QTY;
	public static boolean CHAMPION_ENABLE_VITALITY;
	public static boolean CHAMPION_ENABLE_IN_INSTANCES;
	public static int CHAMPION_ENABLE_AURA;
	public static boolean TVT_EVENT_ENABLED;
	public static boolean TVT_EVENT_IN_INSTANCE;
	public static String TVT_EVENT_INSTANCE_FILE;
	public static String[] TVT_EVENT_INTERVAL;
	public static int TVT_EVENT_PARTICIPATION_TIME;
	public static int TVT_EVENT_RUNNING_TIME;
	public static int TVT_EVENT_PARTICIPATION_NPC_ID;
	public static int[] TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] TVT_EVENT_PARTICIPATION_FEE = new int[2];
	public static int TVT_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_MAX_PLAYERS_IN_TEAMS;
	public static int TVT_EVENT_RESPAWN_TELEPORT_DELAY;
	public static int TVT_EVENT_START_LEAVE_TELEPORT_DELAY;
	public static String TVT_EVENT_TEAM_1_NAME;
	public static int[] TVT_EVENT_TEAM_1_COORDINATES = new int[3];
	public static String TVT_EVENT_TEAM_2_NAME;
	public static int[] TVT_EVENT_TEAM_2_COORDINATES = new int[3];
	public static List<int[]> TVT_EVENT_REWARDS;
	public static boolean TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean TVT_EVENT_SCROLL_ALLOWED;
	public static boolean TVT_EVENT_POTIONS_ALLOWED;
	public static boolean TVT_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> TVT_DOORS_IDS_TO_OPEN;
	public static List<Integer> TVT_DOORS_IDS_TO_CLOSE;
	public static boolean TVT_REWARD_TEAM_TIE;
	public static byte TVT_EVENT_MIN_LVL;
	public static byte TVT_EVENT_MAX_LVL;
	public static int TVT_EVENT_EFFECTS_REMOVAL;
	public static Map<Integer, Integer> TVT_EVENT_FIGHTER_BUFFS;
	public static Map<Integer, Integer> TVT_EVENT_MAGE_BUFFS;
	public static int TVT_EVENT_MAX_PARTICIPANTS_PER_IP;
	public static boolean TVT_ALLOW_VOICED_COMMAND;
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_DURATION;
	public static boolean WEDDING_SAMESEX;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;
	public static boolean HELLBOUND_STATUS;
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	public static boolean ENABLE_WAREHOUSESORTING_CLAN;
	public static boolean ENABLE_WAREHOUSESORTING_PRIVATE;
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
	public static boolean OFFLINE_MODE_NO_DAMAGE;
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_FAME;
	public static boolean ENABLE_MANA_POTIONS_SUPPORT;
	public static boolean DISPLAY_SERVER_TIME;
	public static boolean WELCOME_MESSAGE_ENABLED;
	public static String WELCOME_MESSAGE_TEXT;
	public static int WELCOME_MESSAGE_TIME;
	public static boolean ANTIFEED_ENABLE;
	public static boolean ANTIFEED_DUALBOX;
	public static boolean ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int ANTIFEED_INTERVAL;
	public static boolean ANNOUNCE_PK_PVP;
	public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
	public static String ANNOUNCE_PK_MSG;
	public static String ANNOUNCE_PVP_MSG;
	public static boolean CHAT_ADMIN;
	public static boolean L2WALKER_PROTECTION;
	public static boolean DEBUG_VOICE_COMMAND;
	public static int DUALBOX_CHECK_MAX_PLAYERS_PER_IP;
	public static int DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP;
	public static int DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP;
	public static Map<Integer, Integer> DUALBOX_CHECK_WHITELIST;
	public static boolean ALLOW_CHANGE_PASSWORD;
	public static boolean PROTECTION_IP_ENABLED;
	
	// --------------------------------------------------
	// NPC Settings
	// --------------------------------------------------
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_ATTACKABLE_NPCS;
	public static boolean ALT_GAME_VIEWNPC;
	public static int MAX_DRIFT_RANGE;
	public static boolean DEEPBLUE_DROP_RULES;
	public static boolean DEEPBLUE_DROP_RULES_RAID;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_CREST_WITHOUT_QUEST;
	public static boolean ENABLE_RANDOM_ENCHANT_EFFECT;
	public static int MIN_NPC_LVL_DMG_PENALTY;
	public static Map<Integer, Float> NPC_DMG_PENALTY;
	public static Map<Integer, Float> NPC_CRIT_DMG_PENALTY;
	public static Map<Integer, Float> NPC_SKILL_DMG_PENALTY;
	public static int MIN_NPC_LVL_MAGIC_PENALTY;
	public static Map<Integer, Float> NPC_SKILL_CHANCE_PENALTY;
	public static int DECAY_TIME_TASK;
	public static int NPC_DECAY_TIME;
	public static int RAID_BOSS_DECAY_TIME;
	public static int SPOILED_DECAY_TIME;
	public static int MAX_SWEEPER_TIME;
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static List<Integer> LIST_PET_RENT_NPC;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_PDEFENCE_MULTIPLIER;
	public static double RAID_MDEFENCE_MULTIPLIER;
	public static double RAID_PATTACK_MULTIPLIER;
	public static double RAID_MATTACK_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static Map<Integer, Integer> MINIONS_RESPAWN_TIME;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static boolean RAID_DISABLE_CURSE;
	public static int RAID_CHAOS_TIME;
	public static int GRAND_CHAOS_TIME;
	public static int MINION_CHAOS_TIME;
	public static int INVENTORY_MAXIMUM_PET;
	public static double PET_HP_REGEN_MULTIPLIER;
	public static double PET_MP_REGEN_MULTIPLIER;
	public static List<Integer> NON_TALKING_NPCS;
	public static boolean LIMIT_SUMMONS_PAILAKA;
	public static boolean LUCKPY_ENABLED;
	public static boolean DRAGON_VORTEX_UNLIMITED_SPAWN;
	public static boolean ALLOW_RAIDBOSS_CHANCE_DEBUFF;
	public static double RAIDBOSS_CHANCE_DEBUFF;
	public static boolean ALLOW_GRANDBOSS_CHANCE_DEBUFF;
	public static double GRANDBOSS_CHANCE_DEBUFF;
	public static int[] RAIDBOSS_DEBUFF_SPECIAL;
	public static int[] GRANDBOSS_DEBUFF_SPECIAL;
	public static double RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
	public static double GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
	
	// --------------------------------------------------
	// PvP Settings
	// --------------------------------------------------
	public static int KARMA_MIN_KARMA;
	public static int KARMA_MAX_KARMA;
	public static int KARMA_XP_DIVIDER;
	public static int KARMA_LOST_BASE;
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;
	public static int DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER;
	public static int PUNISH_PK_PLAYER_IF_PKS_OVER;
	public static long PK_MONITOR_PERIOD;
	public static String PK_PUNISHMENT_TYPE;
	public static long PK_PUNISHMENT_PERIOD;
	public static boolean ALLOW_PVP_REWARD;
	public static boolean ALLOW_PVP_REWARD_AUTO_LOOT;
	public static boolean ADD_EXP_SP_ON_PVP;
	public static int ADD_EXP_PVP;
	public static int ADD_SP_PVP;
	public static int PVP_REWARD_ITEM_DROP_CHANCE;
	public static int PVP_REWARD_ITEM_ID;
	public static int PVP_REWARD_ITEM_AMMOUNT;
	public static int PVP_REWARD_ITEM_RADIUS;
	public static String PVP_REWARD_MESSAGE_TEXT;
	public static boolean PVP_COLOR_SYSTEM = false;
	public static int PVP_AMMOUNT1 = 0;
	public static int PVP_AMMOUNT2 = 0;
	public static int PVP_AMMOUNT3 = 0;
	public static int PVP_AMMOUNT4 = 0;
	public static int PVP_AMMOUNT5 = 0;
	public static int COLOR_FOR_AMMOUNT1 = 0;
	public static int COLOR_FOR_AMMOUNT2 = 0;
	public static int COLOR_FOR_AMMOUNT3 = 0;
	public static int COLOR_FOR_AMMOUNT4 = 0;
	public static int COLOR_FOR_AMMOUNT5 = 0;
	public static int TITLE_COLOR_FOR_AMMOUNT1 = 0;
	public static int TITLE_COLOR_FOR_AMMOUNT2 = 0;
	public static int TITLE_COLOR_FOR_AMMOUNT3 = 0;
	public static int TITLE_COLOR_FOR_AMMOUNT4 = 0;
	public static int TITLE_COLOR_FOR_AMMOUNT5 = 0;
	
	// --------------------------------------------------
	// Rate Settings
	// --------------------------------------------------
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_CONSUMABLE_COST;
	public static float RATE_HB_TRUST_INCREASE;
	public static float RATE_HB_TRUST_DECREASE;
	public static float RATE_EXTRACTABLE;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_ITEMS_BY_RAID;
	public static float RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	public static float RATE_QUEST_DROP;
	public static float RATE_QUEST_REWARD;
	public static float RATE_QUEST_REWARD_XP;
	public static float RATE_QUEST_REWARD_SP;
	public static float RATE_QUEST_REWARD_ADENA;
	public static boolean RATE_QUEST_REWARD_USE_MULTIPLIERS;
	public static float RATE_QUEST_REWARD_POTION;
	public static float RATE_QUEST_REWARD_SCROLL;
	public static float RATE_QUEST_REWARD_RECIPE;
	public static float RATE_QUEST_REWARD_MATERIAL;
	public static Map<Integer, Float> RATE_DROP_ITEMS_ID;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_HP_HERBS;
	public static float RATE_DROP_MP_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	public static double[] PLAYER_XP_PERCENT_LOST;
	
	// --------------------------------------------------
	// Seven Signs Settings
	// --------------------------------------------------
	public static boolean ALT_GAME_CASTLE_DAWN;
	public static boolean ALT_GAME_CASTLE_DUSK;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	public static double ALT_SIEGE_DAWN_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DAWN_GATES_MDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_MDEF_MULT;
	public static boolean ALT_STRICT_SEVENSIGNS;
	public static boolean ALT_SEVENSIGNS_LAZY_UPDATE;
	public static int SSQ_DAWN_TICKET_QUANTITY;
	public static int SSQ_DAWN_TICKET_PRICE;
	public static int SSQ_DAWN_TICKET_BUNDLE;
	public static int SSQ_MANORS_AGREEMENT_ID;
	public static int SSQ_JOIN_DAWN_ADENA_FEE;
	
	// --------------------------------------------------
	// Server Settings
	// --------------------------------------------------
	public static int PORT_GAME;
	public static int PORT_LOGIN;
	public static String LOGIN_BIND_ADDRESS;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static String GAMESERVER_HOSTNAME;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIME;
	public static int MAXIMUM_ONLINE_USERS;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static String CLAN_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static File DATAPACK_ROOT;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	public static List<Integer> PROTOCOL_LIST;
	public static boolean LOG_LOGIN_CONTROLLER;
	public static boolean LOGIN_SERVER_SCHEDULE_RESTART;
	public static long LOGIN_SERVER_SCHEDULE_RESTART_TIME;
	
	// --------------------------------------------------
	// MMO Settings
	// --------------------------------------------------
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;
	public static boolean MMO_TCP_NODELAY;
	
	// --------------------------------------------------
	// Vitality Settings
	// --------------------------------------------------
	public static boolean ENABLE_VITALITY;
	public static boolean RECOVER_VITALITY_ON_RECONNECT;
	public static boolean ENABLE_DROP_VITALITY_HERBS;
	public static float RATE_VITALITY_LEVEL_1;
	public static float RATE_VITALITY_LEVEL_2;
	public static float RATE_VITALITY_LEVEL_3;
	public static float RATE_VITALITY_LEVEL_4;
	public static float RATE_DROP_VITALITY_HERBS;
	public static float RATE_RECOVERY_VITALITY_PEACE_ZONE;
	public static float RATE_VITALITY_LOST;
	public static float RATE_VITALITY_GAIN;
	public static float RATE_RECOVERY_ON_RECONNECT;
	public static int STARTING_VITALITY_POINTS;
	
	// --------------------------------------------------
	// No classification assigned to the following yet
	// --------------------------------------------------
	public static int MAX_ITEM_IN_PACKET;
	public static boolean CHECK_KNOWN;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static List<String> GAME_SERVER_SUBNETS;
	public static List<String> GAME_SERVER_HOSTS;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	
	public static enum IdFactoryType
	{
		Compaction,
		BitSet,
		Stack
	}
	
	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;
	
	public static double ENCHANT_CHANCE_ELEMENT_STONE;
	public static double ENCHANT_CHANCE_ELEMENT_CRYSTAL;
	public static double ENCHANT_CHANCE_ELEMENT_JEWEL;
	public static double ENCHANT_CHANCE_ELEMENT_ENERGY;
	public static int[] ENCHANT_BLACKLIST;
	public static boolean SYSTEM_BLESSED_ENCHANT;
	public static int BLESSED_ENCHANT_SAVE;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static int AUGMENTATION_ACC_SKILL_CHANCE;
	public static boolean RETAIL_LIKE_AUGMENTATION;
	public static int[] RETAIL_LIKE_AUGMENTATION_NG_CHANCE;
	public static int[] RETAIL_LIKE_AUGMENTATION_MID_CHANCE;
	public static int[] RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE;
	public static int[] RETAIL_LIKE_AUGMENTATION_TOP_CHANCE;
	public static boolean RETAIL_LIKE_AUGMENTATION_ACCESSORY;
	public static int[] AUGMENTATION_BLACKLIST;
	public static boolean ALT_ALLOW_AUGMENT_PVP_ITEMS;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static boolean IS_TELNET_ENABLED;
	public static boolean SHOW_LICENCE;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static boolean AUTO_LOOT_BY_ID_SYSTEM;
	public static int[] AUTO_LOOT_BY_ID;
	
	// GrandBoss Settings
	
	// Antharas
	public static int ANTHARAS_WAIT_TIME;
	public static int ANTHARAS_SPAWN_INTERVAL;
	public static int ANTHARAS_SPAWN_RANDOM;
	
	// Valakas
	public static int VALAKAS_WAIT_TIME;
	public static int VALAKAS_SPAWN_INTERVAL;
	public static int VALAKAS_SPAWN_RANDOM;
	
	// Baium
	public static int BAIUM_SPAWN_INTERVAL;
	public static int BAIUM_SPAWN_RANDOM;
	
	// Core
	public static int CORE_SPAWN_INTERVAL;
	public static int CORE_SPAWN_RANDOM;
	
	// Orfen
	public static int ORFEN_SPAWN_INTERVAL;
	public static int ORFEN_SPAWN_RANDOM;
	
	// Queen Ant
	public static int QUEEN_ANT_SPAWN_INTERVAL;
	public static int QUEEN_ANT_SPAWN_RANDOM;
	
	// Beleth
	public static int BELETH_MIN_PLAYERS;
	public static int BELETH_SPAWN_INTERVAL;
	public static int BELETH_SPAWN_RANDOM;
	public static int BELETH_SPAWN_DELAY;
	public static int BELETH_ZONE_CLEAN_DELAY;
	public static int BELETH_CLONES_RESPAWN;
	
	public static int MIN_ZAKEN_DAY_PLAYERS;
	public static int MIN_ZAKEN_NIGHT_PLAYERS;
	public static int MAX_ZAKEN_NIGHT_PLAYERS;
	
	public static int MIN_FREYA_PLAYERS;
	public static int MAX_FREYA_PLAYERS;
	public static int MIN_LEVEL_PLAYERS;
	public static int MIN_FREYA_HC_PLAYERS;
	public static int MAX_FREYA_HC_PLAYERS;
	public static int MIN_LEVEL_HC_PLAYERS;
	
	public static int HPH_FIXINTERVALOFHALTER;
	public static int HPH_RANDOMINTERVALOFHALTER;
	public static int HPH_APPTIMEOFHALTER;
	public static int HPH_ACTIVITYTIMEOFHALTER;
	public static int HPH_FIGHTTIMEOFHALTER;
	public static int HPH_CALLROYALGUARDHELPERCOUNT;
	public static int HPH_CALLROYALGUARDHELPERINTERVAL;
	public static int HPH_INTERVALOFDOOROFALTER;
	public static int HPH_TIMEOFLOCKUPDOOROFALTAR;
	
	public static int Interval_Of_Sailren_Spawn;
	public static int Random_Of_Sailren_Spawn;
	
	public static int MIN_FRINTEZZA_PLAYERS;
	public static int MAX_FRINTEZZA_PLAYERS;
	
	public static int CHANGE_STATUS;
	public static int CHANCE_SPAWN;
	public static int RESPAWN_TIME;
	
	// Gracia Seeds Settings
	public static int SOD_TIAT_KILL_COUNT;
	public static long SOD_STAGE_2_LENGTH;
	public static int MIN_TIAT_PLAYERS;
	public static int MAX_TIAT_PLAYERS;
	public static int SOI_EKIMUS_KILL_COUNT;
	public static int MIN_EKIMUS_PLAYERS;
	public static int MAX_EKIMUS_PLAYERS;
	
	// chatfilter
	public static ArrayList<String> FILTER_LIST;
	
	// Security Settings
	public static boolean SECOND_AUTH_ENABLED;
	public static int SECOND_AUTH_MAX_ATTEMPTS;
	public static long SECOND_AUTH_BAN_TIME;
	public static String SECOND_AUTH_REC_LINK;
	public static int BRUT_AVG_TIME;
	public static int BRUT_LOGON_ATTEMPTS;
	public static int BRUT_BAN_IP_TIME;
	public static boolean SECURITY_SKILL_CHECK;
	public static boolean SECURITY_SKILL_CHECK_CLEAR;
	public static int SECURITY_SKILL_CHECK_PUNISH;
	public static boolean ENABLE_SAFE_ADMIN_PROTECTION;
	public static List<String> SAFE_ADMIN_NAMES;
	public static int SAFE_ADMIN_PUNISH;
	public static boolean SAFE_ADMIN_SHOW_ADMIN_ENTER;
	public static boolean BOTREPORT_ENABLE;
	public static String[] BOTREPORT_RESETPOINT_HOUR;
	public static long BOTREPORT_REPORT_DELAY;
	public static boolean BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS;
	
	// Email
	public static String EMAIL_SERVERINFO_NAME;
	public static String EMAIL_SERVERINFO_ADDRESS;
	public static boolean EMAIL_SYS_ENABLED;
	public static String EMAIL_SYS_HOST;
	public static int EMAIL_SYS_PORT;
	public static boolean EMAIL_SYS_SMTP_AUTH;
	public static String EMAIL_SYS_FACTORY;
	public static boolean EMAIL_SYS_FACTORY_CALLBACK;
	public static String EMAIL_SYS_USERNAME;
	public static String EMAIL_SYS_PASSWORD;
	public static String EMAIL_SYS_ADDRESS;
	public static String EMAIL_SYS_SELECTQUERY;
	public static String EMAIL_SYS_DBFIELD;
	
	// Conquerable Halls Settings
	public static int CHS_CLAN_MINLEVEL;
	public static int CHS_MAX_ATTACKERS;
	public static int CHS_MAX_FLAGS_PER_CLAN;
	public static boolean CHS_ENABLE_FAME;
	public static int CHS_FAME_AMOUNT;
	public static int CHS_FAME_FREQUENCY;
	
	// Multi-Language Settings
	public static boolean MULTILANG_ENABLE;
	public static List<String> MULTILANG_ALLOWED = new ArrayList<>();
	public static String MULTILANG_DEFAULT;
	public static boolean MULTILANG_VOICED_ALLOW;
	public static boolean MULTILANG_SM_ENABLE;
	public static List<String> MULTILANG_SM_ALLOWED = new ArrayList<>();
	public static boolean MULTILANG_NS_ENABLE;
	public static List<String> MULTILANG_NS_ALLOWED = new ArrayList<>();
	
	// VoiceCommands Settings
	public static boolean ALLOW_EXP_GAIN_COMMAND;
	public static boolean ALLOW_AUTOLOOT_COMMAND;
	public static boolean VOICE_ONLINE_ENABLE;
	public static int FAKE_ONLINE;
	public static boolean ALLOW_TELETO_LEADER;
	public static int TELETO_LEADER_ID;
	public static int TELETO_LEADER_COUNT;
	public static boolean ALLOW_REPAIR_COMMAND;
	
	// Custom Settings
	public static boolean VITAMIN_MANAGER;
	public static String SERVER_NAME;
	public static boolean ONLINE_PLAYERS_AT_STARTUP;
	public static int ONLINE_PLAYERS_ANNOUNCE_INTERVAL;
	public static boolean ALLOW_NEW_CHARACTER_TITLE;
	public static String NEW_CHARACTER_TITLE;
	public static boolean NEW_CHAR_IS_NOBLE;
	public static boolean NEW_CHAR_IS_HERO;
	public static boolean CUSTOM_STARTER_ITEMS_ENABLED;
	public static List<int[]> CUSTOM_STARTER_ITEMS = new FastList<>();
	public static boolean UNSTUCK_SKILL;
	public static boolean ALLOW_NEW_CHAR_CUSTOM_POSITION;
	public static int NEW_CHAR_POSITION_X;
	public static int NEW_CHAR_POSITION_Y;
	public static int NEW_CHAR_POSITION_Z;
	public static boolean ENABLE_NOBLESS_COLOR;
	public static int NOBLESS_COLOR_NAME;
	public static boolean ENABLE_NOBLESS_TITLE_COLOR;
	public static int NOBLESS_COLOR_TITLE_NAME;
	public static boolean INFINITE_SOUL_SHOT;
	public static boolean INFINITE_SPIRIT_SHOT;
	public static boolean INFINITE_BLESSED_SPIRIT_SHOT;
	public static boolean INFINITE_ARROWS;
	public static boolean ENTER_HELLBOUND_WITHOUT_QUEST;
	public static boolean ALLOW_SURVEY;
	public static boolean ALLOW_SURVEY_GM_VOTING;
	public static int TIME_SURVEY;
	public static boolean AUTO_RESTART_ENABLE;
	public static int AUTO_RESTART_TIME;
	public static String[] AUTO_RESTART_INTERVAL;
	public static boolean SPEED_UP_RUN;
	public static int DISCONNECT_TIMEOUT;
	public static boolean DISCONNECT_SYSTEM_ENABLED;
	public static String DISCONNECT_TITLECOLOR;
	public static String DISCONNECT_TITLE;
	public static boolean CUSTOM_ENCHANT_ITEMS_ENABLED;
	public static Map<Integer, Float> ENCHANT_ITEMS_ID;
	
	// PC Points Settings
	public static boolean PC_BANG_ENABLED;
	public static int PC_POINT_ID;
	public static int PC_BANG_MIN_LEVEL;
	public static int PC_BANG_POINTS_MIN;
	public static int PC_BANG_POINTS_MAX;
	public static int MAX_PC_BANG_POINTS;
	public static boolean ENABLE_DOUBLE_PC_BANG_POINTS;
	public static int DOUBLE_PC_BANG_POINTS_CHANCE;
	public static int PC_BANG_INTERVAL;
	
	// Premium Accounts Settings
	public static boolean USE_PREMIUMSERVICE;
	public static float PREMIUM_RATE_XP;
	public static float PREMIUM_RATE_SP;
	public static float PREMIUM_RATE_DROP_ITEMS;
	public static float PREMIUM_RATE_DROP_SPOIL;
	public static float PREMIUM_RATE_DROP_QUEST;
	public static float PREMIUM_RATE_DROP_ITEMS_BY_RAID;
	public static Map<Integer, Float> PREMIUM_RATE_DROP_ITEMS_ID;
	public static int PREMIUM_ID;
	public static int PREMIUM_COUNT;
	public static boolean AUTO_GIVE_PREMIUM;
	public static int GIVE_PREMIUM_DAYS;
	public static boolean NOTICE_PREMIUM_MESSAGE;
	
	// Community Board Settings
	public static int COMMUNITY_TYPE;
	public static boolean ALLOW_COMMUNITY_CLAN_MANAGER;
	public static boolean ALLOW_COMMUNITY_FAVORITE_MANAGER;
	public static boolean ALLOW_COMMUNITY_FORUMS_MANAGER;
	public static boolean ALLOW_COMMUNITY_FRIENDS_MANAGER;
	public static boolean ALLOW_COMMUNITY_LINK_MANAGER;
	public static boolean ALLOW_COMMUNITY_MAIL_MANAGER;
	public static boolean ALLOW_COMMUNITY_POST_MANAGER;
	public static boolean ALLOW_COMMUNITY_REGION_MANAGER;
	public static boolean ALLOW_COMMUNITY_TOP_MANAGER;
	public static boolean ALLOW_COMMUNITY_TOPIC_MANAGER;
	public static boolean BBS_SHOW_PLAYERLIST;
	public static boolean BBS_COUNT_PLAYERLIST;
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static boolean ALLOW_COMMUNITY_PEACE_ZONE;
	public static boolean ALLOW_COMMUNITY_MULTISELL;
	public static boolean ALLOW_COMMUNITY_CLASS;
	public static boolean ALLOW_COMMUNITY_ENCHANT;
	public static boolean ALLOW_COMMUNITY_BUFF;
	public static boolean ALLOW_COMMUNITY_TELEPORT;
	public static boolean ALLOW_COMMUNITY_STATS;
	public static boolean ALLOW_COMMUNITY_EVENTS;
	public static boolean ALLOW_COMMUNITY_ACCOUNT;
	public static String ALLOW_CLASS_MASTERSCB;
	public static String CLASS_MASTERS_PRICECB;
	public static int[] CLASS_MASTERS_PRICE_LISTCB = new int[4];
	public static int CLASS_MASTERS_PRICE_ITEMCB;
	public static ArrayList<Integer> ALLOW_CLASS_MASTERS_LISTCB = new ArrayList<>();
	public static int ENCHANT_ITEM;
	public static int BUFF_ID_ITEM;
	public static int BUFF_AMOUNT;
	public static int CANCEL_BUFF_AMOUNT;
	public static int HP_BUFF_AMOUNT;
	public static int MP_BUFF_AMOUNT;
	public static int CP_BUFF_AMOUNT;
	public static int BUFF_MAX_SCHEMES;
	public static int BUFF_MAX_SKILLS;
	public static int BUFF_STATIC_BUFF_COST;
	public static boolean BUFF_STORE_SCHEMES;
	public static int BUFFER_PUNISH;
	public static boolean ALLOW_COMMUNITY_SERVICES;
	public static int DelevelItemId;
	public static int DelevelItemCount;
	public static int NoblItemId;
	public static int NoblItemCount;
	public static int GenderItemId;
	public static int GenderItemCount;
	public static int HeroItemId;
	public static int HeroItemCount;
	public static int RecoveryPKItemId;
	public static int RecoveryPKItemCount;
	public static int RecoveryVitalityItemId;
	public static int RecoveryVitalityItemCount;
	public static int SPItemId;
	public static int SPItemCount;
	public static int COMMUNITY_ENCHANT_MIN;
	public static int COMMUNITY_ENCHANT_MAX;
	public static double COMMUNITY_ENCH_D_PRICE_MOD;
	public static double COMMUNITY_ENCH_C_PRICE_MOD;
	public static double COMMUNITY_ENCH_B_PRICE_MOD;
	public static double COMMUNITY_ENCH_A_PRICE_MOD;
	public static double COMMUNITY_ENCH_S_PRICE_MOD;
	public static double COMMUNITY_ENCH_S80_PRICE_MOD;
	public static double COMMUNITY_ENCH_S84_PRICE_MOD;
	public static int COMMUNITY_ENCH_D_WEAPON_PRICE;
	public static int COMMUNITY_ENCH_D_ARMOR_PRICE;
	public static int COMMUNITY_ENCH_C_WEAPON_PRICE;
	public static int COMMUNITY_ENCH_C_ARMOR_PRICE;
	public static int COMMUNITY_ENCH_B_WEAPON_PRICE;
	public static int COMMUNITY_ENCH_B_ARMOR_PRICE;
	public static int COMMUNITY_ENCH_A_WEAPON_PRICE;
	public static int COMMUNITY_ENCH_A_ARMOR_PRICE;
	public static int COMMUNITY_ENCH_S_WEAPON_PRICE;
	public static int COMMUNITY_ENCH_S_ARMOR_PRICE;
	public static int COMMUNITY_ENCH_S80_WEAPON_PRICE;
	public static int COMMUNITY_ENCH_S80_ARMOR_PRICE;
	public static int COMMUNITY_ENCH_S84_WEAPON_PRICE;
	public static int COMMUNITY_ENCH_S84_ARMOR_PRICE;
	public static int COMMUNITY_1_PROF_REWARD;
	public static int COMMUNITY_1_PROF_REWARD_COUNT;
	public static int COMMUNITY_2_PROF_REWARD;
	public static int COMMUNITY_2_PROF_REWARD_COUNT;
	public static int COMMUNITY_3_PROF_REWARD;
	public static int COMMUNITY_3_PROF_REWARD_COUNT;
	public static int NICK_NAME_CHANGE_ITEM;
	public static int NICK_NAME_CHANGE_ITEM_COUNT;
	public static int CHANGE_TITLE_COLOR_ITEM;
	public static int CHANGE_TITLE_COLOR_ITEM_COUNT;
	public static int CHANGE_NICK_COLOR_ITEM;
	public static int CHANGE_NICK_COLOR_ITEM_COUNT;
	
	// Leaderboards Settings
	public static boolean RANK_ARENA_ACCEPT_SAME_IP;
	public static boolean RANK_ARENA_ENABLED;
	public static int RANK_ARENA_INTERVAL;
	public static int RANK_ARENA_REWARD_ID;
	public static int RANK_ARENA_REWARD_COUNT;
	public static boolean RANK_FISHERMAN_ENABLED;
	public static int RANK_FISHERMAN_INTERVAL;
	public static int RANK_FISHERMAN_REWARD_ID;
	public static int RANK_FISHERMAN_REWARD_COUNT;
	public static boolean RANK_CRAFT_ENABLED;
	public static int RANK_CRAFT_INTERVAL;
	public static int RANK_CRAFT_REWARD_ID;
	public static int RANK_CRAFT_REWARD_COUNT;
	public static boolean RANK_TVT_ENABLED;
	public static int RANK_TVT_INTERVAL;
	public static int RANK_TVT_REWARD_ID;
	public static int RANK_TVT_REWARD_COUNT;
	
	// BW Event Settings
	public static boolean BW_AUTO_MODE;
	public static int BW_TEAMS_NUM;
	public static int BW_PLAYER_LEVEL_MIN;
	public static int BW_PLAYER_LEVEL_MAX;
	public static boolean BW_ALLOW_INTERFERENCE;
	public static boolean BW_ALLOW_POTIONS;
	public static boolean BW_ALLOW_SUMMON;
	public static boolean BW_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean BW_ON_START_UNSUMMON_PET;
	public static boolean BW_ALLOW_ENEMY_HEALING;
	public static boolean BW_ALLOW_TEAM_CASTING;
	public static boolean BW_ALLOW_TEAM_ATTACKING;
	public static boolean BW_JOIN_CURSED;
	public static boolean BW_PRICE_NO_KILLS;
	public static boolean BW_AURA;
	public static String BW_EVEN_TEAMS;
	public static boolean BW_ANNOUNCE_REWARD;
	public static String[] BW_REWARD;
	public static String[] BW_REWARD_TOP;
	public static String[] BW_EVENT_INTERVAL;
	public static String[] BW_NPC_LOC;
	public static int BW_NPC_X;
	public static int BW_NPC_Y;
	public static int BW_NPC_Z;
	public static String BW_NPC_LOC_NAME;
	public static String[] BW_DEAD_LOC;
	public static int BW_DEAD_X;
	public static int BW_DEAD_Y;
	public static int BW_DEAD_Z;
	public static int BW_RES_TIME;
	public static int BW_MIN_PLAYERS;
	public static int BW_FIGHT_TIME;
	public static int BW_COUNTDOWN_TIME;
	public static ArrayList<Integer> BW_DOORS_TO_CLOSE = new ArrayList<>();
	public static ArrayList<Integer> BW_DOORS_TO_OPEN = new ArrayList<>();
	
	// CTF Event Settings
	public static boolean CTF_AUTO_MODE;
	public static int CTF_TEAMS_NUM;
	public static int CTF_PLAYER_LEVEL_MIN;
	public static int CTF_PLAYER_LEVEL_MAX;
	public static boolean CTF_ALLOW_INTERFERENCE;
	public static boolean CTF_ALLOW_POTIONS;
	public static boolean CTF_ALLOW_SUMMON;
	public static boolean CTF_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean CTF_ON_START_UNSUMMON_PET;
	public static boolean CTF_ALLOW_ENEMY_HEALING;
	public static boolean CTF_ALLOW_TEAM_CASTING;
	public static boolean CTF_ALLOW_TEAM_ATTACKING;
	public static boolean CTF_JOIN_CURSED;
	public static boolean CTF_PRICE_NO_KILLS;
	public static boolean CTF_AURA;
	public static String CTF_EVEN_TEAMS;
	public static boolean CTF_ANNOUNCE_REWARD;
	public static String[] CTF_REWARD;
	public static String[] CTF_REWARD_TOP;
	public static String[] CTF_EVENT_INTERVAL;
	public static String[] CTF_NPC_LOC;
	public static int CTF_NPC_X;
	public static int CTF_NPC_Y;
	public static int CTF_NPC_Z;
	public static String CTF_NPC_LOC_NAME;
	public static String[] CTF_DEAD_LOC;
	public static int CTF_DEAD_X;
	public static int CTF_DEAD_Y;
	public static int CTF_DEAD_Z;
	public static int CTF_RES_TIME;
	public static int CTF_MIN_PLAYERS;
	public static int CTF_FIGHT_TIME;
	public static int CTF_COUNTDOWN_TIME;
	public static ArrayList<Integer> CTF_DOORS_TO_CLOSE = new ArrayList<>();
	public static ArrayList<Integer> CTF_DOORS_TO_OPEN = new ArrayList<>();
	
	// DM Event Settings
	public static boolean DM_AUTO_MODE;
	public static int DM_PLAYER_LEVEL_MIN;
	public static int DM_PLAYER_LEVEL_MAX;
	public static boolean DM_ALLOW_INTERFERENCE;
	public static boolean DM_ALLOW_POTIONS;
	public static boolean DM_ALLOW_SUMMON;
	public static boolean DM_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean DM_ON_START_UNSUMMON_PET;
	public static boolean DM_ALLOW_ENEMY_HEALING;
	public static boolean DM_ALLOW_TEAM_CASTING;
	public static boolean DM_ALLOW_TEAM_ATTACKING;
	public static boolean DM_JOIN_CURSED;
	public static String DM_EVEN_TEAMS;
	public static boolean DM_ANNOUNCE_REWARD;
	public static String[] DM_REWARD;
	public static String[] DM_EVENT_INTERVAL;
	public static String[] DM_NPC_LOC;
	public static int DM_NPC_X;
	public static int DM_NPC_Y;
	public static int DM_NPC_Z;
	public static String DM_NPC_LOC_NAME;
	public static int DM_MIN_PLAYERS;
	public static int DM_FIGHT_TIME;
	public static int DM_COUNTDOWN_TIME;
	public static String[] DM_START_LOC;
	public static int DM_START_LOC_X;
	public static int DM_START_LOC_Y;
	public static int DM_START_LOC_Z;
	public static ArrayList<Integer> DM_DOORS_TO_CLOSE = new ArrayList<>();
	public static ArrayList<Integer> DM_DOORS_TO_OPEN = new ArrayList<>();
	
	// Fun Events Settings
	public static boolean EVENT_INSTANCE;
	public static boolean EVENT_SHOW_SCOREBOARD;
	public static boolean EVENT_SHOW_JOIN_DIALOG;
	public static int EVENT_CHECK_ACTIVITY_TIME;
	public static boolean ENABLE_EVENT_ENGINE;
	
	// TW Event Settings
	public static boolean TW_AUTO_MODE;
	public static String[] TW_EVENT_INTERVAL;
	public static int TW_TOWN_ID;
	public static boolean TW_ALL_TOWNS;
	public static String[] TW_REWARD;
	public static boolean TW_GIVE_PVP_AND_PK_POINTS;
	public static boolean TW_ALLOW_KARMA;
	public static boolean TW_DISABLE_GK;
	public static boolean TW_AUTO_RES;
	public static int TW_FIGHT_TIME;
	public static boolean TW_LOSE_BUFFS_ON_DEATH;
	
	// TvTRound Event Settings
	public static boolean TVT_ROUND_EVENT_ENABLED;
	public static boolean TVT_ROUND_EVENT_IN_INSTANCE;
	public static String TVT_ROUND_EVENT_INSTANCE_FILE;
	public static String[] TVT_ROUND_EVENT_INTERVAL;
	public static int TVT_ROUND_EVENT_PARTICIPATION_TIME;
	public static int TVT_ROUND_EVENT_FIRST_FIGHT_RUNNING_TIME;
	public static int TVT_ROUND_EVENT_SECOND_FIGHT_RUNNING_TIME;
	public static int TVT_ROUND_EVENT_THIRD_FIGHT_RUNNING_TIME;
	public static int TVT_ROUND_EVENT_PARTICIPATION_NPC_ID;
	public static int[] TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
	public static int[] TVT_ROUND_EVENT_PARTICIPATION_FEE = new int[2];
	public static int TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS;
	public static int TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS;
	public static boolean TVT_ROUND_EVENT_ON_DIE;
	public static int TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY;
	public static String TVT_ROUND_EVENT_TEAM_1_NAME;
	public static int[] TVT_ROUND_EVENT_TEAM_1_COORDINATES = new int[3];
	public static String TVT_ROUND_EVENT_TEAM_2_NAME;
	public static int[] TVT_ROUND_EVENT_TEAM_2_COORDINATES = new int[3];
	public static List<int[]> TVT_ROUND_EVENT_REWARDS;
	public static boolean TVT_ROUND_EVENT_TARGET_TEAM_MEMBERS_ALLOWED;
	public static boolean TVT_ROUND_EVENT_SCROLL_ALLOWED;
	public static boolean TVT_ROUND_EVENT_POTIONS_ALLOWED;
	public static boolean TVT_ROUND_EVENT_SUMMON_BY_ITEM_ALLOWED;
	public static List<Integer> TVT_ROUND_DOORS_IDS_TO_OPEN;
	public static List<Integer> TVT_ROUND_DOORS_IDS_TO_CLOSE;
	public static List<Integer> TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE;
	public static int TVT_ROUND_EVENT_WAIT_OPEN_ANTEROOM_DOORS;
	public static int TVT_ROUND_EVENT_WAIT_CLOSE_ANTEROOM_DOORS;
	public static boolean TVT_ROUND_EVENT_STOP_ON_TIE;
	public static int TVT_ROUND_EVENT_MINIMUM_TIE;
	public static boolean TVT_ROUND_GIVE_POINT_TEAM_TIE;
	public static boolean TVT_ROUND_REWARD_TEAM_TIE;
	public static boolean TVT_ROUND_EVENT_REWARD_ON_SECOND_FIGHT_END;
	public static byte TVT_ROUND_EVENT_MIN_LVL;
	public static byte TVT_ROUND_EVENT_MAX_LVL;
	public static int TVT_ROUND_EVENT_EFFECTS_REMOVAL;
	public static Map<Integer, Integer> TVT_ROUND_EVENT_FIGHTER_BUFFS;
	public static Map<Integer, Integer> TVT_ROUND_EVENT_MAGE_BUFFS;
	public static int TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP;
	public static boolean TVT_ROUND_ALLOW_VOICED_COMMAND;
	
	// Hitman Event Settings
	public static boolean HITMAN_ENABLE_EVENT;
	public static boolean HITMAN_TAKE_KARMA;
	public static boolean HITMAN_ANNOUNCE;
	public static int HITMAN_MAX_PER_PAGE;
	public static List<Integer> HITMAN_CURRENCY;
	public static boolean HITMAN_SAME_TEAM;
	public static int HITMAN_SAVE_TARGET;
	
	// Leprechaun Event Settings
	public static boolean ENABLED_LEPRECHAUN;
	public static int LEPRECHAUN_ID;
	public static int LEPRECHAUN_FIRST_SPAWN_DELAY;
	public static int LEPRECHAUN_RESPAWN_INTERVAL;
	public static int LEPRECHAUN_SPAWN_TIME;
	public static int LEPRECHAUN_ANNOUNCE_INTERVAL;
	public static boolean SHOW_NICK;
	public static boolean SHOW_REGION;
	
	// Delevel Manager Settings
	public static int DELEVEL_NPC_ID;
	public static int DELEVEL_ITEM_ID;
	public static int DELEVEL_LVL_PRICE;
	public static int DELEVEL_VITALITY_PRICE;
	public static boolean DELEVEL_NPC_ENABLE;
	
	// Rename Manager Settings
	public static int RENAME_NPC_ID;
	public static int RENAME_NPC_MIN_LEVEL;
	public static String RENAME_NPC_FEE;
	
	// Color Manager Settings
	public static int COLORNAME_NPC_ID;
	public static int COLORNAME_NPC_MIN_LEVEL;
	public static String[] COLORNAME_NPC_COLORS;
	public static String COLORNAME_NPC_SETTINGS_LINE;
	public static ColorNameNpcSettings COLORNAME_NPC_SETTINGS;
	
	// ServerInfo Manager Settings
	public static int SERVERINFO_NPC_ID;
	public static String[] SERVERINFO_NPC_ADM;
	public static String[] SERVERINFO_NPC_GM;
	public static String SERVERINFO_NPC_DESCRIPTION;
	public static String SERVERINFO_NPC_EMAIL;
	public static String SERVERINFO_NPC_PHONE;
	public static String[] SERVERINFO_NPC_CUSTOM;
	public static String[] SERVERINFO_NPC_DISABLE_PAGE;
	
	// Underground Coliseum Settings
	public static ArrayList<Integer> UC_WARDAYS = new ArrayList<>(7);
	public static int UC_START_HOUR;
	public static int UC_END_HOUR;
	public static int UC_ROUND_TIME;
	public static int UC_PARTY_LIMIT;
	
	// ItemMall Settings
	public static int GAME_POINT_ITEM_ID;
	
	// Custom Events Settings
	// Event Elpiel
	public static int EVENT_TIME_ELPIES;
	public static int EVENT_NUMBER_OF_SPAWNED_ELPIES;
	// Event Rabbits
	public static int EVENT_TIME_RABBITS;
	public static int EVENT_NUMBER_OF_SPAWNED_CHESTS;
	// Event Race
	public static int EVENT_REG_TIME_RACE;
	public static int EVENT_RUNNING_TIME_RACE;
	// Event Heavy Medals
	public static int MEDAL_COUNT1;
	public static int MEDAL_COUNT2;
	public static int MEDAL_CHANCE1;
	public static int MEDAL_CHANCE2;
	// Squash Event
	public static int NECTAR_COUNT;
	public static int NECTAR_CHANCE;
	// Event Master Of Enchanting
	public static int SCROLL_COUNT;
	public static int SCROLL_CHANCE;
	// The Valentine Event
	public static int DARK_CHOCOLATE_COUNT;
	public static int WHITE_CHOCOLATE_COUNT;
	public static int FRESH_CREAM_COUNT;
	public static int DARK_CHOCOLATE_CHANCE;
	public static int WHITE_CHOCOLATE_CHANCE;
	public static int FRESH_CREAM_CHANCE;
	// Event L2Day
	public static int LETTER_COUNT;
	public static int LETTER_CHANCE;
	// Event Christmas
	public static int STAR_COUNT;
	public static int BEAD_COUNT;
	public static int FIR_COUNT;
	public static int FLOWER_COUNT;
	public static int STAR_CHANCE;
	public static int BEAD_CHANCE;
	public static int FIR_CHANCE;
	public static int FLOWER_CHANCE;
	// Event Coffer Of Shadows
	public static int COFFER_PRICE_ID;
	public static int COFFER_PRICE_AMOUNT;
	
	// Vote System Settings
	public static boolean ALLOW_HOPZONE_VOTE_REWARD;
	public static String HOPZONE_SERVER_LINK;
	public static String HOPZONE_FIRST_PAGE_LINK;
	public static int HOPZONE_VOTES_DIFFERENCE;
	public static int HOPZONE_FIRST_PAGE_RANK_NEEDED;
	public static int HOPZONE_REWARD_CHECK_TIME;
	public static Map<Integer, Integer> HOPZONE_SMALL_REWARD = new FastMap<>();
	public static Map<Integer, Integer> HOPZONE_BIG_REWARD = new FastMap<>();
	public static int HOPZONE_DUALBOXES_ALLOWED;
	public static boolean ALLOW_HOPZONE_GAME_SERVER_REPORT;
	public static boolean ALLOW_TOPZONE_VOTE_REWARD;
	public static String TOPZONE_SERVER_LINK;
	public static String TOPZONE_FIRST_PAGE_LINK;
	public static int TOPZONE_VOTES_DIFFERENCE;
	public static int TOPZONE_FIRST_PAGE_RANK_NEEDED;
	public static int TOPZONE_REWARD_CHECK_TIME;
	public static Map<Integer, Integer> TOPZONE_SMALL_REWARD = new FastMap<>();
	public static Map<Integer, Integer> TOPZONE_BIG_REWARD = new FastMap<>();
	public static int TOPZONE_DUALBOXES_ALLOWED;
	public static boolean ALLOW_TOPZONE_GAME_SERVER_REPORT;
	public static boolean ALLOW_VOTE_REWARD_SYSTEM;
	public static int VOTE_REWARD_ADENA_AMOUNT;
	public static int VOTE_REWARD_SECOND_ID;
	public static int VOTE_REWARD_SECOND_COUNT;
	
	// Vote Reward Npc Manager
	public static String VOTE_LINK_HOPZONE;
	public static String VOTE_LINK_TOPZONE;
	public static int VOTE_REWARD_ID;
	public static int VOTE_REWARD_AMOUNT;
	public static int SECS_TO_VOTE;
	
	// Last Hero Settings
	public static boolean LH_ENABLED;
	public static int LH_REGNPC_ID;
	public static int[] LH_REGNPC_COORDINATE = new int[3];
	public static String[] LH_EVENT_INTERVAL;
	public static int LH_MIN_LEVEL;
	public static int LH_TIME_FOR_REGISTRATION_IN_MINUTES;
	public static int LH_ANNOUNCE_REGISTRATION_INTERVAL_IN_SECONDS;
	public static int LH_MAX_PARTICIPANTS_PER_IP;
	public static int LH_TIME_TO_WAIT_BATTLE_IN_SECONDS;
	public static int LH_BATTLE_DURATION_IN_MINUTES;
	public static int LH_MIN_PARTICIPATE_COUNT;
	public static int LH_MAX_PATRICIPATE_COUNT;
	public static int LH_TELEPORT_COORDINATE[][];
	public static int LH_DOORS[];
	
	// AIO Item Settings
	public static boolean ALLOW_AIO_ITEM_COMMAND;
	public static boolean ENABLE_AIO_NPCS;
	public static int AIO_ITEM_ID;
	public static int AIO_TPCOIN;
	public static int AIO_PRICE_PERTP;
	public static boolean AIO_ENABLE_TP_DELAY;
	public static double AIO_DELAY;
	public static boolean AIO_DELAY_SENDMESSAGE;
	public static int CHANGE_GENDER_DONATE_COIN;
	public static int CHANGE_GENDER_DONATE_PRICE;
	public static int CHANGE_GENDER_NORMAL_COIN;
	public static int CHANGE_GENDER_NORMAL_PRICE;
	public static int CHANGE_NAME_COIN;
	public static int CHANGE_NAME_PRICE;
	public static int CHANGE_CNAME_COIN;
	public static int CHANGE_CNAME_PRICE;
	public static int AUGMENT_COIN;
	public static int AUGMENT_PRICE;
	public static int GET_FULL_CLAN_COIN;
	public static int GET_FULL_CLAN_PRICE;
	public static int AIO_EXCHANGE_ID;
	public static int AIO_EXCHANGE_PRICE;
	
	// Monster Rush Event Settings
	public static boolean MR_ENABLED;
	public static String[] MR_EVENT_INTERVAL;
	public static int MR_PARTICIPATION_TIME;
	public static int MR_RUNNING_TIME;
	public static int MRUSH_REWARD_AMOUNT;
	public static int MRUSH_REWARD_ITEM;
	public static int MRUSH_MIN_PLAYERS;
	
	// Olympiad AntiFeed Settings
	public static boolean ENABLE_OLY_FEED;
	public static int OLY_ANTI_FEED_WEAPON_RIGHT;
	public static int OLY_ANTI_FEED_WEAPON_LEFT;
	public static int OLY_ANTI_FEED_GLOVES;
	public static int OLY_ANTI_FEED_CHEST;
	public static int OLY_ANTI_FEED_LEGS;
	public static int OLY_ANTI_FEED_FEET;
	public static int OLY_ANTI_FEED_CLOAK;
	public static int OLY_ANTI_FEED_RIGH_HAND_ARMOR;
	public static int OLY_ANTI_FEED_HAIR_MISC_1;
	public static int OLY_ANTI_FEED_HAIR_MISC_2;
	public static int OLY_ANTI_FEED_RACE;
	public static int OLY_ANTI_FEED_GENDER;
	public static int OLY_ANTI_FEED_CLASS_RADIUS;
	public static int OLY_ANTI_FEED_CLASS_HEIGHT;
	public static int OLY_ANTI_FEED_PLAYER_HAVE_RECS;
	
	// Buffer
	public static boolean NpcBuffer_SmartWindow;
	public static int NpcBuffer_ID;
	public static boolean NpcBuffer_VIP;
	public static boolean VIP_ONLY;
	public static boolean NpcBuffer_EnableBuff;
	public static boolean NpcBuffer_EnableScheme;
	public static boolean NpcBuffer_EnableHeal;
	public static boolean NpcBuffer_EnableBuffs;
	public static boolean NpcBuffer_EnableResist;
	public static boolean NpcBuffer_EnableSong;
	public static boolean NpcBuffer_EnableDance;
	public static boolean NpcBuffer_EnableChant;
	public static boolean NpcBuffer_EnableOther;
	public static boolean NpcBuffer_EnableSpecial;
	public static boolean NpcBuffer_EnableCubic;
	public static boolean NpcBuffer_EnableCancel;
	public static boolean NpcBuffer_EnableBuffSet;
	public static boolean NpcBuffer_EnableBuffPK;
	public static boolean NpcBuffer_EnableFreeBuffs;
	public static boolean NpcBuffer_EnableTimeOut;
	public static int NpcBuffer_TimeOutTime;
	public static int NpcBuffer_MinLevel;
	public static int NpcBuffer_PriceCancel;
	public static int NpcBuffer_PriceHeal;
	public static int NpcBuffer_PriceBuffs;
	public static int NpcBuffer_PriceResist;
	public static int NpcBuffer_PriceSong;
	public static int NpcBuffer_PriceDance;
	public static int NpcBuffer_PriceChant;
	public static int NpcBuffer_PriceOther;
	public static int NpcBuffer_PriceSpecial;
	public static int NpcBuffer_PriceCubic;
	public static int NpcBuffer_PriceSet;
	public static int NpcBuffer_PriceScheme;
	public static int NpcBuffer_MaxScheme;
	public static int NpcBuffer_consumableID;
	
	// Geodata Settings
	public static enum CorrectSpawnsZ
	{
		TOWN,
		MONSTER,
		ALL,
		NONE
	}
	
	public static boolean GEODATA;
	public static CorrectSpawnsZ GEO_CORRECT_Z;
	public static boolean ACCEPT_GEOEDITOR_CONN;
	public static int CLIENT_SHIFTZ;
	public static boolean PATH_CLEAN;
	public static int MAX_Z_DIFF;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static int MIN_LAYER_HEIGHT;
	public static int PATHFIND_MAX_Z_DIFF;
	public static boolean PATHFIND_DIAGONAL;
	public static int TRICK_HEIGHT;
	
	// AntiBot Settings
	public static int ANTIBOT_START_CAPTCHA;
	public static int ANTIBOT_GET_KILLS;
	public static int ANTIBOT_GET_TIME_TO_FILL;
	public static int ANTIBOT_PUNISH;
	
	// Custom Rates Settings
	public static boolean ALLOW_CUSTOM_RATES;
	public static float MONDAY_RATE_EXP;
	public static float MONDAY_RATE_SP;
	public static float TUESDAY_RATE_EXP;
	public static float TUESDAY_RATE_SP;
	public static float WEDNESDAY_RATE_EXP;
	public static float WEDNESDAY_RATE_SP;
	public static float THURSDAY_RATE_EXP;
	public static float THURSDAY_RATE_SP;
	public static float FRIDAY_RATE_EXP;
	public static float FRIDAY_RATE_SP;
	public static float SATURDAY_RATE_EXP;
	public static float SATURDAY_RATE_SP;
	public static float SUNDAY_RATE_EXP;
	public static float SUNDAY_RATE_SP;
	
	// Phantom Players Settings
	public static boolean ALLOW_PHANTOM_PLAYERS;
	public static boolean ALLOW_PHANTOM_SETS;
	public static String PHANTOM_PLAYERS_AKK;
	public static int PHANTOM_PLAYERS_COUNT_FIRST;
	public static long PHANTOM_PLAYERS_DELAY_FIRST;
	public static long PHANTOM_PLAYERS_DESPAWN_FIRST;
	public static int PHANTOM_PLAYERS_DELAY_SPAWN_FIRST;
	public static int PHANTOM_PLAYERS_DELAY_DESPAWN_FIRST;
	public static int PHANTOM_PLAYERS_COUNT_NEXT;
	public static long PHANTOM_PLAYERS_DELAY_NEXT;
	public static long PHANTOM_PLAYERS_DESPAWN_NEXT;
	public static int PHANTOM_PLAYERS_DELAY_SPAWN_NEXT;
	public static int PHANTOM_PLAYERS_DELAY_DESPAWN_NEXT;
	public static int PHANTOM_PLAYERS_ENCHANT_MAX;
	public static long PHANTOM_PLAYERS_CP_REUSE_TIME;
	public static final FastList<Integer> PHANTOM_PLAYERS_NAME_CLOLORS = new FastList<>();
	public static final FastList<Integer> PHANTOM_PLAYERS_TITLE_CLOLORS = new FastList<>();
	public static int[] PHANTOM_BASE_PROFF_CLASSID;
	public static int[] PHANTOM_FIRST_PROFF_CLASSID;
	public static int[] PHANTOM_SECOND_PROFF_CLASSID;
	public static int[] PHANTOM_THIRD_PROFF_CLASSID;
	public static boolean ALLOW_PHANTOM_USE_HEAL_POTION;
	public static boolean ALLOW_PHANTOM_USE_CP_POTION;
	public static int PHANTOM_HEAL_REUSE_TIME;
	public static int MAX_PHANTOM_COUNT;
	
	public static class PvpColor
	{
		public int _nick;
		public int _title;
		
		PvpColor(int nick, int title)
		{
			_nick = nick;
			_title = title;
		}
	}
	
	public static void load()
	{
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			FLOOD_PROTECTOR_USE_ITEM = new FloodProtectorConfig("UseItemFloodProtector");
			FLOOD_PROTECTOR_ROLL_DICE = new FloodProtectorConfig("RollDiceFloodProtector");
			FLOOD_PROTECTOR_FIREWORK = new FloodProtectorConfig("FireworkFloodProtector");
			FLOOD_PROTECTOR_ITEM_PET_SUMMON = new FloodProtectorConfig("ItemPetSummonFloodProtector");
			FLOOD_PROTECTOR_HERO_VOICE = new FloodProtectorConfig("HeroVoiceFloodProtector");
			FLOOD_PROTECTOR_GLOBAL_CHAT = new FloodProtectorConfig("GlobalChatFloodProtector");
			FLOOD_PROTECTOR_SUBCLASS = new FloodProtectorConfig("SubclassFloodProtector");
			FLOOD_PROTECTOR_DROP_ITEM = new FloodProtectorConfig("DropItemFloodProtector");
			FLOOD_PROTECTOR_SERVER_BYPASS = new FloodProtectorConfig("ServerBypassFloodProtector");
			FLOOD_PROTECTOR_MULTISELL = new FloodProtectorConfig("MultiSellFloodProtector");
			FLOOD_PROTECTOR_TRANSACTION = new FloodProtectorConfig("TransactionFloodProtector");
			FLOOD_PROTECTOR_MANUFACTURE = new FloodProtectorConfig("ManufactureFloodProtector");
			FLOOD_PROTECTOR_MANOR = new FloodProtectorConfig("ManorFloodProtector");
			FLOOD_PROTECTOR_SENDMAIL = new FloodProtectorConfig("SendMailFloodProtector");
			FLOOD_PROTECTOR_CHARACTER_SELECT = new FloodProtectorConfig("CharacterSelectFloodProtector");
			FLOOD_PROTECTOR_ITEM_AUCTION = new FloodProtectorConfig("ItemAuctionFloodProtector");
			
			_log.info("Loading GameServer Configuration Files...");
			InputStream is = null;
			try
			{
				try
				{
					L2Properties serverSettings = new L2Properties();
					is = new FileInputStream(new File(CONFIGURATION_FILE));
					serverSettings.load(is);
					
					GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname");
					PORT_GAME = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
					
					GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9014"));
					GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
					
					REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID", "0"));
					ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID", "True"));
					
					DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
					DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jgs");
					DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
					DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
					DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
					DATABASE_MAX_IDLE_TIME = Integer.parseInt(serverSettings.getProperty("MaximumDbIdleTime", "0"));
					
					try
					{
						DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
					}
					catch (IOException e)
					{
						_log.log(Level.WARNING, "Error setting datapack root!", e);
						DATAPACK_ROOT = new File(".");
					}
					
					CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", ".*");
					PET_NAME_TEMPLATE = serverSettings.getProperty("PetNameTemplate", ".*");
					CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", ".*");
					
					MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "7"));
					MAXIMUM_ONLINE_USERS = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
					
					String[] protocols = serverSettings.getProperty("AllowedProtocolRevisions", "267;268;271;273").split(";");
					PROTOCOL_LIST = new ArrayList<>(protocols.length);
					for (String protocol : protocols)
					{
						try
						{
							PROTOCOL_LIST.add(Integer.parseInt(protocol.trim()));
						}
						catch (NumberFormatException e)
						{
							_log.info("Wrong config protocol version: " + protocol + ". Skipped.");
						}
					}
					
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + CONFIGURATION_FILE + " File.");
				}
				try
				{
					L2Properties securitySettings = new L2Properties();
					is = new FileInputStream(new File(SECURITY_CONFIG_FILE));
					securitySettings.load(is);
					
					// Second Auth Settings
					SECOND_AUTH_ENABLED = Boolean.parseBoolean(securitySettings.getProperty("SecondAuthEnabled", "False"));
					SECOND_AUTH_MAX_ATTEMPTS = Integer.parseInt(securitySettings.getProperty("SecondAuthMaxAttempts", "5"));
					SECOND_AUTH_BAN_TIME = Integer.parseInt(securitySettings.getProperty("SecondAuthBanTime", "480"));
					SECOND_AUTH_REC_LINK = securitySettings.getProperty("SecondAuthRecoveryLink", "5");
					L2WALKER_PROTECTION = Boolean.parseBoolean(securitySettings.getProperty("L2WalkerProtection", "False"));
					BRUT_AVG_TIME = Integer.parseInt(securitySettings.getProperty("BrutAvgTime", "30"));
					BRUT_LOGON_ATTEMPTS = Integer.parseInt(securitySettings.getProperty("BrutLogonAttempts", "15"));
					BRUT_BAN_IP_TIME = Integer.parseInt(securitySettings.getProperty("BrutBanIpTime", "900"));
					SECURITY_SKILL_CHECK = Boolean.parseBoolean(securitySettings.getProperty("SkillsCheck", "False"));
					SECURITY_SKILL_CHECK_CLEAR = Boolean.parseBoolean(securitySettings.getProperty("SkillsCheckClear", "False"));
					SECURITY_SKILL_CHECK_PUNISH = Integer.parseInt(securitySettings.getProperty("SkillsCheckPunish", "4"));
					
					ENABLE_SAFE_ADMIN_PROTECTION = Boolean.parseBoolean(securitySettings.getProperty("EnableSafeAdminProtection", "False"));
					String[] props = securitySettings.getProperty("SafeAdminName", "").split(",");
					SAFE_ADMIN_NAMES = new ArrayList<>(props.length);
					if (props.length != 0)
					{
						for (String name : props)
						{
							SAFE_ADMIN_NAMES.add(name);
						}
					}
					SAFE_ADMIN_PUNISH = Integer.parseInt(securitySettings.getProperty("SafeAdminPunish", "3"));
					SAFE_ADMIN_SHOW_ADMIN_ENTER = Boolean.parseBoolean(securitySettings.getProperty("SafeAdminShowAdminEnter", "False"));
					BOTREPORT_ENABLE = Boolean.parseBoolean(securitySettings.getProperty("EnableBotReport", "False"));
					BOTREPORT_RESETPOINT_HOUR = securitySettings.getProperty("BotReportPointsResetHour", "00:00").split(":");
					BOTREPORT_REPORT_DELAY = Integer.parseInt(securitySettings.getProperty("BotReportDelay", "30")) * 60000;
					BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS = Boolean.parseBoolean(securitySettings.getProperty("AllowReportsFromSameClanMembers", "False"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + SECURITY_CONFIG_FILE + " File.");
				}
				
				IPConfigData ipcd = new IPConfigData();
				GAME_SERVER_SUBNETS = ipcd.getSubnets();
				GAME_SERVER_HOSTS = ipcd.getHosts();
				
				// Load Feature L2Properties file (if exists)
				try
				{
					L2Properties Feature = new L2Properties();
					is = new FileInputStream(new File(FEATURE_CONFIG_FILE));
					Feature.load(is);
					
					CH_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallTeleportFunctionFeeRatio", "604800000"));
					CH_TELE1_FEE = Integer.parseInt(Feature.getProperty("ClanHallTeleportFunctionFeeLvl1", "7000"));
					CH_TELE2_FEE = Integer.parseInt(Feature.getProperty("ClanHallTeleportFunctionFeeLvl2", "14000"));
					CH_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallSupportFunctionFeeRatio", "86400000"));
					CH_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl1", "2500"));
					CH_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl2", "5000"));
					CH_SUPPORT3_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl3", "7000"));
					CH_SUPPORT4_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl4", "11000"));
					CH_SUPPORT5_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl5", "21000"));
					CH_SUPPORT6_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl6", "36000"));
					CH_SUPPORT7_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl7", "37000"));
					CH_SUPPORT8_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl8", "52000"));
					CH_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallMpRegenerationFunctionFeeRatio", "86400000"));
					CH_MPREG1_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl1", "2000"));
					CH_MPREG2_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl2", "3750"));
					CH_MPREG3_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl3", "6500"));
					CH_MPREG4_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl4", "13750"));
					CH_MPREG5_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl5", "20000"));
					CH_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallHpRegenerationFunctionFeeRatio", "86400000"));
					CH_HPREG1_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl1", "700"));
					CH_HPREG2_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl2", "800"));
					CH_HPREG3_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl3", "1000"));
					CH_HPREG4_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl4", "1166"));
					CH_HPREG5_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl5", "1500"));
					CH_HPREG6_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl6", "1750"));
					CH_HPREG7_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl7", "2000"));
					CH_HPREG8_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl8", "2250"));
					CH_HPREG9_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl9", "2500"));
					CH_HPREG10_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl10", "3250"));
					CH_HPREG11_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl11", "3270"));
					CH_HPREG12_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl12", "4250"));
					CH_HPREG13_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl13", "5166"));
					CH_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallExpRegenerationFunctionFeeRatio", "86400000"));
					CH_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl1", "3000"));
					CH_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl2", "6000"));
					CH_EXPREG3_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl3", "9000"));
					CH_EXPREG4_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl4", "15000"));
					CH_EXPREG5_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl5", "21000"));
					CH_EXPREG6_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl6", "23330"));
					CH_EXPREG7_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl7", "30000"));
					CH_ITEM_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallItemCreationFunctionFeeRatio", "86400000"));
					CH_ITEM1_FEE = Integer.parseInt(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl1", "30000"));
					CH_ITEM2_FEE = Integer.parseInt(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl2", "70000"));
					CH_ITEM3_FEE = Integer.parseInt(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl3", "140000"));
					CH_CURTAIN_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallCurtainFunctionFeeRatio", "604800000"));
					CH_CURTAIN1_FEE = Integer.parseInt(Feature.getProperty("ClanHallCurtainFunctionFeeLvl1", "2000"));
					CH_CURTAIN2_FEE = Integer.parseInt(Feature.getProperty("ClanHallCurtainFunctionFeeLvl2", "2500"));
					CH_FRONT_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallFrontPlatformFunctionFeeRatio", "259200000"));
					CH_FRONT1_FEE = Integer.parseInt(Feature.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "1300"));
					CH_FRONT2_FEE = Integer.parseInt(Feature.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "4000"));
					CH_BUFF_FREE = Boolean.parseBoolean(Feature.getProperty("AltClanHallMpBuffFree", "False"));
					
					SIEGE_HOUR_LIST = new ArrayList<>();
					for (String hour : Feature.getProperty("SiegeHourList", "").split(","))
					{
						if (Util.isDigit(hour))
						{
							SIEGE_HOUR_LIST.add(Integer.parseInt(hour));
						}
					}
					
					CS_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleTeleportFunctionFeeRatio", "604800000"));
					CS_TELE1_FEE = Integer.parseInt(Feature.getProperty("CastleTeleportFunctionFeeLvl1", "7000"));
					CS_TELE2_FEE = Integer.parseInt(Feature.getProperty("CastleTeleportFunctionFeeLvl2", "14000"));
					CS_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleSupportFunctionFeeRatio", "86400000"));
					CS_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl1", "7000"));
					CS_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl2", "21000"));
					CS_SUPPORT3_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl3", "37000"));
					CS_SUPPORT4_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl4", "52000"));
					CS_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleMpRegenerationFunctionFeeRatio", "86400000"));
					CS_MPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl1", "2000"));
					CS_MPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl2", "6500"));
					CS_MPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl3", "13750"));
					CS_MPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl4", "20000"));
					CS_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleHpRegenerationFunctionFeeRatio", "86400000"));
					CS_HPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl1", "1000"));
					CS_HPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl2", "1500"));
					CS_HPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl3", "2250"));
					CS_HPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl4", "3270"));
					CS_HPREG5_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl5", "5166"));
					CS_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleExpRegenerationFunctionFeeRatio", "86400000"));
					CS_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl1", "9000"));
					CS_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl2", "15000"));
					CS_EXPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl3", "21000"));
					CS_EXPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl4", "30000"));
					
					FS_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressTeleportFunctionFeeRatio", "604800000"));
					FS_TELE1_FEE = Integer.parseInt(Feature.getProperty("FortressTeleportFunctionFeeLvl1", "1000"));
					FS_TELE2_FEE = Integer.parseInt(Feature.getProperty("FortressTeleportFunctionFeeLvl2", "10000"));
					FS_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressSupportFunctionFeeRatio", "86400000"));
					FS_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("FortressSupportFeeLvl1", "7000"));
					FS_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("FortressSupportFeeLvl2", "17000"));
					FS_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressMpRegenerationFunctionFeeRatio", "86400000"));
					FS_MPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressMpRegenerationFeeLvl1", "6500"));
					FS_MPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressMpRegenerationFeeLvl2", "9300"));
					FS_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressHpRegenerationFunctionFeeRatio", "86400000"));
					FS_HPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressHpRegenerationFeeLvl1", "2000"));
					FS_HPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressHpRegenerationFeeLvl2", "3500"));
					FS_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressExpRegenerationFunctionFeeRatio", "86400000"));
					FS_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressExpRegenerationFeeLvl1", "9000"));
					FS_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressExpRegenerationFeeLvl2", "10000"));
					FS_UPDATE_FRQ = Integer.parseInt(Feature.getProperty("FortressPeriodicUpdateFrequency", "360"));
					FS_BLOOD_OATH_COUNT = Integer.parseInt(Feature.getProperty("FortressBloodOathCount", "1"));
					FS_MAX_SUPPLY_LEVEL = Integer.parseInt(Feature.getProperty("FortressMaxSupplyLevel", "6"));
					FS_FEE_FOR_CASTLE = Integer.parseInt(Feature.getProperty("FortressFeeForCastle", "25000"));
					FS_MAX_OWN_TIME = Integer.parseInt(Feature.getProperty("FortressMaximumOwnTime", "168"));
					
					ALT_GAME_CASTLE_DAWN = Boolean.parseBoolean(Feature.getProperty("AltCastleForDawn", "True"));
					ALT_GAME_CASTLE_DUSK = Boolean.parseBoolean(Feature.getProperty("AltCastleForDusk", "True"));
					ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(Feature.getProperty("AltRequireClanCastle", "False"));
					ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(Feature.getProperty("AltFestivalMinPlayer", "5"));
					ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(Feature.getProperty("AltMaxPlayerContrib", "1000000"));
					ALT_FESTIVAL_MANAGER_START = Long.parseLong(Feature.getProperty("AltFestivalManagerStart", "120000"));
					ALT_FESTIVAL_LENGTH = Long.parseLong(Feature.getProperty("AltFestivalLength", "1080000"));
					ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(Feature.getProperty("AltFestivalCycleLength", "2280000"));
					ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalFirstSpawn", "120000"));
					ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(Feature.getProperty("AltFestivalFirstSwarm", "300000"));
					ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalSecondSpawn", "540000"));
					ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(Feature.getProperty("AltFestivalSecondSwarm", "720000"));
					ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalChestSpawn", "900000"));
					ALT_SIEGE_DAWN_GATES_PDEF_MULT = Double.parseDouble(Feature.getProperty("AltDawnGatesPdefMult", "1.1"));
					ALT_SIEGE_DUSK_GATES_PDEF_MULT = Double.parseDouble(Feature.getProperty("AltDuskGatesPdefMult", "0.8"));
					ALT_SIEGE_DAWN_GATES_MDEF_MULT = Double.parseDouble(Feature.getProperty("AltDawnGatesMdefMult", "1.1"));
					ALT_SIEGE_DUSK_GATES_MDEF_MULT = Double.parseDouble(Feature.getProperty("AltDuskGatesMdefMult", "0.8"));
					ALT_STRICT_SEVENSIGNS = Boolean.parseBoolean(Feature.getProperty("StrictSevenSigns", "True"));
					ALT_SEVENSIGNS_LAZY_UPDATE = Boolean.parseBoolean(Feature.getProperty("AltSevenSignsLazyUpdate", "True"));
					
					SSQ_DAWN_TICKET_QUANTITY = Integer.parseInt(Feature.getProperty("SevenSignsDawnTicketQuantity", "300"));
					SSQ_DAWN_TICKET_PRICE = Integer.parseInt(Feature.getProperty("SevenSignsDawnTicketPrice", "1000"));
					SSQ_DAWN_TICKET_BUNDLE = Integer.parseInt(Feature.getProperty("SevenSignsDawnTicketBundle", "10"));
					SSQ_MANORS_AGREEMENT_ID = Integer.parseInt(Feature.getProperty("SevenSignsManorsAgreementId", "6388"));
					SSQ_JOIN_DAWN_ADENA_FEE = Integer.parseInt(Feature.getProperty("SevenSignsJoinDawnFee", "50000"));
					
					TAKE_FORT_POINTS = Integer.parseInt(Feature.getProperty("TakeFortPoints", "200"));
					LOOSE_FORT_POINTS = Integer.parseInt(Feature.getProperty("LooseFortPoints", "0"));
					TAKE_CASTLE_POINTS = Integer.parseInt(Feature.getProperty("TakeCastlePoints", "1500"));
					LOOSE_CASTLE_POINTS = Integer.parseInt(Feature.getProperty("LooseCastlePoints", "3000"));
					CASTLE_DEFENDED_POINTS = Integer.parseInt(Feature.getProperty("CastleDefendedPoints", "750"));
					FESTIVAL_WIN_POINTS = Integer.parseInt(Feature.getProperty("FestivalOfDarknessWin", "200"));
					HERO_POINTS = Integer.parseInt(Feature.getProperty("HeroPoints", "1000"));
					ROYAL_GUARD_COST = Integer.parseInt(Feature.getProperty("CreateRoyalGuardCost", "5000"));
					KNIGHT_UNIT_COST = Integer.parseInt(Feature.getProperty("CreateKnightUnitCost", "10000"));
					KNIGHT_REINFORCE_COST = Integer.parseInt(Feature.getProperty("ReinforceKnightUnitCost", "5000"));
					BALLISTA_POINTS = Integer.parseInt(Feature.getProperty("KillBallistaPoints", "30"));
					BLOODALLIANCE_POINTS = Integer.parseInt(Feature.getProperty("BloodAlliancePoints", "500"));
					BLOODOATH_POINTS = Integer.parseInt(Feature.getProperty("BloodOathPoints", "200"));
					KNIGHTSEPAULETTE_POINTS = Integer.parseInt(Feature.getProperty("KnightsEpaulettePoints", "20"));
					REPUTATION_SCORE_PER_KILL = Integer.parseInt(Feature.getProperty("ReputationScorePerKill", "1"));
					JOIN_ACADEMY_MIN_REP_SCORE = Integer.parseInt(Feature.getProperty("CompleteAcademyMinPoints", "190"));
					JOIN_ACADEMY_MAX_REP_SCORE = Integer.parseInt(Feature.getProperty("CompleteAcademyMaxPoints", "650"));
					RAID_RANKING_1ST = Integer.parseInt(Feature.getProperty("1stRaidRankingPoints", "1250"));
					RAID_RANKING_2ND = Integer.parseInt(Feature.getProperty("2ndRaidRankingPoints", "900"));
					RAID_RANKING_3RD = Integer.parseInt(Feature.getProperty("3rdRaidRankingPoints", "700"));
					RAID_RANKING_4TH = Integer.parseInt(Feature.getProperty("4thRaidRankingPoints", "600"));
					RAID_RANKING_5TH = Integer.parseInt(Feature.getProperty("5thRaidRankingPoints", "450"));
					RAID_RANKING_6TH = Integer.parseInt(Feature.getProperty("6thRaidRankingPoints", "350"));
					RAID_RANKING_7TH = Integer.parseInt(Feature.getProperty("7thRaidRankingPoints", "300"));
					RAID_RANKING_8TH = Integer.parseInt(Feature.getProperty("8thRaidRankingPoints", "200"));
					RAID_RANKING_9TH = Integer.parseInt(Feature.getProperty("9thRaidRankingPoints", "150"));
					RAID_RANKING_10TH = Integer.parseInt(Feature.getProperty("10thRaidRankingPoints", "100"));
					RAID_RANKING_UP_TO_50TH = Integer.parseInt(Feature.getProperty("UpTo50thRaidRankingPoints", "25"));
					RAID_RANKING_UP_TO_100TH = Integer.parseInt(Feature.getProperty("UpTo100thRaidRankingPoints", "12"));
					CLAN_LEVEL_6_COST = Integer.parseInt(Feature.getProperty("ClanLevel6Cost", "5000"));
					CLAN_LEVEL_7_COST = Integer.parseInt(Feature.getProperty("ClanLevel7Cost", "10000"));
					CLAN_LEVEL_8_COST = Integer.parseInt(Feature.getProperty("ClanLevel8Cost", "20000"));
					CLAN_LEVEL_9_COST = Integer.parseInt(Feature.getProperty("ClanLevel9Cost", "40000"));
					CLAN_LEVEL_10_COST = Integer.parseInt(Feature.getProperty("ClanLevel10Cost", "40000"));
					CLAN_LEVEL_11_COST = Integer.parseInt(Feature.getProperty("ClanLevel11Cost", "75000"));
					CLAN_LEVEL_6_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel6Requirement", "30"));
					CLAN_LEVEL_7_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel7Requirement", "50"));
					CLAN_LEVEL_8_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel8Requirement", "80"));
					CLAN_LEVEL_9_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel9Requirement", "120"));
					CLAN_LEVEL_10_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel10Requirement", "140"));
					CLAN_LEVEL_11_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel11Requirement", "170"));
					ALLOW_WYVERN_ALWAYS = Boolean.parseBoolean(Feature.getProperty("AllowRideWyvernAlways", "False"));
					ALLOW_WYVERN_DURING_SIEGE = Boolean.parseBoolean(Feature.getProperty("AllowRideWyvernDuringSiege", "True"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + FEATURE_CONFIG_FILE + " File.");
				}
				
				// Load Character L2Properties file (if exists)
				try
				{
					L2Properties Character = new L2Properties();
					is = new FileInputStream(new File(CHARACTER_CONFIG_FILE));
					Character.load(is);
					
					ALT_GAME_DELEVEL = Boolean.parseBoolean(Character.getProperty("Delevel", "true"));
					DECREASE_SKILL_LEVEL = Boolean.parseBoolean(Character.getProperty("DecreaseSkillOnDelevel", "true"));
					ALT_WEIGHT_LIMIT = Double.parseDouble(Character.getProperty("AltWeightLimit", "1"));
					RUN_SPD_BOOST = Integer.parseInt(Character.getProperty("RunSpeedBoost", "0"));
					DEATH_PENALTY_CHANCE = Integer.parseInt(Character.getProperty("DeathPenaltyChance", "20"));
					RESPAWN_RESTORE_CP = Double.parseDouble(Character.getProperty("RespawnRestoreCP", "0")) / 100;
					RESPAWN_RESTORE_HP = Double.parseDouble(Character.getProperty("RespawnRestoreHP", "65")) / 100;
					RESPAWN_RESTORE_MP = Double.parseDouble(Character.getProperty("RespawnRestoreMP", "0")) / 100;
					HP_REGEN_MULTIPLIER = Double.parseDouble(Character.getProperty("HpRegenMultiplier", "100")) / 100;
					MP_REGEN_MULTIPLIER = Double.parseDouble(Character.getProperty("MpRegenMultiplier", "100")) / 100;
					CP_REGEN_MULTIPLIER = Double.parseDouble(Character.getProperty("CpRegenMultiplier", "100")) / 100;
					ALT_GAME_TIREDNESS = Boolean.parseBoolean(Character.getProperty("AltGameTiredness", "false"));
					ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(Character.getProperty("EnableModifySkillDuration", "false"));
					
					// Create Map only if enabled
					if (ENABLE_MODIFY_SKILL_DURATION)
					{
						String[] propertySplit = Character.getProperty("SkillDurationList", "").split(";");
						SKILL_DURATION_LIST = new HashMap<>(propertySplit.length);
						for (String skill : propertySplit)
						{
							String[] skillSplit = skill.split(",");
							if (skillSplit.length != 2)
							{
								_log.warning(StringUtil.concat("[SkillDurationList]: invalid config property -> SkillDurationList \"", skill, "\""));
							}
							else
							{
								try
								{
									SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
								}
								catch (NumberFormatException nfe)
								{
									if (!skill.isEmpty())
									{
										_log.warning(StringUtil.concat("[SkillDurationList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
									}
								}
							}
						}
					}
					ENABLE_MODIFY_SKILL_REUSE = Boolean.parseBoolean(Character.getProperty("EnableModifySkillReuse", "false"));
					// Create Map only if enabled
					if (ENABLE_MODIFY_SKILL_REUSE)
					{
						String[] propertySplit = Character.getProperty("SkillReuseList", "").split(";");
						SKILL_REUSE_LIST = new HashMap<>(propertySplit.length);
						for (String skill : propertySplit)
						{
							String[] skillSplit = skill.split(",");
							if (skillSplit.length != 2)
							{
								_log.warning(StringUtil.concat("[SkillReuseList]: invalid config property -> SkillReuseList \"", skill, "\""));
							}
							else
							{
								try
								{
									SKILL_REUSE_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
								}
								catch (NumberFormatException nfe)
								{
									if (!skill.isEmpty())
									{
										_log.warning(StringUtil.concat("[SkillReuseList]: invalid config property -> SkillList \"", skillSplit[0], "\"", skillSplit[1]));
									}
								}
							}
						}
					}
					
					AUTO_LEARN_SKILLS = Boolean.parseBoolean(Character.getProperty("AutoLearnSkills", "False"));
					AUTO_LEARN_FS_SKILLS = Boolean.parseBoolean(Character.getProperty("AutoLearnForgottenScrollSkills", "False"));
					AUTO_LOOT_HERBS = Boolean.parseBoolean(Character.getProperty("AutoLootHerbs", "false"));
					BUFFS_MAX_AMOUNT = Byte.parseByte(Character.getProperty("MaxBuffAmount", "20"));
					TRIGGERED_BUFFS_MAX_AMOUNT = Byte.parseByte(Character.getProperty("MaxTriggeredBuffAmount", "12"));
					DANCES_MAX_AMOUNT = Byte.parseByte(Character.getProperty("MaxDanceAmount", "12"));
					DANCE_CANCEL_BUFF = Boolean.parseBoolean(Character.getProperty("DanceCancelBuff", "false"));
					DANCE_CONSUME_ADDITIONAL_MP = Boolean.parseBoolean(Character.getProperty("DanceConsumeAdditionalMP", "true"));
					ALT_STORE_DANCES = Boolean.parseBoolean(Character.getProperty("AltStoreDances", "false"));
					AUTO_LEARN_DIVINE_INSPIRATION = Boolean.parseBoolean(Character.getProperty("AutoLearnDivineInspiration", "false"));
					ALT_GAME_CANCEL_BOW = Character.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || Character.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
					ALT_GAME_CANCEL_CAST = Character.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || Character.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
					EFFECT_CANCELING = Boolean.parseBoolean(Character.getProperty("CancelLesserEffect", "True"));
					ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(Character.getProperty("MagicFailures", "true"));
					PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(Character.getProperty("PlayerFakeDeathUpProtection", "0"));
					STORE_SKILL_COOLTIME = Boolean.parseBoolean(Character.getProperty("StoreSkillCooltime", "true"));
					SUBCLASS_STORE_SKILL_COOLTIME = Boolean.parseBoolean(Character.getProperty("SubclassStoreSkillCooltime", "false"));
					SUBCLASS_STORE_SKILL = Boolean.parseBoolean(Character.getProperty("SubclassSaveSkill", "False"));
					SUMMON_STORE_SKILL_COOLTIME = Boolean.parseBoolean(Character.getProperty("SummonStoreSkillCooltime", "True"));
					ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(Character.getProperty("AltShieldBlocks", "false"));
					ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(Character.getProperty("AltPerfectShieldBlockRate", "10"));
					ALLOW_CLASS_MASTERS = Boolean.parseBoolean(Character.getProperty("AllowClassMasters", "False"));
					ALLOW_ENTIRE_TREE = Boolean.parseBoolean(Character.getProperty("AllowEntireTree", "False"));
					ALTERNATE_CLASS_MASTER = Boolean.parseBoolean(Character.getProperty("AlternateClassMaster", "False"));
					if (ALLOW_CLASS_MASTERS || ALTERNATE_CLASS_MASTER)
					{
						CLASS_MASTER_SETTINGS = new ClassMasterSettings(Character.getProperty("ConfigClassMaster"));
					}
					LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(Character.getProperty("LifeCrystalNeeded", "true"));
					ES_SP_BOOK_NEEDED = Boolean.parseBoolean(Character.getProperty("EnchantSkillSpBookNeeded", "true"));
					DIVINE_SP_BOOK_NEEDED = Boolean.parseBoolean(Character.getProperty("DivineInspirationSpBookNeeded", "true"));
					ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(Character.getProperty("AltGameSkillLearn", "false"));
					ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(Character.getProperty("AltSubClassWithoutQuests", "False"));
					ALT_GAME_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(Character.getProperty("AltSubclassEverywhere", "False"));
					ALT_GAME_SUBCLASS_ALL_CLASSES = Boolean.parseBoolean(Character.getProperty("AltSubClassAllClasses", "False"));
					RESTORE_SERVITOR_ON_RECONNECT = Boolean.parseBoolean(Character.getProperty("RestoreServitorOnReconnect", "True"));
					RESTORE_PET_ON_RECONNECT = Boolean.parseBoolean(Character.getProperty("RestorePetOnReconnect", "True"));
					ALLOW_TRANSFORM_WITHOUT_QUEST = Boolean.parseBoolean(Character.getProperty("AltTransformationWithoutQuest", "False"));
					FEE_DELETE_TRANSFER_SKILLS = Integer.parseInt(Character.getProperty("FeeDeleteTransferSkills", "10000000"));
					FEE_DELETE_SUBCLASS_SKILLS = Integer.parseInt(Character.getProperty("FeeDeleteSubClassSkills", "10000000"));
					ENABLE_VITALITY = Boolean.parseBoolean(Character.getProperty("EnableVitality", "True"));
					RECOVER_VITALITY_ON_RECONNECT = Boolean.parseBoolean(Character.getProperty("RecoverVitalityOnReconnect", "True"));
					STARTING_VITALITY_POINTS = Integer.parseInt(Character.getProperty("StartingVitalityPoints", "20000"));
					MAX_BONUS_EXP = Double.parseDouble(Character.getProperty("MaxExpBonus", "3.5"));
					MAX_BONUS_SP = Double.parseDouble(Character.getProperty("MaxSpBonus", "3.5"));
					MAX_RUN_SPEED = Integer.parseInt(Character.getProperty("MaxRunSpeed", "300"));
					MAX_PCRIT_RATE = Integer.parseInt(Character.getProperty("MaxPCritRate", "500"));
					MAX_MCRIT_RATE = Integer.parseInt(Character.getProperty("MaxMCritRate", "200"));
					MAX_PATK_SPEED = Integer.parseInt(Character.getProperty("MaxPAtkSpeed", "1500"));
					MAX_MATK_SPEED = Integer.parseInt(Character.getProperty("MaxMAtkSpeed", "1999"));
					MAX_EVASION = Integer.parseInt(Character.getProperty("MaxEvasion", "250"));
					MIN_ABNORMAL_STATE_SUCCESS_RATE = Integer.parseInt(Character.getProperty("MinAbnormalStateSuccessRate", "10"));
					MAX_ABNORMAL_STATE_SUCCESS_RATE = Integer.parseInt(Character.getProperty("MaxAbnormalStateSuccessRate", "90"));
					MAX_SUBCLASS = Byte.parseByte(Character.getProperty("MaxSubclass", "3"));
					BASE_SUBCLASS_LEVEL = Byte.parseByte(Character.getProperty("BaseSubclassLevel", "40"));
					MAX_SUBCLASS_LEVEL = Byte.parseByte(Character.getProperty("MaxSubclassLevel", "80"));
					MAX_PVTSTORESELL_SLOTS_DWARF = Integer.parseInt(Character.getProperty("MaxPvtStoreSellSlotsDwarf", "4"));
					MAX_PVTSTORESELL_SLOTS_OTHER = Integer.parseInt(Character.getProperty("MaxPvtStoreSellSlotsOther", "3"));
					MAX_PVTSTOREBUY_SLOTS_DWARF = Integer.parseInt(Character.getProperty("MaxPvtStoreBuySlotsDwarf", "5"));
					MAX_PVTSTOREBUY_SLOTS_OTHER = Integer.parseInt(Character.getProperty("MaxPvtStoreBuySlotsOther", "4"));
					INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(Character.getProperty("MaximumSlotsForNoDwarf", "80"));
					INVENTORY_MAXIMUM_DWARF = Integer.parseInt(Character.getProperty("MaximumSlotsForDwarf", "100"));
					INVENTORY_MAXIMUM_GM = Integer.parseInt(Character.getProperty("MaximumSlotsForGMPlayer", "250"));
					INVENTORY_MAXIMUM_QUEST_ITEMS = Integer.parseInt(Character.getProperty("MaximumSlotsForQuestItems", "100"));
					MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
					WAREHOUSE_SLOTS_DWARF = Integer.parseInt(Character.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
					WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(Character.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
					WAREHOUSE_SLOTS_CLAN = Integer.parseInt(Character.getProperty("MaximumWarehouseSlotsForClan", "150"));
					ALT_FREIGHT_SLOTS = Integer.parseInt(Character.getProperty("MaximumFreightSlots", "200"));
					ALT_FREIGHT_PRICE = Integer.parseInt(Character.getProperty("FreightPrice", "1000"));
					
					AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationNGSkillChance", "15"));
					AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(Character.getProperty("AugmentationNGGlowChance", "0"));
					AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationMidSkillChance", "30"));
					AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(Character.getProperty("AugmentationMidGlowChance", "40"));
					AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationHighSkillChance", "45"));
					AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(Character.getProperty("AugmentationHighGlowChance", "70"));
					AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationTopSkillChance", "60"));
					AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(Character.getProperty("AugmentationTopGlowChance", "100"));
					AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(Character.getProperty("AugmentationBaseStatChance", "1"));
					AUGMENTATION_ACC_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationAccSkillChance", "0"));
					
					RETAIL_LIKE_AUGMENTATION = Boolean.parseBoolean(Character.getProperty("RetailLikeAugmentation", "True"));
					String[] array = Character.getProperty("RetailLikeAugmentationNoGradeChance", "55,35,7,3").split(",");
					RETAIL_LIKE_AUGMENTATION_NG_CHANCE = new int[array.length];
					for (int i = 0; i < 4; i++)
					{
						RETAIL_LIKE_AUGMENTATION_NG_CHANCE[i] = Integer.parseInt(array[i]);
					}
					array = Character.getProperty("RetailLikeAugmentationMidGradeChance", "55,35,7,3").split(",");
					RETAIL_LIKE_AUGMENTATION_MID_CHANCE = new int[array.length];
					for (int i = 0; i < 4; i++)
					{
						RETAIL_LIKE_AUGMENTATION_MID_CHANCE[i] = Integer.parseInt(array[i]);
					}
					array = Character.getProperty("RetailLikeAugmentationHighGradeChance", "55,35,7,3").split(",");
					RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE = new int[array.length];
					for (int i = 0; i < 4; i++)
					{
						RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE[i] = Integer.parseInt(array[i]);
					}
					array = Character.getProperty("RetailLikeAugmentationTopGradeChance", "55,35,7,3").split(",");
					RETAIL_LIKE_AUGMENTATION_TOP_CHANCE = new int[array.length];
					for (int i = 0; i < 4; i++)
					{
						RETAIL_LIKE_AUGMENTATION_TOP_CHANCE[i] = Integer.parseInt(array[i]);
					}
					RETAIL_LIKE_AUGMENTATION_ACCESSORY = Boolean.parseBoolean(Character.getProperty("RetailLikeAugmentationAccessory", "True"));
					
					array = Character.getProperty("AugmentationBlackList", "6656,6657,6658,6659,6660,6661,6662,8191,10170,10314,13740,13741,13742,13743,13744,13745,13746,13747,13748,14592,14593,14594,14595,14596,14597,14598,14599,14600,14664,14665,14666,14667,14668,14669,14670,14671,14672,14801,14802,14803,14804,14805,14806,14807,14808,14809,15282,15283,15284,15285,15286,15287,15288,15289,15290,15291,15292,15293,15294,15295,15296,15297,15298,15299,16025,16026,21712,22173,22174,22175").split(",");
					AUGMENTATION_BLACKLIST = new int[array.length];
					
					for (int i = 0; i < array.length; i++)
					{
						AUGMENTATION_BLACKLIST[i] = Integer.parseInt(array[i]);
					}
					
					Arrays.sort(AUGMENTATION_BLACKLIST);
					ALT_ALLOW_AUGMENT_PVP_ITEMS = Boolean.parseBoolean(Character.getProperty("AltAllowAugmentPvPItems", "false"));
					ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
					ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanShop", "true"));
					ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanTeleport", "true"));
					ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanUseGK", "false"));
					ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanTrade", "true"));
					ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
					MAX_PERSONAL_FAME_POINTS = Integer.parseInt(Character.getProperty("MaxPersonalFamePoints", "100000"));
					FORTRESS_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(Character.getProperty("FortressZoneFameTaskFrequency", "300"));
					FORTRESS_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(Character.getProperty("FortressZoneFameAquirePoints", "31"));
					CASTLE_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(Character.getProperty("CastleZoneFameTaskFrequency", "300"));
					CASTLE_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(Character.getProperty("CastleZoneFameAquirePoints", "125"));
					FAME_FOR_DEAD_PLAYERS = Boolean.parseBoolean(Character.getProperty("FameForDeadPlayers", "true"));
					IS_CRAFTING_ENABLED = Boolean.parseBoolean(Character.getProperty("CraftingEnabled", "true"));
					CRAFT_MASTERWORK = Boolean.parseBoolean(Character.getProperty("CraftMasterwork", "True"));
					DWARF_RECIPE_LIMIT = Integer.parseInt(Character.getProperty("DwarfRecipeLimit", "50"));
					COMMON_RECIPE_LIMIT = Integer.parseInt(Character.getProperty("CommonRecipeLimit", "50"));
					ALT_GAME_CREATION = Boolean.parseBoolean(Character.getProperty("AltGameCreation", "false"));
					ALT_GAME_CREATION_SPEED = Double.parseDouble(Character.getProperty("AltGameCreationSpeed", "1"));
					ALT_GAME_CREATION_XP_RATE = Double.parseDouble(Character.getProperty("AltGameCreationXpRate", "1"));
					ALT_GAME_CREATION_SP_RATE = Double.parseDouble(Character.getProperty("AltGameCreationSpRate", "1"));
					ALT_GAME_CREATION_RARE_XPSP_RATE = Double.parseDouble(Character.getProperty("AltGameCreationRareXpSpRate", "2"));
					ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(Character.getProperty("AltBlacksmithUseRecipes", "true"));
					ALT_CLAN_LEADER_DATE_CHANGE = Integer.parseInt(Character.getProperty("AltClanLeaderDateChange", "3"));
					if ((ALT_CLAN_LEADER_DATE_CHANGE < 1) || (ALT_CLAN_LEADER_DATE_CHANGE > 7))
					{
						_log.log(Level.WARNING, "Wrong value specified for AltClanLeaderDateChange: " + ALT_CLAN_LEADER_DATE_CHANGE);
						ALT_CLAN_LEADER_DATE_CHANGE = 3;
					}
					ALT_CLAN_LEADER_HOUR_CHANGE = Character.getProperty("AltClanLeaderHourChange", "00:00:00");
					ALT_CLAN_LEADER_INSTANT_ACTIVATION = Boolean.parseBoolean(Character.getProperty("AltClanLeaderInstantActivation", "false"));
					ALT_CLAN_JOIN_DAYS = Integer.parseInt(Character.getProperty("DaysBeforeJoinAClan", "1"));
					ALT_CLAN_CREATE_DAYS = Integer.parseInt(Character.getProperty("DaysBeforeCreateAClan", "10"));
					ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(Character.getProperty("DaysToPassToDissolveAClan", "7"));
					ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(Character.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
					ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(Character.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
					ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(Character.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
					ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(Character.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "1"));
					ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(Character.getProperty("AltMaxNumOfClansInAlly", "3"));
					ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(Character.getProperty("AltClanMembersForWar", "15"));
					ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(Character.getProperty("AltMembersCanWithdrawFromClanWH", "false"));
					REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(Character.getProperty("RemoveCastleCirclets", "true"));
					ALT_PARTY_RANGE = Integer.parseInt(Character.getProperty("AltPartyRange", "1600"));
					ALT_PARTY_RANGE2 = Integer.parseInt(Character.getProperty("AltPartyRange2", "1400"));
					ALT_LEAVE_PARTY_LEADER = Boolean.parseBoolean(Character.getProperty("AltLeavePartyLeader", "False"));
					INITIAL_EQUIPMENT_EVENT = Boolean.parseBoolean(Character.getProperty("InitialEquipmentEvent", "False"));
					STARTING_ADENA = Long.parseLong(Character.getProperty("StartingAdena", "0"));
					STARTING_LEVEL = Byte.parseByte(Character.getProperty("StartingLevel", "1"));
					STARTING_SP = Integer.parseInt(Character.getProperty("StartingSP", "0"));
					MAX_ADENA = Long.parseLong(Character.getProperty("MaxAdena", "99900000000"));
					if (MAX_ADENA < 0)
					{
						MAX_ADENA = Long.MAX_VALUE;
					}
					AUTO_LOOT = Boolean.parseBoolean(Character.getProperty("AutoLoot", "false"));
					AUTO_LOOT_RAIDS = Boolean.parseBoolean(Character.getProperty("AutoLootRaids", "false"));
					LOOT_RAIDS_PRIVILEGE_INTERVAL = Integer.parseInt(Character.getProperty("RaidLootRightsInterval", "900")) * 1000;
					LOOT_RAIDS_PRIVILEGE_CC_SIZE = Integer.parseInt(Character.getProperty("RaidLootRightsCCSize", "45"));
					UNSTUCK_INTERVAL = Integer.parseInt(Character.getProperty("UnstuckInterval", "300"));
					TELEPORT_WATCHDOG_TIMEOUT = Integer.parseInt(Character.getProperty("TeleportWatchdogTimeout", "0"));
					PLAYER_SPAWN_PROTECTION = Integer.parseInt(Character.getProperty("PlayerSpawnProtection", "0"));
					String[] items = Character.getProperty("PlayerSpawnProtectionAllowedItems", "0").split(",");
					SPAWN_PROTECTION_ALLOWED_ITEMS = new ArrayList<>(items.length);
					for (String item : items)
					{
						Integer itm = 0;
						try
						{
							itm = Integer.parseInt(item);
						}
						catch (NumberFormatException nfe)
						{
							_log.warning("Player Spawn Protection: Wrong ItemId passed: " + item);
							_log.warning(nfe.getMessage());
						}
						if (itm != 0)
						{
							SPAWN_PROTECTION_ALLOWED_ITEMS.add(itm);
						}
					}
					
					PLAYER_TELEPORT_PROTECTION = Integer.parseInt(Character.getProperty("PlayerTeleportProtection", "0"));
					RANDOM_RESPAWN_IN_TOWN_ENABLED = Boolean.parseBoolean(Character.getProperty("RandomRespawnInTownEnabled", "True"));
					OFFSET_ON_TELEPORT_ENABLED = Boolean.parseBoolean(Character.getProperty("OffsetOnTeleportEnabled", "True"));
					MAX_OFFSET_ON_TELEPORT = Integer.parseInt(Character.getProperty("MaxOffsetOnTeleport", "50"));
					RESTORE_PLAYER_INSTANCE = Boolean.parseBoolean(Character.getProperty("RestorePlayerInstance", "False"));
					ALLOW_SUMMON_TO_INSTANCE = Boolean.parseBoolean(Character.getProperty("AllowSummonToInstance", "True"));
					EJECT_DEAD_PLAYER_TIME = 1000 * Integer.parseInt(Character.getProperty("EjectDeadPlayerTime", "60"));
					PETITIONING_ALLOWED = Boolean.parseBoolean(Character.getProperty("PetitioningAllowed", "True"));
					MAX_PETITIONS_PER_PLAYER = Integer.parseInt(Character.getProperty("MaxPetitionsPerPlayer", "5"));
					MAX_PETITIONS_PENDING = Integer.parseInt(Character.getProperty("MaxPetitionsPending", "25"));
					ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(Character.getProperty("AltFreeTeleporting", "False"));
					DELETE_DAYS = Integer.parseInt(Character.getProperty("DeleteCharAfterDays", "7"));
					ALT_GAME_EXPONENT_XP = Float.parseFloat(Character.getProperty("AltGameExponentXp", "0."));
					ALT_GAME_EXPONENT_SP = Float.parseFloat(Character.getProperty("AltGameExponentSp", "0."));
					PARTY_XP_CUTOFF_METHOD = Character.getProperty("PartyXpCutoffMethod", "highfive");
					PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(Character.getProperty("PartyXpCutoffPercent", "3."));
					PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(Character.getProperty("PartyXpCutoffLevel", "20"));
					final String[] gaps = Character.getProperty("PartyXpCutoffGaps", "0,9;10,14;15,99").split(";");
					PARTY_XP_CUTOFF_GAPS = new int[gaps.length][2];
					for (int i = 0; i < gaps.length; i++)
					{
						PARTY_XP_CUTOFF_GAPS[i] = new int[]
						{
							Integer.parseInt(gaps[i].split(",")[0]),
							Integer.parseInt(gaps[i].split(",")[1])
						};
					}
					final String[] percents = Character.getProperty("PartyXpCutoffGapPercent", "100;30;0").split(";");
					PARTY_XP_CUTOFF_GAP_PERCENTS = new int[percents.length];
					for (int i = 0; i < percents.length; i++)
					{
						PARTY_XP_CUTOFF_GAP_PERCENTS[i] = Integer.parseInt(percents[i]);
					}
					DISABLE_TUTORIAL = Boolean.parseBoolean(Character.getProperty("DisableTutorial", "False"));
					EXPERTISE_PENALTY = Boolean.parseBoolean(Character.getProperty("ExpertisePenalty", "True"));
					STORE_RECIPE_SHOPLIST = Boolean.parseBoolean(Character.getProperty("StoreRecipeShopList", "False"));
					STORE_UI_SETTINGS = Boolean.parseBoolean(Character.getProperty("StoreCharUiSettings", "False"));
					FORBIDDEN_NAMES = Character.getProperty("ForbiddenNames", "").split(",");
					SILENCE_MODE_EXCLUDE = Boolean.parseBoolean(Character.getProperty("SilenceModeExclude", "False"));
					ALT_VALIDATE_TRIGGER_SKILLS = Boolean.parseBoolean(Character.getProperty("AltValidateTriggerSkills", "False"));
					PLAYER_MOVEMENT_BLOCK_TIME = Integer.parseInt(Character.getProperty("NpcTalkBlockingTime", "0")) * 1000;
					SKILL_CHANCE_SHOW = Boolean.parseBoolean(Character.getProperty("SkillChanceShow", "false"));
					ALT_DAGGER_DMG_VS_HEAVY = Float.parseFloat(Character.getProperty("DaggerVSHeavy", "2.50"));
					
					ALT_DAGGER_DMG_VS_ROBE = Float.parseFloat(Character.getProperty("DaggerVSRobe", "1.80"));
					
					ALT_DAGGER_DMG_VS_LIGHT = Float.parseFloat(Character.getProperty("DaggerVSLight", "2.00"));
					ALT_BOW_DMG_VS_HEAVY = Float.parseFloat(Character.getProperty("ArcherVSHeavy", "1.00"));
					ALT_BOW_DMG_VS_ROBE = Float.parseFloat(Character.getProperty("ArcherVSRobe", "1.00"));
					ALT_BOW_DMG_VS_LIGHT = Float.parseFloat(Character.getProperty("ArcherVSLight", "1.00"));
					ALT_MAGES_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltPDamageMages", "1.00"));
					
					ALT_MAGES_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltMDamageMages", "1.00"));
					
					ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltPDamageFighters", "1.00"));
					
					ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltMDamageFighters", "1.00"));
					
					ALT_PETS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltPDamagePets", "1.00"));
					
					ALT_PETS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltMDamagePets", "1.00"));
					
					ALT_NPC_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltPDamageNpc", "1.00"));
					
					ALT_NPC_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltMDamageNpc", "1.00"));
					PATK_SPEED_MULTI = Float.parseFloat(Character.getProperty("AltAttackSpeed", "1.00"));
					MATK_SPEED_MULTI = Float.parseFloat(Character.getProperty("AltCastingSpeed", "1.00"));
					RESTORE_DISPEL_SKILLS = Boolean.parseBoolean(Character.getProperty("RestoreDispelSkills", "False"));
					RESTORE_DISPEL_SKILLS_TIME = Integer.parseInt(Character.getProperty("RestoreDispelSkillsTime", "10"));
					ALT_GAME_VIEWPLAYER = Boolean.parseBoolean(Character.getProperty("AltGameViewPlayer", "False"));
					AUTO_LOOT_BY_ID_SYSTEM = Boolean.parseBoolean(Character.getProperty("AutoLootByIdSystem", "False"));
					array = Character.getProperty("AutoLootById", "0").split(",");
					AUTO_LOOT_BY_ID = new int[array.length];
					for (int i = 0; i < array.length; i++)
					{
						AUTO_LOOT_BY_ID[i] = Integer.parseInt(array[i]);
					}
					Arrays.sort(AUTO_LOOT_BY_ID);
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + CHARACTER_CONFIG_FILE + " file.");
				}
				
				// Load Telnet L2Properties file (if exists)
				try
				{
					L2Properties telnetSettings = new L2Properties();
					is = new FileInputStream(new File(TELNET_FILE));
					telnetSettings.load(is);
					
					IS_TELNET_ENABLED = Boolean.parseBoolean(telnetSettings.getProperty("EnableTelnet", "false"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + TELNET_FILE + " File.");
				}
				
				// MMO
				try
				{
					L2Properties mmoSettings = new L2Properties();
					is = new FileInputStream(new File(MMO_CONFIG_FILE));
					mmoSettings.load(is);
					MMO_SELECTOR_SLEEP_TIME = Integer.parseInt(mmoSettings.getProperty("SleepTime", "20"));
					MMO_MAX_SEND_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxSendPerPass", "12"));
					MMO_MAX_READ_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxReadPerPass", "12"));
					MMO_HELPER_BUFFER_COUNT = Integer.parseInt(mmoSettings.getProperty("HelperBufferCount", "20"));
					MMO_TCP_NODELAY = Boolean.parseBoolean(mmoSettings.getProperty("TcpNoDelay", "False"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + MMO_CONFIG_FILE + " File.");
				}
				
				// Load IdFactory L2Properties file (if exists)
				try
				{
					L2Properties idSettings = new L2Properties();
					is = new FileInputStream(new File(ID_CONFIG_FILE));
					idSettings.load(is);
					
					IDFACTORY_TYPE = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
					BAD_ID_CHECKING = Boolean.parseBoolean(idSettings.getProperty("BadIdChecking", "True"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + ID_CONFIG_FILE + " file.");
				}
				
				// Load General L2Properties file (if exists)
				try
				{
					L2Properties General = new L2Properties();
					is = new FileInputStream(new File(GENERAL_CONFIG_FILE));
					General.load(is);
					
					EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(General.getProperty("EverybodyHasAdminRights", "false"));
					DISPLAY_SERVER_VERSION = Boolean.parseBoolean(General.getProperty("DisplayServerRevision", "True"));
					SERVER_LIST_BRACKET = Boolean.parseBoolean(General.getProperty("ServerListBrackets", "false"));
					SERVER_LIST_TYPE = getServerTypeId(General.getProperty("ServerListType", "Normal").split(","));
					SERVER_LIST_AGE = Integer.parseInt(General.getProperty("ServerListAge", "0"));
					SERVER_GMONLY = Boolean.parseBoolean(General.getProperty("ServerGMOnly", "false"));
					GM_HERO_AURA = Boolean.parseBoolean(General.getProperty("GMHeroAura", "False"));
					GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(General.getProperty("GMStartupInvulnerable", "False"));
					GM_STARTUP_INVISIBLE = Boolean.parseBoolean(General.getProperty("GMStartupInvisible", "False"));
					GM_STARTUP_SILENCE = Boolean.parseBoolean(General.getProperty("GMStartupSilence", "False"));
					GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(General.getProperty("GMStartupAutoList", "False"));
					GM_STARTUP_DIET_MODE = Boolean.parseBoolean(General.getProperty("GMStartupDietMode", "False"));
					GM_ITEM_RESTRICTION = Boolean.parseBoolean(General.getProperty("GMItemRestriction", "True"));
					GM_SKILL_RESTRICTION = Boolean.parseBoolean(General.getProperty("GMSkillRestriction", "True"));
					GM_TRADE_RESTRICTED_ITEMS = Boolean.parseBoolean(General.getProperty("GMTradeRestrictedItems", "False"));
					GM_RESTART_FIGHTING = Boolean.parseBoolean(General.getProperty("GMRestartFighting", "True"));
					GM_ANNOUNCER_NAME = Boolean.parseBoolean(General.getProperty("GMShowAnnouncerName", "False"));
					GM_CRITANNOUNCER_NAME = Boolean.parseBoolean(General.getProperty("GMShowCritAnnouncerName", "False"));
					GM_GIVE_SPECIAL_SKILLS = Boolean.parseBoolean(General.getProperty("GMGiveSpecialSkills", "False"));
					GM_GIVE_SPECIAL_AURA_SKILLS = Boolean.parseBoolean(General.getProperty("GMGiveSpecialAuraSkills", "False"));
					BYPASS_VALIDATION = Boolean.parseBoolean(General.getProperty("BypassValidation", "True"));
					GAMEGUARD_ENFORCE = Boolean.parseBoolean(General.getProperty("GameGuardEnforce", "False"));
					LOG_CHAT = Boolean.parseBoolean(General.getProperty("LogChat", "false"));
					LOG_AUTO_ANNOUNCEMENTS = Boolean.parseBoolean(General.getProperty("LogAutoAnnouncements", "False"));
					LOG_ITEMS = Boolean.parseBoolean(General.getProperty("LogItems", "false"));
					LOG_ITEMS_SMALL_LOG = Boolean.parseBoolean(General.getProperty("LogItemsSmallLog", "false"));
					LOG_ITEM_ENCHANTS = Boolean.parseBoolean(General.getProperty("LogItemEnchants", "false"));
					LOG_SKILL_ENCHANTS = Boolean.parseBoolean(General.getProperty("LogSkillEnchants", "false"));
					GMAUDIT = Boolean.parseBoolean(General.getProperty("GMAudit", "False"));
					LOG_GAME_DAMAGE = Boolean.parseBoolean(General.getProperty("LogGameDamage", "False"));
					LOG_GAME_DAMAGE_THRESHOLD = Integer.parseInt(General.getProperty("LogGameDamageThreshold", "5000"));
					SKILL_CHECK_ENABLE = Boolean.parseBoolean(General.getProperty("SkillCheckEnable", "False"));
					SKILL_CHECK_REMOVE = Boolean.parseBoolean(General.getProperty("SkillCheckRemove", "False"));
					SKILL_CHECK_GM = Boolean.parseBoolean(General.getProperty("SkillCheckGM", "True"));
					DEBUG = Boolean.parseBoolean(General.getProperty("Debug", "false"));
					PACKET_HANDLER_DEBUG = Boolean.parseBoolean(General.getProperty("PacketHandlerDebug", "false"));
					DEVELOPER = Boolean.parseBoolean(General.getProperty("Developer", "false"));
					// ACCEPT_GEOEDITOR_CONN = Boolean.parseBoolean(General.getProperty("AcceptGeoeditorConn", "false"));
					ALT_DEV_NO_HANDLERS = Boolean.parseBoolean(General.getProperty("AltDevNoHandlers", "False"));
					ALT_DEV_NO_QUESTS = Boolean.parseBoolean(General.getProperty("AltDevNoQuests", "False"));
					ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(General.getProperty("AltDevNoSpawns", "False"));
					THREAD_P_EFFECTS = Integer.parseInt(General.getProperty("ThreadPoolSizeEffects", "10"));
					THREAD_P_GENERAL = Integer.parseInt(General.getProperty("ThreadPoolSizeGeneral", "13"));
					IO_PACKET_THREAD_CORE_SIZE = Integer.parseInt(General.getProperty("UrgentPacketThreadCoreSize", "2"));
					GENERAL_PACKET_THREAD_CORE_SIZE = Integer.parseInt(General.getProperty("GeneralPacketThreadCoreSize", "4"));
					GENERAL_THREAD_CORE_SIZE = Integer.parseInt(General.getProperty("GeneralThreadCoreSize", "4"));
					AI_MAX_THREAD = Integer.parseInt(General.getProperty("AiMaxThread", "6"));
					CLIENT_PACKET_QUEUE_SIZE = Integer.parseInt(General.getProperty("ClientPacketQueueSize", "0"));
					if (CLIENT_PACKET_QUEUE_SIZE == 0)
					{
						CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 2;
					}
					CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = Integer.parseInt(General.getProperty("ClientPacketQueueMaxBurstSize", "0"));
					if (CLIENT_PACKET_QUEUE_MAX_BURST_SIZE == 0)
					{
						CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS + 1;
					}
					CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = Integer.parseInt(General.getProperty("ClientPacketQueueMaxPacketsPerSecond", "80"));
					CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = Integer.parseInt(General.getProperty("ClientPacketQueueMeasureInterval", "5"));
					CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = Integer.parseInt(General.getProperty("ClientPacketQueueMaxAveragePacketsPerSecond", "40"));
					CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = Integer.parseInt(General.getProperty("ClientPacketQueueMaxFloodsPerMin", "2"));
					CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = Integer.parseInt(General.getProperty("ClientPacketQueueMaxOverflowsPerMin", "1"));
					CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = Integer.parseInt(General.getProperty("ClientPacketQueueMaxUnderflowsPerMin", "1"));
					CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = Integer.parseInt(General.getProperty("ClientPacketQueueMaxUnknownPerMin", "5"));
					DEADLOCK_DETECTOR = Boolean.parseBoolean(General.getProperty("DeadLockDetector", "True"));
					DEADLOCK_CHECK_INTERVAL = Integer.parseInt(General.getProperty("DeadLockCheckInterval", "20"));
					RESTART_ON_DEADLOCK = Boolean.parseBoolean(General.getProperty("RestartOnDeadlock", "False"));
					ALLOW_DISCARDITEM = Boolean.parseBoolean(General.getProperty("AllowDiscardItem", "True"));
					AUTODESTROY_ITEM_AFTER = Integer.parseInt(General.getProperty("AutoDestroyDroppedItemAfter", "600"));
					HERB_AUTO_DESTROY_TIME = Integer.parseInt(General.getProperty("AutoDestroyHerbTime", "60")) * 1000;
					String[] split = General.getProperty("ListOfProtectedItems", "0").split(",");
					LIST_PROTECTED_ITEMS = new ArrayList<>(split.length);
					for (String id : split)
					{
						LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
					}
					DATABASE_CLEAN_UP = Boolean.parseBoolean(General.getProperty("DatabaseCleanUp", "true"));
					CONNECTION_CLOSE_TIME = Long.parseLong(General.getProperty("ConnectionCloseTime", "60000"));
					CHAR_STORE_INTERVAL = Integer.parseInt(General.getProperty("CharacterDataStoreInterval", "15"));
					LAZY_ITEMS_UPDATE = Boolean.parseBoolean(General.getProperty("LazyItemsUpdate", "false"));
					UPDATE_ITEMS_ON_CHAR_STORE = Boolean.parseBoolean(General.getProperty("UpdateItemsOnCharStore", "false"));
					DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(General.getProperty("DestroyPlayerDroppedItem", "false"));
					DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(General.getProperty("DestroyEquipableItem", "false"));
					SAVE_DROPPED_ITEM = Boolean.parseBoolean(General.getProperty("SaveDroppedItem", "false"));
					EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(General.getProperty("EmptyDroppedItemTableAfterLoad", "false"));
					SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(General.getProperty("SaveDroppedItemInterval", "60")) * 60000;
					CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(General.getProperty("ClearDroppedItemTable", "false"));
					AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(General.getProperty("AutoDeleteInvalidQuestData", "False"));
					PRECISE_DROP_CALCULATION = Boolean.parseBoolean(General.getProperty("PreciseDropCalculation", "True"));
					MULTIPLE_ITEM_DROP = Boolean.parseBoolean(General.getProperty("MultipleItemDrop", "True"));
					FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(General.getProperty("ForceInventoryUpdate", "False"));
					LAZY_CACHE = Boolean.parseBoolean(General.getProperty("LazyCache", "True"));
					CACHE_CHAR_NAMES = Boolean.parseBoolean(General.getProperty("CacheCharNames", "True"));
					MIN_NPC_ANIMATION = Integer.parseInt(General.getProperty("MinNPCAnimation", "10"));
					MAX_NPC_ANIMATION = Integer.parseInt(General.getProperty("MaxNPCAnimation", "20"));
					MIN_MONSTER_ANIMATION = Integer.parseInt(General.getProperty("MinMonsterAnimation", "5"));
					MAX_MONSTER_ANIMATION = Integer.parseInt(General.getProperty("MaxMonsterAnimation", "20"));
					MOVE_BASED_KNOWNLIST = Boolean.parseBoolean(General.getProperty("MoveBasedKnownlist", "False"));
					KNOWNLIST_UPDATE_INTERVAL = Long.parseLong(General.getProperty("KnownListUpdateInterval", "1250"));
					GRIDS_ALWAYS_ON = Boolean.parseBoolean(General.getProperty("GridsAlwaysOn", "False"));
					GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(General.getProperty("GridNeighborTurnOnTime", "1"));
					GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(General.getProperty("GridNeighborTurnOffTime", "90"));
					String str = General.getProperty("EnableFallingDamage", "auto");
					ENABLE_FALLING_DAMAGE = "auto".equalsIgnoreCase(str);
					
					PEACE_ZONE_MODE = Integer.parseInt(General.getProperty("PeaceZoneMode", "0"));
					DEFAULT_GLOBAL_CHAT = General.getProperty("GlobalChat", "ON");
					DEFAULT_TRADE_CHAT = General.getProperty("TradeChat", "ON");
					ALLOW_WAREHOUSE = Boolean.parseBoolean(General.getProperty("AllowWarehouse", "True"));
					WAREHOUSE_CACHE = Boolean.parseBoolean(General.getProperty("WarehouseCache", "False"));
					WAREHOUSE_CACHE_TIME = Integer.parseInt(General.getProperty("WarehouseCacheTime", "15"));
					ALLOW_REFUND = Boolean.parseBoolean(General.getProperty("AllowRefund", "True"));
					ALLOW_MAIL = Boolean.parseBoolean(General.getProperty("AllowMail", "True"));
					ALLOW_ATTACHMENTS = Boolean.parseBoolean(General.getProperty("AllowAttachments", "True"));
					ALLOW_WEAR = Boolean.parseBoolean(General.getProperty("AllowWear", "True"));
					WEAR_DELAY = Integer.parseInt(General.getProperty("WearDelay", "5"));
					WEAR_PRICE = Integer.parseInt(General.getProperty("WearPrice", "10"));
					ALLOW_LOTTERY = Boolean.parseBoolean(General.getProperty("AllowLottery", "True"));
					ALLOW_RACE = Boolean.parseBoolean(General.getProperty("AllowRace", "True"));
					ALLOW_WATER = Boolean.parseBoolean(General.getProperty("AllowWater", "True"));
					ALLOW_RENTPET = Boolean.parseBoolean(General.getProperty("AllowRentPet", "False"));
					ALLOWFISHING = Boolean.parseBoolean(General.getProperty("AllowFishing", "True"));
					ALLOW_MANOR = Boolean.parseBoolean(General.getProperty("AllowManor", "True"));
					ALLOW_BOAT = Boolean.parseBoolean(General.getProperty("AllowBoat", "True"));
					BOAT_BROADCAST_RADIUS = Integer.parseInt(General.getProperty("BoatBroadcastRadius", "20000"));
					ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(General.getProperty("AllowCursedWeapons", "True"));
					ALLOW_PET_WALKERS = Boolean.parseBoolean(General.getProperty("AllowPetWalkers", "True"));
					SERVER_NEWS = Boolean.parseBoolean(General.getProperty("ShowServerNews", "False"));
					USE_SAY_FILTER = Boolean.parseBoolean(General.getProperty("UseChatFilter", "false"));
					CHAT_FILTER_CHARS = General.getProperty("ChatFilterChars", "^_^");
					String[] propertySplit4 = General.getProperty("BanChatChannels", "0;1;8;17").trim().split(";");
					BAN_CHAT_CHANNELS = new int[propertySplit4.length];
					try
					{
						int i = 0;
						for (String chatId : propertySplit4)
						{
							BAN_CHAT_CHANNELS[i++] = Integer.parseInt(chatId);
						}
					}
					catch (NumberFormatException nfe)
					{
						_log.log(Level.WARNING, nfe.getMessage(), nfe);
					}
					ALT_MANOR_REFRESH_TIME = Integer.parseInt(General.getProperty("AltManorRefreshTime", "20"));
					ALT_MANOR_REFRESH_MIN = Integer.parseInt(General.getProperty("AltManorRefreshMin", "00"));
					ALT_MANOR_APPROVE_TIME = Integer.parseInt(General.getProperty("AltManorApproveTime", "6"));
					ALT_MANOR_APPROVE_MIN = Integer.parseInt(General.getProperty("AltManorApproveMin", "00"));
					ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(General.getProperty("AltManorMaintenancePeriod", "360000"));
					ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(General.getProperty("AltManorSaveAllActions", "false"));
					ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(General.getProperty("AltManorSavePeriodRate", "2"));
					ALT_LOTTERY_PRIZE = Long.parseLong(General.getProperty("AltLotteryPrize", "50000"));
					ALT_LOTTERY_TICKET_PRICE = Long.parseLong(General.getProperty("AltLotteryTicketPrice", "2000"));
					ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(General.getProperty("AltLottery5NumberRate", "0.6"));
					ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(General.getProperty("AltLottery4NumberRate", "0.2"));
					ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(General.getProperty("AltLottery3NumberRate", "0.2"));
					ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Long.parseLong(General.getProperty("AltLottery2and1NumberPrize", "200"));
					ALT_ITEM_AUCTION_ENABLED = Boolean.parseBoolean(General.getProperty("AltItemAuctionEnabled", "True"));
					ALT_ITEM_AUCTION_EXPIRED_AFTER = Integer.parseInt(General.getProperty("AltItemAuctionExpiredAfter", "14"));
					ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID = 1000 * (long) Integer.parseInt(General.getProperty("AltItemAuctionTimeExtendsOnBid", "0"));
					FS_TIME_ATTACK = Integer.parseInt(General.getProperty("TimeOfAttack", "50"));
					FS_TIME_COOLDOWN = Integer.parseInt(General.getProperty("TimeOfCoolDown", "5"));
					FS_TIME_ENTRY = Integer.parseInt(General.getProperty("TimeOfEntry", "3"));
					FS_TIME_WARMUP = Integer.parseInt(General.getProperty("TimeOfWarmUp", "2"));
					FS_PARTY_MEMBER_COUNT = Integer.parseInt(General.getProperty("NumberOfNecessaryPartyMembers", "4"));
					if (FS_TIME_ATTACK <= 0)
					{
						FS_TIME_ATTACK = 50;
					}
					if (FS_TIME_COOLDOWN <= 0)
					{
						FS_TIME_COOLDOWN = 5;
					}
					if (FS_TIME_ENTRY <= 0)
					{
						FS_TIME_ENTRY = 3;
					}
					if (FS_TIME_ENTRY <= 0)
					{
						FS_TIME_ENTRY = 3;
					}
					if (FS_TIME_ENTRY <= 0)
					{
						FS_TIME_ENTRY = 3;
					}
					RIFT_MIN_PARTY_SIZE = Integer.parseInt(General.getProperty("RiftMinPartySize", "5"));
					RIFT_MAX_JUMPS = Integer.parseInt(General.getProperty("MaxRiftJumps", "4"));
					RIFT_SPAWN_DELAY = Integer.parseInt(General.getProperty("RiftSpawnDelay", "10000"));
					RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(General.getProperty("AutoJumpsDelayMin", "480"));
					RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(General.getProperty("AutoJumpsDelayMax", "600"));
					RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(General.getProperty("BossRoomTimeMultiply", "1.5"));
					RIFT_ENTER_COST_RECRUIT = Integer.parseInt(General.getProperty("RecruitCost", "18"));
					RIFT_ENTER_COST_SOLDIER = Integer.parseInt(General.getProperty("SoldierCost", "21"));
					RIFT_ENTER_COST_OFFICER = Integer.parseInt(General.getProperty("OfficerCost", "24"));
					RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(General.getProperty("CaptainCost", "27"));
					RIFT_ENTER_COST_COMMANDER = Integer.parseInt(General.getProperty("CommanderCost", "30"));
					RIFT_ENTER_COST_HERO = Integer.parseInt(General.getProperty("HeroCost", "33"));
					DEFAULT_PUNISH = Integer.parseInt(General.getProperty("DefaultPunish", "2"));
					DEFAULT_PUNISH_PARAM = Integer.parseInt(General.getProperty("DefaultPunishParam", "0"));
					ONLY_GM_ITEMS_FREE = Boolean.parseBoolean(General.getProperty("OnlyGMItemsFree", "True"));
					JAIL_IS_PVP = Boolean.parseBoolean(General.getProperty("JailIsPvp", "False"));
					JAIL_DISABLE_CHAT = Boolean.parseBoolean(General.getProperty("JailDisableChat", "True"));
					JAIL_DISABLE_TRANSACTION = Boolean.parseBoolean(General.getProperty("JailDisableTransaction", "False"));
					CUSTOM_SPAWNLIST_TABLE = Boolean.parseBoolean(General.getProperty("CustomSpawnlistTable", "false"));
					SAVE_GMSPAWN_ON_CUSTOM = Boolean.parseBoolean(General.getProperty("SaveGmSpawnOnCustom", "false"));
					CUSTOM_NPC_TABLE = Boolean.parseBoolean(General.getProperty("CustomNpcTable", "false"));
					CUSTOM_NPC_SKILLS_TABLE = Boolean.parseBoolean(General.getProperty("CustomNpcSkillsTable", "false"));
					CUSTOM_TELEPORT_TABLE = Boolean.parseBoolean(General.getProperty("CustomTeleportTable", "false"));
					CUSTOM_DROPLIST_TABLE = Boolean.parseBoolean(General.getProperty("CustomDroplistTable", "false"));
					CUSTOM_NPCBUFFER_TABLES = Boolean.parseBoolean(General.getProperty("CustomNpcBufferTables", "false"));
					CUSTOM_SKILLS_LOAD = Boolean.parseBoolean(General.getProperty("CustomSkillsLoad", "false"));
					CUSTOM_ITEMS_LOAD = Boolean.parseBoolean(General.getProperty("CustomItemsLoad", "false"));
					CUSTOM_MULTISELL_LOAD = Boolean.parseBoolean(General.getProperty("CustomMultisellLoad", "false"));
					CUSTOM_BUYLIST_LOAD = Boolean.parseBoolean(General.getProperty("CustomBuyListLoad", "false"));
					ALT_BIRTHDAY_GIFT = Integer.parseInt(General.getProperty("AltBirthdayGift", "22187"));
					ALT_BIRTHDAY_MAIL_SUBJECT = General.getProperty("AltBirthdayMailSubject", "Happy Birthday!");
					ALT_BIRTHDAY_MAIL_TEXT = General.getProperty("AltBirthdayMailText", "Hello Adventurer!! Seeing as you're one year older now, I thought I would send you some birthday cheer :) Please find your birthday pack attached. May these gifts bring you joy and happiness on this very special day." + EOL + EOL + "Sincerely, Alegria");
					ENABLE_BLOCK_CHECKER_EVENT = Boolean.parseBoolean(General.getProperty("EnableBlockCheckerEvent", "false"));
					MIN_BLOCK_CHECKER_TEAM_MEMBERS = Integer.parseInt(General.getProperty("BlockCheckerMinTeamMembers", "2"));
					if (MIN_BLOCK_CHECKER_TEAM_MEMBERS < 1)
					{
						MIN_BLOCK_CHECKER_TEAM_MEMBERS = 1;
					}
					else if (MIN_BLOCK_CHECKER_TEAM_MEMBERS > 6)
					{
						MIN_BLOCK_CHECKER_TEAM_MEMBERS = 6;
					}
					HBCE_FAIR_PLAY = Boolean.parseBoolean(General.getProperty("HBCEFairPlay", "false"));
					CLEAR_CREST_CACHE = Boolean.parseBoolean(General.getProperty("ClearClanCache", "false"));
					NORMAL_ENCHANT_COST_MULTIPLIER = Integer.parseInt(General.getProperty("NormalEnchantCostMultipiler", "1"));
					SAFE_ENCHANT_COST_MULTIPLIER = Integer.parseInt(General.getProperty("SafeEnchantCostMultipiler", "5"));
					
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + GENERAL_CONFIG_FILE + " File.");
				}
				
				// Load FloodProtector L2Properties file
				try
				{
					L2Properties security = new L2Properties();
					is = new FileInputStream(new File(FLOOD_PROTECTOR_FILE));
					security.load(is);
					
					loadFloodProtectorConfigs(security);
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + FLOOD_PROTECTOR_FILE);
				}
				
				// Load NPC L2Properties file (if exists)
				try
				{
					L2Properties NPC = new L2Properties();
					is = new FileInputStream(new File(NPC_CONFIG_FILE));
					NPC.load(is);
					
					ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(NPC.getProperty("AnnounceMammonSpawn", "False"));
					ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(NPC.getProperty("AltMobAgroInPeaceZone", "True"));
					ALT_ATTACKABLE_NPCS = Boolean.parseBoolean(NPC.getProperty("AltAttackableNpcs", "True"));
					ALT_GAME_VIEWNPC = Boolean.parseBoolean(NPC.getProperty("AltGameViewNpc", "False"));
					MAX_DRIFT_RANGE = Integer.parseInt(NPC.getProperty("MaxDriftRange", "300"));
					DEEPBLUE_DROP_RULES = Boolean.parseBoolean(NPC.getProperty("UseDeepBlueDropRules", "True"));
					DEEPBLUE_DROP_RULES_RAID = Boolean.parseBoolean(NPC.getProperty("UseDeepBlueDropRulesRaid", "True"));
					SHOW_NPC_LVL = Boolean.parseBoolean(NPC.getProperty("ShowNpcLevel", "False"));
					SHOW_CREST_WITHOUT_QUEST = Boolean.parseBoolean(NPC.getProperty("ShowCrestWithoutQuest", "False"));
					ENABLE_RANDOM_ENCHANT_EFFECT = Boolean.parseBoolean(NPC.getProperty("EnableRandomEnchantEffect", "False"));
					MIN_NPC_LVL_DMG_PENALTY = Integer.parseInt(NPC.getProperty("MinNPCLevelForDmgPenalty", "78"));
					NPC_DMG_PENALTY = parseConfigLine(NPC.getProperty("DmgPenaltyForLvLDifferences", "0.7, 0.6, 0.6, 0.55"));
					NPC_CRIT_DMG_PENALTY = parseConfigLine(NPC.getProperty("CritDmgPenaltyForLvLDifferences", "0.75, 0.65, 0.6, 0.58"));
					NPC_SKILL_DMG_PENALTY = parseConfigLine(NPC.getProperty("SkillDmgPenaltyForLvLDifferences", "0.8, 0.7, 0.65, 0.62"));
					MIN_NPC_LVL_MAGIC_PENALTY = Integer.parseInt(NPC.getProperty("MinNPCLevelForMagicPenalty", "78"));
					NPC_SKILL_CHANCE_PENALTY = parseConfigLine(NPC.getProperty("SkillChancePenaltyForLvLDifferences", "2.5, 3.0, 3.25, 3.5"));
					DECAY_TIME_TASK = Integer.parseInt(NPC.getProperty("DecayTimeTask", "5000"));
					NPC_DECAY_TIME = Integer.parseInt(NPC.getProperty("NpcDecayTime", "8500"));
					RAID_BOSS_DECAY_TIME = Integer.parseInt(NPC.getProperty("RaidBossDecayTime", "30000"));
					SPOILED_DECAY_TIME = Integer.parseInt(NPC.getProperty("SpoiledDecayTime", "18500"));
					MAX_SWEEPER_TIME = Integer.parseInt(NPC.getProperty("MaxSweeperTime", "15000"));
					ENABLE_DROP_VITALITY_HERBS = Boolean.parseBoolean(NPC.getProperty("EnableVitalityHerbs", "True"));
					GUARD_ATTACK_AGGRO_MOB = Boolean.parseBoolean(NPC.getProperty("GuardAttackAggroMob", "False"));
					ALLOW_WYVERN_UPGRADER = Boolean.parseBoolean(NPC.getProperty("AllowWyvernUpgrader", "False"));
					String[] split = NPC.getProperty("ListPetRentNpc", "30827").split(",");
					LIST_PET_RENT_NPC = new ArrayList<>(split.length);
					for (String id : split)
					{
						LIST_PET_RENT_NPC.add(Integer.valueOf(id));
					}
					RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidHpRegenMultiplier", "100")) / 100;
					RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidMpRegenMultiplier", "100")) / 100;
					RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidPDefenceMultiplier", "100")) / 100;
					RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidMDefenceMultiplier", "100")) / 100;
					RAID_PATTACK_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidPAttackMultiplier", "100")) / 100;
					RAID_MATTACK_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidMAttackMultiplier", "100")) / 100;
					RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(NPC.getProperty("RaidMinRespawnMultiplier", "1.0"));
					RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(NPC.getProperty("RaidMaxRespawnMultiplier", "1.0"));
					RAID_MINION_RESPAWN_TIMER = Integer.parseInt(NPC.getProperty("RaidMinionRespawnTime", "300000"));
					final String[] propertySplit = NPC.getProperty("CustomMinionsRespawnTime", "").split(";");
					MINIONS_RESPAWN_TIME = new HashMap<>(propertySplit.length);
					for (String prop : propertySplit)
					{
						String[] propSplit = prop.split(",");
						if (propSplit.length != 2)
						{
							_log.warning(StringUtil.concat("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"", prop, "\""));
						}
						
						try
						{
							MINIONS_RESPAWN_TIME.put(Integer.valueOf(propSplit[0]), Integer.valueOf(propSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!prop.isEmpty())
							{
								_log.warning(StringUtil.concat("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime \"", propSplit[0], "\"", propSplit[1]));
							}
						}
					}
					
					RAID_DISABLE_CURSE = Boolean.parseBoolean(NPC.getProperty("DisableRaidCurse", "False"));
					RAID_CHAOS_TIME = Integer.parseInt(NPC.getProperty("RaidChaosTime", "10"));
					GRAND_CHAOS_TIME = Integer.parseInt(NPC.getProperty("GrandChaosTime", "10"));
					MINION_CHAOS_TIME = Integer.parseInt(NPC.getProperty("MinionChaosTime", "10"));
					INVENTORY_MAXIMUM_PET = Integer.parseInt(NPC.getProperty("MaximumSlotsForPet", "12"));
					PET_HP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("PetHpRegenMultiplier", "100")) / 100;
					PET_MP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("PetMpRegenMultiplier", "100")) / 100;
					split = NPC.getProperty("NonTalkingNpcs", "18684,18685,18686,18687,18688,18689,18690,18927,18933,19691,18692,31202,31203,31204,31205,31206,31207,31208,31209,31266,31557,31593,31606,31671,31672,31673,31674,31758,31955,32026,32030,32031,32032,32306,32619,32620,32621").split(",");
					NON_TALKING_NPCS = new ArrayList<>(split.length);
					for (String npcId : split)
					{
						try
						{
							NON_TALKING_NPCS.add(Integer.parseInt(npcId));
						}
						catch (NumberFormatException nfe)
						{
							if (!npcId.isEmpty())
							{
								_log.warning("Could not parse " + npcId + " id for NonTalkingNpcs. Please check that all values are digits and coma separated.");
							}
						}
					}
					LIMIT_SUMMONS_PAILAKA = Boolean.parseBoolean(NPC.getProperty("LimitSummonsPailaka", "False"));
					LUCKPY_ENABLED = Boolean.parseBoolean(NPC.getProperty("LuckpySpawnEnabled", "True"));
					DRAGON_VORTEX_UNLIMITED_SPAWN = Boolean.parseBoolean(NPC.getProperty("DragonVortexUnlimitedSpawn", "False"));
					ALLOW_RAIDBOSS_CHANCE_DEBUFF = Boolean.parseBoolean(NPC.getProperty("AllowRaidBossDebuff", "True"));
					RAIDBOSS_CHANCE_DEBUFF = Double.parseDouble(NPC.getProperty("RaidBossChanceDebuff", "0.9"));
					ALLOW_GRANDBOSS_CHANCE_DEBUFF = Boolean.parseBoolean(NPC.getProperty("AllowGrandBossDebuff", "True"));
					GRANDBOSS_CHANCE_DEBUFF = Double.parseDouble(NPC.getProperty("GrandBossChanceDebuff", "0.3"));
					RAIDBOSS_CHANCE_DEBUFF_SPECIAL = Double.parseDouble(NPC.getProperty("RaidBossChanceDebuffSpecial", "0.4"));
					GRANDBOSS_CHANCE_DEBUFF_SPECIAL = Double.parseDouble(NPC.getProperty("GrandBossChanceDebuffSpecial", "0.1"));
					
					String[] raidList = NPC.getProperty("SpecialRaidBossList", "29020,29068,29028").split(",");
					RAIDBOSS_DEBUFF_SPECIAL = new int[raidList.length];
					for (int i = 0; i < raidList.length; i++)
					{
						RAIDBOSS_DEBUFF_SPECIAL[i] = Integer.parseInt(raidList[i]);
					}
					Arrays.sort(RAIDBOSS_DEBUFF_SPECIAL);
					
					String[] grandList = NPC.getProperty("SpecialGrandBossList", "29020,29068,29028").split(",");
					GRANDBOSS_DEBUFF_SPECIAL = new int[grandList.length];
					for (int i = 0; i < grandList.length; i++)
					{
						GRANDBOSS_DEBUFF_SPECIAL[i] = Integer.parseInt(grandList[i]);
					}
					Arrays.sort(GRANDBOSS_DEBUFF_SPECIAL);
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + NPC_CONFIG_FILE + " File.");
				}
				
				// Load Rates L2Properties file (if exists)
				try
				{
					L2Properties ratesSettings = new L2Properties();
					is = new FileInputStream(new File(RATES_CONFIG_FILE));
					ratesSettings.load(is);
					
					RATE_XP = Float.parseFloat(ratesSettings.getProperty("RateXp", "1."));
					RATE_SP = Float.parseFloat(ratesSettings.getProperty("RateSp", "1."));
					RATE_PARTY_XP = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1."));
					RATE_PARTY_SP = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1."));
					RATE_CONSUMABLE_COST = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1."));
					RATE_EXTRACTABLE = Float.parseFloat(ratesSettings.getProperty("RateExtractable", "1."));
					RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1."));
					RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(ratesSettings.getProperty("RateRaidDropItems", "1."));
					RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1."));
					RATE_DROP_MANOR = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1"));
					RATE_QUEST_DROP = Float.parseFloat(ratesSettings.getProperty("RateQuestDrop", "1."));
					RATE_QUEST_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestReward", "1."));
					RATE_QUEST_REWARD_XP = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardXP", "1."));
					RATE_QUEST_REWARD_SP = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardSP", "1."));
					RATE_QUEST_REWARD_ADENA = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardAdena", "1."));
					RATE_QUEST_REWARD_USE_MULTIPLIERS = Boolean.parseBoolean(ratesSettings.getProperty("UseQuestRewardMultipliers", "False"));
					RATE_QUEST_REWARD_POTION = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardPotion", "1."));
					RATE_QUEST_REWARD_SCROLL = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardScroll", "1."));
					RATE_QUEST_REWARD_RECIPE = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardRecipe", "1."));
					RATE_QUEST_REWARD_MATERIAL = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardMaterial", "1."));
					RATE_HB_TRUST_INCREASE = Float.parseFloat(ratesSettings.getProperty("RateHellboundTrustIncrease", "1."));
					RATE_HB_TRUST_DECREASE = Float.parseFloat(ratesSettings.getProperty("RateHellboundTrustDecrease", "1."));
					
					RATE_VITALITY_LEVEL_1 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel1", "1.5"));
					RATE_VITALITY_LEVEL_2 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel2", "2."));
					RATE_VITALITY_LEVEL_3 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel3", "2.5"));
					RATE_VITALITY_LEVEL_4 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel4", "3."));
					RATE_RECOVERY_VITALITY_PEACE_ZONE = Float.parseFloat(ratesSettings.getProperty("RateRecoveryPeaceZone", "1."));
					RATE_VITALITY_LOST = Float.parseFloat(ratesSettings.getProperty("RateVitalityLost", "1."));
					RATE_VITALITY_GAIN = Float.parseFloat(ratesSettings.getProperty("RateVitalityGain", "1."));
					RATE_RECOVERY_ON_RECONNECT = Float.parseFloat(ratesSettings.getProperty("RateRecoveryOnReconnect", "4."));
					RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));
					RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));
					RATE_DROP_COMMON_HERBS = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "1."));
					RATE_DROP_HP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateHpHerbs", "1."));
					RATE_DROP_MP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateMpHerbs", "1."));
					RATE_DROP_SPECIAL_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "1."));
					RATE_DROP_VITALITY_HERBS = Float.parseFloat(ratesSettings.getProperty("RateVitalityHerbs", "1."));
					PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
					PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
					PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
					PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
					PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));
					PET_XP_RATE = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1."));
					PET_FOOD_RATE = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
					SINEATER_XP_RATE = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1."));
					KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
					KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
					KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
					KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
					KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
					
					// Initializing table
					PLAYER_XP_PERCENT_LOST = new double[Byte.MAX_VALUE + 1];
					
					// Default value
					for (int i = 0; i <= Byte.MAX_VALUE; i++)
					{
						PLAYER_XP_PERCENT_LOST[i] = 1.;
					}
					
					// Now loading into table parsed values
					try
					{
						String[] values = ratesSettings.getProperty("PlayerXPPercentLost", "0,39-7.0;40,75-4.0;76,76-2.5;77,77-2.0;78,78-1.5").split(";");
						
						for (String s : values)
						{
							int min;
							int max;
							double val;
							
							String[] vals = s.split("-");
							String[] mM = vals[0].split(",");
							
							min = Integer.parseInt(mM[0]);
							max = Integer.parseInt(mM[1]);
							val = Double.parseDouble(vals[1]);
							
							for (int i = min; i <= max; i++)
							{
								PLAYER_XP_PERCENT_LOST[i] = val;
							}
						}
					}
					catch (Exception e)
					{
						_log.warning("Error while loading Player XP percent lost");
						_log.warning("Config: " + e.getMessage());
					}
					
					String[] propertySplit = ratesSettings.getProperty("RateDropItemsById", "").split(";");
					RATE_DROP_ITEMS_ID = new HashMap<>(propertySplit.length);
					if (!propertySplit[0].isEmpty())
					{
						for (String item : propertySplit)
						{
							String[] itemSplit = item.split(",");
							if (itemSplit.length != 2)
							{
								_log.warning(StringUtil.concat("Config.load(): invalid config property -> RateDropItemsById \"", item, "\""));
							}
							else
							{
								try
								{
									RATE_DROP_ITEMS_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
								}
								catch (NumberFormatException nfe)
								{
									if (!item.isEmpty())
									{
										_log.warning(StringUtil.concat("Config.load(): invalid config property -> RateDropItemsById \"", item, "\""));
									}
								}
							}
						}
					}
					if (!RATE_DROP_ITEMS_ID.containsKey(PcInventory.ADENA_ID))
					{
						RATE_DROP_ITEMS_ID.put(PcInventory.ADENA_ID, RATE_DROP_ITEMS);
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + RATES_CONFIG_FILE + " File.");
				}
				
				// Load PvP L2Properties file (if exists)
				try
				{
					L2Properties pvpSettings = new L2Properties();
					is = new FileInputStream(new File(PVP_CONFIG_FILE));
					pvpSettings.load(is);
					
					KARMA_MIN_KARMA = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
					KARMA_MAX_KARMA = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
					KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
					KARMA_LOST_BASE = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));
					KARMA_DROP_GM = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
					KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));
					KARMA_PK_LIMIT = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));
					KARMA_NONDROPPABLE_PET_ITEMS = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
					KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694,9388,9389,9390");
					
					String[] array = KARMA_NONDROPPABLE_PET_ITEMS.split(",");
					KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[array.length];
					
					for (int i = 0; i < array.length; i++)
					{
						KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(array[i]);
					}
					
					array = KARMA_NONDROPPABLE_ITEMS.split(",");
					KARMA_LIST_NONDROPPABLE_ITEMS = new int[array.length];
					
					for (int i = 0; i < array.length; i++)
					{
						KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(array[i]);
					}
					
					// sorting so binarySearch can be used later
					Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
					Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);
					
					PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "120000"));
					PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "60000"));
					
					DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER = Integer.parseInt(pvpSettings.getProperty("DisableAttackIfLvlDifferenceOver", "0"));
					PUNISH_PK_PLAYER_IF_PKS_OVER = Integer.parseInt(pvpSettings.getProperty("PunishPKPlayerIfPKsOver", "0"));
					PK_MONITOR_PERIOD = Long.parseLong(pvpSettings.getProperty("PKMonitorPeriod", "3600"));
					PK_PUNISHMENT_TYPE = pvpSettings.getProperty("PKPunishmentType", "jail");
					PK_PUNISHMENT_PERIOD = Long.parseLong(pvpSettings.getProperty("PKPunishmentPeriod", "3600"));
					
					ALLOW_PVP_REWARD = Boolean.parseBoolean(pvpSettings.getProperty("AllowPvpReward", "false"));
					ALLOW_PVP_REWARD_AUTO_LOOT = Boolean.parseBoolean(pvpSettings.getProperty("AllowPvpRewardAutoLoot", "False"));
					ADD_EXP_SP_ON_PVP = Boolean.parseBoolean(pvpSettings.getProperty("AllowAddExpSpAtPvP", "False"));
					ADD_EXP_PVP = Integer.parseInt(pvpSettings.getProperty("AddExpAtPvp", "0"));
					ADD_SP_PVP = Integer.parseInt(pvpSettings.getProperty("AddSpAtPvp", "0"));
					PVP_REWARD_ITEM_DROP_CHANCE = Integer.parseInt(pvpSettings.getProperty("PvpRewarItemDropChance", "100"));
					PVP_REWARD_ITEM_ID = Integer.parseInt(pvpSettings.getProperty("PvpRewardItemId", "57"));
					PVP_REWARD_ITEM_AMMOUNT = Integer.parseInt(pvpSettings.getProperty("PvpRewardAmmount", "100"));
					PVP_REWARD_ITEM_RADIUS = Integer.parseInt(pvpSettings.getProperty("PvpRewardRadius", "100"));
					PVP_REWARD_MESSAGE_TEXT = pvpSettings.getProperty("PvpRewardMessageText", "You killed the player in PVP.");
					PVP_COLOR_SYSTEM = Boolean.parseBoolean(pvpSettings.getProperty("PvPColorSystem", "False"));
					PVP_AMMOUNT1 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount1", "50"));
					PVP_AMMOUNT2 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount2", "100"));
					PVP_AMMOUNT3 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount3", "150"));
					PVP_AMMOUNT4 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount4", "250"));
					PVP_AMMOUNT5 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount5", "500"));
					COLOR_FOR_AMMOUNT1 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount1", "00FF00"));
					COLOR_FOR_AMMOUNT2 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount2", "00FF00"));
					COLOR_FOR_AMMOUNT3 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount3", "00FF00"));
					COLOR_FOR_AMMOUNT4 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount4", "00FF00"));
					COLOR_FOR_AMMOUNT5 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount4", "00FF00"));
					TITLE_COLOR_FOR_AMMOUNT1 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount1", "00FF00"));
					TITLE_COLOR_FOR_AMMOUNT2 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount2", "00FF00"));
					TITLE_COLOR_FOR_AMMOUNT3 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount3", "00FF00"));
					TITLE_COLOR_FOR_AMMOUNT4 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount4", "00FF00"));
					TITLE_COLOR_FOR_AMMOUNT5 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount5", "00FF00"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + PVP_CONFIG_FILE + " File.");
				}
				// Load Olympiad L2Properties file (if exists)
				try
				{
					L2Properties olympiad = new L2Properties();
					is = new FileInputStream(new File(OLYMPIAD_CONFIG_FILE));
					olympiad.load(is);
					
					OLYMPIAD_PERIOD = olympiad.getProperty("CustomOlyPeriod", "MONTH");
					ALT_OLY_START_TIME = Integer.parseInt(olympiad.getProperty("AltOlyStartTime", "18"));
					ALT_OLY_MIN = Integer.parseInt(olympiad.getProperty("AltOlyMin", "00"));
					ALT_OLY_CPERIOD = Long.parseLong(olympiad.getProperty("AltOlyCPeriod", "21600000"));
					ALT_OLY_BATTLE = Long.parseLong(olympiad.getProperty("AltOlyBattle", "300000"));
					ALT_OLY_WPERIOD = Long.parseLong(olympiad.getProperty("AltOlyWPeriod", "604800000"));
					ALT_OLY_VPERIOD = Long.parseLong(olympiad.getProperty("AltOlyVPeriod", "86400000"));
					ALT_OLY_START_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyStartPoints", "10"));
					ALT_OLY_WEEKLY_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyWeeklyPoints", "10"));
					ALT_OLY_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyClassedParticipants", "11"));
					ALT_OLY_NONCLASSED = Integer.parseInt(olympiad.getProperty("AltOlyNonClassedParticipants", "11"));
					ALT_OLY_TEAMS = Integer.parseInt(olympiad.getProperty("AltOlyTeamsParticipants", "6"));
					ALT_OLY_REG_DISPLAY = Integer.parseInt(olympiad.getProperty("AltOlyRegistrationDisplayNumber", "100"));
					ALT_OLY_CLASSED_REWARD = parseItemsList(olympiad.getProperty("AltOlyClassedReward", "13722,50"));
					ALT_OLY_NONCLASSED_REWARD = parseItemsList(olympiad.getProperty("AltOlyNonClassedReward", "13722,40"));
					ALT_OLY_TEAM_REWARD = parseItemsList(olympiad.getProperty("AltOlyTeamReward", "13722,85"));
					ALT_OLY_COMP_RITEM = Integer.parseInt(olympiad.getProperty("AltOlyCompRewItem", "13722"));
					ALT_OLY_MIN_MATCHES = Integer.parseInt(olympiad.getProperty("AltOlyMinMatchesForPoints", "15"));
					ALT_OLY_GP_PER_POINT = Integer.parseInt(olympiad.getProperty("AltOlyGPPerPoint", "1000"));
					ALT_OLY_HERO_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyHeroPoints", "200"));
					ALT_OLY_RANK1_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank1Points", "100"));
					ALT_OLY_RANK2_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank2Points", "75"));
					ALT_OLY_RANK3_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank3Points", "55"));
					ALT_OLY_RANK4_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank4Points", "40"));
					ALT_OLY_RANK5_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank5Points", "30"));
					ALT_OLY_MAX_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyMaxPoints", "10"));
					ALT_OLY_DIVIDER_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyDividerClassed", "5"));
					ALT_OLY_DIVIDER_NON_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyDividerNonClassed", "5"));
					ALT_OLY_MAX_WEEKLY_MATCHES = Integer.parseInt(olympiad.getProperty("AltOlyMaxWeeklyMatches", "70"));
					ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyMaxWeeklyMatchesNonClassed", "60"));
					ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyMaxWeeklyMatchesClassed", "30"));
					ALT_OLY_MAX_WEEKLY_MATCHES_TEAM = Integer.parseInt(olympiad.getProperty("AltOlyMaxWeeklyMatchesTeam", "10"));
					ALT_OLY_LOG_FIGHTS = Boolean.parseBoolean(olympiad.getProperty("AltOlyLogFights", "false"));
					ALT_OLY_SHOW_MONTHLY_WINNERS = Boolean.parseBoolean(olympiad.getProperty("AltOlyShowMonthlyWinners", "true"));
					ALT_OLY_ANNOUNCE_GAMES = Boolean.parseBoolean(olympiad.getProperty("AltOlyAnnounceGames", "true"));
					String[] split = olympiad.getProperty("AltOlyRestrictedItems", "6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,9388,9389,9390,17049,17050,17051,17052,17053,17054,17055,17056,17057,17058,17059,17060,17061,20759,20775,20776,20777,20778,14774").split(",");
					LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>(split.length);
					for (String id : split)
					{
						LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
					}
					ALT_OLY_ENCHANT_LIMIT = Integer.parseInt(olympiad.getProperty("AltOlyEnchantLimit", "-1"));
					ALT_OLY_WAIT_TIME = Integer.parseInt(olympiad.getProperty("AltOlyWaitTime", "120"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + OLYMPIAD_CONFIG_FILE + " File.");
				}
				try
				{
					L2Properties Settings = new L2Properties();
					is = new FileInputStream(HEXID_FILE);
					Settings.load(is);
					SERVER_ID = Integer.parseInt(Settings.getProperty("ServerID"));
					HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
				}
				catch (Exception e)
				{
					_log.warning("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
				}
				
				// Grandboss
				try
				{
					L2Properties grandbossSettings = new L2Properties();
					is = new FileInputStream(new File(GRANDBOSS_CONFIG_FILE));
					grandbossSettings.load(is);
					
					ANTHARAS_WAIT_TIME = Integer.parseInt(grandbossSettings.getProperty("AntharasWaitTime", "30"));
					ANTHARAS_SPAWN_INTERVAL = Integer.parseInt(grandbossSettings.getProperty("IntervalOfAntharasSpawn", "264"));
					ANTHARAS_SPAWN_RANDOM = Integer.parseInt(grandbossSettings.getProperty("RandomOfAntharasSpawn", "72"));
					
					VALAKAS_WAIT_TIME = Integer.parseInt(grandbossSettings.getProperty("ValakasWaitTime", "30"));
					VALAKAS_SPAWN_INTERVAL = Integer.parseInt(grandbossSettings.getProperty("IntervalOfValakasSpawn", "264"));
					VALAKAS_SPAWN_RANDOM = Integer.parseInt(grandbossSettings.getProperty("RandomOfValakasSpawn", "72"));
					
					BAIUM_SPAWN_INTERVAL = Integer.parseInt(grandbossSettings.getProperty("IntervalOfBaiumSpawn", "168"));
					BAIUM_SPAWN_RANDOM = Integer.parseInt(grandbossSettings.getProperty("RandomOfBaiumSpawn", "48"));
					
					CORE_SPAWN_INTERVAL = Integer.parseInt(grandbossSettings.getProperty("IntervalOfCoreSpawn", "60"));
					CORE_SPAWN_RANDOM = Integer.parseInt(grandbossSettings.getProperty("RandomOfCoreSpawn", "24"));
					
					ORFEN_SPAWN_INTERVAL = Integer.parseInt(grandbossSettings.getProperty("IntervalOfOrfenSpawn", "48"));
					ORFEN_SPAWN_RANDOM = Integer.parseInt(grandbossSettings.getProperty("RandomOfOrfenSpawn", "20"));
					
					QUEEN_ANT_SPAWN_INTERVAL = Integer.parseInt(grandbossSettings.getProperty("IntervalOfQueenAntSpawn", "36"));
					QUEEN_ANT_SPAWN_RANDOM = Integer.parseInt(grandbossSettings.getProperty("RandomOfQueenAntSpawn", "17"));
					
					BELETH_SPAWN_INTERVAL = Integer.parseInt(grandbossSettings.getProperty("IntervalOfBelethSpawn", "192"));
					BELETH_SPAWN_RANDOM = Integer.parseInt(grandbossSettings.getProperty("RandomOfBelethSpawn", "148"));
					BELETH_MIN_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("BelethMinPlayers", "36"));
					BELETH_SPAWN_DELAY = Integer.parseInt(grandbossSettings.getProperty("BelethSpawnDelay", "5"));
					BELETH_ZONE_CLEAN_DELAY = Integer.parseInt(grandbossSettings.getProperty("BelethZoneCleanUpDelay", "5"));
					BELETH_CLONES_RESPAWN = Integer.parseInt(grandbossSettings.getProperty("RespawnTimeClones", "60"));
					
					MIN_ZAKEN_DAY_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MinZakenDayPlayers", "9"));
					MIN_ZAKEN_NIGHT_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MinZakenNightPlayers", "72"));
					MAX_ZAKEN_NIGHT_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MaxZakenNightPlayers", "450"));
					
					MIN_FREYA_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MinFreyaPlayers", "18"));
					MAX_FREYA_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MaxFreyaPlayers", "27"));
					MIN_LEVEL_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MinLevelPlayers", "82"));
					MIN_FREYA_HC_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MinFreyaHcPlayers", "36"));
					MAX_FREYA_HC_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MaxFreyaHcPlayers", "45"));
					MIN_LEVEL_HC_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MinLevelHcPlayers", "82"));
					
					// High Priestess van Halter
					HPH_FIXINTERVALOFHALTER = Integer.parseInt(grandbossSettings.getProperty("FixIntervalOfHalter", "172800"));
					if ((HPH_FIXINTERVALOFHALTER < 300) || (HPH_FIXINTERVALOFHALTER > 864000))
					{
						HPH_FIXINTERVALOFHALTER = 172800;
					}
					HPH_FIXINTERVALOFHALTER *= 6000;
					HPH_RANDOMINTERVALOFHALTER = Integer.parseInt(grandbossSettings.getProperty("RandomIntervalOfHalter", "86400"));
					if ((HPH_RANDOMINTERVALOFHALTER < 300) || (HPH_RANDOMINTERVALOFHALTER > 864000))
					{
						HPH_RANDOMINTERVALOFHALTER = 86400;
					}
					HPH_RANDOMINTERVALOFHALTER *= 6000;
					HPH_APPTIMEOFHALTER = Integer.parseInt(grandbossSettings.getProperty("AppTimeOfHalter", "20"));
					if ((HPH_APPTIMEOFHALTER < 5) || (HPH_APPTIMEOFHALTER > 60))
					{
						HPH_APPTIMEOFHALTER = 20;
					}
					HPH_APPTIMEOFHALTER *= 6000;
					HPH_ACTIVITYTIMEOFHALTER = Integer.parseInt(grandbossSettings.getProperty("ActivityTimeOfHalter", "21600"));
					if ((HPH_ACTIVITYTIMEOFHALTER < 7200) || (HPH_ACTIVITYTIMEOFHALTER > 86400))
					{
						HPH_ACTIVITYTIMEOFHALTER = 21600;
					}
					HPH_ACTIVITYTIMEOFHALTER *= 1000;
					HPH_FIGHTTIMEOFHALTER = Integer.parseInt(grandbossSettings.getProperty("FightTimeOfHalter", "7200"));
					if ((HPH_FIGHTTIMEOFHALTER < 7200) || (HPH_FIGHTTIMEOFHALTER > 21600))
					{
						HPH_FIGHTTIMEOFHALTER = 7200;
					}
					HPH_FIGHTTIMEOFHALTER *= 6000;
					HPH_CALLROYALGUARDHELPERCOUNT = Integer.parseInt(grandbossSettings.getProperty("CallRoyalGuardHelperCount", "6"));
					if ((HPH_CALLROYALGUARDHELPERCOUNT < 1) || (HPH_CALLROYALGUARDHELPERCOUNT > 6))
					{
						HPH_CALLROYALGUARDHELPERCOUNT = 6;
					}
					HPH_CALLROYALGUARDHELPERINTERVAL = Integer.parseInt(grandbossSettings.getProperty("CallRoyalGuardHelperInterval", "10"));
					if ((HPH_CALLROYALGUARDHELPERINTERVAL < 1) || (HPH_CALLROYALGUARDHELPERINTERVAL > 60))
					{
						HPH_CALLROYALGUARDHELPERINTERVAL = 10;
					}
					HPH_CALLROYALGUARDHELPERINTERVAL *= 6000;
					HPH_INTERVALOFDOOROFALTER = Integer.parseInt(grandbossSettings.getProperty("IntervalOfDoorOfAlter", "5400"));
					if ((HPH_INTERVALOFDOOROFALTER < 60) || (HPH_INTERVALOFDOOROFALTER > 5400))
					{
						HPH_INTERVALOFDOOROFALTER = 5400;
					}
					HPH_INTERVALOFDOOROFALTER *= 6000;
					HPH_TIMEOFLOCKUPDOOROFALTAR = Integer.parseInt(grandbossSettings.getProperty("TimeOfLockUpDoorOfAltar", "180"));
					if ((HPH_TIMEOFLOCKUPDOOROFALTAR < 60) || (HPH_TIMEOFLOCKUPDOOROFALTAR > 600))
					{
						HPH_TIMEOFLOCKUPDOOROFALTAR = 180;
					}
					HPH_TIMEOFLOCKUPDOOROFALTAR *= 6000;
					
					Interval_Of_Sailren_Spawn = Integer.parseInt(grandbossSettings.getProperty("IntervalOfSailrenSpawn", "12"));
					if ((Interval_Of_Sailren_Spawn < 1) || (Interval_Of_Sailren_Spawn > 480))
					{
						Interval_Of_Sailren_Spawn = 12;
					}
					Interval_Of_Sailren_Spawn = Interval_Of_Sailren_Spawn * 3600000;
					
					Random_Of_Sailren_Spawn = Integer.parseInt(grandbossSettings.getProperty("RandomOfSailrenSpawn", "24"));
					if ((Random_Of_Sailren_Spawn < 1) || (Random_Of_Sailren_Spawn > 192))
					{
						Random_Of_Sailren_Spawn = 24;
					}
					Random_Of_Sailren_Spawn = Random_Of_Sailren_Spawn * 3600000;
					
					MIN_FRINTEZZA_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MinFrintezzaPlayers", "36"));
					MAX_FRINTEZZA_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("MaxFrintezzaPlayers", "45"));
					
					CHANGE_STATUS = Integer.parseInt(grandbossSettings.getProperty("ChangeStatus", "30"));
					CHANCE_SPAWN = Integer.parseInt(grandbossSettings.getProperty("ChanceSpawn", "50"));
					RESPAWN_TIME = Integer.parseInt(grandbossSettings.getProperty("RespawnTime", "720"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + GRANDBOSS_CONFIG_FILE + " File.");
				}
				
				// Gracia Seeds
				try
				{
					L2Properties graciaseedsSettings = new L2Properties();
					is = new FileInputStream(new File(GRACIASEEDS_CONFIG_FILE));
					graciaseedsSettings.load(is);
					
					// Seed of Destruction
					SOD_TIAT_KILL_COUNT = Integer.parseInt(graciaseedsSettings.getProperty("TiatKillCountForNextState", "10"));
					SOD_STAGE_2_LENGTH = Long.parseLong(graciaseedsSettings.getProperty("Stage2Length", "720")) * 60000;
					MIN_TIAT_PLAYERS = Integer.parseInt(graciaseedsSettings.getProperty("MinTiatPlayers", "36"));
					MAX_TIAT_PLAYERS = Integer.parseInt(graciaseedsSettings.getProperty("MaxTiatPlayers", "45"));
					SOI_EKIMUS_KILL_COUNT = Integer.parseInt(graciaseedsSettings.getProperty("EkimusKillCount", "5"));
					MIN_EKIMUS_PLAYERS = Integer.parseInt(graciaseedsSettings.getProperty("MinEkimusPlayers", "18"));
					MAX_EKIMUS_PLAYERS = Integer.parseInt(graciaseedsSettings.getProperty("MaxEkimusPlayers", "27"));
					
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + GRACIASEEDS_CONFIG_FILE + " File.");
				}
				
				try
				{
					FILTER_LIST = new ArrayList<>();
					@SuppressWarnings("resource")
					LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(CHAT_FILTER_FILE))));
					String line = null;
					while ((line = lnr.readLine()) != null)
					{
						if (line.trim().isEmpty() || (line.charAt(0) == '#'))
						{
							continue;
						}
						
						FILTER_LIST.add(line.trim());
					}
					_log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + CHAT_FILTER_FILE + " File.");
				}
				
				// Security
				try
				{
					L2Properties chSiege = new L2Properties();
					is = new FileInputStream(new File(CH_SIEGE_FILE));
					chSiege.load(is);
					
					CHS_MAX_ATTACKERS = Integer.parseInt(chSiege.getProperty("MaxAttackers", "500"));
					CHS_CLAN_MINLEVEL = Integer.parseInt(chSiege.getProperty("MinClanLevel", "4"));
					CHS_MAX_FLAGS_PER_CLAN = Integer.parseInt(chSiege.getProperty("MaxFlagsPerClan", "1"));
					CHS_ENABLE_FAME = Boolean.parseBoolean(chSiege.getProperty("EnableFame", "false"));
					CHS_FAME_AMOUNT = Integer.parseInt(chSiege.getProperty("FameAmount", "0"));
					CHS_FAME_FREQUENCY = Integer.parseInt(chSiege.getProperty("FameFrequency", "0"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties LanguageSettings = new L2Properties();
					is = new FileInputStream(new File(LANGUAGE_FILE));
					LanguageSettings.load(is);
					
					MULTILANG_ENABLE = Boolean.parseBoolean(LanguageSettings.getProperty("MultiLangEnable", "false"));
					String[] allowed = LanguageSettings.getProperty("MultiLangAllowed", "en").split(";");
					MULTILANG_ALLOWED = new ArrayList<>(allowed.length);
					for (String lang : allowed)
					{
						MULTILANG_ALLOWED.add(lang);
					}
					MULTILANG_DEFAULT = LanguageSettings.getProperty("MultiLangDefault", "en");
					if (!MULTILANG_ALLOWED.contains(MULTILANG_DEFAULT))
					{
						_log.warning("Default language: " + MULTILANG_DEFAULT + " is not in allowed list!");
					}
					MULTILANG_VOICED_ALLOW = Boolean.parseBoolean(LanguageSettings.getProperty("MultiLangVoiceCommand", "True"));
					MULTILANG_SM_ENABLE = Boolean.parseBoolean(LanguageSettings.getProperty("MultiLangSystemMessageEnable", "false"));
					allowed = LanguageSettings.getProperty("MultiLangSystemMessageAllowed", "").split(";");
					MULTILANG_SM_ALLOWED = new ArrayList<>(allowed.length);
					for (String lang : allowed)
					{
						if (!lang.isEmpty())
						{
							MULTILANG_SM_ALLOWED.add(lang);
						}
					}
					MULTILANG_NS_ENABLE = Boolean.parseBoolean(LanguageSettings.getProperty("MultiLangNpcStringEnable", "false"));
					allowed = LanguageSettings.getProperty("MultiLangNpcStringAllowed", "").split(";");
					MULTILANG_NS_ALLOWED = new ArrayList<>(allowed.length);
					for (String lang : allowed)
					{
						if (!lang.isEmpty())
						{
							MULTILANG_NS_ALLOWED.add(lang);
						}
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties VoiceSettings = new L2Properties();
					is = new FileInputStream(new File(VOICE_CONFIG_FILE));
					VoiceSettings.load(is);
					
					ALLOW_EXP_GAIN_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowExpGainCommand", "False"));
					ALLOW_AUTOLOOT_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AutoLootVoiceCommand", "False"));
					BANKING_SYSTEM_ENABLED = Boolean.parseBoolean(VoiceSettings.getProperty("BankingEnabled", "false"));
					BANKING_SYSTEM_GOLDBARS = Integer.parseInt(VoiceSettings.getProperty("BankingGoldbarCount", "1"));
					BANKING_SYSTEM_ADENA = Integer.parseInt(VoiceSettings.getProperty("BankingAdenaCount", "500000000"));
					CHAT_ADMIN = Boolean.parseBoolean(VoiceSettings.getProperty("ChatAdmin", "false"));
					HELLBOUND_STATUS = Boolean.parseBoolean(VoiceSettings.getProperty("HellboundStatus", "False"));
					DEBUG_VOICE_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("DebugVoiceCommand", "False"));
					ALLOW_CHANGE_PASSWORD = Boolean.parseBoolean(VoiceSettings.getProperty("AllowChangePassword", "False"));
					VOICE_ONLINE_ENABLE = Boolean.parseBoolean(VoiceSettings.getProperty("OnlineEnable", "False"));
					FAKE_ONLINE = Integer.parseInt(VoiceSettings.getProperty("FakeOnline", "1"));
					ALLOW_TELETO_LEADER = Boolean.parseBoolean(VoiceSettings.getProperty("AllowTeletoLeader", "False"));
					TELETO_LEADER_ID = Integer.parseInt(VoiceSettings.getProperty("TeletoLeaderId", "57"));
					TELETO_LEADER_COUNT = Integer.parseInt(VoiceSettings.getProperty("TeletoLeaderCount", "1000"));
					ALLOW_REPAIR_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowRepairCommand", "False"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties customSettings = new L2Properties();
					is = new FileInputStream(new File(CUSTOM_FILE));
					customSettings.load(is);
					
					SERVER_NAME = customSettings.getProperty("ServerName", "L2J Eternity-World Server");
					VITAMIN_MANAGER = Boolean.parseBoolean(customSettings.getProperty("VitaminManager", "False"));
					ENABLE_MANA_POTIONS_SUPPORT = Boolean.parseBoolean(customSettings.getProperty("EnableManaPotionSupport", "false"));
					DISPLAY_SERVER_TIME = Boolean.parseBoolean(customSettings.getProperty("DisplayServerTime", "false"));
					ENABLE_WAREHOUSESORTING_CLAN = Boolean.parseBoolean(customSettings.getProperty("EnableWarehouseSortingClan", "False"));
					ENABLE_WAREHOUSESORTING_PRIVATE = Boolean.parseBoolean(customSettings.getProperty("EnableWarehouseSortingPrivate", "False"));
					WELCOME_MESSAGE_ENABLED = Boolean.parseBoolean(customSettings.getProperty("ScreenWelcomeMessageEnable", "false"));
					WELCOME_MESSAGE_TEXT = customSettings.getProperty("ScreenWelcomeMessageText", "Welcome to L2J server!");
					WELCOME_MESSAGE_TIME = Integer.parseInt(customSettings.getProperty("ScreenWelcomeMessageTime", "10")) * 1000;
					ANNOUNCE_PK_PVP = Boolean.parseBoolean(customSettings.getProperty("AnnouncePkPvP", "False"));
					ANNOUNCE_PK_PVP_NORMAL_MESSAGE = Boolean.parseBoolean(customSettings.getProperty("AnnouncePkPvPNormalMessage", "True"));
					ANNOUNCE_PK_MSG = customSettings.getProperty("AnnouncePkMsg", "$killer has slaughtered $target");
					ANNOUNCE_PVP_MSG = customSettings.getProperty("AnnouncePvpMsg", "$killer has defeated $target");
					ONLINE_PLAYERS_AT_STARTUP = Boolean.parseBoolean(customSettings.getProperty("ShowOnlinePlayersAtStartup", "True"));
					ONLINE_PLAYERS_ANNOUNCE_INTERVAL = Integer.parseInt(customSettings.getProperty("OnlinePlayersAnnounceInterval", "900000"));
					ALLOW_NEW_CHARACTER_TITLE = Boolean.parseBoolean(customSettings.getProperty("AllowNewCharacterTitle", "False"));
					NEW_CHARACTER_TITLE = customSettings.getProperty("NewCharacterTitle", "Newbie");
					NEW_CHAR_IS_NOBLE = Boolean.parseBoolean(customSettings.getProperty("NewCharIsNoble", "False"));
					NEW_CHAR_IS_HERO = Boolean.parseBoolean(customSettings.getProperty("NewCharIsHero", "False"));
					CUSTOM_STARTER_ITEMS_ENABLED = Boolean.parseBoolean(customSettings.getProperty("CustomStarterItemsEnabled", "False"));
					if (Config.CUSTOM_STARTER_ITEMS_ENABLED)
					{
						String[] propertySplit = customSettings.getProperty("CustomStarterItems", "0,0").split(";");
						for (String starteritems : propertySplit)
						{
							String[] starteritemsSplit = starteritems.split(",");
							if (starteritemsSplit.length != 2)
							{
								CUSTOM_STARTER_ITEMS_ENABLED = false;
								System.out.println("StarterItems[Config.load()]: invalid config property -> starter items \"" + starteritems + "\"");
							}
							else
							{
								try
								{
									CUSTOM_STARTER_ITEMS.add(new int[]
									{
										Integer.parseInt(starteritemsSplit[0]),
										Integer.parseInt(starteritemsSplit[1])
									});
								}
								catch (NumberFormatException nfe)
								{
									if (!starteritems.equals(""))
									{
										CUSTOM_STARTER_ITEMS_ENABLED = false;
										System.out.println("StarterItems[Config.load()]: invalid config property -> starter items \"" + starteritems + "\"");
									}
								}
							}
						}
					}
					UNSTUCK_SKILL = Boolean.parseBoolean(customSettings.getProperty("UnstuckSkill", "False"));
					ALLOW_NEW_CHAR_CUSTOM_POSITION = Boolean.parseBoolean(customSettings.getProperty("AltSpawnNewChar", "False"));
					NEW_CHAR_POSITION_X = Integer.parseInt(customSettings.getProperty("AltSpawnX", "0"));
					NEW_CHAR_POSITION_Y = Integer.parseInt(customSettings.getProperty("AltSpawnY", "0"));
					NEW_CHAR_POSITION_Z = Integer.parseInt(customSettings.getProperty("AltSpawnZ", "0"));
					ENABLE_NOBLESS_COLOR = Boolean.parseBoolean(customSettings.getProperty("EnableNoblessColor", "False"));
					NOBLESS_COLOR_NAME = Integer.decode("0x" + customSettings.getProperty("NoblessColorName", "000000"));
					ENABLE_NOBLESS_TITLE_COLOR = Boolean.parseBoolean(customSettings.getProperty("EnableNoblessTitleColor", "False"));
					NOBLESS_COLOR_TITLE_NAME = Integer.decode("0x" + customSettings.getProperty("NoblessColorTitleName", "000000"));
					INFINITE_SOUL_SHOT = Boolean.parseBoolean(customSettings.getProperty("InfiniteSoulShot", "False"));
					INFINITE_SPIRIT_SHOT = Boolean.parseBoolean(customSettings.getProperty("InfiniteSpiritShot", "False"));
					INFINITE_BLESSED_SPIRIT_SHOT = Boolean.parseBoolean(customSettings.getProperty("InfiniteBlessedSpiritShot", "False"));
					INFINITE_ARROWS = Boolean.parseBoolean(customSettings.getProperty("InfiniteArrows", "False"));
					ENTER_HELLBOUND_WITHOUT_QUEST = Boolean.parseBoolean(customSettings.getProperty("EnterHellBoundWithoutQuest", "False"));
					ALLOW_SURVEY = Boolean.parseBoolean(customSettings.getProperty("AllowSurvey", "True"));
					ALLOW_SURVEY_GM_VOTING = Boolean.parseBoolean(customSettings.getProperty("AllowGmVoting", "True"));
					TIME_SURVEY = Integer.parseInt(customSettings.getProperty("TimeOfSurvey", "300"));
					AUTO_RESTART_ENABLE = Boolean.parseBoolean(customSettings.getProperty("EnableRestartSystem", "False"));
					AUTO_RESTART_TIME = Integer.parseInt(customSettings.getProperty("RestartSeconds", "360"));
					AUTO_RESTART_INTERVAL = customSettings.getProperty("RestartInterval", "00:00").split(",");
					SPEED_UP_RUN = Boolean.parseBoolean(customSettings.getProperty("SpeedUpRunInTown", "False"));
					DISCONNECT_SYSTEM_ENABLED = Boolean.parseBoolean(customSettings.getProperty("DisconnectSystemEnable", "False"));
					DISCONNECT_TIMEOUT = Integer.parseInt(customSettings.getProperty("DisconnectTimeout", "15"));
					DISCONNECT_TITLECOLOR = customSettings.getProperty("DisconnectColorTitle", "FF0000");
					DISCONNECT_TITLE = customSettings.getProperty("DisconnectTitle", "[NO CARRIER]");
					CUSTOM_ENCHANT_ITEMS_ENABLED = Boolean.parseBoolean(customSettings.getProperty("CustomEnchantSystemEnable", "False"));
					String[] propertySplit = customSettings.getProperty("CustomEnchantItemsById", "").split(";");
					ENCHANT_ITEMS_ID = new HashMap<>(propertySplit.length);
					if (!propertySplit[0].isEmpty())
					{
						for (String item : propertySplit)
						{
							String[] itemSplit = item.split(",");
							if (itemSplit.length != 2)
							{
								_log.warning(StringUtil.concat("Config.load(): invalid config property -> CustomEnchantItemsById \"", item, "\""));
							}
							else
							{
								try
								{
									ENCHANT_ITEMS_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
								}
								catch (NumberFormatException nfe)
								{
									if (!item.isEmpty())
									{
										_log.warning(StringUtil.concat("Config.load(): invalid config property -> CustomEnchantItemsById \"", item, "\""));
									}
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties pccaffeSettings = new L2Properties();
					is = new FileInputStream(new File(PCBANG_CONFIG_FILE));
					pccaffeSettings.load(is);
					
					PC_BANG_ENABLED = Boolean.parseBoolean(pccaffeSettings.getProperty("PcBangPointEnable", "false"));
					PC_POINT_ID = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointId", "-100"));
					MAX_PC_BANG_POINTS = Integer.parseInt(pccaffeSettings.getProperty("MaxPcBangPoints", "200000"));
					if (MAX_PC_BANG_POINTS < 0)
					{
						MAX_PC_BANG_POINTS = 0;
					}
					ENABLE_DOUBLE_PC_BANG_POINTS = Boolean.parseBoolean(pccaffeSettings.getProperty("DoublingAcquisitionPoints", "false"));
					DOUBLE_PC_BANG_POINTS_CHANCE = Integer.parseInt(pccaffeSettings.getProperty("DoublingAcquisitionPointsChance", "1"));
					if ((DOUBLE_PC_BANG_POINTS_CHANCE < 0) || (DOUBLE_PC_BANG_POINTS_CHANCE > 100))
					{
						DOUBLE_PC_BANG_POINTS_CHANCE = 1;
					}
					PC_BANG_MIN_LEVEL = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointMinLevel", "20"));
					PC_BANG_POINTS_MIN = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointMinCount", "20"));
					PC_BANG_POINTS_MAX = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointMaxCount", "1000000"));
					PC_BANG_INTERVAL = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointIntervalTime", "900"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties premium = new L2Properties();
					is = new FileInputStream(new File(PREMIUM_CONFIG_FILE));
					premium.load(is);
					
					USE_PREMIUMSERVICE = Boolean.parseBoolean(premium.getProperty("UsePremiumServices", "False"));
					PREMIUM_RATE_XP = Float.parseFloat(premium.getProperty("PremiumRateXp", "2"));
					PREMIUM_RATE_SP = Float.parseFloat(premium.getProperty("PremiumRateSp", "2"));
					PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(premium.getProperty("PremiumRateDropItems", "2"));
					PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(premium.getProperty("PremiumRateDropSpoil", "2"));
					PREMIUM_RATE_DROP_QUEST = Float.parseFloat(premium.getProperty("PremiumRateDropQuest", "2"));
					PREMIUM_RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(premium.getProperty("PremiumRateRaidDropItems", "2"));
					PREMIUM_ID = Integer.parseInt(premium.getProperty("PremiumID", "4037"));
					PREMIUM_COUNT = Integer.parseInt(premium.getProperty("PremiumCount", "15"));
					AUTO_GIVE_PREMIUM = Boolean.parseBoolean(premium.getProperty("AutoGivePremium", "False"));
					GIVE_PREMIUM_DAYS = Integer.parseInt(premium.getProperty("GivePremiumDays", "7"));
					NOTICE_PREMIUM_MESSAGE = Boolean.parseBoolean(premium.getProperty("NoticePremiumMessage", "False"));
					
					String[] propertySplit = premium.getProperty("PremiumRateDropItemsById", "").split(";");
					PREMIUM_RATE_DROP_ITEMS_ID = new HashMap<>(propertySplit.length);
					if (!propertySplit[0].isEmpty())
					{
						for (String item : propertySplit)
						{
							String[] itemSplit = item.split(",");
							if (itemSplit.length != 2)
							{
								_log.warning(StringUtil.concat("Config.load(): invalid config property -> PremiumRateDropItemsById \"", item, "\""));
							}
							else
							{
								try
								{
									PREMIUM_RATE_DROP_ITEMS_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
								}
								catch (NumberFormatException nfe)
								{
									if (!item.isEmpty())
									{
										_log.warning(StringUtil.concat("Config.load(): invalid config property -> PremiumRateDropItemsById \"", item, "\""));
									}
								}
							}
						}
					}
					if (!PREMIUM_RATE_DROP_ITEMS_ID.containsKey(PcInventory.ADENA_ID))
					{
						PREMIUM_RATE_DROP_ITEMS_ID.put(PcInventory.ADENA_ID, PREMIUM_RATE_DROP_ITEMS);
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties CommunityBoardSettings = new L2Properties();
					is = new FileInputStream(new File(COMMUNITY_BOARD_CONFIG_FILE));
					CommunityBoardSettings.load(is);
					
					COMMUNITY_TYPE = Integer.parseInt(CommunityBoardSettings.getProperty("CommunityType", "2"));
					ALLOW_COMMUNITY_CLAN_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowClanManager", "False"));
					ALLOW_COMMUNITY_FAVORITE_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowFavoriteManager", "False"));
					ALLOW_COMMUNITY_FORUMS_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowForumsManager", "False"));
					ALLOW_COMMUNITY_FRIENDS_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowFriendsManager", "False"));
					ALLOW_COMMUNITY_LINK_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowLinkManager", "False"));
					ALLOW_COMMUNITY_MAIL_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowMailManager", "False"));
					ALLOW_COMMUNITY_POST_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowPostManager", "False"));
					ALLOW_COMMUNITY_REGION_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowRegionManager", "False"));
					ALLOW_COMMUNITY_TOP_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowTopManager", "False"));
					ALLOW_COMMUNITY_TOPIC_MANAGER = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowTopicManager", "False"));
					BBS_SHOW_PLAYERLIST = Boolean.parseBoolean(CommunityBoardSettings.getProperty("BBSShowPlayerList", "False"));
					BBS_COUNT_PLAYERLIST = Boolean.parseBoolean(CommunityBoardSettings.getProperty("BBSCountPlayerList", "True"));
					BBS_DEFAULT = CommunityBoardSettings.getProperty("BBSDefault", "_bbshome");
					SHOW_LEVEL_COMMUNITYBOARD = Boolean.parseBoolean(CommunityBoardSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
					SHOW_STATUS_COMMUNITYBOARD = Boolean.parseBoolean(CommunityBoardSettings.getProperty("ShowStatusOnCommunityBoard", "False"));
					NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(CommunityBoardSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
					NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(CommunityBoardSettings.getProperty("NamePerRowOnCommunityBoard", "5"));
					ALLOW_CLASS_MASTERSCB = CommunityBoardSettings.getProperty("AllowClassMastersCB", "0");
					if ((ALLOW_CLASS_MASTERSCB.length() != 0) && !ALLOW_CLASS_MASTERSCB.equals("0"))
					{
						for (final String id : ALLOW_CLASS_MASTERSCB.split(","))
						{
							ALLOW_CLASS_MASTERS_LISTCB.add(Integer.parseInt(id));
						}
					}
					CLASS_MASTERS_PRICECB = CommunityBoardSettings.getProperty("ClassMastersPriceCB", "0,0,0");
					if (CLASS_MASTERS_PRICECB.length() >= 5)
					{
						int level = 0;
						for (final String id : CLASS_MASTERS_PRICECB.split(","))
						{
							CLASS_MASTERS_PRICE_LISTCB[level] = Integer.parseInt(id);
							level++;
						}
					}
					CLASS_MASTERS_PRICE_ITEMCB = Integer.parseInt(CommunityBoardSettings.getProperty("ClassMastersPriceItemCB", "57"));
					
					ALLOW_COMMUNITY_PEACE_ZONE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityPeaceZone", "False"));
					ALLOW_COMMUNITY_MULTISELL = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityMultisell", "False"));
					ALLOW_COMMUNITY_STATS = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityStats", "False"));
					ALLOW_COMMUNITY_EVENTS = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityEvents", "False"));
					ALLOW_COMMUNITY_ACCOUNT = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityAccount", "False"));
					ALLOW_COMMUNITY_TELEPORT = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityTeleport", "False"));
					ALLOW_COMMUNITY_BUFF = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityBuff", "False"));
					ALLOW_COMMUNITY_ENCHANT = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityEnchant", "False"));
					ALLOW_COMMUNITY_CLASS = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityClass", "False"));
					ENCHANT_ITEM = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantItem", "4037"));
					BUFF_ID_ITEM = Integer.parseInt(CommunityBoardSettings.getProperty("BuffItemId", "57"));
					BUFF_AMOUNT = Integer.parseInt(CommunityBoardSettings.getProperty("BuffItemAmount", "10000"));
					CANCEL_BUFF_AMOUNT = Integer.parseInt(CommunityBoardSettings.getProperty("CancelBuffItemAmount", "10000"));
					HP_BUFF_AMOUNT = Integer.parseInt(CommunityBoardSettings.getProperty("RestoreHpBuffItemAmount", "10000"));
					MP_BUFF_AMOUNT = Integer.parseInt(CommunityBoardSettings.getProperty("RestoreMpBuffItemAmount", "10000"));
					CP_BUFF_AMOUNT = Integer.parseInt(CommunityBoardSettings.getProperty("RestoreCpBuffItemAmount", "10000"));
					BUFF_MAX_SCHEMES = Integer.parseInt(CommunityBoardSettings.getProperty("BuffMaxSchemesPerChar", "4"));
					BUFF_MAX_SKILLS = Integer.parseInt(CommunityBoardSettings.getProperty("BuffMaxSkllsperScheme", "24"));
					BUFF_STATIC_BUFF_COST = Integer.parseInt(CommunityBoardSettings.getProperty("BuffStaticCostPerBuff", "10000"));
					BUFF_STORE_SCHEMES = Boolean.parseBoolean(CommunityBoardSettings.getProperty("BuffStoreSchemes", "true"));
					BUFFER_PUNISH = Integer.parseInt(CommunityBoardSettings.getProperty("BufferPunish", "1"));
					ALLOW_COMMUNITY_SERVICES = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityServices", "false"));
					DelevelItemId = Integer.parseInt(CommunityBoardSettings.getProperty("DelevelItemId", "4037"));
					DelevelItemCount = Integer.parseInt(CommunityBoardSettings.getProperty("DelevelItemCount", "10"));
					NoblItemId = Integer.parseInt(CommunityBoardSettings.getProperty("NoblItemId", "4037"));
					NoblItemCount = Integer.parseInt(CommunityBoardSettings.getProperty("NoblItemCount", "50"));
					GenderItemId = Integer.parseInt(CommunityBoardSettings.getProperty("GenderItemId", "4037"));
					GenderItemCount = Integer.parseInt(CommunityBoardSettings.getProperty("GenderItemCount", "30"));
					HeroItemId = Integer.parseInt(CommunityBoardSettings.getProperty("HeroItemId", "4037"));
					HeroItemCount = Integer.parseInt(CommunityBoardSettings.getProperty("HeroItemCount", "100"));
					RecoveryPKItemId = Integer.parseInt(CommunityBoardSettings.getProperty("RecoveryPKItemId", "4037"));
					RecoveryPKItemCount = Integer.parseInt(CommunityBoardSettings.getProperty("RecoveryPKItemCount", "10"));
					RecoveryVitalityItemId = Integer.parseInt(CommunityBoardSettings.getProperty("RecoveryVitalityItemId", "4037"));
					RecoveryVitalityItemCount = Integer.parseInt(CommunityBoardSettings.getProperty("RecoveryVitalityItemCount", "10"));
					SPItemId = Integer.parseInt(CommunityBoardSettings.getProperty("SPItemId", "4037"));
					SPItemCount = Integer.parseInt(CommunityBoardSettings.getProperty("SPItemCount", "10"));
					COMMUNITY_ENCHANT_MIN = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantMin", "5"));
					COMMUNITY_ENCHANT_MAX = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantMax", "20"));
					COMMUNITY_ENCH_D_PRICE_MOD = Double.parseDouble(CommunityBoardSettings.getProperty("Dpricemod", "1.1"));
					COMMUNITY_ENCH_C_PRICE_MOD = Double.parseDouble(CommunityBoardSettings.getProperty("Cpricemod", "1.2"));
					COMMUNITY_ENCH_B_PRICE_MOD = Double.parseDouble(CommunityBoardSettings.getProperty("Bpricemod", "1.3"));
					COMMUNITY_ENCH_A_PRICE_MOD = Double.parseDouble(CommunityBoardSettings.getProperty("Apricemod", "1.4"));
					COMMUNITY_ENCH_S_PRICE_MOD = Double.parseDouble(CommunityBoardSettings.getProperty("Spricemod", "1.5"));
					COMMUNITY_ENCH_S80_PRICE_MOD = Double.parseDouble(CommunityBoardSettings.getProperty("S80pricemod", "1.6"));
					COMMUNITY_ENCH_S84_PRICE_MOD = Double.parseDouble(CommunityBoardSettings.getProperty("S84pricemod", "1.7"));
					COMMUNITY_ENCH_D_WEAPON_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantDWeapon", "2"));
					COMMUNITY_ENCH_D_ARMOR_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantDArmor", "1"));
					COMMUNITY_ENCH_C_WEAPON_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantCWeapon", "4"));
					COMMUNITY_ENCH_C_ARMOR_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantCArmor", "2"));
					COMMUNITY_ENCH_B_WEAPON_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantBWeapon", "6"));
					COMMUNITY_ENCH_B_ARMOR_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantBArmor", "4"));
					COMMUNITY_ENCH_A_WEAPON_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantAWeapon", "8"));
					COMMUNITY_ENCH_A_ARMOR_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantAArmor", "6"));
					COMMUNITY_ENCH_S_WEAPON_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantSWeapon", "10"));
					COMMUNITY_ENCH_S_ARMOR_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantSArmor", "8"));
					COMMUNITY_ENCH_S80_WEAPON_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantS80Weapon", "12"));
					COMMUNITY_ENCH_S80_ARMOR_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantS80Armor", "10"));
					COMMUNITY_ENCH_S84_WEAPON_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantS84Weapon", "14"));
					COMMUNITY_ENCH_S84_ARMOR_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("EnchantS84Armor", "12"));
					COMMUNITY_1_PROF_REWARD = Integer.parseInt(CommunityBoardSettings.getProperty("1ProfRewardItem", "8869"));
					COMMUNITY_1_PROF_REWARD_COUNT = Integer.parseInt(CommunityBoardSettings.getProperty("1ProfRewardItemCount", "15"));
					COMMUNITY_2_PROF_REWARD = Integer.parseInt(CommunityBoardSettings.getProperty("2ProfRewardItem", "8870"));
					COMMUNITY_2_PROF_REWARD_COUNT = Integer.parseInt(CommunityBoardSettings.getProperty("2ProfRewardItemCount", "15"));
					COMMUNITY_3_PROF_REWARD = Integer.parseInt(CommunityBoardSettings.getProperty("3ProfRewardItem", "6622"));
					COMMUNITY_3_PROF_REWARD_COUNT = Integer.parseInt(CommunityBoardSettings.getProperty("3ProfRewardItemCount", "1"));
					
					NICK_NAME_CHANGE_ITEM = Integer.parseInt(CommunityBoardSettings.getProperty("NickNameChangeItem", "4037"));
					NICK_NAME_CHANGE_ITEM_COUNT = Integer.parseInt(CommunityBoardSettings.getProperty("NickNameChangeItemCount", "100"));
					CHANGE_TITLE_COLOR_ITEM = Integer.parseInt(CommunityBoardSettings.getProperty("ChangeTitleColorItem", "4037"));
					CHANGE_TITLE_COLOR_ITEM_COUNT = Integer.parseInt(CommunityBoardSettings.getProperty("ChangeTitleColorItemCount", "50"));
					CHANGE_NICK_COLOR_ITEM = Integer.parseInt(CommunityBoardSettings.getProperty("ChangeNickColorItem", "4037"));
					CHANGE_NICK_COLOR_ITEM_COUNT = Integer.parseInt(CommunityBoardSettings.getProperty("ChangeNickColorItemCount", "50"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties leaderboards = new L2Properties();
					is = new FileInputStream(new File(LEADERBOARDS_CONFIG_FILE));
					leaderboards.load(is);
					
					RANK_ARENA_ACCEPT_SAME_IP = Boolean.parseBoolean(leaderboards.getProperty("ArenaAcceptSameIP", "true"));
					
					RANK_ARENA_ENABLED = Boolean.parseBoolean(leaderboards.getProperty("RankArenaEnabled", "false"));
					RANK_ARENA_INTERVAL = Integer.parseInt(leaderboards.getProperty("RankArenaInterval", "120"));
					RANK_ARENA_REWARD_ID = Integer.parseInt(leaderboards.getProperty("RankArenaRewardId", "57"));
					RANK_ARENA_REWARD_COUNT = Integer.parseInt(leaderboards.getProperty("RankArenaRewardCount", "1000"));
					
					RANK_FISHERMAN_ENABLED = Boolean.parseBoolean(leaderboards.getProperty("RankFishermanEnabled", "false"));
					RANK_FISHERMAN_INTERVAL = Integer.parseInt(leaderboards.getProperty("RankFishermanInterval", "120"));
					RANK_FISHERMAN_REWARD_ID = Integer.parseInt(leaderboards.getProperty("RankFishermanRewardId", "57"));
					RANK_FISHERMAN_REWARD_COUNT = Integer.parseInt(leaderboards.getProperty("RankFishermanRewardCount", "1000"));
					
					RANK_CRAFT_ENABLED = Boolean.parseBoolean(leaderboards.getProperty("RankCraftEnabled", "false"));
					RANK_CRAFT_INTERVAL = Integer.parseInt(leaderboards.getProperty("RankCraftInterval", "120"));
					RANK_CRAFT_REWARD_ID = Integer.parseInt(leaderboards.getProperty("RankCraftRewardId", "57"));
					RANK_CRAFT_REWARD_COUNT = Integer.parseInt(leaderboards.getProperty("RankCraftRewardCount", "1000"));
					
					RANK_TVT_ENABLED = Boolean.parseBoolean(leaderboards.getProperty("RankTvTEnabled", "false"));
					RANK_TVT_INTERVAL = Integer.parseInt(leaderboards.getProperty("RankTvTInterval", "120"));
					RANK_TVT_REWARD_ID = Integer.parseInt(leaderboards.getProperty("RankTvTRewardId", "57"));
					RANK_TVT_REWARD_COUNT = Integer.parseInt(leaderboards.getProperty("RankTvTRewardCount", "1000"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties bwSettings = new L2Properties();
					is = new FileInputStream(new File(BW_FILE));
					bwSettings.load(is);
					
					BW_AUTO_MODE = Boolean.parseBoolean(bwSettings.getProperty("BWAutoMode", "false"));
					BW_TEAMS_NUM = Integer.parseInt(bwSettings.getProperty("BWTeamsNum", "2"));
					BW_PLAYER_LEVEL_MIN = Integer.parseInt(bwSettings.getProperty("BWPlayerLevelMin", "80"));
					BW_PLAYER_LEVEL_MAX = Integer.parseInt(bwSettings.getProperty("BWPlayerLevelMax", "85"));
					BW_ALLOW_INTERFERENCE = Boolean.parseBoolean(bwSettings.getProperty("BWAllowInterference", "false"));
					BW_ALLOW_POTIONS = Boolean.parseBoolean(bwSettings.getProperty("BWAllowPotions", "false"));
					BW_ALLOW_SUMMON = Boolean.parseBoolean(bwSettings.getProperty("BWAllowSummon", "false"));
					BW_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(bwSettings.getProperty("BWOnStartRemoveAllEffects", "true"));
					BW_ON_START_UNSUMMON_PET = Boolean.parseBoolean(bwSettings.getProperty("BWOnStartUnsummonPet", "true"));
					BW_ALLOW_ENEMY_HEALING = Boolean.parseBoolean(bwSettings.getProperty("BWAllowEnemyHealing", "false"));
					BW_ALLOW_TEAM_CASTING = Boolean.parseBoolean(bwSettings.getProperty("BWAllowTeamBuff", "false"));
					BW_ALLOW_TEAM_ATTACKING = Boolean.parseBoolean(bwSettings.getProperty("BWAllowTeamAttacking", "false"));
					BW_JOIN_CURSED = Boolean.parseBoolean(bwSettings.getProperty("BWJoinWithCursedWeapon", "true"));
					BW_PRICE_NO_KILLS = Boolean.parseBoolean(bwSettings.getProperty("BWPriceNoKills", "false"));
					BW_AURA = Boolean.parseBoolean(bwSettings.getProperty("BWAura", "true"));
					BW_EVEN_TEAMS = bwSettings.getProperty("BWEvenTeams", "SHUFFLE");
					BW_REWARD = bwSettings.getProperty("BWReward", "57:1000").split(";");
					BW_REWARD_TOP = bwSettings.getProperty("BWRewardTop", "57:1000").split(";");
					BW_ANNOUNCE_REWARD = Boolean.parseBoolean(bwSettings.getProperty("BWAnnounceReward", "false"));
					BW_EVENT_INTERVAL = bwSettings.getProperty("BWEventInterval", "20:00").split(",");
					BW_NPC_LOC = bwSettings.getProperty("BWNpcLoc", "147711,-55236,-2737").split(",");
					BW_NPC_X = Integer.parseInt(BW_NPC_LOC[0]);
					BW_NPC_Y = Integer.parseInt(BW_NPC_LOC[1]);
					BW_NPC_Z = Integer.parseInt(BW_NPC_LOC[2]);
					BW_NPC_LOC_NAME = bwSettings.getProperty("BWNpcLocName", "Goddard Town");
					BW_DEAD_LOC = bwSettings.getProperty("BWDeadLoc", "147711,-55236,-2737").split(",");
					BW_DEAD_X = Integer.parseInt(BW_DEAD_LOC[0]);
					BW_DEAD_Y = Integer.parseInt(BW_DEAD_LOC[1]);
					BW_DEAD_Z = Integer.parseInt(BW_DEAD_LOC[2]);
					BW_RES_TIME = Integer.parseInt(bwSettings.getProperty("BWResTime", "1"));
					BW_MIN_PLAYERS = Integer.parseInt(bwSettings.getProperty("BWMinPlayers", "4"));
					BW_FIGHT_TIME = Integer.parseInt(bwSettings.getProperty("BWFightTime", "10"));
					BW_COUNTDOWN_TIME = Integer.parseInt(bwSettings.getProperty("BWCountDownTime", "30"));
					String[] BWdoorsToClose = bwSettings.getProperty("BWDoorsToClose", "").split(",");
					for (String door : BWdoorsToClose)
					{
						BW_DOORS_TO_CLOSE.add(Integer.parseInt(door));
					}
					String[] BWdoorsToOpen = bwSettings.getProperty("BWDoorsToOpen", "").split(",");
					for (String door : BWdoorsToOpen)
					{
						BW_DOORS_TO_OPEN.add(Integer.parseInt(door));
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties cftSettings = new L2Properties();
					is = new FileInputStream(new File(CTF_FILE));
					cftSettings.load(is);
					
					CTF_AUTO_MODE = Boolean.parseBoolean(cftSettings.getProperty("CTFAutoMode", "false"));
					CTF_TEAMS_NUM = Integer.parseInt(cftSettings.getProperty("CTFTeamsNum", "2"));
					CTF_PLAYER_LEVEL_MIN = Integer.parseInt(cftSettings.getProperty("CTFPlayerLevelMin", "80"));
					CTF_PLAYER_LEVEL_MAX = Integer.parseInt(cftSettings.getProperty("CTFPlayerLevelMax", "85"));
					CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(cftSettings.getProperty("CTFAllowInterference", "false"));
					CTF_ALLOW_POTIONS = Boolean.parseBoolean(cftSettings.getProperty("CTFAllowPotions", "false"));
					CTF_ALLOW_SUMMON = Boolean.parseBoolean(cftSettings.getProperty("CTFAllowSummon", "false"));
					CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(cftSettings.getProperty("CTFOnStartRemoveAllEffects", "true"));
					CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(cftSettings.getProperty("CTFOnStartUnsummonPet", "true"));
					CTF_ALLOW_ENEMY_HEALING = Boolean.parseBoolean(cftSettings.getProperty("CTFAllowEnemyHealing", "false"));
					CTF_ALLOW_TEAM_CASTING = Boolean.parseBoolean(cftSettings.getProperty("CTFAllowTeamBuff", "false"));
					CTF_ALLOW_TEAM_ATTACKING = Boolean.parseBoolean(cftSettings.getProperty("CTFAllowTeamAttacking", "false"));
					CTF_JOIN_CURSED = Boolean.parseBoolean(cftSettings.getProperty("CTFJoinWithCursedWeapon", "true"));
					CTF_PRICE_NO_KILLS = Boolean.parseBoolean(cftSettings.getProperty("CTFPriceNoKills", "false"));
					CTF_AURA = Boolean.parseBoolean(cftSettings.getProperty("CTFAura", "true"));
					CTF_EVEN_TEAMS = cftSettings.getProperty("CTFEvenTeams", "SHUFFLE");
					CTF_REWARD = cftSettings.getProperty("CTFReward", "57:1000").split(";");
					CTF_REWARD_TOP = cftSettings.getProperty("CTFRewardTop", "57:1000").split(";");
					CTF_ANNOUNCE_REWARD = Boolean.parseBoolean(cftSettings.getProperty("CTFAnnounceReward", "false"));
					CTF_EVENT_INTERVAL = cftSettings.getProperty("CTFEventInterval", "20:00").split(",");
					CTF_NPC_LOC = cftSettings.getProperty("CTFNpcLoc", "147711,-55236,-2737").split(",");
					CTF_NPC_X = Integer.parseInt(CTF_NPC_LOC[0]);
					CTF_NPC_Y = Integer.parseInt(CTF_NPC_LOC[1]);
					CTF_NPC_Z = Integer.parseInt(CTF_NPC_LOC[2]);
					CTF_NPC_LOC_NAME = cftSettings.getProperty("CTFNpcLocName", "Goddard Town");
					CTF_DEAD_LOC = cftSettings.getProperty("CTFDeadLoc", "147711,-55236,-2737").split(",");
					CTF_DEAD_X = Integer.parseInt(CTF_DEAD_LOC[0]);
					CTF_DEAD_Y = Integer.parseInt(CTF_DEAD_LOC[1]);
					CTF_DEAD_Z = Integer.parseInt(CTF_DEAD_LOC[2]);
					CTF_RES_TIME = Integer.parseInt(cftSettings.getProperty("CTFResTime", "1"));
					CTF_MIN_PLAYERS = Integer.parseInt(cftSettings.getProperty("CTFMinPlayers", "4"));
					CTF_FIGHT_TIME = Integer.parseInt(cftSettings.getProperty("CTFFightTime", "10"));
					CTF_COUNTDOWN_TIME = Integer.parseInt(cftSettings.getProperty("CTFCountDownTime", "30"));
					String[] CTFdoorsToClose = cftSettings.getProperty("CTFDoorsToClose", "").split(",");
					for (String door : CTFdoorsToClose)
					{
						CTF_DOORS_TO_CLOSE.add(Integer.parseInt(door));
					}
					String[] CTFdoorsToOpen = cftSettings.getProperty("CTFDoorsToOpen", "").split(",");
					for (String door : CTFdoorsToOpen)
					{
						CTF_DOORS_TO_OPEN.add(Integer.parseInt(door));
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties dmSettings = new L2Properties();
					is = new FileInputStream(new File(DM_FILE));
					dmSettings.load(is);
					
					DM_AUTO_MODE = Boolean.parseBoolean(dmSettings.getProperty("DMAutoMode", "false"));
					DM_PLAYER_LEVEL_MIN = Integer.parseInt(dmSettings.getProperty("DMPlayerLevelMin", "80"));
					DM_PLAYER_LEVEL_MAX = Integer.parseInt(dmSettings.getProperty("DMPlayerLevelMax", "85"));
					DM_ALLOW_INTERFERENCE = Boolean.parseBoolean(dmSettings.getProperty("DMAllowInterference", "false"));
					DM_ALLOW_POTIONS = Boolean.parseBoolean(dmSettings.getProperty("DMAllowPotions", "false"));
					DM_ALLOW_SUMMON = Boolean.parseBoolean(dmSettings.getProperty("DMAllowSummon", "false"));
					DM_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(dmSettings.getProperty("DMOnStartRemoveAllEffects", "true"));
					DM_ON_START_UNSUMMON_PET = Boolean.parseBoolean(dmSettings.getProperty("DMOnStartUnsummonPet", "true"));
					DM_ALLOW_ENEMY_HEALING = Boolean.parseBoolean(dmSettings.getProperty("DMAllowEnemyHealing", "false"));
					DM_ALLOW_TEAM_CASTING = Boolean.parseBoolean(dmSettings.getProperty("DMAllowTeamBuff", "false"));
					DM_ALLOW_TEAM_ATTACKING = Boolean.parseBoolean(dmSettings.getProperty("DMAllowTeamAttacking", "false"));
					DM_JOIN_CURSED = Boolean.parseBoolean(dmSettings.getProperty("DMJoinWithCursedWeapon", "true"));
					DM_EVEN_TEAMS = "SHUFFLE"; // dmSettings.getProperty("DMEvenTeams", "SHUFFLE");
					DM_REWARD = dmSettings.getProperty("DMReward", "57:1000").split(";");
					DM_ANNOUNCE_REWARD = Boolean.parseBoolean(dmSettings.getProperty("DMAnnounceReward", "false"));
					DM_EVENT_INTERVAL = dmSettings.getProperty("DMEventInterval", "20:00").split(",");
					DM_NPC_LOC = dmSettings.getProperty("DMNpcLoc", "147711,-55236,-2737").split(",");
					DM_NPC_X = Integer.parseInt(DM_NPC_LOC[0]);
					DM_NPC_Y = Integer.parseInt(DM_NPC_LOC[1]);
					DM_NPC_Z = Integer.parseInt(DM_NPC_LOC[2]);
					DM_START_LOC = dmSettings.getProperty("DMStartLoc", "-77408,-50656,-10728").split(",");
					DM_START_LOC_X = Integer.parseInt(DM_START_LOC[0]);
					DM_START_LOC_Y = Integer.parseInt(DM_START_LOC[1]);
					DM_START_LOC_Z = Integer.parseInt(DM_START_LOC[2]);
					DM_NPC_LOC_NAME = dmSettings.getProperty("DMNpcLocName", "Goddard Town");
					DM_MIN_PLAYERS = Integer.parseInt(dmSettings.getProperty("DMMinPlayers", "4"));
					DM_FIGHT_TIME = Integer.parseInt(dmSettings.getProperty("DMFightTime", "10"));
					DM_COUNTDOWN_TIME = Integer.parseInt(dmSettings.getProperty("DMCountDownTime", "30"));
					String[] DMdoorsToClose = dmSettings.getProperty("DMDoorsToClose", "").split(",");
					for (String door : DMdoorsToClose)
					{
						DM_DOORS_TO_CLOSE.add(Integer.parseInt(door));
					}
					String[] DMdoorsToOpen = dmSettings.getProperty("DMDoorsToOpen", "").split(",");
					for (String door : DMdoorsToOpen)
					{
						DM_DOORS_TO_OPEN.add(Integer.parseInt(door));
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties funEventsSettings = new L2Properties();
					is = new FileInputStream(new File(FUN_EVENTS_FILE));
					funEventsSettings.load(is);
					
					EVENT_INSTANCE = Boolean.parseBoolean(funEventsSettings.getProperty("EventInstance", "True"));
					EVENT_SHOW_SCOREBOARD = Boolean.parseBoolean(funEventsSettings.getProperty("EventShowScoreBoard", "False"));
					EVENT_SHOW_JOIN_DIALOG = Boolean.parseBoolean(funEventsSettings.getProperty("EventShowJoinDialog", "False"));
					EVENT_CHECK_ACTIVITY_TIME = Integer.parseInt(funEventsSettings.getProperty("EventCheckActivityTime", "60"));
					ENABLE_EVENT_ENGINE = Boolean.parseBoolean(funEventsSettings.getProperty("EnableEventEngine", "False"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties ChampionSettings = new L2Properties();
					is = new FileInputStream(new File(CHAMPION_CONFIG_FILE));
					ChampionSettings.load(is);
					
					CHAMPION_ENABLE = Boolean.parseBoolean(ChampionSettings.getProperty("ChampionEnable", "false"));
					CHAMPION_PASSIVE = Boolean.parseBoolean(ChampionSettings.getProperty("ChampionPassive", "false"));
					CHAMPION_FREQUENCY = Integer.parseInt(ChampionSettings.getProperty("ChampionFrequency", "0"));
					CHAMP_TITLE = ChampionSettings.getProperty("ChampionTitle", "Champion");
					CHAMP_MIN_LVL = Integer.parseInt(ChampionSettings.getProperty("ChampionMinLevel", "20"));
					CHAMP_MAX_LVL = Integer.parseInt(ChampionSettings.getProperty("ChampionMaxLevel", "60"));
					CHAMPION_HP = Integer.parseInt(ChampionSettings.getProperty("ChampionHp", "7"));
					CHAMPION_HP_REGEN = Float.parseFloat(ChampionSettings.getProperty("ChampionHpRegen", "1."));
					CHAMPION_REWARDS = Integer.parseInt(ChampionSettings.getProperty("ChampionRewards", "8"));
					CHAMPION_ADENAS_REWARDS = Float.parseFloat(ChampionSettings.getProperty("ChampionAdenasRewards", "1"));
					CHAMPION_ATK = Float.parseFloat(ChampionSettings.getProperty("ChampionAtk", "1."));
					CHAMPION_SPD_ATK = Float.parseFloat(ChampionSettings.getProperty("ChampionSpdAtk", "1."));
					CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardLowerLvlItemChance", "0"));
					CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardHigherLvlItemChance", "0"));
					CHAMPION_REWARD_ID = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardItemID", "6393"));
					CHAMPION_REWARD_QTY = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardItemQty", "1"));
					CHAMPION_ENABLE_VITALITY = Boolean.parseBoolean(ChampionSettings.getProperty("ChampionEnableVitality", "False"));
					CHAMPION_ENABLE_IN_INSTANCES = Boolean.parseBoolean(ChampionSettings.getProperty("ChampionEnableInInstances", "False"));
					CHAMPION_ENABLE_AURA = Integer.parseInt(ChampionSettings.getProperty("ChampionEnableAura", "0"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties WeddingSettings = new L2Properties();
					is = new FileInputStream(new File(WEDDING_CONFIG_FILE));
					WeddingSettings.load(is);
					
					ALLOW_WEDDING = Boolean.parseBoolean(WeddingSettings.getProperty("AllowWedding", "False"));
					WEDDING_PRICE = Integer.parseInt(WeddingSettings.getProperty("WeddingPrice", "250000000"));
					WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingPunishInfidelity", "True"));
					WEDDING_TELEPORT = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingTeleport", "True"));
					WEDDING_TELEPORT_PRICE = Integer.parseInt(WeddingSettings.getProperty("WeddingTeleportPrice", "50000"));
					WEDDING_TELEPORT_DURATION = Integer.parseInt(WeddingSettings.getProperty("WeddingTeleportDuration", "60"));
					WEDDING_SAMESEX = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingAllowSameSex", "False"));
					WEDDING_FORMALWEAR = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingFormalWear", "True"));
					WEDDING_DIVORCE_COSTS = Integer.parseInt(WeddingSettings.getProperty("WeddingDivorceCosts", "20"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties tvtSettings = new L2Properties();
					is = new FileInputStream(new File(TVT_CONFIG_FILE));
					tvtSettings.load(is);
					
					TVT_EVENT_ENABLED = Boolean.parseBoolean(tvtSettings.getProperty("TvTEventEnabled", "false"));
					TVT_EVENT_IN_INSTANCE = Boolean.parseBoolean(tvtSettings.getProperty("TvTEventInInstance", "false"));
					TVT_EVENT_INSTANCE_FILE = tvtSettings.getProperty("TvTEventInstanceFile", "coliseum.xml");
					TVT_EVENT_INTERVAL = tvtSettings.getProperty("TvTEventInterval", "20:00").split(",");
					TVT_EVENT_PARTICIPATION_TIME = Integer.parseInt(tvtSettings.getProperty("TvTEventParticipationTime", "3600"));
					TVT_EVENT_RUNNING_TIME = Integer.parseInt(tvtSettings.getProperty("TvTEventRunningTime", "1800"));
					TVT_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(tvtSettings.getProperty("TvTEventParticipationNpcId", "0"));
					
					if (TVT_EVENT_PARTICIPATION_NPC_ID == 0)
					{
						TVT_EVENT_ENABLED = false;
						_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcId");
					}
					else
					{
						String[] propertySplit = tvtSettings.getProperty("TvTEventParticipationNpcCoordinates", "0,0,0").split(",");
						if (propertySplit.length < 3)
						{
							TVT_EVENT_ENABLED = false;
							_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationNpcCoordinates");
						}
						else
						{
							TVT_EVENT_REWARDS = new ArrayList<>();
							TVT_DOORS_IDS_TO_OPEN = new ArrayList<>();
							TVT_DOORS_IDS_TO_CLOSE = new ArrayList<>();
							TVT_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
							TVT_EVENT_TEAM_1_COORDINATES = new int[3];
							TVT_EVENT_TEAM_2_COORDINATES = new int[3];
							TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
							TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
							TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
							if (propertySplit.length == 4)
							{
								TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
							}
							TVT_EVENT_MIN_PLAYERS_IN_TEAMS = Integer.parseInt(tvtSettings.getProperty("TvTEventMinPlayersInTeams", "1"));
							TVT_EVENT_MAX_PLAYERS_IN_TEAMS = Integer.parseInt(tvtSettings.getProperty("TvTEventMaxPlayersInTeams", "20"));
							TVT_EVENT_MIN_LVL = (byte) Integer.parseInt(tvtSettings.getProperty("TvTEventMinPlayerLevel", "1"));
							TVT_EVENT_MAX_LVL = (byte) Integer.parseInt(tvtSettings.getProperty("TvTEventMaxPlayerLevel", "80"));
							TVT_EVENT_RESPAWN_TELEPORT_DELAY = Integer.parseInt(tvtSettings.getProperty("TvTEventRespawnTeleportDelay", "20"));
							TVT_EVENT_START_LEAVE_TELEPORT_DELAY = Integer.parseInt(tvtSettings.getProperty("TvTEventStartLeaveTeleportDelay", "20"));
							TVT_EVENT_EFFECTS_REMOVAL = Integer.parseInt(tvtSettings.getProperty("TvTEventEffectsRemoval", "0"));
							TVT_EVENT_MAX_PARTICIPANTS_PER_IP = Integer.parseInt(tvtSettings.getProperty("TvTEventMaxParticipantsPerIP", "0"));
							TVT_ALLOW_VOICED_COMMAND = Boolean.parseBoolean(tvtSettings.getProperty("TvTAllowVoicedInfoCommand", "false"));
							TVT_EVENT_TEAM_1_NAME = tvtSettings.getProperty("TvTEventTeam1Name", "Team1");
							propertySplit = tvtSettings.getProperty("TvTEventTeam1Coordinates", "0,0,0").split(",");
							if (propertySplit.length < 3)
							{
								TVT_EVENT_ENABLED = false;
								_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam1Coordinates");
							}
							else
							{
								TVT_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
								TVT_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
								TVT_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
								TVT_EVENT_TEAM_2_NAME = tvtSettings.getProperty("TvTEventTeam2Name", "Team2");
								propertySplit = tvtSettings.getProperty("TvTEventTeam2Coordinates", "0,0,0").split(",");
								if (propertySplit.length < 3)
								{
									TVT_EVENT_ENABLED = false;
									_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventTeam2Coordinates");
								}
								else
								{
									TVT_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
									TVT_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
									TVT_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
									propertySplit = tvtSettings.getProperty("TvTEventParticipationFee", "0,0").split(",");
									try
									{
										TVT_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
										TVT_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
									}
									catch (NumberFormatException nfe)
									{
										if (propertySplit.length > 0)
										{
											_log.warning("TvTEventEngine[Config.load()]: invalid config property -> TvTEventParticipationFee");
										}
									}
									propertySplit = tvtSettings.getProperty("TvTEventReward", "57,100000").split(";");
									for (String reward : propertySplit)
									{
										String[] rewardSplit = reward.split(",");
										if (rewardSplit.length != 2)
										{
											_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"", reward, "\""));
										}
										else
										{
											try
											{
												TVT_EVENT_REWARDS.add(new int[]
												{
													Integer.parseInt(rewardSplit[0]),
													Integer.parseInt(rewardSplit[1])
												});
											}
											catch (NumberFormatException nfe)
											{
												if (!reward.isEmpty())
												{
													_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventReward \"", reward, "\""));
												}
											}
										}
									}
									
									TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = Boolean.parseBoolean(tvtSettings.getProperty("TvTEventTargetTeamMembersAllowed", "true"));
									TVT_EVENT_SCROLL_ALLOWED = Boolean.parseBoolean(tvtSettings.getProperty("TvTEventScrollsAllowed", "false"));
									TVT_EVENT_POTIONS_ALLOWED = Boolean.parseBoolean(tvtSettings.getProperty("TvTEventPotionsAllowed", "false"));
									TVT_EVENT_SUMMON_BY_ITEM_ALLOWED = Boolean.parseBoolean(tvtSettings.getProperty("TvTEventSummonByItemAllowed", "false"));
									TVT_REWARD_TEAM_TIE = Boolean.parseBoolean(tvtSettings.getProperty("TvTRewardTeamTie", "false"));
									propertySplit = tvtSettings.getProperty("TvTDoorsToOpen", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
											{
												_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTDoorsToOpen \"", door, "\""));
											}
										}
									}
									
									propertySplit = tvtSettings.getProperty("TvTDoorsToClose", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
											{
												_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTDoorsToClose \"", door, "\""));
											}
										}
									}
									
									propertySplit = tvtSettings.getProperty("TvTEventFighterBuffs", "").split(";");
									if (!propertySplit[0].isEmpty())
									{
										TVT_EVENT_FIGHTER_BUFFS = new HashMap<>(propertySplit.length);
										for (String skill : propertySplit)
										{
											String[] skillSplit = skill.split(",");
											if (skillSplit.length != 2)
											{
												_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs \"", skill, "\""));
											}
											else
											{
												try
												{
													TVT_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
												}
												catch (NumberFormatException nfe)
												{
													if (!skill.isEmpty())
													{
														_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventFighterBuffs \"", skill, "\""));
													}
												}
											}
										}
									}
									
									propertySplit = tvtSettings.getProperty("TvTEventMageBuffs", "").split(";");
									if (!propertySplit[0].isEmpty())
									{
										TVT_EVENT_MAGE_BUFFS = new HashMap<>(propertySplit.length);
										for (String skill : propertySplit)
										{
											String[] skillSplit = skill.split(",");
											if (skillSplit.length != 2)
											{
												_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs \"", skill, "\""));
											}
											else
											{
												try
												{
													TVT_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
												}
												catch (NumberFormatException nfe)
												{
													if (!skill.isEmpty())
													{
														_log.warning(StringUtil.concat("TvTEventEngine[Config.load()]: invalid config property -> TvTEventMageBuffs \"", skill, "\""));
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties offtradeSettings = new L2Properties();
					is = new FileInputStream(new File(OFFLINE_TRADE_CONFIG_FILE));
					offtradeSettings.load(is);
					
					OFFLINE_TRADE_ENABLE = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineTradeEnable", "false"));
					OFFLINE_CRAFT_ENABLE = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineCraftEnable", "false"));
					OFFLINE_MODE_IN_PEACE_ZONE = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineModeInPaceZone", "False"));
					OFFLINE_MODE_NO_DAMAGE = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineModeNoDamage", "False"));
					OFFLINE_SET_NAME_COLOR = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineSetNameColor", "false"));
					OFFLINE_NAME_COLOR = Integer.decode("0x" + offtradeSettings.getProperty("OfflineNameColor", "808080"));
					OFFLINE_FAME = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineFame", "true"));
					RESTORE_OFFLINERS = Boolean.parseBoolean(offtradeSettings.getProperty("RestoreOffliners", "false"));
					OFFLINE_MAX_DAYS = Integer.parseInt(offtradeSettings.getProperty("OfflineMaxDays", "10"));
					OFFLINE_DISCONNECT_FINISHED = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineDisconnectFinished", "true"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties AntiFeedSettings = new L2Properties();
					is = new FileInputStream(new File(ANTIFEED_CONFIG_FILE));
					AntiFeedSettings.load(is);
					
					ANTIFEED_ENABLE = Boolean.parseBoolean(AntiFeedSettings.getProperty("AntiFeedEnable", "false"));
					ANTIFEED_DUALBOX = Boolean.parseBoolean(AntiFeedSettings.getProperty("AntiFeedDualbox", "true"));
					ANTIFEED_DISCONNECTED_AS_DUALBOX = Boolean.parseBoolean(AntiFeedSettings.getProperty("AntiFeedDisconnectedAsDualbox", "true"));
					ANTIFEED_INTERVAL = 1000 * Integer.parseInt(AntiFeedSettings.getProperty("AntiFeedInterval", "120"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties DualBoxSettings = new L2Properties();
					is = new FileInputStream(new File(DUALBOX_CONFIG_FILE));
					DualBoxSettings.load(is);
					
					DUALBOX_CHECK_MAX_PLAYERS_PER_IP = Integer.parseInt(DualBoxSettings.getProperty("DualboxCheckMaxPlayersPerIP", "0"));
					DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP = Integer.parseInt(DualBoxSettings.getProperty("DualboxCheckMaxOlympiadParticipantsPerIP", "0"));
					DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP = Integer.parseInt(DualBoxSettings.getProperty("DualboxCheckMaxL2EventParticipantsPerIP", "0"));
					String[] propertySplit = DualBoxSettings.getProperty("DualboxCheckWhitelist", "127.0.0.1,0").split(";");
					DUALBOX_CHECK_WHITELIST = new HashMap<>(propertySplit.length);
					for (String entry : propertySplit)
					{
						String[] entrySplit = entry.split(",");
						if (entrySplit.length != 2)
						{
							_log.warning(StringUtil.concat("DualboxCheck[Config.load()]: invalid config property -> DualboxCheckWhitelist \"", entry, "\""));
						}
						else
						{
							try
							{
								int num = Integer.parseInt(entrySplit[1]);
								num = num == 0 ? -1 : num;
								DUALBOX_CHECK_WHITELIST.put(InetAddress.getByName(entrySplit[0]).hashCode(), num);
							}
							catch (UnknownHostException e)
							{
								_log.warning(StringUtil.concat("DualboxCheck[Config.load()]: invalid address -> DualboxCheckWhitelist \"", entrySplit[0], "\""));
							}
							catch (NumberFormatException e)
							{
								_log.warning(StringUtil.concat("DualboxCheck[Config.load()]: invalid number -> DualboxCheckWhitelist \"", entrySplit[1], "\""));
							}
						}
					}
					PROTECTION_IP_ENABLED = Boolean.parseBoolean(DualBoxSettings.getProperty("ProtectionIpEnabled", "False"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties enchantSettings = new L2Properties();
					is = new FileInputStream(new File(ENCHANT_CONFIG_FILE));
					enchantSettings.load(is);
					
					ENCHANT_CHANCE_ELEMENT_STONE = Double.parseDouble(enchantSettings.getProperty("EnchantChanceElementStone", "50"));
					ENCHANT_CHANCE_ELEMENT_CRYSTAL = Double.parseDouble(enchantSettings.getProperty("EnchantChanceElementCrystal", "30"));
					ENCHANT_CHANCE_ELEMENT_JEWEL = Double.parseDouble(enchantSettings.getProperty("EnchantChanceElementJewel", "20"));
					ENCHANT_CHANCE_ELEMENT_ENERGY = Double.parseDouble(enchantSettings.getProperty("EnchantChanceElementEnergy", "10"));
					String[] notenchantable = enchantSettings.getProperty("EnchantBlackList", "7816,7817,7818,7819,7820,7821,7822,7823,7824,7825,7826,7827,7828,7829,7830,7831,13293,13294,13296").split(",");
					ENCHANT_BLACKLIST = new int[notenchantable.length];
					for (int i = 0; i < notenchantable.length; i++)
					{
						ENCHANT_BLACKLIST[i] = Integer.parseInt(notenchantable[i]);
					}
					Arrays.sort(ENCHANT_BLACKLIST);
					SYSTEM_BLESSED_ENCHANT = Boolean.parseBoolean(enchantSettings.getProperty("SystemBlessedEnchant", "False"));
					BLESSED_ENCHANT_SAVE = Integer.parseInt(enchantSettings.getProperty("BlessedEnchantSave", "0"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties twSettings = new L2Properties();
					is = new FileInputStream(new File(TW_CONFIG_FILE));
					twSettings.load(is);
					
					TW_AUTO_MODE = Boolean.parseBoolean(twSettings.getProperty("TWAutoMode", "false"));
					TW_EVENT_INTERVAL = twSettings.getProperty("TWEventInterval", "20:00").split(",");
					TW_TOWN_ID = Integer.parseInt(twSettings.getProperty("TWTownId", "9"));
					TW_ALL_TOWNS = Boolean.parseBoolean(twSettings.getProperty("TWAllTowns", "False"));
					TW_GIVE_PVP_AND_PK_POINTS = Boolean.parseBoolean(twSettings.getProperty("TownWarGivePvPAndPkPoints", "False"));
					TW_ALLOW_KARMA = Boolean.parseBoolean(twSettings.getProperty("TWAllowKarma", "False"));
					TW_DISABLE_GK = Boolean.parseBoolean(twSettings.getProperty("TWDisableGK", "True"));
					TW_REWARD = twSettings.getProperty("TWReward", "57:1000").split(";");
					TW_AUTO_RES = Boolean.parseBoolean(twSettings.getProperty("TWAutoRes", "False"));
					TW_FIGHT_TIME = Integer.parseInt(twSettings.getProperty("TWFightTime", "10"));
					TW_LOSE_BUFFS_ON_DEATH = Boolean.parseBoolean(twSettings.getProperty("TownWarLoseBuffsOnDeath", "False"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties HitmanSettings = new L2Properties();
					is = new FileInputStream(new File(HITMAN_CONFIG));
					HitmanSettings.load(is);
					
					HITMAN_ENABLE_EVENT = Boolean.parseBoolean(HitmanSettings.getProperty("EnableHitmanEvent", "False"));
					HITMAN_TAKE_KARMA = Boolean.parseBoolean(HitmanSettings.getProperty("HitmansTakekarma", "True"));
					HITMAN_ANNOUNCE = Boolean.parseBoolean(HitmanSettings.getProperty("HitmanAnnounce", "False"));
					HITMAN_MAX_PER_PAGE = Integer.parseInt(HitmanSettings.getProperty("HitmanMaxPerPage", "20"));
					String[] split = HitmanSettings.getProperty("HitmanCurrency", "57,4037,9143").split(",");
					HITMAN_CURRENCY = new ArrayList<>();
					for (String id : split)
					{
						try
						{
							Integer itemId = Integer.parseInt(id);
							HITMAN_CURRENCY.add(itemId);
						}
						catch (Exception e)
						{
							_log.info("Wrong config item id: " + id + ". Skipped.");
						}
					}
					HITMAN_SAME_TEAM = Boolean.parseBoolean(HitmanSettings.getProperty("HitmanSameTeam", "False"));
					HITMAN_SAVE_TARGET = Integer.parseInt(HitmanSettings.getProperty("HitmanSaveTarget", "15"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties delevelSettings = new L2Properties();
					is = new FileInputStream(new File(DELEVEL_CONFIG_FILE));
					delevelSettings.load(is);
					
					DELEVEL_NPC_ID = Integer.parseInt(delevelSettings.getProperty("DelevelNpcID", "50022"));
					DELEVEL_ITEM_ID = Integer.parseInt(delevelSettings.getProperty("DelevelItemID", "57"));
					DELEVEL_LVL_PRICE = Integer.parseInt(delevelSettings.getProperty("DelevelLvLPrice", "5000"));
					DELEVEL_VITALITY_PRICE = Integer.parseInt(delevelSettings.getProperty("DelevelVitelityPrice", "10000"));
					DELEVEL_NPC_ENABLE = Boolean.parseBoolean(delevelSettings.getProperty("AllowDelevelNPC", "False"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties renameSettings = new L2Properties();
					is = new FileInputStream(new File(RENAME_CONFIG_FILE));
					renameSettings.load(is);
					
					RENAME_NPC_ID = Integer.parseInt(renameSettings.getProperty("RenameNpcID", "50024"));
					RENAME_NPC_MIN_LEVEL = Integer.parseInt(renameSettings.getProperty("RenameNpcMinLevel", "40"));
					RENAME_NPC_FEE = renameSettings.getProperty("RenameNpcFee", "57,250000");
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties ColorNameNpcSettings = new L2Properties();
					is = new FileInputStream(new File(COLORNAME_NPC_CONFIG));
					ColorNameNpcSettings.load(is);
					
					COLORNAME_NPC_ID = Integer.parseInt(ColorNameNpcSettings.getProperty("ColorNameNpcID", "50023"));
					COLORNAME_NPC_MIN_LEVEL = Integer.parseInt(ColorNameNpcSettings.getProperty("ColorNameNpcMinLevel", "40"));
					COLORNAME_NPC_COLORS = ColorNameNpcSettings.getProperty("ColorNameNpcColors", "009900;FF99FF;BF00FF;FFFFFF;00FF00;000000;80FF80;AAAAAA").split("\\;");
					COLORNAME_NPC_SETTINGS_LINE = ColorNameNpcSettings.getProperty("ConfigColorNameNpc");
					COLORNAME_NPC_SETTINGS = new ColorNameNpcSettings(COLORNAME_NPC_SETTINGS_LINE);
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties ServerInfoSettings = new L2Properties();
					is = new FileInputStream(new File(SERVERINFO_NPC_CONFIG));
					ServerInfoSettings.load(is);
					
					SERVERINFO_NPC_ID = Integer.parseInt(ServerInfoSettings.getProperty("ServerInfoNpcID", "50026"));
					SERVERINFO_NPC_ADM = ServerInfoSettings.getProperty("ServerInfoNpcAdm", "AdmServer").split("\\;");
					SERVERINFO_NPC_GM = ServerInfoSettings.getProperty("ServerInfoNpcGm", "GmServer 01;GmServer 02").split("\\;");
					SERVERINFO_NPC_DESCRIPTION = ServerInfoSettings.getProperty("ServerInfoNpcDescription", "Server description.");
					SERVERINFO_NPC_EMAIL = ServerInfoSettings.getProperty("ServerInfoNpcEmail", "user@user.com");
					SERVERINFO_NPC_PHONE = ServerInfoSettings.getProperty("ServerInfoNpcPhone", "0");
					SERVERINFO_NPC_CUSTOM = ServerInfoSettings.getProperty("ServerInfoNpcCustom", "ame 01;Name 02;Name 03").split("\\;");
					SERVERINFO_NPC_DISABLE_PAGE = ServerInfoSettings.getProperty("ServerInfoNpcDisablePage", "0").split("\\;");
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties undergroundcoliseum = new L2Properties();
					is = new FileInputStream(new File(UNDERGROUND_CONFIG_FILE));
					undergroundcoliseum.load(is);
					
					UC_START_HOUR = Integer.parseInt(undergroundcoliseum.getProperty("StartHour", "21"));
					UC_END_HOUR = Integer.parseInt(undergroundcoliseum.getProperty("EndHour", "23"));
					UC_ROUND_TIME = Integer.parseInt(undergroundcoliseum.getProperty("RoundTime", "10"));
					UC_PARTY_LIMIT = Integer.parseInt(undergroundcoliseum.getProperty("PartyLimit", "7"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties vote = new L2Properties();
					is = new FileInputStream(new File(VOTE_CONFIG_FILE));
					vote.load(is);
					
					ALLOW_HOPZONE_VOTE_REWARD = Boolean.parseBoolean(vote.getProperty("AllowHopzoneVoteReward", "false"));
					HOPZONE_SERVER_LINK = vote.getProperty("HopzoneServerLink", "http://l2.hopzone.net/lineage2/details/74078/L2World-Servers/");
					HOPZONE_FIRST_PAGE_LINK = vote.getProperty("HopzoneFirstPageLink", "http://l2.hopzone.net/lineage2/");
					HOPZONE_VOTES_DIFFERENCE = Integer.parseInt(vote.getProperty("HopzoneVotesDifference", "5"));
					HOPZONE_FIRST_PAGE_RANK_NEEDED = Integer.parseInt(vote.getProperty("HopzoneFirstPageRankNeeded", "15"));
					HOPZONE_REWARD_CHECK_TIME = Integer.parseInt(vote.getProperty("HopzoneRewardCheckTime", "5"));
					String HOPZONE_SMALL_REWARD_VALUE = vote.getProperty("HopzoneSmallReward", "57,100000000;");
					String[] hopzone_small_reward_splitted_1 = HOPZONE_SMALL_REWARD_VALUE.split(";");
					for (String i : hopzone_small_reward_splitted_1)
					{
						String[] hopzone_small_reward_splitted_2 = i.split(",");
						HOPZONE_SMALL_REWARD.put(Integer.parseInt(hopzone_small_reward_splitted_2[0]), Integer.parseInt(hopzone_small_reward_splitted_2[1]));
					}
					String HOPZONE_BIG_REWARD_VALUE = vote.getProperty("HopzoneBigReward", "3470,1;");
					String[] hopzone_big_reward_splitted_1 = HOPZONE_BIG_REWARD_VALUE.split(";");
					for (String i : hopzone_big_reward_splitted_1)
					{
						String[] hopzone_big_reward_splitted_2 = i.split(",");
						HOPZONE_BIG_REWARD.put(Integer.parseInt(hopzone_big_reward_splitted_2[0]), Integer.parseInt(hopzone_big_reward_splitted_2[1]));
					}
					HOPZONE_DUALBOXES_ALLOWED = Integer.parseInt(vote.getProperty("HopzoneDualboxesAllowed", "1"));
					ALLOW_HOPZONE_GAME_SERVER_REPORT = Boolean.parseBoolean(vote.getProperty("AllowHopzoneGameServerReport", "false"));
					ALLOW_TOPZONE_VOTE_REWARD = Boolean.parseBoolean(vote.getProperty("AllowTopzoneVoteReward", "false"));
					TOPZONE_SERVER_LINK = vote.getProperty("TopzoneServerLink", "http://l2.topzone.net/lineage2/details/74078/L2World-Servers/");
					TOPZONE_FIRST_PAGE_LINK = vote.getProperty("TopzoneFirstPageLink", "http://l2.topzone.net/lineage2/");
					TOPZONE_VOTES_DIFFERENCE = Integer.parseInt(vote.getProperty("TopzoneVotesDifference", "5"));
					TOPZONE_FIRST_PAGE_RANK_NEEDED = Integer.parseInt(vote.getProperty("TopzoneFirstPageRankNeeded", "15"));
					TOPZONE_REWARD_CHECK_TIME = Integer.parseInt(vote.getProperty("TopzoneRewardCheckTime", "5"));
					String TOPZONE_SMALL_REWARD_VALUE = vote.getProperty("TopzoneSmallReward", "57,100000000;");
					String[] topzone_small_reward_splitted_1 = TOPZONE_SMALL_REWARD_VALUE.split(";");
					for (String i : topzone_small_reward_splitted_1)
					{
						String[] topzone_small_reward_splitted_2 = i.split(",");
						TOPZONE_SMALL_REWARD.put(Integer.parseInt(topzone_small_reward_splitted_2[0]), Integer.parseInt(topzone_small_reward_splitted_2[1]));
					}
					String TOPZONE_BIG_REWARD_VALUE = vote.getProperty("TopzoneBigReward", "3470,1;");
					String[] topzone_big_reward_splitted_1 = TOPZONE_BIG_REWARD_VALUE.split(";");
					for (String i : topzone_big_reward_splitted_1)
					{
						String[] topzone_big_reward_splitted_2 = i.split(",");
						TOPZONE_BIG_REWARD.put(Integer.parseInt(topzone_big_reward_splitted_2[0]), Integer.parseInt(topzone_big_reward_splitted_2[1]));
					}
					TOPZONE_DUALBOXES_ALLOWED = Integer.parseInt(vote.getProperty("TopzoneDualboxesAllowed", "1"));
					ALLOW_TOPZONE_GAME_SERVER_REPORT = Boolean.parseBoolean(vote.getProperty("AllowTopzoneGameServerReport", "false"));
					ALLOW_VOTE_REWARD_SYSTEM = Boolean.parseBoolean(vote.getProperty("AllowVoteRewardSystem", "False"));
					VOTE_REWARD_ADENA_AMOUNT = Integer.parseInt(vote.getProperty("VoteRewardAdenaAmount", "25000000"));
					VOTE_REWARD_SECOND_ID = Integer.parseInt(vote.getProperty("VoteRewardSecondID", "4037"));
					VOTE_REWARD_SECOND_COUNT = Integer.parseInt(vote.getProperty("VoteRewardSecondCount", "1"));
					
					VOTE_LINK_HOPZONE = vote.getProperty("HopzoneUrl", "http://l2.hopzone.net/lineage2/details/99999/My_Server");
					VOTE_LINK_TOPZONE = vote.getProperty("TopzoneUrl", "http://l2topzone.com/lineage2/server-info/1111/My_Server.html");
					VOTE_REWARD_ID = Integer.parseInt(vote.getProperty("VoteRewardId", "4037"));
					VOTE_REWARD_AMOUNT = Integer.parseInt(vote.getProperty("VoteRewardAmount", "10"));
					SECS_TO_VOTE = Integer.parseInt(vote.getProperty("SecondsToVote", "20"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties tvtround = new L2Properties();
					is = new FileInputStream(new File(TVT_ROUND_CONFIGURATION_FILE));
					tvtround.load(is);
					
					TVT_ROUND_EVENT_ENABLED = Boolean.parseBoolean(tvtround.getProperty("TvTRoundEventEnabled", "false"));
					TVT_ROUND_EVENT_IN_INSTANCE = Boolean.parseBoolean(tvtround.getProperty("TvTRoundEventInInstance", "false"));
					TVT_ROUND_EVENT_INSTANCE_FILE = tvtround.getProperty("TvTRoundEventInstanceFile", "coliseum.xml");
					TVT_ROUND_EVENT_INTERVAL = tvtround.getProperty("TvTRoundEventInterval", "20:00").split(",");
					TVT_ROUND_EVENT_PARTICIPATION_TIME = Integer.parseInt(tvtround.getProperty("TvTRoundEventParticipationTime", "3600"));
					TVT_ROUND_EVENT_FIRST_FIGHT_RUNNING_TIME = Integer.parseInt(tvtround.getProperty("TvTRoundEventFirstFightRunningTime", "1800"));
					TVT_ROUND_EVENT_SECOND_FIGHT_RUNNING_TIME = Integer.parseInt(tvtround.getProperty("TvTRoundEventSecondFightRunningTime", "1800"));
					TVT_ROUND_EVENT_THIRD_FIGHT_RUNNING_TIME = Integer.parseInt(tvtround.getProperty("TvTRoundEventThirdFightRunningTime", "1800"));
					TVT_ROUND_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(tvtround.getProperty("TvTRoundEventParticipationNpcId", "0"));
					TVT_ROUND_EVENT_ON_DIE = Boolean.parseBoolean(tvtround.getProperty("TvTRoundEventOnDie", "true"));
					if (TVT_ROUND_EVENT_PARTICIPATION_NPC_ID == 0)
					{
						TVT_ROUND_EVENT_ENABLED = false;
						_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventParticipationNpcId");
					}
					else
					{
						String[] propertySplit = tvtround.getProperty("TvTRoundEventParticipationNpcCoordinates", "0,0,0").split(",");
						if (propertySplit.length < 3)
						{
							TVT_ROUND_EVENT_ENABLED = false;
							_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventParticipationNpcCoordinates");
						}
						else
						{
							TVT_ROUND_EVENT_REWARDS = new ArrayList<>();
							TVT_ROUND_DOORS_IDS_TO_OPEN = new ArrayList<>();
							TVT_ROUND_DOORS_IDS_TO_CLOSE = new ArrayList<>();
							TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE = new ArrayList<>();
							TVT_ROUND_EVENT_WAIT_OPEN_ANTEROOM_DOORS = Integer.parseInt(tvtround.getProperty("TvTRoundEventWaitOpenAnteroomDoors", "30"));
							TVT_ROUND_EVENT_WAIT_CLOSE_ANTEROOM_DOORS = Integer.parseInt(tvtround.getProperty("TvTRoundEventWaitCloseAnteroomDoors", "15"));
							TVT_ROUND_EVENT_STOP_ON_TIE = Boolean.parseBoolean(tvtround.getProperty("TvTRoundEventStopOnTie", "false"));
							TVT_ROUND_EVENT_MINIMUM_TIE = Integer.parseInt(tvtround.getProperty("TvTRoundEventMinimumTie", "1"));
							if ((TVT_ROUND_EVENT_MINIMUM_TIE != 1) && (TVT_ROUND_EVENT_MINIMUM_TIE != 2) && (TVT_ROUND_EVENT_MINIMUM_TIE != 3))
							{
								TVT_ROUND_EVENT_MINIMUM_TIE = 1;
							}
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES = new int[4];
							TVT_ROUND_EVENT_TEAM_1_COORDINATES = new int[3];
							TVT_ROUND_EVENT_TEAM_2_COORDINATES = new int[3];
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
							TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
							if (propertySplit.length == 4)
							{
								TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[3] = Integer.parseInt(propertySplit[3]);
							}
							TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS = Integer.parseInt(tvtround.getProperty("TvTRoundEventMinPlayersInTeams", "1"));
							TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS = Integer.parseInt(tvtround.getProperty("TvTRoundEventMaxPlayersInTeams", "20"));
							TVT_ROUND_EVENT_MIN_LVL = (byte) Integer.parseInt(tvtround.getProperty("TvTRoundEventMinPlayerLevel", "1"));
							TVT_ROUND_EVENT_MAX_LVL = (byte) Integer.parseInt(tvtround.getProperty("TvTRoundEventMaxPlayerLevel", "80"));
							TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY = Integer.parseInt(tvtround.getProperty("TvTRoundEventStartRespawnLeaveTeleportDelay", "10"));
							TVT_ROUND_EVENT_EFFECTS_REMOVAL = Integer.parseInt(tvtround.getProperty("TvTRoundEventEffectsRemoval", "0"));
							TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP = Integer.parseInt(tvtround.getProperty("TvTRoundEventMaxParticipantsPerIP", "0"));
							TVT_ROUND_ALLOW_VOICED_COMMAND = Boolean.parseBoolean(tvtround.getProperty("TvTRoundAllowVoicedInfoCommand", "false"));
							TVT_ROUND_EVENT_TEAM_1_NAME = tvtround.getProperty("TvTRoundEventTeam1Name", "Team1");
							propertySplit = tvtround.getProperty("TvTRoundEventTeam1Coordinates", "0,0,0").split(",");
							if (propertySplit.length < 3)
							{
								TVT_ROUND_EVENT_ENABLED = false;
								_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventTeam1Coordinates");
							}
							else
							{
								TVT_ROUND_EVENT_TEAM_1_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
								TVT_ROUND_EVENT_TEAM_1_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
								TVT_ROUND_EVENT_TEAM_1_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
								TVT_ROUND_EVENT_TEAM_2_NAME = tvtround.getProperty("TvTRoundEventTeam2Name", "Team2");
								propertySplit = tvtround.getProperty("TvTRoundEventTeam2Coordinates", "0,0,0").split(",");
								if (propertySplit.length < 3)
								{
									TVT_ROUND_EVENT_ENABLED = false;
									_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventTeam2Coordinates");
								}
								else
								{
									TVT_ROUND_EVENT_TEAM_2_COORDINATES[0] = Integer.parseInt(propertySplit[0]);
									TVT_ROUND_EVENT_TEAM_2_COORDINATES[1] = Integer.parseInt(propertySplit[1]);
									TVT_ROUND_EVENT_TEAM_2_COORDINATES[2] = Integer.parseInt(propertySplit[2]);
									propertySplit = tvtround.getProperty("TvTRoundEventParticipationFee", "0,0").split(",");
									try
									{
										TVT_ROUND_EVENT_PARTICIPATION_FEE[0] = Integer.parseInt(propertySplit[0]);
										TVT_ROUND_EVENT_PARTICIPATION_FEE[1] = Integer.parseInt(propertySplit[1]);
									}
									catch (NumberFormatException nfe)
									{
										if (propertySplit.length > 0)
										{
											_log.warning("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventParticipationFee");
										}
									}
									propertySplit = tvtround.getProperty("TvTRoundEventReward", "57,100000").split(";");
									for (String reward : propertySplit)
									{
										String[] rewardSplit = reward.split(",");
										if (rewardSplit.length != 2)
										{
											_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventReward \"", reward, "\""));
										}
										else
										{
											try
											{
												TVT_ROUND_EVENT_REWARDS.add(new int[]
												{
													Integer.parseInt(rewardSplit[0]),
													Integer.parseInt(rewardSplit[1])
												});
											}
											catch (NumberFormatException nfe)
											{
												if (!reward.isEmpty())
												{
													_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventReward \"", reward, "\""));
												}
											}
										}
									}
									
									TVT_ROUND_EVENT_TARGET_TEAM_MEMBERS_ALLOWED = Boolean.parseBoolean(tvtround.getProperty("TvTRoundEventTargetTeamMembersAllowed", "true"));
									TVT_ROUND_EVENT_SCROLL_ALLOWED = Boolean.parseBoolean(tvtround.getProperty("TvTRoundEventScrollsAllowed", "false"));
									TVT_ROUND_EVENT_POTIONS_ALLOWED = Boolean.parseBoolean(tvtround.getProperty("TvTRoundEventPotionsAllowed", "false"));
									TVT_ROUND_EVENT_SUMMON_BY_ITEM_ALLOWED = Boolean.parseBoolean(tvtround.getProperty("TvTRoundEventSummonByItemAllowed", "false"));
									TVT_ROUND_GIVE_POINT_TEAM_TIE = Boolean.parseBoolean(tvtround.getProperty("TvTRoundGivePointTeamTie", "false"));
									TVT_ROUND_REWARD_TEAM_TIE = Boolean.parseBoolean(tvtround.getProperty("TvTRoundRewardTeamTie", "false"));
									TVT_ROUND_EVENT_REWARD_ON_SECOND_FIGHT_END = Boolean.parseBoolean(tvtround.getProperty("TvTRoundEventRewardOnSecondFightEnd", "false"));
									propertySplit = tvtround.getProperty("TvTRoundDoorsToOpen", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_ROUND_DOORS_IDS_TO_OPEN.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
											{
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundDoorsToOpen \"", door, "\""));
											}
										}
									}
									
									propertySplit = tvtround.getProperty("TvTRoundDoorsToClose", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_ROUND_DOORS_IDS_TO_CLOSE.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
											{
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundDoorsToClose \"", door, "\""));
											}
										}
									}
									
									propertySplit = tvtround.getProperty("TvTRoundAnteroomDoorsToOpenClose", "").split(";");
									for (String door : propertySplit)
									{
										try
										{
											TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE.add(Integer.parseInt(door));
										}
										catch (NumberFormatException nfe)
										{
											if (!door.isEmpty())
											{
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundAnteroomDoorsToOpenClose \"", door, "\""));
											}
										}
									}
									
									propertySplit = tvtround.getProperty("TvTRoundEventFighterBuffs", "").split(";");
									if (!propertySplit[0].isEmpty())
									{
										TVT_ROUND_EVENT_FIGHTER_BUFFS = new HashMap<>(propertySplit.length);
										for (String skill : propertySplit)
										{
											String[] skillSplit = skill.split(",");
											if (skillSplit.length != 2)
											{
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventFighterBuffs \"", skill, "\""));
											}
											else
											{
												try
												{
													TVT_ROUND_EVENT_FIGHTER_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
												}
												catch (NumberFormatException nfe)
												{
													if (!skill.isEmpty())
													{
														_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventFighterBuffs \"", skill, "\""));
													}
												}
											}
										}
									}
									
									propertySplit = tvtround.getProperty("TvTRoundEventMageBuffs", "").split(";");
									if (!propertySplit[0].isEmpty())
									{
										TVT_ROUND_EVENT_MAGE_BUFFS = new HashMap<>(propertySplit.length);
										for (String skill : propertySplit)
										{
											String[] skillSplit = skill.split(",");
											if (skillSplit.length != 2)
											{
												_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventMageBuffs \"", skill, "\""));
											}
											else
											{
												try
												{
													TVT_ROUND_EVENT_MAGE_BUFFS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
												}
												catch (NumberFormatException nfe)
												{
													if (!skill.isEmpty())
													{
														_log.warning(StringUtil.concat("TvTRoundEventEngine[Config.load()]: invalid config property -> TvTRoundEventMageBuffs \"", skill, "\""));
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
				}
				try
				{
					L2Properties itemmallSettings = new L2Properties();
					is = new FileInputStream(new File(ITEM_MALL_CONFIG_FILE));
					itemmallSettings.load(is);
					
					GAME_POINT_ITEM_ID = Integer.parseInt(itemmallSettings.getProperty("GamePointItemId", "-1"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + ITEM_MALL_CONFIG_FILE + " File.");
				}
				try
				{
					L2Properties customEventsSettings = new L2Properties();
					is = new FileInputStream(new File(CUSTOM_EVENTS_FILE));
					customEventsSettings.load(is);
					
					EVENT_TIME_ELPIES = Integer.parseInt(customEventsSettings.getProperty("EventTimeElpies", "2"));
					EVENT_NUMBER_OF_SPAWNED_ELPIES = Integer.parseInt(customEventsSettings.getProperty("EventNumberOfSpawnedElpies", "100"));
					EVENT_TIME_RABBITS = Integer.parseInt(customEventsSettings.getProperty("EventTimeRabbits", "10"));
					EVENT_NUMBER_OF_SPAWNED_CHESTS = Integer.parseInt(customEventsSettings.getProperty("EventNumberOfSpawnedChest", "100"));
					EVENT_REG_TIME_RACE = Integer.parseInt(customEventsSettings.getProperty("EventRegTimeRace", "5"));
					EVENT_RUNNING_TIME_RACE = Integer.parseInt(customEventsSettings.getProperty("EventRunningTimeRace", "10"));
					MEDAL_COUNT1 = Integer.parseInt(customEventsSettings.getProperty("MedalCount1", "1"));
					MEDAL_COUNT2 = Integer.parseInt(customEventsSettings.getProperty("MedalCount2", "1"));
					MEDAL_CHANCE1 = Integer.parseInt(customEventsSettings.getProperty("MedalChance1", "100"));
					MEDAL_CHANCE2 = Integer.parseInt(customEventsSettings.getProperty("MedalChance2", "50"));
					NECTAR_COUNT = Integer.parseInt(customEventsSettings.getProperty("NectarCount", "1"));
					NECTAR_CHANCE = Integer.parseInt(customEventsSettings.getProperty("NectarChance", "100"));
					SCROLL_COUNT = Integer.parseInt(customEventsSettings.getProperty("ScrollCount", "1"));
					SCROLL_CHANCE = Integer.parseInt(customEventsSettings.getProperty("ScrollChance", "10"));
					DARK_CHOCOLATE_COUNT = Integer.parseInt(customEventsSettings.getProperty("DarkChocolateCount", "1"));
					WHITE_CHOCOLATE_COUNT = Integer.parseInt(customEventsSettings.getProperty("WhiteChocolateCount", "1"));
					FRESH_CREAM_COUNT = Integer.parseInt(customEventsSettings.getProperty("FreshCreamCount", "1"));
					DARK_CHOCOLATE_CHANCE = Integer.parseInt(customEventsSettings.getProperty("DarkChocolateChance", "20"));
					WHITE_CHOCOLATE_CHANCE = Integer.parseInt(customEventsSettings.getProperty("WhiteChocolateChance", "20"));
					FRESH_CREAM_CHANCE = Integer.parseInt(customEventsSettings.getProperty("FreshCreamChance", "5"));
					LETTER_COUNT = Integer.parseInt(customEventsSettings.getProperty("LetterCount", "1"));
					LETTER_CHANCE = Integer.parseInt(customEventsSettings.getProperty("LetterChance", "70"));
					STAR_COUNT = Integer.parseInt(customEventsSettings.getProperty("StarCount", "1"));
					BEAD_COUNT = Integer.parseInt(customEventsSettings.getProperty("BeadCount", "1"));
					FIR_COUNT = Integer.parseInt(customEventsSettings.getProperty("FirCount", "1"));
					FLOWER_COUNT = Integer.parseInt(customEventsSettings.getProperty("FlowerCount", "1"));
					STAR_CHANCE = Integer.parseInt(customEventsSettings.getProperty("StarChance", "20"));
					BEAD_CHANCE = Integer.parseInt(customEventsSettings.getProperty("BeadChance", "20"));
					FIR_CHANCE = Integer.parseInt(customEventsSettings.getProperty("FirChance", "50"));
					FLOWER_CHANCE = Integer.parseInt(customEventsSettings.getProperty("FlowerChance", "5"));
					COFFER_PRICE_ID = Integer.parseInt(customEventsSettings.getProperty("CofferPriceId", "57"));
					COFFER_PRICE_AMOUNT = Integer.parseInt(customEventsSettings.getProperty("CofferPriceCount", "50000"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + CUSTOM_EVENTS_FILE + " File.");
				}
				try
				{
					L2Properties LHSettings = new L2Properties();
					is = new FileInputStream(new File(LASTHERO_FILE));
					LHSettings.load(is);
					
					LH_ENABLED = Boolean.parseBoolean(LHSettings.getProperty("LHEventEnable", "false"));
					LH_REGNPC_ID = Integer.parseInt(LHSettings.getProperty("LHEventRegNpc", "77777"));
					
					String[] propertySplit = LHSettings.getProperty("LHEventParticipationNpcCoordinates", "82698,148638,-3468").split(",");
					if (propertySplit.length == 3)
					{
						LH_REGNPC_COORDINATE = new int[3];
						LH_REGNPC_COORDINATE[0] = Integer.parseInt(propertySplit[0]);
						LH_REGNPC_COORDINATE[1] = Integer.parseInt(propertySplit[1]);
						LH_REGNPC_COORDINATE[2] = Integer.parseInt(propertySplit[2]);
					}
					else
					{
						LH_ENABLED = false;
						_log.warning("LHEventEngine[Config.load()]: invalid config property -> LHEventParticipationNpcCoordinates");
					}
					LH_EVENT_INTERVAL = LHSettings.getProperty("LHEventInterval", "10:00").split(",");
					LH_MIN_LEVEL = Integer.parseInt(LHSettings.getProperty("LHEventMinLevel", "76"));
					LH_TIME_FOR_REGISTRATION_IN_MINUTES = Integer.parseInt(LHSettings.getProperty("LHEventTimeForRegistrationInMinutes", "5"));
					LH_ANNOUNCE_REGISTRATION_INTERVAL_IN_SECONDS = Integer.parseInt(LHSettings.getProperty("LHEventAnnounceRegistrationIntervalInSeconds", "30"));
					LH_TIME_TO_WAIT_BATTLE_IN_SECONDS = Integer.parseInt(LHSettings.getProperty("LHEventTimeToWaitBattleInSeconds", "30"));
					LH_MAX_PARTICIPANTS_PER_IP = Integer.parseInt(LHSettings.getProperty("LHEventMaxParticipantsPerIP", "1"));
					LH_BATTLE_DURATION_IN_MINUTES = Integer.parseInt(LHSettings.getProperty("LHEventBattleDurationInMinutes", "10"));
					LH_MIN_PARTICIPATE_COUNT = Integer.parseInt(LHSettings.getProperty("LHEventMinParticipateCount", "2"));
					LH_MAX_PATRICIPATE_COUNT = Integer.parseInt(LHSettings.getProperty("LHEventMaxParticipateCount", "100"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + LASTHERO_FILE + " File.");
				}
				try
				{
					L2Properties leprechaunEventSettings = new L2Properties();
					is = new FileInputStream(new File(LEPRECHAUN_FILE));
					leprechaunEventSettings.load(is);
					
					ENABLED_LEPRECHAUN = Boolean.parseBoolean(leprechaunEventSettings.getProperty("EnabledLeprechaun", "False"));
					if (ENABLED_LEPRECHAUN)
					{
						LEPRECHAUN_ID = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunId", "7805"));
						LEPRECHAUN_FIRST_SPAWN_DELAY = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunFirstSpawnDelay", "5"));
						LEPRECHAUN_RESPAWN_INTERVAL = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunRespawnInterval", "60"));
						LEPRECHAUN_SPAWN_TIME = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunSpawnTime", "30"));
						LEPRECHAUN_ANNOUNCE_INTERVAL = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunAnnounceInterval", "5"));
						SHOW_NICK = Boolean.parseBoolean(leprechaunEventSettings.getProperty("ShowNick", "True"));
						SHOW_REGION = Boolean.parseBoolean(leprechaunEventSettings.getProperty("ShowRegion", "True"));
					}
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + LEPRECHAUN_FILE + " File.");
				}
				try
				{
					L2Properties aioSettings = new L2Properties();
					is = new FileInputStream(new File(AIO_CONFIG_FILE));
					aioSettings.load(is);
					
					ALLOW_AIO_ITEM_COMMAND = Boolean.parseBoolean(aioSettings.getProperty("AllowAioItemVoiceCommand", "False"));
					ENABLE_AIO_NPCS = Boolean.parseBoolean(aioSettings.getProperty("EnableAioNpcs", "False"));
					AIO_ITEM_ID = Integer.parseInt(aioSettings.getProperty("AioItemId", "41005"));
					AIO_TPCOIN = Integer.parseInt(aioSettings.getProperty("TeleportCoin", "57"));
					AIO_PRICE_PERTP = Integer.parseInt(aioSettings.getProperty("TeleportPrice", "100"));
					AIO_ENABLE_TP_DELAY = Boolean.parseBoolean(aioSettings.getProperty("AioEnableTPDelay", "False"));
					AIO_DELAY = Double.parseDouble(aioSettings.getProperty("AioDelay", "0.75"));
					AIO_DELAY_SENDMESSAGE = Boolean.parseBoolean(aioSettings.getProperty("AioDelaySendMessage", "False"));
					CHANGE_GENDER_DONATE_COIN = Integer.parseInt(aioSettings.getProperty("ChangeGenderDonateCoin", "40000"));
					CHANGE_GENDER_DONATE_PRICE = Integer.parseInt(aioSettings.getProperty("ChangeGenderDonatePrice", "5"));
					CHANGE_GENDER_NORMAL_COIN = Integer.parseInt(aioSettings.getProperty("ChangeGenderNormalCoin", "40002"));
					CHANGE_GENDER_NORMAL_PRICE = Integer.parseInt(aioSettings.getProperty("ChangeGenderNormalPrice", "60000"));
					CHANGE_NAME_COIN = Integer.parseInt(aioSettings.getProperty("ChangeNameCoin", "40002"));
					CHANGE_NAME_PRICE = Integer.parseInt(aioSettings.getProperty("ChangeNamePrice", "30000"));
					CHANGE_CNAME_COIN = Integer.parseInt(aioSettings.getProperty("ChangeClanNameCoin", "40000"));
					CHANGE_CNAME_PRICE = Integer.parseInt(aioSettings.getProperty("ChangeClanNamePrice", "10"));
					AUGMENT_COIN = Integer.parseInt(aioSettings.getProperty("AugmentCoin", "40000"));
					AUGMENT_PRICE = Integer.parseInt(aioSettings.getProperty("AugmentPrice", "5"));
					GET_FULL_CLAN_COIN = Integer.parseInt(aioSettings.getProperty("GetFullClanCoin", "40000"));
					GET_FULL_CLAN_PRICE = Integer.parseInt(aioSettings.getProperty("GetFullClanPrice", "30"));
					AIO_EXCHANGE_ID = Integer.parseInt(aioSettings.getProperty("AioExchangeId", "40002"));
					AIO_EXCHANGE_PRICE = Integer.parseInt(aioSettings.getProperty("AioExchangePrice", "100000000"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + AIO_CONFIG_FILE + " File.");
				}
				try
				{
					L2Properties MREventFiles = new L2Properties();
					is = new FileInputStream(new File(MR_FILES));
					MREventFiles.load(is);
					
					MR_ENABLED = Boolean.parseBoolean(MREventFiles.getProperty("EnableMonsterRush", "False"));
					MR_EVENT_INTERVAL = MREventFiles.getProperty("MREventInterval", "18:00").split(",");
					MR_PARTICIPATION_TIME = Integer.parseInt(MREventFiles.getProperty("MREventParticipationTime", "3600"));
					MR_RUNNING_TIME = Integer.parseInt(MREventFiles.getProperty("MREventRunningTime", "1800"));
					MRUSH_REWARD_AMOUNT = Integer.parseInt(MREventFiles.getProperty("MrEventRewardAmount", "1"));
					MRUSH_REWARD_ITEM = Integer.parseInt(MREventFiles.getProperty("MrEventRewardItem", "3481"));
					MRUSH_MIN_PLAYERS = Integer.parseInt(MREventFiles.getProperty("MrMinEventPlayers", "15"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + MR_FILES + " File.");
				}
				try
				{
					L2Properties antifeedoly = new L2Properties();
					is = new FileInputStream(new File(OLY_ANTI_FEED_FILE));
					antifeedoly.load(is);
					
					ENABLE_OLY_FEED = Boolean.parseBoolean(antifeedoly.getProperty("OlympiadAntiFeedEnable", "False"));
					OLY_ANTI_FEED_WEAPON_RIGHT = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedRightWeapon", "0"));
					OLY_ANTI_FEED_WEAPON_LEFT = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedLeftWeapon", "0"));
					OLY_ANTI_FEED_GLOVES = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedGloves", "0"));
					OLY_ANTI_FEED_CHEST = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedChest", "0"));
					OLY_ANTI_FEED_LEGS = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedLegs", "0"));
					OLY_ANTI_FEED_FEET = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedFeet", "0"));
					OLY_ANTI_FEED_CLOAK = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedCloak", "0"));
					OLY_ANTI_FEED_RIGH_HAND_ARMOR = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedRightArmor", "0"));
					OLY_ANTI_FEED_HAIR_MISC_1 = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedHair1", "0"));
					OLY_ANTI_FEED_HAIR_MISC_2 = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedHair2", "0"));
					OLY_ANTI_FEED_RACE = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedRace", "0"));
					OLY_ANTI_FEED_GENDER = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedGender", "0"));
					OLY_ANTI_FEED_CLASS_RADIUS = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedClassRadius", "0"));
					OLY_ANTI_FEED_CLASS_HEIGHT = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedClassHeight", "0"));
					OLY_ANTI_FEED_PLAYER_HAVE_RECS = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedHaveRecs", "0"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + OLY_ANTI_FEED_FILE + " File.");
				}
				try
				{
					L2Properties npc_buffer = new L2Properties();
					is = new FileInputStream(new File(NPC_BUFFER));
					npc_buffer.load(is);
					
					NpcBuffer_SmartWindow = Boolean.parseBoolean(npc_buffer.getProperty("EnableSmartWindow", "False"));
					NpcBuffer_ID = Integer.parseInt(npc_buffer.getProperty("BufferId", "65535"));
					VIP_ONLY = Boolean.parseBoolean(npc_buffer.getProperty("OnlyVip", "False"));
					NpcBuffer_VIP = Boolean.parseBoolean(npc_buffer.getProperty("EnableVIP", "False"));
					NpcBuffer_EnableBuff = Boolean.parseBoolean(npc_buffer.getProperty("EnableBuffSection", "True"));
					NpcBuffer_EnableScheme = Boolean.parseBoolean(npc_buffer.getProperty("EnableScheme", "True"));
					NpcBuffer_EnableHeal = Boolean.parseBoolean(npc_buffer.getProperty("EnableHeal", "True"));
					NpcBuffer_EnableBuffs = Boolean.parseBoolean(npc_buffer.getProperty("EnableBuffs", "True"));
					NpcBuffer_EnableResist = Boolean.parseBoolean(npc_buffer.getProperty("EnableResist", "True"));
					NpcBuffer_EnableSong = Boolean.parseBoolean(npc_buffer.getProperty("EnableSongs", "True"));
					NpcBuffer_EnableDance = Boolean.parseBoolean(npc_buffer.getProperty("EnableDances", "True"));
					NpcBuffer_EnableChant = Boolean.parseBoolean(npc_buffer.getProperty("EnableChants", "True"));
					NpcBuffer_EnableOther = Boolean.parseBoolean(npc_buffer.getProperty("EnableOther", "True"));
					NpcBuffer_EnableSpecial = Boolean.parseBoolean(npc_buffer.getProperty("EnableSpecial", "True"));
					NpcBuffer_EnableCubic = Boolean.parseBoolean(npc_buffer.getProperty("EnableCubic", "True"));
					NpcBuffer_EnableCancel = Boolean.parseBoolean(npc_buffer.getProperty("EnableRemoveBuffs", "True"));
					NpcBuffer_EnableBuffSet = Boolean.parseBoolean(npc_buffer.getProperty("EnableBuffSet", "True"));
					NpcBuffer_EnableBuffPK = Boolean.parseBoolean(npc_buffer.getProperty("EnableBuffForPK", "False"));
					NpcBuffer_EnableFreeBuffs = Boolean.parseBoolean(npc_buffer.getProperty("EnableFreeBuffs", "True"));
					NpcBuffer_EnableTimeOut = Boolean.parseBoolean(npc_buffer.getProperty("EnableTimeOut", "True"));
					NpcBuffer_TimeOutTime = Integer.parseInt(npc_buffer.getProperty("TimeoutTime", "10"));
					NpcBuffer_MinLevel = Integer.parseInt(npc_buffer.getProperty("MinimumLevel", "20"));
					NpcBuffer_PriceCancel = Integer.parseInt(npc_buffer.getProperty("RemoveBuffsPrice", "100000"));
					NpcBuffer_PriceHeal = Integer.parseInt(npc_buffer.getProperty("HealPrice", "100000"));
					NpcBuffer_PriceBuffs = Integer.parseInt(npc_buffer.getProperty("BuffsPrice", "100000"));
					NpcBuffer_PriceResist = Integer.parseInt(npc_buffer.getProperty("ResistPrice", "100000"));
					NpcBuffer_PriceSong = Integer.parseInt(npc_buffer.getProperty("SongPrice", "100000"));
					NpcBuffer_PriceDance = Integer.parseInt(npc_buffer.getProperty("DancePrice", "100000"));
					NpcBuffer_PriceChant = Integer.parseInt(npc_buffer.getProperty("ChantsPrice", "100000"));
					NpcBuffer_PriceOther = Integer.parseInt(npc_buffer.getProperty("OtherPrice", "100000"));
					NpcBuffer_PriceSpecial = Integer.parseInt(npc_buffer.getProperty("SpecialPrice", "100000"));
					NpcBuffer_PriceCubic = Integer.parseInt(npc_buffer.getProperty("CubicPrice", "100000"));
					NpcBuffer_PriceSet = Integer.parseInt(npc_buffer.getProperty("SetPrice", "10000000"));
					NpcBuffer_PriceScheme = Integer.parseInt(npc_buffer.getProperty("SchemePrice", "10000000"));
					NpcBuffer_MaxScheme = Integer.parseInt(npc_buffer.getProperty("MaxScheme", "4"));
					NpcBuffer_consumableID = Integer.parseInt(npc_buffer.getProperty("ConsumableID", "57"));
					
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + NPC_BUFFER + " File.");
				}
				try
				{
					L2Properties geoSettings = new L2Properties();
					is = new FileInputStream(new File(GEO_CONFIG_FILE));
					geoSettings.load(is);
					
					GEODATA = Boolean.parseBoolean(geoSettings.getProperty("AllowGeoData", "False"));
					String correctZ = GEODATA ? geoSettings.getProperty("GeoCorrectSpawnZ", "ALL") : "NONE";
					GEO_CORRECT_Z = CorrectSpawnsZ.valueOf(correctZ.toUpperCase());
					ACCEPT_GEOEDITOR_CONN = Boolean.parseBoolean(geoSettings.getProperty("AcceptGeoeditorConnect", "False"));
					CLIENT_SHIFTZ = Integer.parseInt(geoSettings.getProperty("GeoClientShiftZ", "16"));
					PATH_CLEAN = Boolean.parseBoolean(geoSettings.getProperty("PathFindClean", "True"));
					ALLOW_FALL_FROM_WALLS = Boolean.parseBoolean(geoSettings.getProperty("AllowFallFromWalls", "False"));
					PATHFIND_DIAGONAL = Boolean.parseBoolean(geoSettings.getProperty("PathFindDiagonal", "False"));
					MAX_Z_DIFF = Integer.parseInt(geoSettings.getProperty("MaxZDiff", "64"));
					MIN_LAYER_HEIGHT = Integer.parseInt(geoSettings.getProperty("MinLayerHeight", "64"));
					PATHFIND_MAX_Z_DIFF = Integer.parseInt(geoSettings.getProperty("PathFindMaxZDiff", "32"));
					TRICK_HEIGHT = Integer.parseInt(geoSettings.getProperty("MinTrickHeight", "16"));
					
					PathFindBuffers.initBuffers("8x100;8x128;8x192;4x256;2x320;2x384;1x500");
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + GEO_CONFIG_FILE + " File.");
				}
				try
				{
					L2Properties antiBotSettings = new L2Properties();
					is = new FileInputStream(new File(ANTIBOT_CONFIG));
					antiBotSettings.load(is);
					
					ANTIBOT_START_CAPTCHA = Integer.parseInt(antiBotSettings.getProperty("EnableAntiBotSystem", "0"));
					ANTIBOT_GET_KILLS = Integer.parseInt(antiBotSettings.getProperty("AntiBotNextCheckOn", "100"));
					ANTIBOT_GET_TIME_TO_FILL = Integer.parseInt(antiBotSettings.getProperty("AntiBotTimeToFill", "60000"));
					ANTIBOT_PUNISH = Integer.parseInt(antiBotSettings.getProperty("AntiBotPunish", "4"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + ANTIBOT_CONFIG + " File.");
				}
				try
				{
					L2Properties customRatesSettings = new L2Properties();
					is = new FileInputStream(new File(CUSTOM_RATES_CONFIG));
					customRatesSettings.load(is);
					
					ALLOW_CUSTOM_RATES = Boolean.parseBoolean(customRatesSettings.getProperty("AllowCustomRates", "False"));
					MONDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomMondayExp", "2"));
					MONDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomMondaySp", "2"));
					TUESDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomTuesdayExp", "2"));
					TUESDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomTuesdaySp", "2"));
					WEDNESDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomWednesdayExp", "2"));
					WEDNESDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomWednesdaySp", "2"));
					THURSDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomThursdayExp", "2"));
					THURSDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomThursdaySp", "2"));
					FRIDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomFridayExp", "2"));
					FRIDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomFridaySp", "2"));
					SATURDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomSaturdayExp", "2"));
					SATURDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomSaturdaySp", "2"));
					SUNDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomSundayExp", "2"));
					SUNDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomSundaySp", "2"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + CUSTOM_RATES_CONFIG + " File.");
				}
				try
				{
					L2Properties phantomSettings = new L2Properties();
					is = new FileInputStream(new File(PHANTOMS_CONFIG_FILE));
					phantomSettings.load(is);
					
					ALLOW_PHANTOM_PLAYERS = Boolean.parseBoolean(phantomSettings.getProperty("AllowPhantomPlayers", "False"));
					ALLOW_PHANTOM_SETS = Boolean.parseBoolean(phantomSettings.getProperty("AllowPhantomSets", "False"));
					PHANTOM_PLAYERS_AKK = phantomSettings.getProperty("PhantomPlayerAccounts", "l2-dream.ru");
					PHANTOM_PLAYERS_COUNT_FIRST = Integer.parseInt(phantomSettings.getProperty("FirstCount", "50"));
					PHANTOM_PLAYERS_DELAY_FIRST = Integer.parseInt(phantomSettings.getProperty("FirstDelay", "5"));
					PHANTOM_PLAYERS_DESPAWN_FIRST = TimeUnit.MINUTES.toMillis(Integer.parseInt(phantomSettings.getProperty("FirstDespawn", "60")));
					PHANTOM_PLAYERS_DELAY_SPAWN_FIRST = (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(phantomSettings.getProperty("FirstDelaySpawn", "1")));
					PHANTOM_PLAYERS_DELAY_DESPAWN_FIRST = (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(phantomSettings.getProperty("FirstDelayDespawn", "20")));
					PHANTOM_PLAYERS_COUNT_NEXT = Integer.parseInt(phantomSettings.getProperty("NextCount", "50"));
					PHANTOM_PLAYERS_CP_REUSE_TIME = Integer.parseInt(phantomSettings.getProperty("CpReuseTime", "200"));
					PHANTOM_PLAYERS_DELAY_NEXT = TimeUnit.MINUTES.toMillis(Integer.parseInt(phantomSettings.getProperty("NextDelay", "15")));
					PHANTOM_PLAYERS_DESPAWN_NEXT = TimeUnit.MINUTES.toMillis(Integer.parseInt(phantomSettings.getProperty("NextDespawn", "90")));
					PHANTOM_PLAYERS_DELAY_SPAWN_NEXT = (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(phantomSettings.getProperty("NextDelaySpawn", "20")));
					PHANTOM_PLAYERS_DELAY_DESPAWN_NEXT = (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(phantomSettings.getProperty("NextDelayDespawn", "30")));
					PHANTOM_PLAYERS_ENCHANT_MAX = Integer.parseInt(phantomSettings.getProperty("PhantomEnchantMax", "14"));
					
					String[] nicks = phantomSettings.getProperty("PhantomNameColors", "FFFFFF,FFFFFF").split(",");
					for (String ncolor : nicks)
					{
						String nick = new TextBuilder(ncolor).reverse().toString();
						PHANTOM_PLAYERS_NAME_CLOLORS.add(Integer.decode("0x" + nick));
					}
					String[] titles = phantomSettings.getProperty("PhantomTitleColors", "FFFF77,FFFF77").split(",");
					for (String tcolor : titles)
					{
						String title = new TextBuilder(tcolor).reverse().toString();
						PHANTOM_PLAYERS_TITLE_CLOLORS.add(Integer.decode("0x" + title));
					}
					
					String[] baseClassId = phantomSettings.getProperty("PhantomBaseProffClassId", "0,10,18,25,31,38,44,49").split(",");
					PHANTOM_BASE_PROFF_CLASSID = new int[baseClassId.length];
					for (int i = 0; i < baseClassId.length; i++)
					{
						PHANTOM_BASE_PROFF_CLASSID[i] = Integer.parseInt(baseClassId[i]);
					}
					Arrays.sort(PHANTOM_BASE_PROFF_CLASSID);
					
					String[] firstClassId = phantomSettings.getProperty("PhantomFirstProffClassId", "1,4,7,11,15,19,22,26").split(",");
					PHANTOM_FIRST_PROFF_CLASSID = new int[firstClassId.length];
					for (int i = 0; i < firstClassId.length; i++)
					{
						PHANTOM_FIRST_PROFF_CLASSID[i] = Integer.parseInt(firstClassId[i]);
					}
					Arrays.sort(PHANTOM_FIRST_PROFF_CLASSID);
					
					String[] secondClassId = phantomSettings.getProperty("PhantomSecondProffClassId", "2,3,5,6,8,9,12,13").split(",");
					PHANTOM_SECOND_PROFF_CLASSID = new int[secondClassId.length];
					for (int i = 0; i < secondClassId.length; i++)
					{
						PHANTOM_SECOND_PROFF_CLASSID[i] = Integer.parseInt(secondClassId[i]);
					}
					Arrays.sort(PHANTOM_SECOND_PROFF_CLASSID);
					
					String[] thirdClassId = phantomSettings.getProperty("PhantomSecondProffClassId", "88,89,90,91,92,93,94,95").split(",");
					PHANTOM_THIRD_PROFF_CLASSID = new int[thirdClassId.length];
					for (int i = 0; i < thirdClassId.length; i++)
					{
						PHANTOM_THIRD_PROFF_CLASSID[i] = Integer.parseInt(thirdClassId[i]);
					}
					Arrays.sort(PHANTOM_THIRD_PROFF_CLASSID);
					
					ALLOW_PHANTOM_USE_HEAL_POTION = Boolean.parseBoolean(phantomSettings.getProperty("AllowPhantomUseHealPotion", "False"));
					ALLOW_PHANTOM_USE_CP_POTION = Boolean.parseBoolean(phantomSettings.getProperty("AllowPhantomUseCpPotion", "False"));
					PHANTOM_HEAL_REUSE_TIME = Integer.parseInt(phantomSettings.getProperty("HealReuseTime", "10000"));
					MAX_PHANTOM_COUNT = Integer.parseInt(phantomSettings.getProperty("TotalPhantomCount", "100"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + PHANTOMS_CONFIG_FILE + " File.");
				}
			}
			finally
			{
				try
				{
					is.close();
				}
				catch (Exception e)
				{
				}
			}
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			_log.info("loading login config");
			InputStream is = null;
			try
			{
				try
				{
					L2Properties serverSettings = new L2Properties();
					is = new FileInputStream(new File(LOGIN_CONFIGURATION_FILE));
					serverSettings.load(is);
					
					GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHostname", "*");
					GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9013"));
					
					LOGIN_BIND_ADDRESS = serverSettings.getProperty("LoginserverHostname", "*");
					PORT_LOGIN = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
					
					try
					{
						DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
					}
					catch (IOException e)
					{
						_log.log(Level.WARNING, "Error setting datapack root!", e);
						DATAPACK_ROOT = new File(".");
					}
					
					DEBUG = Boolean.parseBoolean(serverSettings.getProperty("Debug", "false"));
					
					ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer", "True"));
					
					LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "5"));
					LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "900"));
					
					LOG_LOGIN_CONTROLLER = Boolean.parseBoolean(serverSettings.getProperty("LogLoginController", "true"));
					
					LOGIN_SERVER_SCHEDULE_RESTART = Boolean.parseBoolean(serverSettings.getProperty("LoginRestartSchedule", "False"));
					LOGIN_SERVER_SCHEDULE_RESTART_TIME = Long.parseLong(serverSettings.getProperty("LoginRestartTime", "24"));
					
					DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
					DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jls");
					DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
					DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
					DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
					DATABASE_MAX_IDLE_TIME = Integer.parseInt(serverSettings.getProperty("MaximumDbIdleTime", "0"));
					
					SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "true"));
					
					AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "True"));
					
					FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection", "True"));
					FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit", "15"));
					NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime", "700"));
					FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime", "350"));
					MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP", "50"));
					CONNECTION_CLOSE_TIME = Long.parseLong(serverSettings.getProperty("ConnectionCloseTime", "60000"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + LOGIN_CONFIGURATION_FILE + " File.");
				}
				// MMO
				try
				{
					_log.info("Loading " + MMO_CONFIG_FILE.replaceAll("./config/main/", ""));
					L2Properties mmoSettings = new L2Properties();
					is = new FileInputStream(new File(MMO_CONFIG_FILE));
					mmoSettings.load(is);
					MMO_SELECTOR_SLEEP_TIME = Integer.parseInt(mmoSettings.getProperty("SleepTime", "20"));
					MMO_MAX_SEND_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxSendPerPass", "12"));
					MMO_MAX_READ_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxReadPerPass", "12"));
					MMO_HELPER_BUFFER_COUNT = Integer.parseInt(mmoSettings.getProperty("HelperBufferCount", "20"));
					MMO_TCP_NODELAY = Boolean.parseBoolean(mmoSettings.getProperty("TcpNoDelay", "False"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + MMO_CONFIG_FILE + " File.");
				}
				
				// Load Telnet L2Properties file (if exists)
				try
				{
					L2Properties telnetSettings = new L2Properties();
					is = new FileInputStream(new File(TELNET_FILE));
					telnetSettings.load(is);
					
					IS_TELNET_ENABLED = Boolean.parseBoolean(telnetSettings.getProperty("EnableTelnet", "false"));
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + TELNET_FILE + " File.");
				}
				
				// Email
				try
				{
					L2Properties emailSettings = new L2Properties();
					is = new FileInputStream(new File(EMAIL_CONFIG_FILE));
					emailSettings.load(is);
					
					EMAIL_SERVERINFO_NAME = emailSettings.getProperty("ServerInfoName", "Unconfigured L2J Server");
					EMAIL_SERVERINFO_ADDRESS = emailSettings.getProperty("ServerInfoAddress", "info@myl2jserver.com");
					
					EMAIL_SYS_ENABLED = Boolean.parseBoolean(emailSettings.getProperty("EmailSystemEnabled", "false"));
					EMAIL_SYS_HOST = emailSettings.getProperty("SmtpServerHost", "smtp.gmail.com");
					EMAIL_SYS_PORT = Integer.parseInt(emailSettings.getProperty("SmtpServerPort", "465"));
					EMAIL_SYS_SMTP_AUTH = Boolean.parseBoolean(emailSettings.getProperty("SmtpAuthRequired", "true"));
					EMAIL_SYS_FACTORY = emailSettings.getProperty("SmtpFactory", "javax.net.ssl.SSLSocketFactory");
					EMAIL_SYS_FACTORY_CALLBACK = Boolean.parseBoolean(emailSettings.getProperty("SmtpFactoryCallback", "false"));
					EMAIL_SYS_USERNAME = emailSettings.getProperty("SmtpUsername", "user@gmail.com");
					EMAIL_SYS_PASSWORD = emailSettings.getProperty("SmtpPassword", "password");
					EMAIL_SYS_ADDRESS = emailSettings.getProperty("EmailSystemAddress", "noreply@myl2jserver.com");
					EMAIL_SYS_SELECTQUERY = emailSettings.getProperty("EmailDBSelectQuery", "SELECT value FROM account_data WHERE account_name=? AND var='email_addr'");
					EMAIL_SYS_DBFIELD = emailSettings.getProperty("EmailDBField", "value");
				}
				catch (Exception e)
				{
					_log.warning("Config: " + e.getMessage());
					throw new Error("Failed to Load " + EMAIL_CONFIG_FILE + " File.");
				}
			}
			finally
			{
				try
				{
					is.close();
				}
				catch (Exception e)
				{
				}
			}
		}
		else
		{
			_log.severe("Could not Load Config: server mode was not set");
		}
	}
	
	/**
	 * Set a new value to a game parameter from the admin console.
	 * @param pName (String) : name of the parameter to change
	 * @param pValue (String) : new value of the parameter
	 * @return boolean : true if modification has been made
	 */
	public static boolean setParameterValue(String pName, String pValue)
	{
		if (pName.equalsIgnoreCase("RateXp"))
		{
			RATE_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateSp"))
		{
			RATE_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RatePartyXp"))
		{
			RATE_PARTY_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RatePartySp"))
		{
			RATE_PARTY_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateConsumableCost"))
		{
			RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateExtractable"))
		{
			RATE_EXTRACTABLE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropItems"))
		{
			RATE_DROP_ITEMS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropAdena"))
		{
			RATE_DROP_ITEMS_ID.put(PcInventory.ADENA_ID, Float.parseFloat(pValue));
		}
		else if (pName.equalsIgnoreCase("RateRaidDropItems"))
		{
			RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropSpoil"))
		{
			RATE_DROP_SPOIL = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropManor"))
		{
			RATE_DROP_MANOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestDrop"))
		{
			RATE_QUEST_DROP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestReward"))
		{
			RATE_QUEST_REWARD = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestRewardXP"))
		{
			RATE_QUEST_REWARD_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestRewardSP"))
		{
			RATE_QUEST_REWARD_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestRewardAdena"))
		{
			RATE_QUEST_REWARD_ADENA = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("UseQuestRewardMultipliers"))
		{
			RATE_QUEST_REWARD_USE_MULTIPLIERS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestRewardPotion"))
		{
			RATE_QUEST_REWARD_POTION = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestRewardScroll"))
		{
			RATE_QUEST_REWARD_SCROLL = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestRewardRecipe"))
		{
			RATE_QUEST_REWARD_RECIPE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestRewardMaterial"))
		{
			RATE_QUEST_REWARD_MATERIAL = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateHellboundTrustIncrease"))
		{
			RATE_HB_TRUST_INCREASE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateHellboundTrustDecrease"))
		{
			RATE_HB_TRUST_DECREASE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateVitalityLevel1"))
		{
			RATE_VITALITY_LEVEL_1 = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateVitalityLevel2"))
		{
			RATE_VITALITY_LEVEL_2 = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateVitalityLevel3"))
		{
			RATE_VITALITY_LEVEL_3 = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateVitalityLevel4"))
		{
			RATE_VITALITY_LEVEL_4 = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateRecoveryPeaceZone"))
		{
			RATE_RECOVERY_VITALITY_PEACE_ZONE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateVitalityLost"))
		{
			RATE_VITALITY_LOST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateVitalityGain"))
		{
			RATE_VITALITY_GAIN = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateRecoveryOnReconnect"))
		{
			RATE_RECOVERY_ON_RECONNECT = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateKarmaExpLost"))
		{
			RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice"))
		{
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateCommonHerbs"))
		{
			RATE_DROP_COMMON_HERBS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateHpHerbs"))
		{
			RATE_DROP_HP_HERBS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateMpHerbs"))
		{
			RATE_DROP_MP_HERBS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateSpecialHerbs"))
		{
			RATE_DROP_SPECIAL_HERBS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateVitalityHerbs"))
		{
			RATE_DROP_VITALITY_HERBS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerDropLimit"))
		{
			PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDrop"))
		{
			PLAYER_RATE_DROP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropItem"))
		{
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropEquip"))
		{
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropEquipWeapon"))
		{
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PetXpRate"))
		{
			PET_XP_RATE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PetFoodRate"))
		{
			PET_FOOD_RATE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("SinEaterXpRate"))
		{
			SINEATER_XP_RATE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaDropLimit"))
		{
			KARMA_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDrop"))
		{
			KARMA_RATE_DROP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropItem"))
		{
			KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropEquip"))
		{
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropEquipWeapon"))
		{
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter"))
		{
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("DestroyPlayerDroppedItem"))
		{
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DestroyEquipableItem"))
		{
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("SaveDroppedItem"))
		{
			SAVE_DROPPED_ITEM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("EmptyDroppedItemTableAfterLoad"))
		{
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("SaveDroppedItemInterval"))
		{
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClearDroppedItemTable"))
		{
			CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("PreciseDropCalculation"))
		{
			PRECISE_DROP_CALCULATION = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MultipleItemDrop"))
		{
			MULTIPLE_ITEM_DROP = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DeleteCharAfterDays"))
		{
			DELETE_DAYS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueSize"))
		{
			CLIENT_PACKET_QUEUE_SIZE = Integer.parseInt(pValue);
			if (CLIENT_PACKET_QUEUE_SIZE == 0)
			{
				CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 1;
			}
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxBurstSize"))
		{
			CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = Integer.parseInt(pValue);
			if (CLIENT_PACKET_QUEUE_MAX_BURST_SIZE == 0)
			{
				CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS;
			}
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxPacketsPerSecond"))
		{
			CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMeasureInterval"))
		{
			CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxAveragePacketsPerSecond"))
		{
			CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxFloodsPerMin"))
		{
			CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxOverflowsPerMin"))
		{
			CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxUnderflowsPerMin"))
		{
			CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClientPacketQueueMaxUnknownPerMin"))
		{
			CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowDiscardItem"))
		{
			ALLOW_DISCARDITEM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowRefund"))
		{
			ALLOW_REFUND = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWarehouse"))
		{
			ALLOW_WAREHOUSE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWear"))
		{
			ALLOW_WEAR = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WearDelay"))
		{
			WEAR_DELAY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WearPrice"))
		{
			WEAR_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWater"))
		{
			ALLOW_WATER = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowRentPet"))
		{
			ALLOW_RENTPET = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("BoatBroadcastRadius"))
		{
			BOAT_BROADCAST_RADIUS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowCursedWeapons"))
		{
			ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowManor"))
		{
			ALLOW_MANOR = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowPetWalkers"))
		{
			ALLOW_PET_WALKERS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("BypassValidation"))
		{
			BYPASS_VALIDATION = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CommunityType"))
		{
			COMMUNITY_TYPE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BBSShowPlayerList"))
		{
			BBS_SHOW_PLAYERLIST = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("BBSDefault"))
		{
			BBS_DEFAULT = pValue;
		}
		else if (pName.equalsIgnoreCase("ShowLevelOnCommunityBoard"))
		{
			SHOW_LEVEL_COMMUNITYBOARD = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("ShowStatusOnCommunityBoard"))
		{
			SHOW_STATUS_COMMUNITYBOARD = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("NamePageSizeOnCommunityBoard"))
		{
			NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("NamePerRowOnCommunityBoard"))
		{
			NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ShowServerNews"))
		{
			SERVER_NEWS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("ShowNpcLevel"))
		{
			SHOW_NPC_LVL = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("ShowCrestWithoutQuest"))
		{
			SHOW_CREST_WITHOUT_QUEST = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("ForceInventoryUpdate"))
		{
			FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData"))
		{
			AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumOnlineUsers"))
		{
			MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PeaceZoneMode"))
		{
			PEACE_ZONE_MODE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CheckKnownList"))
		{
			CHECK_KNOWN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxDriftRange"))
		{
			MAX_DRIFT_RANGE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("UseDeepBlueDropRules"))
		{
			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("UseDeepBlueDropRulesRaid"))
		{
			DEEPBLUE_DROP_RULES_RAID = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("GuardAttackAggroMob"))
		{
			GUARD_ATTACK_AGGRO_MOB = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CancelLesserEffect"))
		{
			EFFECT_CANCELING = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForNoDwarf"))
		{
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForDwarf"))
		{
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForGMPlayer"))
		{
			INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForQuestItems"))
		{
			INVENTORY_MAXIMUM_QUEST_ITEMS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf"))
		{
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf"))
		{
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan"))
		{
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceElementStone"))
		{
			ENCHANT_CHANCE_ELEMENT_STONE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceElementCrystal"))
		{
			ENCHANT_CHANCE_ELEMENT_CRYSTAL = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceElementJewel"))
		{
			ENCHANT_CHANCE_ELEMENT_JEWEL = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceElementEnergy"))
		{
			ENCHANT_CHANCE_ELEMENT_ENERGY = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationNGSkillChance"))
		{
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationNGGlowChance"))
		{
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationMidSkillChance"))
		{
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationMidGlowChance"))
		{
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationHighSkillChance"))
		{
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationHighGlowChance"))
		{
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationTopSkillChance"))
		{
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationTopGlowChance"))
		{
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AugmentationBaseStatChance"))
		{
			AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("HpRegenMultiplier"))
		{
			HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("MpRegenMultiplier"))
		{
			MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("CpRegenMultiplier"))
		{
			CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidHpRegenMultiplier"))
		{
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidMpRegenMultiplier"))
		{
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidPDefenceMultiplier"))
		{
			RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RaidMDefenceMultiplier"))
		{
			RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RaidPAttackMultiplier"))
		{
			RAID_PATTACK_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RaidMAttackMultiplier"))
		{
			RAID_MATTACK_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RaidMinionRespawnTime"))
		{
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidChaosTime"))
		{
			RAID_CHAOS_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("GrandChaosTime"))
		{
			GRAND_CHAOS_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MinionChaosTime"))
		{
			MINION_CHAOS_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingAdena"))
		{
			STARTING_ADENA = Long.parseLong(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingLevel"))
		{
			STARTING_LEVEL = Byte.parseByte(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingSP"))
		{
			STARTING_SP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("UnstuckInterval"))
		{
			UNSTUCK_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("TeleportWatchdogTimeout"))
		{
			TELEPORT_WATCHDOG_TIMEOUT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerSpawnProtection"))
		{
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerFakeDeathUpProtection"))
		{
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("RestorePlayerInstance"))
		{
			RESTORE_PLAYER_INSTANCE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowSummonToInstance"))
		{
			ALLOW_SUMMON_TO_INSTANCE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffMethod"))
		{
			PARTY_XP_CUTOFF_METHOD = pValue;
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffPercent"))
		{
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffLevel"))
		{
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreCP"))
		{
			RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreHP"))
		{
			RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreMP"))
		{
			RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreSellSlotsDwarf"))
		{
			MAX_PVTSTORESELL_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreSellSlotsOther"))
		{
			MAX_PVTSTORESELL_SLOTS_OTHER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreBuySlotsDwarf"))
		{
			MAX_PVTSTOREBUY_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreBuySlotsOther"))
		{
			MAX_PVTSTOREBUY_SLOTS_OTHER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StoreSkillCooltime"))
		{
			STORE_SKILL_COOLTIME = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("SubclassStoreSkillCooltime"))
		{
			SUBCLASS_STORE_SKILL_COOLTIME = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AnnounceMammonSpawn"))
		{
			ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameTiredness"))
		{
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("EnableFallingDamage"))
		{
			ENABLE_FALLING_DAMAGE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreation"))
		{
			ALT_GAME_CREATION = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationSpeed"))
		{
			ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationXpRate"))
		{
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationRareXpSpRate"))
		{
			ALT_GAME_CREATION_RARE_XPSP_RATE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationSpRate"))
		{
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltWeightLimit"))
		{
			ALT_WEIGHT_LIMIT = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltBlacksmithUseRecipes"))
		{
			ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameSkillLearn"))
		{
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("RemoveCastleCirclets"))
		{
			REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("ReputationScorePerKill"))
		{
			REPUTATION_SCORE_PER_KILL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCancelByHit"))
		{
			ALT_GAME_CANCEL_BOW = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
		}
		
		else if (pName.equalsIgnoreCase("AltShieldBlocks"))
		{
			ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPerfectShieldBlockRate"))
		{
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("Delevel"))
		{
			ALT_GAME_DELEVEL = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MagicFailures"))
		{
			ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltMobAgroInPeaceZone"))
		{
			ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameExponentXp"))
		{
			ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameExponentSp"))
		{
			ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowClassMasters"))
		{
			ALLOW_CLASS_MASTERS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowEntireTree"))
		{
			ALLOW_ENTIRE_TREE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AlternateClassMaster"))
		{
			ALTERNATE_CLASS_MASTER = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPartyRange"))
		{
			ALT_PARTY_RANGE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPartyRange2"))
		{
			ALT_PARTY_RANGE2 = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltLeavePartyLeader"))
		{
			ALT_LEAVE_PARTY_LEADER = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CraftingEnabled"))
		{
			IS_CRAFTING_ENABLED = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("CraftMasterwork"))
		{
			CRAFT_MASTERWORK = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("LifeCrystalNeeded"))
		{
			LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLoot"))
		{
			AUTO_LOOT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLootRaids"))
		{
			AUTO_LOOT_RAIDS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLootHerbs"))
		{
			AUTO_LOOT_HERBS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanBeKilledInPeaceZone"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanShop"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseGK"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTrade"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxPersonalFamePoints"))
		{
			MAX_PERSONAL_FAME_POINTS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("FortressZoneFameTaskFrequency"))
		{
			FORTRESS_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("FortressZoneFameAquirePoints"))
		{
			FORTRESS_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CastleZoneFameTaskFrequency"))
		{
			CASTLE_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CastleZoneFameAquirePoints"))
		{
			CASTLE_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltCastleForDawn"))
		{
			ALT_GAME_CASTLE_DAWN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltCastleForDusk"))
		{
			ALT_GAME_CASTLE_DUSK = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltRequireClanCastle"))
		{
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltFreeTeleporting"))
		{
			ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltSubClassWithoutQuests"))
		{
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltSubclassEverywhere"))
		{
			ALT_GAME_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AltMembersCanWithdrawFromClanWH"))
		{
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DwarfRecipeLimit"))
		{
			DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CommonRecipeLimit"))
		{
			COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionEnable"))
		{
			CHAMPION_ENABLE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionFrequency"))
		{
			CHAMPION_FREQUENCY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionMinLevel"))
		{
			CHAMP_MIN_LVL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionMaxLevel"))
		{
			CHAMP_MAX_LVL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionHp"))
		{
			CHAMPION_HP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionHpRegen"))
		{
			CHAMPION_HP_REGEN = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewards"))
		{
			CHAMPION_REWARDS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionAdenasRewards"))
		{
			CHAMPION_ADENAS_REWARDS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionAtk"))
		{
			CHAMPION_ATK = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionSpdAtk"))
		{
			CHAMPION_SPD_ATK = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardLowerLvlItemChance"))
		{
			CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardHigherLvlItemChance"))
		{
			CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItemID"))
		{
			CHAMPION_REWARD_ID = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItemQty"))
		{
			CHAMPION_REWARD_QTY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionEnableInInstances"))
		{
			CHAMPION_ENABLE_IN_INSTANCES = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWedding"))
		{
			ALLOW_WEDDING = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingPrice"))
		{
			WEDDING_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingPunishInfidelity"))
		{
			WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingTeleport"))
		{
			WEDDING_TELEPORT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingTeleportPrice"))
		{
			WEDDING_TELEPORT_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingTeleportDuration"))
		{
			WEDDING_TELEPORT_DURATION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingAllowSameSex"))
		{
			WEDDING_SAMESEX = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingFormalWear"))
		{
			WEDDING_FORMALWEAR = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("WeddingDivorceCosts"))
		{
			WEDDING_DIVORCE_COSTS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("TvTEventEnabled"))
		{
			TVT_EVENT_ENABLED = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("TvTEventInterval"))
		{
			TVT_EVENT_INTERVAL = pValue.split(",");
		}
		else if (pName.equalsIgnoreCase("TvTEventParticipationTime"))
		{
			TVT_EVENT_PARTICIPATION_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("TvTEventRunningTime"))
		{
			TVT_EVENT_RUNNING_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("TvTEventParticipationNpcId"))
		{
			TVT_EVENT_PARTICIPATION_NPC_ID = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnableWarehouseSortingClan"))
		{
			ENABLE_WAREHOUSESORTING_CLAN = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("EnableWarehouseSortingPrivate"))
		{
			ENABLE_WAREHOUSESORTING_PRIVATE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("EnableManaPotionSupport"))
		{
			ENABLE_MANA_POTIONS_SUPPORT = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DisplayServerTime"))
		{
			DISPLAY_SERVER_TIME = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AntiFeedEnable"))
		{
			ANTIFEED_ENABLE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AntiFeedDualbox"))
		{
			ANTIFEED_DUALBOX = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AntiFeedDisconnectedAsDualbox"))
		{
			ANTIFEED_DISCONNECTED_AS_DUALBOX = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AntiFeedInterval"))
		{
			ANTIFEED_INTERVAL = 1000 * Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MinKarma"))
		{
			KARMA_MIN_KARMA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxKarma"))
		{
			KARMA_MAX_KARMA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("XPDivider"))
		{
			KARMA_XP_DIVIDER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BaseKarmaLost"))
		{
			KARMA_LOST_BASE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CanGMDropEquipment"))
		{
			KARMA_DROP_GM = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint"))
		{
			KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop"))
		{
			KARMA_PK_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PvPVsNormalTime"))
		{
			PVP_NORMAL_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PvPVsPvPTime"))
		{
			PVP_PVP_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("DisableAttackIfLvlDifferenceOver"))
		{
			DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PunishPKPlayerIfPKsOver"))
		{
			PUNISH_PK_PLAYER_IF_PKS_OVER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PKMonitorPeriod"))
		{
			PK_MONITOR_PERIOD = Long.parseLong(pValue);
		}
		else if (pName.equalsIgnoreCase("PKPunishmentType"))
		{
			PK_PUNISHMENT_TYPE = pValue;
		}
		else if (pName.equalsIgnoreCase("PKPunishmentPeriod"))
		{
			PK_PUNISHMENT_PERIOD = Long.parseLong(pValue);
		}
		else if (pName.equalsIgnoreCase("GlobalChat"))
		{
			DEFAULT_GLOBAL_CHAT = pValue;
		}
		else if (pName.equalsIgnoreCase("TradeChat"))
		{
			DEFAULT_TRADE_CHAT = pValue;
		}
		else if (pName.equalsIgnoreCase("PremiumRateXp"))
		{
			PREMIUM_RATE_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PremiumRateSp"))
		{
			PREMIUM_RATE_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PremiumRateDropSpoil"))
		{
			PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PremiumRateDropItems"))
		{
			PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PremiumID"))
		{
			PREMIUM_ID = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PremiumCount"))
		{
			PREMIUM_COUNT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PremiumRateDropQuest"))
		{
			PREMIUM_RATE_DROP_QUEST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PremiumRateDropAdena"))
		{
			PREMIUM_RATE_DROP_ITEMS_ID.put(PcInventory.ADENA_ID, Float.parseFloat(pValue));
		}
		else if (pName.equalsIgnoreCase("PremiumRateRaidDropItems"))
		{
			PREMIUM_RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(pValue);
		}
		else
		{
			try
			{
				if (!pName.startsWith("Interval_") && !pName.startsWith("Random_"))
				{
					pName = pName.toUpperCase();
				}
				Field clazField = Config.class.getField(pName);
				int modifiers = clazField.getModifiers();
				// just in case :)
				if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers))
				{
					throw new SecurityException("Cannot modify non public or non static config!");
				}
				
				if (clazField.getType() == int.class)
				{
					clazField.setInt(clazField, Integer.parseInt(pValue));
				}
				else if (clazField.getType() == short.class)
				{
					clazField.setShort(clazField, Short.parseShort(pValue));
				}
				else if (clazField.getType() == byte.class)
				{
					clazField.setByte(clazField, Byte.parseByte(pValue));
				}
				else if (clazField.getType() == long.class)
				{
					clazField.setLong(clazField, Long.parseLong(pValue));
				}
				else if (clazField.getType() == float.class)
				{
					clazField.setFloat(clazField, Float.parseFloat(pValue));
				}
				else if (clazField.getType() == double.class)
				{
					clazField.setDouble(clazField, Double.parseDouble(pValue));
				}
				else if (clazField.getType() == boolean.class)
				{
					clazField.setBoolean(clazField, Boolean.parseBoolean(pValue));
				}
				else if (clazField.getType() == String.class)
				{
					clazField.set(clazField, pValue);
				}
				else
				{
					return false;
				}
			}
			catch (NoSuchFieldException e)
			{
				return false;
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
				return false;
			}
		}
		return true;
	}
	
	public static void saveHexid(int serverId, String string)
	{
		Config.saveHexid(serverId, string, HEXID_FILE);
	}
	
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			L2Properties hexSetting = new L2Properties();
			File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			try (OutputStream out = new FileOutputStream(file))
			{
				hexSetting.setProperty("ServerID", String.valueOf(serverId));
				hexSetting.setProperty("HexID", hexId);
				hexSetting.store(out, "the hexID to auth into login");
			}
		}
		catch (Exception e)
		{
			_log.warning(StringUtil.concat("Failed to save hex id to ", fileName, " File."));
			_log.warning("Config: " + e.getMessage());
		}
	}
	
	private static void loadFloodProtectorConfigs(final L2Properties properties)
	{
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", "4");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ROLL_DICE, "RollDice", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_FIREWORK, "Firework", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_PET_SUMMON, "ItemPetSummon", "16");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_HERO_VOICE, "HeroVoice", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GLOBAL_CHAT, "GlobalChat", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SUBCLASS, "Subclass", "20");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_DROP_ITEM, "DropItem", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SERVER_BYPASS, "ServerBypass", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MULTISELL, "MultiSell", "1");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRANSACTION, "Transaction", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANUFACTURE, "Manufacture", "3");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANOR, "Manor", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SENDMAIL, "SendMail", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CHARACTER_SELECT, "CharacterSelect", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_AUCTION, "ItemAuction", "9");
	}
	
	private static void loadFloodProtectorConfig(final L2Properties properties, final FloodProtectorConfig config, final String configString, final String defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval));
		config.LOG_FLOODING = Boolean.parseBoolean(properties.getProperty(StringUtil.concat("FloodProtector", configString, "LogFlooding"), "False"));
		config.PUNISHMENT_LIMIT = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), "0"));
		config.PUNISHMENT_TYPE = properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), "0"));
	}
	
	public static int getServerTypeId(String[] serverTypes)
	{
		int tType = 0;
		for (String cType : serverTypes)
		{
			cType = cType.trim();
			if (cType.equalsIgnoreCase("Normal"))
			{
				tType |= 0x01;
			}
			else if (cType.equalsIgnoreCase("Relax"))
			{
				tType |= 0x02;
			}
			else if (cType.equalsIgnoreCase("Test"))
			{
				tType |= 0x04;
			}
			else if (cType.equalsIgnoreCase("NoLabel"))
			{
				tType |= 0x08;
			}
			else if (cType.equalsIgnoreCase("Restricted"))
			{
				tType |= 0x10;
			}
			else if (cType.equalsIgnoreCase("Event"))
			{
				tType |= 0x20;
			}
			else if (cType.equalsIgnoreCase("Free"))
			{
				tType |= 0x40;
			}
		}
		return tType;
	}
	
	public static class ClassMasterSettings
	{
		private final Map<Integer, Map<Integer, Integer>> _claimItems;
		private final Map<Integer, Map<Integer, Integer>> _rewardItems;
		private final Map<Integer, Boolean> _allowedClassChange;
		
		public ClassMasterSettings(String _configLine)
		{
			_claimItems = new HashMap<>(3);
			_rewardItems = new HashMap<>(3);
			_allowedClassChange = new HashMap<>(3);
			if (_configLine != null)
			{
				parseConfigLine(_configLine.trim());
			}
		}
		
		private void parseConfigLine(String _configLine)
		{
			StringTokenizer st = new StringTokenizer(_configLine, ";");
			
			while (st.hasMoreTokens())
			{
				// get allowed class change
				int job = Integer.parseInt(st.nextToken());
				
				_allowedClassChange.put(job, true);
				
				Map<Integer, Integer> _items = new HashMap<>();
				// parse items needed for class change
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						int _itemId = Integer.parseInt(st3.nextToken());
						int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}
				
				_claimItems.put(job, _items);
				
				_items = new HashMap<>();
				// parse gifts after class change
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						int _itemId = Integer.parseInt(st3.nextToken());
						int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}
				
				_rewardItems.put(job, _items);
			}
		}
		
		public boolean isAllowed(int job)
		{
			if (_allowedClassChange == null)
			{
				return false;
			}
			if (_allowedClassChange.containsKey(job))
			{
				return _allowedClassChange.get(job);
			}
			
			return false;
		}
		
		public Map<Integer, Integer> getRewardItems(int job)
		{
			if (_rewardItems.containsKey(job))
			{
				return _rewardItems.get(job);
			}
			
			return null;
		}
		
		public Map<Integer, Integer> getRequireItems(int job)
		{
			if (_claimItems.containsKey(job))
			{
				return _claimItems.get(job);
			}
			
			return null;
		}
	}
	
	private static Map<Integer, Float> parseConfigLine(String line)
	{
		String[] propertySplit = line.split(",");
		Map<Integer, Float> ret = new HashMap<>(propertySplit.length);
		int i = 0;
		for (String value : propertySplit)
		{
			ret.put(i++, Float.parseFloat(value));
		}
		return ret;
	}
	
	public static class ColorNameNpcSettings
	{
		private final Map<Integer, Map<Integer, Integer>> _feeItems;
		private final Map<Integer, Boolean> _allowedColorName;
		
		public ColorNameNpcSettings(String _configLine)
		{
			_feeItems = new HashMap<>();
			_allowedColorName = new HashMap<>();
			if (_configLine != null)
			{
				parseConfigLine(_configLine.trim());
			}
		}
		
		private void parseConfigLine(String _configLine)
		{
			StringTokenizer st = new StringTokenizer(_configLine, ";");
			while (st.hasMoreTokens())
			{
				int days = Integer.parseInt(st.nextToken());
				_allowedColorName.put(days, true);
				Map<Integer, Integer> _items = new HashMap<>();
				
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						int _itemId = Integer.parseInt(st3.nextToken());
						int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}
				_feeItems.put(days, _items);
			}
		}
		
		public boolean isAllowed(int days)
		{
			return (_allowedColorName != null) && _allowedColorName.containsKey(days);
		}
		
		public String[] getDays()
		{
			String _day = "";
			String _days[];
			for (int key : _allowedColorName.keySet())
			{
				_day += key + ";";
			}
			_days = _day.split(";");
			return _days;
		}
		
		public Map<Integer, Integer> getFeeItems(int days)
		{
			if (_feeItems.containsKey(days))
			{
				return _feeItems.get(days);
			}
			return null;
		}
	}
	
	private static int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
		{
			return null;
		}
		
		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				_log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid entry -> \"", valueSplit[0], "\", should be itemId,itemNumber"));
				return null;
			}
			
			result[i] = new int[2];
			try
			{
				result[i][0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid itemId -> \"", valueSplit[0], "\""));
				return null;
			}
			try
			{
				result[i][1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warning(StringUtil.concat("parseItemsList[Config.load()]: invalid item number -> \"", valueSplit[1], "\""));
				return null;
			}
			i++;
		}
		return result;
	}
	
	private static class IPConfigData extends DocumentParser
	{
		private static final List<String> _subnets = new ArrayList<>(5);
		private static final List<String> _hosts = new ArrayList<>(5);
		
		public IPConfigData()
		{
			load();
		}
		
		@Override
		public void load()
		{
			File f = new File(IP_CONFIG_FILE);
			if (f.exists())
			{
				_log.log(Level.INFO, "Network Config: ipconfig.xml exists using manual configuration...");
				parseFile(new File(IP_CONFIG_FILE));
			}
			else
			{
				_log.log(Level.INFO, "Network Config: ipconfig.xml doesn't exists using automatic configuration...");
				autoIpConfig();
			}
		}
		
		@Override
		protected void parseDocument()
		{
			NamedNodeMap attrs;
			for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("gameserver".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("define".equalsIgnoreCase(d.getNodeName()))
						{
							attrs = d.getAttributes();
							_subnets.add(attrs.getNamedItem("subnet").getNodeValue());
							_hosts.add(attrs.getNamedItem("address").getNodeValue());
							
							if (_hosts.size() != _subnets.size())
							{
								_log.log(Level.WARNING, "Failed to Load " + IP_CONFIG_FILE + " File - subnets does not match server addresses.");
							}
						}
					}
					
					Node att = n.getAttributes().getNamedItem("address");
					if (att == null)
					{
						_log.log(Level.WARNING, "Failed to load " + IP_CONFIG_FILE + " file - default server address is missing.");
						_hosts.add("127.0.0.1");
					}
					else
					{
						_hosts.add(att.getNodeValue());
					}
					_subnets.add("0.0.0.0/0");
				}
			}
		}
		
		protected void autoIpConfig()
		{
			String externalIp = "127.0.0.1";
			try
			{
				URL autoIp = new URL("http://api.externalip.net/ip/");
				try (BufferedReader in = new BufferedReader(new InputStreamReader(autoIp.openStream())))
				{
					externalIp = in.readLine();
				}
			}
			catch (IOException e)
			{
				_log.log(Level.INFO, "Network Config: Failed to connect to api.externalip.net please check your internet connection using 127.0.0.1!");
				externalIp = "127.0.0.1";
			}
			
			try
			{
				Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
				
				Subnet sub = new Subnet();
				while (niList.hasMoreElements())
				{
					NetworkInterface ni = niList.nextElement();
					
					if (!ni.isUp() || ni.isVirtual())
					{
						continue;
					}
					
					if (!ni.isLoopback() && ((ni.getHardwareAddress() == null) || (ni.getHardwareAddress().length != 6)))
					{
						continue;
					}
					
					for (InterfaceAddress ia : ni.getInterfaceAddresses())
					{
						if (ia.getAddress() instanceof Inet6Address)
						{
							continue;
						}
						
						sub.setIPAddress(ia.getAddress().getHostAddress());
						sub.setMaskedBits(ia.getNetworkPrefixLength());
						String subnet = sub.getSubnetAddress() + '/' + sub.getMaskedBits();
						if (!_subnets.contains(subnet) && !subnet.equals("0.0.0.0/0"))
						{
							_subnets.add(subnet);
							_hosts.add(sub.getIPAddress());
							_log.log(Level.INFO, "Network Config: Adding new subnet: " + subnet + " address: " + sub.getIPAddress());
						}
					}
				}
				_hosts.add(externalIp);
				_subnets.add("0.0.0.0/0");
				_log.log(Level.INFO, "Network Config: Adding new subnet: 0.0.0.0/0 address: " + externalIp);
			}
			catch (SocketException e)
			{
				_log.log(Level.INFO, "Network Config: Configuration failed please configure manually using ipconfig.xml", e);
				System.exit(0);
			}
		}
		
		protected List<String> getSubnets()
		{
			if (_subnets.isEmpty())
			{
				return Arrays.asList("0.0.0.0/0");
			}
			return _subnets;
		}
		
		protected List<String> getHosts()
		{
			if (_hosts.isEmpty())
			{
				return Arrays.asList("127.0.0.1");
			}
			return _hosts;
		}
	}
}