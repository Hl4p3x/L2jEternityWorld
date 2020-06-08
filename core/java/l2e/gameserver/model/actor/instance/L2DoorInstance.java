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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javolution.util.FastList;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.L2CharacterAI;
import l2e.gameserver.ai.L2DoorAI;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Range;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.knownlist.DoorKnownList;
import l2e.gameserver.model.actor.status.DoorStatus;
import l2e.gameserver.model.actor.templates.L2DoorTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.DoorStatusUpdate;
import l2e.gameserver.network.serverpackets.OnEventTrigger;
import l2e.gameserver.network.serverpackets.StaticObject;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.geoserver.model.GeoCollision;
import l2e.geoserver.model.GeoShape;
import l2e.util.Rnd;

public class L2DoorInstance extends L2Character implements GeoCollision
{
	private static final byte OPEN_BY_CLICK = 1;
	private static final byte OPEN_BY_TIME = 2;
	private static final byte OPEN_BY_ITEM = 4;
	private static final byte OPEN_BY_SKILL = 8;
	private static final byte OPEN_BY_CYCLE = 16;
	
	private int _castleIndex = -2;
	private int _fortIndex = -2;
	private ClanHall _clanHall;
	private boolean _open = false;
	private boolean _isAttackableDoor = false;
	private boolean _isTargetable;
	private final boolean _checkCollision;
	private int _openType = 0;
	private int _meshindex = 1;
	private int _level = 0;
	protected int _closeTime = -1;
	protected int _openTime = -1;
	protected int _randomTime = -1;
	private Future<?> _autoCloseTask;
	private L2Range _range;
	private boolean _isGeoReverted = false;
	
	public L2DoorInstance(int objectId, L2DoorTemplate template, StatsSet data)
	{
		super(objectId, template);
		
		setInstanceType(InstanceType.L2DoorInstance);
		setIsInvul(false);
		_isTargetable = data.getBool("targetable", true);
		if (getGroupName() != null)
		{
			DoorParser.addDoorGroup(getGroupName(), getDoorId());
		}
		if (data.getString("default_status", "close").equals("open"))
		{
			_open = true;
		}
		_closeTime = data.getInteger("close_time", -1);
		_level = data.getInteger("level", 0);
		_openType = data.getInteger("open_method", 0);
		_checkCollision = data.getBool("check_collision", true);
		if (isOpenableByTime())
		{
			_openTime = data.getInteger("open_time");
			_randomTime = data.getInteger("random_time", -1);
			startTimerOpen();
		}
		int clanhallId = data.getInteger("clanhall_id", 0);
		if (clanhallId > 0)
		{
			ClanHall hall = ClanHallManager.getAllClanHalls().get(clanhallId);
			if (hall != null)
			{
				setClanHall(hall);
				hall.getDoors().add(this);
			}
		}
	}
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		@Override
		public L2DoorInstance getActor()
		{
			return L2DoorInstance.this;
		}
		
		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}
		
		@Override
		public void moveTo(int x, int y, int z)
		{
		}
		
		@Override
		public void stopMove(Location loc)
		{
		}
		
		@Override
		public void doAttack(L2Character target)
		{
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
		}
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
					_ai = new L2DoorAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return _ai;
	}
	
	private void startTimerOpen()
	{
		int delay = _open ? _openTime : _closeTime;
		if (_randomTime > 0)
		{
			delay += Rnd.get(_randomTime);
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new TimerOpen(), delay * 1000);
	}
	
	@Override
	public final DoorKnownList getKnownList()
	{
		return (DoorKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new DoorKnownList(this));
	}
	
	@Override
	public L2DoorTemplate getTemplate()
	{
		return (L2DoorTemplate) super.getTemplate();
		
	}
	
	@Override
	public final DoorStatus getStatus()
	{
		return (DoorStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new DoorStatus(this));
	}
	
	public final boolean isOpenableBySkill()
	{
		return (_openType & OPEN_BY_SKILL) != 0;
	}
	
	public final boolean isOpenableByItem()
	{
		return (_openType & OPEN_BY_ITEM) != 0;
	}
	
	public final boolean isOpenableByClick()
	{
		return (_openType & OPEN_BY_CLICK) != 0;
	}
	
	public final boolean isOpenableByTime()
	{
		return (_openType & OPEN_BY_TIME) != 0;
	}
	
	public final boolean isOpenableByCycle()
	{
		return (_openType & OPEN_BY_CYCLE) != 0;
	}
	
	@Override
	public final int getLevel()
	{
		return _level;
	}
	
	@Override
	public int getId()
	{
		return getTemplate().getId();
	}
	
	public int getDoorId()
	{
		return getTemplate().doorId;
	}
	
	public boolean getOpen()
	{
		return _open;
	}
	
	public void setOpen(boolean open)
	{
		_open = open;
		if (getChildId() > 0)
		{
			L2DoorInstance sibling = getSiblingDoor(getChildId());
			if (sibling != null)
			{
				sibling.notifyChildEvent(open);
			}
			else
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": cannot find child id: " + getChildId());
			}
		}
	}
	
	public boolean getIsAttackableDoor()
	{
		return _isAttackableDoor;
	}
	
	public boolean getIsShowHp()
	{
		return getTemplate().showHp;
	}
	
	public void setIsAttackableDoor(boolean val)
	{
		_isAttackableDoor = val;
	}
	
	public int getDamage()
	{
		int dmg = 6 - (int) Math.ceil((getCurrentHp() / getMaxHp()) * 6);
		if (dmg > 6)
		{
			return 6;
		}
		if (dmg < 0)
		{
			return 0;
		}
		return dmg;
	}
	
	public final Castle getCastle()
	{
		if (_castleIndex < 0)
		{
			_castleIndex = CastleManager.getInstance().getCastleIndex(this);
		}
		if (_castleIndex < 0)
		{
			return null;
		}
		return CastleManager.getInstance().getCastles().get(_castleIndex);
	}
	
	public final Fort getFort()
	{
		if (_fortIndex < 0)
		{
			_fortIndex = FortManager.getInstance().getFortIndex(this);
		}
		if (_fortIndex < 0)
		{
			return null;
		}
		return FortManager.getInstance().getForts().get(_fortIndex);
	}
	
	public void setClanHall(ClanHall clanhall)
	{
		_clanHall = clanhall;
	}
	
	public ClanHall getClanHall()
	{
		return _clanHall;
	}
	
	public boolean isEnemy()
	{
		if ((getCastle() != null) && (getCastle().getId() > 0) && getCastle().getZone().isActive() && getIsShowHp())
		{
			return true;
		}
		if ((getFort() != null) && (getFort().getId() > 0) && getFort().getZone().isActive() && getIsShowHp())
		{
			return true;
		}
		if ((getClanHall() != null) && getClanHall().isSiegableHall() && ((SiegableHall) getClanHall()).getSiegeZone().isActive() && getIsShowHp())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (!(attacker instanceof L2Playable))
		{
			return false;
		}
		
		if (getIsAttackableDoor())
		{
			return true;
		}
		if (!getIsShowHp())
		{
			return false;
		}
		
		L2PcInstance actingPlayer = attacker.getActingPlayer();
		
		if (getClanHall() != null)
		{
			if (!getClanHall().isSiegableHall())
			{
				return false;
			}
			return ((SiegableHall) getClanHall()).isInSiege() && ((SiegableHall) getClanHall()).getSiege().doorIsAutoAttackable() && ((SiegableHall) getClanHall()).getSiege().checkIsAttacker(actingPlayer.getClan());
		}
		boolean isCastle = ((getCastle() != null) && (getCastle().getId() > 0) && getCastle().getZone().isActive());
		boolean isFort = ((getFort() != null) && (getFort().getId() > 0) && getFort().getZone().isActive());
		int activeSiegeId = (getFort() != null ? getFort().getId() : (getCastle() != null ? getCastle().getId() : 0));
		
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, activeSiegeId))
			{
				return false;
			}
			return true;
		}
		else if (isFort)
		{
			L2Clan clan = actingPlayer.getClan();
			if ((clan != null) && (clan == getFort().getOwnerClan()))
			{
				return false;
			}
		}
		else if (isCastle)
		{
			L2Clan clan = actingPlayer.getClan();
			if ((clan != null) && (clan.getId() == getCastle().getOwnerId()))
			{
				return false;
			}
		}
		return (isCastle || isFort);
	}
	
	@Override
	public void updateAbnormalEffect()
	{
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		if ((knownPlayers == null) || knownPlayers.isEmpty())
		{
			return;
		}
		
		StaticObject su = new StaticObject(this, false);
		DoorStatusUpdate dsu = new DoorStatusUpdate(this);
		OnEventTrigger oe = null;
		if (getEmitter() > 0)
		{
			oe = new OnEventTrigger(this, getOpen());
		}
		
		for (L2PcInstance player : knownPlayers)
		{
			if ((player == null) || !isVisibleFor(player))
			{
				continue;
			}
			
			if (player.isGM())
			{
				su = new StaticObject(this, true);
			}
			
			if (((getCastle() != null) && (getCastle().getId() > 0)) || ((getFort() != null) && (getFort().getId() > 0)))
			{
				su = new StaticObject(this, true);
			}
			
			player.sendPacket(su);
			player.sendPacket(dsu);
			if (oe != null)
			{
				player.sendPacket(oe);
			}
		}
	}
	
	public final void openMe()
	{
		if (getGroupName() != null)
		{
			manageGroupOpen(true, getGroupName());
			return;
		}
		setOpen(true);
		openGeo();
		broadcastStatusUpdate();
		startAutoCloseTask();
	}
	
	public final void closeMe()
	{
		Future<?> oldTask = _autoCloseTask;
		if (oldTask != null)
		{
			_autoCloseTask = null;
			oldTask.cancel(false);
		}
		if (getGroupName() != null)
		{
			manageGroupOpen(false, getGroupName());
			return;
		}
		setOpen(false);
		closeGeo();
		broadcastStatusUpdate();
	}
	
	private void manageGroupOpen(boolean open, String groupName)
	{
		Set<Integer> set = DoorParser.getDoorsByGroup(groupName);
		L2DoorInstance first = null;
		for (Integer id : set)
		{
			L2DoorInstance door = getSiblingDoor(id);
			if (first == null)
			{
				first = door;
			}
			
			if (door.getOpen() != open)
			{
				door.setOpen(open);
				door.broadcastStatusUpdate();
			}
		}
		if ((first != null) && open)
		{
			first.startAutoCloseTask();
		}
	}
	
	private void notifyChildEvent(boolean open)
	{
		byte openThis = open ? getTemplate().masterDoorOpen : getTemplate().masterDoorClose;
		
		if (openThis == 0)
		{
			return;
		}
		else if (openThis == 1)
		{
			openMe();
		}
		else if (openThis == -1)
		{
			closeMe();
		}
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getTemplate().doorId + "](" + getObjectId() + ")";
	}
	
	public String getDoorName()
	{
		return getTemplate().name;
	}
	
	public int getX(int i)
	{
		return getTemplate().nodeX[i];
	}
	
	public int getY(int i)
	{
		return getTemplate().nodeY[i];
	}
	
	public int getZMin()
	{
		return getTemplate().nodeZ;
	}
	
	public int getZMax()
	{
		return getTemplate().nodeZ + getTemplate().height;
	}
	
	public Collection<L2DefenderInstance> getKnownDefenders()
	{
		FastList<L2DefenderInstance> result = new FastList<>();
		
		Collection<L2Object> objs = getKnownList().getKnownObjects().values();
		for (L2Object obj : objs)
		{
			if (obj instanceof L2DefenderInstance)
			{
				result.add((L2DefenderInstance) obj);
			}
		}
		
		return result;
	}
	
	public void setMeshIndex(int mesh)
	{
		_meshindex = mesh;
	}
	
	public int getMeshIndex()
	{
		return _meshindex;
	}
	
	public int getEmitter()
	{
		return getTemplate().emmiter;
	}
	
	public boolean isWall()
	{
		return getTemplate().isWall;
	}
	
	public String getGroupName()
	{
		return getTemplate().groupName;
	}
	
	public int getChildId()
	{
		return getTemplate().childDoorId;
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (isWall() && !(attacker instanceof L2SiegeSummonInstance))
		{
			return;
		}
		
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	@Override
	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		boolean isFort = ((getFort() != null) && (getFort().getId() > 0) && getFort().getSiege().getIsInProgress());
		boolean isCastle = ((getCastle() != null) && (getCastle().getId() > 0) && getCastle().getSiege().getIsInProgress());
		boolean isHall = ((getClanHall() != null) && getClanHall().isSiegableHall() && ((SiegableHall) getClanHall()).isInSiege());
		
		if (isFort || isCastle || isHall)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_GATE_BROKEN_DOWN));
		}
		return true;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (isVisibleFor(activeChar))
		{
			if (getEmitter() > 0)
			{
				activeChar.sendPacket(new OnEventTrigger(this, getOpen()));
			}
			activeChar.sendPacket(new StaticObject(this, activeChar.isGM()));
		}
	}
	
	public void setTargetable(boolean b)
	{
		_isTargetable = b;
		broadcastStatusUpdate();
	}
	
	@Override
	public boolean isTargetable()
	{
		return _isTargetable;
	}
	
	public boolean checkCollision()
	{
		return _checkCollision;
	}
	
	private L2DoorInstance getSiblingDoor(int doorId)
	{
		if (getInstanceId() == 0)
		{
			return DoorParser.getInstance().getDoor(doorId);
		}
		
		Instance inst = InstanceManager.getInstance().getInstance(getInstanceId());
		if (inst != null)
		{
			return inst.getDoor(doorId);
		}
		
		return null;
	}
	
	private void startAutoCloseTask()
	{
		if ((_closeTime < 0) || isOpenableByTime())
		{
			return;
		}
		Future<?> oldTask = _autoCloseTask;
		if (oldTask != null)
		{
			_autoCloseTask = null;
			oldTask.cancel(false);
		}
		_autoCloseTask = ThreadPoolManager.getInstance().scheduleGeneral(new AutoClose(), _closeTime * 1000);
	}
	
	class AutoClose implements Runnable
	{
		@Override
		public void run()
		{
			if (getOpen())
			{
				closeMe();
			}
		}
	}
	
	class TimerOpen implements Runnable
	{
		@Override
		public void run()
		{
			boolean open = getOpen();
			if (open)
			{
				closeMe();
			}
			else
			{
				openMe();
			}
			
			int delay = open ? _closeTime : _openTime;
			if (_randomTime > 0)
			{
				delay += Rnd.get(_randomTime);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(this, delay * 1000);
		}
	}
	
	@Override
	public boolean isDoor()
	{
		return true;
	}
	
	public void setRange(L2Range range)
	{
		_range = range;
	}
	
	public L2Range getRange()
	{
		return _range;
	}
	
	private byte[][] _geoAround;
	
	@Override
	public boolean isConcrete()
	{
		return true;
	}
	
	@Override
	public GeoShape getShape()
	{
		return getRange();
	}
	
	@Override
	public byte[][] getGeoAround()
	{
		return _geoAround;
	}
	
	@Override
	public void setGeoAround(byte[][] geo)
	{
		_geoAround = geo;
	}
	
	public void openGeo()
	{
		if (getInstanceId() == 0)
		{
			if (!_isGeoReverted)
			{
				GeoClient.getInstance().removeGeoCollision(this);
			}
			else
			{
				GeoClient.getInstance().applyGeoCollision(this);
			}
		}
	}
	
	public void closeGeo()
	{
		if (getInstanceId() == 0)
		{
			if (!_isGeoReverted)
			{
				GeoClient.getInstance().applyGeoCollision(this);
			}
			else if (getGeoAround() != null)
			{
				GeoClient.getInstance().removeGeoCollision(this);
			}
		}
	}
	
	public void setIsGeoReverted(boolean value)
	{
		_isGeoReverted = value;
	}
	
	public boolean isGeoReverted()
	{
		return _isGeoReverted;
	}
}