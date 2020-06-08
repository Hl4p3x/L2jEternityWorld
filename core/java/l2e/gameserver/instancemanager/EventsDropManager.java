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
package l2e.gameserver.instancemanager;

import java.util.HashMap;
import java.util.Map;

import javolution.util.FastList;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.util.Rnd;

public class EventsDropManager
{
	private static EventsDropManager _instance;

	private boolean _haveActiveCristmasEvent = false;
	private boolean _haveActiveMedalsEvent = false;
	private boolean _haveActiveL2DayEvent = false;
	private boolean _haveActiveMasterOfEnchantingEvent = false;
	private boolean _haveActiveSquashEvent = false;
	private boolean _haveActiveValentineEvent = false;

	private final Map<Integer, rewardRule> _rewardCristmasRules = new HashMap<>();
	private final Map<Integer, rewardRule> _rewardMedalsRules = new HashMap<>();
	private final Map<Integer, rewardRule> _rewardL2DayRules = new HashMap<>();
	private final Map<Integer, rewardRule> _rewardMasterOfEnchantingRules = new HashMap<>();
	private final Map<Integer, rewardRule> _rewardSquashRules = new HashMap<>();
	private final Map<Integer, rewardRule> _rewardValentineRules = new HashMap<>();
	
	public static enum ruleType
	{
		ALL_NPC,
		BY_NPCID,
		BY_ZONE
	}
	
	public static final EventsDropManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new EventsDropManager();
		}
		return _instance;
	}
	
	public boolean haveActiveCristmasEvent()
	{
		return _haveActiveCristmasEvent;
	}

	public boolean haveActiveMedalsEvent()
	{
		return _haveActiveMedalsEvent;
	}

	public boolean haveActiveL2DayEvent()
	{
		return _haveActiveL2DayEvent;
	}

	public boolean haveActiveMasterOfEnchantingEvent()
	{
		return _haveActiveMasterOfEnchantingEvent;
	}

	public boolean haveActiveSquashEvent()
	{
		return _haveActiveSquashEvent;
	}

	public boolean haveActiveValentineEvent()
	{
		return _haveActiveValentineEvent;
	}

	public void addValentineRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[])
	{
		addValentineRule(event, type, itemId, itemCnt, itemChance, true);
	}
	
	public void addValentineRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[], boolean lvlControl)
	{
		rewardRule rule = new rewardRule();
		rule._eventName = event;
		rule._ruleType = type;
		rule._rewardCnt = itemId.length;
		rule._levDifferenceControl = lvlControl;
		for (int x = 1; x <= itemId.length; x++)
		{
			rule._itemId.add(itemId[x - 1]);
		}
		for (int x = 1; x <= itemCnt.length; x++)
		{
			rule._itemCnt.add(itemCnt[x - 1]);
		}
		for (int x = 1; x <= itemChance.length; x++)
		{
			rule._itemChance.add(itemChance[x - 1]);
		}
		_rewardValentineRules.put(_rewardValentineRules.size() + 1, rule);
		_haveActiveValentineEvent = true;
	}

	public void addSquashRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[])
	{
		addSquashRule(event, type, itemId, itemCnt, itemChance, true);
	}
	
	public void addSquashRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[], boolean lvlControl)
	{
		rewardRule rule = new rewardRule();
		rule._eventName = event;
		rule._ruleType = type;
		rule._rewardCnt = itemId.length;
		rule._levDifferenceControl = lvlControl;
		for (int x = 1; x <= itemId.length; x++)
		{
			rule._itemId.add(itemId[x - 1]);
		}
		for (int x = 1; x <= itemCnt.length; x++)
		{
			rule._itemCnt.add(itemCnt[x - 1]);
		}
		for (int x = 1; x <= itemChance.length; x++)
		{
			rule._itemChance.add(itemChance[x - 1]);
		}
		_rewardSquashRules.put(_rewardSquashRules.size() + 1, rule);
		_haveActiveSquashEvent = true;
	}

	public void addMasterOfEnchantingRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[])
	{
		addMasterOfEnchantingRule(event, type, itemId, itemCnt, itemChance, true);
	}
	
	public void addMasterOfEnchantingRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[], boolean lvlControl)
	{
		rewardRule rule = new rewardRule();
		rule._eventName = event;
		rule._ruleType = type;
		rule._rewardCnt = itemId.length;
		rule._levDifferenceControl = lvlControl;
		for (int x = 1; x <= itemId.length; x++)
		{
			rule._itemId.add(itemId[x - 1]);
		}
		for (int x = 1; x <= itemCnt.length; x++)
		{
			rule._itemCnt.add(itemCnt[x - 1]);
		}
		for (int x = 1; x <= itemChance.length; x++)
		{
			rule._itemChance.add(itemChance[x - 1]);
		}
		_rewardMasterOfEnchantingRules.put(_rewardMasterOfEnchantingRules.size() + 1, rule);
		_haveActiveMasterOfEnchantingEvent = true;
	}
	
	public void addCristmasRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[])
	{
		addCristmasRule(event, type, itemId, itemCnt, itemChance, true);
	}
	
	public void addCristmasRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[], boolean lvlControl)
	{
		rewardRule rule = new rewardRule();
		rule._eventName = event;
		rule._ruleType = type;
		rule._rewardCnt = itemId.length;
		rule._levDifferenceControl = lvlControl;
		for (int x = 1; x <= itemId.length; x++)
		{
			rule._itemId.add(itemId[x - 1]);
		}
		for (int x = 1; x <= itemCnt.length; x++)
		{
			rule._itemCnt.add(itemCnt[x - 1]);
		}
		for (int x = 1; x <= itemChance.length; x++)
		{
			rule._itemChance.add(itemChance[x - 1]);
		}
		_rewardCristmasRules.put(_rewardCristmasRules.size() + 1, rule);
		_haveActiveCristmasEvent = true;
	}

	public void addMedalsRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[])
	{
		addMedalsRule(event, type, itemId, itemCnt, itemChance, true);
	}
	
	public void addMedalsRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[], boolean lvlControl)
	{
		rewardRule rule = new rewardRule();
		rule._eventName = event;
		rule._ruleType = type;
		rule._rewardCnt = itemId.length;
		rule._levDifferenceControl = lvlControl;
		for (int x = 1; x <= itemId.length; x++)
		{
			rule._itemId.add(itemId[x - 1]);
		}
		for (int x = 1; x <= itemCnt.length; x++)
		{
			rule._itemCnt.add(itemCnt[x - 1]);
		}
		for (int x = 1; x <= itemChance.length; x++)
		{
			rule._itemChance.add(itemChance[x - 1]);
		}
		_rewardMedalsRules.put(_rewardMedalsRules.size() + 1, rule);
		_haveActiveMedalsEvent = true;
	}

	public void addL2DayRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[])
	{
		addL2DayRule(event, type, itemId, itemCnt, itemChance, true);
	}
	
	public void addL2DayRule(String event, ruleType type, int itemId[], int itemCnt[], int itemChance[], boolean lvlControl)
	{
		rewardRule rule = new rewardRule();
		rule._eventName = event;
		rule._ruleType = type;
		rule._rewardCnt = itemId.length;
		rule._levDifferenceControl = lvlControl;
		for (int x = 1; x <= itemId.length; x++)
		{
			rule._itemId.add(itemId[x - 1]);
		}
		for (int x = 1; x <= itemCnt.length; x++)
		{
			rule._itemCnt.add(itemCnt[x - 1]);
		}
		for (int x = 1; x <= itemChance.length; x++)
		{
			rule._itemChance.add(itemChance[x - 1]);
		}
		_rewardL2DayRules.put(_rewardL2DayRules.size() + 1, rule);
		_haveActiveL2DayEvent = true;
	}
	
	public void removeCristmasRules(String event)
	{
		for (rewardRule tmp : _rewardCristmasRules.values())
		{
			if (tmp._eventName == event)
			{
				_rewardCristmasRules.remove(tmp);
				_haveActiveCristmasEvent = false;
			}
		}
	}

	public void removeMedalsRules(String event)
	{
		for (rewardRule tmp : _rewardMedalsRules.values())
		{
			if (tmp._eventName == event)
			{
				_rewardMedalsRules.remove(tmp);
				_haveActiveMedalsEvent = false;
			}
		}
	}

	public void removeL2DayRules(String event)
	{
		for (rewardRule tmp : _rewardL2DayRules.values())
		{
			if (tmp._eventName == event)
			{
				_rewardL2DayRules.remove(tmp);
				_haveActiveL2DayEvent = false;
			}
		}
	}

	public void removeMasterOfEnchantingRules(String event)
	{
		for (rewardRule tmp : _rewardMasterOfEnchantingRules.values())
		{
			if (tmp._eventName == event)
			{
				_rewardMasterOfEnchantingRules.remove(tmp);
				_haveActiveMasterOfEnchantingEvent = false;
			}
		}
	}

	public void removeSquashRules(String event)
	{
		for (rewardRule tmp : _rewardSquashRules.values())
		{
			if (tmp._eventName == event)
			{
				_rewardSquashRules.remove(tmp);
				_haveActiveSquashEvent = false;
			}
		}
	}

	public void removeValentineRules(String event)
	{
		for (rewardRule tmp : _rewardValentineRules.values())
		{
			if (tmp._eventName == event)
			{
				_rewardValentineRules.remove(tmp);
				_haveActiveValentineEvent = false;
			}
		}
	}
	
	public int[] calculateCristmasRewardItem(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int res[] =
		{
			0,
			0
		};
		int lvlDif = lastAttacker.getLevel() - npcTemplate.getLevel();
		FastList<rewards> _rewards = new FastList<>();
		
		if (_rewardCristmasRules.size() > 0)
		{
			for (rewardRule tmp : _rewardCristmasRules.values())
			{
				if (tmp._levDifferenceControl && ((lvlDif > 7) || (lvlDif < -7)))
				{
					continue;
				}
				if (tmp._ruleType == ruleType.ALL_NPC)
				{
					int cnt = 0;
					while (cnt <= (tmp._rewardCnt - 1))
					{
						if (tmp._itemChance.get(cnt) >= Rnd.get(0, 1000))
						{
							_rewards.add(new rewards(tmp._itemId.get(cnt), tmp._itemCnt.get(cnt)));
						}
						cnt++;
					}
				}
			}
		}

		if (_rewards.size() > 0)
		{
			int rndRew = Rnd.get(_rewards.size());
			res[0] = _rewards.get(rndRew)._rewardId;
			res[1] = _rewards.get(rndRew)._rewardCnt;
		}
		return res;
	}

	public int[] calculateMedalsRewardItem(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int res[] =
		{
			0,
			0
		};
		int lvlDif = lastAttacker.getLevel() - npcTemplate.getLevel();
		FastList<rewards> _rewards = new FastList<>();

		if (_rewardMedalsRules.size() > 0)
		{
			for (rewardRule tmp : _rewardMedalsRules.values())
			{
				if (tmp._levDifferenceControl && ((lvlDif > 7) || (lvlDif < -7)))
				{
					continue;
				}
				if (tmp._ruleType == ruleType.ALL_NPC)
				{
					int cnt = 0;
					while (cnt <= (tmp._rewardCnt - 1))
					{
						if (tmp._itemChance.get(cnt) >= Rnd.get(0, 1000))
						{
							_rewards.add(new rewards(tmp._itemId.get(cnt), tmp._itemCnt.get(cnt)));
						}
						cnt++;
					}
				}
			}
		}

		if (_rewards.size() > 0)
		{
			int rndRew = Rnd.get(_rewards.size());
			res[0] = _rewards.get(rndRew)._rewardId;
			res[1] = _rewards.get(rndRew)._rewardCnt;
		}
		return res;
	}

	public int[] calculateL2DayRewardItem(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int res[] =
		{
			0,
			0
		};
		int lvlDif = lastAttacker.getLevel() - npcTemplate.getLevel();
		FastList<rewards> _rewards = new FastList<>();

		if (_rewardL2DayRules.size() > 0)
		{
			for (rewardRule tmp : _rewardL2DayRules.values())
			{
				if (tmp._levDifferenceControl && ((lvlDif > 7) || (lvlDif < -7)))
				{
					continue;
				}
				if (tmp._ruleType == ruleType.ALL_NPC)
				{
					int cnt = 0;
					while (cnt <= (tmp._rewardCnt - 1))
					{
						if (tmp._itemChance.get(cnt) >= Rnd.get(0, 1000))
						{
							_rewards.add(new rewards(tmp._itemId.get(cnt), tmp._itemCnt.get(cnt)));
						}
						cnt++;
					}
				}
			}
		}

		if (_rewards.size() > 0)
		{
			int rndRew = Rnd.get(_rewards.size());
			res[0] = _rewards.get(rndRew)._rewardId;
			res[1] = _rewards.get(rndRew)._rewardCnt;
		}
		return res;
	}

	public int[] calculateMasterOfEnchantingRewardItem(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int res[] =
		{
			0,
			0
		};
		int lvlDif = lastAttacker.getLevel() - npcTemplate.getLevel();
		FastList<rewards> _rewards = new FastList<>();

		if (_rewardMasterOfEnchantingRules.size() > 0)
		{
			for (rewardRule tmp : _rewardMasterOfEnchantingRules.values())
			{
				if (tmp._levDifferenceControl && ((lvlDif > 7) || (lvlDif < -7)))
				{
					continue;
				}
				if (tmp._ruleType == ruleType.ALL_NPC)
				{
					int cnt = 0;
					while (cnt <= (tmp._rewardCnt - 1))
					{
						if (tmp._itemChance.get(cnt) >= Rnd.get(0, 1000))
						{
							_rewards.add(new rewards(tmp._itemId.get(cnt), tmp._itemCnt.get(cnt)));
						}
						cnt++;
					}
				}
			}
		}

		if (_rewards.size() > 0)
		{
			int rndRew = Rnd.get(_rewards.size());
			res[0] = _rewards.get(rndRew)._rewardId;
			res[1] = _rewards.get(rndRew)._rewardCnt;
		}
		return res;
	}

	public int[] calculateSquashRewardItem(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int res[] =
		{
			0,
			0
		};
		int lvlDif = lastAttacker.getLevel() - npcTemplate.getLevel();
		FastList<rewards> _rewards = new FastList<>();

		if (_rewardSquashRules.size() > 0)
		{
			for (rewardRule tmp : _rewardSquashRules.values())
			{
				if (tmp._levDifferenceControl && ((lvlDif > 7) || (lvlDif < -7)))
				{
					continue;
				}
				if (tmp._ruleType == ruleType.ALL_NPC)
				{
					int cnt = 0;
					while (cnt <= (tmp._rewardCnt - 1))
					{
						if (tmp._itemChance.get(cnt) >= Rnd.get(0, 1000))
						{
							_rewards.add(new rewards(tmp._itemId.get(cnt), tmp._itemCnt.get(cnt)));
						}
						cnt++;
					}
				}
			}
		}

		if (_rewards.size() > 0)
		{
			int rndRew = Rnd.get(_rewards.size());
			res[0] = _rewards.get(rndRew)._rewardId;
			res[1] = _rewards.get(rndRew)._rewardCnt;
		}
		return res;
	}

	public int[] calculateValentineRewardItem(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int res[] =
		{
			0,
			0
		};
		int lvlDif = lastAttacker.getLevel() - npcTemplate.getLevel();
		FastList<rewards> _rewards = new FastList<>();

		if (_rewardValentineRules.size() > 0)
		{
			for (rewardRule tmp : _rewardValentineRules.values())
			{
				if (tmp._levDifferenceControl && ((lvlDif > 7) || (lvlDif < -7)))
				{
					continue;
				}
				if (tmp._ruleType == ruleType.ALL_NPC)
				{
					int cnt = 0;
					while (cnt <= (tmp._rewardCnt - 1))
					{
						if (tmp._itemChance.get(cnt) >= Rnd.get(0, 1000))
						{
							_rewards.add(new rewards(tmp._itemId.get(cnt), tmp._itemCnt.get(cnt)));
						}
						cnt++;
					}
				}
			}
		}

		if (_rewards.size() > 0)
		{
			int rndRew = Rnd.get(_rewards.size());
			res[0] = _rewards.get(rndRew)._rewardId;
			res[1] = _rewards.get(rndRew)._rewardCnt;
		}
		return res;
	}
	
	public class rewardRule
	{
		public int _rewardCnt = 0;
		public String _eventName;
		public ruleType _ruleType = null;
		public boolean _levDifferenceControl;
		public FastList<Integer> _mobId = new FastList<>();
		public L2ZoneType _zone = null;
		public FastList<Integer> _itemId = new FastList<>();
		public FastList<Integer> _itemCnt = new FastList<>();
		public FastList<Integer> _itemChance = new FastList<>();
	}
	
	private class rewards
	{
		public int _rewardId;
		public int _rewardCnt;
		
		public rewards(int Id, int Cnt)
		{
			_rewardId = Id;
			_rewardCnt = Cnt;
		}
	}
}