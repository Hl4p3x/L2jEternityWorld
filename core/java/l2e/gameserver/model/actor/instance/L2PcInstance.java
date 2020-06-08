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
package l2e.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ItemsAutoDestroy;
import l2e.gameserver.LoginServerThread;
import l2e.gameserver.RecipeController;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.ai.L2CharacterAI;
import l2e.gameserver.ai.L2PhantomAI;
import l2e.gameserver.ai.L2PhantomArcherAI;
import l2e.gameserver.ai.L2PhantomMageAI;
import l2e.gameserver.ai.L2PlayerAI;
import l2e.gameserver.ai.L2SummonAI;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.cache.WarehouseCacheManager;
import l2e.gameserver.communitybbs.BB.Forum;
import l2e.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2e.gameserver.communitybbs.Manager.MailBBSManager;
import l2e.gameserver.communitybbs.Manager.RegionBBSManager;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.sql.CharSummonHolder;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.NevitHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SkillHolder.FrequentSkill;
import l2e.gameserver.data.xml.AdminParser;
import l2e.gameserver.data.xml.CategoryParser;
import l2e.gameserver.data.xml.CharTemplateParser;
import l2e.gameserver.data.xml.ClassListParser;
import l2e.gameserver.data.xml.EnchantSkillGroupsParser;
import l2e.gameserver.data.xml.ExperienceParser;
import l2e.gameserver.data.xml.FishParser;
import l2e.gameserver.data.xml.HennaParser;
import l2e.gameserver.data.xml.PetsParser;
import l2e.gameserver.data.xml.RecipeParser;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.handler.ItemHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.CoupleManager;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.instancemanager.DuelManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.FunEventsManager;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.instancemanager.leaderboards.ArenaLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.TvTLeaderboard;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.BlockList;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.L2AccessLevel;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2ClanMember;
import l2e.gameserver.model.L2ContactList;
import l2e.gameserver.model.L2EnchantSkillLearn;
import l2e.gameserver.model.L2ManufactureItem;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2Party.messageType;
import l2e.gameserver.model.L2PetData;
import l2e.gameserver.model.L2PetLevelData;
import l2e.gameserver.model.L2PremiumItem;
import l2e.gameserver.model.L2Radar;
import l2e.gameserver.model.L2RecipeList;
import l2e.gameserver.model.L2Request;
import l2e.gameserver.model.L2ShortCut;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Macro;
import l2e.gameserver.model.MacroList;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.PartyMatchRoom;
import l2e.gameserver.model.PartyMatchRoomList;
import l2e.gameserver.model.PartyMatchWaitingList;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.ShortCuts;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.TeleportBookmark;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.TerritoryWard;
import l2e.gameserver.model.TimeStamp;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.UIKeysSettings;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Decoy;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.L2Vehicle;
import l2e.gameserver.model.actor.appearance.PcAppearance;
import l2e.gameserver.model.actor.events.PlayerEvents;
import l2e.gameserver.model.actor.knownlist.PcKnownList;
import l2e.gameserver.model.actor.position.PcPosition;
import l2e.gameserver.model.actor.protection.AdminProtection;
import l2e.gameserver.model.actor.stat.PcStat;
import l2e.gameserver.model.actor.status.PcStatus;
import l2e.gameserver.model.actor.tasks.player.DismountTask;
import l2e.gameserver.model.actor.tasks.player.FameTask;
import l2e.gameserver.model.actor.tasks.player.GameGuardCheckTask;
import l2e.gameserver.model.actor.tasks.player.InventoryEnableTask;
import l2e.gameserver.model.actor.tasks.player.LookingForFishTask;
import l2e.gameserver.model.actor.tasks.player.PcPointsTask;
import l2e.gameserver.model.actor.tasks.player.PetFeedTask;
import l2e.gameserver.model.actor.tasks.player.PvPFlagTask;
import l2e.gameserver.model.actor.tasks.player.RecoBonusTaskEnd;
import l2e.gameserver.model.actor.tasks.player.RecoGiveTask;
import l2e.gameserver.model.actor.tasks.player.RentPetTask;
import l2e.gameserver.model.actor.tasks.player.ResetChargesTask;
import l2e.gameserver.model.actor.tasks.player.ResetSoulsTask;
import l2e.gameserver.model.actor.tasks.player.ShortBuffTask;
import l2e.gameserver.model.actor.tasks.player.SitDownTask;
import l2e.gameserver.model.actor.tasks.player.StandUpTask;
import l2e.gameserver.model.actor.tasks.player.TeleportWatchdogTask;
import l2e.gameserver.model.actor.tasks.player.VitalityTask;
import l2e.gameserver.model.actor.tasks.player.WarnUserTakeBreakTask;
import l2e.gameserver.model.actor.tasks.player.WaterTask;
import l2e.gameserver.model.actor.templates.L2PcTemplate;
import l2e.gameserver.model.actor.transform.Transform;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.ClassLevel;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.base.Sex;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.model.effects.EffectFlag;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Duel;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.entity.L2Event;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.entity.events.FunEvent;
import l2e.gameserver.model.entity.events.Hitman;
import l2e.gameserver.model.entity.events.MonsterRush;
import l2e.gameserver.model.entity.events.TW;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.entity.events.phoenix.Interface;
import l2e.gameserver.model.entity.mods.AchievementsManager;
import l2e.gameserver.model.entity.underground_coliseum.UCTeam;
import l2e.gameserver.model.fishing.L2Fish;
import l2e.gameserver.model.fishing.L2Fishing;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.holders.SkillUseHolder;
import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.itemcontainer.ItemContainer;
import l2e.gameserver.model.itemcontainer.PcFreight;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.itemcontainer.PcRefund;
import l2e.gameserver.model.itemcontainer.PcWarehouse;
import l2e.gameserver.model.itemcontainer.PetInventory;
import l2e.gameserver.model.items.L2Armor;
import l2e.gameserver.model.items.L2EtcItem;
import l2e.gameserver.model.items.L2Henna;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2ActionType;
import l2e.gameserver.model.items.type.L2ArmorType;
import l2e.gameserver.model.items.type.L2EtcItemType;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.multisell.PreparedListContainer;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.olympiad.OlympiadGameTask;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTask;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.Quest.QuestEventType;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.restriction.GlobalRestrictions;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.model.skills.l2skills.L2SkillSiegeFlag;
import l2e.gameserver.model.skills.l2skills.L2SkillSummon;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.variables.AccountVariables;
import l2e.gameserver.model.variables.PlayerVariables;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.L2BossZone;
import l2e.gameserver.model.zone.type.L2TownZone;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.CameraMode;
import l2e.gameserver.network.serverpackets.ChangeWaitType;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.EtcStatusUpdate;
import l2e.gameserver.network.serverpackets.ExAutoSoulShot;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.ExDominionWarStart;
import l2e.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import l2e.gameserver.network.serverpackets.ExFishingEnd;
import l2e.gameserver.network.serverpackets.ExFishingStart;
import l2e.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import l2e.gameserver.network.serverpackets.ExGetOnAirShip;
import l2e.gameserver.network.serverpackets.ExMailArrived;
import l2e.gameserver.network.serverpackets.ExNevitAdventEffect;
import l2e.gameserver.network.serverpackets.ExNevitAdventPointInfoPacket;
import l2e.gameserver.network.serverpackets.ExNevitAdventTimeChange;
import l2e.gameserver.network.serverpackets.ExOlympiadMode;
import l2e.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import l2e.gameserver.network.serverpackets.ExSetCompassZoneCode;
import l2e.gameserver.network.serverpackets.ExStartScenePlayer;
import l2e.gameserver.network.serverpackets.ExStorageMaxCount;
import l2e.gameserver.network.serverpackets.ExUseSharedGroupItem;
import l2e.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2e.gameserver.network.serverpackets.FriendStatusPacket;
import l2e.gameserver.network.serverpackets.GameGuardQuery;
import l2e.gameserver.network.serverpackets.GetOnVehicle;
import l2e.gameserver.network.serverpackets.HennaInfo;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.LeaveWorld;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.MyTargetSelected;
import l2e.gameserver.network.serverpackets.NicknameChanged;
import l2e.gameserver.network.serverpackets.NormalCamera;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.ObservationMode;
import l2e.gameserver.network.serverpackets.ObservationReturn;
import l2e.gameserver.network.serverpackets.PartySmallWindowUpdate;
import l2e.gameserver.network.serverpackets.PetInventoryUpdate;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.PrivateStoreListBuy;
import l2e.gameserver.network.serverpackets.PrivateStoreListSell;
import l2e.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import l2e.gameserver.network.serverpackets.PrivateStoreManageListSell;
import l2e.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import l2e.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2e.gameserver.network.serverpackets.RecipeShopMsg;
import l2e.gameserver.network.serverpackets.RecipeShopSellList;
import l2e.gameserver.network.serverpackets.RelationChanged;
import l2e.gameserver.network.serverpackets.Ride;
import l2e.gameserver.network.serverpackets.ServerClose;
import l2e.gameserver.network.serverpackets.SetupGauge;
import l2e.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import l2e.gameserver.network.serverpackets.ShortCutInit;
import l2e.gameserver.network.serverpackets.SkillCoolTime;
import l2e.gameserver.network.serverpackets.SkillList;
import l2e.gameserver.network.serverpackets.Snoop;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.StopMove;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TargetSelected;
import l2e.gameserver.network.serverpackets.TargetUnselected;
import l2e.gameserver.network.serverpackets.TradeDone;
import l2e.gameserver.network.serverpackets.TradeOtherDone;
import l2e.gameserver.network.serverpackets.TradeStart;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.network.serverpackets.ValidateLocation;
import l2e.gameserver.scripting.scriptengine.events.EquipmentEvent;
import l2e.gameserver.scripting.scriptengine.events.HennaEvent;
import l2e.gameserver.scripting.scriptengine.events.ProfessionChangeEvent;
import l2e.gameserver.scripting.scriptengine.events.TransformEvent;
import l2e.gameserver.scripting.scriptengine.listeners.player.EquipmentListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.EventListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.HennaListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.ProfessionChangeListener;
import l2e.gameserver.scripting.scriptengine.listeners.player.TransformListener;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;
import l2e.gameserver.util.Broadcast;
import l2e.gameserver.util.FloodProtectors;
import l2e.gameserver.util.PlayerEventStatus;
import l2e.gameserver.util.Point3D;
import l2e.gameserver.util.Util;
import l2e.protection.Protection;
import l2e.protection.network.ProtectionManager;
import l2e.scripts.events.LastHero;
import l2e.util.L2FastList;
import l2e.util.MySqlUtils;
import l2e.util.Rnd;
import l2e.util.Strings;

public final class L2PcInstance extends L2Playable
{
	private static final String RESTORE_SKILLS_FOR_CHAR_DEFAULT = "SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index=?";
	private static final String RESTORE_SKILLS_FOR_CHAR_MODIFER = "SELECT skill_id,skill_level FROM character_skills WHERE charId=? ORDER BY skill_id , skill_level ASC";
	private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
	private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND charId=? AND class_index=?";
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?";
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE charId=? AND class_index=?";
	
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (charId,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE charId=? AND class_index=? ORDER BY buff_index ASC";
	private static final String RESTORE_SKILL_SAVE_MODIFER = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE charId=? ORDER BY buff_index ASC";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE charId=? AND class_index=?";
	private static final String DELETE_SKILL_SAVE_MODIFER = "DELETE FROM character_skills_save WHERE charId=?";
	
	private static final String ADD_ITEM_REUSE_SAVE = "INSERT INTO character_item_reuse_save (charId,itemId,itemObjId,reuseDelay,systime) VALUES (?,?,?,?,?)";
	private static final String RESTORE_ITEM_REUSE_SAVE = "SELECT charId,itemId,itemObjId,reuseDelay,systime FROM character_item_reuse_save WHERE charId=?";
	private static final String DELETE_ITEM_REUSE_SAVE = "DELETE FROM character_item_reuse_save WHERE charId=?";
	
	private static final String INSERT_CHARACTER = "INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,fame,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,title_color,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,createDate) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,fame=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,title_color=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,newbie=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,bookmarkslot=?,vitality_points=?,pccafe_points=?,language=?, hitman_target=?, game_points=? WHERE charId=?";
	private static final String RESTORE_CHARACTER = "SELECT * FROM characters WHERE charId=?";
	
	private static final String INSERT_TP_BOOKMARK = "INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)";
	private static final String UPDATE_TP_BOOKMARK = "UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?";
	private static final String RESTORE_TP_BOOKMARK = "SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?";
	private static final String DELETE_TP_BOOKMARK = "DELETE FROM character_tpbookmark WHERE charId=? AND Id=?";
	
	private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
	private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE charId=? AND class_index =?";
	private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";
	
	private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE charId=? AND class_index=?";
	private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (charId,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?";
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE charId=? AND class_index=?";
	
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE charId=? AND class_index=?";
	
	private static final String RESTORE_PREMIUMSERVICE = "SELECT premium_service,enddate FROM character_premium WHERE account_name=?";
	private static final String UPDATE_PREMIUMSERVICE = "UPDATE character_premium SET premium_service=?,enddate=? WHERE account_name=?";
	
	private static final String DELETE_PHANTOM_HENNAS = "DELETE FROM character_hennas WHERE charId=?";
	private static final String DELETE_PHANTOM_SHORTCUTS = "DELETE FROM character_shortcuts WHERE charId=?";
	private static final String DELETE_PHANTOM_SKILL_SAVE = "DELETE FROM character_skills_save WHERE charId=?";
	private static final String DELETE_PHANTOM_SKILLS = "DELETE FROM character_skills WHERE charId=?";
	private static final String DELETE_PHANTOM_SUBCLASS = "DELETE FROM character_subclasses WHERE charId=?";
	
	private static final String COND_OVERRIDE_KEY = "cond_override";
	
	public static final int ID_NONE = -1;
	public static final int REQUEST_TIMEOUT = 15;
	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_PRIVATE_PACKAGE_SELL = 8;
	
	public boolean eventSitForced;
	
	public enum ConfirmDialogScripts
	{
		None,
		Wedding,
		LastHero,
		Tvt,
		TvTRound,
		MonsterRush
	}
	
	public ConfirmDialogScripts CurrentConfirmDialog = ConfirmDialogScripts.None;
	
	private static final List<HennaListener> HENNA_LISTENERS = new FastList<HennaListener>().shared();
	private static final List<EquipmentListener> GLOBAL_EQUIPMENT_LISTENERS = new FastList<EquipmentListener>().shared();
	private static final List<ProfessionChangeListener> GLOBAL_PROFESSION_CHANGE_LISTENERS = new FastList<ProfessionChangeListener>().shared();
	
	private final List<EquipmentListener> _equipmentListeners = new FastList<EquipmentListener>().shared();
	private final List<TransformListener> _transformListeners = new FastList<TransformListener>().shared();
	private final List<ProfessionChangeListener> _professionChangeListeners = new FastList<ProfessionChangeListener>().shared();
	private final List<EventListener> _eventListeners = new FastList<EventListener>().shared();
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		public L2PcInstance getPlayer()
		{
			return L2PcInstance.this;
		}
		
		public void doPickupItem(L2Object object)
		{
			L2PcInstance.this.doPickupItem(object);
		}
		
		public void doInteract(L2Character target)
		{
			L2PcInstance.this.doInteract(target);
		}
		
		@Override
		public void doAttack(L2Character target)
		{
			updateLastActivityAction();
			if ((target instanceof L2PcInstance) && isPKProtected((L2PcInstance) target))
			{
				sendMessage("You can't attack player with too low level.");
				sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				super.doAttack(target);
				
				getPlayer().setRecentFakeDeath(false);
			}
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
			updateLastActivityAction();
			super.doCast(skill);
			
			getPlayer().setRecentFakeDeath(false);
		}
	}
	
	private L2GameClient _client;
	
	private final String _accountName;
	private long _deleteTimer;
	private Calendar _createDate = Calendar.getInstance();
	
	private volatile boolean _isOnline = false;
	private long _onlineTime;
	private long _onlineBeginTime;
	private long _lastAccess;
	private long _uptime;
	
	private final ReentrantLock _subclassLock = new ReentrantLock();
	protected int _baseClass;
	protected int _activeClass;
	protected int _classIndex = 0;
	
	private int _controlItemId;
	private L2PetData _data;
	private L2PetLevelData _leveldata;
	private int _curFeed;
	protected Future<?> _mountFeedTask;
	private ScheduledFuture<?> _dismountTask;
	private boolean _petItems = false;
	
	private int _pcBangPoints = 0;
	
	private boolean _isAioMultisell = false;
	private boolean _isUsingAioWh = false;
	
	private Map<Integer, SubClass> _subClasses;
	
	private final PcAppearance _appearance;
	
	@Deprecated
	private final int _charId = 0x00030b7a;
	
	private long _expBeforeDeath;
	
	private int _karma;
	private int _pvpKills;
	private int _pkKills;
	private byte _pvpFlag;
	
	private int _fame;
	private ScheduledFuture<?> _fameTask;
	
	private ScheduledFuture<?> _vitalityTask;
	private ScheduledFuture<?> _pcCafePointsTask;
	
	private long _gamePoints;
	
	private volatile ScheduledFuture<?> _teleportWatchdog;
	
	private byte _siegeState = 0;
	private int _siegeSide = 0;
	
	private int _curWeightPenalty = 0;
	
	private int _lastCompassZone;
	
	private boolean _isIn7sDungeon = false;
	
	private boolean _isInKrateisCube = false;
	
	private final L2ContactList _contactList = new L2ContactList(this);
	
	private int _bookmarkslot = 0;
	
	private final Map<Integer, TeleportBookmark> _tpbookmarks = new FastMap<>();
	
	private boolean _canFeed;
	private int _eventEffectId = 0;
	private boolean _isInSiege;
	private boolean _isInHideoutSiege = false;
	
	private L2PcTemplate _antifeedTemplate = null;
	private boolean _antifeedSex;
	
	public static boolean _survey_running = false;
	public static int yes = 0;
	public static int no = 0;
	private boolean _isOnSurvey = false;
	private long _lastsurvey;
	public static long lastsurvey = 0L;
	public static long surveyDelay = 43200000L;
	
	private boolean _inOlympiadMode = false;
	private boolean _OlympiadStart = false;
	private int _olympiadGameId = -1;
	private int _olympiadSide = -1;
	public int olyBuff = 0;
	
	private boolean _isInDuel = false;
	private int _duelState = Duel.DUELSTATE_NODUEL;
	private int _duelId = 0;
	private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	
	private L2Vehicle _vehicle = null;
	private Point3D _inVehiclePosition;
	
	public ScheduledFuture<?> _taskforfish;
	private MountType _mountType = MountType.NONE;
	private int _mountNpcId;
	private int _mountLevel;
	private int _mountObjectID = 0;
	
	public int _telemode = 0;
	
	private boolean _inCrystallize;
	private boolean _inCraftMode;
	
	private long _offlineShopStart = 0;
	
	private Transform _transformation;
	
	private final Map<Integer, L2RecipeList> _dwarvenRecipeBook = new FastMap<>();
	private final Map<Integer, L2RecipeList> _commonRecipeBook = new FastMap<>();
	
	private final Map<Integer, L2PremiumItem> _premiumItems = new FastMap<>();
	
	private boolean _waitTypeSitting;
	
	public boolean _needPartyBroadcast = false;
	
	private int _lastX;
	private int _lastY;
	private int _lastZ;
	private boolean _observerMode = false;
	
	private final Point3D _lastServerPosition = new Point3D(0, 0, 0);
	
	private int _recomHave;
	private int _recomLeft;
	private ScheduledFuture<?> _recoBonusTask;
	private boolean _isRecoBonusActive = false;
	private int _recoBonusMode = 0;
	private ScheduledFuture<?> _recoGiveTask;
	protected boolean _recoTwoHoursGiven = false;
	
	private ScheduledFuture<?> _adventBonusTask;
	protected ScheduledFuture<?> _adventBlessingTask;
	
	private final PcInventory _inventory = new PcInventory(this);
	private final PcFreight _freight = new PcFreight(this);
	private PcWarehouse _warehouse;
	private PcRefund _refund;
	
	private int _privatestore;
	
	private TradeList _activeTradeList;
	private ItemContainer _activeWarehouse;
	private volatile Map<Integer, L2ManufactureItem> _manufactureItems;
	private String _storeName = "";
	private TradeList _sellList;
	private TradeList _buyList;
	
	private PreparedListContainer _currentMultiSell = null;
	
	private int _newbie;
	
	private boolean _noble = false;
	private boolean _hero = false;
	
	private L2Npc _lastFolkNpc = null;
	
	private int _questNpcObject = 0;
	
	private final Map<String, QuestState> _quests = new FastMap<>();
	
	private final ShortCuts _shortCuts = new ShortCuts(this);
	
	private final MacroList _macros = new MacroList(this);
	
	private final List<L2PcInstance> _snoopListener = new FastList<>();
	private final List<L2PcInstance> _snoopedPlayer = new FastList<>();
	
	private final L2Henna[] _henna = new L2Henna[3];
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;
	
	private L2Summon _summon = null;
	private L2Decoy _decoy = null;
	private L2TrapInstance _trap = null;
	private int _agathionId = 0;
	private List<L2TamedBeastInstance> _tamedBeast = null;
	
	private boolean _minimapAllowed = false;
	
	private final L2Radar _radar;
	
	private int _partyroom = 0;
	
	private int _clanId;
	private L2Clan _clan;
	
	private int _apprentice = 0;
	private int _sponsor = 0;
	
	private long _clanJoinExpiryTime;
	private long _clanCreateExpiryTime;
	
	private int _powerGrade = 0;
	private int _clanPrivileges = 0;
	
	private int _pledgeClass = 0;
	private int _pledgeType = 0;
	
	private int _lvlJoinedAcademy = 0;
	
	private int _wantsPeace = 0;
	
	private long _lastActivityMove = 0;
	private long _lastActivityAction = 0;
	
	private int _deathPenaltyBuffLevel = 0;
	
	private final AtomicInteger _charges = new AtomicInteger();
	private ScheduledFuture<?> _chargeTask = null;
	
	private int _souls = 0;
	private ScheduledFuture<?> _soulTask = null;
	
	private Location _currentSkillWorldPosition;
	
	private L2AccessLevel _accessLevel;
	
	private boolean _messageRefusal = false;
	
	private boolean _silenceMode = false;
	private List<Integer> _silenceModeExcluded;
	private boolean _dietMode = false;
	private boolean _tradeRefusal = false;
	private boolean _exchangeRefusal = false;
	
	private L2Party _party;
	
	private L2PcInstance _activeRequester;
	private long _requestExpireTime = 0;
	private final L2Request _request = new L2Request(this);
	private L2ItemInstance _arrowItem;
	private L2ItemInstance _boltItem;
	
	private long _protectEndTime = 0;
	
	public int expertiseIndex = 0;
	
	public static final int[] EXPERTISE_LEVELS =
	{
		0,
		20,
		40,
		52,
		61,
		76,
		80,
		84,
		Integer.MAX_VALUE
	};
	
	private boolean _voting = false;
	
	String code;
	private int Kills = -1;
	private boolean codeRight = false;
	
	public boolean isSpawnProtected()
	{
		return _protectEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	private long _teleportProtectEndTime = 0;
	
	public boolean isTeleportProtected()
	{
		return _teleportProtectEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	private long _recentFakeDeathEndTime = 0;
	private boolean _isFakeDeath;
	
	private L2Weapon _fistsWeaponItem;
	
	private final Map<Integer, String> _chars = new FastMap<>();
	
	private int _expertiseArmorPenalty = 0;
	private int _expertiseWeaponPenalty = 0;
	
	private boolean _isEnchanting = false;
	private int _activeEnchantItemId = ID_NONE;
	private int _activeEnchantSupportItemId = ID_NONE;
	private int _activeEnchantAttrItemId = ID_NONE;
	private long _activeEnchantTimestamp = 0;
	
	protected boolean _inventoryDisable = false;
	
	private final List<L2CubicInstance> _cubics = new CopyOnWriteArrayList<>();
	
	protected FastSet<Integer> _activeSoulShots = new FastSet<Integer>().shared();
	
	public final ReentrantLock soulShotLock = new ReentrantLock();
	
	private PlayerEventStatus eventStatus = null;
	
	private int UCKills = 0;
	private int UCDeaths = 0;
	public static final int UC_STATE_NONE = 0;
	public static final int UC_STATE_POINT = 1;
	public static final int UC_STATE_ARENA = 2;
	private int UCState = 0;
	
	private byte _handysBlockCheckerEventArena = -1;
	
	private final int _loto[] = new int[5];
	
	private final int _race[] = new int[2];
	
	private final BlockList _blockList = new BlockList(this);
	
	public String _eventName = "";
	public int _eventTeamId;
	public String _eventOriginalTitle;
	public boolean _eventTeleported;
	public int _eventOriginalNameColor, _eventOriginalKarma, _eventCountKills;
	public int _CTFHaveFlagOfTeam, _CTFCountFlags;
	
	private L2Fishing _fishCombat;
	private boolean _fishing = false;
	private int _fishx = 0;
	private int _fishy = 0;
	private int _fishz = 0;
	
	private Set<Integer> _transformAllowedSkills;
	private ScheduledFuture<?> _taskRentPet;
	private ScheduledFuture<?> _taskWater;
	
	private final List<String> _validBypass = new L2FastList<>(true);
	private final List<String> _validBypass2 = new L2FastList<>(true);
	
	private Forum _forumMail;
	private Forum _forumMemo;
	private Map<String, String> _userSession;
	
	private SkillUseHolder _currentSkill;
	private SkillUseHolder _currentPetSkill;
	
	private SkillUseHolder _queuedSkill;
	
	private int _cursedWeaponEquippedId = 0;
	private boolean _combatFlagEquippedId = false;
	
	private int _reviveRequested = 0;
	private double _revivePower = 0;
	private boolean _revivePet = false;
	
	private double _cpUpdateIncCheck = .0;
	private double _cpUpdateDecCheck = .0;
	private double _cpUpdateInterval = .0;
	private double _mpUpdateIncCheck = .0;
	private double _mpUpdateDecCheck = .0;
	private double _mpUpdateInterval = .0;
	
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	
	private static final int FALLING_VALIDATION_DELAY = 10000;
	private volatile long _fallingTimestamp = 0;
	
	private int _multiSocialTarget = 0;
	private int _multiSociaAction = 0;
	
	private int _movieId = 0;
	
	private String _adminConfirmCmd = null;
	
	private volatile long _lastItemAuctionInfoRequest = 0;
	
	private Future<?> _PvPRegTask;
	
	private long _pvpFlagLasts;
	
	private long _notMoveUntil = 0;
	
	private Map<Integer, L2Skill> _customSkills = null;
	
	private int _KamalokaID = 0;
	
	private boolean _inMonsterRush = false;
	
	private AdminProtection _AdminProtection = null;
	
	private boolean _canRevive = true;
	
	public void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}
	
	public long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}
	
	public void startPvPFlag()
	{
		updatePvPFlag(1);
		
		if (_PvPRegTask == null)
		{
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlagTask(this), 1000, 1000);
		}
	}
	
	public void stopPvpRegTask()
	{
		if (_PvPRegTask != null)
		{
			_PvPRegTask.cancel(true);
			_PvPRegTask = null;
		}
	}
	
	public void stopPvPFlag()
	{
		stopPvpRegTask();
		
		updatePvPFlag(0);
		
		_PvPRegTask = null;
	}
	
	private UIKeysSettings _uiKeySettings;
	
	private ScheduledFuture<?> _shortBuffTask = null;
	
	private boolean _married = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _engagerequest = false;
	private int _engageid = 0;
	private boolean _marryrequest = false;
	private boolean _marryaccepted = false;
	
	private final List<Long> _pKsCounter = new FastList<>();
	
	private String _lastPetitionGmName = null;
	
	protected static class SummonRequest
	{
		private L2PcInstance _target = null;
		private L2Skill _skill = null;
		
		public void setTarget(L2PcInstance destination, L2Skill skill)
		{
			_target = destination;
			_skill = skill;
		}
		
		public L2PcInstance getTarget()
		{
			return _target;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
	}
	
	public static L2PcInstance create(L2PcTemplate template, String accountName, String name, PcAppearance app)
	{
		L2PcInstance player = new L2PcInstance(IdFactory.getInstance().getNextId(), template, accountName, app, false);
		
		player.setName(name);
		player.setCreateDate(Calendar.getInstance());
		player.setBaseClass(player.getClassId());
		player.setNewbie(1);
		player.setRecomLeft(20);
		return player.createDb() ? player : null;
	}
	
	public String getAccountName()
	{
		if (getClient() == null)
		{
			return getAccountNamePlayer();
		}
		return getClient().getAccountName();
	}
	
	public String getAccountNamePlayer()
	{
		return _accountName;
	}
	
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}
	
	public int getRelation(L2PcInstance target)
	{
		int result = 0;
		
		if (getClan() != null)
		{
			result |= RelationChanged.RELATION_CLAN_MEMBER;
			if (getClan() == target.getClan())
			{
				result |= RelationChanged.RELATION_CLAN_MATE;
			}
			if (getAllyId() != 0)
			{
				result |= RelationChanged.RELATION_ALLY_MEMBER;
			}
		}
		if (isClanLeader())
		{
			result |= RelationChanged.RELATION_LEADER;
		}
		if ((getParty() != null) && (getParty() == target.getParty()))
		{
			result |= RelationChanged.RELATION_HAS_PARTY;
			for (int i = 0; i < getParty().getMembers().size(); i++)
			{
				if (getParty().getMembers().get(i) != this)
				{
					continue;
				}
				switch (i)
				{
					case 0:
						result |= RelationChanged.RELATION_PARTYLEADER;
						break;
					case 1:
						result |= RelationChanged.RELATION_PARTY4;
						break;
					case 2:
						result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1;
						break;
					case 3:
						result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2;
						break;
					case 4:
						result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY1;
						break;
					case 5:
						result |= RelationChanged.RELATION_PARTY3;
						break;
					case 6:
						result |= RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1;
						break;
					case 7:
						result |= RelationChanged.RELATION_PARTY2;
						break;
					case 8:
						result |= RelationChanged.RELATION_PARTY1;
						break;
				}
			}
		}
		if (getSiegeState() != 0)
		{
			if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(this) != 0)
			{
				result |= RelationChanged.RELATION_TERRITORY_WAR;
			}
			else
			{
				result |= RelationChanged.RELATION_INSIEGE;
				if (getSiegeState() != target.getSiegeState())
				{
					result |= RelationChanged.RELATION_ENEMY;
				}
				else
				{
					result |= RelationChanged.RELATION_ALLY;
				}
				if (getSiegeState() == 1)
				{
					result |= RelationChanged.RELATION_ATTACKER;
				}
			}
		}
		if ((getClan() != null) && (target.getClan() != null))
		{
			if ((target.getPledgeType() != L2Clan.SUBUNIT_ACADEMY) && (getPledgeType() != L2Clan.SUBUNIT_ACADEMY) && target.getClan().isAtWarWith(getClan().getId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getId()))
				{
					result |= RelationChanged.RELATION_MUTUAL_WAR;
				}
			}
		}
		if (getBlockCheckerArena() != -1)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
			if (holder.getPlayerTeam(this) == 0)
			{
				result |= RelationChanged.RELATION_ENEMY;
			}
			else
			{
				result |= RelationChanged.RELATION_ALLY;
			}
			result |= RelationChanged.RELATION_ATTACKER;
		}
		return result;
	}
	
	public static L2PcInstance load(int objectId)
	{
		return restore(objectId);
	}
	
	private void initPcStatusUpdateValues()
	{
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}
	
	private L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app, boolean fantome)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2PcInstance);
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		_accountName = accountName;
		app.setOwner(this);
		_appearance = app;
		
		if (fantome)
		{
			switch (getClassId().getId())
			{
				case 10:
				case 25:
				case 38:
				case 11:
				case 15:
				case 26:
				case 29:
				case 39:
				case 42:
				case 12:
				case 13:
				case 14:
				case 16:
				case 27:
				case 30:
				case 40:
				case 43:
				case 94:
				case 95:
				case 98:
				case 103:
				case 105:
				case 110:
				case 112:
					_ai = new L2PhantomMageAI(new AIAccessor());
					break;
				case 9:
				case 24:
				case 37:
				case 92:
				case 102:
				case 109:
				case 130:
					_ai = new L2PhantomArcherAI(new AIAccessor());
					break;
				default:
					_ai = new L2PhantomAI(new AIAccessor());
					break;
			}
		}
		else
		{
			_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
		}
		
		_radar = new L2Radar(this);
		
		startVitalityTask();
	}
	
	@Override
	public final PcKnownList getKnownList()
	{
		return (PcKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new PcKnownList(this));
	}
	
	@Override
	public final PcStat getStat()
	{
		return (PcStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new PcStat(this));
	}
	
	@Override
	public final PcStatus getStatus()
	{
		return (PcStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new PcStatus(this));
	}
	
	@Override
	public void initCharEvents()
	{
		setCharEvents(new PlayerEvents(this));
	}
	
	@Override
	public PlayerEvents getEvents()
	{
		return (PlayerEvents) super.getEvents();
	}
	
	@Override
	public PcPosition getPosition()
	{
		return (PcPosition) super.getPosition();
	}
	
	@Override
	public void initPosition()
	{
		setObjectPosition(new PcPosition(this));
	}
	
	public final PcAppearance getAppearance()
	{
		return _appearance;
	}
	
	public final L2PcTemplate getBaseTemplate()
	{
		return CharTemplateParser.getInstance().getTemplate(_baseClass);
	}
	
	@Override
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate) super.getTemplate();
	}
	
	public void setTemplate(ClassId newclass)
	{
		super.setTemplate(CharTemplateParser.getInstance().getTemplate(newclass));
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					if (isPhantome())
					{
						switch (getClassId().getId())
						{
							case 10:
							case 25:
							case 38:
							case 11:
							case 15:
							case 26:
							case 29:
							case 39:
							case 42:
							case 12:
							case 13:
							case 14:
							case 16:
							case 27:
							case 30:
							case 40:
							case 43:
							case 94:
							case 95:
							case 98:
							case 103:
							case 105:
							case 110:
							case 112:
								_ai = new L2PhantomMageAI(new AIAccessor());
								break;
							case 9:
							case 24:
							case 37:
							case 92:
							case 102:
							case 109:
							case 130:
								_ai = new L2PhantomArcherAI(new AIAccessor());
								break;
							default:
								_ai = new L2PhantomAI(new AIAccessor());
								break;
						}
					}
					else
					{
						_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
					}
				}
			}
		}
		return _ai;
	}
	
	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}
	
	@Override
	public double getLevelMod()
	{
		if (isTransformed())
		{
			double levelMod = getTransformation().getLevelMod(this);
			if (levelMod > -1)
			{
				return levelMod;
			}
		}
		return super.getLevelMod();
	}
	
	public int getNewbie()
	{
		return _newbie;
	}
	
	public void setNewbie(int newbieRewards)
	{
		_newbie = newbieRewards;
	}
	
	public void setBaseClass(int baseClass)
	{
		_baseClass = baseClass;
	}
	
	public void setBaseClass(ClassId classId)
	{
		_baseClass = classId.ordinal();
	}
	
	public boolean isInStoreMode()
	{
		return (getPrivateStoreType() > L2PcInstance.STORE_PRIVATE_NONE);
	}
	
	public boolean isInCraftMode()
	{
		return _inCraftMode;
	}
	
	public void isInCraftMode(boolean b)
	{
		_inCraftMode = b;
	}
	
	public void logout()
	{
		logout(true);
	}
	
	public void logout(boolean closeClient)
	{
		try
		{
			if (Protection.isProtectionOn())
			{
				ProtectionManager.scheduleSendPacketToClient(0, this);
			}
			LastHero.OnLogout(this);
			closeNetConnection(closeClient);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on logout(): " + e.getMessage(), e);
		}
	}
	
	public L2RecipeList[] getCommonRecipeBook()
	{
		return _commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
	}
	
	public L2RecipeList[] getDwarvenRecipeBook()
	{
		return _dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
	}
	
	public void registerCommonRecipeList(L2RecipeList recipe, boolean saveToDb)
	{
		_commonRecipeBook.put(recipe.getId(), recipe);
		
		if (saveToDb)
		{
			insertNewRecipeParser(recipe.getId(), false);
		}
	}
	
	public void registerDwarvenRecipeList(L2RecipeList recipe, boolean saveToDb)
	{
		_dwarvenRecipeBook.put(recipe.getId(), recipe);
		
		if (saveToDb)
		{
			insertNewRecipeParser(recipe.getId(), true);
		}
	}
	
	public boolean hasRecipeList(int recipeId)
	{
		return _dwarvenRecipeBook.containsKey(recipeId) || _commonRecipeBook.containsKey(recipeId);
	}
	
	public void unregisterRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.remove(recipeId) != null)
		{
			deleteRecipeParser(recipeId, true);
		}
		else if (_commonRecipeBook.remove(recipeId) != null)
		{
			deleteRecipeParser(recipeId, false);
		}
		else
		{
			_log.warning("Attempted to remove unknown RecipeList: " + recipeId);
		}
		
		for (L2ShortCut sc : getAllShortCuts())
		{
			if ((sc != null) && (sc.getId() == recipeId) && (sc.getType() == L2ShortCut.TYPE_RECIPE))
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
	}
	
	private void insertNewRecipeParser(int recipeId, boolean isDwarf)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO character_recipebook (charId, id, classIndex, type) values(?,?,?,?)"))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, recipeId);
			statement.setInt(3, isDwarf ? _classIndex : 0);
			statement.setInt(4, isDwarf ? 1 : 0);
			statement.execute();
		}
		catch (SQLException e)
		{
			if (_log.isLoggable(Level.SEVERE))
			{
				_log.log(Level.SEVERE, "SQL exception while inserting recipe: " + recipeId + " from character " + getObjectId(), e);
			}
		}
	}
	
	private void deleteRecipeParser(int recipeId, boolean isDwarf)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=? AND id=? AND classIndex=?"))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, recipeId);
			statement.setInt(3, isDwarf ? _classIndex : 0);
			statement.execute();
		}
		catch (SQLException e)
		{
			if (_log.isLoggable(Level.SEVERE))
			{
				_log.log(Level.SEVERE, "SQL exception while deleting recipe: " + recipeId + " from character " + getObjectId(), e);
			}
		}
	}
	
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}
	
	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}
	
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}
	
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuestName(), qs);
	}
	
	public boolean hasQuestState(String quest)
	{
		return _quests.containsKey(quest);
	}
	
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}
	
	private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state)
	{
		final int len = questStateArray.length;
		QuestState[] tmp = new QuestState[len + 1];
		System.arraycopy(questStateArray, 0, tmp, 0, len);
		tmp[len] = state;
		return tmp;
	}
	
	public Quest[] getAllActiveQuests()
	{
		List<Quest> quests = new ArrayList<>();
		for (QuestState qs : _quests.values())
		{
			if ((qs == null) || (qs.getQuest() == null) || (!qs.isStarted() && !Config.DEVELOPER))
			{
				continue;
			}
			final int questId = qs.getQuest().getId();
			if ((questId > 19999) || (questId < 1))
			{
				continue;
			}
			quests.add(qs.getQuest());
		}
		return quests.toArray(new Quest[quests.size()]);
	}
	
	public QuestState[] getQuestsForAttacks(L2Npc npc)
	{
		QuestState[] states = null;
		
		for (Quest quest : npc.getTemplate().getEventQuests(QuestEventType.ON_ATTACK))
		{
			if (getQuestState(quest.getName()) != null)
			{
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			}
		}
		return states;
	}
	
	public QuestState[] getQuestsForKills(L2Npc npc)
	{
		QuestState[] states = null;
		
		for (Quest quest : npc.getTemplate().getEventQuests(QuestEventType.ON_KILL))
		{
			if (getQuestState(quest.getName()) != null)
			{
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			}
		}
		return states;
	}
	
	public QuestState[] getQuestsForTalk(int npcId)
	{
		QuestState[] states = null;
		
		List<Quest> quests = NpcTable.getInstance().getTemplate(npcId).getEventQuests(QuestEventType.ON_TALK);
		if (quests != null)
		{
			for (Quest quest : quests)
			{
				if (quest != null)
				{
					if (getQuestState(quest.getName()) != null)
					{
						if (states == null)
						{
							states = new QuestState[]
							{
								getQuestState(quest.getName())
							};
						}
						else
						{
							states = addToQuestStateArray(states, getQuestState(quest.getName()));
						}
					}
				}
			}
		}
		return states;
	}
	
	public QuestState processQuestEvent(String quest, String event)
	{
		QuestState retval = null;
		if (event == null)
		{
			event = "";
		}
		QuestState qs = getQuestState(quest);
		if ((qs == null) && event.isEmpty())
		{
			return retval;
		}
		if (qs == null)
		{
			Quest q = QuestManager.getInstance().getQuest(quest);
			if (q == null)
			{
				return retval;
			}
			qs = q.newQuestState(this);
		}
		if (qs != null)
		{
			if (getLastQuestNpcObject() > 0)
			{
				L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
				if ((object instanceof L2Npc) && isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					L2Npc npc = (L2Npc) object;
					QuestState[] states = getQuestsForTalk(npc.getId());
					
					if (states != null)
					{
						for (QuestState state : states)
						{
							if (state.getQuest().getName().equals(qs.getQuest().getName()))
							{
								if (qs.getQuest().notifyEvent(event, npc, this))
								{
									showQuestWindow(quest, State.getStateName(qs.getState()));
								}
								
								retval = qs;
							}
						}
					}
				}
			}
		}
		return retval;
	}
	
	private void showQuestWindow(String questId, String stateId)
	{
		String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
		String content = HtmCache.getInstance().getHtm(getLang(), path);
		
		if (content != null)
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
		}
		
		sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private volatile List<QuestState> _notifyQuestOfDeathList;
	
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null)
		{
			return;
		}
		
		if (!getNotifyQuestOfDeath().contains(qs))
		{
			getNotifyQuestOfDeath().add(qs);
		}
	}
	
	public void removeNotifyQuestOfDeath(QuestState qs)
	{
		if ((qs == null) || (_notifyQuestOfDeathList == null))
		{
			return;
		}
		
		_notifyQuestOfDeathList.remove(qs);
	}
	
	public final List<QuestState> getNotifyQuestOfDeath()
	{
		if (_notifyQuestOfDeathList == null)
		{
			synchronized (this)
			{
				if (_notifyQuestOfDeathList == null)
				{
					_notifyQuestOfDeathList = new FastList<>();
				}
			}
		}
		return _notifyQuestOfDeathList;
	}
	
	public final boolean isNotifyQuestOfDeathEmpty()
	{
		return (_notifyQuestOfDeathList == null) || _notifyQuestOfDeathList.isEmpty();
	}
	
	public L2ShortCut[] getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}
	
	public L2ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}
	
	private L2PcTemplate createRandomAntifeedTemplate()
	{
		Race race = null;
		
		while (race == null)
		{
			race = Race.values()[Rnd.get(Race.values().length)];
			if ((race == getRace()) || (race == Race.Kamael))
			{
				race = null;
			}
		}
		
		PlayerClass p;
		for (ClassId c : ClassId.values())
		{
			p = PlayerClass.values()[c.getId()];
			if (p.isOfRace(race) && p.isOfLevel(ClassLevel.Fourth))
			{
				_antifeedTemplate = CharTemplateParser.getInstance().getTemplate(c);
				break;
			}
		}
		
		if (getRace() == Race.Kamael)
		{
			_antifeedSex = getAppearance().getSex();
		}
		
		_antifeedSex = Rnd.get(2) == 0 ? true : false;
		
		return _antifeedTemplate;
	}
	
	public void startAntifeedProtection(boolean start, boolean broadcast)
	{
		if (!start)
		{
			getAppearance().setVisibleName(getName());
			_antifeedTemplate = null;
		}
		else
		{
			getAppearance().setVisibleName("Unknown");
			createRandomAntifeedTemplate();
		}
	}
	
	public L2PcTemplate getAntifeedTemplate()
	{
		return _antifeedTemplate;
	}
	
	public boolean getAntifeedSex()
	{
		return _antifeedSex;
	}
	
	public void registerShortCut(L2ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}
	
	public void updateShortCuts(int skillId, int skillLevel)
	{
		_shortCuts.updateShortCuts(skillId, skillLevel);
	}
	
	public void registerShortCut(L2ShortCut shortcut, boolean storeToDb)
	{
		_shortCuts.registerShortCut(shortcut, storeToDb);
	}
	
	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}
	
	public void deleteShortCut(int slot, int page, boolean fromDb)
	{
		_shortCuts.deleteShortCut(slot, page, fromDb);
	}
	
	public void removeAllShortcuts()
	{
		_shortCuts.tempRemoveAll();
	}
	
	public void registerMacro(Macro macro)
	{
		_macros.registerMacro(macro);
	}
	
	public void deleteMacro(int id)
	{
		_macros.deleteMacro(id);
	}
	
	public MacroList getMacros()
	{
		return _macros;
	}
	
	public void setSiegeState(byte siegeState)
	{
		_siegeState = siegeState;
	}
	
	public byte getSiegeState()
	{
		return _siegeState;
	}
	
	public void setSiegeSide(int val)
	{
		_siegeSide = val;
	}
	
	public boolean isRegisteredOnThisSiegeField(int val)
	{
		if ((_siegeSide != val) && ((_siegeSide < 81) || (_siegeSide > 89)))
		{
			return false;
		}
		return true;
	}
	
	public int getSiegeSide()
	{
		return _siegeSide;
	}
	
	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = (byte) pvpFlag;
	}
	
	@Override
	public byte getPvpFlag()
	{
		return _pvpFlag;
	}
	
	@Override
	public void updatePvPFlag(int value)
	{
		if (getPvpFlag() == value)
		{
			return;
		}
		setPvpFlag(value);
		
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		
		if (hasSummon())
		{
			sendPacket(new RelationChanged(getSummon(), getRelation(this), false));
		}
		
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		
		for (L2PcInstance target : plrs)
		{
			target.sendPacket(new RelationChanged(this, getRelation(target), isAutoAttackable(target)));
			if (hasSummon())
			{
				target.sendPacket(new RelationChanged(getSummon(), getRelation(target), isAutoAttackable(target)));
			}
		}
	}
	
	@Override
	public void revalidateZone(boolean force)
	{
		if (getWorldRegion() == null)
		{
			return;
		}
		
		if (force)
		{
			_zoneValidateCounter = 4;
		}
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
			{
				_zoneValidateCounter = 4;
			}
			else
			{
				return;
			}
		}
		
		getWorldRegion().revalidateZones(this);
		
		if (Config.ALLOW_WATER)
		{
			checkWaterState();
		}
		
		if (isInsideZone(ZoneId.ALTERED))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.ALTEREDZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.ALTEREDZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.ALTEREDZONE);
			sendPacket(cz);
		}
		else if (isInsideZone(ZoneId.SIEGE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2);
			sendPacket(cz);
		}
		else if (isInsideZone(ZoneId.PVP))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE);
			sendPacket(cz);
		}
		else if (isIn7sDungeon())
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE);
			sendPacket(cz);
		}
		else if (isInsideZone(ZoneId.PEACE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE);
			sendPacket(cz);
		}
		else
		{
			if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
			{
				return;
			}
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				updatePvPStatus();
			}
			_lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE);
			sendPacket(cz);
		}
	}
	
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
	}
	
	public int getDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN);
	}
	
	public boolean hasCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}
	
	public int getCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}
	
	public int getPkKills()
	{
		return _pkKills;
	}
	
	public void setPkKills(int pkKills)
	{
		if (!getEvents().onPKChange(_pkKills, pkKills))
		{
			return;
		}
		_pkKills = pkKills;
	}
	
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}
	
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}
	
	public int getRecomHave()
	{
		return _recomHave;
	}
	
	protected void incRecomHave()
	{
		if (_recomHave < 255)
		{
			_recomHave++;
		}
	}
	
	public void setRecomHave(int value)
	{
		_recomHave = Math.min(Math.max(value, 0), 255);
	}
	
	public void setRecomLeft(int value)
	{
		_recomLeft = Math.min(Math.max(value, 0), 255);
	}
	
	public int getRecomLeft()
	{
		return _recomLeft;
	}
	
	protected void decRecomLeft()
	{
		if (_recomLeft > 0)
		{
			_recomLeft--;
		}
	}
	
	public void giveRecom(L2PcInstance target)
	{
		target.incRecomHave();
		decRecomLeft();
	}
	
	public void setExpBeforeDeath(long exp)
	{
		_expBeforeDeath = exp;
	}
	
	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}
	
	@Override
	public int getKarma()
	{
		return _karma;
	}
	
	public void setKarma(int karma)
	{
		if (!getEvents().onKarmaChange(_karma, karma))
		{
			return;
		}
		
		if (karma < 0)
		{
			karma = 0;
		}
		
		if ((_karma == 0) && (karma > 0))
		{
			Collection<L2Object> objs = getKnownList().getKnownObjects().values();
			
			for (L2Object object : objs)
			{
				if (!(object instanceof L2GuardInstance))
				{
					continue;
				}
				
				if (((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					((L2GuardInstance) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		else if ((_karma > 0) && (karma == 0))
		{
			setKarmaFlag(0);
		}
		
		_karma = karma;
		broadcastKarma();
	}
	
	public int getExpertiseArmorPenalty()
	{
		return _expertiseArmorPenalty;
	}
	
	public int getExpertiseWeaponPenalty()
	{
		return _expertiseWeaponPenalty;
	}
	
	public int getWeightPenalty()
	{
		if (_dietMode)
		{
			return 0;
		}
		return _curWeightPenalty;
	}
	
	public void refreshOverloaded()
	{
		int maxLoad = getMaxLoad();
		if (maxLoad > 0)
		{
			long weightproc = (((getCurrentLoad() - getBonusWeightPenalty()) * 1000) / getMaxLoad());
			int newWeightPenalty;
			if ((weightproc < 500) || _dietMode)
			{
				newWeightPenalty = 0;
			}
			else if (weightproc < 666)
			{
				newWeightPenalty = 1;
			}
			else if (weightproc < 800)
			{
				newWeightPenalty = 2;
			}
			else if (weightproc < 1000)
			{
				newWeightPenalty = 3;
			}
			else
			{
				newWeightPenalty = 4;
			}
			
			if (_curWeightPenalty != newWeightPenalty)
			{
				_curWeightPenalty = newWeightPenalty;
				if ((newWeightPenalty > 0) && !_dietMode)
				{
					addSkill(SkillHolder.getInstance().getInfo(4270, newWeightPenalty));
					setIsOverloaded(getCurrentLoad() > maxLoad);
				}
				else
				{
					removeSkill(getKnownSkill(4270), false, true);
					setIsOverloaded(false);
				}
				sendPacket(new UserInfo(this));
				sendPacket(new EtcStatusUpdate(this));
				broadcastPacket(new CharInfo(this));
				broadcastPacket(new ExBrExtraUserInfo(this));
			}
		}
	}
	
	public void refreshExpertisePenalty()
	{
		if (!Config.EXPERTISE_PENALTY)
		{
			return;
		}
		
		int level = (int) calcStat(Stats.GRADE_EXPERTISE_LEVEL, getLevel(), null, null);
		int i = 0;
		for (i = 0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (level < EXPERTISE_LEVELS[i + 1])
			{
				break;
			}
		}
		
		@SuppressWarnings("unused")
		boolean skillUpdate = false;
		
		if (expertiseIndex != i)
		{
			expertiseIndex = i;
			if (expertiseIndex > 0)
			{
				addSkill(SkillHolder.getInstance().getInfo(239, expertiseIndex), false);
				skillUpdate = true;
			}
		}
		
		int armorPenalty = 0;
		int weaponPenalty = 0;
		
		for (L2ItemInstance item : getInventory().getItems())
		{
			if ((item != null) && item.isEquipped() && ((item.getItemType() != L2EtcItemType.ARROW) && (item.getItemType() != L2EtcItemType.BOLT)))
			{
				int crystaltype = item.getItem().getCrystalType();
				
				if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
				{
					if (crystaltype > weaponPenalty)
					{
						weaponPenalty = crystaltype;
					}
				}
				else if ((item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR) || (item.getItem().getType2() == L2Item.TYPE2_ACCESSORY))
				{
					if (crystaltype > armorPenalty)
					{
						armorPenalty = crystaltype;
					}
				}
			}
		}
		
		boolean changed = false;
		
		armorPenalty = armorPenalty - expertiseIndex;
		armorPenalty = Math.min(Math.max(armorPenalty, 0), 4);
		
		if ((getExpertiseArmorPenalty() != armorPenalty) || (getSkillLevel(6213) != armorPenalty))
		{
			_expertiseArmorPenalty = armorPenalty;
			
			if (_expertiseArmorPenalty > 0)
			{
				addSkill(SkillHolder.getInstance().getInfo(6213, _expertiseArmorPenalty));
			}
			else
			{
				removeSkill(getKnownSkill(6213), false, true);
			}
			changed = true;
		}
		
		weaponPenalty = weaponPenalty - expertiseIndex;
		weaponPenalty = Math.min(Math.max(weaponPenalty, 0), 4);
		
		if ((getExpertiseWeaponPenalty() != weaponPenalty) || (getSkillLevel(6209) != weaponPenalty))
		{
			_expertiseWeaponPenalty = weaponPenalty;
			
			if (_expertiseWeaponPenalty > 0)
			{
				addSkill(SkillHolder.getInstance().getInfo(6209, _expertiseWeaponPenalty));
			}
			else
			{
				removeSkill(getKnownSkill(6209), false, true);
			}
			
			changed = true;
		}
		
		if (changed)
		{
			sendPacket(new EtcStatusUpdate(this));
		}
	}
	
	public void useEquippableItem(L2ItemInstance item, boolean abortAttack)
	{
		L2ItemInstance[] items = null;
		final boolean isEquiped = item.isEquipped();
		final int oldInvLimit = getInventoryLimit();
		SystemMessage sm = null;
		
		if (!fireEquipmentListeners(isEquiped, item))
		{
			return;
		}
		if (isEquiped)
		{
			if (item.getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item);
			}
			sendPacket(sm);
			
			int slot = getInventory().getSlotFromItem(item);
			
			if (slot == L2Item.SLOT_DECO)
			{
				items = getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			}
			else
			{
				items = getInventory().unEquipItemInBodySlotAndRecord(slot);
			}
		}
		else
		{
			items = getInventory().equipItemAndRecord(item);
			
			if (item.isEquipped())
			{
				if (item.getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
					sm.addItemName(item);
				}
				sendPacket(sm);
				
				item.decreaseMana(false);
				
				if ((item.getItem().getBodyPart() & L2Item.SLOT_MULTI_ALLWEAPON) != 0)
				{
					rechargeShots(true, true);
				}
			}
			else
			{
				sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			}
		}
		refreshExpertisePenalty();
		
		broadcastUserInfo();
		
		InventoryUpdate iu = new InventoryUpdate();
		iu.addItems(Arrays.asList(items));
		sendPacket(iu);
		
		if (abortAttack)
		{
			abortAttack();
		}
		
		if (getInventoryLimit() != oldInvLimit)
		{
			sendPacket(new ExStorageMaxCount(this));
		}
	}
	
	public int getPvpKills()
	{
		return _pvpKills;
	}
	
	public void setPvpKills(int pvpKills)
	{
		if (!getEvents().onPvPChange(_pvpKills, pvpKills))
		{
			return;
		}
		_pvpKills = pvpKills;
	}
	
	public int getFame()
	{
		return _fame;
	}
	
	public void setFame(int fame)
	{
		if (!getEvents().onFameChange(_fame, fame))
		{
			return;
		}
		_fame = (fame > Config.MAX_PERSONAL_FAME_POINTS) ? Config.MAX_PERSONAL_FAME_POINTS : fame;
	}
	
	public ClassId getClassId()
	{
		return getTemplate().getClassId();
	}
	
	public void setClassId(int Id)
	{
		if (!_subclassLock.tryLock())
		{
			return;
		}
		
		try
		{
			if ((getLvlJoinedAcademy() != 0) && (_clan != null) && (PlayerClass.values()[Id].getLevel() == ClassLevel.Third))
			{
				if (getLvlJoinedAcademy() <= 16)
				{
					_clan.addReputationScore(Config.JOIN_ACADEMY_MAX_REP_SCORE, true);
				}
				else if (getLvlJoinedAcademy() >= 39)
				{
					_clan.addReputationScore(Config.JOIN_ACADEMY_MIN_REP_SCORE, true);
				}
				else
				{
					_clan.addReputationScore((Config.JOIN_ACADEMY_MAX_REP_SCORE - ((getLvlJoinedAcademy() - 16) * 20)), true);
				}
				setLvlJoinedAcademy(0);
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
				msg.addPcName(this);
				_clan.broadcastToOnlineMembers(msg);
				_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
				_clan.removeClanMember(getObjectId(), 0);
				sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
				sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
				getInventory().addItem("Gift", 8181, 1, this, null);
			}
			if (isSubClassActive())
			{
				getSubClasses().get(_classIndex).setClassId(Id);
			}
			setTarget(this);
			broadcastPacket(new MagicSkillUse(this, 5103, 1, 1000, 0));
			setClassTemplate(Id);
			if (getClassId().level() == 3)
			{
				sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
			}
			else
			{
				sendPacket(SystemMessageId.CLASS_TRANSFER);
			}
			
			if (isInParty())
			{
				getParty().broadcastPacket(new PartySmallWindowUpdate(this));
			}
			
			if (getClan() != null)
			{
				getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
			}
			
			rewardSkills();
			
			if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && Config.DECREASE_SKILL_LEVEL)
			{
				checkPlayerSkills();
			}
		}
		finally
		{
			_subclassLock.unlock();
		}
	}
	
	private ClassId _learningClass = getClassId();
	
	public ClassId getLearningClass()
	{
		return _learningClass;
	}
	
	public void setLearningClass(ClassId learningClass)
	{
		_learningClass = learningClass;
	}
	
	public long getExp()
	{
		return getStat().getExp();
	}
	
	public void setActiveEnchantAttrItemId(int objectId)
	{
		_activeEnchantAttrItemId = objectId;
	}
	
	public int getActiveEnchantAttrItemId()
	{
		return _activeEnchantAttrItemId;
	}
	
	public void setActiveEnchantItemId(int objectId)
	{
		if (objectId == ID_NONE)
		{
			setActiveEnchantSupportItemId(ID_NONE);
			setActiveEnchantTimestamp(0);
			setIsEnchanting(false);
		}
		_activeEnchantItemId = objectId;
	}
	
	public int getActiveEnchantItemId()
	{
		return _activeEnchantItemId;
	}
	
	public void setActiveEnchantSupportItemId(int objectId)
	{
		_activeEnchantSupportItemId = objectId;
	}
	
	public int getActiveEnchantSupportItemId()
	{
		return _activeEnchantSupportItemId;
	}
	
	public long getActiveEnchantTimestamp()
	{
		return _activeEnchantTimestamp;
	}
	
	public void setActiveEnchantTimestamp(long val)
	{
		_activeEnchantTimestamp = val;
	}
	
	public void setIsEnchanting(boolean val)
	{
		_isEnchanting = val;
	}
	
	public boolean isEnchanting()
	{
		return _isEnchanting;
	}
	
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}
	
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}
	
	public L2Weapon findFistsWeaponItem(int classId)
	{
		L2Weapon weaponItem = null;
		if ((classId >= 0x00) && (classId <= 0x09))
		{
			L2Item temp = ItemHolder.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x0a) && (classId <= 0x11))
		{
			L2Item temp = ItemHolder.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x12) && (classId <= 0x18))
		{
			L2Item temp = ItemHolder.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x19) && (classId <= 0x1e))
		{
			L2Item temp = ItemHolder.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x1f) && (classId <= 0x25))
		{
			L2Item temp = ItemHolder.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x26) && (classId <= 0x2b))
		{
			L2Item temp = ItemHolder.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x2c) && (classId <= 0x30))
		{
			L2Item temp = ItemHolder.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x31) && (classId <= 0x34))
		{
			L2Item temp = ItemHolder.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x35) && (classId <= 0x39))
		{
			L2Item temp = ItemHolder.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
		}
		return weaponItem;
	}
	
	public void rewardSkills()
	{
		if (Config.AUTO_LEARN_SKILLS || isPhantome())
		{
			giveAvailableSkills(Config.AUTO_LEARN_FS_SKILLS, true);
		}
		else
		{
			giveAvailableAutoGetSkills();
		}
		
		if (Config.UNSTUCK_SKILL && (getSkillLevel((short) 1050) < 0))
		{
			addSkill(SkillHolder.getInstance().getInfo(2099, 1), false);
		}
		
		checkPlayerSkills();
		checkItemRestriction();
		sendSkillList();
	}
	
	public void regiveTemporarySkills()
	{
		if (isNoble())
		{
			setNoble(true);
		}
		
		if (isHero())
		{
			setHero(true);
		}
		
		if (getClan() != null)
		{
			L2Clan clan = getClan();
			clan.addSkillEffects(this);
			
			if ((clan.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel()) && isClanLeader())
			{
				SiegeManager.getInstance().addSiegeSkills(this);
			}
			if (getClan().getCastleId() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(getClan()).giveResidentialSkills(this);
			}
			if (getClan().getFortId() > 0)
			{
				FortManager.getInstance().getFortByOwner(getClan()).giveResidentialSkills(this);
			}
		}
		getInventory().reloadEquippedItems();
		
		restoreDeathPenaltyBuffLevel();
	}
	
	public int giveAvailableSkills(boolean includedByFs, boolean includeAutoGet)
	{
		int skillCounter = 0;
		
		Collection<L2Skill> skills = SkillTreesParser.getInstance().getAllAvailableSkills(this, getClassId(), includedByFs, includeAutoGet);
		for (L2Skill sk : skills)
		{
			if (getKnownSkill(sk.getId()) == sk)
			{
				continue;
			}
			
			if (getSkillLevel(sk.getId()) == -1)
			{
				skillCounter++;
			}
			
			if (sk.isToggle())
			{
				final L2Effect toggleEffect = getFirstEffect(sk.getId());
				if (toggleEffect != null)
				{
					toggleEffect.exit();
					sk.getEffects(this, this);
				}
			}
			addSkill(sk, true);
		}
		
		if (Config.AUTO_LEARN_SKILLS || (isPhantome() && (skillCounter > 0)))
		{
			sendMessage("You have learned " + skillCounter + " new skills");
		}
		return skillCounter;
	}
	
	public void giveAvailableAutoGetSkills()
	{
		final List<L2SkillLearn> autoGetSkills = SkillTreesParser.getInstance().getAvailableAutoGetSkills(this);
		final SkillHolder st = SkillHolder.getInstance();
		L2Skill skill;
		for (L2SkillLearn s : autoGetSkills)
		{
			skill = st.getInfo(s.getSkillId(), s.getSkillLevel());
			if (skill != null)
			{
				addSkill(skill, true);
			}
			else
			{
				_log.warning("Skipping null auto-get skill for player: " + toString());
			}
		}
	}
	
	public void setExp(long exp)
	{
		if (exp < 0)
		{
			exp = 0;
		}
		
		getStat().setExp(exp);
	}
	
	public Race getRace()
	{
		if (!isSubClassActive())
		{
			return getTemplate().getRace();
		}
		return CharTemplateParser.getInstance().getTemplate(_baseClass).getRace();
	}
	
	public L2Radar getRadar()
	{
		return _radar;
	}
	
	public boolean isMinimapAllowed()
	{
		return _minimapAllowed;
	}
	
	public void setMinimapAllowed(boolean b)
	{
		_minimapAllowed = b;
	}
	
	public int getSp()
	{
		return getStat().getSp();
	}
	
	public void setSp(int sp)
	{
		if (sp < 0)
		{
			sp = 0;
		}
		
		super.getStat().setSp(sp);
	}
	
	public boolean isCastleLord(int castleId)
	{
		L2Clan clan = getClan();
		
		if ((clan != null) && (clan.getLeader().getPlayerInstance() == this))
		{
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if ((castle != null) && (castle == CastleManager.getInstance().getCastleById(castleId)))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int getClanId()
	{
		return _clanId;
	}
	
	public int getClanCrestId()
	{
		if (_clan != null)
		{
			return _clan.getCrestId();
		}
		
		return 0;
	}
	
	public int getClanCrestLargeId()
	{
		if (_clan != null)
		{
			return _clan.getCrestLargeId();
		}
		
		return 0;
	}
	
	public long getClanJoinExpiryTime()
	{
		return _clanJoinExpiryTime;
	}
	
	public void setClanJoinExpiryTime(long time)
	{
		_clanJoinExpiryTime = time;
	}
	
	public long getClanCreateExpiryTime()
	{
		return _clanCreateExpiryTime;
	}
	
	public void setClanCreateExpiryTime(long time)
	{
		_clanCreateExpiryTime = time;
	}
	
	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}
	
	@Override
	public PcInventory getInventory()
	{
		return _inventory;
	}
	
	public void removeItemFromShortCut(int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}
	
	public boolean isSitting()
	{
		return _waitTypeSitting;
	}
	
	public void setIsSitting(boolean state)
	{
		_waitTypeSitting = state;
	}
	
	public void sitDown()
	{
		sitDown(true);
	}
	
	public void sitDown(boolean checkCast)
	{
		if (checkCast && isCastingNow())
		{
			sendMessage("Cannot sit while casting");
			return;
		}
		
		if (!_waitTypeSitting && !isAttackingDisabled() && !isOutOfControl() && !isImmobilized())
		{
			breakAttack();
			setIsSitting(true);
			getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			ThreadPoolManager.getInstance().scheduleGeneral(new SitDownTask(this), 2500);
			setIsParalyzed(true);
		}
	}
	
	public void standUp()
	{
		if (!GlobalRestrictions.canStandUp(this))
		{
		}
		else if (L2Event.isParticipant(this) && getEventStatus().eventSitForced)
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
		}
		else if (Interface.isParticipating(getObjectId()) && eventSitForced)
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
		}
		else if (_waitTypeSitting && !isInStoreMode() && !isAlikeDead())
		{
			if (_effects.isAffected(EffectFlag.RELAXING))
			{
				stopEffects(L2EffectType.RELAXING);
			}
			
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(this), 2500);
		}
	}
	
	public PcWarehouse getWarehouse()
	{
		if (_warehouse == null)
		{
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}
		if (Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().addCacheTask(this);
		}
		return _warehouse;
	}
	
	public void clearWarehouse()
	{
		if (_warehouse != null)
		{
			_warehouse.deleteMe();
		}
		_warehouse = null;
	}
	
	public PcFreight getFreight()
	{
		return _freight;
	}
	
	public boolean hasRefund()
	{
		return (_refund != null) && (_refund.getSize() > 0) && Config.ALLOW_REFUND;
	}
	
	public PcRefund getRefund()
	{
		if (_refund == null)
		{
			_refund = new PcRefund(this);
		}
		return _refund;
	}
	
	public void clearRefund()
	{
		if (_refund != null)
		{
			_refund.deleteMe();
		}
		_refund = null;
	}
	
	@Deprecated
	public int getCharId()
	{
		return _charId;
	}
	
	public long getAdena()
	{
		return _inventory.getAdena();
	}
	
	public long getAncientAdena()
	{
		return _inventory.getAncientAdena();
	}
	
	public void addAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			sm.addItemNumber(count);
			sendPacket(sm);
		}
		
		if (count > 0)
		{
			_inventory.addAdena(process, count, this, reference);
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAdenaInstance());
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	public boolean reduceAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (count > getAdena())
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}
			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance adenaItem = _inventory.getAdenaInstance();
			if (!_inventory.reduceAdena(process, count, this, reference))
			{
				return false;
			}
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(adenaItem);
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	public void addAncientAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
			sm.addItemNumber(count);
			sendPacket(sm);
		}
		
		if (count > 0)
		{
			_inventory.addAncientAdena(process, count, this, reference);
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAncientAdenaInstance());
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	public boolean reduceAncientAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (count > getAncientAdena())
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}
			
			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
			if (!_inventory.reduceAncientAdena(process, count, this, reference))
			{
				return false;
			}
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(ancientAdenaItem);
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage)
			{
				if (count > 1)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
					sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
					sm.addItemNumber(count);
					sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
					sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
					sendPacket(sm);
				}
			}
		}
		return true;
	}
	
	public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		if (item.getCount() > 0)
		{
			if (sendMessage)
			{
				if (item.getCount() > 1)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
					sm.addItemName(item);
					sm.addItemNumber(item.getCount());
					sendPacket(sm);
				}
				else if (item.getEnchantLevel() > 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item);
					sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
					sm.addItemName(item);
					sendPacket(sm);
				}
			}
			L2ItemInstance newitem = _inventory.addItem(process, item, this, reference);
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(newitem);
				sendPacket(playerIU);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
			
			if (!canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !_inventory.validateCapacity(0, item.isQuestItem()) && newitem.isDropable() && (!newitem.isStackable() || (newitem.getLastChange() != L2ItemInstance.MODIFIED)))
			{
				dropItem("InvDrop", newitem, null, true, true);
			}
			else if (CursedWeaponsManager.getInstance().isCursed(newitem.getId()))
			{
				CursedWeaponsManager.getInstance().activate(this, newitem);
			}
			else if (FortSiegeManager.getInstance().isCombat(item.getId()))
			{
				if (FortSiegeManager.getInstance().activateCombatFlag(this, item))
				{
					Fort fort = FortManager.getInstance().getFort(this);
					fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG), getName());
				}
			}
			else if ((item.getId() >= 13560) && (item.getId() <= 13568))
			{
				TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(item.getId() - 13479);
				if (ward != null)
				{
					ward.activate(this, item);
				}
			}
		}
	}
	
	public L2ItemInstance addItem(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		if (count > 0)
		{
			L2ItemInstance item = null;
			if (ItemHolder.getInstance().getTemplate(itemId) != null)
			{
				item = ItemHolder.getInstance().createDummyItem(itemId);
			}
			else
			{
				_log.log(Level.SEVERE, "Item doesn't exist so cannot be added. Item ID: " + itemId);
				return null;
			}
			if (sendMessage && ((!isCastingNow() && (item.getItemType() == L2EtcItemType.HERB)) || (item.getItemType() != L2EtcItemType.HERB)))
			{
				if (count > 1)
				{
					if (process.equalsIgnoreCase("Sweeper") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sm.addItemNumber(count);
						sendPacket(sm);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(itemId);
						sm.addItemNumber(count);
						sendPacket(sm);
					}
				}
				else
				{
					if (process.equalsIgnoreCase("Sweeper") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
					}
				}
			}
			
			if (item.getItemType() == L2EtcItemType.HERB)
			{
				final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
				if (handler == null)
				{
					_log.warning("No item handler registered for Herb ID " + item.getId() + "!");
				}
				else
				{
					handler.useItem(this, new L2ItemInstance(itemId), false);
				}
			}
			else
			{
				L2ItemInstance createdItem = _inventory.addItem(process, itemId, count, this, reference);
				
				if (!canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !_inventory.validateCapacity(0, item.isQuestItem()) && createdItem.isDropable() && (!createdItem.isStackable() || (createdItem.getLastChange() != L2ItemInstance.MODIFIED)))
				{
					dropItem("InvDrop", createdItem, null, true);
				}
				else if (CursedWeaponsManager.getInstance().isCursed(createdItem.getId()))
				{
					CursedWeaponsManager.getInstance().activate(this, createdItem);
				}
				else if (FortSiegeManager.getInstance().isCombat(createdItem.getId()))
				{
					if (FortSiegeManager.getInstance().activateCombatFlag(this, item))
					{
						Fort fort = FortManager.getInstance().getFort(this);
						fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG), getName());
					}
				}
				else if ((createdItem.getId() >= 13560) && (createdItem.getId() <= 13568))
				{
					TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(createdItem.getId() - 13479);
					if (ward != null)
					{
						ward.activate(this, createdItem);
					}
				}
				return createdItem;
			}
		}
		return null;
	}
	
	public void addItem(String process, ItemsHolder item, L2Object reference, boolean sendMessage)
	{
		addItem(process, item.getId(), item.getCount(), reference, sendMessage);
	}
	
	public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		return destroyItem(process, item, item.getCount(), reference, sendMessage);
	}
	
	public boolean destroyItem(String process, L2ItemInstance item, long count, L2Object reference, boolean sendMessage)
	{
		item = _inventory.destroyItem(process, item, count, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(item);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item);
				sendPacket(sm);
			}
		}
		return true;
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		return destroyItem(process, item, count, reference, sendMessage);
	}
	
	public boolean destroyItemWithoutTrace(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if ((item == null) || (item.getCount() < count))
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		return destroyItem(null, item, count, reference, sendMessage);
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		if (itemId == PcInventory.ADENA_ID)
		{
			return reduceAdena(process, count, reference, sendMessage);
		}
		
		L2ItemInstance item = _inventory.getItemByItemId(itemId);
		
		if ((item == null) || (item.getCount() < count) || (_inventory.destroyItemByItemId(process, itemId, count, this, reference) == null))
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(itemId);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(itemId);
				sendPacket(sm);
			}
		}
		return true;
	}
	
	public L2ItemInstance transferItem(String process, int objectId, long count, Inventory target, L2Object reference)
	{
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null)
		{
			return null;
		}
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
		{
			return null;
		}
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			
			if ((oldItem.getCount() > 0) && (oldItem != newItem))
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
			
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		StatusUpdate playerSU = new StatusUpdate(this);
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);
		
		if (target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory) target).getOwner();
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
				
				targetPlayer.sendPacket(playerIU);
			}
			else
			{
				targetPlayer.sendPacket(new ItemList(targetPlayer, false));
			}
			
			playerSU = new StatusUpdate(targetPlayer);
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();
			
			if (newItem.getCount() > count)
			{
				petIU.addModifiedItem(newItem);
			}
			else
			{
				petIU.addNewItem(newItem);
			}
			
			((PetInventory) target).getOwner().sendPacket(petIU);
		}
		return newItem;
	}
	
	public boolean exchangeItemsById(String process, L2Object reference, int coinId, long cost, int rewardId, long count, boolean sendMessage)
	{
		final PcInventory inv = getInventory();
		if (!inv.validateCapacityByItemId(rewardId, count))
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.SLOTS_FULL);
			}
			return false;
		}
		
		if (!inv.validateWeightByItemId(rewardId, count))
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			}
			return false;
		}
		
		if (destroyItemByItemId(process, coinId, cost, reference, sendMessage))
		{
			addItem(process, rewardId, count, reference, sendMessage);
			return true;
		}
		return false;
	}
	
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage, boolean protectItem)
	{
		item = _inventory.dropItem(process, item, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		item.dropMe(this, (getX() + Rnd.get(50)) - 25, (getY() + Rnd.get(50)) - 25, getZ() + 20);
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getId()))
		{
			if ((item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM) || !item.isEquipable())
			{
				ItemsAutoDestroy.getInstance().addItem(item);
			}
		}
		
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if (!item.isEquipable() || (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM))
			{
				item.setProtected(false);
			}
			else
			{
				item.setProtected(true);
			}
		}
		else
		{
			item.setProtected(true);
		}
		
		if (protectItem)
		{
			item.getDropProtection().protect(this);
		}
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item);
			sendPacket(sm);
		}
		return true;
	}
	
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		return dropItem(process, item, reference, sendMessage, false);
	}
	
	public L2ItemInstance dropItem(String process, int objectId, long count, int x, int y, int z, L2Object reference, boolean sendMessage, boolean protectItem)
	{
		L2ItemInstance invitem = _inventory.getItemByObjectId(objectId);
		L2ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return null;
		}
		
		item.dropMe(this, x, y, z);
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getId()))
		{
			if ((item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM) || !item.isEquipable())
			{
				ItemsAutoDestroy.getInstance().addItem(item);
			}
		}
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if (!item.isEquipable() || (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM))
			{
				item.setProtected(false);
			}
			else
			{
				item.setProtected(true);
			}
		}
		else
		{
			item.setProtected(true);
		}
		
		if (protectItem)
		{
			item.getDropProtection().protect(this);
		}
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(invitem);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item);
			sendPacket(sm);
		}
		return item;
	}
	
	public L2ItemInstance checkItemManipulation(int objectId, long count, String action)
	{
		if (L2World.getInstance().findObject(objectId) == null)
		{
			_log.finest(getObjectId() + ": player tried to " + action + " item not available in L2World");
			return null;
		}
		
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if ((item == null) || (item.getOwnerId() != getObjectId()))
		{
			_log.finest(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}
		
		if ((count < 0) || ((count > 1) && !item.isStackable()))
		{
			_log.finest(getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}
		
		if (count > item.getCount())
		{
			_log.finest(getObjectId() + ": player tried to " + action + " more items than he owns");
			return null;
		}
		
		if ((hasSummon() && (getSummon().getControlObjectId() == objectId)) || (getMountObjectID() == objectId))
		{
			if (Config.DEBUG)
			{
				_log.finest(getObjectId() + ": player tried to " + action + " item controling pet");
			}
			
			return null;
		}
		
		if (getActiveEnchantItemId() == objectId)
		{
			if (Config.DEBUG)
			{
				_log.finest(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}
			return null;
		}
		
		if (item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow()))
		{
			return null;
		}
		
		return item;
	}
	
	public void setProtection(boolean protect)
	{
		if (Config.DEVELOPER && (protect || (_protectEndTime > 0)))
		{
			_log.warning(getName() + ": Protection " + (protect ? "ON " + (GameTimeController.getInstance().getGameTicks() + (Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND)) : "OFF") + " (currently " + GameTimeController.getInstance().getGameTicks() + ")");
		}
		_protectEndTime = protect ? GameTimeController.getInstance().getGameTicks() + (Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND) : 0;
	}
	
	public void setTeleportProtection(boolean protect)
	{
		if (Config.DEVELOPER && (protect || (_teleportProtectEndTime > 0)))
		{
			_log.warning(getName() + ": Tele Protection " + (protect ? "ON " + (GameTimeController.getInstance().getGameTicks() + (Config.PLAYER_TELEPORT_PROTECTION * GameTimeController.TICKS_PER_SECOND)) : "OFF") + " (currently " + GameTimeController.getInstance().getGameTicks() + ")");
		}
		_teleportProtectEndTime = protect ? GameTimeController.getInstance().getGameTicks() + (Config.PLAYER_TELEPORT_PROTECTION * GameTimeController.TICKS_PER_SECOND) : 0;
	}
	
	public void setRecentFakeDeath(boolean protect)
	{
		_recentFakeDeathEndTime = protect ? GameTimeController.getInstance().getGameTicks() + (Config.PLAYER_FAKEDEATH_UP_PROTECTION * GameTimeController.TICKS_PER_SECOND) : 0;
	}
	
	public boolean isRecentFakeDeath()
	{
		return _recentFakeDeathEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public final boolean isFakeDeath()
	{
		return _isFakeDeath;
	}
	
	public final void setIsFakeDeath(boolean value)
	{
		_isFakeDeath = value;
	}
	
	@Override
	public final boolean isAlikeDead()
	{
		return super.isAlikeDead() || isFakeDeath();
	}
	
	public L2GameClient getClient()
	{
		return _client;
	}
	
	public void setClient(L2GameClient client)
	{
		_client = client;
	}
	
	public String getIPAddress()
	{
		String ip = "N/A";
		if ((_client != null) && (_client.getConnectionAddress() != null))
		{
			ip = _client.getConnectionAddress().getHostAddress();
		}
		return ip;
	}
	
	private void closeNetConnection(boolean closeClient)
	{
		L2GameClient client = _client;
		
		if (client != null)
		{
			if (Protection.isProtectionOn())
			{
				ProtectionManager.scheduleSendPacketToClient(0, this);
			}
			
			if (client.isDetached())
			{
				client.cleanMe(true);
			}
			else
			{
				if (!client.getConnection().isClosed())
				{
					if (closeClient)
					{
						client.close(LeaveWorld.STATIC_PACKET);
					}
					else
					{
						client.close(ServerClose.STATIC_PACKET);
					}
				}
			}
		}
	}
	
	public Location getCurrentSkillWorldPosition()
	{
		return _currentSkillWorldPosition;
	}
	
	public void setCurrentSkillWorldPosition(Location worldPosition)
	{
		_currentSkillWorldPosition = worldPosition;
	}
	
	@Override
	public void enableSkill(L2Skill skill)
	{
		super.enableSkill(skill);
		_reuseTimeStampsSkills.remove(skill.getReuseHashCode());
	}
	
	@Override
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if (!super.checkDoCastConditions(skill))
		{
			return false;
		}
		
		if ((skill.getSkillType() == L2SkillType.SUMMON) && (hasSummon() || isMounted() || inObserverMode()))
		{
			sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			return false;
		}
		
		if (isInOlympiadMode() && (skill.isHeroSkill() || (skill.getSkillType() == L2SkillType.RESURRECT)))
		{
			sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}
		
		if (((getCharges() < skill.getChargeConsume())) || (isInAirShip() && !skill.hasEffectType(L2EffectType.REFUEL_AIRSHIP)))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
			sendPacket(sm);
			return false;
		}
		return true;
	}
	
	private boolean needCpUpdate()
	{
		double currentCp = getCurrentCp();
		
		if ((currentCp <= 1.0) || (getMaxCp() < MAX_HP_BAR_PX))
		{
			return true;
		}
		
		if ((currentCp <= _cpUpdateDecCheck) || (currentCp >= _cpUpdateIncCheck))
		{
			if (currentCp == getMaxCp())
			{
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean needMpUpdate()
	{
		double currentMp = getCurrentMp();
		
		if ((currentMp <= 1.0) || (getMaxMp() < MAX_HP_BAR_PX))
		{
			return true;
		}
		
		if ((currentMp <= _mpUpdateDecCheck) || (currentMp >= _mpUpdateIncCheck))
		{
			if (currentMp == getMaxMp())
			{
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
		sendPacket(su);
		
		final boolean needCpUpdate = needCpUpdate();
		final boolean needHpUpdate = needHpUpdate();
		
		if (isInParty() && (needCpUpdate || needHpUpdate || needMpUpdate()))
		{
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		}
		
		if (isInOlympiadMode() && isOlympiadStart() && (needCpUpdate || needHpUpdate))
		{
			final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(getOlympiadGameId());
			if ((game != null) && game.isBattleStarted())
			{
				game.getZone().broadcastStatusUpdate(this);
			}
		}
		
		if (isInDuel() && (needCpUpdate || needHpUpdate))
		{
			DuelManager.getInstance().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));
		}
	}
	
	public final void broadcastUserInfo()
	{
		sendPacket(new UserInfo(this));
		broadcastPacket(new CharInfo(this));
		broadcastPacket(new ExBrExtraUserInfo(this));
		if (TerritoryWarManager.getInstance().isTWInProgress() && (TerritoryWarManager.getInstance().checkIsRegistered(-1, getObjectId()) || TerritoryWarManager.getInstance().checkIsRegistered(-1, getClan())))
		{
			broadcastPacket(new ExDominionWarStart(this));
		}
	}
	
	public final void broadcastTitleInfo()
	{
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		broadcastPacket(new NicknameChanged(this));
	}
	
	@Override
	public final void broadcastPacket(L2GameServerPacket mov)
	{
		if (!(mov instanceof CharInfo))
		{
			sendPacket(mov);
		}
		
		mov.setInvisible(isInvisible());
		
		final Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if ((player == null) || !isVisibleFor(player))
			{
				continue;
			}
			player.sendPacket(mov);
			if (mov instanceof CharInfo)
			{
				int relation = getRelation(player);
				Integer oldrelation = getKnownList().getKnownRelations().get(player.getObjectId());
				if ((oldrelation != null) && (oldrelation != relation))
				{
					player.sendPacket(new RelationChanged(this, relation, isAutoAttackable(player)));
					if (hasSummon())
					{
						player.sendPacket(new RelationChanged(getSummon(), relation, isAutoAttackable(player)));
					}
				}
			}
		}
	}
	
	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		if (!(mov instanceof CharInfo))
		{
			sendPacket(mov);
		}
		
		mov.setInvisible(isInvisible());
		
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player == null)
			{
				continue;
			}
			if (isInsideRadius(player, radiusInKnownlist, false, false))
			{
				player.sendPacket(mov);
				if (mov instanceof CharInfo)
				{
					int relation = getRelation(player);
					Integer oldrelation = getKnownList().getKnownRelations().get(player.getObjectId());
					if ((oldrelation != null) && (oldrelation != relation))
					{
						player.sendPacket(new RelationChanged(this, relation, isAutoAttackable(player)));
						if (hasSummon())
						{
							player.sendPacket(new RelationChanged(getSummon(), relation, isAutoAttackable(player)));
						}
					}
				}
			}
		}
	}
	
	@Override
	public int getAllyId()
	{
		if (_clan == null)
		{
			return 0;
		}
		return _clan.getAllyId();
	}
	
	public int getAllyCrestId()
	{
		if (getClanId() == 0)
		{
			return 0;
		}
		if (getClan().getAllyId() == 0)
		{
			return 0;
		}
		return getClan().getAllyCrestId();
	}
	
	public void queryGameGuard()
	{
		if (getClient() != null)
		{
			getClient().setGameGuardOk(false);
			sendPacket(new GameGuardQuery());
		}
		
		if (Config.GAMEGUARD_ENFORCE)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardCheckTask(this), 30 * 1000);
		}
	}
	
	@Override
	public void sendPacket(L2GameServerPacket packet)
	{
		if (_client != null)
		{
			_client.sendPacket(packet);
		}
	}
	
	@Override
	public void sendPacket(SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}
	
	public void doInteract(L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			L2PcInstance temp = (L2PcInstance) target;
			sendPacket(ActionFailed.STATIC_PACKET);
			
			if ((temp.getPrivateStoreType() == STORE_PRIVATE_SELL) || (temp.getPrivateStoreType() == STORE_PRIVATE_PACKAGE_SELL))
			{
				sendPacket(new PrivateStoreListSell(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
			{
				sendPacket(new PrivateStoreListBuy(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
			{
				sendPacket(new RecipeShopSellList(this, temp));
			}
			
		}
		else
		{
			if (target != null)
			{
				target.onAction(this);
			}
		}
	}
	
	public void doAutoLoot(L2Attackable target, ItemsHolder item)
	{
		if (isInParty() && (ItemHolder.getInstance().getTemplate(item.getId()).getItemType() != L2EtcItemType.HERB))
		{
			getParty().distributeItem(this, item, false, target);
		}
		else if (item.getId() == PcInventory.ADENA_ID)
		{
			addAdena("Loot", item.getCount(), target, true);
		}
		else
		{
			addItem("Loot", item, target, true);
		}
	}
	
	protected void doPickupItem(L2Object object)
	{
		if (isAlikeDead() || isFakeDeath())
		{
			return;
		}
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		if (!(object instanceof L2ItemInstance))
		{
			_log.warning(this + " trying to pickup wrong target." + getTarget());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		sendPacket(ActionFailed.STATIC_PACKET);
		
		StopMove sm = new StopMove(this);
		sendPacket(sm);
		
		SystemMessage smsg = null;
		synchronized (target)
		{
			if (!target.isVisible())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!target.getDropProtection().tryPickUp(this))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
				return;
			}
			
			if (((isInParty() && (getParty().getLootDistribution() == L2Party.ITEM_LOOTER)) || !isInParty()) && !_inventory.validateCapacity(target))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.SLOTS_FULL);
				return;
			}
			
			if (isInvul() && !canOverrideCond(PcCondOverride.ITEM_CONDITIONS))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
				return;
			}
			
			if ((target.getOwnerId() != 0) && (target.getOwnerId() != getObjectId()) && !isInLooterParty(target.getOwnerId()))
			{
				if (target.getId() == PcInventory.ADENA_ID)
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addItemNumber(target.getCount());
				}
				else if (target.getCount() > 1)
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target);
					smsg.addItemNumber(target.getCount());
				}
				else
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target);
				}
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(smsg);
				return;
			}
			
			if (FortSiegeManager.getInstance().isCombat(target.getId()))
			{
				if (!FortSiegeManager.getInstance().checkIfCanPickup(this))
				{
					return;
				}
			}
			
			if ((target.getItemLootShedule() != null) && ((target.getOwnerId() == getObjectId()) || isInLooterParty(target.getOwnerId())))
			{
				target.resetOwnerTimer();
			}
			
			target.pickupMe(this);
			if (Config.SAVE_DROPPED_ITEM)
			{
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
		}
		
		if (target.getItemType() == L2EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
			if (handler == null)
			{
				_log.warning("No item handler registered for item ID: " + target.getId() + ".");
			}
			else
			{
				handler.useItem(this, target, false);
			}
			ItemHolder.getInstance().destroyItem("Consume", target, this, null);
		}
		else if (CursedWeaponsManager.getInstance().isCursed(target.getId()))
		{
			addItem("Pickup", target, null, true);
		}
		else if (FortSiegeManager.getInstance().isCombat(target.getId()))
		{
			addItem("Pickup", target, null, true);
		}
		else
		{
			if ((target.getItemType() instanceof L2ArmorType) || (target.getItemType() instanceof L2WeaponType))
			{
				if (target.getEnchantLevel() > 0)
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2_S3);
					smsg.addPcName(this);
					smsg.addNumber(target.getEnchantLevel());
					smsg.addItemName(target.getId());
					broadcastPacket(smsg, 1400);
				}
				else
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2);
					smsg.addPcName(this);
					smsg.addItemName(target.getId());
					broadcastPacket(smsg, 1400);
				}
			}
			
			if (isInParty())
			{
				getParty().distributeItem(this, target);
			}
			else if ((target.getId() == PcInventory.ADENA_ID) && (getInventory().getAdenaInstance() != null))
			{
				addAdena("Pickup", target.getCount(), null, true);
				ItemHolder.getInstance().destroyItem("Pickup", target, this, null);
			}
			else
			{
				addItem("Pickup", target, null, true);
				final L2ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				if (weapon != null)
				{
					final L2EtcItem etcItem = target.getEtcItem();
					if (etcItem != null)
					{
						final L2EtcItemType itemType = etcItem.getItemType();
						if ((weapon.getItemType() == L2WeaponType.BOW) && (itemType == L2EtcItemType.ARROW))
						{
							checkAndEquipArrows();
						}
						else if ((weapon.getItemType() == L2WeaponType.CROSSBOW) && (itemType == L2EtcItemType.BOLT))
						{
							checkAndEquipBolts();
						}
					}
				}
			}
		}
	}
	
	public boolean canOpenPrivateStore()
	{
		return !isAlikeDead() && !isInOlympiadMode() && !isMounted() && !isInsideZone(ZoneId.NO_STORE) && !isCastingNow();
	}
	
	public void tryOpenPrivateBuyStore()
	{
		if (canOpenPrivateStore())
		{
			if ((getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY) || (getPrivateStoreType() == (L2PcInstance.STORE_PRIVATE_BUY + 1)))
			{
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			}
			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
			{
				if (isSitting())
				{
					standUp();
				}
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY + 1);
				sendPacket(new PrivateStoreManageListBuy(this));
			}
		}
		else
		{
			if (isInsideZone(ZoneId.NO_STORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			}
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public void tryOpenPrivateSellStore(boolean isPackageSale)
	{
		if (canOpenPrivateStore())
		{
			if ((getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL) || (getPrivateStoreType() == (L2PcInstance.STORE_PRIVATE_SELL + 1)) || (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL))
			{
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			}
			
			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
			{
				if (isSitting())
				{
					standUp();
				}
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL + 1);
				sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
			}
		}
		else
		{
			if (isInsideZone(ZoneId.NO_STORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			}
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public final PreparedListContainer getMultiSell()
	{
		return _currentMultiSell;
	}
	
	public final void setMultiSell(PreparedListContainer list)
	{
		_currentMultiSell = list;
	}
	
	@Override
	public boolean isTransformed()
	{
		return (_transformation != null) && !_transformation.isStance();
	}
	
	public boolean isInStance()
	{
		return (_transformation != null) && _transformation.isStance();
	}
	
	public void transform(Transform transformation)
	{
		if (_transformation != null)
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			sendPacket(msg);
			return;
		}
		
		if (!fireTransformListeners(transformation, true))
		{
			return;
		}
		
		setQueuedSkill(null, false, false);
		if (isMounted())
		{
			dismount();
		}
		_transformation = transformation;
		stopAllToggles();
		transformation.onTransform(this);
		sendSkillList();
		sendPacket(new SkillCoolTime(this));
		broadcastUserInfo();
	}
	
	@Override
	public void untransform()
	{
		if (_transformation != null)
		{
			if (!fireTransformListeners(_transformation, false))
			{
				return;
			}
			setQueuedSkill(null, false, false);
			_transformation.onUntransform(this);
			_transformation = null;
			stopEffects(L2EffectType.TRANSFORMATION);
			sendSkillList();
			sendPacket(new SkillCoolTime(this));
			broadcastUserInfo();
		}
	}
	
	@Override
	public Transform getTransformation()
	{
		return _transformation;
	}
	
	public int getTransformationId()
	{
		return (isTransformed() ? getTransformation().getId() : 0);
	}
	
	@Override
	public void setTarget(L2Object newTarget)
	{
		if (newTarget != null)
		{
			boolean isInParty = (newTarget.isPlayer() && isInParty() && getParty().containsPlayer(newTarget.getActingPlayer()));
			
			if (!isInParty && (Math.abs(newTarget.getZ() - getZ()) > 1000))
			{
				newTarget = null;
			}
			
			if ((newTarget != null) && !isInParty && !newTarget.isVisible())
			{
				newTarget = null;
			}
			
			if (!isGM() && (newTarget instanceof L2Vehicle))
			{
				newTarget = null;
			}
		}
		
		if (newTarget instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) newTarget;
			if (!Interface.canTargetPlayer(getObjectId(), player.getObjectId()))
			{
				return;
			}
		}
		
		L2Object oldTarget = getTarget();
		
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
			{
				if ((newTarget != null) && (newTarget.getObjectId() != getObjectId()))
				{
					sendPacket(new ValidateLocation(newTarget));
				}
				return;
			}
			oldTarget.removeStatusListener(this);
		}
		
		if (newTarget instanceof L2Character)
		{
			final L2Character target = (L2Character) newTarget;
			
			if (newTarget.getObjectId() != getObjectId())
			{
				sendPacket(new ValidateLocation(target));
			}
			
			sendPacket(new MyTargetSelected(this, target));
			target.addStatusListener(this);
			
			final StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.MAX_HP, target.getMaxHp());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			sendPacket(su);
			Broadcast.toKnownPlayers(this, new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ()));
		}
		
		if ((newTarget == null) && (getTarget() != null))
		{
			broadcastPacket(new TargetUnselected(this));
		}
		super.setTarget(newTarget);
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		
		if (weapon == null)
		{
			return getFistsWeaponItem();
		}
		
		return (L2Weapon) weapon.getItem();
	}
	
	public L2ItemInstance getChestArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}
	
	public L2ItemInstance getLegsArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
	}
	
	public L2Armor getActiveChestArmorItem()
	{
		L2ItemInstance armor = getChestArmorInstance();
		
		if (armor == null)
		{
			return null;
		}
		
		return (L2Armor) armor.getItem();
	}
	
	public L2Armor getActiveLegsArmorItem()
	{
		L2ItemInstance legs = getLegsArmorInstance();
		
		if (legs == null)
		{
			return null;
		}
		
		return (L2Armor) legs.getItem();
	}
	
	public boolean isWearingHeavyArmor()
	{
		L2ItemInstance legs = getLegsArmorInstance();
		L2ItemInstance armor = getChestArmorInstance();
		
		if ((armor != null) && (legs != null))
		{
			if (((L2ArmorType) legs.getItemType() == L2ArmorType.HEAVY) && ((L2ArmorType) armor.getItemType() == L2ArmorType.HEAVY))
			{
				return true;
			}
		}
		if (armor != null)
		{
			if (((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR) && ((L2ArmorType) armor.getItemType() == L2ArmorType.HEAVY)))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isWearingLightArmor()
	{
		L2ItemInstance legs = getLegsArmorInstance();
		L2ItemInstance armor = getChestArmorInstance();
		
		if ((armor != null) && (legs != null))
		{
			if (((L2ArmorType) legs.getItemType() == L2ArmorType.LIGHT) && ((L2ArmorType) armor.getItemType() == L2ArmorType.LIGHT))
			{
				return true;
			}
		}
		if (armor != null)
		{
			if (((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR) && ((L2ArmorType) armor.getItemType() == L2ArmorType.LIGHT)))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isWearingMagicArmor()
	{
		L2ItemInstance legs = getLegsArmorInstance();
		L2ItemInstance armor = getChestArmorInstance();
		
		if ((armor != null) && (legs != null))
		{
			if (((L2ArmorType) legs.getItemType() == L2ArmorType.MAGIC) && ((L2ArmorType) armor.getItemType() == L2ArmorType.MAGIC))
			{
				return true;
			}
		}
		if (armor != null)
		{
			if (((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR) && ((L2ArmorType) armor.getItemType() == L2ArmorType.MAGIC)))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isMarried()
	{
		return _married;
	}
	
	public void setMarried(boolean state)
	{
		_married = state;
	}
	
	public boolean isEngageRequest()
	{
		return _engagerequest;
	}
	
	public void setEngageRequest(boolean state, int playerid)
	{
		_engagerequest = state;
		_engageid = playerid;
	}
	
	public void setMarryRequest(boolean state)
	{
		_marryrequest = state;
	}
	
	public boolean isMarryRequest()
	{
		return _marryrequest;
	}
	
	public void setMarryAccepted(boolean state)
	{
		_marryaccepted = state;
	}
	
	public boolean isMarryAccepted()
	{
		return _marryaccepted;
	}
	
	public int getEngageId()
	{
		return _engageid;
	}
	
	public int getPartnerId()
	{
		return _partnerId;
	}
	
	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}
	
	public int getCoupleId()
	{
		return _coupleId;
	}
	
	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}
	
	public void scriptAswer(int answer)
	{
		switch (CurrentConfirmDialog)
		{
			case None:
				_log.log(Level.WARNING, "Character " + getName() + " send ConfirmDlg answer for unknown ConfirmDlg");
				break;
			case Wedding:
				if (Config.ALLOW_WEDDING)
				{
					engageAnswer(answer);
				}
				CurrentConfirmDialog = ConfirmDialogScripts.None;
				break;
			case LastHero:
				LastHero.OnConfirmDlgAnswer(this, answer);
				CurrentConfirmDialog = ConfirmDialogScripts.None;
				break;
			case Tvt:
				if (answer == 1)
				{
					TvTEvent.onBypass("tvt_event_participation", this);
				}
				break;
			case TvTRound:
				if (answer == 1)
				{
					TvTRoundEvent.onBypass("tvt_round_event_participation", this);
				}
				break;
			case MonsterRush:
				MonsterRush.OnConfirmDlgAnswer(this, answer);
				CurrentConfirmDialog = ConfirmDialogScripts.None;
				break;
			default:
				break;
		}
	}
	
	public void engageAnswer(int answer)
	{
		if (!_engagerequest)
		{
			return;
		}
		else if (_engageid == 0)
		{
			return;
		}
		else
		{
			L2PcInstance ptarget = L2World.getInstance().getPlayer(_engageid);
			setEngageRequest(false, 0);
			if (ptarget != null)
			{
				if (answer == 1)
				{
					CoupleManager.getInstance().createCouple(ptarget, L2PcInstance.this);
					ptarget.sendMessage("Request to Engage has been >ACCEPTED<");
				}
				else
				{
					ptarget.sendMessage("Request to Engage has been >DENIED<!");
				}
			}
		}
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	
	@Override
	public L2Item getSecondaryWeaponItem()
	{
		L2ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (item != null)
		{
			return item.getItem();
		}
		return null;
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (killer != null)
		{
			if (!GlobalRestrictions.playerKilled(killer, this))
			{
				return false;
			}
		}
		
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (isMounted())
		{
			stopFeed();
		}
		synchronized (this)
		{
			if (isFakeDeath())
			{
				stopFakeDeath(true);
			}
		}
		
		if (killer != null)
		{
			final L2PcInstance pk = killer.getActingPlayer();
			if (pk != null)
			{
				pk.getEvents().onPvPKill(this);
				
				TvTEvent.onKill(killer, this);
				TvTRoundEvent.onKill(killer, this);
				LastHero.onKill(killer, this);
				
				if (Interface.isParticipating(getObjectId()) && Interface.isParticipating(pk.getObjectId()))
				{
					Interface.onKill(getObjectId(), pk.getObjectId());
					Interface.onDie(getObjectId(), pk.getObjectId());
				}
				
				if (L2Event.isParticipant(pk))
				{
					pk.getEventStatus().kills.add(this);
				}
				
				if ((killer instanceof L2PcInstance) && isInsideZone(ZoneId.PVP) && !isInSiege() && Config.RANK_ARENA_ENABLED)
				{
					L2PcInstance k = (L2PcInstance) killer;
					String killIp = k.getClient().getConnection().getInetAddress().getHostAddress();
					String DeathIp = getClient().getConnection().getInetAddress().getHostAddress();
					if (!killIp.equals(DeathIp) || Config.RANK_ARENA_ACCEPT_SAME_IP || (!killer.isGM() && !isGM()))
					{
						ArenaLeaderboard.getInstance().onKill(killer.getObjectId(), killer.getName());
						ArenaLeaderboard.getInstance().onDeath(getObjectId(), getName());
					}
				}
				
				if (Config.RANK_ARENA_ENABLED && (killer instanceof L2PcInstance) && isInsideZone(ZoneId.PVP) && !isInSiege() && !TvTEvent.isPlayerParticipant(getObjectId()))
				{
					if (!killer.isGM() && !isGM())
					{
						ArenaLeaderboard.getInstance().onKill(killer.getObjectId(), killer.getName());
						ArenaLeaderboard.getInstance().onDeath(getObjectId(), getName());
					}
				}
				
				if (Config.RANK_TVT_ENABLED && (killer instanceof L2PcInstance) && TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(getObjectId()))
				{
					if (!killer.isGM() && !isGM())
					{
						TvTLeaderboard.getInstance().onKill(killer.getObjectId(), killer.getName());
						TvTLeaderboard.getInstance().onDeath(getObjectId(), getName());
					}
				}
				
				if (Config.ANNOUNCE_PK_PVP && !pk.isGM())
				{
					String msg = "";
					if (getPvpFlag() == 0)
					{
						msg = Config.ANNOUNCE_PK_MSG.replace("$killer", pk.getName()).replace("$target", getName());
						if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
							sm.addString(msg);
							Announcements.getInstance().announceToAll(sm);
						}
						else
						{
							Announcements.getInstance().announceToAll(msg);
						}
					}
					else if (getPvpFlag() != 0)
					{
						msg = Config.ANNOUNCE_PVP_MSG.replace("$killer", pk.getName()).replace("$target", getName());
						if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
							sm.addString(msg);
							Announcements.getInstance().announceToAll(sm);
						}
						else
						{
							Announcements.getInstance().announceToAll(msg);
						}
					}
				}
				
				if (Config.HITMAN_ENABLE_EVENT)
				{
					Hitman.getInstance().onDeath(pk, this);
				}
			}
			
			broadcastStatusUpdate();
			setExpBeforeDeath(0);
			
			if (isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().drop(_cursedWeaponEquippedId, killer);
			}
			else if (isCombatFlagEquipped())
			{
				if (TerritoryWarManager.getInstance().isTWInProgress())
				{
					TerritoryWarManager.getInstance().dropCombatFlag(this, true, false);
				}
				else
				{
					Fort fort = FortManager.getInstance().getFort(this);
					if (fort != null)
					{
						FortSiegeManager.getInstance().dropCombatFlag(this, fort.getId());
					}
					else
					{
						int slot = getInventory().getSlotFromItem(getInventory().getItemByItemId(9819));
						getInventory().unEquipItemInBodySlot(slot);
						destroyItem("CombatFlag", getInventory().getItemByItemId(9819), null, true);
					}
				}
			}
			else
			{
				if ((pk == null) || !pk.isCursedWeaponEquipped())
				{
					onDieDropItem(killer);
					
					if (!(isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE)))
					{
						if ((pk != null) && (pk.getClan() != null) && (getClan() != null) && !isAcademyMember() && !(pk.isAcademyMember()))
						{
							if ((_clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(_clan.getId())) || (isInSiege() && pk.isInSiege()))
							{
								if (AntiFeedManager.getInstance().check(killer, this))
								{
									if (getClan().getReputationScore() > 0)
									{
										pk.getClan().addReputationScore(Config.REPUTATION_SCORE_PER_KILL, false);
									}
									
									if (pk.getClan().getReputationScore() > 0)
									{
										_clan.takeReputationScore(Config.REPUTATION_SCORE_PER_KILL, false);
									}
								}
							}
						}
					}
					
					if (Config.ALT_GAME_DELEVEL)
					{
						if (!isLucky())
						{
							final boolean siegeNpc = (killer instanceof L2DefenderInstance) || (killer instanceof L2FortCommanderInstance);
							final boolean atWar = (pk != null) && (getClan() != null) && (getClan().isAtWarWith(pk.getClanId()));
							deathPenalty(atWar, (pk != null), siegeNpc);
						}
					}
					else if (!(isInsideZone(ZoneId.PVP) && !isInSiege()) || (pk == null))
					{
						onDieUpdateKarma();
					}
				}
			}
		}
		
		if (!_cubics.isEmpty())
		{
			for (L2CubicInstance cubic : _cubics)
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
		}
		
		if (_fusionSkill != null)
		{
			abortCast();
		}
		
		for (L2Character character : getKnownList().getKnownCharacters())
		{
			if ((character.getFusionSkill() != null) && (character.getFusionSkill().getTarget() == this))
			{
				character.abortCast();
			}
		}
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			getParty().getDimensionalRift().getDeadMemberList().add(this);
		}
		
		if (getAgathionId() != 0)
		{
			setAgathionId(0);
		}
		
		if ((killer != null) && (getParty() != null) && (getParty().getUCState() instanceof UCTeam))
		{
			((UCTeam) getParty().getUCState()).onKill(this, killer.getActingPlayer());
		}
		
		calculateDeathPenaltyBuffLevel(killer);
		
		stopRentPet();
		stopWaterTask();
		
		AntiFeedManager.getInstance().setLastDeathTime(getObjectId());
		if (Config.TW_AUTO_RES && isInTownWarEvent())
		{
			reviveRequest(this, null, false);
		}
		else if (isPhoenixBlessed() || (isAffected(EffectFlag.CHARM_OF_COURAGE) && isInSiege()))
		{
			reviveRequest(this, null, false);
		}
		return true;
	}
	
	private void onDieDropItem(L2Character killer)
	{
		if (L2Event.isParticipant(this) || (killer == null))
		{
			return;
		}
		
		L2PcInstance pk = killer.getActingPlayer();
		if ((getKarma() <= 0) && (pk != null) && (pk.getClan() != null) && (getClan() != null) && (pk.getClan().isAtWarWith(getClanId())))
		{
			return;
		}
		
		if ((!isInsideZone(ZoneId.PVP) || (pk == null)) && (!isGM() || Config.KARMA_DROP_GM))
		{
			boolean isKarmaDrop = false;
			boolean isKillerNpc = (killer instanceof L2Npc);
			int pkLimit = Config.KARMA_PK_LIMIT;
			
			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;
			
			if ((getKarma() > 0) && (getPkKills() >= pkLimit))
			{
				isKarmaDrop = true;
				dropPercent = Config.KARMA_RATE_DROP;
				dropEquip = Config.KARMA_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.KARMA_RATE_DROP_ITEM;
				dropLimit = Config.KARMA_DROP_LIMIT;
			}
			else if (isKillerNpc && (getLevel() > 4) && !isFestivalParticipant())
			{
				dropPercent = Config.PLAYER_RATE_DROP;
				dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.PLAYER_RATE_DROP_ITEM;
				dropLimit = Config.PLAYER_DROP_LIMIT;
			}
			
			if ((dropPercent > 0) && (Rnd.get(100) < dropPercent))
			{
				int dropCount = 0;
				
				int itemDropPercent = 0;
				
				for (L2ItemInstance itemDrop : getInventory().getItems())
				{
					if (itemDrop.isShadowItem() || itemDrop.isTimeLimitedItem() || !itemDrop.isDropable() || (itemDrop.getId() == PcInventory.ADENA_ID) || (itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST) || (hasSummon() && (getSummon().getControlObjectId() == itemDrop.getId())) || (Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_ITEMS, itemDrop.getId()) >= 0) || (Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.getId()) >= 0))
					{
						continue;
					}
					
					if (itemDrop.isEquipped())
					{
						itemDropPercent = itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
						getInventory().unEquipItemInSlot(itemDrop.getLocationSlot());
					}
					else
					{
						itemDropPercent = dropItem;
					}
					
					if (Rnd.get(100) < itemDropPercent)
					{
						dropItem("DieDrop", itemDrop, killer, true);
						
						if (isKarmaDrop)
						{
							_log.warning(getName() + " has karma and dropped id = " + itemDrop.getId() + ", count = " + itemDrop.getCount());
						}
						else
						{
							_log.warning(getName() + " dropped id = " + itemDrop.getId() + ", count = " + itemDrop.getCount());
						}
						
						if (++dropCount >= dropLimit)
						{
							break;
						}
					}
				}
			}
		}
	}
	
	private void onDieUpdateKarma()
	{
		if (getKarma() > 0)
		{
			double karmaLost = Config.KARMA_LOST_BASE;
			karmaLost *= getLevel();
			karmaLost *= (getLevel() / 100.0);
			karmaLost = Math.round(karmaLost);
			if (karmaLost < 0)
			{
				karmaLost = 1;
			}
			
			setKarma(getKarma() - (int) karmaLost);
		}
	}
	
	public void onKillUpdatePvPKarma(L2Character target)
	{
		if (target == null)
		{
			return;
		}
		if (!(target instanceof L2Playable))
		{
			return;
		}
		
		L2PcInstance targetPlayer = target.getActingPlayer();
		
		if (targetPlayer == null)
		{
			return;
		}
		
		if (targetPlayer == this)
		{
			return;
		}
		
		if (GlobalRestrictions.fakePvPZone(this, targetPlayer))
		{
			return;
		}
		
		if (isCursedWeaponEquipped() && target.isPlayer())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquippedId);
			return;
		}
		
		if (isInDuel() && targetPlayer.isInDuel())
		{
			return;
		}
		
		if (isInsideZone(ZoneId.PVP) || targetPlayer.isInsideZone(ZoneId.PVP))
		{
			if ((getSiegeState() > 0) && (targetPlayer.getSiegeState() > 0) && (getSiegeState() != targetPlayer.getSiegeState()))
			{
				final L2Clan killerClan = getClan();
				final L2Clan targetClan = targetPlayer.getClan();
				if ((killerClan != null) && (targetClan != null))
				{
					killerClan.addSiegeKill();
					targetClan.addSiegeDeath();
				}
			}
			return;
		}
		
		if ((checkIfPvP(target) && (targetPlayer.getPvpFlag() != 0)) || (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP)))
		{
			increasePvpKills(target);
		}
		else
		{
			if ((targetPlayer.getClan() != null) && (getClan() != null) && getClan().isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(getClanId()) && (targetPlayer.getPledgeType() != L2Clan.SUBUNIT_ACADEMY) && (getPledgeType() != L2Clan.SUBUNIT_ACADEMY))
			{
				increasePvpKills(target);
				return;
			}
			
			if (targetPlayer.getKarma() > 0)
			{
				if (Config.KARMA_AWARD_PK_KILL)
				{
					increasePvpKills(target);
				}
			}
			else if (targetPlayer.getPvpFlag() == 0)
			{
				if (Config.HITMAN_ENABLE_EVENT && Config.HITMAN_TAKE_KARMA && Hitman.getInstance().exists(targetPlayer.getObjectId()))
				{
					return;
				}
				
				increasePkKillsAndKarma(target);
				checkItemRestriction();
			}
		}
	}
	
	public void increasePvpKills(L2Character target)
	{
		increasePvpKills(target, false);
	}
	
	public void increasePvpKills(L2Character target, boolean event)
	{
		if (isFightingInEvent() || isFightingInTW())
		{
			return;
		}
		
		if (target instanceof L2PcInstance)
		{
			if (event || AntiFeedManager.getInstance().check(this, target))
			{
				if (!isInTownWarEvent() || (isInTownWarEvent() && Config.TW_GIVE_PVP_AND_PK_POINTS))
				{
					setPvpKills(getPvpKills() + 1);
					if (Config.ADD_EXP_SP_ON_PVP)
					{
						addExpAndSp(Config.ADD_EXP_PVP, Config.ADD_SP_PVP);
					}
					if (Config.ALLOW_PVP_REWARD_AUTO_LOOT)
					{
						if (Rnd.get(100) >= Config.PVP_REWARD_ITEM_DROP_CHANCE)
						{
							addItem("Loot", Config.PVP_REWARD_ITEM_ID, Config.PVP_REWARD_ITEM_AMMOUNT, this, true);
						}
						sendMessage(Config.PVP_REWARD_MESSAGE_TEXT);
					}
					if (Config.ALLOW_PVP_REWARD)
					{
						int PvpRewardItemId = (Config.PVP_REWARD_ITEM_ID);
						int PvpRewardItemAmmount = (Config.PVP_REWARD_ITEM_AMMOUNT);
						L2ItemInstance item = ItemHolder.getInstance().createItem("Loot", PvpRewardItemId, PvpRewardItemAmmount, this, true);
						int randDropLim = (Config.PVP_REWARD_ITEM_RADIUS);
						int newX = (target.getX() + Rnd.get((randDropLim * 2) + 1)) - randDropLim;
						int newY = (target.getY() + Rnd.get((randDropLim * 2) + 1)) - randDropLim;
						if (Rnd.get(100) >= Config.PVP_REWARD_ITEM_DROP_CHANCE)
						{
							item.dropMe(null, newX, newY, target.getZ());
						}
						sendMessage(Config.PVP_REWARD_MESSAGE_TEXT);
					}
					sendPacket(new UserInfo(this));
					sendPacket(new ExBrExtraUserInfo(this));
				}
			}
		}
	}
	
	public void increasePkKillsAndKarma(L2Character target)
	{
		if (isFightingInEvent() || isFightingInTW())
		{
			return;
		}
		
		int baseKarma = Config.KARMA_MIN_KARMA;
		int newKarma = baseKarma;
		int karmaLimit = Config.KARMA_MAX_KARMA;
		
		int pkLVL = getLevel();
		int pkPKCount = getPkKills();
		
		int targLVL = target.getLevel();
		
		int lvlDiffMulti = 0;
		int pkCountMulti = 0;
		
		if (pkPKCount > 0)
		{
			pkCountMulti = pkPKCount / 2;
		}
		else
		{
			pkCountMulti = 1;
		}
		
		if (pkCountMulti < 1)
		{
			pkCountMulti = 1;
		}
		
		if (pkLVL > targLVL)
		{
			lvlDiffMulti = pkLVL / targLVL;
		}
		else
		{
			lvlDiffMulti = 1;
		}
		
		if (lvlDiffMulti < 1)
		{
			lvlDiffMulti = 1;
		}
		
		newKarma *= pkCountMulti;
		newKarma *= lvlDiffMulti;
		
		if (newKarma < baseKarma)
		{
			newKarma = baseKarma;
		}
		if (newKarma > karmaLimit)
		{
			newKarma = karmaLimit;
		}
		
		if (getKarma() > (Integer.MAX_VALUE - newKarma))
		{
			newKarma = Integer.MAX_VALUE - getKarma();
		}
		
		if ((!isInTownWarEvent()) || (Config.TW_ALLOW_KARMA && isInTownWarEvent()))
		{
			setKarma(getKarma() + newKarma);
		}
		
		if ((target instanceof L2PcInstance) && AntiFeedManager.getInstance().check(this, target))
		{
			int newPks = 0;
			if (!isInTownWarEvent() || (isInTownWarEvent() && Config.TW_GIVE_PVP_AND_PK_POINTS))
			{
				newPks = getPkKills() + 1;
				setPkKills(newPks);
			}
		}
		
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		
		if (Config.PUNISH_PK_PLAYER_IF_PKS_OVER > 0)
		{
			for (final Long pKTime : _pKsCounter)
			{
				if ((System.currentTimeMillis() - pKTime.longValue()) > (Config.PK_MONITOR_PERIOD * 1000))
				{
					_pKsCounter.remove(pKTime);
				}
				else
				{
					break;
				}
			}
			_pKsCounter.add(Long.valueOf(System.currentTimeMillis()));
			
			if (_pKsCounter.size() > Config.PUNISH_PK_PLAYER_IF_PKS_OVER)
			{
				if ("jail".equals(Config.PK_PUNISHMENT_TYPE))
				{
					PunishmentManager.getInstance().startPunishment(new PunishmentTask(this, PunishmentAffect.CHARACTER, PunishmentType.JAIL, System.currentTimeMillis() + (int) (Config.PK_PUNISHMENT_PERIOD / 60), "", getClass().getSimpleName()));
					sendMessage("Jailed for excessive PK.");
				}
			}
		}
	}
	
	public int calculateKarmaLost(long exp)
	{
		long expGained = Math.abs(exp);
		expGained /= Config.KARMA_XP_DIVIDER;
		
		int karmaLost = 0;
		if (expGained > Integer.MAX_VALUE)
		{
			karmaLost = Integer.MAX_VALUE;
		}
		else
		{
			karmaLost = (int) expGained;
		}
		
		if (karmaLost < Config.KARMA_LOST_BASE)
		{
			karmaLost = Config.KARMA_LOST_BASE;
		}
		if (karmaLost > getKarma())
		{
			karmaLost = getKarma();
		}
		
		return karmaLost;
	}
	
	public void updatePvPStatus()
	{
		if (isFightingInEvent() || isFightingInTW())
		{
			return;
		}
		if (isInsideZone(ZoneId.PVP))
		{
			return;
		}
		setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
		
		if (getPvpFlag() == 0)
		{
			startPvPFlag();
		}
	}
	
	public void updatePvPStatus(L2Character target)
	{
		L2PcInstance player_target = target.getActingPlayer();
		
		if (isFightingInEvent() || isFightingInTW())
		{
			return;
		}
		
		if (GlobalRestrictions.fakePvPZone(this, player_target))
		{
			return;
		}
		
		if (player_target == null)
		{
			return;
		}
		
		if ((isInDuel() && (player_target.getDuelId() == getDuelId())))
		{
			return;
		}
		if ((!isInsideZone(ZoneId.PVP) || !player_target.isInsideZone(ZoneId.PVP)) && (player_target.getKarma() == 0))
		{
			if (checkIfPvP(player_target))
			{
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_PVP_TIME);
			}
			else
			{
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
			}
			if (getPvpFlag() == 0)
			{
				startPvPFlag();
			}
		}
	}
	
	public boolean isLucky()
	{
		return ((getLevel() <= 9) && (getFirstPassiveEffect(L2EffectType.LUCKY) != null));
	}
	
	public void restoreExp(double restorePercent)
	{
		if (getExpBeforeDeath() > 0)
		{
			getStat().addExp(Math.round(((getExpBeforeDeath() - getExp()) * restorePercent) / 100));
			setExpBeforeDeath(0);
		}
	}
	
	public void deathPenalty(boolean atwar, boolean killed_by_pc, boolean killed_by_siege_npc)
	{
		final int lvl = getLevel();
		
		int clan_luck = getSkillLevel(L2Skill.SKILL_CLAN_LUCK);
		
		double clan_luck_modificator = 1.0;
		
		if (!killed_by_pc)
		{
			switch (clan_luck)
			{
				case 3:
					clan_luck_modificator = 0.8;
					break;
				case 2:
					clan_luck_modificator = 0.8;
					break;
				case 1:
					clan_luck_modificator = 0.88;
					break;
				default:
					clan_luck_modificator = 1.0;
					break;
			}
		}
		else
		{
			switch (clan_luck)
			{
				case 3:
					clan_luck_modificator = 0.5;
					break;
				case 2:
					clan_luck_modificator = 0.5;
					break;
				case 1:
					clan_luck_modificator = 0.5;
					break;
				default:
					clan_luck_modificator = 1.0;
					break;
			}
		}
		double percentLost = Config.PLAYER_XP_PERCENT_LOST[getLevel()] * clan_luck_modificator;
		
		if (getKarma() > 0)
		{
			percentLost *= Config.RATE_KARMA_EXP_LOST;
		}
		
		if (isFestivalParticipant() || atwar)
		{
			percentLost /= 4.0;
		}
		
		long lostExp = 0;
		if (!isFightingInEvent() && !isFightingInTW())
		{
			if (lvl < ExperienceParser.getInstance().getMaxLevel())
			{
				lostExp = Math.round(((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost) / 100);
			}
			else
			{
				lostExp = Math.round(((getStat().getExpForLevel(ExperienceParser.getInstance().getMaxLevel()) - getStat().getExpForLevel(ExperienceParser.getInstance().getMaxLevel() - 1)) * percentLost) / 100);
			}
		}
		if (!L2Event.isParticipant(this))
		{
			if (lvl < ExperienceParser.getInstance().getMaxLevel())
			{
				lostExp = Math.round(((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost) / 100);
			}
			else
			{
				lostExp = Math.round(((getStat().getExpForLevel(ExperienceParser.getInstance().getMaxLevel()) - getStat().getExpForLevel(ExperienceParser.getInstance().getMaxLevel() - 1)) * percentLost) / 100);
			}
		}
		
		setExpBeforeDeath(getExp());
		
		if (isInsideZone(ZoneId.PVP))
		{
			if (isInsideZone(ZoneId.SIEGE))
			{
				if (isInSiege() && (killed_by_pc || killed_by_siege_npc))
				{
					lostExp = 0;
				}
			}
			else if (killed_by_pc)
			{
				lostExp = 0;
			}
		}
		getStat().addExp(-lostExp);
	}
	
	public boolean isPartyWaiting()
	{
		return PartyMatchWaitingList.getInstance().getPlayers().contains(this);
	}
	
	public void setPartyRoom(int id)
	{
		_partyroom = id;
	}
	
	public int getPartyRoom()
	{
		return _partyroom;
	}
	
	public boolean isInPartyMatchRoom()
	{
		return _partyroom > 0;
	}
	
	public void startTimers()
	{
		startPcBangPointsTask();
	}
	
	public void stopAllTimers()
	{
		stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopWaterTask();
		stopFeed();
		clearPetData();
		storePetFood(_mountNpcId);
		stopRentPet();
		stopPvpRegTask();
		stopSoulTask();
		stopChargeTask();
		stopFameTask();
		stopVitalityTask();
		stopRecoBonusTask();
		stopRecoGiveTask();
		stopAdventBlessingTask();
		stopAdventBonusTask();
		stopPcBangPointsTask();
	}
	
	@Override
	public L2Summon getSummon()
	{
		return _summon;
	}
	
	public L2Decoy getDecoy()
	{
		return _decoy;
	}
	
	public L2TrapInstance getTrap()
	{
		return _trap;
	}
	
	public void setPet(L2Summon summon)
	{
		_summon = summon;
	}
	
	public void setDecoy(L2Decoy decoy)
	{
		_decoy = decoy;
	}
	
	public void setTrap(L2TrapInstance trap)
	{
		_trap = trap;
	}
	
	public List<L2TamedBeastInstance> getTrainedBeasts()
	{
		return _tamedBeast;
	}
	
	public void addTrainedBeast(L2TamedBeastInstance tamedBeast)
	{
		if (_tamedBeast == null)
		{
			_tamedBeast = new FastList<>();
		}
		_tamedBeast.add(tamedBeast);
	}
	
	public L2Request getRequest()
	{
		return _request;
	}
	
	public void setActiveRequester(L2PcInstance requester)
	{
		_activeRequester = requester;
	}
	
	public L2PcInstance getActiveRequester()
	{
		L2PcInstance requester = _activeRequester;
		if (requester != null)
		{
			if (requester.isRequestExpired() && (_activeTradeList == null))
			{
				_activeRequester = null;
			}
		}
		return _activeRequester;
	}
	
	public boolean isProcessingRequest()
	{
		return (getActiveRequester() != null) || (_requestExpireTime > GameTimeController.getInstance().getGameTicks());
	}
	
	public boolean isProcessingTransaction()
	{
		return (getActiveRequester() != null) || (_activeTradeList != null) || (_requestExpireTime > GameTimeController.getInstance().getGameTicks());
	}
	
	public void onTransactionRequest(L2PcInstance partner)
	{
		_requestExpireTime = GameTimeController.getInstance().getGameTicks() + (REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND);
		partner.setActiveRequester(this);
	}
	
	public boolean isRequestExpired()
	{
		return !(_requestExpireTime > GameTimeController.getInstance().getGameTicks());
	}
	
	public void onTransactionResponse()
	{
		_requestExpireTime = 0;
	}
	
	public void setActiveWarehouse(ItemContainer warehouse)
	{
		_activeWarehouse = warehouse;
	}
	
	public ItemContainer getActiveWarehouse()
	{
		return _activeWarehouse;
	}
	
	public void setActiveTradeList(TradeList tradeList)
	{
		_activeTradeList = tradeList;
	}
	
	public TradeList getActiveTradeList()
	{
		return _activeTradeList;
	}
	
	public void onTradeStart(L2PcInstance partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
		
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_C1);
		msg.addPcName(partner);
		sendPacket(msg);
		sendPacket(new TradeStart(this));
	}
	
	public void onTradeConfirm(L2PcInstance partner)
	{
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_CONFIRMED_TRADE);
		msg.addPcName(partner);
		sendPacket(msg);
		sendPacket(TradeOtherDone.STATIC_PACKET);
	}
	
	public void onTradeCancel(L2PcInstance partner)
	{
		if (_activeTradeList == null)
		{
			return;
		}
		
		_activeTradeList.lock();
		_activeTradeList = null;
		
		sendPacket(new TradeDone(0));
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_CANCELED_TRADE);
		msg.addPcName(partner);
		sendPacket(msg);
	}
	
	public void onTradeFinish(boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(new TradeDone(1));
		if (successfull)
		{
			sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
		}
	}
	
	public void startTrade(L2PcInstance partner)
	{
		onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	public void cancelActiveTrade()
	{
		if (_activeTradeList == null)
		{
			return;
		}
		
		L2PcInstance partner = _activeTradeList.getPartner();
		if (partner != null)
		{
			partner.onTradeCancel(this);
		}
		onTradeCancel(this);
	}
	
	public boolean hasManufactureShop()
	{
		return (_manufactureItems != null) && !_manufactureItems.isEmpty();
	}
	
	public Map<Integer, L2ManufactureItem> getManufactureItems()
	{
		if (_manufactureItems == null)
		{
			synchronized (this)
			{
				if (_manufactureItems == null)
				{
					_manufactureItems = Collections.synchronizedMap(new LinkedHashMap<Integer, L2ManufactureItem>());
				}
			}
		}
		return _manufactureItems;
	}
	
	public String getStoreName()
	{
		return _storeName;
	}
	
	public void setStoreName(String name)
	{
		_storeName = name == null ? "" : name;
	}
	
	public TradeList getSellList()
	{
		if (_sellList == null)
		{
			_sellList = new TradeList(this);
		}
		return _sellList;
	}
	
	public TradeList getBuyList()
	{
		if (_buyList == null)
		{
			_buyList = new TradeList(this);
		}
		return _buyList;
	}
	
	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
		
		if (Config.OFFLINE_DISCONNECT_FINISHED && (_privatestore == STORE_PRIVATE_NONE) && ((getClient() == null) || getClient().isDetached()))
		{
			deleteMe();
		}
	}
	
	public int getPrivateStoreType()
	{
		return _privatestore;
	}
	
	public void setClan(L2Clan clan)
	{
		_clan = clan;
		setTitle("");
		
		if (clan == null)
		{
			_clanId = 0;
			_clanPrivileges = 0;
			_pledgeType = 0;
			_powerGrade = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			_activeWarehouse = null;
			return;
		}
		
		if (!clan.isMember(getObjectId()) && (!isPhantome()))
		{
			setClan(null);
			return;
		}
		_clanId = clan.getId();
	}
	
	public L2Clan getClan()
	{
		return _clan;
	}
	
	public boolean isClanLeader()
	{
		if (getClan() == null)
		{
			return false;
		}
		return getObjectId() == getClan().getLeaderId();
	}
	
	@Override
	protected void reduceArrowCount(boolean bolts)
	{
		if (isPhantome())
		{
			return;
		}
		
		L2ItemInstance arrows = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		
		if (arrows == null)
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			if (bolts)
			{
				_boltItem = null;
			}
			else
			{
				_arrowItem = null;
			}
			sendPacket(new ItemList(this, false));
			return;
		}
		
		if (arrows.getCount() > 1)
		{
			synchronized (arrows)
			{
				arrows.changeCountWithoutTrace(-1, this, null);
				arrows.setLastChange(L2ItemInstance.MODIFIED);
				
				if ((GameTimeController.getInstance().getGameTicks() % 10) == 0)
				{
					arrows.updateDatabase();
				}
				_inventory.refreshWeight();
			}
		}
		else
		{
			_inventory.destroyItem("Consume", arrows, this, null);
			
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			if (bolts)
			{
				_boltItem = null;
			}
			else
			{
				_arrowItem = null;
			}
			
			sendPacket(new ItemList(this, false));
			return;
		}
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(arrows);
			sendPacket(iu);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
	}
	
	@Override
	protected boolean checkAndEquipArrows()
	{
		if (isPhantome())
		{
			return true;
		}
		
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			
			if (_arrowItem != null)
			{
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				
				ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		return _arrowItem != null;
	}
	
	@Override
	protected boolean checkAndEquipBolts()
	{
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			_boltItem = getInventory().findBoltForCrossBow(getActiveWeaponItem());
			
			if (_boltItem != null)
			{
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _boltItem);
				
				ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			_boltItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		return _boltItem != null;
	}
	
	public boolean disarmWeapons()
	{
		final L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			return true;
		}
		
		if (isCursedWeaponEquipped())
		{
			return false;
		}
		
		if (isCombatFlagEquipped())
		{
			return false;
		}
		
		if (wpn.getWeaponItem().isForceEquip())
		{
			return false;
		}
		
		final L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
		final InventoryUpdate iu = new InventoryUpdate();
		for (L2ItemInstance itm : unequiped)
		{
			iu.addModifiedItem(itm);
		}
		
		sendPacket(iu);
		abortAttack();
		broadcastUserInfo();
		
		if (unequiped.length > 0)
		{
			final SystemMessage sm;
			if (unequiped[0].getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(unequiped[0].getEnchantLevel());
				sm.addItemName(unequiped[0]);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(unequiped[0]);
			}
			sendPacket(sm);
		}
		return true;
	}
	
	public boolean disarmShield()
	{
		L2ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null)
		{
			L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			sendPacket(iu);
			
			abortAttack();
			broadcastUserInfo();
			
			if (unequiped.length > 0)
			{
				SystemMessage sm = null;
				if (unequiped[0].getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequiped[0].getEnchantLevel());
					sm.addItemName(unequiped[0]);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequiped[0]);
				}
				sendPacket(sm);
			}
		}
		return true;
	}
	
	public boolean mount(L2Summon pet)
	{
		if (!disarmWeapons() || !disarmShield() || isTransformed())
		{
			return false;
		}
		
		if (!GeoClient.getInstance().canSeeTarget(this, pet))
		{
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			return false;
		}
		
		stopAllToggles();
		setMount(pet.getId(), pet.getLevel());
		setMountObjectID(pet.getControlObjectId());
		clearPetData();
		startFeed(pet.getId());
		broadcastPacket(new Ride(this));
		broadcastUserInfo();
		
		pet.unSummon(this);
		
		return true;
	}
	
	public boolean mount(int npcId, int controlItemObjId, boolean useFood)
	{
		if (!disarmWeapons() || !disarmShield() || isTransformed())
		{
			return false;
		}
		
		stopAllToggles();
		setMount(npcId, getLevel());
		clearPetData();
		setMountObjectID(controlItemObjId);
		broadcastPacket(new Ride(this));
		broadcastUserInfo();
		if (useFood)
		{
			startFeed(npcId);
		}
		return true;
	}
	
	public boolean mountPlayer(L2Summon pet)
	{
		if ((pet != null) && pet.isMountable() && !isMounted() && !isBetrayed())
		{
			if (isDead())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				return false;
			}
			else if (pet.isDead())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				return false;
			}
			else if (pet.isInCombat() || pet.isRooted())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				return false;
				
			}
			else if (isInCombat())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			}
			else if (isSitting())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				return false;
			}
			else if (isFishing())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
				return false;
			}
			else if (isTransformed() || isCursedWeaponEquipped())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (getInventory().getItemByItemId(9819) != null)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendMessage("You cannot mount a steed while holding a flag.");
				return false;
			}
			else if (pet.isHungry())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			else if (!Util.checkIfInRange(200, this, pet, true))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT);
				return false;
			}
			else if (!pet.isDead() && !isMounted())
			{
				mount(pet);
			}
		}
		else if (isRentedPet())
		{
			stopRentPet();
		}
		else if (isMounted())
		{
			if ((getMountType() == MountType.WYVERN) && isInsideZone(ZoneId.NO_LANDING))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
				return false;
			}
			else if (isHungry())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			else
			{
				dismount();
			}
		}
		return true;
	}
	
	public boolean dismount()
	{
		boolean wasFlying = isFlying();
		
		sendPacket(new SetupGauge(3, 0, 0));
		int petId = _mountNpcId;
		setMount(0, 0);
		stopFeed();
		clearPetData();
		if (wasFlying)
		{
			removeSkill(SkillHolder.FrequentSkill.WYVERN_BREATH.getSkill());
		}
		broadcastPacket(new Ride(this));
		setMountObjectID(0);
		storePetFood(petId);
		broadcastUserInfo();
		return true;
	}
	
	public void setUptime(long time)
	{
		_uptime = time;
	}
	
	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}
	
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || (_teleportProtectEndTime > GameTimeController.getInstance().getGameTicks());
	}
	
	@Override
	public boolean isBlocked()
	{
		return super.isBlocked() || inObserverMode() || isTeleporting();
	}
	
	@Override
	public boolean isInParty()
	{
		return _party != null;
	}
	
	public void setParty(L2Party party)
	{
		_party = party;
	}
	
	public void joinParty(L2Party party)
	{
		if (party != null)
		{
			_party = party;
			party.addPartyMember(this);
		}
	}
	
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this, messageType.Disconnected);
			_party = null;
		}
	}
	
	@Override
	public L2Party getParty()
	{
		return _party;
	}
	
	@Override
	public boolean isGM()
	{
		return getAccessLevel().isGm();
	}
	
	public void setAccessLevel(int level)
	{
		_accessLevel = AdminParser.getInstance().getAccessLevel(level);
		
		getAppearance().setNameColor(_accessLevel.getNameColor());
		getAppearance().setTitleColor(_accessLevel.getTitleColor());
		broadcastUserInfo();
		
		CharNameHolder.getInstance().addName(this);
		
		if (!AdminParser.getInstance().hasAccessLevel(level))
		{
			_log.warning("Tryed to set unregistered access level " + level + " for " + toString() + ". Setting access level without privileges!");
		}
		else if (!Config.ENABLE_SAFE_ADMIN_PROTECTION && (level > 0))
		{
			_log.warning(_accessLevel.getName() + " access level set for character " + getName() + "! Just a warning to be careful ;)");
		}
	}
	
	public void setAccountAccesslevel(int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}
	
	@Override
	public L2AccessLevel getAccessLevel()
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
		{
			return AdminParser.getInstance().getMasterAccessLevel();
		}
		else if (_accessLevel == null)
		{
			setAccessLevel(0);
		}
		
		return _accessLevel;
	}
	
	public void updateAndBroadcastStatus(int broadcastType)
	{
		refreshOverloaded();
		refreshExpertisePenalty();
		
		if (broadcastType == 1)
		{
			sendPacket(new UserInfo(this));
			sendPacket(new ExBrExtraUserInfo(this));
		}
		
		if (broadcastType == 2)
		{
			broadcastUserInfo();
		}
	}
	
	public void setKarmaFlag(int flag)
	{
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			if (hasSummon())
			{
				player.sendPacket(new RelationChanged(getSummon(), getRelation(player), isAutoAttackable(player)));
			}
		}
	}
	
	public void broadcastKarma()
	{
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		sendPacket(su);
		
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			if (hasSummon())
			{
				player.sendPacket(new RelationChanged(getSummon(), getRelation(player), isAutoAttackable(player)));
			}
		}
	}
	
	public void setOnlineStatus(boolean isOnline, boolean updateInDb)
	{
		if (_isOnline != isOnline)
		{
			_isOnline = isOnline;
		}
		
		if (Config.HITMAN_ENABLE_EVENT && Hitman.getInstance().exists(getObjectId()))
		{
			Hitman.getInstance().getTarget(getObjectId()).setOnline(isOnline);
		}
		
		if (updateInDb)
		{
			updateOnlineStatus();
		}
	}
	
	public void setIsIn7sDungeon(boolean isIn7sDungeon)
	{
		_isIn7sDungeon = isIn7sDungeon;
	}
	
	public void updateOnlineStatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE charId=?"))
		{
			statement.setInt(1, isOnlineInt());
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed updating character online status.", e);
		}
	}
	
	private boolean createDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_CHARACTER))
		{
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getCurrentMp());
			statement.setInt(11, getAppearance().getFace());
			statement.setInt(12, getAppearance().getHairStyle());
			statement.setInt(13, getAppearance().getHairColor());
			statement.setInt(14, getAppearance().getSex() ? 1 : 0);
			statement.setLong(15, getExp());
			statement.setInt(16, getSp());
			statement.setInt(17, getKarma());
			statement.setInt(18, getFame());
			statement.setInt(19, getPvpKills());
			statement.setInt(20, getPkKills());
			statement.setInt(21, getClanId());
			statement.setInt(22, getRace().ordinal());
			statement.setInt(23, getClassId().getId());
			statement.setLong(24, getDeleteTimer());
			statement.setInt(25, hasDwarvenCraft() ? 1 : 0);
			statement.setString(26, getTitle());
			statement.setInt(27, getAppearance().getTitleColor());
			statement.setInt(28, getAccessLevel().getLevel());
			statement.setInt(29, isOnlineInt());
			statement.setInt(30, isIn7sDungeon() ? 1 : 0);
			statement.setInt(31, getClanPrivileges());
			statement.setInt(32, getWantsPeace());
			statement.setInt(33, getBaseClass());
			statement.setInt(34, getNewbie());
			statement.setInt(35, isNoble() ? 1 : 0);
			statement.setLong(36, 0);
			statement.setDate(37, new Date(getCreateDate().getTimeInMillis()));
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not insert char data: " + e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	private static L2PcInstance restore(int objectId)
	{
		L2PcInstance player = null;
		double currentCp = 0;
		double currentHp = 0;
		double currentMp = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER))
		{
			statement.setInt(1, objectId);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					final int activeClassId = rset.getInt("classid");
					final boolean female = rset.getInt("sex") != Sex.MALE;
					final L2PcTemplate template = CharTemplateParser.getInstance().getTemplate(activeClassId);
					PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);
					
					player = new L2PcInstance(objectId, template, rset.getString("account_name"), app, false);
					restorePremServiceData(player, rset.getString("account_name"));
					player.setName(rset.getString("char_name"));
					player._lastAccess = rset.getLong("lastAccess");
					
					player.getStat().setExp(rset.getLong("exp"));
					player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
					player.getStat().setLevel(rset.getByte("level"));
					player.getStat().setSp(rset.getInt("sp"));
					
					player.setWantsPeace(rset.getInt("wantspeace"));
					
					player.setHeading(rset.getInt("heading"));
					
					player.setKarma(rset.getInt("karma"));
					player.setFame(rset.getInt("fame"));
					player.setPvpKills(rset.getInt("pvpkills"));
					player.setPkKills(rset.getInt("pkkills"));
					player.setOnlineTime(rset.getLong("onlinetime"));
					player.setNewbie(rset.getInt("newbie"));
					player.setNoble(rset.getInt("nobless") == 1);
					
					player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
					if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
					{
						player.setClanJoinExpiryTime(0);
					}
					player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
					if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
					{
						player.setClanCreateExpiryTime(0);
					}
					
					int clanId = rset.getInt("clanid");
					player.setPowerGrade(rset.getInt("power_grade"));
					player.setPledgeType(rset.getInt("subpledge"));
					
					if (clanId > 0)
					{
						player.setClan(ClanHolder.getInstance().getClan(clanId));
					}
					
					if (player.getClan() != null)
					{
						if (player.getClan().getLeaderId() != player.getObjectId())
						{
							if (player.getPowerGrade() == 0)
							{
								player.setPowerGrade(5);
							}
							player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
						}
						else
						{
							player.setClanPrivileges(L2Clan.CP_ALL);
							player.setPowerGrade(1);
						}
						player.setPledgeClass(L2ClanMember.calculatePledgeClass(player));
					}
					else
					{
						if (player.isNoble())
						{
							player.setPledgeClass(5);
						}
						
						if (player.isHero())
						{
							player.setPledgeClass(8);
						}
						player.setClanPrivileges(L2Clan.CP_NOTHING);
					}
					
					player.setDeleteTimer(rset.getLong("deletetime"));
					
					player.setTitle(rset.getString("title"));
					player.setAccessLevel(rset.getInt("accesslevel"));
					int titleColor = rset.getInt("title_color");
					if (titleColor != 0xFFFFFF)
					{
						player.getAppearance().setTitleColor(titleColor);
					}
					player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
					player.setUptime(System.currentTimeMillis());
					
					currentHp = rset.getDouble("curHp");
					currentCp = rset.getDouble("curCp");
					currentMp = rset.getDouble("curMp");
					
					player._classIndex = 0;
					
					try
					{
						player.setBaseClass(rset.getInt("base_class"));
					}
					catch (Exception e)
					{
						player.setBaseClass(activeClassId);
					}
					
					if (restoreSubClassData(player))
					{
						if (activeClassId != player.getBaseClass())
						{
							for (SubClass subClass : player.getSubClasses().values())
							{
								if (subClass.getClassId() == activeClassId)
								{
									player._classIndex = subClass.getClassIndex();
								}
							}
						}
					}
					if ((player.getClassIndex() == 0) && (activeClassId != player.getBaseClass()))
					{
						player.setClassId(player.getBaseClass());
						_log.warning("Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
					}
					else
					{
						player._activeClass = activeClassId;
					}
					
					player.setApprentice(rset.getInt("apprentice"));
					player.setSponsor(rset.getInt("sponsor"));
					player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
					player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
					
					CursedWeaponsManager.getInstance().checkPlayer(player);
					
					player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
					
					player.setVitalityPoints(rset.getInt("vitality_points"), true);
					
					player.setPcBangPoints(rset.getInt("pccafe_points"));
					
					player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
					
					player.setBookMarkSlot(rset.getInt("BookmarkSlot"));
					
					player.getCreateDate().setTime(rset.getDate("createDate"));
					
					player.setGamePoints(rset.getLong("game_points"));
					
					player.setLang(rset.getString("language"));
					
					player.setHitmanTarget(rset.getInt("hitman_target"));
					
					player.setSurveystatus(rset.getLong("surveystatus"));
					
					try (PreparedStatement stmt = con.prepareStatement("SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?"))
					{
						stmt.setString(1, player._accountName);
						stmt.setInt(2, objectId);
						try (ResultSet chars = stmt.executeQuery())
						{
							while (chars.next())
							{
								player._chars.put(chars.getInt("charId"), chars.getString("char_name"));
							}
						}
					}
				}
			}
			
			if (player == null)
			{
				return null;
			}
			
			if (Hero.getInstance().isHero(objectId))
			{
				player.setHero(true);
			}
			player.getInventory().restore();
			player.getFreight().restore();
			
			if (!Config.WAREHOUSE_CACHE)
			{
				player.getWarehouse();
			}
			player.restoreCharData();
			player.rewardSkills();
			player.restoreItemReuse();
			player.setCurrentCp(currentCp);
			player.setCurrentHp(currentHp);
			player.setCurrentMp(currentMp);
			
			if (currentHp < 0.5)
			{
				player.setIsDead(true);
				player.stopHpMpRegeneration();
			}
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			
			if (player.hasSummon())
			{
				player.getSummon().setOwner(player);
			}
			
			player.refreshOverloaded();
			player.refreshExpertisePenalty();
			player.restoreFriendList();
			
			if (Config.STORE_UI_SETTINGS)
			{
				player.restoreUISettings();
			}
			
			player.loadVariables();
			
			if (player.isGM())
			{
				final long masks = player.getVariables().getLong(COND_OVERRIDE_KEY, PcCondOverride.getAllExceptionsMask());
				player.setOverrideCond(masks);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed loading character.", e);
		}
		return player;
	}
	
	public Forum getMail()
	{
		if (_forumMail == null)
		{
			setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			
			if (_forumMail == null)
			{
				ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), Forum.MAIL, Forum.OWNERONLY, getObjectId());
				setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			}
		}
		
		return _forumMail;
	}
	
	public void setMail(Forum forum)
	{
		_forumMail = forum;
	}
	
	public Forum getMemo()
	{
		if (_forumMemo == null)
		{
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			
			if (_forumMemo == null)
			{
				ForumsBBSManager.getInstance().createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO, Forum.OWNERONLY, getObjectId());
				setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			}
		}
		
		return _forumMemo;
	}
	
	public void setMemo(Forum forum)
	{
		_forumMemo = forum;
	}
	
	private static boolean restoreSubClassData(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES))
		{
			statement.setInt(1, player.getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					SubClass subClass = new SubClass();
					subClass.setClassId(rset.getInt("class_id"));
					subClass.setLevel(rset.getByte("level"));
					subClass.setExp(rset.getLong("exp"));
					subClass.setSp(rset.getInt("sp"));
					subClass.setClassIndex(rset.getInt("class_index"));
					
					player.getSubClasses().put(subClass.getClassIndex(), subClass);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore classes for " + player.getName() + ": " + e.getMessage(), e);
		}
		return true;
	}
	
	private void restoreCharData()
	{
		restoreSkills();
		_macros.restoreMe();
		_shortCuts.restoreMe();
		restoreHenna();
		restoreTeleportBookmark();
		restoreRecipeBook(true);
		
		if (Config.STORE_RECIPE_SHOPLIST)
		{
			restoreRecipeShopList();
		}
		
		loadPremiumItemList();
		restorePetInventoryItems();
	}
	
	private void restoreRecipeBook(boolean loadCommon)
	{
		final String sql = loadCommon ? "SELECT id, type, classIndex FROM character_recipebook WHERE charId=?" : "SELECT id FROM character_recipebook WHERE charId=? AND classIndex=? AND type = 1";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql))
		{
			statement.setInt(1, getObjectId());
			if (!loadCommon)
			{
				statement.setInt(2, _classIndex);
			}
			
			try (ResultSet rset = statement.executeQuery())
			{
				_dwarvenRecipeBook.clear();
				
				L2RecipeList recipe;
				RecipeParser rd = RecipeParser.getInstance();
				while (rset.next())
				{
					recipe = rd.getRecipeList(rset.getInt("id"));
					if (loadCommon)
					{
						if (rset.getInt(2) == 1)
						{
							if (rset.getInt(3) == _classIndex)
							{
								registerDwarvenRecipeList(recipe, false);
							}
						}
						else
						{
							registerCommonRecipeList(recipe, false);
						}
					}
					else
					{
						registerDwarvenRecipeList(recipe, false);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore recipe book data:" + e.getMessage(), e);
		}
	}
	
	public Map<Integer, L2PremiumItem> getPremiumItemList()
	{
		return _premiumItems;
	}
	
	private void loadPremiumItemList()
	{
		final String sql = "SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql))
		{
			statement.setInt(1, getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int itemNum = rset.getInt("itemNum");
					int itemId = rset.getInt("itemId");
					long itemCount = rset.getLong("itemCount");
					String itemSender = rset.getString("itemSender");
					_premiumItems.put(itemNum, new L2PremiumItem(itemId, itemCount, itemSender));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore premium items: " + e.getMessage(), e);
		}
	}
	
	public void updatePremiumItem(int itemNum, long newcount)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=? "))
		{
			statement.setLong(1, newcount);
			statement.setInt(2, getObjectId());
			statement.setInt(3, itemNum);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not update premium items: " + e.getMessage(), e);
		}
	}
	
	public void deletePremiumItem(int itemNum)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=? "))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, itemNum);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not delete premium item: " + e);
		}
	}
	
	public synchronized void store(boolean storeActiveEffects)
	{
		storeCharBase();
		storeCharSub();
		storeEffect(storeActiveEffects);
		storeItemReuseDelay();
		
		if (Config.STORE_RECIPE_SHOPLIST)
		{
			storeRecipeShopList();
		}
		
		if (Config.STORE_UI_SETTINGS)
		{
			storeUISettings();
		}
		
		SevenSigns.getInstance().saveSevenSignsData(getObjectId());
		
		final PlayerVariables vars = getScript(PlayerVariables.class);
		if (vars != null)
		{
			vars.storeMe();
		}
		
		final AccountVariables aVars = getScript(AccountVariables.class);
		if (aVars != null)
		{
			aVars.storeMe();
		}
	}
	
	@Override
	public void store()
	{
		store(true);
	}
	
	private void storeCharBase()
	{
		long exp = getStat().getBaseExp();
		int level = getStat().getBaseLevel();
		int sp = getStat().getBaseSp();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER))
		{
			statement.setInt(1, level);
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getCurrentMp());
			statement.setInt(8, getAppearance().getFace());
			statement.setInt(9, getAppearance().getHairStyle());
			statement.setInt(10, getAppearance().getHairColor());
			statement.setInt(11, getAppearance().getSex() ? 1 : 0);
			statement.setInt(12, getHeading());
			statement.setInt(13, _observerMode ? _lastX : getX());
			statement.setInt(14, _observerMode ? _lastY : getY());
			statement.setInt(15, _observerMode ? _lastZ : getZ());
			statement.setLong(16, exp);
			statement.setLong(17, getExpBeforeDeath());
			statement.setInt(18, sp);
			statement.setInt(19, getKarma());
			statement.setInt(20, getFame());
			statement.setInt(21, getPvpKills());
			statement.setInt(22, getPkKills());
			statement.setInt(23, getClanId());
			statement.setInt(24, getRace().ordinal());
			statement.setInt(25, getClassId().getId());
			statement.setLong(26, getDeleteTimer());
			statement.setString(27, getTitle());
			statement.setInt(28, getAppearance().getTitleColor());
			statement.setInt(29, getAccessLevel().getLevel());
			statement.setInt(30, isOnlineInt());
			statement.setInt(31, isIn7sDungeon() ? 1 : 0);
			statement.setInt(32, getClanPrivileges());
			statement.setInt(33, getWantsPeace());
			statement.setInt(34, getBaseClass());
			
			long totalOnlineTime = _onlineTime;
			
			if (_onlineBeginTime > 0)
			{
				totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;
			}
			
			statement.setLong(35, totalOnlineTime);
			statement.setInt(36, getNewbie());
			statement.setInt(37, isNoble() ? 1 : 0);
			statement.setInt(38, getPowerGrade());
			statement.setInt(39, getPledgeType());
			statement.setInt(40, getLvlJoinedAcademy());
			statement.setLong(41, getApprentice());
			statement.setLong(42, getSponsor());
			statement.setLong(43, getClanJoinExpiryTime());
			statement.setLong(44, getClanCreateExpiryTime());
			statement.setString(45, getName());
			statement.setLong(46, getDeathPenaltyBuffLevel());
			statement.setInt(47, getBookMarkSlot());
			statement.setInt(48, getVitalityPoints());
			statement.setInt(49, getPcBangPoints());
			statement.setString(50, getLang());
			statement.setInt(51, getHitmanTarget());
			statement.setLong(52, getGamePoints());
			statement.setInt(53, getObjectId());
			
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store char base data: " + this + " - " + e.getMessage(), e);
		}
	}
	
	private void storeCharSub()
	{
		if (getTotalSubClasses() <= 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS))
		{
			for (SubClass subClass : getSubClasses().values())
			{
				statement.setLong(1, subClass.getExp());
				statement.setInt(2, subClass.getSp());
				statement.setInt(3, subClass.getLevel());
				statement.setInt(4, subClass.getClassId());
				statement.setInt(5, getObjectId());
				statement.setInt(6, subClass.getClassIndex());
				
				statement.execute();
				statement.clearParameters();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store sub class data for " + getName() + ": " + e.getMessage(), e);
		}
	}
	
	@Override
	public void storeEffect(boolean storeEffects)
	{
		if (!Config.STORE_SKILL_COOLTIME)
		{
			return;
		}
		
		boolean isAcumulative = Config.SUBCLASS_STORE_SKILL;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(isAcumulative ? DELETE_SKILL_SAVE_MODIFER : DELETE_SKILL_SAVE);
			
			statement.setInt(1, getObjectId());
			if (!isAcumulative)
			{
				statement.setInt(2, getClassIndex());
			}
			statement.execute();
			statement.close();
			
			int buff_index = 0;
			
			final List<Integer> storedSkills = new ArrayList<>();
			
			statement = con.prepareStatement(ADD_SKILL_SAVE);
			
			if (storeEffects)
			{
				for (L2Effect effect : getAllEffects())
				{
					if (effect == null)
					{
						continue;
					}
					
					switch (effect.getEffectType())
					{
						case HEAL_OVER_TIME:
						case CPHEAL_OVER_TIME:
						case HIDE:
							continue;
					}
					
					if (effect.getAbnormalType().equalsIgnoreCase("SEED_OF_KNIGHT"))
					{
						continue;
					}
					
					if (effect.getAbnormalType().equalsIgnoreCase("LIFE_FORCE_OTHERS"))
					{
						continue;
					}
					
					L2Skill skill = effect.getSkill();
					if (storedSkills.contains(skill.getReuseHashCode()))
					{
						continue;
					}
					
					if (skill.isDance() && !Config.ALT_STORE_DANCES)
					{
						continue;
					}
					
					storedSkills.add(skill.getReuseHashCode());
					
					if (effect.isInUse() && !skill.isToggle())
					{
						
						statement.setInt(1, getObjectId());
						statement.setInt(2, skill.getId());
						statement.setInt(3, skill.getLevel());
						statement.setInt(4, effect.getTickCount());
						statement.setInt(5, effect.getTime());
						
						if (_reuseTimeStampsSkills.containsKey(skill.getReuseHashCode()))
						{
							TimeStamp t = _reuseTimeStampsSkills.get(skill.getReuseHashCode());
							statement.setLong(6, t.hasNotPassed() ? t.getReuse() : 0);
							statement.setDouble(7, t.hasNotPassed() ? t.getStamp() : 0);
						}
						else
						{
							statement.setLong(6, 0);
							statement.setDouble(7, 0);
						}
						
						statement.setInt(8, 0);
						statement.setInt(9, getClassIndex());
						statement.setInt(10, ++buff_index);
						
						statement.execute();
					}
				}
			}
			int hash;
			TimeStamp t;
			for (Entry<Integer, TimeStamp> ts : _reuseTimeStampsSkills.entrySet())
			{
				hash = ts.getKey();
				if (storedSkills.contains(hash))
				{
					continue;
				}
				t = ts.getValue();
				if ((t != null) && t.hasNotPassed())
				{
					storedSkills.add(hash);
					
					statement.setInt(1, getObjectId());
					statement.setInt(2, t.getSkillId());
					statement.setInt(3, t.getSkillLvl());
					statement.setInt(4, -1);
					statement.setInt(5, -1);
					statement.setLong(6, t.getReuse());
					statement.setDouble(7, t.getStamp());
					statement.setInt(8, 1);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buff_index);
					
					statement.execute();
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store char effect data: ", e);
		}
	}
	
	private void storeItemReuseDelay()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps1 = con.prepareStatement(DELETE_ITEM_REUSE_SAVE);
			PreparedStatement ps2 = con.prepareStatement(ADD_ITEM_REUSE_SAVE))
		{
			ps1.setInt(1, getObjectId());
			ps1.execute();
			
			for (TimeStamp ts : _reuseTimeStampsItems.values())
			{
				if ((ts != null) && ts.hasNotPassed())
				{
					ps2.setInt(1, getObjectId());
					ps2.setInt(2, ts.getItemId());
					ps2.setInt(3, ts.getItemObjectId());
					ps2.setLong(4, ts.getReuse());
					ps2.setDouble(5, ts.getStamp());
					ps2.execute();
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store char item reuse data: ", e);
		}
	}
	
	public boolean isOnline()
	{
		return _isOnline;
	}
	
	public int isOnlineInt()
	{
		if (_isOnline && (getClient() != null))
		{
			return getClient().isDetached() ? 2 : 1;
		}
		return 0;
	}
	
	public boolean isIn7sDungeon()
	{
		return _isIn7sDungeon;
	}
	
	@Override
	public L2Skill addSkill(L2Skill newSkill)
	{
		addCustomSkill(newSkill);
		return super.addSkill(newSkill);
	}
	
	public L2Skill addSkill(L2Skill newSkill, boolean store)
	{
		final L2Skill oldSkill = addSkill(newSkill);
		
		if (store)
		{
			storeSkill(newSkill, oldSkill, -1);
		}
		return oldSkill;
	}
	
	@Override
	public L2Skill removeSkill(L2Skill skill, boolean store)
	{
		removeCustomSkill(skill);
		return store ? removeSkill(skill) : super.removeSkill(skill, true);
	}
	
	public L2Skill removeSkill(L2Skill skill, boolean store, boolean cancelEffect)
	{
		removeCustomSkill(skill);
		return store ? removeSkill(skill) : super.removeSkill(skill, cancelEffect);
	}
	
	public L2Skill removeSkill(L2Skill skill)
	{
		removeCustomSkill(skill);
		final L2Skill oldSkill = super.removeSkill(skill, true);
		if (oldSkill != null)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR))
			{
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getClassIndex());
				statement.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Error could not delete skill: " + e.getMessage(), e);
			}
		}
		
		if ((getTransformationId() > 0) || isCursedWeaponEquipped())
		{
			return oldSkill;
		}
		
		final L2ShortCut[] allShortCuts = getAllShortCuts();
		for (L2ShortCut sc : allShortCuts)
		{
			if ((sc != null) && (skill != null) && (sc.getId() == skill.getId()) && (sc.getType() == L2ShortCut.TYPE_SKILL) && !((skill.getId() >= 3080) && (skill.getId() <= 3259)))
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		return oldSkill;
	}
	
	private void storeSkill(L2Skill newSkill, L2Skill oldSkill, int newClassIndex)
	{
		int classIndex = _classIndex;
		
		if (newClassIndex > -1)
		{
			classIndex = newClassIndex;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			
			if ((oldSkill != null) && (newSkill != null))
			{
				statement = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL);
				statement.setInt(1, newSkill.getLevel());
				statement.setInt(2, oldSkill.getId());
				statement.setInt(3, getObjectId());
				statement.setInt(4, classIndex);
				statement.execute();
				statement.close();
			}
			else if (newSkill != null)
			{
				statement = con.prepareStatement(ADD_NEW_SKILL);
				statement.setInt(1, getObjectId());
				statement.setInt(2, newSkill.getId());
				statement.setInt(3, newSkill.getLevel());
				statement.setInt(4, classIndex);
				statement.execute();
				statement.close();
			}
			else
			{
				_log.warning("could not store new skill. its NULL");
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error could not store char skills: " + e.getMessage(), e);
		}
	}
	
	private void restoreSkills()
	{
		boolean isAcumulative = Config.SUBCLASS_STORE_SKILL;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(isAcumulative ? RESTORE_SKILLS_FOR_CHAR_MODIFER : RESTORE_SKILLS_FOR_CHAR_DEFAULT))
		{
			statement.setInt(1, getObjectId());
			if (!isAcumulative)
			{
				statement.setInt(2, getClassIndex());
			}
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				if ((id > 9000) && (id < 9007))
				{
					continue;
				}
				
				L2Skill skill = SkillHolder.getInstance().getInfo(id, level);
				if (skill == null)
				{
					_log.warning("Skipped null skill Id: " + id + " Level: " + level + " while restoring player skills for playerObjId: " + getObjectId());
					continue;
				}
				
				addSkill(skill);
				
				if (Config.SKILL_CHECK_ENABLE && (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS) || Config.SKILL_CHECK_GM))
				{
					if (!SkillTreesParser.getInstance().isSkillAllowed(this, skill))
					{
						Util.handleIllegalPlayerAction(this, "Player " + getName() + " has invalid skill " + skill.getName() + " (" + skill.getId() + "/" + skill.getLevel() + "), class:" + ClassListParser.getInstance().getClass(getClassId()).getClassName(), 1);
						if (Config.SKILL_CHECK_REMOVE)
						{
							removeSkill(skill);
						}
					}
				}
			}
			rset.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore character " + this + " skills: " + e.getMessage(), e);
		}
	}
	
	@Override
	public void restoreEffects()
	{
		boolean isAcumulative = Config.SUBCLASS_STORE_SKILL;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(isAcumulative ? RESTORE_SKILL_SAVE_MODIFER : RESTORE_SKILL_SAVE))
		{
			statement.setInt(1, getObjectId());
			if (!isAcumulative)
			{
				statement.setInt(2, getClassIndex());
			}
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int effectCount = rset.getInt("effect_count");
					int effectCurTime = rset.getInt("effect_cur_time");
					long reuseDelay = rset.getLong("reuse_delay");
					long systime = rset.getLong("systime");
					int restoreType = rset.getInt("restore_type");
					
					final L2Skill skill = SkillHolder.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
					if (skill == null)
					{
						continue;
					}
					
					final long remainingTime = systime - System.currentTimeMillis();
					if (remainingTime > 10)
					{
						disableSkill(skill, remainingTime);
						addTimeStamp(skill, reuseDelay, systime);
					}
					
					if (restoreType > 0)
					{
						continue;
					}
					
					if (skill.hasEffects())
					{
						Env env = new Env();
						env.setCharacter(this);
						env.setTarget(this);
						env.setSkill(skill);
						
						L2Effect ef;
						for (EffectTemplate et : skill.getEffectTemplates())
						{
							ef = et.getEffect(env);
							if (ef != null)
							{
								ef.setCount(effectCount);
								ef.setFirstTime(effectCurTime);
								ef.scheduleEffect();
							}
						}
					}
				}
			}
			try (PreparedStatement del = con.prepareStatement(isAcumulative ? DELETE_SKILL_SAVE_MODIFER : DELETE_SKILL_SAVE))
			{
				del.setInt(1, getObjectId());
				if (!isAcumulative)
				{
					del.setInt(2, getClassIndex());
				}
				del.executeUpdate();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore " + this + " active effect data: " + e.getMessage(), e);
		}
	}
	
	private void restoreItemReuse()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_ITEM_REUSE_SAVE);
			statement.setInt(1, getObjectId());
			final ResultSet rset = statement.executeQuery();
			int itemId;
			@SuppressWarnings("unused")
			int itemObjId;
			long reuseDelay;
			long systime;
			boolean isInInventory;
			long remainingTime;
			while (rset.next())
			{
				itemId = rset.getInt("itemId");
				itemObjId = rset.getInt("itemObjId");
				reuseDelay = rset.getLong("reuseDelay");
				systime = rset.getLong("systime");
				isInInventory = true;
				
				L2ItemInstance item = getInventory().getItemByItemId(itemId);
				if (item == null)
				{
					item = getWarehouse().getItemByItemId(itemId);
					isInInventory = false;
				}
				
				if ((item != null) && (item.getId() == itemId) && (item.getReuseDelay() > 0))
				{
					remainingTime = systime - System.currentTimeMillis();
					
					if (remainingTime > 10)
					{
						addTimeStampItem(item, reuseDelay, systime);
						
						if (isInInventory && item.isEtcItem())
						{
							final int group = item.getSharedReuseGroup();
							if (group > 0)
							{
								sendPacket(new ExUseSharedGroupItem(itemId, group, (int) remainingTime, (int) reuseDelay));
							}
						}
					}
				}
			}
			rset.close();
			statement.close();
			
			statement = con.prepareStatement(DELETE_ITEM_REUSE_SAVE);
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore " + this + " Item Reuse data: " + e.getMessage(), e);
		}
	}
	
	private void restoreHenna()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			for (int i = 0; i < 3; i++)
			{
				_henna[i] = null;
			}
			
			int slot;
			int symbolId;
			while (rset.next())
			{
				slot = rset.getInt("slot");
				if ((slot < 1) || (slot > 3))
				{
					continue;
				}
				
				symbolId = rset.getInt("symbol_id");
				if (symbolId == 0)
				{
					continue;
				}
				_henna[slot - 1] = HennaParser.getInstance().getHenna(symbolId);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed restoing character " + this + " hennas.", e);
		}
		recalcHennaStats();
	}
	
	public int getHennaEmptySlots()
	{
		int totalSlots = 0;
		if (getClassId().level() == 1)
		{
			totalSlots = 2;
		}
		else
		{
			totalSlots = 3;
		}
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] != null)
			{
				totalSlots--;
			}
		}
		
		if (totalSlots <= 0)
		{
			return 0;
		}
		
		return totalSlots;
	}
	
	public boolean removeHenna(int slot)
	{
		if (!fireHennaListeners(getHenna(slot + 1), false))
		{
			return false;
		}
		
		if ((slot < 1) || (slot > 3))
		{
			return false;
		}
		
		slot--;
		
		L2Henna henna = _henna[slot];
		if (henna == null)
		{
			return false;
		}
		
		_henna[slot] = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getClassIndex());
			
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed remocing character henna.", e);
		}
		recalcHennaStats();
		sendPacket(new HennaInfo(this));
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		getInventory().addItem("Henna", henna.getDyeItemId(), henna.getCancelCount(), this, null);
		reduceAdena("Henna", henna.getCancelFee(), this, false);
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(henna.getDyeItemId());
		sm.addItemNumber(henna.getCancelCount());
		sendPacket(sm);
		sendPacket(SystemMessageId.SYMBOL_DELETED);
		return true;
	}
	
	public boolean addHenna(L2Henna henna)
	{
		if (!fireHennaListeners(henna, true))
		{
			return false;
		}
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				_henna[i] = henna;
				recalcHennaStats();
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA))
				{
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getDyeId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getClassIndex());
					
					statement.execute();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Failed saving character henna.", e);
				}
				sendPacket(new HennaInfo(this));
				sendPacket(new UserInfo(this));
				sendPacket(new ExBrExtraUserInfo(this));
				
				return true;
			}
		}
		return false;
	}
	
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		
		for (L2Henna h : _henna)
		{
			if (h == null)
			{
				continue;
			}
			
			_hennaINT += ((_hennaINT + h.getStatINT()) > 5) ? 5 - _hennaINT : h.getStatINT();
			_hennaSTR += ((_hennaSTR + h.getStatSTR()) > 5) ? 5 - _hennaSTR : h.getStatSTR();
			_hennaMEN += ((_hennaMEN + h.getStatMEN()) > 5) ? 5 - _hennaMEN : h.getStatMEN();
			_hennaCON += ((_hennaCON + h.getStatCON()) > 5) ? 5 - _hennaCON : h.getStatCON();
			_hennaWIT += ((_hennaWIT + h.getStatWIT()) > 5) ? 5 - _hennaWIT : h.getStatWIT();
			_hennaDEX += ((_hennaDEX + h.getStatDEX()) > 5) ? 5 - _hennaDEX : h.getStatDEX();
		}
	}
	
	public L2Henna getHenna(int slot)
	{
		if ((slot < 1) || (slot > 3))
		{
			return null;
		}
		return _henna[slot - 1];
	}
	
	public boolean hasHennas()
	{
		for (L2Henna henna : _henna)
		{
			if (henna != null)
			{
				return true;
			}
		}
		return false;
	}
	
	public L2Henna[] getHennaList()
	{
		return _henna;
	}
	
	public int getHennaStatINT()
	{
		return _hennaINT;
	}
	
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}
	
	public int getHennaStatCON()
	{
		return _hennaCON;
	}
	
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}
	
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}
	
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker == null)
		{
			return false;
		}
		
		if ((attacker == this) || (attacker == getSummon()))
		{
			return false;
		}
		
		if (attacker instanceof L2PcInstance)
		{
			if (GlobalRestrictions.fakePvPZone((L2PcInstance) attacker, this))
			{
				return true;
			}
			if (isCombatFlagEquipped() && (((L2PcInstance) attacker).getSiegeSide() != 0))
			{
				return true;
			}
		}
		
		if (attacker instanceof L2FriendlyMobInstance)
		{
			return false;
		}
		
		if (attacker.isMonster())
		{
			return true;
		}
		
		if (attacker.isPlayer() && (getDuelState() == Duel.DUELSTATE_DUELLING) && (getDuelId() == ((L2PcInstance) attacker).getDuelId()))
		{
			return true;
		}
		
		if (isInParty() && getParty().getMembers().contains(attacker))
		{
			return false;
		}
		
		if (attacker.isPlayer() && attacker.getActingPlayer().isInOlympiadMode())
		{
			if (isInOlympiadMode() && isOlympiadStart() && (((L2PcInstance) attacker).getOlympiadGameId() == getOlympiadGameId()))
			{
				return true;
			}
			return false;
		}
		
		if (isOnEvent())
		{
			return true;
		}
		
		if (isInTownWarEvent())
		{
			return true;
		}
		
		if (attacker.isPlayable())
		{
			if (isInsideZone(ZoneId.PEACE))
			{
				return false;
			}
			
			L2PcInstance attackerPlayer = attacker.getActingPlayer();
			
			if (getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				if (siege != null)
				{
					if (siege.checkIsDefender(attackerPlayer.getClan()) && siege.checkIsDefender(getClan()))
					{
						return false;
					}
					
					if (siege.checkIsAttacker(attackerPlayer.getClan()) && siege.checkIsAttacker(getClan()))
					{
						return false;
					}
				}
				
				if ((getClan() != null) && (attackerPlayer.getClan() != null) && getClan().isAtWarWith(attackerPlayer.getClanId()) && attackerPlayer.getClan().isAtWarWith(getClanId()) && (getWantsPeace() == 0) && (attackerPlayer.getWantsPeace() == 0) && !isAcademyMember())
				{
					return true;
				}
			}
			
			if ((isInsideZone(ZoneId.PVP) && attackerPlayer.isInsideZone(ZoneId.PVP)) && !(isInsideZone(ZoneId.SIEGE) && attackerPlayer.isInsideZone(ZoneId.SIEGE)))
			{
				return true;
			}
			
			if ((getClan() != null) && getClan().isMember(attacker.getObjectId()))
			{
				return false;
			}
			
			if (attacker.isPlayer() && (getAllyId() != 0) && (getAllyId() == attackerPlayer.getAllyId()))
			{
				return false;
			}
			
			if ((isInsideZone(ZoneId.PVP) && attackerPlayer.isInsideZone(ZoneId.PVP)) && (isInsideZone(ZoneId.SIEGE) && attackerPlayer.isInsideZone(ZoneId.SIEGE)))
			{
				return true;
			}
		}
		else if (attacker instanceof L2DefenderInstance)
		{
			if (getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(this);
				return ((siege != null) && siege.checkIsAttacker(getClan()));
			}
		}
		
		if ((getKarma() > 0) || (getPvpFlag() > 0))
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (skill.isPassive())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isCastingNow())
		{
			SkillUseHolder currentSkill = getCurrentSkill();
			
			if ((currentSkill != null) && (skill.getId() == currentSkill.getSkillId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (isSkillDisabled(skill))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			setQueuedSkill(skill, forceUse, dontMove);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		setIsCastingNow(true);
		setCurrentSkill(skill, forceUse, dontMove);
		
		if (getQueuedSkill() != null)
		{
			setQueuedSkill(null, false, false);
		}
		
		if (!checkUseMagicConditions(skill, forceUse, dontMove))
		{
			setIsCastingNow(false);
			return false;
		}
		
		L2Object target = null;
		switch (skill.getTargetType())
		{
			case AURA:
			case FRONT_AURA:
			case BEHIND_AURA:
			case GROUND:
			case SELF:
			case AURA_CORPSE_MOB:
				target = this;
				break;
			default:
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		if ((target != this) && (target instanceof L2PcInstance) && skill.isOffensive() && isPKProtected((L2PcInstance) target))
		{
			setIsCastingNow(false);
			sendMessage("You cannot attack player with too low level.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		return true;
	}
	
	private boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		L2SkillType sklType = skill.getSkillType();
		
		if (isOutOfControl() || isParalyzed() || isStunned() || isSleeping())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isFishing() && ((sklType != L2SkillType.PUMPING) && (sklType != L2SkillType.REELING) && (sklType != L2SkillType.FISHING)))
		{
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return false;
		}
		
		if (inObserverMode())
		{
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isSitting())
		{
			sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (skill.isToggle())
		{
			L2Effect effect = getFirstEffect(skill.getId());
			
			if (effect != null)
			{
				effect.exit();
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		if (isFakeDeath())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		L2Object target = null;
		L2TargetType sklTargetType = skill.getTargetType();
		Location worldPosition = getCurrentSkillWorldPosition();
		
		if ((sklTargetType == L2TargetType.GROUND) && (worldPosition == null))
		{
			_log.info("WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		switch (sklTargetType)
		{
			case AURA:
			case FRONT_AURA:
			case BEHIND_AURA:
			case PARTY:
			case CLAN:
			case PARTY_CLAN:
			case GROUND:
			case SELF:
			case AREA_SUMMON:
			case AURA_CORPSE_MOB:
				target = this;
				break;
			case PET:
			case SERVITOR:
			case SUMMON:
				target = getSummon();
				break;
			default:
				target = getTarget();
				break;
		}
		
		if (target == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (target.isDoor())
		{
			if ((((L2DoorInstance) target).getCastle() != null) && (((L2DoorInstance) target).getCastle().getId() > 0))
			{
				if (!((L2DoorInstance) target).getCastle().getSiege().getIsInProgress())
				{
					return false;
				}
			}
			else if ((((L2DoorInstance) target).getFort() != null) && (((L2DoorInstance) target).getFort().getId() > 0) && !((L2DoorInstance) target).getIsShowHp())
			{
				if (!((L2DoorInstance) target).getFort().getSiege().getIsInProgress())
				{
					return false;
				}
			}
		}
		
		if (isInDuel())
		{
			if (target instanceof L2Playable)
			{
				L2PcInstance cha = target.getActingPlayer();
				if (cha.getDuelId() != getDuelId())
				{
					sendMessage("You cannot do this while duelling.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		// if (isInOlympiadMode() && (target == null) && (target != this) && (target != target.getActingPlayer().getParty().getMembers()) && (target.isPlayer()))
		// {
		// if ((skill.hasEffectType(L2EffectType.HEAL)))
		// {
		// sendPacket(SystemMessageId.INCORRECT_TARGET);
		// sendPacket(ActionFailed.STATIC_PACKET);
		// return false;
		// }
		// }
		
		if (isFightingInEvent() && (target instanceof L2PcInstance))
		{
			L2PcInstance p_target = (L2PcInstance) target;
			
			if ((skill.getTargetType() == L2TargetType.ONE) && isInSameTeam(p_target))
			{
				if ((getEventName().equals("CTF") && !Config.CTF_ALLOW_TEAM_CASTING) || (getEventName().equals("BW") && !Config.BW_ALLOW_TEAM_CASTING) || (getEventName().equals("DM") && !Config.DM_ALLOW_TEAM_CASTING))
				{
					sendPacket(SystemMessageId.INCORRECT_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			if ((skill.hasEffectType(L2EffectType.HEAL)) && isInSameEvent(p_target) && !isInSameTeam(p_target))
			{
				if ((getEventName().equals("CTF") && !Config.CTF_ALLOW_ENEMY_HEALING) || (getEventName().equals("BW") && !Config.BW_ALLOW_ENEMY_HEALING) || (getEventName().equals("DM") && !Config.DM_ALLOW_ENEMY_HEALING))
				{
					sendPacket(SystemMessageId.INCORRECT_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			if ((skill.getTargetType() == L2TargetType.ONE) && isInSameTeam(p_target))
			{
				if ((getEventName().equals("CTF") && !Config.CTF_ALLOW_TEAM_ATTACKING) || (getEventName().equals("BW") && !Config.BW_ALLOW_TEAM_ATTACKING) || (getEventName().equals("DM") && !Config.DM_ALLOW_TEAM_ATTACKING))
				{
					sendPacket(SystemMessageId.INCORRECT_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		if ((skill.getTargetType() == L2TargetType.ONE) || (skill.getTargetType() == L2TargetType.AREA) || (skill.getTargetType() == L2TargetType.FRONT_AREA) || (skill.getTargetType() == L2TargetType.BEHIND_AREA))
		{
			if (target instanceof L2CustomCTFFlagInstance)
			{
				return false;
			}
			if ((target instanceof L2CustomBWBaseInstance) && (!((L2CustomBWBaseInstance) target).canAttack(this) || !skill.isEffectTypeBattle()))
			{
				return false;
			}
		}
		
		if (((skill.getId() == 13) || (skill.getId() == 299) || (skill.getId() == 448)) && ((!SiegeManager.getInstance().checkIfOkToSummon(this, false) && !FortSiegeManager.getInstance().checkIfOkToSummon(this, false)) || (SevenSigns.getInstance().checkSummonConditions(this))))
		{
			return false;
		}
		
		if (isSkillDisabled(skill))
		{
			SystemMessage sm = null;
			if (_reuseTimeStampsSkills.containsKey(skill.getReuseHashCode()))
			{
				int remainingTime = (int) (_reuseTimeStampsSkills.get(skill.getReuseHashCode()).getRemaining() / 1000);
				int hours = remainingTime / 3600;
				int minutes = (remainingTime % 3600) / 60;
				int seconds = (remainingTime % 60);
				if (hours > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
					sm.addSkillName(skill);
					sm.addNumber(hours);
					sm.addNumber(minutes);
				}
				else if (minutes > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
					sm.addSkillName(skill);
					sm.addNumber(minutes);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
					sm.addSkillName(skill);
				}
				
				sm.addNumber(seconds);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill);
			}
			sendPacket(sm);
			return false;
		}
		
		if (isFightingInEvent())
		{
			if ((sklType == L2SkillType.SUMMON) && (skill instanceof L2SkillSummon))
			{
				if ((getEventName().equals("CTF") && !Config.CTF_ALLOW_SUMMON) || (getEventName().equals("BW") && !Config.BW_ALLOW_SUMMON) || (getEventName().equals("DM") && !Config.DM_ALLOW_SUMMON))
				{
					if (((L2SkillSummon) skill).getSummonTemplate() != null)
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SUMMON_S1_ON_BATTLEGROUND).addNpcName(((L2SkillSummon) skill).getSummonTemplate()));
					}
					return false;
				}
			}
		}
		
		if (!skill.checkCondition(this, target, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (skill.isOffensive())
		{
			if ((isInsidePeaceZone(this, target)) && !getAccessLevel().allowPeaceAttack())
			{
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (isInOlympiadMode() && !isOlympiadStart())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if ((target.getActingPlayer() != null) && (getSiegeState() > 0) && isInsideZone(ZoneId.SIEGE) && (target.getActingPlayer().getSiegeState() == getSiegeState()) && (target.getActingPlayer() != this) && (target.getActingPlayer().getSiegeSide() == getSiegeSide()))
			{
				if (TerritoryWarManager.getInstance().isTWInProgress())
				{
					sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
				}
				else
				{
					sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
				}
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			switch (skill.getSkillType())
			{
				case UNLOCK:
				case UNLOCK_SPECIAL:
				case DELUXE_KEY_UNLOCK:
				{
					break;
				}
				default:
				{
					if (!target.isAttackable() && !getAccessLevel().allowPeaceAttack())
					{
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				}
			}
			
			if ((target instanceof L2EventMonsterInstance) && ((L2EventMonsterInstance) target).eventSkillAttackBlocked())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (!target.isAutoAttackable(this) && !forceUse)
			{
				switch (sklTargetType)
				{
					case AURA:
					case FRONT_AURA:
					case BEHIND_AURA:
					case AURA_CORPSE_MOB:
					case CLAN:
					case PARTY:
					case SELF:
					case GROUND:
					case AREA_SUMMON:
					case UNLOCKABLE:
						break;
					default:
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
				}
			}
			
			if (dontMove)
			{
				if (sklTargetType == L2TargetType.GROUND)
				{
					if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), skill.getCastRange() + getTemplate().getCollisionRadius(), false, false))
					{
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				}
				else if ((skill.getCastRange() > 0) && !isInsideRadius(target, skill.getCastRange() + getTemplate().getCollisionRadius(), false, false))
				{
					sendPacket(SystemMessageId.TARGET_TOO_FAR);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		if (skill.hasEffectType(L2EffectType.TELEPORT_TO_TARGET))
		{
			if (isMovementDisabled())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill.getId());
				sendPacket(sm);
				sendPacket(ActionFailed.STATIC_PACKET);
				
				return false;
			}
			
			if (isInsideZone(ZoneId.PEACE) && !isInTownWarEvent())
			{
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				
				return false;
			}
			
		}
		
		if (!skill.isOffensive() && target.isMonster() && !forceUse && !skill.isNeutral())
		{
			switch (sklTargetType)
			{
				case PET:
				case SERVITOR:
				case SUMMON:
				case AURA:
				case FRONT_AURA:
				case BEHIND_AURA:
				case AURA_CORPSE_MOB:
				case CLAN:
				case PARTY_CLAN:
				case SELF:
				case PARTY:
				case CORPSE_MOB:
				case AREA_CORPSE_MOB:
				case GROUND:
					break;
				default:
				{
					switch (sklType)
					{
						case DELUXE_KEY_UNLOCK:
						case UNLOCK:
							break;
						default:
							sendPacket(ActionFailed.STATIC_PACKET);
							return false;
					}
					break;
				}
			}
		}
		
		switch (sklTargetType)
		{
			case PARTY:
			case CLAN:
			case PARTY_CLAN:
			case AURA:
			case FRONT_AURA:
			case BEHIND_AURA:
			case GROUND:
			case SELF:
				break;
			default:
				if (!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack())
				{
					sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
		}
		
		if (((sklTargetType == L2TargetType.HOLY) && !checkIfOkToCastSealOfRule(CastleManager.getInstance().getCastle(this), false, skill, target)) || ((sklTargetType == L2TargetType.FLAGPOLE) && !checkIfOkToCastFlagDisplay(FortManager.getInstance().getFort(this), false, skill, target)) || ((sklType == L2SkillType.SIEGEFLAG) && !L2SkillSiegeFlag.checkIfOkToPlaceFlag(this, false, skill.getId() == 844)))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return false;
		}
		
		if (skill.getCastRange() > 0)
		{
			if (sklTargetType == L2TargetType.GROUND)
			{
				if (!GeoClient.getInstance().canSeeTarget(this, worldPosition))
				{
					sendPacket(SystemMessageId.CANT_SEE_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else if (!GeoClient.getInstance().canSeeTarget(this, target))
			{
				sendPacket(SystemMessageId.CANT_SEE_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		return true;
	}
	
	public boolean checkIfOkToCastSealOfRule(Castle castle, boolean isCheckOnly, L2Skill skill, L2Object target)
	{
		SystemMessage sm;
		if ((castle == null) || (castle.getId() <= 0))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else if (!castle.getArtefacts().contains(target))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else if (!Util.checkIfInRange(200, this, target, true))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
		}
		else if (castle.getSiege().getAttackerClan(getClan()) == null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else
		{
			if (!isCheckOnly)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING);
				castle.getSiege().announceToPlayer(sm, false);
			}
			return true;
		}
		
		sendPacket(sm);
		return false;
	}
	
	public boolean checkIfOkToCastFlagDisplay(Fort fort, boolean isCheckOnly, L2Skill skill, L2Object target)
	{
		SystemMessage sm;
		
		if ((fort == null) || (fort.getId() <= 0))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else if (fort.getFlagPole() != target)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else if (!Util.checkIfInRange(200, this, target, true))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
		}
		else if (fort.getSiege().getAttackerClan(getClan()) == null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else
		{
			if (!isCheckOnly)
			{
				fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_TRYING_RAISE_FLAG), getClan().getName());
			}
			return true;
		}
		
		sendPacket(sm);
		return false;
	}
	
	public boolean isInLooterParty(int LooterId)
	{
		L2PcInstance looter = L2World.getInstance().getPlayer(LooterId);
		
		if (isInParty() && getParty().isInCommandChannel() && (looter != null))
		{
			return getParty().getCommandChannel().getMembers().contains(looter);
		}
		
		if (isInParty() && (looter != null))
		{
			return getParty().getMembers().contains(looter);
		}
		
		return false;
	}
	
	public boolean checkPvpSkill(L2Object target, L2Skill skill)
	{
		return checkPvpSkill(target, skill, false);
	}
	
	public boolean checkPvpSkill(L2Object target, L2Skill skill, boolean srcIsSummon)
	{
		final L2PcInstance targetPlayer = target != null ? target.getActingPlayer() : null;
		if (!(target instanceof L2EventChestInstance) && (targetPlayer != null) && (target != this) && !(isInDuel() && (targetPlayer.getDuelId() == getDuelId())) && !isInsideZone(ZoneId.PVP) && !targetPlayer.isInsideZone(ZoneId.PVP))
		{
			SkillUseHolder skilldat = getCurrentSkill();
			SkillUseHolder skilldatpet = getCurrentPetSkill();
			if (skill.isPVP())
			{
				if ((getClan() != null) && (targetPlayer.getClan() != null))
				{
					if (getClan().isAtWarWith(targetPlayer.getClan().getId()) && targetPlayer.getClan().isAtWarWith(getClan().getId()))
					{
						return true;
					}
				}
				if ((targetPlayer.getPvpFlag() == 0) && (targetPlayer.getKarma() == 0))
				{
					return false;
				}
			}
			else if (((skilldat != null) && !skilldat.isCtrlPressed() && skill.isOffensive() && !srcIsSummon) || ((skilldatpet != null) && !skilldatpet.isCtrlPressed() && skill.isOffensive() && srcIsSummon))
			{
				if ((getClan() != null) && (targetPlayer.getClan() != null))
				{
					if (getClan().isAtWarWith(targetPlayer.getClan().getId()) && targetPlayer.getClan().isAtWarWith(getClan().getId()))
					{
						return true;
					}
				}
				if ((targetPlayer.getPvpFlag() == 0) && (targetPlayer.getKarma() == 0))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}
	
	public boolean isMounted()
	{
		return _mountType != MountType.NONE;
	}
	
	public boolean checkLandingState()
	{
		if (isInsideZone(ZoneId.NO_LANDING))
		{
			return true;
		}
		else if (isInsideZone(ZoneId.SIEGE) && !((getClan() != null) && (CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan())) && (this == getClan().getLeader().getPlayerInstance())))
		{
			return true;
		}
		
		return false;
	}
	
	public void setMount(int npcId, int npcLevel)
	{
		final MountType type = MountType.findByNpcId(npcId);
		switch (type)
		{
			case NONE:
			{
				setIsFlying(false);
				break;
			}
			case STRIDER:
			{
				if (isNoble())
				{
					addSkill(FrequentSkill.STRIDER_SIEGE_ASSAULT.getSkill(), false);
				}
				break;
			}
			case WYVERN:
			{
				setIsFlying(true);
				break;
			}
		}
		
		_mountType = type;
		_mountNpcId = npcId;
		_mountLevel = npcLevel;
	}
	
	public MountType getMountType()
	{
		return _mountType;
	}
	
	@Override
	public final void stopAllEffects()
	{
		super.stopAllEffects();
		updateAndBroadcastStatus(2);
	}
	
	@Override
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		updateAndBroadcastStatus(2);
	}
	
	public final void stopAllEffectsNotStayOnSubclassChange()
	{
		for (L2Effect effect : _effects.getAllEffects())
		{
			if ((effect != null) && !effect.getSkill().isStayOnSubclassChange())
			{
				effect.exit(true);
			}
		}
		updateAndBroadcastStatus(2);
	}
	
	public final void stopAllToggles()
	{
		_effects.stopAllToggles();
	}
	
	public final void stopCubics()
	{
		if (!_cubics.isEmpty())
		{
			for (L2CubicInstance cubic : _cubics)
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
			broadcastUserInfo();
		}
	}
	
	public final void stopCubicsByOthers()
	{
		if (!_cubics.isEmpty())
		{
			boolean broadcast = false;
			for (L2CubicInstance cubic : _cubics)
			{
				if (cubic.givenByOther())
				{
					cubic.stopAction();
					cubic.cancelDisappear();
					_cubics.remove(cubic);
					broadcast = true;
				}
			}
			if (broadcast)
			{
				broadcastUserInfo();
			}
		}
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		broadcastUserInfo();
	}
	
	public void setInventoryBlockingStatus(boolean val)
	{
		_inventoryDisable = val;
		if (val)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnableTask(this), 1500);
		}
	}
	
	public boolean isInventoryDisabled()
	{
		return _inventoryDisable;
	}
	
	public List<L2CubicInstance> getCubics()
	{
		return _cubics;
	}
	
	public void addCubic(int id, int level, double cubicPower, int cubicDelay, int cubicSkillChance, int cubicMaxCount, int cubicDuration, boolean givenByOther)
	{
		_cubics.add(new L2CubicInstance(this, id, level, (int) cubicPower, cubicDelay, cubicSkillChance, cubicMaxCount, cubicDuration, givenByOther));
	}
	
	public L2CubicInstance getCubicById(int id)
	{
		for (L2CubicInstance c : _cubics)
		{
			if (c.getId() == id)
			{
				return c;
			}
		}
		return null;
	}
	
	public int getEnchantEffect()
	{
		L2ItemInstance wpn = getActiveWeaponInstance();
		
		if (wpn == null)
		{
			return 0;
		}
		
		return Math.min(127, wpn.getEnchantLevel());
	}
	
	public void setLastFolkNPC(L2Npc folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}
	
	public L2Npc getLastFolkNPC()
	{
		return _lastFolkNpc;
	}
	
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isParticipant(this);
	}
	
	public void addAutoSoulShot(int itemId)
	{
		_activeSoulShots.add(itemId);
	}
	
	public boolean removeAutoSoulShot(int itemId)
	{
		return _activeSoulShots.remove(itemId);
	}
	
	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic)
	{
		L2ItemInstance item;
		IItemHandler handler;
		
		if ((_activeSoulShots == null) || _activeSoulShots.isEmpty())
		{
			return;
		}
		
		for (int itemId : _activeSoulShots)
		{
			item = getInventory().getItemByItemId(itemId);
			
			if (item != null)
			{
				if (magic)
				{
					if (item.getItem().getDefaultAction() == L2ActionType.spiritshot)
					{
						handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null)
						{
							handler.useItem(this, item, false);
						}
					}
				}
				
				if (physical)
				{
					if (item.getItem().getDefaultAction() == L2ActionType.soulshot)
					{
						handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null)
						{
							handler.useItem(this, item, false);
						}
					}
				}
			}
			else
			{
				removeAutoSoulShot(itemId);
			}
		}
	}
	
	public void disableAutoShotByCrystalType(int crystalType)
	{
		for (int itemId : _activeSoulShots)
		{
			if (ItemHolder.getInstance().getTemplate(itemId).getCrystalType() == crystalType)
			{
				disableAutoShot(itemId);
			}
		}
	}
	
	public boolean disableAutoShot(int itemId)
	{
		if (_activeSoulShots.contains(itemId))
		{
			removeAutoSoulShot(itemId);
			sendPacket(new ExAutoSoulShot(itemId, 0));
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
			sm.addItemName(itemId);
			sendPacket(sm);
			return true;
		}
		return false;
	}
	
	public void disableAutoShotsAll()
	{
		for (int itemId : _activeSoulShots)
		{
			sendPacket(new ExAutoSoulShot(itemId, 0));
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
			sm.addItemName(itemId);
			sendPacket(sm);
		}
		_activeSoulShots.clear();
	}
	
	private ScheduledFuture<?> _taskWarnUserTakeBreak;
	
	public int getClanPrivileges()
	{
		return _clanPrivileges;
	}
	
	public void setClanPrivileges(int n)
	{
		_clanPrivileges = n;
	}
	
	public void setPledgeClass(int classId)
	{
		_pledgeClass = classId;
		checkItemRestriction();
	}
	
	public int getPledgeClass()
	{
		return _pledgeClass;
	}
	
	public void setPledgeType(int typeId)
	{
		_pledgeType = typeId;
	}
	
	public int getPledgeType()
	{
		return _pledgeType;
	}
	
	public int getApprentice()
	{
		return _apprentice;
	}
	
	public void setApprentice(int apprentice_id)
	{
		_apprentice = apprentice_id;
	}
	
	public int getSponsor()
	{
		return _sponsor;
	}
	
	public void setSponsor(int sponsor_id)
	{
		_sponsor = sponsor_id;
	}
	
	public int getBookMarkSlot()
	{
		return _bookmarkslot;
	}
	
	public void setBookMarkSlot(int slot)
	{
		_bookmarkslot = slot;
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}
	
	@Override
	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}
	
	public void enterObserverMode(int x, int y, int z)
	{
		_lastX = getX();
		_lastY = getY();
		_lastZ = getZ();
		
		stopEffects(L2EffectType.HIDE);
		
		_observerMode = true;
		setTarget(null);
		setIsParalyzed(true);
		startParalyze();
		setIsInvul(true);
		setInvisible(true);
		sendPacket(new ObservationMode(x, y, z));
		getKnownList().removeAllKnownObjects();
		setXYZ(x, y, z);
		broadcastUserInfo();
	}
	
	public void setLastCords(int x, int y, int z)
	{
		_lastX = getX();
		_lastY = getY();
		_lastZ = getZ();
	}
	
	public void enterOlympiadObserverMode(Location loc, int id)
	{
		if (hasSummon())
		{
			getSummon().unSummon(this);
		}
		
		stopEffects(L2EffectType.HIDE);
		
		if (!_cubics.isEmpty())
		{
			for (L2CubicInstance cubic : _cubics)
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
		}
		
		if (getParty() != null)
		{
			getParty().removePartyMember(this, messageType.Expelled);
		}
		
		_olympiadGameId = id;
		if (isSitting())
		{
			standUp();
		}
		if (!_observerMode)
		{
			_lastX = getX();
			_lastY = getY();
			_lastZ = getZ();
		}
		
		_observerMode = true;
		setTarget(null);
		setIsInvul(true);
		setInvisible(true);
		teleToLocation(loc, false);
		sendPacket(new ExOlympiadMode(3));
		
		broadcastUserInfo();
	}
	
	public void leaveObserverMode()
	{
		setTarget(null);
		getKnownList().removeAllKnownObjects();
		setXYZ(_lastX, _lastY, _lastZ);
		setIsParalyzed(false);
		if (!isGM())
		{
			setInvisible(false);
			setIsInvul(false);
		}
		if (hasAI())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		setFalling();
		_observerMode = false;
		setLastCords(0, 0, 0);
		sendPacket(new ObservationReturn(this));
		broadcastUserInfo();
	}
	
	public void leaveOlympiadObserverMode()
	{
		if (_olympiadGameId == -1)
		{
			return;
		}
		_olympiadGameId = -1;
		_observerMode = false;
		setTarget(null);
		sendPacket(new ExOlympiadMode(0));
		setInstanceId(0);
		teleToLocation(_lastX, _lastY, _lastZ, true);
		if (!isGM())
		{
			setInvisible(false);
			setIsInvul(false);
		}
		if (hasAI())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		setLastCords(0, 0, 0);
		broadcastUserInfo();
	}
	
	public void setOlympiadSide(int i)
	{
		_olympiadSide = i;
	}
	
	public int getOlympiadSide()
	{
		return _olympiadSide;
	}
	
	public void setOlympiadGameId(int id)
	{
		_olympiadGameId = id;
	}
	
	public int getOlympiadGameId()
	{
		return _olympiadGameId;
	}
	
	public int getLastX()
	{
		return _lastX;
	}
	
	public int getLastY()
	{
		return _lastY;
	}
	
	public int getLastZ()
	{
		return _lastZ;
	}
	
	public boolean inObserverMode()
	{
		return _observerMode;
	}
	
	public int getTeleMode()
	{
		return _telemode;
	}
	
	public void setTeleMode(int mode)
	{
		_telemode = mode;
	}
	
	public void setLoto(int i, int val)
	{
		_loto[i] = val;
	}
	
	public int getLoto(int i)
	{
		return _loto[i];
	}
	
	public void setRace(int i, int val)
	{
		_race[i] = val;
	}
	
	public int getRace(int i)
	{
		return _race[i];
	}
	
	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}
	
	public void setMessageRefusal(boolean mode)
	{
		_messageRefusal = mode;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void setDietMode(boolean mode)
	{
		_dietMode = mode;
	}
	
	public boolean getDietMode()
	{
		return _dietMode;
	}
	
	public void setTradeRefusal(boolean mode)
	{
		_tradeRefusal = mode;
	}
	
	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}
	
	public void setExchangeRefusal(boolean mode)
	{
		_exchangeRefusal = mode;
	}
	
	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}
	
	public BlockList getBlockList()
	{
		return _blockList;
	}
	
	public void setHero(boolean hero)
	{
		if (hero && (_baseClass == _activeClass))
		{
			for (L2Skill skill : SkillTreesParser.getInstance().getHeroSkillTree().values())
			{
				addSkill(skill, false);
			}
		}
		else
		{
			for (L2Skill skill : SkillTreesParser.getInstance().getHeroSkillTree().values())
			{
				removeSkill(skill, false, true);
			}
		}
		_hero = hero;
		
		sendSkillList();
	}
	
	public void setIsInOlympiadMode(boolean b)
	{
		_inOlympiadMode = b;
	}
	
	public void setIsOlympiadStart(boolean b)
	{
		_OlympiadStart = b;
	}
	
	public boolean isOlympiadStart()
	{
		return _OlympiadStart;
	}
	
	public boolean isHero()
	{
		return _hero;
	}
	
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}
	
	public boolean isInDuel()
	{
		return _isInDuel;
	}
	
	public int getDuelId()
	{
		return _duelId;
	}
	
	public void setDuelState(int mode)
	{
		_duelState = mode;
	}
	
	public int getDuelState()
	{
		return _duelState;
	}
	
	public void setIsInDuel(int duelId)
	{
		if (duelId > 0)
		{
			_isInDuel = true;
			_duelState = Duel.DUELSTATE_DUELLING;
			_duelId = duelId;
		}
		else
		{
			if (_duelState == Duel.DUELSTATE_DEAD)
			{
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			_isInDuel = false;
			_duelState = Duel.DUELSTATE_NODUEL;
			_duelId = 0;
		}
	}
	
	public SystemMessage getNoDuelReason()
	{
		SystemMessage sm = SystemMessage.getSystemMessage(_noDuelReason);
		sm.addPcName(this);
		_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
		return sm;
	}
	
	public boolean canDuel()
	{
		if (isInCombat() || isJailed())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
			return false;
		}
		if (isDead() || isAlikeDead() || ((getCurrentHp() < (getMaxHp() / 2)) || (getCurrentMp() < (getMaxMp() / 2))))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_HP_OR_MP_IS_BELOW_50_PERCENT;
			return false;
		}
		if (isInDuel())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL;
			return false;
		}
		if (isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(this))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
			return false;
		}
		if (isCursedWeaponEquipped())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE;
			return false;
		}
		if (getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
			return false;
		}
		if (isMounted() || isInBoat())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER;
			return false;
		}
		if (isFishing())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING;
			return false;
		}
		if (isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.PEACE) || isInsideZone(ZoneId.SIEGE))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
			return false;
		}
		return true;
	}
	
	public boolean isNoble()
	{
		return _noble;
	}
	
	public void setNoble(boolean val)
	{
		if (Config.ENABLE_NOBLESS_COLOR)
		{
			getAppearance().setNameColor(Config.NOBLESS_COLOR_NAME);
		}
		if (Config.ENABLE_NOBLESS_TITLE_COLOR)
		{
			getAppearance().setTitleColor(Config.NOBLESS_COLOR_TITLE_NAME);
		}
		
		final Collection<L2Skill> nobleSkillTree = SkillTreesParser.getInstance().getNobleSkillTree().values();
		if (val)
		{
			for (L2Skill skill : nobleSkillTree)
			{
				addSkill(skill, false);
			}
		}
		else
		{
			for (L2Skill skill : nobleSkillTree)
			{
				removeSkill(skill, false, true);
			}
		}
		
		_noble = val;
		
		sendSkillList();
	}
	
	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}
	
	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}
	
	public boolean isAcademyMember()
	{
		return _lvlJoinedAcademy > 0;
	}
	
	@Override
	public void setTeam(int team)
	{
		super.setTeam(team);
		broadcastUserInfo();
		if (hasSummon())
		{
			getSummon().broadcastStatusUpdate();
		}
	}
	
	public void setWantsPeace(int wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}
	
	public int getWantsPeace()
	{
		return _wantsPeace;
	}
	
	public boolean isFishing()
	{
		return _fishing;
	}
	
	public void setFishing(boolean fishing)
	{
		_fishing = fishing;
	}
	
	public void sendSkillList()
	{
		sendSkillList(this);
	}
	
	public void sendSkillList(L2PcInstance player)
	{
		boolean isDisabled = false;
		SkillList sl = new SkillList();
		if (player != null)
		{
			for (L2Skill s : player.getAllSkills())
			{
				if (s == null)
				{
					continue;
				}
				
				if ((_transformation != null) && (!hasTransformSkill(s.getId()) && !s.allowOnTransform()))
				{
					continue;
				}
				
				if (player.getClan() != null)
				{
					isDisabled = s.isClanSkill() && (player.getClan().getReputationScore() < 0);
				}
				
				boolean isEnchantable = SkillHolder.getInstance().isEnchantable(s.getId());
				if (isEnchantable)
				{
					L2EnchantSkillLearn esl = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(s.getId());
					if (esl != null)
					{
						if (s.getLevel() < esl.getBaseLevel())
						{
							isEnchantable = false;
						}
					}
					else
					{
						isEnchantable = false;
					}
				}
				sl.addSkill(s.getDisplayId(), s.getDisplayLevel(), s.isPassive(), isDisabled, isEnchantable);
			}
		}
		sendPacket(sl);
	}
	
	public boolean addSubClass(int classId, int classIndex)
	{
		if (!_subclassLock.tryLock())
		{
			return false;
		}
		
		try
		{
			if ((getTotalSubClasses() == Config.MAX_SUBCLASS) || (classIndex == 0))
			{
				return false;
			}
			
			if (getSubClasses().containsKey(classIndex))
			{
				return false;
			}
			
			SubClass newClass = new SubClass();
			newClass.setClassId(classId);
			newClass.setClassIndex(classIndex);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS))
			{
				statement.setInt(1, getObjectId());
				statement.setInt(2, newClass.getClassId());
				statement.setLong(3, newClass.getExp());
				statement.setInt(4, newClass.getSp());
				statement.setInt(5, newClass.getLevel());
				statement.setInt(6, newClass.getClassIndex());
				
				statement.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "WARNING: Could not add character sub class for " + getName() + ": " + e.getMessage(), e);
				return false;
			}
			getSubClasses().put(newClass.getClassIndex(), newClass);
			
			final ClassId subTemplate = ClassId.getClassId(classId);
			final Map<Integer, L2SkillLearn> skillTree = SkillTreesParser.getInstance().getCompleteClassSkillTree(subTemplate);
			final Map<Integer, L2Skill> prevSkillList = new HashMap<>();
			
			for (L2SkillLearn skillInfo : skillTree.values())
			{
				if (skillInfo.getGetLevel() <= 40)
				{
					L2Skill prevSkill = prevSkillList.get(skillInfo.getSkillId());
					L2Skill newSkill = SkillHolder.getInstance().getInfo(skillInfo.getSkillId(), skillInfo.getSkillLevel());
					
					if ((prevSkill != null) && (prevSkill.getLevel() > newSkill.getLevel()))
					{
						continue;
					}
					
					prevSkillList.put(newSkill.getId(), newSkill);
					storeSkill(newSkill, prevSkill, classIndex);
				}
			}
			return true;
		}
		finally
		{
			_subclassLock.unlock();
		}
	}
	
	public boolean modifySubClass(int classIndex, int newClassId)
	{
		if (!_subclassLock.tryLock())
		{
			return false;
		}
		
		try
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNAS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
				
				statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
				
				statement = con.prepareStatement(DELETE_SKILL_SAVE);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
				
				statement = con.prepareStatement(DELETE_CHAR_SKILLS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
				
				statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not modify sub class for " + getName() + " to class index " + classIndex + ": " + e.getMessage(), e);
				
				getSubClasses().remove(classIndex);
				return false;
			}
			getSubClasses().remove(classIndex);
		}
		finally
		{
			_subclassLock.unlock();
		}
		return addSubClass(newClassId, classIndex);
	}
	
	public boolean isSubClassActive()
	{
		return _classIndex > 0;
	}
	
	public Map<Integer, SubClass> getSubClasses()
	{
		if (_subClasses == null)
		{
			_subClasses = new FastMap<>();
		}
		
		return _subClasses;
	}
	
	public int getTotalSubClasses()
	{
		return getSubClasses().size();
	}
	
	public int getBaseClass()
	{
		return _baseClass;
	}
	
	public int getActiveClass()
	{
		return _activeClass;
	}
	
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	private void setClassTemplate(int classId)
	{
		_activeClass = classId;
		
		final L2PcTemplate pcTemplate = CharTemplateParser.getInstance().getTemplate(classId);
		if (pcTemplate == null)
		{
			_log.severe("Missing template for classId: " + classId);
			throw new Error();
		}
		setTemplate(pcTemplate);
		fireProfessionChangeListeners(pcTemplate);
	}
	
	public boolean setActiveClass(int classIndex)
	{
		if (!_subclassLock.tryLock())
		{
			return false;
		}
		
		try
		{
			if (Interface.isRegistered(getObjectId()))
			{
				return false;
			}
			
			if (_transformation != null)
			{
				return false;
			}
			
			for (L2ItemInstance item : getInventory().getAugmentedItems())
			{
				if ((item != null) && item.isEquipped())
				{
					item.getAugmentation().removeBonus(this);
				}
			}
			
			abortCast();
			
			for (L2Character character : getKnownList().getKnownCharacters())
			{
				if ((character.getFusionSkill() != null) && (character.getFusionSkill().getTarget() == this))
				{
					character.abortCast();
				}
			}
			
			store(Config.SUBCLASS_STORE_SKILL_COOLTIME);
			_reuseTimeStampsSkills.clear();
			_charges.set(0);
			stopChargeTask();
			
			if (hasServitor())
			{
				getSummon().unSummon(this);
			}
			
			if (classIndex == 0)
			{
				setClassTemplate(getBaseClass());
			}
			else
			{
				try
				{
					setClassTemplate(getSubClasses().get(classIndex).getClassId());
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Could not switch " + getName() + "'s sub class to class index " + classIndex + ": " + e.getMessage(), e);
					return false;
				}
			}
			_classIndex = classIndex;
			
			setLearningClass(getClassId());
			
			if (isInParty())
			{
				getParty().recalculatePartyLevel();
			}
			
			for (L2Skill oldSkill : getAllSkills())
			{
				removeSkill(oldSkill, false, true);
			}
			
			stopAllEffectsExceptThoseThatLastThroughDeath();
			stopAllEffectsNotStayOnSubclassChange();
			stopCubics();
			
			restoreRecipeBook(false);
			restoreDeathPenaltyBuffLevel();
			
			restoreSkills();
			rewardSkills();
			regiveTemporarySkills();
			
			if ((_disabledSkills != null) && !_disabledSkills.isEmpty())
			{
				_disabledSkills.clear();
			}
			
			restoreEffects();
			updateEffectIcons();
			sendPacket(new EtcStatusUpdate(this));
			
			QuestState st = getQuestState("_422_RepentYourSins");
			if (st != null)
			{
				st.exitQuest(true);
			}
			
			for (int i = 0; i < 3; i++)
			{
				_henna[i] = null;
			}
			
			restoreHenna();
			sendPacket(new HennaInfo(this));
			
			if (getCurrentHp() > getMaxHp())
			{
				setCurrentHp(getMaxHp());
			}
			if (getCurrentMp() > getMaxMp())
			{
				setCurrentMp(getMaxMp());
			}
			if (getCurrentCp() > getMaxCp())
			{
				setCurrentCp(getMaxCp());
			}
			
			refreshOverloaded();
			refreshExpertisePenalty();
			setExpBeforeDeath(0);
			
			_shortCuts.restoreMe();
			sendPacket(new ShortCutInit(this));
			
			broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));
			sendPacket(new SkillCoolTime(this));
			sendPacket(new ExStorageMaxCount(this));
			
			broadcastUserInfo();
			
			return true;
		}
		finally
		{
			_subclassLock.unlock();
		}
	}
	
	public boolean isLocked()
	{
		return _subclassLock.isLocked();
	}
	
	public void stopWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak != null)
		{
			_taskWarnUserTakeBreak.cancel(true);
			_taskWarnUserTakeBreak = null;
		}
	}
	
	public void startWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak == null)
		{
			_taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreakTask(this), 7200000, 7200000);
		}
	}
	
	public void stopRentPet()
	{
		if (_taskRentPet != null)
		{
			if (checkLandingState() && (getMountType() == MountType.WYVERN))
			{
				teleToLocation(TeleportWhereType.TOWN);
			}
			
			if (dismount())
			{
				_taskRentPet.cancel(true);
				_taskRentPet = null;
			}
		}
	}
	
	public void startRentPet(int seconds)
	{
		if (_taskRentPet == null)
		{
			_taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(this), seconds * 1000L, seconds * 1000L);
		}
	}
	
	public boolean isRentedPet()
	{
		if (_taskRentPet != null)
		{
			return true;
		}
		
		return false;
	}
	
	public void stopWaterTask()
	{
		if (_taskWater != null)
		{
			_taskWater.cancel(false);
			
			_taskWater = null;
			sendPacket(new SetupGauge(2, 0));
		}
	}
	
	public void startWaterTask()
	{
		if (!isDead() && (_taskWater == null))
		{
			int timeinwater = (int) calcStat(Stats.BREATH, 60000, this, null);
			
			sendPacket(new SetupGauge(2, timeinwater));
			_taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(this), timeinwater, 1000);
		}
	}
	
	public boolean isInWater()
	{
		if (_taskWater != null)
		{
			return true;
		}
		
		return false;
	}
	
	public void checkWaterState()
	{
		if (isInsideZone(ZoneId.WATER))
		{
			startWaterTask();
		}
		else
		{
			stopWaterTask();
		}
	}
	
	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();
		
		if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
		{
			if (!isGM() && isIn7sDungeon() && (SevenSigns.getInstance().getPlayerCabal(getObjectId()) != SevenSigns.getInstance().getCabalHighestScore()))
			{
				teleToLocation(TeleportWhereType.TOWN);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
			}
		}
		else
		{
			if (!isGM() && isIn7sDungeon() && (SevenSigns.getInstance().getPlayerCabal(getObjectId()) == SevenSigns.CABAL_NULL))
			{
				teleToLocation(TeleportWhereType.TOWN);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
			}
		}
		
		if (isGM())
		{
			if (isInvul())
			{
				sendMessage("Entering world in Invulnerable mode.");
			}
			if (isInvisible())
			{
				sendMessage("Entering world in Invisible mode.");
			}
			if (isSilenceMode())
			{
				sendMessage("Entering world in Silence mode.");
			}
		}
		
		if (MailBBSManager.getInstance().checkUnreadMail(this) > 0)
		{
			sendPacket(SystemMessageId.NEW_MAIL);
			sendPacket(ExMailArrived.STATIC_PACKET);
		}
		
		if (Config.STORE_SKILL_COOLTIME)
		{
			restoreEffects();
		}
		
		revalidateZone(true);
		
		notifyFriends();
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && Config.DECREASE_SKILL_LEVEL)
		{
			checkPlayerSkills();
		}
		getEvents().onPlayerLogin();
		
		try
		{
			for (L2ZoneType zone : ZoneManager.getInstance().getZones(this))
			{
				zone.onPlayerLoginInside(this);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
	}
	
	public long getLastAccess()
	{
		return _lastAccess;
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		stopEffects(L2EffectType.CHARMOFCOURAGE);
		updateEffectIcons();
		sendPacket(new EtcStatusUpdate(this));
		_reviveRequested = 0;
		_revivePower = 0;
		
		if (isMounted())
		{
			startFeed(_mountNpcId);
		}
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			if (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ()))
			{
				getParty().getDimensionalRift().memberRessurected(this);
			}
		}
		
		if (getInstanceId() > 0)
		{
			final Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
			if (instance != null)
			{
				instance.cancelEjectDeadPlayer(this);
			}
		}
		GlobalRestrictions.playerRevived(this);
	}
	
	@Override
	public void setName(String value)
	{
		super.setName(value);
		if (Config.CACHE_CHAR_NAMES)
		{
			CharNameHolder.getInstance().addName(this);
		}
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		restoreExp(revivePower);
		doRevive();
	}
	
	public void reviveRequest(L2PcInstance reviver, L2Skill skill, boolean Pet)
	{
		if (isResurrectionBlocked())
		{
			return;
		}
		
		if (_reviveRequested == 1)
		{
			if (_revivePet == Pet)
			{
				reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
			}
			else
			{
				if (Pet)
				{
					reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2);
				}
				else
				{
					reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
				}
			}
			return;
		}
		if ((Pet && hasSummon() && getSummon().isDead()) || (!Pet && isDead()))
		{
			_reviveRequested = 1;
			int restoreExp = 0;
			if (isInTownWarEvent())
			{
				_revivePower = 100;
			}
			else if (isPhoenixBlessed())
			{
				_revivePower = 100;
			}
			else if (isAffected(EffectFlag.CHARM_OF_COURAGE))
			{
				_revivePower = 0;
			}
			else
			{
				_revivePower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), reviver);
			}
			
			restoreExp = (int) Math.round(((getExpBeforeDeath() - getExp()) * _revivePower) / 100);
			
			_revivePet = Pet;
			
			if (isAffected(EffectFlag.CHARM_OF_COURAGE))
			{
				ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE.getId());
				dlg.addTime(60000);
				sendPacket(dlg);
				return;
			}
			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_XP.getId());
			dlg.addPcName(reviver);
			dlg.addString(Integer.toString(restoreExp));
			sendPacket(dlg);
		}
	}
	
	public void reviveAnswer(int answer)
	{
		if ((_reviveRequested != 1) || (!isDead() && !_revivePet) || (_revivePet && hasSummon() && !getSummon().isDead()))
		{
			return;
		}
		
		if ((answer == 0) && isPhoenixBlessed())
		{
			stopEffects(L2EffectType.PHOENIX_BLESSING);
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		
		if (answer == 1)
		{
			if (!_revivePet)
			{
				if (_revivePower != 0)
				{
					doRevive(_revivePower);
				}
				else
				{
					doRevive();
				}
			}
			else if (hasSummon())
			{
				if (_revivePower != 0)
				{
					getSummon().doRevive(_revivePower);
				}
				else
				{
					getSummon().doRevive();
				}
			}
		}
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	public boolean isReviveRequested()
	{
		return (_reviveRequested == 1);
	}
	
	public boolean isRevivingPet()
	{
		return _revivePet;
	}
	
	public void removeReviving()
	{
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	public void onActionRequest()
	{
		if (isSpawnProtected())
		{
			sendPacket(SystemMessageId.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS);
			
			if (Config.RESTORE_SERVITOR_ON_RECONNECT && !hasSummon() && CharSummonHolder.getInstance().getServitors().containsKey(getObjectId()))
			{
				CharSummonHolder.getInstance().restoreServitor(this);
			}
			if (Config.RESTORE_PET_ON_RECONNECT && !hasSummon() && CharSummonHolder.getInstance().getPets().containsKey(getObjectId()))
			{
				CharSummonHolder.getInstance().restorePet(this);
			}
		}
		if (isTeleportProtected())
		{
			sendMessage("Teleport spawn protection ended.");
		}
		setProtection(false);
		setTeleportProtection(false);
	}
	
	public int getExpertiseLevel()
	{
		int level = getSkillLevel(239);
		if (level < 0)
		{
			level = 0;
		}
		return level;
	}
	
	@Override
	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		if ((getVehicle() != null) && !getVehicle().isTeleporting())
		{
			setVehicle(null);
		}
		
		if (isFlyingMounted() && (z < -1005))
		{
			z = -1005;
		}
		
		super.teleToLocation(x, y, z, heading, allowRandomOffset);
	}
	
	@Override
	public final void onTeleported()
	{
		super.onTeleported();
		
		if (isInAirShip())
		{
			getAirShip().sendInfo(this);
		}
		
		revalidateZone(true);
		
		checkItemRestriction();
		
		if ((Config.PLAYER_TELEPORT_PROTECTION > 0) && !isInOlympiadMode())
		{
			setTeleportProtection(true);
		}
		
		if (getTrainedBeasts() != null)
		{
			for (L2TamedBeastInstance tamedBeast : getTrainedBeasts())
			{
				tamedBeast.deleteMe();
			}
			getTrainedBeasts().clear();
		}
		
		if (hasSummon())
		{
			getSummon().setFollowStatus(false);
			getSummon().teleToLocation(getPosition().getX(), getPosition().getY(), getPosition().getZ(), false);
			((L2SummonAI) getSummon().getAI()).setStartFollowController(true);
			getSummon().setFollowStatus(true);
			getSummon().updateAndBroadcastStatus(0);
		}
		TvTEvent.onTeleported(this);
		TvTRoundEvent.onTeleported(this);
	}
	
	@Override
	public void setIsTeleporting(boolean teleport)
	{
		setIsTeleporting(teleport, true);
	}
	
	public void setIsTeleporting(boolean teleport, boolean useWatchDog)
	{
		super.setIsTeleporting(teleport);
		if (!useWatchDog)
		{
			return;
		}
		if (teleport)
		{
			if ((_teleportWatchdog == null) && (Config.TELEPORT_WATCHDOG_TIMEOUT > 0))
			{
				synchronized (this)
				{
					if (_teleportWatchdog == null)
					{
						_teleportWatchdog = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportWatchdogTask(this), Config.TELEPORT_WATCHDOG_TIMEOUT * 1000);
					}
				}
			}
		}
		else
		{
			if (_teleportWatchdog != null)
			{
				_teleportWatchdog.cancel(false);
				_teleportWatchdog = null;
			}
		}
	}
	
	public void setLastServerPosition(int x, int y, int z)
	{
		_lastServerPosition.setXYZ(x, y, z);
	}
	
	public Point3D getLastServerPosition()
	{
		return _lastServerPosition;
	}
	
	public boolean checkLastServerPosition(int x, int y, int z)
	{
		return _lastServerPosition.equals(x, y, z);
	}
	
	public int getLastServerDistance(int x, int y, int z)
	{
		double dx = (x - _lastServerPosition.getX());
		double dy = (y - _lastServerPosition.getY());
		double dz = (z - _lastServerPosition.getZ());
		
		return (int) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		if (getExpOn())
		{
			getStat().addExpAndSp(addToExp, addToSp, false);
		}
		else
		{
			getStat().addExpAndSp(0, addToSp, false);
		}
	}
	
	public void addExpAndSp(long addToExp, int addToSp, boolean useVitality)
	{
		if (getExpOn())
		{
			getStat().addExpAndSp(addToExp, addToSp, useVitality);
		}
		else
		{
			getStat().addExpAndSp(0, addToSp, useVitality);
		}
	}
	
	public void removeExpAndSp(long removeExp, int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp, true);
	}
	
	public void removeExpAndSp(long removeExp, int removeSp, boolean sendMessage)
	{
		getStat().removeExpAndSp(removeExp, removeSp, sendMessage);
	}
	
	@Override
	public void reduceCurrentHp(double value, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (skill != null)
		{
			getStatus().reduceHp(value, attacker, awake, isDOT, skill.isToggle(), skill.getDmgDirectlyToHP());
		}
		else
		{
			getStatus().reduceHp(value, attacker, awake, isDOT, false, false);
		}
		
		if (getTrainedBeasts() != null)
		{
			for (L2TamedBeastInstance tamedBeast : getTrainedBeasts())
			{
				tamedBeast.onOwnerGotAttacked(attacker);
			}
		}
	}
	
	public void broadcastSnoop(int type, String name, String _text)
	{
		if (!_snoopListener.isEmpty())
		{
			Snoop sn = new Snoop(getObjectId(), getName(), type, name, _text);
			
			for (L2PcInstance pci : _snoopListener)
			{
				if (pci != null)
				{
					pci.sendPacket(sn);
				}
			}
		}
	}
	
	public void addSnooper(L2PcInstance pci)
	{
		if (!_snoopListener.contains(pci))
		{
			_snoopListener.add(pci);
		}
	}
	
	public void removeSnooper(L2PcInstance pci)
	{
		_snoopListener.remove(pci);
	}
	
	public void addSnooped(L2PcInstance pci)
	{
		if (!_snoopedPlayer.contains(pci))
		{
			_snoopedPlayer.add(pci);
		}
	}
	
	public void removeSnooped(L2PcInstance pci)
	{
		_snoopedPlayer.remove(pci);
	}
	
	public void addBypass(String bypass)
	{
		if (bypass == null)
		{
			return;
		}
		
		_validBypass.add(bypass);
	}
	
	public void addBypass2(String bypass)
	{
		if (bypass == null)
		{
			return;
		}
		
		_validBypass2.add(bypass);
	}
	
	public boolean validateBypass(String cmd)
	{
		if (!Config.BYPASS_VALIDATION)
		{
			return true;
		}
		
		for (String bp : _validBypass)
		{
			if (bp == null)
			{
				continue;
			}
			
			if (bp.equals(cmd))
			{
				return true;
			}
		}
		
		for (String bp : _validBypass2)
		{
			if (bp == null)
			{
				continue;
			}
			
			if (cmd.startsWith(bp))
			{
				return true;
			}
		}
		
		_log.warning("[L2PcInstance] player [" + getName() + "] sent invalid bypass '" + cmd + "'.");
		return false;
	}
	
	public boolean validateItemManipulation(int objectId, String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if ((item == null) || (item.getOwnerId() != getObjectId()))
		{
			_log.finest(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}
		
		if ((hasSummon() && (getSummon().getControlObjectId() == objectId)) || (getMountObjectID() == objectId))
		{
			if (Config.DEBUG)
			{
				_log.finest(getObjectId() + ": player tried to " + action + " item controling pet");
			}
			
			return false;
		}
		
		if (getActiveEnchantItemId() == objectId)
		{
			if (Config.DEBUG)
			{
				_log.finest(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(item.getId()))
		{
			return false;
		}
		
		return true;
	}
	
	public void clearBypass()
	{
		_validBypass.clear();
		_validBypass2.clear();
	}
	
	public boolean isInBoat()
	{
		return (_vehicle != null) && _vehicle.isBoat();
	}
	
	public L2BoatInstance getBoat()
	{
		return (L2BoatInstance) _vehicle;
	}
	
	public boolean isInAirShip()
	{
		return (_vehicle != null) && _vehicle.isAirShip();
	}
	
	public L2AirShipInstance getAirShip()
	{
		return (L2AirShipInstance) _vehicle;
	}
	
	public L2Vehicle getVehicle()
	{
		return _vehicle;
	}
	
	public void setVehicle(L2Vehicle v)
	{
		if ((v == null) && (_vehicle != null))
		{
			_vehicle.removePassenger(this);
		}
		
		_vehicle = v;
	}
	
	public boolean isInVehicle()
	{
		return _vehicle != null;
	}
	
	public void setInCrystallize(boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}
	
	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}
	
	public Point3D getInVehiclePosition()
	{
		return _inVehiclePosition;
	}
	
	public void setInVehiclePosition(Point3D pt)
	{
		_inVehiclePosition = pt;
	}
	
	public void setIsInKrateisCube(boolean choice)
	{
		_isInKrateisCube = choice;
	}
	
	public boolean getIsInKrateisCube()
	{
		return _isInKrateisCube;
	}
	
	@Override
	public void deleteMe()
	{
		cleanup();
		store();
		super.deleteMe();
	}
	
	private synchronized void cleanup()
	{
		getEvents().onPlayerLogout();
		
		try
		{
			for (L2ZoneType zone : ZoneManager.getInstance().getZones(this))
			{
				zone.onPlayerLogoutInside(this);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			if (!isOnline())
			{
				_log.log(Level.SEVERE, "deleteMe() called on offline character " + this, new RuntimeException());
			}
			setOnlineStatus(false, true);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			if (Config.ENABLE_BLOCK_CHECKER_EVENT && (getBlockCheckerArena() != -1))
			{
				HandysBlockCheckerManager.getInstance().onDisconnect(this);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			_isOnline = false;
			abortAttack();
			abortCast();
			stopMove(null);
			setDebug(null);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			if (getInventory().getItemByItemId(9819) != null)
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(this, fort.getId());
				}
				else
				{
					int slot = getInventory().getSlotFromItem(getInventory().getItemByItemId(9819));
					getInventory().unEquipItemInBodySlot(slot);
					destroyItem("CombatFlag", getInventory().getItemByItemId(9819), null, true);
				}
			}
			else if (isCombatFlagEquipped())
			{
				TerritoryWarManager.getInstance().dropCombatFlag(this, false, false);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			PartyMatchWaitingList.getInstance().removePlayer(this);
			if (_partyroom != 0)
			{
				PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
				if (room != null)
				{
					room.deleteMember(this);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			if (isFlying())
			{
				removeSkill(SkillHolder.getInstance().getInfo(4289, 1));
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			storeRecommendations();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			stopAllTimers();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			setIsTeleporting(false);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			RecipeController.getInstance().requestMakeItemAbort(this);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			setTarget(null);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			if (_fusionSkill != null)
			{
				abortCast();
			}
			
			for (L2Character character : getKnownList().getKnownCharacters())
			{
				if ((character.getFusionSkill() != null) && (character.getFusionSkill().getTarget() == this))
				{
					character.abortCast();
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			for (L2Effect effect : getAllEffects())
			{
				if (effect.getSkill().isToggle())
				{
					effect.exit();
					continue;
				}
				
				switch (effect.getEffectType())
				{
					case SIGNET_GROUND:
					case SIGNET_EFFECT:
						effect.exit();
						break;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		final L2WorldRegion oldRegion = getWorldRegion();
		
		if (oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}
		
		try
		{
			decayMe();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		if (isInParty())
		{
			try
			{
				leaveParty();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "deleteMe()", e);
			}
		}
		
		if (OlympiadManager.getInstance().isRegistered(this) || (getOlympiadGameId() != -1))
		{
			OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
		}
		
		if (hasSummon())
		{
			try
			{
				getSummon().setRestoreSummon(true);
				getSummon().unSummon(this);
				
				if (hasSummon())
				{
					getSummon().broadcastNpcInfo(0);
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "deleteMe()", e);
			}
		}
		
		if (getClan() != null)
		{
			try
			{
				L2ClanMember clanMember = getClan().getClanMember(getObjectId());
				if (clanMember != null)
				{
					clanMember.setPlayerInstance(null);
				}
				
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "deleteMe()", e);
			}
		}
		
		if (getActiveRequester() != null)
		{
			setActiveRequester(null);
			cancelActiveTrade();
		}
		
		if (isGM())
		{
			try
			{
				AdminParser.getInstance().deleteGm(this);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "deleteMe()", e);
			}
		}
		
		try
		{
			if (inObserverMode())
			{
				setXYZInvisible(_lastX, _lastY, _lastZ);
			}
			
			if (getVehicle() != null)
			{
				getVehicle().oustPlayer(this);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			final int instanceId = getInstanceId();
			if ((instanceId != 0) && !Config.RESTORE_PLAYER_INSTANCE)
			{
				final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
				if (inst != null)
				{
					inst.removePlayer(getObjectId());
					final Location loc = inst.getSpawnLoc();
					if (loc != null)
					{
						final int x = loc.getX() + Rnd.get(-30, 30);
						final int y = loc.getY() + Rnd.get(-30, 30);
						setXYZInvisible(x, y, loc.getZ());
						if (hasSummon())
						{
							getSummon().teleToLocation(loc, true);
							getSummon().setInstanceId(0);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			TvTEvent.onLogout(this);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			TvTRoundEvent.onLogout(this);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			getInventory().deleteMe();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			clearWarehouse();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		if (Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().remCacheTask(this);
		}
		
		try
		{
			getFreight().deleteMe();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			clearRefund();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		if (isCursedWeaponEquipped())
		{
			try
			{
				CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).setPlayer(null);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "deleteMe()", e);
			}
		}
		
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		
		try
		{
			Interface.onLogout(getObjectId());
			if (Interface.isParticipating(getObjectId()))
			{
				Interface.eventOnLogout(getObjectId());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (getClanId() > 0)
		{
			getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
		}
		
		for (L2PcInstance player : _snoopedPlayer)
		{
			player.removeSnooper(this);
		}
		
		for (L2PcInstance player : _snoopListener)
		{
			player.removeSnooped(this);
		}
		
		L2World.getInstance().removeObject(this);
		L2World.getInstance().removeFromAllPlayers(this);
		
		try
		{
			RegionBBSManager.getInstance().changeCommunityBoard(this);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on deleteMe() changeCommunityBoard: " + e.getMessage(), e);
		}
		
		try
		{
			notifyFriends();
			getBlockList().playerLogout();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on deleteMe() notifyFriends: " + e.getMessage(), e);
		}
	}
	
	private L2Fish _fish;
	
	public void startFishing(int _x, int _y, int _z, boolean isHotSpring)
	{
		stopMove(null);
		setIsImmobilized(true);
		_fishing = true;
		_fishx = _x;
		_fishy = _y;
		_fishz = _z;
		
		int lvl = GetRandomFishLvl();
		int group = GetRandomGroup();
		int type = GetRandomFishType(group);
		List<L2Fish> fishs = FishParser.getInstance().getFish(lvl, type, group);
		
		if ((fishs == null) || fishs.isEmpty())
		{
			sendMessage("Error - Fishes are not definied");
			endFishing(false);
			return;
		}
		int check = Rnd.get(fishs.size());
		
		_fish = fishs.get(check).clone();
		
		if (isHotSpring && (_lure.getId() == 8548) && (getSkillLevel(1315) > 19) && Rnd.nextBoolean())
		{
			_fish = new L2Fish(271, 8547, "Old Box", 10, 20, 100, 618, 1185, 0, 10, 40, 20, 30, 3, 618, 0, 1);
		}
		
		fishs.clear();
		fishs = null;
		sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		if (!GameTimeController.getInstance().isNight() && _lure.isNightLure())
		{
			_fish.setFishGroup(-1);
		}
		
		broadcastPacket(new ExFishingStart(this, _fish.getFishGroup(), _x, _y, _z, _lure.isNightLure()));
		sendPacket(new PlaySound(1, "SF_P_01", 0, 0, 0, 0, 0));
		startLookingForFishTask();
	}
	
	public void stopLookingForFishTask()
	{
		if (_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}
	
	public void startLookingForFishTask()
	{
		if (!isDead() && (_taskforfish == null))
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;
			
			if (_lure != null)
			{
				int lureid = _lure.getId();
				isNoob = _fish.getFishGrade() == 0;
				isUpperGrade = _fish.getFishGrade() == 2;
				if ((lureid == 6519) || (lureid == 6522) || (lureid == 6525) || (lureid == 8505) || (lureid == 8508) || (lureid == 8511))
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.33)));
				}
				else if ((lureid == 6520) || (lureid == 6523) || (lureid == 6526) || ((lureid >= 8505) && (lureid <= 8513)) || ((lureid >= 7610) && (lureid <= 7613)) || ((lureid >= 7807) && (lureid <= 7809)) || ((lureid >= 8484) && (lureid <= 8486)))
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.00)));
				}
				else if ((lureid == 6521) || (lureid == 6524) || (lureid == 6527) || (lureid == 8507) || (lureid == 8510) || (lureid == 8513) || (lureid == 8548))
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (0.66)));
				}
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(this, _fish.getStartCombatTime(), _fish.getFishGuts(), _fish.getFishGroup(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}
	
	private int GetRandomGroup()
	{
		switch (_lure.getId())
		{
			case 7807:
			case 7808:
			case 7809:
			case 8486:
				return 0;
			case 8485:
			case 8506:
			case 8509:
			case 8512:
				return 2;
			default:
				return 1;
		}
	}
	
	private int GetRandomFishType(int group)
	{
		int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0:
				switch (_lure.getId())
				{
					case 7807:
						if (check <= 54)
						{
							type = 5;
						}
						else if (check <= 77)
						{
							type = 4;
						}
						else
						{
							type = 6;
						}
						break;
					case 7808:
						if (check <= 54)
						{
							type = 4;
						}
						else if (check <= 77)
						{
							type = 6;
						}
						else
						{
							type = 5;
						}
						break;
					case 7809:
						if (check <= 54)
						{
							type = 6;
						}
						else if (check <= 77)
						{
							type = 5;
						}
						else
						{
							type = 4;
						}
						break;
					case 8486:
						if (check <= 33)
						{
							type = 4;
						}
						else if (check <= 66)
						{
							type = 5;
						}
						else
						{
							type = 6;
						}
						break;
				}
				break;
			case 1:
				switch (_lure.getId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					case 6519:
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54)
						{
							type = 1;
						}
						else if (check <= 74)
						{
							type = 0;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6522:
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54)
						{
							type = 0;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6525:
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55)
						{
							type = 2;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 0;
						}
						else
						{
							type = 3;
						}
						break;
					case 8484:
						if (check <= 33)
						{
							type = 0;
						}
						else if (check <= 66)
						{
							type = 1;
						}
						else
						{
							type = 2;
						}
						break;
				}
				break;
			case 2:
				switch (_lure.getId())
				{
					case 8506:
						if (check <= 54)
						{
							type = 8;
						}
						else if (check <= 77)
						{
							type = 7;
						}
						else
						{
							type = 9;
						}
						break;
					case 8509:
						if (check <= 54)
						{
							type = 7;
						}
						else if (check <= 77)
						{
							type = 9;
						}
						else
						{
							type = 8;
						}
						break;
					case 8512:
						if (check <= 54)
						{
							type = 9;
						}
						else if (check <= 77)
						{
							type = 8;
						}
						else
						{
							type = 7;
						}
						break;
					case 8485:
						if (check <= 33)
						{
							type = 7;
						}
						else if (check <= 66)
						{
							type = 8;
						}
						else
						{
							type = 9;
						}
						break;
				}
		}
		return type;
	}
	
	private int GetRandomFishLvl()
	{
		int skilllvl = getSkillLevel(1315);
		final L2Effect e = getFirstEffect(2274);
		if (e != null)
		{
			skilllvl = (int) e.getSkill().getPower();
		}
		if (skilllvl <= 0)
		{
			return 1;
		}
		int randomlvl;
		int check = Rnd.get(100);
		
		if (check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if (check <= 85)
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
			if (randomlvl > 27)
			{
				randomlvl = 27;
			}
		}
		return randomlvl;
	}
	
	public void startFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
	}
	
	public void endFishing(boolean win)
	{
		_fishing = false;
		_fishx = 0;
		_fishy = 0;
		_fishz = 0;
		if (_fishCombat == null)
		{
			sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		}
		_fishCombat = null;
		_lure = null;
		broadcastPacket(new ExFishingEnd(win, this));
		sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		setIsImmobilized(false);
		stopLookingForFishTask();
	}
	
	public L2Fishing getFishCombat()
	{
		return _fishCombat;
	}
	
	public int getFishx()
	{
		return _fishx;
	}
	
	public int getFishy()
	{
		return _fishy;
	}
	
	public int getFishz()
	{
		return _fishz;
	}
	
	public void setLure(L2ItemInstance lure)
	{
		_lure = lure;
	}
	
	public L2ItemInstance getLure()
	{
		return _lure;
	}
	
	public int getInventoryLimit()
	{
		int ivlim;
		if (isGM())
		{
			ivlim = Config.INVENTORY_MAXIMUM_GM;
		}
		else if (getRace() == Race.Dwarf)
		{
			ivlim = Config.INVENTORY_MAXIMUM_DWARF;
		}
		else
		{
			ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
		}
		ivlim += (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);
		
		return ivlim;
	}
	
	public int getWareHouseLimit()
	{
		int whlim;
		if (getRace() == Race.Dwarf)
		{
			whlim = Config.WAREHOUSE_SLOTS_DWARF;
		}
		else
		{
			whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
		}
		
		whlim += (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);
		
		return whlim;
	}
	
	public int getPrivateSellStoreLimit()
	{
		int pslim;
		
		if (getRace() == Race.Dwarf)
		{
			pslim = Config.MAX_PVTSTORESELL_SLOTS_DWARF;
		}
		else
		{
			pslim = Config.MAX_PVTSTORESELL_SLOTS_OTHER;
		}
		
		pslim += (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);
		
		return pslim;
	}
	
	public int getPrivateBuyStoreLimit()
	{
		int pblim;
		
		if (getRace() == Race.Dwarf)
		{
			pblim = Config.MAX_PVTSTOREBUY_SLOTS_DWARF;
		}
		else
		{
			pblim = Config.MAX_PVTSTOREBUY_SLOTS_OTHER;
		}
		pblim += (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);
		
		return pblim;
	}
	
	public int getDwarfRecipeLimit()
	{
		int recdlim = Config.DWARF_RECIPE_LIMIT;
		recdlim += (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
		return recdlim;
	}
	
	public int getCommonRecipeLimit()
	{
		int recclim = Config.COMMON_RECIPE_LIMIT;
		recclim += (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
		return recclim;
	}
	
	public int getMountNpcId()
	{
		return _mountNpcId;
	}
	
	public int getMountLevel()
	{
		return _mountLevel;
	}
	
	public void setMountObjectID(int newID)
	{
		_mountObjectID = newID;
	}
	
	public int getMountObjectID()
	{
		return _mountObjectID;
	}
	
	private L2ItemInstance _lure = null;
	private int _shortBuffTaskSkillId = 0;
	
	public SkillUseHolder getCurrentSkill()
	{
		return _currentSkill;
	}
	
	public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			_currentSkill = null;
			return;
		}
		_currentSkill = new SkillUseHolder(currentSkill, ctrlPressed, shiftPressed);
	}
	
	public SkillUseHolder getCurrentPetSkill()
	{
		return _currentPetSkill;
	}
	
	public void setCurrentPetSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			_currentPetSkill = null;
			return;
		}
		_currentPetSkill = new SkillUseHolder(currentSkill, ctrlPressed, shiftPressed);
	}
	
	public SkillUseHolder getQueuedSkill()
	{
		return _queuedSkill;
	}
	
	public void setQueuedSkill(L2Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (queuedSkill == null)
		{
			_queuedSkill = null;
			return;
		}
		_queuedSkill = new SkillUseHolder(queuedSkill, ctrlPressed, shiftPressed);
	}
	
	public boolean isJailed()
	{
		return PunishmentManager.getInstance().hasPunishment(getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.JAIL) || PunishmentManager.getInstance().hasPunishment(getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.JAIL) || PunishmentManager.getInstance().hasPunishment(getIPAddress(), PunishmentAffect.IP, PunishmentType.JAIL);
	}
	
	public boolean isChatBanned()
	{
		return PunishmentManager.getInstance().hasPunishment(getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN) || PunishmentManager.getInstance().hasPunishment(getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.CHAT_BAN) || PunishmentManager.getInstance().hasPunishment(getIPAddress(), PunishmentAffect.IP, PunishmentType.CHAT_BAN);
	}
	
	public void startFameTask(long delay, int fameFixRate)
	{
		if ((getLevel() < 40) || (getClassId().level() < 2))
		{
			return;
		}
		if (_fameTask == null)
		{
			_fameTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FameTask(this, fameFixRate), delay, delay);
		}
	}
	
	public void stopFameTask()
	{
		if (_fameTask != null)
		{
			_fameTask.cancel(false);
			_fameTask = null;
		}
	}
	
	public void startVitalityTask()
	{
		if (Config.ENABLE_VITALITY && (_vitalityTask == null))
		{
			_vitalityTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new VitalityTask(this), 1000, 60000);
		}
	}
	
	public void stopVitalityTask()
	{
		if (_vitalityTask != null)
		{
			_vitalityTask.cancel(false);
			_vitalityTask = null;
		}
	}
	
	public int getPowerGrade()
	{
		return _powerGrade;
	}
	
	public void setPowerGrade(int power)
	{
		_powerGrade = power;
	}
	
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}
	
	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;
	}
	
	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}
	
	public boolean isCombatFlagEquipped()
	{
		return _combatFlagEquippedId;
	}
	
	public void setCombatFlagEquipped(boolean value)
	{
		_combatFlagEquippedId = value;
	}
	
	public int getChargedSouls()
	{
		return _souls;
	}
	
	public void increaseSouls(int count)
	{
		_souls += count;
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
		sm.addNumber(count);
		sm.addNumber(_souls);
		sendPacket(sm);
		restartSoulTask();
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public boolean decreaseSouls(int count, L2Skill skill)
	{
		_souls -= count;
		
		if (getChargedSouls() < 0)
		{
			_souls = 0;
		}
		
		if (getChargedSouls() == 0)
		{
			stopSoulTask();
		}
		else
		{
			restartSoulTask();
		}
		sendPacket(new EtcStatusUpdate(this));
		return true;
	}
	
	public void clearSouls()
	{
		_souls = 0;
		stopSoulTask();
		sendPacket(new EtcStatusUpdate(this));
	}
	
	private void restartSoulTask()
	{
		if (_soulTask != null)
		{
			_soulTask.cancel(false);
			_soulTask = null;
		}
		_soulTask = ThreadPoolManager.getInstance().scheduleGeneral(new ResetSoulsTask(this), 600000);
	}
	
	public void stopSoulTask()
	{
		if (_soulTask != null)
		{
			_soulTask.cancel(false);
			_soulTask = null;
		}
	}
	
	public void shortBuffStatusUpdate(int magicId, int level, int time)
	{
		if (_shortBuffTask != null)
		{
			_shortBuffTask.cancel(false);
			_shortBuffTask = null;
		}
		_shortBuffTask = ThreadPoolManager.getInstance().scheduleGeneral(new ShortBuffTask(this), time * 1000);
		setShortBuffTaskSkillId(magicId);
		sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
	}
	
	public int getShortBuffTaskSkillId()
	{
		return _shortBuffTaskSkillId;
	}
	
	public void setShortBuffTaskSkillId(int id)
	{
		_shortBuffTaskSkillId = id;
	}
	
	public int getDeathPenaltyBuffLevel()
	{
		return _deathPenaltyBuffLevel;
	}
	
	public void setDeathPenaltyBuffLevel(int level)
	{
		_deathPenaltyBuffLevel = level;
	}
	
	public void calculateDeathPenaltyBuffLevel(L2Character killer)
	{
		if (((getKarma() > 0) || (Rnd.get(1, 100) <= Config.DEATH_PENALTY_CHANCE)) && !(killer instanceof L2PcInstance) && !(canOverrideCond(PcCondOverride.DEATH_PENALTY)) && !(isCharmOfLuckAffected() && killer.isRaid()) && !isPhoenixBlessed() && !isLucky() && !isBlockedFromDeathPenalty() && !(isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.SIEGE)))
		{
			increaseDeathPenaltyBuffLevel();
		}
	}
	
	public void increaseDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() >= 15)
		{
			return;
		}
		
		if (getDeathPenaltyBuffLevel() != 0)
		{
			L2Skill skill = SkillHolder.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
			
			if (skill != null)
			{
				removeSkill(skill, true);
			}
		}
		
		_deathPenaltyBuffLevel++;
		
		addSkill(SkillHolder.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		sendPacket(new EtcStatusUpdate(this));
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
		sm.addNumber(getDeathPenaltyBuffLevel());
		sendPacket(sm);
	}
	
	public void reduceDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() <= 0)
		{
			return;
		}
		
		L2Skill skill = SkillHolder.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null)
		{
			removeSkill(skill, true);
		}
		
		_deathPenaltyBuffLevel--;
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillHolder.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			sendPacket(new EtcStatusUpdate(this));
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
		}
		else
		{
			sendPacket(new EtcStatusUpdate(this));
			sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
		}
	}
	
	public void restoreDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillHolder.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		}
	}
	
	private final Map<Integer, TimeStamp> _reuseTimeStampsItems = new FastMap<>();
	
	@Override
	public void addTimeStampItem(L2ItemInstance item, long reuse)
	{
		_reuseTimeStampsItems.put(item.getObjectId(), new TimeStamp(item, reuse));
	}
	
	public void addTimeStampItem(L2ItemInstance item, long reuse, long systime)
	{
		_reuseTimeStampsItems.put(item.getObjectId(), new TimeStamp(item, reuse, systime));
	}
	
	@Override
	public long getItemRemainingReuseTime(int itemObjId)
	{
		if (!_reuseTimeStampsItems.containsKey(itemObjId))
		{
			return -1;
		}
		return _reuseTimeStampsItems.get(itemObjId).getRemaining();
	}
	
	public long getReuseDelayOnGroup(int group)
	{
		if (group > 0)
		{
			for (TimeStamp ts : _reuseTimeStampsItems.values())
			{
				if ((ts.getSharedReuseGroup() == group) && ts.hasNotPassed())
				{
					return ts.getRemaining();
				}
			}
		}
		return 0;
	}
	
	private final Map<Integer, TimeStamp> _reuseTimeStampsSkills = new FastMap<>();
	
	public Map<Integer, TimeStamp> getSkillReuseTimeStamps()
	{
		return _reuseTimeStampsSkills;
	}
	
	@Override
	public long getSkillRemainingReuseTime(int skillReuseHashId)
	{
		if (!_reuseTimeStampsSkills.containsKey(skillReuseHashId))
		{
			return -1;
		}
		return _reuseTimeStampsSkills.get(skillReuseHashId).getRemaining();
	}
	
	public boolean hasSkillReuse(int skillReuseHashId)
	{
		if (!_reuseTimeStampsSkills.containsKey(skillReuseHashId))
		{
			return false;
		}
		return _reuseTimeStampsSkills.get(skillReuseHashId).hasNotPassed();
	}
	
	public TimeStamp getSkillReuseTimeStamp(int skillReuseHashId)
	{
		return _reuseTimeStampsSkills.get(skillReuseHashId);
	}
	
	@Override
	public void addTimeStamp(L2Skill skill, long reuse)
	{
		_reuseTimeStampsSkills.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse));
	}
	
	public void addTimeStamp(L2Skill skill, long reuse, long systime)
	{
		_reuseTimeStampsSkills.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse, systime));
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return this;
	}
	
	@Override
	public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss)
		{
			if (target.isPlayer())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_EVADED_C2_ATTACK);
				sm.addPcName(target.getActingPlayer());
				sm.addCharName(this);
				target.sendPacket(sm);
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ATTACK_WENT_ASTRAY);
			sm.addPcName(this);
			sendPacket(sm);
			return;
		}
		
		if (pcrit)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAD_CRITICAL_HIT);
			sm.addPcName(this);
			sendPacket(sm);
		}
		if (mcrit)
		{
			sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
		}
		
		if (isInOlympiadMode() && target.isPlayer() && target.getActingPlayer().isInOlympiadMode() && (target.getActingPlayer().getOlympiadGameId() == getOlympiadGameId()))
		{
			OlympiadGameManager.getInstance().notifyCompetitorDamage(this, damage);
		}
		
		final SystemMessage sm;
		
		if (target.isInvul() && !target.isNpc())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
		}
		else if (target.isDoor() || (target instanceof L2ControlTowerInstance))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG);
			sm.addNumber(damage);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DONE_S3_DAMAGE_TO_C2);
			sm.addPcName(this);
			sm.addCharName(target);
			sm.addNumber(damage);
		}
		sendPacket(sm);
	}
	
	public void setAgathionId(int npcId)
	{
		_agathionId = npcId;
	}
	
	public int getAgathionId()
	{
		return _agathionId;
	}
	
	public int getVitalityPoints()
	{
		return getStat().getVitalityPoints();
	}
	
	public int getVitalityLevel()
	{
		return getStat().getVitalityLevel();
	}
	
	public void setVitalityPoints(int points, boolean quiet)
	{
		getStat().setVitalityPoints(points, quiet);
	}
	
	public void updateVitalityPoints(float points, boolean useRates, boolean quiet)
	{
		getStat().updateVitalityPoints(points, useRates, quiet);
	}
	
	public void checkItemRestriction()
	{
		for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
		{
			L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);
			if ((equippedItem != null) && !equippedItem.getItem().checkCondition(this, this, false))
			{
				getInventory().unEquipItemInSlot(i);
				
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(equippedItem);
				sendPacket(iu);
				
				SystemMessage sm = null;
				if (equippedItem.getItem().getBodyPart() == L2Item.SLOT_BACK)
				{
					sendPacket(SystemMessageId.CLOAK_REMOVED_BECAUSE_ARMOR_SET_REMOVED);
					return;
				}
				
				if (equippedItem.getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(equippedItem.getEnchantLevel());
					sm.addItemName(equippedItem);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(equippedItem);
				}
				sendPacket(sm);
			}
		}
	}
	
	public void addTransformSkill(int id)
	{
		if (_transformAllowedSkills == null)
		{
			synchronized (this)
			{
				if (_transformAllowedSkills == null)
				{
					_transformAllowedSkills = new HashSet<>();
				}
			}
		}
		_transformAllowedSkills.add(id);
	}
	
	public boolean hasTransformSkill(int id)
	{
		return (_transformAllowedSkills != null) && _transformAllowedSkills.contains(id);
	}
	
	public synchronized void removeAllTransformSkills()
	{
		_transformAllowedSkills = null;
	}
	
	protected void startFeed(int npcId)
	{
		_canFeed = npcId > 0;
		if (!isMounted())
		{
			return;
		}
		if (hasSummon())
		{
			setCurrentFeed(((L2PetInstance) getSummon()).getCurrentFed());
			_controlItemId = getSummon().getControlObjectId();
			sendPacket(new SetupGauge(3, (getCurrentFeed() * 10000) / getFeedConsume(), (getMaxFeed() * 10000) / getFeedConsume()));
			if (!isDead())
			{
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PetFeedTask(this), 10000, 10000);
			}
		}
		else if (_canFeed)
		{
			setCurrentFeed(getMaxFeed());
			SetupGauge sg = new SetupGauge(3, (getCurrentFeed() * 10000) / getFeedConsume(), (getMaxFeed() * 10000) / getFeedConsume());
			sendPacket(sg);
			if (!isDead())
			{
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PetFeedTask(this), 10000, 10000);
			}
		}
	}
	
	public void stopFeed()
	{
		if (_mountFeedTask != null)
		{
			_mountFeedTask.cancel(false);
			_mountFeedTask = null;
		}
	}
	
	private final void clearPetData()
	{
		_data = null;
	}
	
	public final L2PetData getPetData(int npcId)
	{
		if (_data == null)
		{
			_data = PetsParser.getInstance().getPetData(npcId);
		}
		return _data;
	}
	
	private final L2PetLevelData getPetLevelData(int npcId)
	{
		if (_leveldata == null)
		{
			_leveldata = PetsParser.getInstance().getPetData(npcId).getPetLevelData(getMountLevel());
		}
		return _leveldata;
	}
	
	public int getCurrentFeed()
	{
		return _curFeed;
	}
	
	public int getFeedConsume()
	{
		if (isAttackingNow())
		{
			return getPetLevelData(_mountNpcId).getPetFeedBattle();
		}
		return getPetLevelData(_mountNpcId).getPetFeedNormal();
	}
	
	public void setCurrentFeed(int num)
	{
		boolean lastHungryState = isHungry();
		_curFeed = num > getMaxFeed() ? getMaxFeed() : num;
		SetupGauge sg = new SetupGauge(3, (getCurrentFeed() * 10000) / getFeedConsume(), (getMaxFeed() * 10000) / getFeedConsume());
		sendPacket(sg);
		if (lastHungryState != isHungry())
		{
			broadcastUserInfo();
		}
	}
	
	private int getMaxFeed()
	{
		return getPetLevelData(_mountNpcId).getPetMaxFeed();
	}
	
	public boolean isHungry()
	{
		return _canFeed ? (getCurrentFeed() < ((getPetData(getMountNpcId()).getHungryLimit() / 100f) * getPetLevelData(getMountNpcId()).getPetMaxFeed())) : false;
	}
	
	public void enteredNoLanding(int delay)
	{
		_dismountTask = ThreadPoolManager.getInstance().scheduleGeneral(new DismountTask(this), delay * 1000);
	}
	
	public void exitedNoLanding()
	{
		if (_dismountTask != null)
		{
			_dismountTask.cancel(true);
			_dismountTask = null;
		}
	}
	
	public void storePetFood(int petId)
	{
		if ((_controlItemId != 0) && (petId != 0))
		{
			String req;
			req = "UPDATE pets SET fed=? WHERE item_obj_id = ?";
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(req))
			{
				statement.setInt(1, getCurrentFeed());
				statement.setInt(2, _controlItemId);
				statement.executeUpdate();
				_controlItemId = 0;
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Failed to store Pet [NpcId: " + petId + "] data", e);
			}
		}
	}
	
	public int getEventEffectId()
	{
		return _eventEffectId;
	}
	
	public void startEventEffect(AbnormalEffect mask)
	{
		_eventEffectId |= mask.getMask();
		broadcastUserInfo();
	}
	
	public void stopEventEffect(AbnormalEffect mask)
	{
		_eventEffectId &= ~mask.getMask();
		broadcastUserInfo();
	}
	
	public void setIsInSiege(boolean b)
	{
		_isInSiege = b;
	}
	
	public boolean isInSiege()
	{
		return _isInSiege;
	}
	
	public void setIsInHideoutSiege(boolean isInHideoutSiege)
	{
		_isInHideoutSiege = isInHideoutSiege;
	}
	
	public boolean isInHideoutSiege()
	{
		return _isInHideoutSiege;
	}
	
	public FloodProtectors getFloodProtectors()
	{
		return getClient().getFloodProtectors();
	}
	
	public boolean isFlyingMounted()
	{
		return (isTransformed() && (getTransformation().isFlying()));
	}
	
	public int getCharges()
	{
		return _charges.get();
	}
	
	public void increaseCharges(int count, int max)
	{
		if (_charges.get() >= max)
		{
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			return;
		}
		restartChargeTask();
		
		if (_charges.addAndGet(count) >= max)
		{
			_charges.set(max);
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
		}
		else
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
			sm.addNumber(_charges.get());
			sendPacket(sm);
		}
		
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public boolean decreaseCharges(int count)
	{
		if (_charges.get() < count)
		{
			return false;
		}
		
		if (_charges.addAndGet(-count) == 0)
		{
			stopChargeTask();
		}
		else
		{
			restartChargeTask();
		}
		
		sendPacket(new EtcStatusUpdate(this));
		return true;
	}
	
	public void clearCharges()
	{
		_charges.set(0);
		sendPacket(new EtcStatusUpdate(this));
	}
	
	private void restartChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
		_chargeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ResetChargesTask(this), 600000);
	}
	
	public void stopChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
	}
	
	public void teleportBookmarkModify(int id, int icon, String tag, String name)
	{
		final TeleportBookmark bookmark = _tpbookmarks.get(id);
		if (bookmark != null)
		{
			bookmark.setIcon(icon);
			bookmark.setTag(tag);
			bookmark.setName(name);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_TP_BOOKMARK))
			{
				statement.setInt(1, icon);
				statement.setString(2, tag);
				statement.setString(3, name);
				statement.setInt(4, getObjectId());
				statement.setInt(5, id);
				statement.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not update character teleport bookmark data: " + e.getMessage(), e);
			}
		}
		
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}
	
	public void teleportBookmarkDelete(int id)
	{
		if (_tpbookmarks.remove(id) != null)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(DELETE_TP_BOOKMARK))
			{
				statement.setInt(1, getObjectId());
				statement.setInt(2, id);
				statement.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not delete character teleport bookmark data: " + e.getMessage(), e);
			}
			
			sendPacket(new ExGetBookMarkInfoPacket(this));
		}
	}
	
	public void teleportBookmarkGo(int id)
	{
		if (!teleportBookmarkCondition(0))
		{
			return;
		}
		if (getInventory().getInventoryItemCount(13016, 0) == 0)
		{
			sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_BECAUSE_YOU_DO_NOT_HAVE_A_TELEPORT_ITEM);
			return;
		}
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(13016);
		sendPacket(sm);
		
		final TeleportBookmark bookmark = _tpbookmarks.get(id);
		if (bookmark != null)
		{
			destroyItem("Consume", getInventory().getItemByItemId(13016).getObjectId(), 1, null, false);
			teleToLocation(bookmark, false);
		}
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}
	
	public boolean teleportBookmarkCondition(int type)
	{
		if (isInCombat())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
			return false;
		}
		else if (isInSiege() || (getSiegeState() != 0))
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING);
			return false;
		}
		else if (isInDuel())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
			return false;
		}
		else if (isFlying())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
			return false;
		}
		else if (isInOlympiadMode())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
			return false;
		}
		else if (isParalyzed())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_PARALYZED);
			return false;
		}
		else if (isDead())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD);
			return false;
		}
		else if ((type == 1) && (isIn7sDungeon() || (isInParty() && getParty().isInDimensionalRift())))
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			return false;
		}
		else if (isInWater())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
			return false;
		}
		else if ((type == 1) && (isInsideZone(ZoneId.SIEGE) || isInsideZone(ZoneId.CLAN_HALL) || isInsideZone(ZoneId.JAIL) || isInsideZone(ZoneId.CASTLE) || isInsideZone(ZoneId.NO_SUMMON_FRIEND) || isInsideZone(ZoneId.FORT)))
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			return false;
		}
		else if (isInsideZone(ZoneId.NO_BOOKMARK) || isInBoat() || isInAirShip())
		{
			if (type == 0)
			{
				sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA);
			}
			else if (type == 1)
			{
				sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			}
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public void teleportBookmarkAdd(int x, int y, int z, int icon, String tag, String name)
	{
		if (!teleportBookmarkCondition(1))
		{
			return;
		}
		
		if (_tpbookmarks.size() >= _bookmarkslot)
		{
			sendPacket(SystemMessageId.YOU_HAVE_NO_SPACE_TO_SAVE_THE_TELEPORT_LOCATION);
			return;
		}
		
		if (getInventory().getInventoryItemCount(20033, 0) == 0)
		{
			sendPacket(SystemMessageId.YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG);
			return;
		}
		
		int id;
		for (id = 1; id <= _bookmarkslot; ++id)
		{
			if (!_tpbookmarks.containsKey(id))
			{
				break;
			}
		}
		
		_tpbookmarks.put(id, new TeleportBookmark(id, x, y, z, icon, tag, name));
		
		destroyItem("Consume", getInventory().getItemByItemId(20033).getObjectId(), 1, null, false);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(20033);
		sendPacket(sm);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_TP_BOOKMARK))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, id);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, icon);
			statement.setString(7, tag);
			statement.setString(8, name);
			
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not insert character teleport bookmark data: " + e.getMessage(), e);
		}
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}
	
	public void restoreTeleportBookmark()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_TP_BOOKMARK))
		{
			statement.setInt(1, getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					_tpbookmarks.put(rset.getInt("Id"), new TeleportBookmark(rset.getInt("Id"), rset.getInt("x"), rset.getInt("y"), rset.getInt("z"), rset.getInt("icon"), rset.getString("tag"), rset.getString("name")));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed restoing character teleport bookmark.", e);
		}
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (isInBoat())
		{
			getPosition().setWorldPosition(getBoat().getPosition().getWorldPosition());
			
			activeChar.sendPacket(new CharInfo(this));
			activeChar.sendPacket(new ExBrExtraUserInfo(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if ((oldrelation != null) && (oldrelation != relation1))
			{
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if (hasSummon())
				{
					activeChar.sendPacket(new RelationChanged(getSummon(), relation1, isAutoAttackable(activeChar)));
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if ((oldrelation != null) && (oldrelation != relation2))
			{
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if (activeChar.hasSummon())
				{
					sendPacket(new RelationChanged(activeChar.getSummon(), relation2, activeChar.isAutoAttackable(this)));
				}
			}
			activeChar.sendPacket(new GetOnVehicle(getObjectId(), getBoat().getObjectId(), getInVehiclePosition()));
		}
		else if (isInAirShip())
		{
			getPosition().setWorldPosition(getAirShip().getPosition().getWorldPosition());
			
			activeChar.sendPacket(new CharInfo(this));
			activeChar.sendPacket(new ExBrExtraUserInfo(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if ((oldrelation != null) && (oldrelation != relation1))
			{
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if (hasSummon())
				{
					activeChar.sendPacket(new RelationChanged(getSummon(), relation1, isAutoAttackable(activeChar)));
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if ((oldrelation != null) && (oldrelation != relation2))
			{
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if (activeChar.hasSummon())
				{
					sendPacket(new RelationChanged(activeChar.getSummon(), relation2, activeChar.isAutoAttackable(this)));
				}
			}
			activeChar.sendPacket(new ExGetOnAirShip(this, getAirShip()));
		}
		else
		{
			activeChar.sendPacket(new CharInfo(this));
			activeChar.sendPacket(new ExBrExtraUserInfo(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if ((oldrelation != null) && (oldrelation != relation1))
			{
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if (hasSummon())
				{
					activeChar.sendPacket(new RelationChanged(getSummon(), relation1, isAutoAttackable(activeChar)));
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if ((oldrelation != null) && (oldrelation != relation2))
			{
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if (activeChar.hasSummon())
				{
					sendPacket(new RelationChanged(activeChar.getSummon(), relation2, activeChar.isAutoAttackable(this)));
				}
			}
		}
		
		switch (getPrivateStoreType())
		{
			case L2PcInstance.STORE_PRIVATE_SELL:
				activeChar.sendPacket(new PrivateStoreMsgSell(this));
				break;
			case L2PcInstance.STORE_PRIVATE_PACKAGE_SELL:
				activeChar.sendPacket(new ExPrivateStoreSetWholeMsg(this));
				break;
			case L2PcInstance.STORE_PRIVATE_BUY:
				activeChar.sendPacket(new PrivateStoreMsgBuy(this));
				break;
			case L2PcInstance.STORE_PRIVATE_MANUFACTURE:
				activeChar.sendPacket(new RecipeShopMsg(this));
				break;
		}
	}
	
	public void showQuestMovie(int id)
	{
		if (_movieId > 0)
		{
			return;
		}
		abortAttack();
		abortCast();
		stopMove(null);
		_movieId = id;
		sendPacket(new ExStartScenePlayer(id));
	}
	
	public boolean isAllowedToEnchantSkills()
	{
		if (isLocked())
		{
			return false;
		}
		if (isTransformed() || isInStance())
		{
			return false;
		}
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(this))
		{
			return false;
		}
		if (isCastingNow() || isCastingSimultaneouslyNow())
		{
			return false;
		}
		if (isInBoat() || isInAirShip())
		{
			return false;
		}
		return true;
	}
	
	public void setCreateDate(Calendar createDate)
	{
		_createDate = createDate;
	}
	
	public Calendar getCreateDate()
	{
		return _createDate;
	}
	
	public int checkBirthDay()
	{
		Calendar now = Calendar.getInstance();
		
		if ((_createDate.get(Calendar.DAY_OF_MONTH) == 29) && (_createDate.get(Calendar.MONTH) == 1))
		{
			_createDate.add(Calendar.HOUR_OF_DAY, -24);
		}
		
		if ((now.get(Calendar.MONTH) == _createDate.get(Calendar.MONTH)) && (now.get(Calendar.DAY_OF_MONTH) == _createDate.get(Calendar.DAY_OF_MONTH)) && (now.get(Calendar.YEAR) != _createDate.get(Calendar.YEAR)))
		{
			return 0;
		}
		
		int i;
		for (i = 1; i < 6; i++)
		{
			now.add(Calendar.HOUR_OF_DAY, 24);
			if ((now.get(Calendar.MONTH) == _createDate.get(Calendar.MONTH)) && (now.get(Calendar.DAY_OF_MONTH) == _createDate.get(Calendar.DAY_OF_MONTH)) && (now.get(Calendar.YEAR) != _createDate.get(Calendar.YEAR)))
			{
				return i;
			}
		}
		return -1;
	}
	
	private final List<Integer> _friendList = new FastList<>();
	private final List<Integer> _completedAchievements = new FastList<>();
	
	public List<Integer> getFriendList()
	{
		return _friendList;
	}
	
	public void restoreFriendList()
	{
		_friendList.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			String sqlQuery = "SELECT friendId FROM character_friends WHERE charId=? AND relation=0";
			PreparedStatement statement = con.prepareStatement(sqlQuery);
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			
			int friendId;
			while (rset.next())
			{
				friendId = rset.getInt("friendId");
				if (friendId == getObjectId())
				{
					continue;
				}
				_friendList.add(friendId);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error found in " + getName() + "'s FriendList: " + e.getMessage(), e);
		}
	}
	
	private void notifyFriends()
	{
		FriendStatusPacket pkt = new FriendStatusPacket(getObjectId());
		for (int id : _friendList)
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(id);
			if (friend != null)
			{
				friend.sendPacket(pkt);
			}
		}
	}
	
	public boolean isSilenceMode()
	{
		return _silenceMode;
	}
	
	public boolean isSilenceMode(int playerObjId)
	{
		if (Config.SILENCE_MODE_EXCLUDE && _silenceMode && (_silenceModeExcluded != null))
		{
			return !_silenceModeExcluded.contains(playerObjId);
		}
		return _silenceMode;
	}
	
	public void setSilenceMode(boolean mode)
	{
		_silenceMode = mode;
		if (_silenceModeExcluded != null)
		{
			_silenceModeExcluded.clear();
		}
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void addSilenceModeExcluded(int playerObjId)
	{
		if (_silenceModeExcluded == null)
		{
			_silenceModeExcluded = new ArrayList<>(1);
		}
		_silenceModeExcluded.add(playerObjId);
	}
	
	private void storeRecipeShopList()
	{
		if (hasManufactureShop())
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				try (PreparedStatement st = con.prepareStatement("DELETE FROM character_recipeshoplist WHERE charId=?"))
				{
					st.setInt(1, getObjectId());
					st.execute();
				}
				
				try (PreparedStatement st = con.prepareStatement("INSERT INTO character_recipeshoplist (`charId`, `recipeId`, `price`, `index`) VALUES (?, ?, ?, ?)"))
				{
					int i = 1;
					for (L2ManufactureItem item : _manufactureItems.values())
					{
						st.setInt(1, getObjectId());
						st.setInt(2, item.getRecipeId());
						st.setLong(3, item.getCost());
						st.setInt(4, i++);
						st.addBatch();
					}
					st.executeBatch();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Could not store recipe shop for playerId " + getObjectId() + ": ", e);
			}
		}
	}
	
	private void restoreRecipeShopList()
	{
		if (_manufactureItems != null)
		{
			_manufactureItems.clear();
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM character_recipeshoplist WHERE charId=? ORDER BY `index`"))
		{
			statement.setInt(1, getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					getManufactureItems().put(rset.getInt("recipeId"), new L2ManufactureItem(rset.getInt("recipeId"), rset.getLong("price")));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore recipe shop list data for playerId: " + getObjectId(), e);
		}
	}
	
	public double getCollisionRadius()
	{
		if (isMounted() && (getMountNpcId() > 0))
		{
			return NpcTable.getInstance().getTemplate(getMountNpcId()).getfCollisionRadius();
		}
		else if (isTransformed())
		{
			return getTransformation().getCollisionRadius(this);
		}
		return getAppearance().getSex() ? getBaseTemplate().getFCollisionRadiusFemale() : getBaseTemplate().getfCollisionRadius();
	}
	
	public double getCollisionHeight()
	{
		if (isMounted() && (getMountNpcId() > 0))
		{
			return NpcTable.getInstance().getTemplate(getMountNpcId()).getfCollisionHeight();
		}
		else if (isTransformed())
		{
			return getTransformation().getCollisionHeight(this);
		}
		return getAppearance().getSex() ? getBaseTemplate().getFCollisionHeightFemale() : getBaseTemplate().getfCollisionHeight();
	}
	
	public final int getClientX()
	{
		return _clientX;
	}
	
	public final int getClientY()
	{
		return _clientY;
	}
	
	public final int getClientZ()
	{
		return _clientZ;
	}
	
	public final int getClientHeading()
	{
		return _clientHeading;
	}
	
	public final void setClientX(int val)
	{
		_clientX = val;
	}
	
	public final void setClientY(int val)
	{
		_clientY = val;
	}
	
	public final void setClientZ(int val)
	{
		_clientZ = val;
	}
	
	public final void setClientHeading(int val)
	{
		_clientHeading = val;
	}
	
	public final boolean isFalling(int z)
	{
		if (isDead() || isFlying() || isFlyingMounted() || isInsideZone(ZoneId.WATER))
		{
			return false;
		}
		
		if (System.currentTimeMillis() < _fallingTimestamp)
		{
			return true;
		}
		
		final int deltaZ = getZ() - z;
		if (deltaZ <= getBaseTemplate().getSafeFallHeight())
		{
			return false;
		}
		
		final int damage = (int) Formulas.calcFallDam(this, deltaZ);
		if (damage > 0)
		{
			reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null, false, true, null);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1);
			sm.addNumber(damage);
			sendPacket(sm);
		}
		
		setFalling();
		
		return false;
	}
	
	public final void setFalling()
	{
		_fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
	}
	
	public int getMovieId()
	{
		return _movieId;
	}
	
	public void setMovieId(int id)
	{
		_movieId = id;
	}
	
	public void updateLastItemAuctionRequest()
	{
		_lastItemAuctionInfoRequest = System.currentTimeMillis();
	}
	
	public boolean isItemAuctionPolling()
	{
		return (System.currentTimeMillis() - _lastItemAuctionInfoRequest) < 2000;
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || (_movieId > 0);
	}
	
	private void restoreUISettings()
	{
		_uiKeySettings = new UIKeysSettings(getObjectId());
	}
	
	private void storeUISettings()
	{
		if (_uiKeySettings == null)
		{
			return;
		}
		
		if (!_uiKeySettings.isSaved())
		{
			_uiKeySettings.saveInDB();
		}
	}
	
	public UIKeysSettings getUISettings()
	{
		return _uiKeySettings;
	}
	
	public String getLang()
	{
		return Config.MULTILANG_ENABLE && (getVar("lang@") != null) ? getVar("lang@") : Config.MULTILANG_DEFAULT;
	}
	
	public void setLang(String lang)
	{
		setVar("lang@", lang);
	}
	
	public boolean getUseAutoLoot()
	{
		if (Config.AUTO_LOOT && getVarB("useAutoLoot@"))
			return true;
		else
			return !Config.ALLOW_AUTOLOOT_COMMAND || getVarB("useAutoLoot@");
	}
	
	public boolean getUseAutoLootHerbs()
	{
		return !Config.ALLOW_AUTOLOOT_COMMAND || getVarB("useAutoLootHerbs@");
	}
	
	public boolean getExpOn()
	{
		return !getVarB("blockedEXP@");
	}
	
	HashMap<String, String> user_variables = new HashMap<>();
	
	public void setVar(String name, Object value)
	{
		setVar(name, String.valueOf(value));
	}
	
	public void setVar(String name, String value)
	{
		if (user_variables.containsKey(name) && user_variables.get(name).equals(value))
		{
			return;
		}
		user_variables.put(name, value);
		MySqlUtils.executeStatement("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (" + getObjectId() + ",'user-var','" + Strings.addSlashes(name) + "','" + Strings.addSlashes(value) + "',-1)");
	}
	
	public void unsetVar(String name)
	{
		if (name == null)
		{
			return;
		}
		if (user_variables.remove(name) != null)
		{
			MySqlUtils.executeStatement("DELETE FROM `character_variables` WHERE `obj_id`='" + getObjectId() + "' AND `type`='user-var' AND `name`='" + name + "' LIMIT 1");
		}
	}
	
	public String getVar(String name)
	{
		return user_variables.get(name);
	}
	
	public boolean getVarB(String name, boolean defaultVal)
	{
		String var = user_variables.get(name);
		if (var == null)
		{
			return defaultVal;
		}
		return ((!(var.equals("0"))) && (!(var.equalsIgnoreCase("false"))));
	}
	
	public boolean getVarB(String name)
	{
		String var = user_variables.get(name);
		return ((var != null) && (!(var.equals("0"))) && (!(var.equalsIgnoreCase("false"))));
	}
	
	public HashMap<String, String> getVars()
	{
		return user_variables;
	}
	
	private void loadVariables()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement offline = con.prepareStatement("SELECT * FROM character_variables WHERE obj_id = ?"))
		{
			offline.setInt(1, getObjectId());
			ResultSet rs = offline.executeQuery();
			while (rs.next())
			{
				String name = rs.getString("name");
				String value = Strings.stripSlashes(rs.getString("value"));
				this.user_variables.put(name, value);
			}
			
			if (getVar("lang@") == null)
			{
				setVar("lang@", Config.MULTILANG_DEFAULT);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public long getOfflineStartTime()
	{
		return _offlineShopStart;
	}
	
	public void setOfflineStartTime(long time)
	{
		_offlineShopStart = time;
	}
	
	public void removeFromBossZone()
	{
		try
		{
			for (L2BossZone _zone : GrandBossManager.getInstance().getZones())
			{
				_zone.removePlayer(this);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on removeFromBossZone(): " + e.getMessage(), e);
		}
	}
	
	public void checkPlayerSkills()
	{
		L2SkillLearn learn;
		for (Entry<Integer, L2Skill> e : getSkills().entrySet())
		{
			learn = SkillTreesParser.getInstance().getClassSkill(e.getKey(), e.getValue().getLevel() % 100, getClassId());
			if (learn != null)
			{
				int lvlDiff = e.getKey() == L2Skill.SKILL_EXPERTISE ? 0 : 9;
				if (getLevel() < (learn.getGetLevel() - lvlDiff))
				{
					deacreaseSkillLevel(e.getValue(), lvlDiff);
				}
			}
		}
	}
	
	private void deacreaseSkillLevel(L2Skill skill, int lvlDiff)
	{
		int nextLevel = -1;
		final Map<Integer, L2SkillLearn> skillTree = SkillTreesParser.getInstance().getCompleteClassSkillTree(getClassId());
		for (L2SkillLearn sl : skillTree.values())
		{
			if ((sl.getSkillId() == skill.getId()) && (nextLevel < sl.getSkillLevel()) && (getLevel() >= (sl.getGetLevel() - lvlDiff)))
			{
				nextLevel = sl.getSkillLevel();
			}
		}
		
		if (nextLevel == -1)
		{
			_log.info("Removing skill " + skill + " from player " + toString());
			removeSkill(skill, true);
		}
		else
		{
			_log.info("Decreasing skill " + skill + " to " + nextLevel + " for player " + toString());
			addSkill(SkillHolder.getInstance().getInfo(skill.getId(), nextLevel), true);
		}
	}
	
	public boolean canMakeSocialAction()
	{
		return ((getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE) && (getActiveRequester() == null) && !isAlikeDead() && !isAllSkillsDisabled() && !isInDuel() && !isCastingNow() && !isCastingSimultaneouslyNow() && (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) && !AttackStanceTaskManager.getInstance().hasAttackStanceTask(this) && !isInOlympiadMode());
	}
	
	public void setMultiSocialAction(int id, int targetId)
	{
		_multiSociaAction = id;
		_multiSocialTarget = targetId;
	}
	
	public int getMultiSociaAction()
	{
		return _multiSociaAction;
	}
	
	public int getMultiSocialTarget()
	{
		return _multiSocialTarget;
	}
	
	public Collection<TeleportBookmark> getTeleportBookmarks()
	{
		return _tpbookmarks.values();
	}
	
	public int getBookmarkslot()
	{
		return _bookmarkslot;
	}
	
	public int getQuestInventoryLimit()
	{
		return Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}
	
	public boolean canAttackCharacter(L2Character cha)
	{
		if (cha instanceof L2Attackable)
		{
			return true;
		}
		else if (cha instanceof L2Playable)
		{
			if (cha.isInsideZone(ZoneId.PVP) && !cha.isInsideZone(ZoneId.SIEGE))
			{
				return true;
			}
			
			L2PcInstance target;
			if (cha instanceof L2Summon)
			{
				target = ((L2Summon) cha).getOwner();
			}
			else
			{
				target = (L2PcInstance) cha;
			}
			
			if (isInDuel() && target.isInDuel() && (target.getDuelId() == getDuelId()))
			{
				return true;
			}
			else if (isInParty() && target.isInParty())
			{
				if (getParty() == target.getParty())
				{
					return false;
				}
				if (((getParty().getCommandChannel() != null) || (target.getParty().getCommandChannel() != null)) && (getParty().getCommandChannel() == target.getParty().getCommandChannel()))
				{
					return false;
				}
			}
			else if ((getClan() != null) && (target.getClan() != null))
			{
				if (getClanId() == target.getClanId())
				{
					return false;
				}
				if (((getAllyId() > 0) || (target.getAllyId() > 0)) && (getAllyId() == target.getAllyId()))
				{
					return false;
				}
				if (getClan().isAtWarWith(target.getClan().getId()) && target.getClan().isAtWarWith(getClan().getId()))
				{
					return true;
				}
			}
			else if ((getClan() == null) || (target.getClan() == null))
			{
				if ((target.getPvpFlag() == 0) && (target.getKarma() == 0))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isInventoryUnder90(boolean includeQuestInv)
	{
		return (getInventory().getSize(includeQuestInv) <= (getInventoryLimit() * 0.9));
	}
	
	public boolean havePetInvItems()
	{
		return _petItems;
	}
	
	public void setPetInvItems(boolean haveit)
	{
		_petItems = haveit;
	}
	
	private void restorePetInventoryItems()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT object_id FROM `items` WHERE `owner_id`=? AND (`loc`='PET' OR `loc`='PET_EQUIP') LIMIT 1;"))
		{
			statement.setInt(1, getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next() && (rset.getInt("object_id") > 0))
				{
					setPetInvItems(true);
				}
				else
				{
					setPetInvItems(false);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not check Items in Pet Inventory for playerId: " + getObjectId(), e);
		}
	}
	
	public String getAdminConfirmCmd()
	{
		return _adminConfirmCmd;
	}
	
	public void setAdminConfirmCmd(String adminConfirmCmd)
	{
		_adminConfirmCmd = adminConfirmCmd;
	}
	
	public void setBlockCheckerArena(byte arena)
	{
		_handysBlockCheckerEventArena = arena;
	}
	
	public int getBlockCheckerArena()
	{
		return _handysBlockCheckerEventArena;
	}
	
	private long loadRecommendations()
	{
		long _time_left = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT rec_have,rec_left,time_left FROM character_reco_bonus WHERE charId=? LIMIT 1"))
		{
			statement.setInt(1, getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					setRecomHave(rset.getInt("rec_have"));
					setRecomLeft(rset.getInt("rec_left"));
					_time_left = rset.getLong("time_left");
				}
				else
				{
					_time_left = 3600000;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore Recommendations for player: " + getObjectId(), e);
		}
		return _time_left;
	}
	
	public void storeRecommendations()
	{
		long recoTaskEnd = 0;
		if (_recoBonusTask != null)
		{
			recoTaskEnd = Math.max(0, _recoBonusTask.getDelay(TimeUnit.MILLISECONDS));
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO character_reco_bonus (charId,rec_have,rec_left,time_left) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE rec_have=?, rec_left=?, time_left=?"))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, getRecomHave());
			statement.setInt(3, getRecomLeft());
			statement.setLong(4, recoTaskEnd);
			statement.setInt(5, getRecomHave());
			statement.setInt(6, getRecomLeft());
			statement.setLong(7, recoTaskEnd);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not update Recommendations for player: " + getObjectId(), e);
		}
	}
	
	public boolean RecoBonusActive()
	{
		return _isRecoBonusActive;
	}
	
	public void setRecoBonusActive(boolean mode)
	{
		_isRecoBonusActive = mode;
		sendPacket(new ExVoteSystemInfo(this));
	}
	
	public void checkRecoBonusTask()
	{
		long taskTime = loadRecommendations();
		
		if (taskTime > 0)
		{
			if (taskTime == 3600000)
			{
				setRecomLeft(getRecomLeft() + 20);
			}
			_recoBonusTask = ThreadPoolManager.getInstance().scheduleGeneral(new RecoBonusTaskEnd(this), taskTime);
		}
		_recoGiveTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RecoGiveTask(this), 7200000, 3600000);
		storeRecommendations();
	}
	
	public void stopRecoBonusTask()
	{
		if (_recoBonusTask != null)
		{
			_recoBonusTask.cancel(false);
			_recoBonusTask = null;
		}
	}
	
	public void stopRecoGiveTask()
	{
		if (_recoGiveTask != null)
		{
			_recoGiveTask.cancel(false);
			_recoGiveTask = null;
		}
	}
	
	public boolean isRecoTwoHoursGiven()
	{
		return _recoTwoHoursGiven;
	}
	
	public void setRecoTwoHoursGiven(boolean val)
	{
		_recoTwoHoursGiven = val;
	}
	
	public int getRecomBonusTime()
	{
		if (_recoBonusTask != null)
		{
			return (int) Math.max(0, _recoBonusTask.getDelay(TimeUnit.SECONDS));
		}
		
		return 0;
	}
	
	public int getRecomBonusType()
	{
		return _recoBonusMode;
	}
	
	public void setLastPetitionGmName(String gmName)
	{
		_lastPetitionGmName = gmName;
	}
	
	public String getLastPetitionGmName()
	{
		return _lastPetitionGmName;
	}
	
	public L2ContactList getContactList()
	{
		return _contactList;
	}
	
	public void setEventStatus()
	{
		eventStatus = new PlayerEventStatus(this);
	}
	
	public void setEventStatus(PlayerEventStatus pes)
	{
		eventStatus = pes;
	}
	
	public PlayerEventStatus getEventStatus()
	{
		return eventStatus;
	}
	
	public L2PcInstance setRecomBonusType(int mode)
	{
		_recoBonusMode = mode;
		return this;
	}
	
	public long getNotMoveUntil()
	{
		return _notMoveUntil;
	}
	
	public void updateNotMoveUntil()
	{
		_notMoveUntil = System.currentTimeMillis() + Config.PLAYER_MOVEMENT_BLOCK_TIME;
	}
	
	@Override
	public boolean isPlayer()
	{
		return true;
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		final L2ItemInstance weapon = getActiveWeaponInstance();
		return (weapon != null) && weapon.isChargedShot(type);
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		final L2ItemInstance weapon = getActiveWeaponInstance();
		if (weapon != null)
		{
			weapon.setChargedShot(type, charged);
		}
	}
	
	public final L2Skill getCustomSkill(int skillId)
	{
		return (_customSkills != null) ? _customSkills.get(skillId) : null;
	}
	
	private final void addCustomSkill(L2Skill skill)
	{
		if ((skill != null) && (skill.getDisplayId() != skill.getId()))
		{
			if (_customSkills == null)
			{
				_customSkills = new FastMap<Integer, L2Skill>().shared();
			}
			_customSkills.put(skill.getDisplayId(), skill);
		}
	}
	
	private final void removeCustomSkill(L2Skill skill)
	{
		if ((skill != null) && (_customSkills != null) && (skill.getDisplayId() != skill.getId()))
		{
			_customSkills.remove(skill.getDisplayId());
		}
	}
	
	@Override
	public boolean canRevive()
	{
		return _canRevive;
	}
	
	@Override
	public void setCanRevive(boolean val)
	{
		_canRevive = val;
	}
	
	private boolean fireEquipmentListeners(boolean isEquiped, L2ItemInstance item)
	{
		if (item != null)
		{
			EquipmentEvent event = new EquipmentEvent();
			event.setEquipped(!isEquiped);
			event.setItem(item);
			for (EquipmentListener listener : _equipmentListeners)
			{
				if (!listener.onEquip(event))
				{
					return false;
				}
			}
			for (EquipmentListener listener : GLOBAL_EQUIPMENT_LISTENERS)
			{
				if (!listener.onEquip(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean fireTransformListeners(Transform transformation, boolean isTransforming)
	{
		if ((transformation != null) && !_transformListeners.isEmpty())
		{
			TransformEvent event = new TransformEvent();
			event.setTransformation(transformation);
			event.setTransforming(isTransforming);
			for (TransformListener listener : _transformListeners)
			{
				if (!listener.onTransform(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean fireHennaListeners(L2Henna henna, boolean isAdding)
	{
		if ((henna != null) && !HENNA_LISTENERS.isEmpty())
		{
			HennaEvent event = new HennaEvent();
			event.setAdd(isAdding);
			event.setHenna(henna);
			event.setPlayer(this);
			for (HennaListener listener : HENNA_LISTENERS)
			{
				if (!listener.onRemoveHenna(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private void fireProfessionChangeListeners(L2PcTemplate t)
	{
		if (!_professionChangeListeners.isEmpty() || !GLOBAL_PROFESSION_CHANGE_LISTENERS.isEmpty())
		{
			ProfessionChangeEvent event = null;
			event = new ProfessionChangeEvent();
			event.setPlayer(this);
			event.setSubClass(isSubClassActive());
			event.setTemplate(t);
			for (ProfessionChangeListener listener : _professionChangeListeners)
			{
				listener.professionChanged(event);
			}
			for (ProfessionChangeListener listener : GLOBAL_PROFESSION_CHANGE_LISTENERS)
			{
				listener.professionChanged(event);
			}
		}
	}
	
	@Override
	public boolean isOnEvent()
	{
		for (EventListener listener : _eventListeners)
		{
			if (listener.isOnEvent())
			{
				return true;
			}
		}
		return super.isOnEvent();
	}
	
	public boolean isBlockedFromExit()
	{
		for (EventListener listener : _eventListeners)
		{
			if (listener.isOnEvent() && listener.isBlockingExit())
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isBlockedFromDeathPenalty()
	{
		for (EventListener listener : _eventListeners)
		{
			if (listener.isOnEvent() && listener.isBlockingDeathPenalty())
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addOverrideCond(PcCondOverride... excs)
	{
		super.addOverrideCond(excs);
		getVariables().set(COND_OVERRIDE_KEY, Long.toString(_exceptions));
	}
	
	@Override
	public void removeOverridedCond(PcCondOverride... excs)
	{
		super.removeOverridedCond(excs);
		getVariables().set(COND_OVERRIDE_KEY, Long.toString(_exceptions));
	}
	
	public boolean hasVariables()
	{
		return getScript(PlayerVariables.class) != null;
	}
	
	public PlayerVariables getVariables()
	{
		final PlayerVariables vars = getScript(PlayerVariables.class);
		return vars != null ? vars : addScript(new PlayerVariables(getObjectId()));
	}
	
	public boolean hasAccountVariables()
	{
		return getScript(AccountVariables.class) != null;
	}
	
	public AccountVariables getAccountVariables()
	{
		final AccountVariables vars = getScript(AccountVariables.class);
		return vars != null ? vars : addScript(new AccountVariables(getAccountName()));
	}
	
	public static void addHennaListener(HennaListener listener)
	{
		if (!HENNA_LISTENERS.contains(listener))
		{
			HENNA_LISTENERS.add(listener);
		}
	}
	
	public static void removeHennaListener(HennaListener listener)
	{
		HENNA_LISTENERS.remove(listener);
	}
	
	public void addEquipmentListener(EquipmentListener listener)
	{
		if (!_equipmentListeners.contains(listener))
		{
			_equipmentListeners.add(listener);
		}
	}
	
	public void removeEquipmentListener(EquipmentListener listener)
	{
		_equipmentListeners.remove(listener);
	}
	
	public static void addGlobalEquipmentListener(EquipmentListener listener)
	{
		if (!GLOBAL_EQUIPMENT_LISTENERS.contains(listener))
		{
			GLOBAL_EQUIPMENT_LISTENERS.add(listener);
		}
	}
	
	public static void removeGlobalEquipmentListener(EquipmentListener listener)
	{
		GLOBAL_EQUIPMENT_LISTENERS.remove(listener);
	}
	
	public void addTransformListener(TransformListener listener)
	{
		if (!_transformListeners.contains(listener))
		{
			_transformListeners.add(listener);
		}
	}
	
	public void removeTransformListener(TransformListener listener)
	{
		_transformListeners.remove(listener);
	}
	
	public void addProfessionChangeListener(ProfessionChangeListener listener)
	{
		if (!_professionChangeListeners.contains(listener))
		{
			_professionChangeListeners.add(listener);
		}
	}
	
	public void removeProfessionChangeListener(ProfessionChangeListener listener)
	{
		_professionChangeListeners.remove(listener);
	}
	
	public static void addGlobalProfessionChangeListener(ProfessionChangeListener listener)
	{
		if (!GLOBAL_PROFESSION_CHANGE_LISTENERS.contains(listener))
		{
			GLOBAL_PROFESSION_CHANGE_LISTENERS.add(listener);
		}
	}
	
	public static void removeGlobalProfessionChangeListener(ProfessionChangeListener listener)
	{
		GLOBAL_PROFESSION_CHANGE_LISTENERS.remove(listener);
	}
	
	public void addEventListener(EventListener listener)
	{
		_eventListeners.add(listener);
	}
	
	public void removeEventListener(EventListener listener)
	{
		_eventListeners.remove(listener);
	}
	
	public void removeEventListener(Class<? extends EventListener> clazz)
	{
		final Iterator<EventListener> it = _eventListeners.iterator();
		EventListener event;
		while (it.hasNext())
		{
			event = it.next();
			if (event.getClass() == clazz)
			{
				it.remove();
			}
		}
	}
	
	public Collection<EventListener> getEventListeners()
	{
		return _eventListeners;
	}
	
	public void enterMovieMode()
	{
		setTarget(null);
		stopMove(null);
		setIsInvul(true);
		setIsImmobilized(true);
		sendPacket(CameraMode.FIRST_PERSON);
	}
	
	public void leaveMovieMode()
	{
		if (!isGM())
		{
			setIsInvul(false);
		}
		setIsImmobilized(false);
		sendPacket(CameraMode.THIRD_PERSON);
		sendPacket(NormalCamera.STATIC_PACKET);
	}
	
	public void specialCamera(L2Object target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCamera((L2Character) target, dist, yaw, pitch, time, duration));
	}
	
	public int getPcBangPoints()
	{
		return _pcBangPoints;
	}
	
	public void setPcBangPoints(final int i)
	{
		if (i < 200000)
		{
			_pcBangPoints = i;
		}
		else
		{
			_pcBangPoints = 200000;
		}
	}
	
	private void createPSdb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO character_premium (account_name,premium_service,enddate) values(?,?,?)");
			statement.setString(1, _accountName);
			statement.setInt(2, 0);
			statement.setLong(3, 0);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not insert char data: " + e);
			e.printStackTrace();
			return;
		}
	}
	
	private static void PStimeOver(String account)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE);
			statement.setInt(1, 0);
			statement.setLong(2, 0);
			statement.setString(3, account);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning("PremiumService:  Could not increase data");
		}
	}
	
	private static void restorePremServiceData(L2PcInstance player, String account)
	{
		boolean sucess = false;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_PREMIUMSERVICE);
			statement.setString(1, account);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				sucess = true;
				if (Config.USE_PREMIUMSERVICE)
				{
					if (rset.getLong("enddate") <= System.currentTimeMillis())
					{
						PStimeOver(account);
						player.setPremiumService(0);
					}
					else
					{
						player.setPremiumService(rset.getInt("premium_service"));
					}
				}
				else
				{
					player.setPremiumService(0);
				}
			}
			statement.close();
			
		}
		catch (Exception e)
		{
			_log.warning("PremiumService: Could not restore PremiumService data for:" + account + "." + e);
			e.printStackTrace();
		}
		
		if (sucess == false)
		{
			player.createPSdb();
			player.setPremiumService(0);
		}
	}
	
	public String getEventName()
	{
		return _eventName == null ? "" : _eventName;
	}
	
	public boolean isFightingInTW()
	{
		L2TownZone Town = TownManager.getTown(getX(), getY(), getZ());
		TW event = (TW) FunEventsManager.getInstance().getEvent("TW");
		if ((Town != null) && (event.getState() == FunEvent.State.FIGHTING))
		{
			if (((Town.getTownId() == Config.TW_TOWN_ID) && !Config.TW_ALL_TOWNS) || Config.TW_ALL_TOWNS)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isFightingInEvent()
	{
		return FunEventsManager.getInstance().isFightingInEvent(this);
	}
	
	public boolean isInSameTeam(L2PcInstance player)
	{
		return isInSameEvent(player) && (_eventTeamId == player._eventTeamId);
		
	}
	
	public boolean isInSameEvent(L2PcInstance player)
	{
		return isFightingInEvent() && player.isFightingInEvent() && getEventName().equals(player.getEventName());
		
	}
	
	public synchronized void updateLastActivityMove()
	{
		_lastActivityMove = System.currentTimeMillis();
	}
	
	public synchronized boolean checkLastActivityMove(long checkTime)
	{
		return (_lastActivityMove >= (System.currentTimeMillis() - checkTime));
	}
	
	public synchronized void updateLastActivityAction()
	{
		_lastActivityAction = System.currentTimeMillis();
	}
	
	public synchronized boolean checkLastActivityAction(long checkTime)
	{
		return (_lastActivityAction >= (System.currentTimeMillis() - checkTime));
	}
	
	public int getUCKills()
	{
		return UCKills;
	}
	
	public void increaseKillCountUC()
	{
		UCKills++;
	}
	
	public int getUCDeaths()
	{
		return UCDeaths;
	}
	
	public void increaseDeathCountUC()
	{
		UCDeaths++;
	}
	
	public void cleanUCStats()
	{
		UCDeaths = 0;
		UCKills = 0;
	}
	
	public void setUCState(int state)
	{
		UCState = state;
	}
	
	public int getUCState()
	{
		return UCState;
	}
	
	public long getGamePoints()
	{
		return _gamePoints;
	}
	
	public void setGamePoints(long gamePoints)
	{
		_gamePoints = gamePoints;
	}
	
	public boolean isPKProtected(final L2PcInstance target)
	{
		if ((Config.DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER > 0) && (target != null))
		{
			final L2PcInstance targetPlayer = target;
			
			if (targetPlayer.isInsideZone(ZoneId.SIEGE) || targetPlayer.isInsideZone(ZoneId.PVP) || ((getClan() != null) && (targetPlayer.getClan() != null) && targetPlayer.getClan().isAtWarWith(getClanId())))
			{
				return false;
			}
			
			if ((targetPlayer.getPvpFlag() == 0) && (targetPlayer.getKarma() == 0) && ((targetPlayer.getLevel() + Config.DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER) < getLevel()))
			{
				return true;
			}
		}
		return false;
	}
	
	public void stopAdventBlessingTask()
	{
		if (_adventBlessingTask != null)
		{
			_adventBlessingTask.cancel(false);
			_adventBlessingTask = null;
		}
	}
	
	public void stopAdventBonusTask()
	{
		if (_adventBonusTask != null)
		{
			_adventBonusTask.cancel(false);
			_adventBonusTask = null;
		}
	}
	
	private class AdventPoints implements Runnable
	{
		@Override
		public void run()
		{
			L2PcInstance.this.incAdventPoints(20, true);
		}
	}
	
	@SuppressWarnings("synthetic-access")
	public void startAdventTask()
	{
		if (_adventBonusTask == null)
		{
			int advent_time = NevitHolder.getInstance().getAdventTime(getObjectId());
			if (advent_time < 14400)
			{
				_adventBonusTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AdventPoints(), 60000, 60000);
				sendPacket(new ExNevitAdventTimeChange(getAdventTime(), true));
			}
		}
	}
	
	protected class AdventBlessingEnd implements Runnable
	{
		@Override
		public void run()
		{
			L2PcInstance.this.stopSpecialEffect(AbnormalEffect.AVE_ADVENT_BLESSING);
			L2PcInstance.this.sendPacket(new ExNevitAdventEffect(0));
			L2PcInstance.this.sendPacket(new ExNevitAdventPointInfoPacket(L2PcInstance.this));
			L2PcInstance.this.sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_HAS_ENDED);
			
			_adventBlessingTask = null;
		}
	}
	
	public boolean isAdventBlessingActive()
	{
		return ((_adventBlessingTask != null) && (_adventBlessingTask.getDelay(TimeUnit.MILLISECONDS) > 0));
	}
	
	public int getAdventTime()
	{
		return NevitHolder.getInstance().getAdventTime(getObjectId());
	}
	
	public void incAdventPoints(int value, boolean decreasetime)
	{
		int adventPoints = NevitHolder.getInstance().getAdventPoints(getObjectId());
		int adventTime = NevitHolder.getInstance().getAdventTime(getObjectId());
		
		if (decreasetime)
		{
			adventTime = adventTime + 60;
			if (adventTime >= 14400)
			{
				adventTime = 15000;
				stopAdventBonusTask();
				_adventBonusTask = null;
			}
			NevitHolder.getInstance().setAdventTime(getObjectId(), adventTime, true);
		}
		if (_adventBonusTask != null)
		{
			if ((adventPoints + value) >= 7200)
			{
				adventPoints = 0;
				
				if (!isAdventBlessingActive())
				{
					startAbnormalEffect(AbnormalEffect.AVE_ADVENT_BLESSING);
					_adventBlessingTask = ThreadPoolManager.getInstance().scheduleGeneral(new AdventBlessingEnd(), 180000);
					sendPacket(SystemMessageId.FROM_NOW_ON_ANGEL_NEVIT_ABIDE_WITH_YOU);
					L2PcInstance.this.sendPacket(new ExNevitAdventEffect(180));
				}
			}
			else
			{
				adventPoints = adventPoints + value;
			}
		}
		NevitHolder.getInstance().setAdventPoints(getObjectId(), adventPoints, true);
		sendPacket(new ExNevitAdventPointInfoPacket(this));
		sendPacket(new ExNevitAdventTimeChange(getAdventTime(), _adventBonusTask != null));
	}
	
	public void sendAdventPointMsg()
	{
		int adventPoints = NevitHolder.getInstance().getAdventPoints(getObjectId());
		if (adventPoints >= 5760)
		{
			sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE);
		}
		else if (adventPoints >= 3600)
		{
			sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
		}
		else if (adventPoints >= 1440)
		{
			sendPacket(SystemMessageId.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING);
		}
	}
	
	public void pauseAdventTask()
	{
		stopAdventBonusTask();
		sendPacket(new ExNevitAdventTimeChange(getAdventTime(), false));
	}
	
	public static void midenismos()
	{
		yes = 0;
		no = 0;
	}
	
	public static int getYes()
	{
		return yes;
	}
	
	public static int getNo()
	{
		return no;
	}
	
	public boolean isSurveyer()
	{
		return _isOnSurvey;
	}
	
	public void setSurveyer(boolean test)
	{
		_isOnSurvey = test;
	}
	
	public void Surveystatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET surveystatus=? WHERE charId=?");
			statement.setLong(1, System.currentTimeMillis());
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
		}
	}
	
	public void setSurveystatus(long surveystatus)
	{
		_lastsurvey = System.currentTimeMillis();
	}
	
	public long getSurveystatus()
	{
		return _lastsurvey;
	}
	
	public static void CheckSurveyStatus(L2PcInstance activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT surveystatus FROM characters WHERE charId=?"))
		{
			statement.setInt(1, activeChar.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				lastsurvey = rset.getLong("surveystatus");
			}
		}
		catch (Exception e)
		{
		}
	}
	
	public int getRevision()
	{
		return _client == null ? 0 : _client.getRevision();
	}
	
	public int getKamalokaId()
	{
		return _KamalokaID;
	}
	
	public void setKamalokaId(int instanceId)
	{
		_KamalokaID = instanceId;
	}
	
	public void setIsInMonsterRush(boolean choice)
	{
		_inMonsterRush = choice;
	}
	
	public boolean getIsInMonsterRush()
	{
		return _inMonsterRush;
	}
	
	public void checkPlayer()
	{
		if (isGM() || !Config.SECURITY_SKILL_CHECK || isTransformed() || isInStance())
		{
			return;
		}
		
		boolean haveWrongSkills = false;
		
		for (L2Skill skill : getAllSkills())
		{
			boolean wrongSkill = false;
			for (int skillId : SkillTreesParser.getInstance().getRestrictedSkills(getClassId()))
			{
				if (skill.getId() == skillId)
				{
					wrongSkill = true;
					break;
				}
			}
			if (wrongSkill)
			{
				haveWrongSkills = true;
				if (Config.SECURITY_SKILL_CHECK_CLEAR)
				{
					removeSkill(skill);
				}
				else
				{
					break;
				}
			}
		}
		if (haveWrongSkills)
		{
			if (Config.SECURITY_SKILL_CHECK_CLEAR)
			{
				sendPacket(new SkillList());
			}
			_log.warning("Skills Checking: Possible cheater with wrong skills! Name: " + getName() + " (" + getCharId() + ")" + ", account: " + getAccountName() + ", IP: " + getClient().getConnection().getInetAddress());
			Util.handleIllegalPlayerAction(this, "Possible cheater with wrong skills! Name: " + getName() + " (" + getCharId() + ")" + ", account: " + getAccountName(), Config.SECURITY_SKILL_CHECK_PUNISH);
		}
	}
	
	public AdminProtection getAdminProtection()
	{
		if (_AdminProtection == null)
		{
			_AdminProtection = new AdminProtection(this);
		}
		return _AdminProtection;
	}
	
	public boolean isVoting()
	{
		return _voting;
	}
	
	public void setVoting(boolean voting)
	{
		_voting = voting;
	}
	
	public long getOnlineTime()
	{
		return _onlineTime;
	}
	
	public List<Integer> getCompletedAchievements()
	{
		return _completedAchievements;
	}
	
	public boolean readyAchievementsList()
	{
		return !_completedAchievements.isEmpty();
	}
	
	public void saveAchievemntData()
	{
	}
	
	public void getAchievemntData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(new StringBuilder().append("SELECT * FROM achievements WHERE owner_id=").append(getObjectId()).toString());
			
			ResultSet rs = statement.executeQuery();
			
			String values = "owner_id";
			String in = Integer.toString(getObjectId());
			String questionMarks = in;
			int ilosc = AchievementsManager.getInstance().getAchievementList().size();
			
			if (rs.next())
			{
				_completedAchievements.clear();
				for (int i = 1; i <= ilosc; i++)
				{
					int a = rs.getInt(new StringBuilder().append("a").append(i).toString());
					
					if (_completedAchievements.contains(Integer.valueOf(i)))
					{
						continue;
					}
					if ((a != 1) && (!String.valueOf(a).startsWith("1")))
					{
						continue;
					}
					_completedAchievements.add(Integer.valueOf(i));
				}
			}
			else
			{
				for (int i = 1; i <= ilosc; i++)
				{
					values = new StringBuilder().append(values).append(", a").append(i).toString();
					questionMarks = new StringBuilder().append(questionMarks).append(", 0").toString();
				}
				
				String s = new StringBuilder().append("INSERT INTO achievements(").append(values).append(") VALUES (").append(questionMarks).append(")").toString();
				PreparedStatement insertStatement = con.prepareStatement(s);
				
				insertStatement.execute();
				insertStatement.close();
			}
		}
		catch (SQLException e)
		{
			_log.warning(new StringBuilder().append("[Achievements event loaded data: ]").append(e).toString());
		}
	}
	
	public void saveAchievementData(int achievementID, int objid)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement statement = con.createStatement();
			if ((achievementID == 4) || (achievementID == 6) || (achievementID == 11) || (achievementID == 13))
			{
				statement.executeUpdate(new StringBuilder().append("UPDATE achievements SET a").append(achievementID).append("=1").append(objid).append(" WHERE owner_id=").append(getObjectId()).toString());
			}
			else
			{
				statement.executeUpdate(new StringBuilder().append("UPDATE achievements SET a").append(achievementID).append("=1 WHERE owner_id=").append(getObjectId()).toString());
			}
			statement.close();
			
			if (!_completedAchievements.contains(Integer.valueOf(achievementID)))
			{
				_completedAchievements.add(Integer.valueOf(achievementID));
			}
		}
		catch (SQLException e)
		{
			_log.warning(new StringBuilder().append("[ACHIEVEMENTS SAVE GETDATA]").append(e).toString());
		}
	}
	
	public String getSessionVar(String key)
	{
		if (_userSession == null)
		{
			return null;
		}
		return _userSession.get(key);
	}
	
	public void setSessionVar(String key, String val)
	{
		if (_userSession == null)
		{
			_userSession = new ConcurrentHashMap<>();
		}
		
		if ((val == null) || val.isEmpty())
		{
			_userSession.remove(key);
		}
		else
		{
			_userSession.put(key, val);
		}
	}
	
	@Override
	public boolean isInCategory(CategoryType type)
	{
		return CategoryParser.getInstance().isInCategory(type, getClassId().getId());
	}
	
	@Override
	public int getId()
	{
		return 0;
	}
	
	public void startPcBangPointsTask()
	{
		if (!Config.PC_BANG_ENABLED || (Config.PC_BANG_INTERVAL <= 0))
		{
			return;
		}
		
		if (_pcCafePointsTask == null)
		{
			_pcCafePointsTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PcPointsTask(this), Config.PC_BANG_INTERVAL * 1000, Config.PC_BANG_INTERVAL * 1000);
		}
	}
	
	public void stopPcBangPointsTask()
	{
		if (_pcCafePointsTask != null)
		{
			_pcCafePointsTask.cancel(false);
		}
		_pcCafePointsTask = null;
	}
	
	public boolean isPartyBanned()
	{
		return PunishmentManager.getInstance().hasPunishment(getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN);
	}
	
	public void setCode(StringBuilder finalString)
	{
		code = finalString.toString();
	}
	
	public String getCode()
	{
		return code;
	}
	
	public void setCodeRight(boolean code)
	{
		codeRight = code;
	}
	
	public boolean isCodeRight()
	{
		return codeRight;
	}
	
	public void setKills(int AntiBotKills)
	{
		Kills = AntiBotKills;
	}
	
	public int getKills()
	{
		return Kills;
	}
	
	public void setIsUsingAioMultisell(boolean b)
	{
		_isAioMultisell = b;
	}
	
	public boolean isAioMultisell()
	{
		return _isAioMultisell;
	}
	
	public void setIsUsingAioWh(boolean b)
	{
		_isUsingAioWh = b;
	}
	
	public boolean isUsingAioWh()
	{
		return _isUsingAioWh;
	}
	
	public Location _phantomLoc = null;
	
	public void setPhantomLoc(int x, int y, int z)
	{
		_phantomLoc = new Location(x, y, z);
	}
	
	public Location getPhantomLoc()
	{
		return _phantomLoc;
	}
	
	public static L2PcInstance restorePhantoms(int objectId, int lvl, int classId)
	{
		L2PcInstance player = null;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement statement = con.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM `characters` WHERE `charId`=" + objectId + " LIMIT 1");
			if (rset.next())
			{
				final boolean female = rset.getInt("sex") != Sex.MALE;
				final L2PcTemplate template = CharTemplateParser.getInstance().getTemplate(classId);
				
				byte abc = (byte) Rnd.get(3);
				PcAppearance app = new PcAppearance(abc, abc, abc, female);
				
				player = new L2PcInstance(objectId, template, rset.getString("account_name"), app, true);
				player.setPhantome(true);
				player.setName(rset.getString("char_name"));
				player.setTitle("");
				player.setKarma(rset.getInt("karma"));
				player.setFame(rset.getInt("fame"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNoble(rset.getInt("nobless") == 1);
				player.setCreateDate(Calendar.getInstance());
				player.setBaseClass(classId);
				player.setAccessLevel(0);
				player.setUptime(System.currentTimeMillis());
				
				int clanId = rset.getInt("clanid");
				if (clanId > 0)
				{
					player.setClan(ClanHolder.getInstance().getClan(clanId));
				}
			}
			
			if (player == null)
			{
				return null;
			}
			
			cleanUpPhantomSkills(player.getObjectId());
			
			if (Hero.getInstance().isHero(objectId))
			{
				player.setHero(true);
			}
			
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			
			if (player.hasSummon())
			{
				player.getSummon().setOwner(player);
			}
			
			player.refreshOverloaded();
			player.setPhantomLvl(lvl);
			player.refreshExpertisePenalty();
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "Could not restore char data!", e);
		}
		return player;
	}
	
	public void setPhantomLvl(int level)
	{
		byte lvl = ((byte) level);
		if ((lvl >= 1) && (lvl <= ExperienceParser.getInstance().getMaxLevel()))
		{
			long pXp = getExp();
			long tXp = ExperienceParser.getInstance().getExpForLevel(lvl);
			
			if (pXp > tXp)
			{
				removeExpAndSp(pXp - tXp, 0);
			}
			else if (pXp < tXp)
			{
				addExpAndSp(tXp - pXp, 0);
			}
		}
	}
	
	protected static void cleanUpPhantomSkills(int objectId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(DELETE_PHANTOM_HENNAS);
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement(DELETE_PHANTOM_SHORTCUTS);
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement(DELETE_PHANTOM_SKILL_SAVE);
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement(DELETE_PHANTOM_SKILLS);
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement(DELETE_PHANTOM_SUBCLASS);
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not remove phantom skills: " + e.getMessage(), e);
		}
	}
	
	@Override
	public void teleToClosestTown()
	{
		teleToLocation(TeleportWhereType.TOWN);
	}
}