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

import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import l2e.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.xml.CategoryParser;
import l2e.gameserver.handler.BypassHandler;
import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.L2NpcAIData;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import l2e.gameserver.model.actor.instance.L2DoormenInstance;
import l2e.gameserver.model.actor.instance.L2FestivalGuideInstance;
import l2e.gameserver.model.actor.instance.L2FishermanInstance;
import l2e.gameserver.model.actor.instance.L2MerchantInstance;
import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2TeleporterInstance;
import l2e.gameserver.model.actor.instance.L2TrainerInstance;
import l2e.gameserver.model.actor.instance.L2WarehouseInstance;
import l2e.gameserver.model.actor.knownlist.NpcKnownList;
import l2e.gameserver.model.actor.stat.NpcStat;
import l2e.gameserver.model.actor.status.NpcStatus;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.actor.templates.L2NpcTemplate.AIType;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.entity.events.MonsterRush;
import l2e.gameserver.model.entity.events.phoenix.Interface;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.Quest.QuestEventType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.variables.NpcVariables;
import l2e.gameserver.model.zone.type.L2TownZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ExChangeNpcState;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.ServerObjectInfo;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.taskmanager.DecayTaskManager;
import l2e.gameserver.util.Broadcast;
import l2e.util.Rnd;

public class L2Npc extends L2Character
{
	public static final int INTERACTION_DISTANCE = 150;
	private L2Spawn _spawn;
	private boolean _isBusy = false;
	private String _busyMessage = "";
	private volatile boolean _isDecayed = false;
	private int _castleIndex = -2;
	private int _fortIndex = -2;
	
	private boolean _isNoAnimation = false;
	private boolean _isRandomAnimationEnabled = true;
	
	private boolean _eventMob = false;
	private boolean _isInTown = false;
	private boolean _isAutoAttackable = false;
	private boolean _isSevenSignsMonster = false;
	private boolean _isRunner = false;
	private boolean _isSpecialCamera = false;
	private boolean _isEkimusFood = false;
	
	private long _lastSocialBroadcast = 0;
	
	private final int _minimalSocialInterval = 6000;
	
	protected RandomAnimationTask _rAniTask = null;
	private int _currentLHandId;
	private int _currentRHandId;
	private int _currentEnchant;
	private double _currentCollisionHeight;
	private double _currentCollisionRadius;
	
	private int _soulshotamount = 0;
	private int _spiritshotamount = 0;
	private int _displayEffect = 0;
	
	private L2Character _summoner = null;
	
	private final L2NpcAIData _staticAIData = getTemplate().getAIDataStatic();
	
	private int _shotsMask = 0;
	
	public int getSoulShotChance()
	{
		return _staticAIData.getSoulShotChance();
	}
	
	public int getSpiritShotChance()
	{
		return _staticAIData.getSpiritShotChance();
	}
	
	public int getEnemyRange()
	{
		return _staticAIData.getEnemyRange();
	}
	
	public String getEnemyClan()
	{
		return _staticAIData.getEnemyClan();
	}
	
	public int getClanRange()
	{
		return _staticAIData.getClanRange();
	}
	
	public String getClan()
	{
		return _staticAIData.getClan();
	}
	
	public int getPrimarySkillId()
	{
		return _staticAIData.getPrimarySkillId();
	}
	
	public int getMinSkillChance()
	{
		return _staticAIData.getMinSkillChance();
	}
	
	public int getMaxSkillChance()
	{
		return _staticAIData.getMaxSkillChance();
	}
	
	public int getCanMove()
	{
		return _staticAIData.getCanMove();
	}
	
	public int getIsChaos()
	{
		return _staticAIData.getIsChaos();
	}
	
	public int getCanDodge()
	{
		return _staticAIData.getDodge();
	}
	
	public int getSSkillChance()
	{
		return _staticAIData.getShortRangeChance();
	}
	
	public int getLSkillChance()
	{
		return _staticAIData.getLongRangeChance();
	}
	
	public int getSwitchRangeChance()
	{
		return _staticAIData.getSwitchRangeChance();
	}
	
	public boolean hasLSkill()
	{
		if (_staticAIData.getLongRangeSkill() == 0)
		{
			return false;
		}
		return true;
	}
	
	public boolean hasSSkill()
	{
		if (_staticAIData.getShortRangeSkill() == 0)
		{
			return false;
		}
		return true;
	}
	
	public List<L2Skill> getLongRangeSkill()
	{
		final List<L2Skill> skilldata = new ArrayList<>();
		if ((_staticAIData == null) || (_staticAIData.getLongRangeSkill() == 0))
		{
			return skilldata;
		}
		
		switch (_staticAIData.getLongRangeSkill())
		{
			case -1:
			{
				Collection<L2Skill> skills = getAllSkills();
				
				if (skills != null)
				{
					for (L2Skill sk : skills)
					{
						if ((sk == null) || sk.isPassive() || (sk.getTargetType() == L2TargetType.SELF))
						{
							continue;
						}
						
						if (sk.getCastRange() >= 200)
						{
							skilldata.add(sk);
						}
					}
				}
				break;
			}
			case 1:
			{
				if (getTemplate().getUniversalSkills() != null)
				{
					for (L2Skill sk : getTemplate().getUniversalSkills())
					{
						if (sk.getCastRange() >= 200)
						{
							skilldata.add(sk);
						}
					}
				}
				break;
			}
			default:
			{
				for (L2Skill sk : getAllSkills())
				{
					if (sk.getId() == _staticAIData.getLongRangeSkill())
					{
						skilldata.add(sk);
					}
				}
			}
		}
		return skilldata;
	}
	
	public List<L2Skill> getShortRangeSkill()
	{
		final List<L2Skill> skilldata = new ArrayList<>();
		if ((_staticAIData == null) || (_staticAIData.getShortRangeSkill() == 0))
		{
			return skilldata;
		}
		
		switch (_staticAIData.getShortRangeSkill())
		{
			case -1:
			{
				Collection<L2Skill> skills = getAllSkills();
				
				if (skills != null)
				{
					for (L2Skill sk : skills)
					{
						if ((sk == null) || sk.isPassive() || (sk.getTargetType() == L2TargetType.SELF))
						{
							continue;
						}
						if (sk.getCastRange() <= 200)
						{
							skilldata.add(sk);
						}
					}
				}
				break;
			}
			case 1:
			{
				if (getTemplate().getUniversalSkills() != null)
				{
					for (L2Skill sk : getTemplate().getUniversalSkills())
					{
						if (sk.getCastRange() <= 200)
						{
							skilldata.add(sk);
						}
					}
				}
				break;
			}
			default:
			{
				for (L2Skill sk : getAllSkills())
				{
					if (sk.getId() == _staticAIData.getShortRangeSkill())
					{
						skilldata.add(sk);
					}
				}
			}
		}
		return skilldata;
	}
	
	protected class RandomAnimationTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (this != _rAniTask)
				{
					return;
				}
				if (isMob())
				{
					if (getAI().getIntention() != AI_INTENTION_ACTIVE)
					{
						return;
					}
				}
				else
				{
					if (!isInActiveRegion())
					{
						return;
					}
				}
				
				if (!(isDead() || isStunned() || isSleeping() || isParalyzed()))
				{
					onRandomAnimation(Rnd.get(2, 3));
				}
				
				startRandomAnimationTimer();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	public void onRandomAnimation(int animationId)
	{
		long now = System.currentTimeMillis();
		if ((now - _lastSocialBroadcast) > _minimalSocialInterval)
		{
			_lastSocialBroadcast = now;
			broadcastPacket(new SocialAction(getObjectId(), animationId));
		}
	}
	
	public void startRandomAnimationTimer()
	{
		if (!hasRandomAnimation())
		{
			return;
		}
		
		int minWait = isMob() ? Config.MIN_MONSTER_ANIMATION : Config.MIN_NPC_ANIMATION;
		int maxWait = isMob() ? Config.MAX_MONSTER_ANIMATION : Config.MAX_NPC_ANIMATION;
		
		int interval = Rnd.get(minWait, maxWait) * 1000;
		_rAniTask = new RandomAnimationTask();
		ThreadPoolManager.getInstance().scheduleGeneral(_rAniTask, interval);
	}
	
	public boolean hasRandomAnimation()
	{
		return (((Config.MAX_NPC_ANIMATION > 0) && _isRandomAnimationEnabled && !getAiType().equals(AIType.CORPSE)) || !(getActingNpc().getId() == 29119));
	}
	
	public L2Npc(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Npc);
		initCharStatusUpdateValues();
		
		_currentLHandId = getTemplate().getLeftHand();
		_currentRHandId = getTemplate().getRightHand();
		_currentEnchant = Config.ENABLE_RANDOM_ENCHANT_EFFECT ? Rnd.get(4, 21) : getTemplate().getEnchantEffect();
		_currentCollisionHeight = getTemplate().getfCollisionHeight();
		_currentCollisionRadius = getTemplate().getfCollisionRadius();
		
		if (template == null)
		{
			_log.severe("No template for Npc. Please check your datapack is setup correctly.");
			return;
		}
		setName(template.getName());
	}
	
	@Override
	public NpcKnownList getKnownList()
	{
		return (NpcKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new NpcKnownList(this));
	}
	
	@Override
	public NpcStat getStat()
	{
		return (NpcStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new NpcStat(this));
	}
	
	@Override
	public NpcStatus getStatus()
	{
		return (NpcStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new NpcStatus(this));
	}
	
	@Override
	public final L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	@Override
	public int getId()
	{
		return getTemplate().getId();
	}
	
	@Override
	public boolean isAttackable()
	{
		return Config.ALT_ATTACKABLE_NPCS;
	}
	
	public final String getFactionId()
	{
		return getClan();
	}
	
	@Override
	public final int getLevel()
	{
		return getTemplate().getLevel();
	}
	
	public boolean isAggressive()
	{
		return false;
	}
	
	public int getAggroRange()
	{
		return _staticAIData.getAggroRange();
	}
	
	public int getFactionRange()
	{
		return getClanRange();
	}
	
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if ((player == null) || !isVisibleFor(player))
			{
				continue;
			}
			if (getRunSpeed() == 0)
			{
				player.sendPacket(new ServerObjectInfo(this, player));
			}
			else
			{
				player.sendPacket(new AbstractNpcInfo.NpcInfo(this, player));
			}
		}
	}
	
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2FestivalGuideInstance)
		{
			return 10000;
		}
		
		if ((object instanceof L2NpcInstance) || !(object instanceof L2Character))
		{
			return 0;
		}
		
		if (object instanceof L2Playable)
		{
			return 1500; //
		}
		return 500;
	}
	
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
	
	public boolean isEventMob()
	{
		return _eventMob;
	}
	
	public void setEventMob(boolean val)
	{
		_eventMob = val;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _isAutoAttackable;
	}
	
	public void setAutoAttackable(boolean flag)
	{
		_isAutoAttackable = flag;
	}
	
	public int getLeftHandItem()
	{
		return _currentLHandId;
	}
	
	public int getRightHandItem()
	{
		return _currentRHandId;
	}
	
	public int getEnchantEffect()
	{
		return _currentEnchant;
	}
	
	public final boolean isBusy()
	{
		return _isBusy;
	}
	
	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}
	
	public final String getBusyMessage()
	{
		return _busyMessage;
	}
	
	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}
	
	public boolean isWarehouse()
	{
		return false;
	}
	
	public boolean canTarget(L2PcInstance player)
	{
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		if (player.isLockedTarget() && (player.getLockedTarget() != this))
		{
			player.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		return true;
	}
	
	public boolean canInteract(L2PcInstance player)
	{
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
		{
			return false;
		}
		if (player.isDead() || player.isFakeDeath())
		{
			return false;
		}
		if (player.isSitting())
		{
			return false;
		}
		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			return false;
		}
		if (!isInsideRadius(player, INTERACTION_DISTANCE, true, false))
		{
			return false;
		}
		if ((player.getInstanceId() != getInstanceId()) && (player.getInstanceId() != -1))
		{
			return false;
		}
		if (isBusy())
		{
			return false;
		}
		return true;
	}
	
	public final Castle getCastle()
	{
		if (_castleIndex < 0)
		{
			L2TownZone town = TownManager.getTown(getX(), getY(), getZ());
			
			if (town != null)
			{
				_castleIndex = CastleManager.getInstance().getCastleIndex(town.getTaxById());
			}
			
			if (_castleIndex < 0)
			{
				_castleIndex = CastleManager.getInstance().findNearestCastleIndex(this);
			}
			else
			{
				_isInTown = true;
			}
		}
		
		if (_castleIndex < 0)
		{
			return null;
		}
		
		return CastleManager.getInstance().getCastles().get(_castleIndex);
	}
	
	public boolean isMyLord(L2PcInstance player)
	{
		if (player.isClanLeader())
		{
			final int castleId = getCastle() != null ? getCastle().getId() : -1;
			final int fortId = getFort() != null ? getFort().getId() : -1;
			return (player.getClan().getCastleId() == castleId) || (player.getClan().getFortId() == fortId);
		}
		return false;
	}
	
	public final SiegableHall getConquerableHall()
	{
		return CHSiegeManager.getInstance().getNearbyClanHall(getX(), getY(), 10000);
	}
	
	public final Castle getCastle(long maxDistance)
	{
		int index = CastleManager.getInstance().findNearestCastleIndex(this, maxDistance);
		
		if (index < 0)
		{
			return null;
		}
		
		return CastleManager.getInstance().getCastles().get(index);
	}
	
	public final Fort getFort()
	{
		if (_fortIndex < 0)
		{
			Fort fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
			if (fort != null)
			{
				_fortIndex = FortManager.getInstance().getFortIndex(fort.getId());
			}
			
			if (_fortIndex < 0)
			{
				_fortIndex = FortManager.getInstance().findNearestFortIndex(this);
			}
		}
		
		if (_fortIndex < 0)
		{
			return null;
		}
		
		return FortManager.getInstance().getForts().get(_fortIndex);
	}
	
	public final Fort getFort(long maxDistance)
	{
		int index = FortManager.getInstance().findNearestFortIndex(this, maxDistance);
		
		if (index < 0)
		{
			return null;
		}
		
		return FortManager.getInstance().getForts().get(index);
	}
	
	public final boolean getIsInTown()
	{
		if (_castleIndex < 0)
		{
			getCastle();
		}
		
		return _isInTown;
	}
	
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		{
			if (isBusy() && (getBusyMessage().length() > 0))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getLang(), "data/html/npcbusy.htm");
				html.replace("%busymessage%", getBusyMessage());
				html.replace("%npcname%", getName());
				html.replace("%playername%", player.getName());
				player.sendPacket(html);
			}
			else
			{
				IBypassHandler handler = BypassHandler.getInstance().getHandler(command);
				if (handler != null)
				{
					handler.useBypass(command, player, this);
				}
				else
				{
					_log.info(getClass().getSimpleName() + ": Unknown NPC bypass: \"" + command + "\" NpcId: " + getId());
				}
			}
		}
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		int weaponId = getTemplate().getRightHand();
		
		if (weaponId < 1)
		{
			return null;
		}
		
		L2Item item = ItemHolder.getInstance().getTemplate(getTemplate().getRightHand());
		
		if (!(item instanceof L2Weapon))
		{
			return null;
		}
		
		return (L2Weapon) item;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		int weaponId = getTemplate().getLeftHand();
		
		if (weaponId < 1)
		{
			return null;
		}
		
		L2Item item = ItemHolder.getInstance().getTemplate(getTemplate().getLeftHand());
		
		if (!(item instanceof L2Weapon))
		{
			return null;
		}
		
		return (L2Weapon) item;
	}
	
	public void insertObjectIdAndShowChatWindow(L2PcInstance player, String content)
	{
		content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
		NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}
	
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		String temp = "data/html/default/" + pom + ".htm";
		
		if (!Config.LAZY_CACHE)
		{
			if (HtmCache.getInstance().contains(temp))
			{
				return temp;
			}
		}
		else
		{
			if (HtmCache.getInstance().isLoadable(temp))
			{
				return temp;
			}
		}
		return "data/html/npcdefault.htm";
	}
	
	public void showChatWindow(L2PcInstance player)
	{
		showChatWindow(player, 0);
	}
	
	private boolean showPkDenyChatWindow(L2PcInstance player, String type)
	{
		String html = HtmCache.getInstance().getHtm(player.getLang(), "data/html/" + type + "/" + getId() + "-pk.htm");
		
		if (html != null)
		{
			NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId());
			pkDenyMsg.setHtml(html);
			player.sendPacket(pkDenyMsg);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		return false;
	}
	
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (Config.NON_TALKING_NPCS.contains(getId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isCursedWeaponEquipped() && (!(player.getTarget() instanceof L2ClanHallManagerInstance) || !(player.getTarget() instanceof L2DoormenInstance)))
		{
			player.setTarget(player);
			return;
		}
		if (player.getKarma() > 0)
		{
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (this instanceof L2MerchantInstance))
			{
				if (showPkDenyChatWindow(player, "merchant"))
				{
					return;
				}
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (this instanceof L2TeleporterInstance))
			{
				if (showPkDenyChatWindow(player, "teleporter"))
				{
					return;
				}
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (this instanceof L2WarehouseInstance))
			{
				if (showPkDenyChatWindow(player, "warehouse"))
				{
					return;
				}
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (this instanceof L2FishermanInstance))
			{
				if (showPkDenyChatWindow(player, "fisherman"))
				{
					return;
				}
			}
		}
		
		if (getTemplate().isType("L2Auctioneer") && (val == 0))
		{
			return;
		}
		
		int npcId = getTemplate().getId();
		
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS);
		int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		switch (npcId)
		{
			case 31127:
			case 31128:
			case 31129:
			case 31130:
			case 31131:
				filename += "festival/dawn_guide.htm";
				break;
			case 31137:
			case 31138:
			case 31139:
			case 31140:
			case 31141:
				filename += "festival/dusk_guide.htm";
				break;
			case 31092:
				filename += "blkmrkt_1.htm";
				break;
			case 31113:
				if (Config.ALT_STRICT_SEVENSIGNS)
				{
					switch (compWinner)
					{
						case SevenSigns.CABAL_DAWN:
							if ((playerCabal != compWinner) || (playerCabal != sealAvariceOwner))
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
							break;
						case SevenSigns.CABAL_DUSK:
							if ((playerCabal != compWinner) || (playerCabal != sealAvariceOwner))
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
							break;
						default:
							player.sendPacket(SystemMessageId.SSQ_COMPETITION_UNDERWAY);
							return;
					}
				}
				filename += "mammmerch_1.htm";
				break;
			case 31126:
				if (Config.ALT_STRICT_SEVENSIGNS)
				{
					switch (compWinner)
					{
						case SevenSigns.CABAL_DAWN:
							if ((playerCabal != compWinner) || (playerCabal != sealGnosisOwner))
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
							break;
						case SevenSigns.CABAL_DUSK:
							if ((playerCabal != compWinner) || (playerCabal != sealGnosisOwner))
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
							break;
						default:
							player.sendPacket(SystemMessageId.SSQ_COMPETITION_UNDERWAY);
							return;
					}
				}
				filename += "mammblack_1.htm";
				break;
			case 31132:
			case 31133:
			case 31134:
			case 31135:
			case 31136:
			case 31142:
			case 31143:
			case 31144:
			case 31145:
			case 31146:
				filename += "festival/festival_witch.htm";
				break;
			case 31688:
				if (player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
				}
				else
				{
					filename = (getHtmlPath(npcId, val));
				}
				break;
			case 31690:
			case 31769:
			case 31770:
			case 31771:
			case 31772:
				if (player.isHero() || player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
				}
				else
				{
					filename = (getHtmlPath(npcId, val));
				}
				break;
			case 36402:
				if (player.olyBuff > 0)
				{
					filename = (player.olyBuff == 5 ? Olympiad.OLYMPIAD_HTML_PATH + "olympiad_buffs.htm" : Olympiad.OLYMPIAD_HTML_PATH + "olympiad_5buffs.htm");
				}
				else
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "olympiad_nobuffs.htm";
				}
				break;
			case 30298:
				if (player.isAcademyMember())
				{
					filename = (getHtmlPath(npcId, 1));
				}
				else
				{
					filename = (getHtmlPath(npcId, val));
				}
				break;
			default:
				if ((npcId >= 31865) && (npcId <= 31918))
				{
					if (val == 0)
					{
						filename += "rift/GuardianOfBorder.htm";
					}
					else
					{
						filename += "rift/GuardianOfBorder-" + val + ".htm";
					}
					break;
				}
				if (((npcId >= 31093) && (npcId <= 31094)) || ((npcId >= 31172) && (npcId <= 31201)) || ((npcId >= 31239) && (npcId <= 31254)))
				{
					return;
				}
				
				filename = (getHtmlPath(npcId, val));
				break;
		}
		
		if (Interface.talkNpc(player.getObjectId(), getObjectId()))
		{
			return;
		}
		
		if (npcId == Interface.getInt("managerNpcId", 0))
		{
			Interface.showFirstHtml(player.getObjectId(), getObjectId());
			return;
		}
		
		if (Interface.isParticipating(player.getObjectId()))
		{
			if (Interface.onTalkNpc(getObjectId(), player.getObjectId()))
			{
				return;
			}
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getLang(), filename);
		
		if (this instanceof L2MerchantInstance)
		{
			if (Config.LIST_PET_RENT_NPC.contains(npcId))
			{
				html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
			}
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void showChatWindow(L2PcInstance player, String filename)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public int getExpReward(int isPremium)
	{
		if (isPremium == 1)
		{
			if (Config.ALLOW_CUSTOM_RATES)
			{
				if (isMonday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_XP * Config.MONDAY_RATE_EXP);
				}
				else if (isTuesday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_XP * Config.TUESDAY_RATE_EXP);
				}
				else if (isWednesday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_XP * Config.WEDNESDAY_RATE_EXP);
				}
				else if (isThursday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_XP * Config.THURSDAY_RATE_EXP);
				}
				else if (isFriday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_XP * Config.FRIDAY_RATE_EXP);
				}
				else if (isSaturday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_XP * Config.SATURDAY_RATE_EXP);
				}
				else if (isSunday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_XP * Config.SUNDAY_RATE_EXP);
				}
			}
			return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_XP);
		}
		
		if (Config.ALLOW_CUSTOM_RATES)
		{
			if (isMonday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.MONDAY_RATE_EXP);
			}
			else if (isTuesday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.TUESDAY_RATE_EXP);
			}
			else if (isWednesday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.WEDNESDAY_RATE_EXP);
			}
			else if (isThursday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.THURSDAY_RATE_EXP);
			}
			else if (isFriday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.FRIDAY_RATE_EXP);
			}
			else if (isSaturday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.SATURDAY_RATE_EXP);
			}
			else if (isSunday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.SUNDAY_RATE_EXP);
			}
		}
		return (int) (getTemplate().getRewardExp() * Config.RATE_XP);
	}
	
	public int getSpReward(int isPremium)
	{
		if (isPremium == 1)
		{
			if (Config.ALLOW_CUSTOM_RATES)
			{
				if (isMonday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_SP * Config.MONDAY_RATE_SP);
				}
				else if (isTuesday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_SP * Config.TUESDAY_RATE_SP);
				}
				else if (isWednesday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_SP * Config.WEDNESDAY_RATE_SP);
				}
				else if (isThursday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_SP * Config.THURSDAY_RATE_SP);
				}
				else if (isFriday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_SP * Config.FRIDAY_RATE_SP);
				}
				else if (isSaturday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_SP * Config.SATURDAY_RATE_SP);
				}
				else if (isSunday() != 0)
				{
					return (int) (getTemplate().getRewardExp() * Config.PREMIUM_RATE_SP * Config.SUNDAY_RATE_SP);
				}
			}
			return (int) (getTemplate().getRewardSp() * Config.PREMIUM_RATE_SP);
		}
		
		if (Config.ALLOW_CUSTOM_RATES)
		{
			if (isMonday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.MONDAY_RATE_SP);
			}
			else if (isTuesday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.TUESDAY_RATE_SP);
			}
			else if (isWednesday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.WEDNESDAY_RATE_SP);
			}
			else if (isThursday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.THURSDAY_RATE_SP);
			}
			else if (isFriday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.FRIDAY_RATE_SP);
			}
			else if (isSaturday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.SATURDAY_RATE_SP);
			}
			else if (isSunday() != 0)
			{
				return (int) (getTemplate().getRewardExp() * Config.SUNDAY_RATE_SP);
			}
		}
		return (int) (getTemplate().getRewardSp() * Config.RATE_SP);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (getTemplate()._npcId == 40030)
		{
			deleteMe();
			MonsterRush.endByLordDeath();
		}
		
		_currentLHandId = getTemplate().getLeftHand();
		_currentRHandId = getTemplate().getRightHand();
		_currentCollisionHeight = getTemplate().getfCollisionHeight();
		_currentCollisionRadius = getTemplate().getfCollisionRadius();
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	public void setSpawn(L2Spawn spawn)
	{
		_spawn = spawn;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		_soulshotamount = getTemplate().getAIDataStatic().getSoulShot();
		_spiritshotamount = getTemplate().getAIDataStatic().getSpiritShot();
		
		if (getTemplate().getEventQuests(QuestEventType.ON_SPAWN) != null)
		{
			for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_SPAWN))
			{
				quest.notifySpawn(this);
			}
		}
		
		if (!isTeleporting())
		{
			WalkingManager.getInstance().onSpawn(this);
		}
	}
	
	@Override
	public void onDecay()
	{
		if (isDecayed())
		{
			return;
		}
		setDecayed(true);
		super.onDecay();
		
		if (_spawn != null)
		{
			_spawn.decreaseCount(this);
		}
		
		WalkingManager.getInstance().onDeath(this);
	}
	
	@Override
	public void deleteMe()
	{
		L2WorldRegion oldRegion = getWorldRegion();
		
		try
		{
			onDecay();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed decayMe().", e);
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
		if (oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}
		
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed removing cleaning knownlist.", e);
		}
		L2World.getInstance().removeObject(this);
		
		super.deleteMe();
	}
	
	public L2Spawn getSpawn()
	{
		return _spawn;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + getName() + "(" + getId() + ")" + "[" + getObjectId() + "]";
	}
	
	public boolean isDecayed()
	{
		return _isDecayed;
	}
	
	public void setDecayed(boolean decayed)
	{
		_isDecayed = decayed;
	}
	
	public void endDecayTask()
	{
		if (!isDecayed())
		{
			DecayTaskManager.getInstance().cancelDecayTask(this);
			onDecay();
		}
	}
	
	public boolean isMob()
	{
		return false;
	}
	
	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
		updateAbnormalEffect();
	}
	
	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
		updateAbnormalEffect();
	}
	
	public void setLRHandId(int newLWeaponId, int newRWeaponId)
	{
		_currentRHandId = newRWeaponId;
		_currentLHandId = newLWeaponId;
		updateAbnormalEffect();
	}
	
	public void setEnchant(int newEnchantValue)
	{
		_currentEnchant = newEnchantValue;
		updateAbnormalEffect();
	}
	
	public boolean isShowName()
	{
		return _staticAIData.showName();
	}
	
	@Override
	public boolean isTargetable()
	{
		return _staticAIData.isTargetable();
	}
	
	public void setCollisionHeight(double height)
	{
		_currentCollisionHeight = height;
	}
	
	public void setCollisionRadius(double radius)
	{
		_currentCollisionRadius = radius;
	}
	
	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}
	
	public double getCollisionRadius()
	{
		return _currentCollisionRadius;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (isVisibleFor(activeChar))
		{
			if (Config.CHECK_KNOWN && activeChar.isGM())
			{
				activeChar.sendMessage("Added NPC: " + getName());
			}
			
			if (getRunSpeed() == 0)
			{
				activeChar.sendPacket(new ServerObjectInfo(this, activeChar));
			}
			else
			{
				activeChar.sendPacket(new AbstractNpcInfo.NpcInfo(this, activeChar));
			}
		}
	}
	
	public void showNoTeachHtml(L2PcInstance player)
	{
		int npcId = getId();
		String html = "";
		
		if (this instanceof L2WarehouseInstance)
		{
			html = HtmCache.getInstance().getHtm(player.getLang(), "data/html/warehouse/" + npcId + "-noteach.htm");
		}
		else if (this instanceof L2TrainerInstance)
		{
			html = HtmCache.getInstance().getHtm(player.getLang(), "data/html/trainer/" + npcId + "-noteach.htm");
			if (html == null)
			{
				html = HtmCache.getInstance().getHtm(player.getLang(), "data/scripts/ai/npc/Trainers/HealerTrainer/" + npcId + "-noteach.html");
			}
		}
		
		final NpcHtmlMessage noTeachMsg = new NpcHtmlMessage(getObjectId());
		if (html == null)
		{
			_log.warning("Npc " + npcId + " missing noTeach html!");
			noTeachMsg.setHtml("<html><body>I cannot teach you any skills.<br>You must find your current class teachers.</body></html>");
		}
		else
		{
			noTeachMsg.setHtml(html);
			noTeachMsg.replace("%objectId%", String.valueOf(getObjectId()));
		}
		player.sendPacket(noTeachMsg);
	}
	
	public L2Npc scheduleDespawn(long delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new DespawnTask(), delay);
		return this;
	}
	
	public class DespawnTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!L2Npc.this.isDecayed())
			{
				L2Npc.this.deleteMe();
			}
		}
	}
	
	@Override
	protected final void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{
		try
		{
			if (getTemplate().getEventQuests(QuestEventType.ON_SPELL_FINISHED) != null)
			{
				L2PcInstance player = null;
				if (target != null)
				{
					player = target.getActingPlayer();
				}
				for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_SPELL_FINISHED))
				{
					quest.notifySpellFinished(this, player, skill);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || (getCanMove() == 0) || getAiType().equals(AIType.CORPSE);
	}
	
	public AIType getAiType()
	{
		return _staticAIData.getAiType();
	}
	
	public void setDisplayEffect(int val)
	{
		if (val != _displayEffect)
		{
			_displayEffect = val;
			broadcastPacket(new ExChangeNpcState(getObjectId(), val));
		}
	}
	
	public int getDisplayEffect()
	{
		return _displayEffect;
	}
	
	public int getColorEffect()
	{
		return 0;
	}
	
	public void broadcastNpcSay(String text)
	{
		broadcastNpcSay(0, text);
	}
	
	public void broadcastNpcSay(int messageType, String text)
	{
		broadcastPacket(new NpcSay(getObjectId(), messageType, getId(), text));
	}
	
	public L2Character getSummoner()
	{
		return _summoner;
	}
	
	public void setSummoner(L2Character summoner)
	{
		_summoner = summoner;
	}
	
	@Override
	public boolean isNpc()
	{
		return true;
	}
	
	@Override
	public void setTeam(int id)
	{
		super.setTeam(id);
		broadcastInfo();
	}
	
	public final boolean isNoAnimation()
	{
		return _isNoAnimation;
	}
	
	public final void setIsNoAnimation(boolean value)
	{
		_isNoAnimation = value;
	}
	
	private boolean _serverSideTitle = getTemplate()._serverSideTitle;
	private boolean _serverSideName = getTemplate()._serverSideName;
	
	public boolean getServerSideTitle()
	{
		return _serverSideTitle;
	}
	
	public void setServerSideTitle(boolean status)
	{
		_serverSideTitle = status;
	}
	
	public boolean getServerSideName()
	{
		return _serverSideName;
	}
	
	public void setServerSideName(boolean status)
	{
		_serverSideName = status;
	}
	
	@Override
	public boolean isWalker()
	{
		return WalkingManager.getInstance().isRegistered(this);
	}
	
	@Override
	public boolean isRunner()
	{
		return _isRunner;
	}
	
	public void setIsRunner(boolean status)
	{
		_isRunner = status;
	}
	
	@Override
	public boolean isSpecialCamera()
	{
		return _isSpecialCamera;
	}
	
	public void setIsSpecialCamera(boolean status)
	{
		_isSpecialCamera = status;
	}
	
	@Override
	public boolean isEkimusFood()
	{
		return _isEkimusFood;
	}
	
	public void setIsEkimusFood(boolean status)
	{
		_isEkimusFood = status;
	}
	
	public void setRandomAnimationEnabled(boolean val)
	{
		_isRandomAnimationEnabled = val;
	}
	
	public boolean isRandomAnimationEnabled()
	{
		return _isRandomAnimationEnabled;
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
		{
			_shotsMask |= type.getMask();
		}
		else
		{
			_shotsMask &= ~type.getMask();
		}
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic)
	{
		if ((_soulshotamount > 0) || (_spiritshotamount > 0))
		{
			if (physical)
			{
				if (_soulshotamount == 0)
				{
					return;
				}
				else if (Rnd.get(100) > getSoulShotChance())
				{
					return;
				}
				_soulshotamount--;
				Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2154, 1, 0, 0), 600);
				setChargedShot(ShotType.SOULSHOTS, true);
			}
			if (magic)
			{
				if (_spiritshotamount == 0)
				{
					return;
				}
				else if (Rnd.get(100) > getSpiritShotChance())
				{
					return;
				}
				_spiritshotamount--;
				Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2061, 1, 0, 0), 600);
				setChargedShot(ShotType.SPIRITSHOTS, true);
			}
		}
	}
	
	public int getScriptValue()
	{
		return getVariables().getInteger("SCRIPT_VAL");
	}
	
	public void setScriptValue(int val)
	{
		getVariables().set("SCRIPT_VAL", val);
	}
	
	public boolean isScriptValue(int val)
	{
		return getVariables().getInteger("SCRIPT_VAL") == val;
	}
	
	public void broadcastEvent(String eventName, int radius, L2Object reference)
	{
		for (L2Object obj : L2World.getInstance().getVisibleObjects(this, radius))
		{
			if (obj.isNpc() && (((L2Npc) obj).getTemplate().getEventQuests(QuestEventType.ON_EVENT_RECEIVED) != null))
			{
				for (Quest quest : ((L2Npc) obj).getTemplate().getEventQuests(QuestEventType.ON_EVENT_RECEIVED))
				{
					quest.notifyEventReceived(eventName, this, (L2Npc) obj, reference);
				}
			}
		}
	}
	
	@Override
	public boolean isInCategory(CategoryType type)
	{
		return CategoryParser.getInstance().isInCategory(type, getId());
	}
	
	@Override
	public L2Npc getActingNpc()
	{
		return this;
	}
	
	public final boolean isSevenSignsMonster()
	{
		return _isSevenSignsMonster;
	}
	
	public void setSevenSignsMonster(boolean isSevenSignsMonster)
	{
		_isSevenSignsMonster = isSevenSignsMonster;
	}
	
	public boolean hasVariables()
	{
		return getScript(NpcVariables.class) != null;
	}
	
	public NpcVariables getVariables()
	{
		final NpcVariables vars = getScript(NpcVariables.class);
		return vars != null ? vars : addScript(new NpcVariables());
	}
	
	public boolean staysInSpawnLoc()
	{
		return ((getSpawn() != null) && (getSpawn().getX(this) == getX()) && (getSpawn().getY(this) == getY()));
	}
	
	private int isMonday()
	{
		return Calendar.MONDAY;
	}
	
	private int isTuesday()
	{
		return Calendar.TUESDAY;
	}
	
	private int isWednesday()
	{
		return Calendar.WEDNESDAY;
	}
	
	private int isThursday()
	{
		return Calendar.THURSDAY;
	}
	
	private int isFriday()
	{
		return Calendar.FRIDAY;
	}
	
	private int isSaturday()
	{
		return Calendar.SATURDAY;
	}
	
	private int isSunday()
	{
		return Calendar.SUNDAY;
	}
	
	@Override
	public boolean isVisibleFor(L2PcInstance player)
	{
		if (getTemplate().getEventQuests(QuestEventType.ON_CAN_SEE_ME) != null)
		{
			for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_CAN_SEE_ME))
			{
				return quest.notifyOnCanSeeMe(this, player);
			}
		}
		return super.isVisibleFor(player);
	}
}