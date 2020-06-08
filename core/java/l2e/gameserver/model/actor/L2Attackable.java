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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ItemsAutoDestroy;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.ai.L2AttackableAI;
import l2e.gameserver.ai.L2CharacterAI;
import l2e.gameserver.ai.L2FortSiegeGuardAI;
import l2e.gameserver.ai.L2SiegeGuardAI;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.xml.HerbDropParser;
import l2e.gameserver.data.xml.ManorParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.EventsDropManager;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.EventDroplist;
import l2e.gameserver.model.EventDroplist.DateDrop;
import l2e.gameserver.model.L2CommandChannel;
import l2e.gameserver.model.L2DropCategory;
import l2e.gameserver.model.L2DropData;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.events.AttackableEvents;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.actor.instance.L2ServitorInstance;
import l2e.gameserver.model.actor.knownlist.AttackableKnownList;
import l2e.gameserver.model.actor.status.AttackableStatus;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.AbnormalEffect;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2EtcItemType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PledgeCrestVote;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.taskmanager.DecayTaskManager;
import l2e.gameserver.util.L2TIntObjectHashMap;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;
import gov.nasa.worldwind.formats.dds.DDSConverter;

public class L2Attackable extends L2Npc
{
	private boolean _isRaid = false;
	private boolean _isRaidMinion = false;
	private boolean _champion = false;
	
	private final Map<L2Character, AggroInfo> _aggroList = new FastMap<L2Character, AggroInfo>().shared();
	private boolean _isReturningToSpawnPoint = false;
	private boolean _canReturnToSpawnPoint = true;
	private boolean _seeThroughSilentMove = false;
	private ItemsHolder[] _sweepItems;
	private ItemsHolder[] _harvestItems;
	private boolean _seeded;
	private int _seedType = 0;
	private int _seederObjId = 0;
	
	private boolean _overhit;
	
	private double _overhitDamage;
	
	private L2Character _overhitAttacker;
	
	private volatile L2CommandChannel _firstCommandChannelAttacked = null;
	private CommandChannelTimer _commandChannelTimer = null;
	private long _commandChannelLastAttack = 0;
	
	private boolean _absorbed;
	
	private final L2TIntObjectHashMap<AbsorberInfo> _absorbersList = new L2TIntObjectHashMap<>();
	
	private boolean _mustGiveExpSp;
	
	private boolean _isSpoil = false;
	
	private int _isSpoiledBy = 0;
	
	protected int _onKillDelay = 5000;
	
	private static final StringBuilder finalString = new StringBuilder();
	
	public static final class AggroInfo
	{
		private final L2Character _attacker;
		private int _hate = 0;
		private int _damage = 0;
		
		AggroInfo(L2Character pAttacker)
		{
			_attacker = pAttacker;
		}
		
		public final L2Character getAttacker()
		{
			return _attacker;
		}
		
		public final int getHate()
		{
			return _hate;
		}
		
		public final int checkHate(L2Character owner)
		{
			if (_attacker.isAlikeDead() || !_attacker.isVisible() || !owner.getKnownList().knowsObject(_attacker))
			{
				_hate = 0;
			}
			
			return _hate;
		}
		
		public final void addHate(int value)
		{
			_hate = (int) Math.min(_hate + (long) value, 999999999);
		}
		
		public final void stopHate()
		{
			_hate = 0;
		}
		
		public final int getDamage()
		{
			return _damage;
		}
		
		public final void addDamage(int value)
		{
			_damage = (int) Math.min(_damage + (long) value, 999999999);
		}
		
		@Override
		public final boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			
			if (obj instanceof AggroInfo)
			{
				return (((AggroInfo) obj).getAttacker() == _attacker);
			}
			
			return false;
		}
		
		@Override
		public final int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	protected final class RewardInfo
	{
		private final L2PcInstance _attacker;
		private int _damage = 0;
		
		public RewardInfo(L2PcInstance attacker, int damage)
		{
			_attacker = attacker;
			_damage = damage;
		}
		
		public L2PcInstance getAttacker()
		{
			return _attacker;
		}
		
		public void addDamage(int damage)
		{
			_damage += damage;
		}
		
		public int getDamage()
		{
			return _damage;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			
			if (obj instanceof RewardInfo)
			{
				return (((RewardInfo) obj)._attacker == _attacker);
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	public static final class AbsorberInfo
	{
		public int _objId;
		public double _absorbedHP;
		
		AbsorberInfo(int objId, double pAbsorbedHP)
		{
			_objId = objId;
			_absorbedHP = pAbsorbedHP;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			
			if (obj instanceof AbsorberInfo)
			{
				return (((AbsorberInfo) obj)._objId == _objId);
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _objId;
		}
	}
	
	public L2Attackable(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Attackable);
		setIsInvul(false);
		_mustGiveExpSp = true;
	}
	
	@Override
	public AttackableKnownList getKnownList()
	{
		return (AttackableKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new AttackableKnownList(this));
	}
	
	@Override
	public AttackableStatus getStatus()
	{
		return (AttackableStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new AttackableStatus(this));
	}
	
	@Override
	public void initCharEvents()
	{
		setCharEvents(new AttackableEvents(this));
	}
	
	@Override
	public AttackableEvents getEvents()
	{
		return (AttackableEvents) super.getEvents();
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
					_ai = new L2AttackableAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return _ai;
	}
	
	public final Map<L2Character, AggroInfo> getAggroList()
	{
		return _aggroList;
	}
	
	public final boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}
	
	public final void setisReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}
	
	public final boolean canReturnToSpawnPoint()
	{
		return _canReturnToSpawnPoint;
	}
	
	public final void setCanReturnToSpawnPoint(boolean value)
	{
		_canReturnToSpawnPoint = value;
	}
	
	public boolean canSeeThroughSilentMove()
	{
		return _seeThroughSilentMove;
	}
	
	public void setSeeThroughSilentMove(boolean val)
	{
		_seeThroughSilentMove = val;
	}
	
	public void useMagic(L2Skill skill)
	{
		if ((skill == null) || isAlikeDead() || skill.isPassive() || isCastingNow() || isSkillDisabled(skill))
		{
			return;
		}
		
		if ((getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))) || (getCurrentHp() <= skill.getHpConsume()))
		{
			return;
		}
		
		if (!skill.isStatic())
		{
			if (skill.isMagic())
			{
				if (isMuted())
				{
					return;
				}
			}
			else
			{
				if (isPhysicalMuted())
				{
					return;
				}
			}
		}
		
		final L2Object target = skill.getFirstOfTargetList(this);
		if (target != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(damage, attacker, true, false, skill);
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (isRaid() && !isMinion() && (attacker != null) && (attacker.getParty() != null) && attacker.getParty().isInCommandChannel() && attacker.getParty().getCommandChannel().meetRaidWarCondition(this))
		{
			if (_firstCommandChannelAttacked == null)
			{
				synchronized (this)
				{
					if (_firstCommandChannelAttacked == null)
					{
						_firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
						if (_firstCommandChannelAttacked != null)
						{
							_commandChannelTimer = new CommandChannelTimer(this);
							_commandChannelLastAttack = System.currentTimeMillis();
							ThreadPoolManager.getInstance().scheduleGeneral(_commandChannelTimer, 10000);
							_firstCommandChannelAttacked.broadcastPacket(new CreatureSay(0, Say2.PARTYROOM_ALL, "", "You have looting rights!"));
						}
					}
				}
			}
			else if (attacker.getParty().getCommandChannel().equals(_firstCommandChannelAttacked))
			{
				_commandChannelLastAttack = System.currentTimeMillis();
			}
		}
		
		if (isEventMob())
		{
			return;
		}
		
		if (attacker != null)
		{
			addDamage(attacker, (int) damage, skill);
		}
		
		if (this instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) this;
			
			if (master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}
			
			master = master.getLeader();
			if ((master != null) && master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}
		}
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	public synchronized void setMustRewardExpSp(boolean value)
	{
		_mustGiveExpSp = value;
	}
	
	public synchronized boolean getMustRewardExpSP()
	{
		return _mustGiveExpSp;
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if ((killer == null) || !super.doDie(killer))
		{
			return false;
		}
		if (killer instanceof L2ServitorInstance)
		{
			killer = ((L2ServitorInstance) killer).getOwner();
		}
		if (killer instanceof L2PetInstance)
		{
			killer = ((L2PetInstance) killer).getOwner();
		}
		if (killer instanceof L2Summon)
		{
			killer = ((L2Summon) killer).getOwner();
		}
		if (killer instanceof L2PcInstance)
		{
			if (killer.isInsideZone(ZoneId.SIEGE))
			{
				return true;
			}
			else if (!killer.isInsideZone(ZoneId.SIEGE))
			{
				((L2PcInstance) killer).setKills(((L2PcInstance) killer).getKills() + Config.ANTIBOT_START_CAPTCHA);
				if ((((L2PcInstance) killer).getKills() >= Config.ANTIBOT_GET_KILLS) || (((L2PcInstance) killer).getKills() == 0))
				{
					int imgId = IdFactory.getInstance().getNextId();
					try
					{
						File captcha = new File("data/images/captcha/captcha.png");
						ImageIO.write(generateCaptcha(), "png", captcha);
						PledgeCrestVote packet = new PledgeCrestVote(imgId, DDSConverter.convertToDDS(captcha).array());
						killer.sendPacket(packet);
					}
					catch (Exception e)
					{
						_log.warning(e.getMessage());
					}
					killer.startAbnormalEffect(AbnormalEffect.REAL_TARGET);
					killer.setIsParalyzed(true);
					killer.setIsInvul(true);
					
					NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
					adminReply.setHtml("<html><title>" + LocalizationStorage.getInstance().getString(((L2PcInstance) killer).getLang(), "CaptchaAntibot.SYSTEM") + "</title><body><center>" + LocalizationStorage.getInstance().getString(((L2PcInstance) killer).getLang(), "CaptchaAntibot.ENTER_DIGITS") + "<br><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + imgId + "\" width=256 height=64><br><font color=\"888888\">" + LocalizationStorage.getInstance().getString(((L2PcInstance) killer).getLang(), "CaptchaAntibot.UPPERCASE_LETTER") + "</font><br><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(((L2PcInstance) killer).getLang(), "CaptchaAntibot.REGISTER_LETTER") + "</font><br><edit var=\"antibot\" width=110><br><button value=\"" + LocalizationStorage.getInstance().getString(((L2PcInstance) killer).getLang(), "CaptchaAntibot.CONFIRM") + "\" action=\"bypass -h voice .antibot $antibot\" width=80 height=26 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"><br>" + LocalizationStorage.getInstance().getString(((L2PcInstance) killer).getLang(), "CaptchaAntibot.TIME_TO_ANSWER") + "</center></body></html>");
					killer.sendPacket(adminReply);
					((L2PcInstance) killer).setCode(finalString);
					ThreadPoolManager.getInstance().scheduleGeneral(new CaptchaTimer((L2PcInstance) killer), Config.ANTIBOT_GET_TIME_TO_FILL);
					((L2PcInstance) killer).setCodeRight(false);
					finalString.replace(0, 5, "");
				}
			}
		}
		
		try
		{
			L2PcInstance player = null;
			
			if (killer != null)
			{
				player = killer.getActingPlayer();
			}
			
			if (player != null)
			{
				if (getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null)
				{
					for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
					{
						ThreadPoolManager.getInstance().scheduleEffect(new OnKillNotifyTask(this, quest, player, (killer != null) && killer.isSummon()), _onKillDelay);
					}
				}
			}
			else if (killer instanceof L2Npc)
			{
				if (getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null)
				{
					for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
					{
						ThreadPoolManager.getInstance().scheduleEffect(new OnKillByMobNotifyTask(this, quest, (L2Npc) killer), _onKillDelay);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
		return true;
	}
	
	protected static class OnKillNotifyTask implements Runnable
	{
		private final L2Attackable _attackable;
		private final Quest _quest;
		private final L2PcInstance _killer;
		private final boolean _isSummon;
		
		public OnKillNotifyTask(L2Attackable attackable, Quest quest, L2PcInstance killer, boolean isSummon)
		{
			_attackable = attackable;
			_quest = quest;
			_killer = killer;
			_isSummon = isSummon;
		}
		
		@Override
		public void run()
		{
			if ((_quest != null) && (_attackable != null) && (_killer != null))
			{
				_quest.notifyKill(_attackable, _killer, _isSummon);
			}
		}
	}
	
	protected static class OnKillByMobNotifyTask implements Runnable
	{
		private final L2Npc _attackable;
		private final Quest _quest;
		private final L2Npc _killer;
		
		public OnKillByMobNotifyTask(L2Npc attackable, Quest quest, L2Npc killer)
		{
			_attackable = attackable;
			_quest = quest;
			_killer = killer;
		}
		
		@Override
		public void run()
		{
			_quest.notifyKillByMob(_attackable, _killer);
		}
	}
	
	@Override
	protected void calculateRewards(L2Character lastAttacker)
	{
		try
		{
			if (getAggroList().isEmpty())
			{
				return;
			}
			
			final Map<L2PcInstance, RewardInfo> rewards = new ConcurrentHashMap<>();
			
			L2PcInstance maxDealer = null;
			int maxDamage = 0;
			long totalDamage = 0;
			
			for (AggroInfo info : getAggroList().values())
			{
				if (info == null)
				{
					continue;
				}
				
				final L2PcInstance attacker = info.getAttacker().getActingPlayer();
				if (attacker != null)
				{
					final int damage = info.getDamage();
					
					if (damage > 1)
					{
						if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, attacker, true))
						{
							continue;
						}
						
						totalDamage += damage;
						
						RewardInfo reward = rewards.get(attacker);
						if (reward == null)
						{
							reward = new RewardInfo(attacker, damage);
							rewards.put(attacker, reward);
						}
						else
						{
							reward.addDamage(damage);
						}
						
						if (reward.getDamage() > maxDamage)
						{
							maxDealer = attacker;
							maxDamage = reward.getDamage();
						}
					}
				}
			}
			
			doItemDrop((maxDealer != null) && maxDealer.isOnline() ? maxDealer : lastAttacker);
			
			doEventDrop(lastAttacker);
			
			if (!getMustRewardExpSP())
			{
				return;
			}
			
			if (!rewards.isEmpty())
			{
				int[] expSp;
				
				for (RewardInfo reward : rewards.values())
				{
					if (reward == null)
					{
						continue;
					}
					
					final L2PcInstance attacker = reward.getAttacker();
					
					final int damage = reward.getDamage();
					
					final L2Party attackerParty = attacker.getParty();
					
					final float penalty = attacker.hasServitor() ? ((L2ServitorInstance) attacker.getSummon()).getExpPenalty() : 0;
					
					if (attackerParty == null)
					{
						if (attacker.getKnownList().knowsObject(this))
						{
							final int levelDiff = attacker.getLevel() - getLevel();
							
							expSp = calculateExpAndSp(levelDiff, damage, totalDamage, attacker.getPremiumService());
							long exp = expSp[0];
							int sp = expSp[1];
							
							if (Config.CHAMPION_ENABLE && isChampion())
							{
								exp *= Config.CHAMPION_REWARDS;
								sp *= Config.CHAMPION_REWARDS;
							}
							
							exp *= 1 - penalty;
							
							L2Character overhitAttacker = getOverhitAttacker();
							if (isOverhit() && (overhitAttacker != null) && (overhitAttacker.getActingPlayer() != null) && (attacker == overhitAttacker.getActingPlayer()))
							{
								attacker.sendPacket(SystemMessageId.OVER_HIT);
								exp += calculateOverhitExp(exp);
							}
							
							if (!attacker.isDead())
							{
								exp *= attacker.getRExp();
								sp *= attacker.getRSp();
								long addexp = Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null));
								int addsp = (int) attacker.calcStat(Stats.EXPSP_RATE, sp, null, null);
								
								attacker.addExpAndSp(addexp, addsp, useVitalityRate());
								if (addexp > 0)
								{
									attacker.updateVitalityPoints(getVitalityPoints(damage), true, false);
								}
							}
						}
					}
					else
					{
						int partyDmg = 0;
						float partyMul = 1;
						int partyLvl = 0;
						
						final List<L2PcInstance> rewardedMembers = new ArrayList<>();
						final List<L2PcInstance> groupMembers = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getMembers() : attackerParty.getMembers();
						for (L2PcInstance partyPlayer : groupMembers)
						{
							if ((partyPlayer == null) || partyPlayer.isDead())
							{
								continue;
							}
							
							final RewardInfo reward2 = rewards.get(partyPlayer);
							
							if (reward2 != null)
							{
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, partyPlayer, true))
								{
									partyDmg += reward2.getDamage();
									rewardedMembers.add(partyPlayer);
									
									if (partyPlayer.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
										{
											partyLvl = attackerParty.getCommandChannel().getLevel();
										}
										else
										{
											partyLvl = partyPlayer.getLevel();
										}
									}
								}
								rewards.remove(partyPlayer);
							}
							else
							{
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, partyPlayer, true))
								{
									rewardedMembers.add(partyPlayer);
									if (partyPlayer.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
										{
											partyLvl = attackerParty.getCommandChannel().getLevel();
										}
										else
										{
											partyLvl = partyPlayer.getLevel();
										}
									}
								}
							}
						}
						
						if (partyDmg < totalDamage)
						{
							partyMul = ((float) partyDmg / totalDamage);
						}
						
						final int levelDiff = partyLvl - getLevel();
						
						expSp = calculateExpAndSp(levelDiff, partyDmg, totalDamage, 1);
						long exp_premium = expSp[0];
						int sp_premium = expSp[1];
						
						expSp = calculateExpAndSp(levelDiff, partyDmg, totalDamage, 0);
						long exp = expSp[0];
						int sp = expSp[1];
						
						if (Config.CHAMPION_ENABLE && isChampion())
						{
							exp *= Config.CHAMPION_REWARDS;
							sp *= Config.CHAMPION_REWARDS;
							exp_premium *= Config.CHAMPION_REWARDS;
							sp_premium *= Config.CHAMPION_REWARDS;
						}
						
						exp *= partyMul;
						sp *= partyMul;
						exp_premium *= partyMul;
						sp_premium *= partyMul;
						
						L2Character overhitAttacker = getOverhitAttacker();
						if (isOverhit() && (overhitAttacker != null) && (overhitAttacker.getActingPlayer() != null) && (attacker == overhitAttacker.getActingPlayer()))
						{
							attacker.sendPacket(SystemMessageId.OVER_HIT);
							exp += calculateOverhitExp(exp);
							exp_premium += calculateOverhitExp(exp_premium);
						}
						
						if (partyDmg > 0)
						{
							attackerParty.distributeXpAndSp(exp_premium, sp_premium, exp, sp, rewardedMembers, partyLvl, partyDmg, this);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
	}
	
	@Override
	public void addAttackerToAttackByList(L2Character player)
	{
		if ((player == null) || (player == this) || getAttackByList().contains(player))
		{
			return;
		}
		getAttackByList().add(player);
	}
	
	public void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!isDead())
		{
			try
			{
				if (isWalker() && !isCoreAIDisabled() && WalkingManager.getInstance().isOnWalk(this))
				{
					if (isEkimusFood())
					{
						WalkingManager.getInstance().resumeMoving(this);
					}
					else
					{
						WalkingManager.getInstance().stopMoving(this, false, true);
					}
				}
				
				L2PcInstance player = attacker.getActingPlayer();
				if (player != null)
				{
					if (getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null)
					{
						for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
						{
							quest.notifyAttack(this, player, damage, attacker.isSummon(), skill);
						}
					}
				}
				else
				{
					getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
					addDamageHate(attacker, damage, (damage * 100) / (getLevel() + 7));
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		L2PcInstance targetPlayer = attacker.getActingPlayer();
		AggroInfo ai = getAggroList().get(attacker);
		
		if (ai == null)
		{
			ai = new AggroInfo(attacker);
			getAggroList().put(attacker, ai);
		}
		ai.addDamage(damage);
		
		if ((targetPlayer == null) || (targetPlayer.getTrap() == null) || !targetPlayer.getTrap().isTriggered())
		{
			ai.addHate(aggro);
		}
		
		if ((targetPlayer != null) && (aggro == 0))
		{
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER) != null)
			{
				for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER))
				{
					quest.notifyAggroRangeEnter(this, targetPlayer, attacker.isSummon());
				}
			}
		}
		else if ((targetPlayer == null) && (aggro == 0))
		{
			aggro = 1;
			ai.addHate(1);
		}
		
		if ((aggro > 0) && (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	public void reduceHate(L2Character target, int amount)
	{
		if ((getAI() instanceof L2SiegeGuardAI) || (getAI() instanceof L2FortSiegeGuardAI))
		{
			stopHating(target);
			setTarget(null);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return;
		}
		
		if (target == null)
		{
			L2Character mostHated = getMostHated();
			
			if (mostHated == null)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				return;
			}
			
			for (AggroInfo ai : getAggroList().values())
			{
				if (ai == null)
				{
					return;
				}
				ai.addHate(-amount);
			}
			
			amount = getHating(mostHated);
			
			if (amount <= 0)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
			return;
		}
		AggroInfo ai = getAggroList().get(target);
		
		if (ai == null)
		{
			return;
		}
		ai.addHate(-amount);
		
		if (ai.getHate() <= 0)
		{
			if (getMostHated() == null)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
		}
	}
	
	public void stopHating(L2Character target)
	{
		if (target == null)
		{
			return;
		}
		AggroInfo ai = getAggroList().get(target);
		if (ai != null)
		{
			ai.stopHate();
		}
	}
	
	public L2Character getMostHated()
	{
		if (getAggroList().isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		L2Character mostHated = null;
		int maxHate = 0;
		
		for (AggroInfo ai : getAggroList().values())
		{
			if (ai == null)
			{
				continue;
			}
			
			if (ai.checkHate(this) > maxHate)
			{
				mostHated = ai.getAttacker();
				maxHate = ai.getHate();
			}
		}
		return mostHated;
	}
	
	public List<L2Character> get2MostHated()
	{
		if (getAggroList().isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		L2Character mostHated = null;
		L2Character secondMostHated = null;
		int maxHate = 0;
		List<L2Character> result = new ArrayList<>();
		
		for (AggroInfo ai : getAggroList().values())
		{
			if (ai == null)
			{
				continue;
			}
			
			if (ai.checkHate(this) > maxHate)
			{
				secondMostHated = mostHated;
				mostHated = ai.getAttacker();
				maxHate = ai.getHate();
			}
		}
		
		result.add(mostHated);
		
		if (getAttackByList().contains(secondMostHated))
		{
			result.add(secondMostHated);
		}
		else
		{
			result.add(null);
		}
		return result;
	}
	
	public List<L2Character> getHateList()
	{
		if (getAggroList().isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		List<L2Character> result = new ArrayList<>();
		for (AggroInfo ai : getAggroList().values())
		{
			if (ai == null)
			{
				continue;
			}
			ai.checkHate(this);
			
			result.add(ai.getAttacker());
		}
		return result;
	}
	
	public int getHating(final L2Character target)
	{
		if (getAggroList().isEmpty() || (target == null))
		{
			return 0;
		}
		
		final AggroInfo ai = getAggroList().get(target);
		
		if (ai == null)
		{
			return 0;
		}
		
		if (ai.getAttacker() instanceof L2PcInstance)
		{
			L2PcInstance act = (L2PcInstance) ai.getAttacker();
			if (act.isInvisible() || ai.getAttacker().isInvul() || act.isSpawnProtected())
			{
				getAggroList().remove(target);
				return 0;
			}
		}
		
		if (!ai.getAttacker().isVisible() || ai.getAttacker().isInvisible())
		{
			getAggroList().remove(target);
			return 0;
		}
		
		if (ai.getAttacker().isAlikeDead())
		{
			ai.stopHate();
			return 0;
		}
		return ai.getHate();
	}
	
	private ItemsHolder calculateRewardItem(L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
	{
		double dropChance = drop.getChance();
		
		int deepBlueDrop = 1;
		
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			if (levelModifier > 0)
			{
				deepBlueDrop = 3;
				if (drop.getItemId() == PcInventory.ADENA_ID)
				{
					deepBlueDrop *= isRaid() && !isRaidMinion() ? (int) Config.RATE_DROP_ITEMS_BY_RAID : (int) Config.RATE_DROP_ITEMS;
				}
			}
		}
		
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}
		
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			dropChance = ((drop.getChance() - ((drop.getChance() * levelModifier) / 100)) / deepBlueDrop);
		}
		
		if (Config.RATE_DROP_ITEMS_ID.containsKey(drop.getItemId()))
		{
			if (lastAttacker.getPremiumService() == 1)
			{
				dropChance *= Config.PREMIUM_RATE_DROP_ITEMS_ID.get(drop.getItemId()) * 3; // wtf? correct drop chance.
			}
			else
			{
				dropChance *= Config.RATE_DROP_ITEMS_ID.get(drop.getItemId()) * 3; // wtf? correct drop chance.
			}
		}
		else if (isSweep)
		{
			if (lastAttacker.getPremiumService() == 1)
			{
				dropChance *= Config.PREMIUM_RATE_DROP_SPOIL;
			}
			else
			{
				dropChance *= Config.RATE_DROP_SPOIL;
			}
		}
		else
		{
			if (lastAttacker.getPremiumService() == 1)
			{
				dropChance *= isRaid() && !isRaidMinion() ? Config.PREMIUM_RATE_DROP_ITEMS_BY_RAID : Config.PREMIUM_RATE_DROP_ITEMS;
			}
			else
			{
				dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
			}
		}
		
		if (Config.CHAMPION_ENABLE && isChampion())
		{
			dropChance *= Config.CHAMPION_REWARDS;
		}
		
		if (dropChance < 1)
		{
			dropChance = 1;
		}
		
		int minCount = drop.getMinDrop();
		int maxCount = drop.getMaxDrop();
		int itemCount = 0;
		
		if ((dropChance > L2DropData.MAX_CHANCE) && !Config.PRECISE_DROP_CALCULATION)
		{
			int multiplier = (int) dropChance / L2DropData.MAX_CHANCE;
			
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
			
			dropChance = dropChance % L2DropData.MAX_CHANCE;
		}
		int random = Rnd.get(L2DropData.MAX_CHANCE);
		while (random < dropChance)
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
			
			dropChance -= L2DropData.MAX_CHANCE;
		}
		
		if (Config.CHAMPION_ENABLE && isChampion() && ((drop.getItemId() == PcInventory.ADENA_ID) || Util.contains(SevenSigns.SEAL_STONE_IDS, drop.getItemId())))
		{
			itemCount *= Config.CHAMPION_ADENAS_REWARDS;
		}
		
		if (itemCount > 0)
		{
			return new ItemsHolder(drop.getItemId(), itemCount);
		}
		
		return null;
	}
	
	private ItemsHolder calculateCategorizedRewardItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops, int levelModifier)
	{
		if (categoryDrops == null)
		{
			return null;
		}
		
		int basecategoryDropChance = categoryDrops.getCategoryChance();
		int categoryDropChance = basecategoryDropChance;
		
		int deepBlueDrop = 1;
		
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			if (levelModifier > 0)
			{
				deepBlueDrop = 3;
			}
		}
		
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}
		
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			categoryDropChance = ((categoryDropChance - ((categoryDropChance * levelModifier) / 100)) / deepBlueDrop);
		}
		
		if (lastAttacker.getPremiumService() == 1)
		{
			categoryDropChance *= isRaid() && !isRaidMinion() ? Config.PREMIUM_RATE_DROP_ITEMS_BY_RAID : Config.PREMIUM_RATE_DROP_ITEMS;
		}
		else
		{
			categoryDropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		}
		
		if (Config.CHAMPION_ENABLE && isChampion())
		{
			categoryDropChance *= Config.CHAMPION_REWARDS;
		}
		
		if (categoryDropChance < 1)
		{
			categoryDropChance = 1;
		}
		
		if (Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne(isRaid() && !isRaidMinion());
			
			if (drop == null)
			{
				return null;
			}
			
			double dropChance = drop.getChance();
			
			if (Config.RATE_DROP_ITEMS_ID.containsKey(drop.getItemId()))
			{
				if (lastAttacker.getPremiumService() == 1)
				{
					dropChance *= Config.PREMIUM_RATE_DROP_ITEMS_ID.get(drop.getItemId()) * 3; // wtf? correct drop chance.
				}
				else
				{
					dropChance *= Config.RATE_DROP_ITEMS_ID.get(drop.getItemId()) * 3; // wtf? correct drop chance.
				}
			}
			else
			{
				if (lastAttacker.getPremiumService() == 1)
				{
					dropChance *= isRaid() && !isRaidMinion() ? Config.PREMIUM_RATE_DROP_ITEMS_BY_RAID : Config.PREMIUM_RATE_DROP_ITEMS;
				}
				else
				{
					dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
				}
			}
			
			if (Config.CHAMPION_ENABLE && isChampion())
			{
				dropChance *= Config.CHAMPION_REWARDS;
			}
			
			dropChance = Math.round(dropChance);
			
			if (dropChance < L2DropData.MAX_CHANCE)
			{
				dropChance = L2DropData.MAX_CHANCE;
			}
			
			int min = drop.getMinDrop();
			int max = drop.getMaxDrop();
			
			int itemCount = 0;
			
			if ((dropChance > L2DropData.MAX_CHANCE) && !Config.PRECISE_DROP_CALCULATION)
			{
				long multiplier = Math.round(dropChance / L2DropData.MAX_CHANCE);
				
				if (min < max)
				{
					itemCount += Rnd.get(min * multiplier, max * multiplier);
				}
				else if (min == max)
				{
					itemCount += min * multiplier;
				}
				else
				{
					itemCount += multiplier;
				}
				
				dropChance = dropChance % L2DropData.MAX_CHANCE;
			}
			int random = Rnd.get(L2DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				if (min < max)
				{
					itemCount += Rnd.get(min, max);
				}
				else if (min == max)
				{
					itemCount += min;
				}
				else
				{
					itemCount++;
				}
				
				dropChance -= L2DropData.MAX_CHANCE;
			}
			
			if (Config.CHAMPION_ENABLE && isChampion() && ((drop.getItemId() == PcInventory.ADENA_ID) || Util.contains(SevenSigns.SEAL_STONE_IDS, drop.getItemId())))
			{
				itemCount *= Config.CHAMPION_ADENAS_REWARDS;
			}
			
			if (!Config.MULTIPLE_ITEM_DROP && !ItemHolder.getInstance().getTemplate(drop.getItemId()).isStackable() && (itemCount > 1))
			{
				itemCount = 1;
			}
			
			if (itemCount > 0)
			{
				return new ItemsHolder(drop.getItemId(), itemCount);
			}
		}
		return null;
	}
	
	private int calculateLevelModifierForDrop(L2PcInstance lastAttacker)
	{
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			int highestLevel = lastAttacker.getLevel();
			
			if (!getAttackByList().isEmpty())
			{
				for (L2Character atkChar : getAttackByList())
				{
					if ((atkChar != null) && (atkChar.getLevel() > highestLevel))
					{
						highestLevel = atkChar.getLevel();
					}
				}
			}
			
			if ((highestLevel - 9) >= getLevel())
			{
				return ((highestLevel - (getLevel() + 8)) * 9);
			}
		}
		return 0;
	}
	
	private ItemsHolder calculateCategorizedHerbItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops)
	{
		if (categoryDrops == null)
		{
			return null;
		}
		
		int basecategoryDropChance = categoryDrops.getCategoryChance();
		int categoryDropChance = basecategoryDropChance;
		
		switch (categoryDrops.getCategoryType())
		{
			case 0:
				if (Config.ENABLE_DROP_VITALITY_HERBS)
				{
					categoryDropChance *= Config.RATE_DROP_VITALITY_HERBS;
				}
				else
				{
					return null;
				}
				break;
			case 1:
				categoryDropChance *= Config.RATE_DROP_HP_HERBS;
				break;
			case 2:
				categoryDropChance *= Config.RATE_DROP_MP_HERBS;
				break;
			case 3:
				categoryDropChance *= Config.RATE_DROP_SPECIAL_HERBS;
				break;
			default:
				categoryDropChance *= Config.RATE_DROP_COMMON_HERBS;
		}
		
		if (categoryDropChance < 1)
		{
			categoryDropChance = 1;
		}
		
		if (Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne(false);
			
			if (drop == null)
			{
				return null;
			}
			
			double dropChance = drop.getChance();
			
			switch (categoryDrops.getCategoryType())
			{
				case 0:
					dropChance *= Config.RATE_DROP_VITALITY_HERBS;
					break;
				case 1:
					dropChance *= Config.RATE_DROP_HP_HERBS;
					break;
				case 2:
					dropChance *= Config.RATE_DROP_MP_HERBS;
					break;
				case 3:
					dropChance *= Config.RATE_DROP_SPECIAL_HERBS;
					break;
				default:
					dropChance *= Config.RATE_DROP_COMMON_HERBS;
			}
			
			if (dropChance < L2DropData.MAX_CHANCE)
			{
				dropChance = L2DropData.MAX_CHANCE;
			}
			
			int min = drop.getMinDrop();
			int max = drop.getMaxDrop();
			
			int itemCount = 0;
			
			if ((dropChance > L2DropData.MAX_CHANCE) && !Config.PRECISE_DROP_CALCULATION)
			{
				long multiplier = Math.round(dropChance / L2DropData.MAX_CHANCE);
				
				if (min < max)
				{
					itemCount += Rnd.get(min * multiplier, max * multiplier);
				}
				else if (min == max)
				{
					itemCount += min * multiplier;
				}
				else
				{
					itemCount += multiplier;
				}
				
				dropChance = dropChance % L2DropData.MAX_CHANCE;
			}
			int random = Rnd.get(L2DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				if (min < max)
				{
					itemCount += Rnd.get(min, max);
				}
				else if (min == max)
				{
					itemCount += min;
				}
				else
				{
					itemCount++;
				}
				
				dropChance -= L2DropData.MAX_CHANCE;
			}
			
			if (itemCount > 0)
			{
				return new ItemsHolder(drop.getItemId(), itemCount);
			}
		}
		return null;
	}
	
	public void doItemDrop(L2Character mainDamageDealer)
	{
		doItemDrop(getTemplate(), mainDamageDealer);
	}
	
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character mainDamageDealer)
	{
		if (mainDamageDealer == null)
		{
			return;
		}
		
		L2PcInstance player = mainDamageDealer.getActingPlayer();
		
		if (player == null)
		{
			return;
		}
		
		int levelModifier = calculateLevelModifierForDrop(player);
		
		CursedWeaponsManager.getInstance().checkDrop(this, player);
		
		for (L2DropCategory cat : npcTemplate.getDropData())
		{
			ItemsHolder item = null;
			if (cat.isSweep())
			{
				if (isSpoil())
				{
					List<ItemsHolder> sweepList = new ArrayList<>();
					
					for (L2DropData drop : cat.getAllDrops())
					{
						item = calculateRewardItem(player, drop, levelModifier, true);
						if (item == null)
						{
							continue;
						}
						sweepList.add(item);
					}
					
					if (!sweepList.isEmpty())
					{
						_sweepItems = sweepList.toArray(new ItemsHolder[sweepList.size()]);
					}
				}
			}
			else
			{
				if (isSeeded())
				{
					L2DropData drop = cat.dropSeedAllowedDropsOnly();
					
					if (drop == null)
					{
						continue;
					}
					
					item = calculateRewardItem(player, drop, levelModifier, false);
				}
				else
				{
					item = calculateCategorizedRewardItem(player, cat, levelModifier);
				}
				
				if (item != null)
				{
					if (isFlying() || ((player.getUseAutoLoot() && (!isRaid() && Config.AUTO_LOOT)) || (isRaid() && Config.AUTO_LOOT_RAIDS)))
					{
						player.doAutoLoot(this, item);
					}
					else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
					{
						if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
						{
							player.doAutoLoot(this, item);
						}
						else
						{
							dropItem(player, item);
						}
					}
					else
					{
						dropItem(player, item);
					}
					
					if (isRaid() && !isRaidMinion())
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
						sm.addCharName(this);
						sm.addItemName(item.getId());
						sm.addItemNumber(item.getCount());
						broadcastPacket(sm);
					}
				}
			}
		}
		
		if (Config.CHAMPION_ENABLE && isChampion() && ((Config.CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE > 0) || (Config.CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE > 0)))
		{
			int champqty = Rnd.get(Config.CHAMPION_REWARD_QTY);
			ItemsHolder item = new ItemsHolder(Config.CHAMPION_REWARD_ID, ++champqty);
			
			if ((player.getLevel() <= getLevel()) && (Rnd.get(100) < Config.CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE))
			{
				if (((player.getUseAutoLoot() && Config.AUTO_LOOT) || isFlying()))
				{
					player.addItem("ChampionLoot", item.getId(), item.getCount(), this, true);
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
					{
						player.doAutoLoot(this, item);
					}
					else
					{
						dropItem(player, item);
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
			else if ((player.getLevel() > getLevel()) && (Rnd.get(100) < Config.CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE))
			{
				if (((player.getUseAutoLoot() && Config.AUTO_LOOT) || isFlying()))
				{
					player.addItem("ChampionLoot", item.getId(), item.getCount(), this, true);
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
					{
						player.doAutoLoot(this, item);
					}
					else
					{
						dropItem(player, item);
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		
		if (EventsDropManager.getInstance().haveActiveCristmasEvent())
		{
			int rewardItem[] = EventsDropManager.getInstance().calculateCristmasRewardItem(npcTemplate, mainDamageDealer);
			if ((rewardItem[0] > 0) && (rewardItem[1] > 0))
			{
				ItemsHolder item = new ItemsHolder(rewardItem[0], rewardItem[1]);
				if (((player.getUseAutoLoot() && Config.AUTO_LOOT) || isFlying()))
				{
					player.addItem("EventLoot", item.getId(), item.getCount(), this, true);
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
					{
						player.doAutoLoot(this, item);
					}
					else
					{
						dropItem(player, item);
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		
		if (EventsDropManager.getInstance().haveActiveMedalsEvent())
		{
			int rewardItem[] = EventsDropManager.getInstance().calculateMedalsRewardItem(npcTemplate, mainDamageDealer);
			if ((rewardItem[0] > 0) && (rewardItem[1] > 0))
			{
				ItemsHolder item = new ItemsHolder(rewardItem[0], rewardItem[1]);
				if (((player.getUseAutoLoot() && Config.AUTO_LOOT) || isFlying()))
				{
					player.addItem("EventLoot", item.getId(), item.getCount(), this, true);
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
					{
						player.doAutoLoot(this, item);
					}
					else
					{
						dropItem(player, item);
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		
		if (EventsDropManager.getInstance().haveActiveL2DayEvent())
		{
			int rewardItem[] = EventsDropManager.getInstance().calculateL2DayRewardItem(npcTemplate, mainDamageDealer);
			if ((rewardItem[0] > 0) && (rewardItem[1] > 0))
			{
				ItemsHolder item = new ItemsHolder(rewardItem[0], rewardItem[1]);
				if (((player.getUseAutoLoot() && Config.AUTO_LOOT) || isFlying()))
				{
					player.addItem("EventLoot", item.getId(), item.getCount(), this, true);
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
					{
						player.doAutoLoot(this, item);
					}
					else
					{
						dropItem(player, item);
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		
		if (EventsDropManager.getInstance().haveActiveMasterOfEnchantingEvent())
		{
			int rewardItem[] = EventsDropManager.getInstance().calculateMasterOfEnchantingRewardItem(npcTemplate, mainDamageDealer);
			if ((rewardItem[0] > 0) && (rewardItem[1] > 0))
			{
				ItemsHolder item = new ItemsHolder(rewardItem[0], rewardItem[1]);
				if (((player.getUseAutoLoot() && Config.AUTO_LOOT) || isFlying()))
				{
					player.addItem("EventLoot", item.getId(), item.getCount(), this, true);
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
					{
						player.doAutoLoot(this, item);
					}
					else
					{
						dropItem(player, item);
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		
		if (EventsDropManager.getInstance().haveActiveSquashEvent())
		{
			int rewardItem[] = EventsDropManager.getInstance().calculateSquashRewardItem(npcTemplate, mainDamageDealer);
			if ((rewardItem[0] > 0) && (rewardItem[1] > 0))
			{
				ItemsHolder item = new ItemsHolder(rewardItem[0], rewardItem[1]);
				if (((player.getUseAutoLoot() && Config.AUTO_LOOT) || isFlying()))
				{
					player.addItem("EventLoot", item.getId(), item.getCount(), this, true);
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
					{
						player.doAutoLoot(this, item);
					}
					else
					{
						dropItem(player, item);
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		
		if (EventsDropManager.getInstance().haveActiveValentineEvent())
		{
			int rewardItem[] = EventsDropManager.getInstance().calculateValentineRewardItem(npcTemplate, mainDamageDealer);
			if ((rewardItem[0] > 0) && (rewardItem[1] > 0))
			{
				ItemsHolder item = new ItemsHolder(rewardItem[0], rewardItem[1]);
				if (((player.getUseAutoLoot() && Config.AUTO_LOOT) || isFlying()))
				{
					player.addItem("EventLoot", item.getId(), item.getCount(), this, true);
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, item.getId()) >= 0)
					{
						player.doAutoLoot(this, item);
					}
					else
					{
						dropItem(player, item);
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		
		if (getTemplate().getDropHerbGroup() > 0)
		{
			for (L2DropCategory cat : HerbDropParser.getInstance().getHerbDroplist(getTemplate().getDropHerbGroup()))
			{
				ItemsHolder item = calculateCategorizedHerbItem(player, cat);
				if (item != null)
				{
					long count = item.getCount();
					if (count > 1)
					{
						final ItemsHolder herb = new ItemsHolder(item.getId(), 1);
						for (int i = 0; i < count; i++)
						{
							dropItem(player, herb);
						}
					}
					else if (isFlying() || (player.getUseAutoLootHerbs() && Config.AUTO_LOOT_HERBS))
					{
						player.addItem("Loot", item.getId(), count, this, true);
					}
					else
					{
						dropItem(player, item);
					}
				}
			}
		}
	}
	
	public void doEventDrop(L2Character lastAttacker)
	{
		if (lastAttacker == null)
		{
			return;
		}
		
		L2PcInstance player = lastAttacker.getActingPlayer();
		
		if (player == null)
		{
			return;
		}
		
		if ((player.getLevel() - getLevel()) > 9)
		{
			return;
		}
		
		for (DateDrop drop : EventDroplist.getInstance().getAllDrops())
		{
			if (Rnd.get(L2DropData.MAX_CHANCE) < drop.getEventDrop().getDropChance())
			{
				final ItemsHolder rewardItem = new ItemsHolder(drop.getEventDrop().getItemIdList()[Rnd.get(drop.getEventDrop().getItemIdList().length)], Rnd.get(drop.getEventDrop().getMinCount(), drop.getEventDrop().getMaxCount()));
				
				if (((player.getUseAutoLoot() && Config.AUTO_LOOT) || isFlying()))
				{
					player.doAutoLoot(this, rewardItem);
				}
				else if (Config.AUTO_LOOT_BY_ID_SYSTEM)
				{
					if (Arrays.binarySearch(Config.AUTO_LOOT_BY_ID, rewardItem.getId()) >= 0)
					{
						player.doAutoLoot(this, rewardItem);
					}
					else
					{
						dropItem(player, rewardItem);
					}
				}
				else
				{
					dropItem(player, rewardItem);
				}
			}
		}
	}
	
	public L2ItemInstance dropItem(L2PcInstance mainDamageDealer, ItemsHolder item)
	{
		int randDropLim = 70;
		
		L2ItemInstance ditem = null;
		for (int i = 0; i < item.getCount(); i++)
		{
			int newX = (getX() + Rnd.get((randDropLim * 2) + 1)) - randDropLim;
			int newY = (getY() + Rnd.get((randDropLim * 2) + 1)) - randDropLim;
			int newZ = Math.max(getZ(), mainDamageDealer.getZ()) + 20;
			
			if (ItemHolder.getInstance().getTemplate(item.getId()) != null)
			{
				ditem = ItemHolder.getInstance().createItem("Loot", item.getId(), item.getCount(), mainDamageDealer, this);
				ditem.getDropProtection().protect(mainDamageDealer);
				ditem.dropMe(this, newX, newY, newZ);
				
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getId()))
				{
					if (((Config.AUTODESTROY_ITEM_AFTER > 0) && (ditem.getItemType() != L2EtcItemType.HERB)) || ((Config.HERB_AUTO_DESTROY_TIME > 0) && (ditem.getItemType() == L2EtcItemType.HERB)))
					{
						ItemsAutoDestroy.getInstance().addItem(ditem);
					}
				}
				ditem.setProtected(false);
				
				if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
				{
					break;
				}
			}
			else
			{
				_log.log(Level.SEVERE, "Item doesn't exist so cannot be dropped. Item ID: " + item.getId());
			}
		}
		return ditem;
	}
	
	public L2ItemInstance dropItem(L2PcInstance lastAttacker, int itemId, int itemCount)
	{
		return dropItem(lastAttacker, new ItemsHolder(itemId, itemCount));
	}
	
	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}
	
	public boolean noTarget()
	{
		return getAggroList().isEmpty();
	}
	
	public boolean containsTarget(L2Character player)
	{
		return getAggroList().containsKey(player);
	}
	
	public void clearAggroList()
	{
		getAggroList().clear();
		
		_overhit = false;
		_overhitDamage = 0;
		_overhitAttacker = null;
	}
	
	@Override
	public boolean isSweepActive()
	{
		return _sweepItems != null;
	}
	
	public List<L2Item> getSpoilLootItems()
	{
		final List<L2Item> lootItems = new ArrayList<>();
		if (isSweepActive())
		{
			for (ItemsHolder item : _sweepItems)
			{
				lootItems.add(ItemHolder.getInstance().createDummyItem(item.getId()).getItem());
			}
		}
		return lootItems;
	}
	
	public synchronized ItemsHolder[] takeSweep()
	{
		ItemsHolder[] sweep = _sweepItems;
		_sweepItems = null;
		return sweep;
	}
	
	public synchronized ItemsHolder[] takeHarvest()
	{
		ItemsHolder[] harvest = _harvestItems;
		_harvestItems = null;
		return harvest;
	}
	
	public boolean isOldCorpse(L2PcInstance attacker, int time, boolean sendMessage)
	{
		if (DecayTaskManager.getInstance().getTasks().containsKey(this) && ((System.currentTimeMillis() - DecayTaskManager.getInstance().getTasks().get(this)) > time))
		{
			if (sendMessage && (attacker != null))
			{
				attacker.sendPacket(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED);
			}
			return true;
		}
		return false;
	}
	
	public boolean checkSpoilOwner(L2PcInstance sweeper, boolean sendMessage)
	{
		if ((sweeper.getObjectId() != getIsSpoiledBy()) && !sweeper.isInLooterParty(getIsSpoiledBy()))
		{
			if (sendMessage)
			{
				sweeper.sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED);
			}
			return false;
		}
		return true;
	}
	
	public void overhitEnabled(boolean status)
	{
		_overhit = status;
	}
	
	public void setOverhitValues(L2Character attacker, double damage)
	{
		double overhitDmg = -(getCurrentHp() - damage);
		if (overhitDmg < 0)
		{
			overhitEnabled(false);
			_overhitDamage = 0;
			_overhitAttacker = null;
			return;
		}
		overhitEnabled(true);
		_overhitDamage = overhitDmg;
		_overhitAttacker = attacker;
	}
	
	public L2Character getOverhitAttacker()
	{
		return _overhitAttacker;
	}
	
	public double getOverhitDamage()
	{
		return _overhitDamage;
	}
	
	public boolean isOverhit()
	{
		return _overhit;
	}
	
	public void absorbSoul()
	{
		_absorbed = true;
	}
	
	public boolean isAbsorbed()
	{
		return _absorbed;
	}
	
	public void addAbsorber(L2PcInstance attacker)
	{
		AbsorberInfo ai = _absorbersList.get(attacker.getObjectId());
		
		if (ai == null)
		{
			ai = new AbsorberInfo(attacker.getObjectId(), getCurrentHp());
			_absorbersList.put(attacker.getObjectId(), ai);
		}
		else
		{
			ai._objId = attacker.getObjectId();
			ai._absorbedHP = getCurrentHp();
		}
		absorbSoul();
	}
	
	public void resetAbsorbList()
	{
		_absorbed = false;
		_absorbersList.clear();
	}
	
	public L2TIntObjectHashMap<AbsorberInfo> getAbsorbersList()
	{
		return _absorbersList;
	}
	
	private int[] calculateExpAndSp(int diff, int damage, long totalDamage, int IsPremium)
	{
		double xp;
		double sp;
		
		if (diff < -5)
		{
			diff = -5;
		}
		
		xp = ((double) getExpReward(IsPremium) * damage) / totalDamage;
		if (Config.ALT_GAME_EXPONENT_XP != 0)
		{
			xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
		}
		
		sp = ((double) getSpReward(IsPremium) * damage) / totalDamage;
		if (Config.ALT_GAME_EXPONENT_SP != 0)
		{
			sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		}
		
		if ((Config.ALT_GAME_EXPONENT_XP == 0) && (Config.ALT_GAME_EXPONENT_SP == 0))
		{
			if (diff > 5)
			{
				double pow = Math.pow((double) 5 / 6, diff - 5);
				xp = xp * pow;
				sp = sp * pow;
			}
			
			if (xp <= 0)
			{
				xp = 0;
				sp = 0;
			}
			else if (sp <= 0)
			{
				sp = 0;
			}
		}
		int[] tmp =
		{
			(int) xp,
			(int) sp
		};
		return tmp;
	}
	
	public long calculateOverhitExp(long normalExp)
	{
		double overhitPercentage = ((getOverhitDamage() * 100) / getMaxHp());
		
		if (overhitPercentage > 25)
		{
			overhitPercentage = 25;
		}
		
		double overhitExp = ((overhitPercentage / 100) * normalExp);
		
		long bonusOverhit = Math.round(overhitExp);
		return bonusOverhit;
	}
	
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		setSpoil(false);
		clearAggroList();
		_harvestItems = null;
		_seeded = false;
		_seedType = 0;
		_seederObjId = 0;
		overhitEnabled(false);
		
		_sweepItems = null;
		resetAbsorbList();
		
		setWalking();
		
		if (!isInActiveRegion())
		{
			if (hasAI())
			{
				getAI().stopAITask();
			}
		}
	}
	
	public boolean isSpoil()
	{
		return _isSpoil;
	}
	
	public void setSpoil(boolean isSpoil)
	{
		_isSpoil = isSpoil;
	}
	
	public final int getIsSpoiledBy()
	{
		return _isSpoiledBy;
	}
	
	public final void setIsSpoiledBy(int value)
	{
		_isSpoiledBy = value;
	}
	
	public void setSeeded(L2PcInstance seeder)
	{
		if ((_seedType != 0) && (_seederObjId == seeder.getObjectId()))
		{
			setSeeded(_seedType, seeder.getLevel());
		}
	}
	
	public void setSeeded(int id, L2PcInstance seeder)
	{
		if (!_seeded)
		{
			_seedType = id;
			_seederObjId = seeder.getObjectId();
		}
	}
	
	private void setSeeded(int id, int seederLvl)
	{
		_seeded = true;
		_seedType = id;
		int count = 1;
		
		Set<Integer> skillIds = getTemplate().getSkills().keySet();
		
		if (skillIds != null)
		{
			for (int skillId : skillIds)
			{
				switch (skillId)
				{
					case 4303:
						count *= 2;
						break;
					case 4304:
						count *= 3;
						break;
					case 4305:
						count *= 4;
						break;
					case 4306:
						count *= 5;
						break;
					case 4307:
						count *= 6;
						break;
					case 4308:
						count *= 7;
						break;
					case 4309:
						count *= 8;
						break;
					case 4310:
						count *= 9;
						break;
				}
			}
		}
		
		int diff = (getLevel() - (ManorParser.getInstance().getSeedLevel(_seedType) - 5));
		
		if (diff > 0)
		{
			count += diff;
		}
		_harvestItems = new ItemsHolder[]
		{
			new ItemsHolder(ManorParser.getInstance().getCropType(_seedType), count * Config.RATE_DROP_MANOR)
		};
	}
	
	public int getSeederId()
	{
		return _seederObjId;
	}
	
	public int getSeedType()
	{
		return _seedType;
	}
	
	public boolean isSeeded()
	{
		return _seeded;
	}
	
	public final void setOnKillDelay(int delay)
	{
		_onKillDelay = delay;
	}
	
	public final int getOnKillDelay()
	{
		return _onKillDelay;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return ((Config.MAX_MONSTER_ANIMATION > 0) && isRandomAnimationEnabled() && !(this instanceof L2GrandBossInstance));
	}
	
	@Override
	public boolean isMob()
	{
		return true;
	}
	
	protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer)
	{
		_commandChannelTimer = commandChannelTimer;
	}
	
	public CommandChannelTimer getCommandChannelTimer()
	{
		return _commandChannelTimer;
	}
	
	public L2CommandChannel getFirstCommandChannelAttacked()
	{
		return _firstCommandChannelAttacked;
	}
	
	public void setFirstCommandChannelAttacked(L2CommandChannel firstCommandChannelAttacked)
	{
		_firstCommandChannelAttacked = firstCommandChannelAttacked;
	}
	
	public long getCommandChannelLastAttack()
	{
		return _commandChannelLastAttack;
	}
	
	public void setCommandChannelLastAttack(long channelLastAttack)
	{
		_commandChannelLastAttack = channelLastAttack;
	}
	
	private static class CommandChannelTimer implements Runnable
	{
		private final L2Attackable _monster;
		
		public CommandChannelTimer(L2Attackable monster)
		{
			_monster = monster;
		}
		
		@Override
		public void run()
		{
			if ((System.currentTimeMillis() - _monster.getCommandChannelLastAttack()) > Config.LOOT_RAIDS_PRIVILEGE_INTERVAL)
			{
				_monster.setCommandChannelTimer(null);
				_monster.setFirstCommandChannelAttacked(null);
				_monster.setCommandChannelLastAttack(0);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(this, 10000);
			}
		}
	}
	
	public void returnHome()
	{
		clearAggroList();
		
		if (hasAI() && (getSpawn() != null))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, getSpawn().getLocation());
		}
	}
	
	public float getVitalityPoints(int damage)
	{
		if (damage <= 0)
		{
			return 0;
		}
		
		final float divider = getTemplate().getBaseVitalityDivider();
		if (divider == 0)
		{
			return 0;
		}
		
		return -Math.min(damage, getMaxHp()) / divider;
	}
	
	public boolean useVitalityRate()
	{
		if (isChampion() && !Config.CHAMPION_ENABLE_VITALITY)
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isRaid()
	{
		return _isRaid;
	}
	
	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}
	
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isRaidMinion = val;
	}
	
	@Override
	public boolean isRaidMinion()
	{
		return _isRaidMinion;
	}
	
	@Override
	public boolean isMinion()
	{
		return getLeader() != null;
	}
	
	public L2Attackable getLeader()
	{
		return null;
	}
	
	@Override
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}
	
	@Override
	public boolean isChampion()
	{
		return _champion;
	}
	
	@Override
	public boolean isL2Attackable()
	{
		return true;
	}
	
	public boolean canShowLevelInTitle()
	{
		return !(getName().equals("Chest"));
	}
	
	private static BufferedImage generateCaptcha()
	{
		Color textColor = new Color(95, 230, 30);
		Color circleColor = new Color(95, 230, 30);
		Font textFont = new Font("comic sans ms", Font.BOLD, 24);
		int charsToPrint = 5;
		int width = 256;
		int height = 64;
		int circlesToDraw = 8;
		float horizMargin = 20.0f;
		double rotationRange = 0.7;
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
		
		g.setColor(new Color(30, 31, 31));
		g.fillRect(0, 0, width, height);
		
		g.setColor(circleColor);
		for (int i = 0; i < circlesToDraw; i++)
		{
			int circleRadius = (int) ((Math.random() * height) / 2.0);
			int circleX = (int) ((Math.random() * width) - circleRadius);
			int circleY = (int) ((Math.random() * height) - circleRadius);
			g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
		}
		
		g.setColor(textColor);
		g.setFont(textFont);
		
		FontMetrics fontMetrics = g.getFontMetrics();
		int maxAdvance = fontMetrics.getMaxAdvance();
		int fontHeight = fontMetrics.getHeight();
		
		String elegibleChars = "abcdefghjklmpqrstuvwxyz0123456789";
		char[] chars = elegibleChars.toCharArray();
		
		float spaceForLetters = (-horizMargin * 2) + width;
		float spacePerChar = spaceForLetters / (charsToPrint - 1.0f);
		
		for (int i = 0; i < charsToPrint; i++)
		{
			double randomValue = Math.random();
			int randomIndex = (int) Math.round(randomValue * (chars.length - 1));
			char characterToShow = chars[randomIndex];
			finalString.append(characterToShow);
			
			int charWidth = fontMetrics.charWidth(characterToShow);
			int charDim = Math.max(maxAdvance, fontHeight);
			int halfCharDim = (charDim / 2);
			
			BufferedImage charImage = new BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB);
			Graphics2D charGraphics = charImage.createGraphics();
			charGraphics.translate(halfCharDim, halfCharDim);
			double angle = (Math.random() - 0.5) * rotationRange;
			charGraphics.transform(AffineTransform.getRotateInstance(angle));
			charGraphics.translate(-halfCharDim, -halfCharDim);
			charGraphics.setColor(textColor);
			charGraphics.setFont(textFont);
			
			int charX = (int) ((0.5 * charDim) - (0.5 * charWidth));
			charGraphics.drawString("" + characterToShow, charX, (((charDim - fontMetrics.getAscent()) / 2) + fontMetrics.getAscent()));
			
			float x = (horizMargin + (spacePerChar * (i))) - (charDim / 2.0f);
			int y = ((height - charDim) / 2);
			g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);
			
			charGraphics.dispose();
		}
		
		g.dispose();
		
		return bufferedImage;
	}
	
	protected class CaptchaTimer implements Runnable
	{
		L2PcInstance activeChar;
		
		public CaptchaTimer(L2PcInstance player)
		{
			activeChar = player;
		}
		
		@Override
		public void run()
		{
			if (!activeChar.isCodeRight())
			{
				activeChar.stopAbnormalEffect(AbnormalEffect.REAL_TARGET);
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
				npcHtmlMessage.setHtml("<html><title>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.SYSTEM") + "</title><body><center><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.PASSED") + "</font><br><br><font color=\"66FF00\"><center></font><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.WILL_PUNISH") + "</font><br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.EXIT") + "\" action=\"bypass -h npc_%objectId%_Quest\" width=45 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
				if (activeChar.isFlyingMounted())
				{
					activeChar.untransform();
				}
				Util.handleIllegalPlayerAction(activeChar, "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "CaptchaAntibot.PUNISH_MSG") + "", Config.ANTIBOT_PUNISH);
				activeChar.setIsInvul(false);
				activeChar.setIsParalyzed(false);
				activeChar.sendPacket(npcHtmlMessage);
			}
		}
	}
}