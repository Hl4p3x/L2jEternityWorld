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
package l2e.gameserver.model;

import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.handler.ActionHandler;
import l2e.gameserver.handler.ActionShiftHandler;
import l2e.gameserver.handler.IActionHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.knownlist.ObjectKnownList;
import l2e.gameserver.model.actor.poly.ObjectPoly;
import l2e.gameserver.model.actor.position.ObjectPosition;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.interfaces.IPositionable;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.DeleteObject;
import l2e.gameserver.network.serverpackets.ExSendUIEvent;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;

public abstract class L2Object implements IIdentifiable, IPositionable
{
	private boolean _isVisible;
	private boolean _isInvisible;
	private ObjectKnownList _knownList;
	private String _name;
	private int _objectId;
	private ObjectPoly _poly;
	private ObjectPosition _position;
	private int _instanceId = 0;
	private boolean _spawned = false;
	
	private InstanceType _instanceType = null;
	private volatile Map<String, Object> _scripts;
	
	public L2Object(int objectId)
	{
		setInstanceType(InstanceType.L2Object);
		_objectId = objectId;
		initKnownList();
		initPosition();
		if (this instanceof L2PcInstance)
		{
			_spawned = false;
		}
		else
		{
			_spawned = true;
		}
	}
	
	public static enum InstanceType
	{
		L2Object(null),
		L2ItemInstance(L2Object),
		L2Character(L2Object),
		L2Npc(L2Character),
		L2Playable(L2Character),
		L2Summon(L2Playable),
		L2Decoy(L2Character),
		L2PcInstance(L2Playable),
		L2NpcInstance(L2Npc),
		L2MerchantInstance(L2NpcInstance),
		L2WarehouseInstance(L2NpcInstance),
		L2StaticObjectInstance(L2Character),
		L2DoorInstance(L2Character),
		L2TerrainObjectInstance(L2Npc),
		L2EffectPointInstance(L2Npc),
		L2ServitorInstance(L2Summon),
		L2SiegeSummonInstance(L2ServitorInstance),
		L2MerchantSummonInstance(L2ServitorInstance),
		L2PetInstance(L2Summon),
		L2BabyPetInstance(L2PetInstance),
		L2DecoyInstance(L2Decoy),
		L2TrapInstance(L2Npc),
		L2Attackable(L2Npc),
		L2GuardInstance(L2Attackable),
		L2QuestGuardInstance(L2GuardInstance),
		L2MonsterInstance(L2Attackable),
		L2ChestInstance(L2MonsterInstance),
		L2ControllableMobInstance(L2MonsterInstance),
		L2FeedableBeastInstance(L2MonsterInstance),
		L2TamedBeastInstance(L2FeedableBeastInstance),
		L2FriendlyMobInstance(L2Attackable),
		L2RiftInvaderInstance(L2MonsterInstance),
		L2RaidBossInstance(L2MonsterInstance),
		L2GrandBossInstance(L2RaidBossInstance),
		L2FlyNpcInstance(L2NpcInstance),
		L2FlyMonsterInstance(L2MonsterInstance),
		L2FlyRaidBossInstance(L2RaidBossInstance),
		L2FlyTerrainObjectInstance(L2Npc),
		L2SepulcherNpcInstance(L2NpcInstance),
		L2SepulcherMonsterInstance(L2MonsterInstance),
		L2FestivalGiudeInstance(L2Npc),
		L2FestivalMonsterInstance(L2MonsterInstance),
		L2Vehicle(L2Character),
		L2BoatInstance(L2Vehicle),
		L2AirShipInstance(L2Vehicle),
		L2ControllableAirShipInstance(L2AirShipInstance),
		L2DefenderInstance(L2Attackable),
		L2ArtefactInstance(L2NpcInstance),
		L2ControlTowerInstance(L2Npc),
		L2FlameTowerInstance(L2Npc),
		L2SiegeFlagInstance(L2Npc),
		L2SiegeNpcInstance(L2Npc),
		L2FortBallistaInstance(L2Npc),
		L2FortCommanderInstance(L2DefenderInstance),
		L2CastleChamberlainInstance(L2MerchantInstance),
		L2CastleMagicianInstance(L2NpcInstance),
		L2FortEnvoyInstance(L2Npc),
		L2FortLogisticsInstance(L2MerchantInstance),
		L2FortManagerInstance(L2MerchantInstance),
		L2FortSiegeNpcInstance(L2Npc),
		L2FortSupportCaptainInstance(L2MerchantInstance),
		L2SignsPriestInstance(L2Npc),
		L2DawnPriestInstance(L2SignsPriestInstance),
		L2DuskPriestInstance(L2SignsPriestInstance),
		L2DungeonGatekeeperInstance(L2Npc),
		L2AdventurerInstance(L2NpcInstance),
		L2AuctioneerInstance(L2Npc),
		L2ClanHallManagerInstance(L2MerchantInstance),
		L2FishermanInstance(L2MerchantInstance),
		L2ManorManagerInstance(L2MerchantInstance),
		L2ObservationInstance(L2Npc),
		L2OlympiadManagerInstance(L2Npc),
		L2PetManagerInstance(L2MerchantInstance),
		L2RaceManagerInstance(L2Npc),
		L2SymbolMakerInstance(L2Npc),
		L2TeleporterInstance(L2Npc),
		L2TrainerInstance(L2NpcInstance),
		L2VillageMasterInstance(L2NpcInstance),
		L2DoormenInstance(L2NpcInstance),
		L2CastleDoormenInstance(L2DoormenInstance),
		L2FortDoormenInstance(L2DoormenInstance),
		L2ClanHallDoormenInstance(L2DoormenInstance),
		L2ClassMasterInstance(L2NpcInstance),
		L2NpcBufferInstance(L2Npc),
		L2TvTEventNpcInstance(L2Npc),
		L2TvTRoundEventNpcInstance(L2Npc),
		L2WeddingManagerInstance(L2Npc),
		L2EventMobInstance(L2Npc),
		L2UCManagerInstance(L2Npc),
		L2ChronoMonsterInstance(L2MonsterInstance),
		L2AioNpcInstance(L2Npc),
		L2HotSpringSquashInstance(L2MonsterInstance);
		
		private final InstanceType _parent;
		private final long _typeL;
		private final long _typeH;
		private final long _maskL;
		private final long _maskH;
		
		private InstanceType(InstanceType parent)
		{
			_parent = parent;
			
			final int high = this.ordinal() - (Long.SIZE - 1);
			if (high < 0)
			{
				_typeL = 1L << this.ordinal();
				_typeH = 0;
			}
			else
			{
				_typeL = 0;
				_typeH = 1L << high;
			}
			
			if ((_typeL < 0) || (_typeH < 0))
			{
				throw new Error("Too many instance types, failed to load " + this.name());
			}
			
			if (parent != null)
			{
				_maskL = _typeL | parent._maskL;
				_maskH = _typeH | parent._maskH;
			}
			else
			{
				_maskL = _typeL;
				_maskH = _typeH;
			}
		}
		
		public final InstanceType getParent()
		{
			return _parent;
		}
		
		public final boolean isType(InstanceType it)
		{
			return ((_maskL & it._typeL) > 0) || ((_maskH & it._typeH) > 0);
		}
		
		public final boolean isTypes(InstanceType... it)
		{
			for (InstanceType i : it)
			{
				if (isType(i))
				{
					return true;
				}
			}
			return false;
		}
	}
	
	protected final void setInstanceType(InstanceType instanceType)
	{
		_instanceType = instanceType;
	}
	
	public final InstanceType getInstanceType()
	{
		return _instanceType;
	}
	
	public final boolean isInstanceType(InstanceType instanceType)
	{
		return _instanceType.isType(instanceType);
	}
	
	public final boolean isInstanceTypes(InstanceType... instanceType)
	{
		return _instanceType.isTypes(instanceType);
	}
	
	public void onAction(L2PcInstance player)
	{
		onAction(player, true);
	}
	
	public void onAction(L2PcInstance player, boolean interact)
	{
		IActionHandler handler = ActionHandler.getInstance().getHandler(getInstanceType());
		if (handler != null)
		{
			handler.action(player, this, interact);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onActionShift(L2PcInstance player)
	{
		IActionHandler handler = ActionShiftHandler.getInstance().getHandler(getInstanceType());
		if (handler != null)
		{
			handler.action(player, this, true);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onForcedAttack(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onSpawn()
	{
	}
	
	public final void setXYZ(int x, int y, int z)
	{
		getPosition().setXYZ(x, y, z);
	}
	
	public final void setXYZInvisible(int x, int y, int z)
	{
		getPosition().setXYZInvisible(x, y, z);
	}
	
	@Override
	public final int getX()
	{
		assert (getPosition().getWorldRegion() != null) || _isVisible;
		return getPosition().getX();
	}
	
	@Override
	public final int getY()
	{
		assert (getPosition().getWorldRegion() != null) || _isVisible;
		return getPosition().getY();
	}
	
	@Override
	public final int getZ()
	{
		assert (getPosition().getWorldRegion() != null) || _isVisible;
		return getPosition().getZ();
	}
	
	@Override
	public Location getLocation()
	{
		return new Location(getX(), getY(), getZ(), getHeading(), getInstanceId());
	}
	
	public int getHeading()
	{
		return 0;
	}
	
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	public void setInstanceId(int instanceId)
	{
		if ((instanceId < 0) || (_instanceId == instanceId))
		{
			return;
		}
		
		Instance oldI = InstanceManager.getInstance().getInstance(_instanceId);
		Instance newI = InstanceManager.getInstance().getInstance(instanceId);
		
		if (newI == null)
		{
			return;
		}
		
		if (isPlayer())
		{
			L2PcInstance player = getActingPlayer();
			if ((_instanceId > 0) && (oldI != null))
			{
				oldI.removePlayer(getObjectId());
				if (oldI.isShowTimer())
				{
					int startTime = (int) ((System.currentTimeMillis() - oldI.getInstanceStartTime()) / 1000);
					int endTime = (int) ((oldI.getInstanceEndTime() - oldI.getInstanceStartTime()) / 1000);
					if (oldI.isTimerIncrease())
					{
						sendPacket(new ExSendUIEvent(getActingPlayer(), true, true, startTime, endTime, oldI.getTimerText()));
					}
					else
					{
						sendPacket(new ExSendUIEvent(getActingPlayer(), true, false, endTime - startTime, 0, oldI.getTimerText()));
					}
				}
			}
			if (instanceId > 0)
			{
				newI.addPlayer(getObjectId());
				if (newI.isShowTimer())
				{
					int startTime = (int) ((System.currentTimeMillis() - newI.getInstanceStartTime()) / 1000);
					int endTime = (int) ((newI.getInstanceEndTime() - newI.getInstanceStartTime()) / 1000);
					if (newI.isTimerIncrease())
					{
						sendPacket(new ExSendUIEvent(getActingPlayer(), false, true, startTime, endTime, newI.getTimerText()));
					}
					else
					{
						sendPacket(new ExSendUIEvent(getActingPlayer(), false, false, endTime - startTime, 0, newI.getTimerText()));
					}
				}
			}
			
			if (player.hasSummon())
			{
				player.getSummon().setInstanceId(instanceId);
			}
		}
		else if (isNpc())
		{
			L2Npc npc = (L2Npc) this;
			if ((_instanceId > 0) && (oldI != null))
			{
				oldI.removeNpc(npc);
			}
			if (instanceId > 0)
			{
				newI.addNpc(npc);
			}
		}
		
		_instanceId = instanceId;
		
		if (_isVisible && (_knownList != null))
		{
			if (isPlayer())
			{
			}
			else
			{
				decayMe();
				spawnMe();
			}
		}
	}
	
	public void decayMe()
	{
		assert getPosition().getWorldRegion() != null;
		
		L2WorldRegion reg = getPosition().getWorldRegion();
		
		synchronized (this)
		{
			_isVisible = false;
			getPosition().setWorldRegion(null);
		}
		L2World.getInstance().removeVisibleObject(this, reg);
		L2World.getInstance().removeObject(this);
	}
	
	public void refreshID()
	{
		L2World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		_objectId = IdFactory.getInstance().getNextId();
	}
	
	public final void spawnMe()
	{
		assert (getPosition().getWorldRegion() == null) && (getPosition().getWorldPosition().getX() != 0) && (getPosition().getWorldPosition().getY() != 0) && (getPosition().getWorldPosition().getZ() != 0);
		
		synchronized (this)
		{
			_isVisible = true;
			getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
			
			L2World.getInstance().storeObject(this);
			
			getPosition().getWorldRegion().addVisibleObject(this);
		}
		L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion());
		
		onSpawn();
	}
	
	public final void spawnMe(int x, int y, int z)
	{
		assert getPosition().getWorldRegion() == null;
		
		synchronized (this)
		{
			_isVisible = true;
			
			if (x > L2World.MAP_MAX_X)
			{
				x = L2World.MAP_MAX_X - 5000;
			}
			if (x < L2World.MAP_MIN_X)
			{
				x = L2World.MAP_MIN_X + 5000;
			}
			if (y > L2World.MAP_MAX_Y)
			{
				y = L2World.MAP_MAX_Y - 5000;
			}
			if (y < L2World.MAP_MIN_Y)
			{
				y = L2World.MAP_MIN_Y + 5000;
			}
			
			getPosition().setWorldPosition(x, y, z);
			getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
		}
		
		L2World.getInstance().storeObject(this);
		
		getPosition().getWorldRegion().addVisibleObject(this);
		
		L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion());
		
		onSpawn();
	}
	
	public void toggleVisible()
	{
		if (isVisible())
		{
			decayMe();
		}
		else
		{
			spawnMe();
		}
	}
	
	public boolean isAttackable()
	{
		return false;
	}
	
	public abstract boolean isAutoAttackable(L2Character attacker);
	
	public final boolean isVisible()
	{
		return getPosition().getWorldRegion() != null;
	}
	
	public final void setIsVisible(boolean value)
	{
		_isVisible = value;
		if (!_isVisible)
		{
			getPosition().setWorldRegion(null);
		}
	}
	
	public ObjectKnownList getKnownList()
	{
		return _knownList;
	}
	
	public void initKnownList()
	{
		_knownList = new ObjectKnownList(this);
	}
	
	public final void setKnownList(ObjectKnownList value)
	{
		_knownList = value;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public void setName(String value)
	{
		_name = value;
	}
	
	public final int getObjectId()
	{
		return _objectId;
	}
	
	public final ObjectPoly getPoly()
	{
		if (_poly == null)
		{
			_poly = new ObjectPoly(this);
		}
		return _poly;
	}
	
	public ObjectPosition getPosition()
	{
		return _position;
	}
	
	public void initPosition()
	{
		_position = new ObjectPosition(this);
	}
	
	public final void setObjectPosition(ObjectPosition value)
	{
		_position = value;
	}
	
	public L2WorldRegion getWorldRegion()
	{
		return getPosition().getWorldRegion();
	}
	
	public L2PcInstance getActingPlayer()
	{
		return null;
	}
	
	public L2Npc getActingNpc()
	{
		return null;
	}
	
	public final static L2PcInstance getActingPlayer(L2Object obj)
	{
		return (obj == null ? null : obj.getActingPlayer());
	}
	
	public void sendInfo(L2PcInstance activeChar)
	{
	}
	
	@Override
	public String toString()
	{
		return (getClass().getSimpleName() + ":" + getName() + "[" + getObjectId() + "]");
	}
	
	public void sendPacket(L2GameServerPacket mov)
	{
	}
	
	public void sendPacket(SystemMessageId id)
	{
	}
	
	public L2Character getActingCharacter()
	{
		return null;
	}
	
	public boolean isPlayer()
	{
		return false;
	}
	
	public boolean isPlayable()
	{
		return false;
	}
	
	public boolean isSummon()
	{
		return false;
	}
	
	public boolean isPet()
	{
		return false;
	}
	
	public boolean isServitor()
	{
		return false;
	}
	
	public boolean isCharacter()
	{
		return false;
	}
	
	public boolean isDoor()
	{
		return false;
	}
	
	public boolean isNpc()
	{
		return false;
	}
	
	public boolean isL2Attackable()
	{
		return false;
	}
	
	public boolean isMonster()
	{
		return false;
	}
	
	public boolean isTrap()
	{
		return false;
	}
	
	public boolean isItem()
	{
		return false;
	}
	
	public boolean isWalker()
	{
		return false;
	}
	
	public boolean isRunner()
	{
		return false;
	}
	
	public boolean isPhantome()
	{
		return false;
	}
	
	public boolean isSpecialCamera()
	{
		return false;
	}
	
	public boolean isEkimusFood()
	{
		return false;
	}
	
	public boolean isTargetable()
	{
		return true;
	}
	
	public boolean isInsideZone(ZoneId zone)
	{
		return false;
	}
	
	public boolean isChargedShot(ShotType type)
	{
		return false;
	}
	
	public void setChargedShot(ShotType type, boolean charged)
	{
	}
	
	public void rechargeShots(boolean physical, boolean magical)
	{
	}
	
	public final <T> T addScript(T script)
	{
		if (_scripts == null)
		{
			synchronized (this)
			{
				if (_scripts == null)
				{
					_scripts = new FastMap<String, Object>().shared();
				}
			}
		}
		_scripts.put(script.getClass().getName(), script);
		return script;
	}
	
	@SuppressWarnings("unchecked")
	public final <T> T removeScript(Class<T> script)
	{
		if (_scripts == null)
		{
			return null;
		}
		return (T) _scripts.remove(script.getName());
	}
	
	@SuppressWarnings("unchecked")
	public final <T> T getScript(Class<T> script)
	{
		if (_scripts == null)
		{
			return null;
		}
		return (T) _scripts.get(script.getName());
	}
	
	public void removeStatusListener(L2Character object)
	{
	}
	
	public final boolean isSpawned()
	{
		return _spawned;
	}
	
	protected void setSpawned(boolean value)
	{
		_spawned = value;
	}
	
	public boolean isInvisible()
	{
		return _isInvisible;
	}
	
	public void setInvisible(boolean invis)
	{
		_isInvisible = invis;
		if (invis)
		{
			final DeleteObject deletePacket = new DeleteObject(this);
			for (L2Object obj : getKnownList().getKnownObjects().values())
			{
				if ((obj != null) && obj.isPlayer())
				{
					final L2PcInstance player = obj.getActingPlayer();
					if (!isVisibleFor(player))
					{
						obj.sendPacket(deletePacket);
					}
				}
			}
		}
		broadcastInfo();
	}
	
	public boolean isVisibleFor(L2PcInstance player)
	{
		return !isInvisible() || player.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS);
	}
	
	public void broadcastInfo()
	{
		for (L2Object obj : getKnownList().getKnownObjects().values())
		{
			if ((obj != null) && obj.isPlayer() && isVisibleFor(obj.getActingPlayer()))
			{
				sendInfo(obj.getActingPlayer());
			}
		}
	}
	
	public Location getLoc()
	{
		return new Location(getPosition().getX(), getPosition().getY(), getPosition().getZ(), getHeading());
	}
	
	public final long getXYDeltaSq(int x, int y)
	{
		long dx = x - getX();
		long dy = y - getY();
		return (dx * dx) + (dy * dy);
	}
	
	public final long getXYZDeltaSq(int x, int y, int z)
	{
		return getXYDeltaSq(x, y) + getZDeltaSq(z);
	}
	
	public final long getZDeltaSq(int z)
	{
		long dz = z - getZ();
		return dz * dz;
	}
	
	public final double getDistance(L2Object obj)
	{
		if (obj == null)
		{
			return 0;
		}
		return Math.sqrt(getXYDeltaSq(obj.getX(), obj.getY()));
	}
	
	public final double getDistance3D(L2Object obj)
	{
		if (obj == null)
		{
			return 0;
		}
		return Math.sqrt(getXYZDeltaSq(obj.getX(), obj.getY(), obj.getZ()));
	}
	
	public final double getRealDistance3D(L2Object obj)
	{
		return getRealDistance3D(obj, false);
	}
	
	public final double getRealDistance3D(L2Object obj, boolean ignoreZ)
	{
		double distance = ignoreZ ? getDistance(obj) : getDistance3D(obj);
		if (isPlayer())
		{
			distance -= ((L2PcInstance) this).getTemplate()._collisionRadius;
		}
		if (obj.isCharacter())
		{
			distance -= ((L2Character) obj).getTemplate()._collisionRadius;
		}
		return distance > 0 ? distance : 0;
	}
}