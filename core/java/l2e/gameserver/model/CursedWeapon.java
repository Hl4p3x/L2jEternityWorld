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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SkillHolder.FrequentSkill;
import l2e.gameserver.data.xml.TransformParser;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.FunEventsManager;
import l2e.gameserver.model.L2Party.messageType;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.Earthquake;
import l2e.gameserver.network.serverpackets.ExRedSky;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.util.Broadcast;
import l2e.gameserver.util.Point3D;
import l2e.util.Rnd;

public class CursedWeapon
{
	private static final Logger _log = Logger.getLogger(CursedWeapon.class.getName());
	
	private final String _name;
	private final int _itemId;
	private final int _skillId;
	private final int _skillMaxLevel;
	private int _dropRate;
	private int _duration;
	private int _durationLost;
	private int _disapearChance;
	private int _stageKills;
	
	private boolean _isDropped = false;
	private boolean _isActivated = false;
	private ScheduledFuture<?> _removeTask;
	
	private int _nbKills = 0;
	private long _endTime = 0;
	
	private int _playerId = 0;
	protected L2PcInstance _player = null;
	private L2ItemInstance _item = null;
	private int _playerKarma = 0;
	private int _playerPkKills = 0;
	protected int transformationId = 0;
	
	public CursedWeapon(int itemId, int skillId, String name)
	{
		_name = name;
		_itemId = itemId;
		_skillId = skillId;
		_skillMaxLevel = SkillHolder.getInstance().getMaxLevel(_skillId);
	}
	
	public void endOfLife()
	{
		if (_isActivated)
		{
			if ((_player != null) && _player.isOnline())
			{
				_log.info(_name + " being removed online.");
				
				_player.abortAttack();
				
				_player.setKarma(_playerKarma);
				_player.setPkKills(_playerPkKills);
				_player.setCursedWeaponEquippedId(0);
				removeSkill();
				
				_player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_LR_HAND);
				_player.store();
				
				L2ItemInstance removedItem = _player.getInventory().destroyItemByItemId("", _itemId, 1, _player, null);
				if (!Config.FORCE_INVENTORY_UPDATE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					if (removedItem.getCount() == 0)
					{
						iu.addRemovedItem(removedItem);
					}
					else
					{
						iu.addModifiedItem(removedItem);
					}
					
					_player.sendPacket(iu);
				}
				else
				{
					_player.sendPacket(new ItemList(_player, true));
				}
				
				_player.broadcastUserInfo();
			}
			else
			{
				_log.info(_name + " being removed offline.");
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
					statement.setInt(1, _playerId);
					statement.setInt(2, _itemId);
					if (statement.executeUpdate() != 1)
					{
						_log.warning("Error while deleting itemId " + _itemId + " from userId " + _playerId);
					}
					statement.close();
					statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE charId=?");
					statement.setInt(1, _playerKarma);
					statement.setInt(2, _playerPkKills);
					statement.setInt(3, _playerId);
					if (statement.executeUpdate() != 1)
					{
						_log.warning("Error while updating karma & pkkills for userId " + _playerId);
					}
					
					statement.close();
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Could not delete : " + e.getMessage(), e);
				}
			}
		}
		else
		{
			if ((_player != null) && (_player.getInventory().getItemByItemId(_itemId) != null))
			{
				L2ItemInstance removedItem = _player.getInventory().destroyItemByItemId("", _itemId, 1, _player, null);
				if (!Config.FORCE_INVENTORY_UPDATE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					if (removedItem.getCount() == 0)
					{
						iu.addRemovedItem(removedItem);
					}
					else
					{
						iu.addModifiedItem(removedItem);
					}
					
					_player.sendPacket(iu);
				}
				else
				{
					_player.sendPacket(new ItemList(_player, true));
				}
				
				_player.broadcastUserInfo();
			}
			else if (_item != null)
			{
				_item.decayMe();
				L2World.getInstance().removeObject(_item);
				_log.info(_name + " item has been removed from World.");
			}
		}
		CursedWeaponsManager.removeFromDb(_itemId);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
		sm.addItemName(_itemId);
		CursedWeaponsManager.announce(sm);
		
		cancelTask();
		_isActivated = false;
		_isDropped = false;
		_endTime = 0;
		_player = null;
		_playerId = 0;
		_playerKarma = 0;
		_playerPkKills = 0;
		_item = null;
		_nbKills = 0;
	}
	
	private void cancelTask()
	{
		if (_removeTask != null)
		{
			_removeTask.cancel(true);
			_removeTask = null;
		}
	}
	
	private class RemoveTask implements Runnable
	{
		protected RemoveTask()
		{
		}
		
		@Override
		public void run()
		{
			if (System.currentTimeMillis() >= getEndTime())
			{
				endOfLife();
			}
		}
	}
	
	private void dropIt(L2Attackable attackable, L2PcInstance player)
	{
		dropIt(attackable, player, null, true);
	}
	
	private void dropIt(L2Attackable attackable, L2PcInstance player, L2Character killer, boolean fromMonster)
	{
		_isActivated = false;
		
		if (fromMonster)
		{
			_item = attackable.dropItem(player, _itemId, 1);
			_item.setDropTime(0);
			
			ExRedSky packet = new ExRedSky(10);
			Earthquake eq = new Earthquake(player.getX(), player.getY(), player.getZ(), 14, 3);
			Broadcast.toAllOnlinePlayers(packet);
			Broadcast.toAllOnlinePlayers(eq);
		}
		else
		{
			_item = _player.getInventory().getItemByItemId(_itemId);
			_player.dropItem("DieDrop", _item, killer, true);
			_player.setKarma(_playerKarma);
			_player.setPkKills(_playerPkKills);
			_player.setCursedWeaponEquippedId(0);
			removeSkill();
			_player.abortAttack();
		}
		_isDropped = true;
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION);
		if (player != null)
		{
			sm.addZoneName(player.getX(), player.getY(), player.getZ());
		}
		else if (_player != null)
		{
			sm.addZoneName(_player.getX(), _player.getY(), _player.getZ());
		}
		else
		{
			sm.addZoneName(killer.getX(), killer.getY(), killer.getZ());
		}
		sm.addItemName(_itemId);
		CursedWeaponsManager.announce(sm);
	}
	
	public void cursedOnLogin()
	{
		doTransform();
		giveSkill();
		
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION);
		msg.addZoneName(_player.getX(), _player.getY(), _player.getZ());
		msg.addItemName(_player.getCursedWeaponEquippedId());
		CursedWeaponsManager.announce(msg);
		
		CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(_player.getCursedWeaponEquippedId());
		SystemMessage msg2 = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
		int timeLeft = (int) (cw.getTimeLeft() / 60000);
		msg2.addItemName(_player.getCursedWeaponEquippedId());
		msg2.addNumber(timeLeft);
		_player.sendPacket(msg2);
	}
	
	public void giveSkill()
	{
		int level = 1 + (_nbKills / _stageKills);
		if (level > _skillMaxLevel)
		{
			level = _skillMaxLevel;
		}
		
		final L2Skill skill = SkillHolder.getInstance().getInfo(_skillId, level);
		_player.addSkill(skill, false);
		
		_player.addSkill(FrequentSkill.VOID_BURST.getSkill(), false);
		_player.addTransformSkill(FrequentSkill.VOID_BURST.getId());
		_player.addSkill(FrequentSkill.VOID_FLOW.getSkill(), false);
		_player.addTransformSkill(FrequentSkill.VOID_FLOW.getId());
		_player.sendSkillList();
	}
	
	public void doTransform()
	{
		if (_itemId == 8689)
		{
			transformationId = 302;
		}
		else if (_itemId == 8190)
		{
			transformationId = 301;
		}
		
		if (_player.isTransformed() || _player.isInStance())
		{
			_player.stopTransformation(true);
			
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					TransformParser.getInstance().transformPlayer(transformationId, _player);
				}
			}, 500);
		}
		else
		{
			TransformParser.getInstance().transformPlayer(transformationId, _player);
		}
	}
	
	public void removeSkill()
	{
		_player.removeSkill(_skillId);
		_player.removeSkill(SkillHolder.FrequentSkill.VOID_BURST.getSkill().getId());
		_player.removeSkill(SkillHolder.FrequentSkill.VOID_FLOW.getSkill().getId());
		_player.untransform();
		_player.sendSkillList();
	}
	
	public void reActivate()
	{
		_isActivated = true;
		if ((_endTime - System.currentTimeMillis()) <= 0)
		{
			endOfLife();
		}
		else
		{
			_removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), _durationLost * 12000L, _durationLost * 12000L);
		}
		
	}
	
	public boolean checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if (Rnd.get(100000) < _dropRate)
		{
			dropIt(attackable, player);
			
			_endTime = System.currentTimeMillis() + (_duration * 60000L);
			_removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), _durationLost * 12000L, _durationLost * 12000L);
			
			return true;
		}
		
		return false;
	}
	
	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		if (player.isMounted() && !player.dismount())
		{
			player.sendPacket(SystemMessageId.FAILED_TO_PICKUP_S1);
			player.dropItem("InvDrop", item, null, true);
			return;
		}
		
		if (player.getEventName() != null)
		{
			FunEventsManager.getInstance().notifyJoinCursed(player);
		}
		
		_isActivated = true;
		
		_player = player;
		_playerId = _player.getObjectId();
		_playerKarma = _player.getKarma();
		_playerPkKills = _player.getPkKills();
		saveData();
		
		_player.setCursedWeaponEquippedId(_itemId);
		_player.setKarma(9999999);
		_player.setPkKills(0);
		if (_player.isInParty())
		{
			_player.getParty().removePartyMember(_player, messageType.Expelled);
		}
		
		doTransform();
		giveSkill();
		
		_item = item;
		_player.getInventory().equipItem(_item);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(_item);
		_player.sendPacket(sm);
		_player.setCurrentHpMp(_player.getMaxHp(), _player.getMaxMp());
		_player.setCurrentCp(_player.getMaxCp());
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_item);
			_player.sendPacket(iu);
		}
		else
		{
			_player.sendPacket(new ItemList(_player, false));
		}
		
		_player.broadcastUserInfo();
		
		SocialAction atk = new SocialAction(_player.getObjectId(), 17);
		
		_player.broadcastPacket(atk);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION);
		sm.addZoneName(_player.getX(), _player.getY(), _player.getZ());
		sm.addItemName(_item);
		CursedWeaponsManager.announce(sm);
	}
	
	public void saveData()
	{
		if (Config.DEBUG)
		{
			_log.info("CursedWeapon: Saving data to disk.");
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, _itemId);
			statement.executeUpdate();
			statement.close();
			
			if (_isActivated)
			{
				statement = con.prepareStatement("INSERT INTO cursed_weapons (itemId, charId, playerKarma, playerPkKills, nbKills, endTime) VALUES (?, ?, ?, ?, ?, ?)");
				statement.setInt(1, _itemId);
				statement.setInt(2, _playerId);
				statement.setInt(3, _playerKarma);
				statement.setInt(4, _playerPkKills);
				statement.setInt(5, _nbKills);
				statement.setLong(6, _endTime);
				statement.executeUpdate();
				statement.close();
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "CursedWeapon: Failed to save data.", e);
		}
	}
	
	public void dropIt(L2Character killer)
	{
		if (Rnd.get(100) <= _disapearChance)
		{
			endOfLife();
		}
		else
		{
			dropIt(null, null, killer, false);
			_player.setKarma(_playerKarma);
			_player.setPkKills(_playerPkKills);
			_player.setCursedWeaponEquippedId(0);
			removeSkill();
			
			_player.abortAttack();
			_player.broadcastUserInfo();
		}
	}
	
	public void increaseKills()
	{
		_nbKills++;
		
		if ((_player != null) && _player.isOnline())
		{
			_player.setPkKills(_nbKills);
			_player.sendPacket(new UserInfo(_player));
			
			if (((_nbKills % _stageKills) == 0) && (_nbKills <= (_stageKills * (_skillMaxLevel - 1))))
			{
				giveSkill();
			}
		}
		_endTime -= _durationLost * 60000L;
		saveData();
	}
	
	public void setDisapearChance(int disapearChance)
	{
		_disapearChance = disapearChance;
	}
	
	public void setDropRate(int dropRate)
	{
		_dropRate = dropRate;
	}
	
	public void setDuration(int duration)
	{
		_duration = duration;
	}
	
	public void setDurationLost(int durationLost)
	{
		_durationLost = durationLost;
	}
	
	public void setStageKills(int stageKills)
	{
		_stageKills = stageKills;
	}
	
	public void setNbKills(int nbKills)
	{
		_nbKills = nbKills;
	}
	
	public void setPlayerId(int playerId)
	{
		_playerId = playerId;
	}
	
	public void setPlayerKarma(int playerKarma)
	{
		_playerKarma = playerKarma;
	}
	
	public void setPlayerPkKills(int playerPkKills)
	{
		_playerPkKills = playerPkKills;
	}
	
	public void setActivated(boolean isActivated)
	{
		_isActivated = isActivated;
	}
	
	public void setDropped(boolean isDropped)
	{
		_isDropped = isDropped;
	}
	
	public void setEndTime(long endTime)
	{
		_endTime = endTime;
	}
	
	public void setPlayer(L2PcInstance player)
	{
		_player = player;
	}
	
	public void setItem(L2ItemInstance item)
	{
		_item = item;
	}
	
	public boolean isActivated()
	{
		return _isActivated;
	}
	
	public boolean isDropped()
	{
		return _isDropped;
	}
	
	public long getEndTime()
	{
		return _endTime;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public int getPlayerId()
	{
		return _playerId;
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	public int getPlayerKarma()
	{
		return _playerKarma;
	}
	
	public int getPlayerPkKills()
	{
		return _playerPkKills;
	}
	
	public int getNbKills()
	{
		return _nbKills;
	}
	
	public int getStageKills()
	{
		return _stageKills;
	}
	
	public boolean isActive()
	{
		return _isActivated || _isDropped;
	}
	
	public int getLevel()
	{
		if (_nbKills > (_stageKills * _skillMaxLevel))
		{
			return _skillMaxLevel;
		}
		return (_nbKills / _stageKills);
	}
	
	public long getTimeLeft()
	{
		return _endTime - System.currentTimeMillis();
	}
	
	public void goTo(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		if (_isActivated && (_player != null))
		{
			player.teleToLocation(_player.getX(), _player.getY(), _player.getZ() + 20, true);
		}
		else if (_isDropped && (_item != null))
		{
			player.teleToLocation(_item.getX(), _item.getY(), _item.getZ() + 20, true);
		}
		else
		{
			player.sendMessage(_name + " isn't in the World.");
		}
	}
	
	public Point3D getWorldPosition()
	{
		if (_isActivated && (_player != null))
		{
			return _player.getPosition().getWorldPosition();
		}
		
		if (_isDropped && (_item != null))
		{
			return _item.getPosition().getWorldPosition();
		}
		
		return null;
	}
	
	public long getDuration()
	{
		return _duration;
	}
}