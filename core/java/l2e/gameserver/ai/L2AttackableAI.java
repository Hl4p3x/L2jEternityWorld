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
package l2e.gameserver.ai;

import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import l2e.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.TerritoryHolder;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import l2e.gameserver.model.actor.instance.L2FriendlyMobInstance;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2GuardInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2RaidBossInstance;
import l2e.gameserver.model.actor.instance.L2RiftInvaderInstance;
import l2e.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.actor.templates.L2NpcTemplate.AIType;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.Quest.QuestEventType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

public class L2AttackableAI extends L2CharacterAI implements Runnable
{
	private static final int RANDOM_WALK_RATE = 30;
	private static final int MAX_ATTACK_TIMEOUT = 1200;
	private Future<?> _aiTask;
	private int _attackTimeout;
	private int _globalAggro;
	private boolean _thinking;
	
	private int timepass = 0;
	private int chaostime = 0;
	private final L2NpcTemplate _skillrender;
	private List<L2Skill> shortRangeSkills = new ArrayList<>();
	private List<L2Skill> longRangeSkills = new ArrayList<>();
	int lastBuffTick;
	
	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		_skillrender = NpcTable.getInstance().getTemplate(getActiveChar().getTemplate().getId());
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10;
	}
	
	@Override
	public void run()
	{
		onEvtThink();
	}
	
	private boolean autoAttackCondition(L2Character target)
	{
		if ((target == null) || (getActiveChar() == null))
		{
			return false;
		}
		final L2Attackable me = getActiveChar();
		
		if (target.isInvul())
		{
			if ((target instanceof L2PcInstance) && ((L2PcInstance) target).isGM())
			{
				return false;
			}
			if ((target instanceof L2Summon) && ((L2Summon) target).getOwner().isGM())
			{
				return false;
			}
		}
		
		if (target instanceof L2DoorInstance)
		{
			return false;
		}
		
		if (target.isAlikeDead() || ((target instanceof L2Playable) && !me.isInsideRadius(target, me.getAggroRange(), true, false)))
		{
			return false;
		}
		
		if (target.isPlayable())
		{
			if ((!(me instanceof L2GrandBossInstance) || !(me.isRaid())) && !(me.canSeeThroughSilentMove()) && ((L2Playable) target).isSilentMoving())
			{
				return false;
			}
		}
		
		final L2PcInstance player = target.getActingPlayer();
		if (player != null)
		{
			if (player.isGM() && !player.getAccessLevel().canTakeAggro())
			{
				return false;
			}
			
			if (player.isRecentFakeDeath())
			{
				return false;
			}
			
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				byte riftType = player.getParty().getDimensionalRift().getType();
				byte riftRoom = player.getParty().getDimensionalRift().getCurrentRoom();
				
				if ((me instanceof L2RiftInvaderInstance) && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ()))
				{
					return false;
				}
			}
		}
		
		if (me instanceof L2GuardInstance)
		{
			if ((player != null) && (player.getKarma() > 0))
			{
				return GeoClient.getInstance().canSeeTarget(me, player);
			}
			
			if ((target instanceof L2MonsterInstance) && Config.GUARD_ATTACK_AGGRO_MOB)
			{
				return (((L2MonsterInstance) target).isAggressive() && GeoClient.getInstance().canSeeTarget(me, target));
			}
			
			return false;
		}
		else if (me instanceof L2FriendlyMobInstance)
		{
			if (target instanceof L2Npc)
			{
				return false;
			}
			
			if ((target instanceof L2PcInstance) && (((L2PcInstance) target).getKarma() > 0))
			{
				return GeoClient.getInstance().canSeeTarget(me, target);
			}
			return false;
		}
		else
		{
			if (target instanceof L2Attackable)
			{
				if ((me.getEnemyClan() == null) || (((L2Attackable) target).getClan() == null))
				{
					return false;
				}
				
				if (!target.isAutoAttackable(me))
				{
					return false;
				}
				
				if (me.getEnemyClan().equals(((L2Attackable) target).getClan()))
				{
					if (me.isInsideRadius(target, me.getEnemyRange(), false, false))
					{
						return GeoClient.getInstance().canSeeTarget(me, target);
					}
					return false;
				}
				if ((me.getIsChaos() > 0) && me.isInsideRadius(target, me.getIsChaos(), false, false))
				{
					if ((me.getFactionId() != null) && me.getFactionId().equals(((L2Attackable) target).getFactionId()))
					{
						return false;
					}
					return GeoClient.getInstance().canSeeTarget(me, target);
				}
			}
			
			if ((target instanceof L2Attackable) || (target instanceof L2Npc))
			{
				return false;
			}
			
			if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(ZoneId.PEACE))
			{
				return false;
			}
			
			if (me.isChampion() && Config.CHAMPION_PASSIVE)
			{
				return false;
			}
			
			return (me.isAggressive() && GeoClient.getInstance().canSeeTarget(me, target));
		}
	}
	
	public void startAITask()
	{
		if (_aiTask == null)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
	}
	
	@Override
	public void stopAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		super.stopAITask();
	}
	
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if ((intention == AI_INTENTION_IDLE) || (intention == AI_INTENTION_ACTIVE))
		{
			L2Attackable npc = getActiveChar();
			if (!npc.isAlikeDead())
			{
				if (!npc.getKnownList().getKnownPlayers().isEmpty())
				{
					intention = AI_INTENTION_ACTIVE;
				}
				else
				{
					if (npc.getSpawn() != null)
					{
						final int range = Config.MAX_DRIFT_RANGE;
						if (!npc.isInsideRadius(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), range + range, true, false))
						{
							intention = AI_INTENTION_ACTIVE;
						}
					}
				}
			}
			
			if (intention == AI_INTENTION_IDLE)
			{
				super.changeIntention(AI_INTENTION_IDLE, null, null);
				
				if (_aiTask != null)
				{
					_aiTask.cancel(true);
					_aiTask = null;
				}
				_accessor.detachAI();
				
				return;
			}
		}
		super.changeIntention(intention, arg0, arg1);
		
		startAITask();
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
		
		if ((lastBuffTick + 30) < GameTimeController.getInstance().getGameTicks())
		{
			for (L2Skill sk : _skillrender.getBuffSkills())
			{
				if (cast(sk))
				{
					break;
				}
			}
			lastBuffTick = GameTimeController.getInstance().getGameTicks();
		}
		super.onIntentionAttack(target);
	}
	
	private void thinkCast()
	{
		if (checkTargetLost(getCastTarget()))
		{
			setCastTarget(null);
			return;
		}
		if (maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill)))
		{
			return;
		}
		clientStopMoving(null);
		setIntention(AI_INTENTION_ACTIVE);
		_accessor.doCast(_skill);
	}
	
	private void thinkActive()
	{
		L2Attackable npc = getActiveChar();
		
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}
		
		if (_globalAggro >= 0)
		{
			Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			
			for (L2Object obj : objs)
			{
				if (!(obj instanceof L2Character) || (obj instanceof L2StaticObjectInstance))
				{
					continue;
				}
				L2Character target = (L2Character) obj;
				
				if ((npc instanceof L2FestivalMonsterInstance) && (obj instanceof L2PcInstance))
				{
					L2PcInstance targetPlayer = (L2PcInstance) obj;
					
					if (!(targetPlayer.isFestivalParticipant()))
					{
						continue;
					}
				}
				
				if (autoAttackCondition(target))
				{
					int hating = npc.getHating(target);
					
					if (hating == 0)
					{
						npc.addDamageHate(target, 0, 0);
					}
				}
			}
			
			L2Character hated;
			if (npc.isConfused())
			{
				hated = getAttackTarget();
			}
			else
			{
				hated = npc.getMostHated();
			}
			
			if ((hated != null) && !npc.isCoreAIDisabled())
			{
				int aggro = npc.getHating(hated);
				
				if ((aggro + _globalAggro) > 0)
				{
					if (!npc.isRunning())
					{
						if (npc.isEkimusFood())
						{
							npc.setWalking();
						}
						else
						{
							npc.setRunning();
						}
					}
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
				}
				return;
			}
		}
		
		if ((npc.getCurrentHp() == npc.getMaxHp()) && (npc.getCurrentMp() == npc.getMaxMp()) && !npc.getAttackByList().isEmpty() && (Rnd.nextInt(500) == 0))
		{
			npc.clearAggroList();
			npc.getAttackByList().clear();
			if (npc instanceof L2MonsterInstance)
			{
				if (((L2MonsterInstance) npc).hasMinions())
				{
					((L2MonsterInstance) npc).getMinionList().deleteReusedMinions();
				}
			}
		}
		
		if ((npc instanceof L2GuardInstance) && !npc.isWalker() && !npc.isRunner() && !npc.isSpecialCamera() && !npc.isEkimusFood())
		{
			((L2GuardInstance) npc).returnHome();
		}
		
		if (npc instanceof L2FestivalMonsterInstance)
		{
			return;
		}
		
		if (!npc.canReturnToSpawnPoint())
		{
			return;
		}
		
		final L2Character leader = npc.getLeader();
		if ((leader != null) && !leader.isAlikeDead())
		{
			final int offset;
			final int minRadius = 30;
			
			if (npc.isRaidMinion())
			{
				offset = 500;
			}
			else
			{
				offset = 200;
			}
			
			if (leader.isRunning())
			{
				npc.setRunning();
			}
			else
			{
				npc.setWalking();
			}
			
			if (npc.getPlanDistanceSq(leader) > (offset * offset))
			{
				int x1, y1, z1;
				x1 = Rnd.get(minRadius * 2, offset * 2);
				y1 = Rnd.get(x1, offset * 2);
				y1 = (int) Math.sqrt((y1 * y1) - (x1 * x1));
				if (x1 > (offset + minRadius))
				{
					x1 = (leader.getX() + x1) - offset;
				}
				else
				{
					x1 = (leader.getX() - x1) + minRadius;
				}
				if (y1 > (offset + minRadius))
				{
					y1 = (leader.getY() + y1) - offset;
				}
				else
				{
					y1 = (leader.getY() - y1) + minRadius;
				}
				
				z1 = leader.getZ();
				moveTo(x1, y1, z1);
				return;
			}
			else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0)
			{
				for (L2Skill sk : _skillrender.getBuffSkills())
				{
					if (cast(sk))
					{
						return;
					}
				}
			}
		}
		else if ((npc.getSpawn() != null) && (Rnd.nextInt(RANDOM_WALK_RATE) == 0) && !npc.isNoRndWalk())
		{
			int x1, y1, z1;
			final int range = Config.MAX_DRIFT_RANGE;
			
			for (L2Skill sk : _skillrender.getBuffSkills())
			{
				if (cast(sk))
				{
					return;
				}
			}
			
			if ((npc.getSpawn().getX() == 0) && (npc.getSpawn().getY() == 0))
			{
				int p[] = TerritoryHolder.getInstance().getRandomPoint(npc.getSpawn().getLocationId());
				x1 = p[0];
				y1 = p[1];
				z1 = p[2];
				
				double distance2 = npc.getPlanDistanceSq(x1, y1);
				
				if (distance2 > ((range + range) * (range + range)))
				{
					npc.setisReturningToSpawnPoint(true);
					float delay = (float) Math.sqrt(distance2) / range;
					x1 = npc.getX() + (int) ((x1 - npc.getX()) / delay);
					y1 = npc.getY() + (int) ((y1 - npc.getY()) / delay);
				}
				
				if ((TerritoryHolder.getInstance().getProcMax(npc.getSpawn().getLocationId()) > 0) && !npc.isReturningToSpawnPoint())
				{
					return;
				}
			}
			else
			{
				x1 = npc.getSpawn().getX();
				y1 = npc.getSpawn().getY();
				z1 = npc.getSpawn().getZ();
				
				if (!npc.isInsideRadius(x1, y1, range, false))
				{
					npc.setisReturningToSpawnPoint(true);
				}
				else
				{
					x1 = Rnd.nextInt(range * 2);
					y1 = Rnd.get(x1, range * 2);
					y1 = (int) Math.sqrt((y1 * y1) - (x1 * x1));
					x1 += npc.getSpawn().getX() - range;
					y1 += npc.getSpawn().getY() - range;
					z1 = npc.getZ();
					z1 = GeoClient.getInstance().getHeight(x1, y1, z1);
				}
			}
			moveTo(x1, y1, z1);
		}
	}
	
	private void thinkAttack()
	{
		final L2Attackable npc = getActiveChar();
		if (npc.isCastingNow())
		{
			return;
		}
		
		L2Character originalAttackTarget = getAttackTarget();
		if ((originalAttackTarget == null) || originalAttackTarget.isAlikeDead() || (_attackTimeout < GameTimeController.getInstance().getGameTicks()))
		{
			if (originalAttackTarget != null)
			{
				npc.stopHating(originalAttackTarget);
			}
			
			setIntention(AI_INTENTION_ACTIVE);
			
			npc.setWalking();
			return;
		}
		
		final int collision = npc.getTemplate().getCollisionRadius();
		
		String faction_id = getActiveChar().getFactionId();
		if ((faction_id != null) && !faction_id.isEmpty())
		{
			int factionRange = npc.getClanRange() + collision;
			Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			try
			{
				for (L2Object obj : objs)
				{
					if (obj instanceof L2Npc)
					{
						L2Npc called = (L2Npc) obj;
						
						final String npcfaction = called.getFactionId();
						if ((npcfaction == null) || npcfaction.isEmpty())
						{
							continue;
						}
						
						boolean sevenSignFaction = false;
						
						if ("c_dungeon_clan".equals(faction_id) && ("c_dungeon_lilim".equals(npcfaction) || "c_dungeon_nephi".equals(npcfaction)))
						{
							sevenSignFaction = true;
						}
						else if ("c_dungeon_lilim".equals(faction_id) && "c_dungeon_clan".equals(npcfaction))
						{
							sevenSignFaction = true;
						}
						else if ("c_dungeon_nephi".equals(faction_id) && "c_dungeon_clan".equals(npcfaction))
						{
							sevenSignFaction = true;
						}
						
						if (!faction_id.equals(npcfaction) && !sevenSignFaction)
						{
							continue;
						}
						
						if (npc.isInsideRadius(called, factionRange, true, false) && called.hasAI())
						{
							if ((Math.abs(originalAttackTarget.getZ() - called.getZ()) < 600) && npc.getAttackByList().contains(originalAttackTarget) && ((called.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE) || (called.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)) && (called.getInstanceId() == npc.getInstanceId()))
							{
								if (originalAttackTarget.isPlayable())
								{
									List<Quest> quests = called.getTemplate().getEventQuests(QuestEventType.ON_FACTION_CALL);
									if ((quests != null) && !quests.isEmpty())
									{
										L2PcInstance player = originalAttackTarget.getActingPlayer();
										boolean isSummon = originalAttackTarget.isSummon();
										for (Quest quest : quests)
										{
											quest.notifyFactionCall(called, getActiveChar(), player, isSummon);
										}
									}
								}
								else if ((called instanceof L2Attackable) && (getAttackTarget() != null) && (called.getAI()._intention != CtrlIntention.AI_INTENTION_ATTACK))
								{
									((L2Attackable) called).addDamageHate(getAttackTarget(), 0, npc.getHating(getAttackTarget()));
									called.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAttackTarget());
								}
							}
						}
					}
				}
			}
			catch (NullPointerException e)
			{
				_log.warning(getClass().getSimpleName() + ": thinkAttack() faction call failed: " + e.getMessage());
			}
		}
		
		if (npc.isCoreAIDisabled())
		{
			return;
		}
		
		L2Character mostHate = npc.getMostHated();
		if (mostHate == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		
		setAttackTarget(mostHate);
		npc.setTarget(mostHate);
		
		final int combinedCollision = collision + mostHate.getTemplate().getCollisionRadius();
		
		if (!_skillrender.getSuicideSkills().isEmpty() && ((int) ((npc.getCurrentHp() / npc.getMaxHp()) * 100) < 30))
		{
			final L2Skill skill = _skillrender.getSuicideSkills().get(Rnd.nextInt(_skillrender.getSuicideSkills().size()));
			if (Util.checkIfInRange(skill.getAffectRange(), getActiveChar(), mostHate, false) && (Rnd.get(100) < Rnd.get(npc.getMinSkillChance(), npc.getMaxSkillChance())))
			{
				if (cast(skill))
				{
					return;
				}
				
				for (L2Skill sk : _skillrender.getSuicideSkills())
				{
					if (cast(sk))
					{
						return;
					}
				}
			}
		}
		
		if (!npc.isMovementDisabled() && (Rnd.nextInt(100) <= 3))
		{
			for (L2Object nearby : npc.getKnownList().getKnownObjects().values())
			{
				if ((nearby instanceof L2Attackable) && npc.isInsideRadius(nearby, collision, false, false) && (nearby != mostHate))
				{
					int newX = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
					{
						newX = mostHate.getX() + newX;
					}
					else
					{
						newX = mostHate.getX() - newX;
					}
					int newY = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
					{
						newY = mostHate.getY() + newY;
					}
					else
					{
						newY = mostHate.getY() - newY;
					}
					
					if (!npc.isInsideRadius(newX, newY, collision, false))
					{
						int newZ = npc.getZ() + 30;
						if (GeoClient.getInstance().canMoveToCoord(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ, true))
						{
							moveTo(newX, newY, newZ);
						}
					}
					return;
				}
			}
		}
		
		if (!npc.isMovementDisabled() && (npc.getCanDodge() > 0))
		{
			if (Rnd.get(100) <= npc.getCanDodge())
			{
				double distance2 = npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY());
				if (Math.sqrt(distance2) <= (60 + combinedCollision))
				{
					int posX = npc.getX();
					int posY = npc.getY();
					int posZ = npc.getZ() + 30;
					
					if (originalAttackTarget.getX() < posX)
					{
						posX = posX + 300;
					}
					else
					{
						posX = posX - 300;
					}
					
					if (originalAttackTarget.getY() < posY)
					{
						posY = posY + 300;
					}
					else
					{
						posY = posY - 300;
					}
					
					if (GeoClient.getInstance().canMoveToCoord(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ, true))
					{
						setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(posX, posY, posZ, 0));
					}
					return;
				}
			}
		}
		
		if (npc.isRaid() || npc.isRaidMinion())
		{
			chaostime++;
			if (npc instanceof L2RaidBossInstance)
			{
				if (!((L2MonsterInstance) npc).hasMinions())
				{
					if (chaostime > Config.RAID_CHAOS_TIME)
					{
						if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 100) / npc.getMaxHp())))
						{
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				}
				else
				{
					if (chaostime > Config.RAID_CHAOS_TIME)
					{
						if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 200) / npc.getMaxHp())))
						{
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				}
			}
			else if (npc instanceof L2GrandBossInstance)
			{
				if (chaostime > Config.GRAND_CHAOS_TIME)
				{
					double chaosRate = 100 - ((npc.getCurrentHp() * 300) / npc.getMaxHp());
					if (((chaosRate <= 10) && (Rnd.get(100) <= 10)) || ((chaosRate > 10) && (Rnd.get(100) <= chaosRate)))
					{
						aggroReconsider();
						chaostime = 0;
						return;
					}
				}
			}
			else
			{
				if (chaostime > Config.MINION_CHAOS_TIME)
				{
					if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 200) / npc.getMaxHp())))
					{
						aggroReconsider();
						chaostime = 0;
						return;
					}
				}
			}
		}
		
		if (!_skillrender.getGeneralskills().isEmpty())
		{
			if (!_skillrender.getHealSkills().isEmpty())
			{
				double percentage = (npc.getCurrentHp() / npc.getMaxHp()) * 100;
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if ((leader != null) && !leader.isDead() && (Rnd.get(100) > ((leader.getCurrentHp() / leader.getMaxHp()) * 100)))
					{
						for (L2Skill sk : _skillrender.getHealSkills())
						{
							if (sk.getTargetType() == L2TargetType.SELF)
							{
								continue;
							}
							if (!checkSkillCastConditions(sk))
							{
								continue;
							}
							if (!Util.checkIfInRange((sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius()), npc, leader, false) && !isParty(sk) && !npc.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius());
								return;
							}
							if (GeoClient.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								npc.setTarget(leader);
								clientStopMoving(null);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				if (Rnd.get(100) < ((100 - percentage) / 3))
				{
					for (L2Skill sk : _skillrender.getHealSkills())
					{
						if (!checkSkillCastConditions(sk))
						{
							continue;
						}
						clientStopMoving(null);
						npc.setTarget(npc);
						npc.doCast(sk);
						return;
					}
				}
				for (L2Skill sk : _skillrender.getHealSkills())
				{
					if (!checkSkillCastConditions(sk))
					{
						continue;
					}
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						for (L2Character obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision))
						{
							if (!(obj instanceof L2Attackable) || obj.isDead())
							{
								continue;
							}
							
							L2Attackable targets = ((L2Attackable) obj);
							if ((npc.getFactionId() != null) && !npc.getFactionId().equals(targets.getFactionId()))
							{
								continue;
							}
							percentage = (targets.getCurrentHp() / targets.getMaxHp()) * 100;
							if (Rnd.get(100) < ((100 - percentage) / 10))
							{
								if (GeoClient.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									npc.setTarget(obj);
									npc.doCast(sk);
									return;
								}
							}
						}
					}
					if (isParty(sk))
					{
						clientStopMoving(null);
						npc.doCast(sk);
						return;
					}
				}
			}
			
			if (!_skillrender.getResSkills().isEmpty())
			{
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if ((leader != null) && leader.isDead())
					{
						for (L2Skill sk : _skillrender.getResSkills())
						{
							if (sk.getTargetType() == L2TargetType.SELF)
							{
								continue;
							}
							if (!checkSkillCastConditions(sk))
							{
								continue;
							}
							if (!Util.checkIfInRange((sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius()), npc, leader, false) && !isParty(sk) && !npc.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius());
								return;
							}
							if (GeoClient.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								npc.setTarget(leader);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				for (L2Skill sk : _skillrender.getResSkills())
				{
					if (!checkSkillCastConditions(sk))
					{
						continue;
					}
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						for (L2Character obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision))
						{
							if (!(obj instanceof L2Attackable) || !obj.isDead())
							{
								continue;
							}
							
							L2Attackable targets = ((L2Attackable) obj);
							if ((npc.getFactionId() != null) && !npc.getFactionId().equals(targets.getFactionId()))
							{
								continue;
							}
							if (Rnd.get(100) < 10)
							{
								if (GeoClient.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									npc.setTarget(obj);
									npc.doCast(sk);
									return;
								}
							}
						}
					}
					if (isParty(sk))
					{
						clientStopMoving(null);
						L2Object target = getAttackTarget();
						npc.setTarget(npc);
						npc.doCast(sk);
						npc.setTarget(target);
						return;
					}
				}
			}
		}
		
		double dist = Math.sqrt(npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY()));
		int dist2 = (int) dist - collision;
		int range = npc.getPhysicalAttackRange() + combinedCollision;
		if (mostHate.isMoving())
		{
			range = range + 50;
			if (npc.isMoving())
			{
				range = range + 50;
			}
		}
		
		if ((npc.isMovementDisabled() && ((dist > range) || mostHate.isMoving())) || ((dist > range) && mostHate.isMoving()))
		{
			movementDisable();
			return;
		}
		
		setTimepass(0);
		if (!_skillrender.getGeneralskills().isEmpty())
		{
			if (Rnd.get(100) < Rnd.get(npc.getMinSkillChance(), npc.getMaxSkillChance()))
			{
				L2Skill skills = _skillrender.getGeneralskills().get(Rnd.nextInt(_skillrender.getGeneralskills().size()));
				if (cast(skills))
				{
					return;
				}
				for (L2Skill sk : _skillrender.getGeneralskills())
				{
					if (cast(sk))
					{
						return;
					}
				}
			}
			
			if (npc.hasLSkill() || npc.hasSSkill())
			{
				final List<L2Skill> shortRangeSkills = shortRangeSkillRender();
				if (!shortRangeSkills.isEmpty() && npc.hasSSkill() && (dist2 <= 150) && (Rnd.get(100) <= npc.getSSkillChance()))
				{
					final L2Skill shortRangeSkill = shortRangeSkills.get(Rnd.get(shortRangeSkills.size()));
					if ((shortRangeSkill != null) && cast(shortRangeSkill))
					{
						return;
					}
					for (L2Skill sk : shortRangeSkills)
					{
						if ((sk != null) && cast(sk))
						{
							return;
						}
					}
				}
				
				final List<L2Skill> longRangeSkills = longRangeSkillRender();
				if (!longRangeSkills.isEmpty() && npc.hasLSkill() && (dist2 > 150) && (Rnd.get(100) <= npc.getLSkillChance()))
				{
					final L2Skill longRangeSkill = longRangeSkills.get(Rnd.get(longRangeSkills.size()));
					if ((longRangeSkill != null) && cast(longRangeSkill))
					{
						return;
					}
					for (L2Skill sk : longRangeSkills)
					{
						if ((sk != null) && cast(sk))
						{
							return;
						}
					}
				}
			}
		}
		
		if ((dist2 > range) || !GeoClient.getInstance().canSeeTarget(npc, mostHate))
		{
			if (npc.isMovementDisabled())
			{
				targetReconsider();
			}
			else if (getAttackTarget() != null)
			{
				if (getAttackTarget().isMoving())
				{
					range -= 100;
				}
				
				if (range < 5)
				{
					range = 5;
				}
				moveToPawn(getAttackTarget(), range);
			}
			return;
		}
		melee(npc.getPrimarySkillId());
	}
	
	private void melee(int type)
	{
		if (type != 0)
		{
			switch (type)
			{
				case -1:
				{
					if (_skillrender.getGeneralskills() != null)
					{
						for (L2Skill sk : _skillrender.getGeneralskills())
						{
							if (cast(sk))
							{
								return;
							}
						}
					}
					break;
				}
				case 1:
				{
					for (L2Skill sk : _skillrender.getAtkSkills())
					{
						if (cast(sk))
						{
							return;
						}
					}
					break;
				}
				default:
				{
					for (L2Skill sk : _skillrender.getGeneralskills())
					{
						if (sk.getId() == getActiveChar().getPrimarySkillId())
						{
							if (cast(sk))
							{
								return;
							}
						}
					}
					break;
				}
			}
		}
		_accessor.doAttack(getAttackTarget());
	}
	
	private boolean cast(L2Skill sk)
	{
		if (sk == null)
		{
			return false;
		}
		
		final L2Attackable caster = getActiveChar();
		
		if (caster.isCastingNow() && !sk.isSimultaneousCast())
		{
			return false;
		}
		
		if (!checkSkillCastConditions(sk))
		{
			return false;
		}
		if (getAttackTarget() == null)
		{
			if (caster.getMostHated() != null)
			{
				setAttackTarget(caster.getMostHated());
			}
		}
		L2Character attackTarget = getAttackTarget();
		if (attackTarget == null)
		{
			return false;
		}
		double dist = Math.sqrt(caster.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
		double dist2 = dist - attackTarget.getTemplate().getCollisionRadius();
		double range = caster.getPhysicalAttackRange() + caster.getTemplate().getCollisionRadius() + attackTarget.getTemplate().getCollisionRadius();
		double srange = sk.getCastRange() + caster.getTemplate().getCollisionRadius();
		if (attackTarget.isMoving())
		{
			dist2 = dist2 - 30;
		}
		
		switch (sk.getSkillType())
		{
		
			case BUFF:
			{
				if (caster.getFirstEffect(sk) == null)
				{
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					return true;
				}
				
				if (sk.getTargetType() == L2TargetType.SELF)
				{
					return false;
				}
				
				if (sk.getTargetType() == L2TargetType.ONE)
				{
					L2Character target = effectTargetReconsider(sk, true);
					if (target != null)
					{
						clientStopMoving(null);
						L2Object targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				
				if (canParty(sk))
				{
					clientStopMoving(null);
					L2Object targets = attackTarget;
					caster.setTarget(caster);
					caster.doCast(sk);
					caster.setTarget(targets);
					return true;
				}
				break;
			}
			case RESURRECT:
			{
				if (!isParty(sk))
				{
					if (caster.isMinion() && (sk.getTargetType() != L2TargetType.SELF))
					{
						L2Character leader = caster.getLeader();
						if ((leader != null) && leader.isDead())
						{
							if (!Util.checkIfInRange((sk.getCastRange() + caster.getTemplate().getCollisionRadius() + leader.getTemplate().getCollisionRadius()), caster, leader, false) && !isParty(sk) && !caster.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + caster.getTemplate().getCollisionRadius() + leader.getTemplate().getCollisionRadius());
							}
						}
						if (GeoClient.getInstance().canSeeTarget(caster, leader))
						{
							clientStopMoving(null);
							caster.setTarget(leader);
							caster.doCast(sk);
							return true;
						}
					}
					
					for (L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().getCollisionRadius()))
					{
						if (!(obj instanceof L2Attackable) || !obj.isDead())
						{
							continue;
						}
						
						L2Attackable targets = ((L2Attackable) obj);
						if ((caster.getFactionId() != null) && !caster.getFactionId().equals(targets.getFactionId()))
						{
							continue;
						}
						if (Rnd.get(100) < 10)
						{
							if (GeoClient.getInstance().canSeeTarget(caster, targets))
							{
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				else if (isParty(sk))
				{
					for (L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getAffectRange() + caster.getTemplate().getCollisionRadius()))
					{
						if (!(obj instanceof L2Attackable))
						{
							continue;
						}
						L2Npc targets = ((L2Npc) obj);
						if ((caster.getFactionId() != null) && caster.getFactionId().equals(targets.getFactionId()))
						{
							if ((obj.getCurrentHp() < obj.getMaxHp()) && (Rnd.get(100) <= 20))
							{
								clientStopMoving(null);
								caster.setTarget(caster);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				break;
			}
			case DEBUFF:
			case POISON:
			case DOT:
			case MDOT:
			case BLEED:
			{
				if (GeoClient.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && !attackTarget.isDead() && (dist2 <= srange))
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA) || (sk.getTargetType() == L2TargetType.AURA_CORPSE_MOB))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					if (((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA)) && GeoClient.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == L2TargetType.ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			case SLEEP:
			{
				if (sk.getTargetType() == L2TargetType.ONE)
				{
					if (!attackTarget.isDead() && (dist2 <= srange))
					{
						if ((dist2 > range) || attackTarget.isMoving())
						{
							if (attackTarget.getFirstEffect(sk) == null)
							{
								clientStopMoving(null);
								caster.doCast(sk);
								return true;
							}
						}
					}
					
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					if (((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA)) && GeoClient.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			case ROOT:
			case STUN:
			case PARALYZE:
			case MUTE:
			case FEAR:
			{
				if (GeoClient.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && (dist2 <= srange))
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					if (((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA)) && GeoClient.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == L2TargetType.ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			case PDAM:
			case MDAM:
			case BLOW:
			case DRAIN:
			case CHARGEDAM:
			case FATAL:
			case DEATHLINK:
			case MANADAM:
			case CPDAMPERCENT:
			{
				if (!canAura(sk))
				{
					if (GeoClient.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					L2Character target = skillTargetReconsider(sk);
					if (target != null)
					{
						clientStopMoving(null);
						L2Object targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				else
				{
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}
				break;
			}
			default:
			{
				if (sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.CANCEL_ALL, L2EffectType.NEGATE))
				{
					if (Rnd.get(50) != 0)
					{
						return true;
					}
					
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						if ((attackTarget.getFirstEffect(L2EffectType.BUFF) != null) && GeoClient.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
						{
							clientStopMoving(null);
							caster.doCast(sk);
							return true;
						}
						L2Character target = effectTargetReconsider(sk, false);
						if (target != null)
						{
							clientStopMoving(null);
							L2Object targets = attackTarget;
							caster.setTarget(target);
							caster.doCast(sk);
							caster.setTarget(targets);
							return true;
						}
					}
					else if (canAOE(sk))
					{
						if (((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA)) && GeoClient.getInstance().canSeeTarget(caster, attackTarget))
						
						{
							clientStopMoving(null);
							caster.doCast(sk);
							return true;
						}
						else if (((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA)) && GeoClient.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
						{
							clientStopMoving(null);
							caster.doCast(sk);
							return true;
						}
					}
				}
				if (sk.hasEffectType(L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
				{
					double percentage = (caster.getCurrentHp() / caster.getMaxHp()) * 100;
					if (caster.isMinion() && (sk.getTargetType() != L2TargetType.SELF))
					{
						L2Character leader = caster.getLeader();
						if ((leader != null) && !leader.isDead() && (Rnd.get(100) > ((leader.getCurrentHp() / leader.getMaxHp()) * 100)))
						{
							if (!Util.checkIfInRange((sk.getCastRange() + caster.getTemplate().getCollisionRadius() + leader.getTemplate().getCollisionRadius()), caster, leader, false) && !isParty(sk) && !caster.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + caster.getTemplate().getCollisionRadius() + leader.getTemplate().getCollisionRadius());
							}
							if (GeoClient.getInstance().canSeeTarget(caster, leader))
							{
								clientStopMoving(null);
								caster.setTarget(leader);
								caster.doCast(sk);
								return true;
							}
						}
					}
					if (Rnd.get(100) < ((100 - percentage) / 3))
					{
						clientStopMoving(null);
						caster.setTarget(caster);
						caster.doCast(sk);
						return true;
					}
					
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						for (L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().getCollisionRadius()))
						{
							if (!(obj instanceof L2Attackable) || obj.isDead())
							{
								continue;
							}
							
							L2Attackable targets = ((L2Attackable) obj);
							if ((caster.getFactionId() != null) && !caster.getFactionId().equals(targets.getFactionId()))
							{
								continue;
							}
							percentage = (targets.getCurrentHp() / targets.getMaxHp()) * 100;
							if (Rnd.get(100) < ((100 - percentage) / 10))
							{
								if (GeoClient.getInstance().canSeeTarget(caster, targets))
								{
									clientStopMoving(null);
									caster.setTarget(obj);
									caster.doCast(sk);
									return true;
								}
							}
						}
					}
					if (isParty(sk))
					{
						for (L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getAffectRange() + caster.getTemplate().getCollisionRadius()))
						{
							if (!(obj instanceof L2Attackable))
							{
								continue;
							}
							L2Npc targets = ((L2Npc) obj);
							if ((caster.getFactionId() != null) && targets.getFactionId().equals(caster.getFactionId()))
							{
								if ((obj.getCurrentHp() < obj.getMaxHp()) && (Rnd.get(100) <= 20))
								{
									clientStopMoving(null);
									caster.setTarget(caster);
									caster.doCast(sk);
									return true;
								}
							}
						}
					}
				}
				if (!canAura(sk))
				{
					if (GeoClient.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					L2Character target = skillTargetReconsider(sk);
					if (target != null)
					{
						clientStopMoving(null);
						L2Object targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				else
				{
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}
				
			}
				break;
		}
		return false;
	}
	
	private void movementDisable()
	{
		final L2Attackable npc = getActiveChar();
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		try
		{
			if (npc.getTarget() == null)
			{
				npc.setTarget(getAttackTarget());
			}
			dist = Math.sqrt(npc.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
			dist2 = dist - npc.getTemplate().getCollisionRadius();
			range = npc.getPhysicalAttackRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
			if (getAttackTarget().isMoving())
			{
				dist = dist - 30;
				if (npc.isMoving())
				{
					dist = dist - 50;
				}
			}
			
			if (!_skillrender.getGeneralskills().isEmpty())
			{
				int random = Rnd.get(100);
				if (!_skillrender.getImmobiliseSkills().isEmpty() && !getAttackTarget().isImmobilized() && (random < 2))
				{
					for (L2Skill sk : _skillrender.getImmobiliseSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoClient.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if (getAttackTarget().getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							npc.doCast(sk);
							return;
						}
					}
				}
				
				if (!_skillrender.getCostOverTimeSkills().isEmpty() && (random < 5))
				{
					for (L2Skill sk : _skillrender.getCostOverTimeSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoClient.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if (getAttackTarget().getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							npc.doCast(sk);
							return;
						}
					}
				}
				
				if (!_skillrender.getDebuffSkills().isEmpty() && (random < 8))
				{
					for (L2Skill sk : _skillrender.getDebuffSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoClient.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if (getAttackTarget().getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							npc.doCast(sk);
							return;
						}
					}
				}
				
				if (!_skillrender.getNegativeSkills().isEmpty() && (random < 9))
				{
					for (L2Skill sk : _skillrender.getNegativeSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoClient.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
						{
							clientStopMoving(null);
							npc.doCast(sk);
							return;
						}
					}
				}
				
				if (!_skillrender.getAtkSkills().isEmpty() && (npc.isMovementDisabled() || (npc.getAiType() == AIType.MAGE) || (npc.getAiType() == AIType.HEALER)))
				{
					for (L2Skill sk : _skillrender.getAtkSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoClient.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						clientStopMoving(null);
						npc.doCast(sk);
						return;
					}
				}
			}
			
			if (npc.isMovementDisabled())
			{
				targetReconsider();
				
				return;
			}
			
			if ((dist > range) || !GeoClient.getInstance().canSeeTarget(npc, getAttackTarget()))
			{
				if (getAttackTarget().isMoving())
				{
					range -= 100;
				}
				if (range < 5)
				{
					range = 5;
				}
				moveToPawn(getAttackTarget(), range);
				return;
				
			}
			melee(npc.getPrimarySkillId());
		}
		catch (NullPointerException e)
		{
			setIntention(AI_INTENTION_ACTIVE);
			_log.warning(getClass().getSimpleName() + ": " + this + " - failed executing movementDisable(): " + e.getMessage());
			return;
		}
	}
	
	private boolean checkSkillCastConditions(L2Skill skill)
	{
		if (skill.getMpConsume() >= getActiveChar().getCurrentMp())
		{
			return false;
		}
		
		if (getActiveChar().isSkillDisabled(skill))
		{
			return false;
		}
		
		if (!skill.isStatic() && ((skill.isMagic() && getActiveChar().isMuted()) || getActiveChar().isPhysicalMuted()))
		{
			return false;
		}
		return true;
	}
	
	private L2Character effectTargetReconsider(L2Skill sk, boolean positive)
	{
		if (sk == null)
		{
			return null;
		}
		L2Attackable actor = getActiveChar();
		if (!sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.CANCEL_ALL, L2EffectType.NEGATE))
		{
			if (!positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;
				
				for (L2Character obj : actor.getAttackByList())
				{
					if ((obj == null) || obj.isDead() || !GeoClient.getInstance().canSeeTarget(actor, obj) || (obj == getAttackTarget()))
					{
						continue;
					}
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().getCollisionRadius();
						range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
						if (obj.isMoving())
						{
							dist2 = dist2 - 70;
						}
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (dist2 <= range)
					{
						if (getAttackTarget().getFirstEffect(sk) == null)
						{
							return obj;
						}
					}
				}
				
				for (L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if (obj.isDead() || !GeoClient.getInstance().canSeeTarget(actor, obj))
					{
						continue;
					}
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist;
						range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
						if (obj.isMoving())
						{
							dist2 = dist2 - 70;
						}
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (obj instanceof L2Attackable)
					{
						if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
						{
							if (dist2 <= range)
							{
								if (getAttackTarget().getFirstEffect(sk) == null)
								{
									return obj;
								}
							}
						}
					}
					if ((obj instanceof L2PcInstance) || (obj instanceof L2Summon))
					{
						if (dist2 <= range)
						{
							if (getAttackTarget().getFirstEffect(sk) == null)
							{
								return obj;
							}
						}
					}
				}
			}
			else if (positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;
				for (L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if (!(obj instanceof L2Attackable) || obj.isDead() || !GeoClient.getInstance().canSeeTarget(actor, obj))
					{
						continue;
					}
					
					L2Attackable targets = ((L2Attackable) obj);
					if ((actor.getFactionId() != null) && !actor.getFactionId().equals(targets.getFactionId()))
					{
						continue;
					}
					
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().getCollisionRadius();
						range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
						if (obj.isMoving())
						{
							dist2 = dist2 - 70;
						}
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (dist2 <= range)
					{
						if (obj.getFirstEffect(sk) == null)
						{
							return obj;
						}
					}
				}
			}
		}
		else
		{
			double dist = 0;
			double dist2 = 0;
			int range = 0;
			range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
			for (L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
			{
				if ((obj == null) || obj.isDead() || !GeoClient.getInstance().canSeeTarget(actor, obj))
				{
					continue;
				}
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().getCollisionRadius();
					range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
					if (obj.isMoving())
					{
						dist2 = dist2 - 70;
					}
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (obj instanceof L2Attackable)
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						if (dist2 <= range)
						{
							if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
							{
								return obj;
							}
						}
					}
				}
				if ((obj instanceof L2PcInstance) || (obj instanceof L2Summon))
				{
					if (dist2 <= range)
					{
						if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
						{
							return obj;
						}
					}
				}
			}
		}
		return null;
	}
	
	private L2Character skillTargetReconsider(L2Skill sk)
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActiveChar();
		if (actor.getHateList() != null)
		{
			for (L2Character obj : actor.getHateList())
			{
				if ((obj == null) || !GeoClient.getInstance().canSeeTarget(actor, obj) || obj.isDead())
				{
					continue;
				}
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().getCollisionRadius();
					range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (dist2 <= range)
				{
					return obj;
				}
			}
		}
		
		if (!(actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(target.getX(), target.getY()));
					dist2 = dist;
					range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
				}
				catch (NullPointerException e)
				{
					continue;
				}
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				if ((obj == null) || !GeoClient.getInstance().canSeeTarget(actor, obj) || (dist2 > range))
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
				{
					return obj;
					
				}
				if (obj instanceof L2Attackable)
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						return obj;
					}
					if (actor.getIsChaos() != 0)
					{
						if ((((L2Attackable) obj).getFactionId() != null) && ((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}
						
						return obj;
					}
				}
				if (obj instanceof L2Summon)
				{
					return obj;
				}
			}
		}
		return null;
	}
	
	private void targetReconsider()
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActiveChar();
		L2Character MostHate = actor.getMostHated();
		if (actor.getHateList() != null)
		{
			for (L2Character obj : actor.getHateList())
			{
				if ((obj == null) || !GeoClient.getInstance().canSeeTarget(actor, obj) || obj.isDead() || (obj != MostHate) || (obj == actor))
				{
					continue;
				}
				try
				{
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().getCollisionRadius();
					range = actor.getPhysicalAttackRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
					if (obj.isMoving())
					{
						dist2 = dist2 - 70;
					}
				}
				catch (NullPointerException e)
				{
					continue;
				}
				
				if (dist2 <= range)
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
					return;
				}
			}
		}
		if (!(actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				
				if ((obj == null) || !GeoClient.getInstance().canSeeTarget(actor, obj) || obj.isDead() || (obj != MostHate) || (obj == actor) || (obj == getAttackTarget()))
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
					
				}
				else if (obj instanceof L2Attackable)
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0)
					{
						if ((((L2Attackable) obj).getFactionId() != null) && ((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}
						
						if (MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
						setAttackTarget(obj);
					}
				}
				else if (obj instanceof L2Summon)
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}
	
	private void aggroReconsider()
	{
		L2Attackable actor = getActiveChar();
		L2Character MostHate = actor.getMostHated();
		
		if (actor.getHateList() != null)
		{
			
			int rand = Rnd.get(actor.getHateList().size());
			int count = 0;
			for (L2Character obj : actor.getHateList())
			{
				if (count < rand)
				{
					count++;
					continue;
				}
				
				if ((obj == null) || !GeoClient.getInstance().canSeeTarget(actor, obj) || obj.isDead() || (obj == getAttackTarget()) || (obj == actor))
				{
					continue;
				}
				
				try
				{
					actor.setTarget(getAttackTarget());
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (MostHate != null)
				{
					actor.addDamageHate(obj, 0, actor.getHating(MostHate));
				}
				else
				{
					actor.addDamageHate(obj, 0, 2000);
				}
				actor.setTarget(obj);
				setAttackTarget(obj);
				return;
			}
		}
		
		if (!(actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				else
				{
					continue;
				}
				if (!GeoClient.getInstance().canSeeTarget(actor, obj) || obj.isDead() || (obj != MostHate) || (obj == actor))
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
				{
					if ((MostHate != null) && !MostHate.isDead())
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						if (MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0)
					{
						if ((((L2Attackable) obj).getFactionId() != null) && ((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}
						
						if (MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
						setAttackTarget(obj);
					}
				}
				else if (obj instanceof L2Summon)
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}
	
	private List<L2Skill> longRangeSkillRender()
	{
		longRangeSkills = _skillrender.getLongRangeSkills();
		if (longRangeSkills.isEmpty())
		{
			longRangeSkills = getActiveChar().getLongRangeSkill();
		}
		return longRangeSkills;
	}
	
	private List<L2Skill> shortRangeSkillRender()
	{
		shortRangeSkills = _skillrender.getShortRangeSkills();
		if (shortRangeSkills.isEmpty())
		{
			shortRangeSkills = getActiveChar().getShortRangeSkill();
		}
		return shortRangeSkills;
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking || getActiveChar().isAllSkillsDisabled())
		{
			return;
		}
		
		_thinking = true;
		
		try
		{
			switch (getIntention())
			{
				case AI_INTENTION_ACTIVE:
					thinkActive();
					break;
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
			}
		}
		catch (Exception e)
		{
			_log.warning(getClass().getSimpleName() + ": " + this + " -  onEvtThink() failed: " + e.getMessage());
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		L2Attackable me = getActiveChar();
		
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
		
		L2PcInstance player = me.getActingPlayer();
		if (player != null)
		{
			if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
			{
				if (!player.isGM() && (SevenSigns.getInstance().getPlayerCabal(player.getObjectId()) != SevenSigns.getInstance().getCabalHighestScore()) && me.isSevenSignsMonster())
				{
					player.teleToLocation(TeleportWhereType.TOWN);
					player.sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
					return;
				}
			}
			else
			{
				if (!player.isGM() && (SevenSigns.getInstance().getPlayerCabal(player.getObjectId()) == SevenSigns.CABAL_NULL) && me.isSevenSignsMonster())
				{
					player.teleToLocation(TeleportWhereType.TOWN);
					player.sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
					return;
				}
			}
		}
		
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}
		
		me.addDamageHate(attacker, 0, 1);
		
		if (!me.isRunning())
		{
			if (me.isEkimusFood())
			{
				me.setWalking();
			}
			else
			{
				me.setRunning();
			}
		}
		
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		else if (me.getMostHated() != getAttackTarget())
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		
		if (me instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) me;
			
			if (master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
			}
			
			master = master.getLeader();
			if ((master != null) && master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
			}
		}
		super.onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		L2Attackable me = getActiveChar();
		
		if (target != null)
		{
			me.addDamageHate(target, 0, aggro);
			
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (!me.isRunning())
				{
					if (me.isEkimusFood())
					{
						me.setWalking();
					}
					else
					{
						me.setRunning();
					}
				}
				
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			
			if (me instanceof L2MonsterInstance)
			{
				L2MonsterInstance master = (L2MonsterInstance) me;
				
				if (master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}
				
				master = master.getLeader();
				if ((master != null) && master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}
			}
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		_attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}
	
	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}
	
	public void setTimepass(int TP)
	{
		timepass = TP;
	}
	
	public int getTimepass()
	{
		return timepass;
	}
	
	public L2Attackable getActiveChar()
	{
		return (L2Attackable) _actor;
	}
}