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
package l2e.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.quest.Quest.QuestSound;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowQuestMark;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.QuestList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TutorialCloseHtml;
import l2e.gameserver.network.serverpackets.TutorialEnableClientEvent;
import l2e.gameserver.network.serverpackets.TutorialShowHtml;
import l2e.gameserver.network.serverpackets.TutorialShowQuestionMark;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

public final class QuestState
{
	protected static final Logger _log = Logger.getLogger(QuestState.class.getName());
	
	private final String _questName;
	private final L2PcInstance _player;
	private byte _state;
	private Map<String, String> _vars;
	private boolean _isExitQuestOnCleanUp = false;
	
	public static enum QuestType
	{
		REPEATABLE,
		ONE_TIME,
		DAILY
	}
	
	public QuestState(Quest quest, L2PcInstance player, byte state)
	{
		_questName = quest.getName();
		_player = player;
		_state = state;
		
		player.setQuestState(this);
	}
	
	public String getQuestName()
	{
		return _questName;
	}
	
	public Quest getQuest()
	{
		return QuestManager.getInstance().getQuest(_questName);
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	public byte getState()
	{
		return _state;
	}
	
	public boolean isCreated()
	{
		return (_state == State.CREATED);
	}
	
	public boolean isStarted()
	{
		return (_state == State.STARTED);
	}
	
	public boolean isCompleted()
	{
		return (_state == State.COMPLETED);
	}
	
	public boolean setState(byte state)
	{
		return setState(state, true);
	}
	
	public boolean setState(byte state, boolean saveInDb)
	{
		if (_state == state)
		{
			return false;
		}
		final boolean newQuest = isCreated();
		_state = state;
		if (saveInDb)
		{
			if (newQuest)
			{
				Quest.createQuestInDb(this);
			}
			else
			{
				Quest.updateQuestInDb(this);
			}
		}
		
		_player.sendPacket(new QuestList());
		return true;
	}
	
	public String setInternal(String var, String val)
	{
		if (_vars == null)
		{
			_vars = new HashMap<>();
		}
		
		if (val == null)
		{
			val = "";
		}
		
		_vars.put(var, val);
		return val;
	}
	
	public String set(String var, int val)
	{
		return set(var, Integer.toString(val));
	}
	
	public String set(String var, String val)
	{
		if (_vars == null)
		{
			_vars = new HashMap<>();
		}
		
		if (val == null)
		{
			val = "";
		}
		
		String old = _vars.put(var, val);
		
		if (old != null)
		{
			Quest.updateQuestVarInDb(this, var, val);
		}
		else
		{
			Quest.createQuestVarInDb(this, var, val);
		}
		
		if ("cond".equals(var))
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch (Exception ex)
				{
					previousVal = 0;
				}
				setCond(Integer.parseInt(val), previousVal);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, _player.getName() + ", " + getQuestName() + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent: " + e.getMessage(), e);
			}
		}
		
		return val;
	}
	
	private void setCond(int cond, int old)
	{
		if (cond == old)
		{
			return;
		}
		
		int completedStateFlags = 0;
		
		if ((cond < 3) || (cond > 31))
		{
			unset("__compltdStateFlags");
		}
		else
		{
			completedStateFlags = getInt("__compltdStateFlags");
		}
		
		if (completedStateFlags == 0)
		{
			if (cond > (old + 1))
			{
				completedStateFlags = 0x80000001;
				completedStateFlags |= ((1 << old) - 1);
				
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		else if (cond < old)
		{
			completedStateFlags &= ((1 << cond) - 1);
			
			if (completedStateFlags == ((1 << cond) - 1))
			{
				unset("__compltdStateFlags");
			}
			else
			{
				completedStateFlags |= 0x80000001;
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		else
		{
			completedStateFlags |= (1 << (cond - 1));
			set("__compltdStateFlags", String.valueOf(completedStateFlags));
		}
		_player.sendPacket(new QuestList());
		
		final Quest q = getQuest();
		if (!q.isCustomQuest() && (cond > 0))
		{
			_player.sendPacket(new ExShowQuestMark(q.getId()));
		}
	}
	
	public String unset(String var)
	{
		if (_vars == null)
		{
			return null;
		}
		
		String old = _vars.remove(var);
		if (old != null)
		{
			Quest.deleteQuestVarInDb(this, var);
		}
		return old;
	}
	
	public final void saveGlobalQuestVar(String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO character_quest_global_data (charId, var, value) VALUES (?, ?, ?)"))
		{
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not insert player's global quest variable: " + e.getMessage(), e);
		}
	}
	
	public final String getGlobalQuestVar(String var)
	{
		String result = "";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT value FROM character_quest_global_data WHERE charId = ? AND var = ?"))
		{
			ps.setInt(1, _player.getObjectId());
			ps.setString(2, var);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.first())
				{
					result = rs.getString(1);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not load player's global quest variable: " + e.getMessage(), e);
		}
		return result;
	}
	
	public final void deleteGlobalQuestVar(String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId = ? AND var = ?"))
		{
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete player's global quest variable; charId = " + _player.getObjectId() + ", variable name = " + var + ". Exception: " + e.getMessage(), e);
		}
	}
	
	public String get(String var)
	{
		if (_vars == null)
		{
			return null;
		}
		
		return _vars.get(var);
	}
	
	public int getInt(String var)
	{
		if (_vars == null)
		{
			return 0;
		}
		
		final String variable = _vars.get(var);
		if ((variable == null) || variable.isEmpty())
		{
			return 0;
		}
		
		int varint = 0;
		try
		{
			varint = Integer.parseInt(variable);
		}
		catch (NumberFormatException nfe)
		{
			_log.log(Level.INFO, "Quest " + getQuestName() + ", method getInt(" + var + "), tried to parse a non-integer value (" + variable + "). Char ID: " + _player.getObjectId(), nfe);
		}
		
		return varint;
	}
	
	public boolean isCond(int condition)
	{
		return (getInt("cond") == condition);
	}
	
	public QuestState setCond(int value)
	{
		if (isStarted())
		{
			set("cond", Integer.toString(value));
		}
		return this;
	}
	
	public int getCond()
	{
		if (isStarted())
		{
			return getInt("cond");
		}
		return 0;
	}
	
	public boolean isSet(String variable)
	{
		return (get(variable) != null);
	}
	
	public QuestState setCond(int value, boolean playQuestMiddle)
	{
		if (!isStarted())
		{
			return this;
		}
		set("cond", String.valueOf(value));
		
		if (playQuestMiddle)
		{
			Quest.playSound(_player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return this;
	}
	
	public void addNotifyOfDeath(L2Character character)
	{
		if (!(character.isPlayer()))
		{
			return;
		}
		((L2PcInstance) character).addNotifyQuestOfDeath(this);
	}
	
	public long getQuestItemsCount(int itemId)
	{
		return Quest.getQuestItemsCount(_player, itemId);
	}
	
	public long getQuestItemsCount(int... itemsIds)
	{
		long result = 0;
		for (int id : itemsIds)
		{
			result += getQuestItemsCount(id);
		}
		return result;
	}
	
	public boolean hasQuestItems(int itemId)
	{
		return Quest.hasQuestItems(_player, itemId);
	}
	
	public boolean hasQuestItems(int... itemIds)
	{
		return Quest.hasQuestItems(_player, itemIds);
	}
	
	public int getEnchantLevel(int itemId)
	{
		return Quest.getEnchantLevel(_player, itemId);
	}
	
	public void giveAdena(long count, boolean applyRates)
	{
		giveItems(PcInventory.ADENA_ID, count, applyRates ? 0 : 1);
	}
	
	public void rewardItems(ItemsHolder item)
	{
		Quest.rewardItems(_player, item);
	}
	
	public void rewardItems(int itemId, long count)
	{
		Quest.rewardItems(_player, itemId, count);
	}
	
	public void giveItems(int itemId, long count)
	{
		Quest.giveItems(_player, itemId, count, 0);
	}
	
	public void giveItems(int itemId, double count)
	{
		giveItems(itemId, (long) count, 0);
	}
	
	public void giveItems(int itemId, long count, boolean ignored)
	{
		giveItems(itemId, count, 0);
	}
	
	public void giveItems(ItemsHolder holder)
	{
		Quest.giveItems(_player, holder.getId(), holder.getCount(), 0);
	}
	
	public void giveItems(int itemId, long count, int enchantlevel)
	{
		Quest.giveItems(_player, itemId, count, enchantlevel);
	}
	
	public void giveItems(int itemId, long count, byte attributeId, int attributeLevel)
	{
		Quest.giveItems(_player, itemId, count, attributeId, attributeLevel);
	}
	
	public boolean dropQuestItems(int itemId, int count, int dropChance)
	{
		return dropQuestItems(itemId, count, count, -1, dropChance, true);
	}
	
	public boolean dropQuestItems(int itemId, int count, long neededCount, int dropChance, boolean sound)
	{
		return Quest.dropQuestItems(_player, itemId, count, count, neededCount, dropChance, sound);
	}
	
	public boolean dropQuestItems(int itemId, int minCount, int maxCount, long neededCount, int dropChance, boolean sound)
	{
		return Quest.dropQuestItems(_player, itemId, minCount, maxCount, neededCount, dropChance, sound);
	}
	
	public boolean dropQuestItems(final int itemId, final int minCount, final int maxCount, final long neededCount, final boolean infiniteCount, final float dropChance, final boolean sound)
	{
		final long currentCount = getQuestItemsCount(itemId);
		
		if (!infiniteCount && (neededCount > 0) && (currentCount >= neededCount))
		{
			return true;
		}
		
		final int MAX_CHANCE = 1000;
		final int adjDropChance = (int) (dropChance * (MAX_CHANCE / 100) * Config.RATE_QUEST_DROP);
		int curDropChance = adjDropChance;
		
		final int adjMaxCount = (int) (maxCount * Config.RATE_QUEST_DROP);
		
		long itemCount = 0;
		
		if ((curDropChance > MAX_CHANCE) && !Config.PRECISE_DROP_CALCULATION)
		{
			int multiplier = curDropChance / MAX_CHANCE;
			
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount * multiplier, maxCount * multiplier);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount * multiplier;
			}
			else
			{
				itemCount += multiplier;
			}
			
			curDropChance %= MAX_CHANCE;
		}
		
		final int random = Rnd.get(MAX_CHANCE);
		
		while (random < curDropChance)
		{
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}
			curDropChance -= MAX_CHANCE;
		}
		
		if (itemCount > 0)
		{
			if (itemCount > adjMaxCount)
			{
				itemCount = adjMaxCount;
			}
			
			itemCount *= 1; // Config.RATE_DROP_QUEST_ITEM_AMOUNT - Don't Support in core
			
			if (!infiniteCount && (neededCount > 0) && ((currentCount + itemCount) > neededCount))
			{
				itemCount = neededCount - currentCount;
			}
			
			if (!getPlayer().getInventory().validateCapacityByItemId(itemId))
			{
				return false;
			}
			
			getPlayer().addItem("Quest", itemId, itemCount, getPlayer().getTarget(), true);
			
			if (sound)
			{
				if (neededCount == 0)
				{
					playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else
				{
					playSound(((currentCount % neededCount) + itemCount) < neededCount ? QuestSound.ITEMSOUND_QUEST_ITEMGET : QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return (!infiniteCount && (neededCount > 0) && ((currentCount + itemCount) >= neededCount));
	}
	
	public boolean dropItemsAlways(int itemId, int count, long neededCount)
	{
		return dropItems(itemId, count, neededCount, 1000000, (byte) 1);
	}
	
	public synchronized long dropItems(int itemId, long count, long limit)
	{
		boolean have = false;
		long qic = getQuestItemsCount(itemId);
		if (qic > 0)
		{
			have = true;
		}
		
		if (count <= 0)
		{
			return qic;
		}
		
		count = (int) (count * Config.RATE_QUEST_DROP);
		if ((limit > 0) && ((qic + count) > limit))
		{
			count = limit - qic;
		}
		
		L2ItemInstance item = getPlayer().getInventory().addItem("QuestItemDrop", itemId, count, getPlayer(), getPlayer().getTarget());
		
		if (item == null)
		{
			return qic;
		}
		
		if (count > 1)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			smsg.addItemName(item);
			smsg.addItemNumber(count);
			getPlayer().sendPacket(smsg);
		}
		else
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
			smsg.addItemName(item);
			getPlayer().sendPacket(smsg);
		}
		
		InventoryUpdate iu = new InventoryUpdate();
		if (item.isStackable() && have)
		{
			iu.addModifiedItem(item);
		}
		else
		{
			iu.addNewItem(item);
		}
		getPlayer().sendPacket(iu);
		
		StatusUpdate su = new StatusUpdate(getPlayer().getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getPlayer().getCurrentLoad());
		getPlayer().sendPacket(su);
		return (qic + count);
	}
	
	public boolean dropItems(int itemId, int count, long neededCount, int dropChance)
	{
		return dropItems(itemId, count, neededCount, dropChance, (byte) 0);
	}
	
	public boolean dropItems(int itemId, int count, long neededCount, int dropChance, byte type)
	{
		long currentCount = getQuestItemsCount(itemId);
		
		if ((neededCount > 0) && (currentCount >= neededCount))
		{
			return true;
		}
		
		int amount = 0;
		switch (type)
		{
			case 0:
				dropChance = (int) (dropChance * Config.RATE_QUEST_DROP);
				amount = count * (dropChance / 1000000);
				if (Rnd.get(1000000) < (dropChance % 1000000))
				{
					amount += count;
				}
				break;
			case 1:
				if (Rnd.get(1000000) < dropChance)
				{
					amount = (int) (count * Config.RATE_QUEST_DROP);
				}
				break;
			case 2:
				if (Rnd.get(1000000) < (dropChance * Config.RATE_QUEST_DROP))
				{
					amount = count;
				}
				break;
			case 3:
				if (Rnd.get(1000000) < dropChance)
				{
					amount = count;
				}
				break;
		}
		
		boolean reached = false;
		if (amount > 0)
		{
			if (neededCount > 0)
			{
				reached = (currentCount + amount) >= neededCount;
				amount = (int) (reached ? neededCount - currentCount : amount);
			}
			
			if (!_player.getInventory().validateCapacityByItemId(itemId))
			{
				return false;
			}
			giveItems(itemId, amount, 0);
			playSound(reached ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
		}
		return (neededCount > 0) && (reached);
	}
	
	public int rollDrop(int count, double calcChance)
	{
		if ((calcChance <= 0) || (count <= 0))
		{
			return 0;
		}
		return rollDrop(count, count, calcChance);
	}
	
	public int rollDrop(int min, int max, double calcChance)
	{
		if ((calcChance <= 0) || (min <= 0) || (max <= 0))
		{
			return 0;
		}
		
		int dropmult = 1;
		calcChance *= getRateQuestsDrop();
		
		if (calcChance > 100)
		{
			if ((int) Math.ceil(calcChance / 100) <= (calcChance / 100))
			{
				calcChance = Math.nextUp(calcChance);
			}
			dropmult = (int) Math.ceil(calcChance / 100);
			calcChance = calcChance / dropmult;
		}
		return Rnd.chance(calcChance) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}
	
	public boolean rollAndGive(int itemId, int count, double calcChance)
	{
		if ((calcChance <= 0) || (count <= 0) || (itemId <= 0))
		{
			return false;
		}
		
		int countToDrop = rollDrop(count, calcChance);
		
		if (countToDrop > 0)
		{
			giveItems(itemId, countToDrop, false);
			playSound("ItemSound.quest_itemget");
			return true;
		}
		return false;
	}
	
	public boolean rollAndGive(int itemId, int min, int max, int limit, double calcChance)
	{
		if ((calcChance <= 0) || (min <= 0) || (max <= 0) || (limit <= 0) || (itemId <= 0))
		{
			return false;
		}
		
		long count = rollDrop(min, max, calcChance);
		
		if (count > 0)
		{
			long alreadyCount = getQuestItemsCount(itemId);
			if ((alreadyCount + count) > limit)
			{
				count = limit - alreadyCount;
			}
			
			if (count > 0)
			{
				giveItems(itemId, count, false);
				if ((count + alreadyCount) < limit)
				{
					playSound("ItemSound.quest_itemget");
				}
				else
				{
					playSound("ItemSound.quest_middle");
					return true;
				}
			}
		}
		return false;
	}
	
	public void rollAndGive(int itemId, int min, int max, double calcChance)
	{
		if ((calcChance <= 0) || (min <= 0) || (max <= 0) || (itemId <= 0))
		{
			return;
		}
		
		int count = rollDrop(min, max, calcChance);
		
		if (count > 0)
		{
			giveItems(itemId, count, false);
			playSound("ItemSound.quest_itemget");
		}
	}
	
	public boolean giveItemRandomly(int itemId, long amount, long limit, double dropChance, boolean playSound)
	{
		return Quest.giveItemRandomly(_player, null, itemId, amount, amount, limit, dropChance, playSound);
	}
	
	public boolean giveItemRandomly(L2Npc npc, int itemId, long amount, long limit, double dropChance, boolean playSound)
	{
		return Quest.giveItemRandomly(_player, npc, itemId, amount, amount, limit, dropChance, playSound);
	}
	
	public boolean giveItemRandomly(L2Npc npc, int itemId, long minAmount, long maxAmount, long limit, double dropChance, boolean playSound)
	{
		return Quest.giveItemRandomly(_player, npc, itemId, minAmount, maxAmount, limit, dropChance, playSound);
	}
	
	public void addRadar(int x, int y, int z)
	{
		_player.getRadar().addMarker(x, y, z);
	}
	
	public void removeRadar(int x, int y, int z)
	{
		_player.getRadar().removeMarker(x, y, z);
	}
	
	public void clearRadar()
	{
		_player.getRadar().removeAllMarkers();
	}
	
	public void takeItems(int itemId, long count)
	{
		Quest.takeItems(_player, itemId, count);
	}
	
	public long takeAllItems(int itemId)
	{
		return Quest.takeAllItems(getPlayer(), itemId, -1);
	}
	
	public long takeAllItems(int... itemsIds)
	{
		long result = 0;
		for (int id : itemsIds)
		{
			result += takeAllItems(id);
		}
		return result;
	}
	
	public void playSound(String sound)
	{
		Quest.playSound(_player, sound);
	}
	
	public void playSound(QuestSound sound)
	{
		Quest.playSound(_player, sound);
	}
	
	public void addExpAndSp(int exp, int sp)
	{
		exp *= getPlayer().getStat().getRExp();
		sp *= getPlayer().getStat().getRSp();
		
		Quest.addExpAndSp(_player, exp, sp);
	}
	
	public int getItemEquipped(int loc)
	{
		return Quest.getItemEquipped(_player, loc);
	}
	
	public final boolean isExitQuestOnCleanUp()
	{
		return _isExitQuestOnCleanUp;
	}
	
	public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
	{
		_isExitQuestOnCleanUp = isExitQuestOnCleanUp;
	}
	
	public void startQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), false);
	}
	
	public void startQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), false);
	}
	
	public void startRepeatingQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), true);
	}
	
	public void startRepeatingQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), true);
	}
	
	public final QuestTimer getQuestTimer(String name)
	{
		return getQuest().getQuestTimer(name, null, getPlayer());
	}
	
	public L2Npc addSpawn(int npcId)
	{
		return addSpawn(npcId, _player.getX(), _player.getY(), _player.getZ(), 0, false, 0, false);
	}
	
	public L2Npc addSpawn(int npcId, int despawnDelay)
	{
		return addSpawn(npcId, _player.getX(), _player.getY(), _player.getZ(), 0, false, despawnDelay, false);
	}
	
	public L2Npc addSpawn(int npcId, int x, int y, int z)
	{
		return addSpawn(npcId, x, y, z, 0, false, 0, false);
	}
	
	public L2Npc addSpawn(int npcId, int x, int y, int z, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, 0, false, despawnDelay, false);
	}
	
	public L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, 0, false);
	}
	
	public L2Npc addSpawn(int npcId, L2Character cha, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, despawnDelay, false);
	}
	
	public L2Npc addSpawn(int npcId, L2Character cha, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay, false);
	}
	
	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false);
	}
	
	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	{
		return Quest.addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn);
	}
	
	public String showHtmlFile(String fileName)
	{
		return getQuest().showHtmlFile(getPlayer(), fileName);
	}
	
	public QuestState startQuest()
	{
		if (isCreated() && !getQuest().isCustomQuest())
		{
			set("cond", "1");
			setState(State.STARTED);
			playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		return this;
	}
	
	public QuestState exitQuest(QuestType type)
	{
		switch (type)
		{
			case DAILY:
			{
				exitQuest(false);
				setRestartTime();
				break;
			}
			default:
			{
				exitQuest(type == QuestType.REPEATABLE);
				break;
			}
		}
		return this;
	}
	
	public QuestState exitQuest(QuestType type, boolean playExitQuest)
	{
		exitQuest(type);
		if (playExitQuest)
		{
			playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return this;
	}
	
	public QuestState exitQuest(boolean repeatable)
	{
		_player.removeNotifyQuestOfDeath(this);
		
		if (!isStarted())
		{
			return this;
		}
		getQuest().removeRegisteredQuestItems(_player);
		
		Quest.deleteQuestInDb(this, repeatable);
		if (repeatable)
		{
			_player.delQuestState(getQuestName());
			_player.sendPacket(new QuestList());
		}
		else
		{
			setState(State.COMPLETED);
		}
		_vars = null;
		return this;
	}
	
	public QuestState exitQuest(boolean repeatable, boolean playExitQuest)
	{
		exitQuest(repeatable);
		if (playExitQuest)
		{
			playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return this;
	}
	
	public void showQuestionMark(int number)
	{
		_player.sendPacket(new TutorialShowQuestionMark(number));
	}
	
	public void playTutorialVoice(String voice)
	{
		_player.sendPacket(new PlaySound(2, voice, 0, 0, _player.getX(), _player.getY(), _player.getZ()));
	}
	
	public void showTutorialHTML(String html)
	{
		L2PcInstance player = getPlayer();
		String lang = player.getLang();
		String filepath = "data/scripts/quests/_255_Tutorial/" + lang + "/" + html;
		String oriPath = filepath;
		String content = HtmCache.getInstance().getHtm(filepath);
		if (content == null)
		{
			filepath = "data/scripts/quests/_255_Tutorial/en/" + html;
			content = HtmCache.getInstance().getHtm(filepath);
		}
		if (content == null)
		{
			_log.warning("Cache[HTML]: Missing HTML page: " + oriPath);
			content = "<html><body>File data/scripts/quests/_255_Tutorial/" + html + " not found or file is empty.</body></html>";
		}
		player.sendPacket(new TutorialShowHtml(content));
	}
	
	public void closeTutorialHtml()
	{
		_player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
	}
	
	public void onTutorialClientEvent(int number)
	{
		_player.sendPacket(new TutorialEnableClientEvent(number));
	}
	
	public void dropItem(L2MonsterInstance npc, L2PcInstance player, int itemId, int count)
	{
		npc.dropItem(player, itemId, count);
	}
	
	public void setRestartTime()
	{
		final Calendar reDo = Calendar.getInstance();
		if (reDo.get(Calendar.HOUR_OF_DAY) >= getQuest().getResetHour())
		{
			reDo.add(Calendar.DATE, 1);
		}
		reDo.set(Calendar.HOUR_OF_DAY, getQuest().getResetHour());
		reDo.set(Calendar.MINUTE, getQuest().getResetMinutes());
		set("restartTime", String.valueOf(reDo.getTimeInMillis()));
	}
	
	public boolean isNowAvailable()
	{
		final String val = get("restartTime");
		return ((val == null) || !Util.isDigit(val)) || (Long.parseLong(val) <= System.currentTimeMillis());
	}
	
	public int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	public double getRateQuestsDrop()
	{
		return Config.RATE_QUEST_DROP;
	}
}