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
package l2e.gameserver.model.actor.stat;

import l2e.Config;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.xml.ExperienceParser;
import l2e.gameserver.data.xml.PetsParser;
import l2e.gameserver.model.L2PetLevelData;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.instance.L2ClassMasterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.actor.transform.TransformTemplate;
import l2e.gameserver.model.entity.RecoBonus;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.restriction.GlobalRestrictions;
import l2e.gameserver.model.stats.MoveType;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.ExVitalityPointInfo;
import l2e.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.util.Util;

public class PcStat extends PlayableStat
{
	private int _oldMaxHp;
	private int _oldMaxMp;
	private int _oldMaxCp;
	private float _vitalityPoints = 1;
	private byte _vitalityLevel = 0;
	private long _startingXp;
	
	public static final int VITALITY_LEVELS[] =
	{
		240,
		2000,
		13000,
		17000,
		20000
	};
	public static final int MAX_VITALITY_POINTS = VITALITY_LEVELS[4];
	public static final int MIN_VITALITY_POINTS = 1;
	
	public PcStat(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addExp(long value)
	{
		L2PcInstance activeChar = getActiveChar();
		
		if (!getActiveChar().getAccessLevel().canGainExp())
		{
			return false;
		}
		
		if (!super.addExp(value))
		{
			return false;
		}
		
		if (!activeChar.isCursedWeaponEquipped() && (activeChar.getKarma() > 0) && (activeChar.isGM() || !activeChar.isInsideZone(ZoneId.PVP)))
		{
			int karmaLost = activeChar.calculateKarmaLost(value);
			if (karmaLost > 0)
			{
				activeChar.setKarma(activeChar.getKarma() - karmaLost);
			}
		}
		activeChar.sendPacket(new UserInfo(activeChar));
		activeChar.sendPacket(new ExBrExtraUserInfo(activeChar));
		return true;
	}
	
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		return addExpAndSp(addToExp, addToSp, false);
	}
	
	public boolean addExpAndSp(long addToExp, int addToSp, boolean useBonuses)
	{
		L2PcInstance activeChar = getActiveChar();
		
		double basePercent = addToExp;
		if (useBonuses)
		{
			if (Config.ENABLE_VITALITY)
			{
				if (activeChar.isAdventBlessingActive())
				{
					addToExp *= Config.RATE_VITALITY_LEVEL_4;
					addToSp *= Config.RATE_VITALITY_LEVEL_4;
				}
				else
				{
					switch (_vitalityLevel)
					{
						case 1:
							addToExp *= Config.RATE_VITALITY_LEVEL_1;
							addToSp *= Config.RATE_VITALITY_LEVEL_1;
							break;
						case 2:
							addToExp *= Config.RATE_VITALITY_LEVEL_2;
							addToSp *= Config.RATE_VITALITY_LEVEL_2;
							break;
						case 3:
							addToExp *= Config.RATE_VITALITY_LEVEL_3;
							addToSp *= Config.RATE_VITALITY_LEVEL_3;
							break;
						case 4:
							addToExp *= Config.RATE_VITALITY_LEVEL_4;
							addToSp *= Config.RATE_VITALITY_LEVEL_4;
							break;
					}
				}
			}
			if ((addToExp > 0) && !activeChar.isInsideZone(ZoneId.PEACE))
			{
				activeChar.startAdventTask();
			}
		}
		basePercent = basePercent / addToExp;
		
		if (!activeChar.getAccessLevel().canGainExp())
		{
			return false;
		}
		
		long baseExp = addToExp;
		int baseSp = addToSp;
		
		double bonusExp = 1.;
		double bonusSp = 1.;
		
		if (useBonuses)
		{
			bonusExp = getExpBonusMultiplier();
			bonusSp = getSpBonusMultiplier();
		}
		
		addToExp *= bonusExp;
		addToSp *= bonusSp;
		
		float ratioTakenByPlayer = 0;
		
		if (activeChar.hasPet() && Util.checkIfInShortRadius(Config.ALT_PARTY_RANGE, activeChar, activeChar.getSummon(), false))
		{
			L2PetInstance pet = (L2PetInstance) activeChar.getSummon();
			ratioTakenByPlayer = pet.getPetLevelData().getOwnerExpTaken() / 100f;
			
			if (ratioTakenByPlayer > 1)
			{
				ratioTakenByPlayer = 1;
			}
			
			if (!pet.isDead())
			{
				pet.addExpAndSp((long) (addToExp * (1 - ratioTakenByPlayer)), (int) (addToSp * (1 - ratioTakenByPlayer)));
			}
			
			addToExp = (long) (addToExp * ratioTakenByPlayer);
			addToSp = (int) (addToSp * ratioTakenByPlayer);
		}
		
		if (!super.addExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		SystemMessage sm = null;
		if ((addToExp == 0) && (addToSp != 0))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP);
			sm.addNumber(addToSp);
		}
		else if ((addToSp == 0) && (addToExp != 0))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE);
			sm.addNumber((int) addToExp);
		}
		else
		{
			if ((addToExp - baseExp) > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4);
				sm.addNumber((int) addToExp);
				sm.addNumber((int) (addToExp - baseExp));
				sm.addNumber(addToSp);
				sm.addNumber((addToSp - baseSp));
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP);
				sm.addNumber((int) addToExp);
				sm.addNumber(addToSp);
			}
		}
		activeChar.sendPacket(sm);
		return true;
	}
	
	@Override
	public boolean removeExpAndSp(long addToExp, int addToSp)
	{
		return removeExpAndSp(addToExp, addToSp, true);
	}
	
	public boolean removeExpAndSp(long addToExp, int addToSp, boolean sendMessage)
	{
		int level = getLevel();
		if (!super.removeExpAndSp(addToExp, addToSp))
		{
			return false;
		}
		
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
			sm.addNumber((int) addToExp);
			getActiveChar().sendPacket(sm);
			sm = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
			sm.addNumber(addToSp);
			getActiveChar().sendPacket(sm);
			if (getLevel() < level)
			{
				getActiveChar().broadcastStatusUpdate();
			}
		}
		return true;
	}
	
	@Override
	public final boolean addLevel(byte value)
	{
		if ((getLevel() + value) > (ExperienceParser.getInstance().getMaxLevel() - 1))
		{
			return false;
		}
		
		if (!getActiveChar().getEvents().onLevelChange(value))
		{
			return false;
		}
		
		boolean levelIncreased = super.addLevel(value);
		if (levelIncreased)
		{
			if (!Config.DISABLE_TUTORIAL)
			{
				QuestState qs = getActiveChar().getQuestState("_255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE40", null, getActiveChar());
				}
			}
			
			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
			getActiveChar().sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
			GlobalRestrictions.levelChanged(getActiveChar());
			L2ClassMasterInstance.showQuestionMark(getActiveChar());
		}
		
		getActiveChar().rewardSkills();
		
		if (getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().updateClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
		}
		if (getActiveChar().isInParty())
		{
			getActiveChar().getParty().recalculatePartyLevel();
		}
		
		if (getActiveChar().isTransformed() || getActiveChar().isInStance())
		{
			getActiveChar().getTransformation().onLevelUp(getActiveChar());
		}
		
		if (getActiveChar().hasPet())
		{
			final L2PetInstance pet = (L2PetInstance) getActiveChar().getSummon();
			if (pet.getPetData().isSynchLevel() && (pet.getLevel() != getLevel()))
			{
				pet.getStat().setLevel(getLevel());
				pet.getStat().getExpForLevel(getActiveChar().getLevel());
				pet.setCurrentHp(pet.getMaxHp());
				pet.setCurrentMp(pet.getMaxMp());
				pet.broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
				pet.updateAndBroadcastStatus(1);
			}
		}
		
		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().sendPacket(su);
		
		getActiveChar().refreshOverloaded();
		getActiveChar().refreshExpertisePenalty();
		
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));
		getActiveChar().sendPacket(new ExBrExtraUserInfo(getActiveChar()));
		getActiveChar().sendPacket(new ExVoteSystemInfo(getActiveChar()));
		getActiveChar().incAdventPoints(2000, false);
		
		return levelIncreased;
	}
	
	@Override
	public boolean addSp(int value)
	{
		if (!super.addSp(value))
		{
			return false;
		}
		
		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.SP, getSp());
		getActiveChar().sendPacket(su);
		
		return true;
	}
	
	@Override
	public final long getExpForLevel(int level)
	{
		return ExperienceParser.getInstance().getExpForLevel(level);
	}
	
	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
	
	@Override
	public final long getExp()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();
		}
		
		return super.getExp();
	}
	
	public final long getBaseExp()
	{
		return super.getExp();
	}
	
	@Override
	public final void setExp(long value)
	{
		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
		}
		else
		{
			super.setExp(value);
		}
	}
	
	public void setStartingExp(long value)
	{
		if (Config.BOTREPORT_ENABLE)
		{
			_startingXp = value;
		}
	}
	
	public long getStartingExp()
	{
		return _startingXp;
	}
	
	@Override
	public final byte getLevel()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
		}
		
		return super.getLevel();
	}
	
	public final byte getBaseLevel()
	{
		return super.getLevel();
	}
	
	@Override
	public final void setLevel(byte value)
	{
		if (value > (ExperienceParser.getInstance().getMaxLevel() - 1))
		{
			value = (byte) (ExperienceParser.getInstance().getMaxLevel() - 1);
		}
		
		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		}
		else
		{
			super.setLevel(value);
		}
	}
	
	@Override
	public final int getMaxCp()
	{
		int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stats.MAX_CP, getActiveChar().getTemplate().getBaseCpMax(getActiveChar().getLevel()));
		if (val != _oldMaxCp)
		{
			_oldMaxCp = val;
			
			if (getActiveChar().getStatus().getCurrentCp() != val)
			{
				getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp());
			}
		}
		return val;
	}
	
	@Override
	public final int getMaxHp()
	{
		int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stats.MAX_HP, getActiveChar().getTemplate().getBaseHpMax(getActiveChar().getLevel()));
		if (val != _oldMaxHp)
		{
			_oldMaxHp = val;
			
			if (getActiveChar().getStatus().getCurrentHp() != val)
			{
				getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp());
			}
		}
		
		return val;
	}
	
	@Override
	public final int getMaxMp()
	{
		int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stats.MAX_MP, getActiveChar().getTemplate().getBaseMpMax(getActiveChar().getLevel()));
		if (val != _oldMaxMp)
		{
			_oldMaxMp = val;
			
			if (getActiveChar().getStatus().getCurrentMp() != val)
			{
				getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp());
			}
		}
		
		return val;
	}
	
	@Override
	public final int getSp()
	{
		if (getActiveChar().isSubClassActive())
		{
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		}
		
		return super.getSp();
	}
	
	public final int getBaseSp()
	{
		return super.getSp();
	}
	
	@Override
	public final void setSp(int value)
	{
		if (getActiveChar().isSubClassActive())
		{
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
		}
		else
		{
			super.setSp(value);
		}
	}
	
	@Override
	public float getBaseMoveSpeed(MoveType type)
	{
		final L2PcInstance player = getActiveChar();
		float val = super.getBaseMoveSpeed(type);
		
		if (getActiveChar().isTransformed())
		{
			final TransformTemplate template = getActiveChar().getTransformation().getTemplate(getActiveChar());
			if (template != null)
			{
				return template.getBaseMoveSpeed(type);
			}
		}
		else if (player.isMounted())
		{
			final L2PetLevelData data = PetsParser.getInstance().getPetLevelData(getActiveChar().getMountNpcId(), getActiveChar().getMountLevel());
			if (data != null)
			{
				return data.getSpeedOnRide(type);
			}
		}
		return val;
	}
	
	@Override
	public int getRunSpeed()
	{
		int val = super.getRunSpeed() + Config.RUN_SPD_BOOST;
		
		if ((val > Config.MAX_RUN_SPEED) && !getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
		{
			return Config.MAX_RUN_SPEED;
		}
		
		if (getActiveChar().isMounted())
		{
			if ((getActiveChar().getMountLevel() - getActiveChar().getLevel()) >= 10)
			{
				val /= 2;
			}
			
			if (getActiveChar().isHungry())
			{
				val /= 2;
			}
		}
		
		if (Config.SPEED_UP_RUN)
		{
			if (getActiveChar().isInsideZone(ZoneId.PEACE))
			{
				val *= 2;
			}
		}
		return val;
	}
	
	@Override
	public int getWalkSpeed()
	{
		int val = super.getWalkSpeed() + Config.RUN_SPD_BOOST;
		
		if ((val > Config.MAX_RUN_SPEED) && !getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
		{
			return Config.MAX_RUN_SPEED;
		}
		
		if (getActiveChar().isMounted())
		{
			if ((getActiveChar().getMountLevel() - getActiveChar().getLevel()) >= 10)
			{
				val /= 2;
			}
			
			if (getActiveChar().isHungry())
			{
				val /= 2;
			}
		}
		
		if (Config.SPEED_UP_RUN)
		{
			if (getActiveChar().isInsideZone(ZoneId.PEACE))
			{
				val *= 2;
			}
		}
		return val;
	}
	
	@Override
	public int getPAtkSpd()
	{
		int val = super.getPAtkSpd();
		
		if ((val > Config.MAX_PATK_SPEED) && !getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
		{
			return Config.MAX_PATK_SPEED;
		}
		
		return val;
	}
	
	@Override
	public float getMovementSpeedMultiplier()
	{
		if (getActiveChar().isMounted())
		{
			final L2PetLevelData data = PetsParser.getInstance().getPetLevelData(getActiveChar().getMountNpcId(), getActiveChar().getMountLevel());
			float baseSpeed = data != null ? data.getSpeedOnRide(MoveType.RUN) : NpcTable.getInstance().getTemplate(getActiveChar().getMountNpcId()).getBaseMoveSpeed(MoveType.RUN);
			return (getRunSpeed() / baseSpeed);
		}
		return super.getMovementSpeedMultiplier();
	}
	
	private void updateVitalityLevel(boolean quiet)
	{
		final byte level;
		
		if (_vitalityPoints <= VITALITY_LEVELS[0])
		{
			level = 0;
		}
		else if (_vitalityPoints <= VITALITY_LEVELS[1])
		{
			level = 1;
		}
		else if (_vitalityPoints <= VITALITY_LEVELS[2])
		{
			level = 2;
		}
		else if (_vitalityPoints <= VITALITY_LEVELS[3])
		{
			level = 3;
		}
		else
		{
			level = 4;
		}
		
		if (!quiet && (level != _vitalityLevel))
		{
			if (level < _vitalityLevel)
			{
				getActiveChar().sendPacket(SystemMessageId.VITALITY_HAS_DECREASED);
			}
			else
			{
				getActiveChar().sendPacket(SystemMessageId.VITALITY_HAS_INCREASED);
			}
			if (level == 0)
			{
				getActiveChar().sendPacket(SystemMessageId.VITALITY_IS_EXHAUSTED);
			}
			else if (level == 4)
			{
				getActiveChar().sendPacket(SystemMessageId.VITALITY_IS_AT_MAXIMUM);
			}
		}
		
		_vitalityLevel = level;
	}
	
	/*
	 * Return current vitality points in integer format
	 */
	public int getVitalityPoints()
	{
		return (int) _vitalityPoints;
	}
	
	public void setVitalityPoints(int points, boolean quiet)
	{
		points = Math.min(Math.max(points, MIN_VITALITY_POINTS), MAX_VITALITY_POINTS);
		if (points == _vitalityPoints)
		{
			return;
		}
		
		_vitalityPoints = points;
		updateVitalityLevel(quiet);
		getActiveChar().sendPacket(new ExVitalityPointInfo(getVitalityPoints()));
	}
	
	public synchronized void updateVitalityPoints(float points, boolean useRates, boolean quiet)
	{
		if ((points == 0) || !Config.ENABLE_VITALITY)
		{
			return;
		}
		
		if (useRates)
		{
			if (getActiveChar().isLucky())
			{
				return;
			}
			
			if (points < 0)
			{
				int stat = (int) calcStat(Stats.VITALITY_CONSUME_RATE, 1, getActiveChar(), null);
				
				if (getActiveChar().isAdventBlessingActive())
				{
					stat -= 10;
				}
				
				if (stat == 0)
				{
					return;
				}
				if (stat < 0)
				{
					points = -points;
				}
			}
			
			if (points > 0)
			{
				points *= Config.RATE_VITALITY_GAIN;
			}
			else
			{
				points *= Config.RATE_VITALITY_LOST;
			}
		}
		
		if (points > 0)
		{
			points = Math.min(_vitalityPoints + points, MAX_VITALITY_POINTS);
		}
		else
		{
			points = Math.max(_vitalityPoints + points, MIN_VITALITY_POINTS);
		}
		
		if (Math.abs(points - _vitalityPoints) <= 1e-6)
		{
			return;
		}
		
		_vitalityPoints = points;
		updateVitalityLevel(quiet);
	}
	
	public double getVitalityMultiplier()
	{
		double vitality = 1.0;
		
		if (Config.ENABLE_VITALITY)
		{
			switch (getVitalityLevel())
			{
				case 1:
					vitality = Config.RATE_VITALITY_LEVEL_1;
					break;
				case 2:
					vitality = Config.RATE_VITALITY_LEVEL_2;
					break;
				case 3:
					vitality = Config.RATE_VITALITY_LEVEL_3;
					break;
				case 4:
					vitality = Config.RATE_VITALITY_LEVEL_4;
					break;
			}
		}
		
		return vitality;
	}
	
	public byte getVitalityLevel()
	{
		return _vitalityLevel;
	}
	
	public double getExpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double nevits = 1.0;
		double hunting = 1.0;
		double bonusExp = 1.0;
		
		vitality = getVitalityMultiplier();
		
		nevits = RecoBonus.getRecoMultiplier(getActiveChar());
		
		bonusExp = 1 + (calcStat(Stats.BONUS_EXP, 0, null, null) / 100);
		
		if (vitality > 1.0)
		{
			bonus += (vitality - 1);
		}
		if (nevits > 1.0)
		{
			bonus += (nevits - 1);
		}
		if (hunting > 1.0)
		{
			bonus += (hunting - 1);
		}
		if (bonusExp > 1)
		{
			bonus += (bonusExp - 1);
		}
		
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, Config.MAX_BONUS_EXP);
		
		return bonus;
	}
	
	public double getSpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double nevits = 1.0;
		double hunting = 1.0;
		double bonusSp = 1.0;
		
		vitality = getVitalityMultiplier();
		
		nevits = RecoBonus.getRecoMultiplier(getActiveChar());
		
		bonusSp = 1 + (calcStat(Stats.BONUS_SP, 0, null, null) / 100);
		
		if (vitality > 1.0)
		{
			bonus += (vitality - 1);
		}
		if (nevits > 1.0)
		{
			bonus += (nevits - 1);
		}
		if (hunting > 1.0)
		{
			bonus += (hunting - 1);
		}
		if (bonusSp > 1)
		{
			bonus += (bonusSp - 1);
		}
		
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, Config.MAX_BONUS_SP);
		
		return bonus;
	}
}