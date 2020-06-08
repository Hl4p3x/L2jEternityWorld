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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.CharSummonHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.SummonEffects;
import l2e.gameserver.model.SummonEffects.SummonEffect;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.l2skills.L2SkillSummon;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.SetSummonRemainTime;
import gnu.trove.map.hash.TIntObjectHashMap;

public class L2ServitorInstance extends L2Summon
{
	protected static final Logger log = Logger.getLogger(L2ServitorInstance.class.getName());
	
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_summon_skills_save (ownerId,ownerClassIndex,summonSkillId,skill_id,skill_level,effect_count,effect_cur_time,buff_index) VALUES (?,?,?,?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time,buff_index FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=? ORDER BY buff_index ASC";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_summon_skills_save WHERE ownerId=? AND ownerClassIndex=? AND summonSkillId=?";
	
	private float _expPenalty = 0;
	private int _itemConsumeId;
	private int _itemConsumeCount;
	private int _itemConsumeSteps;
	private final int _totalLifeTime;
	private final int _timeLostIdle;
	private final int _timeLostActive;
	private int _timeRemaining;
	private int _nextItemConsumeTime;
	public int lastShowntimeRemaining;
	
	protected Future<?> _summonLifeTask;
	
	private int _referenceSkill;
	
	private boolean _shareElementals = false;
	private double _sharedElementalsPercent = 1;
	
	public L2ServitorInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner);
		setInstanceType(InstanceType.L2ServitorInstance);
		setShowSummonAnimation(true);
		
		if (skill != null)
		{
			final L2SkillSummon summonSkill = (L2SkillSummon) skill;
			_itemConsumeId = summonSkill.getItemConsumeIdOT();
			_itemConsumeCount = summonSkill.getItemConsumeOT();
			_itemConsumeSteps = summonSkill.getItemConsumeSteps();
			_totalLifeTime = summonSkill.getTotalLifeTime();
			_timeLostIdle = summonSkill.getTimeLostIdle();
			_timeLostActive = summonSkill.getTimeLostActive();
			_referenceSkill = summonSkill.getId();
		}
		else
		{
			_itemConsumeId = 0;
			_itemConsumeCount = 0;
			_itemConsumeSteps = 0;
			_totalLifeTime = 1200000;
			_timeLostIdle = 1000;
			_timeLostActive = 1000;
		}
		_timeRemaining = _totalLifeTime;
		lastShowntimeRemaining = _totalLifeTime;
		
		if (_itemConsumeId == 0)
		{
			_nextItemConsumeTime = -1;
		}
		else if (_itemConsumeSteps == 0)
		{
			_nextItemConsumeTime = -1;
		}
		else
		{
			_nextItemConsumeTime = _totalLifeTime - (_totalLifeTime / (_itemConsumeSteps + 1));
		}
		
		int delay = 1000;
		
		if (Config.DEBUG && (_itemConsumeCount != 0))
		{
			_log.warning(getClass().getSimpleName() + ": Item Consume ID: " + _itemConsumeId + ", Count: " + _itemConsumeCount + ", Rate: " + _itemConsumeSteps + " times.");
		}
		if (Config.DEBUG)
		{
			_log.warning(getClass().getSimpleName() + ": Task Delay " + (delay / 1000) + " seconds.");
		}
		
		_summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SummonLifetime(getOwner(), this), delay, delay);
	}
	
	@Override
	public final int getLevel()
	{
		return (getTemplate() != null ? getTemplate().getLevel() : 0);
	}
	
	@Override
	public int getSummonType()
	{
		return 1;
	}
	
	public void setExpPenalty(float expPenalty)
	{
		_expPenalty = expPenalty;
	}
	
	public float getExpPenalty()
	{
		return _expPenalty;
	}
	
	public void setSharedElementals(final boolean val)
	{
		_shareElementals = val;
	}
	
	public boolean isSharingElementals()
	{
		return _shareElementals;
	}
	
	public void setSharedElementalsValue(final double val)
	{
		_sharedElementalsPercent = val;
	}
	
	public double sharedElementalsPercent()
	{
		return _sharedElementalsPercent;
	}
	
	public int getItemConsumeCount()
	{
		return _itemConsumeCount;
	}
	
	public int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	public int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}
	
	public int getNextItemConsumeTime()
	{
		return _nextItemConsumeTime;
	}
	
	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}
	
	public int getTimeLostIdle()
	{
		return _timeLostIdle;
	}
	
	public int getTimeLostActive()
	{
		return _timeLostActive;
	}
	
	public int getTimeRemaining()
	{
		return _timeRemaining;
	}
	
	public void setNextItemConsumeTime(int value)
	{
		_nextItemConsumeTime = value;
	}
	
	public void decNextItemConsumeTime(int value)
	{
		_nextItemConsumeTime -= value;
	}
	
	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}
	
	public void addExpAndSp(int addToExp, int addToSp)
	{
		getOwner().addExpAndSp(addToExp, addToSp);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (Config.DEBUG)
		{
			_log.warning(getClass().getSimpleName() + ": " + getTemplate().getName() + " (" + getOwner().getName() + ") has been killed.");
		}
		
		if (_summonLifeTask != null)
		{
			_summonLifeTask.cancel(false);
			_summonLifeTask = null;
		}
		
		CharSummonHolder.getInstance().removeServitor(getOwner());
		
		return true;
		
	}
	
	@Override
	public void doCast(L2Skill skill)
	{
		final int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		if (petLevel >= 70)
		{
			skillLevel += (petLevel - 65) / 10;
		}
		
		if (skillLevel < 1)
		{
			skillLevel = 1;
		}
		
		final L2Skill skillToCast = SkillHolder.getInstance().getInfo(skill.getId(), skillLevel);
		
		if (skillToCast != null)
		{
			super.doCast(skillToCast);
		}
		else
		{
			super.doCast(skill);
		}
	}
	
	@Override
	public void setRestoreSummon(boolean val)
	{
		_restoreSummon = val;
	}
	
	@Override
	public final void stopSkillEffects(int skillId)
	{
		super.stopSkillEffects(skillId);
		final TIntObjectHashMap<List<SummonEffect>> servitorEffects = SummonEffects.getInstance().getServitorEffects(getOwner());
		if (servitorEffects != null)
		{
			final List<SummonEffect> effects = servitorEffects.get(getReferenceSkill());
			if ((effects != null) && !effects.isEmpty())
			{
				for (SummonEffect effect : effects)
				{
					final L2Skill skill = effect.getSkill();
					if ((skill != null) && (skill.getId() == skillId))
					{
						effects.remove(effect);
					}
				}
			}
		}
	}
	
	@Override
	public void store()
	{
		if ((_referenceSkill == 0) || isDead())
		{
			return;
		}
		
		if (Config.RESTORE_SERVITOR_ON_RECONNECT)
		{
			CharSummonHolder.getInstance().saveSummon(this);
		}
	}
	
	@Override
	public void storeEffect(boolean storeEffects)
	{
		if (!Config.SUMMON_STORE_SKILL_COOLTIME)
		{
			return;
		}
		
		if (getOwner().isInOlympiadMode())
		{
			return;
		}
		
		if (SummonEffects.getInstance().getServitorEffectsOwner().contains(getOwner().getObjectId()) && SummonEffects.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).contains(getOwner().getClassIndex()) && SummonEffects.getInstance().getServitorEffects(getOwner()).contains(getReferenceSkill()))
		{
			SummonEffects.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()).clear();
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE))
		{
			statement.setInt(1, getOwner().getObjectId());
			statement.setInt(2, getOwner().getClassIndex());
			statement.setInt(3, getReferenceSkill());
			statement.execute();
			
			int buff_index = 0;
			
			final List<Integer> storedSkills = new FastList<>();
			
			if (storeEffects)
			{
				try (PreparedStatement ps2 = con.prepareStatement(ADD_SKILL_SAVE))
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
						
						if (effect.getAbnormalType().equalsIgnoreCase("LIFE_FORCE_OTHERS"))
						{
							continue;
						}
						
						L2Skill skill = effect.getSkill();
						
						if (skill.isDance() && !Config.ALT_STORE_DANCES)
						{
							continue;
						}
						
						if (storedSkills.contains(skill.getReuseHashCode()))
						{
							continue;
						}
						
						storedSkills.add(skill.getReuseHashCode());
						
						if (effect.isInUse() && !skill.isToggle())
						{
							ps2.setInt(1, getOwner().getObjectId());
							ps2.setInt(2, getOwner().getClassIndex());
							ps2.setInt(3, getReferenceSkill());
							ps2.setInt(4, skill.getId());
							ps2.setInt(5, skill.getLevel());
							ps2.setInt(6, effect.getTickCount());
							ps2.setInt(7, effect.getTime());
							ps2.setInt(8, ++buff_index);
							ps2.execute();
							
							if (!SummonEffects.getInstance().getServitorEffectsOwner().contains(getOwner().getObjectId()))
							{
								SummonEffects.getInstance().getServitorEffectsOwner().put(getOwner().getObjectId(), new TIntObjectHashMap<TIntObjectHashMap<List<SummonEffect>>>());
							}
							if (!SummonEffects.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).contains(getOwner().getClassIndex()))
							{
								SummonEffects.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).put(getOwner().getClassIndex(), new TIntObjectHashMap<List<SummonEffect>>());
							}
							if (!SummonEffects.getInstance().getServitorEffects(getOwner()).contains(getReferenceSkill()))
							{
								SummonEffects.getInstance().getServitorEffects(getOwner()).put(getReferenceSkill(), new FastList<SummonEffect>());
							}
							SummonEffects.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()).add(SummonEffects.getInstance().new SummonEffect(skill, effect.getTickCount(), effect.getTime()));
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store summon effect data: ", e);
		}
	}
	
	@Override
	public void restoreEffects()
	{
		if (getOwner().isInOlympiadMode())
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (!SummonEffects.getInstance().getServitorEffectsOwner().contains(getOwner().getObjectId()) || !SummonEffects.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).contains(getOwner().getClassIndex()) || !SummonEffects.getInstance().getServitorEffects(getOwner()).contains(getReferenceSkill()))
			{
				try (PreparedStatement statement = con.prepareStatement(RESTORE_SKILL_SAVE))
				{
					statement.setInt(1, getOwner().getObjectId());
					statement.setInt(2, getOwner().getClassIndex());
					statement.setInt(3, getReferenceSkill());
					try (ResultSet rset = statement.executeQuery())
					{
						while (rset.next())
						{
							int effectCount = rset.getInt("effect_count");
							int effectCurTime = rset.getInt("effect_cur_time");
							
							final L2Skill skill = SkillHolder.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
							if (skill == null)
							{
								continue;
							}
							
							if (skill.hasEffects())
							{
								if (!SummonEffects.getInstance().getServitorEffectsOwner().contains(getOwner().getObjectId()))
								{
									SummonEffects.getInstance().getServitorEffectsOwner().put(getOwner().getObjectId(), new TIntObjectHashMap<TIntObjectHashMap<List<SummonEffect>>>());
								}
								if (!SummonEffects.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).contains(getOwner().getClassIndex()))
								{
									SummonEffects.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).put(getOwner().getClassIndex(), new TIntObjectHashMap<List<SummonEffect>>());
								}
								if (!SummonEffects.getInstance().getServitorEffects(getOwner()).contains(getReferenceSkill()))
								{
									SummonEffects.getInstance().getServitorEffects(getOwner()).put(getReferenceSkill(), new FastList<SummonEffect>());
								}
								
								SummonEffects.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()).add(SummonEffects.getInstance().new SummonEffect(skill, effectCount, effectCurTime));
							}
						}
					}
				}
			}
			
			try (PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE))
			{
				statement.setInt(1, getOwner().getObjectId());
				statement.setInt(2, getOwner().getClassIndex());
				statement.setInt(3, getReferenceSkill());
				statement.executeUpdate();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore " + this + " active effect data: " + e.getMessage(), e);
		}
		finally
		{
			if (!SummonEffects.getInstance().getServitorEffectsOwner().contains(getOwner().getObjectId()) || !SummonEffects.getInstance().getServitorEffectsOwner().get(getOwner().getObjectId()).contains(getOwner().getClassIndex()) || !SummonEffects.getInstance().getServitorEffects(getOwner()).contains(getReferenceSkill()))
			{
				return;
			}
			
			for (SummonEffect se : SummonEffects.getInstance().getServitorEffects(getOwner()).get(getReferenceSkill()))
			{
				if (se == null)
				{
					continue;
				}
				Env env = new Env();
				env.setCharacter(this);
				env.setTarget(this);
				env.setSkill(se.getSkill());
				L2Effect ef;
				for (EffectTemplate et : se.getSkill().getEffectTemplates())
				{
					ef = et.getEffect(env);
					if (ef != null)
					{
						ef.setCount(se.getEffectCount());
						ef.setFirstTime(se.getEffectCurTime());
						ef.scheduleEffect();
					}
				}
			}
		}
	}
	
	static class SummonLifetime implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2ServitorInstance _summon;
		
		SummonLifetime(L2PcInstance activeChar, L2ServitorInstance newpet)
		{
			_activeChar = activeChar;
			_summon = newpet;
		}
		
		@Override
		public void run()
		{
			if (Config.DEBUG)
			{
				log.warning(getClass().getSimpleName() + ": " + _summon.getTemplate().getName() + " (" + _activeChar.getName() + ") run task.");
			}
			
			try
			{
				double oldTimeRemaining = _summon.getTimeRemaining();
				int maxTime = _summon.getTotalLifeTime();
				double newTimeRemaining;
				
				if (_summon.isAttackingNow())
				{
					_summon.decTimeRemaining(_summon.getTimeLostActive());
				}
				else
				{
					_summon.decTimeRemaining(_summon.getTimeLostIdle());
				}
				newTimeRemaining = _summon.getTimeRemaining();
				
				if (newTimeRemaining < 0)
				{
					_summon.unSummon(_activeChar);
				}
				else if ((newTimeRemaining <= _summon.getNextItemConsumeTime()) && (oldTimeRemaining > _summon.getNextItemConsumeTime()))
				{
					_summon.decNextItemConsumeTime(maxTime / (_summon.getItemConsumeSteps() + 1));
					
					if ((_summon.getItemConsumeCount() > 0) && (_summon.getItemConsumeId() != 0) && !_summon.isDead() && !_summon.destroyItemByItemId("Consume", _summon.getItemConsumeId(), _summon.getItemConsumeCount(), _activeChar, true))
					{
						_summon.unSummon(_activeChar);
					}
				}
				
				if ((_summon.lastShowntimeRemaining - newTimeRemaining) > (maxTime / 352))
				{
					_summon.sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					_summon.lastShowntimeRemaining = (int) newTimeRemaining;
					_summon.updateEffectIcons();
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "Error on player [" + _activeChar.getName() + "] summon item consume task.", e);
			}
		}
	}
	
	@Override
	public void unSummon(L2PcInstance owner)
	{
		if (Config.DEBUG)
		{
			_log.info(getClass().getSimpleName() + ": " + getTemplate().getName() + " (" + owner.getName() + ") unsummoned.");
		}
		
		if (_summonLifeTask != null)
		{
			_summonLifeTask.cancel(false);
			_summonLifeTask = null;
		}
		
		super.unSummon(owner);
		
		if (!_restoreSummon)
		{
			CharSummonHolder.getInstance().removeServitor(owner);
		}
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		if (Config.DEBUG)
		{
			_log.warning(getClass().getSimpleName() + ": " + getTemplate().getName() + " (" + getOwner().getName() + ") consume.");
		}
		
		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}
	
	public void setTimeRemaining(int time)
	{
		_timeRemaining = time;
	}
	
	public int getReferenceSkill()
	{
		return _referenceSkill;
	}
	
	@Override
	public byte getAttackElement()
	{
		if (isSharingElementals() && (getOwner() != null))
		{
			return getOwner().getAttackElement();
		}
		return super.getAttackElement();
	}
	
	@Override
	public int getAttackElementValue(byte attackAttribute)
	{
		if (isSharingElementals() && (getOwner() != null))
		{
			return (int) (getOwner().getAttackElementValue(attackAttribute) * sharedElementalsPercent());
		}
		return super.getAttackElementValue(attackAttribute);
	}
	
	@Override
	public int getDefenseElementValue(byte defenseAttribute)
	{
		if (isSharingElementals() && (getOwner() != null))
		{
			return (int) (getOwner().getDefenseElementValue(defenseAttribute) * sharedElementalsPercent());
		}
		return super.getDefenseElementValue(defenseAttribute);
	}
	
	@Override
	public boolean isServitor()
	{
		return true;
	}
}