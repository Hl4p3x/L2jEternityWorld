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

import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.List;
import java.util.concurrent.Future;

import javolution.util.FastList;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.StopMove;
import l2e.gameserver.util.Point3D;
import l2e.util.Rnd;

public final class L2TamedBeastInstance extends L2FeedableBeastInstance
{
	private int _foodSkillId;
	private static final int MAX_DISTANCE_FROM_HOME = 30000;
	private static final int MAX_DISTANCE_FROM_OWNER = 2000;
	private static final int MAX_DURATION = 1200000;
	private static final int DURATION_CHECK_INTERVAL = 60000;
	private static final int DURATION_INCREASE_INTERVAL = 20000;
	private static final int BUFF_INTERVAL = 5000;
	private int _remainingTime = MAX_DURATION;
	private int _homeX, _homeY, _homeZ;
	protected L2PcInstance _owner;
	private Future<?> _buffTask = null;
	private Future<?> _durationCheckTask = null;
	protected boolean _isFreyaBeast;
	private List<L2Skill> _beastSkills = null;
	
	public L2TamedBeastInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TamedBeastInstance);
		setHome(this);
	}
	
	public L2TamedBeastInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int foodSkillId, int x, int y, int z)
	{
		super(objectId, template);
		_isFreyaBeast = false;
		setInstanceType(InstanceType.L2TamedBeastInstance);
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setOwner(owner);
		setFoodType(foodSkillId);
		setHome(x, y, z);
		this.spawnMe(x, y, z);
	}
	
	public L2TamedBeastInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int food, int x, int y, int z, boolean isFreyaBeast)
	{
		super(objectId, template);
		_isFreyaBeast = isFreyaBeast;
		setInstanceType(InstanceType.L2TamedBeastInstance);
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setFoodType(food);
		setHome(x, y, z);
		spawnMe(x, y, z);
		setOwner(owner);
		if (isFreyaBeast)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
		}
		
	}
	
	public void onReceiveFood()
	{
		_remainingTime = _remainingTime + DURATION_INCREASE_INTERVAL;
		if (_remainingTime > MAX_DURATION)
		{
			_remainingTime = MAX_DURATION;
		}
	}
	
	public Point3D getHome()
	{
		return new Point3D(_homeX, _homeY, _homeZ);
	}
	
	public void setHome(int x, int y, int z)
	{
		_homeX = x;
		_homeY = y;
		_homeZ = z;
	}
	
	public void setHome(L2Character c)
	{
		setHome(c.getX(), c.getY(), c.getZ());
	}
	
	public int getRemainingTime()
	{
		return _remainingTime;
	}
	
	public void setRemainingTime(int duration)
	{
		_remainingTime = duration;
	}
	
	public int getFoodType()
	{
		return _foodSkillId;
	}
	
	public void setFoodType(int foodItemId)
	{
		if (foodItemId > 0)
		{
			_foodSkillId = foodItemId;
			
			if (_durationCheckTask != null)
			{
				_durationCheckTask.cancel(true);
			}
			_durationCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckDuration(this), DURATION_CHECK_INTERVAL, DURATION_CHECK_INTERVAL);
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		getAI().stopFollow();
		if (_buffTask != null)
		{
			_buffTask.cancel(true);
		}
		if (_durationCheckTask != null)
		{
			_durationCheckTask.cancel(true);
		}
		
		if ((_owner != null) && (_owner.getTrainedBeasts() != null))
		{
			_owner.getTrainedBeasts().remove(this);
		}
		_buffTask = null;
		_durationCheckTask = null;
		_owner = null;
		_foodSkillId = 0;
		_remainingTime = 0;
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !_isFreyaBeast;
	}
	
	public boolean isFreyaBeast()
	{
		return _isFreyaBeast;
	}
	
	public void addBeastSkill(L2Skill skill)
	{
		if (_beastSkills == null)
		{
			_beastSkills = new FastList<>();
		}
		_beastSkills.add(skill);
	}
	
	public void castBeastSkills()
	{
		if ((_owner == null) || (_beastSkills == null))
		{
			return;
		}
		int delay = 100;
		for (L2Skill skill : _beastSkills)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new buffCast(skill), delay);
			delay += (100 + skill.getHitTime());
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new buffCast(null), delay);
	}
	
	private class buffCast implements Runnable
	{
		private final L2Skill _skill;
		
		public buffCast(L2Skill skill)
		{
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			if (_skill == null)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
			}
			else
			{
				sitCastAndFollow(_skill, _owner);
			}
		}
	}
	
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public void setOwner(L2PcInstance owner)
	{
		if (owner != null)
		{
			_owner = owner;
			setTitle(owner.getName());
			setShowSummonAnimation(true);
			broadcastPacket(new AbstractNpcInfo.NpcInfo(this, owner));
			
			owner.addTrainedBeast(this);
			
			getAI().startFollow(_owner, 100);
			
			if (!_isFreyaBeast)
			{
				int totalBuffsAvailable = 0;
				for (L2Skill skill : getTemplate().getSkills().values())
				{
					if (skill.getSkillType() == L2SkillType.BUFF)
					{
						totalBuffsAvailable++;
					}
				}
				
				if (_buffTask != null)
				{
					_buffTask.cancel(true);
				}
				_buffTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckOwnerBuffs(this, totalBuffsAvailable), BUFF_INTERVAL, BUFF_INTERVAL);
			}
		}
		else
		{
			deleteMe();
		}
	}
	
	public boolean isTooFarFromHome()
	{
		return !isInsideRadius(_homeX, _homeY, _homeZ, MAX_DISTANCE_FROM_HOME, true, true);
	}
	
	@Override
	public void deleteMe()
	{
		if (_buffTask != null)
		{
			_buffTask.cancel(true);
		}
		_durationCheckTask.cancel(true);
		stopHpMpRegeneration();
		
		if ((_owner != null) && (_owner.getTrainedBeasts() != null))
		{
			_owner.getTrainedBeasts().remove(this);
		}
		setTarget(null);
		_buffTask = null;
		_durationCheckTask = null;
		_owner = null;
		_foodSkillId = 0;
		_remainingTime = 0;
		
		super.deleteMe();
	}
	
	public void onOwnerGotAttacked(L2Character attacker)
	{
		if ((_owner == null) || !_owner.isOnline())
		{
			deleteMe();
			return;
		}
		
		if (!_owner.isInsideRadius(this, MAX_DISTANCE_FROM_OWNER, true, true))
		{
			getAI().startFollow(_owner);
			return;
		}
		
		if (_owner.isDead() || _isFreyaBeast)
		{
			return;
		}
		
		if (isCastingNow())
		{
			return;
		}
		
		float HPRatio = ((float) _owner.getCurrentHp()) / _owner.getMaxHp();
		
		if (HPRatio >= 0.8)
		{
			for (L2Skill skill : getTemplate().getSkills().values())
			{
				if ((skill.getSkillType() == L2SkillType.DEBUFF) && (Rnd.get(3) < 1) && ((attacker != null) && (attacker.getFirstEffect(skill) != null)))
				{
					sitCastAndFollow(skill, attacker);
				}
			}
		}
		else if (HPRatio < 0.5)
		{
			int chance = 1;
			if (HPRatio < 0.25)
			{
				chance = 2;
			}
			
			for (L2Skill skill : getTemplate().getSkills().values())
			{
				if ((Rnd.get(5) < chance) && skill.hasEffectType(L2EffectType.CPHEAL, L2EffectType.HEAL, L2EffectType.HEAL_PERCENT, L2EffectType.MANAHEAL_BY_LEVEL, L2EffectType.MANAHEAL_PERCENT))
				{
					sitCastAndFollow(skill, _owner);
				}
			}
		}
	}
	
	protected void sitCastAndFollow(L2Skill skill, L2Character target)
	{
		stopMove(null);
		broadcastPacket(new StopMove(this));
		getAI().setIntention(AI_INTENTION_IDLE);
		
		setTarget(target);
		doCast(skill);
		getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
	}
	
	private static class CheckDuration implements Runnable
	{
		private final L2TamedBeastInstance _tamedBeast;
		
		CheckDuration(L2TamedBeastInstance tamedBeast)
		{
			_tamedBeast = tamedBeast;
		}
		
		@Override
		public void run()
		{
			int foodTypeSkillId = _tamedBeast.getFoodType();
			L2PcInstance owner = _tamedBeast.getOwner();
			
			L2ItemInstance item = null;
			if (_tamedBeast._isFreyaBeast)
			{
				item = owner.getInventory().getItemByItemId(foodTypeSkillId);
				if ((item != null) && (item.getCount() >= 1))
				{
					owner.destroyItem("BeastMob", item, 1, _tamedBeast, true);
					_tamedBeast.broadcastPacket(new SocialAction(_tamedBeast.getObjectId(), 3));
				}
				else
				{
					_tamedBeast.deleteMe();
				}
			}
			else
			{
				_tamedBeast.setRemainingTime(_tamedBeast.getRemainingTime() - DURATION_CHECK_INTERVAL);
				if (foodTypeSkillId == 2188)
				{
					item = owner.getInventory().getItemByItemId(6643);
				}
				else if (foodTypeSkillId == 2189)
				{
					item = owner.getInventory().getItemByItemId(6644);
				}
				
				if ((item != null) && (item.getCount() >= 1))
				{
					L2Object oldTarget = owner.getTarget();
					owner.setTarget(_tamedBeast);
					L2Object[] targets =
					{
						_tamedBeast
					};
					
					owner.callSkill(SkillHolder.getInstance().getInfo(foodTypeSkillId, 1), targets);
					owner.setTarget(oldTarget);
				}
				else
				{
					if (_tamedBeast.getRemainingTime() < (MAX_DURATION - 300000))
					{
						_tamedBeast.setRemainingTime(-1);
					}
				}
				
				if (_tamedBeast.getRemainingTime() <= 0)
				{
					_tamedBeast.deleteMe();
				}
			}
		}
	}
	
	private class CheckOwnerBuffs implements Runnable
	{
		private final L2TamedBeastInstance _tamedBeast;
		private final int _numBuffs;
		
		CheckOwnerBuffs(L2TamedBeastInstance tamedBeast, int numBuffs)
		{
			_tamedBeast = tamedBeast;
			_numBuffs = numBuffs;
		}
		
		@Override
		public void run()
		{
			L2PcInstance owner = _tamedBeast.getOwner();
			
			if ((owner == null) || !owner.isOnline())
			{
				deleteMe();
				return;
			}
			
			if (!isInsideRadius(owner, MAX_DISTANCE_FROM_OWNER, true, true))
			{
				getAI().startFollow(owner);
				return;
			}
			
			if (owner.isDead())
			{
				return;
			}
			
			if (isCastingNow())
			{
				return;
			}
			
			int totalBuffsOnOwner = 0;
			int i = 0;
			int rand = Rnd.get(_numBuffs);
			L2Skill buffToGive = null;
			
			for (L2Skill skill : _tamedBeast.getTemplate().getSkills().values())
			{
				if (skill.getSkillType() == L2SkillType.BUFF)
				{
					if (i++ == rand)
					{
						buffToGive = skill;
					}
					if (owner.getFirstEffect(skill) != null)
					{
						totalBuffsOnOwner++;
					}
				}
			}
			
			if (((_numBuffs * 2) / 3) > totalBuffsOnOwner)
			{
				_tamedBeast.sitCastAndFollow(buffToGive, owner);
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _tamedBeast.getOwner());
		}
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if ((player == null) || !canTarget(player))
		{
			return;
		}
		
		if (this != player.getTarget())
		{
			player.setTarget(this);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && (Math.abs(player.getZ() - getZ()) < 100))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}