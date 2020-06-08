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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.WeakFastSet;
import l2e.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.ai.L2AttackableAI;
import l2e.gameserver.ai.L2CharacterAI;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.handler.SkillHandler;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.ChanceSkillList;
import l2e.gameserver.model.CharEffectList;
import l2e.gameserver.model.FusionSkill;
import l2e.gameserver.model.L2AccessLevel;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.events.CharEvents;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2EventMapGuardInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.actor.instance.L2RiftInvaderInstance;
import l2e.gameserver.model.actor.knownlist.CharKnownList;
import l2e.gameserver.model.actor.position.CharPosition;
import l2e.gameserver.model.actor.stat.CharStat;
import l2e.gameserver.model.actor.status.CharStatus;
import l2e.gameserver.model.actor.tasks.character.HitTask;
import l2e.gameserver.model.actor.tasks.character.MagicUseTask;
import l2e.gameserver.model.actor.tasks.character.NotifyAITask;
import l2e.gameserver.model.actor.tasks.character.QueuedMagicUseTask;
import l2e.gameserver.model.actor.tasks.character.UsePotionTask;
import l2e.gameserver.model.actor.templates.L2CharTemplate;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.actor.transform.Transform;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.model.effects.EffectFlag;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.entity.events.phoenix.Interface;
import l2e.gameserver.model.holders.SkillUseHolder;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.interfaces.IChanceSkillTrigger;
import l2e.gameserver.model.interfaces.ISkillsHolder;
import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.options.OptionsSkillHolder;
import l2e.gameserver.model.options.OptionsSkillType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.l2skills.L2SkillSummon;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Calculator;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.Attack;
import l2e.gameserver.network.serverpackets.ChangeMoveType;
import l2e.gameserver.network.serverpackets.ChangeWaitType;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.MagicSkillCanceld;
import l2e.gameserver.network.serverpackets.MagicSkillLaunched;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.network.serverpackets.Revive;
import l2e.gameserver.network.serverpackets.ServerObjectInfo;
import l2e.gameserver.network.serverpackets.SetupGauge;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.StopMove;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TeleportToLocation;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;
import l2e.gameserver.util.L2TIntObjectHashMap;
import l2e.gameserver.util.PositionUtils;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

public abstract class L2Character extends L2Object implements ISkillsHolder
{
	public static final Logger _log = Logger.getLogger(L2Character.class.getName());
	
	private volatile Set<L2Character> _attackByList;
	private volatile boolean _isCastingNow = false;
	private volatile boolean _isCastingSimultaneouslyNow = false;
	private L2Skill _lastSkillCast;
	private L2Skill _lastSimultaneousSkillCast;
	
	private boolean _isDead = false;
	private boolean _isImmobilized = false;
	private boolean _isOverloaded = false;
	private boolean _isParalyzed = false;
	private boolean _isPendingRevive = false;
	private boolean _isRunning = false;
	private boolean _isNoRndWalk = false;
	protected boolean _showSummonAnimation = false;
	protected boolean _isTeleporting = false;
	private boolean _isInvul = false;
	private boolean _isMortal = true;
	private boolean _isFlying = false;
	private boolean _champion = false;
	private boolean _phantome = false;
	
	private int _PremiumService;
	private boolean _isInTownWar;
	private boolean _blocked;
	
	private CharStat _stat;
	private CharStatus _status;
	private CharEvents _events;
	private L2CharTemplate _template;
	private String _title;
	
	public static final double MAX_HP_BAR_PX = 352.0;
	
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;
	
	private Calculator[] _calculators;
	protected final ReentrantLock _lock = new ReentrantLock();
	private L2WorldRegion _oldReg;
	protected Collection<L2DoorInstance> _doors;
	
	private boolean _isNoLethal = false;
	
	private boolean _isAttackDisabled = false;
	
	private final FastMap<Integer, L2Skill> _skills = new FastMap<>();
	
	private volatile ChanceSkillList _chanceSkills;
	
	protected FusionSkill _fusionSkill;
	
	private final byte[] _zones = new byte[ZoneId.getZoneCount()];
	protected byte _zoneValidateCounter = 4;
	
	private L2Character _debugger = null;
	
	private final ReentrantLock _teleportLock;
	
	private int _team;
	
	protected long _exceptions = 0L;
	
	private volatile Map<Integer, OptionsSkillHolder> _triggerSkills;
	
	public boolean isDebug()
	{
		return _debugger != null;
	}
	
	public void setDebug(L2Character d)
	{
		_debugger = d;
	}
	
	public void sendDebugPacket(L2GameServerPacket pkt)
	{
		if (_debugger != null)
		{
			_debugger.sendPacket(pkt);
		}
	}
	
	public void sendDebugMessage(String msg)
	{
		if (_debugger != null)
		{
			_debugger.sendMessage(msg);
		}
	}
	
	public Inventory getInventory()
	{
		return null;
	}
	
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		return true;
	}
	
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		return true;
	}
	
	@Override
	public final boolean isInsideZone(ZoneId zone)
	{
		Instance instance = null;
		
		if (InstanceManager.getInstance().instanceExist(getInstanceId()))
		{
			instance = InstanceManager.getInstance().getInstance(getInstanceId());
		}
		
		switch (zone)
		{
			case PVP:
				if ((instance != null) && instance.isPvPInstance())
				{
					return true;
				}
				return (_zones[ZoneId.PVP.getId()] > 0) && (_zones[ZoneId.PEACE.getId()] == 0);
			case PEACE:
				if ((instance != null) && instance.isPvPInstance())
				{
					return false;
				}
		}
		return _zones[zone.getId()] > 0;
	}
	
	public final void setInsideZone(ZoneId zone, final boolean state)
	{
		synchronized (_zones)
		{
			if (state)
			{
				_zones[zone.getId()]++;
			}
			else
			{
				_zones[zone.getId()]--;
				if (_zones[zone.getId()] < 0)
				{
					_zones[zone.getId()] = 0;
				}
			}
		}
	}
	
	public boolean isTransformed()
	{
		return false;
	}
	
	public Transform getTransformation()
	{
		return null;
	}
	
	public void untransform()
	{
	}
	
	public boolean isGM()
	{
		return false;
	}
	
	public L2AccessLevel getAccessLevel()
	{
		return null;
	}
	
	public boolean isInTownWarEvent()
	{
		return _isInTownWar;
	}
	
	public void setInTownWarEvent(boolean value)
	{
		_isInTownWar = value;
	}
	
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		
		if (template == null)
		{
			throw new NullPointerException("Template is null!");
		}
		setInstanceType(InstanceType.L2Character);
		initCharStat();
		initCharStatus();
		initCharEvents();
		
		_skills.shared();
		
		_template = template;
		
		if (isDoor())
		{
			_calculators = Formulas.getStdDoorCalculators();
		}
		else if (isNpc())
		{
			_calculators = NPC_STD_CALCULATOR;
			
			if (template.getSkills() != null)
			{
				_skills.putAll(template.getSkills());
			}
			
			for (L2Skill skill : _skills.values())
			{
				addStatFuncs(skill.getStatFuncs(null, this));
			}
		}
		else
		{
			_calculators = new Calculator[Stats.NUM_STATS];
			
			if (isSummon())
			{
				_skills.putAll(((L2NpcTemplate) template).getSkills());
				
				for (L2Skill skill : _skills.values())
				{
					addStatFuncs(skill.getStatFuncs(null, this));
				}
			}
			Formulas.addFuncsToNewCharacter(this);
		}
		setIsInvul(true);
		_teleportLock = new ReentrantLock();
	}
	
	protected void initCharStatusUpdateValues()
	{
		_hpUpdateIncCheck = getMaxHp();
		_hpUpdateInterval = _hpUpdateIncCheck / MAX_HP_BAR_PX;
		_hpUpdateDecCheck = _hpUpdateIncCheck - _hpUpdateInterval;
	}
	
	public void onDecay()
	{
		L2WorldRegion reg = getWorldRegion();
		decayMe();
		if (reg != null)
		{
			reg.removeFromZones(this);
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		revalidateZone(true);
	}
	
	public void onTeleported()
	{
		if (!_teleportLock.tryLock())
		{
			return;
		}
		try
		{
			if (!isTeleporting())
			{
				return;
			}
			spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());
			setIsTeleporting(false);
			getEvents().onTeleported();
		}
		finally
		{
			_teleportLock.unlock();
		}
		if (_isPendingRevive || isPhantome())
		{
			doRevive();
		}
	}
	
	public void addAttackerToAttackByList(L2Character player)
	{
	}
	
	public void broadcastPacket(L2GameServerPacket mov)
	{
		mov.setInvisible(isInvisible());
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player != null)
			{
				player.sendPacket(mov);
			}
		}
	}
	
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		mov.setInvisible(isInvisible());
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if ((player != null) && isInsideRadius(player, radiusInKnownlist, false, false))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	protected boolean needHpUpdate()
	{
		double currentHp = getCurrentHp();
		double maxHp = getMaxHp();
		
		if ((currentHp <= 1.0) || (maxHp < MAX_HP_BAR_PX))
		{
			return true;
		}
		
		if ((currentHp < _hpUpdateDecCheck) || (Math.abs(currentHp - _hpUpdateDecCheck) <= 1e-6) || (currentHp > _hpUpdateIncCheck) || (Math.abs(currentHp - _hpUpdateIncCheck) <= 1e-6))
		{
			if (Math.abs(currentHp - maxHp) <= 1e-6)
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty() || !needHpUpdate())
		{
			return;
		}
		
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		
		for (L2Character temp : getStatus().getStatusListener())
		{
			if (temp != null)
			{
				temp.sendPacket(su);
			}
		}
	}
	
	public void sendMessage(String text)
	{
	}
	
	public void teleToLocation(int x, int y, int z, int heading, int randomOffset)
	{
		if (Config.TW_DISABLE_GK && isInTownWarEvent() && !isPendingRevive())
		{
			sendMessage((new CustomMessage("TW.DISABLE_GK", getActingPlayer().getLang())).toString());
			return;
		}
		
		stopMove(null, false);
		abortAttack();
		abortCast();
		
		setIsTeleporting(true);
		setTarget(null);
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		if (Config.OFFSET_ON_TELEPORT_ENABLED && (randomOffset > 0))
		{
			int xt = x + Rnd.get(-randomOffset, randomOffset);
			int yt = y + Rnd.get(-randomOffset, randomOffset);
			int zt = GeoClient.getInstance().getSpawnHeight(xt, yt, z);
			Location dest = GeoClient.getInstance().moveCheckWithoutDoors(new Location(x, y, z), new Location(xt, yt, zt), true);
			if (dest != null)
			{
				x = dest.getX();
				y = dest.getY();
				z = dest.getZ();
			}
		}
		
		z += 4;
		
		broadcastPacket(new TeleportToLocation(this, x, y, z, heading));
		
		decayMe();
		
		getPosition().setXYZ(x, y, z);
		
		if (heading != 0)
		{
			getPosition().setHeading(heading);
		}
		
		if (!isPlayer() || ((getActingPlayer().getClient() != null) && getActingPlayer().getClient().isDetached()))
		{
			onTeleported();
		}
		
		if (isPhantome())
		{
			onTeleported();
		}
		revalidateZone(true);
	}
	
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getHeading(), 0);
	}
	
	public void teleToLocation(int x, int y, int z, int randomOffset)
	{
		teleToLocation(x, y, z, getHeading(), randomOffset);
	}
	
	public void teleToLocation(Location loc, int randomOffset)
	{
		setInstanceId(loc.getInstanceId());
		
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		
		if (isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), false))
		{
			L2PcInstance player = getActingPlayer();
			player.sendMessage("You have been sent to the waiting room.");
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
			int[] newCoords = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportCoorinates();
			x = newCoords[0];
			y = newCoords[1];
			z = newCoords[2];
		}
		teleToLocation(x, y, z, getHeading(), randomOffset);
	}
	
	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, teleportWhere), true);
	}
	
	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		teleToLocation(loc, (allowRandomOffset ? Config.MAX_OFFSET_ON_TELEPORT : 0));
	}
	
	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		if (allowRandomOffset)
		{
			teleToLocation(x, y, z, Config.MAX_OFFSET_ON_TELEPORT);
		}
		else
		{
			teleToLocation(x, y, z, 0);
		}
	}
	
	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		if (allowRandomOffset)
		{
			teleToLocation(x, y, z, heading, Config.MAX_OFFSET_ON_TELEPORT);
		}
		else
		{
			teleToLocation(x, y, z, heading, 0);
		}
	}
	
	protected void doAttack(L2Character target)
	{
		if ((this instanceof L2PcInstance) && Interface.doAttack(getObjectId(), target.getObjectId()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((this instanceof L2Summon) && Interface.doAttack(((L2Summon) this).getOwner().getObjectId(), target.getObjectId()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((target == null) || isAttackingDisabled() || !getEvents().onAttack(target))
		{
			return;
		}
		
		if (!isAlikeDead())
		{
			if ((isNpc() && target.isAlikeDead()) || !getKnownList().knowsObject(target))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (isPlayer())
			{
				if (target.isDead())
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				final L2PcInstance actor = getActingPlayer();
				if (actor.isTransformed() && !actor.getTransformation().canAttack())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if (getActiveWeaponItem() != null)
		{
			L2Weapon wpn = getActiveWeaponItem();
			if (!wpn.isAttackWeapon() && !isGM())
			{
				if (wpn.getItemType() == L2WeaponType.FISHINGROD)
				{
					sendPacket(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE);
				}
				else
				{
					sendPacket(SystemMessageId.THAT_WEAPON_CANT_ATTACK);
				}
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (getActingPlayer() != null)
		{
			if (getActingPlayer().inObserverMode())
			{
				sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			else if ((target.getActingPlayer() != null) && (getActingPlayer().getSiegeState() > 0) && isInsideZone(ZoneId.SIEGE) && (target.getActingPlayer().getSiegeState() == getActingPlayer().getSiegeState()) && (target.getActingPlayer() != this) && (target.getActingPlayer().getSiegeSide() == getActingPlayer().getSiegeSide()))
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
				return;
			}
			else if (target.isInsidePeaceZone(getActingPlayer()))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		stopEffectsOnAction();
		
		L2Weapon weaponItem = getActiveWeaponItem();
		
		if (!GeoClient.getInstance().canSeeTarget(this, target))
		{
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((weaponItem != null) && !isTransformed())
		{
			if (weaponItem.getItemType() == L2WeaponType.BOW)
			{
				if (isPlayer())
				{
					if (target.isInsidePeaceZone(getActingPlayer()))
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					if (!checkAndEquipArrows())
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(SystemMessageId.NOT_ENOUGH_ARROWS);
						return;
					}
					
					if (_disableBowAttackEndTime <= GameTimeController.getInstance().getGameTicks())
					{
						int mpConsume = weaponItem.getMpConsume();
						if ((weaponItem.getReducedMpConsume() > 0) && (Rnd.get(100) < weaponItem.getReducedMpConsumeChance()))
						{
							mpConsume = weaponItem.getReducedMpConsume();
						}
						mpConsume = (int) calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume, null, null);
						
						if (getCurrentMp() < mpConsume)
						{
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						
						if (mpConsume > 0)
						{
							getStatus().reduceMp(mpConsume);
						}
						
						_disableBowAttackEndTime = (5 * GameTimeController.TICKS_PER_SECOND) + GameTimeController.getInstance().getGameTicks();
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
			if (weaponItem.getItemType() == L2WeaponType.CROSSBOW)
			{
				if (isPlayer())
				{
					if (target.isInsidePeaceZone(getActingPlayer()))
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					if (!checkAndEquipBolts())
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(SystemMessageId.NOT_ENOUGH_BOLTS);
						return;
					}
					
					if (_disableCrossBowAttackEndTime <= GameTimeController.getInstance().getGameTicks())
					{
						int mpConsume = weaponItem.getMpConsume();
						if ((weaponItem.getReducedMpConsume() > 0) && (Rnd.get(100) < weaponItem.getReducedMpConsumeChance()))
						{
							mpConsume = weaponItem.getReducedMpConsume();
						}
						mpConsume = (int) calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume, null, null);
						
						if (getCurrentMp() < mpConsume)
						{
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						
						if (mpConsume > 0)
						{
							getStatus().reduceMp(mpConsume);
						}
						
						_disableCrossBowAttackEndTime = (5 * GameTimeController.TICKS_PER_SECOND) + GameTimeController.getInstance().getGameTicks();
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				else if (isNpc())
				{
					if (_disableCrossBowAttackEndTime > GameTimeController.getInstance().getGameTicks())
					{
						return;
					}
				}
			}
		}
		target.getKnownList().addKnownObject(this);
		
		if (Config.ALT_GAME_TIREDNESS)
		{
			setCurrentCp(getCurrentCp() - 10);
		}
		
		rechargeShots(true, false);
		boolean wasSSCharged = isChargedShot(ShotType.SOULSHOTS);
		final int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		final int timeToHit = timeAtk / 2;
		_attackEndTime = (GameTimeController.getInstance().getGameTicks() + (timeAtk / GameTimeController.MILLIS_IN_TICK)) - 1;
		int ssGrade = (weaponItem != null) ? weaponItem.getItemGradeSPlus() : 0;
		
		if (isPhantome())
		{
			if (weaponItem != null)
			{
				wasSSCharged = true;
				if ((weaponItem.getCrystalType() == L2Item.CRYSTAL_S84) || (weaponItem.getCrystalType() == L2Item.CRYSTAL_S80) || (weaponItem.getCrystalType() == L2Item.CRYSTAL_S))
				{
					ssGrade = 5;
				}
				else if (weaponItem.getCrystalType() == L2Item.CRYSTAL_A)
				{
					ssGrade = 4;
				}
				else if (weaponItem.getCrystalType() == L2Item.CRYSTAL_B)
				{
					ssGrade = 3;
				}
				else if (weaponItem.getCrystalType() == L2Item.CRYSTAL_C)
				{
					ssGrade = 2;
				}
				else if (weaponItem.getCrystalType() == L2Item.CRYSTAL_D)
				{
					ssGrade = 1;
				}
				else if (weaponItem.getCrystalType() == L2Item.CRYSTAL_NONE)
				{
					ssGrade = 0;
				}
			}
		}
		
		Attack attack = new Attack(this, target, wasSSCharged, ssGrade);
		
		setHeading(Util.calculateHeadingFrom(this, target));
		
		int reuse = calculateReuseTime(target, weaponItem);
		
		boolean hitted;
		if ((weaponItem == null) || isTransformed())
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
		{
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		}
		else if (weaponItem.getItemType() == L2WeaponType.CROSSBOW)
		{
			hitted = doAttackHitByCrossBow(attack, target, timeAtk, reuse);
		}
		else if (weaponItem.getItemType() == L2WeaponType.POLE)
		{
			hitted = doAttackHitByPole(attack, target, timeToHit);
		}
		else if (isUsingDualWeapon())
		{
			hitted = doAttackHitByDual(attack, target, timeToHit);
		}
		else
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		
		final L2PcInstance player = getActingPlayer();
		
		if (player != null)
		{
			AttackStanceTaskManager.getInstance().addAttackStanceTask(player);
			
			if (player.getSummon() != target)
			{
				player.updatePvPStatus(target);
			}
		}
		
		if (!hitted)
		{
			abortAttack();
		}
		else
		{
			setChargedShot(ShotType.SOULSHOTS, false);
			
			if ((this instanceof L2PcInstance) && (target instanceof L2PcInstance))
			{
				if (Interface.isParticipating(getObjectId()))
				{
					Interface.onHit(getObjectId(), target.getObjectId());
				}
			}
			
			if (player != null)
			{
				if (player.isCursedWeaponEquipped())
				{
					if (!target.isInvul())
					{
						target.setCurrentCp(0);
					}
				}
				else if (player.isHero())
				{
					if (target.isPlayer() && target.getActingPlayer().isCursedWeaponEquipped())
					{
						target.setCurrentCp(0);
					}
				}
			}
		}
		
		if (attack.hasHits())
		{
			broadcastPacket(attack);
		}
		
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}
	
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		if (!Config.INFINITE_ARROWS)
		{
			reduceArrowCount(false);
			_move = null;
		}
		
		if (!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.hasSoulshot());
			damage1 *= (Math.sqrt(getDistanceSq(target)) / 4000) + 0.8;
		}
		
		if (isPlayer())
		{
			sendPacket(new SetupGauge(SetupGauge.RED, sAtk + reuse));
		}
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk);
		
		_disableBowAttackEndTime = ((sAtk + reuse) / GameTimeController.MILLIS_IN_TICK) + GameTimeController.getInstance().getGameTicks();
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		return !miss1;
	}
	
	private boolean doAttackHitByCrossBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		reduceArrowCount(true);
		
		_move = null;
		
		if (!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.hasSoulshot());
		}
		
		if (isPlayer())
		{
			sendPacket(SystemMessageId.CROSSBOW_PREPARING_TO_FIRE);
			
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}
		
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk);
		_disableCrossBowAttackEndTime = ((sAtk + reuse) / GameTimeController.MILLIS_IN_TICK) + GameTimeController.getInstance().getGameTicks();
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		return !miss1;
	}
	
	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		
		if (!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.hasSoulshot());
			damage1 /= 2;
		}
		
		if (!miss2)
		{
			shld2 = Formulas.calcShldUse(this, target);
			crit2 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, attack.hasSoulshot());
			damage2 /= 2;
		}
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk / 2);
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage2, crit2, miss2, attack.hasSoulshot(), shld2), sAtk);
		
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
		
		return (!miss1 || !miss2);
	}
	
	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		int maxRadius = getPhysicalAttackRange();
		int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);
		
		int attackRandomCountMax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 0, null, null);
		int attackcount = 0;
		
		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		L2Character temp;
		Collection<L2Object> objs = getKnownList().getKnownObjects().values();
		
		for (L2Object obj : objs)
		{
			if (obj == target)
			{
				continue;
			}
			
			if (obj instanceof L2Character)
			{
				if (obj.isPet() && isPlayer() && (((L2PetInstance) obj).getOwner() == getActingPlayer()))
				{
					continue;
				}
				
				if (!Util.checkIfInRange(maxRadius, this, obj, false))
				{
					continue;
				}
				
				if (Math.abs(obj.getZ() - getZ()) > 650)
				{
					continue;
				}
				if (!isFacing(obj, maxAngleDiff))
				{
					continue;
				}
				
				if (isL2Attackable() && obj.isPlayer() && getTarget().isL2Attackable())
				{
					continue;
				}
				
				if (isL2Attackable() && obj.isL2Attackable() && (((L2Attackable) this).getEnemyClan() == null) && (((L2Attackable) this).getIsChaos() == 0))
				{
					continue;
				}
				
				if (isL2Attackable() && obj.isL2Attackable() && !((L2Attackable) this).getEnemyClan().equals(((L2Attackable) obj).getClan()) && (((L2Attackable) this).getIsChaos() == 0))
				{
					continue;
				}
				
				temp = (L2Character) obj;
				
				if (!temp.isAlikeDead())
				{
					if ((temp == getAI().getAttackTarget()) || temp.isAutoAttackable(this))
					{
						hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
						attackpercent /= 1.15;
						
						attackcount++;
						if (attackcount > attackRandomCountMax)
						{
							break;
						}
					}
				}
			}
		}
		return hitted;
	}
	
	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}
	
	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		if (!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.hasSoulshot());
			
			if (attackpercent != 100)
			{
				damage1 = (int) ((damage1 * attackpercent) / 100);
			}
		}
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk);
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		return !miss1;
	}
	
	public void doCast(L2Skill skill)
	{
		beginCast(skill, false);
	}
	
	public void doSimultaneousCast(L2Skill skill)
	{
		beginCast(skill, true);
	}
	
	public void doCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if (!checkDoCastConditions(skill))
		{
			setIsCastingNow(false);
			return;
		}
		
		if (skill.isSimultaneousCast())
		{
			doSimultaneousCast(skill, target, targets);
			return;
		}
		stopEffectsOnAction();
		beginCast(skill, false, target, targets);
	}
	
	public void doSimultaneousCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if (!checkDoCastConditions(skill))
		{
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		stopEffectsOnAction();
		beginCast(skill, true, target, targets);
	}
	
	private void beginCast(L2Skill skill, boolean simultaneously)
	{
		if (this instanceof L2PcInstance)
		{
			if (Interface.isParticipating(getObjectId()))
			{
				
				if (this.getTarget() instanceof L2PcInstance)
				{
					if (Interface.areTeammates(getObjectId(), getTarget().getObjectId()) && !Interface.getBoolean("friendlyFireEnabled", 0) && skill.isOffensive())
					{
						if (simultaneously)
						{
							setIsCastingSimultaneouslyNow(false);
						}
						else
						{
							setIsCastingNow(false);
						}
						if (isPlayer())
						{
							sendPacket(ActionFailed.STATIC_PACKET);
							getAI().setIntention(AI_INTENTION_ACTIVE);
						}
						return;
					}
				}
				
				if (getTarget() != null)
				{
					if (!Interface.canAttack(getObjectId(), getTarget().getObjectId()) && skill.isOffensive())
					{
						if (simultaneously)
						{
							setIsCastingSimultaneouslyNow(false);
						}
						else
						{
							setIsCastingNow(false);
						}
						if (isPlayer())
						{
							sendPacket(ActionFailed.STATIC_PACKET);
							getAI().setIntention(AI_INTENTION_ACTIVE);
						}
						return;
					}
				}
				
				if (!Interface.onUseMagic(this.getObjectId(), skill.getId()))
				{
					if (simultaneously)
					{
						setIsCastingSimultaneouslyNow(false);
					}
					else
					{
						setIsCastingNow(false);
					}
					if (isPlayer())
					{
						sendPacket(ActionFailed.STATIC_PACKET);
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}
			}
		}
		
		if (this instanceof L2Summon)
		{
			if (Interface.isParticipating(((L2Summon) this).getOwner().getObjectId()))
			{
				if (this.getTarget() instanceof L2PcInstance)
				{
					if (Interface.areTeammates(((L2Summon) this).getOwner().getObjectId(), getTarget().getObjectId()) && !Interface.getBoolean("friendlyFireEnabled", 0) && skill.isOffensive())
					{
						if (simultaneously)
						{
							setIsCastingSimultaneouslyNow(false);
						}
						else
						{
							setIsCastingNow(false);
						}
						return;
					}
				}
				
				if (!Interface.canAttack(((L2Summon) this).getOwner().getObjectId(), this.getTarget().getObjectId()))
				{
					if (simultaneously)
					{
						setIsCastingSimultaneouslyNow(false);
					}
					else
					{
						setIsCastingNow(false);
					}
					return;
				}
				
				if (!Interface.onUseMagic(((L2Summon) this).getOwner().getObjectId(), skill.getId()))
				{
					if (simultaneously)
					{
						setIsCastingSimultaneouslyNow(false);
					}
					else
					{
						setIsCastingNow(false);
					}
					if (isPlayer())
					{
						sendPacket(ActionFailed.STATIC_PACKET);
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}
				
			}
		}
		
		if (!checkDoCastConditions(skill))
		{
			if (simultaneously)
			{
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				setIsCastingNow(false);
			}
			if (isPlayer())
			{
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}
		
		if (skill.isSimultaneousCast() && !simultaneously)
		{
			simultaneously = true;
		}
		
		stopEffectsOnAction();
		
		rechargeShots(skill.useSoulShot(), skill.useSpiritShot());
		
		L2Character target = null;
		L2Object[] targets = skill.getTargetList(this);
		
		boolean doit = false;
		
		switch (skill.getTargetType())
		{
			case AREA_SUMMON:
				target = getSummon();
				break;
			case AURA:
			case AURA_CORPSE_MOB:
			case FRONT_AURA:
			case BEHIND_AURA:
			case GROUND:
				target = this;
				break;
			case SELF:
			case PET:
			case SERVITOR:
			case SUMMON:
			case OWNER_PET:
			case PARTY:
			case CLAN:
			case PARTY_CLAN:
				doit = true;
			default:
				if (targets.length == 0)
				{
					if (simultaneously)
					{
						setIsCastingSimultaneouslyNow(false);
					}
					else
					{
						setIsCastingNow(false);
					}
					
					if (isPlayer())
					{
						sendPacket(ActionFailed.STATIC_PACKET);
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}
				
				switch (skill.getSkillType())
				{
					case BUFF:
						doit = true;
						break;
					case DUMMY:
						if (skill.hasEffectType(L2EffectType.CPHEAL, L2EffectType.HEAL))
						{
							doit = true;
						}
						break;
				}
				
				if (doit)
				{
					target = (L2Character) targets[0];
				}
				else
				{
					target = (L2Character) getTarget();
				}
		}
		beginCast(skill, simultaneously, target, targets);
	}
	
	private void beginCast(L2Skill skill, boolean simultaneously, L2Character target, L2Object[] targets)
	{
		if ((target == null) || !getEvents().onMagic(skill, simultaneously, target, targets))
		{
			if (simultaneously)
			{
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				setIsCastingNow(false);
			}
			if (isPlayer())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}
		
		if (skill.getSkillType() == L2SkillType.RESURRECT)
		{
			if (isResurrectionBlocked() || target.isResurrectionBlocked())
			{
				sendPacket(SystemMessageId.REJECT_RESURRECTION);
				target.sendPacket(SystemMessageId.REJECT_RESURRECTION);
				
				if (simultaneously)
				{
					setIsCastingSimultaneouslyNow(false);
				}
				else
				{
					setIsCastingNow(false);
				}
				
				if (isPlayer())
				{
					getAI().setIntention(AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
				}
				return;
			}
		}
		int magicId = skill.getId();
		int skillTime = (skill.getHitTime() + skill.getCoolTime());
		
		boolean effectWhileCasting = (skill.getSkillType() == L2SkillType.FUSION) || (skill.getSkillType() == L2SkillType.SIGNET_CASTTIME);
		if (!effectWhileCasting)
		{
			if (!skill.isStatic())
			{
				skillTime = Formulas.calcAtkSpd(this, skill, skillTime);
			}
			
			if (skill.isMagic() && (isChargedShot(ShotType.SPIRITSHOTS) || isChargedShot(ShotType.BLESSED_SPIRITSHOTS)))
			{
				skillTime = (int) (0.6 * skillTime);
			}
		}
		
		if (skill.isMagic() && ((skill.getHitTime() + skill.getCoolTime()) > 550) && (skillTime < 550))
		{
			skillTime = 550;
		}
		else if (!skill.isStatic() && ((skill.getHitTime() + skill.getCoolTime()) >= 500) && (skillTime < 500))
		{
			skillTime = 500;
		}
		
		if (isCastingSimultaneouslyNow() && simultaneously)
		{
			ThreadPoolManager.getInstance().scheduleAi(new UsePotionTask(this, skill), 100);
			return;
		}
		
		if (simultaneously)
		{
			setIsCastingSimultaneouslyNow(true);
		}
		else
		{
			setIsCastingNow(true);
		}
		
		if (!simultaneously)
		{
			_castInterruptTime = -2 + GameTimeController.getInstance().getGameTicks() + (skillTime / GameTimeController.MILLIS_IN_TICK);
			setLastSkillCast(skill);
		}
		else
		{
			setLastSimultaneousSkillCast(skill);
		}
		
		int reuseDelay;
		
		if (skill.isStaticReuse() || skill.isStatic())
		{
			reuseDelay = (skill.getReuseDelay());
		}
		else if (skill.isMagic())
		{
			reuseDelay = (int) (skill.getReuseDelay() * calcStat(Stats.MAGIC_REUSE_RATE, 1, null, null));
		}
		else
		{
			reuseDelay = (int) (skill.getReuseDelay() * calcStat(Stats.P_REUSE, 1, null, null));
		}
		
		boolean skillMastery = Formulas.calcSkillMastery(this, skill);
		
		if ((reuseDelay > 30000) && !skillMastery)
		{
			addTimeStamp(skill, reuseDelay);
		}
		
		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			getStatus().reduceMp(initmpcons);
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}
		
		if (reuseDelay > 10)
		{
			if (skillMastery)
			{
				reuseDelay = 100;
				
				if (getActingPlayer() != null)
				{
					getActingPlayer().sendPacket(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
				}
			}
			
			disableSkill(skill, reuseDelay);
		}
		
		if (target != this)
		{
			setHeading(Util.calculateHeadingFrom(this, target));
		}
		
		if (effectWhileCasting)
		{
			if (skill.getItemConsumeId() > 0)
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					if (simultaneously)
					{
						setIsCastingSimultaneouslyNow(false);
					}
					else
					{
						setIsCastingNow(false);
					}
					
					if (isPlayer())
					{
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}
			}
			
			if (skill.getMaxSoulConsumeCount() > 0)
			{
				if (isPlayer())
				{
					if (!getActingPlayer().decreaseSouls(skill.getMaxSoulConsumeCount(), skill))
					{
						if (simultaneously)
						{
							setIsCastingSimultaneouslyNow(false);
						}
						else
						{
							setIsCastingNow(false);
						}
						return;
					}
				}
			}
			
			if (skill.getSkillType() == L2SkillType.FUSION)
			{
				startFusionSkill(target, skill);
			}
			else
			{
				callSkill(skill, targets);
			}
		}
		broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), skill.getDisplayLevel(), skillTime, reuseDelay));
		
		if (isPlayer())
		{
			SystemMessage sm = null;
			switch (magicId)
			{
				case 1312:
				{
					break;
				}
				case 2046:
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMON_A_PET);
					break;
				}
				default:
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1);
					sm.addSkillName(skill);
				}
			}
			sendPacket(sm);
		}
		
		if (isPlayable())
		{
			if (!effectWhileCasting && (skill.getItemConsumeId() > 0))
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					getActingPlayer().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					abortCast();
					return;
				}
			}
			
			if ((skill.getReferenceItemId() > 0) && (ItemHolder.getInstance().getTemplate(skill.getReferenceItemId()).getBodyPart() == L2Item.SLOT_DECO))
			{
				for (L2ItemInstance item : getInventory().getItemsByItemId(skill.getReferenceItemId()))
				{
					if (item.isEquipped())
					{
						item.decreaseMana(false, item.useSkillDisTime());
						break;
					}
				}
			}
		}
		
		for (int negateSkillId : skill.getNegateCasterId())
		{
			if (negateSkillId != 0)
			{
				stopSkillEffects(negateSkillId);
			}
		}
		
		MagicUseTask mut = new MagicUseTask(this, targets, skill, skillTime, simultaneously);
		
		if (skillTime > 0)
		{
			if (isPlayer() && !effectWhileCasting)
			{
				sendPacket(new SetupGauge(SetupGauge.BLUE, skillTime));
			}
			
			if (skill.getHitCounts() > 0)
			{
				skillTime = (skillTime * skill.getHitTimings()[0]) / 100;
			}
			
			if (effectWhileCasting)
			{
				mut.setPhase(2);
			}
			
			if (simultaneously)
			{
				Future<?> future = _skillCast2;
				if (future != null)
				{
					future.cancel(true);
					_skillCast2 = null;
				}
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, skillTime - 400);
			}
			else
			{
				Future<?> future = _skillCast;
				if (future != null)
				{
					future.cancel(true);
					_skillCast = null;
				}
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, skillTime - 400);
			}
		}
		else
		{
			mut.setSkillTime(0);
			onMagicLaunchedTimer(mut);
		}
	}
	
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if ((skill == null) || isSkillDisabled(skill) || (((skill.getFlyRadius() > 0) || (skill.getFlyType() != null)) && isMovementDisabled()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)))
		{
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (getCurrentHp() <= skill.getHpConsume())
		{
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!skill.isStatic())
		{
			if (skill.isMagic())
			{
				if (isMuted())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else
			{
				if (isPhysicalMuted())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		if ((skill.getSkillType() == L2SkillType.SIGNET) || (skill.getSkillType() == L2SkillType.SIGNET_CASTTIME))
		{
			L2WorldRegion region = getWorldRegion();
			if (region == null)
			{
				return false;
			}
			boolean canCast = true;
			if ((skill.getTargetType() == L2TargetType.GROUND) && isPlayer())
			{
				Location wp = getActingPlayer().getCurrentSkillWorldPosition();
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
				{
					canCast = false;
				}
			}
			else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
			{
				canCast = false;
			}
			if (!canCast && !isInTownWarEvent())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill);
				sendPacket(sm);
				return false;
			}
		}
		
		if (getActiveWeaponItem() != null)
		{
			L2Weapon wep = getActiveWeaponItem();
			if (wep.useWeaponSkillsOnly() && !isGM() && wep.hasSkills())
			{
				boolean found = false;
				for (SkillsHolder sh : wep.getSkills())
				{
					if (sh.getSkillId() == skill.getId())
					{
						found = true;
					}
				}
				
				if (!found)
				{
					if (getActingPlayer() != null)
					{
						sendPacket(SystemMessageId.WEAPON_CAN_USE_ONLY_WEAPON_SKILL);
					}
					return false;
				}
			}
		}
		
		if ((skill.getItemConsumeId() > 0) && (getInventory() != null))
		{
			L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());
			
			if ((requiredItems == null) || (requiredItems.getCount() < skill.getItemConsume()))
			{
				if (skill.getSkillType() == L2SkillType.SUMMON)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					sendPacket(sm);
				}
				else
				{
					sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
				}
				return false;
			}
		}
		return true;
	}
	
	public void addTimeStampItem(L2ItemInstance item, long reuse)
	{
	}
	
	public long getItemRemainingReuseTime(int itemObjId)
	{
		return -1;
	}
	
	public void addTimeStamp(L2Skill skill, long reuse)
	{
	}
	
	public long getSkillRemainingReuseTime(int skillReuseHashId)
	{
		return -1;
	}
	
	public void startFusionSkill(L2Character target, L2Skill skill)
	{
		if (skill.getSkillType() != L2SkillType.FUSION)
		{
			return;
		}
		
		if (_fusionSkill == null)
		{
			_fusionSkill = new FusionSkill(this, target, skill);
		}
	}
	
	public boolean doDie(L2Character killer)
	{
		synchronized (this)
		{
			if (isDead())
			{
				return false;
			}
			setCurrentHp(0);
			setIsDead(true);
		}
		
		if (!getEvents().onDeath(killer))
		{
			return false;
		}
		setTarget(null);
		stopMove(null);
		getStatus().stopHpMpRegeneration();
		
		if (isPlayable() && ((L2Playable) this).isPhoenixBlessed())
		{
			if (((L2Playable) this).isCharmOfLuckAffected())
			{
				stopEffects(L2EffectType.CHARM_OF_LUCK);
			}
			if (((L2Playable) this).isNoblesseBlessed())
			{
				stopEffects(L2EffectType.NOBLESSE_BLESSING);
			}
		}
		else if (isPlayable() && ((L2Playable) this).isNoblesseBlessed())
		{
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
			if (((L2Playable) this).isCharmOfLuckAffected())
			{
				stopEffects(L2EffectType.CHARM_OF_LUCK);
			}
		}
		else if (!Config.TW_LOSE_BUFFS_ON_DEATH && isInTownWarEvent())
		{
		}
		else
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		
		if (isPlayer() && (getActingPlayer().getAgathionId() != 0))
		{
			getActingPlayer().setAgathionId(0);
		}
		calculateRewards(killer);
		broadcastStatusUpdate();
		
		if (hasAI())
		{
			getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		}
		
		if (getWorldRegion() != null)
		{
			getWorldRegion().onDeath(this);
		}
		
		getAttackByList().clear();
		
		if (isSummon())
		{
			if (((L2Summon) this).isPhoenixBlessed() && (((L2Summon) this).getOwner() != null))
			{
				((L2Summon) this).getOwner().reviveRequest(((L2Summon) this).getOwner(), null, true);
			}
		}
		if (isPlayer())
		{
			if (((L2Playable) this).isPhoenixBlessed())
			{
				getActingPlayer().reviveRequest(getActingPlayer(), null, false);
			}
			else if (isAffected(EffectFlag.CHARM_OF_COURAGE) && getActingPlayer().isInSiege())
			{
				getActingPlayer().reviveRequest(getActingPlayer(), null, false);
			}
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
		return true;
	}
	
	public void deleteMe()
	{
		setDebug(null);
		
		if (hasAI())
		{
			getAI().stopAITask();
		}
	}
	
	protected void calculateRewards(L2Character killer)
	{
	}
	
	public void doRevive()
	{
		if (!isDead())
		{
			return;
		}
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			setIsDead(false);
			boolean restorefull = false;
			
			if (isPlayable() && ((L2Playable) this).isPhoenixBlessed())
			{
				restorefull = true;
				stopEffects(L2EffectType.PHOENIX_BLESSING);
			}
			if (restorefull)
			{
				_status.setCurrentCp(getCurrentCp());
				_status.setCurrentHp(getMaxHp());
				_status.setCurrentMp(getMaxMp());
			}
			else
			{
				if ((Config.RESPAWN_RESTORE_CP > 0) && (getCurrentCp() < (getMaxCp() * Config.RESPAWN_RESTORE_CP)))
				{
					_status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				}
				if ((Config.RESPAWN_RESTORE_HP > 0) && (getCurrentHp() < (getMaxHp() * Config.RESPAWN_RESTORE_HP)))
				{
					_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
				}
				if ((Config.RESPAWN_RESTORE_MP > 0) && (getCurrentMp() < (getMaxMp() * Config.RESPAWN_RESTORE_MP)))
				{
					_status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
				}
			}
			broadcastPacket(new Revive(this));
			if (getWorldRegion() != null)
			{
				getWorldRegion().onRevive(this);
			}
		}
		else
		{
			setIsPendingRevive(true);
		}
	}
	
	public void doRevive(double revivePower)
	{
		doRevive();
	}
	
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2CharacterAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return _ai;
	}
	
	public void setAI(L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = getAI();
		if ((oldAI != null) && (oldAI != newAI) && (oldAI instanceof L2AttackableAI))
		{
			((L2AttackableAI) oldAI).stopAITask();
		}
		_ai = newAI;
	}
	
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	public boolean isRaid()
	{
		return false;
	}
	
	public boolean isMinion()
	{
		return false;
	}
	
	public boolean isRaidMinion()
	{
		return false;
	}
	
	public final Set<L2Character> getAttackByList()
	{
		if (_attackByList == null)
		{
			synchronized (this)
			{
				if (_attackByList == null)
				{
					_attackByList = new WeakFastSet<>(true);
				}
			}
		}
		return _attackByList;
	}
	
	public final L2Skill getLastSimultaneousSkillCast()
	{
		return _lastSimultaneousSkillCast;
	}
	
	public void setLastSimultaneousSkillCast(L2Skill skill)
	{
		_lastSimultaneousSkillCast = skill;
	}
	
	public final L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}
	
	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}
	
	public final boolean isNoRndWalk()
	{
		return _isNoRndWalk;
	}
	
	public final void setIsNoRndWalk(boolean value)
	{
		_isNoRndWalk = value;
	}
	
	public final boolean isAfraid()
	{
		return isAffected(EffectFlag.FEAR);
	}
	
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isStunned() || isSleeping() || isParalyzed();
	}
	
	public boolean isAttackingDisabled()
	{
		return isFlying() || isStunned() || isSleeping() || (_attackEndTime > GameTimeController.getInstance().getGameTicks()) || isAlikeDead() || isParalyzed() || isPhysicalAttackMuted() || isCoreAIDisabled();
	}
	
	public final Calculator[] getCalculators()
	{
		return _calculators;
	}
	
	public final boolean isConfused()
	{
		return isAffected(EffectFlag.CONFUSED);
	}
	
	public boolean isAlikeDead()
	{
		return _isDead;
	}
	
	public final boolean isDead()
	{
		return _isDead;
	}
	
	public final void setIsDead(boolean value)
	{
		_isDead = value;
	}
	
	public boolean isImmobilized()
	{
		return _isImmobilized;
	}
	
	public void setIsImmobilized(boolean value)
	{
		_isImmobilized = value;
	}
	
	public final boolean isMuted()
	{
		return isAffected(EffectFlag.MUTED);
	}
	
	public final boolean isPhysicalMuted()
	{
		return isAffected(EffectFlag.PSYCHICAL_MUTED);
	}
	
	public final boolean isPhysicalAttackMuted()
	{
		return isAffected(EffectFlag.PSYCHICAL_ATTACK_MUTED);
	}
	
	public boolean isMovementDisabled()
	{
		return isBlocked() || isStunned() || isRooted() || isSleeping() || isOverloaded() || isParalyzed() || isImmobilized() || isAlikeDead() || isTeleporting();
	}
	
	public final boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid();
	}
	
	public final boolean isOverloaded()
	{
		return _isOverloaded;
	}
	
	public final void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}
	
	public final boolean isParalyzed()
	{
		return _isParalyzed || isAffected(EffectFlag.PARALYZED);
	}
	
	public final void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}
	
	public final boolean isPendingRevive()
	{
		return isDead() && _isPendingRevive;
	}
	
	public final void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}
	
	public final boolean isDisarmed()
	{
		return isAffected(EffectFlag.DISARMED);
	}
	
	public L2Summon getSummon()
	{
		return null;
	}
	
	public final boolean hasSummon()
	{
		return getSummon() != null;
	}
	
	public final boolean hasPet()
	{
		return hasSummon() && getSummon().isPet();
	}
	
	public final boolean hasServitor()
	{
		return hasSummon() && getSummon().isServitor();
	}
	
	public final boolean isRooted()
	{
		return isAffected(EffectFlag.ROOTED);
	}
	
	public boolean isRunning()
	{
		return _isRunning;
	}
	
	public final void setIsRunning(boolean value)
	{
		_isRunning = value;
		if (getRunSpeed() != 0)
		{
			broadcastPacket(new ChangeMoveType(this));
		}
		if (isPlayer())
		{
			getActingPlayer().broadcastUserInfo();
		}
		else if (isSummon())
		{
			((L2Summon) this).broadcastStatusUpdate();
		}
		else if (isNpc())
		{
			Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
			for (L2PcInstance player : plrs)
			{
				if ((player == null) || !isVisibleFor(player))
				{
					continue;
				}
				else if (getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
				}
				else
				{
					player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) this, player));
				}
			}
		}
	}
	
	public final void setRunning()
	{
		if (!isRunning())
		{
			setIsRunning(true);
		}
	}
	
	public final boolean isSleeping()
	{
		return isAffected(EffectFlag.SLEEP);
	}
	
	public final boolean isStunned()
	{
		return isAffected(EffectFlag.STUNNED);
	}
	
	public final boolean isBetrayed()
	{
		return isAffected(EffectFlag.BETRAYED);
	}
	
	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}
	
	public void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}
	
	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}
	
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || isAffected(EffectFlag.INVUL);
	}
	
	public void setIsMortal(boolean b)
	{
		_isMortal = b;
	}
	
	public boolean isMortal()
	{
		return _isMortal;
	}
	
	public boolean isUndead()
	{
		return false;
	}
	
	public boolean isResurrectionBlocked()
	{
		return isAffected(EffectFlag.BLOCK_RESURRECTION);
	}
	
	public final boolean isFlying()
	{
		return _isFlying;
	}
	
	public final void setIsFlying(boolean mode)
	{
		_isFlying = mode;
	}
	
	@Override
	public CharKnownList getKnownList()
	{
		return ((CharKnownList) super.getKnownList());
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new CharKnownList(this));
	}
	
	public CharStat getStat()
	{
		return _stat;
	}
	
	public void initCharStat()
	{
		_stat = new CharStat(this);
	}
	
	public final void setStat(CharStat value)
	{
		_stat = value;
	}
	
	public CharStatus getStatus()
	{
		return _status;
	}
	
	public void initCharStatus()
	{
		_status = new CharStatus(this);
	}
	
	public final void setStatus(CharStatus value)
	{
		_status = value;
	}
	
	@Override
	public CharPosition getPosition()
	{
		return (CharPosition) super.getPosition();
	}
	
	@Override
	public void initPosition()
	{
		setObjectPosition(new CharPosition(this));
	}
	
	public void initCharEvents()
	{
		_events = new CharEvents(this);
	}
	
	public void setCharEvents(CharEvents events)
	{
		_events = events;
	}
	
	public CharEvents getEvents()
	{
		return _events;
	}
	
	public L2CharTemplate getTemplate()
	{
		return _template;
	}
	
	protected final void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}
	
	public final String getTitle()
	{
		return _title;
	}
	
	public final void setTitle(String value)
	{
		if (value == null)
		{
			_title = "";
		}
		else
		{
			_title = value.length() > 21 ? value.substring(0, 20) : value;
		}
	}
	
	public final void setWalking()
	{
		if (isRunning())
		{
			setIsRunning(false);
		}
	}
	
	private int _AbnormalEffects;
	
	protected CharEffectList _effects = new CharEffectList(this);
	
	private int _SpecialEffects;
	
	public void addEffect(L2Effect newEffect)
	{
		_effects.queueEffect(newEffect, false);
	}
	
	public final void removeEffect(L2Effect effect)
	{
		_effects.queueEffect(effect, true);
	}
	
	public final void startAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects |= mask.getMask();
		updateAbnormalEffect();
	}
	
	public final void startSpecialEffect(AbnormalEffect[] mask)
	{
		for (AbnormalEffect special : mask)
		{
			_SpecialEffects |= special.getMask();
		}
		updateAbnormalEffect();
	}
	
	public final void startAbnormalEffect(int mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}
	
	public final void startSpecialEffect(int mask)
	{
		_SpecialEffects |= mask;
		updateAbnormalEffect();
	}
	
	public final void startFakeDeath()
	{
		if (!isPlayer())
		{
			return;
		}
		
		getActingPlayer().setIsFakeDeath(true);
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}
	
	public final void startStunning()
	{
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED);
		if (!isSummon())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		updateAbnormalEffect();
	}
	
	public final void startParalyze()
	{
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED);
	}
	
	public final void stopAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects &= ~mask.getMask();
		updateAbnormalEffect();
	}
	
	public final void stopSpecialEffect(AbnormalEffect[] mask)
	{
		for (AbnormalEffect special : mask)
		{
			_SpecialEffects &= ~special.getMask();
		}
		updateAbnormalEffect();
	}
	
	public final void stopSpecialEffect(AbnormalEffect mask)
	{
		_SpecialEffects &= ~mask.getMask();
		updateAbnormalEffect();
	}
	
	public final void stopAbnormalEffect(int mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	public final void stopSpecialEffect(int mask)
	{
		_SpecialEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	public void stopAllEffects()
	{
		_effects.stopAllEffects();
	}
	
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		_effects.stopAllEffectsExceptThoseThatLastThroughDeath();
	}
	
	public void stopSkillEffects(int skillId)
	{
		_effects.stopSkillEffects(skillId);
	}
	
	public final void stopEffects(L2EffectType type)
	{
		_effects.stopEffects(type);
	}
	
	public final void stopEffectsOnAction()
	{
		_effects.stopEffectsOnAction();
	}
	
	public final void stopEffectsOnDamage(boolean awake)
	{
		_effects.stopEffectsOnDamage(awake);
	}
	
	public final void stopFakeDeath(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.FAKE_DEATH);
		}
		
		if (isPlayer())
		{
			getActingPlayer().setIsFakeDeath(false);
			getActingPlayer().setRecentFakeDeath(true);
		}
		
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH));
		broadcastPacket(new Revive(this));
	}
	
	public final void stopStunning(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.STUN);
		}
		
		if (!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}
	
	public final void stopTransformation(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.TRANSFORMATION);
		}
		
		if (isPlayer())
		{
			if (getActingPlayer().getTransformation() != null)
			{
				getActingPlayer().untransform();
			}
		}
		
		if (!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}
	
	public abstract void updateAbnormalEffect();
	
	public final void updateEffectIcons()
	{
		updateEffectIcons(false);
	}
	
	public void updateEffectIcons(boolean partyOnly)
	{
	}
	
	public int getAbnormalEffect()
	{
		int ae = _AbnormalEffects;
		if (!isFlying() && isStunned())
		{
			ae |= AbnormalEffect.STUN.getMask();
		}
		if (!isFlying() && isRooted())
		{
			ae |= AbnormalEffect.ROOT.getMask();
		}
		if (isSleeping())
		{
			ae |= AbnormalEffect.SLEEP.getMask();
		}
		if (isConfused())
		{
			ae |= AbnormalEffect.FEAR.getMask();
		}
		if (isMuted())
		{
			ae |= AbnormalEffect.MUTED.getMask();
		}
		if (isPhysicalMuted())
		{
			ae |= AbnormalEffect.MUTED.getMask();
		}
		if (isAfraid())
		{
			ae |= AbnormalEffect.SKULL_FEAR.getMask();
		}
		return ae;
	}
	
	public int getSpecialEffect()
	{
		int se = _SpecialEffects;
		if (isFlying() && isStunned())
		{
			se |= AbnormalEffect.S_AIR_STUN.getMask();
		}
		if (isFlying() && isRooted())
		{
			se |= AbnormalEffect.S_AIR_ROOT.getMask();
		}
		return se;
	}
	
	public final L2Effect[] getAllEffects()
	{
		return _effects.getAllEffects();
	}
	
	public final L2Effect getFirstEffect(int skillId)
	{
		return _effects.getFirstEffect(skillId);
	}
	
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		return _effects.getFirstEffect(skill);
	}
	
	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		return _effects.getFirstEffect(tp);
	}
	
	public final L2Effect getFirstPassiveEffect(L2EffectType type)
	{
		return _effects.getFirstPassiveEffect(type);
	}
	
	public class AIAccessor
	{
		public L2Character getActor()
		{
			return L2Character.this;
		}
		
		public void moveTo(int x, int y, int z, int offset)
		{
			L2Character.this.moveToLocation(x, y, z, offset);
		}
		
		public void moveTo(int x, int y, int z)
		{
			L2Character.this.moveToLocation(x, y, z, 0);
		}
		
		public void stopMove(Location loc)
		{
			L2Character.this.stopMove(loc);
		}
		
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}
		
		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}
		
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(getActor(), evt);
		}
		
		public void detachAI()
		{
			if (isWalker())
			{
				return;
			}
			_ai = null;
		}
	}
	
	public static class MoveData
	{
		public int _moveStartTime;
		public int _moveTimestamp;
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate;
		public double _yAccurate;
		public double _zAccurate;
		public int _heading;
		
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public Vector<Location> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}
	
	protected L2TIntObjectHashMap<Long> _disabledSkills;
	private boolean _allSkillsDisabled;
	
	protected MoveData _move;
	
	private int _heading;
	
	private L2Object _target;
	
	private int _attackEndTime;
	private int _disableBowAttackEndTime;
	private int _disableCrossBowAttackEndTime;
	
	private int _castInterruptTime;
	
	private static final Calculator[] NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators();
	
	protected volatile L2CharacterAI _ai;
	
	protected Future<?> _skillCast;
	protected Future<?> _skillCast2;
	
	public final void addStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		
		synchronized (this)
		{
			if (_calculators == NPC_STD_CALCULATOR)
			{
				_calculators = new Calculator[Stats.NUM_STATS];
				
				for (int i = 0; i < Stats.NUM_STATS; i++)
				{
					if (NPC_STD_CALCULATOR[i] != null)
					{
						_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
					}
				}
			}
			int stat = f.stat.ordinal();
			
			if (_calculators[stat] == null)
			{
				_calculators[stat] = new Calculator();
			}
			
			_calculators[stat].addFunc(f);
		}
	}
	
	public final void addStatFuncs(Func[] funcs)
	{
		List<Stats> modifiedStats = new ArrayList<>();
		
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			addStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}
	
	public final void removeStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		
		int stat = f.stat.ordinal();
		
		synchronized (this)
		{
			if (_calculators[stat] == null)
			{
				return;
			}
			
			_calculators[stat].removeFunc(f);
			
			if (_calculators[stat].size() == 0)
			{
				_calculators[stat] = null;
			}
			
			if (isNpc())
			{
				int i = 0;
				for (; i < Stats.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}
				
				if (i >= Stats.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}
		}
	}
	
	public final void removeStatFuncs(Func[] funcs)
	{
		
		List<Stats> modifiedStats = new ArrayList<>();
		
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			removeStatFunc(f);
		}
		
		broadcastModifiedStats(modifiedStats);
		
	}
	
	public final void removeStatsOwner(Object owner)
	{
		List<Stats> modifiedStats = null;
		
		int i = 0;
		synchronized (this)
		{
			for (Calculator calc : _calculators)
			{
				if (calc != null)
				{
					if (modifiedStats != null)
					{
						modifiedStats.addAll(calc.removeOwner(owner));
					}
					else
					{
						modifiedStats = calc.removeOwner(owner);
					}
					
					if (calc.size() == 0)
					{
						_calculators[i] = null;
					}
				}
				i++;
			}
			
			if (isNpc())
			{
				i = 0;
				for (; i < Stats.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}
				
				if (i >= Stats.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}
			
			if (owner instanceof L2Effect)
			{
				if (!((L2Effect) owner).isPreventExitUpdate())
				{
					broadcastModifiedStats(modifiedStats);
				}
			}
			else
			{
				broadcastModifiedStats(modifiedStats);
			}
		}
	}
	
	protected void broadcastModifiedStats(List<Stats> stats)
	{
		if ((stats == null) || stats.isEmpty())
		{
			return;
		}
		
		if (isSummon())
		{
			L2Summon summon = (L2Summon) this;
			if (summon.getOwner() != null)
			{
				summon.updateAndBroadcastStatus(1);
			}
		}
		else
		{
			boolean broadcastFull = false;
			StatusUpdate su = new StatusUpdate(this);
			
			for (Stats stat : stats)
			{
				if (stat == Stats.POWER_ATTACK_SPEED)
				{
					su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
				}
				else if (stat == Stats.MAGIC_ATTACK_SPEED)
				{
					su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
				}
				else if (stat == Stats.MOVE_SPEED)
				{
					broadcastFull = true;
				}
			}
			
			if (isPlayer())
			{
				if (broadcastFull)
				{
					getActingPlayer().updateAndBroadcastStatus(2);
				}
				else
				{
					getActingPlayer().updateAndBroadcastStatus(1);
					if (su.hasAttributes())
					{
						broadcastPacket(su);
					}
				}
				if ((getSummon() != null) && isAffected(EffectFlag.SERVITOR_SHARE))
				{
					getSummon().broadcastStatusUpdate();
				}
			}
			else if (isNpc())
			{
				if (broadcastFull)
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
							player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
						}
						else
						{
							player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) this, player));
						}
					}
				}
				else if (su.hasAttributes())
				{
					broadcastPacket(su);
				}
			}
			else if (su.hasAttributes())
			{
				broadcastPacket(su);
			}
		}
	}
	
	@Override
	public final int getHeading()
	{
		return _heading;
	}
	
	public final void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public final int getXdestination()
	{
		MoveData m = _move;
		
		if (m != null)
		{
			return m._xDestination;
		}
		return getX();
	}
	
	public final int getYdestination()
	{
		MoveData m = _move;
		
		if (m != null)
		{
			return m._yDestination;
		}
		return getY();
	}
	
	public final int getZdestination()
	{
		MoveData m = _move;
		
		if (m != null)
		{
			return m._zDestination;
		}
		return getZ();
	}
	
	public boolean isInCombat()
	{
		return hasAI() && ((getAI().getAttackTarget() != null) || getAI().isAutoAttacking());
	}
	
	public final boolean isMoving()
	{
		return _move != null;
	}
	
	public final boolean isOnGeodataPath()
	{
		MoveData m = _move;
		if ((m == null) || (m.onGeodataPathIndex == -1))
		{
			return false;
		}
		return m.onGeodataPathIndex != (m.geoPath.size() - 1);
	}
	
	public final boolean isCastingNow()
	{
		return _isCastingNow;
	}
	
	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
	}
	
	public final boolean isCastingSimultaneouslyNow()
	{
		return _isCastingSimultaneouslyNow;
	}
	
	public void setIsCastingSimultaneouslyNow(boolean value)
	{
		_isCastingSimultaneouslyNow = value;
	}
	
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public int getCastInterruptTime()
	{
		return _castInterruptTime;
	}
	
	public boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public final void abortCast()
	{
		if (isCastingNow() || isCastingSimultaneouslyNow())
		{
			Future<?> future = _skillCast;
			if (future != null)
			{
				future.cancel(true);
				_skillCast = null;
			}
			future = _skillCast2;
			if (future != null)
			{
				future.cancel(true);
				_skillCast2 = null;
			}
			
			if (getFusionSkill() != null)
			{
				getFusionSkill().onCastAbort();
			}
			
			L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
			if (mog != null)
			{
				mog.exit();
			}
			
			if (_allSkillsDisabled)
			{
				enableAllSkills();
			}
			setIsCastingNow(false);
			setIsCastingSimultaneouslyNow(false);
			_castInterruptTime = 0;
			if (isPlayer())
			{
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
			}
			broadcastPacket(new MagicSkillCanceld(getObjectId()));
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public boolean updatePosition(int gameTicks)
	{
		MoveData m = _move;
		
		if (m == null)
		{
			return true;
		}
		
		if (!isVisible())
		{
			_move = null;
			return true;
		}
		
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}
		
		if (m._moveTimestamp == gameTicks)
		{
			return false;
		}
		
		int xPrev = getX();
		int yPrev = getY();
		int zPrev = getZ();
		
		double dx, dy, dz;
		if (Config.GEODATA)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}
		
		final boolean isFloating = isFlying() || isInsideZone(ZoneId.WATER);
		
		if ((Config.GEODATA) && !isFloating && !m.disregardingGeodata && ((GameTimeController.getInstance().getGameTicks() % 10) == 0))
		{
			int geoHeight = GeoClient.getInstance().getSpawnHeight(xPrev, yPrev, zPrev);
			dz = m._zDestination - geoHeight;
			if (isPlayer() && (Math.abs(getActingPlayer().getClientZ() - geoHeight) > 200) && (Math.abs(getActingPlayer().getClientZ() - geoHeight) < 1500))
			{
				dz = m._zDestination - zPrev;
			}
			else if (isInCombat() && (Math.abs(dz) > 200) && (((dx * dx) + (dy * dy)) < 40000))
			{
				dz = m._zDestination - zPrev;
			}
			else
			{
				zPrev = geoHeight;
			}
		}
		else
		{
			dz = m._zDestination - zPrev;
		}
		
		double delta = (dx * dx) + (dy * dy);
		if ((delta < 10000) && ((dz * dz) > 2500) && !isFloating)
		{
			delta = Math.sqrt(delta);
		}
		else
		{
			delta = Math.sqrt(delta + (dz * dz));
		}
		
		double distFraction = Double.MAX_VALUE;
		if (delta > 1)
		{
			final double distPassed = (getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp)) / GameTimeController.TICKS_PER_SECOND;
			distFraction = distPassed / delta;
		}
		
		if (distFraction > 1)
		{
			super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;
			
			super.getPosition().setXYZ((int) (m._xAccurate), (int) (m._yAccurate), zPrev + (int) ((dz * distFraction) + 0.5));
		}
		revalidateZone(false);
		m._moveTimestamp = gameTicks;
		
		return (distFraction > 1);
	}
	
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
	}
	
	public void stopMove(Location loc)
	{
		stopMove(loc, false);
	}
	
	public void stopMove(Location loc, boolean updateKnownObjects)
	{
		_move = null;
		
		if (loc != null)
		{
			getPosition().setXYZ(loc.getX(), loc.getY(), loc.getZ());
			setHeading(loc.getHeading());
			revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
		if (Config.MOVE_BASED_KNOWNLIST && updateKnownObjects)
		{
			getKnownList().findObjects();
		}
	}
	
	public void setTarget(L2Object object)
	{
		if ((object != null) && !object.isVisible())
		{
			object = null;
		}
		
		if ((object != null) && (object != _target))
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		_target = object;
	}
	
	public final int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}
		return -1;
	}
	
	public final L2Object getTarget()
	{
		return _target;
	}
	
	public void moveToLocation(int x, int y, int z, int offset)
	{
		float speed = getStat().getMoveSpeed();
		if ((speed <= 0) || isMovementDisabled())
		{
			return;
		}
		
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		
		double dx = (x - curX);
		double dy = (y - curY);
		double dz = (z - curZ);
		double distance = Math.sqrt((dx * dx) + (dy * dy));
		
		final boolean verticalMovementOnly = isFlying() && (distance == 0) && (dz != 0);
		if (verticalMovementOnly)
		{
			distance = Math.abs(dz);
		}
		
		if ((Config.GEODATA) && isInsideZone(ZoneId.WATER) && (distance > 700))
		{
			double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = (x - curX);
			dy = (y - curY);
			dz = (z - curZ);
			distance = Math.sqrt((dx * dx) + (dy * dy));
		}
		
		double cos;
		double sin;
		
		if ((offset > 0) || (distance < 1))
		{
			offset -= Math.abs(dz);
			if (offset < 5)
			{
				offset = 5;
			}
			
			if ((distance < 1) || ((distance - offset) <= 0))
			{
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				
				return;
			}
			sin = dy / distance;
			cos = dx / distance;
			
			distance -= (offset - 5);
			
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);
		}
		else
		{
			sin = dy / distance;
			cos = dx / distance;
		}
		MoveData m = new MoveData();
		m.onGeodataPathIndex = -1;
		m.disregardingGeodata = false;
		
		if ((Config.GEODATA) && !isFlying() && (!isInsideZone(ZoneId.WATER) || isInsideZone(ZoneId.SIEGE)))
		{
			final boolean isInVehicle = isPlayer() && (getActingPlayer().getVehicle() != null);
			if (isInVehicle)
			{
				m.disregardingGeodata = true;
			}
			
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = (originalX - L2World.MAP_MIN_X) >> 4;
			int gty = (originalY - L2World.MAP_MIN_Y) >> 4;
			
			if (((Config.GEODATA) && !(isL2Attackable() && ((L2Attackable) this).isReturningToSpawnPoint())) || (isPlayer() && !(isInVehicle && (distance > 1500))) || isSummon() || isNpc() || isMonster())
			{
				if (isOnGeodataPath())
				{
					try
					{
						if ((gtx == _move.geoPathGtx) && (gty == _move.geoPathGty))
						{
							return;
						}
						_move.onGeodataPathIndex = -1;
					}
					catch (NullPointerException e)
					{
					}
				}
				
				if ((curX < L2World.MAP_MIN_X) || (curX > L2World.MAP_MAX_X) || (curY < L2World.MAP_MIN_Y) || (curY > L2World.MAP_MAX_Y))
				{
					_log.warning("Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (isPlayer())
					{
						getActingPlayer().logout();
					}
					else if (isSummon())
					{
						return;
					}
					else
					{
						onDecay();
					}
					return;
				}
				
				Location destiny = GeoClient.getInstance().moveCheck(curX, curY, curZ, x, y, z, false);
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				dx = x - curX;
				dy = y - curY;
				dz = z - curZ;
				distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt((dx * dx) + (dy * dy));
			}
			
			if ((Config.GEODATA) && ((originalDistance - distance) > 5) && (distance < 2000) && !this.isAfraid())
			{
				if ((isPlayable() && !isInVehicle) || isPhantome() || isMinion() || isInCombat() || isMonster() || isSummon() || isNpc() || isL2Attackable())
				{
					m.geoPath = GeoClient.getInstance().pathFind(curX, curY, curZ, new Location(originalX, originalY, originalZ));
					if ((m.geoPath == null) || (m.geoPath.size() == 0))
					{
						m.disregardingGeodata = true;
						x = originalX;
						y = originalY;
						z = originalZ;
						distance = originalDistance;
					}
					else
					{
						m.onGeodataPathIndex = 0;
						m.geoPathGtx = gtx;
						m.geoPathGty = gty;
						m.geoPathAccurateTx = originalX;
						m.geoPathAccurateTy = originalY;
						
						x = m.geoPath.get(m.onGeodataPathIndex).getX();
						y = m.geoPath.get(m.onGeodataPathIndex).getY();
						z = m.geoPath.get(m.onGeodataPathIndex).getZ();
						
						if (checkIfDoorsBetween(new Location(x, y, z), null))
						{
							m.geoPath = null;
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						for (int i = 0; i < (m.geoPath.size() - 1); i++)
						{
							if (checkIfDoorsBetween(m.geoPath.get(i + 1), null))
							{
								m.geoPath = null;
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								return;
							}
						}
						dx = x - curX;
						dy = y - curY;
						dz = z - curZ;
						distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt((dx * dx) + (dy * dy));
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}
			
			if ((distance < 1) && ((Config.GEODATA) || isPlayable() || (this instanceof L2RiftInvaderInstance) || isAfraid()))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return;
			}
		}
		
		if ((isFlying() || isInsideZone(ZoneId.WATER)) && !verticalMovementOnly)
		{
			distance = Math.sqrt((distance * distance) + (dz * dz));
		}
		
		int ticksToMove = 1 + (int) ((GameTimeController.TICKS_PER_SECOND * distance) / speed);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z;
		m._heading = 0;
		if (!verticalMovementOnly)
		{
			setHeading(Util.calculateHeadingFrom(cos, sin));
		}
		
		m._moveStartTime = GameTimeController.getInstance().getGameTicks();
		_move = m;
		
		GameTimeController.getInstance().registerMovingObject(this);
		
		if ((ticksToMove * GameTimeController.MILLIS_IN_TICK) > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
	}
	
	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath())
		{
			_move = null;
			return false;
		}
		float speed = getStat().getMoveSpeed();
		if ((speed <= 0) || isMovementDisabled())
		{
			_move = null;
			return false;
		}
		
		MoveData md = _move;
		if (md == null)
		{
			return false;
		}
		
		MoveData m = new MoveData();
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1;
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;
		
		if (md.onGeodataPathIndex == (md.geoPath.size() - 2))
		{
			m._xDestination = md.geoPathAccurateTx;
			m._yDestination = md.geoPathAccurateTy;
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		else
		{
			m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		double dx = (m._xDestination - super.getX());
		double dy = (m._yDestination - super.getY());
		double distance = Math.sqrt((dx * dx) + (dy * dy));
		
		if (distance != 0)
		{
			setHeading(Util.calculateHeadingFrom(getX(), getY(), m._xDestination, m._yDestination));
		}
		
		int ticksToMove = 1 + (int) ((GameTimeController.TICKS_PER_SECOND * distance) / speed);
		
		m._heading = 0;
		m._moveStartTime = GameTimeController.getInstance().getGameTicks();
		
		_move = m;
		
		GameTimeController.getInstance().registerMovingObject(this);
		
		if ((ticksToMove * GameTimeController.MILLIS_IN_TICK) > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		MoveToLocation msg = new MoveToLocation(this);
		broadcastPacket(msg);
		
		return true;
	}
	
	public boolean validateMovementHeading(int heading)
	{
		MoveData m = _move;
		
		if (m == null)
		{
			return true;
		}
		
		boolean result = true;
		if (m._heading != heading)
		{
			result = (m._heading == 0);
			m._heading = heading;
		}
		return result;
	}
	
	@Deprecated
	public final double getDistance(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();
		
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	@Deprecated
	public final double getDistance(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		
		return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	public final double getDistanceSq(L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}
	
	public final double getDistanceSq(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		
		return ((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	public final double getPlanDistanceSq(L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}
	
	public final double getPlanDistanceSq(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();
		
		return ((dx * dx) + (dy * dy));
	}
	
	public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
	}
	
	public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}
	
	public final boolean isInsideRadius(Location loc, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(loc.getX(), loc.getY(), loc.getZ(), radius, checkZ, strictCheck);
	}
	
	public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		boolean isInsideRadius = false;
		if (strictCheck)
		{
			if (checkZ)
			{
				isInsideRadius = ((dx * dx) + (dy * dy) + (dz * dz)) < (radius * radius);
			}
			else
			{
				isInsideRadius = ((dx * dx) + (dy * dy)) < (radius * radius);
			}
		}
		else
		{
			if (checkZ)
			{
				isInsideRadius = ((dx * dx) + (dy * dy) + (dz * dz)) <= (radius * radius);
			}
			else
			{
				isInsideRadius = ((dx * dx) + (dy * dy)) <= (radius * radius);
			}
		}
		return isInsideRadius;
	}
	
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	protected boolean checkAndEquipBolts()
	{
		return true;
	}
	
	public void addExpAndSp(long addToExp, int addToSp)
	{
	}
	
	public abstract L2ItemInstance getActiveWeaponInstance();
	
	public abstract L2Weapon getActiveWeaponItem();
	
	public abstract L2ItemInstance getSecondaryWeaponInstance();
	
	public abstract L2Item getSecondaryWeaponItem();
	
	public void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		if ((target == null) || isAlikeDead() || (isNpc() && ((L2Npc) this).isEventMob()))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if ((isNpc() && target.isAlikeDead()) || target.isDead() || (!getKnownList().knowsObject(target) && !isDoor()))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (miss)
		{
			if (target.hasAI())
			{
				target.getAI().notifyEvent(CtrlEvent.EVT_EVADED, this);
			}
			
			if (target.getChanceSkills() != null)
			{
				target.getChanceSkills().onEvadedHit(this);
			}
		}
		sendDamageMessage(target, damage, false, crit, miss);
		
		if (target.isRaid() && target.giveRaidCurse() && !Config.RAID_DISABLE_CURSE)
		{
			if (getLevel() > (target.getLevel() + 8))
			{
				L2Skill skill = SkillHolder.FrequentSkill.RAID_CURSE2.getSkill();
				
				if (skill != null)
				{
					abortAttack();
					abortCast();
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					skill.getEffects(target, this);
				}
				else
				{
					_log.warning("Skill 4515 at level 1 is missing in DP.");
				}
				
				damage = 0;
			}
		}
		
		if (target.isPlayer())
		{
			L2PcInstance enemy = target.getActingPlayer();
			enemy.getAI().clientStartAutoAttack();
		}
		
		if (!miss && (damage > 0))
		{
			L2Weapon weapon = getActiveWeaponItem();
			boolean isBow = ((weapon != null) && ((weapon.getItemType() == L2WeaponType.BOW) || (weapon.getItemType() == L2WeaponType.CROSSBOW)));
			int reflectedDamage = 0;
			
			if (!isBow && !target.isInvul())
			{
				if (!target.isRaid() || (getActingPlayer() == null) || (getActingPlayer().getLevel() <= (target.getLevel() + 8)))
				{
					double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
					
					if (reflectPercent > 0)
					{
						reflectedDamage = (int) ((reflectPercent / 100.) * damage);
						
						if (reflectedDamage > target.getMaxHp())
						{
							reflectedDamage = target.getMaxHp();
						}
					}
				}
			}
			
			target.reduceCurrentHp(damage, this, null);
			target.notifyDamageReceived(damage, this, null, crit, false);
			
			if (reflectedDamage > 0)
			{
				reduceCurrentHp(reflectedDamage, target, true, false, null);
				notifyDamageReceived(reflectedDamage, target, null, crit, false);
			}
			
			if (!isBow)
			{
				double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
				
				if (absorbPercent > 0)
				{
					int maxCanAbsorb = (int) (getMaxRecoverableHp() - getCurrentHp());
					int absorbDamage = (int) ((absorbPercent / 100.) * damage);
					
					if (absorbDamage > maxCanAbsorb)
					{
						absorbDamage = maxCanAbsorb;
					}
					
					if (absorbDamage > 0)
					{
						setCurrentHp(getCurrentHp() + absorbDamage);
					}
				}
				
				absorbPercent = getStat().calcStat(Stats.ABSORB_MANA_DAMAGE_PERCENT, 0, null, null);
				
				if (absorbPercent > 0)
				{
					int maxCanAbsorb = (int) (getMaxRecoverableMp() - getCurrentMp());
					int absorbDamage = (int) ((absorbPercent / 100.) * damage);
					
					if (absorbDamage > maxCanAbsorb)
					{
						absorbDamage = maxCanAbsorb;
					}
					
					if (absorbDamage > 0)
					{
						setCurrentMp(getCurrentMp() + absorbDamage);
					}
				}
				
			}
			
			if (target.hasAI())
			{
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
			}
			getAI().clientStartAutoAttack();
			if (isSummon())
			{
				L2PcInstance owner = ((L2Summon) this).getOwner();
				if (owner != null)
				{
					owner.getAI().clientStartAutoAttack();
				}
			}
			
			if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
			{
				target.breakAttack();
				target.breakCast();
			}
			
			if (_chanceSkills != null)
			{
				_chanceSkills.onHit(target, damage, false, crit);
				if (reflectedDamage > 0)
				{
					_chanceSkills.onHit(target, reflectedDamage, true, false);
				}
			}
			
			if (_triggerSkills != null)
			{
				for (OptionsSkillHolder holder : _triggerSkills.values())
				{
					if ((!crit && (holder.getSkillType() == OptionsSkillType.ATTACK)) || ((holder.getSkillType() == OptionsSkillType.CRITICAL) && crit))
					{
						if (Rnd.get(100) < holder.getChance())
						{
							makeTriggerCast(holder.getSkill(), target);
						}
					}
				}
			}
			
			if (target.getChanceSkills() != null)
			{
				target.getChanceSkills().onHit(this, damage, true, crit);
			}
		}
		
		L2Weapon activeWeapon = getActiveWeaponItem();
		if (activeWeapon != null)
		{
			activeWeapon.getSkillEffects(this, target, crit);
		}
		
		if ((this instanceof L2EventMapGuardInstance) && (target instanceof L2PcInstance))
		{
			target.doDie(this);
		}
	}
	
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			abortAttack();
			
			if (isPlayer())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.ATTACK_FAILED);
			}
		}
	}
	
	public void breakCast()
	{
		if (isCastingNow() && canAbortCast() && (getLastSkillCast() != null) && (getLastSkillCast().isMagic() || getLastSkillCast().isStatic()))
		{
			abortCast();
			
			if (isPlayer())
			{
				sendPacket(SystemMessageId.CASTING_INTERRUPTED);
			}
		}
	}
	
	protected void reduceArrowCount(boolean bolts)
	{
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		L2PcInstance activeChar = getActingPlayer();
		
		if ((activeChar != null) && activeChar.isFightingInEvent() && activeChar.isInSameTeam(player))
		{
			if (((activeChar.getEventName().equals("CTF") || player.getEventName().equals("CTF")) && !Config.CTF_ALLOW_INTERFERENCE) || ((activeChar.getEventName().equals("BW") || player.getEventName().equals("BW")) && !Config.BW_ALLOW_INTERFERENCE))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (isInsidePeaceZone(player) && !player.isInTownWarEvent())
		{
			player.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode() && (player.getTarget() != null) && player.getTarget().isPlayable())
		{
			L2PcInstance target = null;
			L2Object object = player.getTarget();
			if ((object != null) && object.isPlayable())
			{
				target = object.getActingPlayer();
			}
			
			if ((target == null) || (target.isInOlympiadMode() && (!player.isOlympiadStart() || (player.getOlympiadGameId() != target.getOlympiadGameId()))))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if ((player.getTarget() != null) && !player.getTarget().isAttackable() && !player.getAccessLevel().allowPeaceAttack())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isConfused())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isBlocked())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!GeoClient.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(SystemMessageId.CANT_SEE_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getBlockCheckerArena() != -1)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
	}
	
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}
	
	public boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return (!attacker.getAccessLevel().allowPeaceAttack() && isInsidePeaceZone((L2Object) attacker, target));
	}
	
	public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null)
		{
			return false;
		}
		if (!(target.isPlayable() && attacker.isPlayable()))
		{
			return false;
		}
		if (InstanceManager.getInstance().getInstance(getInstanceId()).isPvPInstance())
		{
			return false;
		}
		
		if (TerritoryWarManager.PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE && TerritoryWarManager.getInstance().isTWInProgress())
		{
			if (target.isPlayer() && target.getActingPlayer().isCombatFlagEquipped())
			{
				return false;
			}
		}
		
		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			if ((target.getActingPlayer() != null) && (target.getActingPlayer().getKarma() > 0))
			{
				return false;
			}
			
			if ((attacker.getActingPlayer() != null) && (attacker.getActingPlayer().getKarma() > 0) && (target.getActingPlayer() != null) && (target.getActingPlayer().getPvpFlag() > 0))
			{
				return false;
			}
			
			if ((attacker instanceof L2Character) && (target instanceof L2Character))
			{
				return (target.isInsideZone(ZoneId.PEACE) || attacker.isInsideZone(ZoneId.PEACE));
			}
			
			if (attacker instanceof L2Character)
			{
				return ((TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null) || attacker.isInsideZone(ZoneId.PEACE));
			}
		}
		
		if ((attacker instanceof L2Character) && (target instanceof L2Character))
		{
			return (target.isInsideZone(ZoneId.PEACE) || attacker.isInsideZone(ZoneId.PEACE));
		}
		
		if (attacker instanceof L2Character)
		{
			return ((TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null) || attacker.isInsideZone(ZoneId.PEACE));
		}
		return ((TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null) || (TownManager.getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null));
	}
	
	public boolean isInActiveRegion()
	{
		L2WorldRegion region = getWorldRegion();
		return ((region != null) && (region.isActive()));
	}
	
	public boolean isInParty()
	{
		return false;
	}
	
	public L2Party getParty()
	{
		return null;
	}
	
	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		if ((weapon != null) && !isTransformed())
		{
			switch (weapon.getItemType())
			{
				case BOW:
					return (1500 * 345) / getPAtkSpd();
				case CROSSBOW:
					return (1200 * 345) / getPAtkSpd();
				case DAGGER:
					break;
			}
		}
		return Formulas.calcPAtkSpd(this, target, getPAtkSpd());
	}
	
	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if ((weapon == null) || isTransformed())
		{
			return 0;
		}
		
		int reuse = weapon.getReuseDelay();
		
		if (reuse == 0)
		{
			return 0;
		}
		
		reuse *= getStat().getWeaponReuseModifier(target);
		double atkSpd = getStat().getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
			case CROSSBOW:
				return (int) ((reuse * 345) / atkSpd);
			default:
				return (int) ((reuse * 312) / atkSpd);
		}
	}
	
	public boolean isUsingDualWeapon()
	{
		return false;
	}
	
	@Override
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			
			if (oldSkill != null)
			{
				if ((oldSkill.triggerAnotherSkill()))
				{
					removeSkill(oldSkill.getTriggeredId(), true);
				}
				removeStatsOwner(oldSkill);
			}
			addStatFuncs(newSkill.getStatFuncs(null, this));
			
			if ((oldSkill != null) && (_chanceSkills != null))
			{
				removeChanceSkill(oldSkill.getId());
			}
			
			if (newSkill.isChance())
			{
				addChanceTrigger(newSkill);
			}
			newSkill.getEffectsPassive(this);
		}
		return oldSkill;
	}
	
	public L2Skill removeSkill(L2Skill skill, boolean cancelEffect)
	{
		return (skill != null) ? removeSkill(skill.getId(), cancelEffect) : null;
	}
	
	public L2Skill removeSkill(int skillId)
	{
		return removeSkill(skillId, true);
	}
	
	public L2Skill removeSkill(int skillId, boolean cancelEffect)
	{
		L2Skill oldSkill = _skills.remove(skillId);
		if (oldSkill != null)
		{
			if ((oldSkill.triggerAnotherSkill()) && (oldSkill.getTriggeredId() > 0))
			{
				removeSkill(oldSkill.getTriggeredId(), true);
			}
			
			if ((getLastSkillCast() != null) && isCastingNow())
			{
				if (oldSkill.getId() == getLastSkillCast().getId())
				{
					abortCast();
				}
			}
			
			if ((getLastSimultaneousSkillCast() != null) && isCastingSimultaneouslyNow())
			{
				if (oldSkill.getId() == getLastSimultaneousSkillCast().getId())
				{
					abortCast();
				}
			}
			
			_effects.removePassiveEffects(skillId);
			
			if (cancelEffect || oldSkill.isToggle())
			{
				L2Effect e = getFirstEffect(oldSkill);
				if ((e == null) || (e.getEffectType() != L2EffectType.TRANSFORMATION))
				{
					removeStatsOwner(oldSkill);
					stopSkillEffects(oldSkill.getId());
				}
			}
			
			if (isPlayer())
			{
				if ((oldSkill instanceof L2SkillSummon) && (oldSkill.getId() == 710) && hasSummon() && (getSummon().getId() == 14870))
				{
					getActingPlayer().getSummon().unSummon(getActingPlayer());
				}
			}
			
			if (oldSkill.isChance() && (_chanceSkills != null))
			{
				removeChanceSkill(oldSkill.getId());
			}
		}
		return oldSkill;
	}
	
	public void removeChanceSkill(int id)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		synchronized (_chanceSkills)
		{
			for (IChanceSkillTrigger trigger : _chanceSkills.keySet())
			{
				if (!(trigger instanceof L2Skill))
				{
					continue;
				}
				if (((L2Skill) trigger).getId() == id)
				{
					_chanceSkills.remove(trigger);
				}
			}
		}
	}
	
	public void addChanceTrigger(IChanceSkillTrigger trigger)
	{
		if (_chanceSkills == null)
		{
			synchronized (this)
			{
				if (_chanceSkills == null)
				{
					_chanceSkills = new ChanceSkillList(this);
				}
			}
		}
		_chanceSkills.put(trigger, trigger.getTriggeredChanceCondition());
	}
	
	public void removeChanceEffect(IChanceSkillTrigger effect)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		_chanceSkills.remove(effect);
	}
	
	public void onStartChanceEffect(byte element)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		
		_chanceSkills.onStart(element);
	}
	
	public void onActionTimeChanceEffect(byte element)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		
		_chanceSkills.onActionTime(element);
	}
	
	public void onExitChanceEffect(byte element)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		
		_chanceSkills.onExit(element);
	}
	
	public final Collection<L2Skill> getAllSkills()
	{
		return new ArrayList<>(_skills.values());
	}
	
	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}
	
	@Override
	public int getSkillLevel(int skillId)
	{
		final L2Skill skill = getKnownSkill(skillId);
		
		return (skill == null) ? -1 : skill.getLevel();
	}
	
	@Override
	public final L2Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}
	
	public int getBuffCount()
	{
		return _effects.getBuffCount();
	}
	
	public int getDanceCount()
	{
		return _effects.getDanceCount();
	}
	
	public void onMagicLaunchedTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.getSkill();
		L2Object[] targets = mut.getTargets();
		
		if ((skill == null) || (targets == null))
		{
			abortCast();
			return;
		}
		
		if (targets.length == 0)
		{
			switch (skill.getTargetType())
			{
				case AURA:
				case FRONT_AURA:
				case BEHIND_AURA:
				case AURA_CORPSE_MOB:
					break;
				default:
					abortCast();
					return;
			}
		}
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
		{
			escapeRange = skill.getEffectRange();
		}
		else if ((skill.getCastRange() < 0) && (skill.getAffectRange() > 80))
		{
			escapeRange = skill.getAffectRange();
		}
		
		if ((targets.length > 0) && (escapeRange > 0))
		{
			int _skiprange = 0;
			int _skippeace = 0;
			List<L2Character> targetList = new FastList<>(targets.length);
			for (L2Object target : targets)
			{
				if (target instanceof L2Character)
				{
					if (!isInsideRadius(target.getX(), target.getY(), target.getZ(), escapeRange + getTemplate().getCollisionRadius(), true, false))
					{
						_skiprange++;
						continue;
					}
					
					if (skill.isOffensive() && !skill.isNeutral())
					{
						if (isPlayer())
						{
							if (((L2Character) target).isInsidePeaceZone(getActingPlayer()))
							{
								_skippeace++;
								continue;
							}
						}
						else
						{
							if (((L2Character) target).isInsidePeaceZone(this, target))
							{
								_skippeace++;
								continue;
							}
						}
					}
					targetList.add((L2Character) target);
				}
			}
			if (targetList.isEmpty())
			{
				if (isPlayer())
				{
					if (_skiprange > 0)
					{
						sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
					}
					else if (_skippeace > 0)
					{
						sendPacket(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE);
					}
				}
				abortCast();
				return;
			}
			mut.setTargets(targetList.toArray(new L2Character[targetList.size()]));
		}
		
		if ((mut.isSimultaneous() && !isCastingSimultaneouslyNow()) || (!mut.isSimultaneous() && !isCastingNow()) || (isAlikeDead() && !skill.isStatic()))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if (!skill.isStatic())
		{
			broadcastPacket(new MagicSkillLaunched(this, skill.getDisplayId(), skill.getDisplayLevel(), targets));
		}
		
		mut.setPhase(2);
		if (mut.getSkillTime() == 0)
		{
			onMagicHitTimer(mut);
		}
		else
		{
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, 400);
		}
	}
	
	public void onMagicHitTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.getSkill();
		final L2Object[] targets = mut.getTargets();
		
		if ((skill == null) || (targets == null))
		{
			abortCast();
			return;
		}
		
		if (getFusionSkill() != null)
		{
			if (mut.isSimultaneous())
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			getFusionSkill().onCastAbort();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}
		L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			if (mut.isSimultaneous())
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			mog.exit();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}
		
		try
		{
			for (L2Object tgt : targets)
			{
				if (tgt.isPlayable())
				{
					L2Character target = (L2Character) tgt;
					
					if (skill.getSkillType() == L2SkillType.BUFF)
					{
						SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						smsg.addSkillName(skill);
						target.sendPacket(smsg);
					}
					
					if (isPlayer() && target.isSummon())
					{
						((L2Summon) target).updateAndBroadcastStatus(1);
					}
				}
			}
			
			StatusUpdate su = new StatusUpdate(this);
			boolean isSendStatus = false;
			double mpConsume = getStat().getMpConsume(skill);
			
			if (mpConsume > 0)
			{
				if (mpConsume > getCurrentMp())
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_MP);
					abortCast();
					return;
				}
				
				getStatus().reduceMp(mpConsume);
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}
			
			if (skill.getHpConsume() > 0)
			{
				double consumeHp = skill.getHpConsume();
				if (consumeHp >= getCurrentHp())
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_HP);
					abortCast();
					return;
				}
				getStatus().reduceHp(consumeHp, this, true);
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				isSendStatus = true;
			}
			
			if (isSendStatus)
			{
				sendPacket(su);
			}
			
			if (isPlayer())
			{
				if (skill.getChargeConsume() > 0)
				{
					getActingPlayer().decreaseCharges(skill.getChargeConsume());
				}
				
				if (skill.getMaxSoulConsumeCount() > 0)
				{
					if (!getActingPlayer().decreaseSouls(skill.getMaxSoulConsumeCount(), skill))
					{
						abortCast();
						return;
					}
				}
			}
			
			if ((this instanceof L2PcInstance) && (_target instanceof L2PcInstance))
			{
				if (Interface.isParticipating(getObjectId()))
				{
					Interface.onHit(getObjectId(), _target.getObjectId());
				}
			}
			
			if (mut.getCount() > 0)
			{
				final L2ItemInstance weaponInst = getActiveWeaponInstance();
				if (weaponInst != null)
				{
					if (mut.getSkill().useSoulShot())
					{
						setChargedShot(ShotType.SOULSHOTS, true);
					}
					else if (mut.getSkill().useSpiritShot())
					{
						setChargedShot(ShotType.SPIRITSHOTS, true);
					}
				}
			}
			callSkill(mut.getSkill(), mut.getTargets());
		}
		catch (NullPointerException e)
		{
			_log.log(Level.WARNING, "", e);
		}
		
		if (mut.getSkillTime() > 0)
		{
			mut.setCount(mut.getCount() + 1);
			if (mut.getCount() < skill.getHitCounts())
			{
				int skillTime = (mut.getSkillTime() * skill.getHitTimings()[mut.getCount()]) / 100;
				if (mut.isSimultaneous())
				{
					_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, skillTime);
				}
				else
				{
					_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, skillTime);
				}
				return;
			}
		}
		
		mut.setPhase(3);
		if (mut.getSkillTime() == 0)
		{
			onMagicFinalizer(mut);
		}
		else
		{
			if (mut.isSimultaneous())
			{
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, 0);
			}
			else
			{
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, 0);
			}
		}
	}
	
	public void onMagicFinalizer(MagicUseTask mut)
	{
		if (mut.isSimultaneous())
		{
			_skillCast2 = null;
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		
		_skillCast = null;
		setIsCastingNow(false);
		_castInterruptTime = 0;
		
		final L2Skill skill = mut.getSkill();
		final L2Object target = mut.getTargets().length > 0 ? mut.getTargets()[0] : null;
		
		if ((skill.nextActionIsAttack()) && (getTarget() instanceof L2Character) && (getTarget() != this) && (target != null) && (getTarget() == target) && target.isAttackable())
		{
			if ((getAI() == null) || (getAI().getNextIntention() == null) || (getAI().getNextIntention().getCtrlIntention() != CtrlIntention.AI_INTENTION_MOVE_TO))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		
		if (skill.isOffensive() && !skill.isNeutral())
		{
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
					getAI().clientStartAutoAttack();
					break;
				}
			}
		}
		
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
		notifyQuestEventSkillFinished(skill, target);
		
		if (isPlayer())
		{
			L2PcInstance currPlayer = getActingPlayer();
			SkillUseHolder queuedSkill = currPlayer.getQueuedSkill();
			
			currPlayer.setCurrentSkill(null, false, false);
			
			if (queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);
				ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
			}
		}
	}
	
	protected void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{
	}
	
	public L2TIntObjectHashMap<Long> getDisabledSkills()
	{
		return _disabledSkills;
	}
	
	public void enableSkill(L2Skill skill)
	{
		if ((skill == null) || (_disabledSkills == null))
		{
			return;
		}
		
		_disabledSkills.remove(Integer.valueOf(skill.getReuseHashCode()));
	}
	
	public void disableSkill(L2Skill skill, long delay)
	{
		if (skill == null)
		{
			return;
		}
		
		if (_disabledSkills == null)
		{
			_disabledSkills = new L2TIntObjectHashMap<>();
		}
		
		_disabledSkills.put(Integer.valueOf(skill.getReuseHashCode()), delay > 10 ? System.currentTimeMillis() + delay : Long.MAX_VALUE);
	}
	
	public boolean isSkillDisabled(L2Skill skill)
	{
		if (skill == null)
		{
			return true;
		}
		
		return isSkillDisabled(skill.getReuseHashCode());
	}
	
	public boolean isSkillDisabled(int reuseHashcode)
	{
		if (isAllSkillsDisabled())
		{
			return true;
		}
		
		if (_disabledSkills == null)
		{
			return false;
		}
		
		final Long timeStamp = _disabledSkills.get(Integer.valueOf(reuseHashcode));
		if (timeStamp == null)
		{
			return false;
		}
		
		if (timeStamp < System.currentTimeMillis())
		{
			_disabledSkills.remove(Integer.valueOf(reuseHashcode));
			return false;
		}
		
		return true;
	}
	
	public void disableAllSkills()
	{
		_allSkillsDisabled = true;
	}
	
	public void enableAllSkills()
	{
		_allSkillsDisabled = false;
	}
	
	public void callSkill(L2Skill skill, L2Object[] targets)
	{
		try
		{
			L2Weapon activeWeapon = getActiveWeaponItem();
			
			if (skill.isToggle() && (getFirstEffect(skill.getId()) != null))
			{
				return;
			}
			
			for (L2Object trg : targets)
			{
				if (trg instanceof L2Character)
				{
					L2Character target = (L2Character) trg;
					L2Character targetsAttackTarget = null;
					L2Character targetsCastTarget = null;
					if (target.hasAI())
					{
						targetsAttackTarget = target.getAI().getAttackTarget();
						targetsCastTarget = target.getAI().getCastTarget();
					}
					if (!Config.RAID_DISABLE_CURSE && ((target.isRaid() && target.giveRaidCurse() && (getLevel() > (target.getLevel() + 8))) || (!skill.isOffensive() && (targetsAttackTarget != null) && targetsAttackTarget.isRaid() && targetsAttackTarget.giveRaidCurse() && targetsAttackTarget.getAttackByList().contains(target) && (getLevel() > (targetsAttackTarget.getLevel() + 8))) || (!skill.isOffensive() && (targetsCastTarget != null) && targetsCastTarget.isRaid() && targetsCastTarget.giveRaidCurse() && targetsCastTarget.getAttackByList().contains(target) && (getLevel() > (targetsCastTarget.getLevel() + 8)))))
					{
						if (skill.isMagic())
						{
							L2Skill tempSkill = SkillHolder.FrequentSkill.RAID_CURSE.getSkill();
							if (tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
							else if (_log.isLoggable(Level.WARNING))
							{
								_log.log(Level.WARNING, "Skill 4215 at level 1 is missing in DP.");
							}
						}
						else
						{
							L2Skill tempSkill = SkillHolder.FrequentSkill.RAID_CURSE2.getSkill();
							if (tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
							else if (_log.isLoggable(Level.WARNING))
							{
								_log.log(Level.WARNING, "Skill 4515 at level 1 is missing in DP.");
							}
						}
						return;
					}
					
					if (skill.isOverhit())
					{
						if (target.isL2Attackable())
						{
							((L2Attackable) target).overhitEnabled(true);
						}
					}
					
					if (!skill.isStatic())
					{
						if ((activeWeapon != null) && !target.isDead())
						{
							if ((activeWeapon.getSkillEffects(this, target, skill).length > 0) && isPlayer())
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED);
								sm.addSkillName(skill);
								sendPacket(sm);
							}
						}
						
						if (_chanceSkills != null)
						{
							_chanceSkills.onSkillHit(target, skill, false);
						}
						
						if (target.getChanceSkills() != null)
						{
							target.getChanceSkills().onSkillHit(this, skill, true);
						}
						
						if (_triggerSkills != null)
						{
							for (OptionsSkillHolder holder : _triggerSkills.values())
							{
								if ((skill.isMagic() && (holder.getSkillType() == OptionsSkillType.MAGIC)) || (skill.isPhysical() && (holder.getSkillType() == OptionsSkillType.ATTACK)))
								{
									if (Rnd.get(100) < holder.getChance())
									{
										makeTriggerCast(holder.getSkill(), target);
									}
								}
							}
						}
					}
				}
			}
			
			final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
			if (handler != null)
			{
				handler.useSkill(this, skill, targets);
			}
			else
			{
				skill.useSkill(this, targets);
			}
			
			L2PcInstance player = getActingPlayer();
			if (player != null)
			{
				for (L2Object target : targets)
				{
					if (target instanceof L2Character)
					{
						if (skill.isNeutral())
						{
						}
						else if (skill.isOffensive())
						{
							if (target.isPlayer() || target.isSummon() || target.isTrap())
							{
								if ((skill.getSkillType() != L2SkillType.SIGNET) && (skill.getSkillType() != L2SkillType.SIGNET_CASTTIME))
								{
									if (target.isPlayer())
									{
										target.getActingPlayer().getAI().clientStartAutoAttack();
									}
									else if (target.isSummon() && ((L2Character) target).hasAI())
									{
										L2PcInstance owner = ((L2Summon) target).getOwner();
										if (owner != null)
										{
											owner.getAI().clientStartAutoAttack();
										}
									}
									
									if ((player.getSummon() != target) && !isTrap())
									{
										player.updatePvPStatus((L2Character) target);
									}
								}
							}
							else if (target.isL2Attackable())
							{
								switch (skill.getId())
								{
									case 51:
									case 511:
										break;
									default:
										((L2Character) target).addAttackerToAttackByList(this);
								}
							}
							
							if (((L2Character) target).hasAI())
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
										break;
									default:
										((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
								}
							}
						}
						else
						{
							if (target.isPlayer())
							{
								if (!(target.equals(this) || target.equals(player)) && ((target.getActingPlayer().getPvpFlag() > 0) || (target.getActingPlayer().getKarma() > 0)))
								{
									player.updatePvPStatus();
								}
							}
							else if (target.isL2Attackable())
							{
								switch (skill.getSkillType())
								{
									case SUMMON:
									case UNLOCK:
									case DELUXE_KEY_UNLOCK:
									case UNLOCK_SPECIAL:
										break;
									default:
										player.updatePvPStatus();
								}
							}
						}
					}
				}
				
				Collection<L2Object> objs = player.getKnownList().getKnownObjects().values();
				for (L2Object spMob : objs)
				{
					if ((spMob != null) && spMob.isNpc())
					{
						L2Npc npcMob = (L2Npc) spMob;
						
						if ((npcMob.isInsideRadius(player, 1000, true, true)) && (npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null))
						{
							for (Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
							{
								quest.notifySkillSee(npcMob, player, skill, targets, isSummon());
							}
						}
					}
				}
			}
			
			if (skill.isOffensive())
			{
				switch (skill.getSkillType())
				{
					case AGGREDUCE:
					case AGGREDUCE_CHAR:
					case AGGREMOVE:
						break;
					default:
						for (L2Object target : targets)
						{
							if ((target instanceof L2Character) && ((L2Character) target).hasAI())
							{
								((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
							}
						}
						break;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": callSkill() failed.", e);
		}
	}
	
	public boolean isBehind(L2Object target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 60;
		
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(this, target1);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= (-360 + maxAngleDiff))
			{
				angleDiff += 360;
			}
			if (angleDiff >= (360 - maxAngleDiff))
			{
				angleDiff -= 360;
			}
			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isBehindTarget()
	{
		return isBehind(getTarget());
	}
	
	public boolean isInFrontOf(L2Character target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 60;
		if (target == null)
		{
			return false;
		}
		
		angleTarget = Util.calculateAngleFrom(target, this);
		angleChar = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-360 + maxAngleDiff))
		{
			angleDiff += 360;
		}
		if (angleDiff >= (360 - maxAngleDiff))
		{
			angleDiff -= 360;
		}
		
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	public boolean isFacing(L2Object target, int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if (target == null)
		{
			return false;
		}
		maxAngleDiff = maxAngle / 2.;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-360 + maxAngleDiff))
		{
			angleDiff += 360;
		}
		if (angleDiff >= (360 - maxAngleDiff))
		{
			angleDiff -= 360;
		}
		
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	public boolean isInFrontOfTarget()
	{
		L2Object target = getTarget();
		if (target instanceof L2Character)
		{
			return isInFrontOf((L2Character) target);
		}
		return false;
	}
	
	public double getLevelMod()
	{
		return ((getLevel() + 89) / 100d);
	}
	
	public final void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}
	
	public final void forceIsCasting(int newSkillCastEndTick)
	{
		setIsCastingNow(true);
		_castInterruptTime = newSkillCastEndTick - 4;
	}
	
	private boolean _AIdisabled = false;
	
	public void updatePvPFlag(int value)
	{
	}
	
	public final double getRandomDamageMultiplier()
	{
		L2Weapon activeWeapon = getActiveWeaponItem();
		int random;
		
		if (activeWeapon != null)
		{
			random = activeWeapon.getRandomDamage();
		}
		else
		{
			random = 5 + (int) Math.sqrt(getLevel());
		}
		
		return (1 + ((double) Rnd.get(0 - random, random) / 100));
	}
	
	public int getAttackEndTime()
	{
		return _attackEndTime;
	}
	
	public int getBowAttackEndTime()
	{
		return _disableBowAttackEndTime;
	}
	
	public abstract int getLevel();
	
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}
	
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	public final float getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}
	
	public int getCON()
	{
		return getStat().getCON();
	}
	
	public int getDEX()
	{
		return getStat().getDEX();
	}
	
	public final double getCriticalDmg(L2Character target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}
	
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}
	
	public int getINT()
	{
		return getStat().getINT();
	}
	
	public final int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}
	
	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}
	
	public final int getMaxRecoverableCp()
	{
		return getStat().getMaxRecoverableCp();
	}
	
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}
	
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
	
	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}
	
	public int getMaxRecoverableMp()
	{
		return getStat().getMaxRecoverableMp();
	}
	
	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}
	
	public int getMaxRecoverableHp()
	{
		return getStat().getMaxRecoverableHp();
	}
	
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}
	
	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}
	
	public int getMEN()
	{
		return getStat().getMEN();
	}
	
	public double getMReuseRate(L2Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}
	
	public float getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}
	
	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}
	
	public double getPAtkAnimals(L2Character target)
	{
		return getStat().getPAtkAnimals(target);
	}
	
	public double getPAtkDragons(L2Character target)
	{
		return getStat().getPAtkDragons(target);
	}
	
	public double getPAtkInsects(L2Character target)
	{
		return getStat().getPAtkInsects(target);
	}
	
	public double getPAtkMonsters(L2Character target)
	{
		return getStat().getPAtkMonsters(target);
	}
	
	public double getPAtkPlants(L2Character target)
	{
		return getStat().getPAtkPlants(target);
	}
	
	public double getPAtkGiants(L2Character target)
	{
		return getStat().getPAtkGiants(target);
	}
	
	public double getPAtkMagicCreatures(L2Character target)
	{
		return getStat().getPAtkMagicCreatures(target);
	}
	
	public double getPDefAnimals(L2Character target)
	{
		return getStat().getPDefAnimals(target);
	}
	
	public double getPDefDragons(L2Character target)
	{
		return getStat().getPDefDragons(target);
	}
	
	public double getPDefInsects(L2Character target)
	{
		return getStat().getPDefInsects(target);
	}
	
	public double getPDefMonsters(L2Character target)
	{
		return getStat().getPDefMonsters(target);
	}
	
	public double getPDefPlants(L2Character target)
	{
		return getStat().getPDefPlants(target);
	}
	
	public double getPDefGiants(L2Character target)
	{
		return getStat().getPDefGiants(target);
	}
	
	public double getPDefMagicCreatures(L2Character target)
	{
		return getStat().getPDefMagicCreatures(target);
	}
	
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}
	
	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}
	
	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}
	
	public int getSwimRunSpeed()
	{
		return getStat().getSwimRunSpeed();
	}
	
	public final int getShldDef()
	{
		return getStat().getShldDef();
	}
	
	public int getSTR()
	{
		return getStat().getSTR();
	}
	
	public final int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}
	
	public final int getSwimWalkSpeed()
	{
		return getStat().getSwimWalkSpeed();
	}
	
	public int getWIT()
	{
		return getStat().getWIT();
	}
	
	public double getRExp()
	{
		return getStat().getRExp();
	}
	
	public double getRSp()
	{
		return getStat().getRSp();
	}
	
	public void addStatusListener(L2Character object)
	{
		getStatus().addStatusListener(object);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}
	
	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, !skill.isToggle(), true, skill);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (Config.CHAMPION_ENABLE && isChampion() && (Config.CHAMPION_HP != 0))
		{
			getStatus().reduceHp(i / Config.CHAMPION_HP, attacker, awake, isDOT, false);
		}
		else
		{
			getStatus().reduceHp(i, attacker, awake, isDOT, false);
		}
	}
	
	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}
	
	@Override
	public void removeStatusListener(L2Character object)
	{
		getStatus().removeStatusListener(object);
	}
	
	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}
	
	public final double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}
	
	public final void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}
	
	public final void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}
	
	public final double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}
	
	public final void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}
	
	public final double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}
	
	public final void setCurrentMp(Double newMp)
	{
		setCurrentMp((double) newMp);
	}
	
	public final void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}
	
	public int getMaxLoad()
	{
		if (isPlayer() || isPet())
		{
			double baseLoad = Math.floor(BaseStats.CON.calcBonus(this) * 69000 * Config.ALT_WEIGHT_LIMIT);
			return (int) calcStat(Stats.WEIGHT_LIMIT, baseLoad, this, null);
		}
		return 0;
	}
	
	public int getBonusWeightPenalty()
	{
		if (isPlayer() || isPet())
		{
			return (int) calcStat(Stats.WEIGHT_PENALTY, 1, this, null);
		}
		return 0;
	}
	
	public int getCurrentLoad()
	{
		if (isPlayer() || isPet())
		{
			return getInventory().getTotalWeight();
		}
		return 0;
	}
	
	public boolean isChampion()
	{
		return _champion;
	}
	
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}
	
	public int getMaxBuffCount()
	{
		final L2Effect effect = getFirstPassiveEffect(L2EffectType.ENLARGE_ABNORMAL_SLOT);
		return Config.BUFFS_MAX_AMOUNT + (effect == null ? 0 : (int) effect.calc());
	}
	
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}
	
	public FusionSkill getFusionSkill()
	{
		return _fusionSkill;
	}
	
	public void setFusionSkill(FusionSkill fb)
	{
		_fusionSkill = fb;
	}
	
	public byte getAttackElement()
	{
		return getStat().getAttackElement();
	}
	
	public int getAttackElementValue(byte attackAttribute)
	{
		return getStat().getAttackElementValue(attackAttribute);
	}
	
	public int getDefenseElementValue(byte defenseAttribute)
	{
		return getStat().getDefenseElementValue(defenseAttribute);
	}
	
	public final void startPhysicalAttackMuted()
	{
		abortAttack();
	}
	
	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}
	
	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}
	
	public boolean giveRaidCurse()
	{
		return true;
	}
	
	public boolean isAffected(EffectFlag flag)
	{
		return _effects.isAffected(flag);
	}
	
	public void broadcastSocialAction(int id)
	{
		broadcastPacket(new SocialAction(getObjectId(), id));
	}
	
	public int getTeam()
	{
		return _team;
	}
	
	public void setTeam(int id)
	{
		if ((id >= 0) && (id <= 2))
		{
			_team = id;
		}
	}
	
	public void addOverrideCond(PcCondOverride... excs)
	{
		for (PcCondOverride exc : excs)
		{
			_exceptions |= exc.getMask();
		}
	}
	
	public void removeOverridedCond(PcCondOverride... excs)
	{
		for (PcCondOverride exc : excs)
		{
			_exceptions &= ~exc.getMask();
		}
	}
	
	public boolean canOverrideCond(PcCondOverride excs)
	{
		return (_exceptions & excs.getMask()) == excs.getMask();
	}
	
	public void setOverrideCond(long masks)
	{
		_exceptions = masks;
	}
	
	public Map<Integer, OptionsSkillHolder> getTriggerSkills()
	{
		if (_triggerSkills == null)
		{
			synchronized (this)
			{
				if (_triggerSkills == null)
				{
					_triggerSkills = new FastMap<Integer, OptionsSkillHolder>().shared();
				}
			}
		}
		return _triggerSkills;
	}
	
	public void addTriggerSkill(OptionsSkillHolder holder)
	{
		getTriggerSkills().put(holder.getSkillId(), holder);
	}
	
	public void removeTriggerSkill(OptionsSkillHolder holder)
	{
		getTriggerSkills().remove(holder.getSkillId());
	}
	
	public void makeTriggerCast(L2Skill skill, L2Character target)
	{
		try
		{
			if (skill.checkCondition(this, target, false))
			{
				if (skill.triggersChanceSkill())
				{
					skill = SkillHolder.getInstance().getInfo(skill.getTriggeredChanceId(), skill.getTriggeredChanceLevel());
					if ((skill == null) || (skill.getSkillType() == L2SkillType.NOTDONE))
					{
						return;
					}
					
					if (!skill.checkCondition(this, target, false))
					{
						return;
					}
				}
				
				if (isSkillDisabled(skill))
				{
					return;
				}
				
				if (skill.getReuseDelay() > 0)
				{
					disableSkill(skill, skill.getReuseDelay());
				}
				
				final L2Object[] targets = skill.getTargetList(this, false, target);
				
				if (targets.length == 0)
				{
					return;
				}
				
				final L2Character firstTarget = (L2Character) targets[0];
				
				if (Config.ALT_VALIDATE_TRIGGER_SKILLS && isPlayable() && (firstTarget != null) && firstTarget.isPlayable())
				{
					final L2PcInstance player = getActingPlayer();
					if (!player.checkPvpSkill(firstTarget, skill, isSummon()))
					{
						return;
					}
				}
				
				broadcastPacket(new MagicSkillLaunched(this, skill.getDisplayId(), skill.getLevel(), targets));
				broadcastPacket(new MagicSkillUse(this, firstTarget, skill.getDisplayId(), skill.getLevel(), 0, 0));
				
				final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
				if (handler != null)
				{
					handler.useSkill(this, skill, targets);
				}
				else
				{
					skill.useSkill(this, targets);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
	
	public boolean canRevive()
	{
		return true;
	}
	
	public void setCanRevive(boolean val)
	{
	}
	
	public boolean isSweepActive()
	{
		return false;
	}
	
	public boolean isOnEvent()
	{
		return false;
	}
	
	public int getClanId()
	{
		return 0;
	}
	
	public int getAllyId()
	{
		return 0;
	}
	
	public void notifyDamageReceived(double damage, L2Character attacker, L2Skill skill, boolean critical, boolean damageOverTime)
	{
		getEvents().onDamageReceived(damage, attacker, skill, critical, damageOverTime);
		attacker.getEvents().onDamageDealt(damage, this, skill, critical, damageOverTime);
	}
	
	public final boolean isNoLethal()
	{
		return _isNoLethal;
	}
	
	public final void setIsNoLethal(boolean value)
	{
		_isNoLethal = value;
	}
	
	public final boolean isAttackDisabled()
	{
		return _isAttackDisabled;
	}
	
	public final void setIsAttackDisabled(boolean value)
	{
		_isAttackDisabled = value;
	}
	
	public void setPremiumService(int PS)
	{
		_PremiumService = PS;
	}
	
	public int getPremiumService()
	{
		return _PremiumService;
	}
	
	public void block()
	{
		_blocked = true;
	}
	
	public void unblock()
	{
		_blocked = false;
	}
	
	public boolean isBlocked()
	{
		return _blocked;
	}
	
	public boolean isInCategory(CategoryType type)
	{
		return false;
	}
	
	@Override
	public boolean isCharacter()
	{
		return true;
	}
	
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}
	
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}
	
	public boolean checkIfDoorsBetween(Location toPos, L2Object target)
	{
		if (!(this instanceof L2Playable) && !(this instanceof L2Attackable))
		{
			return false;
		}
		
		if (getWorldRegion() == null)
		{
			return false;
		}
		
		if (_oldReg != getWorldRegion())
		{
			_lock.lock();
			try
			{
				_oldReg = getWorldRegion();
				_doors = L2World.getVisibleDoors(this);
			}
			finally
			{
				_lock.unlock();
			}
		}
		
		if ((_doors == null) || (_doors.size() == 0))
		{
			return false;
		}
		
		int skipDoor = 0;
		if (target instanceof L2DoorInstance)
		{
			return false;
		}
		
		Location pos = getLocation();
		
		for (L2DoorInstance door : _doors)
		{
			if (getInstanceId() != door.getInstanceId())
			{
				continue;
			}
			
			if ((door == null) || (door.getStatus().getCurrentHp() <= 0) || (door.getOpen() && !door.isGeoReverted()) || (!door.getOpen() && door.isGeoReverted()))
			{
				continue;
			}
			
			if ((skipDoor > 0) && (skipDoor == door.getDoorId()))
			{
				continue;
			}
			
			int res = door.getRange().findIntersect(pos, toPos);
			if (res > -1)
			{
				return true;
			}
		}
		return false;
	}
	
	public void setLoc(Location loc)
	{
		getPosition().setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public Location getFlyLocation(L2Character target, L2Skill skill)
	{
		if ((target != null) && (target != this))
		{
			Location loc;
			
			double radian = PositionUtils.convertHeadingToRadian(target.getHeading());
			if (skill.isFlyToBack())
			{
				loc = new Location(target.getX() + (int) (Math.sin(radian) * 60), target.getY() - (int) (Math.cos(radian) * 60), target.getZ());
			}
			else
			{
				loc = new Location(target.getX() - (int) (Math.sin(radian) * 60), target.getY() + (int) (Math.cos(radian) * 60), target.getZ());
			}
			
			if (isFlying())
			{
				if (isPlayer() && isTransformed() && ((loc.getZ() <= 0) || (loc.getZ() >= 6000)))
				{
					return null;
				}
			}
			else
			{
				loc.correctGeoZ();
				if (!GeoClient.getInstance().canMoveToCoord(getX(), getY(), getZ(), loc.getX(), loc.getY(), loc.getZ(), true))
				{
					loc = target.getLoc();
					if (!GeoClient.getInstance().canMoveToCoord(getX(), getY(), getZ(), loc.getX(), loc.getY(), loc.getZ(), true))
					{
						return null;
					}
				}
			}
			return loc;
		}
		
		double radian = PositionUtils.convertHeadingToRadian(getHeading());
		int x1 = -(int) (Math.sin(radian) * skill.getFlyRadius());
		int y1 = (int) (Math.cos(radian) * skill.getFlyRadius());
		
		if (isFlying())
		{
			return GeoClient.getInstance().moveCheckInAir(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ(), getTemplate().getCollisionRadius());
		}
		
		if (skill.getId() == 1448)
		{
			return GeoClient.getInstance().moveCheck(getX(), getY(), getZ(), getX() - x1, getY() - y1, getZ(), true);
		}
		return GeoClient.getInstance().moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ(), true);
	}
	
	public boolean isInZonePeace()
	{
		return isInsideZone(ZoneId.PEACE);
	}
	
	public void sayString(String text, int type)
	{
		broadcastPacket(new CreatureSay(getObjectId(), type, getName(), text));
	}
	
	@Override
	public boolean isPhantome()
	{
		return _phantome;
	}
	
	public void setPhantome(boolean p)
	{
		_phantome = p;
	}
	
	public Location getFakeLoc()
	{
		return null;
	}
	
	public void rndWalk()
	{
		int posX = getX();
		int posY = getY();
		int posZ = getZ();
		switch (Rnd.get(1, 6))
		{
			case 1:
				posX += 40;
				posY += 180;
				break;
			case 2:
				posX += 150;
				posY += 50;
				break;
			case 3:
				posX += 69;
				posY -= 100;
				break;
			case 4:
				posX += 10;
				posY -= 100;
				break;
			case 5:
				posX -= 150;
				posY -= 20;
				break;
			case 6:
				posX -= 100;
				posY += 60;
		}
		
		if (GeoClient.getInstance().canMoveToCoord(getX(), getY(), getZ(), posX, posY, posZ, true))
		{
			setRunning();
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(posX, posY, posZ));
		}
	}
	
	public int calcHeading(int x, int y)
	{
		return (int) (Math.atan2(getY() - y, getX() - x) * 10430.378350470453D) + 32768;
	}
	
	public void teleToClosestTown()
	{
	}
}