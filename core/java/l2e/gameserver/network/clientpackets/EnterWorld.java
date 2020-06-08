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
package l2e.gameserver.network.clientpackets;

import l2e.Config;
import l2e.EternityWorld;
import l2e.gameserver.Announcements;
import l2e.gameserver.AutoRestart;
import l2e.gameserver.LoginServerThread;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.communitybbs.Manager.RegionBBSManager;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.CharSchemesHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.AdminParser;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.CoupleManager;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.KrateisCubeManager;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.instancemanager.PetitionManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.SiegeRewardManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.appearance.PcAppearance;
import l2e.gameserver.model.actor.instance.L2ClassMasterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Couple;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.FortSiege;
import l2e.gameserver.model.entity.L2Event;
import l2e.gameserver.model.entity.ProtectionIP;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.entity.clanhall.AuctionableHall;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.entity.events.Hitman;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.restriction.GlobalRestrictions;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.Die;
import l2e.gameserver.network.serverpackets.EtcStatusUpdate;
import l2e.gameserver.network.serverpackets.ExBasicActionList;
import l2e.gameserver.network.serverpackets.ExBrPremiumState;
import l2e.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import l2e.gameserver.network.serverpackets.ExKrateiMatchCCRecord;
import l2e.gameserver.network.serverpackets.ExNevitAdventEffect;
import l2e.gameserver.network.serverpackets.ExNevitAdventPointInfoPacket;
import l2e.gameserver.network.serverpackets.ExNevitAdventTimeChange;
import l2e.gameserver.network.serverpackets.ExNoticePostArrived;
import l2e.gameserver.network.serverpackets.ExNotifyPremiumItem;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.ExShowContactList;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.ExStorageMaxCount;
import l2e.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2e.gameserver.network.serverpackets.FriendList;
import l2e.gameserver.network.serverpackets.HennaInfo;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.PledgeSkillList;
import l2e.gameserver.network.serverpackets.PledgeStatusChanged;
import l2e.gameserver.network.serverpackets.QuestList;
import l2e.gameserver.network.serverpackets.ShortCutInit;
import l2e.gameserver.network.serverpackets.SkillCoolTime;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.protection.Protection;
import l2e.protection.network.ProtectionManager;
import l2e.scripts.services.PremiumNpc;
import l2e.util.Base64;

public class EnterWorld extends L2GameClientPacket
{
	private final int[][] tracert = new int[5][4];
	
	@Override
	protected void readImpl()
	{
		readB(new byte[32]);
		readD();
		readD();
		readD();
		readD();
		readB(new byte[32]);
		readD();
		for (int i = 0; i < 5; i++)
		{
			for (int o = 0; o < 4; o++)
			{
				tracert[i][o] = readC();
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar returned 'null'.");
			getClient().closeNow();
			return;
		}
		
		String[] adress = new String[5];
		for (int i = 0; i < 5; i++)
		{
			adress[i] = tracert[i][0] + "." + tracert[i][1] + "." + tracert[i][2] + "." + tracert[i][3];
		}
		
		LoginServerThread.getInstance().sendClientTracert(activeChar.getAccountName(), adress);
		
		getClient().setClientTracert(tracert);
		
		if (Config.RESTORE_PLAYER_INSTANCE)
		{
			activeChar.setInstanceId(InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId()));
		}
		else
		{
			int instanceId = InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId());
			if (instanceId > 0)
			{
				InstanceManager.getInstance().getInstance(instanceId).removePlayer(activeChar.getObjectId());
			}
		}
		
		if (!activeChar.isGM())
		{
			int pvpAmmount = activeChar.getPvpKills();
			PcAppearance charAppearance = activeChar.getAppearance();
			
			if (Config.PVP_COLOR_SYSTEM)
			{
				if ((pvpAmmount >= Config.PVP_AMMOUNT1) && (pvpAmmount < Config.PVP_AMMOUNT2))
				{
					charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT1);
					charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT1);
				}
				else if ((pvpAmmount >= Config.PVP_AMMOUNT2) && (pvpAmmount < Config.PVP_AMMOUNT3))
				{
					charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT2);
					charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT2);
				}
				else if ((pvpAmmount >= Config.PVP_AMMOUNT3) && (pvpAmmount < Config.PVP_AMMOUNT4))
				{
					charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT3);
					charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT3);
				}
				else if ((pvpAmmount >= Config.PVP_AMMOUNT4) && (pvpAmmount < Config.PVP_AMMOUNT5))
				{
					charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT4);
					charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT4);
				}
				else if (pvpAmmount >= Config.PVP_AMMOUNT5)
				{
					charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT5);
					charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT5);
				}
			}
		}
		
		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
			{
				_log.warning("User already exists in Object ID map! User " + activeChar.getName() + " is a character clone.");
			}
		}
		
		if (activeChar.isGM())
		{
			if (Config.ENABLE_SAFE_ADMIN_PROTECTION)
			{
				if (Config.SAFE_ADMIN_NAMES.contains(activeChar.getName()))
				{
					activeChar.getAdminProtection().setIsSafeAdmin(true);
					if (Config.SAFE_ADMIN_SHOW_ADMIN_ENTER)
					{
						_log.info("Safe Admin: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") has been logged in.");
					}
				}
				else
				{
					activeChar.getAdminProtection().punishUnSafeAdmin();
					_log.warning("WARNING: Unsafe Admin: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") as been logged in.");
					_log.warning("If you have enabled some punishment, He will be punished.");
				}
			}
			
			if (Config.GM_STARTUP_INVULNERABLE && AdminParser.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
			{
				activeChar.setIsInvul(true);
			}
			
			if (Config.GM_STARTUP_INVISIBLE && AdminParser.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel()))
			{
				activeChar.setInvisible(true);
			}
			
			if (Config.GM_STARTUP_SILENCE && AdminParser.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
			{
				activeChar.setSilenceMode(true);
			}
			
			if (Config.GM_STARTUP_DIET_MODE && AdminParser.getInstance().hasAccess("admin_diet", activeChar.getAccessLevel()))
			{
				activeChar.setDietMode(true);
				activeChar.refreshOverloaded();
			}
			
			if (Config.GM_STARTUP_AUTO_LIST && AdminParser.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()))
			{
				AdminParser.getInstance().addGm(activeChar, false);
			}
			else
			{
				AdminParser.getInstance().addGm(activeChar, true);
			}
			
			if (Config.GM_GIVE_SPECIAL_SKILLS)
			{
				SkillTreesParser.getInstance().addSkills(activeChar, false);
			}
			
			if (Config.GM_GIVE_SPECIAL_AURA_SKILLS)
			{
				SkillTreesParser.getInstance().addSkills(activeChar, true);
			}
		}
		
		long plX = activeChar.getX();
		long plY = activeChar.getY();
		long plZ = activeChar.getZ();
		
		if (KrateisCubeManager.getInstance().isRegistered(activeChar))
		{
			activeChar.setIsInKrateisCube(true);
			activeChar.sendPacket(new ExKrateiMatchCCRecord(1, KrateisCubeManager.krateisScore));
		}
		else if (plZ < -8000)
		{
			if (plZ > -8500)
			{
				if (plX > -91326)
				{
					if (plX < -74008)
					{
						if (plY > -91329)
						{
							if (plY < -74231)
							{
								activeChar.teleToLocation(-70381, -70937, -1428);
							}
						}
					}
				}
			}
		}
		
		if (activeChar.getCurrentHp() < 0.5)
		{
			activeChar.setIsDead(true);
		}
		
		boolean showClanNotice = false;
		
		if (activeChar.getClan() != null)
		{
			activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));
			
			notifyClanMembers(activeChar);
			
			notifySponsorOrApprentice(activeChar);
			
			AuctionableHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
			
			if (clanHall != null)
			{
				if (!clanHall.getPaid())
				{
					activeChar.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
				}
			}
			
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(siege.getCastle().getId());
				}
				
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(siege.getCastle().getId());
				}
			}
			
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(siege.getFort().getId());
				}
				
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(siege.getFort().getId());
				}
			}
			
			for (SiegableHall hall : CHSiegeManager.getInstance().getConquerableHalls().values())
			{
				if (!hall.isInSiege())
				{
					continue;
				}
				
				if (hall.isRegistered(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(hall.getId());
					activeChar.setIsInHideoutSiege(true);
				}
			}
			
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));
			
			if (activeChar.getClan().getCastleId() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
			}
			
			if (activeChar.getClan().getFortId() > 0)
			{
				FortManager.getInstance().getFortByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
			}
			
			showClanNotice = activeChar.getClan().isNoticeEnabled();
		}
		
		if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar) > 0)
		{
			if (TerritoryWarManager.getInstance().isTWInProgress())
			{
				activeChar.setSiegeState((byte) 1);
			}
			activeChar.setSiegeSide(TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar));
		}
		
		if (SevenSigns.getInstance().isSealValidationPeriod() && (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) != SevenSigns.CABAL_NULL))
		{
			int cabal = SevenSigns.getInstance().getPlayerCabal(activeChar.getObjectId());
			if (cabal != SevenSigns.CABAL_NULL)
			{
				if (cabal == SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
				{
					activeChar.addSkill(SkillHolder.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
				}
				else
				{
					activeChar.addSkill(SkillHolder.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
				}
			}
		}
		else
		{
			activeChar.removeSkill(SkillHolder.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
			activeChar.removeSkill(SkillHolder.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
		}
		
		if (Config.ENABLE_VITALITY && Config.RECOVER_VITALITY_ON_RECONNECT)
		{
			float points = (Config.RATE_RECOVERY_ON_RECONNECT * (System.currentTimeMillis() - activeChar.getLastAccess())) / 60000;
			if (points > 0)
			{
				activeChar.updateVitalityPoints(points, false, true);
			}
		}
		
		activeChar.checkRecoBonusTask();
		
		activeChar.broadcastUserInfo();
		
		// Send Macro List
		activeChar.getMacros().sendUpdate();
		
		// Send Item List
		sendPacket(new ItemList(activeChar, false));
		
		// Send Premium Info
		if (activeChar.getPremiumService() == 1)
		{
			activeChar.sendPacket(new ExBrPremiumState(activeChar.getObjectId(), 1));
		}
		else
		{
			activeChar.sendPacket(new ExBrPremiumState(activeChar.getObjectId(), 0));
		}
		activeChar.checkPlayer();
		
		// Send GG check
		activeChar.queryGameGuard();
		
		// Send Teleport Bookmark List
		sendPacket(new ExGetBookMarkInfoPacket(activeChar));
		
		// Send Shortcuts
		sendPacket(new ShortCutInit(activeChar));
		
		// Send Action list
		activeChar.sendPacket(ExBasicActionList.STATIC_PACKET);
		
		// Send Skill list
		activeChar.sendSkillList();
		
		// Send Dye Information
		activeChar.sendPacket(new HennaInfo(activeChar));
		
		Quest.playerEnter(activeChar);
		
		if (!Config.DISABLE_TUTORIAL)
		{
			loadTutorial(activeChar);
		}
		
		for (Quest quest : QuestManager.getInstance().getAllManagedScripts())
		{
			if ((quest != null) && quest.getOnEnterWorld())
			{
				quest.notifyEnterWorld(activeChar);
			}
		}
		activeChar.sendPacket(new QuestList());
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}
		
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		activeChar.getInventory().applyItemSkills();
		
		if (L2Event.isParticipant(activeChar))
		{
			L2Event.restorePlayerEventStatus(activeChar);
		}
		
		// Wedding Checks
		if (Config.ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar, activeChar.getPartnerId());
		}
		
		if (activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquippedId()).cursedOnLogin();
		}
		
		activeChar.updateEffectIcons();
		
		if (Config.PC_BANG_ENABLED)
		{
			if (activeChar.getPcBangPoints() > 0)
			{
				activeChar.sendPacket(new ExPCCafePointInfo(activeChar.getPcBangPoints(), 0, false, false, 1));
			}
			else
			{
				activeChar.sendPacket(new ExPCCafePointInfo());
			}
		}
		
		activeChar.startTimers();
		
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
		// Expand Skill
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		
		sendPacket(new FriendList(activeChar));
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addString(activeChar.getName());
		for (int id : activeChar.getFriendList())
		{
			L2Object obj = L2World.getInstance().findObject(id);
			if (obj != null)
			{
				obj.sendPacket(sm);
			}
		}
		
		activeChar.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		
		if (Config.NEW_CHAR_IS_HERO)
		{
			activeChar.setHero(true);
		}
		
		if (Config.DISPLAY_SERVER_VERSION)
		{
			activeChar.sendMessage(getText("TDJKIEV0ZXJuaXR5LVdvcmxkIFNlcnZlciBWZXJzaW9uOg==") + "  " + EternityWorld._revision);
			activeChar.sendMessage(getText("Q29weXJpZ2h0IMKpIDIwMTAgLSAyMDE0Cg=="));
		}
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);
		
		if (Config.ALLOW_COMMUNITY_BUFF)
		{
			CharSchemesHolder.getInstance().onPlayerLogin(activeChar.getObjectId());
		}
		
		if (Config.AUTO_RESTART_ENABLE)
		{
			CreatureSay msg3 = new CreatureSay(0, Say2.BATTLEFIELD, "[Auto Restart]", "Next auto restart at " + AutoRestart.getInstance().getRestartNextTime() + " hs.");
			activeChar.sendPacket(msg3);
		}
		
		if (showClanNotice)
		{
			NpcHtmlMessage notice = new NpcHtmlMessage(1);
			notice.setFile(activeChar.getLang(), "data/html/clanNotice.htm");
			notice.replace("%clan_name%", activeChar.getClan().getName());
			notice.replace("%notice_text%", activeChar.getClan().getNotice());
			notice.disableValidation();
			sendPacket(notice);
		}
		else if (Config.SERVER_NEWS)
		{
			String serverNews = HtmCache.getInstance().getHtm(activeChar.getLang(), "data/html/servnews.htm");
			if (serverNews != null)
			{
				sendPacket(new NpcHtmlMessage(1, serverNews));
			}
		}
		
		if (Config.PETITIONING_ALLOWED)
		{
			PetitionManager.getInstance().checkPetitionMessages(activeChar);
		}
		
		// Add by LordWinter
		if (Config.ONLINE_PLAYERS_AT_STARTUP)
		{
			activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "OnlinePlayers.ONLINE") + " " + L2World.getInstance().getAllPlayers().size());
		}
		
		if (activeChar.isAlikeDead())
		{
			sendPacket(new Die(activeChar));
		}
		
		sendPacket(new SkillCoolTime(activeChar));
		sendPacket(new ExVoteSystemInfo(activeChar));
		sendPacket(new ExNevitAdventEffect(0));
		sendPacket(new ExNevitAdventPointInfoPacket(activeChar));
		sendPacket(new ExNevitAdventTimeChange(activeChar.getAdventTime(), false));
		sendPacket(new ExShowContactList(activeChar));
		
		activeChar.onPlayerEnter();
		
		for (L2ItemInstance i : activeChar.getInventory().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
			if (i.isShadowItem() && i.isEquipped())
			{
				i.decreaseMana(false);
			}
		}
		
		for (L2ItemInstance i : activeChar.getWarehouse().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
		}
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		}
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		}
		
		if (SiegeRewardManager.ACTIVATED_SYSTEM && !SiegeRewardManager.REWARD_ACTIVE_MEMBERS_ONLY)
		{
			SiegeRewardManager.getInstance().processWorldEnter(activeChar);
		}
		
		if (activeChar.getInventory().getItemByItemId(9819) != null)
		{
			Fort fort = FortManager.getInstance().getFort(activeChar);
			
			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getId());
			}
			else
			{
				int slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
				activeChar.getInventory().unEquipItemInBodySlot(slot);
				activeChar.destroyItem("CombatFlag", activeChar.getInventory().getItemByItemId(9819), null, true);
			}
		}
		
		if (!activeChar.canOverrideCond(PcCondOverride.ZONE_CONDITIONS) && activeChar.isInsideZone(ZoneId.SIEGE) && (!activeChar.isInSiege() || (activeChar.getSiegeState() < 2)))
		{
			activeChar.teleToLocation(TeleportWhereType.TOWN);
		}
		
		if (Config.ALLOW_MAIL)
		{
			if (MailManager.getInstance().hasUnreadPost(activeChar))
			{
				sendPacket(ExNoticePostArrived.valueOf(false));
			}
		}
		
		RegionBBSManager.getInstance().changeCommunityBoard(activeChar);
		
		ProtectionIP.onEnterWorld(activeChar);
		
		TvTEvent.onLogin(activeChar);
		TvTRoundEvent.onLogin(activeChar);
		
		if (Config.WELCOME_MESSAGE_ENABLED)
		{
			activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_MESSAGE_TEXT, Config.WELCOME_MESSAGE_TIME));
		}
		
		L2ClassMasterInstance.showQuestionMark(activeChar);
		
		int birthday = activeChar.checkBirthDay();
		if (birthday == 0)
		{
			activeChar.sendPacket(SystemMessageId.YOUR_BIRTHDAY_GIFT_HAS_ARRIVED);
		}
		else if (birthday != -1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY);
			sm.addString(Integer.toString(birthday));
			activeChar.sendPacket(sm);
		}
		
		if (!activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(ExNotifyPremiumItem.STATIC_PACKET);
		}
		
		GlobalRestrictions.playerLoggedIn(activeChar);
		
		if (Config.HITMAN_ENABLE_EVENT)
		{
			Hitman.getInstance().onEnterWorld(activeChar);
		}
		
		if (Protection.isProtectionOn())
		{
			ProtectionManager.SendSpecialSting(getClient());
		}
		
		if (Config.AUTO_GIVE_PREMIUM)
		{
			if (activeChar.getPremiumService() != 1)
			{
				String var = GlobalVariablesManager.getInstance().getStoredVariable("Premium-" + activeChar.getAccountName());
				if (var == null)
				{
					PremiumNpc.getPS(activeChar);
					PremiumNpc.addPremiumServices(Config.GIVE_PREMIUM_DAYS, activeChar);
					GlobalVariablesManager.getInstance().storeVariable("Premium-" + activeChar.getAccountName(), String.valueOf(activeChar.getName()));
					if (Config.NOTICE_PREMIUM_MESSAGE)
					{
						CustomMessage msg = new CustomMessage("Premium.NOTICE_MESSAGE", activeChar.getLang());
						msg.add(Config.GIVE_PREMIUM_DAYS);
						activeChar.sendPacket(new ExShowScreenMessage(msg.toString(), 10000));
					}
				}
			}
		}
	}
	
	private void engage(L2PcInstance cha)
	{
		int chaId = cha.getObjectId();
		
		for (Couple cl : CoupleManager.getInstance().getCouples())
		{
			if ((cl.getPlayer1Id() == chaId) || (cl.getPlayer2Id() == chaId))
			{
				if (cl.getMaried())
				{
					cha.setMarried(true);
				}
				
				cha.setCoupleId(cl.getId());
				
				if (cl.getPlayer1Id() == chaId)
				{
					cha.setPartnerId(cl.getPlayer2Id());
				}
				else
				{
					cha.setPartnerId(cl.getPlayer1Id());
				}
			}
		}
	}
	
	private void notifyPartner(L2PcInstance cha, int partnerId)
	{
		int objId = cha.getPartnerId();
		if (objId != 0)
		{
			final L2PcInstance partner = L2World.getInstance().getPlayer(objId);
			if (partner != null)
			{
				partner.sendMessage("Your Partner has logged in.");
			}
		}
	}
	
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		final L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}
	
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			final L2PcInstance sponsor = L2World.getInstance().getPlayer(activeChar.getSponsor());
			if (sponsor != null)
			{
				final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			final L2PcInstance apprentice = L2World.getInstance().getPlayer(activeChar.getApprentice());
			if (apprentice != null)
			{
				final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}
	
	private String getText(String string)
	{
		return new String(Base64.decode(string));
	}
	
	private void loadTutorial(L2PcInstance player)
	{
		final QuestState qs = player.getQuestState("_255_Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}